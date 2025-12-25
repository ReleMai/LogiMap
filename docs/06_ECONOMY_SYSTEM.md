# LogiMap Developer Guide - Economy System

## Table of Contents
1. [Currency System](#currency-system)
2. [Town Economy](#town-economy)
3. [Trading System](#trading-system)
4. [Marketplace](#marketplace)
5. [Price Dynamics](#price-dynamics)
6. [Code Examples](#code-examples)

---

## Currency System

### Currency Class
```java
public class Currency {
    // Medieval-style denominations
    private int gold;    // 1 gold = 100 silver
    private int silver;  // 1 silver = 100 copper
    private int copper;
    
    public Currency() {
        this.gold = 0;
        this.silver = 0;
        this.copper = 0;
    }
    
    public Currency(int gold, int silver, int copper) {
        this.gold = gold;
        this.silver = silver;
        this.copper = copper;
        normalize();
    }
    
    // Convert to total copper for calculations
    public int toCopper() {
        return gold * 10000 + silver * 100 + copper;
    }
    
    // Create from copper amount
    public static Currency fromCopper(int copper) {
        Currency c = new Currency();
        c.gold = copper / 10000;
        copper %= 10000;
        c.silver = copper / 100;
        c.copper = copper % 100;
        return c;
    }
    
    // Normalize values (carry over)
    private void normalize() {
        if (copper >= 100) {
            silver += copper / 100;
            copper %= 100;
        }
        if (silver >= 100) {
            gold += silver / 100;
            silver %= 100;
        }
    }
    
    // Add currency
    public void add(Currency other) {
        this.gold += other.gold;
        this.silver += other.silver;
        this.copper += other.copper;
        normalize();
    }
    
    // Subtract (returns false if insufficient)
    public boolean subtract(Currency cost) {
        int total = toCopper();
        int costTotal = cost.toCopper();
        
        if (total < costTotal) return false;
        
        Currency result = fromCopper(total - costTotal);
        this.gold = result.gold;
        this.silver = result.silver;
        this.copper = result.copper;
        return true;
    }
    
    public boolean canAfford(int copperCost) {
        return toCopper() >= copperCost;
    }
    
    // Display formatting
    public String toString() {
        if (gold > 0) {
            return String.format("%dg %ds %dc", gold, silver, copper);
        } else if (silver > 0) {
            return String.format("%ds %dc", silver, copper);
        }
        return String.format("%dc", copper);
    }
}
```

### Price Calculations
```java
public class PriceCalculator {
    // Base prices in copper
    private static final Map<String, Integer> BASE_PRICES = Map.of(
        "iron_ore", 50,
        "gold_ore", 500,
        "wheat", 20,
        "bread", 30,
        "iron_sword", 5000,
        "health_potion", 2500
    );
    
    // Calculate buy price (player buying from shop)
    public static int getBuyPrice(String itemId, Town town, PlayerSprite player) {
        int basePrice = BASE_PRICES.getOrDefault(itemId, 100);
        
        // Town supply modifier (less supply = higher price)
        double supplyModifier = town.getSupplyModifier(itemId);
        
        // Player charisma discount
        int charisma = player.getStats().getStat(CharacterStats.Stat.CHA);
        double charismaDiscount = 1.0 - (charisma * 0.01);  // 1% per CHA
        
        // Shop markup (shops always sell for more)
        double shopMarkup = 1.3;
        
        return (int) (basePrice * supplyModifier * charismaDiscount * shopMarkup);
    }
    
    // Calculate sell price (player selling to shop)
    public static int getSellPrice(String itemId, Town town, PlayerSprite player) {
        int basePrice = BASE_PRICES.getOrDefault(itemId, 100);
        
        // Town demand modifier (more demand = higher price)
        double demandModifier = town.getDemandModifier(itemId);
        
        // Player charisma bonus
        int charisma = player.getStats().getStat(CharacterStats.Stat.CHA);
        double charismaBonus = 1.0 + (charisma * 0.005);  // 0.5% per CHA
        
        // Shop markdown (shops always buy for less)
        double shopMarkdown = 0.5;
        
        return (int) (basePrice * demandModifier * charismaBonus * shopMarkdown);
    }
}
```

---

## Town Economy

### Town Economic Properties
```java
public class Town {
    // Economy
    private Map<String, Integer> inventory;   // Items in stock
    private Map<String, Double> supplyLevel;  // 0.5-2.0 multiplier
    private Map<String, Double> demandLevel;  // 0.5-2.0 multiplier
    private Currency treasury;
    
    // Specialization affects prices
    private TownType type;  // FISHING, FARMING, MINING, etc.
    
    public void initializeEconomy(List<ResourceNode> nearbyResources) {
        inventory = new HashMap<>();
        supplyLevel = new HashMap<>();
        demandLevel = new HashMap<>();
        treasury = new Currency(100, 0, 0);  // Start with 100 gold
        
        // Stock based on town type and nearby resources
        for (ResourceNode node : nearbyResources) {
            String itemId = node.getResourceType().getItemId();
            inventory.put(itemId, 50 + random.nextInt(100));
            supplyLevel.put(itemId, 1.2);  // Nearby = more supply
        }
        
        // Town type bonuses
        switch (type) {
            case FISHING_VILLAGE:
                inventory.put("fish", 100);
                supplyLevel.put("fish", 1.5);
                demandLevel.put("bread", 1.3);
                break;
            case MINING_TOWN:
                inventory.put("iron_ore", 80);
                inventory.put("stone", 150);
                supplyLevel.put("iron_ore", 1.5);
                demandLevel.put("food", 1.4);
                break;
            // ... etc
        }
    }
    
    public double getSupplyModifier(String itemId) {
        return supplyLevel.getOrDefault(itemId, 1.0);
    }
    
    public double getDemandModifier(String itemId) {
        return demandLevel.getOrDefault(itemId, 1.0);
    }
}
```

### Town Warehouse
```java
public class TownWarehouse {
    private Town town;
    private Map<ResourceType, Integer> storage;
    private int maxCapacity;
    
    public TownWarehouse(Town town) {
        this.town = town;
        this.storage = new EnumMap<>(ResourceType.class);
        this.maxCapacity = town.isMajor() ? 5000 : 2000;
    }
    
    // Deposit resources
    public boolean deposit(ResourceType type, int amount) {
        int current = getTotalStored();
        if (current + amount > maxCapacity) return false;
        
        storage.merge(type, amount, Integer::sum);
        return true;
    }
    
    // Withdraw resources
    public int withdraw(ResourceType type, int amount) {
        int available = storage.getOrDefault(type, 0);
        int toWithdraw = Math.min(amount, available);
        
        storage.put(type, available - toWithdraw);
        return toWithdraw;
    }
    
    // Get storage status
    public double getCapacityUsed() {
        return (double) getTotalStored() / maxCapacity;
    }
}
```

---

## Trading System

### Shop Interface
```java
public class ShopUI {
    private Town town;
    private PlayerSprite player;
    private List<ShopItem> shopItems;
    private List<PlayerItem> playerItems;
    
    public void open(Town town, PlayerSprite player) {
        this.town = town;
        this.player = player;
        
        // Populate shop inventory
        shopItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : town.getInventory().entrySet()) {
            if (entry.getValue() > 0) {
                int buyPrice = PriceCalculator.getBuyPrice(entry.getKey(), town, player);
                shopItems.add(new ShopItem(entry.getKey(), entry.getValue(), buyPrice));
            }
        }
        
        // Populate player sellable items
        playerItems = new ArrayList<>();
        for (Item item : player.getInventory().getAllItems()) {
            if (item.isTradeable()) {
                int sellPrice = PriceCalculator.getSellPrice(item.getId(), town, player);
                playerItems.add(new PlayerItem(item, sellPrice));
            }
        }
        
        refreshUI();
    }
    
    // Buy from shop
    public void buyItem(ShopItem item, int quantity) {
        int totalCost = item.getPrice() * quantity;
        
        if (!player.getCurrency().canAfford(totalCost)) {
            showMessage("Not enough money!");
            return;
        }
        
        if (item.getStock() < quantity) {
            showMessage("Not enough in stock!");
            return;
        }
        
        // Complete transaction
        player.getCurrency().subtract(Currency.fromCopper(totalCost));
        town.getTreasury().add(Currency.fromCopper(totalCost));
        town.removeFromInventory(item.getId(), quantity);
        
        Item purchased = ItemRegistry.create(item.getId());
        purchased.setQuantity(quantity);
        player.getInventory().addItem(purchased);
        
        refreshUI();
    }
    
    // Sell to shop
    public void sellItem(PlayerItem item, int quantity) {
        int totalValue = item.getSellPrice() * quantity;
        
        if (!town.getTreasury().canAfford(totalValue)) {
            showMessage("Shop can't afford this!");
            return;
        }
        
        // Complete transaction
        player.getInventory().removeItem(item.getItem(), quantity);
        player.getCurrency().add(Currency.fromCopper(totalValue));
        town.getTreasury().subtract(Currency.fromCopper(totalValue));
        town.addToInventory(item.getId(), quantity);
        
        refreshUI();
    }
}
```

### Trade Routes (Future)
```java
public class TradeRoute {
    private Town source;
    private Town destination;
    private String goodType;
    private int quantity;
    private double profitMargin;
    
    public TradeRoute(Town source, Town destination, String goodType) {
        this.source = source;
        this.destination = destination;
        this.goodType = goodType;
        calculateProfit();
    }
    
    private void calculateProfit() {
        int buyPrice = PriceCalculator.getBuyPrice(goodType, source, null);
        int sellPrice = PriceCalculator.getSellPrice(goodType, destination, null);
        profitMargin = (double) (sellPrice - buyPrice) / buyPrice;
    }
    
    public boolean isProfitable() {
        return profitMargin > 0.1;  // At least 10% profit
    }
}
```

---

## Marketplace

### Marketplace UI
```java
public class MarketplaceUI {
    private Town town;
    private List<MarketStall> stalls;
    
    public class MarketStall {
        private String stallName;
        private NPC merchant;
        private List<Item> goods;
        private String specialty;  // "weapons", "food", "potions", etc.
        
        public MarketStall(NPC merchant) {
            this.merchant = merchant;
            this.stallName = merchant.getName() + "'s Stall";
            this.goods = generateGoods(merchant.getType());
        }
        
        private List<Item> generateGoods(NPCType type) {
            return switch (type) {
                case MERCHANT -> generateGeneralGoods();
                case BLACKSMITH -> generateWeaponsAndArmor();
                case INNKEEPER -> generateFoodAndDrink();
                default -> generateMiscGoods();
            };
        }
    }
    
    public void open(Town town) {
        this.town = town;
        this.stalls = new ArrayList<>();
        
        // Create stalls from town merchants
        for (NPC npc : town.getNPCs()) {
            if (npc.getType() == NPCType.MERCHANT || 
                npc.getType() == NPCType.BLACKSMITH) {
                stalls.add(new MarketStall(npc));
            }
        }
        
        showUI();
    }
    
    private void showUI() {
        // Grid of stall buttons
        for (int i = 0; i < stalls.size(); i++) {
            MarketStall stall = stalls.get(i);
            Button stallBtn = new Button(stall.getStallName());
            stallBtn.setOnAction(e -> openStall(stall));
            // Position in grid...
        }
    }
}
```

---

## Price Dynamics

### Supply and Demand
```java
public class EconomySystem {
    private List<Town> towns;
    private double updateInterval = 60.0;  // Update every game minute
    private double timer = 0;
    
    public void update(double deltaTime) {
        timer += deltaTime;
        if (timer < updateInterval) return;
        timer = 0;
        
        for (Town town : towns) {
            updateTownEconomy(town);
        }
    }
    
    private void updateTownEconomy(Town town) {
        // Natural production (towns produce goods over time)
        produceGoods(town);
        
        // Natural consumption (towns consume goods over time)
        consumeGoods(town);
        
        // Adjust supply/demand based on inventory levels
        adjustPrices(town);
    }
    
    private void produceGoods(Town town) {
        switch (town.getType()) {
            case FARMING_TOWN:
                town.addToInventory("wheat", random.nextInt(5) + 1);
                town.addToInventory("vegetables", random.nextInt(3) + 1);
                break;
            case MINING_TOWN:
                town.addToInventory("iron_ore", random.nextInt(3) + 1);
                town.addToInventory("stone", random.nextInt(5) + 1);
                break;
            case FISHING_VILLAGE:
                town.addToInventory("fish", random.nextInt(5) + 1);
                break;
        }
    }
    
    private void consumeGoods(Town town) {
        // Population consumes food
        int population = town.getPopulation();
        int foodNeeded = population / 10;
        
        // Try to consume food types
        town.removeFromInventory("bread", foodNeeded);
        town.removeFromInventory("fish", foodNeeded / 2);
    }
    
    private void adjustPrices(Town town) {
        for (String itemId : town.getInventory().keySet()) {
            int stock = town.getInventory().get(itemId);
            double currentSupply = town.getSupplyLevel(itemId);
            
            // High stock = more supply = lower prices
            if (stock > 100) {
                town.setSupplyLevel(itemId, Math.min(2.0, currentSupply + 0.05));
            } else if (stock < 20) {
                town.setSupplyLevel(itemId, Math.max(0.5, currentSupply - 0.05));
            }
        }
    }
}
```

### Price Fluctuations
```java
public class PriceHistory {
    private List<PricePoint> history;
    private static final int MAX_HISTORY = 30;  // 30 data points
    
    public void recordPrice(String itemId, int price, GameTime time) {
        history.add(new PricePoint(itemId, price, time.getDay()));
        
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }
    
    // Get price trend
    public PriceTrend getTrend(String itemId) {
        List<Integer> prices = history.stream()
            .filter(p -> p.itemId.equals(itemId))
            .map(p -> p.price)
            .toList();
        
        if (prices.size() < 3) return PriceTrend.STABLE;
        
        int recent = prices.get(prices.size() - 1);
        int old = prices.get(0);
        double change = (double) (recent - old) / old;
        
        if (change > 0.1) return PriceTrend.RISING;
        if (change < -0.1) return PriceTrend.FALLING;
        return PriceTrend.STABLE;
    }
    
    public enum PriceTrend {
        RISING("↑", Color.GREEN),
        FALLING("↓", Color.RED),
        STABLE("→", Color.GRAY);
    }
}
```

---

## Code Examples

### Complete Shop Transaction
```java
public class ShopTransaction {
    private PlayerSprite player;
    private Town town;
    private List<TransactionItem> cart;
    
    public void addToCart(String itemId, int quantity, boolean isBuying) {
        int price = isBuying 
            ? PriceCalculator.getBuyPrice(itemId, town, player)
            : PriceCalculator.getSellPrice(itemId, town, player);
        
        cart.add(new TransactionItem(itemId, quantity, price, isBuying));
    }
    
    public int getCartTotal() {
        int total = 0;
        for (TransactionItem item : cart) {
            if (item.isBuying) {
                total -= item.price * item.quantity;  // Spending
            } else {
                total += item.price * item.quantity;  // Earning
            }
        }
        return total;
    }
    
    public boolean completeTransaction() {
        int netCost = getCartTotal();
        
        // Validate
        if (netCost < 0 && !player.getCurrency().canAfford(-netCost)) {
            return false;  // Can't afford
        }
        
        // Process each item
        for (TransactionItem item : cart) {
            if (item.isBuying) {
                // Buy from shop
                if (town.getInventory(item.itemId) < item.quantity) {
                    continue;  // Skip if out of stock
                }
                
                town.removeFromInventory(item.itemId, item.quantity);
                Item newItem = ItemRegistry.create(item.itemId);
                newItem.setQuantity(item.quantity);
                player.getInventory().addItem(newItem);
            } else {
                // Sell to shop
                player.getInventory().removeById(item.itemId, item.quantity);
                town.addToInventory(item.itemId, item.quantity);
            }
        }
        
        // Handle money
        if (netCost < 0) {
            player.getCurrency().subtract(Currency.fromCopper(-netCost));
            town.getTreasury().add(Currency.fromCopper(-netCost));
        } else {
            town.getTreasury().subtract(Currency.fromCopper(netCost));
            player.getCurrency().add(Currency.fromCopper(netCost));
        }
        
        cart.clear();
        return true;
    }
}
```

### Economy UI Display
```java
public void renderPriceDisplay(GraphicsContext gc, String itemId, Town town) {
    int buyPrice = PriceCalculator.getBuyPrice(itemId, town, player);
    int sellPrice = PriceCalculator.getSellPrice(itemId, town, player);
    PriceTrend trend = priceHistory.getTrend(itemId);
    
    // Item name
    gc.setFill(Color.WHITE);
    gc.fillText(ItemRegistry.getName(itemId), x, y);
    
    // Buy price (green)
    gc.setFill(Color.LIGHTGREEN);
    gc.fillText("Buy: " + Currency.fromCopper(buyPrice), x + 150, y);
    
    // Sell price (gold)
    gc.setFill(Color.GOLD);
    gc.fillText("Sell: " + Currency.fromCopper(sellPrice), x + 250, y);
    
    // Trend indicator
    gc.setFill(trend.getColor());
    gc.fillText(trend.getSymbol(), x + 350, y);
}
```

---

## Tips for Economy Development

### Balancing
```java
// Good starting values for medieval economy:
// - Average worker earns ~10-20 silver/day
// - Basic meal costs ~5-10 copper
// - Simple weapon costs ~1-5 gold
// - Armor costs ~5-20 gold
// - Horse costs ~20-50 gold

public class EconomyBalancer {
    public static final int DAILY_WAGE = 1500;        // 15 silver in copper
    public static final int MEAL_COST = 10;           // 10 copper
    public static final int BASIC_WEAPON = 50000;     // 5 gold in copper
    public static final int GOOD_ARMOR = 100000;      // 10 gold in copper
}
```

### Preventing Exploits
```java
// Limit buy/sell quantities
private static final int MAX_TRANSACTION = 99;

// Cooldown between transactions
private double lastTransaction = 0;
private static final double TRANSACTION_COOLDOWN = 1.0;

// Price stabilization (prevent rapid arbitrage)
public int getStabilizedPrice(String itemId, int basePrice, boolean isBuying) {
    int recentTransactions = getRecentTransactionCount(itemId);
    double stabilityFactor = 1.0 + (recentTransactions * 0.05);  // 5% per transaction
    
    return isBuying 
        ? (int)(basePrice * stabilityFactor)
        : (int)(basePrice / stabilityFactor);
}
```

---

## Next: [07_UI_COMPONENTS.md](07_UI_COMPONENTS.md)
