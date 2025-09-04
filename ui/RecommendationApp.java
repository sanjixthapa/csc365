package ui;

import services.WebScraper;
import logic.TextProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main application class that orchestrates the scraping, processing,
 * and display of word frequency data. This class will eventually be
 * converted to a graphical user interface (GUI).
 */
public class RecommendationApp {

    private final WebScraper scraper;
    private final TextProcessor processor;

    public RecommendationApp() {
        this.scraper = new WebScraper();
        this.processor = new TextProcessor();
    }

    /**
     * The main execution method for the application.
     */
    public void run() {
        // In a real app, get this path from a config file or args.
        String filePath = "src/urls.txt";

        System.out.println("Starting web scraping process...");

        Map<String, Integer> combinedWordFrequency = processAllUrls(filePath);

        System.out.println("\n----------------------------------------------------");
        System.out.println("Final Word Frequency Table (Top 20):");
        printSortedFrequencies(combinedWordFrequency);

        System.out.println("\nWeb scraping process completed.");
    }

    /**
     * Reads URLs from a file, scrapes each one, and builds a single map of word frequencies.
     *
     * @param filePath The path to the file containing the list of URLs.
     * @return A map with each word and its total count.
     */
    private Map<String, Integer> processAllUrls(String filePath) {
        Map<String, Integer> combinedWordFrequency = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String url;
            while ((url = reader.readLine()) != null) {
                if (url.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                System.out.println("Processing URL: " + url);
                String scrapedText = scraper.scrapeUrl(url);

                if (scrapedText != null) {
                    Map<String, Integer> pageWordFrequency = processor.countWords(scrapedText);

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

    /**
     * Prints the words from the frequency map, sorted by their count in descending order.
     *
     * @param wordFrequency The map of words and their counts.
     */
    private void printSortedFrequencies(Map<String, Integer> wordFrequency) {
        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFrequency.entrySet());
        sortedWords.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int limit = Math.min(20, sortedWords.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedWords.get(i);
            System.out.printf("%-20s: %d%n", entry.getKey(), entry.getValue());
        }
    }
}
