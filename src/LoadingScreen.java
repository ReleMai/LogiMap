import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Random;

/**
 * Loading Screen with rotating planet and blurry background.
 * 
 * Features:
 * - Animated rotating planet sphere in center
 * - "Game is loading" text
 * - Moving blurry background with small generated terrain
 */
public class LoadingScreen extends StackPane {
    
    // Style constants
    private static final String DARK_BG = "#0a0a0a";
    private static final String TEXT_COLOR = "#e0e0e0";
    private static final String ACCENT_COLOR = "#4a9eff";
    
    // Components
    private Canvas backgroundCanvas;
    private Canvas planetCanvas;
    private Text loadingText;
    private Text statusText;
    
    // Animation state
    private AnimationTimer backgroundAnimator;
    private AnimationTimer planetAnimator;
    private double backgroundOffset = 0;
    private double planetRotation = 0;
    private int[][] backgroundTerrain;
    private long animationSeed;
    
    // Loading state
    private volatile boolean isLoading = true;
    private volatile String currentStatus = "Initializing...";
    
    public LoadingScreen() {
        this(new Random().nextLong());
    }
    
    public LoadingScreen(long seed) {
        this.animationSeed = seed;
        setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Generate background terrain
        backgroundTerrain = generateBackgroundTerrain(200, 150, seed);
        
        // Create background layer (blurry moving terrain)
        backgroundCanvas = new Canvas(800, 600);
        backgroundCanvas.setEffect(new GaussianBlur(15));
        backgroundCanvas.setOpacity(0.4);
        
        // Create planet layer
        StackPane planetContainer = createPlanetDisplay();
        
        // Create text layer
        VBox textContainer = createTextDisplay();
        
        // Layer everything
        getChildren().addAll(backgroundCanvas, planetContainer, textContainer);
        
        // Start animations
        startBackgroundAnimation();
        startPlanetAnimation();
        
        // Bind canvas size to parent
        widthProperty().addListener((obs, old, newVal) -> {
            backgroundCanvas.setWidth(newVal.doubleValue());
            drawBackground();
        });
        heightProperty().addListener((obs, old, newVal) -> {
            backgroundCanvas.setHeight(newVal.doubleValue());
            drawBackground();
        });
    }
    
    /**
     * Creates the planet display with rotating sphere.
     */
    private StackPane createPlanetDisplay() {
        planetCanvas = new Canvas(200, 200);
        
        StackPane container = new StackPane(planetCanvas);
        container.setMaxSize(200, 200);
        return container;
    }
    
    /**
     * Creates the text display.
     */
    private VBox createTextDisplay() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 0, 100, 0));
        
        // Spacer to push text below planet
        Region spacer = new Region();
        spacer.setMinHeight(150);
        
        loadingText = new Text("GAME IS LOADING");
        loadingText.setFont(Font.font("System", FontWeight.BOLD, 24));
        loadingText.setFill(Color.web(ACCENT_COLOR));
        
        statusText = new Text(currentStatus);
        statusText.setFont(Font.font("System", FontWeight.NORMAL, 14));
        statusText.setFill(Color.web(TEXT_COLOR, 0.7));
        
        // Loading dots animation
        Text dots = new Text("");
        dots.setFont(Font.font("System", FontWeight.BOLD, 24));
        dots.setFill(Color.web(ACCENT_COLOR));
        
        HBox loadingRow = new HBox(5, loadingText, dots);
        loadingRow.setAlignment(Pos.CENTER);
        
        container.getChildren().addAll(spacer, loadingRow, statusText);
        
        // Animate loading dots
        AnimationTimer dotsAnimator = new AnimationTimer() {
            private long lastUpdate = 0;
            private int dotCount = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate > 400_000_000) { // 400ms
                    dotCount = (dotCount + 1) % 4;
                    dots.setText(".".repeat(dotCount));
                    lastUpdate = now;
                }
            }
        };
        dotsAnimator.start();
        
        return container;
    }
    
    /**
     * Generates terrain for the moving background.
     */
    private int[][] generateBackgroundTerrain(int width, int height, long seed) {
        int[][] terrain = new int[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double nx = x / (double) width;
                double ny = y / (double) height;
                
                double value = 0;
                value += noise(nx * 4, ny * 4, seed) * 0.5;
                value += noise(nx * 8, ny * 8, seed + 1) * 0.25;
                value += noise(nx * 16, ny * 16, seed + 2) * 0.125;
                
                value = (value + 1) / 2;
                
                if (value < 0.4) terrain[x][y] = 0;
                else if (value < 0.5) terrain[x][y] = 1;
                else if (value < 0.65) terrain[x][y] = 2;
                else if (value < 0.8) terrain[x][y] = 3;
                else terrain[x][y] = 4;
            }
        }
        
        return terrain;
    }
    
    /**
     * Simple value noise function.
     */
    private double noise(double x, double y, long seed) {
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
    
    /**
     * Starts the background animation.
     */
    private void startBackgroundAnimation() {
        backgroundAnimator = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate > 33_000_000) { // ~30 FPS
                    backgroundOffset += 0.3;
                    drawBackground();
                    lastUpdate = now;
                }
            }
        };
        backgroundAnimator.start();
    }
    
    /**
     * Draws the moving background terrain.
     */
    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();
        
        // Clear
        gc.setFill(Color.web(DARK_BG));
        gc.fillRect(0, 0, width, height);
        
        // Terrain colors (darker for background)
        Color[] colors = {
            Color.web("#0a1520"),  // Deep water
            Color.web("#152535"),  // Shallow water
            Color.web("#1a2a1a"),  // Land
            Color.web("#252520"),  // Hills
            Color.web("#303030")   // Mountains
        };
        
        int terrainWidth = backgroundTerrain.length;
        int terrainHeight = backgroundTerrain[0].length;
        double scaleX = width / terrainWidth * 2;
        double scaleY = height / terrainHeight * 2;
        
        int offsetX = (int) (backgroundOffset % terrainWidth);
        
        for (int x = 0; x < terrainWidth; x++) {
            for (int y = 0; y < terrainHeight; y++) {
                int tx = (x + offsetX) % terrainWidth;
                int t = backgroundTerrain[tx][y];
                gc.setFill(colors[Math.min(t, colors.length - 1)]);
                gc.fillRect(x * scaleX - scaleX, y * scaleY, scaleX + 1, scaleY + 1);
            }
        }
    }
    
    /**
     * Starts the planet rotation animation.
     */
    private void startPlanetAnimation() {
        planetAnimator = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (now - lastUpdate > 16_000_000) { // ~60 FPS
                    planetRotation += 0.5;
                    drawPlanet();
                    lastUpdate = now;
                }
            }
        };
        planetAnimator.start();
    }
    
    /**
     * Draws the rotating planet sphere.
     */
    private void drawPlanet() {
        GraphicsContext gc = planetCanvas.getGraphicsContext2D();
        double size = planetCanvas.getWidth();
        double centerX = size / 2;
        double centerY = size / 2;
        double radius = size * 0.4;
        
        // Clear
        gc.clearRect(0, 0, size, size);
        
        // Draw planet sphere with gradient
        RadialGradient gradient = new RadialGradient(
            0, 0, 
            centerX - radius * 0.3, centerY - radius * 0.3,
            radius * 1.2,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#4a7a9f")),
            new Stop(0.3, Color.web("#2a5a7f")),
            new Stop(0.7, Color.web("#1a3a5f")),
            new Stop(1, Color.web("#0a1a2f"))
        );
        
        gc.setFill(gradient);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw rotating terrain features on planet
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
        
        // Add atmosphere glow
        RadialGradient atmosphere = new RadialGradient(
            0, 0,
            centerX, centerY,
            radius * 1.1,
            false, CycleMethod.NO_CYCLE,
            new Stop(0.85, Color.TRANSPARENT),
            new Stop(0.95, Color.web(ACCENT_COLOR, 0.2)),
            new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(atmosphere);
        gc.fillOval(centerX - radius * 1.1, centerY - radius * 1.1, radius * 2.2, radius * 2.2);
        
        // Add specular highlight
        RadialGradient highlight = new RadialGradient(
            0, 0,
            centerX - radius * 0.4, centerY - radius * 0.4,
            radius * 0.6,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#ffffff", 0.3)),
            new Stop(0.5, Color.web("#ffffff", 0.1)),
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
        Random rand = new Random((long) rotation);
        
        for (int i = 0; i <= points; i++) {
            double a = angle + (i / (double) points) * Math.PI * 0.5 - Math.PI * 0.25;
            double r = radius * (0.3 + rand.nextDouble() * 0.3);
            
            // Apply spherical distortion
            double x = cx + Math.cos(a) * r;
            double y = cy + Math.sin(a) * r * 0.5; // Flatten vertically for perspective
            
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        
        gc.closePath();
        gc.fill();
    }
    
    /**
     * Updates the loading status text.
     */
    public void setStatus(String status) {
        this.currentStatus = status;
        Platform.runLater(() -> statusText.setText(status));
    }
    
    /**
     * Stops all animations and prepares for removal.
     */
    public void stop() {
        isLoading = false;
        if (backgroundAnimator != null) backgroundAnimator.stop();
        if (planetAnimator != null) planetAnimator.stop();
    }
}
