import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * A draggable, resizable window component for floating panels.
 * Used for character sheet, gear sheet, inventory, etc.
 */
public class DraggableWindow extends VBox {
    
    // Style constants
    private static final String DARK_BG = "#1a1510";
    private static final String MEDIUM_BG = "#252015";
    private static final String LIGHT_BG = "#352a1a";
    private static final String ACCENT_COLOR = "#c4a574";
    private static final String TEXT_COLOR = "#d4c4a4";
    private static final String BORDER_COLOR = "#5a4a30";
    
    // Window state
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean isDragging = false;
    
    // Components
    private HBox titleBar;
    private Label titleLabel;
    private VBox contentArea;
    private Runnable onClose;
    
    // Size constraints
    private double minWidth = 200;
    private double minHeight = 150;
    
    public DraggableWindow(String title) {
        this(title, 300, 400);
    }
    
    public DraggableWindow(String title, double width, double height) {
        setPrefSize(width, height);
        setMinSize(minWidth, minHeight);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 3, 3);"
        );
        
        // Create title bar
        titleBar = createTitleBar(title);
        
        // Create content area
        contentArea = new VBox();
        contentArea.setPadding(new Insets(8));
        contentArea.setSpacing(8);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        getChildren().addAll(titleBar, contentArea);
        
        // Setup dragging
        setupDragging();
    }
    
    private HBox createTitleBar(String title) {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(6, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 6 6 0 0;" +
            "-fx-cursor: move;"
        );
        
        titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(ACCENT_COLOR));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Close button
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a08060;" +
            "-fx-padding: 2 6;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
            "-fx-background-color: #503030;" +
            "-fx-text-fill: #ff7070;" +
            "-fx-padding: 2 6;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12;"
        ));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a08060;" +
            "-fx-padding: 2 6;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12;"
        ));
        closeBtn.setOnAction(e -> {
            if (onClose != null) onClose.run();
            close();
        });
        
        bar.getChildren().addAll(titleLabel, spacer, closeBtn);
        return bar;
    }
    
    private void setupDragging() {
        titleBar.setOnMousePressed(e -> {
            if (getParent() instanceof Pane) {
                dragOffsetX = e.getSceneX() - getLayoutX();
                dragOffsetY = e.getSceneY() - getLayoutY();
                isDragging = true;
                toFront();
            }
        });
        
        titleBar.setOnMouseDragged(e -> {
            if (isDragging && getParent() instanceof Pane) {
                Pane parent = (Pane) getParent();
                
                double newX = e.getSceneX() - dragOffsetX;
                double newY = e.getSceneY() - dragOffsetY;
                
                // Constrain to parent bounds
                newX = Math.max(0, Math.min(newX, parent.getWidth() - getWidth()));
                newY = Math.max(0, Math.min(newY, parent.getHeight() - getHeight()));
                
                setLayoutX(newX);
                setLayoutY(newY);
            }
        });
        
        titleBar.setOnMouseReleased(e -> {
            isDragging = false;
        });
    }
    
    /**
     * Set content of the window
     */
    public void setContent(Node content) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);
    }
    
    /**
     * Add content to the window
     */
    public void addContent(Node content) {
        contentArea.getChildren().add(content);
    }
    
    /**
     * Get content area for direct access
     */
    public VBox getContentArea() {
        return contentArea;
    }
    
    /**
     * Set the window title
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    
    /**
     * Set close handler
     */
    public void setOnClose(Runnable handler) {
        this.onClose = handler;
    }
    
    /**
     * Close and remove the window
     */
    public void close() {
        if (getParent() instanceof Pane) {
            ((Pane) getParent()).getChildren().remove(this);
        }
    }
    
    /**
     * Show the window at a position
     */
    public void showAt(Pane parent, double x, double y) {
        if (!parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
        setLayoutX(x);
        setLayoutY(y);
        toFront();
        setVisible(true);
    }
    
    /**
     * Center the window in its parent
     */
    public void centerIn(Pane parent) {
        double x = (parent.getWidth() - getPrefWidth()) / 2;
        double y = (parent.getHeight() - getPrefHeight()) / 2;
        showAt(parent, x, y);
    }
    
    /**
     * Static helper to create styled label
     */
    public static Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(TEXT_COLOR));
        return label;
    }
    
    /**
     * Static helper to create section header
     */
    public static Label createHeader(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(ACCENT_COLOR));
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        label.setPadding(new Insets(5, 0, 2, 0));
        return label;
    }
}
