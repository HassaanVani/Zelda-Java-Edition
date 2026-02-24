package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Wallmaster: giant hand that emerges from walls and grabs Link,
 * sending him back to the dungeon entrance. 2 HP.
 * NES-accurate: On grab, TELEPORT Link back to dungeon entrance (no heart damage).
 */
public class Wallmaster extends ZeldaEnemy {
    private enum Phase { HIDDEN, EMERGING, ACTIVE, RETREATING }
    private Phase phase = Phase.HIDDEN;
    private int phaseTimer = 0;
    private double targetX, targetY;
    private static final int EMERGE_TIME = 30;
    private static final int ACTIVE_TIME = 120;
    private static final int HIDDEN_TIME = 90;

    // Flag to signal that Link was grabbed (checked by ZeldaDungeon/ZeldaGame)
    private boolean grabbedPlayer = false;

    public Wallmaster(double x, double y) {
        super(x, y, 2, 0, AIType.CHASE); // 0 contact damage — grab teleports instead
        applyStats(EnemyStats.wallmaster());
        this.damage = 0; // Override: Wallmaster does NOT deal heart damage
        sprite = loadSprite("sprites/Enemies/Wall Master.gif");
    }

    public boolean hasGrabbedPlayer() { return grabbedPlayer; }
    public void resetGrab() { grabbedPlayer = false; }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        phaseTimer++;

        switch (phase) {
            case HIDDEN:
                if (phaseTimer >= HIDDEN_TIME) {
                    // Emerge from nearest wall towards player
                    targetX = player.getWorldX();
                    targetY = player.getWorldY();
                    phase = Phase.EMERGING;
                    phaseTimer = 0;
                    // Start at edge of room
                    if (Math.random() < 0.5) {
                        x = (targetX < ZeldaRoom.ROOM_PIXEL_W / 2) ? 4 : ZeldaRoom.ROOM_PIXEL_W - width - 4;
                        y = targetY;
                    } else {
                        x = targetX;
                        y = (targetY < ZeldaRoom.ROOM_PIXEL_H / 2) ? 4 : ZeldaRoom.ROOM_PIXEL_H - height - 4;
                    }
                }
                break;

            case EMERGING:
                if (phaseTimer >= EMERGE_TIME) {
                    phase = Phase.ACTIVE;
                    phaseTimer = 0;
                }
                break;

            case ACTIVE:
                // Chase player
                double dx = player.getWorldX() - x;
                double dy = player.getWorldY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 0) {
                    x += (dx / dist) * speed;
                    y += (dy / dist) * speed;
                }

                // Check for grab — touching Link = teleport to entrance
                if (getHitbox().intersects(player.getHitbox())) {
                    grabbedPlayer = true;
                    phase = Phase.HIDDEN;
                    phaseTimer = 0;
                }

                if (phaseTimer >= ACTIVE_TIME) {
                    phase = Phase.RETREATING;
                    phaseTimer = 0;
                }
                break;

            case RETREATING:
                if (phaseTimer >= EMERGE_TIME) {
                    phase = Phase.HIDDEN;
                    phaseTimer = 0;
                }
                break;
        }

        x = Math.max(4, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 4));
        y = Math.max(4, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 4));
    }

    @Override
    public boolean canDamage() { return false; } // Wallmaster doesn't deal normal contact damage

    @Override
    public void damage(int amount) {
        if (phase != Phase.ACTIVE && phase != Phase.EMERGING) return;
        super.damage(amount);
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active || phase == Phase.HIDDEN) return;
        float alpha = (phase == Phase.EMERGING || phase == Phase.RETREATING) ? 0.5f : 1.0f;
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(new Color(80, 40, 120));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(new Color(120, 80, 160));
            g2.fillRect((int)x + 2, (int)y + 2, 5, 8);
            g2.fillRect((int)x + 9, (int)y + 2, 5, 8);
        }

        g2.setComposite(old);
    }
}
