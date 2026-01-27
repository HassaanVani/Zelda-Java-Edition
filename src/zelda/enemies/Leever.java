package zelda.enemies;

import zelda.ZeldaEnemy;
import zelda.ZeldaPlayer;
import zelda.ZeldaRoom;
import zelda.Projectile;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Leever extends ZeldaEnemy {
    private boolean isBlue;
    private int stateTimer = 0;
    private LeverState state = LeverState.BURROWED;
    private double targetX, targetY;
    private BufferedImage spriteEmerging;
    private BufferedImage spriteFull;
    
    private enum LeverState {
        BURROWED,
        EMERGING,
        ACTIVE,
        BURROWING
    }
    
    public Leever(int x, int y, boolean isBlue) {
        super(x, y, isBlue ? 2 : 1, 1, AIType.CHASE);
        this.isBlue = isBlue;
        this.speed = isBlue ? 0.8 : 0.5;
        loadLeeverSprites();
    }
    
    private void loadLeeverSprites() {
        String color = isBlue ? "Blue" : "Red";
        spriteEmerging = loadSprite("sprites/Enemies/Leever - " + color + "1.gif");
        spriteFull = loadSprite("sprites/Enemies/Leever - " + color + "2.gif");
        sprite = null;
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        super.update(player, room, projectiles);
        stateTimer++;
        
        switch (state) {
            case BURROWED:
                sprite = null;
                if (stateTimer > 90 + Math.random() * 60) {
                    double dx = player.getWorldX() - x;
                    double dy = player.getWorldY() - y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    
                    if (dist < 100) {
                        targetX = player.getWorldX() + (Math.random() - 0.5) * 40;
                        targetY = player.getWorldY() + (Math.random() - 0.5) * 40;
                    } else {
                        targetX = x + (Math.random() - 0.5) * 60;
                        targetY = y + (Math.random() - 0.5) * 60;
                    }
                    state = LeverState.EMERGING;
                    stateTimer = 0;
                }
                break;
                
            case EMERGING:
                sprite = spriteEmerging;
                if (stateTimer > 20) {
                    state = LeverState.ACTIVE;
                    sprite = spriteFull;
                    stateTimer = 0;
                }
                break;
                
            case ACTIVE:
                sprite = spriteFull;
                double dx = targetX - x;
                double dy = targetY - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist > 4) {
                    x += (dx / dist) * speed;
                    y += (dy / dist) * speed;
                }
                
                if (stateTimer > 120 || dist <= 4) {
                    state = LeverState.BURROWING;
                    stateTimer = 0;
                }
                break;
                
            case BURROWING:
                sprite = spriteEmerging;
                if (stateTimer > 20) {
                    state = LeverState.BURROWED;
                    stateTimer = 0;
                }
                break;
        }
    }
    
    @Override
    public void render(Graphics2D g2) {
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, 16, 16, null);
        }
        
        if (invulnerableFrames > 0) {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillRect((int)x, (int)y, 16, 16);
        }
    }
    
    @Override
    public boolean canDamage() {
        return state == LeverState.ACTIVE;
    }
    
    @Override
    public Rectangle getHitbox() {
        if (state == LeverState.BURROWED) {
            return new Rectangle(0, 0, 0, 0);
        }
        return super.getHitbox();
    }
}
