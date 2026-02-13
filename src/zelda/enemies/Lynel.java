package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Lynel: centaur-like enemy that shoots sword beams at the player.
 * Red: 4 HP, 2 damage. Blue: 6 HP, 4 damage.
 */
public class Lynel extends ZeldaEnemy {
    private boolean isBlue;
    private int shootCooldown = 0;
    private static final int SHOOT_INTERVAL = 70;

    public Lynel(double x, double y, boolean blue) {
        super(x, y, blue ? 6 : 4, blue ? 4 : 2, AIType.SHOOTER);
        this.isBlue = blue;
        this.speed = blue ? 0.75 : 0.6;
        sprite = loadSprite(blue ? "sprites/Enemies/Lynel (Blue).png" : "sprites/Enemies/Lynel (Red).png");
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
            if (dist < 130 && dist > 0) {
                double spd = 2.5;
                Projectile beam = new Projectile(x + width / 2, y + height / 2,
                    (dx / dist) * spd, (dy / dist) * spd, false);
                beam.setColor(isBlue ? new Color(100, 100, 255) : new Color(255, 200, 100));
                beam.setSize(6, 6);
                beam.setDamage(isBlue ? 4 : 2);
                projectiles.add(beam);
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
            g2.setColor(isBlue ? new Color(60, 60, 180) : new Color(180, 60, 40));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.YELLOW);
            g2.fillRect((int)x + 6, (int)y, 4, 10);
        }
    }
}
