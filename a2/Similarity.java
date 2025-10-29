public class Similarity {
     static double cosineSimilarity(HT vectorA, HT vectorB) {
        double dotProduct = 0.0;
        double magA = 0.0;
        double magB = 0.0;

        for (HT.Node node : vectorA.nodes()) {
            String word = (String) node.key;
            double valA = (Double) node.value;
            Double valB_obj = (Double) vectorB.get(word);
            double valB = (valB_obj == null) ? 0.0 : valB_obj;
            dotProduct += valA * valB;
            magA += valA * valA;
        }
        for (HT.Node node : vectorB.nodes()) {
            double valB = (Double) node.value;
            magB += valB * valB;
        }
        if (magA == 0.0 || magB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(magA) * Math.sqrt(magB));
    }

     static HT countWords(String text) {
        HT counts = new HT();
        for (String word : text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+")) {
            if (!word.isEmpty()) counts.add(word);
        }
        return counts;
    }
}
