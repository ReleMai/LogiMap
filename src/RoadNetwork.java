import javafx.geometry.Point2D;
import java.util.*;

/**
 * Manages the network of roads connecting structures on the map.
 * Handles road generation using intelligent pathfinding algorithms
 * and maintains the graph structure for logistics calculations.
 */
public class RoadNetwork {
    
    private final List<Road> roads;
    private final List<MapStructure> nodes;
    private final TerrainType[][] terrainMap;
    private final int width;
    private final int height;
    private final Random random;
    
    public RoadNetwork(TerrainType[][] terrainMap, int width, int height, long seed) {
        this.roads = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.terrainMap = terrainMap;
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
    }
    
    public void addNode(MapStructure structure) {
        if (!nodes.contains(structure)) {
            nodes.add(structure);
        }
    }
    
    public void addRoad(Road road) {
        roads.add(road);
    }
    
    public Road connectStructures(String roadName, MapStructure from, MapStructure to) {
        // Determine road quality based on proximity to major towns
        RoadQuality quality = determineRoadQuality(from, to);
        Road road = new Road(roadName, from, to, quality, terrainMap, width, height, random);
        roads.add(road);
        return road;
    }
    
    private RoadQuality determineRoadQuality(MapStructure from, MapStructure to) {
        // Check if either structure is a major town
        boolean fromMajor = isMajorTown(from);
        boolean toMajor = isMajorTown(to);
        
        if (fromMajor && toMajor) {
            // Major city to major city - pristine asphalt
            return RoadQuality.ASPHALT;
        } else if (fromMajor || toMajor) {
            // One major city - paved road
            return RoadQuality.PAVED;
        } else {
            // Remote locations - gravel or dirt
            double distance = from.getPosition().distance(to.getPosition());
            if (distance > 100) {
                return RoadQuality.DIRT;
            } else {
                return RoadQuality.GRAVEL;
            }
        }
    }
    
    private boolean isMajorTown(MapStructure structure) {
        if (structure instanceof Town town) {
            return town.isMajor();
        }
        return false;
    }
    
    public List<Road> getRoads() {
        return roads;
    }
    
    public List<MapStructure> getNodes() {
        return nodes;
    }
    
    public RoadQuality getRoadQualityAt(int gridX, int gridY) {
        for (Road road : roads) {
            for (Point2D point : road.getPath()) {
                if ((int)point.getX() == gridX && (int)point.getY() == gridY) {
                    return road.getQuality();
                }
            }
        }
        return null;
    }
}
