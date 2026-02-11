package zelda;

import engine.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TitleScreen {
    private ZeldaGame game;
    private KeyHandler keyHandler;

    private enum ScreenState { INTRO, TITLE, FILE_SELECT, NAME_ENTRY, ELIMINATION }

    private ScreenState state = ScreenState.INTRO;
    private int selectedSlot = 0;
    private String enteredName = "";
    private int cursorBlink = 0;
    private boolean keyReleased = true;
    private int introTimer = 0;
    private int scrollY = 0;

    private BufferedImage logo;
    private SaveManager.SaveData[] saveSlots = new SaveManager.SaveData[3];

    private static final Color NES_BROWN = new Color(188, 140, 76);
    private static final Color NES_RED = new Color(180, 56, 0);
    private static final Color NES_ORANGE = new Color(252, 152, 56);
    private static final Color NES_GRAY = new Color(116, 116, 116);

    private static final int SCROLL_SPEED = 3;
    private static final int SCROLL_END = 200;

    private static final int LOGO_X = 38;
    private static final int LOGO_Y = 18;
    private static final int LOGO_W = 180;
    private static final int LOGO_H = 100;

    private static final int WATERFALL_X = 88;
    private static final int WATERFALL_Y = 120;
    private static final int WATERFALL_W = 80;
    private static final int WATERFALL_H = 80;

    private static final int FILE_SLOT_START_Y = 64;
    private static final int FILE_SLOT_SPACING = 40;
    private static final int FILE_HEART_X = 32;
    private static final int FILE_NAME_X = 72;
    private static final int FILE_SLOT_NUM_X = 56;

    private static final int CHAR_GRID_START_X = 32;
    private static final int CHAR_GRID_START_Y = 96;
    private static final int CHAR_CELL_W = 16;
    private static final int CHAR_CELL_H = 16;
    private static final int CHARS_PER_ROW = 11;
    private static final int CHAR_ROWS = 3;
    private static final int MAX_NAME_LENGTH = 8;

    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123789 -";

    private static final String[] STORY_TEXT = {
        "",
        "MANY YEARS AGO PRINCE",
        "DARKNESS \"GANNON\" STOLE",
        "ONE OF THE TRIFORCE",
        "WITH POWER. PRINCESS",
        "ZELDA HAD ONE OF THE",
        "TRIFORCE WITH WISDOM.",
        "SHE DIVIDED IT INTO",
        "8 UNITS TO HIDE IT FROM",
        "\"GANNON\" BEFORE SHE WAS",
        "CAPTURED. GO FIND THE",
        "8 UNITS \"LINK\" TO SAVE",
        "HER."
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
        cursorBlink++;

        switch (state) {
            case INTRO: updateIntro(); break;
            case TITLE: updateTitle(); break;
            case FILE_SELECT: updateFileSelect(); break;
            case NAME_ENTRY: updateNameEntry(); break;
            case ELIMINATION: break;
        }
    }

    private void updateIntro() {
        introTimer++;
        scrollY = introTimer / SCROLL_SPEED;

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

    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 256, 240);

        switch (state) {
            case INTRO: renderIntro(g2); break;
            case TITLE: renderTitle(g2); break;
            case FILE_SELECT: renderFileSelect(g2); break;
            case NAME_ENTRY: renderNameEntry(g2); break;
            case ELIMINATION: break;
        }
    }

    private void renderIntro(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.setColor(NES_BROWN);

        int textStartY = 240 - scrollY;
        for (int i = 0; i < STORY_TEXT.length; i++) {
            int textY = textStartY + i * 16;
            if (textY > -16 && textY < 240) {
                String line = STORY_TEXT[i];
                int textWidth = g2.getFontMetrics().stringWidth(line);
                g2.drawString(line, (256 - textWidth) / 2, textY);
            }
        }

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 256, 48);
        g2.fillRect(0, 200, 256, 40);

        renderWaterfall(g2);
    }

    private void renderWaterfall(Graphics2D g2) {
        g2.setColor(new Color(60, 88, 36));
        g2.fillRect(WATERFALL_X - 20, WATERFALL_Y - 8, WATERFALL_W + 40, WATERFALL_H + 16);

        int animOffset = (cursorBlink / 4) % 8;
        g2.setColor(new Color(92, 148, 252));
        for (int y = WATERFALL_Y; y < WATERFALL_Y + WATERFALL_H; y += 4) {
            int rowOffset = ((y + animOffset) / 4) % 2 == 0 ? 2 : -2;
            g2.fillRect(WATERFALL_X + rowOffset, y, WATERFALL_W - 4, 3);
        }

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(WATERFALL_X + 8, WATERFALL_Y - 4, WATERFALL_W - 16, 8);

        if (logo != null) {
            g2.drawImage(logo, LOGO_X, LOGO_Y, LOGO_W, LOGO_H, null);
        } else {
            drawPixelText(g2, "THE LEGEND OF", 72, 24, NES_BROWN);
            drawPixelText(g2, "ZELDA", 104, 48, NES_BROWN);
        }
    }

    private void renderTitle(Graphics2D g2) {
        renderWaterfall(g2);

        if ((cursorBlink / 30) % 2 == 0) {
            drawPixelText(g2, "PUSH START BUTTON", 56, 175, NES_BROWN);
        }
        drawPixelText(g2, "@ 1986 NINTENDO", 72, 208, NES_BROWN);
    }

    private void renderFileSelect(Graphics2D g2) {
        drawPixelText(g2, "- SELECT -", 88, 32, NES_RED);

        for (int i = 0; i < 3; i++) {
            int y = FILE_SLOT_START_Y + i * FILE_SLOT_SPACING;
            boolean selected = (i == selectedSlot);

            drawHeart(g2, FILE_HEART_X, y, selected);
            drawPixelText(g2, String.valueOf(i + 1), FILE_SLOT_NUM_X, y + 4, Color.WHITE);

            if (saveSlots[i] != null) {
                drawPixelText(g2, saveSlots[i].playerName, FILE_NAME_X, y + 4, Color.WHITE);

                int hearts = saveSlots[i].maxHealth / 2;
                for (int h = 0; h < hearts; h++) {
                    drawSmallHeart(g2, FILE_NAME_X + 80 + h * 9, y + 2);
                }
            } else {
                drawPixelText(g2, "--------", FILE_NAME_X, y + 4, NES_GRAY);
            }
        }

        int registerY = 200;
        drawHeart(g2, FILE_HEART_X, registerY, selectedSlot == 3);
        drawPixelText(g2, "REGISTER YOUR NAME", FILE_SLOT_NUM_X, registerY + 4, NES_RED);

        int elimY = 216;
        drawHeart(g2, FILE_HEART_X, elimY, selectedSlot == 4);
        drawPixelText(g2, "ELIMINATION MODE", FILE_SLOT_NUM_X, elimY + 4, NES_RED);
    }

    private void renderNameEntry(Graphics2D g2) {
        drawPixelText(g2, "REGISTER YOUR NAME", 56, 24, NES_RED);

        drawHeart(g2, 24, 48, true);

        String displayName = enteredName;
        for (int i = enteredName.length(); i < MAX_NAME_LENGTH; i++) displayName += "_";
        drawPixelText(g2, displayName, 48, 52, Color.WHITE);

        for (int row = 0; row < CHAR_ROWS; row++) {
            for (int col = 0; col < CHARS_PER_ROW; col++) {
                int idx = row * CHARS_PER_ROW + col;
                if (idx < VALID_CHARS.length()) {
                    int x = CHAR_GRID_START_X + col * CHAR_CELL_W;
                    int y = CHAR_GRID_START_Y + row * CHAR_CELL_H;
                    boolean selected = (row == charSelectY && col == charSelectX);

                    Color color = selected ? NES_RED : Color.WHITE;
                    if (selected && charSelectY < CHAR_ROWS) {
                        g2.setColor(Color.WHITE);
                        g2.fillRect(x - 2, y - 10, 12, 12);
                        color = Color.BLACK;
                    }
                    drawPixelText(g2, String.valueOf(VALID_CHARS.charAt(idx)), x, y, color);
                }
            }
        }

        int registerY = CHAR_GRID_START_Y + CHAR_ROWS * CHAR_CELL_H + 16;
        boolean registerSelected = (charSelectY == CHAR_ROWS);
        if (registerSelected) {
            g2.setColor(Color.WHITE);
            g2.fillRect(CHAR_GRID_START_X - 4, registerY - 10, 96, 14);
        }
        drawPixelText(g2, "REGISTER END", CHAR_GRID_START_X, registerY,
            registerSelected ? Color.BLACK : NES_RED);

        drawPixelText(g2, "Z:ADD  X:DEL  ENTER:OK", 32, 210, NES_GRAY);
    }

    private void drawHeart(Graphics2D g2, int x, int y, boolean filled) {
        if (filled && (cursorBlink / 16) % 2 == 0) {
            g2.setColor(NES_RED);
        } else if (filled) {
            g2.setColor(NES_ORANGE);
        } else {
            g2.setColor(NES_GRAY);
        }
        int[] xPoints = {x+4, x+8, x+12, x+8, x+4, x};
        int[] yPoints = {y, y-2, y, y+8, y+8, y};
        g2.fillPolygon(xPoints, yPoints, 6);
    }

    private void drawSmallHeart(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(200, 72, 72));
        int[] xp = {x+3, x+6, x+9, x+6, x+3, x};
        int[] yp = {y, y-1, y, y+6, y+6, y};
        g2.fillPolygon(xp, yp, 6);
    }

    private void drawPixelText(Graphics2D g2, String text, int x, int y, Color color) {
        g2.setColor(color);
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString(text, x, y);
    }
}
