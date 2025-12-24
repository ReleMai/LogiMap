import javafx.scene.paint.Color;

/**
 * Enumeration of terrain types with visual properties and traversal costs.
 * Each terrain type has associated colors, movement costs, and buildability.
 */
public enum TerrainType {
    // Water bodies
    DEEP_OCEAN("Deep Ocean", Color.web("#0a2a4a"), Color.web("#061e36"), 0.0, false),
    OCEAN("Ocean", Color.web("#1e4d8b"), Color.web("#143a6b"), 0.0, false),
    SHALLOW_WATER("Shallow Water", Color.web("#3a7ab8"), Color.web("#2a6090"), 0.0, false),
    
    // Coastal
    BEACH("Beach", Color.web("#e8d4a8"), Color.web("#d4c090"), 1.2, true),
    REEF("Coral Reef", Color.web("#40a0a0"), Color.web("#308888"), 0.0, false),
    
    // Lowlands
    GRASS("Grassland", Color.web("#7cba4c"), Color.web("#5a9830"), 1.0, true),
    PLAINS("Plains", Color.web("#a8c868"), Color.web("#90b050"), 1.0, true),
    MEADOW("Meadow", Color.web("#90d050"), Color.web("#78b838"), 1.0, true),
    
    // Forests
    FOREST("Forest", Color.web("#2d6b1a"), Color.web("#1f4d12"), 1.5, true),
    DENSE_FOREST("Dense Forest", Color.web("#1a4a0f"), Color.web("#0f2808"), 2.0, true),
    TAIGA("Taiga", Color.web("#3a6848"), Color.web("#2a5038"), 1.8, true),
    JUNGLE("Jungle", Color.web("#1a5a20"), Color.web("#104010"), 2.5, true),
    
    // Wetlands
    SWAMP("Swamp", Color.web("#4a6840"), Color.web("#385030"), 2.2, false),
    MARSH("Marsh", Color.web("#5a7850"), Color.web("#486040"), 2.0, false),
    
    // Arid
    DESERT("Desert", Color.web("#e8d090"), Color.web("#d0b868"), 1.3, true),
    DUNES("Sand Dunes", Color.web("#f0d878"), Color.web("#d8c060"), 1.5, true),
    SAVANNA("Savanna", Color.web("#c8b060"), Color.web("#b09848"), 1.1, true),
    SCRUBLAND("Scrubland", Color.web("#9a9060"), Color.web("#807848"), 1.2, true),
    
    // Highlands
    HILLS("Hills", Color.web("#8a9a70"), Color.web("#707f58"), 1.4, true),
    ROCKY_HILLS("Rocky Hills", Color.web("#888078"), Color.web("#686058"), 1.6, true),
    MOUNTAIN("Mountain", Color.web("#7a7068"), Color.web("#5a5048"), 3.0, false),
    MOUNTAIN_PEAK("Mountain Peak", Color.web("#9a9090"), Color.web("#787070"), 0.0, false),
    VOLCANO("Volcano", Color.web("#4a3028"), Color.web("#2a1810"), 0.0, false),
    
    // Cold
    TUNDRA("Tundra", Color.web("#a8b8a0"), Color.web("#90a088"), 1.4, true),
    SNOW("Snow", Color.web("#e8f0f0"), Color.web("#d0e0e0"), 1.8, true),
    ICE("Ice", Color.web("#c0e0f0"), Color.web("#a0c8e0"), 2.5, false),
    GLACIER("Glacier", Color.web("#b0d8e8"), Color.web("#90c0d8"), 0.0, false),
    
    // Special
    CLIFF("Cliff", Color.web("#686058"), Color.web("#484038"), 0.0, false),
    CANYON("Canyon", Color.web("#a08060"), Color.web("#806040"), 0.0, false),
    LAVA("Lava", Color.web("#ff4010"), Color.web("#c02000"), 0.0, false);
    
    private final String displayName;
    private final Color primaryColor;
    private final Color shadowColor;
    private final double movementCost;
    private final boolean buildable;
    
    TerrainType(String displayName, Color primaryColor, Color shadowColor, 
                double movementCost, boolean buildable) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.shadowColor = shadowColor;
        this.movementCost = movementCost;
        this.buildable = buildable;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getName() {
        return displayName;
    }
    
    public Color getPrimaryColor() {
        return primaryColor;
    }
    
    public Color getShadowColor() {
        return shadowColor;
    }
    
    public double getMovementCost() {
        return movementCost;
    }
    
    public boolean isBuildable() {
        return buildable;
    }
    
    public boolean isWater() {
        return this == DEEP_OCEAN || this == OCEAN || this == SHALLOW_WATER || this == REEF;
    }
    
    public boolean isDeepWater() {
        return this == DEEP_OCEAN || this == OCEAN;
    }
    
    public boolean isShallowWater() {
        return this == SHALLOW_WATER || this == REEF;
    }
    
    public boolean isForest() {
        return this == FOREST || this == DENSE_FOREST || this == TAIGA || this == JUNGLE;
    }
    
    public boolean isMountainous() {
        return this == MOUNTAIN || this == MOUNTAIN_PEAK || this == ROCKY_HILLS || this == VOLCANO;
    }
}
