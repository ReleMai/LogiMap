import java.util.Random;

/**
 * Represents an enemy in combat.
 * Can be bandits, wild animals, or other hostile entities.
 */
public class Enemy {
    
    // Enemy types
    public enum Type {
        // Bandits
        BANDIT_SCOUT("Bandit Scout", 5, 2, 4, 10, 3, "🗡️"),
        BANDIT("Bandit", 8, 4, 3, 20, 5, "⚔️"),
        BANDIT_LEADER("Bandit Leader", 15, 8, 3, 50, 15, "👑"),
        
        // Wildlife
        WOLF("Wolf", 6, 2, 5, 15, 2, "🐺"),
        BEAR("Bear", 12, 6, 2, 40, 8, "🐻"),
        BOAR("Boar", 8, 4, 3, 25, 5, "🐗"),
        
        // Monsters
        GOBLIN("Goblin", 4, 2, 4, 8, 2, "👺"),
        ORC("Orc", 12, 6, 2, 35, 10, "👹"),
        TROLL("Troll", 20, 10, 1, 100, 25, "🧌");
        
        private final String name;
        private final int baseAttack;
        private final int baseDefense;
        private final int baseSpeed;
        private final int baseHealth;
        private final int baseGold;
        private final String icon;
        
        Type(String name, int attack, int defense, int speed, int health, int gold, String icon) {
            this.name = name;
            this.baseAttack = attack;
            this.baseDefense = defense;
            this.baseSpeed = speed;
            this.baseHealth = health;
            this.baseGold = gold;
            this.icon = icon;
        }
        
        public String getName() { return name; }
        public int getBaseAttack() { return baseAttack; }
        public int getBaseDefense() { return baseDefense; }
        public int getBaseSpeed() { return baseSpeed; }
        public int getBaseHealth() { return baseHealth; }
        public int getBaseGold() { return baseGold; }
        public String getIcon() { return icon; }
    }
    
    // Identity
    private String name;
    private Type type;
    private int level;
    
    // Stats
    private int maxHealth;
    private int currentHealth;
    private int attack;
    private int defense;
    private int speed;
    
    // Loot
    private int goldDrop;
    private int experienceValue;
    
    private Random random = new Random();
    
    /**
     * Creates an enemy of the specified type and level.
     */
    public Enemy(Type type, int level) {
        this.type = type;
        this.level = Math.max(1, level);
        this.name = type.getName();
        
        // Scale stats by level
        double levelMod = 1.0 + (level - 1) * 0.15;
        this.maxHealth = (int)(type.getBaseHealth() * levelMod);
        this.currentHealth = maxHealth;
        this.attack = (int)(type.getBaseAttack() * levelMod);
        this.defense = (int)(type.getBaseDefense() * levelMod);
        this.speed = type.getBaseSpeed();
        
        // Calculate loot
        this.goldDrop = (int)(type.getBaseGold() * levelMod) + random.nextInt(5);
        this.experienceValue = (int)((type.getBaseHealth() + type.getBaseAttack()) * levelMod / 2);
    }
    
    /**
     * Creates a random enemy from a group.
     */
    public static Enemy createRandom(EnemyGroup group, int areaLevel) {
        Type[] types = group.getTypes();
        Type type = types[new Random().nextInt(types.length)];
        int level = Math.max(1, areaLevel + new Random().nextInt(3) - 1);
        return new Enemy(type, level);
    }
    
    /**
     * Takes damage and returns remaining health.
     */
    public int takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense / 2);
        currentHealth = Math.max(0, currentHealth - actualDamage);
        return currentHealth;
    }
    
    /**
     * Heals the enemy.
     */
    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
    
    // Getters
    public String getName() { return name; }
    public Type getType() { return type; }
    public int getLevel() { return level; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public int getGoldDrop() { return goldDrop; }
    public int getExperienceValue() { return experienceValue; }
    public String getIcon() { return type.getIcon(); }
    
    /**
     * Gets a display string for the enemy.
     */
    public String getDisplayString() {
        return type.getIcon() + " " + name + " (Lv." + level + ") " + currentHealth + "/" + maxHealth + " HP";
    }
    
    /**
     * Enemy group types for spawning.
     */
    public enum EnemyGroup {
        BANDITS(new Type[]{Type.BANDIT_SCOUT, Type.BANDIT, Type.BANDIT_LEADER}),
        WILDLIFE(new Type[]{Type.WOLF, Type.BEAR, Type.BOAR}),
        GOBLINS(new Type[]{Type.GOBLIN}),
        ORCS(new Type[]{Type.ORC}),
        MONSTERS(new Type[]{Type.GOBLIN, Type.ORC, Type.TROLL});
        
        private final Type[] types;
        
        EnemyGroup(Type[] types) {
            this.types = types;
        }
        
        public Type[] getTypes() { return types; }
    }
}
