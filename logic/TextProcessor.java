package logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles text processing tasks like tokenization, stop-word removal,
 * and word frequency counting.
 */
public class TextProcessor {

    // A set of common words to be filtered out (stop words).
    private static final Set<String> STOP_WORDS = new HashSet<>(Set.of(
            "are", "from", "that", "this", "its", "they", "be", "when", "also", "at",
            "has", "but", "not", "which", "or", "the", "of", "and", "a", "an",
            "is", "in", "to", "it", "for", "on", "with", "as", "by", "i", "me",
            "my", "you", "your", "yours", "he", "him", "his", "she", "her", "hers",
            "we", "us", "our", "ours", "them", "their", "theirs", "what", "who",
            "whom", "where", "why", "how", "all", "any", "both", "each", "few",
            "more", "most", "other", "some", "such", "no", "nor", "can", "will",
            "just", "so", "than", "too", "very", "would", "should", "could"
    ));

    /**
     * Counts the frequency of each word in a given text string.
     * This method tokenizes the text and ignores non-alphabetic characters.
     *
     * @param text The input text to process.
     * @return A map where keys are words and values are their frequencies.
     */
    public Map<String, Integer> countWords(String text) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        // Split the text by any character that is not a letter. This is a simple tokenizer.
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty() && !STOP_WORDS.contains(word)) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        return wordFrequency;
    }
}
