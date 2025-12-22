import java.util.*;

/**
 * DemoWorld - Creates a demonstration world using the revolutionary WorldGenerator.
 * 
 * Features:
 * - Procedurally generated terrain with tectonic simulation
 * - Climate-based biome distribution
 * - Intelligent structure placement based on terrain suitability
 * - Points of interest from natural landmarks
 */
public class DemoWorld {
    
    private static final int MAP_WIDTH = 1000;
    private static final int MAP_HEIGHT = 1000;
    
    // World generation components
    private WorldGenerator worldGen;
    private TerrainGenerator terrain;  // Kept for compatibility
    private RoadNetwork roadNetwork;
    private ResourceMap resourceMap;
    private String mapName;
    private List<MapStructure> structures;
    private long seed;
    private int startX, startY;
    
    // World metadata
    private List<PointOfInterest> pointsOfInterest;
    
    /**
     * Default constructor using default seed.
     */
    public DemoWorld() {
        this("Nightshade", 12345L, MAP_WIDTH / 2, MAP_HEIGHT / 2);
    }
    
    /**
     * Constructor with custom world configuration.
     * @param worldName Name of the world
     * @param seed Random seed for generation
     * @param startX Starting X position
     * @param startY Starting Y position
     */
    public DemoWorld(String worldName, long seed, int startX, int startY) {
        this.seed = seed;
        this.startX = startX;
        this.startY = startY;
        this.mapName = worldName;
        
        System.out.println("=== DemoWorld: Initializing World '" + worldName + "' with seed " + seed + " ===");
        
        // Use the new WorldGenerator for terrain
        this.worldGen = new WorldGenerator(MAP_WIDTH, MAP_HEIGHT, seed);
        
        // Create terrain wrapper for compatibility (skip generation, we'll copy data)
        this.terrain = new TerrainGenerator(MAP_WIDTH, MAP_HEIGHT, seed, false);
        
        // Copy terrain from WorldGenerator to TerrainGenerator for compatibility
        copyTerrainData();
        
        this.roadNetwork = new RoadNetwork(terrain.getTerrainMap(), MAP_WIDTH, MAP_HEIGHT, seed);
        this.resourceMap = new ResourceMap(terrain.getTerrainMap(), MAP_WIDTH, MAP_HEIGHT, seed);
        this.structures = new ArrayList<>();
        this.pointsOfInterest = worldGen.getPointsOfInterest();
        
        // Generate structures using intelligent placement
        generateDemoStructures();
        
        System.out.println("DemoWorld: Placed " + structures.size() + " structures");
        System.out.println("=== DemoWorld: World '" + mapName + "' ready! ===");
    }
    
    /**
     * Gets the starting X coordinate for the player.
     */
    public int getStartX() { return startX; }
    
    /**
     * Gets the starting Y coordinate for the player.
     */
    public int getStartY() { return startY; }
    
    /**
     * Gets the world seed.
     */
    public long getSeed() { return seed; }
    
    /**
     * Copies terrain data from WorldGenerator to TerrainGenerator for compatibility.
     */
    private void copyTerrainData() {
        TerrainType[][] terrainMap = terrain.getTerrainMap();
        TerrainType[][] worldTerrain = worldGen.getTerrainMap();
        
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                terrainMap[x][y] = worldTerrain[x][y];
            }
        }
    }
    
    /**
     * Generates demo structures with intelligent placement.
     * Uses terrain analysis to place towns in suitable locations.
     */
    private void generateDemoStructures() {
        Random rand = new Random(seed);
        
        // Find optimal locations for major towns using terrain analysis - more for larger map
        List<int[]> majorTownLocations = findOptimalTownLocations(8, true, rand);
        List<int[]> minorTownLocations = findOptimalTownLocations(20, false, rand);
        
        // Place major towns with terrain-appropriate names
        for (int i = 0; i < majorTownLocations.size(); i++) {
            int[] loc = majorTownLocations.get(i);
            String name = getTerrainAppropriateName(loc[0], loc[1], true);
            Town town = new Town(loc[0], loc[1], name, true);
            placeStructure(town);
        }
        
        // Place minor towns with terrain-appropriate names
        for (int i = 0; i < minorTownLocations.size(); i++) {
            int[] loc = minorTownLocations.get(i);
            String name = getTerrainAppropriateName(loc[0], loc[1], false);
            Town town = new Town(loc[0], loc[1], name, false);
            placeStructure(town);
        }
        
        System.out.println("DemoWorld: Placed " + structures.size() + " structures");
    }
    
    /**
     * Gets a terrain-appropriate name for a location.
     */
    private String getTerrainAppropriateName(int x, int y, boolean major) {
        boolean nearWater = distanceToWater(x, y) < 15;
        TerrainType t = worldGen.getTerrain(x, y);
        boolean isMountain = t != null && t.isMountainous();
        boolean isForest = t == TerrainType.FOREST || t == TerrainType.DENSE_FOREST;
        
        return NameGenerator.getTerrainAppropriateLocationName(nearWater, isMountain, isForest, x, y, seed);
    }
    
    /**
     * Finds optimal locations for towns based on terrain suitability.
     * Considers: flat land, proximity to water, not too close to other towns.
     */
    private List<int[]> findOptimalTownLocations(int count, boolean major, Random rand) {
        List<int[]> locations = new ArrayList<>();
        // Scale distances for larger maps
        int minDistance = major ? MAP_WIDTH / 8 : MAP_WIDTH / 20;
        
        // Score all potential locations - use larger step for big maps
        List<int[]> candidates = new ArrayList<>();
        int margin = MAP_WIDTH / 25;
        int step = Math.max(10, MAP_WIDTH / 100);
        
        for (int x = margin; x < MAP_WIDTH - margin; x += step) {
            for (int y = margin; y < MAP_HEIGHT - margin; y += step) {
                double score = scoreTownLocation(x, y, major);
                if (score > 0.5) {
                    candidates.add(new int[]{x, y, (int)(score * 1000)});
                }
            }
        }
        
        // Sort by score descending
        candidates.sort((a, b) -> b[2] - a[2]);
        
        // Select locations with minimum spacing
        for (int[] candidate : candidates) {
            if (locations.size() >= count) break;
            
            boolean tooClose = false;
            for (int[] existing : locations) {
                double dist = Math.sqrt(Math.pow(candidate[0] - existing[0], 2) + 
                                        Math.pow(candidate[1] - existing[1], 2));
                if (dist < minDistance) {
                    tooClose = true;
                    break;
                }
            }
            
            if (!tooClose) {
                locations.add(new int[]{candidate[0], candidate[1]});
            }
        }
        
        return locations;
    }
    
    /**
     * Scores a location for town placement suitability.
     */
    private double scoreTownLocation(int x, int y, boolean major) {
        // Get structure size to check all tiles
        int structSize = major ? 8 : 5;
        
        // Check ALL tiles the structure will occupy
        for (int dx = 0; dx < structSize; dx++) {
            for (int dy = 0; dy < structSize; dy++) {
                int checkX = x + dx;
                int checkY = y + dy;
                
                if (checkX >= MAP_WIDTH || checkY >= MAP_HEIGHT) return 0;
                
                TerrainType t = worldGen.getTerrain(checkX, checkY);
                if (t == null || t.isWater() || !t.isBuildable()) return 0;
            }
        }
        
        TerrainType centerTerrain = worldGen.getTerrain(x + structSize/2, y + structSize/2);
        if (centerTerrain == null) return 0;
        
        double score = 0.5;
        
        // Prefer flat, fertile land
        if (centerTerrain == TerrainType.GRASS || centerTerrain == TerrainType.PLAINS || centerTerrain == TerrainType.MEADOW) {
            score += 0.3;
        } else if (centerTerrain == TerrainType.FOREST || centerTerrain == TerrainType.SAVANNA) {
            score += 0.1;
        } else if (centerTerrain.isMountainous()) {
            score -= 0.3;
        }
        
        // Check proximity to water (good for trade)
        int waterDist = distanceToWater(x, y);
        if (waterDist < 30 && waterDist > 5) {
            score += 0.2; // Near water but not on coast
        }
        
        // Major towns prefer central locations
        if (major) {
            double centerDist = Math.sqrt(Math.pow(x - MAP_WIDTH/2.0, 2) + 
                                          Math.pow(y - MAP_HEIGHT/2.0, 2));
            double maxDist = Math.sqrt(MAP_WIDTH * MAP_WIDTH + MAP_HEIGHT * MAP_HEIGHT) / 2;
            score += (1 - centerDist / maxDist) * 0.2;
        }
        
        // Consider mineral richness
        score += worldGen.getMineralRichness(x, y) * 0.1;
        
        return score;
    }
    
    /**
     * Finds distance to nearest water.
     */
    private int distanceToWater(int x, int y) {
        for (int r = 1; r < 50; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < MAP_WIDTH && ny >= 0 && ny < MAP_HEIGHT) {
                        TerrainType t = worldGen.getTerrain(nx, ny);
                        if (t != null && t.isWater()) return r;
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }
    
    /**
     * Places a structure, ensuring it's on valid land and doesn't overlap others.
     */
    private void placeStructure(MapStructure s) {
        int gx = s.getGridX();
        int gy = s.getGridY();
        int size = s.getSize();
        
        // Check if ALL tiles are valid
        boolean allValid = true;
        for (int dx = 0; dx < size && allValid; dx++) {
            for (int dy = 0; dy < size && allValid; dy++) {
                int checkX = gx + dx;
                int checkY = gy + dy;
                if (checkX >= MAP_WIDTH || checkY >= MAP_HEIGHT) {
                    allValid = false;
                } else {
                    TerrainType t = worldGen.getTerrain(checkX, checkY);
                    if (t == null || t.isWater() || !t.isBuildable()) {
                        allValid = false;
                    }
                }
            }
        }
        
        // If not all valid, try to nudge to a fully valid location
        if (!allValid) {
            int[] p = nudgeToValidArea(gx, gy, size);
            if (p == null) {
                System.out.println("Could not place structure: " + s.getName());
                return; // Skip this structure entirely
            }
            s.gridX = p[0];
            s.gridY = p[1];
        }
        
        // Check for overlap with existing structures
        for (MapStructure existing : structures) {
            if (structuresOverlap(s, existing)) {
                System.out.println("Skipping overlapping structure: " + s.getName());
                return; // Skip this structure
            }
        }
        
        structures.add(s);
        roadNetwork.addNode(s);
    }
    
    /**
     * Checks if two structures overlap (including a buffer zone).
     */
    private boolean structuresOverlap(MapStructure a, MapStructure b) {
        int buffer = 2; // Minimum gap between structures
        int ax1 = a.getGridX() - buffer;
        int ay1 = a.getGridY() - buffer;
        int ax2 = a.getGridX() + a.getSize() + buffer;
        int ay2 = a.getGridY() + a.getSize() + buffer;
        
        int bx1 = b.getGridX();
        int by1 = b.getGridY();
        int bx2 = b.getGridX() + b.getSize();
        int by2 = b.getGridY() + b.getSize();
        
        return ax1 < bx2 && ax2 > bx1 && ay1 < by2 && ay2 > by1;
    }
    
    /**
     * Finds nearest valid location where ALL tiles are buildable.
     */
    private int[] nudgeToValidArea(int x, int y, int size) {
        java.util.Queue<int[]> q = new java.util.ArrayDeque<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        q.add(new int[]{x, y});
        visited.add(x + "," + y);
        
        int maxAttempts = 1000;
        int attempts = 0;
        
        while (!q.isEmpty() && attempts < maxAttempts) {
            attempts++;
            int[] c = q.poll();
            
            // Check if ALL tiles at this position are valid
            boolean allValid = true;
            for (int dx = 0; dx < size && allValid; dx++) {
                for (int dy = 0; dy < size && allValid; dy++) {
                    int checkX = c[0] + dx;
                    int checkY = c[1] + dy;
                    if (checkX >= MAP_WIDTH || checkY >= MAP_HEIGHT) {
                        allValid = false;
                    } else {
                        TerrainType t = worldGen.getTerrain(checkX, checkY);
                        if (t == null || t.isWater() || !t.isBuildable()) {
                            allValid = false;
                        }
                    }
                }
            }
            
            if (allValid) {
                return c;
            }
            
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}};
            for (int[] d : dirs) {
                int nx = c[0] + d[0] * 2, ny = c[1] + d[1] * 2;
                String key = nx + "," + ny;
                if (nx < 0 || ny < 0 || nx >= MAP_WIDTH - size || ny >= MAP_HEIGHT - size) continue;
                if (!visited.contains(key)) {
                    visited.add(key);
                    q.add(new int[]{nx, ny});
                }
            }
        }
        
        return null; // Could not find valid location
    }

    /**
     * Finds nearest buildable land from a given position.
     */
    private int[] nudgeToLand(int x, int y) {
        java.util.Queue<int[]> q = new java.util.ArrayDeque<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        q.add(new int[]{x, y});
        visited.add(x + "," + y);
        
        while (!q.isEmpty()) {
            int[] c = q.poll();
            TerrainType t = worldGen.getTerrain(c[0], c[1]);
            if (t != null && !t.isWater() && t.isBuildable()) {
                return c;
            }
            
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = c[0] + d[0], ny = c[1] + d[1];
                if (nx < 0 || ny < 0 || nx >= MAP_WIDTH || ny >= MAP_HEIGHT) continue;
                String k = nx + "," + ny;
                if (!visited.contains(k)) {
                    visited.add(k);
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return new int[]{x, y};
    }
    
    // ==================== PUBLIC ACCESSORS ====================
    
    public TerrainGenerator getTerrain() {
        return terrain;
    }
    
    public WorldGenerator getWorldGenerator() {
        return worldGen;
    }
    
    public RoadNetwork getRoadNetwork() {
        return roadNetwork;
    }
    
    public ResourceMap getResourceMap() {
        return resourceMap;
    }
    
    public List<MapStructure> getStructures() {
        return structures;
    }
    
    public int getMapWidth() {
        return MAP_WIDTH;
    }
    
    public int getMapHeight() {
        return MAP_HEIGHT;
    }
    
    public String getMapName() {
        return mapName;
    }
    
    public List<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }
    
    /**
     * Gets elevation at a world position.
     */
    public double getElevation(int x, int y) {
        return worldGen.getElevation(x, y);
    }
    
    /**
     * Gets temperature at a world position (0 = cold, 1 = hot).
     */
    public double getTemperature(int x, int y) {
        return worldGen.getTemperature(x, y);
    }
    
    /**
     * Gets moisture at a world position (0 = dry, 1 = wet).
     */
    public double getMoisture(int x, int y) {
        return worldGen.getMoisture(x, y);
    }
    
    /**
     * Gets region name at a world position.
     */
    public String getRegionName(int x, int y) {
        return worldGen.getRegionName(x, y);
    }
    
    public MapStructure getStructureAt(int gridX, int gridY) {
        // First pass: check if point is exactly within structure bounds (tight hitbox)
        for (MapStructure structure : structures) {
            int structX = structure.getGridX();
            int structY = structure.getGridY();
            int size = structure.getSize();
            
            // Check if within structure bounds with minimal padding (1 tile)
            int padding = 1;
            if (gridX >= structX - padding && gridX < structX + size + padding &&
                gridY >= structY - padding && gridY < structY + size + padding) {
                return structure;
            }
        }
        
        // Second pass: find nearest structure within a small range
        MapStructure nearest = null;
        double nearestDist = Double.MAX_VALUE;
        int maxSearchRadius = 2; // Reduced search radius for tighter hitboxes
        
        for (MapStructure structure : structures) {
            int centerX = structure.getGridX() + structure.getSize() / 2;
            int centerY = structure.getGridY() + structure.getSize() / 2;
            double dist = Math.sqrt(Math.pow(centerX - gridX, 2) + Math.pow(centerY - gridY, 2));
            
            // Only match if within tight radius of the structure
            double effectiveRadius = maxSearchRadius + (structure.getSize() / 2.0);
            if (dist < nearestDist && dist <= effectiveRadius) {
                nearestDist = dist;
                nearest = structure;
            }
        }
        
        return nearest;
    }
}
