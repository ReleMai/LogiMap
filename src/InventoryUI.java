import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.function.BiConsumer;

/**
 * Visual grid-based inventory UI component.
 * Supports drag-and-drop, shift-click transfers, and item tooltips.
 */
public class InventoryUI extends VBox {
    
    // Style constants - Medieval theme
    private static final String DARK_BG = "#1f1a10";
    private static final String MEDIUM_BG = "#2d2418";
    private static final String LIGHT_BG = "#3d3020";
    private static final String ACCENT_COLOR = "#c4a574";
    private static final String TEXT_COLOR = "#d4c4a4";
    private static final String SLOT_BG = "#252015";
    private static final String SLOT_HOVER = "#3a3020";
    private static final String SLOT_SELECTED = "#4a4030";
    private static final String BORDER_COLOR = "#5a4a30";
    
    // Slot size - smaller for compact UI
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_PADDING = 3;
    
    // Inventory reference
    private final Inventory inventory;
    private final Canvas canvas;
    private final GraphicsContext gc;
    
    // Interaction state
    private int hoveredSlot = -1;
    private int selectedSlot = -1;
    private ItemStack draggedItem = null;
    private double dragX, dragY;
    private boolean isDragging = false;
    
    // Tooltip
    private Tooltip itemTooltip;
    
    // Events
    private BiConsumer<Integer, ItemStack> onSlotClick;
    private BiConsumer<Integer, Inventory> onShiftClick;
    private Runnable onClose;
    
    // Animation
    private AnimationTimer renderTimer;
    
    public InventoryUI(Inventory inventory) {
        this.inventory = inventory;
        
        setSpacing(6);
        setPadding(new Insets(0));
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
        // No background - will be inside DraggableWindow
        setStyle("-fx-background-color: transparent;");
        
        // Title bar with sort buttons
        HBox titleBar = createTitleBar();
        
        // Canvas for rendering slots
        int canvasWidth = inventory.getCols() * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING;
        int canvasHeight = inventory.getRows() * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING;
        
        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D();
        
        // Setup interaction
        setupMouseHandlers();
        
        // Create tooltip
        itemTooltip = new Tooltip();
        itemTooltip.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 8;" +
            "-fx-font-size: 12;"
        );
        Tooltip.install(canvas, itemTooltip);
        
        // Inventory change listener
        inventory.setOnSlotChanged(slot -> render());
        inventory.setOnInventoryChanged(this::render);
        
        getChildren().addAll(titleBar, canvas);
        
        // Initial render
        render();
    }
    
    private HBox createTitleBar() {
        HBox titleBar = new HBox(8);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 0, 4, 0));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Sort buttons
        Button sortNameBtn = createSortButton("A-Z", "Sort by name", () -> inventory.sort(Inventory.SortMode.NAME));
        Button sortRarityBtn = createSortButton("★", "Sort by rarity", () -> inventory.sort(Inventory.SortMode.RARITY));
        Button sortValueBtn = createSortButton("$", "Sort by value", () -> inventory.sort(Inventory.SortMode.VALUE));
        Button stackBtn = createSortButton("⊞", "Stack items", () -> inventory.stackItems());
        
        // Slot count
        Label slotCount = new Label(inventory.getUsedSlots() + "/" + inventory.getSize());
        slotCount.setTextFill(Color.web(TEXT_COLOR, 0.7));
        slotCount.setFont(Font.font("System", 11));
        
        inventory.setOnInventoryChanged(() -> {
            slotCount.setText(inventory.getUsedSlots() + "/" + inventory.getSize());
            render();
        });
        
        titleBar.getChildren().addAll(sortNameBtn, sortRarityBtn, sortValueBtn, stackBtn, spacer, slotCount);
        
        return titleBar;
    }
    
    private Button createSortButton(String text, String tooltip, Runnable action) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 4 8 4 8;" +
            "-fx-font-size: 11;" +
            "-fx-cursor: hand;"
        );
        btn.setOnAction(e -> action.run());
        return btn;
    }
    
    private void setupMouseHandlers() {
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMousePressed(this::handleMousePress);
        canvas.setOnMouseDragged(this::handleMouseDrag);
        canvas.setOnMouseReleased(this::handleMouseRelease);
        canvas.setOnMouseExited(e -> {
            hoveredSlot = -1;
            itemTooltip.hide();
            render();
        });
    }
    
    private void handleMouseMove(MouseEvent e) {
        int slot = getSlotAtPosition(e.getX(), e.getY());
        
        if (slot != hoveredSlot) {
            hoveredSlot = slot;
            updateTooltip(slot);
            render();
        }
    }
    
    private void handleMousePress(MouseEvent e) {
        int slot = getSlotAtPosition(e.getX(), e.getY());
        if (slot == -1) return;
        
        ItemStack stack = inventory.getSlot(slot);
        
        if (e.getButton() == MouseButton.PRIMARY) {
            if (e.isShiftDown() && onShiftClick != null) {
                // Shift-click transfer
                onShiftClick.accept(slot, inventory);
            } else if (stack != null && !stack.isEmpty()) {
                // Start drag
                selectedSlot = slot;
                isDragging = true;
                draggedItem = stack.copy();
                dragX = e.getX();
                dragY = e.getY();
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {
            // Right-click: split stack in half
            if (stack != null && stack.getQuantity() > 1) {
                ItemStack split = stack.splitHalf();
                ItemStack remaining = inventory.addItem(split);
                if (remaining != null) {
                    // Put back if couldn't add
                    stack.add(remaining.getQuantity());
                }
            }
        }
        
        if (onSlotClick != null) {
            onSlotClick.accept(slot, stack);
        }
        
        render();
    }
    
    private void handleMouseDrag(MouseEvent e) {
        if (isDragging && draggedItem != null) {
            dragX = e.getX();
            dragY = e.getY();
            render();
        }
    }
    
    private void handleMouseRelease(MouseEvent e) {
        if (isDragging && draggedItem != null) {
            int targetSlot = getSlotAtPosition(e.getX(), e.getY());
            
            if (targetSlot != -1 && targetSlot != selectedSlot) {
                // Try to merge or swap
                ItemStack targetStack = inventory.getSlot(targetSlot);
                
                if (targetStack == null || targetStack.isEmpty()) {
                    // Move to empty slot
                    inventory.swapSlots(selectedSlot, targetSlot);
                } else if (targetStack.canMergeWith(draggedItem)) {
                    // Merge stacks
                    ItemStack sourceStack = inventory.getSlot(selectedSlot);
                    ItemStack remaining = targetStack.merge(sourceStack);
                    inventory.setSlot(selectedSlot, remaining);
                } else {
                    // Swap different items
                    inventory.swapSlots(selectedSlot, targetSlot);
                }
            }
            
            // Play animation
            playDropAnimation(targetSlot != -1 ? targetSlot : selectedSlot);
        }
        
        isDragging = false;
        draggedItem = null;
        selectedSlot = -1;
        render();
    }
    
    private int getSlotAtPosition(double x, double y) {
        int col = (int) ((x - SLOT_PADDING) / (SLOT_SIZE + SLOT_PADDING));
        int row = (int) ((y - SLOT_PADDING) / (SLOT_SIZE + SLOT_PADDING));
        
        if (col < 0 || col >= inventory.getCols()) return -1;
        if (row < 0 || row >= inventory.getRows()) return -1;
        
        // Check if actually inside the slot (not in padding)
        double slotX = SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
        double slotY = SLOT_PADDING + row * (SLOT_SIZE + SLOT_PADDING);
        
        if (x < slotX || x > slotX + SLOT_SIZE) return -1;
        if (y < slotY || y > slotY + SLOT_SIZE) return -1;
        
        return row * inventory.getCols() + col;
    }
    
    private void updateTooltip(int slot) {
        if (slot == -1) {
            itemTooltip.setText("");
            itemTooltip.hide();
            return;
        }
        
        ItemStack stack = inventory.getSlot(slot);
        if (stack == null || stack.isEmpty()) {
            itemTooltip.setText("Empty slot");
        } else {
            itemTooltip.setText(stack.getItem().getTooltip() + 
                (stack.getQuantity() > 1 ? "\n\nQuantity: " + stack.getQuantity() : ""));
        }
    }
    
    // === Rendering ===
    
    public void render() {
        // Clear canvas
        gc.setFill(Color.web(DARK_BG));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw slots
        for (int row = 0; row < inventory.getRows(); row++) {
            for (int col = 0; col < inventory.getCols(); col++) {
                int slot = row * inventory.getCols() + col;
                double x = SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
                double y = SLOT_PADDING + row * (SLOT_SIZE + SLOT_PADDING);
                
                renderSlot(slot, x, y);
            }
        }
        
        // Draw dragged item on top
        if (isDragging && draggedItem != null) {
            renderDraggedItem();
        }
    }
    
    private void renderSlot(int slot, double x, double y) {
        ItemStack stack = inventory.getSlot(slot);
        boolean isHovered = slot == hoveredSlot;
        boolean isSelected = slot == selectedSlot;
        
        // Slot background
        if (isSelected) {
            gc.setFill(Color.web(SLOT_SELECTED));
        } else if (isHovered) {
            gc.setFill(Color.web(SLOT_HOVER));
        } else {
            gc.setFill(Color.web(SLOT_BG));
        }
        gc.fillRoundRect(x, y, SLOT_SIZE, SLOT_SIZE, 6, 6);
        
        // Slot border
        gc.setStroke(Color.web(LIGHT_BG));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, SLOT_SIZE, SLOT_SIZE, 6, 6);
        
        // Item
        if (stack != null && !stack.isEmpty() && !isSelected) {
            renderItem(stack, x + 4, y + 4, SLOT_SIZE - 8);
        }
        
        // Highlight if hovered
        if (isHovered && !isDragging) {
            gc.setStroke(Color.web(ACCENT_COLOR, 0.6));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x + 1, y + 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
        }
    }
    
    private void renderItem(ItemStack stack, double x, double y, double size) {
        Item item = stack.getItem();
        
        // Render item icon
        item.renderIcon(gc, x, y, size);
        
        // Quantity
        if (stack.getQuantity() > 1) {
            String qtyText = String.valueOf(stack.getQuantity());
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("System", FontWeight.BOLD, 11));
            gc.fillText(qtyText, x + size - gc.getFont().getSize() * qtyText.length() * 0.6 + 1, y + size - 2 + 1);
            gc.setFill(Color.WHITE);
            gc.fillText(qtyText, x + size - gc.getFont().getSize() * qtyText.length() * 0.6, y + size - 2);
        }
        
        // Durability bar (if applicable)
        if (item.isEquipment() && item.getDurability() < item.getMaxDurability()) {
            double durPercent = (double) item.getDurability() / item.getMaxDurability();
            double barWidth = size * 0.8;
            double barHeight = 3;
            double barX = x + (size - barWidth) / 2;
            double barY = y + size - barHeight - 2;
            
            // Background
            gc.setFill(Color.web("#333333"));
            gc.fillRect(barX, barY, barWidth, barHeight);
            
            // Durability
            Color durColor = durPercent > 0.5 ? Color.web("#44ff44") : 
                            durPercent > 0.25 ? Color.web("#ffff44") : Color.web("#ff4444");
            gc.setFill(durColor);
            gc.fillRect(barX, barY, barWidth * durPercent, barHeight);
        }
    }
    
    private void renderDraggedItem() {
        if (draggedItem == null) return;
        
        double size = SLOT_SIZE - 8;
        double x = dragX - size / 2;
        double y = dragY - size / 2;
        
        // Semi-transparent
        gc.setGlobalAlpha(0.8);
        renderItem(draggedItem, x, y, size);
        gc.setGlobalAlpha(1.0);
    }
    
    private void playDropAnimation(int slot) {
        int col = slot % inventory.getCols();
        int row = slot / inventory.getCols();
        double targetX = SLOT_PADDING + col * (SLOT_SIZE + SLOT_PADDING);
        double targetY = SLOT_PADDING + row * (SLOT_SIZE + SLOT_PADDING);
        
        // Simple scale animation effect
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), canvas);
        scale.setFromX(1.02);
        scale.setFromY(1.02);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }
    
    // === Public API ===
    
    public void setOnSlotClick(BiConsumer<Integer, ItemStack> callback) {
        this.onSlotClick = callback;
    }
    
    public void setOnShiftClick(BiConsumer<Integer, Inventory> callback) {
        this.onShiftClick = callback;
    }
    
    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }

    public Inventory getInventory() {
        return inventory;
    }
    
    public void refresh() {
        render();
    }
}
