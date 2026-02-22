package zelda;

import engine.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TitleScreen {
    private ZeldaGame game;
    private KeyHandler keyHandler;

    private enum ScreenState { INTRO, TITLE, FILE_SELECT, NAME_ENTRY, ELIMINATION }

    private ScreenState state = ScreenState.INTRO;
    private int selectedSlot = 0;
    private String enteredName = "";
    private int frame = 0;
    private boolean keyReleased = true;
    private int introTimer = 0;
    private float scrollY = 0;

    private BufferedImage logo;
    private SaveManager.SaveData[] saveSlots = new SaveManager.SaveData[3];

    // NES-accurate palette colors
    private static final Color NES_BLACK = new Color(0, 0, 0);
    private static final Color NES_BROWN = new Color(200, 148, 64);
    private static final Color NES_DARK_BROWN = new Color(120, 68, 0);
    private static final Color NES_RED = new Color(188, 0, 0);
    private static final Color NES_ORANGE = new Color(252, 152, 56);
    private static final Color NES_GOLD = new Color(252, 188, 60);
    private static final Color NES_GREEN = new Color(0, 120, 0);
    private static final Color NES_DARK_GREEN = new Color(0, 80, 0);
    private static final Color NES_FOREST = new Color(44, 100, 20);
    private static final Color NES_WATER = new Color(60, 128, 252);
    private static final Color NES_WATER2 = new Color(92, 148, 252);
    private static final Color NES_GRAY = new Color(124, 124, 124);
    private static final Color NES_WHITE = new Color(252, 252, 252);
    private static final Color NES_ROCK = new Color(120, 100, 68);

    private static final float SCROLL_SPEED = 0.5f;
    private static final int SCROLL_END = 340;

    private static final int CHARS_PER_ROW = 11;
    private static final int CHAR_ROWS = 3;
    private static final int MAX_NAME_LENGTH = 8;
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789-";

    private static final String[] STORY_TEXT = {
        "",
        "",
        " MANY YEARS AGO PRINCE",
        " DARKNESS \"GANNON\"",
        " STOLE ONE OF THE",
        " TRIFORCE WITH POWER.",
        "",
        " PRINCESS ZELDA HAD",
        " ONE OF THE TRIFORCE",
        " WITH WISDOM.",
        "",
        " SHE DIVIDED IT INTO",
        " 8 UNITS TO HIDE IT",
        " FROM \"GANNON\" BEFORE",
        " SHE WAS CAPTURED.",
        "",
        " GO FIND THE 8 UNITS",
        " \"LINK\" TO SAVE HER.",
    };

    private int charSelectX = 0;
    private int charSelectY = 0;

    public TitleScreen(ZeldaGame game, KeyHandler keyHandler) {
        this.game = game;
        this.keyHandler = keyHandler;
        loadLogo();
        loadSaveSlots();
    }

    private void loadLogo() {
        try {
            File logoFile = new File("sprites/Logo.gif");
            if (logoFile.exists()) {
                logo = ImageIO.read(logoFile);
            }
        } catch (Exception e) { logo = null; }
    }

    private void loadSaveSlots() {
        SaveManager saveManager = game.getSaveManager();
        for (int i = 0; i < 3; i++) {
            saveSlots[i] = saveManager.loadGame(i);
        }
    }

    public void update() {
        frame++;
        switch (state) {
            case INTRO: updateIntro(); break;
            case TITLE: updateTitle(); break;
            case FILE_SELECT: updateFileSelect(); break;
            case NAME_ENTRY: updateNameEntry(); break;
            case ELIMINATION: updateElimination(); break;
        }
    }

    private void updateIntro() {
        introTimer++;
        scrollY = introTimer * SCROLL_SPEED;

        if (keyHandler.startPressed && keyReleased) {
            state = ScreenState.TITLE;
            keyReleased = false;
        }
        if (scrollY > SCROLL_END) state = ScreenState.TITLE;
        if (!keyHandler.startPressed) keyReleased = true;
    }

    private void updateTitle() {
        if (keyHandler.startPressed && keyReleased) {
            state = ScreenState.FILE_SELECT;
            loadSaveSlots();
            keyReleased = false;
        }
        if (!keyHandler.startPressed) keyReleased = true;
    }

    private void updateFileSelect() {
        if (keyHandler.upPressed && keyReleased) {
            selectedSlot = (selectedSlot - 1 + 5) % 5;
            keyReleased = false;
        }
        if (keyHandler.downPressed && keyReleased) {
            selectedSlot = (selectedSlot + 1) % 5;
            keyReleased = false;
        }
        if (keyHandler.startPressed && keyReleased) {
            if (selectedSlot < 3) {
                if (saveSlots[selectedSlot] != null) {
                    game.loadGame(selectedSlot);
                } else {
                    enteredName = "";
                    charSelectX = 0;
                    charSelectY = 0;
                    state = ScreenState.NAME_ENTRY;
                }
            } else if (selectedSlot == 3) {
                state = ScreenState.ELIMINATION;
            }
            keyReleased = false;
        }
        if (keyHandler.escapePressed && keyReleased) {
            state = ScreenState.TITLE;
            keyReleased = false;
        }
        if (!keyHandler.upPressed && !keyHandler.downPressed &&
            !keyHandler.startPressed && !keyHandler.escapePressed) {
            keyReleased = true;
        }
    }

    private void updateNameEntry() {
        if (keyHandler.leftPressed && keyReleased) {
            charSelectX = (charSelectX - 1 + CHARS_PER_ROW) % CHARS_PER_ROW;
            keyReleased = false;
        }
        if (keyHandler.rightPressed && keyReleased) {
            charSelectX = (charSelectX + 1) % CHARS_PER_ROW;
            keyReleased = false;
        }
        if (keyHandler.upPressed && keyReleased) {
            charSelectY = (charSelectY - 1 + CHAR_ROWS + 1) % (CHAR_ROWS + 1);
            keyReleased = false;
        }
        if (keyHandler.downPressed && keyReleased) {
            charSelectY = (charSelectY + 1) % (CHAR_ROWS + 1);
            keyReleased = false;
        }
        if (keyHandler.zPressed && keyReleased) {
            if (charSelectY < CHAR_ROWS) {
                int charIndex = charSelectY * CHARS_PER_ROW + charSelectX;
                if (charIndex < VALID_CHARS.length() && enteredName.length() < MAX_NAME_LENGTH) {
                    enteredName += VALID_CHARS.charAt(charIndex);
                }
            } else if (enteredName.length() > 0) {
                game.startNewGame(enteredName.trim(), selectedSlot);
            }
            keyReleased = false;
        }
        if (keyHandler.xPressed && keyReleased) {
            if (enteredName.length() > 0) {
                enteredName = enteredName.substring(0, enteredName.length() - 1);
            }
            keyReleased = false;
        }
        if (keyHandler.startPressed && keyReleased && enteredName.length() > 0) {
            game.startNewGame(enteredName.trim(), selectedSlot);
            keyReleased = false;
        }
        if (keyHandler.escapePressed && keyReleased) {
            state = ScreenState.FILE_SELECT;
            keyReleased = false;
        }
        if (!keyHandler.leftPressed && !keyHandler.rightPressed &&
            !keyHandler.upPressed && !keyHandler.downPressed &&
            !keyHandler.zPressed && !keyHandler.xPressed &&
            !keyHandler.startPressed && !keyHandler.escapePressed) {
            keyReleased = true;
        }
    }

    private int elimSlot = 0;

    private void updateElimination() {
        if (keyHandler.upPressed && keyReleased) {
            elimSlot = (elimSlot - 1 + 3) % 3;
            keyReleased = false;
        }
        if (keyHandler.downPressed && keyReleased) {
            elimSlot = (elimSlot + 1) % 3;
            keyReleased = false;
        }
        if (keyHandler.startPressed && keyReleased) {
            if (saveSlots[elimSlot] != null) {
                game.getSaveManager().deleteSave(elimSlot);
                saveSlots[elimSlot] = null;
            }
            keyReleased = false;
        }
        if (keyHandler.escapePressed && keyReleased) {
            state = ScreenState.FILE_SELECT;
            keyReleased = false;
        }
        if (!keyHandler.upPressed && !keyHandler.downPressed &&
            !keyHandler.startPressed && !keyHandler.escapePressed) {
            keyReleased = true;
        }
    }

    // ======================== Rendering ========================

    public void render(Graphics2D g2) {
        g2.setColor(NES_BLACK);
        g2.fillRect(0, 0, 256, 240);

        switch (state) {
            case INTRO: renderIntro(g2); break;
            case TITLE: renderTitle(g2); break;
            case FILE_SELECT: renderFileSelect(g2); break;
            case NAME_ENTRY: renderNameEntry(g2); break;
            case ELIMINATION: renderElimination(g2); break;
        }
    }

    private void renderIntro(Graphics2D g2) {
        // Landscape scene at top (always visible)
        renderLandscape(g2);

        // Scrolling story text in the lower portion
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        int textAreaTop = 140;
        int textAreaBot = 228;
        int textStartY = textAreaBot - (int)scrollY;

        Shape oldClip = g2.getClip();
        g2.setClip(0, textAreaTop, 256, textAreaBot - textAreaTop);

        for (int i = 0; i < STORY_TEXT.length; i++) {
            int textY = textStartY + i * 14;
            if (textY > textAreaTop - 14 && textY < textAreaBot + 14) {
                String line = STORY_TEXT[i];
                g2.setColor(NES_BROWN);
                int textWidth = g2.getFontMetrics().stringWidth(line);
                g2.drawString(line, (256 - textWidth) / 2, textY);
            }
        }
        g2.setClip(oldClip);
    }

    private void renderLandscape(Graphics2D g2) {
        // Sky / dark background
        g2.setColor(NES_BLACK);
        g2.fillRect(0, 0, 256, 140);

        // Logo at top
        if (logo != null) {
            g2.drawImage(logo, 32, 8, 192, 84, null);
        } else {
            renderTextLogo(g2, 8);
        }

        // Green hillside with waterfall
        int hillY = 92;

        // Left hill
        g2.setColor(NES_FOREST);
        g2.fillRect(0, hillY, 96, 48);
        // Left trees
        renderTree(g2, 8, hillY - 12);
        renderTree(g2, 28, hillY - 8);
        renderTree(g2, 56, hillY - 14);
        renderTree(g2, 80, hillY - 6);

        // Right hill
        g2.setColor(NES_FOREST);
        g2.fillRect(160, hillY, 96, 48);
        // Right trees
        renderTree(g2, 168, hillY - 10);
        renderTree(g2, 192, hillY - 14);
        renderTree(g2, 216, hillY - 8);
        renderTree(g2, 236, hillY - 12);

        // Rocky cliff edges
        g2.setColor(NES_ROCK);
        g2.fillRect(88, hillY, 12, 48);
        g2.fillRect(156, hillY, 12, 48);

        // Waterfall opening (dark)
        g2.setColor(NES_BLACK);
        g2.fillRect(100, hillY - 4, 56, 52);

        // Waterfall animation
        int animOff = (frame / 3) % 8;
        for (int y = hillY; y < hillY + 44; y += 4) {
            int waveOff = ((y + animOff) / 4) % 2 == 0 ? 2 : -2;
            g2.setColor(((y + animOff) / 8) % 2 == 0 ? NES_WATER : NES_WATER2);
            g2.fillRect(108 + waveOff, y, 40, 3);
        }

        // Water pool at bottom
        g2.setColor(NES_WATER);
        g2.fillRect(92, hillY + 44, 72, 8);
        g2.setColor(NES_WATER2);
        int poolWave = (frame / 6) % 2;
        for (int x = 92; x < 164; x += 8) {
            g2.fillRect(x + poolWave * 4, hillY + 46, 4, 2);
        }

        // Ground below waterfall
        g2.setColor(NES_DARK_GREEN);
        g2.fillRect(0, hillY + 48, 256, 4);
        g2.setColor(NES_DARK_BROWN);
        g2.fillRect(0, hillY + 52, 256, 88);
    }

    private void renderTree(Graphics2D g2, int x, int y) {
        // Simple NES-style tree: dark trunk + green crown
        g2.setColor(NES_DARK_BROWN);
        g2.fillRect(x + 4, y + 10, 4, 6);
        g2.setColor(NES_GREEN);
        g2.fillOval(x, y, 12, 12);
        g2.setColor(NES_DARK_GREEN);
        g2.fillOval(x + 2, y + 2, 8, 8);
    }

    private void renderTextLogo(Graphics2D g2, int baseY) {
        // Color cycling for the title text
        Color[] titleColors = {NES_GOLD, NES_ORANGE, NES_BROWN, NES_ORANGE};
        int colorIdx = (frame / 8) % titleColors.length;

        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.setColor(titleColors[colorIdx]);
        centerText(g2, "THE  LEGEND  OF", baseY + 16);

        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.setColor(titleColors[(colorIdx + 1) % titleColors.length]);
        centerText(g2, "Z E L D A", baseY + 44);

        // Triforce symbol
        drawTriforce(g2, 104, baseY + 52, NES_GOLD);

        // Subtitle
        g2.setFont(new Font("Monospaced", Font.BOLD, 7));
        g2.setColor(NES_WHITE);
        centerText(g2, "- NES JAVA EDITION -", baseY + 74);
    }

    private void drawTriforce(Graphics2D g2, int cx, int cy, Color color) {
        g2.setColor(color);
        // Top triangle
        int[] tx = {cx + 24, cx + 32, cx + 16};
        int[] ty = {cy, cy + 12, cy + 12};
        g2.fillPolygon(tx, ty, 3);
        // Bottom-left triangle
        int[] lx = {cx + 16, cx + 24, cx + 8};
        int[] ly = {cy + 12, cy + 24, cy + 24};
        g2.fillPolygon(lx, ly, 3);
        // Bottom-right triangle
        int[] rx = {cx + 32, cx + 40, cx + 24};
        int[] ry = {cy + 12, cy + 24, cy + 24};
        g2.fillPolygon(rx, ry, 3);
    }

    private void renderTitle(Graphics2D g2) {
        renderLandscape(g2);

        // Flashing "PUSH START BUTTON"
        if ((frame / 30) % 2 == 0) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 8));
            g2.setColor(NES_BROWN);
            centerText(g2, "PUSH START BUTTON", 172);
        }

        // Copyright
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g2.setColor(NES_BROWN);
        centerText(g2, "(C) 1986 NINTENDO", 200);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 6));
        g2.setColor(NES_GRAY);
        centerText(g2, "JAVA EDITION BY HASSAAN", 216);
    }

    private void renderFileSelect(Graphics2D g2) {
        // Title bar
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.setColor(NES_RED);
        centerText(g2, "- SELECT -", 28);

        // Decorative line
        g2.setColor(NES_DARK_BROWN);
        g2.fillRect(24, 34, 208, 1);

        // Three save slots
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        for (int i = 0; i < 3; i++) {
            int y = 52 + i * 44;
            boolean sel = (i == selectedSlot);

            // Cursor heart
            drawHeart(g2, 24, y + 2, sel);

            // Slot number
            g2.setColor(NES_WHITE);
            g2.drawString(String.valueOf(i + 1) + ".", 40, y + 10);

            if (saveSlots[i] != null) {
                // Player name
                g2.setColor(NES_WHITE);
                g2.drawString(saveSlots[i].playerName, 64, y + 10);

                // Hearts display
                int hearts = saveSlots[i].maxHealth / 2;
                for (int h = 0; h < Math.min(hearts, 10); h++) {
                    drawSmallHeart(g2, 140 + h * 10, y + 4);
                }

                // Death count (skull icon + number)
                int deathCount = saveSlots[i].deathCount;
                g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
                g2.setColor(NES_GRAY);
                g2.drawString("x" + deathCount, 78, y + 22);

                // Triforce count (small triangles)
                if (saveSlots[i].inventory != null) {
                    int triCount = saveSlots[i].inventory.getTriforceCount();
                    for (int t = 0; t < 8; t++) {
                        if (saveSlots[i].inventory.hasTriforce(t + 1)) {
                            g2.setColor(NES_GOLD);
                        } else {
                            g2.setColor(new Color(40, 40, 40));
                        }
                        int tx = 140 + t * 12;
                        int ty = y + 16;
                        int[] triX = {tx + 4, tx, tx + 8};
                        int[] triY = {ty, ty + 6, ty + 6};
                        g2.fillPolygon(triX, triY, 3);
                    }
                }
                g2.setFont(new Font("Monospaced", Font.BOLD, 8));
            } else {
                g2.setColor(NES_GRAY);
                g2.drawString("- EMPTY -", 64, y + 10);
            }

            // Slot separator
            g2.setColor(new Color(40, 40, 40));
            g2.fillRect(28, y + 28, 200, 1);
        }

        // Register option
        int regY = 192;
        drawHeart(g2, 24, regY + 2, selectedSlot == 3);
        g2.setColor(NES_RED);
        g2.drawString("REGISTER YOUR NAME", 44, regY + 10);

        // Elimination option
        int elimY = 212;
        drawHeart(g2, 24, elimY + 2, selectedSlot == 4);
        g2.setColor(NES_RED);
        g2.drawString("ELIMINATION MODE", 44, elimY + 10);
    }

    private void renderElimination(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.setColor(NES_RED);
        centerText(g2, "- ELIMINATION MODE -", 28);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g2.setColor(NES_GRAY);
        centerText(g2, "SELECT A FILE TO DELETE", 42);

        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        for (int i = 0; i < 3; i++) {
            int y = 64 + i * 44;
            boolean sel = (i == elimSlot);

            // Skull cursor for elimination
            if (sel) {
                g2.setColor(NES_RED);
                g2.drawString("X", 26, y + 10);
            }

            g2.setColor(NES_WHITE);
            g2.drawString(String.valueOf(i + 1) + ".", 40, y + 10);

            if (saveSlots[i] != null) {
                g2.setColor(sel ? NES_RED : NES_WHITE);
                g2.drawString(saveSlots[i].playerName, 64, y + 10);
            } else {
                g2.setColor(NES_GRAY);
                g2.drawString("- EMPTY -", 64, y + 10);
            }
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g2.setColor(NES_GRAY);
        centerText(g2, "ENTER=DELETE  ESC=BACK", 216);
    }

    private void renderNameEntry(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.setColor(NES_RED);
        centerText(g2, "REGISTER YOUR NAME", 24);

        // Current name display with blinking cursor
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        drawHeart(g2, 20, 42, true);

        StringBuilder displayName = new StringBuilder(enteredName);
        for (int i = enteredName.length(); i < MAX_NAME_LENGTH; i++) {
            displayName.append((i == enteredName.length() && (frame / 15) % 2 == 0) ? "_" : " ");
        }
        g2.setColor(NES_WHITE);
        g2.drawString(displayName.toString(), 44, 52);

        // Character grid
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        int gridX = 28, gridY = 80;
        int cellW = 18, cellH = 18;

        for (int row = 0; row < CHAR_ROWS; row++) {
            for (int col = 0; col < CHARS_PER_ROW; col++) {
                int idx = row * CHARS_PER_ROW + col;
                if (idx < VALID_CHARS.length()) {
                    int cx = gridX + col * cellW;
                    int cy = gridY + row * cellH;
                    boolean sel = (row == charSelectY && col == charSelectX);

                    if (sel) {
                        // Highlighted selection box
                        g2.setColor(NES_WHITE);
                        g2.fillRect(cx - 1, cy - 11, cellW - 2, cellH - 2);
                        g2.setColor(NES_BLACK);
                    } else {
                        g2.setColor(NES_WHITE);
                    }
                    g2.drawString(String.valueOf(VALID_CHARS.charAt(idx)), cx + 2, cy);
                }
            }
        }

        // "REGISTER END" button
        int regY = gridY + CHAR_ROWS * cellH + 12;
        boolean regSel = (charSelectY == CHAR_ROWS);
        if (regSel) {
            g2.setColor(NES_WHITE);
            g2.fillRect(gridX - 2, regY - 11, 110, 14);
            g2.setColor(NES_BLACK);
        } else {
            g2.setColor(NES_RED);
        }
        g2.drawString("REGISTER END", gridX + 4, regY);

        // Controls help
        g2.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g2.setColor(NES_GRAY);
        centerText(g2, "Z=ADD  X=DELETE  ENTER=START", 216);
    }

    // ======================== Drawing Helpers ========================

    private void drawHeart(Graphics2D g2, int x, int y, boolean selected) {
        if (selected) {
            // Animated pulsing heart
            g2.setColor((frame / 12) % 2 == 0 ? NES_RED : NES_ORANGE);
        } else {
            g2.setColor(NES_GRAY);
        }
        // Better heart shape
        int[] hx = {x+5, x+7, x+9, x+11, x+13, x+11, x+9, x+5, x+1, x-1, x+1, x+3};
        int[] hy = {y-2, y-3, y-2, y-1, y+2, y+5, y+8, y+10, y+5, y+2, y-1, y-2};
        g2.fillPolygon(hx, hy, 12);
    }

    private void drawSmallHeart(Graphics2D g2, int x, int y) {
        g2.setColor(NES_RED);
        g2.fillRect(x + 1, y, 2, 1);
        g2.fillRect(x + 5, y, 2, 1);
        g2.fillRect(x, y + 1, 8, 2);
        g2.fillRect(x + 1, y + 3, 6, 1);
        g2.fillRect(x + 2, y + 4, 4, 1);
        g2.fillRect(x + 3, y + 5, 2, 1);
    }

    private void centerText(Graphics2D g2, String text, int y) {
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (256 - w) / 2, y);
    }
}
