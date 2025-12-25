import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Social tab panel showing NPCs the player has met and relationship status.
 * Part of the side menu tab system.
 */
public class SocialPanel extends VBox {
    
    // Colors
    private static final String DARK_BG = "#1a1a1a";
    private static final String MEDIUM_BG = "#2a2a2a";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String GOLD_COLOR = "#ffd700";
    private static final String TEXT_COLOR = "#e0e0e0";
    
    private RelationshipManager relationshipManager;
    private VBox npcListContainer;
    private Label totalMetLabel;
    private ComboBox<String> filterCombo;
    private TextField searchField;
    private Consumer<NPC> onNPCSelected;
    
    public SocialPanel(RelationshipManager relationshipManager) {
        this.relationshipManager = relationshipManager;
        
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: " + DARK_BG + ";");
        
        createUI();
    }
    
    private void createUI() {
        // Header
        Label header = new Label("👥 Known NPCs");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setTextFill(Color.web(GOLD_COLOR));
        
        // Stats bar
        HBox statsBar = createStatsBar();
        
        // Filter controls
        HBox filterBar = createFilterBar();
        
        // NPC list scroll area
        npcListContainer = new VBox(8);
        npcListContainer.setPadding(new Insets(5));
        
        ScrollPane scrollPane = new ScrollPane(npcListContainer);
        scrollPane.setStyle(
            "-fx-background: " + MEDIUM_BG + ";" +
            "-fx-background-color: " + MEDIUM_BG + ";"
        );
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        getChildren().addAll(header, statsBar, filterBar, scrollPane);
        
        // Initial refresh
        refreshNPCList();
    }
    
    private HBox createStatsBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8));
        bar.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-background-radius: 5;"
        );
        
        totalMetLabel = new Label("NPCs Met: 0");
        totalMetLabel.setFont(Font.font("Arial", 12));
        totalMetLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label friendsLabel = new Label("| Friends: 0");
        friendsLabel.setFont(Font.font("Arial", 12));
        friendsLabel.setTextFill(Color.web("#90EE90"));
        friendsLabel.setId("friendsLabel");
        
        bar.getChildren().addAll(totalMetLabel, friendsLabel);
        return bar;
    }
    
    private HBox createFilterBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search by name...");
        searchField.setPrefWidth(150);
        searchField.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-prompt-text-fill: #666666;"
        );
        searchField.textProperty().addListener((obs, old, newVal) -> refreshNPCList());
        
        // Filter dropdown
        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll(
            "All NPCs",
            "Recent",
            "Friends+",
            "By Town"
        );
        filterCombo.setValue("Recent");
        filterCombo.setStyle(
            "-fx-background-color: " + MEDIUM_BG + ";" +
            "-fx-text-fill: " + TEXT_COLOR + ";"
        );
        filterCombo.setOnAction(e -> refreshNPCList());
        
        bar.getChildren().addAll(searchField, filterCombo);
        return bar;
    }
    
    /**
     * Refreshes the NPC list based on current filter.
     */
    public void refreshNPCList() {
        npcListContainer.getChildren().clear();
        
        if (relationshipManager == null) {
            addEmptyMessage();
            return;
        }
        
        // Update stats
        int total = relationshipManager.getTotalNPCsMet();
        totalMetLabel.setText("NPCs Met: " + total);
        
        int friends = relationshipManager.getNPCsByLevel(
            RelationshipManager.RelationshipLevel.FRIEND).size();
        Label friendsLabel = (Label) lookup("#friendsLabel");
        if (friendsLabel != null) {
            friendsLabel.setText("| Friends: " + friends);
        }
        
        // Get filtered list
        List<RelationshipManager.NPCRelationship> npcs;
        String filter = filterCombo.getValue();
        
        npcs = switch (filter) {
            case "Recent" -> relationshipManager.getRecentInteractions();
            case "Friends+" -> relationshipManager.getNPCsByLevel(
                RelationshipManager.RelationshipLevel.FRIEND);
            default -> relationshipManager.getAllKnownNPCs();
        };
        
        // Apply search filter
        String search = searchField.getText().toLowerCase().trim();
        if (!search.isEmpty()) {
            npcs = npcs.stream()
                .filter(r -> r.getNpcName().toLowerCase().contains(search) ||
                            r.getHomeTownName().toLowerCase().contains(search))
                .toList();
        }
        
        if (npcs.isEmpty()) {
            addEmptyMessage();
            return;
        }
        
        // Add NPC entries
        for (RelationshipManager.NPCRelationship rel : npcs) {
            npcListContainer.getChildren().add(createNPCEntry(rel));
        }
    }
    
    private void addEmptyMessage() {
        Label empty = new Label("No NPCs found.\nTalk to villagers to add them here!");
        empty.setFont(Font.font("Arial", 12));
        empty.setTextFill(Color.web(TEXT_COLOR, 0.6));
        empty.setWrapText(true);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(20));
        npcListContainer.getChildren().add(empty);
    }
    
    private VBox createNPCEntry(RelationshipManager.NPCRelationship rel) {
        VBox entry = new VBox(5);
        entry.setPadding(new Insets(10));
        entry.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 8;"
        );
        
        // Header row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Type icon
        StackPane icon = new StackPane();
        Circle iconBg = new Circle(15);
        iconBg.setFill(Color.web(rel.getLevel().getColor(), 0.3));
        iconBg.setStroke(Color.web(rel.getLevel().getColor()));
        iconBg.setStrokeWidth(2);
        
        Label iconLabel = new Label(getTypeIcon(rel.getNpcType()));
        iconLabel.setFont(Font.font("Segoe UI Emoji", 14));
        
        icon.getChildren().addAll(iconBg, iconLabel);
        
        // Name and type
        VBox nameBox = new VBox(2);
        
        Label nameLabel = new Label(rel.getNpcName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label typeLabel = new Label(rel.getNpcType().getName() + " • " + rel.getHomeTownName());
        typeLabel.setFont(Font.font("Arial", 10));
        typeLabel.setTextFill(Color.web(TEXT_COLOR, 0.7));
        
        nameBox.getChildren().addAll(nameLabel, typeLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        
        // Relationship indicator
        VBox relBox = new VBox(2);
        relBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label relLevel = new Label(rel.getLevel().getDisplayName());
        relLevel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        relLevel.setTextFill(Color.web(rel.getLevel().getColor()));
        
        // Relationship progress bar
        ProgressBar relProgress = new ProgressBar();
        relProgress.setProgress(rel.getRelationshipValue() / 100.0);
        relProgress.setPrefWidth(60);
        relProgress.setPrefHeight(6);
        relProgress.setStyle("-fx-accent: " + rel.getLevel().getColor() + ";");
        
        relBox.getChildren().addAll(relLevel, relProgress);
        
        header.getChildren().addAll(icon, nameBox, relBox);
        
        // Info row
        HBox infoRow = new HBox(15);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setPadding(new Insets(5, 0, 0, 40));
        
        Label interactions = new Label("💬 " + rel.getTotalInteractions() + " chats");
        interactions.setFont(Font.font("Arial", 10));
        interactions.setTextFill(Color.web(TEXT_COLOR, 0.6));
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
        Label lastSeen = new Label("Last seen: " + sdf.format(new Date(rel.getLastInteraction())));
        lastSeen.setFont(Font.font("Arial", 10));
        lastSeen.setTextFill(Color.web(TEXT_COLOR, 0.6));
        
        infoRow.getChildren().addAll(interactions, lastSeen);
        
        entry.getChildren().addAll(header, infoRow);
        
        // Hover effect
        entry.setOnMouseEntered(e -> entry.setStyle(
            "-fx-background-color: derive(" + LIGHT_BG + ", 10%);" +
            "-fx-background-radius: 8;"
        ));
        entry.setOnMouseExited(e -> entry.setStyle(
            "-fx-background-color: " + LIGHT_BG + ";" +
            "-fx-background-radius: 8;"
        ));
        
        return entry;
    }
    
    private String getTypeIcon(NPC.NPCType type) {
        return switch (type) {
            case VILLAGER -> "👤";
            case MERCHANT -> "💰";
            case GUARD -> "⚔";
            case NOBLE -> "👑";
            case PEASANT -> "🌾";
            case BLACKSMITH -> "🔨";
            case INNKEEPER -> "🍺";
            case BARD -> "🎵";
        };
    }
    
    public void setRelationshipManager(RelationshipManager manager) {
        this.relationshipManager = manager;
        refreshNPCList();
    }
    
    public void setOnNPCSelected(Consumer<NPC> callback) {
        this.onNPCSelected = callback;
    }
}
