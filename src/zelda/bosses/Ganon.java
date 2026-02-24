package zelda.bosses;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Ganon: final boss of Level 9.
 * NES-accurate (Z_04.asm:10284-10545, Ganon_ScenePhase2):
 * - BLUE state: Invisible. Teleports like Blue Wizzrobe. Shoots fireballs every $40 frames.
 *   Sword hits reveal briefly + count toward brown threshold.
 * - BROWN state: Visible. State counter counts DOWN every other frame.
 *   >= $30: drawn opaque. < $30: drawn translucent (flickering).
 *   When counter reaches 0: returns to BLUE/invisible + randomize location.
 *   Silver Arrow during brown state = KILL.
 * - DYING state: Burst rays + ashes + spawn Triforce of Power.
 * Contact damage: 8 half-hearts (4 hearts). Fireball damage: 2 half-hearts.
 */
public class Ganon extends ZeldaEnemy {
    private enum Phase { BLUE, BROWN, DYING, DEAD }
    private Phase phase = Phase.BLUE;

    private boolean visible = false;
    private int visibleTimer = 0;
    private int shootTimer = 0;
    private int hitCount = 0;
    private int brownStateCounter = 0; // NES ObjState: counts down from $60 to 0
    private int dyingCounter = 0;      // NES Ganon_ObjPhase: counts up from 1

    private static final int HITS_TO_BROWN = 8;  // ~8 Magical Sword hits
    private static final int VISIBLE_DURATION = 20;
    private static final int SHOOT_INTERVAL = 64; // NES: $40 frames
    private static final int BROWN_INITIAL = 0x60; // NES: starts at $60, counts to 0

    // NES teleport positions (Z_04.asm:10416): Y=$A0, X=random $30 or $B0
    private static final double[] TELEPORT_X = {48, 176}; // $30, $B0
    private static final double TELEPORT_Y = 80;           // $A0 adjusted for Java coords

    private BufferedImage[] blueFrames = new BufferedImage[5];
    private BufferedImage[] redFrames = new BufferedImage[5];
    private int animFrame = 0;
    private int animTimer = 0;
    private int frameCounter = 0; // local frame counter for various timing
    private static final int ANIM_SPEED = 8;

    public Ganon(double x, double y) {
        super(x, y, 8, 4, AIType.SHOOTER);
        applyStats(EnemyStats.ganon());
        this.width = 24;
        this.height = 32;
        this.speed = 0;
        loadGanonSprites();
    }

    private void loadGanonSprites() {
        for (int i = 0; i < 5; i++) {
            blueFrames[i] = loadSprite("sprites/Bosses/7 - Ganon - Blue" + (i + 1) + ".gif");
            redFrames[i] = loadSprite("sprites/Bosses/7 - Ganon - Red" + (i + 1) + ".gif");
        }
        sprite = blueFrames[0];
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        frameCounter++;

        switch (phase) {
            case BLUE:
                updateBluePhase(player, projectiles);
                break;
            case BROWN:
                updateBrownPhase(player, projectiles);
                break;
            case DYING:
                updateDying();
                break;
            case DEAD:
                active = false;
                break;
        }
    }

    /** BLUE state: invisible, teleports, shoots fireballs. */
    private void updateBluePhase(ZeldaPlayer player, List<Projectile> projectiles) {
        // NES: animate every frame so a hit shows random pose
        animFrame = (animFrame + 1) % 5;
        if (animFrame < blueFrames.length) sprite = blueFrames[animFrame];

        // Visibility timer (revealed briefly by sword hits)
        if (visible) {
            visibleTimer--;
            if (visibleTimer <= 0) visible = false;
        }

        // NES: move like Blue Wizzrobe teleporting (move + check tile)
        moveTimer--;
        if (moveTimer <= 0) {
            direction = (int)(Math.random() * 4);
            moveTimer = 8 + (int)(Math.random() * 16);
        }
        double nx = x, ny = y;
        switch (direction) {
            case 0: ny -= 1.0; break;
            case 1: nx -= 1.0; break;
            case 2: ny += 1.0; break;
            case 3: nx += 1.0; break;
        }
        // Keep in room bounds
        if (nx >= 16 && nx <= ZeldaRoom.ROOM_PIXEL_W - width - 16) x = nx;
        if (ny >= 16 && ny <= ZeldaRoom.ROOM_PIXEL_H - height - 16) y = ny;

        // NES: shoot fireballs every $40 (64) frames
        shootTimer--;
        if (shootTimer <= 0) {
            shootFireballs(player, projectiles);
            shootTimer = SHOOT_INTERVAL;
        }
    }

    /** BROWN state: visible, counter counts down, returns to blue when 0. */
    private void updateBrownPhase(ZeldaPlayer player, List<Projectile> projectiles) {
        visible = true;

        // NES: animate through red/brown frames
        animTimer++;
        if (animTimer >= ANIM_SPEED) { animTimer = 0; animFrame = (animFrame + 1) % 5; }
        if (animFrame < redFrames.length) sprite = redFrames[animFrame];

        // NES: decrement state every other frame
        if (frameCounter % 2 == 0) {
            brownStateCounter--;
            if (brownStateCounter <= 0) {
                // Return to blue/invisible
                phase = Phase.BLUE;
                visible = false;
                randomizeLocation();
                return;
            }
        }

        // Still shoots but slower
        shootTimer--;
        if (shootTimer <= 0) {
            shootFireballs(player, projectiles);
            shootTimer = SHOOT_INTERVAL * 2;
        }
    }

    /** DYING state: burst rays animation then deactivate. */
    private void updateDying() {
        dyingCounter++;
        if (dyingCounter >= 0xA0) {
            // Death complete — Ganon becomes ashes, Triforce appears
            active = false;
            phase = Phase.DEAD;
        }
    }

    /** NES: randomize Ganon position. Y=$A0, X=random($30 or $B0). */
    private void randomizeLocation() {
        int idx = (int)(Math.random() * TELEPORT_X.length);
        x = TELEPORT_X[idx];
        y = TELEPORT_Y;
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
                fb.setDamage(EnemyStats.FIREBALL_DAMAGE);
                projectiles.add(fb);
            }
        }
    }

    @Override
    public void damage(int amount) {
        if (invulnerableFrames > 0) return;

        if (phase == Phase.BLUE) {
            // Sword hit reveals Ganon briefly and counts toward brown threshold
            visible = true;
            visibleTimer = VISIBLE_DURATION;
            damageTimer = DAMAGE_FLASH_FRAMES;
            invulnerableFrames = DEFAULT_INVULN;
            hitCount += amount;

            if (hitCount >= HITS_TO_BROWN) {
                // Transition to brown (visible) state
                phase = Phase.BROWN;
                brownStateCounter = BROWN_INITIAL;
                visible = true;
            }
        }
        // In BROWN phase, normal weapons do NOT kill — only silver arrow
    }

    /**
     * Called specifically when a silver arrow hits Ganon.
     * Silver arrow in brown state = KILL. In blue state = counts as normal hit.
     */
    public void damageBysilverArrow(int amount) {
        if (invulnerableFrames > 0) return;

        if (phase == Phase.BLUE) {
            damage(amount);
        }

        if (phase == Phase.BROWN) {
            // Silver arrow in brown phase: begin death sequence
            health = 0;
            phase = Phase.DYING;
            dyingCounter = 1;
            damageTimer = DAMAGE_FLASH_FRAMES;
        }
    }

    @Override
    public boolean canDamage() {
        return active && phase != Phase.DYING && phase != Phase.DEAD;
    }

    public boolean isDying() { return phase == Phase.DYING; }

    @Override
    public void render(Graphics2D g2) {
        if (!active && phase != Phase.DYING) return;

        if (phase == Phase.DYING) {
            renderDying(g2);
            return;
        }

        if (!visible) {
            // NES: Ganon is invisible — draw nothing (no shimmer in NES)
            return;
        }

        boolean flash = damageTimer > 0 && (damageTimer / 3) % 2 == 0;
        boolean brown = (phase == Phase.BROWN);

        // NES: brown state < $30 = translucent (flickering)
        if (brown && brownStateCounter < 0x30 && frameCounter % 2 == 0) {
            return; // Skip drawing every other frame for flicker effect
        }

        if (sprite != null && !flash) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
            if (brown) {
                // Brown tint overlay
                g2.setColor(new Color(139, 69, 19, 100));
                g2.fillRect((int)x, (int)y, width, height);
            }
        } else {
            Color bodyColor = brown ? new Color(139, 69, 19) : new Color(60, 0, 120);
            g2.setColor(flash ? Color.WHITE : bodyColor);
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.RED);
            g2.fillOval((int)x + 5, (int)y + 8, 4, 4);
            g2.fillOval((int)x + 15, (int)y + 8, 4, 4);
            g2.setColor(new Color(200, 200, 0));
            g2.fillRect((int)x + width / 2 - 1, (int)y + height - 4, 2, 8);
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 8, width, 4);
        g2.setColor(brown ? Color.YELLOW : Color.RED);
        double hpFraction = (double) Math.max(0, HITS_TO_BROWN - hitCount) / HITS_TO_BROWN;
        if (brown) hpFraction = (double) brownStateCounter / BROWN_INITIAL;
        int hw = (int)(hpFraction * width);
        g2.fillRect((int)x, (int)y - 8, hw, 4);
    }

    /** Render dying sequence: burst rays + ashes pile. */
    private void renderDying(Graphics2D g2) {
        int ix = (int)x, iy = (int)y;

        if (dyingCounter < 0x50) {
            // Draw Ganon body fading
            Composite old = g2.getComposite();
            float alpha = 1.0f - (float)dyingCounter / 0x50f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));
            if (sprite != null) {
                g2.drawImage(sprite, ix, iy, width, height, null);
            } else {
                g2.setColor(new Color(139, 69, 19));
                g2.fillRect(ix, iy, width, height);
            }
            g2.setComposite(old);
        } else {
            // Ashes pile
            g2.setColor(new Color(80, 80, 80));
            g2.fillRect(ix + 4, iy + height - 8, width - 8, 8);
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(ix + 6, iy + height - 6, width - 12, 4);
        }

        // Burst rays expanding outward
        if (dyingCounter >= 0x50 && dyingCounter < 0xA0) {
            int rayDist = (dyingCounter - 0x50) * 2;
            g2.setColor(new Color(255, 255, 100));
            int cx = ix + width / 2, cy = iy + height / 2;
            for (int dir = 0; dir < 8; dir++) {
                double angle = dir * Math.PI / 4;
                int rx = cx + (int)(Math.cos(angle) * rayDist);
                int ry = cy + (int)(Math.sin(angle) * rayDist);
                g2.fillRect(rx - 2, ry - 2, 4, 4);
            }
        }
    }
}
