package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * Wizzrobe: magic-casting enemy.
 * Blue Wizzrobe: Phases through walls. Appears, shoots unblockable magic beam, disappears. Immune to boomerang. 3 HP.
 * Red Wizzrobe: Teleports randomly. Appears briefly to fire unblockable magic beam, vanishes instantly. 3 HP.
 * Magic beams CANNOT be blocked by ANY shield (including Magical Shield).
 */
public class Wizzrobe extends ZeldaEnemy {
    private boolean isBlue;
    private int phaseTimer = 0;
    private int shootCooldown = 0;
    private boolean visible = true;

    // Red Wizzrobe teleport timing
    private static final int RED_VISIBLE_FRAMES = 30;
    private static final int RED_INVISIBLE_FRAMES = 60;
    private static final int RED_SHOOT_FRAME = 15; // shoots midway through visible phase

    // Blue Wizzrobe phasing timing
    private static final int BLUE_MOVE_FRAMES = 80;
    private static final int BLUE_SHOOT_INTERVAL = 90;

    public Wizzrobe(double x, double y, boolean blue) {
        super(x, y, 3, 2, AIType.SHOOTER);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.wizzrobeBlue() : EnemyStats.wizzrobeRed());
        sprite = loadSprite(blue ? "sprites/Enemies/Wizzrobe (Blue).png" : "sprites/Enemies/Wizzrobe (Red).png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (isBlue) {
            updateBlue(player, room, projectiles);
        } else {
            updateRed(player, projectiles);
        }
    }

    /**
     * Blue Wizzrobe: phases through walls, always visible while moving, shoots periodically.
     */
    private void updateBlue(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        visible = true;
        moveTimer--;
        if (moveTimer <= 0) {
            direction = (int)(Math.random() * 4);
            moveTimer = 40 + (int)(Math.random() * 40);
        }

        // Move through walls (ignore collision)
        switch (direction) {
            case 0: y -= speed; break;
            case 1: x -= speed; break;
            case 2: y += speed; break;
            case 3: x += speed; break;
        }

        // Wrap around room edges
        if (x < 8) x = ZeldaRoom.ROOM_PIXEL_W - width - 8;
        if (x > ZeldaRoom.ROOM_PIXEL_W - width - 8) x = 8;
        if (y < 8) y = ZeldaRoom.ROOM_PIXEL_H - height - 8;
        if (y > ZeldaRoom.ROOM_PIXEL_H - height - 8) y = 8;

        shootCooldown--;
        if (shootCooldown <= 0) {
            shootMagicBeam(player, projectiles);
            shootCooldown = BLUE_SHOOT_INTERVAL;
        }
    }

    /**
     * Red Wizzrobe: teleports randomly, appears briefly to fire, vanishes.
     */
    private void updateRed(ZeldaPlayer player, List<Projectile> projectiles) {
        phaseTimer++;

        if (phaseTimer < RED_INVISIBLE_FRAMES) {
            // Invisible phase
            visible = false;
        } else if (phaseTimer < RED_INVISIBLE_FRAMES + RED_VISIBLE_FRAMES) {
            // Visible phase — appear and shoot
            visible = true;
            if (phaseTimer == RED_INVISIBLE_FRAMES + RED_SHOOT_FRAME) {
                shootMagicBeam(player, projectiles);
            }
        } else {
            // Teleport to new random position and restart cycle
            x = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_W - 64);
            y = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_H - 64);
            phaseTimer = 0;
        }
    }

    /**
     * Fires an unblockable magic beam toward Link.
     * Red beams do 1 heart (2 half-hearts), Blue beams do 2 hearts (4 half-hearts).
     */
    private void shootMagicBeam(ZeldaPlayer player, List<Projectile> projectiles) {
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            double spd = 2.5;
            Projectile magic = new Projectile(x + width / 2, y + height / 2,
                (dx / dist) * spd, (dy / dist) * spd, false);
            magic.setColor(isBlue ? Color.CYAN : new Color(255, 100, 50));
            magic.setSize(6, 6);
            // Blue beam: 4 half-hearts (2 full hearts), Red beam: 2 half-hearts (1 full heart)
            magic.setDamage(isBlue ? 4 : 2);
            // Mark as unblockable (magic beams can't be shielded)
            magic.setUnblockable(true);
            projectiles.add(magic);
        }
    }

    @Override
    public boolean canDamage() { return active && visible; }

    @Override
    public void damage(int amount) {
        if (!visible) return; // Can't damage while invisible
        super.damage(amount);
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active || !visible) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(isBlue ? new Color(80, 80, 220) : new Color(220, 60, 60));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x + 5, (int)y + 2, 6, 4);
        }
    }
}
