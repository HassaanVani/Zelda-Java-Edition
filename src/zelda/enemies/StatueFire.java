package zelda.enemies;

import java.awt.*;
import java.util.List;
import zelda.*;

/**
 * StatueFire: invulnerable dungeon wall statue that periodically shoots fireballs.
 * Fireballs are unblockable by any shield.
 * Part of the room environment, cannot be killed.
 */
public class StatueFire extends ZeldaEnemy {
    private int shootTimer = 0;
    private static final int SHOOT_INTERVAL = 120; // ~2 seconds
    private int facing; // 0=up, 1=left, 2=down, 3=right

    public StatueFire(double x, double y) {
        this(x, y, 2); // Default: facing down
    }

    public StatueFire(double x, double y, int facing) {
        super(x, y, 999, 0, AIType.RANDOM);
        applyStats(EnemyStats.statueFire());
        this.facing = facing;
        this.shootTimer = (int)(Math.random() * SHOOT_INTERVAL); // Stagger shots
    }

    @Override
    public void damage(int amount) {
        // Invulnerable
    }

    @Override
    public boolean canDamage() { return false; } // Damage via projectiles only

    @Override
    public Rectangle getHitbox() {
        return new Rectangle(0, 0, 0, 0); // No physical hitbox (embedded in wall)
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;

        shootTimer++;
        if (shootTimer >= SHOOT_INTERVAL) {
            shootTimer = 0;
            shootFireball(player, projectiles);
        }
    }

    private void shootFireball(ZeldaPlayer player, List<Projectile> projectiles) {
        double spd = 1.5;
        double vx = 0, vy = 0;
        switch (facing) {
            case 0: vy = -spd; break;
            case 1: vx = -spd; break;
            case 2: vy = spd; break;
            case 3: vx = spd; break;
        }

        Projectile fireball = new Projectile(x + width / 2 - 3, y + height / 2 - 3, vx, vy, false);
        fireball.setColor(new Color(255, 80, 0));
        fireball.setSize(6, 6);
        fireball.setDamage(1); // 1/2 heart
        fireball.setUnblockable(true); // Cannot be blocked by any shield
        projectiles.add(fireball);
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        // Draw as a stone face
        g2.setColor(new Color(100, 100, 100));
        g2.fillRect((int)x, (int)y, width, height);
        g2.setColor(new Color(60, 60, 60));
        g2.fillRect((int)x + 3, (int)y + 4, 4, 3);
        g2.fillRect((int)x + 9, (int)y + 4, 4, 3);
        g2.setColor(new Color(180, 60, 0));
        g2.fillRect((int)x + 5, (int)y + 10, 6, 3);
    }
}
