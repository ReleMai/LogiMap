import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Manages NPCs across all towns in the world.
 * Handles spawning, updating, and rendering of NPCs.
 * Uses role-based population system for villages and cities.
 */
public class NPCManager {
    
    // NPC storage by town
    private Map<Town, List<NPC>> npcsByTown;
    private Map<Town, SettlementPopulation> populations;
    private List<NPC> roamingNPCs;
    private List<NPCParty> parties;
    
    // Reference to game time for schedule updates
    private GameTime gameTime;
    
    // Reference to road network for NPC travel speed
    private RoadNetwork roadNetwork;
    
    // Current hour cache for schedule checks
    private int lastCheckedHour = -1;
    
    // Configuration constants
    private static final int MIN_NPCS_VILLAGE = 3;
    private static final int MAX_NPCS_VILLAGE = 6;
    private static final int MIN_NPCS_CITY = 8;
    private static final int MAX_NPCS_CITY = 15;
    private static final double PARTY_FORMATION_CHANCE = 0.8;
    private static final int MIN_PARTIES = 2;
    
    // NPC type weights for extra NPCs beyond role-based ones
    private static final Map<NPC.NPCType, Integer> VILLAGE_WEIGHTS = Map.of(
        NPC.NPCType.VILLAGER, 40,
        NPC.NPCType.PEASANT, 30,
        NPC.NPCType.MERCHANT, 10,
        NPC.NPCType.GUARD, 5,
        NPC.NPCType.INNKEEPER, 5,
        NPC.NPCType.BLACKSMITH, 5,
        NPC.NPCType.BARD, 5
    );
    
    private static final Map<NPC.NPCType, Integer> CITY_WEIGHTS = Map.of(
        NPC.NPCType.VILLAGER, 25,
        NPC.NPCType.PEASANT, 15,
        NPC.NPCType.MERCHANT, 20,
        NPC.NPCType.GUARD, 15,
        NPC.NPCType.NOBLE, 10,
        NPC.NPCType.INNKEEPER, 5,
        NPC.NPCType.BLACKSMITH, 5,
        NPC.NPCType.BARD, 5
    );
    
    private Random random;
    private EconomySystem economySystem = null;
    
    public NPCManager() {
        npcsByTown = new HashMap<>();
        populations = new HashMap<>();
        roamingNPCs = new ArrayList<>();
        parties = new ArrayList<>();
        random = new Random();
    }

    public void setEconomySystem(EconomySystem economy) {
        this.economySystem = economy;
    }

    /**
     * Gets the SettlementPopulation object for a town if available.
     */
    public SettlementPopulation getPopulationForTown(Town town) {
        return populations.get(town);
    }
    
    public void setGameTime(GameTime gameTime) {
        this.gameTime = gameTime;
    }
    
    public void setRoadNetwork(RoadNetwork network) {
        this.roadNetwork = network;
        // Update all existing NPCs with road network
        for (List<NPC> npcs : npcsByTown.values()) {
            for (NPC npc : npcs) {
                npc.setRoadNetwork(network);
            }
        }
        for (NPC npc : roamingNPCs) {
            npc.setRoadNetwork(network);
        }
    }

    /**
     * Creates a handful of traveling traders and gatherers moving between towns.
     * Some roam solo, others form parties.
     */
    public void spawnRoamers(List<Town> towns) {
        if (towns == null || towns.size() < 2) return;
        roamingNPCs.clear();
        parties.clear();
        
        int roamCount = Math.min(6, towns.size() * 2); // More roamers for variety
        
        for (int i = 0; i < roamCount; i++) {
            Town start = towns.get(random.nextInt(towns.size()));
            Town dest = pickDifferentTown(towns, start);
            NPC.NPCType type = (i % 2 == 0) ? NPC.NPCType.MERCHANT : NPC.NPCType.PEASANT;
            NPC npc = NPC.createRandom(type, start);
            npc.setWanderRadius(start.isMajor() ? 3.0 : 2.0);
            npc.setTravelTarget(dest);
            roamingNPCs.add(npc);
        }
        
        // Form parties from some roamers
        formParties(towns);
    }
    
    /**
     * Forms NPC parties from roaming NPCs.
     */
    private void formParties(List<Town> towns) {
        if (roamingNPCs.size() < 2) return;
        
        List<NPC> availableNPCs = new ArrayList<>(roamingNPCs);
        int partiesFormed = 0;
        int maxParties = Math.max(MIN_PARTIES, roamingNPCs.size() / 2); // More parties
        
        // Guarantee at least MIN_PARTIES are formed if we have enough NPCs
        while (availableNPCs.size() >= 2 && partiesFormed < maxParties && 
               (partiesFormed < MIN_PARTIES || Math.random() < PARTY_FORMATION_CHANCE)) {
            // Pick 2-4 NPCs for a party
            int partySize = 2 + random.nextInt(Math.min(3, availableNPCs.size() - 1));
            List<NPC> partyMembers = new ArrayList<>();
            
            for (int i = 0; i < partySize && !availableNPCs.isEmpty(); i++) {
                NPC npc = availableNPCs.remove(random.nextInt(availableNPCs.size()));
                partyMembers.add(npc);
                roamingNPCs.remove(npc); // Remove from solo roamers
            }
            
            // Create party
            NPCParty party = new NPCParty(partyMembers);
            
            // Pick a destination town for the party
            if (!towns.isEmpty()) {
                Town dest = towns.get(random.nextInt(towns.size()));
                party.setDestination(dest);
            }
            
            parties.add(party);
            partiesFormed++;
        }
    }

    /**
     * Populates all towns in the list with resident NPCs.
     * Uses the role-based SettlementPopulation system.
     */
    public void populateAllTowns(List<Town> towns) {
        if (towns == null) return;
        for (Town town : towns) {
            populateTown(town);
        }
        
        // Assign city ownership of nearby villages
        assignCityVillageOwnership(towns);
    }
    
    /**
     * Populates a town with NPCs using the role-based system.
     * Villages get: 1 Elder, 1 Transporter, 2 Peasants (4 total)
     * Cities get: 1 Mayor, 4 Guards, 3 Transporters, 2 Tax Collectors (10 total)
     */
    public void populateTown(Town town) {
        if (npcsByTown.containsKey(town)) {
            return; // Already populated
        }
        
        // Create settlement population manager
        SettlementPopulation population = new SettlementPopulation(town);
        List<NPC> npcs = population.spawnInitialPopulation();
        
        // Set road network reference for all NPCs
        if (roadNetwork != null) {
            for (NPC npc : npcs) {
                npc.setRoadNetwork(roadNetwork);
            }
        }
        
        // If we have an economy system, register the town and ensure supply/demand exist
        if (economySystem != null) {
            economySystem.registerTown(town);
        }

        // Store references
        populations.put(town, population);
        npcsByTown.put(town, npcs);

        // Let each NPC know its manager for callbacks
        for (NPC npc : npcs) {
            npc.setManager(this);
        }
        
        System.out.println("Populated " + town.getName() + " with " + npcs.size() + " NPCs" +
            (town.isMajor() ? " (City)" : " (Village)"));
    }
    
    /**
     * Assigns villages to nearby cities for taxation.
     */
    private void assignCityVillageOwnership(List<Town> towns) {
        List<Town> cities = new ArrayList<>();
        List<Town> villages = new ArrayList<>();
        
        for (Town town : towns) {
            if (town.isMajor()) {
                cities.add(town);
            } else {
                villages.add(town);
            }
        }
        
        // Assign each village to nearest city
        for (Town village : villages) {
            Town nearestCity = null;
            double nearestDist = Double.MAX_VALUE;
            
            for (Town city : cities) {
                double dx = city.getGridX() - village.getGridX();
                double dy = city.getGridY() - village.getGridY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestCity = city;
                }
            }
            
            if (nearestCity != null && nearestDist < 100) { // Within 100 tiles
                SettlementPopulation cityPop = populations.get(nearestCity);
                SettlementPopulation villagePop = populations.get(village);
                
                if (cityPop != null && villagePop != null) {
                    cityPop.addOwnedVillage(village);
                    villagePop.setParentCity(nearestCity);
                    
                    // Assign tax collectors to this village
                    assignTaxCollectorRoute(nearestCity, village);
                }
            }
        }
    }
    
    /**
     * Assigns a tax collector from the city to visit the village.
     */
    private void assignTaxCollectorRoute(Town city, Town village) {
        List<NPC> cityNPCs = npcsByTown.get(city);
        if (cityNPCs == null) return;
        
        for (NPC npc : cityNPCs) {
            if (npc.getRole() == NPCRole.TAX_COLLECTOR && npc.getAssignedDestination() == null) {
                npc.setAssignedDestination(village);
                break;
            }
        }
    }
    
    // Reference to world for job site lookups
    private Object worldRef = null;
    
    public void setWorldReference(Object world) {
        this.worldRef = world;
    }
    
    /**
     * Assigns jobs to NPCs in villages based on village type.
     * Should be called after world generation.
     */
    public void assignVillagerJobs() {
        for (Map.Entry<Town, List<NPC>> entry : npcsByTown.entrySet()) {
            Town town = entry.getKey();
            
            // Only assign jobs to village NPCs, not city NPCs
            if (town.isMajor()) continue;
            
            List<NPC> npcs = entry.getValue();
            VillageType villageType = town.getVillageType();
            if (villageType == null) continue;
            
            // Assign jobs to some NPCs (not all - some should wander)
            int workersNeeded = Math.max(1, npcs.size() / 2);
            int assigned = 0;
            
            for (NPC npc : npcs) {
                if (assigned >= workersNeeded) break;
                
                // Only peasants and villagers get resource jobs
                if (npc.getType() != NPC.NPCType.PEASANT && 
                    npc.getType() != NPC.NPCType.VILLAGER) continue;
                
                // Get job site location (around the village)
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = 4 + random.nextDouble() * 6; // 4-10 tiles from center
                double siteX = town.getGridX() + town.getSize() / 2.0 + Math.cos(angle) * distance;
                double siteY = town.getGridY() + town.getSize() / 2.0 + Math.sin(angle) * distance;
                
                NPCJob.JobType jobType = npc.getPreferredJobType();
                NPCJob job = new NPCJob(jobType, siteX, siteY, town);
                npc.assignJob(job);
                assigned++;
            }
        }
    }
    
    /**
     * Reassigns jobs to NPCs that have completed their jobs.
     * Call this periodically during game update.
     */
    public void reassignCompletedJobs() {
        for (Map.Entry<Town, List<NPC>> entry : npcsByTown.entrySet()) {
            Town town = entry.getKey();
            if (town.isMajor()) continue;
            
            for (NPC npc : entry.getValue()) {
                // Skip if NPC has an active job
                if (npc.hasJob()) continue;
                
                // Only reassign to worker types
                if (npc.getType() != NPC.NPCType.PEASANT && 
                    npc.getType() != NPC.NPCType.VILLAGER) continue;
                
                // 30% chance to start a new job each check
                if (random.nextDouble() < 0.3) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = 4 + random.nextDouble() * 6;
                    double siteX = town.getGridX() + town.getSize() / 2.0 + Math.cos(angle) * distance;
                    double siteY = town.getGridY() + town.getSize() / 2.0 + Math.sin(angle) * distance;
                    
                    NPCJob.JobType jobType = npc.getPreferredJobType();
                    // Find nearest major town (city) for selling exports; fallback to home town if none
                    Town nearestMajor = null;
                    double bestDist = Double.MAX_VALUE;
                    for (Town t2 : populations.keySet()) {
                        if (!t2.isMajor() || t2.equals(town)) continue;
                        double dx = t2.getGridX() - town.getGridX();
                        double dy = t2.getGridY() - town.getGridY();
                        double d = Math.sqrt(dx * dx + dy * dy);
                        if (d < bestDist) { bestDist = d; nearestMajor = t2; }
                    }

                    Town deliveryTown = nearestMajor != null ? nearestMajor : town;
                    NPCJob job = new NPCJob(jobType, siteX, siteY, deliveryTown);
                    npc.assignJob(job);
                }
            }
        }
    }
    
    /**
     * Selects an NPC type based on weighted probabilities.
     */
    private NPC.NPCType selectNPCType(boolean isCity) {
        Map<NPC.NPCType, Integer> weights = isCity ? CITY_WEIGHTS : VILLAGE_WEIGHTS;
        
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(totalWeight);
        
        int cumulative = 0;
        for (Map.Entry<NPC.NPCType, Integer> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        
        return NPC.NPCType.VILLAGER; // Fallback
    }
    
    /**
     * Updates all NPCs near the given world position.
     */
    // Accumulator for job reassignment (don't check every frame)
    private double jobReassignTimer = 0;
    private static final double JOB_REASSIGN_INTERVAL = 5.0; // Check every 5 seconds
    
    public void update(double deltaTime, double playerX, double playerY, double viewRadius, List<Town> allTowns) {
        for (Map.Entry<Town, List<NPC>> entry : npcsByTown.entrySet()) {
            Town town = entry.getKey();
            
            // Only update NPCs near the player for performance
            double dx = town.getGridX() - playerX;
            double dy = town.getGridY() - playerY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist <= viewRadius + 5) { // Extra buffer
                for (NPC npc : entry.getValue()) {
                    npc.update(deltaTime);
                }
            }
        }

        // Update solo roaming NPCs
        for (NPC npc : roamingNPCs) {
            npc.update(deltaTime);
        }
        
        // Update NPC parties
        for (NPCParty party : parties) {
            party.update(deltaTime, allTowns);
        }
        
        // Periodically reassign jobs to idle NPCs
        jobReassignTimer += deltaTime;
        if (jobReassignTimer >= JOB_REASSIGN_INTERVAL) {
            jobReassignTimer = 0;
            reassignCompletedJobs();
        }
    }

    /**
     * Handles an NPC delivering gathered resources back to their town.
     * Splits the goods: half stored (if warehouse exists), half sold into town income via EconomySystem.
     */
    // Handles an NPC delivering gathered resources. deliveryTown may be null (will fallback to npc's homeTown)
    public void handleNPCDelivery(NPC npc, String resourceType, int quantity, Town deliveryTown) {
        if (npc == null || resourceType == null || quantity <= 0) return;
        Town town = deliveryTown != null ? deliveryTown : npc.getHomeTown();
        if (town == null) return;

        // Map to item ID
        String itemId = mapResourceTypeToItemId(resourceType);
        if (itemId == null) itemId = "stone_common";

        int toStorage = quantity / 2;
        int toSell = quantity - toStorage;

        // If the town produces this resource, don't sell it there — store it all instead
        String townSpecific = town.getSpecificResourceId();
        if (townSpecific != null && townSpecific.equals(itemId)) {
            // All goes to storage / town supply
            toStorage = quantity;
            toSell = 0;
            System.out.println("NPC sale blocked at " + town.getName() + ": town produces " + itemId + " — storing instead.");
        }

        // Store half in warehouse if available
        TownWarehouse wh = town.getWarehouse();
        if (toStorage > 0) {
            if (wh != null && wh.isPurchased() && wh.getStorage() != null) {
                ItemStack stack = ItemRegistry.createStack(itemId, toStorage);
                if (stack != null) {
                    wh.getStorage().addItem(stack);
                }
            } else {
                // Add to town supply directly if no warehouse
                if (economySystem != null) economySystem.addSupplyToTown(town, itemId, toStorage);
            }
        }

        // Sell the rest - behavior differs if selling at another (major) town
        if (toSell > 0 && economySystem != null) {
            int unitPrice = economySystem.getBuyPrice(town, itemId); // price town is willing to pay
            int revenue = unitPrice * toSell;

            // If selling at a major town (city) and NPC's home town is different, NPC carries revenue back to origin
            Town origin = npc.getHomeTown();
            SettlementPopulation destPop = populations.get(town);

            if (town.isMajor() && origin != null && !origin.equals(town)) {
                // NPC will carry gold back to origin and the city receives the supply
                npc.addCarryingGold(revenue);
                System.out.println(npc.getName() + " sold " + toSell + " " + itemId + " at " + town.getName() + " and will carry " + revenue + " gold back to " + origin.getName());
                // City supply increases
                economySystem.addSupplyToTown(town, itemId, toSell);

                // Assign NPC a return job to their origin so they bring back and deposit the gold
                double ox = origin.getGridX() + origin.getSize() / 2.0;
                double oy = origin.getGridY() + origin.getSize() / 2.0;
                NPCJob returnJob = new NPCJob(NPCJob.JobType.DELIVER_GOODS, ox, oy, origin);
                npc.assignJob(returnJob);
            } else {
                // Local sell: add revenue to destination
                if (destPop != null) destPop.addIncome(revenue);
                economySystem.addSupplyToTown(town, itemId, toSell);
            }

            // If NPC is carrying gold and has returned to its home town, deposit it into the home vault
            if (npc.getCarryingGold() > 0 && npc.getHomeTown() != null && npc.getHomeTown().equals(town)) {
                SettlementPopulation homePop = populations.get(town);
                if (homePop != null) {
                    homePop.addGold(npc.getCarryingGold());
                    System.out.println(npc.getName() + " deposited " + npc.getCarryingGold() + " gold into " + town.getName() + "'s vault.");
                }
                npc.setCarryingGold(0);
            }
        }
    }

    // Map resource type strings to item IDs
    private String mapResourceTypeToItemId(String resourceType) {
        if (resourceType == null) return null;
        switch (resourceType) {
            case "ore": return "ore_iron";
            case "crops":
            case "grain": return "grain_wheat";
            case "fish": return "fish_common";
            case "livestock": return "meat_mutton";
            default: return null;
        }
    }

    /**
     * Renders all NPCs within the visible area.
     * Uses proper world-to-screen coordinate transformation.
     */
    public void render(GraphicsContext gc, double offsetX, double offsetY, double zoom, double canvasWidth, double canvasHeight, int gridSize) {
        double tileSize = gridSize * zoom;
        
        // Render town NPCs
        for (Map.Entry<Town, List<NPC>> entry : npcsByTown.entrySet()) {
            for (NPC npc : entry.getValue()) {
                // Convert world coordinates to screen coordinates
                double screenX = npc.getWorldX() * tileSize + offsetX;
                double screenY = npc.getWorldY() * tileSize + offsetY;
                
                // Only render if on screen
                if (screenX >= -tileSize && screenX <= canvasWidth + tileSize &&
                    screenY >= -tileSize && screenY <= canvasHeight + tileSize) {
                    
                    double npcSize = tileSize * 0.8;
                    npc.render(gc, screenX + (tileSize - npcSize) / 2, screenY + (tileSize - npcSize) / 2, npcSize);
                }
            }
        }

        // Render solo roaming NPCs
        for (NPC npc : roamingNPCs) {
            // Convert world coordinates to screen coordinates
            double screenX = npc.getWorldX() * tileSize + offsetX;
            double screenY = npc.getWorldY() * tileSize + offsetY;
            
            if (screenX >= -tileSize && screenX <= canvasWidth + tileSize &&
                screenY >= -tileSize && screenY <= canvasHeight + tileSize) {
                double npcSize = tileSize * 0.8;
                npc.render(gc, screenX + (tileSize - npcSize) / 2, screenY + (tileSize - npcSize) / 2, npcSize);
            }
        }
        
        // Render parties
        for (NPCParty party : parties) {
            // Render the party leader at the party's center position
            if (party.getLeader() != null) {
                double screenX = party.getWorldX() * tileSize + offsetX;
                double screenY = party.getWorldY() * tileSize + offsetY;
                
                if (screenX >= -tileSize && screenX <= canvasWidth + tileSize &&
                    screenY >= -tileSize && screenY <= canvasHeight + tileSize) {
                    
                    double npcSize = tileSize * 0.8;
                    NPC leader = party.getLeader();
                    leader.render(gc, screenX + (tileSize - npcSize) / 2, screenY + (tileSize - npcSize) / 2, npcSize);
                    
                    // Render party count badge
                    if (party.getSize() > 1) {
                        renderPartyBadge(gc, screenX + tileSize * 0.6, screenY + tileSize * 0.1, party.getSize(), tileSize);
                    }
                }
            }
        }
    }
    
    /**
     * Renders a party count badge over the party sprite.
     */
    private void renderPartyBadge(GraphicsContext gc, double x, double y, int count, double tileSize) {
        double badgeSize = tileSize * 0.3;
        
        // Draw circle background
        gc.setFill(Color.web("#FFD700")); // Gold
        gc.fillOval(x, y, badgeSize, badgeSize);
        
        // Draw border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        gc.strokeOval(x, y, badgeSize, badgeSize);
        
        // Draw count number
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, badgeSize * 0.7));
        
        String text = String.valueOf(count);
        Text textNode = new Text(text);
        textNode.setFont(gc.getFont());
        double textWidth = textNode.getLayoutBounds().getWidth();
        double textHeight = textNode.getLayoutBounds().getHeight();
        
        gc.fillText(text, x + (badgeSize - textWidth) / 2, y + badgeSize / 2 + textHeight / 3);
    }
    
    /**
     * Gets NPCs at a specific world position (for interaction).
     * Uses tight hitboxes and returns the closest matching NPC.
     * Also checks for NPC parties.
     */
    public NPC getNPCAt(double worldX, double worldY, double radius) {
        NPC closest = null;
        double closestDistance = Double.MAX_VALUE;
        
        // Check parties first (they're rendered on top)
        for (NPCParty party : parties) {
            double dx = party.getWorldX() - worldX;
            double dy = party.getWorldY() - worldY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= radius && party.getLeader() != null && dist < closestDistance) {
                closest = party.getLeader();
                closestDistance = dist;
            }
        }
        
        // Check town NPCs - find the closest one
        for (List<NPC> npcs : npcsByTown.values()) {
            for (NPC npc : npcs) {
                if (npc.canInteract()) {
                    double dist = npc.getDistanceTo(worldX, worldY);
                    if (dist <= radius && dist < closestDistance) {
                        closest = npc;
                        closestDistance = dist;
                    }
                }
            }
        }
        
        // Check solo roaming NPCs
        for (NPC npc : roamingNPCs) {
            if (npc.canInteract()) {
                double dist = npc.getDistanceTo(worldX, worldY);
                if (dist <= radius && dist < closestDistance) {
                    closest = npc;
                    closestDistance = dist;
                }
            }
        }
        return closest;
    }
    
    /**
     * Gets the party containing the specified NPC, if any.
     */
    public NPCParty getPartyForNPC(NPC npc) {
        for (NPCParty party : parties) {
            if (party.containsNPC(npc)) {
                return party;
            }
        }
        return null;
    }
    
    /**
     * Gets all NPCs in a specific town.
     */
    public List<NPC> getNPCsInTown(Town town) {
        return npcsByTown.getOrDefault(town, new ArrayList<>());
    }
    
    /**
     * Clears all NPCs (for world reset).
     */
    public void clear() {
        npcsByTown.clear();
        roamingNPCs.clear();
    }
    
    /**
     * Gets total NPC count across all towns.
     */
    public int getTotalNPCCount() {
        return npcsByTown.values().stream().mapToInt(List::size).sum() + roamingNPCs.size();
    }

    private Town pickDifferentTown(List<Town> towns, Town exclude) {
        Town choice = exclude;
        for (int attempts = 0; attempts < 5 && choice == exclude; attempts++) {
            choice = towns.get(random.nextInt(towns.size()));
        }
        if (choice == exclude && towns.size() > 1) {
            // Force a different town
            choice = towns.get((towns.indexOf(exclude) + 1) % towns.size());
        }
        return choice;
    }
}
