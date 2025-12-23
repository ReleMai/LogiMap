import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Complete player status panel showing stats, health, energy, and currency.
 * Proper vertical layout with clear sections.
 */
public class CharacterSheet extends VBox {
    
    // Medieval theme colors
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String GOLD = "#c4a574";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    private static final String HP_COLOR = "#c04040";
    private static final String ENERGY_COLOR = "#40a040";
    
    private static final int PANEL_WIDTH = 220;
    
    private PlayerSprite player;
    private PlayerEnergy energy;
    
    private double dragStartX, dragStartY;
    private boolean isDragging = false;
    private Runnable onClose;
    
    // UI elements to update
    private ProgressBar healthBar;
    private ProgressBar energyBar;
    private Label healthText;
    private Label energyText;
    private Label goldLabel;
    private Label silverLabel;
    private Label copperLabel;
    private Canvas spriteCanvas;
    
    public CharacterSheet(PlayerSprite player) {
        this(player, null);
    }
    
    public CharacterSheet(PlayerSprite player, PlayerEnergy energy) {
        this.player = player;
        this.energy = energy;
        
        setPrefWidth(PANEL_WIDTH);
        setMaxWidth(PANEL_WIDTH);
        setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);"
        );
        setSpacing(0);
        setPadding(new Insets(0));
        
        getChildren().addAll(
            createTitleBar(),
            createPlayerSection(),
            createStatsSection(),
            createCurrencySection()
        );
    }
    
    private HBox createTitleBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(6, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_MED + ");" +
            "-fx-background-radius: 6 6 0 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        bar.setCursor(javafx.scene.Cursor.MOVE);
        
        Label title = new Label("Character");
        title.setTextFill(Color.web(GOLD));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // No close button - windows stay open in side panel
        bar.getChildren().addAll(title, spacer);
        
        setupDragging(bar);
        return bar;
    }
    
    private VBox createPlayerSection() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(10));
        section.setAlignment(Pos.CENTER);
        section.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        // Player sprite
        spriteCanvas = new Canvas(64, 80);
        renderSprite();
        
        // Player name
        Label nameLabel = new Label(player.getName());
        nameLabel.setTextFill(Color.web(GOLD));
        nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        
        // Level (placeholder)
        Label levelLabel = new Label("Level 1 Adventurer");
        levelLabel.setTextFill(Color.web(TEXT_DIM));
        levelLabel.setFont(Font.font("Georgia", 11));
        
        section.getChildren().addAll(spriteCanvas, nameLabel, levelLabel);
        return section;
    }
    
    private VBox createStatsSection() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: " + BG_MED + "; -fx-border-color: " + BORDER + "; -fx-border-width: 1 0 1 0;");
        
        // Health bar
        VBox healthBox = createStatBar("Health", HP_COLOR, player.getHealth(), player.getMaxHealth());
        healthBar = (ProgressBar) ((HBox) healthBox.getChildren().get(1)).getChildren().get(0);
        healthText = (Label) ((HBox) healthBox.getChildren().get(0)).getChildren().get(2); // Index 2: after nameLabel (0), spacer (1)
        
        // Energy bar
        int currentEnergy = energy != null ? (int) energy.getCurrentEnergy() : 100;
        int maxEnergy = energy != null ? (int) energy.getMaxEnergy() : 100;
        VBox energyBox = createStatBar("Energy", ENERGY_COLOR, currentEnergy, maxEnergy);
        energyBar = (ProgressBar) ((HBox) energyBox.getChildren().get(1)).getChildren().get(0);
        energyText = (Label) ((HBox) energyBox.getChildren().get(0)).getChildren().get(2); // Index 2: after nameLabel (0), spacer (1)
        
        // Combat stats
        HBox combatStats = new HBox(20);
        combatStats.setAlignment(Pos.CENTER);
        combatStats.setPadding(new Insets(5, 0, 0, 0));
        
        VBox atkBox = createStatDisplay("Attack", "0", "#c08080");
        VBox defBox = createStatDisplay("Defense", "0", "#80a0c0");
        VBox spdBox = createStatDisplay("Speed", "10", "#80c080");
        
        combatStats.getChildren().addAll(atkBox, defBox, spdBox);
        
        section.getChildren().addAll(healthBox, energyBox, combatStats);
        return section;
    }
    
    private VBox createStatBar(String name, String color, int current, int max) {
        VBox box = new VBox(2);
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.web(TEXT));
        nameLabel.setFont(Font.font("Georgia", 11));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valueLabel = new Label(current + "/" + max);
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        
        header.getChildren().addAll(nameLabel, spacer, valueLabel);
        
        HBox barBox = new HBox();
        ProgressBar bar = new ProgressBar((double) current / max);
        bar.setPrefWidth(PANEL_WIDTH - 24);
        bar.setPrefHeight(14);
        bar.setStyle("-fx-accent: " + color + "; -fx-background-color: #101010; -fx-background-radius: 3;");
        barBox.getChildren().add(bar);
        
        box.getChildren().addAll(header, barBox);
        return box;
    }
    
    private VBox createStatDisplay(String name, String value, String color) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        
        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.web(TEXT_DIM));
        nameLabel.setFont(Font.font("Georgia", 9));
        
        box.getChildren().addAll(valueLabel, nameLabel);
        return box;
    }
    
    private VBox createCurrencySection() {
        VBox section = new VBox(5);
        section.setPadding(new Insets(10));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setStyle("-fx-background-color: " + BG_DARK + "; -fx-background-radius: 0 0 6 6;");
        
        Label header = new Label("Currency");
        header.setTextFill(Color.web(TEXT_DIM));
        header.setFont(Font.font("Georgia", 10));
        
        HBox coins = new HBox(15);
        coins.setAlignment(Pos.CENTER);
        
        Currency c = player.getCurrency();
        
        goldLabel = new Label(c.getGold() + " Gold");
        goldLabel.setTextFill(Color.web("#ffd700"));
        goldLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        
        silverLabel = new Label(c.getSilver() + " Silver");
        silverLabel.setTextFill(Color.web("#c0c0c0"));
        silverLabel.setFont(Font.font("Georgia", 11));
        
        copperLabel = new Label(c.getCopper() + " Copper");
        copperLabel.setTextFill(Color.web("#cd7f32"));
        copperLabel.setFont(Font.font("Georgia", 11));
        
        coins.getChildren().addAll(goldLabel, silverLabel, copperLabel);
        
        section.getChildren().addAll(header, coins);
        return section;
    }
    
    private void renderSprite() {
        GraphicsContext gc = spriteCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0a0805"));
        gc.fillRoundRect(0, 0, 64, 80, 5, 5);
        gc.setStroke(Color.web(BORDER));
        gc.setLineWidth(1);
        gc.strokeRoundRect(0, 0, 64, 80, 5, 5);
        player.render(gc, 8, 8, 48);
    }
    
    private Button createCloseButton() {
        Button btn = new Button("X");
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 0 4; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #5a2020; -fx-text-fill: #ff8080; -fx-font-size: 12; -fx-padding: 0 4; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 0 4; -fx-cursor: hand;"));
        btn.setOnAction(e -> { if (onClose != null) onClose.run(); setVisible(false); });
        return btn;
    }
    
    private void setupDragging(HBox bar) {
        bar.setOnMousePressed(e -> {
            if (getParent() instanceof Pane) {
                dragStartX = e.getSceneX() - getLayoutX();
                dragStartY = e.getSceneY() - getLayoutY();
                isDragging = true;
                toFront();
            }
        });
        bar.setOnMouseDragged(e -> {
            if (isDragging && getParent() instanceof Pane) {
                Pane parent = (Pane) getParent();
                setLayoutX(Math.max(0, Math.min(e.getSceneX() - dragStartX, parent.getWidth() - getWidth())));
                setLayoutY(Math.max(0, Math.min(e.getSceneY() - dragStartY, parent.getHeight() - getHeight())));
            }
        });
        bar.setOnMouseReleased(e -> isDragging = false);
    }
    
    public void setEnergy(PlayerEnergy energy) {
        this.energy = energy;
    }
    
    public void refresh() {
        // Update health
        double healthPercent = (double) player.getHealth() / player.getMaxHealth();
        healthBar.setProgress(healthPercent);
        healthText.setText(player.getHealth() + "/" + player.getMaxHealth());
        
        // Update energy
        if (energy != null) {
            energyBar.setProgress(energy.getEnergyPercent());
            energyText.setText((int) energy.getCurrentEnergy() + "/" + (int) energy.getMaxEnergy());
        }
        
        // Update currency
        Currency c = player.getCurrency();
        goldLabel.setText(c.getGold() + " Gold");
        silverLabel.setText(c.getSilver() + " Silver");
        copperLabel.setText(c.getCopper() + " Copper");
        
        // Re-render sprite
        renderSprite();
    }
    
    public void setOnClose(Runnable callback) { this.onClose = callback; }
}
