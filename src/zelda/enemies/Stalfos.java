package zelda.enemies;

import zelda.*;
import java.util.List;

public class Stalfos extends ZeldaEnemy {
    private static final int CHASE_RANGE = 80;
    
    public Stalfos(double x, double y) {
        super(x, y, 2, AIType.CHASE);
        this.speed = 0.7;
        loadStalfosSprite();
    }
    
    private void loadStalfosSprite() {
        sprite = loadSprite("sprites/Enemies/Stalfos.gif");
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;
        
        if (damageTimer > 0) damageTimer--;
        
        animationCounter++;
        if (animationCounter >= 10) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }
        
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < CHASE_RANGE && dist > 0) {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
            
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = dx > 0 ? 1 : 3;
            } else {
                direction = dy > 0 ? 2 : 0;
            }
        } else {
            randomMove(room);
        }
        
        int centerX = (int)(x + width/2);
        int centerY = (int)(y + height/2);
        
        if (!room.isWalkable(centerX, centerY)) {
            x = oldX;
            y = oldY;
        }
        
        x = Math.max(8, Math.min(x, 256 - width - 8));
        y = Math.max(8, Math.min(y, 176 - height - 8));
    }
}
