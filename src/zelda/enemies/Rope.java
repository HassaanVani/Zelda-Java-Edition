package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Rope: snake enemy that charges at the player when aligned horizontally OR vertically.
 * NES-accurate: wanders slowly, then charges at ~3x speed when axis-aligned with Link.
 * 1 HP, 1/2 heart contact.
 */
public class Rope extends ZeldaEnemy {
    private boolean charging = false;
    private int chargeDir = 3; // 0=up, 1=left, 2=down, 3=right
    private static final double CHARGE_SPEED = 3.0;
    private static final double NORMAL_SPEED = 0.5;
    private static final int ALIGN_THRESHOLD = 8;
    private static final int CHARGE_RANGE = 96;

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

        if (!charging) {
            // Check horizontal alignment (same row) — charge left/right
            if (Math.abs(dy) < ALIGN_THRESHOLD && Math.abs(dx) < CHARGE_RANGE && Math.abs(dx) > 4) {
                charging = true;
                chargeDir = dx > 0 ? 3 : 1;
            }
            // Check vertical alignment (same column) — charge up/down
            else if (Math.abs(dx) < ALIGN_THRESHOLD && Math.abs(dy) < CHARGE_RANGE && Math.abs(dy) > 4) {
                charging = true;
                chargeDir = dy > 0 ? 2 : 0;
            }
        }

        if (charging) {
            double nx = x, ny = y;
            switch (chargeDir) {
                case 0: ny -= CHARGE_SPEED; break;
                case 1: nx -= CHARGE_SPEED; break;
                case 2: ny += CHARGE_SPEED; break;
                case 3: nx += CHARGE_SPEED; break;
            }
            if (room != null && room.isWalkable((int)(nx + width / 2), (int)(ny + height / 2))) {
                x = nx;
                y = ny;
            } else {
                charging = false;
            }
            // Stop charging at room edges
            if (x < 4 || x > ZeldaRoom.ROOM_PIXEL_W - width - 4
                || y < 4 || y > ZeldaRoom.ROOM_PIXEL_H - height - 4) {
                charging = false;
            }
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
