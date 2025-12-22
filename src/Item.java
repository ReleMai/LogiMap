import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Base class for all items in the game.
 * Items can be stacked, traded, equipped, or used.
 */
public class Item {
    
    public enum Category {
        EQUIPMENT,      // Wearable gear
        WEAPON,         // Main/off-hand weapons
        CONSUMABLE,     // Food, potions
        MATERIAL,       // Crafting materials
        TOOL,           // Tools for gathering/crafting
        CURRENCY,       // Money items (for display purposes)
        QUEST,          // Quest items
        MISC            // Miscellaneous
    }
    
    public enum Rarity {
        COMMON(Color.web("#9d9d9d"), "Common"),
        UNCOMMON(Color.web("#1eff00"), "Uncommon"),
        RARE(Color.web("#0070dd"), "Rare"),
        EPIC(Color.web("#a335ee"), "Epic"),
        LEGENDARY(Color.web("#ff8000"), "Legendary");
        
        private final Color color;
        private final String displayName;
        
        Rarity(Color color, String displayName) {
            this.color = color;
            this.displayName = displayName;
        }
        
        public Color getColor() { return color; }
        public String getDisplayName() { return displayName; }
    }
    
    // Core properties
    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final Rarity rarity;
    private final int maxStackSize;
    private final long baseValue;  // Value in copper
    
    // Visual properties
    private final Color primaryColor;
    private final Color secondaryColor;
    private final String iconSymbol;  // Unicode symbol for icon
    
    // Equipment-specific (if applicable)
    private Equipment.Slot equipSlot;
    private Equipment.Type equipType;
    
    // Stats
    private int attack;
    private int defense;
    private int durability;
    private int maxDurability;
    
    public Item(String id, String name, String description, Category category, 
                Rarity rarity, int maxStackSize, long baseValue,
                Color primaryColor, Color secondaryColor, String iconSymbol) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.rarity = rarity;
        this.maxStackSize = maxStackSize;
        this.baseValue = baseValue;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.iconSymbol = iconSymbol;
        this.durability = 100;
        this.maxDurability = 100;
    }
    
    // === Builder Pattern for easier creation ===
    
    public Item withEquipment(Equipment.Slot slot, Equipment.Type type) {
        this.equipSlot = slot;
        this.equipType = type;
        return this;
    }
    
    public Item withStats(int attack, int defense) {
        this.attack = attack;
        this.defense = defense;
        return this;
    }
    
    public Item withDurability(int maxDurability) {
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        return this;
    }
    
    // === Getters ===
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Rarity getRarity() { return rarity; }
    public int getMaxStackSize() { return maxStackSize; }
    public long getBaseValue() { return baseValue; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public String getIconSymbol() { return iconSymbol; }
    public Equipment.Slot getEquipSlot() { return equipSlot; }
    public Equipment.Type getEquipType() { return equipType; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    
    public boolean isEquipment() {
        return category == Category.EQUIPMENT || category == Category.WEAPON;
    }
    
    public boolean isStackable() {
        return maxStackSize > 1;
    }
    
    public void setDurability(int durability) {
        this.durability = Math.max(0, Math.min(durability, maxDurability));
    }
    
    // === Rendering ===
    
    /**
     * Renders the item icon in a slot.
     */
    public void renderIcon(GraphicsContext gc, double x, double y, double size) {
        // Background based on rarity
        gc.setFill(rarity.getColor().deriveColor(0, 1, 0.3, 0.5));
        gc.fillRoundRect(x, y, size, size, size * 0.15, size * 0.15);
        
        // Item shape/icon
        gc.setFill(primaryColor);
        double iconSize = size * 0.7;
        double iconX = x + (size - iconSize) / 2;
        double iconY = y + (size - iconSize) / 2;
        
        renderItemShape(gc, iconX, iconY, iconSize);
        
        // Rarity border
        gc.setStroke(rarity.getColor());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 1, y + 1, size - 2, size - 2, size * 0.15, size * 0.15);
    }
    
    /**
     * Renders the specific item shape based on category.
     */
    protected void renderItemShape(GraphicsContext gc, double x, double y, double size) {
        gc.setFill(primaryColor);
        
        switch (category) {
            case EQUIPMENT:
                renderEquipmentShape(gc, x, y, size);
                break;
            case WEAPON:
                renderWeaponShape(gc, x, y, size);
                break;
            case CONSUMABLE:
                // Potion/food shape
                gc.fillOval(x + size * 0.2, y + size * 0.3, size * 0.6, size * 0.6);
                gc.fillRect(x + size * 0.35, y, size * 0.3, size * 0.35);
                break;
            case MATERIAL:
                // Crystal/material shape
                gc.fillPolygon(
                    new double[]{x + size * 0.5, x + size * 0.8, x + size * 0.65, x + size * 0.35, x + size * 0.2},
                    new double[]{y, y + size * 0.4, y + size, y + size, y + size * 0.4},
                    5
                );
                break;
            case TOOL:
                // Tool shape (hammer-like)
                gc.setFill(secondaryColor);
                gc.fillRect(x + size * 0.45, y + size * 0.3, size * 0.1, size * 0.65);
                gc.setFill(primaryColor);
                gc.fillRect(x + size * 0.2, y + size * 0.1, size * 0.6, size * 0.25);
                break;
            default:
                // Default square
                gc.fillRoundRect(x, y, size, size, size * 0.2, size * 0.2);
                break;
        }
        
        // Add icon symbol if present
        if (iconSymbol != null && !iconSymbol.isEmpty()) {
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("System", size * 0.5));
            gc.fillText(iconSymbol, x + size * 0.25, y + size * 0.65);
        }
    }
    
    private void renderEquipmentShape(GraphicsContext gc, double x, double y, double size) {
        if (equipSlot == null) {
            gc.fillRoundRect(x, y, size, size, size * 0.2, size * 0.2);
            return;
        }
        
        switch (equipSlot) {
            case HEAD:
                // Helmet shape
                gc.fillArc(x, y + size * 0.2, size, size * 0.8, 0, 180, javafx.scene.shape.ArcType.ROUND);
                break;
            case BODY:
                // Chest armor shape
                gc.fillRoundRect(x + size * 0.1, y, size * 0.8, size, size * 0.2, size * 0.2);
                gc.setFill(secondaryColor);
                gc.fillRect(x + size * 0.35, y + size * 0.1, size * 0.3, size * 0.15);
                break;
            case LEGS:
                // Pants shape
                gc.fillRect(x + size * 0.15, y, size * 0.7, size * 0.4);
                gc.fillRect(x + size * 0.15, y + size * 0.35, size * 0.25, size * 0.65);
                gc.fillRect(x + size * 0.6, y + size * 0.35, size * 0.25, size * 0.65);
                break;
            case FEET:
                // Boots shape
                gc.fillRoundRect(x + size * 0.1, y + size * 0.3, size * 0.35, size * 0.7, size * 0.1, size * 0.1);
                gc.fillRoundRect(x + size * 0.55, y + size * 0.3, size * 0.35, size * 0.7, size * 0.1, size * 0.1);
                break;
            case CAPE:
                // Cape shape
                gc.fillPolygon(
                    new double[]{x + size * 0.2, x + size * 0.8, x + size * 0.9, x + size * 0.5, x + size * 0.1},
                    new double[]{y, y, y + size, y + size * 0.8, y + size},
                    5
                );
                break;
            default:
                gc.fillRoundRect(x, y, size, size, size * 0.2, size * 0.2);
                break;
        }
    }
    
    private void renderWeaponShape(GraphicsContext gc, double x, double y, double size) {
        // Sword shape by default
        gc.setFill(secondaryColor);
        gc.fillRect(x + size * 0.45, y + size * 0.6, size * 0.1, size * 0.35); // Handle
        gc.setFill(primaryColor);
        gc.fillPolygon(
            new double[]{x + size * 0.5, x + size * 0.65, x + size * 0.55, x + size * 0.45, x + size * 0.35},
            new double[]{y, y + size * 0.55, y + size * 0.65, y + size * 0.65, y + size * 0.55},
            5
        ); // Blade
        gc.setFill(secondaryColor);
        gc.fillRect(x + size * 0.25, y + size * 0.55, size * 0.5, size * 0.08); // Guard
    }
    
    /**
     * Gets a formatted tooltip string for this item.
     */
    public String getTooltip() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(rarity.getDisplayName()).append(" ").append(category.name().toLowerCase()).append("\n");
        
        if (attack > 0) sb.append("Attack: +").append(attack).append("\n");
        if (defense > 0) sb.append("Defense: +").append(defense).append("\n");
        
        if (!description.isEmpty()) {
            sb.append("\n").append(description).append("\n");
        }
        
        sb.append("\nValue: ").append(Currency.fromCopper(baseValue).toShortString());
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item) {
            return this.id.equals(((Item) obj).id);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
