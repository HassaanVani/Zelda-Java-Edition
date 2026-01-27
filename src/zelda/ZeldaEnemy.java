package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;

public abstract class ZeldaEnemy {
    protected double x, y;
    protected double oldX, oldY;
    protected int width = 14;
    protected int height = 14;
    protected double speed = 0.8;
    
    protected int health;
    protected int maxHealth;
    protected int damage = 1;
    protected boolean active = true;
    
    protected int direction = 2;
    
    protected int damageTimer = 0;
    protected int damageCooldown = 30;
    protected int invulnerableFrames = 0;
    
    protected BufferedImage sprite;
    protected int animationFrame = 0;
    protected int animationCounter = 0;
    
    public enum AIType {
        PATROL,
        CHASE,
        RANDOM,
        SHOOTER,
        STATIONARY
    }
    
    protected AIType aiType;
    
    public ZeldaEnemy(double x, double y, int health, AIType aiType) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.aiType = aiType;
        this.damage = 1;
    }
    
    public ZeldaEnemy(double x, double y, int health, int damage, AIType aiType) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.aiType = aiType;
    }
    
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;
        
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        
        animationCounter++;
        if (animationCounter >= 10) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }
    }
    
    protected void moveTowardsPlayer(ZeldaPlayer player) {
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist > 0) {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
            
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = dx > 0 ? 1 : 3;
            } else {
                direction = dy > 0 ? 2 : 0;
            }
        }
    }
    
    protected void randomMove(ZeldaRoom room) {
        if (Math.random() < 0.02) {
            direction = (int)(Math.random() * 4);
        }
        
        switch (direction) {
            case 0: y -= speed; break;
            case 1: x += speed; break;
            case 2: y += speed; break;
            case 3: x -= speed; break;
        }
        
        int centerX = (int)(x + width/2);
        int centerY = (int)(y + height/2);
        
        if (!room.isWalkable(centerX, centerY)) {
            x = oldX;
            y = oldY;
            direction = (direction + 1 + (int)(Math.random() * 3)) % 4;
        }
        
        x = Math.max(8, Math.min(x, 256 - width - 8));
        y = Math.max(8, Math.min(y, 176 - height - 8));
    }
    
    public void damage(int amount) {
        if (damageTimer <= 0) {
            health -= amount;
            damageTimer = damageCooldown;
            invulnerableFrames = 20;
            if (health <= 0) {
                active = false;
            }
        }
    }
    
    public void render(Graphics2D g2) {
        if (!active) return;
        
        int drawX = (int)x;
        int drawY = (int)y;
        int size = 16;
        
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect(drawX, drawY, size, size);
            return;
        }
        
        if (sprite != null) {
            boolean flipX = (direction == 1);
            
            if (flipX) {
                g2.drawImage(sprite, drawX + size, drawY, -size, size, null);
            } else {
                g2.drawImage(sprite, drawX, drawY, size, size, null);
            }
        } else {
            g2.setColor(Color.RED);
            g2.fillRect(drawX, drawY, size, size);
        }
    }
    
    protected BufferedImage loadSprite(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (Exception e) {}
        return null;
    }
    
    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    
    public boolean canDamage() {
        return active && damageTimer <= damageCooldown / 2;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public int getDamage() { return damage; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
}
