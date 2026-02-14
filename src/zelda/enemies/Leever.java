package zelda.enemies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

public class Leever extends ZeldaEnemy {
    private static final int BURROW_DURATION = 90;
    private static final int EMERGE_DURATION = 20;
    private static final int ACTIVE_DURATION = 120;
    private static final double CHASE_RANGE = 100;

    private boolean isBlue;
    private int stateTimer = 0;
    private LeeverState state = LeeverState.BURROWED;
    private double targetX, targetY;
    private BufferedImage spriteEmerging;
    private BufferedImage spriteFull;

    private enum LeeverState { BURROWED, EMERGING, ACTIVE, BURROWING }

    public Leever(double x, double y, boolean isBlue) {
        super(x, y, 2, 1, AIType.CHASE);
        this.isBlue = isBlue;
        applyStats(isBlue ? EnemyStats.leeverBlue() : EnemyStats.leeverRed());
        loadLeeverSprites();
    }

    private void loadLeeverSprites() {
        String color = isBlue ? "Blue" : "Red";
        spriteEmerging = loadSprite("sprites/Enemies/Leever - " + color + "1.gif");
        spriteFull = loadSprite("sprites/Enemies/Leever - " + color + "2.gif");
        sprite = null;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (damageTimer > 0) damageTimer--;
        stateTimer++;

        switch (state) {
            case BURROWED:
                sprite = null;
                if (stateTimer > BURROW_DURATION + Math.random() * 60) {
                    double dx = player.getWorldX() - x;
                    double dy = player.getWorldY() - y;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < CHASE_RANGE) {
                        targetX = player.getWorldX() + (Math.random() - 0.5) * 40;
                        targetY = player.getWorldY() + (Math.random() - 0.5) * 40;
                    } else {
                        targetX = x + (Math.random() - 0.5) * 60;
                        targetY = y + (Math.random() - 0.5) * 60;
                    }
                    state = LeeverState.EMERGING;
                    stateTimer = 0;
                }
                break;

            case EMERGING:
                sprite = spriteEmerging;
                if (stateTimer > EMERGE_DURATION) {
                    state = LeeverState.ACTIVE;
                    sprite = spriteFull;
                    stateTimer = 0;
                }
                break;

            case ACTIVE:
                sprite = spriteFull;
                double dx = targetX - x;
                double dy = targetY - y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist > 4) {
                    x += (dx / dist) * speed;
                    y += (dy / dist) * speed;
                }

                if (stateTimer > ACTIVE_DURATION || dist <= 4) {
                    state = LeeverState.BURROWING;
                    stateTimer = 0;
                }
                break;

            case BURROWING:
                sprite = spriteEmerging;
                if (stateTimer > EMERGE_DURATION) {
                    state = LeeverState.BURROWED;
                    stateTimer = 0;
                }
                break;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, 16, 16, null);
        }
        if (invulnerableFrames > 0) {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillRect((int)x, (int)y, 16, 16);
        }
    }

    @Override
    public boolean canDamage() { return state == LeeverState.ACTIVE; }

    @Override
    public Rectangle getHitbox() {
        if (state == LeeverState.BURROWED) return new Rectangle(0, 0, 0, 0);
        return super.getHitbox();
    }
}
