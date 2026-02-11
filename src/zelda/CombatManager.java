package zelda;

import java.awt.*;

public class CombatManager {
    public CombatManager() {}

    public void checkCombat(ZeldaPlayer player, ZeldaRoom room, AudioManager audio) {
        if (room == null || player == null) return;

        if (player.isAttacking() && player.hasSword()) {
            Rectangle swordBox = player.getSwordHitbox();
            if (swordBox != null) {
                for (ZeldaEnemy e : room.getEnemies()) {
                    if (e.isAlive() && swordBox.intersects(e.getHitbox())) {
                        e.damage(1);
                    }
                }
            }
        }

        for (ZeldaEnemy e : room.getEnemies()) {
            if (e.isAlive() && e.canDamage() && e.getHitbox().intersects(player.getHitbox())) {
                player.takeDamage(e.getDamage(), e.getX(), e.getY());
            }
        }

        for (Projectile p : room.getProjectiles()) {
            if (p.isActive() && !p.isPlayerProjectile()) {
                if (p.getHitbox().intersects(player.getHitbox())) {
                    player.takeDamage(1, p.getX(), p.getY());
                    p.deactivate();
                }
            }
        }
    }
}
