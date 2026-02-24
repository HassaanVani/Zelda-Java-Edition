package zelda.enemies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Goriya: throws boomerangs at the player. Moves randomly.
 * Red Goriya: 3 HP. Blue Goriya: 5 HP, faster boomerang.
 */
public class Goriya extends ZeldaEnemy {
    private boolean isBlue;
    private int shootCooldown = 0;
    private boolean boomerangOut = false; // NES: Goriya pauses while boomerang is active
    private static final int SHOOT_INTERVAL = 90;
    private BufferedImage frontSprite;
    private BufferedImage backSprite;
    private BufferedImage leftSprite;

    public Goriya(double x, double y, boolean blue) {
        super(x, y, 3, 1, AIType.SHOOTER);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.goriyaBlue() : EnemyStats.goriyaRed());
        loadGoriyaSprites();
    }

    private void loadGoriyaSprites() {
        String color = isBlue ? "Blue" : "Red";
        frontSprite = loadSprite("sprites/Enemies/Goriya - " + color + " (Front).gif");
        backSprite = loadSprite("sprites/Enemies/Goriya - " + color + " (Back).gif");
        leftSprite = loadSprite("sprites/Enemies/Goriya - " + color + " (Left).gif");
        sprite = frontSprite;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // NES: Goriya pauses while boomerang is out
        if (boomerangOut) {
            // Check if our boomerang is still active
            boolean found = false;
            for (Projectile p : projectiles) {
                if (p.isActive() && !p.isPlayerProjectile() && p.getOwner() == this) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                boomerangOut = false;
                shootCooldown = SHOOT_INTERVAL;
            }
            return; // Don't move while boomerang is out
        }

        if (room != null) randomMove(room);

        shootCooldown--;
        if (shootCooldown <= 0) {
            // NES: shoot boomerang in facing direction (cardinal)
            double boomSpeed = isBlue ? 2.5 : 2.0;
            double vx = 0, vy = 0;
            switch (direction) {
                case 0: vy = -boomSpeed; break;
                case 1: vx = -boomSpeed; break;
                case 2: vy = boomSpeed; break;
                case 3: vx = boomSpeed; break;
            }
            Projectile boom = new Projectile(x + width / 2, y + height / 2, vx, vy, false);
            boom.setColor(new Color(180, 120, 60));
            boom.setSize(8, 8);
            boom.setDamage(EnemyStats.BOOMERANG_PROJ_DAMAGE);
            boom.setOwner(this);
            projectiles.add(boom);
            boomerangOut = true;
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
                g2.setColor(isBlue ? new Color(60, 100, 200) : new Color(200, 80, 60));
                g2.fillRect((int)x, (int)y, width, height);
                g2.setColor(Color.YELLOW);
                g2.fillOval((int)x + 4, (int)y + 4, 8, 8);
            }
        }
    }
}
