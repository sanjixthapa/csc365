import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

static final int NUM_CLUSTERS = 7;
static final int MAX_ITERATIONS = 100;

static List<WebData> pages = new ArrayList<>();
static HT wordDocCount = new HT();
static int totalDocs = 0;

public static void main() {
    System.out.println("1. Loading pages");
    loadPages();
    TfidfCalculator tfidfCalc = new TfidfCalculator(wordDocCount, totalDocs);

    System.out.println("2. Calculating TF-IDF");
    for (int i = 0; i < pages.size(); i++) {
        HT counts = pages.get(i).tfidfVector;
        HT tfidf = tfidfCalc.computeTfidf(counts);
        WebData original = pages.get(i);
        pages.set(i, new WebData(original.pageURL, original.pageTitle, tfidf));
    }
    System.out.println("3. Clustering (K=" + NUM_CLUSTERS + ")");
    ClusteringResult result = clusterPages(); // Get the combined result
    System.out.println("4. Saving data");
    saveData(result.assignments, result.centroids, tfidfCalc);
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
                HT counts = Similarity.countWords(doc.body().text());
                wordCounts.add(counts);
                titles.add(title);
                urls.add(url.trim());
                totalDocs++;
                for (HT.Node node : counts.nodes()) {
                    wordDocCount.add(node.key);
                }
            } catch (Exception e) {
                System.err.println(" Failed: " + url);
            }
        }
    } catch (IOException e) {
        System.err.println("Fatal: Could not read urls file");
        System.exit(1);
    }
    for (int i = 0; i < totalDocs; i++)
        pages.add(new WebData(urls.get(i), titles.get(i), wordCounts.get(i)));
}

// Change the return type from int[] to ClusteringResult
static ClusteringResult clusterPages() {
    int[] assignments = new int[pages.size()];
    HT[] centroids = initializeCentroids();
    for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
        if (!assignToClusters(centroids, assignments)) {
            System.out.println("Converged at iteration " + (iter + 1));
            break;
        }
        updateCentroids(centroids, assignments);
    }
    return new ClusteringResult(assignments, centroids);
}

static HT[] initializeCentroids() {
    HT[] centroids = new HT[NUM_CLUSTERS];
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < pages.size(); i++) indices.add(i);
    Collections.shuffle(indices);
    for (int i = 0; i < NUM_CLUSTERS; i++) {
        HT originalVector = pages.get(indices.get(i)).tfidfVector;
        HT centroidCopy = new HT();
        for (HT.Node node : originalVector.nodes()) {
            centroidCopy.add(node.key, node.value);
        }
        centroids[i] = centroidCopy;
    }
    return centroids;
}

static boolean assignToClusters(HT[] centroids, int[] assignments) {
    boolean changed = false;
    for (int i = 0; i < pages.size(); i++) {
        int bestCluster = 0;
        double bestSim = -1;
        for (int j = 0; j < NUM_CLUSTERS; j++) {
            double sim = Similarity.cosineSimilarity(pages.get(i).tfidfVector, centroids[j]);
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

static void updateCentroids(HT[] centroids, int[] assignments) {
    for (int cluster = 0; cluster < NUM_CLUSTERS; cluster++) {
        List<Integer> members = new ArrayList<>();
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == cluster) members.add(i);
        }
        centroids[cluster] = new HT();
        for (int pageIdx : members) {
            for (HT.Node node : pages.get(pageIdx).tfidfVector.nodes()) {
                String word = (String) node.key;
                double score = (Double) node.value;
                Double currentSum = (Double) centroids[cluster].get(word);
                double current = (currentSum == null) ? 0.0 : currentSum;
                centroids[cluster].add(word, current + score);
            }
        }
        for (HT.Node node : centroids[cluster].nodes()) {
            centroids[cluster].add(node.key, (Double) node.value / members.size());
        }
    }
}

static void saveData(int[] clusters, HT[] centroids, TfidfCalculator tfidfCalc) {
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
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("data/centroids.dat"))) {
            out.writeObject(centroids);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("data/tfidf.dat"))) {
            out.writeObject(tfidfCalc);
        }
        System.out.println("Saved " + pages.size() + " pages, centroids, and TfidfCalculator");
    } catch (IOException e) {
        System.err.println("Error: " + e.getMessage());
    }
}
record ClusteringResult(int[] assignments, HT[] centroids) {}
