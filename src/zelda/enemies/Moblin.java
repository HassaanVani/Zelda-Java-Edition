package zelda.enemies;

import zelda.*;
import java.util.List;

public class Moblin extends ZeldaEnemy {
    private int shootTimer = 0;
    private static final int SHOOT_COOLDOWN = 120;
    
    private boolean isBlack;
    private double patrolStartX, patrolEndX;
    
    public Moblin(double x, double y, boolean black) {
        super(x, y, black ? 3 : 2, AIType.PATROL);
        this.isBlack = black;
        this.speed = black ? 0.7 : 0.6;
        this.patrolStartX = x - 40;
        this.patrolEndX = x + 40;
        loadMoblinSprite();
    }
    
    private void loadMoblinSprite() {
        String color = isBlack ? "Black" : "Red";
        sprite = loadSprite("sprites/Enemies/Moblin - " + color + " (Front).gif");
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;
        
        if (damageTimer > 0) damageTimer--;
        if (shootTimer > 0) shootTimer--;
        
        animationCounter++;
        if (animationCounter >= 12) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }
        
        doPatrol(room);
        
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 80 && shootTimer == 0) {
            shootSpear(player, projectiles);
            shootTimer = SHOOT_COOLDOWN;
        }
    }
    
    private void doPatrol(ZeldaRoom room) {
        if (direction == 1) {
            x += speed;
            if (x > patrolEndX) direction = 3;
        } else if (direction == 3) {
            x -= speed;
            if (x < patrolStartX) direction = 1;
        } else {
            if (Math.random() < 0.01) {
                direction = Math.random() < 0.5 ? 1 : 3;
            }
        }
        
        int centerX = (int)(x + width/2);
        int centerY = (int)(y + height/2);
        
        if (!room.isWalkable(centerX, centerY)) {
            x = oldX;
            y = oldY;
            direction = (direction == 1) ? 3 : 1;
        }
        
        x = Math.max(8, Math.min(x, 256 - width - 8));
        y = Math.max(8, Math.min(y, 176 - height - 8));
    }
    
    private void shootSpear(ZeldaPlayer player, List<Projectile> projectiles) {
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist > 0) {
            double vx = (dx / dist) * 2.0;
            double vy = (dy / dist) * 2.0;
            
            Projectile spear = new Projectile(x + 3, y + 3, vx, vy, false);
            spear.setColor(new java.awt.Color(139, 69, 19));
            spear.setSize(4, 12);
            projectiles.add(spear);
        }
    }
}
