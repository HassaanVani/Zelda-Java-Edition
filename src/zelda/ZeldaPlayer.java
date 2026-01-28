package zelda;

import engine.KeyHandler;
import java.awt.*;
import javax.swing.ImageIcon;
import java.io.File;

public class ZeldaPlayer {
    private int worldX, worldY;
    private int oldX, oldY;
    private double speed = 1.5;
    
    private int health = 6;
    private int maxHealth = 6;
    private int rupees = 0;
    private int keys = 0;
    private int bombs = 0;
    
    private String name = "LINK";
    private boolean hasSword = false;
    private boolean hasBoomerang = false;
    
    private int direction = 2;
    private boolean moving = false;
    
    private boolean attacking = false;
    private int attackTimer = 0;
    private static final int ATTACK_DURATION = 15;
    private Rectangle swordHitbox = new Rectangle(0, 0, 14, 14);
    private boolean attackKeyReleased = true;
    
    private int invulnerableFrames = 0;
    private static final int INVULNERABLE_TIME = 60;
    
    private KeyHandler keyHandler;
    
    private Image[][] walkImages = new Image[4][2];
    private Image[][] attackImages = new Image[4][2];
    
    public ZeldaPlayer(int x, int y, KeyHandler keyHandler) {
        this.worldX = x;
        this.worldY = y;
        this.keyHandler = keyHandler;
        loadSprites();
    }
    
    private void loadSprites() {
        String base = "sprites/Link/Link (Normal) ";
        
        walkImages[0][0] = loadGif(base + "(Back).gif");
        walkImages[0][1] = walkImages[0][0];
        walkImages[1][0] = loadGif(base + "(Left)1.gif");
        walkImages[1][1] = loadGif(base + "(Left)2.gif");
        walkImages[2][0] = loadGif(base + "(Front)1.gif");
        walkImages[2][1] = loadGif(base + "(Front)2.gif");
        walkImages[3][0] = walkImages[1][0];
        walkImages[3][1] = walkImages[1][1];
        
        attackImages[0][0] = loadGif(base + "(Back) - Wooden Sword.gif");
        attackImages[0][1] = attackImages[0][0];
        attackImages[1][0] = loadGif(base + "(Left) - Wooden Sword1.gif");
        attackImages[1][1] = loadGif(base + "(Left) - Wooden Sword2.gif");
        attackImages[2][0] = loadGif(base + "(Front) - Wooden Sword1.gif");
        attackImages[2][1] = loadGif(base + "(Front) - Wooden Sword2.gif");
        attackImages[3][0] = attackImages[1][0];
        attackImages[3][1] = attackImages[1][1];
    }
    
    private Image loadGif(String path) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(path).getImage();
        return null;
    }
    
    public void update() {
        oldX = worldX;
        oldY = worldY;
        
        if (invulnerableFrames > 0) invulnerableFrames--;
        
        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) attacking = false;
        }
        
        if (keyHandler.zPressed && attackKeyReleased && !attacking && hasSword) {
            attacking = true;
            attackTimer = ATTACK_DURATION;
            attackKeyReleased = false;
        }
        if (!keyHandler.zPressed) attackKeyReleased = true;
        
        moving = false;
        if (!attacking) {
            if (keyHandler.upPressed) { worldY -= speed; direction = 0; moving = true; }
            else if (keyHandler.downPressed) { worldY += speed; direction = 2; moving = true; }
            else if (keyHandler.leftPressed) { worldX -= speed; direction = 3; moving = true; }
            else if (keyHandler.rightPressed) { worldX += speed; direction = 1; moving = true; }
        }
        
        updateSwordHitbox();
    }
    
    private void updateSwordHitbox() {
        if (!attacking) {
            swordHitbox.setBounds(0, 0, 0, 0);
            return;
        }
        
        int len = 16;
        int w = 10;
        switch (direction) {
            case 0: swordHitbox.setBounds(worldX + 3, worldY - len, w, len); break;
            case 1: swordHitbox.setBounds(worldX + 16, worldY + 3, len, w); break;
            case 2: swordHitbox.setBounds(worldX + 3, worldY + 16, w, len); break;
            case 3: swordHitbox.setBounds(worldX - len, worldY + 3, len, w); break;
        }
    }
    
    public void render(Graphics2D g2) {
        if (invulnerableFrames > 0 && (invulnerableFrames / 4) % 2 == 0) return;
        
        Image[][] sprites = attacking ? attackImages : walkImages;
        Image img = sprites[direction][0];
        
        int x = worldX;
        int y = worldY;
        int w = 16;
        int h = 16;
        
        if (attacking) {
            switch (direction) {
                case 0: y -= 12; h = 28; break;
                case 1: w = 28; break;
                case 2: h = 28; break;
                case 3: x -= 12; w = 28; break;
            }
        }
        
        if (img != null) {
            if (direction == 1) {
                g2.drawImage(img, x + w, y, -w, h, null);
            } else {
                g2.drawImage(img, x, y, w, h, null);
            }
        } else {
            g2.setColor(new Color(0, 168, 0));
            g2.fillRect(worldX, worldY, 16, 16);
        }
    }
    
    public void damage(int amt) {
        if (invulnerableFrames == 0) {
            health = Math.max(0, health - amt);
            invulnerableFrames = INVULNERABLE_TIME;
        }
    }
    
    public void heal(int amt) { health = Math.min(maxHealth, health + amt); }
    public void addHeartContainer() { maxHealth += 2; health = maxHealth; }
    public void rollbackPosition() { worldX = oldX; worldY = oldY; }
    public void setPosition(int x, int y) { worldX = x; worldY = y; }
    
    public Rectangle getHitbox() { return new Rectangle(worldX + 2, worldY + 2, 12, 12); }
    public Rectangle getSwordHitbox() { return swordHitbox; }
    
    public boolean isDead() { return health <= 0; }
    public boolean isAttacking() { return attacking; }
    public boolean hasSword() { return hasSword; }
    public void setSword(boolean v) { hasSword = v; }
    public boolean hasBoomerang() { return hasBoomerang; }
    public void setBoomerang(boolean v) { hasBoomerang = v; }
    
    public int getWorldX() { return worldX; }
    public int getWorldY() { return worldY; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getRupees() { return rupees; }
    public int getKeys() { return keys; }
    public int getBombs() { return bombs; }
    public String getName() { return name; }
    public int getDirection() { return direction; }
    
    public void setHealth(int v) { health = Math.max(0, Math.min(v, maxHealth)); }
    public void setMaxHealth(int v) { maxHealth = v; }
    public void setRupees(int v) { rupees = v; }
    public void setKeys(int v) { keys = v; }
    public void setBombs(int v) { bombs = v; }
    public void setName(String v) { name = v; }
    
    public void addRupees(int v) { rupees = Math.min(255, rupees + v); }
    public void addKeys(int v) { keys += v; }
    public void addBombs(int v) { bombs = Math.min(8, bombs + v); }
    public boolean useKey() { if (keys > 0) { keys--; return true; } return false; }
    public boolean useBomb() { if (bombs > 0) { bombs--; return true; } return false; }
}
