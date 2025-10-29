import java.io.*;

public class TfidfCalculator implements Serializable {
    private final HT wordDocCount;
    private final int totalDocs;

    TfidfCalculator(HT wordDocCount, int totalDocs) {
        this.wordDocCount = wordDocCount;
        this.totalDocs = totalDocs;
    }

    HT computeTfidf(HT wordCounts) {
        int totalWords = 0;
        for (HT.Node node : wordCounts.nodes()) totalWords += node.count;
        HT tfidf = new HT();
        for (HT.Node node : wordCounts.nodes()) {
            String word = (String) node.key;
            double tf = (double) node.count / totalWords;
            double idf = Math.log((double) totalDocs / wordDocCount.getCount(word));
            tfidf.add(word, tf * idf);
        }
        return tfidf;
    }

     HT computeTfidfForText(String text) {
        HT wordCounts = Similarity.countWords(text);
        return computeTfidf(wordCounts);
    }
}
