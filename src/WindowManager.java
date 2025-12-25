import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.*;

/**
 * WindowManager - Central system for managing floating game windows.
 * 
 * BLUEPRINT FOR CREATING NEW WINDOWS:
 * ===================================
 * 
 * 1. Create your content panel (extends VBox or any Node):
 *    ```
 *    MyPanel panel = new MyPanel();
 *    ```
 * 
 * 2. Register and create a managed window:
 *    ```
 *    windowManager.registerWindow("myWindow", "My Window Title", panel, 300, 400);
 *    ```
 * 
 * 3. Show/hide/toggle the window:
 *    ```
 *    windowManager.showWindow("myWindow");
 *    windowManager.hideWindow("myWindow");
 *    windowManager.toggleWindow("myWindow");
 *    ```
 * 
 * 4. Optional: Set window position
 *    ```
 *    windowManager.setWindowPosition("myWindow", 100, 150);
 *    ```
 * 
 * 5. Optional: Add close callback
 *    ```
 *    windowManager.setOnWindowClose("myWindow", () -> { ... });
 *    ```
 * 
 * WINDOW FEATURES:
 * - Medieval themed styling
 * - Draggable by title bar
 * - Smooth pop-in/pop-out animations
 * - Auto-stacking (click to bring to front)
 * - Boundary containment within parent
 * - Drop shadow and glow effects
 */
public class WindowManager {
    
    // Medieval theme colors
    private static final String BG_DARKEST = "#0d0906";
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String BG_LIGHTER = "#4a3a20";
    private static final String GOLD = "#c4a574";
    private static final String GOLD_BRIGHT = "#ddc090";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    private static final String BORDER_LIGHT = "#7a6a50";
    private static final String CLOSE_HOVER = "#8a3030";
    
    // Animation settings
    private static final double ANIM_DURATION = 200;
    private static final double SCALE_START = 0.85;
    private static final double SCALE_END = 1.0;
    
    // Window registry
    private final Map<String, ManagedWindow> windows = new HashMap<>();
    private final Pane windowLayer;
    private int zIndex = 0;
    
    /**
     * Creates a WindowManager attached to the given layer pane.
     * @param windowLayer The Pane where windows will be added (should overlay the game)
     */
    public WindowManager(Pane windowLayer) {
        this.windowLayer = windowLayer;
        windowLayer.setPickOnBounds(false);
    }
    
    /**
     * Registers a new managed window.
     * @param id Unique identifier for the window
     * @param title Title shown in the window header
     * @param content The content node to display
     * @param width Preferred width
     * @param height Preferred height (0 for auto)
     */
    public void registerWindow(String id, String title, Node content, double width, double height) {
        if (windows.containsKey(id)) {
            System.out.println("Window '" + id + "' already registered");
            return;
        }
        
        ManagedWindow window = new ManagedWindow(id, title, content, width, height);
        windows.put(id, window);
        windowLayer.getChildren().add(window.container);
        window.container.setVisible(false);
    }
    
    /**
     * Shows a window with animation.
     */
    public void showWindow(String id) {
        ManagedWindow window = windows.get(id);
        if (window == null) return;
        
        window.container.setVisible(true);
        bringToFront(id);
        animateIn(window);
    }
    
    /**
     * Hides a window with animation.
     */
    public void hideWindow(String id) {
        ManagedWindow window = windows.get(id);
        if (window == null) return;
        
        animateOut(window);
    }
    
    /**
     * Toggles window visibility.
     */
    public void toggleWindow(String id) {
        ManagedWindow window = windows.get(id);
        if (window == null) return;
        
        if (window.container.isVisible() && window.container.getOpacity() > 0.5) {
            hideWindow(id);
        } else {
            showWindow(id);
        }
    }
    
    /**
     * Brings a window to the front.
     */
    public void bringToFront(String id) {
        ManagedWindow window = windows.get(id);
        if (window == null) return;
        
        window.container.toFront();
        window.zOrder = ++zIndex;
    }
    
    /**
     * Sets window position.
     */
    public void setWindowPosition(String id, double x, double y) {
        ManagedWindow window = windows.get(id);
        if (window == null) return;
        
        window.container.setLayoutX(x);
        window.container.setLayoutY(y);
    }
    
    /**
     * Gets window position X.
     */
    public double getWindowX(String id) {
        ManagedWindow window = windows.get(id);
        return window != null ? window.container.getLayoutX() : 0;
    }
    
    /**
     * Gets window position Y.
     */
    public double getWindowY(String id) {
        ManagedWindow window = windows.get(id);
        return window != null ? window.container.getLayoutY() : 0;
    }
    
    /**
     * Checks if a window is visible.
     */
    public boolean isWindowVisible(String id) {
        ManagedWindow window = windows.get(id);
        return window != null && window.container.isVisible() && window.container.getOpacity() > 0.5;
    }
    
    /**
     * Sets callback when window is closed.
     */
    public void setOnWindowClose(String id, Runnable callback) {
        ManagedWindow window = windows.get(id);
        if (window != null) {
            window.onClose = callback;
        }
    }
    
    /**
     * Gets the content node for a window (for refreshing etc).
     */
    public Node getWindowContent(String id) {
        ManagedWindow window = windows.get(id);
        return window != null ? window.content : null;
    }
    
    /**
     * Refreshes a window's content if it implements Refreshable.
     */
    public void refreshWindow(String id) {
        ManagedWindow window = windows.get(id);
        if (window != null && window.content instanceof Refreshable) {
            ((Refreshable) window.content).refresh();
        }
    }
    
    /**
     * Refreshes all visible windows.
     */
    public void refreshAllVisible() {
        for (ManagedWindow window : windows.values()) {
            if (window.container.isVisible() && window.content instanceof Refreshable) {
                ((Refreshable) window.content).refresh();
            }
        }
    }
    
    /**
     * Hides all windows.
     */
    public void hideAll() {
        for (String id : windows.keySet()) {
            if (isWindowVisible(id)) {
                hideWindow(id);
            }
        }
    }
    
    // === Animation Methods ===
    
    private void animateIn(ManagedWindow window) {
        VBox container = window.container;
        
        // Set starting state
        container.setScaleX(SCALE_START);
        container.setScaleY(SCALE_START);
        container.setOpacity(0);
        
        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(ANIM_DURATION), container);
        scale.setFromX(SCALE_START);
        scale.setFromY(SCALE_START);
        scale.setToX(SCALE_END);
        scale.setToY(SCALE_END);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        // Fade animation
        FadeTransition fade = new FadeTransition(Duration.millis(ANIM_DURATION), container);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        
        // Play together
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }
    
    private void animateOut(ManagedWindow window) {
        VBox container = window.container;
        
        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(ANIM_DURATION * 0.7), container);
        scale.setToX(SCALE_START);
        scale.setToY(SCALE_START);
        scale.setInterpolator(Interpolator.EASE_IN);
        
        // Fade animation
        FadeTransition fade = new FadeTransition(Duration.millis(ANIM_DURATION * 0.7), container);
        fade.setToValue(0);
        fade.setInterpolator(Interpolator.EASE_IN);
        
        // Play together
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.setOnFinished(e -> {
            container.setVisible(false);
            container.setScaleX(1);
            container.setScaleY(1);
            container.setOpacity(1);
            if (window.onClose != null) window.onClose.run();
        });
        parallel.play();
    }
    
    // === Inner Classes ===
    
    /**
     * Interface for refreshable content panels.
     */
    public interface Refreshable {
        void refresh();
    }
    
    /**
     * Internal window container class.
     */
    private class ManagedWindow {
        String id;
        String title;
        Node content;
        VBox container;
        int zOrder;
        Runnable onClose;
        
        // Drag state
        double dragOffsetX, dragOffsetY;
        boolean isDragging;
        
        ManagedWindow(String id, String title, Node content, double width, double height) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.container = createWindowContainer(width, height);
        }
        
        private VBox createWindowContainer(double width, double height) {
            VBox box = new VBox(0);
            box.setMinWidth(width);
            box.setPrefWidth(width);
            if (height > 0) {
                box.setMinHeight(height);
                box.setPrefHeight(height);
            }
            
            box.setStyle(
                "-fx-background-color: " + BG_MED + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 12;"
            );
            
            // Drop shadow
            DropShadow shadow = new DropShadow();
            shadow.setRadius(25);
            shadow.setColor(Color.color(0, 0, 0, 0.75));
            shadow.setOffsetX(4);
            shadow.setOffsetY(4);
            box.setEffect(shadow);
            
            // Title bar
            HBox titleBar = createTitleBar();
            
            // Content wrapper
            VBox contentWrapper = new VBox();
            contentWrapper.getChildren().add(content);
            contentWrapper.setStyle("-fx-background-color: transparent;");
            VBox.setVgrow(contentWrapper, Priority.ALWAYS);
            
            // Bottom decoration
            HBox bottomDecor = createBottomDecor();
            
            box.getChildren().addAll(titleBar, contentWrapper, bottomDecor);
            
            // Click to bring to front
            box.setOnMousePressed(e -> bringToFront(id));
            
            return box;
        }
        
        private HBox createTitleBar() {
            HBox bar = new HBox();
            bar.setAlignment(Pos.CENTER_LEFT);
            bar.setPadding(new Insets(10, 12, 10, 14));
            bar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_MED + ");" +
                "-fx-background-radius: 10 10 0 0;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 0 0 2 0;"
            );
            bar.setCursor(Cursor.MOVE);
            
            // Left ornament
            Label leftOrn = new Label("⚜");
            leftOrn.setFont(Font.font("Georgia", 12));
            leftOrn.setTextFill(Color.web(GOLD, 0.6));
            
            // Title
            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            titleLabel.setTextFill(Color.web(GOLD_BRIGHT));
            titleLabel.setPadding(new Insets(0, 0, 0, 8));
            
            // Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Close button
            Label closeBtn = createCloseButton();
            
            bar.getChildren().addAll(leftOrn, titleLabel, spacer, closeBtn);
            
            // Drag handling
            bar.setOnMousePressed(e -> {
                isDragging = true;
                dragOffsetX = e.getSceneX() - container.getLayoutX();
                dragOffsetY = e.getSceneY() - container.getLayoutY();
                bringToFront(id);
                e.consume();
            });
            
            bar.setOnMouseDragged(e -> {
                if (isDragging) {
                    double newX = e.getSceneX() - dragOffsetX;
                    double newY = e.getSceneY() - dragOffsetY;
                    
                    // Constrain to parent
                    if (windowLayer != null) {
                        newX = Math.max(0, Math.min(newX, windowLayer.getWidth() - container.getWidth()));
                        newY = Math.max(0, Math.min(newY, windowLayer.getHeight() - container.getHeight()));
                    }
                    
                    container.setLayoutX(newX);
                    container.setLayoutY(newY);
                    e.consume();
                }
            });
            
            bar.setOnMouseReleased(e -> {
                isDragging = false;
                e.consume();
            });
            
            return bar;
        }
        
        private Label createCloseButton() {
            Label btn = new Label("✕");
            btn.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            btn.setTextFill(Color.web(TEXT_DIM));
            btn.setPadding(new Insets(2, 8, 2, 8));
            btn.setCursor(Cursor.HAND);
            btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 4;");
            
            btn.setOnMouseEntered(e -> {
                btn.setTextFill(Color.WHITE);
                btn.setStyle("-fx-background-color: " + CLOSE_HOVER + "; -fx-background-radius: 4;");
            });
            
            btn.setOnMouseExited(e -> {
                btn.setTextFill(Color.web(TEXT_DIM));
                btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 4;");
            });
            
            btn.setOnMouseClicked(e -> {
                hideWindow(id);
                e.consume();
            });
            
            return btn;
        }
        
        private HBox createBottomDecor() {
            HBox decor = new HBox();
            decor.setAlignment(Pos.CENTER);
            decor.setPadding(new Insets(6, 0, 10, 0));
            decor.setStyle("-fx-background-color: " + BG_MED + "; -fx-background-radius: 0 0 10 10;");
            
            Rectangle left = new Rectangle(40, 2);
            left.setFill(Color.web(GOLD, 0.25));
            
            Label center = new Label("◆");
            center.setFont(Font.font("Georgia", 10));
            center.setTextFill(Color.web(GOLD, 0.4));
            center.setPadding(new Insets(0, 8, 0, 8));
            
            Rectangle right = new Rectangle(40, 2);
            right.setFill(Color.web(GOLD, 0.25));
            
            decor.getChildren().addAll(left, center, right);
            return decor;
        }
    }
}
