import javafx.scene.paint.Color;

/**
 * Represents a town settlement on the map.
 * Towns serve as trade hubs and population centers.
 * Can be either major towns (larger, higher trade value) or minor towns.
 * 
 * Villages (minor towns) are assigned a VillageType that determines
 * their primary economic activity and what resources spawn nearby.
 */
public class Town extends MapStructure {
    
    private boolean isMajor;
    private double tradeValue;
    
    // Village classification system
    private VillageType villageType;
    private Object specificResource; // e.g., GrainType.WHEAT for agricultural
    private boolean hasInn = false;
    private int innCost = 2; // Gold cost for instant rest (reduced from 5)
    
    // Town warehouse for player storage
    private TownWarehouse warehouse;
    
    // Tavern NPCs available for hire
    private TavernNPC[] tavernNPCs;
    private boolean tavernPopulated = false;
    
    // Visual variant for rendering (0-3)
    private int visualVariant;
    
    // Entrance points for roads and NPCs
    private int entranceX;
    private int entranceY;
    
    public Town(int gridX, int gridY, String name, boolean isMajor) {
        super(gridX, gridY, name, isMajor ? 8 : 4, 
              isMajor ? Color.web("#d4a574") : Color.web("#c9956d"), 
              isMajor ? "Major Town" : "Minor Town");
        
        this.isMajor = isMajor;
        this.population = isMajor ? 50000 : 10000;
        this.tradeValue = isMajor ? 500 : 100;
        
        // Set visual variant based on position hash for consistency (5 variants)
        this.visualVariant = Math.abs((gridX * 31 + gridY * 17) % 5);
        
        // Set entrance at the south side of town (center bottom)
        this.entranceX = gridX + size / 2;
        this.entranceY = gridY + size;
        
        // Major towns have mixed economy and always have inns
        // Minor towns get classified with a specific type
        if (isMajor) {
            this.villageType = VillageType.MIXED;
            this.hasInn = true;
        } else {
            this.villageType = VillageType.AGRICULTURAL; // Default, should be set properly
            this.hasInn = false;
        }
        
        // Initialize warehouse
        this.warehouse = new TownWarehouse(name, isMajor);
    }
    
    /**
     * Creates a classified village with a specific type.
     */
    public Town(int gridX, int gridY, String name, VillageType type, Object specificResource) {
        super(gridX, gridY, name, 4, 
              Color.web(type.getRoofColor()), 
              type.getDisplayName());
        
        this.isMajor = false;
        this.villageType = type;
        this.specificResource = specificResource;
        this.population = 5000 + Math.random() * 10000;
        this.tradeValue = 50 + Math.random() * 100;
        this.hasInn = Math.random() < 0.3; // 30% of villages have inns
        this.innCost = 2; // Cheaper than cities
        
        // Set visual variant based on position hash for consistency (5 variants)
        this.visualVariant = Math.abs((gridX * 31 + gridY * 17) % 5);
        
        // Set entrance at the south side of town (center bottom)
        this.entranceX = gridX + size / 2;
        this.entranceY = gridY + size;
        
        // Initialize warehouse
        this.warehouse = new TownWarehouse(name, false);
    }
    
    // ==================== Entrance/Position Methods ====================
    
    /**
     * Gets the entrance X coordinate (where roads connect and NPCs enter).
     */
    public int getEntranceX() {
        return entranceX;
    }
    
    /**
     * Gets the entrance Y coordinate.
     */
    public int getEntranceY() {
        return entranceY;
    }
    
    /**
     * Gets the center X coordinate of the town.
     */
    public int getCenterX() {
        return gridX + size / 2;
    }
    
    /**
     * Gets the center Y coordinate of the town.
     */
    public int getCenterY() {
        return gridY + size / 2;
    }
    
    /**
     * Gets the visual variant (0-3) for rendering different town sprites.
     */
    public int getVisualVariant() {
        return visualVariant;
    }
    
    public boolean isMajor() {
        return isMajor;
    }
    
    public double getTradeValue() {
        return tradeValue;
    }
    
    public VillageType getVillageType() {
        return villageType;
    }
    
    public void setVillageType(VillageType type) {
        this.villageType = type;
        this.type = type.getDisplayName();
        this.color = Color.web(type.getRoofColor());
    }
    
    public Object getSpecificResource() {
        return specificResource;
    }
    
    public void setSpecificResource(Object resource) {
        this.specificResource = resource;
    }

    /**
     * Returns the canonical item ID for the town's specific resource, or null if none.
     */
    public String getSpecificResourceId() {
        if (specificResource == null) return null;
        if (specificResource instanceof GrainType) {
            return "grain_" + ((GrainType) specificResource).name().toLowerCase();
        } else if (specificResource instanceof WoodType) {
            return "timber_" + ((WoodType) specificResource).name().toLowerCase();
        } else if (specificResource instanceof StoneType) {
            return "stone_" + ((StoneType) specificResource).name().toLowerCase();
        } else if (specificResource instanceof FishType) {
            return "fish_" + ((FishType) specificResource).name().toLowerCase();
        } else if (specificResource instanceof OreType) {
            return "ore_" + ((OreType) specificResource).name().toLowerCase();
        } else if (specificResource instanceof LivestockType) {
            return ((LivestockType) specificResource).getItemId();
        }
        return null;
    }

    /**
     * Checks if this town produces the given high-level resource category.
     * Used by NPC parties to avoid selling local goods back to producers.
     */
    public boolean producesResourceType(ResourceType type) {
        if (specificResource == null) return false;
        if (type == ResourceType.FERTILITY) {
            return specificResource instanceof GrainType || specificResource instanceof LivestockType;
        } else if (type == ResourceType.WOOD) {
            return specificResource instanceof WoodType;
        } else if (type == ResourceType.STONE) {
            return specificResource instanceof StoneType || specificResource instanceof OreType;
        } else if (type == ResourceType.ORE) {
            return specificResource instanceof OreType;
        } else if (type == ResourceType.FISH) {
            return specificResource instanceof FishType;
        }
        return false;
    }
    
    public boolean hasInn() {
        return hasInn || isMajor; // Major towns always have inns
    }
    
    public int getInnCost() {
        return isMajor ? innCost : (innCost / 2); // Villages cheaper
    }
    
    /**
     * Gets the town warehouse for player storage.
     */
    public TownWarehouse getWarehouse() {
        return warehouse;
    }
    
    /**
     * Sets the town warehouse (used during save loading).
     */
    public void setWarehouse(TownWarehouse warehouse) {
        this.warehouse = warehouse;
    }
    
    /**
     * Gets the rest location type for this town.
     */
    public PlayerEnergy.RestLocation getRestLocation() {
        if (hasInn()) {
            return isMajor ? PlayerEnergy.RestLocation.CITY_INN 
                          : PlayerEnergy.RestLocation.VILLAGE_INN;
        }
        return PlayerEnergy.RestLocation.VILLAGE_OUTDOORS;
    }
    
    /**
     * Gets the tavern NPCs available for hire.
     * Generates them on first access (lazy loading).
     */
    public TavernNPC[] getTavernNPCs() {
        if (!tavernPopulated) {
            generateTavernNPCs();
        }
        return tavernNPCs;
    }
    
    /**
     * Generates tavern NPCs based on town size and type.
     */
    private void generateTavernNPCs() {
        // Determine number of NPCs based on town type
        int npcCount;
        if (isMajor) {
            // Major towns have more recruits (4-8)
            npcCount = 4 + (int)(Math.random() * 5);
        } else if (hasInn()) {
            // Villages with inns have some recruits (2-4)
            npcCount = 2 + (int)(Math.random() * 3);
        } else {
            // Small villages have few recruits (1-2)
            npcCount = 1 + (int)(Math.random() * 2);
        }
        
        // Generate a balanced group for major towns, random for villages
        if (isMajor) {
            tavernNPCs = TavernNPC.generateBalancedGroup(npcCount);
        } else {
            tavernNPCs = TavernNPC.generateTavernNPCs(npcCount);
        }
        
        tavernPopulated = true;
    }
    
    /**
     * Refreshes the tavern NPCs (called when time passes or player revisits).
     * Keeps hired NPCs tracked but generates new available ones.
     */
    public void refreshTavernNPCs() {
        // In future: could track hired NPCs separately
        // For now, just regenerate
        tavernPopulated = false;
    }
    
    /**
     * Gets the number of available (unhired) NPCs in the tavern.
     */
    public int getAvailableNPCCount() {
        if (tavernNPCs == null) return 0;
        int count = 0;
        for (TavernNPC npc : tavernNPCs) {
            if (!npc.isHired()) count++;
        }
        return count;
    }
    
    /**
     * Gets a display string for the primary resource.
     */
    public String getResourceDisplay() {
        if (specificResource != null) {
            if (specificResource instanceof GrainType) {
                return ((GrainType) specificResource).getDisplayName();
            }
            return specificResource.toString();
        }
        if (villageType != null) {
            return villageType.getPrimaryResource().getDisplayName();
        }
        return "Mixed";
    }
    
    @Override
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append("Type: ").append(type).append("\n");
        
        if (!isMajor && villageType != null) {
            sb.append("Economy: ").append(villageType.getPrimaryResource().getDisplayName()).append("\n");
            if (specificResource != null) {
                sb.append("Specialty: ").append(getResourceDisplay()).append("\n");
            }
        }
        
        sb.append(String.format("Population: %,.0f\n", population));
        sb.append(String.format("Trade Value: %.0f\n", tradeValue));
        
        if (hasInn()) {
            sb.append("Inn: Available (").append(getInnCost()).append(" gold)\n");
        } else {
            sb.append("Rest: Village square (free, slow)\n");
        }
        
        sb.append("Size: ").append(size).append(" cells");
        
        return sb.toString();
    }
}
