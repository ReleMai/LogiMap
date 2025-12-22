import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Gear Sheet UI - Shows player sprite with equipment slots around it.
 * Draggable window that displays equipped gear and allows equip/unequip.
 */
public class GearSheet extends DraggableWindow {
    
    // Style constants
    private static final String SLOT_BG = "#252015";
    private static final String SLOT_HOVER = "#3a3020";
    private static final String SLOT_EMPTY = "#1a1510";
    private static final String ACCENT_COLOR = "#c4a574";
    private static final String TEXT_COLOR = "#d4c4a4";
    
    // Slot size
    private static final int SLOT_SIZE = 44;
    
    // Player reference
    private PlayerSprite player;
    
    // Canvas for sprite preview
    private Canvas spriteCanvas;
    
    // Equipment slot areas
    private Canvas headSlot;
    private Canvas bodySlot;
    private Canvas legsSlot;
    private Canvas feetSlot;
    private Canvas mainHandSlot;
    private Canvas offHandSlot;
    private Canvas capeSlot;
    
    // Hover state
    private Equipment.Slot hoveredSlot = null;
    
    // Event handlers
    private Runnable onGearChanged;
    
    public GearSheet(PlayerSprite player) {
        super("⚔ Equipment", 280, 380);
        this.player = player;
        
        createContent();
    }
    
    private void createContent() {
        VBox content = getContentArea();
        content.setAlignment(Pos.TOP_CENTER);
        content.setSpacing(10);
        
        // Main layout - sprite in center, slots around
        GridPane gearGrid = new GridPane();
        gearGrid.setAlignment(Pos.CENTER);
        gearGrid.setHgap(8);
        gearGrid.setVgap(8);
        gearGrid.setPadding(new Insets(5));
        
        // Create slots
        headSlot = createSlot("Head", Equipment.Slot.HEAD);
        bodySlot = createSlot("Body", Equipment.Slot.BODY);
        legsSlot = createSlot("Legs", Equipment.Slot.LEGS);
        feetSlot = createSlot("Feet", Equipment.Slot.FEET);
        mainHandSlot = createSlot("Weapon", Equipment.Slot.MAIN_HAND);
        offHandSlot = createSlot("Off-hand", Equipment.Slot.OFF_HAND);
        capeSlot = createSlot("Cape", Equipment.Slot.CAPE);
        
        // Sprite preview canvas (larger)
        spriteCanvas = new Canvas(80, 120);
        renderSprite();
        
        // Container for sprite
        VBox spriteBox = new VBox(5);
        spriteBox.setAlignment(Pos.CENTER);
        spriteBox.getChildren().add(spriteCanvas);
        
        // Layout:
        //       [Head]
        // [Off] [Body] [Main]
        //       [Legs]
        //       [Feet]
        // [Cape]
        
        // Row 0: Head
        gearGrid.add(headSlot, 1, 0);
        
        // Row 1: Off-hand, Body/Sprite, Main-hand
        gearGrid.add(offHandSlot, 0, 1);
        gearGrid.add(spriteBox, 1, 1, 1, 2);
        gearGrid.add(mainHandSlot, 2, 1);
        
        // Row 2: empty, (sprite continues), cape
        gearGrid.add(capeSlot, 2, 2);
        
        // Row 3: Legs
        gearGrid.add(legsSlot, 1, 3);
        
        // Row 4: Feet
        gearGrid.add(feetSlot, 1, 4);
        
        content.getChildren().add(gearGrid);
        
        // Stats summary
        content.getChildren().add(createStatsBar());
    }
    
    private Canvas createSlot(String label, Equipment.Slot slot) {
        Canvas canvas = new Canvas(SLOT_SIZE, SLOT_SIZE);
        renderSlot(canvas, slot, false);
        
        // Tooltip
        Tooltip tooltip = new Tooltip(label);
        Tooltip.install(canvas, tooltip);
        
        // Mouse handlers
        canvas.setOnMouseEntered(e -> {
            hoveredSlot = slot;
            renderSlot(canvas, slot, true);
            updateTooltip(tooltip, slot);
        });
        
        canvas.setOnMouseExited(e -> {
            hoveredSlot = null;
            renderSlot(canvas, slot, false);
        });
        
        canvas.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Right-click to unequip
                Equipment removed = player.unequip(slot);
                if (removed != null) {
                    // Try to add to inventory
                    // Note: Would need to convert Equipment to ItemStack
                    renderSlot(canvas, slot, true);
                    renderSprite();
                    if (onGearChanged != null) onGearChanged.run();
                }
            }
        });
        
        return canvas;
    }
    
    private void renderSlot(Canvas canvas, Equipment.Slot slot, boolean hovered) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        
        // Background
        gc.setFill(Color.web(hovered ? SLOT_HOVER : SLOT_BG));
        gc.fillRoundRect(0, 0, w, h, 6, 6);
        
        // Border
        gc.setStroke(Color.web(ACCENT_COLOR, hovered ? 0.8 : 0.4));
        gc.setLineWidth(hovered ? 2 : 1);
        gc.strokeRoundRect(1, 1, w - 2, h - 2, 5, 5);
        
        // Get equipped item
        Equipment equipped = player.getEquipment(slot);
        
        if (equipped != null) {
            // Render the equipped item
            renderEquipmentIcon(gc, equipped, 4, 4, w - 8, h - 8);
        } else {
            // Empty slot indicator
            gc.setFill(Color.web(TEXT_COLOR, 0.2));
            gc.setFont(Font.font("System", 20));
            String icon = getSlotIcon(slot);
            double textWidth = icon.length() * 10;
            gc.fillText(icon, (w - textWidth) / 2 + 5, h / 2 + 7);
        }
    }
    
    private void renderEquipmentIcon(GraphicsContext gc, Equipment item, double x, double y, double w, double h) {
        // Use the equipment's visual properties
        Color primary = item.getPrimaryColor();
        Color secondary = item.getSecondaryColor();
        
        // Draw a simplified icon based on slot
        gc.setFill(primary);
        
        switch (item.getSlot()) {
            case HEAD:
                // Helmet shape
                gc.fillArc(x, y + h * 0.2, w, h * 0.7, 0, 180, javafx.scene.shape.ArcType.ROUND);
                break;
            case BODY:
                // Armor shape
                gc.fillRoundRect(x + w * 0.15, y + h * 0.1, w * 0.7, h * 0.8, w * 0.2, w * 0.2);
                break;
            case LEGS:
                // Pants shape
                gc.fillRoundRect(x + w * 0.1, y, w * 0.35, h * 0.9, w * 0.1, w * 0.1);
                gc.fillRoundRect(x + w * 0.55, y, w * 0.35, h * 0.9, w * 0.1, w * 0.1);
                break;
            case FEET:
                // Boot shape
                gc.fillRoundRect(x + w * 0.1, y + h * 0.3, w * 0.8, h * 0.6, w * 0.2, w * 0.2);
                break;
            case MAIN_HAND:
                // Sword shape
                gc.fillRect(x + w * 0.4, y, w * 0.2, h * 0.7);
                gc.setFill(secondary);
                gc.fillRect(x + w * 0.2, y + h * 0.7, w * 0.6, h * 0.1);
                break;
            case OFF_HAND:
                // Shield shape
                gc.fillOval(x + w * 0.1, y + h * 0.1, w * 0.8, h * 0.8);
                break;
            case CAPE:
                // Cape shape
                gc.fillPolygon(
                    new double[]{x + w * 0.2, x + w * 0.5, x + w * 0.8},
                    new double[]{y, y + h, y}, 3);
                break;
        }
        
        gc.setStroke(Color.web("#2a1a10"));
        gc.setLineWidth(1);
    }
    
    private String getSlotIcon(Equipment.Slot slot) {
        switch (slot) {
            case HEAD: return "👒";
            case BODY: return "👕";
            case LEGS: return "👖";
            case FEET: return "👟";
            case MAIN_HAND: return "⚔";
            case OFF_HAND: return "🛡";
            case CAPE: return "🧥";
            default: return "?";
        }
    }
    
    private void updateTooltip(Tooltip tooltip, Equipment.Slot slot) {
        Equipment equipped = player.getEquipment(slot);
        if (equipped != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(equipped.getName()).append("\n");
            if (equipped.getDefense() > 0) sb.append("Defense: +").append(equipped.getDefense()).append("\n");
            if (equipped.getAttack() > 0) sb.append("Attack: +").append(equipped.getAttack()).append("\n");
            sb.append("Value: ").append(equipped.getValue()).append("g\n");
            sb.append("\nRight-click to unequip");
            tooltip.setText(sb.toString());
        } else {
            tooltip.setText(slot.name() + " (Empty)\nDrag item here to equip");
        }
    }
    
    private void renderSprite() {
        GraphicsContext gc = spriteCanvas.getGraphicsContext2D();
        double w = spriteCanvas.getWidth();
        double h = spriteCanvas.getHeight();
        
        // Clear
        gc.clearRect(0, 0, w, h);
        
        // Background
        gc.setFill(Color.web("#1a1510"));
        gc.fillRoundRect(0, 0, w, h, 8, 8);
        
        // Render player at center
        player.render(gc, (w - 60) / 2, (h - 90) / 2, 60);
    }
    
    private HBox createStatsBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(8, 5, 5, 5));
        bar.setStyle("-fx-background-color: #1a1510; -fx-background-radius: 5;");
        
        // Calculate totals
        int totalDefense = 0;
        int totalAttack = 0;
        
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            Equipment item = player.getEquipment(slot);
            if (item != null) {
                totalDefense += item.getDefense();
                totalAttack += item.getAttack();
            }
        }
        
        Label defLabel = new Label("🛡 " + totalDefense);
        defLabel.setTextFill(Color.web("#70a0d0"));
        defLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label atkLabel = new Label("⚔ " + totalAttack);
        atkLabel.setTextFill(Color.web("#d07070"));
        atkLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        bar.getChildren().addAll(defLabel, atkLabel);
        
        return bar;
    }
    
    /**
     * Refresh all slot displays
     */
    public void refresh() {
        renderSlot(headSlot, Equipment.Slot.HEAD, hoveredSlot == Equipment.Slot.HEAD);
        renderSlot(bodySlot, Equipment.Slot.BODY, hoveredSlot == Equipment.Slot.BODY);
        renderSlot(legsSlot, Equipment.Slot.LEGS, hoveredSlot == Equipment.Slot.LEGS);
        renderSlot(feetSlot, Equipment.Slot.FEET, hoveredSlot == Equipment.Slot.FEET);
        renderSlot(mainHandSlot, Equipment.Slot.MAIN_HAND, hoveredSlot == Equipment.Slot.MAIN_HAND);
        renderSlot(offHandSlot, Equipment.Slot.OFF_HAND, hoveredSlot == Equipment.Slot.OFF_HAND);
        renderSlot(capeSlot, Equipment.Slot.CAPE, hoveredSlot == Equipment.Slot.CAPE);
        renderSprite();
        
        // Rebuild stats bar
        VBox content = getContentArea();
        if (content.getChildren().size() > 1) {
            content.getChildren().remove(content.getChildren().size() - 1);
        }
        content.getChildren().add(createStatsBar());
    }
    
    public void setOnGearChanged(Runnable handler) {
        this.onGearChanged = handler;
    }
}
