package logic;

import java.util.*;

public class SimilarityCalculator {

    public double calculateSimilarity(Map<String, Integer> freq1, Map<String, Integer> freq2) {
        if (freq1.isEmpty() || freq2.isEmpty()) {
            return 0.0;
        }

        Set<String> commonWords = new HashSet<>(freq1.keySet());
        commonWords.retainAll(freq2.keySet());

        if (commonWords.isEmpty()) {
            return 0.0;
        }

        // Simple similarity: common words / total unique words
        Set<String> allWords = new HashSet<>(freq1.keySet());
        allWords.addAll(freq2.keySet());

        return (double) commonWords.size() / allWords.size();
    }

    public List<Integer> findTopSimilar(int targetIndex, List<Map<String, Integer>> allFrequencies) {
        Map<String, Integer> targetFreq = allFrequencies.get(targetIndex);
        List<SimilarityPair> similarities = new ArrayList<>();

        for (int i = 0; i < allFrequencies.size(); i++) {
            if (i != targetIndex) {
                double similarity = calculateSimilarity(targetFreq, allFrequencies.get(i));
                similarities.add(new SimilarityPair(i, similarity));
            }
        }

        similarities.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < Math.min(2, similarities.size()); i++) {
            result.add(similarities.get(i).index);
        }
        return result;
    }

    private static class SimilarityPair {
        int index;
        double similarity;

        SimilarityPair(int index, double similarity) {
            this.index = index;
            this.similarity = similarity;
        }
    }
}