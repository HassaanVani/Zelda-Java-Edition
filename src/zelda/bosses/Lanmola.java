package zelda.bosses;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import zelda.*;

/**
 * Lanmola: giant centipede boss.
 * Only the HEAD is vulnerable, body segments are invulnerable.
 * Red: head 4 HP. Blue: head 6 HP.
 * Appears in Dungeon 8 and 2nd quest dungeons.
 */
public class Lanmola extends ZeldaEnemy {
    private boolean isBlue;
    private List<double[]> bodyPositions = new ArrayList<>();
    private static final int BODY_SEGMENTS = 6;
    private static final int SEGMENT_SIZE = 12;
    private static final int HISTORY_SIZE = BODY_SEGMENTS * 8; // positions to track
    private List<double[]> positionHistory = new ArrayList<>();

    private double moveAngle;
    private int turnTimer = 0;
    private static final int TURN_INTERVAL = 40;

    public Lanmola(double x, double y, boolean blue) {
        super(x, y, blue ? 6 : 4, blue ? 2 : 1, AIType.RANDOM);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.lanmolaBlue() : EnemyStats.lanmolaRed());
        this.width = 14;
        this.height = 14;
        this.moveAngle = Math.random() * Math.PI * 2;

        // Initialize body segment positions behind head
        for (int i = 0; i < HISTORY_SIZE; i++) {
            positionHistory.add(new double[]{x, y});
        }
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Direction changes
        turnTimer++;
        if (turnTimer >= TURN_INTERVAL) {
            turnTimer = 0;
            // Aim somewhat toward player
            double dx = player.getWorldX() - x;
            double dy = player.getWorldY() - y;
            double targetAngle = Math.atan2(dy, dx);
            moveAngle += (targetAngle - moveAngle) * 0.3 + (Math.random() - 0.5) * 0.5;
        }

        // Move head
        x += Math.cos(moveAngle) * speed;
        y += Math.sin(moveAngle) * speed;

        // Bounce off walls
        if (x < 12 || x > ZeldaRoom.ROOM_PIXEL_W - width - 12) {
            moveAngle = Math.PI - moveAngle;
            x = Math.max(12, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 12));
        }
        if (y < 12 || y > ZeldaRoom.ROOM_PIXEL_H - height - 12) {
            moveAngle = -moveAngle;
            y = Math.max(12, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 12));
        }

        // Record position history
        positionHistory.add(0, new double[]{x, y});
        if (positionHistory.size() > HISTORY_SIZE) {
            positionHistory.remove(positionHistory.size() - 1);
        }

        // Body segments contact damage (they're invulnerable but still hurt Link)
        if (player != null) {
            for (int i = 0; i < BODY_SEGMENTS; i++) {
                int idx = (i + 1) * 8;
                if (idx < positionHistory.size()) {
                    double[] pos = positionHistory.get(idx);
                    Rectangle segBox = new Rectangle((int)pos[0], (int)pos[1], SEGMENT_SIZE, SEGMENT_SIZE);
                    if (segBox.intersects(player.getHitbox())) {
                        player.takeDamage(damage, pos[0], pos[1]);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        Color bodyColor = isBlue ? new Color(60, 60, 180) : new Color(180, 60, 60);
        Color headColor = isBlue ? new Color(80, 80, 220) : new Color(220, 80, 80);

        // Draw body segments (from tail to head so head draws on top)
        for (int i = BODY_SEGMENTS - 1; i >= 0; i--) {
            int idx = (i + 1) * 8;
            if (idx < positionHistory.size()) {
                double[] pos = positionHistory.get(idx);
                g2.setColor(bodyColor);
                g2.fillOval((int)pos[0], (int)pos[1], SEGMENT_SIZE, SEGMENT_SIZE);
            }
        }

        // Draw head
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(headColor);
        }
        g2.fillOval((int)x, (int)y, width, height);
        // Eyes
        g2.setColor(Color.WHITE);
        g2.fillOval((int)x + 3, (int)y + 3, 4, 4);
        g2.fillOval((int)x + 8, (int)y + 3, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval((int)x + 4, (int)y + 4, 2, 2);
        g2.fillOval((int)x + 9, (int)y + 4, 2, 2);
    }
}
