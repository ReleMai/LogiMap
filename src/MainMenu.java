import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;

/**
 * MainMenu - The game's main menu with animated background and stylized UI.
 * 
 * Features:
 * - High-quality animated terrain background
 * - Spinning planet logo behind title
 * - New Game, Load, Settings, Exit buttons
 * - Update panel with latest changes
 */
public class MainMenu extends StackPane {
    
    // Style constants
    private static final String DARK_BG = "#0a0a12";
    private static final String MEDIUM_BG = "#1a1a2e";
    private static final String LIGHT_BG = "#2a2a4e";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String ACCENT_GLOW = "#6ab4ff";
    private static final String TEXT_COLOR = "#e0e0e0";
    private static final String GOLD_COLOR = "#ffd700";
    
    // Animation components
    private Canvas backgroundCanvas;
    private Canvas planetCanvas;
    private AnimationTimer backgroundAnimator;
    private AnimationTimer planetAnimator;
    private double backgroundOffset = 0;
    private double planetRotation = 0;
    
    // Terrain data for background
    private int[][] backgroundTerrain;
    private static final int BG_WIDTH = 400;
    private static final int BG_HEIGHT = 250;
    
    // UI components
    private VBox updatePanel;
    private boolean updatePanelVisible = false;
    private TranslateTransition updateSlideIn;
    private TranslateTransition updateSlideOut;
    
    // Callbacks
    private Runnable onNewGame;
    private Runnable onLoadGame;
    private Runnable onSettings;
    private Runnable onExit;
    
    public MainMenu() {
        setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Generate background terrain
        generateBackgroundTerrain();
        
        // Create layers
        createBackgroundLayer();
        createContentLayer();
        createUpdatePanel();
        
        // Start animations
        startAnimations();
    }
    
    // ==================== Background Layer ====================
    
    private void createBackgroundLayer() {
        backgroundCanvas = new Canvas(1920, 1080);
        
        // Apply blur effect for dreamy background
        GaussianBlur blur = new GaussianBlur(3);
        backgroundCanvas.setEffect(blur);
        
        // Bind to parent size
        backgroundCanvas.widthProperty().bind(widthProperty());
        backgroundCanvas.heightProperty().bind(heightProperty());
        
        getChildren().add(backgroundCanvas);
    }
    
    private void generateBackgroundTerrain() {
        backgroundTerrain = new int[BG_WIDTH][BG_HEIGHT];
        long seed = System.currentTimeMillis();
        
        double seaLevel = 0.45;
        
        for (int x = 0; x < BG_WIDTH; x++) {
            for (int y = 0; y < BG_HEIGHT; y++) {
                double value = octaveNoise(x, y, seed, 0.008, 4, 0.5);
                value += octaveNoise(x, y, seed + 100, 0.02, 2, 0.5) * 0.3;
                
                if (value < seaLevel - 0.15) backgroundTerrain[x][y] = 0;      // Deep water
                else if (value < seaLevel - 0.05) backgroundTerrain[x][y] = 1; // Ocean
                else if (value < seaLevel) backgroundTerrain[x][y] = 2;        // Shallow
                else if (value < seaLevel + 0.02) backgroundTerrain[x][y] = 3; // Beach
                else if (value < seaLevel + 0.15) backgroundTerrain[x][y] = 4; // Grass
                else if (value < seaLevel + 0.25) backgroundTerrain[x][y] = 5; // Forest
                else if (value < seaLevel + 0.35) backgroundTerrain[x][y] = 6; // Hills
                else if (value < seaLevel + 0.45) backgroundTerrain[x][y] = 7; // Mountain
                else backgroundTerrain[x][y] = 8;                               // Snow
            }
        }
    }
    
    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();
        
        // Dark gradient overlay base
        gc.setFill(Color.web(DARK_BG));
        gc.fillRect(0, 0, width, height);
        
        // Calculate scale to ensure seamless coverage
        double scale = Math.max(width / BG_WIDTH, height / BG_HEIGHT) * 1.1;
        
        // Terrain colors - slightly muted for background
        Color[] terrainColors = {
            Color.web("#0a1425"),  // Deep water
            Color.web("#0f2040"),  // Ocean
            Color.web("#1a4060"),  // Shallow
            Color.web("#2a5a4a"),  // Beach/coastal
            Color.web("#2a5038"),  // Grass
            Color.web("#1e4028"),  // Forest
            Color.web("#404050"),  // Hills
            Color.web("#505060"),  // Mountain
            Color.web("#606070")   // Snow
        };
        
        // Use sub-pixel smooth scrolling
        double smoothOffset = backgroundOffset;
        
        // Calculate the pixel offset for smooth scrolling
        double pixelOffset = smoothOffset * scale;
        
        // Draw terrain as one continuous image
        for (int x = 0; x < BG_WIDTH; x++) {
            for (int y = 0; y < BG_HEIGHT; y++) {
                int terrainType = backgroundTerrain[x][y];
                
                // Calculate screen position with smooth offset
                double screenX = x * scale - (pixelOffset % (BG_WIDTH * scale));
                double screenY = y * scale;
                
                // Draw tile at main position
                gc.setFill(terrainColors[Math.min(terrainType, terrainColors.length - 1)]);
                gc.fillRect(screenX, screenY, scale + 0.5, scale + 0.5);
                
                // Draw wrapped tile for seamless scrolling
                if (screenX < 0) {
                    gc.fillRect(screenX + BG_WIDTH * scale, screenY, scale + 0.5, scale + 0.5);
                } else if (screenX + scale > width - BG_WIDTH * scale) {
                    gc.fillRect(screenX - BG_WIDTH * scale, screenY, scale + 0.5, scale + 0.5);
                }
            }
        }
        
        // Strong vignette overlay for professional look
        RadialGradient vignette = new RadialGradient(
            0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.4, Color.TRANSPARENT),
            new Stop(0.7, Color.color(0, 0, 0, 0.4)),
            new Stop(0.85, Color.color(0, 0, 0, 0.7)),
            new Stop(1.0, Color.color(0, 0, 0, 0.95))
        );
        gc.setFill(vignette);
        gc.fillRect(0, 0, width, height);
        
        // Edge darkening - all four sides
        // Top edge
        LinearGradient topEdge = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0, 0, 0, 0.8)),
            new Stop(0.15, Color.color(0, 0, 0, 0.3)),
            new Stop(0.3, Color.TRANSPARENT)
        );
        gc.setFill(topEdge);
        gc.fillRect(0, 0, width, height * 0.3);
        
        // Bottom edge
        LinearGradient bottomEdge = new LinearGradient(
            0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0, 0, 0, 0.8)),
            new Stop(0.15, Color.color(0, 0, 0, 0.3)),
            new Stop(0.3, Color.TRANSPARENT)
        );
        gc.setFill(bottomEdge);
        gc.fillRect(0, height * 0.7, width, height * 0.3);
        
        // Left edge
        LinearGradient leftEdge = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0, 0, 0, 0.7)),
            new Stop(0.1, Color.color(0, 0, 0, 0.2)),
            new Stop(0.2, Color.TRANSPARENT)
        );
        gc.setFill(leftEdge);
        gc.fillRect(0, 0, width * 0.2, height);
        
        // Right edge
        LinearGradient rightEdge = new LinearGradient(
            1, 0, 0, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0, 0, 0, 0.7)),
            new Stop(0.1, Color.color(0, 0, 0, 0.2)),
            new Stop(0.2, Color.TRANSPARENT)
        );
        gc.setFill(rightEdge);
        gc.fillRect(width * 0.8, 0, width * 0.2, height);
    }
    
    // ==================== Content Layer ====================
    
    private void createContentLayer() {
        VBox contentBox = new VBox(25);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(50));
        
        // Logo section with spinning planet
        StackPane logoSection = createLogoSection();
        
        // Menu buttons
        VBox buttonSection = createButtonSection();
        
        // Version info
        Label versionLabel = new Label("Version 1.0.0 - Alpha");
        versionLabel.setFont(Font.font("Arial", 11));
        versionLabel.setTextFill(Color.web(TEXT_COLOR, 0.5));
        
        contentBox.getChildren().addAll(logoSection, buttonSection, versionLabel);
        
        // Position content
        VBox.setVgrow(logoSection, Priority.NEVER);
        VBox.setVgrow(buttonSection, Priority.NEVER);
        
        getChildren().add(contentBox);
        
        // Update button positioned in bottom-right corner
        Button updateBtn = createUpdateButton();
        updateBtn.setOnAction(e -> toggleUpdatePanel());
        StackPane.setAlignment(updateBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(updateBtn, new Insets(0, 30, 30, 0));
        getChildren().add(updateBtn);
    }
    
    private StackPane createLogoSection() {
        StackPane logoContainer = new StackPane();
        logoContainer.setMaxHeight(160);
        logoContainer.setMinHeight(160);
        
        // Planet canvas (behind title) - larger sphere
        planetCanvas = new Canvas(140, 140);
        
        // Title box - smaller compact rectangle
        VBox titleBox = new VBox(2);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(12, 30, 12, 30));
        titleBox.setMaxWidth(220);
        titleBox.setStyle(
            "-fx-background-color: rgba(26, 26, 46, 0.95);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;"
        );
        
        // Drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(ACCENT_COLOR, 0.6));
        shadow.setRadius(15);
        shadow.setSpread(0.1);
        titleBox.setEffect(shadow);
        
        // Game title - smaller font
        Text titleText = new Text("LOGIMAP");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleText.setFill(new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(ACCENT_GLOW)),
            new Stop(0.5, Color.WHITE),
            new Stop(1, Color.web(ACCENT_COLOR))
        ));
        
        // Glow effect on title
        Glow glow = new Glow(0.5);
        titleText.setEffect(glow);
        
        titleBox.getChildren().add(titleText);
        
        // Layer planet behind title - planet is larger and visible around edges
        logoContainer.getChildren().addAll(planetCanvas, titleBox);
        StackPane.setAlignment(planetCanvas, Pos.CENTER);
        StackPane.setAlignment(titleBox, Pos.CENTER);
        
        return logoContainer;
    }
    
    private void drawPlanet() {
        GraphicsContext gc = planetCanvas.getGraphicsContext2D();
        double size = planetCanvas.getWidth();
        double centerX = size / 2;
        double centerY = size / 2;
        double radius = size * 0.48;
        
        // Clear
        gc.clearRect(0, 0, size, size);
        
        // Planet base with gradient - matches LoadingScreen style
        RadialGradient planetGradient = new RadialGradient(
            0, 0, 
            centerX - radius * 0.3, centerY - radius * 0.3,
            radius * 1.2,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#4a7a9f")),
            new Stop(0.3, Color.web("#2a5a7f")),
            new Stop(0.7, Color.web("#1a3a5f")),
            new Stop(1, Color.web("#0a1a2f"))
        );
        gc.setFill(planetGradient);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw rotating terrain features
        gc.save();
        gc.beginPath();
        gc.arc(centerX, centerY, radius, radius, 0, 360);
        gc.closePath();
        gc.clip();
        
        // Draw land masses that rotate
        gc.setFill(Color.web("#3a6a3a", 0.6));
        drawRotatingLandmass(gc, centerX, centerY, radius, planetRotation);
        drawRotatingLandmass(gc, centerX, centerY, radius, planetRotation + 120);
        drawRotatingLandmass(gc, centerX, centerY, radius, planetRotation + 240);
        
        gc.restore();
        
        // Atmosphere glow
        RadialGradient atmosphereGlow = new RadialGradient(
            0, 0, centerX, centerY, radius * 1.15,
            false, CycleMethod.NO_CYCLE,
            new Stop(0.85, Color.TRANSPARENT),
            new Stop(0.95, Color.web(ACCENT_COLOR, 0.3)),
            new Stop(1.0, Color.web(ACCENT_GLOW, 0.1))
        );
        gc.setFill(atmosphereGlow);
        gc.fillOval(centerX - radius * 1.15, centerY - radius * 1.15, radius * 2.3, radius * 2.3);
        
        // Specular highlight
        RadialGradient highlight = new RadialGradient(
            0, 0, 
            centerX - radius * 0.4, centerY - radius * 0.4,
            radius * 0.6,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(1, 1, 1, 0.3)),
            new Stop(0.5, Color.color(1, 1, 1, 0.1)),
            new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(highlight);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
    
    /**
     * Draws a rotating landmass on the planet.
     */
    private void drawRotatingLandmass(GraphicsContext gc, double cx, double cy, double radius, double rotation) {
        double angle = Math.toRadians(rotation);
        
        // Create irregular continent shape
        gc.beginPath();
        
        int points = 12;
        java.util.Random rand = new java.util.Random((long) rotation);
        
        for (int i = 0; i <= points; i++) {
            double a = angle + (i / (double) points) * Math.PI * 0.5 - Math.PI * 0.25;
            double r = radius * (0.3 + rand.nextDouble() * 0.3);
            
            // Apply spherical distortion
            double x = cx + Math.cos(a) * r;
            double y = cy + Math.sin(a) * r * 0.5;
            
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.closePath();
        gc.fill();
    }
    private VBox createButtonSection() {
        VBox buttonBox = new VBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(250);
        
        // Main menu buttons
        Button newGameBtn = createMenuButton("NEW GAME", true);
        Button loadBtn = createMenuButton("LOAD GAME", false);
        Button settingsBtn = createMenuButton("SETTINGS", false);
        Button exitBtn = createMenuButton("EXIT", false);
        
        newGameBtn.setOnAction(e -> {
            if (onNewGame != null) onNewGame.run();
        });
        
        loadBtn.setOnAction(e -> {
            if (onLoadGame != null) onLoadGame.run();
        });
        
        settingsBtn.setOnAction(e -> {
            if (onSettings != null) onSettings.run();
        });
        
        exitBtn.setOnAction(e -> {
            if (onExit != null) onExit.run();
        });
        
        buttonBox.getChildren().addAll(newGameBtn, loadBtn, settingsBtn, exitBtn);
        
        return buttonBox;
    }
    
    private Button createMenuButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setPrefWidth(250);
        btn.setPrefHeight(50);
        
        String baseColor = isPrimary ? ACCENT_COLOR : LIGHT_BG;
        String hoverColor = isPrimary ? ACCENT_GLOW : ACCENT_COLOR;
        
        String normalStyle = String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);",
            baseColor
        );
        
        String hoverStyle = String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, %s, 15, 0.3, 0, 0);",
            hoverColor, ACCENT_COLOR
        );
        
        btn.setStyle(normalStyle);
        
        // Hover animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100), btn);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);
        
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100), btn);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            scaleIn.playFromStart();
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(normalStyle);
            scaleOut.playFromStart();
        });
        
        return btn;
    }
    
    private Button createUpdateButton() {
        Button btn = new Button("📋");
        btn.setFont(Font.font("Arial", 20));
        btn.setPrefSize(50, 50);
        btn.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-text-fill: " + ACCENT_COLOR + ";" +
            "-fx-background-radius: 25;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 25;" +
            "-fx-cursor: hand;"
        );
        
        // Tooltip
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("View Updates");
        javafx.scene.control.Tooltip.install(btn, tooltip);
        
        return btn;
    }
    
    // ==================== Update Panel ====================
    
    private void createUpdatePanel() {
        updatePanel = new VBox(15);
        updatePanel.setPadding(new Insets(20));
        updatePanel.setMinWidth(350);
        updatePanel.setMaxWidth(350);
        updatePanel.setPrefWidth(350);
        updatePanel.setMinHeight(400);
        updatePanel.setMaxHeight(500);
        updatePanel.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;"
        );
        
        // Drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setRadius(20);
        updatePanel.setEffect(shadow);
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("📰 LATEST UPDATES");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(ACCENT_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-font-size: 16;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> toggleUpdatePanel());
        
        header.getChildren().addAll(titleLabel, spacer, closeBtn);
        
        // Update content
        VBox updatesContent = new VBox(15);
        updatesContent.setPadding(new Insets(10, 0, 0, 0));
        
        // Add update entries
        updatesContent.getChildren().addAll(
            createUpdateEntry("Version 1.0.0 - Alpha", "December 2024", 
                "• Main menu with animated background\n" +
                "• World generation system\n" +
                "• Procedural terrain with climate simulation\n" +
                "• Town and city placement\n" +
                "• Road network generation\n" +
                "• Save/Load system\n" +
                "• Multiple settlement sprites\n" +
                "• Tile information tooltip\n" +
                "• Performance optimizations"),
            
            createUpdateEntry("Coming Soon", "Future Updates",
                "• Trade route system\n" +
                "• Resource management\n" +
                "• Vehicle fleet control\n" +
                "• Economy simulation\n" +
                "• Multiplayer support")
        );
        
        ScrollPane scrollPane = new ScrollPane(updatesContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;"
        );
        scrollPane.setMaxHeight(350);
        
        updatePanel.getChildren().addAll(header, scrollPane);
        
        // Position off-screen initially
        updatePanel.setTranslateX(400);
        StackPane.setAlignment(updatePanel, Pos.CENTER_RIGHT);
        StackPane.setMargin(updatePanel, new Insets(0, 30, 0, 0));
        
        // Create animations
        updateSlideIn = new TranslateTransition(Duration.millis(300), updatePanel);
        updateSlideIn.setToX(0);
        updateSlideIn.setInterpolator(Interpolator.EASE_OUT);
        
        updateSlideOut = new TranslateTransition(Duration.millis(300), updatePanel);
        updateSlideOut.setToX(400);
        updateSlideOut.setInterpolator(Interpolator.EASE_IN);
        
        getChildren().add(updatePanel);
    }
    
    private VBox createUpdateEntry(String version, String date, String content) {
        VBox entry = new VBox(5);
        entry.setPadding(new Insets(10));
        entry.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label versionLabel = new Label(version);
        versionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        versionLabel.setTextFill(Color.web(GOLD_COLOR));
        
        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("Arial", 11));
        dateLabel.setTextFill(Color.web(TEXT_COLOR, 0.6));
        
        headerRow.getChildren().addAll(versionLabel, dateLabel);
        
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Arial", 12));
        contentLabel.setTextFill(Color.web(TEXT_COLOR, 0.9));
        contentLabel.setWrapText(true);
        
        entry.getChildren().addAll(headerRow, contentLabel);
        return entry;
    }
    
    private void toggleUpdatePanel() {
        if (updatePanelVisible) {
            updateSlideOut.play();
            updatePanelVisible = false;
        } else {
            updateSlideIn.play();
            updatePanelVisible = true;
        }
    }
    
    // ==================== Animation ====================
    
    private void startAnimations() {
        // Background scrolling - smoother animation
        backgroundAnimator = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) { // 60 FPS for smoothness
                    backgroundOffset += 0.08; // Slower, smoother scroll
                    drawBackground();
                    lastUpdate = now;
                }
            }
        };
        backgroundAnimator.start();
        
        // Planet rotation
        planetAnimator = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) { // ~60 FPS
                    planetRotation += 0.3;
                    drawPlanet();
                    lastUpdate = now;
                }
            }
        };
        planetAnimator.start();
    }
    
    public void stop() {
        if (backgroundAnimator != null) backgroundAnimator.stop();
        if (planetAnimator != null) planetAnimator.stop();
    }
    
    // ==================== Noise Functions ====================
    
    private double octaveNoise(int x, int y, long seed, double scale, int octaves, double persistence) {
        double value = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            value += noiseValue(x * scale * frequency, y * scale * frequency, seed + i * 1000) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return (value / maxValue + 1) / 2;
    }
    
    private double noiseValue(double x, double y, long seed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        double fx = x - ix;
        double fy = y - iy;
        
        double u = fx * fx * (3 - 2 * fx);
        double v = fy * fy * (3 - 2 * fy);
        
        double n00 = hash(ix, iy, seed);
        double n10 = hash(ix + 1, iy, seed);
        double n01 = hash(ix, iy + 1, seed);
        double n11 = hash(ix + 1, iy + 1, seed);
        
        double nx0 = n00 + (n10 - n00) * u;
        double nx1 = n01 + (n11 - n01) * u;
        return nx0 + (nx1 - nx0) * v;
    }
    
    private double hash(int x, int y, long seed) {
        long h = seed;
        h ^= x * 374761393L;
        h ^= y * 668265263L;
        h = (h ^ (h >> 13)) * 1274126177L;
        return ((h & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL) * 2 - 1;
    }
    
    // ==================== Public API ====================
    
    public void setOnNewGame(Runnable callback) { this.onNewGame = callback; }
    public void setOnLoadGame(Runnable callback) { this.onLoadGame = callback; }
    public void setOnSettings(Runnable callback) { this.onSettings = callback; }
    public void setOnExit(Runnable callback) { this.onExit = callback; }
}
