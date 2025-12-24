# NPC Party System Implementation

## Overview
Implemented a comprehensive NPC party system where NPCs can group together, share resources, and travel between towns.

## New Features

### 1. NPCParty Class (`NPCParty.java`)
A complete party management system with:

- **Party Formation**: Groups of 2-4 NPCs that travel together
- **Shared Inventory**: Parties share a common resource pool
- **AI Behaviors**: 
  - **IDLE**: Resting between activities
  - **TRAVELING**: Moving between towns
  - **GATHERING**: Collecting resources
  - **TRADING**: Selling goods at towns
- **Formation Movement**: Members arranged in a small cluster around party center
- **Activity Description**: Detailed info about party status and inventory

### 2. Enhanced NPCManager
Updated to handle both solo NPCs and parties:

- **Party Formation Logic**: 40% chance roamers form parties (2-4 members)
- **Visual Party Indicator**: Gold badge with member count displayed on party sprite
- **Party Rendering**: Leader rendered at center with count badge
- **Party Interaction**: Click detection prioritizes parties
- **Dynamic AI**: Parties autonomously pick destinations, gather resources, and trade

### 3. Party Visual Indicators
- Gold circular badge with black number showing party size
- Positioned in top-right of the party leader sprite
- Only shows when party has 2+ members
- Professional styling with border and centered text

### 4. Fixed NPC Hiring System
- **PartyMember Updates**: Added missing methods:
  - `setProfessionName(String)`: Preserves NPC's original profession
  - `setGender(boolean)`: Maintains gender consistency
  - `setStatsFromCharacterStats(CharacterStats)`: Transfers stats properly
- **Stat Transfer**: Uses CharacterStats.Stat enum correctly (STR, DEX, CON)
- **Identity Preservation**: Hired NPCs now keep their name and profession in party menu

### 5. NPC Party Interactions
- Clicking a party opens interaction with the party leader
- Can see all party members through interaction
- `getPartyForNPC(NPC)` method to find which party an NPC belongs to
- Shared party inventory accessible through interactions

## Technical Details

### Party AI Behavior
```java
public enum PartyTask {
    IDLE,       // Resting
    GATHERING,  // Collecting resources
    TRADING,    // Selling at town
    TRAVELING   // Moving between towns
}
```

Parties autonomously:
1. Rest at towns after completing activities
2. Pick new destinations from available towns
3. Gather resources (40% chance)
4. Travel to towns to trade (40% chance)
5. Sell inventory and pick new tasks

### Resource Management
- Parties accumulate resources while gathering
- Resources sold when arriving at towns
- Shared inventory system (Map<ResourceType, Integer>)
- Methods: `addResource()`, `removeResource()`, `getResourceAmount()`

### Formation System
- Members positioned in circular formation around party center
- Small radius (0.3 tiles) for tight grouping
- All members move together synchronized
- Leader represents the party in interactions

## Usage

### For Players
1. **Spot Parties**: Look for NPCs with gold number badges
2. **Interact**: Right-click party leader to interact with whole party
3. **Observe Behavior**: Watch parties travel between towns, gather resources
4. **Hire from Taverns**: Recruited NPCs now preserve their names/professions

### For Developers
```java
// Create a party
List<NPC> members = new ArrayList<>();
NPCParty party = new NPCParty(members);

// Set destination
party.setDestination(targetTown);

// Get party info
String description = party.getActivityDescription();

// Access inventory
Map<ResourceType, Integer> inventory = party.getSharedInventory();
```

## Configuration
- `PARTY_FORMATION_CHANCE = 0.4`: 40% of roamers form parties
- Party size: 2-4 members
- Formation radius: 0.3 tiles
- Movement speed: 0.5 tiles/second
- Task timing: 2-5 seconds per activity

## Integration Points

### MapCanvas
- Updated `npcManager.update()` call to include towns list
- NPC click detection works with parties

### NPCManager
- `render()`: Displays party count badges
- `update()`: Processes party AI and movement
- `getNPCAt()`: Prioritizes party interactions
- `getPartyForNPC()`: Finds NPC's party membership

### TavernRecruitmentPanel
- Fixed `createPartyMemberFromNPC()` to preserve identity
- Properly transfers CharacterStats to PartyMember

## Future Enhancements
Possible additions:
- Party combat bonuses
- Equipment rendering for NPCs (like PlayerSprite)
- Party reputation/loyalty system
- Custom party names and formations
- NPC recruitment into player party from world parties
- Trade negotiations with NPC parties
- Party quests and contracts
