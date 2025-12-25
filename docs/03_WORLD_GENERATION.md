# LogiMap Developer Guide - World Generation

## Table of Contents
1. [Overview](#overview)
2. [Terrain Generation](#terrain-generation)
3. [Town Placement](#town-placement)
4. [Resource Distribution](#resource-distribution)
5. [Road Network](#road-network)
6. [Code Examples](#code-examples)

---

## Overview

World generation in LogiMap creates:
1. **Terrain** - Mountains, forests, plains, water
2. **Towns** - Cities and villages
3. **Resources** - Mining nodes, lumber, farms
4. **Roads** - Connecting settlements

### Generation Flow
```
WorldGenerator.generate()
    │
    ├─→ TerrainGenerator.generate()
    │       ├─→ Generate heightmap (Perlin noise)
    │       ├─→ Generate moisture map
    │       ├─→ Assign terrain types
    │       └─→ Add decorations
    │
    ├─→ placeTowns()
    │       ├─→ Find suitable locations
    │       ├─→ Place major cities
    │       └─→ Place villages
    │
    ├─→ placeResources()
    │       ├─→ Mining nodes (mountains)
    │       ├─→ Lumber (forests)
    │       └─→ Farms (plains)
    │
    └─→ generateRoads()
            └─→ Connect all towns via A* pathfinding
```

---

## Terrain Generation

### TerrainType Enum
```java
public enum TerrainType {
    DEEP_WATER("#1a5276", 0.0),
    SHALLOW_WATER("#2980b9", 0.2),
    BEACH("#f4d03f", 0.3),
    GRASSLAND("#27ae60", 0.4),
    FOREST("#1e8449", 0.5),
    HILLS("#a04000", 0.7),
    MOUNTAIN("#6c7a89", 0.85),
    SNOW_PEAK("#ecf0f1", 0.95);
    
    private final String color;
    private final double minHeight;
    
    public Color getColor() {
        return Color.web(color);
    }
    
    public static TerrainType fromHeight(double height, double moisture) {
        // Height determines base terrain
        if (height < 0.2) return DEEP_WATER;
        if (height < 0.3) return SHALLOW_WATER;
        if (height < 0.35) return BEACH;
        if (height < 0.7) {
            // Moisture affects vegetation
            return moisture > 0.5 ? FOREST : GRASSLAND;
        }
        if (height < 0.85) return HILLS;
        if (height < 0.95) return MOUNTAIN;
        return SNOW_PEAK;
    }
}
```

### Perlin Noise Generation
```java
public class TerrainGenerator {
    private static final int OCTAVES = 6;
    private static final double PERSISTENCE = 0.5;
    private static final double SCALE = 0.02;
    
    private long seed;
    private PerlinNoise noise;
    
    public TerrainType[][] generate(int width, int height) {
        TerrainType[][] terrain = new TerrainType[width][height];
        double[][] heightMap = generateHeightMap(width, height);
        double[][] moistureMap = generateMoistureMap(width, height);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                terrain[x][y] = TerrainType.fromHeight(
                    heightMap[x][y], 
                    moistureMap[x][y]
                );
            }
        }
        
        return terrain;
    }
    
    private double[][] generateHeightMap(int width, int height) {
        double[][] map = new double[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double value = 0;
                double amplitude = 1;
                double frequency = SCALE;
                
                // Multiple octaves for natural-looking terrain
                for (int i = 0; i < OCTAVES; i++) {
                    value += noise.noise(x * frequency, y * frequency) * amplitude;
                    amplitude *= PERSISTENCE;
                    frequency *= 2;
                }
                
                // Normalize to 0-1
                map[x][y] = (value + 1) / 2;
            }
        }
        
        return map;
    }
}
```

### Island Mask (Optional)
```java
// Force edges to be water for island worlds
private void applyIslandMask(double[][] heightMap) {
    int width = heightMap.length;
    int height = heightMap[0].length;
    
    double centerX = width / 2.0;
    double centerY = height / 2.0;
    double maxDist = Math.min(centerX, centerY);
    
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            double distX = Math.abs(x - centerX) / centerX;
            double distY = Math.abs(y - centerY) / centerY;
            double dist = Math.max(distX, distY);
            
            // Fade to water at edges
            double falloff = 1 - Math.pow(dist, 2);
            heightMap[x][y] *= falloff;
        }
    }
}
```

---

## Town Placement

### Placement Rules
1. Must be on walkable terrain (grass, plains)
2. Minimum distance between towns
3. Cities prefer flat, central areas
4. Villages can be anywhere valid

```java
public class TownPlacer {
    private static final int MIN_TOWN_DISTANCE = 15;
    private static final int CITY_MIN_DISTANCE = 30;
    
    public List<Town> placeTowns(TerrainType[][] terrain, int numCities, int numVillages) {
        List<Town> towns = new ArrayList<>();
        
        // Place cities first (they need better locations)
        for (int i = 0; i < numCities; i++) {
            Point loc = findCityLocation(terrain, towns);
            if (loc != null) {
                Town city = new Town(loc.x, loc.y, true);
                city.setName(NameGenerator.generateTownName(true));
                towns.add(city);
            }
        }
        
        // Then place villages
        for (int i = 0; i < numVillages; i++) {
            Point loc = findVillageLocation(terrain, towns);
            if (loc != null) {
                Town village = new Town(loc.x, loc.y, false);
                village.setName(NameGenerator.generateTownName(false));
                towns.add(village);
            }
        }
        
        return towns;
    }
    
    private Point findCityLocation(TerrainType[][] terrain, List<Town> existing) {
        int width = terrain.length;
        int height = terrain[0].length;
        
        // Try random positions, pick best
        Point best = null;
        int bestScore = -1;
        
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            
            if (!isValidTownLocation(terrain, x, y)) continue;
            if (!hasMinDistance(existing, x, y, CITY_MIN_DISTANCE)) continue;
            
            int score = calculateLocationScore(terrain, x, y);
            if (score > bestScore) {
                bestScore = score;
                best = new Point(x, y);
            }
        }
        
        return best;
    }
    
    private int calculateLocationScore(TerrainType[][] terrain, int x, int y) {
        int score = 0;
        
        // Prefer flat areas
        int radius = 5;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (isGrassland(terrain, x + dx, y + dy)) {
                    score += 2;
                }
                if (isNearWater(terrain, x + dx, y + dy)) {
                    score += 1;  // Water access is valuable
                }
            }
        }
        
        return score;
    }
}
```

### Town Types
```java
public enum TownType {
    FISHING_VILLAGE("Fishing Village", "#4a90a4", true),
    FARMING_TOWN("Farming Town", "#7cb342", false),
    MINING_TOWN("Mining Town", "#8d6e63", false),
    TRADING_POST("Trading Post", "#ffa726", true),
    CASTLE_TOWN("Castle Town", "#5c6bc0", false),
    PORT_CITY("Port City", "#26a69a", true);
    
    // Town type affects:
    // - Available goods
    // - NPC professions
    // - Building styles
}
```

---

## Resource Distribution

### Resource Types and Terrain
```java
public class ResourcePlacer {
    
    // Resource placement rules by terrain
    private static final Map<TerrainType, List<ResourceType>> TERRAIN_RESOURCES = Map.of(
        TerrainType.MOUNTAIN, List.of(ResourceType.IRON, ResourceType.STONE, ResourceType.GOLD),
        TerrainType.HILLS, List.of(ResourceType.STONE, ResourceType.COPPER),
        TerrainType.FOREST, List.of(ResourceType.WOOD, ResourceType.HERBS),
        TerrainType.GRASSLAND, List.of(ResourceType.GRAIN, ResourceType.LIVESTOCK),
        TerrainType.SHALLOW_WATER, List.of(ResourceType.FISH)
    );
    
    public List<ResourceNode> placeResources(TerrainType[][] terrain, List<Town> towns) {
        List<ResourceNode> resources = new ArrayList<>();
        
        // Place resources near towns
        for (Town town : towns) {
            int radius = town.isMajor() ? 15 : 10;
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = town.getGridX() + dx;
                    int y = town.getGridY() + dy;
                    
                    if (!isInBounds(terrain, x, y)) continue;
                    
                    TerrainType type = terrain[x][y];
                    List<ResourceType> possible = TERRAIN_RESOURCES.get(type);
                    
                    if (possible != null && random.nextDouble() < 0.05) {
                        ResourceType resType = possible.get(random.nextInt(possible.size()));
                        resources.add(createResourceNode(resType, x, y));
                    }
                }
            }
        }
        
        return resources;
    }
}
```

### Resource Node Classes
```java
// Base class for all resource nodes
public abstract class ResourceNodeBase {
    protected double worldX, worldY;
    protected int currentAmount;
    protected int maxAmount;
    protected double respawnTimer;
    
    public abstract void render(GraphicsContext gc, double x, double y, double size);
    public abstract ResourceItem harvest();
}

// Specific implementations
public class LumberNode extends ResourceNodeBase {
    private WoodType woodType;  // Oak, Pine, Birch, etc.
}

public class QuarryNode extends ResourceNodeBase {
    private StoneType stoneType;  // Granite, Marble, etc.
}

public class OreNode extends ResourceNodeBase {
    private OreType oreType;  // Iron, Gold, Copper, etc.
}
```

---

## Road Network

### A* Pathfinding
```java
public class RoadNetwork {
    
    public List<Road> generateRoads(List<Town> towns, TerrainType[][] terrain) {
        List<Road> roads = new ArrayList<>();
        
        // Connect each town to nearest neighbors
        for (Town town : towns) {
            List<Town> nearest = findNearestTowns(town, towns, 3);
            
            for (Town neighbor : nearest) {
                if (!hasRoadBetween(roads, town, neighbor)) {
                    List<Point> path = findPath(town, neighbor, terrain);
                    if (path != null) {
                        roads.add(new Road(town, neighbor, path));
                    }
                }
            }
        }
        
        return roads;
    }
    
    private List<Point> findPath(Town start, Town end, TerrainType[][] terrain) {
        // A* pathfinding
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<Point> closed = new HashSet<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Double> gScore = new HashMap<>();
        
        Point startPos = new Point(start.getGridX(), start.getGridY());
        Point endPos = new Point(end.getGridX(), end.getGridY());
        
        open.add(new Node(startPos, 0, heuristic(startPos, endPos)));
        gScore.put(startPos, 0.0);
        
        while (!open.isEmpty()) {
            Node current = open.poll();
            
            if (current.pos.equals(endPos)) {
                return reconstructPath(cameFrom, current.pos);
            }
            
            closed.add(current.pos);
            
            for (Point neighbor : getNeighbors(current.pos, terrain)) {
                if (closed.contains(neighbor)) continue;
                
                double tentativeG = gScore.get(current.pos) + 
                                   getMoveCost(terrain, neighbor);
                
                if (!gScore.containsKey(neighbor) || tentativeG < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current.pos);
                    gScore.put(neighbor, tentativeG);
                    
                    double f = tentativeG + heuristic(neighbor, endPos);
                    open.add(new Node(neighbor, tentativeG, f));
                }
            }
        }
        
        return null;  // No path found
    }
    
    private double getMoveCost(TerrainType[][] terrain, Point p) {
        TerrainType type = terrain[p.x][p.y];
        
        return switch (type) {
            case DEEP_WATER, SHALLOW_WATER -> 999;  // Can't cross
            case MOUNTAIN, SNOW_PEAK -> 10;          // Very expensive
            case HILLS, FOREST -> 3;                 // Moderate
            case GRASSLAND, BEACH -> 1;              // Easy
            default -> 2;
        };
    }
}
```

### Road Rendering
```java
public class Road {
    private List<Point> path;
    private RoadQuality quality;  // Dirt, Cobblestone, Paved
    
    public void render(GraphicsContext gc, double offsetX, double offsetY, 
                       double tileSize) {
        gc.setStroke(quality.getColor());
        gc.setLineWidth(tileSize * quality.getWidth());
        gc.setLineCap(StrokeLineCap.ROUND);
        
        gc.beginPath();
        boolean first = true;
        
        for (Point p : path) {
            double screenX = p.x * tileSize + offsetX + tileSize / 2;
            double screenY = p.y * tileSize + offsetY + tileSize / 2;
            
            if (first) {
                gc.moveTo(screenX, screenY);
                first = false;
            } else {
                gc.lineTo(screenX, screenY);
            }
        }
        
        gc.stroke();
    }
}

public enum RoadQuality {
    DIRT("#8B7355", 0.3),
    COBBLESTONE("#696969", 0.4),
    PAVED("#4a4a4a", 0.5);
    
    // Better roads = faster travel
    public double getSpeedMultiplier() { ... }
}
```

---

## Code Examples

### Complete World Generation
```java
public class WorldGenerator {
    private long seed;
    private Random random;
    
    public DemoWorld generate(int width, int height, WorldSettings settings) {
        random = new Random(seed);
        
        // 1. Generate terrain
        TerrainGenerator terrainGen = new TerrainGenerator(seed);
        TerrainType[][] terrain = terrainGen.generate(width, height);
        
        // 2. Place towns
        TownPlacer townPlacer = new TownPlacer(random);
        List<Town> towns = townPlacer.placeTowns(
            terrain, 
            settings.numCities,
            settings.numVillages
        );
        
        // 3. Place resources
        ResourcePlacer resourcePlacer = new ResourcePlacer(random);
        List<ResourceNode> resources = resourcePlacer.placeResources(terrain, towns);
        
        // 4. Generate roads
        RoadNetwork roadNetwork = new RoadNetwork();
        List<Road> roads = roadNetwork.generateRoads(towns, terrain);
        
        // 5. Create world object
        DemoWorld world = new DemoWorld(width, height);
        world.setTerrain(terrain);
        world.setTowns(towns);
        world.setResources(resources);
        world.setRoads(roads);
        
        // 6. Initialize economy
        for (Town town : towns) {
            town.initializeEconomy(resources);
        }
        
        return world;
    }
}
```

### Using the World Generation Menu
```java
public class WorldGenMenu {
    private Slider sizeSlider;
    private Slider townSlider;
    private TextField seedField;
    
    public void onGenerateClicked() {
        WorldSettings settings = new WorldSettings();
        settings.width = (int) sizeSlider.getValue();
        settings.height = (int) sizeSlider.getValue();
        settings.numCities = (int) (townSlider.getValue() * 0.3);
        settings.numVillages = (int) (townSlider.getValue() * 0.7);
        settings.seed = parseSeed(seedField.getText());
        
        // Show loading screen
        loadingScreen.show("Generating world...");
        
        // Generate in background thread
        Task<DemoWorld> task = new Task<>() {
            protected DemoWorld call() {
                return worldGenerator.generate(settings);
            }
        };
        
        task.setOnSucceeded(e -> {
            loadingScreen.hide();
            startGame(task.getValue());
        });
        
        new Thread(task).start();
    }
}
```

---

## Tips for World Generation

### Seed Consistency
```java
// Always use seeded random for reproducible worlds
Random random = new Random(seed);
PerlinNoise noise = new PerlinNoise(seed);

// Same seed = same world every time
```

### Performance
```java
// Generate in chunks for large worlds
private void generateChunked(int chunkSize) {
    for (int cx = 0; cx < width / chunkSize; cx++) {
        for (int cy = 0; cy < height / chunkSize; cy++) {
            generateChunk(cx * chunkSize, cy * chunkSize, chunkSize);
            updateProgress((cx * chunkSize + cy) / totalChunks);
        }
    }
}
```

### Biome Blending
```java
// Smooth transitions between terrain types
private TerrainType getBlendedTerrain(int x, int y) {
    // Sample 3x3 area
    Map<TerrainType, Integer> counts = new HashMap<>();
    for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
            TerrainType t = getRawTerrain(x + dx, y + dy);
            counts.merge(t, 1, Integer::sum);
        }
    }
    
    // Return most common
    return counts.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(TerrainType.GRASSLAND);
}
```

---

## Next: [04_NPC_SYSTEM.md](04_NPC_SYSTEM.md)
