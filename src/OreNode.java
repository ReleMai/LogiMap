import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Ore node that spawns in mountain regions near mining villages.
 * Displays as rock formation with visible ore veins.
 */
public class OreNode extends ResourceNodeBase {
    
    private OreType oreType;
    private double[] veinPositions;  // Positions of ore veins
    private double[] veinSizes;
    private int rockFormation;       // 0-2 different rock shapes
    private double shimmerPhase;
    private long animationStart;
    
    /**
     * Creates an ore node near a mining village.
     */
    public OreNode(double worldX, double worldY, OreType oreType, Town parentVillage) {
        super(worldX, worldY, oreType, parentVillage);
        this.oreType = oreType;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // Generate vein positions (4-6 veins)
        int veinCount = 4 + rand.nextInt(3);
        this.veinPositions = new double[veinCount * 2];
        this.veinSizes = new double[veinCount];
        
        for (int i = 0; i < veinCount; i++) {
            veinPositions[i * 2] = (rand.nextDouble() - 0.5) * 0.6;
            veinPositions[i * 2 + 1] = (rand.nextDouble() - 0.5) * 0.5;
            veinSizes[i] = 0.08 + rand.nextDouble() * 0.12;
        }
        
        this.rockFormation = rand.nextInt(3);
        this.shimmerPhase = rand.nextDouble() * Math.PI * 2;
        this.animationStart = System.currentTimeMillis();
        
        // Ore-specific defaults
        this.maxHarvests = 6 + rand.nextInt(5); // 6-10 harvests
        this.harvestsRemaining = maxHarvests;
        this.regrowthTimeMs = 180000 + rand.nextInt(120000); // 3-5 minutes
        this.interactionRadius = 3.5;
        this.size = 1.0 + rand.nextDouble() * 0.4;
    }
    
    @Override
    public void render(GraphicsContext gc, double screenX, double screenY, double zoom, int tileSize) {
        double baseSize = size * zoom * tileSize * 1.3;
        
        // Animation
        long elapsed = System.currentTimeMillis() - animationStart;
        double time = elapsed / 1000.0;
        double shimmer = Math.sin(time * 2 + shimmerPhase) * 0.5 + 0.5;
        
        Color rock = oreType.getRockColor();
        Color ore = oreType.getOreColor();
        Color shine = oreType.getShimmerColor();
        
        // Adjust based on harvest state
        double harvestRatio = (double) harvestsRemaining / maxHarvests;
        if (harvestRatio < 0.5) {
            ore = ore.deriveColor(0, 0.7, 0.8, 1);
        }
        
        // Draw shadow
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.3));
        gc.fillOval(screenX - baseSize * 0.4, screenY + baseSize * 0.05, baseSize * 0.8, baseSize * 0.25);
        
        // Draw rock formation
        switch (rockFormation) {
            case 0: renderRockyOutcrop(gc, screenX, screenY, baseSize, rock); break;
            case 1: renderCrystalFormation(gc, screenX, screenY, baseSize, rock); break;
            case 2: renderBoulderCluster(gc, screenX, screenY, baseSize, rock); break;
        }
        
        // Draw ore veins with shimmer
        renderOreVeins(gc, screenX, screenY, baseSize, ore, shine, shimmer);
        
        // Draw pickaxe marks if partially mined
        if (harvestRatio < 0.8) {
            renderPickaxeMarks(gc, screenX, screenY, baseSize, (int)((1 - harvestRatio) * 5));
        }
        
        // Draw interaction indicator if harvestable
        if (isHarvestable && harvestsRemaining > 0) {
            gc.setFill(ore.deriveColor(0, 1, 1, 0.3));
            gc.fillOval(screenX - baseSize * 0.5, screenY - baseSize * 0.2, baseSize, baseSize * 0.5);
        }
    }
    
    private void renderRockyOutcrop(GraphicsContext gc, double x, double y, double size, Color rock) {
        // Jagged rock formation
        gc.setFill(rock);
        double[] xPoints = {
            x - size * 0.35, x - size * 0.2, x - size * 0.05, x + size * 0.15, 
            x + size * 0.35, x + size * 0.3, x - size * 0.1, x - size * 0.3
        };
        double[] yPoints = {
            y + size * 0.1, y - size * 0.3, y - size * 0.45, y - size * 0.35,
            y + size * 0.05, y + size * 0.15, y + size * 0.15, y + size * 0.12
        };
        gc.fillPolygon(xPoints, yPoints, 8);
        
        // Highlight
        gc.setFill(rock.brighter());
        double[] xTop = {x - size * 0.18, x - size * 0.03, x + size * 0.1, x - size * 0.05};
        double[] yTop = {y - size * 0.28, y - size * 0.43, y - size * 0.32, y - size * 0.25};
        gc.fillPolygon(xTop, yTop, 4);
        
        // Dark crevice
        gc.setFill(rock.darker().darker());
        gc.fillRect(x - size * 0.05, y - size * 0.15, size * 0.03, size * 0.2);
    }
    
    private void renderCrystalFormation(GraphicsContext gc, double x, double y, double size, Color rock) {
        // Pointed crystal-like rocks
        gc.setFill(rock);
        
        // Main crystal
        double[] mainX = {x - size * 0.1, x, x + size * 0.1, x};
        double[] mainY = {y + size * 0.1, y - size * 0.5, y + size * 0.1, y + size * 0.15};
        gc.fillPolygon(mainX, mainY, 4);
        
        // Left crystal
        double[] leftX = {x - size * 0.3, x - size * 0.2, x - size * 0.15, x - size * 0.25};
        double[] leftY = {y + size * 0.1, y - size * 0.25, y + size * 0.05, y + size * 0.12};
        gc.fillPolygon(leftX, leftY, 4);
        
        // Right crystal
        double[] rightX = {x + size * 0.15, x + size * 0.25, x + size * 0.3, x + size * 0.2};
        double[] rightY = {y + size * 0.08, y - size * 0.3, y + size * 0.1, y + size * 0.12};
        gc.fillPolygon(rightX, rightY, 4);
        
        // Highlights
        gc.setFill(rock.brighter());
        gc.fillPolygon(
            new double[]{x - size * 0.05, x, x + size * 0.02, x - size * 0.02},
            new double[]{y - size * 0.1, y - size * 0.45, y - size * 0.1, y - size * 0.05}, 4
        );
    }
    
    private void renderBoulderCluster(GraphicsContext gc, double x, double y, double size, Color rock) {
        // Multiple rounded boulders
        gc.setFill(rock.darker());
        gc.fillOval(x - size * 0.3, y - size * 0.1, size * 0.4, size * 0.25);
        gc.fillOval(x + size * 0.05, y - size * 0.05, size * 0.3, size * 0.2);
        
        gc.setFill(rock);
        gc.fillOval(x - size * 0.25, y - size * 0.35, size * 0.45, size * 0.35);
        gc.fillOval(x + size * 0.0, y - size * 0.25, size * 0.35, size * 0.3);
        
        // Highlights
        gc.setFill(rock.brighter());
        gc.fillOval(x - size * 0.15, y - size * 0.32, size * 0.2, size * 0.15);
        gc.fillOval(x + size * 0.08, y - size * 0.22, size * 0.15, size * 0.12);
    }
    
    private void renderOreVeins(GraphicsContext gc, double x, double y, double size, 
                                 Color ore, Color shine, double shimmer) {
        for (int i = 0; i < veinSizes.length; i++) {
            double vx = x + veinPositions[i * 2] * size;
            double vy = y + veinPositions[i * 2 + 1] * size - size * 0.15;
            double vSize = veinSizes[i] * size;
            
            // Ore vein
            gc.setFill(ore);
            gc.fillOval(vx - vSize / 2, vy - vSize / 2, vSize, vSize * 0.8);
            
            // Shimmer highlight (animated)
            gc.setFill(shine.deriveColor(0, 1, 1, 0.3 + shimmer * 0.4));
            gc.fillOval(vx - vSize * 0.3, vy - vSize * 0.4, vSize * 0.4, vSize * 0.3);
        }
    }
    
    private void renderPickaxeMarks(GraphicsContext gc, double x, double y, double size, int count) {
        gc.setStroke(Color.web("#1a1a1a"));
        gc.setLineWidth(1.5);
        
        Random markRand = new Random((long)(worldX * 100 + worldY * 100));
        for (int i = 0; i < count; i++) {
            double mx = x + (markRand.nextDouble() - 0.5) * size * 0.5;
            double my = y + (markRand.nextDouble() - 0.5) * size * 0.3 - size * 0.1;
            double angle = markRand.nextDouble() * 0.5 - 0.25;
            
            gc.save();
            gc.translate(mx, my);
            gc.rotate(angle * 30);
            gc.strokeLine(-size * 0.05, -size * 0.03, size * 0.05, size * 0.03);
            gc.strokeLine(-size * 0.04, size * 0.02, size * 0.04, -size * 0.04);
            gc.restore();
        }
    }
    
    @Override
    public int getBaseYield() {
        Random rand = new Random();
        int base = 2 + rand.nextInt(4); // 2-5 ore
        return (int)(base * oreType.getMineModifier());
    }
    
    @Override
    public String getResourceCategory() {
        return "Ore";
    }
    
    @Override
    public String getItemId() {
        return oreType.getItemId();
    }
    
    @Override
    public String getDisplayName() {
        return oreType.getDisplayName() + " Deposit";
    }
    
    public OreType getOreType() {
        return oreType;
    }
}
