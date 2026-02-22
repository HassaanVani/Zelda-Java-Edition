package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Boulder: falling rock hazard in Death Mountain area (overworld row 0-1).
 * Invincible environmental hazard that falls from the top of the screen.
 * 1/2 heart contact damage.
 */
public class Boulder extends ZeldaEnemy {
    private static final double FALL_SPEED = 2.0;
    private boolean falling = false;
    private int spawnTimer = 0;
    private static final int SPAWN_INTERVAL = 60; // New boulder every ~1 second
    private double startX;

    public Boulder(double x, double y) {
        super(x, y, 999, 1, AIType.RANDOM);
        applyStats(EnemyStats.boulder());
        this.startX = x;
        this.y = -16; // Start above screen
        this.falling = true;
    }

    @Override
    public void damage(int amount) {
        // Invulnerable
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;

        if (falling) {
            y += FALL_SPEED;
            // Slight horizontal wobble
            x += (Math.random() - 0.5) * 0.5;

            // When it falls past the bottom, respawn at top with new random X
            if (y > ZeldaRoom.ROOM_PIXEL_H + 16) {
                respawn();
            }
        } else {
            spawnTimer++;
            if (spawnTimer >= SPAWN_INTERVAL) {
                falling = true;
                spawnTimer = 0;
            }
        }
    }

    private void respawn() {
        x = 16 + Math.random() * (ZeldaRoom.ROOM_PIXEL_W - 32);
        y = -16;
        falling = true;
    }

    @Override
    public boolean canDamage() { return active && falling && y > 0; }

    @Override
    public Rectangle getHitbox() {
        if (!falling || y < 0) return new Rectangle(0, 0, 0, 0);
        return super.getHitbox();
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active || !falling || y < -8) return;
        // Draw as a dark rock
        g2.setColor(new Color(100, 80, 60));
        g2.fillOval((int)x, (int)y, width, height);
        g2.setColor(new Color(80, 60, 40));
        g2.drawOval((int)x, (int)y, width, height);
        g2.fillOval((int)x + 3, (int)y + 3, 5, 4);
    }
}
