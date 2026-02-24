package zelda;

/**
 * Defines static data for all 9 dungeon layouts (1st Quest).
 * Each dungeon has a unique NES-accurate shape with proper room connections,
 * enemy types, items, and special features.
 *
 * Door types: 0=NONE, 1=OPEN, 2=LOCKED, 3=BOSS_LOCKED, 4=BOMBABLE, 5=BOMBED, 6=SHUTTER
 */
public class DungeonData {

    /** Defines a single room in a dungeon. */
    public static class DungeonRoomDef {
        public final int localX, localY;
        public final int mapCol, mapRow;
        public final String[] enemies;
        public final int[] doors;            // [N, W, S, E]
        public final String itemType;
        public final String bossType;
        public final boolean isDark;
        public final boolean hasBlock;
        public final int blockPushDir;       // -1=any, 0=N, 1=W, 2=S, 3=E
        public final boolean isStairway;
        public final int stairTargetX;
        public final int stairTargetY;
        public String oldManType = null;     // null, "DOOR_REPAIR", "GRUMBLE", "MONEY_OR_LIFE", "HINT"
        public String oldManText = null;
        public int oldManCost = 0;

        public DungeonRoomDef(int localX, int localY, int mapCol, int mapRow,
                              String[] enemies, int[] doors, String itemType,
                              String bossType, boolean isDark, boolean hasBlock,
                              int blockPushDir,
                              boolean isStairway, int stairTargetX, int stairTargetY) {
            this.localX = localX;
            this.localY = localY;
            this.mapCol = mapCol;
            this.mapRow = mapRow;
            this.enemies = enemies;
            this.doors = doors;
            this.itemType = itemType;
            this.bossType = bossType;
            this.isDark = isDark;
            this.hasBlock = hasBlock;
            this.blockPushDir = blockPushDir;
            this.isStairway = isStairway;
            this.stairTargetX = stairTargetX;
            this.stairTargetY = stairTargetY;
        }

        /** Set Old Man NPC for this room. Returns self for chaining. */
        public DungeonRoomDef withOldMan(String type, String text, int cost) {
            this.oldManType = type;
            this.oldManText = text;
            this.oldManCost = cost;
            return this;
        }
    }

    /** Complete definition of a dungeon. */
    public static class DungeonDef {
        public final int number;
        public final String name;
        public final int entranceX, entranceY;
        public final int mapWidth, mapHeight;
        public final DungeonRoomDef[] rooms;
        public final String dungeonItem;
        public final boolean[][] minimapMask;

        public DungeonDef(int number, String name, int entranceX, int entranceY,
                          int mapWidth, int mapHeight, DungeonRoomDef[] rooms, String dungeonItem) {
            this(number, name, entranceX, entranceY, mapWidth, mapHeight, rooms, dungeonItem, null);
        }

        public DungeonDef(int number, String name, int entranceX, int entranceY,
                          int mapWidth, int mapHeight, DungeonRoomDef[] rooms,
                          String dungeonItem, boolean[][] minimapMask) {
            this.number = number;
            this.name = name;
            this.entranceX = entranceX;
            this.entranceY = entranceY;
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;
            this.rooms = rooms;
            this.dungeonItem = dungeonItem;
            this.minimapMask = (minimapMask != null) ? minimapMask : buildMask(rooms, mapWidth, mapHeight);
        }
    }

    // Auto-generate minimap mask from room positions
    private static boolean[][] buildMask(DungeonRoomDef[] rooms, int w, int h) {
        boolean[][] mask = new boolean[w][h];
        for (DungeonRoomDef r : rooms) {
            if (r.localX >= 0 && r.localX < w && r.localY >= 0 && r.localY < h) {
                mask[r.localX][r.localY] = true;
            }
        }
        return mask;
    }

    // ===== Door state constants =====
    private static final int NONE = 0;
    private static final int OPEN = 1;
    private static final int LOCKED = 2;
    private static final int BOSS = 3;
    private static final int BOMBABLE = 4;
    private static final int BOMBED = 5;
    private static final int SHUTTER = 6;

    // ===== Helpers =====
    private static int[] doors(int n, int w, int s, int e) { return new int[]{n, w, s, e}; }
    private static String[] e(String... types) { return types; }
    private static final String[] EMPTY = new String[0];

    // ===== Per-room NES atlas tile mapping =====
    // The atlas (zelda-dungeons.png) is a 16x16 grid representing the NES underworld:
    //   Rows 0-7  = UW Block 1 (Levels 1-6 share this grid)
    //   Rows 8-15 = UW Block 2 (Levels 7-9 share this grid)
    // NES Y-axis: row 0 = top of dungeon (triforce end), row 7 = bottom (entrance end)
    // All NES dungeon entrances are at row 7 of their block.
    //
    // Each room gets its own unique atlas tile based on its NES grid position:
    //   atlas_col = BASE_COL[level] + localX
    //   atlas_row = BLOCK_ROW_OFFSET[level] + 7 - localY
    //
    // BASE_COL: column offset converting localX=0 to the leftmost NES column for that dungeon
    // BLOCK_ROW_OFFSET: 0 for Block 1 (levels 1-6), 8 for Block 2 (levels 7-9)
    //
    // Determined by matching DungeonData room shapes to colored tile clusters in the atlas:
    //   Level 1 (Eagle):  Teal cluster,     cols 1-6,  rows 2-7  → base_col=0
    //   Level 2 (Moon):   Blue cluster,     cols 12-15,rows 0-7  → base_col=11
    //   Level 3 (Manji):  MedGreen cluster, cols 9-13, rows 2-7  → base_col=9
    //   Level 4 (Snake):  Gold+Brown cluster,cols 0-3, rows 0-7  → base_col=0
    //   Level 5 (Lizard): Green+DkGreen,    cols 4-7,  rows 0-7  → base_col=3
    //   Level 6 (Dragon): Gold cluster B,   cols 8-13, rows 0-7  → base_col=7
    //   Level 7 (Demon):  Green cluster,    cols 0-6,  rows 8-15 → base_col=0
    //   Level 8 (Lion):   Mixed cluster,    cols 1-5,  rows 8-15 → base_col=0
    //   Level 9 (Skull):  Gray cluster,     cols 0-7,  rows 8-15 → base_col=0

    // Current dungeon level — set at start of each buildLevel*() method
    private static int currentLevel = 1;

    //                                     unused, L1, L2, L3, L4, L5, L6, L7, L8, L9
    private static final int[] BASE_COL =       {0,  0, 11,  9,  0,  3,  7,  0,  0,  0};
    private static final int[] BLOCK_ROW_OFF =  {0,  0,  0,  0,  0,  0,  0,  8,  8,  8};

    /** Compute the atlas tile {col, row} for a room at (localX, localY) in the current dungeon. */
    private static int[] computeAtlasTile(int localX, int localY) {
        int col = BASE_COL[currentLevel] + localX;
        int row = BLOCK_ROW_OFF[currentLevel] + 7 - localY;
        // Clamp to valid atlas range [0,15]
        col = Math.min(Math.max(col, 0), 15);
        row = Math.min(Math.max(row, 0), 15);
        return new int[]{col, row};
    }

    /** Basic room */
    private static DungeonRoomDef r(int lx, int ly, int mc, int mr,
                                     String[] enemies, int[] doors, String item) {
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], enemies, doors, item, null, false, false, -1, false, -1, -1);
    }
    /** Entrance room — south passage always open for player entry/exit */
    private static DungeonRoomDef entr(int lx, int ly, int mc, int mr, int[] doors) {
        doors[2] = OPEN; // South passage open (entrance/exit)
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], EMPTY, doors, null, null, false, false, -1, false, -1, -1);
    }
    /** Boss room */
    private static DungeonRoomDef boss(int lx, int ly, int mc, int mr,
                                        int[] doors, String bossType) {
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], EMPTY, doors, null, bossType, false, false, -1, false, -1, -1);
    }
    /** Dark room */
    private static DungeonRoomDef dark(int lx, int ly, int mc, int mr,
                                        String[] enemies, int[] doors, String item) {
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], enemies, doors, item, null, true, false, -1, false, -1, -1);
    }
    /** Room with pushable block */
    private static DungeonRoomDef block(int lx, int ly, int mc, int mr,
                                         String[] enemies, int[] doors, String item, int pushDir) {
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], enemies, doors, item, null, false, true, pushDir, false, -1, -1);
    }
    /** Room with stairway (block push reveals stairs) */
    private static DungeonRoomDef stair(int lx, int ly, int mc, int mr,
                                         String[] enemies, int[] doors, String item,
                                         int pushDir, int stx, int sty) {
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], enemies, doors, item, null, false, true, pushDir, true, stx, sty);
    }
    /** Triforce room */
    private static DungeonRoomDef triforce(int lx, int ly, int mc, int mr) {
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], EMPTY, doors(NONE, NONE, OPEN, NONE),
            "TRIFORCE", null, false, false, -1, false, -1, -1);
    }
    /** Zelda room (D9 only) */
    private static DungeonRoomDef zelda(int lx, int ly, int mc, int mr) {
        int[] at = computeAtlasTile(lx, ly);
        return new DungeonRoomDef(lx, ly, at[0], at[1], EMPTY, doors(NONE, NONE, OPEN, NONE),
            "ZELDA", null, false, false, -1, false, -1, -1);
    }

    // Old Man room types
    public static final String OLDMAN_DOOR_REPAIR = "DOOR_REPAIR";
    public static final String OLDMAN_GRUMBLE = "GRUMBLE";
    public static final String OLDMAN_MONEY_OR_LIFE = "MONEY_OR_LIFE";
    public static final String OLDMAN_HINT = "HINT";

    public static DungeonDef getDungeon(int level) {
        switch (level) {
            case 1: return buildLevel1();
            case 2: return buildLevel2();
            case 3: return buildLevel3();
            case 4: return buildLevel4();
            case 5: return buildLevel5();
            case 6: return buildLevel6();
            case 7: return buildLevel7();
            case 8: return buildLevel8();
            case 9: return buildLevel9();
            default: return buildLevel1();
        }
    }

    // ==================== LEVEL 1: Eagle ====================
    // Shape: eagle/bird with two wing rows and a central spine
    //                    T(3,5)
    //                    B(3,4)
    // (0,3)(1,3)(2,3)(3,3)(4,3)(5,3)    <- upper wings
    //                    (3,2)
    // (0,1)(1,1)(2,1)(3,1)(4,1)(5,1)    <- lower wings
    //                    E(3,0)
    // Enemies: Stalfos, Keese, Gel, GoriyaRed, Wallmaster
    // Boss: Aquamentus | Items: Bow, Boomerang
    private static DungeonDef buildLevel1() {
        currentLevel = 1;
        DungeonRoomDef[] rooms = {
            // Entrance
            entr(3, 0, 3, 0, doors(OPEN, NONE, NONE, NONE)),

            // Lower wing row (y=1)
            r(0, 1, 0, 1, e("Stalfos", "Stalfos", "Stalfos"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(1, 1, 1, 1, e("KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue"),
                doors(NONE, OPEN, NONE, OPEN), null),
            r(2, 1, 2, 1, e("KeeseBlue", "KeeseBlue", "KeeseBlue"),
                doors(NONE, OPEN, NONE, OPEN), "KEY"),
            r(3, 1, 3, 1, EMPTY,
                doors(LOCKED, OPEN, OPEN, OPEN), null),
            r(4, 1, 4, 1, e("Stalfos", "Stalfos", "Stalfos", "Stalfos", "Stalfos"),
                doors(NONE, OPEN, NONE, OPEN), "KEY"),
            r(5, 1, 5, 1, e("KeeseBlue", "KeeseBlue", "KeeseBlue"),
                doors(NONE, OPEN, NONE, NONE), "MAP"),

            // Spine (y=2)
            r(3, 2, 3, 2, e("Stalfos", "Stalfos", "Stalfos"),
                doors(OPEN, NONE, OPEN, NONE), null),

            // Upper wing row (y=3)
            stair(0, 3, 0, 3, e("Trap", "Trap", "Trap", "Trap"),
                doors(NONE, NONE, NONE, OPEN), null, 1, 0, 4),
            r(1, 3, 1, 3, EMPTY,
                doors(NONE, OPEN, NONE, OPEN), null)
                .withOldMan(OLDMAN_HINT, "EASTMOST PENNINSULA IS THE SECRET", 0),
            r(2, 3, 2, 3, e("GoriyaRed", "GoriyaRed", "GoriyaRed"),
                doors(NONE, OPEN, NONE, OPEN), "BOOMERANG"),
            r(3, 3, 3, 3, e("GoriyaRed", "GoriyaRed", "GoriyaRed"),
                doors(BOSS, OPEN, OPEN, OPEN), "BOSS_KEY"),
            r(4, 3, 4, 3, e("KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue"),
                doors(NONE, OPEN, NONE, OPEN), "COMPASS"),
            r(5, 3, 5, 3, e("Wallmaster", "Wallmaster"),
                doors(NONE, OPEN, NONE, NONE), "KEY"),

            // Boss room
            boss(3, 4, 3, 4, doors(OPEN, NONE, OPEN, NONE), "Aquamentus"),
            // Triforce
            triforce(3, 5, 3, 5),

            // Underground BOW room (accessed via stairway from 0,3)
            stair(0, 4, 0, 4, e("KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue"),
                doors(NONE, NONE, NONE, NONE), "BOW", 0, 0, 3),
        };
        return new DungeonDef(1, "EAGLE", 3, 0, 6, 7, rooms, "BOW");
    }

    // ==================== LEVEL 2: Moon ====================
    // Shape: crescent/moon arc
    // (0,3)(1,3)               (4,3)(5,3)
    // (0,2)                         (5,2)
    // (0,1)(1,1)(2,1)(3,1)(4,1)(5,1)
    //                E(2,0)
    // Enemies: Rope, GoriyaRed/Blue, Zol, Gel, Moldorm
    // Boss: Dodongo | Item: Magical Boomerang
    private static DungeonDef buildLevel2() {
        currentLevel = 2;
        DungeonRoomDef[] rooms = {
            // Entrance
            entr(2, 0, 2, 0, doors(OPEN, NONE, NONE, NONE)),

            // Base arc (y=1)
            r(0, 1, 0, 1, e("Rope", "Rope", "Rope"),
                doors(OPEN, NONE, NONE, OPEN), "KEY"),
            r(1, 1, 1, 1, e("GoriyaRed", "GoriyaRed"),
                doors(NONE, OPEN, NONE, OPEN), null),
            block(2, 1, 2, 1, e("Zol", "Zol", "Zol"),
                doors(NONE, OPEN, OPEN, OPEN), null, 2),
            r(3, 1, 3, 1, e("Rope", "Rope", "Rope"),
                doors(SHUTTER, OPEN, NONE, OPEN), "COMPASS"),
            r(4, 1, 4, 1, e("GoriyaRed", "GoriyaRed", "GoriyaRed"),
                doors(NONE, OPEN, NONE, OPEN), "MAP"),
            r(5, 1, 5, 1, e("Zol", "Zol", "Gel", "Gel"),
                doors(OPEN, OPEN, NONE, NONE), "KEY"),

            // Left pillar (y=2)
            r(0, 2, 0, 2, e("Rope", "Rope"),
                doors(OPEN, NONE, OPEN, NONE), null),
            // Right pillar (y=2)
            r(5, 2, 5, 2, e("GoriyaRed", "GoriyaRed", "Rope"),
                doors(OPEN, NONE, OPEN, NONE), "KEY"),

            // Top arc (y=3) — boss area
            r(0, 3, 0, 3, e("GoriyaRed", "GoriyaRed"),
                doors(NONE, NONE, OPEN, OPEN), "MAGICAL_BOOMERANG"),
            r(1, 3, 1, 3, e("Rope", "Rope", "Rope"),
                doors(BOSS, OPEN, NONE, NONE), "BOSS_KEY"),
            // Boss & Triforce at top right
            boss(4, 3, 4, 3, doors(OPEN, NONE, NONE, OPEN), "Dodongo"),
            r(5, 3, 5, 3, e("Zol", "Zol", "Zol", "Gel", "Gel"),
                doors(NONE, OPEN, OPEN, NONE), null),

            // Triforce above boss area  — connect to boss room north
            triforce(1, 4, 1, 4),
        };
        return new DungeonDef(2, "MOON", 2, 0, 6, 5, rooms, "MAGICAL_BOOMERANG");
    }

    // ==================== LEVEL 3: Manji ====================
    // Shape: manji/swastika pattern with central hub and 4 arms
    //           (2,4)
    // (0,3)(1,3)(2,3)(3,3)(4,3)
    //           (2,2)
    // (0,1)(1,1)(2,1)(3,1)(4,1)
    //           E(2,0)
    // Enemies: DarknutRed (first appearance), Zol, Keese
    // Boss: Manhandla | Item: Raft
    private static DungeonDef buildLevel3() {
        currentLevel = 3;
        DungeonRoomDef[] rooms = {
            // Entrance
            entr(2, 0, 2, 0, doors(OPEN, NONE, NONE, NONE)),

            // Lower arm row (y=1)
            dark(0, 1, 0, 1, e("KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(1, 1, 1, 1, e("Zol", "Zol", "Zol"),
                doors(NONE, OPEN, NONE, OPEN), null),
            block(2, 1, 2, 1, e("DarknutRed", "DarknutRed", "DarknutRed"),
                doors(SHUTTER, OPEN, OPEN, OPEN), null, 0),
            r(3, 1, 3, 1, e("DarknutRed", "DarknutRed"),
                doors(NONE, OPEN, NONE, OPEN), "COMPASS"),
            r(4, 1, 4, 1, e("KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue"),
                doors(NONE, OPEN, NONE, NONE), "MAP"),

            // Center (y=2)
            r(2, 2, 2, 2, e("DarknutRed", "DarknutRed"),
                doors(OPEN, NONE, OPEN, NONE), "KEY"),

            // Upper arm row (y=3) — stairway and boss path
            r(0, 3, 0, 3, e("Zol", "Zol", "Gel", "Gel"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            stair(1, 3, 1, 3, e("DarknutRed", "DarknutRed"),
                doors(NONE, OPEN, NONE, OPEN), null, 3, 3, 3),
            r(2, 3, 2, 3, e("DarknutRed", "DarknutRed", "DarknutRed"),
                doors(BOSS, OPEN, OPEN, OPEN), "BOSS_KEY"),
            r(3, 3, 3, 3, e("Zol", "Zol", "Zol", "Zol"),
                doors(SHUTTER, OPEN, NONE, OPEN), "RAFT"),
            r(4, 3, 4, 3, e("DarknutRed", "DarknutRed"),
                doors(NONE, OPEN, NONE, NONE), "KEY"),

            // Boss column (y=4)
            boss(2, 4, 2, 4, doors(OPEN, NONE, OPEN, NONE), "Manhandla"),
            triforce(3, 4, 3, 4),
        };
        return new DungeonDef(3, "MANJI", 2, 0, 5, 5, rooms, "RAFT");
    }

    // ==================== LEVEL 4: Snake ====================
    // Shape: S-shaped winding path
    // (0,4)(1,4)(2,4)              <- boss area
    //           (2,3)
    //      (1,2)(2,2)(3,2)
    //      (1,1)
    // (0,0)(1,0)                   <- entrance area
    // Enemies: Vire, LikeLike, DarknutRed/Blue, Keese
    // Boss: Gleeok (2 heads) | Item: Stepladder
    private static DungeonDef buildLevel4() {
        currentLevel = 4;
        DungeonRoomDef[] rooms = {
            // Entrance row (y=0)
            r(0, 0, 0, 0, e("Vire", "Vire", "KeeseRed", "KeeseRed"),
                doors(NONE, NONE, NONE, OPEN), "MAP"),
            entr(1, 0, 1, 0, doors(OPEN, OPEN, NONE, NONE)),

            // First turn (y=1)
            r(1, 1, 1, 1, e("Vire", "Vire", "Vire"),
                doors(SHUTTER, NONE, OPEN, NONE), "KEY"),

            // Middle row (y=2)
            block(1, 2, 1, 2, e("DarknutRed", "DarknutRed"),
                doors(NONE, NONE, OPEN, OPEN), "COMPASS", 3),
            r(2, 2, 2, 2, e("LikeLike", "LikeLike", "Vire"),
                doors(SHUTTER, OPEN, NONE, OPEN), "KEY"),
            dark(3, 2, 3, 2, e("KeeseRed", "KeeseRed", "KeeseRed", "KeeseRed", "KeeseRed"),
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY"),

            // Second turn (y=3)
            r(2, 3, 2, 3, e("DarknutBlue", "DarknutBlue"),
                doors(BOSS, NONE, OPEN, NONE), "STEPLADDER"),

            // Boss row (y=4)
            r(0, 4, 0, 4, e("Vire", "Vire", "KeeseRed", "KeeseRed"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            boss(1, 4, 1, 4, doors(OPEN, OPEN, NONE, OPEN), "Gleeok2"),
            triforce(2, 4, 2, 4),
        };
        return new DungeonDef(4, "SNAKE", 1, 0, 4, 5, rooms, "STEPLADDER");
    }

    // ==================== LEVEL 5: Lizard ====================
    // Shape: lizard with legs and tail
    //                T(3,5)
    //                B(3,4)
    // (1,3)(2,3)(3,3)(4,3)(5,3)     <- body
    //                (3,2)
    // (1,1)(2,1)(3,1)(4,1)(5,1)     <- lower body
    //      (2,0)          (4,0)     <- legs
    //           E(3,0)              <- tail/entrance
    // Enemies: PolsVoice, Gibdo, DarknutBlue, Zol
    // Boss: Digdogger | Item: Recorder
    private static DungeonDef buildLevel5() {
        currentLevel = 5;
        DungeonRoomDef[] rooms = {
            // Entrance & legs (y=0)
            r(2, 0, 2, 0, e("Zol", "Zol", "Zol"),
                doors(OPEN, NONE, NONE, OPEN), "KEY"),
            entr(3, 0, 3, 0, doors(OPEN, OPEN, NONE, OPEN)),
            r(4, 0, 4, 0, e("Gibdo", "Gibdo"),
                doors(OPEN, OPEN, NONE, NONE), "KEY"),

            // Lower body (y=1)
            r(1, 1, 1, 1, e("Gibdo", "Gibdo"),
                doors(NONE, NONE, NONE, OPEN), "MAP"),
            block(2, 1, 2, 1, e("DarknutBlue", "DarknutBlue"),
                doors(NONE, OPEN, OPEN, OPEN), null, 0),
            r(3, 1, 3, 1, e("PolsVoice", "PolsVoice", "KeeseBlue", "KeeseBlue"),
                doors(SHUTTER, OPEN, OPEN, OPEN), "COMPASS"),
            r(4, 1, 4, 1, e("Gibdo", "Gibdo", "Gibdo"),
                doors(NONE, OPEN, OPEN, OPEN), "KEY"),
            r(5, 1, 5, 1, e("Zol", "Zol", "Zol", "Zol"),
                doors(NONE, OPEN, NONE, NONE), null),

            // Spine (y=2)
            r(3, 2, 3, 2, e("DarknutBlue", "DarknutBlue"),
                doors(LOCKED, NONE, OPEN, NONE), null),

            // Upper body (y=3)
            dark(1, 3, 1, 3, e("PolsVoice", "PolsVoice"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(2, 3, 2, 3, e("Gibdo", "Gibdo", "Gibdo"),
                doors(NONE, OPEN, NONE, OPEN), null),
            r(3, 3, 3, 3, e("DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(BOSS, OPEN, OPEN, OPEN), "BOSS_KEY"),
            r(4, 3, 4, 3, e("PolsVoice", "PolsVoice", "PolsVoice"),
                doors(SHUTTER, OPEN, NONE, OPEN), "RECORDER"),
            r(5, 3, 5, 3, e("Gibdo", "Gibdo"),
                doors(NONE, OPEN, NONE, NONE), "KEY"),

            // Boss (y=4)
            boss(3, 4, 3, 4, doors(OPEN, NONE, OPEN, NONE), "Digdogger"),
            triforce(3, 5, 3, 5),
        };
        return new DungeonDef(5, "LIZARD", 3, 0, 6, 6, rooms, "RECORDER");
    }

    // ==================== LEVEL 6: Dragon ====================
    // Shape: dragon with head, body, and tail
    // (0,4)(1,4)(2,4)                   <- tail
    //           (2,3)(3,3)              <- body bend
    //      (1,2)(2,2)                   <- body
    // (0,1)     (2,1)(3,1)(4,1)         <- body + legs
    //      (1,0)(2,0)                   <- head/entrance
    // Enemies: WizzrobeRed/Blue (first appearance), LikeLike, Bubble
    // Boss: Gohma (Red) | Item: Magical Rod
    private static DungeonDef buildLevel6() {
        currentLevel = 6;
        DungeonRoomDef[] rooms = {
            // Entrance/head (y=0)
            r(1, 0, 1, 0, e("WizzrobeBlue", "WizzrobeBlue"),
                doors(OPEN, NONE, NONE, OPEN), "KEY"),
            entr(2, 0, 2, 0, doors(OPEN, OPEN, NONE, NONE)),

            // Body (y=1)
            dark(0, 1, 0, 1, e("Bubble", "Bubble", "LikeLike"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(2, 1, 2, 1, e("WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed"),
                doors(SHUTTER, NONE, OPEN, OPEN), "COMPASS"),
            r(3, 1, 3, 1, e("LikeLike", "LikeLike"),
                doors(NONE, OPEN, NONE, OPEN), "KEY"),
            r(4, 1, 4, 1, e("WizzrobeRed", "WizzrobeRed"),
                doors(NONE, OPEN, NONE, NONE), "MAP"),

            // Body middle (y=2)
            block(1, 2, 1, 2, e("WizzrobeRed", "WizzrobeRed"),
                doors(NONE, NONE, NONE, OPEN), "KEY", 3),
            r(2, 2, 2, 2, e("WizzrobeBlue", "WizzrobeBlue"),
                doors(LOCKED, OPEN, OPEN, NONE), "MAGICAL_ROD"),

            // Upper body (y=3)
            r(2, 3, 2, 3, e("LikeLike", "LikeLike", "WizzrobeRed"),
                doors(BOSS, NONE, OPEN, OPEN), "BOSS_KEY"),
            r(3, 3, 3, 3, e("WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed"),
                doors(NONE, OPEN, NONE, NONE), "KEY"),

            // Tail (y=4) — boss area
            r(0, 4, 0, 4, e("WizzrobeRed", "WizzrobeRed", "LikeLike"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            boss(1, 4, 1, 4, doors(OPEN, OPEN, NONE, OPEN), "GohmaRed"),
            triforce(2, 4, 2, 4),
        };
        return new DungeonDef(6, "DRAGON", 2, 0, 5, 5, rooms, "MAGICAL_ROD");
    }

    // ==================== LEVEL 7: Demon ====================
    // Shape: demon/skull face
    // (0,4)(1,4)(2,4)(3,4)(4,4)    <- forehead
    //      (1,3)          (3,3)    <- eyes
    //           (2,2)              <- nose
    //      (1,1)     (3,1)         <- mouth
    //           E(2,0)             <- chin/entrance
    // Enemies: GoriyaBlue, DarknutBlue, Wallmaster, Stalfos, Moldorm
    // Boss: Aquamentus | Item: Red Candle
    // Note: "Grumble Grumble" Goriya blocks a passage (feed Food)
    private static DungeonDef buildLevel7() {
        currentLevel = 7;
        DungeonRoomDef[] rooms = {
            // Entrance/chin (y=0)
            entr(2, 0, 2, 0, doors(OPEN, NONE, NONE, NONE)),

            // Mouth (y=1) — two rooms with gap
            r(1, 1, 1, 1, e("GoriyaBlue", "GoriyaBlue", "Stalfos"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(2, 1, 2, 1, e("DarknutBlue", "DarknutBlue"),
                doors(SHUTTER, OPEN, OPEN, OPEN), null),
            r(3, 1, 3, 1, e("Wallmaster", "Wallmaster"),
                doors(NONE, OPEN, NONE, NONE), "MAP"),

            // Nose (y=2)
            block(2, 2, 2, 2, e("DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(OPEN, NONE, OPEN, NONE), "COMPASS", 0),

            // Eyes (y=3) — left eye has grumble goriya, right has Red Candle
            r(1, 3, 1, 3, EMPTY,
                doors(OPEN, NONE, NONE, NONE), "BOSS_KEY")
                .withOldMan(OLDMAN_GRUMBLE, "GRUMBLE,GRUMBLE...", 0),
            r(3, 3, 3, 3, e("GoriyaBlue", "GoriyaBlue"),
                doors(OPEN, NONE, NONE, NONE), "RED_CANDLE"),

            // Forehead (y=4) — boss area
            dark(0, 4, 0, 4, e("GoriyaBlue", "GoriyaBlue", "GoriyaBlue"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(1, 4, 1, 4, e("DarknutBlue", "DarknutBlue"),
                doors(NONE, OPEN, OPEN, OPEN), null),
            r(2, 4, 2, 4, e("Wallmaster", "Wallmaster", "DarknutBlue"),
                doors(BOSS, OPEN, OPEN, OPEN), "KEY"),
            r(3, 4, 3, 4, e("GoriyaBlue", "GoriyaBlue"),
                doors(NONE, OPEN, OPEN, OPEN), "KEY"),
            r(4, 4, 4, 4, e("Stalfos", "Stalfos", "Stalfos", "Stalfos"),
                doors(NONE, OPEN, NONE, NONE), "KEY"),

            // Boss above forehead
            boss(2, 5, 2, 5, doors(OPEN, NONE, OPEN, NONE), "Aquamentus"),
            triforce(2, 6, 2, 6),
        };
        return new DungeonDef(7, "DEMON", 2, 0, 5, 7, rooms, "RED_CANDLE");
    }

    // ==================== LEVEL 8: Lion ====================
    // Shape: lion with mane and body
    // (0,5)(1,5)(2,5)(3,5)(4,5)    <- mane
    //           (2,4)              <- neck
    // (0,3)(1,3)(2,3)(3,3)(4,3)    <- body
    //           (2,2)              <- belly
    // (0,1)(1,1)(2,1)(3,1)(4,1)    <- legs
    //           E(2,0)             <- tail/entrance
    // Enemies: DarknutBlue, WizzrobeBlue, Gibdo, PolsVoice, LanmolaRed
    // Boss: Gleeok (4 heads) | Items: Magical Key, Book
    private static DungeonDef buildLevel8() {
        currentLevel = 8;
        DungeonRoomDef[] rooms = {
            // Entrance (y=0)
            entr(2, 0, 2, 0, doors(OPEN, NONE, NONE, NONE)),

            // Legs (y=1)
            r(0, 1, 0, 1, e("DarknutBlue", "DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(1, 1, 1, 1, e("Gibdo", "Gibdo", "Gibdo"),
                doors(NONE, OPEN, NONE, OPEN), null),
            block(2, 1, 2, 1, e("WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed"),
                doors(SHUTTER, OPEN, OPEN, OPEN), null, 0),
            r(3, 1, 3, 1, e("PolsVoice", "PolsVoice", "Gibdo"),
                doors(NONE, OPEN, NONE, OPEN), "MAP"),
            dark(4, 1, 4, 1, e("DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(NONE, OPEN, NONE, NONE), "KEY"),

            // Belly (y=2)
            r(2, 2, 2, 2, e("WizzrobeRed", "WizzrobeRed", "WizzrobeRed"),
                doors(LOCKED, NONE, OPEN, NONE), "COMPASS"),

            // Body (y=3) — first room is door repair old man
            r(0, 3, 0, 3, EMPTY,
                doors(NONE, NONE, NONE, OPEN), null)
                .withOldMan(OLDMAN_DOOR_REPAIR, "PAY ME FOR THE DOOR REPAIR CHARGE", 20),
            r(1, 3, 1, 3, e("DarknutBlue", "DarknutBlue", "Gibdo"),
                doors(NONE, OPEN, NONE, OPEN), null),
            r(2, 3, 2, 3, e("DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(SHUTTER, OPEN, OPEN, OPEN), "MAGICAL_KEY"),
            r(3, 3, 3, 3, e("Gibdo", "Gibdo", "PolsVoice", "PolsVoice"),
                doors(NONE, OPEN, NONE, OPEN), "KEY"),
            r(4, 3, 4, 3, e("DarknutBlue", "DarknutBlue"),
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY"),

            // Neck (y=4)
            r(2, 4, 2, 4, e("WizzrobeBlue", "WizzrobeBlue", "DarknutBlue"),
                doors(BOSS, NONE, OPEN, NONE), null),

            // Mane/head (y=5) — boss area
            r(0, 5, 0, 5, e("DarknutBlue", "DarknutBlue", "Gibdo", "Gibdo"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            boss(1, 5, 1, 5, doors(OPEN, OPEN, NONE, OPEN), "Gleeok4"),
            triforce(2, 5, 2, 5),
            r(3, 5, 3, 5, e("WizzrobeRed", "WizzrobeRed", "WizzrobeBlue"),
                doors(NONE, OPEN, NONE, OPEN), "KEY"),
            r(4, 5, 4, 5, e("Gibdo", "Gibdo", "Gibdo", "Gibdo"),
                doors(NONE, OPEN, NONE, NONE), null),
        };
        return new DungeonDef(8, "LION", 2, 0, 5, 6, rooms, "MAGICAL_KEY");
    }

    // ==================== LEVEL 9: Death Mountain ====================
    // Shape: skull — the largest and final dungeon
    //                T(3,7)
    //                B(3,6)
    // (0,5)(1,5)(2,5)(3,5)(4,5)(5,5)(6,5)    <- skull top
    // (0,4)     (2,4)(3,4)(4,4)     (6,4)    <- eye sockets
    // (0,3)(1,3)(2,3)     (4,3)(5,3)(6,3)    <- jaw
    //                (3,2)                    <- chin
    //           (2,1)(3,1)(4,1)               <- neck
    //                E(3,0)                   <- entrance
    // Enemies: ALL types — Wizzrobe, DarknutBlue, LikeLike, Gibdo, Bubble, Trap
    // Patra mini-boss, then Ganon final boss
    // Items: Silver Arrow, Red Ring (mapped as KEY for now)
    private static DungeonDef buildLevel9() {
        currentLevel = 9;
        DungeonRoomDef[] rooms = {
            // Entrance (y=0)
            entr(3, 0, 3, 0, doors(OPEN, NONE, NONE, NONE)),

            // Neck (y=1)
            dark(2, 1, 2, 1, e("WizzrobeRed", "WizzrobeRed", "WizzrobeRed"),
                doors(NONE, NONE, NONE, OPEN), "KEY"),
            r(3, 1, 3, 1, e("DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(SHUTTER, OPEN, OPEN, OPEN), "KEY"),
            r(4, 1, 4, 1, e("Gibdo", "Gibdo", "Gibdo", "Gibdo"),
                doors(NONE, OPEN, NONE, NONE), "MAP"),

            // Chin (y=2)
            block(3, 2, 3, 2, e("WizzrobeBlue", "WizzrobeBlue", "LikeLike", "LikeLike"),
                doors(LOCKED, NONE, OPEN, NONE), "COMPASS", 0),

            // Jaw (y=3) — left side has money-or-life old man
            r(0, 3, 0, 3, EMPTY,
                doors(OPEN, NONE, NONE, OPEN), null)
                .withOldMan(OLDMAN_MONEY_OR_LIFE, "LEAVE YOUR LIFE OR MONEY", 50),
            r(1, 3, 1, 3, e("WizzrobeRed", "WizzrobeRed", "WizzrobeBlue"),
                doors(NONE, OPEN, NONE, OPEN), null),
            r(2, 3, 2, 3, e("LikeLike", "LikeLike", "Gibdo", "Gibdo"),
                doors(NONE, OPEN, NONE, NONE), "KEY"),
            // Gap at (3,3)
            r(4, 3, 4, 3, e("WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed"),
                doors(NONE, NONE, NONE, OPEN), "SILVER_ARROW"),
            r(5, 3, 5, 3, e("DarknutBlue", "DarknutBlue"),
                doors(NONE, OPEN, NONE, OPEN), null),
            r(6, 3, 6, 3, e("Gibdo", "Gibdo", "Gibdo", "Gibdo"),
                doors(OPEN, OPEN, NONE, NONE), "KEY"),

            // Eye sockets (y=4)
            dark(0, 4, 0, 4, e("WizzrobeBlue", "WizzrobeBlue", "WizzrobeBlue"),
                doors(OPEN, NONE, OPEN, NONE), "KEY"),
            // Patra mini-boss in left eye
            boss(2, 4, 2, 4, doors(SHUTTER, NONE, NONE, OPEN), "Patra"),
            r(3, 4, 3, 4, e("DarknutBlue", "DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(SHUTTER, OPEN, OPEN, OPEN), "BOSS_KEY"),
            r(4, 4, 4, 4, e("WizzrobeRed", "WizzrobeRed", "LikeLike"),
                doors(SHUTTER, OPEN, NONE, NONE), "KEY"),
            dark(6, 4, 6, 4, e("WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed", "WizzrobeRed"),
                doors(OPEN, NONE, OPEN, NONE), "KEY"),

            // Skull top (y=5) — boss gauntlet
            r(0, 5, 0, 5, e("DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(NONE, NONE, OPEN, OPEN), "KEY"),
            r(1, 5, 1, 5, e("WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed"),
                doors(NONE, OPEN, NONE, OPEN), null),
            r(2, 5, 2, 5, e("DarknutBlue", "DarknutBlue", "WizzrobeBlue"),
                doors(NONE, OPEN, OPEN, OPEN), null),
            r(3, 5, 3, 5, e("LikeLike", "LikeLike", "WizzrobeRed", "WizzrobeRed"),
                doors(BOSS, OPEN, OPEN, OPEN), null),
            r(4, 5, 4, 5, e("Gibdo", "Gibdo", "DarknutBlue", "DarknutBlue"),
                doors(NONE, OPEN, OPEN, OPEN), null),
            r(5, 5, 5, 5, e("WizzrobeBlue", "WizzrobeBlue"),
                doors(NONE, OPEN, NONE, OPEN), null),
            r(6, 5, 6, 5, e("DarknutBlue", "DarknutBlue", "DarknutBlue", "DarknutBlue"),
                doors(NONE, OPEN, OPEN, NONE), "KEY"),

            // Ganon (y=6)
            boss(3, 6, 3, 6, doors(OPEN, NONE, OPEN, NONE), "Ganon"),
            // Zelda (y=7)
            zelda(3, 7, 3, 7),
        };
        return new DungeonDef(9, "DEATH MOUNTAIN", 3, 0, 7, 8, rooms, "SILVER_ARROW");
    }
}
