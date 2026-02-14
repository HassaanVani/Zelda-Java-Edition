package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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

    private static final Color CAVE_BG = new Color(52, 28, 0);
    private static final Color CAVE_WALL_OUTER = new Color(146, 146, 146);
    private static final Color CAVE_WALL_INNER = new Color(100, 100, 100);
    private static final Color FIRE_COLOR1 = new Color(252, 152, 56);
    private static final Color FIRE_COLOR2 = new Color(252, 216, 108);

    private int frameCounter = 0;

    private boolean active = false;
    private boolean itemTaken = false;
    private int selectedSlot = -1;  // which item slot player chose (-1 = none)
    private CaveData.CaveDef currentCave = null;
    private BufferedImage oldManSprite;

    public Cave() {
        oldManSprite = loadImage("sprites/NPCs/Old Man.gif");
    }

    private BufferedImage loadImage(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception e) {}
        return null;
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

        // Background
        g2.setColor(CAVE_BG);
        g2.fillRect(0, 0, CAVE_WIDTH, CAVE_HEIGHT);

        // NES-style cave walls with arch shape
        g2.setColor(CAVE_WALL_OUTER);
        // Top wall
        g2.fillRect(0, 0, CAVE_WIDTH, 32);
        // Side walls
        g2.fillRect(0, 0, 32, CAVE_HEIGHT);
        g2.fillRect(CAVE_WIDTH - 32, 0, 32, CAVE_HEIGHT);
        // Inner wall detail
        g2.setColor(CAVE_WALL_INNER);
        g2.fillRect(32, 0, CAVE_WIDTH - 64, 24);
        g2.fillRect(8, 32, 16, CAVE_HEIGHT - 32);
        g2.fillRect(CAVE_WIDTH - 24, 32, 16, CAVE_HEIGHT - 32);

        // Cave arch opening (darker inset)
        g2.setColor(CAVE_BG);
        g2.fillRect(32, 24, CAVE_WIDTH - 64, CAVE_HEIGHT - 24);

        // Floor area
        g2.setColor(new Color(72, 40, 8));
        g2.fillRect(32, CAVE_HEIGHT - 48, CAVE_WIDTH - 64, 48);

        // Exit opening at bottom center
        g2.setColor(Color.BLACK);
        g2.fillRect(CAVE_WIDTH / 2 - 16, CAVE_HEIGHT - 8, 32, 8);

        // Fire torches with flickering animation
        boolean flicker = (frameCounter / 6) % 2 == 0;
        Color fireC = flicker ? FIRE_COLOR1 : FIRE_COLOR2;
        g2.setColor(fireC);
        g2.fillRect(72, 60, 8, 8);
        g2.fillRect(176, 60, 8, 8);
        // Torch base
        g2.setColor(CAVE_WALL_INNER);
        g2.fillRect(73, 68, 6, 4);
        g2.fillRect(177, 68, 6, 4);

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
        Color c;
        switch (itemName) {
            case "WOODEN_SWORD": case "WHITE_SWORD": case "MAGICAL_SWORD":
                c = new Color(160, 120, 60); break;
            case "BLUE_RING": c = new Color(60, 60, 220); break;
            case "RED_RING":  c = Color.RED; break;
            case "BLUE_CANDLE": case "RED_CANDLE": c = FIRE_COLOR1; break;
            case "ARROW": case "SILVER_ARROW": c = new Color(200, 200, 200); break;
            case "BOMB": c = new Color(80, 80, 80); break;
            case "KEY": case "MAGIC_KEY": c = new Color(220, 180, 60); break;
            case "SHIELD": c = new Color(100, 100, 200); break;
            case "FOOD": c = new Color(200, 60, 60); break;
            case "LETTER": c = new Color(220, 220, 200); break;
            case "HEART_CONTAINER": c = Color.RED; break;
            case "BLUE_POTION": c = new Color(60, 60, 220); break;
            case "RED_POTION": case "SECOND_POTION": c = new Color(220, 60, 60); break;
            default: c = new Color(200, 200, 60); break;
        }
        g2.setColor(c);
        g2.fillRect(ix, iy, ITEM_W, ITEM_H);
        // Item name label
        g2.setColor(Color.WHITE);
        String label = itemName.length() > 6 ? itemName.substring(0, 6) : itemName;
        g2.drawString(label, ix - 4, iy - 4);
    }

    public boolean isActive() { return active; }
    public boolean isSwordTaken() {
        if (currentCave == null) return false;
        return itemTaken && currentCave.itemNames.length > 0 &&
            (currentCave.itemNames[0].endsWith("SWORD"));
    }
}
