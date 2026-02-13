package zelda.bosses;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Gleeok: multi-headed dragon boss. Each head shoots fireballs.
 * Detached heads become flying projectile-heads.
 * Dungeon 4 (2 heads), Dungeon 8 (4 heads). HP = heads * 4.
 */
public class Gleeok extends ZeldaEnemy {
    private int totalHeads;
    private int headsAlive;
    private int[] headHP;
    private double[][] headOffset; // relative x,y offset from body per head
    private double[] headAngle;
    private int shootTimer = 0;
    private static final int SHOOT_INTERVAL = 80;

    public Gleeok(double x, double y) {
        this(x, y, 2);
    }

    public Gleeok(double x, double y, int heads) {
        super(x, y, heads * 4, 2, AIType.SHOOTER);
        this.width = 24;
        this.height = 32;
        this.speed = 0.2;
        this.damage = 2;
        this.totalHeads = heads;
        this.headsAlive = heads;
        this.headHP = new int[heads];
        this.headOffset = new double[heads][2];
        this.headAngle = new double[heads];

        for (int i = 0; i < heads; i++) {
            headHP[i] = 4;
            headAngle[i] = (Math.PI * 2 / heads) * i;
            headOffset[i][0] = Math.cos(headAngle[i]) * 16;
            headOffset[i][1] = -12 + Math.sin(headAngle[i]) * 8;
        }
        sprite = loadSprite("sprites/Bosses/Gleeok.png");
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
                        fb.setDamage(2);
                        projectiles.add(fb);
                    }
                }
            }
            shootTimer = SHOOT_INTERVAL;
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

        // Draw necks and heads
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

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 8, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((double)health / maxHealth * width);
        g2.fillRect((int)x, (int)y - 8, hw, 4);
    }
}
