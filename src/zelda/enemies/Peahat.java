package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Peahat: flying plant enemy.
 * NES-accurate: INVULNERABLE while flying/moving. Only vulnerable when STOPPED.
 * Hover movement in random patterns, periodically stops.
 * 2 HP, 1/2 heart contact.
 */
public class Peahat extends ZeldaEnemy {
    private static final double FLOAT_AMPLITUDE = 2.0;
    private static final double FLOAT_SPEED = 0.15;
    private static final int PAUSE_MIN = 60;
    private static final int PAUSE_RANGE = 60;
    private static final int MOVE_TIMEOUT = 180;

    private double angle = 0;
    private double floatOffset = 0;
    private int moveTimer = 0;
    private boolean flying = true; // true = moving/flying (invulnerable), false = stopped (vulnerable)
    private double targetX, targetY;
    private int pauseTimer = 0;

    public Peahat(double x, double y) {
        super(x, y, 2, 1, AIType.RANDOM);
        applyStats(EnemyStats.peahat());
        loadPeahatSprite();
        targetX = x;
        targetY = y;
    }

    private void loadPeahatSprite() {
        sprite = loadSprite("sprites/Enemies/Peahat.gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (damageTimer > 0) damageTimer--;

        angle += FLOAT_SPEED;
        floatOffset = Math.sin(angle) * FLOAT_AMPLITUDE;

        if (flying) {
            moveTimer++;
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 4) {
                x += (dx / dist) * speed;
                y += (dy / dist) * speed;
            }
            if (dist <= 4 || moveTimer > MOVE_TIMEOUT) {
                flying = false;
                pauseTimer = 0;
            }
        } else {
            pauseTimer++;
            if (pauseTimer > PAUSE_MIN + Math.random() * PAUSE_RANGE) {
                flying = true;
                moveTimer = 0;
                targetX = 32 + Math.random() * 192;
                targetY = 32 + Math.random() * 112;
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        int drawY = (int)(y + floatOffset);
        if (sprite != null) {
            g2.drawImage(sprite, (int) x, drawY, 16, 16, null);
        } else {
            g2.setColor(new Color(180, 100, 50));
            g2.fillOval((int) x, drawY, 16, 16);
        }
        // Show subtle visual cue when stopped (vulnerable)
        if (!flying) {
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRect((int) x, drawY, 16, 16);
        }
        if (invulnerableFrames > 0) {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillRect((int) x, drawY, 16, 16);
        }
    }

    @Override
    public void damage(int amount) {
        // Only vulnerable when stopped (not flying)
        if (flying) return;
        super.damage(amount);
    }

    @Override
    public boolean canDamage() { return active && flying; } // Deals contact damage while flying
}
