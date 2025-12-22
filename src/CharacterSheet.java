import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Character Sheet UI - Shows player stats, attributes, and body part health.
 * Draggable window displaying character information.
 */
public class CharacterSheet extends DraggableWindow {
    
    // Style constants
    private static final String DARK_BG = "#1a1510";
    private static final String ACCENT_COLOR = "#c4a574";
    private static final String TEXT_COLOR = "#d4c4a4";
    
    // Player reference
    private PlayerSprite player;
    
    // UI elements for updating
    private Label healthLabel;
    private ProgressBar healthBar;
    private VBox bodyPartsBox;
    private VBox statsBox;
    
    public CharacterSheet(PlayerSprite player) {
        super("📜 " + player.getName(), 260, 420);
        this.player = player;
        
        createContent();
    }
    
    private void createContent() {
        VBox content = getContentArea();
        content.setSpacing(10);
        
        // Player portrait section
        content.getChildren().add(createPortraitSection());
        
        // Health overview
        content.getChildren().add(createHealthSection());
        
        // Body parts health
        content.getChildren().add(createBodyPartsSection());
        
        // Attributes
        content.getChildren().add(createAttributesSection());
        
        // Currency
        content.getChildren().add(createCurrencySection());
    }
    
    private HBox createPortraitSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(5));
        section.setStyle("-fx-background-color: " + DARK_BG + "; -fx-background-radius: 5;");
        
        // Small sprite preview
        Canvas portrait = new Canvas(50, 70);
        GraphicsContext gc = portrait.getGraphicsContext2D();
        gc.setFill(Color.web("#151510"));
        gc.fillRoundRect(0, 0, 50, 70, 5, 5);
        player.render(gc, 0, 5, 45);
        
        // Name and level
        VBox info = new VBox(3);
        
        Label nameLabel = new Label(player.getName());
        nameLabel.setTextFill(Color.web(ACCENT_COLOR));
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label levelLabel = new Label("Level 1 Adventurer");
        levelLabel.setTextFill(Color.web(TEXT_COLOR, 0.7));
        levelLabel.setFont(Font.font("System", 11));
        
        info.getChildren().addAll(nameLabel, levelLabel);
        
        section.getChildren().addAll(portrait, info);
        return section;
    }
    
    private VBox createHealthSection() {
        VBox section = new VBox(5);
        section.setPadding(new Insets(5));
        section.setStyle("-fx-background-color: " + DARK_BG + "; -fx-background-radius: 5;");
        
        Label header = DraggableWindow.createHeader("❤ HEALTH");
        
        HBox healthRow = new HBox(10);
        healthRow.setAlignment(Pos.CENTER_LEFT);
        
        healthBar = new ProgressBar((double) player.getHealth() / player.getMaxHealth());
        healthBar.setPrefWidth(150);
        healthBar.setStyle(
            "-fx-accent: #c04040;" +
            "-fx-background-color: #301010;" +
            "-fx-background-radius: 3;"
        );
        
        healthLabel = new Label(player.getHealth() + " / " + player.getMaxHealth());
        healthLabel.setTextFill(Color.web(TEXT_COLOR));
        healthLabel.setFont(Font.font("System", 11));
        
        healthRow.getChildren().addAll(healthBar, healthLabel);
        section.getChildren().addAll(header, healthRow);
        
        return section;
    }
    
    private VBox createBodyPartsSection() {
        bodyPartsBox = new VBox(4);
        bodyPartsBox.setPadding(new Insets(5));
        bodyPartsBox.setStyle("-fx-background-color: " + DARK_BG + "; -fx-background-radius: 5;");
        
        Label header = DraggableWindow.createHeader("🦴 BODY CONDITION");
        bodyPartsBox.getChildren().add(header);
        
        // Add each body part
        for (BodyPart.Type type : BodyPart.Type.values()) {
            bodyPartsBox.getChildren().add(createBodyPartRow(type));
        }
        
        return bodyPartsBox;
    }
    
    private HBox createBodyPartRow(BodyPart.Type type) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        
        BodyPart part = player.getBodyPart(type);
        
        // Part name
        Label nameLabel = new Label(type.getDisplayName());
        nameLabel.setTextFill(Color.web(TEXT_COLOR));
        nameLabel.setFont(Font.font("System", 10));
        nameLabel.setPrefWidth(70);
        
        // Health bar
        ProgressBar partBar = new ProgressBar(part.getHealthPercent());
        partBar.setPrefWidth(80);
        partBar.setPrefHeight(10);
        
        // Color based on health
        String barColor;
        if (part.isBroken()) {
            barColor = "#404040";
        } else if (part.isCrippled()) {
            barColor = "#a04040";
        } else if (part.getHealthPercent() < 0.5) {
            barColor = "#a0a040";
        } else {
            barColor = "#40a040";
        }
        partBar.setStyle("-fx-accent: " + barColor + "; -fx-background-color: #202020;");
        
        // Status
        Label statusLabel = new Label(part.getStatusText());
        statusLabel.setFont(Font.font("System", 9));
        if (part.isBroken()) {
            statusLabel.setTextFill(Color.web("#808080"));
        } else if (part.isCrippled()) {
            statusLabel.setTextFill(Color.web("#ff6060"));
        } else {
            statusLabel.setTextFill(Color.web("#80c080"));
        }
        statusLabel.setPrefWidth(50);
        
        row.getChildren().addAll(nameLabel, partBar, statusLabel);
        return row;
    }
    
    private VBox createAttributesSection() {
        statsBox = new VBox(4);
        statsBox.setPadding(new Insets(5));
        statsBox.setStyle("-fx-background-color: " + DARK_BG + "; -fx-background-radius: 5;");
        
        Label header = DraggableWindow.createHeader("📊 STATS");
        statsBox.getChildren().add(header);
        
        // Calculate stats from equipment
        int totalDefense = player.getTotalArmor();
        int totalAttack = 0;
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            Equipment eq = player.getEquipment(slot);
            if (eq != null) totalAttack += eq.getAttack();
        }
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(3);
        
        statsGrid.add(createStatLabel("Defense:", true), 0, 0);
        statsGrid.add(createStatLabel(String.valueOf(totalDefense), false), 1, 0);
        
        statsGrid.add(createStatLabel("Attack:", true), 0, 1);
        statsGrid.add(createStatLabel(String.valueOf(totalAttack), false), 1, 1);
        
        statsGrid.add(createStatLabel("Speed:", true), 0, 2);
        String speedMod = player.hasAnyCrippledPart() ? "Slowed" : "Normal";
        statsGrid.add(createStatLabel(speedMod, false), 1, 2);
        
        statsBox.getChildren().add(statsGrid);
        
        return statsBox;
    }
    
    private Label createStatLabel(String text, boolean isName) {
        Label label = new Label(text);
        label.setFont(Font.font("System", isName ? FontWeight.NORMAL : FontWeight.BOLD, 11));
        label.setTextFill(Color.web(isName ? TEXT_COLOR : ACCENT_COLOR));
        return label;
    }
    
    private HBox createCurrencySection() {
        HBox section = new HBox(10);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(8, 5, 5, 5));
        section.setStyle("-fx-background-color: " + DARK_BG + "; -fx-background-radius: 5;");
        
        Currency currency = player.getCurrency();
        
        // Gold
        Label goldLabel = new Label("🪙 " + currency.getGold() + "g");
        goldLabel.setTextFill(Color.web("#ffd700"));
        goldLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        // Silver
        Label silverLabel = new Label("⚪ " + currency.getSilver() + "s");
        silverLabel.setTextFill(Color.web("#c0c0c0"));
        silverLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        // Copper
        Label copperLabel = new Label("🔶 " + currency.getCopper() + "c");
        copperLabel.setTextFill(Color.web("#b87333"));
        copperLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        section.getChildren().addAll(goldLabel, silverLabel, copperLabel);
        
        return section;
    }
    
    /**
     * Refresh all displayed data
     */
    public void refresh() {
        // Update health
        healthBar.setProgress((double) player.getHealth() / player.getMaxHealth());
        healthLabel.setText(player.getHealth() + " / " + player.getMaxHealth());
        
        // Rebuild body parts section
        bodyPartsBox.getChildren().clear();
        bodyPartsBox.getChildren().add(DraggableWindow.createHeader("🦴 BODY CONDITION"));
        for (BodyPart.Type type : BodyPart.Type.values()) {
            bodyPartsBox.getChildren().add(createBodyPartRow(type));
        }
        
        // Rebuild stats section  
        statsBox.getChildren().clear();
        statsBox.getChildren().add(DraggableWindow.createHeader("📊 STATS"));
        
        int totalDefense = player.getTotalArmor();
        int totalAttack = 0;
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            Equipment eq = player.getEquipment(slot);
            if (eq != null) totalAttack += eq.getAttack();
        }
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(3);
        statsGrid.add(createStatLabel("Defense:", true), 0, 0);
        statsGrid.add(createStatLabel(String.valueOf(totalDefense), false), 1, 0);
        statsGrid.add(createStatLabel("Attack:", true), 0, 1);
        statsGrid.add(createStatLabel(String.valueOf(totalAttack), false), 1, 1);
        statsGrid.add(createStatLabel("Speed:", true), 0, 2);
        statsGrid.add(createStatLabel(player.hasAnyCrippledPart() ? "Slowed" : "Normal", false), 1, 2);
        
        statsBox.getChildren().add(statsGrid);
    }
}
