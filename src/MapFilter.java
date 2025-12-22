import javafx.scene.paint.Color;

public interface MapFilter {
    String getFilterName();
    Color getTerrainColor(TerrainType terrain);
    Color getRoadColor(RoadQuality quality);
    Color getStructureColor(MapStructure structure);
}
