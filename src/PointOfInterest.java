/**
 * Point of Interest - Represents a notable location on the map.
 * Used for logistics planning and strategic locations.
 */
public class PointOfInterest {
    public final int x;
    public final int y;
    public final String name;
    public final PoiType type;
    
    public PointOfInterest(int x, int y, String name, PoiType type) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.type = type;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public String getName() { return name; }
    public PoiType getType() { return type; }
}
