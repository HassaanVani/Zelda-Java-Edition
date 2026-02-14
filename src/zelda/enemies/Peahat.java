package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

public class Peahat extends ZeldaEnemy {
    private static final double FLOAT_AMPLITUDE = 2.0;
    private static final double FLOAT_SPEED = 0.15;
    private static final int PAUSE_MIN = 60;
    private static final int PAUSE_RANGE = 60;
    private static final int MOVE_TIMEOUT = 180;

    private double angle = 0;
    private double floatOffset = 0;
    private int moveTimer = 0;
    private boolean moving = true;
    private double targetX, targetY;
    private int pauseTimer = 0;

    public Peahat(double x, double y) {
        super(x, y, 2, 1, AIType.RANDOM);
        applyStats(EnemyStats.peahat());
        loadPeahatSprite();
        targetX = x;
        targetY = y;
    }

    private void loadPeahatSprite() {
        sprite = loadSprite("sprites/Enemies/Peahat.gif");
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (damageTimer > 0) damageTimer--;

        angle += FLOAT_SPEED;
        floatOffset = Math.sin(angle) * FLOAT_AMPLITUDE;

        if (moving) {
            moveTimer++;
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 4) {
                x += (dx / dist) * speed;
                y += (dy / dist) * speed;
            }
            if (dist <= 4 || moveTimer > MOVE_TIMEOUT) {
                moving = false;
                pauseTimer = 0;
            }
        } else {
            pauseTimer++;
            if (pauseTimer > PAUSE_MIN + Math.random() * PAUSE_RANGE) {
                moving = true;
                moveTimer = 0;
                targetX = 32 + Math.random() * 192;
                targetY = 32 + Math.random() * 112;
            }
        }
    }

    @Override
    public void render(Graphics2D g2) {
        int drawY = (int)(y + floatOffset);
        if (sprite != null) {
            g2.drawImage(sprite, (int) x, drawY, 16, 16, null);
        } else {
            g2.setColor(new Color(180, 100, 50));
            g2.fillOval((int) x, drawY, 16, 16);
        }
        if (invulnerableFrames > 0 || !moving) {
            g2.setColor(new Color(255, 255, 255, moving ? 100 : 50));
            g2.fillRect((int) x, drawY, 16, 16);
        }
    }

    @Override
    public void damage(int amount) {
        if (!moving) super.damage(amount);
    }

    @Override
    public boolean canDamage() { return moving; }
}
