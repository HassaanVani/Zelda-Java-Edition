package zelda.bosses;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Ganon: final boss of Level 9.
 * NES-accurate 3-phase system:
 * - Phase 1: INVISIBLE. Teleports around room. Shoots fireballs. Sword hits reveal briefly.
 * - Phase 2: After enough hits (~8 Magical Sword), turns brown/stunned, becomes visible.
 * - Phase 3: Must be finished with SILVER ARROW. Without Silver Arrows, Ganon CANNOT be killed.
 * 2 hearts contact damage.
 */
public class Ganon extends ZeldaEnemy {
    private enum Phase { INVISIBLE, STUNNED, DEAD }
    private Phase phase = Phase.INVISIBLE;

    private boolean visible = false;
    private int visibleTimer = 0;
    private int teleportTimer = 0;
    private int shootTimer = 0;
    private int hitCount = 0;
    private static final int HITS_TO_STUN = 8;
    private static final int VISIBLE_DURATION = 20;
    private static final int TELEPORT_INTERVAL = 60;
    private static final int SHOOT_INTERVAL = 45;

    // NES teleport positions: X=$30(48) or X=$B0(176), Y=$A0(160)
    private static final double[] TELEPORT_X = {48, 176, 80, 144};
    private static final double TELEPORT_Y = 80;

    public Ganon(double x, double y) {
        super(x, y, 8, 4, AIType.SHOOTER);
        applyStats(EnemyStats.ganon());
        this.width = 24;
        this.height = 32;
        this.speed = 0;
        this.damage = 4; // 2 hearts
        sprite = loadSprite("sprites/Bosses/9 - Ganon.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        switch (phase) {
            case INVISIBLE:
                // Teleport around
                teleportTimer++;
                if (teleportTimer >= TELEPORT_INTERVAL) {
                    int idx = (int)(Math.random() * TELEPORT_X.length);
                    x = TELEPORT_X[idx];
                    y = TELEPORT_Y + (Math.random() - 0.5) * 40;
                    teleportTimer = 0;
                }

                // Visibility timer (revealed by sword hits)
                if (visible) {
                    visibleTimer--;
                    if (visibleTimer <= 0) visible = false;
                }

                // Shoot fireballs
                shootTimer--;
                if (shootTimer <= 0) {
                    shootFireballs(player, projectiles);
                    shootTimer = SHOOT_INTERVAL;
                }
                break;

            case STUNNED:
                // Visible, stunned, waiting for silver arrow
                visible = true;
                // Still shoots occasionally but slower
                shootTimer--;
                if (shootTimer <= 0) {
                    shootFireballs(player, projectiles);
                    shootTimer = SHOOT_INTERVAL * 2;
                }
                break;

            case DEAD:
                active = false;
                break;
        }
    }

    private void shootFireballs(ZeldaPlayer player, List<Projectile> projectiles) {
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            double baseAngle = Math.atan2(dy, dx);
            double spd = 2.0;
            for (int i = -1; i <= 1; i++) {
                double angle = baseAngle + i * (Math.PI / 8);
                Projectile fb = new Projectile(x + width / 2, y + height / 2,
                    Math.cos(angle) * spd, Math.sin(angle) * spd, false);
                fb.setColor(new Color(100, 0, 200));
                fb.setSize(8, 8);
                fb.setDamage(4); // 2 hearts
                projectiles.add(fb);
            }
        }
    }

    @Override
    public void damage(int amount) {
        if (invulnerableFrames > 0) return;

        if (phase == Phase.INVISIBLE) {
            // Sword hit reveals Ganon briefly and counts toward stun threshold
            visible = true;
            visibleTimer = VISIBLE_DURATION;
            damageTimer = DAMAGE_FLASH_FRAMES;
            invulnerableFrames = DEFAULT_INVULN;
            hitCount += amount;

            if (hitCount >= HITS_TO_STUN) {
                // Transition to stunned phase
                phase = Phase.STUNNED;
                visible = true;
            }
        }
        // In STUNNED phase, normal weapons don't kill — only silver arrow
    }

    /**
     * Called specifically when a silver arrow hits Ganon.
     * Only silver arrows can deliver the killing blow (in stunned phase).
     */
    public void damageBysilverArrow(int amount) {
        if (invulnerableFrames > 0) return;

        if (phase == Phase.INVISIBLE) {
            // Silver arrow in invisible phase: counts as hit + reveals
            damage(amount);
        }

        if (phase == Phase.STUNNED) {
            // Silver arrow in stunned phase: KILL
            health = 0;
            phase = Phase.DEAD;
            active = false;
            damageTimer = DAMAGE_FLASH_FRAMES;
        }
    }

    @Override
    public boolean canDamage() {
        return active; // Contact damage even when invisible
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        if (!visible) {
            // Draw faint shimmer
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            g2.setColor(new Color(80, 0, 160));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setComposite(old);
            return;
        }

        boolean flash = damageTimer > 0 && (damageTimer / 3) % 2 == 0;
        boolean stunned = (phase == Phase.STUNNED);

        if (sprite != null && !flash) {
            if (stunned) {
                // Brown tint for stunned phase
                g2.drawImage(sprite, (int)x, (int)y, width, height, null);
                g2.setColor(new Color(139, 69, 19, 100)); // brown overlay
                g2.fillRect((int)x, (int)y, width, height);
            } else {
                g2.drawImage(sprite, (int)x, (int)y, width, height, null);
            }
        } else {
            Color bodyColor = stunned ? new Color(139, 69, 19) : new Color(60, 0, 120);
            g2.setColor(flash ? Color.WHITE : bodyColor);
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
        g2.setColor(stunned ? Color.YELLOW : Color.RED);
        double hpFraction = (double) Math.max(0, HITS_TO_STUN - hitCount) / HITS_TO_STUN;
        if (stunned) hpFraction = 0.1; // Low bar in stunned state
        int hw = (int)(hpFraction * width);
        g2.fillRect((int)x, (int)y - 8, hw, 4);
    }
}
