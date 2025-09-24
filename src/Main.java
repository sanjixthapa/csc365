import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private final List<WebPage> pages = new ArrayList<>();
    private JComboBox<String> dropdown;
    private JTextArea results;

    // TF-IDF data
    private final HT wordDocCount = new HT(); // How many docs each word appears in
    private int totalDocs = 0;

    private record WebPage(String title, HT tfidfScores) {}

    public static void main() {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    public Main() {
        setupUI();
        loadPages();
    }

    private void setupUI() {
        // Window Configuration
        setTitle("Wikipedia Recommender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        // Top Panel with Controls
        JPanel top = new JPanel();
        top.add(new JLabel("Page:"));
        dropdown = new JComboBox<>();
        top.add(dropdown);

        JButton findBtn = new JButton("Find Similar");
        findBtn.addActionListener(_ -> findSimilar());
        top.add(findBtn);
        // Center and Bottom Components
        add(top, BorderLayout.NORTH);

        results = new JTextArea();
        results.setEditable(false);
        add(results, BorderLayout.CENTER);
    }

    private void loadPages() {
        // Step 1: Load all pages and count word occurrences
        List<HT> allWordCounts = new ArrayList<>();
        List<String> wikiTitles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/urls"))) {
            String url;
            while ((url = reader.readLine()) != null) {
                if (url.trim().isEmpty()) continue;
                try {
                    Document doc = Jsoup.connect(url.trim()).get();
                    HT wordCounts = countWords(doc.body().text());

                    allWordCounts.add(wordCounts);
                    wikiTitles.add(doc.title());
                    totalDocs++;
                    // Count how many documents each word appears in
                    for (HT.Node bucket : wordCounts.table) {
                        for (HT.Node node = bucket; node != null; node = node.next) {
                            String word = (String) node.key;
                            Integer count = (Integer) wordInDoc.get(word);
                            wordInDoc.add(word, count == null ? 1 : count + 1);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed: " + url);
                }
            }
        } catch (IOException e) {
            results.setText("Error reading urls file");
            return;
        }
        // Step 2: Calculate TF-IDF for each page
        for (int i = 0; i < allWordCounts.size(); i++) {
            HT tfidfScores = calculateTFIDF(allWordCounts.get(i));
            pages.add(new WebPage(wikiTitles.get(i), tfidfScores));
            dropdown.addItem(wikiTitles.get(i));
        }
        results.setText("Select page and click Find Similar");
    }

    private HT countWords(String text) {
        HT counts = new HT();
        String[] words = text.toLowerCase().replaceAll("[^a-z ]", "")
                .split("\\s+");
        for (String word : words) {
            Integer current = (Integer) counts.get(word);
            if (current == null) counts.add(word, 1);
            else counts.add(word, current + 1);
        }
        return counts;
    }

    private void countDocumentFrequencies(HT wordCounts) {
        for (HT.Node bucket : wordCounts.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                String word = (String) node.key;
                Integer count = (Integer) wordDocCount.get(word);
                wordDocCount.add(word, count == null ? 1 : count + 1);
            }
        }
    }

    private HT calculateTFIDF(HT wordCounts) {
        HT tfidf = new HT();
        // Get total words in this document
        int totalWords = 0;
        for (HT.Node bucket : wordCounts.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                totalWords = totalWords + (Integer) node.value;
            }
        }
        // Calculate TF-IDF for each word
        for (HT.Node bucket : wordCounts.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                String word = (String) node.key;
                int wordFreq = (Integer) node.value;
                int docsWithWord = (Integer) wordDocCount.get(word);

                // TF-IDF calculation
                double tf = (double) wordFreq / totalWords;
                double idf = Math.log((double) totalDocs / docsWithWord);
                double tfidfScore = tf * idf;

                tfidf.add(word, tfidfScore);
            }
        }
        return tfidf;
    }

    private void findSimilar() {
        if (pages.isEmpty()) return;
        WebPage selected = pages.get(dropdown.getSelectedIndex());
        WebPage best = null, second = null;
        double bestScore = 0, secondScore = 0;

        // Compare with all other pages
        for (WebPage other : pages) {
            if (other == selected) continue;
            double score = similarity(selected.tfidfScores, other.tfidfScores);
            if (score > bestScore) {
                second = best;
                secondScore = bestScore;
                best = other;
                bestScore = score;
            } else if (score > secondScore) {
                second = other;
                secondScore = score;
            }
        }

        StringBuilder result = new StringBuilder();
        result.append(" Most similar to \"").append(selected.title).append("\":\n\n");
        if (best != null) {
            result.append(" 1. ").append(best.title).append("\n   Score: ")
                    .append(String.format("%.3f", bestScore)).append("\n\n");
        }
        if (second != null) {
            result.append(" 2. ").append(second.title).append("\n   Score: ")
                    .append(String.format("%.3f", secondScore));
        }
        results.setText(String.valueOf(result));
    }

    private double similarity(HT tfidf1, HT tfidf2) {
        double dot = 0, mag1 = 0, mag2 = 0;
        // Calculate dot product and first magnitude
        for (HT.Node bucket : tfidf1.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                double score1 = (Double) node.value;
                Double score2Obj = (Double) tfidf2.get(node.key);
                double score2;
                if (score2Obj == null) score2 = 0;
                else score2 = score2Obj;

                dot = dot + (score1 * score2);
                mag1 = mag1 + (score1 * score1);
                mag2 = mag2 + (score2 * score2);
            }
        }
        // Add remaining words from the second document to mag2
        for (HT.Node bucket : tfidf2.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                if (tfidf1.get(node.key) == null) {
                    double score2 = (Double) node.value;
                    mag2 = mag2 + (score2 * score2);
                }
            }
        }
        if (mag1 == 0 || mag2 == 0) return 0;
        return dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
    }
}