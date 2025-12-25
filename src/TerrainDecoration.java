import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

/**
 * Terrain decoration system with sprite variants and biome tags.
 */
public class TerrainDecoration {
    
    /**
     * Decoration categories with their valid biome tags.
     */
    public enum Category {
        GRASS("grass", "plains", "meadow", "savanna"),
        ROCK("rocky", "mountain", "hills", "desert"),
        TREE("forest", "woodland", "taiga", "jungle"),
        CORAL("reef", "coastal"),
        ASH("volcano", "lava", "charred"),
        PIER("pier", "coastal"),
        WINDMILL("farm", "agricultural");
        
        private final Set<String> validTags;
        
        Category(String... tags) {
            this.validTags = new HashSet<>(Arrays.asList(tags));
        }
        
        public boolean matchesBiome(TerrainType terrain) {
            Set<String> biomeTags = getBiomeTags(terrain);
            for (String tag : validTags) {
                if (biomeTags.contains(tag)) return true;
            }
            return false;
        }
    }
    
    /**
     * Get tags for a terrain type.
     */
    public static Set<String> getBiomeTags(TerrainType terrain) {
        Set<String> tags = new HashSet<>();
        switch (terrain) {
            case GRASS: tags.addAll(Arrays.asList("grass", "plains")); break;
            case PLAINS: tags.addAll(Arrays.asList("plains", "grass", "meadow")); break;
            case MEADOW: tags.addAll(Arrays.asList("meadow", "grass", "plains")); break;
            case FOREST: tags.addAll(Arrays.asList("forest", "woodland")); break;
            case DENSE_FOREST: tags.addAll(Arrays.asList("forest", "woodland", "dense")); break;
            case TAIGA: tags.addAll(Arrays.asList("taiga", "forest", "cold")); break;
            case JUNGLE: tags.addAll(Arrays.asList("jungle", "forest", "dense")); break;
            case HILLS: tags.addAll(Arrays.asList("hills", "rocky")); break;
            case ROCKY_HILLS: tags.addAll(Arrays.asList("rocky", "hills", "mountain")); break;
            case MOUNTAIN: tags.addAll(Arrays.asList("mountain", "rocky")); break;
            case DESERT: tags.addAll(Arrays.asList("desert", "rocky", "arid")); break;
            case SAVANNA: tags.addAll(Arrays.asList("savanna", "grass", "arid")); break;
            case SCRUBLAND: tags.addAll(Arrays.asList("scrubland", "arid")); break;
            case BEACH: tags.addAll(Arrays.asList("beach", "coastal")); break;
            case REEF: tags.addAll(Arrays.asList("reef", "coastal")); break;
            case SWAMP: tags.addAll(Arrays.asList("swamp", "wetland")); break;
            case MARSH: tags.addAll(Arrays.asList("marsh", "wetland")); break;
            case VOLCANO: tags.addAll(Arrays.asList("volcano", "lava")); break;
            case LAVA: tags.addAll(Arrays.asList("lava", "charred")); break;
            case TUNDRA: tags.addAll(Arrays.asList("tundra", "cold")); break;
            case SNOW: tags.addAll(Arrays.asList("snow", "cold")); break;
            default: break;
        }
        return tags;
    }
    
    // Decoration instance data
    private final int worldX;
    private final int worldY;
    private final Category category;
    private final int variant; // 0-9 for each category
    private final double size;
    private final double rotation;
    
    public TerrainDecoration(int x, int y, Category category, int variant, double size, double rotation) {
        this.worldX = x;
        this.worldY = y;
        this.category = category;
        this.variant = variant % 10;
        this.size = size;
        this.rotation = rotation;
    }
    
    /**
     * Renders this decoration at the appropriate screen position.
     */
    public void render(GraphicsContext gc, double viewX, double viewY, double zoom, int tileSize) {
        double screenX = (worldX - viewX) * zoom * tileSize;
        double screenY = (worldY - viewY) * zoom * tileSize;
        double renderSize = size * zoom * tileSize * 0.8;
        
        // Skip if off screen
        if (screenX < -50 || screenX > gc.getCanvas().getWidth() + 50 ||
            screenY < -50 || screenY > gc.getCanvas().getHeight() + 50) {
            return;
        }
        
        // Round to nearest pixel to prevent sub-pixel jitter
        screenX = Math.round(screenX);
        screenY = Math.round(screenY);
        
        gc.save();
        gc.translate(screenX, screenY);
        if (rotation != 0) gc.rotate(rotation);
        
        switch (category) {
            case GRASS: renderGrass(gc, renderSize); break;
            case ROCK: renderRock(gc, renderSize); break;
            case TREE: renderTree(gc, renderSize); break;
            case CORAL: renderCoral(gc, renderSize); break;
            case ASH: renderAsh(gc, renderSize); break;
            case PIER: renderPier(gc, renderSize); break;
            case WINDMILL: renderWindmill(gc, renderSize); break;
        }
        
        gc.restore();
    }
    
    // ==================== GRASS SPRITES (10 variants) ====================
    
    private void renderGrass(GraphicsContext gc, double size) {
        switch (variant) {
            case 0: renderGrassTuft(gc, size, 3, Color.web("#5a8830")); break;
            case 1: renderGrassTuft(gc, size, 5, Color.web("#6a9838")); break;
            case 2: renderGrassBlade(gc, size, Color.web("#4a7828")); break;
            case 3: renderGrassClump(gc, size, Color.web("#5a9030")); break;
            case 4: renderTallGrass(gc, size, Color.web("#4a8028")); break;
            case 5: renderFlowerGrass(gc, size, Color.web("#5a8830"), Color.web("#e8e030")); break;
            case 6: renderFlowerGrass(gc, size, Color.web("#5a8830"), Color.web("#e080a0")); break;
            case 7: renderDryGrass(gc, size, Color.web("#a89858")); break;
            case 8: renderFern(gc, size, Color.web("#408028")); break;
            case 9: renderBush(gc, size, Color.web("#3a6820")); break;
        }
    }
    
    private void renderGrassTuft(GraphicsContext gc, double size, int blades, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(Math.max(1, size * 0.1));
        for (int i = 0; i < blades; i++) {
            double angle = -30 + (60.0 / (blades - 1)) * i;
            double rad = Math.toRadians(angle);
            double h = size * (0.6 + Math.random() * 0.4);
            gc.strokeLine(0, 0, Math.sin(rad) * h * 0.3, -h);
        }
    }
    
    private void renderGrassBlade(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillPolygon(
            new double[] { -size * 0.05, 0, size * 0.05 },
            new double[] { 0, -size, 0 },
            3
        );
    }
    
    private void renderGrassClump(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        for (int i = 0; i < 5; i++) {
            double ox = (i - 2) * size * 0.12;
            double h = size * (0.5 + (i % 2) * 0.3);
            gc.fillPolygon(
                new double[] { ox - size * 0.04, ox, ox + size * 0.04 },
                new double[] { 0, -h, 0 },
                3
            );
        }
    }
    
    private void renderTallGrass(GraphicsContext gc, double size, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(Math.max(1, size * 0.08));
        for (int i = 0; i < 4; i++) {
            double ox = (i - 1.5) * size * 0.15;
            double sway = Math.sin(i * 0.8) * size * 0.15;
            gc.strokeLine(ox, 0, ox + sway, -size * (0.8 + i * 0.1));
        }
    }
    
    private void renderFlowerGrass(GraphicsContext gc, double size, Color grassColor, Color flowerColor) {
        renderGrassTuft(gc, size * 0.7, 3, grassColor);
        gc.setFill(flowerColor);
        gc.fillOval(-size * 0.1, -size * 0.9, size * 0.2, size * 0.2);
    }
    
    private void renderDryGrass(GraphicsContext gc, double size, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(Math.max(1, size * 0.06));
        for (int i = 0; i < 4; i++) {
            double angle = -40 + i * 25;
            double rad = Math.toRadians(angle);
            double h = size * 0.6;
            gc.strokeLine(0, 0, Math.sin(rad) * h * 0.4, -h);
        }
    }
    
    private void renderFern(GraphicsContext gc, double size, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(Math.max(1, size * 0.05));
        // Main stem
        gc.strokeLine(0, 0, 0, -size * 0.8);
        // Fronds
        for (int i = 0; i < 5; i++) {
            double y = -size * 0.2 - i * size * 0.12;
            double len = size * 0.2 * (1 - i * 0.15);
            gc.strokeLine(0, y, -len, y - len * 0.3);
            gc.strokeLine(0, y, len, y - len * 0.3);
        }
    }
    
    private void renderBush(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillOval(-size * 0.4, -size * 0.5, size * 0.8, size * 0.5);
        gc.setFill(color.brighter());
        gc.fillOval(-size * 0.25, -size * 0.6, size * 0.5, size * 0.35);
    }
    
    // ==================== ROCK SPRITES (10 variants) ====================
    
    private void renderRock(GraphicsContext gc, double size) {
        switch (variant) {
            case 0: renderSmallRock(gc, size, Color.web("#686058")); break;
            case 1: renderMediumRock(gc, size, Color.web("#787068")); break;
            case 2: renderFlatRock(gc, size, Color.web("#606058")); break;
            case 3: renderRockCluster(gc, size, Color.web("#707060")); break;
            case 4: renderBoulder(gc, size, Color.web("#585850")); break;
            case 5: renderMossyRock(gc, size, Color.web("#686860"), Color.web("#4a6830")); break;
            case 6: renderPebbles(gc, size, Color.web("#808078")); break;
            case 7: renderCrackedRock(gc, size, Color.web("#686058")); break;
            case 8: renderSlateRock(gc, size, Color.web("#505048")); break;
            case 9: renderSandstoneRock(gc, size, Color.web("#c0a078")); break;
        }
    }
    
    private void renderSmallRock(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillOval(-size * 0.3, -size * 0.2, size * 0.6, size * 0.4);
        gc.setFill(color.brighter());
        gc.fillOval(-size * 0.2, -size * 0.25, size * 0.25, size * 0.15);
    }
    
    private void renderMediumRock(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        double[] xp = { -size * 0.4, -size * 0.2, size * 0.3, size * 0.4, size * 0.1, -size * 0.3 };
        double[] yp = { -size * 0.1, -size * 0.4, -size * 0.35, -size * 0.05, size * 0.1, size * 0.05 };
        gc.fillPolygon(xp, yp, 6);
        gc.setFill(color.brighter());
        gc.fillOval(-size * 0.15, -size * 0.35, size * 0.3, size * 0.15);
    }
    
    private void renderFlatRock(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillOval(-size * 0.5, -size * 0.15, size, size * 0.3);
        gc.setStroke(color.darker());
        gc.setLineWidth(1);
        gc.strokeOval(-size * 0.5, -size * 0.15, size, size * 0.3);
    }
    
    private void renderRockCluster(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillOval(-size * 0.35, -size * 0.15, size * 0.35, size * 0.25);
        gc.fillOval(0, -size * 0.2, size * 0.4, size * 0.3);
        gc.fillOval(-size * 0.15, -size * 0.35, size * 0.3, size * 0.2);
    }
    
    private void renderBoulder(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillOval(-size * 0.45, -size * 0.5, size * 0.9, size * 0.6);
        gc.setFill(color.brighter());
        gc.fillOval(-size * 0.3, -size * 0.45, size * 0.4, size * 0.25);
        gc.setFill(color.darker());
        gc.fillOval(-size * 0.1, -size * 0.1, size * 0.4, size * 0.2);
    }
    
    private void renderMossyRock(GraphicsContext gc, double size, Color rockColor, Color mossColor) {
        renderSmallRock(gc, size, rockColor);
        gc.setFill(mossColor);
        gc.fillOval(-size * 0.25, -size * 0.15, size * 0.3, size * 0.15);
    }
    
    private void renderPebbles(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        for (int i = 0; i < 5; i++) {
            double ox = (i - 2) * size * 0.18;
            double oy = ((i % 2) - 0.5) * size * 0.1;
            gc.fillOval(ox - size * 0.08, oy - size * 0.05, size * 0.16, size * 0.1);
        }
    }
    
    private void renderCrackedRock(GraphicsContext gc, double size, Color color) {
        renderMediumRock(gc, size, color);
        gc.setStroke(color.darker().darker());
        gc.setLineWidth(1);
        gc.strokeLine(-size * 0.1, -size * 0.3, size * 0.05, -size * 0.05);
        gc.strokeLine(-size * 0.05, -size * 0.2, size * 0.1, -size * 0.25);
    }
    
    private void renderSlateRock(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillRect(-size * 0.4, -size * 0.25, size * 0.8, size * 0.08);
        gc.fillRect(-size * 0.35, -size * 0.15, size * 0.7, size * 0.06);
        gc.fillRect(-size * 0.3, -size * 0.07, size * 0.6, size * 0.08);
    }
    
    private void renderSandstoneRock(GraphicsContext gc, double size, Color color) {
        gc.setFill(color);
        gc.fillRoundRect(-size * 0.35, -size * 0.35, size * 0.7, size * 0.45, size * 0.1, size * 0.1);
        gc.setStroke(color.darker());
        gc.setLineWidth(1);
        gc.strokeLine(-size * 0.25, -size * 0.2, size * 0.25, -size * 0.2);
        gc.strokeLine(-size * 0.2, -size * 0.1, size * 0.2, -size * 0.1);
    }
    
    // ==================== TREE SPRITES (10 variants) ====================
    
    private void renderTree(GraphicsContext gc, double size) {
        switch (variant) {
            case 0: renderOakTree(gc, size); break;
            case 1: renderPineTree(gc, size); break;
            case 2: renderBirchTree(gc, size); break;
            case 3: renderWillowTree(gc, size); break;
            case 4: renderMapleTree(gc, size); break;
            case 5: renderSpruceTree(gc, size); break;
            case 6: renderDeadTree(gc, size); break;
            case 7: renderBushyTree(gc, size); break;
            case 8: renderTallPine(gc, size); break;
            case 9: renderJungleTree(gc, size); break;
        }
    }
    
    private void renderOakTree(GraphicsContext gc, double size) {
        // Trunk
        gc.setFill(Color.web("#5a4030"));
        gc.fillRect(-size * 0.08, -size * 0.5, size * 0.16, size * 0.5);
        // Canopy
        gc.setFill(Color.web("#3a6820"));
        gc.fillOval(-size * 0.4, -size * 0.9, size * 0.8, size * 0.55);
        gc.setFill(Color.web("#4a7828"));
        gc.fillOval(-size * 0.3, -size * 0.95, size * 0.5, size * 0.4);
    }
    
    private void renderPineTree(GraphicsContext gc, double size) {
        // Trunk
        gc.setFill(Color.web("#5a4030"));
        gc.fillRect(-size * 0.06, -size * 0.4, size * 0.12, size * 0.4);
        // Triangular canopy
        gc.setFill(Color.web("#2a5828"));
        gc.fillPolygon(
            new double[] { 0, -size * 0.35, size * 0.35 },
            new double[] { -size * 0.95, -size * 0.35, -size * 0.35 },
            3
        );
        gc.setFill(Color.web("#3a6830"));
        gc.fillPolygon(
            new double[] { 0, -size * 0.3, size * 0.3 },
            new double[] { -size * 0.7, -size * 0.25, -size * 0.25 },
            3
        );
    }
    
    private void renderBirchTree(GraphicsContext gc, double size) {
        // White trunk with marks
        gc.setFill(Color.web("#e8e0d8"));
        gc.fillRect(-size * 0.05, -size * 0.55, size * 0.1, size * 0.55);
        gc.setFill(Color.web("#404040"));
        for (int i = 0; i < 4; i++) {
            gc.fillRect(-size * 0.04, -size * 0.15 - i * size * 0.12, size * 0.03, size * 0.02);
        }
        // Light canopy
        gc.setFill(Color.web("#70a840"));
        gc.fillOval(-size * 0.3, -size * 0.85, size * 0.6, size * 0.4);
    }
    
    private void renderWillowTree(GraphicsContext gc, double size) {
        // Trunk
        gc.setFill(Color.web("#6a5040"));
        gc.fillRect(-size * 0.08, -size * 0.4, size * 0.16, size * 0.4);
        // Drooping canopy
        gc.setFill(Color.web("#5a8838"));
        gc.fillOval(-size * 0.45, -size * 0.7, size * 0.9, size * 0.45);
        // Hanging branches
        gc.setStroke(Color.web("#4a7828"));
        gc.setLineWidth(Math.max(1, size * 0.03));
        for (int i = 0; i < 7; i++) {
            double ox = -size * 0.35 + i * size * 0.12;
            gc.strokeLine(ox, -size * 0.4, ox + (i - 3) * size * 0.03, -size * 0.1);
        }
    }
    
    private void renderMapleTree(GraphicsContext gc, double size) {
        // Trunk
        gc.setFill(Color.web("#5a4030"));
        gc.fillRect(-size * 0.07, -size * 0.45, size * 0.14, size * 0.45);
        // Rounded canopy
        gc.setFill(Color.web("#b85030")); // Red-orange for maple
        gc.fillOval(-size * 0.35, -size * 0.85, size * 0.7, size * 0.5);
        gc.setFill(Color.web("#c86040"));
        gc.fillOval(-size * 0.25, -size * 0.9, size * 0.4, size * 0.35);
    }
    
    private void renderSpruceTree(GraphicsContext gc, double size) {
        // Trunk
        gc.setFill(Color.web("#4a3828"));
        gc.fillRect(-size * 0.05, -size * 0.35, size * 0.1, size * 0.35);
        // Layered canopy
        gc.setFill(Color.web("#1a4020"));
        for (int i = 0; i < 4; i++) {
            double w = size * (0.4 - i * 0.08);
            double y = -size * 0.35 - i * size * 0.18;
            gc.fillPolygon(
                new double[] { 0, -w, w },
                new double[] { y - size * 0.2, y, y },
                3
            );
        }
    }
    
    private void renderDeadTree(GraphicsContext gc, double size) {
        gc.setStroke(Color.web("#4a4038"));
        gc.setLineWidth(Math.max(2, size * 0.08));
        // Main trunk
        gc.strokeLine(0, 0, 0, -size * 0.7);
        // Bare branches
        gc.setLineWidth(Math.max(1, size * 0.04));
        gc.strokeLine(0, -size * 0.5, -size * 0.25, -size * 0.7);
        gc.strokeLine(0, -size * 0.5, size * 0.2, -size * 0.65);
        gc.strokeLine(0, -size * 0.35, -size * 0.15, -size * 0.5);
        gc.strokeLine(0, -size * 0.35, size * 0.18, -size * 0.45);
    }
    
    private void renderBushyTree(GraphicsContext gc, double size) {
        // Trunk
        gc.setFill(Color.web("#5a4030"));
        gc.fillRect(-size * 0.06, -size * 0.35, size * 0.12, size * 0.35);
        // Dense round canopy
        gc.setFill(Color.web("#2a5018"));
        gc.fillOval(-size * 0.4, -size * 0.75, size * 0.8, size * 0.55);
        gc.fillOval(-size * 0.35, -size * 0.85, size * 0.7, size * 0.45);
        gc.fillOval(-size * 0.25, -size * 0.65, size * 0.5, size * 0.35);
    }
    
    private void renderTallPine(GraphicsContext gc, double size) {
        // Tall trunk
        gc.setFill(Color.web("#5a4030"));
        gc.fillRect(-size * 0.04, -size * 0.6, size * 0.08, size * 0.6);
        // Narrow canopy
        gc.setFill(Color.web("#1a4828"));
        gc.fillPolygon(
            new double[] { 0, -size * 0.2, size * 0.2 },
            new double[] { -size, -size * 0.55, -size * 0.55 },
            3
        );
        gc.fillPolygon(
            new double[] { 0, -size * 0.18, size * 0.18 },
            new double[] { -size * 0.75, -size * 0.4, -size * 0.4 },
            3
        );
    }
    
    private void renderJungleTree(GraphicsContext gc, double size) {
        // Thick trunk
        gc.setFill(Color.web("#5a5038"));
        gc.fillRect(-size * 0.1, -size * 0.5, size * 0.2, size * 0.5);
        // Large dense canopy
        gc.setFill(Color.web("#1a5020"));
        gc.fillOval(-size * 0.5, -size * 0.95, size, size * 0.6);
        gc.setFill(Color.web("#2a6028"));
        gc.fillOval(-size * 0.4, -size * 0.85, size * 0.6, size * 0.4);
        // Vines
        gc.setStroke(Color.web("#3a5020"));
        gc.setLineWidth(1);
        gc.strokeLine(-size * 0.35, -size * 0.5, -size * 0.4, -size * 0.1);
        gc.strokeLine(size * 0.3, -size * 0.55, size * 0.35, -size * 0.15);
    }
    
    // ==================== CORAL & ASH SPRITES ====================
    
    private void renderCoral(GraphicsContext gc, double size) {
        // Coral cluster - colorful small rounded forms
        Color[] palette = { Color.web("#ff6a00"), Color.web("#ff9a80"), Color.web("#b000b0"), Color.web("#3ab0ff") };
        for (int i = 0; i < 4; i++) {
            double ang = i * Math.PI * 0.5 + (i % 2 == 0 ? -0.3 : 0.2);
            double ox = Math.cos(ang) * size * 0.18;
            double oy = Math.sin(ang) * size * 0.12;
            gc.setFill(palette[i]);
            gc.fillOval(ox - size * 0.18, oy - size * 0.18, size * 0.36, size * 0.36);
            gc.setFill(palette[i].brighter());
            gc.fillOval(ox - size * 0.08, oy - size * 0.08, size * 0.16, size * 0.16);
        }
        // Small anemone tentacle strokes
        gc.setStroke(Color.web("#ffb380"));
        gc.setLineWidth(Math.max(1, size * 0.06));
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI * 2 / 6;
            gc.strokeLine(Math.cos(a) * size * 0.05, Math.sin(a) * size * 0.05, Math.cos(a) * size * 0.25, Math.sin(a) * size * 0.25);
        }
    }
    
    private void renderAsh(GraphicsContext gc, double size) {
        // Charred rock with smoke puffs
        Color base = Color.web("#4a4038");
        gc.setFill(base);
        gc.fillOval(-size * 0.35, -size * 0.15, size * 0.7, size * 0.3);
        gc.setFill(base.darker());
        gc.fillOval(-size * 0.15, -size * 0.3, size * 0.3, size * 0.18);

        // Smoke puffs above
        gc.setFill(Color.web("#888888", 0.35));
        gc.fillOval(-size * 0.25, -size * 0.6, size * 0.5, size * 0.25);
        gc.fillOval(-size * 0.1, -size * 0.75, size * 0.25, size * 0.18);
    }
    
    // ==================== PIER & WINDMILL SPRITES ====================
    
    private void renderPier(GraphicsContext gc, double size) {
        // Simple wooden pier with posts and planks, oriented outward
        gc.setFill(Color.web("#8b7355"));
        // Plank platform
        gc.fillRect(-size * 0.4, -size * 0.1, size * 0.8, size * 0.15);
        // Pier walkway
        gc.fillRect(-size * 0.05, 0, size * 0.1, size * 0.7);
        // Posts
        gc.setFill(Color.web("#5a4a3a"));
        for (double px = -0.35; px <= 0.35; px += 0.15) {
            gc.fillRect(px * size, size * 0.05, size * 0.03, size * 0.35);
        }
        // Small fishing nets/boxes
        gc.setFill(Color.web("#3a3a3a"));
        gc.fillRect(-size * 0.3, size * 0.05, size * 0.08, size * 0.06);
        gc.fillRect(size * 0.18, size * 0.05, size * 0.08, size * 0.06);
    }
    
    private void renderWindmill(GraphicsContext gc, double size) {
        // Windmill: stone base + rotating blades
        gc.setFill(Color.web("#d4c9b0"));
        gc.fillRect(-size * 0.12, -size * 0.35, size * 0.24, size * 0.35);
        // Roof
        gc.setFill(Color.web("#8b4513"));
        gc.fillPolygon(new double[]{-size * 0.16, 0, size * 0.16}, new double[]{-size * 0.35, -size * 0.5, -size * 0.35}, 3);
        // Blades
        gc.setStroke(Color.web("#ffffff", 0.9));
        gc.setLineWidth(Math.max(1, size * 0.04));
        gc.strokeLine(0, -size * 0.45, 0, -size * 0.65);
        gc.strokeLine(-size * 0.08, -size * 0.56, size * 0.08, -size * 0.56);
        gc.strokeLine(-size * 0.06, -size * 0.62, size * 0.06, -size * 0.50);
    }
    
    // Getters
    public int getWorldX() { return worldX; }
    public int getWorldY() { return worldY; }
    public Category getCategory() { return category; }
    public int getVariant() { return variant; }
}
