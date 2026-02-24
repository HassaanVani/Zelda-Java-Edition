package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Bubble: invulnerable enemy that bounces diagonally around the room.
 * Contact disables Link's sword temporarily instead of dealing damage.
 * Red Bubble disables sword, Blue Bubble re-enables it (NES behavior).
 */
public class Bubble extends ZeldaEnemy {
    private double vx, vy;
    private boolean isBlue;
    private static final double SPEED = 1.0;

    public Bubble(double x, double y, boolean isBlue) {
        super(x, y, 999, 0, AIType.RANDOM);
        applyStats(EnemyStats.bubble());
        this.isBlue = isBlue;

        // Start moving in a random diagonal direction
        vx = (Math.random() < 0.5) ? SPEED : -SPEED;
        vy = (Math.random() < 0.5) ? SPEED : -SPEED;

        String color = isBlue ? "Blue" : "Red";
        sprite = loadSprite("sprites/Enemies/Bubble - " + color + ".gif");
    }

    public Bubble(double x, double y) {
        this(x, y, false);
    }

    @Override
    public void damage(int amount) {
        // Bubbles are completely invulnerable
    }

    @Override
    public boolean canDamage() {
        // Bubbles don't deal normal contact damage; they have a special effect
        return false;
    }

    public boolean isBlue() { return isBlue; }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;

        x += vx;
        y += vy;

        // Bounce off room edges
        if (x <= 8 || x >= ZeldaRoom.ROOM_PIXEL_W - width - 8) {
            vx = -vx;
            x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        }
        if (y <= 8 || y >= ZeldaRoom.ROOM_PIXEL_H - height - 8) {
            vy = -vy;
            y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
        }

        // Check contact with player — disable/enable sword
        if (player != null && getHitbox().intersects(player.getHitbox())) {
            if (isBlue) {
                player.getInventory().enableSword();
            } else {
                player.getInventory().disableSword();
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(isBlue ? new Color(100, 100, 255) : new Color(255, 100, 100));
            g2.fillOval((int)x, (int)y, width, height);
            g2.setColor(Color.WHITE);
            g2.drawOval((int)x, (int)y, width, height);
        }
    }
}
