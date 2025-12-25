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
    private boolean movementLocked = false;  // Lock movement during actions
    
    // Movement speed (grid cells per second)
    private static final double MOVE_SPEED = 8.0;
    private static final double SHALLOW_WATER_SPEED_MULT = 0.7;  // 30% speed reduction in shallow water
    private static final double DEEP_WATER_SPEED_MULT = 0.3;     // 70% speed reduction in deep water
    private static final double SWIMMING_STAMINA_COST = 0.5;     // Stamina cost per second when swimming
    
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
    
    // Player's party
    private Party party;
    
    // Character stats (primary attributes and derived stats)
    private CharacterStats characterStats;
    
    // Animation state
    private double animationTime = 0;
    private boolean facingRight = true;
    
    // Swimming state
    private boolean isSwimming = false;
    private boolean isInShallowWater = false;
    private TerrainType currentTerrain = null;
    
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
        
        // Initialize party (starts empty)
        this.party = new Party();
        
        // Initialize character stats with default values (average human stats)
        this.characterStats = new CharacterStats();
        
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
     * @param deltaTime Time since last update in seconds
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
            
            // Apply water movement penalties
            if (isSwimming) {
                moveAmount *= DEEP_WATER_SPEED_MULT;
            } else if (isInShallowWater) {
                moveAmount *= SHALLOW_WATER_SPEED_MULT;
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
     * Updates the terrain type the player is currently on.
     * This affects movement speed and animation.
     */
    public void setCurrentTerrain(TerrainType terrain) {
        this.currentTerrain = terrain;
        if (terrain != null && terrain.isWater()) {
            if (terrain.isDeepWater()) {
                isSwimming = true;
                isInShallowWater = false;
            } else {
                isSwimming = false;
                isInShallowWater = true;
            }
        } else {
            isSwimming = false;
            isInShallowWater = false;
        }
    }
    
    /**
     * Gets the stamina cost per second for current movement.
     * Walking on land costs no stamina, shallow water costs none,
     * swimming in deep water costs stamina.
     */
    public double getMovementStaminaCost() {
        if (isSwimming && isMoving) {
            return SWIMMING_STAMINA_COST;
        }
        return 0.0;
    }
    
    /**
     * Returns true if player is currently swimming.
     */
    public boolean isSwimming() {
        return isSwimming;
    }
    
    /**
     * Returns true if player is in shallow water.
     */
    public boolean isInShallowWater() {
        return isInShallowWater;
    }
    
    /**
     * Sets the movement target for the player.
     * Movement is blocked if movementLocked is true.
     */
    public void moveTo(double targetX, double targetY) {
        if (movementLocked) return;  // Don't allow movement during actions
        
        this.targetGridX = targetX;
        this.targetGridY = targetY;
        this.isMoving = true;
        
        double dx = targetX - gridX;
        if (Math.abs(dx) > 0.1) {
            facingRight = dx > 0;
        }
    }
    
    /**
     * Locks or unlocks player movement.
     * Used during gathering and other actions.
     */
    public void setMovementLocked(boolean locked) {
        this.movementLocked = locked;
        if (locked) {
            // Stop any current movement
            this.targetGridX = this.gridX;
            this.targetGridY = this.gridY;
            this.isMoving = false;
        }
    }
    
    /**
     * Checks if movement is currently locked.
     */
    public boolean isMovementLocked() {
        return movementLocked;
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
     * Renders a clean player sprite with connected body parts.
     */
    public void render(GraphicsContext gc, double screenX, double screenY, double size) {
        double centerX = screenX + size / 2;
        double bottomY = screenY + size * 0.95; // Slight offset from bottom
        
        // Swimming vs walking animation
        double bobOffset = 0;
        double legSwing = 0;
        double armSwing = 0;
        double swimOffset = 0;
        
        if (isSwimming && isMoving) {
            // Swimming animation - horizontal stroke motion
            bobOffset = Math.sin(animationTime * 6) * size * 0.02;
            armSwing = Math.sin(animationTime * 6) * 30; // Wide arm strokes
            swimOffset = -size * 0.3; // Sink lower in water
        } else if (isInShallowWater && isMoving) {
            // Wading animation - slower walk
            bobOffset = Math.sin(animationTime * 6) * size * 0.01;
            legSwing = Math.sin(animationTime * 5) * 6; // Smaller leg swing
            armSwing = Math.sin(animationTime * 5 + Math.PI) * 4;
        } else if (isMoving) {
            // Normal walking animation
            bobOffset = Math.sin(animationTime * 10) * size * 0.015;
            legSwing = Math.sin(animationTime * 8) * 10;
            armSwing = Math.sin(animationTime * 8 + Math.PI) * 8;
        }
        
        double scaleX = facingRight ? 1 : -1;
        
        gc.save();
        gc.translate(centerX, bottomY + bobOffset + swimOffset);
        gc.scale(scaleX, 1);
        
        // Rotate body slightly when swimming
        if (isSwimming) {
            gc.rotate(15); // Tilt forward
        }
        
        // Unit-based proportions for a chibi-style character
        double unit = size / 12.0;
        
        // Body dimensions
        double headR = unit * 2.2;       // Head radius
        double bodyW = unit * 3.0;       // Body width  
        double bodyH = unit * 2.8;       // Body height
        double legW = unit * 1.1;        // Leg width
        double legH = unit * 2.5;        // Leg height
        double armW = unit * 0.9;        // Arm width
        double armH = unit * 2.2;        // Arm height
        
        // Y positions (from feet up)
        double legsTop = -legH;
        double bodyBottom = legsTop + unit * 0.3; // Overlap with legs
        double bodyTop = bodyBottom - bodyH;
        double headCenter = bodyTop - headR * 0.6; // Head overlaps body slightly
        
        // Shadow (don't show in water)
        if (!isSwimming && !isInShallowWater) {
            gc.setFill(Color.color(0, 0, 0, 0.25));
            gc.fillOval(-unit * 1.5, -unit * 0.3, unit * 3, unit * 0.8);
        }
        
        // === CAPE (back layer) ===
        if (cape != null && !isSwimming) {
            cape.renderCape(gc, -bodyW * 0.55, bodyTop, bodyW * 1.1, bodyH + legH * 0.5);
        }
        
        // === LEFT ARM (behind body) ===
        gc.save();
        gc.translate(-bodyW / 2 - armW * 0.2, bodyTop + unit * 0.3);
        if (isMoving) gc.rotate(armSwing);
        renderArm(gc, bodyParts.get(BodyPart.Type.LEFT_ARM), armW, armH, false);
        gc.restore();
        
        // === LEGS === (hidden when swimming)
        if (!isSwimming) {
            double legGap = unit * 0.3;
            
            // Left leg
            gc.save();
            gc.translate(-legGap, legsTop);
            if (isMoving) gc.rotate(legSwing);
            renderLeg(gc, bodyParts.get(BodyPart.Type.LEFT_LEG), legW, legH);
            gc.restore();
            
            // Right leg
            gc.save();
            gc.translate(legGap, legsTop);
            if (isMoving) gc.rotate(-legSwing);
            renderLeg(gc, bodyParts.get(BodyPart.Type.RIGHT_LEG), legW, legH);
            gc.restore();
        }
        
        // === BODY ===
        renderTorso(gc, bodyParts.get(BodyPart.Type.CHEST), -bodyW / 2, bodyTop, bodyW, bodyH);
        
        // === RIGHT ARM (in front) ===
        gc.save();
        gc.translate(bodyW / 2 - armW * 0.3, bodyTop + unit * 0.3);
        if (isMoving) gc.rotate(-armSwing);
        renderArm(gc, bodyParts.get(BodyPart.Type.RIGHT_ARM), armW, armH, true);
        gc.restore();
        
        // === HEAD ===
        renderHead(gc, bodyParts.get(BodyPart.Type.HEAD), headCenter, headR);
        
        // Draw water ripples when in water
        if (isSwimming || isInShallowWater) {
            gc.setFill(Color.color(0.3, 0.5, 0.8, 0.3));
            double rippleSize = unit * 4;
            gc.fillOval(-rippleSize, -unit * 0.5, rippleSize * 2, unit);
        }
        
        gc.restore();
        
        // No name rendering - removed as requested
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
        
        // Eyes - two eyes
        gc.setFill(Color.WHITE);
        double eyeSize = radius * 0.28;
        double eyeY = centerY - radius * 0.1;
        // Left eye
        gc.fillOval(-radius * 0.35, eyeY - eyeSize/2, eyeSize, eyeSize);
        // Right eye
        gc.fillOval(radius * 0.1, eyeY - eyeSize/2, eyeSize, eyeSize);
        
        // Pupils
        gc.setFill(Color.web("#1a1a1a"));
        double pupilSize = eyeSize * 0.5;
        gc.fillOval(-radius * 0.28, eyeY - pupilSize/2, pupilSize, pupilSize);
        gc.fillOval(radius * 0.17, eyeY - pupilSize/2, pupilSize, pupilSize);
        
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
    public Party getParty() { return party; }
    public CharacterStats getCharacterStats() { return characterStats; }
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
