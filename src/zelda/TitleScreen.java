package zelda;

import engine.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TitleScreen {
    private ZeldaGame game;
    private KeyHandler keyHandler;
    
    private enum ScreenState {
        INTRO,
        TITLE,
        FILE_SELECT,
        NAME_ENTRY,
        ELIMINATION
    }
    
    private ScreenState state = ScreenState.INTRO;
    private int selectedSlot = 0;
    private String enteredName = "";
    private int cursorBlink = 0;
    private boolean keyReleased = true;
    private int introTimer = 0;
    
    private BufferedImage logo;
    private SaveManager.SaveData[] saveSlots = new SaveManager.SaveData[3];
    
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
    
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123789 -";
    private int charSelectX = 0;
    private int charSelectY = 0;
    
    private int scrollY = 0;
    
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
        } catch (Exception e) {
            logo = null;
        }
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
            case INTRO:
                updateIntro();
                break;
            case TITLE:
                updateTitle();
                break;
            case FILE_SELECT:
                updateFileSelect();
                break;
            case NAME_ENTRY:
                updateNameEntry();
                break;
            case ELIMINATION:
                break;
        }
    }
    
    private void updateIntro() {
        introTimer++;
        scrollY = introTimer / 3;
        
        if (keyHandler.startPressed && keyReleased) {
            state = ScreenState.TITLE;
            keyReleased = false;
        }
        
        if (scrollY > 200) {
            state = ScreenState.TITLE;
        }
        
        if (!keyHandler.startPressed) {
            keyReleased = true;
        }
    }
    
    private void updateTitle() {
        if (keyHandler.startPressed && keyReleased) {
            state = ScreenState.FILE_SELECT;
            loadSaveSlots();
            keyReleased = false;
        }
        if (!keyHandler.startPressed) {
            keyReleased = true;
        }
    }
    
    private void updateFileSelect() {
        if (keyHandler.upPressed && keyReleased) {
            selectedSlot = (selectedSlot - 1 + 4) % 4;
            keyReleased = false;
        }
        if (keyHandler.downPressed && keyReleased) {
            selectedSlot = (selectedSlot + 1) % 4;
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
        int charsPerRow = 11;
        int numRows = 3;
        
        if (keyHandler.leftPressed && keyReleased) {
            charSelectX = (charSelectX - 1 + charsPerRow) % charsPerRow;
            keyReleased = false;
        }
        if (keyHandler.rightPressed && keyReleased) {
            charSelectX = (charSelectX + 1) % charsPerRow;
            keyReleased = false;
        }
        if (keyHandler.upPressed && keyReleased) {
            charSelectY = (charSelectY - 1 + numRows + 1) % (numRows + 1);
            keyReleased = false;
        }
        if (keyHandler.downPressed && keyReleased) {
            charSelectY = (charSelectY + 1) % (numRows + 1);
            keyReleased = false;
        }
        
        if (keyHandler.zPressed && keyReleased) {
            if (charSelectY < numRows) {
                int charIndex = charSelectY * charsPerRow + charSelectX;
                if (charIndex < VALID_CHARS.length() && enteredName.length() < 8) {
                    enteredName += VALID_CHARS.charAt(charIndex);
                }
            } else {
                if (enteredName.length() > 0) {
                    game.startNewGame(enteredName.trim(), selectedSlot);
                }
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
            case INTRO:
                renderIntro(g2);
                break;
            case TITLE:
                renderTitle(g2);
                break;
            case FILE_SELECT:
                renderFileSelect(g2);
                break;
            case NAME_ENTRY:
                renderNameEntry(g2);
                break;
            case ELIMINATION:
                break;
        }
    }
    
    private void renderIntro(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 256, 240);
        
        g2.setColor(new Color(188, 140, 76));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        
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
        int wfX = 88;
        int wfY = 120;
        int wfWidth = 80;
        int wfHeight = 80;
        
        g2.setColor(new Color(60, 88, 36));
        g2.fillRect(wfX - 20, wfY - 8, wfWidth + 40, wfHeight + 16);
        
        int animOffset = (cursorBlink / 4) % 8;
        g2.setColor(new Color(92, 148, 252));
        for (int y = wfY; y < wfY + wfHeight; y += 4) {
            int rowOffset = ((y + animOffset) / 4) % 2 == 0 ? 2 : -2;
            g2.fillRect(wfX + rowOffset, y, wfWidth - 4, 3);
        }
        
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(wfX + 8, wfY - 4, wfWidth - 16, 8);
        
        if (logo != null) {
            int logoX = (256 - logo.getWidth()) / 2;
            g2.drawImage(logo, logoX, 20, null);
        } else {
            drawPixelText(g2, "THE LEGEND OF", 72, 24, new Color(188, 140, 76));
            drawPixelText(g2, "ZELDA", 104, 48, new Color(188, 140, 76));
        }
    }
    
    private void renderTitle(Graphics2D g2) {
        renderWaterfall(g2);
        
        if ((cursorBlink / 30) % 2 == 0) {
            drawPixelText(g2, "PUSH START BUTTON", 56, 175, new Color(188, 140, 76));
        }
        
        drawPixelText(g2, "@ 1986 NINTENDO", 72, 208, new Color(188, 140, 76));
    }
    
    private void renderFileSelect(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, 256, 240);
        
        drawPixelText(g2, "- REGISTER YOUR NAME -", 40, 24, Color.WHITE);
        
        for (int i = 0; i < 3; i++) {
            int y = 56 + i * 40;
            
            g2.setColor(new Color(180, 56, 0));
            drawHeart(g2, 24, y, i == selectedSlot && selectedSlot < 3);
            
            String slotLabel = String.valueOf(i + 1);
            drawPixelText(g2, slotLabel, 48, y + 4, Color.WHITE);
            
            if (saveSlots[i] != null) {
                drawPixelText(g2, saveSlots[i].playerName, 72, y + 4, Color.WHITE);
            } else {
                drawPixelText(g2, "--------", 72, y + 4, new Color(100, 100, 100));
            }
        }
        
        int elimY = 190;
        drawHeart(g2, 24, elimY, selectedSlot == 3);
        drawPixelText(g2, "ELIMINATION MODE", 48, elimY + 4, new Color(180, 56, 0));
    }
    
    private void renderNameEntry(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 256, 240);
        
        drawPixelText(g2, "REGISTER YOUR NAME", 56, 24, new Color(180, 56, 0));
        
        g2.setColor(new Color(180, 56, 0));
        drawHeart(g2, 24, 48, true);
        
        g2.setColor(Color.WHITE);
        String displayName = enteredName;
        for (int i = enteredName.length(); i < 8; i++) {
            displayName += "_";
        }
        drawPixelText(g2, displayName, 48, 52, Color.WHITE);
        
        int charsPerRow = 11;
        int startX = 32;
        int startY = 96;
        int charWidth = 16;
        int charHeight = 16;
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < charsPerRow; col++) {
                int idx = row * charsPerRow + col;
                if (idx < VALID_CHARS.length()) {
                    int x = startX + col * charWidth;
                    int y = startY + row * charHeight;
                    
                    boolean selected = (row == charSelectY && col == charSelectX);
                    Color color = selected ? new Color(180, 56, 0) : Color.WHITE;
                    
                    if (selected && charSelectY < 3) {
                        g2.setColor(Color.WHITE);
                        g2.fillRect(x - 2, y - 10, 12, 12);
                        color = Color.BLACK;
                    }
                    
                    drawPixelText(g2, String.valueOf(VALID_CHARS.charAt(idx)), x, y, color);
                }
            }
        }
        
        int registerY = startY + 3 * charHeight + 16;
        boolean registerSelected = (charSelectY == 3);
        if (registerSelected) {
            g2.setColor(Color.WHITE);
            g2.fillRect(startX - 4, registerY - 10, 80, 14);
        }
        drawPixelText(g2, "REGISTER", startX, registerY, registerSelected ? Color.BLACK : new Color(180, 56, 0));
        
        drawPixelText(g2, "Z:ADD X:DEL START:OK", 40, 210, new Color(100, 100, 100));
    }
    
    private void drawHeart(Graphics2D g2, int x, int y, boolean filled) {
        if (filled && (cursorBlink / 16) % 2 == 0) {
            g2.setColor(new Color(180, 56, 0));
        } else if (filled) {
            g2.setColor(new Color(252, 152, 56));
        } else {
            g2.setColor(new Color(100, 100, 100));
        }
        
        int[] xPoints = {x+4, x+8, x+12, x+8, x+4, x};
        int[] yPoints = {y, y-2, y, y+8, y+8, y};
        g2.fillPolygon(xPoints, yPoints, 6);
    }
    
    private void drawPixelText(Graphics2D g2, String text, int x, int y, Color color) {
        g2.setColor(color);
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString(text, x, y);
    }
}
