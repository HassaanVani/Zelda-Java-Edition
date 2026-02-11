package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Cave {
    public static final int CAVE_WIDTH = 256;
    public static final int CAVE_HEIGHT = 176;

    public static final int OLD_MAN_X = 104;
    public static final int OLD_MAN_Y = 56;
    public static final int OLD_MAN_W = 16;
    public static final int OLD_MAN_H = 16;

    public static final int SWORD_X = 120;
    public static final int SWORD_Y = 80;
    public static final int SWORD_W = 8;
    public static final int SWORD_H = 16;

    public static final int TEXT_X = 48;
    public static final int TEXT_Y = 40;
    public static final String OLD_MAN_TEXT = "IT'S DANGEROUS TO GO";
    public static final String OLD_MAN_TEXT2 = "ALONE! TAKE THIS.";

    public static final double PLAYER_SPAWN_X = 120;
    public static final double PLAYER_SPAWN_Y = 144;
    public static final int EXIT_THRESHOLD_Y = 168;

    private static final Color CAVE_BG = new Color(52, 28, 0);
    private static final Color CAVE_WALL = new Color(116, 116, 116);
    private static final Color FIRE_COLOR = new Color(252, 152, 56);

    private boolean swordTaken = false;
    private boolean active = false;
    private BufferedImage oldManSprite;
    private BufferedImage swordSprite;

    public Cave() {
        loadSprites();
    }

    private void loadSprites() {
        oldManSprite = loadImage("sprites/NPCs/Old Man.gif");
        swordSprite = loadImage("sprites/Objects/Wooden Sword (Up).gif");
    }

    private BufferedImage loadImage(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception e) {}
        return null;
    }

    public void enter(ZeldaPlayer player) {
        active = true;
        player.setPosition(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
    }

    public boolean update(ZeldaPlayer player) {
        if (!active) return false;

        if (!swordTaken) {
            Rectangle playerBox = player.getHitbox();
            Rectangle swordBox = new Rectangle(SWORD_X, SWORD_Y, SWORD_W, SWORD_H);
            if (playerBox.intersects(swordBox)) {
                swordTaken = true;
                player.setHasSword(true);
            }
        }

        if (player.getWorldY() >= EXIT_THRESHOLD_Y) {
            active = false;
            return true;
        }
        return false;
    }

    public void render(Graphics2D g2) {
        g2.setColor(CAVE_BG);
        g2.fillRect(0, 0, CAVE_WIDTH, CAVE_HEIGHT);

        g2.setColor(CAVE_WALL);
        g2.fillRect(0, 0, CAVE_WIDTH, 24);
        g2.fillRect(0, 0, 24, CAVE_HEIGHT);
        g2.fillRect(CAVE_WIDTH - 24, 0, 24, CAVE_HEIGHT);

        g2.setColor(FIRE_COLOR);
        g2.fillRect(80, 72, 8, 8);
        g2.fillRect(168, 72, 8, 8);

        if (oldManSprite != null) {
            g2.drawImage(oldManSprite, OLD_MAN_X, OLD_MAN_Y, OLD_MAN_W, OLD_MAN_H, null);
        } else {
            g2.setColor(new Color(200, 120, 60));
            g2.fillRect(OLD_MAN_X, OLD_MAN_Y, OLD_MAN_W, OLD_MAN_H);
        }

        if (!swordTaken) {
            if (swordSprite != null) {
                g2.drawImage(swordSprite, SWORD_X, SWORD_Y, SWORD_W, SWORD_H, null);
            } else {
                g2.setColor(new Color(160, 120, 60));
                g2.fillRect(SWORD_X, SWORD_Y, SWORD_W, SWORD_H);
            }
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.setColor(Color.WHITE);
        g2.drawString(OLD_MAN_TEXT, TEXT_X, TEXT_Y);
        g2.drawString(OLD_MAN_TEXT2, TEXT_X + 16, TEXT_Y + 14);
    }

    public boolean isActive() { return active; }
    public boolean isSwordTaken() { return swordTaken; }
}
