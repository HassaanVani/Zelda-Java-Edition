package zelda;

import engine.KeyHandler;
import java.awt.*;
import java.io.File;
import javax.swing.ImageIcon;

public class ZeldaPlayer {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;
    public static final double MOVE_SPEED = 1.5;
    public static final int ATTACK_DURATION = 12;
    public static final int INVULN_FRAMES = 24;
    public static final int ANIM_CYCLE_RATE = 10;
    public static final int KNOCKBACK_DISTANCE = 32;
    public static final double KNOCKBACK_SPEED = 2.0;

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

    private boolean bItemKeyReleased = true;
    private int bItemCooldown = 0;
    private static final int B_ITEM_COOLDOWN_FRAMES = 20;
    private boolean candleUsedThisScreen = false;
    private java.util.List<Projectile> roomProjectiles;

    // B-item usage flags for ZeldaGame to react to
    private boolean justUsedRecorder = false;
    private boolean justUsedFood = false;

    private Inventory inventory;
    private String name = "LINK";
    private String currentColorVariant = "Normal";

    private int invulnerableFrames = 0;
    private double knockbackDX = 0;
    private double knockbackDY = 0;
    private int knockbackRemaining = 0;

    private KeyHandler keyHandler;

    private Image[][] walkImages = new Image[4][2];
    private Image[][] attackImages = new Image[4][2];

    public ZeldaPlayer(double x, double y, KeyHandler kh) {
        this.worldX = x;
        this.worldY = y;
        this.keyHandler = kh;
        this.inventory = new Inventory();
        loadSprites();
    }

    public ZeldaPlayer(double x, double y, KeyHandler kh, Inventory inventory) {
        this.worldX = x;
        this.worldY = y;
        this.keyHandler = kh;
        this.inventory = inventory;
        loadSprites();
    }

    private void loadSprites() {
        currentColorVariant = inventory.getLinkColorVariant();
        String swordName = getSwordSpriteName();
        String base = "sprites/Link/Link (" + currentColorVariant + ") ";

        walkImages[DIR_UP][0] = loadGif(base + "(Back).gif");
        walkImages[DIR_UP][1] = walkImages[DIR_UP][0];
        walkImages[DIR_LEFT][0] = loadGif(base + "(Left)1.gif");
        walkImages[DIR_LEFT][1] = loadGif(base + "(Left)2.gif");
        walkImages[DIR_DOWN][0] = loadGif(base + "(Front)1.gif");
        walkImages[DIR_DOWN][1] = loadGif(base + "(Front)2.gif");
        walkImages[DIR_RIGHT][0] = walkImages[DIR_LEFT][0];
        walkImages[DIR_RIGHT][1] = walkImages[DIR_LEFT][1];

        if (swordName != null) {
            attackImages[DIR_UP][0] = loadGif(base + "(Back) - " + swordName + ".gif");
            attackImages[DIR_UP][1] = attackImages[DIR_UP][0];
            attackImages[DIR_LEFT][0] = loadGif(base + "(Left) - " + swordName + "1.gif");
            attackImages[DIR_LEFT][1] = loadGif(base + "(Left) - " + swordName + "2.gif");
            attackImages[DIR_DOWN][0] = loadGif(base + "(Front) - " + swordName + "1.gif");
            attackImages[DIR_DOWN][1] = loadGif(base + "(Front) - " + swordName + "2.gif");
            attackImages[DIR_RIGHT][0] = attackImages[DIR_LEFT][0];
            attackImages[DIR_RIGHT][1] = attackImages[DIR_LEFT][1];
        }
    }

    private String getSwordSpriteName() {
        switch (inventory.getSwordLevel()) {
            case 1: return "Wooden Sword";
            case 2: return "White Sword";
            case 3: return "Magical Sword";
            default: return null;
        }
    }

    public void reloadSprites() {
        if (!currentColorVariant.equals(inventory.getLinkColorVariant())) {
            loadSprites();
        }
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

        if (knockbackRemaining > 0) {
            worldX += knockbackDX;
            worldY += knockbackDY;
            knockbackRemaining -= (int) KNOCKBACK_SPEED;
            if (knockbackRemaining <= 0) {
                knockbackRemaining = 0;
                knockbackDX = 0;
                knockbackDY = 0;
            }
            clampToPlayArea();
            return;
        }

        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) attacking = false;
        }

        if (keyHandler.zPressed && attackKeyReleased && !attacking && inventory.hasSword()) {
            attacking = true;
            attackTimer = ATTACK_DURATION;
            attackKeyReleased = false;
        }
        if (!keyHandler.zPressed) attackKeyReleased = true;

        if (bItemCooldown > 0) bItemCooldown--;
        if (keyHandler.xPressed && bItemKeyReleased && bItemCooldown == 0) {
            useBItem();
            bItemKeyReleased = false;
        }
        if (!keyHandler.xPressed) bItemKeyReleased = true;

        moving = false;
        if (!attacking) {
            int prevDir = direction;
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
            // NES grid alignment: snap to 8px boundary when changing perpendicular axis
            if (moving && prevDir != direction) {
                boolean wasVertical = (prevDir == DIR_UP || prevDir == DIR_DOWN);
                boolean nowVertical = (direction == DIR_UP || direction == DIR_DOWN);
                if (wasVertical != nowVertical) {
                    if (nowVertical) {
                        // Changing from horizontal to vertical: snap X to 8px grid
                        worldX = Math.round(worldX / 8.0) * 8;
                    } else {
                        // Changing from vertical to horizontal: snap Y to 8px grid
                        worldY = Math.round(worldY / 8.0) * 8;
                    }
                }
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
        // NES-style invincibility: alternate between visible and color-tinted frames
        boolean invulnHidden = invulnerableFrames > 0 && (invulnerableFrames / 2) % 3 == 0;
        if (invulnHidden) return;

        boolean isAttack = attacking && inventory.hasSword();
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

            // NES-style color flash overlay during invulnerability
            if (invulnerableFrames > 0) {
                int flashPhase = (invulnerableFrames / 2) % 3;
                Color flashColor;
                switch (flashPhase) {
                    case 1: flashColor = new Color(255, 60, 60, 100); break;   // red tint
                    case 2: flashColor = new Color(60, 60, 255, 100); break;   // blue tint
                    default: flashColor = new Color(255, 255, 255, 80); break;  // white tint
                }
                g2.setColor(flashColor);
                g2.fillRect(drawX, drawY, dw, dh);
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
        if (!attacking || !inventory.hasSword()) return null;
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
        inventory.takeDamage(amount);
        invulnerableFrames = INVULN_FRAMES;

        double dx = worldX - sourceX;
        double dy = worldY - sourceY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            knockbackDX = (dx / dist) * KNOCKBACK_SPEED;
            knockbackDY = (dy / dist) * KNOCKBACK_SPEED;
            knockbackRemaining = KNOCKBACK_DISTANCE;
        }
    }

    public boolean isKnockedBack() { return knockbackRemaining > 0; }

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

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inv) { this.inventory = inv; loadSprites(); }

    public int getHealth() { return inventory.getHealth(); }
    public void setHealth(int h) { inventory.setHealth(h); }
    public int getMaxHealth() { return inventory.getMaxHealth(); }
    public void setMaxHealth(int m) { inventory.setMaxHealth(m); }
    public boolean isAlive() { return inventory.isAlive(); }

    public int getRupees() { return inventory.getRupees(); }
    public void setRupees(int r) { inventory.setRupees(r); }
    public void addRupees(int a) { inventory.addRupees(a); }
    public int getKeys() { return inventory.getKeys(); }
    public void setKeys(int k) { inventory.setKeys(k); }
    public void addKeys(int k) { inventory.addKeys(k); }
    public int getBombs() { return inventory.getBombs(); }
    public void setBombs(int b) { inventory.setBombs(b); }
    public void addBombs(int b) { inventory.addBombs(b); }

    public boolean hasSword() { return inventory.hasSword(); }
    public void setHasSword(boolean s) {
        if (s && inventory.getSwordLevel() == 0) inventory.setSwordLevel(1);
        else if (!s) inventory.setSwordLevel(0);
        loadSprites();
    }
    public boolean hasBoomerang() { return inventory.hasBoomerang(); }
    public void setHasBoomerang(boolean b) {
        if (b && inventory.getBoomerangLevel() == 0) inventory.setBoomerangLevel(1);
        else if (!b) inventory.setBoomerangLevel(0);
    }

    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public void heal(int amount) { inventory.heal(amount); }

    public void setRoomProjectiles(java.util.List<Projectile> projs) { this.roomProjectiles = projs; }
    public void onScreenChange() { candleUsedThisScreen = false; }

    public boolean consumeRecorderFlag() {
        if (justUsedRecorder) { justUsedRecorder = false; return true; }
        return false;
    }
    public boolean consumeFoodFlag() {
        if (justUsedFood) { justUsedFood = false; return true; }
        return false;
    }

    private void useBItem() {
        if (roomProjectiles == null) return;
        Inventory.BItem bItem = inventory.getSelectedBItem();
        if (bItem == Inventory.BItem.NONE) return;

        double cx = worldX + WIDTH / 2;
        double cy = worldY + HEIGHT / 2;
        double speed;
        double vx = 0, vy = 0;

        switch (bItem) {
            case BOOMERANG:
                if (!inventory.hasBoomerang()) return;
                // Only one boomerang at a time — check if one is already active
                for (Projectile p : roomProjectiles) {
                    if (p.isActive() && p.isPlayerProjectile() && p.isReturning() ||
                        (p.isActive() && p.isPlayerProjectile() && p.isBoomerangType())) return;
                }
                speed = inventory.hasMagicalBoomerang() ? 4.0 : 3.0;
                switch (direction) {
                    case DIR_UP:    vy = -speed; break;
                    case DIR_DOWN:  vy = speed; break;
                    case DIR_LEFT:  vx = -speed; break;
                    case DIR_RIGHT: vx = speed; break;
                }
                Projectile boom = new Projectile(cx - 4, cy - 4, vx, vy, true);
                boom.setColor(inventory.hasMagicalBoomerang() ? Color.CYAN : new Color(180, 120, 60));
                boom.setSize(8, 8);
                boom.setDamage(0); // boomerang stuns, doesn't kill
                boom.setReturnTarget(this, inventory.hasMagicalBoomerang() ? 256 : 80);
                boom.setBoomerangType(true);
                roomProjectiles.add(boom);
                bItemCooldown = B_ITEM_COOLDOWN_FRAMES;
                break;

            case BOMB:
                if (!inventory.useBomb()) return;
                Projectile bomb = new Projectile(cx - 4, cy - 4, 0, 0, true);
                bomb.setColor(Color.DARK_GRAY);
                bomb.setSize(12, 12);
                bomb.setDamage(4);
                bomb.setIsBomb(true);
                roomProjectiles.add(bomb);
                bItemCooldown = B_ITEM_COOLDOWN_FRAMES * 2;
                break;

            case BOW:
                if (!inventory.canShootArrow()) return;
                inventory.addRupees(-1);
                speed = 4.0;
                switch (direction) {
                    case DIR_UP:    vy = -speed; break;
                    case DIR_DOWN:  vy = speed; break;
                    case DIR_LEFT:  vx = -speed; break;
                    case DIR_RIGHT: vx = speed; break;
                }
                Projectile arrow = new Projectile(cx - 2, cy - 2, vx, vy, true);
                arrow.setColor(inventory.hasSilverArrows() ? Color.WHITE : new Color(180, 120, 40));
                arrow.setSize(4, 8);
                arrow.setDamage(inventory.hasSilverArrows() ? 4 : 2);
                roomProjectiles.add(arrow);
                bItemCooldown = B_ITEM_COOLDOWN_FRAMES;
                break;

            case CANDLE:
                if (!inventory.hasCandle()) return;
                if (!inventory.hasRedCandle() && candleUsedThisScreen) return;
                speed = 2.0;
                switch (direction) {
                    case DIR_UP:    vy = -speed; break;
                    case DIR_DOWN:  vy = speed; break;
                    case DIR_LEFT:  vx = -speed; break;
                    case DIR_RIGHT: vx = speed; break;
                }
                Projectile fire = new Projectile(cx - 4, cy - 4, vx, vy, true);
                fire.setColor(Color.ORANGE);
                fire.setSize(8, 8);
                fire.setDamage(1);
                fire.setCandleFire(true); // Travel ~48px then become stationary flame
                roomProjectiles.add(fire);
                candleUsedThisScreen = true;
                bItemCooldown = B_ITEM_COOLDOWN_FRAMES;
                break;

            case ROD:
                if (!inventory.hasRod()) return;
                speed = 3.0;
                switch (direction) {
                    case DIR_UP:    vy = -speed; break;
                    case DIR_DOWN:  vy = speed; break;
                    case DIR_LEFT:  vx = -speed; break;
                    case DIR_RIGHT: vx = speed; break;
                }
                Projectile rodBeam = new Projectile(cx - 3, cy - 3, vx, vy, true);
                rodBeam.setColor(new Color(255, 100, 100));
                rodBeam.setSize(6, 6);
                rodBeam.setDamage(inventory.hasBook() ? 2 : 1);
                rodBeam.setLeaveFire(inventory.hasBook());
                roomProjectiles.add(rodBeam);
                bItemCooldown = B_ITEM_COOLDOWN_FRAMES;
                break;

            case RECORDER:
                if (!inventory.hasRecorder()) return;
                justUsedRecorder = true;
                bItemCooldown = B_ITEM_COOLDOWN_FRAMES * 3;
                break;

            case FOOD:
                if (!inventory.hasFood()) return;
                justUsedFood = true;
                bItemCooldown = B_ITEM_COOLDOWN_FRAMES * 2;
                break;

            case POTION:
                if (inventory.getPotionCount() > 0) {
                    inventory.usePotion();
                    bItemCooldown = B_ITEM_COOLDOWN_FRAMES * 3;
                }
                break;

            default:
                break;
        }
    }
}
