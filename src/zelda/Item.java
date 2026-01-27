package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Item {
    public enum ItemType {
        HEART(2, 0, 0, 0),
        HEART_CONTAINER(0, 0, 0, 0),
        RUPEE_GREEN(0, 1, 0, 0),
        RUPEE_BLUE(0, 5, 0, 0),
        RUPEE_RED(0, 20, 0, 0),
        KEY(0, 0, 1, 0),
        BOMB(0, 0, 0, 1),
        CLOCK(0, 0, 0, 0),
        SWORD(0, 0, 0, 0),
        BOOMERANG(0, 0, 0, 0);
        
        public final int healAmount;
        public final int rupeeAmount;
        public final int keyAmount;
        public final int bombAmount;
        
        ItemType(int heal, int rupees, int keys, int bombs) {
            this.healAmount = heal;
            this.rupeeAmount = rupees;
            this.keyAmount = keys;
            this.bombAmount = bombs;
        }
    }
    
    private double x, y;
    private int width = 8;
    private int height = 8;
    private ItemType type;
    private boolean active = true;
    
    private int lifeTimer = 600;
    private int blinkTimer = 0;
    
    private BufferedImage sprite;
    
    public Item(double x, double y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        loadSprite();
    }
    
    private void loadSprite() {
        String basePath = "sprites/Objects/";
        String filename = "";
        
        switch (type) {
            case HEART: filename = "Heart.gif"; break;
            case HEART_CONTAINER: filename = "Heart Container.gif"; break;
            case RUPEE_GREEN: filename = "Rupy.gif"; break;
            case RUPEE_BLUE: filename = "Rupy.gif"; break;
            case RUPEE_RED: filename = "Rupy.gif"; break;
            case KEY: filename = "Key.gif"; break;
            case BOMB: filename = "Bomb.gif"; break;
            case CLOCK: filename = "Clock.gif"; break;
            case SWORD: filename = "Wooden Sword (Up).gif"; break;
            case BOOMERANG: filename = "Boomerang.gif"; break;
        }
        
        try {
            File file = new File(basePath + filename);
            if (file.exists()) {
                sprite = ImageIO.read(file);
                width = sprite.getWidth();
                height = sprite.getHeight();
            }
        } catch (Exception e) {}
    }
    
    public void update() {
        lifeTimer--;
        if (lifeTimer <= 0) {
            active = false;
        }
        
        if (lifeTimer < 120) {
            blinkTimer++;
        }
    }
    
    public void applyToPlayer(ZeldaPlayer player) {
        switch (type) {
            case HEART:
                player.heal(type.healAmount);
                break;
            case HEART_CONTAINER:
                player.addHeartContainer();
                break;
            case RUPEE_GREEN:
            case RUPEE_BLUE:
            case RUPEE_RED:
                player.addRupees(type.rupeeAmount);
                break;
            case KEY:
                player.addKeys(type.keyAmount);
                break;
            case BOMB:
                player.addBombs(type.bombAmount);
                break;
            case SWORD:
                player.setSword(true);
                break;
            case BOOMERANG:
                player.setBoomerang(true);
                break;
            case CLOCK:
                break;
        }
        active = false;
    }
    
    public void render(Graphics2D g2) {
        if (!active) return;
        if (blinkTimer > 0 && (blinkTimer / 4) % 2 == 0) return;
        
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, null);
        } else {
            Color color = getItemColor();
            g2.setColor(color);
            
            switch (type) {
                case HEART:
                case HEART_CONTAINER:
                    g2.fillOval((int)x, (int)y, width, height);
                    break;
                case RUPEE_GREEN:
                case RUPEE_BLUE:
                case RUPEE_RED:
                    int[] xPoints = {(int)x + 4, (int)x, (int)x + 4, (int)x + 8};
                    int[] yPoints = {(int)y, (int)y + 6, (int)y + 12, (int)y + 6};
                    g2.fillPolygon(xPoints, yPoints, 4);
                    break;
                case KEY:
                    g2.fillRect((int)x, (int)y, 6, 12);
                    break;
                case BOMB:
                    g2.fillOval((int)x, (int)y + 4, 8, 8);
                    g2.setColor(Color.ORANGE);
                    g2.fillRect((int)x + 3, (int)y, 2, 4);
                    break;
                default:
                    g2.fillRect((int)x, (int)y, width, height);
            }
        }
    }
    
    private Color getItemColor() {
        switch (type) {
            case HEART:
            case HEART_CONTAINER:
            case RUPEE_RED:
                return Color.RED;
            case RUPEE_GREEN:
                return new Color(0, 200, 0);
            case RUPEE_BLUE:
                return new Color(50, 100, 255);
            case KEY:
                return new Color(255, 215, 0);
            case BOMB:
                return Color.DARK_GRAY;
            case CLOCK:
                return Color.YELLOW;
            default:
                return Color.WHITE;
        }
    }
    
    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    
    public boolean isActive() { return active; }
    public ItemType getType() { return type; }
}
