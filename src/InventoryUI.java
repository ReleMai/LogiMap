import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Grid-based inventory panel with proper item slots and drag/drop.
 * Shows items in a scrollable grid with item info.
 */
public class InventoryUI extends VBox {
    
    // Medieval theme colors
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String GOLD = "#c4a574";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    private static final String SLOT_EMPTY = "#151210";
    
    private static final int PANEL_WIDTH = 320;
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_GAP = 4;
    private static final int VISIBLE_ROWS = 4;
    
    private Inventory inventory;
    private Canvas[] slots;
    private int selectedSlot = -1;
    private ItemStack draggedStack = null;
    
    private double dragStartX, dragStartY;
    private boolean isDragging = false;
    private Runnable onClose;
    
    // Info panel elements
    private Label itemNameLabel;
    private Label itemDescLabel;
    private VBox itemInfoBox;
    private PlayerSprite player; // Reference to player for equipping
    
    // Drag and drop support
    private static ItemStack draggedItem = null;
    private static int draggedFromSlot = -1;
    private static InventoryUI draggedFromInventory = null;
    
    public InventoryUI(Inventory inventory) {
        this(inventory, null);
    }
    
    public InventoryUI(Inventory inventory, PlayerSprite player) {
        this.inventory = inventory;
        this.player = player;
        this.slots = new Canvas[inventory.getSize()];
        
        setPrefWidth(PANEL_WIDTH);
        setMaxWidth(PANEL_WIDTH);
        setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);"
        );
        setSpacing(0);
        setPadding(new Insets(0));
        
        getChildren().addAll(
            createTitleBar(),
            createInventoryGrid(),
            createInfoPanel()
        );
        
        // Register for inventory changes to auto-refresh
        inventory.setOnInventoryChanged(() -> {
            javafx.application.Platform.runLater(() -> refresh());
        });
    }
    
    /**
     * Gets the currently dragged item (for cross-panel drag-drop).
     */
    public static ItemStack getDraggedItem() { return draggedItem; }
    public static int getDraggedFromSlot() { return draggedFromSlot; }
    public static InventoryUI getDraggedFromInventory() { return draggedFromInventory; }
    
    /**
     * Clears drag state.
     */
    public static void clearDragState() {
        draggedItem = null;
        draggedFromSlot = -1;
        draggedFromInventory = null;
    }
    
    /**
     * Finds the item ID for an equipment type (for converting equipment back to inventory items).
     */
    private String findItemIdForEquipment(Equipment.Type type) {
        return switch (type) {
            case RAGGED_SHIRT -> "ragged_shirt";
            case RAGGED_PANTS -> "ragged_pants";
            case CLOTH_SHIRT -> "cloth_shirt";
            case CLOTH_PANTS -> "cloth_pants";
            case LEATHER_CAP -> "leather_cap";
            case LEATHER_VEST -> "leather_vest";
            case LEATHER_PANTS -> "leather_pants";
            case LEATHER_BOOTS -> "leather_boots";
            case IRON_HELM -> "iron_helm";
            case IRON_PLATE -> "iron_chest";
            case IRON_GREAVES -> "iron_greaves";
            case IRON_BOOTS -> "iron_boots";
            case IRON_SWORD -> "iron_sword";
            case IRON_SHIELD -> "iron_shield";
            case WOODEN_SWORD -> "wooden_sword";
            case WOODEN_SHIELD -> "wooden_shield";
            case CLOTH_CAPE -> "cloth_cape";
            default -> null;
        };
    }
    
    private HBox createTitleBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(6, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_MED + ");" +
            "-fx-background-radius: 6 6 0 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        bar.setCursor(javafx.scene.Cursor.MOVE);
        
        Label title = new Label("Inventory");
        title.setTextFill(Color.web(GOLD));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Capacity label
        Label capacityLabel = new Label(getItemCount() + "/" + inventory.getSize());
        capacityLabel.setTextFill(Color.web(TEXT_DIM));
        capacityLabel.setFont(Font.font("Georgia", 11));
        
        // No close button - windows stay open in side panel
        bar.getChildren().addAll(title, spacer, capacityLabel);
        
        setupDragging(bar);
        return bar;
    }
    
    private int getItemCount() {
        int count = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!inventory.isSlotEmpty(i)) count++;
        }
        return count;
    }
    
    private ScrollPane createInventoryGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(SLOT_GAP);
        grid.setVgap(SLOT_GAP);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        int cols = inventory.getCols();
        int rows = inventory.getRows();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            int row = i / cols;
            int col = i % cols;
            
            Canvas slot = createSlot(i);
            slots[i] = slot;
            grid.add(slot, col, row);
        }
        
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight((SLOT_SIZE + SLOT_GAP) * Math.min(VISIBLE_ROWS, rows) + 20);
        scroll.setMaxHeight((SLOT_SIZE + SLOT_GAP) * Math.min(VISIBLE_ROWS, rows) + 20);
        scroll.setStyle("-fx-background: " + BG_DARK + "; -fx-background-color: " + BG_DARK + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        return scroll;
    }
    
    private Canvas createSlot(int index) {
        Canvas canvas = new Canvas(SLOT_SIZE, SLOT_SIZE);
        renderSlot(canvas, index);
        
        canvas.setOnMouseClicked(e -> {
            ItemStack stack = inventory.getSlot(index);
            if (stack != null && !stack.isEmpty()) {
                selectedSlot = index;
                updateItemInfo(stack);
                refresh();
                
                // Double click to equip (if equippable)
                if (e.getClickCount() == 2) {
                    Item item = stack.getItem();
                    if (item.isEquipment() && player != null) {
                        Equipment equipment = item.toEquipment();
                        if (equipment != null) {
                            player.equip(equipment);
                            // Remove the item from inventory
                            stack.remove(1);
                            if (stack.isEmpty()) {
                                inventory.setSlot(index, null);
                            }
                            notifyItemEquipped(item);
                            refresh();
                        }
                    }
                }
            } else {
                selectedSlot = -1;
                updateItemInfo(null);
                refresh();
            }
        });
        
        canvas.setOnMouseEntered(e -> {
            ItemStack stack = inventory.getSlot(index);
            if (stack != null && !stack.isEmpty()) {
                Item item = stack.getItem();
                Tooltip tip = new Tooltip(item.getName() + "\n" + item.getDescription());
                tip.setStyle("-fx-background-color: " + BG_DARK + "; -fx-text-fill: " + TEXT + "; -fx-font-family: Georgia;");
                Tooltip.install(canvas, tip);
            }
        });
        
        // Drag start - use static state for cross-panel drag
        canvas.setOnDragDetected(e -> {
            ItemStack stack = inventory.getSlot(index);
            if (stack != null && !stack.isEmpty()) {
                draggedItem = stack;
                draggedFromSlot = index;
                draggedFromInventory = this;
                draggedStack = stack; // Keep local for internal drag
                canvas.startFullDrag();
                
                // Visual feedback - highlight the dragged slot
                canvas.setStyle("-fx-effect: dropshadow(gaussian, #c4a574, 8, 0.6, 0, 0);");
                
                // Show ghost sprite at cursor
                DragOverlay.startDrag(stack.getItem(), e.getSceneX(), e.getSceneY());
            }
        });
        
        // Track mouse movement for ghost sprite
        canvas.setOnMouseDragged(e -> {
            if (DragOverlay.isDragging()) {
                DragOverlay.updatePosition(e.getSceneX(), e.getSceneY());
            }
        });
        
        // Drag over
        canvas.setOnMouseDragEntered(e -> {
            if (draggedItem != null || draggedStack != null) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(Color.web(GOLD));
                gc.setLineWidth(2);
                gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
            }
        });
        
        canvas.setOnMouseDragExited(e -> {
            renderSlot(canvas, index);
        });
        
        // Drop - handle both internal and cross-panel drops
        canvas.setOnMouseDragReleased(e -> {
            // Check if dropping equipment from gear slot
            Equipment draggedEquip = DragOverlay.getDraggedEquipment();
            if (draggedEquip != null && player != null) {
                // Unequip and add to this slot
                Equipment.Slot fromSlot = DragOverlay.getDraggedFromSlot();
                if (fromSlot != null) {
                    player.unequip(fromSlot);
                    String itemId = findItemIdForEquipment(draggedEquip.getType());
                    if (itemId != null) {
                        inventory.addItem(itemId, 1);
                    }
                    refresh();
                    if (onItemEquipped != null) onItemEquipped.accept(null);
                }
            } else if (draggedStack != null) {
                int fromIndex = findSlotIndex(draggedStack);
                if (fromIndex != index && fromIndex != -1) {
                    inventory.swapSlots(fromIndex, index);
                    refresh();
                }
            }
            // Save the source slot index before clearing state
            int sourceSlot = draggedFromSlot;
            
            // Clear all drag states
            draggedStack = null;
            clearDragState();
            DragOverlay.endDrag();
            
            // Clear visual feedback from source slot (use saved index)
            if (sourceSlot >= 0 && sourceSlot < slots.length) {
                slots[sourceSlot].setStyle("");
            }
        });
        
        return canvas;
    }
    
    private int findSlotIndex(ItemStack stack) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getSlot(i) == stack) return i;
        }
        return -1;
    }
    
    private void renderSlot(Canvas canvas, int index) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, SLOT_SIZE, SLOT_SIZE);
        
        // Slot background
        gc.setFill(Color.web(SLOT_EMPTY));
        gc.fillRoundRect(0, 0, SLOT_SIZE, SLOT_SIZE, 5, 5);
        
        // Selection highlight
        if (index == selectedSlot) {
            gc.setStroke(Color.web(GOLD));
            gc.setLineWidth(2);
            gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
        } else {
            gc.setStroke(Color.web(BORDER));
            gc.setLineWidth(1);
            gc.strokeRoundRect(0, 0, SLOT_SIZE, SLOT_SIZE, 5, 5);
        }
        
        // Draw item if present
        ItemStack stack = inventory.getSlot(index);
        if (stack != null && !stack.isEmpty()) {
            renderItem(gc, stack);
        }
    }
    
    private void renderItem(GraphicsContext gc, ItemStack stack) {
        Item item = stack.getItem();
        
        // Use ItemRenderer for detailed medieval sprites (same as gear sheet)
        Color primary = item.getPrimaryColor();
        Color secondary = item.getSecondaryColor();
        if (primary == null) primary = item.getRarity().getColor();
        if (secondary == null) secondary = primary.darker();
        
        double padding = 4;
        double size = SLOT_SIZE - padding * 2;
        
        // Render based on item type using ItemRenderer
        item.renderItemShape(gc, padding, padding, size);
        
        // Stack count
        if (stack.getQuantity() > 1) {
            gc.setFill(Color.web("#000000").deriveColor(0, 1, 1, 0.6));
            gc.fillRoundRect(SLOT_SIZE - 18, SLOT_SIZE - 16, 16, 14, 3, 3);
            gc.setFill(Color.web("#ffffff"));
            gc.setFont(Font.font("Georgia", FontWeight.BOLD, 10));
            String qty = stack.getQuantity() > 99 ? "99+" : String.valueOf(stack.getQuantity());
            gc.fillText(qty, SLOT_SIZE - 16, SLOT_SIZE - 5);
        }
    }
    
    private VBox createInfoPanel() {
        itemInfoBox = new VBox(4);
        itemInfoBox.setPadding(new Insets(10));
        itemInfoBox.setStyle("-fx-background-color: " + BG_MED + "; -fx-background-radius: 0 0 6 6; -fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0;");
        itemInfoBox.setPrefHeight(80);
        itemInfoBox.setMinHeight(80);
        
        itemNameLabel = new Label("Select an item");
        itemNameLabel.setTextFill(Color.web(GOLD));
        itemNameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        
        itemDescLabel = new Label("Click on an item to see details\nDouble-click to equip");
        itemDescLabel.setTextFill(Color.web(TEXT_DIM));
        itemDescLabel.setFont(Font.font("Georgia", 10));
        itemDescLabel.setWrapText(true);
        
        itemInfoBox.getChildren().addAll(itemNameLabel, itemDescLabel);
        return itemInfoBox;
    }
    
    private void updateItemInfo(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            Item item = stack.getItem();
            itemNameLabel.setText(item.getName() + (stack.getQuantity() > 1 ? " x" + stack.getQuantity() : ""));
            
            StringBuilder desc = new StringBuilder(item.getDescription());
            desc.append("\nRarity: ").append(item.getRarity().getDisplayName());
            desc.append("\nValue: ").append(stack.getTotalValue()).append(" copper");
            
            if (item.isEquipment()) {
                desc.append("\nSlot: ").append(item.getEquipSlot().name());
                if (item.getAttack() > 0) desc.append("\nAttack: +").append(item.getAttack());
                if (item.getDefense() > 0) desc.append("\nDefense: +").append(item.getDefense());
                desc.append("\n[Double-click to equip]");
            }
            itemDescLabel.setText(desc.toString());
        } else {
            itemNameLabel.setText("Select an item");
            itemDescLabel.setText("Click on an item to see details\nDouble-click to equip");
        }
    }
    
    private Button createCloseButton() {
        Button btn = new Button("X");
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 0 4; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #5a2020; -fx-text-fill: #ff8080; -fx-font-size: 12; -fx-padding: 0 4; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT + "; -fx-font-size: 12; -fx-padding: 0 4; -fx-cursor: hand;"));
        btn.setOnAction(e -> { if (onClose != null) onClose.run(); setVisible(false); });
        return btn;
    }
    
    private void setupDragging(HBox bar) {
        bar.setOnMousePressed(e -> {
            if (getParent() instanceof Pane) {
                dragStartX = e.getSceneX() - getLayoutX();
                dragStartY = e.getSceneY() - getLayoutY();
                isDragging = true;
                toFront();
            }
        });
        bar.setOnMouseDragged(e -> {
            if (isDragging && getParent() instanceof Pane) {
                Pane parent = (Pane) getParent();
                setLayoutX(Math.max(0, Math.min(e.getSceneX() - dragStartX, parent.getWidth() - getWidth())));
                setLayoutY(Math.max(0, Math.min(e.getSceneY() - dragStartY, parent.getHeight() - getHeight())));
            }
        });
        bar.setOnMouseReleased(e -> isDragging = false);
    }
    
    public void refresh() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                renderSlot(slots[i], i);
            }
        }
        
        // Update info if item selected
        if (selectedSlot >= 0 && selectedSlot < inventory.getSize()) {
            ItemStack stack = inventory.getSlot(selectedSlot);
            updateItemInfo(stack);
        }
    }
    
    public void setOnClose(Runnable callback) { this.onClose = callback; }
    
    /**
     * Callback when an item is equipped from inventory.
     */
    private java.util.function.Consumer<Item> onItemEquipped;
    
    public void setOnItemEquipped(java.util.function.Consumer<Item> callback) { 
        this.onItemEquipped = callback; 
    }
    
    /**
     * Called when an item is equipped - notifies listeners.
     */
    protected void notifyItemEquipped(Item item) {
        if (onItemEquipped != null) {
            onItemEquipped.accept(item);
        }
    }
}
