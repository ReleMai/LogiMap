import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Random;
import java.util.function.Consumer;

/**
 * World Generation Menu - Allows players to configure and preview their world before generation.
 * 
 * Features:
 * - World name and seed input
 * - Random name/seed generation
 * - Live preview of world terrain (low quality for performance)
 * - Starting region selection
 */
public class WorldGenMenu extends StackPane {
    
    // Style constants
    private static final String DARK_BG = "#1a1a1a";
    private static final String MEDIUM_BG = "#2a2a2a";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String TEXT_COLOR = "#e0e0e0";
    private static final String TEXT_DIM = "#888888";
    private static final String PANEL_BORDER = "#404040";
    
    // World names for random generation
    private static final String[] NAME_PREFIXES = {
        "New", "Old", "Great", "Lost", "Forgotten", "Ancient", "Northern", "Southern",
        "Eastern", "Western", "Crystal", "Shadow", "Golden", "Silver", "Iron", "Emerald"
    };
    private static final String[] NAME_SUFFIXES = {
        "haven", "ford", "port", "shire", "vale", "march", "reach", "lands",
        "realm", "kingdom", "territory", "frontier", "coast", "plains", "highlands"
    };
    
    // UI components
    private TextField worldNameField;
    private TextField seedField;
    private Canvas previewCanvas;
    private VBox startingRegionBox;
    private Button generateButton;
    
    // Preview state
    private long currentSeed = 12345L;
    private int[][] previewTerrain;
    private static final int PREVIEW_SIZE = 200;
    private AnimationTimer previewUpdater;
    
    // Starting regions
    private int[] selectedRegion = null;
    private int[][] startingRegions = new int[3][2];
    private ToggleGroup regionToggleGroup;
    
    // Callback when world is ready to generate
    private Consumer<WorldConfig> onGenerateWorld;
    private Runnable onBack;
    
    public WorldGenMenu() {
        setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Main layout container
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setMaxWidth(1200);
        mainLayout.setMaxHeight(800);
        
        // Title
        Label title = new Label("CREATE NEW WORLD");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web(ACCENT_COLOR));
        title.setPadding(new Insets(0, 0, 20, 0));
        
        VBox titleBox = new VBox(title);
        titleBox.setAlignment(Pos.CENTER);
        mainLayout.setTop(titleBox);
        
        // Left panel - World configuration
        VBox leftPanel = createLeftPanel();
        mainLayout.setLeft(leftPanel);
        
        // Right panel - Preview and region selection
        VBox rightPanel = createRightPanel();
        mainLayout.setRight(rightPanel);
        
        // Bottom - Generate button
        HBox bottomPanel = createBottomPanel();
        mainLayout.setBottom(bottomPanel);
        
        getChildren().add(mainLayout);
        setAlignment(Pos.CENTER);
        
        // Initialize preview
        updatePreview();
    }
    
    /**
     * Creates the left configuration panel.
     */
    private VBox createLeftPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setMinWidth(350);
        panel.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + PANEL_BORDER + ";" +
            "-fx-border-radius: 8;"
        );
        
        // Section 1: World Name and Seed
        VBox nameSection = createNameSeedSection();
        
        // Section 2: World Settings (placeholder for future)
        VBox settingsSection = createSettingsSection();
        
        panel.getChildren().addAll(nameSection, settingsSection);
        return panel;
    }
    
    /**
     * Creates the name and seed input section.
     */
    private VBox createNameSeedSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        Label header = createSectionHeader("WORLD IDENTITY");
        
        // World name input
        Label nameLabel = createLabel("World Name:");
        worldNameField = createTextField("Enter world name...");
        worldNameField.setText(generateRandomName());
        
        // Seed input
        Label seedLabel = createLabel("World Seed:");
        HBox seedBox = new HBox(10);
        seedField = createTextField("Enter seed number...");
        seedField.setText(String.valueOf(currentSeed));
        seedField.textProperty().addListener((obs, old, newVal) -> {
            try {
                currentSeed = Long.parseLong(newVal.trim());
                updatePreview();
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
        
        Button randomSeedBtn = createButton("🎲", "Generate random seed");
        randomSeedBtn.setOnAction(e -> {
            currentSeed = new Random().nextLong() & 0xFFFFFFFFL;
            seedField.setText(String.valueOf(currentSeed));
            updatePreview();
        });
        
        seedBox.getChildren().addAll(seedField, randomSeedBtn);
        HBox.setHgrow(seedField, Priority.ALWAYS);
        
        // Random name button
        Button randomNameBtn = createButton("Random Name", "Generate a random world name");
        randomNameBtn.setOnAction(e -> worldNameField.setText(generateRandomName()));
        
        // Randomize all button
        Button randomAllBtn = createButton("🎲 Randomize All", "Generate random name and seed");
        randomAllBtn.setStyle(randomAllBtn.getStyle() + "-fx-background-color: " + ACCENT_COLOR + ";");
        randomAllBtn.setOnAction(e -> {
            worldNameField.setText(generateRandomName());
            currentSeed = new Random().nextLong() & 0xFFFFFFFFL;
            seedField.setText(String.valueOf(currentSeed));
            updatePreview();
        });
        
        section.getChildren().addAll(
            header,
            nameLabel, worldNameField,
            seedLabel, seedBox,
            randomNameBtn,
            new Separator(),
            randomAllBtn
        );
        
        return section;
    }
    
    // Sliders for village/city settings
    private Slider villageCountSlider;
    private Slider cityCountSlider;
    private Slider agriculturalSlider;
    private Slider pastoralSlider;
    private Slider lumberSlider;
    private Slider miningSlider;
    private Slider fishingSlider;
    
    // Biome settings
    private Slider forestCoverSlider;
    private Slider desertCoverSlider;
    private Slider mountainCoverSlider;
    private Slider waterCoverSlider;
    
    // Labels for village type percentages
    private Label agriculturalLabel;
    private Label pastoralLabel;
    private Label lumberLabel;
    private Label miningLabel;
    private Label fishingLabel;
    private Label totalPercentLabel;
    
    // Labels for biome percentages
    private Label forestLabel;
    private Label desertLabel;
    private Label mountainLabel;
    private Label waterLabel;
    private Label grasslandLabel;  // Derived from remaining percentage
    private Label biomeTotalLabel;
    
    // Flags to prevent recursive updates during slider balancing
    private boolean isUpdatingTypeSliders = false;
    private boolean isUpdatingBiomeSliders = false;
    
    /**
     * Creates the world settings section with village/city controls and biome settings.
     */
    private VBox createSettingsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        Label header = createSectionHeader("SETTLEMENT SETTINGS");
        
        // Village count slider
        Label villageLabel = createLabel("Villages: 25");
        villageCountSlider = new Slider(5, 50, 25);
        villageCountSlider.setShowTickMarks(true);
        villageCountSlider.setMajorTickUnit(15);
        villageCountSlider.valueProperty().addListener((obs, old, newVal) -> {
            villageLabel.setText("Villages: " + newVal.intValue());
        });
        styleSlider(villageCountSlider);
        
        // City count slider
        Label cityLabel = createLabel("Cities: 3");
        cityCountSlider = new Slider(0, 10, 3);
        cityCountSlider.setShowTickMarks(true);
        cityCountSlider.setMajorTickUnit(5);
        cityCountSlider.valueProperty().addListener((obs, old, newVal) -> {
            cityLabel.setText("Cities: " + newVal.intValue());
        });
        styleSlider(cityCountSlider);
        
        // Separator
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: " + PANEL_BORDER + ";");
        
        // Village type distribution header
        Label typeHeader = createSectionHeader("VILLAGE TYPE DISTRIBUTION");
        totalPercentLabel = createLabel("Total: 100%");
        totalPercentLabel.setStyle(totalPercentLabel.getStyle() + "-fx-font-size: 11; -fx-text-fill: #66ff66;");
        
        // Type sliders (0-100, must balance to 100%)
        agriculturalSlider = createBalancedTypeSlider(20);
        pastoralSlider = createBalancedTypeSlider(20);
        lumberSlider = createBalancedTypeSlider(20);
        miningSlider = createBalancedTypeSlider(20);
        fishingSlider = createBalancedTypeSlider(20);
        
        // Separator
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: " + PANEL_BORDER + ";");
        
        // Biome settings header
        Label biomeHeader = createSectionHeader("BIOME SETTINGS (Total: 100%)");
        biomeTotalLabel = createLabel("Total: 100%");
        biomeTotalLabel.setStyle(biomeTotalLabel.getStyle() + "-fx-font-size: 11; -fx-text-fill: #66ff66;");
        
        // Biome coverage sliders (balanced to 100%)
        // Default: Forest 20%, Desert 10%, Mountains 15%, Water 30%, Grassland 25%
        forestCoverSlider = createBalancedBiomeSlider(20);
        desertCoverSlider = createBalancedBiomeSlider(10);
        mountainCoverSlider = createBalancedBiomeSlider(15);
        waterCoverSlider = createBalancedBiomeSlider(30);
        // Note: Grassland is calculated as 100 - (forest + desert + mountain + water)
        
        section.getChildren().addAll(
            header,
            villageLabel, villageCountSlider,
            cityLabel, cityCountSlider,
            sep1,
            typeHeader, totalPercentLabel,
            createBalancedSliderRow("🌾 Agricultural:", agriculturalSlider, () -> agriculturalLabel),
            createBalancedSliderRow("🐄 Pastoral:", pastoralSlider, () -> pastoralLabel),
            createBalancedSliderRow("🪓 Lumber:", lumberSlider, () -> lumberLabel),
            createBalancedSliderRow("⛏️ Mining:", miningSlider, () -> miningLabel),
            createBalancedSliderRow("🐟 Fishing:", fishingSlider, () -> fishingLabel),
            sep2,
            biomeHeader, biomeTotalLabel,
            createBiomeSliderRow("🌲 Forest:", forestCoverSlider, () -> forestLabel),
            createBiomeSliderRow("🏜️ Desert:", desertCoverSlider, () -> desertLabel),
            createBiomeSliderRow("⛰️ Mountains:", mountainCoverSlider, () -> mountainLabel),
            createBiomeSliderRow("🌊 Water:", waterCoverSlider, () -> waterLabel),
            createGrasslandRow()
        );
        
        return section;
    }
    
    /**
     * Creates a slider for village type distribution (balanced to 100%).
     */
    private Slider createBalancedTypeSlider(int defaultValue) {
        Slider slider = new Slider(0, 100, defaultValue);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.valueProperty().addListener((obs, old, newVal) -> {
            if (!isUpdatingTypeSliders) {
                balanceTypeSliders(slider, old.doubleValue(), newVal.doubleValue());
            }
            updateTotalPercent();
        });
        styleSlider(slider);
        return slider;
    }
    
    /**
     * Creates a slider for biome coverage distribution (balanced to 100%).
     */
    private Slider createBalancedBiomeSlider(int defaultValue) {
        Slider slider = new Slider(0, 100, defaultValue);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.valueProperty().addListener((obs, old, newVal) -> {
            if (!isUpdatingBiomeSliders) {
                balanceBiomeSliders(slider, old.doubleValue(), newVal.doubleValue());
            }
            updateBiomeTotalPercent();
        });
        styleSlider(slider);
        return slider;
    }
    
    /**
     * Updates the total percentage label and color based on sum of sliders.
     */
    private void updateTotalPercent() {
        int total = (int) agriculturalSlider.getValue() +
                    (int) pastoralSlider.getValue() +
                    (int) lumberSlider.getValue() +
                    (int) miningSlider.getValue() +
                    (int) fishingSlider.getValue();
        
        totalPercentLabel.setText("Total: " + total + "%");
        
        if (total == 100) {
            totalPercentLabel.setStyle("-fx-text-fill: #66ff66; -fx-font-weight: bold;"); // Green - perfect
        } else if (total < 100) {
            totalPercentLabel.setStyle("-fx-text-fill: #ffaa66; -fx-font-weight: bold;"); // Orange - under
        } else {
            totalPercentLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-weight: bold;"); // Red - over
        }
    }
    
    /**
     * Balances village type sliders so they always total exactly 100%.
     * When one slider changes, the others adjust proportionally.
     * Uses integer math with remainder distribution to avoid rounding errors.
     */
    private void balanceTypeSliders(Slider changedSlider, double oldValue, double newValue) {
        isUpdatingTypeSliders = true;
        
        Slider[] allSliders = {agriculturalSlider, pastoralSlider, lumberSlider, miningSlider, fishingSlider};
        int changedValue = (int) Math.round(newValue);
        int targetRemaining = 100 - changedValue; // Other sliders must sum to this
        
        // Calculate current total of other sliders
        int otherTotal = 0;
        java.util.List<Slider> otherSliders = new java.util.ArrayList<>();
        for (Slider s : allSliders) {
            if (s != changedSlider && s != null) {
                otherTotal += (int) Math.round(s.getValue());
                otherSliders.add(s);
            }
        }
        
        if (otherSliders.isEmpty()) {
            isUpdatingTypeSliders = false;
            return;
        }
        
        // Distribute targetRemaining across other sliders proportionally
        int[] newValues = new int[otherSliders.size()];
        int distributed = 0;
        
        if (otherTotal > 0) {
            // Distribute proportionally based on current values
            for (int i = 0; i < otherSliders.size(); i++) {
                double proportion = otherSliders.get(i).getValue() / otherTotal;
                newValues[i] = (int) Math.round(targetRemaining * proportion);
                newValues[i] = Math.max(0, Math.min(100, newValues[i])); // Clamp
                distributed += newValues[i];
            }
        } else {
            // All others are 0, distribute evenly
            int perSlider = targetRemaining / otherSliders.size();
            for (int i = 0; i < otherSliders.size(); i++) {
                newValues[i] = perSlider;
                distributed += perSlider;
            }
        }
        
        // Fix any rounding error by adjusting the largest slider
        int remainder = targetRemaining - distributed;
        if (remainder != 0 && !otherSliders.isEmpty()) {
            // Find the largest slider to adjust
            int maxIdx = 0;
            for (int i = 1; i < newValues.length; i++) {
                if (newValues[i] > newValues[maxIdx]) maxIdx = i;
            }
            newValues[maxIdx] = Math.max(0, Math.min(100, newValues[maxIdx] + remainder));
        }
        
        // Apply the new values
        for (int i = 0; i < otherSliders.size(); i++) {
            otherSliders.get(i).setValue(newValues[i]);
        }
        
        isUpdatingTypeSliders = false;
    }
    
    /**
     * Balances biome sliders so they always total 100% (with grassland as remainder).
     */
    private void balanceBiomeSliders(Slider changedSlider, double oldValue, double newValue) {
        isUpdatingBiomeSliders = true;
        
        Slider[] allSliders = {forestCoverSlider, desertCoverSlider, mountainCoverSlider, waterCoverSlider};
        double delta = newValue - oldValue;
        
        // Calculate total of other sliders
        double otherTotal = 0;
        int otherCount = 0;
        for (Slider s : allSliders) {
            if (s != changedSlider && s != null) {
                otherTotal += s.getValue();
                otherCount++;
            }
        }
        
        // Ensure the new value + others doesn't exceed 100 (leave room for grassland)
        double maxForThisSlider = 100 - otherTotal;
        if (newValue > maxForThisSlider) {
            changedSlider.setValue(maxForThisSlider);
        }
        
        isUpdatingBiomeSliders = false;
    }
    
    /**
     * Updates the biome total percentage and grassland display.
     * Total of all 4 adjustable biomes must equal 100%.
     */
    private void updateBiomeTotalPercent() {
        int forest = (int) forestCoverSlider.getValue();
        int desert = (int) desertCoverSlider.getValue();
        int mountain = (int) mountainCoverSlider.getValue();
        int water = (int) waterCoverSlider.getValue();
        int total = forest + desert + mountain + water;
        int grassland = Math.max(0, 100 - total);
        
        // Update labels
        if (biomeTotalLabel != null) {
            biomeTotalLabel.setText("Total: " + (total + grassland) + "% (Grassland fills remaining)");
            
            if (total <= 100) {
                biomeTotalLabel.setStyle("-fx-text-fill: #66ff66; -fx-font-weight: bold;"); // Green
            } else {
                biomeTotalLabel.setStyle("-fx-text-fill: #ff6666; -fx-font-weight: bold;"); // Red - over
            }
        }
        
        // Update grassland label
        if (grasslandLabel != null) {
            grasslandLabel.setText(grassland + "%");
            if (grassland == 0) {
                grasslandLabel.setStyle("-fx-text-fill: #ffaa66;"); // Orange warning - no grassland
            } else {
                grasslandLabel.setStyle("-fx-text-fill: " + ACCENT_COLOR + ";");
            }
        }
    }
    
    /**
     * Creates a labeled row with a biome slider showing percentage.
     */
    private HBox createBiomeSliderRow(String label, Slider slider, java.util.function.Supplier<Label> labelRef) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = createLabel(label);
        nameLabel.setMinWidth(100);
        
        Label valueLabel = createLabel((int)slider.getValue() + "%");
        valueLabel.setMinWidth(40);
        valueLabel.setStyle(valueLabel.getStyle() + "-fx-text-fill: " + ACCENT_COLOR + ";");
        
        // Store reference based on slider
        if (slider == forestCoverSlider) forestLabel = valueLabel;
        else if (slider == desertCoverSlider) desertLabel = valueLabel;
        else if (slider == mountainCoverSlider) mountainLabel = valueLabel;
        else if (slider == waterCoverSlider) waterLabel = valueLabel;
        
        slider.valueProperty().addListener((obs, old, newVal) -> {
            valueLabel.setText(newVal.intValue() + "%");
        });
        
        HBox.setHgrow(slider, Priority.ALWAYS);
        row.getChildren().addAll(nameLabel, slider, valueLabel);
        return row;
    }
    
    /**
     * Creates the grassland row (derived from remaining percentage).
     */
    private HBox createGrasslandRow() {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = createLabel("🌿 Grassland:");
        nameLabel.setMinWidth(100);
        
        Label infoLabel = createLabel("(auto-calculated from remaining)");
        infoLabel.setStyle("-fx-font-size: 10; -fx-text-fill: " + TEXT_DIM + ";");
        
        grasslandLabel = createLabel("25%");
        grasslandLabel.setMinWidth(40);
        grasslandLabel.setStyle(grasslandLabel.getStyle() + "-fx-text-fill: " + ACCENT_COLOR + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        row.getChildren().addAll(nameLabel, infoLabel, spacer, grasslandLabel);
        return row;
    }
    
    /**
     * Creates a labeled row with a balanced slider showing percentage.
     */
    private HBox createBalancedSliderRow(String label, Slider slider, java.util.function.Supplier<Label> labelRef) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = createLabel(label);
        nameLabel.setMinWidth(100);
        
        Label valueLabel = createLabel((int)slider.getValue() + "%");
        valueLabel.setMinWidth(40);
        valueLabel.setStyle(valueLabel.getStyle() + "-fx-text-fill: " + ACCENT_COLOR + ";");
        
        // Store reference based on slider
        if (slider == agriculturalSlider) agriculturalLabel = valueLabel;
        else if (slider == pastoralSlider) pastoralLabel = valueLabel;
        else if (slider == lumberSlider) lumberLabel = valueLabel;
        else if (slider == miningSlider) miningLabel = valueLabel;
        else if (slider == fishingSlider) fishingLabel = valueLabel;
        
        slider.valueProperty().addListener((obs, old, newVal) -> {
            valueLabel.setText(newVal.intValue() + "%");
        });
        
        HBox.setHgrow(slider, Priority.ALWAYS);
        row.getChildren().addAll(nameLabel, slider, valueLabel);
        return row;
    }
    
    /**
     * Applies consistent styling to a slider.
     */
    private void styleSlider(Slider slider) {
        slider.setStyle(
            "-fx-control-inner-background: " + DARK_BG + ";" +
            "-fx-accent: " + ACCENT_COLOR + ";"
        );
    }
    
    /**
     * Creates the right panel with preview and region selection.
     */
    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setMinWidth(500);
        panel.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + PANEL_BORDER + ";" +
            "-fx-border-radius: 8;"
        );
        
        // Preview section
        VBox previewSection = createPreviewSection();
        
        // Starting region selection
        VBox regionSection = createRegionSection();
        
        panel.getChildren().addAll(previewSection, regionSection);
        return panel;
    }
    
    /**
     * Creates the world preview section.
     */
    private VBox createPreviewSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 6;"
        );
        section.setAlignment(Pos.CENTER);
        
        Label header = createSectionHeader("WORLD PREVIEW");
        
        // Preview canvas
        previewCanvas = new Canvas(400, 400);
        previewCanvas.setStyle("-fx-effect: dropshadow(gaussian, black, 10, 0.3, 0, 2);");
        
        StackPane canvasContainer = new StackPane(previewCanvas);
        canvasContainer.setStyle(
            "-fx-background-color: #000;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 4;"
        );
        canvasContainer.setMaxSize(404, 404);
        
        Label hint = createLabel("Preview updates automatically with seed changes");
        hint.setStyle(hint.getStyle() + "-fx-font-size: 11; -fx-opacity: 0.6;");
        
        section.getChildren().addAll(header, canvasContainer, hint);
        return section;
    }
    
    /**
     * Creates the starting region selection section.
     */
    private VBox createRegionSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        Label header = createSectionHeader("SELECT STARTING REGION");
        
        startingRegionBox = new VBox(8);
        regionToggleGroup = new ToggleGroup();
        
        // Will be populated when preview updates
        Label placeholder = createLabel("Generating starting locations...");
        startingRegionBox.getChildren().add(placeholder);
        
        section.getChildren().addAll(header, startingRegionBox);
        return section;
    }
    
    /**
     * Creates the bottom panel with generate button.
     */
    private HBox createBottomPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(20, 0, 0, 0));
        panel.setAlignment(Pos.CENTER);
        
        // Reset button to restore all defaults
        Button resetButton = createButton("⟳ Reset All", "Reset all sliders to default values");
        resetButton.setStyle(resetButton.getStyle() + "-fx-padding: 15 25 15 25;");
        resetButton.setOnAction(e -> resetAllSliders());
        
        generateButton = new Button("⚡ GENERATE WORLD");
        generateButton.setFont(Font.font("System", FontWeight.BOLD, 18));
        generateButton.setStyle(
            "-fx-background-color: " + ACCENT_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 15 40 15 40;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        generateButton.setOnMouseEntered(e -> 
            generateButton.setStyle(generateButton.getStyle().replace(ACCENT_COLOR, "#5ab0ff")));
        generateButton.setOnMouseExited(e -> 
            generateButton.setStyle(generateButton.getStyle().replace("#5ab0ff", ACCENT_COLOR)));
        
        generateButton.setOnAction(e -> startWorldGeneration());
        
        Button backButton = createButton("← Back", "Return to main menu");
        backButton.setStyle(backButton.getStyle() + "-fx-padding: 15 30 15 30;");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });
        
        panel.getChildren().addAll(backButton, resetButton, generateButton);
        return panel;
    }
    
    /**
     * Resets all sliders to their default values.
     */
    private void resetAllSliders() {
        // Prevent recursive updates during reset
        isUpdatingTypeSliders = true;
        isUpdatingBiomeSliders = true;
        
        // Reset biome sliders
        forestCoverSlider.setValue(15);
        desertCoverSlider.setValue(10);
        mountainCoverSlider.setValue(15);
        waterCoverSlider.setValue(10);
        // Grassland is calculated from remainder (50%)
        
        // Reset village type sliders (total 100%)
        agriculturalSlider.setValue(40);
        pastoralSlider.setValue(25);
        lumberSlider.setValue(15);
        miningSlider.setValue(10);
        fishingSlider.setValue(10);
        
        // Reset count sliders
        villageCountSlider.setValue(30);
        cityCountSlider.setValue(5);
        
        // Re-enable slider updates
        isUpdatingTypeSliders = false;
        isUpdatingBiomeSliders = false;
        
        // Update all labels
        updateTotalPercent();
        updateBiomeTotalPercent();
        
        // Update preview
        updatePreview();
    }
    
    /**
     * Updates the preview with current seed.
     */
    private void updatePreview() {
        // Generate low-resolution preview
        previewTerrain = generatePreviewTerrain(currentSeed, PREVIEW_SIZE);
        
        // Find starting regions
        findStartingRegions();
        
        // Draw preview
        drawPreview();
        
        // Update region selection UI
        updateRegionSelection();
    }
    
    /**
     * Generates a low-resolution terrain preview using the SAME algorithm as WorldGenerator.
     * This ensures the preview matches the actual generated world.
     */
    private int[][] generatePreviewTerrain(long seed, int size) {
        int[][] terrain = new int[size][size];
        
        // Match WorldGenerator's configuration exactly
        double landCoverage = 0.55;
        double seaLevel = 1.0 - landCoverage;  // 0.45
        
        // Same seeds as WorldGenerator
        long continentSeed = seed;
        long detailSeed = seed * 2 + 1;
        long mountainSeed = seed * 3 + 2;
        
        // Scale factor: preview size to 1000x1000 world
        double scale = 1000.0 / size;
        
        for (int px = 0; px < size; px++) {
            for (int py = 0; py < size; py++) {
                // Map preview coordinates to world coordinates
                int x = (int) (px * scale);
                int y = (int) (py * scale);
                
                // EXACT same algorithm as WorldGenerator.generateElevation()
                // Continental shapes (large scale)
                double continental = octaveNoise(x, y, continentSeed, 0.002, 4, 0.5);
                
                // Regional variation (medium scale)
                double regional = octaveNoise(x, y, detailSeed, 0.008, 3, 0.5);
                
                // Local detail (small scale)
                double detail = octaveNoise(x, y, detailSeed + 100, 0.025, 2, 0.5);
                
                // Combine layers - same weights as WorldGenerator
                double base = continental * 0.6 + regional * 0.3 + detail * 0.1;
                
                // Islands - same algorithm
                double islandNoise = octaveNoise(x, y, detailSeed + 200, 0.015, 2, 0.5);
                if (islandNoise > 0.65 && base < seaLevel && base > seaLevel - 0.15) {
                    base += (islandNoise - 0.65) * 0.3 * 0.8;
                }
                
                // Mountains on high ground - same algorithm
                if (base > seaLevel + 0.1) {
                    double mountainNoise = octaveNoise(x, y, mountainSeed, 0.012, 4, 0.6);
                    double ridgeNoise = ridgedNoise(x, y, mountainSeed + 50, 0.008, 3);
                    double mountainFactor = (base - seaLevel) * mountainNoise * ridgeNoise;
                    base += mountainFactor * 0.4 * 0.4;
                }
                
                // Hills in mid elevations
                if (base > seaLevel && base < seaLevel + 0.3) {
                    double hillNoise = octaveNoise(x, y, detailSeed + 300, 0.02, 2, 0.5);
                    base += hillNoise * 0.5 * 0.08;
                }
                
                double elev = clamp(base, 0, 1);
                
                // Classify terrain - match WorldGenerator thresholds
                if (elev < seaLevel - 0.15) terrain[px][py] = 0;      // Deep water
                else if (elev < seaLevel - 0.05) terrain[px][py] = 1; // Ocean
                else if (elev < seaLevel) terrain[px][py] = 1;        // Shallow water
                else if (elev < seaLevel + 0.02) terrain[px][py] = 2; // Beach
                else if (elev < 0.52) terrain[px][py] = 3;            // Lowland (grass)
                else if (elev < 0.60) terrain[px][py] = 4;            // Hills
                else if (elev < 0.72) terrain[px][py] = 5;            // Rocky hills
                else if (elev < 0.85) terrain[px][py] = 5;            // Mountains
                else terrain[px][py] = 6;                              // Snow peaks
            }
        }
        
        return terrain;
    }
    
    /**
     * Multi-octave noise - matches WorldGenerator exactly.
     */
    private double octaveNoise(int x, int y, long noiseSeed, double scale, int octaves, double persistence) {
        double value = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            value += noiseValue(x * scale * frequency, y * scale * frequency, noiseSeed + i * 1000) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return (value / maxValue + 1) / 2;
    }
    
    /**
     * Ridged noise for mountains - matches WorldGenerator.
     */
    private double ridgedNoise(int x, int y, long noiseSeed, double scale, int octaves) {
        double value = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            double n = noiseValue(x * scale * frequency, y * scale * frequency, noiseSeed + i * 1000);
            n = 1.0 - Math.abs(n);
            n = n * n;
            value += n * amplitude;
            maxValue += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }
        
        return value / maxValue;
    }
    
    /**
     * Value noise function - matches WorldGenerator exactly.
     */
    private double noiseValue(double x, double y, long noiseSeed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        double fx = x - ix;
        double fy = y - iy;
        
        double u = smoothstep(fx);
        double v = smoothstep(fy);
        
        double n00 = hash(ix, iy, noiseSeed);
        double n10 = hash(ix + 1, iy, noiseSeed);
        double n01 = hash(ix, iy + 1, noiseSeed);
        double n11 = hash(ix + 1, iy + 1, noiseSeed);
        
        double nx0 = lerp(n00, n10, u);
        double nx1 = lerp(n01, n11, u);
        return lerp(nx0, nx1, v);
    }
    
    private double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }
    
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private double hash(int x, int y, long seed) {
        long h = seed;
        h ^= x * 374761393L;
        h ^= y * 668265263L;
        h = (h ^ (h >> 13)) * 1274126177L;
        return ((h & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL) * 2 - 1;
    }
    
    /**
     * Finds good starting regions based on terrain.
     */
    private void findStartingRegions() {
        Random rand = new Random(currentSeed + 999);
        int regionSpacing = PREVIEW_SIZE / 4;
        int attempts = 0;
        int found = 0;
        
        while (found < 3 && attempts < 100) {
            int x = regionSpacing + rand.nextInt(PREVIEW_SIZE - 2 * regionSpacing);
            int y = regionSpacing + rand.nextInt(PREVIEW_SIZE - 2 * regionSpacing);
            
            // Check if this is a good location (on land, not too close to others)
            if (isGoodStartingLocation(x, y) && !tooCloseToOtherRegions(x, y, found)) {
                startingRegions[found][0] = x;
                startingRegions[found][1] = y;
                found++;
            }
            attempts++;
        }
        
        // Fill remaining with fallback locations
        while (found < 3) {
            startingRegions[found][0] = PREVIEW_SIZE / 4 + (found * PREVIEW_SIZE / 3);
            startingRegions[found][1] = PREVIEW_SIZE / 2;
            found++;
        }
        
        // Default to first region
        if (selectedRegion == null) {
            selectedRegion = startingRegions[0];
        }
    }
    
    private boolean isGoodStartingLocation(int x, int y) {
        if (x < 5 || x >= PREVIEW_SIZE - 5 || y < 5 || y >= PREVIEW_SIZE - 5) return false;
        
        // Check surrounding area for land
        int landCount = 0;
        int waterCount = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int t = previewTerrain[x + dx][y + dy];
                if (t >= 2 && t <= 4) landCount++;
                if (t <= 1) waterCount++;
            }
        }
        
        // Good location: mostly land, some water nearby for trade
        return landCount >= 30 && waterCount >= 5 && waterCount <= 20;
    }
    
    private boolean tooCloseToOtherRegions(int x, int y, int regionCount) {
        int minDist = PREVIEW_SIZE / 5;
        for (int i = 0; i < regionCount; i++) {
            int dx = x - startingRegions[i][0];
            int dy = y - startingRegions[i][1];
            if (dx * dx + dy * dy < minDist * minDist) return true;
        }
        return false;
    }
    
    /**
     * Draws the terrain preview.
     */
    private void drawPreview() {
        GraphicsContext gc = previewCanvas.getGraphicsContext2D();
        double scale = previewCanvas.getWidth() / PREVIEW_SIZE;
        
        // Terrain colors
        Color[] terrainColors = {
            Color.web("#1a3a5c"),  // Deep water
            Color.web("#2a5a8c"),  // Shallow water
            Color.web("#c4b896"),  // Beach
            Color.web("#6b8e23"),  // Lowland (grass)
            Color.web("#8b7355"),  // Hills
            Color.web("#808080"),  // Mountains
            Color.web("#f0f0f0")   // Snow
        };
        
        // Draw terrain
        for (int x = 0; x < PREVIEW_SIZE; x++) {
            for (int y = 0; y < PREVIEW_SIZE; y++) {
                int t = previewTerrain[x][y];
                gc.setFill(terrainColors[Math.min(t, terrainColors.length - 1)]);
                gc.fillRect(x * scale, y * scale, scale + 1, scale + 1);
            }
        }
        
        // Draw starting region markers
        gc.setStroke(Color.web(ACCENT_COLOR));
        gc.setLineWidth(2);
        for (int i = 0; i < 3; i++) {
            double rx = startingRegions[i][0] * scale;
            double ry = startingRegions[i][1] * scale;
            double size = 20;
            
            // Highlight selected region
            if (selectedRegion == startingRegions[i]) {
                gc.setFill(Color.web(ACCENT_COLOR, 0.3));
                gc.fillOval(rx - size, ry - size, size * 2, size * 2);
            }
            
            gc.strokeOval(rx - size / 2, ry - size / 2, size, size);
            gc.setFill(Color.WHITE);
            gc.fillText(String.valueOf(i + 1), rx - 4, ry + 4);
        }
    }
    
    // Store region names to pass to world generation
    private String[] regionNames = new String[3];
    
    /**
     * Updates the region selection UI.
     */
    private void updateRegionSelection() {
        startingRegionBox.getChildren().clear();
        
        String[] regionTypes = {"Coastal Valley", "Highland Plains", "River Delta"};
        
        for (int i = 0; i < 3; i++) {
            final int regionIndex = i;
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(regionToggleGroup);
            rb.setSelected(i == 0);
            
            // Use NameGenerator for consistent, deterministic region names
            // Map preview coordinates to world coordinates for naming
            int worldX = (int) ((startingRegions[i][0] / (double) PREVIEW_SIZE) * 1000);
            int worldY = (int) ((startingRegions[i][1] / (double) PREVIEW_SIZE) * 1000);
            String regionName = NameGenerator.getLocationNameForCoords(worldX, worldY, currentSeed);
            regionNames[i] = regionName;
            
            String regionType = regionTypes[i % regionTypes.length];
            
            VBox regionInfo = new VBox(2);
            Label nameLabel = new Label((i + 1) + ". " + regionName);
            nameLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-weight: bold;");
            Label typeLabel = new Label(regionType);
            typeLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-opacity: 0.7; -fx-font-size: 11;");
            regionInfo.getChildren().addAll(nameLabel, typeLabel);
            
            HBox regionRow = new HBox(10, rb, regionInfo);
            regionRow.setAlignment(Pos.CENTER_LEFT);
            regionRow.setPadding(new Insets(5));
            regionRow.setStyle("-fx-background-color: " + MEDIUM_BG + "; -fx-background-radius: 4;");
            regionRow.setOnMouseClicked(e -> {
                rb.setSelected(true);
                selectedRegion = startingRegions[regionIndex];
                drawPreview();
            });
            
            rb.setOnAction(e -> {
                selectedRegion = startingRegions[regionIndex];
                drawPreview();
            });
            
            startingRegionBox.getChildren().add(regionRow);
        }
    }
    
    private String generateRegionName(Random rand) {
        // Fallback method - use NameGenerator instead
        return NameGenerator.getRandomLocationName();
    }
    
    /**
     * Starts the world generation process.
     */
    private void startWorldGeneration() {
        if (onGenerateWorld != null) {
            WorldConfig config = new WorldConfig();
            config.worldName = worldNameField.getText().trim();
            if (config.worldName.isEmpty()) {
                config.worldName = generateRandomName();
            }
            config.seed = currentSeed;
            config.startX = (int) ((selectedRegion[0] / (double) PREVIEW_SIZE) * 1000);
            config.startY = (int) ((selectedRegion[1] / (double) PREVIEW_SIZE) * 1000);
            
            // Find the selected region index and store the region name
            for (int i = 0; i < 3; i++) {
                if (selectedRegion == startingRegions[i]) {
                    config.startRegionName = regionNames[i];
                    break;
                }
            }
            
            // Settlement settings
            config.totalVillages = (int) villageCountSlider.getValue();
            config.totalCities = (int) cityCountSlider.getValue();
            
            // Village type distribution (normalize to 100% if needed)
            int total = (int) agriculturalSlider.getValue() +
                        (int) pastoralSlider.getValue() +
                        (int) lumberSlider.getValue() +
                        (int) miningSlider.getValue() +
                        (int) fishingSlider.getValue();
            
            if (total > 0) {
                double scale = 100.0 / total;
                config.agriculturalPercent = (int) Math.round(agriculturalSlider.getValue() * scale);
                config.pastoralPercent = (int) Math.round(pastoralSlider.getValue() * scale);
                config.lumberPercent = (int) Math.round(lumberSlider.getValue() * scale);
                config.miningPercent = (int) Math.round(miningSlider.getValue() * scale);
                config.fishingPercent = (int) Math.round(fishingSlider.getValue() * scale);
            }
            
            // Biome settings (normalized to 100%)
            int forest = (int) forestCoverSlider.getValue();
            int desert = (int) desertCoverSlider.getValue();
            int mountain = (int) mountainCoverSlider.getValue();
            int water = (int) waterCoverSlider.getValue();
            int biomeTotal = forest + desert + mountain + water;
            
            // If over 100%, scale down proportionally
            if (biomeTotal > 100) {
                double scale = 100.0 / biomeTotal;
                forest = (int) Math.round(forest * scale);
                desert = (int) Math.round(desert * scale);
                mountain = (int) Math.round(mountain * scale);
                water = (int) Math.round(water * scale);
            }
            
            config.forestPercent = forest;
            config.desertPercent = desert;
            config.mountainPercent = mountain;
            config.waterPercent = water;
            // Grassland fills the remainder (100 - forest - desert - mountain - water)
            
            onGenerateWorld.accept(config);
        }
    }
    
    /**
     * Generates a random world name using NameGenerator.
     */
    private String generateRandomName() {
        return NameGenerator.generateWorldName(System.currentTimeMillis());
    }
    
    // ==================== UI Helper Methods ====================
    
    private Label createSectionHeader(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(ACCENT_COLOR));
        label.setPadding(new Insets(0, 0, 5, 0));
        return label;
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(TEXT_COLOR));
        return label;
    }
    
    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-border-color: " + PANEL_BORDER + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8;"
        );
        return field;
    }
    
    private Button createButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 8 15 8 15;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }
    
    /**
     * Sets the callback for when world generation should begin.
     */
    public void setOnGenerateWorld(Consumer<WorldConfig> callback) {
        this.onGenerateWorld = callback;
    }
    
    /**
     * Sets the callback for returning to main menu.
     */
    public void setOnBack(Runnable callback) {
        this.onBack = callback;
    }
    
    /**
     * Configuration for world generation.
     */
    public static class WorldConfig {
        public String worldName;
        public long seed;
        public int startX;
        public int startY;
        public String startRegionName;
        
        // Village/City quantity settings
        public int totalVillages = 25;  // Default village count
        public int totalCities = 3;     // Default city count
        
        // Village type distribution (percentages, should sum to 100)
        public int agriculturalPercent = 20;
        public int pastoralPercent = 20;
        public int lumberPercent = 20;
        public int miningPercent = 20;
        public int fishingPercent = 20;
        
        // Biome settings (percentages)
        public int forestPercent = 30;
        public int desertPercent = 15;
        public int mountainPercent = 20;
        public int waterPercent = 45;
    }
}
