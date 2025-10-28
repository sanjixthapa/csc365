import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Application extends JFrame {
    private List<WebData> pages;
    private int[] clusters;
    private JComboBox<String> clusterDropdown;
    private JComboBox<String> pageDropdown;
    private JTextArea resultsArea;

    public static void main() {
        SwingUtilities.invokeLater(() -> new Application().setVisible(true));
    }

    Application() {
        loadData();
        setupGUI();
    }

    void loadData() {
        try {
            try (ObjectInputStream input = new ObjectInputStream(
                    new FileInputStream("data/clusters.dat"))) {
                clusters = (int[]) input.readObject();
            }
            pages = new ArrayList<>();
            for (int i = 0; i < clusters.length; i++) {
                try (ObjectInputStream input = new ObjectInputStream(
                        new FileInputStream("data/page_" + i + ".dat"))) {
                    pages.add((WebData) input.readObject());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not load data files." +
                            "\nRun Loader.java first!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    void setupGUI() {
        setTitle("Wikipedia Recommender");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel();
        clusterDropdown = new JComboBox<>();
        pageDropdown = new JComboBox<>();
        JButton findButton = new JButton("Find Similar");
        topPanel.add(new JLabel("Cluster:"));
        topPanel.add(clusterDropdown);
        topPanel.add(new JLabel("Page:"));
        topPanel.add(pageDropdown);
        topPanel.add(findButton);

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);

        add(topPanel, BorderLayout.NORTH);
        add(resultsArea, BorderLayout.CENTER);

        populateClusterDropdown();
        clusterDropdown.addActionListener(_ -> updatePageDropdown());
        findButton.addActionListener(_ -> findSimilarPages());
        updatePageDropdown();
    }

    void populateClusterDropdown() {
        Set<Integer> uniqueClusters = new TreeSet<>();
        for (int c : clusters)
            uniqueClusters.add(c);
        for (int id : uniqueClusters)
            clusterDropdown.addItem("Cluster " + id);
    }

    void updatePageDropdown() {
        pageDropdown.removeAllItems();
        int clusterID = 0;
        String selected = (String) clusterDropdown.getSelectedItem();
        if (selected != null) {
            clusterID = Integer.parseInt(selected.split(" ")[1]);
        }
        for (int i = 0; i < pages.size(); i++) {
            if (clusters[i] == clusterID) {
                pageDropdown.addItem(pages.get(i).pageTitle);
            }
        }
    }

    void findSimilarPages() {
        String selectedTitle = (String) pageDropdown.getSelectedItem();
        int selectedIdx = -1;
        for (int i = 0; i < pages.size(); i++)
            if (pages.get(i).pageTitle.equals(selectedTitle)) {
                selectedIdx = i;
            }
        WebData selectedPage = pages.get(selectedIdx);

        List<Result> results = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++)
            if (i != selectedIdx) {
                double similarity = cosineSimilarity(selectedPage.tfidfVector, pages.get(i).tfidfVector);
                results.add(new Result(pages.get(i), similarity, clusters[i]));
            }
        results.sort((a, b) -> Double.compare(b.score, a.score));
        displayResults(selectedTitle, clusters[selectedIdx], results);
    }

    double cosineSimilarity(HT vectorA, HT vectorB) {
        double dotProduct = 0;
        double magA = 0;
        double magB = 0;

        for (HT.Node node : vectorA.nodes()) {
            if (node.value instanceof Double) {
                double valA = (Double) node.value;
                double valB;
                // Check if vectorB also has this word.
                if (vectorB.get(node.key) instanceof Double) {
                    valB = (Double) vectorB.get(node.key); // If yes, get its score
                } else {
                    valB = 0.0; // If no, its score is 0
                }
                dotProduct = dotProduct + valA * valB;
                magA = magA + valA * valA;
            }
        }
        for (HT.Node node : vectorB.nodes()) {
            if (node.value instanceof Double) {
                magB = magB + (Double) node.value * (Double) node.value;
            }
        }
        if (magA == 0 || magB == 0) return 0;
        return dotProduct / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    void displayResults(String selectedTitle, int selectedCluster, List<Result> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("Selected: ").append(selectedTitle).append("\n");
        sb.append("Cluster: ").append(selectedCluster).append("\n\n");
        sb.append("Most Similar:\n\n");

        for (int i = 0; i < 2; i++) {
            Result r = results.get(i);
            sb.append(i + 1).append(". ").append(r.page.pageTitle).append("\n");
            sb.append("   Score: ").append(String.format("%.4f", r.score));
            sb.append(" | Cluster: ").append(r.cluster).append("\n\n");
        }
        resultsArea.setText(sb.toString());
    }

    static class Result {
        WebData page;
        double score;
        int cluster;

        Result(WebData page, double score, int cluster) {
            this.page = page;
            this.score = score;
            this.cluster = cluster;
        }
    }
}
