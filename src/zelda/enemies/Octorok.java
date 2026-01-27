package zelda.enemies;

import zelda.*;
import java.util.List;

public class Octorok extends ZeldaEnemy {
    private int shootTimer = 0;
    private static final int SHOOT_COOLDOWN = 90;
    private static final int SHOOT_RANGE = 100;
    
    private boolean isBlue;
    
    public Octorok(double x, double y, boolean blue) {
        super(x, y, blue ? 2 : 1, AIType.SHOOTER);
        this.isBlue = blue;
        this.speed = blue ? 0.6 : 0.5;
        loadOctorokSprite();
    }
    
    private void loadOctorokSprite() {
        String color = isBlue ? "Blue" : "Red";
        sprite = loadSprite("sprites/Enemies/Octorok - " + color + " (Front).gif");
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;
        
        if (damageTimer > 0) damageTimer--;
        if (shootTimer > 0) shootTimer--;
        
        animationCounter++;
        if (animationCounter >= 10) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }
        
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < SHOOT_RANGE && shootTimer == 0) {
            shoot(player, projectiles);
            shootTimer = SHOOT_COOLDOWN;
        }
        
        randomMove(room);
    }
    
    private void shoot(ZeldaPlayer player, List<Projectile> projectiles) {
        double vx = 0, vy = 0;
        double projSpeed = 1.5;
        
        switch (direction) {
            case 0: vy = -projSpeed; break;
            case 1: vx = projSpeed; break;
            case 2: vy = projSpeed; break;
            case 3: vx = -projSpeed; break;
        }
        
        Projectile rock = new Projectile(x + 3, y + 3, vx, vy, false);
        rock.setColor(new java.awt.Color(139, 90, 43));
        rock.setSize(6, 6);
        projectiles.add(rock);
    }
}
