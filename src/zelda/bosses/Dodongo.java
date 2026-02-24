package zelda.bosses;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Dodongo: dinosaur boss that can only be damaged by bombs fed into its mouth.
 * Walks in straight lines, turns on wall collision. Immune to sword.
 * 2 bomb hits to kill. Dungeon 2 boss.
 */
public class Dodongo extends ZeldaEnemy {
    private int bombsEaten = 0;
    private static final int BOMBS_TO_KILL = 2;
    private int stunTimer = 0;
    private BufferedImage frontSprite;
    private BufferedImage backSprite;
    private BufferedImage leftSprite;

    public Dodongo(double x, double y) {
        super(x, y, 4, 1, AIType.RANDOM);
        applyStats(EnemyStats.dodongo());
        this.width = 24;
        this.height = 16;
        this.speed = 0.6;
        this.direction = (int)(Math.random() * 4);
        loadDodongoSprites();
    }

    private void loadDodongoSprites() {
        frontSprite = loadSprite("sprites/Bosses/2 - Dodongo (Front).gif");
        backSprite = loadSprite("sprites/Bosses/2 - Dodongo (Back).gif");
        leftSprite = loadSprite("sprites/Bosses/2 - Dodongo (Left).gif");
        sprite = frontSprite;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (stunTimer > 0) {
            stunTimer--;
            return;
        }

        // Check if walking into a bomb projectile (player-owned, stationary)
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            if (p.isPlayerOwned() && p.isActive() && p.getDamage() >= 4) {
                if (getHitbox().intersects(p.getHitbox())) {
                    bombsEaten++;
                    p.deactivate();
                    stunTimer = 60;
                    damageTimer = DAMAGE_FLASH_FRAMES;
                    if (bombsEaten >= BOMBS_TO_KILL) {
                        health = 0;
                        active = false;
                    }
                    return;
                }
            }
        }

        // Walk in current direction
        double nx = x, ny = y;
        switch (direction) {
            case 0: ny -= speed; break;
            case 1: nx -= speed; break;
            case 2: ny += speed; break;
            case 3: nx += speed; break;
        }

        boolean blocked = false;
        if (nx < 8 || nx > ZeldaRoom.ROOM_PIXEL_W - width - 8) blocked = true;
        if (ny < 8 || ny > ZeldaRoom.ROOM_PIXEL_H - height - 8) blocked = true;

        if (!blocked) {
            x = nx;
            y = ny;
        } else {
            direction = (direction + 1 + (int)(Math.random() * 3)) % 4;
        }

        // Update sprite based on direction
        switch (direction) {
            case 0: // up (back)
                sprite = backSprite;
                break;
            case 1: // left
                sprite = leftSprite;
                break;
            case 2: // down (front)
                sprite = frontSprite;
                break;
            case 3: // right (flip left)
                sprite = leftSprite; // will flip in render
                break;
        }
    }

    @Override
    public void damage(int amount) {
        // Immune to sword — only bombs work (handled in update)
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            if (direction == 3 && leftSprite != null) {
                // Flip horizontally for right-facing
                g2.drawImage(sprite, (int)x + width, (int)y, -width, height, null);
            } else {
                g2.drawImage(sprite, (int)x, (int)y, width, height, null);
            }
        } else {
            g2.setColor(new Color(120, 80, 40));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(new Color(100, 60, 30));
            g2.fillRect((int)x + 2, (int)y + 2, 8, 6);
            g2.setColor(Color.RED);
            g2.fillRect((int)x + 4, (int)y + 3, 3, 3);
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 6, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((1.0 - (double)bombsEaten / BOMBS_TO_KILL) * width);
        g2.fillRect((int)x, (int)y - 6, hw, 4);
    }
}
