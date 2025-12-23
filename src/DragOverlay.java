import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Ghost sprite overlay that follows the cursor when dragging items.
 * Provides visual feedback during drag-and-drop operations.
 */
public class DragOverlay {
    
    private static Canvas overlayCanvas;
    private static Pane parentLayer;
    private static boolean isDragging = false;
    
    // Current drag info
    private static Item draggedItem;
    private static Equipment draggedEquipment;
    private static Equipment.Slot draggedSlot;
    
    private static final int OVERLAY_SIZE = 48;
    
    /**
     * Initializes the drag overlay system with the floating panel layer.
     */
    public static void initialize(Pane floatingLayer) {
        parentLayer = floatingLayer;
        overlayCanvas = new Canvas(OVERLAY_SIZE, OVERLAY_SIZE);
        overlayCanvas.setMouseTransparent(true);
        overlayCanvas.setVisible(false);
        overlayCanvas.setOpacity(0.8);
        
        // Add to layer but keep invisible until drag starts
        if (!floatingLayer.getChildren().contains(overlayCanvas)) {
            floatingLayer.getChildren().add(overlayCanvas);
        }
    }
    
    /**
     * Starts dragging an item from inventory.
     */
    public static void startDrag(Item item, double x, double y) {
        if (overlayCanvas == null || parentLayer == null) return;
        
        draggedItem = item;
        draggedEquipment = null;
        draggedSlot = null;
        isDragging = true;
        
        renderItemGhost();
        updatePosition(x, y);
        overlayCanvas.setVisible(true);
        overlayCanvas.toFront();
    }
    
    /**
     * Starts dragging equipment from a gear slot.
     */
    public static void startDragEquipment(Equipment equip, Equipment.Slot slot, double x, double y) {
        if (overlayCanvas == null || parentLayer == null) return;
        
        draggedItem = null;
        draggedEquipment = equip;
        draggedSlot = slot;
        isDragging = true;
        
        renderEquipmentGhost();
        updatePosition(x, y);
        overlayCanvas.setVisible(true);
        overlayCanvas.toFront();
    }
    
    /**
     * Updates the overlay position to follow the cursor.
     */
    public static void updatePosition(double sceneX, double sceneY) {
        if (overlayCanvas == null || !isDragging) return;
        
        // Convert scene coordinates to parent coordinates
        if (parentLayer != null) {
            javafx.geometry.Point2D local = parentLayer.sceneToLocal(sceneX, sceneY);
            overlayCanvas.setLayoutX(local.getX() - OVERLAY_SIZE / 2);
            overlayCanvas.setLayoutY(local.getY() - OVERLAY_SIZE / 2);
        }
    }
    
    /**
     * Ends the drag operation and hides the overlay.
     */
    public static void endDrag() {
        isDragging = false;
        draggedItem = null;
        draggedEquipment = null;
        draggedSlot = null;
        
        if (overlayCanvas != null) {
            overlayCanvas.setVisible(false);
        }
    }
    
    /**
     * Returns true if currently dragging.
     */
    public static boolean isDragging() {
        return isDragging;
    }
    
    /**
     * Gets the dragged equipment (if dragging from gear slot).
     */
    public static Equipment getDraggedEquipment() {
        return draggedEquipment;
    }
    
    /**
     * Gets the slot the equipment was dragged from.
     */
    public static Equipment.Slot getDraggedFromSlot() {
        return draggedSlot;
    }
    
    /**
     * Renders the item as a ghost sprite.
     */
    private static void renderItemGhost() {
        if (overlayCanvas == null || draggedItem == null) return;
        
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, OVERLAY_SIZE, OVERLAY_SIZE);
        
        // Semi-transparent background
        gc.setFill(Color.web("#1a1208").deriveColor(0, 1, 1, 0.7));
        gc.fillRoundRect(0, 0, OVERLAY_SIZE, OVERLAY_SIZE, 6, 6);
        
        // Border with glow effect
        gc.setStroke(Color.web("#c4a574").deriveColor(0, 1, 1, 0.8));
        gc.setLineWidth(2);
        gc.strokeRoundRect(1, 1, OVERLAY_SIZE - 2, OVERLAY_SIZE - 2, 6, 6);
        
        // Render the item using ItemRenderer
        Item item = draggedItem;
        Color primary = item.getPrimaryColor() != null ? item.getPrimaryColor() : Color.web("#606060");
        Color secondary = item.getSecondaryColor() != null ? item.getSecondaryColor() : primary.darker();
        
        double padding = 6;
        double size = OVERLAY_SIZE - padding * 2;
        
        if (item.isEquipment() && item.getEquipSlot() != null) {
            Equipment.Type type = item.getEquipType();
            if (type != null) {
                ItemRenderer.renderEquipment(gc, padding, padding, size, item.getEquipSlot(), type, primary, secondary);
            } else {
                renderGenericIcon(gc, item, padding, size);
            }
        } else {
            // Use ItemRenderer for other item types
            item.renderItemShape(gc, padding, padding, size);
        }
    }
    
    /**
     * Renders the equipment as a ghost sprite.
     */
    private static void renderEquipmentGhost() {
        if (overlayCanvas == null || draggedEquipment == null) return;
        
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, OVERLAY_SIZE, OVERLAY_SIZE);
        
        // Semi-transparent background
        gc.setFill(Color.web("#1a1208").deriveColor(0, 1, 1, 0.7));
        gc.fillRoundRect(0, 0, OVERLAY_SIZE, OVERLAY_SIZE, 6, 6);
        
        // Border with glow effect
        gc.setStroke(Color.web("#c4a574").deriveColor(0, 1, 1, 0.8));
        gc.setLineWidth(2);
        gc.strokeRoundRect(1, 1, OVERLAY_SIZE - 2, OVERLAY_SIZE - 2, 6, 6);
        
        // Render equipment using ItemRenderer
        Equipment equip = draggedEquipment;
        Color primary = equip.getPrimaryColor() != null ? equip.getPrimaryColor() : Color.web("#606060");
        Color secondary = equip.getSecondaryColor() != null ? equip.getSecondaryColor() : primary.darker();
        
        double padding = 6;
        double size = OVERLAY_SIZE - padding * 2;
        
        if (draggedSlot != null) {
            ItemRenderer.renderEquipment(gc, padding, padding, size, draggedSlot, equip.getType(), primary, secondary);
        }
    }
    
    /**
     * Renders a generic icon for items without specific rendering.
     */
    private static void renderGenericIcon(GraphicsContext gc, Item item, double padding, double size) {
        Color color = item.getPrimaryColor() != null ? item.getPrimaryColor() : Color.web("#606060");
        gc.setFill(color);
        gc.fillRoundRect(padding + 4, padding + 4, size - 8, size - 8, 4, 4);
        
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Georgia", javafx.scene.text.FontWeight.BOLD, 16));
        String initial = item.getName().substring(0, 1).toUpperCase();
        gc.fillText(initial, OVERLAY_SIZE / 2 - 5, OVERLAY_SIZE / 2 + 5);
    }
}
