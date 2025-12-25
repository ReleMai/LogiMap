import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * ItemRenderer - Detailed medieval-style sprite rendering for all game items.
 * 
 * This class provides hand-crafted pixel-art style rendering for:
 * - Equipment (armor, weapons, clothing)
 * - Materials (ores, wood, grain, gems)
 * - Consumables (potions, food)
 * - Tools (pickaxe, axe, hammer)
 * - Misc items
 * 
 * Each render method takes the GraphicsContext, position, size, and relevant item data.
 */
public class ItemRenderer {
    
    // === EQUIPMENT RENDERING ===
    
    /**
     * Renders equipment based on slot and type.
     */
    public static void renderEquipment(GraphicsContext gc, double x, double y, double size, 
                                        Equipment.Slot slot, Equipment.Type type, Color primary, Color secondary) {
        if (slot == null || type == null) {
            renderDefaultItem(gc, x, y, size, primary);
            return;
        }
        
        switch (slot) {
            case HEAD:
                renderHeadgear(gc, x, y, size, type, primary, secondary);
                break;
            case BODY:
                renderBodyArmor(gc, x, y, size, type, primary, secondary);
                break;
            case LEGS:
                renderLegArmor(gc, x, y, size, type, primary, secondary);
                break;
            case FEET:
                renderFootwear(gc, x, y, size, type, primary, secondary);
                break;
            case MAIN_HAND:
                renderWeapon(gc, x, y, size, type, primary, secondary);
                break;
            case OFF_HAND:
                renderOffhand(gc, x, y, size, type, primary, secondary);
                break;
            case CAPE:
                renderCape(gc, x, y, size, type, primary, secondary);
                break;
            default:
                renderDefaultItem(gc, x, y, size, primary);
        }
    }
    
    /**
     * Renders headgear (helmets, caps, crowns).
     */
    public static void renderHeadgear(GraphicsContext gc, double x, double y, double size,
                                       Equipment.Type type, Color primary, Color secondary) {
        double cx = x + size / 2;
        double cy = y + size / 2;
        
        if (type == null) type = Equipment.Type.LEATHER_CAP;
        
        switch (type) {
            case LEATHER_CAP:
                // Leather cap - rounded with stitching
                gc.setFill(primary);
                gc.fillArc(x + size * 0.15, y + size * 0.25, size * 0.7, size * 0.6, 0, 180, javafx.scene.shape.ArcType.ROUND);
                // Brim
                gc.setFill(secondary);
                gc.fillRect(x + size * 0.1, y + size * 0.52, size * 0.8, size * 0.15);
                // Stitching detail
                gc.setStroke(secondary.darker());
                gc.setLineWidth(1);
                gc.strokeLine(cx - size * 0.2, y + size * 0.35, cx + size * 0.2, y + size * 0.35);
                break;
                
            case IRON_HELM:
            case STEEL_HELM:
                // Medieval great helm
                Color metalColor = type == Equipment.Type.STEEL_HELM ? Color.web("#c0c0c0") : Color.web("#808080");
                // Main dome
                gc.setFill(metalColor);
                gc.fillArc(x + size * 0.12, y + size * 0.1, size * 0.76, size * 0.65, 0, 180, javafx.scene.shape.ArcType.ROUND);
                // Face plate
                gc.fillRect(x + size * 0.2, y + size * 0.35, size * 0.6, size * 0.45);
                // Eye slit
                gc.setFill(Color.BLACK);
                gc.fillRect(x + size * 0.25, y + size * 0.45, size * 0.5, size * 0.08);
                // Nose guard
                gc.setFill(metalColor.brighter());
                gc.fillRect(cx - size * 0.04, y + size * 0.4, size * 0.08, size * 0.35);
                // Rivets
                gc.setFill(metalColor.darker());
                gc.fillOval(x + size * 0.25, y + size * 0.55, size * 0.06, size * 0.06);
                gc.fillOval(x + size * 0.69, y + size * 0.55, size * 0.06, size * 0.06);
                break;
                
            case CROWN:
                // Royal crown
                gc.setFill(Color.GOLD);
                // Band
                gc.fillRect(x + size * 0.15, y + size * 0.55, size * 0.7, size * 0.2);
                // Points
                double[] crownX = {x + size * 0.15, x + size * 0.25, x + size * 0.35, cx, x + size * 0.65, x + size * 0.75, x + size * 0.85};
                double[] crownY = {y + size * 0.55, y + size * 0.25, y + size * 0.55, y + size * 0.2, y + size * 0.55, y + size * 0.25, y + size * 0.55};
                gc.fillPolygon(crownX, crownY, 7);
                // Gems
                gc.setFill(Color.RED);
                gc.fillOval(cx - size * 0.05, y + size * 0.3, size * 0.1, size * 0.1);
                gc.setFill(Color.BLUE);
                gc.fillOval(x + size * 0.25, y + size * 0.58, size * 0.08, size * 0.08);
                gc.fillOval(x + size * 0.67, y + size * 0.58, size * 0.08, size * 0.08);
                break;
                
            default:
                // Default cap
                gc.setFill(primary);
                gc.fillArc(x + size * 0.15, y + size * 0.25, size * 0.7, size * 0.6, 0, 180, javafx.scene.shape.ArcType.ROUND);
        }
    }
    
    /**
     * Renders body armor (shirts, vests, plate).
     */
    public static void renderBodyArmor(GraphicsContext gc, double x, double y, double size,
                                        Equipment.Type type, Color primary, Color secondary) {
        double cx = x + size / 2;
        
        if (type == null) type = Equipment.Type.CLOTH_SHIRT;
        
        switch (type) {
            case RAGGED_SHIRT:
                // Torn, patched shirt
                gc.setFill(primary);
                // Main body
                gc.fillRect(x + size * 0.2, y + size * 0.15, size * 0.6, size * 0.7);
                // Sleeves
                gc.fillRect(x + size * 0.05, y + size * 0.18, size * 0.2, size * 0.35);
                gc.fillRect(x + size * 0.75, y + size * 0.18, size * 0.2, size * 0.35);
                // Collar
                gc.setFill(secondary);
                gc.fillArc(x + size * 0.3, y + size * 0.08, size * 0.4, size * 0.2, 0, 180, javafx.scene.shape.ArcType.ROUND);
                // Tears and patches
                gc.setStroke(secondary.darker());
                gc.setLineWidth(1);
                gc.strokeLine(x + size * 0.35, y + size * 0.4, x + size * 0.45, y + size * 0.55);
                gc.strokeLine(x + size * 0.6, y + size * 0.5, x + size * 0.65, y + size * 0.7);
                // Patch
                gc.setFill(secondary.darker());
                gc.fillRect(x + size * 0.5, y + size * 0.6, size * 0.15, size * 0.12);
                break;
                
            case CLOTH_SHIRT:
                // Simple cloth shirt
                gc.setFill(primary);
                gc.fillRect(x + size * 0.2, y + size * 0.12, size * 0.6, size * 0.75);
                // Sleeves
                gc.fillRect(x + size * 0.05, y + size * 0.15, size * 0.2, size * 0.4);
                gc.fillRect(x + size * 0.75, y + size * 0.15, size * 0.2, size * 0.4);
                // Collar
                gc.setFill(secondary);
                gc.fillArc(x + size * 0.32, y + size * 0.05, size * 0.36, size * 0.18, 0, 180, javafx.scene.shape.ArcType.ROUND);
                // Button line
                gc.setStroke(secondary);
                gc.setLineWidth(1.5);
                gc.strokeLine(cx, y + size * 0.2, cx, y + size * 0.7);
                break;
                
            case LEATHER_VEST:
                // Leather vest with straps
                gc.setFill(primary);
                // Main body
                gc.fillRoundRect(x + size * 0.18, y + size * 0.1, size * 0.64, size * 0.8, size * 0.1, size * 0.1);
                // Arm holes
                gc.setFill(Color.web("#1a1208"));
                gc.fillOval(x + size * 0.08, y + size * 0.18, size * 0.18, size * 0.25);
                gc.fillOval(x + size * 0.74, y + size * 0.18, size * 0.18, size * 0.25);
                // Straps
                gc.setFill(secondary);
                gc.fillRect(x + size * 0.25, y + size * 0.25, size * 0.08, size * 0.5);
                gc.fillRect(x + size * 0.67, y + size * 0.25, size * 0.08, size * 0.5);
                // Buckles
                gc.setFill(Color.GOLDENROD);
                gc.fillRect(x + size * 0.26, y + size * 0.4, size * 0.06, size * 0.08);
                gc.fillRect(x + size * 0.68, y + size * 0.4, size * 0.06, size * 0.08);
                break;
                
            case CHAINMAIL:
                // Chainmail hauberk
                gc.setFill(Color.web("#707070"));
                gc.fillRoundRect(x + size * 0.15, y + size * 0.08, size * 0.7, size * 0.85, size * 0.08, size * 0.08);
                // Chain pattern (simplified)
                gc.setStroke(Color.web("#909090"));
                gc.setLineWidth(0.5);
                for (double py = y + size * 0.15; py < y + size * 0.85; py += size * 0.08) {
                    for (double px = x + size * 0.2; px < x + size * 0.8; px += size * 0.08) {
                        gc.strokeOval(px, py, size * 0.06, size * 0.06);
                    }
                }
                // Collar
                gc.setFill(Color.web("#606060"));
                gc.fillRect(x + size * 0.3, y + size * 0.05, size * 0.4, size * 0.12);
                break;
                
            case IRON_PLATE:
            case STEEL_PLATE:
                // Plate armor
                Color plateColor = type == Equipment.Type.STEEL_PLATE ? Color.web("#c8c8c8") : Color.web("#909090");
                gc.setFill(plateColor);
                // Chest plate
                gc.fillRoundRect(x + size * 0.18, y + size * 0.15, size * 0.64, size * 0.7, size * 0.1, size * 0.1);
                // Shoulder pauldrons
                gc.fillOval(x + size * 0.05, y + size * 0.12, size * 0.25, size * 0.2);
                gc.fillOval(x + size * 0.7, y + size * 0.12, size * 0.25, size * 0.2);
                // Center ridge
                gc.setFill(plateColor.brighter());
                gc.fillRect(cx - size * 0.03, y + size * 0.2, size * 0.06, size * 0.5);
                // Rivets
                gc.setFill(plateColor.darker());
                gc.fillOval(x + size * 0.25, y + size * 0.25, size * 0.05, size * 0.05);
                gc.fillOval(x + size * 0.7, y + size * 0.25, size * 0.05, size * 0.05);
                gc.fillOval(x + size * 0.25, y + size * 0.65, size * 0.05, size * 0.05);
                gc.fillOval(x + size * 0.7, y + size * 0.65, size * 0.05, size * 0.05);
                break;
                
            default:
                // Default shirt
                gc.setFill(primary);
                gc.fillRect(x + size * 0.2, y + size * 0.12, size * 0.6, size * 0.75);
                gc.fillRect(x + size * 0.05, y + size * 0.15, size * 0.2, size * 0.4);
                gc.fillRect(x + size * 0.75, y + size * 0.15, size * 0.2, size * 0.4);
        }
    }
    
    /**
     * Renders leg armor (pants, greaves).
     */
    public static void renderLegArmor(GraphicsContext gc, double x, double y, double size,
                                       Equipment.Type type, Color primary, Color secondary) {
        double cx = x + size / 2;
        
        if (type == null) type = Equipment.Type.CLOTH_PANTS;
        
        switch (type) {
            case RAGGED_PANTS:
                // Torn pants
                gc.setFill(primary);
                // Waist
                gc.fillRect(x + size * 0.2, y + size * 0.05, size * 0.6, size * 0.2);
                // Left leg
                gc.fillRect(x + size * 0.2, y + size * 0.2, size * 0.25, size * 0.7);
                // Right leg
                gc.fillRect(x + size * 0.55, y + size * 0.2, size * 0.25, size * 0.7);
                // Tears
                gc.setFill(Color.web("#1a1208"));
                gc.fillOval(x + size * 0.25, y + size * 0.5, size * 0.1, size * 0.15);
                gc.fillRect(x + size * 0.6, y + size * 0.75, size * 0.12, size * 0.15);
                // Belt
                gc.setFill(secondary);
                gc.fillRect(x + size * 0.18, y + size * 0.08, size * 0.64, size * 0.08);
                break;
                
            case CLOTH_PANTS:
            case LEATHER_PANTS:
                // Standard pants
                gc.setFill(primary);
                gc.fillRect(x + size * 0.2, y + size * 0.05, size * 0.6, size * 0.22);
                gc.fillRect(x + size * 0.2, y + size * 0.22, size * 0.25, size * 0.7);
                gc.fillRect(x + size * 0.55, y + size * 0.22, size * 0.25, size * 0.7);
                // Belt
                gc.setFill(secondary);
                gc.fillRect(x + size * 0.18, y + size * 0.08, size * 0.64, size * 0.1);
                // Belt buckle
                gc.setFill(Color.GOLDENROD);
                gc.fillRect(cx - size * 0.05, y + size * 0.09, size * 0.1, size * 0.08);
                // Seam
                gc.setStroke(secondary.darker());
                gc.setLineWidth(1);
                gc.strokeLine(x + size * 0.32, y + size * 0.25, x + size * 0.32, y + size * 0.85);
                gc.strokeLine(x + size * 0.68, y + size * 0.25, x + size * 0.68, y + size * 0.85);
                break;
                
            case IRON_GREAVES:
            case STEEL_GREAVES:
                // Metal leg armor
                Color greavesColor = type == Equipment.Type.STEEL_GREAVES ? Color.web("#c0c0c0") : Color.web("#808080");
                gc.setFill(greavesColor);
                // Tassets (upper)
                gc.fillRoundRect(x + size * 0.15, y + size * 0.05, size * 0.7, size * 0.25, size * 0.05, size * 0.05);
                // Greaves (lower)
                gc.fillRoundRect(x + size * 0.18, y + size * 0.28, size * 0.26, size * 0.65, size * 0.05, size * 0.05);
                gc.fillRoundRect(x + size * 0.56, y + size * 0.28, size * 0.26, size * 0.65, size * 0.05, size * 0.05);
                // Knee guards
                gc.setFill(greavesColor.brighter());
                gc.fillOval(x + size * 0.22, y + size * 0.32, size * 0.18, size * 0.15);
                gc.fillOval(x + size * 0.6, y + size * 0.32, size * 0.18, size * 0.15);
                break;
                
            default:
                gc.setFill(primary);
                gc.fillRect(x + size * 0.2, y + size * 0.05, size * 0.6, size * 0.22);
                gc.fillRect(x + size * 0.2, y + size * 0.22, size * 0.25, size * 0.7);
                gc.fillRect(x + size * 0.55, y + size * 0.22, size * 0.25, size * 0.7);
        }
    }
    
    /**
     * Renders footwear (sandals, boots).
     */
    public static void renderFootwear(GraphicsContext gc, double x, double y, double size,
                                       Equipment.Type type, Color primary, Color secondary) {
        if (type == null) type = Equipment.Type.LEATHER_BOOTS;
        
        switch (type) {
            case SANDALS:
                // Simple sandals
                gc.setFill(primary);
                // Soles
                gc.fillRoundRect(x + size * 0.08, y + size * 0.5, size * 0.35, size * 0.45, size * 0.1, size * 0.2);
                gc.fillRoundRect(x + size * 0.57, y + size * 0.5, size * 0.35, size * 0.45, size * 0.1, size * 0.2);
                // Straps
                gc.setFill(secondary);
                gc.fillRect(x + size * 0.12, y + size * 0.55, size * 0.27, size * 0.06);
                gc.fillRect(x + size * 0.61, y + size * 0.55, size * 0.27, size * 0.06);
                gc.fillRect(x + size * 0.12, y + size * 0.7, size * 0.27, size * 0.06);
                gc.fillRect(x + size * 0.61, y + size * 0.7, size * 0.27, size * 0.06);
                break;
                
            case LEATHER_BOOTS:
                // Leather boots
                gc.setFill(primary);
                // Boot bodies
                gc.fillRoundRect(x + size * 0.08, y + size * 0.15, size * 0.35, size * 0.75, size * 0.08, size * 0.15);
                gc.fillRoundRect(x + size * 0.57, y + size * 0.15, size * 0.35, size * 0.75, size * 0.08, size * 0.15);
                // Soles
                gc.setFill(secondary.darker());
                gc.fillRect(x + size * 0.06, y + size * 0.82, size * 0.39, size * 0.12);
                gc.fillRect(x + size * 0.55, y + size * 0.82, size * 0.39, size * 0.12);
                // Laces
                gc.setStroke(secondary);
                gc.setLineWidth(1);
                for (double ly = y + size * 0.3; ly < y + size * 0.7; ly += size * 0.12) {
                    gc.strokeLine(x + size * 0.18, ly, x + size * 0.32, ly);
                    gc.strokeLine(x + size * 0.68, ly, x + size * 0.82, ly);
                }
                break;
                
            case IRON_BOOTS:
            case STEEL_BOOTS:
                // Metal boots (sabatons)
                Color bootColor = type == Equipment.Type.STEEL_BOOTS ? Color.web("#b0b0b0") : Color.web("#707070");
                gc.setFill(bootColor);
                // Main boot
                gc.fillRoundRect(x + size * 0.08, y + size * 0.2, size * 0.35, size * 0.7, size * 0.05, size * 0.1);
                gc.fillRoundRect(x + size * 0.57, y + size * 0.2, size * 0.35, size * 0.7, size * 0.05, size * 0.1);
                // Toe guards
                gc.setFill(bootColor.brighter());
                gc.fillArc(x + size * 0.08, y + size * 0.75, size * 0.35, size * 0.2, 180, 180, javafx.scene.shape.ArcType.ROUND);
                gc.fillArc(x + size * 0.57, y + size * 0.75, size * 0.35, size * 0.2, 180, 180, javafx.scene.shape.ArcType.ROUND);
                // Segment lines
                gc.setStroke(bootColor.darker());
                gc.setLineWidth(1);
                gc.strokeLine(x + size * 0.1, y + size * 0.4, x + size * 0.4, y + size * 0.4);
                gc.strokeLine(x + size * 0.6, y + size * 0.4, x + size * 0.9, y + size * 0.4);
                gc.strokeLine(x + size * 0.1, y + size * 0.55, x + size * 0.4, y + size * 0.55);
                gc.strokeLine(x + size * 0.6, y + size * 0.55, x + size * 0.9, y + size * 0.55);
                break;
                
            default:
                gc.setFill(primary);
                gc.fillRoundRect(x + size * 0.08, y + size * 0.2, size * 0.35, size * 0.7, size * 0.08, size * 0.15);
                gc.fillRoundRect(x + size * 0.57, y + size * 0.2, size * 0.35, size * 0.7, size * 0.08, size * 0.15);
        }
    }
    
    /**
     * Renders weapons (swords, axes, etc).
     */
    public static void renderWeapon(GraphicsContext gc, double x, double y, double size,
                                     Equipment.Type type, Color primary, Color secondary) {
        double cx = x + size / 2;
        
        if (type == null) type = Equipment.Type.IRON_SWORD;
        
        switch (type) {
            case WOODEN_SWORD:
            case IRON_SWORD:
            case STEEL_SWORD:
                // Sword
                Color bladeColor = type == Equipment.Type.WOODEN_SWORD ? Color.web("#8b7355") :
                                   type == Equipment.Type.STEEL_SWORD ? Color.web("#d0d0d0") : Color.web("#a0a0a0");
                // Blade
                gc.setFill(bladeColor);
                gc.fillPolygon(
                    new double[]{cx, cx + size * 0.12, cx + size * 0.08, cx - size * 0.08, cx - size * 0.12},
                    new double[]{y + size * 0.05, y + size * 0.5, y + size * 0.65, y + size * 0.65, y + size * 0.5},
                    5
                );
                // Edge highlight
                gc.setFill(bladeColor.brighter());
                gc.fillRect(cx - size * 0.02, y + size * 0.1, size * 0.04, size * 0.5);
                // Guard
                gc.setFill(secondary);
                gc.fillRect(x + size * 0.2, y + size * 0.62, size * 0.6, size * 0.08);
                // Handle
                gc.setFill(Color.web("#4a3020"));
                gc.fillRect(cx - size * 0.06, y + size * 0.68, size * 0.12, size * 0.22);
                // Pommel
                gc.setFill(secondary);
                gc.fillOval(cx - size * 0.07, y + size * 0.88, size * 0.14, size * 0.1);
                break;
                
            case AXE:
                // Battle axe
                gc.setFill(Color.web("#8b7355"));
                // Handle
                gc.fillRect(cx - size * 0.04, y + size * 0.15, size * 0.08, size * 0.75);
                // Axe head
                gc.setFill(Color.web("#909090"));
                gc.fillPolygon(
                    new double[]{cx + size * 0.02, cx + size * 0.35, cx + size * 0.35, cx + size * 0.02},
                    new double[]{y + size * 0.1, y + size * 0.05, y + size * 0.4, y + size * 0.45},
                    4
                );
                // Edge
                gc.setFill(Color.web("#c0c0c0"));
                gc.fillArc(cx + size * 0.25, y + size * 0.05, size * 0.2, size * 0.35, -90, 180, javafx.scene.shape.ArcType.ROUND);
                break;
                
            case MACE:
                // Mace
                gc.setFill(Color.web("#5a4030"));
                // Handle
                gc.fillRect(cx - size * 0.05, y + size * 0.4, size * 0.1, size * 0.5);
                // Head
                gc.setFill(Color.web("#808080"));
                gc.fillOval(cx - size * 0.18, y + size * 0.08, size * 0.36, size * 0.36);
                // Spikes
                gc.setFill(Color.web("#a0a0a0"));
                double[] spikeAngles = {0, 45, 90, 135, 180, 225, 270, 315};
                for (double angle : spikeAngles) {
                    double rad = Math.toRadians(angle);
                    double sx = cx + Math.cos(rad) * size * 0.18;
                    double sy = y + size * 0.26 + Math.sin(rad) * size * 0.18;
                    gc.fillOval(sx - size * 0.04, sy - size * 0.04, size * 0.08, size * 0.08);
                }
                break;
                
            case SPEAR:
                // Spear
                gc.setFill(Color.web("#6b5344"));
                // Shaft
                gc.fillRect(cx - size * 0.03, y + size * 0.25, size * 0.06, size * 0.7);
                // Spearhead
                gc.setFill(Color.web("#a0a0a0"));
                gc.fillPolygon(
                    new double[]{cx, cx + size * 0.1, cx - size * 0.1},
                    new double[]{y + size * 0.02, y + size * 0.28, y + size * 0.28},
                    3
                );
                break;
                
            default:
                // Default sword shape
                gc.setFill(primary);
                gc.fillRect(cx - size * 0.06, y + size * 0.1, size * 0.12, size * 0.55);
                gc.setFill(secondary);
                gc.fillRect(x + size * 0.25, y + size * 0.62, size * 0.5, size * 0.08);
                gc.setFill(Color.web("#4a3020"));
                gc.fillRect(cx - size * 0.05, y + size * 0.68, size * 0.1, size * 0.25);
        }
    }
    
    /**
     * Renders off-hand items (shields, torches).
     */
    public static void renderOffhand(GraphicsContext gc, double x, double y, double size,
                                      Equipment.Type type, Color primary, Color secondary) {
        double cx = x + size / 2;
        double cy = y + size / 2;
        
        if (type == null) type = Equipment.Type.WOODEN_SHIELD;
        
        switch (type) {
            case WOODEN_SHIELD:
            case IRON_SHIELD:
            case STEEL_SHIELD:
                // Shield
                Color shieldColor = type == Equipment.Type.WOODEN_SHIELD ? Color.web("#8b7355") :
                                    type == Equipment.Type.STEEL_SHIELD ? Color.web("#c0c0c0") : Color.web("#909090");
                // Shield body
                gc.setFill(shieldColor);
                gc.fillRoundRect(x + size * 0.15, y + size * 0.08, size * 0.7, size * 0.84, size * 0.15, size * 0.15);
                // Boss (center bump)
                gc.setFill(shieldColor.brighter());
                gc.fillOval(cx - size * 0.12, cy - size * 0.12, size * 0.24, size * 0.24);
                // Edge trim
                gc.setStroke(type == Equipment.Type.WOODEN_SHIELD ? Color.web("#5a4030") : shieldColor.darker());
                gc.setLineWidth(2);
                gc.strokeRoundRect(x + size * 0.15, y + size * 0.08, size * 0.7, size * 0.84, size * 0.15, size * 0.15);
                // Emblem (cross for iron/steel)
                if (type != Equipment.Type.WOODEN_SHIELD) {
                    gc.setFill(Color.web("#800000"));
                    gc.fillRect(cx - size * 0.04, y + size * 0.2, size * 0.08, size * 0.55);
                    gc.fillRect(x + size * 0.28, cy - size * 0.04, size * 0.44, size * 0.08);
                }
                break;
                
            case TORCH:
                // Torch
                gc.setFill(Color.web("#5a4030"));
                // Handle
                gc.fillRect(cx - size * 0.06, y + size * 0.35, size * 0.12, size * 0.55);
                // Wrapping
                gc.setFill(Color.web("#4a4040"));
                gc.fillRect(cx - size * 0.08, y + size * 0.25, size * 0.16, size * 0.15);
                // Flame
                gc.setFill(Color.ORANGE);
                gc.fillOval(cx - size * 0.12, y + size * 0.05, size * 0.24, size * 0.25);
                gc.setFill(Color.YELLOW);
                gc.fillOval(cx - size * 0.08, y + size * 0.1, size * 0.16, size * 0.15);
                gc.setFill(Color.WHITE);
                gc.fillOval(cx - size * 0.04, y + size * 0.13, size * 0.08, size * 0.08);
                break;
                
            default:
                gc.setFill(primary);
                gc.fillRoundRect(x + size * 0.15, y + size * 0.1, size * 0.7, size * 0.8, size * 0.1, size * 0.1);
        }
    }
    
    /**
     * Renders capes.
     */
    public static void renderCape(GraphicsContext gc, double x, double y, double size,
                                   Equipment.Type type, Color primary, Color secondary) {
        double cx = x + size / 2;
        
        if (type == null) type = Equipment.Type.CLOTH_CAPE;
        
        // Cape body
        gc.setFill(primary);
        gc.fillPolygon(
            new double[]{x + size * 0.2, x + size * 0.8, x + size * 0.75, cx, x + size * 0.25},
            new double[]{y + size * 0.1, y + size * 0.1, y + size * 0.9, y + size * 0.95, y + size * 0.9},
            5
        );
        
        // Collar/clasp
        gc.setFill(secondary);
        gc.fillRect(x + size * 0.18, y + size * 0.08, size * 0.64, size * 0.1);
        
        // Clasp detail
        gc.setFill(Color.GOLDENROD);
        gc.fillOval(cx - size * 0.06, y + size * 0.09, size * 0.12, size * 0.08);
        
        // Inner shadow/fold
        gc.setFill(primary.darker());
        gc.fillPolygon(
            new double[]{cx - size * 0.15, cx + size * 0.15, cx},
            new double[]{y + size * 0.2, y + size * 0.2, y + size * 0.85},
            3
        );
    }
    
    // === MATERIAL RENDERING ===
    
    /**
     * Renders grain (wheat, oat, barley, rye).
     */
    public static void renderGrain(GraphicsContext gc, double x, double y, double size, 
                                    Color primary, Color secondary, String grainType) {
        double cx = x + size / 2;
        
        // Draw 3 stalks
        for (int i = 0; i < 3; i++) {
            double stalkX = cx + (i - 1) * size * 0.22;
            double stalkBaseY = y + size * 0.92;
            double stalkTopY = y + size * 0.18;
            
            // Stalk
            gc.setStroke(secondary.darker());
            gc.setLineWidth(Math.max(1.5, size * 0.05));
            gc.strokeLine(stalkX, stalkBaseY, stalkX, stalkTopY + size * 0.2);
            
            // Grain head
            gc.setFill(primary);
            double headY = stalkTopY;
            double kernelW = size * 0.1;
            double kernelH = size * 0.07;
            
            // Kernel pairs
            for (int k = 0; k < 4; k++) {
                double ky = headY + k * kernelH * 1.4;
                gc.fillOval(stalkX - kernelW * 1.1, ky, kernelW, kernelH);
                gc.fillOval(stalkX + kernelW * 0.1, ky, kernelW, kernelH);
            }
            
            // Top kernel
            gc.fillOval(stalkX - kernelW * 0.5, headY - kernelH * 0.2, kernelW, kernelH);
            
            // Awns
            gc.setStroke(primary.brighter());
            gc.setLineWidth(0.8);
            gc.strokeLine(stalkX - kernelW * 0.3, headY - kernelH * 0.2, stalkX - kernelW * 0.8, headY - size * 0.08);
            gc.strokeLine(stalkX + kernelW * 0.3, headY - kernelH * 0.2, stalkX + kernelW * 0.8, headY - size * 0.08);
        }
    }
    
    /**
     * Renders ore/stone materials.
     */
    public static void renderOre(GraphicsContext gc, double x, double y, double size, 
                                  Color primary, Color secondary) {
        // Rock shape
        gc.setFill(Color.web("#505050"));
        gc.fillPolygon(
            new double[]{x + size * 0.1, x + size * 0.4, x + size * 0.7, x + size * 0.9, x + size * 0.75, x + size * 0.3},
            new double[]{y + size * 0.5, y + size * 0.1, y + size * 0.15, y + size * 0.5, y + size * 0.9, y + size * 0.85},
            6
        );
        
        // Ore veins
        gc.setFill(primary);
        gc.fillOval(x + size * 0.25, y + size * 0.35, size * 0.18, size * 0.15);
        gc.fillOval(x + size * 0.55, y + size * 0.25, size * 0.2, size * 0.18);
        gc.fillOval(x + size * 0.35, y + size * 0.55, size * 0.22, size * 0.18);
        
        // Sparkle
        gc.setFill(primary.brighter());
        gc.fillOval(x + size * 0.6, y + size * 0.3, size * 0.06, size * 0.06);
    }
    
    /**
     * Renders wood/lumber.
     */
    public static void renderWood(GraphicsContext gc, double x, double y, double size, 
                                   Color primary, Color secondary) {
        // Log shape
        gc.setFill(primary);
        gc.fillRoundRect(x + size * 0.1, y + size * 0.25, size * 0.8, size * 0.5, size * 0.1, size * 0.1);
        
        // End grain (circle)
        gc.setFill(secondary);
        gc.fillOval(x + size * 0.7, y + size * 0.28, size * 0.22, size * 0.44);
        
        // Rings
        gc.setStroke(primary.darker());
        gc.setLineWidth(0.8);
        gc.strokeOval(x + size * 0.74, y + size * 0.38, size * 0.12, size * 0.24);
        gc.strokeOval(x + size * 0.77, y + size * 0.44, size * 0.06, size * 0.12);
        
        // Bark lines
        gc.strokeLine(x + size * 0.15, y + size * 0.32, x + size * 0.65, y + size * 0.32);
        gc.strokeLine(x + size * 0.2, y + size * 0.5, x + size * 0.6, y + size * 0.5);
        gc.strokeLine(x + size * 0.15, y + size * 0.68, x + size * 0.65, y + size * 0.68);
    }
    
    /**
     * Renders gems (ruby, emerald, sapphire).
     */
    public static void renderGem(GraphicsContext gc, double x, double y, double size, 
                                  Color primary, Color secondary) {
        double cx = x + size / 2;
        double cy = y + size / 2;
        
        // Gem cut shape
        gc.setFill(primary);
        gc.fillPolygon(
            new double[]{cx, x + size * 0.85, x + size * 0.7, x + size * 0.3, x + size * 0.15},
            new double[]{y + size * 0.1, y + size * 0.4, y + size * 0.9, y + size * 0.9, y + size * 0.4},
            5
        );
        
        // Facets
        gc.setFill(primary.brighter());
        gc.fillPolygon(
            new double[]{cx, x + size * 0.65, cx, x + size * 0.35},
            new double[]{y + size * 0.1, y + size * 0.35, y + size * 0.5, y + size * 0.35},
            4
        );
        
        // Inner glow
        gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.3));
        gc.fillOval(cx - size * 0.08, cy - size * 0.15, size * 0.12, size * 0.08);
        
        // Outline
        gc.setStroke(primary.darker());
        gc.setLineWidth(1);
        gc.strokePolygon(
            new double[]{cx, x + size * 0.85, x + size * 0.7, x + size * 0.3, x + size * 0.15},
            new double[]{y + size * 0.1, y + size * 0.4, y + size * 0.9, y + size * 0.9, y + size * 0.4},
            5
        );
    }
    
    // === CONSUMABLE RENDERING ===
    
    /**
     * Renders potions.
     */
    public static void renderPotion(GraphicsContext gc, double x, double y, double size, 
                                     Color primary, Color secondary) {
        double cx = x + size / 2;
        
        // Bottle body
        gc.setFill(Color.web("#404060").deriveColor(0, 1, 1, 0.7));
        gc.fillOval(x + size * 0.2, y + size * 0.35, size * 0.6, size * 0.55);
        
        // Neck
        gc.fillRect(cx - size * 0.1, y + size * 0.15, size * 0.2, size * 0.25);
        
        // Cork
        gc.setFill(Color.web("#8b7355"));
        gc.fillRect(cx - size * 0.08, y + size * 0.08, size * 0.16, size * 0.12);
        
        // Liquid
        gc.setFill(primary.deriveColor(0, 1, 1, 0.8));
        gc.fillOval(x + size * 0.25, y + size * 0.45, size * 0.5, size * 0.4);
        
        // Highlight
        gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.4));
        gc.fillOval(x + size * 0.3, y + size * 0.5, size * 0.12, size * 0.15);
        
        // Bubbles
        gc.setFill(primary.brighter().deriveColor(0, 1, 1, 0.6));
        gc.fillOval(x + size * 0.55, y + size * 0.55, size * 0.08, size * 0.08);
        gc.fillOval(x + size * 0.45, y + size * 0.65, size * 0.06, size * 0.06);
    }
    
    /**
     * Renders food items (bread, meat, etc).
     */
    public static void renderFood(GraphicsContext gc, double x, double y, double size, 
                                   Color primary, Color secondary, String foodType) {
        double cx = x + size / 2;
        
        if ("bread".equals(foodType)) {
            // Bread loaf
            gc.setFill(primary);
            gc.fillOval(x + size * 0.1, y + size * 0.3, size * 0.8, size * 0.55);
            // Crust top
            gc.setFill(primary.darker());
            gc.fillArc(x + size * 0.1, y + size * 0.25, size * 0.8, size * 0.45, 0, 180, javafx.scene.shape.ArcType.CHORD);
            // Score marks
            gc.setStroke(primary.darker().darker());
            gc.setLineWidth(1.5);
            gc.strokeLine(x + size * 0.3, y + size * 0.35, x + size * 0.4, y + size * 0.55);
            gc.strokeLine(x + size * 0.5, y + size * 0.32, x + size * 0.5, y + size * 0.55);
            gc.strokeLine(x + size * 0.6, y + size * 0.35, x + size * 0.7, y + size * 0.55);
        } else {
            // Default: meat/drumstick
            gc.setFill(primary);
            gc.fillOval(x + size * 0.15, y + size * 0.2, size * 0.5, size * 0.45);
            // Bone
            gc.setFill(Color.web("#f0e8d8"));
            gc.fillRect(x + size * 0.55, y + size * 0.35, size * 0.35, size * 0.12);
            gc.fillOval(x + size * 0.8, y + size * 0.32, size * 0.12, size * 0.18);
        }
    }
    
    // === TOOL RENDERING ===
    
    /**
     * Renders tools (pickaxe, axe, hammer).
     */
    public static void renderTool(GraphicsContext gc, double x, double y, double size, 
                                   Color primary, Color secondary, String toolType) {
        double cx = x + size / 2;
        
        if ("pickaxe".equals(toolType)) {
            // Handle
            gc.setFill(Color.web("#6b5344"));
            gc.fillRect(cx - size * 0.04, y + size * 0.4, size * 0.08, size * 0.55);
            // Head
            gc.setFill(primary);
            gc.fillRect(x + size * 0.1, y + size * 0.25, size * 0.8, size * 0.18);
            // Picks
            gc.fillPolygon(
                new double[]{x + size * 0.1, x + size * 0.02, x + size * 0.18},
                new double[]{y + size * 0.34, y + size * 0.2, y + size * 0.25},
                3
            );
            gc.fillPolygon(
                new double[]{x + size * 0.9, x + size * 0.98, x + size * 0.82},
                new double[]{y + size * 0.34, y + size * 0.2, y + size * 0.25},
                3
            );
        } else if ("axe".equals(toolType)) {
            // Handle
            gc.setFill(Color.web("#6b5344"));
            gc.fillRect(cx - size * 0.04, y + size * 0.35, size * 0.08, size * 0.6);
            // Head
            gc.setFill(primary);
            gc.fillPolygon(
                new double[]{cx + size * 0.02, cx + size * 0.35, cx + size * 0.35, cx + size * 0.02},
                new double[]{y + size * 0.1, y + size * 0.05, y + size * 0.45, y + size * 0.5},
                4
            );
            // Edge
            gc.setFill(primary.brighter());
            gc.fillArc(cx + size * 0.25, y + size * 0.05, size * 0.18, size * 0.4, -90, 180, javafx.scene.shape.ArcType.ROUND);
        } else {
            // Default: hammer
            gc.setFill(Color.web("#6b5344"));
            gc.fillRect(cx - size * 0.04, y + size * 0.4, size * 0.08, size * 0.55);
            gc.setFill(primary);
            gc.fillRoundRect(x + size * 0.15, y + size * 0.12, size * 0.7, size * 0.32, size * 0.08, size * 0.08);
        }
    }
    
    // === DEFAULT/FALLBACK ===
    
    /**
     * Default item rendering.
     */
    public static void renderDefaultItem(GraphicsContext gc, double x, double y, double size, Color primary) {
        gc.setFill(primary);
        gc.fillRoundRect(x + size * 0.15, y + size * 0.15, size * 0.7, size * 0.7, size * 0.15, size * 0.15);
        gc.setStroke(primary.darker());
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x + size * 0.15, y + size * 0.15, size * 0.7, size * 0.7, size * 0.15, size * 0.15);
    }
}
