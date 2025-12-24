import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Fishing node that spawns in water near fishing villages.
 * Displays as a jumping fish with water splashes.
 */
public class FisheryNode extends ResourceNodeBase {
    
    private FishType fishType;
    private double jumpPhase;      // Animation phase
    private double splashPhase;    // Splash animation
    private double[] ripplePositions;
    private long animationStart;
    
    /**
     * Creates a fishing node near a fishing village (in water).
     */
    public FisheryNode(double worldX, double worldY, FishType fishType, Town parentVillage) {
        super(worldX, worldY, fishType, parentVillage);
        this.fishType = fishType;
        
        Random rand = new Random((long)(worldX * 1000 + worldY));
        
        // Animation
        this.jumpPhase = rand.nextDouble() * Math.PI * 2;
        this.splashPhase = rand.nextDouble() * Math.PI * 2;
        this.animationStart = System.currentTimeMillis();
        
        // Ripple positions
        this.ripplePositions = new double[6];
        for (int i = 0; i < 6; i++) {
            ripplePositions[i] = rand.nextDouble();
        }
        
        // Fishery-specific defaults
        this.maxHarvests = 6 + rand.nextInt(4); // 6-9 catches
        this.harvestsRemaining = maxHarvests;
        this.regrowthTimeMs = 45000 + rand.nextInt(30000); // 45s-1.25min
        this.interactionRadius = 3.0;
        this.size = 0.9 + rand.nextDouble() * 0.3;
    }
    
    @Override
    public void render(GraphicsContext gc, double screenX, double screenY, double zoom, int tileSize) {
        double baseSize = size * zoom * tileSize;
        
        // Animate
        long elapsed = System.currentTimeMillis() - animationStart;
        double time = elapsed / 1000.0;
        double jump = Math.sin(time * 2 + jumpPhase);
        double splash = Math.abs(Math.sin(time * 2.5 + splashPhase));
        
        // Draw water ripples
        renderWaterRipples(gc, screenX, screenY, baseSize, time);
        
        // Draw splash effects when fish is "jumping"
        if (jump > 0.5) {
            renderSplash(gc, screenX, screenY, baseSize, splash);
        }
        
        // Draw jumping fish (only when above water)
        if (jump > 0) {
            double fishY = screenY - jump * baseSize * 0.4;
            double fishRotation = jump * 30 - 15;
            renderFish(gc, screenX, fishY, baseSize * 0.6, fishRotation);
        }
        
        // Draw interaction indicator if harvestable
        if (isHarvestable && harvestsRemaining > 0) {
            gc.setFill(Color.web("#4169E1").deriveColor(0, 1, 1, 0.3));
            gc.fillOval(screenX - baseSize * 0.6, screenY - baseSize * 0.2, baseSize * 1.2, baseSize * 0.5);
        }
    }
    
    private void renderWaterRipples(GraphicsContext gc, double x, double y, double size, double time) {
        gc.setStroke(Color.web("#ADD8E6").deriveColor(0, 1, 1, 0.4));
        gc.setLineWidth(1.5);
        
        for (int i = 0; i < 3; i++) {
            double phase = ripplePositions[i] * Math.PI * 2;
            double rippleSize = (Math.sin(time * 1.5 + phase) * 0.3 + 0.7) * size * (0.4 + i * 0.2);
            double alpha = 0.4 - i * 0.1;
            gc.setStroke(Color.web("#ADD8E6").deriveColor(0, 1, 1, alpha));
            gc.strokeOval(x - rippleSize / 2, y - rippleSize / 4, rippleSize, rippleSize / 2);
        }
    }
    
    private void renderSplash(GraphicsContext gc, double x, double y, double size, double intensity) {
        gc.setFill(Color.web("#E0FFFF").deriveColor(0, 1, 1, 0.6 * intensity));
        
        // Water droplets
        for (int i = 0; i < 5; i++) {
            double angle = (i - 2) * 0.4;
            double dist = size * 0.2 * intensity;
            double dx = Math.sin(angle) * dist;
            double dy = -Math.abs(Math.cos(angle)) * dist * 1.5;
            double dropSize = size * 0.08 * (1 - i * 0.15);
            gc.fillOval(x + dx - dropSize / 2, y + dy - dropSize / 2, dropSize, dropSize);
        }
    }
    
    /**
     * Renders the fish sprite with detailed appearance based on fish type.
     */
    private void renderFish(GraphicsContext gc, double x, double y, double size, double rotation) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(rotation);
        
        Color body = fishType.getBodyColor();
        Color fin = fishType.getFinColor();
        Color belly = fishType.getBellyColor();
        
        switch (fishType) {
            case TROUT: renderTrout(gc, size, body, fin, belly); break;
            case SALMON: renderSalmon(gc, size, body, fin, belly); break;
            case BASS: renderBass(gc, size, body, fin, belly); break;
            case CATFISH: renderCatfish(gc, size, body, fin, belly); break;
        }
        
        gc.restore();
    }
    
    private void renderTrout(GraphicsContext gc, double size, Color body, Color fin, Color belly) {
        // Streamlined body
        gc.setFill(body);
        gc.fillOval(-size * 0.4, -size * 0.15, size * 0.8, size * 0.3);
        
        // Belly
        gc.setFill(belly);
        gc.fillOval(-size * 0.3, size * 0.02, size * 0.5, size * 0.12);
        
        // Spots
        gc.setFill(Color.web("#8B0000").deriveColor(0, 1, 1, 0.6));
        for (int i = 0; i < 5; i++) {
            double sx = -size * 0.25 + i * size * 0.12;
            double sy = -size * 0.05 + (i % 2) * size * 0.06;
            gc.fillOval(sx, sy, size * 0.04, size * 0.04);
        }
        
        // Tail fin
        gc.setFill(fin);
        double[] tailX = {size * 0.35, size * 0.5, size * 0.5, size * 0.35};
        double[] tailY = {0, -size * 0.15, size * 0.15, 0};
        gc.fillPolygon(tailX, tailY, 4);
        
        // Dorsal fin
        double[] dorsalX = {-size * 0.1, 0, size * 0.1, 0};
        double[] dorsalY = {-size * 0.12, -size * 0.25, -size * 0.12, -size * 0.15};
        gc.fillPolygon(dorsalX, dorsalY, 4);
        
        // Eye
        gc.setFill(Color.WHITE);
        gc.fillOval(-size * 0.32, -size * 0.08, size * 0.08, size * 0.08);
        gc.setFill(Color.BLACK);
        gc.fillOval(-size * 0.3, -size * 0.06, size * 0.04, size * 0.04);
    }
    
    private void renderSalmon(GraphicsContext gc, double size, Color body, Color fin, Color belly) {
        // Larger, muscular body
        gc.setFill(body);
        gc.fillOval(-size * 0.45, -size * 0.18, size * 0.9, size * 0.36);
        
        // Pink belly
        gc.setFill(belly);
        gc.fillOval(-size * 0.35, size * 0.0, size * 0.6, size * 0.15);
        
        // Pink stripe
        gc.setFill(fin.deriveColor(0, 0.7, 1, 0.5));
        gc.fillRect(-size * 0.3, -size * 0.02, size * 0.55, size * 0.04);
        
        // Tail fin (forked)
        gc.setFill(fin);
        double[] tailX = {size * 0.4, size * 0.55, size * 0.45, size * 0.55, size * 0.4};
        double[] tailY = {-size * 0.05, -size * 0.2, 0, size * 0.2, size * 0.05};
        gc.fillPolygon(tailX, tailY, 5);
        
        // Dorsal fin
        double[] dorsalX = {-size * 0.05, size * 0.05, size * 0.15, size * 0.05};
        double[] dorsalY = {-size * 0.15, -size * 0.3, -size * 0.15, -size * 0.18};
        gc.fillPolygon(dorsalX, dorsalY, 4);
        
        // Eye
        gc.setFill(Color.WHITE);
        gc.fillOval(-size * 0.38, -size * 0.1, size * 0.1, size * 0.1);
        gc.setFill(Color.BLACK);
        gc.fillOval(-size * 0.35, -size * 0.07, size * 0.05, size * 0.05);
    }
    
    private void renderBass(GraphicsContext gc, double size, Color body, Color fin, Color belly) {
        // Compact, deep body
        gc.setFill(body);
        gc.fillOval(-size * 0.35, -size * 0.2, size * 0.7, size * 0.4);
        
        // Light belly
        gc.setFill(belly);
        gc.fillOval(-size * 0.25, size * 0.02, size * 0.45, size * 0.15);
        
        // Dark stripes
        gc.setStroke(body.darker());
        gc.setLineWidth(2);
        for (int i = 0; i < 4; i++) {
            double sx = -size * 0.2 + i * size * 0.12;
            gc.strokeLine(sx, -size * 0.12, sx + size * 0.03, size * 0.12);
        }
        
        // Tail fin
        gc.setFill(fin);
        double[] tailX = {size * 0.3, size * 0.45, size * 0.45, size * 0.3};
        double[] tailY = {0, -size * 0.15, size * 0.15, 0};
        gc.fillPolygon(tailX, tailY, 4);
        
        // Spiny dorsal fin
        gc.setFill(fin);
        for (int i = 0; i < 6; i++) {
            double fx = -size * 0.15 + i * size * 0.06;
            double fh = size * 0.12 + (3 - Math.abs(i - 2.5)) * size * 0.03;
            gc.fillRect(fx, -size * 0.18 - fh, size * 0.02, fh);
        }
        
        // Eye
        gc.setFill(Color.web("#FFD700"));
        gc.fillOval(-size * 0.28, -size * 0.1, size * 0.1, size * 0.1);
        gc.setFill(Color.BLACK);
        gc.fillOval(-size * 0.25, -size * 0.07, size * 0.05, size * 0.05);
    }
    
    private void renderCatfish(GraphicsContext gc, double size, Color body, Color fin, Color belly) {
        // Flat, wide body
        gc.setFill(body);
        gc.fillOval(-size * 0.4, -size * 0.12, size * 0.8, size * 0.28);
        
        // Lighter belly
        gc.setFill(belly);
        gc.fillOval(-size * 0.3, size * 0.02, size * 0.5, size * 0.12);
        
        // Whiskers (barbels)
        gc.setStroke(body.darker());
        gc.setLineWidth(1.5);
        gc.strokeLine(-size * 0.35, -size * 0.02, -size * 0.5, -size * 0.1);
        gc.strokeLine(-size * 0.35, 0, -size * 0.52, size * 0.02);
        gc.strokeLine(-size * 0.35, size * 0.02, -size * 0.5, size * 0.12);
        
        // Tail fin (rounded)
        gc.setFill(fin);
        gc.fillOval(size * 0.25, -size * 0.12, size * 0.2, size * 0.24);
        
        // Dorsal fin (small)
        gc.setFill(fin);
        double[] dorsalX = {-size * 0.05, 0, size * 0.08};
        double[] dorsalY = {-size * 0.1, -size * 0.2, -size * 0.1};
        gc.fillPolygon(dorsalX, dorsalY, 3);
        
        // Small eyes
        gc.setFill(Color.web("#333333"));
        gc.fillOval(-size * 0.32, -size * 0.06, size * 0.06, size * 0.06);
    }
    
    @Override
    public int getBaseYield() {
        Random rand = new Random();
        int base = 2 + rand.nextInt(4); // 2-5 fish
        return (int)(base * fishType.getCatchModifier());
    }
    
    @Override
    public String getResourceCategory() {
        return "Fish";
    }
    
    @Override
    public String getItemId() {
        return fishType.getItemId();
    }
    
    @Override
    public String getDisplayName() {
        return fishType.getDisplayName() + " School";
    }
    
    public FishType getFishType() {
        return fishType;
    }
}
