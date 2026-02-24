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
            if (roomX == 0) return new RoomDef(0, 0, new EnemySpawn[]{
                    e("LynelRed", 60, 56), e("LynelRed", 140, 88),
                    e("TektiteRed", 190, 48), e("TektiteRed", 100, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 1) return new RoomDef(1, 0, new EnemySpawn[]{
                    e("LynelBlue", 80, 48), e("LynelRed", 160, 96),
                    e("TektiteRed", 50, 120), e("TektiteBlue", 200, 64),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 2) return new RoomDef(2, 0, new EnemySpawn[]{
                    e("TektiteRed", 56, 40), e("TektiteRed", 120, 80),
                    e("TektiteBlue", 184, 56), e("LynelRed", 140, 130),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 3) return cv(3, 0, CAVE_HINT_TRIFORCE);
            if (roomX == 4) return new RoomDef(4, 0, new EnemySpawn[]{
                    e("LynelRed", 72, 64), e("LynelBlue", 168, 104),
                    e("TektiteBlue", 120, 40), e("TektiteRed", 48, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 5) return new RoomDef(5, 0, new EnemySpawn[]{
                    e("TektiteBlue", 64, 48), e("TektiteBlue", 160, 48),
                    e("LynelRed", 112, 96), e("TektiteRed", 192, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 6) return dg(6, 0, 9);
            if (roomX == 7) return new RoomDef(7, 0, new EnemySpawn[]{
                    e("LynelBlue", 80, 56), e("LynelBlue", 176, 96),
                    e("TektiteRed", 48, 120), e("TektiteBlue", 200, 40),
                    e("TektiteRed", 128, 72),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 8) return new RoomDef(8, 0, new EnemySpawn[]{
                    e("LynelRed", 60, 48), e("TektiteBlue", 150, 64),
                    e("TektiteRed", 100, 120), e("LynelRed", 192, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 9) return new RoomDef(9, 0, new EnemySpawn[]{
                    e("TektiteRed", 48, 40), e("TektiteBlue", 128, 56),
                    e("LynelRed", 80, 104), e("LynelBlue", 184, 88),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 10) return new RoomDef(10, 0, new EnemySpawn[]{
                    e("LynelBlue", 72, 64), e("LynelBlue", 168, 48),
                    e("TektiteRed", 120, 128), e("TektiteBlue", 200, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 11) return new RoomDef(11, 0, new EnemySpawn[]{
                    e("LynelRed", 56, 56), e("TektiteBlue", 144, 40),
                    e("TektiteRed", 96, 112), e("LynelRed", 192, 80),
                    e("TektiteBlue", 48, 136),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 12) return new RoomDef(12, 0, NO_ENEMIES,
                new RoomSecret[]{ new RoomSecret(SecretType.PUSH_GRAVE, 7, 3, CAVE_MAGICAL_SWORD) },
                CAVE_MAGICAL_SWORD, 7, 3, -1, true);
            if (roomX == 13) return new RoomDef(13, 0, new EnemySpawn[]{
                    e("LynelBlue", 64, 48), e("LynelRed", 176, 72),
                    e("TektiteRed", 112, 120), e("TektiteBlue", 48, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 14) return new RoomDef(14, 0, new EnemySpawn[]{
                    e("TektiteBlue", 56, 40), e("TektiteRed", 160, 56),
                    e("LynelRed", 100, 96), e("LynelBlue", 192, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 15) return new RoomDef(15, 0, new EnemySpawn[]{
                    e("LynelRed", 72, 56), e("LynelBlue", 152, 88),
                    e("TektiteBlue", 200, 40), e("TektiteRed", 48, 128),
                    e("LynelRed", 120, 136),
                }, NO_SECRETS, -1, -1, -1, -1, false);
        }

        // ==================== ROW 1: Mountains ====================
        if (roomY == 1) {
            if (roomX == 0) return new RoomDef(0, 1, new EnemySpawn[]{
                    e("LynelRed", 64, 56), e("MoblinBlue", 160, 80),
                    e("TektiteRed", 112, 128), e("Peahat", 192, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 1)  return cv(1, 1, CAVE_SHOP_A1);
            if (roomX == 2)  return cv(2, 1, CAVE_WHITE_SWORD);
            if (roomX == 3) return new RoomDef(3, 1, new EnemySpawn[]{
                    e("TektiteRed", 56, 48), e("TektiteBlue", 144, 64),
                    e("LynelRed", 100, 112), e("MoblinBlue", 200, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 4) return new RoomDef(4, 1, new EnemySpawn[]{
                    e("MoblinBlue", 72, 56), e("MoblinBlue", 168, 88),
                    e("TektiteRed", 48, 120), e("Peahat", 200, 48),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 5) return new RoomDef(5, 1, new EnemySpawn[]{
                    e("LynelRed", 80, 64), e("TektiteBlue", 160, 40),
                    e("TektiteRed", 120, 104), e("MoblinBlue", 48, 88),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 6)  return cv(6, 1, CAVE_HINT_TREE);
            if (roomX == 7) return new RoomDef(7, 1, new EnemySpawn[]{
                    e("Peahat", 60, 48), e("Peahat", 152, 72),
                    e("LynelRed", 104, 120), e("TektiteRed", 192, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 8) return new RoomDef(8, 1, new EnemySpawn[]{
                    e("TektiteBlue", 48, 40), e("TektiteBlue", 176, 56),
                    e("MoblinBlue", 112, 96), e("LynelRed", 64, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 9)  return cv(9, 1, CAVE_SHOP_B1);
            if (roomX == 10) return dg(10, 1, 5);
            if (roomX == 11) return new RoomDef(11, 1, new EnemySpawn[]{
                    e("LynelRed", 56, 56), e("LynelBlue", 168, 80),
                    e("TektiteRed", 112, 40), e("MoblinBlue", 200, 120),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 12) return dg(12, 1, 7);
            if (roomX == 13) return new RoomDef(13, 1, new EnemySpawn[]{
                    e("MoblinBlue", 72, 48), e("TektiteRed", 160, 64),
                    e("Peahat", 120, 112), e("LynelRed", 48, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 14) return cv(14, 1, CAVE_DOOR_REPAIR);
            if (roomX == 15) return new RoomDef(15, 1, new EnemySpawn[]{
                    e("TektiteBlue", 64, 40), e("TektiteRed", 184, 56),
                    e("LynelRed", 128, 96), e("MoblinBlue", 56, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
        }

        // ==================== ROW 2: Lake/Mountains ====================
        if (roomY == 2) {
            if (roomX == 0)  return cv(0, 2, CAVE_SHOP_A2);
            if (roomX == 1) return new RoomDef(1, 2, new EnemySpawn[]{
                    e("TektiteBlue", 64, 48), e("TektiteBlue", 160, 72),
                    e("OctorokBlue", 112, 120), e("Zola", 192, 88),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 2)  return dg(2, 2, 6);
            if (roomX == 3)  return cv(3, 2, CAVE_LETTER);
            if (roomX == 4) return new RoomDef(4, 2, new EnemySpawn[]{
                    e("OctorokBlue", 56, 56), e("OctorokBlue", 168, 80),
                    e("TektiteBlue", 104, 40), e("TektiteBlue", 200, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 5)  return dg(5, 2, 4);
            if (roomX == 6) return new RoomDef(6, 2, new EnemySpawn[]{
                    e("TektiteBlue", 72, 48), e("Zola", 144, 96),
                    e("OctorokBlue", 48, 112), e("TektiteBlue", 192, 56),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 7)  return fy(7, 2, CAVE_FAIRY_1);
            if (roomX == 8)  return cv(8, 2, CAVE_TAKE_ANY_2);
            if (roomX == 9) return new RoomDef(9, 2, new EnemySpawn[]{
                    e("OctorokBlue", 60, 48), e("OctorokBlue", 152, 64),
                    e("TektiteBlue", 112, 112), e("Zola", 184, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 10) return new RoomDef(10, 2, new EnemySpawn[]{
                    e("TektiteBlue", 48, 40), e("TektiteBlue", 176, 72),
                    e("OctorokBlue", 128, 104), e("OctorokBlue", 64, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 11) return fy(11, 2, CAVE_FAIRY_2);
            if (roomX == 12) return new RoomDef(12, 2, new EnemySpawn[]{
                    e("Zola", 96, 72), e("Zola", 176, 96),
                    e("OctorokBlue", 56, 112), e("TektiteBlue", 144, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 13) return new RoomDef(13, 2, new EnemySpawn[]{
                    e("OctorokBlue", 72, 56), e("TektiteBlue", 160, 48),
                    e("OctorokBlue", 120, 104), e("TektiteBlue", 48, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 14) return new RoomDef(14, 2, new EnemySpawn[]{
                    e("Armos", 64, 48), e("Armos", 96, 48), e("Armos", 128, 48),
                    e("Armos", 160, 48), e("Armos", 192, 48),
                    e("Armos", 64, 80), e("Armos", 96, 80), e("Armos", 128, 80),
                    e("Armos", 160, 80), e("Armos", 192, 80),
                }, new RoomSecret[]{ new RoomSecret(SecretType.PUSH_ROCK, 12, 3, -1) },
                -1, -1, -1, -1, false);
            if (roomX == 15) return new RoomDef(15, 2, new EnemySpawn[]{
                    e("TektiteBlue", 56, 44), e("OctorokBlue", 152, 68),
                    e("Zola", 104, 100), e("TektiteBlue", 192, 132),
                }, NO_SECRETS, -1, -1, -1, -1, false);
        }

        // ==================== ROW 3: Lake Hylia ====================
        if (roomY == 3) {
            if (roomX == 0)  return dg(0, 3, 8);
            if (roomX == 1) return new RoomDef(1, 3, new EnemySpawn[]{
                    e("TektiteBlue", 64, 48), e("OctorokRed", 160, 80),
                    e("TektiteBlue", 112, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 2) return new RoomDef(2, 3, new EnemySpawn[]{
                    e("OctorokRed", 56, 56), e("OctorokRed", 168, 72),
                    e("TektiteBlue", 104, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 3) return new RoomDef(3, 3, new EnemySpawn[]{
                    e("Zola", 96, 64), e("Zola", 176, 96),
                    e("OctorokRed", 48, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 4) return new RoomDef(4, 3, new EnemySpawn[]{
                    e("TektiteBlue", 72, 48), e("TektiteBlue", 160, 56),
                    e("OctorokRed", 120, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 5) return new RoomDef(5, 3, new EnemySpawn[]{
                    e("OctorokRed", 60, 64), e("TektiteBlue", 152, 40),
                    e("Zola", 112, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 6) {
                double[] p1 = sp(6, 3, 0), p2 = sp(6, 3, 1);
                return new RoomDef(6, 3, new EnemySpawn[]{
                    e("Zola", p1[0], p1[1]), e("Zola", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            if (roomX == 7)  return dg(7, 3, 1);
            if (roomX == 8) {
                double[] p1 = sp(8, 3, 0), p2 = sp(8, 3, 1);
                return new RoomDef(8, 3, new EnemySpawn[]{
                    e("Zola", p1[0], p1[1]), e("Zola", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            if (roomX == 9) {
                double[] p1 = sp(9, 3, 0), p2 = sp(9, 3, 1);
                return new RoomDef(9, 3, new EnemySpawn[]{
                    e("Zola", p1[0], p1[1]), e("Zola", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            if (roomX == 10) {
                double[] p1 = sp(10, 3, 0), p2 = sp(10, 3, 1);
                return new RoomDef(10, 3, new EnemySpawn[]{
                    e("Zola", p1[0], p1[1]), e("Zola", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            if (roomX == 11) return cv(11, 3, CAVE_HINT_SWORD);
            if (roomX == 12) return dg(12, 3, 2);
            if (roomX == 13) return new RoomDef(13, 3, new EnemySpawn[]{
                    e("OctorokRed", 64, 56), e("TektiteBlue", 168, 48),
                    e("OctorokRed", 112, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 14) return new RoomDef(14, 3, new EnemySpawn[]{
                    e("Zola", 100, 60), e("Zola", 170, 90),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 15) return fy(15, 3, CAVE_FAIRY_3);
        }

        // ==================== ROW 4: Forest ====================
        if (roomY == 4) {
            if (roomX == 0) return new RoomDef(0, 4, new EnemySpawn[]{
                    e("MoblinRed", 64, 56), e("MoblinRed", 168, 80),
                    e("OctorokRed", 112, 120), e("MoblinBlue", 48, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 1)  return cv(1, 4, CAVE_HINT_PENINSULA);
            if (roomX == 2)  return new RoomDef(2, 4, new EnemySpawn[]{
                    e("Armos", 64, 48), e("Armos", 128, 48), e("Armos", 192, 48),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 3) return new RoomDef(3, 4, new EnemySpawn[]{
                    e("MoblinBlue", 72, 48), e("MoblinRed", 160, 72),
                    e("OctorokRed", 104, 112), e("MoblinRed", 192, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 4)  return dg(4, 4, 3);
            if (roomX == 5) return new RoomDef(5, 4, new EnemySpawn[]{
                    e("MoblinRed", 56, 56), e("MoblinBlue", 152, 64),
                    e("OctorokRed", 96, 112), e("MoblinRed", 200, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 6)  return cv(6, 4, CAVE_SHOP_D);
            if (roomX == 7) return new RoomDef(7, 4, new EnemySpawn[]{
                    e("OctorokRed", 64, 48), e("MoblinRed", 168, 80),
                    e("MoblinBlue", 112, 128), e("OctorokRed", 48, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 8)  return cv(8, 4, CAVE_HINT_MAZE);
            if (roomX == 9) return new RoomDef(9, 4, new EnemySpawn[]{
                    e("MoblinRed", 72, 56), e("MoblinRed", 160, 48),
                    e("MoblinBlue", 120, 104), e("OctorokRed", 192, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 10) return new RoomDef(10, 4, new EnemySpawn[]{
                    e("MoblinBlue", 56, 48), e("MoblinRed", 176, 72),
                    e("OctorokRed", 104, 112), e("MoblinRed", 48, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 11) return new RoomDef(11, 4, new EnemySpawn[]{
                    e("MoblinRed", 64, 56), e("MoblinBlue", 168, 64),
                    e("OctorokRed", 128, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 12) return cv(12, 4, CAVE_TAKE_ANY_1);
            if (roomX == 13) return new RoomDef(13, 4, new EnemySpawn[]{
                    e("OctorokRed", 56, 48), e("MoblinRed", 160, 80),
                    e("MoblinBlue", 112, 120), e("OctorokRed", 200, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 14) return new RoomDef(14, 4, new EnemySpawn[]{
                    e("MoblinRed", 72, 48), e("MoblinRed", 168, 72),
                    e("MoblinBlue", 104, 112), e("OctorokRed", 192, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 15) return new RoomDef(15, 4, new EnemySpawn[]{
                    e("MoblinBlue", 56, 56), e("MoblinRed", 152, 48),
                    e("OctorokRed", 120, 104), e("MoblinRed", 200, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
        }

        // ==================== ROW 5: Graveyard/Forest ====================
        if (roomY == 5) {
            if (roomX == 0) return new RoomDef(0, 5, new EnemySpawn[]{
                    e("LeeverBlue", 64, 56), e("LeeverBlue", 160, 80),
                    e("LeeverRed", 112, 120), e("OctorokBlue", 48, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 1) return new RoomDef(1, 5, new EnemySpawn[]{
                    e("LeeverBlue", 72, 48), e("LeeverRed", 168, 72),
                    e("OctorokBlue", 104, 112), e("LeeverBlue", 192, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 2) return new RoomDef(2, 5, new EnemySpawn[]{
                    e("OctorokBlue", 56, 56), e("LeeverBlue", 160, 64),
                    e("LeeverRed", 120, 104), e("OctorokBlue", 48, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 3)  return fy(3, 5, CAVE_FAIRY_4);
            if (roomX == 4)  return new RoomDef(4, 5, new EnemySpawn[]{
                    e("MoblinRed", 80, 60), e("MoblinRed", 160, 100),
                }, new RoomSecret[]{ new RoomSecret(SecretType.BURN_BUSH, 10, 6, CAVE_SHOP_B2) },
                -1, -1, -1, -1, false);
            if (roomX == 5)  return cv(5, 5, CAVE_SHOP_C);
            if (roomX == 6)  return fy(6, 5, CAVE_FAIRY_1);
            if (roomX == 7) {
                double[] p1 = sp(7, 5, 0), p2 = sp(7, 5, 1);
                return new RoomDef(7, 5, new EnemySpawn[]{
                    e("Ghini", p1[0], p1[1]), e("Ghini", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
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
            if (roomX == 11) {
                double[] p1 = sp(11, 5, 0), p2 = sp(11, 5, 1);
                return new RoomDef(11, 5, new EnemySpawn[]{
                    e("Ghini", p1[0], p1[1]), e("Ghini", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            if (roomX == 12) {
                double[] p1 = sp(12, 5, 0), p2 = sp(12, 5, 1);
                return new RoomDef(12, 5, new EnemySpawn[]{
                    e("Ghini", p1[0], p1[1]), e("Ghini", p2[0], p2[1]),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            }
            if (roomX == 13) return cv(13, 5, CAVE_HINT_FAIRY);
            if (roomX == 14) return new RoomDef(14, 5, new EnemySpawn[]{
                    e("LeeverBlue", 64, 48), e("LeeverBlue", 168, 72),
                    e("OctorokBlue", 112, 112), e("LeeverRed", 192, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 15) return new RoomDef(15, 5, new EnemySpawn[]{
                    e("OctorokBlue", 56, 56), e("LeeverBlue", 152, 48),
                    e("LeeverRed", 104, 104), e("LeeverBlue", 200, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
        }

        // ==================== ROW 6: Plains ====================
        if (roomY == 6) {
            if (roomX == 0)  return cv(0, 6, CAVE_HINT_GRAVE);
            if (roomX == 1) return new RoomDef(1, 6, new EnemySpawn[]{
                    e("Peahat", 64, 48), e("MoblinRed", 160, 80),
                    e("OctorokRed", 112, 120), e("Peahat", 192, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 2) return new RoomDef(2, 6, new EnemySpawn[]{
                    e("MoblinRed", 72, 56), e("OctorokRed", 168, 72),
                    e("Peahat", 104, 112), e("MoblinRed", 48, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 3) return new RoomDef(3, 6, new EnemySpawn[]{
                    e("OctorokRed", 56, 48), e("Peahat", 160, 64),
                    e("MoblinRed", 120, 104), e("OctorokRed", 192, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 4)  return cv(4, 6, CAVE_MONEY_GAME);
            if (roomX == 5) return new RoomDef(5, 6, new EnemySpawn[]{
                    e("Peahat", 64, 56), e("Peahat", 176, 48),
                    e("OctorokRed", 112, 104), e("MoblinRed", 48, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 6) return new RoomDef(6, 6, new EnemySpawn[]{
                    e("MoblinRed", 72, 48), e("MoblinRed", 168, 80),
                    e("Peahat", 120, 128), e("OctorokRed", 192, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 7)  return cv(7, 6, CAVE_SHOP_A3);
            if (roomX == 8)  return cv(8, 6, CAVE_HINT_DODONGO);
            if (roomX == 9) return new RoomDef(9, 6, new EnemySpawn[]{
                    e("OctorokRed", 56, 56), e("Peahat", 152, 48),
                    e("MoblinRed", 104, 112), e("OctorokRed", 200, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 10) return cv(10, 6, CAVE_SHOP_B3);
            if (roomX == 11) return new RoomDef(11, 6, new EnemySpawn[]{
                    e("Peahat", 64, 48), e("MoblinRed", 168, 72),
                    e("OctorokRed", 112, 112), e("Peahat", 48, 96),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 12) return new RoomDef(12, 6, new EnemySpawn[]{
                    e("MoblinRed", 72, 56), e("OctorokRed", 160, 64),
                    e("Peahat", 120, 104), e("MoblinRed", 192, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 13) return new RoomDef(13, 6, new EnemySpawn[]{
                    e("OctorokRed", 56, 48), e("OctorokRed", 168, 80),
                    e("Peahat", 104, 120), e("MoblinRed", 200, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 14) return new RoomDef(14, 6, new EnemySpawn[]{
                    e("Peahat", 64, 56), e("Peahat", 176, 64),
                    e("MoblinRed", 128, 104), e("OctorokRed", 48, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 15) return new RoomDef(15, 6, new EnemySpawn[]{
                    e("MoblinRed", 72, 48), e("OctorokRed", 160, 72),
                    e("Peahat", 112, 112), e("OctorokRed", 192, 40),
                }, NO_SECRETS, -1, -1, -1, -1, false);
        }

        // ==================== ROW 7: Starting Area ====================
        if (roomY == 7) {
            if (roomX == 0) return new RoomDef(0, 7, new EnemySpawn[]{
                    e("OctorokRed", 64, 56), e("OctorokRed", 160, 80),
                    e("TektiteRed", 112, 120),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 1) return new RoomDef(1, 7, new EnemySpawn[]{
                    e("OctorokRed", 72, 48), e("OctorokBlue", 168, 72),
                    e("OctorokRed", 120, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 2) return new RoomDef(2, 7, new EnemySpawn[]{
                    e("OctorokBlue", 56, 56), e("OctorokRed", 152, 64),
                    e("TektiteRed", 104, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 3)  return cv(3, 7, CAVE_POTION_SHOP);
            if (roomX == 4) return new RoomDef(4, 7, new EnemySpawn[]{
                    e("OctorokRed", 64, 48), e("OctorokRed", 168, 80),
                    e("OctorokBlue", 112, 128),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 5) return new RoomDef(5, 7, new EnemySpawn[]{
                    e("OctorokRed", 56, 56), e("OctorokBlue", 160, 48),
                    e("OctorokRed", 120, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 6) return new RoomDef(6, 7, new EnemySpawn[]{
                    e("OctorokRed", 72, 48), e("OctorokRed", 176, 72),
                    e("TektiteRed", 128, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 7)  return cv(7, 7, CAVE_SWORD);
            if (roomX == 8) return new RoomDef(8, 7, new EnemySpawn[]{
                    e("OctorokRed", 64, 56), e("OctorokBlue", 168, 64),
                    e("OctorokRed", 104, 120),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 9) return new RoomDef(9, 7, new EnemySpawn[]{
                    e("OctorokRed", 48, 48), e("OctorokRed", 160, 80),
                    e("OctorokBlue", 112, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 10) return new RoomDef(10, 7, new EnemySpawn[]{
                    e("TektiteRed", 56, 40), e("OctorokRed", 152, 72),
                    e("OctorokRed", 104, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 11) return new RoomDef(11, 7, new EnemySpawn[]{
                    e("OctorokBlue", 72, 56), e("OctorokRed", 168, 48),
                    e("OctorokRed", 120, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 12) return cv(12, 7, CAVE_SHOP_B2);
            if (roomX == 13) return new RoomDef(13, 7, new EnemySpawn[]{
                    e("OctorokRed", 56, 48), e("OctorokBlue", 160, 72),
                    e("TektiteRed", 112, 112),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 14) return new RoomDef(14, 7, new EnemySpawn[]{
                    e("OctorokRed", 64, 56), e("OctorokRed", 176, 64),
                    e("OctorokBlue", 128, 104),
                }, NO_SECRETS, -1, -1, -1, -1, false);
            if (roomX == 15) return cv(15, 7, CAVE_HINT_SPECTACLE);
        }

        // Fallback — should never be reached since all 128 rooms are defined above
        return biome(roomX, roomY, "OctorokRed", "OctorokBlue", null);
    }

    // ======================== Helper factory methods ========================

    /** Simple cave room — entrance position auto-detected from collision data. */
    private static RoomDef cv(int x, int y, int caveId) {
        int[] pos = OverworldCollisionData.findCaveEntrance(x, y);
        int tx = (pos != null) ? pos[0] : -1;
        int ty = (pos != null) ? pos[1] : -1;
        return new RoomDef(x, y, NO_ENEMIES, NO_SECRETS, caveId, tx, ty, -1, true);
    }

    /** Simple dungeon entrance room with entrance at specified tile position. */
    private static RoomDef dg(int x, int y, int dungeonId, int entranceTX, int entranceTY) {
        return new RoomDef(x, y, NO_ENEMIES, NO_SECRETS, -1, entranceTX, entranceTY, dungeonId, true);
    }

    /** Simple dungeon entrance room — entrance position auto-detected from collision data. */
    private static RoomDef dg(int x, int y, int dungeonId) {
        int[] pos = OverworldCollisionData.findCaveEntrance(x, y);
        int tx = (pos != null) ? pos[0] : -1;
        int ty = (pos != null) ? pos[1] : -1;
        return dg(x, y, dungeonId, tx, ty);
    }

    /** Fairy fountain room — entrance position auto-detected from collision data. */
    private static RoomDef fy(int x, int y, int caveId) {
        int[] pos = OverworldCollisionData.findCaveEntrance(x, y);
        int tx = (pos != null) ? pos[0] : -1;
        int ty = (pos != null) ? pos[1] : -1;
        return new RoomDef(x, y, NO_ENEMIES,
            new RoomSecret[]{ new RoomSecret(SecretType.FAIRY_FOUNTAIN, tx, ty, -1) },
            caveId, tx, ty, -1, true);
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
        int[][] entrances = getDungeonEntrances();
        int[][] tiles = new int[9][];
        for (int i = 0; i < 9; i++) {
            int[] pos = OverworldCollisionData.findCaveEntrance(entrances[i][0], entrances[i][1]);
            tiles[i] = (pos != null) ? pos : new int[]{-1, -1};
        }
        return tiles;
    }
}
