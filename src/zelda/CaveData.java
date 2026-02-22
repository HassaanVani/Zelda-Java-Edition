package zelda;

/**
 * Defines all overworld cave/shop contents for the 1st Quest.
 * Each cave has a type, NPC text, and items offered.
 *
 * Cave types:
 * - ITEM_CAVE: Old man gives a single item (sword, letter, etc.)
 * - SHOP: Merchant sells up to 3 items for rupees
 * - HINT: Old man/woman gives a hint
 * - MONEY: Pay or take rupees
 * - POTION_SHOP: Sells potions (requires Letter)
 * - HEART_CONTAINER: Contains a heart container
 * - WHITE_SWORD: Requires 5+ hearts
 * - MAGICAL_SWORD: Requires 12+ hearts
 * - MONEY_GAME: Gambling game
 */
public class CaveData {

    public enum CaveType {
        ITEM_CAVE, SHOP, HINT, MONEY_MAKING_GAME,
        POTION_SHOP, HEART_CONTAINER, WHITE_SWORD, MAGICAL_SWORD
    }

    public static class CaveDef {
        public final CaveType type;
        public final String npcText;
        public final String npcText2;
        public final String[] itemNames;   // items offered (up to 3)
        public final int[] itemCosts;      // rupee cost per item (0 = free)
        public final int requiredHearts;   // min hearts needed (0 = none)

        public CaveDef(CaveType type, String text, String text2,
                       String[] items, int[] costs, int requiredHearts) {
            this.type = type;
            this.npcText = text;
            this.npcText2 = text2;
            this.itemNames = items;
            this.itemCosts = costs;
            this.requiredHearts = requiredHearts;
        }
    }

    /**
     * Returns the cave definition for a given overworld room coordinate.
     * Based on the NES 1st Quest cave placements.
     */
    public static CaveDef getCave(int roomX, int roomY) {
        String key = roomX + "," + roomY;
        switch (key) {

            // ==================== Sword Caves ====================
            case "7,7":  // Wooden Sword (starting screen)
                return new CaveDef(CaveType.ITEM_CAVE,
                    "IT'S DANGEROUS TO GO",
                    "ALONE! TAKE THIS.",
                    new String[]{"WOODEN_SWORD"}, new int[]{0}, 0);

            case "2,1":  // White Sword (requires 5+ hearts)
                return new CaveDef(CaveType.WHITE_SWORD,
                    "MASTER USING IT AND",
                    "YOU CAN HAVE THIS.",
                    new String[]{"WHITE_SWORD"}, new int[]{0}, 5);

            case "12,0": // Magical Sword (requires 12+ hearts, push grave)
                return new CaveDef(CaveType.MAGICAL_SWORD,
                    "MASTER USING IT AND",
                    "YOU CAN HAVE THIS.",
                    new String[]{"MAGICAL_SWORD"}, new int[]{0}, 12);

            // ==================== Shop Type A (Shield, Key, Candle) ====================
            case "1,1":  // Mountain shop
            case "0,2":  // Lake area shop
            case "7,6":  // Plains shop
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"SHIELD", "KEY", "BLUE_CANDLE"},
                    new int[]{160, 100, 60}, 0);

            // ==================== Shop Type B (Bombs, Arrow, Candle) ====================
            case "9,1":  // Mountain shop
            case "12,7": // Starting area shop
            case "10,6": // Plains shop
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"BOMB", "ARROW", "BLUE_CANDLE"},
                    new int[]{20, 80, 60}, 0);

            // ==================== Shop Type C (Blue Ring, Food, Key) ====================
            case "5,5":  // Blue Ring shop
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"BLUE_RING", "FOOD", "KEY"},
                    new int[]{250, 100, 80}, 0);

            // ==================== Shop Type D (Food, Bombs, Key) ====================
            case "6,4":  // Forest shop
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"FOOD", "BOMB", "KEY"},
                    new int[]{100, 20, 80}, 0);

            // ==================== Letter Cave ====================
            case "3,2":
                return new CaveDef(CaveType.ITEM_CAVE,
                    "TAKE THIS TO THE OLD",
                    "WOMAN.",
                    new String[]{"LETTER"}, new int[]{0}, 0);

            // ==================== Potion Shop (requires Letter) ====================
            case "3,7":
                return new CaveDef(CaveType.POTION_SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"BLUE_POTION", "RED_POTION"},
                    new int[]{40, 68}, 0);

            // ==================== Take Any (Heart Container) Caves ====================
            case "12,4":
                return new CaveDef(CaveType.HEART_CONTAINER,
                    "TAKE ANY ROAD YOU",
                    "WANT.",
                    new String[]{"HEART_CONTAINER", "SECOND_POTION"},
                    new int[]{0, 68}, 0);

            case "8,2":
                return new CaveDef(CaveType.HEART_CONTAINER,
                    "TAKE ANY ROAD YOU",
                    "WANT.",
                    new String[]{"HEART_CONTAINER", "RED_POTION"},
                    new int[]{0, 68}, 0);

            // ==================== Money Making Game ====================
            case "4,6":
                return new CaveDef(CaveType.MONEY_MAKING_GAME,
                    "LET'S PLAY MONEY",
                    "MAKING GAME.",
                    new String[]{"-10", "-40", "50"},
                    new int[]{0, 0, 0}, 0);

            // ==================== Fairy Fountains ====================
            case "7,2":  // Lake fairy
            case "11,2": // East fairy
            case "3,5":  // West fairy (pond)
            case "6,5":  // Central fairy
            case "15,3": // Peninsula fairy
                return new CaveDef(CaveType.ITEM_CAVE,
                    "",
                    "",
                    new String[0], new int[0], 0);

            // ==================== Door Repair Charge ====================
            case "14,1":
                return new CaveDef(CaveType.SHOP,
                    "PAY ME FOR THE DOOR",
                    "REPAIR CHARGE.",
                    new String[]{"-20"},
                    new int[]{0}, 0);

            // ==================== Hint Caves ====================
            case "1,4":  // Peninsula hint
                return hint("EASTMOST PENINSULA", "IS THE SECRET.");

            case "8,6":  // Dodongo hint
                return hint("DODONGO DISLIKES", "SMOKE.");

            case "11,3": // Sword hint
                return hint("DID YOU GET THE SWORD", "FROM THE OLD MAN?");

            case "6,1":  // Tree hint
                return hint("SECRET IS IN THE TREE", "AT THE DEAD-END.");

            case "13,5": // Fairy hint
                return hint("THERE ARE SECRETS", "WHERE FAIRIES DON'T LIVE.");

            case "3,0":  // Triforce hint (Death Mountain)
                return hint("ONES WHO DOES NOT HAVE", "TRIFORCE CAN'T GO IN.");

            case "0,6":  // Grave hint
                return hint("MEET THE OLD MAN", "AT THE GRAVE.");

            case "15,7": // Spectacle Rock hint
                return hint("SPECTACLE ROCK IS AN", "ENTRANCE TO DEATH MTN.");

            case "8,4":  // Forest maze hint
                return hint("GO NORTH,WEST,SOUTH,", "WEST TO THE FOREST.");

            default:
                return null;
        }
    }

    /**
     * Returns a cave definition by its numeric ID (RoomData.CAVE_* constants).
     * Used when secrets (bomb walls, burn bushes, push rocks) reveal hidden caves.
     */
    public static CaveDef getCaveById(int caveId) {
        switch (caveId) {
            case RoomData.CAVE_SWORD:
                return new CaveDef(CaveType.ITEM_CAVE,
                    "IT'S DANGEROUS TO GO", "ALONE! TAKE THIS.",
                    new String[]{"WOODEN_SWORD"}, new int[]{0}, 0);
            case RoomData.CAVE_SHOP_A1: case RoomData.CAVE_SHOP_A2: case RoomData.CAVE_SHOP_A3:
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!", "",
                    new String[]{"SHIELD", "KEY", "BLUE_CANDLE"},
                    new int[]{160, 100, 60}, 0);
            case RoomData.CAVE_SHOP_B1: case RoomData.CAVE_SHOP_B2: case RoomData.CAVE_SHOP_B3:
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!", "",
                    new String[]{"BOMB", "ARROW", "BLUE_CANDLE"},
                    new int[]{20, 80, 60}, 0);
            case RoomData.CAVE_SHOP_C:
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!", "",
                    new String[]{"BLUE_RING", "FOOD", "KEY"},
                    new int[]{250, 100, 80}, 0);
            case RoomData.CAVE_SHOP_D:
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!", "",
                    new String[]{"FOOD", "BOMB", "KEY"},
                    new int[]{100, 20, 80}, 0);
            case RoomData.CAVE_WHITE_SWORD:
                return new CaveDef(CaveType.WHITE_SWORD,
                    "MASTER USING IT AND", "YOU CAN HAVE THIS.",
                    new String[]{"WHITE_SWORD"}, new int[]{0}, 5);
            case RoomData.CAVE_MAGICAL_SWORD:
                return new CaveDef(CaveType.MAGICAL_SWORD,
                    "MASTER USING IT AND", "YOU CAN HAVE THIS.",
                    new String[]{"MAGICAL_SWORD"}, new int[]{0}, 12);
            case RoomData.CAVE_POTION_SHOP:
                return new CaveDef(CaveType.POTION_SHOP,
                    "BUY SOMETHIN' WILL YA!", "",
                    new String[]{"BLUE_POTION", "RED_POTION"},
                    new int[]{40, 68}, 0);
            case RoomData.CAVE_MONEY_GAME:
                return new CaveDef(CaveType.MONEY_MAKING_GAME,
                    "LET'S PLAY MONEY", "MAKING GAME.",
                    new String[]{"-10", "-40", "50"}, new int[]{0, 0, 0}, 0);
            case RoomData.CAVE_LETTER:
                return new CaveDef(CaveType.ITEM_CAVE,
                    "TAKE THIS TO THE OLD", "WOMAN.",
                    new String[]{"LETTER"}, new int[]{0}, 0);
            case RoomData.CAVE_DOOR_REPAIR:
                return new CaveDef(CaveType.SHOP,
                    "PAY ME FOR THE DOOR", "REPAIR CHARGE.",
                    new String[]{"-20"}, new int[]{0}, 0);
            case RoomData.CAVE_TAKE_ANY_1: case RoomData.CAVE_TAKE_ANY_2:
                return new CaveDef(CaveType.HEART_CONTAINER,
                    "TAKE ANY ROAD YOU", "WANT.",
                    new String[]{"HEART_CONTAINER", "RED_POTION"},
                    new int[]{0, 68}, 0);
            case RoomData.CAVE_COAST_SECRET:
                return new CaveDef(CaveType.ITEM_CAVE,
                    "IT'S A SECRET TO", "EVERYBODY.",
                    new String[]{"30"}, new int[]{0}, 0); // 30 rupees
            case RoomData.CAVE_FAIRY_1: case RoomData.CAVE_FAIRY_2:
            case RoomData.CAVE_FAIRY_3: case RoomData.CAVE_FAIRY_4:
                return new CaveDef(CaveType.ITEM_CAVE, "", "",
                    new String[0], new int[0], 0);
            case RoomData.CAVE_HINT_PENINSULA:
                return hint("EASTMOST PENINSULA", "IS THE SECRET.");
            case RoomData.CAVE_HINT_DODONGO:
                return hint("DODONGO DISLIKES", "SMOKE.");
            case RoomData.CAVE_HINT_SWORD:
                return hint("DID YOU GET THE SWORD", "FROM THE OLD MAN?");
            case RoomData.CAVE_HINT_TREE:
                return hint("SECRET IS IN THE TREE", "AT THE DEAD-END.");
            case RoomData.CAVE_HINT_FAIRY:
                return hint("THERE ARE SECRETS", "WHERE FAIRIES DON'T LIVE.");
            case RoomData.CAVE_HINT_TRIFORCE:
                return hint("ONES WHO DOES NOT HAVE", "TRIFORCE CAN'T GO IN.");
            case RoomData.CAVE_HINT_GRAVE:
                return hint("MEET THE OLD MAN", "AT THE GRAVE.");
            case RoomData.CAVE_HINT_SPECTACLE:
                return hint("SPECTACLE ROCK IS AN", "ENTRANCE TO DEATH MTN.");
            case RoomData.CAVE_HINT_MAZE:
                return hint("GO NORTH,WEST,SOUTH,", "WEST TO THE FOREST.");
            default:
                return null;
        }
    }

    /** Helper to create hint caves. */
    private static CaveDef hint(String line1, String line2) {
        return new CaveDef(CaveType.HINT, line1, line2,
            new String[0], new int[0], 0);
    }
}
