package zelda;

import java.awt.*;
import java.io.File;
import javax.swing.ImageIcon;

public class ZeldaHUD {
    private ZeldaPlayer player;
    private boolean inDungeon = false;
    private int dungeonLevel = 0;

    private Image heartFull, heartHalf, heartEmpty;
    private Image rupeeIcon, keyIcon, bombIcon;
    private Image swordIcon, whiteSwordIcon, magicalSwordIcon;
    private Image boomerangIcon, bowIcon, candleIcon, recorderIcon, foodIcon, potionIcon, rodIcon;

    private static final int HUD_HEIGHT = 56;

    private static final int MINIMAP_X = 16;
    private static final int MINIMAP_Y = 18;
    private static final int MINIMAP_W = 64;
    private static final int MINIMAP_H = 32;

    private static final int ITEM_BOX_X = 128;
    private static final int ITEM_BOX_Y = 16;
    private static final int ITEM_BOX_W = 16;
    private static final int ITEM_BOX_H = 16;
    private static final int ITEM_BOX_GAP = 24;

    private static final int RUPEE_X = 96;
    private static final int RUPEE_Y = 16;
    private static final int KEY_X = 96;
    private static final int KEY_Y = 32;
    private static final int BOMB_X = 96;
    private static final int BOMB_Y = 42;

    private static final int LIFE_X = 176;
    private static final int LIFE_Y = 8;
    private static final int HEART_SIZE = 8;
    private static final int HEARTS_PER_ROW = 8;
    private static final int HEART_GAP = 1;

    private static final Color HUD_BG = Color.BLACK;
    private static final Color HUD_TEXT = Color.WHITE;
    private static final Color LIFE_COLOR = new Color(200, 72, 72);
    private static final Color MAP_BG = new Color(80, 80, 80);
    private static final Color MAP_BORDER = new Color(116, 116, 116);
    private static final Color MAP_DOT = new Color(0, 200, 0);
    private static final Color MAP_DOT_ALT = new Color(0, 128, 0);

    private int blinkTimer = 0;

    public ZeldaHUD() {
        loadSprites();
    }

    private void loadSprites() {
        String p = "sprites/Objects/";
        heartFull = loadGif(p + "Life1.gif");
        heartHalf = loadGif(p + "Life2.gif");
        heartEmpty = loadGif(p + "Life3.gif");
        rupeeIcon = loadGif(p + "Rupy.gif");
        keyIcon = loadGif(p + "Key.gif");
        bombIcon = loadGif(p + "Bomb.gif");
        swordIcon = loadGif(p + "Wooden Sword (Up).gif");
        whiteSwordIcon = loadGif(p + "White Sword (Up).gif");
        magicalSwordIcon = loadGif(p + "Magical Sword (Up).gif");
        boomerangIcon = loadGif(p + "Boomerang.gif");
        bowIcon = loadGif(p + "Bow.gif");
        candleIcon = loadGif(p + "Blue Candle.gif");
        recorderIcon = loadGif(p + "Recorder.gif");
        foodIcon = loadGif(p + "Food.gif");
        potionIcon = loadGif(p + "Potion (Blue).gif");
        rodIcon = loadGif(p + "Magical Rod.gif");
    }

    private Image loadGif(String path) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(path).getImage();
        return null;
    }

    public void setPlayer(ZeldaPlayer p) { this.player = p; }
    public void setInDungeon(boolean d, int level) { this.inDungeon = d; this.dungeonLevel = level; }

    public void render(Graphics2D g2, ZeldaRoom room) {
        blinkTimer++;

        g2.setColor(HUD_BG);
        g2.fillRect(0, 0, 256, HUD_HEIGHT);

        renderMinimap(g2, room);
        renderInventory(g2);
        renderLife(g2);
    }

    private void renderMinimap(Graphics2D g2, ZeldaRoom room) {
        g2.setColor(MAP_BG);
        g2.fillRect(MINIMAP_X, MINIMAP_Y, MINIMAP_W, MINIMAP_H);
        g2.setColor(MAP_BORDER);
        g2.drawRect(MINIMAP_X - 1, MINIMAP_Y - 1, MINIMAP_W + 1, MINIMAP_H + 1);

        if (room != null) {
            int cellW = MINIMAP_W / OverworldRenderer.MAP_COLS;
            int cellH = MINIMAP_H / OverworldRenderer.MAP_ROWS;
            int dx = MINIMAP_X + room.getRoomX() * cellW;
            int dy = MINIMAP_Y + room.getRoomY() * cellH;

            Color dotColor = (blinkTimer / 10) % 2 == 0 ? MAP_DOT : MAP_DOT_ALT;
            g2.setColor(dotColor);
            g2.fillRect(dx, dy, Math.max(cellW, 3), Math.max(cellH, 3));
        }

        if (inDungeon) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 7));
            g2.setColor(HUD_TEXT);
            g2.drawString("LEVEL-" + dungeonLevel, MINIMAP_X, MINIMAP_Y - 4);
        }
    }

    private void renderInventory(Graphics2D g2) {
        if (player == null) return;
        Inventory inv = player.getInventory();

        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));

        g2.setColor(HUD_TEXT);
        g2.drawString("B", ITEM_BOX_X - 8, ITEM_BOX_Y - 2);
        g2.drawString("A", ITEM_BOX_X + ITEM_BOX_GAP - 8, ITEM_BOX_Y - 2);

        g2.setColor(MAP_BORDER);
        g2.drawRect(ITEM_BOX_X - 10, ITEM_BOX_Y, ITEM_BOX_W, ITEM_BOX_H);
        g2.drawRect(ITEM_BOX_X + ITEM_BOX_GAP - 10, ITEM_BOX_Y, ITEM_BOX_W, ITEM_BOX_H);

        // B-item: draw the currently selected B-item
        Image bItemIcon = getBItemIcon(inv.getSelectedBItem());
        if (bItemIcon != null) {
            g2.drawImage(bItemIcon, ITEM_BOX_X - 8, ITEM_BOX_Y + 2, 12, 12, null);
        }

        // A-item: draw sword based on level
        Image currentSword = getSwordIcon(inv.getSwordLevel());
        if (currentSword != null) {
            g2.drawImage(currentSword, ITEM_BOX_X + ITEM_BOX_GAP - 8, ITEM_BOX_Y + 2, 12, 12, null);
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g2.setColor(HUD_TEXT);

        if (rupeeIcon != null) g2.drawImage(rupeeIcon, RUPEE_X, RUPEE_Y, 8, 8, null);
        g2.drawString("X" + String.format("%03d", player.getRupees()), RUPEE_X + 10, RUPEE_Y + 8);

        if (keyIcon != null) g2.drawImage(keyIcon, KEY_X, KEY_Y, 6, 10, null);
        g2.drawString("X" + String.format("%02d", player.getKeys()), KEY_X + 10, KEY_Y + 8);

        if (bombIcon != null) g2.drawImage(bombIcon, BOMB_X, BOMB_Y, 8, 8, null);
        g2.drawString("X" + String.format("%02d", player.getBombs()), BOMB_X + 10, BOMB_Y + 8);
    }

    private Image getBItemIcon(Inventory.BItem bItem) {
        switch (bItem) {
            case BOOMERANG: return boomerangIcon;
            case BOMB:      return bombIcon;
            case BOW:       return bowIcon;
            case CANDLE:    return candleIcon;
            case RECORDER:  return recorderIcon;
            case FOOD:      return foodIcon;
            case POTION:    return potionIcon;
            case ROD:       return rodIcon;
            default:        return null;
        }
    }

    private Image getSwordIcon(int level) {
        switch (level) {
            case 1:  return swordIcon;
            case 2:  return whiteSwordIcon != null ? whiteSwordIcon : swordIcon;
            case 3:  return magicalSwordIcon != null ? magicalSwordIcon : swordIcon;
            default: return null;
        }
    }

    private void renderLife(Graphics2D g2) {
        if (player == null) return;

        g2.setColor(LIFE_COLOR);
        g2.setFont(new Font("Monospaced", Font.BOLD, 7));
        g2.drawString("-LIFE-", LIFE_X + 16, LIFE_Y + 6);

        int heartsTotal = player.getMaxHealth() / 2;
        int heartsFull = player.getHealth() / 2;
        boolean hasHalf = player.getHealth() % 2 == 1;

        for (int i = 0; i < heartsTotal; i++) {
            int hx = LIFE_X + (i % HEARTS_PER_ROW) * (HEART_SIZE + HEART_GAP);
            int hy = LIFE_Y + 12 + (i / HEARTS_PER_ROW) * (HEART_SIZE + 2);

            Image img;
            if (i < heartsFull) img = heartFull;
            else if (i == heartsFull && hasHalf) img = heartHalf;
            else img = heartEmpty;

            if (img != null) {
                g2.drawImage(img, hx, hy, HEART_SIZE, HEART_SIZE, null);
            } else {
                g2.setColor(i < heartsFull ? Color.RED : Color.DARK_GRAY);
                g2.fillRect(hx, hy, HEART_SIZE, HEART_SIZE);
            }
        }
    }
}
