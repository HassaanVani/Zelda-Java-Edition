package zelda.bosses;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Manhandla: plant boss with 4 arm/pincer segments.
 * NES-accurate:
 * - Each arm shoots fireballs independently, can be destroyed individually (1 hit each)
 * - Speed INCREASES each time an arm is destroyed (gets very fast with 1 arm left)
 * - Well-placed bomb in center kills ALL arms instantly
 * - 1/2 heart contact per arm
 */
public class Manhandla extends ZeldaEnemy {
    private int clawsAlive = 4;
    private boolean[] clawActive = {true, true, true, true}; // N, W, S, E
    private int shootTimer = 0;
    private static final int SHOOT_INTERVAL_BASE = 90;
    private double vx, vy;
    private static final double BASE_SPEED = 0.5;
    private static final double SPEED_INCREASE_PER_ARM = 0.5; // Significant speed increase

    public Manhandla(double x, double y) {
        super(x, y, 4, 1, AIType.RANDOM); // 4 HP total (1 per arm)
        this.width = 24;
        this.height = 24;
        this.speed = BASE_SPEED;
        this.damage = 1; // 1/2 heart
        vx = speed;
        vy = speed;
        sprite = loadSprite("sprites/Bosses/3 - Manhandla.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Speed increases as arms are destroyed
        double currentSpeed = BASE_SPEED + (4 - clawsAlive) * SPEED_INCREASE_PER_ARM;
        double mag = Math.sqrt(vx * vx + vy * vy);
        if (mag > 0) {
            vx = (vx / mag) * currentSpeed;
            vy = (vy / mag) * currentSpeed;
        }

        x += vx;
        y += vy;

        if (x < 16 || x > ZeldaRoom.ROOM_PIXEL_W - width - 16) {
            vx = -vx;
            x = Math.max(16, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 16));
        }
        if (y < 16 || y > ZeldaRoom.ROOM_PIXEL_H - height - 16) {
            vy = -vy;
            y = Math.max(16, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 16));
        }

        // Check for bomb in center — kills all arms instantly
        for (Projectile p : projectiles) {
            if (p.isActive() && p.isPlayerProjectile() && p.isBomb() && p.getBombPhase() == 2) {
                double cx = x + width / 2;
                double cy = y + height / 2;
                if (p.getHitbox().contains(cx, cy)) {
                    // Bomb hit center — destroy all arms
                    for (int i = 0; i < 4; i++) clawActive[i] = false;
                    clawsAlive = 0;
                    health = 0;
                    active = false;
                    p.deactivate();
                    return;
                }
            }
        }

        // Shoot fireballs from active claws
        shootTimer--;
        int interval = SHOOT_INTERVAL_BASE - (4 - clawsAlive) * 15;
        if (shootTimer <= 0 && clawsAlive > 0) {
            double[][] clawDirs = {{0, -1}, {-1, 0}, {0, 1}, {1, 0}};
            for (int i = 0; i < 4; i++) {
                if (clawActive[i]) {
                    double cx = x + width / 2 + clawDirs[i][0] * 14;
                    double cy = y + height / 2 + clawDirs[i][1] * 14;
                    Projectile fb = new Projectile(cx, cy, clawDirs[i][0] * 1.5, clawDirs[i][1] * 1.5, false);
                    fb.setColor(new Color(0, 200, 0));
                    fb.setSize(6, 6);
                    fb.setDamage(1);
                    projectiles.add(fb);
                }
            }
            shootTimer = Math.max(30, interval);
        }
    }

    @Override
    public void damage(int amount) {
        if (invulnerableFrames > 0) return;

        // Destroy a random active claw (1 hit each)
        java.util.List<Integer> alive = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (clawActive[i]) alive.add(i);
        }
        if (!alive.isEmpty()) {
            int idx = alive.get((int)(Math.random() * alive.size()));
            clawActive[idx] = false;
            clawsAlive--;
        }

        damageTimer = DAMAGE_FLASH_FRAMES;
        invulnerableFrames = DEFAULT_INVULN;
        health--;
        if (health <= 0 || clawsAlive <= 0) {
            active = false;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
            return;
        }

        // Body
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(new Color(0, 140, 0));
            g2.fillOval((int)x + 4, (int)y + 4, width - 8, height - 8);

            // Claws
            g2.setColor(new Color(0, 180, 0));
            int cx = (int)x + width / 2 - 4;
            int cy = (int)y + height / 2 - 4;
            if (clawActive[0]) g2.fillRect(cx, (int)y - 4, 8, 8);
            if (clawActive[1]) g2.fillRect((int)x - 4, cy, 8, 8);
            if (clawActive[2]) g2.fillRect(cx, (int)y + height - 4, 8, 8);
            if (clawActive[3]) g2.fillRect((int)x + width - 4, cy, 8, 8);
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 6, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((double)clawsAlive / 4.0 * width);
        g2.fillRect((int)x, (int)y - 6, hw, 4);
    }
}
