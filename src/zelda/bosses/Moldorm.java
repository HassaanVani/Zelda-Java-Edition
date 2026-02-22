package zelda.bosses;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import zelda.*;

/**
 * Moldorm: segmented worm boss.
 * Multiple segments follow the head in a chain.
 * Each segment: 1 HP, independently destructible.
 * Unpredictable movement patterns.
 * 1/2 heart contact per segment.
 */
public class Moldorm extends ZeldaEnemy {
    private List<MoldormSegment> segments = new ArrayList<>();
    private static final int SEGMENT_COUNT = 5;
    private static final int SEGMENT_SIZE = 10;
    private static final double MOVE_SPEED = 1.0;
    private double moveAngle;
    private int turnTimer = 0;
    private static final int TURN_INTERVAL = 30;

    public Moldorm(double x, double y) {
        super(x, y, 1, 1, AIType.RANDOM);
        applyStats(EnemyStats.moldorm());
        this.width = 12;
        this.height = 12;
        this.moveAngle = Math.random() * Math.PI * 2;

        // Create trailing body segments
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            segments.add(new MoldormSegment(x - (i + 1) * 10, y));
        }
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Unpredictable direction changes
        turnTimer++;
        if (turnTimer >= TURN_INTERVAL) {
            turnTimer = 0;
            moveAngle += (Math.random() - 0.5) * Math.PI * 0.8;
        }

        // Move head
        double nx = x + Math.cos(moveAngle) * MOVE_SPEED;
        double ny = y + Math.sin(moveAngle) * MOVE_SPEED;

        // Bounce off walls
        if (nx < 12 || nx > ZeldaRoom.ROOM_PIXEL_W - width - 12) {
            moveAngle = Math.PI - moveAngle;
            nx = Math.max(12, Math.min(nx, ZeldaRoom.ROOM_PIXEL_W - width - 12));
        }
        if (ny < 12 || ny > ZeldaRoom.ROOM_PIXEL_H - height - 12) {
            moveAngle = -moveAngle;
            ny = Math.max(12, Math.min(ny, ZeldaRoom.ROOM_PIXEL_H - height - 12));
        }

        // Update segments to follow (chain behavior)
        double prevX = x, prevY = y;
        for (MoldormSegment seg : segments) {
            if (!seg.alive) { prevX = seg.x; prevY = seg.y; continue; }
            double dx = prevX - seg.x;
            double dy = prevY - seg.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > SEGMENT_SIZE) {
                seg.x += (dx / dist) * (dist - SEGMENT_SIZE);
                seg.y += (dy / dist) * (dist - SEGMENT_SIZE);
            }
            prevX = seg.x;
            prevY = seg.y;
        }

        x = nx;
        y = ny;

        // Check segment collision with player projectiles
        for (MoldormSegment seg : segments) {
            if (!seg.alive) continue;
            Rectangle segHitbox = seg.getHitbox();
            for (Projectile p : projectiles) {
                if (p.isActive() && p.isPlayerProjectile() && !p.isReturning()
                    && !(p.isBomb() && p.getBombPhase() != 2)
                    && p.getHitbox().intersects(segHitbox)) {
                    seg.alive = false;
                    if (!p.isPiercing()) p.deactivate();
                    break;
                }
            }
            // Segment contact damage
            if (seg.alive && player != null && segHitbox.intersects(player.getHitbox())) {
                player.takeDamage(damage, seg.x, seg.y);
            }
        }

        // Head dies = whole Moldorm dies. Or all segments dead = die.
        boolean anyAlive = false;
        for (MoldormSegment seg : segments) {
            if (seg.alive) { anyAlive = true; break; }
        }
        if (!anyAlive && health <= 0) {
            active = false;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        // Draw segments (tail to head)
        for (int i = segments.size() - 1; i >= 0; i--) {
            MoldormSegment seg = segments.get(i);
            if (seg.alive) seg.render(g2);
        }

        // Draw head
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(new Color(200, 100, 40));
        }
        g2.fillOval((int)x, (int)y, width, height);
        g2.setColor(Color.BLACK);
        g2.fillOval((int)x + 3, (int)y + 3, 3, 3);
        g2.fillOval((int)x + 7, (int)y + 3, 3, 3);
    }

    /** Inner class for body segments. */
    private static class MoldormSegment {
        double x, y;
        boolean alive = true;

        MoldormSegment(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Rectangle getHitbox() {
            return new Rectangle((int)x, (int)y, SEGMENT_SIZE, SEGMENT_SIZE);
        }

        void render(Graphics2D g2) {
            g2.setColor(new Color(180, 80, 30));
            g2.fillOval((int)x, (int)y, SEGMENT_SIZE, SEGMENT_SIZE);
        }
    }
}
