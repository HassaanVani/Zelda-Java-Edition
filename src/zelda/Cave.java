package zelda;

import java.awt.*;
import javax.swing.ImageIcon;
import java.io.File;

public class Cave {
    private boolean active = false;
    private boolean swordTaken = false;
    
    private Image oldManSprite;
    private Image swordSprite;
    private Image flameSprite;
    
    public Cave() {
        loadSprites();
    }
    
    private void loadSprites() {
        oldManSprite = loadGif("sprites/NPCs/Old Man.gif");
        swordSprite = loadGif("sprites/Objects/Wooden Sword (Up).gif");
        flameSprite = loadGif("sprites/Objects/Fire1.gif");
    }
    
    private Image loadGif(String path) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(path).getImage();
        return null;
    }
    
    public void enter() {
        active = true;
    }
    
    public void exit() {
        active = false;
    }
    
    public boolean isActive() { return active; }
    public boolean isSwordTaken() { return swordTaken; }
    
    public void update(ZeldaPlayer player) {
        if (!active || swordTaken) return;
        
        Rectangle swordBox = new Rectangle(120, 100, 16, 16);
        if (player.getHitbox().intersects(swordBox)) {
            swordTaken = true;
            player.setSword(true);
        }
    }
    
    public boolean checkExit(ZeldaPlayer player) {
        return player.getWorldY() > 160;
    }
    
    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 256, 176);
        
        if (flameSprite != null) {
            g2.drawImage(flameSprite, 72, 48, 16, 16, null);
            g2.drawImage(flameSprite, 168, 48, 16, 16, null);
        } else {
            g2.setColor(new Color(255, 100, 0));
            g2.fillOval(74, 50, 12, 12);
            g2.fillOval(170, 50, 12, 12);
        }
        
        if (oldManSprite != null) {
            g2.drawImage(oldManSprite, 120, 48, 16, 16, null);
        } else {
            g2.setColor(new Color(180, 100, 60));
            g2.fillRect(120, 48, 16, 16);
        }
        
        if (!swordTaken) {
            if (swordSprite != null) {
                g2.drawImage(swordSprite, 122, 100, 12, 16, null);
            } else {
                g2.setColor(new Color(160, 160, 160));
                g2.fillRect(124, 100, 8, 14);
            }
        }
        
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.setColor(Color.WHITE);
        g2.drawString("IT'S DANGEROUS TO GO", 64, 72);
        g2.drawString("ALONE! TAKE THIS.", 72, 84);
        
        g2.setColor(Color.WHITE);
        g2.fillRect(112, 168, 32, 8);
    }
}
