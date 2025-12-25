/**
 * Defines specific roles that NPCs can have within settlements.
 * Roles determine behavior, schedule, and responsibilities.
 */
public enum NPCRole {
    
    // === Village Roles ===
    
    /**
     * Village Elder - Handles village politics and administration.
     * Stays near the village center, speaks with villagers.
     */
    VILLAGE_ELDER("Village Elder", "👴", true, false, 
        new DailySchedule(6, 18), // 6 AM to 6 PM
        "Manages village affairs and represents the village to outsiders."),
    
    /**
     * Goods Transporter - Takes collected goods to cities.
     * Travels between village and assigned city.
     */
    GOODS_TRANSPORTER("Goods Transporter", "📦", false, true,
        new DailySchedule(7, 17), // 7 AM to 5 PM
        "Transports village goods to cities and returns with profits."),
    
    /**
     * Peasant - Collects resources from village nodes.
     * Travels to resource nodes to gather materials.
     */
    PEASANT_WORKER("Peasant", "🌾", false, false,
        new DailySchedule(6, 16), // 6 AM to 4 PM - early start
        "Works the land and gathers resources for the village."),
    
    // === City Roles ===
    
    /**
     * Mayor - Handles city politics and oversees villages.
     * Stays in city hall / castle area.
     */
    MAYOR("Mayor", "🎩", true, false,
        new DailySchedule(8, 20), // 8 AM to 8 PM - longer hours
        "Governs the city and its surrounding villages."),
    
    /**
     * City Guard - Patrols the city perimeter.
     * Walks patrol routes around the city.
     */
    CITY_GUARD("City Guard", "⚔", false, false,
        new DailySchedule(6, 22), // Long shifts - 6 AM to 10 PM
        "Protects the city and maintains order."),
    
    /**
     * City Goods Transporter - Transports crafted goods.
     * Travels between cities and to villages.
     */
    CITY_TRANSPORTER("Goods Transporter", "🛒", false, true,
        new DailySchedule(7, 18), // 7 AM to 6 PM
        "Transports crafted goods to trade with other settlements."),
    
    /**
     * Tax Collector - Collects taxes from villages.
     * Visits assigned villages to collect daily earnings.
     */
    TAX_COLLECTOR("Tax Collector", "💰", false, true,
        new DailySchedule(8, 16), // 8 AM to 4 PM
        "Collects taxes from villages under the city's control."),
    
    // === Universal Roles ===
    
    /**
     * Wanderer - Default NPC with no specific role.
     * Wanders randomly within settlement.
     */
    WANDERER("Wanderer", "👤", false, false,
        new DailySchedule(8, 20),
        "A resident going about their daily life.");
    
    private final String displayName;
    private final String icon;
    private final boolean isLeader; // Stays near center, doesn't do physical work
    private final boolean travels;  // Travels outside settlement
    private final DailySchedule schedule;
    private final String description;
    
    NPCRole(String displayName, String icon, boolean isLeader, boolean travels,
            DailySchedule schedule, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.isLeader = isLeader;
        this.travels = travels;
        this.schedule = schedule;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public boolean isLeader() { return isLeader; }
    public boolean doesTravel() { return travels; }
    public DailySchedule getSchedule() { return schedule; }
    public String getDescription() { return description; }
    
    /**
     * Daily schedule defining work hours.
     */
    public static class DailySchedule {
        private final int workStartHour; // 0-23
        private final int workEndHour;   // 0-23
        
        public DailySchedule(int startHour, int endHour) {
            this.workStartHour = startHour;
            this.workEndHour = endHour;
        }
        
        public int getWorkStartHour() { return workStartHour; }
        public int getWorkEndHour() { return workEndHour; }
        
        /**
         * Check if NPC should be working at the given hour.
         */
        public boolean isWorkingHour(int hour) {
            if (workStartHour <= workEndHour) {
                return hour >= workStartHour && hour < workEndHour;
            } else {
                // Handles overnight shifts (e.g., 22 to 6)
                return hour >= workStartHour || hour < workEndHour;
            }
        }
        
        /**
         * Check if NPC should be heading home (last hour of work).
         */
        public boolean isGoingHomeTime(int hour) {
            int lastWorkHour = workEndHour - 1;
            if (lastWorkHour < 0) lastWorkHour = 23;
            return hour == lastWorkHour;
        }
        
        /**
         * Check if NPC should be leaving home (first hour of work).
         */
        public boolean isLeavingHomeTime(int hour) {
            return hour == workStartHour;
        }
    }
    
    /**
     * Gets the NPC type most suitable for this role.
     */
    public NPC.NPCType getSuitableNPCType() {
        return switch (this) {
            case VILLAGE_ELDER -> NPC.NPCType.NOBLE;
            case GOODS_TRANSPORTER, CITY_TRANSPORTER -> NPC.NPCType.MERCHANT;
            case PEASANT_WORKER -> NPC.NPCType.PEASANT;
            case MAYOR -> NPC.NPCType.NOBLE;
            case CITY_GUARD -> NPC.NPCType.GUARD;
            case TAX_COLLECTOR -> NPC.NPCType.MERCHANT;
            case WANDERER -> NPC.NPCType.VILLAGER;
        };
    }
    
    /**
     * Gets roles appropriate for villages.
     */
    public static NPCRole[] getVillageRoles() {
        return new NPCRole[] { VILLAGE_ELDER, GOODS_TRANSPORTER, PEASANT_WORKER };
    }
    
    /**
     * Gets roles appropriate for cities.
     */
    public static NPCRole[] getCityRoles() {
        return new NPCRole[] { MAYOR, CITY_GUARD, CITY_TRANSPORTER, TAX_COLLECTOR };
    }
}
