import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Represents a farmland node that spawns around agricultural villages.
 * 
 * Farmland nodes display as "farmlines" - rows of crops that can be harvested.
 * The appearance changes based on the crop type and growth stage.
 */
public class FarmlandNode {
    
    // Position in world coordinates
    private double worldX;
    private double worldY;
    
    // Farmland properties
    private GrainType grainType;
    private GrowthStage growthStage;
    private int plotWidth;  // Number of crop rows
    private int plotHeight;
    private double rotation; // Slight rotation for variety
    
    // Interaction state
    private boolean isHarvestable;
    private int harvestsRemaining;
    private int maxHarvests;
    private long lastHarvestTime;
    private long regrowthTimeMs; // Time to regrow after harvest
    
    // Parent village reference
    private Town parentVillage;
    
    // Visual variety
    private int variant; // Different row patterns
    private double[] rowOffsets; // Slight offset per row for natural look
    
    /**
     * Growth stages for crops.
     */
    public enum GrowthStage {
        SEEDS("Seeds", 0.0, false, "#8B7355"),
        SPROUTS("Sprouts", 0.25, false, "#6B8E23"),
        GROWING("Growing", 0.5, false, "#556B2F"),
        MATURE("Mature", 0.75, true, "#8B8B00"),
        RIPE("Ripe", 1.0, true, "#DAA520"),
        HARVESTED("Harvested", 0.0, false, "#8B7355");
        
        private final String displayName;
        private final double visualScale;
        private final boolean canHarvest;
        private final String baseColor;
        
        GrowthStage(String displayName, double visualScale, boolean canHarvest, String baseColor) {
            this.displayName = displayName;
            this.visualScale = visualScale;
            this.canHarvest = canHarvest;
            this.baseColor = baseColor;
        }
        
        public String getDisplayName() { return displayName; }
        public double getVisualScale() { return visualScale; }
        public boolean canHarvest() { return canHarvest; }
        public String getBaseColor() { return baseColor; }
    }
    
    /**
     * Creates a farmland node near a village.
     */
    public FarmlandNode(double worldX, double worldY, GrainType grainType, Town parentVillage) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.grainType = grainType;
        this.parentVillage = parentVillage;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // Plot size varies
        this.plotWidth = 3 + rand.nextInt(4); // 3-6 rows
        this.plotHeight = 4 + rand.nextInt(4); // 4-7 stalks per row
        
        // Slight rotation for variety
        this.rotation = (rand.nextDouble() - 0.5) * 0.2; // -0.1 to 0.1 radians
        
        // Growth stage - start at random mature stages
        this.growthStage = rand.nextDouble() < 0.7 ? GrowthStage.RIPE : GrowthStage.MATURE;
        
        // Harvest properties
        this.maxHarvests = 3 + rand.nextInt(3); // 3-5 harvests before depleted
        this.harvestsRemaining = maxHarvests;
        this.isHarvestable = growthStage.canHarvest();
        this.regrowthTimeMs = 60000 + rand.nextInt(60000); // 1-2 minutes real time
        
        // Visual variety
        this.variant = rand.nextInt(4);
        this.rowOffsets = new double[plotWidth];
        for (int i = 0; i < plotWidth; i++) {
            rowOffsets[i] = (rand.nextDouble() - 0.5) * 2;
        }
    }
    
    /**
     * Renders the farmland as rows of crops (farmlines).
     */
    public void render(GraphicsContext gc, double screenX, double screenY, double scale) {
        gc.save();
        
        // Transform for rotation
        gc.translate(screenX, screenY);
        gc.rotate(Math.toDegrees(rotation));
        
        double rowSpacing = 8 * scale;
        double stalkSpacing = 6 * scale;
        double plotStartX = -plotWidth * rowSpacing / 2;
        double plotStartY = -plotHeight * stalkSpacing / 2;
        
        // Draw soil/plowed lines first
        renderSoil(gc, plotStartX, plotStartY, rowSpacing, stalkSpacing, scale);
        
        // Draw crops if not harvested
        if (growthStage != GrowthStage.HARVESTED && growthStage != GrowthStage.SEEDS) {
            renderCrops(gc, plotStartX, plotStartY, rowSpacing, stalkSpacing, scale);
        }
        
        // Draw harvestable indicator
        if (isHarvestable && growthStage.canHarvest()) {
            renderHarvestIndicator(gc, scale);
        }
        
        gc.restore();
    }
    
    /**
     * Renders the plowed soil lines.
     */
    private void renderSoil(GraphicsContext gc, double startX, double startY, 
                            double rowSpacing, double stalkSpacing, double scale) {
        // Soil color varies by growth stage
        Color soilDark;
        Color soilLight;
        
        switch (growthStage) {
            case HARVESTED:
                // Depleted, dry soil
                soilDark = Color.web("#4a3520");
                soilLight = Color.web("#5a4530");
                break;
            case SEEDS:
                // Freshly tilled
                soilDark = Color.web("#5a4530");
                soilLight = Color.web("#7a6550");
                break;
            default:
                // Normal fertile soil
                soilDark = Color.web("#5a4530");
                soilLight = Color.web("#7a6550");
                break;
        }
        
        double plotWidth_px = this.plotWidth * rowSpacing;
        double plotHeight_px = this.plotHeight * stalkSpacing;
        
        // Background soil
        gc.setFill(soilDark);
        gc.fillRect(startX - 2*scale, startY - 2*scale, 
                    plotWidth_px + 4*scale, plotHeight_px + 4*scale);
        
        // Plowed row lines
        gc.setStroke(soilLight);
        gc.setLineWidth(1.5 * scale);
        
        for (int row = 0; row < this.plotWidth; row++) {
            double x = startX + row * rowSpacing + rowSpacing/2;
            gc.strokeLine(x, startY, x, startY + plotHeight_px);
        }
        
        // Furrow shadows
        gc.setStroke(Color.web("#3a2520").deriveColor(0, 1, 1, 0.5));
        gc.setLineWidth(0.5 * scale);
        for (int row = 0; row < this.plotWidth; row++) {
            double x = startX + row * rowSpacing + rowSpacing/2 + scale;
            gc.strokeLine(x, startY, x, startY + plotHeight_px);
        }
        
        // For HARVESTED state, show some stubble
        if (growthStage == GrowthStage.HARVESTED) {
            gc.setStroke(Color.web("#706040"));
            gc.setLineWidth(0.5 * scale);
            for (int row = 0; row < this.plotWidth; row++) {
                for (int col = 0; col < this.plotHeight; col++) {
                    if ((row + col) % 3 == 0) {
                        double x = startX + row * rowSpacing + rowSpacing/2;
                        double y = startY + col * stalkSpacing + stalkSpacing/2;
                        gc.strokeLine(x, y, x, y - 2*scale);
                    }
                }
            }
        }
        
        // For SEEDS state, show seed dots
        if (growthStage == GrowthStage.SEEDS) {
            gc.setFill(Color.web("#8B7355"));
            for (int row = 0; row < this.plotWidth; row++) {
                for (int col = 0; col < this.plotHeight; col++) {
                    double x = startX + row * rowSpacing + rowSpacing/2;
                    double y = startY + col * stalkSpacing + stalkSpacing/2;
                    gc.fillOval(x - scale, y - scale, 2*scale, 2*scale);
                }
            }
        }
    }
    
    /**
     * Renders the crop stalks in rows.
     */
    private void renderCrops(GraphicsContext gc, double startX, double startY,
                             double rowSpacing, double stalkSpacing, double scale) {
        double growthScale = growthStage.getVisualScale();
        
        for (int row = 0; row < plotWidth; row++) {
            double rowX = startX + row * rowSpacing + rowSpacing/2 + rowOffsets[row] * scale;
            
            for (int stalk = 0; stalk < plotHeight; stalk++) {
                double stalkY = startY + stalk * stalkSpacing + stalkSpacing/2;
                
                // Slight variation per stalk
                double stalkVariation = ((row + stalk) % 3 - 1) * scale;
                
                // Render individual crop stalk
                renderStalk(gc, rowX + stalkVariation, stalkY, scale * growthScale);
            }
        }
    }
    
    /**
     * Renders a single crop stalk based on grain type.
     */
    private void renderStalk(GraphicsContext gc, double x, double y, double scale) {
        if (scale < 0.1) return; // Too small to render
        
        double height = 10 * scale;
        
        // Stalk color varies by growth stage
        Color stalkColor;
        switch (growthStage) {
            case SPROUTS:
                stalkColor = Color.web("#90EE90"); // Light green for sprouts
                break;
            case GROWING:
                stalkColor = Color.web("#6B8E23"); // Olive green for growing
                break;
            case MATURE:
                stalkColor = Color.web("#8B8B00"); // Darker olive for mature
                break;
            case RIPE:
                stalkColor = Color.web("#DAA520"); // Golden for ripe
                break;
            default:
                stalkColor = Color.web("#6B8E23");
                break;
        }
        
        // Stalk (stem)
        gc.setStroke(stalkColor);
        gc.setLineWidth(Math.max(1, scale));
        gc.strokeLine(x, y, x, y - height);
        
        // Grain head based on type - use growth scale from growth stage
        double growth = growthStage.getVisualScale();
        grainType.renderStalk(gc, x, y - height, scale * 0.8, growth);
    }
    
    /**
     * Renders a sparkle indicator for harvestable crops.
     */
    private void renderHarvestIndicator(GraphicsContext gc, double scale) {
        if (scale < 0.5) return; // Don't show at low zoom
        
        // Pulsing glow effect
        long time = System.currentTimeMillis();
        double pulse = 0.5 + 0.5 * Math.sin(time / 300.0);
        
        gc.setFill(Color.GOLD.deriveColor(0, 1, 1, 0.3 * pulse));
        double size = 30 * scale;
        gc.fillOval(-size/2, -size/2, size, size);
        
        // Small sparkle
        gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.7 * pulse));
        gc.fillOval(-2*scale, -20*scale, 4*scale, 4*scale);
    }
    
    /**
     * Attempts to harvest this farmland.
     * Returns the amount harvested, or 0 if cannot harvest.
     */
    public int harvest(GameTime gameTime, PlayerEnergy energy) {
        // Check if can harvest
        if (!isHarvestable || !growthStage.canHarvest()) {
            return 0;
        }
        
        // Check time of day
        if (!gameTime.canFarm()) {
            return 0;
        }
        
        // Check and consume energy
        if (!energy.consumeEnergy(PlayerEnergy.FARMING_COST)) {
            return 0;
        }
        
        // Perform harvest
        int yield = calculateYield();
        harvestsRemaining--;
        lastHarvestTime = System.currentTimeMillis();
        
        // Update state
        if (harvestsRemaining <= 0) {
            growthStage = GrowthStage.HARVESTED;
            isHarvestable = false;
        } else {
            growthStage = GrowthStage.GROWING;
            isHarvestable = false;
        }
        
        return yield;
    }
    
    /**
     * Directly harvests without checking energy/time (for timed actions).
     * Call only after validation is already done by the interaction system.
     */
    public int harvestDirect() {
        if (!isHarvestable || !growthStage.canHarvest()) {
            return 0;
        }
        
        int yield = calculateYield();
        harvestsRemaining--;
        lastHarvestTime = System.currentTimeMillis();
        
        if (harvestsRemaining <= 0) {
            growthStage = GrowthStage.HARVESTED;
            isHarvestable = false;
        } else {
            growthStage = GrowthStage.GROWING;
            isHarvestable = false;
        }
        
        return yield;
    }
    
    /**
     * Calculates harvest yield based on growth stage and plot size.
     */
    private int calculateYield() {
        int baseYield = plotWidth * plotHeight / 3;
        
        // Ripe crops give more
        if (growthStage == GrowthStage.RIPE) {
            baseYield = (int)(baseYield * 1.5);
        }
        
        // Some randomness
        Random rand = new Random();
        return Math.max(1, baseYield + rand.nextInt(3) - 1);
    }
    
    /**
     * Updates growth state over time.
     */
    public void update() {
        // Regrow after harvest
        if (growthStage == GrowthStage.GROWING && !isHarvestable) {
            long elapsed = System.currentTimeMillis() - lastHarvestTime;
            if (elapsed >= regrowthTimeMs) {
                growthStage = GrowthStage.MATURE;
                
                // 50% chance to be fully ripe
                if (Math.random() < 0.5) {
                    growthStage = GrowthStage.RIPE;
                }
                isHarvestable = true;
            }
        }
        
        // Harvested fields slowly restore (respawn)
        if (growthStage == GrowthStage.HARVESTED) {
            long elapsed = System.currentTimeMillis() - lastHarvestTime;
            if (elapsed >= regrowthTimeMs * 3) {
                // Field regrows
                harvestsRemaining = maxHarvests;
                growthStage = GrowthStage.SPROUTS;
            }
        }
        
        // Sprouts grow over time
        if (growthStage == GrowthStage.SPROUTS) {
            long elapsed = System.currentTimeMillis() - lastHarvestTime;
            if (elapsed >= regrowthTimeMs * 4) {
                growthStage = GrowthStage.MATURE;
                isHarvestable = true;
            }
        }
    }
    
    /**
     * Checks if a point is within this farmland's bounds.
     */
    public boolean contains(double checkX, double checkY, double scale) {
        double halfWidth = plotWidth * 8 * scale / 2;
        double halfHeight = plotHeight * 6 * scale / 2;
        
        return Math.abs(checkX - worldX) < halfWidth && 
               Math.abs(checkY - worldY) < halfHeight;
    }
    
    // ==================== GETTERS ====================
    
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public int getWidth() { return plotWidth; }
    public int getHeight() { return plotHeight; }
    public GrainType getGrainType() { return grainType; }
    public GrowthStage getGrowthStage() { return growthStage; }
    public boolean isHarvestable() { return isHarvestable && growthStage.canHarvest(); }
    public int getHarvestsRemaining() { return harvestsRemaining; }
    public Town getParentVillage() { return parentVillage; }
    
    /**
     * Gets info for tooltip display.
     */
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(grainType.getDisplayName()).append(" Field\n");
        sb.append("Stage: ").append(growthStage.getDisplayName()).append("\n");
        
        if (isHarvestable()) {
            sb.append("Status: Ready to harvest!\n");
            sb.append("Harvests left: ").append(harvestsRemaining);
        } else if (growthStage == GrowthStage.HARVESTED) {
            sb.append("Status: Depleted (regrowing)");
        } else {
            sb.append("Status: Growing...");
        }
        
        if (parentVillage != null) {
            sb.append("\nVillage: ").append(parentVillage.getName());
        }
        
        return sb.toString();
    }
}
