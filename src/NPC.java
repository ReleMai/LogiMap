import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
    
    // Role and Schedule
    private NPCRole role = null;
    private ScheduleState scheduleState = ScheduleState.WORKING;
    private boolean isInsideBuilding = false;
    private double homeX, homeY; // Where NPC goes to sleep
    
    public enum ScheduleState {
        AT_HOME,        // Inside building, not visible
        LEAVING_HOME,   // Walking out of building
        WORKING,        // Performing daily duties
        GOING_HOME,     // Walking back to building
        PATROLLING      // For guards - on patrol route
    }
    
    // Patrol system (for guards)
    private boolean isPatrolling = false;
    private double patrolAngleOffset = 0;
    private double patrolRadius = 0;
    private double patrolAngle = 0;
    private double patrolSpeed = 0.5; // Radians per second
    
    // Position (in world coordinates)
    private double worldX;
    private double worldY;
    
    // Movement
    private double targetX;
    private double targetY;
    private double speed = 1.2; // Tiles per second (faster movement)
    private double baseSpeed = 1.2;
    private boolean onRoad = false; // Moving faster on roads
    private RoadNetwork roadNetwork = null; // Reference for road speed calculation
    private boolean isMoving = false;
    private double idleTimer = 0;
    private double idleDelay = 0; // Time to wait before moving again
    private double actionTimer = 0; // For performing actions
    private NPCAction currentAction = NPCAction.IDLE;
    
    public enum NPCAction {
        IDLE, WALKING, WORKING, TALKING, SHOPPING, GATHERING, DELIVERING, PATROLLING, COLLECTING_TAX
    }
    
    // Job system
    private NPCJob currentJob = null;
    private double jobProgress = 0;
    private int carryingResources = 0;
    private String carryingResourceType = null;
    private int carryingGold = 0; // For tax collectors/transporters

    // Manager reference (set by NPCManager) for delivery callbacks
    private NPCManager manager = null;

    public void setManager(NPCManager manager) { this.manager = manager; }
    
    // Animation
    private int animFrame = 0;
    private double animTimer = 0;
    private Direction facing = Direction.SOUTH;
    private double harvestAnimTimer = 0;
    
    // Town reference
    private Town homeTown;
    private double wanderRadius = 4.0; // Default wander radius (will be adjusted based on town size)
    private Town travelTarget; // Next town to travel toward
    private Town assignedDestination; // For transporters/tax collectors - assigned route
    private boolean travelingBetweenTowns = false;
    private double travelLingerTimer = 0;
    private double travelLingerDuration = 0;
    
    // Hiring
    private boolean isHired = false;
    private int hiringCost = 50; // Base cost to hire
    private int dailyWage = 5;   // Daily payment

    // Interaction flag
    private boolean canInteract = true;

    // Dialogue lines for NPC (set in setupDialogue)
    private String[] dialogueLines;

    // Inventory for this NPC (initialized in constructor)
    private Inventory inventory = null;

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
        this.homeX = worldX;
        this.homeY = worldY;
        this.isMale = random.nextBoolean();
        
        // Set dialogue based on type
        setupDialogue();
        
        // Random initial idle delay
        idleDelay = 2 + random.nextDouble() * 4;

        // Initialize a small inventory for carrying gathered resources
        this.inventory = new Inventory("NPC: " + name, 1, 6);

        // Harvest animation timer
        this.harvestAnimTimer = 0;
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
     * Updates NPC state (movement, animation, job).
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
        
        // Update job if active
        if (currentJob != null) {
            updateJob(deltaTime);
            return; // Job takes priority over random wandering
        }

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
                // Apply road speed bonus if on a road
                double moveSpeed = baseSpeed;
                if (roadNetwork != null && roadNetwork.isOnRoad(worldX, worldY)) {
                    moveSpeed *= roadNetwork.getSpeedMultiplierAt(worldX, worldY);
                    onRoad = true;
                } else {
                    onRoad = false;
                }
                
                double moveAmount = moveSpeed * deltaTime;
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
     * Updates NPC while performing a job.
     */
    private void updateJob(double deltaTime) {
        if (currentJob == null) return;
        
        // Update job progress
        currentJob.update(deltaTime);
        // If job just switched to RETURNING and we haven't recorded carried resources yet
        if (currentJob.getState() == NPCJob.JobState.RETURNING && carryingResources == 0) {
            int qty = currentJob.getResourcesGathered();
            String resType = currentJob.getResourceType();
            String itemId = mapResourceTypeToItemId(resType);
            int added = 0;
            if (itemId != null && inventory != null) {
                ItemStack stack = ItemRegistry.createStack(itemId, qty);
                ItemStack rem = inventory.addItem(stack);
                int remQty = rem != null ? rem.getQuantity() : 0;
                added = qty - remQty;
            } else {
                // If we can't create a stack, fall back to simple counter
                added = qty;
            }
            carryingResources = added;
            carryingResourceType = resType;
        }
        currentAction = currentJob.getNPCAction();
        
        // Handle job state
        if (currentJob.requiresMovement()) {
            double[] dest = currentJob.getCurrentDestination();
            if (dest != null) {
                if (!isMoving || Math.abs(targetX - dest[0]) > 0.1 || Math.abs(targetY - dest[1]) > 0.1) {
                    targetX = dest[0];
                    targetY = dest[1];
                    isMoving = true;
                }
                
                // Move towards target
                double dx = targetX - worldX;
                double dy = targetY - worldY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < 0.5) {
                    // Arrived at destination
                    worldX = targetX;
                    worldY = targetY;
                    isMoving = false;
                    currentJob.arrivedAtDestination();
                    
                    // Update carrying status when starting work
                    if (currentJob.getState() == NPCJob.JobState.WORKING) {
                        carryingResources = 0;
                        carryingResourceType = null;
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
            }
        } else if (currentJob.isWorking()) {
            // NPC is stationary, working
            isMoving = false;
            
            // Advance harvest animation timer
            harvestAnimTimer += deltaTime;
            

            // If delivering, we keep carryingResources until delivery handling at completion

        } else {
            // Not working: reset harvest animation
            harvestAnimTimer = 0;
        }
        
        // Check if job is complete
        if (currentJob.isComplete()) {
            // Deliver resources to town economy
            if (carryingResources > 0 && homeTown != null) {
                int deliverQty = carryingResources;
                // Remove items from inventory first if possible
                String itemId = mapResourceTypeToItemId(carryingResourceType);
                if (itemId != null && inventory != null) {
                    int removed = inventory.removeItem(itemId, carryingResources);
                    deliverQty = removed;
                }

                if (deliverQty > 0) {
                    if (manager != null) {
                        Town destTown = currentJob != null ? currentJob.getTargetTown() : null;
                        manager.handleNPCDelivery(this, carryingResourceType, deliverQty, destTown);
                    } else {
                        // Fallback: just drop resources
                        System.out.println("NPC delivered " + deliverQty + " " + carryingResourceType + " but no manager to process delivery.");
                    }
                }

                carryingResources = 0;
                carryingResourceType = null;
            }
            
            currentJob = null;
            currentAction = NPCAction.IDLE;
            idleTimer = 0;
            idleDelay = 1.0 + random.nextDouble() * 3; // Rest after job
        }
    }
    
    /**
     * Gets the NPC's current action.
     */
    public NPCAction getCurrentAction() {
        return currentAction;
    }

    /**
     * Maps a general resource type name to a canonical item ID.
     */
    private String mapResourceTypeToItemId(String resourceType) {
        if (resourceType == null) return null;
        String key = resourceType.toLowerCase();
        switch (key) {
            case "wood": return "timber_oak";
            case "stone": return "stone_common";
            case "ore": return "ore_iron";
            case "grain": case "crops": return "grain_wheat";
            case "fish": return "fish_common";
            case "livestock": return "meat_mutton";
            default: return null;
        }
    }

    /**
     * Renders a simple tool animation for harvesting based on job type.
     */
    private void renderHarvestAnimation(GraphicsContext gc, double cx, double cy, double size, NPCJob.JobType jt) {
        gc.save();
        double phase = Math.sin(harvestAnimTimer * 6) * 20; // degrees
        gc.translate(cx, cy);
        gc.rotate(phase);

        switch (jt) {
            case GATHER_WOOD -> {
                // Draw axe: handle and head
                gc.setFill(Color.web("#8B5A2B"));
                gc.fillRect(-size*0.02, -size*0.25, size*0.04, size*0.4); // handle
                gc.setFill(Color.web("#C0C0C0"));
                gc.fillRect(size*0.02, -size*0.28, size*0.12, size*0.06); // head
            }
            case GATHER_CROPS -> {
                // Draw scythe
                gc.setStroke(Color.web("#8B5A2B"));
                gc.setLineWidth(2);
                gc.strokeLine(-size*0.25, -size*0.15, size*0.25, -size*0.05); // handle
                gc.setStroke(Color.web("#C0C0C0"));
                gc.setLineWidth(3);
                gc.strokeArc(size*0.05, -size*0.2, size*0.4, size*0.3, -90, 120, javafx.scene.shape.ArcType.OPEN);
            }
            case GATHER_ORE, GATHER_STONE -> {
                // Draw pickaxe
                gc.setFill(Color.web("#8B5A2B"));
                gc.fillRect(-size*0.02, -size*0.25, size*0.04, size*0.4); // handle
                gc.setFill(Color.web("#B0B0B0"));
                gc.fillRect(-size*0.18, -size*0.32, size*0.12, size*0.06); // head left
                gc.fillRect(size*0.06, -size*0.18, size*0.12, size*0.06); // head right
            }
            case GATHER_FISH -> {
                // Draw fishing rod
                gc.setStroke(Color.web("#8B5A2B"));
                gc.setLineWidth(2);
                gc.strokeLine(-size*0.25, -size*0.15, size*0.25, -size*0.35);
                gc.setStroke(Color.web("#000000"));
                gc.setLineWidth(1);
                gc.strokeLine(size*0.25, -size*0.35, size*0.3, -size*0.25);
            }
            default -> {
                // Generic tool
                gc.setFill(Color.web("#8B5A2B"));
                gc.fillRect(-size*0.02, -size*0.2, size*0.04, size*0.25);
            }
        }

        gc.restore();
    }
    
    /**
     * Chooses a new random target within wander radius of town CENTER.
     */
    private void chooseNewTarget() {
        if (homeTown == null) return;
        
        // Get town center
        double centerX = homeTown.getGridX() + homeTown.getSize() / 2.0;
        double centerY = homeTown.getGridY() + homeTown.getSize() / 2.0;
        
        // Random position within wander radius of town center
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = 1.0 + random.nextDouble() * wanderRadius; // Min 1 tile, max wanderRadius
        
        targetX = centerX + Math.cos(angle) * radius;
        targetY = centerY + Math.sin(angle) * radius;
        
        isMoving = true;
        currentAction = NPCAction.WALKING;
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

        // --- Harvest/Gather Animation Overlay ---
        if (currentAction == NPCAction.GATHERING && currentJob != null && currentJob.isWorking()) {
            // Try sprite frames first, fall back to vector drawing
            Image[] frames = SpriteManager.getInstance().getHarvestFrames(currentJob.getType());
            if (frames != null && frames.length > 0) {
                int idx = (int)(harvestAnimTimer * 6) % frames.length; // 6 FPS approx
                double frameSize = size * 0.7;
                gc.drawImage(frames[idx], screenX + size/2 - frameSize/2, screenY + size*0.15 - frameSize/2, frameSize, frameSize);
            } else {
                renderHarvestAnimation(gc, screenX + size / 2, screenY + size * 0.45, size, currentJob.getType());
            }
        }

        // --- Inventory indicator ---
        if (inventory != null && !inventory.isEmpty()) {
            int used = inventory.getUsedSlots();
            // Small bag icon
            double ix = screenX + size * 0.1;
            double iy = screenY - size * 0.12;
            gc.setFill(Color.web("#6b4a2b"));
            gc.fillOval(ix, iy, size * 0.22, size * 0.12);
            gc.setFill(Color.web("#d4a574"));
            gc.fillOval(ix + size * 0.05, iy + size * 0.02, size * 0.1, size * 0.06);
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, Math.max(9, (int)(size*0.12))));
            gc.fillText(String.valueOf(used), ix + size * 0.12, iy + size * 0.07);
        }

        // --- Carried resource indicator ---
        if (carryingResources > 0 && carryingResourceType != null) {
            String itemId = mapResourceTypeToItemId(carryingResourceType);
            if (itemId != null) {
                ItemStack stack = ItemRegistry.createStack(itemId, carryingResources);
                if (stack != null && stack.getItem() != null) {
                    double wx = screenX + size * 0.5 - size * 0.12;
                    double wy = screenY - size * 0.32;
                    stack.getItem().renderIcon(gc, wx, wy, size * 0.24);
                    gc.setFill(Color.WHITE);
                    gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, Math.max(10, (int)(size*0.12))));
                    gc.fillText(String.valueOf(carryingResources), wx + size * 0.18, wy + size * 0.18);
                }
            }
        }
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
    
    /**
     * Gets the distance from this NPC to the given world coordinates.
     */
    public double getDistanceTo(double x, double y) {
        double dx = worldX - x;
        double dy = worldY - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // === Job System Methods ===
    
    /**
     * Assigns a job to this NPC.
     */
    public void assignJob(NPCJob job) {
        this.currentJob = job;
        this.jobProgress = 0;
        if (job != null) {
            this.currentAction = job.getNPCAction();
        }
    }
    
    /**
     * Gets the current job, if any.
     */
    public NPCJob getCurrentJob() {
        return currentJob;
    }
    
    /**
     * Checks if NPC is currently working a job.
     */
    public boolean hasJob() {
        return currentJob != null && !currentJob.isComplete();
    }
    
    /**
     * Gets resources being carried.
     */
    public int getCarryingResources() {
        return carryingResources;
    }
    
    /**
     * Gets the type of resource being carried.
     */
    public String getCarryingResourceType() {
        return carryingResourceType;
    }
    
    /**
     * Gets the NPC's inventory.
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Gets the appropriate job type for this NPC based on their type and home village.
     */
    public NPCJob.JobType getPreferredJobType() {
        // Check village type first
        if (homeTown != null && !homeTown.isMajor()) {
            VillageType villageType = homeTown.getVillageType();
            if (villageType != null) {
                return switch (villageType) {
                    case LUMBER -> NPCJob.JobType.GATHER_WOOD;
                    case MINING -> NPCJob.JobType.GATHER_STONE; // or GATHER_ORE
                    case AGRICULTURAL -> NPCJob.JobType.GATHER_CROPS;
                    case FISHING -> NPCJob.JobType.GATHER_FISH;
                    case PASTORAL -> NPCJob.JobType.TEND_LIVESTOCK;
                    default -> NPCJob.JobType.IDLE_WORK;
                };
            }
        }
        
        // Fall back to NPC type
        return switch (type) {
            case PEASANT -> NPCJob.JobType.GATHER_CROPS;
            case BLACKSMITH -> NPCJob.JobType.GATHER_ORE;
            case MERCHANT -> NPCJob.JobType.DELIVER_GOODS;
            case GUARD -> NPCJob.JobType.PATROL;
            default -> NPCJob.JobType.IDLE_WORK;
        };
    }
    
    // === Role and Schedule Methods ===
    
    public NPCRole getRole() { return role; }
    public void setRole(NPCRole role) { 
        this.role = role;
        // Adjust hiring cost based on role
        if (role != null) {
            this.hiringCost = role.isLeader() ? 200 : (role.doesTravel() ? 100 : 50);
            this.dailyWage = role.isLeader() ? 20 : (role.doesTravel() ? 10 : 5);
        }
    }
    
    public ScheduleState getScheduleState() { return scheduleState; }
    public void setScheduleState(ScheduleState state) { 
        this.scheduleState = state;
        if (state == ScheduleState.AT_HOME) {
            isInsideBuilding = true;
        } else {
            isInsideBuilding = false;
        }
    }
    
    public boolean isInsideBuilding() { return isInsideBuilding; }
    public void setInsideBuilding(boolean inside) { this.isInsideBuilding = inside; }
    
    public double getHomeX() { return homeX; }
    public double getHomeY() { return homeY; }
    public void setHomePosition(double x, double y) { 
        this.homeX = x; 
        this.homeY = y; 
    }
    
    // === Patrol Methods (for guards) ===
    
    public boolean isPatrolling() { return isPatrolling; }
    public void setIsPatrolling(boolean patrolling) { this.isPatrolling = patrolling; }
    
    public double getPatrolAngleOffset() { return patrolAngleOffset; }
    public void setPatrolAngleOffset(double offset) { this.patrolAngleOffset = offset; }
    
    public void setPatrolRadius(double radius) { this.patrolRadius = radius; }
    
    /**
     * Updates patrol position for guards.
     */
    public void updatePatrol(double deltaTime, int settlementSize) {
        if (!isPatrolling) return;
        
        patrolAngle += patrolSpeed * deltaTime;
        if (patrolAngle > Math.PI * 2) patrolAngle -= Math.PI * 2;
        
        // Calculate patrol position around settlement
        double angle = patrolAngle + patrolAngleOffset;
        double radius = settlementSize / 2.0 + 2; // Just outside settlement
        
        double cx = homeTown.getGridX() + settlementSize / 2.0;
        double cy = homeTown.getGridY() + settlementSize / 2.0;
        
        targetX = cx + Math.cos(angle) * radius;
        targetY = cy + Math.sin(angle) * radius;
        isMoving = true;
        currentAction = NPCAction.PATROLLING;
    }
    
    // === Road Movement ===
    
    public boolean isOnRoad() { return onRoad; }
    public void setOnRoad(boolean onRoad) { 
        this.onRoad = onRoad;
        this.speed = onRoad ? baseSpeed * 1.5 : baseSpeed; // 50% faster on roads
    }
    
    /**
     * Sets the road network reference for speed calculations.
     */
    public void setRoadNetwork(RoadNetwork network) {
        this.roadNetwork = network;
    }
    
    // === Hiring System ===
    
    public boolean isHired() { return isHired; }
    public void setHired(boolean hired) { this.isHired = hired; }
    
    public int getHiringCost() { return hiringCost; }
    public int getDailyWage() { return dailyWage; }
    
    // === Cargo/Gold ===
    
    public int getCarryingGold() { return carryingGold; }
    public void setCarryingGold(int gold) { this.carryingGold = gold; }
    public void addCarryingGold(int gold) { this.carryingGold += gold; }
    
    // === Assigned Destinations ===
    
    public Town getAssignedDestination() { return assignedDestination; }
    public void setAssignedDestination(Town dest) { this.assignedDestination = dest; }
    
    /**
     * Gets NPC skills as a map for display.
     */
    public java.util.Map<String, Integer> getSkills() {
        java.util.Map<String, Integer> skills = new java.util.HashMap<>();
        
        // Base skills on type and role
        switch (type) {
            case PEASANT:
                skills.put("Farming", 60 + random.nextInt(40));
                skills.put("Gathering", 50 + random.nextInt(40));
                skills.put("Strength", 40 + random.nextInt(30));
                break;
            case GUARD:
                skills.put("Combat", 60 + random.nextInt(40));
                skills.put("Defense", 50 + random.nextInt(40));
                skills.put("Vigilance", 40 + random.nextInt(30));
                break;
            case MERCHANT:
                skills.put("Trading", 70 + random.nextInt(30));
                skills.put("Negotiation", 50 + random.nextInt(40));
                skills.put("Appraisal", 40 + random.nextInt(30));
                break;
            case BLACKSMITH:
                skills.put("Smithing", 70 + random.nextInt(30));
                skills.put("Metalwork", 60 + random.nextInt(30));
                skills.put("Repair", 50 + random.nextInt(30));
                break;
            default:
                skills.put("General", 40 + random.nextInt(40));
                skills.put("Labor", 30 + random.nextInt(40));
                break;
        }
        
        return skills;
    }
}

