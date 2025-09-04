package ui;

import services.WebScraper;
import logic.TextProcessor;
import logic.SimilarityCalculator;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

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
    }

    public void run() {
        System.out.println("Loading pages...");
        loadPages();
        if (urls.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pages loaded! Check urls.txt file.");
            return;
        }
        for (String title : titles) {
            pageSelector.addItem(title);
        }
        setVisible(true);
        System.out.println("Ready! Loaded " + urls.size() + " pages.");
    }

    private void setupUI() {
        setTitle("Wikipedia Recommender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

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
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Arial", Font.PLAIN, 12));
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);
    }

    private void loadPages() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/urls.txt"))) {
            String url;
            while ((url = reader.readLine()) != null) {
                url = url.trim();
                if (url.isEmpty()) continue;

                System.out.println("Loading: " + url);
                String content = scraper.scrapeUrl(url);
                if (content != null) {
                    String title = scraper.getTitleFromUrl(url);
                    Map<String, Integer> freq = processor.getWordFrequencies(content);

                    urls.add(url);
                    titles.add(title);
                    frequencies.add(freq);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void findSimilarPages() {
        int selectedIndex = pageSelector.getSelectedIndex();
        if (selectedIndex < 0) {
            resultsArea.setText("Please select a page first.");
            return;
        }

        List<Integer> similarIndices = calculator.findTopSimilar(selectedIndex, frequencies);

        StringBuilder result = new StringBuilder();
        result.append("Selected: ").append(titles.get(selectedIndex)).append("\n\n");
        result.append("Similar pages:\n");

        for (int i = 0; i < similarIndices.size(); i++) {
            int index = similarIndices.get(i);
            double similarity = calculator.calculateSimilarity(
                    frequencies.get(selectedIndex),
                    frequencies.get(index)
            );

            result.append(String.format("%d. %s (%.3f similarity)\n",
                    i + 1, titles.get(index), similarity));
            result.append("   ").append(urls.get(index)).append("\n\n");
        }

        if (similarIndices.isEmpty()) {
            result.append("No similar pages found.");
        }

        resultsArea.setText(result.toString());
    }
}