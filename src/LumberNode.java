import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Lumber node that spawns around forestry villages.
 * Displays as a group of trees that can be harvested for wood.
 */
public class LumberNode extends ResourceNodeBase {
    
    private WoodType woodType;
    private int treeCount;
    private double[] treeOffsets; // X,Y offsets for each tree
    private double[] treeSizes;   // Size multiplier for each tree
    
    /**
     * Creates a lumber node near a forestry village.
     */
    public LumberNode(double worldX, double worldY, WoodType woodType, Town parentVillage) {
        super(worldX, worldY, woodType, parentVillage);
        this.woodType = woodType;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // 3-5 trees per node
        this.treeCount = 3 + rand.nextInt(3);
        this.treeOffsets = new double[treeCount * 2];
        this.treeSizes = new double[treeCount];
        
        // Generate tree positions within node area
        for (int i = 0; i < treeCount; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = rand.nextDouble() * 1.5;
            treeOffsets[i * 2] = Math.cos(angle) * dist;
            treeOffsets[i * 2 + 1] = Math.sin(angle) * dist;
            treeSizes[i] = 0.7 + rand.nextDouble() * 0.6;
        }
        
        // Lumber-specific defaults
        this.maxHarvests = 4 + rand.nextInt(3); // 4-6 harvests
        this.harvestsRemaining = maxHarvests;
        this.regrowthTimeMs = 90000 + rand.nextInt(60000); // 1.5-2.5 minutes
        this.interactionRadius = 4.0;
    }
    
    @Override
    public void render(GraphicsContext gc, double screenX, double screenY, double zoom, int tileSize) {
        double baseSize = size * zoom * tileSize;
        
        // Render each tree
        for (int i = 0; i < treeCount; i++) {
            double tx = screenX + treeOffsets[i * 2] * zoom * tileSize;
            double ty = screenY + treeOffsets[i * 2 + 1] * zoom * tileSize;
            double treeSize = baseSize * treeSizes[i];
            
            renderTree(gc, tx, ty, treeSize, i);
        }
        
        // Draw interaction indicator if harvestable
        if (isHarvestable && harvestsRemaining > 0) {
            gc.setFill(Color.web("#FFD700").deriveColor(0, 1, 1, 0.3));
            gc.fillOval(screenX - baseSize * 0.8, screenY - baseSize * 0.3, baseSize * 1.6, baseSize * 0.8);
        }
    }
    
    /**
     * Renders a single tree with detailed sprite based on wood type.
     */
    private void renderTree(GraphicsContext gc, double x, double y, double size, int index) {
        Color bark = woodType.getBarkColor();
        Color leaves = woodType.getLeafColor();
        
        // Adjust colors based on harvest state
        if (harvestsRemaining <= maxHarvests / 3) {
            // Depleted look
            bark = bark.deriveColor(0, 0.7, 0.8, 1);
            leaves = leaves.deriveColor(0, 0.6, 0.7, 1);
        }
        
        double trunkW = size * 0.15;
        double trunkH = size * 0.5;
        double canopySize = size * 0.8;
        
        // Draw trunk
        gc.setFill(bark);
        gc.fillRect(x - trunkW / 2, y - trunkH * 0.3, trunkW, trunkH);
        
        // Draw trunk details (bark lines)
        gc.setStroke(bark.darker());
        gc.setLineWidth(1);
        for (int i = 0; i < 3; i++) {
            double ly = y - trunkH * 0.2 + i * trunkH * 0.25;
            gc.strokeLine(x - trunkW * 0.3, ly, x - trunkW * 0.1, ly + trunkH * 0.1);
            gc.strokeLine(x + trunkW * 0.1, ly + trunkH * 0.05, x + trunkW * 0.3, ly + trunkH * 0.15);
        }
        
        // Draw canopy based on wood type
        switch (woodType) {
            case OAK:
                renderOakCanopy(gc, x, y - trunkH * 0.4, canopySize, leaves);
                break;
            case BIRCH:
                renderBirchCanopy(gc, x, y - trunkH * 0.4, canopySize, leaves);
                break;
            case PINE:
                renderPineCanopy(gc, x, y - trunkH * 0.35, canopySize, leaves);
                break;
            case MAPLE:
                renderMapleCanopy(gc, x, y - trunkH * 0.4, canopySize, leaves);
                break;
        }
        
        // Draw small shadow
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.2));
        gc.fillOval(x - size * 0.25, y + trunkH * 0.15, size * 0.5, size * 0.15);
    }
    
    private void renderOakCanopy(GraphicsContext gc, double x, double y, double size, Color color) {
        // Oak: Round, full canopy with multiple layers
        gc.setFill(color.darker());
        gc.fillOval(x - size * 0.55, y - size * 0.4, size * 1.1, size * 0.9);
        gc.setFill(color);
        gc.fillOval(x - size * 0.5, y - size * 0.45, size, size * 0.8);
        gc.setFill(color.brighter());
        gc.fillOval(x - size * 0.3, y - size * 0.5, size * 0.5, size * 0.4);
    }
    
    private void renderBirchCanopy(GraphicsContext gc, double x, double y, double size, Color color) {
        // Birch: Lighter, more spread out canopy
        gc.setFill(color.darker());
        gc.fillOval(x - size * 0.4, y - size * 0.3, size * 0.35, size * 0.5);
        gc.fillOval(x + size * 0.05, y - size * 0.35, size * 0.35, size * 0.55);
        gc.setFill(color);
        gc.fillOval(x - size * 0.35, y - size * 0.45, size * 0.7, size * 0.6);
        gc.setFill(color.brighter());
        gc.fillOval(x - size * 0.2, y - size * 0.5, size * 0.35, size * 0.3);
    }
    
    private void renderPineCanopy(GraphicsContext gc, double x, double y, double size, Color color) {
        // Pine: Triangular evergreen shape
        double[] xPoints = {x, x - size * 0.5, x + size * 0.5};
        double[] yPoints = {y - size * 0.8, y + size * 0.1, y + size * 0.1};
        gc.setFill(color.darker());
        gc.fillPolygon(xPoints, yPoints, 3);
        
        double[] xPoints2 = {x, x - size * 0.4, x + size * 0.4};
        double[] yPoints2 = {y - size * 0.9, y - size * 0.1, y - size * 0.1};
        gc.setFill(color);
        gc.fillPolygon(xPoints2, yPoints2, 3);
        
        double[] xPoints3 = {x, x - size * 0.3, x + size * 0.3};
        double[] yPoints3 = {y - size, y - size * 0.3, y - size * 0.3};
        gc.setFill(color.brighter());
        gc.fillPolygon(xPoints3, yPoints3, 3);
    }
    
    private void renderMapleCanopy(GraphicsContext gc, double x, double y, double size, Color color) {
        // Maple: Star-like canopy with fall colors
        gc.setFill(color.darker());
        for (int i = 0; i < 5; i++) {
            double angle = i * Math.PI * 2 / 5 - Math.PI / 2;
            double px = x + Math.cos(angle) * size * 0.4;
            double py = y + Math.sin(angle) * size * 0.35;
            gc.fillOval(px - size * 0.25, py - size * 0.2, size * 0.5, size * 0.4);
        }
        gc.setFill(color);
        gc.fillOval(x - size * 0.4, y - size * 0.35, size * 0.8, size * 0.7);
        gc.setFill(color.brighter());
        gc.fillOval(x - size * 0.25, y - size * 0.4, size * 0.4, size * 0.35);
    }
    
    @Override
    public int getBaseYield() {
        Random rand = new Random();
        int base = 5 + rand.nextInt(6); // 5-10 base
        return (int)(base * woodType.getHarvestModifier());
    }
    
    @Override
    public String getResourceCategory() {
        return "Timber";
    }
    
    @Override
    public String getItemId() {
        return woodType.getItemId();
    }
    
    @Override
    public String getDisplayName() {
        return woodType.getDisplayName() + " Grove";
    }
    
    public WoodType getWoodType() {
        return woodType;
    }
}
