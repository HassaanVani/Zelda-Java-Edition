package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Item {
    public enum ItemType {
        HEART(2, 0, 0, 0),
        HEART_CONTAINER(0, 0, 0, 0),
        RUPEE(0, 1, 0, 0),
        FIVE_RUPEES(0, 5, 0, 0),
        KEY(0, 0, 1, 0),
        BOMB(0, 0, 0, 1),
        CLOCK(0, 0, 0, 0),
        SWORD(0, 0, 0, 0),
        BOOMERANG(0, 0, 0, 0),
        MAP(0, 0, 0, 0),
        COMPASS(0, 0, 0, 0),
        TRIFORCE(0, 0, 0, 0);

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
            case RUPEE: filename = "Rupy.gif"; break;
            case FIVE_RUPEES: filename = "Rupy.gif"; break;
            case KEY: filename = "Key.gif"; break;
            case BOMB: filename = "Bomb.gif"; break;
            case CLOCK: filename = "Clock.gif"; break;
            case SWORD: filename = "Wooden Sword (Up).gif"; break;
            case BOOMERANG: filename = "Boomerang.gif"; break;
            case MAP: filename = "Map.gif"; break;
            case COMPASS: filename = "Compass.gif"; break;
            case TRIFORCE: filename = "Triforce Shard.gif"; break;
        }

        try {
            File file = new File(basePath + filename);
            if (file.exists()) {
                sprite = ImageIO.read(file);
                width = Math.max(sprite.getWidth(), 8);
                height = Math.max(sprite.getHeight(), 8);
            }
        } catch (Exception e) {}
    }

    public void update() {
        lifeTimer--;
        if (lifeTimer <= 0) active = false;
        if (lifeTimer < 120) blinkTimer++;
    }

    public void applyEffect(ZeldaPlayer player) {
        switch (type) {
            case HEART:
                player.heal(type.healAmount);
                break;
            case RUPEE:
            case FIVE_RUPEES:
                player.addRupees(type.rupeeAmount);
                break;
            case KEY:
                player.addKeys(type.keyAmount);
                break;
            case BOMB:
                player.addBombs(type.bombAmount);
                break;
            case SWORD:
                player.setHasSword(true);
                break;
            case BOOMERANG:
                player.setHasBoomerang(true);
                break;
            default:
                break;
        }
        active = false;
    }

    public boolean intersects(Rectangle rect) {
        return active && getHitbox().intersects(rect);
    }

    public void render(Graphics2D g2) {
        if (!active) return;
        if (blinkTimer > 0 && (blinkTimer / 4) % 2 == 0) return;

        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, null);
        } else {
            g2.setColor(getItemColor());
            g2.fillRect((int)x, (int)y, width, height);
        }
    }

    private Color getItemColor() {
        switch (type) {
            case HEART: return Color.RED;
            case RUPEE: return new Color(0, 200, 0);
            case FIVE_RUPEES: return new Color(50, 100, 255);
            case KEY: return new Color(255, 215, 0);
            case BOMB: return Color.DARK_GRAY;
            case TRIFORCE: return Color.YELLOW;
            default: return Color.WHITE;
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public boolean isAlive() { return active; }
    public boolean isActive() { return active; }
    public ItemType getType() { return type; }
}
