package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Pols Voice: rabbit-like enemy that hops around. Weak to arrows (1-hit kill).
 * 6 HP normally, 1 HP vs arrows. Bounces diagonally.
 */
public class PolsVoice extends ZeldaEnemy {
    private double vx, vy;

    public PolsVoice(double x, double y) {
        super(x, y, 6, 1, AIType.RANDOM);
        applyStats(EnemyStats.polsVoice());
        vx = (Math.random() < 0.5 ? -1 : 1) * speed;
        vy = (Math.random() < 0.5 ? -1 : 1) * speed;
        sprite = loadSprite("sprites/Enemies/Pols Voice.gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        x += vx;
        y += vy;

        if (x < 8 || x > ZeldaRoom.ROOM_PIXEL_W - width - 8) {
            vx = -vx;
            x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        }
        if (y < 8 || y > ZeldaRoom.ROOM_PIXEL_H - height - 8) {
            vy = -vy;
            y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
        }
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
            g2.setColor(new Color(200, 180, 160));
            g2.fillOval((int)x, (int)y, width, height);
            g2.setColor(new Color(220, 200, 180));
            g2.fillOval((int)x + 3, (int)y - 4, 4, 8);
            g2.fillOval((int)x + 10, (int)y - 4, 4, 8);
        }
    }
}
