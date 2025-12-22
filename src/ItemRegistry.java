import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of all items in the game.
 * Provides factory methods for creating item instances.
 */
public class ItemRegistry {
    
    private static final Map<String, Item> ITEMS = new HashMap<>();
    
    static {
        registerAllItems();
    }
    
    private static void registerAllItems() {
        // === Starting Clothes (no stats, just for decency) ===
        register(new Item(
            "ragged_shirt", "Ragged Shirt", 
            "A worn, threadbare shirt. It's seen better days, but it covers you.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 5,
            Color.web("#8b7355"), Color.web("#6b5344"), "👕"
        ).withEquipment(Equipment.Slot.BODY, Equipment.Type.LEATHER_VEST).withStats(0, 0));
        
        register(new Item(
            "ragged_pants", "Ragged Pants",
            "Patched and frayed trousers. They barely hold together.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 5,
            Color.web("#6b5344"), Color.web("#5a4233"), "👖"
        ).withEquipment(Equipment.Slot.LEGS, Equipment.Type.LEATHER_PANTS).withStats(0, 0));
        
        // === Basic Clothing ===
        register(new Item(
            "cloth_shirt", "Cloth Shirt",
            "A simple but clean shirt made of linen.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 50,
            Color.web("#e8e8e8"), Color.web("#c0c0c0"), "👕"
        ).withEquipment(Equipment.Slot.BODY, Equipment.Type.LEATHER_VEST).withStats(0, 1));
        
        register(new Item(
            "cloth_pants", "Cloth Pants",
            "Simple trousers suitable for everyday wear.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 50,
            Color.web("#4a4a4a"), Color.web("#3a3a3a"), "👖"
        ).withEquipment(Equipment.Slot.LEGS, Equipment.Type.LEATHER_PANTS).withStats(0, 1));
        
        // === Leather Armor ===
        register(new Item(
            "leather_cap", "Leather Cap",
            "A sturdy leather cap that provides basic head protection.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 150,
            Color.web("#8b4513"), Color.web("#654321"), "🎩"
        ).withEquipment(Equipment.Slot.HEAD, Equipment.Type.LEATHER_CAP).withStats(0, 3).withDurability(50));
        
        register(new Item(
            "leather_vest", "Leather Vest",
            "A vest made of tanned leather, offering decent protection.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 250,
            Color.web("#8b4513"), Color.web("#654321"), "🦺"
        ).withEquipment(Equipment.Slot.BODY, Equipment.Type.LEATHER_VEST).withStats(0, 5).withDurability(60));
        
        register(new Item(
            "leather_pants", "Leather Pants",
            "Durable leather trousers suited for travel.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 200,
            Color.web("#8b4513"), Color.web("#654321"), "👖"
        ).withEquipment(Equipment.Slot.LEGS, Equipment.Type.LEATHER_PANTS).withStats(0, 4).withDurability(55));
        
        register(new Item(
            "leather_boots", "Leather Boots",
            "Comfortable boots for long journeys.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 180,
            Color.web("#8b4513"), Color.web("#654321"), "👢"
        ).withEquipment(Equipment.Slot.FEET, Equipment.Type.LEATHER_BOOTS).withStats(0, 2).withDurability(45));
        
        // === Iron Armor ===
        register(new Item(
            "iron_helm", "Iron Helm",
            "A helmet forged from iron. Heavy but protective.",
            Item.Category.EQUIPMENT, Item.Rarity.UNCOMMON, 1, 500,
            Color.web("#708090"), Color.web("#4a5568"), "⛑"
        ).withEquipment(Equipment.Slot.HEAD, Equipment.Type.IRON_HELM).withStats(0, 8).withDurability(80));
        
        register(new Item(
            "iron_plate", "Iron Chestplate",
            "A breastplate of solid iron construction.",
            Item.Category.EQUIPMENT, Item.Rarity.UNCOMMON, 1, 800,
            Color.web("#708090"), Color.web("#4a5568"), "🛡"
        ).withEquipment(Equipment.Slot.BODY, Equipment.Type.IRON_PLATE).withStats(0, 12).withDurability(100));
        
        register(new Item(
            "iron_greaves", "Iron Greaves",
            "Leg armor crafted from iron plates.",
            Item.Category.EQUIPMENT, Item.Rarity.UNCOMMON, 1, 600,
            Color.web("#708090"), Color.web("#4a5568"), "🦿"
        ).withEquipment(Equipment.Slot.LEGS, Equipment.Type.IRON_GREAVES).withStats(0, 10).withDurability(90));
        
        register(new Item(
            "iron_boots", "Iron Boots",
            "Heavy iron footwear for maximum protection.",
            Item.Category.EQUIPMENT, Item.Rarity.UNCOMMON, 1, 450,
            Color.web("#708090"), Color.web("#4a5568"), "👢"
        ).withEquipment(Equipment.Slot.FEET, Equipment.Type.IRON_BOOTS).withStats(0, 6).withDurability(75));
        
        // === Weapons ===
        register(new Item(
            "wooden_sword", "Wooden Sword",
            "A practice sword made of wood. Better than nothing.",
            Item.Category.WEAPON, Item.Rarity.COMMON, 1, 25,
            Color.web("#deb887"), Color.web("#8b7355"), "🗡"
        ).withEquipment(Equipment.Slot.MAIN_HAND, Equipment.Type.WOODEN_SWORD).withStats(3, 0).withDurability(30));
        
        register(new Item(
            "iron_sword", "Iron Sword",
            "A reliable sword forged from iron.",
            Item.Category.WEAPON, Item.Rarity.UNCOMMON, 1, 400,
            Color.web("#708090"), Color.web("#4a3728"), "⚔"
        ).withEquipment(Equipment.Slot.MAIN_HAND, Equipment.Type.IRON_SWORD).withStats(10, 0).withDurability(80));
        
        register(new Item(
            "steel_sword", "Steel Sword",
            "A finely crafted blade of tempered steel.",
            Item.Category.WEAPON, Item.Rarity.RARE, 1, 1200,
            Color.web("#b0c4de"), Color.web("#2f4f4f"), "⚔"
        ).withEquipment(Equipment.Slot.MAIN_HAND, Equipment.Type.STEEL_SWORD).withStats(18, 0).withDurability(120));
        
        // === Shields ===
        register(new Item(
            "wooden_shield", "Wooden Shield",
            "A basic shield made of wooden planks.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 75,
            Color.web("#deb887"), Color.web("#8b7355"), "🛡"
        ).withEquipment(Equipment.Slot.OFF_HAND, Equipment.Type.WOODEN_SHIELD).withStats(0, 5).withDurability(40));
        
        register(new Item(
            "iron_shield", "Iron Shield",
            "A sturdy shield reinforced with iron.",
            Item.Category.EQUIPMENT, Item.Rarity.UNCOMMON, 1, 350,
            Color.web("#708090"), Color.web("#4a5568"), "🛡"
        ).withEquipment(Equipment.Slot.OFF_HAND, Equipment.Type.IRON_SHIELD).withStats(0, 12).withDurability(90));
        
        // === Capes ===
        register(new Item(
            "cloth_cape", "Cloth Cape",
            "A simple cape that offers warmth.",
            Item.Category.EQUIPMENT, Item.Rarity.COMMON, 1, 100,
            Color.web("#8b0000"), Color.web("#4a0000"), "🧥"
        ).withEquipment(Equipment.Slot.CAPE, Equipment.Type.CLOTH_CAPE).withStats(0, 1));
        
        register(new Item(
            "merchant_cape", "Merchant's Cape",
            "A fine cape worn by successful traders.",
            Item.Category.EQUIPMENT, Item.Rarity.UNCOMMON, 1, 500,
            Color.web("#4169e1"), Color.web("#ffd700"), "🧥"
        ).withEquipment(Equipment.Slot.CAPE, Equipment.Type.MERCHANT_CAPE).withStats(0, 2));
        
        // === Consumables ===
        register(new Item(
            "bread", "Bread",
            "A fresh loaf of bread. Restores a small amount of health.",
            Item.Category.CONSUMABLE, Item.Rarity.COMMON, 20, 10,
            Color.web("#daa520"), Color.web("#8b4513"), "🍞"
        ));
        
        register(new Item(
            "cheese", "Cheese Wheel",
            "A wheel of aged cheese. Quite filling.",
            Item.Category.CONSUMABLE, Item.Rarity.COMMON, 10, 25,
            Color.web("#ffd700"), Color.web("#daa520"), "🧀"
        ));
        
        register(new Item(
            "health_potion", "Health Potion",
            "A red potion that restores health when consumed.",
            Item.Category.CONSUMABLE, Item.Rarity.UNCOMMON, 10, 100,
            Color.web("#ff4444"), Color.web("#aa0000"), "🧪"
        ));
        
        register(new Item(
            "ale", "Mug of Ale",
            "A frothy mug of ale. Good for morale!",
            Item.Category.CONSUMABLE, Item.Rarity.COMMON, 10, 15,
            Color.web("#d2691e"), Color.web("#8b4513"), "🍺"
        ));
        
        // === Materials ===
        register(new Item(
            "iron_ore", "Iron Ore",
            "Raw iron ore. Can be smelted into iron ingots.",
            Item.Category.MATERIAL, Item.Rarity.COMMON, 50, 20,
            Color.web("#708090"), Color.web("#4a5568"), "🪨"
        ));
        
        register(new Item(
            "iron_ingot", "Iron Ingot",
            "A bar of refined iron, ready for crafting.",
            Item.Category.MATERIAL, Item.Rarity.COMMON, 30, 50,
            Color.web("#a0a0a0"), Color.web("#606060"), "🔩"
        ));
        
        register(new Item(
            "leather", "Leather",
            "Tanned animal hide, useful for crafting.",
            Item.Category.MATERIAL, Item.Rarity.COMMON, 30, 30,
            Color.web("#8b4513"), Color.web("#654321"), "🥩"
        ));
        
        register(new Item(
            "wood", "Wood Planks",
            "Cut and prepared wooden planks.",
            Item.Category.MATERIAL, Item.Rarity.COMMON, 50, 10,
            Color.web("#deb887"), Color.web("#8b7355"), "🪵"
        ));
        
        register(new Item(
            "cloth", "Cloth",
            "A bolt of woven cloth fabric.",
            Item.Category.MATERIAL, Item.Rarity.COMMON, 30, 15,
            Color.web("#f5f5dc"), Color.web("#d4c4a0"), "🧵"
        ));
        
        // === Tools ===
        register(new Item(
            "pickaxe", "Iron Pickaxe",
            "A sturdy pickaxe for mining ore.",
            Item.Category.TOOL, Item.Rarity.COMMON, 1, 200,
            Color.web("#708090"), Color.web("#8b7355"), "⛏"
        ).withDurability(100));
        
        register(new Item(
            "axe", "Woodcutter's Axe",
            "An axe for chopping trees.",
            Item.Category.TOOL, Item.Rarity.COMMON, 1, 180,
            Color.web("#708090"), Color.web("#8b7355"), "🪓"
        ).withDurability(100));
        
        // === Misc/Valuables ===
        register(new Item(
            "gem_ruby", "Ruby",
            "A brilliant red gemstone of considerable value.",
            Item.Category.MISC, Item.Rarity.RARE, 10, 2000,
            Color.web("#e0115f"), Color.web("#8b0000"), "💎"
        ));
        
        register(new Item(
            "gem_sapphire", "Sapphire",
            "A deep blue gemstone prized by nobility.",
            Item.Category.MISC, Item.Rarity.RARE, 10, 2000,
            Color.web("#0f52ba"), Color.web("#000080"), "💎"
        ));
        
        register(new Item(
            "gem_emerald", "Emerald",
            "A vivid green gemstone of exceptional clarity.",
            Item.Category.MISC, Item.Rarity.RARE, 10, 2500,
            Color.web("#50c878"), Color.web("#228b22"), "💎"
        ));
        
        register(new Item(
            "gold_ring", "Gold Ring",
            "A simple ring made of pure gold.",
            Item.Category.MISC, Item.Rarity.UNCOMMON, 5, 500,
            Color.web("#ffd700"), Color.web("#daa520"), "💍"
        ));
    }
    
    private static void register(Item item) {
        ITEMS.put(item.getId(), item);
    }
    
    /**
     * Gets an item by its ID.
     * @return A new instance of the item, or null if not found
     */
    public static Item get(String id) {
        Item template = ITEMS.get(id);
        if (template == null) return null;
        
        // Return a copy to prevent modification of the template
        Item copy = new Item(
            template.getId(), template.getName(), template.getDescription(),
            template.getCategory(), template.getRarity(), template.getMaxStackSize(),
            template.getBaseValue(), template.getPrimaryColor(), template.getSecondaryColor(),
            template.getIconSymbol()
        );
        
        if (template.getEquipSlot() != null) {
            copy.withEquipment(template.getEquipSlot(), template.getEquipType());
        }
        copy.withStats(template.getAttack(), template.getDefense());
        if (template.getMaxDurability() > 0) {
            copy.withDurability(template.getMaxDurability());
        }
        
        return copy;
    }
    
    /**
     * Creates an ItemStack with the specified item and quantity.
     */
    public static ItemStack createStack(String itemId, int quantity) {
        Item item = get(itemId);
        if (item == null) return null;
        return new ItemStack(item, quantity);
    }
    
    /**
     * Gets all registered item IDs.
     */
    public static String[] getAllItemIds() {
        return ITEMS.keySet().toArray(new String[0]);
    }
    
    /**
     * Checks if an item exists in the registry.
     */
    public static boolean exists(String id) {
        return ITEMS.containsKey(id);
    }
}
