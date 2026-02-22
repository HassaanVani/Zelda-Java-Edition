package zelda.bosses;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Aquamentus: dragon boss for Dungeons 1 and 7.
 * NES-accurate:
 * - Bounces horizontally between X=$88 (136) and X=$C8 (200)
 * - Fires 3 fireballs in spread (straight + angled up + angled down)
 * - Fire interval: random $70+ frames (~112+ frames)
 * - 6 HP, 1/2 heart contact
 */
public class Aquamentus extends ZeldaEnemy {
    private int shootTimer = 0;
    private static final int SHOOT_COOLDOWN_MIN = 112;
    private static final int SHOOT_COOLDOWN_RANGE = 48;
    private static final double MIN_X = 136; // $88
    private static final double MAX_X = 200; // $C8

    private double moveDir = -1; // Start moving left
    private int animFrame = 0;
    private int animTimer = 0;

    public Aquamentus(double x, double y) {
        super(x, y, 6, 1, AIType.SHOOTER);
        applyStats(EnemyStats.aquamentus());
        this.width = 24;
        this.height = 32;
        this.speed = 0.3;
        this.shootTimer = SHOOT_COOLDOWN_MIN;
        loadAquamentusSprite();
    }

    private void loadAquamentusSprite() {
        sprite = loadSprite("sprites/Bosses/1 - Aquamentus-1.gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;

        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (shootTimer > 0) shootTimer--;

        // Animation
        animTimer++;
        if (animTimer >= 15) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 2;
            String spritePath = "sprites/Bosses/1 - Aquamentus-" + (animFrame + 1) + ".gif";
            sprite = loadSprite(spritePath);
        }

        // NES-accurate horizontal bounce between min and max X
        x += moveDir * speed;
        if (x <= MIN_X) { moveDir = 1; x = MIN_X; }
        if (x >= MAX_X) { moveDir = -1; x = MAX_X; }

        // Fire 3-spread fireballs
        if (shootTimer <= 0) {
            shootFireballs(player, projectiles);
            shootTimer = SHOOT_COOLDOWN_MIN + (int)(Math.random() * SHOOT_COOLDOWN_RANGE);
        }
    }

    private void shootFireballs(ZeldaPlayer player, List<Projectile> projectiles) {
        // NES fires 3 fireballs: straight left, angled up-left, angled down-left
        double projSpeed = 1.5;
        double spread = Math.PI / 6; // 30 degrees

        for (int i = -1; i <= 1; i++) {
            double angle = Math.PI + i * spread; // PI = straight left
            double vx = Math.cos(angle) * projSpeed;
            double vy = Math.sin(angle) * projSpeed;

            Projectile fireball = new Projectile(x, y + height / 2, vx, vy, false);
            fireball.setColor(Color.ORANGE);
            fireball.setSize(10, 10);
            fireball.setDamage(1); // 1/2 heart
            projectiles.add(fireball);
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, null);
        } else {
            g2.setColor(new Color(0, 100, 0));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.RED);
            g2.fillOval((int)x + width - 8, (int)y + 8, 6, 6);
            g2.setColor(new Color(0, 80, 0));
            g2.fillRect((int)x - 8, (int)y + 12, 10, 4);
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 6, width, 4);
        g2.setColor(Color.RED);
        int healthWidth = (int)((double)health / maxHealth * width);
        g2.fillRect((int)x, (int)y - 6, healthWidth, 4);
    }
}
