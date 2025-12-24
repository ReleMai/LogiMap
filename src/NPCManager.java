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
 */
public class NPCManager {
    
    // NPC storage by town
    private Map<Town, List<NPC>> npcsByTown;
    private List<NPC> roamingNPCs;
    private List<NPCParty> parties;
    
    // Configuration
    private static final int MIN_NPCS_VILLAGE = 3;
    private static final int MAX_NPCS_VILLAGE = 6;
    private static final int MIN_NPCS_CITY = 8;
    private static final int MAX_NPCS_CITY = 15;
    
    // Party formation chance for roaming NPCs
    private static final double PARTY_FORMATION_CHANCE = 0.8; // 80% chance roamers form parties
    private static final int MIN_PARTIES = 2; // Minimum parties to spawn
    
    // NPC type weights for villages and cities
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
    
    public NPCManager() {
        npcsByTown = new HashMap<>();
        roamingNPCs = new ArrayList<>();
        parties = new ArrayList<>();
        random = new Random();
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
     */
    public void populateAllTowns(List<Town> towns) {
        if (towns == null) return;
        for (Town town : towns) {
            populateTown(town);
        }
    }
    
    /**
     * Populates a town with NPCs based on its size.
     */
    public void populateTown(Town town) {
        if (npcsByTown.containsKey(town)) {
            return; // Already populated
        }
        
        List<NPC> npcs = new ArrayList<>();
        boolean isCity = town.isMajor();
        
        // Determine NPC count
        int minNpcs = isCity ? MIN_NPCS_CITY : MIN_NPCS_VILLAGE;
        int maxNpcs = isCity ? MAX_NPCS_CITY : MAX_NPCS_VILLAGE;
        int npcCount = minNpcs + random.nextInt(maxNpcs - minNpcs + 1);
        
        // Spawn NPCs
        for (int i = 0; i < npcCount; i++) {
            NPC.NPCType type = selectNPCType(isCity);
            NPC npc = NPC.createRandom(type, town);
            
            // Vary wander radius based on town size
            npc.setWanderRadius(isCity ? 2.5 : 1.5);
            
            npcs.add(npc);
        }
        
        npcsByTown.put(town, npcs);
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
     * Also checks for NPC parties.
     */
    public NPC getNPCAt(double worldX, double worldY, double radius) {
        // Check parties first (they're rendered on top)
        for (NPCParty party : parties) {
            double dx = party.getWorldX() - worldX;
            double dy = party.getWorldY() - worldY;
            if (Math.sqrt(dx * dx + dy * dy) <= radius && party.getLeader() != null) {
                return party.getLeader(); // Return leader for interaction
            }
        }
        
        // Check town NPCs
        for (List<NPC> npcs : npcsByTown.values()) {
            for (NPC npc : npcs) {
                if (npc.isNear(worldX, worldY, radius) && npc.canInteract()) {
                    return npc;
                }
            }
        }
        
        // Check solo roaming NPCs
        for (NPC npc : roamingNPCs) {
            if (npc.isNear(worldX, worldY, radius) && npc.canInteract()) {
                return npc;
            }
        }
        return null;
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
