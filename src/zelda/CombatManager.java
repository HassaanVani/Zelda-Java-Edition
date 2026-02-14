package zelda;

import java.awt.*;

public class CombatManager {

    private boolean swordBeamFired = false;
    private ItemDropSystem itemDropSystem;

    public CombatManager() {}

    public void setItemDropSystem(ItemDropSystem ids) { this.itemDropSystem = ids; }

    public void checkCombat(ZeldaPlayer player, ZeldaRoom room, AudioManager audio) {
        if (room == null || player == null) return;
        doCombat(player, room.getEnemies(), room.getProjectiles(), audio);
        // Sword beam into overworld room projectiles
        Inventory inv = player.getInventory();
        if (player.isAttacking() && inv.hasSword() && inv.isFullHealth() && !swordBeamFired) {
            fireSwordBeam(player, room.getProjectiles(), inv.getSwordDamage());
            swordBeamFired = true;
        }
        if (!player.isAttacking()) swordBeamFired = false;
    }

    public void checkDungeonCombat(ZeldaPlayer player, DungeonRoom room, AudioManager audio) {
        if (room == null || player == null) return;
        doCombat(player, room.getEnemies(), room.getProjectiles(), audio);
        // Sword beam into dungeon room projectiles
        Inventory inv = player.getInventory();
        if (player.isAttacking() && inv.hasSword() && inv.isFullHealth() && !swordBeamFired) {
            fireSwordBeam(player, room.getProjectiles(), inv.getSwordDamage());
            swordBeamFired = true;
        }
        if (!player.isAttacking()) swordBeamFired = false;
    }

    private void doCombat(ZeldaPlayer player, java.util.List<ZeldaEnemy> enemies,
                          java.util.List<Projectile> projectiles, AudioManager audio) {
        Inventory inv = player.getInventory();
        int swordDmg = inv.getSwordDamage();

        // --- Sword vs enemies ---
        if (player.isAttacking() && inv.hasSword()) {
            Rectangle swordBox = player.getSwordHitbox();
            if (swordBox != null) {
                for (ZeldaEnemy e : enemies) {
                    if (e.isAlive() && swordBox.intersects(e.getHitbox())) {
                        if (e.isImmuneToWeapon(EnemyStats.DMG_SWORD)) continue;
                        if (e.hasFrontShield() && isFacingAttacker(e, player)) continue;
                        e.damage(swordDmg);
                    }
                }
            }
        }

        // --- Player projectiles vs enemies ---
        for (Projectile p : projectiles) {
            if (p.isActive() && p.isPlayerProjectile()) {
                // Bombs only damage during explode phase
                if (p.isBomb() && p.getBombPhase() != 2) continue;
                // Returning boomerangs don't deal damage
                if (p.isReturning()) continue;
                for (ZeldaEnemy e : enemies) {
                    if (e.isAlive() && p.getHitbox().intersects(e.getHitbox())) {
                        int weaponType = getProjectileWeaponType(p);
                        if (e.isImmuneToWeapon(weaponType)) {
                            if (!p.isPiercing()) p.deactivate();
                            break;
                        }
                        if (e.hasFrontShield() && isProjectileHittingFront(e, p)) {
                            if (!p.isPiercing()) p.deactivate();
                            break;
                        }
                        int dmg = p.getDamage();
                        // Pols Voice: instant kill by arrow
                        if (e instanceof zelda.enemies.PolsVoice && weaponType == EnemyStats.DMG_ARROW) {
                            dmg = e.getHealth();
                        }
                        // Gohma: only arrows damage, and only when eye is open
                        if (e instanceof zelda.bosses.Gohma) {
                            if (weaponType == EnemyStats.DMG_ARROW) {
                                ((zelda.bosses.Gohma) e).damageByArrow(e.getHealth()); // instant kill
                            }
                            // Non-arrow projectiles do nothing to Gohma
                        } else if (e instanceof zelda.bosses.Ganon) {
                            // Silver arrow (damage >= 4) can kill Ganon
                            if (weaponType == EnemyStats.DMG_ARROW && dmg >= 4) {
                                ((zelda.bosses.Ganon) e).damageBysilverArrow(dmg);
                            } else {
                                e.damage(dmg); // reveals + damages but can't kill
                            }
                        } else {
                            e.damage(dmg);
                        }
                        if (!p.isPiercing()) p.deactivate();
                        break;
                    }
                }
            }
        }

        // --- Enemy contact damage ---
        for (ZeldaEnemy e : enemies) {
            if (e.isAlive() && e.canDamage() && e.getHitbox().intersects(player.getHitbox())) {
                player.takeDamage(e.getDamage(), e.getX(), e.getY());
                if (itemDropSystem != null) itemDropSystem.onLinkDamaged();
            }
        }

        // --- Enemy projectiles vs player (with shield deflection) ---
        for (Projectile p : projectiles) {
            if (p.isActive() && !p.isPlayerProjectile()) {
                if (p.getHitbox().intersects(player.getHitbox())) {
                    if (canShieldDeflect(player, p)) {
                        p.deactivate();
                    } else {
                        player.takeDamage(p.getDamage(), p.getX(), p.getY());
                        if (itemDropSystem != null) itemDropSystem.onLinkDamaged();
                        p.deactivate();
                    }
                }
            }
        }
    }

    private void fireSwordBeam(ZeldaPlayer player, java.util.List<Projectile> projectiles, int damage) {
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
        projectiles.add(beam);
    }

    /**
     * Returns true if the enemy is facing the attacker (player).
     * Used for Darknut front-shield: attacks from the direction the enemy faces are parried.
     */
    private boolean isFacingAttacker(ZeldaEnemy enemy, ZeldaPlayer player) {
        int eDir = enemy.getDirection();
        double dx = player.getWorldX() - enemy.getX();
        double dy = player.getWorldY() - enemy.getY();
        switch (eDir) {
            case 0: return dy < 0;  // enemy facing up, player is above
            case 1: return dx < 0;  // enemy facing left, player is left
            case 2: return dy > 0;  // enemy facing down, player is below
            case 3: return dx > 0;  // enemy facing right, player is right
        }
        return false;
    }

    /**
     * Returns true if the projectile is hitting the enemy's front face.
     */
    private boolean isProjectileHittingFront(ZeldaEnemy enemy, Projectile p) {
        int eDir = enemy.getDirection();
        double dx = p.getX() - enemy.getX();
        double dy = p.getY() - enemy.getY();
        switch (eDir) {
            case 0: return dy < 0;
            case 1: return dx < 0;
            case 2: return dy > 0;
            case 3: return dx > 0;
        }
        return false;
    }

    /**
     * Determines the weapon damage type of a player projectile based on its properties.
     */
    private int getProjectileWeaponType(Projectile p) {
        if (p.getDamage() == 0) return EnemyStats.DMG_BOOMERANG;
        java.awt.Color c = p.getColor();
        if (c.equals(Color.ORANGE) || c.equals(new Color(255, 100, 100))) return EnemyStats.DMG_FIRE;
        if (c.equals(Color.DARK_GRAY)) return EnemyStats.DMG_BOMB;
        if (c.equals(Color.WHITE) || c.equals(new Color(180, 120, 40))) return EnemyStats.DMG_ARROW;
        if (c.equals(new Color(200, 200, 255))) return EnemyStats.DMG_SWORD; // sword beam
        return EnemyStats.DMG_ROD;
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

        // Determine projectile type from color
        Color c = p.getColor();
        boolean isRock = c.equals(Color.ORANGE) && p.getDamage() <= 1;
        boolean isArrow = c.equals(new Color(180, 120, 40)) || c.equals(Color.WHITE);

        // Magical shield blocks everything deflectable
        if (inv.hasMagicalShield()) return true;

        // Small shield only blocks rocks and arrows
        if (inv.getShieldLevel() >= 1) {
            return isRock || isArrow;
        }

        return false;
    }
}
