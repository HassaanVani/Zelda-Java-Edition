package zelda.enemies;

import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.ImageIcon;
import zelda.*;

public class Moblin extends ZeldaEnemy {
    private static final int SHOOT_RANGE = 80;
    private static final int SHOOT_COOLDOWN = 100;
    private static final double SPEAR_SPEED = 2.5;

    private boolean isRed;
    private int shootTimer = 0;
    private Image frontSprite;
    private Image backSprite;
    private Image leftSprite;

    public Moblin(double x, double y, boolean red) {
        super(x, y, 1, AIType.SHOOTER);
        this.isRed = red;
        applyStats(red ? EnemyStats.moblinRed() : EnemyStats.moblinBlue());
        loadMoblinSprites();
    }

    private void loadMoblinSprites() {
        String color = isRed ? "Red" : "Black";
        frontSprite = loadGif("sprites/Enemies/Moblin - " + color + " (Front).gif");
        backSprite = loadGif("sprites/Enemies/Moblin - " + color + " (Back).gif");
        leftSprite = loadGif("sprites/Enemies/Moblin - " + color + " (Left).gif");
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
            shootSpear(projectiles);
            shootTimer = SHOOT_COOLDOWN;
        }
    }

    private void shootSpear(List<Projectile> projectiles) {
        double vx = 0, vy = 0;
        switch (direction) {
            case 0: vy = -SPEAR_SPEED; break;
            case 1: vx = -SPEAR_SPEED; break;
            case 2: vy = SPEAR_SPEED; break;
            case 3: vx = SPEAR_SPEED; break;
        }
        Projectile p = new Projectile(x + width/2, y + height/2, vx, vy, false);
        p.setColor(isRed ? new Color(180, 56, 0) : new Color(80, 80, 80));
        p.setSize(4, 12);
        p.setDamage(EnemyStats.SPEAR_DAMAGE);
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
            case 2: // down
                if (frontSprite != null) g2.drawImage(frontSprite, dx, dy, width, height, null);
                break;
            case 0: // up
                if (backSprite != null) g2.drawImage(backSprite, dx, dy, width, height, null);
                break;
            case 1: // left
                if (leftSprite != null) g2.drawImage(leftSprite, dx, dy, width, height, null);
                break;
            case 3: // right (flip left)
                if (leftSprite != null) g2.drawImage(leftSprite, dx + width, dy, -width, height, null);
                break;
        }

        if (frontSprite == null && leftSprite == null && backSprite == null) {
            g2.setColor(isRed ? Color.RED : Color.DARK_GRAY);
            g2.fillRect(dx, dy, width, height);
        }
    }
}
