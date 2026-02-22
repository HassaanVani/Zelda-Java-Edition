package zelda.bosses;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Gohma: giant spider/crab boss.
 * NES-accurate:
 * - Moves left-right, eye cycles: closed -> half-open -> open -> half-open -> closed
 * - ONLY damaged by arrows hitting the OPEN eye
 * - Red Gohma: 1 arrow kill. Blue Gohma: 3 arrows.
 * - Shoots fireballs periodically (every ~65 frames)
 * - 1/2 heart contact
 */
public class Gohma extends ZeldaEnemy {
    private boolean isBlue;

    // Eye state: 0=closed, 1=half-open, 2=open (vulnerable), 3=half-open (closing)
    private int eyeState = 0;
    private int eyeTimer = 0;
    private static final int EYE_CLOSED_DURATION = 60;
    private static final int EYE_HALF_OPEN_DURATION = 15;
    private static final int EYE_OPEN_DURATION = 30;

    private int shootTimer = 65; // $41 frames
    private double moveDir = 1;
    private int arrowHits = 0;

    public Gohma(double x, double y) {
        this(x, y, false);
    }

    public Gohma(double x, double y, boolean blue) {
        super(x, y, blue ? 3 : 1, 1, AIType.SHOOTER);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.gohmaBlue() : EnemyStats.gohmaRed());
        this.width = 24;
        this.height = 16;
        this.speed = 0.6;
        this.damage = 1; // 1/2 heart
        sprite = loadSprite("sprites/Bosses/6 - Gohma.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // Move side to side
        x += moveDir * speed;
        if (x < 32 || x > ZeldaRoom.ROOM_PIXEL_W - width - 32) {
            moveDir = -moveDir;
        }

        // Eye cycle: closed -> half-open -> open -> half-open (closing) -> closed
        eyeTimer++;
        switch (eyeState) {
            case 0: // Closed
                if (eyeTimer >= EYE_CLOSED_DURATION) { eyeState = 1; eyeTimer = 0; }
                break;
            case 1: // Half-open (opening)
                if (eyeTimer >= EYE_HALF_OPEN_DURATION) { eyeState = 2; eyeTimer = 0; }
                break;
            case 2: // Open (vulnerable)
                if (eyeTimer >= EYE_OPEN_DURATION) { eyeState = 3; eyeTimer = 0; }
                break;
            case 3: // Half-open (closing)
                if (eyeTimer >= EYE_HALF_OPEN_DURATION) { eyeState = 0; eyeTimer = 0; }
                break;
        }

        // Shoot fireballs periodically
        shootTimer--;
        if (shootTimer <= 0) {
            double dx = player.getWorldX() - (x + width / 2);
            double dy = player.getWorldY() - (y + height / 2);
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                Projectile fb = new Projectile(x + width / 2, y + height / 2,
                    (dx / dist) * 2.0, (dy / dist) * 2.0, false);
                fb.setColor(new Color(200, 50, 50));
                fb.setSize(6, 6);
                fb.setDamage(1);
                projectiles.add(fb);
            }
            shootTimer = 65; // $41 frames
        }
    }

    @Override
    public void damage(int amount) {
        // Gohma is only damaged by arrows when eye is open.
        // All other damage sources are rejected.
    }

    /**
     * Called specifically when an arrow hits Gohma.
     * Only works when eye is FULLY open (state 2).
     * Red: 1 arrow kill. Blue: 3 arrows.
     */
    public void damageByArrow(int amount) {
        if (eyeState != 2) return; // Only open eye
        if (invulnerableFrames > 0) return;

        arrowHits++;
        int required = isBlue ? 3 : 1;

        damageTimer = DAMAGE_FLASH_FRAMES;
        invulnerableFrames = DEFAULT_INVULN;

        if (arrowHits >= required) {
            health = 0;
            active = false;
        }
    }

    public boolean isEyeOpen() { return eyeState == 2; }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        boolean flash = damageTimer > 0 && (damageTimer / 3) % 2 == 0;

        if (sprite != null && !flash) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            // Body
            Color bodyColor = isBlue ? new Color(60, 60, 120) : new Color(120, 60, 30);
            g2.setColor(flash ? Color.WHITE : bodyColor);
            g2.fillOval((int)x, (int)y, width, height);

            // Legs
            g2.setColor(flash ? Color.WHITE : bodyColor.darker());
            for (int i = 0; i < 4; i++) {
                int lx = (int)x + 2 + i * 6;
                g2.fillRect(lx, (int)y + height - 2, 3, 6);
            }

            // Eye
            int ecx = (int)x + width / 2;
            int ecy = (int)y + height / 2;
            switch (eyeState) {
                case 0: // Closed
                    g2.setColor(new Color(80, 40, 20));
                    g2.drawLine(ecx - 4, ecy, ecx + 4, ecy);
                    break;
                case 1: case 3: // Half-open
                    g2.setColor(Color.YELLOW);
                    g2.fillOval(ecx - 3, ecy - 1, 6, 3);
                    break;
                case 2: // Open (vulnerable!)
                    g2.setColor(Color.RED);
                    g2.fillOval(ecx - 4, ecy - 4, 8, 8);
                    g2.setColor(Color.BLACK);
                    g2.fillOval(ecx - 1, ecy - 1, 3, 3);
                    break;
            }
        }

        // Health bar
        int required = isBlue ? 3 : 1;
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 6, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((1.0 - (double)arrowHits / required) * width);
        g2.fillRect((int)x, (int)y - 6, Math.max(0, hw), 4);
    }
}
