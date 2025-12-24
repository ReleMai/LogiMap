import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Combat system for handling battles between the player's party and enemies.
 * Uses a simple turn-based auto-resolve system with optional tactical view.
 */
public class Combat {
    
    // Combat participants
    private Party playerParty;
    private List<Enemy> enemies;
    private PlayerSprite player;
    
    // Combat state
    private boolean inCombat = false;
    private boolean playerTurn = true;
    private int round = 1;
    private List<String> combatLog;
    
    // Combat result
    public enum Result {
        VICTORY,
        DEFEAT,
        FLED,
        IN_PROGRESS
    }
    private Result result = Result.IN_PROGRESS;
    
    // Random for combat calculations
    private Random random = new Random();
    
    // Combat settings
    private static final double MORALE_ATTACK_BONUS = 0.003; // 0.3% per morale point
    private static final double MORALE_DEFENSE_BONUS = 0.002; // 0.2% per morale point
    private static final double FLEE_BASE_CHANCE = 0.4;
    private static final double SPEED_FLEE_BONUS = 0.02; // 2% per speed point
    
    /**
     * Creates a new combat encounter.
     */
    public Combat(Party playerParty, PlayerSprite player, List<Enemy> enemies) {
        this.playerParty = playerParty;
        this.player = player;
        this.enemies = new ArrayList<>(enemies);
        this.combatLog = new ArrayList<>();
        this.inCombat = true;
        
        addLog("Combat begins! " + enemies.size() + " enemies attack!");
    }
    
    /**
     * Auto-resolves the entire combat.
     */
    public Result autoResolve() {
        while (result == Result.IN_PROGRESS) {
            executeRound();
        }
        return result;
    }
    
    /**
     * Executes a single round of combat.
     */
    public void executeRound() {
        if (result != Result.IN_PROGRESS) return;
        
        addLog("--- Round " + round + " ---");
        
        // Player party attacks
        if (playerTurn) {
            executePlayerAttacks();
            playerTurn = false;
        } else {
            // Enemy attacks
            executeEnemyAttacks();
            playerTurn = true;
            round++;
        }
        
        // Check for combat end
        checkCombatEnd();
    }
    
    /**
     * Executes attacks from the player's party.
     */
    private void executePlayerAttacks() {
        // Player attacks first (if player has a weapon)
        if (player != null) {
            int playerDamage = calculatePlayerDamage();
            Enemy target = getRandomLivingEnemy();
            if (target != null && playerDamage > 0) {
                target.takeDamage(playerDamage);
                addLog("You deal " + playerDamage + " damage to " + target.getName() + "!");
                if (target.getCurrentHealth() <= 0) {
                    addLog(target.getName() + " is defeated!");
                }
            }
        }
        
        // Party members attack
        if (playerParty != null) {
            for (PartyMember member : playerParty.getMembers()) {
                if (member.getCurrentHealth() <= 0) continue;
                
                Enemy target = getRandomLivingEnemy();
                if (target == null) break;
                
                int damage = calculateMemberDamage(member);
                target.takeDamage(damage);
                addLog(member.getName() + " deals " + damage + " damage to " + target.getName() + "!");
                
                if (target.getCurrentHealth() <= 0) {
                    addLog(target.getName() + " is defeated!");
                }
            }
        }
    }
    
    /**
     * Executes attacks from enemies.
     */
    private void executeEnemyAttacks() {
        for (Enemy enemy : enemies) {
            if (enemy.getCurrentHealth() <= 0) continue;
            
            // Enemies attack party members or player
            int damage = enemy.getAttack() + random.nextInt(enemy.getAttack() / 2 + 1);
            
            // Determine target (player or party member)
            if (playerParty != null && !playerParty.getMembers().isEmpty() && random.nextDouble() > 0.3) {
                // 70% chance to target party members
                PartyMember target = getRandomLivingMember();
                if (target != null) {
                    int defense = target.getDefense();
                    int actualDamage = Math.max(1, damage - defense);
                    target.takeDamage(actualDamage);
                    addLog(enemy.getName() + " attacks " + target.getName() + " for " + actualDamage + " damage!");
                    
                    if (target.getCurrentHealth() <= 0) {
                        addLog(target.getName() + " is knocked out!");
                    }
                }
            } else {
                // Target player
                // Player health is handled by PlayerEnergy
                addLog(enemy.getName() + " attacks you!");
            }
        }
    }
    
    /**
     * Calculates player damage based on equipped weapon.
     */
    private int calculatePlayerDamage() {
        int baseDamage = 5; // Base fist damage
        
        if (player != null) {
            Equipment weapon = player.getMainHand();
            if (weapon != null) {
                baseDamage = weapon.getValue();
            }
        }
        
        // Add some randomness
        return baseDamage + random.nextInt(baseDamage / 2 + 1);
    }
    
    /**
     * Calculates damage from a party member.
     */
    private int calculateMemberDamage(PartyMember member) {
        int attack = member.getAttack();
        
        // Morale bonus
        double moraleBonus = 1.0 + (member.getMorale() - 50) * MORALE_ATTACK_BONUS;
        
        // Random variance
        int damage = (int)(attack * moraleBonus) + random.nextInt(attack / 2 + 1);
        
        return Math.max(1, damage);
    }
    
    /**
     * Gets a random living enemy.
     */
    private Enemy getRandomLivingEnemy() {
        List<Enemy> living = new ArrayList<>();
        for (Enemy e : enemies) {
            if (e.getCurrentHealth() > 0) {
                living.add(e);
            }
        }
        if (living.isEmpty()) return null;
        return living.get(random.nextInt(living.size()));
    }
    
    /**
     * Gets a random living party member.
     */
    private PartyMember getRandomLivingMember() {
        if (playerParty == null) return null;
        List<PartyMember> living = new ArrayList<>();
        for (PartyMember m : playerParty.getMembers()) {
            if (m.getCurrentHealth() > 0) {
                living.add(m);
            }
        }
        if (living.isEmpty()) return null;
        return living.get(random.nextInt(living.size()));
    }
    
    /**
     * Checks if combat has ended.
     */
    private void checkCombatEnd() {
        // Check if all enemies are dead
        boolean allEnemiesDead = true;
        for (Enemy e : enemies) {
            if (e.getCurrentHealth() > 0) {
                allEnemiesDead = false;
                break;
            }
        }
        
        if (allEnemiesDead) {
            result = Result.VICTORY;
            inCombat = false;
            addLog("Victory! All enemies defeated!");
            return;
        }
        
        // Check if party is wiped
        boolean partyWiped = true;
        if (playerParty != null) {
            for (PartyMember m : playerParty.getMembers()) {
                if (m.getCurrentHealth() > 0) {
                    partyWiped = false;
                    break;
                }
            }
        } else {
            partyWiped = false; // Solo player can't be wiped through party
        }
        
        if (partyWiped && (playerParty == null || playerParty.getMembers().isEmpty())) {
            // Player alone - check rounds limit (player escapes after 10 rounds)
            if (round > 10) {
                result = Result.FLED;
                inCombat = false;
                addLog("You flee from combat!");
            }
        }
    }
    
    /**
     * Attempts to flee from combat.
     * @return true if flee successful
     */
    public boolean attemptFlee() {
        double fleeChance = FLEE_BASE_CHANCE;
        
        // Party speed bonus
        if (playerParty != null) {
            int partySpeed = playerParty.getAverageSpeed();
            fleeChance += partySpeed * SPEED_FLEE_BONUS;
        }
        
        // Each round makes fleeing easier
        fleeChance += round * 0.05;
        
        if (random.nextDouble() < fleeChance) {
            result = Result.FLED;
            inCombat = false;
            addLog("You successfully flee from combat!");
            return true;
        } else {
            addLog("Failed to flee!");
            // Enemies get a free attack
            executeEnemyAttacks();
            return false;
        }
    }
    
    /**
     * Adds a message to the combat log.
     */
    private void addLog(String message) {
        combatLog.add(message);
    }
    
    // Getters
    public boolean isInCombat() { return inCombat; }
    public Result getResult() { return result; }
    public int getRound() { return round; }
    public List<String> getCombatLog() { return combatLog; }
    public List<Enemy> getEnemies() { return enemies; }
    
    /**
     * Gets total experience gained from defeated enemies.
     */
    public int getExperienceGained() {
        int exp = 0;
        for (Enemy e : enemies) {
            if (e.getCurrentHealth() <= 0) {
                exp += e.getExperienceValue();
            }
        }
        return exp;
    }
    
    /**
     * Gets total gold dropped from defeated enemies.
     */
    public int getGoldDropped() {
        int gold = 0;
        for (Enemy e : enemies) {
            if (e.getCurrentHealth() <= 0) {
                gold += e.getGoldDrop();
            }
        }
        return gold;
    }
}
