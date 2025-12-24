# LogiMap Developer Guide - Player Systems

## Table of Contents
1. [PlayerSprite Overview](#playersprite-overview)
2. [Movement System](#movement-system)
3. [Character Stats](#character-stats)
4. [Inventory System](#inventory-system)
5. [Equipment System](#equipment-system)
6. [Party System](#party-system)
7. [Code Examples](#code-examples)

---

## PlayerSprite Overview

The player is represented by the `PlayerSprite` class, which handles:
- Position and movement
- Rendering (body, equipment, animations)
- Stats and attributes
- Inventory management
- Party management

### Core Properties
```java
public class PlayerSprite {
    // Position
    private double gridX, gridY;
    private double targetX, targetY;
    private boolean isMoving;
    
    // Movement
    private double baseSpeed = 3.0;
    private double currentSpeed;
    private Direction facing = Direction.SOUTH;
    
    // Animation
    private int animFrame = 0;
    private double animTimer = 0;
    
    // Body parts for rendering
    private BodyPart head, chest, leftArm, rightArm, leftLeg, rightLeg;
    
    // Systems
    private CharacterStats characterStats;
    private Inventory inventory;
    private Equipment equipment;
    private Party party;
    private Currency currency;
    
    // Appearance
    private boolean isMale;
    private Color hairColor;
}
```

---

## Movement System

### Point-and-Click Movement
```java
// Called when player clicks on map
public void moveTo(double targetX, double targetY) {
    this.targetX = targetX;
    this.targetY = targetY;
    this.isMoving = true;
    
    // Calculate path (simple direct movement)
    calculatePath();
}

public void update(double deltaTime, TerrainType[][] terrain) {
    if (!isMoving) return;
    
    // Calculate direction to target
    double dx = targetX - gridX;
    double dy = targetY - gridY;
    double distance = Math.sqrt(dx * dx + dy * dy);
    
    if (distance < 0.1) {
        // Arrived at destination
        gridX = targetX;
        gridY = targetY;
        isMoving = false;
        return;
    }
    
    // Apply terrain speed modifier
    TerrainType currentTerrain = terrain[(int)gridX][(int)gridY];
    double terrainModifier = getTerrainSpeedModifier(currentTerrain);
    
    // Calculate actual movement
    double moveSpeed = currentSpeed * terrainModifier * deltaTime;
    double moveX = (dx / distance) * moveSpeed;
    double moveY = (dy / distance) * moveSpeed;
    
    gridX += moveX;
    gridY += moveY;
    
    // Update facing direction
    updateFacing(dx, dy);
    
    // Update animation
    updateWalkAnimation(deltaTime);
}

private double getTerrainSpeedModifier(TerrainType terrain) {
    return switch (terrain) {
        case GRASSLAND, BEACH -> 1.0;
        case FOREST -> 0.7;
        case HILLS -> 0.5;
        case SHALLOW_WATER -> 0.3;
        default -> 0.8;
    };
}
```

### Direction and Facing
```java
public enum Direction {
    NORTH, SOUTH, EAST, WEST,
    NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
}

private void updateFacing(double dx, double dy) {
    // 8-directional facing
    double angle = Math.atan2(dy, dx);
    
    if (angle >= -Math.PI/8 && angle < Math.PI/8) {
        facing = Direction.EAST;
    } else if (angle >= Math.PI/8 && angle < 3*Math.PI/8) {
        facing = Direction.SOUTH_EAST;
    } else if (angle >= 3*Math.PI/8 && angle < 5*Math.PI/8) {
        facing = Direction.SOUTH;
    } else if (angle >= 5*Math.PI/8 && angle < 7*Math.PI/8) {
        facing = Direction.SOUTH_WEST;
    } else if (angle >= 7*Math.PI/8 || angle < -7*Math.PI/8) {
        facing = Direction.WEST;
    } else if (angle >= -7*Math.PI/8 && angle < -5*Math.PI/8) {
        facing = Direction.NORTH_WEST;
    } else if (angle >= -5*Math.PI/8 && angle < -3*Math.PI/8) {
        facing = Direction.NORTH;
    } else {
        facing = Direction.NORTH_EAST;
    }
}
```

---

## Character Stats

### Stats System
```java
public class CharacterStats {
    public enum Stat {
        STR("Strength", "Physical power and melee damage"),
        DEX("Dexterity", "Agility, accuracy, and dodge"),
        CON("Constitution", "Health and endurance"),
        INT("Intelligence", "Magic power and learning"),
        WIS("Wisdom", "Perception and willpower"),
        CHA("Charisma", "Influence and leadership"),
        LCK("Luck", "Fortune and critical chance");
    }
    
    private int[] baseStats;      // 1-20 scale
    private int[] tempModifiers;  // From buffs/debuffs
    
    // Derived stats
    public int getMaxHealth() {
        return getStat(Stat.CON) * 10 + getStat(Stat.STR) * 2;
    }
    
    public int getMaxStamina() {
        return getStat(Stat.CON) * 5 + getStat(Stat.DEX) * 3;
    }
    
    public int getMeleeDamage() {
        return getStat(Stat.STR) * 2 + getStat(Stat.DEX) / 2;
    }
    
    public int getDodgeChance() {
        return getStat(Stat.DEX) * 2 + getStat(Stat.LCK);
    }
    
    public int getMaxPartySize() {
        return 1 + getStat(Stat.CHA) / 3;
    }
}
```

### Stat Generation
```java
// Random generation using 3d6 (bell curve, 3-18 range)
public static CharacterStats generateRandom() {
    CharacterStats stats = new CharacterStats();
    for (int i = 0; i < Stat.values().length; i++) {
        stats.baseStats[i] = roll3d6();  // 3-18
    }
    return stats;
}

// Profession-biased generation
public static CharacterStats generateForProfession(Stat primary, Stat secondary) {
    CharacterStats stats = generateRandom();
    stats.baseStats[primary.ordinal()] = Math.min(20, stats.baseStats[primary.ordinal()] + 2);
    stats.baseStats[secondary.ordinal()] = Math.min(20, stats.baseStats[secondary.ordinal()] + 1);
    return stats;
}

private static int roll3d6() {
    return (random.nextInt(6) + 1) + (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
}
```

---

## Inventory System

### Inventory Class
```java
public class Inventory {
    private static final int MAX_SLOTS = 40;
    private Item[] slots;
    
    public Inventory() {
        slots = new Item[MAX_SLOTS];
    }
    
    // Add item to inventory
    public boolean addItem(Item item) {
        // First, try to stack with existing
        if (item.isStackable()) {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != null && slots[i].canStackWith(item)) {
                    slots[i].addToStack(item.getQuantity());
                    return true;
                }
            }
        }
        
        // Find empty slot
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                return true;
            }
        }
        
        return false;  // Inventory full
    }
    
    // Remove item
    public Item removeItem(int slot) {
        if (slot < 0 || slot >= slots.length) return null;
        Item item = slots[slot];
        slots[slot] = null;
        return item;
    }
    
    // Get total weight
    public double getTotalWeight() {
        double weight = 0;
        for (Item item : slots) {
            if (item != null) {
                weight += item.getWeight() * item.getQuantity();
            }
        }
        return weight;
    }
}
```

### Item Class
```java
public class Item {
    private String id;
    private String name;
    private String description;
    private ItemType type;
    private int quantity;
    private int maxStack;
    private double weight;
    private int value;  // Base gold value
    
    // For equipment
    private EquipmentSlot slot;
    private Map<String, Integer> stats;  // Stat bonuses
    
    public enum ItemType {
        WEAPON, ARMOR, CONSUMABLE, MATERIAL, QUEST, MISC
    }
    
    public boolean isStackable() {
        return type == ItemType.MATERIAL || 
               type == ItemType.CONSUMABLE ||
               type == ItemType.MISC;
    }
}
```

### Item Registry
```java
public class ItemRegistry {
    private static final Map<String, Item> ITEMS = new HashMap<>();
    
    static {
        // Register all items
        register(new Item("iron_ore", "Iron Ore", ItemType.MATERIAL)
            .setWeight(1.0).setValue(5).setMaxStack(99));
        
        register(new Item("health_potion", "Health Potion", ItemType.CONSUMABLE)
            .setWeight(0.2).setValue(25).setMaxStack(20));
        
        register(new Item("iron_sword", "Iron Sword", ItemType.WEAPON)
            .setWeight(3.0).setValue(50).setSlot(EquipmentSlot.MAIN_HAND)
            .addStat("attack", 10));
    }
    
    public static Item create(String id) {
        Item template = ITEMS.get(id);
        return template != null ? template.clone() : null;
    }
}
```

---

## Equipment System

### Equipment Slots
```java
public enum EquipmentSlot {
    HEAD("Head", "Helmets, hats"),
    CHEST("Chest", "Armor, robes"),
    LEGS("Legs", "Pants, greaves"),
    FEET("Feet", "Boots, shoes"),
    HANDS("Hands", "Gloves, gauntlets"),
    MAIN_HAND("Main Hand", "Weapons"),
    OFF_HAND("Off Hand", "Shields, torches"),
    RING_1("Ring 1", "Rings"),
    RING_2("Ring 2", "Rings"),
    AMULET("Amulet", "Necklaces");
}

public class Equipment {
    private Map<EquipmentSlot, Item> equipped;
    
    public Equipment() {
        equipped = new EnumMap<>(EquipmentSlot.class);
    }
    
    // Equip item
    public Item equip(Item item) {
        if (item.getSlot() == null) return item;  // Not equippable
        
        Item previous = equipped.get(item.getSlot());
        equipped.put(item.getSlot(), item);
        return previous;  // Return unequipped item
    }
    
    // Get total stat bonus from equipment
    public int getTotalStat(String stat) {
        int total = 0;
        for (Item item : equipped.values()) {
            if (item != null) {
                total += item.getStat(stat);
            }
        }
        return total;
    }
    
    // Get armor rating
    public int getArmorRating() {
        return getTotalStat("armor") + getTotalStat("defense");
    }
}
```

### Rendering Equipped Items
```java
// In PlayerSprite.render()
private void renderEquipment(GraphicsContext gc, double x, double y, double size) {
    // Render in order: back items, body, front items
    
    // Cape (back)
    Item cape = equipment.get(EquipmentSlot.CAPE);
    if (cape != null) {
        renderCape(gc, cape, x, y, size);
    }
    
    // Chest armor over body
    Item chest = equipment.get(EquipmentSlot.CHEST);
    if (chest != null) {
        gc.setFill(getItemColor(chest));
        gc.fillRect(x + size * 0.2, y + size * 0.35, size * 0.6, size * 0.35);
    }
    
    // Helmet
    Item helmet = equipment.get(EquipmentSlot.HEAD);
    if (helmet != null) {
        renderHelmet(gc, helmet, x, y, size);
    }
    
    // Weapon in hand
    Item weapon = equipment.get(EquipmentSlot.MAIN_HAND);
    if (weapon != null) {
        renderWeapon(gc, weapon, x, y, size);
    }
}
```

---

## Party System

### Party Class
```java
public class Party {
    private List<PartyMember> members;
    private int maxSize;
    
    public Party(int maxSize) {
        this.members = new ArrayList<>();
        this.maxSize = maxSize;
    }
    
    public boolean addMember(PartyMember member) {
        if (members.size() >= maxSize) return false;
        members.add(member);
        return true;
    }
    
    public void removeMember(PartyMember member) {
        members.remove(member);
    }
    
    // Calculate party combat power
    public int getTotalCombatPower() {
        return members.stream()
            .mapToInt(PartyMember::getCombatPower)
            .sum();
    }
    
    // Get party carry capacity
    public double getTotalCarryCapacity() {
        return members.stream()
            .mapToDouble(m -> 50 + m.getStrength() * 5)
            .sum();
    }
}
```

### PartyMember Class
```java
public class PartyMember {
    private String name;
    private boolean isMale;
    private Role role;
    private String professionName;
    
    // Stats
    private int attack, defense, speed;
    private int maxHealth, currentHealth;
    private int morale;  // 0-100
    
    // Equipment
    private Equipment equipment;
    
    public enum Role {
        WARRIOR("Warrior", "Front-line fighter"),
        ARCHER("Archer", "Ranged attacker"),
        MAGE("Mage", "Magic user"),
        HEALER("Healer", "Support and healing"),
        SCOUT("Scout", "Fast and evasive"),
        MERCHANT("Merchant", "Trading bonuses");
        
        public String getName() { ... }
    }
    
    // Combat calculations
    public int getCombatPower() {
        return attack + defense + speed + equipment.getTotalStat("attack");
    }
    
    public int dealDamage() {
        int baseDamage = attack;
        int weaponDamage = equipment.getTotalStat("attack");
        int critBonus = random.nextInt(100) < 10 ? baseDamage : 0;  // 10% crit
        return baseDamage + weaponDamage + critBonus;
    }
}
```

---

## Code Examples

### Complete Player Rendering
```java
public void render(GraphicsContext gc, double screenX, double screenY, double size) {
    // Shadow
    gc.setFill(Color.color(0, 0, 0, 0.25));
    gc.fillOval(screenX + size * 0.1, screenY + size * 0.85, size * 0.8, size * 0.15);
    
    // Body parts (from back to front)
    double bodyY = screenY + size * 0.3;
    
    // Back arm (if facing certain direction)
    if (facing == Direction.EAST || facing == Direction.SOUTH_EAST) {
        renderArm(gc, leftArm, screenX - size * 0.1, bodyY, size * 0.15, size * 0.3);
    }
    
    // Legs (animated)
    double legOffset = isMoving ? Math.sin(animFrame * Math.PI / 2) * size * 0.05 : 0;
    renderLeg(gc, leftLeg, screenX + size * 0.25, bodyY + size * 0.35 + legOffset, size * 0.15, size * 0.3);
    renderLeg(gc, rightLeg, screenX + size * 0.45, bodyY + size * 0.35 - legOffset, size * 0.15, size * 0.3);
    
    // Body/chest
    renderTorso(gc, chest, screenX + size * 0.2, bodyY, size * 0.6, size * 0.4);
    
    // Front arm
    if (facing != Direction.EAST && facing != Direction.SOUTH_EAST) {
        renderArm(gc, rightArm, screenX + size * 0.6, bodyY, size * 0.15, size * 0.3);
    }
    
    // Head
    renderHead(gc, head, screenY, size * 0.3);
    
    // Equipment overlay
    renderEquipment(gc, screenX, screenY, size);
    
    // Name tag (if enabled)
    if (showNameTag) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(size * 0.15));
        gc.fillText(name, screenX, screenY - 5);
    }
}
```

### Inventory UI Integration
```java
public class InventoryUI {
    private Inventory inventory;
    private int selectedSlot = -1;
    
    public void render(GraphicsContext gc) {
        // Draw grid of slots
        for (int i = 0; i < inventory.getSize(); i++) {
            int row = i / COLS;
            int col = i % COLS;
            double x = startX + col * SLOT_SIZE;
            double y = startY + row * SLOT_SIZE;
            
            // Slot background
            gc.setFill(i == selectedSlot ? SELECTED_COLOR : SLOT_COLOR);
            gc.fillRect(x, y, SLOT_SIZE - 2, SLOT_SIZE - 2);
            
            // Item icon
            Item item = inventory.getItem(i);
            if (item != null) {
                renderItemIcon(gc, item, x + 2, y + 2, SLOT_SIZE - 4);
                
                // Stack count
                if (item.getQuantity() > 1) {
                    gc.setFill(Color.WHITE);
                    gc.fillText(String.valueOf(item.getQuantity()), 
                               x + SLOT_SIZE - 15, y + SLOT_SIZE - 5);
                }
            }
        }
    }
    
    // Drag and drop
    public void onDragStart(int slot) {
        draggedItem = inventory.getItem(slot);
        dragSourceSlot = slot;
    }
    
    public void onDrop(int targetSlot) {
        if (draggedItem != null) {
            inventory.swapSlots(dragSourceSlot, targetSlot);
            draggedItem = null;
        }
    }
}
```

### Party Management
```java
// Adding party member from tavern
public void hireNPC(TavernNPC npc) {
    if (party.isFull()) {
        showMessage("Party is full!");
        return;
    }
    
    if (!currency.canAfford(npc.getHireCost())) {
        showMessage("Not enough gold!");
        return;
    }
    
    currency.subtract(npc.getHireCost());
    
    PartyMember member = new PartyMember(npc.getName(), npc.getRole());
    member.setStatsFromCharacterStats(npc.getStats());
    
    party.addMember(member);
    showMessage(npc.getName() + " joined your party!");
}

// Dismissing party member
public void dismissMember(PartyMember member) {
    if (party.getSize() <= 1) {
        showMessage("Can't dismiss your last party member!");
        return;
    }
    
    party.removeMember(member);
    showMessage(member.getName() + " left the party.");
}
```

---

## Tips for Player Systems

### Performance
```java
// Cache stat calculations
private int cachedCombatPower = -1;
private boolean statsDirty = true;

public int getCombatPower() {
    if (statsDirty) {
        cachedCombatPower = calculateCombatPower();
        statsDirty = false;
    }
    return cachedCombatPower;
}

// Mark dirty when equipment changes
public void onEquipmentChanged() {
    statsDirty = true;
}
```

### Save/Load
```java
// Serialize player state
public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", name);
    json.addProperty("gridX", gridX);
    json.addProperty("gridY", gridY);
    json.add("stats", characterStats.toJson());
    json.add("inventory", inventory.toJson());
    json.add("equipment", equipment.toJson());
    json.add("party", party.toJson());
    json.add("currency", currency.toJson());
    return json;
}

public static PlayerSprite fromJson(JsonObject json) {
    PlayerSprite player = new PlayerSprite();
    player.name = json.get("name").getAsString();
    player.gridX = json.get("gridX").getAsDouble();
    player.gridY = json.get("gridY").getAsDouble();
    player.characterStats = CharacterStats.fromJson(json.get("stats"));
    player.inventory = Inventory.fromJson(json.get("inventory"));
    player.equipment = Equipment.fromJson(json.get("equipment"));
    player.party = Party.fromJson(json.get("party"));
    player.currency = Currency.fromJson(json.get("currency"));
    return player;
}
```

---

## Next: [06_ECONOMY_SYSTEM.md](06_ECONOMY_SYSTEM.md)
