package zelda;

import engine.KeyHandler;
import java.awt.*;
import javax.swing.ImageIcon;
import java.io.File;

public class ZeldaPlayer {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;
    public static final double MOVE_SPEED = 1.5;
    public static final int ATTACK_DURATION = 12;
    public static final int INVULN_FRAMES = 60;
    public static final int ANIM_CYCLE_RATE = 10;
    public static final int KNOCKBACK_FORCE = 4;

    public static final int DIR_UP = 0;
    public static final int DIR_LEFT = 1;
    public static final int DIR_DOWN = 2;
    public static final int DIR_RIGHT = 3;

    private double worldX, worldY;
    private double oldX, oldY;
    private int direction = DIR_DOWN;
    private boolean moving = false;
    private int animFrame = 0;
    private int animCounter = 0;

    private boolean attacking = false;
    private int attackTimer = 0;
    private boolean attackKeyReleased = true;

    private int health = 6;
    private int maxHealth = 6;
    private int rupees = 0;
    private int keys = 0;
    private int bombs = 0;
    private boolean hasSword = false;
    private boolean hasBoomerang = false;
    private String name = "LINK";

    private int invulnerableFrames = 0;

    private KeyHandler keyHandler;

    private Image[][] walkImages = new Image[4][2];
    private Image[][] attackImages = new Image[4][2];

    public ZeldaPlayer(double x, double y, KeyHandler kh) {
        this.worldX = x;
        this.worldY = y;
        this.keyHandler = kh;
        loadSprites();
    }

    private void loadSprites() {
        String base = "sprites/Link/Link (Normal) ";

        walkImages[DIR_UP][0] = loadGif(base + "(Back).gif");
        walkImages[DIR_UP][1] = walkImages[DIR_UP][0];
        walkImages[DIR_LEFT][0] = loadGif(base + "(Left)1.gif");
        walkImages[DIR_LEFT][1] = loadGif(base + "(Left)2.gif");
        walkImages[DIR_DOWN][0] = loadGif(base + "(Front)1.gif");
        walkImages[DIR_DOWN][1] = loadGif(base + "(Front)2.gif");
        walkImages[DIR_RIGHT][0] = walkImages[DIR_LEFT][0];
        walkImages[DIR_RIGHT][1] = walkImages[DIR_LEFT][1];

        attackImages[DIR_UP][0] = loadGif(base + "(Back) - Wooden Sword.gif");
        attackImages[DIR_UP][1] = attackImages[DIR_UP][0];
        attackImages[DIR_LEFT][0] = loadGif(base + "(Left) - Wooden Sword1.gif");
        attackImages[DIR_LEFT][1] = loadGif(base + "(Left) - Wooden Sword2.gif");
        attackImages[DIR_DOWN][0] = loadGif(base + "(Front) - Wooden Sword1.gif");
        attackImages[DIR_DOWN][1] = loadGif(base + "(Front) - Wooden Sword2.gif");
        attackImages[DIR_RIGHT][0] = attackImages[DIR_LEFT][0];
        attackImages[DIR_RIGHT][1] = attackImages[DIR_LEFT][1];
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
            if (keyHandler.upPressed) {
                direction = DIR_UP;
                worldY -= MOVE_SPEED;
                moving = true;
            } else if (keyHandler.downPressed) {
                direction = DIR_DOWN;
                worldY += MOVE_SPEED;
                moving = true;
            } else if (keyHandler.leftPressed) {
                direction = DIR_LEFT;
                worldX -= MOVE_SPEED;
                moving = true;
            } else if (keyHandler.rightPressed) {
                direction = DIR_RIGHT;
                worldX += MOVE_SPEED;
                moving = true;
            }
        }

        if (moving) {
            animCounter++;
            if (animCounter >= ANIM_CYCLE_RATE) {
                animCounter = 0;
                animFrame = (animFrame + 1) % 2;
            }
        } else {
            animFrame = 0;
            animCounter = 0;
        }
    }

    public void render(Graphics2D g2) {
        if (invulnerableFrames > 0 && (invulnerableFrames / 3) % 2 == 0) return;

        boolean isAttack = attacking && hasSword;
        Image img = isAttack ? attackImages[direction][animFrame] : walkImages[direction][animFrame];
        boolean flipH = (direction == DIR_RIGHT);

        if (img != null) {
            int drawX = (int) worldX;
            int drawY = (int) worldY;
            int dw, dh;

            if (isAttack) {
                dw = img.getWidth(null);
                dh = img.getHeight(null);
                if (dw <= 0) dw = WIDTH;
                if (dh <= 0) dh = HEIGHT;
                if (direction == DIR_UP) drawY -= (dh - HEIGHT);
                if (direction == DIR_LEFT) drawX -= (dw - WIDTH);
            } else {
                dw = WIDTH;
                dh = HEIGHT;
            }

            if (flipH) {
                g2.drawImage(img, drawX + dw, drawY, -dw, dh, null);
            } else {
                g2.drawImage(img, drawX, drawY, dw, dh, null);
            }
        } else {
            g2.setColor(Color.GREEN);
            g2.fillRect((int)worldX, (int)worldY, WIDTH, HEIGHT);
            if (isAttack) renderSword(g2);
        }
    }

    private void renderSword(Graphics2D g2) {
        g2.setColor(new Color(160, 120, 60));
        int sx = (int)worldX, sy = (int)worldY;
        switch (direction) {
            case DIR_UP:    g2.fillRect(sx + 3, sy - 12, 4, 12); break;
            case DIR_DOWN:  g2.fillRect(sx + 9, sy + HEIGHT, 4, 12); break;
            case DIR_LEFT:  g2.fillRect(sx - 12, sy + 6, 12, 4); break;
            case DIR_RIGHT: g2.fillRect(sx + WIDTH, sy + 6, 12, 4); break;
        }
    }

    public Rectangle getSwordHitbox() {
        if (!attacking || !hasSword) return null;
        int sx = (int)worldX, sy = (int)worldY;
        switch (direction) {
            case DIR_UP:    return new Rectangle(sx + 2, sy - 14, 6, 14);
            case DIR_DOWN:  return new Rectangle(sx + 8, sy + HEIGHT, 6, 14);
            case DIR_LEFT:  return new Rectangle(sx - 14, sy + 4, 14, 6);
            case DIR_RIGHT: return new Rectangle(sx + WIDTH, sy + 4, 14, 6);
        }
        return null;
    }

    public void takeDamage(int amount, double sourceX, double sourceY) {
        if (invulnerableFrames > 0) return;
        health -= amount;
        invulnerableFrames = INVULN_FRAMES;

        double dx = worldX - sourceX;
        double dy = worldY - sourceY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            worldX += (dx / dist) * KNOCKBACK_FORCE;
            worldY += (dy / dist) * KNOCKBACK_FORCE;
        }

        if (health <= 0) health = 0;
    }

    public void revertPosition() {
        worldX = oldX;
        worldY = oldY;
    }

    public void clampToPlayArea() {
        worldX = Math.max(0, Math.min(worldX, ZeldaRoom.ROOM_PIXEL_W - WIDTH));
        worldY = Math.max(0, Math.min(worldY, ZeldaRoom.ROOM_PIXEL_H - HEIGHT));
    }

    public Rectangle getHitbox() {
        return new Rectangle((int)worldX + 2, (int)worldY + 2, WIDTH - 4, HEIGHT - 4);
    }

    // Getters and setters
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public void setPosition(double x, double y) { this.worldX = x; this.worldY = y; }
    public int getDirection() { return direction; }
    public boolean isMoving() { return moving; }
    public boolean isAttacking() { return attacking; }

    public int getHealth() { return health; }
    public void setHealth(int h) { health = h; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int m) { maxHealth = m; }
    public boolean isAlive() { return health > 0; }

    public int getRupees() { return rupees; }
    public void setRupees(int r) { rupees = r; }
    public void addRupees(int a) { rupees += a; }
    public int getKeys() { return keys; }
    public void setKeys(int k) { keys = k; }
    public void addKeys(int k) { keys += k; }
    public int getBombs() { return bombs; }
    public void setBombs(int b) { bombs = b; }
    public void addBombs(int b) { bombs += b; }

    public boolean hasSword() { return hasSword; }
    public void setHasSword(boolean s) { hasSword = s; }
    public boolean hasBoomerang() { return hasBoomerang; }
    public void setHasBoomerang(boolean b) { hasBoomerang = b; }

    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public void heal(int amount) { health = Math.min(health + amount, maxHealth); }
}
