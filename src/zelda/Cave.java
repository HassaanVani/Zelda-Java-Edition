package zelda;

import java.awt.*;
import javax.swing.ImageIcon;
import java.io.File;

public class Cave {
    private int x, y;
    private int width = 48;
    private int height = 40;
    
    private boolean swordTaken = false;
    
    private Image oldManSprite;
    private Image swordSprite;
    
    public Cave(int x, int y) {
        this.x = x;
        this.y = y;
        loadSprites();
    }
    
    private void loadSprites() {
        File oldManFile = new File("sprites/NPCs/Old Man.gif");
        if (oldManFile.exists()) {
            oldManSprite = new ImageIcon(oldManFile.getPath()).getImage();
        }
        
        File swordFile = new File("sprites/Objects/Wooden Sword (Up).gif");
        if (swordFile.exists()) {
            swordSprite = new ImageIcon(swordFile.getPath()).getImage();
        }
    }
    
    public void update(ZeldaPlayer player) {
        if (swordTaken) return;
        
        Rectangle swordBox = new Rectangle(x + 18, y + 24, 12, 16);
        if (player.getHitbox().intersects(swordBox)) {
            swordTaken = true;
            player.setSword(true);
        }
    }
    
    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(x, y, width, height);
        
        if (oldManSprite != null) {
            g2.drawImage(oldManSprite, x + 16, y + 4, 16, 16, null);
        } else {
            g2.setColor(new Color(180, 140, 100));
            g2.fillRect(x + 18, y + 4, 12, 14);
        }
        
        if (!swordTaken) {
            if (swordSprite != null) {
                g2.drawImage(swordSprite, x + 19, y + 22, 10, 14, null);
            } else {
                g2.setColor(new Color(160, 160, 160));
                g2.fillRect(x + 20, y + 22, 8, 12);
            }
        }
        
        g2.setColor(new Color(180, 100, 40));
        g2.fillRect(x + 2, y + 4, 10, 16);
        g2.fillRect(x + width - 12, y + 4, 10, 16);
        
        g2.setFont(new Font("Monospaced", Font.PLAIN, 6));
        g2.setColor(Color.WHITE);
        g2.drawString("IT'S DANGEROUS", x - 12, y - 10);
        g2.drawString("TO GO ALONE!", x - 8, y - 3);
    }
    
    public boolean isSwordTaken() { return swordTaken; }
}
