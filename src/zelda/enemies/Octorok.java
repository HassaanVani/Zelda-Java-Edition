package zelda.enemies;

import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.ImageIcon;
import zelda.*;

public class Octorok extends ZeldaEnemy {
    private static final int SHOOT_RANGE = 80;
    private static final int SHOOT_COOLDOWN = 90;
    private static final double PROJECTILE_SPEED = 2.0;

    private boolean isRed;
    private int shootTimer = 0;
    private Image frontSprite;
    private Image leftSprite;

    public Octorok(double x, double y, boolean red) {
        super(x, y, 1, AIType.SHOOTER);
        this.isRed = red;
        applyStats(red ? EnemyStats.octorokRed() : EnemyStats.octorokBlue());
        loadOctorokSprites();
    }

    private void loadOctorokSprites() {
        String color = isRed ? "Red" : "Blue";
        frontSprite = loadGif("sprites/Enemies/Octorok - " + color + " (Front).gif");
        leftSprite = loadGif("sprites/Enemies/Octorok - " + color + " (Left).gif");
    }

    private Image loadGif(String path) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(path).getImage();
        return null;
    }

    @Override
    public void update(ZeldaPlayer player, ZeldaRoom room, List<Projectile> projectiles) {
        oldX = x;
        oldY = y;

        if (damageTimer > 0) damageTimer--;
        if (invulnerableFrames > 0) invulnerableFrames--;
        if (shootTimer > 0) shootTimer--;

        animationCounter++;
        if (animationCounter >= 10) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 2;
        }

        randomMove(room);

        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < SHOOT_RANGE && shootTimer == 0) {
            shoot(player, projectiles);
            shootTimer = SHOOT_COOLDOWN;
        }
    }

    private void shoot(ZeldaPlayer player, List<Projectile> projectiles) {
        double dx = player.getWorldX() - x;
        double dy = player.getWorldY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        double vx = (dx / dist) * PROJECTILE_SPEED;
        double vy = (dy / dist) * PROJECTILE_SPEED;

        Projectile p = new Projectile(x + width/2, y + height/2, vx, vy, false);
        p.setColor(isRed ? new Color(180, 56, 0) : new Color(100, 100, 200));
        projectiles.add(p);
    }

    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        if (damageTimer > 0 && (damageTimer / 3) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x, (int)y, width, height);
            return;
        }

        int dx = (int) x;
        int dy = (int) y;

        switch (direction) {
            case 2: // down (front)
                if (frontSprite != null) {
                    g2.drawImage(frontSprite, dx, dy, width, height, null);
                }
                break;
            case 0: // up (flip front vertically)
                if (frontSprite != null) {
                    g2.drawImage(frontSprite, dx, dy + height, width, -height, null);
                }
                break;
            case 1: // left
                if (leftSprite != null) {
                    g2.drawImage(leftSprite, dx, dy, width, height, null);
                }
                break;
            case 3: // right (flip left horizontally)
                if (leftSprite != null) {
                    g2.drawImage(leftSprite, dx + width, dy, -width, height, null);
                }
                break;
        }

        if (frontSprite == null && leftSprite == null) {
            g2.setColor(isRed ? Color.RED : Color.BLUE);
            g2.fillOval(dx, dy, width, height);
        }
    }
}
