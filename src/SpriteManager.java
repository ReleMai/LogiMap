import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * Generates and caches simple sprite frames for harvest animations.
 * Frames are generated programmatically (vector) and act as sprite frames.
 */
public class SpriteManager {
    private static SpriteManager instance = null;
    private final Map<NPCJob.JobType, Image[]> cache = new HashMap<>();

    private SpriteManager() {}

    public static SpriteManager getInstance() {
        if (instance == null) instance = new SpriteManager();
        return instance;
    }

    public Image[] getHarvestFrames(NPCJob.JobType jt) {
        if (cache.containsKey(jt)) return cache.get(jt);
        // Try to load a sprite sheet PNG from assets first
        Image[] frames = loadFramesFromSheet(jt);
        if (frames == null || frames.length == 0) {
            frames = generateHarvestFrames(jt, 4, 64);
        }
        cache.put(jt, frames);
        return frames;
    }

    private Image[] loadFramesFromSheet(NPCJob.JobType jt) {
        try {
            String name = "harvest_" + jt.name().toLowerCase();
            File f = new File("assets/sprites/" + name + ".png");
            if (!f.exists()) return null;
            Image sheet = new Image(f.toURI().toString());
            int h = (int)Math.round(sheet.getHeight());
            int totalW = (int)Math.round(sheet.getWidth());
            if (h <= 0) return null;
            int count = totalW / h;
            Image[] out = new Image[count];
            for (int i = 0; i < count; i++) {
                out[i] = new WritableImage(sheet.getPixelReader(), i * h, 0, h, h);
            }
            return out;
        } catch (Exception e) {
            // Can't load sheet, fall back
            return null;
        }
    }

    private Image[] generateHarvestFrames(NPCJob.JobType jt, int frames, int size) {
        Image[] out = new Image[frames];
        for (int i = 0; i < frames; i++) {
            // If we're on FX thread, use Canvas.snapshot to get anti-aliased vector frames.
            boolean fxThread = false;
            try {
                fxThread = javafx.application.Platform.isFxApplicationThread();
            } catch (Throwable ignored) {}

            if (fxThread) {
                Canvas c = new Canvas(size, size);
                GraphicsContext gc = c.getGraphicsContext2D();
                gc.setFill(Color.web("#000000", 0));
                gc.fillRect(0, 0, size, size);

                double cx = size / 2.0;
                double cy = size / 2.0;

                // Background subtle circle
                gc.setFill(Color.web("#333333", 0.0));
                gc.fillOval(0, 0, size, size);

                // Draw simple tool animation based on type and frame index -> different offsets
                double phase = Math.sin((double)i / frames * Math.PI * 2) * 10;

                switch (jt) {
                    case GATHER_CROPS -> drawScythe(gc, cx, cy, size, phase);
                    case GATHER_WOOD -> drawAxe(gc, cx, cy, size, phase);
                    case GATHER_STONE, GATHER_ORE -> drawPick(gc, cx, cy, size, phase);
                    case GATHER_FISH -> drawRod(gc, cx, cy, size, phase);
                    default -> drawGenericTool(gc, cx, cy, size, phase);
                }

                SnapshotParameters sp = new SnapshotParameters();
                sp.setFill(Color.TRANSPARENT);
                WritableImage img = new WritableImage(size, size);
                c.snapshot(sp, img);
                out[i] = img;
            } else {
                // Not on FX thread: generate a simple placeholder image with PixelWriter
                WritableImage img = new WritableImage(size, size);
                javafx.scene.image.PixelWriter pw = img.getPixelWriter();
                int r = 28, g = 28, b = 28;
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        pw.setArgb(x, y, (255 << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                // Draw a simple colored stroke-like marker for frame
                int cx = size / 2;
                int cy = size / 2;
                int phase = (int)(Math.sin((double)i / frames * Math.PI * 2) * 6);
                int color = (372 << 8) ^ 0xFFAA66; // pseudo color
                // For simplicity, set a few pixels as a crude marker
                for (int a = -6; a <= 6; a++) {
                    int px = cx + a + phase;
                    int py = cy - Math.abs(a);
                    if (px >= 0 && px < size && py >= 0 && py < size) {
                        pw.setArgb(px, py, 0xFFFFFFFF);
                    }
                }
                out[i] = img;
            }
        }
        return out;
    }

    private void drawAxe(GraphicsContext gc, double cx, double cy, double s, double phase) {
        gc.setFill(Color.web("#8B5A2B"));
        gc.fillRect(cx - s * 0.02, cy - s * 0.35, s * 0.04, s * 0.6);
        gc.setFill(Color.web("#B0B0B0"));
        gc.fillRect(cx + s * 0.05 + phase * 0.02, cy - s * 0.28, s * 0.14, s * 0.06);
    }
    private void drawScythe(GraphicsContext gc, double cx, double cy, double s, double phase) {
        gc.setStroke(Color.web("#8B5A2B"));
        gc.setLineWidth(3);
        gc.strokeLine(cx - s * 0.25, cy - s * 0.05, cx + s * 0.25 + phase * 0.02, cy - s * 0.15);
        gc.setStroke(Color.web("#C0C0C0"));
        gc.setLineWidth(3);
        gc.strokeArc(cx + s * 0.05, cy - s * 0.25, s * 0.4, s * 0.3, -90, 120, javafx.scene.shape.ArcType.OPEN);
    }
    private void drawPick(GraphicsContext gc, double cx, double cy, double s, double phase) {
        gc.setFill(Color.web("#8B5A2B"));
        gc.fillRect(cx - s * 0.02, cy - s * 0.35, s * 0.04, s * 0.6);
        gc.setFill(Color.web("#B0B0B0"));
        gc.fillRect(cx - s * 0.18 + phase * 0.02, cy - s * 0.32, s * 0.12, s * 0.06);
        gc.fillRect(cx + s * 0.06 + phase * 0.01, cy - s * 0.18, s * 0.12, s * 0.06);
    }
    private void drawRod(GraphicsContext gc, double cx, double cy, double s, double phase) {
        gc.setStroke(Color.web("#8B5A2B"));
        gc.setLineWidth(2);
        gc.strokeLine(cx - s * 0.25, cy - s * 0.05, cx + s * 0.25, cy - s * 0.35 - phase * 0.02);
        gc.setStroke(Color.web("#000000"));
        gc.setLineWidth(1);
        gc.strokeLine(cx + s * 0.25, cy - s * 0.35 - phase * 0.02, cx + s * 0.30, cy - s * 0.25);
    }
    private void drawGenericTool(GraphicsContext gc, double cx, double cy, double s, double phase) {
        gc.setFill(Color.web("#8B5A2B"));
        gc.fillRect(cx - s * 0.02, cy - s * 0.25, s * 0.04, s * 0.4);
    }
}
