package zelda;

import java.util.HashMap;

public class CollisionMap {
    private static final int TILES_X = 16;
    private static final int TILES_Y = 11;
    
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
        
        if (renderer != null) {
            int[][] grid = renderer.generateCollisionGrid(roomX, roomY);
            roomCollisions.put(key, grid);
            return grid;
        }
        
        return createDefaultGrid();
    }
    
    private int[][] createDefaultGrid() {
        int[][] grid = new int[TILES_X][TILES_Y];
        for (int x = 0; x < TILES_X; x++) {
            for (int y = 0; y < TILES_Y; y++) {
                if (x == 0 || x == TILES_X - 1 || y == 0 || y == TILES_Y - 1) {
                    grid[x][y] = 1;
                } else {
                    grid[x][y] = 0;
                }
            }
        }
        return grid;
    }
    
    public TileType getTileType(int roomX, int roomY, int tileX, int tileY) {
        if (tileX < 0 || tileX >= TILES_X || tileY < 0 || tileY >= TILES_Y) {
            return TileType.WALL;
        }
        
        int[][] grid = getCollisionGrid(roomX, roomY);
        int value = grid[tileX][tileY];
        
        return TileType.fromId(value);
    }
    
    public boolean isWalkable(int roomX, int roomY, int pixelX, int pixelY) {
        int tileX = pixelX / 16;
        int tileY = pixelY / 16;
        return getTileType(roomX, roomY, tileX, tileY).walkable;
    }
    
    public void setCustomCollision(int roomX, int roomY, int[][] grid) {
        roomCollisions.put(roomX + "," + roomY, grid);
    }
}
