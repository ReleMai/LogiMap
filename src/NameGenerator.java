import java.util.Random;

/**
 * Generates medieval-themed names for locations and worlds.
 * Contains 200+ location names and 200+ world names.
 */
public class NameGenerator {
    
    // ==================== Location Names (200+) ====================
    private static final String[] LOCATION_NAMES = {
        // Classic Medieval Towns
        "Ironhold", "Ravenspire", "Wolfgate", "Thornwood", "Ashford",
        "Blackwater", "Stonebridge", "Goldcrest", "Silverpeak", "Oakvale",
        "Highcliff", "Deepwell", "Riverdale", "Shadowfen", "Brightmoor",
        "Dragonmere", "Frostholm", "Sunhaven", "Moonfall", "Starlight",
        "Windmere", "Stormwatch", "Fireheart", "Iceveil", "Earthgate",
        
        // Castle/Fort Names
        "Greycastle", "Redkeep", "Whitehall", "Blackthorn", "Ironfort",
        "Stonekeep", "Duskhold", "Dawnguard", "Nightwatch", "Daybreak",
        "Shadowkeep", "Lightspire", "Darkholm", "Brightcastle", "Grimfort",
        
        // Trade Towns
        "Markethollow", "Coinsworth", "Tradegate", "Merchantsend", "Goldport",
        "Silvermarket", "Copperdale", "Ironvale", "Steelwick", "Bronzeton",
        "Gembrook", "Jewelford", "Richmont", "Wealthgate", "Prosperton",
        
        // Coastal/Port Towns
        "Seahaven", "Tidemarsh", "Saltwind", "Harborview", "Anchorage",
        "Waverest", "Shorefall", "Driftwood", "Sailmist", "Seabreeze",
        "Fisherton", "Pearlfin", "Coralcove", "Shelterport", "Marindale",
        "Baywatch", "Crestwave", "Deepshore", "Saltmere", "Tidesend",
        
        // Forest/Woodland Towns
        "Fernwood", "Mossdale", "Willowbrook", "Birchvale", "Pinecrest",
        "Eldergrove", "Mapleton", "Cedarhaven", "Ashwood", "Briarwood",
        "Hollowoak", "Vineleaf", "Greenhollow", "Sylvandale", "Woodmere",
        "Forestgate", "Timberfall", "Leafwind", "Barkstone", "Rootholm",
        
        // Mountain/Highland Towns
        "Peakview", "Summithold", "Cliffside", "Rockdale", "Bouldergate",
        "Cragmoor", "Stonepeak", "Highpass", "Ridgemont", "Valleyford",
        "Gorgemere", "Ravensrock", "Eaglecrest", "Falconspire", "Hawksmoor",
        "Mountaingate", "Alpinedale", "Snowpeak", "Frostpeak", "Icecrest",
        
        // River/Lake Towns
        "Riverside", "Lakemere", "Streamdale", "Brookhaven", "Pondview",
        "Watergate", "Millbrook", "Fordham", "Crossings", "Bridgeton",
        "Damsworth", "Fallsview", "Rapidston", "Stillwater", "Clearbrook",
        "Springdale", "Wellspring", "Fountainhead", "Baybrook", "Creekside",
        
        // Plains/Meadow Towns
        "Meadowvale", "Plainsworth", "Grassholm", "Fieldgate", "Harvest",
        "Wheatdale", "Barleybrook", "Hayward", "Cornfield", "Millworth",
        "Farmerston", "Shepherdsgate", "Herdsman", "Rancher", "Plowfield",
        "Greenfield", "Goldmeadow", "Sunfield", "Wildgrass", "Opendale",
        
        // Religious/Holy Towns
        "Sanctum", "Holywell", "Blesseddale", "Templegate", "Shrinewood",
        "Prayerstone", "Faithhold", "Devotion", "Soulrest", "Spiritholm",
        "Lightbringer", "Dawnchapel", "Evensong", "Vespers", "Matinsgate",
        "Pilgrimrest", "Saintsworth", "Angelfall", "Seraphim", "Celestia",
        
        // Mining Towns
        "Ironvein", "Goldmine", "Silverpit", "Copperholm", "Coaldale",
        "Oreshire", "Gemstone", "Quarrytown", "Mineshaft", "Deepdelve",
        "Tunnelton", "Cavemere", "Underwold", "Darkmine", "Richvein",
        
        // Crafting/Industry Towns
        "Forgeton", "Smithdale", "Anvilholm", "Hammerfall", "Steelgate",
        "Bladeshire", "Armordale", "Shieldwall", "Weaponsworth", "Craftholm",
        "Tannerdale", "Weaverton", "Potterfield", "Glasswick", "Dyerbrook",
        "Carpenter", "Masondale", "Buildergate", "Stonemason", "Woodwright",
        
        // Noble/Royal Towns
        "Kingsport", "Queensgate", "Princevale", "Dukeston", "Earlswick",
        "Baronsworth", "Lordsholm", "Ladymere", "Noblegate", "Royalford",
        "Crownsville", "Scepterdale", "Thronehollow", "Regalton", "Majestia",
        
        // Dark/Mysterious Towns
        "Shadowmere", "Darkhollow", "Grimdale", "Dreadfort", "Gloomhaven",
        "Nightshade", "Curseton", "Hauntdale", "Specterwick", "Ghostholm",
        "Witchwood", "Warlockgate", "Sorcerervale", "Mysticdale", "Enigmaton",
        
        // Ancient/Historic Towns
        "Oldtown", "Ancientgate", "Ruindale", "Relicholm", "Heritage",
        "Legacyton", "Historica", "Timeworn", "Agegate", "Elderholm",
        "Ancestral", "Founderswick", "Originalton", "Firstdale", "Primordial",
        
        // Seasonal Towns
        "Springvale", "Summerdale", "Autumnhollow", "Winterfell", "Solstice",
        "Equinoxgate", "Harvestmoon", "Midwinter", "Midsummer", "Firstfrost",
        
        // Compass Direction Towns
        "Northgate", "Southmere", "Eastdale", "Westhaven", "Centerholm",
        "Bordertown", "Frontierdale", "Edgewick", "Outskirts", "Heartland",
        
        // Additional Medieval Names
        "Aldwick", "Bramston", "Caldwell", "Dunmoor", "Edgewood",
        "Falconbridge", "Glendale", "Hartwick", "Ivywood", "Jarrow",
        "Kingswell", "Langford", "Moorgate", "Norwood", "Oldbridge",
        "Pemberton", "Queensbury", "Redmoor", "Sherwood", "Thornbury",
        "Underwood", "Valebrook", "Westbrook", "Yarmouth", "Zelton",
        "Abbotsford", "Bellwick", "Crowmere", "Dunwich", "Elmwood",
        "Foxhollow", "Greymoor", "Hillcrest", "Ironwood", "Jadeholm"
    };
    
    // ==================== World Names (200+) ====================
    private static final String[] WORLD_PREFIXES = {
        "New", "Old", "Great", "Lost", "Forgotten", "Ancient", "Northern", "Southern",
        "Eastern", "Western", "Crystal", "Shadow", "Golden", "Silver", "Iron", "Emerald",
        "Sapphire", "Ruby", "Diamond", "Bronze", "Copper", "Steel", "Obsidian", "Jade",
        "Amber", "Crimson", "Azure", "Verdant", "Ashen", "Frozen", "Burning", "Mystic",
        "Sacred", "Cursed", "Blessed", "Eternal", "Mortal", "Divine", "Infernal", "Celestial"
    };
    
    private static final String[] WORLD_SUFFIXES = {
        "haven", "ford", "port", "shire", "vale", "march", "reach", "lands",
        "realm", "kingdom", "territory", "frontier", "coast", "plains", "highlands",
        "lowlands", "domain", "empire", "dynasty", "sovereignty", "dominion", "province",
        "crown", "throne", "scepter", "banner", "crest", "shield", "sword", "spear"
    };
    
    private static final String[] FULL_WORLD_NAMES = {
        // Kingdom Names
        "The Kingdom of Valdoria", "The Realm of Aethermoor", "The Empire of Drakonheim",
        "The Dominion of Shadowmere", "The Province of Crystalvale", "The Sovereignty of Ironpeak",
        "The Crown of Goldenheart", "The Banner of Silvercrest", "The Shield of Stonewatch",
        
        // Fantasy World Names
        "Eldoria", "Mythrandir", "Avaloria", "Drakenmoor", "Valorheim",
        "Celestria", "Nethermoor", "Frostgard", "Sunspire", "Moonshade",
        "Stormveil", "Thunderhold", "Lightbringer", "Darkhollow", "Grimwald",
        "Fairhaven", "Dreadmoor", "Hopesend", "Glorydale", "Ragemont",
        
        // Geographic World Names
        "The Shattered Isles", "The Broken Lands", "The Endless Plains",
        "The Frozen North", "The Burning South", "The Mystic East", "The Wild West",
        "The Central Kingdoms", "The Outer Reaches", "The Inner Sanctum",
        "The Highland Confederacy", "The Lowland Alliance", "The Coastal Federation",
        "The Mountain Tribes", "The Forest Kingdoms", "The Desert Dominion",
        "The Swamp Territories", "The Island Nations", "The Continental Empire",
        
        // Single Word Names
        "Aloria", "Bryndor", "Calindra", "Dawnshire", "Everhold",
        "Frostheim", "Galdoria", "Hallowmere", "Ironvale", "Jadespire",
        "Kaldoria", "Luminar", "Mythoria", "Nightveil", "Oakheart",
        "Primordia", "Questor", "Ravencrest", "Sundalon", "Thornwood",
        "Umbria", "Valorian", "Westmarch", "Xandalor", "Yewdale",
        "Zephyria", "Aethon", "Brimstone", "Cascadia", "Duskfall",
        "Emberheart", "Flamecrest", "Glacierholm", "Havenwood", "Iceguard",
        
        // Two Word Names
        "Dragon's Reach", "King's Landing", "Queen's Rest", "Knight's Honor",
        "Wizard's Tower", "Archer's Mark", "Warrior's Path", "Mage's Domain",
        "Rogue's Haven", "Cleric's Sanctuary", "Paladin's Light", "Ranger's Wood",
        "Bard's Tale", "Monk's Retreat", "Druid's Grove", "Warlock's Pit",
        
        // Descriptive Names
        "The Verdant Kingdom", "The Ashen Empire", "The Gilded Realm",
        "The Twilight Domain", "The Dawn Territories", "The Dusk Province",
        "The Starlit Lands", "The Moonlit Shores", "The Sunlit Plains",
        "The Shadowed Vale", "The Brightmoor Confederacy", "The Darkmoor Alliance"
    };
    
    private static final Random random = new Random();
    
    // Generic quality names that work anywhere - good mix of styles
    private static final String[] QUALITY_NAMES = {
        // Classic Medieval
        "Ironhold", "Ravenspire", "Thornwood", "Ashford", "Blackwater",
        "Stonebridge", "Goldcrest", "Oakvale", "Highcliff", "Deepwell",
        "Brightmoor", "Moonfall", "Windmere", "Frostholm", "Sunhaven",
        
        // Castle/Keep Names
        "Greycastle", "Redkeep", "Whitehall", "Duskhold", "Dawnguard",
        "Shadowkeep", "Lightspire", "Grimfort", "Nightwatch", "Daybreak",
        
        // Traditional Town Names  
        "Aldwick", "Bramston", "Caldwell", "Dunmoor", "Edgewood",
        "Falconbridge", "Glendale", "Hartwick", "Ivywood", "Jarrow",
        "Kingswell", "Langford", "Moorgate", "Norwood", "Oldbridge",
        "Pemberton", "Queensbury", "Redmoor", "Thornbury", "Westbrook",
        
        // Nature-Inspired (subtle)
        "Ferndale", "Willowbrook", "Birchvale", "Pinecrest", "Eldergrove",
        "Ashwood", "Briarwood", "Hollowoak", "Greenhollow", "Woodmere",
        
        // Geographic (subtle)
        "Ridgemont", "Valleyford", "Cliffside", "Hillcrest", "Plainsworth",
        "Brookhaven", "Riverside", "Millbrook", "Clearbrook", "Stillwater",
        
        // Trade/Prosperity
        "Markethollow", "Tradegate", "Coinsworth", "Richmont", "Goldfield",
        "Copperhill", "Ironvale", "Steelwick", "Silverstream", "Gembrook",
        
        // Historic/Noble
        "Kingsport", "Queensgate", "Lordsholm", "Noblegate", "Crownsville",
        "Regalton", "Earlswick", "Baronsworth", "Princevale", "Dukeston",
        
        // Misc Quality Names
        "Stonehaven", "Ravensrock", "Eaglecrest", "Hawksmoor", "Foxhollow",
        "Wolfsgate", "Bearclaw", "Stagrun", "Lynxmoor", "Crowmere",
        "Dunwich", "Greymoor", "Bellwick", "Abbotsford", "Sherwood",
        "Crossings", "Bridgeton", "Fordham", "Millworth", "Farmerston"
    };
    
    // Special coastal names (only used when VERY close to water)
    private static final String[] COASTAL_SPECIAL = {
        "Seahaven", "Harborview", "Anchorage", "Saltwind", "Waverest",
        "Marindale", "Baywatch", "Shelterport", "Tidesend", "Driftwood"
    };
    
    // Special mountain names (only for high elevation)
    private static final String[] MOUNTAIN_SPECIAL = {
        "Peakview", "Summithold", "Highpass", "Stonepeak", "Cragmoor",
        "Alpinedale", "Frostpeak", "Snowcrest", "Ironvein", "Quarrytown"
    };
    
    /**
     * Gets a location name by index (for consistent naming).
     */
    public static String getLocationName(int index) {
        return LOCATION_NAMES[Math.abs(index) % LOCATION_NAMES.length];
    }
    
    /**
     * Gets a random location name.
     */
    public static String getRandomLocationName() {
        return LOCATION_NAMES[random.nextInt(LOCATION_NAMES.length)];
    }
    
    /**
     * Gets a location name based on coordinates (deterministic).
     */
    public static String getLocationNameForCoords(int x, int y, long seed) {
        int hash = (int) ((x * 374761393L + y * 668265263L + seed) % LOCATION_NAMES.length);
        if (hash < 0) hash += LOCATION_NAMES.length;
        return LOCATION_NAMES[hash];
    }
    
    /**
     * Gets a terrain-appropriate location name.
     * Mostly uses generic quality names, with occasional terrain-specific names.
     * @param nearWater true if within 10 tiles of water
     * @param isMountain true if in mountainous terrain
     * @param isForest true if in forest terrain
     * @param x coordinate for hash
     * @param y coordinate for hash
     * @param seed world seed
     * @return appropriate name for the terrain
     */
    public static String getTerrainAppropriateLocationName(boolean nearWater, boolean isMountain, 
                                                            boolean isForest, int x, int y, long seed) {
        int hash = Math.abs((int) ((x * 374761393L + y * 668265263L + seed) % 1000));
        
        // 80% of the time, use generic quality names regardless of terrain
        // This creates variety and avoids boring repetitive biome names
        if (hash % 10 < 8) {
            return QUALITY_NAMES[hash % QUALITY_NAMES.length];
        }
        
        // 20% of the time, use terrain-specific if applicable
        // Only use coastal names if VERY close to water (not just "near")
        if (nearWater && hash % 5 == 0) {
            return COASTAL_SPECIAL[hash % COASTAL_SPECIAL.length];
        }
        
        // Only use mountain names for actual mountains
        if (isMountain && hash % 5 == 1) {
            return MOUNTAIN_SPECIAL[hash % MOUNTAIN_SPECIAL.length];
        }
        
        // Default to quality names
        return QUALITY_NAMES[hash % QUALITY_NAMES.length];
    }
    
    /**
     * Generates a random world name.
     */
    public static String generateWorldName() {
        int type = random.nextInt(3);
        
        switch (type) {
            case 0:
                // Prefix + Suffix
                return WORLD_PREFIXES[random.nextInt(WORLD_PREFIXES.length)] + 
                       WORLD_SUFFIXES[random.nextInt(WORLD_SUFFIXES.length)];
            case 1:
                // Full name from list
                return FULL_WORLD_NAMES[random.nextInt(FULL_WORLD_NAMES.length)];
            default:
                // "The X of Y" format
                return "The " + WORLD_PREFIXES[random.nextInt(WORLD_PREFIXES.length)] + 
                       " " + capitalize(WORLD_SUFFIXES[random.nextInt(WORLD_SUFFIXES.length)]);
        }
    }
    
    /**
     * Generates a world name from seed (deterministic).
     */
    public static String generateWorldName(long seed) {
        Random seededRandom = new Random(seed);
        int type = seededRandom.nextInt(3);
        
        switch (type) {
            case 0:
                return WORLD_PREFIXES[seededRandom.nextInt(WORLD_PREFIXES.length)] + 
                       WORLD_SUFFIXES[seededRandom.nextInt(WORLD_SUFFIXES.length)];
            case 1:
                return FULL_WORLD_NAMES[seededRandom.nextInt(FULL_WORLD_NAMES.length)];
            default:
                return "The " + WORLD_PREFIXES[seededRandom.nextInt(WORLD_PREFIXES.length)] + 
                       " " + capitalize(WORLD_SUFFIXES[seededRandom.nextInt(WORLD_SUFFIXES.length)]);
        }
    }
    
    /**
     * Gets the total number of location names available.
     */
    public static int getLocationNameCount() {
        return LOCATION_NAMES.length;
    }
    
    /**
     * Gets all location names.
     */
    public static String[] getAllLocationNames() {
        return LOCATION_NAMES.clone();
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
