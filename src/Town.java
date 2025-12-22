import javafx.scene.paint.Color;

/**
 * Represents a town settlement on the map.
 * Towns serve as trade hubs and population centers.
 * Can be either major towns (larger, higher trade value) or minor towns.
 */
public class Town extends MapStructure {
    
    private boolean isMajor;
    private double tradeValue;
    
    public Town(int gridX, int gridY, String name, boolean isMajor) {
        super(gridX, gridY, name, isMajor ? 8 : 4, 
              isMajor ? Color.web("#d4a574") : Color.web("#c9956d"), 
              isMajor ? "Major Town" : "Minor Town");
        
        this.isMajor = isMajor;
        this.population = isMajor ? 50000 : 10000;
        this.tradeValue = isMajor ? 500 : 100;
    }
    
    public boolean isMajor() {
        return isMajor;
    }
    
    public double getTradeValue() {
        return tradeValue;
    }
    
    @Override
    public String getInfo() {
        return String.format(
            "%s\n" +
            "Type: %s\n" +
            "Population: %,.0f\n" +
            "Trade Value: %.0f\n" +
            "Size: %d cells",
            name, type, population, tradeValue, size
        );
    }
}
