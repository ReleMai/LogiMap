import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main UI class for LogiMap - Logistics & Supply Chain Simulator.
 * Provides the primary application window with map display and control panels.
 */
public class LogiMapUI extends Application {
    
    // Layout components
    private BorderPane mainLayout;
    private StackPane centerStack; // Stack for map + overlay menu
    private TabManager tabManager;
    private MapCanvas mapCanvas;
    private NewsTicker newsTicker;
    private InteractionMenu interactionMenu;
    private VBox menuPanel; // The sliding menu panel
    private Button toggleMenuButton;
    private boolean menuVisible = true;
    private boolean isAnimating = false;
    private static final double MENU_WIDTH = 270;
    private static final double ANIMATION_DURATION = 300;
    
    // World data
    private DemoWorld world;
    private String worldName;
    
    // Callback for returning to main menu
    private Runnable onReturnToMenu;
    
    // Style constants - Medieval/Parchment theme
    private static final String DARK_BG = "#1a1a1a";
    private static final String MEDIUM_BG = "#2a2a2a";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String TEXT_COLOR = "#e0e0e0";
    
    // Menu theme colors
    private static final String MENU_BG = "#2d2418"; // Dark parchment
    private static final String MENU_BORDER = "#8b7355"; // Leather brown
    private static final String MENU_HEADER = "#1a1408"; // Very dark brown
    
    /**
     * Default constructor for standalone launch.
     */
    public LogiMapUI() {
        this.world = null;
        this.worldName = "LogiMap";
    }
    
    /**
     * Constructor with pre-generated world.
     */
    public LogiMapUI(DemoWorld world, String worldName) {
        this.world = world;
        this.worldName = worldName;
    }
    
    @Override
    public void start(Stage primaryStage) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Initialize UI components
        initializeTabBar();
        initializeMapDisplay();
        initializeBottomSection();
        initializeOverlayMenu();
        
        // Create scene
        Scene scene = new Scene(mainLayout, 1400, 900);
        scene.getStylesheets().add(createStylesheet());
        
        // Configure stage
        primaryStage.setTitle("LogiMap - " + worldName);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setOnShown(e -> mapCanvas.onStageReady());
        primaryStage.show();
    }
    
    /**
     * Starts the UI within an existing root pane.
     */
    public void startWithRoot(Stage primaryStage, StackPane root) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Initialize UI components
        initializeTabBar();
        initializeMapDisplayWithWorld();
        initializeBottomSection();
        initializeOverlayMenu();
        
        // Add to root
        root.getChildren().add(mainLayout);
        
        // Update stage title
        primaryStage.setTitle("LogiMap - " + worldName);
        
        // Bind to root size
        mainLayout.prefWidthProperty().bind(root.widthProperty());
        mainLayout.prefHeightProperty().bind(root.heightProperty());
        
        // Trigger stage ready
        mapCanvas.onStageReady();
    }
    
    /**
     * Initializes the top tab bar for mode selection.
     */
    private void initializeTabBar() {
        tabManager = new TabManager(this);
        mainLayout.setTop(tabManager.getTabBar());
    }
    
    /**
     * Initializes the central map display area.
     */
    private void initializeMapDisplay() {
        mapCanvas = new MapCanvas();
        javafx.scene.canvas.Canvas canvas = mapCanvas.getCanvas();
        
        StackPane mapContainer = new StackPane(canvas);
        mapContainer.setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Bind canvas to container size
        canvas.widthProperty().bind(mapContainer.widthProperty());
        canvas.heightProperty().bind(mapContainer.heightProperty());
        
        // Create center stack that will hold map and overlay menu
        centerStack = new StackPane(mapContainer);
        mainLayout.setCenter(centerStack);
    }
    
    /**
     * Initializes the central map display area with a pre-generated world.
     */
    private void initializeMapDisplayWithWorld() {
        if (world != null) {
            mapCanvas = new MapCanvas(world);
        } else {
            mapCanvas = new MapCanvas();
        }
        javafx.scene.canvas.Canvas canvas = mapCanvas.getCanvas();
        
        StackPane mapContainer = new StackPane(canvas);
        mapContainer.setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Bind canvas to container size
        canvas.widthProperty().bind(mapContainer.widthProperty());
        canvas.heightProperty().bind(mapContainer.heightProperty());
        
        // Create center stack that will hold map and overlay menu
        centerStack = new StackPane(mapContainer);
        mainLayout.setCenter(centerStack);
    }
    
    /**
     * Initializes the bottom section with news ticker and controls.
     */
    private void initializeBottomSection() {
        HBox bottomSection = new HBox(10);
        bottomSection.setPadding(new Insets(5, 10, 5, 10));
        bottomSection.setAlignment(Pos.CENTER_LEFT);
        bottomSection.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        // News ticker
        newsTicker = new NewsTicker();
        HBox.setHgrow(newsTicker.getContainer(), Priority.ALWAYS);
        
        // Log viewer button
        Button logButton = createStyledButton("LOG", LIGHT_BG, TEXT_COLOR);
        logButton.setOnAction(e -> openLogViewer());
        
        bottomSection.getChildren().addAll(newsTicker.getContainer(), logButton);
        mainLayout.setBottom(bottomSection);
    }
    
    /**
     * Initializes the overlay menu panel that slides in from the left.
     */
    private void initializeOverlayMenu() {
        // Interaction menu with tabbed controls
        interactionMenu = new InteractionMenu();
        
        // Wire up control handlers
        wireControlHandlers();
        
        // Create the themed menu panel
        menuPanel = createThemedMenuPanel();
        
        // Position on left side
        StackPane.setAlignment(menuPanel, Pos.CENTER_LEFT);
        
        // Add to the center stack (overlays the map)
        centerStack.getChildren().add(menuPanel);
    }
    
    /**
     * Creates a medieval-themed menu panel with integrated toggle tab.
     */
    private VBox createThemedMenuPanel() {
        VBox panel = new VBox(0);
        panel.setMaxWidth(MENU_WIDTH + 30); // Extra for toggle tab
        panel.setPrefWidth(MENU_WIDTH + 30);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        panel.setPickOnBounds(false);
        
        // Main content area (header + menu in an HBox with toggle)
        HBox contentWithToggle = new HBox(0);
        contentWithToggle.setAlignment(Pos.TOP_LEFT);
        contentWithToggle.setPickOnBounds(false);
        
        // Left side: the actual menu content
        VBox menuContent = new VBox(0);
        menuContent.setPrefWidth(MENU_WIDTH);
        menuContent.setMaxWidth(MENU_WIDTH);
        
        // Header with title
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setMinHeight(42);
        header.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #3d2f1f, " + MENU_HEADER + ");" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-width: 3 3 0 0;" +
            "-fx-border-radius: 0 12 0 0;" +
            "-fx-background-radius: 0 12 0 0;"
        );
        
        Label titleLabel = new Label("⚔  CONTROLS  ⚔");
        titleLabel.setStyle(
            "-fx-text-fill: #c4a574;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );
        header.getChildren().add(titleLabel);
        
        // Menu body
        VBox interactionContainer = interactionMenu.getContainer();
        interactionContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + MENU_BG + ", #1f1a10);" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-width: 0 3 3 0;" +
            "-fx-border-radius: 0 0 12 0;" +
            "-fx-background-radius: 0 0 12 0;" +
            "-fx-padding: 5 0 10 0;"
        );
        interactionContainer.setPrefWidth(MENU_WIDTH);
        interactionContainer.setMaxWidth(MENU_WIDTH);
        VBox.setVgrow(interactionContainer, Priority.ALWAYS);
        
        menuContent.getChildren().addAll(header, interactionContainer);
        
        // Right side: toggle button
        toggleMenuButton = new Button("◀");
        toggleMenuButton.setPrefWidth(28);
        toggleMenuButton.setPrefHeight(90);
        toggleMenuButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #3d2f1f, " + MENU_HEADER + ");" +
            "-fx-text-fill: #c4a574;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5;" +
            "-fx-background-radius: 0 8 8 0;" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-width: 3 3 3 0;" +
            "-fx-border-radius: 0 8 8 0;" +
            "-fx-cursor: hand;"
        );
        toggleMenuButton.setOnAction(e -> toggleMenu());
        
        // Wrap toggle in VBox to center it vertically
        VBox toggleWrapper = new VBox(toggleMenuButton);
        toggleWrapper.setAlignment(Pos.CENTER);
        toggleWrapper.setPadding(new Insets(50, 0, 0, 0));
        toggleWrapper.setPickOnBounds(false);
        
        contentWithToggle.getChildren().addAll(menuContent, toggleWrapper);
        panel.getChildren().add(contentWithToggle);
        
        return panel;
    }
    
    /**
     * Toggles the menu visibility with a slide animation.
     */
    private void toggleMenu() {
        if (isAnimating) return;
        isAnimating = true;
        
        TranslateTransition transition = new TranslateTransition(Duration.millis(ANIMATION_DURATION), menuPanel);
        
        if (menuVisible) {
            // Slide out to hide (keep toggle visible)
            transition.setToX(-MENU_WIDTH);
            transition.setOnFinished(e -> {
                menuVisible = false;
                toggleMenuButton.setText("▶");
                isAnimating = false;
            });
        } else {
            // Slide in to show
            transition.setToX(0);
            transition.setOnFinished(e -> {
                menuVisible = true;
                toggleMenuButton.setText("◀");
                isAnimating = false;
            });
        }
        
        transition.play();
    }
    
    /**
     * Wires up control handlers from the interaction menu to map canvas.
     */
    private void wireControlHandlers() {
        // Grid control handlers
        interactionMenu.setGridControlHandlers(
            factor -> mapCanvas.setGridBrightness(factor),
            () -> mapCanvas.toggleGridNumbers()
        );
        
        // Filter handlers
        interactionMenu.setFilterHandlers(
            () -> mapCanvas.setMapFilter(new StandardFilter()),
            () -> mapCanvas.setMapFilter(new TopographicalFilter()),
            () -> mapCanvas.setMapFilter(new ResourceHeatmapFilter(mapCanvas.getWorld().getResourceMap()))
        );
        
        // Save/Load handlers
        interactionMenu.setSaveLoadHandlers(
            () -> showSaveMenu(),
            () -> showLoadMenu()
        );
        
        // Main Menu handler
        interactionMenu.setMainMenuHandler(() -> returnToMainMenu());
    }
    
    /**
     * Shows the save world menu.
     */
    private void showSaveMenu() {
        SaveLoadMenu saveMenu = new SaveLoadMenu(true);
        saveMenu.setCurrentWorld(
            mapCanvas.getWorld(),
            worldName,
            mapCanvas.getViewX(),
            mapCanvas.getViewY(),
            mapCanvas.getZoom()
        );
        saveMenu.setOnClose(() -> {
            StackPane parent = (StackPane) mainLayout.getParent();
            if (parent != null) {
                parent.getChildren().removeIf(node -> node instanceof SaveLoadMenu);
            }
        });
        
        StackPane parent = (StackPane) mainLayout.getParent();
        if (parent != null) {
            parent.getChildren().add(saveMenu);
        }
    }
    
    /**
     * Shows the load world menu.
     */
    private void showLoadMenu() {
        SaveLoadMenu loadMenu = new SaveLoadMenu(false);
        loadMenu.setOnLoadWorld(result -> {
            // Update world and UI
            this.world = result.world;
            this.worldName = result.worldName;
            
            // Reinitialize map with new world
            mapCanvas = new MapCanvas(world);
            javafx.scene.canvas.Canvas canvas = mapCanvas.getCanvas();
            
            StackPane mapContainer = new StackPane(canvas);
            mapContainer.setStyle("-fx-background-color: " + DARK_BG + ";");
            
            // Bind canvas to container size
            canvas.widthProperty().bind(mapContainer.widthProperty());
            canvas.heightProperty().bind(mapContainer.heightProperty());
            
            // Update the center stack - keep menu panel on top
            centerStack.getChildren().clear();
            centerStack.getChildren().addAll(mapContainer, menuPanel);
            
            // Set view position
            mapCanvas.setView(result.viewX, result.viewY, result.zoom);
            
            // Re-wire controls
            wireControlHandlers();
            
            // Close menu
            StackPane parent = (StackPane) mainLayout.getParent();
            if (parent != null) {
                parent.getChildren().removeIf(node -> node instanceof SaveLoadMenu);
            }
            
            // Update title
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            if (stage != null) {
                stage.setTitle("LogiMap - " + worldName);
            }
            
            newsTicker.addNewsItem("World loaded: " + worldName);
        });
        loadMenu.setOnClose(() -> {
            StackPane parent = (StackPane) mainLayout.getParent();
            if (parent != null) {
                parent.getChildren().removeIf(node -> node instanceof SaveLoadMenu);
            }
        });
        
        StackPane parent = (StackPane) mainLayout.getParent();
        if (parent != null) {
            parent.getChildren().add(loadMenu);
        }
    }
    
    /**
     * Opens the notification log viewer window.
     */
    private void openLogViewer() {
        Stage logStage = new Stage();
        logStage.setTitle("Notification Log");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: " + DARK_BG + ";");
        
        Label title = new Label("Notification History");
        title.setStyle(
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );
        
        TextArea logArea = new TextArea(newsTicker.getLogHistory());
        logArea.setEditable(false);
        logArea.setStyle(
            "-fx-control-inner-background: " + MEDIUM_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";"
        );
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        Button closeButton = createStyledButton("Close", ACCENT_COLOR, "white");
        closeButton.setOnAction(e -> logStage.close());
        
        layout.getChildren().addAll(title, logArea, closeButton);
        logStage.setScene(new Scene(layout, 500, 400));
        logStage.show();
    }
    
    /**
     * Creates a styled button with consistent appearance.
     */
    private Button createStyledButton(String text, String bgColor, String textColor) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 5 15 5 15;"
        );
        return button;
    }
    
    /**
     * Switches the active tab and updates UI accordingly.
     */
    public void switchTab(String tabName) {
        boolean showControls = tabName.equals("Local") || tabName.equals("Region");
        
        if (showControls) {
            menuPanel.setVisible(true);
        } else {
            menuPanel.setVisible(false);
        }
        
        mapCanvas.setMapMode(tabName);
    }
    
    /**
     * Creates the application stylesheet.
     */
    private String createStylesheet() {
        return "data:text/css," +
            ".button:hover { -fx-opacity: 0.85; }" +
            ".tab-button:hover { -fx-background-color: " + LIGHT_BG + "; }";
    }
    
    /**
     * Sets the callback for returning to main menu.
     */
    public void setOnReturnToMenu(Runnable callback) {
        this.onReturnToMenu = callback;
    }
    
    /**
     * Returns to the main menu.
     */
    public void returnToMainMenu() {
        if (onReturnToMenu != null) {
            onReturnToMenu.run();
        }
    }
    
    /**
     * Sets the map view position and zoom.
     */
    public void setView(int viewX, int viewY, double zoom) {
        if (mapCanvas != null) {
            mapCanvas.setView(viewX, viewY, zoom);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
