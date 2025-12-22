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
    
    /**
     * Creates the world settings section (placeholder for future features).
     */
    private VBox createSettingsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        Label header = createSectionHeader("WORLD SETTINGS");
        
        Label placeholder = createLabel("Additional settings coming soon...");
        placeholder.setStyle(placeholder.getStyle() + "-fx-font-style: italic; -fx-opacity: 0.6;");
        
        // Placeholder sliders for future settings
        VBox sliders = new VBox(8);
        sliders.setOpacity(0.5);
        
        Label landLabel = createLabel("Land Coverage:");
        Slider landSlider = new Slider(0.3, 0.8, 0.55);
        landSlider.setDisable(true);
        
        Label mountainLabel = createLabel("Mountain Frequency:");
        Slider mountainSlider = new Slider(0.1, 0.8, 0.4);
        mountainSlider.setDisable(true);
        
        sliders.getChildren().addAll(landLabel, landSlider, mountainLabel, mountainSlider);
        
        section.getChildren().addAll(header, placeholder, sliders);
        return section;
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
        
        panel.getChildren().addAll(backButton, generateButton);
        return panel;
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
    }
}
