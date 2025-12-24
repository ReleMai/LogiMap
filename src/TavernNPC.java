import java.util.Random;

/**
 * Tavern NPC - A hireable character found in tavern locations.
 * 
 * Tavern NPCs can be hired to join the player's party.
 * They have:
 * - Name (generated based on gender and profession)
 * - Gender (affects name and sprite)
 * - Stats (CharacterStats with profession bonuses)
 * - Profession (with tier, perks, traits)
 * - Hiring fee and daily wage
 * - Morale (affects performance)
 * - Equipment (can be given items)
 */
public class TavernNPC {
    
    private static final Random random = new Random();
    
    // Identity
    private String name;
    private MedievalNameGenerator.Gender gender;
    private int age;
    
    // Stats and profession
    private CharacterStats stats;
    private NPCProfession profession;
    
    // Employment status
    private boolean hired = false;
    private int daysEmployed = 0;
    private int morale = 100;  // 0-100, affects performance
    
    // Current state
    private int currentHealth;
    private int currentStamina;
    
    // Description for flavor text
    private String backstory;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Creates a new random tavern NPC.
     */
    public TavernNPC() {
        this.gender = MedievalNameGenerator.randomGender();
        this.profession = NPCProfession.createRandom(randomProfessionType());
        this.name = MedievalNameGenerator.generateNameForProfession(gender, profession.getType().getDisplayName());
        this.age = 18 + random.nextInt(42);  // 18-59 years old
        
        // Generate stats based on profession
        this.stats = CharacterStats.generateForProfession(
            profession.getType().getPrimaryStat(),
            profession.getType().getSecondaryStat()
        );
        
        // Apply trait modifiers to stats
        for (NPCProfession.Trait trait : profession.getTraits()) {
            for (CharacterStats.Stat stat : CharacterStats.Stat.values()) {
                int mod = trait.getStatModifier(stat);
                if (mod != 0) {
                    stats.addModifier(stat, mod);
                }
            }
        }
        
        // Set current HP/Stamina to max
        this.currentHealth = stats.getMaxHealth();
        this.currentStamina = stats.getMaxStamina();
        
        // Generate backstory
        this.backstory = generateBackstory();
    }
    
    /**
     * Creates a tavern NPC with specific profession.
     */
    public TavernNPC(NPCProfession.ProfessionType profType) {
        this.gender = MedievalNameGenerator.randomGender();
        this.profession = NPCProfession.createRandom(profType);
        this.name = MedievalNameGenerator.generateNameForProfession(gender, profession.getType().getDisplayName());
        this.age = 18 + random.nextInt(42);
        
        this.stats = CharacterStats.generateForProfession(
            profession.getType().getPrimaryStat(),
            profession.getType().getSecondaryStat()
        );
        
        for (NPCProfession.Trait trait : profession.getTraits()) {
            for (CharacterStats.Stat stat : CharacterStats.Stat.values()) {
                stats.addModifier(stat, trait.getStatModifier(stat));
            }
        }
        
        this.currentHealth = stats.getMaxHealth();
        this.currentStamina = stats.getMaxStamina();
        this.backstory = generateBackstory();
    }
    
    /**
     * Creates a tavern NPC with specific gender and profession.
     */
    public TavernNPC(MedievalNameGenerator.Gender gender, NPCProfession.ProfessionType profType) {
        this.gender = gender;
        this.profession = NPCProfession.createRandom(profType);
        this.name = MedievalNameGenerator.generateNameForProfession(gender, profession.getType().getDisplayName());
        this.age = 18 + random.nextInt(42);
        
        this.stats = CharacterStats.generateForProfession(
            profession.getType().getPrimaryStat(),
            profession.getType().getSecondaryStat()
        );
        
        for (NPCProfession.Trait trait : profession.getTraits()) {
            for (CharacterStats.Stat stat : CharacterStats.Stat.values()) {
                stats.addModifier(stat, trait.getStatModifier(stat));
            }
        }
        
        this.currentHealth = stats.getMaxHealth();
        this.currentStamina = stats.getMaxStamina();
        this.backstory = generateBackstory();
    }
    
    // ==================== HIRING ====================
    
    /**
     * Gets the one-time hiring fee.
     */
    public int getHiringFee() {
        return profession.getHiringFee();
    }
    
    /**
     * Gets the daily wage cost.
     */
    public int getDailyWage() {
        return profession.getDailyWage();
    }
    
    /**
     * Hires this NPC (marks as employed).
     */
    public void hire() {
        this.hired = true;
        this.daysEmployed = 0;
        this.morale = 100;
    }
    
    /**
     * Fires/dismisses this NPC.
     */
    public void dismiss() {
        this.hired = false;
    }
    
    /**
     * Checks if NPC is currently hired.
     */
    public boolean isHired() {
        return hired;
    }
    
    /**
     * Processes a day passing (costs wage, updates morale).
     * @return true if NPC stays, false if they quit
     */
    public boolean processDay(boolean paidWage) {
        if (!hired) return true;
        
        daysEmployed++;
        
        if (paidWage) {
            // Happy to be paid
            adjustMorale(2);
        } else {
            // Not happy about unpaid work
            adjustMorale(-15);
            
            // Might quit if morale too low
            if (morale < 20 && random.nextDouble() < 0.3) {
                dismiss();
                return false;
            }
        }
        
        return true;
    }
    
    // ==================== MORALE ====================
    
    /**
     * Adjusts morale by an amount (-100 to +100).
     */
    public void adjustMorale(int amount) {
        morale = Math.max(0, Math.min(100, morale + amount));
    }
    
    /**
     * Gets current morale (0-100).
     */
    public int getMorale() {
        return morale;
    }
    
    /**
     * Gets morale descriptor.
     */
    public String getMoraleStatus() {
        if (morale >= 80) return "Excellent";
        if (morale >= 60) return "Good";
        if (morale >= 40) return "Fair";
        if (morale >= 20) return "Poor";
        return "Terrible";
    }
    
    /**
     * Gets morale performance multiplier (0.5 to 1.2).
     */
    public double getMoraleMultiplier() {
        return 0.5 + (morale / 100.0) * 0.7;
    }
    
    // ==================== COMBAT / HEALTH ====================
    
    /**
     * Takes damage.
     * @return true if NPC is still alive
     */
    public boolean takeDamage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        return currentHealth > 0;
    }
    
    /**
     * Heals HP.
     */
    public void heal(int amount) {
        currentHealth = Math.min(stats.getMaxHealth(), currentHealth + amount);
    }
    
    /**
     * Fully heals HP and stamina.
     */
    public void fullHeal() {
        currentHealth = stats.getMaxHealth();
        currentStamina = stats.getMaxStamina();
    }
    
    /**
     * Uses stamina.
     * @return true if had enough stamina
     */
    public boolean useStamina(int amount) {
        if (currentStamina < amount) return false;
        currentStamina -= amount;
        return true;
    }
    
    /**
     * Restores stamina.
     */
    public void restoreStamina(int amount) {
        currentStamina = Math.min(stats.getMaxStamina(), currentStamina + amount);
    }
    
    public int getCurrentHealth() { return currentHealth; }
    public int getCurrentStamina() { return currentStamina; }
    public boolean isAlive() { return currentHealth > 0; }
    
    // ==================== BACKSTORY ====================
    
    /**
     * Generates a random backstory based on profession and traits.
     */
    private String generateBackstory() {
        String[] origins = {
            "hails from a small village in the north",
            "grew up in a bustling city",
            "was raised on a farm",
            "comes from a family of wanderers",
            "escaped from a war-torn land",
            "left home to seek fortune",
            "was orphaned at a young age",
            "trained under a renowned master",
            "served in a lord's household",
            "traveled with a merchant caravan"
        };
        
        String[] motivations = {
            "seeks adventure and gold",
            "is looking for honest work",
            "hopes to make a name for themselves",
            "needs coin to support their family",
            "wants to travel and see the world",
            "is running from a troubled past",
            "dreams of becoming famous",
            "seeks to hone their skills",
            "is looking for a fresh start",
            "wants revenge on those who wronged them"
        };
        
        String pronoun = (gender == MedievalNameGenerator.Gender.MALE) ? "He" : "She";
        String origin = origins[random.nextInt(origins.length)];
        String motivation = motivations[random.nextInt(motivations.length)];
        
        return pronoun + " " + origin + " and " + motivation + ".";
    }
    
    // ==================== UTILITY ====================
    
    /**
     * Gets a random profession type weighted by tavern encounter likelihood.
     */
    private static NPCProfession.ProfessionType randomProfessionType() {
        NPCProfession.ProfessionType[] types = NPCProfession.ProfessionType.values();
        return types[random.nextInt(types.length)];
    }
    
    /**
     * Creates a batch of random tavern NPCs.
     */
    public static TavernNPC[] generateTavernNPCs(int count) {
        TavernNPC[] npcs = new TavernNPC[count];
        for (int i = 0; i < count; i++) {
            npcs[i] = new TavernNPC();
        }
        return npcs;
    }
    
    /**
     * Creates a balanced group of tavern NPCs from different categories.
     */
    public static TavernNPC[] generateBalancedGroup(int count) {
        TavernNPC[] npcs = new TavernNPC[count];
        NPCProfession.Category[] categories = NPCProfession.Category.values();
        
        for (int i = 0; i < count; i++) {
            NPCProfession.Category cat = categories[i % categories.length];
            NPCProfession prof = NPCProfession.createRandomFromCategory(cat);
            npcs[i] = new TavernNPC(prof.getType());
        }
        return npcs;
    }
    
    // ==================== GETTERS ====================
    
    public String getName() { return name; }
    public MedievalNameGenerator.Gender getGender() { return gender; }
    public int getAge() { return age; }
    public CharacterStats getStats() { return stats; }
    public NPCProfession getProfession() { return profession; }
    public String getBackstory() { return backstory; }
    public int getDaysEmployed() { return daysEmployed; }
    
    /**
     * Gets display name with profession.
     */
    public String getDisplayName() {
        return name + " (" + profession.getType().getDisplayName() + ")";
    }
    
    /**
     * Gets short summary for tavern display.
     */
    public String getTavernSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(profession.getTitle()).append("\n");
        sb.append("Level ").append(stats.getLevel()).append(" | ");
        sb.append("Age ").append(age).append("\n");
        sb.append("Hire: ").append(getHiringFee()).append("g | ");
        sb.append("Wage: ").append(getDailyWage()).append("g/day");
        return sb.toString();
    }
    
    /**
     * Gets full character sheet info.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════\n");
        sb.append("  ").append(name).append("\n");
        sb.append("═══════════════════════════════════\n");
        sb.append(profession.getTitle()).append(" | Age ").append(age).append("\n");
        sb.append(backstory).append("\n\n");
        
        sb.append("--- STATS ---\n");
        sb.append("STR: ").append(stats.getStrength());
        sb.append(" | DEX: ").append(stats.getDexterity());
        sb.append(" | CON: ").append(stats.getConstitution()).append("\n");
        sb.append("INT: ").append(stats.getIntelligence());
        sb.append(" | WIS: ").append(stats.getWisdom());
        sb.append(" | CHA: ").append(stats.getCharisma()).append("\n");
        sb.append("LCK: ").append(stats.getLuck()).append("\n\n");
        
        sb.append("HP: ").append(currentHealth).append("/").append(stats.getMaxHealth());
        sb.append(" | Stamina: ").append(currentStamina).append("/").append(stats.getMaxStamina()).append("\n\n");
        
        sb.append("--- EMPLOYMENT ---\n");
        sb.append("Hiring Fee: ").append(getHiringFee()).append(" gold\n");
        sb.append("Daily Wage: ").append(getDailyWage()).append(" gold\n");
        if (hired) {
            sb.append("Days Employed: ").append(daysEmployed).append("\n");
            sb.append("Morale: ").append(morale).append("% (").append(getMoraleStatus()).append(")\n");
        } else {
            sb.append("Status: Available for hire\n");
        }
        sb.append("\n");
        
        sb.append(profession.toString());
        
        return sb.toString();
    }
}
