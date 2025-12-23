import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.*;

/**
 * Marketplace UI for buying and selling goods at towns.
 * Medieval-themed trading interface with custom tabs and filters.
 */
public class MarketplaceUI extends StackPane {
    
    // Medieval theme colors
    private static final String BG_DARKEST = "#0d0906";
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String BG_LIGHTER = "#4a3a20";
    private static final String GOLD = "#c4a574";
    private static final String GOLD_BRIGHT = "#ddc090";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    private static final String BORDER_LIGHT = "#7a6a50";
    private static final String BUY_COLOR = "#2a5a2a";
    private static final String BUY_HOVER = "#3a7a3a";
    private static final String SELL_COLOR = "#5a3a2a";
    private static final String SELL_HOVER = "#7a4a3a";
    private static final String TAB_ACTIVE = "#4a3a20";
    private static final String TAB_INACTIVE = "#2a1f10";
    
    // Currency colors
    private static final Color GOLD_COLOR = Color.web("#ffd700");
    private static final Color SILVER_COLOR = Color.web("#c0c0c0");
    private static final Color COPPER_COLOR = Color.web("#b87333");
    
    // Item category filters
    private enum ItemFilter {
        ALL("All", "🏷"),
        GRAIN("Grain", "🌾"),
        MATERIAL("Materials", "🪨"),
        FOOD("Food", "🍞"),
        EQUIPMENT("Equipment", "⚔");
        
        private final String name;
        private final String icon;
        ItemFilter(String name, String icon) { this.name = name; this.icon = icon; }
        public String getName() { return name; }
        public String getIcon() { return icon; }
    }
    
    // Current state
    private Town currentTown;
    private PlayerSprite player;
    private EconomySystem economy;
    private boolean isBuyMode = true;
    private ItemFilter currentFilter = ItemFilter.ALL;
    
    // UI components
    private VBox mainPanel;
    private Label townNameLabel;
    private HBox currencyDisplay;
    private Label goldLabel, silverLabel, copperLabel;
    private VBox itemsContainer;
    private HBox mainTabBar;
    private HBox filterBar;
    private ScrollPane itemsScroll;
    
    // Callbacks
    private Runnable onClose;
    private Runnable onReturnToTown;
    
    public MarketplaceUI() {
        // Darkened background overlay
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.color(0, 0, 0, 0.7));
        overlay.widthProperty().bind(widthProperty());
        overlay.heightProperty().bind(heightProperty());
        overlay.setOnMouseClicked(e -> close());
        
        // Main panel
        mainPanel = createMainPanel();
        
        getChildren().addAll(overlay, mainPanel);
        setVisible(false);
        setAlignment(Pos.CENTER);
    }
    
    private VBox createMainPanel() {
        VBox panel = new VBox(0);
        panel.setMaxWidth(550);
        panel.setMaxHeight(620);
        panel.setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 4;" +
            "-fx-border-radius: 15;"
        );
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setColor(Color.color(0, 0, 0, 0.8));
        panel.setEffect(shadow);
        
        // Header
        VBox header = createHeader();
        
        // Currency bar
        HBox currencyBar = createCurrencyBar();
        
        // Custom tabs for Buy/Sell
        mainTabBar = createMainTabBar();
        
        // Filter subtabs
        filterBar = createFilterBar();
        
        // Items container with styled scroll
        itemsContainer = new VBox(8);
        itemsContainer.setPadding(new Insets(15));
        itemsContainer.setStyle("-fx-background-color: " + BG_MED + ";");
        
        itemsScroll = new ScrollPane(itemsContainer);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setPrefHeight(300);
        styleScrollPane(itemsScroll);
        VBox.setVgrow(itemsScroll, Priority.ALWAYS);
        
        // Footer with buttons
        HBox footer = createFooter();
        
        panel.getChildren().addAll(header, currencyBar, mainTabBar, filterBar, itemsScroll, footer);
        return panel;
    }
    
    private void styleScrollPane(ScrollPane scrollPane) {
        scrollPane.setStyle(
            "-fx-background: " + BG_MED + ";" +
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1 0 1 0;"
        );
        // Custom scrollbar styling via CSS
        scrollPane.getStylesheets().add("data:text/css," +
            ".scroll-pane .scroll-bar:vertical {" +
            "  -fx-background-color: " + BG_DARK + ";" +
            "  -fx-pref-width: 12px;" +
            "}" +
            ".scroll-pane .scroll-bar:vertical .thumb {" +
            "  -fx-background-color: " + BORDER + ";" +
            "  -fx-background-radius: 6px;" +
            "}" +
            ".scroll-pane .scroll-bar:vertical .thumb:hover {" +
            "  -fx-background-color: " + GOLD + ";" +
            "}" +
            ".scroll-pane .scroll-bar:vertical .increment-button," +
            ".scroll-pane .scroll-bar:vertical .decrement-button {" +
            "  -fx-background-color: " + BG_DARK + ";" +
            "  -fx-padding: 3;" +
            "}" +
            ".scroll-pane .scroll-bar:vertical .increment-arrow," +
            ".scroll-pane .scroll-bar:vertical .decrement-arrow {" +
            "  -fx-background-color: " + BORDER + ";" +
            "}" +
            ".scroll-pane .corner {" +
            "  -fx-background-color: " + BG_DARK + ";" +
            "}"
        );
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 20, 15, 20));
        header.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_MED + ");" +
            "-fx-background-radius: 12 12 0 0;"
        );
        
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER);
        
        Label leftScroll = new Label("⚜");
        leftScroll.setFont(Font.font("Georgia", 24));
        leftScroll.setTextFill(Color.web(GOLD));
        
        townNameLabel = new Label("Marketplace");
        townNameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        townNameLabel.setTextFill(Color.web(GOLD_BRIGHT));
        
        Label rightScroll = new Label("⚜");
        rightScroll.setFont(Font.font("Georgia", 24));
        rightScroll.setTextFill(Color.web(GOLD));
        
        titleRow.getChildren().addAll(leftScroll, townNameLabel, rightScroll);
        
        Rectangle divider = new Rectangle(200, 2);
        divider.setFill(Color.web(GOLD, 0.5));
        
        header.getChildren().addAll(titleRow, divider);
        return header;
    }
    
    private HBox createCurrencyBar() {
        HBox bar = new HBox(20);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle(
            "-fx-background-color: " + BG_DARKEST + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1 0 1 0;"
        );
        
        Label yourPurse = new Label("Your Purse:");
        yourPurse.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        yourPurse.setTextFill(Color.web(TEXT_DIM));
        
        HBox goldBox = createCoinDisplay(GOLD_COLOR, "0");
        goldLabel = (Label) goldBox.getChildren().get(1);
        
        HBox silverBox = createCoinDisplay(SILVER_COLOR, "0");
        silverLabel = (Label) silverBox.getChildren().get(1);
        
        HBox copperBox = createCoinDisplay(COPPER_COLOR, "0");
        copperLabel = (Label) copperBox.getChildren().get(1);
        
        bar.getChildren().addAll(yourPurse, goldBox, silverBox, copperBox);
        currencyDisplay = bar;
        return bar;
    }
    
    private HBox createCoinDisplay(Color color, String value) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER);
        
        Canvas coinCanvas = new Canvas(18, 18);
        drawCoin(coinCanvas.getGraphicsContext2D(), color, 9, 9, 8);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        valueLabel.setTextFill(color);
        valueLabel.setMinWidth(35);
        
        box.getChildren().addAll(coinCanvas, valueLabel);
        return box;
    }
    
    private void drawCoin(GraphicsContext gc, Color color, double cx, double cy, double radius) {
        gc.setFill(color.darker());
        gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        gc.setFill(color);
        gc.fillOval(cx - radius * 0.8, cy - radius * 0.8, radius * 1.6, radius * 1.6);
        gc.setFill(color.brighter());
        gc.fillOval(cx - radius * 0.4, cy - radius * 0.6, radius * 0.5, radius * 0.4);
    }
    
    private HBox createMainTabBar() {
        HBox bar = new HBox(0);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(0));
        bar.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        Button buyTab = createMainTab("🛒 Buy Goods", true);
        Button sellTab = createMainTab("💰 Sell Goods", false);
        
        buyTab.setOnAction(e -> {
            isBuyMode = true;
            updateMainTabStyles(buyTab, sellTab);
            refreshItems();
        });
        
        sellTab.setOnAction(e -> {
            isBuyMode = false;
            updateMainTabStyles(buyTab, sellTab);
            refreshItems();
        });
        
        HBox.setHgrow(buyTab, Priority.ALWAYS);
        HBox.setHgrow(sellTab, Priority.ALWAYS);
        
        bar.getChildren().addAll(buyTab, sellTab);
        return bar;
    }
    
    private Button createMainTab(String text, boolean active) {
        Button tab = new Button(text);
        tab.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        tab.setMaxWidth(Double.MAX_VALUE);
        tab.setPadding(new Insets(12, 20, 12, 20));
        tab.setStyle(getMainTabStyle(active));
        return tab;
    }
    
    private String getMainTabStyle(boolean active) {
        String bg = active ? TAB_ACTIVE : TAB_INACTIVE;
        String textColor = active ? GOLD_BRIGHT : TEXT_DIM;
        String border = active ? GOLD : BORDER;
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: " + textColor + ";" +
               "-fx-background-radius: 0;" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: 0 0 3 0;" +
               "-fx-cursor: hand;";
    }
    
    private void updateMainTabStyles(Button buyTab, Button sellTab) {
        buyTab.setStyle(getMainTabStyle(isBuyMode));
        sellTab.setStyle(getMainTabStyle(!isBuyMode));
    }
    
    private HBox createFilterBar() {
        HBox bar = new HBox(5);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 15, 8, 15));
        bar.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setFont(Font.font("Georgia", 11));
        filterLabel.setTextFill(Color.web(TEXT_DIM));
        
        bar.getChildren().add(filterLabel);
        
        for (ItemFilter filter : ItemFilter.values()) {
            Button filterBtn = createFilterButton(filter);
            bar.getChildren().add(filterBtn);
        }
        
        return bar;
    }
    
    private Button createFilterButton(ItemFilter filter) {
        Button btn = new Button(filter.getIcon() + " " + filter.getName());
        btn.setFont(Font.font("Georgia", 10));
        btn.setPadding(new Insets(4, 8, 4, 8));
        btn.setStyle(getFilterStyle(filter == currentFilter));
        
        btn.setOnAction(e -> {
            currentFilter = filter;
            updateFilterStyles();
            refreshItems();
        });
        
        btn.setUserData(filter);
        return btn;
    }
    
    private String getFilterStyle(boolean active) {
        String bg = active ? BORDER : BG_LIGHTER;
        String textColor = active ? GOLD_BRIGHT : TEXT;
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: " + textColor + ";" +
               "-fx-background-radius: 4;" +
               "-fx-border-color: " + (active ? GOLD : BORDER) + ";" +
               "-fx-border-radius: 4;" +
               "-fx-cursor: hand;";
    }
    
    private void updateFilterStyles() {
        for (javafx.scene.Node node : filterBar.getChildren()) {
            if (node instanceof Button && node.getUserData() instanceof ItemFilter) {
                Button btn = (Button) node;
                ItemFilter filter = (ItemFilter) btn.getUserData();
                btn.setStyle(getFilterStyle(filter == currentFilter));
            }
        }
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15, 20, 20, 20));
        footer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_MED + ", " + BG_LIGHT + ");" +
            "-fx-background-radius: 0 0 12 12;"
        );
        
        // Return to Town button
        Button returnBtn = createFooterButton("🏘 Return to Town");
        returnBtn.setOnAction(e -> {
            close();
            if (onReturnToTown != null) onReturnToTown.run();
        });
        
        // Close button
        Button closeBtn = createFooterButton("✕ Close");
        closeBtn.setOnAction(e -> close());
        
        footer.getChildren().addAll(returnBtn, closeBtn);
        return footer;
    }
    
    private Button createFooterButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(10, 25, 10, 25));
        String baseStyle = 
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHTER + ", " + BG_LIGHT + ");" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER_LIGHT + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;";
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_LIGHTER + ");" +
            "-fx-text-fill: " + GOLD + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + GOLD + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }
    
    // === Show / Refresh ===
    
    public void show(Town town, PlayerSprite player, EconomySystem economy) {
        this.currentTown = town;
        this.player = player;
        this.economy = economy;
        
        String townType = town.isMajor() ? "City" : "Village";
        townNameLabel.setText(town.getName() + " " + townType + " Market");
        
        updateCurrencyDisplay();
        refreshItems();
        
        setVisible(true);
        
        mainPanel.setScaleX(0.85);
        mainPanel.setScaleY(0.85);
        mainPanel.setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), mainPanel);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        FadeTransition fade = new FadeTransition(Duration.millis(250), mainPanel);
        fade.setToValue(1.0);
        
        scale.play();
        fade.play();
    }
    
    private void updateCurrencyDisplay() {
        if (player != null) {
            Currency currency = player.getCurrency();
            goldLabel.setText(String.valueOf(currency.getGold()));
            silverLabel.setText(String.valueOf(currency.getSilver()));
            copperLabel.setText(String.valueOf(currency.getCopper()));
        }
    }
    
    private void refreshItems() {
        if (isBuyMode) {
            populateBuyList();
        } else {
            populateSellList();
        }
    }
    
    private void populateBuyList() {
        itemsContainer.getChildren().clear();
        
        if (economy == null || currentTown == null) {
            addEmptyMessage("No items available", "The market is empty.");
            return;
        }
        
        // Get items that actually exist in the registry
        List<String> validItems = getValidBuyItems();
        
        if (validItems.isEmpty()) {
            addEmptyMessage("No items for sale", "Check back later for new stock.");
            return;
        }
        
        for (String itemId : validItems) {
            Item item = ItemRegistry.get(itemId);
            if (item != null && matchesFilter(item)) {
                int quantity = economy.getItemQuantity(currentTown, itemId);
                if (quantity > 0) {
                    HBox itemRow = createBuyItemRow(item, itemId, quantity);
                    itemsContainer.getChildren().add(itemRow);
                }
            }
        }
        
        if (itemsContainer.getChildren().isEmpty()) {
            addEmptyMessage("No matching items", "Try a different filter.");
        }
    }
    
    private List<String> getValidBuyItems() {
        List<String> valid = new ArrayList<>();
        if (economy == null || currentTown == null) return valid;
        
        List<String> available = economy.getAvailableItems(currentTown);
        for (String itemId : available) {
            // Only include items that exist in the registry
            if (ItemRegistry.exists(itemId)) {
                valid.add(itemId);
            }
        }
        return valid;
    }
    
    private boolean matchesFilter(Item item) {
        if (currentFilter == ItemFilter.ALL) return true;
        
        switch (currentFilter) {
            case GRAIN:
                return item.getId().startsWith("grain_");
            case MATERIAL:
                return item.getCategory() == Item.Category.MATERIAL && !item.getId().startsWith("grain_");
            case FOOD:
                return item.getCategory() == Item.Category.CONSUMABLE;
            case EQUIPMENT:
                return item.getCategory() == Item.Category.EQUIPMENT || 
                       item.getCategory() == Item.Category.WEAPON;
            default:
                return true;
        }
    }
    
    private void populateSellList() {
        itemsContainer.getChildren().clear();
        
        if (player == null) {
            addEmptyMessage("No items to sell", "Your inventory is empty.");
            return;
        }
        
        Inventory inventory = player.getInventory();
        boolean hasItems = false;
        
        // Debug output
        System.out.println("Checking inventory for sellable items...");
        System.out.println("Inventory size: " + inventory.getSize());
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getSlot(i);
            if (stack != null && !stack.isEmpty()) {
                Item item = stack.getItem();
                System.out.println("Slot " + i + ": " + (item != null ? item.getName() + " x" + stack.getQuantity() : "null item"));
                
                if (item != null && isSellable(item) && matchesFilter(item)) {
                    HBox itemRow = createSellItemRow(item, stack.getQuantity(), i);
                    itemsContainer.getChildren().add(itemRow);
                    hasItems = true;
                }
            }
        }
        
        if (!hasItems) {
            String filterMsg = currentFilter == ItemFilter.ALL ? 
                "Gather resources to trade at the market!" :
                "No " + currentFilter.getName().toLowerCase() + " items to sell.";
            addEmptyMessage("Nothing to sell", filterMsg);
        }
    }
    
    private boolean isSellable(Item item) {
        // Materials, consumables, and misc items can be sold
        return item.getCategory() == Item.Category.MATERIAL ||
               item.getCategory() == Item.Category.CONSUMABLE ||
               item.getCategory() == Item.Category.MISC;
    }
    
    private void addEmptyMessage(String title, String subtitle) {
        VBox msgBox = new VBox(5);
        msgBox.setAlignment(Pos.CENTER);
        msgBox.setPadding(new Insets(40));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web(TEXT_DIM));
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Georgia", 12));
        subtitleLabel.setTextFill(Color.web(TEXT_DIM, 0.7));
        
        msgBox.getChildren().addAll(titleLabel, subtitleLabel);
        itemsContainer.getChildren().add(msgBox);
    }
    
    private HBox createBuyItemRow(Item item, String itemId, int availableQty) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle(
            "-fx-background-color: " + BG_DARK + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        
        // Item icon
        Canvas iconCanvas = new Canvas(40, 40);
        item.renderIcon(iconCanvas.getGraphicsContext2D(), 0, 0, 40);
        
        // Item info
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        nameLabel.setTextFill(item.getRarity().getColor());
        
        Label stockLabel = new Label("In Stock: " + availableQty);
        stockLabel.setFont(Font.font("Georgia", 11));
        stockLabel.setTextFill(Color.web(TEXT_DIM));
        
        infoBox.getChildren().addAll(nameLabel, stockLabel);
        
        // Price
        long priceCopper = economy.getBuyPrice(currentTown, itemId);
        VBox priceBox = createPriceDisplay(priceCopper);
        priceBox.setMinWidth(90);
        
        // Buy button
        Button buyBtn = createActionButton("Buy", BUY_COLOR, BUY_HOVER);
        final long finalPrice = priceCopper;
        buyBtn.setOnAction(e -> buyItem(itemId, 1, finalPrice));
        
        if (player == null || !player.getCurrency().canAfford(priceCopper)) {
            buyBtn.setDisable(true);
            buyBtn.setStyle(
                "-fx-background-color: #303030;" +
                "-fx-text-fill: #606060;" +
                "-fx-background-radius: 5;" +
                "-fx-border-color: #404040;" +
                "-fx-border-radius: 5;"
            );
        }
        
        row.getChildren().addAll(iconCanvas, infoBox, priceBox, buyBtn);
        return row;
    }
    
    private HBox createSellItemRow(Item item, int quantity, int slotIndex) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle(
            "-fx-background-color: " + BG_DARK + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        
        // Item icon
        Canvas iconCanvas = new Canvas(40, 40);
        item.renderIcon(iconCanvas.getGraphicsContext2D(), 0, 0, 40);
        
        // Item info
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        nameLabel.setTextFill(item.getRarity().getColor());
        
        Label qtyLabel = new Label("Owned: " + quantity);
        qtyLabel.setFont(Font.font("Georgia", 11));
        qtyLabel.setTextFill(Color.web(TEXT_DIM));
        
        infoBox.getChildren().addAll(nameLabel, qtyLabel);
        
        // Calculate sell price
        long baseValueCopper = item.getBaseValue();
        double modifier = currentTown.isMajor() ? 0.85 : 0.70;
        long sellPriceCopper = Math.max(1, (long)(baseValueCopper * modifier));
        
        VBox priceBox = createPriceDisplay(sellPriceCopper);
        priceBox.setMinWidth(90);
        
        // Sell buttons
        VBox btnBox = new VBox(4);
        btnBox.setAlignment(Pos.CENTER);
        
        Button sellBtn = createActionButton("Sell 1", SELL_COLOR, SELL_HOVER);
        final long finalPrice = sellPriceCopper;
        final int finalSlot = slotIndex;
        sellBtn.setOnAction(e -> sellItem(finalPrice, 1, finalSlot));
        
        Button sellAllBtn = createActionButton("Sell All", "#4a3020", "#6a4030");
        sellAllBtn.setOnAction(e -> sellItem(finalPrice, quantity, finalSlot));
        
        btnBox.getChildren().addAll(sellBtn, sellAllBtn);
        
        row.getChildren().addAll(iconCanvas, infoBox, priceBox, btnBox);
        return row;
    }
    
    private VBox createPriceDisplay(long copperValue) {
        VBox priceBox = new VBox(0);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        
        int gold = (int)(copperValue / Currency.COPPER_PER_GOLD);
        int silver = (int)((copperValue % Currency.COPPER_PER_GOLD) / Currency.COPPER_PER_SILVER);
        int copper = (int)(copperValue % Currency.COPPER_PER_SILVER);
        
        HBox coinRow = new HBox(6);
        coinRow.setAlignment(Pos.CENTER_RIGHT);
        
        if (gold > 0) {
            coinRow.getChildren().add(createSmallCoinLabel(gold, GOLD_COLOR));
        }
        if (silver > 0 || gold > 0) {
            coinRow.getChildren().add(createSmallCoinLabel(silver, SILVER_COLOR));
        }
        coinRow.getChildren().add(createSmallCoinLabel(copper, COPPER_COLOR));
        
        priceBox.getChildren().add(coinRow);
        return priceBox;
    }
    
    private HBox createSmallCoinLabel(int value, Color color) {
        HBox box = new HBox(2);
        box.setAlignment(Pos.CENTER);
        
        Canvas coin = new Canvas(12, 12);
        drawCoin(coin.getGraphicsContext2D(), color, 6, 6, 5);
        
        Label lbl = new Label(String.valueOf(value));
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        lbl.setTextFill(color);
        
        box.getChildren().addAll(coin, lbl);
        return box;
    }
    
    private Button createActionButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        btn.setPadding(new Insets(6, 12, 6, 12));
        btn.setMinWidth(70);
        String baseStyle = 
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;";
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + hoverColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + GOLD + ";" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }
    
    // === Buy/Sell Actions ===
    
    private void buyItem(String itemId, int quantity, long pricePerItem) {
        if (player == null || economy == null || currentTown == null) return;
        
        long totalCost = pricePerItem * quantity;
        if (player.getCurrency().canAfford(totalCost)) {
            player.getCurrency().subtract(totalCost);
            
            ItemStack bought = ItemRegistry.createStack(itemId, quantity);
            if (bought != null) {
                player.getInventory().addItem(bought);
            }
            
            updateCurrencyDisplay();
            refreshItems();
        }
    }
    
    private void sellItem(long pricePerItem, int quantity, int slotIndex) {
        if (player == null || currentTown == null) return;
        
        long earnings = pricePerItem * quantity;
        player.getCurrency().add(earnings);
        
        ItemStack stack = player.getInventory().getSlot(slotIndex);
        if (stack != null) {
            stack.remove(quantity);
            if (stack.isEmpty()) {
                player.getInventory().setSlot(slotIndex, null);
            }
        }
        
        updateCurrencyDisplay();
        refreshItems();
    }
    
    // === Close / Callbacks ===
    
    public void close() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), mainPanel);
        scale.setToX(0.9);
        scale.setToY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(150), this);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            setVisible(false);
            setOpacity(1);
            if (onClose != null) onClose.run();
        });
        
        scale.play();
        fade.play();
    }
    
    public void setOnClose(Runnable handler) { this.onClose = handler; }
    public void setOnReturnToTown(Runnable handler) { this.onReturnToTown = handler; }
    public boolean isShowing() { return isVisible(); }
    public Town getCurrentTown() { return currentTown; }
}
