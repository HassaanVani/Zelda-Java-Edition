package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Goriya: throws boomerangs at the player. Moves randomly.
 * Red Goriya: 3 HP. Blue Goriya: 5 HP, faster boomerang.
 */
public class Goriya extends ZeldaEnemy {
    private boolean isBlue;
    private int shootCooldown = 0;
    private static final int SHOOT_INTERVAL = 90;

    public Goriya(double x, double y, boolean blue) {
        super(x, y, 3, 1, AIType.SHOOTER);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.goriyaBlue() : EnemyStats.goriyaRed());
        sprite = loadSprite(blue ? "sprites/Enemies/Goriya (Blue).png" : "sprites/Enemies/Goriya (Red).png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (room != null) randomMove(room);

        shootCooldown--;
        if (shootCooldown <= 0) {
            double dx = player.getWorldX() - x;
            double dy = player.getWorldY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 120) {
                double boomSpeed = isBlue ? 2.5 : 2.0;
                double vx = (dx / dist) * boomSpeed;
                double vy = (dy / dist) * boomSpeed;
                Projectile boom = new Projectile(x + width / 2, y + height / 2, vx, vy, false);
                boom.setColor(new Color(180, 120, 60));
                boom.setSize(8, 8);
                boom.setDamage(1);
                projectiles.add(boom);
                shootCooldown = SHOOT_INTERVAL;
            }
        }
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
            g2.setColor(isBlue ? new Color(60, 100, 200) : new Color(200, 80, 60));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.YELLOW);
            g2.fillOval((int)x + 4, (int)y + 4, 8, 8);
        }
    }
}
