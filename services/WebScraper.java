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
     * Connects to a URL, scrapes the main article text, and returns it.
     *
     * @param url The URL of the page to scrape.
     * @return The text content of the page, or null if an error occurs.
     */
    public String scrapeUrl(String url) {
        try {
            // Connect to the URL and get the HTML document.
            Document doc = Jsoup.connect(url).get();
            // Select the main content area of a Wikipedia page (#mw-content-text).
            Element contentDiv = doc.selectFirst("#mw-content-text");

            if (contentDiv != null) {
                Element textBody = contentDiv.selectFirst(".mw-parser-output");
                if (textBody != null) {
                    // Return the text content, converted to lowercase for consistent counting.
                    return textBody.text().toLowerCase();
                }
            }

            // If the main content area can't be found, return null.
            System.err.println("  - Could not find main content on page: " + url);
            return null;

        } catch (IOException e) {
            // If there's a connection or network error, print a message and return null.
            System.err.println("  - Error connecting to " + url + ": " + e.getMessage());
            return null;
        }
    }
}
