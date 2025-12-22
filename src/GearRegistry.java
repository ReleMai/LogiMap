import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of all gear templates for the game.
 * Modders can add new gear by registering templates here.
 * 
 * Usage:
 *   GearRegistry.register(new GearTemplate("my_helmet", "My Helmet", SlotType.HEAD)
 *       .withColors(Color.RED, Color.DARKRED, Color.ORANGE)
 *       .withStyle(RenderStyle.PLATE)
 *       .withStats(10, 0, 100));
 */
public class GearRegistry {
    
    private static final Map<String, GearTemplate> templates = new HashMap<>();
    
    // Initialize default gear
    static {
        registerDefaults();
    }
    
    /**
     * Register all default game gear
     */
    private static void registerDefaults() {
        // === HEADGEAR ===
        register(new GearTemplate("leather_cap", "Leather Cap", GearTemplate.SlotType.HEAD)
            .withColors(Color.web("#8b6914"), Color.web("#5c4a1a"), Color.web("#a08030"))
            .withStyle(GearTemplate.RenderStyle.LEATHER)
            .withStats(2, 0, 15));
            
        register(new GearTemplate("iron_helm", "Iron Helm", GearTemplate.SlotType.HEAD)
            .withColors(Color.web("#808080"), Color.web("#505050"), Color.web("#a0a0a0"))
            .withStyle(GearTemplate.RenderStyle.IRON)
            .withStats(5, 0, 50));
            
        register(new GearTemplate("steel_helm", "Steel Helm", GearTemplate.SlotType.HEAD)
            .withColors(Color.web("#c0c0c0"), Color.web("#909090"), Color.web("#e0e0e0"))
            .withStyle(GearTemplate.RenderStyle.STEEL)
            .withStats(8, 0, 120));
            
        register(new GearTemplate("crown", "Royal Crown", GearTemplate.SlotType.HEAD)
            .withColors(Color.web("#ffd700"), Color.web("#daa520"), Color.web("#ffec8b"))
            .withStyle(GearTemplate.RenderStyle.ROYAL)
            .withStats(1, 0, 1000));
        
        // === CHEST ARMOR ===
        register(new GearTemplate("ragged_shirt", "Ragged Shirt", GearTemplate.SlotType.CHEST)
            .withColors(Color.web("#b8a878"), Color.web("#8a7858"), Color.web("#d4c4a4"))
            .withStyle(GearTemplate.RenderStyle.RAGGED)
            .withStats(0, 0, 1));
            
        register(new GearTemplate("cloth_shirt", "Cloth Shirt", GearTemplate.SlotType.CHEST)
            .withColors(Color.web("#c4b090"), Color.web("#a09070"), Color.web("#d4c4a4"))
            .withStyle(GearTemplate.RenderStyle.CLOTH)
            .withStats(1, 0, 5));
            
        register(new GearTemplate("leather_vest", "Leather Vest", GearTemplate.SlotType.CHEST)
            .withColors(Color.web("#8b6914"), Color.web("#5c4a1a"), Color.web("#a08030"))
            .withStyle(GearTemplate.RenderStyle.LEATHER)
            .withStats(3, 0, 25));
            
        register(new GearTemplate("chainmail", "Chainmail", GearTemplate.SlotType.CHEST)
            .withColors(Color.web("#909090"), Color.web("#606060"), Color.web("#b0b0b0"))
            .withStyle(GearTemplate.RenderStyle.CHAINMAIL)
            .withStats(6, 0, 80));
            
        register(new GearTemplate("iron_plate", "Iron Plate", GearTemplate.SlotType.CHEST)
            .withColors(Color.web("#808080"), Color.web("#505050"), Color.web("#a0a0a0"))
            .withStyle(GearTemplate.RenderStyle.PLATE)
            .withStats(10, 0, 150));
            
        register(new GearTemplate("steel_plate", "Steel Plate", GearTemplate.SlotType.CHEST)
            .withColors(Color.web("#c0c0c0"), Color.web("#909090"), Color.web("#e0e0e0"))
            .withStyle(GearTemplate.RenderStyle.STEEL)
            .withStats(15, 0, 300));
            
        register(new GearTemplate("royal_armor", "Royal Armor", GearTemplate.SlotType.CHEST)
            .withColors(Color.web("#4040c0"), Color.web("#303090"), Color.web("#ffd700"))
            .withStyle(GearTemplate.RenderStyle.ROYAL)
            .withStats(12, 0, 500));
        
        // === LEG ARMOR ===
        register(new GearTemplate("ragged_pants", "Ragged Pants", GearTemplate.SlotType.LEFT_LEG)
            .withColors(Color.web("#9a8a6a"), Color.web("#7a6a4a"), Color.web("#b4a484"))
            .withStyle(GearTemplate.RenderStyle.RAGGED)
            .withStats(0, 0, 1));
            
        register(new GearTemplate("cloth_pants", "Cloth Pants", GearTemplate.SlotType.LEFT_LEG)
            .withColors(Color.web("#8a7a5a"), Color.web("#6a5a3a"), Color.web("#a49474"))
            .withStyle(GearTemplate.RenderStyle.CLOTH)
            .withStats(1, 0, 5));
            
        register(new GearTemplate("leather_pants", "Leather Pants", GearTemplate.SlotType.LEFT_LEG)
            .withColors(Color.web("#6b4914"), Color.web("#4c3a1a"), Color.web("#8a6030"))
            .withStyle(GearTemplate.RenderStyle.LEATHER)
            .withStats(2, 0, 20));
            
        register(new GearTemplate("iron_greaves", "Iron Greaves", GearTemplate.SlotType.LEFT_LEG)
            .withColors(Color.web("#808080"), Color.web("#505050"), Color.web("#a0a0a0"))
            .withStyle(GearTemplate.RenderStyle.IRON)
            .withStats(5, 0, 75));
            
        register(new GearTemplate("steel_greaves", "Steel Greaves", GearTemplate.SlotType.LEFT_LEG)
            .withColors(Color.web("#c0c0c0"), Color.web("#909090"), Color.web("#e0e0e0"))
            .withStyle(GearTemplate.RenderStyle.STEEL)
            .withStats(8, 0, 150));
        
        // === FOOTWEAR ===
        register(new GearTemplate("sandals", "Sandals", GearTemplate.SlotType.FEET)
            .withColors(Color.web("#8b6914"), Color.web("#5c4a1a"), Color.web("#a08030"))
            .withStyle(GearTemplate.RenderStyle.LEATHER)
            .withStats(0, 0, 3));
            
        register(new GearTemplate("leather_boots", "Leather Boots", GearTemplate.SlotType.FEET)
            .withColors(Color.web("#6b4914"), Color.web("#4c3a1a"), Color.web("#8a6030"))
            .withStyle(GearTemplate.RenderStyle.LEATHER)
            .withStats(2, 0, 15));
            
        register(new GearTemplate("iron_boots", "Iron Boots", GearTemplate.SlotType.FEET)
            .withColors(Color.web("#808080"), Color.web("#505050"), Color.web("#a0a0a0"))
            .withStyle(GearTemplate.RenderStyle.IRON)
            .withStats(4, 0, 60));
            
        register(new GearTemplate("steel_boots", "Steel Boots", GearTemplate.SlotType.FEET)
            .withColors(Color.web("#c0c0c0"), Color.web("#909090"), Color.web("#e0e0e0"))
            .withStyle(GearTemplate.RenderStyle.STEEL)
            .withStats(6, 0, 100));
        
        // === WEAPONS ===
        register(new GearTemplate("wooden_sword", "Wooden Sword", GearTemplate.SlotType.MAIN_HAND)
            .withColors(Color.web("#8b6914"), Color.web("#5c4a1a"), Color.web("#6b4914"))
            .withStyle(GearTemplate.RenderStyle.WOODEN)
            .withStats(0, 3, 5));
            
        register(new GearTemplate("iron_sword", "Iron Sword", GearTemplate.SlotType.MAIN_HAND)
            .withColors(Color.web("#808080"), Color.web("#505050"), Color.web("#6b4914"))
            .withStyle(GearTemplate.RenderStyle.IRON)
            .withStats(0, 8, 50));
            
        register(new GearTemplate("steel_sword", "Steel Sword", GearTemplate.SlotType.MAIN_HAND)
            .withColors(Color.web("#c0c0c0"), Color.web("#909090"), Color.web("#6b4914"))
            .withStyle(GearTemplate.RenderStyle.STEEL)
            .withStats(0, 15, 120));
        
        // === SHIELDS ===
        register(new GearTemplate("wooden_shield", "Wooden Shield", GearTemplate.SlotType.OFF_HAND)
            .withColors(Color.web("#8b6914"), Color.web("#5c4a1a"), Color.web("#a08030"))
            .withStyle(GearTemplate.RenderStyle.WOODEN)
            .withStats(3, 0, 10));
            
        register(new GearTemplate("iron_shield", "Iron Shield", GearTemplate.SlotType.OFF_HAND)
            .withColors(Color.web("#808080"), Color.web("#505050"), Color.web("#a0a0a0"))
            .withStyle(GearTemplate.RenderStyle.IRON)
            .withStats(6, 0, 60));
            
        register(new GearTemplate("steel_shield", "Steel Shield", GearTemplate.SlotType.OFF_HAND)
            .withColors(Color.web("#c0c0c0"), Color.web("#909090"), Color.web("#e0e0e0"))
            .withStyle(GearTemplate.RenderStyle.STEEL)
            .withStats(10, 0, 100));
        
        // === CAPES ===
        register(new GearTemplate("cloth_cape", "Cloth Cape", GearTemplate.SlotType.BACK)
            .withColors(Color.web("#6a5a4a"), Color.web("#4a3a2a"), Color.web("#8a7a6a"))
            .withStyle(GearTemplate.RenderStyle.CLOTH)
            .withStats(0, 0, 10));
            
        register(new GearTemplate("wool_cape", "Wool Cape", GearTemplate.SlotType.BACK)
            .withColors(Color.web("#8b0000"), Color.web("#5b0000"), Color.web("#ab2020"))
            .withStyle(GearTemplate.RenderStyle.CLOTH)
            .withStats(1, 0, 30));
            
        register(new GearTemplate("royal_cape", "Royal Cape", GearTemplate.SlotType.BACK)
            .withColors(Color.web("#4b0082"), Color.web("#2b0052"), Color.web("#ffd700"))
            .withStyle(GearTemplate.RenderStyle.ROYAL)
            .withStats(2, 0, 200));
    }
    
    /**
     * Register a gear template
     */
    public static void register(GearTemplate template) {
        templates.put(template.getId(), template);
    }
    
    /**
     * Get a template by ID
     */
    public static GearTemplate get(String id) {
        return templates.get(id);
    }
    
    /**
     * Check if template exists
     */
    public static boolean exists(String id) {
        return templates.containsKey(id);
    }
    
    /**
     * Get all templates
     */
    public static Map<String, GearTemplate> getAll() {
        return new HashMap<>(templates);
    }
    
    /**
     * Get templates for a specific slot
     */
    public static Map<String, GearTemplate> getBySlot(GearTemplate.SlotType slot) {
        Map<String, GearTemplate> result = new HashMap<>();
        for (Map.Entry<String, GearTemplate> entry : templates.entrySet()) {
            if (entry.getValue().getSlot() == slot) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
