import javafx.scene.paint.Color;

public class Stoneworks extends MapStructure {
    
    private double outputPerDay;
    
    public Stoneworks(int gridX, int gridY, String name) {
        super(gridX, gridY, name, 5, Color.web("#a0a0a0"), "Stoneworks");
        this.population = 700;
        this.outputPerDay = 90;
        this.productivity = 0.8;
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
            "Type: Stoneworks\n" +
            "Output: %.1f stone/day\n" +
            "Workers: %,.0f\n" +
            "Efficiency: %.0f%%",
            name, outputPerDay, population, productivity * 100
        );
    }
}
