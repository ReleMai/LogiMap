import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * NPC Profession System for LogiMap.
 * 
 * Professions define an NPC's role, skills, and abilities.
 * Each profession has:
 * - Tier (1-5): Skill level / expertise
 * - Primary & secondary stats: Affects derived values
 * - Perks: Special abilities unlocked at higher tiers
 * - Base wage: Cost to hire per day
 */
public class NPCProfession {
    
    private static final Random random = new Random();
    
    // ==================== PROFESSION TYPES ====================
    
    public enum ProfessionType {
        // Combat professions
        MERCENARY("Mercenary", "A skilled fighter for hire", 
                  CharacterStats.Stat.STR, CharacterStats.Stat.CON, Category.COMBAT, 15),
        ARCHER("Archer", "Expert with bow and crossbow",
               CharacterStats.Stat.DEX, CharacterStats.Stat.WIS, Category.COMBAT, 12),
        KNIGHT("Knight", "Armored warrior of noble training",
               CharacterStats.Stat.STR, CharacterStats.Stat.CHA, Category.COMBAT, 25),
        ROGUE("Rogue", "Stealthy fighter and lockpick",
              CharacterStats.Stat.DEX, CharacterStats.Stat.LCK, Category.COMBAT, 10),
        BERSERKER("Berserker", "Fearless warrior of great fury",
                  CharacterStats.Stat.STR, CharacterStats.Stat.CON, Category.COMBAT, 18),
        
        // Crafting professions
        BLACKSMITH("Blacksmith", "Forges weapons and armor",
                   CharacterStats.Stat.STR, CharacterStats.Stat.INT, Category.CRAFT, 12),
        CARPENTER("Carpenter", "Builds structures and furniture",
                  CharacterStats.Stat.DEX, CharacterStats.Stat.INT, Category.CRAFT, 8),
        ALCHEMIST("Alchemist", "Brews potions and elixirs",
                  CharacterStats.Stat.INT, CharacterStats.Stat.WIS, Category.CRAFT, 15),
        TAILOR("Tailor", "Crafts clothing and light armor",
               CharacterStats.Stat.DEX, CharacterStats.Stat.CHA, Category.CRAFT, 8),
        COOK("Cook", "Prepares food with special effects",
             CharacterStats.Stat.WIS, CharacterStats.Stat.INT, Category.CRAFT, 6),
        
        // Gathering professions
        MINER("Miner", "Expert at extracting ores",
              CharacterStats.Stat.STR, CharacterStats.Stat.CON, Category.GATHER, 7),
        WOODCUTTER("Woodcutter", "Skilled at felling trees",
                   CharacterStats.Stat.STR, CharacterStats.Stat.DEX, Category.GATHER, 6),
        HUNTER("Hunter", "Tracks and hunts wild game",
               CharacterStats.Stat.DEX, CharacterStats.Stat.WIS, Category.GATHER, 8),
        FARMER("Farmer", "Grows crops and tends livestock",
               CharacterStats.Stat.CON, CharacterStats.Stat.WIS, Category.GATHER, 5),
        FISHERMAN("Fisherman", "Catches fish and sea creatures",
                  CharacterStats.Stat.DEX, CharacterStats.Stat.LCK, Category.GATHER, 5),
        
        // Support professions
        HEALER("Healer", "Mends wounds and cures ailments",
               CharacterStats.Stat.WIS, CharacterStats.Stat.INT, Category.SUPPORT, 14),
        BARD("Bard", "Inspires with music and tales",
             CharacterStats.Stat.CHA, CharacterStats.Stat.DEX, Category.SUPPORT, 10),
        SCOUT("Scout", "Expert at reconnaissance",
              CharacterStats.Stat.DEX, CharacterStats.Stat.WIS, Category.SUPPORT, 9),
        PORTER("Porter", "Carries heavy loads",
               CharacterStats.Stat.STR, CharacterStats.Stat.CON, Category.SUPPORT, 4),
        SCHOLAR("Scholar", "Researches and teaches",
                CharacterStats.Stat.INT, CharacterStats.Stat.WIS, Category.SUPPORT, 12),
        
        // Trade professions
        MERCHANT("Merchant", "Skilled at buying and selling",
                 CharacterStats.Stat.CHA, CharacterStats.Stat.INT, Category.TRADE, 10),
        APPRAISER("Appraiser", "Values items and detects fakes",
                  CharacterStats.Stat.INT, CharacterStats.Stat.WIS, Category.TRADE, 12),
        CARAVAN_GUARD("Caravan Guard", "Protects trade routes",
                      CharacterStats.Stat.STR, CharacterStats.Stat.DEX, Category.TRADE, 11);
        
        private final String displayName;
        private final String description;
        private final CharacterStats.Stat primaryStat;
        private final CharacterStats.Stat secondaryStat;
        private final Category category;
        private final int baseWage;  // Gold per day
        
        ProfessionType(String displayName, String description, 
                       CharacterStats.Stat primary, CharacterStats.Stat secondary,
                       Category category, int baseWage) {
            this.displayName = displayName;
            this.description = description;
            this.primaryStat = primary;
            this.secondaryStat = secondary;
            this.category = category;
            this.baseWage = baseWage;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public CharacterStats.Stat getPrimaryStat() { return primaryStat; }
        public CharacterStats.Stat getSecondaryStat() { return secondaryStat; }
        public Category getCategory() { return category; }
        public int getBaseWage() { return baseWage; }
    }
    
    /**
     * Profession categories for filtering.
     */
    public enum Category {
        COMBAT("Combat", "#cc3333"),      // Red
        CRAFT("Crafting", "#33cc33"),     // Green
        GATHER("Gathering", "#cccc33"),   // Yellow
        SUPPORT("Support", "#3333cc"),    // Blue
        TRADE("Trade", "#cc9933");        // Gold
        
        private final String displayName;
        private final String color;
        
        Category(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    /**
     * Profession tiers (skill levels).
     */
    public enum Tier {
        NOVICE(1, "Novice", 1.0, 0),
        APPRENTICE(2, "Apprentice", 1.3, 1),
        JOURNEYMAN(3, "Journeyman", 1.6, 2),
        EXPERT(4, "Expert", 2.0, 3),
        MASTER(5, "Master", 2.5, 4);
        
        private final int level;
        private final String displayName;
        private final double wageMultiplier;
        private final int bonusPerks;
        
        Tier(int level, String displayName, double wageMultiplier, int bonusPerks) {
            this.level = level;
            this.displayName = displayName;
            this.wageMultiplier = wageMultiplier;
            this.bonusPerks = bonusPerks;
        }
        
        public int getLevel() { return level; }
        public String getDisplayName() { return displayName; }
        public double getWageMultiplier() { return wageMultiplier; }
        public int getBonusPerks() { return bonusPerks; }
        
        public static Tier fromLevel(int level) {
            for (Tier t : values()) {
                if (t.level == level) return t;
            }
            return NOVICE;
        }
    }
    
    // ==================== PERKS ====================
    
    /**
     * Profession perks - special abilities.
     */
    public enum Perk {
        // Combat perks
        SHIELD_BASH("Shield Bash", "Can stun enemies with shield", ProfessionType.MERCENARY),
        DOUBLE_STRIKE("Double Strike", "Chance to attack twice", ProfessionType.MERCENARY),
        PRECISION_SHOT("Precision Shot", "+20% critical chance", ProfessionType.ARCHER),
        RAPID_FIRE("Rapid Fire", "Attack speed increased", ProfessionType.ARCHER),
        HONOR_GUARD("Honor Guard", "+50% protection to party leader", ProfessionType.KNIGHT),
        RALLY("Rally", "Boosts party morale in combat", ProfessionType.KNIGHT),
        BACKSTAB("Backstab", "Triple damage from stealth", ProfessionType.ROGUE),
        PICKPOCKET("Pickpocket", "Can steal from NPCs", ProfessionType.ROGUE),
        RAGE("Rage", "Double damage when low health", ProfessionType.BERSERKER),
        
        // Craft perks
        MASTERWORK("Masterwork", "10% chance for superior quality", ProfessionType.BLACKSMITH),
        REPAIR("Repair", "Can repair equipment in field", ProfessionType.BLACKSMITH),
        QUICK_BUILD("Quick Build", "Structures built 25% faster", ProfessionType.CARPENTER),
        BREW_MASTERY("Brew Mastery", "Potions 50% more effective", ProfessionType.ALCHEMIST),
        FINE_THREAD("Fine Thread", "Crafted clothes give bonus stats", ProfessionType.TAILOR),
        GOURMET("Gourmet", "Food gives +50% buff duration", ProfessionType.COOK),
        
        // Gather perks
        MOTHERLODE("Motherlode", "15% chance for double ore", ProfessionType.MINER),
        EFFICIENT_AXE("Efficient Axe", "20% less stamina for cutting", ProfessionType.WOODCUTTER),
        TRACKER("Tracker", "Can track animals on map", ProfessionType.HUNTER),
        GREEN_THUMB("Green Thumb", "Crops grow 25% faster", ProfessionType.FARMER),
        LUCKY_CATCH("Lucky Catch", "Chance for rare fish", ProfessionType.FISHERMAN),
        
        // Support perks
        FIRST_AID("First Aid", "Heal in combat", ProfessionType.HEALER),
        CURE_DISEASE("Cure Disease", "Can cure status effects", ProfessionType.HEALER),
        INSPIRE("Inspire", "+10% to all party stats", ProfessionType.BARD),
        DISTRACT("Distract", "Enemies less likely to target party", ProfessionType.BARD),
        PATHFINDER("Pathfinder", "Party moves 15% faster", ProfessionType.SCOUT),
        PACK_MULE("Pack Mule", "+50 carry capacity", ProfessionType.PORTER),
        LORE_MASTER("Lore Master", "Identifies unknown items", ProfessionType.SCHOLAR),
        
        // Trade perks
        HAGGLE("Haggle", "+10% better prices", ProfessionType.MERCHANT),
        CONNECTIONS("Connections", "Access to rare goods", ProfessionType.MERCHANT),
        TRUE_VALUE("True Value", "See item quality before buying", ProfessionType.APPRAISER),
        VIGILANT("Vigilant", "Warns of ambushes", ProfessionType.CARAVAN_GUARD);
        
        private final String displayName;
        private final String description;
        private final ProfessionType profession;
        
        Perk(String displayName, String description, ProfessionType profession) {
            this.displayName = displayName;
            this.description = description;
            this.profession = profession;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public ProfessionType getProfession() { return profession; }
        
        /**
         * Gets all perks for a profession.
         */
        public static List<Perk> getPerksForProfession(ProfessionType profession) {
            List<Perk> perks = new ArrayList<>();
            for (Perk p : values()) {
                if (p.profession == profession) {
                    perks.add(p);
                }
            }
            return perks;
        }
    }
    
    // ==================== TRAITS (Positive and Negative) ====================
    
    /**
     * Character traits that modify behavior/stats.
     */
    public enum Trait {
        // Positive traits
        BRAVE("Brave", "Never flees from combat", true, 0, 0, 0, 2, 0, 0, 0),
        STRONG("Strong", "+2 Strength", true, 2, 0, 0, 0, 0, 0, 0),
        NIMBLE("Nimble", "+2 Dexterity", true, 0, 2, 0, 0, 0, 0, 0),
        HARDY("Hardy", "+2 Constitution", true, 0, 0, 2, 0, 0, 0, 0),
        CLEVER("Clever", "+2 Intelligence", true, 0, 0, 0, 2, 0, 0, 0),
        PERCEPTIVE("Perceptive", "+2 Wisdom", true, 0, 0, 0, 0, 2, 0, 0),
        CHARISMATIC("Charismatic", "+2 Charisma", true, 0, 0, 0, 0, 0, 2, 0),
        LUCKY("Lucky", "+2 Luck", true, 0, 0, 0, 0, 0, 0, 2),
        DILIGENT("Diligent", "Works 20% faster", true, 0, 0, 0, 0, 0, 0, 0),
        LOYAL("Loyal", "Never abandons party", true, 0, 0, 0, 0, 0, 0, 0),
        FRUGAL("Frugal", "Costs 15% less to employ", true, 0, 0, 0, 0, 0, 0, 0),
        ENTHUSIASTIC("Enthusiastic", "+10% experience gain", true, 0, 0, 0, 0, 0, 0, 0),
        
        // Negative traits
        COWARDLY("Cowardly", "May flee from combat", false, 0, 0, 0, -2, 0, 0, 0),
        WEAK("Weak", "-2 Strength", false, -2, 0, 0, 0, 0, 0, 0),
        CLUMSY("Clumsy", "-2 Dexterity", false, 0, -2, 0, 0, 0, 0, 0),
        SICKLY("Sickly", "-2 Constitution", false, 0, 0, -2, 0, 0, 0, 0),
        SLOW_WITTED("Slow-Witted", "-2 Intelligence", false, 0, 0, 0, -2, 0, 0, 0),
        OBLIVIOUS("Oblivious", "-2 Wisdom", false, 0, 0, 0, 0, -2, 0, 0),
        ABRASIVE("Abrasive", "-2 Charisma", false, 0, 0, 0, 0, 0, -2, 0),
        UNLUCKY("Unlucky", "-2 Luck", false, 0, 0, 0, 0, 0, 0, -2),
        LAZY("Lazy", "Works 20% slower", false, 0, 0, 0, 0, 0, 0, 0),
        GREEDY("Greedy", "Demands 20% more pay", false, 0, 0, 0, 0, 0, 0, 0),
        DRUNKARD("Drunkard", "May miss work", false, 0, 0, 0, 0, 0, 0, 0),
        PESSIMISTIC("Pessimistic", "-10% experience gain", false, 0, 0, 0, 0, 0, 0, 0);
        
        private final String displayName;
        private final String description;
        private final boolean positive;
        private final int[] statModifiers;  // STR, DEX, CON, INT, WIS, CHA, LCK
        
        Trait(String displayName, String description, boolean positive,
              int str, int dex, int con, int intel, int wis, int cha, int lck) {
            this.displayName = displayName;
            this.description = description;
            this.positive = positive;
            this.statModifiers = new int[] {str, dex, con, intel, wis, cha, lck};
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public boolean isPositive() { return positive; }
        public int getStatModifier(CharacterStats.Stat stat) {
            return statModifiers[stat.ordinal()];
        }
        
        /**
         * Gets a random positive trait.
         */
        public static Trait getRandomPositive() {
            List<Trait> positives = new ArrayList<>();
            for (Trait t : values()) {
                if (t.positive) positives.add(t);
            }
            return positives.get(random.nextInt(positives.size()));
        }
        
        /**
         * Gets a random negative trait.
         */
        public static Trait getRandomNegative() {
            List<Trait> negatives = new ArrayList<>();
            for (Trait t : values()) {
                if (!t.positive) negatives.add(t);
            }
            return negatives.get(random.nextInt(negatives.size()));
        }
    }
    
    // ==================== INSTANCE FIELDS ====================
    
    private final ProfessionType type;
    private Tier tier;
    private List<Perk> unlockedPerks;
    private List<Trait> traits;
    private int experience;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Creates a new profession instance.
     */
    public NPCProfession(ProfessionType type, Tier tier) {
        this.type = type;
        this.tier = tier;
        this.unlockedPerks = new ArrayList<>();
        this.traits = new ArrayList<>();
        this.experience = 0;
        
        // Unlock perks based on tier
        unlockPerksForTier();
    }
    
    /**
     * Creates a profession with random tier (weighted toward lower).
     */
    public static NPCProfession createRandom(ProfessionType type) {
        // Weighted random tier: 40% Novice, 30% Apprentice, 15% Journeyman, 10% Expert, 5% Master
        double roll = random.nextDouble();
        Tier tier;
        if (roll < 0.40) tier = Tier.NOVICE;
        else if (roll < 0.70) tier = Tier.APPRENTICE;
        else if (roll < 0.85) tier = Tier.JOURNEYMAN;
        else if (roll < 0.95) tier = Tier.EXPERT;
        else tier = Tier.MASTER;
        
        NPCProfession prof = new NPCProfession(type, tier);
        
        // Add 1-2 random traits
        int traitCount = 1 + random.nextInt(2);
        for (int i = 0; i < traitCount; i++) {
            // 60% chance positive, 40% chance negative
            Trait trait = random.nextDouble() < 0.6 
                ? Trait.getRandomPositive() 
                : Trait.getRandomNegative();
            if (!prof.traits.contains(trait)) {
                prof.traits.add(trait);
            }
        }
        
        return prof;
    }
    
    /**
     * Creates a random profession from a category.
     */
    public static NPCProfession createRandomFromCategory(Category category) {
        List<ProfessionType> matches = new ArrayList<>();
        for (ProfessionType p : ProfessionType.values()) {
            if (p.getCategory() == category) {
                matches.add(p);
            }
        }
        ProfessionType type = matches.get(random.nextInt(matches.size()));
        return createRandom(type);
    }
    
    // ==================== PERK MANAGEMENT ====================
    
    /**
     * Unlocks perks based on current tier.
     */
    private void unlockPerksForTier() {
        List<Perk> available = Perk.getPerksForProfession(type);
        int perksToUnlock = tier.getBonusPerks();
        
        for (int i = 0; i < Math.min(perksToUnlock, available.size()); i++) {
            if (!unlockedPerks.contains(available.get(i))) {
                unlockedPerks.add(available.get(i));
            }
        }
    }
    
    /**
     * Checks if a perk is unlocked.
     */
    public boolean hasPerk(Perk perk) {
        return unlockedPerks.contains(perk);
    }
    
    /**
     * Gets all unlocked perks.
     */
    public List<Perk> getUnlockedPerks() {
        return new ArrayList<>(unlockedPerks);
    }
    
    // ==================== TRAIT MANAGEMENT ====================
    
    /**
     * Adds a trait.
     */
    public void addTrait(Trait trait) {
        if (!traits.contains(trait)) {
            traits.add(trait);
        }
    }
    
    /**
     * Removes a trait.
     */
    public void removeTrait(Trait trait) {
        traits.remove(trait);
    }
    
    /**
     * Gets all traits.
     */
    public List<Trait> getTraits() {
        return new ArrayList<>(traits);
    }
    
    /**
     * Checks if has a specific trait.
     */
    public boolean hasTrait(Trait trait) {
        return traits.contains(trait);
    }
    
    // ==================== WAGE CALCULATION ====================
    
    /**
     * Calculates daily wage based on tier and traits.
     */
    public int getDailyWage() {
        // Global discount to make hiring more affordable
        double wage = type.getBaseWage() * tier.getWageMultiplier() * 0.65;
        
        // Apply trait modifiers
        for (Trait t : traits) {
            if (t == Trait.FRUGAL) wage *= 0.85;
            if (t == Trait.GREEDY) wage *= 1.20;
        }
        
        return (int) Math.ceil(wage);
    }
    
    /**
     * Calculates hiring fee (typically 3-5 days wage upfront).
     */
    public int getHiringFee() {
        // Pay fewer days up-front than before to reduce barrier to entry
        return (int) Math.ceil(getDailyWage() * (2 + tier.getLevel()));
    }
    
    // ==================== EXPERIENCE & LEVELING ====================
    
    /**
     * Adds experience and checks for tier-up.
     * @return true if tier increased
     */
    public boolean addExperience(int amount) {
        experience += amount;
        int expNeeded = getExperienceToNextTier();
        
        if (expNeeded > 0 && experience >= expNeeded && tier.getLevel() < 5) {
            experience -= expNeeded;
            tier = Tier.fromLevel(tier.getLevel() + 1);
            unlockPerksForTier();
            return true;
        }
        return false;
    }
    
    /**
     * Gets experience needed for next tier.
     */
    public int getExperienceToNextTier() {
        if (tier.getLevel() >= 5) return -1;  // Already max
        return tier.getLevel() * 500;  // 500, 1000, 1500, 2000
    }
    
    // ==================== GETTERS ====================
    
    public ProfessionType getType() { return type; }
    public Tier getTier() { return tier; }
    public int getExperience() { return experience; }
    public Category getCategory() { return type.getCategory(); }
    
    /**
     * Gets a formatted title like "Journeyman Blacksmith".
     */
    public String getTitle() {
        return tier.getDisplayName() + " " + type.getDisplayName();
    }
    
    /**
     * Gets short display like "Blacksmith (Tier 3)".
     */
    public String getShortDisplay() {
        return type.getDisplayName() + " (Tier " + tier.getLevel() + ")";
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle()).append("\n");
        sb.append("Category: ").append(type.getCategory().getDisplayName()).append("\n");
        sb.append("Daily Wage: ").append(getDailyWage()).append(" gold\n");
        sb.append("Hiring Fee: ").append(getHiringFee()).append(" gold\n");
        
        if (!unlockedPerks.isEmpty()) {
            sb.append("Perks: ");
            for (int i = 0; i < unlockedPerks.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(unlockedPerks.get(i).getDisplayName());
            }
            sb.append("\n");
        }
        
        if (!traits.isEmpty()) {
            sb.append("Traits: ");
            for (int i = 0; i < traits.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(traits.get(i).getDisplayName());
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
