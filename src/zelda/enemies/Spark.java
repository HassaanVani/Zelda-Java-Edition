package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Spark (Anti-Fairy): invulnerable enemy that travels along walls/blocks.
 * Contact disables Link's sword (similar to Bubble).
 * Cannot be killed by any weapon.
 */
public class Spark extends ZeldaEnemy {
    private double vx, vy;
    private static final double SPEED = 1.0;

    public Spark(double x, double y) {
        super(x, y, 999, 0, AIType.RANDOM);
        applyStats(EnemyStats.spark());
        // Start moving in a random cardinal direction
        switch ((int)(Math.random() * 4)) {
            case 0: vx = 0; vy = -SPEED; break;
            case 1: vx = -SPEED; vy = 0; break;
            case 2: vx = 0; vy = SPEED; break;
            default: vx = SPEED; vy = 0; break;
        }
    }

    @Override
    public void damage(int amount) {
        // Invulnerable
    }

    @Override
    public boolean canDamage() { return false; } // Special effect, not normal damage

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;

        // Move along walls — try to keep a wall on the left side
        double nx = x + vx;
        double ny = y + vy;

        boolean canMove = room != null && room.isWalkable((int)(nx + width / 2), (int)(ny + height / 2));
        if (!canMove || nx < 4 || nx > ZeldaRoom.ROOM_PIXEL_W - width - 4
            || ny < 4 || ny > ZeldaRoom.ROOM_PIXEL_H - height - 4) {
            // Turn right (clockwise) when hitting a wall
            double oldVx = vx;
            vx = -vy;
            vy = oldVx;
        } else {
            x = nx;
            y = ny;
        }

        // Check contact — disable sword
        if (player != null && getHitbox().intersects(player.getHitbox())) {
            player.getInventory().disableSword();
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        // Draw as a small glowing orb
        g2.setColor(new Color(255, 255, 100));
        g2.fillOval((int)x + 2, (int)y + 2, width - 4, height - 4);
        g2.setColor(new Color(255, 200, 60));
        g2.drawOval((int)x + 1, (int)y + 1, width - 2, height - 2);
    }
}
