import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Handles timed actions with a visual progress bar.
 * Actions take game time to complete and show a loading bar.
 * Time flows 4x faster during gathering for better gameplay.
 */
public class ActionProgress {
    
    /**
     * Types of resource gathering for animation purposes.
     */
    public enum GatherType {
        MINING,      // Hammer hitting rock
        FARMING,     // Scythe slashing
        FISHING,     // Fish jumping
        HUNTING,     // Butcher knife
        LUMBER,      // Axe chopping
        GENERIC      // Default (no special animation)
    }
    
    // Action state
    private boolean inProgress = false;
    private String actionName = "";
    private int totalGameMinutes = 0;      // Total game minutes for action
    private int elapsedGameMinutes = 0;    // Game minutes elapsed
    private Runnable onComplete = null;    // Callback when action completes
    private Runnable onCancel = null;      // Callback when action is cancelled
    private GatherType gatherType = GatherType.GENERIC;  // Current gathering type
    
    // Time multiplier during gathering (4x faster)
    private static final double GATHERING_TIME_MULTIPLIER = 4.0;
    private double previousTimeMultiplier = 1.0;  // Store original speed
    
    // Reference to game time for tracking
    private GameTime gameTime;
    private int startGameMinute = 0;  // Total game minutes when action started
    
    // Reference to player for movement lock
    private PlayerSprite player;
    
    // Display position
    private double displayX = 0;
    private double displayY = 0;
    private static final double BAR_WIDTH = 200;
    private static final double BAR_HEIGHT = 25;
    
    // Animation
    private double animationPhase = 0;
    
    /**
     * Creates a new ActionProgress system.
     */
    public ActionProgress(GameTime gameTime) {
        this.gameTime = gameTime;
    }
    
    /**
     * Sets the player reference for movement locking.
     */
    public void setPlayer(PlayerSprite player) {
        this.player = player;
    }
    
    /**
     * Starts a timed action.
     * @param name The name of the action to display
     * @param durationMinutes How long the action takes in game minutes
     * @param onComplete Callback when action completes successfully
     * @param onCancel Callback when action is cancelled (can be null)
     */
    public void startAction(String name, int durationMinutes, Runnable onComplete, Runnable onCancel) {
        startAction(name, durationMinutes, GatherType.GENERIC, onComplete, onCancel);
    }
    
    /**
     * Starts a timed action with gathering type for animations.
     * @param name The name of the action to display
     * @param durationMinutes How long the action takes in game minutes
     * @param type The type of gathering for animation
     * @param onComplete Callback when action completes successfully
     * @param onCancel Callback when action is cancelled (can be null)
     */
    public void startAction(String name, int durationMinutes, GatherType type, Runnable onComplete, Runnable onCancel) {
        this.actionName = name;
        this.totalGameMinutes = durationMinutes;
        this.elapsedGameMinutes = 0;
        this.startGameMinute = gameTime.getTotalMinutes();
        this.onComplete = onComplete;
        this.onCancel = onCancel;
        this.gatherType = type;
        this.inProgress = true;
        this.animationPhase = 0;
        
        // Speed up time during gathering
        this.previousTimeMultiplier = gameTime.getTimeMultiplier();
        gameTime.setTimeMultiplier(GATHERING_TIME_MULTIPLIER);
        
        // Lock player movement
        if (player != null) {
            player.setMovementLocked(true);
        }
    }
    
    /**
     * Starts a timed action with default 30 minute duration.
     */
    public void startAction(String name, Runnable onComplete) {
        startAction(name, 30, onComplete, null);
    }

    
    /**
     * Updates the action progress based on elapsed game time.
     * Should be called every frame.
     * @param deltaTime Real time elapsed in seconds (for animation)
     */
    public void update(double deltaTime) {
        if (!inProgress || gameTime == null) return;
        
        // Update animation
        animationPhase += deltaTime * 2;
        if (animationPhase > Math.PI * 2) {
            animationPhase -= Math.PI * 2;
        }
        
        // Check if game time is paused
        if (gameTime.isPaused()) return;
        
        // Calculate elapsed game minutes
        int currentGameMinute = gameTime.getTotalMinutes();
        elapsedGameMinutes = currentGameMinute - startGameMinute;
        
        // Check if action is complete
        if (elapsedGameMinutes >= totalGameMinutes) {
            completeAction();
        }
    }
    
    /**
     * Completes the action and triggers callback.
     */
    private void completeAction() {
        inProgress = false;
        if (onComplete != null) {
            onComplete.run();
        }
        reset();
    }
    
    /**
     * Cancels the current action.
     */
    public void cancel() {
        if (!inProgress) return;
        inProgress = false;
        if (onCancel != null) {
            onCancel.run();
        }
        reset();
    }
    
    /**
     * Resets the progress state.
     */
    private void reset() {
        // Restore original time speed only if we still own the gathering multiplier.
        // If the player changed the speed during the action, preserve their choice.
        if (gameTime != null) {
            if (Math.abs(gameTime.getTimeMultiplier() - GATHERING_TIME_MULTIPLIER) < 0.01) {
                gameTime.setTimeMultiplier(previousTimeMultiplier);
            } else {
                // Player modified the time while gathering; do not override.
                System.out.println("ActionProgress: player changed time multiplier during action; leaving it at " + gameTime.getTimeMultiplier());
            }
        }
        
        // Unlock player movement
        if (player != null) {
            player.setMovementLocked(false);
        }
        
        actionName = "";
        totalGameMinutes = 0;
        elapsedGameMinutes = 0;
        onComplete = null;
        onCancel = null;
        gatherType = GatherType.GENERIC;
    }
    
    /**
     * Sets the display position for the progress bar.
     */
    public void setPosition(double x, double y) {
        this.displayX = x;
        this.displayY = y;
    }
    
    /**
     * Renders the progress bar overlay.
     * Should be called during the render phase.
     */
    public void render(GraphicsContext gc, double canvasWidth, double canvasHeight) {
        if (!inProgress) return;
        
        // Center the progress bar on screen
        double x = (canvasWidth - BAR_WIDTH) / 2;
        double y = canvasHeight / 2 - BAR_HEIGHT / 2;
        
        // Background overlay (semi-transparent)
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        // Progress bar container - taller to accommodate animation
        double containerPadding = 20;
        double containerWidth = BAR_WIDTH + containerPadding * 2;
        double containerHeight = BAR_HEIGHT + 80; // Extra height for animation
        double containerX = x - containerPadding;
        double containerY = y - 55; // Adjusted for animation space
        
        // Container background
        gc.setFill(Color.web("#1a1208").deriveColor(0, 1, 1, 0.95));
        gc.fillRoundRect(containerX, containerY, containerWidth, containerHeight, 12, 12);
        
        // Container border
        gc.setStroke(Color.web("#c4a574"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(containerX, containerY, containerWidth, containerHeight, 12, 12);
        
        // Action name
        gc.setFill(Color.web("#e8e0d0"));
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        gc.fillText(actionName, containerX + containerPadding, containerY + 25);
        
        // Render gathering animation above progress bar
        renderGatherAnimation(gc, containerX + containerWidth / 2, containerY + 55);
        
        // Progress bar background (positioned lower)
        double barY = y + 10;
        gc.setFill(Color.web("#2a2a2a"));
        gc.fillRoundRect(x, barY, BAR_WIDTH, BAR_HEIGHT, 5, 5);
        
        // Progress bar fill
        double progress = Math.min(1.0, (double) elapsedGameMinutes / totalGameMinutes);
        double fillWidth = BAR_WIDTH * progress;
        
        // Animated gradient effect
        double shimmer = Math.sin(animationPhase) * 0.1 + 0.9;
        Color fillColor = Color.web("#c4a574").deriveColor(0, 1, shimmer, 1);
        gc.setFill(fillColor);
        gc.fillRoundRect(x, barY, fillWidth, BAR_HEIGHT, 5, 5);
        
        // Shimmer highlight
        if (fillWidth > 10) {
            double shimmerX = x + (fillWidth - 10) * ((Math.sin(animationPhase * 2) + 1) / 2);
            gc.setFill(Color.rgb(255, 255, 255, 0.3));
            gc.fillRect(shimmerX, barY + 2, 8, BAR_HEIGHT - 4);
        }
        
        // Progress bar border
        gc.setStroke(Color.web("#c4a574").darker());
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, barY, BAR_WIDTH, BAR_HEIGHT, 5, 5);
        
        // Progress percentage
        int percentage = (int) (progress * 100);
        gc.setFill(Color.web("#ffffff"));
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        String progressText = percentage + "%";
        double textWidth = gc.getFont().getSize() * progressText.length() * 0.5;
        gc.fillText(progressText, x + BAR_WIDTH / 2 - textWidth / 2, barY + BAR_HEIGHT / 2 + 4);
        
        // Time remaining
        int remainingMinutes = totalGameMinutes - elapsedGameMinutes;
        String timeText = formatTime(remainingMinutes) + " remaining";
        gc.setFill(Color.web("#aaaaaa"));
        gc.setFont(Font.font("Georgia", 10));
        gc.fillText(timeText, containerX + containerPadding, containerY + containerHeight - 10);
        
        // Cancel hint
        gc.setFill(Color.web("#888888"));
        gc.fillText("[Esc] Cancel", containerX + containerWidth - 70, containerY + containerHeight - 10);
    }
    
    /**
     * Renders the gathering animation based on type.
     */
    private void renderGatherAnimation(GraphicsContext gc, double centerX, double centerY) {
        double size = 30;
        double animOffset = Math.sin(animationPhase * 3) * 5; // Bouncing motion
        double swingAngle = Math.sin(animationPhase * 4) * 30; // Swinging motion
        
        gc.save();
        gc.translate(centerX, centerY);
        
        switch (gatherType) {
            case MINING:
                // Hammer hitting rock animation
                gc.save();
                gc.rotate(swingAngle);
                renderHammer(gc, -size/2, -size/2 + animOffset, size);
                gc.restore();
                renderRock(gc, size * 0.3, 0, size * 0.6);
                // Sparkle effects on hit
                if (Math.sin(animationPhase * 4) > 0.8) {
                    renderSparkles(gc, size * 0.2, -5);
                }
                break;
                
            case FARMING:
                // Scythe slashing animation
                gc.save();
                gc.rotate(-20 + swingAngle);
                renderScythe(gc, -size/2, -size/2, size);
                gc.restore();
                renderWheat(gc, size * 0.4, 5, size * 0.5);
                break;
                
            case FISHING:
                // Fish jumping animation
                double jumpY = -Math.abs(Math.sin(animationPhase * 2)) * 15;
                renderFish(gc, 0, jumpY, size * 0.8);
                // Water splash
                renderWaterSplash(gc, 0, 10);
                break;
                
            case HUNTING:
                // Butcher knife slicing animation
                gc.save();
                gc.rotate(swingAngle * 0.5);
                renderKnife(gc, -size/2, -size/2 + animOffset * 0.5, size);
                gc.restore();
                renderMeat(gc, size * 0.3, 5, size * 0.5);
                break;
                
            case LUMBER:
                // Axe chopping animation
                gc.save();
                gc.rotate(-30 + swingAngle);
                renderAxe(gc, -size/2, -size/2, size);
                gc.restore();
                renderLog(gc, size * 0.3, 5, size * 0.5);
                // Wood chip effects
                if (Math.sin(animationPhase * 4) > 0.8) {
                    renderWoodChips(gc, size * 0.2, 0);
                }
                break;
                
            case GENERIC:
            default:
                // Simple spinning gears animation
                renderGears(gc, 0, 0, size);
                break;
        }
        
        gc.restore();
    }
    
    // ==================== Animation Sprite Rendering ====================
    
    private void renderHammer(GraphicsContext gc, double x, double y, double size) {
        // Handle
        gc.setFill(Color.web("#8B4513"));
        gc.fillRect(x + size * 0.4, y + size * 0.3, size * 0.15, size * 0.6);
        // Head
        gc.setFill(Color.web("#707070"));
        gc.fillRoundRect(x + size * 0.2, y + size * 0.1, size * 0.5, size * 0.3, 4, 4);
        // Highlight
        gc.setFill(Color.web("#909090"));
        gc.fillRect(x + size * 0.25, y + size * 0.15, size * 0.15, size * 0.1);
    }
    
    private void renderRock(GraphicsContext gc, double x, double y, double size) {
        gc.setFill(Color.web("#606060"));
        gc.fillOval(x, y, size, size * 0.7);
        gc.setFill(Color.web("#505050"));
        gc.fillOval(x + size * 0.1, y + size * 0.1, size * 0.4, size * 0.3);
    }
    
    private void renderSparkles(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#ffff80"));
        for (int i = 0; i < 3; i++) {
            double sx = x + Math.cos(animationPhase + i * 2) * 8;
            double sy = y + Math.sin(animationPhase + i * 2) * 8;
            gc.fillOval(sx, sy, 3, 3);
        }
    }
    
    private void renderScythe(GraphicsContext gc, double x, double y, double size) {
        // Handle
        gc.setFill(Color.web("#8B4513"));
        gc.fillRect(x + size * 0.4, y + size * 0.2, size * 0.12, size * 0.7);
        // Blade (curved)
        gc.setFill(Color.web("#c0c0c0"));
        gc.fillArc(x, y, size * 0.8, size * 0.5, 0, 180, javafx.scene.shape.ArcType.ROUND);
    }
    
    private void renderWheat(GraphicsContext gc, double x, double y, double size) {
        gc.setStroke(Color.web("#d4a017"));
        gc.setLineWidth(2);
        for (int i = 0; i < 3; i++) {
            double wx = x + i * 5 - 5;
            gc.strokeLine(wx, y + size, wx, y);
            // Wheat head
            gc.setFill(Color.web("#d4a017"));
            gc.fillOval(wx - 2, y - 5, 4, 10);
        }
    }
    
    private void renderFish(GraphicsContext gc, double x, double y, double size) {
        // Body
        gc.setFill(Color.web("#4a90b8"));
        gc.fillOval(x - size/2, y - size * 0.25, size, size * 0.5);
        // Tail
        gc.fillPolygon(
            new double[] {x - size/2, x - size/2 - size * 0.3, x - size/2 - size * 0.3},
            new double[] {y, y - size * 0.2, y + size * 0.2},
            3
        );
        // Eye
        gc.setFill(Color.WHITE);
        gc.fillOval(x + size * 0.2, y - size * 0.1, size * 0.12, size * 0.12);
        gc.setFill(Color.BLACK);
        gc.fillOval(x + size * 0.22, y - size * 0.08, size * 0.06, size * 0.06);
    }
    
    private void renderWaterSplash(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#80c0ff").deriveColor(0, 1, 1, 0.6));
        double splashPhase = Math.abs(Math.sin(animationPhase * 2));
        for (int i = 0; i < 5; i++) {
            double sx = x + (i - 2) * 6;
            double sy = y - splashPhase * 10 * (1 - Math.abs(i - 2) * 0.3);
            gc.fillOval(sx - 2, sy, 4, 6);
        }
    }
    
    private void renderKnife(GraphicsContext gc, double x, double y, double size) {
        // Handle
        gc.setFill(Color.web("#5a3a20"));
        gc.fillRoundRect(x + size * 0.5, y + size * 0.5, size * 0.35, size * 0.15, 3, 3);
        // Blade
        gc.setFill(Color.web("#d0d0d0"));
        gc.fillPolygon(
            new double[] {x + size * 0.1, x + size * 0.5, x + size * 0.5},
            new double[] {y + size * 0.55, y + size * 0.45, y + size * 0.65},
            3
        );
    }
    
    private void renderMeat(GraphicsContext gc, double x, double y, double size) {
        // Main meat
        gc.setFill(Color.web("#c04040"));
        gc.fillOval(x, y, size, size * 0.6);
        // Fat streaks
        gc.setFill(Color.web("#f0d0c0"));
        gc.fillOval(x + size * 0.2, y + size * 0.1, size * 0.2, size * 0.15);
        gc.fillOval(x + size * 0.5, y + size * 0.3, size * 0.15, size * 0.1);
    }
    
    private void renderAxe(GraphicsContext gc, double x, double y, double size) {
        // Handle
        gc.setFill(Color.web("#8B4513"));
        gc.fillRect(x + size * 0.4, y + size * 0.2, size * 0.12, size * 0.7);
        // Blade
        gc.setFill(Color.web("#808080"));
        gc.fillPolygon(
            new double[] {x + size * 0.2, x + size * 0.5, x + size * 0.5},
            new double[] {y + size * 0.15, y, y + size * 0.35},
            3
        );
    }
    
    private void renderLog(GraphicsContext gc, double x, double y, double size) {
        // Log body
        gc.setFill(Color.web("#8B4513"));
        gc.fillRoundRect(x, y, size, size * 0.4, 5, 5);
        // Log end
        gc.setFill(Color.web("#d4a060"));
        gc.fillOval(x + size - 3, y, 6, size * 0.4);
        // Rings
        gc.setStroke(Color.web("#6a3000"));
        gc.setLineWidth(1);
        gc.strokeOval(x + size - 1, y + size * 0.1, 3, size * 0.2);
    }
    
    private void renderWoodChips(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#d4a060"));
        for (int i = 0; i < 4; i++) {
            double cx = x + Math.cos(animationPhase * 2 + i) * 12;
            double cy = y + Math.sin(animationPhase * 2 + i) * 8 - 5;
            gc.fillRect(cx, cy, 4, 2);
        }
    }
    
    private void renderGears(GraphicsContext gc, double x, double y, double size) {
        gc.save();
        gc.rotate(animationPhase * 30); // Spinning
        gc.setFill(Color.web("#707070"));
        // Outer gear
        gc.fillOval(x - size/2, y - size/2, size, size);
        gc.setFill(Color.web("#505050"));
        gc.fillOval(x - size * 0.3, y - size * 0.3, size * 0.6, size * 0.6);
        // Gear teeth
        gc.setFill(Color.web("#707070"));
        for (int i = 0; i < 8; i++) {
            double angle = i * 45 * Math.PI / 180;
            double tx = x + Math.cos(angle) * size * 0.45;
            double ty = y + Math.sin(angle) * size * 0.45;
            gc.fillRect(tx - 3, ty - 3, 6, 6);
        }
        gc.restore();
    }
    
    /**
     * Formats game minutes as hours and minutes.
     */
    private String formatTime(int minutes) {
        if (minutes >= 60) {
            int hours = minutes / 60;
            int mins = minutes % 60;
            return hours + "h " + mins + "m";
        }
        return minutes + "m";
    }
    
    /**
     * Handles key press events.
     * @return true if the key was handled
     */
    public boolean handleKeyPress(javafx.scene.input.KeyCode keyCode) {
        if (!inProgress) return false;
        
        if (keyCode == javafx.scene.input.KeyCode.ESCAPE) {
            cancel();
            return true;
        }
        return false;
    }
    
    /**
     * Checks if an action is currently in progress.
     */
    public boolean isInProgress() {
        return inProgress;
    }
    
    /**
     * Gets the current progress as a value from 0.0 to 1.0.
     */
    public double getProgress() {
        if (totalGameMinutes == 0) return 0;
        return Math.min(1.0, (double) elapsedGameMinutes / totalGameMinutes);
    }
    
    /**
     * Gets the name of the current action.
     */
    public String getActionName() {
        return actionName;
    }
}
