import javafx.scene.paint.Color;

public class Millworks extends MapStructure {
    
    private String processType; // "Grain", "Wood", "Stone"
    private double outputPerDay;
    
    public Millworks(int gridX, int gridY, String name, String processType) {
        super(gridX, gridY, name, 4, Color.web("#b8956d"), "Millworks");
        this.processType = processType;
        this.population = 500;
        this.outputPerDay = 70;
        this.productivity = 0.85;
    }
    
    public String getProcessType() {
        return processType;
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
            "Type: Millworks (%s)\n" +
            "Output: %.1f units/day\n" +
            "Workers: %,.0f\n" +
            "Efficiency: %.0f%%",
            name, processType, outputPerDay, population, productivity * 100
        );
    }
}
