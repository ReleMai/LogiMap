import javafx.scene.paint.Color;

public enum RoadQuality {
    ASPHALT("Asphalt Road", Color.web("#3a3a3a"), 3.0, 1.8),   // 80% faster
    PAVED("Paved Road", Color.web("#5a5a5a"), 3.5, 1.6),       // 60% faster  
    GRAVEL("Gravel Road", Color.web("#8b7355"), 4.0, 1.4),     // 40% faster
    DIRT("Dirt Road", Color.web("#6b5d4f"), 4.5, 1.2);         // 20% faster
    
    private final String name;
    private final Color color;
    private final double width;
    private final double speedMultiplier;
    
    RoadQuality(String name, Color color, double width, double speedMultiplier) {
        this.name = name;
        this.color = color;
        this.width = width;
        this.speedMultiplier = speedMultiplier;
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
    
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}
