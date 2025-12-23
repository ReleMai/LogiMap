import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * SettingsMenu - Game settings overlay with real settings persistence.
 */
public class SettingsMenu extends StackPane {
    
    private static final String DARK_BG = "#0a0a12";
    private static final String MEDIUM_BG = "#1a1a2e";
    private static final String LIGHT_BG = "#2a2a4e";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String TEXT_COLOR = "#e0e0e0";
    
    private Runnable onClose;
    private Stage primaryStage;
    private GameSettings settings;
    
    // UI Controls
    private Slider musicSlider;
    private Slider sfxSlider;
    private ComboBox<String> displayModeCombo;
    private ComboBox<String> resolutionCombo;
    private CheckBox vsyncCheck;
    private CheckBox autoSaveCheck;
    private Spinner<Integer> autoSaveIntervalSpinner;
    private Slider scrollSpeedSlider;
    private Slider zoomSpeedSlider;
    private CheckBox moveToInteractCheck;
    
    public SettingsMenu() {
        this(null);
    }
    
    public SettingsMenu(Stage stage) {
        this.primaryStage = stage;
        this.settings = GameSettings.getInstance();
        
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        setAlignment(Pos.CENTER);
        
        createContent();
        loadCurrentSettings();
        
        // Click outside to close
        setOnMouseClicked(e -> {
            if (e.getTarget() == this && onClose != null) {
                onClose.run();
            }
        });
    }
    
    private void loadCurrentSettings() {
        musicSlider.setValue(settings.getMusicVolume());
        sfxSlider.setValue(settings.getSfxVolume());
        displayModeCombo.setValue(settings.getDisplayMode().getDisplayName());
        resolutionCombo.setValue(settings.getResolution().displayName);
        vsyncCheck.setSelected(settings.isVsync());
        autoSaveCheck.setSelected(settings.isAutoSave());
        autoSaveIntervalSpinner.getValueFactory().setValue(settings.getAutoSaveInterval());
        scrollSpeedSlider.setValue(settings.getScrollSpeed() * 100);
        zoomSpeedSlider.setValue(settings.getZoomSpeed() * 100);
        moveToInteractCheck.setSelected(settings.isMoveToInteract());
    }
    
    private void createContent() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(25));
        panel.setMaxWidth(550);
        panel.setMaxHeight(650);
        panel.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;"
        );
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setRadius(30);
        panel.setEffect(shadow);
        
        // Header
        HBox header = createHeader();
        
        // Scrollable content
        VBox sectionsBox = new VBox(12);
        sectionsBox.getChildren().addAll(
            createDisplaySection(),
            createAudioSection(),
            createGameplaySection(),
            createControlsSection()
        );
        
        ScrollPane scrollPane = new ScrollPane(sectionsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setMaxHeight(450);
        
        // Buttons
        HBox buttonRow = createButtonRow();
        
        panel.getChildren().addAll(header, scrollPane, buttonRow);
        getChildren().add(panel);
        
        // Animate in
        panel.setScaleX(0.8);
        panel.setScaleY(0.8);
        panel.setOpacity(0);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), panel);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), panel);
        fadeIn.setToValue(1);
        
        scaleIn.play();
        fadeIn.play();
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("⚙️ SETTINGS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(ACCENT_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-font-size: 20;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> {
            if (onClose != null) onClose.run();
        });
        
        header.getChildren().addAll(title, spacer, closeBtn);
        return header;
    }
    
    private VBox createDisplaySection() {
        VBox section = createSection("🖥️ Display");
        
        // Display Mode
        HBox modeRow = new HBox(10);
        modeRow.setAlignment(Pos.CENTER_LEFT);
        Label modeLabel = createLabel("Display Mode");
        displayModeCombo = new ComboBox<>();
        displayModeCombo.getItems().addAll(
            GameSettings.DisplayMode.FULLSCREEN.getDisplayName(),
            GameSettings.DisplayMode.WINDOWED_FULLSCREEN.getDisplayName(),
            GameSettings.DisplayMode.WINDOWED.getDisplayName()
        );
        displayModeCombo.setValue(GameSettings.DisplayMode.WINDOWED.getDisplayName());
        styleComboBox(displayModeCombo);
        modeRow.getChildren().addAll(modeLabel, displayModeCombo);
        
        // Resolution
        HBox resRow = new HBox(10);
        resRow.setAlignment(Pos.CENTER_LEFT);
        Label resLabel = createLabel("Resolution");
        resolutionCombo = new ComboBox<>();
        for (GameSettings.Resolution res : GameSettings.Resolution.values()) {
            resolutionCombo.getItems().add(res.displayName);
        }
        resolutionCombo.setValue(GameSettings.Resolution.RES_1920x1080.displayName);
        styleComboBox(resolutionCombo);
        resRow.getChildren().addAll(resLabel, resolutionCombo);
        
        // VSync
        vsyncCheck = createCheckBox("VSync (Reduces screen tearing)", true);
        
        section.getChildren().addAll(modeRow, resRow, vsyncCheck);
        return section;
    }
    
    private VBox createAudioSection() {
        VBox section = createSection("🔊 Audio");
        
        // Music volume
        HBox musicRow = new HBox(10);
        musicRow.setAlignment(Pos.CENTER_LEFT);
        Label musicLabel = createLabel("Music Volume");
        musicSlider = createSlider(70);
        Label musicValue = createValueLabel(musicSlider, "%");
        musicRow.getChildren().addAll(musicLabel, musicSlider, musicValue);
        
        // SFX volume
        HBox sfxRow = new HBox(10);
        sfxRow.setAlignment(Pos.CENTER_LEFT);
        Label sfxLabel = createLabel("SFX Volume");
        sfxSlider = createSlider(80);
        Label sfxValue = createValueLabel(sfxSlider, "%");
        sfxRow.getChildren().addAll(sfxLabel, sfxSlider, sfxValue);
        
        section.getChildren().addAll(musicRow, sfxRow);
        return section;
    }
    
    private VBox createGameplaySection() {
        VBox section = createSection("🎮 Gameplay");
        
        // Auto-save checkbox
        autoSaveCheck = createCheckBox("Enable Auto-Save", true);
        
        // Auto-save interval
        HBox intervalRow = new HBox(10);
        intervalRow.setAlignment(Pos.CENTER_LEFT);
        Label intervalLabel = createLabel("Auto-Save Interval");
        autoSaveIntervalSpinner = new Spinner<>(1, 30, 5);
        autoSaveIntervalSpinner.setPrefWidth(80);
        autoSaveIntervalSpinner.setEditable(true);
        Label minLabel = new Label("minutes");
        minLabel.setTextFill(Color.web(TEXT_COLOR, 0.7));
        intervalRow.getChildren().addAll(intervalLabel, autoSaveIntervalSpinner, minLabel);
        
        section.getChildren().addAll(autoSaveCheck, intervalRow);
        return section;
    }
    
    private VBox createControlsSection() {
        VBox section = createSection("🎯 Controls");
        
        // Scroll speed
        HBox scrollRow = new HBox(10);
        scrollRow.setAlignment(Pos.CENTER_LEFT);
        Label scrollLabel = createLabel("Scroll Speed");
        scrollSpeedSlider = new Slider(10, 300, 100);
        scrollSpeedSlider.setPrefWidth(180);
        Label scrollValue = createValueLabel(scrollSpeedSlider, "%");
        scrollRow.getChildren().addAll(scrollLabel, scrollSpeedSlider, scrollValue);
        
        // Zoom speed
        HBox zoomRow = new HBox(10);
        zoomRow.setAlignment(Pos.CENTER_LEFT);
        Label zoomLabel = createLabel("Zoom Speed");
        zoomSpeedSlider = new Slider(10, 300, 100);
        zoomSpeedSlider.setPrefWidth(180);
        Label zoomValue = createValueLabel(zoomSpeedSlider, "%");
        zoomRow.getChildren().addAll(zoomLabel, zoomSpeedSlider, zoomValue);
        
        // Move to Interact option
        moveToInteractCheck = createCheckBox("Move to Interact", true);
        Label moveToInteractHint = new Label("Right-click moves player to interactable objects");
        moveToInteractHint.setFont(Font.font("Arial", 10));
        moveToInteractHint.setTextFill(Color.web(TEXT_COLOR, 0.6));
        VBox moveToInteractRow = new VBox(2);
        moveToInteractRow.getChildren().addAll(moveToInteractCheck, moveToInteractHint);
        
        section.getChildren().addAll(scrollRow, zoomRow, moveToInteractRow);
        return section;
    }
    
    private VBox createSection(String title) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(12));
        section.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 8;"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        section.getChildren().add(titleLabel);
        return section;
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 13));
        label.setTextFill(Color.web(TEXT_COLOR, 0.9));
        label.setMinWidth(130);
        return label;
    }
    
    private Slider createSlider(double value) {
        Slider slider = new Slider(0, 100, value);
        slider.setPrefWidth(180);
        return slider;
    }
    
    private Label createValueLabel(Slider slider, String suffix) {
        Label label = new Label(String.valueOf((int) slider.getValue()) + suffix);
        label.setFont(Font.font("Arial", 12));
        label.setTextFill(Color.web(TEXT_COLOR, 0.7));
        label.setMinWidth(45);
        
        slider.valueProperty().addListener((obs, old, val) -> {
            label.setText(String.valueOf(val.intValue()) + suffix);
        });
        
        return label;
    }
    
    private CheckBox createCheckBox(String text, boolean selected) {
        CheckBox cb = new CheckBox(text);
        cb.setSelected(selected);
        cb.setFont(Font.font("Arial", 13));
        cb.setTextFill(Color.web(TEXT_COLOR, 0.9));
        cb.setStyle("-fx-cursor: hand;");
        return cb;
    }
    
    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-border-color: #404060;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        combo.setPrefWidth(180);
    }
    
    private HBox createButtonRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(10, 0, 0, 0));
        
        Button applyBtn = new Button("Apply");
        applyBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        applyBtn.setPrefWidth(100);
        applyBtn.setPrefHeight(38);
        applyBtn.setStyle(
            "-fx-background-color: " + ACCENT_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        applyBtn.setOnAction(e -> applySettings());
        
        Button resetBtn = new Button("Reset");
        resetBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        resetBtn.setPrefWidth(100);
        resetBtn.setPrefHeight(38);
        resetBtn.setStyle(
            "-fx-background-color: #804040;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        resetBtn.setOnAction(e -> resetToDefaults());
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(38);
        cancelBtn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> {
            if (onClose != null) onClose.run();
        });
        
        row.getChildren().addAll(applyBtn, resetBtn, cancelBtn);
        return row;
    }
    
    private void applySettings() {
        // Apply display mode
        String modeStr = displayModeCombo.getValue();
        settings.setDisplayMode(GameSettings.DisplayMode.fromString(modeStr));
        
        // Apply resolution
        String resStr = resolutionCombo.getValue();
        settings.setResolution(GameSettings.Resolution.fromString(resStr));
        
        // Apply other settings
        settings.setVsync(vsyncCheck.isSelected());
        settings.setMusicVolume((int) musicSlider.getValue());
        settings.setSfxVolume((int) sfxSlider.getValue());
        settings.setAutoSave(autoSaveCheck.isSelected());
        settings.setAutoSaveInterval(autoSaveIntervalSpinner.getValue());
        settings.setScrollSpeed(scrollSpeedSlider.getValue() / 100.0);
        settings.setZoomSpeed(zoomSpeedSlider.getValue() / 100.0);
        settings.setMoveToInteract(moveToInteractCheck.isSelected());
        
        // Save to file
        settings.save();
        
        // Apply window settings if we have access to the stage
        if (primaryStage != null) {
            applyWindowSettings();
        }
        
        if (onClose != null) onClose.run();
    }
    
    private void applyWindowSettings() {
        GameSettings.Resolution res = settings.getResolution();
        GameSettings.DisplayMode mode = settings.getDisplayMode();
        
        switch (mode) {
            case FULLSCREEN:
                primaryStage.setFullScreen(true);
                break;
            case WINDOWED_FULLSCREEN:
                primaryStage.setFullScreen(false);
                primaryStage.setMaximized(true);
                break;
            case WINDOWED:
                primaryStage.setFullScreen(false);
                primaryStage.setMaximized(false);
                primaryStage.setWidth(res.width);
                primaryStage.setHeight(res.height);
                primaryStage.centerOnScreen();
                break;
        }
    }
    
    private void resetToDefaults() {
        settings.resetToDefaults();
        loadCurrentSettings();
    }
    
    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }
    
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }
}
