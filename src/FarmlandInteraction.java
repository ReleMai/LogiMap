import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Interaction menu for farmland nodes.
 * Shows when player right-clicks on harvestable farmland.
 */
public class FarmlandInteraction {
    
    // Medieval theme colors
    private static final Color BG_DARK = Color.web("#1a1208");
    private static final Color BG_MED = Color.web("#2a1f10");
    private static final Color BORDER = Color.web("#c4a574");
    private static final Color TEXT_GOLD = Color.web("#c4a574");
    private static final Color TEXT_WHITE = Color.web("#e8e0d0");
    private static final Color BUTTON_NORMAL = Color.web("#3a2a15");
    private static final Color BUTTON_HOVER = Color.web("#4a3a25");
    private static final Color BUTTON_DISABLED = Color.web("#2a2010");
    
    // Menu state
    private boolean visible = false;
    private double menuX, menuY;
    private double menuWidth = 280;
    private double menuHeight = 200;
    
    // Current target
    private FarmlandNode targetFarmland;
    private GameTime gameTime;
    private PlayerEnergy playerEnergy;
    
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
        
        // Check harvest button bounds (relative to menu)
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        double harvestBtnX = menuX + 20;
        double cancelBtnX = menuX + menuWidth - 120;
        
        harvestHovered = mouseX >= harvestBtnX && mouseX <= harvestBtnX + btnWidth &&
                        mouseY >= btnY && mouseY <= btnY + btnHeight;
        
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
        
        // Check buttons
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        double harvestBtnX = menuX + 20;
        double cancelBtnX = menuX + menuWidth - 120;
        
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
     * Performs the harvest action.
     */
    private void performHarvest() {
        if (targetFarmland == null) return;
        
        // Perform harvest
        int yield = targetFarmland.harvest(gameTime, playerEnergy);
        
        if (yield > 0) {
            // Advance time for farming action (30 minutes)
            gameTime.advanceTime(30);
            
            // Notify callback
            if (harvestCallback != null) {
                harvestCallback.onHarvest(targetFarmland, yield, targetFarmland.getGrainType());
            }
        }
        
        hide();
    }
    
    /**
     * Renders the interaction menu.
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
        
        // Title
        gc.setFill(TEXT_GOLD);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        gc.fillText(targetFarmland.getGrainType().getDisplayName() + " Field", menuX + 15, menuY + 25);
        
        // Info section
        double infoY = menuY + 50;
        gc.setFill(TEXT_WHITE);
        gc.setFont(Font.font("Georgia", 12));
        
        // Growth stage
        gc.fillText("Status: " + targetFarmland.getGrowthStage().getDisplayName(), menuX + 15, infoY);
        infoY += 20;
        
        // Harvests remaining
        gc.fillText("Harvests left: " + targetFarmland.getHarvestsRemaining(), menuX + 15, infoY);
        infoY += 20;
        
        // Time of day
        String timeStr = gameTime.canFarm() ? "Daytime" : "Night (cannot farm)";
        Color timeColor = gameTime.canFarm() ? TEXT_WHITE : Color.web("#ff6060");
        gc.setFill(timeColor);
        gc.fillText("Time: " + timeStr, menuX + 15, infoY);
        infoY += 20;
        
        // Energy
        String energyStr = playerEnergy.getFormattedEnergy();
        Color energyColor = playerEnergy.canFarm() ? TEXT_WHITE : Color.web("#ff6060");
        gc.setFill(energyColor);
        gc.fillText("Energy: " + energyStr + " (Cost: " + PlayerEnergy.FARMING_COST + ")", menuX + 15, infoY);
        
        // Buttons
        double btnY = menuY + menuHeight - 70;
        double btnWidth = 100;
        double btnHeight = 30;
        
        // Harvest button
        boolean canHarvestNow = canHarvest();
        drawButton(gc, menuX + 20, btnY, btnWidth, btnHeight, "Harvest", 
                  canHarvestNow, harvestHovered && canHarvestNow);
        
        // Cancel button
        drawButton(gc, menuX + menuWidth - 120, btnY, btnWidth, btnHeight, "Cancel", 
                  true, cancelHovered);
        
        // Cannot harvest reason
        if (!canHarvestNow) {
            gc.setFill(Color.web("#ff8080"));
            gc.setFont(Font.font("Georgia", 10));
            gc.fillText(getCannotHarvestReason(), menuX + 15, menuY + menuHeight - 20);
        }
    }
    
    /**
     * Draws a button.
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
