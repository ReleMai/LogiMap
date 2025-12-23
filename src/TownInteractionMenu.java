import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Medieval-styled popup menu for town interactions.
 * Displays when right-clicking on a town after the player moves there.
 * Provides options like trading, resting, recruiting, and viewing town info.
 */
public class TownInteractionMenu extends StackPane {
    
    // The town being interacted with
    private Town currentTown;
    private PlayerSprite player;
    
    // UI Components
    private VBox menuContainer;
    private Label townNameLabel;
    private Label townTypeLabel;
    private Label populationLabel;
    private VBox optionsContainer;
    private Button restBtn;
    
    // Callback handlers
    private Runnable onClose;
    private Runnable onTrade;
    private Runnable onRest;
    private Runnable onRecruit;
    private Runnable onViewInfo;
    
    // Style constants - Medieval parchment theme
    private static final String MENU_BG = "#2d2418";
    private static final String MENU_BORDER = "#8b7355";
    private static final String MENU_HEADER = "#1a1408";
    private static final String ACCENT_COLOR = "#c4a574";
    private static final String TEXT_COLOR = "#d4c4a4";
    private static final String BUTTON_BG = "#3d3020";
    private static final String BUTTON_HOVER = "#4d4030";
    private static final String GOLD_TEXT = "#ffd700";
    
    public TownInteractionMenu() {
        // Darkened background overlay
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.color(0, 0, 0, 0.6));
        overlay.widthProperty().bind(this.widthProperty());
        overlay.heightProperty().bind(this.heightProperty());
        overlay.setOnMouseClicked(e -> close());
        
        // Create menu panel
        menuContainer = createMenuPanel();
        
        this.getChildren().addAll(overlay, menuContainer);
        this.setVisible(false);
        this.setAlignment(Pos.CENTER);
    }
    
    /**
     * Creates the styled menu panel.
     */
    private VBox createMenuPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0));
        panel.setMaxWidth(400);
        panel.setMaxHeight(500);
        panel.setStyle(
            "-fx-background-color: " + MENU_BG + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 15;"
        );
        
        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setColor(Color.color(0, 0, 0, 0.7));
        panel.setEffect(shadow);
        
        // Header section
        VBox header = createHeader();
        
        // Town info section
        VBox infoSection = createInfoSection();
        
        // Options section
        optionsContainer = createOptionsSection();
        
        // Close button
        Button closeBtn = createStyledButton("✕ Close", false);
        closeBtn.setOnAction(e -> close());
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        HBox closeBox = new HBox(closeBtn);
        closeBox.setPadding(new Insets(10, 20, 20, 20));
        HBox.setHgrow(closeBtn, Priority.ALWAYS);
        
        panel.getChildren().addAll(header, infoSection, optionsContainer, closeBox);
        
        return panel;
    }
    
    /**
     * Creates the decorative header with town name.
     */
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 20, 15, 20));
        header.setStyle(
            "-fx-background-color: " + MENU_HEADER + ";" +
            "-fx-background-radius: 12 12 0 0;"
        );
        
        // Decorative line
        Rectangle topLine = new Rectangle(150, 2);
        topLine.setFill(Color.web(ACCENT_COLOR));
        
        // Town name
        townNameLabel = new Label("Town Name");
        townNameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        townNameLabel.setTextFill(Color.web(GOLD_TEXT));
        
        // Town type
        townTypeLabel = new Label("Major Town");
        townTypeLabel.setFont(Font.font("Georgia", FontWeight.NORMAL, 14));
        townTypeLabel.setTextFill(Color.web(ACCENT_COLOR));
        
        // Decorative line
        Rectangle bottomLine = new Rectangle(150, 2);
        bottomLine.setFill(Color.web(ACCENT_COLOR));
        
        header.getChildren().addAll(topLine, townNameLabel, townTypeLabel, bottomLine);
        
        return header;
    }
    
    /**
     * Creates the town information section.
     */
    private VBox createInfoSection() {
        VBox info = new VBox(8);
        info.setPadding(new Insets(10, 20, 10, 20));
        
        // Population
        populationLabel = new Label("Population: 0");
        populationLabel.setFont(Font.font("Arial", 14));
        populationLabel.setTextFill(Color.web(TEXT_COLOR));
        
        // Separator
        Rectangle sep = new Rectangle();
        sep.setWidth(360);
        sep.setHeight(1);
        sep.setFill(Color.web(MENU_BORDER, 0.5));
        
        info.getChildren().addAll(populationLabel, sep);
        
        return info;
    }
    
    /**
     * Creates the options section with action buttons.
     */
    private VBox createOptionsSection() {
        VBox options = new VBox(10);
        options.setPadding(new Insets(5, 20, 10, 20));
        
        // Section title
        Label title = new Label("⚔ Actions");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(ACCENT_COLOR));
        
        // Trade button
        Button tradeBtn = createStyledButton("🏪 Visit Marketplace", true);
        tradeBtn.setOnAction(e -> {
            if (onTrade != null) onTrade.run();
        });
        
        // Rest button (text will be updated when showing)
        restBtn = createStyledButton("🛏 Rest", true);
        restBtn.setOnAction(e -> {
            if (onRest != null) onRest.run();
        });
        
        // Recruit button
        Button recruitBtn = createStyledButton("👥 Recruit Companions", true);
        recruitBtn.setOnAction(e -> {
            if (onRecruit != null) onRecruit.run();
        });
        
        // View info button
        Button infoBtn = createStyledButton("📜 Town Information", true);
        infoBtn.setOnAction(e -> {
            if (onViewInfo != null) onViewInfo.run();
        });
        
        options.getChildren().addAll(title, tradeBtn, restBtn, recruitBtn, infoBtn);
        
        return options;
    }
    
    /**
     * Creates a styled medieval button.
     */
    private Button createStyledButton(String text, boolean fullWidth) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        btn.setTextFill(Color.web(TEXT_COLOR));
        btn.setPadding(new Insets(12, 20, 12, 20));
        
        if (fullWidth) {
            btn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btn, Priority.ALWAYS);
        }
        
        String baseStyle = 
            "-fx-background-color: " + BUTTON_BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;";
        
        String hoverStyle = 
            "-fx-background-color: " + BUTTON_HOVER + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;";
        
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        
        return btn;
    }
    
    /**
     * Shows the menu for a specific town.
     */
    public void showForTown(Town town, PlayerSprite player) {
        this.currentTown = town;
        this.player = player;
        
        // Update labels
        townNameLabel.setText(town.getName());
        townTypeLabel.setText(town.isMajor() ? "⭐ Major Town" : "Minor Settlement");
        populationLabel.setText("👥 Population: " + formatNumber((int)town.getPopulation()));
        
        // Update rest button text based on town type
        PlayerEnergy.RestLocation restLocation = town.getRestLocation();
        int cost = restLocation.getCost();
        if (cost > 0) {
            restBtn.setText("🛏 " + restLocation.getDisplayName() + " (" + cost + " gold)");
        } else {
            restBtn.setText("🛏 Rest at " + restLocation.getDisplayName() + " (Free)");
        }
        
        // Show with animation
        this.setVisible(true);
        
        // Scale animation
        menuContainer.setScaleX(0.8);
        menuContainer.setScaleY(0.8);
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), menuContainer);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
        
        // Fade animation
        this.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(200), this);
        fade.setToValue(1.0);
        fade.play();
    }
    
    /**
     * Closes the menu with animation.
     */
    public void close() {
        // Scale out animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), menuContainer);
        scale.setToX(0.9);
        scale.setToY(0.9);
        
        // Fade out animation
        FadeTransition fade = new FadeTransition(Duration.millis(150), this);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            this.setVisible(false);
            if (onClose != null) onClose.run();
        });
        
        scale.play();
        fade.play();
    }
    
    /**
     * Formats a number with commas.
     */
    private String formatNumber(int num) {
        return String.format("%,d", num);
    }
    
    // === Setters for callbacks ===
    
    public void setOnClose(Runnable handler) { this.onClose = handler; }
    public void setOnTrade(Runnable handler) { this.onTrade = handler; }
    public void setOnRest(Runnable handler) { this.onRest = handler; }
    public void setOnRecruit(Runnable handler) { this.onRecruit = handler; }
    public void setOnViewInfo(Runnable handler) { this.onViewInfo = handler; }
    
    // === Getters ===
    
    public Town getCurrentTown() { return currentTown; }
    public boolean isShowing() { return this.isVisible(); }
}
