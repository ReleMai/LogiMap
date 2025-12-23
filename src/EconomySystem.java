import java.util.*;

/**
 * Economy system for buying and selling resources.
 * 
 * Features:
 * - Base prices for all items
 * - Scarcity multipliers based on local supply/demand
 * - Different prices at villages vs cities
 * - Price memory (tracks recent trades)
 */
public class EconomySystem {
    
    // Base prices for different resource types (in gold)
    private static final Map<String, Integer> BASE_PRICES = new HashMap<>();
    
    static {
        // Grain prices
        BASE_PRICES.put("grain_wheat", 2);
        BASE_PRICES.put("grain_oat", 3);
        BASE_PRICES.put("grain_barley", 3);
        BASE_PRICES.put("grain_rye", 4);
        
        // Other resources (future)
        BASE_PRICES.put("wood_oak", 5);
        BASE_PRICES.put("wood_pine", 3);
        BASE_PRICES.put("ore_iron", 8);
        BASE_PRICES.put("ore_copper", 6);
        BASE_PRICES.put("fish_common", 4);
        BASE_PRICES.put("fish_rare", 12);
        BASE_PRICES.put("stone_common", 2);
        BASE_PRICES.put("stone_marble", 10);
    }
    
    // Track supply at each town (resource ID -> quantity available)
    private Map<Town, Map<String, Integer>> townSupply = new HashMap<>();
    
    // Track demand at each town (resource ID -> demand level 0-2)
    private Map<Town, Map<String, Double>> townDemand = new HashMap<>();
    
    // Price fluctuation settings
    private static final double MIN_SCARCITY_MULT = 0.5;  // Lowest price when oversupplied
    private static final double MAX_SCARCITY_MULT = 2.5;  // Highest price when scarce
    private static final double VILLAGE_BUY_MULT = 0.8;   // Villages buy for 80% of value
    private static final double CITY_BUY_MULT = 0.9;      // Cities buy for 90% of value
    
    // Random for fluctuations
    private Random random;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 60000; // Update economy every minute
    
    /**
     * Creates an economy system.
     */
    public EconomySystem(long seed) {
        this.random = new Random(seed);
        initializeSupplyDemand();
    }
    
    /**
     * Initializes supply and demand for all towns.
     */
    private void initializeSupplyDemand() {
        // Will be populated when towns are added
    }
    
    /**
     * Registers a town in the economy.
     */
    public void registerTown(Town town) {
        Map<String, Integer> supply = new HashMap<>();
        Map<String, Double> demand = new HashMap<>();
        
        // Initialize based on village type
        VillageType type = town.getVillageType();
        
        for (String resourceId : BASE_PRICES.keySet()) {
            // Default supply and demand
            int baseSupply = 50 + random.nextInt(50);
            double baseDemand = 1.0;
            
            // Adjust based on village specialty
            if (type != null) {
                if (resourceId.startsWith("grain_") && type == VillageType.AGRICULTURAL) {
                    // Agricultural villages produce grain - high supply
                    baseSupply *= 2;
                    baseDemand = 0.5; // Low demand (they have plenty)
                    
                    // Even higher for their specific grain type
                    Object specific = town.getSpecificResource();
                    if (specific instanceof GrainType) {
                        String specificId = "grain_" + ((GrainType) specific).name().toLowerCase();
                        if (resourceId.equals(specificId)) {
                            baseSupply *= 2;
                            baseDemand = 0.3;
                        }
                    }
                } else if (resourceId.startsWith("wood_") && type == VillageType.LUMBER) {
                    baseSupply *= 2;
                    baseDemand = 0.5;
                } else if (resourceId.startsWith("ore_") && type == VillageType.MINING) {
                    baseSupply *= 2;
                    baseDemand = 0.5;
                } else if (resourceId.startsWith("fish_") && type == VillageType.FISHING) {
                    baseSupply *= 2;
                    baseDemand = 0.5;
                }
            }
            
            // Cities have more demand and higher base supply
            if (town.isMajor()) {
                baseSupply *= 1.5;
                baseDemand *= 1.5;
            }
            
            supply.put(resourceId, baseSupply);
            demand.put(resourceId, baseDemand);
        }
        
        townSupply.put(town, supply);
        townDemand.put(town, demand);
    }
    
    /**
     * Updates the economy simulation.
     */
    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_INTERVAL_MS) return;
        lastUpdateTime = now;
        
        // Fluctuate supply and demand slightly
        for (Town town : townSupply.keySet()) {
            Map<String, Integer> supply = townSupply.get(town);
            Map<String, Double> demand = townDemand.get(town);
            
            for (String resourceId : supply.keySet()) {
                // Natural production/consumption
                int currentSupply = supply.get(resourceId);
                double currentDemand = demand.get(resourceId);
                
                // Supply slowly regenerates
                int regen = 1 + random.nextInt(3);
                
                // Adjust regen based on village type
                VillageType type = town.getVillageType();
                if (type != null) {
                    if ((resourceId.startsWith("grain_") && type == VillageType.AGRICULTURAL) ||
                        (resourceId.startsWith("wood_") && type == VillageType.LUMBER) ||
                        (resourceId.startsWith("ore_") && type == VillageType.MINING) ||
                        (resourceId.startsWith("fish_") && type == VillageType.FISHING)) {
                        regen *= 3;
                    }
                }
                
                supply.put(resourceId, Math.min(200, currentSupply + regen));
                
                // Demand fluctuates randomly
                double demandChange = (random.nextDouble() - 0.5) * 0.1;
                demand.put(resourceId, Math.max(0.3, Math.min(2.0, currentDemand + demandChange)));
            }
        }
    }
    
    // ==================== PRICE CALCULATION ====================
    
    /**
     * Gets the sell price for a resource at a town.
     */
    public int getSellPrice(Town town, ResourceItem item) {
        String resourceId = item.getId();
        int basePrice = BASE_PRICES.getOrDefault(resourceId, 1);
        
        double scarcity = getScarcityMultiplier(town, resourceId);
        double quality = item.getQuality().getPriceMultiplier();
        double locationMult = town.isMajor() ? CITY_BUY_MULT : VILLAGE_BUY_MULT;
        
        double finalPrice = basePrice * scarcity * quality * locationMult;
        return Math.max(1, (int) Math.round(finalPrice));
    }
    
    /**
     * Gets the buy price for a resource at a town (if they're selling).
     */
    public int getBuyPrice(Town town, String resourceId) {
        int basePrice = BASE_PRICES.getOrDefault(resourceId, 1);
        double scarcity = getScarcityMultiplier(town, resourceId);
        
        // Buying is more expensive than selling
        double finalPrice = basePrice * scarcity * 1.2;
        return Math.max(1, (int) Math.round(finalPrice));
    }
    
    /**
     * Gets the scarcity multiplier for a resource at a town.
     * Low supply + high demand = high price.
     */
    public double getScarcityMultiplier(Town town, String resourceId) {
        Map<String, Integer> supply = townSupply.get(town);
        Map<String, Double> demand = townDemand.get(town);
        
        if (supply == null || demand == null) {
            return 1.0;
        }
        
        int currentSupply = supply.getOrDefault(resourceId, 50);
        double currentDemand = demand.getOrDefault(resourceId, 1.0);
        
        // Calculate scarcity: low supply + high demand = high multiplier
        // Supply of 0-50 is scarce, 50-100 is normal, 100+ is oversupplied
        double supplyFactor = 1.5 - (currentSupply / 100.0);
        supplyFactor = Math.max(0.5, Math.min(2.0, supplyFactor));
        
        double multiplier = supplyFactor * currentDemand;
        return Math.max(MIN_SCARCITY_MULT, Math.min(MAX_SCARCITY_MULT, multiplier));
    }
    
    // ==================== TRADING ====================
    
    /**
     * Sells items to a town. Returns gold earned.
     */
    public int sellToTown(Town town, ResourceItem item, int quantity) {
        int unitPrice = getSellPrice(town, item);
        int actualQuantity = Math.min(quantity, item.getQuantity());
        int totalGold = unitPrice * actualQuantity;
        
        // Update town supply
        Map<String, Integer> supply = townSupply.get(town);
        if (supply != null) {
            int currentSupply = supply.getOrDefault(item.getId(), 0);
            supply.put(item.getId(), currentSupply + actualQuantity);
        }
        
        // Remove from item stack
        item.removeQuantity(actualQuantity);
        
        return totalGold;
    }
    
    /**
     * Checks if town has a resource to sell.
     */
    public boolean canBuyFromTown(Town town, String resourceId, int quantity) {
        Map<String, Integer> supply = townSupply.get(town);
        if (supply == null) return false;
        return supply.getOrDefault(resourceId, 0) >= quantity;
    }
    
    /**
     * Buys items from a town. Returns null if can't afford or unavailable.
     */
    public ResourceItem buyFromTown(Town town, String resourceId, int quantity, int playerGold) {
        if (!canBuyFromTown(town, resourceId, quantity)) {
            return null;
        }
        
        int unitPrice = getBuyPrice(town, resourceId);
        int totalCost = unitPrice * quantity;
        
        if (playerGold < totalCost) {
            return null;
        }
        
        // Update town supply
        Map<String, Integer> supply = townSupply.get(town);
        int currentSupply = supply.get(resourceId);
        supply.put(resourceId, currentSupply - quantity);
        
        // Create and return the item (based on resource ID)
        if (resourceId.startsWith("grain_")) {
            String grainName = resourceId.substring(6).toUpperCase();
            try {
                GrainType grainType = GrainType.valueOf(grainName);
                ResourceItem.Quality quality = ResourceItem.Quality.random(random);
                return new ResourceItem(grainType, quality, quantity);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        // Generic item for non-grain resources
        ResourceItem item = new ResourceItem(resourceId, resourceId, 
            ResourceItem.Category.GRAIN, BASE_PRICES.getOrDefault(resourceId, 1));
        item.setQuantity(quantity);
        return item;
    }
    
    // ==================== INFO ====================
    
    /**
     * Gets a price summary for a resource at a town.
     */
    public String getPriceSummary(Town town, String resourceId) {
        int basePrice = BASE_PRICES.getOrDefault(resourceId, 1);
        double scarcity = getScarcityMultiplier(town, resourceId);
        int buyPrice = getBuyPrice(town, resourceId);
        
        Map<String, Integer> supply = townSupply.get(town);
        int currentSupply = supply != null ? supply.getOrDefault(resourceId, 0) : 0;
        
        String trend;
        if (scarcity > 1.3) trend = "(High demand!)";
        else if (scarcity < 0.7) trend = "(Oversupplied)";
        else trend = "(Normal)";
        
        return String.format("%s - Buy: %d gold %s (Stock: %d)", 
            resourceId, buyPrice, trend, currentSupply);
    }
    
    /**
     * Gets the supply level at a town.
     */
    public int getSupplyLevel(Town town, String resourceId) {
        Map<String, Integer> supply = townSupply.get(town);
        return supply != null ? supply.getOrDefault(resourceId, 0) : 0;
    }
    
    /**
     * Gets a list of available items at a town (items with supply > 0).
     */
    public List<String> getAvailableItems(Town town) {
        List<String> available = new ArrayList<>();
        Map<String, Integer> supply = townSupply.get(town);
        if (supply != null) {
            for (Map.Entry<String, Integer> entry : supply.entrySet()) {
                if (entry.getValue() > 0) {
                    available.add(entry.getKey());
                }
            }
        }
        // If no items in supply, show base items
        if (available.isEmpty()) {
            available.addAll(BASE_PRICES.keySet());
        }
        return available;
    }
    
    /**
     * Gets the quantity of an item available at a town.
     */
    public int getItemQuantity(Town town, String itemId) {
        Map<String, Integer> supply = townSupply.get(town);
        if (supply != null) {
            return supply.getOrDefault(itemId, 0);
        }
        // Return default stock if town not registered
        return 25 + (int)(Math.random() * 25);
    }
}
