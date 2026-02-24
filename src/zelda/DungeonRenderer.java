package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class DungeonRenderer {
    private static final int DUNGEON_MAP_COLS = 16;
    private static final int DUNGEON_MAP_ROWS = 16;

    public static final int DISPLAY_WIDTH = 256;
    public static final int DISPLAY_HEIGHT = 176;

    private static final int ROOM_BORDER_PIXELS = 32;

    private BufferedImage dungeonMap;
    private int sourceRoomWidth;
    private int sourceRoomHeight;
    private boolean mapLoaded = false;

    private int currentDungeonLevel = 1;

    public DungeonRenderer() {
        loadDungeonMap();
    }

    private void loadDungeonMap() {
        try {
            File f = new File("sprites/Worlds/zelda-dungeons.png");
            if (f.exists()) {
                dungeonMap = ImageIO.read(f);
                mapLoaded = true;

                sourceRoomWidth = dungeonMap.getWidth() / DUNGEON_MAP_COLS;
                sourceRoomHeight = dungeonMap.getHeight() / DUNGEON_MAP_ROWS;
            }
        } catch (Exception e) {
            System.err.println("[DungeonMap] Load failed: " + e.getMessage());
        }
    }

    public BufferedImage getRoomImage(int mapCol, int mapRow) {
        if (!mapLoaded || dungeonMap == null) return null;
        if (mapCol < 0 || mapCol >= DUNGEON_MAP_COLS || mapRow < 0 || mapRow >= DUNGEON_MAP_ROWS) return null;

        int sx = mapCol * sourceRoomWidth;
        int sy = mapRow * sourceRoomHeight;

        if (sx + sourceRoomWidth <= dungeonMap.getWidth() && sy + sourceRoomHeight <= dungeonMap.getHeight()) {
            return dungeonMap.getSubimage(sx, sy, sourceRoomWidth, sourceRoomHeight);
        }
        return null;
    }

    public void setDungeonLevel(int level) { this.currentDungeonLevel = level; }
    public int getDungeonLevel() { return currentDungeonLevel; }

    public void renderRoom(Graphics2D g2, int mapCol, int mapRow) {
        BufferedImage roomImg = getRoomImage(mapCol, mapRow);
        if (roomImg != null) {
            g2.drawImage(roomImg, 0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT, null);
            // Apply dungeon color tint overlay — strong enough to recolor rooms
            // that were rendered with a different dungeon's palette in the atlas
            // (multiple dungeons share the same UW block grid positions)
            Color tint = NESPalette.getDungeonWallColor(currentDungeonLevel);
            g2.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 110));
            g2.fillRect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
        } else {
            renderFallbackRoom(g2);
        }
    }

    private void renderFallbackRoom(Graphics2D g2) {
        Color floorColor = NESPalette.getDungeonFloorColor(currentDungeonLevel);
        Color wallColor = NESPalette.getDungeonWallColor(currentDungeonLevel);
        Color doorColor = NESPalette.getDungeonDoorColor(currentDungeonLevel);

        g2.setColor(floorColor);
        g2.fillRect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        g2.setColor(wallColor);
        g2.fillRect(0, 0, DISPLAY_WIDTH, ROOM_BORDER_PIXELS);
        g2.fillRect(0, DISPLAY_HEIGHT - ROOM_BORDER_PIXELS, DISPLAY_WIDTH, ROOM_BORDER_PIXELS);
        g2.fillRect(0, 0, ROOM_BORDER_PIXELS, DISPLAY_HEIGHT);
        g2.fillRect(DISPLAY_WIDTH - ROOM_BORDER_PIXELS, 0, ROOM_BORDER_PIXELS, DISPLAY_HEIGHT);

        int doorW = 32, doorH = 32;
        g2.setColor(doorColor);
        g2.fillRect(DISPLAY_WIDTH / 2 - doorW / 2, 0, doorW, doorH);
        g2.fillRect(DISPLAY_WIDTH / 2 - doorW / 2, DISPLAY_HEIGHT - doorH, doorW, doorH);
        g2.fillRect(0, DISPLAY_HEIGHT / 2 - doorH / 2, doorW, doorH);
        g2.fillRect(DISPLAY_WIDTH - doorW, DISPLAY_HEIGHT / 2 - doorH / 2, doorW, doorH);
    }

    public int[][] generateCollisionGrid(int mapCol, int mapRow) {
        int tilesX = ZeldaRoom.TILES_X;
        int tilesY = ZeldaRoom.TILES_Y;
        int[][] grid = new int[tilesX][tilesY];

        for (int tx = 0; tx < tilesX; tx++) {
            for (int ty = 0; ty < tilesY; ty++) {
                if (tx <= 1 || tx >= tilesX - 2 || ty <= 1 || ty >= tilesY - 2) {
                    grid[tx][ty] = TileType.WALL.ordinal();
                } else {
                    grid[tx][ty] = TileType.FLOOR.ordinal();
                }
            }
        }

        int midX = tilesX / 2;
        int midY = tilesY / 2;
        grid[midX][0] = TileType.FLOOR.ordinal();
        grid[midX][1] = TileType.FLOOR.ordinal();
        grid[midX][tilesY - 1] = TileType.FLOOR.ordinal();
        grid[midX][tilesY - 2] = TileType.FLOOR.ordinal();
        grid[0][midY] = TileType.FLOOR.ordinal();
        grid[1][midY] = TileType.FLOOR.ordinal();
        grid[tilesX - 1][midY] = TileType.FLOOR.ordinal();
        grid[tilesX - 2][midY] = TileType.FLOOR.ordinal();

        return grid;
    }

    public boolean isMapLoaded() { return mapLoaded; }
}
