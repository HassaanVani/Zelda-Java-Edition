package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Rope: snake enemy that charges at the player when aligned horizontally.
 * 1 HP, fast charge speed.
 */
public class Rope extends ZeldaEnemy {
    private boolean charging = false;
    private int chargeDir = 3; // 1=left, 3=right
    private static final double CHARGE_SPEED = 2.5;
    private static final double NORMAL_SPEED = 0.5;

    public Rope(double x, double y) {
        super(x, y, 1, 1, AIType.CHASE);
        applyStats(EnemyStats.rope());
        sprite = loadSprite("sprites/Enemies/Rope.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        double dy = player.getWorldY() - y;
        double dx = player.getWorldX() - x;

        if (!charging && Math.abs(dy) < 8 && Math.abs(dx) < 80) {
            charging = true;
            chargeDir = dx > 0 ? 3 : 1;
        }

        if (charging) {
            double nx = (chargeDir == 3) ? x + CHARGE_SPEED : x - CHARGE_SPEED;
            if (room != null && room.isWalkable((int)(nx + width / 2), (int)(y + height / 2))) {
                x = nx;
            } else {
                charging = false;
            }
            if (x < 4 || x > ZeldaRoom.ROOM_PIXEL_W - width - 4) charging = false;
        } else {
            if (room != null) randomMove(room);
        }

        x = Math.max(4, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 4));
        y = Math.max(4, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 4));
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
            g2.setColor(new Color(200, 100, 40));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.BLACK);
            g2.fillRect((int)x + 2, (int)y + 6, 3, 3);
        }
    }
}
