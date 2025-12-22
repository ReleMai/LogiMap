import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Template system for gear rendering on player sprites.
 * Provides standardized rendering methods that modders can extend.
 * 
 * Each template defines how gear appears on a specific body part:
 * - HEAD: Helmets, hats, crowns, hoods
 * - CHEST: Shirts, armor, robes
 * - ARMS: Gloves, bracers, sleeves
 * - LEGS: Pants, greaves, skirts
 * - FEET: Boots, sandals, shoes
 * - MAIN_HAND: Weapons
 * - OFF_HAND: Shields, torches, secondary weapons
 * - BACK: Capes, cloaks, backpacks
 */
public class GearTemplate {
    
    /**
     * Gear slot types for the modular body system
     */
    public enum SlotType {
        HEAD,
        CHEST,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG,
        FEET,
        MAIN_HAND,
        OFF_HAND,
        BACK
    }
    
    // Template identity
    private final String id;
    private final String name;
    private final SlotType slot;
    
    // Visual properties
    private Color primaryColor;
    private Color secondaryColor;
    private Color accentColor;
    private Color outlineColor;
    
    // Stats
    private int defense;
    private int attack;
    private int value;
    
    // Rendering style
    private RenderStyle style;
    
    public enum RenderStyle {
        CLOTH,      // Soft fabric look
        LEATHER,    // Brown with stitching
        CHAINMAIL,  // Ring pattern
        PLATE,      // Solid metal
        ROYAL,      // Gold trim
        RAGGED,     // Torn/worn look
        WOODEN,     // Simple wood
        IRON,       // Gray metal
        STEEL,      // Polished metal
        GOLD        // Golden
    }
    
    public GearTemplate(String id, String name, SlotType slot) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.primaryColor = Color.GRAY;
        this.secondaryColor = Color.DARKGRAY;
        this.accentColor = Color.LIGHTGRAY;
        this.outlineColor = Color.web("#2a1a10");
        this.style = RenderStyle.CLOTH;
    }
    
    // === Builder Pattern for Easy Configuration ===
    
    public GearTemplate withColors(Color primary, Color secondary, Color accent) {
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.accentColor = accent;
        return this;
    }
    
    public GearTemplate withStyle(RenderStyle style) {
        this.style = style;
        return this;
    }
    
    public GearTemplate withStats(int defense, int attack, int value) {
        this.defense = defense;
        this.attack = attack;
        this.value = value;
        return this;
    }
    
    // === Rendering Methods ===
    
    /**
     * Main render method - delegates to slot-specific rendering
     */
    public void render(GraphicsContext gc, double x, double y, double width, double height) {
        switch (slot) {
            case HEAD:
                renderHead(gc, x, y, width, height);
                break;
            case CHEST:
                renderChest(gc, x, y, width, height);
                break;
            case LEFT_ARM:
            case RIGHT_ARM:
                renderArm(gc, x, y, width, height);
                break;
            case LEFT_LEG:
            case RIGHT_LEG:
                renderLeg(gc, x, y, width, height);
                break;
            case FEET:
                renderFeet(gc, x, y, width, height);
                break;
            case MAIN_HAND:
                renderMainHand(gc, x, y, width, height);
                break;
            case OFF_HAND:
                renderOffHand(gc, x, y, width, height);
                break;
            case BACK:
                renderBack(gc, x, y, width, height);
                break;
        }
    }
    
    /**
     * Renders headgear - Override this for custom head items
     */
    protected void renderHead(GraphicsContext gc, double x, double y, double w, double h) {
        switch (style) {
            case CLOTH:
            case LEATHER:
                // Cap style
                gc.setFill(primaryColor);
                gc.fillArc(x - w * 0.1, y - h * 0.15, w * 1.2, h * 0.6, 0, 180, 
                          javafx.scene.shape.ArcType.ROUND);
                gc.setStroke(outlineColor);
                gc.setLineWidth(1);
                gc.strokeArc(x - w * 0.1, y - h * 0.15, w * 1.2, h * 0.6, 0, 180,
                            javafx.scene.shape.ArcType.OPEN);
                break;
                
            case CHAINMAIL:
            case IRON:
            case PLATE:
            case STEEL:
                // Helmet style
                gc.setFill(primaryColor);
                gc.fillArc(x - w * 0.15, y - h * 0.25, w * 1.3, h * 0.75, 0, 180,
                          javafx.scene.shape.ArcType.ROUND);
                // Nose guard
                gc.fillRect(x + w * 0.4, y + h * 0.15, w * 0.12, h * 0.35);
                gc.setStroke(outlineColor);
                gc.setLineWidth(1.5);
                gc.strokeArc(x - w * 0.15, y - h * 0.25, w * 1.3, h * 0.75, 0, 180,
                            javafx.scene.shape.ArcType.OPEN);
                break;
                
            case ROYAL:
            case GOLD:
                // Crown style
                gc.setFill(Color.web("#ffd700"));
                double crownY = y - h * 0.1;
                gc.fillRect(x - w * 0.1, crownY, w * 1.2, h * 0.2);
                // Points
                double pointW = w * 0.24;
                for (int i = 0; i < 5; i++) {
                    double px = x - w * 0.1 + i * pointW;
                    gc.fillPolygon(
                        new double[]{px, px + pointW / 2, px + pointW},
                        new double[]{crownY, crownY - h * 0.25, crownY}, 3);
                }
                // Gems
                gc.setFill(Color.web("#ff0040"));
                gc.fillOval(x + w * 0.4, crownY + h * 0.03, w * 0.12, h * 0.12);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders chest armor - Override for custom body armor
     */
    protected void renderChest(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(primaryColor);
        gc.fillRoundRect(x, y, w, h, w * 0.2, w * 0.2);
        
        switch (style) {
            case RAGGED:
                // Torn lines
                gc.setStroke(secondaryColor);
                gc.setLineWidth(0.5);
                gc.strokeLine(x + w * 0.2, y + h * 0.3, x + w * 0.35, y + h * 0.5);
                gc.strokeLine(x + w * 0.6, y + h * 0.6, x + w * 0.75, y + h * 0.8);
                break;
                
            case LEATHER:
                // Lacing
                gc.setStroke(secondaryColor);
                gc.setLineWidth(1);
                for (int i = 0; i < 3; i++) {
                    double ly = y + h * 0.25 + i * h * 0.2;
                    gc.strokeLine(x + w * 0.4, ly, x + w * 0.6, ly);
                }
                break;
                
            case CHAINMAIL:
                // Ring pattern
                gc.setStroke(secondaryColor);
                gc.setLineWidth(0.5);
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 4; col++) {
                        double cx = x + w * 0.12 + col * w * 0.2;
                        double cy = y + h * 0.1 + row * h * 0.15;
                        gc.strokeOval(cx, cy, w * 0.12, h * 0.08);
                    }
                }
                break;
                
            case PLATE:
            case IRON:
            case STEEL:
                // Plate lines
                gc.setStroke(secondaryColor);
                gc.setLineWidth(1.5);
                gc.strokeLine(x + w * 0.5, y + h * 0.1, x + w * 0.5, y + h * 0.9);
                gc.strokeLine(x + w * 0.1, y + h * 0.5, x + w * 0.9, y + h * 0.5);
                // Rivets
                gc.setFill(accentColor);
                gc.fillOval(x + w * 0.15, y + h * 0.15, w * 0.06, h * 0.05);
                gc.fillOval(x + w * 0.79, y + h * 0.15, w * 0.06, h * 0.05);
                gc.fillOval(x + w * 0.15, y + h * 0.8, w * 0.06, h * 0.05);
                gc.fillOval(x + w * 0.79, y + h * 0.8, w * 0.06, h * 0.05);
                break;
                
            case ROYAL:
                // Gold trim
                gc.setStroke(Color.web("#ffd700"));
                gc.setLineWidth(2);
                gc.strokeRoundRect(x + 2, y + 2, w - 4, h - 4, w * 0.15, w * 0.15);
                // Emblem
                gc.setFill(Color.web("#ffd700"));
                gc.fillOval(x + w * 0.35, y + h * 0.35, w * 0.3, h * 0.2);
                break;
                
            default:
                // Basic outline
                gc.setStroke(outlineColor);
                gc.setLineWidth(1);
                gc.strokeRoundRect(x, y, w, h, w * 0.2, w * 0.2);
                break;
        }
    }
    
    /**
     * Renders arm covering (bracers, sleeves)
     */
    protected void renderArm(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(primaryColor);
        gc.fillRoundRect(x, y, w, h, w * 0.4, w * 0.4);
        gc.setStroke(outlineColor);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, w, h, w * 0.4, w * 0.4);
    }
    
    /**
     * Renders leg armor (pants, greaves)
     */
    protected void renderLeg(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(primaryColor);
        gc.fillRoundRect(x - w * 0.1, y, w * 1.2, h * 0.5, w * 0.2, w * 0.2);
        
        if (style == RenderStyle.RAGGED) {
            gc.setStroke(secondaryColor);
            gc.setLineWidth(0.5);
            gc.strokeLine(x + w * 0.1, y + h * 0.35, x + w * 0.3, y + h * 0.45);
        }
        
        gc.setStroke(outlineColor);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x - w * 0.1, y, w * 1.2, h * 0.5, w * 0.2, w * 0.2);
    }
    
    /**
     * Renders footwear
     */
    protected void renderFeet(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(primaryColor);
        gc.fillRoundRect(x - w * 0.2, y, w * 1.4, h * 1.5, w * 0.3, w * 0.3);
        gc.setStroke(outlineColor);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x - w * 0.2, y, w * 1.4, h * 1.5, w * 0.3, w * 0.3);
    }
    
    /**
     * Renders main hand weapon
     */
    protected void renderMainHand(GraphicsContext gc, double handX, double handY, double w, double h) {
        double scale = w * 0.1;
        
        switch (style) {
            case WOODEN:
                // Wooden sword
                gc.setFill(Color.web("#8b6914"));
                gc.fillRect(handX - scale, handY - scale * 20, scale * 2, scale * 20);
                gc.fillPolygon(
                    new double[]{handX - scale, handX, handX + scale},
                    new double[]{handY - scale * 20, handY - scale * 25, handY - scale * 20}, 3);
                // Handle
                gc.setFill(Color.web("#5c4a1a"));
                gc.fillRect(handX - scale * 0.5, handY, scale, scale * 6);
                break;
                
            case IRON:
            case STEEL:
                // Metal sword
                gc.setFill(primaryColor);
                gc.fillRect(handX - scale * 0.8, handY - scale * 22, scale * 1.6, scale * 22);
                gc.fillPolygon(
                    new double[]{handX - scale * 0.8, handX, handX + scale * 0.8},
                    new double[]{handY - scale * 22, handY - scale * 28, handY - scale * 22}, 3);
                // Guard
                gc.setFill(secondaryColor);
                gc.fillRect(handX - scale * 3, handY - scale, scale * 6, scale * 2);
                // Handle
                gc.setFill(accentColor);
                gc.fillRect(handX - scale * 0.4, handY, scale * 0.8, scale * 6);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders off-hand item (shield, torch)
     */
    protected void renderOffHand(GraphicsContext gc, double handX, double handY, double w, double h) {
        double scale = w * 0.1;
        
        switch (style) {
            case WOODEN:
                // Wooden shield
                gc.setFill(Color.web("#8b6914"));
                gc.fillOval(handX - scale * 6, handY - scale * 10, scale * 8, scale * 12);
                gc.setStroke(Color.web("#5c4a1a"));
                gc.setLineWidth(2);
                gc.strokeOval(handX - scale * 6, handY - scale * 10, scale * 8, scale * 12);
                break;
                
            case IRON:
            case STEEL:
                // Metal shield
                gc.setFill(primaryColor);
                gc.fillRoundRect(handX - scale * 6, handY - scale * 12, scale * 8, scale * 14, scale * 2, scale * 2);
                gc.setStroke(secondaryColor);
                gc.setLineWidth(2);
                gc.strokeRoundRect(handX - scale * 6, handY - scale * 12, scale * 8, scale * 14, scale * 2, scale * 2);
                // Boss
                gc.setFill(accentColor);
                gc.fillOval(handX - scale * 3, handY - scale * 6, scale * 3, scale * 3);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders back items (capes, cloaks)
     */
    protected void renderBack(GraphicsContext gc, double x, double y, double w, double h) {
        gc.setFill(primaryColor);
        // Cape shape - flows down
        double[] xPoints = {x, x + w * 0.1, x + w * 0.9, x + w};
        double[] yPoints = {y, y + h, y + h, y};
        gc.fillPolygon(xPoints, yPoints, 4);
        
        // Bottom wave
        gc.beginPath();
        gc.moveTo(x + w * 0.1, y + h);
        gc.quadraticCurveTo(x + w * 0.3, y + h * 1.1, x + w * 0.5, y + h);
        gc.quadraticCurveTo(x + w * 0.7, y + h * 0.9, x + w * 0.9, y + h);
        gc.closePath();
        gc.fill();
        
        gc.setStroke(outlineColor);
        gc.setLineWidth(1);
        gc.strokePolygon(xPoints, yPoints, 4);
    }
    
    // === Getters ===
    
    public String getId() { return id; }
    public String getName() { return name; }
    public SlotType getSlot() { return slot; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public Color getAccentColor() { return accentColor; }
    public int getDefense() { return defense; }
    public int getAttack() { return attack; }
    public int getValue() { return value; }
    public RenderStyle getStyle() { return style; }
}
