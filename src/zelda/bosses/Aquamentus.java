package zelda.bosses;

import zelda.*;
import java.awt.*;
import java.util.List;

public class Aquamentus extends ZeldaEnemy {
    private int shootTimer = 0;
    private static final int SHOOT_COOLDOWN = 120;
    
    private double moveTargetX;
    private int moveTimer = 0;
    
    private int animFrame = 0;
    private int animTimer = 0;
    
    public Aquamentus(double x, double y) {
        super(x, y, 6, AIType.SHOOTER);
        this.width = 24;
        this.height = 32;
        this.speed = 0.3;
        this.damage = 2;
        this.moveTargetX = x;
        
        loadAquamentusSprite();
    }
    
    private void loadAquamentusSprite() {
        sprite = loadSprite("sprites/Bosses/1 - Aquamentus-1.gif");
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;
        
        if (damageTimer > 0) damageTimer--;
        if (shootTimer > 0) shootTimer--;
        
        animTimer++;
        if (animTimer >= 15) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 2;
            
            String spritePath = "sprites/Bosses/1 - Aquamentus-" + (animFrame + 1) + ".gif";
            sprite = loadSprite(spritePath);
        }
        
        moveTimer--;
        if (moveTimer <= 0) {
            moveTargetX = 160 + Math.random() * 80;
            moveTimer = 60 + (int)(Math.random() * 60);
        }
        
        if (Math.abs(x - moveTargetX) > speed) {
            x += (moveTargetX > x) ? speed : -speed;
        }
        
        if (shootTimer == 0) {
            shootFireballs(player, projectiles);
            shootTimer = SHOOT_COOLDOWN;
        }
    }
    
    private void shootFireballs(ZeldaPlayer player, List<Projectile> projectiles) {
        double baseAngle = Math.atan2(player.getWorldY() - y, player.getWorldX() - x);
        double spread = Math.PI / 6;
        double projSpeed = 1.5;
        
        for (int i = -1; i <= 1; i++) {
            double angle = baseAngle + i * spread;
            double vx = Math.cos(angle) * projSpeed;
            double vy = Math.sin(angle) * projSpeed;
            
            Projectile fireball = new Projectile(x, y + height/2, vx, vy, false);
            fireball.setColor(Color.ORANGE);
            fireball.setSize(10, 10);
            projectiles.add(fireball);
        }
    }
    
    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, null);
        } else {
            g2.setColor(new Color(0, 100, 0));
            g2.fillRect((int)x, (int)y, width, height);
            
            g2.setColor(Color.RED);
            g2.fillOval((int)x + width - 8, (int)y + 8, 6, 6);
            
            g2.setColor(new Color(0, 80, 0));
            g2.fillRect((int)x - 8, (int)y + 12, 10, 4);
        }
        
        g2.setColor(Color.BLACK);
        g2.fillRect((int)x, (int)y - 6, width, 4);
        g2.setColor(Color.RED);
        int healthWidth = (int)((double)health / maxHealth * width);
        g2.fillRect((int)x, (int)y - 6, healthWidth, 4);
    }
}
