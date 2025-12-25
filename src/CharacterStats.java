import java.util.Random;

/**
 * Character Stats System for LogiMap.
 * 
 * Primary Attributes (1-20 scale, average human is 10):
 * - STR (Strength): Physical power, melee damage, carry capacity
 * - DEX (Dexterity): Agility, ranged accuracy, dodge chance
 * - CON (Constitution): Health, stamina, disease resistance
 * - INT (Intelligence): Magic power, learning speed, crafting quality
 * - WIS (Wisdom): Perception, willpower, magic resistance
 * - CHA (Charisma): Persuasion, party size, trade prices
 * - LCK (Luck): Critical chance, loot quality, random events
 * 
 * Secondary Stats (derived from primary):
 * - Max Health: CON * 10 + STR * 2
 * - Max Stamina: CON * 5 + DEX * 3
 * - Melee Damage: STR * 2 + DEX / 2
 * - Ranged Damage: DEX * 2 + STR / 2
 * - Magic Power: INT * 3 + WIS
 * - Armor: Base + STR / 2
 * - Dodge: DEX * 2 + LCK
 * - Critical Chance: LCK * 2 + DEX
 * - Trade Modifier: CHA * 2 (percentage discount/bonus)
 * - Max Party Size: 1 + CHA / 3 (rounded down)
 */
public class CharacterStats {
    
    // Primary attribute indices
    public enum Stat {
        STR("Strength", "Physical power and melee damage"),
        DEX("Dexterity", "Agility, accuracy, and dodge"),
        CON("Constitution", "Health and endurance"),
        INT("Intelligence", "Magic power and learning"),
        WIS("Wisdom", "Perception and willpower"),
        CHA("Charisma", "Influence and leadership"),
        LCK("Luck", "Fortune and critical chance");
        
        private final String fullName;
        private final String description;
        
        Stat(String fullName, String description) {
            this.fullName = fullName;
            this.description = description;
        }
        
        public String getFullName() { return fullName; }
        public String getDescription() { return description; }
    }
    
    // Base stat values (1-20 scale)
    private int[] baseStats;
    
    // Temporary modifiers (from buffs, debuffs, equipment)
    private int[] tempModifiers;
    
    // Experience and leveling
    private int level = 1;
    private int experience = 0;
    private int statPoints = 0;  // Unspent stat points
    
    // Random for stat generation
    private static final Random random = new Random();
    
    /**
     * Creates a new character with default average stats (all 10).
     */
    public CharacterStats() {
        baseStats = new int[Stat.values().length];
        tempModifiers = new int[Stat.values().length];
        
        // Default to average human stats
        for (int i = 0; i < baseStats.length; i++) {
            baseStats[i] = 10;
        }
    }
    
    /**
     * Creates a character with specified base stats.
     */
    public CharacterStats(int str, int dex, int con, int intel, int wis, int cha, int lck) {
        baseStats = new int[] {str, dex, con, intel, wis, cha, lck};
        tempModifiers = new int[Stat.values().length];
    }
    
    /**
     * Generates random stats using 3d6 method (bell curve, 3-18 range).
     */
    public static CharacterStats generateRandom() {
        CharacterStats stats = new CharacterStats();
        for (int i = 0; i < stats.baseStats.length; i++) {
            stats.baseStats[i] = roll3d6();
        }
        return stats;
    }
    
    /**
     * Generates random stats with a class/profession bias.
     * @param primaryStat Primary stat gets +2
     * @param secondaryStat Secondary stat gets +1
     */
    public static CharacterStats generateForProfession(Stat primaryStat, Stat secondaryStat) {
        CharacterStats stats = generateRandom();
        stats.baseStats[primaryStat.ordinal()] = Math.min(20, stats.baseStats[primaryStat.ordinal()] + 2);
        stats.baseStats[secondaryStat.ordinal()] = Math.min(20, stats.baseStats[secondaryStat.ordinal()] + 1);
        return stats;
    }
    
    /**
     * Rolls 3d6 for stat generation (3-18 range, average 10.5).
     */
    private static int roll3d6() {
        return (random.nextInt(6) + 1) + (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
    }
    
    // ==================== BASE STAT ACCESSORS ====================
    
    /**
     * Gets the base value of a stat (without modifiers).
     */
    public int getBaseStat(Stat stat) {
        return baseStats[stat.ordinal()];
    }
    
    /**
     * Gets the total value of a stat (base + modifiers).
     */
    public int getStat(Stat stat) {
        return Math.max(1, baseStats[stat.ordinal()] + tempModifiers[stat.ordinal()]);
    }
    
    /**
     * Sets the base value of a stat.
     */
    public void setBaseStat(Stat stat, int value) {
        baseStats[stat.ordinal()] = Math.max(1, Math.min(20, value));
    }
    
    /**
     * Increases a base stat by 1 (costs 1 stat point).
     */
    public boolean increaseBaseStat(Stat stat) {
        if (statPoints <= 0) return false;
        if (baseStats[stat.ordinal()] >= 20) return false;
        
        baseStats[stat.ordinal()]++;
        statPoints--;
        return true;
    }
    
    // ==================== TEMPORARY MODIFIERS ====================
    
    /**
     * Adds a temporary modifier to a stat.
     */
    public void addModifier(Stat stat, int amount) {
        tempModifiers[stat.ordinal()] += amount;
    }
    
    /**
     * Removes a temporary modifier from a stat.
     */
    public void removeModifier(Stat stat, int amount) {
        tempModifiers[stat.ordinal()] -= amount;
    }
    
    /**
     * Clears all temporary modifiers.
     */
    public void clearModifiers() {
        for (int i = 0; i < tempModifiers.length; i++) {
            tempModifiers[i] = 0;
        }
    }
    
    /**
     * Gets the current modifier value for a stat.
     */
    public int getModifier(Stat stat) {
        return tempModifiers[stat.ordinal()];
    }
    
    // ==================== DERIVED STATS ====================
    
    /**
     * Calculates max health from CON and STR.
     */
    public int getMaxHealth() {
        return getStat(Stat.CON) * 10 + getStat(Stat.STR) * 2;
    }
    
    /**
     * Calculates max stamina from CON and DEX.
     */
    public int getMaxStamina() {
        return getStat(Stat.CON) * 5 + getStat(Stat.DEX) * 3;
    }
    
    /**
     * Calculates melee damage bonus.
     */
    public int getMeleeDamage() {
        return getStat(Stat.STR) * 2 + getStat(Stat.DEX) / 2;
    }
    
    /**
     * Calculates ranged damage bonus.
     */
    public int getRangedDamage() {
        return getStat(Stat.DEX) * 2 + getStat(Stat.STR) / 2;
    }
    
    /**
     * Calculates magic power.
     */
    public int getMagicPower() {
        return getStat(Stat.INT) * 3 + getStat(Stat.WIS);
    }
    
    /**
     * Calculates dodge chance (percentage, 0-100).
     */
    public int getDodgeChance() {
        return Math.min(75, getStat(Stat.DEX) * 2 + getStat(Stat.LCK));
    }
    
    /**
     * Calculates critical hit chance (percentage, 0-100).
     */
    public int getCritChance() {
        return Math.min(50, getStat(Stat.LCK) * 2 + getStat(Stat.DEX));
    }
    
    /**
     * Calculates trade price modifier (percentage bonus/discount).
     * Positive = better prices when buying, worse when selling
     */
    public int getTradeModifier() {
        return (getStat(Stat.CHA) - 10) * 2;  // -20% to +20% from CHA
    }
    
    /**
     * Calculates maximum party size.
     */
    public int getMaxPartySize() {
        return 1 + getStat(Stat.CHA) / 3;  // 1 to 7 party members
    }
    
    /**
     * Calculates carry capacity in weight units.
     */
    public int getCarryCapacity() {
        return getStat(Stat.STR) * 10 + getStat(Stat.CON) * 2;
    }
    
    /**
     * Calculates magic resistance (percentage, 0-75).
     */
    public int getMagicResistance() {
        return Math.min(75, getStat(Stat.WIS) * 2 + getStat(Stat.CON));
    }
    
    /**
     * Calculates perception/awareness (affects finding hidden things).
     */
    public int getPerception() {
        return getStat(Stat.WIS) * 2 + getStat(Stat.INT);
    }
    
    // ==================== LEVELING ====================
    
    /**
     * Gets current level.
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Gets current experience.
     */
    public int getExperience() {
        return experience;
    }
    
    /**
     * Gets experience needed for next level.
     */
    public int getExperienceToNextLevel() {
        return level * 100 + (level - 1) * 50;  // 100, 150, 200, 250...
    }
    
    /**
     * Gets unspent stat points.
     */
    public int getStatPoints() {
        return statPoints;
    }
    
    /**
     * Adds experience and handles leveling up.
     * @return Number of levels gained
     */
    public int addExperience(int amount) {
        if (amount <= 0) return 0;
        
        experience += amount;
        int levelsGained = 0;
        
        while (experience >= getExperienceToNextLevel()) {
            experience -= getExperienceToNextLevel();
            level++;
            statPoints += 2;  // 2 stat points per level
            levelsGained++;
        }
        
        return levelsGained;
    }
    
    /**
     * Sets level directly (for testing/cheats).
     */
    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }
    
    // ==================== STAT MODIFIER CALCULATION ====================
    
    /**
     * Gets the D&D-style modifier for a stat (-4 to +5).
     * Modifier = (stat - 10) / 2
     */
    public int getStatModifier(Stat stat) {
        return (getStat(stat) - 10) / 2;
    }
    
    /**
     * Gets display string for stat modifier (e.g., "+2" or "-1").
     */
    public String getStatModifierDisplay(Stat stat) {
        int mod = getStatModifier(stat);
        return (mod >= 0 ? "+" : "") + mod;
    }
    
    // ==================== UTILITY ====================
    
    /**
     * Creates a copy of these stats.
     */
    public CharacterStats copy() {
        CharacterStats copy = new CharacterStats();
        System.arraycopy(baseStats, 0, copy.baseStats, 0, baseStats.length);
        System.arraycopy(tempModifiers, 0, copy.tempModifiers, 0, tempModifiers.length);
        copy.level = level;
        copy.experience = experience;
        copy.statPoints = statPoints;
        return copy;
    }
    
    /**
     * Returns a formatted string of all stats.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Level ").append(level).append(" (").append(experience).append("/")
          .append(getExperienceToNextLevel()).append(" XP)\n");
        
        for (Stat stat : Stat.values()) {
            sb.append(stat.name()).append(": ").append(getStat(stat));
            int mod = getModifier(stat);
            if (mod != 0) {
                sb.append(" (").append(mod >= 0 ? "+" : "").append(mod).append(")");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    // ==================== QUICK ACCESS CONVENIENCE METHODS ====================
    
    public int getStrength() { return getStat(Stat.STR); }
    public int getDexterity() { return getStat(Stat.DEX); }
    public int getConstitution() { return getStat(Stat.CON); }
    public int getIntelligence() { return getStat(Stat.INT); }
    public int getWisdom() { return getStat(Stat.WIS); }
    public int getCharisma() { return getStat(Stat.CHA); }
    public int getLuck() { return getStat(Stat.LCK); }
}
