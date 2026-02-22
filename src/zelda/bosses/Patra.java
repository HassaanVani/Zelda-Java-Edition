package zelda.bosses;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import zelda.*;

/**
 * Patra: Dungeon 9 mini-boss.
 * Central eye parent + 8 smaller eyes orbiting.
 * Must kill ALL 8 orbiters first, then core becomes vulnerable.
 * Orbiters: 1 HP each. Core: ~4 HP.
 */
public class Patra extends ZeldaEnemy {
    private List<PatraOrbiter> orbiters = new ArrayList<>();
    private double centerAngle = 0;
    private static final double ORBIT_SPEED = 0.04;
    private static final double OUTER_RADIUS = 44; // $2C
    private static final double INNER_RADIUS = 24; // $18
    private boolean outerOrbit = true;
    private int orbitSwitchTimer = 0;
    private static final int ORBIT_SWITCH_INTERVAL = 300;

    private double moveVX = 0.25, moveVY = 0.25;

    public Patra(double x, double y) {
        super(x, y, 4, 2, AIType.RANDOM);
        applyStats(EnemyStats.patraCore());
        this.width = 16;
        this.height = 16;
        this.immunityMask = EnemyStats.DMG_ALL; // Core invulnerable until all orbiters dead

        // Create 8 orbiting eyes
        for (int i = 0; i < 8; i++) {
            orbiters.add(new PatraOrbiter(i * (Math.PI * 2 / 8)));
        }
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Move core around room
        x += moveVX;
        y += moveVY;
        if (x < 24 || x > ZeldaRoom.ROOM_PIXEL_W - width - 24) {
            moveVX = -moveVX;
            x = Math.max(24, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 24));
        }
        if (y < 24 || y > ZeldaRoom.ROOM_PIXEL_H - height - 24) {
            moveVY = -moveVY;
            y = Math.max(24, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 24));
        }

        // Update orbit
        centerAngle += ORBIT_SPEED;
        orbitSwitchTimer++;
        if (orbitSwitchTimer >= ORBIT_SWITCH_INTERVAL) {
            outerOrbit = !outerOrbit;
            orbitSwitchTimer = 0;
        }

        double radius = outerOrbit ? OUTER_RADIUS : INNER_RADIUS;

        // Remove dead orbiters and update living ones
        orbiters.removeIf(o -> !o.alive);
        for (PatraOrbiter o : orbiters) {
            o.update(x + width / 2.0, y + height / 2.0, radius, centerAngle);
        }

        // Core becomes vulnerable when all orbiters are dead
        if (orbiters.isEmpty()) {
            immunityMask = 0; // Now vulnerable to all weapons
        }

        // Check orbiter collision with player projectiles
        for (PatraOrbiter o : orbiters) {
            if (!o.alive) continue;
            Rectangle orbHitbox = o.getHitbox();
            for (Projectile p : projectiles) {
                if (p.isActive() && p.isPlayerProjectile() && !p.isReturning()
                    && p.getHitbox().intersects(orbHitbox)) {
                    o.alive = false;
                    if (!p.isPiercing()) p.deactivate();
                    break;
                }
            }
        }

        // Orbiter contact damage
        if (player != null) {
            for (PatraOrbiter o : orbiters) {
                if (o.alive && o.getHitbox().intersects(player.getHitbox())) {
                    player.takeDamage(damage, o.x, o.y);
                    break;
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        // Draw orbiters first (behind core)
        for (PatraOrbiter o : orbiters) {
            if (o.alive) o.render(g2);
        }

        // Draw core
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else {
            g2.setColor(orbiters.isEmpty() ? new Color(220, 60, 60) : new Color(100, 60, 60));
            g2.fillOval((int)x, (int)y, width, height);
            // Eye detail
            g2.setColor(Color.WHITE);
            g2.fillOval((int)x + 4, (int)y + 4, 8, 8);
            g2.setColor(Color.BLACK);
            g2.fillOval((int)x + 6, (int)y + 6, 4, 4);
        }
    }

    /** Inner class for orbiting eyes. */
    private static class PatraOrbiter {
        double x, y;
        double angleOffset;
        boolean alive = true;
        static final int SIZE = 8;

        PatraOrbiter(double angleOffset) {
            this.angleOffset = angleOffset;
        }

        void update(double centerX, double centerY, double radius, double masterAngle) {
            double angle = masterAngle + angleOffset;
            x = centerX + Math.cos(angle) * radius - SIZE / 2.0;
            y = centerY + Math.sin(angle) * radius - SIZE / 2.0;
        }

        Rectangle getHitbox() {
            return new Rectangle((int)x, (int)y, SIZE, SIZE);
        }

        void render(Graphics2D g2) {
            g2.setColor(new Color(180, 80, 80));
            g2.fillOval((int)x, (int)y, SIZE, SIZE);
            g2.setColor(Color.WHITE);
            g2.fillOval((int)x + 2, (int)y + 2, 4, 4);
            g2.setColor(Color.BLACK);
            g2.fillOval((int)x + 3, (int)y + 3, 2, 2);
        }
    }
}
