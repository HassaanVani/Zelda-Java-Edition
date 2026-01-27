package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class ZeldaHUD {
    private ZeldaPlayer player;
    
    private BufferedImage heartFull;
    private BufferedImage heartHalf;
    private BufferedImage heartEmpty;
    private BufferedImage rupeeIcon;
    private BufferedImage keyIcon;
    private BufferedImage bombIcon;
    private BufferedImage swordIcon;
    
    private static final Color NES_BLACK = new Color(0, 0, 0);
    private static final Color NES_GRAY = new Color(116, 116, 116);
    private static final Color NES_WHITE = Color.WHITE;
    private static final Color NES_RED = new Color(180, 56, 0);
    private static final Color NES_DARK_GRAY = new Color(60, 60, 60);
    
    public ZeldaHUD() {
        loadSprites();
    }
    
    private void loadSprites() {
        String basePath = "sprites/Objects/";
        try {
            File f1 = new File(basePath + "Life1.gif");
            File f2 = new File(basePath + "Life2.gif");
            File f3 = new File(basePath + "Life3.gif");
            
            if (f1.exists()) heartFull = ImageIO.read(f1);
            if (f2.exists()) heartHalf = ImageIO.read(f2);
            if (f3.exists()) heartEmpty = ImageIO.read(f3);
            
            File rupeeFile = new File(basePath + "Rupy.gif");
            File keyFile = new File(basePath + "Key.gif");
            File bombFile = new File(basePath + "Bomb.gif");
            File swordFile = new File(basePath + "Wooden Sword (Up).gif");
            
            if (rupeeFile.exists()) rupeeIcon = ImageIO.read(rupeeFile);
            if (keyFile.exists()) keyIcon = ImageIO.read(keyFile);
            if (bombFile.exists()) bombIcon = ImageIO.read(bombFile);
            if (swordFile.exists()) swordIcon = ImageIO.read(swordFile);
        } catch (Exception e) {}
    }
    
    public void setPlayer(ZeldaPlayer player) {
        this.player = player;
    }
    
    public void render(Graphics2D g2, ZeldaRoom currentRoom) {
        g2.setColor(NES_BLACK);
        g2.fillRect(0, 0, 256, 56);
        
        renderMinimap(g2, currentRoom);
        
        g2.setColor(NES_GRAY);
        g2.drawLine(88, 8, 88, 48);
        
        renderInventory(g2);
        
        renderLifeSection(g2);
    }
    
    private void renderMinimap(Graphics2D g2, ZeldaRoom room) {
        int mapX = 16;
        int mapY = 16;
        int mapWidth = 64;
        int mapHeight = 32;
        
        g2.setColor(NES_GRAY);
        g2.drawRect(mapX - 1, mapY - 1, mapWidth + 2, mapHeight + 2);
        
        g2.setColor(NES_DARK_GRAY);
        g2.fillRect(mapX, mapY, mapWidth, mapHeight);
        
        if (room != null) {
            int screenW = mapWidth / 16;
            int screenH = mapHeight / 8;
            int posX = mapX + room.getRoomX() * screenW;
            int posY = mapY + room.getRoomY() * screenH;
            
            g2.setColor(new Color(0, 168, 0));
            g2.fillRect(posX, posY, screenW, screenH);
        }
    }
    
    private void renderInventory(Graphics2D g2) {
        if (player == null) return;
        
        int invX = 96;
        int invY = 8;
        
        g2.setColor(NES_GRAY);
        g2.drawRect(invX + 4, invY + 16, 24, 16);
        g2.drawRect(invX + 32, invY + 16, 24, 16);
        
        g2.setColor(NES_WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.drawString("B", invX + 12, invY + 12);
        g2.drawString("A", invX + 40, invY + 12);
        
        if (player.hasSword() && swordIcon != null) {
            g2.drawImage(swordIcon, invX + 36, invY + 18, 16, 14, null);
        } else if (player.hasSword()) {
            g2.setColor(NES_WHITE);
            g2.fillRect(invX + 40, invY + 19, 8, 12);
        }
        
        int statY = invY + 38;
        
        g2.setColor(NES_WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        
        if (rupeeIcon != null) {
            g2.drawImage(rupeeIcon, invX - 2, statY - 8, 8, 8, null);
        } else {
            g2.setColor(new Color(0, 168, 0));
            g2.drawString("X", invX, statY);
        }
        g2.setColor(NES_WHITE);
        g2.drawString(String.format("%02d", player.getRupees()), invX + 10, statY);
        
        if (keyIcon != null) {
            g2.drawImage(keyIcon, invX + 30, statY - 8, 8, 8, null);
        } else {
            g2.setColor(new Color(252, 152, 56));
            g2.drawString("X", invX + 32, statY);
        }
        g2.setColor(NES_WHITE);
        g2.drawString(String.format("%02d", player.getKeys()), invX + 42, statY);
        
        if (bombIcon != null) {
            g2.drawImage(bombIcon, invX - 2, statY + 2, 8, 8, null);
        } else {
            g2.setColor(new Color(0, 88, 248));
            g2.drawString("X", invX, statY + 10);
        }
        g2.setColor(NES_WHITE);
        g2.drawString(String.format("%02d", player.getBombs()), invX + 10, statY + 10);
    }
    
    private void renderLifeSection(Graphics2D g2) {
        if (player == null) return;
        
        int lifeX = 168;
        int lifeY = 16;
        
        g2.setColor(NES_RED);
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString("-LIFE-", lifeX + 16, lifeY);
        
        int heartX = lifeX;
        int heartY = lifeY + 8;
        int heartSize = 8;
        int spacing = 8;
        
        int maxHearts = player.getMaxHealth() / 2;
        int fullHearts = player.getHealth() / 2;
        int halfHeart = player.getHealth() % 2;
        
        for (int i = 0; i < maxHearts; i++) {
            int hx = heartX + (i % 8) * spacing;
            int hy = heartY + (i / 8) * (spacing + 2);
            
            BufferedImage heartImg;
            if (i < fullHearts) {
                heartImg = heartFull;
            } else if (i == fullHearts && halfHeart > 0) {
                heartImg = heartHalf;
            } else {
                heartImg = heartEmpty;
            }
            
            if (heartImg != null) {
                g2.drawImage(heartImg, hx, hy, heartSize, heartSize, null);
            } else {
                if (i < fullHearts) {
                    g2.setColor(NES_RED);
                } else if (i == fullHearts && halfHeart > 0) {
                    g2.setColor(new Color(180, 100, 100));
                } else {
                    g2.setColor(NES_DARK_GRAY);
                }
                g2.fillRect(hx, hy, heartSize - 1, heartSize - 1);
            }
        }
    }
}
