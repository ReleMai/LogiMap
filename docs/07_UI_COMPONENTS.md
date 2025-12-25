# LogiMap Developer Guide - UI Components

## Table of Contents
1. [UI Architecture](#ui-architecture)
2. [Floating Panels](#floating-panels)
3. [Draggable Windows](#draggable-windows)
4. [Common UI Patterns](#common-ui-patterns)
5. [Styling](#styling)
6. [Code Examples](#code-examples)

---

## UI Architecture

### Layer Structure
```
┌─────────────────────────────────────────┐
│              Dialog Layer               │ ← Modal dialogs
├─────────────────────────────────────────┤
│              Window Layer               │ ← Draggable windows
├─────────────────────────────────────────┤
│              HUD Layer                  │ ← Always-visible UI
├─────────────────────────────────────────┤
│              Tooltip Layer              │ ← Hover information
├─────────────────────────────────────────┤
│              Game Canvas                │ ← Main game rendering
└─────────────────────────────────────────┘
```

### Main UI Container (LogiMapUI)
```java
public class LogiMapUI extends Application {
    private StackPane root;        // Root container
    private BorderPane gamePane;   // Game layout
    private MapCanvas mapCanvas;   // Game rendering
    private Pane uiOverlay;        // UI elements over game
    
    @Override
    public void start(Stage stage) {
        root = new StackPane();
        gamePane = new BorderPane();
        uiOverlay = new Pane();
        
        // Layer setup
        root.getChildren().addAll(gamePane, uiOverlay);
        
        // Game canvas in center
        mapCanvas = new MapCanvas(world, 1200, 800);
        gamePane.setCenter(mapCanvas.getCanvas());
        
        // HUD elements
        setupHUD();
        
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);
        stage.show();
    }
    
    private void setupHUD() {
        // Top bar (time, resources)
        HBox topBar = createTopBar();
        gamePane.setTop(topBar);
        
        // Bottom bar (action buttons, news ticker)
        HBox bottomBar = createBottomBar();
        gamePane.setBottom(bottomBar);
        
        // Right panel (minimap)
        VBox rightPanel = createRightPanel();
        gamePane.setRight(rightPanel);
    }
}
```

---

## Floating Panels

### FloatingPanel Class
```java
public class FloatingPanel extends VBox {
    private HBox titleBar;
    private Label titleLabel;
    private Button closeButton;
    private VBox content;
    
    // Dragging state
    private double dragStartX, dragStartY;
    private double panelStartX, panelStartY;
    
    public FloatingPanel(String title) {
        // Styling
        getStyleClass().add("floating-panel");
        
        // Title bar
        titleBar = new HBox(10);
        titleBar.getStyleClass().add("panel-title-bar");
        
        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("panel-title");
        
        closeButton = new Button("×");
        closeButton.getStyleClass().add("panel-close-btn");
        closeButton.setOnAction(e -> hide());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        titleBar.getChildren().addAll(titleLabel, spacer, closeButton);
        
        // Content area
        content = new VBox(10);
        content.getStyleClass().add("panel-content");
        content.setPadding(new Insets(10));
        
        getChildren().addAll(titleBar, content);
        
        // Make draggable
        setupDragging();
    }
    
    private void setupDragging() {
        titleBar.setOnMousePressed(e -> {
            dragStartX = e.getScreenX();
            dragStartY = e.getScreenY();
            panelStartX = getLayoutX();
            panelStartY = getLayoutY();
        });
        
        titleBar.setOnMouseDragged(e -> {
            double deltaX = e.getScreenX() - dragStartX;
            double deltaY = e.getScreenY() - dragStartY;
            
            setLayoutX(panelStartX + deltaX);
            setLayoutY(panelStartY + deltaY);
        });
    }
    
    public void addContent(Node node) {
        content.getChildren().add(node);
    }
    
    public void show() {
        setVisible(true);
    }
    
    public void hide() {
        setVisible(false);
    }
}
```

### Usage Example
```java
// Create inventory panel
FloatingPanel inventoryPanel = new FloatingPanel("Inventory");
inventoryPanel.addContent(createInventoryGrid());
inventoryPanel.setLayoutX(50);
inventoryPanel.setLayoutY(100);
uiOverlay.getChildren().add(inventoryPanel);

// Show/hide with key
scene.setOnKeyPressed(e -> {
    if (e.getCode() == KeyCode.I) {
        inventoryPanel.setVisible(!inventoryPanel.isVisible());
    }
});
```

---

## Draggable Windows

### DraggableWindow Class
```java
public class DraggableWindow extends VBox {
    private HBox titleBar;
    private VBox content;
    private boolean isMinimized = false;
    
    // Resize handles
    private boolean isResizable = true;
    private double minWidth = 200;
    private double minHeight = 150;
    
    public DraggableWindow(String title, double width, double height) {
        setPrefSize(width, height);
        setMinSize(minWidth, minHeight);
        getStyleClass().add("draggable-window");
        
        // Title bar with controls
        titleBar = createTitleBar(title);
        
        // Content area
        content = new VBox();
        content.getStyleClass().add("window-content");
        VBox.setVgrow(content, Priority.ALWAYS);
        
        getChildren().addAll(titleBar, content);
        
        setupInteraction();
    }
    
    private HBox createTitleBar(String title) {
        HBox bar = new HBox(5);
        bar.getStyleClass().add("window-title-bar");
        bar.setPadding(new Insets(5, 10, 5, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("window-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Window controls
        Button minimizeBtn = new Button("─");
        minimizeBtn.getStyleClass().add("window-btn");
        minimizeBtn.setOnAction(e -> toggleMinimize());
        
        Button closeBtn = new Button("×");
        closeBtn.getStyleClass().add("window-btn", "close-btn");
        closeBtn.setOnAction(e -> close());
        
        bar.getChildren().addAll(titleLabel, spacer, minimizeBtn, closeBtn);
        return bar;
    }
    
    private void toggleMinimize() {
        isMinimized = !isMinimized;
        content.setVisible(!isMinimized);
        content.setManaged(!isMinimized);
        
        if (isMinimized) {
            setPrefHeight(titleBar.getHeight() + 10);
        } else {
            setPrefHeight(Region.USE_COMPUTED_SIZE);
        }
    }
    
    private void setupInteraction() {
        // Dragging
        titleBar.setOnMousePressed(this::onDragStart);
        titleBar.setOnMouseDragged(this::onDrag);
        
        // Resizing (corner handle)
        if (isResizable) {
            setupResizing();
        }
        
        // Bring to front on click
        setOnMouseClicked(e -> toFront());
    }
    
    private double dragOffsetX, dragOffsetY;
    
    private void onDragStart(MouseEvent e) {
        dragOffsetX = e.getSceneX() - getLayoutX();
        dragOffsetY = e.getSceneY() - getLayoutY();
        toFront();
    }
    
    private void onDrag(MouseEvent e) {
        setLayoutX(e.getSceneX() - dragOffsetX);
        setLayoutY(e.getSceneY() - dragOffsetY);
    }
}
```

### Window Manager
```java
public class WindowManager {
    private Pane container;
    private List<DraggableWindow> windows;
    private DraggableWindow focusedWindow;
    
    public WindowManager(Pane container) {
        this.container = container;
        this.windows = new ArrayList<>();
    }
    
    public DraggableWindow createWindow(String title, double width, double height) {
        DraggableWindow window = new DraggableWindow(title, width, height);
        window.setOnMouseClicked(e -> focusWindow(window));
        
        windows.add(window);
        container.getChildren().add(window);
        
        // Center new window
        window.setLayoutX((container.getWidth() - width) / 2);
        window.setLayoutY((container.getHeight() - height) / 2);
        
        focusWindow(window);
        return window;
    }
    
    public void focusWindow(DraggableWindow window) {
        if (focusedWindow != null) {
            focusedWindow.getStyleClass().remove("focused");
        }
        
        focusedWindow = window;
        window.getStyleClass().add("focused");
        window.toFront();
    }
    
    public void closeWindow(DraggableWindow window) {
        windows.remove(window);
        container.getChildren().remove(window);
        
        if (focusedWindow == window && !windows.isEmpty()) {
            focusWindow(windows.get(windows.size() - 1));
        }
    }
}
```

---

## Common UI Patterns

### Tooltip System
```java
public class GameTooltip extends VBox {
    private static GameTooltip instance;
    
    private Label titleLabel;
    private Label descriptionLabel;
    private VBox statsBox;
    
    private GameTooltip() {
        getStyleClass().add("game-tooltip");
        setVisible(false);
        setMouseTransparent(true);  // Don't block clicks
        
        titleLabel = new Label();
        titleLabel.getStyleClass().add("tooltip-title");
        
        descriptionLabel = new Label();
        descriptionLabel.getStyleClass().add("tooltip-desc");
        descriptionLabel.setWrapText(true);
        
        statsBox = new VBox(2);
        statsBox.getStyleClass().add("tooltip-stats");
        
        getChildren().addAll(titleLabel, descriptionLabel, statsBox);
    }
    
    public static GameTooltip getInstance() {
        if (instance == null) {
            instance = new GameTooltip();
        }
        return instance;
    }
    
    public void showForItem(Item item, double x, double y) {
        titleLabel.setText(item.getName());
        titleLabel.setStyle("-fx-text-fill: " + item.getRarityColor());
        
        descriptionLabel.setText(item.getDescription());
        
        statsBox.getChildren().clear();
        for (Map.Entry<String, Integer> stat : item.getStats().entrySet()) {
            Label statLabel = new Label("+" + stat.getValue() + " " + stat.getKey());
            statLabel.getStyleClass().add("tooltip-stat");
            statsBox.getChildren().add(statLabel);
        }
        
        setLayoutX(x + 15);
        setLayoutY(y + 15);
        setVisible(true);
    }
    
    public void hide() {
        setVisible(false);
    }
}

// Usage
itemSlot.setOnMouseEntered(e -> {
    if (item != null) {
        GameTooltip.getInstance().showForItem(item, e.getScreenX(), e.getScreenY());
    }
});

itemSlot.setOnMouseExited(e -> {
    GameTooltip.getInstance().hide();
});
```

### Confirmation Dialog
```java
public class ConfirmDialog extends VBox {
    private Consumer<Boolean> callback;
    
    public ConfirmDialog(String message, Consumer<Boolean> callback) {
        this.callback = callback;
        getStyleClass().add("confirm-dialog");
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        
        Button yesBtn = new Button("Yes");
        yesBtn.getStyleClass().add("btn-primary");
        yesBtn.setOnAction(e -> {
            callback.accept(true);
            close();
        });
        
        Button noBtn = new Button("No");
        noBtn.getStyleClass().add("btn-secondary");
        noBtn.setOnAction(e -> {
            callback.accept(false);
            close();
        });
        
        buttons.getChildren().addAll(yesBtn, noBtn);
        getChildren().addAll(messageLabel, buttons);
    }
    
    public void show(Pane parent) {
        // Darken background
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5)");
        overlay.prefWidthProperty().bind(parent.widthProperty());
        overlay.prefHeightProperty().bind(parent.heightProperty());
        
        // Center dialog
        setLayoutX((parent.getWidth() - getPrefWidth()) / 2);
        setLayoutY((parent.getHeight() - getPrefHeight()) / 2);
        
        parent.getChildren().addAll(overlay, this);
    }
    
    private void close() {
        getParent().getChildren().remove(this);
    }
}

// Usage
new ConfirmDialog("Are you sure you want to sell this item?", confirmed -> {
    if (confirmed) {
        sellItem(item);
    }
}).show(uiOverlay);
```

### Tab Manager
```java
public class TabManager extends VBox {
    private HBox tabBar;
    private StackPane contentArea;
    private List<Tab> tabs;
    private Tab activeTab;
    
    public TabManager() {
        tabs = new ArrayList<>();
        
        tabBar = new HBox();
        tabBar.getStyleClass().add("tab-bar");
        
        contentArea = new StackPane();
        contentArea.getStyleClass().add("tab-content");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        getChildren().addAll(tabBar, contentArea);
    }
    
    public void addTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tabs.add(tab);
        
        Button tabBtn = new Button(title);
        tabBtn.getStyleClass().add("tab-button");
        tabBtn.setOnAction(e -> selectTab(tab));
        tab.setButton(tabBtn);
        
        tabBar.getChildren().add(tabBtn);
        contentArea.getChildren().add(content);
        content.setVisible(false);
        
        if (activeTab == null) {
            selectTab(tab);
        }
    }
    
    public void selectTab(Tab tab) {
        if (activeTab != null) {
            activeTab.getButton().getStyleClass().remove("active");
            activeTab.getContent().setVisible(false);
        }
        
        activeTab = tab;
        activeTab.getButton().getStyleClass().add("active");
        activeTab.getContent().setVisible(true);
    }
    
    private static class Tab {
        private String title;
        private Node content;
        private Button button;
        
        public Tab(String title, Node content) {
            this.title = title;
            this.content = content;
        }
        
        // Getters and setters...
    }
}

// Usage
TabManager tabs = new TabManager();
tabs.addTab("Inventory", inventoryPanel);
tabs.addTab("Equipment", equipmentPanel);
tabs.addTab("Stats", statsPanel);
```

---

## Styling

### CSS Stylesheet (style.css)
```css
/* Base styling */
.root {
    -fx-font-family: "Segoe UI", Arial, sans-serif;
    -fx-font-size: 14px;
}

/* Floating panels */
.floating-panel {
    -fx-background-color: linear-gradient(to bottom, #2a2a3a, #1a1a2a);
    -fx-border-color: #4a9eff;
    -fx-border-width: 2px;
    -fx-border-radius: 5px;
    -fx-background-radius: 5px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);
}

.panel-title-bar {
    -fx-background-color: #3a3a4a;
    -fx-padding: 8px 12px;
    -fx-background-radius: 5px 5px 0 0;
}

.panel-title {
    -fx-text-fill: #e0e0e0;
    -fx-font-weight: bold;
    -fx-font-size: 16px;
}

.panel-close-btn {
    -fx-background-color: transparent;
    -fx-text-fill: #888;
    -fx-font-size: 18px;
    -fx-cursor: hand;
}

.panel-close-btn:hover {
    -fx-text-fill: #ff4444;
}

.panel-content {
    -fx-background-color: transparent;
}

/* Buttons */
.btn-primary {
    -fx-background-color: #4a9eff;
    -fx-text-fill: white;
    -fx-padding: 8px 16px;
    -fx-cursor: hand;
}

.btn-primary:hover {
    -fx-background-color: #6ab0ff;
}

.btn-secondary {
    -fx-background-color: #444;
    -fx-text-fill: #ccc;
    -fx-padding: 8px 16px;
}

.btn-secondary:hover {
    -fx-background-color: #555;
}

/* Tooltips */
.game-tooltip {
    -fx-background-color: rgba(20, 20, 30, 0.95);
    -fx-border-color: #4a9eff;
    -fx-border-width: 1px;
    -fx-padding: 10px;
    -fx-max-width: 250px;
}

.tooltip-title {
    -fx-font-weight: bold;
    -fx-font-size: 14px;
}

.tooltip-desc {
    -fx-text-fill: #aaa;
    -fx-font-size: 12px;
}

.tooltip-stat {
    -fx-text-fill: #88ff88;
    -fx-font-size: 11px;
}

/* Item rarity colors */
.rarity-common { -fx-text-fill: #ffffff; }
.rarity-uncommon { -fx-text-fill: #1eff00; }
.rarity-rare { -fx-text-fill: #0070dd; }
.rarity-epic { -fx-text-fill: #a335ee; }
.rarity-legendary { -fx-text-fill: #ff8000; }

/* Inventory slots */
.inventory-slot {
    -fx-background-color: #333;
    -fx-border-color: #555;
    -fx-border-width: 1px;
    -fx-min-width: 48px;
    -fx-min-height: 48px;
}

.inventory-slot:hover {
    -fx-border-color: #4a9eff;
}

.inventory-slot.selected {
    -fx-border-color: #ffd700;
    -fx-border-width: 2px;
}

/* Tab system */
.tab-bar {
    -fx-background-color: #2a2a3a;
    -fx-padding: 0;
}

.tab-button {
    -fx-background-color: transparent;
    -fx-text-fill: #888;
    -fx-padding: 10px 20px;
    -fx-background-radius: 0;
}

.tab-button:hover {
    -fx-background-color: #3a3a4a;
    -fx-text-fill: #ccc;
}

.tab-button.active {
    -fx-background-color: #1a1a2a;
    -fx-text-fill: #4a9eff;
    -fx-border-color: #4a9eff transparent transparent transparent;
    -fx-border-width: 2px 0 0 0;
}

/* Scrollbars */
.scroll-bar {
    -fx-background-color: #1a1a2a;
}

.scroll-bar .thumb {
    -fx-background-color: #4a4a5a;
    -fx-background-radius: 3px;
}

.scroll-bar .thumb:hover {
    -fx-background-color: #5a5a6a;
}
```

---

## Code Examples

### Complete Inventory UI
```java
public class InventoryUI extends FloatingPanel {
    private GridPane itemGrid;
    private Label goldLabel;
    private Label weightLabel;
    private Inventory inventory;
    private PlayerSprite player;
    
    private int selectedSlot = -1;
    private Item draggedItem = null;
    
    public InventoryUI(PlayerSprite player) {
        super("Inventory");
        this.player = player;
        this.inventory = player.getInventory();
        
        // Header with gold and weight
        HBox header = new HBox(20);
        goldLabel = new Label("Gold: 0");
        goldLabel.getStyleClass().add("gold-text");
        weightLabel = new Label("Weight: 0/100");
        header.getChildren().addAll(goldLabel, weightLabel);
        
        // Item grid
        itemGrid = new GridPane();
        itemGrid.setHgap(4);
        itemGrid.setVgap(4);
        
        createSlots();
        
        addContent(header);
        addContent(itemGrid);
        
        refresh();
    }
    
    private void createSlots() {
        int cols = 8;
        int rows = 5;
        
        for (int i = 0; i < cols * rows; i++) {
            final int slot = i;
            
            StackPane slotPane = new StackPane();
            slotPane.getStyleClass().add("inventory-slot");
            slotPane.setPrefSize(48, 48);
            
            // Click handling
            slotPane.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    selectSlot(slot);
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    showContextMenu(slot, e.getScreenX(), e.getScreenY());
                }
            });
            
            // Drag and drop
            slotPane.setOnDragDetected(e -> {
                if (inventory.getItem(slot) != null) {
                    draggedItem = inventory.getItem(slot);
                    // Start drag...
                }
            });
            
            slotPane.setOnDragDropped(e -> {
                if (draggedItem != null) {
                    inventory.swapSlots(selectedSlot, slot);
                    draggedItem = null;
                    refresh();
                }
            });
            
            // Tooltip
            slotPane.setOnMouseEntered(e -> {
                Item item = inventory.getItem(slot);
                if (item != null) {
                    GameTooltip.getInstance().showForItem(item, e.getScreenX(), e.getScreenY());
                }
            });
            
            slotPane.setOnMouseExited(e -> {
                GameTooltip.getInstance().hide();
            });
            
            itemGrid.add(slotPane, i % cols, i / cols);
        }
    }
    
    public void refresh() {
        // Update gold display
        goldLabel.setText("Gold: " + player.getCurrency().toString());
        
        // Update weight
        double weight = inventory.getTotalWeight();
        double maxWeight = 100 + player.getStats().getStat(CharacterStats.Stat.STR) * 5;
        weightLabel.setText(String.format("Weight: %.1f/%.0f", weight, maxWeight));
        
        // Update slots
        for (int i = 0; i < itemGrid.getChildren().size(); i++) {
            StackPane slot = (StackPane) itemGrid.getChildren().get(i);
            slot.getChildren().clear();
            
            Item item = inventory.getItem(i);
            if (item != null) {
                // Item icon
                ImageView icon = new ImageView(ItemRenderer.getIcon(item));
                icon.setFitWidth(40);
                icon.setFitHeight(40);
                
                slot.getChildren().add(icon);
                
                // Stack count
                if (item.getQuantity() > 1) {
                    Label countLabel = new Label(String.valueOf(item.getQuantity()));
                    countLabel.getStyleClass().add("stack-count");
                    StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);
                    slot.getChildren().add(countLabel);
                }
            }
            
            // Selection highlight
            if (i == selectedSlot) {
                slot.getStyleClass().add("selected");
            } else {
                slot.getStyleClass().remove("selected");
            }
        }
    }
    
    private void selectSlot(int slot) {
        selectedSlot = slot;
        refresh();
    }
    
    private void showContextMenu(int slot, double x, double y) {
        Item item = inventory.getItem(slot);
        if (item == null) return;
        
        ContextMenu menu = new ContextMenu();
        
        if (item.isEquippable()) {
            MenuItem equipItem = new MenuItem("Equip");
            equipItem.setOnAction(e -> equipItem(slot));
            menu.getItems().add(equipItem);
        }
        
        if (item.isConsumable()) {
            MenuItem useItem = new MenuItem("Use");
            useItem.setOnAction(e -> useItem(slot));
            menu.getItems().add(useItem);
        }
        
        MenuItem dropItem = new MenuItem("Drop");
        dropItem.setOnAction(e -> dropItem(slot));
        menu.getItems().add(dropItem);
        
        menu.show(this, x, y);
    }
}
```

---

## Tips for UI Development

### Responsiveness
```java
// Bind to parent size
panel.prefWidthProperty().bind(container.widthProperty().multiply(0.3));

// Use percentage-based layouts
AnchorPane.setLeftAnchor(leftPanel, 10.0);
AnchorPane.setTopAnchor(leftPanel, 10.0);
AnchorPane.setBottomAnchor(leftPanel, 10.0);
```

### Performance
```java
// Don't update UI every frame
private double uiUpdateTimer = 0;
private static final double UI_UPDATE_INTERVAL = 0.1;

public void update(double deltaTime) {
    uiUpdateTimer += deltaTime;
    if (uiUpdateTimer >= UI_UPDATE_INTERVAL) {
        uiUpdateTimer = 0;
        refreshUI();
    }
}
```

### Accessibility
```java
// Keyboard navigation
scene.setOnKeyPressed(e -> {
    switch (e.getCode()) {
        case I: toggleInventory(); break;
        case C: toggleCharacterSheet(); break;
        case M: toggleMap(); break;
        case ESCAPE: closeTopWindow(); break;
    }
});

// Focus management
panel.setFocusTraversable(true);
```

---

## Next: [08_ADDING_NEW_FEATURES.md](08_ADDING_NEW_FEATURES.md)
