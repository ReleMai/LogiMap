import javafx.scene.paint.Color;

public class StandardFilter implements MapFilter {
    
    @Override
    public String getFilterName() {
        return "Standard";
    }
    
    @Override
    public Color getTerrainColor(TerrainType terrain) {
        return terrain.getPrimaryColor();
    }
    
    @Override
    public Color getRoadColor(RoadQuality quality) {
        return quality.getColor();
    }
    
    @Override
    public Color getStructureColor(MapStructure structure) {
        return structure.getColor();
    }
}
