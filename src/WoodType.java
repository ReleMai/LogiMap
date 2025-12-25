import javafx.scene.paint.Color;

/**
 * Types of wood that can be harvested from lumber nodes.
 */
public enum WoodType {
    OAK("Oak", "Strong, versatile hardwood", 
        Color.web("#8B6914"), Color.web("#228B22"), 8, 1.0),
    
    BIRCH("Birch", "Light-colored softwood", 
        Color.web("#D4C4A8"), Color.web("#90EE90"), 6, 0.8),
    
    PINE("Pine", "Evergreen softwood with sap", 
        Color.web("#654321"), Color.web("#2E8B57"), 7, 0.9),
    
    MAPLE("Maple", "Dense hardwood with beautiful grain", 
        Color.web("#CD853F"), Color.web("#FF6347"), 10, 1.2);
    
    private final String displayName;
    private final String description;
    private final Color barkColor;
    private final Color leafColor;
    private final int baseValue;
    private final double harvestModifier;
    
    WoodType(String displayName, String description, Color barkColor, Color leafColor, 
             int baseValue, double harvestModifier) {
        this.displayName = displayName;
        this.description = description;
        this.barkColor = barkColor;
        this.leafColor = leafColor;
        this.baseValue = baseValue;
        this.harvestModifier = harvestModifier;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Color getBarkColor() { return barkColor; }
    public Color getLeafColor() { return leafColor; }
    public int getBaseValue() { return baseValue; }
    public double getHarvestModifier() { return harvestModifier; }
    
    /**
     * Gets a random wood type.
     */
    public static WoodType random(java.util.Random rand) {
        WoodType[] types = values();
        return types[rand.nextInt(types.length)];
    }
    
    /**
     * Gets the item ID for this wood type.
     */
    public String getItemId() {
        return "timber_" + name().toLowerCase();
    }
}
