package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Ghini: ghost enemy found in graveyards. Floats around erratically.
 * The first Ghini is the "real" one; touching gravestones spawns more.
 * 9 HP (only the original can be killed to despawn all).
 */
public class Ghini extends ZeldaEnemy {
    private double floatAngle = Math.random() * Math.PI * 2;
    private double floatRadius = 30 + Math.random() * 20;
    private double centerX, centerY;

    public Ghini(double x, double y) {
        super(x, y, 10, 1, AIType.RANDOM);
        applyStats(EnemyStats.ghini());
        this.centerX = x;
        this.centerY = y;
        sprite = loadSprite("sprites/Enemies/Ghini.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        floatAngle += 0.03;
        x = centerX + Math.cos(floatAngle) * floatRadius;
        y = centerY + Math.sin(floatAngle) * floatRadius;

        // Slowly drift center towards player
        double dx = player.getWorldX() - centerX;
        double dy = player.getWorldY() - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            centerX += (dx / dist) * 0.2;
            centerY += (dy / dist) * 0.2;
        }

        centerX = Math.max(16, Math.min(centerX, ZeldaRoom.ROOM_PIXEL_W - 16));
        centerY = Math.max(16, Math.min(centerY, ZeldaRoom.ROOM_PIXEL_H - 16));
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
            g2.setColor(new Color(200, 200, 220, 180));
            g2.fillOval((int)x, (int)y, width, height);
            g2.setColor(Color.BLACK);
            g2.fillRect((int)x + 4, (int)y + 5, 3, 3);
            g2.fillRect((int)x + 10, (int)y + 5, 3, 3);
        }
    }
}
