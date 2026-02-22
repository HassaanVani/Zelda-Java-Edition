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
        FAIRY_FOUNTAIN, // fairy healing area
        KILL_ALL        // killing all enemies reveals item/passage
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

    // ======================== Cave IDs ========================
    public static final int CAVE_SWORD = 0;
    public static final int CAVE_SHOP_A1 = 1;   // Shield 160, Key 100, Blue Candle 60
    public static final int CAVE_SHOP_A2 = 2;
    public static final int CAVE_SHOP_A3 = 3;
    public static final int CAVE_SHOP_B1 = 4;   // Bombs 20, Arrow 80, Blue Candle 60
    public static final int CAVE_SHOP_B2 = 5;
    public static final int CAVE_SHOP_B3 = 6;
    public static final int CAVE_SHOP_C  = 7;   // Blue Ring 250, Food 100, Key 80
    public static final int CAVE_SHOP_D  = 8;   // Food 100, Bombs 20, Key 80
    public static final int CAVE_WHITE_SWORD = 9;
    public static final int CAVE_MAGICAL_SWORD = 10;
    public static final int CAVE_POTION_SHOP = 11;
    public static final int CAVE_MONEY_GAME = 12;
    public static final int CAVE_FAIRY_1 = 13;
    public static final int CAVE_FAIRY_2 = 14;
    public static final int CAVE_FAIRY_3 = 15;
    public static final int CAVE_FAIRY_4 = 16;
    public static final int CAVE_TAKE_ANY_1 = 17;
    public static final int CAVE_TAKE_ANY_2 = 18;
    public static final int CAVE_LETTER = 19;
    public static final int CAVE_DOOR_REPAIR = 20;
    public static final int CAVE_HINT_PENINSULA = 21;
    public static final int CAVE_HINT_DODONGO = 22;
    public static final int CAVE_HINT_SWORD = 23;
    public static final int CAVE_HINT_TREE = 24;
    public static final int CAVE_HINT_FAIRY = 25;
    public static final int CAVE_HINT_TRIFORCE = 26;
    public static final int CAVE_HINT_GRAVE = 27;
    public static final int CAVE_HINT_SPECTACLE = 28;
    public static final int CAVE_HINT_MAZE = 29;
    public static final int CAVE_COAST_SECRET = 30;

    private static final EnemySpawn[] NO_ENEMIES = new EnemySpawn[0];
    private static final RoomSecret[] NO_SECRETS = new RoomSecret[0];

    private static EnemySpawn e(String type, double x, double y) {
        return new EnemySpawn(type, x, y);
    }

    /**
     * Returns the room definition for the given overworld coordinates.
     * Covers all 128 rooms of the NES 1st Quest overworld.
     */
    public static RoomDef getRoomDef(int roomX, int roomY) {

        // ==================== ROW 0: Death Mountain ====================
        if (roomY == 0) {
            if (roomX == 3) return cv(3, 0, CAVE_HINT_TRIFORCE);
            if (roomX == 6) return dg(6, 0, 9);
            if (roomX == 12) return new RoomDef(12, 0, NO_ENEMIES,
                new RoomSecret[]{ new RoomSecret(SecretType.PUSH_GRAVE, 7, 3, CAVE_MAGICAL_SWORD) },
                CAVE_MAGICAL_SWORD, 7, 3, -1, true);
            return biome(roomX, roomY, "LynelRed", "TektiteRed", null);
        }

        // ==================== ROW 1: Mountains ====================
        if (roomY == 1) {
            if (roomX == 1)  return cv(1, 1, CAVE_SHOP_A1);
            if (roomX == 2)  return cv(2, 1, CAVE_WHITE_SWORD);
            if (roomX == 6)  return cv(6, 1, CAVE_HINT_TREE);
            if (roomX == 9)  return cv(9, 1, CAVE_SHOP_B1);
            if (roomX == 10) return dg(10, 1, 5);
            if (roomX == 12) return dg(12, 1, 7);
            if (roomX == 14) return cv(14, 1, CAVE_DOOR_REPAIR);
            return biome(roomX, roomY, "LynelRed", "MoblinBlue", "TektiteRed");
        }

        // ==================== ROW 2: Lake/Mountains ====================
        if (roomY == 2) {
            if (roomX == 0)  return cv(0, 2, CAVE_SHOP_A2);
            if (roomX == 2)  return dg(2, 2, 6);
            if (roomX == 3)  return cv(3, 2, CAVE_LETTER);
            if (roomX == 5)  return dg(5, 2, 4);
            if (roomX == 7)  return fy(7, 2, CAVE_FAIRY_1);
            if (roomX == 8)  return cv(8, 2, CAVE_TAKE_ANY_2);
            if (roomX == 11) return fy(11, 2, CAVE_FAIRY_2);
            if (roomX == 14) return new RoomDef(14, 2, new EnemySpawn[]{
                    e("Armos", 64, 48), e("Armos", 96, 48), e("Armos", 128, 48),
                    e("Armos", 160, 48), e("Armos", 192, 48),
                    e("Armos", 64, 80), e("Armos", 96, 80), e("Armos", 128, 80),
                    e("Armos", 160, 80), e("Armos", 192, 80),
                }, new RoomSecret[]{ new RoomSecret(SecretType.PUSH_ROCK, 12, 3, -1) },
                -1, -1, -1, -1, false);
            return biome(roomX, roomY, "TektiteBlue", "OctorokBlue", null);
        }

        // ==================== ROW 3: Lake Hylia ====================
        if (roomY == 3) {
            if (roomX == 0)  return dg(0, 3, 8);
            if (roomX == 7)  return dg(7, 3, 1);
            if (roomX == 11) return cv(11, 3, CAVE_HINT_SWORD);
            if (roomX == 12) return dg(12, 3, 2);
            if (roomX == 15) return fy(15, 3, CAVE_FAIRY_3);
            if (roomX == 14) return new RoomDef(14, 3, new EnemySpawn[]{
                    e("Zola", 100, 60), e("Zola", 170, 90),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX >= 6 && roomX <= 10 && roomX != 7) {
                double[] p1 = sp(roomX, roomY, 0), p2 = sp(roomX, roomY, 1);
                return new RoomDef(roomX, roomY, new EnemySpawn[]{
                    e("Zola", p1[0], p1[1]), e("Zola", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            return biome(roomX, roomY, "TektiteBlue", "OctorokRed", null);
        }

        // ==================== ROW 4: Forest ====================
        if (roomY == 4) {
            if (roomX == 1)  return cv(1, 4, CAVE_HINT_PENINSULA);
            if (roomX == 2)  return new RoomDef(2, 4, new EnemySpawn[]{
                    e("Armos", 64, 48), e("Armos", 128, 48), e("Armos", 192, 48),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 4)  return dg(4, 4, 3);
            if (roomX == 6)  return cv(6, 4, CAVE_SHOP_D);
            if (roomX == 8)  return cv(8, 4, CAVE_HINT_MAZE);
            if (roomX == 12) return cv(12, 4, CAVE_TAKE_ANY_1);
            return biome(roomX, roomY, "MoblinRed", "MoblinBlue", "OctorokRed");
        }

        // ==================== ROW 5: Graveyard/Forest ====================
        if (roomY == 5) {
            if (roomX == 3)  return fy(3, 5, CAVE_FAIRY_4);
            if (roomX == 4)  return new RoomDef(4, 5, new EnemySpawn[]{
                    e("MoblinRed", 80, 60), e("MoblinRed", 160, 100),
                }, new RoomSecret[]{ new RoomSecret(SecretType.BURN_BUSH, 10, 6, CAVE_SHOP_B2) },
                -1, -1, -1, -1, false);
            if (roomX == 5)  return cv(5, 5, CAVE_SHOP_C);
            if (roomX == 6)  return fy(6, 5, CAVE_FAIRY_1);
            if (roomX == 8)  return new RoomDef(8, 5, new EnemySpawn[]{
                    e("Ghini", 80, 48),
                }, new RoomSecret[]{ new RoomSecret(SecretType.PUSH_GRAVE, 5, 4, CAVE_HINT_DODONGO) },
                -1, -1, -1, -1, false);
            if (roomX == 9)  return new RoomDef(9, 5, new EnemySpawn[]{
                    e("Ghini", 60, 48), e("Ghini", 150, 80), e("Ghini", 100, 120),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 10) return new RoomDef(10, 5, new EnemySpawn[]{
                    e("OctorokBlue", 64, 48), e("OctorokBlue", 160, 80),
                }, new RoomSecret[]{ new RoomSecret(SecretType.BOMB_WALL, 7, 0, CAVE_COAST_SECRET) },
                -1, -1, -1, -1, false);
            if (roomX == 13) return cv(13, 5, CAVE_HINT_FAIRY);
            if (roomX >= 7 && roomX <= 12) {
                double[] p1 = sp(roomX, roomY, 0), p2 = sp(roomX, roomY, 1);
                return new RoomDef(roomX, roomY, new EnemySpawn[]{
                    e("Ghini", p1[0], p1[1]), e("Ghini", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            return biome(roomX, roomY, "LeeverBlue", "LeeverRed", null);
        }

        // ==================== ROW 6: Plains ====================
        if (roomY == 6) {
            if (roomX == 0)  return cv(0, 6, CAVE_HINT_GRAVE);
            if (roomX == 4)  return cv(4, 6, CAVE_MONEY_GAME);
            if (roomX == 7)  return cv(7, 6, CAVE_SHOP_A3);
            if (roomX == 8)  return cv(8, 6, CAVE_HINT_DODONGO);
            if (roomX == 10) return cv(10, 6, CAVE_SHOP_B3);
            return biome(roomX, roomY, "Peahat", "MoblinRed", "OctorokRed");
        }

        // ==================== ROW 7: Starting Area ====================
        if (roomY == 7) {
            if (roomX == 3)  return cv(3, 7, CAVE_POTION_SHOP);
            if (roomX == 7)  return cv(7, 7, CAVE_SWORD);
            if (roomX == 12) return cv(12, 7, CAVE_SHOP_B2);
            if (roomX == 15) return cv(15, 7, CAVE_HINT_SPECTACLE);
            return biome(roomX, roomY, "OctorokRed", "OctorokBlue", null);
        }

        return biome(roomX, roomY, "OctorokRed", "OctorokBlue", null);
    }

    // ======================== Helper factory methods ========================

    /** Simple cave room (no enemies, cave entrance at tile 7,3). */
    private static RoomDef cv(int x, int y, int caveId) {
        return new RoomDef(x, y, NO_ENEMIES, NO_SECRETS, caveId, 7, 3, -1, true);
    }

    /** Simple dungeon entrance room. */
    private static RoomDef dg(int x, int y, int dungeonId) {
        return new RoomDef(x, y, NO_ENEMIES, NO_SECRETS, -1, -1, -1, dungeonId, true);
    }

    /** Fairy fountain room (entrance at tile 7,5). */
    private static RoomDef fy(int x, int y, int caveId) {
        return new RoomDef(x, y, NO_ENEMIES,
            new RoomSecret[]{ new RoomSecret(SecretType.FAIRY_FOUNTAIN, 7, 5, -1) },
            caveId, 7, 5, -1, true);
    }

    /** Deterministic spawn position from room coords + index. */
    private static double[] sp(int rx, int ry, int idx) {
        int seed = rx * 17 + ry * 31 + idx * 53;
        double x = 40 + ((seed & 0xFF) % 10) * 18;
        double y = 32 + (((seed >> 8) & 0xFF) % 7) * 18;
        return new double[]{x, y};
    }

    /** Biome-based room with 2-3 enemy types. */
    private static RoomDef biome(int rx, int ry, String e1, String e2, String e3) {
        double[] p1 = sp(rx, ry, 0), p2 = sp(rx, ry, 1), p3 = sp(rx, ry, 2);
        if (e3 != null) {
            return new RoomDef(rx, ry, new EnemySpawn[]{
                e(e1, p1[0], p1[1]), e(e2, p2[0], p2[1]), e(e3, p3[0], p3[1]),
            }, NO_SECRETS, -1, -1, -1, -1, false);
        }
        return new RoomDef(rx, ry, new EnemySpawn[]{
            e(e1, p1[0], p1[1]), e(e2, p2[0], p2[1]),
            e(((rx + ry) % 2 == 0) ? e1 : e2, p3[0], p3[1]),
        }, NO_SECRETS, -1, -1, -1, -1, false);
    }

    /**
     * Returns the list of all dungeon entrance room coordinates for the 1st quest.
     * Index 0 = dungeon 1, index 8 = dungeon 9.
     */
    public static int[][] getDungeonEntrances() {
        return new int[][] {
            { 7, 3},  // Dungeon 1
            {12, 3},  // Dungeon 2
            { 4, 4},  // Dungeon 3
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
