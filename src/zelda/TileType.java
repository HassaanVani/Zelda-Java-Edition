package zelda;

public enum TileType {
    FLOOR(true, false),
    WALL(false, false),
    WATER(false, true),
    TREE(false, false),
    SAND(true, false),
    CAVE(true, true),
    STAIRS(true, true),
    GRAVE(false, false),
    DOCK(true, false),
    BRIDGE(true, false);
    
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
