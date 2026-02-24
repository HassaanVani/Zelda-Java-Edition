package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Item {
    public enum ItemType {
        HEART(2, 0, 0, 0),
        HEART_CONTAINER(0, 0, 0, 0),
        RUPEE(0, 1, 0, 0),
        FIVE_RUPEES(0, 5, 0, 0),
        KEY(0, 0, 1, 0),
        BOMB(0, 0, 0, 1),
        FAIRY(6, 0, 0, 0),
        BOMB_DROP(0, 0, 0, 4),
        CLOCK(0, 0, 0, 0),
        SWORD(0, 0, 0, 0),
        BOOMERANG(0, 0, 0, 0),
        MAP(0, 0, 0, 0),
        COMPASS(0, 0, 0, 0),
        TRIFORCE(0, 0, 0, 0),
        BOSS_KEY(0, 0, 0, 0),
        BOW(0, 0, 0, 0),
        MAGICAL_BOOMERANG(0, 0, 0, 0),
        RAFT(0, 0, 0, 0),
        STEPLADDER(0, 0, 0, 0),
        RECORDER(0, 0, 0, 0),
        MAGICAL_ROD(0, 0, 0, 0),
        RED_CANDLE(0, 0, 0, 0),
        MAGICAL_KEY(0, 0, 0, 0),
        SILVER_ARROW(0, 0, 0, 0),
        BOOK(0, 0, 0, 0),
        ZELDA(0, 0, 0, 0);

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
            case FAIRY: basePath = "sprites/NPCs/"; filename = "Fairy.gif"; break;
            case BOMB_DROP: filename = "Bomb.gif"; break;
            case CLOCK: filename = "Clock.gif"; break;
            case SWORD: filename = "Wooden Sword (Up).gif"; break;
            case BOOMERANG: filename = "Boomerang.gif"; break;
            case MAP: filename = "Map.gif"; break;
            case COMPASS: filename = "Compass.gif"; break;
            case TRIFORCE: filename = "Triforce1.gif"; break;
            case BOSS_KEY: filename = "Key.gif"; break;
            case BOW: filename = "Bow.gif"; break;
            case MAGICAL_BOOMERANG: filename = "Magical Boomerang.gif"; break;
            case RAFT: filename = "Raft1.gif"; break;
            case STEPLADDER: filename = "Stepladder.gif"; break;
            case RECORDER: filename = "Recorder.gif"; break;
            case MAGICAL_ROD: filename = "Magical Rod.gif"; break;
            case RED_CANDLE: filename = "Red Candle.gif"; break;
            case MAGICAL_KEY: filename = "Magical Key.gif"; break;
            case SILVER_ARROW: filename = "Silver Arrow (Up).gif"; break;
            case BOOK: filename = "Book of Magic.gif"; break;
            case ZELDA: basePath = "sprites/NPCs/"; filename = "Princess Zelda.gif"; break;
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
        Inventory inv = player.getInventory();
        switch (type) {
            case HEART:
                player.heal(type.healAmount);
                break;
            case HEART_CONTAINER:
                inv.addHeartContainer();
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
            case FAIRY:
                player.heal(type.healAmount);
                break;
            case BOMB_DROP:
                player.addBombs(type.bombAmount);
                break;
            case SWORD:
                player.setHasSword(true);
                break;
            case BOOMERANG:
                player.setHasBoomerang(true);
                break;
            case MAP:
                // Dungeon map — handled by dungeon-level code
                break;
            case COMPASS:
                // Dungeon compass — handled by dungeon-level code
                break;
            case TRIFORCE:
                // Triforce shard — handled by dungeon-level code
                break;
            case CLOCK:
                inv.setFreezeTimer(300); // ~5 seconds at 60fps
                break;
            case BOW:
                inv.setHasBow(true);
                if (inv.getArrowLevel() == 0) inv.setArrowLevel(1);
                break;
            case MAGICAL_BOOMERANG:
                inv.setBoomerangLevel(2);
                break;
            case RAFT:
                inv.setHasRaft(true);
                break;
            case STEPLADDER:
                inv.setHasLadder(true);
                break;
            case RECORDER:
                inv.setHasRecorder(true);
                break;
            case MAGICAL_ROD:
                inv.setHasRod(true);
                break;
            case RED_CANDLE:
                inv.setCandleLevel(2);
                break;
            case MAGICAL_KEY:
                inv.setHasMagicKey(true);
                break;
            case SILVER_ARROW:
                inv.setArrowLevel(2);
                if (!inv.hasBow()) inv.setHasBow(true);
                break;
            case BOOK:
                inv.setHasBook(true);
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

        int drawX = (int)x;
        int drawY = (int)y;

        // Important items get a palette-cycling flash effect (NES-style)
        boolean isSpecial = isSpecialItem();
        if (isSpecial && sprite != null) {
            int flashCycle = (lifeTimer / 8) % 4;
            if (flashCycle == 0) {
                // Normal render
                g2.drawImage(sprite, drawX, drawY, null);
            } else {
                // Flash with tinted overlay
                g2.drawImage(sprite, drawX, drawY, null);
                Color flashColor;
                switch (flashCycle) {
                    case 1: flashColor = new Color(255, 255, 255, 80); break;
                    case 2: flashColor = new Color(255, 200, 60, 60); break;
                    case 3: flashColor = new Color(200, 255, 200, 60); break;
                    default: flashColor = new Color(255, 255, 255, 40); break;
                }
                g2.setColor(flashColor);
                g2.fillRect(drawX, drawY, width, height);
            }
        } else if (sprite != null) {
            g2.drawImage(sprite, drawX, drawY, null);
        } else {
            g2.setColor(getItemColor());
            g2.fillRect(drawX, drawY, width, height);
        }
    }

    private boolean isSpecialItem() {
        switch (type) {
            case TRIFORCE: case HEART_CONTAINER: case BOW: case MAGICAL_BOOMERANG:
            case RAFT: case STEPLADDER: case RECORDER: case MAGICAL_ROD:
            case RED_CANDLE: case MAGICAL_KEY: case SILVER_ARROW: case BOSS_KEY:
                return true;
            default: return false;
        }
    }

    private Color getItemColor() {
        switch (type) {
            case HEART: return Color.RED;
            case RUPEE: return new Color(0, 200, 0);
            case FIVE_RUPEES: return new Color(50, 100, 255);
            case KEY: return new Color(255, 215, 0);
            case BOMB:
            case BOMB_DROP: return Color.DARK_GRAY;
            case FAIRY: return new Color(255, 180, 200);
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
