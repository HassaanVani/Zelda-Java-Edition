package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Trap: invulnerable blade that sits still until Link aligns on an axis,
 * then charges toward him. After reaching the target or hitting a wall,
 * it returns to its home position. NES-accurate behavior.
 */
public class Trap extends ZeldaEnemy {
    private enum TrapState { IDLE, CHARGING, RETURNING }

    private TrapState trapState = TrapState.IDLE;
    private final double homeX, homeY;
    private double moveVX, moveVY;
    private static final double CHARGE_SPEED = 3.0;
    private static final double RETURN_SPEED = 1.0;
    private static final int ALIGN_THRESHOLD = 8;

    public Trap(double x, double y) {
        super(x, y, 999, 1, AIType.RANDOM);
        applyStats(EnemyStats.trap());
        homeX = x;
        homeY = y;
        sprite = loadSprite("sprites/Enemies/Trap.gif");
    }

    @Override
    public void damage(int amount) {
        // Traps are completely invulnerable
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active || player == null) return;

        switch (trapState) {
            case IDLE:
                // Check if Link is aligned horizontally or vertically
                double px = player.getWorldX() + ZeldaPlayer.WIDTH / 2.0;
                double py = player.getWorldY() + ZeldaPlayer.HEIGHT / 2.0;
                double cx = x + width / 2.0;
                double cy = y + height / 2.0;

                if (Math.abs(px - cx) < ALIGN_THRESHOLD) {
                    // Vertically aligned — charge up or down
                    moveVX = 0;
                    moveVY = (py > cy) ? CHARGE_SPEED : -CHARGE_SPEED;
                    trapState = TrapState.CHARGING;
                } else if (Math.abs(py - cy) < ALIGN_THRESHOLD) {
                    // Horizontally aligned — charge left or right
                    moveVX = (px > cx) ? CHARGE_SPEED : -CHARGE_SPEED;
                    moveVY = 0;
                    trapState = TrapState.CHARGING;
                }
                break;

            case CHARGING:
                x += moveVX;
                y += moveVY;

                // Stop at room edges
                if (x <= 8 || x >= ZeldaRoom.ROOM_PIXEL_W - width - 8
                    || y <= 8 || y >= ZeldaRoom.ROOM_PIXEL_H - height - 8) {
                    x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
                    y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
                    trapState = TrapState.RETURNING;
                }
                break;

            case RETURNING:
                double dx = homeX - x;
                double dy = homeY - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < RETURN_SPEED) {
                    x = homeX;
                    y = homeY;
                    trapState = TrapState.IDLE;
                } else {
                    x += (dx / dist) * RETURN_SPEED;
                    y += (dy / dist) * RETURN_SPEED;
                }
                break;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.GRAY);
            g2.drawRect((int)x, (int)y, width, height);
            // Draw X pattern
            g2.drawLine((int)x, (int)y, (int)x + width, (int)y + height);
            g2.drawLine((int)x + width, (int)y, (int)x, (int)y + height);
        }
    }
}
