/**
 * Types of livestock that can be raised in pastoral villages.
 * Each type produces a specific meat item.
 */
public enum LivestockType {
    SHEEP("Sheep", "mutton", "Mutton", "#d4c4b0", 3, 6),      // Woolly sheep - 3-6 yield
    COW("Cow", "beef", "Beef", "#8b6914", 5, 10),             // Cattle - 5-10 yield  
    PIG("Pig", "pork", "Pork", "#ffb6c1", 4, 8),              // Pigs - 4-8 yield
    CHICKEN("Chicken", "chicken", "Chicken", "#f5f5dc", 2, 4); // Poultry - 2-4 yield
    
    private final String displayName;
    private final String itemSuffix;  // For itemId: meat_<suffix>
    private final String meatName;    // Display name for the meat
    private final String color;       // Color for rendering
    private final int minYield;       // Minimum harvest yield
    private final int maxYield;       // Maximum harvest yield
    
    LivestockType(String displayName, String itemSuffix, String meatName, 
                  String color, int minYield, int maxYield) {
        this.displayName = displayName;
        this.itemSuffix = itemSuffix;
        this.meatName = meatName;
        this.color = color;
        this.minYield = minYield;
        this.maxYield = maxYield;
    }
    
    public String getDisplayName() { return displayName; }
    public String getItemSuffix() { return itemSuffix; }
    public String getMeatName() { return meatName; }
    public String getColor() { return color; }
    public int getMinYield() { return minYield; }
    public int getMaxYield() { return maxYield; }
    
    /**
     * Gets the full item ID for this livestock's meat.
     */
    public String getItemId() {
        return "meat_" + itemSuffix;
    }
    
    /**
     * Gets the livestock type color.
     */
    public javafx.scene.paint.Color getColorFX() {
        return javafx.scene.paint.Color.web(color);
    }
    
    /**
     * Calculates random yield within range.
     */
    public int calculateYield() {
        return minYield + (int)(Math.random() * (maxYield - minYield + 1));
    }
    
    /**
     * Returns a random livestock type.
     */
    public static LivestockType random() {
        LivestockType[] values = values();
        return values[(int)(Math.random() * values.length)];
    }
    
    /**
     * Returns the most common livestock for a given terrain.
     */
    public static LivestockType forTerrain(TerrainType terrain) {
        if (terrain == null) return SHEEP;
        
        return switch (terrain) {
            case GRASS, PLAINS, MEADOW -> Math.random() < 0.5 ? COW : SHEEP;
            case FOREST -> PIG;  // Pigs forage in forest
            case HILLS -> SHEEP;       // Sheep on hillsides
            default -> CHICKEN;        // Chickens anywhere
        };
    }
}
