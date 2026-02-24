package zelda.bosses;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import zelda.*;

/**
 * Gleeok: multi-headed dragon boss.
 * NES-accurate:
 * - Multiple heads on necks, each head must be destroyed independently
 * - Destroyed heads detach and become floating, invulnerable fireball shooters
 * - Each head: ~10 HP (reduced per head for balance). Heads shoot fireballs.
 * - Variants: 2-headed (D4), 3-headed (D6), 4-headed (D8)
 * - 1 heart contact
 */
public class Gleeok extends ZeldaEnemy {
    private int totalHeads;
    private int headsAlive;
    private int[] headHP;
    private double[][] headOffset;
    private double[] headAngle;
    private List<DetachedHead> detachedHeads = new ArrayList<>();
    private int shootTimer = 0;
    private static final int SHOOT_INTERVAL = 80;
    private static final int HEAD_HP_EACH = 10; // NES: $A0/$10 = 10 wooden sword hits per head

    public Gleeok(double x, double y) {
        this(x, y, 2);
    }

    public Gleeok(double x, double y, int heads) {
        super(x, y, heads * HEAD_HP_EACH, 2, AIType.SHOOTER);
        applyStats(EnemyStats.gleeok());
        this.width = 24;
        this.height = 32;
        this.speed = 0.2;
        this.totalHeads = heads;
        this.headsAlive = heads;
        this.headHP = new int[heads];
        this.headOffset = new double[heads][2];
        this.headAngle = new double[heads];

        for (int i = 0; i < heads; i++) {
            headHP[i] = HEAD_HP_EACH;
            headAngle[i] = (Math.PI * 2 / heads) * i;
            headOffset[i][0] = Math.cos(headAngle[i]) * 16;
            headOffset[i][1] = -12 + Math.sin(headAngle[i]) * 8;
        }
        sprite = loadSprite("sprites/Bosses/4 - Gleeok.gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Sway body left-right
        x += Math.sin(System.currentTimeMillis() * 0.002) * speed;
        x = Math.max(32, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 32));

        // Animate head positions
        for (int i = 0; i < totalHeads; i++) {
            if (headHP[i] > 0) {
                headAngle[i] += 0.03 + i * 0.01;
                headOffset[i][0] = Math.cos(headAngle[i]) * 18;
                headOffset[i][1] = -14 + Math.sin(headAngle[i]) * 10;
            }
        }

        // Shoot fireballs from alive heads
        shootTimer--;
        if (shootTimer <= 0) {
            for (int i = 0; i < totalHeads; i++) {
                if (headHP[i] > 0) {
                    double hx = x + width / 2 + headOffset[i][0];
                    double hy = y + headOffset[i][1];
                    double dx = player.getWorldX() - hx;
                    double dy = player.getWorldY() - hy;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0) {
                        Projectile fb = new Projectile(hx, hy, (dx / dist) * 1.5, (dy / dist) * 1.5, false);
                        fb.setColor(Color.ORANGE);
                        fb.setSize(8, 8);
                        fb.setDamage(EnemyStats.FIREBALL_DAMAGE);
                        projectiles.add(fb);
                    }
                }
            }
            shootTimer = SHOOT_INTERVAL;
        }

        // Update detached heads (floating invulnerable fireball shooters)
        for (DetachedHead dh : detachedHeads) {
            dh.update(player, projectiles);
        }
    }

    @Override
    public void damage(int amount) {
        if (invulnerableFrames > 0) return;

        // Damage first alive head
        for (int i = 0; i < totalHeads; i++) {
            if (headHP[i] > 0) {
                headHP[i] -= amount;
                if (headHP[i] <= 0) {
                    // Head detached! Create floating invulnerable fireball shooter
                    double hx = x + width / 2 + headOffset[i][0];
                    double hy = y + headOffset[i][1];
                    detachedHeads.add(new DetachedHead(hx, hy));
                    headsAlive--;
                }
                break;
            }
        }

        damageTimer = DAMAGE_FLASH_FRAMES;
        invulnerableFrames = DEFAULT_INVULN;
        health -= amount;
        if (health <= 0 || headsAlive <= 0) {
            active = false;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        boolean flash = damageTimer > 0 && (damageTimer / 3) % 2 == 0;

        // Draw necks and alive heads
        for (int i = 0; i < totalHeads; i++) {
            if (headHP[i] > 0) {
                int hx = (int)(x + width / 2 + headOffset[i][0]);
                int hy = (int)(y + headOffset[i][1]);
                int bx = (int)(x + width / 2);
                int by = (int)(y + 4);

                g2.setColor(flash ? Color.WHITE : new Color(100, 160, 60));
                g2.drawLine(bx, by, hx, hy);
                g2.setColor(flash ? Color.WHITE : new Color(60, 140, 40));
                g2.fillOval(hx - 5, hy - 5, 10, 10);
                g2.setColor(Color.RED);
                g2.fillRect(hx - 1, hy - 2, 2, 2);
            }
        }

        // Body
        if (sprite != null && !flash) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(flash ? Color.WHITE : new Color(80, 140, 40));
            g2.fillRect((int)x, (int)y, width, height);
        }

        // Draw detached floating heads
        for (DetachedHead dh : detachedHeads) {
            dh.render(g2);
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 8, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((double)Math.max(0, health) / maxHealth * width);
        g2.fillRect((int)x, (int)y - 8, hw, 4);
    }

    /**
     * Detached head: invulnerable floating fireball shooter.
     * Bounces around room and periodically shoots at Link.
     */
    private static class DetachedHead {
        double x, y, vx, vy;
        int shootTimer = 60;
        static final int SHOOT_INTERVAL = 90;

        DetachedHead(double x, double y) {
            this.x = x;
            this.y = y;
            this.vx = (Math.random() < 0.5 ? 1 : -1) * (0.5 + Math.random() * 0.5);
            this.vy = (Math.random() < 0.5 ? 1 : -1) * (0.5 + Math.random() * 0.5);
        }

        void update(ZeldaPlayer player, List<Projectile> projectiles) {
            x += vx;
            y += vy;

            if (x < 8 || x > ZeldaRoom.ROOM_PIXEL_W - 16) { vx = -vx; x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - 16)); }
            if (y < 8 || y > ZeldaRoom.ROOM_PIXEL_H - 16) { vy = -vy; y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - 16)); }

            shootTimer--;
            if (shootTimer <= 0) {
                double dx = player.getWorldX() - x;
                double dy = player.getWorldY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 0) {
                    Projectile fb = new Projectile(x, y, (dx / dist) * 1.5, (dy / dist) * 1.5, false);
                    fb.setColor(new Color(255, 140, 0));
                    fb.setSize(6, 6);
                    fb.setDamage(EnemyStats.FIREBALL_DAMAGE);
                    projectiles.add(fb);
                }
                shootTimer = SHOOT_INTERVAL;
            }
        }

        void render(Graphics2D g2) {
            // Ghostly detached head
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2.setColor(new Color(140, 200, 80));
            g2.fillOval((int)x - 4, (int)y - 4, 8, 8);
            g2.setColor(Color.RED);
            g2.fillRect((int)x - 1, (int)y - 1, 2, 2);
            g2.setComposite(old);
        }
    }
}
