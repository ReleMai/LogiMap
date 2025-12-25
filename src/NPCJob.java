import java.util.Random;

/**
 * Represents a job/task that an NPC can perform.
 * Jobs include gathering resources, delivering goods, working at a location, etc.
 */
public class NPCJob {
    
    public enum JobType {
        GATHER_WOOD("Gathering Wood", "🪵", 8.0, 3),
        GATHER_STONE("Gathering Stone", "🪨", 10.0, 2),
        GATHER_ORE("Mining Ore", "⛏", 12.0, 2),
        GATHER_CROPS("Harvesting Crops", "🌾", 6.0, 4),
        GATHER_FISH("Fishing", "🐟", 7.0, 3),
        TEND_LIVESTOCK("Tending Livestock", "🐄", 5.0, 0),
        DELIVER_GOODS("Delivering Goods", "📦", 0, 0),
        WORK_SHOP("Working at Shop", "🏪", 15.0, 0),
        PATROL("Patrolling", "🛡", 0, 0),
        IDLE_WORK("Light Work", "✋", 3.0, 0);
        
        private final String displayName;
        private final String icon;
        private final double baseDuration; // In seconds
        private final int baseYield;       // Resources gathered
        
        JobType(String displayName, String icon, double baseDuration, int baseYield) {
            this.displayName = displayName;
            this.icon = icon;
            this.baseDuration = baseDuration;
            this.baseYield = baseYield;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public double getBaseDuration() { return baseDuration; }
        public int getBaseYield() { return baseYield; }
    }
    
    public enum JobState {
        TRAVELING_TO_SITE,  // Walking to resource location
        WORKING,            // Performing the job
        RETURNING,          // Going back to town
        DELIVERING,         // At town, delivering goods
        COMPLETE            // Job finished
    }
    
    private JobType type;
    private JobState state;
    private double progress;        // 0-1 for current state
    private double totalTime;       // Total time this job takes
    
    // Locations
    private double siteX, siteY;           // Resource site location
    private double deliveryX, deliveryY;   // Town/delivery location
    private Town targetTown;               // Town to deliver to
    
    // Results
    private int resourcesGathered;
    private String resourceType;
    
    private Random random = new Random();
    
    /**
     * Creates a new job for an NPC.
     */
    public NPCJob(JobType type, double siteX, double siteY, Town deliveryTown) {
        this.type = type;
        this.state = JobState.TRAVELING_TO_SITE;
        this.progress = 0;
        this.siteX = siteX;
        this.siteY = siteY;
        this.targetTown = deliveryTown;
        
        if (deliveryTown != null) {
            this.deliveryX = deliveryTown.getGridX() + deliveryTown.getSize() / 2.0;
            this.deliveryY = deliveryTown.getGridY() + deliveryTown.getSize() / 2.0;
        }
        
        // Calculate total time with some variation
        this.totalTime = type.getBaseDuration() * (0.8 + random.nextDouble() * 0.4);
        
        // Set resource type based on job
        this.resourceType = getResourceTypeForJob(type);
    }
    
    private String getResourceTypeForJob(JobType type) {
        return switch (type) {
            case GATHER_WOOD -> "Wood";
            case GATHER_STONE -> "Stone";
            case GATHER_ORE -> "Ore";
            case GATHER_CROPS -> "Grain";
            case GATHER_FISH -> "Fish";
            case TEND_LIVESTOCK -> "Livestock";
            default -> null;
        };
    }
    
    /**
     * Updates job progress.
     * @return true if job state changed
     */
    public boolean update(double deltaTime) {
        if (state == JobState.COMPLETE) return false;
        
        boolean stateChanged = false;
        
        switch (state) {
            case WORKING:
                progress += deltaTime / totalTime;
                if (progress >= 1.0) {
                    // Finished working, calculate yield
                    resourcesGathered = type.getBaseYield() + random.nextInt(2);
                    state = JobState.RETURNING;
                    progress = 0;
                    stateChanged = true;
                }
                break;
                
            case DELIVERING:
                progress += deltaTime / 2.0; // 2 seconds to deliver
                if (progress >= 1.0) {
                    state = JobState.COMPLETE;
                    stateChanged = true;
                }
                break;
                
            default:
                // TRAVELING_TO_SITE and RETURNING are handled by NPC movement
                break;
        }
        
        return stateChanged;
    }
    
    /**
     * Called when NPC arrives at destination.
     */
    public void arrivedAtDestination() {
        switch (state) {
            case TRAVELING_TO_SITE:
                state = JobState.WORKING;
                progress = 0;
                break;
            case RETURNING:
                state = JobState.DELIVERING;
                progress = 0;
                break;
            default:
                break;
        }
    }
    
    /**
     * Gets the current destination based on job state.
     */
    public double[] getCurrentDestination() {
        return switch (state) {
            case TRAVELING_TO_SITE, WORKING -> new double[]{siteX, siteY};
            case RETURNING, DELIVERING -> new double[]{deliveryX, deliveryY};
            default -> null;
        };
    }
    
    /**
     * Checks if NPC should be moving.
     */
    public boolean requiresMovement() {
        return state == JobState.TRAVELING_TO_SITE || state == JobState.RETURNING;
    }
    
    /**
     * Checks if NPC is actively working (not moving).
     */
    public boolean isWorking() {
        return state == JobState.WORKING || state == JobState.DELIVERING;
    }
    
    /**
     * Gets the NPC action that corresponds to current job state.
     */
    public NPC.NPCAction getNPCAction() {
        return switch (state) {
            case TRAVELING_TO_SITE -> NPC.NPCAction.WALKING;
            case WORKING -> NPC.NPCAction.GATHERING;
            case RETURNING -> NPC.NPCAction.WALKING;
            case DELIVERING -> NPC.NPCAction.DELIVERING;
            case COMPLETE -> NPC.NPCAction.IDLE;
        };
    }
    
    // Getters
    public JobType getType() { return type; }
    public JobState getState() { return state; }
    public double getProgress() { return progress; }
    public double getTotalTime() { return totalTime; }
    public double getSiteX() { return siteX; }
    public double getSiteY() { return siteY; }
    public Town getTargetTown() { return targetTown; }
    public int getResourcesGathered() { return resourcesGathered; }
    public String getResourceType() { return resourceType; }
    public boolean isComplete() { return state == JobState.COMPLETE; }
    
    /**
     * Gets a description of current job status.
     */
    public String getStatusDescription() {
        return switch (state) {
            case TRAVELING_TO_SITE -> "Heading to " + type.getDisplayName().toLowerCase() + " site";
            case WORKING -> type.getDisplayName() + " (" + (int)(progress * 100) + "%)";
            case RETURNING -> "Returning to " + (targetTown != null ? targetTown.getName() : "town");
            case DELIVERING -> "Delivering " + resourcesGathered + " " + resourceType;
            case COMPLETE -> "Job complete";
        };
    }
}
