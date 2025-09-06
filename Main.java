import ui.RecommendationApp;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RecommendationApp app = new RecommendationApp();
            app.setVisible(true);
        });
    }
}