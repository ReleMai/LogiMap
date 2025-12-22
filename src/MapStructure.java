import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all map structures (towns, quarries, camps, etc.).
 * Defines common properties such as position, name, size, and productivity.
 * Subclasses implement specific structure behavior and resource production.
 */
public abstract class MapStructure {
    
    protected Point2D position;
    protected String name;
    protected int gridX;
    protected int gridY;
    protected int size; // in grid cells
    protected Color color;
    protected String type;
    protected double population;
    protected double productivity;
    
    public MapStructure(int gridX, int gridY, String name, int size, Color color, String type) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.position = new Point2D(gridX, gridY);
        this.name = name;
        this.size = size;
        this.color = color;
        this.type = type;
        this.population = 0;
        this.productivity = 1.0;
    }
    
    public abstract String getInfo();
    
    public int getGridX() {
        return gridX;
    }
    
    public int getGridY() {
        return gridY;
    }
    
    public Point2D getPosition() {
        return position;
    }
    
    public String getName() {
        return name;
    }
    
    public int getSize() {
        return size;
    }
    
    public Color getColor() {
        return color;
    }
    
    public String getType() {
        return type;
    }
    
    public double getPopulation() {
        return population;
    }
    
    public void setPopulation(double pop) {
        this.population = pop;
    }
    
    public double getProductivity() {
        return productivity;
    }
    
    public void setProductivity(double prod) {
        this.productivity = prod;
    }
    
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
