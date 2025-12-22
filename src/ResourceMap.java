import java.util.Random;

public class ResourceMap {
    private final int width;
    private final int height;
    private final double[][] ore;
    private final double[][] stone;
    private final double[][] gem;
    private final double[][] wood;
    private final double[][] fertility;
    private final double[][] fish;

    public ResourceMap(TerrainType[][] terrain, int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.ore = new double[width][height];
        this.stone = new double[width][height];
        this.gem = new double[width][height];
        this.wood = new double[width][height];
        this.fertility = new double[width][height];
        this.fish = new double[width][height];
        generate(terrain, seed);
    }

    private void generate(TerrainType[][] terrain, long seed) {
        Random rand = new Random(seed);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TerrainType t = terrain[x][y];

                double n1 = noise(rand, x, y, 0.02);
                double n2 = noise(rand, x, y, 0.06);
                double richness = (n1 * 0.6 + n2 * 0.4);

                double oreBase = 0, stoneBase = 0, gemBase = 0, woodBase = 0, fertBase = 0, fishBase = 0;
                
                // Determine resource base values based on terrain type
                if (t.isMountainous()) {
                    oreBase = 0.6; stoneBase = 0.7; gemBase = 0.25;
                    fertBase = 0.1; woodBase = 0.15;
                } else if (t.isForest()) {
                    woodBase = 0.7; fertBase = 0.4; stoneBase = 0.2; oreBase = 0.2;
                } else if (t == TerrainType.GRASS || t == TerrainType.PLAINS || t == TerrainType.MEADOW) {
                    fertBase = 0.7; woodBase = 0.3; stoneBase = 0.2; oreBase = 0.15;
                } else if (t == TerrainType.BEACH || t == TerrainType.DESERT || t == TerrainType.DUNES) {
                    fertBase = 0.15; woodBase = 0.05; stoneBase = 0.1; oreBase = 0.05;
                } else if (t == TerrainType.SWAMP || t == TerrainType.MARSH) {
                    fertBase = 0.5; woodBase = 0.4; stoneBase = 0.15; oreBase = 0.1;
                } else if (t.isWater()) {
                    fishBase = 0.8;
                } else if (t == TerrainType.SAVANNA || t == TerrainType.SCRUBLAND) {
                    fertBase = 0.4; woodBase = 0.2; stoneBase = 0.25; oreBase = 0.2;
                } else if (t == TerrainType.HILLS || t == TerrainType.ROCKY_HILLS) {
                    oreBase = 0.4; stoneBase = 0.5; fertBase = 0.3; woodBase = 0.2;
                } else if (t == TerrainType.TUNDRA || t == TerrainType.SNOW) {
                    fertBase = 0.2; woodBase = 0.15; stoneBase = 0.3; oreBase = 0.25;
                }

                // lakes vs ocean approximation: fewer water neighbors => lake with higher fish
                if (t.isWater()) {
                    int landNeighbors = countLandNeighbors(terrain, x, y);
                    fishBase += landNeighbors >= 3 ? 0.15 : 0.0;
                }

                ore[x][y] = clamp01(oreBase * 0.6 + richness * 0.4);
                stone[x][y] = clamp01(stoneBase * 0.6 + richness * 0.4);
                gem[x][y] = clamp01(gemBase * 0.4 + richness * 0.3) * 0.8; // rarer
                wood[x][y] = clamp01(woodBase * 0.7 + richness * 0.3);
                fertility[x][y] = clamp01(fertBase * 0.7 + (1.0 - Math.abs(0.5 - n1)) * 0.2);
                fish[x][y] = clamp01(fishBase * 0.7 + n2 * 0.3);
            }
        }
    }

    private int countLandNeighbors(TerrainType[][] terrain, int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = Math.floorMod(x + dx, width);
                int ny = Math.floorMod(y + dy, height);
                if (!terrain[nx][ny].isWater()) count++;
            }
        }
        return count;
    }

    private double noise(Random rand, int x, int y, double scale) {
        // Deterministic pseudo-noise from trig combos; avoid rand usage per cell
        double n = Math.sin(x * scale) * Math.cos(y * scale) * 0.5
                 + Math.sin(x * scale * 1.7) * 0.3
                 + Math.cos(y * scale * 1.3) * 0.2;
        return (n + 1) / 2.0;
    }

    private double clamp01(double v) { return Math.max(0, Math.min(1, v)); }

    public double get(ResourceType type, int x, int y) {
        switch (type) {
            case ORE: return ore[x][y];
            case STONE: return stone[x][y];
            case GEM: return gem[x][y];
            case WOOD: return wood[x][y];
            case FERTILITY: return fertility[x][y];
            case FISH: return fish[x][y];
            default: return 0;
        }
    }
}
