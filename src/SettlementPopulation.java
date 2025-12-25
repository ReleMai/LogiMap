import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the population of a settlement (village or city).
 * Handles NPC spawning with proper roles and schedules.
 */
public class SettlementPopulation {
    
    // Village population configuration
    public static final int VILLAGE_POPULATION = 4;
    public static final int VILLAGE_ELDERS = 1;
    public static final int VILLAGE_TRANSPORTERS = 1;
    public static final int VILLAGE_PEASANTS = 2;
    
    // City population configuration
    public static final int CITY_POPULATION = 10;
    public static final int CITY_MAYORS = 1;
    public static final int CITY_GUARDS = 4;
    public static final int CITY_TRANSPORTERS = 3;
    public static final int CITY_TAX_COLLECTORS = 2;
    
    private Town settlement;
    private List<NPC> residents;
    private int gold; // Settlement treasury
    private int dailyIncome; // Income from resources/taxes
    private Town parentCity; // For villages - the city that owns them
    private List<Town> ownedVillages; // For cities - villages they control
    
    private Random random;
    
    public SettlementPopulation(Town settlement) {
        this.settlement = settlement;
        this.residents = new ArrayList<>();
        this.ownedVillages = new ArrayList<>();
        this.random = new Random(settlement.getName().hashCode());
        this.gold = settlement.isMajor() ? 1000 : 100; // Cities start with more gold
        this.dailyIncome = 0;
    }
    
    /**
     * Spawns the initial population for this settlement.
     */
    public List<NPC> spawnInitialPopulation() {
        residents.clear();
        
        if (settlement.isMajor()) {
            spawnCityPopulation();
        } else {
            spawnVillagePopulation();
        }
        
        return residents;
    }
    
    /**
     * Spawns NPCs for a village.
     */
    private void spawnVillagePopulation() {
        int centerX = settlement.getGridX() + settlement.getSize() / 2;
        int centerY = settlement.getGridY() + settlement.getSize() / 2;
        
        // 1 Village Elder
        for (int i = 0; i < VILLAGE_ELDERS; i++) {
            NPC elder = createNPC(NPCRole.VILLAGE_ELDER, centerX, centerY, 0.5);
            residents.add(elder);
        }
        
        // 1 Goods Transporter
        for (int i = 0; i < VILLAGE_TRANSPORTERS; i++) {
            NPC transporter = createNPC(NPCRole.GOODS_TRANSPORTER, centerX, centerY, 1.5);
            residents.add(transporter);
        }
        
        // 2 Peasants
        for (int i = 0; i < VILLAGE_PEASANTS; i++) {
            NPC peasant = createNPC(NPCRole.PEASANT_WORKER, centerX, centerY, 2.0);
            residents.add(peasant);
        }
    }
    
    /**
     * Spawns NPCs for a city.
     */
    private void spawnCityPopulation() {
        int centerX = settlement.getGridX() + settlement.getSize() / 2;
        int centerY = settlement.getGridY() + settlement.getSize() / 2;
        
        // 1 Mayor
        for (int i = 0; i < CITY_MAYORS; i++) {
            NPC mayor = createNPC(NPCRole.MAYOR, centerX, centerY, 0.5);
            residents.add(mayor);
        }
        
        // 4 Guards
        for (int i = 0; i < CITY_GUARDS; i++) {
            NPC guard = createNPC(NPCRole.CITY_GUARD, centerX, centerY, 3.0);
            setupGuardPatrol(guard, i, CITY_GUARDS);
            residents.add(guard);
        }
        
        // 3 Goods Transporters
        for (int i = 0; i < CITY_TRANSPORTERS; i++) {
            NPC transporter = createNPC(NPCRole.CITY_TRANSPORTER, centerX, centerY, 2.0);
            residents.add(transporter);
        }
        
        // 2 Tax Collectors
        for (int i = 0; i < CITY_TAX_COLLECTORS; i++) {
            NPC collector = createNPC(NPCRole.TAX_COLLECTOR, centerX, centerY, 1.5);
            residents.add(collector);
        }
    }
    
    /**
     * Creates an NPC with a specific role at a location.
     */
    private NPC createNPC(NPCRole role, int centerX, int centerY, double maxOffset) {
        // Random position near center
        double offsetX = (random.nextDouble() - 0.5) * 2 * maxOffset;
        double offsetY = (random.nextDouble() - 0.5) * 2 * maxOffset;
        
        String name = NameGenerator.generateName(role.isLeader());
        NPC.NPCType type = role.getSuitableNPCType();
        
        NPC npc = new NPC(name, type, settlement, centerX + offsetX, centerY + offsetY);
        npc.setRole(role);
        
        // Set wander radius based on settlement size and role
        // Leaders stay close, workers roam more, guards patrol the perimeter
        double townSize = settlement.getSize();
        double baseWanderRadius = townSize * 0.8; // Most of the town
        
        if (role.isLeader()) {
            npc.setWanderRadius(baseWanderRadius * 0.4); // Leaders stay in center
        } else if (role == NPCRole.CITY_GUARD) {
            npc.setWanderRadius(baseWanderRadius * 1.2); // Guards patrol wider
        } else {
            npc.setWanderRadius(baseWanderRadius); // Workers spread out
        }
        
        return npc;
    }
    
    /**
     * Sets up patrol route for a guard.
     */
    private void setupGuardPatrol(NPC guard, int guardIndex, int totalGuards) {
        // Guards patrol in a perimeter around the settlement
        // Each guard is assigned a section of the perimeter
        double angleOffset = (2 * Math.PI * guardIndex) / totalGuards;
        guard.setPatrolAngleOffset(angleOffset);
        guard.setIsPatrolling(true);
    }
    
    /**
     * Updates all residents based on the current game time.
     */
    public void updateForTime(int currentHour, double deltaTime) {
        for (NPC npc : residents) {
            NPCRole role = npc.getRole();
            if (role == null) continue;
            
            NPCRole.DailySchedule schedule = role.getSchedule();
            
            if (schedule.isWorkingHour(currentHour)) {
                // Should be working
                if (npc.getScheduleState() == NPC.ScheduleState.AT_HOME) {
                    npc.setScheduleState(NPC.ScheduleState.LEAVING_HOME);
                }
            } else {
                // Should be home
                if (npc.getScheduleState() == NPC.ScheduleState.WORKING) {
                    npc.setScheduleState(NPC.ScheduleState.GOING_HOME);
                }
            }
        }
    }
    
    /**
     * Adds daily income to the settlement treasury.
     */
    public void addIncome(int amount) {
        dailyIncome += amount;
    }
    
    /**
     * Processes end of day - collect income, pay expenses.
     */
    public void processEndOfDay() {
        gold += dailyIncome;
        dailyIncome = 0;
    }
    
    /**
     * Collects taxes (for cities collecting from villages).
     * @return Amount of tax collected
     */
    public int collectTaxes(double taxRate) {
        int taxAmount = (int)(dailyIncome * taxRate);
        dailyIncome -= taxAmount;
        return taxAmount;
    }
    
    // === Getters and Setters ===
    
    public List<NPC> getResidents() { return residents; }
    public int getPopulationCount() { return residents.size(); }
    public int getGold() { return gold; }
    public void addGold(int amount) { gold += amount; }
    public int getDailyIncome() { return dailyIncome; }
    
    public Town getParentCity() { return parentCity; }
    public void setParentCity(Town city) { this.parentCity = city; }
    
    public List<Town> getOwnedVillages() { return ownedVillages; }
    public void addOwnedVillage(Town village) { ownedVillages.add(village); }
    public void removeOwnedVillage(Town village) { ownedVillages.remove(village); }
    
    /**
     * Gets the NPC by role.
     */
    public List<NPC> getNPCsByRole(NPCRole role) {
        List<NPC> result = new ArrayList<>();
        for (NPC npc : residents) {
            if (npc.getRole() == role) {
                result.add(npc);
            }
        }
        return result;
    }
    
    /**
     * Gets the settlement leader (Elder or Mayor).
     */
    public NPC getLeader() {
        for (NPC npc : residents) {
            NPCRole role = npc.getRole();
            if (role != null && role.isLeader()) {
                return npc;
            }
        }
        return null;
    }
}
