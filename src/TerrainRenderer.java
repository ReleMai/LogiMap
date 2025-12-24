import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import java.util.Random;

/**
 * Professional terrain tile renderer with detailed texturing and smooth blending.
 * 
 * Features:
 * - Multi-layer terrain texturing
 * - Smooth edge blending between different terrain types
 * - Animated water with waves and foam
 * - Decorative details (grass tufts, rocks, etc.)
 * - Stylized medieval map aesthetic
 */
public class TerrainRenderer {
    
    // Texture patterns for each terrain type
    private static final int TEXTURE_DETAIL = 4;  // Sub-divisions per tile
    
    // Animation state
    private double waterAnimPhase = 0;
    private double wavePhase = 0;
    
    // Random for consistent texturing
    private long seed;
    private Random textureRandom;
    
    public TerrainRenderer(long seed) {
        this.seed = seed;
        this.textureRandom = new Random(seed);
    }
    
    /**
     * Updates animation phases.
     */
    public void update(double deltaTime) {
        waterAnimPhase += deltaTime * 0.5;
        wavePhase += deltaTime * 2.0;
        if (waterAnimPhase > Math.PI * 2) waterAnimPhase -= Math.PI * 2;
        if (wavePhase > Math.PI * 2) wavePhase -= Math.PI * 2;
    }
    
    /**
     * Renders a terrain tile with full detail and blending.
     */
    public void renderTile(GraphicsContext gc, int x, int y, double screenX, double screenY, 
                          double size, TerrainType terrain, TerrainType[][] neighbors) {
        if (terrain == null) return;
        
        // Reset random for consistent texturing at this position
        textureRandom.setSeed(seed + x * 10000L + y);
        
        if (terrain.isWater()) {
            renderWaterTile(gc, x, y, screenX, screenY, size, terrain, neighbors);
        } else {
            renderLandTile(gc, x, y, screenX, screenY, size, terrain, neighbors);
        }
    }
    
    /**
     * Renders a land terrain tile with texture and blending.
     */
    private void renderLandTile(GraphicsContext gc, int x, int y, double screenX, double screenY,
                                double size, TerrainType terrain, TerrainType[][] neighbors) {
        // Base color with slight variation
        Color baseColor = terrain.getPrimaryColor();
        Color shadowColor = terrain.getShadowColor();
        
        // Fill base
        gc.setFill(baseColor);
        gc.fillRect(screenX, screenY, size, size);
        
        // Add texture pattern based on terrain type
        renderTerrainTexture(gc, x, y, screenX, screenY, size, terrain);
        
        // Blend edges with neighboring tiles
        renderEdgeBlending(gc, x, y, screenX, screenY, size, terrain, neighbors);
        
        // Add decorative details
        renderTerrainDetails(gc, x, y, screenX, screenY, size, terrain);
    }
    
    /**
     * Renders terrain-specific texture patterns.
     */
    private void renderTerrainTexture(GraphicsContext gc, int x, int y, double screenX, double screenY,
                                      double size, TerrainType terrain) {
        double cellSize = size / TEXTURE_DETAIL;
        
        switch (terrain) {
            case GRASS:
            case PLAINS:
            case MEADOW:
                renderGrassTexture(gc, screenX, screenY, size, terrain);
                break;
            case FOREST:
            case DENSE_FOREST:
            case TAIGA:
            case JUNGLE:
                renderForestTexture(gc, screenX, screenY, size, terrain);
                break;
            case DESERT:
            case DUNES:
            case SAVANNA:
                renderDesertTexture(gc, screenX, screenY, size, terrain);
                break;
            case BEACH:
                renderBeachTexture(gc, screenX, screenY, size);
                break;
            case HILLS:
            case ROCKY_HILLS:
                renderHillsTexture(gc, screenX, screenY, size, terrain);
                break;
            case MOUNTAIN:
            case MOUNTAIN_PEAK:
                renderMountainTexture(gc, screenX, screenY, size, terrain);
                break;
            case SNOW:
            case TUNDRA:
            case ICE:
            case GLACIER:
                renderSnowTexture(gc, screenX, screenY, size, terrain);
                break;
            case SWAMP:
            case MARSH:
                renderSwampTexture(gc, screenX, screenY, size, terrain);
                break;
            default:
                // No extra texture
                break;
        }
    }
    
    /**
     * Renders grass texture with small tufts and color variation.
     */
    private void renderGrassTexture(GraphicsContext gc, double screenX, double screenY, 
                                   double size, TerrainType terrain) {
        Color base = terrain.getPrimaryColor();
        
        // Add color variation spots
        for (int i = 0; i < 6; i++) {
            double spotX = screenX + textureRandom.nextDouble() * size;
            double spotY = screenY + textureRandom.nextDouble() * size;
            double spotSize = size * (0.1 + textureRandom.nextDouble() * 0.15);
            
            // Slightly darker or lighter
            Color spotColor = textureRandom.nextBoolean() 
                ? base.darker().desaturate()
                : base.brighter().saturate();
            gc.setFill(spotColor.deriveColor(0, 1, 1, 0.3));
            gc.fillOval(spotX - spotSize/2, spotY - spotSize/2, spotSize, spotSize);
        }
        
        // Small grass tuft lines
        gc.setStroke(base.darker());
        gc.setLineWidth(0.5);
        for (int i = 0; i < 3; i++) {
            double tuftX = screenX + textureRandom.nextDouble() * size;
            double tuftY = screenY + size * 0.6 + textureRandom.nextDouble() * size * 0.4;
            double height = size * (0.08 + textureRandom.nextDouble() * 0.08);
            gc.strokeLine(tuftX, tuftY, tuftX + (textureRandom.nextDouble() - 0.5) * 2, tuftY - height);
        }
    }
    
    /**
     * Renders forest texture with tree shadows.
     */
    private void renderForestTexture(GraphicsContext gc, double screenX, double screenY,
                                    double size, TerrainType terrain) {
        Color base = terrain.getPrimaryColor();
        Color shadow = terrain.getShadowColor();
        
        // Tree canopy shadows (dark spots)
        for (int i = 0; i < 4; i++) {
            double treeX = screenX + textureRandom.nextDouble() * size;
            double treeY = screenY + textureRandom.nextDouble() * size;
            double treeSize = size * (0.2 + textureRandom.nextDouble() * 0.25);
            
            gc.setFill(shadow.deriveColor(0, 1, 1, 0.4));
            gc.fillOval(treeX - treeSize/2, treeY - treeSize/2, treeSize, treeSize);
            
            // Slightly lighter center
            gc.setFill(base.deriveColor(0, 1, 1.1, 0.3));
            gc.fillOval(treeX - treeSize/4, treeY - treeSize/3, treeSize/2, treeSize/2);
        }
    }
    
    /**
     * Renders desert texture with sand patterns.
     */
    private void renderDesertTexture(GraphicsContext gc, double screenX, double screenY,
                                    double size, TerrainType terrain) {
        Color base = terrain.getPrimaryColor();
        
        // Sand ripple lines
        gc.setStroke(base.darker().deriveColor(0, 1, 1, 0.3));
        gc.setLineWidth(0.5);
        for (int i = 0; i < 3; i++) {
            double y = screenY + (i + 1) * size / 4;
            double wave = Math.sin((screenX + i * 10) * 0.1) * 2;
            gc.strokeLine(screenX, y + wave, screenX + size, y - wave);
        }
        
        // Occasional darker sand spots
        if (textureRandom.nextDouble() < 0.3) {
            double spotX = screenX + textureRandom.nextDouble() * size;
            double spotY = screenY + textureRandom.nextDouble() * size;
            gc.setFill(base.darker().deriveColor(0, 1, 1, 0.2));
            gc.fillOval(spotX, spotY, size * 0.15, size * 0.1);
        }
    }
    
    /**
     * Renders beach texture with sand and pebbles.
     */
    private void renderBeachTexture(GraphicsContext gc, double screenX, double screenY, double size) {
        // Small pebbles
        gc.setFill(Color.web("#a09080").deriveColor(0, 1, 1, 0.5));
        for (int i = 0; i < 3; i++) {
            double px = screenX + textureRandom.nextDouble() * size;
            double py = screenY + textureRandom.nextDouble() * size;
            double ps = size * (0.03 + textureRandom.nextDouble() * 0.04);
            gc.fillOval(px, py, ps, ps * 0.7);
        }
        
        // Shell-like specks
        if (textureRandom.nextDouble() < 0.2) {
            gc.setFill(Color.web("#ffffff").deriveColor(0, 1, 1, 0.3));
            double sx = screenX + textureRandom.nextDouble() * size;
            double sy = screenY + textureRandom.nextDouble() * size;
            gc.fillOval(sx, sy, size * 0.05, size * 0.03);
        }
    }
    
    /**
     * Renders hills texture with contour-like shadows.
     */
    private void renderHillsTexture(GraphicsContext gc, double screenX, double screenY,
                                   double size, TerrainType terrain) {
        Color shadow = terrain.getShadowColor();
        
        // Contour shadow arcs
        gc.setStroke(shadow.deriveColor(0, 1, 1, 0.3));
        gc.setLineWidth(1);
        
        for (int i = 0; i < 2; i++) {
            double arcX = screenX + textureRandom.nextDouble() * size * 0.5;
            double arcY = screenY + textureRandom.nextDouble() * size * 0.5;
            double arcW = size * (0.3 + textureRandom.nextDouble() * 0.3);
            double arcH = size * (0.2 + textureRandom.nextDouble() * 0.2);
            gc.strokeArc(arcX, arcY, arcW, arcH, 0, 180, javafx.scene.shape.ArcType.OPEN);
        }
        
        // Rocky hills get small rock shapes
        if (terrain == TerrainType.ROCKY_HILLS) {
            gc.setFill(Color.web("#666666").deriveColor(0, 1, 1, 0.4));
            for (int i = 0; i < 2; i++) {
                double rx = screenX + textureRandom.nextDouble() * size;
                double ry = screenY + textureRandom.nextDouble() * size;
                gc.fillOval(rx, ry, size * 0.1, size * 0.06);
            }
        }
    }
    
    /**
     * Renders mountain texture with rocky patterns.
     */
    private void renderMountainTexture(GraphicsContext gc, double screenX, double screenY,
                                      double size, TerrainType terrain) {
        Color base = terrain.getPrimaryColor();
        Color shadow = terrain.getShadowColor();
        
        // Rocky cracks/lines
        gc.setStroke(shadow.deriveColor(0, 1, 1, 0.5));
        gc.setLineWidth(0.5);
        for (int i = 0; i < 3; i++) {
            double x1 = screenX + textureRandom.nextDouble() * size;
            double y1 = screenY + textureRandom.nextDouble() * size;
            double x2 = x1 + (textureRandom.nextDouble() - 0.5) * size * 0.3;
            double y2 = y1 + textureRandom.nextDouble() * size * 0.3;
            gc.strokeLine(x1, y1, x2, y2);
        }
        
        // Snow caps for mountain peaks
        if (terrain == TerrainType.MOUNTAIN_PEAK) {
            gc.setFill(Color.web("#ffffff").deriveColor(0, 1, 1, 0.5));
            double snowX = screenX + size * 0.2;
            double snowY = screenY + size * 0.1;
            gc.fillOval(snowX, snowY, size * 0.6, size * 0.4);
        }
    }
    
    /**
     * Renders snow texture with sparkle effects.
     */
    private void renderSnowTexture(GraphicsContext gc, double screenX, double screenY,
                                  double size, TerrainType terrain) {
        // Snow sparkles
        gc.setFill(Color.web("#ffffff").deriveColor(0, 1, 1, 0.6));
        for (int i = 0; i < 4; i++) {
            double sx = screenX + textureRandom.nextDouble() * size;
            double sy = screenY + textureRandom.nextDouble() * size;
            double ss = size * 0.02;
            gc.fillOval(sx, sy, ss, ss);
        }
        
        // Subtle blue shadows for depth
        if (terrain == TerrainType.ICE || terrain == TerrainType.GLACIER) {
            gc.setFill(Color.web("#80c0ff").deriveColor(0, 1, 1, 0.2));
            double iceX = screenX + textureRandom.nextDouble() * size * 0.5;
            double iceY = screenY + textureRandom.nextDouble() * size * 0.5;
            gc.fillRect(iceX, iceY, size * 0.3, size * 0.2);
        }
    }
    
    /**
     * Renders swamp texture with murky water patches.
     */
    private void renderSwampTexture(GraphicsContext gc, double screenX, double screenY,
                                   double size, TerrainType terrain) {
        // Murky water patches
        gc.setFill(Color.web("#304030").deriveColor(0, 1, 1, 0.3));
        for (int i = 0; i < 2; i++) {
            double px = screenX + textureRandom.nextDouble() * size;
            double py = screenY + textureRandom.nextDouble() * size;
            double ps = size * (0.15 + textureRandom.nextDouble() * 0.15);
            gc.fillOval(px, py, ps, ps * 0.7);
        }
        
        // Moss/algae streaks
        gc.setFill(Color.web("#405030").deriveColor(0, 1, 1, 0.4));
        double mossX = screenX + textureRandom.nextDouble() * size;
        double mossY = screenY + textureRandom.nextDouble() * size;
        gc.fillOval(mossX, mossY, size * 0.2, size * 0.05);
    }
    
    /**
     * Renders smooth blending at tile edges.
     */
    private void renderEdgeBlending(GraphicsContext gc, int x, int y, double screenX, double screenY,
                                   double size, TerrainType terrain, TerrainType[][] neighbors) {
        if (neighbors == null) return;
        
        double blendSize = size * 0.25;  // How far the blend extends
        
        // Blend with each neighbor
        // North (y-1)
        if (neighbors[1][0] != null && neighbors[1][0] != terrain && !neighbors[1][0].isWater()) {
            blendEdge(gc, screenX, screenY, size, blendSize, 0, neighbors[1][0], terrain);
        }
        // South (y+1)
        if (neighbors[1][2] != null && neighbors[1][2] != terrain && !neighbors[1][2].isWater()) {
            blendEdge(gc, screenX, screenY, size, blendSize, 2, neighbors[1][2], terrain);
        }
        // West (x-1)
        if (neighbors[0][1] != null && neighbors[0][1] != terrain && !neighbors[0][1].isWater()) {
            blendEdge(gc, screenX, screenY, size, blendSize, 3, neighbors[0][1], terrain);
        }
        // East (x+1)
        if (neighbors[2][1] != null && neighbors[2][1] != terrain && !neighbors[2][1].isWater()) {
            blendEdge(gc, screenX, screenY, size, blendSize, 1, neighbors[2][1], terrain);
        }
    }
    
    /**
     * Blends an edge with a neighboring terrain.
     * @param direction 0=North, 1=East, 2=South, 3=West
     */
    private void blendEdge(GraphicsContext gc, double screenX, double screenY, double size,
                          double blendSize, int direction, TerrainType neighbor, TerrainType current) {
        Color neighborColor = neighbor.getPrimaryColor();
        
        // Create gradient blend
        double startX, startY, endX, endY;
        switch (direction) {
            case 0: // North - blend at top
                startX = screenX; startY = screenY;
                endX = screenX; endY = screenY + blendSize;
                gc.setFill(neighborColor.deriveColor(0, 1, 1, 0.3));
                gc.fillRect(screenX, screenY, size, blendSize * 0.5);
                break;
            case 2: // South - blend at bottom
                gc.setFill(neighborColor.deriveColor(0, 1, 1, 0.3));
                gc.fillRect(screenX, screenY + size - blendSize * 0.5, size, blendSize * 0.5);
                break;
            case 3: // West - blend at left
                gc.setFill(neighborColor.deriveColor(0, 1, 1, 0.3));
                gc.fillRect(screenX, screenY, blendSize * 0.5, size);
                break;
            case 1: // East - blend at right
                gc.setFill(neighborColor.deriveColor(0, 1, 1, 0.3));
                gc.fillRect(screenX + size - blendSize * 0.5, screenY, blendSize * 0.5, size);
                break;
        }
    }
    
    /**
     * Renders additional decorative details.
     */
    private void renderTerrainDetails(GraphicsContext gc, int x, int y, double screenX, double screenY,
                                     double size, TerrainType terrain) {
        // Add random small details based on terrain
        if (textureRandom.nextDouble() < 0.15) {
            switch (terrain) {
                case GRASS:
                case PLAINS:
                case MEADOW:
                    // Small flower dots
                    Color[] flowerColors = {Color.web("#ff6688"), Color.web("#ffff66"), 
                                           Color.web("#ffffff"), Color.web("#ff88ff")};
                    Color flower = flowerColors[textureRandom.nextInt(flowerColors.length)];
                    double fx = screenX + textureRandom.nextDouble() * size;
                    double fy = screenY + textureRandom.nextDouble() * size;
                    gc.setFill(flower);
                    gc.fillOval(fx, fy, size * 0.04, size * 0.04);
                    break;
                case HILLS:
                case ROCKY_HILLS:
                    // Small rock
                    gc.setFill(Color.web("#808080").deriveColor(0, 1, 1, 0.6));
                    double rx = screenX + textureRandom.nextDouble() * size;
                    double ry = screenY + textureRandom.nextDouble() * size;
                    gc.fillOval(rx, ry, size * 0.08, size * 0.05);
                    break;
                default:
                    break;
            }
        }
    }
    
    // ==================== WATER RENDERING ====================
    
    /**
     * Renders an animated water tile with enhanced water appearance.
     */
    private void renderWaterTile(GraphicsContext gc, int x, int y, double screenX, double screenY,
                                double size, TerrainType terrain, TerrainType[][] neighbors) {
        // Get water depth for color
        Color baseColor = getWaterBaseColor(terrain);
        
        // Animated color shift - more visible
        double shift = Math.sin(waterAnimPhase + x * 0.3 + y * 0.2) * 0.08;
        Color animatedColor = baseColor.deriveColor(shift * 10, 1, 1 + shift, 1);
        
        // Fill base water
        gc.setFill(animatedColor);
        gc.fillRect(screenX, screenY, size, size);
        
        // Add water depth gradient overlay for more water-like look
        renderWaterDepthGradient(gc, screenX, screenY, size, terrain);
        
        // Add wave patterns - more visible
        renderWaves(gc, x, y, screenX, screenY, size, terrain);
        
        // Add foam near shores with curved corners
        renderShoreFoam(gc, x, y, screenX, screenY, size, neighbors);
        
        // Add water decorations (fish, lilypads for shallow) - spread out more
        renderWaterDecorations(gc, x, y, screenX, screenY, size, terrain);
    }
    
    /**
     * Adds depth gradient to water for more realistic appearance.
     */
    private void renderWaterDepthGradient(GraphicsContext gc, double screenX, double screenY, 
                                          double size, TerrainType terrain) {
        // Add subtle horizontal gradient for depth effect
        gc.setFill(Color.web("#ffffff").deriveColor(0, 1, 1, 0.06));
        gc.fillRect(screenX, screenY, size, size * 0.3);
        
        // Add slight darker bottom for depth
        gc.setFill(Color.web("#000000").deriveColor(0, 1, 1, 0.08));
        gc.fillRect(screenX, screenY + size * 0.7, size, size * 0.3);
        
        // Add shimmer effect
        double shimmerX = screenX + size * 0.3 + Math.sin(wavePhase * 2) * size * 0.1;
        gc.setFill(Color.web("#ffffff").deriveColor(0, 1, 1, 0.1));
        gc.fillOval(shimmerX, screenY + size * 0.2, size * 0.15, size * 0.08);
    }
    
    /**
     * Gets the base color for water based on depth - more saturated colors.
     */
    private Color getWaterBaseColor(TerrainType terrain) {
        switch (terrain) {
            case DEEP_OCEAN:
                return Color.web("#0a3055");
            case OCEAN:
                return Color.web("#1a5595");
            case SHALLOW_WATER:
                return Color.web("#3585c8");
            case REEF:
                return Color.web("#35b0b0");
            default:
                return Color.web("#2070b0");
        }
    }
    
    /**
     * Renders animated wave patterns - optimized for performance.
     */
    private void renderWaves(GraphicsContext gc, int x, int y, double screenX, double screenY,
                            double size, TerrainType terrain) {
        // Skip wave rendering at small zoom levels for performance
        if (size < 10) return;
        
        // Light wave highlights - reduced to 2 waves (was 4)
        gc.setStroke(Color.web("#ffffff").deriveColor(0, 1, 1, 0.25));
        gc.setLineWidth(1.0);
        
        for (int i = 0; i < 2; i++) {
            double waveY = screenY + (i + 0.5) * size / 2.2;
            double offset = Math.sin(wavePhase + x * 0.5 + i * 0.7) * size * 0.08;
            
            gc.beginPath();
            gc.moveTo(screenX, waveY + offset);
            // Simplified wave - fewer control points (size/4 instead of size/8)
            for (double wx = 0; wx <= size; wx += size / 4) {
                double wy = waveY + Math.sin(wavePhase * 1.2 + (screenX + wx) * 0.08 + i * 0.6) * size * 0.04;
                gc.lineTo(screenX + wx, wy + offset);
            }
            gc.stroke();
        }
        
        // Removed wave shadows for performance
    }
    
    /**
     * Renders foam effects near shorelines with curved corners.
     */
    private void renderShoreFoam(GraphicsContext gc, int x, int y, double screenX, double screenY,
                                double size, TerrainType[][] neighbors) {
        if (neighbors == null) return;
        
        double foamSize = size * 0.15;
        double foamSpacing = size / 6;
        double waveOffset = Math.sin(wavePhase) * size * 0.04;
        
        // Check cardinal directions for land
        boolean northLand = neighbors[1][0] != null && !neighbors[1][0].isWater();
        boolean southLand = neighbors[1][2] != null && !neighbors[1][2].isWater();
        boolean westLand = neighbors[0][1] != null && !neighbors[0][1].isWater();
        boolean eastLand = neighbors[2][1] != null && !neighbors[2][1].isWater();
        
        // Check corners for curved foam
        boolean nwLand = neighbors[0][0] != null && !neighbors[0][0].isWater();
        boolean neLand = neighbors[2][0] != null && !neighbors[2][0].isWater();
        boolean swLand = neighbors[0][2] != null && !neighbors[0][2].isWater();
        boolean seLand = neighbors[2][2] != null && !neighbors[2][2].isWater();
        
        gc.setFill(Color.web("#ffffff").deriveColor(0, 1, 1, 0.5));
        
        // North shore foam
        if (northLand) {
            double foamY = screenY + waveOffset;
            for (double fx = foamSpacing/2; fx < size; fx += foamSpacing) {
                double bubbleOffset = Math.sin(wavePhase + fx * 0.3) * size * 0.02;
                gc.fillOval(screenX + fx - foamSize/2, foamY + bubbleOffset, foamSize, foamSize * 0.5);
            }
        }
        // South shore foam
        if (southLand) {
            double foamY = screenY + size - foamSize * 0.6 - waveOffset;
            for (double fx = foamSpacing/2; fx < size; fx += foamSpacing) {
                double bubbleOffset = Math.sin(wavePhase + fx * 0.3) * size * 0.02;
                gc.fillOval(screenX + fx - foamSize/2, foamY + bubbleOffset, foamSize, foamSize * 0.5);
            }
        }
        // West shore foam
        if (westLand) {
            double foamX = screenX + waveOffset;
            for (double fy = foamSpacing/2; fy < size; fy += foamSpacing) {
                double bubbleOffset = Math.sin(wavePhase + fy * 0.3) * size * 0.02;
                gc.fillOval(foamX + bubbleOffset, screenY + fy - foamSize/2, foamSize * 0.5, foamSize);
            }
        }
        // East shore foam
        if (eastLand) {
            double foamX = screenX + size - foamSize * 0.6 - waveOffset;
            for (double fy = foamSpacing/2; fy < size; fy += foamSpacing) {
                double bubbleOffset = Math.sin(wavePhase + fy * 0.3) * size * 0.02;
                gc.fillOval(foamX + bubbleOffset, screenY + fy - foamSize/2, foamSize * 0.5, foamSize);
            }
        }
        
        // Render curved corner foam
        double cornerRadius = size * 0.25;
        gc.setFill(Color.web("#ffffff").deriveColor(0, 1, 1, 0.45));
        
        // Northwest corner curve
        if (nwLand && !northLand && !westLand) {
            renderCornerFoam(gc, screenX, screenY, cornerRadius, 0, waveOffset);
        }
        // Northeast corner curve
        if (neLand && !northLand && !eastLand) {
            renderCornerFoam(gc, screenX + size - cornerRadius, screenY, cornerRadius, 1, waveOffset);
        }
        // Southwest corner curve
        if (swLand && !southLand && !westLand) {
            renderCornerFoam(gc, screenX, screenY + size - cornerRadius, cornerRadius, 2, waveOffset);
        }
        // Southeast corner curve
        if (seLand && !southLand && !eastLand) {
            renderCornerFoam(gc, screenX + size - cornerRadius, screenY + size - cornerRadius, cornerRadius, 3, waveOffset);
        }
    }
    
    /**
     * Renders curved foam at corners where diagonal land meets water.
     * @param corner 0=NW, 1=NE, 2=SW, 3=SE
     */
    private void renderCornerFoam(GraphicsContext gc, double x, double y, double radius, int corner, double waveOffset) {
        int segments = 5;
        double startAngle, endAngle;
        
        switch (corner) {
            case 0: startAngle = Math.PI; endAngle = Math.PI * 1.5; break;      // NW
            case 1: startAngle = Math.PI * 1.5; endAngle = Math.PI * 2; break;  // NE
            case 2: startAngle = Math.PI * 0.5; endAngle = Math.PI; break;      // SW
            case 3: startAngle = 0; endAngle = Math.PI * 0.5; break;            // SE
            default: return;
        }
        
        double centerX = x + (corner == 0 || corner == 2 ? radius : 0);
        double centerY = y + (corner == 0 || corner == 1 ? radius : 0);
        
        for (int i = 0; i <= segments; i++) {
            double angle = startAngle + (endAngle - startAngle) * i / segments;
            double foamR = radius * (0.85 + Math.sin(wavePhase + i * 0.5) * 0.1);
            double fx = centerX + Math.cos(angle) * foamR;
            double fy = centerY + Math.sin(angle) * foamR;
            gc.fillOval(fx - radius * 0.1, fy - radius * 0.08, radius * 0.2, radius * 0.16);
        }
    }
    
    /**
     * Renders water decorations like fish shadows or lilypads - sparse for better performance.
     */
    private void renderWaterDecorations(GraphicsContext gc, int x, int y, double screenX, double screenY,
                                       double size, TerrainType terrain) {
        // Only render at reasonable zoom levels for performance
        if (size < 12) return;  // Increased threshold for performance
        
        // Unique seed per tile for consistent but varied placement
        long tileSeed = seed + x * 31337L + y * 7919L;
        textureRandom.setSeed(tileSeed);
        
        // Shallow water lilypads - REDUCED: 12% chance (was 30%), max 2 (was 3)
        if (terrain == TerrainType.SHALLOW_WATER) {
            if (textureRandom.nextDouble() < 0.12) {
                int lilyCount = 1 + textureRandom.nextInt(2);  // 1-2 (was 1-3)
                for (int i = 0; i < lilyCount; i++) {
                    // Fully randomized positions within tile (no grid pattern)
                    double lx = screenX + textureRandom.nextDouble() * size * 0.7 + size * 0.1;
                    double ly = screenY + textureRandom.nextDouble() * size * 0.7 + size * 0.1;
                    double lilySize = size * (0.12 + textureRandom.nextDouble() * 0.08);
                    
                    // Lilypad
                    gc.setFill(Color.web("#408040").deriveColor(0, 1, 1, 0.7));
                    gc.fillOval(lx, ly, lilySize, lilySize * 0.8);
                    
                    // Lily pad detail (notch)
                    gc.setFill(getWaterBaseColor(terrain));
                    gc.fillArc(lx + lilySize * 0.3, ly + lilySize * 0.2, lilySize * 0.4, lilySize * 0.4, 180, 90, javafx.scene.shape.ArcType.ROUND);
                    
                    // Pink flower on some (reduced to 25% from 40%)
                    if (textureRandom.nextDouble() < 0.25) {
                        gc.setFill(Color.web("#ff88aa"));
                        gc.fillOval(lx + lilySize * 0.3, ly + lilySize * 0.15, lilySize * 0.35, lilySize * 0.35);
                        gc.setFill(Color.web("#ffdd66"));
                        gc.fillOval(lx + lilySize * 0.38, ly + lilySize * 0.23, lilySize * 0.18, lilySize * 0.18);
                    }
                }
            }
        }
        
        // Reef corals - REDUCED: 20% chance (was 50%), max 2 (was 2-4)
        if (terrain == TerrainType.REEF) {
            if (textureRandom.nextDouble() < 0.20) {
                int coralCount = 1 + textureRandom.nextInt(2);  // 1-2 (was 2-4)
                Color[] coralColors = {Color.web("#ff6060"), Color.web("#ff8040"), 
                                      Color.web("#ffff60"), Color.web("#60ff60"), Color.web("#ff60c0")};
                for (int i = 0; i < coralCount; i++) {
                    double cx = screenX + textureRandom.nextDouble() * size * 0.8 + size * 0.1;
                    double cy = screenY + textureRandom.nextDouble() * size * 0.8 + size * 0.1;
                    double coralSize = size * (0.08 + textureRandom.nextDouble() * 0.06);
                    
                    gc.setFill(coralColors[textureRandom.nextInt(coralColors.length)].deriveColor(0, 1, 1, 0.5));
                    gc.fillOval(cx, cy, coralSize, coralSize * 0.7);
                }
            }
        }
        
        // Fish shadows - REDUCED: 5% chance (was 12%), max 1 (was 1-2)
        if ((terrain == TerrainType.OCEAN || terrain == TerrainType.SHALLOW_WATER) 
            && textureRandom.nextDouble() < 0.05) {
            // Only 1 fish per tile now (was 1-2)
            double offsetX = textureRandom.nextDouble() * size;
            double offsetY = textureRandom.nextDouble() * size * 0.4;
            double fishPhase = waterAnimPhase * 15 + offsetX;
            double fishX = screenX + (fishPhase % (size * 1.4)) - size * 0.2;
            double fishY = screenY + size * 0.3 + offsetY;
            double fishSize = size * (0.1 + textureRandom.nextDouble() * 0.06);
            
            gc.setFill(Color.web("#001530").deriveColor(0, 1, 1, 0.2));
            // Fish body (ellipse)
            gc.fillOval(fishX, fishY, fishSize, fishSize * 0.4);
            // Tail
            double[] tailX = {fishX, fishX - fishSize * 0.35, fishX - fishSize * 0.35};
            double[] tailY = {fishY + fishSize * 0.2, fishY, fishY + fishSize * 0.4};
            gc.fillPolygon(tailX, tailY, 3);
        }
    }
    
    /**
     * Gets the 3x3 grid of neighboring terrain types.
     * Returns array[x][y] where [1][1] is the center tile.
     */
    public static TerrainType[][] getNeighbors(TerrainType[][] terrainMap, int x, int y) {
        TerrainType[][] neighbors = new TerrainType[3][3];
        int width = terrainMap.length;
        int height = terrainMap[0].length;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    neighbors[dx + 1][dy + 1] = terrainMap[nx][ny];
                }
            }
        }
        
        return neighbors;
    }
}
