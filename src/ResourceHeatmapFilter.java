import javafx.scene.paint.Color;

public class ResourceHeatmapFilter implements MapFilter {
    private final ResourceMap resources;

    public ResourceHeatmapFilter(ResourceMap resources) {
        this.resources = resources;
    }

    @Override
    public String getFilterName() { return "Resource Heatmap"; }

    @Override
    public Color getTerrainColor(TerrainType terrain) {
        // Unused in this filter; MapCanvas will call getCellColor.
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

    public Color getCellColor(int x, int y) {
        double ore = resources.get(ResourceType.ORE, x, y);
        double stone = resources.get(ResourceType.STONE, x, y);
        double gem = resources.get(ResourceType.GEM, x, y);
        double wood = resources.get(ResourceType.WOOD, x, y);
        double fert = resources.get(ResourceType.FERTILITY, x, y);
        double fish = resources.get(ResourceType.FISH, x, y);

        // Dominant resource coloring with brightness from intensity
        double max = ore; ResourceType dominant = ResourceType.ORE;
        if (stone > max) { max = stone; dominant = ResourceType.STONE; }
        if (gem > max)   { max = gem;   dominant = ResourceType.GEM; }
        if (wood > max)  { max = wood;  dominant = ResourceType.WOOD; }
        if (fert > max)  { max = fert;  dominant = ResourceType.FERTILITY; }
        if (fish > max)  { max = fish;  dominant = ResourceType.FISH; }

        Color base;
        switch (dominant) {
            case ORE: base = Color.web("#ff6a00"); break;       // orange-red
            case STONE: base = Color.web("#c0c0c0"); break;     // light gray
            case GEM: base = Color.web("#b000b0"); break;       // magenta
            case WOOD: base = Color.web("#2dbf4f"); break;      // green
            case FERTILITY: base = Color.web("#c7f000"); break; // yellow-green
            case FISH: base = Color.web("#3ab0ff"); break;      // blue
            default: base = Color.WHITE; break;
        }
        double brightness = Math.max(0.2, Math.min(1.0, max));
        return base.interpolate(Color.BLACK, 1.0 - brightness);
    }
}
