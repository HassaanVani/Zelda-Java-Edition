package zelda;

import java.awt.*;

public class Projectile {
    private double x, y;
    private double vx, vy;
    private int width = 8;
    private int height = 8;

    private boolean active = true;
    private boolean playerOwned;

    private int lifetime = 120;
    private Color color = Color.WHITE;
    private int damage = 1;
    private boolean piercing = false;

    // Boomerang return behavior
    private boolean returning = false;
    private ZeldaPlayer returnTarget = null;
    private int outboundDistance = 0;
    private int maxOutbound = 80; // ~5 tiles for wooden, full screen for magical
    private double returnSpeed = 3.0;

    // Bomb phase behavior (NES: $30, $18, $0C, $06 = 48, 24, 12, 6 frames)
    private boolean isBomb = false;
    private int bombPhase = 0; // 0=fuse, 1=flash, 2=explode, 3=fade
    private int bombTimer = 0;
    private static final int BOMB_FUSE = 48;    // $30
    private static final int BOMB_FLASH = 24;   // $18
    private static final int BOMB_EXPLODE = 12; // $0C
    private static final int BOMB_FADE = 6;     // $06
    private int explosionRadius = 24;

    // Candle fire: travels then becomes stationary flame
    private boolean isCandleFire = false;
    private int travelDistance = 0;
    private static final int CANDLE_TRAVEL_DIST = 48;
    private static final int STATIONARY_FLAME_LIFE = 60;

    public Projectile(double x, double y, double vx, double vy, boolean playerOwned) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.playerOwned = playerOwned;
        this.color = playerOwned ? Color.GREEN : Color.ORANGE;
    }

    public void update() {
        if (isBomb) {
            updateBomb();
            return;
        }

        // Candle fire: travel then stop and become stationary flame
        if (isCandleFire && !returning) {
            travelDistance += (int) Math.sqrt(vx * vx + vy * vy);
            if (travelDistance >= CANDLE_TRAVEL_DIST) {
                vx = 0;
                vy = 0;
                lifetime = STATIONARY_FLAME_LIFE;
                isCandleFire = false; // Now it's just a stationary flame
            }
        }

        if (returning && returnTarget != null) {
            // Return towards player
            double tx = returnTarget.getWorldX() + ZeldaPlayer.WIDTH / 2.0;
            double ty = returnTarget.getWorldY() + ZeldaPlayer.HEIGHT / 2.0;
            double dx = tx - (x + width / 2.0);
            double dy = ty - (y + height / 2.0);
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < returnSpeed * 2) {
                active = false; // caught
                return;
            }
            vx = (dx / dist) * returnSpeed;
            vy = (dy / dist) * returnSpeed;
        } else if (returnTarget != null) {
            // Outbound phase — track distance
            outboundDistance += (int) Math.sqrt(vx * vx + vy * vy);
            if (outboundDistance >= maxOutbound) {
                returning = true;
            }
        }

        x += vx;
        y += vy;
        lifetime--;
        if (lifetime <= 0) active = false;
        // Boomerangs don't die at screen edge — they return
        if (returnTarget != null) {
            if (x < -8 || x > 264 || y < -8 || y > 184) returning = true;
        } else {
            if (x < -8 || x > 264 || y < -8 || y > 184) active = false;
        }
    }

    private void updateBomb() {
        bombTimer++;
        switch (bombPhase) {
            case 0: // Fuse ($30 = 48 frames)
                if (bombTimer >= BOMB_FUSE) {
                    bombPhase = 1;
                    bombTimer = 0;
                }
                break;
            case 1: // Flash ($18 = 24 frames)
                if (bombTimer >= BOMB_FLASH) {
                    bombPhase = 2;
                    bombTimer = 0;
                    // Expand hitbox for explosion
                    x -= explosionRadius / 2.0;
                    y -= explosionRadius / 2.0;
                    width = explosionRadius;
                    height = explosionRadius;
                }
                break;
            case 2: // Explode ($0C = 12 frames)
                if (bombTimer >= BOMB_EXPLODE) {
                    bombPhase = 3;
                    bombTimer = 0;
                }
                break;
            case 3: // Fade ($06 = 6 frames)
                if (bombTimer >= BOMB_FADE) {
                    active = false;
                }
                break;
        }
    }

    public void render(Graphics2D g2) {
        if (!active) return;

        if (isBomb) {
            renderBomb(g2);
            return;
        }

        g2.setColor(color);
        g2.fillOval((int)x, (int)y, width, height);
        g2.setColor(Color.WHITE);
        g2.drawOval((int)x, (int)y, width, height);
    }

    private void renderBomb(Graphics2D g2) {
        switch (bombPhase) {
            case 0: // Fuse — draw bomb
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval((int)x, (int)y, width, height);
                g2.setColor(Color.ORANGE);
                g2.fillRect((int)x + width / 2 - 1, (int)y - 3, 2, 4);
                break;
            case 1: // Flash — white blink
                g2.setColor((bombTimer / 2) % 2 == 0 ? Color.WHITE : Color.YELLOW);
                g2.fillOval((int)x - 2, (int)y - 2, width + 4, height + 4);
                break;
            case 2: // Explode — expanding ring
                float alpha = 1.0f - (float) bombTimer / BOMB_EXPLODE;
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, alpha)));
                g2.setColor(Color.ORANGE);
                g2.fillOval((int)x, (int)y, width, height);
                g2.setColor(Color.YELLOW);
                g2.fillOval((int)x + 4, (int)y + 4, width - 8, height - 8);
                g2.setComposite(old);
                break;
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isActive() { return active; }
    public boolean isPlayerProjectile() { return playerOwned; }
    public boolean isPlayerOwned() { return playerOwned; }
    public void deactivate() { active = false; }

    public Color getColor() { return color; }
    public void setColor(Color c) { color = c; }
    public boolean isMoving() { return vx != 0 || vy != 0; }
    public void setSize(int w, int h) { width = w; height = h; }
    public int getDamage() { return damage; }
    public void setDamage(int d) { damage = d; }
    public boolean isPiercing() { return piercing; }
    public void setPiercing(boolean p) { piercing = p; }

    // Boomerang return
    public void setReturnTarget(ZeldaPlayer target, int maxDist) {
        this.returnTarget = target;
        this.maxOutbound = maxDist;
        this.returnSpeed = Math.sqrt(vx * vx + vy * vy);
    }
    public boolean isReturning() { return returning; }

    // Bomb phases
    public void setIsBomb(boolean b) { this.isBomb = b; }
    public boolean isBomb() { return isBomb; }
    public int getBombPhase() { return bombPhase; }

    // Rod + Book: leave fire where beam hits
    private boolean leaveFire = false;
    public void setLeaveFire(boolean f) { this.leaveFire = f; }
    public boolean doesLeaveFire() { return leaveFire; }

    // Candle fire
    public void setCandleFire(boolean cf) { this.isCandleFire = cf; }
    public boolean isCandleFire() { return isCandleFire; }

    // Boomerang type flag (to prevent multiple active boomerangs)
    private boolean boomerangType = false;
    public void setBoomerangType(boolean b) { this.boomerangType = b; }
    public boolean isBoomerangType() { return boomerangType; }

    // Unblockable projectiles (Wizzrobe magic, statue fire) bypass all shields
    private boolean unblockable = false;
    public void setUnblockable(boolean u) { this.unblockable = u; }
    public boolean isUnblockable() { return unblockable; }
}
