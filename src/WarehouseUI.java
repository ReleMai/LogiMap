import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * UI for interacting with town warehouses.
 * Allows purchasing, upgrading, and transferring items.
 */
public class WarehouseUI extends VBox {
    
    // Medieval theme colors
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String GOLD = "#c4a574";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    private static final String SLOT_EMPTY = "#151210";
    
    private static final int PANEL_WIDTH = 350;
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_GAP = 4;
    
    private TownWarehouse warehouse;
    private Inventory playerInventory;
    private PlayerSprite player;
    
    private Canvas[] warehouseSlots;
    private Label statusLabel;
    private Button upgradeButton;
    private VBox storageGrid;
    
    private Runnable onClose;
    private Runnable onInventoryChanged;
    
    /**
     * Creates a warehouse UI for a specific town.
     */
    public WarehouseUI(TownWarehouse warehouse, PlayerSprite player) {
        this.warehouse = warehouse;
        this.player = player;
        this.playerInventory = player != null ? player.getInventory() : null;
        
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
            createStatusSection(),
            createStorageSection(),
            createButtonSection()
        );
        
        refresh();
    }
    
    private HBox createTitleBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_MED + ");" +
            "-fx-background-radius: 6 6 0 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        Label title = new Label("📦 " + warehouse.getTownName() + " Warehouse");
        title.setTextFill(Color.web(GOLD));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT_DIM + ";" +
            "-fx-font-size: 14;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> { if (onClose != null) onClose.run(); });
        
        bar.getChildren().addAll(title, spacer, closeBtn);
        return bar;
    }
    
    private VBox createStatusSection() {
        VBox section = new VBox(5);
        section.setPadding(new Insets(10, 12, 10, 12));
        section.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        statusLabel = new Label();
        statusLabel.setTextFill(Color.web(TEXT));
        statusLabel.setFont(Font.font("Georgia", 11));
        statusLabel.setWrapText(true);
        
        section.getChildren().add(statusLabel);
        return section;
    }
    
    private VBox createStorageSection() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        Label header = new Label("Storage");
        header.setTextFill(Color.web(GOLD));
        header.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        
        storageGrid = new VBox(8);
        
        ScrollPane scroll = new ScrollPane(storageGrid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        scroll.setMaxHeight(200);
        scroll.setStyle("-fx-background: " + BG_DARK + "; -fx-background-color: " + BG_DARK + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        section.getChildren().addAll(header, scroll);
        return section;
    }
    
    private HBox createButtonSection() {
        HBox section = new HBox(10);
        section.setPadding(new Insets(10, 12, 12, 12));
        section.setAlignment(Pos.CENTER);
        section.setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 0 0 6 6;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        upgradeButton = new Button("Purchase Warehouse");
        styleButton(upgradeButton);
        upgradeButton.setOnAction(e -> handleUpgrade());
        
        section.getChildren().add(upgradeButton);
        return section;
    }
    
    private void styleButton(Button btn) {
        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4a3a20, #3a2a15);" +
            "-fx-text-fill: " + GOLD + ";" +
            "-fx-font-family: Georgia;" +
            "-fx-font-size: 11;" +
            "-fx-padding: 6 12;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#4a3a20", "#5a4a30")));
        btn.setOnMouseExited(e -> styleButton(btn));
    }
    
    /**
     * Handles purchase/upgrade button click.
     */
    private void handleUpgrade() {
        if (player == null) return;
        
        int cost = warehouse.isPurchased() ? warehouse.getNextUpgradeCost() : warehouse.getPurchaseCost();
        long playerCopper = player.getCurrency().getTotalCopper();
        
        if (playerCopper >= cost) {
            // Deduct cost
            player.getCurrency().subtract(cost);
            
            // Upgrade warehouse
            warehouse.upgrade();
            
            // Refresh UI
            refresh();
            
            if (onInventoryChanged != null) {
                onInventoryChanged.run();
            }
        }
    }
    
    /**
     * Refreshes the entire UI.
     */
    public void refresh() {
        updateStatus();
        updateStorageGrid();
        updateButtons();
    }
    
    private void updateStatus() {
        StringBuilder sb = new StringBuilder();
        
        if (!warehouse.isPurchased()) {
            sb.append("No warehouse established yet.\n");
            sb.append("Purchase to store items in this ").append(warehouse.isCity() ? "city" : "village").append(".\n\n");
            sb.append("Cost: ").append(formatCost(warehouse.getPurchaseCost()));
            if (warehouse.isCity()) {
                sb.append(" (20% city discount!)");
            }
        } else {
            sb.append("Tier: ").append(warehouse.getTier()).append("/").append(warehouse.getMaxTier()).append("\n");
            sb.append("Capacity: ").append(warehouse.getCapacity()).append(" slots\n");
            
            Inventory storage = warehouse.getStorage();
            if (storage != null) {
                int freeSlots = 0;
                for (int i = 0; i < storage.getSize(); i++) {
                    if (storage.isSlotEmpty(i)) freeSlots++;
                }
                int used = warehouse.getCapacity() - freeSlots;
                sb.append("Used: ").append(used).append("/").append(warehouse.getCapacity());
            }
            
            if (warehouse.getTier() < warehouse.getMaxTier()) {
                sb.append("\n\nNext upgrade: ").append(formatCost(warehouse.getNextUpgradeCost()));
                sb.append(" (+5 slots)");
            }
        }
        
        statusLabel.setText(sb.toString());
    }
    
    private void updateStorageGrid() {
        storageGrid.getChildren().clear();
        
        if (!warehouse.isPurchased()) {
            Label emptyLabel = new Label("Purchase warehouse to store items here");
            emptyLabel.setTextFill(Color.web(TEXT_DIM));
            emptyLabel.setFont(Font.font("Georgia", 11));
            storageGrid.getChildren().add(emptyLabel);
            return;
        }
        
        Inventory storage = warehouse.getStorage();
        if (storage == null) return;
        
        GridPane grid = new GridPane();
        grid.setHgap(SLOT_GAP);
        grid.setVgap(SLOT_GAP);
        grid.setAlignment(Pos.CENTER);
        
        int cols = 5;
        warehouseSlots = new Canvas[storage.getSize()];
        
        for (int i = 0; i < storage.getSize(); i++) {
            int row = i / cols;
            int col = i % cols;
            
            Canvas slot = createWarehouseSlot(i);
            warehouseSlots[i] = slot;
            grid.add(slot, col, row);
        }
        
        storageGrid.getChildren().add(grid);
    }
    
    private Canvas createWarehouseSlot(int index) {
        Canvas canvas = new Canvas(SLOT_SIZE, SLOT_SIZE);
        renderSlot(canvas, index);
        
        // Click to retrieve item
        canvas.setOnMouseClicked(e -> {
            if (warehouse.retrieveItem(playerInventory, index)) {
                refresh();
                if (onInventoryChanged != null) {
                    onInventoryChanged.run();
                }
            }
        });
        
        // Drop support - accept items from inventory
        canvas.setOnMouseDragEntered(e -> {
            if (InventoryUI.getDraggedItem() != null) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(Color.web(GOLD));
                gc.setLineWidth(2);
                gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
            }
        });
        
        canvas.setOnMouseDragExited(e -> {
            renderSlot(canvas, index);
        });
        
        canvas.setOnMouseDragReleased(e -> {
            ItemStack draggedItem = InventoryUI.getDraggedItem();
            int fromSlot = InventoryUI.getDraggedFromSlot();
            
            if (draggedItem != null && playerInventory != null) {
                if (warehouse.storeItem(playerInventory, fromSlot)) {
                    refresh();
                    if (onInventoryChanged != null) {
                        onInventoryChanged.run();
                    }
                }
            }
            
            InventoryUI.clearDragState();
            DragOverlay.endDrag();
        });
        
        return canvas;
    }
    
    private void renderSlot(Canvas canvas, int index) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, SLOT_SIZE, SLOT_SIZE);
        
        // Slot background
        gc.setFill(Color.web(SLOT_EMPTY));
        gc.fillRoundRect(0, 0, SLOT_SIZE, SLOT_SIZE, 5, 5);
        
        // Border
        gc.setStroke(Color.web(BORDER));
        gc.setLineWidth(1);
        gc.strokeRoundRect(0, 0, SLOT_SIZE, SLOT_SIZE, 5, 5);
        
        // Draw item if present
        Inventory storage = warehouse.getStorage();
        if (storage != null) {
            ItemStack stack = storage.getSlot(index);
            if (stack != null && !stack.isEmpty()) {
                renderItem(gc, stack);
            }
        }
    }
    
    private void renderItem(GraphicsContext gc, ItemStack stack) {
        Item item = stack.getItem();
        
        double padding = 4;
        double size = SLOT_SIZE - padding * 2;
        
        // Render item using ItemRenderer
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
    
    private void updateButtons() {
        long playerCopper = player != null ? player.getCurrency().getTotalCopper() : 0;
        
        if (!warehouse.isPurchased()) {
            upgradeButton.setText("Purchase Warehouse (" + formatCost(warehouse.getPurchaseCost()) + ")");
            upgradeButton.setDisable(player == null || playerCopper < warehouse.getPurchaseCost());
        } else if (warehouse.getTier() < warehouse.getMaxTier()) {
            upgradeButton.setText("Upgrade (" + formatCost(warehouse.getNextUpgradeCost()) + ")");
            upgradeButton.setDisable(player == null || playerCopper < warehouse.getNextUpgradeCost());
        } else {
            upgradeButton.setText("Maximum Capacity");
            upgradeButton.setDisable(true);
        }
    }
    
    private String formatCost(int copper) {
        if (copper >= 1000) {
            return (copper / 1000) + "g " + ((copper % 1000) / 100) + "s";
        } else if (copper >= 100) {
            return (copper / 100) + " silver";
        }
        return copper + " copper";
    }
    
    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }
    
    public void setOnInventoryChanged(Runnable callback) {
        this.onInventoryChanged = callback;
    }
}
