import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.function.BiConsumer;

/**
 * Panel for recruiting NPCs at taverns.
 * Displays available hirelings with their stats, profession, and costs.
 */
public class TavernRecruitmentPanel extends StackPane {
    
    // Medieval theme colors (matching other panels)
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String GOLD = "#c4a574";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    private static final String BUTTON_BG = "#3d3020";
    private static final String BUTTON_HOVER = "#4d4030";
    private static final String ACCENT = "#8b7355";
    
    // Current state
    private Town currentTown;
    private PlayerSprite player;
    private TavernNPC[] availableNPCs;
    
    // UI components
    private VBox mainPanel;
    private Label townNameLabel;
    private VBox npcListContainer;
    private Label partyStatusLabel;
    private Label goldLabel;
    
    // Callbacks
    private Runnable onClose;
    private BiConsumer<TavernNPC, Boolean> onHire; // NPC and success flag
    
    public TavernRecruitmentPanel() {
        // Darkened background overlay
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.color(0, 0, 0, 0.7));
        overlay.widthProperty().bind(this.widthProperty());
        overlay.heightProperty().bind(this.heightProperty());
        overlay.setOnMouseClicked(e -> close());
        
        // Create main panel
        mainPanel = createMainPanel();
        
        this.getChildren().addAll(overlay, mainPanel);
        this.setVisible(false);
        this.setAlignment(Pos.CENTER);
    }
    
    private VBox createMainPanel() {
        VBox panel = new VBox(0);
        panel.setMaxWidth(500);
        panel.setMaxHeight(600);
        panel.setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 12;"
        );
        
        // Drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(25);
        shadow.setColor(Color.color(0, 0, 0, 0.8));
        panel.setEffect(shadow);
        
        // Header
        VBox header = createHeader();
        
        // Status bar (gold and party size)
        HBox statusBar = createStatusBar();
        
        // NPC list in scrollable container
        ScrollPane scrollPane = createNPCScrollPane();
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Footer with close button
        HBox footer = createFooter();
        
        panel.getChildren().addAll(header, statusBar, scrollPane, footer);
        return panel;
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 20, 15, 20));
        header.setStyle(
            "-fx-background-color: " + BG_DARK + ";" +
            "-fx-background-radius: 10 10 0 0;"
        );
        
        // Decorative line
        Rectangle topLine = new Rectangle(200, 2);
        topLine.setFill(Color.web(GOLD));
        
        // Title
        Label title = new Label("🍺 The Tavern");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(GOLD));
        
        // Town name
        townNameLabel = new Label("Town Name");
        townNameLabel.setFont(Font.font("Georgia", FontWeight.NORMAL, 14));
        townNameLabel.setTextFill(Color.web(TEXT_DIM));
        
        // Decorative line
        Rectangle bottomLine = new Rectangle(200, 2);
        bottomLine.setFill(Color.web(GOLD));
        
        // Subtitle
        Label subtitle = new Label("Adventurers looking for work");
        subtitle.setFont(Font.font("Georgia", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web(TEXT_DIM));
        
        header.getChildren().addAll(topLine, title, townNameLabel, bottomLine, subtitle);
        return header;
    }
    
    private HBox createStatusBar() {
        HBox bar = new HBox(20);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10, 15, 10, 15));
        bar.setStyle(
            "-fx-background-color: " + BG_LIGHT + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1 0 1 0;"
        );
        
        // Gold display
        goldLabel = new Label("💰 0 Gold");
        goldLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        goldLabel.setTextFill(Color.web("#ffd700"));
        
        // Party status
        partyStatusLabel = new Label("👥 Party: 0 / 5");
        partyStatusLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        partyStatusLabel.setTextFill(Color.web(GOLD));
        
        bar.getChildren().addAll(goldLabel, partyStatusLabel);
        return bar;
    }
    
    private ScrollPane createNPCScrollPane() {
        npcListContainer = new VBox(10);
        npcListContainer.setPadding(new Insets(15));
        npcListContainer.setStyle("-fx-background-color: " + BG_MED + ";");
        
        ScrollPane scrollPane = new ScrollPane(npcListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: " + BG_MED + ";" +
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-border-color: transparent;"
        );
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(350);
        
        return scrollPane;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15, 20, 20, 20));
        footer.setStyle("-fx-background-color: " + BG_DARK + "; -fx-background-radius: 0 0 10 10;");
        
        Button closeBtn = createStyledButton("✕ Leave Tavern", false);
        closeBtn.setOnAction(e -> close());
        closeBtn.setPrefWidth(200);
        
        footer.getChildren().add(closeBtn);
        return footer;
    }
    
    /**
     * Creates a card displaying an NPC's information.
     */
    private VBox createNPCCard(TavernNPC npc) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
            "-fx-background-color: " + BG_DARK + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        
        // Header row: Name and profession
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        // Gender icon
        String genderIcon = npc.getGender() == MedievalNameGenerator.Gender.MALE ? "♂" : "♀";
        Label genderLabel = new Label(genderIcon);
        genderLabel.setFont(Font.font("Georgia", 14));
        genderLabel.setTextFill(Color.web(npc.getGender() == MedievalNameGenerator.Gender.MALE ? "#6090c0" : "#c06090"));
        
        // Name
        Label nameLabel = new Label(npc.getName());
        nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web(GOLD));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Profession tier badge
        NPCProfession prof = npc.getProfession();
        Label tierBadge = new Label(prof.getTier().getDisplayName());
        tierBadge.setFont(Font.font("Georgia", FontWeight.BOLD, 10));
        tierBadge.setTextFill(Color.web(getTierColor(prof.getTier())));
        tierBadge.setPadding(new Insets(2, 6, 2, 6));
        tierBadge.setStyle(
            "-fx-background-color: " + BG_LIGHT + ";" +
            "-fx-background-radius: 4;" +
            "-fx-border-color: " + getTierColor(prof.getTier()) + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;"
        );
        
        headerRow.getChildren().addAll(genderLabel, nameLabel, spacer, tierBadge);
        
        // Profession title
        Label profLabel = new Label(prof.getTitle());
        profLabel.setFont(Font.font("Georgia", 12));
        profLabel.setTextFill(Color.web(TEXT));
        
        // Age
        Label ageLabel = new Label("Age: " + npc.getAge());
        ageLabel.setFont(Font.font("Georgia", 10));
        ageLabel.setTextFill(Color.web(TEXT_DIM));
        
        // Stats row
        HBox statsRow = createStatsRow(npc);
        
        // Traits
        HBox traitsRow = createTraitsRow(npc);
        
        // Cost row
        HBox costRow = new HBox(15);
        costRow.setAlignment(Pos.CENTER_LEFT);
        costRow.setPadding(new Insets(5, 0, 0, 0));
        
        Label hiringFee = new Label("Hire: " + npc.getHiringFee() + "g");
        hiringFee.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        hiringFee.setTextFill(Color.web("#ffd700"));
        
        Label dailyWage = new Label("Wage: " + npc.getDailyWage() + "g/day");
        dailyWage.setFont(Font.font("Georgia", 11));
        dailyWage.setTextFill(Color.web(TEXT_DIM));
        
        Region costSpacer = new Region();
        HBox.setHgrow(costSpacer, Priority.ALWAYS);
        
        // Hire button
        Button hireBtn = createHireButton(npc);
        
        costRow.getChildren().addAll(hiringFee, dailyWage, costSpacer, hireBtn);
        
        card.getChildren().addAll(headerRow, profLabel, ageLabel, statsRow, traitsRow, costRow);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + BG_LIGHT + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + GOLD + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: " + BG_DARK + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        ));
        
        return card;
    }
    
    private HBox createStatsRow(TavernNPC npc) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));
        
        CharacterStats stats = npc.getStats();
        
        // Show key stats
        row.getChildren().addAll(
            createStatBadge("STR", stats.getStrength(), "#c08080"),
            createStatBadge("DEX", stats.getDexterity(), "#80c080"),
            createStatBadge("CON", stats.getConstitution(), "#c0a080"),
            createStatBadge("INT", stats.getIntelligence(), "#8080c0"),
            createStatBadge("WIS", stats.getWisdom(), "#80a0c0"),
            createStatBadge("CHA", stats.getCharisma(), "#c080a0"),
            createStatBadge("LCK", stats.getLuck(), "#a0c080")
        );
        
        return row;
    }
    
    private VBox createStatBadge(String name, int value, String color) {
        VBox badge = new VBox(0);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(2, 4, 2, 4));
        badge.setStyle("-fx-background-color: " + BG_MED + "; -fx-background-radius: 3;");
        
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        valueLabel.setTextFill(Color.web(value >= 12 ? color : (value <= 8 ? "#808080" : TEXT)));
        
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Georgia", 7));
        nameLabel.setTextFill(Color.web(TEXT_DIM));
        
        badge.getChildren().addAll(valueLabel, nameLabel);
        return badge;
    }
    
    private HBox createTraitsRow(TavernNPC npc) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        
        NPCProfession prof = npc.getProfession();
        for (NPCProfession.Trait trait : prof.getTraits()) {
            Label traitLabel = new Label(trait.getDisplayName());
            traitLabel.setFont(Font.font("Georgia", 9));
            traitLabel.setTextFill(Color.web(trait.isPositive() ? "#80c080" : "#c08080"));
            traitLabel.setPadding(new Insets(1, 4, 1, 4));
            traitLabel.setStyle(
                "-fx-background-color: " + BG_MED + ";" +
                "-fx-background-radius: 3;"
            );
            
            // Tooltip with description
            Tooltip tooltip = new Tooltip(trait.getDescription());
            tooltip.setFont(Font.font("Georgia", 11));
            Tooltip.install(traitLabel, tooltip);
            
            row.getChildren().add(traitLabel);
        }
        
        // Show individual perk badges with tooltips
        for (NPCProfession.Perk perk : prof.getUnlockedPerks()) {
            Label perkLabel = new Label("★ " + perk.getDisplayName());
            perkLabel.setFont(Font.font("Georgia", 9));
            perkLabel.setTextFill(Color.web(GOLD));
            perkLabel.setPadding(new Insets(1, 5, 1, 5));
            perkLabel.setStyle(
                "-fx-background-color: " + BG_MED + ";" +
                "-fx-background-radius: 3;"
            );
            Tooltip perkTip = new Tooltip(perk.getDescription());
            perkTip.setFont(Font.font("Georgia", 11));
            Tooltip.install(perkLabel, perkTip);
            row.getChildren().add(perkLabel);
        }
        
        return row;
    }
    
    private Button createHireButton(TavernNPC npc) {
        Button btn = new Button("Hire");
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        btn.setPadding(new Insets(6, 15, 6, 15));
        
        updateHireButton(btn, npc);
        
        btn.setOnAction(e -> attemptHire(npc, btn));
        
        return btn;
    }
    
    private void updateHireButton(Button btn, TavernNPC npc) {
        boolean canAfford = player != null && player.getCurrency().getTotalCopper() >= npc.getHiringFee() * 100;
        boolean partyNotFull = player != null && !player.getParty().isFull();
        boolean canHire = canAfford && partyNotFull;
        
        String enabledStyle = 
            "-fx-background-color: linear-gradient(to bottom, #4a6030, #3a5020);" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: #6a8040;" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;";
        
        String disabledStyle = 
            "-fx-background-color: " + BG_LIGHT + ";" +
            "-fx-text-fill: #606060;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 5;";
        
        btn.setStyle(canHire ? enabledStyle : disabledStyle);
        btn.setDisable(!canHire);
        
        if (!canAfford) {
            btn.setText("Can't Afford");
        } else if (!partyNotFull) {
            btn.setText("Party Full");
        } else {
            btn.setText("Hire");
        }
    }
    
    private void attemptHire(TavernNPC npc, Button btn) {
        if (player == null) return;
        
        int cost = npc.getHiringFee();
        Currency currency = player.getCurrency();
        Party party = player.getParty();
        
        // Check if can afford
        if (currency.getTotalCopper() < cost * 100) {
            if (onHire != null) onHire.accept(npc, false);
            return;
        }
        
        // Check party size
        if (party.isFull()) {
            if (onHire != null) onHire.accept(npc, false);
            return;
        }
        
        // Deduct cost (convert gold to copper for the currency system)
        currency.subtract(cost * 100); // Cost is in gold, currency uses copper
        
        // Mark NPC as hired
        npc.hire();
        
        // Create a PartyMember from the TavernNPC and add to party
        PartyMember member = createPartyMemberFromNPC(npc);
        party.addMember(member);
        
        // Update UI
        refreshPanel();
        
        // Callback
        if (onHire != null) onHire.accept(npc, true);
    }
    
    /**
     * Creates a PartyMember from a TavernNPC.
     */
    private PartyMember createPartyMemberFromNPC(TavernNPC npc) {
        // Map NPCProfession to PartyMember.Role
        PartyMember.Role role = mapProfessionToRole(npc.getProfession().getType());
        
        // Create party member with NPC's EXACT name and role
        PartyMember member = new PartyMember(npc.getName(), role);
        
        // Store the original profession info for display
        member.setProfessionName(npc.getProfession().getType().getDisplayName());
        member.setGender(npc.getGender() == MedievalNameGenerator.Gender.MALE);
        
        // Transfer stats from TavernNPC to PartyMember
        CharacterStats npcStats = npc.getStats();
        member.setStatsFromCharacterStats(npcStats);
        
        return member;
    }
    
    /**
     * Maps an NPC profession to a party member role.
     */
    private PartyMember.Role mapProfessionToRole(NPCProfession.ProfessionType profType) {
        return switch (profType.getCategory()) {
            case COMBAT -> PartyMember.Role.WARRIOR;
            case SUPPORT -> PartyMember.Role.HEALER;
            case CRAFT -> PartyMember.Role.LABORER;
            case GATHER -> PartyMember.Role.SCOUT;
            case TRADE -> PartyMember.Role.MERCHANT;
        };
    }
    
    private String getTierColor(NPCProfession.Tier tier) {
        return switch (tier) {
            case NOVICE -> "#808080";     // Grey
            case APPRENTICE -> "#80c080"; // Green
            case JOURNEYMAN -> "#6090c0"; // Blue
            case EXPERT -> "#a060c0";     // Purple
            case MASTER -> "#ffd700";     // Gold
        };
    }
    
    private Button createStyledButton(String text, boolean primary) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.NORMAL, 13));
        btn.setPadding(new Insets(10, 20, 10, 20));
        
        String baseStyle = primary ?
            "-fx-background-color: linear-gradient(to bottom, #4a3020, #3a2015);" +
            "-fx-text-fill: " + GOLD + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + GOLD + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
            :
            "-fx-background-color: " + BUTTON_BG + ";" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;";
        
        String hoverStyle = primary ?
            "-fx-background-color: linear-gradient(to bottom, #5a4030, #4a3025);" +
            "-fx-text-fill: #ffd700;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: #ffd700;" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;"
            :
            "-fx-background-color: " + BUTTON_HOVER + ";" +
            "-fx-text-fill: " + GOLD + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + GOLD + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;";
        
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        
        return btn;
    }
    
    /**
     * Shows the recruitment panel for a specific town.
     */
    public void showForTown(Town town, PlayerSprite player) {
        this.currentTown = town;
        this.player = player;
        
        // Get available NPCs from town
        this.availableNPCs = town.getTavernNPCs();
        
        // Update UI
        townNameLabel.setText(town.getName());
        refreshPanel();
        
        // Show with animation
        this.setVisible(true);
        
        mainPanel.setScaleX(0.9);
        mainPanel.setScaleY(0.9);
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), mainPanel);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
        
        this.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(200), this);
        fade.setToValue(1.0);
        fade.play();
    }
    
    /**
     * Refreshes the panel with current data.
     */
    private void refreshPanel() {
        // Update gold display
        if (player != null) {
            int gold = player.getCurrency().getGold();
            int silver = player.getCurrency().getSilver();
            goldLabel.setText("💰 " + gold + "g " + silver + "s");
            
            Party party = player.getParty();
            CharacterStats stats = player.getCharacterStats();
            int maxParty = stats != null ? stats.getMaxPartySize() : 5;
            partyStatusLabel.setText("👥 Party: " + party.getSize() + " / " + maxParty);
        }
        
        // Rebuild NPC list
        npcListContainer.getChildren().clear();
        
        if (availableNPCs == null || availableNPCs.length == 0) {
            Label emptyLabel = new Label("No adventurers available for hire.");
            emptyLabel.setFont(Font.font("Georgia", 12));
            emptyLabel.setTextFill(Color.web(TEXT_DIM));
            npcListContainer.getChildren().add(emptyLabel);
        } else {
            for (TavernNPC npc : availableNPCs) {
                if (!npc.isHired()) {
                    npcListContainer.getChildren().add(createNPCCard(npc));
                }
            }
            
            // Check if all hired
            if (npcListContainer.getChildren().isEmpty()) {
                Label allHiredLabel = new Label("You've hired everyone available!");
                allHiredLabel.setFont(Font.font("Georgia", 12));
                allHiredLabel.setTextFill(Color.web(GOLD));
                npcListContainer.getChildren().add(allHiredLabel);
            }
        }
    }
    
    /**
     * Closes the panel with animation.
     */
    public void close() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), mainPanel);
        scale.setToX(0.95);
        scale.setToY(0.95);
        
        FadeTransition fade = new FadeTransition(Duration.millis(150), this);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            this.setVisible(false);
            if (onClose != null) onClose.run();
        });
        
        scale.play();
        fade.play();
    }
    
    // === Getters and Setters ===
    
    public void setOnClose(Runnable handler) { this.onClose = handler; }
    public void setOnHire(BiConsumer<TavernNPC, Boolean> handler) { this.onHire = handler; }
    public boolean isShowing() { return this.isVisible(); }
    public Town getCurrentTown() { return currentTown; }
}
