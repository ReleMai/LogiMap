import java.util.*;

/**
 * Manages relationships between the player and NPCs they've interacted with.
 * Tracks interaction history and relationship levels.
 */
public class RelationshipManager {
    
    // NPC ID -> Relationship data
    private Map<String, NPCRelationship> relationships;
    private List<NPCRelationship> recentInteractions;
    private static final int MAX_RECENT = 20;
    
    public enum RelationshipLevel {
        STRANGER(0, "Stranger", "#808080"),
        ACQUAINTANCE(10, "Acquaintance", "#a0a0a0"),
        FRIENDLY(25, "Friendly", "#90EE90"),
        FRIEND(50, "Friend", "#4a9eff"),
        CLOSE_FRIEND(75, "Close Friend", "#9370DB"),
        TRUSTED(100, "Trusted", "#FFD700");
        
        private final int threshold;
        private final String displayName;
        private final String color;
        
        RelationshipLevel(int threshold, String displayName, String color) {
            this.threshold = threshold;
            this.displayName = displayName;
            this.color = color;
        }
        
        public int getThreshold() { return threshold; }
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
        
        public static RelationshipLevel fromValue(int value) {
            RelationshipLevel result = STRANGER;
            for (RelationshipLevel level : values()) {
                if (value >= level.threshold) {
                    result = level;
                }
            }
            return result;
        }
    }
    
    /**
     * Data class for storing relationship info with an NPC.
     */
    public static class NPCRelationship {
        private String npcId;           // Unique identifier
        private String npcName;
        private NPC.NPCType npcType;
        private String homeTownName;
        private int relationshipValue;
        private int totalInteractions;
        private long firstMet;
        private long lastInteraction;
        
        // Transient reference to actual NPC (not saved)
        private transient NPC npcRef;
        
        public NPCRelationship(NPC npc) {
            this.npcId = generateNPCId(npc);
            this.npcName = npc.getName();
            this.npcType = npc.getType();
            this.homeTownName = npc.getHomeTown() != null ? npc.getHomeTown().getName() : "Unknown";
            this.relationshipValue = 0;
            this.totalInteractions = 0;
            this.firstMet = System.currentTimeMillis();
            this.lastInteraction = this.firstMet;
            this.npcRef = npc;
        }
        
        private static String generateNPCId(NPC npc) {
            // Generate unique ID from name + type + home town
            String townName = npc.getHomeTown() != null ? npc.getHomeTown().getName() : "wanderer";
            return npc.getName() + "_" + npc.getType().name() + "_" + townName;
        }
        
        public void recordInteraction() {
            totalInteractions++;
            lastInteraction = System.currentTimeMillis();
            // Small relationship gain just for talking
            relationshipValue = Math.min(150, relationshipValue + 1);
        }
        
        public void modifyRelationship(int amount) {
            relationshipValue = Math.max(0, Math.min(150, relationshipValue + amount));
        }
        
        public RelationshipLevel getLevel() {
            return RelationshipLevel.fromValue(relationshipValue);
        }
        
        // Getters
        public String getNpcId() { return npcId; }
        public String getNpcName() { return npcName; }
        public NPC.NPCType getNpcType() { return npcType; }
        public String getHomeTownName() { return homeTownName; }
        public int getRelationshipValue() { return relationshipValue; }
        public int getTotalInteractions() { return totalInteractions; }
        public long getFirstMet() { return firstMet; }
        public long getLastInteraction() { return lastInteraction; }
        public NPC getNpcRef() { return npcRef; }
        public void setNpcRef(NPC npc) { this.npcRef = npc; }
    }
    
    public RelationshipManager() {
        relationships = new HashMap<>();
        recentInteractions = new ArrayList<>();
    }
    
    /**
     * Records an interaction with an NPC.
     */
    public void recordInteraction(NPC npc) {
        if (npc == null) return;
        
        String npcId = NPCRelationship.generateNPCId(npc);
        
        NPCRelationship rel = relationships.get(npcId);
        if (rel == null) {
            // First meeting
            rel = new NPCRelationship(npc);
            relationships.put(npcId, rel);
        } else {
            rel.setNpcRef(npc);
        }
        
        rel.recordInteraction();
        
        // Update recent interactions list
        recentInteractions.remove(rel);
        recentInteractions.add(0, rel);
        if (recentInteractions.size() > MAX_RECENT) {
            recentInteractions.remove(recentInteractions.size() - 1);
        }
    }
    
    /**
     * Modifies relationship with an NPC.
     */
    public void modifyRelationship(NPC npc, int amount) {
        if (npc == null) return;
        
        String npcId = NPCRelationship.generateNPCId(npc);
        NPCRelationship rel = relationships.get(npcId);
        
        if (rel != null) {
            rel.modifyRelationship(amount);
        }
    }
    
    /**
     * Gets relationship level with an NPC.
     */
    public RelationshipLevel getRelationshipLevel(NPC npc) {
        if (npc == null) return RelationshipLevel.STRANGER;
        
        String npcId = NPCRelationship.generateNPCId(npc);
        NPCRelationship rel = relationships.get(npcId);
        
        return rel != null ? rel.getLevel() : RelationshipLevel.STRANGER;
    }
    
    /**
     * Gets raw relationship value.
     */
    public int getRelationshipValue(NPC npc) {
        if (npc == null) return 0;
        
        String npcId = NPCRelationship.generateNPCId(npc);
        NPCRelationship rel = relationships.get(npcId);
        
        return rel != null ? rel.getRelationshipValue() : 0;
    }
    
    /**
     * Gets all known NPCs (have interacted with at least once).
     */
    public List<NPCRelationship> getAllKnownNPCs() {
        return new ArrayList<>(relationships.values());
    }
    
    /**
     * Gets recent interactions in order.
     */
    public List<NPCRelationship> getRecentInteractions() {
        return new ArrayList<>(recentInteractions);
    }
    
    /**
     * Gets total number of unique NPCs met.
     */
    public int getTotalNPCsMet() {
        return relationships.size();
    }
    
    /**
     * Gets NPCs filtered by relationship level.
     */
    public List<NPCRelationship> getNPCsByLevel(RelationshipLevel minLevel) {
        List<NPCRelationship> result = new ArrayList<>();
        for (NPCRelationship rel : relationships.values()) {
            if (rel.getRelationshipValue() >= minLevel.getThreshold()) {
                result.add(rel);
            }
        }
        // Sort by relationship value descending
        result.sort((a, b) -> Integer.compare(b.getRelationshipValue(), a.getRelationshipValue()));
        return result;
    }
    
    /**
     * Gets NPCs from a specific town.
     */
    public List<NPCRelationship> getNPCsFromTown(String townName) {
        List<NPCRelationship> result = new ArrayList<>();
        for (NPCRelationship rel : relationships.values()) {
            if (rel.getHomeTownName().equals(townName)) {
                result.add(rel);
            }
        }
        return result;
    }
    
    /**
     * Checks if player has met this NPC before.
     */
    public boolean hasMetNPC(NPC npc) {
        if (npc == null) return false;
        String npcId = NPCRelationship.generateNPCId(npc);
        return relationships.containsKey(npcId);
    }
    
    /**
     * Gets relationship data for a specific NPC.
     */
    public NPCRelationship getRelationship(NPC npc) {
        if (npc == null) return null;
        String npcId = NPCRelationship.generateNPCId(npc);
        return relationships.get(npcId);
    }
}
