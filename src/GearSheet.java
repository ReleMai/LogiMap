import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// For drag-drop between inventory and gear

/**
 * Equipment panel showing all gear slots around a character silhouette.
 * Proper layout with slots positioned around the player model.
 */
public class GearSheet extends VBox {
    
    // Medieval theme colors
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String GOLD = "#c4a574";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    private static final String SLOT_EMPTY = "#1a1510";
    
    private static final int PANEL_WIDTH = 260;
    private static final int SLOT_SIZE = 44;
    
    private PlayerSprite player;
    
    private double dragStartX, dragStartY;
    private boolean isDragging = false;
    private Runnable onClose;
    private Runnable onGearChanged;  // Callback when gear changes
    
    // Equipment slots
    private Canvas[] equipSlots = new Canvas[7];
    private Equipment.Slot[] slotTypes = {
        Equipment.Slot.HEAD,
        Equipment.Slot.BODY,
        Equipment.Slot.LEGS,
        Equipment.Slot.FEET,
        Equipment.Slot.MAIN_HAND,
        Equipment.Slot.OFF_HAND,
        Equipment.Slot.CAPE
    };
    
    // Character canvas reference for refresh
    private Canvas characterCanvas;
    // Stats section reference for refresh
    private VBox statsSection;
    
    public GearSheet(PlayerSprite player) {
        this.player = player;
        
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
            createEquipmentSection(),
            createStatsSection()
        );
        
        // Add scene-level mouse release handler to clean up drag state
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
                    // Clean up drag state if still dragging
                    if (DragOverlay.isDragging()) {
                        InventoryUI.clearDragState();
                        DragOverlay.endDrag();
                        refresh();
                    }
                });
            }
        });
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
        
        Label title = new Label("Equipment");
        title.setTextFill(Color.web(GOLD));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // No close button - windows stay open in side panel
        bar.getChildren().addAll(title, spacer);
        
        setupDragging(bar);
        return bar;
    }
    
    private VBox createEquipmentSection() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(15));
        section.setAlignment(Pos.CENTER);
        section.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        // Create equipment layout around character
        // Row 1: Head slot centered
        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER);
        equipSlots[0] = createEquipSlot(Equipment.Slot.HEAD, 0);
        row1.getChildren().add(equipSlots[0]);
        
        // Row 2: Cape - Character sprite - empty
        HBox row2 = new HBox(10);
        row2.setAlignment(Pos.CENTER);
        
        equipSlots[6] = createEquipSlot(Equipment.Slot.CAPE, 6);
        
        // Character model in center - store reference for refresh
        characterCanvas = new Canvas(80, 100);
        renderCharacter(characterCanvas);
        
        VBox rightSlots = new VBox(4);
        rightSlots.setAlignment(Pos.CENTER);
        Region spacer = new Region();
        spacer.setPrefSize(SLOT_SIZE, SLOT_SIZE);
        rightSlots.getChildren().add(spacer);
        
        row2.getChildren().addAll(equipSlots[6], characterCanvas, rightSlots);
        
        // Row 3: Main Hand - Body - Off Hand
        HBox row3 = new HBox(10);
        row3.setAlignment(Pos.CENTER);
        equipSlots[4] = createEquipSlot(Equipment.Slot.MAIN_HAND, 4);
        equipSlots[1] = createEquipSlot(Equipment.Slot.BODY, 1);
        equipSlots[5] = createEquipSlot(Equipment.Slot.OFF_HAND, 5);
        row3.getChildren().addAll(equipSlots[4], equipSlots[1], equipSlots[5]);
        
        // Row 4: Legs centered
        HBox row4 = new HBox();
        row4.setAlignment(Pos.CENTER);
        equipSlots[2] = createEquipSlot(Equipment.Slot.LEGS, 2);
        row4.getChildren().add(equipSlots[2]);
        
        // Row 5: Feet centered
        HBox row5 = new HBox();
        row5.setAlignment(Pos.CENTER);
        equipSlots[3] = createEquipSlot(Equipment.Slot.FEET, 3);
        row5.getChildren().add(equipSlots[3]);
        
        section.getChildren().addAll(row1, row2, row3, row4, row5);
        return section;
    }
    
    private Canvas createEquipSlot(Equipment.Slot slot, int index) {
        Canvas canvas = new Canvas(SLOT_SIZE, SLOT_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Draw slot background
        gc.setFill(Color.web(SLOT_EMPTY));
        gc.fillRoundRect(0, 0, SLOT_SIZE, SLOT_SIZE, 5, 5);
        
        // Draw slot border
        gc.setStroke(Color.web(BORDER));
        gc.setLineWidth(2);
        gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
        
        // Draw slot label with tag
        gc.setFill(Color.web(TEXT_DIM).deriveColor(0, 1, 1, 0.3));
        gc.setFont(Font.font("Georgia", 9));
        String label = getSlotLabel(slot);
        gc.fillText(label, 6, SLOT_SIZE / 2 + 3);
        
        // Draw slot icon indicator (shows what type of item goes here)
        String slotIcon = getSlotIcon(slot);
        if (slotIcon != null) {
            gc.setFill(Color.web(TEXT_DIM).deriveColor(0, 1, 1, 0.2));
            gc.setFont(Font.font("Georgia", 16));
            gc.fillText(slotIcon, SLOT_SIZE / 2 - 6, SLOT_SIZE / 2 + 6);
        }
        
        // Check if item equipped
        Equipment equipped = player.getEquipment(slot);
        if (equipped != null) {
            renderEquippedItem(gc, equipped, slot);
        }
        
        // Add tooltip with slot tag
        String tipText = "[" + slot.name().replace("_", " ") + " SLOT]\n" + 
            (equipped != null ? equipped.getName() : "Empty - Drag equipment here");
        Tooltip tip = new Tooltip(tipText);
        tip.setStyle("-fx-background-color: " + BG_DARK + "; -fx-text-fill: " + TEXT + "; -fx-font-family: Georgia;");
        Tooltip.install(canvas, tip);
        
        // Store slot type in canvas properties for drag-drop validation
        canvas.setUserData(slot);
        
        // Drag FROM gear slot (to unequip by dragging to inventory)
        canvas.setOnDragDetected(e -> {
            Equipment currentEquip = player.getEquipment(slot);
            if (currentEquip != null) {
                canvas.startFullDrag();
                DragOverlay.startDragEquipment(currentEquip, slot, e.getSceneX(), e.getSceneY());
                canvas.setStyle("-fx-effect: dropshadow(gaussian, #c4a574, 8, 0.6, 0, 0);");
            }
        });
        
        canvas.setOnMouseDragged(e -> {
            if (DragOverlay.isDragging()) {
                DragOverlay.updatePosition(e.getSceneX(), e.getSceneY());
            }
        });
        
        // Click handler - unequip (only if not dragging)
        canvas.setOnMouseClicked(e -> {
            Equipment currentEquip = player.getEquipment(slot);
            if (currentEquip != null && !DragOverlay.isDragging()) {
                // Unequip and add back to inventory
                Equipment unequipped = player.unequip(slot);
                if (unequipped != null && player.getInventory() != null) {
                    // Try to find matching item in registry
                    String itemId = findItemIdForEquipment(unequipped.getType());
                    if (itemId != null) {
                        player.getInventory().addItem(itemId, 1);
                    }
                }
                refresh();
                if (onGearChanged != null) onGearChanged.run();
            }
        });
        
        // Drag-drop support - accept items from inventory
        canvas.setOnMouseDragEntered(e -> {
            ItemStack draggedItem = InventoryUI.getDraggedItem();
            if (draggedItem != null && canEquipToSlot(draggedItem.getItem(), slot)) {
                // Valid drop target - highlight green
                gc.setStroke(Color.web("#40a040"));
                gc.setLineWidth(3);
                gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
            } else if (draggedItem != null) {
                // Invalid drop target - highlight red
                gc.setStroke(Color.web("#a04040"));
                gc.setLineWidth(3);
                gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
            }
        });
        
        canvas.setOnMouseDragExited(e -> {
            // Redraw the slot normally
            gc.setFill(Color.web(SLOT_EMPTY));
            gc.fillRoundRect(0, 0, SLOT_SIZE, SLOT_SIZE, 5, 5);
            gc.setStroke(Color.web(BORDER));
            gc.setLineWidth(2);
            gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
            gc.setFill(Color.web(TEXT_DIM).deriveColor(0, 1, 1, 0.3));
            gc.setFont(Font.font("Georgia", 9));
            gc.fillText(label, 6, SLOT_SIZE / 2 + 3);
            if (slotIcon != null) {
                gc.setFill(Color.web(TEXT_DIM).deriveColor(0, 1, 1, 0.2));
                gc.setFont(Font.font("Georgia", 16));
                gc.fillText(slotIcon, SLOT_SIZE / 2 - 6, SLOT_SIZE / 2 + 6);
            }
            Equipment equippedNow = player.getEquipment(slot);
            if (equippedNow != null) {
                renderEquippedItem(gc, equippedNow, slot);
            }
        });
        
        canvas.setOnMouseDragReleased(e -> {
            ItemStack draggedItem = InventoryUI.getDraggedItem();
            int draggedSlot = InventoryUI.getDraggedFromSlot();
            InventoryUI draggedFrom = InventoryUI.getDraggedFromInventory();
            
            if (draggedItem != null && canEquipToSlot(draggedItem.getItem(), slot)) {
                Item item = draggedItem.getItem();
                Equipment equipment = item.toEquipment();
                
                if (equipment != null) {
                    // Equip the item
                    player.equip(equipment);
                    
                    // Remove from inventory
                    if (draggedFrom != null && draggedSlot >= 0) {
                        Inventory inv = player.getInventory();
                        if (inv != null) {
                            ItemStack invStack = inv.getSlot(draggedSlot);
                            if (invStack != null) {
                                invStack.remove(1);
                                if (invStack.isEmpty()) {
                                    inv.setSlot(draggedSlot, null);
                                }
                            }
                        }
                    }
                    
                    refresh();
                    if (onGearChanged != null) onGearChanged.run();
                }
            }
            
            InventoryUI.clearDragState();
            DragOverlay.endDrag();
        });
        
        canvas.setOnMouseEntered(e -> {
            canvas.setStyle("-fx-effect: dropshadow(gaussian, #c4a574, 5, 0.5, 0, 0);");
        });
        canvas.setOnMouseExited(e -> {
            canvas.setStyle("");
        });
        
        return canvas;
    }
    
    /**
     * Checks if an item can be equipped to a specific slot.
     */
    private boolean canEquipToSlot(Item item, Equipment.Slot slot) {
        if (item == null || !item.isEquipment()) return false;
        Equipment.Slot itemSlot = item.getEquipSlot();
        return itemSlot == slot;
    }
    
    /**
     * Gets the slot icon indicator.
     */
    private String getSlotIcon(Equipment.Slot slot) {
        return switch (slot) {
            case HEAD -> "🎩";
            case BODY -> "👕";
            case LEGS -> "👖";
            case FEET -> "👢";
            case MAIN_HAND -> "⚔";
            case OFF_HAND -> "🛡";
            case CAPE -> "🧥";
        };
    }
    
    /**
     * Finds the item ID for an equipment type.
     */
    private String findItemIdForEquipment(Equipment.Type type) {
        // Map equipment types back to item IDs
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
    
    private void renderEquippedItem(GraphicsContext gc, Equipment item, Equipment.Slot slot) {
        // Clear and redraw background
        gc.setFill(Color.web("#1a1510"));
        gc.fillRoundRect(2, 2, SLOT_SIZE - 4, SLOT_SIZE - 4, 4, 4);
        
        // Use ItemRenderer for detailed sprite
        Color itemColor = item.getPrimaryColor();
        Color secondaryColor = item.getSecondaryColor();
        if (itemColor == null) itemColor = Color.web("#606060");
        if (secondaryColor == null) secondaryColor = itemColor.darker();
        
        // Render using ItemRenderer
        double iconPadding = 4;
        double iconSize = SLOT_SIZE - iconPadding * 2;
        ItemRenderer.renderEquipment(gc, iconPadding, iconPadding, iconSize, slot, item.getType(), itemColor, secondaryColor);
    }
    
    private String getSlotLabel(Equipment.Slot slot) {
        return switch (slot) {
            case HEAD -> "Head";
            case BODY -> "Body";
            case LEGS -> "Legs";
            case FEET -> "Feet";
            case MAIN_HAND -> "Weapon";
            case OFF_HAND -> "Shield";
            case CAPE -> "Cape";
        };
    }
    
    private void renderCharacter(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0a0805"));
        gc.fillRoundRect(0, 0, 80, 100, 5, 5);
        gc.setStroke(Color.web(BORDER));
        gc.setLineWidth(1);
        gc.strokeRoundRect(0, 0, 80, 100, 5, 5);
        player.render(gc, 10, 10, 60);
    }
    
    private VBox createStatsSection() {
        VBox section = new VBox(6);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: " + BG_MED + "; -fx-background-radius: 0 0 6 6; -fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0;");
        
        Label header = new Label("Equipment Bonuses");
        header.setTextFill(Color.web(TEXT_DIM));
        header.setFont(Font.font("Georgia", 10));
        
        HBox stats = new HBox(25);
        stats.setAlignment(Pos.CENTER);
        
        // Calculate total bonuses from equipped items
        int totalAtk = 0, totalDef = 0;
        for (Equipment.Slot slot : slotTypes) {
            Equipment item = player.getEquipment(slot);
            if (item != null) {
                totalAtk += item.getAttack();
                totalDef += item.getDefense();
            }
        }
        
        VBox atkBox = createBonusStat("Attack", "+" + totalAtk, "#c08080");
        VBox defBox = createBonusStat("Defense", "+" + totalDef, "#80a0c0");
        VBox spdBox = createBonusStat("Speed", "+0", "#80c080");
        
        stats.getChildren().addAll(atkBox, defBox, spdBox);
        
        section.getChildren().addAll(header, stats);
        return section;
    }
    
    private VBox createBonusStat(String name, String value, String color) {
        VBox box = new VBox(1);
        box.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        
        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.web(TEXT_DIM));
        nameLabel.setFont(Font.font("Georgia", 9));
        
        box.getChildren().addAll(valueLabel, nameLabel);
        return box;
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
        // Refresh all equipment slots
        for (int i = 0; i < equipSlots.length; i++) {
            if (equipSlots[i] != null) {
                GraphicsContext gc = equipSlots[i].getGraphicsContext2D();
                gc.clearRect(0, 0, SLOT_SIZE, SLOT_SIZE);
                
                // Redraw slot
                gc.setFill(Color.web(SLOT_EMPTY));
                gc.fillRoundRect(0, 0, SLOT_SIZE, SLOT_SIZE, 5, 5);
                gc.setStroke(Color.web(BORDER));
                gc.setLineWidth(2);
                gc.strokeRoundRect(1, 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 5, 5);
                
                Equipment equipped = player.getEquipment(slotTypes[i]);
                if (equipped != null) {
                    renderEquippedItem(gc, equipped, slotTypes[i]);
                } else {
                    // Draw slot label and icon
                    gc.setFill(Color.web(TEXT_DIM).deriveColor(0, 1, 1, 0.3));
                    gc.setFont(Font.font("Georgia", 9));
                    String label = getSlotLabel(slotTypes[i]);
                    gc.fillText(label, 6, SLOT_SIZE / 2 + 3);
                    
                    String slotIcon = getSlotIcon(slotTypes[i]);
                    if (slotIcon != null) {
                        gc.setFill(Color.web(TEXT_DIM).deriveColor(0, 1, 1, 0.2));
                        gc.setFont(Font.font("Georgia", 16));
                        gc.fillText(slotIcon, SLOT_SIZE / 2 - 6, SLOT_SIZE / 2 + 6);
                    }
                }
                
                // Update tooltip to reflect current state
                String tipText = "[" + slotTypes[i].name().replace("_", " ") + " SLOT]\n" + 
                    (equipped != null ? equipped.getName() : "Empty - Drag equipment here");
                Tooltip tip = new Tooltip(tipText);
                tip.setStyle("-fx-background-color: " + BG_DARK + "; -fx-text-fill: " + TEXT + "; -fx-font-family: Georgia;");
                Tooltip.install(equipSlots[i], tip);
            }
        }
        
        // Refresh character sprite to show equipped gear
        if (characterCanvas != null) {
            renderCharacter(characterCanvas);
        }
        
        // Refresh stats section
        refreshStats();
    }
    
    /**
     * Refreshes the equipment stats display.
     */
    private void refreshStats() {
        // Find and update stats section if it exists
        if (getChildren().size() >= 3 && getChildren().get(2) instanceof VBox) {
            VBox oldStats = (VBox) getChildren().get(2);
            VBox newStats = createStatsSection();
            getChildren().set(2, newStats);
        }
    }
    
    public void setOnClose(Runnable callback) { this.onClose = callback; }
    
    public void setOnGearChanged(Runnable callback) { 
        this.onGearChanged = callback;
    }
}
