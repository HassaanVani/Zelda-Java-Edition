package zelda;

/**
 * Defines static data for all 9 dungeon layouts (1st Quest).
 * Each dungeon is defined by its rooms, door connections, enemy types,
 * item placements, and special features.
 *
 * Based on the NES Legend of Zelda dungeon maps.
 */
public class DungeonData {

    /** Defines a single room in a dungeon. */
    public static class DungeonRoomDef {
        public final int localX, localY;     // position in dungeon grid
        public final int mapCol, mapRow;     // position in dungeon tileset image
        public final String[] enemies;       // enemy types to spawn (using EnemyFactory names)
        public final int[] doors;            // door states: 0=NONE, 1=OPEN, 2=LOCKED, 3=BOSS_LOCKED, 4=BOMBED
        public final String itemType;        // null, "KEY", "MAP", "COMPASS", "BOSS_KEY", "BOOMERANG", "BOW", etc.
        public final String bossType;        // null or boss type name
        public final boolean isDark;         // requires candle to see
        public final boolean hasBlock;       // has pushable block puzzle
        public final boolean isStairway;     // has stairway to another room
        public final int stairwayTarget;     // index of target room in same dungeon (-1 if none)

        public DungeonRoomDef(int localX, int localY, int mapCol, int mapRow,
                              String[] enemies, int[] doors, String itemType,
                              String bossType, boolean isDark, boolean hasBlock,
                              boolean isStairway, int stairwayTarget) {
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
            this.isStairway = isStairway;
            this.stairwayTarget = stairwayTarget;
        }
    }

    /** Complete definition of a dungeon. */
    public static class DungeonDef {
        public final int number;
        public final String name;
        public final int entranceX, entranceY; // entrance room local coords
        public final int mapWidth, mapHeight;
        public final DungeonRoomDef[] rooms;
        public final String dungeonItem;       // the special item found in this dungeon
        public final boolean[][] minimapMask;  // shape mask for minimap display

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
            this.minimapMask = minimapMask;
        }
    }

    // Door state constants matching ZeldaDungeon doorStates array indices
    private static final int NONE = 0;
    private static final int OPEN = 1;
    private static final int LOCKED = 2;
    private static final int BOSS = 3;
    private static final int BOMBABLE = 4;
    private static final int BOMBED = 5;

    // Shorthand for doors array: [NORTH, WEST, SOUTH, EAST]
    private static int[] doors(int n, int w, int s, int e) { return new int[]{n, w, s, e}; }

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
    private static DungeonDef buildLevel1() {
        DungeonRoomDef[] rooms = {
            // Entrance room (3,0)
            new DungeonRoomDef(3, 0, 1, 0, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            // West room (2,0) - has key
            new DungeonRoomDef(2, 0, 0, 0,
                new String[]{"Stalfos", "Stalfos", "Stalfos"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            // East room (4,0) - has key
            new DungeonRoomDef(4, 0, 2, 0,
                new String[]{"KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue"},
                doors(NONE, OPEN, NONE, OPEN), "KEY", null, false, false, false, -1),
            // Far east room (5,0) - has map
            new DungeonRoomDef(5, 0, 3, 0,
                new String[]{"Stalfos", "Stalfos"},
                doors(NONE, OPEN, NONE, NONE), "MAP", null, false, false, false, -1),
            // Center room (3,1) - locked doors
            new DungeonRoomDef(3, 1, 1, 1,
                new String[]{"Stalfos", "Stalfos", "Stalfos"},
                doors(LOCKED, LOCKED, OPEN, LOCKED), null, null, false, true, false, -1),
            // West inner (2,1) - has compass
            new DungeonRoomDef(2, 1, 0, 1,
                new String[]{"Stalfos", "Stalfos"},
                doors(NONE, NONE, NONE, OPEN), "COMPASS", null, false, false, false, -1),
            // East inner (4,1) - has boomerang
            new DungeonRoomDef(4, 1, 2, 1,
                new String[]{"GoriyaRed", "GoriyaRed", "GoriyaRed"},
                doors(NONE, OPEN, NONE, NONE), "BOW", null, false, false, false, -1),
            // Boss corridor (3,2) - keese
            new DungeonRoomDef(3, 2, 1, 2,
                new String[]{"KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseRed", "KeeseRed"},
                doors(BOSS, NONE, OPEN, LOCKED), null, null, false, false, false, -1),
            // Boss key room (4,2)
            new DungeonRoomDef(4, 2, 2, 2,
                new String[]{"Stalfos", "Stalfos", "Stalfos"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, false, false, false, -1),
            // Boss room (3,3) - Aquamentus
            new DungeonRoomDef(3, 3, 1, 3,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Aquamentus", false, false, false, -1),
            // Triforce room (3,4)
            new DungeonRoomDef(3, 4, 1, 4,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(1, "EAGLE", 3, 0, 6, 5, rooms, "BOW");
    }

    // ==================== LEVEL 2: Moon ====================
    private static DungeonDef buildLevel2() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 5, 0, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 0, 4, 0,
                new String[]{"Rope", "Rope", "Rope"},
                doors(OPEN, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 0, 6, 0,
                new String[]{"GoriyaRed", "GoriyaRed"},
                doors(OPEN, OPEN, NONE, NONE), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(2, 1, 4, 1,
                new String[]{"Rope", "Rope"},
                doors(NONE, NONE, OPEN, NONE), "MAP", null, false, false, false, -1),
            new DungeonRoomDef(3, 1, 5, 1,
                new String[]{"GoriyaRed", "GoriyaRed", "GoriyaRed"},
                doors(LOCKED, LOCKED, OPEN, LOCKED), null, null, false, true, false, -1),
            new DungeonRoomDef(4, 1, 6, 1,
                new String[]{"Rope", "Rope", "Rope"},
                doors(NONE, OPEN, OPEN, NONE), "COMPASS", null, false, false, false, -1),
            new DungeonRoomDef(3, 2, 5, 2,
                new String[]{"GoriyaBlue", "GoriyaBlue"},
                doors(BOSS, NONE, OPEN, LOCKED), "MAGICAL_BOOMERANG", null, false, false, false, -1),
            new DungeonRoomDef(4, 2, 6, 2,
                new String[]{"Rope", "Rope", "Rope"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, false, false, false, -1),
            new DungeonRoomDef(3, 3, 5, 3,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Dodongo", false, false, false, -1),
            new DungeonRoomDef(3, 4, 5, 4,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(2, "MOON", 3, 0, 6, 5, rooms, "MAGICAL_BOOMERANG");
    }

    // ==================== LEVEL 3: Manji ====================
    private static DungeonDef buildLevel3() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 9, 0, new String[0],
                doors(OPEN, NONE, NONE, NONE), null, null, false, false, false, -1),
            new DungeonRoomDef(3, 1, 9, 1,
                new String[]{"DarknutRed", "DarknutRed", "DarknutRed"},
                doors(OPEN, OPEN, OPEN, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 1, 8, 1,
                new String[]{"DarknutRed", "DarknutRed"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 1, 10, 1,
                new String[]{"KeeseBlue", "KeeseBlue", "KeeseBlue", "KeeseBlue"},
                doors(NONE, OPEN, NONE, NONE), "MAP", null, true, false, false, -1),
            new DungeonRoomDef(3, 2, 9, 2,
                new String[]{"DarknutRed", "DarknutRed", "DarknutRed"},
                doors(BOSS, NONE, OPEN, LOCKED), "COMPASS", null, false, true, false, -1),
            new DungeonRoomDef(4, 2, 10, 2,
                new String[]{"DarknutRed", "DarknutRed"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, false, false, false, -1),
            new DungeonRoomDef(3, 3, 9, 3,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Manhandla", false, false, false, -1),
            new DungeonRoomDef(3, 4, 9, 4,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(3, "MANJI", 3, 0, 6, 5, rooms, "RAFT");
    }

    // ==================== LEVEL 4: Snake ====================
    private static DungeonDef buildLevel4() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 13, 0, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 0, 12, 0,
                new String[]{"Vire", "Vire", "Vire"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 0, 14, 0,
                new String[]{"KeeseRed", "KeeseRed", "KeeseRed", "KeeseRed"},
                doors(NONE, OPEN, NONE, NONE), "MAP", null, true, false, false, -1),
            new DungeonRoomDef(3, 1, 13, 1,
                new String[]{"DarknutBlue", "DarknutBlue"},
                doors(LOCKED, NONE, OPEN, NONE), "COMPASS", null, false, true, false, -1),
            new DungeonRoomDef(3, 2, 13, 2,
                new String[]{"Vire", "Vire"},
                doors(BOSS, NONE, OPEN, LOCKED), "STEPLADDER", null, false, false, false, -1),
            new DungeonRoomDef(4, 2, 14, 2,
                new String[]{"Vire", "Vire", "KeeseRed", "KeeseRed"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, true, false, false, -1),
            new DungeonRoomDef(3, 3, 13, 3,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Gleeok", false, false, false, -1),
            new DungeonRoomDef(3, 4, 13, 4,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(4, "SNAKE", 3, 0, 6, 5, rooms, "STEPLADDER");
    }

    // ==================== LEVEL 5: Lizard ====================
    private static DungeonDef buildLevel5() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 1, 5, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 0, 0, 5,
                new String[]{"PolsVoice", "PolsVoice", "KeeseBlue", "KeeseBlue"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 0, 2, 5,
                new String[]{"Gibdo", "Gibdo", "Gibdo"},
                doors(NONE, OPEN, NONE, NONE), "MAP", null, false, false, false, -1),
            new DungeonRoomDef(3, 1, 1, 6,
                new String[]{"DarknutBlue", "DarknutBlue", "DarknutBlue"},
                doors(LOCKED, LOCKED, OPEN, NONE), "COMPASS", null, false, true, false, -1),
            new DungeonRoomDef(2, 1, 0, 6,
                new String[]{"Gibdo", "Gibdo"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(3, 2, 1, 7,
                new String[]{"PolsVoice", "PolsVoice"},
                doors(BOSS, NONE, OPEN, LOCKED), "RECORDER", null, false, false, false, -1),
            new DungeonRoomDef(4, 2, 2, 7,
                new String[]{"Gibdo", "Gibdo", "Gibdo"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, false, false, false, -1),
            new DungeonRoomDef(3, 3, 1, 8,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Digdogger", false, false, false, -1),
            new DungeonRoomDef(3, 4, 1, 9,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(5, "LIZARD", 3, 0, 6, 5, rooms, "RECORDER");
    }

    // ==================== LEVEL 6: Dragon ====================
    private static DungeonDef buildLevel6() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 5, 5, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 0, 4, 5,
                new String[]{"WizzrobeBlue", "WizzrobeBlue", "WizzrobeBlue"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 0, 6, 5,
                new String[]{"WizzrobeRed", "WizzrobeRed", "LikeLike"},
                doors(NONE, OPEN, NONE, NONE), "MAP", null, false, false, false, -1),
            new DungeonRoomDef(3, 1, 5, 6,
                new String[]{"WizzrobeBlue", "WizzrobeBlue"},
                doors(LOCKED, LOCKED, OPEN, LOCKED), "COMPASS", null, false, true, false, -1),
            new DungeonRoomDef(2, 1, 4, 6,
                new String[]{"LikeLike", "LikeLike"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 1, 6, 6,
                new String[]{"WizzrobeRed", "WizzrobeRed"},
                doors(NONE, OPEN, NONE, NONE), "KEY", null, true, false, false, -1),
            new DungeonRoomDef(3, 2, 5, 7,
                new String[]{"WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed"},
                doors(BOSS, NONE, OPEN, LOCKED), "MAGICAL_ROD", null, false, false, false, -1),
            new DungeonRoomDef(4, 2, 6, 7,
                new String[]{"WizzrobeRed", "WizzrobeRed"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, true, false, false, -1),
            new DungeonRoomDef(3, 3, 5, 8,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Gohma", false, false, false, -1),
            new DungeonRoomDef(3, 4, 5, 9,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(6, "DRAGON", 3, 0, 6, 5, rooms, "MAGICAL_ROD");
    }

    // ==================== LEVEL 7: Demon ====================
    private static DungeonDef buildLevel7() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 9, 5, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 0, 8, 5,
                new String[]{"Goriya", "Goriya", "Goriya", "Stalfos"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 0, 10, 5,
                new String[]{"Wallmaster", "Wallmaster"},
                doors(OPEN, OPEN, NONE, NONE), "MAP", null, false, false, false, -1),
            new DungeonRoomDef(4, 1, 10, 6,
                new String[]{"DarknutRed", "DarknutRed", "DarknutRed"},
                doors(NONE, NONE, OPEN, NONE), "COMPASS", null, false, false, false, -1),
            new DungeonRoomDef(3, 1, 9, 6,
                new String[]{"Goriya", "Goriya", "DarknutRed"},
                doors(LOCKED, NONE, OPEN, NONE), null, null, false, true, false, -1),
            new DungeonRoomDef(3, 2, 9, 7,
                new String[]{"DarknutBlue", "DarknutBlue", "DarknutBlue"},
                doors(BOSS, NONE, OPEN, LOCKED), "RED_CANDLE", null, false, false, false, -1),
            new DungeonRoomDef(4, 2, 10, 7,
                new String[]{"Goriya", "Goriya", "DarknutRed"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, false, false, false, -1),
            new DungeonRoomDef(3, 3, 9, 8,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Aquamentus", false, false, false, -1),
            new DungeonRoomDef(3, 4, 9, 9,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(7, "DEMON", 3, 0, 6, 5, rooms, "RED_CANDLE");
    }

    // ==================== LEVEL 8: Lion ====================
    private static DungeonDef buildLevel8() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 1, 10, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 0, 0, 10,
                new String[]{"DarknutBlue", "DarknutBlue", "DarknutBlue", "DarknutBlue"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 0, 2, 10,
                new String[]{"Gibdo", "Gibdo", "Gibdo", "PolsVoice"},
                doors(OPEN, OPEN, NONE, NONE), "MAP", null, true, false, false, -1),
            new DungeonRoomDef(4, 1, 2, 11,
                new String[]{"WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed"},
                doors(NONE, NONE, OPEN, NONE), "COMPASS", null, false, false, false, -1),
            new DungeonRoomDef(3, 1, 1, 11,
                new String[]{"DarknutBlue", "DarknutBlue", "Gibdo", "Gibdo"},
                doors(LOCKED, LOCKED, OPEN, NONE), null, null, false, true, false, -1),
            new DungeonRoomDef(2, 1, 0, 11,
                new String[]{"WizzrobeRed", "WizzrobeRed", "WizzrobeRed"},
                doors(NONE, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(3, 2, 1, 12,
                new String[]{"DarknutBlue", "DarknutBlue", "DarknutBlue"},
                doors(BOSS, NONE, OPEN, LOCKED), "MAGICAL_KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 2, 2, 12,
                new String[]{"DarknutBlue", "DarknutBlue", "Gibdo", "Gibdo"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, true, false, false, -1),
            new DungeonRoomDef(3, 3, 1, 13,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Gleeok", false, false, false, -1),
            new DungeonRoomDef(3, 4, 1, 14,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "TRIFORCE", null, false, false, false, -1),
        };
        return new DungeonDef(8, "LION", 3, 0, 6, 5, rooms, "MAGICAL_KEY");
    }

    // ==================== LEVEL 9: Death Mountain ====================
    private static DungeonDef buildLevel9() {
        DungeonRoomDef[] rooms = {
            new DungeonRoomDef(3, 0, 5, 10, new String[0],
                doors(OPEN, OPEN, NONE, OPEN), null, null, false, false, false, -1),
            new DungeonRoomDef(2, 0, 4, 10,
                new String[]{"WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed", "LikeLike"},
                doors(OPEN, NONE, NONE, OPEN), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(4, 0, 6, 10,
                new String[]{"DarknutBlue", "DarknutBlue", "DarknutBlue", "DarknutBlue"},
                doors(OPEN, OPEN, NONE, NONE), "MAP", null, false, false, false, -1),
            new DungeonRoomDef(2, 1, 4, 11,
                new String[]{"WizzrobeRed", "WizzrobeRed", "WizzrobeRed"},
                doors(NONE, NONE, OPEN, NONE), "COMPASS", null, true, false, false, -1),
            new DungeonRoomDef(4, 1, 6, 11,
                new String[]{"Gibdo", "Gibdo", "Gibdo", "Gibdo"},
                doors(NONE, NONE, OPEN, NONE), "KEY", null, false, false, false, -1),
            new DungeonRoomDef(3, 1, 5, 11,
                new String[]{"DarknutBlue", "DarknutBlue", "WizzrobeBlue", "WizzrobeBlue"},
                doors(LOCKED, LOCKED, OPEN, LOCKED), null, null, false, true, false, -1),
            new DungeonRoomDef(3, 2, 5, 12,
                new String[]{"WizzrobeRed", "WizzrobeRed", "LikeLike", "LikeLike"},
                doors(BOSS, NONE, OPEN, LOCKED), "SILVER_ARROW", null, false, false, false, -1),
            new DungeonRoomDef(4, 2, 6, 12,
                new String[]{"WizzrobeBlue", "WizzrobeBlue", "WizzrobeRed", "WizzrobeRed"},
                doors(NONE, OPEN, NONE, NONE), "BOSS_KEY", null, true, false, false, -1),
            new DungeonRoomDef(3, 3, 5, 13,
                new String[0],
                doors(OPEN, NONE, OPEN, NONE), null, "Ganon", false, false, false, -1),
            // Zelda's room
            new DungeonRoomDef(3, 4, 5, 14,
                new String[0],
                doors(NONE, NONE, OPEN, NONE), "ZELDA", null, false, false, false, -1),
        };
        return new DungeonDef(9, "DEATH MOUNTAIN", 3, 0, 6, 5, rooms, "SILVER_ARROW");
    }
}
