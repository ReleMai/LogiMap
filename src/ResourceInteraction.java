import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * BLUEPRINT: Generic interaction menu for all resource node types.
 * 
 * This menu works with any class extending ResourceNodeBase.
 * It displays resource information and handles harvest actions.
 * Uses ActionProgress for timed gathering (30 game minutes).
 * 
 * == USAGE ==
 * 1. Create an instance with GameTime and PlayerEnergy
 * 2. Call setActionProgress() to enable timed actions
 * 3. Call show() with any ResourceNodeBase subclass
 * 4. The menu automatically adapts to the resource type
 */
public class ResourceInteraction {
    
    // Medieval theme colors
    private static final Color BG_DARK = Color.web("#1a1208");
    private static final Color BG_MED = Color.web("#2a1f10");
    private static final Color BORDER = Color.web("#c4a574");
    private static final Color TEXT_GOLD = Color.web("#c4a574");
    private static final Color TEXT_WHITE = Color.web("#e8e0d0");
    private static final Color BUTTON_NORMAL = Color.web("#3a2a15");
    private static final Color BUTTON_HOVER = Color.web("#4a3a25");
    private static final Color BUTTON_DISABLED = Color.web("#2a2010");
    
    // Action duration in game minutes (reduced for faster gameplay)
    private static final int GATHERING_DURATION = 5;
    
    // Menu state
    private boolean visible = false;
    private double menuX, menuY;
    private double menuWidth = 280;
    private double menuHeight = 250;
    
    // Current target
    private ResourceNodeBase targetNode;
    private GameTime gameTime;
    private PlayerEnergy playerEnergy;
    private ActionProgress actionProgress;  // For timed actions
    
    // Button states
    private boolean harvestHovered = false;
    private boolean cancelHovered = false;
    
    // Callback for harvest action
    private HarvestCallback harvestCallback;
    
    // Energy cost per harvest (can be customized per resource type)
    private int energyCost = 10;
    
    // Cached estimated yield (to prevent flickering from random calculations)
    private int cachedEstimatedYield = 0;
    
    /**
     * Callback interface for harvest action.
     */
    public interface HarvestCallback {
        void onHarvest(ResourceNodeBase node, int yield, String itemId);
    }
    
    /**
     * Creates the interaction menu.
     */
    public ResourceInteraction(GameTime gameTime, PlayerEnergy playerEnergy) {
        this.gameTime = gameTime;
        this.playerEnergy = playerEnergy;
    }
    
    /**
     * Sets the action progress system for timed gathering.
     */
    public void setActionProgress(ActionProgress actionProgress) {
        this.actionProgress = actionProgress;
    }
    
    /**
     * Sets the harvest callback.
     */
    public void setHarvestCallback(HarvestCallback callback) {
        this.harvestCallback = callback;
    }
    
    /**
     * Shows the menu for a resource node.
     */
    public void show(ResourceNodeBase node, double screenX, double screenY) {
        this.targetNode = node;
        this.visible = true;
        
        // Adjust energy cost based on resource type
        this.energyCost = getEnergyCostForResource(node);
        
        // Cache estimated yield to prevent flickering from random recalculation
        this.cachedEstimatedYield = node.getBaseYield();
        
        // Position menu near click, but keep on screen
        this.menuX = screenX - menuWidth / 2;
        this.menuY = screenY - menuHeight - 20;
        
        if (menuY < 10) menuY = screenY + 20;
    }
    
    /**
     * Gets energy cost based on resource type.
     */
    private int getEnergyCostForResource(ResourceNodeBase node) {
        String category = node.getResourceCategory();
        return switch (category) {
            case "Grain" -> 8;
            case "Timber" -> 15;
            case "Stone" -> 18;
            case "Fish" -> 12;
            case "Ore" -> 20;
            default -> 10;
        };
    }
    
    /**
     * Gets action verb based on resource type.
     */
    private String getActionVerb(ResourceNodeBase node) {
        String category = node.getResourceCategory();
        return switch (category) {
            case "Grain" -> "Harvest";
            case "Timber" -> "Chop";
            case "Stone" -> "Quarry";
            case "Fish" -> "Fish";
            case "Ore" -> "Mine";
            default -> "Gather";
        };
    }
    
    /**
     * Gets icon for resource type.
     */
    private String getResourceIcon(ResourceNodeBase node) {
        String category = node.getResourceCategory();
        return switch (category) {
            case "Grain" -> "🌾";
            case "Timber" -> "🪵";
            case "Stone" -> "🪨";
            case "Fish" -> "🐟";
            case "Ore" -> "⛏";
            default -> "📦";
        };
    }
    
    /**
     * Hides the menu.
     */
    public void hide() {
        this.visible = false;
        this.targetNode = null;
    }
    
    /**
     * Checks if menu is visible.
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Handles mouse movement for button hover.
     */
    public void onMouseMove(double mouseX, double mouseY) {
        if (!visible) return;
        
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        
        // Harvest button
        double harvestX = menuX + menuWidth / 2 - btnWidth - 10;
        harvestHovered = mouseX >= harvestX && mouseX <= harvestX + btnWidth &&
                         mouseY >= btnY && mouseY <= btnY + btnHeight;
        
        // Cancel button
        double cancelX = menuX + menuWidth / 2 + 10;
        cancelHovered = mouseX >= cancelX && mouseX <= cancelX + btnWidth &&
                        mouseY >= btnY && mouseY <= btnY + btnHeight;
    }
    
    /**
     * Handles mouse click.
     * @return true if click was handled
     */
    public boolean onMouseClick(double mouseX, double mouseY) {
        if (!visible) return false;
        
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        
        // Check harvest button
        double harvestX = menuX + menuWidth / 2 - btnWidth - 10;
        if (mouseX >= harvestX && mouseX <= harvestX + btnWidth &&
            mouseY >= btnY && mouseY <= btnY + btnHeight) {
            
            if (canHarvest()) {
                performHarvest();
            }
            return true;
        }
        
        // Check cancel button
        double cancelX = menuX + menuWidth / 2 + 10;
        if (mouseX >= cancelX && mouseX <= cancelX + btnWidth &&
            mouseY >= btnY && mouseY <= btnY + btnHeight) {
            hide();
            return true;
        }
        
        // Click outside menu
        if (mouseX < menuX || mouseX > menuX + menuWidth ||
            mouseY < menuY || mouseY > menuY + menuHeight) {
            hide();
            return true;
        }
        
        return true;
    }
    
    /**
     * Checks if harvest is possible.
     */
    private boolean canHarvest() {
        if (targetNode == null || !targetNode.isHarvestable()) return false;
        if (playerEnergy != null && playerEnergy.getCurrentEnergy() < energyCost) return false;
        if (actionProgress != null && actionProgress.isInProgress()) return false;
        return true;
    }
    
    /**
     * Performs the harvest action using timed progress.
     */
    private void performHarvest() {
        if (targetNode == null) return;
        
        // Consume energy upfront
        if (playerEnergy != null) {
            playerEnergy.consumeEnergy(energyCost);
        }
        
        // Save target info for callback
        final ResourceNodeBase node = targetNode;
        final String actionVerb = getActionVerb(node);
        
        // Hide menu while action is in progress
        hide();
        
        // Start timed action if action progress is available
        if (actionProgress != null) {
            actionProgress.startAction(
                actionVerb + "ing " + node.getDisplayName(),
                GATHERING_DURATION,
                () -> {
                    // On complete: get yield and callback
                    int yield = node.harvest();
                    String itemId = node.getItemId();
                    
                    if (harvestCallback != null && yield > 0) {
                        harvestCallback.onHarvest(node, yield, itemId);
                    }
                },
                () -> {
                    // On cancel: refund half energy
                    if (playerEnergy != null) {
                        playerEnergy.addEnergy(energyCost / 2);
                    }
                }
            );
        } else {
            // Fallback to instant if no action progress (legacy)
            if (gameTime != null) {
                gameTime.advanceTime(15);
            }
            
            int yield = node.harvest();
            String itemId = node.getItemId();
            
            if (harvestCallback != null && yield > 0) {
                harvestCallback.onHarvest(node, yield, itemId);
            }
        }
    }
    
    /**
     * Renders the menu.
     */
    public void render(GraphicsContext gc) {
        if (!visible || targetNode == null) return;
        
        // Background
        gc.setFill(BG_DARK.deriveColor(0, 1, 1, 0.95));
        gc.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 10, 10);
        
        // Border
        gc.setStroke(BORDER);
        gc.setLineWidth(2);
        gc.strokeRoundRect(menuX, menuY, menuWidth, menuHeight, 10, 10);
        
        // Header
        gc.setFill(BG_MED);
        gc.fillRoundRect(menuX + 2, menuY + 2, menuWidth - 4, 35, 8, 8);
        
        // Title with icon
        String icon = getResourceIcon(targetNode);
        gc.setFill(TEXT_GOLD);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        gc.fillText(icon + " " + targetNode.getDisplayName(), menuX + 15, menuY + 25);
        
        // Resource info
        gc.setFill(TEXT_WHITE);
        gc.setFont(Font.font("Georgia", 12));
        
        double infoY = menuY + 55;
        
        // Resource type
        String resourceName = targetNode.getResourceType().toString();
        if (targetNode.getResourceType() instanceof Enum) {
            resourceName = ((Enum<?>) targetNode.getResourceType()).name().replace("_", " ");
            resourceName = resourceName.substring(0, 1) + resourceName.substring(1).toLowerCase();
        }
        gc.fillText("Type: " + resourceName, menuX + 15, infoY);
        
        // Status
        gc.fillText("Status: " + targetNode.getStatusText(), menuX + 15, infoY + 20);
        
        // Expected yield (use cached value to prevent flickering)
        gc.fillText("Est. Yield: ~" + cachedEstimatedYield + " " + targetNode.getResourceCategory().toLowerCase(), 
                    menuX + 15, infoY + 40);
        
        // Action time
        gc.setFill(Color.web("#80a0c0"));
        gc.fillText("Time: " + GATHERING_DURATION + " minutes", menuX + 15, infoY + 60);
        
        // Energy cost
        String energyColor = (playerEnergy != null && playerEnergy.getCurrentEnergy() >= energyCost) 
                            ? "#80c080" : "#c08080";
        gc.setFill(Color.web(energyColor));
        gc.fillText("Energy Cost: " + energyCost, menuX + 15, infoY + 80);
        
        // Current energy
        if (playerEnergy != null) {
            gc.setFill(TEXT_WHITE.deriveColor(0, 1, 1, 0.7));
            gc.setFont(Font.font("Georgia", 10));
            gc.fillText("Current Energy: " + playerEnergy.getCurrentEnergy() + "/" + playerEnergy.getMaxEnergy(),
                       menuX + 15, infoY + 98);
        }
        
        // Buttons
        renderButtons(gc);
    }
    
    private void renderButtons(GraphicsContext gc) {
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        
        boolean canHarvestNow = canHarvest();
        String actionVerb = getActionVerb(targetNode);
        
        // Harvest button
        double harvestX = menuX + menuWidth / 2 - btnWidth - 10;
        Color harvestBg = !canHarvestNow ? BUTTON_DISABLED : 
                          (harvestHovered ? BUTTON_HOVER : BUTTON_NORMAL);
        gc.setFill(harvestBg);
        gc.fillRoundRect(harvestX, btnY, btnWidth, btnHeight, 5, 5);
        gc.setStroke(canHarvestNow ? BORDER : BORDER.deriveColor(0, 0.5, 0.7, 1));
        gc.setLineWidth(1);
        gc.strokeRoundRect(harvestX, btnY, btnWidth, btnHeight, 5, 5);
        
        gc.setFill(canHarvestNow ? TEXT_GOLD : TEXT_WHITE.deriveColor(0, 0.5, 0.7, 1));
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        gc.fillText(actionVerb, harvestX + btnWidth / 2 - 25, btnY + 20);
        
        // Cancel button
        double cancelX = menuX + menuWidth / 2 + 10;
        gc.setFill(cancelHovered ? BUTTON_HOVER : BUTTON_NORMAL);
        gc.fillRoundRect(cancelX, btnY, btnWidth, btnHeight, 5, 5);
        gc.setStroke(BORDER);
        gc.strokeRoundRect(cancelX, btnY, btnWidth, btnHeight, 5, 5);
        
        gc.setFill(TEXT_WHITE);
        gc.fillText("Cancel", cancelX + btnWidth / 2 - 22, btnY + 20);
        
        // Hint
        gc.setFill(TEXT_WHITE.deriveColor(0, 1, 1, 0.5));
        gc.setFont(Font.font("Georgia", 9));
        gc.fillText("Right-click again to close", menuX + menuWidth / 2 - 60, menuY + menuHeight - 15);
    }
    
    /**
     * Gets the current target node.
     */
    public ResourceNodeBase getTargetNode() {
        return targetNode;
    }
}
