package logic;

import java.util.*;

public class SimilarityCalculator {
    public double calculateSimilarity(Map<String, Integer> freq1, Map<String, Integer> freq2) {
        if (freq1 == null || freq2 == null || freq1.isEmpty() || freq2.isEmpty()) {
            return 0.0;
        }

        Set<String> allWords = new HashSet<>(freq1.keySet());
        allWords.addAll(freq2.keySet());

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (String word : allWords) {
            int count1 = freq1.getOrDefault(word, 0);
            int count2 = freq2.getOrDefault(word, 0);

            dotProduct += (double) count1 * count2;
            magnitude1 += (double) count1 * count1;
            magnitude2 += (double) count2 * count2;
        }

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }

    public List<SimilarityPair> findTopSimilar(int targetIndex, List<Map<String, Integer>> allFrequencies) {
        Map<String, Integer> targetFreq = allFrequencies.get(targetIndex);
        List<SimilarityPair> similarities = new ArrayList<>();

        for (int i = 0; i < allFrequencies.size(); i++) {
            if (i != targetIndex) {
                double similarity = calculateSimilarity(targetFreq, allFrequencies.get(i));
                similarities.add(new SimilarityPair(i, similarity));
            }
        }

        similarities.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        // Return the top 2 results
        int topN = Math.min(2, similarities.size());
        return similarities.subList(0, topN);
    }

    public static class SimilarityPair {
        private final int index;
        private final double similarity;

        SimilarityPair(int index, double similarity) {
            this.index = index;
            this.similarity = similarity;
        }

        public int getIndex() {
            return index;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}