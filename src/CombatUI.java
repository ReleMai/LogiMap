import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

/**
 * UI for displaying combat encounters and results.
 */
public class CombatUI extends StackPane {
    
    private static final String DARK_BG = "#1a1510";
    private static final String MEDIUM_BG = "#2a2015";
    private static final String ACCENT_COLOR = "#c4a574";
    private static final String TEXT_COLOR = "#e8e0d0";
    private static final String DANGER_COLOR = "#ff6666";
    private static final String SUCCESS_COLOR = "#66ff66";
    
    private Combat combat;
    private VBox mainPanel;
    private VBox combatLogBox;
    private Label statusLabel;
    private HBox buttonRow;
    
    private Runnable onClose;
    private Runnable onVictory;
    private Runnable onDefeat;
    
    public CombatUI() {
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        setAlignment(Pos.CENTER);
        setVisible(false);
        
        createUI();
    }
    
    private void createUI() {
        mainPanel = new VBox(15);
        mainPanel.setAlignment(Pos.TOP_CENTER);
        mainPanel.setPadding(new Insets(25));
        mainPanel.setMaxWidth(500);
        mainPanel.setMaxHeight(600);
        mainPanel.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;"
        );
        
        // Header
        Label titleLabel = new Label("⚔ COMBAT ⚔");
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(ACCENT_COLOR));
        
        // Status
        statusLabel = new Label("Battle in progress...");
        statusLabel.setFont(Font.font("Georgia", 14));
        statusLabel.setTextFill(Color.web(TEXT_COLOR));
        
        // Combat log
        combatLogBox = new VBox(5);
        combatLogBox.setPadding(new Insets(10));
        combatLogBox.setStyle("-fx-background-color: " + DARK_BG + "; -fx-background-radius: 8;");
        
        ScrollPane scrollPane = new ScrollPane(combatLogBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Buttons
        buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);
        
        Button attackBtn = createButton("⚔ Attack", () -> {
            if (combat != null && combat.isInCombat()) {
                combat.executeRound();
                combat.executeRound(); // Execute both player and enemy turns
                updateDisplay();
            }
        });
        
        Button fleeBtn = createButton("🏃 Flee", () -> {
            if (combat != null && combat.isInCombat()) {
                combat.attemptFlee();
                updateDisplay();
            }
        });
        
        Button closeBtn = createButton("Close", () -> {
            setVisible(false);
            if (onClose != null) onClose.run();
            
            // Trigger victory/defeat callbacks
            if (combat != null) {
                if (combat.getResult() == Combat.Result.VICTORY && onVictory != null) {
                    onVictory.run();
                } else if (combat.getResult() == Combat.Result.DEFEAT && onDefeat != null) {
                    onDefeat.run();
                }
            }
        });
        closeBtn.setVisible(false);
        
        buttonRow.getChildren().addAll(attackBtn, fleeBtn, closeBtn);
        
        mainPanel.getChildren().addAll(titleLabel, statusLabel, scrollPane, buttonRow);
        getChildren().add(mainPanel);
    }
    
    private Button createButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        btn.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 10 20;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-text-fill: " + ACCENT_COLOR + ";" +
            "-fx-padding: 10 20;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 10 20;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnAction(e -> action.run());
        return btn;
    }
    
    /**
     * Starts a new combat encounter.
     */
    public void startCombat(Combat combat) {
        this.combat = combat;
        combatLogBox.getChildren().clear();
        updateDisplay();
        setVisible(true);
    }
    
    /**
     * Updates the display with current combat state.
     */
    private void updateDisplay() {
        if (combat == null) return;
        
        // Update combat log
        combatLogBox.getChildren().clear();
        for (String log : combat.getCombatLog()) {
            Label logLabel = new Label(log);
            logLabel.setFont(Font.font("Georgia", 12));
            logLabel.setTextFill(Color.web(TEXT_COLOR));
            logLabel.setWrapText(true);
            combatLogBox.getChildren().add(logLabel);
        }
        
        // Update status
        if (combat.getResult() == Combat.Result.IN_PROGRESS) {
            statusLabel.setText("Round " + combat.getRound() + " - " + 
                countLivingEnemies() + " enemies remaining");
            statusLabel.setTextFill(Color.web(TEXT_COLOR));
        } else {
            updateResultDisplay();
        }
    }
    
    /**
     * Updates display for combat result.
     */
    private void updateResultDisplay() {
        // Hide combat buttons, show close button
        buttonRow.getChildren().get(0).setVisible(false); // Attack
        buttonRow.getChildren().get(1).setVisible(false); // Flee
        buttonRow.getChildren().get(2).setVisible(true);  // Close
        
        switch (combat.getResult()) {
            case VICTORY:
                statusLabel.setText("VICTORY! +" + combat.getExperienceGained() + " EXP, +" + 
                    combat.getGoldDropped() + " gold");
                statusLabel.setTextFill(Color.web(SUCCESS_COLOR));
                break;
            case DEFEAT:
                statusLabel.setText("DEFEATED! You have fallen in battle.");
                statusLabel.setTextFill(Color.web(DANGER_COLOR));
                break;
            case FLED:
                statusLabel.setText("ESCAPED! You fled from combat.");
                statusLabel.setTextFill(Color.web(ACCENT_COLOR));
                break;
            default:
                break;
        }
    }
    
    /**
     * Counts living enemies.
     */
    private int countLivingEnemies() {
        if (combat == null) return 0;
        int count = 0;
        for (Enemy e : combat.getEnemies()) {
            if (e.getCurrentHealth() > 0) count++;
        }
        return count;
    }
    
    // Setters for callbacks
    public void setOnClose(Runnable callback) { this.onClose = callback; }
    public void setOnVictory(Runnable callback) { this.onVictory = callback; }
    public void setOnDefeat(Runnable callback) { this.onDefeat = callback; }
}
