import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private final List<WebPage> pages = new ArrayList<>();
    private JComboBox<String> pageSelector;
    private JTextArea resultsArea;

    // Simplified stop words - just the most common ones
    private static final Set<String> STOP_WORDS = Set.of
            ("the", "and", "or", "in", "on", "at", "to", "for", "of", "with", "by");

    private record WebPage(String title, HT wordFreqs) {}

    public static void main() {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    public Main() {
        setupUI();
        loadPages();
    }

    private void setupUI() {
        setTitle("Wikipedia Recommender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);

        JPanel top = new JPanel();
        top.add(new JLabel("Page:"));
        pageSelector = new JComboBox<>();
        top.add(pageSelector);

        JButton findBtn = new JButton("Find Similar");
        findBtn.addActionListener(_ -> findSimilar());
        top.add(findBtn);

        add(top, BorderLayout.NORTH);

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);
    }

    private void loadPages() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/urls"))) {
            String url;
            while ((url = reader.readLine()) != null) {
                if (url.trim().isEmpty()) continue;

                try {
                    Document doc = Jsoup.connect(url.trim()).get();
                    String title = doc.title();
                    HT wordFreqs = countWords(doc.body().text());

                    pages.add(new WebPage(title, wordFreqs));
                    pageSelector.addItem(title);

                } catch (Exception e) {
                    System.err.println("Failed to load: " + url);
                }
            }
            resultsArea.setText("Ready! Select a page and click 'Find Similar'");

        } catch (IOException e) {
            resultsArea.setText("Error: Could not read urls.txt");
        }
    }

    private HT countWords(String text) {
        HT counts = new HT();

        String[] words = text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");

        for (String word : words) {
            if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                Integer current = (Integer) counts.get(word);
                if (current == null) counts.add(word, 1);
                else counts.add(word, current + 1);
            }
        }
        return counts;
    }

    private void findSimilar() {
        if (pages.isEmpty()) return;

        WebPage selectedPage = pages.get(pageSelector.getSelectedIndex());
        double firstScore = 0, secondScore = 0;
        WebPage firstMatch = null, secondMatch = null;

        // Find the two most similar pages
        for (WebPage otherPage : pages) {
            if (otherPage == selectedPage) continue;

            double similarity = cosineSimilarity(selectedPage.wordFreqs, otherPage.wordFreqs);
            if (similarity > firstScore) {
                secondScore = firstScore;
                secondMatch = firstMatch;
                firstScore = similarity;
                firstMatch = otherPage;
            } else if (similarity > secondScore) {
                secondScore = similarity;
                secondMatch = otherPage;
            }
        }

        // Display results
        StringBuilder result = new StringBuilder(String.format(
                "Most similar to \"%s\":\n\n", selectedPage.title));

        if (firstMatch != null) {
            result.append(String.format("1. %s\n   Similarity Score: %.3f\n\n",
                    firstMatch.title, firstScore));
        }
        if (secondMatch != null) {
            result.append(String.format("2. %s\n   Similarity Score: %.3f",
                    secondMatch.title, secondScore));
        }

        resultsArea.setText(result.toString());
    }

    private double cosineSimilarity(HT map1, HT map2) {
        double dotProduct = 0, mag1 = 0, mag2 = 0;

        // Process words in map1
        HT.Node[] table = map1.table;
        for (HT.Node bucket : table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                String word = (String) node.key;
                int count1 = (Integer) node.value;
                // Add to the magnitude of the first vector
                mag1 += count1 * count1;
                // If the same word exists in the second map, add to the dot product
                Integer count2Obj = (Integer) map2.get(word);
                if (count2Obj != null) {
                    dotProduct += count1 * count2Obj;
                }
            }
        }
        // 2. Iterate through the second map.
        // Calculate the magnitude of the second vector.
        for (HT.Node bucket : map2.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                int count2 = (Integer) node.value;
                mag2 += count2 * count2;
            }
        }
        if (mag1 == 0 || mag2 == 0) return 0;
        return dotProduct / (Math.sqrt(mag1) * Math.sqrt(mag2));
    }
}