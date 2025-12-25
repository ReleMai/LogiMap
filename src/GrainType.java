import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Types of grain resources that can be farmed.
 */
public enum GrainType {
    WHEAT("Wheat", Color.web("#d4a030"), Color.web("#c49020"), 0.35),
    OAT("Oat", Color.web("#c8b860"), Color.web("#b0a048"), 0.25),
    BARLEY("Barley", Color.web("#d0b050"), Color.web("#b89838"), 0.25),
    RYE("Rye", Color.web("#a08848"), Color.web("#887030"), 0.15);
    
    private final String displayName;
    private final Color primaryColor;
    private final Color secondaryColor;
    private final double spawnWeight;
    
    GrainType(String displayName, Color primaryColor, Color secondaryColor, double spawnWeight) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.spawnWeight = spawnWeight;
    }
    
    public String getDisplayName() { return displayName; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public double getSpawnWeight() { return spawnWeight; }
    
    /**
     * Renders a single grain stalk at the given position.
     */
    public void renderStalk(GraphicsContext gc, double x, double y, double size, double growth) {
        double stalkHeight = size * (0.6 + growth * 0.4);
        double headSize = size * 0.25 * growth;
        
        // Stalk
        gc.setStroke(secondaryColor.darker());
        gc.setLineWidth(Math.max(1, size * 0.08));
        double sway = Math.sin(x * 0.1 + y * 0.1) * size * 0.1;
        gc.strokeLine(x, y, x + sway, y - stalkHeight);
        
        // Grain head based on type
        gc.setFill(primaryColor);
        double headY = y - stalkHeight;
        double headX = x + sway;
        
        switch (this) {
            case WHEAT:
                // Wheat has a wider, fuller head
                for (int i = 0; i < 5; i++) {
                    double angle = -30 + i * 15;
                    double rad = Math.toRadians(angle);
                    gc.fillOval(headX + Math.sin(rad) * headSize * 0.3 - headSize * 0.15,
                               headY - headSize + i * headSize * 0.3 - headSize * 0.15,
                               headSize * 0.3, headSize * 0.4);
                }
                break;
            case OAT:
                // Oat has drooping seeds
                gc.setStroke(primaryColor);
                gc.setLineWidth(1);
                for (int i = 0; i < 4; i++) {
                    double seedX = headX + (i - 1.5) * headSize * 0.4;
                    double seedY = headY + i * headSize * 0.15;
                    gc.strokeLine(headX, headY - headSize * 0.5, seedX, seedY);
                    gc.fillOval(seedX - headSize * 0.1, seedY, headSize * 0.2, headSize * 0.3);
                }
                break;
            case BARLEY:
                // Barley has long awns
                gc.fillRect(headX - headSize * 0.1, headY - headSize, headSize * 0.2, headSize);
                gc.setStroke(primaryColor.brighter());
                gc.setLineWidth(0.5);
                for (int i = 0; i < 6; i++) {
                    double awnY = headY - headSize + i * headSize * 0.2;
                    gc.strokeLine(headX, awnY, headX + headSize * 0.4, awnY - headSize * 0.2);
                    gc.strokeLine(headX, awnY, headX - headSize * 0.4, awnY - headSize * 0.2);
                }
                break;
            case RYE:
                // Rye has a slender, dense head
                gc.fillRect(headX - headSize * 0.08, headY - headSize * 1.2, headSize * 0.16, headSize * 1.2);
                gc.setStroke(primaryColor.darker());
                gc.setLineWidth(0.5);
                for (int i = 0; i < 8; i++) {
                    double awnY = headY - headSize * 1.2 + i * headSize * 0.15;
                    gc.strokeLine(headX, awnY, headX + headSize * 0.25, awnY - headSize * 0.1);
                }
                break;
        }
    }
    
    /**
     * Selects a random grain type based on spawn weights.
     */
    public static GrainType randomWeighted(java.util.Random random) {
        double total = 0;
        for (GrainType type : values()) total += type.spawnWeight;
        
        double roll = random.nextDouble() * total;
        double cumulative = 0;
        for (GrainType type : values()) {
            cumulative += type.spawnWeight;
            if (roll < cumulative) return type;
        }
        return WHEAT;
    }
}
