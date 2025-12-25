# Resource Node Blueprint

> **Last Updated:** December 24, 2025
> This document is a reference template for creating new resource node types in LogiMap.
> Update this file whenever the resource system changes.

---

## Table of Contents
1. [System Overview](#system-overview)
2. [Base Class Reference](#base-class-reference)
3. [Required Components](#required-components)
4. [Timer System](#timer-system)
5. [Creating a New Resource Node](#creating-a-new-resource-node)
6. [Interaction Menu](#interaction-menu)
7. [Registration Checklist](#registration-checklist)
8. [Existing Node Reference](#existing-node-reference)

---

## System Overview

Resource nodes are interactive world objects that players can harvest for resources.
Each node type spawns near villages based on the village's specialization.

### Core Architecture
```
ResourceNodeBase (abstract base)
    ├── LumberNode (WoodType)
    ├── QuarryNode (StoneType)
    ├── OreNode (OreType)
    ├── FisheryNode (FishType)
    └── PastoralNode (if exists)

FarmlandNode (standalone - different architecture)
    └── Uses GrainType, GrowthStage
```

---

## Base Class Reference

### ResourceNodeBase Properties

```java
// === POSITION ===
protected double worldX;              // World X coordinate
protected double worldY;              // World Y coordinate

// === HARVEST SYSTEM ===
protected boolean isHarvestable;      // Can currently be harvested
protected int harvestsRemaining;      // Harvests left before depletion
protected int maxHarvests;            // Maximum harvests when full
protected long lastHarvestTime;       // Timestamp of last harvest (ms)
protected long regrowthTimeMs;        // Time to regrow after harvest (ms)

// === RESOURCE TYPE ===
protected Object resourceType;        // The specific enum value (WoodType, StoneType, etc.)
protected Town parentVillage;         // Village this node belongs to

// === VISUAL ===
protected int variant;                // Visual variation (0-3 typically)
protected double rotation;            // Slight rotation for variety
protected double size;                // Size multiplier (0.8-1.2 typically)

// === INTERACTION ===
protected double interactionRadius;   // How close player must be (default: 3.0)
```

### Required Abstract Methods

```java
// Renders the node sprite
public abstract void render(GraphicsContext gc, double screenX, double screenY, 
                           double zoom, int tileSize);

// Returns harvest amount (base yield before modifiers)
public abstract int getBaseYield();

// Returns category name: "Grain", "Timber", "Stone", "Fish", "Ore"
public abstract String getResourceCategory();

// Returns item ID for inventory: "grain_wheat", "wood_oak", etc.
public abstract String getItemId();
```

---

## Timer System

### Gather Timer (ActionProgress)
The gather timer is handled by the **ActionProgress** system, not the node itself.
When player initiates gathering:

1. `ResourceInteraction` or `FarmlandInteraction` calls `actionProgress.startAction()`
2. Action runs for `GATHERING_DURATION` game minutes (currently 5 minutes)
3. On completion, `harvest()` or `harvestDirect()` is called
4. Node updates `lastHarvestTime` and `isHarvestable`

```java
// In ResourceInteraction.java
private static final int GATHERING_DURATION = 5; // Game minutes

// Start gathering action
actionProgress.startAction(
    "Gathering " + targetNode.getDisplayName(),
    GATHERING_DURATION,
    this::onGatherComplete  // Callback when done
);
```

### Regrowth Timer (Cooldown)
After being harvested, nodes need time to regrow:

```java
// In ResourceNodeBase.update()
public void update() {
    if (!isHarvestable && harvestsRemaining <= 0) {
        long timeSinceHarvest = System.currentTimeMillis() - lastHarvestTime;
        if (timeSinceHarvest >= regrowthTimeMs) {
            // Regrow
            harvestsRemaining = maxHarvests;
            isHarvestable = true;
        }
    }
}
```

### Default Timer Values by Resource Type

| Resource Type | maxHarvests | regrowthTimeMs | Notes |
|--------------|-------------|----------------|-------|
| Lumber       | 4-6         | 90-150s        | Trees regrow slowly |
| Quarry       | 5-8         | 120-180s       | Stone is abundant |
| Ore          | 6-10        | 180-300s       | Ore is rare, long regrow |
| Fishery      | 6-9         | 45-75s         | Fish respawn quickly |
| Farmland     | 3-5         | 60-120s        | Crops grow moderately |

---

## Creating a New Resource Node

### Step 1: Create Resource Type Enum

```java
/**
 * Enumeration of [Resource] types that can be harvested.
 */
public enum NewResourceType {
    TYPE_A("Type A", "#color1", "#color2", 10, "Flavor text"),
    TYPE_B("Type B", "#color1", "#color2", 15, "Flavor text");
    
    private final String displayName;
    private final Color primaryColor;
    private final Color secondaryColor;
    private final int baseYield;
    private final String description;
    
    // Constructor and getters...
    
    // Optional: Render method for visual sprite
    public void renderSprite(GraphicsContext gc, double x, double y, double size) {
        // Draw the resource sprite
    }
}
```

### Step 2: Create Node Class

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * [Description] node that spawns near [village type] villages.
 * Displays as [visual description].
 */
public class NewResourceNode extends ResourceNodeBase {
    
    private NewResourceType resourceType;
    // Add custom visual properties
    
    /**
     * Creates a [resource] node near a village.
     */
    public NewResourceNode(double worldX, double worldY, NewResourceType type, Town parentVillage) {
        super(worldX, worldY, type, parentVillage);
        this.resourceType = type;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // === TIMER CONFIGURATION ===
        this.maxHarvests = X + rand.nextInt(Y);        // X-Y harvests
        this.harvestsRemaining = maxHarvests;
        this.regrowthTimeMs = Xms + rand.nextInt(Yms); // Time to regrow
        this.interactionRadius = 3.0;                   // Interaction range
        this.size = 0.8 + rand.nextDouble() * 0.4;     // Visual size
        
        // Generate visual variety
        // ...
    }
    
    @Override
    public void render(GraphicsContext gc, double screenX, double screenY, 
                      double zoom, int tileSize) {
        double baseSize = size * zoom * tileSize;
        
        // Draw shadow
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.25));
        gc.fillOval(screenX - baseSize * 0.4, screenY + baseSize * 0.1, 
                    baseSize * 0.8, baseSize * 0.3);
        
        // Draw main sprite
        // ...
        
        // Draw harvest indicator if ready
        if (isHarvestable && harvestsRemaining > 0) {
            gc.setFill(Color.web("#FFD700").deriveColor(0, 1, 1, 0.3));
            gc.fillOval(screenX - baseSize * 0.5, screenY - baseSize * 0.2, 
                        baseSize, baseSize * 0.5);
        }
    }
    
    @Override
    public int getBaseYield() {
        return resourceType.getBaseYield();
    }
    
    @Override
    public String getResourceCategory() {
        return "NewCategory"; // e.g., "Timber", "Stone", etc.
    }
    
    @Override
    public String getItemId() {
        return "category_" + resourceType.name().toLowerCase();
        // e.g., "wood_oak", "stone_granite", "ore_iron"
    }
}
```

### Step 3: Register in ItemRegistry

```java
// In ItemRegistry.java, add items for each resource type
new Item("category_typea", "Type A", "Description", ItemCategory.MATERIAL, 0, 10, "🎯"),
new Item("category_typeb", "Type B", "Description", ItemCategory.MATERIAL, 0, 15, "🎯"),
```

### Step 4: Add Node Generation in DemoWorld

```java
// In DemoWorld.java, add spawning logic
private List<NewResourceNode> newResourceNodes = new ArrayList<>();

private void spawnNewResourceNodes(Town village) {
    if (!village.getInfo().contains("relevant type")) return;
    
    Random rand = new Random(village.getName().hashCode());
    int nodeCount = 3 + rand.nextInt(4);
    
    for (int i = 0; i < nodeCount; i++) {
        double angle = rand.nextDouble() * Math.PI * 2;
        double dist = 4 + rand.nextDouble() * 6;
        double nx = village.getGridX() + Math.cos(angle) * dist;
        double ny = village.getGridY() + Math.sin(angle) * dist;
        
        // Pick random type
        NewResourceType type = NewResourceType.values()[rand.nextInt(NewResourceType.values().length)];
        
        newResourceNodes.add(new NewResourceNode(nx, ny, type, village));
    }
}
```

---

## Interaction Menu

Resource nodes use `ResourceInteraction.java` for generic handling.
It automatically adapts based on `getResourceCategory()`:

| Category | Action Verb | Energy Cost | Icon |
|----------|-------------|-------------|------|
| Grain    | Harvest     | 8           | 🌾   |
| Timber   | Chop        | 15          | 🪵   |
| Stone    | Quarry      | 18          | 🪨   |
| Fish     | Fish        | 12          | 🐟   |
| Ore      | Mine        | 20          | ⛏    |

### Custom Interaction Menu (Optional)

If your resource needs special behavior, create a custom interaction class:

```java
public class NewResourceInteraction {
    // Menu dimensions (unified with other menus)
    private double menuWidth = 280;
    private double menuHeight = 250;
    
    // Timer duration in game minutes
    private static final int GATHERING_DURATION = 5;
    
    // Required systems
    private GameTime gameTime;
    private PlayerEnergy playerEnergy;
    private ActionProgress actionProgress;
    
    // Must implement:
    // - show(Node, screenX, screenY)
    // - hide()
    // - render(GraphicsContext)
    // - onMouseClick(x, y) -> boolean
    // - onMouseMove(x, y)
    // - isVisible() -> boolean
}
```

---

## Registration Checklist

When adding a new resource node type:

- [ ] Create resource type enum (e.g., `NewType.java`)
- [ ] Create node class extending `ResourceNodeBase`
- [ ] Implement all abstract methods:
  - [ ] `render()`
  - [ ] `getBaseYield()`
  - [ ] `getResourceCategory()`
  - [ ] `getItemId()`
- [ ] Configure timers in constructor:
  - [ ] `maxHarvests`
  - [ ] `regrowthTimeMs`
  - [ ] `interactionRadius`
- [ ] Register items in `ItemRegistry.java`
- [ ] Add node spawning in `DemoWorld.java`
- [ ] Add to rendering loop in `MapCanvas.java` or equivalent
- [ ] Add click detection in `LogiMapUI.java`
- [ ] Test harvest → regrow → harvest cycle
- [ ] Update this blueprint document!

---

## Existing Node Reference

### LumberNode
- **Type Enum:** `WoodType` (OAK, BIRCH, PINE, MAPLE)
- **Category:** "Timber"
- **Harvests:** 4-6
- **Regrowth:** 90-150 seconds
- **Gather Timer:** 5 game minutes (via `ResourceInteraction` → `ActionProgress`)
- **Visual:** Group of 3-5 trees with detailed canopy
- **Interaction:** Uses generic `ResourceInteraction.java`

### QuarryNode
- **Type Enum:** `StoneType` (GRANITE, LIMESTONE, SLATE, SANDSTONE)
- **Category:** "Stone"
- **Harvests:** 5-8
- **Regrowth:** 120-180 seconds
- **Gather Timer:** 5 game minutes (via `ResourceInteraction` → `ActionProgress`)
- **Visual:** Boulder with sledgehammer
- **Interaction:** Uses generic `ResourceInteraction.java`

### OreNode
- **Type Enum:** `OreType` (IRON, COPPER, GOLD, SILVER)
- **Category:** "Ore"
- **Harvests:** 6-10
- **Regrowth:** 180-300 seconds
- **Gather Timer:** 5 game minutes (via `ResourceInteraction` → `ActionProgress`)
- **Visual:** Rock formation with shimmering veins
- **Interaction:** Uses generic `ResourceInteraction.java`

### FisheryNode
- **Type Enum:** `FishType` (TROUT, SALMON, CARP, BASS)
- **Category:** "Fish"
- **Harvests:** 6-9
- **Regrowth:** 45-75 seconds
- **Gather Timer:** 5 game minutes (via `ResourceInteraction` → `ActionProgress`)
- **Visual:** Animated jumping fish with ripples
- **Interaction:** Uses generic `ResourceInteraction.java`

### FarmlandNode (Standalone)
- **Type Enum:** `GrainType` (WHEAT, BARLEY, RYE, OATS)
- **Category:** "Grain"
- **Harvests:** 3-5
- **Regrowth:** 60-120 seconds
- **Gather Timer:** 5 game minutes (via `FarmlandInteraction` → `ActionProgress`)
- **Growth Stages:** SEEDS → SPROUTS → GROWING → MATURE → RIPE → HARVESTED
- **Visual:** Rows of crop stalks
- **Interaction:** Uses `FarmlandInteraction.java` (custom, not ResourceInteraction)

---

## Notes

- **FarmlandNode** does not extend `ResourceNodeBase` - it has its own growth stage system
- All nodes should call `update()` each frame to handle regrowth timers
- The `ActionProgress` system handles the gather timer (player-side delay)
- The `regrowthTimeMs` handles the resource cooldown (node-side delay)
- Keep visual sprites consistent with medieval theme
- Test at different zoom levels for rendering

---

*End of Blueprint*
