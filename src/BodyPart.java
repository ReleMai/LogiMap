import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a body part of the player character.
 * Each body part can take damage and have gear equipped to it.
 */
public class BodyPart {
    
    /**
     * Types of body parts
     */
    public enum Type {
        HEAD("Head", Equipment.Slot.HEAD, 25),
        CHEST("Chest", Equipment.Slot.BODY, 40),
        RIGHT_ARM("Right Arm", Equipment.Slot.MAIN_HAND, 15),
        LEFT_ARM("Left Arm", Equipment.Slot.OFF_HAND, 15),
        RIGHT_LEG("Right Leg", Equipment.Slot.LEGS, 20),
        LEFT_LEG("Left Leg", Equipment.Slot.FEET, 20);
        
        private final String displayName;
        private final Equipment.Slot gearSlot;
        private final int baseHealth;
        
        Type(String displayName, Equipment.Slot gearSlot, int baseHealth) {
            this.displayName = displayName;
            this.gearSlot = gearSlot;
            this.baseHealth = baseHealth;
        }
        
        public String getDisplayName() { return displayName; }
        public Equipment.Slot getGearSlot() { return gearSlot; }
        public int getBaseHealth() { return baseHealth; }
    }
    
    // Part identity
    private final Type type;
    private String name;
    
    // Health/damage
    private int maxHealth;
    private int currentHealth;
    private boolean isCrippled = false;
    private boolean isBroken = false;
    
    // Armor/protection
    private int armor = 0;
    private Equipment equippedGear = null;
    
    // Visual state
    private double damageFlash = 0; // Flashes red when hit
    private boolean isHighlighted = false;
    
    // Colors
    private static final Color SKIN_COLOR = Color.web("#e8c4a0");
    private static final Color DAMAGED_TINT = Color.web("#ff6b6b");
    private static final Color CRIPPLED_TINT = Color.web("#8b4513");
    private static final Color UNDERWEAR_COLOR = Color.web("#f0f0e8");
    private static final Color OUTLINE_COLOR = Color.web("#2a1a10");
    
    /**
     * Creates a new body part.
     */
    public BodyPart(Type type) {
        this.type = type;
        this.name = type.getDisplayName();
        this.maxHealth = type.getBaseHealth();
        this.currentHealth = maxHealth;
    }
    
    /**
     * Takes damage to this body part.
     * Returns actual damage dealt after armor reduction.
     */
    public int takeDamage(int rawDamage) {
        // Calculate damage after armor
        int effectiveArmor = armor;
        if (equippedGear != null) {
            effectiveArmor += equippedGear.getDefense();
        }
        
        int actualDamage = Math.max(1, rawDamage - effectiveArmor);
        currentHealth -= actualDamage;
        damageFlash = 1.0; // Start flash animation
        
        // Check for status changes
        if (currentHealth <= maxHealth * 0.25 && !isCrippled) {
            isCrippled = true;
        }
        if (currentHealth <= 0) {
            currentHealth = 0;
            isBroken = true;
        }
        
        return actualDamage;
    }
    
    /**
     * Heals this body part.
     */
    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
        
        // Recovery from status effects
        if (currentHealth > maxHealth * 0.25) {
            isCrippled = false;
        }
        if (currentHealth > 0) {
            isBroken = false;
        }
    }
    
    /**
     * Equips gear to this body part.
     * @return Previously equipped gear, or null
     */
    public Equipment equipGear(Equipment gear) {
        Equipment old = this.equippedGear;
        this.equippedGear = gear;
        return old;
    }
    
    /**
     * Removes equipped gear.
     * @return The removed gear, or null
     */
    public Equipment unequipGear() {
        Equipment old = this.equippedGear;
        this.equippedGear = null;
        return old;
    }
    
    /**
     * Updates animation state.
     */
    public void update(double deltaTime) {
        if (damageFlash > 0) {
            damageFlash = Math.max(0, damageFlash - deltaTime * 3);
        }
    }
    
    /**
     * Gets the current skin color based on damage state.
     */
    public Color getCurrentSkinColor() {
        Color base = SKIN_COLOR;
        
        if (isBroken) {
            base = CRIPPLED_TINT;
        } else if (isCrippled) {
            // Blend toward damaged tint
            base = interpolateColor(SKIN_COLOR, CRIPPLED_TINT, 0.5);
        } else if (currentHealth < maxHealth * 0.5) {
            // Show some damage
            double ratio = 1.0 - (currentHealth / (double)(maxHealth * 0.5));
            base = interpolateColor(SKIN_COLOR, DAMAGED_TINT, ratio * 0.3);
        }
        
        // Flash effect
        if (damageFlash > 0) {
            base = interpolateColor(base, Color.RED, damageFlash * 0.5);
        }
        
        return base;
    }
    
    /**
     * Interpolates between two colors.
     */
    private Color interpolateColor(Color a, Color b, double t) {
        return Color.color(
            a.getRed() + (b.getRed() - a.getRed()) * t,
            a.getGreen() + (b.getGreen() - a.getGreen()) * t,
            a.getBlue() + (b.getBlue() - a.getBlue()) * t
        );
    }
    
    /**
     * Renders a health bar for this body part.
     */
    public void renderHealthBar(GraphicsContext gc, double x, double y, double width, double height) {
        // Background
        gc.setFill(Color.web("#333333"));
        gc.fillRect(x, y, width, height);
        
        // Health fill
        double healthRatio = currentHealth / (double) maxHealth;
        Color healthColor = Color.GREEN;
        if (healthRatio < 0.25) {
            healthColor = Color.RED;
        } else if (healthRatio < 0.5) {
            healthColor = Color.ORANGE;
        } else if (healthRatio < 0.75) {
            healthColor = Color.YELLOW;
        }
        
        gc.setFill(healthColor);
        gc.fillRect(x + 1, y + 1, (width - 2) * healthRatio, height - 2);
        
        // Border
        gc.setStroke(Color.web("#666666"));
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
    }
    
    // === Getters ===
    
    public Type getType() { return type; }
    public String getName() { return name; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public boolean isCrippled() { return isCrippled; }
    public boolean isBroken() { return isBroken; }
    public int getArmor() { return armor; }
    public Equipment getEquippedGear() { return equippedGear; }
    public boolean isHighlighted() { return isHighlighted; }
    public void setHighlighted(boolean highlighted) { this.isHighlighted = highlighted; }
    
    public double getHealthPercent() {
        return currentHealth / (double) maxHealth;
    }
    
    public int getTotalArmor() {
        int total = armor;
        if (equippedGear != null) {
            total += equippedGear.getDefense();
        }
        return total;
    }
    
    /**
     * Gets status text for this body part.
     */
    public String getStatusText() {
        if (isBroken) return "BROKEN";
        if (isCrippled) return "Crippled";
        if (currentHealth < maxHealth) return "Wounded";
        return "Healthy";
    }
    
    // Static colors for rendering
    public static Color getSkinColor() { return SKIN_COLOR; }
    public static Color getUnderwearColor() { return UNDERWEAR_COLOR; }
    public static Color getOutlineColor() { return OUTLINE_COLOR; }
}
