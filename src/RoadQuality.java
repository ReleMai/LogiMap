import javafx.scene.paint.Color;

public enum RoadQuality {
    ASPHALT("Asphalt Road", Color.web("#3a3a3a"), 2.0),
    PAVED("Paved Road", Color.web("#5a5a5a"), 2.5),
    GRAVEL("Gravel Road", Color.web("#8b7355"), 3.0),
    DIRT("Dirt Road", Color.web("#6b5d4f"), 3.5);
    
    private final String name;
    private final Color color;
    private final double width;
    
    RoadQuality(String name, Color color, double width) {
        this.name = name;
        this.color = color;
        this.width = width;
    }
    
    public String getName() {
        return name;
    }
    
    public Color getColor() {
        return color;
    }
    
    public double getWidth() {
        return width;
    }
}
