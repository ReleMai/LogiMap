import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * BACKUP FILE - Original Player character sprite with equipment visualization.
 * Renders a humanoid figure that can display equipped gear.
 * Base appearance is a peasant in white underwear.
 * 
 * NOTE: This is a backup copy. The class has been renamed to avoid conflicts.
 * The active PlayerSprite.java now uses modular body parts.
 */
public class PlayerSprite_backup {
    
    // Position in grid coordinates
    private double gridX;
    private double gridY;
    
    // Target position for movement
    private double targetGridX;
    private double targetGridY;
    private boolean isMoving = false;
    
    // Movement speed (grid cells per second)
    private static final double MOVE_SPEED = 8.0;
    
    // Equipment slots
    private Equipment headGear;
    private Equipment bodyArmor;
    private Equipment legArmor;
    private Equipment footwear;
    private Equipment mainHand;  // Weapon
    private Equipment offHand;   // Shield or second weapon
    private Equipment cape;
    
    // Player stats
    private String name;
    private int gold = 100;
    private int health = 100;
    private int maxHealth = 100;
    
    // Currency system
    private Currency currency;
    
    // Player inventory
    private Inventory inventory;
    
    // Animation state
    private double animationTime = 0;
    private boolean facingRight = true;
    
    // Sprite colors (base peasant)
    private static final Color SKIN_COLOR = Color.web("#e8c4a0");
    private static final Color UNDERWEAR_COLOR = Color.web("#f0f0e8");
    private static final Color HAIR_COLOR = Color.web("#4a3728");
    private static final Color OUTLINE_COLOR = Color.web("#2a1a10");
    
    /**
     * Creates a new player sprite at the given position.
     * Player starts in basic white underwear - clothing must be equipped.
     */
    public PlayerSprite_backup(String name, int startX, int startY) {
        this.name = name;
        this.gridX = startX;
        this.gridY = startY;
        this.targetGridX = startX;
        this.targetGridY = startY;
        
        // Player starts with NO clothes equipped (white underwear visible)
        // Ragged clothes are given as items in inventory instead
        this.bodyArmor = null;
        this.legArmor = null;
        
        // Initialize currency (start with 50 copper)
        this.currency = new Currency(0, 0, 50);
        
        // Initialize inventory (4 rows x 6 columns = 24 slots - smaller)
        this.inventory = new Inventory("Backpack", 4, 6);
        
        // Give player starting items (ragged clothes to equip)
        this.inventory.addItem(ItemRegistry.createStack("ragged_shirt", 1));
        this.inventory.addItem(ItemRegistry.createStack("ragged_pants", 1));
        // Some starting supplies
        this.inventory.addItem(ItemRegistry.createStack("bread", 3));
    }
    
    /**
     * Updates player position based on movement target.
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void update(double deltaTime) {
        if (!isMoving) return;
        
        double dx = targetGridX - gridX;
        double dy = targetGridY - gridY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 0.1) {
            // Arrived at destination
            gridX = targetGridX;
            gridY = targetGridY;
            isMoving = false;
        } else {
            // Move toward target
            double moveAmount = MOVE_SPEED * deltaTime;
            if (moveAmount >= distance) {
                gridX = targetGridX;
                gridY = targetGridY;
                isMoving = false;
            } else {
                double ratio = moveAmount / distance;
                gridX += dx * ratio;
                gridY += dy * ratio;
                
                // Update facing direction
                if (Math.abs(dx) > 0.1) {
                    facingRight = dx > 0;
                }
            }
        }
        
        // Update animation
        animationTime += deltaTime;
    }
    
    /**
     * Sets the movement target for the player.
     */
    public void moveTo(double targetX, double targetY) {
        this.targetGridX = targetX;
        this.targetGridY = targetY;
        this.isMoving = true;
        
        // Update facing direction
        double dx = targetX - gridX;
        if (Math.abs(dx) > 0.1) {
            facingRight = dx > 0;
        }
    }
    
    /**
     * Teleports the player to a position instantly.
     */
    public void teleportTo(double x, double y) {
        this.gridX = x;
        this.gridY = y;
        this.targetGridX = x;
        this.targetGridY = y;
        this.isMoving = false;
    }
    
    /**
     * Renders the player sprite at the given screen position.
     */
    public void render(GraphicsContext gc, double screenX, double screenY, double size) {
        // Sprite dimensions
        double spriteWidth = size * 0.6;
        double spriteHeight = size * 0.9;
        double centerX = screenX + size / 2;
        double bottomY = screenY + size;
        
        // Walking animation offset
        double bobOffset = 0;
        double legSwing = 0;
        if (isMoving) {
            bobOffset = Math.sin(animationTime * 10) * size * 0.02;
            legSwing = Math.sin(animationTime * 8) * 0.3;
        }
        
        // Flip for facing direction
        double scaleX = facingRight ? 1 : -1;
        
        gc.save();
        gc.translate(centerX, bottomY + bobOffset);
        gc.scale(scaleX, 1);
        
        // Draw shadow
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(-spriteWidth * 0.4, -size * 0.05, spriteWidth * 0.8, size * 0.1);
        
        // === LEGS (white underwear shorts) ===
        double legWidth = spriteWidth * 0.2;
        double legHeight = spriteHeight * 0.35;
        double legY = -legHeight;
        
        // Left leg
        gc.save();
        if (isMoving) gc.rotate(legSwing * 20);
        drawLeg(gc, -legWidth * 0.8, legY, legWidth, legHeight);
        gc.restore();
        
        // Right leg
        gc.save();
        if (isMoving) gc.rotate(-legSwing * 20);
        drawLeg(gc, legWidth * 0.3, legY, legWidth, legHeight);
        gc.restore();
        
        // === TORSO (bare chest) ===
        double torsoWidth = spriteWidth * 0.5;
        double torsoHeight = spriteHeight * 0.35;
        double torsoY = -legHeight - torsoHeight;
        
        // Draw bare torso (skin color)
        gc.setFill(SKIN_COLOR);
        gc.fillRoundRect(-torsoWidth / 2, torsoY, torsoWidth, torsoHeight, 
                         torsoWidth * 0.3, torsoWidth * 0.3);
        
        // Torso outline
        gc.setStroke(OUTLINE_COLOR);
        gc.setLineWidth(Math.max(1, size * 0.02));
        gc.strokeRoundRect(-torsoWidth / 2, torsoY, torsoWidth, torsoHeight,
                          torsoWidth * 0.3, torsoWidth * 0.3);
        
        // Draw body armor if equipped
        if (bodyArmor != null) {
            bodyArmor.renderOnBody(gc, -torsoWidth / 2, torsoY, torsoWidth, torsoHeight);
        }
        
        // === ARMS ===
        double armWidth = spriteWidth * 0.12;
        double armHeight = spriteHeight * 0.3;
        double armY = torsoY + torsoHeight * 0.1;
        
        // Left arm (behind)
        gc.setFill(SKIN_COLOR);
        gc.fillRoundRect(-torsoWidth / 2 - armWidth * 0.5, armY, armWidth, armHeight,
                        armWidth * 0.5, armWidth * 0.5);
        gc.setStroke(OUTLINE_COLOR);
        gc.strokeRoundRect(-torsoWidth / 2 - armWidth * 0.5, armY, armWidth, armHeight,
                          armWidth * 0.5, armWidth * 0.5);
        
        // Right arm (front)
        gc.setFill(SKIN_COLOR);
        gc.fillRoundRect(torsoWidth / 2 - armWidth * 0.5, armY, armWidth, armHeight,
                        armWidth * 0.5, armWidth * 0.5);
        gc.setStroke(OUTLINE_COLOR);
        gc.strokeRoundRect(torsoWidth / 2 - armWidth * 0.5, armY, armWidth, armHeight,
                          armWidth * 0.5, armWidth * 0.5);
        
        // Draw weapons if equipped
        if (mainHand != null) {
            mainHand.renderInHand(gc, torsoWidth / 2, armY + armHeight, size);
        }
        if (offHand != null) {
            offHand.renderInHand(gc, -torsoWidth / 2, armY + armHeight, size);
        }
        
        // === HEAD ===
        double headSize = spriteWidth * 0.4;
        double headY = torsoY - headSize * 0.8;
        
        // Draw head
        gc.setFill(SKIN_COLOR);
        gc.fillOval(-headSize / 2, headY, headSize, headSize);
        gc.setStroke(OUTLINE_COLOR);
        gc.strokeOval(-headSize / 2, headY, headSize, headSize);
        
        // Draw hair
        gc.setFill(HAIR_COLOR);
        gc.fillArc(-headSize / 2, headY - headSize * 0.1, headSize, headSize * 0.6,
                   0, 180, javafx.scene.shape.ArcType.ROUND);
        
        // Draw eyes
        gc.setFill(Color.web("#2a2a2a"));
        double eyeSize = headSize * 0.12;
        gc.fillOval(headSize * 0.08, headY + headSize * 0.35, eyeSize, eyeSize);
        
        // Draw headgear if equipped
        if (headGear != null) {
            headGear.renderOnHead(gc, -headSize / 2, headY, headSize, headSize);
        }
        
        // Draw cape if equipped
        if (cape != null) {
            cape.renderCape(gc, -torsoWidth / 2, torsoY, torsoWidth, torsoHeight + legHeight);
        }
        
        gc.restore();
        
        // Draw player name above sprite
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 
                   Math.max(10, size * 0.15)));
        double textWidth = name.length() * size * 0.08;
        gc.fillText(name, screenX + size / 2 - textWidth / 2, screenY - size * 0.1);
    }
    
    /**
     * Draws a leg with underwear shorts.
     */
    private void drawLeg(GraphicsContext gc, double x, double y, double width, double height) {
        // Upper leg (underwear)
        gc.setFill(UNDERWEAR_COLOR);
        gc.fillRoundRect(x, y, width, height * 0.4, width * 0.3, width * 0.3);
        gc.setStroke(OUTLINE_COLOR);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, width, height * 0.4, width * 0.3, width * 0.3);
        
        // Lower leg (skin)
        gc.setFill(SKIN_COLOR);
        gc.fillRoundRect(x, y + height * 0.35, width, height * 0.65, width * 0.3, width * 0.3);
        gc.setStroke(OUTLINE_COLOR);
        gc.strokeRoundRect(x, y + height * 0.35, width, height * 0.65, width * 0.3, width * 0.3);
        
        // Draw leg armor if equipped
        if (legArmor != null) {
            legArmor.renderOnLegs(gc, x, y, width, height);
        }
        
        // Draw footwear if equipped
        if (footwear != null) {
            footwear.renderOnFeet(gc, x, y + height * 0.85, width, height * 0.2);
        }
    }
    
    // === Equipment Management ===
    
    /**
     * Equips an item to the appropriate slot based on its type.
     */
    public void equip(Equipment item) {
        if (item == null) return;
        
        switch (item.getSlot()) {
            case HEAD:
                this.headGear = item;
                break;
            case BODY:
                this.bodyArmor = item;
                break;
            case LEGS:
                this.legArmor = item;
                break;
            case FEET:
                this.footwear = item;
                break;
            case MAIN_HAND:
                this.mainHand = item;
                break;
            case OFF_HAND:
                this.offHand = item;
                break;
            case CAPE:
                this.cape = item;
                break;
        }
    }
    
    /**
     * Unequips an item from the specified slot.
     * @return The unequipped item, or null if slot was empty
     */
    public Equipment unequip(Equipment.Slot slot) {
        Equipment removed = null;
        switch (slot) {
            case HEAD:
                removed = headGear;
                headGear = null;
                break;
            case BODY:
                removed = bodyArmor;
                bodyArmor = null;
                break;
            case LEGS:
                removed = legArmor;
                legArmor = null;
                break;
            case FEET:
                removed = footwear;
                footwear = null;
                break;
            case MAIN_HAND:
                removed = mainHand;
                mainHand = null;
                break;
            case OFF_HAND:
                removed = offHand;
                offHand = null;
                break;
            case CAPE:
                removed = cape;
                cape = null;
                break;
        }
        return removed;
    }
    
    public void equipHead(Equipment item) { this.headGear = item; }
    public void equipBody(Equipment item) { this.bodyArmor = item; }
    public void equipLegs(Equipment item) { this.legArmor = item; }
    public void equipFeet(Equipment item) { this.footwear = item; }
    public void equipMainHand(Equipment item) { this.mainHand = item; }
    public void equipOffHand(Equipment item) { this.offHand = item; }
    public void equipCape(Equipment item) { this.cape = item; }
    
    public Equipment getHeadGear() { return headGear; }
    public Equipment getBodyArmor() { return bodyArmor; }
    public Equipment getLegArmor() { return legArmor; }
    public Equipment getFootwear() { return footwear; }
    public Equipment getMainHand() { return mainHand; }
    public Equipment getOffHand() { return offHand; }
    public Equipment getCape() { return cape; }
    
    /**
     * Gets equipment for a specific slot.
     */
    public Equipment getEquipment(Equipment.Slot slot) {
        switch (slot) {
            case HEAD: return headGear;
            case BODY: return bodyArmor;
            case LEGS: return legArmor;
            case FEET: return footwear;
            case MAIN_HAND: return mainHand;
            case OFF_HAND: return offHand;
            case CAPE: return cape;
            default: return null;
        }
    }
    
    // === Getters/Setters ===
    
    public double getGridX() { return gridX; }
    public double getGridY() { return gridY; }
    public double getTargetGridX() { return targetGridX; }
    public double getTargetGridY() { return targetGridY; }
    public boolean isMoving() { return isMoving; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public void addGold(int amount) { this.gold += amount; }
    public Currency getCurrency() { return currency; }
    public Inventory getInventory() { return inventory; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public void setHealth(int health) { this.health = Math.min(maxHealth, Math.max(0, health)); }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
}
