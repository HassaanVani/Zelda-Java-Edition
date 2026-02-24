package zelda.enemies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Lynel: centaur-like enemy that shoots sword beams at the player.
 * Red: 4 HP, 2 damage. Blue: 6 HP, 4 damage.
 */
public class Lynel extends ZeldaEnemy {
    private boolean isBlue;
    private int shootCooldown = 0;
    private static final int SHOOT_INTERVAL = 70;
    private BufferedImage frontSprite;
    private BufferedImage backSprite;
    private BufferedImage leftSprite;

    public Lynel(double x, double y, boolean blue) {
        super(x, y, 4, 2, AIType.SHOOTER);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.lynelBlue() : EnemyStats.lynelRed());
        loadLynelSprites();
    }

    private void loadLynelSprites() {
        String color = isBlue ? "Blue" : "Red";
        frontSprite = loadSprite("sprites/Enemies/Lynel - " + color + " (Front).gif");
        backSprite = loadSprite("sprites/Enemies/Lynel - " + color + " (Back).gif");
        leftSprite = loadSprite("sprites/Enemies/Lynel - " + color + " (Left).gif");
        sprite = frontSprite;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (room != null) randomMove(room);

        shootCooldown--;
        if (shootCooldown <= 0) {
            double vx = 0, vy = 0;
            double spd = 2.5;
            switch (direction) {
                case 0: vy = -spd; break; // up
                case 1: vx = -spd; break; // left
                case 2: vy = spd; break;  // down
                case 3: vx = spd; break;  // right
            }
            Projectile beam = new Projectile(x + width / 2, y + height / 2, vx, vy, false);
            beam.setColor(isBlue ? new Color(100, 100, 255) : new Color(255, 200, 100));
            beam.setSize(6, 6);
            beam.setDamage(EnemyStats.SWORD_BEAM_DAMAGE);
            projectiles.add(beam);
            shootCooldown = SHOOT_INTERVAL;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else {
            BufferedImage currentSprite = null;
            boolean flipH = false;
            switch (direction) {
                case 0: // up (back)
                    currentSprite = backSprite;
                    break;
                case 1: // left
                    currentSprite = leftSprite;
                    break;
                case 2: // down (front)
                    currentSprite = frontSprite;
                    break;
                case 3: // right (flip left horizontally)
                    currentSprite = leftSprite;
                    flipH = true;
                    break;
            }

            if (currentSprite != null) {
                if (flipH) {
                    g2.drawImage(currentSprite, (int)x + width, (int)y, -width, height, null);
                } else {
                    g2.drawImage(currentSprite, (int)x, (int)y, width, height, null);
                }
            } else {
                g2.setColor(isBlue ? new Color(60, 60, 180) : new Color(180, 60, 40));
                g2.fillRect((int)x, (int)y, width, height);
                g2.setColor(Color.YELLOW);
                g2.fillRect((int)x + 6, (int)y, 4, 10);
            }
        }
    }
}
