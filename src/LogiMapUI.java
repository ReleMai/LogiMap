import javafx.animation.AnimationTimer;
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
    
    // Player and gameplay
    private PlayerSprite player;
    private TownInteractionMenu townInteractionMenu;
    private LoreDialogue loreDialogue;
    private AnimationTimer gameLoop;
    private long lastUpdateTime = 0;
    
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
            
            // Create player at starting town
            initializePlayer();
            
            // Set up town interaction callback
            mapCanvas.setOnTownInteraction(this::handleTownInteraction);
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
        
        // Start the game loop
        startGameLoop();
    }
    
    /**
     * Initializes the player sprite at the starting town.
     */
    private void initializePlayer() {
        if (world == null) return;
        
        // Get the starting town
        Town startingTown = world.getStartingTown();
        int startX, startY;
        
        if (startingTown != null) {
            // Place player at the center of the starting town
            startX = startingTown.getGridX() + startingTown.getSize() / 2;
            startY = startingTown.getGridY() + startingTown.getSize() / 2;
            System.out.println("Player starting at town: " + startingTown.getName());
        } else {
            // Fallback to world start position
            startX = world.getStartX();
            startY = world.getStartY();
        }
        
        // Create the player sprite
        player = new PlayerSprite("Adventurer", startX, startY);
        mapCanvas.setPlayer(player);
        
        // Store starting position for delayed centering
        final int finalStartX = startX;
        final int finalStartY = startY;
        
        // Delay the view centering until canvas is properly sized
        javafx.application.Platform.runLater(() -> {
            // Use another runLater to ensure layout is complete
            javafx.application.Platform.runLater(() -> {
                mapCanvas.setView(finalStartX, finalStartY, 0.8);
                System.out.println("Camera centered on player at: " + finalStartX + ", " + finalStartY);
            });
        });
    }
    
    /**
     * Starts the game loop for animations and updates.
     */
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastWalletUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }
                
                // Calculate delta time in seconds
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                
                // Cap delta time to prevent large jumps
                deltaTime = Math.min(deltaTime, 0.1);
                
                // Update game state
                mapCanvas.update(deltaTime);
                
                // Update wallet display every 500ms
                if (now - lastWalletUpdate > 500_000_000) {
                    if (player != null && interactionMenu != null) {
                        interactionMenu.updateWallet(player.getCurrency());
                    }
                    lastWalletUpdate = now;
                }
                
                // Render
                mapCanvas.render();
            }
        };
        gameLoop.start();
    }
    
    /**
     * Stops the game loop.
     */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
    
    /**
     * Handles right-click interaction with a town.
     * Shows lore dialogue first, then town menu if player chooses to enter.
     */
    private void handleTownInteraction(Town town) {
        if (loreDialogue != null && player != null) {
            // Generate a lord name for castles (major towns)
            String lordName = generateLordName(town.getName());
            
            if (town.isMajor()) {
                // Major towns are castles - show castle arrival dialogue
                loreDialogue.showCastleArrival(lordName, town.getName(), 
                    () -> {
                        // Player chose to enter - show town menu
                        if (townInteractionMenu != null) {
                            townInteractionMenu.showForTown(town, player);
                        }
                    },
                    () -> {
                        // Player chose to leave - do nothing
                        System.out.println("Player left " + town.getName());
                    }
                );
            } else {
                // Minor towns - show simpler town arrival dialogue
                loreDialogue.showTownArrival(town.getName(), false,
                    () -> {
                        // Player chose to enter - show town menu
                        if (townInteractionMenu != null) {
                            townInteractionMenu.showForTown(town, player);
                        }
                    },
                    () -> {
                        // Player chose to leave - do nothing
                        System.out.println("Player continued past " + town.getName());
                    }
                );
            }
        }
    }
    
    /**
     * Generates a lord name based on the town name.
     */
    private String generateLordName(String townName) {
        // Use the town name seed to generate a consistent lord name
        long seed = townName.hashCode();
        String[] firstNames = {"Aldric", "Edmund", "Godfrey", "Harald", "Leofric", 
                              "Oswald", "Roderick", "Theodric", "Wulfric", "Cedric",
                              "Baldwin", "Conrad", "Dietrich", "Eberhard", "Friedrich"};
        String[] lastNames = {"Blackwood", "Ironforge", "Stormwind", "Ravencrest", "Goldmane",
                             "Silverbane", "Thornwall", "Nightfall", "Dragonheart", "Lionmane"};
        
        int firstIndex = (int) Math.abs(seed % firstNames.length);
        int lastIndex = (int) Math.abs((seed / 100) % lastNames.length);
        
        return firstNames[firstIndex] + " " + lastNames[lastIndex];
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
        
        // Create town interaction menu
        townInteractionMenu = new TownInteractionMenu();
        townInteractionMenu.setOnTrade(() -> {
            System.out.println("Opening marketplace...");
            // TODO: Implement marketplace
        });
        townInteractionMenu.setOnRest(() -> {
            if (player != null && player.getGold() >= 10) {
                player.addGold(-10);
                player.setHealth(player.getMaxHealth());
                System.out.println("Rested at inn. Health restored!");
            }
        });
        townInteractionMenu.setOnRecruit(() -> {
            System.out.println("Opening recruitment...");
            // TODO: Implement recruitment
        });
        townInteractionMenu.setOnViewInfo(() -> {
            System.out.println("Viewing town info...");
            // TODO: Implement town info view
        });
        
        // Create lore dialogue for story interactions
        loreDialogue = new LoreDialogue();
        
        // Add to the center stack (overlays the map)
        // Order matters: menu panel, town interaction, then lore dialogue on top
        centerStack.getChildren().addAll(menuPanel, townInteractionMenu, loreDialogue);
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
        
        // Character handlers
        interactionMenu.setCharacterHandlers(
            () -> openCharacterSheet(),
            () -> openGearSheet(),
            () -> openInventory(),
            () -> openRelationships(),
            () -> openSkills(),
            () -> openJournal()
        );
        
        // Update wallet display
        if (player != null) {
            interactionMenu.updateWallet(player.getCurrency());
        }
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
        switch (tabName) {
            case "Map":
                menuPanel.setVisible(true);
                mapCanvas.setMapMode("Map");
                break;
                
            case "Region":
                menuPanel.setVisible(true);
                mapCanvas.setMapMode("Region");
                break;
                
            default:
                menuPanel.setVisible(true);
                break;
        }
    }
    
    // Character Sheet UI reference
    private CharacterSheet characterSheetUI;
    private GearSheet gearSheetUI;

    /**
     * Opens the character sheet window (stats and attributes).
     */
    private void openCharacterSheet() {
        if (player == null) return;
        
        if (characterSheetUI == null) {
            characterSheetUI = new CharacterSheet(player);
            characterSheetUI.setOnClose(() -> characterSheetUI.setVisible(false));
        }
        
        // Update and show
        characterSheetUI.refresh();
        
        if (!centerStack.getChildren().contains(characterSheetUI)) {
            centerStack.getChildren().add(characterSheetUI);
        }
        
        // Position to the left of center
        double x = (centerStack.getWidth() - characterSheetUI.getPrefWidth()) / 2 - 150;
        double y = (centerStack.getHeight() - characterSheetUI.getPrefHeight()) / 2;
        characterSheetUI.setLayoutX(Math.max(10, x));
        characterSheetUI.setLayoutY(Math.max(10, y));
        
        characterSheetUI.setVisible(true);
        characterSheetUI.toFront();
    }
    
    /**
     * Opens the gear/equipment sheet window.
     */
    private void openGearSheet() {
        if (player == null) return;
        
        if (gearSheetUI == null) {
            gearSheetUI = new GearSheet(player);
            gearSheetUI.setOnClose(() -> gearSheetUI.setVisible(false));
            gearSheetUI.setOnGearChanged(() -> {
                if (characterSheetUI != null) characterSheetUI.refresh();
            });
        }
        
        // Update and show
        gearSheetUI.refresh();
        
        if (!centerStack.getChildren().contains(gearSheetUI)) {
            centerStack.getChildren().add(gearSheetUI);
        }
        
        // Position to the right of center
        double x = (centerStack.getWidth() - gearSheetUI.getPrefWidth()) / 2 + 150;
        double y = (centerStack.getHeight() - gearSheetUI.getPrefHeight()) / 2;
        gearSheetUI.setLayoutX(Math.max(10, x));
        gearSheetUI.setLayoutY(Math.max(10, y));
        
        gearSheetUI.setVisible(true);
        gearSheetUI.toFront();
    }

    // Inventory UI reference
    private InventoryUI inventoryUI;
    private DraggableWindow inventoryWindow;

    /**
     * Opens the inventory window.
     */
    private void openInventory() {
        if (player == null) return;
        
        if (inventoryWindow == null) {
            inventoryUI = new InventoryUI(player.getInventory());
            inventoryUI.setOnClose(() -> inventoryWindow.setVisible(false));
            
            // Wrap in draggable window
            inventoryWindow = new DraggableWindow("🎒 Inventory", 
                inventoryUI.getPrefWidth() + 20, inventoryUI.getPrefHeight() + 50);
            inventoryWindow.setContent(inventoryUI);
            inventoryWindow.setOnClose(() -> inventoryWindow.setVisible(false));
        }
        
        // Refresh and show
        inventoryUI.refresh();
        
        if (!centerStack.getChildren().contains(inventoryWindow)) {
            centerStack.getChildren().add(inventoryWindow);
        }
        
        // Position below center
        double x = (centerStack.getWidth() - inventoryWindow.getPrefWidth()) / 2;
        double y = (centerStack.getHeight() - inventoryWindow.getPrefHeight()) / 2 + 100;
        inventoryWindow.setLayoutX(Math.max(10, x));
        inventoryWindow.setLayoutY(Math.max(10, y));
        
        inventoryWindow.setVisible(true);
        inventoryWindow.toFront();
    }
    
    /**
     * Opens the relationships panel.
     */
    private void openRelationships() {
        // TODO: Implement relationships panel
        newsTicker.addNewsItem("Relationships panel coming soon!");
    }
    
    /**
     * Opens the skills panel.
     */
    private void openSkills() {
        // TODO: Implement skills panel
        newsTicker.addNewsItem("Skills panel coming soon!");
    }
    
    /**
     * Opens the journal.
     */
    private void openJournal() {
        // TODO: Implement journal
        newsTicker.addNewsItem("Journal coming soon!");
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
