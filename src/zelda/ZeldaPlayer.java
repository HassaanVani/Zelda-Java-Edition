package zelda;

import engine.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
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
    private boolean hasSword = true;
    private boolean hasBoomerang = false;
    
    private int direction = 2;
    private boolean moving = false;
    
    private boolean attacking = false;
    private int attackTimer = 0;
    private static final int ATTACK_DURATION = 12;
    private Rectangle swordHitbox = new Rectangle(0, 0, 12, 12);
    private boolean attackKeyReleased = true;
    
    private int invulnerableFrames = 0;
    private static final int INVULNERABLE_TIME = 60;
    
    private int frame = 0;
    private int animCounter = 0;
    
    private KeyHandler keyHandler;
    
    private BufferedImage[][] walkFrames = new BufferedImage[4][2];
    private BufferedImage[][] attackFrames = new BufferedImage[4][2];
    
    public ZeldaPlayer(int x, int y, KeyHandler keyHandler) {
        this.worldX = x;
        this.worldY = y;
        this.keyHandler = keyHandler;
        loadSprites();
    }
    
    private void loadSprites() {
        String base = "sprites/Link/Link (Normal) ";
        
        walkFrames[0][0] = loadImg(base + "(Back).gif");
        walkFrames[0][1] = walkFrames[0][0];
        walkFrames[1][0] = loadImg(base + "(Left)1.gif");
        walkFrames[1][1] = loadImg(base + "(Left)2.gif");
        walkFrames[2][0] = loadImg(base + "(Front)1.gif");
        walkFrames[2][1] = loadImg(base + "(Front)2.gif");
        walkFrames[3][0] = walkFrames[1][0];
        walkFrames[3][1] = walkFrames[1][1];
        
        attackFrames[0][0] = loadImg(base + "(Back) - Wooden Sword.gif");
        attackFrames[0][1] = attackFrames[0][0];
        attackFrames[1][0] = loadImg(base + "(Left) - Wooden Sword1.gif");
        attackFrames[1][1] = loadImg(base + "(Left) - Wooden Sword2.gif");
        attackFrames[2][0] = loadImg(base + "(Front) - Wooden Sword1.gif");
        attackFrames[2][1] = loadImg(base + "(Front) - Wooden Sword2.gif");
        attackFrames[3][0] = attackFrames[1][0];
        attackFrames[3][1] = attackFrames[1][1];
        
        System.out.println("[Link] Sprites: Walk=" + 
            (walkFrames[2][0] != null ? "OK" : "FAIL") + 
            " Attack=" + (attackFrames[2][0] != null ? "OK" : "FAIL"));
    }
    
    private BufferedImage loadImg(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
            System.err.println("[Link] Missing: " + path);
        } catch (Exception e) {}
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
        
        if (moving) {
            animCounter++;
            if (animCounter >= 8) { animCounter = 0; frame = (frame + 1) % 2; }
        } else {
            frame = 0;
        }
        
        updateSwordHitbox();
    }
    
    private void updateSwordHitbox() {
        int len = 14;
        switch (direction) {
            case 0: swordHitbox.setBounds(worldX + 2, worldY - len, 12, len); break;
            case 1: swordHitbox.setBounds(worldX + 16, worldY + 2, len, 12); break;
            case 2: swordHitbox.setBounds(worldX + 2, worldY + 16, 12, len); break;
            case 3: swordHitbox.setBounds(worldX - len, worldY + 2, len, 12); break;
        }
    }
    
    public void render(Graphics2D g2) {
        if (invulnerableFrames > 0 && (invulnerableFrames / 4) % 2 == 0) return;
        
        BufferedImage[][] sprites = attacking ? attackFrames : walkFrames;
        BufferedImage img = sprites[direction][frame];
        
        int x = worldX;
        int y = worldY;
        int w = 16;
        int h = 16;
        
        if (img != null) {
            if (direction == 1) {
                g2.drawImage(img, x + w, y, -w, h, null);
            } else {
                g2.drawImage(img, x, y, w, h, null);
            }
        } else {
            g2.setColor(new Color(0, 128, 0));
            g2.fillRect(x, y, w, h);
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
    public Rectangle getSwordHitbox() { return attacking ? swordHitbox : new Rectangle(0,0,0,0); }
    
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
