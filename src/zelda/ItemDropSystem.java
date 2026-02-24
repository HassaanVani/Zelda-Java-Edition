package zelda;

/**
 * NES-accurate deterministic item drop system.
 * Derived from Z_04.asm SetUpDroppedItem routine (lines 11030-11200).
 *
 * The NES does NOT use random drops. Instead:
 * 1. Each enemy type belongs to a drop class (0-3)
 * 2. A global WorldKillCycle (0-9) increments with each kill
 * 3. Drop is looked up from a 4x10 table[class][killCycle]
 * 4. A per-class random threshold determines if the drop actually appears
 * 5. Every 16th kill guarantees a fairy
 * 6. After 10 consecutive kills without Link being hurt, guaranteed bomb/5-rupee
 */
public class ItemDropSystem {

    // NES item IDs mapped to ItemType
    // $00 = bomb, $0F = 5 rupees, $18 = rupee, $21 = heart, $22 = fairy, $23 = fairy
    private static final int ID_BOMB     = 0x00;
    private static final int ID_5RUPEES  = 0x0F;
    private static final int ID_RUPEE    = 0x18;
    private static final int ID_HEART    = 0x21;
    private static final int ID_FAIRY    = 0x22;
    private static final int ID_FAIRY2   = 0x23;

    // Drop table: 4 rows (classes) x 10 columns (kill cycle positions)
    // From Z_04.asm DropItemTable (lines 11063-11068)
    private static final int[][] DROP_TABLE = {
        // Class 0: easy enemies (Octorok red, Moblin red, Keese, Stalfos, Rope, Leever blue)
        { ID_FAIRY,  ID_RUPEE,  ID_FAIRY,  ID_RUPEE,  ID_FAIRY2, ID_RUPEE,
          ID_FAIRY,  ID_FAIRY,  ID_RUPEE,  ID_RUPEE },
        // Class 1: medium enemies (Armos, Ghini, Darknut red, Goriya red, Gel, Zola, Lynel red, LikeLike, Peahat)
        { ID_5RUPEES, ID_RUPEE, ID_FAIRY,  ID_RUPEE,  ID_5RUPEES, ID_FAIRY,
          ID_HEART,  ID_RUPEE,  ID_RUPEE,  ID_RUPEE },
        // Class 2: hard enemies (Gibdo, Darknut blue, Wizzrobe blue, Vire, Goriya blue, etc.)
        { ID_FAIRY,  ID_BOMB,   ID_RUPEE,  ID_HEART,  ID_RUPEE,  ID_FAIRY,
          ID_BOMB,   ID_RUPEE,  ID_BOMB,   ID_FAIRY },
        // Class 3: bosses and everything else
        { ID_FAIRY,  ID_FAIRY,  ID_FAIRY,  ID_FAIRY2, ID_RUPEE,  ID_FAIRY,
          ID_FAIRY2, ID_FAIRY,  ID_FAIRY,  ID_RUPEE }
    };

    // Per-class random thresholds (out of 256) — higher = more likely to drop
    // From Z_04.asm DropItemRates: $50, $98, $68, $68
    private static final int[] DROP_RATES = { 0x50, 0x98, 0x68, 0x68 };

    // Global state
    private int worldKillCycle = 0;    // 0-9, cycles through drop table columns
    private int worldKillCount = 0;    // total kills since last reset, fairy at $10 (16)
    private int helpDropCount = 0;     // consecutive kills without Link being hurt
    private int helpDropValue = 0;     // 0 = 5rupees, other = bomb for guaranteed drop

    public ItemDropSystem() {}

    /**
     * Called when an enemy is killed. Returns the ItemType to drop, or null if no drop.
     * @param dropClass the enemy's drop class (0-3), or -1 for no drops
     */
    public Item.ItemType onEnemyKilled(int dropClass) {
        // Increment global kill counters
        worldKillCycle = (worldKillCycle + 1) % 10;
        worldKillCount++;
        helpDropCount++;

        // No drops for class -1
        if (dropClass < 0 || dropClass > 3) return null;

        // Look up item from drop table
        int itemId = DROP_TABLE[dropClass][worldKillCycle];

        // Every 16th kill guarantees a fairy
        if (worldKillCount >= 16) {
            worldKillCount = 0;
            return Item.ItemType.FAIRY;
        }

        // After 10 consecutive kills without being hurt, guaranteed help drop
        if (helpDropCount >= 10) {
            Item.ItemType helpItem;
            if (helpDropValue == 0) {
                helpItem = Item.ItemType.FIVE_RUPEES;
            } else {
                helpItem = Item.ItemType.BOMB_DROP;
            }
            helpDropCount = 0;
            helpDropValue = 0;
            return helpItem;
        }

        // Random chance to cancel the drop based on class rate
        int randomVal = (int)(Math.random() * 256);
        if (randomVal >= DROP_RATES[dropClass]) {
            return null; // drop canceled
        }

        // Convert NES item ID to Java ItemType
        return nesIdToItemType(itemId);
    }

    /**
     * Called when Link takes damage. Resets the help drop counters.
     * From Z_01.asm Link_BeHarmed: resets WorldKillCount, HelpDropCount, HelpDropValue
     */
    public void onLinkDamaged() {
        worldKillCount = 0;
        helpDropCount = 0;
        helpDropValue = 0;
    }

    /**
     * Converts NES item ID to Java ItemType.
     */
    private Item.ItemType nesIdToItemType(int nesId) {
        switch (nesId) {
            case ID_BOMB:    return Item.ItemType.BOMB_DROP;
            case ID_5RUPEES: return Item.ItemType.FIVE_RUPEES;
            case ID_RUPEE:   return Item.ItemType.RUPEE;
            case ID_HEART:   return Item.ItemType.HEART;
            case ID_FAIRY:
            case ID_FAIRY2:  return Item.ItemType.FAIRY;
            default:         return Item.ItemType.RUPEE;
        }
    }

}
