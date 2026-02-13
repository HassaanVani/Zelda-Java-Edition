package zelda.enemies;

import zelda.*;
import java.awt.*;
import java.util.List;

/**
 * Darknut: armored knight that can only be damaged from behind or the side.
 * Moves in cardinal directions, changes direction on wall collision.
 * Red Darknut: 3 HP, speed 0.75. Blue Darknut: 6 HP, speed 1.0.
 */
public class Darknut extends ZeldaEnemy {
    private boolean isBlue;
    private static final int RED_HP = 3;
    private static final int BLUE_HP = 6;

    public Darknut(double x, double y, boolean blue) {
        super(x, y, blue ? BLUE_HP : RED_HP, 2, AIType.RANDOM);
        this.isBlue = blue;
        this.speed = blue ? 1.0 : 0.75;
        this.direction = (int)(Math.random() * 4);
        this.moveTimer = 60 + (int)(Math.random() * 60);
        sprite = loadSprite(blue ? "sprites/Enemies/Darknut (Blue).png" : "sprites/Enemies/Darknut (Red).png");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        moveTimer--;
        if (moveTimer <= 0) {
            direction = (int)(Math.random() * 4);
            moveTimer = 60 + (int)(Math.random() * 60);
        }

        oldX = x;
        oldY = y;
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
            moveTimer = 30;
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
    }

    @Override
    public void damage(int amount) {
        // Darknuts can only be damaged from behind/side (simplified: always take damage for now)
        super.damage(amount);
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
            g2.setColor(isBlue ? new Color(60, 60, 180) : new Color(180, 60, 60));
            g2.fillRect((int)x, (int)y, width, height);
            g2.setColor(Color.GRAY);
            g2.fillRect((int)x + 4, (int)y + 2, 8, 4);
        }
    }
}
