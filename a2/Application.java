import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Application extends JFrame {
    private List<WebData> pages;
    private int[] clusters;
    private HT[] centroids;
    private TfidfCalculator tfidfCalc;
    private JTextArea inputArea;
    private JTextArea resultsArea;

    public static void main() {
        SwingUtilities.invokeLater(() -> new Application().setVisible(true));
    }

    public Application() {
        loadData();
        setupGUI();
    }

    private void loadData() {
        try {
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream("data/clusters.dat"))) {
                clusters = (int[]) input.readObject();
            }
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream("data/centroids.dat"))) {
                centroids = (HT[]) input.readObject();
            }
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream("data/tfidf.dat"))) {
                tfidfCalc = (TfidfCalculator) input.readObject();
            }
            pages = new ArrayList<>();
            for (int i = 0; i < clusters.length; i++) {
                try (ObjectInputStream input = new ObjectInputStream(new FileInputStream("data/page_" + i + ".dat"))) {
                    pages.add((WebData) input.readObject());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not load data files.\nRun Loader.java first!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setupGUI() {
        setTitle("Wikipedia Recommender");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea();
        JButton findButton = new JButton("Find Similar");
        topPanel.add(new JLabel("Enter new page text:"), BorderLayout.NORTH);
        topPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        topPanel.add(findButton, BorderLayout.EAST);

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        findButton.addActionListener(_ -> findSimilarPages());
    }

    private void findSimilarPages() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            resultsArea.setText("Please enter text.");
            return;
        }
        HT tfidfVector = tfidfCalc.computeTfidfForText(text);
        int clusterIdx = nearestCluster(tfidfVector);
        List<Result> results = new ArrayList<>();

        for (int i = 0; i < pages.size(); i++) {
            if (clusters[i] == clusterIdx) {
                double sim = Similarity.cosineSimilarity(tfidfVector, pages.get(i).tfidfVector);
                results.add(new Result(pages.get(i), sim, clusterIdx));
            }
        }
        results.sort((a, b) -> Double.compare(b.score, a.score));
        displayResults(clusterIdx, results);
    }

    private int nearestCluster(HT tfidfVector) {
        int best = 0;
        double bestSim = -1;
        for (int i = 0; i < centroids.length; i++) {
            double sim = Similarity.cosineSimilarity(tfidfVector, centroids[i]);
            if (sim > bestSim) {
                bestSim = sim;
                best = i;
            }
        }
        return best;
    }

    private void displayResults(int cluster, List<Result> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nearest Cluster: ").append(cluster).append("\n\n");
        sb.append("Most Similar Pages:\n\n");
        for (int i = 0; i < Math.min(2, results.size()); i++) {
            Result r = results.get(i);
            sb.append((i + 1)).append(". ").append(r.page.pageTitle).append("\n");
            sb.append("   Score: ").append(String.format("%.4f", r.score)).append("\n\n");
        }
        resultsArea.setText(sb.toString());
    }
    record Result(WebData page, double score, int cluster) {}
}
