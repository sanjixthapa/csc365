package ui;

import services.WebScraper;
import logic.TextProcessor;
import logic.SimilarityCalculator;
import logic.SimilarityCalculator.SimilarityPair;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class RecommendationApp extends JFrame {

    private final WebScraper scraper = new WebScraper();
    private final TextProcessor processor = new TextProcessor();
    private final SimilarityCalculator calculator = new SimilarityCalculator();

    private final List<String> urls = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final List<Map<String, Integer>> frequencies = new ArrayList<>();

    private JComboBox<String> pageSelector;
    private JTextArea resultsArea;

    public RecommendationApp() {
        setupUI();
        loadPages();
        populateDropdown();
    }

    private void setupUI() {
        setTitle("Wikipedia Recommender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Center the window on the screen

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select a page:"));

        pageSelector = new JComboBox<>();
        pageSelector.setPreferredSize(new Dimension(300, 25));
        topPanel.add(pageSelector);

        JButton findButton = new JButton("Find Similar");
        findButton.addActionListener(e -> findSimilarPages());
        topPanel.add(findButton);
        add(topPanel, BorderLayout.NORTH);

        resultsArea = new JTextArea();
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsArea.setEditable(false);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);
    }

    private void loadPages() {
        System.out.println("Loading pages");
        try (BufferedReader reader = new BufferedReader(new FileReader("src/urls.txt"))) {
            String url;
            while ((url = reader.readLine()) != null) {
                url = url.trim();
                if (url.isEmpty()) continue;

                System.out.println("Processing: " + url);
                String[] pageData = scraper.scrapePage(url);

                if (pageData != null) {
                    String title = pageData[0];
                    String content = pageData[1];
                    Map<String, Integer> wordCounts = processor.getWordFrequencies(content);

                    urls.add(url);
                    titles.add(title);
                    frequencies.add(wordCounts);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateDropdown() {
        if (urls.isEmpty()) {
            resultsArea.setText("No pages were loaded. Please check your urls.txt file.");
            return;
        }
        for (String title : titles) {
            pageSelector.addItem(title);
        }
        resultsArea.setText("Please select a page to find similar articles.");
        System.out.println("Ready! Loaded " + urls.size() + " pages.");
    }

    private void findSimilarPages() {
        int selectedIndex = pageSelector.getSelectedIndex();
        if (selectedIndex < 0)
            return;

        List<SimilarityPair> similarPairs = calculator.findTopSimilar(selectedIndex, frequencies);

        StringBuilder result = new StringBuilder();
        result.append("Selected: ").append(titles.get(selectedIndex)).append("\n\n");

        if (similarPairs.isEmpty()) {
            result.append("No other pages were available for comparison.");
        } else {
            for (int i = 0; i < similarPairs.size(); i++) {
                SimilarityPair pair = similarPairs.get(i);
                result.append(String.format("%d. %s (Similarity: %.3f)\n",
                        i + 1, titles.get(pair.getIndex()), pair.getSimilarity()));
                result.append("   URL: ").append(urls.get(pair.getIndex())).append("\n\n");
            }
        }
        resultsArea.setText(result.toString());
    }
}