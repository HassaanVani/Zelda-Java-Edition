package zelda.enemies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Vire: bat-demon that splits into two Keese when killed. Bounces around room.
 * 4 HP, 1 damage.
 */
public class Vire extends ZeldaEnemy {
    private double vx, vy;
    private List<ZeldaEnemy> roomEnemies;
    private BufferedImage frontSprite;
    private BufferedImage backSprite;

    public void setRoomEnemies(List<ZeldaEnemy> enemies) {
        this.roomEnemies = enemies;
    }

    public Vire(double x, double y) {
        super(x, y, 2, 1, AIType.RANDOM);
        applyStats(EnemyStats.vire());
        vx = (Math.random() < 0.5 ? -1 : 1) * speed;
        vy = (Math.random() < 0.5 ? -1 : 1) * speed;
        loadVireSprites();
    }

    private void loadVireSprites() {
        frontSprite = loadSprite("sprites/Enemies/Vire (Front).gif");
        backSprite = loadSprite("sprites/Enemies/Vire (Back).gif");
        sprite = frontSprite;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        x += vx;
        y += vy;

        if (x < 8 || x > ZeldaRoom.ROOM_PIXEL_W - width - 8) {
            vx = -vx;
            x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        }
        if (y < 8 || y > ZeldaRoom.ROOM_PIXEL_H - height - 8) {
            vy = -vy;
            y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
        }

        // Update sprite based on vertical movement direction
        if (vy < 0) {
            sprite = backSprite; // moving up
        } else {
            sprite = frontSprite; // moving down
        }

        // Slightly adjust towards player
        if (player == null) return;
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            vx += (dx / dist) * 0.02;
            vy += (dy / dist) * 0.02;
            double mag = Math.sqrt(vx * vx + vy * vy);
            if (mag > speed * 1.5) {
                vx = (vx / mag) * speed * 1.5;
                vy = (vy / mag) * speed * 1.5;
            }
        }
    }

    @Override
    public void damage(int amount) {
        health -= amount;
        damageTimer = 10;
        invulnerableFrames = 15;
        if (health <= 0) {
            active = false;
            spawnKeese();
        }
    }

    private void spawnKeese() {
        if (roomEnemies != null) {
            roomEnemies.add(new Keese(x - 8, y, false));
            roomEnemies.add(new Keese(x + 8, y, false));
        }
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
            g2.setColor(new Color(140, 60, 180));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(new Color(200, 100, 220));
            g2.fillRect((int)x + 2, (int)y, 5, 6);
            g2.fillRect((int)x + 10, (int)y, 5, 6);
        }
    }
}
