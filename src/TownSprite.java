import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

/**
 * TownSprite - Renders different settlement types with unique visual styles.
 * 
 * Settlement Types:
 * 1. Village - Small cluster of houses
 * 2. Town - Medium settlement with market square
 * 3. City - Large walled city with towers
 * 4. Capital - Grand city with castle
 * 5. Port Town - Coastal settlement with docks
 */
public class TownSprite {
    
    // Settlement type enum
    public enum SettlementType {
        VILLAGE,
        TOWN,
        CITY,
        CAPITAL,
        PORT_TOWN
    }
    
    // Colors
    private static final Color WALL_COLOR = Color.web("#5a5a5a");
    private static final Color WALL_TOP = Color.web("#7a7a7a");
    private static final Color BUILDING_1 = Color.web("#d4a574");  // Tan
    private static final Color BUILDING_2 = Color.web("#c9956d");  // Brown
    private static final Color BUILDING_3 = Color.web("#b8a090");  // Gray-tan
    private static final Color ROOF_RED = Color.web("#8b4513");
    private static final Color ROOF_BLUE = Color.web("#4a6580");
    private static final Color ROOF_BROWN = Color.web("#654321");
    private static final Color TOWER_GOLD = Color.web("#ffd700");
    private static final Color WINDOW_COLOR = Color.web("#fff8dc");
    private static final Color DOOR_COLOR = Color.web("#4a3520");
    private static final Color DOCK_COLOR = Color.web("#8b7355");
    private static final Color WATER_COLOR = Color.web("#4a90c0");
    private static final Color FLAG_RED = Color.web("#cc3333");
    private static final Color FLAG_BLUE = Color.web("#3366cc");
    
    /**
     * Determines settlement type based on Town properties.
     */
    public static SettlementType getSettlementType(Town town, boolean nearWater) {
        if (town.isMajor()) {
            // Major towns are cities or capitals
            if (town.getName().toLowerCase().contains("capital") || 
                town.getName().toLowerCase().contains("metropolis")) {
                return SettlementType.CAPITAL;
            }
            if (nearWater && (town.getName().toLowerCase().contains("port") || 
                             town.getName().toLowerCase().contains("haven") ||
                             town.getName().toLowerCase().contains("coastal"))) {
                return SettlementType.PORT_TOWN;
            }
            return SettlementType.CITY;
        } else {
            // Minor towns are villages or towns
            if (nearWater && town.getName().toLowerCase().contains("port")) {
                return SettlementType.PORT_TOWN;
            }
            if (town.getPopulation() < 5000) {
                return SettlementType.VILLAGE;
            }
            return SettlementType.TOWN;
        }
    }
    
    /**
     * Renders a settlement sprite at the given position.
     */
    public static void render(GraphicsContext gc, Town town, double x, double y, double size, boolean nearWater) {
        SettlementType type = getSettlementType(town, nearWater);
        VillageType villageType = town.getVillageType();
        int variant = town.getVisualVariant();
        
        switch (type) {
            case VILLAGE:
                renderVillageWithVariant(gc, x, y, size, villageType, variant);
                break;
            case TOWN:
                renderTownWithVariant(gc, x, y, size, villageType, variant);
                break;
            case CITY:
                renderCityWithVariant(gc, x, y, size, variant);
                break;
            case CAPITAL:
                renderCapital(gc, x, y, size);
                break;
            case PORT_TOWN:
                renderPortTown(gc, x, y, size);
                break;
        }
    }
    
    /**
     * Village with visual variant (4 different layouts).
     */
    private static void renderVillageWithVariant(GraphicsContext gc, double x, double y, double size, VillageType villageType, int variant) {
        // Get colors from village type
        Color roofColor = villageType != null ? Color.web(villageType.getRoofColor()) : ROOF_BROWN;
        Color accentColor = villageType != null ? Color.web(villageType.getAccentColor()) : Color.web("#2d5016");
        
        double houseSize = size * 0.22;
        double roofHeight = houseSize * 0.5;
        
        // Background area with accent color tint
        gc.setFill(accentColor.deriveColor(0, 1, 1, 0.3));
        gc.fillOval(x + size * 0.1, y + size * 0.1, size * 0.8, size * 0.8);
        
        // Different house layouts based on variant
        double[][] housePositions;
        switch (variant) {
            case 0: // Clustered center
                housePositions = new double[][] {
                    {0.30, 0.25}, {0.50, 0.22}, {0.25, 0.50}, {0.48, 0.48}
                };
                break;
            case 1: // Diamond formation
                housePositions = new double[][] {
                    {0.40, 0.15}, {0.20, 0.40}, {0.55, 0.40}, {0.38, 0.55}
                };
                break;
            case 2: // L-shape
                housePositions = new double[][] {
                    {0.20, 0.20}, {0.45, 0.20}, {0.20, 0.45}, {0.20, 0.65}
                };
                break;
            default: // Square formation
                housePositions = new double[][] {
                    {0.22, 0.22}, {0.52, 0.22}, {0.22, 0.52}, {0.52, 0.52}
                };
                break;
        }
        
        Color[] houseColors = {BUILDING_1, BUILDING_2, BUILDING_3, BUILDING_1};
        
        for (int i = 0; i < 4; i++) {
            double hx = x + housePositions[i][0] * size;
            double hy = y + housePositions[i][1] * size;
            
            // House body
            gc.setFill(houseColors[(i + variant) % 4]);
            gc.fillRect(hx, hy, houseSize, houseSize);
            
            // Roof with village type color
            gc.setFill(roofColor);
            gc.fillPolygon(
                new double[]{hx - houseSize * 0.1, hx + houseSize / 2, hx + houseSize * 1.1},
                new double[]{hy, hy - roofHeight, hy},
                3
            );
            
            // Door
            gc.setFill(DOOR_COLOR);
            gc.fillRect(hx + houseSize * 0.35, hy + houseSize * 0.5, houseSize * 0.3, houseSize * 0.5);
            
            // Window (variant determines which houses have windows)
            if ((i + variant) % 3 != 0) {
                gc.setFill(WINDOW_COLOR);
                gc.fillRect(hx + houseSize * 0.15, hy + houseSize * 0.2, houseSize * 0.2, houseSize * 0.2);
            }
        }
        
        // Optional: Add a well or tree in center for some variants
        if (variant == 1) {
            // Draw a well
            double wellX = x + size * 0.38;
            double wellY = y + size * 0.38;
            gc.setFill(Color.web("#696969"));
            gc.fillOval(wellX, wellY, size * 0.14, size * 0.14);
            gc.setFill(Color.web("#4a90c0"));
            gc.fillOval(wellX + size * 0.02, wellY + size * 0.02, size * 0.10, size * 0.10);
        } else if (variant == 3) {
            // Draw a tree
            double treeX = x + size * 0.35;
            double treeY = y + size * 0.35;
            gc.setFill(Color.web("#8b4513"));
            gc.fillRect(treeX + size * 0.06, treeY + size * 0.10, size * 0.04, size * 0.10);
            gc.setFill(Color.web("#228b22"));
            gc.fillOval(treeX, treeY, size * 0.16, size * 0.14);
        }
        
        // Themed accents per village type for immersion
        if (villageType == VillageType.MINING) {
            // Small mine entrance or slag pile
            double mx = x + size * 0.08;
            double my = y + size * 0.72;
            gc.setFill(Color.web("#60584f"));
            gc.fillOval(mx, my, size * 0.14, size * 0.08);
            gc.setFill(Color.web("#444"));
            gc.fillRect(mx + size * 0.02, my - size * 0.04, size * 0.08, size * 0.06);
            // Add a tiny pickaxe icon
            gc.setStroke(Color.web("#333"));
            gc.setLineWidth(1);
            gc.strokeLine(mx + size * 0.06, my - size * 0.03, mx + size * 0.12, my + size * 0.02);
        } else if (villageType == VillageType.FISHING) {
            // Fish racks / drying lines near the water side of village
            double fx = x + size * 0.65;
            double fy = y + size * 0.42;
            gc.setStroke(Color.web("#8b7355"));
            gc.setLineWidth(1.5);
            gc.strokeLine(fx, fy, fx + size * 0.12, fy - size * 0.06);
            gc.strokeLine(fx + size * 0.02, fy + size * 0.02, fx + size * 0.14, fy - size * 0.02);
            // Small boat by variant
            if (variant % 2 == 0) {
                double shipX = x + size * 0.68;
                double shipY = y + size * 0.78;
                gc.setFill(DOCK_COLOR);
                gc.fillPolygon(new double[]{shipX, shipX + size * 0.02, shipX + size * 0.06}, new double[]{shipY + size * 0.02, shipY, shipY + size * 0.02}, 3);
            }
        } else if (villageType == VillageType.AGRICULTURAL) {
            // Small fields or a windmill marker
            double wx = x + size * 0.7;
            double wy = y + size * 0.18;
            gc.setFill(Color.web("#c9b575"));
            gc.fillRect(wx, wy, size * 0.12, size * 0.06);
            gc.setFill(Color.web("#8b4513"));
            gc.fillRect(wx + size * 0.05, wy - size * 0.06, size * 0.02, size * 0.06);
        }
    }
    
    /**
     * Renders a decoration specific to the village type.
     * NOTE: Currently disabled for cleaner sprite appearance.
     * Can be re-enabled by calling this method from renderVillageWithType.
     */
    @SuppressWarnings("unused")
    private static void renderVillageTypeDecoration(GraphicsContext gc, double x, double y, double size, VillageType villageType) {
        double centerX = x + size * 0.45;
        double centerY = y + size * 0.43;
        double iconSize = size * 0.15;
        
        if (villageType == null) {
            // Default: tree
            gc.setFill(Color.web("#228b22"));
            gc.fillOval(centerX, centerY, iconSize, iconSize);
            return;
        }
        
        switch (villageType) {
            case AGRICULTURAL:
                // Wheat/grain icon - golden stalks
                gc.setFill(Color.web("#daa520"));
                // Draw wheat stalks
                for (int i = 0; i < 3; i++) {
                    double sx = centerX + i * iconSize * 0.4;
                    gc.fillRect(sx, centerY + iconSize * 0.3, 2, iconSize * 0.7);
                    gc.fillOval(sx - 2, centerY, 6, iconSize * 0.4);
                }
                break;
                
            case PASTORAL:
                // Barn/animal icon - red barn
                gc.setFill(Color.web("#8b2500"));
                gc.fillRect(centerX, centerY + iconSize * 0.3, iconSize, iconSize * 0.7);
                gc.fillPolygon(
                    new double[]{centerX - 2, centerX + iconSize / 2, centerX + iconSize + 2},
                    new double[]{centerY + iconSize * 0.3, centerY, centerY + iconSize * 0.3},
                    3
                );
                break;
                
            case MINING:
                // Pickaxe/mine icon - gray rock with pick
                gc.setFill(Color.web("#696969"));
                gc.fillOval(centerX, centerY + iconSize * 0.2, iconSize, iconSize * 0.8);
                gc.setFill(Color.web("#404040"));
                gc.fillOval(centerX + iconSize * 0.2, centerY + iconSize * 0.4, iconSize * 0.6, iconSize * 0.5);
                break;
                
            case LUMBER:
                // Tree stump/logs icon
                gc.setFill(Color.web("#8b4513"));
                gc.fillOval(centerX + iconSize * 0.1, centerY + iconSize * 0.4, iconSize * 0.8, iconSize * 0.6);
                gc.setFill(Color.web("#228b22"));
                gc.fillOval(centerX, centerY, iconSize * 0.6, iconSize * 0.5);
                gc.fillOval(centerX + iconSize * 0.4, centerY + iconSize * 0.1, iconSize * 0.6, iconSize * 0.5);
                break;
                
            case FISHING:
                // Fish/boat icon - blue water with boat
                gc.setFill(Color.web("#4a90c0"));
                gc.fillOval(centerX, centerY + iconSize * 0.4, iconSize, iconSize * 0.6);
                gc.setFill(Color.web("#8b7355"));
                gc.fillPolygon(
                    new double[]{centerX + iconSize * 0.2, centerX + iconSize * 0.5, centerX + iconSize * 0.8},
                    new double[]{centerY + iconSize * 0.5, centerY + iconSize * 0.3, centerY + iconSize * 0.5},
                    3
                );
                break;
                
            case TRADING:
                // Market stall/coins icon
                gc.setFill(Color.web("#ffd700"));
                gc.fillOval(centerX, centerY + iconSize * 0.3, iconSize * 0.4, iconSize * 0.4);
                gc.fillOval(centerX + iconSize * 0.3, centerY + iconSize * 0.4, iconSize * 0.4, iconSize * 0.4);
                gc.fillOval(centerX + iconSize * 0.6, centerY + iconSize * 0.3, iconSize * 0.4, iconSize * 0.4);
                break;
                
            default:
                // Generic tree
                gc.setFill(Color.web("#228b22"));
                gc.fillOval(centerX, centerY, iconSize, iconSize);
                break;
        }
    }
    
    /**
     * Town with visual variant (4 different layouts).
     */
    private static void renderTownWithVariant(GraphicsContext gc, double x, double y, double size, VillageType villageType, int variant) {
        Color roofColor = villageType != null ? Color.web(villageType.getRoofColor()) : ROOF_BROWN;
        Color accentColor = villageType != null ? Color.web(villageType.getAccentColor()) : Color.web("#3a3a3a");
        
        // Paved area shape based on variant
        gc.setFill(Color.web("#9a8878", 0.6));
        switch (variant) {
            case 0: // Square market
                gc.fillRect(x + size * 0.2, y + size * 0.2, size * 0.6, size * 0.6);
                break;
            case 1: // Circular plaza
                gc.fillOval(x + size * 0.15, y + size * 0.15, size * 0.7, size * 0.7);
                break;
            case 2: // Diamond shape
                gc.fillPolygon(
                    new double[]{x + size * 0.5, x + size * 0.85, x + size * 0.5, x + size * 0.15},
                    new double[]{y + size * 0.15, y + size * 0.5, y + size * 0.85, y + size * 0.5},
                    4
                );
                break;
            default: // Rectangular
                gc.fillRect(x + size * 0.15, y + size * 0.25, size * 0.7, size * 0.5);
                break;
        }
        
        // Market square with accent
        gc.setFill(accentColor.deriveColor(0, 0.5, 1.5, 0.4));
        gc.fillRect(x + size * 0.35, y + size * 0.35, size * 0.3, size * 0.3);
        
        // Buildings around the square - positions vary by variant
        double buildingW = size * 0.18;
        double buildingH = size * 0.22;
        
        double[][] buildingPositions;
        switch (variant) {
            case 0:
                buildingPositions = new double[][] {
                    {0.15, 0.10}, {0.45, 0.08}, {0.12, 0.65}, {0.55, 0.68}, {0.02, 0.35}, {0.72, 0.30}
                };
                break;
            case 1:
                buildingPositions = new double[][] {
                    {0.20, 0.05}, {0.55, 0.10}, {0.08, 0.55}, {0.60, 0.60}, {0.05, 0.25}, {0.75, 0.35}
                };
                break;
            case 2:
                buildingPositions = new double[][] {
                    {0.35, 0.02}, {0.62, 0.25}, {0.62, 0.55}, {0.35, 0.75}, {0.08, 0.55}, {0.08, 0.25}
                };
                break;
            default:
                buildingPositions = new double[][] {
                    {0.18, 0.08}, {0.50, 0.05}, {0.15, 0.70}, {0.52, 0.72}, {0.02, 0.40}, {0.78, 0.38}
                };
                break;
        }
        
        Color[] roofVariants = {roofColor, roofColor.darker(), roofColor.brighter(), roofColor, roofColor.darker(), roofColor.brighter()};
        Color[] buildingColors = {BUILDING_1, BUILDING_2, BUILDING_3, BUILDING_1, BUILDING_2, BUILDING_3};
        
        for (int i = 0; i < buildingPositions.length; i++) {
            drawBuilding(gc, x + buildingPositions[i][0] * size, y + buildingPositions[i][1] * size, 
                buildingW * (0.9 + (i % 2) * 0.3), buildingH * (0.9 + (i % 3) * 0.1), 
                buildingColors[i], roofVariants[i]);
        }
        
        // Center feature varies by variant
        if (variant == 0 || variant == 3) {
            // Market stall
            gc.setFill(Color.web("#cc9966"));
            gc.fillRect(x + size * 0.42, y + size * 0.42, size * 0.16, size * 0.16);
            gc.setFill(Color.web("#ffffff", 0.8));
            gc.fillRect(x + size * 0.40, y + size * 0.38, size * 0.20, size * 0.06);
        } else if (variant == 1) {
            // Fountain
            gc.setFill(Color.web("#696969"));
            gc.fillOval(x + size * 0.40, y + size * 0.40, size * 0.20, size * 0.20);
            gc.setFill(Color.web("#4a90c0"));
            gc.fillOval(x + size * 0.43, y + size * 0.43, size * 0.14, size * 0.14);
        } else {
            // Statue
            gc.setFill(Color.web("#808080"));
            gc.fillRect(x + size * 0.44, y + size * 0.35, size * 0.12, size * 0.25);
            gc.fillOval(x + size * 0.42, y + size * 0.30, size * 0.16, size * 0.12);
        }
    }
    
    /**
     * City with visual variant (4 different layouts).
     */
    private static void renderCityWithVariant(GraphicsContext gc, double x, double y, double size, int variant) {
        // Outer wall shape varies
        gc.setFill(WALL_COLOR);
        switch (variant) {
            case 0: // Square fortress
                gc.fillRect(x + size * 0.08, y + size * 0.08, size * 0.84, size * 0.84);
                gc.setFill(Color.web("#a09080"));
                gc.fillRect(x + size * 0.12, y + size * 0.12, size * 0.76, size * 0.76);
                break;
            case 1: // Rounded walls
                gc.fillOval(x + size * 0.06, y + size * 0.06, size * 0.88, size * 0.88);
                gc.setFill(Color.web("#a09080"));
                gc.fillOval(x + size * 0.10, y + size * 0.10, size * 0.80, size * 0.80);
                break;
            case 2: // Hexagonal
                double[] hexX = new double[6];
                double[] hexY = new double[6];
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 6 + i * Math.PI / 3;
                    hexX[i] = x + size * 0.5 + size * 0.44 * Math.cos(angle);
                    hexY[i] = y + size * 0.5 + size * 0.44 * Math.sin(angle);
                }
                gc.fillPolygon(hexX, hexY, 6);
                gc.setFill(Color.web("#a09080"));
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 6 + i * Math.PI / 3;
                    hexX[i] = x + size * 0.5 + size * 0.38 * Math.cos(angle);
                    hexY[i] = y + size * 0.5 + size * 0.38 * Math.sin(angle);
                }
                gc.fillPolygon(hexX, hexY, 6);
                break;
            default: // Diamond shape
                gc.fillPolygon(
                    new double[]{x + size * 0.5, x + size * 0.92, x + size * 0.5, x + size * 0.08},
                    new double[]{y + size * 0.08, y + size * 0.5, y + size * 0.92, y + size * 0.5},
                    4
                );
                gc.setFill(Color.web("#a09080"));
                gc.fillPolygon(
                    new double[]{x + size * 0.5, x + size * 0.85, x + size * 0.5, x + size * 0.15},
                    new double[]{y + size * 0.15, y + size * 0.5, y + size * 0.85, y + size * 0.5},
                    4
                );
                break;
        }
        
        // Inner buildings - vary layout by variant
        double buildingW = size * 0.14;
        double buildingH = size * 0.18;
        
        // Central keep/castle
        gc.setFill(WALL_COLOR);
        gc.fillRect(x + size * 0.38, y + size * 0.38, size * 0.24, size * 0.24);
        gc.setFill(WALL_TOP);
        gc.fillRect(x + size * 0.40, y + size * 0.40, size * 0.20, size * 0.20);
        
        // Tower on keep
        gc.setFill(WALL_COLOR);
        gc.fillRect(x + size * 0.45, y + size * 0.30, size * 0.10, size * 0.15);
        gc.setFill(TOWER_GOLD);
        gc.fillPolygon(
            new double[]{x + size * 0.44, x + size * 0.50, x + size * 0.56},
            new double[]{y + size * 0.30, y + size * 0.22, y + size * 0.30},
            3
        );
        
        // Surrounding buildings - positions depend on variant
        double[][] buildingPos;
        switch (variant) {
            case 0:
                buildingPos = new double[][] {
                    {0.15, 0.15}, {0.65, 0.15}, {0.15, 0.65}, {0.65, 0.65},
                    {0.40, 0.15}, {0.15, 0.40}, {0.65, 0.40}, {0.40, 0.65}
                };
                break;
            case 1:
                buildingPos = new double[][] {
                    {0.25, 0.18}, {0.55, 0.18}, {0.18, 0.50}, {0.65, 0.50},
                    {0.25, 0.70}, {0.55, 0.70}, {0.40, 0.12}, {0.40, 0.75}
                };
                break;
            default:
                buildingPos = new double[][] {
                    {0.22, 0.25}, {0.60, 0.25}, {0.22, 0.55}, {0.60, 0.55},
                    {0.35, 0.18}, {0.52, 0.18}, {0.35, 0.68}, {0.52, 0.68}
                };
                break;
        }
        
        Color[] roofs = {ROOF_RED, ROOF_BLUE, ROOF_BROWN, ROOF_RED, ROOF_BROWN, ROOF_BLUE, ROOF_RED, ROOF_BROWN};
        Color[] walls = {BUILDING_1, BUILDING_2, BUILDING_3, BUILDING_1, BUILDING_2, BUILDING_3, BUILDING_1, BUILDING_2};
        
        for (int i = 0; i < buildingPos.length; i++) {
            drawBuilding(gc, x + buildingPos[i][0] * size, y + buildingPos[i][1] * size, 
                buildingW * (0.9 + (i % 2) * 0.2), buildingH * (0.85 + (i % 3) * 0.1), 
                walls[i], roofs[i]);
        }
        
        // Corner towers
        renderCornerTowers(gc, x, y, size, variant == 0 || variant == 3);
    }
    
    /**
     * Town with village type-specific theming (legacy, still used by renderTownWithType).
     */
    private static void renderTownWithType(GraphicsContext gc, double x, double y, double size, VillageType villageType) {
        Color roofColor = villageType != null ? Color.web(villageType.getRoofColor()) : ROOF_BROWN;
        Color accentColor = villageType != null ? Color.web(villageType.getAccentColor()) : Color.web("#3a3a3a");
        
        // Paved area (market square)
        gc.setFill(Color.web("#9a8878", 0.6));
        gc.fillRect(x + size * 0.2, y + size * 0.2, size * 0.6, size * 0.6);
        
        // Market square with accent
        gc.setFill(accentColor.deriveColor(0, 0.5, 1.5, 0.4));
        gc.fillRect(x + size * 0.35, y + size * 0.35, size * 0.3, size * 0.3);
        
        // Buildings around the square
        double buildingW = size * 0.18;
        double buildingH = size * 0.22;
        
        // Top buildings with village type roof color
        drawBuilding(gc, x + size * 0.15, y + size * 0.10, buildingW, buildingH, BUILDING_1, roofColor);
        drawBuilding(gc, x + size * 0.45, y + size * 0.08, buildingW * 1.3, buildingH, BUILDING_2, roofColor.darker());
        
        // Bottom buildings
        drawBuilding(gc, x + size * 0.12, y + size * 0.65, buildingW * 1.2, buildingH, BUILDING_3, roofColor);
        drawBuilding(gc, x + size * 0.55, y + size * 0.68, buildingW, buildingH * 0.9, BUILDING_1, roofColor.darker());
        
        // Side buildings
        drawBuilding(gc, x + size * 0.02, y + size * 0.35, buildingW * 0.9, buildingH, BUILDING_2, roofColor);
        drawBuilding(gc, x + size * 0.72, y + size * 0.30, buildingW, buildingH * 1.1, BUILDING_1, roofColor.darker());
        
        // Village type decoration in market center
        renderVillageTypeDecoration(gc, x + size * 0.32, y + size * 0.32, size * 0.35, villageType);
    }
    
    /**
     * Village: Small cluster of 3-4 simple houses.
     */
    private static void renderVillage(GraphicsContext gc, double x, double y, double size) {
        double houseSize = size * 0.22;
        double roofHeight = houseSize * 0.5;
        
        // Background grass area
        gc.setFill(Color.web("#5a7d4a", 0.3));
        gc.fillOval(x + size * 0.1, y + size * 0.1, size * 0.8, size * 0.8);
        
        // Draw 3-4 small houses
        double[][] housePositions = {
            {0.25, 0.25},
            {0.55, 0.20},
            {0.20, 0.55},
            {0.50, 0.50}
        };
        
        Color[] houseColors = {BUILDING_1, BUILDING_2, BUILDING_3, BUILDING_1};
        Color[] roofColors = {ROOF_BROWN, ROOF_RED, ROOF_BROWN, ROOF_RED};
        
        for (int i = 0; i < 4; i++) {
            double hx = x + housePositions[i][0] * size;
            double hy = y + housePositions[i][1] * size;
            
            // House body
            gc.setFill(houseColors[i]);
            gc.fillRect(hx, hy, houseSize, houseSize);
            
            // Roof (triangle)
            gc.setFill(roofColors[i]);
            gc.fillPolygon(
                new double[]{hx - houseSize * 0.1, hx + houseSize / 2, hx + houseSize * 1.1},
                new double[]{hy, hy - roofHeight, hy},
                3
            );
            
            // Door
            gc.setFill(DOOR_COLOR);
            gc.fillRect(hx + houseSize * 0.35, hy + houseSize * 0.5, houseSize * 0.3, houseSize * 0.5);
            
            // Window
            if (i < 2) {
                gc.setFill(WINDOW_COLOR);
                gc.fillRect(hx + houseSize * 0.15, hy + houseSize * 0.2, houseSize * 0.2, houseSize * 0.2);
            }
        }
        
        // Central well or tree
        gc.setFill(Color.web("#228b22"));
        gc.fillOval(x + size * 0.40, y + size * 0.38, size * 0.15, size * 0.15);
    }
    
    /**
     * Town: Medium settlement with market square and larger buildings.
     */
    private static void renderTown(GraphicsContext gc, double x, double y, double size) {
        // Paved area (market square)
        gc.setFill(Color.web("#9a8878", 0.6));
        gc.fillRect(x + size * 0.2, y + size * 0.2, size * 0.6, size * 0.6);
        
        // Market square
        gc.setFill(Color.web("#c4b090"));
        gc.fillRect(x + size * 0.35, y + size * 0.35, size * 0.3, size * 0.3);
        
        // Buildings around the square
        double buildingW = size * 0.18;
        double buildingH = size * 0.22;
        
        // Top buildings
        drawBuilding(gc, x + size * 0.15, y + size * 0.10, buildingW, buildingH, BUILDING_1, ROOF_RED);
        drawBuilding(gc, x + size * 0.45, y + size * 0.08, buildingW * 1.3, buildingH, BUILDING_2, ROOF_BLUE);
        
        // Bottom buildings
        drawBuilding(gc, x + size * 0.12, y + size * 0.65, buildingW * 1.2, buildingH, BUILDING_3, ROOF_BROWN);
        drawBuilding(gc, x + size * 0.55, y + size * 0.68, buildingW, buildingH * 0.9, BUILDING_1, ROOF_RED);
        
        // Side buildings
        drawBuilding(gc, x + size * 0.02, y + size * 0.35, buildingW * 0.9, buildingH, BUILDING_2, ROOF_BROWN);
        drawBuilding(gc, x + size * 0.72, y + size * 0.30, buildingW, buildingH * 1.1, BUILDING_1, ROOF_BLUE);
        
        // Market stall in center
        gc.setFill(Color.web("#cc9966"));
        gc.fillRect(x + size * 0.42, y + size * 0.42, size * 0.16, size * 0.16);
        gc.setFill(Color.web("#ffffff", 0.8));
        gc.fillRect(x + size * 0.40, y + size * 0.38, size * 0.20, size * 0.06);
    }
    
    /**
     * City: Large walled city with towers and many buildings.
     */
    private static void renderCity(GraphicsContext gc, double x, double y, double size) {
        double wallThickness = size * 0.06;
        double towerSize = size * 0.12;
        
        // City wall (outer)
        gc.setFill(WALL_COLOR);
        gc.fillRect(x + size * 0.05, y + size * 0.05, size * 0.9, size * 0.9);
        
        // Inner city area
        gc.setFill(Color.web("#a09080"));
        gc.fillRect(x + size * 0.1, y + size * 0.1, size * 0.8, size * 0.8);
        
        // Streets (cross pattern)
        gc.setFill(Color.web("#7a7060"));
        gc.fillRect(x + size * 0.45, y + size * 0.1, size * 0.1, size * 0.8);
        gc.fillRect(x + size * 0.1, y + size * 0.45, size * 0.8, size * 0.1);
        
        // Building blocks
        Color[] blockColors = {BUILDING_1, BUILDING_2, BUILDING_3, BUILDING_1};
        double blockSize = size * 0.28;
        double[][] blockPos = {
            {0.12, 0.12},
            {0.58, 0.12},
            {0.12, 0.58},
            {0.58, 0.58}
        };
        
        for (int i = 0; i < 4; i++) {
            gc.setFill(blockColors[i]);
            gc.fillRect(x + blockPos[i][0] * size, y + blockPos[i][1] * size, blockSize, blockSize);
            
            // Roofs
            gc.setFill(i % 2 == 0 ? ROOF_RED : ROOF_BLUE);
            gc.fillRect(x + blockPos[i][0] * size, y + blockPos[i][1] * size, blockSize, size * 0.05);
        }
        
        // Corner towers
        double[][] towerPos = {
            {0.02, 0.02},
            {0.86, 0.02},
            {0.02, 0.86},
            {0.86, 0.86}
        };
        
        for (double[] pos : towerPos) {
            // Tower base
            gc.setFill(WALL_COLOR);
            gc.fillRect(x + pos[0] * size, y + pos[1] * size, towerSize, towerSize);
            
            // Tower top
            gc.setFill(WALL_TOP);
            gc.fillRect(x + pos[0] * size - size * 0.01, y + pos[1] * size - size * 0.01, 
                       towerSize + size * 0.02, size * 0.03);
            
            // Tower roof
            gc.setFill(ROOF_BLUE);
            gc.fillPolygon(
                new double[]{x + pos[0] * size, x + (pos[0] + 0.06) * size, x + (pos[0] + 0.12) * size},
                new double[]{y + pos[1] * size, y + (pos[1] - 0.06) * size, y + pos[1] * size},
                3
            );
        }
        
        // Main gate
        gc.setFill(DOOR_COLOR);
        gc.fillRect(x + size * 0.43, y + size * 0.90, size * 0.14, size * 0.10);
        gc.setFill(Color.web("#3a2510"));
        gc.fillRect(x + size * 0.45, y + size * 0.92, size * 0.10, size * 0.08);
    }
    
    /**
     * Capital: Grand city with castle, multiple towers, and flags.
     */
    private static void renderCapital(GraphicsContext gc, double x, double y, double size) {
        // Outer fortifications
        gc.setFill(WALL_COLOR);
        gc.fillRect(x + size * 0.02, y + size * 0.02, size * 0.96, size * 0.96);
        
        // Inner city
        gc.setFill(Color.web("#b0a090"));
        gc.fillRect(x + size * 0.08, y + size * 0.08, size * 0.84, size * 0.84);
        
        // Central castle/palace
        double castleX = x + size * 0.30;
        double castleY = y + size * 0.25;
        double castleW = size * 0.40;
        double castleH = size * 0.35;
        
        // Castle base
        gc.setFill(Color.web("#8a7a6a"));
        gc.fillRect(castleX, castleY, castleW, castleH);
        
        // Castle main building
        gc.setFill(BUILDING_1);
        gc.fillRect(castleX + castleW * 0.15, castleY + castleH * 0.2, castleW * 0.7, castleH * 0.6);
        
        // Castle roof
        gc.setFill(ROOF_BLUE);
        gc.fillPolygon(
            new double[]{castleX + castleW * 0.10, castleX + castleW * 0.50, castleX + castleW * 0.90},
            new double[]{castleY + castleH * 0.2, castleY - castleH * 0.2, castleY + castleH * 0.2},
            3
        );
        
        // Castle towers
        double ctSize = size * 0.10;
        double[][] ctPos = {
            {0.28, 0.22},
            {0.62, 0.22},
            {0.28, 0.52},
            {0.62, 0.52}
        };
        
        for (int i = 0; i < ctPos.length; i++) {
            double tx = x + ctPos[i][0] * size;
            double ty = y + ctPos[i][1] * size;
            
            gc.setFill(WALL_COLOR);
            gc.fillRect(tx, ty, ctSize, ctSize * 1.3);
            
            gc.setFill(TOWER_GOLD);
            gc.fillPolygon(
                new double[]{tx - size * 0.01, tx + ctSize * 0.5, tx + ctSize + size * 0.01},
                new double[]{ty, ty - ctSize * 0.6, ty},
                3
            );
            
            // Flag on top towers
            if (i < 2) {
                gc.setStroke(Color.web("#4a4a4a"));
                gc.setLineWidth(1);
                gc.strokeLine(tx + ctSize * 0.5, ty - ctSize * 0.6, tx + ctSize * 0.5, ty - ctSize * 1.2);
                gc.setFill(i == 0 ? FLAG_RED : FLAG_BLUE);
                gc.fillPolygon(
                    new double[]{tx + ctSize * 0.5, tx + ctSize * 0.5, tx + ctSize * 0.9},
                    new double[]{ty - ctSize * 1.2, ty - ctSize * 0.85, ty - ctSize * 1.0},
                    3
                );
            }
        }
        
        // Surrounding buildings
        drawBuilding(gc, x + size * 0.10, y + size * 0.12, size * 0.15, size * 0.12, BUILDING_2, ROOF_RED);
        drawBuilding(gc, x + size * 0.75, y + size * 0.12, size * 0.15, size * 0.12, BUILDING_3, ROOF_RED);
        drawBuilding(gc, x + size * 0.10, y + size * 0.65, size * 0.18, size * 0.14, BUILDING_1, ROOF_BROWN);
        drawBuilding(gc, x + size * 0.72, y + size * 0.65, size * 0.18, size * 0.14, BUILDING_2, ROOF_BROWN);
        
        // Corner towers with flags
        renderCornerTowers(gc, x, y, size, true);
    }
    
    /**
     * Port Town: Coastal settlement with docks and warehouses.
     */
    private static void renderPortTown(GraphicsContext gc, double x, double y, double size) {
        // Water area (bottom portion)
        gc.setFill(WATER_COLOR);
        gc.fillRect(x, y + size * 0.65, size, size * 0.35);
        
        // Wave details
        gc.setStroke(Color.web("#6ab0d0"));
        gc.setLineWidth(1);
        for (int i = 0; i < 3; i++) {
            double wy = y + size * (0.72 + i * 0.08);
            gc.strokeLine(x + size * 0.1, wy, x + size * 0.3, wy - size * 0.02);
            gc.strokeLine(x + size * 0.3, wy - size * 0.02, x + size * 0.5, wy);
            gc.strokeLine(x + size * 0.5, wy, x + size * 0.7, wy - size * 0.02);
            gc.strokeLine(x + size * 0.7, wy - size * 0.02, x + size * 0.9, wy);
        }
        
        // Land area
        gc.setFill(Color.web("#c4b090"));
        gc.fillRect(x, y, size, size * 0.68);
        
        // Docks
        gc.setFill(DOCK_COLOR);
        gc.fillRect(x + size * 0.15, y + size * 0.55, size * 0.12, size * 0.25);
        gc.fillRect(x + size * 0.40, y + size * 0.52, size * 0.15, size * 0.28);
        gc.fillRect(x + size * 0.70, y + size * 0.55, size * 0.12, size * 0.25);
        
        // Dock supports
        gc.setFill(Color.web("#5a4a3a"));
        for (double dx : new double[]{0.16, 0.24, 0.42, 0.52, 0.72, 0.80}) {
            gc.fillRect(x + dx * size, y + size * 0.70, size * 0.02, size * 0.15);
        }
        
        // Warehouse buildings along waterfront
        drawBuilding(gc, x + size * 0.08, y + size * 0.38, size * 0.22, size * 0.18, BUILDING_3, ROOF_BROWN);
        drawBuilding(gc, x + size * 0.35, y + size * 0.35, size * 0.25, size * 0.18, BUILDING_2, ROOF_BROWN);
        drawBuilding(gc, x + size * 0.68, y + size * 0.38, size * 0.22, size * 0.18, BUILDING_3, ROOF_BROWN);
        
        // Town buildings (back row)
        drawBuilding(gc, x + size * 0.12, y + size * 0.08, size * 0.18, size * 0.22, BUILDING_1, ROOF_RED);
        drawBuilding(gc, x + size * 0.38, y + size * 0.05, size * 0.24, size * 0.25, BUILDING_2, ROOF_BLUE);
        drawBuilding(gc, x + size * 0.70, y + size * 0.08, size * 0.18, size * 0.22, BUILDING_1, ROOF_RED);
        
        // Lighthouse on one side
        double lhX = x + size * 0.88;
        double lhY = y + size * 0.55;
        gc.setFill(Color.web("#f0f0f0"));
        gc.fillRect(lhX, lhY, size * 0.08, size * 0.25);
        gc.setFill(Color.web("#cc3333"));
        gc.fillRect(lhX, lhY + size * 0.08, size * 0.08, size * 0.04);
        gc.fillRect(lhX, lhY + size * 0.16, size * 0.08, size * 0.04);
        
        // Lighthouse top
        gc.setFill(TOWER_GOLD);
        gc.fillOval(lhX - size * 0.01, lhY - size * 0.05, size * 0.10, size * 0.08);
        
        // Ship in harbor
        double shipX = x + size * 0.45;
        double shipY = y + size * 0.78;
        gc.setFill(DOCK_COLOR);
        gc.fillPolygon(
            new double[]{shipX, shipX + size * 0.03, shipX + size * 0.12, shipX + size * 0.15},
            new double[]{shipY + size * 0.04, shipY, shipY, shipY + size * 0.04},
            4
        );
        gc.setFill(Color.web("#ffffff"));
        gc.fillRect(shipX + size * 0.06, shipY - size * 0.08, size * 0.02, size * 0.08);
        gc.fillPolygon(
            new double[]{shipX + size * 0.08, shipX + size * 0.08, shipX + size * 0.14},
            new double[]{shipY - size * 0.07, shipY - size * 0.02, shipY - size * 0.04},
            3
        );
    }
    
    // ==================== Helper Methods ====================
    
    private static void drawBuilding(GraphicsContext gc, double x, double y, double w, double h, 
                                     Color wallColor, Color roofColor) {
        // Building body
        gc.setFill(wallColor);
        gc.fillRect(x, y + h * 0.2, w, h * 0.8);
        
        // Roof
        gc.setFill(roofColor);
        gc.fillPolygon(
            new double[]{x - w * 0.05, x + w / 2, x + w * 1.05},
            new double[]{y + h * 0.2, y, y + h * 0.2},
            3
        );
        
        // Windows
        gc.setFill(WINDOW_COLOR);
        int numWindows = (int) Math.max(1, w / 15);
        double windowW = w * 0.12;
        double windowH = h * 0.15;
        double spacing = w / (numWindows + 1);
        
        for (int i = 1; i <= numWindows; i++) {
            gc.fillRect(x + spacing * i - windowW / 2, y + h * 0.35, windowW, windowH);
        }
        
        // Door
        gc.setFill(DOOR_COLOR);
        gc.fillRect(x + w * 0.4, y + h * 0.6, w * 0.2, h * 0.4);
    }
    
    private static void renderCornerTowers(GraphicsContext gc, double x, double y, double size, boolean withFlags) {
        double towerSize = size * 0.10;
        double[][] positions = {
            {0.0, 0.0},
            {0.90, 0.0},
            {0.0, 0.90},
            {0.90, 0.90}
        };
        
        for (int i = 0; i < positions.length; i++) {
            double tx = x + positions[i][0] * size;
            double ty = y + positions[i][1] * size;
            
            // Tower body
            gc.setFill(WALL_COLOR);
            gc.fillRect(tx, ty, towerSize, towerSize);
            
            // Tower top
            gc.setFill(WALL_TOP);
            gc.fillRect(tx - size * 0.01, ty - size * 0.02, towerSize + size * 0.02, size * 0.04);
            
            // Crenellations
            gc.setFill(WALL_COLOR);
            for (int j = 0; j < 3; j++) {
                gc.fillRect(tx + j * towerSize * 0.35, ty - size * 0.04, towerSize * 0.2, size * 0.03);
            }
            
            // Flag
            if (withFlags && i < 2) {
                gc.setStroke(Color.web("#3a3a3a"));
                gc.setLineWidth(1);
                gc.strokeLine(tx + towerSize * 0.5, ty - size * 0.04, tx + towerSize * 0.5, ty - size * 0.15);
                gc.setFill(i == 0 ? FLAG_RED : FLAG_BLUE);
                gc.fillPolygon(
                    new double[]{tx + towerSize * 0.5, tx + towerSize * 0.5, tx + towerSize * 0.9},
                    new double[]{ty - size * 0.15, ty - size * 0.08, ty - size * 0.11},
                    3
                );
            }
        }
    }
}
