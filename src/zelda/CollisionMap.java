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

        // Auto-detect cave/dungeon entrance from sprite map and mark walkable
        RoomData.RoomDef def = RoomData.getRoomDef(roomX, roomY);
        if (def != null && renderer != null) {
            boolean hasEntrance = (def.caveId >= 0 || def.dungeonId >= 0);
            if (hasEntrance) {
                int[] opening = renderer.findDarkOpening(roomX, roomY);
                if (opening != null) {
                    detectedEntrances.put(key, opening);
                    markEntranceWalkable(grid, opening[0], opening[1]);
                } else {
                    // Fallback: find the topmost WALL tile adjacent to a
                    // walkable tile near the horizontal center — that's where
                    // a cave entrance would naturally appear
                    int[] fallback = findFallbackEntrance(grid);
                    if (fallback != null) {
                        detectedEntrances.put(key, fallback);
                        markEntranceWalkable(grid, fallback[0], fallback[1]);
                    } else {
                        // Last resort: use hardcoded position
                        int ftx = (def.caveId >= 0 && def.caveTileX >= 0) ? def.caveTileX : 7;
                        int fty = (def.caveId >= 0 && def.caveTileY >= 0) ? def.caveTileY : 3;
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

    /**
     * Fallback entrance finder: scan the collision grid for a WALL tile near the
     * horizontal center that has a FLOOR tile below it. This finds the boundary
     * between terrain and open ground, which is where a cave entrance would be.
     */
    private int[] findFallbackEntrance(int[][] grid) {
        int wallOrd = TileType.WALL.ordinal();
        int floorOrd = TileType.FLOOR.ordinal();
        int sandOrd = TileType.SAND.ordinal();

        int bestTX = -1, bestTY = -1;
        float bestScore = Float.MAX_VALUE;

        for (int ty = 1; ty < TILES_Y - 2; ty++) {
            for (int tx = 2; tx < TILES_X - 2; tx++) {
                if (grid[tx][ty] != wallOrd) continue;

                // Must have a walkable tile below (within 2 rows)
                boolean hasFloorBelow = false;
                for (int dy = 1; dy <= 2 && ty + dy < TILES_Y; dy++) {
                    int below = grid[tx][ty + dy];
                    if (below == floorOrd || below == sandOrd) {
                        hasFloorBelow = true;
                        break;
                    }
                }
                if (!hasFloorBelow) continue;

                // Score: prefer center, upper rows
                float centerDist = Math.abs(tx - TILES_X / 2.0f);
                float score = centerDist * 3 + ty * 1.5f;
                if (score < bestScore) {
                    bestScore = score;
                    bestTX = tx;
                    bestTY = ty;
                }
            }
        }
        return bestTX >= 0 ? new int[]{bestTX, bestTY} : null;
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
