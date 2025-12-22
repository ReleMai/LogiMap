import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Player character sprite with modular body parts system.
 * Each body part can take damage and have gear equipped.
 * 
 * Body Parts:
 * - Head: Takes damage, wears headgear
 * - Chest: Main body, wears armor
 * - RArm (Right Arm): Holds main hand weapon
 * - LArm (Left Arm): Holds off-hand/shield
 * - RLeg (Right Leg): Lower body armor
 * - LLeg (Left Leg): Footwear
 */
public class PlayerSprite {
    
    // Position in grid coordinates
    private double gridX;
    private double gridY;
    
    // Target position for movement
    private double targetGridX;
    private double targetGridY;
    private boolean isMoving = false;
    
    // Movement speed (grid cells per second)
    private static final double MOVE_SPEED = 8.0;
    
    // Modular body parts
    private Map<BodyPart.Type, BodyPart> bodyParts;
    
    // Equipment slots (kept for compatibility)
    private Equipment headGear;
    private Equipment bodyArmor;
    private Equipment legArmor;
    private Equipment footwear;
    private Equipment mainHand;
    private Equipment offHand;
    private Equipment cape;
    
    // Player stats
    private String name;
    private int gold = 100;
    
    // Currency system
    private Currency currency;
    
    // Player inventory
    private Inventory inventory;
    
    // Animation state
    private double animationTime = 0;
    private boolean facingRight = true;
    
    // Visual customization
    private Color hairColor = Color.web("#4a3728");
    private int hairStyle = 0;
    
    // Constants
    private static final Color OUTLINE_COLOR = Color.web("#2a1a10");
    
    /**
     * Creates a new player sprite at the given position.
     * Player starts in basic white underwear - clothing must be equipped.
     */
    public PlayerSprite(String name, int startX, int startY) {
        this.name = name;
        this.gridX = startX;
        this.gridY = startY;
        this.targetGridX = startX;
        this.targetGridY = startY;
        
        // Initialize body parts
        initializeBodyParts();
        
        // Player starts with NO clothes equipped (white underwear visible)
        this.bodyArmor = null;
        this.legArmor = null;
        
        // Initialize currency (start with 50 copper)
        this.currency = new Currency(0, 0, 50);
        
        // Initialize inventory (4 rows x 6 columns = 24 slots)
        this.inventory = new Inventory("Backpack", 4, 6);
        
        // Give player starting items (ragged clothes to equip)
        this.inventory.addItem(ItemRegistry.createStack("ragged_shirt", 1));
        this.inventory.addItem(ItemRegistry.createStack("ragged_pants", 1));
        // Some starting supplies
        this.inventory.addItem(ItemRegistry.createStack("bread", 3));
    }
    
    /**
     * Initializes all body parts.
     */
    private void initializeBodyParts() {
        bodyParts = new HashMap<>();
        for (BodyPart.Type type : BodyPart.Type.values()) {
            bodyParts.put(type, new BodyPart(type));
        }
    }
    
    /**
     * Gets a specific body part.
     */
    public BodyPart getBodyPart(BodyPart.Type type) {
        return bodyParts.get(type);
    }
    
    /**
     * Gets total health across all body parts.
     */
    public int getHealth() {
        int total = 0;
        for (BodyPart part : bodyParts.values()) {
            total += part.getCurrentHealth();
        }
        return total;
    }
    
    /**
     * Gets max health across all body parts.
     */
    public int getMaxHealth() {
        int total = 0;
        for (BodyPart part : bodyParts.values()) {
            total += part.getMaxHealth();
        }
        return total;
    }
    
    /**
     * Sets health - distributes across body parts proportionally.
     */
    public void setHealth(int health) {
        double ratio = health / (double) getMaxHealth();
        for (BodyPart part : bodyParts.values()) {
            int partHealth = (int) (part.getMaxHealth() * ratio);
            while (part.getCurrentHealth() > partHealth) {
                part.takeDamage(1);
            }
            while (part.getCurrentHealth() < partHealth) {
                part.heal(1);
            }
        }
    }
    
    /**
     * Takes damage to a specific body part.
     */
    public int takeDamage(BodyPart.Type partType, int damage) {
        BodyPart part = bodyParts.get(partType);
        if (part != null) {
            return part.takeDamage(damage);
        }
        return 0;
    }
    
    /**
     * Takes damage to a random body part.
     */
    public int takeDamageRandom(int damage) {
        BodyPart.Type[] types = BodyPart.Type.values();
        BodyPart.Type target = types[(int)(Math.random() * types.length)];
        return takeDamage(target, damage);
    }
    
    /**
     * Heals a specific body part.
     */
    public void heal(BodyPart.Type partType, int amount) {
        BodyPart part = bodyParts.get(partType);
        if (part != null) {
            part.heal(amount);
        }
    }
    
    /**
     * Heals all body parts.
     */
    public void healAll(int amount) {
        for (BodyPart part : bodyParts.values()) {
            part.heal(amount);
        }
    }
    
    /**
     * Updates player position and body parts.
     */
    public void update(double deltaTime) {
        // Update body parts
        for (BodyPart part : bodyParts.values()) {
            part.update(deltaTime);
        }
        
        if (!isMoving) return;
        
        double dx = targetGridX - gridX;
        double dy = targetGridY - gridY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 0.1) {
            gridX = targetGridX;
            gridY = targetGridY;
            isMoving = false;
        } else {
            double moveAmount = MOVE_SPEED * deltaTime;
            
            // Apply movement penalty if legs are damaged
            BodyPart rightLeg = bodyParts.get(BodyPart.Type.RIGHT_LEG);
            BodyPart leftLeg = bodyParts.get(BodyPart.Type.LEFT_LEG);
            if (rightLeg.isCrippled() || leftLeg.isCrippled()) {
                moveAmount *= 0.5; // 50% speed when leg is crippled
            }
            
            if (moveAmount >= distance) {
                gridX = targetGridX;
                gridY = targetGridY;
                isMoving = false;
            } else {
                double ratio = moveAmount / distance;
                gridX += dx * ratio;
                gridY += dy * ratio;
                
                if (Math.abs(dx) > 0.1) {
                    facingRight = dx > 0;
                }
            }
        }
        
        animationTime += deltaTime;
    }
    
    /**
     * Sets the movement target for the player.
     */
    public void moveTo(double targetX, double targetY) {
        this.targetGridX = targetX;
        this.targetGridY = targetY;
        this.isMoving = true;
        
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
     * Renders a cleaner player sprite with modular body parts.
     * All body parts are tightly connected with no gaps.
     */
    public void render(GraphicsContext gc, double screenX, double screenY, double size) {
        double centerX = screenX + size / 2;
        double bottomY = screenY + size;
        
        // Walking animation
        double bobOffset = 0;
        double legSwing = 0;
        double armSwing = 0;
        if (isMoving) {
            bobOffset = Math.sin(animationTime * 10) * size * 0.02;
            legSwing = Math.sin(animationTime * 8) * 12;
            armSwing = Math.sin(animationTime * 8 + Math.PI) * 8;
        }
        
        double scaleX = facingRight ? 1 : -1;
        
        gc.save();
        gc.translate(centerX, bottomY + bobOffset);
        gc.scale(scaleX, 1);
        
        // Proportional sizing - all based on size for proper scaling
        double headSize = size * 0.35;      // Head diameter
        double torsoW = size * 0.32;        // Torso width
        double torsoH = size * 0.28;        // Torso height
        double legW = size * 0.12;          // Leg width
        double legH = size * 0.30;          // Leg height
        double armW = size * 0.10;          // Arm width
        double armH = size * 0.28;          // Arm height
        
        // Calculate Y positions from bottom up (no gaps!)
        double footY = 0;                            // Ground level
        double legTopY = -legH;                      // Top of legs
        double torsoBottomY = legTopY;               // Torso connects to legs
        double torsoTopY = torsoBottomY - torsoH;    // Top of torso
        double neckY = torsoTopY;                    // Neck connects to torso
        double headCenterY = neckY - headSize * 0.35; // Head center (overlaps slightly)
        
        // Shadow
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(-size * 0.15, -size * 0.03, size * 0.30, size * 0.08);
        
        // === BACK LAYER ===
        
        // Cape (behind everything)
        if (cape != null) {
            cape.renderCape(gc, -torsoW * 0.6, torsoTopY, torsoW * 1.2, torsoH + legH * 0.7);
        }
        
        // Left arm (behind body)
        gc.save();
        double leftArmX = -torsoW / 2 - armW * 0.3;
        gc.translate(leftArmX, torsoTopY + armH * 0.1);
        if (isMoving) gc.rotate(armSwing);
        renderArm(gc, bodyParts.get(BodyPart.Type.LEFT_ARM), armW, armH, false);
        gc.restore();
        
        // === LEGS ===
        double legSpacing = legW * 0.7;
        
        // Left leg
        gc.save();
        gc.translate(-legSpacing, legTopY);
        if (isMoving) gc.rotate(legSwing);
        renderLeg(gc, bodyParts.get(BodyPart.Type.LEFT_LEG), legW, legH);
        gc.restore();
        
        // Right leg
        gc.save();
        gc.translate(legSpacing, legTopY);
        if (isMoving) gc.rotate(-legSwing);
        renderLeg(gc, bodyParts.get(BodyPart.Type.RIGHT_LEG), legW, legH);
        gc.restore();
        
        // === TORSO ===
        renderTorso(gc, bodyParts.get(BodyPart.Type.CHEST), 
                   -torsoW / 2, torsoTopY, torsoW, torsoH);
        
        // === FRONT LAYER ===
        
        // Right arm (in front)
        gc.save();
        double rightArmX = torsoW / 2 - armW * 0.7;
        gc.translate(rightArmX, torsoTopY + armH * 0.1);
        if (isMoving) gc.rotate(-armSwing);
        renderArm(gc, bodyParts.get(BodyPart.Type.RIGHT_ARM), armW, armH, true);
        gc.restore();
        
        // === HEAD ===
        renderHead(gc, bodyParts.get(BodyPart.Type.HEAD), headCenterY, headSize / 2);
        
        gc.restore();
        
        // Name above sprite
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 
                   Math.max(10, size * 0.13)));
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(name);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getLayoutBounds().getWidth();
        gc.fillText(name, screenX + size / 2 - textWidth / 2, screenY - size * 0.05);
    }
    
    /**
     * Renders head with hair and face
     */
    private void renderHead(GraphicsContext gc, BodyPart head, double centerY, double radius) {
        Color skinColor = head.getCurrentSkinColor();
        
        // Head circle
        gc.setFill(skinColor);
        gc.fillOval(-radius, centerY - radius, radius * 2, radius * 2);
        
        // Hair (covers top half)
        gc.setFill(hairColor);
        gc.fillArc(-radius * 1.02, centerY - radius * 1.05, radius * 2.04, radius * 1.5, 
                   20, 140, javafx.scene.shape.ArcType.CHORD);
        
        // Eye
        gc.setFill(Color.web("#1a1a1a"));
        double eyeSize = radius * 0.22;
        gc.fillOval(radius * 0.2, centerY - radius * 0.15, eyeSize, eyeSize);
        
        // Outline
        gc.setStroke(OUTLINE_COLOR);
        gc.setLineWidth(1.5);
        gc.strokeOval(-radius, centerY - radius, radius * 2, radius * 2);
        
        // Headgear
        if (headGear != null) {
            headGear.renderOnHead(gc, -radius, centerY - radius, radius * 2, radius * 2);
        }
        
        // Highlight if selected
        if (head.isHighlighted()) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(-radius - 2, centerY - radius - 2, radius * 2 + 4, radius * 2 + 4);
        }
    }
    
    /**
     * Renders torso (body armor area)
     */
    private void renderTorso(GraphicsContext gc, BodyPart chest, 
                             double x, double y, double w, double h) {
        Color skinColor = chest.getCurrentSkinColor();
        
        // Base torso
        gc.setFill(skinColor);
        gc.fillRoundRect(x, y, w, h, w * 0.25, w * 0.25);
        
        // Outline
        gc.setStroke(OUTLINE_COLOR);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, w, h, w * 0.25, w * 0.25);
        
        // Body armor overlay
        if (bodyArmor != null) {
            bodyArmor.renderOnBody(gc, x, y, w, h);
        }
        
        // Highlight
        if (chest.isHighlighted()) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRoundRect(x - 2, y - 2, w + 4, h + 4, w * 0.25, w * 0.25);
        }
    }
    
    /**
     * Renders arm
     */
    private void renderArm(GraphicsContext gc, BodyPart arm, 
                           double width, double length, boolean isRight) {
        Color skinColor = arm.getCurrentSkinColor();
        
        // Arm rectangle
        gc.setFill(skinColor);
        gc.fillRoundRect(-width / 2, 0, width, length, width * 0.6, width * 0.6);
        
        // Outline
        gc.setStroke(OUTLINE_COLOR);
        gc.setLineWidth(1);
        gc.strokeRoundRect(-width / 2, 0, width, length, width * 0.6, width * 0.6);
        
        // Weapons
        if (isRight && mainHand != null) {
            mainHand.renderInHand(gc, 0, length, width * 3.5);
        } else if (!isRight && offHand != null) {
            offHand.renderInHand(gc, 0, length, width * 3.5);
        }
        
        // Highlight
        if (arm.isHighlighted()) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRoundRect(-width / 2 - 1, -1, width + 2, length + 2, width * 0.6, width * 0.6);
        }
    }
    
    /**
     * Renders leg with underwear shorts and footwear
     */
    private void renderLeg(GraphicsContext gc, BodyPart leg, double width, double length) {
        Color skinColor = leg.getCurrentSkinColor();
        
        // Upper portion (underwear shorts)
        double shortsLen = length * 0.35;
        gc.setFill(BodyPart.getUnderwearColor());
        gc.fillRoundRect(-width / 2, 0, width, shortsLen, width * 0.4, width * 0.4);
        
        // Lower leg (skin)
        gc.setFill(skinColor);
        gc.fillRoundRect(-width / 2, shortsLen - width * 0.15, width, length - shortsLen + width * 0.15, 
                         width * 0.4, width * 0.4);
        
        // Outlines
        gc.setStroke(OUTLINE_COLOR);
        gc.setLineWidth(1);
        gc.strokeRoundRect(-width / 2, 0, width, shortsLen, width * 0.4, width * 0.4);
        gc.strokeRoundRect(-width / 2, shortsLen - width * 0.15, width, length - shortsLen + width * 0.15, 
                          width * 0.4, width * 0.4);
        
        // Leg armor overlay
        if (legArmor != null) {
            legArmor.renderOnLegs(gc, -width / 2, 0, width, length);
        }
        
        // Footwear
        if (footwear != null) {
            footwear.renderOnFeet(gc, -width / 2, length - width * 0.35, width, width * 0.5);
        }
        
        // Highlight
        if (leg.isHighlighted()) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRoundRect(-width / 2 - 1, -1, width + 2, length + 2, width * 0.4, width * 0.4);
        }
    }
    
    // === Equipment Management ===
    
    /**
     * Equips an item to the appropriate slot based on its type.
     * Also equips to the body part for stat tracking.
     */
    public void equip(Equipment item) {
        if (item == null) return;
        
        switch (item.getSlot()) {
            case HEAD:
                this.headGear = item;
                bodyParts.get(BodyPart.Type.HEAD).equipGear(item);
                break;
            case BODY:
                this.bodyArmor = item;
                bodyParts.get(BodyPart.Type.CHEST).equipGear(item);
                break;
            case LEGS:
                this.legArmor = item;
                bodyParts.get(BodyPart.Type.RIGHT_LEG).equipGear(item);
                break;
            case FEET:
                this.footwear = item;
                bodyParts.get(BodyPart.Type.LEFT_LEG).equipGear(item);
                break;
            case MAIN_HAND:
                this.mainHand = item;
                bodyParts.get(BodyPart.Type.RIGHT_ARM).equipGear(item);
                break;
            case OFF_HAND:
                this.offHand = item;
                bodyParts.get(BodyPart.Type.LEFT_ARM).equipGear(item);
                break;
            case CAPE:
                this.cape = item;
                break;
        }
    }
    
    /**
     * Unequips an item from the specified slot.
     */
    public Equipment unequip(Equipment.Slot slot) {
        Equipment removed = null;
        switch (slot) {
            case HEAD:
                removed = headGear;
                headGear = null;
                bodyParts.get(BodyPart.Type.HEAD).unequipGear();
                break;
            case BODY:
                removed = bodyArmor;
                bodyArmor = null;
                bodyParts.get(BodyPart.Type.CHEST).unequipGear();
                break;
            case LEGS:
                removed = legArmor;
                legArmor = null;
                bodyParts.get(BodyPart.Type.RIGHT_LEG).unequipGear();
                break;
            case FEET:
                removed = footwear;
                footwear = null;
                bodyParts.get(BodyPart.Type.LEFT_LEG).unequipGear();
                break;
            case MAIN_HAND:
                removed = mainHand;
                mainHand = null;
                bodyParts.get(BodyPart.Type.RIGHT_ARM).unequipGear();
                break;
            case OFF_HAND:
                removed = offHand;
                offHand = null;
                bodyParts.get(BodyPart.Type.LEFT_ARM).unequipGear();
                break;
            case CAPE:
                removed = cape;
                cape = null;
                break;
        }
        return removed;
    }
    
    // === Quick equip methods for compatibility ===
    public void equipHead(Equipment item) { equip(item); }
    public void equipBody(Equipment item) { equip(item); }
    public void equipLegs(Equipment item) { equip(item); }
    public void equipFeet(Equipment item) { equip(item); }
    public void equipMainHand(Equipment item) { equip(item); }
    public void equipOffHand(Equipment item) { equip(item); }
    public void equipCape(Equipment item) { equip(item); }
    
    // === Equipment getters ===
    public Equipment getHeadGear() { return headGear; }
    public Equipment getBodyArmor() { return bodyArmor; }
    public Equipment getLegArmor() { return legArmor; }
    public Equipment getFootwear() { return footwear; }
    public Equipment getMainHand() { return mainHand; }
    public Equipment getOffHand() { return offHand; }
    public Equipment getCape() { return cape; }
    
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
    
    // === Position getters/setters ===
    public double getGridX() { return gridX; }
    public double getGridY() { return gridY; }
    public double getTargetGridX() { return targetGridX; }
    public double getTargetGridY() { return targetGridY; }
    public boolean isMoving() { return isMoving; }
    
    // === Other getters/setters ===
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public void addGold(int amount) { this.gold += amount; }
    public Currency getCurrency() { return currency; }
    public Inventory getInventory() { return inventory; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public Map<BodyPart.Type, BodyPart> getAllBodyParts() { return bodyParts; }
    public Color getHairColor() { return hairColor; }
    public void setHairColor(Color color) { this.hairColor = color; }
    public int getHairStyle() { return hairStyle; }
    public void setHairStyle(int style) { this.hairStyle = style; }
    
    /**
     * Checks if any body part is broken.
     */
    public boolean hasAnyBrokenPart() {
        for (BodyPart part : bodyParts.values()) {
            if (part.isBroken()) return true;
        }
        return false;
    }
    
    /**
     * Checks if any body part is crippled.
     */
    public boolean hasAnyCrippledPart() {
        for (BodyPart part : bodyParts.values()) {
            if (part.isCrippled()) return true;
        }
        return false;
    }
    
    /**
     * Gets total armor from all body parts and equipment.
     */
    public int getTotalArmor() {
        int total = 0;
        for (BodyPart part : bodyParts.values()) {
            total += part.getTotalArmor();
        }
        return total;
    }
}
