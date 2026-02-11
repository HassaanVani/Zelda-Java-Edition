package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;

public abstract class ZeldaEnemy {
    public static final int DEFAULT_WIDTH = 16;
    public static final int DEFAULT_HEIGHT = 16;
    public static final int DAMAGE_FLASH_FRAMES = 20;
    public static final int DEFAULT_INVULN = 30;
    public static final int RANDOM_MOVE_INTERVAL = 60;

    public enum AIType { RANDOM, CHASE, SHOOTER, JUMPER }

    protected double x, y, oldX, oldY;
    protected int width = DEFAULT_WIDTH;
    protected int height = DEFAULT_HEIGHT;
    protected double speed = 0.5;
    protected int health;
    protected int maxHealth;
    protected int damage = 1;
    protected AIType aiType;
    protected boolean active = true;

    protected int direction = 0;
    protected int animationFrame = 0;
    protected int animationCounter = 0;
    protected int damageTimer = 0;
    protected int invulnerableFrames = 0;
    protected int moveTimer = 0;

    protected BufferedImage sprite;

    public ZeldaEnemy(double x, double y, int health, AIType aiType) {
        this(x, y, health, 1, aiType);
    }

    public ZeldaEnemy(double x, double y, int health, int damage, AIType aiType) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.aiType = aiType;
    }

    public abstract void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles);

    public void render(Graphics2D g2) {
        if (!active) return;

        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(Color.RED);
            g2.fillRect((int)x, (int)y, width, height);
        }
    }

    public void damage(int amount) {
        if (invulnerableFrames > 0) return;
        health -= amount;
        damageTimer = DAMAGE_FLASH_FRAMES;
        invulnerableFrames = DEFAULT_INVULN;
        if (health <= 0) active = false;
    }

    protected void randomMove(ZeldaRoom room) {
        moveTimer--;
        if (moveTimer <= 0) {
            direction = (int)(Math.random() * 4);
            moveTimer = RANDOM_MOVE_INTERVAL + (int)(Math.random() * 60);
        }

        double nx = x, ny = y;
        switch (direction) {
            case 0: ny -= speed; break;
            case 1: nx -= speed; break;
            case 2: ny += speed; break;
            case 3: nx += speed; break;
        }

        int cx = (int)(nx + width / 2);
        int cy = (int)(ny + height / 2);

        if (room.isWalkable(cx, cy)) {
            x = nx;
            y = ny;
        } else {
            moveTimer = 0;
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
    }

    protected BufferedImage loadSprite(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception e) { }
        return null;
    }

    public boolean canDamage() { return active; }
    public boolean isAlive() { return active && health > 0; }

    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getDamage() { return damage; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isActive() { return active; }
}
