import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedural terrain generator using fractal noise algorithms.
 * This class is maintained for compatibility - see WorldGenerator for advanced features.
 * Generates realistic terrain with oceans, beaches, mountains, forests, and other biomes.
 */
public class TerrainGenerator {
    
    // Dimensions
    private final int width;
    private final int height;
    
    // Terrain data
    private TerrainType[][] terrain;
    private double[][] elevation;
    private double[][] moisture;
    private WaterType[][] waterType;
    private double[][] waterDepth;
    private boolean[][] snow;
    
    // Decorations and resource nodes (set from WorldGenerator)
    private List<TerrainDecoration> decorations = new ArrayList<>();
    private List<ResourceNode> resourceNodes = new ArrayList<>();
    
    // Generation parameters
    private final Random random;
    private static final double NOISE_SCALE = 0.03;
    
    // Elevation thresholds
    private static final double OCEAN_LEVEL = 0.35;
    private static final double BEACH_LEVEL = 0.38;
    private static final double HIGHLAND_LEVEL = 0.80;
    private static final double SNOW_LEVEL = 0.90;
    
    /**
     * Creates a new terrain generator with the specified dimensions and seed.
     */
    public TerrainGenerator(int width, int height, long seed) {
        this(width, height, seed, true);
    }
    
    /**
     * Creates a new terrain generator, optionally skipping generation.
     * Use generate=false when you'll copy terrain from WorldGenerator.
     */
    public TerrainGenerator(int width, int height, long seed, boolean generate) {
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
        this.terrain = new TerrainType[width][height];
        this.waterType = new WaterType[width][height];
        this.waterDepth = new double[width][height];
        this.elevation = new double[width][height];
        this.moisture = new double[width][height];
        this.snow = new boolean[width][height];
        
        if (generate) {
            generateTerrain();
        }
    }
    
    /**
     * Main terrain generation pipeline.
     */
    private void generateTerrain() {
        // Initialize data arrays
        elevation = generateFractalNoise(NOISE_SCALE);
        moisture = generateFractalNoise(NOISE_SCALE * 1.2);
        waterType = new WaterType[width][height];
        waterDepth = new double[width][height];
        snow = new boolean[width][height];
        
        // Generate coordinate warping for natural-looking terrain
        double[][] warpX = generateFractalNoise(NOISE_SCALE * 2.0);
        double[][] warpY = generateFractalNoise(NOISE_SCALE * 2.2);
        
        // Calculate center for continental distribution
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double maxDistance = Math.sqrt(centerX * centerX + centerY * centerY);
        
        // Assign terrain types based on elevation and moisture
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double distance = calculateDistance(x, y, centerX, centerY) / maxDistance;
                double continental = 1.0 - distance;
                
                // Calculate warped elevation
                double warpedElevation = calculateWarpedElevation(x, y, continental, warpX, warpY);
                double moist = moisture[x][y];
                
                // Assign terrain type
                terrain[x][y] = determineTerrainType(warpedElevation, moist, distance);
                snow[x][y] = shouldHaveSnow(terrain[x][y], warpedElevation);
            }
        }
        
        // Apply terrain refinements
        addInteriorLakes();
        applyCoastalCliffs();
        classifyWaterBodies();
        smoothTransitions();
    }
    
    /**
     * Generates fractal noise using multiple octaves.
     */
    private double[][] generateFractalNoise(double baseScale) {
        double[][] noise = new double[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double value = 0;
                double amplitude = 0.6;
                double frequency = 1.0;
                
                for (int octave = 0; octave < 4; octave++) {
                    double scale = baseScale * frequency;
                    double sample = Math.sin(x * scale) * Math.cos(y * scale) * 0.5
                                  + Math.sin((x + y) * scale * 0.7) * 0.3
                                  + Math.cos((x - y) * scale * 0.9) * 0.2;
                    value += ((sample + 1) / 2.0) * amplitude;
                    amplitude *= 0.5;
                    frequency *= 2.0;
                }
                
                noise[x][y] = clamp(value, 0, 1);
            }
        }
        
        return noise;
    }
    
    /**
     * Calculates distance between two points.
     */
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculates warped elevation for more natural terrain.
     */
    private double calculateWarpedElevation(int x, int y, double continental, 
                                            double[][] warpX, double[][] warpY) {
        double baseElevation = elevation[x][y] * 0.7 + continental * 0.6 - 0.25;
        double warpEffect = (warpX[x][y] - 0.5) * 0.08 + (warpY[x][y] - 0.5) * 0.08;
        return clamp(baseElevation + warpEffect, 0, 1);
    }
    
    /**
     * Determines terrain type based on elevation and moisture.
     */
    private TerrainType determineTerrainType(double elev, double moisture, double distanceFromCenter) {
        if (elev < OCEAN_LEVEL) {
            return TerrainType.OCEAN;  // Updated from WATER
        } else if (elev < BEACH_LEVEL) {
            return TerrainType.BEACH;  // Updated from SAND
        } else if (elev > HIGHLAND_LEVEL) {
            return moisture < 0.4 ? TerrainType.ROCKY_HILLS : TerrainType.MOUNTAIN;  // Updated from ROCK
        } else if (moisture > 0.7) {
            return distanceFromCenter < 0.6 ? TerrainType.FOREST : TerrainType.SWAMP;
        } else if (moisture > 0.45) {
            return TerrainType.GRASS;
        } else {
            return TerrainType.ROCKY_HILLS;  // Updated from ROCK
        }
    }
    
    /**
     * Checks if a tile should have snow.
     */
    private boolean shouldHaveSnow(TerrainType type, double elev) {
        return (type == TerrainType.MOUNTAIN || type.isMountainous()) && elev > SNOW_LEVEL;
    }
    
    /**
     * Adds interior lakes to the terrain.
     */
    private void addInteriorLakes() {
        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                if (!terrain[x][y].isWater() && 
                    elevation[x][y] < 0.45 && 
                    moisture[x][y] > 0.6 &&
                    random.nextDouble() < 0.02) {
                    createWaterBlob(x, y, 2 + random.nextInt(3));
                }
            }
        }
    }
    
    /**
     * Creates a circular water body at the specified location.
     */
    private void createWaterBlob(int centerX, int centerY, int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (isValidPosition(x, y)) {
                    int dx = x - centerX;
                    int dy = y - centerY;
                    if (dx * dx + dy * dy <= radius * radius) {
                        if (!terrain[x][y].isMountainous()) {
                            terrain[x][y] = TerrainType.SHALLOW_WATER;  // Updated from WATER
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Applies coastal cliffs where mountains meet water.
     */
    private void applyCoastalCliffs() {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (terrain[x][y] == TerrainType.MOUNTAIN && isTouchingWater(x, y)) {
                    convertBeachToRock(x, y);
                }
            }
        }
    }
    
    /**
     * Checks if a tile is touching water or beach.
     */
    private boolean isTouchingWater(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                TerrainType neighbor = terrain[x + dx][y + dy];
                if (neighbor.isWater() || neighbor == TerrainType.BEACH) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Converts beach tiles around a position to rock.
     */
    private void convertBeachToRock(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (terrain[x + dx][y + dy] == TerrainType.BEACH) {
                    terrain[x + dx][y + dy] = TerrainType.ROCKY_HILLS;  // Updated from ROCK
                }
            }
        }
    }
    
    /**
     * Classifies water bodies by type and depth.
     */
    private void classifyWaterBodies() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (terrain[x][y].isWater()) {
                    int coastDistance = calculateCoastDistance(x, y);
                    
                    if (isLake(x, y)) {
                        waterType[x][y] = WaterType.FRESH_LAKE;
                        waterDepth[x][y] = 0.4;
                    } else if (coastDistance > 10) {
                        waterType[x][y] = WaterType.OCEAN_DEEP;
                        waterDepth[x][y] = Math.min(1.0, coastDistance / 15.0);
                    } else if (coastDistance > 5) {
                        waterType[x][y] = WaterType.OCEAN;
                        waterDepth[x][y] = Math.min(1.0, coastDistance / 15.0);
                    } else {
                        waterType[x][y] = WaterType.OCEAN_SHALLOW;
                        waterDepth[x][y] = Math.min(1.0, coastDistance / 15.0);
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a water tile is a lake.
     */
    private boolean isLake(int x, int y) {
        return terrain[x][y].isWater() && 
               calculateCoastDistance(x, y) > 4 && 
               moisture[x][y] > 0.6;
    }
    
    /**
     * Calculates distance to nearest non-water tile.
     */
    private int calculateCoastDistance(int x, int y) {
        int maxRadius = Math.max(width, height);
        for (int r = 0; r < maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (isValidPosition(nx, ny) && !terrain[nx][ny].isWater()) {
                        return r;
                    }
                }
            }
        }
        return maxRadius;
    }
    
    /**
     * Smooths terrain transitions using majority neighbor voting.
     */
    private void smoothTransitions() {
        TerrainType[][] smoothed = new TerrainType[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                smoothed[x][y] = getMajorityNeighbor(x, y);
            }
        }
        
        terrain = smoothed;
    }
    
    /**
     * Gets the most common terrain type among neighbors.
     */
    private TerrainType getMajorityNeighbor(int x, int y) {
        int[] counts = new int[TerrainType.values().length];
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = Math.floorMod(x + dx, width);
                int ny = Math.floorMod(y + dy, height);
                counts[terrain[nx][ny].ordinal()]++;
            }
        }
        
        int maxIndex = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[maxIndex]) {
                maxIndex = i;
            }
        }
        
        return TerrainType.values()[maxIndex];
    }
    
    /**
     * Checks if a position is within bounds.
     */
    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * Clamps a value between min and max.
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    // ==================== Public Accessors ====================
    
    public TerrainType getTerrain(int x, int y) {
        return isValidPosition(x, y) ? terrain[x][y] : TerrainType.GRASS;
    }
    
    public TerrainType[][] getTerrainMap() {
        return terrain;
    }
    
    public WaterType getWaterType(int x, int y) {
        return isValidPosition(x, y) ? waterType[x][y] : null;
    }
    
    public double getWaterDepth(int x, int y) {
        return isValidPosition(x, y) ? waterDepth[x][y] : 0;
    }
    
    public double getElevation(int x, int y) {
        return isValidPosition(x, y) ? elevation[x][y] : 0;
    }
    
    public boolean isSnow(int x, int y) {
        return isValidPosition(x, y) && snow[x][y];
    }
    
    public List<TerrainDecoration> getDecorations() {
        return decorations;
    }
    
    public void setDecorations(List<TerrainDecoration> decorations) {
        this.decorations = decorations != null ? decorations : new ArrayList<>();
    }
    
    public List<ResourceNode> getResourceNodes() {
        return resourceNodes;
    }
    
    public void setResourceNodes(List<ResourceNode> nodes) {
        this.resourceNodes = nodes != null ? nodes : new ArrayList<>();
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
