import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class NewsTicker {
    
    private StackPane container;
    private Canvas tickerCanvas;
    private GraphicsContext gc;
    private AnimationTimer animator;
    
    private List<String> newsItems;
    private StringBuilder newsLog;
    private double scrollX = 0;
    private String currentText = "";
    
    // Control for random updates
    private boolean randomUpdatesEnabled = false;
    
    private static final Color BG_COLOR = Color.web("#1a1a1a");
    private static final Color TEXT_COLOR = Color.web("#e0e0e0");
    private static final Color ACCENT_COLOR = Color.web("#4a9eff");
    private static final double SCROLL_SPEED = 1.0;
    
    public NewsTicker() {
        container = new StackPane();
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + toHex(BG_COLOR) + ";");
        
        tickerCanvas = new Canvas(1200, 30);
        gc = tickerCanvas.getGraphicsContext2D();
        
        container.getChildren().add(tickerCanvas);
        
        // Initialize news items
        newsItems = new ArrayList<>();
        newsLog = new StringBuilder();
        
        // Add initial news
        addNewsItem("Welcome to LogiMap - Logistics & Supply Chain Simulator");
        
        buildTickerText();
        startAnimation();
    }
    
    public void addNewsItem(String item) {
        newsItems.add(item);
        newsLog.append("[").append(getCurrentTime()).append("] ").append(item).append("\n");
        buildTickerText();
    }
    
    private void buildTickerText() {
        StringBuilder sb = new StringBuilder();
        for (String item : newsItems) {
            sb.append("  ●  ").append(item);
        }
        sb.append("  ●  "); // Add separator at the end for seamless loop
        currentText = sb.toString();
    }
    
    private void startAnimation() {
        animator = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        animator.start();
    }
    
    private void render() {
        double width = tickerCanvas.getWidth();
        double height = tickerCanvas.getHeight();
        
        // Clear canvas
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, width, height);
        
        // Draw border
        gc.setStroke(ACCENT_COLOR);
        gc.setLineWidth(1);
        gc.strokeLine(0, 0, width, 0);
        
        // Set text properties
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        // Calculate text width
        double textWidth = gc.getFont().getSize() * currentText.length() * 0.6; // Approximate
        
        // Draw scrolling text
        gc.fillText(currentText, width - scrollX, height / 2 + 5);
        
        // If text has scrolled off screen, draw it again for seamless loop
        if (scrollX > textWidth) {
            scrollX = 0;
        }
        
        // Update scroll position
        scrollX += SCROLL_SPEED;
    }
    
    private String getCurrentTime() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }
    
    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    
    public String getLogHistory() {
        return newsLog.toString();
    }
    
    public StackPane getContainer() {
        return container;
    }
    
    public void stop() {
        if (animator != null) {
            animator.stop();
        }
    }
    
    /**
     * Enable or disable random news updates.
     * @param enabled Whether random updates should occur
     */
    public void setRandomUpdatesEnabled(boolean enabled) {
        this.randomUpdatesEnabled = enabled;
    }
    
    /**
     * Check if random updates are enabled.
     * @return true if random updates are enabled
     */
    public boolean isRandomUpdatesEnabled() {
        return randomUpdatesEnabled;
    }
}
