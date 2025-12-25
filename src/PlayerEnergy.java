/**
 * Energy system for the player character.
 * 
 * Energy is consumed by actions:
 * - Farming: 10 energy per harvest
 * - Mining: 15 energy per harvest
 * - Walking: 0.1 energy per tile (negligible)
 * - Running: 0.3 energy per tile
 * 
 * Energy is restored by:
 * - Resting at village: 1 energy per game minute (slow, free)
 * - Resting at city inn: Instant full restore (costs gold)
 * - Eating food: Variable energy restore
 * - Sleeping: Full restore over 8 hours
 */
public class PlayerEnergy {
    
    // Energy constants
    public static final int MAX_ENERGY = 100;
    public static final int LOW_ENERGY_THRESHOLD = 20;
    public static final int CRITICAL_ENERGY_THRESHOLD = 5;
    
    // Energy costs for actions
    public static final int FARMING_COST = 10;
    public static final int MINING_COST = 15;
    public static final int WOODCUTTING_COST = 12;
    public static final int FISHING_COST = 8;
    public static final double WALKING_COST_PER_TILE = 0.05;
    public static final double RUNNING_COST_PER_TILE = 0.15;
    public static final double SWIMMING_COST_PER_TILE = 3.0;  // Deep water swimming cost
    public static final double WADING_COST_PER_TILE = 0.0;    // Shallow water (wading) is free
    
    // Recovery rates
    public static final double VILLAGE_REST_PER_MINUTE = 1.0;  // 1 energy per game minute
    public static final double PASSIVE_RECOVERY_PER_MINUTE = 0.1; // Slow passive regen
    public static final int INN_REST_COST_GOLD = 2; // Cost to rest at city inn (reduced from 5)
    
    // Current state
    private double currentEnergy;
    private double maxEnergy;
    private boolean isResting = false;
    private RestLocation restLocation = null;
    
    // Exhaustion state
    private boolean isExhausted = false; // True when energy hits 0
    private double exhaustionRecoveryBonus = 0; // Bonus recovery when exhausted
    
    /**
     * Rest location types with different recovery properties.
     */
    public enum RestLocation {
        NONE("None", 0, 0, false),
        VILLAGE_OUTDOORS("Village Square", VILLAGE_REST_PER_MINUTE * 0.5, 0, true),
        VILLAGE_INN("Village Inn", VILLAGE_REST_PER_MINUTE, 1, true), // Reduced from 2
        CITY_INN("City Inn", MAX_ENERGY, INN_REST_COST_GOLD, false), // Instant restore
        CAMPFIRE("Campfire", VILLAGE_REST_PER_MINUTE * 0.3, 0, true),
        HOME("Home", VILLAGE_REST_PER_MINUTE * 1.5, 0, true);
        
        private final String displayName;
        private final double recoveryPerMinute;
        private final int cost;
        private final boolean takesTime;
        
        RestLocation(String displayName, double recoveryPerMinute, int cost, boolean takesTime) {
            this.displayName = displayName;
            this.recoveryPerMinute = recoveryPerMinute;
            this.cost = cost;
            this.takesTime = takesTime;
        }
        
        public String getDisplayName() { return displayName; }
        public double getRecoveryPerMinute() { return recoveryPerMinute; }
        public int getCost() { return cost; }
        public boolean takesTime() { return takesTime; }
    }
    
    /**
     * Energy status levels for UI display.
     */
    public enum EnergyStatus {
        FULL("Full", "#40c040"),
        HIGH("Good", "#80c040"),
        MEDIUM("Moderate", "#c0c040"),
        LOW("Low", "#c08040"),
        CRITICAL("Critical", "#c04040"),
        EXHAUSTED("Exhausted", "#800000");
        
        private final String displayName;
        private final String color;
        
        EnergyStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    /**
     * Creates a new energy system with full energy.
     */
    public PlayerEnergy() {
        this.maxEnergy = MAX_ENERGY;
        this.currentEnergy = MAX_ENERGY;
    }
    
    /**
     * Creates a new energy system with custom max energy.
     */
    public PlayerEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
        this.currentEnergy = maxEnergy;
    }
    
    // ==================== ENERGY CONSUMPTION ====================
    
    /**
     * Attempts to consume energy for an action.
     * Returns true if successful, false if not enough energy.
     */
    public boolean consumeEnergy(double amount) {
        if (currentEnergy < amount) {
            return false;
        }
        
        currentEnergy -= amount;
        
        // Check for exhaustion
        if (currentEnergy <= 0) {
            currentEnergy = 0;
            isExhausted = true;
            exhaustionRecoveryBonus = 0.5; // 50% faster recovery when recovering from exhaustion
        }
        
        return true;
    }
    
    /**
     * Checks if player has enough energy for an action.
     */
    public boolean hasEnergy(double amount) {
        return currentEnergy >= amount;
    }
    
    /**
     * Checks if player can perform farming.
     */
    public boolean canFarm() {
        return hasEnergy(FARMING_COST) && !isExhausted;
    }
    
    /**
     * Checks if player can perform mining.
     */
    public boolean canMine() {
        return hasEnergy(MINING_COST) && !isExhausted;
    }
    
    /**
     * Consumes energy for walking a certain number of tiles.
     */
    public void consumeWalkingEnergy(double tiles) {
        consumeEnergy(tiles * WALKING_COST_PER_TILE);
    }
    
    /**
     * Consumes energy for running a certain number of tiles.
     */
    public void consumeRunningEnergy(double tiles) {
        consumeEnergy(tiles * RUNNING_COST_PER_TILE);
    }
    
    /**
     * Consumes energy for swimming in deep water.
     * Swimming costs 3 energy per tile - very tiring!
     */
    public void consumeSwimmingEnergy(double tiles) {
        consumeEnergy(tiles * SWIMMING_COST_PER_TILE);
    }
    
    /**
     * Consumes energy for wading in shallow water.
     * Wading is free - no energy cost.
     */
    public void consumeWadingEnergy(double tiles) {
        consumeEnergy(tiles * WADING_COST_PER_TILE);
    }
    
    // ==================== ENERGY RECOVERY ====================
    
    /**
     * Recovers energy over time (called each game minute).
     */
    public void updateRecovery(int gameMinutesElapsed) {
        if (isResting && restLocation != null) {
            if (restLocation.takesTime()) {
                // Gradual recovery
                double recovery = restLocation.getRecoveryPerMinute() * gameMinutesElapsed;
                recovery *= (1.0 + exhaustionRecoveryBonus);
                addEnergy(recovery);
            }
        } else {
            // Passive recovery (very slow)
            double recovery = PASSIVE_RECOVERY_PER_MINUTE * gameMinutesElapsed;
            addEnergy(recovery);
        }
        
        // Clear exhaustion bonus when fully recovered
        if (currentEnergy >= maxEnergy * 0.5) {
            exhaustionRecoveryBonus = 0;
        }
        
        // Clear exhausted state when above critical
        if (currentEnergy > CRITICAL_ENERGY_THRESHOLD) {
            isExhausted = false;
        }
    }
    
    /**
     * Adds energy (for food, potions, etc.)
     */
    public void addEnergy(double amount) {
        currentEnergy = Math.min(maxEnergy, currentEnergy + amount);
    }
    
    /**
     * Fully restores energy (for city inn rest).
     */
    public void fullyRestore() {
        currentEnergy = maxEnergy;
        isExhausted = false;
        exhaustionRecoveryBonus = 0;
    }
    
    /**
     * Starts resting at a location.
     */
    public void startResting(RestLocation location) {
        this.isResting = true;
        this.restLocation = location;
        
        // If instant restore (city inn)
        if (!location.takesTime()) {
            fullyRestore();
            stopResting();
        }
    }
    
    /**
     * Stops resting.
     */
    public void stopResting() {
        this.isResting = false;
        this.restLocation = null;
    }
    
    /**
     * Calculates time needed to fully restore energy at current location.
     * Returns minutes needed, or -1 if not resting or instant restore.
     */
    public int getTimeToFullRestore() {
        if (!isResting || restLocation == null || !restLocation.takesTime()) {
            return -1;
        }
        
        double needed = maxEnergy - currentEnergy;
        double ratePerMin = restLocation.getRecoveryPerMinute() * (1.0 + exhaustionRecoveryBonus);
        
        if (ratePerMin <= 0) return -1;
        
        return (int) Math.ceil(needed / ratePerMin);
    }
    
    // ==================== STATUS QUERIES ====================
    
    /**
     * Gets the current energy status.
     */
    public EnergyStatus getStatus() {
        if (isExhausted) return EnergyStatus.EXHAUSTED;
        
        double percent = getEnergyPercent();
        if (percent >= 1.0) return EnergyStatus.FULL;
        if (percent >= 0.7) return EnergyStatus.HIGH;
        if (percent >= 0.4) return EnergyStatus.MEDIUM;
        if (percent >= 0.2) return EnergyStatus.LOW;
        return EnergyStatus.CRITICAL;
    }
    
    /**
     * Gets energy as a percentage (0.0 to 1.0).
     */
    public double getEnergyPercent() {
        return currentEnergy / maxEnergy;
    }
    
    /**
     * Gets formatted energy string.
     */
    public String getFormattedEnergy() {
        return String.format("%.0f/%.0f", currentEnergy, maxEnergy);
    }
    
    /**
     * Checks if energy is low (should warn player).
     */
    public boolean isLow() {
        return currentEnergy <= LOW_ENERGY_THRESHOLD;
    }
    
    /**
     * Checks if energy is critical.
     */
    public boolean isCritical() {
        return currentEnergy <= CRITICAL_ENERGY_THRESHOLD;
    }
    
    // ==================== GETTERS/SETTERS ====================
    
    public double getCurrentEnergy() { return currentEnergy; }
    public double getMaxEnergy() { return maxEnergy; }
    public boolean isResting() { return isResting; }
    public RestLocation getRestLocation() { return restLocation; }
    public boolean isExhausted() { return isExhausted; }
    
    public void setMaxEnergy(double max) {
        this.maxEnergy = max;
        if (currentEnergy > maxEnergy) {
            currentEnergy = maxEnergy;
        }
    }
    
    /**
     * Sets current energy directly (for loading saved games).
     */
    public void setCurrentEnergy(double energy) {
        this.currentEnergy = Math.max(0, Math.min(maxEnergy, energy));
        this.isExhausted = (currentEnergy <= 0);
    }
}
