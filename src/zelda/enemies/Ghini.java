package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Ghini: ghost enemy found in graveyards.
 * NES-accurate ringleader mechanic:
 * - First Ghini on the screen is the "ringleader" (can be damaged, 10 HP).
 * - Additional Ghinis spawn when Link touches gravestones — these are INVINCIBLE.
 * - Killing the ringleader kills ALL Ghinis on screen.
 */
public class Ghini extends ZeldaEnemy {
    private double floatAngle = Math.random() * Math.PI * 2;
    private double floatRadius = 30 + Math.random() * 20;
    private double centerX, centerY;

    private boolean isRingleader;
    private List<ZeldaEnemy> roomEnemies;

    /** Create a ringleader Ghini (first one on screen). */
    public Ghini(double x, double y) {
        this(x, y, true);
    }

    /** Create a Ghini with explicit ringleader flag. */
    public Ghini(double x, double y, boolean ringleader) {
        super(x, y, 10, 1, AIType.RANDOM);
        this.isRingleader = ringleader;
        if (ringleader) {
            applyStats(EnemyStats.ghini());
        } else {
            applyStats(EnemyStats.ghiniSpawned());
        }
        this.centerX = x;
        this.centerY = y;
        sprite = loadSprite("sprites/Enemies/Ghini.png");
    }

    public boolean isRingleader() { return isRingleader; }
    public void setRoomEnemies(List<ZeldaEnemy> enemies) { this.roomEnemies = enemies; }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        floatAngle += 0.03;
        x = centerX + Math.cos(floatAngle) * floatRadius;
        y = centerY + Math.sin(floatAngle) * floatRadius;

        // Slowly drift center towards player
        double dx = player.getWorldX() - centerX;
        double dy = player.getWorldY() - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            centerX += (dx / dist) * 0.2;
            centerY += (dy / dist) * 0.2;
        }

        centerX = Math.max(16, Math.min(centerX, ZeldaRoom.ROOM_PIXEL_W - 16));
        centerY = Math.max(16, Math.min(centerY, ZeldaRoom.ROOM_PIXEL_H - 16));
    }

    @Override
    public void damage(int amount) {
        if (!isRingleader) return; // Spawned Ghinis are invincible
        super.damage(amount);
        if (health <= 0) {
            // Killing ringleader kills ALL Ghinis
            killAllGhinis();
        }
    }

    private void killAllGhinis() {
        if (roomEnemies != null) {
            for (ZeldaEnemy e : roomEnemies) {
                if (e instanceof Ghini && e != this) {
                    ((Ghini) e).forceKill();
                }
            }
        }
    }

    /** Force-kill a spawned Ghini (called when ringleader dies). */
    public void forceKill() {
        active = false;
        health = 0;
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            // Spawned Ghinis are slightly transparent
            if (!isRingleader) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.drawImage(sprite, (int)x, (int)y, width, height, null);
                g2.setComposite(old);
            } else {
                g2.drawImage(sprite, (int)x, (int)y, width, height, null);
            }
        } else {
            g2.setColor(new Color(200, 200, 220, isRingleader ? 220 : 140));
            g2.fillOval((int)x, (int)y, width, height);
            g2.setColor(Color.BLACK);
            g2.fillRect((int)x + 4, (int)y + 5, 3, 3);
            g2.fillRect((int)x + 10, (int)y + 5, 3, 3);
        }
    }
}
