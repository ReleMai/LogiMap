import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main application entry point for LogiMap.
 * Handles the flow from main menu -> world gen -> loading -> game.
 */
public class App extends Application {
    
    private Stage primaryStage;
    private StackPane root;
    private MainMenu mainMenu;
    private WorldGenMenu worldGenMenu;
    private LoadingScreen loadingScreen;
    private LogiMapUI gameUI;
    private SettingsMenu settingsMenu;
    
    // World configuration from menu
    private WorldGenMenu.WorldConfig worldConfig;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new StackPane();
        
        // Create and show the main menu
        showMainMenu();
        
        // Create scene
        Scene scene = new Scene(root, 1400, 900);
        
        // Configure stage
        primaryStage.setTitle("LogiMap - Logistics & Supply Chain Simulator");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        // Ensure proper shutdown
        primaryStage.setOnCloseRequest(event -> {
            if (loadingScreen != null) loadingScreen.stop();
            if (mainMenu != null) mainMenu.stop();
            System.exit(0);
        });
    }
    
    /**
     * Shows the main menu.
     */
    private void showMainMenu() {
        if (mainMenu != null) mainMenu.stop();
        
        mainMenu = new MainMenu();
        mainMenu.setOnNewGame(this::showWorldGenMenu);
        mainMenu.setOnLoadGame(this::showLoadMenu);
        mainMenu.setOnSettings(this::showSettings);
        mainMenu.setOnExit(() -> {
            if (mainMenu != null) mainMenu.stop();
            System.exit(0);
        });
        
        root.getChildren().clear();
        root.getChildren().add(mainMenu);
    }
    
    /**
     * Shows the load game menu.
     */
    private void showLoadMenu() {
        SaveLoadMenu loadMenu = new SaveLoadMenu(false);
        loadMenu.setOnLoadWorld(result -> {
            loadSavedWorld(result);
        });
        loadMenu.setOnClose(() -> {
            root.getChildren().remove(loadMenu);
        });
        root.getChildren().add(loadMenu);
    }
    
    /**
     * Loads a saved world.
     */
    private void loadSavedWorld(WorldSaveManager.LoadResult loadResult) {
        // Remove any overlays
        root.getChildren().removeIf(node -> node instanceof SaveLoadMenu);
        
        // Show loading screen
        loadingScreen = new LoadingScreen(System.currentTimeMillis());
        root.getChildren().clear();
        root.getChildren().add(loadingScreen);
        
        Thread loadThread = new Thread(() -> {
            try {
                updateLoadingStatus("Loading saved world...");
                
                WorldSaveManager.LoadResult result = loadResult;
                
                if (result != null && result.world != null) {
                    updateLoadingStatus("Restoring world state...");
                    Thread.sleep(200);
                    
                    Platform.runLater(() -> {
                        loadingScreen.stop();
                        showLoadedGame(result);
                    });
                } else {
                    Platform.runLater(() -> {
                        loadingScreen.setStatus("Failed to load save file");
                        Thread delayThread = new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(this::showMainMenu);
                            } catch (InterruptedException e) {}
                        });
                        delayThread.start();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingScreen.setStatus("Error: " + e.getMessage());
                });
            }
        });
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Shows the loaded game.
     */
    private void showLoadedGame(WorldSaveManager.LoadResult result) {
        if (mainMenu != null) mainMenu.stop();
        
        gameUI = new LogiMapUI(result.world, result.worldName);
        gameUI.setOnReturnToMenu(this::showMainMenu);
        
        root.getChildren().clear();
        gameUI.startWithRoot(primaryStage, root);
        
        // Restore view state
        if (result.viewX != 0 || result.viewY != 0) {
            gameUI.setView(result.viewX, result.viewY, result.zoom);
        }
    }
    
    /**
     * Shows the settings menu.
     */
    private void showSettings() {
        settingsMenu = new SettingsMenu();
        settingsMenu.setOnClose(() -> {
            root.getChildren().remove(settingsMenu);
        });
        root.getChildren().add(settingsMenu);
    }
    
    /**
     * Shows the world generation menu.
     */
    private void showWorldGenMenu() {
        if (mainMenu != null) mainMenu.stop();
        
        worldGenMenu = new WorldGenMenu();
        worldGenMenu.setOnGenerateWorld(this::startWorldGeneration);
        worldGenMenu.setOnBack(this::showMainMenu);
        
        root.getChildren().clear();
        root.getChildren().add(worldGenMenu);
    }
    
    /**
     * Starts the world generation process.
     */
    private void startWorldGeneration(WorldGenMenu.WorldConfig config) {
        this.worldConfig = config;
        
        // Show loading screen
        loadingScreen = new LoadingScreen(config.seed);
        root.getChildren().clear();
        root.getChildren().add(loadingScreen);
        
        // Generate world in background thread
        Thread generationThread = new Thread(() -> {
            try {
                // Update status
                updateLoadingStatus("Creating terrain...");
                Thread.sleep(200); // Small delay to show status
                
                // Create the demo world with the specified seed
                DemoWorld world = createWorld(config);
                
                updateLoadingStatus("Building road networks...");
                Thread.sleep(100);
                
                updateLoadingStatus("Placing structures...");
                Thread.sleep(100);
                
                updateLoadingStatus("Finalizing...");
                Thread.sleep(200);
                
                // Switch to game UI on JavaFX thread
                Platform.runLater(() -> {
                    loadingScreen.stop();
                    showGame(world, config);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingScreen.setStatus("Error: " + e.getMessage());
                });
            }
        });
        
        generationThread.setDaemon(true);
        generationThread.start();
    }
    
    /**
     * Creates the world with the given configuration.
     */
    private DemoWorld createWorld(WorldGenMenu.WorldConfig config) {
        // Set the seed for world generation
        return new DemoWorld(config.worldName, config.seed, config.startX, config.startY);
    }
    
    /**
     * Updates the loading screen status.
     */
    private void updateLoadingStatus(String status) {
        Platform.runLater(() -> {
            if (loadingScreen != null) {
                loadingScreen.setStatus(status);
            }
        });
    }
    
    /**
     * Shows the main game UI.
     */
    private void showGame(DemoWorld world, WorldGenMenu.WorldConfig config) {
        if (mainMenu != null) mainMenu.stop();
        
        gameUI = new LogiMapUI(world, config.worldName);
        gameUI.setOnReturnToMenu(this::showMainMenu);
        
        root.getChildren().clear();
        
        // LogiMapUI expects to set up its own scene, so we call start
        gameUI.startWithRoot(primaryStage, root);
        
        // Center the camera on the starting location
        gameUI.setView(config.startX, config.startY, 0.5);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
