package zelda.enemies;

import zelda.ZeldaEnemy;
import zelda.ZeldaPlayer;
import zelda.ZeldaRoom;
import zelda.Projectile;
import java.util.List;

public class Tektite extends ZeldaEnemy {
    private boolean isBlue;
    private int jumpTimer = 0;
    private int jumpCooldown = 60;
    private double velX = 0, velY = 0;
    private boolean jumping = false;
    private int animFrame = 0;
    private int animTimer = 0;
    
    public Tektite(int x, int y, boolean isBlue) {
        super(x, y, 1, 1, AIType.RANDOM);
        this.isBlue = isBlue;
        loadTektiteSprite();
    }
    
    private void loadTektiteSprite() {
        String color = isBlue ? "Blue" : "Red";
        sprite = loadSprite("sprites/Enemies/Tektike - " + color + ".gif");
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        super.update(player, room, projectiles);
        
        animTimer++;
        if (animTimer >= 8) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 2;
        }
        
        if (jumping) {
            x += velX;
            y += velY;
            velY += 0.3;
            
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
    }
    
    private double targetY;
    
    private void startJump(ZeldaPlayer player) {
        jumping = true;
        targetY = y;
        
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist > 0) {
            double jumpDist = Math.min(dist, 40 + Math.random() * 30);
            velX = (dx / dist) * jumpDist / 30;
            velY = -4 - Math.random() * 2;
        } else {
            velX = (Math.random() - 0.5) * 3;
            velY = -4;
        }
        
        jumpCooldown = 40 + (int)(Math.random() * 40);
    }
    
    @Override
    public boolean canDamage() {
        return true;
    }
}
