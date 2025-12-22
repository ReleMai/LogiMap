import javafx.scene.paint.Color;

public class TopographicalFilter implements MapFilter {
    
    @Override
    public String getFilterName() {
        return "Topographical";
    }
    
    @Override
    public Color getTerrainColor(TerrainType terrain) {
        // Water types
        if (terrain.isWater()) {
            return Color.web("#2e5f9e");
        }
        
        // Mountains and rocky terrain
        if (terrain.isMountainous()) {
            return Color.web("#8b7355");
        }
        
        // Forests
        if (terrain.isForest()) {
            return Color.web("#1a4d2e");
        }
        
        // Special terrain types
        switch (terrain) {
            case GRASS:
            case PLAINS:
            case MEADOW:
                return Color.web("#4a7c3d");
            case BEACH:
            case DESERT:
            case DUNES:
                return Color.web("#d9c49f");
            case SWAMP:
            case MARSH:
                return Color.web("#5a6b47");
            case SAVANNA:
            case SCRUBLAND:
                return Color.web("#7a8a50");
            case HILLS:
                return Color.web("#6a7a50");
            case TUNDRA:
            case SNOW:
            case ICE:
            case GLACIER:
                return Color.web("#d0e8f0");
            default:
                return terrain.getPrimaryColor();
        }
    }
    
    @Override
    public Color getRoadColor(RoadQuality quality) {
        switch (quality) {
            case ASPHALT:
                return Color.web("#2a2a2a");
            case PAVED:
                return Color.web("#4a4a4a");
            case GRAVEL:
                return Color.web("#a89968");
            case DIRT:
                return Color.web("#8b7355");
            default:
                return quality.getColor();
        }
    }
    
    @Override
    public Color getStructureColor(MapStructure structure) {
        // Show structures with bright colors for topo view
        Color baseColor = structure.getColor();
        return baseColor.brighter().brighter();
    }
}
