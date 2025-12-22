import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Currency system using Copper, Silver, Gold.
 * 100 Copper = 1 Silver
 * 100 Silver = 1 Gold
 */
public class Currency {
    
    // Total value stored as copper for easy calculations
    private long totalCopper;
    
    public static final int COPPER_PER_SILVER = 100;
    public static final int SILVER_PER_GOLD = 100;
    public static final int COPPER_PER_GOLD = COPPER_PER_SILVER * SILVER_PER_GOLD; // 10,000
    
    public Currency() {
        this.totalCopper = 0;
    }
    
    public Currency(int copper) {
        this.totalCopper = copper;
    }
    
    public Currency(int gold, int silver, int copper) {
        this.totalCopper = (long) gold * COPPER_PER_GOLD + 
                          (long) silver * COPPER_PER_SILVER + 
                          copper;
    }
    
    // === Getters for display ===
    
    public int getGold() {
        return (int) (totalCopper / COPPER_PER_GOLD);
    }
    
    public int getSilver() {
        return (int) ((totalCopper % COPPER_PER_GOLD) / COPPER_PER_SILVER);
    }
    
    public int getCopper() {
        return (int) (totalCopper % COPPER_PER_SILVER);
    }
    
    public long getTotalCopper() {
        return totalCopper;
    }
    
    // === Operations ===
    
    public void add(Currency other) {
        this.totalCopper += other.totalCopper;
    }
    
    public void add(int copper) {
        this.totalCopper += copper;
    }
    
    public void add(long copper) {
        this.totalCopper += copper;
    }
    
    public void addGold(int gold) {
        this.totalCopper += (long) gold * COPPER_PER_GOLD;
    }
    
    public void addSilver(int silver) {
        this.totalCopper += (long) silver * COPPER_PER_SILVER;
    }
    
    public void addCopper(int copper) {
        this.totalCopper += copper;
    }
    
    public boolean canAfford(Currency cost) {
        return this.totalCopper >= cost.totalCopper;
    }
    
    public boolean canAfford(long copperCost) {
        return this.totalCopper >= copperCost;
    }
    
    public boolean subtract(Currency cost) {
        if (canAfford(cost)) {
            this.totalCopper -= cost.totalCopper;
            return true;
        }
        return false;
    }
    
    public boolean subtract(long copperCost) {
        if (canAfford(copperCost)) {
            this.totalCopper -= copperCost;
            return true;
        }
        return false;
    }
    
    public void set(long totalCopper) {
        this.totalCopper = Math.max(0, totalCopper);
    }
    
    // === Display ===
    
    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        int gold = getGold();
        int silver = getSilver();
        int copper = getCopper();
        
        if (gold > 0) sb.append(gold).append("g ");
        if (silver > 0 || gold > 0) sb.append(silver).append("s ");
        sb.append(copper).append("c");
        
        return sb.toString().trim();
    }
    
    public String toFullString() {
        return String.format("%d Gold, %d Silver, %d Copper", getGold(), getSilver(), getCopper());
    }
    
    @Override
    public String toString() {
        return toShortString();
    }
    
    // === Static Factory Methods ===
    
    public static Currency gold(int amount) {
        return new Currency(amount, 0, 0);
    }
    
    public static Currency silver(int amount) {
        return new Currency(0, amount, 0);
    }
    
    public static Currency copper(int amount) {
        return new Currency(amount);
    }
    
    public static Currency fromCopper(long copper) {
        Currency c = new Currency();
        c.totalCopper = copper;
        return c;
    }
    
    public Currency copy() {
        return Currency.fromCopper(this.totalCopper);
    }
    
    // === Sprite Rendering ===
    
    /**
     * Renders a copper coin sprite.
     */
    public static void renderCopperCoin(GraphicsContext gc, double x, double y, double size) {
        // Coin base (copper brown)
        gc.setFill(Color.web("#b87333"));
        gc.fillOval(x, y, size, size);
        
        // Inner ring
        gc.setStroke(Color.web("#8b4513"));
        gc.setLineWidth(size * 0.08);
        gc.strokeOval(x + size * 0.15, y + size * 0.15, size * 0.7, size * 0.7);
        
        // Center mark
        gc.setFill(Color.web("#cd853f"));
        gc.fillOval(x + size * 0.35, y + size * 0.35, size * 0.3, size * 0.3);
        
        // Shine
        gc.setFill(Color.color(1, 1, 1, 0.3));
        gc.fillOval(x + size * 0.2, y + size * 0.15, size * 0.25, size * 0.15);
    }
    
    /**
     * Renders a silver coin sprite.
     */
    public static void renderSilverCoin(GraphicsContext gc, double x, double y, double size) {
        // Coin base (silver)
        gc.setFill(Color.web("#c0c0c0"));
        gc.fillOval(x, y, size, size);
        
        // Inner ring
        gc.setStroke(Color.web("#808080"));
        gc.setLineWidth(size * 0.08);
        gc.strokeOval(x + size * 0.15, y + size * 0.15, size * 0.7, size * 0.7);
        
        // Crown/star mark
        gc.setFill(Color.web("#a0a0a0"));
        double cx = x + size * 0.5;
        double cy = y + size * 0.5;
        double r = size * 0.15;
        // Simple star shape
        gc.fillPolygon(
            new double[]{cx, cx + r * 0.4, cx + r, cx + r * 0.5, cx + r * 0.7, cx, cx - r * 0.7, cx - r * 0.5, cx - r, cx - r * 0.4},
            new double[]{cy - r, cy - r * 0.4, cy - r * 0.3, cy + r * 0.2, cy + r, cy + r * 0.5, cy + r, cy + r * 0.2, cy - r * 0.3, cy - r * 0.4},
            10
        );
        
        // Shine
        gc.setFill(Color.color(1, 1, 1, 0.4));
        gc.fillOval(x + size * 0.2, y + size * 0.15, size * 0.25, size * 0.15);
    }
    
    /**
     * Renders a gold coin sprite.
     */
    public static void renderGoldCoin(GraphicsContext gc, double x, double y, double size) {
        // Coin base (gold)
        gc.setFill(Color.web("#ffd700"));
        gc.fillOval(x, y, size, size);
        
        // Inner ring
        gc.setStroke(Color.web("#daa520"));
        gc.setLineWidth(size * 0.1);
        gc.strokeOval(x + size * 0.12, y + size * 0.12, size * 0.76, size * 0.76);
        
        // Crown emblem
        gc.setFill(Color.web("#b8860b"));
        double cx = x + size * 0.5;
        double baseY = y + size * 0.6;
        // Crown base
        gc.fillRect(cx - size * 0.25, baseY, size * 0.5, size * 0.12);
        // Crown points
        gc.fillPolygon(
            new double[]{cx - size * 0.25, cx - size * 0.15, cx, cx + size * 0.15, cx + size * 0.25},
            new double[]{baseY, y + size * 0.35, baseY - size * 0.1, y + size * 0.35, baseY},
            5
        );
        
        // Shine
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.fillOval(x + size * 0.18, y + size * 0.12, size * 0.3, size * 0.18);
    }
    
    /**
     * Renders a stack of coins.
     */
    public static void renderCoinStack(GraphicsContext gc, double x, double y, double coinSize, int count, String type) {
        count = Math.min(count, 5); // Max 5 visible coins in stack
        
        for (int i = 0; i < count; i++) {
            double offset = i * coinSize * 0.2;
            switch (type.toLowerCase()) {
                case "gold":
                    renderGoldCoin(gc, x, y - offset, coinSize);
                    break;
                case "silver":
                    renderSilverCoin(gc, x, y - offset, coinSize);
                    break;
                default:
                    renderCopperCoin(gc, x, y - offset, coinSize);
                    break;
            }
        }
    }
}
