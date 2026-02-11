package zelda.enemies;

import zelda.*;
import java.util.List;

public class Keese extends ZeldaEnemy {
    private static final int MOVE_INTERVAL_MIN = 30;
    private static final int MOVE_INTERVAL_RANGE = 60;
    private static final double BLUE_SPEED = 1.2;
    private static final double RED_SPEED = 1.0;

    private double targetX, targetY;
    private int moveTimer = 0;
    private boolean isBlue;

    public Keese(double x, double y, boolean blue) {
        super(x, y, 1, AIType.RANDOM);
        this.isBlue = blue;
        this.speed = blue ? BLUE_SPEED : RED_SPEED;
        this.targetX = x;
        this.targetY = y;
        loadKeeseSprite();
    }

    private void loadKeeseSprite() {
        String color = isBlue ? "Blue" : "Red";
        sprite = loadSprite("sprites/Enemies/Keese - " + color + ".gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;

        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        animationCounter++;
        if (animationCounter >= 6) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }

        moveTimer--;
        if (moveTimer <= 0) {
            targetX = 16 + Math.random() * 224;
            targetY = 16 + Math.random() * 144;
            moveTimer = MOVE_INTERVAL_MIN + (int)(Math.random() * MOVE_INTERVAL_RANGE);
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > speed) {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
    }
}
