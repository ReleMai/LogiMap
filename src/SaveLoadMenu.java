import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;

/**
 * Save/Load Menu UI - Provides interface for saving and loading game worlds.
 */
public class SaveLoadMenu extends StackPane {
    
    // Style constants
    private static final String DARK_BG = "#1a1a1a";
    private static final String MEDIUM_BG = "#2a2a2a";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String TEXT_COLOR = "#e0e0e0";
    private static final String PANEL_BORDER = "#404040";
    private static final String DANGER_COLOR = "#ff4444";
    
    // UI Components
    private VBox saveList;
    private TextField saveNameField;
    private Label statusLabel;
    
    // Current world info
    private DemoWorld currentWorld;
    private String currentWorldName;
    private int viewX, viewY;
    private double zoom;
    
    // Callbacks
    private Consumer<WorldSaveManager.LoadResult> onLoadWorld;
    private Runnable onClose;
    
    // Mode
    private boolean saveMode;
    
    public SaveLoadMenu(boolean saveMode) {
        this.saveMode = saveMode;
        
        setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        setAlignment(Pos.CENTER);
        
        VBox mainPanel = createMainPanel();
        getChildren().add(mainPanel);
        
        // Close when clicking outside
        setOnMouseClicked(e -> {
            if (e.getTarget() == this && onClose != null) {
                onClose.run();
            }
        });
        
        refreshSaveList();
    }
    
    private VBox createMainPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setMaxWidth(500);
        panel.setMaxHeight(600);
        panel.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 2;"
        );
        
        // Title
        Label title = new Label(saveMode ? "💾 SAVE WORLD" : "📂 LOAD WORLD");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(ACCENT_COLOR));
        
        // Save name input (save mode only)
        VBox saveNameSection = new VBox(5);
        if (saveMode) {
            Label nameLabel = createLabel("Save Name:");
            saveNameField = createTextField("Enter save name...");
            saveNameSection.getChildren().addAll(nameLabel, saveNameField);
        }
        
        // Save list
        Label listLabel = createLabel(saveMode ? "Existing Saves:" : "Available Saves:");
        
        saveList = new VBox(8);
        saveList.setPadding(new Insets(5));
        
        ScrollPane scrollPane = new ScrollPane(saveList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle(
            "-fx-background: " + DARK_BG + ";" +
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-border-color: " + PANEL_BORDER + ";"
        );
        
        // Status label
        statusLabel = createLabel("");
        statusLabel.setStyle(statusLabel.getStyle() + "-fx-font-style: italic;");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        if (saveMode) {
            Button saveButton = createButton("💾 Save", ACCENT_COLOR);
            saveButton.setOnAction(e -> performSave());
            buttonBox.getChildren().add(saveButton);
        }
        
        Button closeButton = createButton("✖ Close", LIGHT_BG);
        closeButton.setOnAction(e -> {
            if (onClose != null) onClose.run();
        });
        buttonBox.getChildren().add(closeButton);
        
        panel.getChildren().addAll(title);
        if (saveMode) {
            panel.getChildren().add(saveNameSection);
        }
        panel.getChildren().addAll(listLabel, scrollPane, statusLabel, buttonBox);
        
        return panel;
    }
    
    private void refreshSaveList() {
        saveList.getChildren().clear();
        
        List<WorldSaveManager.SaveInfo> saves = WorldSaveManager.listSaves();
        
        if (saves.isEmpty()) {
            Label noSaves = createLabel("No saved worlds found.");
            noSaves.setStyle(noSaves.getStyle() + "-fx-opacity: 0.6;");
            saveList.getChildren().add(noSaves);
            return;
        }
        
        for (WorldSaveManager.SaveInfo info : saves) {
            HBox saveRow = createSaveRow(info);
            saveList.getChildren().add(saveRow);
        }
    }
    
    private HBox createSaveRow(WorldSaveManager.SaveInfo info) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 6;"
        );
        
        // World info
        VBox infoBox = new VBox(3);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label nameLabel = new Label(info.worldName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label detailsLabel = new Label(String.format(
            "Seed: %d | Structures: %d | %s",
            info.seed, info.structureCount, info.getFormattedTime()
        ));
        detailsLabel.setFont(Font.font("System", 11));
        detailsLabel.setTextFill(Color.web(TEXT_COLOR, 0.7));
        
        infoBox.getChildren().addAll(nameLabel, detailsLabel);
        
        // Action buttons
        HBox buttonBox = new HBox(5);
        
        if (!saveMode) {
            Button loadBtn = createSmallButton("📂", "Load this world");
            loadBtn.setOnAction(e -> performLoad(info.filename));
            buttonBox.getChildren().add(loadBtn);
        }
        
        Button deleteBtn = createSmallButton("🗑", "Delete save");
        deleteBtn.setStyle(deleteBtn.getStyle().replace(LIGHT_BG, "#553333"));
        deleteBtn.setOnAction(e -> {
            if (confirmDelete(info.worldName)) {
                WorldSaveManager.deleteSave(info.filename);
                refreshSaveList();
                setStatus("Deleted: " + info.worldName, false);
            }
        });
        buttonBox.getChildren().add(deleteBtn);
        
        row.getChildren().addAll(infoBox, buttonBox);
        
        // Double-click to load
        if (!saveMode) {
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    performLoad(info.filename);
                }
            });
        } else {
            // In save mode, clicking fills the name field
            row.setOnMouseClicked(e -> {
                saveNameField.setText(info.worldName);
            });
        }
        
        return row;
    }
    
    private void performSave() {
        if (currentWorld == null) {
            setStatus("Error: No world to save!", true);
            return;
        }
        
        String saveName = saveNameField.getText().trim();
        if (saveName.isEmpty()) {
            saveName = currentWorldName != null ? currentWorldName : "Unnamed World";
        }
        
        boolean success = WorldSaveManager.saveWorld(
            currentWorld, saveName, viewX, viewY, zoom
        );
        
        if (success) {
            setStatus("World saved successfully!", false);
            refreshSaveList();
        } else {
            setStatus("Failed to save world!", true);
        }
    }
    
    private void performLoad(String filename) {
        WorldSaveManager.LoadResult result = WorldSaveManager.loadWorld(filename);
        
        if (result != null && onLoadWorld != null) {
            setStatus("Loading world...", false);
            onLoadWorld.accept(result);
        } else {
            setStatus("Failed to load world!", true);
        }
    }
    
    private boolean confirmDelete(String worldName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Save");
        alert.setHeaderText("Delete \"" + worldName + "\"?");
        alert.setContentText("This action cannot be undone.");
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.web(DANGER_COLOR) : Color.web(ACCENT_COLOR));
    }
    
    // ==================== UI Helper Methods ====================
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web(TEXT_COLOR));
        return label;
    }
    
    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-border-color: " + PANEL_BORDER + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8;"
        );
        return field;
    }
    
    private Button createButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", FontWeight.BOLD, 14));
        btn.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }
    
    private Button createSmallButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 5 10 5 10;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }
    
    // ==================== Public API ====================
    
    public void setCurrentWorld(DemoWorld world, String worldName, int viewX, int viewY, double zoom) {
        this.currentWorld = world;
        this.currentWorldName = worldName;
        this.viewX = viewX;
        this.viewY = viewY;
        this.zoom = zoom;
        
        if (saveMode && saveNameField != null) {
            saveNameField.setText(worldName);
        }
    }
    
    public void setOnLoadWorld(Consumer<WorldSaveManager.LoadResult> callback) {
        this.onLoadWorld = callback;
    }
    
    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }
}
