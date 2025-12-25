import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Interaction menu for farmland nodes.
 * Shows when player right-clicks on harvestable farmland.
 * 
 * Style unified with ResourceInteraction for consistent look across all resource types.
 */
public class FarmlandInteraction {
    
    // Medieval theme colors (unified with ResourceInteraction)
    private static final Color BG_DARK = Color.web("#1a1208");
    private static final Color BG_MED = Color.web("#2a1f10");
    private static final Color BORDER = Color.web("#c4a574");
    private static final Color TEXT_GOLD = Color.web("#c4a574");
    private static final Color TEXT_WHITE = Color.web("#e8e0d0");
    private static final Color BUTTON_NORMAL = Color.web("#3a2a15");
    private static final Color BUTTON_HOVER = Color.web("#4a3a25");
    private static final Color BUTTON_DISABLED = Color.web("#2a2010");
    
    // Menu state (unified dimensions with ResourceInteraction)
    private boolean visible = false;
    private double menuX, menuY;
    private double menuWidth = 280;
    private double menuHeight = 250;
    
    // Action duration in game minutes
    private static final int HARVEST_DURATION = 5;
    
    // Current target
    private FarmlandNode targetFarmland;
    private GameTime gameTime;
    private PlayerEnergy playerEnergy;
    private ActionProgress actionProgress;  // For timed actions
    
    // Button states
    private boolean harvestHovered = false;
    private boolean cancelHovered = false;
    
    // Callback for harvest action
    private HarvestCallback harvestCallback;
    
    /**
     * Callback interface for harvest action.
     */
    public interface HarvestCallback {
        void onHarvest(FarmlandNode farmland, int yield, GrainType grainType);
    }
    
    /**
     * Creates the interaction menu.
     */
    public FarmlandInteraction(GameTime gameTime, PlayerEnergy playerEnergy) {
        this.gameTime = gameTime;
        this.playerEnergy = playerEnergy;
    }
    
    /**
     * Sets the harvest callback.
     */
    public void setHarvestCallback(HarvestCallback callback) {
        this.harvestCallback = callback;
    }
    
    /**
     * Sets the action progress system for timed harvesting.
     */
    public void setActionProgress(ActionProgress actionProgress) {
        this.actionProgress = actionProgress;
    }
    
    /**
     * Shows the menu for a farmland node.
     */
    public void show(FarmlandNode farmland, double screenX, double screenY) {
        this.targetFarmland = farmland;
        this.visible = true;
        
        // Position menu near click, but keep on screen
        this.menuX = screenX - menuWidth / 2;
        this.menuY = screenY - menuHeight - 20;
        
        if (menuY < 10) menuY = screenY + 20;
    }
    
    /**
     * Hides the menu.
     */
    public void hide() {
        this.visible = false;
        this.targetFarmland = null;
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
        
        // Button bounds (centered like ResourceInteraction)
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        
        // Harvest button (left of center)
        double harvestBtnX = menuX + menuWidth / 2 - btnWidth - 10;
        harvestHovered = mouseX >= harvestBtnX && mouseX <= harvestBtnX + btnWidth &&
                        mouseY >= btnY && mouseY <= btnY + btnHeight;
        
        // Cancel button (right of center)
        double cancelBtnX = menuX + menuWidth / 2 + 10;
        cancelHovered = mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnWidth &&
                       mouseY >= btnY && mouseY <= btnY + btnHeight;
    }
    
    /**
     * Handles mouse click. Returns true if click was handled.
     */
    public boolean onClick(double mouseX, double mouseY) {
        if (!visible) return false;
        
        // Check if click is outside menu
        if (mouseX < menuX || mouseX > menuX + menuWidth ||
            mouseY < menuY || mouseY > menuY + menuHeight) {
            hide();
            return true;
        }
        
        // Check buttons (centered positioning)
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        double harvestBtnX = menuX + menuWidth / 2 - btnWidth - 10;
        double cancelBtnX = menuX + menuWidth / 2 + 10;
        
        // Harvest button
        if (mouseX >= harvestBtnX && mouseX <= harvestBtnX + btnWidth &&
            mouseY >= btnY && mouseY <= btnY + btnHeight) {
            
            if (canHarvest()) {
                performHarvest();
            }
            return true;
        }
        
        // Cancel button
        if (mouseX >= cancelBtnX && mouseX <= cancelBtnX + btnWidth &&
            mouseY >= btnY && mouseY <= btnY + btnHeight) {
            hide();
            return true;
        }
        
        return true; // Click was on menu
    }
    
    /**
     * Checks if harvesting is currently possible.
     */
    private boolean canHarvest() {
        if (targetFarmland == null) return false;
        if (!targetFarmland.isHarvestable()) return false;
        if (!gameTime.canFarm()) return false;
        if (!playerEnergy.canFarm()) return false;
        return true;
    }
    
    /**
     * Gets the reason why harvesting is not possible.
     */
    private String getCannotHarvestReason() {
        if (targetFarmland == null) return "No target";
        if (!targetFarmland.isHarvestable()) return "Not ready to harvest";
        if (!gameTime.canFarm()) return "Cannot farm at night";
        if (!playerEnergy.canFarm()) return "Too tired to farm";
        return "";
    }
    
    /**
     * Performs the harvest action using timed progress.
     */
    private void performHarvest() {
        if (targetFarmland == null) return;
        
        // Consume energy upfront
        if (!playerEnergy.consumeEnergy(PlayerEnergy.FARMING_COST)) {
            return;
        }
        
        // Save target info for callback
        final FarmlandNode farmland = targetFarmland;
        
        // Hide menu while action is in progress
        hide();
        
        // Start timed action if action progress is available
        if (actionProgress != null) {
            actionProgress.startAction(
                "Harvesting " + farmland.getGrainType().getDisplayName(),
                HARVEST_DURATION,
                () -> {
                    // On complete: perform actual harvest
                    int yield = farmland.harvestDirect();
                    
                    if (yield > 0 && harvestCallback != null) {
                        harvestCallback.onHarvest(farmland, yield, farmland.getGrainType());
                    }
                },
                () -> {
                    // On cancel: refund half energy
                    playerEnergy.addEnergy(PlayerEnergy.FARMING_COST / 2);
                }
            );
        } else {
            // Fallback to instant if no action progress (legacy)
            gameTime.advanceTime(30);
            int yield = farmland.harvestDirect();
            
            if (yield > 0 && harvestCallback != null) {
                harvestCallback.onHarvest(farmland, yield, farmland.getGrainType());
            }
        }
    }
    
    /**
     * Renders the interaction menu (unified style with ResourceInteraction).
     */
    public void render(GraphicsContext gc) {
        if (!visible || targetFarmland == null) return;
        
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
        gc.setFill(TEXT_GOLD);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        gc.fillText("🌾 " + targetFarmland.getGrainType().getDisplayName() + " Field", menuX + 15, menuY + 25);
        
        // Info section
        gc.setFill(TEXT_WHITE);
        gc.setFont(Font.font("Georgia", 12));
        
        double infoY = menuY + 55;
        
        // Growth stage/type
        gc.fillText("Type: " + targetFarmland.getGrainType().getDisplayName(), menuX + 15, infoY);
        
        // Status
        gc.fillText("Status: " + targetFarmland.getGrowthStage().getDisplayName(), menuX + 15, infoY + 20);
        
        // Expected yield
        int estimatedYield = targetFarmland.getEstimatedYield();
        gc.fillText("Est. Yield: ~" + estimatedYield + " grain", menuX + 15, infoY + 40);
        
        // Action time
        gc.setFill(Color.web("#80a0c0"));
        gc.fillText("Time: " + HARVEST_DURATION + " minutes", menuX + 15, infoY + 60);
        
        // Energy cost
        int energyCost = PlayerEnergy.FARMING_COST;
        String energyColor = playerEnergy.canFarm() ? "#80c080" : "#c08080";
        gc.setFill(Color.web(energyColor));
        gc.fillText("Energy Cost: " + energyCost, menuX + 15, infoY + 80);
        
        // Current energy
        gc.setFill(TEXT_WHITE.deriveColor(0, 1, 1, 0.7));
        gc.setFont(Font.font("Georgia", 10));
        gc.fillText("Current Energy: " + playerEnergy.getCurrentEnergy() + "/" + playerEnergy.getMaxEnergy(),
                   menuX + 15, infoY + 98);
        
        // Buttons (centered positioning)
        renderButtons(gc);
    }
    
    /**
     * Renders the buttons (unified style with ResourceInteraction).
     */
    private void renderButtons(GraphicsContext gc) {
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        
        boolean canHarvestNow = canHarvest();
        
        // Harvest button (left of center)
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
        gc.fillText("Harvest", harvestX + btnWidth / 2 - 25, btnY + 20);
        
        // Cancel button (right of center)
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
     * Draws a button (legacy method - kept for compatibility).
     */
    private void drawButton(GraphicsContext gc, double x, double y, double w, double h,
                           String text, boolean enabled, boolean hovered) {
        // Background
        Color bgColor = !enabled ? BUTTON_DISABLED : (hovered ? BUTTON_HOVER : BUTTON_NORMAL);
        gc.setFill(bgColor);
        gc.fillRoundRect(x, y, w, h, 5, 5);
        
        // Border
        gc.setStroke(enabled ? BORDER : BORDER.darker());
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, w, h, 5, 5);
        
        // Text
        gc.setFill(enabled ? TEXT_WHITE : TEXT_WHITE.darker());
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        double textWidth = text.length() * 7;
        gc.fillText(text, x + (w - textWidth) / 2, y + h / 2 + 4);
    }
    
    public FarmlandNode getTargetFarmland() {
        return targetFarmland;
    }
}
