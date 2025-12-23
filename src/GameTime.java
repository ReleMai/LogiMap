import javafx.scene.paint.Color;

/**
 * Game time system managing day/night cycle, time passage, and time-based events.
 * 
 * Time flows at an accelerated rate: 1 real second = configurable game minutes.
 * A full day is 24 game hours (1440 game minutes).
 * 
 * Sun rises in the WEST (left side of map) at 6:00
 * Sun sets in the EAST (right side of map) at 20:00
 */
public class GameTime {
    
    // Time constants
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY; // 1440
    
    // Time of day thresholds (in hours)
    public static final int DAWN_START = 5;      // 5:00 AM - Dawn begins
    public static final int SUNRISE = 6;         // 6:00 AM - Sun rises
    public static final int MORNING = 8;         // 8:00 AM - Full daylight
    public static final int NOON = 12;           // 12:00 PM - Midday
    public static final int AFTERNOON = 16;      // 4:00 PM - Late afternoon
    public static final int SUNSET = 19;         // 7:00 PM - Sunset begins
    public static final int DUSK = 20;           // 8:00 PM - Sun sets
    public static final int NIGHT = 21;          // 9:00 PM - Full night
    
    // Time flow rate: real milliseconds per game minute
    // Default: 1000ms (1 second) = 1 game minute, so 24 real minutes = 1 game day
    private double realMsPerGameMinute = 1000.0;
    
    // Current time state
    private int currentDay = 1;
    private int currentHour = 8;      // Start at 8:00 AM
    private int currentMinute = 0;
    private double minuteFraction = 0; // Accumulated sub-minute time
    
    // Time flow control
    private boolean paused = false;
    private double timeMultiplier = 1.0; // Can speed up/slow down time
    
    // Cached values for rendering
    private TimeOfDay cachedTimeOfDay = TimeOfDay.MORNING;
    private double cachedSunPosition = 0.5; // 0 = west horizon, 0.5 = zenith, 1 = east horizon
    private Color cachedSkyTint = Color.WHITE;
    
    /**
     * Time of day periods with associated properties.
     */
    public enum TimeOfDay {
        DAWN("Dawn", 5, 6, true, 0.3),
        MORNING("Morning", 6, 12, true, 1.0),
        AFTERNOON("Afternoon", 12, 19, true, 1.0),
        DUSK("Dusk", 19, 21, true, 0.4),
        NIGHT("Night", 21, 5, false, 0.1);
        
        private final String displayName;
        private final int startHour;
        private final int endHour;
        private final boolean canFarm;
        private final double visibility; // 0.0 to 1.0
        
        TimeOfDay(String displayName, int startHour, int endHour, boolean canFarm, double visibility) {
            this.displayName = displayName;
            this.startHour = startHour;
            this.endHour = endHour;
            this.canFarm = canFarm;
            this.visibility = visibility;
        }
        
        public String getDisplayName() { return displayName; }
        public int getStartHour() { return startHour; }
        public int getEndHour() { return endHour; }
        public boolean canFarm() { return canFarm; }
        public double getVisibility() { return visibility; }
    }
    
    /**
     * Creates a new game time system starting at day 1, 8:00 AM.
     */
    public GameTime() {
        updateCachedValues();
    }
    
    /**
     * Creates a game time system with custom starting time.
     */
    public GameTime(int day, int hour, int minute) {
        this.currentDay = day;
        this.currentHour = hour;
        this.currentMinute = minute;
        updateCachedValues();
    }
    
    // ==================== TIME ADVANCEMENT ====================
    
    /**
     * Updates time based on elapsed real time (in milliseconds).
     * Call this every frame with deltaTime.
     */
    public void update(double deltaMs) {
        if (paused) return;
        
        // Accumulate time
        double gameMinutesElapsed = (deltaMs / realMsPerGameMinute) * timeMultiplier;
        minuteFraction += gameMinutesElapsed;
        
        // Process full minutes
        while (minuteFraction >= 1.0) {
            minuteFraction -= 1.0;
            advanceMinute();
        }
    }
    
    /**
     * Advances time by one minute.
     */
    private void advanceMinute() {
        currentMinute++;
        if (currentMinute >= MINUTES_PER_HOUR) {
            currentMinute = 0;
            advanceHour();
        }
        updateCachedValues();
    }
    
    /**
     * Advances time by one hour.
     */
    private void advanceHour() {
        currentHour++;
        if (currentHour >= HOURS_PER_DAY) {
            currentHour = 0;
            currentDay++;
        }
    }
    
    /**
     * Advances time by a specified number of minutes.
     * Used for waiting and action durations.
     */
    public void advanceTime(int minutes) {
        for (int i = 0; i < minutes; i++) {
            advanceMinute();
        }
    }
    
    /**
     * Advances time by hours and minutes.
     */
    public void advanceTime(int hours, int minutes) {
        advanceTime(hours * MINUTES_PER_HOUR + minutes);
    }
    
    /**
     * Waits until a specific hour of the day.
     * Returns the number of minutes waited.
     */
    public int waitUntil(int targetHour) {
        int minutesWaited = 0;
        while (currentHour != targetHour || currentMinute != 0) {
            advanceMinute();
            minutesWaited++;
            if (minutesWaited > MINUTES_PER_DAY) break; // Safety limit
        }
        return minutesWaited;
    }
    
    /**
     * Waits until the next occurrence of a time of day.
     * Returns the number of minutes waited.
     */
    public int waitUntil(TimeOfDay targetTime) {
        return waitUntil(targetTime.getStartHour());
    }
    
    // ==================== TIME QUERIES ====================
    
    /**
     * Gets the current time of day period.
     */
    public TimeOfDay getTimeOfDay() {
        return cachedTimeOfDay;
    }
    
    /**
     * Calculates and returns the current time of day.
     */
    private TimeOfDay calculateTimeOfDay() {
        if (currentHour >= DAWN_START && currentHour < SUNRISE) {
            return TimeOfDay.DAWN;
        } else if (currentHour >= SUNRISE && currentHour < NOON) {
            return TimeOfDay.MORNING;
        } else if (currentHour >= NOON && currentHour < SUNSET) {
            return TimeOfDay.AFTERNOON;
        } else if (currentHour >= SUNSET && currentHour < NIGHT) {
            return TimeOfDay.DUSK;
        } else {
            return TimeOfDay.NIGHT;
        }
    }
    
    /**
     * Gets the sun's position in the sky.
     * Returns 0.0 at west horizon (sunrise), 0.5 at zenith (noon), 1.0 at east horizon (sunset).
     * Returns -1 when sun is below horizon (night).
     */
    public double getSunPosition() {
        return cachedSunPosition;
    }
    
    /**
     * Calculates sun position based on current time.
     */
    private double calculateSunPosition() {
        // Sun is up from SUNRISE (6:00) to DUSK (20:00) = 14 hours
        int sunriseMinute = SUNRISE * MINUTES_PER_HOUR;
        int sunsetMinute = DUSK * MINUTES_PER_HOUR;
        int currentTotalMinute = currentHour * MINUTES_PER_HOUR + currentMinute;
        
        if (currentTotalMinute < sunriseMinute || currentTotalMinute > sunsetMinute) {
            return -1; // Sun below horizon
        }
        
        // Map time to 0-1 range (sunrise to sunset)
        double dayProgress = (double)(currentTotalMinute - sunriseMinute) / (sunsetMinute - sunriseMinute);
        return dayProgress;
    }
    
    /**
     * Gets the sky tint color based on time of day.
     * Used to modify the map's appearance.
     */
    public Color getSkyTint() {
        return cachedSkyTint;
    }
    
    /**
     * Calculates sky tint based on time of day.
     */
    private Color calculateSkyTint() {
        double sunPos = cachedSunPosition;
        
        if (sunPos < 0) {
            // Night - dark blue tint
            return Color.rgb(40, 50, 80);
        }
        
        // Day colors based on sun position
        if (sunPos < 0.1) {
            // Early sunrise - orange/pink
            double t = sunPos / 0.1;
            return Color.rgb(
                (int)(255 * (0.8 + 0.2 * t)),
                (int)(255 * (0.6 + 0.3 * t)),
                (int)(255 * (0.5 + 0.4 * t))
            );
        } else if (sunPos < 0.2) {
            // Late sunrise - warming up
            double t = (sunPos - 0.1) / 0.1;
            return Color.rgb(
                255,
                (int)(255 * (0.9 + 0.1 * t)),
                (int)(255 * (0.9 + 0.1 * t))
            );
        } else if (sunPos < 0.8) {
            // Midday - full daylight
            return Color.rgb(255, 255, 255);
        } else if (sunPos < 0.9) {
            // Early sunset - warming
            double t = (sunPos - 0.8) / 0.1;
            return Color.rgb(
                255,
                (int)(255 * (1.0 - 0.1 * t)),
                (int)(255 * (1.0 - 0.2 * t))
            );
        } else {
            // Late sunset - orange/red
            double t = (sunPos - 0.9) / 0.1;
            return Color.rgb(
                (int)(255 * (1.0 - 0.2 * t)),
                (int)(255 * (0.9 - 0.3 * t)),
                (int)(255 * (0.8 - 0.4 * t))
            );
        }
    }
    
    /**
     * Gets the ambient light level (0.0 to 1.0).
     */
    public double getAmbientLight() {
        return cachedTimeOfDay.getVisibility();
    }
    
    /**
     * Checks if it's currently daytime (can perform outdoor activities).
     */
    public boolean isDaytime() {
        return cachedSunPosition >= 0;
    }
    
    /**
     * Checks if farming is allowed at the current time.
     */
    public boolean canFarm() {
        return cachedTimeOfDay.canFarm();
    }
    
    /**
     * Checks if it's within working hours (6 AM to 8 PM).
     */
    public boolean isWorkingHours() {
        return currentHour >= SUNRISE && currentHour < DUSK;
    }
    
    // ==================== TIME FORMATTING ====================
    
    /**
     * Gets the formatted time string (e.g., "8:30 AM").
     */
    public String getFormattedTime() {
        int displayHour = currentHour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = currentHour < 12 ? "AM" : "PM";
        return String.format("%d:%02d %s", displayHour, currentMinute, amPm);
    }
    
    /**
     * Gets the formatted time with day (e.g., "Day 1 - 8:30 AM").
     */
    public String getFormattedDateTime() {
        return "Day " + currentDay + " - " + getFormattedTime();
    }
    
    /**
     * Gets the formatted time of day (e.g., "Morning").
     */
    public String getFormattedTimeOfDay() {
        return cachedTimeOfDay.getDisplayName();
    }
    
    /**
     * Gets time remaining until a target hour.
     */
    public String getTimeUntil(int targetHour) {
        int currentTotal = currentHour * 60 + currentMinute;
        int targetTotal = targetHour * 60;
        
        if (targetTotal <= currentTotal) {
            targetTotal += MINUTES_PER_DAY;
        }
        
        int diff = targetTotal - currentTotal;
        int hours = diff / 60;
        int minutes = diff % 60;
        
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
    
    // ==================== CACHED VALUE UPDATE ====================
    
    /**
     * Updates all cached values. Called whenever time changes.
     */
    private void updateCachedValues() {
        cachedTimeOfDay = calculateTimeOfDay();
        cachedSunPosition = calculateSunPosition();
        cachedSkyTint = calculateSkyTint();
    }
    
    // ==================== CONTROL ====================
    
    public void pause() { paused = true; }
    public void resume() { paused = false; }
    public void togglePause() { paused = !paused; }
    public boolean isPaused() { return paused; }
    
    public void setTimeMultiplier(double multiplier) { 
        this.timeMultiplier = Math.max(0.1, Math.min(10.0, multiplier)); 
    }
    public double getTimeMultiplier() { return timeMultiplier; }
    
    public void setRealMsPerGameMinute(double ms) {
        this.realMsPerGameMinute = Math.max(100, ms);
    }
    
    // ==================== GETTERS ====================
    
    public int getCurrentDay() { return currentDay; }
    public int getCurrentHour() { return currentHour; }
    public int getCurrentMinute() { return currentMinute; }
    
    /**
     * Gets total minutes since day 1, 00:00.
     */
    public int getTotalMinutes() {
        return (currentDay - 1) * MINUTES_PER_DAY + currentHour * MINUTES_PER_HOUR + currentMinute;
    }
    
    /**
     * Sets the time directly. Use sparingly.
     */
    public void setTime(int day, int hour, int minute) {
        this.currentDay = Math.max(1, day);
        this.currentHour = hour % HOURS_PER_DAY;
        this.currentMinute = minute % MINUTES_PER_HOUR;
        updateCachedValues();
    }
}
