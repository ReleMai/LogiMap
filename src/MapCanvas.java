import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;
import java.util.function.Consumer;

/**
 * Canvas component for rendering the game map with terrain, structures, and roads.
 * Supports panning, zooming, and interactive selection.
 * 
 * Performance optimizations:
 * - Level of Detail (LOD) rendering at low zoom levels
 * - Tile batching to reduce draw calls
 * - Cached terrain colors
 */
public class MapCanvas {
    
    // Canvas components
    private final Canvas canvas;
    private final GraphicsContext gc;
    
    // View state
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;
    private static final int GRID_SIZE = 50;
    private String mapMode = "Local";
    
    // World data
    private final DemoWorld world;
    private MapFilter currentFilter;
    private NPCManager npcManager;
    
    // Interaction state
    private double lastMouseX;
    private double lastMouseY;
    private boolean isDragging = false;
    private MapStructure hoveredStructure = null;
    private MapStructure selectedStructure = null;
    private double dragStartX;
    private double dragStartY;
    private static final double CLICK_THRESHOLD = 5.0; // Pixels to distinguish click from drag
    
    // Player and movement
    private PlayerSprite player;
    private MovementFlag movementFlag;
    private Consumer<Town> onTownInteraction;  // Callback when player arrives at a town
    private Consumer<NPC> onNPCInteraction;    // Callback when player interacts with an NPC
    private Town pendingTownInteraction = null;  // Town to interact with when player arrives
    private NPC pendingNPCInteraction = null;    // NPC to talk to when player arrives
    private ResourceNodeBase pendingResourceInteraction = null;  // Resource node to interact with
    private FarmlandNode pendingFarmlandInteraction = null;  // Farmland node (separate from ResourceNodeBase)
    
    // Game systems
    private GameTime gameTime;
    private PlayerEnergy playerEnergy;
    private FarmlandInteraction farmlandInteraction;
    private ResourceInteraction resourceInteraction;  // Generic resource interaction menu
    private ActionProgress actionProgress;  // Timed action progress system
    private TerrainRenderer terrainRenderer;  // Professional terrain rendering
    
    // Tooltip state
    private boolean ctrlHeld = false;
    private int tooltipGridX = -1;
    private int tooltipGridY = -1;
    private double tooltipScreenX = 0;
    private double tooltipScreenY = 0;
    
    // Grid display options
    private double gridBrightness = 1.0;
    private boolean showGridNumbers = false;
    
    // Camera follow state
    private boolean cameraFollowPlayer = false;  // Whether camera is following player
    private static final double CAMERA_FOLLOW_SMOOTHNESS = 5.0;  // Lower = smoother
    
    // Style constants
    private static final Color GRID_LINE_COLOR = Color.web("#505050");
    private static final Color BG_COLOR = Color.web("#1a1a1a");
    private static final Color TEXT_COLOR = Color.web("#e0e0e0");
    private static final Color TOOLTIP_BG = Color.web("rgba(20, 20, 30, 0.92)");
    private static final Color TOOLTIP_BORDER = Color.web("#4a9eff");
    
    // Zoom limits
    private static final double MIN_ZOOM = 0.01;
    private static final double MAX_ZOOM = 20.0;
    private static final double GRID_FADE_START = 0.3;
    private static final double GRID_FADE_END = 0.7;
    
    // LOD thresholds for performance optimization
    private static final double LOD_THRESHOLD_1 = 0.15;  // Very low zoom - render in blocks
    private static final double LOD_THRESHOLD_2 = 0.08;  // Ultra low zoom - larger blocks
    private static final double LOD_THRESHOLD_3 = 0.04;  // Minimal zoom - very large blocks
    
    /**
     * Creates a MapCanvas with a new default world.
     */
    public MapCanvas() {
        this(new DemoWorld());
    }
    
    /**
     * Creates a MapCanvas with a pre-generated world.
     */
    public MapCanvas(DemoWorld world) {
        canvas = new Canvas(1000, 700);
        gc = canvas.getGraphicsContext2D();
        
        this.world = world;
        currentFilter = new StandardFilter();
        npcManager = new NPCManager();
        npcManager.populateAllTowns(world.getTowns());
        npcManager.spawnRoamers(world.getTowns());
        
        // Initialize movement flag
        this.movementFlag = new MovementFlag();
        
        // Initialize terrain renderer with world seed
        this.terrainRenderer = new TerrainRenderer(world.getSeed());
        
        // Initialize game systems
        this.gameTime = new GameTime();
        this.playerEnergy = new PlayerEnergy();
        this.farmlandInteraction = new FarmlandInteraction(gameTime, playerEnergy);
        this.resourceInteraction = new ResourceInteraction(gameTime, playerEnergy);
        this.actionProgress = new ActionProgress(gameTime);
        
        // Set up action progress with player reference for movement lock
        this.actionProgress.setPlayer(player);
        
        // Set up resource interaction to use action progress
        this.resourceInteraction.setActionProgress(actionProgress);
        
        // Set up farmland interaction to use action progress (timer)
        this.farmlandInteraction.setActionProgress(actionProgress);
        
        // Set up farmland harvest callback
        this.farmlandInteraction.setHarvestCallback((farmland, yield, grainType) -> {
            System.out.println("Harvested " + yield + " " + grainType.getDisplayName() + "!");
            // Add grain to player inventory
            if (player != null) {
                String itemId = "grain_" + grainType.name().toLowerCase();
                ItemStack stack = ItemRegistry.createStack(itemId, yield);
                if (stack != null) {
                    ItemStack remaining = player.getInventory().addItem(stack);
                    if (remaining != null && !remaining.isEmpty()) {
                        System.out.println("Inventory full! " + remaining.getQuantity() + " " + grainType.getDisplayName() + " dropped.");
                    }
                }
            }
        });
        
        // Set up generic resource harvest callback
        this.resourceInteraction.setHarvestCallback((node, yield, itemId) -> {
            System.out.println("Gathered " + yield + " " + node.getDisplayName() + "!");
            // Add resource to player inventory
            if (player != null) {
                ItemStack stack = ItemRegistry.createStack(itemId, yield);
                if (stack != null) {
                    ItemStack remaining = player.getInventory().addItem(stack);
                    if (remaining != null && !remaining.isEmpty()) {
                        System.out.println("Inventory full! " + remaining.getQuantity() + " items dropped.");
                    }
                }
            }
        });
        
        setupEventHandlers();
        setupResizeListeners();
        render();
    }
    
    // ==================== Player Management ====================
    
    /**
     * Sets the player sprite to render and control.
     */
    public void setPlayer(PlayerSprite player) {
        this.player = player;
    }
    
    /**
     * Gets the current player sprite.
     */
    public PlayerSprite getPlayer() {
        return player;
    }
    
    /**
     * Sets the callback for town right-click interactions.
     */
    public void setOnTownInteraction(Consumer<Town> handler) {
        this.onTownInteraction = handler;
    }

    /**
     * Sets the callback for interacting with NPCs.
     */
    public void setOnNPCInteraction(Consumer<NPC> handler) {
        this.onNPCInteraction = handler;
    }
    
    /**
     * Updates player and flag animations. Call this from a game loop.
     * Also advances game time constantly.
     */
    public void update(double deltaTime) {
        // Sync game speed from settings to GameTime
        if (gameTime != null) {
            double settingsSpeed = GameSettings.getInstance().getGameSpeed();
            if (Math.abs(gameTime.getTimeMultiplier() - settingsSpeed) > 0.01) {
                gameTime.setTimeMultiplier(settingsSpeed);
            }
        }
        
        // Update game time (constant flow)
        if (gameTime != null && gameTime.isConstantFlow()) {
            gameTime.update(deltaTime * 1000); // Convert to milliseconds
        }
        
        // Don't update movement or animations when paused
        if (gameTime != null && gameTime.isPaused()) {
            return;
        }
        
        // Update terrain renderer animations (water waves, etc.)
        if (terrainRenderer != null) {
            terrainRenderer.update(deltaTime);
        }
        
        // Update action progress
        if (actionProgress != null) {
            actionProgress.update(deltaTime);
        }
        
        if (player != null) {
            // Update player's current terrain for swimming detection
            if (world != null && world.getTerrain() != null) {
                int px = (int) player.getGridX();
                int py = (int) player.getGridY();
                TerrainType[][] terrainMap = world.getTerrain().getTerrainMap();
                if (px >= 0 && px < terrainMap.length && py >= 0 && py < terrainMap[0].length) {
                    player.setCurrentTerrain(terrainMap[px][py]);
                }
            }
            
            boolean wasMoving = player.isMoving();
            double oldX = player.getGridX();
            double oldY = player.getGridY();
            player.update(deltaTime);
            
            // Calculate distance moved this frame and apply energy cost
            if (wasMoving && playerEnergy != null) {
                double newX = player.getGridX();
                double newY = player.getGridY();
                double tilesMoved = Math.sqrt((newX - oldX) * (newX - oldX) + (newY - oldY) * (newY - oldY));
                
                if (tilesMoved > 0.001) {
                    if (player.isSwimming()) {
                        // Deep water swimming - 3 energy per tile
                        playerEnergy.consumeSwimmingEnergy(tilesMoved);
                    } else if (player.isInShallowWater()) {
                        // Wading in shallow water - free
                        playerEnergy.consumeWadingEnergy(tilesMoved);
                    } else {
                        // Normal walking
                        playerEnergy.consumeWalkingEnergy(tilesMoved);
                    }
                }
            }
            
            // Camera follow: smoothly follow player when enabled and player is moving
            if (cameraFollowPlayer && player.isMoving()) {
                smoothFollowPlayer(deltaTime);
            }
            
            // Check if player just stopped moving and has a pending town interaction
            if (wasMoving && !player.isMoving() && pendingTownInteraction != null) {
                // Check if player is at or near the town
                if (isPlayerAtTown(pendingTownInteraction)) {
                    Town town = pendingTownInteraction;
                    pendingTownInteraction = null;
                    
                    // Trigger the town interaction callback
                    if (onTownInteraction != null) {
                        onTownInteraction.accept(town);
                    }
                }
            }

            // Check if player just stopped moving and has a pending NPC interaction
            if (wasMoving && !player.isMoving() && pendingNPCInteraction != null) {
                if (isPlayerNearNPC(pendingNPCInteraction)) {
                    NPC npc = pendingNPCInteraction;
                    pendingNPCInteraction = null;
                    triggerNPCInteraction(npc);
                }
            }
            
            // Check if player just stopped moving and has a pending farmland interaction
            if (wasMoving && !player.isMoving() && pendingFarmlandInteraction != null) {
                if (isPlayerNearFarmland(pendingFarmlandInteraction)) {
                    FarmlandNode farmland = pendingFarmlandInteraction;
                    pendingFarmlandInteraction = null;
                    
                    double[] screen = gridToScreen((int) farmland.getWorldX(), (int) farmland.getWorldY());
                    farmlandInteraction.show(farmland, screen[0], screen[1]);
                }
            }
            
            // Check if player just stopped moving and has a pending resource interaction
            if (wasMoving && !player.isMoving() && pendingResourceInteraction != null) {
                // Check if player is near the resource node
                if (isPlayerNearResource(pendingResourceInteraction)) {
                    ResourceNodeBase node = pendingResourceInteraction;
                    pendingResourceInteraction = null;
                    
                    // Show generic resource interaction
                    double[] screen = gridToScreen((int) node.getWorldX(), (int) node.getWorldY());
                    resourceInteraction.show(node, screen[0], screen[1]);
                }
            }
        }
        if (movementFlag != null) {
            movementFlag.update(deltaTime);
        }

        if (npcManager != null && player != null) {
            double viewRadius = Math.max(canvas.getWidth(), canvas.getHeight()) / (GRID_SIZE * zoom) + 10;
            npcManager.update(deltaTime, player.getGridX(), player.getGridY(), viewRadius, world.getTowns());
        }
    }
    
    /**
     * Checks if the player is at or near the given town.
     */
    private boolean isPlayerAtTown(Town town) {
        if (player == null || town == null) return false;
        
        int playerX = (int) player.getGridX();
        int playerY = (int) player.getGridY();
        int townX = town.getGridX();
        int townY = town.getGridY();
        int townSize = town.getSize();
        
        // Check if player is within or adjacent to town bounds
        return playerX >= townX - 1 && playerX <= townX + townSize &&
               playerY >= townY - 1 && playerY <= townY + townSize;
    }
    
    /**
     * Checks if the player is near a resource node.
     */
    private boolean isPlayerNearResource(ResourceNodeBase node) {
        if (player == null || node == null) return false;
        
        double playerX = player.getGridX();
        double playerY = player.getGridY();
        
        return node.isInRange(playerX, playerY);
    }

    private boolean isPlayerNearNPC(NPC npc) {
        if (player == null || npc == null) return false;
        double dx = player.getGridX() - npc.getWorldX();
        double dy = player.getGridY() - npc.getWorldY();
        return Math.sqrt(dx * dx + dy * dy) <= 1.2;
    }

    private void triggerNPCInteraction(NPC npc) {
        if (onNPCInteraction != null && npc != null) {
            onNPCInteraction.accept(npc);
        }
    }
    
    /**
     * Checks if the player is near a farmland node.
     */
    private boolean isPlayerNearFarmland(FarmlandNode farmland) {
        if (player == null || farmland == null) return false;
        
        double playerX = player.getGridX();
        double playerY = player.getGridY();
        double farmX = farmland.getWorldX();
        double farmY = farmland.getWorldY();
        
        double dx = playerX - farmX;
        double dy = playerY - farmY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        return distance <= 4; // Close proximity required
    }
    
    // ==================== Event Handling ====================
    
    private void setupEventHandlers() {
        canvas.setOnMousePressed(this::handleMousePress);
        canvas.setOnMouseReleased(this::handleMouseRelease);
        canvas.setOnMouseDragged(this::handleMouseDrag);
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnScroll(this::handleScroll);
        
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(e -> {
            // Check if action progress should handle the key
            if (actionProgress != null && actionProgress.handleKeyPress(e.getCode())) {
                render();
                return;
            }
            
            if (e.getCode() == KeyCode.CONTROL) {
                ctrlHeld = true;
                render();
            } else if (e.getCode() == KeyCode.SPACE) {
                // Toggle pause (only if no action in progress)
                if (gameTime != null && (actionProgress == null || !actionProgress.isInProgress())) {
                    gameTime.togglePause();
                    render();
                }
            } else if (e.getCode() == KeyCode.F) {
                // Toggle fast forward (2x speed, only if no action in progress)
                if (gameTime != null && (actionProgress == null || !actionProgress.isInProgress())) {
                    gameTime.toggleSpeed();
                    render();
                }
            }
        });
        canvas.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.CONTROL) {
                ctrlHeld = false;
                tooltipGridX = -1;
                tooltipGridY = -1;
                render();
            }
        });
    }
    
    private void setupResizeListeners() {
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> render());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> render());
    }
    
    private void handleMousePress(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        dragStartX = e.getX();
        dragStartY = e.getY();
        isDragging = false;  // Will become true if mouse moves enough
    }
    
    private void handleMouseRelease(MouseEvent e) {
        // Calculate drag distance to distinguish click from drag
        double dragDist = Math.sqrt(
            Math.pow(e.getX() - dragStartX, 2) + 
            Math.pow(e.getY() - dragStartY, 2)
        );
        
        boolean wasClick = dragDist < CLICK_THRESHOLD;
        isDragging = false;
        
        if (wasClick) {
            // Check for time control button clicks first
            int timeBtn = getTimeButtonAt(e.getX(), e.getY());
            if (timeBtn == 1 && gameTime != null) {
                // Pause button clicked
                if (actionProgress == null || !actionProgress.isInProgress()) {
                    gameTime.togglePause();
                    render();
                    return;
                }
            } else if (timeBtn == 2 && gameTime != null) {
                // Speed button clicked
                if (actionProgress == null || !actionProgress.isInProgress()) {
                    gameTime.toggleSpeed();
                    render();
                    return;
                }
            } else if (timeBtn == 3) {
                // Center to player button clicked
                centerOnPlayer();
                render();
                return;
            }
            
            // Check if interaction menus should handle the click
            if (farmlandInteraction != null && farmlandInteraction.isVisible()) {
                if (farmlandInteraction.onClick(e.getX(), e.getY())) {
                    render();
                    return;
                }
            }
            if (resourceInteraction != null && resourceInteraction.isVisible()) {
                if (resourceInteraction.onMouseClick(e.getX(), e.getY())) {
                    render();
                    return;
                }
            }
            
            int[] gridPos = screenToGrid(e.getX(), e.getY());
            
            if (e.getButton() == MouseButton.PRIMARY) {
                // Left click: Move player or teleport (dev toggle)
                if (GameSettings.getInstance().isTeleportCheatEnabled() && (e.isControlDown() || ctrlHeld)) {
                    teleportPlayerTo(gridPos[0], gridPos[1]);
                } else {
                    handleLeftClick(gridPos[0], gridPos[1]);
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                // Right click: Interact with structure
                handleRightClick(gridPos[0], gridPos[1]);
            }
        }
        
        render();
    }
    
    /**
     * Handles left-click: Move player to clicked location.
     */
    private void handleLeftClick(int gridX, int gridY) {
        // Don't allow movement when game is paused
        if (gameTime != null && gameTime.isPaused()) {
            return;
        }
        
        // Check if the terrain is walkable (land or water - player can swim!)
        TerrainType[][] terrainMap = world.getTerrain().getTerrainMap();
        if (gridX >= 0 && gridX < world.getMapWidth() && gridY >= 0 && gridY < world.getMapHeight()) {
            TerrainType terrain = terrainMap[gridX][gridY];
            if (terrain != null) {
                // Set movement flag at clicked location (works on land and water)
                if (movementFlag != null) {
                    movementFlag.setPosition(gridX, gridY);
                }
                
                // Move player toward the location - can walk on land or swim in water
                if (player != null) {
                    player.moveTo(gridX, gridY);
                    // Enable camera follow when player clicks to move
                    cameraFollowPlayer = true;
                }
            }
        }
        
        // Still allow structure selection
        MapStructure structure = world.getStructureAt(gridX, gridY);
        selectedStructure = structure;
    }

    /**
     * Teleports the player instantly to a grid location when the cheat toggle is enabled.
     */
    private void teleportPlayerTo(int gridX, int gridY) {
        if (player == null) return;
        player.teleportTo(gridX, gridY);
        if (movementFlag != null) {
            movementFlag.setPosition(gridX, gridY);
        }
        cameraFollowPlayer = true;
        pendingTownInteraction = null;
        pendingNPCInteraction = null;
        pendingFarmlandInteraction = null;
        pendingResourceInteraction = null;
    }
    
    /**
     * Handles right-click: Interact with structures or farmland.
     */
    private void handleRightClick(int gridX, int gridY) {
        // Don't allow interactions when game is paused
        if (gameTime != null && gameTime.isPaused()) {
            return;
        }
        
        // Check for NPC interaction first (use grid coords for click detection)
        NPC clickedNPC = npcManager != null ? npcManager.getNPCAt(gridX + 0.5, gridY + 0.5, 1.5) : null;
        if (clickedNPC != null) {
            if (isPlayerNearNPC(clickedNPC)) {
                triggerNPCInteraction(clickedNPC);
            } else if (GameSettings.getInstance().isMoveToInteract() && player != null) {
                int targetX = (int) Math.round(clickedNPC.getWorldX());
                int targetY = (int) Math.round(clickedNPC.getWorldY());
                player.moveTo(targetX, targetY);
                if (movementFlag != null) {
                    movementFlag.setPosition(targetX, targetY);
                }
                pendingNPCInteraction = clickedNPC;
            }
            return;
        }

        // Check for town/structure FIRST (higher priority than farmland)
        MapStructure structure = world.getStructureAt(gridX, gridY);
        
        if (structure instanceof Town) {
            Town town = (Town) structure;
            
            // Move player to the town
            if (player != null) {
                // Move to center of town
                int targetX = structure.getGridX() + structure.getSize() / 2;
                int targetY = structure.getGridY() + structure.getSize() / 2;
                player.moveTo(targetX, targetY);
                
                // Set pending interaction - will trigger when player arrives
                pendingTownInteraction = town;
            }
            
            // Set flag at town
            if (movementFlag != null) {
                movementFlag.setPosition(structure.getGridX() + structure.getSize() / 2, 
                                        structure.getGridY() + structure.getSize() / 2);
            }
            
            selectedStructure = structure;
            return;
        }
        
        // Check for farmland first (has its own specialized handling)
        FarmlandNode farmland = getFarmlandAt(gridX, gridY);
        if (farmland != null) {
            if (isPlayerNearFarmland(farmland)) {
                double[] screen = gridToScreen(gridX, gridY);
                farmlandInteraction.show(farmland, screen[0], screen[1]);
            } else if (GameSettings.getInstance().isMoveToInteract() && player != null) {
                int targetX = (int) farmland.getWorldX();
                int targetY = (int) farmland.getWorldY();
                player.moveTo(targetX, targetY);
                if (movementFlag != null) {
                    movementFlag.setPosition(targetX, targetY);
                }
                pendingFarmlandInteraction = farmland;
                pendingResourceInteraction = null;
            }
            return;
        }
        
        // Then check for other resource nodes
        ResourceNodeBase resourceNode = getResourceNodeAt(gridX, gridY);
        if (resourceNode != null) {
            // Check if player is near the resource before allowing interaction
            if (isPlayerNearResource(resourceNode)) {
                double[] screen = gridToScreen(gridX, gridY);
                resourceInteraction.show(resourceNode, screen[0], screen[1]);
            } else if (GameSettings.getInstance().isMoveToInteract() && player != null) {
                // Move player towards resource if setting is enabled
                int targetX = (int) resourceNode.getWorldX();
                int targetY = (int) resourceNode.getWorldY();
                player.moveTo(targetX, targetY);
                
                // Set flag at resource location
                if (movementFlag != null) {
                    movementFlag.setPosition(targetX, targetY);
                }
                
                // Set pending resource interaction
                pendingResourceInteraction = resourceNode;
            }
            return;
        }
        
        selectedStructure = structure;
    }
    
    /**
     * Gets any resource node at a grid position.
     * Uses expanded hit detection for better click targeting.
     */
    private ResourceNodeBase getResourceNodeAt(int gridX, int gridY) {
        // Check all resource node types
        List<ResourceNodeBase> allNodes = world.getAllResourceNodes();
        if (allNodes == null) return null;
        
        ResourceNodeBase closest = null;
        double closestDist = Double.MAX_VALUE;
        
        for (ResourceNodeBase node : allNodes) {
            int nodeX = (int) node.getWorldX();
            int nodeY = (int) node.getWorldY();
            int nodeSize = (int) Math.max(3, node.getSize()); // Minimum size of 3 for hit detection
            
            // Check if click is within the node's expanded grid bounds
            if (gridX >= nodeX - 1 && gridX < nodeX + nodeSize + 1 &&
                gridY >= nodeY - 1 && gridY < nodeY + nodeSize + 1) {
                // Calculate distance to node center
                double dx = gridX - (nodeX + nodeSize / 2.0);
                double dy = gridY - (nodeY + nodeSize / 2.0);
                double dist = dx * dx + dy * dy;
                
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = node;
                }
            }
        }
        return closest;
    }
    
    /**
     * Gets farmland at a grid position.
     * Uses tight hit detection - must click within the farmland's actual bounds.
     */
    private FarmlandNode getFarmlandAt(int gridX, int gridY) {
        List<FarmlandNode> farmlands = world.getFarmlandNodes();
        if (farmlands == null) return null;
        
        for (FarmlandNode farmland : farmlands) {
            int farmX = (int) farmland.getWorldX();
            int farmY = (int) farmland.getWorldY();
            int farmWidth = farmland.getWidth();
            int farmHeight = farmland.getHeight();
            
            // Check if click is within the farmland's actual grid bounds
            if (gridX >= farmX && gridX < farmX + farmWidth &&
                gridY >= farmY && gridY < farmY + farmHeight) {
                return farmland;
            }
        }
        return null;
    }
    
    private void handleMouseDrag(MouseEvent e) {
        double dragDist = Math.sqrt(
            Math.pow(e.getX() - dragStartX, 2) + 
            Math.pow(e.getY() - dragStartY, 2)
        );
        
        // Only start dragging if moved past threshold
        if (dragDist >= CLICK_THRESHOLD) {
            isDragging = true;
            // Disable camera follow when user drags the map
            cameraFollowPlayer = false;
        }
        
        if (isDragging) {
            double deltaX = e.getX() - lastMouseX;
            double deltaY = e.getY() - lastMouseY;
            offsetX += deltaX;
            offsetY += deltaY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            render();
        }
    }
    
    private void handleMouseMove(MouseEvent e) {
        // Update interaction menu hover states
        if (farmlandInteraction != null && farmlandInteraction.isVisible()) {
            farmlandInteraction.onMouseMove(e.getX(), e.getY());
        }
        if (resourceInteraction != null && resourceInteraction.isVisible()) {
            resourceInteraction.onMouseMove(e.getX(), e.getY());
        }
        
        // Update time control button hover states
        int timeBtn = getTimeButtonAt(e.getX(), e.getY());
        pauseButtonHovered = (timeBtn == 1);
        speedButtonHovered = (timeBtn == 2);
        centerButtonHovered = (timeBtn == 3);
        
        int[] gridPos = screenToGrid(e.getX(), e.getY());
        hoveredStructure = world.getStructureAt(gridPos[0], gridPos[1]);
        
        if (ctrlHeld) {
            tooltipGridX = gridPos[0];
            tooltipGridY = gridPos[1];
            tooltipScreenX = e.getX();
            tooltipScreenY = e.getY();
        }
        
        render();
    }
    
    private void handleScroll(ScrollEvent e) {
        double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
        double oldZoom = zoom;
        zoom = clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
        
        // Zoom toward mouse position
        double mouseX = e.getX();
        double mouseY = e.getY();
        offsetX = mouseX - (mouseX - offsetX) * (zoom / oldZoom);
        offsetY = mouseY - (mouseY - offsetY) * (zoom / oldZoom);
        
        render();
    }
    
    // ==================== Rendering ====================
    
    /**
     * Renders the entire map canvas.
     */
    public void render() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        
        // Clear canvas
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, width, height);
        
        // Render layers
        renderTerrain(width, height);
        renderDecorations(width, height);
        renderAllResourceNodes(width, height);  // All resource nodes in one pass
        renderRoads(width, height);
        renderStructures(width, height);
        if (npcManager != null) {
            npcManager.render(gc, offsetX, offsetY, zoom, width, height, GRID_SIZE);
        }
        
        // Render movement flag (below player)
        renderMovementFlag();
        
        // Render player sprite
        renderPlayer();
        
        // Render overlays
        if (hoveredStructure != null) {
            renderStructureHover(hoveredStructure);
        }
        if (selectedStructure != null) {
            renderStructureSelection(selectedStructure);
        }
        if (ctrlHeld && tooltipGridX >= 0 && tooltipGridY >= 0) {
            renderTileTooltip();
        }
        
        // Render UI
        renderDayNightOverlay(width, height);
        renderTimeDisplay(width);
        renderMapInfo(width);
        
        // Render interaction menus (on top of everything)
        if (farmlandInteraction != null && farmlandInteraction.isVisible()) {
            farmlandInteraction.render(gc);
        }
        if (resourceInteraction != null && resourceInteraction.isVisible()) {
            resourceInteraction.render(gc);
        }
        
        // Render action progress overlay (top priority)
        if (actionProgress != null && actionProgress.isInProgress()) {
            actionProgress.render(gc, width, height);
        }
    }
    
    /**
     * Renders the movement flag indicator.
     */
    private void renderMovementFlag() {
        if (movementFlag == null || !movementFlag.isVisible()) return;
        
        double[] pos = gridToScreen((int) movementFlag.getGridX(), (int) movementFlag.getGridY());
        double tileSize = GRID_SIZE * zoom;
        
        movementFlag.render(gc, pos[0], pos[1], tileSize);
    }
    
    /**
     * Renders the player sprite on the map.
     */
    private void renderPlayer() {
        if (player == null) return;
        
        double[] pos = gridToScreen((int) player.getGridX(), (int) player.getGridY());
        double tileSize = GRID_SIZE * zoom;
        
        // Only render if visible on screen
        if (pos[0] + tileSize >= 0 && pos[0] <= canvas.getWidth() &&
            pos[1] + tileSize >= 0 && pos[1] <= canvas.getHeight()) {
            
            // Scale sprite size based on zoom, but keep minimum size for visibility
            double minSpriteSize = 40; // Minimum size for zoomed out visibility
            double spriteSize = Math.max(minSpriteSize, tileSize * 1.2);
            
            // Center sprite on tile
            double spriteX = pos[0] + (tileSize - spriteSize) / 2;
            double spriteY = pos[1] + (tileSize - spriteSize);
            
            // At low zoom, add a highlight ring around player for visibility
            if (zoom < 0.3) {
                double ringSize = Math.max(25, spriteSize * 1.3);
                double ringX = pos[0] + tileSize / 2 - ringSize / 2;
                double ringY = pos[1] + tileSize / 2 - ringSize / 2;
                
                // Pulsing glow effect
                double pulse = 0.6 + Math.sin(System.currentTimeMillis() * 0.003) * 0.2;
                gc.setFill(Color.web("#ffdd00").deriveColor(0, 1, 1, pulse * 0.5));
                gc.fillOval(ringX - 3, ringY - 3, ringSize + 6, ringSize + 6);
                
                // Inner ring
                gc.setStroke(Color.web("#ffdd00"));
                gc.setLineWidth(3);
                gc.strokeOval(ringX, ringY, ringSize, ringSize);
                
                // Player marker dot when very zoomed out
                if (zoom < 0.1) {
                    gc.setFill(Color.web("#ff4400"));
                    gc.fillOval(pos[0] + tileSize / 2 - 8, pos[1] + tileSize / 2 - 8, 16, 16);
                    gc.setFill(Color.web("#ffdd00"));
                    gc.fillOval(pos[0] + tileSize / 2 - 5, pos[1] + tileSize / 2 - 5, 10, 10);
                    return; // Don't render full sprite at extreme zoom out
                }
            }
            
            player.render(gc, spriteX, spriteY, spriteSize);
        }
    }
    
    private void renderTerrain(double width, double height) {
        // Calculate visible grid range
        int startX = (int) Math.floor(-offsetX / (GRID_SIZE * zoom)) - 1;
        int startY = (int) Math.floor(-offsetY / (GRID_SIZE * zoom)) - 1;
        int endX = (int) Math.ceil((width - offsetX) / (GRID_SIZE * zoom)) + 1;
        int endY = (int) Math.ceil((height - offsetY) / (GRID_SIZE * zoom)) + 1;
        
        // Clamp to world bounds
        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(world.getMapWidth(), endX);
        endY = Math.min(world.getMapHeight(), endY);
        
        TerrainType[][] terrainMap = world.getTerrain().getTerrainMap();
        
        // Use Level of Detail (LOD) based on zoom level for performance
        if (zoom < LOD_THRESHOLD_3) {
            // Ultra-low zoom: render in 16x16 blocks
            renderTerrainLOD(terrainMap, startX, startY, endX, endY, 16);
        } else if (zoom < LOD_THRESHOLD_2) {
            // Very low zoom: render in 8x8 blocks
            renderTerrainLOD(terrainMap, startX, startY, endX, endY, 8);
        } else if (zoom < LOD_THRESHOLD_1) {
            // Low zoom: render in 4x4 blocks
            renderTerrainLOD(terrainMap, startX, startY, endX, endY, 4);
        } else {
            // Normal zoom: render individual tiles
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    renderTerrainTile(x, y, terrainMap[x][y]);
                }
            }
        }
    }
    
    /**
     * Renders terrain with Level of Detail - samples terrain at block intervals
     * and draws larger rectangles for better performance at low zoom.
     */
    private void renderTerrainLOD(TerrainType[][] terrainMap, int startX, int startY, int endX, int endY, int blockSize) {
        // Align to block boundaries
        int alignedStartX = (startX / blockSize) * blockSize;
        int alignedStartY = (startY / blockSize) * blockSize;
        
        for (int x = alignedStartX; x < endX; x += blockSize) {
            for (int y = alignedStartY; y < endY; y += blockSize) {
                // Sample terrain from center of block
                int sampleX = Math.min(x + blockSize / 2, world.getMapWidth() - 1);
                int sampleY = Math.min(y + blockSize / 2, world.getMapHeight() - 1);
                TerrainType terrain = terrainMap[sampleX][sampleY];
                
                // Get color
                Color terrainColor;
                if (currentFilter instanceof ResourceHeatmapFilter) {
                    terrainColor = ((ResourceHeatmapFilter) currentFilter).getCellColor(sampleX, sampleY);
                } else if (terrain.isWater()) {
                    terrainColor = getWaterColor(sampleX, sampleY);
                } else {
                    terrainColor = currentFilter.getTerrainColor(terrain);
                }
                
                // Calculate screen position and size
                double[] pos = gridToScreen(x, y);
                double cellWidth = blockSize * GRID_SIZE * zoom;
                double cellHeight = blockSize * GRID_SIZE * zoom;
                
                // Clamp to visible area
                double drawX = pos[0];
                double drawY = pos[1];
                double drawW = Math.min(cellWidth, (endX - x) * GRID_SIZE * zoom);
                double drawH = Math.min(cellHeight, (endY - y) * GRID_SIZE * zoom);
                
                gc.setFill(terrainColor);
                gc.fillRect(drawX, drawY, drawW, drawH);
            }
        }
    }
    
    private void renderTerrainTile(int x, int y, TerrainType terrain) {
        double[] pos = gridToScreen(x, y);
        double cellSize = GRID_SIZE * zoom;
        
        // Use professional terrain renderer at reasonable zoom levels
        if (zoom >= 0.5 && terrainRenderer != null && !(currentFilter instanceof ResourceHeatmapFilter)) {
            TerrainType[][] terrainMap = world.getTerrain().getTerrainMap();
            TerrainType[][] neighbors = TerrainRenderer.getNeighbors(terrainMap, x, y);
            terrainRenderer.renderTile(gc, x, y, pos[0], pos[1], cellSize, terrain, neighbors);
        } else {
            // Fallback to simple rendering at low zoom or with special filters
            Color terrainColor;
            if (currentFilter instanceof ResourceHeatmapFilter) {
                terrainColor = ((ResourceHeatmapFilter) currentFilter).getCellColor(x, y);
            } else if (terrain.isWater()) {
                terrainColor = getWaterColor(x, y);
            } else {
                terrainColor = currentFilter.getTerrainColor(terrain);
            }
            
            gc.setFill(terrainColor);
            gc.fillRect(pos[0], pos[1], cellSize, cellSize);
        }
        
        // Draw snow overlay
        if (world.getTerrain().isSnow(x, y) && terrain.isMountainous()) {
            gc.setFill(Color.color(1, 1, 1, 0.35));
            gc.fillRect(pos[0], pos[1], cellSize, cellSize);
        }
        
        // Draw grid lines with fade effect at low zoom (if enabled in settings)
        if (GameSettings.getInstance().isShowGrid() && zoom > GRID_FADE_START) {
            double gridOpacity = Math.min(1.0, (zoom - GRID_FADE_START) / (GRID_FADE_END - GRID_FADE_START));
            Color gridColor = Color.color(
                Math.min(1, GRID_LINE_COLOR.getRed() * gridBrightness),
                Math.min(1, GRID_LINE_COLOR.getGreen() * gridBrightness),
                Math.min(1, GRID_LINE_COLOR.getBlue() * gridBrightness),
                gridOpacity * 0.6
            );
            gc.setStroke(gridColor);
            gc.setLineWidth(0.5);
            gc.strokeRect(pos[0], pos[1], cellSize, cellSize);
            
            // Draw grid IDs if enabled
            if (showGridNumbers && zoom > 0.8) {
                gc.setFill(Color.color(1, 1, 1, 0.7));
                gc.setFont(new Font("Arial", Math.max(8, 10 * zoom)));
                gc.fillText(String.valueOf(y * world.getMapWidth() + x), pos[0] + 4, pos[1] + 14);
            }
        }
    }
    
    private Color getWaterColor(int x, int y) {
        WaterType waterType = world.getTerrain().getWaterType(x, y);
        if (waterType == null) {
            waterType = WaterType.OCEAN;
        }
        
        double depth = world.getTerrain().getWaterDepth(x, y);
        
        Color baseColor;
        switch (waterType) {
            case FRESH_LAKE:
                baseColor = Color.web("#6ec5ff");
                break;
            case OCEAN_SHALLOW:
                baseColor = Color.web("#4ec0cc");
                break;
            case OCEAN:
                baseColor = Color.web("#1c6ea4");
                break;
            case OCEAN_DEEP:
                baseColor = Color.web("#0b3f66");
                break;
            default:
                baseColor = Color.web("#1e4d8b");
        }
        
        // Darken based on depth
        return baseColor.interpolate(Color.BLACK, clamp(depth * 0.6, 0, 0.6));
    }
    
    /**
     * Renders terrain decorations (grass, rocks, trees).
     */
    private void renderDecorations(double width, double height) {
        // Only render decorations at reasonable zoom levels
        if (zoom < 0.3) return;
        
        List<TerrainDecoration> decorations = world.getTerrain().getDecorations();
        if (decorations == null || decorations.isEmpty()) return;
        
        double viewX = -offsetX / (GRID_SIZE * zoom);
        double viewY = -offsetY / (GRID_SIZE * zoom);
        
        for (TerrainDecoration dec : decorations) {
            dec.render(gc, viewX, viewY, zoom, GRID_SIZE);
        }
    }
    
    /**
     * Renders all resource nodes (farmland, lumber, quarry, fishery, ore).
     */
    private void renderAllResourceNodes(double width, double height) {
        // Only render at reasonable zoom levels
        if (zoom < 0.3) return;
        
        // Render old-style resource nodes (terrain-based)
        List<ResourceNode> terrainNodes = world.getTerrain().getResourceNodes();
        if (terrainNodes != null && !terrainNodes.isEmpty()) {
            double viewX = -offsetX / (GRID_SIZE * zoom);
            double viewY = -offsetY / (GRID_SIZE * zoom);
            
            for (ResourceNode node : terrainNodes) {
                node.render(gc, viewX, viewY, zoom, GRID_SIZE);
            }
        }
        
        // Render farmland nodes (separate handling as they don't extend ResourceNodeBase)
        List<FarmlandNode> farmlands = world.getFarmlandNodes();
        if (farmlands != null && !farmlands.isEmpty()) {
            for (FarmlandNode farmland : farmlands) {
                double[] screen = gridToScreen((int)farmland.getWorldX(), (int)farmland.getWorldY());
                
                if (screen[0] > -100 && screen[0] < width + 100 &&
                    screen[1] > -100 && screen[1] < height + 100) {
                    
                    double scale = zoom * GRID_SIZE / 10.0;
                    farmland.render(gc, screen[0], screen[1], scale);
                }
            }
        }
        
        // Render all other village-based resource nodes
        List<ResourceNodeBase> allNodes = world.getAllResourceNodes();
        if (allNodes != null && !allNodes.isEmpty()) {
            for (ResourceNodeBase node : allNodes) {
                double[] screen = gridToScreen((int)node.getWorldX(), (int)node.getWorldY());
                
                // Check if on screen (with buffer)
                if (screen[0] > -100 && screen[0] < width + 100 &&
                    screen[1] > -100 && screen[1] < height + 100) {
                    
                    node.render(gc, screen[0], screen[1], zoom, GRID_SIZE);
                }
            }
        }
    }
    
    private void renderRoads(double width, double height) {
        RoadNetwork roadNetwork = world.getRoadNetwork();
        
        for (Road road : roadNetwork.getRoads()) {
            Color roadColor = currentFilter.getRoadColor(road.getQuality());
            double roadWidth = road.getQuality().getWidth() * zoom;
            double offset = roadWidth / 2.5;
            
            java.util.List<javafx.geometry.Point2D> path = road.getPath();
            for (int i = 0; i < path.size() - 1; i++) {
                javafx.geometry.Point2D p1 = path.get(i);
                javafx.geometry.Point2D p2 = path.get(i + 1);
                
                double[] screen1 = gridToScreen((int) p1.getX(), (int) p1.getY());
                double[] screen2 = gridToScreen((int) p2.getX(), (int) p2.getY());
                
                double cx1 = screen1[0] + GRID_SIZE * zoom / 2;
                double cy1 = screen1[1] + GRID_SIZE * zoom / 2;
                double cx2 = screen2[0] + GRID_SIZE * zoom / 2;
                double cy2 = screen2[1] + GRID_SIZE * zoom / 2;
                
                // Calculate perpendicular offset for double-line roads
                double dx = cx2 - cx1;
                double dy = cy2 - cy1;
                double len = Math.sqrt(dx * dx + dy * dy);
                
                if (len > 0) {
                    double perpX = -dy / len * offset;
                    double perpY = dx / len * offset;
                    
                    gc.setStroke(roadColor);
                    gc.setLineWidth(Math.max(1.0, roadWidth / 2));
                    gc.strokeLine(cx1 + perpX, cy1 + perpY, cx2 + perpX, cy2 + perpY);
                    gc.strokeLine(cx1 - perpX, cy1 - perpY, cx2 - perpX, cy2 - perpY);
                }
            }
        }
    }
    
    private void renderStructures(double width, double height) {
        // Skip rendering structures at very low zoom for performance
        if (zoom < LOD_THRESHOLD_2) {
            // At very low zoom, only render structure markers with names
            for (MapStructure structure : world.getStructures()) {
                double[] pos = gridToScreen(structure.getGridX(), structure.getGridY());
                double markerSize = Math.max(4, structure.getSize() * GRID_SIZE * zoom * 0.5);
                
                // Simple colored dot for each structure
                boolean isMajor = structure instanceof Town && ((Town)structure).isMajor();
                gc.setFill(isMajor ? Color.web("#ffd700") : Color.web("#ffaa66"));
                gc.fillOval(pos[0], pos[1], markerSize, markerSize);
                
                // Draw name for towns even at low zoom
                if (structure instanceof Town && zoom > 0.03) {
                    Town town = (Town) structure;
                    double fontSize = Math.max(8, 10);
                    MedievalFont.renderLabel(gc, town.getName(), 
                        pos[0] + markerSize / 2, pos[1] + markerSize + fontSize + 2, 
                        fontSize, isMajor ? Color.web("#ffd700") : Color.web("#e0e0e0"));
                }
            }
            return;
        }
        
        for (MapStructure structure : world.getStructures()) {
            double[] pos = gridToScreen(structure.getGridX(), structure.getGridY());
            double size = structure.getSize() * GRID_SIZE * zoom;
            
            // Skip if off-screen
            if (pos[0] + size < 0 || pos[0] > canvas.getWidth() ||
                pos[1] + size < 0 || pos[1] > canvas.getHeight()) {
                continue;
            }
            
            // Use new sprite system for towns
            if (structure instanceof Town) {
                Town town = (Town) structure;
                boolean nearWater = isNearWater(town.getGridX(), town.getGridY());
                
                // At medium zoom, use sprites; at low zoom, use simplified rendering
                if (zoom >= 0.3 && size >= 30) {
                    TownSprite.render(gc, town, pos[0], pos[1], size, nearWater);
                } else {
                    // Simplified rendering for small sizes
                    gc.setFill(currentFilter.getStructureColor(structure));
                    gc.fillRect(pos[0], pos[1], size, size);
                    renderStructureDetails(structure, pos, size);
                }
            } else {
                // Non-town structures
                gc.setFill(currentFilter.getStructureColor(structure));
                gc.fillRect(pos[0], pos[1], size, size);
                renderStructureDetails(structure, pos, size);
            }
            
            // Draw border
            gc.setStroke(Color.web("#606060"));
            gc.setLineWidth(1);
            gc.strokeRect(pos[0], pos[1], size, size);
            
            // Draw medieval-style town names using MedievalFont
            if (structure instanceof Town && zoom > 0.25 && size > 30) {
                Town town = (Town) structure;
                double fontSize = Math.max(10, Math.min(24, 14 * zoom));
                double labelX = pos[0] + size / 2;
                double labelY = pos[1] + size + fontSize + 8;
                
                MedievalFont.renderTownName(gc, town.getName(), labelX, labelY, fontSize, town.isMajor());
            } else if (!(structure instanceof Town) && zoom > 0.5 && size > 50) {
                // Simple labels for non-town structures
                gc.setFill(TEXT_COLOR);
                gc.setFont(new Font("Arial", Math.max(8, 12 * zoom)));
                String label = structure.getName().substring(0, Math.min(15, structure.getName().length()));
                gc.fillText(label, pos[0] + 5, pos[1] + size + 15);
            }
        }
    }
    
    /**
     * Checks if a position is near water (for port town detection).
     */
    private boolean isNearWater(int x, int y) {
        TerrainType[][] terrain = world.getTerrain().getTerrainMap();
        int checkRadius = 10;
        
        for (int dx = -checkRadius; dx <= checkRadius; dx++) {
            for (int dy = -checkRadius; dy <= checkRadius; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < world.getMapWidth() && ny >= 0 && ny < world.getMapHeight()) {
                    if (terrain[nx][ny].isWater()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void renderStructureDetails(MapStructure structure, double[] pos, double size) {
        if (structure instanceof Town) {
            Town town = (Town) structure;
            if (town.isMajor()) {
                // Major towns get corner towers
                gc.setFill(Color.web("#ffcc00"));
                double towerSize = Math.max(3, size * 0.15);
                gc.fillRect(pos[0] + towerSize, pos[1] + towerSize, towerSize * 0.6, towerSize * 0.6);
                gc.fillRect(pos[0] + size - towerSize * 1.6, pos[1] + towerSize, towerSize * 0.6, towerSize * 0.6);
                gc.fillRect(pos[0] + towerSize, pos[1] + size - towerSize * 1.6, towerSize * 0.6, towerSize * 0.6);
                gc.fillRect(pos[0] + size - towerSize * 1.6, pos[1] + size - towerSize * 1.6, towerSize * 0.6, towerSize * 0.6);
                
                // Center tower
                gc.setFill(Color.web("#ffaa00"));
                gc.fillRect(pos[0] + size/2 - towerSize*0.5, pos[1] + size/2 - towerSize*0.5, towerSize, towerSize);
            } else {
                // Minor towns get simple circle
                gc.setFill(Color.web("#ffdd88"));
                double circleSize = Math.max(2, size * 0.1);
                gc.fillOval(pos[0] + size/2 - circleSize, pos[1] + size/2 - circleSize, circleSize * 2, circleSize * 2);
            }
        } else if (structure instanceof MiningQuarry || structure instanceof Stoneworks) {
            // X marks for mining
            gc.setStroke(Color.web("#888888"));
            gc.setLineWidth(Math.max(1, size * 0.08));
            double cross = size * 0.3;
            gc.strokeLine(pos[0] + cross, pos[1] + cross, pos[0] + size - cross, pos[1] + size - cross);
            gc.strokeLine(pos[0] + size - cross, pos[1] + cross, pos[0] + cross, pos[1] + size - cross);
        } else if (structure instanceof LumberCamp) {
            // Tree symbol for lumber
            gc.setFill(Color.web("#228822"));
            double treeSize = Math.max(2, size * 0.12);
            gc.fillPolygon(
                new double[]{pos[0] + size/2, pos[0] + size/2 - treeSize, pos[0] + size/2 + treeSize},
                new double[]{pos[1] + size * 0.2, pos[1] + size * 0.45, pos[1] + size * 0.45}, 3
            );
            gc.fillPolygon(
                new double[]{pos[0] + size/2, pos[0] + size/2 - treeSize * 0.8, pos[0] + size/2 + treeSize * 0.8},
                new double[]{pos[1] + size * 0.35, pos[1] + size * 0.55, pos[1] + size * 0.55}, 3
            );
        } else if (structure instanceof Millworks) {
            // Circle for mills
            gc.setFill(Color.web("#cc8844"));
            double radius = Math.max(2, size * 0.15);
            gc.fillOval(pos[0] + size/2 - radius, pos[1] + size/2 - radius, radius * 2, radius * 2);
        }
    }
    
    private void renderStructureHover(MapStructure structure) {
        double[] pos = gridToScreen(structure.getGridX(), structure.getGridY());
        double size = structure.getSize() * GRID_SIZE * zoom;
        
        gc.setStroke(Color.web("#ffff00"));
        gc.setLineWidth(3);
        gc.strokeRect(pos[0], pos[1], size, size);
        
        // Tooltip
        gc.setFill(Color.web("rgba(0,0,0,0.8)"));
        gc.fillRect(pos[0], pos[1] - 30, 200, 30);
        gc.setFill(Color.web("#ffff00"));
        gc.setFont(new Font("Arial", 10));
        gc.fillText(structure.getName() + " (" + structure.getType() + ")", pos[0] + 5, pos[1] - 10);
    }
    
    private void renderStructureSelection(MapStructure structure) {
        double[] pos = gridToScreen(structure.getGridX(), structure.getGridY());
        double size = structure.getSize() * GRID_SIZE * zoom;
        
        gc.setStroke(Color.web("#00ff00"));
        gc.setLineWidth(4);
        gc.strokeRect(pos[0] - 2, pos[1] - 2, size + 4, size + 4);
    }
    
    private void renderTileTooltip() {
        if (!isValidGridPosition(tooltipGridX, tooltipGridY)) {
            return;
        }
        
        TerrainGenerator terrain = world.getTerrain();
        TerrainType tt = terrain.getTerrainMap()[tooltipGridX][tooltipGridY];
        
        // Build tooltip content
        StringBuilder sb = new StringBuilder();
        sb.append("📍 Tile [").append(tooltipGridX).append(", ").append(tooltipGridY).append("]\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("🏔 Terrain: ").append(tt.getDisplayName()).append("\n");
        sb.append("📊 Elevation: ").append(String.format("%.2f", terrain.getElevation(tooltipGridX, tooltipGridY))).append("\n");
        
        if (tt.isWater()) {
            WaterType wt = terrain.getWaterType(tooltipGridX, tooltipGridY);
            sb.append("🌊 Water Type: ").append(wt != null ? formatWaterType(wt) : "Water").append("\n");
            sb.append("🔵 Depth: ").append(String.format("%.2f", terrain.getWaterDepth(tooltipGridX, tooltipGridY))).append("\n");
        }
        
        // Show climate info
        try {
            double moisture = world.getWorldGenerator().getMoisture(tooltipGridX, tooltipGridY);
            double temperature = world.getWorldGenerator().getTemperature(tooltipGridX, tooltipGridY);
            sb.append("💧 Moisture: ").append(String.format("%.0f%%", moisture * 100)).append("\n");
            sb.append("🌡 Temperature: ").append(getTemperatureDesc(temperature)).append("\n");
        } catch (Exception e) {
            // WorldGenerator may not be available
        }
        
        // Show region name if available
        try {
            String regionName = world.getRegionName(tooltipGridX, tooltipGridY);
            if (regionName != null && !regionName.equals("Unknown")) {
                sb.append("🗺 Region: ").append(regionName).append("\n");
            }
        } catch (Exception e) {
            // WorldGenerator may not be available
        }
        
        if (terrain.isSnow(tooltipGridX, tooltipGridY)) {
            sb.append("❄ Snow Cover: Yes\n");
        }
        
        MapStructure struct = world.getStructureAt(tooltipGridX, tooltipGridY);
        if (struct != null) {
            sb.append("━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("🏛 Structure: ").append(struct.getName()).append("\n");
            sb.append("📝 Type: ").append(struct.getType()).append("\n");
            if (struct.getPopulation() > 0) {
                sb.append("👥 Population: ").append(String.format("%,.0f", struct.getPopulation())).append("\n");
            }
        }
        
        // Render tooltip
        String[] lines = sb.toString().split("\n");
        int tooltipWidth = 200;
        int lineHeight = 16;
        int tooltipHeight = lines.length * lineHeight + 12;
        
        double tx = tooltipScreenX + 15;
        double ty = tooltipScreenY + 15;
        if (tx + tooltipWidth > canvas.getWidth()) tx = tooltipScreenX - tooltipWidth - 5;
        if (ty + tooltipHeight > canvas.getHeight()) ty = tooltipScreenY - tooltipHeight - 5;
        
        // Tooltip background with gradient effect
        gc.setFill(Color.web("rgba(15, 15, 25, 0.95)"));
        gc.fillRoundRect(tx, ty, tooltipWidth, tooltipHeight, 8, 8);
        gc.setStroke(TOOLTIP_BORDER);
        gc.setLineWidth(2);
        gc.strokeRoundRect(tx, ty, tooltipWidth, tooltipHeight, 8, 8);
        
        // Header highlight
        gc.setFill(Color.web("rgba(74, 158, 255, 0.15)"));
        gc.fillRoundRect(tx + 2, ty + 2, tooltipWidth - 4, lineHeight + 4, 6, 6);
        
        gc.setFill(TEXT_COLOR);
        gc.setFont(new Font("Arial", 11));
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Highlight header
            if (i == 0) {
                gc.setFill(Color.web(TOOLTIP_BORDER.toString()));
            } else if (line.startsWith("━")) {
                gc.setFill(Color.web("#555555"));
            } else {
                gc.setFill(TEXT_COLOR);
            }
            gc.fillText(line, tx + 10, ty + 14 + i * lineHeight);
        }
    }
    
    private String formatWaterType(WaterType wt) {
        switch (wt) {
            case FRESH_LAKE: return "Fresh Lake";
            case OCEAN_SHALLOW: return "Shallow Ocean";
            case OCEAN: return "Ocean";
            case OCEAN_DEEP: return "Deep Ocean";
            default: return wt.name();
        }
    }
    
    private String getTemperatureDesc(double temp) {
        if (temp < 0.15) return "Freezing";
        if (temp < 0.30) return "Cold";
        if (temp < 0.45) return "Cool";
        if (temp < 0.55) return "Moderate";
        if (temp < 0.70) return "Warm";
        if (temp < 0.85) return "Hot";
        return "Scorching";
    }
    
    /**
     * Renders a tinted overlay based on time of day.
     */
    private void renderDayNightOverlay(double width, double height) {
        if (gameTime == null) return;
        
        Color skyTint = gameTime.getSkyTint();
        
        // Apply subtle tint over the entire map
        // The tint is semi-transparent to maintain visibility
        double opacity = 0.15; // Subtle effect
        
        // Night is darker
        if (gameTime.getTimeOfDay() == GameTime.TimeOfDay.NIGHT) {
            opacity = 0.35;
        } else if (gameTime.getTimeOfDay() == GameTime.TimeOfDay.DUSK ||
                   gameTime.getTimeOfDay() == GameTime.TimeOfDay.DAWN) {
            opacity = 0.2;
        }
        
        gc.setFill(skyTint.deriveColor(0, 1, 1, opacity));
        gc.fillRect(0, 0, width, height);
    }
    
    /**
     * Renders the time display with date, sun position indicator, and time controls.
     */
    private void renderTimeDisplay(double width) {
        if (gameTime == null) return;
        
        // Position in top-right corner - larger for date display
        double displayX = width - 220;
        double displayY = 10;
        double displayWidth = 210;
        double displayHeight = 80;
        
        // Background
        gc.setFill(Color.web("#1a1208").deriveColor(0, 1, 1, 0.85));
        gc.fillRoundRect(displayX, displayY, displayWidth, displayHeight, 8, 8);
        
        // Border
        gc.setStroke(Color.web("#c4a574"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(displayX, displayY, displayWidth, displayHeight, 8, 8);
        
        // Date text (e.g., "January 1st, 500 AD")
        gc.setFill(Color.web("#c4a574"));
        gc.setFont(Font.font("Georgia", javafx.scene.text.FontWeight.BOLD, 11));
        gc.fillText(gameTime.getFormattedDate(), displayX + 10, displayY + 16);
        
        // Time text
        gc.setFill(Color.web("#e8e0d0"));
        gc.setFont(Font.font("Georgia", javafx.scene.text.FontWeight.BOLD, 14));
        gc.fillText(gameTime.getFormattedTime(), displayX + 10, displayY + 35);
        
        // Time of day
        gc.setFont(Font.font("Georgia", 11));
        gc.setFill(gameTime.getSkyTint());
        gc.fillText(gameTime.getTimeOfDay().getDisplayName(), displayX + 95, displayY + 35);
        
        // Speed indicator
        String speedText = gameTime.getSpeedDescription();
        Color speedColor = gameTime.isPaused() ? Color.web("#ff6666") : 
                          (gameTime.isFastSpeed() ? Color.web("#66ff66") : Color.web("#aaaaaa"));
        gc.setFill(speedColor);
        gc.setFont(Font.font("Georgia", 10));
        gc.fillText(speedText, displayX + 160, displayY + 35);
        
        // Sun position indicator (horizontal bar)
        double barX = displayX + 10;
        double barY = displayY + 45;
        double barWidth = displayWidth - 20;
        double barHeight = 10;
        
        // Bar background (sky gradient)
        gc.setFill(Color.web("#0a1535")); // Night sky
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Daytime section (lighter)
        double dayStart = barWidth * 0.25; // 6:00 = 25% of day
        double dayEnd = barWidth * 0.833;   // 20:00 = 83% of day
        gc.setFill(Color.web("#4a90d0").deriveColor(0, 1, 1, 0.5));
        gc.fillRect(barX + dayStart, barY, dayEnd - dayStart, barHeight);
        
        // Sun indicator
        double sunX = barX + gameTime.getSunPosition() * barWidth;
        Color sunColor = gameTime.getTimeOfDay() == GameTime.TimeOfDay.NIGHT 
            ? Color.web("#aaaaaa") // Moon color
            : Color.web("#ffdd00"); // Sun color
        
        gc.setFill(sunColor);
        gc.fillOval(sunX - 4, barY + barHeight/2 - 4, 8, 8);
        
        // Border for bar
        gc.setStroke(Color.web("#c4a574").darker());
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
        
        // Time controls hint
        gc.setFill(Color.web("#888888"));
        gc.setFont(Font.font("Georgia", 9));
        gc.fillText("[Space]=Pause  [F]=Fast", displayX + 10, displayY + 72);
        
        // Draw time control buttons
        renderTimeControlButtons(displayX + displayWidth - 75, displayY + 58);
        
        // Render energy meter right below
        renderEnergyMeter(displayX, displayY + displayHeight + 5, displayWidth);
    }
    
    // Time control button state
    private boolean pauseButtonHovered = false;
    private boolean speedButtonHovered = false;
    private boolean centerButtonHovered = false;
    private double timeButtonX, timeButtonY;
    private static final double TIME_BUTTON_SIZE = 22;
    
    /**
     * Renders clickable time control buttons and center to player button.
     */
    private void renderTimeControlButtons(double x, double y) {
        timeButtonX = x;
        timeButtonY = y;
        
        // Pause/Play button
        double btn1X = x;
        Color btn1Bg = pauseButtonHovered ? Color.web("#4a3a25") : Color.web("#2a1f10");
        Color btn1Border = gameTime.isPaused() ? Color.web("#ff6666") : Color.web("#c4a574");
        
        gc.setFill(btn1Bg);
        gc.fillRoundRect(btn1X, y, TIME_BUTTON_SIZE, TIME_BUTTON_SIZE, 4, 4);
        gc.setStroke(btn1Border);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(btn1X, y, TIME_BUTTON_SIZE, TIME_BUTTON_SIZE, 4, 4);
        
        // Draw pause/play icon
        gc.setFill(btn1Border);
        if (gameTime.isPaused()) {
            // Play triangle
            double[] xPoints = {btn1X + 7, btn1X + 7, btn1X + 17};
            double[] yPoints = {y + 5, y + 17, y + 11};
            gc.fillPolygon(xPoints, yPoints, 3);
        } else {
            // Pause bars
            gc.fillRect(btn1X + 6, y + 5, 3, 12);
            gc.fillRect(btn1X + 13, y + 5, 3, 12);
        }
        
        // Speed button
        double btn2X = x + TIME_BUTTON_SIZE + 5;
        Color btn2Bg = speedButtonHovered ? Color.web("#4a3a25") : Color.web("#2a1f10");
        Color btn2Border = gameTime.isFastSpeed() ? Color.web("#66ff66") : Color.web("#c4a574");
        
        gc.setFill(btn2Bg);
        gc.fillRoundRect(btn2X, y, TIME_BUTTON_SIZE, TIME_BUTTON_SIZE, 4, 4);
        gc.setStroke(btn2Border);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(btn2X, y, TIME_BUTTON_SIZE, TIME_BUTTON_SIZE, 4, 4);
        
        // Draw fast forward icon (>>)
        gc.setFill(btn2Border);
        gc.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
        gc.fillText(">>", btn2X + 3, y + 15);
        
        // Center to player button
        double btn3X = x + (TIME_BUTTON_SIZE + 5) * 2;
        Color btn3Bg = centerButtonHovered ? Color.web("#4a3a25") : Color.web("#2a1f10");
        Color btn3Border = Color.web("#c4a574");
        
        gc.setFill(btn3Bg);
        gc.fillRoundRect(btn3X, y, TIME_BUTTON_SIZE, TIME_BUTTON_SIZE, 4, 4);
        gc.setStroke(btn3Border);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(btn3X, y, TIME_BUTTON_SIZE, TIME_BUTTON_SIZE, 4, 4);
        
        // Draw crosshair/target icon for center button
        gc.setStroke(btn3Border);
        gc.setLineWidth(1.5);
        double cx = btn3X + TIME_BUTTON_SIZE / 2;
        double cy = y + TIME_BUTTON_SIZE / 2;
        gc.strokeLine(cx - 5, cy, cx + 5, cy);
        gc.strokeLine(cx, cy - 5, cx, cy + 5);
        gc.strokeOval(cx - 4, cy - 4, 8, 8);
    }
    
    /**
     * Checks if a point is over a time control button and returns which one (0=none, 1=pause, 2=speed, 3=center).
     */
    private int getTimeButtonAt(double mouseX, double mouseY) {
        if (timeButtonX == 0 && timeButtonY == 0) return 0;
        
        // Pause button
        if (mouseX >= timeButtonX && mouseX < timeButtonX + TIME_BUTTON_SIZE &&
            mouseY >= timeButtonY && mouseY < timeButtonY + TIME_BUTTON_SIZE) {
            return 1;
        }
        
        // Speed button
        double btn2X = timeButtonX + TIME_BUTTON_SIZE + 5;
        if (mouseX >= btn2X && mouseX < btn2X + TIME_BUTTON_SIZE &&
            mouseY >= timeButtonY && mouseY < timeButtonY + TIME_BUTTON_SIZE) {
            return 2;
        }
        
        // Center button
        double btn3X = timeButtonX + (TIME_BUTTON_SIZE + 5) * 2;
        if (mouseX >= btn3X && mouseX < btn3X + TIME_BUTTON_SIZE &&
            mouseY >= timeButtonY && mouseY < timeButtonY + TIME_BUTTON_SIZE) {
            return 3;
        }
        
        return 0;
    }
    
    /**
     * Renders the energy meter display beneath the time meter.
     */
    private void renderEnergyMeter(double displayX, double displayY, double displayWidth) {
        if (playerEnergy == null) return;
        
        double displayHeight = 35;
        
        // Background
        gc.setFill(Color.web("#1a1208").deriveColor(0, 1, 1, 0.85));
        gc.fillRoundRect(displayX, displayY, displayWidth, displayHeight, 8, 8);
        
        // Border
        gc.setStroke(Color.web("#c4a574"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(displayX, displayY, displayWidth, displayHeight, 8, 8);
        
        // Energy label and value
        gc.setFill(Color.web("#e8e0d0"));
        gc.setFont(Font.font("Georgia", javafx.scene.text.FontWeight.BOLD, 11));
        gc.fillText("Energy", displayX + 10, displayY + 15);
        
        // Percentage text
        String energyText = (int)playerEnergy.getCurrentEnergy() + "/" + (int)playerEnergy.getMaxEnergy();
        gc.setFill(getEnergyColor());
        gc.fillText(energyText, displayX + 70, displayY + 15);
        
        // Energy bar
        double barX = displayX + 10;
        double barY = displayY + 20;
        double barWidth = displayWidth - 20;
        double barHeight = 10;
        
        // Bar background
        gc.setFill(Color.web("#101010"));
        gc.fillRect(barX, barY, barWidth, barHeight);
        
        // Filled portion
        double fillWidth = barWidth * playerEnergy.getEnergyPercent();
        gc.setFill(getEnergyColor());
        gc.fillRect(barX, barY, fillWidth, barHeight);
        
        // Border for bar
        gc.setStroke(Color.web("#5a4a30"));
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);
    }
    
    /**
     * Gets the color for the energy bar based on current energy level.
     */
    private Color getEnergyColor() {
        if (playerEnergy == null) return Color.web("#40a040");
        
        double percent = playerEnergy.getEnergyPercent();
        if (percent > 0.6) {
            return Color.web("#40a040"); // Green - good
        } else if (percent > 0.3) {
            return Color.web("#c0a040"); // Yellow - medium
        } else {
            return Color.web("#c04040"); // Red - low
        }
    }
    
    private void renderMapInfo(double width) {
        String info = String.format("Mode: %s | Zoom: %.0f%% | Structures: %d",
            mapMode, zoom * 100, world.getStructures().size());
        gc.setFill(TEXT_COLOR);
        gc.setFont(new Font("Arial", 12));
        gc.fillText(info, 10, 20);
    }
    
    // ==================== Coordinate Conversion ====================
    
    private double[] gridToScreen(int gridX, int gridY) {
        return new double[]{
            gridX * GRID_SIZE * zoom + offsetX,
            gridY * GRID_SIZE * zoom + offsetY
        };
    }
    
    private int[] screenToGrid(double screenX, double screenY) {
        // Use round instead of floor for more accurate center-based selection
        return new int[]{
            (int) Math.round((screenX - offsetX) / (GRID_SIZE * zoom) - 0.5),
            (int) Math.round((screenY - offsetY) / (GRID_SIZE * zoom) - 0.5)
        };
    }
    
    private boolean isValidGridPosition(int x, int y) {
        return x >= 0 && x < world.getMapWidth() && y >= 0 && y < world.getMapHeight();
    }
    
    // ==================== Utility Methods ====================
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    // ==================== Public API ====================
    
    public void setMapMode(String mode) {
        this.mapMode = mode;
        render();
    }
    
    public void setMapFilter(MapFilter filter) {
        this.currentFilter = filter;
        render();
    }
    
    public void setGridBrightness(double factor) {
        this.gridBrightness = clamp(factor, 0.3, 2.0);
        render();
    }
    
    public void toggleGridNumbers() {
        this.showGridNumbers = !this.showGridNumbers;
        render();
    }
    
    public void zoomIn() {
        adjustZoom(1.1);
    }
    
    public void zoomOut() {
        adjustZoom(0.9);
    }
    
    public void fitToView() {
        offsetX = 0;
        offsetY = 0;
        zoom = 1.0;
        render();
    }
    
    /**
     * Centers the view on the player sprite.
     */
    public void centerOnPlayer() {
        if (player == null) return;
        
        double playerGridX = player.getGridX();
        double playerGridY = player.getGridY();
        
        // Calculate offset to center player on screen
        offsetX = canvas.getWidth() / 2 - playerGridX * GRID_SIZE * zoom;
        offsetY = canvas.getHeight() / 2 - playerGridY * GRID_SIZE * zoom;
        
        render();
    }
    
    /**
     * Smoothly follows the player when camera follow mode is enabled.
     * Uses linear interpolation for smooth camera movement.
     */
    private void smoothFollowPlayer(double deltaTime) {
        if (player == null) return;
        
        double playerGridX = player.getGridX();
        double playerGridY = player.getGridY();
        
        // Target offset to center player on screen
        double targetOffsetX = canvas.getWidth() / 2 - playerGridX * GRID_SIZE * zoom;
        double targetOffsetY = canvas.getHeight() / 2 - playerGridY * GRID_SIZE * zoom;
        
        // Smoothly interpolate current offset toward target
        double lerpFactor = Math.min(1.0, deltaTime * CAMERA_FOLLOW_SMOOTHNESS);
        offsetX += (targetOffsetX - offsetX) * lerpFactor;
        offsetY += (targetOffsetY - offsetY) * lerpFactor;
    }
    
    /**
     * Returns whether camera is currently following the player.
     */
    public boolean isCameraFollowPlayer() {
        return cameraFollowPlayer;
    }
    
    /**
     * Sets whether camera should follow the player.
     */
    public void setCameraFollowPlayer(boolean follow) {
        this.cameraFollowPlayer = follow;
    }

    /**
     * Instant energy refill for dev tools.
     */
    public void refillEnergyCheat() {
        if (playerEnergy != null) {
            playerEnergy.fullyRestore();
            render();
        }
    }
    
    private void adjustZoom(double factor) {
        double oldZoom = zoom;
        zoom = clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        offsetX = centerX - (centerX - offsetX) * (zoom / oldZoom);
        offsetY = centerY - (centerY - offsetY) * (zoom / oldZoom);
        render();
    }
    
    public void onStageReady() {
        render();
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
    
    public DemoWorld getWorld() {
        return world;
    }
    
    public GameTime getGameTime() {
        return gameTime;
    }
    
    public PlayerEnergy getPlayerEnergy() {
        return playerEnergy;
    }
    
    // View state getters for save/load
    public int getViewX() {
        int[] gridPos = screenToGrid(canvas.getWidth() / 2, canvas.getHeight() / 2);
        return gridPos[0];
    }
    
    public int getViewY() {
        int[] gridPos = screenToGrid(canvas.getWidth() / 2, canvas.getHeight() / 2);
        return gridPos[1];
    }
    
    public double getZoom() {
        return zoom;
    }
    
    // View state setters for save/load
    public void setView(int centerX, int centerY, double zoom) {
        this.zoom = clamp(zoom, MIN_ZOOM, MAX_ZOOM);
        this.offsetX = canvas.getWidth() / 2 - centerX * GRID_SIZE * this.zoom;
        this.offsetY = canvas.getHeight() / 2 - centerY * GRID_SIZE * this.zoom;
        render();
    }
}
