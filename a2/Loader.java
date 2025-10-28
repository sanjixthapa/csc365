import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.util.*;

static final int NUM_CLUSTERS = 7;
static final int MAX_ITERATIONS = 100;

static List<WebData> pages = new ArrayList<>();
static HT wordDocCount = new HT();
static int totalDocs = 0;

public static void main() {
    System.out.println("1. Loading pages");
    loadPages();

    System.out.println("2. Calculating TF-IDF vectors");
    calculateTfidf();

    System.out.println("3. Clustering pages (K=" + NUM_CLUSTERS + ")");
    int[] clusters = clusterPages();

    System.out.println("4. Saving data");
    saveData(clusters);

    System.out.println("\nDone! Run Application.java");
}

static void loadPages() {
    List<HT> wordCounts = new ArrayList<>();
    List<String> titles = new ArrayList<>();
    List<String> urls = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader("urls"))) {
        String url;
        while ((url = reader.readLine()) != null) {
            if (url.trim().isEmpty()) continue;
            try {
                Document doc = Jsoup.connect(url.trim()).get();
                String title = doc.title();
                System.out.println(" - " + title);

                HT counts = countWords(doc.body().text());
                wordCounts.add(counts);
                titles.add(title);
                urls.add(url.trim());
                totalDocs++;

                for (HT.Node node : counts.nodes()) {
                    wordDocCount.add(node.key);
                }
            } catch (Exception e) {
                System.err.println("  Failed: " + url + " (" + e.getMessage() + ")");
            }
        }
    } catch (IOException e) {
        System.err.println("Fatal: Could not read urls file");
        System.exit(1);
    }
    for (int i = 0; i < totalDocs; i++) {
        pages.add(new WebData(urls.get(i), titles.get(i), wordCounts.get(i)));
    }
    System.out.println("Loaded " + totalDocs + " pages");
}

static HT countWords(String text) {
    HT counts = new HT();
    String[] words = text.toLowerCase()
            .replaceAll("[^a-z ]", "")
            .split("\\s+");
    for (String word : words) {
        counts.add(word);
    }
    return counts;
}

static void calculateTfidf() {
    for (int i = 0; i < pages.size(); i++) {
        WebData page = pages.get(i);
        HT wordCounts = page.tfidfVector;
        int totalWords = 0;
        for (HT.Node node : wordCounts.nodes()) {
            totalWords = totalWords + node.count;
        }
        HT tfidfScores = new HT();
        for (HT.Node node : wordCounts.nodes()) {
            String word = (String) node.key;
            double tf = (double) node.count / totalWords;
            double idf = Math.log((double) totalDocs / wordDocCount.getCount(word));
            tfidfScores.add(word, tf * idf);
        }
         pages.set(i, new WebData(page.pageURL, page.pageTitle, tfidfScores));
    }
}

static int[] clusterPages() {
    List<String> vocab = buildVocabulary();
    HT wordIndex = buildWordIndex(vocab);
    //[page_index][word_index] -> tf-idf_score
    double[][] vectors = buildVectors(vocab.size(), wordIndex);
    int[] assignments = new int[pages.size()];
    double[][] centroids = initializeCentroids(vectors, vocab.size());
    for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
        if (!assignToClusters(vectors, centroids, assignments)) {
            System.out.println("Converged at iteration " + (iter + 1));
            break;
        }
        updateCentroids(vectors, centroids, assignments);
    }
     return assignments;
}

static List<String> buildVocabulary() {
    List<String> vocab = new ArrayList<>();
    for (HT.Node node : wordDocCount.nodes()) {
        vocab.add((String) node.key);//list of unique words
    }
    Collections.sort(vocab);//sort alphabetically
    return vocab;
}

static HT buildWordIndex(List<String> vocab) {
    HT index = new HT();
    for (int i = 0; i < vocab.size(); i++) {
        index.add(vocab.get(i), i);//add index to the unique words
    }
    return index;
}

static double[][] buildVectors(int vocabSize, HT wordIndex) {
    double[][] vectors = new double[pages.size()][vocabSize];
    for (int i = 0; i < pages.size(); i++) {
        for (HT.Node node : pages.get(i).tfidfVector.nodes()) {
            Integer idx = (Integer) wordIndex.get(node.key);
            if (idx != null) {
                vectors[i][idx] = (Double) node.value;
            }
        }
    }
    return vectors;
}

static double[][] initializeCentroids(double[][] vectors, int vocabSize) {
    double[][] centroids = new double[NUM_CLUSTERS][vocabSize];
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < vectors.length; i++)
        indices.add(i);
    Collections.shuffle(indices);
    for (int i = 0; i < NUM_CLUSTERS; i++) {
        int pageIdx = indices.get(i);
        System.arraycopy(vectors[pageIdx], 0, centroids[i], 0, vocabSize);
    }
    return centroids;
}

static boolean assignToClusters(double[][] vectors, double[][] centroids, int[] assignments) {
    boolean changed = false;
    for (int i = 0; i < vectors.length; i++) {
        int bestCluster = 0;
        double bestSim = -1;
        for (int j = 0; j < NUM_CLUSTERS; j++) {
            double sim = cosineSimilarity(vectors[i], centroids[j]);
            if (sim > bestSim) {
                bestSim = sim;
                bestCluster = j;
            }
        }
        if (assignments[i] != bestCluster) {
            assignments[i] = bestCluster;
            changed = true;
        }
    }
    return changed;
}

static void updateCentroids(double[][] vectors, double[][] centroids, int[] assignments) {
    int vocabSize = vectors[0].length;

    for (int cluster = 0; cluster < NUM_CLUSTERS; cluster++) {
        List<Integer> membersOfCluster = new ArrayList<>();
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == cluster) {
                membersOfCluster.add(i);
            }
        }
        Arrays.fill(centroids[cluster], 0.0);
        for (int pageIndex : membersOfCluster) {
            for (int i = 0; i < vocabSize; i++) {
                centroids[cluster][i] = centroids[cluster][i] + vectors[pageIndex][i];
            }
        }
        if (!membersOfCluster.isEmpty()) {
            for (int i = 0; i < vocabSize; i++) {
                centroids[cluster][i] = centroids[cluster][i] / membersOfCluster.size();
            }
        }
    }
}

static double cosineSimilarity(double[] a, double[] b) {
    double dot = 0;
    double magA = 0;
    double magB = 0;
    for (int i = 0; i < a.length; i++) {
        dot = dot + a[i] * b[i];
        magA = magA + a[i] * a[i];
        magB = magB + b[i] * b[i];
    }
    if (magA == 0 || magB == 0) return 0;
    return dot / (Math.sqrt(magA) * Math.sqrt(magB));
}

static void saveData(int[] clusters) {
    try {
        for (int i = 0; i < pages.size(); i++) {
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream("data/page_" + i + ".dat"))) {
                out.writeObject(pages.get(i));
                    }
                }
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("data/clusters.dat"))) {
            out.writeObject(clusters);
        }
        System.out.println("Saved " + pages.size() + " pages and clusters.dat");
    } catch (IOException e) {
        System.err.println("Error saving: " + e.getMessage());
    }
}
