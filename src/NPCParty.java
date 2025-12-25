import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a group of NPCs traveling together.
 * Parties share a common destination, inventory, and can be interacted with as a group.
 */
public class NPCParty {
    
    private List<NPC> members;
    private String partyName;
    private Map<ResourceType, Integer> sharedInventory;
    
    // Movement synchronization
    private double worldX;
    private double worldY;
    private double targetX;
    private double targetY;
    private Town destination;
    
    // Party behavior
    private PartyTask currentTask;
    private double taskTimer = 0;
    
    public enum PartyTask {
        IDLE("Resting"),
        GATHERING("Gathering resources"),
        TRADING("Trading with town"),
        TRAVELING("Traveling between towns");
        
        private final String description;
        PartyTask(String desc) { this.description = desc; }
        public String getDescription() { return description; }
    }
    
    /**
     * Creates a new party with the given members.
     */
    public NPCParty(List<NPC> members) {
        this.members = new ArrayList<>(members);
        this.sharedInventory = new HashMap<>();
        this.currentTask = PartyTask.IDLE;
        
        // Generate party name based on leader
        if (!members.isEmpty()) {
            NPC leader = members.get(0);
            partyName = leader.getName() + "'s Party";
            worldX = leader.getWorldX();
            worldY = leader.getWorldY();
            targetX = worldX;
            targetY = worldY;
        } else {
            partyName = "Wandering Party";
        }
    }
    
    /**
     * Creates a party from a single NPC (converts solo traveler to party).
     */
    public static NPCParty fromSingleNPC(NPC npc) {
        List<NPC> members = new ArrayList<>();
        members.add(npc);
        return new NPCParty(members);
    }
    
    /**
     * Adds an NPC to the party.
     */
    public void addMember(NPC npc) {
        if (!members.contains(npc)) {
            members.add(npc);
            // Sync position with party
            npc.setPosition(worldX, worldY);
        }
    }
    
    /**
     * Removes an NPC from the party.
     */
    public void removeMember(NPC npc) {
        members.remove(npc);
        // If party is empty, it should be disbanded by manager
    }
    
    /**
     * Gets the party leader (first member).
     */
    public NPC getLeader() {
        return members.isEmpty() ? null : members.get(0);
    }
    
    /**
     * Gets all party members.
     */
    public List<NPC> getMembers() {
        return new ArrayList<>(members);
    }
    
    /**
     * Gets party size.
     */
    public int getSize() {
        return members.size();
    }
    
    /**
     * Gets party name.
     */
    public String getPartyName() {
        return partyName;
    }
    
    /**
     * Sets the party's name.
     */
    public void setPartyName(String name) {
        this.partyName = name;
    }
    
    /**
     * Gets the party's current world position (center of formation).
     */
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    
    /**
     * Sets the party's position and synchronizes all members.
     */
    public void setPosition(double x, double y) {
        this.worldX = x;
        this.worldY = y;
        
        // Arrange members in a small cluster
        for (int i = 0; i < members.size(); i++) {
            NPC npc = members.get(i);
            
            // Offset members slightly to show they're grouped
            double offsetX = 0;
            double offsetY = 0;
            
            if (members.size() > 1) {
                // Arrange in a circle or small formation
                double angle = (i * 2.0 * Math.PI) / members.size();
                double radius = 0.3; // Very small cluster
                offsetX = Math.cos(angle) * radius;
                offsetY = Math.sin(angle) * radius;
            }
            
            npc.setPosition(worldX + offsetX, worldY + offsetY);
        }
    }
    
    /**
     * Moves the party toward its target.
     */
    public void moveTowards(double destX, double destY, double speed, double deltaTime) {
        double dx = destX - worldX;
        double dy = destY - worldY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0.1) {
            double moveDistance = speed * deltaTime;
            if (moveDistance > distance) {
                moveDistance = distance;
            }
            
            worldX += (dx / distance) * moveDistance;
            worldY += (dy / distance) * moveDistance;
            
            // Update all member positions
            setPosition(worldX, worldY);
        }
    }
    
    /**
     * Sets the party's destination.
     */
    public void setDestination(Town town) {
        this.destination = town;
        if (town != null) {
            this.targetX = town.getGridX();
            this.targetY = town.getGridY();
            this.currentTask = PartyTask.TRAVELING;
        }
    }
    
    /**
     * Gets the party's current destination.
     */
    public Town getDestination() {
        return destination;
    }
    
    /**
     * Updates party logic.
     */
    public void update(double deltaTime, List<Town> allTowns) {
        taskTimer += deltaTime;
        
        switch (currentTask) {
            case IDLE:
                // Wait for a bit, then pick a new task
                if (taskTimer > 5) {
                    pickNewTask(allTowns);
                    taskTimer = 0;
                }
                break;
                
            case TRAVELING:
                // Move toward destination
                if (destination != null) {
                    moveTowards(destination.getGridX(), destination.getGridY(), 0.5, deltaTime);
                    
                    // Check if arrived
                    double dx = destination.getGridX() - worldX;
                    double dy = destination.getGridY() - worldY;
                    if (Math.sqrt(dx * dx + dy * dy) < 0.5) {
                        // Arrived!
                        currentTask = PartyTask.TRADING;
                        taskTimer = 0;
                    }
                }
                break;
                
            case GATHERING:
                // Gather resources over time
                if (taskTimer > 3) {
                    // Add a random resource
                    ResourceType[] types = ResourceType.values();
                    ResourceType type = types[(int)(Math.random() * types.length)];
                    addResource(type, 1 + (int)(Math.random() * 3));
                    
                    // Switch to trading after gathering
                    currentTask = PartyTask.IDLE;
                    taskTimer = 0;
                }
                break;
                
            case TRADING:
                // Sell resources at town
                if (taskTimer > 2) {
                    // Remove only resources that the destination WOULD buy here;
                    // keep resources that match destination's production so parties travel instead of selling locally.
                    if (destination != null) {
                        Map<ResourceType, Integer> remaining = new java.util.HashMap<>();
                        for (Map.Entry<ResourceType, Integer> e : sharedInventory.entrySet()) {
                            ResourceType type = e.getKey();
                            int amt = e.getValue();
                            if (destination.producesResourceType(type)) {
                                // Keep this resource, do not sell here
                                remaining.put(type, remaining.getOrDefault(type, 0) + amt);
                                System.out.println("Party: kept " + amt + " " + type + " at " + destination.getName() + " (local product)");
                            } else {
                                // Selling at destination - could add economy effects here later
                                System.out.println("Party: sold " + amt + " " + type + " at " + destination.getName());
                            }
                        }
                        sharedInventory.clear();
                        sharedInventory.putAll(remaining);
                    } else {
                        // No destination: clear inventory as before
                        sharedInventory.clear();
                    }
                    
                    // Pick new destination
                    pickNewTask(allTowns);
                    taskTimer = 0;
                }
                break;
        }
    }
    
    /**
     * Picks a new task for the party based on current state.
     */
    private void pickNewTask(List<Town> allTowns) {
        if (allTowns == null || allTowns.isEmpty()) {
            currentTask = PartyTask.IDLE;
            return;
        }
        
        double roll = Math.random();
        
        if (roll < 0.4) {
            // Travel to a new town
            Town newDest = allTowns.get((int)(Math.random() * allTowns.size()));
            setDestination(newDest);
        } else if (roll < 0.7) {
            // Gather resources
            currentTask = PartyTask.GATHERING;
        } else {
            // Rest/idle
            currentTask = PartyTask.IDLE;
        }
    }
    
    /**
     * Adds a resource to the shared inventory.
     */
    public void addResource(ResourceType type, int amount) {
        sharedInventory.put(type, sharedInventory.getOrDefault(type, 0) + amount);
    }
    
    /**
     * Removes a resource from the shared inventory.
     */
    public boolean removeResource(ResourceType type, int amount) {
        int current = sharedInventory.getOrDefault(type, 0);
        if (current >= amount) {
            sharedInventory.put(type, current - amount);
            return true;
        }
        return false;
    }
    
    /**
     * Gets the amount of a resource in the shared inventory.
     */
    public int getResourceAmount(ResourceType type) {
        return sharedInventory.getOrDefault(type, 0);
    }
    
    /**
     * Gets the shared inventory.
     */
    public Map<ResourceType, Integer> getSharedInventory() {
        return new HashMap<>(sharedInventory);
    }
    
    /**
     * Gets the party's current task.
     */
    public PartyTask getCurrentTask() {
        return currentTask;
    }
    
    /**
     * Checks if this party contains the given NPC.
     */
    public boolean containsNPC(NPC npc) {
        return members.contains(npc);
    }
    
    /**
     * Gets a description of the party's current activity.
     */
    public String getActivityDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(partyName).append(" (").append(members.size()).append(" members)\n");
        sb.append("Activity: ").append(currentTask.getDescription()).append("\n");
        
        if (destination != null) {
            sb.append("Destination: ").append(destination.getName()).append("\n");
        }
        
        if (!sharedInventory.isEmpty()) {
            sb.append("Carrying: ");
            boolean first = true;
            for (Map.Entry<ResourceType, Integer> entry : sharedInventory.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getValue()).append(" ").append(entry.getKey().name());
                first = false;
            }
            sb.append("\n");
        }
        
        sb.append("\nMembers:\n");
        for (NPC npc : members) {
            sb.append("- ").append(npc.getName()).append(" (").append(npc.getType().getName()).append(")\n");
        }
        
        return sb.toString();
    }
}
