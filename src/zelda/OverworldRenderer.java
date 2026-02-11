package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class OverworldRenderer {
    public static final int MAP_COLS = 16;
    public static final int MAP_ROWS = 8;
    public static final int DISPLAY_WIDTH = 256;
    public static final int DISPLAY_HEIGHT = 176;

    private int sourceRoomWidth;
    private int sourceRoomHeight;

    private BufferedImage overworldMap;
    private BufferedImage[][] roomCache;
    private int[][][] collisionCache;
    private boolean mapLoaded = false;

    private static final int SAMPLES_PER_TILE = 5;

    private static final float WATER_HUE_MIN = 0.55f;
    private static final float WATER_HUE_MAX = 0.72f;
    private static final float WATER_SAT_MIN = 0.3f;
    private static final float WATER_BRI_MIN = 0.4f;

    private static final float TREE_HUE_MIN = 0.20f;
    private static final float TREE_HUE_MAX = 0.45f;
    private static final float TREE_SAT_MIN = 0.3f;
    private static final float TREE_BRI_MAX = 0.7f;

    private static final float DARK_BRI_MAX = 0.2f;
    private static final float GROUND_SAT_MAX = 0.6f;
    private static final float GROUND_BRI_MIN = 0.5f;

    private static final float BRIDGE_HUE_MIN = 0.04f;
    private static final float BRIDGE_HUE_MAX = 0.15f;
    private static final float BRIDGE_SAT_MIN = 0.3f;
    private static final float BRIDGE_BRI_MIN = 0.4f;
    private static final float BRIDGE_BRI_MAX = 0.85f;

    public OverworldRenderer() {
        roomCache = new BufferedImage[MAP_COLS][MAP_ROWS];
        collisionCache = new int[MAP_COLS][MAP_ROWS][];
        loadOverworldMap();
    }

    private void loadOverworldMap() {
        try {
            File mapFile = new File("sprites/Worlds/entire_worldmap_single_image.png");
            if (mapFile.exists()) {
                overworldMap = ImageIO.read(mapFile);
                mapLoaded = true;

                sourceRoomWidth = overworldMap.getWidth() / MAP_COLS;
                sourceRoomHeight = overworldMap.getHeight() / MAP_ROWS;
            }
        } catch (Exception e) {
            System.err.println("[Map] Load failed: " + e.getMessage());
        }
    }

    public BufferedImage getRoomImage(int roomX, int roomY) {
        if (roomX < 0 || roomX >= MAP_COLS || roomY < 0 || roomY >= MAP_ROWS) return null;
        if (roomCache[roomX][roomY] != null) return roomCache[roomX][roomY];
        if (overworldMap == null) return null;

        int sx = roomX * sourceRoomWidth;
        int sy = roomY * sourceRoomHeight;

        if (sx + sourceRoomWidth <= overworldMap.getWidth() && sy + sourceRoomHeight <= overworldMap.getHeight()) {
            roomCache[roomX][roomY] = overworldMap.getSubimage(sx, sy, sourceRoomWidth, sourceRoomHeight);
        }

        return roomCache[roomX][roomY];
    }

    public void renderRoom(Graphics2D g2, int roomX, int roomY) {
        BufferedImage roomImg = getRoomImage(roomX, roomY);
        if (roomImg != null) {
            g2.drawImage(roomImg, 0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT, null);
        } else {
            g2.setColor(new Color(124, 252, 0));
            g2.fillRect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
        }
    }

    public int classifyPixel(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        float hue = hsb[0], sat = hsb[1], bri = hsb[2];

        if (bri < DARK_BRI_MAX) return TileType.WALL.ordinal();

        if (hue >= WATER_HUE_MIN && hue <= WATER_HUE_MAX && sat >= WATER_SAT_MIN && bri >= WATER_BRI_MIN) {
            return TileType.WATER.ordinal();
        }

        if (hue >= BRIDGE_HUE_MIN && hue <= BRIDGE_HUE_MAX && sat >= BRIDGE_SAT_MIN
            && bri >= BRIDGE_BRI_MIN && bri <= BRIDGE_BRI_MAX) {
            return TileType.BRIDGE.ordinal();
        }

        if (hue >= TREE_HUE_MIN && hue <= TREE_HUE_MAX && sat >= TREE_SAT_MIN && bri <= TREE_BRI_MAX) {
            return TileType.TREE.ordinal();
        }

        if (sat < GROUND_SAT_MAX && bri >= GROUND_BRI_MIN) {
            return TileType.FLOOR.ordinal();
        }

        if (bri >= GROUND_BRI_MIN) {
            return TileType.SAND.ordinal();
        }

        return TileType.WALL.ordinal();
    }

    public int[][] generateCollisionGrid(int roomX, int roomY) {
        int tilesX = ZeldaRoom.TILES_X;
        int tilesY = ZeldaRoom.TILES_Y;
        int[][] grid = new int[tilesX][tilesY];

        BufferedImage roomImg = getRoomImage(roomX, roomY);
        if (roomImg == null) {
            for (int x = 0; x < tilesX; x++)
                for (int y = 0; y < tilesY; y++)
                    grid[x][y] = (x == 0 || x == tilesX - 1 || y == 0 || y == tilesY - 1)
                        ? TileType.WALL.ordinal() : TileType.FLOOR.ordinal();
            return grid;
        }

        for (int tx = 0; tx < tilesX; tx++) {
            for (int ty = 0; ty < tilesY; ty++) {
                grid[tx][ty] = classifyTile(roomImg, tx, ty);
            }
        }

        return grid;
    }

    private int classifyTile(BufferedImage roomImg, int tx, int ty) {
        int tilePixelW = sourceRoomWidth / ZeldaRoom.TILES_X;
        int tilePixelH = sourceRoomHeight / ZeldaRoom.TILES_Y;
        int baseX = tx * tilePixelW;
        int baseY = ty * tilePixelH;

        int[] votes = new int[TileType.values().length];

        int[][] offsets = {
            {tilePixelW / 2, tilePixelH / 2},
            {tilePixelW / 4, tilePixelH / 4},
            {3 * tilePixelW / 4, tilePixelH / 4},
            {tilePixelW / 4, 3 * tilePixelH / 4},
            {3 * tilePixelW / 4, 3 * tilePixelH / 4}
        };

        for (int[] off : offsets) {
            int px = Math.min(baseX + off[0], roomImg.getWidth() - 1);
            int py = Math.min(baseY + off[1], roomImg.getHeight() - 1);
            int rgb = roomImg.getRGB(px, py) & 0xFFFFFF;
            int type = classifyPixel(rgb);
            votes[type]++;
        }

        int bestType = TileType.FLOOR.ordinal();
        int bestCount = 0;
        for (int i = 0; i < votes.length; i++) {
            if (votes[i] > bestCount) {
                bestCount = votes[i];
                bestType = i;
            }
        }

        if (votes[TileType.WALL.ordinal()] >= 2 || votes[TileType.WATER.ordinal()] >= 2 || votes[TileType.TREE.ordinal()] >= 2) {
            if (votes[TileType.WALL.ordinal()] >= votes[TileType.FLOOR.ordinal()] &&
                votes[TileType.WALL.ordinal()] >= votes[TileType.WATER.ordinal()]) {
                return TileType.WALL.ordinal();
            }
            if (votes[TileType.WATER.ordinal()] >= votes[TileType.FLOOR.ordinal()]) {
                return TileType.WATER.ordinal();
            }
            if (votes[TileType.TREE.ordinal()] >= votes[TileType.FLOOR.ordinal()]) {
                return TileType.TREE.ordinal();
            }
        }

        return bestType;
    }

    public boolean isMapLoaded() { return mapLoaded; }
    public int getSourceRoomWidth() { return sourceRoomWidth; }
    public int getSourceRoomHeight() { return sourceRoomHeight; }
}
