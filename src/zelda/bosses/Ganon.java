package zelda.bosses;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Ganon: final boss of Level 9. Invisible, teleports around room,
 * shoots fireballs. Can only be damaged when briefly visible after
 * being hit by a Silver Arrow. 8 HP, 4 damage.
 */
public class Ganon extends ZeldaEnemy {
    private boolean visible = false;
    private int visibleTimer = 0;
    private int teleportTimer = 0;
    private int shootTimer = 0;
    private static final int VISIBLE_DURATION = 30;
    private static final int TELEPORT_INTERVAL = 60;
    private static final int SHOOT_INTERVAL = 45;

    public Ganon(double x, double y) {
        super(x, y, 8, 4, AIType.SHOOTER);
        this.width = 24;
        this.height = 32;
        this.speed = 0;
        this.damage = 4;
        sprite = loadSprite("sprites/Bosses/9 - Ganon.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Teleport around
        teleportTimer++;
        if (teleportTimer >= TELEPORT_INTERVAL) {
            x = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_W - 64);
            y = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_H - 64);
            teleportTimer = 0;
        }

        // Visibility timer
        if (visible) {
            visibleTimer--;
            if (visibleTimer <= 0) {
                visible = false;
            }
        }

        // Shoot fireballs from invisible position
        shootTimer--;
        if (shootTimer <= 0) {
            double dx = player.getWorldX() - x;
            double dy = player.getWorldY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                // Shoot spread of fireballs
                double baseAngle = Math.atan2(dy, dx);
                for (int i = -1; i <= 1; i++) {
                    double angle = baseAngle + i * (Math.PI / 8);
                    double spd = 2.0;
                    Projectile fb = new Projectile(x + width / 2, y + height / 2,
                        Math.cos(angle) * spd, Math.sin(angle) * spd, false);
                    fb.setColor(new Color(100, 0, 200));
                    fb.setSize(8, 8);
                    fb.setDamage(4);
                    projectiles.add(fb);
                }
            }
            shootTimer = SHOOT_INTERVAL;
        }
    }

    @Override
    public void damage(int amount) {
        if (invulnerableFrames > 0) return;

        // Ganon becomes visible when hit
        visible = true;
        visibleTimer = VISIBLE_DURATION;
        damageTimer = DAMAGE_FLASH_FRAMES;
        invulnerableFrames = DEFAULT_INVULN;

        // Only silver arrows can actually kill (damage >= 4)
        if (amount >= 4) {
            health -= amount;
            if (health <= 0) {
                active = false;
            }
        }
    }

    @Override
    public boolean canDamage() {
        return active; // Always deals contact damage (invisible or not)
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        if (!visible) {
            // Draw a faint shimmer to hint at position
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            g2.setColor(new Color(80, 0, 160));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setComposite(old);
            return;
        }

        boolean flash = damageTimer > 0 && (damageTimer / 3) % 2 == 0;

        if (sprite != null && !flash) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(flash ? Color.WHITE : new Color(60, 0, 120));
            g2.fillRect((int)x, (int)y, width, height);
            // Eyes
            g2.setColor(Color.RED);
            g2.fillOval((int)x + 5, (int)y + 8, 4, 4);
            g2.fillOval((int)x + 15, (int)y + 8, 4, 4);
            // Trident
            g2.setColor(new Color(200, 200, 0));
            g2.fillRect((int)x + width / 2 - 1, (int)y + height - 4, 2, 8);
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 8, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((double)health / maxHealth * width);
        g2.fillRect((int)x, (int)y - 8, hw, 4);
    }
}
