import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * A draggable floating panel window for game UI.
 * Used for character sheet, inventory, and gear panels.
 * Matches the medieval theme and allows panels to be moved freely.
 */
public class FloatingPanel extends VBox {
    
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
    
    private String title;
    private Node content;
    private boolean isDragging = false;
    private double dragOffsetX, dragOffsetY;
    private Runnable onClose;
    
    // Title bar elements
    private HBox titleBar;
    private Label titleLabel;
    private Label closeButton;
    
    public FloatingPanel(String title, Node content) {
        this.title = title;
        this.content = content;
        
        setupPanel();
        setupDragging();
    }
    
    private void setupPanel() {
        setMinWidth(200);
        setMaxWidth(400);
        setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 10;"
        );
        
        // Drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setColor(Color.color(0, 0, 0, 0.7));
        shadow.setOffsetX(5);
        shadow.setOffsetY(5);
        setEffect(shadow);
        
        // Title bar
        titleBar = createTitleBar();
        
        // Content wrapper
        VBox contentWrapper = new VBox();
        contentWrapper.getChildren().add(content);
        contentWrapper.setStyle("-fx-background-color: " + BG_MED + ";");
        VBox.setVgrow(contentWrapper, Priority.ALWAYS);
        
        // Bottom border decoration
        HBox bottomDecor = createBottomDecor();
        
        getChildren().addAll(titleBar, contentWrapper, bottomDecor);
    }
    
    private HBox createTitleBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 10, 8, 12));
        bar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_MED + ");" +
            "-fx-background-radius: 8 8 0 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 2 0;"
        );
        bar.setCursor(Cursor.MOVE);
        
        // Left decoration
        Label leftDecor = new Label("◆");
        leftDecor.setFont(Font.font("Georgia", 10));
        leftDecor.setTextFill(Color.web(GOLD, 0.7));
        
        // Title
        titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(GOLD_BRIGHT));
        titleLabel.setPadding(new Insets(0, 10, 0, 8));
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Close button
        closeButton = new Label("✕");
        closeButton.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        closeButton.setTextFill(Color.web(TEXT_DIM));
        closeButton.setPadding(new Insets(2, 8, 2, 8));
        closeButton.setCursor(Cursor.HAND);
        closeButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 4;"
        );
        
        closeButton.setOnMouseEntered(e -> {
            closeButton.setTextFill(Color.WHITE);
            closeButton.setStyle(
                "-fx-background-color: " + CLOSE_HOVER + ";" +
                "-fx-background-radius: 4;"
            );
        });
        
        closeButton.setOnMouseExited(e -> {
            closeButton.setTextFill(Color.web(TEXT_DIM));
            closeButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 4;"
            );
        });
        
        closeButton.setOnMouseClicked(e -> {
            close();
            e.consume();
        });
        
        bar.getChildren().addAll(leftDecor, titleLabel, spacer, closeButton);
        return bar;
    }
    
    private HBox createBottomDecor() {
        HBox decor = new HBox();
        decor.setAlignment(Pos.CENTER);
        decor.setPadding(new Insets(6, 0, 8, 0));
        decor.setStyle("-fx-background-color: " + BG_MED + "; -fx-background-radius: 0 0 8 8;");
        
        Rectangle leftLine = new Rectangle(50, 2);
        leftLine.setFill(Color.web(GOLD, 0.3));
        
        Label centerDecor = new Label("⚜");
        centerDecor.setFont(Font.font("Georgia", 12));
        centerDecor.setTextFill(Color.web(GOLD, 0.5));
        centerDecor.setPadding(new Insets(0, 8, 0, 8));
        
        Rectangle rightLine = new Rectangle(50, 2);
        rightLine.setFill(Color.web(GOLD, 0.3));
        
        decor.getChildren().addAll(leftLine, centerDecor, rightLine);
        return decor;
    }
    
    private void setupDragging() {
        titleBar.setOnMousePressed(e -> {
            isDragging = true;
            dragOffsetX = e.getSceneX() - getLayoutX();
            dragOffsetY = e.getSceneY() - getLayoutY();
            toFront();
            e.consume();
        });
        
        titleBar.setOnMouseDragged(e -> {
            if (isDragging) {
                double newX = e.getSceneX() - dragOffsetX;
                double newY = e.getSceneY() - dragOffsetY;
                
                // Keep within parent bounds
                if (getParent() instanceof Pane) {
                    Pane parent = (Pane) getParent();
                    newX = Math.max(0, Math.min(newX, parent.getWidth() - getWidth()));
                    newY = Math.max(0, Math.min(newY, parent.getHeight() - getHeight()));
                }
                
                setLayoutX(newX);
                setLayoutY(newY);
                e.consume();
            }
        });
        
        titleBar.setOnMouseReleased(e -> {
            isDragging = false;
            e.consume();
        });
    }
    
    public void show() {
        setVisible(true);
        toFront();
        
        // Animate in
        setScaleX(0.9);
        setScaleY(0.9);
        setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), this);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        FadeTransition fade = new FadeTransition(Duration.millis(150), this);
        fade.setToValue(1.0);
        
        scale.play();
        fade.play();
    }
    
    public void close() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), this);
        scale.setToX(0.9);
        scale.setToY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(100), this);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            setVisible(false);
            if (onClose != null) onClose.run();
        });
        
        scale.play();
        fade.play();
    }
    
    public void setOnClose(Runnable handler) {
        this.onClose = handler;
    }
    
    public void setTitle(String title) {
        this.title = title;
        titleLabel.setText(title);
    }
    
    public String getTitle() {
        return title;
    }
    
    public Node getContent() {
        return content;
    }
    
    public boolean isShowing() {
        return isVisible();
    }
    
    /**
     * Hides the panel with an animation.
     */
    public void hide() {
        // Fade out animation
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(150), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> setVisible(false));
        fadeOut.play();
    }
    
    /**
     * Centers this panel within its parent.
     */
    public void centerInParent() {
        if (getParent() instanceof Pane) {
            Pane parent = (Pane) getParent();
            setLayoutX((parent.getWidth() - getWidth()) / 2);
            setLayoutY((parent.getHeight() - getHeight()) / 2);
        }
    }
    
    /**
     * Positions this panel at a specific location.
     */
    public void setPosition(double x, double y) {
        setLayoutX(x);
        setLayoutY(y);
    }
}
