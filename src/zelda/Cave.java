package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class Cave {
    public static final int CAVE_WIDTH = 256;
    public static final int CAVE_HEIGHT = 176;

    public static final int NPC_X = 104;
    public static final int NPC_Y = 56;
    public static final int NPC_W = 16;
    public static final int NPC_H = 16;

    public static final int TEXT_X = 32;
    public static final int TEXT_Y = 40;

    public static final double PLAYER_SPAWN_X = 120;
    public static final double PLAYER_SPAWN_Y = 144;
    public static final int EXIT_THRESHOLD_Y = 168;

    // NES-accurate item positions: X=$58=88, $78=120, $98=152
    private static final int[] ITEM_X = {88, 120, 152};
    private static final int ITEM_Y = 88;
    private static final int ITEM_W = 12;
    private static final int ITEM_H = 16;

    // NES cave palette: $0F=black background, $30=white walls, $00=dark gray, $12=blue accent
    private static final Color CAVE_BG = Color.BLACK;
    private static final Color CAVE_WALL = new Color(0x62, 0x62, 0x62); // NES $00 dark gray
    private static final Color CAVE_FLOOR = new Color(0x72, 0x40, 0x00); // dark brown floor strip
    private static final Color FIRE_COLOR1 = new Color(252, 152, 56);
    private static final Color FIRE_COLOR2 = new Color(252, 216, 108);

    private int frameCounter = 0;

    private boolean active = false;
    private boolean itemTaken = false;
    private int selectedSlot = -1;  // which item slot player chose (-1 = none)
    private CaveData.CaveDef currentCave = null;
    private BufferedImage oldManSprite;

    // Item sprite cache for cave shop/reward rendering
    private static final HashMap<String, BufferedImage> itemSpriteCache = new HashMap<>();
    private static boolean itemSpritesLoaded = false;

    public Cave() {
        oldManSprite = loadImage("sprites/NPCs/Old Man.gif");
        loadItemSprites();
    }

    private BufferedImage loadImage(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception e) {}
        return null;
    }

    private static void loadItemSprites() {
        if (itemSpritesLoaded) return;
        itemSpritesLoaded = true;
        String b = "sprites/Objects/";
        cacheItem("WOODEN_SWORD", b + "Wooden Sword (Up).gif");
        cacheItem("WHITE_SWORD", b + "White Sword (Up).gif");
        cacheItem("MAGICAL_SWORD", b + "Magical Sword (Up).gif");
        cacheItem("BLUE_RING", b + "Blue Ring.gif");
        cacheItem("RED_RING", b + "Red Ring.gif");
        cacheItem("BLUE_CANDLE", b + "Blue Candle.gif");
        cacheItem("RED_CANDLE", b + "Red Candle.gif");
        cacheItem("ARROW", b + "Arrow (Up).gif");
        cacheItem("SILVER_ARROW", b + "Silver Arrow (Up).gif");
        cacheItem("BOW", b + "Bow.gif");
        cacheItem("BOOMERANG", b + "Boomerang.gif");
        cacheItem("MAGICAL_BOOMERANG", b + "Magical Boomerang.gif");
        cacheItem("BOMB", b + "Bomb.gif");
        cacheItem("KEY", b + "Key.gif");
        cacheItem("MAGIC_KEY", b + "Magical Key.gif");
        cacheItem("SHIELD", b + "Magical Shield.gif");
        cacheItem("FOOD", b + "Food.gif");
        cacheItem("LETTER", b + "Letter.gif");
        cacheItem("RECORDER", b + "Recorder.gif");
        cacheItem("RAFT", b + "Raft1.gif");
        cacheItem("LADDER", b + "Stepladder.gif");
        cacheItem("BRACELET", b + "Power Bracelet.gif");
        cacheItem("ROD", b + "Magical Rod.gif");
        cacheItem("BOOK", b + "Book of Magic.gif");
        cacheItem("HEART_CONTAINER", b + "Heart Container.gif");
        cacheItem("BLUE_POTION", b + "Life Potion.gif");
        cacheItem("RED_POTION", b + "Life Potion.gif");
        cacheItem("SECOND_POTION", b + "2nd Potion.gif");
    }

    private static void cacheItem(String key, String path) {
        try {
            File f = new File(path);
            if (f.exists()) itemSpriteCache.put(key, ImageIO.read(f));
        } catch (Exception e) {}
    }

    public void enter(ZeldaPlayer player, int roomX, int roomY) {
        active = true;
        itemTaken = false;
        selectedSlot = -1;
        currentCave = CaveData.getCave(roomX, roomY);
        player.setPosition(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
    }

    public void enter(ZeldaPlayer player) {
        enter(player, 7, 7);
    }

    /** Enter a specific cave by its numeric ID (for revealed secrets). */
    public void enterById(ZeldaPlayer player, int caveId) {
        active = true;
        itemTaken = false;
        selectedSlot = -1;
        currentCave = CaveData.getCaveById(caveId);
        player.setPosition(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
    }

    public boolean update(ZeldaPlayer player) {
        if (!active) return false;

        if (currentCave != null && !itemTaken && currentCave.itemNames.length > 0) {
            Rectangle playerBox = player.getHitbox();
            for (int i = 0; i < currentCave.itemNames.length; i++) {
                Rectangle itemBox = new Rectangle(ITEM_X[i], ITEM_Y, ITEM_W, ITEM_H);
                if (playerBox.intersects(itemBox)) {
                    if (tryTakeItem(player, i)) {
                        selectedSlot = i;
                        itemTaken = true;
                    }
                    break;
                }
            }
        }

        if (player.getWorldY() >= EXIT_THRESHOLD_Y) {
            active = false;
            return true;
        }
        return false;
    }

    private boolean tryTakeItem(ZeldaPlayer player, int slot) {
        if (currentCave == null || slot >= currentCave.itemNames.length) return false;

        Inventory inv = player.getInventory();
        String itemName = currentCave.itemNames[slot];
        int cost = currentCave.itemCosts[slot];

        // Check heart requirement
        if (currentCave.requiredHearts > 0 && inv.getHeartContainers() < currentCave.requiredHearts) {
            return false;
        }

        // Check cost
        if (cost > 0 && !inv.spendRupees(cost)) {
            return false;
        }

        // Grant item
        return grantItem(inv, itemName);
    }

    private boolean grantItem(Inventory inv, String itemName) {
        switch (itemName) {
            case "WOODEN_SWORD":   inv.setSwordLevel(Math.max(inv.getSwordLevel(), 1)); return true;
            case "WHITE_SWORD":    inv.setSwordLevel(Math.max(inv.getSwordLevel(), 2)); return true;
            case "MAGICAL_SWORD":  inv.setSwordLevel(3); return true;
            case "BLUE_RING":      inv.setRingLevel(Math.max(inv.getRingLevel(), 1)); return true;
            case "RED_RING":       inv.setRingLevel(2); return true;
            case "BLUE_CANDLE":    inv.setCandleLevel(Math.max(inv.getCandleLevel(), 1)); return true;
            case "RED_CANDLE":     inv.setCandleLevel(2); return true;
            case "ARROW":          inv.setArrowLevel(Math.max(inv.getArrowLevel(), 1)); return true;
            case "SILVER_ARROW":   inv.setArrowLevel(2); return true;
            case "BOW":            inv.setHasBow(true); return true;
            case "BOOMERANG":      inv.setBoomerangLevel(Math.max(inv.getBoomerangLevel(), 1)); return true;
            case "MAGICAL_BOOMERANG": inv.setBoomerangLevel(2); return true;
            case "BOMB":           inv.addBombs(4); return true;
            case "KEY":            inv.addKeys(1); return true;
            case "SHIELD":         inv.setShieldLevel(Math.max(inv.getShieldLevel(), 2)); return true;
            case "FOOD":           inv.setHasFood(true); return true;
            case "LETTER":         inv.setLetterState(Math.max(inv.getLetterState(), 1)); return true;
            case "RECORDER":       inv.setHasRecorder(true); return true;
            case "RAFT":           inv.setHasRaft(true); return true;
            case "LADDER":         inv.setHasLadder(true); return true;
            case "BRACELET":       inv.setHasBracelet(true); return true;
            case "MAGIC_KEY":      inv.setHasMagicKey(true); return true;
            case "ROD":            inv.setHasRod(true); return true;
            case "BOOK":           inv.setHasBook(true); return true;
            case "HEART_CONTAINER": inv.addHeartContainer(); return true;
            case "BLUE_POTION":    inv.setPotionCount(1); return true;
            case "RED_POTION":
            case "SECOND_POTION":  inv.setPotionCount(2); return true;
            default:
                // Money game entries like "-10", "-40", "50"
                try {
                    int amount = Integer.parseInt(itemName);
                    inv.addRupees(amount);
                    return true;
                } catch (NumberFormatException e) {
                    System.err.println("[Cave] Unknown item: " + itemName);
                    return false;
                }
        }
    }

    public void render(Graphics2D g2) {
        frameCounter++;

        // Black void background — NES caves use $0F (black) as BG
        g2.setColor(CAVE_BG);
        g2.fillRect(0, 0, CAVE_WIDTH, CAVE_HEIGHT);

        // NES cave: thick gray block walls forming a rectangular room
        // Wall structure matches NES tile layout (~32px thick borders)
        int wallL = 32;                  // left wall X
        int wallR = CAVE_WIDTH - 32;     // right wall X end
        int wallT = 16;                  // top wall Y
        int wallB = 160;                 // bottom wall Y end
        int thick = 32;                  // wall thickness
        int exitGap = 32;               // exit opening width

        // Draw the full rectangular wall frame (dark gray NES $00)
        g2.setColor(CAVE_WALL);
        g2.fillRect(wallL, wallT, wallR - wallL, thick);              // top wall
        g2.fillRect(wallL, wallT, thick, wallB - wallT);              // left wall
        g2.fillRect(wallR - thick, wallT, thick, wallB - wallT);      // right wall
        // Bottom wall with exit gap in center
        int midX = CAVE_WIDTH / 2;
        g2.fillRect(wallL, wallB - thick, midX - exitGap / 2 - wallL, thick); // bottom-left
        g2.fillRect(midX + exitGap / 2, wallB - thick, wallR - midX - exitGap / 2, thick); // bottom-right

        // Floor area inside the walls (brown)
        int floorY = wallB - thick - 32;
        g2.setColor(CAVE_FLOOR);
        g2.fillRect(wallL + thick, floorY, wallR - wallL - thick * 2, 32);

        // Fire torches with flickering animation (flanking the Old Man)
        boolean flicker = (frameCounter / 6) % 2 == 0;
        Color fireC = flicker ? FIRE_COLOR1 : FIRE_COLOR2;
        g2.setColor(fireC);
        g2.fillRect(72, 56, 8, 8);
        g2.fillRect(176, 56, 8, 8);
        // Torch base
        g2.setColor(CAVE_WALL);
        g2.fillRect(73, 64, 6, 4);
        g2.fillRect(177, 64, 6, 4);

        // NPC
        if (oldManSprite != null) {
            g2.drawImage(oldManSprite, NPC_X, NPC_Y, NPC_W, NPC_H, null);
        } else {
            g2.setColor(new Color(200, 120, 60));
            g2.fillRect(NPC_X, NPC_Y, NPC_W, NPC_H);
        }

        // Text
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.setColor(Color.WHITE);
        if (currentCave != null) {
            g2.drawString(currentCave.npcText, TEXT_X, TEXT_Y);
            if (currentCave.npcText2 != null && !currentCave.npcText2.isEmpty()) {
                g2.drawString(currentCave.npcText2, TEXT_X, TEXT_Y + 14);
            }
        }

        // Items
        if (currentCave != null && currentCave.itemNames.length > 0) {
            for (int i = 0; i < currentCave.itemNames.length; i++) {
                if (itemTaken && i == selectedSlot) continue; // hide taken item
                int ix = ITEM_X[i];
                renderItemSlot(g2, currentCave.itemNames[i], ix, ITEM_Y);

                // Price label
                if (currentCave.itemCosts[i] > 0) {
                    g2.setColor(Color.WHITE);
                    g2.drawString(String.valueOf(currentCave.itemCosts[i]),
                        ix, ITEM_Y + ITEM_H + 10);
                }
            }
        }
    }

    private void renderItemSlot(Graphics2D g2, String itemName, int ix, int iy) {
        BufferedImage spr = itemSpriteCache.get(itemName);
        if (spr != null) {
            int dx = ix + (ITEM_W - spr.getWidth()) / 2;
            int dy = iy + (ITEM_H - spr.getHeight()) / 2;
            g2.drawImage(spr, dx, dy, null);
        } else {
            // Fallback for money game entries and unknown items
            g2.setColor(new Color(200, 200, 60));
            g2.fillRect(ix, iy, ITEM_W, ITEM_H);
        }
    }

    public boolean isActive() { return active; }
    public boolean isSwordTaken() {
        if (currentCave == null) return false;
        return itemTaken && currentCave.itemNames.length > 0 &&
            (currentCave.itemNames[0].endsWith("SWORD"));
    }
}
