package logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles text processing tasks like tokenization, stop-word removal,
 * and word frequency counting. This class operates on clean text and
 * is independent of the data source.
 */
public class TextProcessor {

    // A comprehensive set of common English words to be filtered out during analysis.
    private static final Set<String> stopWords = new HashSet<>(Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
            "any", "are", "as", "at", "be", "because", "been", "before", "being", "below",
            "between", "both", "but", "by", "can", "did", "do", "does", "doing", "down",
            "during", "each", "few", "for", "from", "further", "had", "has", "have", "having",
            "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i",
            "if", "in", "into", "is", "it", "its", "itself", "just", "me", "more", "most",
            "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once", "only",
            "or", "other", "our", "ours", "ourselves", "out", "over", "own", "s", "same",
            "she", "should", "so", "some", "such", "t", "than", "that", "the", "their",
            "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those",
            "through", "to", "too", "under", "until", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with",
            "you", "your", "yours", "yourself", "yourselves"
    ));

    public Map<String, Integer> getWordFrequencies(String cleanText) {
        if (cleanText == null || cleanText.isEmpty()) {
            return new HashMap<>();
        }

        List<String> allTokens = Arrays.asList(cleanText.split("\\s+"));
        List<String> validTokens = allTokens.stream()
                .filter(token -> token.length() > 1)
                .filter(token -> !stopWords.contains(token))
                .toList();

        Map<String, Integer> wordFrequency = new HashMap<>();
        for (String token : validTokens) {
            wordFrequency.put(token, wordFrequency.getOrDefault(token, 0) + 1);
        }
        return wordFrequency;
    }
}