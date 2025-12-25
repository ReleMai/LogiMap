import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the player's party of companions.
 * Manages party members, movement, and combat calculations.
 */
public class Party {
    
    // Party members
    private List<PartyMember> members;
    
    // Party limits
    private int maxPartySize = 10;
    private static final int INITIAL_MAX_SIZE = 5;
    
    // Party stats (calculated from members)
    private int totalAttack;
    private int totalDefense;
    private int averageSpeed;
    private int totalWeeklyWages;
    
    // Party morale (affects all members)
    private int partyMorale = 100;
    
    // Movement bonus from scouts
    private double movementBonus = 1.0;
    
    // Party formation for display
    public enum Formation {
        LINE,       // Single file
        WEDGE,      // V-shape
        SQUARE,     // Compact
        SPREAD      // Wide formation
    }
    private Formation formation = Formation.LINE;
    
    public Party() {
        members = new ArrayList<>();
        maxPartySize = INITIAL_MAX_SIZE;
    }
    
    /**
     * Adds a member to the party.
     * @return true if successful, false if party is full
     */
    public boolean addMember(PartyMember member) {
        if (members.size() >= maxPartySize) {
            return false;
        }
        
        members.add(member);
        recalculateStats();
        return true;
    }
    
    /**
     * Removes a member from the party.
     */
    public boolean removeMember(PartyMember member) {
        boolean removed = members.remove(member);
        if (removed) {
            recalculateStats();
        }
        return removed;
    }
    
    /**
     * Removes a member by index.
     */
    public PartyMember removeMember(int index) {
        if (index >= 0 && index < members.size()) {
            PartyMember member = members.remove(index);
            recalculateStats();
            return member;
        }
        return null;
    }
    
    /**
     * Recalculates party stats from all members.
     */
    private void recalculateStats() {
        totalAttack = 0;
        totalDefense = 0;
        totalWeeklyWages = 0;
        int totalSpeed = 0;
        int scoutCount = 0;
        
        for (PartyMember member : members) {
            totalAttack += member.getAttack();
            totalDefense += member.getDefense();
            totalSpeed += member.getSpeed();
            totalWeeklyWages += member.getWeeklyWage();
            
            if (member.getRole() == PartyMember.Role.SCOUT) {
                scoutCount++;
            }
        }
        
        averageSpeed = members.isEmpty() ? 0 : totalSpeed / members.size();
        
        // Scouts provide movement bonus (5% per scout, max 20%)
        movementBonus = 1.0 + Math.min(scoutCount * 0.05, 0.2);
    }
    
    /**
     * Gets the total combat power of the party.
     */
    public int getCombatPower() {
        int power = 0;
        for (PartyMember member : members) {
            power += member.getCombatPower();
        }
        return power;
    }
    
    /**
     * Heals all party members by a percentage.
     */
    public void healAll(double percentage) {
        for (PartyMember member : members) {
            int healAmount = (int)(member.getMaxHealth() * percentage);
            member.heal(healAmount);
        }
    }
    
    /**
     * Updates party morale (affects all members).
     */
    public void updateMorale(int change) {
        partyMorale = Math.max(0, Math.min(100, partyMorale + change));
        
        // Update individual member morale towards party morale
        for (PartyMember member : members) {
            int memberMorale = member.getMorale();
            int diff = partyMorale - memberMorale;
            member.setMorale(memberMorale + diff / 2);
        }
    }
    
    /**
     * Pays weekly wages. Returns true if successful.
     */
    public boolean payWages(Currency playerCurrency) {
        if (playerCurrency.getTotalCopper() >= totalWeeklyWages) {
            playerCurrency.subtract(totalWeeklyWages);
            updateMorale(5); // Boost morale when paid
            return true;
        } else {
            updateMorale(-20); // Big morale hit if unpaid
            return false;
        }
    }
    
    /**
     * Renders the party on the map near the player.
     */
    public void render(GraphicsContext gc, double playerScreenX, double playerScreenY, double tileSize) {
        if (members.isEmpty()) return;
        
        int count = Math.min(members.size(), 3); // Show max 3 sprites
        double memberSize = tileSize * 0.5;
        
        // Position members based on formation
        double[][] positions = getFormationPositions(count, tileSize);
        
        for (int i = 0; i < count; i++) {
            PartyMember member = members.get(i);
            double x = playerScreenX + positions[i][0];
            double y = playerScreenY + positions[i][1];
            member.renderMapSprite(gc, x, y, memberSize);
        }
        
        // If more than 3 members, show count badge
        if (members.size() > 3) {
            double badgeX = playerScreenX + tileSize * 0.8;
            double badgeY = playerScreenY - tileSize * 0.2;
            
            gc.setFill(Color.web("#1a1208").deriveColor(0, 1, 1, 0.9));
            gc.fillOval(badgeX, badgeY, tileSize * 0.4, tileSize * 0.4);
            gc.setStroke(Color.web("#c4a574"));
            gc.setLineWidth(1);
            gc.strokeOval(badgeX, badgeY, tileSize * 0.4, tileSize * 0.4);
            
            gc.setFill(Color.web("#c4a574"));
            gc.setFont(javafx.scene.text.Font.font("Georgia", javafx.scene.text.FontWeight.BOLD, tileSize * 0.25));
            gc.fillText("+" + (members.size() - 3), badgeX + tileSize * 0.08, badgeY + tileSize * 0.28);
        }
    }
    
    /**
     * Gets formation positions relative to player.
     */
    private double[][] getFormationPositions(int count, double tileSize) {
        return switch (formation) {
            case LINE -> new double[][] {
                {-tileSize * 0.6, tileSize * 0.3},
                {-tileSize * 1.0, tileSize * 0.5},
                {-tileSize * 1.4, tileSize * 0.7}
            };
            case WEDGE -> new double[][] {
                {-tileSize * 0.5, tileSize * 0.5},
                {-tileSize * 0.8, 0},
                {-tileSize * 0.8, tileSize}
            };
            case SQUARE -> new double[][] {
                {-tileSize * 0.5, 0},
                {-tileSize * 0.5, tileSize * 0.5},
                {0, tileSize * 0.5}
            };
            case SPREAD -> new double[][] {
                {-tileSize * 0.8, -tileSize * 0.3},
                {-tileSize * 0.8, tileSize * 0.8},
                {tileSize * 0.3, tileSize * 0.5}
            };
        };
    }
    
    /**
     * Increases max party size (from leadership skill, etc.).
     */
    public void increaseMaxSize(int amount) {
        maxPartySize += amount;
    }
    
    // Getters
    public List<PartyMember> getMembers() { return members; }
    public int getSize() { return members.size(); }
    public int getMaxSize() { return maxPartySize; }
    public int getTotalAttack() { return totalAttack; }
    public int getTotalDefense() { return totalDefense; }
    public int getAverageSpeed() { return averageSpeed; }
    public int getTotalWeeklyWages() { return totalWeeklyWages; }
    public int getPartyMorale() { return partyMorale; }
    public double getMovementBonus() { return movementBonus; }
    public Formation getFormation() { return formation; }
    
    public void setFormation(Formation formation) { this.formation = formation; }
    
    public boolean isEmpty() { return members.isEmpty(); }
    public boolean isFull() { return members.size() >= maxPartySize; }
    
    /**
     * Gets a member by index.
     */
    public PartyMember getMember(int index) {
        if (index >= 0 && index < members.size()) {
            return members.get(index);
        }
        return null;
    }
    
    /**
     * Converts party to a save-friendly map.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("maxPartySize", maxPartySize);
        data.put("partyMorale", partyMorale);
        data.put("formation", formation.name());
        
        List<Map<String, Object>> memberData = new ArrayList<>();
        for (PartyMember member : members) {
            memberData.add(member.toMap());
        }
        data.put("members", memberData);
        
        return data;
    }
    
    /**
     * Creates a party from saved data.
     */
    @SuppressWarnings("unchecked")
    public static Party fromMap(Map<String, Object> data) {
        Party party = new Party();
        
        try {
            party.maxPartySize = (Integer) data.getOrDefault("maxPartySize", INITIAL_MAX_SIZE);
            party.partyMorale = (Integer) data.getOrDefault("partyMorale", 100);
            
            String formationStr = (String) data.getOrDefault("formation", "LINE");
            party.formation = Formation.valueOf(formationStr);
            
            List<Map<String, Object>> memberData = (List<Map<String, Object>>) data.get("members");
            if (memberData != null) {
                for (Map<String, Object> md : memberData) {
                    PartyMember member = PartyMember.fromMap(md);
                    if (member != null) {
                        party.members.add(member);
                    }
                }
            }
            
            party.recalculateStats();
        } catch (Exception e) {
            System.err.println("Failed to load party: " + e.getMessage());
        }
        
        return party;
    }
}
