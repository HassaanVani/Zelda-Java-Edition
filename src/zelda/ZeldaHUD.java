package zelda;

import java.awt.*;
import javax.swing.ImageIcon;
import java.io.File;

public class ZeldaHUD {
    private ZeldaPlayer player;
    
    private Image heartFull, heartHalf, heartEmpty;
    private Image rupeeIcon, keyIcon, bombIcon, swordIcon;
    
    private static final Color BG = new Color(0, 0, 0);
    private static final Color TEXT = Color.WHITE;
    private static final Color LIFE = new Color(200, 72, 72);
    
    public ZeldaHUD() {
        loadSprites();
    }
    
    private void loadSprites() {
        String p = "sprites/Objects/";
        heartFull = loadGif(p + "Life1.gif");
        heartHalf = loadGif(p + "Life2.gif");
        heartEmpty = loadGif(p + "Life3.gif");
        rupeeIcon = loadGif(p + "Rupy.gif");
        keyIcon = loadGif(p + "Key.gif");
        bombIcon = loadGif(p + "Bomb.gif");
        swordIcon = loadGif(p + "Wooden Sword (Up).gif");
    }
    
    private Image loadGif(String path) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(path).getImage();
        return null;
    }
    
    public void setPlayer(ZeldaPlayer p) { this.player = p; }
    
    public void render(Graphics2D g2, ZeldaRoom room) {
        g2.setColor(BG);
        g2.fillRect(0, 0, 256, 56);
        
        renderMinimap(g2, room);
        renderInventory(g2);
        renderLife(g2);
    }
    
    private void renderMinimap(Graphics2D g2, ZeldaRoom room) {
        int x = 16, y = 18, w = 64, h = 32;
        
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(x, y, w, h);
        g2.setColor(new Color(120, 120, 120));
        g2.drawRect(x-1, y-1, w+1, h+1);
        
        if (room != null) {
            int dx = x + (room.getRoomX() * w / 16);
            int dy = y + (room.getRoomY() * h / 8);
            g2.setColor(new Color(0, 200, 0));
            g2.fillRect(dx, dy, 4, 4);
        }
    }
    
    private void renderInventory(Graphics2D g2) {
        if (player == null) return;
        
        int x = 92;
        int y = 8;
        
        g2.setColor(new Color(120, 120, 120));
        g2.drawRect(x, y + 12, 20, 16);
        g2.drawRect(x + 24, y + 12, 20, 16);
        
        g2.setColor(TEXT);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.drawString("B", x + 6, y + 10);
        g2.drawString("A", x + 30, y + 10);
        
        if (player.hasSword() && swordIcon != null) {
            g2.drawImage(swordIcon, x + 28, y + 14, 12, 12, null);
        }
        
        int iy = y + 34;
        
        if (rupeeIcon != null) {
            g2.drawImage(rupeeIcon, x, iy, 8, 8, null);
        }
        g2.setColor(TEXT);
        g2.drawString(String.format("%03d", player.getRupees()), x + 10, iy + 8);
        
        if (keyIcon != null) {
            g2.drawImage(keyIcon, x + 36, iy - 2, 6, 10, null);
        }
        g2.setColor(TEXT);
        g2.drawString(String.format("%02d", player.getKeys()), x + 44, iy + 8);
        
        int by = iy + 10;
        if (bombIcon != null) {
            g2.drawImage(bombIcon, x, by, 8, 8, null);
        }
        g2.setColor(TEXT);
        g2.drawString(String.format("%02d", player.getBombs()), x + 10, by + 8);
    }
    
    private void renderLife(Graphics2D g2) {
        if (player == null) return;
        
        int x = 176, y = 8;
        
        g2.setColor(LIFE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString("-LIFE-", x + 12, y + 6);
        
        int hx = x;
        int hy = y + 12;
        int size = 8;
        int maxH = player.getMaxHealth() / 2;
        int fullH = player.getHealth() / 2;
        boolean halfH = player.getHealth() % 2 == 1;
        
        for (int i = 0; i < maxH; i++) {
            int px = hx + (i % 8) * (size + 1);
            int py = hy + (i / 8) * (size + 2);
            
            Image img;
            if (i < fullH) img = heartFull;
            else if (i == fullH && halfH) img = heartHalf;
            else img = heartEmpty;
            
            if (img != null) {
                g2.drawImage(img, px, py, size, size, null);
            } else {
                g2.setColor(i < fullH ? Color.RED : Color.DARK_GRAY);
                g2.fillRect(px, py, size, size);
            }
        }
    }
}
