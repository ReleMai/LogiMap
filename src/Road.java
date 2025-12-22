import javafx.geometry.Point2D;
import java.util.*;

/**
 * Represents a road connecting two map structures.
 * Roads are generated using A* pathfinding and follow terrain
 * constraints, preferring flat ground and avoiding water/mountains.
 * Road quality affects travel speed and trade efficiency.
 */
public class Road {
    
    private List<Point2D> path;
    private RoadQuality quality;
    private String name;
    private MapStructure startPoint;
    private MapStructure endPoint;
    private TerrainType[][] terrain;
    private int width;
    private int height;
    private Random random;
    
    public Road(String name, MapStructure start, MapStructure end, RoadQuality quality,
                TerrainType[][] terrain, int width, int height, Random random) {
        this.name = name;
        this.startPoint = start;
        this.endPoint = end;
        this.quality = quality;
        this.path = new ArrayList<>();
        this.terrain = terrain;
        this.width = width;
        this.height = height;
        this.random = random;
        
        generatePath(start.getGridX(), start.getGridY(), 
                    end.getGridX(), end.getGridY());
    }
    
    private void generatePath(int startX, int startY, int endX, int endY) {
        Point2D entryStart = pickEntryPoint(startPoint, endPoint);
        Point2D entryEnd = pickEntryPoint(endPoint, startPoint);

        List<Point2D> targets = createBentTargets(entryStart, entryEnd);
        Point2D current = entryStart;
        path.add(current);

        for (Point2D target : targets) {
            List<Point2D> segment = findPath(current, target);
            if (segment.isEmpty()) {
                break; // fail safe: keep existing partial path
            }
            // Avoid duplicating the starting node of the segment
            for (int i = 1; i < segment.size(); i++) {
                path.add(segment.get(i));
            }
            current = target;
        }
    }

    private Point2D pickEntryPoint(MapStructure from, MapStructure to) {
        int halfSize = Math.max(1, from.getSize() / 2);
        int dx = to.getGridX() - from.getGridX();
        int dy = to.getGridY() - from.getGridY();
        boolean goHorizontal = Math.abs(dx) >= Math.abs(dy);

        int x = from.getGridX();
        int y = from.getGridY();

        if (goHorizontal) {
            x += dx >= 0 ? halfSize : -halfSize;
            y += Math.max(-halfSize + 1, Math.min(halfSize - 1, random.nextInt(halfSize * 2 + 1) - halfSize));
        } else {
            y += dy >= 0 ? halfSize : -halfSize;
            x += Math.max(-halfSize + 1, Math.min(halfSize - 1, random.nextInt(halfSize * 2 + 1) - halfSize));
        }

        x = clamp(x, 1, width - 2);
        y = clamp(y, 1, height - 2);
        Point2D candidate = new Point2D(x, y);
        return nudgeToPassable(candidate);
    }

    private List<Point2D> createBentTargets(Point2D start, Point2D end) {
        List<Point2D> targets = new ArrayList<>();
        int dx = (int) (end.getX() - start.getX());
        int dy = (int) (end.getY() - start.getY());

        // Force at least one bend by inserting an intermediate waypoint
        boolean horizontalFirst = Math.abs(dx) >= Math.abs(dy);
        int offsetPrimary = Math.max(4, (Math.abs(horizontalFirst ? dx : dy) / 3));
        int offsetSecondary = Math.max(2, (Math.abs(horizontalFirst ? dy : dx) / 5));

        int bendX = (int) start.getX();
        int bendY = (int) start.getY();

        if (horizontalFirst) {
            bendX += Integer.signum(dx) * offsetPrimary;
            bendY += Integer.signum(dy == 0 ? 1 : dy) * offsetSecondary;
        } else {
            bendY += Integer.signum(dy) * offsetPrimary;
            bendX += Integer.signum(dx == 0 ? 1 : dx) * offsetSecondary;
        }

        bendX = clamp(bendX, 1, width - 2);
        bendY = clamp(bendY, 1, height - 2);
        Point2D bend = nudgeToPassable(new Point2D(bendX, bendY));
        targets.add(bend);
        targets.add(nudgeToPassable(end));
        return targets;
    }

    private List<Point2D> findPath(Point2D start, Point2D goal) {
        // A* on 4-neighbor grid; diagonals avoided to prevent corner touching
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<String, Node> allNodes = new HashMap<>();

        Node startNode = new Node((int) start.getX(), (int) start.getY(), -1);
        startNode.g = 0;
        startNode.f = heuristic(startNode.x, startNode.y, goal);
        open.add(startNode);
        allNodes.put(key(startNode.x, startNode.y), startNode);

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.x == (int) goal.getX() && current.y == (int) goal.getY()) {
                return reconstruct(current);
            }

            for (int dir = 0; dir < 4; dir++) {
                int nx = current.x + dx(dir);
                int ny = current.y + dy(dir);
                if (!inBounds(nx, ny) || !isPassable(nx, ny)) {
                    continue;
                }

                int turnPenalty = (current.dir == -1 || current.dir == dir) ? 0 : 3;
                int terrainCost = terrainCost(nx, ny);
                int newG = current.g + 10 + turnPenalty + terrainCost;

                String nKey = key(nx, ny);
                Node neighbor = allNodes.getOrDefault(nKey, new Node(nx, ny, dir));

                if (neighbor.g == Integer.MAX_VALUE || newG < neighbor.g) {
                    neighbor.g = newG;
                    neighbor.f = newG + heuristic(nx, ny, goal);
                    neighbor.prev = current;
                    neighbor.dir = dir;
                    allNodes.put(nKey, neighbor);
                    open.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Point2D> reconstruct(Node node) {
        LinkedList<Point2D> pts = new LinkedList<>();
        Node cur = node;
        while (cur != null) {
            pts.addFirst(new Point2D(cur.x, cur.y));
            cur = cur.prev;
        }
        return pts;
    }

    private int heuristic(int x, int y, Point2D goal) {
        return (Math.abs(x - (int) goal.getX()) + Math.abs(y - (int) goal.getY())) * 10;
    }

    private boolean inBounds(int x, int y) {
        return x >= 1 && x < width - 1 && y >= 1 && y < height - 1;
    }

    private boolean isPassable(int x, int y) {
        TerrainType t = terrain[x][y];
        return !t.isWater() && t != TerrainType.BEACH && t.isBuildable();
    }

    private int terrainCost(int x, int y) {
        TerrainType t = terrain[x][y];
        // Use the movement cost from terrain type
        return (int)(t.getMovementCost() * 4);
    }

    private Point2D nudgeToPassable(Point2D pt) {
        int x = (int) pt.getX();
        int y = (int) pt.getY();
        if (inBounds(x, y) && isPassable(x, y)) {
            return pt;
        }
        // small BFS search to find nearest passable cell
        Queue<Point2D> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(pt);
        visited.add(key(x, y));
        while (!queue.isEmpty()) {
            Point2D cur = queue.poll();
            int cx = (int) cur.getX();
            int cy = (int) cur.getY();
            if (inBounds(cx, cy) && isPassable(cx, cy)) {
                return cur;
            }
            for (int dir = 0; dir < 4; dir++) {
                int nx = cx + dx(dir);
                int ny = cy + dy(dir);
                String k = key(nx, ny);
                if (!visited.contains(k)) {
                    visited.add(k);
                    queue.add(new Point2D(nx, ny));
                }
            }
        }
        return pt;
    }

    private int dx(int dir) {
        return dir == 0 ? 1 : dir == 1 ? -1 : 0;
    }

    private int dy(int dir) {
        return dir == 2 ? 1 : dir == 3 ? -1 : 0;
    }

    private String key(int x, int y) {
        return x + "," + y;
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static class Node {
        int x;
        int y;
        int g;
        int f;
        int dir;
        Node prev;

        Node(int x, int y, int dir) {
            this.x = x;
            this.y = y;
            this.dir = dir;
            this.g = Integer.MAX_VALUE;
            this.f = Integer.MAX_VALUE;
        }
    }
    
    public void setQuality(RoadQuality quality) {
        this.quality = quality;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Point2D> getPath() {
        return path;
    }
    
    public RoadQuality getQuality() {
        return quality;
    }
    
    public MapStructure getStartPoint() {
        return startPoint;
    }
    
    public MapStructure getEndPoint() {
        return endPoint;
    }
    
    public double getLength() {
        return path.size();
    }
}
