package zelda.bosses;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Digdogger: large sea urchin boss invulnerable until the Recorder is played.
 * After shrinking, it becomes vulnerable and moves fast. Dungeon 5 boss.
 * Big form: 0 damage taken, bounces around. Small form: 4 HP, fast.
 */
public class Digdogger extends ZeldaEnemy {
    private boolean shrunk = false;
    private double vx, vy;
    private int bigWidth = 24, bigHeight = 24;
    private int smallWidth = 12, smallHeight = 12;

    public Digdogger(double x, double y) {
        super(x, y, 4, 1, AIType.RANDOM);
        this.width = bigWidth;
        this.height = bigHeight;
        this.speed = 0.4;
        this.damage = 2;
        vx = speed;
        vy = speed;
        sprite = loadSprite("sprites/Bosses/5 - Digdogger.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        double currentSpeed = shrunk ? 1.5 : speed;
        double mag = Math.sqrt(vx * vx + vy * vy);
        if (mag > 0) {
            vx = (vx / mag) * currentSpeed;
            vy = (vy / mag) * currentSpeed;
        }

        x += vx;
        y += vy;

        if (x < 16 || x > ZeldaRoom.ROOM_PIXEL_W - width - 16) {
            vx = -vx;
            x = Math.max(16, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 16));
        }
        if (y < 16 || y > ZeldaRoom.ROOM_PIXEL_H - height - 16) {
            vy = -vy;
            y = Math.max(16, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 16));
        }

        // Slightly home towards player when shrunk
        if (shrunk) {
            double dx = player.getWorldX() - x;
            double dy = player.getWorldY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                vx += (dx / dist) * 0.05;
                vy += (dy / dist) * 0.05;
            }
        }
    }

    @Override
    public void damage(int amount) {
        if (!shrunk) return; // Invulnerable in big form
        super.damage(amount);
    }

    /**
     * Called when the Recorder is used — shrinks Digdogger.
     */
    public void shrink() {
        if (!shrunk) {
            shrunk = true;
            width = smallWidth;
            height = smallHeight;
            speed = 1.5;
            damageTimer = DAMAGE_FLASH_FRAMES;
        }
    }

    public boolean isShrunk() { return shrunk; }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        boolean flash = damageTimer > 0 && (damageTimer / 3) % 2 == 0;

        if (flash) {
            g2.setColor(Color.WHITE);
            g2.fillOval((int)x, (int)y, width, height);
        } else if (sprite != null && !shrunk) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            Color bodyColor = shrunk ? new Color(200, 100, 40) : new Color(160, 80, 30);
            g2.setColor(bodyColor);
            g2.fillOval((int)x, (int)y, width, height);

            if (!shrunk) {
                // Spikes
                g2.setColor(new Color(180, 100, 40));
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 / 8) * i;
                    int sx = (int)(x + width / 2 + Math.cos(angle) * (width / 2 + 2));
                    int sy = (int)(y + height / 2 + Math.sin(angle) * (height / 2 + 2));
                    g2.fillOval(sx - 2, sy - 2, 4, 4);
                }
            }

            // Eye
            g2.setColor(Color.RED);
            g2.fillOval((int)x + width / 2 - 2, (int)y + height / 2 - 2, 4, 4);
        }

        // Health bar (only when shrunk)
        if (shrunk) {
            g2.setColor(Color.BLACK);
            g2.fillRect((int)x, (int)y - 6, width, 4);
            g2.setColor(Color.RED);
            int hw = (int)((double)health / maxHealth * width);
            g2.fillRect((int)x, (int)y - 6, hw, 4);
        }
    }
}
