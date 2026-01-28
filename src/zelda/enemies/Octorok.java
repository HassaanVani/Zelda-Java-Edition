package zelda.enemies;

import zelda.*;
import javax.swing.ImageIcon;
import java.awt.*;
import java.io.File;
import java.util.List;

public class Octorok extends ZeldaEnemy {
    private int shootTimer = 0;
    private int moveTimer = 0;
    private int pauseTimer = 0;
    private boolean paused = false;
    
    private static final int SHOOT_COOLDOWN = 90;
    private static final int SHOOT_RANGE = 100;
    
    private boolean isBlue;
    private Image[] directionSprites = new Image[4];
    
    public Octorok(double x, double y, boolean blue) {
        super(x, y, blue ? 2 : 1, AIType.SHOOTER);
        this.isBlue = blue;
        this.speed = blue ? 0.6 : 0.4;
        loadSprites();
    }
    
    private void loadSprites() {
        String color = isBlue ? "Blue" : "Red";
        String base = "sprites/Enemies/Octorok - " + color;
        
        directionSprites[0] = loadGif(base + " (Back).gif");
        directionSprites[1] = loadGif(base + " (Left).gif");
        directionSprites[2] = loadGif(base + " (Front).gif");
        directionSprites[3] = directionSprites[1];
        
        if (directionSprites[2] == null) {
            directionSprites[2] = loadGif(base + ".gif");
        }
    }
    
    private Image loadGif(String path) {
        File f = new File(path);
        if (f.exists()) {
            return new ImageIcon(path).getImage();
        }
        return null;
    }
    
    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;
        
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (shootTimer > 0) shootTimer--;
        
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < SHOOT_RANGE && shootTimer == 0) {
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = dx > 0 ? 1 : 3;
            } else {
                direction = dy > 0 ? 2 : 0;
            }
            shoot(projectiles);
            shootTimer = SHOOT_COOLDOWN;
            paused = true;
            pauseTimer = 30;
        }
        
        if (paused) {
            pauseTimer--;
            if (pauseTimer <= 0) paused = false;
            return;
        }
        
        moveTimer++;
        if (moveTimer > 60 + Math.random() * 40) {
            direction = (int)(Math.random() * 4);
            moveTimer = 0;
        }
        
        switch (direction) {
            case 0: y -= speed; break;
            case 1: x += speed; break;
            case 2: y += speed; break;
            case 3: x -= speed; break;
        }
        
        int cx = (int)(x + width/2);
        int cy = (int)(y + height/2);
        
        if (!room.isWalkable(cx, cy)) {
            x = oldX;
            y = oldY;
            direction = (direction + 1 + (int)(Math.random() * 2)) % 4;
        }
        
        x = Math.max(16, Math.min(x, 240 - width));
        y = Math.max(16, Math.min(y, 160 - height));
    }
    
    private void shoot(List<Projectile> projectiles) {
        double vx = 0, vy = 0;
        double projSpeed = 1.5;
        
        switch (direction) {
            case 0: vy = -projSpeed; break;
            case 1: vx = projSpeed; break;
            case 2: vy = projSpeed; break;
            case 3: vx = -projSpeed; break;
        }
        
        Projectile rock = new Projectile(x + 4, y + 4, vx, vy, false);
        rock.setColor(new Color(139, 90, 43));
        rock.setSize(6, 6);
        projectiles.add(rock);
    }
    
    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        
        int drawX = (int)x;
        int drawY = (int)y;
        
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect(drawX, drawY, 16, 16);
            return;
        }
        
        Image img = directionSprites[direction];
        if (img != null) {
            if (direction == 1) {
                g2.drawImage(img, drawX + 16, drawY, -16, 16, null);
            } else {
                g2.drawImage(img, drawX, drawY, 16, 16, null);
            }
        } else {
            g2.setColor(isBlue ? new Color(0, 100, 200) : new Color(200, 50, 50));
            g2.fillOval(drawX, drawY, 16, 16);
        }
    }
}
