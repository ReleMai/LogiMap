import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lore/Dialogue popup system for story-driven interactions.
 * Shows narrative text with choice buttons in a medieval scroll style.
 */
public class LoreDialogue extends StackPane {
    
    // UI Components
    private VBox dialoguePanel;
    private Label titleLabel;
    private Label loreTextLabel;
    private VBox choicesContainer;
    
    // State
    private List<DialogueChoice> currentChoices = new ArrayList<>();
    private Consumer<Integer> onChoiceSelected;
    
    // Style constants - Parchment/scroll theme
    private static final String SCROLL_BG = "#d4c4a0";
    private static final String SCROLL_DARK = "#b8a878";
    private static final String SCROLL_BORDER = "#8b7355";
    private static final String INK_COLOR = "#2a1a08";
    private static final String ACCENT_COLOR = "#8b0000";
    private static final String BUTTON_BG = "#c4b080";
    private static final String BUTTON_HOVER = "#a89860";
    
    public LoreDialogue() {
        // Darkened background overlay
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.color(0, 0, 0, 0.7));
        overlay.widthProperty().bind(this.widthProperty());
        overlay.heightProperty().bind(this.heightProperty());
        
        // Create dialogue panel (scroll appearance)
        dialoguePanel = createDialoguePanel();
        
        this.getChildren().addAll(overlay, dialoguePanel);
        this.setVisible(false);
        this.setAlignment(Pos.CENTER);
    }
    
    /**
     * Creates the scroll-styled dialogue panel.
     */
    private VBox createDialoguePanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30, 40, 30, 40));
        panel.setMaxWidth(600);
        panel.setMaxHeight(500);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + SCROLL_BG + ", " + SCROLL_DARK + ");" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + SCROLL_BORDER + ";" +
            "-fx-border-width: 4;" +
            "-fx-border-radius: 5;"
        );
        
        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(25);
        shadow.setColor(Color.color(0, 0, 0, 0.8));
        panel.setEffect(shadow);
        
        // Decorative top border
        Rectangle topDecor = new Rectangle(400, 3);
        topDecor.setFill(Color.web(SCROLL_BORDER));
        
        // Title
        titleLabel = new Label("A New Arrival");
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.web(ACCENT_COLOR));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Decorative separator
        Rectangle separator = new Rectangle(200, 2);
        separator.setFill(Color.web(SCROLL_BORDER));
        
        // Lore text
        loreTextLabel = new Label();
        loreTextLabel.setFont(Font.font("Georgia", FontWeight.NORMAL, 16));
        loreTextLabel.setTextFill(Color.web(INK_COLOR));
        loreTextLabel.setWrapText(true);
        loreTextLabel.setTextAlignment(TextAlignment.CENTER);
        loreTextLabel.setMaxWidth(500);
        loreTextLabel.setLineSpacing(4);
        
        // Choices container
        choicesContainer = new VBox(12);
        choicesContainer.setAlignment(Pos.CENTER);
        choicesContainer.setPadding(new Insets(15, 0, 0, 0));
        
        // Decorative bottom border
        Rectangle bottomDecor = new Rectangle(400, 3);
        bottomDecor.setFill(Color.web(SCROLL_BORDER));
        
        panel.getChildren().addAll(topDecor, titleLabel, separator, loreTextLabel, choicesContainer, bottomDecor);
        
        return panel;
    }
    
    /**
     * Shows a dialogue with the given content and choices.
     */
    public void show(String title, String loreText, List<DialogueChoice> choices) {
        this.currentChoices = choices;
        
        // Update content
        titleLabel.setText(title);
        loreTextLabel.setText(loreText);
        
        // Clear and add choices
        choicesContainer.getChildren().clear();
        for (int i = 0; i < choices.size(); i++) {
            final int choiceIndex = i;
            DialogueChoice choice = choices.get(i);
            
            Button choiceBtn = createChoiceButton(choice.getText());
            choiceBtn.setOnAction(e -> handleChoice(choiceIndex, choice));
            
            choicesContainer.getChildren().add(choiceBtn);
        }
        
        // Show with animation
        this.setVisible(true);
        
        // Scale animation
        dialoguePanel.setScaleX(0.85);
        dialoguePanel.setScaleY(0.85);
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), dialoguePanel);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
        
        // Fade animation
        this.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(250), this);
        fade.setToValue(1.0);
        fade.play();
    }
    
    /**
     * Shows a simple castle arrival dialogue.
     */
    public void showCastleArrival(String lordName, String castleName, Runnable onEnter, Runnable onLeave) {
        List<DialogueChoice> choices = new ArrayList<>();
        choices.add(new DialogueChoice("\"I request entry into the castle.\"", () -> {
            close();
            if (onEnter != null) onEnter.run();
        }));
        choices.add(new DialogueChoice("\"Forgive me, I must be on my way.\"", () -> {
            close();
            if (onLeave != null) onLeave.run();
        }));
        
        String loreText = String.format(
            "You have arrived at %s, the castle of Lord %s.\n\n" +
            "As you approach the imposing stone gates, armored guards step forward, " +
            "their halberds crossed before the entrance.\n\n" +
            "\"Halt, traveler! State your business at the castle.\"",
            castleName, lordName
        );
        
        show("⚔ " + castleName + " ⚔", loreText, choices);
    }
    
    /**
     * Shows a town arrival dialogue.
     */
    public void showTownArrival(String townName, boolean isMajor, Runnable onEnter, Runnable onLeave) {
        List<DialogueChoice> choices = new ArrayList<>();
        choices.add(new DialogueChoice("Enter " + townName, () -> {
            close();
            if (onEnter != null) onEnter.run();
        }));
        choices.add(new DialogueChoice("Continue on your journey", () -> {
            close();
            if (onLeave != null) onLeave.run();
        }));
        
        String loreText;
        if (isMajor) {
            loreText = String.format(
                "The bustling town of %s rises before you.\n\n" +
                "Merchants hawk their wares in the crowded streets, " +
                "and the smell of fresh bread wafts from nearby bakeries. " +
                "Town guards patrol the main square, keeping order among the throng of travelers.\n\n" +
                "The town gates stand open, welcoming all who seek trade or respite.",
                townName
            );
        } else {
            loreText = String.format(
                "You arrive at the small settlement of %s.\n\n" +
                "A few modest buildings cluster around a central well. " +
                "Farmers tend their fields nearby, and a weathered sign points toward a humble inn.\n\n" +
                "The villagers eye you with curiosity.",
                townName
            );
        }
        
        show(isMajor ? "🏰 " + townName + " 🏰" : "🏠 " + townName, loreText, choices);
    }
    
    /**
     * Creates a styled choice button.
     */
    private Button createChoiceButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.NORMAL, 14));
        btn.setTextFill(Color.web(INK_COLOR));
        btn.setPadding(new Insets(12, 25, 12, 25));
        btn.setMaxWidth(450);
        btn.setMinWidth(300);
        btn.setWrapText(true);
        btn.setTextAlignment(TextAlignment.CENTER);
        
        String baseStyle = 
            "-fx-background-color: " + BUTTON_BG + ";" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + SCROLL_BORDER + ";" +
            "-fx-border-radius: 5;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;";
        
        String hoverStyle = 
            "-fx-background-color: " + BUTTON_HOVER + ";" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-radius: 5;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;";
        
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        
        return btn;
    }
    
    /**
     * Handles a choice selection.
     */
    private void handleChoice(int index, DialogueChoice choice) {
        if (choice.getAction() != null) {
            choice.getAction().run();
        }
        if (onChoiceSelected != null) {
            onChoiceSelected.accept(index);
        }
    }
    
    /**
     * Closes the dialogue with animation.
     */
    public void close() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), dialoguePanel);
        scale.setToX(0.9);
        scale.setToY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(150), this);
        fade.setToValue(0);
        fade.setOnFinished(e -> this.setVisible(false));
        
        scale.play();
        fade.play();
    }
    
    /**
     * Sets the callback for when a choice is selected.
     */
    public void setOnChoiceSelected(Consumer<Integer> handler) {
        this.onChoiceSelected = handler;
    }
    
    /**
     * Checks if the dialogue is currently visible.
     */
    public boolean isShowing() {
        return this.isVisible();
    }
    
    /**
     * Represents a dialogue choice option.
     */
    public static class DialogueChoice {
        private final String text;
        private final Runnable action;
        
        public DialogueChoice(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
        
        public String getText() { return text; }
        public Runnable getAction() { return action; }
    }
}
