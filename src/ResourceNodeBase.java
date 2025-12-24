import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * BLUEPRINT: Base class for all resource nodes in the game world.
 * 
 * Resource nodes are interactive areas that spawn around villages based on village type.
 * Each node type (Farmland, Lumber, Quarry, Fishery, Ore) extends this base class.
 * 
 * == HOW TO CREATE A NEW RESOURCE NODE ==
 * 
 * 1. Create a new enum for the resource subtype (e.g., WoodType, StoneType, FishType, OreType)
 * 2. Create a new class extending ResourceNodeBase
 * 3. Override the render() method to draw the node's unique sprite
 * 4. Override getYield() to return harvest amounts
 * 5. Register items in ItemRegistry for the new resources
 * 6. Add node generation logic to DemoWorld
 * 7. Create an interaction menu class (or use ResourceInteraction generic handler)
 * 
 * == COMMON PROPERTIES ==
 * - worldX, worldY: Position in world coordinates
 * - parentVillage: The village this node belongs to
 * - harvestsRemaining: How many times it can be harvested before depletion
 * - isHarvestable: Whether the node is ready to harvest
 * - regrowthTimeMs: Time to regenerate after harvest
 * - resourceType: The specific resource this node produces
 */
public abstract class ResourceNodeBase {
    
    // Position in world coordinates
    protected double worldX;
    protected double worldY;
    
    // Node properties
    protected Town parentVillage;
    protected boolean isHarvestable;
    protected int harvestsRemaining;
    protected int maxHarvests;
    protected long lastHarvestTime;
    protected long regrowthTimeMs;
    
    // The specific resource this node produces (e.g., WHEAT, OAK, GRANITE)
    protected Object resourceType;
    
    // Visual variety
    protected int variant;
    protected double rotation;
    protected double size;
    
    // Interaction bounds
    protected double interactionRadius = 3.0;
    
    /**
     * Creates a resource node at the specified location.
     */
    public ResourceNodeBase(double worldX, double worldY, Object resourceType, Town parentVillage) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.resourceType = resourceType;
        this.parentVillage = parentVillage;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // Default values - subclasses should override
        this.maxHarvests = 3 + rand.nextInt(3);
        this.harvestsRemaining = maxHarvests;
        this.isHarvestable = true;
        this.regrowthTimeMs = 60000 + rand.nextInt(60000);
        
        // Visual variety
        this.variant = rand.nextInt(4);
        this.rotation = (rand.nextDouble() - 0.5) * 0.3;
        this.size = 0.8 + rand.nextDouble() * 0.4;
    }
    
    // ==================== ABSTRACT METHODS (MUST OVERRIDE) ====================
    
    /**
     * Renders the node sprite at the given screen position.
     * Each node type implements its unique visual appearance.
     */
    public abstract void render(GraphicsContext gc, double screenX, double screenY, double zoom, int tileSize);
    
    /**
     * Returns the base yield for a single harvest.
     * Subclasses calculate based on growth, variant, etc.
     */
    public abstract int getBaseYield();
    
    /**
     * Returns the resource category name (e.g., "Grain", "Wood", "Stone", "Fish", "Ore")
     */
    public abstract String getResourceCategory();
    
    /**
     * Returns the item ID for the resource produced.
     */
    public abstract String getItemId();
    
    // ==================== COMMON METHODS ====================
    
    /**
     * Attempts to harvest from this node.
     * @return The yield amount, or 0 if not harvestable
     */
    public int harvest() {
        if (!isHarvestable || harvestsRemaining <= 0) {
            return 0;
        }
        
        int yield = getBaseYield();
        harvestsRemaining--;
        lastHarvestTime = System.currentTimeMillis();
        
        if (harvestsRemaining <= 0) {
            isHarvestable = false;
        }
        
        return yield;
    }
    
    /**
     * Updates the node state (regrowth, etc.)
     */
    public void update() {
        if (!isHarvestable && harvestsRemaining <= 0) {
            long timeSinceHarvest = System.currentTimeMillis() - lastHarvestTime;
            if (timeSinceHarvest >= regrowthTimeMs) {
                // Regrow
                harvestsRemaining = maxHarvests;
                isHarvestable = true;
            }
        }
    }
    
    /**
     * Checks if a point is within interaction range of this node.
     */
    public boolean isInRange(double px, double py) {
        double dx = px - worldX;
        double dy = py - worldY;
        return Math.sqrt(dx * dx + dy * dy) <= interactionRadius;
    }
    
    /**
     * Checks if this node contains the given screen point.
     */
    public boolean containsScreenPoint(double screenX, double screenY, double nodeScreenX, 
                                        double nodeScreenY, double renderSize) {
        double halfSize = renderSize / 2;
        return screenX >= nodeScreenX - halfSize && screenX <= nodeScreenX + halfSize &&
               screenY >= nodeScreenY - halfSize && screenY <= nodeScreenY + halfSize;
    }
    
    // ==================== GETTERS/SETTERS ====================
    
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public Town getParentVillage() { return parentVillage; }
    public boolean isHarvestable() { return isHarvestable && harvestsRemaining > 0; }
    public int getHarvestsRemaining() { return harvestsRemaining; }
    public int getMaxHarvests() { return maxHarvests; }
    public Object getResourceType() { return resourceType; }
    public int getVariant() { return variant; }
    public double getRotation() { return rotation; }
    public double getSize() { return size; }
    public double getInteractionRadius() { return interactionRadius; }
    
    public void setInteractionRadius(double radius) { this.interactionRadius = radius; }
    
    /**
     * Returns a display name for this node.
     */
    public String getDisplayName() {
        return getResourceCategory() + " Node";
    }
    
    /**
     * Returns status text for the interaction menu.
     */
    public String getStatusText() {
        if (!isHarvestable) {
            long remaining = regrowthTimeMs - (System.currentTimeMillis() - lastHarvestTime);
            int seconds = (int)(remaining / 1000);
            return "Regrowing... " + seconds + "s";
        }
        return "Ready (" + harvestsRemaining + "/" + maxHarvests + ")";
    }
}
