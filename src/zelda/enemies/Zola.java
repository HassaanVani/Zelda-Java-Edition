package zelda.enemies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Zola (Zora): water enemy that surfaces briefly to shoot fireballs, then submerges.
 * Cannot be damaged while submerged. 2 HP.
 */
public class Zola extends ZeldaEnemy {
    private enum Phase { SUBMERGED, SURFACING, SHOOTING, SUBMERGING }
    private Phase phase = Phase.SUBMERGED;
    private int phaseTimer = 0;
    private static final int SUBMERGE_TIME = 90;
    private static final int SURFACE_TIME = 20;
    private static final int SHOOT_TIME = 40;
    private BufferedImage frontSprite;
    private BufferedImage backSprite;

    public Zola(double x, double y) {
        super(x, y, 2, 1, AIType.SHOOTER);
        applyStats(EnemyStats.zola());
        loadZolaSprites();
    }

    private void loadZolaSprites() {
        frontSprite = loadSprite("sprites/Enemies/Zola (Front).gif");
        backSprite = loadSprite("sprites/Enemies/Zola (Back).gif");
        sprite = frontSprite;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        phaseTimer++;
        switch (phase) {
            case SUBMERGED:
                if (phaseTimer >= SUBMERGE_TIME) {
                    // Reposition randomly in water area
                    x = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_W - 64);
                    y = 32 + Math.random() * (ZeldaRoom.ROOM_PIXEL_H - 64);
                    phase = Phase.SURFACING;
                    phaseTimer = 0;
                }
                break;
            case SURFACING:
                sprite = backSprite; // emerging from water shows back
                if (phaseTimer >= SURFACE_TIME) {
                    phase = Phase.SHOOTING;
                    phaseTimer = 0;
                    sprite = frontSprite; // face player when shooting
                    // Shoot fireball at player
                    double dx = player.getWorldX() - x;
                    double dy = player.getWorldY() - y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0) {
                        double spd = 2.0;
                        Projectile fb = new Projectile(x + width / 2, y + height / 2,
                            (dx / dist) * spd, (dy / dist) * spd, false);
                        fb.setColor(new Color(255, 120, 40));
                        fb.setSize(8, 8);
                        fb.setDamage(2);
                        projectiles.add(fb);
                    }
                }
                break;
            case SHOOTING:
                sprite = frontSprite;
                if (phaseTimer >= SHOOT_TIME) {
                    phase = Phase.SUBMERGING;
                    phaseTimer = 0;
                }
                break;
            case SUBMERGING:
                sprite = backSprite; // sinking back shows back
                if (phaseTimer >= SURFACE_TIME) {
                    phase = Phase.SUBMERGED;
                    phaseTimer = 0;
                }
                break;
        }
    }

    @Override
    public boolean canDamage() {
        return active && (phase == Phase.SURFACING || phase == Phase.SHOOTING);
    }

    @Override
    public void damage(int amount) {
        if (phase == Phase.SUBMERGED || phase == Phase.SUBMERGING) return;
        super.damage(amount);
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (phase == Phase.SUBMERGED) return;

        float alpha = (phase == Phase.SURFACING || phase == Phase.SUBMERGING) ? 0.6f : 1.0f;
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, width, height, null);
        } else {
            g2.setColor(new Color(40, 120, 40));
            g2.fillOval((int)x, (int)y, width, height);
            g2.setColor(Color.RED);
            g2.fillRect((int)x + 4, (int)y + 4, 3, 3);
            g2.fillRect((int)x + 10, (int)y + 4, 3, 3);
        }
        g2.setComposite(old);
    }
}
