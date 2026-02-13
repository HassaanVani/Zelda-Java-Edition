package zelda;

import java.awt.*;

public class CombatManager {

    private boolean swordBeamFired = false;

    public CombatManager() {}

    public void checkCombat(ZeldaPlayer player, ZeldaRoom room, AudioManager audio) {
        if (room == null || player == null) return;

        Inventory inv = player.getInventory();
        int swordDmg = inv.getSwordDamage();

        // --- Sword vs enemies ---
        if (player.isAttacking() && inv.hasSword()) {
            Rectangle swordBox = player.getSwordHitbox();
            if (swordBox != null) {
                for (ZeldaEnemy e : room.getEnemies()) {
                    if (e.isAlive() && swordBox.intersects(e.getHitbox())) {
                        e.damage(swordDmg);
                    }
                }
            }

            // Sword beam: fire once per swing when at full health
            if (inv.isFullHealth() && !swordBeamFired) {
                fireSwordBeam(player, room, swordDmg);
                swordBeamFired = true;
            }
        }
        if (!player.isAttacking()) {
            swordBeamFired = false;
        }

        // --- Player projectiles vs enemies ---
        for (Projectile p : room.getProjectiles()) {
            if (p.isActive() && p.isPlayerProjectile()) {
                for (ZeldaEnemy e : room.getEnemies()) {
                    if (e.isAlive() && p.getHitbox().intersects(e.getHitbox())) {
                        e.damage(p.getDamage());
                        if (!p.isPiercing()) p.deactivate();
                        break;
                    }
                }
            }
        }

        // --- Enemy contact damage ---
        for (ZeldaEnemy e : room.getEnemies()) {
            if (e.isAlive() && e.canDamage() && e.getHitbox().intersects(player.getHitbox())) {
                player.takeDamage(e.getDamage(), e.getX(), e.getY());
            }
        }

        // --- Enemy projectiles vs player (with shield deflection) ---
        for (Projectile p : room.getProjectiles()) {
            if (p.isActive() && !p.isPlayerProjectile()) {
                if (p.getHitbox().intersects(player.getHitbox())) {
                    if (canShieldDeflect(player, p)) {
                        p.deactivate();
                    } else {
                        player.takeDamage(p.getDamage(), p.getX(), p.getY());
                        p.deactivate();
                    }
                }
            }
        }
    }

    private void fireSwordBeam(ZeldaPlayer player, ZeldaRoom room, int damage) {
        double px = player.getWorldX() + ZeldaPlayer.WIDTH / 2;
        double py = player.getWorldY() + ZeldaPlayer.HEIGHT / 2;
        double speed = 3.0;
        double vx = 0, vy = 0;

        switch (player.getDirection()) {
            case ZeldaPlayer.DIR_UP:    vy = -speed; py = player.getWorldY(); break;
            case ZeldaPlayer.DIR_DOWN:  vy = speed;  py = player.getWorldY() + ZeldaPlayer.HEIGHT; break;
            case ZeldaPlayer.DIR_LEFT:  vx = -speed; px = player.getWorldX(); break;
            case ZeldaPlayer.DIR_RIGHT: vx = speed;  px = player.getWorldX() + ZeldaPlayer.WIDTH; break;
        }

        Projectile beam = new Projectile(px - 3, py - 3, vx, vy, true);
        beam.setColor(new Color(200, 200, 255));
        beam.setSize(6, 6);
        beam.setDamage(damage);
        room.getProjectiles().add(beam);
    }

    /**
     * Shield deflects projectiles from the front.
     * Small shield: rocks and arrows.
     * Magical shield: also fireballs and boomerangs.
     */
    private boolean canShieldDeflect(ZeldaPlayer player, Projectile p) {
        Inventory inv = player.getInventory();
        if (inv.getShieldLevel() == 0) return false;

        // Check if projectile is coming from the direction player faces
        int dir = player.getDirection();
        double dx = p.getX() - player.getWorldX();
        double dy = p.getY() - player.getWorldY();

        boolean facingProjectile = false;
        switch (dir) {
            case ZeldaPlayer.DIR_UP:    facingProjectile = dy < 0; break;
            case ZeldaPlayer.DIR_DOWN:  facingProjectile = dy > 0; break;
            case ZeldaPlayer.DIR_LEFT:  facingProjectile = dx < 0; break;
            case ZeldaPlayer.DIR_RIGHT: facingProjectile = dx > 0; break;
        }

        if (!facingProjectile) return false;

        // Small shield blocks basic projectiles; magical shield blocks all
        if (inv.hasMagicalShield()) return true;
        return inv.getShieldLevel() >= 1;
    }
}
