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
            // === Starting sword cave (7,7) ===
            case "7,7":
                return new CaveDef(CaveType.ITEM_CAVE,
                    "IT'S DANGEROUS TO GO",
                    "ALONE! TAKE THIS.",
                    new String[]{"WOODEN_SWORD"}, new int[]{0}, 0);

            // === White Sword cave (5,2) — requires 5 hearts ===
            case "5,2":
                return new CaveDef(CaveType.WHITE_SWORD,
                    "MASTER USING IT AND",
                    "YOU CAN HAVE THIS.",
                    new String[]{"WHITE_SWORD"}, new int[]{0}, 5);

            // === Magical Sword cave (0,0) — requires 12 hearts ===
            case "0,0":
                return new CaveDef(CaveType.MAGICAL_SWORD,
                    "MASTER USING IT AND",
                    "YOU CAN HAVE THIS.",
                    new String[]{"MAGICAL_SWORD"}, new int[]{0}, 12);

            // === Blue Ring shop (5,5) ===
            case "5,5":
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"BLUE_RING", "KEY", "SHIELD"},
                    new int[]{250, 80, 130}, 0);

            // === Candle shop (7,3) ===
            case "7,3":
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"BLUE_CANDLE", "BOMB", "ARROW"},
                    new int[]{60, 20, 80}, 0);

            // === Letter cave (2,2) ===
            case "2,2":
                return new CaveDef(CaveType.ITEM_CAVE,
                    "TAKE THIS TO THE OLD",
                    "WOMAN.",
                    new String[]{"LETTER"}, new int[]{0}, 0);

            // === Heart Container caves ===
            case "12,4":
                return new CaveDef(CaveType.HEART_CONTAINER,
                    "TAKE ANY ROAD YOU",
                    "WANT.",
                    new String[]{"HEART_CONTAINER", "SECOND_POTION"},
                    new int[]{0, 68}, 0);

            case "10,1":
                return new CaveDef(CaveType.HEART_CONTAINER,
                    "TAKE ANY ROAD YOU",
                    "WANT.",
                    new String[]{"HEART_CONTAINER", "RED_POTION"},
                    new int[]{0, 68}, 0);

            // === Potion shop (3,3) — requires Letter ===
            case "3,3":
                return new CaveDef(CaveType.POTION_SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"BLUE_POTION", "RED_POTION"},
                    new int[]{40, 68}, 0);

            // === Bomb upgrade (9,1) ===
            case "9,1":
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"BOMB", "KEY", "BLUE_CANDLE"},
                    new int[]{20, 100, 60}, 0);

            // === Money making game (4,6) ===
            case "4,6":
                return new CaveDef(CaveType.MONEY_MAKING_GAME,
                    "LET'S PLAY MONEY",
                    "MAKING GAME.",
                    new String[]{"-10", "-40", "50"},
                    new int[]{0, 0, 0}, 0);

            // === Hint caves ===
            case "1,4":
                return new CaveDef(CaveType.HINT,
                    "EASTMOST PENINSULA",
                    "IS THE SECRET.",
                    new String[0], new int[0], 0);

            case "8,6":
                return new CaveDef(CaveType.HINT,
                    "DODONGO DISLIKES",
                    "SMOKE.",
                    new String[0], new int[0], 0);

            case "11,3":
                return new CaveDef(CaveType.HINT,
                    "DID YOU GET THE SWORD",
                    "FROM THE OLD MAN?",
                    new String[0], new int[0], 0);

            case "6,1":
                return new CaveDef(CaveType.HINT,
                    "SECRET IS IN THE TREE",
                    "AT THE DEAD-END.",
                    new String[0], new int[0], 0);

            case "13,5":
                return new CaveDef(CaveType.HINT,
                    "THERE ARE SECRETS",
                    "WHERE FAIRIES DON'T LIVE.",
                    new String[0], new int[0], 0);

            // === Arrow shop (12,7) ===
            case "12,7":
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"ARROW", "BOMB", "BLUE_CANDLE"},
                    new int[]{80, 20, 60}, 0);

            // === Food/Bait shop (6,4) ===
            case "6,4":
                return new CaveDef(CaveType.SHOP,
                    "BUY SOMETHIN' WILL YA!",
                    "",
                    new String[]{"FOOD", "BOMB", "KEY"},
                    new int[]{100, 20, 80}, 0);

            default:
                return null;
        }
    }
}
