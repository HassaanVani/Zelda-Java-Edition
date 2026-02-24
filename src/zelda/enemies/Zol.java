package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Zol: larger blob that moves randomly. 2 HP.
 * When killed, splits into 2 Gels at its position.
 */
public class Zol extends ZeldaEnemy {
    private List<ZeldaEnemy> roomEnemies;

    public Zol(double x, double y) {
        super(x, y, 2, 1, AIType.RANDOM);
        applyStats(EnemyStats.zol());
        sprite = loadSprite("sprites/Enemies/Zol - Green.gif");
    }

    public void setRoomEnemies(List<ZeldaEnemy> enemies) {
        this.roomEnemies = enemies;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (room != null) randomMove(room);
    }

    @Override
    public void damage(int amount) {
        health -= amount;
        damageTimer = 10;
        invulnerableFrames = 15;
        if (health <= 0) {
            active = false;
            spawnGels();
        }
    }

    private void spawnGels() {
        if (roomEnemies != null) {
            roomEnemies.add(new Gel(x - 4, y));
            roomEnemies.add(new Gel(x + 4, y));
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
            g2.setColor(new Color(40, 140, 40));
            g2.fillRect((int)x, (int)y, width, height);
        }
    }
}
