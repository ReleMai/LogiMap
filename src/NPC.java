import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Base class for NPCs that can walk around towns and cities.
 * Provides basic movement, rendering, and interaction capabilities.
 */
public class NPC {
    
    // NPC Types
    public enum NPCType {
        VILLAGER("Villager", "#8B7355", "#5C4033"),      // Brown clothes
        MERCHANT("Merchant", "#DAA520", "#8B6914"),      // Gold/merchant clothes
        GUARD("Guard", "#708090", "#2F4F4F"),            // Armored
        NOBLE("Noble", "#800020", "#4A0010"),            // Rich burgundy
        PEASANT("Peasant", "#9C8B6E", "#6B5B3E"),        // Simple clothes
        BLACKSMITH("Blacksmith", "#36454F", "#1C1C1C"),  // Sooty dark
        INNKEEPER("Innkeeper", "#CD853F", "#8B4513"),    // Warm brown apron
        BARD("Bard", "#9370DB", "#6A5ACD");              // Colorful purple
        
        private final String name;
        private final String primaryColor;
        private final String secondaryColor;
        
        NPCType(String name, String primary, String secondary) {
            this.name = name;
            this.primaryColor = primary;
            this.secondaryColor = secondary;
        }
        
        public String getName() { return name; }
        public String getPrimaryColor() { return primaryColor; }
        public String getSecondaryColor() { return secondaryColor; }
    }
    
    // Identity
    private String name;
    private NPCType type;
    private boolean isMale;
    
    // Position (in world coordinates)
    private double worldX;
    private double worldY;
    
    // Movement
    private double targetX;
    private double targetY;
    private double speed = 1.2; // Tiles per second (faster movement)
    private boolean isMoving = false;
    private double idleTimer = 0;
    private double idleDelay = 0; // Time to wait before moving again
    private double actionTimer = 0; // For performing actions
    private NPCAction currentAction = NPCAction.IDLE;
    
    public enum NPCAction {
        IDLE, WALKING, WORKING, TALKING, SHOPPING
    }
    
    // Animation
    private int animFrame = 0;
    private double animTimer = 0;
    private Direction facing = Direction.SOUTH;
    
    // Town reference
    private Town homeTown;
    private double wanderRadius = 2.0; // How far from town center to wander
    private Town travelTarget; // Next town to travel toward
    private boolean travelingBetweenTowns = false;
    private double travelLingerTimer = 0;
    private double travelLingerDuration = 0;
    
    // Interaction
    private boolean canInteract = true;
    private String[] dialogueLines;
    
    // Random for behavior
    private Random random = new Random();
    
    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }
    
    /**
     * Creates an NPC at a specific location in a town.
     */
    public NPC(String name, NPCType type, Town town, double worldX, double worldY) {
        this.name = name;
        this.type = type;
        this.homeTown = town;
        this.worldX = worldX;
        this.worldY = worldY;
        this.targetX = worldX;
        this.targetY = worldY;
        this.isMale = random.nextBoolean();
        
        // Set dialogue based on type
        setupDialogue();
        
        // Random initial idle delay
        idleDelay = 2 + random.nextDouble() * 4;
    }
    
    /**
     * Creates a random NPC of the given type at a town.
     */
    public static NPC createRandom(NPCType type, Town town) {
        String name = NameGenerator.generateName(type == NPCType.NOBLE);
        
        // Position near town center with some randomness
        double offsetX = (Math.random() - 0.5) * 2;
        double offsetY = (Math.random() - 0.5) * 2;
        
        return new NPC(name, type, town, 
            town.getGridX() + offsetX, 
            town.getGridY() + offsetY);
    }
    
    /**
     * Sets up dialogue lines based on NPC type.
     */
    private void setupDialogue() {
        dialogueLines = switch (type) {
            case VILLAGER -> new String[] {
                "Good day, traveler.",
                "The weather has been fair lately.",
                "Have you visited the marketplace?",
                "Stay safe out there."
            };
            case MERCHANT -> new String[] {
                "Looking to trade?",
                "I have the finest wares!",
                "Quality goods at fair prices.",
                "Come back anytime!"
            };
            case GUARD -> new String[] {
                "Move along.",
                "No trouble here.",
                "Stay out of trouble.",
                "The roads aren't safe at night."
            };
            case NOBLE -> new String[] {
                "Hmm, do I know you?",
                "I have important matters to attend to.",
                "This town prospers under my guidance.",
                "Peasants... always in the way."
            };
            case PEASANT -> new String[] {
                "Hard work never ends...",
                "Another day, another copper.",
                "Bless you, kind stranger.",
                "The harvest was good this year."
            };
            case BLACKSMITH -> new String[] {
                "Need something forged?",
                "Steel and iron, that's my trade.",
                "A good blade takes time.",
                "Come back when you need repairs."
            };
            case INNKEEPER -> new String[] {
                "Welcome! Need a room?",
                "The stew is fresh today!",
                "Rest your weary bones here.",
                "We have the best ale in town!"
            };
            case BARD -> new String[] {
                "♪ Tra la la... ♪",
                "Shall I sing you a tale?",
                "I know songs from distant lands!",
                "A coin for a song, good traveler?"
            };
        };
    }
    
    /**
     * Updates NPC state (movement, animation).
     */
    public void update(double deltaTime) {
        // Update animation - faster when moving
        animTimer += deltaTime * (isMoving ? 2.0 : 1.0);
        if (animTimer >= 0.2) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 4; // More animation frames
        }
        
        // Update action timer
        actionTimer += deltaTime;

        // Queue movement toward travel target if set
        if (travelTarget != null && !isMoving) {
            double destX = travelTarget.getGridX() + travelTarget.getSize() / 2.0;
            double destY = travelTarget.getGridY() + travelTarget.getSize() / 2.0;
            targetX = destX;
            targetY = destY;
            isMoving = true;
            travelingBetweenTowns = true;
            currentAction = NPCAction.WALKING;
        }
        
        if (isMoving) {
            // Move towards target
            double dx = targetX - worldX;
            double dy = targetY - worldY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist < 0.05) {
                // Reached target
                worldX = targetX;
                worldY = targetY;
                isMoving = false;
                idleTimer = 0;
                idleDelay = 0.5 + random.nextDouble() * 2; // Shorter wait times for more activity
                currentAction = NPCAction.IDLE;

                // If traveling, swap home town to destination and linger briefly
                if (travelTarget != null) {
                    homeTown = travelTarget;
                    travelTarget = null;
                    travelingBetweenTowns = false;
                    travelLingerDuration = 2 + random.nextDouble() * 3;
                    travelLingerTimer = 0;
                    // Perform action at destination
                    currentAction = random.nextBoolean() ? NPCAction.SHOPPING : NPCAction.WORKING;
                }
            } else {
                // Move towards target
                double moveAmount = speed * deltaTime;
                worldX += (dx / dist) * moveAmount;
                worldY += (dy / dist) * moveAmount;
                
                // Update facing direction
                if (Math.abs(dx) > Math.abs(dy)) {
                    facing = dx > 0 ? Direction.EAST : Direction.WEST;
                } else {
                    facing = dy > 0 ? Direction.SOUTH : Direction.NORTH;
                }
            }
        } else {
            // Idle - wait then choose new target
            if (travelLingerDuration > 0) {
                travelLingerTimer += deltaTime;
                if (travelLingerTimer < travelLingerDuration) {
                    return;
                }
                travelLingerDuration = 0;
            }
            idleTimer += deltaTime;
            if (idleTimer >= idleDelay && travelTarget == null) {
                chooseNewTarget();
            }
        }
    }
    
    /**
     * Gets the NPC's current action.
     */
    public NPCAction getCurrentAction() {
        return currentAction;
    }
    
    /**
     * Chooses a new random target within wander radius.
     */
    private void chooseNewTarget() {
        if (homeTown == null) return;
        
        // Random position near town center
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = random.nextDouble() * wanderRadius;
        
        targetX = homeTown.getGridX() + Math.cos(angle) * radius;
        targetY = homeTown.getGridY() + Math.sin(angle) * radius;
        
        isMoving = true;
    }
    
    /**
     * Renders the NPC on the map.
     */
    public void render(GraphicsContext gc, double screenX, double screenY, double size) {
        Color primary = Color.web(type.getPrimaryColor());
        Color secondary = Color.web(type.getSecondaryColor());
        
        // Body
        double bodyWidth = size * 0.5;
        double bodyHeight = size * 0.6;
        double bodyX = screenX + (size - bodyWidth) / 2;
        double bodyY = screenY + size * 0.3;
        
        gc.setFill(primary);
        gc.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 4, 4);
        
        // Head
        double headSize = size * 0.35;
        double headX = screenX + (size - headSize) / 2;
        double headY = screenY + size * 0.05;
        
        // Skin color
        gc.setFill(Color.web("#FDBCB4")); // Peach skin
        gc.fillOval(headX, headY, headSize, headSize);
        
        // Hair
        gc.setFill(getHairColor());
        gc.fillArc(headX, headY, headSize, headSize * 0.6, 0, 180, javafx.scene.shape.ArcType.ROUND);
        
        // Eyes (based on facing)
        gc.setFill(Color.BLACK);
        double eyeSize = size * 0.06;
        double eyeY = headY + headSize * 0.45;
        
        if (facing == Direction.NORTH) {
            // Looking away - no eyes visible
        } else {
            double eyeOffset = facing == Direction.EAST ? size * 0.05 : 
                               facing == Direction.WEST ? -size * 0.05 : 0;
            gc.fillOval(headX + headSize * 0.3 + eyeOffset, eyeY, eyeSize, eyeSize);
            gc.fillOval(headX + headSize * 0.55 + eyeOffset, eyeY, eyeSize, eyeSize);
        }
        
        // Animation - walking bounce
        if (isMoving && animFrame == 1) {
            // Slight vertical offset for walking animation
            gc.save();
            gc.translate(0, -size * 0.05);
            gc.restore();
        }
        
        // Legs
        gc.setFill(secondary);
        double legWidth = size * 0.15;
        double legHeight = size * 0.2;
        double legY = bodyY + bodyHeight - legHeight * 0.5;
        double leftLegX = bodyX + bodyWidth * 0.2;
        double rightLegX = bodyX + bodyWidth * 0.6;
        
        // Animate legs
        double legOffset = isMoving && animFrame == 1 ? size * 0.03 : 0;
        gc.fillRoundRect(leftLegX - legOffset, legY, legWidth, legHeight, 2, 2);
        gc.fillRoundRect(rightLegX + legOffset, legY, legWidth, legHeight, 2, 2);
        
        // Type-specific accessories
        renderAccessories(gc, screenX, screenY, size);
    }
    
    /**
     * Gets a random hair color.
     */
    private Color getHairColor() {
        // Use name hash for consistent color
        int hash = name.hashCode();
        return switch (Math.abs(hash) % 5) {
            case 0 -> Color.web("#2C1810"); // Dark brown
            case 1 -> Color.web("#4A3728"); // Brown
            case 2 -> Color.web("#1C1C1C"); // Black
            case 3 -> Color.web("#8B4513"); // Auburn
            default -> Color.web("#FFD700"); // Blonde
        };
    }
    
    /**
     * Renders type-specific accessories.
     */
    private void renderAccessories(GraphicsContext gc, double screenX, double screenY, double size) {
        switch (type) {
            case GUARD -> {
                // Helmet
                gc.setFill(Color.web("#708090"));
                double helmX = screenX + size * 0.3;
                double helmY = screenY + size * 0.02;
                gc.fillRoundRect(helmX, helmY, size * 0.4, size * 0.15, 3, 3);
                
                // Sword
                gc.setFill(Color.web("#C0C0C0"));
                gc.fillRect(screenX + size * 0.8, screenY + size * 0.4, size * 0.08, size * 0.4);
            }
            case MERCHANT -> {
                // Hat/cap
                gc.setFill(Color.web("#8B0000"));
                gc.fillOval(screenX + size * 0.25, screenY, size * 0.5, size * 0.15);
            }
            case NOBLE -> {
                // Crown/circlet
                gc.setFill(Color.web("#FFD700"));
                gc.fillRect(screenX + size * 0.35, screenY + size * 0.02, size * 0.3, size * 0.08);
            }
            case BLACKSMITH -> {
                // Apron
                gc.setFill(Color.web("#8B4513"));
                gc.fillRect(screenX + size * 0.35, screenY + size * 0.35, size * 0.3, size * 0.4);
                
                // Hammer
                gc.setFill(Color.web("#4A4A4A"));
                gc.fillRect(screenX + size * 0.85, screenY + size * 0.45, size * 0.12, size * 0.25);
            }
            case BARD -> {
                // Lute
                gc.setFill(Color.web("#DEB887"));
                gc.fillOval(screenX - size * 0.1, screenY + size * 0.4, size * 0.25, size * 0.3);
            }
            default -> {
                // No special accessories
            }
        }
    }
    
    /**
     * Gets a random dialogue line.
     */
    public String getDialogue() {
        if (dialogueLines == null || dialogueLines.length == 0) {
            return "...";
        }
        return dialogueLines[random.nextInt(dialogueLines.length)];
    }
    
    // Getters and setters
    public String getName() { return name; }
    public NPCType getType() { return type; }
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public boolean isMale() { return isMale; }
    public Town getHomeTown() { return homeTown; }
    public boolean canInteract() { return canInteract; }
    public void setCanInteract(boolean can) { this.canInteract = can; }
    
    public void setPosition(double x, double y) {
        this.worldX = x;
        this.worldY = y;
        this.targetX = x;
        this.targetY = y;
    }
    
    public void setWanderRadius(double radius) {
        this.wanderRadius = radius;
    }

    public void setTravelTarget(Town target) {
        if (target == null || target == homeTown) return;
        this.travelTarget = target;
        this.travelLingerDuration = 0;
        this.travelLingerTimer = 0;
    }

    public boolean isTravelingBetweenTowns() {
        return travelingBetweenTowns || travelTarget != null;
    }

    public Town getTravelTarget() {
        return travelTarget;
    }
    
    /**
     * Checks if this NPC is near the given world coordinates.
     */
    public boolean isNear(double x, double y, double radius) {
        double dx = worldX - x;
        double dy = worldY - y;
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }
}
