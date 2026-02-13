package zelda.bosses;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Dodongo: dinosaur boss that can only be damaged by bombs fed into its mouth.
 * Walks in straight lines, turns on wall collision. Immune to sword.
 * 2 bomb hits to kill. Dungeon 2 boss.
 */
public class Dodongo extends ZeldaEnemy {
    private int bombsEaten = 0;
    private static final int BOMBS_TO_KILL = 2;
    private int stunTimer = 0;

    public Dodongo(double x, double y) {
        super(x, y, 4, 1, AIType.RANDOM);
        this.width = 24;
        this.height = 16;
        this.speed = 0.6;
        this.damage = 2;
        this.direction = (int)(Math.random() * 4);
        sprite = loadSprite("sprites/Bosses/2 - Dodongo.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        if (stunTimer > 0) {
            stunTimer--;
            return;
        }

        // Check if walking into a bomb projectile (player-owned, stationary)
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            if (p.isPlayerOwned() && p.isActive() && p.getDamage() >= 4) {
                if (getHitbox().intersects(p.getHitbox())) {
                    bombsEaten++;
                    p.deactivate();
                    stunTimer = 60;
                    damageTimer = DAMAGE_FLASH_FRAMES;
                    if (bombsEaten >= BOMBS_TO_KILL) {
                        health = 0;
                        active = false;
                    }
                    return;
                }
            }
        }

        // Walk in current direction
        double nx = x, ny = y;
        switch (direction) {
            case 0: ny -= speed; break;
            case 1: nx -= speed; break;
            case 2: ny += speed; break;
            case 3: nx += speed; break;
        }

        boolean blocked = false;
        if (nx < 8 || nx > ZeldaRoom.ROOM_PIXEL_W - width - 8) blocked = true;
        if (ny < 8 || ny > ZeldaRoom.ROOM_PIXEL_H - height - 8) blocked = true;

        if (!blocked) {
            x = nx;
            y = ny;
        } else {
            direction = (direction + 1 + (int)(Math.random() * 3)) % 4;
        }
    }

    @Override
    public void damage(int amount) {
        // Immune to sword — only bombs work (handled in update)
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(new Color(120, 80, 40));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(new Color(100, 60, 30));
            g2.fillRect((int)x + 2, (int)y + 2, 8, 6);
            g2.setColor(Color.RED);
            g2.fillRect((int)x + 4, (int)y + 3, 3, 3);
        }

        // Health bar
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 6, width, 4);
        g2.setColor(Color.RED);
        int hw = (int)((1.0 - (double)bombsEaten / BOMBS_TO_KILL) * width);
        g2.fillRect((int)x, (int)y - 6, hw, 4);
    }
}
