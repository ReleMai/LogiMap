import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

/**
 * JavaFX application that generates PNG sprite sheets from SpriteManager frames
 * and writes them to assets/sprites/harvest_<name>.png
 */
public class SpriteAssetGenerator extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            File outDir = new File("assets/sprites");
            if (!outDir.exists()) outDir.mkdirs();

            SpriteManager sm = SpriteManager.getInstance();
            int size = 64;

            for (NPCJob.JobType jt : NPCJob.JobType.values()) {
                Image[] frames = sm.getHarvestFrames(jt);
                if (frames == null || frames.length == 0) continue;

                int w = (int) Math.round(frames[0].getWidth());
                int h = (int) Math.round(frames[0].getHeight());
                WritableImage sheet = new WritableImage(w * frames.length, h);
                PixelReader pr;
                for (int i = 0; i < frames.length; i++) {
                    Image f = frames[i];
                    pr = f.getPixelReader();
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            sheet.getPixelWriter().setArgb(i * w + x, y, pr.getArgb(x, y));
                        }
                    }
                }

                BufferedImage bi = null;
                try {
                    Class<?> sw = Class.forName("javafx.embed.swing.SwingFXUtils");
                    java.lang.reflect.Method fromFX = sw.getMethod("fromFXImage", javafx.scene.image.Image.class, java.awt.image.BufferedImage.class);
                    bi = (BufferedImage) fromFX.invoke(null, sheet, null);
                } catch (ClassNotFoundException cnf) {
                    // Platform may not include javafx.embed.swing - fallback to manual copy
                    bi = new BufferedImage(w * frames.length, h, BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = bi.createGraphics();
                    PixelReader pr2 = sheet.getPixelReader();
                    for (int yy = 0; yy < h; yy++) {
                        for (int xx = 0; xx < w * frames.length; xx++) {
                            bi.setRGB(xx, yy, pr2.getArgb(xx, yy));
                        }
                    }
                    g2.dispose();
                }

                String fileName = String.format(Locale.ROOT, "harvest_%s.png", jt.name().toLowerCase());
                File out = new File(outDir, fileName);
                ImageIO.write(bi, "png", out);
                System.out.println("Wrote sprite sheet: " + out.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}