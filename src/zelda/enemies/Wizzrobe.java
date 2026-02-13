package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Wizzrobe: magic-casting enemy that teleports and shoots magic.
 * Blue: teleports, appears, shoots, disappears. 3 HP.
 * Red: walks through walls, shoots magic beams. 3 HP.
 */
public class Wizzrobe extends ZeldaEnemy {
    private boolean isBlue;
    private int phaseTimer = 0;
    private int shootCooldown = 0;
    private boolean visible = true;
    private static final int PHASE_DURATION = 60;
    private static final int SHOOT_INTERVAL = 80;

    public Wizzrobe(double x, double y, boolean blue) {
        super(x, y, 3, 2, AIType.SHOOTER);
        this.isBlue = blue;
        this.speed = blue ? 0 : 0.75;
        sprite = loadSprite(blue ? "sprites/Enemies/Wizzrobe (Blue).png" : "sprites/Enemies/Wizzrobe (Red).png");
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

    private void updateBlue(ZeldaPlayer player, List<Projectile> projectiles) {
        phaseTimer++;
        if (phaseTimer < PHASE_DURATION / 2) {
            visible = false;
        } else if (phaseTimer < PHASE_DURATION) {
            visible = true;
            if (phaseTimer == PHASE_DURATION / 2 + 5) {
                shootAtPlayer(player, projectiles);
            }
        } else {
            // Teleport to new position
            x = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_W - 64);
            y = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_H - 64);
            phaseTimer = 0;
        }
    }

    private void updateRed(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        // Red wizzrobe moves in straight lines, ignoring walls
        moveTimer--;
        if (moveTimer <= 0) {
            direction = (int)(Math.random() * 4);
            moveTimer = 40 + (int)(Math.random() * 40);
        }

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
            shootAtPlayer(player, projectiles);
            shootCooldown = SHOOT_INTERVAL;
        }
    }

    private void shootAtPlayer(ZeldaPlayer player, List<Projectile> projectiles) {
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            double spd = 2.0;
            Projectile magic = new Projectile(x + width / 2, y + height / 2,
                (dx / dist) * spd, (dy / dist) * spd, false);
            magic.setColor(isBlue ? Color.CYAN : new Color(255, 100, 50));
            magic.setSize(6, 6);
            magic.setDamage(2);
            projectiles.add(magic);
        }
    }

    @Override
    public boolean canDamage() { return active && visible; }

    @Override
    public void damage(int amount) {
        if (!visible && isBlue) return;
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
