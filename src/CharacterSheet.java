import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Complete player status panel showing stats, health, energy, and currency.
 * Now with tabs for Overview and Attributes.
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
    private static final String TAB_ACTIVE = "#3a2a15";
    private static final String TAB_INACTIVE = "#1a1208";
    
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
    private Label levelLabel;
    
    // Tab content containers
    private VBox overviewContent;
    private VBox attributesContent;
    private StackPane contentContainer;
    
    // Attribute labels for updating
    private Label[] statLabels;
    private Label[] statModLabels;
    private Label xpLabel;
    private Label statPointsLabel;
    
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
        
        // Build the overview content
        overviewContent = new VBox(0);
        overviewContent.getChildren().addAll(
            createPlayerSection(),
            createStatsSection(),
            createCurrencySection()
        );
        
        // Build the attributes content
        attributesContent = createAttributesSection();
        
        // Content container to switch between tabs
        contentContainer = new StackPane();
        contentContainer.getChildren().add(overviewContent);
        
        getChildren().addAll(
            createTitleBar(),
            createTabBar(),
            contentContainer
        );
    }
    
    private HBox createTabBar() {
        HBox tabBar = new HBox(0);
        tabBar.setAlignment(Pos.CENTER);
        tabBar.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        ToggleGroup tabGroup = new ToggleGroup();
        
        ToggleButton overviewTab = createTabButton("Overview", tabGroup, true);
        ToggleButton attributesTab = createTabButton("Attributes", tabGroup, false);
        
        overviewTab.setOnAction(e -> showTab(true));
        attributesTab.setOnAction(e -> showTab(false));
        
        HBox.setHgrow(overviewTab, Priority.ALWAYS);
        HBox.setHgrow(attributesTab, Priority.ALWAYS);
        overviewTab.setMaxWidth(Double.MAX_VALUE);
        attributesTab.setMaxWidth(Double.MAX_VALUE);
        
        tabBar.getChildren().addAll(overviewTab, attributesTab);
        return tabBar;
    }
    
    private ToggleButton createTabButton(String text, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        btn.setPadding(new Insets(6, 12, 6, 12));
        
        String activeStyle = 
            "-fx-background-color: " + TAB_ACTIVE + ";" +
            "-fx-text-fill: " + GOLD + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 2 0;";
        
        String inactiveStyle = 
            "-fx-background-color: " + TAB_INACTIVE + ";" +
            "-fx-text-fill: " + TEXT_DIM + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;";
        
        btn.setStyle(selected ? activeStyle : inactiveStyle);
        
        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            btn.setStyle(isSelected ? activeStyle : inactiveStyle);
        });
        
        return btn;
    }
    
    private void showTab(boolean overview) {
        contentContainer.getChildren().clear();
        if (overview) {
            contentContainer.getChildren().add(overviewContent);
        } else {
            contentContainer.getChildren().add(attributesContent);
            refreshAttributes();
        }
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
        
        // Level from CharacterStats
        CharacterStats stats = player.getCharacterStats();
        levelLabel = new Label("Level " + stats.getLevel() + " Adventurer");
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
    
    /**
     * Creates the Attributes tab content showing the 7 primary stats.
     */
    private VBox createAttributesSection() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(12));
        section.setStyle("-fx-background-color: " + BG_DARK + "; -fx-background-radius: 0 0 6 6;");
        
        CharacterStats stats = player.getCharacterStats();
        CharacterStats.Stat[] statTypes = CharacterStats.Stat.values();
        
        // Initialize arrays for updating
        statLabels = new Label[statTypes.length];
        statModLabels = new Label[statTypes.length];
        
        // Header
        Label header = new Label("⚔ Primary Attributes");
        header.setTextFill(Color.web(GOLD));
        header.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        section.getChildren().add(header);
        
        // Stat grid
        GridPane statGrid = new GridPane();
        statGrid.setHgap(8);
        statGrid.setVgap(4);
        statGrid.setPadding(new Insets(5, 0, 10, 0));
        
        for (int i = 0; i < statTypes.length; i++) {
            CharacterStats.Stat stat = statTypes[i];
            
            // Stat abbreviation
            Label abbrevLabel = new Label(stat.name());
            abbrevLabel.setTextFill(Color.web(GOLD));
            abbrevLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
            abbrevLabel.setPrefWidth(35);
            
            // Stat value
            statLabels[i] = new Label(String.valueOf(stats.getStat(stat)));
            statLabels[i].setTextFill(Color.web(TEXT));
            statLabels[i].setFont(Font.font("Georgia", FontWeight.BOLD, 12));
            statLabels[i].setPrefWidth(25);
            statLabels[i].setAlignment(Pos.CENTER);
            
            // Modifier
            statModLabels[i] = new Label(stats.getStatModifierDisplay(stat));
            int mod = stats.getStatModifier(stat);
            statModLabels[i].setTextFill(Color.web(mod >= 0 ? "#80c080" : "#c08080"));
            statModLabels[i].setFont(Font.font("Georgia", 10));
            statModLabels[i].setPrefWidth(25);
            
            // Full name tooltip-like label
            Label nameLabel = new Label(stat.getFullName());
            nameLabel.setTextFill(Color.web(TEXT_DIM));
            nameLabel.setFont(Font.font("Georgia", 9));
            
            statGrid.add(abbrevLabel, 0, i);
            statGrid.add(statLabels[i], 1, i);
            statGrid.add(statModLabels[i], 2, i);
            statGrid.add(nameLabel, 3, i);
        }
        section.getChildren().add(statGrid);
        
        // Derived stats section
        Label derivedHeader = new Label("📊 Derived Stats");
        derivedHeader.setTextFill(Color.web(GOLD));
        derivedHeader.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        section.getChildren().add(derivedHeader);
        
        VBox derivedStats = new VBox(3);
        derivedStats.setPadding(new Insets(5, 0, 10, 0));
        
        derivedStats.getChildren().addAll(
            createDerivedStatRow("Max Health", String.valueOf(stats.getMaxHealth()), HP_COLOR),
            createDerivedStatRow("Max Stamina", String.valueOf(stats.getMaxStamina()), ENERGY_COLOR),
            createDerivedStatRow("Melee Dmg", "+" + stats.getMeleeDamage(), "#c08080"),
            createDerivedStatRow("Dodge", stats.getDodgeChance() + "%", "#80a0c0"),
            createDerivedStatRow("Critical", stats.getCritChance() + "%", "#c0a080"),
            createDerivedStatRow("Trade Bonus", (stats.getTradeModifier() >= 0 ? "+" : "") + stats.getTradeModifier() + "%", "#c4a574"),
            createDerivedStatRow("Max Party", String.valueOf(stats.getMaxPartySize()), "#a0a0c0")
        );
        section.getChildren().add(derivedStats);
        
        // Experience section
        Label xpHeader = new Label("⭐ Experience");
        xpHeader.setTextFill(Color.web(GOLD));
        xpHeader.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        section.getChildren().add(xpHeader);
        
        VBox xpSection = new VBox(3);
        xpSection.setPadding(new Insets(5, 0, 5, 0));
        
        xpLabel = new Label("XP: " + stats.getExperience() + " / " + stats.getExperienceToNextLevel());
        xpLabel.setTextFill(Color.web(TEXT));
        xpLabel.setFont(Font.font("Georgia", 11));
        
        statPointsLabel = new Label("Stat Points: " + stats.getStatPoints());
        statPointsLabel.setTextFill(Color.web(stats.getStatPoints() > 0 ? "#ffd700" : TEXT_DIM));
        statPointsLabel.setFont(Font.font("Georgia", 11));
        
        xpSection.getChildren().addAll(xpLabel, statPointsLabel);
        section.getChildren().add(xpSection);
        
        // Wrap in ScrollPane for overflow
        ScrollPane scrollPane = new ScrollPane(section);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(300);
        
        VBox wrapper = new VBox(scrollPane);
        wrapper.setStyle("-fx-background-color: " + BG_DARK + "; -fx-background-radius: 0 0 6 6;");
        
        return wrapper;
    }
    
    private HBox createDerivedStatRow(String name, String value, String color) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(name + ":");
        nameLabel.setTextFill(Color.web(TEXT_DIM));
        nameLabel.setFont(Font.font("Georgia", 10));
        nameLabel.setPrefWidth(80);
        
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        
        row.getChildren().addAll(nameLabel, valueLabel);
        return row;
    }
    
    /**
     * Refreshes the attributes tab values.
     */
    private void refreshAttributes() {
        if (statLabels == null) return;
        
        CharacterStats stats = player.getCharacterStats();
        CharacterStats.Stat[] statTypes = CharacterStats.Stat.values();
        
        for (int i = 0; i < statTypes.length; i++) {
            CharacterStats.Stat stat = statTypes[i];
            statLabels[i].setText(String.valueOf(stats.getStat(stat)));
            statModLabels[i].setText(stats.getStatModifierDisplay(stat));
            int mod = stats.getStatModifier(stat);
            statModLabels[i].setTextFill(Color.web(mod >= 0 ? "#80c080" : "#c08080"));
        }
        
        xpLabel.setText("XP: " + stats.getExperience() + " / " + stats.getExperienceToNextLevel());
        statPointsLabel.setText("Stat Points: " + stats.getStatPoints());
        statPointsLabel.setTextFill(Color.web(stats.getStatPoints() > 0 ? "#ffd700" : TEXT_DIM));
        
        // Update level label
        levelLabel.setText("Level " + stats.getLevel() + " Adventurer");
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
