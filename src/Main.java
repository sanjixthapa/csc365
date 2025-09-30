import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main extends JFrame {
    private record WebPage(String title, HT tfidfScores) {}//stores tfidf & title for each url

    private final List<WebPage> pages = new ArrayList<>();//list of tfidf & title for each url
    private JComboBox<String> dropdown;
    private JTextArea results;

    private final HT wordInDoc = new HT();//# of urls a word appears in
    private int totalDocs = 0;

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
        dropdown = new JComboBox<>();
        top.add(dropdown);

        JButton btn = new JButton("Find Similar");
        btn.addActionListener(_ -> findSimilar());
        top.add(btn);
        add(top, BorderLayout.NORTH);

        results = new JTextArea();
        results.setEditable(false);
        add(results, BorderLayout.CENTER);
    }

    private void loadPages() {
        List<HT> allWordCounts = new ArrayList<>(); //list of word counts for each page
        List<String> wikiTitles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/urls"))) {
            String url;
            while ((url = reader.readLine()) != null) {
                if (url.trim().isEmpty()) continue;
                try {
                    Document doc = Jsoup.connect(url.trim()).get();
                    HT wordCounts = countWords(doc.body().text());//count words in a page
                    allWordCounts.add(wordCounts);//store
                    wikiTitles.add(doc.title());
                    totalDocs++;
                    //# of articles each unique word appears in
                    for (HT.Node bucket : wordCounts.table) {
                        for (HT.Node node = bucket; node != null; node = node.next) {
                            String word = (String) node.key;
                            wordInDoc.add(word);
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

        for (int i = 0; i < allWordCounts.size(); i++) {
            //for each article calculate its tfidf scores
            HT tfidfScores = calculateTFIDF(allWordCounts.get(i));
            pages.add(new WebPage(wikiTitles.get(i), tfidfScores));
            dropdown.addItem(wikiTitles.get(i));
        }
        results.setText(" Select page and click Find Similar");
    }

    private HT countWords(String text) {
        HT counts = new HT();
        String[] words = text.toLowerCase()
                .replaceAll("[^a-z ]", "").split("\\s+");
        for (String word : words) {
            counts.add(word);
        }
        return counts;
    }

    private HT calculateTFIDF(HT wordCounts) {
        HT tfidf = new HT();
        int totalWords = 0;
        for (HT.Node bucket : wordCounts.table) {//traverses array
            for (HT.Node node = bucket; node != null; node = node.next) {//traverses linked list
                totalWords = totalWords + node.count;
            }
        }
        //tfidf for each word in the wiki
        for (HT.Node bucket : wordCounts.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                String word = (String) node.key;
                int wordFreq =  node.count;
                int docsWithWord = wordInDoc.getCount(word);
                double tf = (double) wordFreq / totalWords;
                double idf = Math.log((double) totalDocs / docsWithWord);
                double tfidfScore = tf * idf;
                tfidf.add(word, tfidfScore);//add word and its score to map
            }
        }
        return tfidf;
    }

    private void findSimilar() {
        WebPage selected = pages.get(dropdown.getSelectedIndex());
        WebPage best = null, second = null;
        double bestScore = 0, secondScore = 0;
        for (WebPage other : pages) {
            if (other == selected) continue; //dont compare page to itself
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
            result.append(String.format(" 1. %s\n Score: %.3f\n\n", best.title, bestScore));
        }
        if (second != null) {
            result.append(String.format(" 2. %s\n Score: %.3f", second.title, secondScore));
        }
        results.setText(String.valueOf(result));
    }

    private double similarity(HT tfidf1, HT tfidf2) {
        double dot = 0;
        double mag1 = 0;
        double mag2 = 0;
        double finalScore;
        //iterates through all the words in wiki tfidf
        for (HT.Node bucket : tfidf1.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                double score1 = (Double) node.value;
                // get score for same word from second wiki
                Double score2Obj = (Double) tfidf2.get(node.key);
                //if the word doesnt exist in the second article, score2 = 0
                double score2 = (score2Obj == null) ? 0 : score2Obj;
                dot = dot + (score1 * score2);
                mag1 = mag1 + (score1 * score1);
            }
        }
        //iterates through all words in the second doc
        for (HT.Node bucket : tfidf2.table) {
            for (HT.Node node = bucket; node != null; node = node.next) {
                double score2 = (Double) node.value;
                mag2 = mag2 + (score2 * score2);
            }
        }
        if (mag1 == 0 || mag2 == 0) return 0;//zero div error
        finalScore = dot / (Math.sqrt(mag1) * Math.sqrt(mag2));//final formula
        return finalScore;
    }
}