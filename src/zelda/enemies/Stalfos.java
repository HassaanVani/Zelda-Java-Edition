package zelda.enemies;

import java.util.List;
import zelda.*;

public class Stalfos extends ZeldaEnemy {
    private static final int CHASE_RANGE = 80;
    
    public Stalfos(double x, double y) {
        super(x, y, 2, AIType.CHASE);
        applyStats(EnemyStats.stalfos());
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
        } else if (room != null) {
            randomMove(room);
        }
        
        int centerX = (int)(x + width/2);
        int centerY = (int)(y + height/2);
        
        if (room != null && !room.isWalkable(centerX, centerY)) {
            x = oldX;
            y = oldY;
        }
        
        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
    }
}
