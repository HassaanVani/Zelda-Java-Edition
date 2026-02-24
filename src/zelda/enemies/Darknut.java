package zelda.enemies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import zelda.*;

/**
 * Darknut: armored knight that can only be damaged from behind or the side.
 * NES-accurate (Z_04.asm:6475 UpdateDarknut):
 * - Walks straight until wall, then picks new random direction
 * - Random turn chance per frame (NES turn rate $80 ≈ 1/2 chance per 256 frames)
 * - NEVER stunned (boomerang has no effect)
 * - Front shield blocks ALL attacks from the facing direction
 * - 8-frame animation cycle
 */
public class Darknut extends ZeldaEnemy {
    private boolean isBlue;
    private BufferedImage frontSprite;
    private BufferedImage backSprite;
    private BufferedImage leftSprite;

    public Darknut(double x, double y, boolean blue) {
        super(x, y, 4, 2, AIType.RANDOM);
        this.isBlue = blue;
        applyStats(blue ? EnemyStats.darknutBlue() : EnemyStats.darknutRed());
        this.frontShield = true;
        // NES: Darknuts are immune to boomerang stun
        this.immunityMask |= EnemyStats.DMG_BOOMERANG;
        this.direction = (int)(Math.random() * 4);
        loadDarknutSprites();
    }

    private void loadDarknutSprites() {
        String color = isBlue ? "Blue" : "Red";
        frontSprite = loadSprite("sprites/Enemies/Darknut - " + color + " (Front).gif");
        backSprite = loadSprite("sprites/Enemies/Darknut - " + color + " (Back).gif");
        leftSprite = loadSprite("sprites/Enemies/Darknut - " + color + " (Left).gif");
        sprite = frontSprite;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        if (!active) return;
        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;

        // NES: Darknuts are NEVER stunned — clear stun every frame
        // (Z_04.asm:6481: LDA #$00 / STA ObjStunTimer, X)

        // NES turn rate $80: random chance to change direction each frame
        if (Math.random() < 0.02) {
            direction = (int)(Math.random() * 4);
        }

        // Animation: 8 frames per cycle
        animationCounter++;
        if (animationCounter >= 8) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }

        oldX = x;
        oldY = y;
        double nx = x, ny = y;
        switch (direction) {
            case 0: ny -= speed; break;
            case 1: nx -= speed; break;
            case 2: ny += speed; break;
            case 3: nx += speed; break;
        }

        // Walk straight; on wall collision pick new random direction
        if (room != null && room.isWalkable((int)(nx + width / 2), (int)(ny + height / 2))) {
            x = nx;
            y = ny;
        } else {
            direction = (int)(Math.random() * 4);
        }

        x = Math.max(8, Math.min(x, ZeldaRoom.ROOM_PIXEL_W - width - 8));
        y = Math.max(8, Math.min(y, ZeldaRoom.ROOM_PIXEL_H - height - 8));
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;
        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
        } else {
            BufferedImage currentSprite = null;
            boolean flipH = false;
            switch (direction) {
                case 0: // up (back)
                    currentSprite = backSprite;
                    break;
                case 1: // left
                    currentSprite = leftSprite;
                    break;
                case 2: // down (front)
                    currentSprite = frontSprite;
                    break;
                case 3: // right (flip left horizontally)
                    currentSprite = leftSprite;
                    flipH = true;
                    break;
            }

            if (currentSprite != null) {
                if (flipH) {
                    g2.drawImage(currentSprite, (int)x + width, (int)y, -width, height, null);
                } else {
                    g2.drawImage(currentSprite, (int)x, (int)y, width, height, null);
                }
            } else {
                g2.setColor(isBlue ? new Color(60, 60, 180) : new Color(180, 60, 60));
                g2.fillRect((int)x, (int)y, width, height);
                g2.setColor(Color.GRAY);
                g2.fillRect((int)x + 4, (int)y + 2, 8, 4);
            }
        }
    }
}
