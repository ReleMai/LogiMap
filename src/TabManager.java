import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TabManager {
    
    private HBox tabBar;
    private LogiMapUI parentUI;
    private Button[] tabButtons;
    private String currentTab = "Map";
    
    private static final String DARK_BG = "#1a1a1a";
    private static final String MEDIUM_BG = "#2a2a2a";
    private static final String LIGHT_BG = "#3a3a3a";
    private static final String ACCENT_COLOR = "#4a9eff";
    private static final String TEXT_COLOR = "#e0e0e0";
    
    private static final String[] TAB_NAMES = {
        "Map", "Region", "Social"
    };
    
    private static final String[] TAB_ICONS = {
        "🗺", "🌍", "👥"
    };
    
    public TabManager(LogiMapUI parentUI) {
        this.parentUI = parentUI;
        createTabBar();
    }
    
    private void createTabBar() {
        tabBar = new HBox(0);
        tabBar.setPadding(new Insets(0));
        tabBar.setStyle("-fx-background-color: " + MEDIUM_BG + "; -fx-border-color: " + ACCENT_COLOR + "; -fx-border-width: 0 0 2 0;");
        tabBar.setAlignment(Pos.CENTER_LEFT);
        
        tabButtons = new Button[TAB_NAMES.length];
        
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final String tabName = TAB_NAMES[i];
            Button tabButton = new Button(TAB_ICONS[i] + " " + tabName);
            tabButton.getStyleClass().add("tab-button");
            
            // Style the button
            updateTabButtonStyle(tabButton, i == 0); // First tab (Map) is active by default
            
            tabButton.setOnAction(e -> selectTab(tabName));
            
            tabButtons[i] = tabButton;
            tabBar.getChildren().add(tabButton);
        }
        
        // Add spacer to push tabs to the left
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        tabBar.getChildren().add(spacer);
    }
    
    private void selectTab(String tabName) {
        currentTab = tabName;
        
        // Update all tab button styles
        for (int i = 0; i < tabButtons.length; i++) {
            boolean isActive = TAB_NAMES[i].equals(tabName);
            updateTabButtonStyle(tabButtons[i], isActive);
        }
        
        // Notify parent UI of tab change
        parentUI.switchTab(tabName);
    }
    
    private void updateTabButtonStyle(Button button, boolean isActive) {
        if (isActive) {
            button.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 12 25 12 25;" +
                "-fx-border-color: transparent;" +
                "-fx-cursor: hand;"
            );
        } else {
            button.setStyle(
                "-fx-background-color: " + LIGHT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-font-weight: normal;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 12 25 12 25;" +
                "-fx-border-color: " + MEDIUM_BG + ";" +
                "-fx-border-width: 0 1 0 0;" +
                "-fx-cursor: hand;"
            );
        }
    }
    
    public HBox getTabBar() {
        return tabBar;
    }
    
    public String getCurrentTab() {
        return currentTab;
    }
}
