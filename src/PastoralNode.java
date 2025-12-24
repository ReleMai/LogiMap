import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Pastoral node that spawns around pastoral villages.
 * Displays as a group of livestock (sheep, cows, pigs, chickens).
 */
public class PastoralNode extends ResourceNodeBase {
    
    private LivestockType livestockType;
    private int animalCount;
    private double[] animalOffsets;  // X,Y offsets for each animal
    private double[] animalSizes;    // Size multiplier for each animal
    private double[] animalAngles;   // Facing direction
    
    /**
     * Creates a pastoral node near a pastoral village.
     */
    public PastoralNode(double worldX, double worldY, LivestockType livestockType, Town parentVillage) {
        super(worldX, worldY, livestockType, parentVillage);
        this.livestockType = livestockType;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // 4-8 animals per node
        this.animalCount = 4 + rand.nextInt(5);
        this.animalOffsets = new double[animalCount * 2];
        this.animalSizes = new double[animalCount];
        this.animalAngles = new double[animalCount];
        
        // Generate animal positions within node area (grazing spread)
        for (int i = 0; i < animalCount; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = 0.5 + rand.nextDouble() * 2.0;
            animalOffsets[i * 2] = Math.cos(angle) * dist;
            animalOffsets[i * 2 + 1] = Math.sin(angle) * dist;
            animalSizes[i] = 0.7 + rand.nextDouble() * 0.5;
            animalAngles[i] = rand.nextDouble() * Math.PI * 2;
        }
        
        // Pastoral-specific defaults
        this.maxHarvests = 3 + rand.nextInt(3); // 3-5 harvests per herd
        this.harvestsRemaining = maxHarvests;
        this.regrowthTimeMs = 120000 + rand.nextInt(60000); // 2-3 minutes for herd replenishment
        this.interactionRadius = 4.0;
        this.size = 4; // Larger area for grazing animals
    }
    
    @Override
    public void render(GraphicsContext gc, double screenX, double screenY, double zoom, int tileSize) {
        double baseSize = size * zoom * tileSize * 0.12;
        
        // Render pasture ground
        gc.setFill(Color.web("#5a8a3a").deriveColor(0, 1, 1, 0.3));
        gc.fillOval(screenX - baseSize * 6, screenY - baseSize * 3, baseSize * 12, baseSize * 8);
        
        // Render each animal
        for (int i = 0; i < animalCount; i++) {
            double ax = screenX + animalOffsets[i * 2] * zoom * tileSize;
            double ay = screenY + animalOffsets[i * 2 + 1] * zoom * tileSize;
            double animalSize = baseSize * animalSizes[i];
            
            renderAnimal(gc, ax, ay, animalSize, i);
        }
        
        // Draw interaction indicator if harvestable
        if (isHarvestable && harvestsRemaining > 0) {
            gc.setFill(Color.web("#FFD700").deriveColor(0, 1, 1, 0.3));
            gc.fillOval(screenX - baseSize * 5, screenY - baseSize * 2.5, baseSize * 10, baseSize * 6);
        }
    }
    
    /**
     * Renders a single animal based on livestock type.
     */
    private void renderAnimal(GraphicsContext gc, double x, double y, double size, int index) {
        Color bodyColor = livestockType.getColorFX();
        
        // Adjust colors based on harvest state
        if (harvestsRemaining <= maxHarvests / 3) {
            bodyColor = bodyColor.deriveColor(0, 0.7, 0.8, 1);
        }
        
        switch (livestockType) {
            case SHEEP -> renderSheep(gc, x, y, size, bodyColor);
            case COW -> renderCow(gc, x, y, size, bodyColor);
            case PIG -> renderPig(gc, x, y, size, bodyColor);
            case CHICKEN -> renderChicken(gc, x, y, size, bodyColor);
        }
    }
    
    /**
     * Renders a sheep - fluffy woolly body.
     */
    private void renderSheep(GraphicsContext gc, double x, double y, double size, Color bodyColor) {
        double bodyW = size * 2.0;
        double bodyH = size * 1.3;
        
        // Fluffy wool body
        gc.setFill(bodyColor);
        gc.fillOval(x - bodyW/2, y - bodyH/2, bodyW, bodyH);
        
        // Wool texture (small bumps)
        gc.setFill(bodyColor.brighter());
        for (int i = 0; i < 5; i++) {
            double bx = x - bodyW/3 + (i % 3) * bodyW/4;
            double by = y - bodyH/4 + (i / 3) * bodyH/3;
            gc.fillOval(bx, by, size * 0.3, size * 0.3);
        }
        
        // Head (darker)
        gc.setFill(Color.web("#2a2a2a"));
        gc.fillOval(x + bodyW/2 - size*0.4, y - size*0.25, size * 0.6, size * 0.5);
        
        // Ears
        gc.fillOval(x + bodyW/2, y - size*0.4, size * 0.25, size * 0.2);
        gc.fillOval(x + bodyW/2, y + size*0.1, size * 0.25, size * 0.2);
        
        // Legs
        gc.setFill(Color.web("#3a3a3a"));
        gc.fillRect(x - bodyW/3, y + bodyH/3, size * 0.15, size * 0.4);
        gc.fillRect(x + bodyW/4, y + bodyH/3, size * 0.15, size * 0.4);
    }
    
    /**
     * Renders a cow - large body with spots.
     */
    private void renderCow(GraphicsContext gc, double x, double y, double size, Color bodyColor) {
        double bodyW = size * 2.5;
        double bodyH = size * 1.5;
        
        // Body
        gc.setFill(bodyColor);
        gc.fillOval(x - bodyW/2, y - bodyH/2, bodyW, bodyH);
        
        // Spots (black patches)
        gc.setFill(Color.web("#2a2010"));
        gc.fillOval(x - bodyW/4, y - bodyH/4, size * 0.5, size * 0.4);
        gc.fillOval(x + bodyW/6, y, size * 0.4, size * 0.35);
        
        // Head
        gc.setFill(bodyColor.darker());
        gc.fillOval(x + bodyW/2 - size*0.3, y - size*0.3, size * 0.7, size * 0.6);
        
        // Horns
        gc.setFill(Color.web("#d4c090"));
        gc.fillRect(x + bodyW/2 + size*0.1, y - size*0.5, size * 0.1, size * 0.25);
        gc.fillRect(x + bodyW/2 + size*0.1, y + size*0.2, size * 0.1, size * 0.25);
        
        // Legs
        gc.setFill(Color.web("#3a3010"));
        gc.fillRect(x - bodyW/3, y + bodyH/3, size * 0.18, size * 0.5);
        gc.fillRect(x + bodyW/4, y + bodyH/3, size * 0.18, size * 0.5);
    }
    
    /**
     * Renders a pig - round pink body.
     */
    private void renderPig(GraphicsContext gc, double x, double y, double size, Color bodyColor) {
        double bodyW = size * 1.8;
        double bodyH = size * 1.4;
        
        // Round body
        gc.setFill(bodyColor);
        gc.fillOval(x - bodyW/2, y - bodyH/2, bodyW, bodyH);
        
        // Head/snout
        gc.setFill(bodyColor.darker());
        gc.fillOval(x + bodyW/2 - size*0.5, y - size*0.25, size * 0.65, size * 0.5);
        
        // Snout
        gc.setFill(Color.web("#ff9999"));
        gc.fillOval(x + bodyW/2 - size*0.1, y - size*0.1, size * 0.3, size * 0.2);
        
        // Ears
        gc.setFill(bodyColor);
        gc.fillOval(x + bodyW/3, y - size*0.5, size * 0.3, size * 0.25);
        gc.fillOval(x + bodyW/3, y + size*0.2, size * 0.3, size * 0.25);
        
        // Curly tail
        gc.setStroke(bodyColor.darker());
        gc.setLineWidth(2);
        gc.strokeArc(x - bodyW/2 - size*0.2, y - size*0.15, size*0.3, size*0.3, 0, 270, javafx.scene.shape.ArcType.OPEN);
        
        // Short legs
        gc.setFill(bodyColor.darker());
        gc.fillRect(x - bodyW/3, y + bodyH/3, size * 0.15, size * 0.3);
        gc.fillRect(x + bodyW/4, y + bodyH/3, size * 0.15, size * 0.3);
    }
    
    /**
     * Renders a chicken - small with feathers.
     */
    private void renderChicken(GraphicsContext gc, double x, double y, double size, Color bodyColor) {
        double bodyW = size * 1.0;
        double bodyH = size * 0.8;
        
        // Body
        gc.setFill(bodyColor);
        gc.fillOval(x - bodyW/2, y - bodyH/2, bodyW, bodyH);
        
        // Wing
        gc.setFill(bodyColor.darker());
        gc.fillOval(x - bodyW/4, y - bodyH/4, bodyW * 0.5, bodyH * 0.6);
        
        // Head
        gc.setFill(bodyColor);
        gc.fillOval(x + bodyW/2 - size*0.2, y - size*0.3, size * 0.4, size * 0.35);
        
        // Beak
        gc.setFill(Color.web("#ffa500"));
        gc.fillPolygon(
            new double[]{x + bodyW/2 + size*0.1, x + bodyW/2 + size*0.25, x + bodyW/2 + size*0.1},
            new double[]{y - size*0.15, y - size*0.1, y - size*0.05},
            3
        );
        
        // Comb (red on top)
        gc.setFill(Color.web("#cc3333"));
        gc.fillRect(x + bodyW/2 - size*0.05, y - size*0.45, size * 0.15, size * 0.15);
        
        // Wattle
        gc.fillOval(x + bodyW/2, y - size*0.05, size * 0.1, size * 0.15);
        
        // Legs
        gc.setFill(Color.web("#ffa500"));
        gc.fillRect(x - size*0.1, y + bodyH/3, size * 0.05, size * 0.25);
        gc.fillRect(x + size*0.05, y + bodyH/3, size * 0.05, size * 0.25);
    }
    
    @Override
    public String getResourceCategory() {
        return "Meat";
    }
    
    @Override
    public String getDisplayName() {
        return livestockType.getDisplayName() + " Herd";
    }
    
    @Override
    public String getItemId() {
        return livestockType.getItemId();
    }
    
    @Override
    public int getBaseYield() {
        return livestockType.calculateYield();
    }
    
    @Override
    public String getStatusText() {
        if (!isHarvestable) {
            long remaining = (regrowthTimeMs - (System.currentTimeMillis() - lastHarvestTime)) / 1000;
            return "Herd recovering (" + remaining + "s)";
        }
        if (harvestsRemaining <= 0) {
            return "Herd depleted";
        }
        return "Ready (" + harvestsRemaining + "/" + maxHarvests + " animals)";
    }
    
    /**
     * Gets the livestock type.
     */
    public LivestockType getLivestockType() {
        return livestockType;
    }
    
    @Override
    public Object getResourceType() {
        return livestockType;
    }
}
