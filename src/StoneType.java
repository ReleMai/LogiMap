import javafx.scene.paint.Color;

/**
 * Types of stone that can be quarried from stone nodes.
 */
public enum StoneType {
    GRANITE("Granite", "Hard, speckled ignite rock", 
        Color.web("#8B7765"), Color.web("#CD853F"), 10, 1.0),
    
    LIMESTONE("Limestone", "Sedimentary rock, good for building", 
        Color.web("#D4C9A8"), Color.web("#F5DEB3"), 8, 0.9),
    
    SLATE("Slate", "Fine-grained metamorphic rock", 
        Color.web("#4A5568"), Color.web("#718096"), 12, 1.1),
    
    SANDSTONE("Sandstone", "Warm-colored sedimentary rock", 
        Color.web("#C4A35A"), Color.web("#DAA520"), 6, 0.85);
    
    private final String displayName;
    private final String description;
    private final Color primaryColor;
    private final Color secondaryColor;
    private final int baseValue;
    private final double harvestModifier;
    
    StoneType(String displayName, String description, Color primaryColor, Color secondaryColor,
              int baseValue, double harvestModifier) {
        this.displayName = displayName;
        this.description = description;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.baseValue = baseValue;
        this.harvestModifier = harvestModifier;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public int getBaseValue() { return baseValue; }
    public double getHarvestModifier() { return harvestModifier; }
    
    /**
     * Gets a random stone type.
     */
    public static StoneType random(java.util.Random rand) {
        StoneType[] types = values();
        return types[rand.nextInt(types.length)];
    }
    
    /**
     * Gets the item ID for this stone type.
     */
    public String getItemId() {
        return "stone_" + name().toLowerCase();
    }
}
