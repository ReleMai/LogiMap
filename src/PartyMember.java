import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a member of the player's party.
 * Based on Mount & Blade style companion system.
 */
public class PartyMember {
    
    // Member types/roles
    public enum Role {
        WARRIOR("Warrior", "⚔", 10, 2, 1.0),      // Good at combat
        SCOUT("Scout", "🏹", 5, 4, 1.2),           // Fast, ranged
        HEALER("Healer", "✚", 3, 3, 0.9),         // Can heal party
        MERCHANT("Merchant", "💰", 4, 3, 1.0),     // Trading bonuses
        LABORER("Laborer", "🔨", 6, 2, 0.8);       // Gathering bonuses
        
        private final String name;
        private final String icon;
        private final int baseAttack;
        private final int baseSpeed;
        private final double wageMod;
        
        Role(String name, String icon, int attack, int speed, double wageMod) {
            this.name = name;
            this.icon = icon;
            this.baseAttack = attack;
            this.baseSpeed = speed;
            this.wageMod = wageMod;
        }
        
        public String getName() { return name; }
        public String getIcon() { return icon; }
        public int getBaseAttack() { return baseAttack; }
        public int getBaseSpeed() { return baseSpeed; }
        public double getWageModifier() { return wageMod; }
    }
    
    // Identity
    private String name;
    private Role role;
    private boolean isMale;
    private String professionName; // Original profession for display (e.g., "Blacksmith", "Archer")
    
    // Stats
    private int level = 1;
    private int experience = 0;
    private int maxHealth = 100;
    private int currentHealth = 100;
    private int attack;
    private int defense;
    private int speed;
    private int morale = 100; // 0-100, affects combat effectiveness
    
    // Wages
    private int weeklyWage; // In copper
    
    // Appearance (for sprite rendering)
    private Color hairColor;
    private Color skinColor;
    private Color clothingColor;
    
    // Traits that affect gameplay
    private Map<String, Integer> skills; // skill name -> level
    
    private Random random = new Random();
    
    /**
     * Creates a party member with the given name and role.
     */
    public PartyMember(String name, Role role) {
        this.name = name;
        this.role = role;
        this.isMale = random.nextBoolean();
        
        // Initialize stats based on role
        this.attack = role.getBaseAttack() + random.nextInt(3);
        this.defense = 2 + random.nextInt(3);
        this.speed = role.getBaseSpeed() + random.nextInt(2);
        
        // Calculate wage based on role and stats
        this.weeklyWage = calculateWage();
        
        // Random appearance
        setupAppearance();
        
        // Initialize skills
        skills = new HashMap<>();
        initializeSkills();
    }
    
    /**
     * Creates a random party member of the given role.
     */
    public static PartyMember createRandom(Role role) {
        String name = NameGenerator.generateName(false);
        return new PartyMember(name, role);
    }
    
    /**
     * Creates a random party member with a random role.
     */
    public static PartyMember createRandom() {
        Role[] roles = Role.values();
        Role role = roles[new Random().nextInt(roles.length)];
        return createRandom(role);
    }
    
    /**
     * Calculates weekly wage based on level and role.
     */
    private int calculateWage() {
        int baseWage = 10 + (level * 5);
        return (int)(baseWage * role.getWageModifier());
    }
    
    /**
     * Sets up random appearance colors.
     */
    private void setupAppearance() {
        // Hair colors
        Color[] hairColors = {
            Color.web("#2C1810"), // Dark brown
            Color.web("#4A3728"), // Brown
            Color.web("#1C1C1C"), // Black
            Color.web("#8B4513"), // Auburn
            Color.web("#FFD700"), // Blonde
            Color.web("#808080")  // Grey
        };
        hairColor = hairColors[random.nextInt(hairColors.length)];
        
        // Skin tones
        Color[] skinColors = {
            Color.web("#FDBCB4"), // Light
            Color.web("#E8BEAC"), // Medium light
            Color.web("#D4A484"), // Medium
            Color.web("#C68642"), // Medium dark
            Color.web("#8D5524")  // Dark
        };
        skinColor = skinColors[random.nextInt(skinColors.length)];
        
        // Clothing based on role
        clothingColor = switch (role) {
            case WARRIOR -> Color.web("#4A4A4A"); // Dark grey/armor
            case SCOUT -> Color.web("#228B22");   // Forest green
            case HEALER -> Color.web("#F0F0F0"); // White robes
            case MERCHANT -> Color.web("#8B4513"); // Brown
            case LABORER -> Color.web("#6B5B3E"); // Tan
        };
    }
    
    /**
     * Initializes skills based on role.
     */
    private void initializeSkills() {
        switch (role) {
            case WARRIOR -> {
                skills.put("Combat", 3 + random.nextInt(3));
                skills.put("Tactics", 1 + random.nextInt(3));
            }
            case SCOUT -> {
                skills.put("Tracking", 3 + random.nextInt(3));
                skills.put("Archery", 2 + random.nextInt(3));
            }
            case HEALER -> {
                skills.put("Medicine", 3 + random.nextInt(3));
                skills.put("Herbalism", 2 + random.nextInt(3));
            }
            case MERCHANT -> {
                skills.put("Trading", 3 + random.nextInt(3));
                skills.put("Persuasion", 2 + random.nextInt(3));
            }
            case LABORER -> {
                skills.put("Mining", 2 + random.nextInt(3));
                skills.put("Woodcutting", 2 + random.nextInt(3));
                skills.put("Farming", 2 + random.nextInt(3));
            }
        }
    }
    
    /**
     * Adds experience and handles leveling up.
     */
    public void addExperience(int amount) {
        experience += amount;
        
        // Level up check (100 XP per level, increasing)
        int xpNeeded = level * 100;
        while (experience >= xpNeeded) {
            experience -= xpNeeded;
            levelUp();
            xpNeeded = level * 100;
        }
    }
    
    /**
     * Handles level up.
     */
    private void levelUp() {
        level++;
        maxHealth += 10;
        currentHealth = maxHealth;
        attack += random.nextInt(2) + 1;
        defense += random.nextInt(2);
        weeklyWage = calculateWage();
    }
    
    /**
     * Takes damage and returns true if still alive.
     */
    public boolean takeDamage(int amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            return false; // Knocked out
        }
        return true;
    }
    
    /**
     * Heals the member.
     */
    public void heal(int amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }
    
    /**
     * Renders a small portrait of this party member.
     */
    public void renderPortrait(GraphicsContext gc, double x, double y, double size) {
        // Background
        gc.setFill(Color.web("#1a1208"));
        gc.fillRoundRect(x, y, size, size, 4, 4);
        
        // Border based on morale
        Color borderColor = morale >= 75 ? Color.web("#4CAF50") :
                           morale >= 50 ? Color.web("#FFC107") :
                           morale >= 25 ? Color.web("#FF9800") : Color.web("#F44336");
        gc.setStroke(borderColor);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, size, size, 4, 4);
        
        // Face
        double faceSize = size * 0.6;
        double faceX = x + (size - faceSize) / 2;
        double faceY = y + size * 0.15;
        
        gc.setFill(skinColor);
        gc.fillOval(faceX, faceY, faceSize, faceSize);
        
        // Hair
        gc.setFill(hairColor);
        gc.fillArc(faceX, faceY, faceSize, faceSize * 0.5, 0, 180, javafx.scene.shape.ArcType.ROUND);
        
        // Eyes
        gc.setFill(Color.BLACK);
        double eyeSize = faceSize * 0.12;
        double eyeY = faceY + faceSize * 0.4;
        gc.fillOval(faceX + faceSize * 0.25, eyeY, eyeSize, eyeSize);
        gc.fillOval(faceX + faceSize * 0.6, eyeY, eyeSize, eyeSize);
        
        // Body hint
        gc.setFill(clothingColor);
        gc.fillRoundRect(x + size * 0.2, y + size * 0.7, size * 0.6, size * 0.25, 3, 3);
        
        // Role icon
        gc.setFill(Color.web("#c4a574"));
        gc.setFont(javafx.scene.text.Font.font("Georgia", size * 0.2));
        gc.fillText(role.getIcon(), x + size * 0.05, y + size * 0.95);
        
        // Health bar (if damaged)
        if (currentHealth < maxHealth) {
            double barWidth = size * 0.8;
            double barHeight = 3;
            double barX = x + size * 0.1;
            double barY = y + size - 5;
            
            // Background
            gc.setFill(Color.web("#333333"));
            gc.fillRect(barX, barY, barWidth, barHeight);
            
            // Health
            double healthPct = (double) currentHealth / maxHealth;
            Color healthColor = healthPct > 0.5 ? Color.GREEN : healthPct > 0.25 ? Color.YELLOW : Color.RED;
            gc.setFill(healthColor);
            gc.fillRect(barX, barY, barWidth * healthPct, barHeight);
        }
    }
    
    /**
     * Renders the member as a mini-sprite for map display.
     */
    public void renderMapSprite(GraphicsContext gc, double x, double y, double size) {
        // Simple body
        gc.setFill(clothingColor);
        gc.fillRoundRect(x + size * 0.3, y + size * 0.4, size * 0.4, size * 0.5, 3, 3);
        
        // Head
        gc.setFill(skinColor);
        double headSize = size * 0.35;
        gc.fillOval(x + (size - headSize) / 2, y + size * 0.1, headSize, headSize);
        
        // Hair
        gc.setFill(hairColor);
        gc.fillArc(x + (size - headSize) / 2, y + size * 0.1, headSize, headSize * 0.5, 0, 180, javafx.scene.shape.ArcType.ROUND);
    }
    
    // Getters
    public String getName() { return name; }
    public Role getRole() { return role; }
    public boolean isMale() { return isMale; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public int getMorale() { return morale; }
    public int getWeeklyWage() { return weeklyWage; }
    public Map<String, Integer> getSkills() { return skills; }
    
    public void setMorale(int morale) { 
        this.morale = Math.max(0, Math.min(100, morale)); 
    }
    
    public void setProfessionName(String professionName) {
        this.professionName = professionName;
    }
    
    public String getProfessionName() {
        return professionName != null ? professionName : role.getName();
    }
    
    public void setGender(boolean isMale) {
        this.isMale = isMale;
    }
    
    /**
     * Transfers stats from CharacterStats to this PartyMember.
     */
    public void setStatsFromCharacterStats(CharacterStats stats) {
        if (stats == null) return;
        
        // Use CharacterStats values to initialize our stats
        this.attack = stats.getStat(CharacterStats.Stat.STR);
        this.defense = stats.getStat(CharacterStats.Stat.CON);
        this.speed = stats.getStat(CharacterStats.Stat.DEX);
        this.maxHealth = stats.getMaxHealth();
        this.currentHealth = maxHealth;
    }
    
    /**
     * Calculates total combat power.
     */
    public int getCombatPower() {
        double moraleMod = 0.5 + (morale / 200.0); // 0.5 to 1.0
        return (int)((attack + defense) * moraleMod);
    }
    
    /**
     * Converts party member to a save-friendly map.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("role", role.name());
        data.put("isMale", isMale);
        data.put("level", level);
        data.put("experience", experience);
        data.put("maxHealth", maxHealth);
        data.put("currentHealth", currentHealth);
        data.put("attack", attack);
        data.put("defense", defense);
        data.put("speed", speed);
        data.put("morale", morale);
        data.put("weeklyWage", weeklyWage);
        data.put("skills", skills);
        return data;
    }
    
    /**
     * Creates a party member from saved data.
     */
    @SuppressWarnings("unchecked")
    public static PartyMember fromMap(Map<String, Object> data) {
        try {
            String name = (String) data.get("name");
            Role role = Role.valueOf((String) data.get("role"));
            
            PartyMember member = new PartyMember(name, role);
            member.isMale = (Boolean) data.getOrDefault("isMale", true);
            member.level = (Integer) data.getOrDefault("level", 1);
            member.experience = (Integer) data.getOrDefault("experience", 0);
            member.maxHealth = (Integer) data.getOrDefault("maxHealth", 100);
            member.currentHealth = (Integer) data.getOrDefault("currentHealth", 100);
            member.attack = (Integer) data.getOrDefault("attack", 5);
            member.defense = (Integer) data.getOrDefault("defense", 3);
            member.speed = (Integer) data.getOrDefault("speed", 3);
            member.morale = (Integer) data.getOrDefault("morale", 100);
            member.weeklyWage = (Integer) data.getOrDefault("weeklyWage", 10);
            
            Map<String, Integer> savedSkills = (Map<String, Integer>) data.get("skills");
            if (savedSkills != null) {
                member.skills = savedSkills;
            }
            
            return member;
        } catch (Exception e) {
            System.err.println("Failed to load party member: " + e.getMessage());
            return null;
        }
    }
}
