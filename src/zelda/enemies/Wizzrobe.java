package zelda.enemies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Wizzrobe: magic-casting enemy.
 * NES-accurate (Z_04.asm UpdateBlueWizzrobe / UpdateRedWizzrobe):
 * - Blue Wizzrobe: TELEPORTER. Appears at random position, fires magic beam, disappears.
 *   10 HP, 4 half-hearts contact, immune to boomerang. Unblockable beam (4 half-hearts).
 * - Red Wizzrobe: WALKER. Moves normally, periodically fires magic beam.
 *   4 HP, 2 half-hearts contact. Beam does 2 half-hearts, unblockable by small shield.
 */
public class Wizzrobe extends ZeldaEnemy {
    private boolean isBlue;
    private int phaseTimer = 0;
    private int shootCooldown = 0;
    private boolean visible = true;
    private BufferedImage frontSprite;
    private BufferedImage backSprite;

    // Blue Wizzrobe teleport timing (appear-shoot-disappear cycle)
    private static final int BLUE_INVISIBLE_FRAMES = 60;
    private static final int BLUE_VISIBLE_FRAMES = 30;
    private static final int BLUE_SHOOT_FRAME = 15; // shoots midway through visible phase

    // Red Wizzrobe walk + shoot timing
    private static final int RED_SHOOT_INTERVAL = 90;

    public Wizzrobe(double x, double y, boolean blue) {
        super(x, y, 3, 2, AIType.SHOOTER);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.wizzrobeBlue() : EnemyStats.wizzrobeRed());
        loadWizzrobeSprites();
    }

    private void loadWizzrobeSprites() {
        String color = isBlue ? "Blue" : "Red";
        frontSprite = loadSprite("sprites/Enemies/Wizzrobe - " + color + " (Front).gif");
        backSprite = loadSprite("sprites/Enemies/Wizzrobe - " + color + " (Back).gif");
        sprite = frontSprite;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (isBlue) {
            updateBlue(player, projectiles);
        } else {
            updateRed(player, room, projectiles);
        }
    }

    /**
     * Blue Wizzrobe (NES: teleporter): appears at random position, fires beam, disappears.
     */
    private void updateBlue(ZeldaPlayer player, List<Projectile> projectiles) {
        phaseTimer++;

        if (phaseTimer < BLUE_INVISIBLE_FRAMES) {
            // Invisible phase — cannot be hit or deal contact damage
            visible = false;
        } else if (phaseTimer < BLUE_INVISIBLE_FRAMES + BLUE_VISIBLE_FRAMES) {
            // Visible phase — appear and shoot
            visible = true;
            if (phaseTimer == BLUE_INVISIBLE_FRAMES + BLUE_SHOOT_FRAME) {
                shootMagicBeam(player, projectiles);
            }
        } else {
            // Teleport to new random walkable position and restart cycle
            x = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_W - 64);
            y = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_H - 64);
            direction = (int)(Math.random() * 4);
            phaseTimer = 0;
        }
    }

    /**
     * Red Wizzrobe (NES: walker): moves normally, shoots periodically.
     */
    private void updateRed(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        visible = true;

        // Walk like a normal wanderer
        moveTimer--;
        if (moveTimer <= 0) {
            direction = (int)(Math.random() * 4);
            moveTimer = 40 + (int)(Math.random() * 40);
        }

        double nx = x, ny = y;
        switch (direction) {
            case 0: ny -= speed; break;
            case 1: nx -= speed; break;
            case 2: ny += speed; break;
            case 3: nx += speed; break;
        }

        if (room != null && room.isWalkable((int)(nx + width / 2), (int)(ny + height / 2))) {
            x = nx;
            y = ny;
        } else {
            direction = (int)(Math.random() * 4);
            moveTimer = 10;
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));

        // Shoot magic beam periodically
        shootCooldown--;
        if (shootCooldown <= 0) {
            shootMagicBeam(player, projectiles);
            shootCooldown = RED_SHOOT_INTERVAL;
        }
    }

    /**
     * Fires an unblockable magic beam toward Link.
     * Blue beam: 4 half-hearts (2 hearts). Red beam: 2 half-hearts (1 heart).
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
            magic.setDamage(EnemyStats.MAGIC_DAMAGE);
            magic.setUnblockable(true);
            projectiles.add(magic);
        }
    }

    @Override
    public boolean canDamage() { return active && visible; }

    @Override
    public void damage(int amount) {
        if (!visible) return; // Can't damage while invisible (Blue teleporter)
        super.damage(amount);
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active || !visible) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else {
            BufferedImage currentSprite = null;
            switch (direction) {
                case 0: // up (back)
                    currentSprite = backSprite;
                    break;
                case 2: // down (front)
                    currentSprite = frontSprite;
                    break;
                default: // left/right: use front sprite
                    currentSprite = frontSprite;
                    break;
            }

            if (currentSprite != null) {
                g2.drawImage(currentSprite, (int)x, (int)y, width, height, null);
            } else {
                g2.setColor(isBlue ? new Color(80, 80, 220) : new Color(220, 60, 60));
                g2.fillRect((int)x, (int)y, width, height);
                g2.setColor(Color.WHITE);
                g2.fillRect((int)x + 5, (int)y + 2, 6, 4);
            }
        }
    }
}
