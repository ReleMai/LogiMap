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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

// Game systems

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
    private VBox menuContentArea; // The area for menu content (can be swapped)
    private Button toggleMenuButton;
    private boolean menuVisible = true;
    private boolean isAnimating = false;
    private static final double MENU_WIDTH = 270;
    private static final double ANIMATION_DURATION = 300;
    private Stage primaryStage; // Reference to main window
    
    // World data
    private DemoWorld world;
    private String worldName;
    
    // Player and gameplay
    private PlayerSprite player;
    private TownInteractionMenu townInteractionMenu;
    private TavernRecruitmentPanel tavernRecruitmentPanel;
    private MarketplaceUI marketplaceUI;
    private EconomySystem economySystem;
    private LoreDialogue loreDialogue;
    private AnimationTimer gameLoop;
    private long lastUpdateTime = 0;
    
    // Floating panels layer for draggable windows
    private Pane floatingPanelLayer;
    private FloatingPanel characterFloatingPanel;
    private FloatingPanel gearFloatingPanel;
    private FloatingPanel inventoryFloatingPanel;
    
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
        this.primaryStage = primaryStage; // Store reference
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
        this.primaryStage = primaryStage; // Store reference
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
            mapCanvas.setOnNPCInteraction(this::handleNPCInteraction);
        } else {
            mapCanvas = new MapCanvas();
            mapCanvas.setOnTownInteraction(this::handleTownInteraction);
            mapCanvas.setOnNPCInteraction(this::handleNPCInteraction);
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
     * If instantTownMenu setting is enabled, skips the dialogue.
     */
    private void handleTownInteraction(Town town) {
        // Check if instant town menu is enabled in settings
        if (GameSettings.getInstance().isInstantTownMenu()) {
            // Skip dialogue and go straight to town menu
            if (townInteractionMenu != null) {
                townInteractionMenu.showForTown(town, player);
            }
            return;
        }
        
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

    private void handleNPCInteraction(NPC npc) {
        if (npc == null || newsTicker == null) return;
        String line = npc.getDialogue();
        String message = npc.getName() + " (" + npc.getType().getName() + "): \"" + line + "\"";
        newsTicker.addNewsItem(message);
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
     * Initializes the bottom section with news ticker.
     * The news ticker includes its own integrated log expansion button.
     */
    private void initializeBottomSection() {
        // News ticker with integrated log button
        newsTicker = new NewsTicker();
        HBox.setHgrow(newsTicker.getContainer(), Priority.ALWAYS);
        
        // Set the news ticker as the bottom section directly
        mainLayout.setBottom(newsTicker.getContainer());
    }
    
    /**
     * Initializes the overlay menu panel that slides in from the left.
     */
    private void initializeOverlayMenu() {
        // Interaction menu with tabbed controls
        interactionMenu = new InteractionMenu();
        
        // Wire up control handlers
        wireControlHandlers();
        wireDevTools();
        
        // Create the themed menu panel
        menuPanel = createThemedMenuPanel();
        
        // Position on left side
        StackPane.setAlignment(menuPanel, Pos.CENTER_LEFT);
        
        // Create economy system
        economySystem = new EconomySystem(world.getSeed());
        // Register all towns
        for (Town town : world.getTowns()) {
            economySystem.registerTown(town);
        }
        
        // Create marketplace UI
        marketplaceUI = new MarketplaceUI();
        marketplaceUI.setOnClose(() -> {
            // Refresh town menu when closing marketplace
            if (townInteractionMenu.isShowing()) {
                // Keep town menu visible
            }
        });
        marketplaceUI.setOnReturnToTown(() -> {
            // Return to town interaction menu
            Town town = marketplaceUI.getCurrentTown();
            if (town != null && player != null) {
                townInteractionMenu.showForTown(town, player);
            }
        });
        
        // Create tavern recruitment panel
        tavernRecruitmentPanel = new TavernRecruitmentPanel();
        tavernRecruitmentPanel.setOnClose(() -> {
            // Return to town interaction menu when closing recruitment
            Town town = tavernRecruitmentPanel.getCurrentTown();
            if (town != null && player != null) {
                townInteractionMenu.showForTown(town, player);
            }
        });
        tavernRecruitmentPanel.setOnHire((npc, success) -> {
            // Handle hire result
            if (success) {
                System.out.println("Hired: " + npc.getName());
            }
        });
        
        // Create town interaction menu
        townInteractionMenu = new TownInteractionMenu();
        townInteractionMenu.setOnTrade(() -> {
            // Open marketplace
            Town town = townInteractionMenu.getCurrentTown();
            if (town != null) {
                townInteractionMenu.close();
                marketplaceUI.show(town, player, economySystem);
            }
        });
        townInteractionMenu.setOnRest(() -> {
            handleResting();
        });
        townInteractionMenu.setOnRecruit(() -> {
            // Open tavern recruitment panel
            Town town = townInteractionMenu.getCurrentTown();
            if (town != null && player != null) {
                townInteractionMenu.close();
                tavernRecruitmentPanel.showForTown(town, player);
            }
        });
        townInteractionMenu.setOnViewInfo(() -> {
            System.out.println("Viewing town info...");
            // TODO: Implement town info view
        });
        townInteractionMenu.setOnWarehouse(() -> {
            Town town = townInteractionMenu.getCurrentTown();
            if (town != null) {
                townInteractionMenu.close();
                showWarehouseUI(town);
            }
        });
        
        // Create lore dialogue for story interactions
        loreDialogue = new LoreDialogue();
        
        // Create floating panel layer for draggable windows (inventory, gear, etc.)
        floatingPanelLayer = new Pane();
        floatingPanelLayer.setPickOnBounds(false); // Let clicks pass through empty areas
        floatingPanelLayer.setMouseTransparent(false);
        
        // Initialize drag overlay system for ghost sprites when dragging items
        DragOverlay.initialize(floatingPanelLayer);
        
        // Initialize floating panels (hidden by default)
        initializeFloatingPanels();
        
        // Add to the center stack (overlays the map)
        // Order matters: menu panel, town interaction, marketplace, recruitment, lore dialogue, then floating panels on top
        centerStack.getChildren().addAll(menuPanel, townInteractionMenu, marketplaceUI, tavernRecruitmentPanel, loreDialogue, floatingPanelLayer);
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
        menuContentArea = new VBox(0);
        menuContentArea.setPrefWidth(MENU_WIDTH);
        menuContentArea.setMaxWidth(MENU_WIDTH);
        
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
        
        menuContentArea.getChildren().addAll(header, interactionContainer);
        
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
        
        contentWithToggle.getChildren().addAll(menuContentArea, toggleWrapper);
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
        
        // Party handler
        interactionMenu.setPartyHandler(() -> openPartyUI());
        
        // Update wallet display
        if (player != null) {
            interactionMenu.updateWallet(player.getCurrency());
        }
    }

    /**
     * Wires developer tool shortcuts for quick testing/cheats.
     */
    private void wireDevTools() {
        interactionMenu.setDevToolHandlers(
            () -> {
                if (player != null) {
                    player.getCurrency().addGold(500);
                    interactionMenu.updateWallet(player.getCurrency());
                }
            },
            () -> {
                if (mapCanvas != null) {
                    mapCanvas.refillEnergyCheat();
                }
            },
            enabled -> GameSettings.getInstance().setTeleportCheatEnabled(enabled)
        );
        interactionMenu.syncTeleportCheat(GameSettings.getInstance().isTeleportCheatEnabled());
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
            mapCanvas.setOnTownInteraction(this::handleTownInteraction);
            mapCanvas.setOnNPCInteraction(this::handleNPCInteraction);
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
    private VBox activeSidePanel; // Currently shown side panel content

    /**
     * Initializes floating panels for character, gear, and inventory.
     */
    private void initializeFloatingPanels() {
        if (player == null) return;
        
        // Character sheet floating panel
        characterSheetUI = new CharacterSheet(player);
        characterFloatingPanel = new FloatingPanel("Character", characterSheetUI);
        characterFloatingPanel.setVisible(false);
        characterFloatingPanel.setLayoutX(100);
        characterFloatingPanel.setLayoutY(100);
        floatingPanelLayer.getChildren().add(characterFloatingPanel);
        
        // Gear sheet floating panel
        gearSheetUI = new GearSheet(player);
        gearSheetUI.setOnGearChanged(() -> {
            if (characterSheetUI != null) characterSheetUI.refresh();
        });
        gearFloatingPanel = new FloatingPanel("Equipment", gearSheetUI);
        gearFloatingPanel.setVisible(false);
        gearFloatingPanel.setLayoutX(350);
        gearFloatingPanel.setLayoutY(100);
        floatingPanelLayer.getChildren().add(gearFloatingPanel);
        
        // Inventory floating panel
        inventoryUI = new InventoryUI(player.getInventory(), player);
        inventoryUI.setOnItemEquipped(item -> {
            // Refresh gear when item is equipped
            if (gearSheetUI != null) gearSheetUI.refresh();
            if (characterSheetUI != null) characterSheetUI.refresh();
        });
        inventoryFloatingPanel = new FloatingPanel("Inventory", inventoryUI);
        inventoryFloatingPanel.setVisible(false);
        inventoryFloatingPanel.setLayoutX(620);
        inventoryFloatingPanel.setLayoutY(100);
        floatingPanelLayer.getChildren().add(inventoryFloatingPanel);
    }
    
    // Party UI and panel
    private PartyUI partyUI;
    private FloatingPanel partyFloatingPanel;
    
    /**
     * Opens the party management UI as a floating panel.
     */
    private void openPartyUI() {
        if (player == null) return;
        
        // Initialize party UI if needed
        if (partyUI == null) {
            partyUI = new PartyUI(player.getParty(), player);
            
            // Create floating panel - PartyUI extends VBox so we can add it directly
            partyFloatingPanel = new FloatingPanel("⚔ Party Management", partyUI);
            partyFloatingPanel.setLayoutX(100);
            partyFloatingPanel.setLayoutY(100);
            floatingPanelLayer.getChildren().add(partyFloatingPanel);
        }
        
        if (partyFloatingPanel.isShowing()) {
            partyFloatingPanel.hide();
        } else {
            partyFloatingPanel.show();
        }
    }

    /**
     * Opens the character sheet as a floating panel.
     */
    private void openCharacterSheet() {
        if (player == null || characterFloatingPanel == null) return;
        
        if (characterFloatingPanel.isShowing()) {
            // Toggle off - hide the panel
            characterFloatingPanel.hide();
        } else {
            characterSheetUI.refresh();
            characterFloatingPanel.show();
        }
    }
    
    /**
     * Opens the gear/equipment sheet as a floating panel.
     */
    private void openGearSheet() {
        if (player == null || gearFloatingPanel == null) return;
        
        if (gearFloatingPanel.isShowing()) {
            // Toggle off - hide the panel
            gearFloatingPanel.hide();
        } else {
            gearSheetUI.refresh();
            gearFloatingPanel.show();
        }
    }

    /**
     * Opens the inventory as a floating panel.
     */
    private void openInventory() {
        if (player == null || inventoryFloatingPanel == null) return;
        
        if (inventoryFloatingPanel.isShowing()) {
            // Toggle off - hide the panel
            inventoryFloatingPanel.hide();
        } else {
            inventoryUI.refresh();
            inventoryFloatingPanel.show();
        }
    }
    
    // Inventory UI reference (moved to floating panels)
    private InventoryUI inventoryUI;
    
    /**
     * Shows a panel in the side menu area, replacing the interaction menu.
     */
    private void showInSidePanel(VBox panel) {
        // Store the active panel
        activeSidePanel = panel;
        
        // Make sure menu is visible
        if (!menuVisible) {
            toggleMenu();
        }
        
        // Replace the content area (not the whole menu structure)
        menuContentArea.getChildren().clear();
        
        // Create header for the side panel
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 10, 8, 10));
        header.setMinHeight(42);
        header.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #3d2f1f, " + MENU_HEADER + ");" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-width: 3 3 0 0;" +
            "-fx-border-radius: 0 12 0 0;" +
            "-fx-background-radius: 0 12 0 0;"
        );
        
        // Back button
        Button backBtn = new Button("← Back");
        backBtn.setStyle(
            "-fx-background-color: #2a1f10;" +
            "-fx-text-fill: #c4a574;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 5 10;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 4;"
        );
        backBtn.setOnAction(e -> closeSidePanel());
        header.getChildren().add(backBtn);
        
        // Content wrapper
        VBox contentWrapper = new VBox(0);
        contentWrapper.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + MENU_BG + ", #1f1a10);" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-width: 0 3 3 0;" +
            "-fx-border-radius: 0 0 12 0;" +
            "-fx-background-radius: 0 0 12 0;" +
            "-fx-padding: 5;"
        );
        contentWrapper.getChildren().add(panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
        VBox.setVgrow(contentWrapper, Priority.ALWAYS);
        
        menuContentArea.getChildren().addAll(header, contentWrapper);
    }
    
    /**
     * Closes the side panel and returns to the interaction menu.
     */
    private void closeSidePanel() {
        activeSidePanel = null;
        
        // Restore the original menu content
        menuContentArea.getChildren().clear();
        
        // Recreate the header
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
        
        // Restore the interaction menu container
        VBox interactionContainer = interactionMenu.getContainer();
        interactionContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + MENU_BG + ", #1f1a10);" +
            "-fx-border-color: " + MENU_BORDER + ";" +
            "-fx-border-width: 0 3 3 0;" +
            "-fx-border-radius: 0 0 12 0;" +
            "-fx-background-radius: 0 0 12 0;" +
            "-fx-padding: 5 0 10 0;"
        );
        VBox.setVgrow(interactionContainer, Priority.ALWAYS);
        
        menuContentArea.getChildren().addAll(header, interactionContainer);
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
     * Handles resting at a town.
     * Free in villages, costs gold in cities.
     * Restores energy and progresses time.
     */
    private void handleResting() {
        if (player == null || mapCanvas == null) return;
        
        Town town = townInteractionMenu.getCurrentTown();
        if (town == null) return;
        
        PlayerEnergy energy = mapCanvas.getPlayerEnergy();
        GameTime gameTime = mapCanvas.getGameTime();
        
        if (energy == null || gameTime == null) return;
        
        // Check if already at full energy
        if (energy.getCurrentEnergy() >= energy.getMaxEnergy()) {
            newsTicker.addNewsItem("You are already fully rested!");
            return;
        }
        
        // Get rest location type
        PlayerEnergy.RestLocation restLocation = town.getRestLocation();
        int cost = restLocation.getCost();
        
        // Check if player can afford
        if (cost > 0 && player.getGold() < cost) {
            newsTicker.addNewsItem("Not enough gold! You need " + cost + " gold to rest here.");
            return;
        }
        
        // Pay cost
        if (cost > 0) {
            player.addGold(-cost);
        }
        
        // Calculate time and energy restoration
        if (restLocation.takesTime()) {
            // Gradual rest - advance time and restore energy
            double energyNeeded = energy.getMaxEnergy() - energy.getCurrentEnergy();
            double ratePerMin = restLocation.getRecoveryPerMinute();
            int minutesNeeded = (int) Math.ceil(energyNeeded / ratePerMin);
            
            // Advance game time
            gameTime.advanceTime(minutesNeeded);
            
            // Restore energy
            energy.fullyRestore();
            
            int hours = minutesNeeded / 60;
            int mins = minutesNeeded % 60;
            String timeStr = hours > 0 ? hours + " hours " + mins + " minutes" : mins + " minutes";
            
            if (cost > 0) {
                newsTicker.addNewsItem("Rested at " + restLocation.getDisplayName() + " for " + timeStr + ". Energy restored! (-" + cost + " gold)");
            } else {
                newsTicker.addNewsItem("Rested at " + restLocation.getDisplayName() + " for " + timeStr + ". Energy restored!");
            }
        } else {
            // Instant restore (city inn)
            energy.fullyRestore();
            newsTicker.addNewsItem("Rested at " + restLocation.getDisplayName() + ". Fully restored! (-" + cost + " gold)");
        }
        
        // Close town menu
        townInteractionMenu.close();
    }
    
    /**
     * Shows the warehouse UI for a specific town.
     */
    private void showWarehouseUI(Town town) {
        if (town == null) return;
        
        TownWarehouse warehouse = town.getWarehouse();
        WarehouseUI warehouseUI = new WarehouseUI(warehouse, player);
        
        warehouseUI.setOnClose(() -> {
            floatingPanelLayer.getChildren().remove(warehouseUI);
        });
        
        warehouseUI.setOnInventoryChanged(() -> {
            // Refresh inventory UI if visible
            if (inventoryUI != null) {
                inventoryUI.refresh();
            }
            // Update wallet display
            interactionMenu.updateWallet(player.getCurrency());
        });
        
        // Position in center
        warehouseUI.setLayoutX((floatingPanelLayer.getWidth() - 350) / 2);
        warehouseUI.setLayoutY((floatingPanelLayer.getHeight() - 400) / 2);
        
        // Make draggable
        final double[] dragOffset = new double[2];
        warehouseUI.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX() - warehouseUI.getLayoutX();
            dragOffset[1] = e.getSceneY() - warehouseUI.getLayoutY();
        });
        warehouseUI.setOnMouseDragged(e -> {
            warehouseUI.setLayoutX(e.getSceneX() - dragOffset[0]);
            warehouseUI.setLayoutY(e.getSceneY() - dragOffset[1]);
        });
        
        floatingPanelLayer.getChildren().add(warehouseUI);
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
