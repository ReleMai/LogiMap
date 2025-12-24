import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Party management UI in Mount & Blade style.
 * Shows party members with details pane on the side.
 */
public class PartyUI extends VBox {
    
    // Medieval theme colors
    private static final String BG_DARK = "#1a1208";
    private static final String BG_MED = "#2a1f10";
    private static final String BG_LIGHT = "#3a2a15";
    private static final String GOLD = "#c4a574";
    private static final String TEXT = "#e8dcc8";
    private static final String TEXT_DIM = "#a89878";
    private static final String BORDER = "#5a4a30";
    
    private static final int PANEL_WIDTH = 500;
    private static final int PORTRAIT_SIZE = 60;
    
    private Party party;
    private PlayerSprite player;
    
    private VBox memberList;
    private VBox detailsPane;
    private Label statsLabel;
    
    private PartyMember selectedMember = null;
    private Runnable onClose;
    
    public PartyUI(Party party, PlayerSprite player) {
        this.party = party;
        this.player = player;
        
        setPrefWidth(PANEL_WIDTH);
        setMaxWidth(PANEL_WIDTH);
        setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 2, 2);"
        );
        setSpacing(0);
        setPadding(new Insets(0));
        
        getChildren().addAll(
            createTitleBar(),
            createPartyStats(),
            createMainContent(),
            createButtonBar()
        );
    }
    
    private HBox createTitleBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + BG_LIGHT + ", " + BG_MED + ");" +
            "-fx-background-radius: 6 6 0 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        Label title = new Label("⚔ Party Management");
        title.setTextFill(Color.web(GOLD));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Party size
        Label sizeLabel = new Label(party.getSize() + "/" + party.getMaxSize() + " members");
        sizeLabel.setTextFill(Color.web(TEXT_DIM));
        sizeLabel.setFont(Font.font("Georgia", 11));
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT_DIM + ";" +
            "-fx-font-size: 14;" +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> { if (onClose != null) onClose.run(); });
        
        bar.getChildren().addAll(title, spacer, sizeLabel, closeBtn);
        return bar;
    }
    
    private HBox createPartyStats() {
        HBox stats = new HBox(20);
        stats.setPadding(new Insets(8, 12, 8, 12));
        stats.setAlignment(Pos.CENTER);
        stats.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        statsLabel = new Label();
        updateStatsLabel();
        statsLabel.setTextFill(Color.web(TEXT));
        statsLabel.setFont(Font.font("Georgia", 10));
        
        stats.getChildren().add(statsLabel);
        return stats;
    }
    
    private void updateStatsLabel() {
        String stats = String.format(
            "⚔ Attack: %d  |  🛡 Defense: %d  |  💨 Speed: %d  |  😊 Morale: %d%%  |  💰 Wages: %d/week",
            party.getTotalAttack(),
            party.getTotalDefense(),
            party.getAverageSpeed(),
            party.getPartyMorale(),
            party.getTotalWeeklyWages()
        );
        statsLabel.setText(stats);
    }
    
    private HBox createMainContent() {
        HBox main = new HBox(10);
        main.setPadding(new Insets(10));
        main.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        // Left side - member list
        VBox leftPane = new VBox(5);
        leftPane.setPrefWidth(200);
        
        Label listLabel = new Label("Party Members");
        listLabel.setTextFill(Color.web(GOLD));
        listLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        
        memberList = new VBox(4);
        memberList.setStyle("-fx-background-color: " + BG_MED + "; -fx-padding: 5;");
        
        ScrollPane scroll = new ScrollPane(memberList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(250);
        scroll.setStyle("-fx-background: " + BG_MED + "; -fx-background-color: " + BG_MED + ";");
        
        leftPane.getChildren().addAll(listLabel, scroll);
        
        // Right side - details pane
        detailsPane = createDetailsPane();
        HBox.setHgrow(detailsPane, Priority.ALWAYS);
        
        main.getChildren().addAll(leftPane, detailsPane);
        
        refreshMemberList();
        
        return main;
    }
    
    private VBox createDetailsPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;"
        );
        
        Label placeholder = new Label("Select a party member\nto view details");
        placeholder.setTextFill(Color.web(TEXT_DIM));
        placeholder.setFont(Font.font("Georgia", 12));
        placeholder.setAlignment(Pos.CENTER);
        
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(placeholder);
        
        return pane;
    }
    
    private void refreshMemberList() {
        memberList.getChildren().clear();
        
        if (party.isEmpty()) {
            Label empty = new Label("No party members");
            empty.setTextFill(Color.web(TEXT_DIM));
            empty.setFont(Font.font("Georgia", 11));
            memberList.getChildren().add(empty);
            return;
        }
        
        for (int i = 0; i < party.getSize(); i++) {
            PartyMember member = party.getMember(i);
            HBox row = createMemberRow(member, i);
            memberList.getChildren().add(row);
        }
    }
    
    private HBox createMemberRow(PartyMember member, int index) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(4, 6, 4, 6));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-background-color: " + (member == selectedMember ? BG_LIGHT : "transparent") + ";" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;"
        );
        
        // Portrait
        Canvas portrait = new Canvas(40, 40);
        member.renderPortrait(portrait.getGraphicsContext2D(), 0, 0, 40);
        
        // Name and role
        VBox info = new VBox(2);
        
        Label nameLabel = new Label(member.getName());
        nameLabel.setTextFill(Color.web(TEXT));
        nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        
        Label roleLabel = new Label(member.getRole().getIcon() + " " + member.getRole().getName() + " Lv." + member.getLevel());
        roleLabel.setTextFill(Color.web(TEXT_DIM));
        roleLabel.setFont(Font.font("Georgia", 9));
        
        info.getChildren().addAll(nameLabel, roleLabel);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        row.getChildren().addAll(portrait, info);
        
        // Click to select
        row.setOnMouseClicked(e -> {
            selectedMember = member;
            refreshMemberList();
            showMemberDetails(member);
        });
        
        row.setOnMouseEntered(e -> {
            if (member != selectedMember) {
                row.setStyle(row.getStyle().replace("transparent", BG_LIGHT + "55"));
            }
        });
        row.setOnMouseExited(e -> {
            if (member != selectedMember) {
                row.setStyle(row.getStyle().replace(BG_LIGHT + "55", "transparent"));
            }
        });
        
        return row;
    }
    
    private void showMemberDetails(PartyMember member) {
        detailsPane.getChildren().clear();
        detailsPane.setAlignment(Pos.TOP_LEFT);
        
        // Large portrait
        Canvas portrait = new Canvas(PORTRAIT_SIZE, PORTRAIT_SIZE);
        member.renderPortrait(portrait.getGraphicsContext2D(), 0, 0, PORTRAIT_SIZE);
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(member.getName());
        nameLabel.setTextFill(Color.web(GOLD));
        nameLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        
        Label roleLabel = new Label(member.getRole().getIcon() + " " + member.getRole().getName());
        roleLabel.setTextFill(Color.web(TEXT));
        roleLabel.setFont(Font.font("Georgia", 11));
        
        nameBox.getChildren().addAll(nameLabel, roleLabel);
        header.getChildren().addAll(portrait, nameBox);
        
        // Stats
        VBox statsBox = new VBox(4);
        statsBox.setPadding(new Insets(10, 0, 0, 0));
        
        addStatRow(statsBox, "Level", String.valueOf(member.getLevel()), TEXT);
        addStatRow(statsBox, "Health", member.getCurrentHealth() + "/" + member.getMaxHealth(), 
            member.getCurrentHealth() < member.getMaxHealth() / 2 ? "#F44336" : TEXT);
        addStatRow(statsBox, "Attack", "+" + member.getAttack(), TEXT);
        addStatRow(statsBox, "Defense", "+" + member.getDefense(), TEXT);
        addStatRow(statsBox, "Speed", String.valueOf(member.getSpeed()), TEXT);
        addStatRow(statsBox, "Morale", member.getMorale() + "%", 
            member.getMorale() < 50 ? "#FF9800" : TEXT);
        addStatRow(statsBox, "Weekly Wage", member.getWeeklyWage() + " copper", TEXT);
        
        // Skills
        if (!member.getSkills().isEmpty()) {
            Label skillsHeader = new Label("Skills:");
            skillsHeader.setTextFill(Color.web(GOLD));
            skillsHeader.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
            skillsHeader.setPadding(new Insets(10, 0, 4, 0));
            statsBox.getChildren().add(skillsHeader);
            
            for (var entry : member.getSkills().entrySet()) {
                addStatRow(statsBox, "  " + entry.getKey(), String.valueOf(entry.getValue()), TEXT_DIM);
            }
        }
        
        // Dismiss button
        Button dismissBtn = new Button("Dismiss");
        dismissBtn.setStyle(
            "-fx-background-color: #5a2020;" +
            "-fx-text-fill: #ff8080;" +
            "-fx-font-family: Georgia;" +
            "-fx-font-size: 10;" +
            "-fx-padding: 4 10;" +
            "-fx-cursor: hand;"
        );
        dismissBtn.setOnAction(e -> {
            party.removeMember(member);
            selectedMember = null;
            refreshMemberList();
            updateStatsLabel();
            detailsPane.getChildren().clear();
            detailsPane.setAlignment(Pos.CENTER);
            Label placeholder = new Label("Member dismissed");
            placeholder.setTextFill(Color.web(TEXT_DIM));
            detailsPane.getChildren().add(placeholder);
        });
        
        VBox dismissBox = new VBox(dismissBtn);
        dismissBox.setPadding(new Insets(15, 0, 0, 0));
        dismissBox.setAlignment(Pos.CENTER_LEFT);
        
        detailsPane.getChildren().addAll(header, statsBox, dismissBox);
    }
    
    private void addStatRow(VBox container, String label, String value, String valueColor) {
        HBox row = new HBox(5);
        
        Label labelL = new Label(label + ":");
        labelL.setTextFill(Color.web(TEXT_DIM));
        labelL.setFont(Font.font("Georgia", 10));
        labelL.setMinWidth(80);
        
        Label valueL = new Label(value);
        valueL.setTextFill(Color.web(valueColor));
        valueL.setFont(Font.font("Georgia", FontWeight.BOLD, 10));
        
        row.getChildren().addAll(labelL, valueL);
        container.getChildren().add(row);
    }
    
    private HBox createButtonBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(10, 12, 12, 12));
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setStyle(
            "-fx-background-color: " + BG_MED + ";" +
            "-fx-background-radius: 0 0 6 6;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        // Formation selector
        Label formLabel = new Label("Formation:");
        formLabel.setTextFill(Color.web(TEXT_DIM));
        formLabel.setFont(Font.font("Georgia", 10));
        
        ComboBox<String> formationBox = new ComboBox<>();
        formationBox.getItems().addAll("Line", "Wedge", "Square", "Spread");
        formationBox.setValue(party.getFormation().name().substring(0, 1) + 
                             party.getFormation().name().substring(1).toLowerCase());
        formationBox.setStyle(
            "-fx-background-color: " + BG_LIGHT + ";" +
            "-fx-font-family: Georgia;" +
            "-fx-font-size: 10;"
        );
        formationBox.setOnAction(e -> {
            String val = formationBox.getValue().toUpperCase();
            party.setFormation(Party.Formation.valueOf(val));
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        bar.getChildren().addAll(formLabel, formationBox, spacer);
        return bar;
    }
    
    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }
    
    public void refresh() {
        refreshMemberList();
        updateStatsLabel();
    }
}
