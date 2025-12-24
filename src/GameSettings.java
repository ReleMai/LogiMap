import java.io.*;
import java.util.Properties;

/**
 * GameSettings - Manages all game settings with persistence.
 * Settings are saved to a properties file and loaded on startup.
 */
public class GameSettings {
    
    private static GameSettings instance;
    private static final String SETTINGS_FILE = "settings.properties";
    
    // Display Settings
    public enum DisplayMode {
        FULLSCREEN("Fullscreen"),
        WINDOWED_FULLSCREEN("Windowed Fullscreen"),
        WINDOWED("Windowed");
        
        private final String displayName;
        
        DisplayMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static DisplayMode fromString(String str) {
            for (DisplayMode mode : values()) {
                if (mode.name().equals(str) || mode.displayName.equals(str)) {
                    return mode;
                }
            }
            return WINDOWED;
        }
    }
    
    public enum Resolution {
        RES_1280x720(1280, 720, "1280 x 720 (HD)"),
        RES_1366x768(1366, 768, "1366 x 768"),
        RES_1600x900(1600, 900, "1600 x 900"),
        RES_1920x1080(1920, 1080, "1920 x 1080 (Full HD)"),
        RES_2560x1440(2560, 1440, "2560 x 1440 (QHD)"),
        RES_3840x2160(3840, 2160, "3840 x 2160 (4K)");
        
        public final int width;
        public final int height;
        public final String displayName;
        
        Resolution(int width, int height, String displayName) {
            this.width = width;
            this.height = height;
            this.displayName = displayName;
        }
        
        public static Resolution fromString(String str) {
            for (Resolution res : values()) {
                if (res.name().equals(str) || res.displayName.equals(str)) {
                    return res;
                }
            }
            return RES_1920x1080;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Settings values
    private DisplayMode displayMode = DisplayMode.WINDOWED;
    private Resolution resolution = Resolution.RES_1920x1080;
    private boolean vsync = true;
    private int musicVolume = 70;
    private int sfxVolume = 80;
    private boolean autoSave = true;
    private int autoSaveInterval = 5; // minutes
    private boolean showTutorialTips = true;
    private double scrollSpeed = 1.0;
    private double zoomSpeed = 1.0;
    private boolean moveToInteract = true; // Right-click moves player to interactable objects
    
    // New graphics/gameplay settings
    private boolean basicGraphicsMode = false; // Simple textures, no decorations, no animations
    private boolean showGrid = true;           // Show map grid
    private boolean instantTownMenu = false;   // Skip guard conversation, open town menu directly
    private double gameSpeed = 1.0;            // Game speed multiplier (0.5x, 1.0x, 2.0x, 4.0x)
    private boolean teleportCheatEnabled = false; // Allow ctrl+click teleport cheat
    
    // Game speed presets
    public static final double SPEED_HALF = 0.5;
    public static final double SPEED_NORMAL = 1.0;
    public static final double SPEED_FAST = 2.0;
    public static final double SPEED_FASTEST = 4.0;
    
    // Listeners for settings changes
    private Runnable onSettingsChanged;
    
    private GameSettings() {
        load();
    }
    
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    // ==================== Getters ====================
    
    public DisplayMode getDisplayMode() { return displayMode; }
    public Resolution getResolution() { return resolution; }
    public boolean isVsync() { return vsync; }
    public int getMusicVolume() { return musicVolume; }
    public int getSfxVolume() { return sfxVolume; }
    public boolean isAutoSave() { return autoSave; }
    public int getAutoSaveInterval() { return autoSaveInterval; }
    public boolean isShowTutorialTips() { return showTutorialTips; }
    public double getScrollSpeed() { return scrollSpeed; }
    public double getZoomSpeed() { return zoomSpeed; }
    public boolean isMoveToInteract() { return moveToInteract; }
    public boolean isBasicGraphicsMode() { return basicGraphicsMode; }
    public boolean isShowGrid() { return showGrid; }
    public boolean isInstantTownMenu() { return instantTownMenu; }
    public double getGameSpeed() { return gameSpeed; }
    public boolean isTeleportCheatEnabled() { return teleportCheatEnabled; }
    
    // ==================== Setters ====================
    
    public void setDisplayMode(DisplayMode mode) {
        this.displayMode = mode;
        notifyChanged();
    }
    
    public void setResolution(Resolution res) {
        this.resolution = res;
        notifyChanged();
    }
    
    public void setVsync(boolean vsync) {
        this.vsync = vsync;
        notifyChanged();
    }
    
    public void setMusicVolume(int volume) {
        this.musicVolume = Math.max(0, Math.min(100, volume));
        notifyChanged();
    }
    
    public void setSfxVolume(int volume) {
        this.sfxVolume = Math.max(0, Math.min(100, volume));
        notifyChanged();
    }
    
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        notifyChanged();
    }
    
    public void setAutoSaveInterval(int minutes) {
        this.autoSaveInterval = Math.max(1, Math.min(60, minutes));
        notifyChanged();
    }
    
    public void setShowTutorialTips(boolean show) {
        this.showTutorialTips = show;
        notifyChanged();
    }
    
    public void setScrollSpeed(double speed) {
        this.scrollSpeed = Math.max(0.1, Math.min(3.0, speed));
        notifyChanged();
    }
    
    public void setZoomSpeed(double speed) {
        this.zoomSpeed = Math.max(0.1, Math.min(3.0, speed));
        notifyChanged();
    }
    
    public void setMoveToInteract(boolean moveToInteract) {
        this.moveToInteract = moveToInteract;
        notifyChanged();
    }
    
    public void setBasicGraphicsMode(boolean basicGraphicsMode) {
        this.basicGraphicsMode = basicGraphicsMode;
        notifyChanged();
    }
    
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        notifyChanged();
    }
    
    public void setInstantTownMenu(boolean instantTownMenu) {
        this.instantTownMenu = instantTownMenu;
        notifyChanged();
    }
    
    public void setGameSpeed(double speed) {
        // Clamp to valid presets
        if (speed <= SPEED_HALF) {
            this.gameSpeed = SPEED_HALF;
        } else if (speed >= SPEED_FASTEST) {
            this.gameSpeed = SPEED_FASTEST;
        } else if (speed >= SPEED_FAST) {
            this.gameSpeed = SPEED_FAST;
        } else {
            this.gameSpeed = SPEED_NORMAL;
        }
        notifyChanged();
    }

    public void setTeleportCheatEnabled(boolean enabled) {
        this.teleportCheatEnabled = enabled;
        notifyChanged();
    }
    
    /**
     * Cycles to the next game speed preset.
     * 0.5x -> 1.0x -> 2.0x -> 4.0x -> 0.5x
     */
    public void cycleGameSpeed() {
        if (gameSpeed <= SPEED_HALF) {
            gameSpeed = SPEED_NORMAL;
        } else if (gameSpeed <= SPEED_NORMAL) {
            gameSpeed = SPEED_FAST;
        } else if (gameSpeed <= SPEED_FAST) {
            gameSpeed = SPEED_FASTEST;
        } else {
            gameSpeed = SPEED_HALF;
        }
        notifyChanged();
    }
    
    /**
     * Gets display name for current game speed.
     */
    public String getGameSpeedName() {
        if (gameSpeed <= SPEED_HALF) return "0.5x (Slow)";
        if (gameSpeed <= SPEED_NORMAL) return "1.0x (Normal)";
        if (gameSpeed <= SPEED_FAST) return "2.0x (Fast)";
        return "4.0x (Fastest)";
    }

    public void setOnSettingsChanged(Runnable callback) {
        this.onSettingsChanged = callback;
    }
    
    private void notifyChanged() {
        if (onSettingsChanged != null) {
            onSettingsChanged.run();
        }
    }
    
    // ==================== Persistence ====================
    
    public void save() {
        Properties props = new Properties();
        
        props.setProperty("displayMode", displayMode.name());
        props.setProperty("resolution", resolution.name());
        props.setProperty("vsync", String.valueOf(vsync));
        props.setProperty("musicVolume", String.valueOf(musicVolume));
        props.setProperty("sfxVolume", String.valueOf(sfxVolume));
        props.setProperty("autoSave", String.valueOf(autoSave));
        props.setProperty("autoSaveInterval", String.valueOf(autoSaveInterval));
        props.setProperty("showTutorialTips", String.valueOf(showTutorialTips));
        props.setProperty("scrollSpeed", String.valueOf(scrollSpeed));
        props.setProperty("zoomSpeed", String.valueOf(zoomSpeed));
        props.setProperty("moveToInteract", String.valueOf(moveToInteract));
        props.setProperty("basicGraphicsMode", String.valueOf(basicGraphicsMode));
        props.setProperty("showGrid", String.valueOf(showGrid));
        props.setProperty("instantTownMenu", String.valueOf(instantTownMenu));
        props.setProperty("gameSpeed", String.valueOf(gameSpeed));
        props.setProperty("teleportCheatEnabled", String.valueOf(teleportCheatEnabled));
        
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            props.store(fos, "LogiMap Game Settings");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
    
    public void load() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return; // Use defaults
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            
            displayMode = DisplayMode.fromString(props.getProperty("displayMode", "WINDOWED"));
            resolution = Resolution.fromString(props.getProperty("resolution", "RES_1920x1080"));
            vsync = Boolean.parseBoolean(props.getProperty("vsync", "true"));
            musicVolume = Integer.parseInt(props.getProperty("musicVolume", "70"));
            sfxVolume = Integer.parseInt(props.getProperty("sfxVolume", "80"));
            autoSave = Boolean.parseBoolean(props.getProperty("autoSave", "true"));
            autoSaveInterval = Integer.parseInt(props.getProperty("autoSaveInterval", "5"));
            showTutorialTips = Boolean.parseBoolean(props.getProperty("showTutorialTips", "true"));
            scrollSpeed = Double.parseDouble(props.getProperty("scrollSpeed", "1.0"));
            zoomSpeed = Double.parseDouble(props.getProperty("zoomSpeed", "1.0"));
            moveToInteract = Boolean.parseBoolean(props.getProperty("moveToInteract", "true"));
            basicGraphicsMode = Boolean.parseBoolean(props.getProperty("basicGraphicsMode", "false"));
            showGrid = Boolean.parseBoolean(props.getProperty("showGrid", "true"));
            instantTownMenu = Boolean.parseBoolean(props.getProperty("instantTownMenu", "false"));
            gameSpeed = Double.parseDouble(props.getProperty("gameSpeed", "1.0"));
            teleportCheatEnabled = Boolean.parseBoolean(props.getProperty("teleportCheatEnabled", "false"));
            
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
        }
    }
    
    /**
     * Resets all settings to defaults.
     */
    public void resetToDefaults() {
        displayMode = DisplayMode.WINDOWED;
        resolution = Resolution.RES_1920x1080;
        vsync = true;
        musicVolume = 70;
        sfxVolume = 80;
        autoSave = true;
        autoSaveInterval = 5;
        showTutorialTips = true;
        scrollSpeed = 1.0;
        zoomSpeed = 1.0;
        moveToInteract = true;
        basicGraphicsMode = false;
        showGrid = true;
        instantTownMenu = false;
        gameSpeed = SPEED_NORMAL;
        teleportCheatEnabled = false;
        notifyChanged();
    }
}
