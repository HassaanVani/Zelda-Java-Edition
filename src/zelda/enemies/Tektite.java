package zelda.enemies;

import zelda.*;
import java.util.List;

public class Tektite extends ZeldaEnemy {
    private static final int JUMP_COOLDOWN_MIN = 40;
    private static final int JUMP_COOLDOWN_RANGE = 40;
    private static final double JUMP_VEL_Y = -4;
    private static final double GRAVITY = 0.3;
    private static final double MAX_JUMP_DIST = 70;

    private boolean isBlue;
    private int jumpTimer = 0;
    private int jumpCooldown = 60;
    private double velX = 0, velY = 0;
    private boolean jumping = false;
    private double targetY;

    public Tektite(double x, double y, boolean isBlue) {
        super(x, y, 1, 1, AIType.JUMPER);
        this.isBlue = isBlue;
        loadTektiteSprite();
    }

    private void loadTektiteSprite() {
        String color = isBlue ? "Blue" : "Red";
        sprite = loadSprite("sprites/Enemies/Tektike - " + color + ".gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (damageTimer > 0) damageTimer--;

        animationCounter++;
        if (animationCounter >= 8) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }

        if (jumping) {
            x += velX;
            y += velY;
            velY += GRAVITY;

            if (velY > 0 && y >= targetY) {
                y = targetY;
                jumping = false;
                jumpTimer = 0;
                velX = 0;
                velY = 0;
            }
        } else {
            jumpTimer++;
            if (jumpTimer >= jumpCooldown) {
                startJump(player);
            }
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
    }

    private void startJump(ZeldaPlayer player) {
        jumping = true;
        targetY = y;

        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            double jumpDist = Math.min(dist, MAX_JUMP_DIST);
            velX = (dx / dist) * jumpDist / 30;
            velY = JUMP_VEL_Y - Math.random() * 2;
        } else {
            velX = (Math.random() - 0.5) * 3;
            velY = JUMP_VEL_Y;
        }

        jumpCooldown = JUMP_COOLDOWN_MIN + (int)(Math.random() * JUMP_COOLDOWN_RANGE);
    }

    @Override
    public boolean canDamage() { return true; }
}
