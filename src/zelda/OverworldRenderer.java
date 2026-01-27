package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class OverworldRenderer {
    private static final int ROOM_WIDTH = 256;
    private static final int ROOM_HEIGHT = 176;
    private static final int MAP_COLS = 16;
    private static final int MAP_ROWS = 8;
    
    private BufferedImage overworldMap;
    private BufferedImage[][] roomCache;
    
    public OverworldRenderer() {
        roomCache = new BufferedImage[MAP_COLS][MAP_ROWS];
        loadOverworldMap();
    }
    
    private void loadOverworldMap() {
        try {
            File mapFile = new File("sprites/Worlds/entire_worldmap_single_image.png");
            if (mapFile.exists()) {
                overworldMap = ImageIO.read(mapFile);
            }
        } catch (Exception e) {
            System.err.println("Failed to load overworld map: " + e.getMessage());
        }
    }
    
    public BufferedImage getRoomImage(int roomX, int roomY) {
        if (roomX < 0 || roomX >= MAP_COLS || roomY < 0 || roomY >= MAP_ROWS) {
            return null;
        }
        
        if (roomCache[roomX][roomY] != null) {
            return roomCache[roomX][roomY];
        }
        
        if (overworldMap == null) {
            return null;
        }
        
        int startX = roomX * ROOM_WIDTH;
        int startY = roomY * ROOM_HEIGHT;
        
        if (startX + ROOM_WIDTH <= overworldMap.getWidth() && 
            startY + ROOM_HEIGHT <= overworldMap.getHeight()) {
            roomCache[roomX][roomY] = overworldMap.getSubimage(startX, startY, ROOM_WIDTH, ROOM_HEIGHT);
        }
        
        return roomCache[roomX][roomY];
    }
    
    public void renderRoom(Graphics2D g2, int roomX, int roomY) {
        BufferedImage roomImg = getRoomImage(roomX, roomY);
        if (roomImg != null) {
            g2.drawImage(roomImg, 0, 0, null);
        } else {
            g2.setColor(new Color(124, 252, 0));
            g2.fillRect(0, 0, ROOM_WIDTH, ROOM_HEIGHT);
        }
    }
    
    public boolean isPixelWalkable(int roomX, int roomY, int pixelX, int pixelY) {
        BufferedImage roomImg = getRoomImage(roomX, roomY);
        if (roomImg == null) return true;
        
        if (pixelX < 0 || pixelX >= ROOM_WIDTH || pixelY < 0 || pixelY >= ROOM_HEIGHT) {
            return false;
        }
        
        int rgb = roomImg.getRGB(pixelX, pixelY);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        
        if (b > 180 && r < 100 && g < 150) return false;
        
        if (r > 100 && g > 80 && g < 130 && b < 80) return false;
        
        if (r < 60 && g < 60 && b < 60) return false;
        
        if (r > 50 && r < 120 && g > 80 && g < 150 && b < 80) return false;
        
        return true;
    }
    
    public boolean isTileWalkable(int roomX, int roomY, int tileX, int tileY) {
        int px = tileX * 16 + 8;
        int py = tileY * 16 + 8;
        return isPixelWalkable(roomX, roomY, px, py);
    }
    
    public boolean isWaterAt(int roomX, int roomY, int pixelX, int pixelY) {
        BufferedImage roomImg = getRoomImage(roomX, roomY);
        if (roomImg == null) return false;
        
        if (pixelX < 0 || pixelX >= ROOM_WIDTH || pixelY < 0 || pixelY >= ROOM_HEIGHT) {
            return false;
        }
        
        int rgb = roomImg.getRGB(pixelX, pixelY);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        
        return (b > 180 && r < 100 && g < 150);
    }
}
