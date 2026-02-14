package zelda.bosses;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Gohma: giant spider/crab boss. Only vulnerable when its eye is open.
 * Weak to arrows (1-hit kill with arrow when eye open). Dungeon 6 boss.
 * Moves side to side, periodically opens eye.
 */
public class Gohma extends ZeldaEnemy {
    private boolean eyeOpen = false;
    private int eyeTimer = 0;
    private static final int EYE_CLOSED_DURATION = 90;
    private static final int EYE_OPEN_DURATION = 40;
    private int shootTimer = 0;
    private double moveDir = 1;

    public Gohma(double x, double y) {
        super(x, y, 4, 1, AIType.SHOOTER);
        this.width = 24;
        this.height = 16;
        this.speed = 0.6;
        this.damage = 1;
        sprite = loadSprite("sprites/Bosses/6 - Gohma.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Move side to side
        x += moveDir * speed;
        if (x < 32 || x > ZeldaRoom.ROOM_PIXEL_W - width - 32) {
            moveDir = -moveDir;
        }

        // Eye open/close cycle
        eyeTimer++;
        if (!eyeOpen && eyeTimer >= EYE_CLOSED_DURATION) {
            eyeOpen = true;
            eyeTimer = 0;
        } else if (eyeOpen && eyeTimer >= EYE_OPEN_DURATION) {
            eyeOpen = false;
            eyeTimer = 0;
        }

        // Shoot when eye is open
        if (eyeOpen) {
            shootTimer--;
            if (shootTimer <= 0) {
                double dx = player.getWorldX() - (x + width / 2);
                double dy = player.getWorldY() - (y + height / 2);
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 0) {
                    Projectile fb = new Projectile(x + width / 2, y + height / 2,
                        (dx / dist) * 2.0, (dy / dist) * 2.0, false);
                    fb.setColor(new Color(200, 50, 50));
                    fb.setSize(6, 6);
                    fb.setDamage(1);
                    projectiles.add(fb);
                }
                shootTimer = 30;
            }
        }
    }

    @Override
    public void damage(int amount) {
        // Gohma is only damaged by arrows when eye is open.
        // Regular damage calls (sword, boomerang, etc.) are rejected.
        // Arrow damage is applied via damageByArrow() called from CombatManager.
    }

    /**
     * Called specifically when an arrow hits Gohma. Only works when eye is open.
     * On NES, arrow to open eye is instant kill.
     */
    public void damageByArrow(int amount) {
        if (!eyeOpen) return;
        health -= amount;
        damageTimer = DAMAGE_FLASH_FRAMES;
        invulnerableFrames = DEFAULT_INVULN;
        if (health <= 0) {
            health = 0;
            active = false;
        }
    }

    public boolean isEyeOpen() { return eyeOpen; }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        boolean flash = damageTimer > 0 && (damageTimer / 3) % 2 == 0;

        if (sprite != null && !flash) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            // Body
            g2.setColor(flash ? Color.WHITE : new Color(120, 60, 30));
            g2.fillOval((int)x, (int)y, width, height);

            // Legs
            g2.setColor(flash ? Color.WHITE : new Color(100, 50, 25));
            for (int i = 0; i < 4; i++) {
                int lx = (int)x + 2 + i * 6;
                g2.fillRect(lx, (int)y + height - 2, 3, 6);
            }

            // Eye
            if (eyeOpen) {
                g2.setColor(Color.RED);
                g2.fillOval((int)x + width / 2 - 3, (int)y + height / 2 - 3, 6, 6);
                g2.setColor(Color.BLACK);
                g2.fillOval((int)x + width / 2 - 1, (int)y + height / 2 - 1, 2, 2);
            } else {
                g2.setColor(new Color(80, 40, 20));
                g2.drawLine((int)x + width / 2 - 4, (int)y + height / 2,
                           (int)x + width / 2 + 4, (int)y + height / 2);
            }
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 6, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((double)health / maxHealth * width);
        g2.fillRect((int)x, (int)y - 6, hw, 4);
    }
}
