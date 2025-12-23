import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

/**
 * Represents a resource node in the world - an area containing harvestable resources.
 */
public class ResourceNode {
    
    public enum Type {
        GRAIN("Grain Field", TerrainType.PLAINS, TerrainType.MEADOW, TerrainType.GRASS),
        FOREST("Forest", TerrainType.FOREST, TerrainType.DENSE_FOREST, TerrainType.TAIGA),
        STONE("Stone Deposit", TerrainType.HILLS, TerrainType.ROCKY_HILLS, TerrainType.MOUNTAIN),
        ORE("Ore Vein", TerrainType.MOUNTAIN, TerrainType.ROCKY_HILLS);
        
        private final String displayName;
        private final TerrainType[] validBiomes;
        
        Type(String displayName, TerrainType... validBiomes) {
            this.displayName = displayName;
            this.validBiomes = validBiomes;
        }
        
        public String getDisplayName() { return displayName; }
        public TerrainType[] getValidBiomes() { return validBiomes; }
        
        public boolean canSpawnOn(TerrainType terrain) {
            for (TerrainType valid : validBiomes) {
                if (valid == terrain) return true;
            }
            return false;
        }
    }
    
    private final int centerX;
    private final int centerY;
    private final int radius;
    private final Type type;
    private final GrainType grainType; // Only for GRAIN type
    private final long seed;
    private final List<double[]> decorations; // x, y, size, variant
    
    public ResourceNode(int x, int y, int radius, Type type, GrainType grainType, long seed) {
        this.centerX = x;
        this.centerY = y;
        this.radius = radius;
        this.type = type;
        this.grainType = grainType;
        this.seed = seed;
        this.decorations = new ArrayList<>();
        generateDecorations();
    }
    
    private void generateDecorations() {
        Random rand = new Random(seed);
        
        // Determine decoration density based on type
        double density = type == Type.GRAIN ? 0.08 : 0.05;
        int count = (int) (Math.PI * radius * radius * density);
        
        for (int i = 0; i < count; i++) {
            // Random position within circular node
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(rand.nextDouble()) * radius * 0.9; // sqrt for even distribution
            double dx = Math.cos(angle) * dist;
            double dy = Math.sin(angle) * dist;
            
            double size = 4 + rand.nextDouble() * 4;
            int variant = rand.nextInt(10);
            
            decorations.add(new double[] { dx, dy, size, variant, rand.nextDouble() }); // growth factor
        }
    }
    
    /**
     * Renders the resource node decorations visible within the viewport.
     */
    public void render(GraphicsContext gc, double viewX, double viewY, double zoom, int tileSize) {
        double screenCenterX = (centerX - viewX) * zoom * tileSize;
        double screenCenterY = (centerY - viewY) * zoom * tileSize;
        double screenRadius = radius * zoom * tileSize;
        
        // Skip if too far off screen
        if (screenCenterX + screenRadius < -100 || screenCenterX - screenRadius > gc.getCanvas().getWidth() + 100 ||
            screenCenterY + screenRadius < -100 || screenCenterY - screenRadius > gc.getCanvas().getHeight() + 100) {
            return;
        }
        
        // Render decorations
        for (double[] dec : decorations) {
            double screenX = screenCenterX + dec[0] * zoom * tileSize;
            double screenY = screenCenterY + dec[1] * zoom * tileSize;
            double size = dec[2] * zoom;
            int variant = (int) dec[3];
            double growth = dec[4];
            
            if (screenX < -50 || screenX > gc.getCanvas().getWidth() + 50 ||
                screenY < -50 || screenY > gc.getCanvas().getHeight() + 50) {
                continue;
            }
            
            if (type == Type.GRAIN && grainType != null) {
                grainType.renderStalk(gc, screenX, screenY, size, 0.7 + growth * 0.3);
            }
        }
    }
    
    // Getters
    public int getCenterX() { return centerX; }
    public int getCenterY() { return centerY; }
    public int getRadius() { return radius; }
    public Type getType() { return type; }
    public GrainType getGrainType() { return grainType; }
    
    public boolean contains(int x, int y) {
        int dx = x - centerX;
        int dy = y - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }
}
