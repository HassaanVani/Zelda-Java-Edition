package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Armos: statue enemy that activates when Link touches it, then chases him.
 * 3 HP, 1 damage. Starts dormant until player makes contact.
 */
public class Armos extends ZeldaEnemy {
    private boolean dormant = true;

    public Armos(double x, double y) {
        super(x, y, 3, 1, AIType.CHASE);
        applyStats(EnemyStats.armos());
        sprite = loadSprite("sprites/Enemies/Armos.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (dormant) {
            double dx = player.getWorldX() - x;
            double dy = player.getWorldY() - y;
            if (Math.abs(dx) < 18 && Math.abs(dy) < 18) {
                dormant = false;
            }
            return;
        }

        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            double nx = x + (dx / dist) * speed;
            double ny = y + (dy / dist) * speed;
            if (room != null && room.isWalkable((int)(nx + width / 2), (int)(ny + height / 2))) {
                x = nx; y = ny;
            }
        }
        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
    }

    @Override
    public boolean canDamage() { return active && !dormant; }

    @Override
    public void damage(int amount) {
        if (dormant) { dormant = false; return; }
        super.damage(amount);
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(dormant ? new Color(140, 140, 140) : new Color(180, 100, 60));
            g2.fillRect((int)x, (int)y, width, height);
            if (!dormant) {
                g2.setColor(Color.BLACK);
                g2.fillRect((int)x + 4, (int)y + 4, 3, 3);
                g2.fillRect((int)x + 10, (int)y + 4, 3, 3);
            }
        }
    }
}
