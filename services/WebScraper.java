package services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;

public class WebScraper {

    public String scrapeUrl(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element content = doc.selectFirst("#mw-content-text");

            if (content != null) {
                return content.text().toLowerCase();
            }
        } catch (IOException e) {
            System.err.println("Error scraping " + url + ": " + e.getMessage());
        }
        return null;
    }

    public String getTitleFromUrl(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element title = doc.selectFirst("h1");
            if (title != null) {
                return title.text();
            }
        } catch (IOException e) {
            // Fallback to URL parsing
        }
        // Extract title from URL as fallback
        String[] parts = url.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1].replace("_", " ");
        }
        return "Unknown";
    }
}