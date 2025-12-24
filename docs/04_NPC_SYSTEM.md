# LogiMap Developer Guide - NPC System

## Table of Contents
1. [System Overview](#system-overview)
2. [NPC Class](#npc-class)
3. [NPCManager](#npcmanager)
4. [NPC Parties](#npc-parties)
5. [Tavern NPCs](#tavern-npcs)
6. [Behavior AI](#behavior-ai)
7. [Code Examples](#code-examples)

---

## System Overview

The NPC system manages all non-player characters:

```
NPCManager
    │
    ├── Town NPCs (stationary, by town)
    │       ├── Villagers
    │       ├── Merchants
    │       ├── Guards
    │       └── etc.
    │
    ├── Roaming NPCs (solo travelers)
    │       └── Travel between towns
    │
    └── NPC Parties (groups)
            ├── Shared inventory
            ├── Group movement
            └── Group activities
```

### Key Classes
| Class | Purpose |
|-------|---------|
| `NPC` | Individual NPC with position, type, behavior |
| `NPCManager` | Manages all NPCs, handles updates and rendering |
| `NPCParty` | Group of NPCs traveling together |
| `TavernNPC` | Recruitable NPCs in taverns |
| `NPCProfession` | Defines NPC jobs and abilities |

---

## NPC Class

### NPC Types
```java
public enum NPCType {
    VILLAGER("Villager", "#8B7355", "#5C4033"),
    MERCHANT("Merchant", "#DAA520", "#8B6914"),
    GUARD("Guard", "#708090", "#2F4F4F"),
    NOBLE("Noble", "#800020", "#4A0010"),
    PEASANT("Peasant", "#9C8B6E", "#6B5B3E"),
    BLACKSMITH("Blacksmith", "#36454F", "#1C1C1C"),
    INNKEEPER("Innkeeper", "#CD853F", "#8B4513"),
    BARD("Bard", "#9370DB", "#6A5ACD");
    
    private final String name;
    private final String primaryColor;    // Body/clothing
    private final String secondaryColor;  // Accent
}
```

### NPC Properties
```java
public class NPC {
    // Identity
    private String name;
    private NPCType type;
    private boolean isMale;
    
    // Position (world coordinates)
    private double worldX;
    private double worldY;
    
    // Movement
    private double targetX, targetY;
    private double speed = 1.2;  // Tiles per second
    private boolean isMoving = false;
    private Direction facing;
    
    // Town association
    private Town homeTown;
    private double wanderRadius = 2.0;
    private Town travelTarget;
    
    // Animation
    private int animFrame = 0;
    private double animTimer = 0;
    
    // Interaction
    private boolean canInteract = true;
    private String[] dialogueLines;
    
    // Current action
    private NPCAction currentAction = NPCAction.IDLE;
    
    public enum NPCAction {
        IDLE, WALKING, WORKING, TALKING, SHOPPING
    }
}
```

### Creating NPCs
```java
// Manual creation
NPC npc = new NPC("Erik", NPCType.GUARD, town, 50.0, 30.0);
npc.setWanderRadius(3.0);

// Random creation
NPC randomNPC = NPC.createRandom(NPCType.MERCHANT, town);
// Generates random name, position near town
```

### NPC Movement
```java
public void update(double deltaTime) {
    // Animation (faster when moving)
    animTimer += deltaTime * (isMoving ? 2.0 : 1.0);
    if (animTimer >= 0.2) {
        animTimer = 0;
        animFrame = (animFrame + 1) % 4;
    }
    
    if (isMoving) {
        // Calculate direction to target
        double dx = targetX - worldX;
        double dy = targetY - worldY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 0.05) {
            // Reached target
            worldX = targetX;
            worldY = targetY;
            isMoving = false;
            currentAction = NPCAction.IDLE;
        } else {
            // Move toward target
            double moveAmount = speed * deltaTime;
            worldX += (dx / dist) * moveAmount;
            worldY += (dy / dist) * moveAmount;
            
            // Update facing direction
            if (Math.abs(dx) > Math.abs(dy)) {
                facing = dx > 0 ? Direction.EAST : Direction.WEST;
            } else {
                facing = dy > 0 ? Direction.SOUTH : Direction.NORTH;
            }
        }
    } else {
        // Idle - wait then pick new target
        idleTimer += deltaTime;
        if (idleTimer >= idleDelay) {
            chooseNewTarget();
        }
    }
}

private void chooseNewTarget() {
    if (homeTown == null) return;
    
    // Random position near town center
    double angle = random.nextDouble() * Math.PI * 2;
    double radius = random.nextDouble() * wanderRadius;
    
    targetX = homeTown.getGridX() + Math.cos(angle) * radius;
    targetY = homeTown.getGridY() + Math.sin(angle) * radius;
    
    isMoving = true;
    currentAction = NPCAction.WALKING;
}
```

---

## NPCManager

### Initialization
```java
public class NPCManager {
    private Map<Town, List<NPC>> npcsByTown;
    private List<NPC> roamingNPCs;
    private List<NPCParty> parties;
    
    public NPCManager() {
        npcsByTown = new HashMap<>();
        roamingNPCs = new ArrayList<>();
        parties = new ArrayList<>();
    }
    
    // Populate all towns with NPCs
    public void populateAllTowns(List<Town> towns) {
        for (Town town : towns) {
            populateTown(town);
        }
    }
    
    // Spawn traveling NPCs
    public void spawnRoamers(List<Town> towns) {
        int roamCount = Math.min(6, towns.size() * 2);
        
        for (int i = 0; i < roamCount; i++) {
            Town start = towns.get(random.nextInt(towns.size()));
            Town dest = pickDifferentTown(towns, start);
            
            NPC npc = NPC.createRandom(NPCType.MERCHANT, start);
            npc.setTravelTarget(dest);
            roamingNPCs.add(npc);
        }
        
        // Form some into parties
        formParties(towns);
    }
}
```

### Town Population
```java
private static final int MIN_NPCS_VILLAGE = 3;
private static final int MAX_NPCS_VILLAGE = 6;
private static final int MIN_NPCS_CITY = 8;
private static final int MAX_NPCS_CITY = 15;

// NPC type distribution by town size
private static final Map<NPCType, Integer> VILLAGE_WEIGHTS = Map.of(
    NPCType.VILLAGER, 40,
    NPCType.PEASANT, 30,
    NPCType.MERCHANT, 10,
    NPCType.GUARD, 5,
    NPCType.BLACKSMITH, 5,
    NPCType.INNKEEPER, 5,
    NPCType.BARD, 5
);

private static final Map<NPCType, Integer> CITY_WEIGHTS = Map.of(
    NPCType.VILLAGER, 25,
    NPCType.PEASANT, 15,
    NPCType.MERCHANT, 20,
    NPCType.GUARD, 15,
    NPCType.NOBLE, 10,
    NPCType.INNKEEPER, 5,
    NPCType.BLACKSMITH, 5,
    NPCType.BARD, 5
);

public void populateTown(Town town) {
    List<NPC> npcs = new ArrayList<>();
    boolean isCity = town.isMajor();
    
    int npcCount = isCity 
        ? MIN_NPCS_CITY + random.nextInt(MAX_NPCS_CITY - MIN_NPCS_CITY + 1)
        : MIN_NPCS_VILLAGE + random.nextInt(MAX_NPCS_VILLAGE - MIN_NPCS_VILLAGE + 1);
    
    for (int i = 0; i < npcCount; i++) {
        NPCType type = selectNPCType(isCity);
        NPC npc = NPC.createRandom(type, town);
        npc.setWanderRadius(isCity ? 2.5 : 1.5);
        npcs.add(npc);
    }
    
    npcsByTown.put(town, npcs);
}
```

### Update and Render
```java
public void update(double deltaTime, double playerX, double playerY, 
                   double viewRadius, List<Town> allTowns) {
    // Update town NPCs (only near player for performance)
    for (Map.Entry<Town, List<NPC>> entry : npcsByTown.entrySet()) {
        Town town = entry.getKey();
        double dist = distance(town, playerX, playerY);
        
        if (dist <= viewRadius + 5) {
            for (NPC npc : entry.getValue()) {
                npc.update(deltaTime);
            }
        }
    }
    
    // Update roaming NPCs
    for (NPC npc : roamingNPCs) {
        npc.update(deltaTime);
    }
    
    // Update parties
    for (NPCParty party : parties) {
        party.update(deltaTime, allTowns);
    }
}

public void render(GraphicsContext gc, double offsetX, double offsetY, 
                   double zoom, double canvasWidth, double canvasHeight, int gridSize) {
    double tileSize = gridSize * zoom;
    
    // Render all NPCs
    for (List<NPC> npcs : npcsByTown.values()) {
        for (NPC npc : npcs) {
            renderNPC(gc, npc, tileSize, offsetX, offsetY, canvasWidth, canvasHeight);
        }
    }
    
    for (NPC npc : roamingNPCs) {
        renderNPC(gc, npc, tileSize, offsetX, offsetY, canvasWidth, canvasHeight);
    }
    
    // Render parties with badges
    for (NPCParty party : parties) {
        renderParty(gc, party, tileSize, offsetX, offsetY, canvasWidth, canvasHeight);
    }
}

private void renderParty(GraphicsContext gc, NPCParty party, ...) {
    NPC leader = party.getLeader();
    if (leader == null) return;
    
    double screenX = party.getWorldX() * tileSize + offsetX;
    double screenY = party.getWorldY() * tileSize + offsetY;
    
    // Check if visible
    if (screenX < -tileSize || screenX > canvasWidth + tileSize) return;
    if (screenY < -tileSize || screenY > canvasHeight + tileSize) return;
    
    // Render leader sprite
    leader.render(gc, screenX, screenY, tileSize * 0.8);
    
    // Render party count badge
    if (party.getSize() > 1) {
        renderPartyBadge(gc, screenX + tileSize * 0.6, screenY, party.getSize());
    }
}
```

---

## NPC Parties

### NPCParty Class
```java
public class NPCParty {
    private List<NPC> members;
    private String partyName;
    private Map<ResourceType, Integer> sharedInventory;
    
    // Position (party moves as unit)
    private double worldX, worldY;
    private Town destination;
    
    // Behavior
    private PartyTask currentTask;
    
    public enum PartyTask {
        IDLE("Resting"),
        GATHERING("Gathering resources"),
        TRADING("Trading with town"),
        TRAVELING("Traveling between towns");
    }
    
    public NPCParty(List<NPC> members) {
        this.members = new ArrayList<>(members);
        this.sharedInventory = new HashMap<>();
        this.currentTask = PartyTask.IDLE;
        
        if (!members.isEmpty()) {
            NPC leader = members.get(0);
            partyName = leader.getName() + "'s Party";
            worldX = leader.getWorldX();
            worldY = leader.getWorldY();
        }
    }
}
```

### Party Movement
```java
public void setPosition(double x, double y) {
    this.worldX = x;
    this.worldY = y;
    
    // Arrange members in formation
    for (int i = 0; i < members.size(); i++) {
        NPC npc = members.get(i);
        
        if (members.size() > 1) {
            // Circular formation
            double angle = (i * 2.0 * Math.PI) / members.size();
            double radius = 0.3;
            double offsetX = Math.cos(angle) * radius;
            double offsetY = Math.sin(angle) * radius;
            npc.setPosition(worldX + offsetX, worldY + offsetY);
        } else {
            npc.setPosition(worldX, worldY);
        }
    }
}

public void moveTowards(double destX, double destY, double speed, double deltaTime) {
    double dx = destX - worldX;
    double dy = destY - worldY;
    double distance = Math.sqrt(dx * dx + dy * dy);
    
    if (distance > 0.1) {
        double moveDistance = Math.min(speed * deltaTime, distance);
        worldX += (dx / distance) * moveDistance;
        worldY += (dy / distance) * moveDistance;
        setPosition(worldX, worldY);  // Update all members
    }
}
```

### Party AI
```java
public void update(double deltaTime, List<Town> allTowns) {
    taskTimer += deltaTime;
    
    switch (currentTask) {
        case IDLE:
            if (taskTimer > 5) {
                pickNewTask(allTowns);
            }
            break;
            
        case TRAVELING:
            if (destination != null) {
                moveTowards(destination.getGridX(), destination.getGridY(), 0.5, deltaTime);
                
                // Check if arrived
                if (distanceTo(destination) < 0.5) {
                    currentTask = PartyTask.TRADING;
                    taskTimer = 0;
                }
            }
            break;
            
        case GATHERING:
            if (taskTimer > 3) {
                // Collect random resource
                ResourceType type = ResourceType.values()[random.nextInt(...)];
                addResource(type, 1 + random.nextInt(3));
                currentTask = PartyTask.IDLE;
            }
            break;
            
        case TRADING:
            if (taskTimer > 2) {
                // Sell all resources
                sharedInventory.clear();
                pickNewTask(allTowns);
            }
            break;
    }
}

private void pickNewTask(List<Town> allTowns) {
    double roll = Math.random();
    
    if (roll < 0.4) {
        // Travel to new town
        destination = allTowns.get(random.nextInt(allTowns.size()));
        currentTask = PartyTask.TRAVELING;
    } else if (roll < 0.7) {
        // Gather resources
        currentTask = PartyTask.GATHERING;
    } else {
        currentTask = PartyTask.IDLE;
    }
    taskTimer = 0;
}
```

---

## Tavern NPCs

### TavernNPC Class
```java
public class TavernNPC {
    private String name;
    private boolean isMale;
    private NPCProfession profession;
    private CharacterStats stats;
    private int hireCost;
    
    // Appearance
    private Color hairColor;
    private Color skinTone;
    
    public static TavernNPC generateRandom() {
        TavernNPC npc = new TavernNPC();
        npc.name = MedievalNameGenerator.generateName();
        npc.isMale = random.nextBoolean();
        npc.profession = NPCProfession.getRandomForTavern();
        npc.stats = CharacterStats.generateForProfession(
            npc.profession.getPrimaryStat(),
            npc.profession.getSecondaryStat()
        );
        npc.hireCost = calculateHireCost(npc.stats, npc.profession);
        return npc;
    }
}
```

### Hiring NPCs
```java
// In TavernRecruitmentPanel
public PartyMember createPartyMemberFromNPC(TavernNPC npc) {
    PartyMember member = new PartyMember(
        npc.getName(),
        PartyMember.Role.fromProfession(npc.getProfession()),
        npc.isMale()
    );
    
    // Preserve NPC identity
    member.setProfessionName(npc.getProfession().getName());
    member.setGender(npc.isMale());
    member.setStatsFromCharacterStats(npc.getStats());
    
    return member;
}

public void onHireClicked(TavernNPC npc) {
    if (player.getCurrency().canAfford(npc.getHireCost())) {
        player.getCurrency().subtract(npc.getHireCost());
        
        PartyMember member = createPartyMemberFromNPC(npc);
        player.getParty().addMember(member);
        
        // Remove from tavern
        availableNPCs.remove(npc);
        refreshUI();
    }
}
```

---

## Behavior AI

### NPC Actions
```java
public enum NPCAction {
    IDLE,      // Standing still
    WALKING,   // Moving to target
    WORKING,   // At workplace (blacksmith hammering, etc.)
    TALKING,   // In conversation
    SHOPPING   // At market stall
}

// Action-specific behavior
public void performAction(double deltaTime) {
    switch (currentAction) {
        case WORKING:
            workTimer += deltaTime;
            if (workTimer >= workDuration) {
                workTimer = 0;
                // Produce item, switch to idle
                currentAction = NPCAction.IDLE;
            }
            break;
            
        case TALKING:
            talkTimer += deltaTime;
            if (talkTimer >= talkDuration) {
                // End conversation
                currentAction = NPCAction.WALKING;
                chooseNewTarget();
            }
            break;
    }
}
```

### Daily Schedule (Future Enhancement)
```java
public class NPCSchedule {
    private Map<Integer, ScheduleEntry> hourlySchedule;
    
    public NPCAction getActionForTime(int hour) {
        ScheduleEntry entry = hourlySchedule.get(hour);
        return entry != null ? entry.action : NPCAction.IDLE;
    }
}

// Example schedule for a Blacksmith
NPCSchedule blacksmithSchedule = new NPCSchedule();
blacksmithSchedule.addEntry(6, NPCAction.WALKING, "home", "shop");
blacksmithSchedule.addEntry(7, NPCAction.WORKING, "shop");
blacksmithSchedule.addEntry(12, NPCAction.WALKING, "shop", "tavern");
blacksmithSchedule.addEntry(13, NPCAction.IDLE, "tavern");  // Lunch
blacksmithSchedule.addEntry(14, NPCAction.WORKING, "shop");
blacksmithSchedule.addEntry(18, NPCAction.WALKING, "shop", "home");
blacksmithSchedule.addEntry(19, NPCAction.IDLE, "home");    // Evening
```

---

## Code Examples

### Adding a New NPC Type
```java
// 1. Add to NPCType enum
public enum NPCType {
    // ... existing types ...
    HEALER("Healer", "#90EE90", "#228B22");  // Light green
}

// 2. Add to weight maps
CITY_WEIGHTS.put(NPCType.HEALER, 5);
VILLAGE_WEIGHTS.put(NPCType.HEALER, 3);

// 3. Add dialogue
case HEALER -> new String[] {
    "Are you injured, traveler?",
    "I can cure what ails you.",
    "Herbs and poultices, my specialty.",
    "Stay healthy out there!"
};

// 4. Add special rendering (if needed)
case HEALER:
    // Draw medical cross
    gc.setFill(Color.RED);
    gc.fillRect(x + size * 0.4, y + size * 0.35, size * 0.2, size * 0.1);
    gc.fillRect(x + size * 0.45, y + size * 0.3, size * 0.1, size * 0.2);
    break;
```

### NPC Interaction System
```java
// In MapCanvas - handling NPC click
private void handleRightClick(int gridX, int gridY) {
    NPC clickedNPC = npcManager.getNPCAt(gridX + 0.5, gridY + 0.5, 1.5);
    
    if (clickedNPC != null) {
        if (isPlayerNearNPC(clickedNPC)) {
            // Immediate interaction
            triggerNPCInteraction(clickedNPC);
        } else {
            // Move to NPC first
            player.moveTo(clickedNPC.getWorldX(), clickedNPC.getWorldY());
            pendingNPCInteraction = clickedNPC;
        }
    }
}

private void triggerNPCInteraction(NPC npc) {
    // Show dialogue
    String dialogue = npc.getRandomDialogue();
    showDialoguePopup(npc.getName(), dialogue);
    
    // Check for special interactions
    if (npc.getType() == NPCType.MERCHANT) {
        showTradeMenu(npc);
    } else if (npc.getType() == NPCType.INNKEEPER) {
        showInnMenu(npc);
    }
}
```

---

## Tips for NPC Development

### Performance
```java
// Only update NPCs near player
double viewRadius = screenSize / (GRID_SIZE * zoom);
for (NPC npc : allNPCs) {
    if (distanceTo(npc, player) <= viewRadius + buffer) {
        npc.update(deltaTime);
    }
}
```

### Natural Movement
```java
// Add slight randomness to movement
double jitter = (random.nextDouble() - 0.5) * 0.1;
worldX += jitter * deltaTime;
worldY += jitter * deltaTime;

// Vary speed slightly
double actualSpeed = speed * (0.9 + random.nextDouble() * 0.2);
```

### Dialogue Variety
```java
// Context-aware dialogue
public String getDialogue(GameTime time, Weather weather) {
    if (time.isNight()) {
        return nightDialogues[random.nextInt(nightDialogues.length)];
    }
    if (weather == Weather.RAIN) {
        return rainDialogues[random.nextInt(rainDialogues.length)];
    }
    return defaultDialogues[random.nextInt(defaultDialogues.length)];
}
```

---

## Next: [05_PLAYER_SYSTEMS.md](05_PLAYER_SYSTEMS.md)
