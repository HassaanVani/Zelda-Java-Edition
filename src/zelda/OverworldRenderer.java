package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class OverworldRenderer {
    private static final int MAP_COLS = 16;
    private static final int MAP_ROWS = 8;
    public static final int DISPLAY_WIDTH = 256;
    public static final int DISPLAY_HEIGHT = 176;
    
    private int sourceRoomWidth = 256;
    private int sourceRoomHeight = 168;
    
    private BufferedImage overworldMap;
    private BufferedImage[][] roomCache;
    private boolean mapLoaded = false;

    private static final int GROUND_COLOR = 0xfcd8a8;
    private static final int COLOR_TOLERANCE = 40;
    
    public OverworldRenderer() {
        roomCache = new BufferedImage[MAP_COLS][MAP_ROWS];
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
                
                System.out.println("[Map] Loaded: " + overworldMap.getWidth() + "x" + overworldMap.getHeight() +
                    " -> Room: " + sourceRoomWidth + "x" + sourceRoomHeight);
            } else {
                System.err.println("[Map] File not found!");
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
            g2.setColor(Color.RED);
            g2.drawString("Room " + roomX + "," + roomY + " missing", 60, 88);
        }
    }
    
    public boolean isGroundColor(int roomX, int roomY, int displayX, int displayY) {
        BufferedImage roomImg = getRoomImage(roomX, roomY);
        if (roomImg == null) return true;
        
        int srcX = (int)(displayX * (double)sourceRoomWidth / DISPLAY_WIDTH);
        int srcY = (int)(displayY * (double)sourceRoomHeight / DISPLAY_HEIGHT);
        
        srcX = Math.max(0, Math.min(srcX, sourceRoomWidth - 1));
        srcY = Math.max(0, Math.min(srcY, sourceRoomHeight - 1));
        
        int rgb = roomImg.getRGB(srcX, srcY) & 0xFFFFFF;
        
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        
        int gr = (GROUND_COLOR >> 16) & 0xFF;
        int gg = (GROUND_COLOR >> 8) & 0xFF;
        int gb = GROUND_COLOR & 0xFF;
        
        return Math.abs(r - gr) < COLOR_TOLERANCE && 
               Math.abs(g - gg) < COLOR_TOLERANCE && 
               Math.abs(b - gb) < COLOR_TOLERANCE;
    }
    
    public int[][] generateCollisionGrid(int roomX, int roomY) {
        int tilesX = 16;
        int tilesY = 11;
        int[][] grid = new int[tilesX][tilesY];
        
        BufferedImage roomImg = getRoomImage(roomX, roomY);
        if (roomImg == null) {
            for (int x = 0; x < tilesX; x++) {
                for (int y = 0; y < tilesY; y++) {
                    grid[x][y] = (x == 0 || x == tilesX-1 || y == 0 || y == tilesY-1) ? 1 : 0;
                }
            }
            return grid;
        }
        
        for (int tx = 0; tx < tilesX; tx++) {
            for (int ty = 0; ty < tilesY; ty++) {
                int centerX = tx * 16 + 8;
                int centerY = ty * 16 + 8;
                grid[tx][ty] = isGroundColor(roomX, roomY, centerX, centerY) ? 0 : 1;
            }
        }
        
        return grid;
    }
    
    public boolean isMapLoaded() { return mapLoaded; }
}
