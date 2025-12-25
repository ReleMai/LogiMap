import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Quarry node that spawns around quarry villages.
 * Displays as a boulder with a sledgehammer leaning against it.
 */
public class QuarryNode extends ResourceNodeBase {
    
    private StoneType stoneType;
    private double[] crackPositions; // Random crack positions on boulder
    private int boulderVariant;
    
    /**
     * Creates a quarry node near a mining village.
     */
    public QuarryNode(double worldX, double worldY, StoneType stoneType, Town parentVillage) {
        super(worldX, worldY, stoneType, parentVillage);
        this.stoneType = stoneType;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // Generate crack positions
        this.crackPositions = new double[8];
        for (int i = 0; i < 8; i++) {
            crackPositions[i] = rand.nextDouble();
        }
        this.boulderVariant = rand.nextInt(3);
        
        // Quarry-specific defaults
        this.maxHarvests = 5 + rand.nextInt(4); // 5-8 harvests
        this.harvestsRemaining = maxHarvests;
        this.regrowthTimeMs = 120000 + rand.nextInt(60000); // 2-3 minutes
        this.interactionRadius = 3.5;
        this.size = 1.0 + rand.nextDouble() * 0.3;
    }
    
    @Override
    public void render(GraphicsContext gc, double screenX, double screenY, double zoom, int tileSize) {
        double baseSize = size * zoom * tileSize * 1.2;
        
        Color primary = stoneType.getPrimaryColor();
        Color secondary = stoneType.getSecondaryColor();
        
        // Adjust colors based on harvest state
        double harvestRatio = (double) harvestsRemaining / maxHarvests;
        if (harvestRatio < 0.5) {
            primary = primary.deriveColor(0, 0.8, 0.9, 1);
            secondary = secondary.deriveColor(0, 0.8, 0.9, 1);
        }
        
        // Draw shadow
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.25));
        gc.fillOval(screenX - baseSize * 0.4, screenY + baseSize * 0.1, baseSize * 0.9, baseSize * 0.3);
        
        // Draw main boulder based on variant
        switch (boulderVariant) {
            case 0: renderRoundBoulder(gc, screenX, screenY, baseSize, primary, secondary); break;
            case 1: renderAngularBoulder(gc, screenX, screenY, baseSize, primary, secondary); break;
            case 2: renderStackedBoulder(gc, screenX, screenY, baseSize, primary, secondary); break;
        }
        
        // Draw sledgehammer leaning against boulder
        renderSledgehammer(gc, screenX + baseSize * 0.35, screenY, baseSize);
        
        // Draw cracks on boulder
        renderCracks(gc, screenX, screenY, baseSize, primary);
        
        // Draw small rock chips around base
        gc.setFill(primary.darker());
        for (int i = 0; i < 4; i++) {
            double cx = screenX + (crackPositions[i] - 0.5) * baseSize * 0.8;
            double cy = screenY + baseSize * 0.15 + crackPositions[i + 4] * baseSize * 0.1;
            double chipSize = baseSize * 0.05 + crackPositions[i] * baseSize * 0.03;
            gc.fillOval(cx, cy, chipSize, chipSize * 0.8);
        }
        
        // Draw interaction indicator if harvestable
        if (isHarvestable && harvestsRemaining > 0) {
            gc.setFill(Color.web("#FFD700").deriveColor(0, 1, 1, 0.3));
            gc.fillOval(screenX - baseSize * 0.5, screenY - baseSize * 0.2, baseSize, baseSize * 0.5);
        }
    }
    
    private void renderRoundBoulder(GraphicsContext gc, double x, double y, double size, 
                                     Color primary, Color secondary) {
        // Main boulder body
        gc.setFill(primary);
        gc.fillOval(x - size * 0.4, y - size * 0.35, size * 0.8, size * 0.55);
        
        // Highlight
        gc.setFill(secondary);
        gc.fillOval(x - size * 0.3, y - size * 0.32, size * 0.4, size * 0.25);
        
        // Dark edge
        gc.setFill(primary.darker());
        gc.fillOval(x - size * 0.35, y - size * 0.1, size * 0.7, size * 0.2);
    }
    
    private void renderAngularBoulder(GraphicsContext gc, double x, double y, double size,
                                       Color primary, Color secondary) {
        // Angular boulder with flat faces
        double[] xPoints = {
            x - size * 0.35, x - size * 0.15, x + size * 0.25, 
            x + size * 0.4, x + size * 0.2, x - size * 0.25
        };
        double[] yPoints = {
            y - size * 0.1, y - size * 0.4, y - size * 0.35,
            y - size * 0.05, y + size * 0.15, y + size * 0.1
        };
        gc.setFill(primary);
        gc.fillPolygon(xPoints, yPoints, 6);
        
        // Top face highlight
        double[] xTop = {x - size * 0.15, x + size * 0.25, x + size * 0.15, x - size * 0.2};
        double[] yTop = {y - size * 0.4, y - size * 0.35, y - size * 0.25, y - size * 0.28};
        gc.setFill(secondary);
        gc.fillPolygon(xTop, yTop, 4);
        
        // Dark side
        gc.setFill(primary.darker());
        double[] xSide = {x + size * 0.25, x + size * 0.4, x + size * 0.2, x + size * 0.15};
        double[] ySide = {y - size * 0.35, y - size * 0.05, y + size * 0.15, y - size * 0.25};
        gc.fillPolygon(xSide, ySide, 4);
    }
    
    private void renderStackedBoulder(GraphicsContext gc, double x, double y, double size,
                                       Color primary, Color secondary) {
        // Two boulders stacked
        // Bottom boulder
        gc.setFill(primary.darker());
        gc.fillOval(x - size * 0.4, y - size * 0.15, size * 0.75, size * 0.35);
        
        // Top boulder
        gc.setFill(primary);
        gc.fillOval(x - size * 0.3, y - size * 0.4, size * 0.55, size * 0.35);
        
        // Highlight on top
        gc.setFill(secondary);
        gc.fillOval(x - size * 0.2, y - size * 0.38, size * 0.3, size * 0.18);
    }
    
    private void renderSledgehammer(GraphicsContext gc, double x, double y, double size) {
        // Handle (wooden)
        gc.setFill(Color.web("#8B4513"));
        gc.save();
        gc.translate(x, y);
        gc.rotate(-15);
        gc.fillRect(-size * 0.03, -size * 0.5, size * 0.06, size * 0.55);
        gc.restore();
        
        // Head (iron)
        gc.setFill(Color.web("#4A4A4A"));
        gc.save();
        gc.translate(x, y);
        gc.rotate(-15);
        gc.fillRect(-size * 0.1, -size * 0.55, size * 0.2, size * 0.12);
        gc.restore();
        
        // Head highlight
        gc.setFill(Color.web("#6A6A6A"));
        gc.save();
        gc.translate(x, y);
        gc.rotate(-15);
        gc.fillRect(-size * 0.08, -size * 0.53, size * 0.08, size * 0.08);
        gc.restore();
    }
    
    private void renderCracks(GraphicsContext gc, double x, double y, double size, Color stoneColor) {
        gc.setStroke(stoneColor.darker().darker());
        gc.setLineWidth(1);
        
        // Draw 2-3 cracks
        for (int i = 0; i < 2; i++) {
            double startX = x + (crackPositions[i] - 0.5) * size * 0.4;
            double startY = y - size * 0.2 + crackPositions[i + 2] * size * 0.2;
            double endX = startX + (crackPositions[i + 4] - 0.5) * size * 0.15;
            double endY = startY + crackPositions[i + 5] * size * 0.15;
            gc.strokeLine(startX, startY, endX, endY);
        }
    }
    
    @Override
    public int getBaseYield() {
        Random rand = new Random();
        int base = 3 + rand.nextInt(5); // 3-7 base
        return (int)(base * stoneType.getHarvestModifier());
    }
    
    @Override
    public String getResourceCategory() {
        return "Stone";
    }
    
    @Override
    public String getItemId() {
        return stoneType.getItemId();
    }
    
    @Override
    public String getDisplayName() {
        return stoneType.getDisplayName() + " Quarry";
    }
    
    public StoneType getStoneType() {
        return stoneType;
    }
}
