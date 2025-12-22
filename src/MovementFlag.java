import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Visual flag indicator showing where the player has clicked to move.
 * Displays an animated medieval-style banner flag at the target location.
 */
public class MovementFlag {
    
    // Position in grid coordinates
    private double gridX;
    private double gridY;
    
    // Animation state
    private double animationTime = 0;
    private boolean visible = false;
    private double fadeAlpha = 1.0;
    
    // Flag colors
    private static final Color POLE_COLOR = Color.web("#4a3020");
    private static final Color FLAG_COLOR = Color.web("#c41e3a");
    private static final Color FLAG_SECONDARY = Color.web("#8b0000");
    private static final Color GOLD_ACCENT = Color.web("#ffd700");
    
    // Fade settings
    private static final double FADE_DELAY = 2.0;  // Seconds before starting fade
    private static final double FADE_DURATION = 1.0;  // Seconds to fully fade
    private double timeSinceSet = 0;
    
    /**
     * Sets a new flag position.
     */
    public void setPosition(double gridX, double gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.visible = true;
        this.fadeAlpha = 1.0;
        this.timeSinceSet = 0;
        this.animationTime = 0;
    }
    
    /**
     * Hides the flag immediately.
     */
    public void hide() {
        this.visible = false;
    }
    
    /**
     * Updates the flag animation.
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void update(double deltaTime) {
        if (!visible) return;
        
        animationTime += deltaTime;
        timeSinceSet += deltaTime;
        
        // Start fading after delay
        if (timeSinceSet > FADE_DELAY) {
            fadeAlpha = Math.max(0, 1.0 - (timeSinceSet - FADE_DELAY) / FADE_DURATION);
            if (fadeAlpha <= 0) {
                visible = false;
            }
        }
    }
    
    /**
     * Renders the flag at the given screen position.
     */
    public void render(GraphicsContext gc, double screenX, double screenY, double tileSize) {
        if (!visible || fadeAlpha <= 0) return;
        
        // Calculate dimensions
        double poleHeight = tileSize * 1.2;
        double poleWidth = tileSize * 0.08;
        double flagWidth = tileSize * 0.5;
        double flagHeight = tileSize * 0.35;
        
        // Center of the tile
        double centerX = screenX + tileSize / 2;
        double baseY = screenY + tileSize;
        
        // Wave animation
        double wave = Math.sin(animationTime * 5) * 0.15;
        double wave2 = Math.sin(animationTime * 7 + 1) * 0.1;
        
        gc.save();
        gc.setGlobalAlpha(fadeAlpha);
        
        // Draw shadow
        gc.setFill(Color.color(0, 0, 0, 0.3 * fadeAlpha));
        gc.fillOval(centerX - tileSize * 0.2, baseY - tileSize * 0.05, 
                   tileSize * 0.4, tileSize * 0.1);
        
        // Draw pole
        gc.setFill(POLE_COLOR);
        gc.fillRect(centerX - poleWidth / 2, baseY - poleHeight, poleWidth, poleHeight);
        
        // Pole top ornament (golden ball)
        gc.setFill(GOLD_ACCENT);
        double ornamentSize = poleWidth * 2;
        gc.fillOval(centerX - ornamentSize / 2, baseY - poleHeight - ornamentSize * 0.7,
                   ornamentSize, ornamentSize);
        
        // Draw flag with wave effect
        double flagTop = baseY - poleHeight + poleHeight * 0.1;
        
        // Flag shape (wavy pennant)
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];
        
        // Top edge
        xPoints[0] = centerX + poleWidth / 2;
        yPoints[0] = flagTop;
        
        xPoints[1] = centerX + flagWidth * (0.5 + wave);
        yPoints[1] = flagTop + flagHeight * 0.1;
        
        xPoints[2] = centerX + flagWidth * (1.0 + wave2);
        yPoints[2] = flagTop + flagHeight * 0.5;
        
        // Bottom edge (returns with swallowtail)
        xPoints[3] = centerX + flagWidth * (0.7 + wave);
        yPoints[3] = flagTop + flagHeight;
        
        xPoints[4] = centerX + poleWidth / 2;
        yPoints[4] = flagTop + flagHeight * 0.7;
        
        xPoints[5] = centerX + poleWidth / 2;
        yPoints[5] = flagTop;
        
        // Draw flag shadow
        gc.setFill(FLAG_SECONDARY);
        gc.fillPolygon(xPoints, yPoints, 6);
        
        // Draw flag main color (slightly offset for 3D effect)
        for (int i = 0; i < 6; i++) {
            yPoints[i] -= flagHeight * 0.05;
        }
        gc.setFill(FLAG_COLOR);
        gc.fillPolygon(xPoints, yPoints, 6);
        
        // Draw cross/emblem on flag
        gc.setFill(GOLD_ACCENT);
        double emblemX = centerX + flagWidth * 0.4;
        double emblemY = flagTop + flagHeight * 0.35;
        double emblemSize = flagHeight * 0.25;
        
        // Simple cross emblem
        gc.fillRect(emblemX - emblemSize * 0.15, emblemY - emblemSize * 0.5,
                   emblemSize * 0.3, emblemSize);
        gc.fillRect(emblemX - emblemSize * 0.4, emblemY - emblemSize * 0.15,
                   emblemSize * 0.8, emblemSize * 0.3);
        
        // Draw target circle on ground
        gc.setStroke(Color.color(1, 0.8, 0, 0.6 * fadeAlpha));
        gc.setLineWidth(2);
        double pulseSize = tileSize * 0.4 * (1.0 + Math.sin(animationTime * 4) * 0.1);
        gc.strokeOval(centerX - pulseSize, baseY - pulseSize * 0.3,
                     pulseSize * 2, pulseSize * 0.6);
        
        gc.restore();
    }
    
    // === Getters ===
    
    public double getGridX() { return gridX; }
    public double getGridY() { return gridY; }
    public boolean isVisible() { return visible; }
}
