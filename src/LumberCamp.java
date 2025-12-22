import javafx.scene.paint.Color;

/**
 * Represents a lumber camp that harvests wood resources.
 * Lumber camps are placed in forested areas and produce timber
 * for construction and trade purposes.
 */
public class LumberCamp extends MapStructure {
    
    private double outputPerDay;
    
    public LumberCamp(int gridX, int gridY, String name) {
        super(gridX, gridY, name, 4, Color.web("#6b5d3f"), "Lumber Camp");
        this.population = 600;
        this.outputPerDay = 80;
        this.productivity = 0.75;
    }
    
    public double getOutputPerDay() {
        return outputPerDay;
    }
    
    public void setOutputPerDay(double output) {
        this.outputPerDay = output;
    }
    
    @Override
    public String getInfo() {
        return String.format(
            "%s\n" +
            "Type: Lumber Camp\n" +
            "Output: %.1f logs/day\n" +
            "Workers: %,.0f\n" +
            "Efficiency: %.0f%%",
            name, outputPerDay, population, productivity * 100
        );
    }
}
