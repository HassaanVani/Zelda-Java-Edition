package zelda;

import java.util.HashMap;

public class CollisionMap {
    private static final int TILES_X = ZeldaRoom.TILES_X;
    private static final int TILES_Y = ZeldaRoom.TILES_Y;
    private static final int TILE_SIZE = ZeldaRoom.TILE_SIZE;

    private HashMap<String, int[][]> roomCollisions = new HashMap<>();
    private HashMap<String, int[]> detectedEntrances = new HashMap<>();
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

        // Detect entrance from pre-computed CAVE tiles or RoomData positions
        RoomData.RoomDef def = RoomData.getRoomDef(roomX, roomY);
        if (def != null) {
            boolean hasEntrance = (def.caveId >= 0 || def.dungeonId >= 0);
            if (hasEntrance) {
                // Check if pre-computed grid already has CAVE tiles (type 5)
                int[] cavePos = findCaveTileInGrid(grid);
                if (cavePos != null) {
                    // Use the center of the CAVE tile cluster as entrance
                    int[] center = findCaveClusterCenter(grid, cavePos[0], cavePos[1]);
                    detectedEntrances.put(key, center);
                    // Ensure approach path below cave entrance is walkable
                    ensureApproachPath(grid, center[0], center[1]);
                } else {
                    // No CAVE tiles in pre-computed data — try image-based dark
                    // opening detection (finds the black entrance rectangle)
                    int[] darkPos = null;
                    if (renderer != null) {
                        darkPos = renderer.findDarkOpening(roomX, roomY);
                    }
                    if (darkPos != null) {
                        grid[darkPos[0]][darkPos[1]] = TileType.CAVE.ordinal();
                        detectedEntrances.put(key, darkPos);
                        ensureApproachPath(grid, darkPos[0], darkPos[1]);
                    } else {
                        // Last resort: use RoomData position (hidden caves etc.)
                        int ftx = (def.caveTileX >= 0) ? def.caveTileX : 7;
                        int fty = (def.caveTileY >= 0) ? def.caveTileY : 3;
                        detectedEntrances.put(key, new int[]{ftx, fty});
                        markEntranceWalkable(grid, ftx, fty);
                    }
                }
            }
        }

        roomCollisions.put(key, grid);
        return grid;
    }

    /**
     * Returns the auto-detected entrance tile [tileX, tileY] for the given room,
     * or null if no entrance was detected from the sprite map.
     */
    public int[] getDetectedEntrance(int roomX, int roomY) {
        String key = roomX + "," + roomY;
        // Ensure the grid has been generated (which triggers detection)
        getCollisionGrid(roomX, roomY);
        return detectedEntrances.get(key);
    }

    /** Find first CAVE tile (type 5) in a grid. */
    private int[] findCaveTileInGrid(int[][] grid) {
        int caveOrd = TileType.CAVE.ordinal();
        for (int ty = 0; ty < TILES_Y; ty++) {
            for (int tx = 0; tx < TILES_X; tx++) {
                if (grid[tx][ty] == caveOrd) {
                    return new int[]{tx, ty};
                }
            }
        }
        return null;
    }

    /** Find center of a horizontal cluster of CAVE tiles starting near (startX, startY). */
    private int[] findCaveClusterCenter(int[][] grid, int startX, int startY) {
        int caveOrd = TileType.CAVE.ordinal();
        int minX = startX, maxX = startX;
        // Expand left
        while (minX > 0 && grid[minX - 1][startY] == caveOrd) minX--;
        // Expand right
        while (maxX < TILES_X - 1 && grid[maxX + 1][startY] == caveOrd) maxX++;
        return new int[]{(minX + maxX) / 2, startY};
    }

    /** Ensure tiles below a cave entrance are walkable for player approach. */
    private void ensureApproachPath(int[][] grid, int tx, int ty) {
        int floor = TileType.FLOOR.ordinal();
        int wallOrd = TileType.WALL.ordinal();
        for (int dy = 1; dy <= 2; dy++) {
            int ny = ty + dy;
            if (ny >= 0 && ny < TILES_Y) {
                for (int dx = -1; dx <= 1; dx++) {
                    int nx = tx + dx;
                    if (nx >= 0 && nx < TILES_X && grid[nx][ny] == wallOrd) {
                        grid[nx][ny] = floor;
                    }
                }
            }
        }
    }

    /** Mark entrance tile and surrounding tiles as walkable FLOOR. */
    private void markEntranceWalkable(int[][] grid, int tx, int ty) {
        int floor = TileType.FLOOR.ordinal();
        // Mark a 3-wide x 3-tall area centered on entrance as walkable
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                int nx = tx + dx;
                int ny = ty + dy;
                if (nx >= 0 && nx < TILES_X && ny >= 0 && ny < TILES_Y) {
                    grid[nx][ny] = floor;
                }
            }
        }
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
