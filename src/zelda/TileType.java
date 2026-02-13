package zelda;

public enum TileType {
    // --- Original types (ordinals 0-9, do not reorder) ---
    FLOOR(true, false),
    WALL(false, false),
    WATER(false, true),
    TREE(false, false),
    SAND(true, false),
    CAVE(true, true),
    STAIRS(true, true),
    GRAVE(false, false),
    DOCK(true, false),
    BRIDGE(true, false),

    // --- Overworld secret types ---
    BOMBABLE_WALL(false, true),     // rock face that opens when bombed
    BURNABLE_BUSH(false, true),     // bush destroyed by candle, reveals stairway
    PUSH_ROCK(false, true),         // pushable rock (some need Power Bracelet)
    PUSH_GRAVE(false, true),        // pushable gravestone revealing stairway

    // --- Dungeon-specific types ---
    DUNGEON_FLOOR(true, false),     // walkable dungeon tile
    DUNGEON_WALL(false, false),     // solid dungeon wall
    DUNGEON_BLOCK(false, true),     // pushable block in dungeons
    DUNGEON_DOOR_OPEN(true, true),  // open door passage
    DUNGEON_DOOR_LOCKED(false, true), // locked door (requires key)
    DUNGEON_DOOR_BOSS(false, true), // boss-locked door (requires boss key)
    DUNGEON_DOOR_BOMBED(false, true), // bombable dungeon wall
    DUNGEON_STAIRS(true, true),     // stairway in dungeon
    DARK(true, false),              // dark room tile (walkable but not visible without candle)

    // --- Traversal item tiles ---
    RAFT_DOCK(true, true),          // dock where raft auto-activates
    LADDER_GAP(false, true),        // 1-tile gap bridgeable by stepladder

    // --- Misc ---
    STATUE(false, false),           // decorative statue (Armos activation point)
    PIT(false, false);              // pit/hole (not walkable)

    public final boolean walkable;
    public final boolean interactive;
    
    TileType(boolean walkable, boolean interactive) {
        this.walkable = walkable;
        this.interactive = interactive;
    }
    
    public static TileType fromId(int id) {
        if (id >= 0 && id < values().length) {
            return values()[id];
        }
        return WALL;
    }
}
