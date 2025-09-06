package services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;

/**
 * A more efficient web scraper that fetches the title and content
 * of a web page in a single network request.
 */
public class WebScraper {

    /**
     * Scrapes a URL for both its title and main article content.
     * @param url The URL of the Wikipedia page to scrape.
     * @return A String array where:
     * - Index 0 is the page title.
     * - Index 1 is the main article content.
     * Returns null if the scraping process fails.
     */
    public String[] scrapePage(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            Element titleElement = doc.selectFirst("h1");
            String title = "";
            if (titleElement != null) {
                title = titleElement.text();
            }
            
            Element contentElement = doc.selectFirst("#mw-content-text");
            String content = (contentElement != null) ? contentElement.text() : "";

            // Return both pieces of data together.
            return new String[]{title, content};

        } catch (IOException e) {
            System.err.println("Error while scraping " + url + ": " + e.getMessage());
            // Return null to indicate that the operation failed.
            return null;
        }
    }
}

