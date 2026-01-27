package zelda;

import engine.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashMap;

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
    
    private int direction = 2;
    private boolean moving = false;
    
    private boolean attacking = false;
    private int attackTimer = 0;
    private static final int ATTACK_DURATION = 15;
    private Rectangle swordHitbox = new Rectangle(0, 0, 16, 14);
    private boolean attackKeyReleased = true;
    
    private int invulnerableFrames = 0;
    private static final int INVULNERABLE_TIME = 60;
    
    private int spriteFrame = 0;
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 8;
    
    private KeyHandler keyHandler;
    
    private HashMap<String, BufferedImage[]> sprites = new HashMap<>();
    private BufferedImage currentSprite;
    
    public ZeldaPlayer(int x, int y, KeyHandler keyHandler) {
        this.worldX = x;
        this.worldY = y;
        this.keyHandler = keyHandler;
        loadSprites();
    }
    
    private void loadSprites() {
        String basePath = "sprites/Link/";
        String variant = "Normal";
        
        try {
            sprites.put("front", new BufferedImage[] {
                loadImage(basePath + "Link (" + variant + ") (Front)1.gif"),
                loadImage(basePath + "Link (" + variant + ") (Front)2.gif")
            });
            sprites.put("back", new BufferedImage[] {
                loadImage(basePath + "Link (" + variant + ") (Back).gif"),
                loadImage(basePath + "Link (" + variant + ") (Back).gif")
            });
            sprites.put("left", new BufferedImage[] {
                loadImage(basePath + "Link (" + variant + ") (Left)1.gif"),
                loadImage(basePath + "Link (" + variant + ") (Left)2.gif")
            });
            
            sprites.put("front_sword", new BufferedImage[] {
                loadImage(basePath + "Link (" + variant + ") (Front) - Wooden Sword1.gif"),
                loadImage(basePath + "Link (" + variant + ") (Front) - Wooden Sword2.gif")
            });
            sprites.put("back_sword", new BufferedImage[] {
                loadImage(basePath + "Link (" + variant + ") (Back) - Wooden Sword.gif"),
                loadImage(basePath + "Link (" + variant + ") (Back) - Wooden Sword.gif")
            });
            sprites.put("left_sword", new BufferedImage[] {
                loadImage(basePath + "Link (" + variant + ") (Left) - Wooden Sword1.gif"),
                loadImage(basePath + "Link (" + variant + ") (Left) - Wooden Sword2.gif")
            });
            
        } catch (Exception e) {
            System.err.println("Failed to load Link sprites: " + e.getMessage());
        }
    }
    
    private BufferedImage loadImage(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }
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
        if (!keyHandler.zPressed) {
            attackKeyReleased = true;
        }
        
        moving = false;
        if (!attacking) {
            if (keyHandler.upPressed) {
                worldY -= speed;
                direction = 0;
                moving = true;
            } else if (keyHandler.downPressed) {
                worldY += speed;
                direction = 2;
                moving = true;
            } else if (keyHandler.leftPressed) {
                worldX -= speed;
                direction = 3;
                moving = true;
            } else if (keyHandler.rightPressed) {
                worldX += speed;
                direction = 1;
                moving = true;
            }
        }
        
        if (moving) {
            animationCounter++;
            if (animationCounter >= ANIMATION_SPEED) {
                animationCounter = 0;
                spriteFrame = (spriteFrame + 1) % 2;
            }
        } else {
            spriteFrame = 0;
        }
        
        updateSwordHitbox();
        updateCurrentSprite();
    }
    
    private void updateSwordHitbox() {
        int offsetX = 0, offsetY = 0;
        int swordWidth = 16, swordHeight = 14;
        
        switch (direction) {
            case 0: offsetX = 0; offsetY = -16; swordWidth = 14; swordHeight = 16; break;
            case 1: offsetX = 16; offsetY = 0; break;
            case 2: offsetX = 0; offsetY = 16; swordWidth = 14; swordHeight = 16; break;
            case 3: offsetX = -16; offsetY = 0; break;
        }
        
        swordHitbox.setBounds(worldX + offsetX, worldY + offsetY, swordWidth, swordHeight);
    }
    
    private void updateCurrentSprite() {
        String key = "";
        switch (direction) {
            case 0: key = "back"; break;
            case 1: key = "left"; break;
            case 2: key = "front"; break;
            case 3: key = "left"; break;
        }
        
        if (attacking) {
            key += "_sword";
        }
        
        BufferedImage[] frames = sprites.get(key);
        if (frames != null && frames[spriteFrame] != null) {
            currentSprite = frames[spriteFrame];
        }
    }
    
    public void render(Graphics2D g2) {
        if (invulnerableFrames > 0 && (invulnerableFrames / 4) % 2 == 0) {
            return;
        }
        
        int drawX = worldX;
        int drawY = worldY;
        int size = 16;
        boolean flipX = (direction == 1);
        
        if (currentSprite != null) {
            if (flipX) {
                g2.drawImage(currentSprite, drawX + size, drawY, -size, size, null);
            } else {
                g2.drawImage(currentSprite, drawX, drawY, size, size, null);
            }
        } else {
            g2.setColor(Color.GREEN);
            g2.fillRect(drawX, drawY, 16, 16);
            
            g2.setColor(Color.DARK_GRAY);
            switch (direction) {
                case 0: g2.fillRect(drawX + 5, drawY + 1, 4, 4); break;
                case 1: g2.fillRect(drawX + 9, drawY + 5, 4, 4); break;
                case 2: g2.fillRect(drawX + 5, drawY + 9, 4, 4); break;
                case 3: g2.fillRect(drawX + 1, drawY + 5, 4, 4); break;
            }
        }
        
        if (attacking) {
            g2.setColor(new Color(150, 150, 150, 128));
            g2.fillRect(swordHitbox.x, swordHitbox.y, swordHitbox.width, swordHitbox.height);
        }
    }
    
    public void damage(int amount) {
        if (invulnerableFrames == 0) {
            health = Math.max(0, health - amount);
            invulnerableFrames = INVULNERABLE_TIME;
        }
    }
    
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    public void addHeartContainer() {
        maxHealth += 2;
        health = maxHealth;
    }
    
    public void rollbackPosition() {
        worldX = oldX;
        worldY = oldY;
    }
    
    public void setPosition(int x, int y) {
        worldX = x;
        worldY = y;
    }
    
    public Rectangle getHitbox() {
        return new Rectangle(worldX + 2, worldY + 2, 10, 12);
    }
    
    public Rectangle getSwordHitbox() {
        return attacking ? swordHitbox : new Rectangle(0, 0, 0, 0);
    }
    
    public boolean isDead() { return health <= 0; }
    public boolean isAttacking() { return attacking; }
    public boolean hasSword() { return hasSword; }
    public void setSword(boolean has) { hasSword = has; }
    
    private boolean hasBoomerang = false;
    public boolean hasBoomerang() { return hasBoomerang; }
    public void setBoomerang(boolean has) { hasBoomerang = has; }
    
    public int getWorldX() { return worldX; }
    public int getWorldY() { return worldY; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getRupees() { return rupees; }
    public int getKeys() { return keys; }
    public int getBombs() { return bombs; }
    public String getName() { return name; }
    public int getDirection() { return direction; }
    
    public void setHealth(int h) { health = Math.max(0, Math.min(h, maxHealth)); }
    public void setMaxHealth(int m) { maxHealth = m; }
    public void setRupees(int r) { rupees = r; }
    public void setKeys(int k) { keys = k; }
    public void setBombs(int b) { bombs = b; }
    public void setName(String n) { name = n; }
    
    public void addRupees(int amount) { rupees = Math.min(255, rupees + amount); }
    public void addKeys(int amount) { keys += amount; }
    public void addBombs(int amount) { bombs = Math.min(8, bombs + amount); }
    public boolean useKey() { if (keys > 0) { keys--; return true; } return false; }
    public boolean useBomb() { if (bombs > 0) { bombs--; return true; } return false; }
}
