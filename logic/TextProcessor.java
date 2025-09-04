package logic;

import java.util.*;

public class TextProcessor {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in",
            "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with"
    );

    public Map<String, Integer> getWordFrequencies(String text) {
        if (text == null || text.isEmpty()) {
            return new HashMap<>();
        }

        String[] words = text.toLowerCase().split("\\s+");
        Map<String, Integer> frequencies = new HashMap<>();

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
            }
        }
        return frequencies;
    }
}