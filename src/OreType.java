import javafx.scene.paint.Color;

/**
 * Types of ore that can be mined from ore nodes.
 */
public enum OreType {
    COPPER("Copper", "Soft, reddish metal for tools", 
        Color.web("#B87333"), Color.web("#CD7F32"), Color.web("#8B4513"), 12, 1.0),
    
    TIN("Tin", "Silvery metal used in alloys", 
        Color.web("#D3D3D3"), Color.web("#C0C0C0"), Color.web("#808080"), 10, 0.9),
    
    IRON("Iron", "Strong metal for weapons and armor", 
        Color.web("#434343"), Color.web("#696969"), Color.web("#2F4F4F"), 18, 1.3),
    
    ZINC("Zinc", "Bluish-white metal for brass", 
        Color.web("#B8C4D0"), Color.web("#A0B0C0"), Color.web("#708090"), 14, 1.1);
    
    private final String displayName;
    private final String description;
    private final Color oreColor;      // Color of ore veins
    private final Color shimmerColor;  // Highlight color
    private final Color rockColor;     // Surrounding rock
    private final int baseValue;
    private final double mineModifier;
    
    OreType(String displayName, String description, Color oreColor, Color shimmerColor,
            Color rockColor, int baseValue, double mineModifier) {
        this.displayName = displayName;
        this.description = description;
        this.oreColor = oreColor;
        this.shimmerColor = shimmerColor;
        this.rockColor = rockColor;
        this.baseValue = baseValue;
        this.mineModifier = mineModifier;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Color getOreColor() { return oreColor; }
    public Color getShimmerColor() { return shimmerColor; }
    public Color getRockColor() { return rockColor; }
    public int getBaseValue() { return baseValue; }
    public double getMineModifier() { return mineModifier; }
    
    /**
     * Gets a random ore type.
     */
    public static OreType random(java.util.Random rand) {
        OreType[] types = values();
        return types[rand.nextInt(types.length)];
    }
    
    /**
     * Gets the item ID for this ore type.
     */
    public String getItemId() {
        return "ore_" + name().toLowerCase();
    }
}
