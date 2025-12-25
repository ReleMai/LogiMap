# LogiMap Developer Guide - Adding New Features

## Table of Contents
1. [Planning a Feature](#planning-a-feature)
2. [Adding New Terrain Types](#adding-new-terrain-types)
3. [Creating New NPC Types](#creating-new-npc-types)
4. [Adding New Item Categories](#adding-new-item-categories)
5. [Implementing New Buildings](#implementing-new-buildings)
6. [Adding New Game Mechanics](#adding-new-game-mechanics)
7. [Testing Your Feature](#testing-your-feature)

---

## Planning a Feature

### Step 1: Identify Components
Before coding, identify which systems your feature touches:

```
Feature: "Add a new merchant NPC type"

Components Affected:
├── NPC.java          (Base NPC class)
├── NPCManager.java   (Spawning, management)
├── Inventory.java    (Trading system)
├── Economy           (Pricing)
└── UI                (Trading interface)
```

### Step 2: Design the Data Flow
```
User clicks NPC → InteractionMenu opens → Trade option selected
    ↓
NPCManager.getSelectedNPC() → Merchant.openShop()
    ↓
TradingUI displays inventory ← Merchant.getInventory()
    ↓
User selects item → validatePurchase() → transaction
    ↓
Update both inventories, refresh UI
```

### Step 3: Write Interfaces First
```java
// Define the contract before implementing
public interface Tradeable {
    Inventory getShopInventory();
    double getBuyPriceModifier();
    double getSellPriceModifier();
    boolean willBuy(Item item);
    boolean willSell(Item item);
}
```

---

## Adding New Terrain Types

### Step 1: Add to TerrainType Enum
```java
// TerrainType.java
public enum TerrainType {
    GRASS(0.8, true, 1.0),
    FOREST(0.6, true, 1.5),
    MOUNTAIN(0.3, false, 3.0),
    WATER(0.0, false, 999),
    DESERT(0.7, true, 1.2),
    SWAMP(0.4, true, 2.0),
    // Add new terrain here:
    VOLCANIC(0.1, false, 4.0);  // NEW
    
    private final double fertility;
    private final boolean buildable;
    private final double movementCost;
    
    TerrainType(double fertility, boolean buildable, double movementCost) {
        this.fertility = fertility;
        this.buildable = buildable;
        this.movementCost = movementCost;
    }
    
    // Getters...
}
```

### Step 2: Update Terrain Generation
```java
// TerrainGenerator.java
private TerrainType determineTerrainType(double height, double moisture, double temp) {
    // Existing logic...
    
    // Add volcanic terrain near extreme heat and low moisture
    if (temp > 0.9 && moisture < 0.2 && height > 0.6) {
        return TerrainType.VOLCANIC;
    }
    
    // Rest of terrain logic...
}
```

### Step 3: Add Rendering
```java
// MapCanvas.java - in renderTerrain()
private Color getTerrainColor(TerrainType type) {
    switch (type) {
        case GRASS: return Color.rgb(76, 153, 0);
        case FOREST: return Color.rgb(34, 102, 51);
        case MOUNTAIN: return Color.rgb(139, 137, 137);
        case WATER: return Color.rgb(64, 164, 223);
        case DESERT: return Color.rgb(237, 201, 175);
        case SWAMP: return Color.rgb(47, 79, 47);
        case VOLCANIC: return Color.rgb(139, 69, 19);  // NEW
        default: return Color.GRAY;
    }
}
```

### Step 4: Add Special Effects (Optional)
```java
// MapCanvas.java - add volcanic particle effects
private void renderVolcanicEffects(GraphicsContext gc, int x, int y, double screenX, double screenY) {
    if (world.getTerrain(x, y) == TerrainType.VOLCANIC && Math.random() < 0.05) {
        // Smoke particles
        gc.setFill(Color.rgb(80, 80, 80, 0.5));
        double offsetX = Math.random() * tileSize;
        double offsetY = Math.random() * tileSize;
        gc.fillOval(screenX + offsetX, screenY + offsetY - animOffset, 4, 4);
    }
}
```

---

## Creating New NPC Types

### Step 1: Create the NPC Class
```java
// Merchant.java
public class Merchant extends NPC implements Tradeable {
    private Inventory shopInventory;
    private MerchantType merchantType;
    private double priceModifier;
    private long lastRestockTime;
    private static final long RESTOCK_INTERVAL = 24 * 60 * 1000; // 24 game hours
    
    public enum MerchantType {
        GENERAL,    // Buys/sells everything
        BLACKSMITH, // Weapons and armor
        ALCHEMIST,  // Potions and ingredients
        JEWELER     // Accessories and gems
    }
    
    public Merchant(String name, MerchantType type, double x, double y) {
        super(name, NPCType.MERCHANT, x, y);
        this.merchantType = type;
        this.priceModifier = 0.9 + Math.random() * 0.2; // 0.9 to 1.1
        this.shopInventory = new Inventory(50);
        
        generateInitialStock();
    }
    
    private void generateInitialStock() {
        switch (merchantType) {
            case GENERAL:
                shopInventory.addItem(new Item("Bread", ItemType.CONSUMABLE, 5));
                shopInventory.addItem(new Item("Rope", ItemType.TOOL, 15));
                shopInventory.addItem(new Item("Torch", ItemType.TOOL, 10));
                break;
            case BLACKSMITH:
                shopInventory.addItem(new Item("Iron Sword", ItemType.WEAPON, 100));
                shopInventory.addItem(new Item("Steel Shield", ItemType.ARMOR, 80));
                shopInventory.addItem(new Item("Chainmail", ItemType.ARMOR, 150));
                break;
            // More types...
        }
    }
    
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        
        // Check for restock
        if (System.currentTimeMillis() - lastRestockTime > RESTOCK_INTERVAL) {
            restockShop();
        }
    }
    
    private void restockShop() {
        // Replenish sold items
        generateInitialStock();
        lastRestockTime = System.currentTimeMillis();
    }
    
    // Tradeable interface implementation
    @Override
    public Inventory getShopInventory() {
        return shopInventory;
    }
    
    @Override
    public double getBuyPriceModifier() {
        return priceModifier * 1.2; // Sells at 120% base
    }
    
    @Override
    public double getSellPriceModifier() {
        return priceModifier * 0.5; // Buys at 50% base
    }
    
    @Override
    public boolean willBuy(Item item) {
        switch (merchantType) {
            case GENERAL: return true;
            case BLACKSMITH: 
                return item.getType() == ItemType.WEAPON || 
                       item.getType() == ItemType.ARMOR;
            case ALCHEMIST:
                return item.getType() == ItemType.CONSUMABLE ||
                       item.getType() == ItemType.INGREDIENT;
            case JEWELER:
                return item.getType() == ItemType.ACCESSORY;
            default: return false;
        }
    }
    
    @Override
    public boolean willSell(Item item) {
        return shopInventory.contains(item);
    }
}
```

### Step 2: Register with NPCManager
```java
// NPCManager.java
public void spawnMerchants(Town town) {
    // Spawn merchants based on town size
    int numMerchants = Math.max(1, town.getPopulation() / 100);
    
    List<Merchant.MerchantType> types = new ArrayList<>(
        Arrays.asList(Merchant.MerchantType.values())
    );
    Collections.shuffle(types);
    
    for (int i = 0; i < numMerchants && i < types.size(); i++) {
        double x = town.getX() + (Math.random() - 0.5) * town.getRadius();
        double y = town.getY() + (Math.random() - 0.5) * town.getRadius();
        
        String name = NameGenerator.generateMerchantName();
        Merchant merchant = new Merchant(name, types.get(i), x, y);
        merchant.setHomeTown(town);
        
        addNPC(merchant);
    }
}
```

### Step 3: Add Interaction
```java
// InteractionMenu.java
private void setupNPCInteraction(NPC npc) {
    clearOptions();
    
    addOption("Talk", () -> startDialogue(npc));
    
    if (npc instanceof Tradeable) {
        addOption("Trade", () -> openTradeWindow((Tradeable) npc));
    }
    
    if (npc instanceof Recruitable) {
        addOption("Recruit", () -> attemptRecruit((Recruitable) npc));
    }
}
```

---

## Adding New Item Categories

### Step 1: Define Item Type
```java
// ItemType.java
public enum ItemType {
    WEAPON,
    ARMOR,
    ACCESSORY,
    CONSUMABLE,
    INGREDIENT,
    TOOL,
    QUEST_ITEM,
    // New type:
    MOUNT       // NEW
}
```

### Step 2: Create Item Subclass
```java
// Mount.java
public class Mount extends Item {
    private double speedBonus;
    private double staminaMax;
    private double currentStamina;
    private MountType mountType;
    
    public enum MountType {
        HORSE(1.5, 100),
        DONKEY(1.2, 80),
        WAR_HORSE(1.8, 120),
        CAMEL(1.3, 150);  // High stamina for desert
        
        final double speedBonus;
        final double staminaMax;
        
        MountType(double speedBonus, double staminaMax) {
            this.speedBonus = speedBonus;
            this.staminaMax = staminaMax;
        }
    }
    
    public Mount(String name, MountType type, int basePrice) {
        super(name, ItemType.MOUNT, basePrice);
        this.mountType = type;
        this.speedBonus = type.speedBonus;
        this.staminaMax = type.staminaMax;
        this.currentStamina = staminaMax;
    }
    
    public void useStamina(double amount) {
        currentStamina = Math.max(0, currentStamina - amount);
    }
    
    public void rest(double amount) {
        currentStamina = Math.min(staminaMax, currentStamina + amount);
    }
    
    public boolean canRide() {
        return currentStamina > 0;
    }
    
    // Getters...
}
```

### Step 3: Integrate with Player
```java
// PlayerSprite.java
private Mount currentMount;

public void mountUp(Mount mount) {
    if (mount != null && mount.canRide()) {
        this.currentMount = mount;
        this.speedMultiplier = mount.getSpeedBonus();
        // Change sprite to mounted version
    }
}

public void dismount() {
    this.currentMount = null;
    this.speedMultiplier = 1.0;
}

@Override
public void update(double deltaTime) {
    super.update(deltaTime);
    
    if (currentMount != null && isMoving()) {
        currentMount.useStamina(deltaTime * 0.1);
        
        if (!currentMount.canRide()) {
            dismount();
            showMessage("Your mount is exhausted!");
        }
    } else if (currentMount != null && !isMoving()) {
        currentMount.rest(deltaTime * 0.5);
    }
}
```

---

## Implementing New Buildings

### Step 1: Create Building Class
```java
// Stable.java
public class Stable extends MapStructure {
    private List<Mount> availableMounts;
    private int maxCapacity;
    private Town parentTown;
    
    public Stable(double x, double y, Town town) {
        super("Stable", x, y, 2, 2); // 2x2 tiles
        this.parentTown = town;
        this.maxCapacity = 5;
        this.availableMounts = new ArrayList<>();
        
        generateMounts();
    }
    
    private void generateMounts() {
        // Generate based on town wealth and location
        int numMounts = 1 + random.nextInt(maxCapacity);
        
        for (int i = 0; i < numMounts; i++) {
            Mount.MountType type = selectMountType();
            String name = NameGenerator.generateHorseName();
            int price = calculatePrice(type);
            
            availableMounts.add(new Mount(name, type, price));
        }
    }
    
    private Mount.MountType selectMountType() {
        // Desert towns have camels
        if (parentTown.getBiome() == BiomeType.DESERT) {
            return Mount.MountType.CAMEL;
        }
        
        // Wealthy towns have war horses
        if (parentTown.getWealth() > 5000 && Math.random() < 0.3) {
            return Mount.MountType.WAR_HORSE;
        }
        
        return Math.random() < 0.7 ? Mount.MountType.HORSE : Mount.MountType.DONKEY;
    }
    
    @Override
    public void render(GraphicsContext gc, double screenX, double screenY, double scale) {
        // Draw stable building
        gc.setFill(Color.SADDLEBROWN);
        gc.fillRect(screenX, screenY, getWidth() * scale, getHeight() * scale);
        
        // Draw roof
        gc.setFill(Color.DARKRED);
        double[] xPoints = {screenX, screenX + getWidth() * scale / 2, screenX + getWidth() * scale};
        double[] yPoints = {screenY, screenY - 10 * scale, screenY};
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Draw horse icon
        gc.setFill(Color.TAN);
        gc.fillOval(screenX + 5, screenY + 5, 10 * scale, 8 * scale);
    }
    
    @Override
    public InteractionResult interact(PlayerSprite player) {
        return new InteractionResult(InteractionType.OPEN_MENU, this);
    }
}
```

### Step 2: Add to Town Generation
```java
// Town.java
private void generateBuildings() {
    // Existing buildings...
    
    // Add stable if town is large enough
    if (population >= 200) {
        double stableX = centerX + (Math.random() - 0.5) * radius * 0.8;
        double stableY = centerY + (Math.random() - 0.5) * radius * 0.8;
        
        buildings.add(new Stable(stableX, stableY, this));
    }
}
```

---

## Adding New Game Mechanics

### Example: Reputation System

#### Step 1: Create Core Class
```java
// ReputationManager.java
public class ReputationManager {
    private Map<String, Integer> factionReputation;
    private Map<String, Integer> townReputation;
    
    public enum ReputationLevel {
        HATED(-1000, -500),
        HOSTILE(-499, -100),
        UNFRIENDLY(-99, -1),
        NEUTRAL(0, 99),
        FRIENDLY(100, 499),
        HONORED(500, 999),
        REVERED(1000, Integer.MAX_VALUE);
        
        final int min, max;
        ReputationLevel(int min, int max) {
            this.min = min;
            this.max = max;
        }
        
        public static ReputationLevel fromValue(int value) {
            for (ReputationLevel level : values()) {
                if (value >= level.min && value <= level.max) {
                    return level;
                }
            }
            return NEUTRAL;
        }
    }
    
    public ReputationManager() {
        factionReputation = new HashMap<>();
        townReputation = new HashMap<>();
    }
    
    public void modifyFactionRep(String faction, int amount) {
        int current = factionReputation.getOrDefault(faction, 0);
        factionReputation.put(faction, Math.max(-1000, Math.min(1000, current + amount)));
        
        // Trigger events based on reputation changes
        checkReputationEvents(faction);
    }
    
    public ReputationLevel getFactionLevel(String faction) {
        return ReputationLevel.fromValue(factionReputation.getOrDefault(faction, 0));
    }
    
    public double getPriceModifier(String faction) {
        ReputationLevel level = getFactionLevel(faction);
        switch (level) {
            case HATED: return 2.0;    // Double prices
            case HOSTILE: return 1.5;
            case UNFRIENDLY: return 1.2;
            case NEUTRAL: return 1.0;
            case FRIENDLY: return 0.95;
            case HONORED: return 0.85;
            case REVERED: return 0.75; // 25% discount
            default: return 1.0;
        }
    }
    
    public boolean canEnterTown(Town town) {
        ReputationLevel level = getFactionLevel(town.getFaction());
        return level != ReputationLevel.HATED && level != ReputationLevel.HOSTILE;
    }
    
    private void checkReputationEvents(String faction) {
        ReputationLevel level = getFactionLevel(faction);
        
        // Unlock special quests at high reputation
        if (level == ReputationLevel.HONORED) {
            QuestManager.getInstance().unlockFactionQuests(faction);
        }
        
        // Become kill-on-sight at very low reputation
        if (level == ReputationLevel.HATED) {
            NPCManager.getInstance().setHostile(faction, true);
        }
    }
}
```

#### Step 2: Integrate with Existing Systems
```java
// PlayerSprite.java
private ReputationManager reputation;

public PlayerSprite(...) {
    // ...
    this.reputation = new ReputationManager();
}

// When completing a trade
public void completeTrade(Tradeable merchant, Item item, boolean buying) {
    Town town = merchant.getHomeTown();
    if (town != null) {
        // Small reputation gain for trading
        reputation.modifyFactionRep(town.getFaction(), 1);
    }
}

// Merchant.java - use reputation for prices
public int getFinalPrice(Item item, PlayerSprite player, boolean buying) {
    double basePrice = item.getBasePrice();
    double repModifier = player.getReputation().getPriceModifier(homeTown.getFaction());
    
    if (buying) {
        return (int)(basePrice * getBuyPriceModifier() * repModifier);
    } else {
        return (int)(basePrice * getSellPriceModifier() / repModifier);
    }
}
```

#### Step 3: Add UI Display
```java
// ReputationPanel.java
public class ReputationPanel extends FloatingPanel {
    public ReputationPanel(ReputationManager reputation) {
        super("Reputation");
        
        VBox content = new VBox(10);
        
        for (String faction : reputation.getKnownFactions()) {
            HBox row = new HBox(10);
            
            Label factionLabel = new Label(faction);
            factionLabel.setPrefWidth(100);
            
            ReputationLevel level = reputation.getFactionLevel(faction);
            Label levelLabel = new Label(level.toString());
            levelLabel.setStyle("-fx-text-fill: " + getRepColor(level));
            
            ProgressBar repBar = new ProgressBar();
            repBar.setProgress(reputation.getRepProgress(faction));
            repBar.setPrefWidth(150);
            
            row.getChildren().addAll(factionLabel, levelLabel, repBar);
            content.getChildren().add(row);
        }
        
        addContent(content);
    }
    
    private String getRepColor(ReputationLevel level) {
        switch (level) {
            case HATED: return "#ff0000";
            case HOSTILE: return "#ff6600";
            case UNFRIENDLY: return "#ffcc00";
            case NEUTRAL: return "#ffffff";
            case FRIENDLY: return "#99ff99";
            case HONORED: return "#66ff66";
            case REVERED: return "#00ff00";
            default: return "#ffffff";
        }
    }
}
```

---

## Testing Your Feature

### Unit Testing Pattern
```java
// Test file: MerchantTest.java
public class MerchantTest {
    
    @Test
    public void testMerchantCreation() {
        Merchant merchant = new Merchant("Test", Merchant.MerchantType.BLACKSMITH, 0, 0);
        
        assertNotNull(merchant.getShopInventory());
        assertTrue(merchant.getShopInventory().getItemCount() > 0);
    }
    
    @Test
    public void testBlacksmithOnlyBuysWeapons() {
        Merchant blacksmith = new Merchant("Smith", Merchant.MerchantType.BLACKSMITH, 0, 0);
        
        Item sword = new Item("Sword", ItemType.WEAPON, 100);
        Item bread = new Item("Bread", ItemType.CONSUMABLE, 5);
        
        assertTrue(blacksmith.willBuy(sword));
        assertFalse(blacksmith.willBuy(bread));
    }
    
    @Test
    public void testPriceModifiers() {
        Merchant merchant = new Merchant("Test", Merchant.MerchantType.GENERAL, 0, 0);
        
        // Buy price should be higher than base
        assertTrue(merchant.getBuyPriceModifier() > 1.0);
        
        // Sell price should be lower than base
        assertTrue(merchant.getSellPriceModifier() < 1.0);
    }
}
```

### Integration Testing
```java
// Test the full flow
public class TradingIntegrationTest {
    
    @Test
    public void testCompleteTrade() {
        // Setup
        PlayerSprite player = new PlayerSprite(0, 0);
        player.getCurrency().add(1000);
        
        Merchant merchant = new Merchant("Test", Merchant.MerchantType.GENERAL, 0, 0);
        Item item = merchant.getShopInventory().getItem(0);
        int price = merchant.getBuyPrice(item);
        
        // Execute trade
        boolean success = TradingSystem.buy(player, merchant, item, 1);
        
        // Verify
        assertTrue(success);
        assertTrue(player.getInventory().contains(item));
        assertEquals(1000 - price, player.getCurrency().getTotal());
    }
}
```

### Debug Checklist
```
□ Does the feature compile without errors?
□ Does the feature work in isolation?
□ Does the feature integrate with existing systems?
□ Are there edge cases that cause crashes?
□ Does the UI update correctly?
□ Is the feature saved/loaded properly?
□ Does the feature affect performance?
□ Is the code documented?
```

---

## Feature Development Workflow

```
1. PLAN
   └── Identify affected systems
   └── Design data structures
   └── Write interfaces

2. IMPLEMENT
   └── Core logic first
   └── Integration points
   └── UI components

3. TEST
   └── Unit tests
   └── Integration tests
   └── Manual testing

4. REFINE
   └── Performance optimization
   └── Code cleanup
   └── Documentation

5. COMMIT
   └── Descriptive commit message
   └── Update changelog
   └── Version bump if needed
```

---

## Quick Reference: Common Modifications

| Task | Files to Modify |
|------|-----------------|
| New terrain | TerrainType.java, TerrainGenerator.java, MapCanvas.java |
| New NPC type | NPC.java (or subclass), NPCManager.java, InteractionMenu.java |
| New item | ItemType.java (optional), Item.java (or subclass) |
| New building | MapStructure.java (or subclass), Town.java |
| New UI panel | FloatingPanel.java (or subclass), LogiMapUI.java |
| New mechanic | New class + integrate with PlayerSprite, World, UI |

---

## Congratulations!

You now have a comprehensive understanding of the LogiMap codebase and how to extend it. Remember:

- **Start small**: Test each component before integrating
- **Read existing code**: The patterns are already established
- **Save often**: Use version control to track changes
- **Document**: Future you will thank present you

Happy coding! 🎮
