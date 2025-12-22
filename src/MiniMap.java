import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Mini-map widget displaying an overview of the entire world map.
 * Shows the current viewport position and allows quick navigation
 * by clicking on the mini-map.
 */
public class MiniMap {
    
    private VBox container;
    private Canvas miniCanvas;
    private GraphicsContext gc;
    private HBox controls;
    
    private static final int MINI_SIZE = 180;
    private static final Color BG_COLOR = Color.web("#2a2a2a");
    private static final Color BORDER_COLOR = Color.web("#4a9eff");
    private static final Color GRID_COLOR = Color.web("#505050");
    private static final Color VIEWPORT_COLOR = Color.web("#4a9eff", 0.4);
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String TEXT_COLOR = "#e0e0e0";
    
    public MiniMap() {
        container = new VBox(5);
        container.setPadding(new Insets(10));
        container.setStyle(
            "-fx-background-color: rgba(42, 42, 42, 0.9);" +
            "-fx-border-color: #4a9eff;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;"
        );
        
        // Create minimap canvas
        miniCanvas = new Canvas(MINI_SIZE, MINI_SIZE);
        gc = miniCanvas.getGraphicsContext2D();
        
        // Create control buttons
        createControls();
        
        container.getChildren().addAll(miniCanvas, controls);
        
        // Initial render
        render();
    }
    
    private void createControls() {
        controls = new HBox(5);
        controls.setAlignment(Pos.CENTER);
        
        Button zoomInBtn = createControlButton("+");
        Button zoomOutBtn = createControlButton("-");
        Button centerBtn = createControlButton("⊙");
        Button fitBtn = createControlButton("⊡");
        
        zoomInBtn.setOnAction(e -> System.out.println("Zoom In")); // Connect to MapCanvas later
        zoomOutBtn.setOnAction(e -> System.out.println("Zoom Out"));
        centerBtn.setOnAction(e -> System.out.println("Center View"));
        fitBtn.setOnAction(e -> System.out.println("Fit to Screen"));
        
        controls.getChildren().addAll(zoomInBtn, zoomOutBtn, centerBtn, fitBtn);
    }
    
    private Button createControlButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 5 10 5 10;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + ACCENT_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 5 10 5 10;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 5 10 5 10;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }
    
    private void render() {
        // Clear
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, MINI_SIZE, MINI_SIZE);
        
        // Draw simplified grid
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);
        
        int cellSize = 20;
        for (int x = 0; x < MINI_SIZE; x += cellSize) {
            for (int y = 0; y < MINI_SIZE; y += cellSize) {
                gc.strokeRect(x, y, cellSize, cellSize);
            }
        }
        
        // Draw viewport indicator (center rectangle showing current view)
        gc.setFill(VIEWPORT_COLOR);
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(2);
        double viewportSize = MINI_SIZE * 0.4;
        double viewportX = (MINI_SIZE - viewportSize) / 2;
        double viewportY = (MINI_SIZE - viewportSize) / 2;
        gc.fillRect(viewportX, viewportY, viewportSize, viewportSize);
        gc.strokeRect(viewportX, viewportY, viewportSize, viewportSize);
        
        // Draw crosshair at center
        gc.setStroke(Color.web("#4a9eff"));
        gc.setLineWidth(1);
        gc.strokeLine(MINI_SIZE / 2, MINI_SIZE / 2 - 5, MINI_SIZE / 2, MINI_SIZE / 2 + 5);
        gc.strokeLine(MINI_SIZE / 2 - 5, MINI_SIZE / 2, MINI_SIZE / 2 + 5, MINI_SIZE / 2);
    }
    
    public void updateViewport(double viewportX, double viewportY, double viewportWidth, double viewportHeight) {
        // This will be called by MapCanvas to update the minimap
        render();
    }
    
    public VBox getContainer() {
        return container;
    }
}
