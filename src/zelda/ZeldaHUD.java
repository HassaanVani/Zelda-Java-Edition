package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class ZeldaHUD {
    private ZeldaPlayer player;
    
    private BufferedImage heartFull, heartHalf, heartEmpty;
    private BufferedImage rupeeIcon, keyIcon, bombIcon, swordIcon;
    
    private static final Color BG_COLOR = new Color(0, 0, 0);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LIFE_COLOR = new Color(180, 56, 0);
    
    public ZeldaHUD() {
        loadSprites();
    }
    
    private void loadSprites() {
        String path = "sprites/Objects/";
        heartFull = loadImg(path + "Life1.gif");
        heartHalf = loadImg(path + "Life2.gif");
        heartEmpty = loadImg(path + "Life3.gif");
        rupeeIcon = loadImg(path + "Rupy.gif");
        keyIcon = loadImg(path + "Key.gif");
        bombIcon = loadImg(path + "Bomb.gif");
        swordIcon = loadImg(path + "Wooden Sword (Up).gif");
    }
    
    private BufferedImage loadImg(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception e) {}
        return null;
    }
    
    public void setPlayer(ZeldaPlayer p) { this.player = p; }
    
    public void render(Graphics2D g2, ZeldaRoom room) {
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, 256, 56);
        
        renderMinimap(g2, room);
        renderInventory(g2);
        renderLife(g2);
    }
    
    private void renderMinimap(Graphics2D g2, ZeldaRoom room) {
        int x = 16, y = 18, w = 64, h = 32;
        
        g2.setColor(new Color(60, 60, 60));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(116, 116, 116));
        g2.drawRect(x-1, y-1, w+1, h+1);
        
        if (room != null) {
            int dotX = x + (room.getRoomX() * w / 16);
            int dotY = y + (room.getRoomY() * h / 8);
            g2.setColor(Color.GREEN);
            g2.fillRect(dotX, dotY, 4, 4);
        }
    }
    
    private void renderInventory(Graphics2D g2) {
        if (player == null) return;
        
        int x = 96;
        
        g2.setColor(new Color(116, 116, 116));
        g2.drawRect(x, 24, 24, 16);
        g2.drawRect(x + 28, 24, 24, 16);
        
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString("B", x + 8, 20);
        g2.drawString("A", x + 36, 20);
        
        if (player.hasSword() && swordIcon != null) {
            g2.drawImage(swordIcon, x + 32, 26, 16, 12, null);
        }
        
        int statX = x;
        int statY = 48;
        
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        
        g2.setColor(new Color(0, 200, 0));
        g2.drawString("$", statX, statY);
        g2.setColor(TEXT_COLOR);
        g2.drawString(String.format("%03d", player.getRupees()), statX + 10, statY);
        
        g2.setColor(new Color(255, 180, 0));
        g2.drawString("K", statX + 44, statY);
        g2.setColor(TEXT_COLOR);
        g2.drawString(String.format("%02d", player.getKeys()), statX + 54, statY);
        
        g2.setColor(new Color(100, 100, 255));
        g2.drawString("B", statX + 78, statY);
        g2.setColor(TEXT_COLOR);
        g2.drawString(String.format("%02d", player.getBombs()), statX + 88, statY);
    }
    
    private void renderLife(Graphics2D g2) {
        if (player == null) return;
        
        int x = 176, y = 16;
        
        g2.setColor(LIFE_COLOR);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.drawString("-LIFE-", x + 8, y);
        
        int heartX = x;
        int heartY = y + 8;
        int size = 8;
        int maxHearts = player.getMaxHealth() / 2;
        int fullHearts = player.getHealth() / 2;
        boolean halfHeart = player.getHealth() % 2 == 1;
        
        for (int i = 0; i < maxHearts; i++) {
            int hx = heartX + (i % 8) * (size + 1);
            int hy = heartY + (i / 8) * (size + 2);
            
            BufferedImage img;
            if (i < fullHearts) {
                img = heartFull;
            } else if (i == fullHearts && halfHeart) {
                img = heartHalf;
            } else {
                img = heartEmpty;
            }
            
            if (img != null) {
                g2.drawImage(img, hx, hy, size, size, null);
            } else {
                g2.setColor(i < fullHearts ? Color.RED : Color.DARK_GRAY);
                g2.fillRect(hx, hy, size, size);
            }
        }
    }
}
