import java.io.Serializable;

class WebData implements Serializable {
    String pageURL;
    String pageTitle;
    HT tfidfVector; // This HT holds word counts first, then TF-IDF scores

    WebData(String url, String title, HT tfidf) {
        this.pageURL = url;
        this.pageTitle = title;
        this.tfidfVector = tfidf;
    }
}
