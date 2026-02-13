package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Gibdo: mummy enemy that walks randomly. Tough, 6 HP.
 * Slow but deals 2 damage on contact.
 */
public class Gibdo extends ZeldaEnemy {

    public Gibdo(double x, double y) {
        super(x, y, 6, 2, AIType.RANDOM);
        this.speed = 0.5;
        sprite = loadSprite("sprites/Enemies/Gibdo.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (room != null) randomMove(room);
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
            g2.setColor(new Color(200, 180, 140));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.BLACK);
            g2.fillRect((int)x + 4, (int)y + 4, 3, 3);
            g2.fillRect((int)x + 10, (int)y + 4, 3, 3);
        }
    }
}
