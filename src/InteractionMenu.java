import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Right-side interaction menu with tabbed organization.
 * Provides controls for map, character, and settings.
 */
public class InteractionMenu {
    
    // Layout components
    private final VBox container;
    private final VBox contentArea;
    private final HBox tabBar;
    private final ToggleGroup tabGroup;
    
    // Tab content panels
    private VBox mapPanel;
    private VBox characterPanel;
    private VBox settingsPanel;
    
    // Wallet display
    private Label walletLabel;
    private Currency playerCurrency;
    
    // Control handlers
    private Runnable onStandardFilter;
    private Runnable onTopoFilter;
    private Runnable onHeatmapFilter;

    private Runnable onSaveWorld;
    private Runnable onLoadWorld;
    private Runnable onMainMenu;
    
    // Filter toggle buttons for visual feedback
    private ToggleButton standardFilterBtn;
    private ToggleButton topoFilterBtn;
    private ToggleButton heatmapFilterBtn;
    private ToggleGroup filterToggleGroup;
    private VBox heatmapLegend;
    private java.util.function.Consumer<Boolean> onHeatmapToggle;

    // Dev tool handlers
    private Runnable onDevGiveGold;
    private Runnable onDevRefillEnergy;
    private java.util.function.Consumer<Boolean> onTeleportToggle;
    private CheckBox teleportCheatCheck;
    
    // Character handlers
    private Runnable onOpenCharacterSheet;
    private Runnable onOpenGearSheet;
    private Runnable onOpenInventory;
    private Runnable onOpenRelationships;
    private Runnable onOpenSkills;
    private Runnable onOpenJournal;
    private Runnable onOpenParty;
    
    // Style constants - Medieval theme
    private static final String DARK_BG = "#1f1a10";
    private static final String MEDIUM_BG = "#2d2418";
    private static final String LIGHT_BG = "#3d3020";
    private static final String ACCENT_COLOR = "#c4a574";
    private static final String TEXT_COLOR = "#d4c4a4";
    private static final String TAB_ACTIVE = "#5a4a30";
    private static final String TAB_INACTIVE = "#2d2418";
    
    public InteractionMenu() {
        container = new VBox();
        container.setPrefWidth(260);
        container.setStyle(
            "-fx-background-color: transparent;"
        );
        
        tabGroup = new ToggleGroup();
        contentArea = new VBox();
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // Create panels FIRST before creating tab bar
        createTabPanels();
        
        // Now create tab bar (panels exist now)
        tabBar = createTabBar();
        
        container.getChildren().addAll(tabBar, contentArea);
        
        // Show Map tab by default
        showPanel(mapPanel);
    }
    
    // Tab button references for updating panels
    private ToggleButton mapTab;
    private ToggleButton characterTab;
    private ToggleButton settingsTab;
    
    /**
     * Creates the tab bar with toggle buttons using icons.
     */
    private HBox createTabBar() {
        HBox bar = new HBox(2);
        bar.setPadding(new Insets(5));
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-background-color: " + DARK_BG + ";");
        
        // Create tabs with proper panel references (panels exist now)
        mapTab = createTab("🗺", "Map", mapPanel);
        characterTab = createTab("👤", "Character", characterPanel);
        settingsTab = createTab("⚙", "Settings", settingsPanel);
        
        mapTab.setSelected(true);
        
        bar.getChildren().addAll(mapTab, characterTab, settingsTab);
        return bar;
    }
    
    /**
     * Creates a tab toggle button with icon and tooltip.
     */
    private ToggleButton createTab(String icon, String tooltip, VBox panel) {
        ToggleButton tab = new ToggleButton(icon);
        tab.setToggleGroup(tabGroup);
        tab.setMaxWidth(Double.MAX_VALUE);
        tab.setMinWidth(40);
        tab.setPrefWidth(45);
        HBox.setHgrow(tab, Priority.ALWAYS);
        
        // Add tooltip to show full name on hover
        tab.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        
        tab.setStyle(createIconTabStyle(false));
        
        tab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            tab.setStyle(createIconTabStyle(newVal));
            if (newVal && panel != null) {
                showPanel(panel);
            }
        });
        
        // Store panel reference for lazy initialization
        tab.setUserData(tooltip);
        
        return tab;
    }
    
    /**
     * Creates CSS style for tab button.
     */
    private String createTabStyle(boolean selected) {
        String bgColor = selected ? TAB_ACTIVE : TAB_INACTIVE;
        String textColor = selected ? "white" : TEXT_COLOR;
        return String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: %s;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 8 6 8;" +
            "-fx-background-radius: 3 3 0 0;",
            bgColor, textColor
        );
    }
    
    /**
     * Creates CSS style for icon tab button.
     */
    private String createIconTabStyle(boolean selected) {
        String bgColor = selected ? TAB_ACTIVE : TAB_INACTIVE;
        String textColor = selected ? "white" : TEXT_COLOR;
        return String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: %s;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 8 4 8 4;" +
            "-fx-background-radius: 3 3 0 0;" +
            "-fx-cursor: hand;",
            bgColor, textColor
        );
    }
    
    /**
     * Creates all tab content panels.
     */
    private void createTabPanels() {
        mapPanel = createMapPanel();
        characterPanel = createCharacterPanel();
        settingsPanel = createSettingsPanel();
    }
    
    /**
     * Shows the specified panel in the content area.
     */
    private void showPanel(VBox panel) {
        contentArea.getChildren().clear();
        if (panel != null) {
            ScrollPane scrollPane = new ScrollPane(panel);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle(
                "-fx-background: " + MEDIUM_BG + ";" +
                "-fx-background-color: " + MEDIUM_BG + ";"
            );
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            contentArea.getChildren().add(scrollPane);
        }
    }
    
    // ==================== Map Panel ====================
    
    private VBox createMapPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: " + MEDIUM_BG + ";");
        
        // Wallet Display
        panel.getChildren().add(createSectionLabel("💰 WALLET"));
        
        VBox walletBox = new VBox(3);
        walletBox.setPadding(new Insets(8));
        walletBox.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: #5a4a30;" +
            "-fx-border-radius: 5;" +
            "-fx-border-width: 1;"
        );
        
        walletLabel = new Label("0g 0s 0c");
        walletLabel.setStyle(
            "-fx-text-fill: #ffd700;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;"
        );
        walletBox.getChildren().add(walletLabel);
        panel.getChildren().add(walletBox);
        
        // Filter Controls (no zoom buttons - use scroll wheel/trackpad)
        panel.getChildren().add(createSectionLabel("MAP FILTERS"));
        
        // Create toggle group for exclusive filter selection
        filterToggleGroup = new ToggleGroup();
        
        standardFilterBtn = createFilterToggle("🗺 Standard");
        topoFilterBtn = createFilterToggle("⛰ Topographical");
        heatmapFilterBtn = createFilterToggle("💎 Resources");
        
        standardFilterBtn.setToggleGroup(filterToggleGroup);
        topoFilterBtn.setToggleGroup(filterToggleGroup);
        heatmapFilterBtn.setToggleGroup(filterToggleGroup);
        
        // Set Standard as default selected
        standardFilterBtn.setSelected(true);
        
        standardFilterBtn.setOnAction(e -> { 
            if (onStandardFilter != null) onStandardFilter.run();
            updateFilterLegend("standard");
        });
        topoFilterBtn.setOnAction(e -> { 
            if (onTopoFilter != null) onTopoFilter.run();
            updateFilterLegend("topo");
        });
        heatmapFilterBtn.setOnAction(e -> { 
            if (onHeatmapFilter != null) onHeatmapFilter.run();
            updateFilterLegend("heatmap");
        });

        // Notify listeners when heatmap button is selected/deselected
        heatmapFilterBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (onHeatmapToggle != null) onHeatmapToggle.accept(newVal);
        });
        
        panel.getChildren().addAll(standardFilterBtn, topoFilterBtn, heatmapFilterBtn);
        
        // Create heatmap legend (initially hidden)
        heatmapLegend = createHeatmapLegend();
        heatmapLegend.setVisible(false);
        heatmapLegend.setManaged(false);
        panel.getChildren().add(heatmapLegend);
        

        
        // Game Controls
        panel.getChildren().add(createSectionLabel("GAME"));
        
        Button saveBtn = createButton("💾 Save World");
        Button loadBtn = createButton("📂 Load World");
        Button menuBtn = createButton("🏠 Main Menu");
        
        saveBtn.setOnAction(e -> { if (onSaveWorld != null) onSaveWorld.run(); });
        loadBtn.setOnAction(e -> { if (onLoadWorld != null) onLoadWorld.run(); });
        menuBtn.setOnAction(e -> { if (onMainMenu != null) onMainMenu.run(); });
        
        panel.getChildren().addAll(saveBtn, loadBtn, menuBtn);
        
        return panel;
    }
    
    // ==================== Character Panel ====================
    
    private VBox createCharacterPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: " + MEDIUM_BG + ";");
        
        // Character Actions
        panel.getChildren().add(createSectionLabel("👤 CHARACTER"));
        
        Button characterSheetBtn = createButton("📜 Character Sheet");
        Button inventoryBtn = createButton("🎒 Inventory");
        Button gearBtn = createButton("⚔ Equipment");
        
        characterSheetBtn.setOnAction(e -> { if (onOpenCharacterSheet != null) onOpenCharacterSheet.run(); });
        inventoryBtn.setOnAction(e -> { if (onOpenInventory != null) onOpenInventory.run(); });
        gearBtn.setOnAction(e -> { if (onOpenGearSheet != null) onOpenGearSheet.run(); });
        
        panel.getChildren().addAll(characterSheetBtn, inventoryBtn, gearBtn);
        
        // Social
        panel.getChildren().add(createSectionLabel("👥 SOCIAL"));
        
        Button partyBtn = createButton("⚔ Party");
        Button relationshipsBtn = createButton("🤝 Relationships");
        Button reputationBtn = createButton("⭐ Reputation");
        
        partyBtn.setOnAction(e -> { if (onOpenParty != null) onOpenParty.run(); });
        relationshipsBtn.setOnAction(e -> { if (onOpenRelationships != null) onOpenRelationships.run(); });
        reputationBtn.setOnAction(e -> { if (onOpenRelationships != null) onOpenRelationships.run(); });
        
        panel.getChildren().addAll(partyBtn, relationshipsBtn, reputationBtn);
        
        // Progression
        panel.getChildren().add(createSectionLabel("📈 PROGRESSION"));
        
        Button skillsBtn = createButton("⚡ Skills");
        Button journalBtn = createButton("📖 Journal");
        Button achievementsBtn = createButton("🏆 Achievements");
        
        skillsBtn.setOnAction(e -> { if (onOpenSkills != null) onOpenSkills.run(); });
        journalBtn.setOnAction(e -> { if (onOpenJournal != null) onOpenJournal.run(); });
        achievementsBtn.setOnAction(e -> { if (onOpenJournal != null) onOpenJournal.run(); });
        
        panel.getChildren().addAll(skillsBtn, journalBtn, achievementsBtn);
        
        return panel;
    }
    
    // ==================== Settings Panel ====================
    
    private VBox createSettingsPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: " + MEDIUM_BG + ";");
        
        // Display Settings
        panel.getChildren().add(createSectionLabel("DISPLAY"));
        
        // Resolution selector
        Label resLabel = createSmallLabel("Resolution:");
        ComboBox<String> resolutionBox = new ComboBox<>();
        resolutionBox.getItems().addAll("1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440", "3840x2160");
        resolutionBox.setValue(GameSettings.getInstance().getResolution().toString().replace("_", "x").replace("RES", ""));
        resolutionBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(resolutionBox);
        
        // Display mode selector
        Label modeLabel = createSmallLabel("Display Mode:");
        ComboBox<String> displayModeBox = new ComboBox<>();
        displayModeBox.getItems().addAll("Fullscreen", "Windowed Fullscreen", "Windowed");
        displayModeBox.setValue(formatDisplayMode(GameSettings.getInstance().getDisplayMode()));
        displayModeBox.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(displayModeBox);
        
        // VSync checkbox
        CheckBox vsyncCheck = new CheckBox("VSync");
        vsyncCheck.setSelected(GameSettings.getInstance().isVsync());
        vsyncCheck.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
        
        panel.getChildren().addAll(resLabel, resolutionBox, modeLabel, displayModeBox, vsyncCheck);
        
        // Audio Settings
        panel.getChildren().add(createSectionLabel("AUDIO"));
        
        Label musicLabel = createSmallLabel("Music Volume:");
        Slider musicSlider = new Slider(0, 100, GameSettings.getInstance().getMusicVolume());
        musicSlider.setShowTickLabels(true);
        musicSlider.setShowTickMarks(true);
        musicSlider.setMajorTickUnit(25);
        
        Label sfxLabel = createSmallLabel("SFX Volume:");
        Slider sfxSlider = new Slider(0, 100, GameSettings.getInstance().getSfxVolume());
        sfxSlider.setShowTickLabels(true);
        sfxSlider.setShowTickMarks(true);
        sfxSlider.setMajorTickUnit(25);
        
        panel.getChildren().addAll(musicLabel, musicSlider, sfxLabel, sfxSlider);
        
        // Gameplay Settings
        panel.getChildren().add(createSectionLabel("GAMEPLAY"));
        
        CheckBox autosaveCheck = new CheckBox("Auto-save");
        autosaveCheck.setSelected(GameSettings.getInstance().isAutoSave());
        autosaveCheck.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
        
        Label autosaveLabel = createSmallLabel("Auto-save Interval (minutes):");
        Slider autosaveSlider = new Slider(1, 30, GameSettings.getInstance().getAutoSaveInterval());
        autosaveSlider.setShowTickLabels(true);
        autosaveSlider.setShowTickMarks(true);
        autosaveSlider.setMajorTickUnit(5);
        autosaveSlider.setDisable(!GameSettings.getInstance().isAutoSave());
        
        autosaveCheck.setOnAction(e -> autosaveSlider.setDisable(!autosaveCheck.isSelected()));
        
        panel.getChildren().addAll(autosaveCheck, autosaveLabel, autosaveSlider);

        // Dev tools / cheats
        panel.getChildren().add(createSectionLabel("DEV TOOLS"));

        Button devGoldBtn = createButton("💰 Add 500g");
        devGoldBtn.setOnAction(e -> { if (onDevGiveGold != null) onDevGiveGold.run(); });
        
        Button devEnergyBtn = createButton("⚡ Refill Energy");
        devEnergyBtn.setOnAction(e -> { if (onDevRefillEnergy != null) onDevRefillEnergy.run(); });
        
        teleportCheatCheck = new CheckBox("Ctrl+Click Teleport");
        teleportCheatCheck.setSelected(GameSettings.getInstance().isTeleportCheatEnabled());
        teleportCheatCheck.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
        teleportCheatCheck.setOnAction(e -> {
            if (onTeleportToggle != null) {
                onTeleportToggle.accept(teleportCheatCheck.isSelected());
            }
        });
        
        panel.getChildren().addAll(devGoldBtn, devEnergyBtn, teleportCheatCheck);
        
        // Apply button
        Button applyBtn = createButton("Apply Settings");
        applyBtn.setStyle(applyBtn.getStyle() + "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;");
        applyBtn.setOnAction(e -> {
            // Save settings
            GameSettings settings = GameSettings.getInstance();
            settings.setResolution(parseResolution(resolutionBox.getValue()));
            settings.setDisplayMode(parseDisplayMode(displayModeBox.getValue()));
            settings.setVsync(vsyncCheck.isSelected());
            settings.setMusicVolume((int) musicSlider.getValue());
            settings.setSfxVolume((int) sfxSlider.getValue());
            settings.setAutoSave(autosaveCheck.isSelected());
            settings.setAutoSaveInterval((int) autosaveSlider.getValue());
            settings.setTeleportCheatEnabled(teleportCheatCheck.isSelected());
            settings.save();
        });
        
        panel.getChildren().add(applyBtn);
        
        return panel;
    }
    
    private Label createSmallLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-size: 11px;");
        return label;
    }
    
    private void styleComboBox(ComboBox<String> box) {
        box.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-border-color: #505050;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;"
        );
    }
    
    private String formatDisplayMode(GameSettings.DisplayMode mode) {
        switch (mode) {
            case FULLSCREEN: return "Fullscreen";
            case WINDOWED_FULLSCREEN: return "Windowed Fullscreen";
            case WINDOWED: return "Windowed";
            default: return "Windowed";
        }
    }
    
    private GameSettings.DisplayMode parseDisplayMode(String text) {
        switch (text) {
            case "Fullscreen": return GameSettings.DisplayMode.FULLSCREEN;
            case "Windowed Fullscreen": return GameSettings.DisplayMode.WINDOWED_FULLSCREEN;
            case "Windowed": return GameSettings.DisplayMode.WINDOWED;
            default: return GameSettings.DisplayMode.WINDOWED;
        }
    }
    
    private GameSettings.Resolution parseResolution(String text) {
        switch (text) {
            case "1280x720": return GameSettings.Resolution.RES_1280x720;
            case "1366x768": return GameSettings.Resolution.RES_1366x768;
            case "1600x900": return GameSettings.Resolution.RES_1600x900;
            case "1920x1080": return GameSettings.Resolution.RES_1920x1080;
            case "2560x1440": return GameSettings.Resolution.RES_2560x1440;
            case "3840x2160": return GameSettings.Resolution.RES_3840x2160;
            default: return GameSettings.Resolution.RES_1920x1080;
        }
    }
    
    // ==================== UI Helpers ====================
    
    /**
     * Creates a section label.
     */
    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        label.setStyle(
            "-fx-text-fill: " + ACCENT_COLOR + ";" +
            "-fx-padding: 10 0 5 0;"
        );
        return label;
    }
    
    /**
     * Creates a styled button.
     */
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        String normalStyle = String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: %s;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 8 10 8 10;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 3;" +
            "-fx-border-radius: 3;",
            LIGHT_BG, TEXT_COLOR
        );
        
        String hoverStyle = String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 8 10 8 10;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 3;" +
            "-fx-border-radius: 3;",
            ACCENT_COLOR
        );
        
        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
        
        return button;
    }
    
    /**
     * Creates a styled toggle button for filters.
     */
    private ToggleButton createFilterToggle(String text) {
        ToggleButton toggle = new ToggleButton(text);
        toggle.setMaxWidth(Double.MAX_VALUE);
        toggle.setAlignment(Pos.CENTER_LEFT);
        
        String normalStyle = String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: %s;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 8 10 8 10;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 3;" +
            "-fx-border-radius: 3;",
            LIGHT_BG, TEXT_COLOR
        );
        
        String selectedStyle = String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 8 10 8 10;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 3;" +
            "-fx-border-radius: 3;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 1;",
            ACCENT_COLOR
        );
        
        toggle.setStyle(normalStyle);
        
        toggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            toggle.setStyle(isSelected ? selectedStyle : normalStyle);
        });
        
        return toggle;
    }
    
    /**
     * Creates a legend for the resource heatmap filter.
     */
    private VBox createHeatmapLegend() {
        VBox legend = new VBox(3);
        legend.setPadding(new Insets(5, 0, 5, 0));
        legend.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-padding: 8;" +
            "-fx-background-radius: 4;"
        );
        
        Label title = new Label("Resource Legend:");
        title.setStyle("-fx-text-fill: " + ACCENT_COLOR + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        legend.getChildren().add(title);
        
        // Add legend entries
        legend.getChildren().add(createLegendEntry("💎 Ore", "#ff6a00"));
        legend.getChildren().add(createLegendEntry("🪨 Stone", "#c0c0c0"));
        legend.getChildren().add(createLegendEntry("💠 Gems", "#b000b0"));
        legend.getChildren().add(createLegendEntry("🌲 Wood", "#2dbf4f"));
        legend.getChildren().add(createLegendEntry("🌾 Fertility", "#c7f000"));
        legend.getChildren().add(createLegendEntry("🐟 Fish", "#3ab0ff"));
        
        return legend;
    }
    
    /**
     * Creates a single legend entry with color indicator.
     */
    private HBox createLegendEntry(String text, String color) {
        HBox entry = new HBox(5);
        entry.setAlignment(Pos.CENTER_LEFT);
        
        // Color indicator
        javafx.scene.shape.Rectangle colorBox = new javafx.scene.shape.Rectangle(12, 12);
        colorBox.setFill(javafx.scene.paint.Color.web(color));
        colorBox.setArcWidth(2);
        colorBox.setArcHeight(2);
        
        // Label
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-size: 10px;");
        
        entry.getChildren().addAll(colorBox, label);
        return entry;
    }
    
    /**
     * Updates the visibility of the filter legend based on the current filter.
     */
    private void updateFilterLegend(String filterType) {
        if (heatmapLegend != null) {
            boolean showLegend = "heatmap".equals(filterType);
            heatmapLegend.setVisible(showLegend);
            heatmapLegend.setManaged(showLegend);
        }
    }
    
    // ==================== Handler Setters ====================
    
    
    /**
     * Sets handlers for filter controls.
     */
    public void setFilterHandlers(Runnable standard, Runnable topo, Runnable heatmap) {
        this.onStandardFilter = standard;
        this.onTopoFilter = topo;
        this.onHeatmapFilter = heatmap;
    }

    /**
     * Sets a handler that receives heatmap toggle state changes (selected/unselected).
     */
    public void setHeatmapToggleHandler(java.util.function.Consumer<Boolean> handler) {
        this.onHeatmapToggle = handler;
    }
    
    /**
     * Sets handlers for save/load controls.
     */
    public void setSaveLoadHandlers(Runnable saveWorld, Runnable loadWorld) {
        this.onSaveWorld = saveWorld;
        this.onLoadWorld = loadWorld;
    }
    
    /**
     * Sets handler for main menu button.
     */
    public void setMainMenuHandler(Runnable mainMenu) {
        this.onMainMenu = mainMenu;
    }
    
    /**
     * Sets handlers for character panel buttons.
     */
    public void setCharacterHandlers(Runnable characterSheet, Runnable gearSheet, Runnable inventory, 
                                      Runnable relationships, Runnable skills, Runnable journal) {
        this.onOpenCharacterSheet = characterSheet;
        this.onOpenGearSheet = gearSheet;
        this.onOpenInventory = inventory;
        this.onOpenRelationships = relationships;
        this.onOpenSkills = skills;
        this.onOpenJournal = journal;
    }
    
    /**
     * Sets the handler for opening the party management window.
     */
    public void setPartyHandler(Runnable partyHandler) {
        this.onOpenParty = partyHandler;
    }

    /**
     * Sets handlers for dev tools (cheats).
     */
    public void setDevToolHandlers(Runnable giveGold, Runnable refillEnergy, java.util.function.Consumer<Boolean> teleportToggle) {
        this.onDevGiveGold = giveGold;
        this.onDevRefillEnergy = refillEnergy;
        this.onTeleportToggle = teleportToggle;
    }

    /**
     * Syncs the teleport cheat checkbox with current setting.
     */
    public void syncTeleportCheat(boolean enabled) {
        if (teleportCheatCheck != null) {
            teleportCheatCheck.setSelected(enabled);
        }
    }
    
    /**
     * Updates the wallet display with current currency.
     */
    public void updateWallet(Currency currency) {
        this.playerCurrency = currency;
        if (walletLabel != null && currency != null) {
            walletLabel.setText(String.format("%dg %ds %dc", 
                currency.getGold(), currency.getSilver(), currency.getCopper()));
        }
    }
    
    /**
     * Returns the main container.
     */
    public VBox getContainer() {
        return container;
    }
}
