import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Equipment system for player and NPC sprites.
 * Each equipment piece knows how to render itself on the appropriate body part.
 */
public class Equipment {
    
    public enum Slot {
        HEAD, BODY, LEGS, FEET, MAIN_HAND, OFF_HAND, CAPE
    }
    
    public enum Type {
        // Starting clothes (ragged)
        RAGGED_SHIRT, RAGGED_PANTS,
        // Headgear
        LEATHER_CAP, IRON_HELM, STEEL_HELM, CROWN,
        // Body armor
        CLOTH_SHIRT, LEATHER_VEST, CHAINMAIL, IRON_PLATE, STEEL_PLATE, ROYAL_ARMOR,
        // Leg armor  
        CLOTH_PANTS, LEATHER_PANTS, CHAINMAIL_LEGS, IRON_GREAVES, STEEL_GREAVES,
        // Footwear
        SANDALS, LEATHER_BOOTS, IRON_BOOTS, STEEL_BOOTS,
        // Weapons (main hand)
        WOODEN_SWORD, IRON_SWORD, STEEL_SWORD, ROYAL_SWORD, AXE, MACE, SPEAR,
        // Off-hand
        WOODEN_SHIELD, IRON_SHIELD, STEEL_SHIELD, TORCH,
        // Capes
        CLOTH_CAPE, WOOL_CAPE, ROYAL_CAPE, MERCHANT_CAPE
    }
    
    private final Type type;
    private final Slot slot;
    private final String name;
    private final int value;
    private final int defense;
    private final int attack;
    
    // Visual properties
    private final Color primaryColor;
    private final Color secondaryColor;
    private final Color accentColor;
    
    public Equipment(Type type) {
        this.type = type;
        this.slot = getSlotForType(type);
        this.name = getNameForType(type);
        this.value = getValueForType(type);
        this.defense = getDefenseForType(type);
        this.attack = getAttackForType(type);
        this.primaryColor = getPrimaryColorForType(type);
        this.secondaryColor = getSecondaryColorForType(type);
        this.accentColor = getAccentColorForType(type);
    }
    
    // === Render Methods for Different Body Parts ===
    
    /**
     * Renders headgear on the head.
     */
    public void renderOnHead(GraphicsContext gc, double x, double y, double width, double height) {
        switch (type) {
            case LEATHER_CAP:
                gc.setFill(primaryColor);
                gc.fillArc(x - width * 0.1, y - height * 0.2, width * 1.2, height * 0.7, 0, 180, 
                          javafx.scene.shape.ArcType.ROUND);
                gc.setStroke(secondaryColor);
                gc.setLineWidth(1);
                gc.strokeArc(x - width * 0.1, y - height * 0.2, width * 1.2, height * 0.7, 0, 180,
                            javafx.scene.shape.ArcType.OPEN);
                break;
                
            case IRON_HELM:
            case STEEL_HELM:
                // Rounded helmet
                gc.setFill(primaryColor);
                gc.fillArc(x - width * 0.15, y - height * 0.3, width * 1.3, height * 0.8, 0, 180,
                          javafx.scene.shape.ArcType.ROUND);
                // Nose guard
                gc.fillRect(x + width * 0.35, y + height * 0.2, width * 0.15, height * 0.4);
                gc.setStroke(secondaryColor);
                gc.setLineWidth(2);
                gc.strokeArc(x - width * 0.15, y - height * 0.3, width * 1.3, height * 0.8, 0, 180,
                            javafx.scene.shape.ArcType.OPEN);
                break;
                
            case CROWN:
                // Crown base
                gc.setFill(Color.web("#ffd700"));
                gc.fillRect(x - width * 0.1, y - height * 0.1, width * 1.2, height * 0.25);
                // Crown points
                double pointWidth = width * 0.2;
                for (int i = 0; i < 5; i++) {
                    double px = x + i * pointWidth;
                    gc.fillPolygon(
                        new double[]{px, px + pointWidth / 2, px + pointWidth},
                        new double[]{y - height * 0.1, y - height * 0.4, y - height * 0.1}, 3);
                }
                // Gems
                gc.setFill(Color.web("#ff0040"));
                gc.fillOval(x + width * 0.4, y - height * 0.05, width * 0.15, height * 0.15);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders body armor on the torso.
     */
    public void renderOnBody(GraphicsContext gc, double x, double y, double width, double height) {
        switch (type) {
            case RAGGED_SHIRT:
            case CLOTH_SHIRT:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x, y, width, height, width * 0.25, width * 0.25);
                // Worn/tattered effect for ragged
                if (type == Type.RAGGED_SHIRT) {
                    gc.setStroke(secondaryColor);
                    gc.setLineWidth(0.5);
                    // Patches/tears
                    gc.strokeLine(x + width * 0.2, y + height * 0.3, x + width * 0.35, y + height * 0.5);
                    gc.strokeLine(x + width * 0.6, y + height * 0.6, x + width * 0.75, y + height * 0.75);
                    gc.strokeLine(x + width * 0.15, y + height * 0.7, x + width * 0.3, y + height * 0.85);
                } else {
                    // Clean stitching for cloth shirt
                    gc.setStroke(secondaryColor);
                    gc.setLineWidth(0.5);
                    gc.strokeLine(x + width * 0.5, y + height * 0.15, x + width * 0.5, y + height * 0.4);
                }
                break;
                
            case LEATHER_VEST:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x, y, width, height, width * 0.2, width * 0.2);
                // Lacing detail
                gc.setStroke(secondaryColor);
                gc.setLineWidth(1);
                for (int i = 0; i < 4; i++) {
                    double ly = y + height * 0.2 + i * height * 0.15;
                    gc.strokeLine(x + width * 0.4, ly, x + width * 0.6, ly);
                }
                break;
                
            case CHAINMAIL:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x, y, width, height, width * 0.2, width * 0.2);
                // Chainmail pattern
                gc.setStroke(secondaryColor);
                gc.setLineWidth(0.5);
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 4; col++) {
                        double cx = x + width * 0.15 + col * width * 0.2;
                        double cy = y + height * 0.1 + row * height * 0.13;
                        gc.strokeOval(cx, cy, width * 0.15, height * 0.08);
                    }
                }
                break;
                
            case IRON_PLATE:
            case STEEL_PLATE:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x, y, width, height, width * 0.15, width * 0.15);
                // Plate segments
                gc.setStroke(secondaryColor);
                gc.setLineWidth(1.5);
                gc.strokeLine(x + width * 0.5, y + height * 0.1, x + width * 0.5, y + height * 0.9);
                gc.strokeLine(x + width * 0.1, y + height * 0.5, x + width * 0.9, y + height * 0.5);
                // Rivets
                gc.setFill(accentColor);
                gc.fillOval(x + width * 0.15, y + height * 0.15, width * 0.08, height * 0.06);
                gc.fillOval(x + width * 0.77, y + height * 0.15, width * 0.08, height * 0.06);
                gc.fillOval(x + width * 0.15, y + height * 0.79, width * 0.08, height * 0.06);
                gc.fillOval(x + width * 0.77, y + height * 0.79, width * 0.08, height * 0.06);
                break;
                
            case ROYAL_ARMOR:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x, y, width, height, width * 0.15, width * 0.15);
                // Gold trim
                gc.setStroke(Color.web("#ffd700"));
                gc.setLineWidth(2);
                gc.strokeRoundRect(x + 2, y + 2, width - 4, height - 4, width * 0.12, width * 0.12);
                // Royal emblem
                gc.setFill(Color.web("#ffd700"));
                gc.fillOval(x + width * 0.35, y + height * 0.35, width * 0.3, height * 0.25);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders leg armor.
     */
    public void renderOnLegs(GraphicsContext gc, double x, double y, double width, double height) {
        switch (type) {
            case RAGGED_PANTS:
            case CLOTH_PANTS:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x - width * 0.1, y, width * 1.2, height * 0.65, width * 0.2, width * 0.2);
                // Worn effect for ragged
                if (type == Type.RAGGED_PANTS) {
                    gc.setStroke(secondaryColor);
                    gc.setLineWidth(0.5);
                    // Patches/frayed edges
                    gc.strokeLine(x + width * 0.1, y + height * 0.45, x + width * 0.25, y + height * 0.55);
                    gc.strokeLine(x + width * 0.7, y + height * 0.5, x + width * 0.85, y + height * 0.6);
                }
                break;
                
            case LEATHER_PANTS:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x - width * 0.1, y, width * 1.2, height * 0.6, width * 0.2, width * 0.2);
                break;
                
            case CHAINMAIL_LEGS:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x - width * 0.1, y, width * 1.2, height * 0.7, width * 0.2, width * 0.2);
                gc.setStroke(secondaryColor);
                gc.setLineWidth(0.5);
                for (int i = 0; i < 4; i++) {
                    double ly = y + i * height * 0.15;
                    gc.strokeLine(x, ly, x + width, ly);
                }
                break;
                
            case IRON_GREAVES:
            case STEEL_GREAVES:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x - width * 0.15, y + height * 0.3, width * 1.3, height * 0.6, 
                                width * 0.2, width * 0.2);
                gc.setStroke(secondaryColor);
                gc.setLineWidth(1);
                gc.strokeRoundRect(x - width * 0.15, y + height * 0.3, width * 1.3, height * 0.6,
                                  width * 0.2, width * 0.2);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders footwear.
     */
    public void renderOnFeet(GraphicsContext gc, double x, double y, double width, double height) {
        switch (type) {
            case SANDALS:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x - width * 0.2, y, width * 1.4, height * 1.2, width * 0.3, width * 0.3);
                break;
                
            case LEATHER_BOOTS:
            case IRON_BOOTS:
            case STEEL_BOOTS:
                gc.setFill(primaryColor);
                gc.fillRoundRect(x - width * 0.3, y - height * 0.5, width * 1.6, height * 2, 
                                width * 0.3, width * 0.3);
                gc.setStroke(secondaryColor);
                gc.setLineWidth(1);
                gc.strokeRoundRect(x - width * 0.3, y - height * 0.5, width * 1.6, height * 2,
                                  width * 0.3, width * 0.3);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders weapon in hand.
     */
    public void renderInHand(GraphicsContext gc, double handX, double handY, double spriteSize) {
        double weaponScale = spriteSize * 0.015;
        
        switch (type) {
            case WOODEN_SWORD:
            case IRON_SWORD:
            case STEEL_SWORD:
            case ROYAL_SWORD:
                // Blade
                gc.setFill(primaryColor);
                gc.fillRect(handX - weaponScale, handY - weaponScale * 25, 
                           weaponScale * 3, weaponScale * 25);
                // Point
                gc.fillPolygon(
                    new double[]{handX - weaponScale, handX + weaponScale * 0.5, handX + weaponScale * 2},
                    new double[]{handY - weaponScale * 25, handY - weaponScale * 32, handY - weaponScale * 25}, 3);
                // Guard
                gc.setFill(secondaryColor);
                gc.fillRect(handX - weaponScale * 4, handY - weaponScale * 2, 
                           weaponScale * 9, weaponScale * 3);
                // Handle
                gc.setFill(accentColor);
                gc.fillRect(handX - weaponScale * 0.5, handY, weaponScale * 2, weaponScale * 8);
                break;
                
            case AXE:
                // Handle
                gc.setFill(Color.web("#6b4423"));
                gc.fillRect(handX, handY - weaponScale * 20, weaponScale * 2, weaponScale * 25);
                // Axe head
                gc.setFill(primaryColor);
                gc.fillArc(handX - weaponScale * 6, handY - weaponScale * 22, 
                          weaponScale * 12, weaponScale * 10, 180, 180, javafx.scene.shape.ArcType.ROUND);
                break;
                
            case SPEAR:
                // Shaft
                gc.setFill(Color.web("#6b4423"));
                gc.fillRect(handX, handY - weaponScale * 35, weaponScale * 1.5, weaponScale * 40);
                // Spear head
                gc.setFill(primaryColor);
                gc.fillPolygon(
                    new double[]{handX - weaponScale, handX + weaponScale * 0.75, handX + weaponScale * 2.5},
                    new double[]{handY - weaponScale * 35, handY - weaponScale * 45, handY - weaponScale * 35}, 3);
                break;
                
            case WOODEN_SHIELD:
            case IRON_SHIELD:
            case STEEL_SHIELD:
                // Shield body
                gc.setFill(primaryColor);
                gc.fillOval(handX - weaponScale * 8, handY - weaponScale * 12, 
                           weaponScale * 14, weaponScale * 18);
                // Shield border
                gc.setStroke(secondaryColor);
                gc.setLineWidth(2);
                gc.strokeOval(handX - weaponScale * 8, handY - weaponScale * 12,
                             weaponScale * 14, weaponScale * 18);
                // Emblem
                gc.setFill(accentColor);
                gc.fillOval(handX - weaponScale * 2, handY - weaponScale * 5,
                           weaponScale * 5, weaponScale * 6);
                break;
                
            case TORCH:
                // Handle
                gc.setFill(Color.web("#6b4423"));
                gc.fillRect(handX, handY - weaponScale * 15, weaponScale * 2, weaponScale * 18);
                // Flame
                gc.setFill(Color.web("#ff6600"));
                gc.fillOval(handX - weaponScale * 2, handY - weaponScale * 22, 
                           weaponScale * 6, weaponScale * 10);
                gc.setFill(Color.web("#ffcc00"));
                gc.fillOval(handX - weaponScale, handY - weaponScale * 20,
                           weaponScale * 4, weaponScale * 7);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Renders cape behind the character.
     */
    public void renderCape(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(primaryColor);
        
        // Cape shape (flows behind)
        double capeWidth = width * 1.4;
        double capeHeight = height * 1.1;
        
        gc.fillPolygon(
            new double[]{x - width * 0.2, x + width * 0.5, x + width * 1.2},
            new double[]{y + height * 0.05, y + capeHeight, y + height * 0.05}, 3);
        
        // Cape border
        gc.setStroke(secondaryColor);
        gc.setLineWidth(2);
        gc.strokePolygon(
            new double[]{x - width * 0.2, x + width * 0.5, x + width * 1.2},
            new double[]{y + height * 0.05, y + capeHeight, y + height * 0.05}, 3);
        
        // Cape clasp
        gc.setFill(accentColor);
        gc.fillOval(x + width * 0.4, y - height * 0.05, width * 0.2, height * 0.08);
    }
    
    // === Static Helper Methods ===
    
    private static Slot getSlotForType(Type type) {
        switch (type) {
            case LEATHER_CAP: case IRON_HELM: case STEEL_HELM: case CROWN:
                return Slot.HEAD;
            case RAGGED_SHIRT: case CLOTH_SHIRT:
            case LEATHER_VEST: case CHAINMAIL: case IRON_PLATE: case STEEL_PLATE: case ROYAL_ARMOR:
                return Slot.BODY;
            case RAGGED_PANTS: case CLOTH_PANTS:
            case LEATHER_PANTS: case CHAINMAIL_LEGS: case IRON_GREAVES: case STEEL_GREAVES:
                return Slot.LEGS;
            case SANDALS: case LEATHER_BOOTS: case IRON_BOOTS: case STEEL_BOOTS:
                return Slot.FEET;
            case WOODEN_SWORD: case IRON_SWORD: case STEEL_SWORD: case ROYAL_SWORD: 
            case AXE: case MACE: case SPEAR:
                return Slot.MAIN_HAND;
            case WOODEN_SHIELD: case IRON_SHIELD: case STEEL_SHIELD: case TORCH:
                return Slot.OFF_HAND;
            case CLOTH_CAPE: case WOOL_CAPE: case ROYAL_CAPE: case MERCHANT_CAPE:
                return Slot.CAPE;
            default:
                return Slot.BODY;
        }
    }
    
    private static String getNameForType(Type type) {
        return type.toString().replace("_", " ");
    }
    
    private static int getValueForType(Type type) {
        switch (type) {
            case RAGGED_SHIRT: case RAGGED_PANTS:
                return 1; // Nearly worthless
            case CLOTH_SHIRT: case CLOTH_PANTS:
                return 5; // Basic clothing
            case LEATHER_CAP: case LEATHER_VEST: case LEATHER_PANTS: case SANDALS: 
            case WOODEN_SWORD: case WOODEN_SHIELD: case CLOTH_CAPE:
                return 10;
            case IRON_HELM: case CHAINMAIL: case CHAINMAIL_LEGS: case LEATHER_BOOTS:
            case IRON_SWORD: case IRON_SHIELD: case TORCH: case WOOL_CAPE: case AXE:
                return 50;
            case STEEL_HELM: case IRON_PLATE: case IRON_GREAVES: case IRON_BOOTS:
            case STEEL_SWORD: case STEEL_SHIELD: case SPEAR:
                return 150;
            case STEEL_PLATE: case STEEL_GREAVES: case STEEL_BOOTS: case MERCHANT_CAPE:
                return 300;
            case CROWN: case ROYAL_ARMOR: case ROYAL_SWORD: case ROYAL_CAPE:
                return 1000;
            default:
                return 10;
        }
    }
    
    private static int getDefenseForType(Type type) {
        switch (type) {
            case RAGGED_SHIRT: case RAGGED_PANTS: case CLOTH_SHIRT: case CLOTH_PANTS:
                return 0; // No protection
            case LEATHER_CAP: case LEATHER_VEST: case LEATHER_PANTS: case SANDALS:
                return 1;
            case IRON_HELM: case CHAINMAIL: case CHAINMAIL_LEGS: case LEATHER_BOOTS:
                return 3;
            case STEEL_HELM: case IRON_PLATE: case IRON_GREAVES: case IRON_BOOTS:
                return 5;
            case STEEL_PLATE: case STEEL_GREAVES: case STEEL_BOOTS:
                return 8;
            case ROYAL_ARMOR: case CROWN:
                return 10;
            case WOODEN_SHIELD:
                return 2;
            case IRON_SHIELD:
                return 4;
            case STEEL_SHIELD:
                return 6;
            default:
                return 0;
        }
    }
    
    private static int getAttackForType(Type type) {
        switch (type) {
            case WOODEN_SWORD:
                return 3;
            case IRON_SWORD: case AXE:
                return 6;
            case STEEL_SWORD: case SPEAR:
                return 10;
            case ROYAL_SWORD:
                return 15;
            case MACE:
                return 8;
            default:
                return 0;
        }
    }
    
    private static Color getPrimaryColorForType(Type type) {
        switch (type) {
            // Ragged/cloth items (worn, faded colors)
            case RAGGED_SHIRT: case RAGGED_PANTS:
                return Color.web("#706050"); // Faded brown/grey
            case CLOTH_SHIRT: case CLOTH_PANTS:
                return Color.web("#a09080"); // Light beige
            // Leather items
            case LEATHER_CAP: case LEATHER_VEST: case LEATHER_PANTS: case LEATHER_BOOTS:
                return Color.web("#8b6914");
            // Iron items
            case IRON_HELM: case IRON_PLATE: case IRON_GREAVES: case IRON_BOOTS:
            case IRON_SWORD: case IRON_SHIELD:
                return Color.web("#707080");
            // Steel items
            case STEEL_HELM: case STEEL_PLATE: case STEEL_GREAVES: case STEEL_BOOTS:
            case STEEL_SWORD: case STEEL_SHIELD:
                return Color.web("#a0a0b0");
            // Chainmail
            case CHAINMAIL: case CHAINMAIL_LEGS:
                return Color.web("#808090");
            // Wood items
            case WOODEN_SWORD: case WOODEN_SHIELD:
                return Color.web("#8b6914");
            // Sandals
            case SANDALS:
                return Color.web("#a08050");
            // Royal items
            case CROWN: case ROYAL_ARMOR: case ROYAL_SWORD: case ROYAL_CAPE:
                return Color.web("#4040a0");
            // Capes
            case CLOTH_CAPE:
                return Color.web("#808080");
            case WOOL_CAPE:
                return Color.web("#604020");
            case MERCHANT_CAPE:
                return Color.web("#206040");
            // Other weapons
            case AXE: case MACE: case SPEAR:
                return Color.web("#909090");
            case TORCH:
                return Color.web("#6b4423");
            default:
                return Color.web("#808080");
        }
    }
    
    private static Color getSecondaryColorForType(Type type) {
        switch (type) {
            case RAGGED_SHIRT: case RAGGED_PANTS:
                return Color.web("#504030"); // Dark stains
            case CLOTH_SHIRT: case CLOTH_PANTS:
                return Color.web("#807060"); // Seams
            case LEATHER_CAP: case LEATHER_VEST: case LEATHER_PANTS: case LEATHER_BOOTS:
                return Color.web("#5a4510");
            case IRON_HELM: case IRON_PLATE: case IRON_GREAVES: case IRON_BOOTS:
            case IRON_SWORD: case IRON_SHIELD:
                return Color.web("#505060");
            case STEEL_HELM: case STEEL_PLATE: case STEEL_GREAVES: case STEEL_BOOTS:
            case STEEL_SWORD: case STEEL_SHIELD:
                return Color.web("#c0c0d0");
            case ROYAL_ARMOR: case ROYAL_SWORD: case ROYAL_CAPE:
                return Color.web("#ffd700");
            default:
                return Color.web("#404040");
        }
    }
    
    private static Color getAccentColorForType(Type type) {
        switch (type) {
            case ROYAL_ARMOR: case ROYAL_SWORD: case CROWN: case ROYAL_CAPE:
                return Color.web("#ffd700");
            case IRON_SWORD: case IRON_SHIELD:
                return Color.web("#4a3020");
            case STEEL_SWORD: case STEEL_SHIELD:
                return Color.web("#302010");
            case MERCHANT_CAPE:
                return Color.web("#ffd700");
            default:
                return Color.web("#604020");
        }
    }
    
    // === Getters ===
    
    public Type getType() { return type; }
    public Slot getSlot() { return slot; }
    public String getName() { return name; }
    public int getValue() { return value; }
    public int getDefense() { return defense; }
    public int getAttack() { return attack; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public Color getAccentColor() { return accentColor; }
}
