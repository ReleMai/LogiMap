import java.util.Random;

/**
 * Medieval Name Generator for NPCs.
 * 
 * Generates authentic-sounding medieval names with:
 * - Gender-specific first names (male/female)
 * - Profession-based surnames (Smith, Miller, Fletcher, etc.)
 * - Noble surnames (von/de + location names)
 * - Regional/cultural variations
 */
public class MedievalNameGenerator {
    
    private static final Random random = new Random();
    
    // ==================== MALE FIRST NAMES ====================
    
    private static final String[] MALE_NAMES_COMMON = {
        // Anglo-Saxon / English
        "William", "Thomas", "John", "Robert", "Richard", "Henry", "Edward", "Walter",
        "Roger", "Ralph", "Geoffrey", "Hugh", "Simon", "Peter", "Stephen", "Nicholas",
        "Adam", "Andrew", "Philip", "James", "Martin", "Gilbert", "Alexander", "David",
        // Germanic
        "Frederick", "Conrad", "Otto", "Ludwig", "Heinrich", "Wilhelm", "Karl", "Albert",
        "Bernard", "Arnold", "Gerhard", "Dietrich", "Werner", "Gunther", "Siegfried",
        // French / Norman
        "Guillaume", "Jacques", "Pierre", "Jean", "Louis", "Charles", "Francis", "Raoul",
        "Gaston", "Olivier", "Roland", "Tristan", "Gilles", "Baudoin", "Thibault",
        // Celtic
        "Duncan", "Malcolm", "Angus", "Lachlan", "Callum", "Niall", "Cormac", "Declan",
        "Dermot", "Ewan", "Fergus", "Hamish", "Iain", "Kenneth", "Roderick"
    };
    
    private static final String[] MALE_NAMES_PEASANT = {
        "Wat", "Jack", "Tom", "Dick", "Harry", "Will", "Ned", "Robin", "Alf", "Bert",
        "Clem", "Dan", "Ed", "Frank", "George", "Hal", "Ike", "Jake", "Lem", "Matt",
        "Nat", "Owen", "Pete", "Ralf", "Sam", "Ted", "Vic", "Walt", "Zeke", "Hugh"
    };
    
    private static final String[] MALE_NAMES_NOBLE = {
        "Aldric", "Beric", "Cedric", "Darian", "Edric", "Florian", "Godric", "Hadrian",
        "Ivar", "Jareth", "Kendrick", "Leoric", "Magnus", "Nikolaus", "Osric", "Percival",
        "Quentin", "Roderic", "Sebastian", "Theodric", "Ulric", "Valentin", "Wolfram"
    };
    
    // ==================== FEMALE FIRST NAMES ====================
    
    private static final String[] FEMALE_NAMES_COMMON = {
        // Anglo-Saxon / English
        "Alice", "Agnes", "Anne", "Beatrice", "Catherine", "Eleanor", "Elizabeth", "Emma",
        "Isabella", "Joan", "Juliana", "Margaret", "Mary", "Matilda", "Rose", "Sarah",
        // Germanic
        "Adelheid", "Brunhilde", "Gertrude", "Hedwig", "Hildegard", "Kunigunde", "Mechthild",
        "Richilde", "Sieglinde", "Walburga", "Gisela", "Irmgard", "Liutgard",
        // French / Norman
        "Blanche", "Cecile", "Charlotte", "Claire", "Colette", "Eloise", "Genevieve",
        "Helene", "Isabeau", "Jeanne", "Marguerite", "Marie", "Nicolette", "Odette",
        // Celtic
        "Brigid", "Deirdre", "Eileen", "Fiona", "Grainne", "Iona", "Keira", "Maeve",
        "Niamh", "Roisin", "Siobhan", "Una", "Aisling", "Caitlin", "Moira"
    };
    
    private static final String[] FEMALE_NAMES_PEASANT = {
        "Meg", "Nell", "Bess", "Peg", "Moll", "Kate", "Nan", "Jane", "Poll", "Sal",
        "Doll", "Gwen", "Hild", "Ida", "Jen", "Kit", "Lil", "Maud", "Nora", "Tess"
    };
    
    private static final String[] FEMALE_NAMES_NOBLE = {
        "Adelina", "Bianca", "Celestine", "Delphine", "Elowen", "Felicity", "Gwyneth",
        "Helena", "Isadora", "Juliette", "Katarina", "Lucinda", "Mirielle", "Noelle",
        "Ophelia", "Philippa", "Rosalind", "Seraphina", "Theodora", "Vivienne"
    };
    
    // ==================== SURNAMES ====================
    
    // Occupational surnames (based on profession)
    private static final String[] SURNAMES_SMITH = {
        "Smith", "Blacksmith", "Goldsmith", "Silversmith", "Coppersmith", "Ironside",
        "Forgehand", "Hammer", "Anvil", "Steelwright"
    };
    
    private static final String[] SURNAMES_FARMER = {
        "Farmer", "Plowman", "Shepherd", "Miller", "Baker", "Brewer", "Cooper",
        "Thatcher", "Hayward", "Fielding", "Barley", "Wheat", "Oats", "Rye"
    };
    
    private static final String[] SURNAMES_HUNTER = {
        "Hunter", "Fletcher", "Bowyer", "Fowler", "Fisher", "Trapper", "Forester",
        "Woodsman", "Archer", "Stalker", "Tracker"
    };
    
    private static final String[] SURNAMES_MERCHANT = {
        "Merchant", "Trader", "Chapman", "Seller", "Dealer", "Monger", "Chandler",
        "Draper", "Mercer", "Vintner", "Goldstein", "Silverman"
    };
    
    private static final String[] SURNAMES_CRAFTSMAN = {
        "Wright", "Carpenter", "Mason", "Potter", "Weaver", "Tanner", "Tailor",
        "Cobbler", "Chandler", "Glazier", "Roper", "Saddler", "Wheeler", "Turner"
    };
    
    private static final String[] SURNAMES_WARRIOR = {
        "Strongarm", "Ironheart", "Steelfist", "Battleborn", "Warblade", "Shieldbreaker",
        "Sergeant", "Marshall", "Knight", "Squire", "Champion", "Valor", "Brave"
    };
    
    private static final String[] SURNAMES_SCHOLAR = {
        "Clerk", "Scribe", "Scholar", "Sage", "Wiseman", "Learned", "Bookman",
        "Penwright", "Inkwell", "Quillman", "Reader", "Lorekeeper"
    };
    
    // Geographic surnames
    private static final String[] SURNAMES_GEOGRAPHIC = {
        "Hill", "Dale", "Brook", "Ford", "Wood", "Field", "Green", "Lake",
        "Stone", "Bridge", "Marsh", "Moore", "Heath", "Grove", "Cliff",
        "Valley", "Meadow", "Glen", "Shore", "Ridge", "Hollow", "Spring"
    };
    
    // Descriptive surnames
    private static final String[] SURNAMES_DESCRIPTIVE = {
        "Long", "Short", "Young", "Old", "White", "Black", "Brown", "Grey",
        "Red", "Swift", "Strong", "Wise", "Bold", "Hardy", "Stern", "Gentle",
        "Quick", "Sharp", "Stout", "Thin", "Tall", "Small", "Fair", "Dark"
    };
    
    // Noble surname prefixes and suffixes
    private static final String[] NOBLE_PREFIXES = {
        "von", "de", "du", "van", "af", "zu", "di", "da", "delle"
    };
    
    private static final String[] NOBLE_PLACES = {
        "Blackwood", "Whitehall", "Stonegate", "Ironhold", "Goldcrest", "Silvermere",
        "Ravenscroft", "Thornwood", "Highcastle", "Deepvale", "Stormwind", "Frostholm",
        "Brightwater", "Darkmore", "Ashford", "Willowmere", "Oakenshield", "Hawkridge",
        "Lionheart", "Dragonstone", "Wolfsbane", "Eaglecrest", "Bearholm", "Foxley"
    };

    // Extra flavor epithets for creative surnames
    private static final String[] EPITHETS = {
        "the Bold", "the Wanderer", "the Stalwart", "the Quick", "the Cunning",
        "the Steadfast", "the Wayfarer", "the Bright", "the Surehand", "the Even-Tongue",
        "the Stoneshaper", "the Farwalker", "the Flameborn", "the Frostborn", "the Oathbound"
    };
    
    // ==================== GENERATION METHODS ====================
    
    /**
     * Gender enumeration for name generation.
     */
    public enum Gender {
        MALE, FEMALE
    }
    
    /**
     * Social class for name style.
     */
    public enum SocialClass {
        PEASANT,   // Simple names, occupational surnames
        COMMON,    // Standard names, varied surnames
        MERCHANT,  // Slightly fancier names
        NOBLE      // Elaborate names, "von/de" surnames
    }
    
    /**
     * Generates a complete random name.
     */
    public static String generateName(Gender gender) {
        return generateName(gender, SocialClass.COMMON);
    }
    
    /**
     * Generates a complete name based on gender and social class.
     */
    public static String generateName(Gender gender, SocialClass socialClass) {
        String firstName = generateFirstName(gender, socialClass);
        String surname = generateSurname(socialClass);
        return firstName + " " + surname;
    }
    
    /**
     * Generates a first name based on gender and social class.
     */
    public static String generateFirstName(Gender gender, SocialClass socialClass) {
        String[] namePool;
        
        if (gender == Gender.MALE) {
            switch (socialClass) {
                case PEASANT:
                    namePool = random.nextDouble() < 0.7 ? MALE_NAMES_PEASANT : MALE_NAMES_COMMON;
                    break;
                case NOBLE:
                    namePool = random.nextDouble() < 0.6 ? MALE_NAMES_NOBLE : MALE_NAMES_COMMON;
                    break;
                default:
                    namePool = MALE_NAMES_COMMON;
            }
        } else {
            switch (socialClass) {
                case PEASANT:
                    namePool = random.nextDouble() < 0.7 ? FEMALE_NAMES_PEASANT : FEMALE_NAMES_COMMON;
                    break;
                case NOBLE:
                    namePool = random.nextDouble() < 0.6 ? FEMALE_NAMES_NOBLE : FEMALE_NAMES_COMMON;
                    break;
                default:
                    namePool = FEMALE_NAMES_COMMON;
            }
        }
        
        return namePool[random.nextInt(namePool.length)];
    }
    
    /**
     * Generates a surname based on social class.
     */
    public static String generateSurname(SocialClass socialClass) {
        String base;
        switch (socialClass) {
            case PEASANT:
                base = random.nextDouble() < 0.5 
                    ? SURNAMES_GEOGRAPHIC[random.nextInt(SURNAMES_GEOGRAPHIC.length)]
                    : SURNAMES_DESCRIPTIVE[random.nextInt(SURNAMES_DESCRIPTIVE.length)];
                break;
            case NOBLE:
                String prefix = NOBLE_PREFIXES[random.nextInt(NOBLE_PREFIXES.length)];
                String place = NOBLE_PLACES[random.nextInt(NOBLE_PLACES.length)];
                base = prefix + " " + place;
                break;
            case MERCHANT:
                base = random.nextDouble() < 0.6
                    ? SURNAMES_MERCHANT[random.nextInt(SURNAMES_MERCHANT.length)]
                    : SURNAMES_CRAFTSMAN[random.nextInt(SURNAMES_CRAFTSMAN.length)];
                break;
            default:
                base = generateCommonSurname();
        }
        return embellishSurname(base);
    }
    
    /**
     * Generates a varied common surname.
     */
    private static String generateCommonSurname() {
        int choice = random.nextInt(6);
        switch (choice) {
            case 0: return SURNAMES_FARMER[random.nextInt(SURNAMES_FARMER.length)];
            case 1: return SURNAMES_CRAFTSMAN[random.nextInt(SURNAMES_CRAFTSMAN.length)];
            case 2: return SURNAMES_GEOGRAPHIC[random.nextInt(SURNAMES_GEOGRAPHIC.length)];
            case 3: return SURNAMES_DESCRIPTIVE[random.nextInt(SURNAMES_DESCRIPTIVE.length)];
            case 4: return SURNAMES_HUNTER[random.nextInt(SURNAMES_HUNTER.length)];
            default: return SURNAMES_SMITH[random.nextInt(SURNAMES_SMITH.length)];
        }
    }
    
    /**
     * Generates a surname appropriate for a profession.
     */
    public static String generateProfessionSurname(String profession) {
        profession = profession.toLowerCase();
        
        if (profession.contains("smith") || profession.contains("forge") || profession.contains("metal")) {
            return SURNAMES_SMITH[random.nextInt(SURNAMES_SMITH.length)];
        }
        if (profession.contains("farm") || profession.contains("mill") || profession.contains("brew")) {
            return SURNAMES_FARMER[random.nextInt(SURNAMES_FARMER.length)];
        }
        if (profession.contains("hunt") || profession.contains("fish") || profession.contains("forest")) {
            return SURNAMES_HUNTER[random.nextInt(SURNAMES_HUNTER.length)];
        }
        if (profession.contains("merchant") || profession.contains("trade") || profession.contains("shop")) {
            return SURNAMES_MERCHANT[random.nextInt(SURNAMES_MERCHANT.length)];
        }
        if (profession.contains("guard") || profession.contains("soldier") || profession.contains("knight")) {
            return SURNAMES_WARRIOR[random.nextInt(SURNAMES_WARRIOR.length)];
        }
        if (profession.contains("scholar") || profession.contains("scribe") || profession.contains("mage")) {
            return SURNAMES_SCHOLAR[random.nextInt(SURNAMES_SCHOLAR.length)];
        }
        if (profession.contains("craft") || profession.contains("build") || profession.contains("make")) {
            return SURNAMES_CRAFTSMAN[random.nextInt(SURNAMES_CRAFTSMAN.length)];
        }
        
        // Default to common surname
        return embellishSurname(generateCommonSurname());
    }
    
    /**
     * Generates a complete name for a profession.
     */
    public static String generateNameForProfession(Gender gender, String profession) {
        String firstName = generateFirstName(gender, SocialClass.COMMON);
        String surname = generateProfessionSurname(profession);
        return firstName + " " + surname;
    }
    
    /**
     * Generates a random gender.
     */
    public static Gender randomGender() {
        return random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
    }

    /**
     * Adds occasional epithets or double surnames for variety.
     */
    private static String embellishSurname(String surname) {
        // Sometimes create a double-barreled surname
        if (random.nextDouble() < 0.12) {
            String second = generateCommonSurname();
            if (!second.equalsIgnoreCase(surname)) {
                surname = surname + "-" + second;
            }
        }
        // Occasionally append an epithet for flair
        if (random.nextDouble() < 0.16) {
            surname = surname + " " + EPITHETS[random.nextInt(EPITHETS.length)];
        }
        return surname;
    }
    
    /**
     * Generates a tavern patron name (common or peasant class).
     */
    public static String generateTavernPatronName(Gender gender) {
        SocialClass socialClass = random.nextDouble() < 0.6 ? SocialClass.PEASANT : SocialClass.COMMON;
        return generateName(gender, socialClass);
    }
    
    /**
     * Generates a noble NPC name.
     */
    public static String generateNobleName(Gender gender) {
        return generateName(gender, SocialClass.NOBLE);
    }
    
    /**
     * Generates a merchant NPC name.
     */
    public static String generateMerchantName(Gender gender) {
        return generateName(gender, SocialClass.MERCHANT);
    }
    
    /**
     * Sets the random seed for reproducible name generation.
     */
    public static void setSeed(long seed) {
        random.setSeed(seed);
    }
}
