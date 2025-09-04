package ui;

import services.WebScraper;
import logic.TextProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class RecommendationApp {

    private final WebScraper scraper;
    private final TextProcessor processor;

    public RecommendationApp() {
        this.scraper = new WebScraper();
        this.processor = new TextProcessor();
    }

    public void run() {
        // In a real app, get this path from a config file or args.
        String filePath = "src/urls.txt";
        Map<String, Integer> combinedWordFrequency = processAllUrls(filePath);
        printSortedFrequencies(combinedWordFrequency);
    }

    private Map<String, Integer> processAllUrls(String filePath) {
        Map<String, Integer> combinedWordFrequency = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String url;
            while ((url = reader.readLine()) != null) {
                if (url.trim().isEmpty()) {
                    continue;
                }
                String scrapedText = scraper.scrapeUrl(url);
                if (scrapedText != null) {
                    Map<String, Integer> pageWordFrequency = processor.getWordFrequencies(scrapedText);

                    // Merge the page's word counts into the combined map
                    pageWordFrequency.forEach((word, count) ->
                            combinedWordFrequency.put(word, combinedWordFrequency.getOrDefault(word, 0) + count));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the URL file: " + e.getMessage());
        }
        return combinedWordFrequency;
    }

    private void printSortedFrequencies(Map<String, Integer> wordFrequency) {
        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFrequency.entrySet());
        sortedWords.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (int i = 0; i < 200; i++) {
            Map.Entry<String, Integer> entry = sortedWords.get(i);
            System.out.printf("%-20s: %d%n", entry.getKey(), entry.getValue());
        }
    }
}
