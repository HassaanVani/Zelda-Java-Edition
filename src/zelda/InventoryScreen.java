package zelda;

import java.awt.*;

/**
 * The pause/inventory subscreen shown when the player presses Start.
 * Displays all collected B-items in a grid, allows cursor-based selection,
 * shows map/compass/triforce pieces for the current dungeon, and
 * displays the use items (raft, ladder, etc.) in the item list.
 */
public class InventoryScreen {
    private static final int SCREEN_W = 256;
    private static final int SCREEN_H = 240;
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color BOX_COLOR = new Color(60, 60, 120);
    private static final Color SELECTED_COLOR = new Color(200, 80, 80);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color LABEL_COLOR = new Color(200, 60, 60);

    // B-item grid layout
    private static final int GRID_X = 128;
    private static final int GRID_Y = 48;
    private static final int CELL_W = 24;
    private static final int CELL_H = 16;
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 2;

    // The B-items in grid order (matches NES layout)
    private static final Inventory.BItem[] GRID_ITEMS = {
        Inventory.BItem.BOOMERANG, Inventory.BItem.BOMB,
        Inventory.BItem.BOW,       Inventory.BItem.CANDLE,
        Inventory.BItem.RECORDER,  Inventory.BItem.FOOD,
        Inventory.BItem.POTION,    Inventory.BItem.ROD
    };

    // Map/compass area
    private static final int MAP_X = 16;
    private static final int MAP_Y = 48;
    private static final int MAP_W = 64;
    private static final int MAP_H = 64;

    // Triforce area
    private static final int TRI_X = 16;
    private static final int TRI_Y = 130;

    // Use items area (right side, below B-grid)
    private static final int USE_X = 128;
    private static final int USE_Y = 96;

    private int cursorCol = 0;
    private int cursorRow = 0;
    private int blinkTimer = 0;

    // Debounce
    private boolean prevUp, prevDown, prevLeft, prevRight, prevAction;

    public InventoryScreen() {}

    public void update(engine.KeyHandler keyHandler, Inventory inv) {
        blinkTimer++;

        boolean up = keyHandler.upPressed;
        boolean down = keyHandler.downPressed;
        boolean left = keyHandler.leftPressed;
        boolean right = keyHandler.rightPressed;
        boolean action = keyHandler.zPressed;

        // Move cursor with debounce
        if (up && !prevUp && cursorRow > 0) cursorRow--;
        if (down && !prevDown && cursorRow < GRID_ROWS - 1) cursorRow++;
        if (left && !prevLeft && cursorCol > 0) cursorCol--;
        if (right && !prevRight && cursorCol < GRID_COLS - 1) cursorCol++;

        // Select B-item on action press
        if (action && !prevAction) {
            int idx = cursorRow * GRID_COLS + cursorCol;
            if (idx < GRID_ITEMS.length) {
                Inventory.BItem item = GRID_ITEMS[idx];
                if (hasItem(inv, item)) {
                    inv.setSelectedBItem(item);
                }
            }
        }

        prevUp = up;
        prevDown = down;
        prevLeft = left;
        prevRight = right;
        prevAction = action;
    }

    private boolean hasItem(Inventory inv, Inventory.BItem item) {
        switch (item) {
            case BOOMERANG: return inv.hasBoomerang();
            case BOMB:      return inv.getBombs() > 0 || inv.getMaxBombs() > 0;
            case BOW:       return inv.hasBow() && inv.hasArrows();
            case CANDLE:    return inv.hasCandle();
            case RECORDER:  return inv.hasRecorder();
            case FOOD:      return inv.hasFood();
            case POTION:    return inv.getPotionCount() > 0;
            case ROD:       return inv.hasRod();
            default:        return false;
        }
    }

    public void render(Graphics2D g2, Inventory inv, int dungeonLevel) {
        // Full black background
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);

        g2.setFont(new Font("Monospaced", Font.BOLD, 8));

        // === B-item selection header ===
        g2.setColor(LABEL_COLOR);
        g2.drawString("USE  B  BUTTON", GRID_X, GRID_Y - 20);
        g2.drawString("FOR THIS", GRID_X + 8, GRID_Y - 10);

        // === B-item grid ===
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int idx = row * GRID_COLS + col;
                int cx = GRID_X + col * CELL_W;
                int cy = GRID_Y + row * CELL_H;

                // Highlight selected item
                Inventory.BItem item = GRID_ITEMS[idx];
                if (item == inv.getSelectedBItem()) {
                    g2.setColor(BOX_COLOR);
                    g2.fillRect(cx - 2, cy - 2, CELL_W, CELL_H);
                }

                // Cursor
                if (row == cursorRow && col == cursorCol) {
                    g2.setColor((blinkTimer / 8) % 2 == 0 ? SELECTED_COLOR : Color.WHITE);
                    g2.drawRect(cx - 3, cy - 3, CELL_W + 1, CELL_H + 1);
                }

                // Draw item if player has it
                if (hasItem(inv, item)) {
                    renderBItemIcon(g2, item, cx + 2, cy);
                }
            }
        }

        // === Inventory items list (use items) ===
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        int uy = USE_Y;
        if (inv.hasRaft())     { g2.drawString("RAFT",    USE_X, uy); uy += 10; }
        if (inv.hasLadder())   { g2.drawString("LADDER",  USE_X, uy); uy += 10; }
        if (inv.hasBracelet()) { g2.drawString("BRACELET",USE_X, uy); uy += 10; }
        if (inv.hasBook())     { g2.drawString("BOOK",    USE_X, uy); uy += 10; }
        if (inv.hasMagicKey()) { g2.drawString("MAGIC KEY",USE_X, uy); uy += 10; }

        // Ring display
        if (inv.getRingLevel() == 1) { g2.setColor(new Color(60,100,220)); g2.drawString("BLUE RING", USE_X + 56, USE_Y); }
        if (inv.getRingLevel() == 2) { g2.setColor(Color.RED); g2.drawString("RED RING", USE_X + 56, USE_Y); }

        // === Map area ===
        g2.setColor(LABEL_COLOR);
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString("MAP", MAP_X + 16, MAP_Y - 6);

        g2.setColor(new Color(40, 40, 80));
        g2.fillRect(MAP_X, MAP_Y, MAP_W, MAP_H);
        g2.setColor(new Color(80, 80, 120));
        g2.drawRect(MAP_X, MAP_Y, MAP_W, MAP_H);

        // Show compass/map icons
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        if (dungeonLevel > 0) {
            g2.setColor(TEXT_COLOR);
            if (inv.hasCompass(dungeonLevel)) g2.drawString("COMPASS", MAP_X, MAP_Y + MAP_H + 10);
            if (inv.hasMap(dungeonLevel))     g2.drawString("MAP", MAP_X + 48, MAP_Y + MAP_H + 10);
        }

        // === Triforce pieces ===
        g2.setColor(LABEL_COLOR);
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString("TRIFORCE", TRI_X + 4, TRI_Y - 4);

        for (int i = 0; i < 8; i++) {
            int tx = TRI_X + (i % 4) * 16;
            int ty = TRI_Y + (i / 4) * 14;
            if (inv.hasTriforce(i + 1)) {
                g2.setColor(new Color(240, 220, 60));
            } else {
                g2.setColor(new Color(40, 40, 40));
            }
            // Draw small triangle for triforce piece
            int[] xp = {tx + 7, tx + 1, tx + 13};
            int[] yp = {ty, ty + 10, ty + 10};
            g2.fillPolygon(xp, yp, 3);
        }
    }

    private void renderBItemIcon(Graphics2D g2, Inventory.BItem item, int ix, int iy) {
        Color c;
        String label;
        switch (item) {
            case BOOMERANG: c = new Color(180, 120, 60); label = "BOOM"; break;
            case BOMB:      c = new Color(80, 80, 80);   label = "BOMB"; break;
            case BOW:       c = new Color(160, 100, 40);  label = "BOW";  break;
            case CANDLE:    c = new Color(60, 60, 220);   label = "CNDL"; break;
            case RECORDER:  c = new Color(120, 200, 120); label = "REC";  break;
            case FOOD:      c = new Color(200, 60, 60);   label = "FOOD"; break;
            case POTION:    c = new Color(60, 60, 220);   label = "POT";  break;
            case ROD:       c = new Color(200, 160, 60);  label = "ROD";  break;
            default:        c = Color.GRAY;               label = "?";    break;
        }
        g2.setColor(c);
        g2.fillRect(ix, iy, 12, 12);
        g2.setColor(TEXT_COLOR);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 6));
        g2.drawString(label, ix - 1, iy + 12 + 6);
    }
}
