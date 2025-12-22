import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop interface for buying and selling items.
 * Accepts both items (at their value) and currency.
 */
public class ShopUI extends StackPane {
    
    // Style constants
    private static final String DARK_BG = "#1a1a1a";
    private static final String MEDIUM_BG = "#2a2a2a";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String TEXT_COLOR = "#e0e0e0";
    private static final String GOLD_COLOR = "#ffd700";
    private static final String SUCCESS_COLOR = "#44ff44";
    private static final String ERROR_COLOR = "#ff4444";
    
    // Shop data
    private String shopName;
    private String shopkeeperName;
    private List<ShopItem> shopInventory;
    private double buyMultiplier = 1.0;   // Price multiplier for buying
    private double sellMultiplier = 0.5;  // Price multiplier for selling (50% value)
    
    // Player references
    private Inventory playerInventory;
    private Currency playerCurrency;
    
    // UI components
    private VBox shopListBox;
    private VBox playerListBox;
    private Label playerGoldLabel;
    private Label statusLabel;
    private InventoryUI playerInvUI;
    
    // Selected item for transaction
    private ShopItem selectedShopItem;
    private int selectedPlayerSlot = -1;
    
    // Callbacks
    private Runnable onClose;
    
    public ShopUI(String shopName, String shopkeeperName, Inventory playerInventory, Currency playerCurrency) {
        this.shopName = shopName;
        this.shopkeeperName = shopkeeperName;
        this.playerInventory = playerInventory;
        this.playerCurrency = playerCurrency;
        this.shopInventory = new ArrayList<>();
        
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        setAlignment(Pos.CENTER);
        
        createUI();
        
        // Close on click outside
        setOnMouseClicked(e -> {
            if (e.getTarget() == this && onClose != null) {
                onClose.run();
            }
        });
    }
    
    private void createUI() {
        VBox mainPanel = new VBox(15);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setMaxWidth(800);
        mainPanel.setMaxHeight(650);
        mainPanel.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + GOLD_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;"
        );
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setRadius(30);
        mainPanel.setEffect(shadow);
        
        // Header
        HBox header = createHeader();
        
        // Shopkeeper greeting
        Label greeting = new Label("\"Welcome to my shop, traveler! What catches your eye?\"");
        greeting.setFont(Font.font("Georgia", FontWeight.NORMAL, 14));
        greeting.setTextFill(Color.web(TEXT_COLOR, 0.8));
        greeting.setStyle("-fx-font-style: italic;");
        
        // Main content: Shop inventory | Player inventory
        HBox contentArea = createContentArea();
        
        // Transaction bar
        HBox transactionBar = createTransactionBar();
        
        // Status/message label
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setTextFill(Color.web(TEXT_COLOR));
        
        mainPanel.getChildren().addAll(header, greeting, contentArea, transactionBar, statusLabel);
        getChildren().add(mainPanel);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🏪 " + shopName);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(GOLD_COLOR));
        
        Label keeper = new Label("Shopkeeper: " + shopkeeperName);
        keeper.setFont(Font.font("System", 12));
        keeper.setTextFill(Color.web(TEXT_COLOR, 0.7));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Player currency display
        HBox currencyBox = createCurrencyDisplay();
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-font-size: 16;" +
            "-fx-padding: 5 10 5 10;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> { if (onClose != null) onClose.run(); });
        
        VBox titleBox = new VBox(2);
        titleBox.getChildren().addAll(title, keeper);
        
        header.getChildren().addAll(titleBox, spacer, currencyBox, closeBtn);
        return header;
    }
    
    private HBox createCurrencyDisplay() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        box.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        // Coin displays
        HBox goldBox = createCoinLabel(playerCurrency.getGold(), "gold");
        HBox silverBox = createCoinLabel(playerCurrency.getSilver(), "silver");
        HBox copperBox = createCoinLabel(playerCurrency.getCopper(), "copper");
        
        playerGoldLabel = new Label(playerCurrency.toShortString());
        playerGoldLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        playerGoldLabel.setTextFill(Color.web(GOLD_COLOR));
        playerGoldLabel.setVisible(false); // Hidden, using individual coins
        
        box.getChildren().addAll(goldBox, silverBox, copperBox);
        return box;
    }
    
    private HBox createCoinLabel(int amount, String type) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER);
        
        Canvas coin = new Canvas(16, 16);
        GraphicsContext gc = coin.getGraphicsContext2D();
        
        switch (type) {
            case "gold":
                Currency.renderGoldCoin(gc, 0, 0, 16);
                break;
            case "silver":
                Currency.renderSilverCoin(gc, 0, 0, 16);
                break;
            default:
                Currency.renderCopperCoin(gc, 0, 0, 16);
                break;
        }
        
        Label label = new Label(String.valueOf(amount));
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setTextFill(Color.web(TEXT_COLOR));
        
        box.getChildren().addAll(coin, label);
        return box;
    }
    
    private HBox createContentArea() {
        HBox area = new HBox(15);
        area.setAlignment(Pos.TOP_CENTER);
        
        // Shop inventory panel
        VBox shopPanel = createShopPanel();
        
        // Divider
        Region divider = new Region();
        divider.setPrefWidth(2);
        divider.setStyle("-fx-background-color: " + LIGHT_BG + ";");
        
        // Player inventory panel
        VBox playerPanel = createPlayerPanel();
        
        HBox.setHgrow(shopPanel, Priority.ALWAYS);
        HBox.setHgrow(playerPanel, Priority.ALWAYS);
        
        area.getChildren().addAll(shopPanel, divider, playerPanel);
        return area;
    }
    
    private VBox createShopPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 8;"
        );
        
        Label title = new Label("For Sale");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(ACCENT_COLOR));
        
        shopListBox = new VBox(5);
        shopListBox.setPadding(new Insets(5));
        
        ScrollPane scrollPane = new ScrollPane(shopListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.setStyle(
            "-fx-background: " + DARK_BG + ";" +
            "-fx-background-color: " + DARK_BG + ";"
        );
        
        panel.getChildren().addAll(title, scrollPane);
        return panel;
    }
    
    private VBox createPlayerPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 8;"
        );
        
        Label title = new Label("Your Items (Click to sell)");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(ACCENT_COLOR));
        
        playerListBox = new VBox(5);
        playerListBox.setPadding(new Insets(5));
        
        ScrollPane scrollPane = new ScrollPane(playerListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.setStyle(
            "-fx-background: " + DARK_BG + ";" +
            "-fx-background-color: " + DARK_BG + ";"
        );
        
        panel.getChildren().addAll(title, scrollPane);
        
        refreshPlayerItems();
        return panel;
    }
    
    private HBox createTransactionBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10));
        bar.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        Button buyBtn = new Button("Buy Selected");
        buyBtn.setStyle(
            "-fx-background-color: " + SUCCESS_COLOR + ";" +
            "-fx-text-fill: " + DARK_BG + ";" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-cursor: hand;"
        );
        buyBtn.setOnAction(e -> performBuy());
        
        Button sellBtn = new Button("Sell Selected");
        sellBtn.setStyle(
            "-fx-background-color: " + GOLD_COLOR + ";" +
            "-fx-text-fill: " + DARK_BG + ";" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-cursor: hand;"
        );
        sellBtn.setOnAction(e -> performSell());
        
        Button sellAllBtn = new Button("Sell All Misc");
        sellAllBtn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 10 15 10 15;" +
            "-fx-cursor: hand;"
        );
        sellAllBtn.setOnAction(e -> sellAllMisc());
        
        bar.getChildren().addAll(buyBtn, sellBtn, sellAllBtn);
        return bar;
    }
    
    // === Shop Operations ===
    
    public void addShopItem(String itemId, int quantity, double priceMultiplier) {
        Item item = ItemRegistry.get(itemId);
        if (item == null) return;
        
        long price = (long) (item.getBaseValue() * buyMultiplier * priceMultiplier);
        shopInventory.add(new ShopItem(item, quantity, price));
        refreshShopItems();
    }
    
    public void addShopItem(String itemId, int quantity) {
        addShopItem(itemId, quantity, 1.0);
    }
    
    private void refreshShopItems() {
        shopListBox.getChildren().clear();
        
        for (ShopItem shopItem : shopInventory) {
            HBox row = createShopItemRow(shopItem);
            shopListBox.getChildren().add(row);
        }
    }
    
    private void refreshPlayerItems() {
        playerListBox.getChildren().clear();
        
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack stack = playerInventory.getSlot(i);
            if (stack != null && !stack.isEmpty()) {
                HBox row = createPlayerItemRow(stack, i);
                playerListBox.getChildren().add(row);
            }
        }
        
        if (playerListBox.getChildren().isEmpty()) {
            Label empty = new Label("No items to sell");
            empty.setTextFill(Color.web(TEXT_COLOR, 0.5));
            playerListBox.getChildren().add(empty);
        }
    }
    
    private HBox createShopItemRow(ShopItem shopItem) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(8));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        
        // Item icon
        Canvas icon = new Canvas(32, 32);
        shopItem.item.renderIcon(icon.getGraphicsContext2D(), 0, 0, 32);
        
        // Item info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label name = new Label(shopItem.item.getName());
        name.setFont(Font.font("System", FontWeight.BOLD, 12));
        name.setTextFill(shopItem.item.getRarity().getColor());
        
        Label stock = new Label("Stock: " + (shopItem.quantity == -1 ? "∞" : shopItem.quantity));
        stock.setFont(Font.font("System", 10));
        stock.setTextFill(Color.web(TEXT_COLOR, 0.7));
        
        info.getChildren().addAll(name, stock);
        
        // Price
        Label price = new Label(Currency.fromCopper(shopItem.price).toShortString());
        price.setFont(Font.font("System", FontWeight.BOLD, 12));
        price.setTextFill(Color.web(GOLD_COLOR));
        
        // Can afford indicator
        boolean canAfford = playerCurrency.canAfford(shopItem.price);
        if (!canAfford) {
            price.setTextFill(Color.web(ERROR_COLOR));
        }
        
        row.getChildren().addAll(icon, info, price);
        
        // Click to select
        row.setOnMouseClicked(e -> {
            selectedShopItem = shopItem;
            selectedPlayerSlot = -1;
            highlightSelectedShopItem();
            setStatus("Selected: " + shopItem.item.getName() + " - " + Currency.fromCopper(shopItem.price).toShortString(), false);
        });
        
        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle(row.getStyle().replace(LIGHT_BG, "#454560")));
        row.setOnMouseExited(e -> row.setStyle(row.getStyle().replace("#454560", LIGHT_BG)));
        
        // Tooltip
        Tooltip.install(row, new Tooltip(shopItem.item.getTooltip()));
        
        return row;
    }
    
    private HBox createPlayerItemRow(ItemStack stack, int slotIndex) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(8));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        
        // Item icon
        Canvas icon = new Canvas(32, 32);
        stack.getItem().renderIcon(icon.getGraphicsContext2D(), 0, 0, 32);
        
        // Item info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        Label name = new Label(stack.getItem().getName() + (stack.getQuantity() > 1 ? " x" + stack.getQuantity() : ""));
        name.setFont(Font.font("System", FontWeight.BOLD, 12));
        name.setTextFill(stack.getItem().getRarity().getColor());
        
        info.getChildren().add(name);
        
        // Sell value
        long sellValue = (long) (stack.getItem().getBaseValue() * sellMultiplier);
        Label price = new Label("Sell: " + Currency.fromCopper(sellValue).toShortString());
        price.setFont(Font.font("System", 11));
        price.setTextFill(Color.web(GOLD_COLOR, 0.8));
        
        row.getChildren().addAll(icon, info, price);
        
        // Click to select
        row.setOnMouseClicked(e -> {
            selectedPlayerSlot = slotIndex;
            selectedShopItem = null;
            highlightSelectedPlayerItem();
            setStatus("Sell " + stack.getItem().getName() + " for " + Currency.fromCopper(sellValue).toShortString(), false);
        });
        
        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle(row.getStyle().replace(LIGHT_BG, "#454560")));
        row.setOnMouseExited(e -> row.setStyle(row.getStyle().replace("#454560", LIGHT_BG)));
        
        // Tooltip
        Tooltip.install(row, new Tooltip(stack.getItem().getTooltip()));
        
        return row;
    }
    
    private void highlightSelectedShopItem() {
        // Simple visual feedback - could be expanded
    }
    
    private void highlightSelectedPlayerItem() {
        // Simple visual feedback - could be expanded  
    }
    
    private void performBuy() {
        if (selectedShopItem == null) {
            setStatus("Select an item to buy!", true);
            return;
        }
        
        if (!playerCurrency.canAfford(selectedShopItem.price)) {
            setStatus("You can't afford that!", true);
            return;
        }
        
        if (playerInventory.isFull()) {
            setStatus("Your inventory is full!", true);
            return;
        }
        
        if (selectedShopItem.quantity == 0) {
            setStatus("Out of stock!", true);
            return;
        }
        
        // Perform transaction
        playerCurrency.subtract(selectedShopItem.price);
        playerInventory.addItem(selectedShopItem.item.getId(), 1);
        
        if (selectedShopItem.quantity > 0) {
            selectedShopItem.quantity--;
        }
        
        setStatus("Purchased " + selectedShopItem.item.getName() + "!", false);
        
        refreshShopItems();
        refreshPlayerItems();
        updateCurrencyDisplay();
        
        playPurchaseAnimation();
    }
    
    private void performSell() {
        if (selectedPlayerSlot == -1) {
            setStatus("Select an item to sell!", true);
            return;
        }
        
        ItemStack stack = playerInventory.getSlot(selectedPlayerSlot);
        if (stack == null || stack.isEmpty()) {
            setStatus("No item selected!", true);
            return;
        }
        
        long sellValue = (long) (stack.getItem().getBaseValue() * sellMultiplier);
        String itemName = stack.getItem().getName();
        
        // Sell one item
        playerInventory.removeFromSlot(selectedPlayerSlot, 1);
        playerCurrency.add(sellValue);
        
        setStatus("Sold " + itemName + " for " + Currency.fromCopper(sellValue).toShortString(), false);
        
        selectedPlayerSlot = -1;
        refreshPlayerItems();
        updateCurrencyDisplay();
        
        playSellAnimation();
    }
    
    private void sellAllMisc() {
        long totalValue = 0;
        int itemsSold = 0;
        
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack stack = playerInventory.getSlot(i);
            if (stack != null && !stack.isEmpty() && 
                stack.getItem().getCategory() == Item.Category.MISC) {
                long value = (long) (stack.getTotalValue() * sellMultiplier);
                totalValue += value;
                itemsSold += stack.getQuantity();
                playerInventory.clearSlot(i);
            }
        }
        
        if (itemsSold > 0) {
            playerCurrency.add(totalValue);
            setStatus("Sold " + itemsSold + " items for " + Currency.fromCopper(totalValue).toShortString(), false);
            refreshPlayerItems();
            updateCurrencyDisplay();
            playSellAnimation();
        } else {
            setStatus("No misc items to sell!", true);
        }
    }
    
    private void updateCurrencyDisplay() {
        // Rebuild currency display
        // This is a simple approach - could be optimized
        getChildren().clear();
        createUI();
        refreshShopItems();
        refreshPlayerItems();
    }
    
    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.web(ERROR_COLOR) : Color.web(SUCCESS_COLOR));
        
        // Fade out after delay
        FadeTransition fade = new FadeTransition(Duration.millis(3000), statusLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.5);
        fade.play();
    }
    
    private void playPurchaseAnimation() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), this);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.01);
        scale.setToY(1.01);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }
    
    private void playSellAnimation() {
        // Simple feedback animation
        FadeTransition fade = new FadeTransition(Duration.millis(50), this);
        fade.setFromValue(1.0);
        fade.setToValue(0.95);
        fade.setAutoReverse(true);
        fade.setCycleCount(2);
        fade.play();
    }
    
    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }
    
    public void setBuyMultiplier(double multiplier) {
        this.buyMultiplier = multiplier;
    }
    
    public void setSellMultiplier(double multiplier) {
        this.sellMultiplier = multiplier;
    }
    
    // Inner class for shop items
    public static class ShopItem {
        Item item;
        int quantity;  // -1 for unlimited
        long price;    // Price in copper
        
        public ShopItem(Item item, int quantity, long price) {
            this.item = item;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
