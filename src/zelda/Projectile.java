package zelda;

import java.awt.*;

public class Projectile {
    private double x, y;
    private double vx, vy;
    private int width = 8;
    private int height = 8;
    
    private boolean active = true;
    private boolean playerOwned;
    
    private int lifetime = 120;
    
    private Color color = Color.WHITE;
    
    public Projectile(double x, double y, double vx, double vy, boolean playerOwned) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.playerOwned = playerOwned;
        
        this.color = playerOwned ? Color.GREEN : Color.ORANGE;
    }
    
    public void update() {
        x += vx;
        y += vy;
        
        lifetime--;
        if (lifetime <= 0) {
            active = false;
        }
        
        if (x < -8 || x > 264 || y < -8 || y > 184) {
            active = false;
        }
    }
    
    public void render(Graphics2D g2) {
        if (!active) return;
        
        g2.setColor(color);
        g2.fillOval((int)x, (int)y, width, height);
        
        g2.setColor(Color.WHITE);
        g2.drawOval((int)x, (int)y, width, height);
    }
    
    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isActive() { return active; }
    public boolean isPlayerOwned() { return playerOwned; }
    public void deactivate() { active = false; }
    
    public void setColor(Color c) { color = c; }
    public void setSize(int w, int h) { width = w; height = h; }
}
