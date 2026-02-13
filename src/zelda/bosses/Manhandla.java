package zelda.bosses;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Manhandla: plant boss with 4 claws. Each claw shoots fireballs.
 * Destroying claws speeds it up. Weak to bombs. Dungeon 3 boss.
 * Total 8 HP (2 per claw). Speed increases as claws are destroyed.
 */
public class Manhandla extends ZeldaEnemy {
    private int clawsAlive = 4;
    private boolean[] clawActive = {true, true, true, true}; // N, W, S, E
    private int[] clawHP = {2, 2, 2, 2};
    private int shootTimer = 0;
    private static final int SHOOT_INTERVAL = 90;
    private double vx, vy;

    public Manhandla(double x, double y) {
        super(x, y, 8, 2, AIType.RANDOM);
        this.width = 24;
        this.height = 24;
        this.speed = 0.5;
        this.damage = 2;
        vx = speed;
        vy = speed;
        sprite = loadSprite("sprites/Bosses/3 - Manhandla.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Movement — bounces off walls, gets faster with fewer claws
        double currentSpeed = speed + (4 - clawsAlive) * 0.3;
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

        // Shoot fireballs from active claws
        shootTimer--;
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
            shootTimer = SHOOT_INTERVAL - (4 - clawsAlive) * 10;
        }
    }

    @Override
    public void damage(int amount) {
        if (invulnerableFrames > 0) return;

        // Damage a random active claw
        for (int i = 0; i < 4; i++) {
            if (clawActive[i]) {
                clawHP[i] -= amount;
                if (clawHP[i] <= 0) {
                    clawActive[i] = false;
                    clawsAlive--;
                }
                break;
            }
        }

        damageTimer = DAMAGE_FLASH_FRAMES;
        invulnerableFrames = DEFAULT_INVULN;
        health -= amount;
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
        int hw = (int)((double)health / maxHealth * width);
        g2.fillRect((int)x, (int)y - 6, hw, 4);
    }
}
