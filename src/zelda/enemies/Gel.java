package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Gel: tiny blob that hops randomly. 1 HP, no drops.
 * Spawned by Zol when Zol is hit with non-killing damage.
 */
public class Gel extends ZeldaEnemy {
    private int hopTimer = 0;
    private static final int HOP_INTERVAL_MIN = 20;
    private static final int HOP_INTERVAL_RANGE = 40;
    private int nextHop;
    private boolean hopping = false;
    private double hopVX, hopVY;
    private int hopFrames = 0;
    private static final int HOP_DURATION = 8;

    public Gel(double x, double y) {
        super(x, y, 1, 1, AIType.RANDOM);
        applyStats(EnemyStats.gel());
        this.width = 8;
        this.height = 8;
        nextHop = HOP_INTERVAL_MIN + (int)(Math.random() * HOP_INTERVAL_RANGE);
        sprite = loadSprite("sprites/Enemies/Gel - Green.gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (hopping) {
            x += hopVX;
            y += hopVY;
            hopFrames++;
            if (hopFrames >= HOP_DURATION) {
                hopping = false;
                hopTimer = 0;
            }
        } else {
            hopTimer++;
            if (hopTimer >= nextHop) {
                hopping = true;
                hopFrames = 0;
                double angle = Math.random() * Math.PI * 2;
                hopVX = Math.cos(angle) * 1.5;
                hopVY = Math.sin(angle) * 1.5;
                nextHop = HOP_INTERVAL_MIN + (int)(Math.random() * HOP_INTERVAL_RANGE);
            }
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
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
            g2.setColor(new Color(60, 180, 60));
            g2.fillRect((int)x, (int)y, width, height);
        }
    }
}
