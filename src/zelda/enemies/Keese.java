package zelda.enemies;

import zelda.*;
import java.util.List;

public class Keese extends ZeldaEnemy {
    private double targetX, targetY;
    private int moveTimer = 0;
    private boolean isBlue;
    
    public Keese(double x, double y, boolean blue) {
        super(x, y, 1, AIType.RANDOM);
        this.isBlue = blue;
        this.speed = blue ? 1.2 : 1.0;
        this.targetX = x;
        this.targetY = y;
        loadKeeseSprite();
    }
    
    private void loadKeeseSprite() {
        String color = isBlue ? "Blue" : "Red";
        sprite = loadSprite("sprites/Enemies/Keese - " + color + ".gif");
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;
        
        if (damageTimer > 0) damageTimer--;
        
        animationCounter++;
        if (animationCounter >= 6) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }
        
        moveTimer--;
        if (moveTimer <= 0) {
            targetX = 16 + Math.random() * 224;
            targetY = 16 + Math.random() * 144;
            moveTimer = 30 + (int)(Math.random() * 60);
        }
        
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist > speed) {
            x += (dx / dist) * speed;
            y += (dy / dist) * speed;
        }
        
        int centerX = (int)(x + width/2);
        int centerY = (int)(y + height/2);
        
        if (!room.isWalkable(centerX, centerY)) {
            x = oldX;
            y = oldY;
            moveTimer = 0;
        }
        
        x = Math.max(8, Math.min(x, 256 - width - 8));
        y = Math.max(8, Math.min(y, 176 - height - 8));
    }
}
