import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a harvestable resource item that can be stored in inventory.
 * 
 * Resource items have:
 * - Type (grain, ore, wood, etc.)
 * - Quantity (stackable)
 * - Quality (affects sell price)
 * - Custom sprite rendering for inventory display
 */
public class ResourceItem {
    
    /**
     * Categories of resource items.
     */
    public enum Category {
        GRAIN("Grain", "#d4a030"),
        ORE("Ore", "#6a6a6a"),
        WOOD("Wood", "#8b5a2b"),
        FISH("Fish", "#4682b4"),
        ANIMAL("Animal Product", "#d2691e"),
        HERB("Herb", "#228b22"),
        STONE("Stone", "#808080");
        
        private final String displayName;
        private final String color;
        
        Category(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    /**
     * Quality levels affect sell price.
     */
    public enum Quality {
        POOR("Poor", 0.5, "#808080"),
        COMMON("Common", 1.0, "#ffffff"),
        GOOD("Good", 1.25, "#00ff00"),
        EXCELLENT("Excellent", 1.5, "#0080ff"),
        EXCEPTIONAL("Exceptional", 2.0, "#8000ff");
        
        private final String displayName;
        private final double priceMultiplier;
        private final String color;
        
        Quality(String displayName, double priceMultiplier, String color) {
            this.displayName = displayName;
            this.priceMultiplier = priceMultiplier;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public double getPriceMultiplier() { return priceMultiplier; }
        public String getColor() { return color; }
        
        /**
         * Determines quality randomly with weighted distribution.
         */
        public static Quality random(java.util.Random rand) {
            double roll = rand.nextDouble();
            if (roll < 0.1) return POOR;
            if (roll < 0.6) return COMMON;
            if (roll < 0.85) return GOOD;
            if (roll < 0.97) return EXCELLENT;
            return EXCEPTIONAL;
        }
    }
    
    // Item properties
    private final String id;
    private final String name;
    private final Category category;
    private final Object subtype; // e.g., GrainType.WHEAT
    private Quality quality;
    private int quantity;
    private int basePrice; // Base sell price in gold
    
    // For grain items
    private GrainType grainType;
    
    /**
     * Creates a new resource item.
     */
    public ResourceItem(String id, String name, Category category, int basePrice) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subtype = null;
        this.quality = Quality.COMMON;
        this.quantity = 1;
        this.basePrice = basePrice;
    }
    
    /**
     * Creates a grain resource item.
     */
    public ResourceItem(GrainType grainType, Quality quality, int quantity) {
        this.id = "grain_" + grainType.name().toLowerCase();
        this.name = grainType.getDisplayName();
        this.category = Category.GRAIN;
        this.subtype = grainType;
        this.grainType = grainType;
        this.quality = quality;
        this.quantity = quantity;
        this.basePrice = getBasePriceForGrain(grainType);
    }
    
    private int getBasePriceForGrain(GrainType type) {
        switch (type) {
            case WHEAT: return 2;
            case OAT: return 3;
            case BARLEY: return 3;
            case RYE: return 4;
            default: return 2;
        }
    }
    
    // ==================== INVENTORY OPERATIONS ====================
    
    /**
     * Adds quantity to this stack. Returns overflow (amount that couldn't fit).
     */
    public int addQuantity(int amount, int maxStack) {
        int space = maxStack - quantity;
        int toAdd = Math.min(space, amount);
        quantity += toAdd;
        return amount - toAdd;
    }
    
    /**
     * Removes quantity from this stack. Returns actual amount removed.
     */
    public int removeQuantity(int amount) {
        int toRemove = Math.min(quantity, amount);
        quantity -= toRemove;
        return toRemove;
    }
    
    /**
     * Checks if this item can stack with another item.
     */
    public boolean canStackWith(ResourceItem other) {
        if (other == null) return false;
        return id.equals(other.id) && quality == other.quality;
    }
    
    // ==================== PRICE CALCULATION ====================
    
    /**
     * Gets the sell price for a single unit at a location.
     * Price is affected by quality and scarcity.
     */
    public int getUnitSellPrice(double scarcityMultiplier) {
        double price = basePrice * quality.getPriceMultiplier() * scarcityMultiplier;
        return Math.max(1, (int) Math.round(price));
    }
    
    /**
     * Gets total sell price for entire stack.
     */
    public int getTotalSellPrice(double scarcityMultiplier) {
        return getUnitSellPrice(scarcityMultiplier) * quantity;
    }
    
    // ==================== RENDERING ====================
    
    /**
     * Renders the item sprite in inventory.
     */
    public void renderSprite(GraphicsContext gc, double x, double y, double size) {
        if (category == Category.GRAIN && grainType != null) {
            renderGrainSprite(gc, x, y, size);
        } else {
            renderGenericSprite(gc, x, y, size);
        }
        
        // Quality border
        if (quality != Quality.COMMON) {
            gc.setStroke(Color.web(quality.getColor()));
            gc.setLineWidth(2);
            gc.strokeRect(x + 2, y + 2, size - 4, size - 4);
        }
        
        // Quantity text
        if (quantity > 1) {
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, size * 0.25));
            String qtyText = String.valueOf(quantity);
            gc.fillText(qtyText, x + size - 5 - qtyText.length() * 6, y + size - 5);
        }
    }
    
    private void renderGrainSprite(GraphicsContext gc, double x, double y, double size) {
        // Background
        gc.setFill(Color.web("#2a2010"));
        gc.fillRect(x, y, size, size);
        
        // Draw small bundle of grain
        double centerX = x + size / 2;
        double centerY = y + size * 0.7;
        double stalkSize = size * 0.4;
        
        // Draw 3 stalks in a bundle
        for (int i = -1; i <= 1; i++) {
            double offsetX = i * size * 0.12;
            double stalkX = centerX + offsetX;
            
            // Stalk
            gc.setStroke(Color.web("#6B8E23"));
            gc.setLineWidth(Math.max(1, size * 0.05));
            double topY = centerY - stalkSize;
            gc.strokeLine(stalkX, centerY, stalkX + offsetX * 0.3, topY);
            
            // Grain head
            if (grainType != null) {
                grainType.renderStalk(gc, stalkX + offsetX * 0.3, topY, size * 0.12, 1.0);
            }
        }
        
        // Binding ribbon
        gc.setStroke(Color.web("#8b4513"));
        gc.setLineWidth(2);
        gc.strokeLine(centerX - size * 0.15, centerY - stalkSize * 0.3, 
                     centerX + size * 0.15, centerY - stalkSize * 0.3);
    }
    
    private void renderGenericSprite(GraphicsContext gc, double x, double y, double size) {
        // Background
        gc.setFill(Color.web(category.getColor()).darker().darker());
        gc.fillRect(x, y, size, size);
        
        // Simple icon based on category
        gc.setFill(Color.web(category.getColor()));
        double iconSize = size * 0.5;
        double iconX = x + (size - iconSize) / 2;
        double iconY = y + (size - iconSize) / 2;
        
        switch (category) {
            case ORE:
            case STONE:
                // Rock shape
                gc.fillPolygon(
                    new double[]{iconX, iconX + iconSize * 0.3, iconX + iconSize * 0.8, iconX + iconSize, iconX + iconSize * 0.6},
                    new double[]{iconY + iconSize, iconY + iconSize * 0.3, iconY, iconY + iconSize * 0.7, iconY + iconSize},
                    5
                );
                break;
            case WOOD:
                // Log shape
                gc.fillRect(iconX, iconY + iconSize * 0.2, iconSize, iconSize * 0.6);
                gc.setFill(Color.web("#654321"));
                gc.fillOval(iconX - iconSize * 0.1, iconY + iconSize * 0.1, iconSize * 0.3, iconSize * 0.8);
                break;
            case FISH:
                // Fish shape
                gc.fillOval(iconX, iconY + iconSize * 0.2, iconSize * 0.8, iconSize * 0.6);
                gc.fillPolygon(
                    new double[]{iconX + iconSize * 0.7, iconX + iconSize, iconX + iconSize * 0.7},
                    new double[]{iconY + iconSize * 0.5, iconY + iconSize * 0.3, iconY + iconSize * 0.1},
                    3
                );
                break;
            default:
                // Simple circle
                gc.fillOval(iconX, iconY, iconSize, iconSize);
        }
    }
    
    // ==================== TOOLTIP ====================
    
    /**
     * Gets tooltip text for this item.
     */
    public String getTooltip(double scarcityMultiplier) {
        StringBuilder sb = new StringBuilder();
        
        // Name with quality color
        if (quality != Quality.COMMON) {
            sb.append("[").append(quality.getDisplayName()).append("] ");
        }
        sb.append(name).append("\n");
        
        sb.append("Category: ").append(category.getDisplayName()).append("\n");
        sb.append("Quantity: ").append(quantity).append("\n");
        
        int unitPrice = getUnitSellPrice(scarcityMultiplier);
        int totalPrice = getTotalSellPrice(scarcityMultiplier);
        
        sb.append("Value: ").append(unitPrice).append(" gold each\n");
        if (quantity > 1) {
            sb.append("Total: ").append(totalPrice).append(" gold");
        }
        
        return sb.toString();
    }
    
    // ==================== GETTERS ====================
    
    public String getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public Object getSubtype() { return subtype; }
    public GrainType getGrainType() { return grainType; }
    public Quality getQuality() { return quality; }
    public int getQuantity() { return quantity; }
    public int getBasePrice() { return basePrice; }
    
    public void setQuantity(int qty) { this.quantity = Math.max(0, qty); }
    public void setQuality(Quality q) { this.quality = q; }
    
    /**
     * Creates a copy of this item with specified quantity.
     */
    public ResourceItem copy(int newQuantity) {
        if (category == Category.GRAIN && grainType != null) {
            return new ResourceItem(grainType, quality, newQuantity);
        }
        ResourceItem copy = new ResourceItem(id, name, category, basePrice);
        copy.quality = quality;
        copy.quantity = newQuantity;
        return copy;
    }
    
    @Override
    public String toString() {
        return quantity + "x " + name + " (" + quality.getDisplayName() + ")";
    }
}
