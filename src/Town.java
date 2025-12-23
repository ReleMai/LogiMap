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
    private int innCost = 5; // Gold cost for instant rest
    
    public Town(int gridX, int gridY, String name, boolean isMajor) {
        super(gridX, gridY, name, isMajor ? 8 : 4, 
              isMajor ? Color.web("#d4a574") : Color.web("#c9956d"), 
              isMajor ? "Major Town" : "Minor Town");
        
        this.isMajor = isMajor;
        this.population = isMajor ? 50000 : 10000;
        this.tradeValue = isMajor ? 500 : 100;
        
        // Major towns have mixed economy and always have inns
        // Minor towns get classified with a specific type
        if (isMajor) {
            this.villageType = VillageType.MIXED;
            this.hasInn = true;
        } else {
            this.villageType = VillageType.AGRICULTURAL; // Default, should be set properly
            this.hasInn = false;
        }
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
    
    public boolean hasInn() {
        return hasInn || isMajor; // Major towns always have inns
    }
    
    public int getInnCost() {
        return isMajor ? innCost : (innCost / 2); // Villages cheaper
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
