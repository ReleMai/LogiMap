import java.util.*;

/**
 * World Generation Engine
 * 
 * A clean, modular terrain generator designed for:
 * - Realistic terrain with proper land/water distribution
 * - Logistics-friendly maps (navigable terrain, strategic resources)
 * - Extensibility for user-created custom worlds
 * 
 * Generation uses layered value noise with configurable parameters.
 */
public class WorldGenerator {
    
    // ==================== WORLD CONFIGURATION ====================
    
    /** World configuration - can be customized for user-created worlds */
    public static class WorldConfig {
        // Dimensions
        public int width = 1000;
        public int height = 1000;
        public long seed = 12345L;
        
        // Land/Water balance (0.0 = all water, 1.0 = all land)
        public double landCoverage = 0.55;
        
        // Terrain features
        public double mountainFrequency = 0.4;
        public double hillFrequency = 0.5;
        public double forestDensity = 0.5;
        
        // Climate
        public double temperature = 0.5;  // 0=cold, 1=hot
        public double moisture = 0.5;     // 0=dry, 1=wet
        
        // Islands
        public boolean generateIslands = true;
        public double islandFrequency = 0.3;
        
        public WorldConfig() {}
        
        public WorldConfig(int width, int height, long seed) {
            this.width = width;
            this.height = height;
            this.seed = seed;
        }
    }
    
    // ==================== INTERNAL STATE ====================
    
    private final WorldConfig config;
    private final Random random;
    private final int width;
    private final int height;
    
    // Noise seeds for different layers
    private final long continentSeed;
    private final long detailSeed;
    private final long mountainSeed;
    private final long moistureSeed;
    private final long temperatureSeed;
    
    // Generated data
    private double[][] elevation;
    private double[][] moisture;
    private double[][] temperature;
    private TerrainType[][] terrain;
    private WaterType[][] waterType;
    private double[][] waterDepth;
    
    // Points of interest for logistics
    private List<PointOfInterest> pointsOfInterest;
    
    // Resource nodes and decorations
    private List<ResourceNode> resourceNodes;
    private List<TerrainDecoration> decorations;
    
    // ==================== CONSTRUCTORS ====================
    
    public WorldGenerator(int width, int height, long seed) {
        this(new WorldConfig(width, height, seed));
    }
    
    public WorldGenerator(WorldConfig config) {
        this.config = config;
        this.width = config.width;
        this.height = config.height;
        this.random = new Random(config.seed);
        
        // Generate unique seeds for each noise layer
        this.continentSeed = config.seed;
        this.detailSeed = config.seed * 2 + 1;
        this.mountainSeed = config.seed * 3 + 2;
        this.moistureSeed = config.seed * 5 + 3;
        this.temperatureSeed = config.seed * 7 + 4;
        
        this.pointsOfInterest = new ArrayList<>();
        this.resourceNodes = new ArrayList<>();
        this.decorations = new ArrayList<>();
        
        generate();
    }
    
    // ==================== MAIN GENERATION ====================
    
    private void generate() {
        System.out.println("WorldGenerator: Starting generation...");
        System.out.flush();
        
        System.out.println("  - Generating elevation...");
        System.out.flush();
        generateElevation();
        System.out.println("  - Generating climate...");
        System.out.flush();
        generateClimate();
        System.out.println("  - Assigning terrain...");
        System.out.flush();
        assignTerrain();
        System.out.println("  - Classifying water...");
        System.out.flush();
        classifyWater();
        System.out.println("  - Generating points of interest...");
        System.out.flush();
        generatePointsOfInterest();
        System.out.println("  - Generating resource nodes...");
        System.out.flush();
        generateResourceNodes();
        System.out.println("  - Generating terrain decorations...");
        System.out.flush();
        generateTerrainDecorations();
        
        System.out.println("WorldGenerator: Generation complete!");
        System.out.flush();
    }
    
    // ==================== ELEVATION GENERATION ====================
    
    private void generateElevation() {
        elevation = new double[width][height];
        
        // Sea level based on land coverage
        double seaLevel = 1.0 - config.landCoverage;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Continental shapes (large scale)
                double continental = octaveNoise(x, y, continentSeed, 0.002, 4, 0.5);
                
                // Regional variation (medium scale)
                double regional = octaveNoise(x, y, detailSeed, 0.008, 3, 0.5);
                
                // Local detail (small scale)
                double detail = octaveNoise(x, y, detailSeed + 100, 0.025, 2, 0.5);
                
                // Combine layers
                double base = continental * 0.6 + regional * 0.3 + detail * 0.1;
                
                // Islands
                if (config.generateIslands) {
                    double islandNoise = octaveNoise(x, y, detailSeed + 200, 0.015, 2, 0.5);
                    if (islandNoise > 0.65 && base < seaLevel && base > seaLevel - 0.15) {
                        base += (islandNoise - 0.65) * config.islandFrequency * 0.8;
                    }
                }
                
                // Mountains on high ground
                if (base > seaLevel + 0.1) {
                    double mountainNoise = octaveNoise(x, y, mountainSeed, 0.012, 4, 0.6);
                    double ridgeNoise = ridgedNoise(x, y, mountainSeed + 50, 0.008, 3);
                    double mountainFactor = (base - seaLevel) * mountainNoise * ridgeNoise;
                    base += mountainFactor * config.mountainFrequency * 0.4;
                }
                
                // Hills in mid elevations
                if (base > seaLevel && base < seaLevel + 0.3) {
                    double hillNoise = octaveNoise(x, y, detailSeed + 300, 0.02, 2, 0.5);
                    base += hillNoise * config.hillFrequency * 0.08;
                }
                
                elevation[x][y] = clamp(base, 0, 1);
            }
        }
        
        smoothArray(elevation, 1);
    }
    
    // ==================== CLIMATE GENERATION ====================
    
    private void generateClimate() {
        moisture = new double[width][height];
        temperature = new double[width][height];
        double seaLevel = 1.0 - config.landCoverage;
        
        // Pre-compute water proximity using distance transform (much faster than per-pixel radius check)
        double[][] waterProximity = computeWaterProximity(seaLevel);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Temperature: latitude + noise + altitude
                double latitude = (double) y / height;
                double latTemp = 1.0 - Math.abs(latitude - 0.5) * 2;
                double tempNoise = octaveNoise(x, y, temperatureSeed, 0.005, 2, 0.5);
                double altitudeEffect = elevation[x][y] > 0.5 ? (elevation[x][y] - 0.5) * 0.6 : 0;
                
                temperature[x][y] = clamp(
                    latTemp * 0.5 + tempNoise * 0.3 + config.temperature * 0.2 - altitudeEffect,
                    0, 1
                );
                
                // Moisture: noise + water proximity + config
                double moistNoise = octaveNoise(x, y, moistureSeed, 0.006, 3, 0.5);
                
                moisture[x][y] = clamp(
                    moistNoise * 0.5 + waterProximity[x][y] * 0.3 + config.moisture * 0.2,
                    0, 1
                );
            }
        }
    }
    
    /** Efficient water proximity computation using distance transform */
    private double[][] computeWaterProximity(double seaLevel) {
        double[][] proximity = new double[width][height];
        int maxDist = 20;
        
        // Initialize: 0 for water, maxDist for land
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                proximity[x][y] = elevation[x][y] < seaLevel ? 0 : maxDist;
            }
        }
        
        // Forward pass
        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                proximity[x][y] = Math.min(proximity[x][y], 
                    Math.min(proximity[x-1][y] + 1, proximity[x][y-1] + 1));
            }
        }
        
        // Backward pass
        for (int x = width - 2; x >= 0; x--) {
            for (int y = height - 2; y >= 0; y--) {
                proximity[x][y] = Math.min(proximity[x][y],
                    Math.min(proximity[x+1][y] + 1, proximity[x][y+1] + 1));
            }
        }
        
        // Normalize to 0-1 range (inverted: closer to water = higher value)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                proximity[x][y] = 1.0 - Math.min(proximity[x][y] / maxDist, 1.0);
            }
        }
        
        return proximity;
    }
    
    // ==================== TERRAIN ASSIGNMENT ====================
    
    private void assignTerrain() {
        terrain = new TerrainType[width][height];
        double seaLevel = 1.0 - config.landCoverage;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double elev = elevation[x][y];
                double temp = temperature[x][y];
                double moist = moisture[x][y];
                
                terrain[x][y] = determineTerrainType(elev, temp, moist, seaLevel);
            }
        }
        
        addTerrainVariety();
    }
    
    private TerrainType determineTerrainType(double elev, double temp, double moist, double seaLevel) {
        // Water
        if (elev < seaLevel - 0.15) return TerrainType.DEEP_OCEAN;
        if (elev < seaLevel - 0.05) return TerrainType.OCEAN;
        if (elev < seaLevel) return TerrainType.SHALLOW_WATER;
        
        // Coastal
        if (elev < seaLevel + 0.02) {
            if (moist > 0.7) return TerrainType.MARSH;
            return TerrainType.BEACH;
        }
        
        // High elevation
        if (elev > 0.85) {
            if (temp < 0.3) return TerrainType.GLACIER;
            return TerrainType.MOUNTAIN_PEAK;
        }
        if (elev > 0.72) {
            if (temp < 0.25) return TerrainType.SNOW;
            return TerrainType.MOUNTAIN;
        }
        if (elev > 0.6) {
            if (temp < 0.3) return TerrainType.TUNDRA;
            return TerrainType.ROCKY_HILLS;
        }
        if (elev > 0.52) return TerrainType.HILLS;
        
        // Climate-based lowlands
        if (temp < 0.2) {
            return moist > 0.4 ? TerrainType.TUNDRA : TerrainType.ICE;
        }
        if (temp < 0.35) {
            return moist > 0.5 ? TerrainType.TAIGA : TerrainType.TUNDRA;
        }
        if (temp < 0.55) {
            if (moist > 0.7) return TerrainType.DENSE_FOREST;
            if (moist > 0.5) return TerrainType.FOREST;
            if (moist > 0.3) return TerrainType.GRASS;
            return TerrainType.SCRUBLAND;
        }
        if (temp < 0.75) {
            if (moist > 0.75) return TerrainType.SWAMP;
            if (moist > 0.55) return TerrainType.FOREST;
            if (moist > 0.35) return TerrainType.SAVANNA;
            if (moist > 0.2) return TerrainType.SCRUBLAND;
            return TerrainType.DESERT;
        }
        
        // Hot regions
        if (moist > 0.8) return TerrainType.JUNGLE;
        if (moist > 0.6) return TerrainType.DENSE_FOREST;
        if (moist > 0.4) return TerrainType.SAVANNA;
        if (moist > 0.2) return TerrainType.DUNES;
        return TerrainType.DESERT;
    }
    
    private void addTerrainVariety() {
        Random varietyRandom = new Random(config.seed + 999);
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (terrain[x][y] == TerrainType.GRASS && varietyRandom.nextDouble() < 0.08) {
                    terrain[x][y] = moisture[x][y] > 0.5 ? TerrainType.MEADOW : TerrainType.PLAINS;
                }
            }
        }
    }
    
    // ==================== WATER CLASSIFICATION ====================
    
    private void classifyWater() {
        waterType = new WaterType[width][height];
        waterDepth = new double[width][height];
        double seaLevel = 1.0 - config.landCoverage;
        
        // First pass: identify lakes vs ocean using efficient connected component labeling
        int[][] waterLabels = new int[width][height];
        boolean[] touchesEdge = new boolean[width * height + 1]; // label -> touches edge
        int currentLabel = 0;
        
        // Label all water bodies
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (terrain[x][y].isWater() && waterLabels[x][y] == 0) {
                    currentLabel++;
                    boolean isOcean = floodFillLabel(x, y, currentLabel, waterLabels);
                    touchesEdge[currentLabel] = isOcean;
                }
            }
        }
        
        // Second pass: assign water types based on labels
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (terrain[x][y].isWater()) {
                    double elev = elevation[x][y];
                    double depth = (seaLevel - elev) / seaLevel;
                    int label = waterLabels[x][y];
                    boolean isLake = label > 0 && !touchesEdge[label];
                    
                    if (isLake) {
                        waterType[x][y] = WaterType.FRESH_LAKE;
                        waterDepth[x][y] = 0.3;
                    } else if (depth > 0.4) {
                        waterType[x][y] = WaterType.OCEAN_DEEP;
                        waterDepth[x][y] = 1.0;
                    } else if (depth > 0.15) {
                        waterType[x][y] = WaterType.OCEAN;
                        waterDepth[x][y] = 0.6;
                    } else {
                        waterType[x][y] = WaterType.OCEAN_SHALLOW;
                        waterDepth[x][y] = 0.2;
                    }
                }
            }
        }
    }
    
    /** Flood fill to label a water body. Returns true if it touches the edge (ocean). */
    private boolean floodFillLabel(int startX, int startY, int label, int[][] labels) {
        boolean touchesEdge = false;
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        labels[startX][startY] = label;
        
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0];
            int y = pos[1];
            
            // Check if this pixel is on the edge
            if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                touchesEdge = true;
            }
            
            // Check 4 neighbors
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (terrain[nx][ny].isWater() && labels[nx][ny] == 0) {
                        labels[nx][ny] = label;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
        
        return touchesEdge;
    }
    
    // ==================== POINTS OF INTEREST ====================
    
    private void generatePointsOfInterest() {
        pointsOfInterest.clear();
        findMountainPeaks();
        findCoastalHarbors();
        findResourceAreas();
    }
    
    private void findMountainPeaks() {
        double maxElev = 0;
        int peakX = 0, peakY = 0;
        
        for (int x = 10; x < width - 10; x++) {
            for (int y = 10; y < height - 10; y++) {
                if (elevation[x][y] > maxElev) {
                    maxElev = elevation[x][y];
                    peakX = x;
                    peakY = y;
                }
            }
        }
        
        if (maxElev > 0.8) {
            pointsOfInterest.add(new PointOfInterest(peakX, peakY, "Highest Peak", PoiType.LANDMARK));
        }
    }
    
    private void findCoastalHarbors() {
        int step = width / 20;
        for (int x = step; x < width - step; x += step) {
            for (int y = step; y < height - step; y += step) {
                if (terrain[x][y] == TerrainType.BEACH || terrain[x][y] == TerrainType.GRASS) {
                    if (hasDeepWaterNearby(x, y)) {
                        pointsOfInterest.add(new PointOfInterest(x, y, "Harbor Site", PoiType.STRATEGIC));
                    }
                }
            }
        }
    }
    
    private boolean hasDeepWaterNearby(int x, int y) {
        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (terrain[nx][ny] == TerrainType.OCEAN || terrain[nx][ny] == TerrainType.DEEP_OCEAN) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void findResourceAreas() {
        int step = width / 15;
        for (int x = step; x < width - step; x += step) {
            for (int y = step; y < height - step; y += step) {
                TerrainType t = terrain[x][y];
                if (t == TerrainType.DENSE_FOREST || t == TerrainType.FOREST) {
                    pointsOfInterest.add(new PointOfInterest(x, y, "Forest Resources", PoiType.RESOURCE));
                } else if (t == TerrainType.MOUNTAIN || t == TerrainType.ROCKY_HILLS) {
                    pointsOfInterest.add(new PointOfInterest(x, y, "Mining Site", PoiType.RESOURCE));
                }
            }
        }
    }
    
    // ==================== NOISE FUNCTIONS ====================
    
    private double octaveNoise(int x, int y, long noiseSeed, double scale, int octaves, double persistence) {
        double value = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            value += noiseValue(x * scale * frequency, y * scale * frequency, noiseSeed + i * 1000) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return (value / maxValue + 1) / 2;
    }
    
    private double ridgedNoise(int x, int y, long noiseSeed, double scale, int octaves) {
        double value = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            double n = noiseValue(x * scale * frequency, y * scale * frequency, noiseSeed + i * 1000);
            n = 1.0 - Math.abs(n);
            n = n * n;
            value += n * amplitude;
            maxValue += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }
        
        return value / maxValue;
    }
    
    private double noiseValue(double x, double y, long noiseSeed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        double fx = x - ix;
        double fy = y - iy;
        
        double u = smoothstep(fx);
        double v = smoothstep(fy);
        
        double n00 = hash(ix, iy, noiseSeed);
        double n10 = hash(ix + 1, iy, noiseSeed);
        double n01 = hash(ix, iy + 1, noiseSeed);
        double n11 = hash(ix + 1, iy + 1, noiseSeed);
        
        double nx0 = lerp(n00, n10, u);
        double nx1 = lerp(n01, n11, u);
        return lerp(nx0, nx1, v);
    }
    
    private double hash(int x, int y, long seed) {
        long h = seed;
        h ^= x * 374761393L;
        h ^= y * 668265263L;
        h = (h ^ (h >> 13)) * 1274126177L;
        return ((h & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL) * 2 - 1;
    }
    
    // ==================== UTILITY FUNCTIONS ====================
    
    private double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }
    
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private void smoothArray(double[][] arr, int iterations) {
        for (int iter = 0; iter < iterations; iter++) {
            double[][] smoothed = new double[width][height];
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    double sum = arr[x][y] * 4;
                    sum += arr[x - 1][y] + arr[x + 1][y] + arr[x][y - 1] + arr[x][y + 1];
                    smoothed[x][y] = sum / 8.0;
                }
            }
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    arr[x][y] = smoothed[x][y];
                }
            }
        }
    }
    
    // ==================== RESOURCE NODE GENERATION ====================
    
    private void generateResourceNodes() {
        resourceNodes.clear();
        Random nodeRand = new Random(config.seed + 7777);
        
        // Generate grain fields on plains/meadow/grass
        int grainNodeCount = (width * height) / 8000; // Roughly 1 node per 8000 tiles
        for (int i = 0; i < grainNodeCount; i++) {
            int attempts = 0;
            while (attempts < 50) {
                int x = nodeRand.nextInt(width - 40) + 20;
                int y = nodeRand.nextInt(height - 40) + 20;
                TerrainType t = terrain[x][y];
                
                if (ResourceNode.Type.GRAIN.canSpawnOn(t)) {
                    // Check not too close to other nodes
                    boolean tooClose = false;
                    for (ResourceNode existing : resourceNodes) {
                        int dx = existing.getCenterX() - x;
                        int dy = existing.getCenterY() - y;
                        if (dx * dx + dy * dy < 2500) { // 50 tile min distance
                            tooClose = true;
                            break;
                        }
                    }
                    
                    if (!tooClose) {
                        int radius = 8 + nodeRand.nextInt(12); // 8-20 tile radius
                        GrainType grainType = GrainType.randomWeighted(nodeRand);
                        resourceNodes.add(new ResourceNode(x, y, radius, ResourceNode.Type.GRAIN, 
                                                          grainType, config.seed + i * 100));
                        break;
                    }
                }
                attempts++;
            }
        }
    }
    
    // ==================== TERRAIN DECORATION GENERATION ====================
    
    private void generateTerrainDecorations() {
        decorations.clear();
        Random decRand = new Random(config.seed + 8888);
        
        // Decoration density varies by biome
        int step = 3; // Check every 3rd tile for performance
        
        for (int x = step; x < width - step; x += step) {
            for (int y = step; y < height - step; y += step) {
                TerrainType t = terrain[x][y];
                if (t.isWater()) continue;
                
                // Skip if inside a resource node
                boolean inNode = false;
                for (ResourceNode node : resourceNodes) {
                    if (node.contains(x, y)) {
                        inNode = true;
                        break;
                    }
                }
                if (inNode) continue;
                
                // Determine decoration chance by terrain
                double chance = getDecorationChance(t);
                
                if (decRand.nextDouble() < chance) {
                    // Choose category based on biome
                    TerrainDecoration.Category cat = chooseDecorationCategory(t, decRand);
                    if (cat != null) {
                        int variant = decRand.nextInt(10);
                        // Increased base size, especially for trees
                        double baseSize = cat == TerrainDecoration.Category.TREE ? 0.5 : 0.3;
                        double sizeRange = cat == TerrainDecoration.Category.TREE ? 0.7 : 0.5;
                        double size = baseSize + decRand.nextDouble() * sizeRange;
                        double rotation = decRand.nextDouble() * 20 - 10;
                        
                        // Add small random offset
                        double ox = decRand.nextDouble() * step - step / 2.0;
                        double oy = decRand.nextDouble() * step - step / 2.0;
                        
                        decorations.add(new TerrainDecoration(
                            (int)(x + ox), (int)(y + oy), cat, variant, size, rotation
                        ));
                    }
                }
            }
        }
    }
    
    private double getDecorationChance(TerrainType t) {
        switch (t) {
            case FOREST:
            case DENSE_FOREST:
            case JUNGLE:
                return 0.65; // Increased - Dense decorations
            case TAIGA:
                return 0.55; // Increased
            case GRASS:
            case MEADOW:
            case PLAINS:
                return 0.35; // Increased
            case SAVANNA:
            case SCRUBLAND:
                return 0.22; // Increased
            case HILLS:
            case ROCKY_HILLS:
                return 0.30; // Increased - more rocks on hills
            case MOUNTAIN:
                return 0.18; // Increased
            case DESERT:
            case DUNES:
                return 0.15; // Increased - more rocks/cacti
            case BEACH:
                return 0.12; // Increased - shells, driftwood
            default:
                return 0.15;
        }
    }
    
    private TerrainDecoration.Category chooseDecorationCategory(TerrainType t, Random rand) {
        // Weight categories by terrain type
        switch (t) {
            case FOREST:
            case DENSE_FOREST:
            case TAIGA:
            case JUNGLE:
                // Mostly trees, some grass/rocks
                double r = rand.nextDouble();
                if (r < 0.65) return TerrainDecoration.Category.TREE;
                if (r < 0.85) return TerrainDecoration.Category.GRASS;
                return TerrainDecoration.Category.ROCK;
                
            case GRASS:
            case MEADOW:
            case PLAINS:
                // Mostly grass, few trees/rocks
                r = rand.nextDouble();
                if (r < 0.7) return TerrainDecoration.Category.GRASS;
                if (r < 0.85) return TerrainDecoration.Category.TREE;
                return TerrainDecoration.Category.ROCK;
                
            case SAVANNA:
            case SCRUBLAND:
                r = rand.nextDouble();
                if (r < 0.5) return TerrainDecoration.Category.GRASS;
                if (r < 0.75) return TerrainDecoration.Category.TREE;
                return TerrainDecoration.Category.ROCK;
                
            case HILLS:
            case ROCKY_HILLS:
            case MOUNTAIN:
                r = rand.nextDouble();
                if (r < 0.6) return TerrainDecoration.Category.ROCK;
                if (r < 0.85) return TerrainDecoration.Category.GRASS;
                return null;
                
            case DESERT:
            case DUNES:
                r = rand.nextDouble();
                if (r < 0.7) return TerrainDecoration.Category.ROCK;
                return TerrainDecoration.Category.GRASS;
                
            case BEACH:
                r = rand.nextDouble();
                if (r < 0.6) return TerrainDecoration.Category.ROCK;
                return TerrainDecoration.Category.GRASS;
                
            default:
                return TerrainDecoration.Category.GRASS;
        }
    }
    
    // ==================== PUBLIC GETTERS ====================
    
    public TerrainType[][] getTerrain() { return terrain; }
    public double[][] getElevation() { return elevation; }
    public WaterType[][] getWaterType() { return waterType; }
    public double[][] getWaterDepth() { return waterDepth; }
    public double[][] getMoisture() { return moisture; }
    public double[][] getTemperature() { return temperature; }
    public List<PointOfInterest> getPointsOfInterest() { return pointsOfInterest; }
    public List<ResourceNode> getResourceNodes() { return resourceNodes; }
    public List<TerrainDecoration> getDecorations() { return decorations; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public WorldConfig getConfig() { return config; }
    
    // Single-point accessors (for DemoWorld compatibility)
    public TerrainType getTerrain(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return terrain[x][y];
    }
    
    public double getElevation(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return elevation[x][y];
    }
    
    public double getMoisture(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return moisture[x][y];
    }
    
    public double getTemperature(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return temperature[x][y];
    }
    
    public double getMineralRichness(int x, int y) {
        // Simple mineral richness based on elevation (mountains have more minerals)
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        double elev = elevation[x][y];
        if (elev > 0.7) return 0.8;
        if (elev > 0.5) return 0.5;
        return 0.2;
    }
    
    public String getRegionName(int x, int y) {
        // Simple region naming based on location and terrain
        if (x < 0 || x >= width || y < 0 || y >= height) return "Unknown";
        
        String[] prefixes = {"North", "South", "East", "West", "Central"};
        String[] suffixes = {"lands", "reach", "vale", "march", "shire"};
        
        int regionX = x / (width / 3);
        int regionY = y / (height / 3);
        int idx = regionX + regionY * 3;
        
        Random nameRand = new Random(config.seed + idx);
        return prefixes[nameRand.nextInt(prefixes.length)] + " " + 
               suffixes[nameRand.nextInt(suffixes.length)];
    }
    
    // For terrain compatibility
    public TerrainType[][] getTerrainMap() { return terrain; }
    
    // ==================== CUSTOM WORLD SUPPORT ====================
    
    public static WorldGenerator fromHeightMap(double[][] heightMap, WorldConfig config) {
        WorldGenerator gen = new WorldGenerator(config);
        for (int x = 0; x < config.width && x < heightMap.length; x++) {
            for (int y = 0; y < config.height && y < heightMap[x].length; y++) {
                gen.elevation[x][y] = heightMap[x][y];
            }
        }
        gen.generateClimate();
        gen.assignTerrain();
        gen.classifyWater();
        gen.generatePointsOfInterest();
        return gen;
    }
    
    public WorldConfig exportConfig() {
        return config;
    }
}
