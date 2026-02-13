package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Like Like: blob enemy that eats Link's Magical Shield on contact.
 * Chases player slowly. 4 HP.
 */
public class LikeLike extends ZeldaEnemy {

    public LikeLike(double x, double y) {
        super(x, y, 4, 1, AIType.CHASE);
        this.speed = 0.4;
        sprite = loadSprite("sprites/Enemies/LikeLike.png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < 100 && dist > 0) {
            double nx = x + (dx / dist) * speed;
            double ny = y + (dy / dist) * speed;
            if (room != null && room.isWalkable((int)(nx + width / 2), (int)(ny + height / 2))) {
                x = nx;
                y = ny;
            }
        } else {
            if (room != null) randomMove(room);
        }

        // Eat shield on contact
        if (getHitbox().intersects(player.getHitbox()) && player.getInventory().hasMagicalShield()) {
            player.getInventory().loseShield();
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
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
            g2.setColor(new Color(200, 140, 80));
            g2.fillOval((int)x, (int)y, width, height);
            g2.setColor(new Color(160, 100, 60));
            g2.fillOval((int)x + 3, (int)y + 4, 10, 8);
        }
    }
}
