import javafx.scene.paint.Color;

/**
 * Types of fish that can be caught from fishing nodes.
 */
public enum FishType {
    TROUT("Trout", "Common freshwater fish", 
        Color.web("#8B9A6B"), Color.web("#CD853F"), Color.web("#FFE4B5"), 6, 1.0),
    
    SALMON("Salmon", "Prized pink-fleshed fish", 
        Color.web("#708090"), Color.web("#FA8072"), Color.web("#E6E6FA"), 10, 1.2),
    
    BASS("Bass", "Popular game fish", 
        Color.web("#4A6741"), Color.web("#9ACD32"), Color.web("#F5F5DC"), 8, 1.0),
    
    CATFISH("Catfish", "Bottom-dwelling whispered fish", 
        Color.web("#5C4033"), Color.web("#8B7355"), Color.web("#D2B48C"), 5, 0.9);
    
    private final String displayName;
    private final String description;
    private final Color bodyColor;
    private final Color finColor;
    private final Color bellyColor;
    private final int baseValue;
    private final double catchModifier;
    
    FishType(String displayName, String description, Color bodyColor, Color finColor, 
             Color bellyColor, int baseValue, double catchModifier) {
        this.displayName = displayName;
        this.description = description;
        this.bodyColor = bodyColor;
        this.finColor = finColor;
        this.bellyColor = bellyColor;
        this.baseValue = baseValue;
        this.catchModifier = catchModifier;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Color getBodyColor() { return bodyColor; }
    public Color getFinColor() { return finColor; }
    public Color getBellyColor() { return bellyColor; }
    public int getBaseValue() { return baseValue; }
    public double getCatchModifier() { return catchModifier; }
    
    /**
     * Gets a random fish type.
     */
    public static FishType random(java.util.Random rand) {
        FishType[] types = values();
        return types[rand.nextInt(types.length)];
    }
    
    /**
     * Gets the item ID for this fish type.
     */
    public String getItemId() {
        return "fish_" + name().toLowerCase();
    }
}
