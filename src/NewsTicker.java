import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * News ticker with expandable activity log.
 * Shows scrolling news at bottom, with a log button that slides open a compact log panel.
 */
public class NewsTicker {
    
    private VBox container;
    private HBox tickerBar;
    private Canvas tickerCanvas;
    private GraphicsContext gc;
    private AnimationTimer animator;
    
    // Log panel components
    private VBox logPanel;
    private VBox logContent;
    private ScrollPane logScrollPane;
    private boolean logExpanded = false;
    private static final double LOG_PANEL_HEIGHT = 120; // Compact height
    
    // Log toggle button (canvas-based)
    private Canvas logButton;
    private boolean logButtonHovered = false;
    
    private List<String> newsItems;
    private List<LogEntry> logEntries;
    private StringBuilder newsLog;
    private double scrollX = 0;
    private String currentText = "";
    
    // Control for random updates
    private boolean randomUpdatesEnabled = false;
    
    private static final Color BG_COLOR = Color.web("#1a1a1a");
    private static final Color TEXT_COLOR = Color.web("#e0e0e0");
    private static final Color ACCENT_COLOR = Color.web("#4a9eff");
    private static final double SCROLL_SPEED = 1.0;
    
    // Log entry types for styling
    public enum LogType {
        INFO("#e0e0e0"),
        GATHER("#80d080"),
        TRADE("#e0c060"),
        DAY_CHANGE("#80a0e0"),
        TRAVEL("#c080c0"),
        WARNING("#e08060");
        
        private final String color;
        LogType(String color) { this.color = color; }
        public String getColor() { return color; }
    }
    
    private static class LogEntry {
        String timestamp;
        String message;
        LogType type;
        
        LogEntry(String timestamp, String message, LogType type) {
            this.timestamp = timestamp;
            this.message = message;
            this.type = type;
        }
    }
    
    public NewsTicker() {
        // Main container is a VBox: log panel on top, ticker bar on bottom
        container = new VBox(0);
        container.setAlignment(Pos.BOTTOM_CENTER);
        
        // Create log panel (starts collapsed)
        createLogPanel();
        
        // Create ticker bar
        createTickerBar();
        
        container.getChildren().addAll(logPanel, tickerBar);
        
        // Initialize news items
        newsItems = new ArrayList<>();
        logEntries = new ArrayList<>();
        newsLog = new StringBuilder();
        
        // Add initial news
        addLogEntry("Welcome to LogiMap - Logistics & Supply Chain Simulator", LogType.INFO);
        
        buildTickerText();
        startAnimation();
    }
    
    /**
     * Creates the ticker bar with scrolling text and log button.
     */
    private void createTickerBar() {
        tickerBar = new HBox(0);
        tickerBar.setAlignment(Pos.CENTER);
        tickerBar.setStyle("-fx-background-color: #1a1a1a;");
        tickerBar.setPrefHeight(30);
        tickerBar.setMaxHeight(30);
        
        // Ticker canvas
        tickerCanvas = new Canvas(1100, 30);
        gc = tickerCanvas.getGraphicsContext2D();
        HBox.setHgrow(tickerCanvas, Priority.ALWAYS);
        
        // Log button (canvas-based)
        logButton = new Canvas(40, 30);
        renderLogButton();
        
        logButton.setOnMouseEntered(e -> {
            logButtonHovered = true;
            renderLogButton();
        });
        logButton.setOnMouseExited(e -> {
            logButtonHovered = false;
            renderLogButton();
        });
        logButton.setOnMouseClicked(e -> toggleLog());
        
        tickerBar.getChildren().addAll(tickerCanvas, logButton);
    }
    
    /**
     * Renders the log toggle button.
     */
    private void renderLogButton() {
        GraphicsContext bgc = logButton.getGraphicsContext2D();
        double w = logButton.getWidth();
        double h = logButton.getHeight();
        
        // Background
        bgc.setFill(logButtonHovered ? Color.web("#3a3a3a") : Color.web("#2a2a2a"));
        bgc.fillRect(0, 0, w, h);
        
        // Left border
        bgc.setStroke(ACCENT_COLOR);
        bgc.setLineWidth(1);
        bgc.strokeLine(0, 0, 0, h);
        
        // Arrow icon (up when closed, down when open)
        bgc.setFill(TEXT_COLOR);
        bgc.setFont(Font.font("Arial", 14));
        String arrow = logExpanded ? "▼" : "▲";
        bgc.fillText(arrow, w / 2 - 5, h / 2 + 5);
    }
    
    /**
     * Creates the expandable log panel.
     */
    private void createLogPanel() {
        logPanel = new VBox(3);
        logPanel.setStyle(
            "-fx-background-color: #151520;" +
            "-fx-border-color: #4a9eff;" +
            "-fx-border-width: 1 0 0 0;"
        );
        logPanel.setPadding(new Insets(6));
        logPanel.setPrefHeight(0);
        logPanel.setMaxHeight(0);
        logPanel.setMinHeight(0);
        
        // Header row
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label headerLabel = new Label("📜 Activity Log");
        headerLabel.setStyle("-fx-text-fill: #4a9eff; -fx-font-weight: bold; -fx-font-size: 11;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label closeLabel = new Label("✕");
        closeLabel.setStyle("-fx-text-fill: #666666; -fx-cursor: hand; -fx-font-size: 11;");
        closeLabel.setOnMouseClicked(e -> toggleLog());
        closeLabel.setOnMouseEntered(e -> closeLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-cursor: hand; -fx-font-size: 11;"));
        closeLabel.setOnMouseExited(e -> closeLabel.setStyle("-fx-text-fill: #666666; -fx-cursor: hand; -fx-font-size: 11;"));
        
        header.getChildren().addAll(headerLabel, spacer, closeLabel);
        
        // Log content
        logContent = new VBox(1);
        logContent.setStyle("-fx-background-color: transparent;");
        
        logScrollPane = new ScrollPane(logContent);
        logScrollPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;" +
            "-fx-border-color: transparent;"
        );
        logScrollPane.setFitToWidth(true);
        logScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScrollPane.setPrefHeight(LOG_PANEL_HEIGHT - 30);
        VBox.setVgrow(logScrollPane, Priority.ALWAYS);
        
        logPanel.getChildren().addAll(header, logScrollPane);
        logPanel.setVisible(false);
        logPanel.setManaged(false);
    }
    
    /**
     * Toggles the log panel with slide animation.
     */
    public void toggleLog() {
        logExpanded = !logExpanded;
        renderLogButton();
        
        if (logExpanded) {
            // Expand
            logPanel.setVisible(true);
            logPanel.setManaged(true);
            
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(logPanel.prefHeightProperty(), 0),
                    new KeyValue(logPanel.maxHeightProperty(), 0)),
                new KeyFrame(Duration.millis(200), 
                    new KeyValue(logPanel.prefHeightProperty(), LOG_PANEL_HEIGHT),
                    new KeyValue(logPanel.maxHeightProperty(), LOG_PANEL_HEIGHT))
            );
            timeline.play();
        } else {
            // Collapse
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(logPanel.prefHeightProperty(), LOG_PANEL_HEIGHT),
                    new KeyValue(logPanel.maxHeightProperty(), LOG_PANEL_HEIGHT)),
                new KeyFrame(Duration.millis(200), 
                    new KeyValue(logPanel.prefHeightProperty(), 0),
                    new KeyValue(logPanel.maxHeightProperty(), 0))
            );
            timeline.setOnFinished(e -> {
                logPanel.setVisible(false);
                logPanel.setManaged(false);
            });
            timeline.play();
        }
    }
    
    /**
     * Adds a news item to both the ticker and the log.
     */
    public void addNewsItem(String item) {
        addLogEntry(item, LogType.INFO);
    }
    
    /**
     * Adds a typed log entry with timestamp.
     */
    public void addLogEntry(String message, LogType type) {
        String timestamp = getCurrentTime();
        newsItems.add(message);
        logEntries.add(new LogEntry(timestamp, message, type));
        newsLog.append("[").append(timestamp).append("] ").append(message).append("\n");
        buildTickerText();
        updateLogContent();
    }
    
    /**
     * Convenience methods for different log types.
     */
    public void logGather(String resourceName, int amount) {
        addLogEntry("Gathered " + amount + " " + resourceName, LogType.GATHER);
    }
    
    public void logTrade(String action, String itemName, int amount, int price) {
        String verb = action.equalsIgnoreCase("buy") ? "Bought" : "Sold";
        addLogEntry(verb + " " + amount + " " + itemName + " for " + price + " coins", LogType.TRADE);
    }
    
    public void logDayChange(String date) {
        addLogEntry("New day: " + date, LogType.DAY_CHANGE);
    }
    
    public void logTravel(String destination) {
        addLogEntry("Arrived at " + destination, LogType.TRAVEL);
    }
    
    /**
     * Updates the visual log content panel.
     */
    private void updateLogContent() {
        logContent.getChildren().clear();
        
        // Show most recent entries first (reverse order), limit to 25 for compact view
        int start = Math.max(0, logEntries.size() - 25);
        for (int i = logEntries.size() - 1; i >= start; i--) {
            LogEntry entry = logEntries.get(i);
            
            HBox entryBox = new HBox(6);
            entryBox.setPadding(new Insets(1, 4, 1, 4));
            
            Label timeLabel = new Label("[" + entry.timestamp + "]");
            timeLabel.setStyle("-fx-text-fill: #555555; -fx-font-family: 'Consolas'; -fx-font-size: 10;");
            
            Label msgLabel = new Label(entry.message);
            msgLabel.setStyle("-fx-text-fill: " + entry.type.getColor() + "; -fx-font-size: 10;");
            
            entryBox.getChildren().addAll(timeLabel, msgLabel);
            logContent.getChildren().add(entryBox);
        }
    }
    
    private void buildTickerText() {
        StringBuilder sb = new StringBuilder();
        // Show only recent items in ticker
        int start = Math.max(0, newsItems.size() - 10);
        for (int i = start; i < newsItems.size(); i++) {
            sb.append("  ●  ").append(newsItems.get(i));
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
    
    public VBox getContainer() {
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
    
    /**
     * Gets whether the log panel is currently expanded.
     */
    public boolean isLogExpanded() {
        return logExpanded;
    }
}
