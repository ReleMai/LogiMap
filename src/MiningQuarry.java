import javafx.scene.paint.Color;

/**
 * Represents a mining quarry for extracting mineral resources.
 * Quarries can produce various resources such as coal, copper,
 * iron, and other minerals based on local terrain deposits.
 */
public class MiningQuarry extends MapStructure {
    
    private String resource; // "Coal", "Copper", "Iron", etc.
    private double outputPerDay;
    
    public MiningQuarry(int gridX, int gridY, String name, String resource) {
        super(gridX, gridY, name, 5, Color.web("#8b7355"), "Mining Quarry");
        this.resource = resource;
        this.population = 800;
        this.outputPerDay = 100;
        this.productivity = 0.8;
    }
    
    public String getResource() {
        return resource;
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
            "Type: Mining Quarry\n" +
            "Resource: %s\n" +
            "Output: %.1f units/day\n" +
            "Workers: %,.0f\n" +
            "Efficiency: %.0f%%",
            name, resource, outputPerDay, population, productivity * 100
        );
    }
}
