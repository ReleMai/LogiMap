import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;

/**
 * Medieval-style font rendering system for town and location names.
 * Renders stylized text with decorative elements reminiscent of medieval manuscripts.
 * 
 * Features:
 * - Ornate letter styling
 * - Drop shadows and glow effects
 * - Decorative underlines and borders
 * - Size variations for different zoom levels
 */
public class MedievalFont {
    
    // Color palette
    private static final Color GOLD = Color.web("#d4a574");
    private static final Color DARK_GOLD = Color.web("#8b6914");
    private static final Color LIGHT_GOLD = Color.web("#f5e6a3");
    private static final Color PARCHMENT = Color.web("#f4e4bc");
    private static final Color INK_BLACK = Color.web("#1a1510");
    private static final Color INK_BROWN = Color.web("#3d2914");
    private static final Color BLOOD_RED = Color.web("#8b0000");
    private static final Color ROYAL_BLUE = Color.web("#1e3a5f");
    
    // Font names to try (system fonts that look medieval-ish)
    private static final String[] MEDIEVAL_FONTS = {
        "Georgia", "Times New Roman", "Palatino Linotype", "Book Antiqua", 
        "Garamond", "Cambria", "Bookman Old Style", "Serif"
    };
    
    private static String selectedFont = null;
    
    /**
     * Renders a town/city name with medieval styling.
     * 
     * @param gc Graphics context
     * @param text The name to render
     * @param x Center X position
     * @param y Y position (baseline)
     * @param size Font size
     * @param isMajor Whether this is a major city (more ornate)
     */
    public static void renderTownName(GraphicsContext gc, String text, double x, double y, 
                                       double size, boolean isMajor) {
        if (text == null || text.isEmpty()) return;
        
        // Clamp size
        size = Math.max(8, Math.min(size, 48));
        
        // Get font
        Font font = getFont(size, isMajor);
        gc.setFont(font);
        
        // Calculate text width for centering
        double textWidth = estimateTextWidth(text, size);
        double startX = x - textWidth / 2;
        
        // Save state
        gc.save();
        
        if (isMajor) {
            renderMajorCityName(gc, text, startX, y, size, textWidth);
        } else {
            renderMinorTownName(gc, text, startX, y, size, textWidth);
        }
        
        // Restore state
        gc.restore();
    }
    
    /**
     * Renders an ornate major city name with banner and decorations.
     */
    private static void renderMajorCityName(GraphicsContext gc, String text, double x, double y, 
                                            double size, double textWidth) {
        // Draw banner/scroll background
        double bannerPadX = size * 0.8;
        double bannerPadY = size * 0.4;
        double bannerX = x - bannerPadX;
        double bannerY = y - size - bannerPadY;
        double bannerW = textWidth + bannerPadX * 2;
        double bannerH = size + bannerPadY * 2;
        
        // Banner shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(bannerX + 3, bannerY + 3, bannerW, bannerH, 8, 8);
        
        // Banner gradient
        LinearGradient bannerGradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, PARCHMENT),
            new Stop(0.3, Color.web("#e8d8a8")),
            new Stop(0.7, Color.web("#d8c898")),
            new Stop(1, Color.web("#c8b888"))
        );
        gc.setFill(bannerGradient);
        gc.fillRoundRect(bannerX, bannerY, bannerW, bannerH, 8, 8);
        
        // Banner border
        gc.setStroke(DARK_GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(bannerX, bannerY, bannerW, bannerH, 8, 8);
        
        // Inner border
        gc.setStroke(GOLD);
        gc.setLineWidth(1);
        gc.strokeRoundRect(bannerX + 3, bannerY + 3, bannerW - 6, bannerH - 6, 6, 6);
        
        // Draw decorative corners
        drawCornerDecoration(gc, bannerX + 4, bannerY + 4, size * 0.3, true, true);
        drawCornerDecoration(gc, bannerX + bannerW - 4, bannerY + 4, size * 0.3, false, true);
        drawCornerDecoration(gc, bannerX + 4, bannerY + bannerH - 4, size * 0.3, true, false);
        drawCornerDecoration(gc, bannerX + bannerW - 4, bannerY + bannerH - 4, size * 0.3, false, false);
        
        // Text shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillText(text, x + 2, y + 1);
        
        // Main text with gradient
        LinearGradient textGradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, DARK_GOLD),
            new Stop(0.5, INK_BROWN),
            new Stop(1, INK_BLACK)
        );
        gc.setFill(textGradient);
        gc.fillText(text, x, y);
        
        // Draw crown symbol above major cities
        if (size >= 12) {
            drawCrownSymbol(gc, x + textWidth / 2, bannerY - size * 0.2, size * 0.4);
        }
    }
    
    /**
     * Renders a simpler minor town name.
     */
    private static void renderMinorTownName(GraphicsContext gc, String text, double x, double y, 
                                            double size, double textWidth) {
        // Simple background pill
        double padX = size * 0.4;
        double padY = size * 0.2;
        double bgX = x - padX;
        double bgY = y - size - padY + 2;
        double bgW = textWidth + padX * 2;
        double bgH = size + padY * 2;
        
        // Shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillRoundRect(bgX + 2, bgY + 2, bgW, bgH, bgH / 2, bgH / 2);
        
        // Background
        gc.setFill(Color.rgb(30, 25, 20, 0.85));
        gc.fillRoundRect(bgX, bgY, bgW, bgH, bgH / 2, bgH / 2);
        
        // Border
        gc.setStroke(DARK_GOLD);
        gc.setLineWidth(1);
        gc.strokeRoundRect(bgX, bgY, bgW, bgH, bgH / 2, bgH / 2);
        
        // Text shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillText(text, x + 1, y);
        
        // Main text
        gc.setFill(PARCHMENT);
        gc.fillText(text, x, y);
    }
    
    /**
     * Draws a decorative corner flourish.
     */
    private static void drawCornerDecoration(GraphicsContext gc, double x, double y, 
                                             double size, boolean flipX, boolean flipY) {
        gc.save();
        gc.setStroke(DARK_GOLD);
        gc.setLineWidth(1.5);
        
        double dx = flipX ? size : -size;
        double dy = flipY ? size : -size;
        
        // Simple L-shaped flourish
        gc.beginPath();
        gc.moveTo(x, y + dy * 0.3);
        gc.lineTo(x, y);
        gc.lineTo(x + dx * 0.3, y);
        gc.stroke();
        
        // Small curl
        gc.beginPath();
        gc.arc(x + dx * 0.15, y + dy * 0.15, size * 0.1, size * 0.1, 
               flipX == flipY ? 0 : 90, 90);
        gc.stroke();
        
        gc.restore();
    }
    
    /**
     * Draws a small crown symbol for major cities.
     */
    private static void drawCrownSymbol(GraphicsContext gc, double x, double y, double size) {
        gc.save();
        gc.setFill(GOLD);
        gc.setStroke(DARK_GOLD);
        gc.setLineWidth(1);
        
        double w = size;
        double h = size * 0.7;
        
        // Crown base
        double[] xPoints = {
            x - w/2, x - w/3, x - w/6, x, x + w/6, x + w/3, x + w/2,
            x + w/2, x - w/2
        };
        double[] yPoints = {
            y, y - h*0.5, y - h*0.3, y - h, y - h*0.3, y - h*0.5, y,
            y + h*0.2, y + h*0.2
        };
        
        gc.fillPolygon(xPoints, yPoints, 9);
        gc.strokePolygon(xPoints, yPoints, 9);
        
        // Crown jewels (small circles)
        gc.setFill(BLOOD_RED);
        gc.fillOval(x - 2, y - h * 0.7, 4, 4);
        gc.setFill(ROYAL_BLUE);
        gc.fillOval(x - w/3 - 1.5, y - h * 0.35, 3, 3);
        gc.fillOval(x + w/3 - 1.5, y - h * 0.35, 3, 3);
        
        gc.restore();
    }
    
    /**
     * Renders a simple location label (for villages, landmarks, etc).
     */
    public static void renderLabel(GraphicsContext gc, String text, double x, double y, 
                                   double size, Color textColor) {
        if (text == null || text.isEmpty()) return;
        
        size = Math.max(8, Math.min(size, 24));
        Font font = getFont(size, false);
        gc.setFont(font);
        
        double textWidth = estimateTextWidth(text, size);
        double startX = x - textWidth / 2;
        
        // Drop shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillText(text, startX + 1, y + 1);
        
        // Main text
        gc.setFill(textColor);
        gc.fillText(text, startX, y);
    }
    
    /**
     * Renders a region name (larger, more subtle).
     */
    public static void renderRegionName(GraphicsContext gc, String text, double x, double y, 
                                        double size) {
        if (text == null || text.isEmpty()) return;
        
        Font font = Font.font(getSelectedFont(), FontWeight.NORMAL, FontPosture.ITALIC, size);
        gc.setFont(font);
        
        double textWidth = estimateTextWidth(text, size);
        double startX = x - textWidth / 2;
        
        // Very subtle shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillText(text, startX + 1, y + 1);
        
        // Main text - semi-transparent
        gc.setFill(Color.rgb(200, 180, 140, 0.5));
        gc.fillText(text, startX, y);
    }
    
    /**
     * Gets an appropriate font for the given size.
     */
    private static Font getFont(double size, boolean bold) {
        String fontName = getSelectedFont();
        FontWeight weight = bold ? FontWeight.BOLD : FontWeight.SEMI_BOLD;
        return Font.font(fontName, weight, size);
    }
    
    /**
     * Selects the best available font from the medieval fonts list.
     */
    private static String getSelectedFont() {
        if (selectedFont == null) {
            for (String fontName : MEDIEVAL_FONTS) {
                Font testFont = Font.font(fontName, 12);
                if (testFont.getFamily().toLowerCase().contains(fontName.toLowerCase().split(" ")[0])) {
                    selectedFont = fontName;
                    break;
                }
            }
            if (selectedFont == null) {
                selectedFont = "Serif";
            }
        }
        return selectedFont;
    }
    
    /**
     * Estimates the width of text at a given size.
     */
    private static double estimateTextWidth(String text, double size) {
        // Rough estimation - actual width depends on font metrics
        return text.length() * size * 0.55;
    }
}
