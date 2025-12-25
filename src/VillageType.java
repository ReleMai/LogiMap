/**
 * Classification types for villages/towns based on their primary economic activity.
 * 
 * Villages are assigned a type at spawn based on nearby terrain and resources.
 * The village type determines:
 * - What resource nodes spawn around it
 * - What goods are available for trade
 * - What services the village offers
 * - Visual appearance and decorations
 */
public enum VillageType {
    
    // Agricultural villages produce food crops
    AGRICULTURAL("Agricultural Village", "Produces grain and crops", 
        ResourceCategory.GRAIN, "#8B7355", "#2d5016"),
    
    // Farming villages focus on animal husbandry
    PASTORAL("Pastoral Village", "Raises livestock and produces dairy", 
        ResourceCategory.ANIMAL, "#9B8355", "#3d5016"),
    
    // Mining villages extract stone and ore
    MINING("Mining Village", "Extracts stone and ore", 
        ResourceCategory.MINERAL, "#6B6355", "#4a4a4a"),
    
    // Lumber villages harvest and process wood
    LUMBER("Lumber Village", "Harvests and processes timber", 
        ResourceCategory.WOOD, "#5B4335", "#1d3516"),
    
    // Fishing villages on coasts or rivers
    FISHING("Fishing Village", "Catches fish and harvests sea resources", 
        ResourceCategory.FISH, "#4B6375", "#1d4a5a"),
    
    // Trading posts focus on commerce
    TRADING("Trading Post", "Hub for regional trade", 
        ResourceCategory.TRADE, "#7B6355", "#4a3a2a"),
    
    // Mixed economy (default for cities)
    MIXED("Settlement", "Diverse economy", 
        ResourceCategory.MIXED, "#6B5545", "#3a3a3a");
    
    private final String displayName;
    private final String description;
    private final ResourceCategory primaryResource;
    private final String roofColor; // Hex color for village roofs
    private final String accentColor; // Hex color for decorations
    
    VillageType(String displayName, String description, ResourceCategory primaryResource,
                String roofColor, String accentColor) {
        this.displayName = displayName;
        this.description = description;
        this.primaryResource = primaryResource;
        this.roofColor = roofColor;
        this.accentColor = accentColor;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public ResourceCategory getPrimaryResource() { return primaryResource; }
    public String getRoofColor() { return roofColor; }
    public String getAccentColor() { return accentColor; }
    
    /**
     * Resource categories that villages can produce.
     */
    public enum ResourceCategory {
        GRAIN("Grain", GrainType.values()),
        ANIMAL("Animal Products", null), // Future: livestock types
        MINERAL("Minerals", null), // Future: ore types
        WOOD("Timber", null), // Future: wood types
        FISH("Fish", null), // Future: fish types
        TRADE("Trade Goods", null),
        MIXED("Mixed", null);
        
        private final String displayName;
        private final Object[] subtypes;
        
        ResourceCategory(String displayName, Object[] subtypes) {
            this.displayName = displayName;
            this.subtypes = subtypes;
        }
        
        public String getDisplayName() { return displayName; }
        public Object[] getSubtypes() { return subtypes; }
        
        /**
         * Gets a random subtype for this category.
         */
        public Object getRandomSubtype(java.util.Random random) {
            if (subtypes == null || subtypes.length == 0) return null;
            return subtypes[random.nextInt(subtypes.length)];
        }
    }
    
    /**
     * Determines the best village type based on terrain.
     * 
     * @param terrain The terrain at the village location
     * @param nearWater Whether the village is near water
     * @param nearMountain Whether the village is near mountains
     * @param nearForest Whether the village is near forest
     * @param random Random source for variety
     * @return The recommended village type
     */
    public static VillageType determineFromTerrain(TerrainType terrain, boolean nearWater, 
                                                     boolean nearMountain, boolean nearForest,
                                                     java.util.Random random) {
        // Priority order based on terrain features
        
        // Coastal = fishing
        if (nearWater && random.nextDouble() < 0.7) {
            return FISHING;
        }
        
        // Near mountains = mining
        if (nearMountain && random.nextDouble() < 0.6) {
            return MINING;
        }
        
        // In/near forest = lumber
        if (nearForest && random.nextDouble() < 0.5) {
            return LUMBER;
        }
        
        // Plains/grassland = agricultural or pastoral
        if (terrain == TerrainType.PLAINS || terrain == TerrainType.GRASS || terrain == TerrainType.MEADOW) {
            return random.nextDouble() < 0.7 ? AGRICULTURAL : PASTORAL;
        }
        
        // Hills = pastoral or mining
        if (terrain == TerrainType.HILLS) {
            return random.nextDouble() < 0.5 ? PASTORAL : MINING;
        }
        
        // Default to agricultural
        return AGRICULTURAL;
    }
}
