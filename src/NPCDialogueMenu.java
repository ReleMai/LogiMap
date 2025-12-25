import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dialogue menu that appears when interacting with an NPC.
 * Shows NPC info, dialogue options, and tracks relationship.
 * When player has party members, displays a sidebar to switch between talking to different members.
 */
public class NPCDialogueMenu extends StackPane {
    
    // Colors
    private static final String DARK_BG = "#1a1a1a";
    private static final String MEDIUM_BG = "#2a2a2a";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String GOLD_COLOR = "#ffd700";
    private static final String TEXT_COLOR = "#e0e0e0";
    private static final String SELECTED_COLOR = "#3a5a8a";
    
    private HBox mainContainer;
    private VBox partyListPanel;
    private VBox dialoguePanel;
    private ScrollPane dialogueScrollPane;
    private Label npcNameLabel;
    private Label npcTypeLabel;
    private Label npcActionLabel;
    private VBox dialogueHistory;
    private VBox optionsBox;
    private Pane overlay;
    
    private NPC currentNPC;
    private PartyMember currentPartyMember;
    private Consumer<NPC> onClose;
    private RelationshipManager relationshipManager;
    private PlayerSprite player;
    private GameTime gameTime;
    private boolean wasGamePaused = false;
    
    public NPCDialogueMenu(RelationshipManager relationshipManager) {
        this.relationshipManager = relationshipManager;
        setVisible(false);
        setPickOnBounds(false);
        
        createUI();
    }
    
    public void setPlayer(PlayerSprite player) {
        this.player = player;
    }
    
    public void setGameTime(GameTime gameTime) {
        this.gameTime = gameTime;
    }
    
    private void createUI() {
        // Semi-transparent overlay
        overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        overlay.setOnMouseClicked(e -> close());
        
        // Party list panel (shown on left when party has members)
        partyListPanel = new VBox(8);
        partyListPanel.setMinWidth(160);
        partyListPanel.setMaxWidth(160);
        partyListPanel.setPadding(new Insets(15));
        partyListPanel.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 15 0 0 15;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2 0 2 2;" +
            "-fx-border-radius: 15 0 0 15;"
        );
        partyListPanel.setVisible(false);
        partyListPanel.setManaged(false);
        
        // Main dialogue panel - larger and more dynamic
        dialoguePanel = new VBox(15);
        dialoguePanel.setMinWidth(500);
        dialoguePanel.setMaxWidth(600);
        dialoguePanel.setMinHeight(400);
        dialoguePanel.setMaxHeight(650);
        dialoguePanel.setPadding(new Insets(25));
        dialoguePanel.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);"
        );
        dialoguePanel.setOnMouseClicked(e -> e.consume()); // Don't close when clicking panel
        
        // NPC header section
        HBox header = createHeader();
        
        // Dialogue history scroll area
        dialogueHistory = new VBox(10);
        dialogueHistory.setPadding(new Insets(12));
        
        dialogueScrollPane = new ScrollPane(dialogueHistory);
        dialogueScrollPane.setStyle(
            "-fx-background: " + MEDIUM_BG + ";" +
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-border-color: " + LIGHT_BG + ";" +
            "-fx-border-radius: 8;"
        );
        dialogueScrollPane.setFitToWidth(true);
        dialogueScrollPane.setPrefHeight(280);
        dialogueScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialogueScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(dialogueScrollPane, Priority.ALWAYS);
        
        // Options section - more spacing
        optionsBox = new VBox(10);
        optionsBox.setPadding(new Insets(15, 0, 10, 0));
        
        // Close button
        Button closeButton = createStyledButton("Farewell", "#666666");
        closeButton.setOnAction(e -> close());
        
        dialoguePanel.getChildren().addAll(header, dialogueScrollPane, optionsBox, closeButton);
        
        // Main container holds party list + dialogue panel
        mainContainer = new HBox(0);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().addAll(partyListPanel, dialoguePanel);
        mainContainer.setOnMouseClicked(e -> e.consume());
        
        // Center the container
        StackPane.setAlignment(mainContainer, Pos.CENTER);
        
        getChildren().addAll(overlay, mainContainer);
    }
    
    /**
     * Updates the party list panel with current party members.
     * Shows the panel only if the player has party members.
     */
    private void updatePartyListPanel() {
        partyListPanel.getChildren().clear();
        
        if (player == null || player.getParty() == null || player.getParty().getMembers().isEmpty()) {
            partyListPanel.setVisible(false);
            partyListPanel.setManaged(false);
            // Reset dialogue panel border radius to full
            dialoguePanel.setStyle(
                "-fx-background-color: " + DARK_BG + ";" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: " + ACCENT_COLOR + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);"
            );
            return;
        }
        
        // Show party panel
        partyListPanel.setVisible(true);
        partyListPanel.setManaged(true);
        
        // Adjust dialogue panel border radius when party panel is shown
        dialoguePanel.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 0 15 15 0;" +
            "-fx-border-color: " + ACCENT_COLOR + ";" +
            "-fx-border-width: 2 2 2 0;" +
            "-fx-border-radius: 0 15 15 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 5);"
        );
        
        // Header for party list
        Label partyHeader = new Label("🎭 Party");
        partyHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        partyHeader.setTextFill(Color.web(GOLD_COLOR));
        partyHeader.setPadding(new Insets(0, 0, 8, 0));
        partyListPanel.getChildren().add(partyHeader);
        
        // Add NPC button at the top if we started with an NPC
        if (currentNPC != null || currentPartyMember == null) {
            Button npcBtn = createNPCButton();
            partyListPanel.getChildren().add(npcBtn);
            
            // Add separator
            Region separator = new Region();
            separator.setPrefHeight(8);
            partyListPanel.getChildren().add(separator);
        }
        
        // Add a button for each party member
        List<PartyMember> members = player.getParty().getMembers();
        for (PartyMember member : members) {
            Button memberBtn = createPartyMemberButton(member);
            partyListPanel.getChildren().add(memberBtn);
        }
    }
    
    /**
     * Creates a styled button for the current NPC.
     */
    private Button createNPCButton() {
        String name = currentNPC != null ? currentNPC.getName() : "Unknown";
        String shortName = name.length() > 10 ? name.substring(0, 10) + "..." : name;
        
        Button btn = new Button("👤 " + shortName);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        
        // Highlight if this is the currently selected (NPC selected when no party member is)
        boolean isSelected = (currentPartyMember == null);
        String bgColor = isSelected ? SELECTED_COLOR : LIGHT_BG;
        
        btn.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 8 10;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            if (currentPartyMember != null) {
                btn.setStyle(
                    "-fx-background-color: " + MEDIUM_BG + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 8 10;" +
                    "-fx-background-radius: 5;" +
                    "-fx-cursor: hand;"
                );
            }
        });
        
        btn.setOnMouseExited(e -> {
            boolean selected = (currentPartyMember == null);
            String bg = selected ? SELECTED_COLOR : LIGHT_BG;
            btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-padding: 8 10;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;"
            );
        });
        
        btn.setOnAction(e -> switchBackToNPC());
        
        return btn;
    }
    
    /**
     * Switches back to talking with the original NPC.
     */
    private void switchBackToNPC() {
        if (currentNPC == null || currentPartyMember == null) {
            return; // Already talking to NPC
        }
        
        currentPartyMember = null;
        
        // Clear dialogue history
        dialogueHistory.getChildren().clear();
        
        // Update header for NPC
        npcNameLabel.setText(currentNPC.getName());
        npcTypeLabel.setText(currentNPC.getType().getName());
        npcActionLabel.setText("Currently: " + formatAction(currentNPC.getCurrentAction()));
        
        // Update party list to show selection
        updatePartyListPanel();
        
        // Add greeting from NPC
        addNPCDialogue("Yes? What did you need?");
        
        // Setup NPC dialogue options
        setupDialogueOptions();
    }
    
    /**
     * Creates a styled button for a party member.
     */
    private Button createPartyMemberButton(PartyMember member) {
        String icon = member.getRole().getIcon();
        String name = member.getName();
        String shortName = name.length() > 10 ? name.substring(0, 10) + "..." : name;
        
        Button btn = new Button(icon + " " + shortName);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        
        // Highlight if this is the currently selected party member
        boolean isSelected = (currentPartyMember != null && currentPartyMember.equals(member));
        String bgColor = isSelected ? SELECTED_COLOR : LIGHT_BG;
        
        btn.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-padding: 8 10;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            if (currentPartyMember == null || !currentPartyMember.equals(member)) {
                btn.setStyle(
                    "-fx-background-color: " + MEDIUM_BG + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 8 10;" +
                    "-fx-background-radius: 5;" +
                    "-fx-cursor: hand;"
                );
            }
        });
        
        btn.setOnMouseExited(e -> {
            boolean selected = (currentPartyMember != null && currentPartyMember.equals(member));
            String bg = selected ? SELECTED_COLOR : LIGHT_BG;
            btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-padding: 8 10;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;"
            );
        });
        
        btn.setOnAction(e -> switchToPartyMember(member));
        
        return btn;
    }
    
    /**
     * Switches the dialogue to a different party member.
     */
    private void switchToPartyMember(PartyMember member) {
        if (member.equals(currentPartyMember)) {
            return; // Already talking to this member
        }
        
        currentPartyMember = member;
        currentNPC = null; // Clear NPC selection
        
        // Clear dialogue history
        dialogueHistory.getChildren().clear();
        
        // Update header for party member
        npcNameLabel.setText(member.getName());
        npcTypeLabel.setText(member.getRole().getIcon() + " " + member.getRole().name() + " - Lvl " + member.getLevel());
        npcActionLabel.setText("Party Member");
        
        // Update party list to show selection
        updatePartyListPanel();
        
        // Add greeting from party member
        addNPCDialogue(getPartyMemberGreeting(member));
        
        // Setup party member dialogue options
        setupPartyMemberOptions(member);
    }
    
    /**
     * Gets a greeting message from a party member.
     */
    private String getPartyMemberGreeting(PartyMember member) {
        String[] greetings = {
            "Yes, " + (player != null ? "boss" : "friend") + "?",
            "What do you need?",
            "I'm here. What's up?",
            "Ready when you are.",
            "How can I help?"
        };
        
        switch (member.getRole()) {
            case WARRIOR:
                return "⚔ My sword is ready. What are your orders?";
            case SCOUT:
                return "🏹 The road ahead looks interesting. What's the plan?";
            case HEALER:
                return "✚ Everyone's health is good. Do you need something?";
            case MERCHANT:
                return "💰 Our supplies are in order. What do you wish to discuss?";
            case LABORER:
                return "🔨 Ready to work. What needs doing?";
            default:
                return greetings[(int)(Math.random() * greetings.length)];
        }
    }
    
    /**
     * Sets up dialogue options for a party member.
     */
    private void setupPartyMemberOptions(PartyMember member) {
        optionsBox.getChildren().clear();
        
        // Status check option
        Button statusBtn = createStyledButton("📊 How are you doing?", "#5a7a9a");
        statusBtn.setOnAction(e -> {
            addPlayerDialogue("How are you holding up?");
            String status = String.format("I'm at %d/%d health. My morale is %s.",
                member.getCurrentHealth(), member.getMaxHealth(),
                member.getMorale() > 70 ? "high" : (member.getMorale() > 40 ? "okay" : "low"));
            addNPCDialogue(status);
        });
        optionsBox.getChildren().add(statusBtn);
        
        // Role-specific options
        switch (member.getRole()) {
            case WARRIOR:
                Button combatBtn = createStyledButton("⚔ Tell me about your combat skills", "#8b4513");
                combatBtn.setOnAction(e -> {
                    addPlayerDialogue("Tell me about your combat abilities.");
                    addNPCDialogue(String.format("My attack is %d and defense is %d. I can hold the front line.",
                        member.getAttack(), member.getDefense()));
                });
                optionsBox.getChildren().add(combatBtn);
                break;
                
            case SCOUT:
                Button scoutBtn = createStyledButton("🏹 What have you seen?", "#228b22");
                scoutBtn.setOnAction(e -> {
                    addPlayerDialogue("Seen anything interesting on the road?");
                    addNPCDialogue("The terrain ahead looks manageable. I'll keep my eyes open for danger.");
                });
                optionsBox.getChildren().add(scoutBtn);
                break;
                
            case HEALER:
                Button healBtn = createStyledButton("✚ Can you heal someone?", "#4169e1");
                healBtn.setOnAction(e -> {
                    addPlayerDialogue("Is anyone injured?");
                    addNPCDialogue("Everyone's in decent shape. I'll let you know if someone needs attention.");
                });
                optionsBox.getChildren().add(healBtn);
                break;
                
            case MERCHANT:
                Button tradeBtn = createStyledButton("💰 How are our finances?", "#daa520");
                tradeBtn.setOnAction(e -> {
                    addPlayerDialogue("What's our financial situation?");
                    addNPCDialogue("We're doing fine. I can help negotiate better prices at markets.");
                });
                optionsBox.getChildren().add(tradeBtn);
                break;
                
            case LABORER:
                Button workBtn = createStyledButton("🔨 What can you help with?", "#8b4513");
                workBtn.setOnAction(e -> {
                    addPlayerDialogue("What kind of work can you do?");
                    addNPCDialogue("I can help with construction, repairs, and carrying supplies. Just say the word.");
                });
                optionsBox.getChildren().add(workBtn);
                break;
        }
        
        // Dismiss option
        Button dismissBtn = createStyledButton("👋 You're dismissed from the party", "#aa4444");
        dismissBtn.setOnAction(e -> {
            addPlayerDialogue("I'm letting you go from the party.");
            addNPCDialogue("I understand. Safe travels, then.");
            // Remove from party
            if (player != null && player.getParty() != null) {
                player.getParty().removeMember(member);
                currentPartyMember = null;
                // Update the party list
                updatePartyListPanel();
                // Switch back to original NPC if there was one, or close
                if (player.getParty().getMembers().isEmpty()) {
                    close();
                } else {
                    // Switch to another party member
                    switchToPartyMember(player.getParty().getMembers().get(0));
                }
            }
        });
        optionsBox.getChildren().add(dismissBtn);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: " + LIGHT_BG + "; -fx-border-width: 0 0 1 0;");
        
        // NPC portrait circle
        StackPane portrait = new StackPane();
        Circle portraitBg = new Circle(30);
        portraitBg.setFill(Color.web(MEDIUM_BG));
        portraitBg.setStroke(Color.web(ACCENT_COLOR));
        portraitBg.setStrokeWidth(2);
        
        Label portraitIcon = new Label("👤");
        portraitIcon.setFont(Font.font("Segoe UI Emoji", 28));
        
        portrait.getChildren().addAll(portraitBg, portraitIcon);
        
        // NPC info
        VBox infoBox = new VBox(3);
        
        npcNameLabel = new Label("NPC Name");
        npcNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        npcNameLabel.setTextFill(Color.web(GOLD_COLOR));
        
        npcTypeLabel = new Label("Type");
        npcTypeLabel.setFont(Font.font("Arial", 12));
        npcTypeLabel.setTextFill(Color.web(TEXT_COLOR, 0.8));
        
        npcActionLabel = new Label("Currently: Idle");
        npcActionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        npcActionLabel.setTextFill(Color.web(ACCENT_COLOR));
        
        infoBox.getChildren().addAll(npcNameLabel, npcTypeLabel, npcActionLabel);
        
        // Relationship indicator
        VBox relBox = new VBox(2);
        relBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(relBox, Priority.ALWAYS);
        
        Label relLabel = new Label("Relationship");
        relLabel.setFont(Font.font("Arial", 10));
        relLabel.setTextFill(Color.web(TEXT_COLOR, 0.6));
        
        Label relValue = new Label("Stranger");
        relValue.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        relValue.setTextFill(Color.web(TEXT_COLOR));
        relValue.setId("relationshipValue");
        
        relBox.getChildren().addAll(relLabel, relValue);
        
        header.getChildren().addAll(portrait, infoBox, relBox);
        return header;
    }
    
    public void show(NPC npc) {
        if (npc == null) return;
        
        this.currentNPC = npc;
        this.currentPartyMember = null; // Clear party member selection when showing NPC
        
        // Pause the game when opening dialogue
        if (gameTime != null) {
            wasGamePaused = gameTime.isPaused();
            if (!wasGamePaused) {
                gameTime.pause();
            }
        }
        
        // Update NPC info
        npcNameLabel.setText(npc.getName());
        npcTypeLabel.setText(npc.getType().getName());
        npcActionLabel.setText("Currently: " + formatAction(npc.getCurrentAction()));
        
        // Show carried item if present
        if (npc.getCarryingResources() > 0 && npc.getCarryingResourceType() != null) {
            String cText = "🎒 " + npc.getCarryingResources() + " x " + npc.getCarryingResourceType();
            Label carry = new Label(cText);
            carry.setFont(Font.font("Arial", 11));
            carry.setTextFill(Color.web("#e8dcc8"));
            carry.setStyle("-fx-background-color: rgba(80,60,40,0.85); -fx-background-radius: 4; -fx-padding: 4 6 4 6;");

            Inventory inv = npc.getInventory();
            if (inv != null && !inv.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack s = inv.getSlot(i);
                    if (s != null && !s.isEmpty()) {
                        sb.append(s.getItem().getName()).append(" x").append(s.getQuantity()).append("\n");
                    }
                }
                if (sb.length() > 0) {
                    Tooltip tip = new Tooltip(sb.toString());
                    Tooltip.install(carry, tip);
                }
            }
            // Add to header row where npcTypeLabel is contained
            if (npcTypeLabel.getParent() instanceof HBox hbox) {
                hbox.getChildren().add(carry);
            }
        }

        // Update relationship status
        updateRelationshipDisplay();
        
        // Clear previous dialogue
        dialogueHistory.getChildren().clear();
        
        // Add greeting based on relationship
        String greeting = npc.getDialogue();
        addNPCDialogue(greeting);
        
        // Track this interaction
        if (relationshipManager != null) {
            relationshipManager.recordInteraction(npc);
        }
        
        // Update party list panel (shows/hides based on party members)
        updatePartyListPanel();
        
        // Setup dialogue options
        setupDialogueOptions();
        
        // Show with animation
        setVisible(true);
        dialoguePanel.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), dialoguePanel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    private void updateRelationshipDisplay() {
        Label relValue = (Label) dialoguePanel.lookup("#relationshipValue");
        if (relValue != null && relationshipManager != null && currentNPC != null) {
            RelationshipManager.RelationshipLevel level = relationshipManager.getRelationshipLevel(currentNPC);
            relValue.setText(level.getDisplayName());
            relValue.setTextFill(Color.web(level.getColor()));
        }
    }
    
    private String formatAction(NPC.NPCAction action) {
        return switch (action) {
            case IDLE -> "Resting";
            case WALKING -> "Walking";
            case WORKING -> "Working";
            case TALKING -> "Talking";
            case SHOPPING -> "Shopping";
            case GATHERING -> "Gathering resources";
            case DELIVERING -> "Making a delivery";
            case PATROLLING -> "On patrol";
            case COLLECTING_TAX -> "Collecting taxes";
        };
    }
    
    private void setupDialogueOptions() {
        optionsBox.getChildren().clear();
        
        // Basic chat option
        Button chatBtn = createStyledButton("💬 Chat", ACCENT_COLOR);
        chatBtn.setOnAction(e -> {
            String response = currentNPC.getDialogue();
            addPlayerDialogue("Let's chat...");
            addNPCDialogue(response);
            if (relationshipManager != null) {
                relationshipManager.modifyRelationship(currentNPC, 1);
                updateRelationshipDisplay();
            }
        });
        
        // Ask about work
        Button workBtn = createStyledButton("⚒ Ask about work", "#8b7355");
        workBtn.setOnAction(e -> {
            addPlayerDialogue("What kind of work do you do?");
            String workResponse = getWorkDialogue();
            addNPCDialogue(workResponse);
        });
        
        // Ask about town
        Button townBtn = createStyledButton("🏘 Ask about town", "#6b8e23");
        townBtn.setOnAction(e -> {
            addPlayerDialogue("Tell me about this place.");
            String townResponse = getTownDialogue();
            addNPCDialogue(townResponse);
            if (relationshipManager != null) {
                relationshipManager.modifyRelationship(currentNPC, 1);
                updateRelationshipDisplay();
            }
        });
        
        // Trade option for merchants and goods transporters
        NPCRole role = currentNPC.getRole();
        if (currentNPC.getType() == NPC.NPCType.MERCHANT || 
            role == NPCRole.GOODS_TRANSPORTER || role == NPCRole.CITY_TRANSPORTER) {
            Button tradeBtn = createStyledButton("💰 Trade", GOLD_COLOR);
            tradeBtn.setOnAction(e -> showTradeInterface());
            optionsBox.getChildren().add(tradeBtn);
        }
        
        // Hire option for eligible workers (peasants, villagers without critical roles)
        if (canBeHired(currentNPC)) {
            Button hireBtn = createStyledButton("🤝 Offer Work", "#9370db");
            hireBtn.setOnAction(e -> showHiringInterface());
            optionsBox.getChildren().add(hireBtn);
        }
        
        // Ask about role-specific information
        if (role != null) {
            Button roleBtn = createStyledButton(role.getIcon() + " Ask about duties", "#5a7a9a");
            roleBtn.setOnAction(e -> {
                addPlayerDialogue("Tell me about your duties here.");
                addNPCDialogue(getRoleDialogue());
            });
            optionsBox.getChildren().add(roleBtn);
        }
        
        // Inspect NPC inventory if available
        if (currentNPC.getInventory() != null) {
            Button inspectBtn = createStyledButton("🎒 Inspect Inventory", "#6b4a2b");
            inspectBtn.setOnAction(ev -> {
                optionsBox.getChildren().clear();
                InventoryUI inv = new InventoryUI(currentNPC.getInventory(), null);
                Button backBtn = createStyledButton("← Back", "#666666");
                backBtn.setOnAction(ev2 -> setupDialogueOptions());
                VBox box = new VBox(8);
                box.setPadding(new Insets(6));
                box.getChildren().addAll(inv, backBtn);
                optionsBox.getChildren().add(box);
            });
            optionsBox.getChildren().add(inspectBtn);
        }
        
        optionsBox.getChildren().addAll(chatBtn, workBtn, townBtn);
    }
    
    /**
     * Checks if an NPC can be hired by the player.
     */
    private boolean canBeHired(NPC npc) {
        if (npc == null) return false;
        NPCRole role = npc.getRole();
        
        // Leaders cannot be hired
        if (role != null && role.isLeader()) return false;
        
        // Guards cannot leave their post
        if (role == NPCRole.CITY_GUARD) return false;
        
        // Tax collectors serve the city
        if (role == NPCRole.TAX_COLLECTOR) return false;
        
        // Peasants, villagers, and wanderers can be hired
        return npc.getType() == NPC.NPCType.PEASANT || 
               npc.getType() == NPC.NPCType.VILLAGER ||
               role == NPCRole.WANDERER ||
               role == NPCRole.PEASANT_WORKER;
    }
    
    /**
     * Shows the trading interface.
     */
    private void showTradeInterface() {
        addPlayerDialogue("I'd like to see your wares.");
        addNPCDialogue("Of course! Take a look at what I have.");
        
        // Clear current options and show trade UI
        optionsBox.getChildren().clear();
        
        VBox tradeBox = new VBox(10);
        tradeBox.setPadding(new Insets(10));
        tradeBox.setStyle("-fx-background-color: " + LIGHT_BG + "; -fx-background-radius: 8;");
        
        Label tradeTitle = new Label("📦 " + currentNPC.getName() + "'s Goods");
        tradeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tradeTitle.setTextFill(Color.web(GOLD_COLOR));
        
        // Sample trade items based on NPC role/type
        VBox itemList = new VBox(5);
        String[] items = getTradeItems();
        for (String item : items) {
            HBox itemRow = createTradeItemRow(item);
            itemList.getChildren().add(itemRow);
        }
        
        // Gold display
        Label playerGold = new Label("Your Gold: " + (player != null ? player.getGold() : 0));
        playerGold.setTextFill(Color.web(GOLD_COLOR));
        
        Button backBtn = createStyledButton("← Back", "#666666");
        backBtn.setOnAction(e -> setupDialogueOptions());
        
        tradeBox.getChildren().addAll(tradeTitle, itemList, playerGold, backBtn);
        optionsBox.getChildren().add(tradeBox);
    }
    
    private String[] getTradeItems() {
        NPCRole role = currentNPC.getRole();
        if (role == NPCRole.GOODS_TRANSPORTER) {
            return new String[]{"Grain Bundle|10|5", "Lumber|15|3", "Stone|20|2"};
        } else if (role == NPCRole.CITY_TRANSPORTER) {
            return new String[]{"Fine Cloth|25|2", "Iron Ingot|30|3", "Spices|40|1"};
        } else {
            return new String[]{"Bread|3|10", "Rope|5|5", "Torch|2|8", "Waterskin|8|3"};
        }
    }
    
    private HBox createTradeItemRow(String itemData) {
        String[] parts = itemData.split("\\|");
        String name = parts[0];
        int price = Integer.parseInt(parts[1]);
        int stock = Integer.parseInt(parts[2]);
        
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-background-color: " + MEDIUM_BG + "; -fx-background-radius: 4;");
        
        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.web(TEXT_COLOR));
        nameLabel.setPrefWidth(100);
        
        Label priceLabel = new Label(price + "g");
        priceLabel.setTextFill(Color.web(GOLD_COLOR));
        priceLabel.setPrefWidth(40);
        
        Label stockLabel = new Label("x" + stock);
        stockLabel.setTextFill(Color.web(TEXT_COLOR, 0.7));
        
        Button buyBtn = new Button("Buy");
        buyBtn.setStyle("-fx-background-color: #4a7c4a; -fx-text-fill: white; -fx-font-size: 10px;");
        buyBtn.setOnAction(e -> {
            if (player != null && player.getGold() >= price) {
                player.addGold(-price);
                addNPCDialogue("Thank you for your purchase!");
            } else {
                addNPCDialogue("You don't have enough gold for that.");
            }
        });
        
        row.getChildren().addAll(nameLabel, priceLabel, stockLabel, buyBtn);
        return row;
    }
    
    /**
     * Shows the hiring interface with NPC skills and cost.
     */
    private void showHiringInterface() {
        addPlayerDialogue("Would you be interested in working for me?");
        
        int relationship = relationshipManager != null ? 
            relationshipManager.getRelationshipValue(currentNPC) : 0;
        
        if (relationship < 20) {
            addNPCDialogue("I appreciate the offer, but I barely know you. Perhaps if we talked more...");
            return;
        }
        
        addNPCDialogue("I could consider that. Let me tell you about my abilities.");
        
        // Clear options and show hiring UI
        optionsBox.getChildren().clear();
        
        VBox hireBox = new VBox(10);
        hireBox.setPadding(new Insets(10));
        hireBox.setStyle("-fx-background-color: " + LIGHT_BG + "; -fx-background-radius: 8;");
        
        Label hireTitle = new Label("🤝 Hire " + currentNPC.getName());
        hireTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hireTitle.setTextFill(Color.web(ACCENT_COLOR));
        
        // Skills display
        Label skillsHeader = new Label("Skills:");
        skillsHeader.setTextFill(Color.web(TEXT_COLOR));
        skillsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        VBox skillsList = new VBox(3);
        java.util.Map<String, Integer> skills = currentNPC.getSkills();
        for (java.util.Map.Entry<String, Integer> skill : skills.entrySet()) {
            HBox skillRow = createSkillRow(skill.getKey(), skill.getValue());
            skillsList.getChildren().add(skillRow);
        }
        
        // Cost display
        int hiringCost = currentNPC.getHiringCost();
        int dailyWage = currentNPC.getDailyWage();
        
        Label costLabel = new Label("💰 Hiring Cost: " + hiringCost + " gold");
        costLabel.setTextFill(Color.web(GOLD_COLOR));
        
        Label wageLabel = new Label("📅 Daily Wage: " + dailyWage + " gold/day");
        wageLabel.setTextFill(Color.web(TEXT_COLOR, 0.8));
        
        // Hire button
        Button confirmHireBtn = createStyledButton("✓ Hire for " + hiringCost + "g", "#4a7c4a");
        confirmHireBtn.setOnAction(e -> {
            if (player != null && player.getGold() >= hiringCost) {
                player.addGold(-hiringCost);
                currentNPC.setHired(true);
                addNPCDialogue("Wonderful! I'm happy to work with you. Lead the way!");
                // TODO: Add to player's party or worker roster
                setupDialogueOptions();
            } else {
                addNPCDialogue("I'm afraid you don't have enough gold to hire me.");
            }
        });
        
        Button backBtn = createStyledButton("← Back", "#666666");
        backBtn.setOnAction(e -> setupDialogueOptions());
        
        HBox buttons = new HBox(10, confirmHireBtn, backBtn);
        
        hireBox.getChildren().addAll(hireTitle, skillsHeader, skillsList, costLabel, wageLabel, buttons);
        optionsBox.getChildren().add(hireBox);
    }
    
    private HBox createSkillRow(String skillName, int value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(skillName);
        nameLabel.setTextFill(Color.web(TEXT_COLOR));
        nameLabel.setPrefWidth(80);
        
        // Skill bar
        ProgressBar bar = new ProgressBar(value / 100.0);
        bar.setPrefWidth(80);
        bar.setPrefHeight(10);
        
        String barColor = value >= 70 ? "#4a7c4a" : (value >= 40 ? "#7a7a4a" : "#7a4a4a");
        bar.setStyle("-fx-accent: " + barColor + ";");
        
        Label valueLabel = new Label(value + "");
        valueLabel.setTextFill(Color.web(TEXT_COLOR, 0.7));
        
        row.getChildren().addAll(nameLabel, bar, valueLabel);
        return row;
    }
    
    /**
     * Gets dialogue about the NPC's role.
     */
    private String getRoleDialogue() {
        NPCRole role = currentNPC.getRole();
        if (role == null) return "I just try to get by, day by day.";
        
        return switch (role) {
            case VILLAGE_ELDER -> "As village elder, I handle disputes, make decisions for our community, " +
                "and represent us when dealing with the city. It's a responsibility passed down through generations.";
            case GOODS_TRANSPORTER -> "I take our village's goods to the city markets and bring back " +
                "supplies and coin. The roads can be dangerous, but the work is essential for our survival.";
            case PEASANT_WORKER -> "I work the land - farming, gathering, whatever needs doing. " +
                "From dawn to dusk, there's always more work to be done.";
            case MAYOR -> "I govern this city and oversee the villages under our protection. " +
                "Taxes, trade agreements, defense - it all falls under my purview.";
            case CITY_GUARD -> "I patrol the streets and walls, keeping our citizens safe. " +
                "We work in shifts to maintain constant vigilance.";
            case CITY_TRANSPORTER -> "I manage trade caravans between cities, bringing goods and " +
                "fostering commerce. It's profitable work, if risky.";
            case TAX_COLLECTOR -> "I travel to our vassal villages to collect the city's due. " +
                "Not everyone welcomes my visits, but the treasury must be filled.";
            case WANDERER -> "I have no fixed role - I go where the wind takes me and help where I can.";
        };
    }
    
    private String getWorkDialogue() {
        if (currentNPC == null) return "I keep busy.";
        
        return switch (currentNPC.getType()) {
            case VILLAGER -> "I help around the village. There's always something that needs doing - " +
                            "fetching water, helping neighbors, tending to small chores.";
            case MERCHANT -> "I buy and sell goods! I travel between towns, bringing wares " +
                            "from distant places. Business has been " + 
                            (Math.random() > 0.5 ? "good" : "slow") + " lately.";
            case GUARD -> "I keep the peace and protect the townfolk. It's honest work, " +
                         "though the pay could be better.";
            case NOBLE -> "Work? I manage my estates and attend to matters of governance. " +
                         "It's a great responsibility.";
            case PEASANT -> "I work the fields from dawn to dusk. It's hard work, but " +
                           "it's an honest living. The harvest " + 
                           (Math.random() > 0.5 ? "looks promising" : "has been difficult") + " this year.";
            case BLACKSMITH -> "I forge tools and weapons. Steel, iron, whatever the folk need. " +
                              "My father taught me the trade, and his father before him.";
            case INNKEEPER -> "I run this establishment! We serve food, drink, and offer " +
                             "rooms for weary travelers like yourself.";
            case BARD -> "I bring joy through song and story! Every tavern welcomes a good " +
                        "tale, and I have many to tell.";
        };
    }
    
    private String getTownDialogue() {
        if (currentNPC == null || currentNPC.getHomeTown() == null) {
            return "This is a nice enough place, I suppose.";
        }
        
        Town town = currentNPC.getHomeTown();
        String townName = town.getName();
        
        if (town.isMajor()) {
            return "Welcome to " + townName + "! It's one of the great cities in this region. " +
                   "The castle overlooks everything, and the markets are always busy. " +
                   "You can find almost anything here if you look hard enough.";
        } else {
            String villageType = town.getVillageType() != null ? 
                town.getVillageType().getDisplayName() : "humble village";
            return townName + " is a " + villageType.toLowerCase() + ". " +
                   "It's small, but it's home. The folk here are hardworking and honest. " +
                   "We don't see many travelers - you must have come a long way.";
        }
    }
    
    private void addNPCDialogue(String text) {
        HBox bubble = new HBox();
        bubble.setAlignment(Pos.CENTER_LEFT);
        bubble.setPadding(new Insets(0, 30, 0, 0));
        
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(320);
        label.setPadding(new Insets(10));
        label.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 10 10 10 0;" +
            "-fx-text-fill: " + TEXT_COLOR + ";"
        );
        label.setFont(Font.font("Arial", 13));
        
        bubble.getChildren().add(label);
        dialogueHistory.getChildren().add(bubble);
        
        // Auto-scroll to bottom
        scrollToBottom();
    }
    
    private void addPlayerDialogue(String text) {
        HBox bubble = new HBox();
        bubble.setAlignment(Pos.CENTER_RIGHT);
        bubble.setPadding(new Insets(0, 0, 0, 30));
        
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(320);
        label.setPadding(new Insets(10));
        label.setStyle(
            "-fx-background-color: " + ACCENT_COLOR + ";" +
            "-fx-background-radius: 10 10 0 10;" +
            "-fx-text-fill: white;"
        );
        label.setFont(Font.font("Arial", 13));
        
        bubble.getChildren().add(label);
        dialogueHistory.getChildren().add(bubble);
        
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        // Delay to allow layout to update
        javafx.application.Platform.runLater(() -> {
            if (dialogueScrollPane != null) {
                dialogueScrollPane.setVvalue(1.0);
            }
        });
    }
    
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: derive(" + color + ", 20%);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        ));
        
        return btn;
    }
    
    public void close() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), dialoguePanel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            setVisible(false);
            
            // Unpause the game if we paused it
            if (gameTime != null && !wasGamePaused) {
                gameTime.resume();
            }
            
            if (onClose != null && currentNPC != null) {
                onClose.accept(currentNPC);
            }
            currentNPC = null;
        });
        fadeOut.play();
    }
    
    public void setOnClose(Consumer<NPC> handler) {
        this.onClose = handler;
    }
    
    public boolean isShowing() {
        return isVisible();
    }
    
    public NPC getCurrentNPC() {
        return currentNPC;
    }
}
