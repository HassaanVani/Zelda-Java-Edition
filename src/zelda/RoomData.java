package zelda;

/**
 * Defines static per-room data for the overworld: enemy spawns, secrets, cave links.
 * Based on the NES Legend of Zelda 1st Quest overworld layout (16x8 grid).
 *
 * Each room is addressed by (roomX, roomY) where X=0..15, Y=0..7.
 * Y=0 is top (Death Mountain area), Y=7 is bottom (starting area).
 */
public class RoomData {

    /** Enemy spawn definition for a single enemy in a room. */
    public static class EnemySpawn {
        public final String type;   // e.g. "OctorokRed", "MoblinBlue", "Lynel"
        public final double x, y;

        public EnemySpawn(String type, double x, double y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    /** Secret/interactive element in a room. */
    public static class RoomSecret {
        public final SecretType type;
        public final int tileX, tileY;
        public final int revealedCaveId; // -1 if no cave

        public RoomSecret(SecretType type, int tileX, int tileY, int revealedCaveId) {
            this.type = type;
            this.tileX = tileX;
            this.tileY = tileY;
            this.revealedCaveId = revealedCaveId;
        }
    }

    public enum SecretType {
        BOMB_WALL,      // bomb this tile to reveal cave
        BURN_BUSH,      // burn bush with candle to reveal stairway
        PUSH_ROCK,      // push rock to reveal stairway
        PUSH_GRAVE,     // push gravestone to reveal stairway
        RECORDER,       // play recorder to reveal stairway
        RAFT_DOCK,      // raft departure point
        FAIRY_FOUNTAIN  // fairy healing area
    }

    /** Complete definition of one overworld room. */
    public static class RoomDef {
        public final int roomX, roomY;
        public final EnemySpawn[] enemies;
        public final RoomSecret[] secrets;
        public final int caveId;           // -1 = no cave entrance
        public final int caveTileX, caveTileY; // tile coords of cave entrance
        public final int dungeonId;        // -1 = no dungeon entrance
        public final boolean noEnemies;    // true = peaceful room

        public RoomDef(int roomX, int roomY, EnemySpawn[] enemies, RoomSecret[] secrets,
                       int caveId, int caveTileX, int caveTileY, int dungeonId, boolean noEnemies) {
            this.roomX = roomX;
            this.roomY = roomY;
            this.enemies = enemies;
            this.secrets = secrets;
            this.caveId = caveId;
            this.caveTileX = caveTileX;
            this.caveTileY = caveTileY;
            this.dungeonId = dungeonId;
            this.noEnemies = noEnemies;
        }
    }

    // Cave IDs for the first quest
    public static final int CAVE_SWORD = 0;
    public static final int CAVE_SHOP_1 = 1;
    public static final int CAVE_SHOP_2 = 2;
    public static final int CAVE_SHOP_3 = 3;
    public static final int CAVE_HINT_1 = 4;
    public static final int CAVE_HINT_2 = 5;
    public static final int CAVE_WHITE_SWORD = 6;
    public static final int CAVE_MAGICAL_SWORD = 7;
    public static final int CAVE_POTION_SHOP = 8;
    public static final int CAVE_MONEY_GAME = 9;
    public static final int CAVE_FAIRY_1 = 10;
    public static final int CAVE_FAIRY_2 = 11;
    public static final int CAVE_FAIRY_3 = 12;
    public static final int CAVE_FAIRY_4 = 13;
    public static final int CAVE_TAKE_ANY_1 = 14;
    public static final int CAVE_TAKE_ANY_2 = 15;
    public static final int CAVE_PAY_ME = 16;
    public static final int CAVE_HINT_3 = 17;
    public static final int CAVE_HINT_4 = 18;
    public static final int CAVE_LETTER = 19;
    public static final int CAVE_BLUE_RING = 20;
    public static final int CAVE_POWER_BRACELET = 21;
    public static final int CAVE_HINT_5 = 22;
    public static final int CAVE_COAST_ITEM = 23;

    /**
     * Returns the room definition for the given overworld coordinates.
     * Returns null if no special data is defined (room uses default behavior).
     */
    public static RoomDef getRoomDef(int roomX, int roomY) {
        // Key rooms from the NES 1st quest
        // Starting screen (7,7): no enemies, sword cave
        if (roomX == 7 && roomY == 7) {
            return new RoomDef(7, 7, new EnemySpawn[0], new RoomSecret[0],
                CAVE_SWORD, 7, 3, -1, true);
        }

        // Dungeon 1 entrance (7,3)
        if (roomX == 7 && roomY == 3) {
            return new RoomDef(7, 3, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 1, true);
        }

        // Dungeon 2 entrance (12,3)
        if (roomX == 12 && roomY == 3) {
            return new RoomDef(12, 3, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 2, true);
        }

        // Dungeon 3 entrance (3,3)
        if (roomX == 3 && roomY == 3) {
            return new RoomDef(3, 3, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 3, true);
        }

        // Dungeon 4 entrance (5,2)
        if (roomX == 5 && roomY == 2) {
            return new RoomDef(5, 2, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 4, true);
        }

        // Dungeon 5 entrance (10,1)
        if (roomX == 10 && roomY == 1) {
            return new RoomDef(10, 1, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 5, true);
        }

        // Dungeon 6 entrance (2,2)
        if (roomX == 2 && roomY == 2) {
            return new RoomDef(2, 2, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 6, true);
        }

        // Dungeon 7 entrance (12,1)
        if (roomX == 12 && roomY == 1) {
            return new RoomDef(12, 1, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 7, true);
        }

        // Dungeon 8 entrance (0,3) - through bombable wall or raft
        if (roomX == 0 && roomY == 3) {
            return new RoomDef(0, 3, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 8, true);
        }

        // Dungeon 9 / Death Mountain entrance (6,0)
        if (roomX == 6 && roomY == 0) {
            return new RoomDef(6, 0, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, 9, true);
        }

        // White Sword cave (2,1) - requires 5+ hearts
        if (roomX == 2 && roomY == 1) {
            return new RoomDef(2, 1, new EnemySpawn[0], new RoomSecret[0],
                CAVE_WHITE_SWORD, 7, 3, -1, true);
        }

        // Rooms with Octorok enemies (south-central overworld)
        if (roomX == 6 && roomY == 7) {
            return new RoomDef(6, 7, new EnemySpawn[] {
                new EnemySpawn("OctorokRed", 48, 48),
                new EnemySpawn("OctorokRed", 128, 80),
                new EnemySpawn("OctorokRed", 80, 120),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        if (roomX == 8 && roomY == 7) {
            return new RoomDef(8, 7, new EnemySpawn[] {
                new EnemySpawn("OctorokRed", 56, 40),
                new EnemySpawn("OctorokRed", 160, 48),
                new EnemySpawn("OctorokBlue", 100, 100),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        if (roomX == 7 && roomY == 6) {
            return new RoomDef(7, 6, new EnemySpawn[0], new RoomSecret[0],
                -1, -1, -1, -1, true);
        }

        // Rooms with Moblins (forest area)
        if (roomX == 5 && roomY == 4) {
            return new RoomDef(5, 4, new EnemySpawn[] {
                new EnemySpawn("MoblinRed", 64, 48),
                new EnemySpawn("MoblinRed", 160, 80),
                new EnemySpawn("MoblinBlue", 112, 120),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        // Rooms with Lynels (mountain area, first quest)
        if (roomX == 4 && roomY == 1) {
            return new RoomDef(4, 1, new EnemySpawn[] {
                new EnemySpawn("LynelRed", 80, 60),
                new EnemySpawn("LynelRed", 176, 100),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        // Graveyard rooms with Ghinis (5,6 area)
        if (roomX == 9 && roomY == 5) {
            return new RoomDef(9, 5, new EnemySpawn[] {
                new EnemySpawn("Ghini", 60, 48),
                new EnemySpawn("Ghini", 150, 80),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        // Shop rooms
        if (roomX == 6 && roomY == 5) {
            return new RoomDef(6, 5, new EnemySpawn[0], new RoomSecret[0],
                CAVE_SHOP_1, 7, 3, -1, true);
        }

        // Fairy fountain
        if (roomX == 11 && roomY == 2) {
            return new RoomDef(11, 2, new EnemySpawn[0],
                new RoomSecret[] {
                    new RoomSecret(SecretType.FAIRY_FOUNTAIN, 7, 5, -1)
                },
                -1, -1, -1, -1, true);
        }

        // Bombable wall secret
        if (roomX == 10 && roomY == 5) {
            return new RoomDef(10, 5, new EnemySpawn[] {
                new EnemySpawn("OctorokBlue", 64, 48),
                new EnemySpawn("OctorokBlue", 160, 80),
            }, new RoomSecret[] {
                new RoomSecret(SecretType.BOMB_WALL, 7, 0, CAVE_HINT_1)
            }, -1, -1, -1, -1, false);
        }

        // Burnable bush secret
        if (roomX == 4 && roomY == 5) {
            return new RoomDef(4, 5, new EnemySpawn[] {
                new EnemySpawn("MoblinRed", 80, 60),
                new EnemySpawn("MoblinRed", 160, 100),
            }, new RoomSecret[] {
                new RoomSecret(SecretType.BURN_BUSH, 10, 6, CAVE_SHOP_2)
            }, -1, -1, -1, -1, false);
        }

        // Pushable gravestone secret
        if (roomX == 8 && roomY == 5) {
            return new RoomDef(8, 5, new EnemySpawn[] {
                new EnemySpawn("Ghini", 80, 48),
            }, new RoomSecret[] {
                new RoomSecret(SecretType.PUSH_GRAVE, 5, 4, CAVE_HINT_2)
            }, -1, -1, -1, -1, false);
        }

        // Rooms with Tektites (rocky terrain)
        if (roomX == 9 && roomY == 3) {
            return new RoomDef(9, 3, new EnemySpawn[] {
                new EnemySpawn("TektiteBlue", 48, 48),
                new EnemySpawn("TektiteBlue", 144, 80),
                new EnemySpawn("TektiteRed", 96, 120),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        // Rooms with Leevers (desert area)
        if (roomX == 1 && roomY == 5) {
            return new RoomDef(1, 5, new EnemySpawn[] {
                new EnemySpawn("LeeverBlue", 64, 56),
                new EnemySpawn("LeeverBlue", 160, 96),
                new EnemySpawn("LeeverRed", 112, 72),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        // Rooms with Peahats
        if (roomX == 3 && roomY == 6) {
            return new RoomDef(3, 6, new EnemySpawn[] {
                new EnemySpawn("Peahat", 64, 56),
                new EnemySpawn("Peahat", 160, 96),
                new EnemySpawn("Peahat", 112, 40),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        // Zola (Zora) water screen
        if (roomX == 14 && roomY == 3) {
            return new RoomDef(14, 3, new EnemySpawn[] {
                new EnemySpawn("Zola", 100, 60),
                new EnemySpawn("Zola", 170, 90),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        // Armos statues screen
        if (roomX == 2 && roomY == 4) {
            return new RoomDef(2, 4, new EnemySpawn[] {
                new EnemySpawn("Armos", 64, 48),
                new EnemySpawn("Armos", 128, 48),
                new EnemySpawn("Armos", 192, 48),
            }, new RoomSecret[0], -1, -1, -1, -1, false);
        }

        return null; // no specific data — room will use default biome spawning
    }

    /**
     * Returns the list of all dungeon entrance room coordinates for the 1st quest.
     * Index 0 = dungeon 1, index 8 = dungeon 9.
     */
    public static int[][] getDungeonEntrances() {
        return new int[][] {
            { 7, 3},  // Dungeon 1
            {12, 3},  // Dungeon 2
            { 3, 3},  // Dungeon 3
            { 5, 2},  // Dungeon 4
            {10, 1},  // Dungeon 5
            { 2, 2},  // Dungeon 6
            {12, 1},  // Dungeon 7
            { 0, 3},  // Dungeon 8
            { 6, 0},  // Dungeon 9
        };
    }

    /**
     * Returns the entrance tile coordinates for each dungeon on its room.
     * [dungeonIndex][0] = tileX, [dungeonIndex][1] = tileY
     */
    public static int[][] getDungeonEntranceTiles() {
        return new int[][] {
            {7, 3},  // Dungeon 1
            {7, 3},  // Dungeon 2
            {7, 3},  // Dungeon 3
            {7, 3},  // Dungeon 4
            {7, 3},  // Dungeon 5
            {7, 3},  // Dungeon 6
            {7, 3},  // Dungeon 7
            {7, 3},  // Dungeon 8
            {7, 3},  // Dungeon 9
        };
    }
}
