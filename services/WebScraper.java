package services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * A utility class for scraping text content from a single URL.
 * It is designed to be reusable and handle web connection errors gracefully.
 */
public class WebScraper {

    /**
     * Connects to a URL, scrapes the main article text, and returns it as a
     * clean string containing only lowercase letters and spaces.
     *
     * @param url The URL of the page to scrape.
     * @return The cleaned text content of the page, or null if an error occurs.
     */
    public String scrapeUrl(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element textBody = doc.selectFirst("#mw-content-text");

            if (textBody != null) {
                return textBody.text().toLowerCase();

            } else {
                System.err.println("  - Could not find main content on page: " + url);
                return null;
            }

        } catch (IOException e) {
            System.err.println("  - Error connecting to " + url + ": " + e.getMessage());
            return null;
        }
    }
}

