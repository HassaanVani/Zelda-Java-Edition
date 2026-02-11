package zelda;

import java.util.HashMap;

public class CollisionMap {
    private static final int TILES_X = ZeldaRoom.TILES_X;
    private static final int TILES_Y = ZeldaRoom.TILES_Y;
    private static final int TILE_SIZE = ZeldaRoom.TILE_SIZE;

    private HashMap<String, int[][]> roomCollisions = new HashMap<>();
    private static OverworldRenderer renderer;

    public CollisionMap() {}

    public void setRenderer(OverworldRenderer r) {
        renderer = r;
    }

    public int[][] getCollisionGrid(int roomX, int roomY) {
        String key = roomX + "," + roomY;

        if (roomCollisions.containsKey(key)) {
            return roomCollisions.get(key);
        }

        int[][] grid;
        if (renderer != null) {
            grid = renderer.generateCollisionGrid(roomX, roomY);
        } else {
            grid = createDefaultGrid();
        }
        roomCollisions.put(key, grid);
        return grid;
    }

    private int[][] createDefaultGrid() {
        int[][] grid = new int[TILES_X][TILES_Y];
        for (int x = 0; x < TILES_X; x++) {
            for (int y = 0; y < TILES_Y; y++) {
                grid[x][y] = (x == 0 || x == TILES_X - 1 || y == 0 || y == TILES_Y - 1)
                    ? TileType.WALL.ordinal() : TileType.FLOOR.ordinal();
            }
        }
        return grid;
    }

    public TileType getTileType(int roomX, int roomY, int tileX, int tileY) {
        if (tileX < 0 || tileX >= TILES_X || tileY < 0 || tileY >= TILES_Y) {
            return TileType.WALL;
        }
        int[][] grid = getCollisionGrid(roomX, roomY);
        return TileType.fromId(grid[tileX][tileY]);
    }

    public boolean isWalkable(int roomX, int roomY, int pixelX, int pixelY) {
        int tileX = pixelX / TILE_SIZE;
        int tileY = pixelY / TILE_SIZE;
        return getTileType(roomX, roomY, tileX, tileY).walkable;
    }

    public void setCustomCollision(int roomX, int roomY, int[][] grid) {
        roomCollisions.put(roomX + "," + roomY, grid);
    }
}
