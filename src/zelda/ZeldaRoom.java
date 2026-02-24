package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ZeldaRoom {
    public static final int TILES_X = 16;
    public static final int TILES_Y = 11;
    public static final int TILE_SIZE = 16;
    public static final int ROOM_PIXEL_W = TILES_X * TILE_SIZE;
    public static final int ROOM_PIXEL_H = TILES_Y * TILE_SIZE;

    public static final int ROOM_PLAY_LEFT = TILE_SIZE;
    public static final int ROOM_PLAY_TOP = TILE_SIZE;
    public static final int ROOM_PLAY_RIGHT = ROOM_PIXEL_W - TILE_SIZE;
    public static final int ROOM_PLAY_BOTTOM = ROOM_PIXEL_H - TILE_SIZE;

    public static final int MIN_ENEMIES = 2;
    public static final int MAX_ENEMIES = 4;
    public static final int SPAWN_MARGIN = 32;
    private static final int SPAWN_ATTEMPTS = 20;

    private int roomX, roomY;
    private List<ZeldaEnemy> enemies = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private boolean visited = false;
    private boolean cleared = false;

    private OverworldRenderer overworldRenderer;
    private CollisionMap collisionMap;
    private RoomData.RoomDef roomDef;
    private ItemDropSystem itemDropSystem;

    private static AudioManager audioManager;
    public static void setAudioManager(AudioManager am) { audioManager = am; }

    // Enemy death poof animation
    private static BufferedImage deathEffectSheet;
    private static boolean deathEffectLoaded = false;
    private static final int DEATH_FRAMES = 4;
    private static final int DEATH_TICKS_PER_FRAME = 4;
    private static final int DEATH_DURATION = DEATH_FRAMES * DEATH_TICKS_PER_FRAME;
    private List<int[]> deathEffects = new ArrayList<>(); // {x, y, timer}

    // Auto-detected entrance tile from sprite map (overrides hardcoded positions)
    private int detectedEntranceTileX = -1;
    private int detectedEntranceTileY = -1;

    public ZeldaRoom(int roomX, int roomY) {
        this.roomX = roomX;
        this.roomY = roomY;
    }

    public void initialize(OverworldRenderer renderer, CollisionMap collision) {
        this.overworldRenderer = renderer;
        this.collisionMap = collision;
        this.roomDef = RoomData.getRoomDef(roomX, roomY);

        // Auto-detect entrance from sprite map for rooms with caves or dungeons
        if (roomDef != null && (roomDef.caveId >= 0 || roomDef.dungeonId >= 0)) {
            int[] detected = collision.getDetectedEntrance(roomX, roomY);
            if (detected != null) {
                detectedEntranceTileX = detected[0];
                detectedEntranceTileY = detected[1];
            }
        }

        if (!visited) {
            spawnEnemies();
            visited = true;
        }
    }

    private void spawnEnemies() {
        // Use RoomData if available
        if (roomDef != null) {
            if (roomDef.noEnemies) return;
            for (RoomData.EnemySpawn es : roomDef.enemies) {
                double sx = es.x, sy = es.y;
                boolean isWaterEnemy = es.type.equals("Zola");
                if (isWaterEnemy) {
                    // Water enemies need WATER tiles, not walkable land
                    TileType tile = getTileAt((int)sx + 8, (int)sy + 8);
                    if (tile != TileType.WATER) {
                        double[] waterSpot = findWaterNear(sx, sy);
                        if (waterSpot == null) continue;
                        sx = waterSpot[0]; sy = waterSpot[1];
                    }
                } else {
                    // Land enemies need walkable tiles
                    if (!isWalkable((int)sx + 8, (int)sy + 8)) {
                        double[] safe = findWalkableNear(sx, sy);
                        if (safe == null) continue;
                        sx = safe[0]; sy = safe[1];
                    }
                }
                ZeldaEnemy enemy = EnemyFactory.create(es.type, sx, sy);
                if (enemy != null) enemies.add(enemy);
            }
            return;
        }

        // Fallback: biome-based random spawning for rooms without data
        int count = MIN_ENEMIES + (int)(Math.random() * (MAX_ENEMIES - MIN_ENEMIES + 1));
        String biome = getBiome(roomX, roomY);
        boolean isWaterBiome = biome.equals("lake");

        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
                double x = SPAWN_MARGIN + Math.random() * (ROOM_PIXEL_W - SPAWN_MARGIN * 2);
                double y = SPAWN_MARGIN + Math.random() * (ROOM_PIXEL_H - SPAWN_MARGIN * 2);

                if (isWaterBiome) {
                    // Water enemies (Zola) need water tiles to spawn in
                    TileType tile = getTileAt((int)x + 8, (int)y + 8);
                    if (tile == TileType.WATER) {
                        ZeldaEnemy enemy = createEnemyForBiome(biome, x, y);
                        if (enemy != null) enemies.add(enemy);
                        break;
                    }
                } else {
                    if (isWalkable((int) x + 8, (int) y + 8)) {
                        ZeldaEnemy enemy = createEnemyForBiome(biome, x, y);
                        if (enemy != null) enemies.add(enemy);
                        break;
                    }
                }
            }
        }
    }

    private String getBiome(int rx, int ry) {
        if (ry <= 1 && rx >= 3 && rx <= 8) return "forest";
        if (ry >= 5 && rx >= 8) return "graveyard";
        if (ry <= 2 && rx >= 12) return "lake";
        if (ry >= 3 && rx <= 3) return "desert";
        if (ry >= 5) return "mountain";
        return "field";
    }

    private ZeldaEnemy createEnemyForBiome(String biome, double x, double y) {
        switch (biome) {
            case "forest":
                return Math.random() < 0.5
                    ? new zelda.enemies.Octorok(x, y, Math.random() < 0.3)
                    : new zelda.enemies.Moblin(x, y, Math.random() < 0.3);
            case "graveyard":
                return new zelda.enemies.Ghini(x, y);
            case "lake":
                return new zelda.enemies.Zola(x, y);
            case "desert":
                return Math.random() < 0.6
                    ? new zelda.enemies.Leever(x, y, Math.random() < 0.3)
                    : new zelda.enemies.Peahat(x, y);
            case "mountain":
                double roll = Math.random();
                if (roll < 0.4) return new zelda.enemies.Tektite(x, y, false);
                if (roll < 0.7) return new zelda.enemies.Octorok(x, y, true);
                return new zelda.enemies.Lynel(x, y, Math.random() < 0.3);
            default:
                double fieldRoll = Math.random();
                if (fieldRoll < 0.4) return new zelda.enemies.Octorok(x, y, false);
                if (fieldRoll < 0.7) return new zelda.enemies.Moblin(x, y, false);
                return new zelda.enemies.Tektite(x, y, false);
        }
    }

    public void update(ZeldaPlayer player) {
        Inventory inv = player.getInventory();
        boolean frozen = inv.isEnemiesFrozen();
        inv.tickFreezeTimer();
        inv.tickSwordDisable();

        for (int i = enemies.size() - 1; i >= 0; i--) {
            ZeldaEnemy e = enemies.get(i);
            if (!frozen) {
                if (!e.processKnockback()) {
                    e.update(player, this, projectiles);
                }
            }
            if (!e.isAlive()) {
                spawnDeathEffect(e.getX(), e.getY());
                dropItem(e);
                enemies.remove(i);
                if (audioManager != null) audioManager.playSFX("Enemy Killed.wav");
            }
        }

        updateDeathEffects();

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            boolean wasBefore = p.isActive();
            p.update();
            if (!p.isActive()) {
                // Rod + Book: spawn fire where beam hit
                if (wasBefore && p.doesLeaveFire()) {
                    Projectile fire = new Projectile(p.getX(), p.getY(), 0, 0, true);
                    fire.setColor(Color.ORANGE);
                    fire.setSize(8, 8);
                    fire.setDamage(1);
                    projectiles.add(fire);
                }
                projectiles.remove(i);
            }
        }

        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            item.update();
            if (item.intersects(player.getHitbox())) {
                item.applyEffect(player);
                items.remove(i);
            } else if (!item.isAlive()) {
                items.remove(i);
            }
        }

        if (enemies.isEmpty()) cleared = true;
    }

    private void dropItem(ZeldaEnemy enemy) {
        if (itemDropSystem == null) return;
        Item.ItemType drop = itemDropSystem.onEnemyKilled(enemy.getDropClass());
        if (drop != null) {
            items.add(new Item(enemy.getX(), enemy.getY(), drop));
        }
    }

    public void setItemDropSystem(ItemDropSystem ids) { this.itemDropSystem = ids; }

    private void spawnDeathEffect(double ex, double ey) {
        if (!deathEffectLoaded) {
            deathEffectLoaded = true;
            try {
                File f = new File("sprites/Effects/enemy_death.png");
                if (f.exists()) deathEffectSheet = ImageIO.read(f);
            } catch (Exception e) {}
        }
        deathEffects.add(new int[]{(int)ex, (int)ey, 0});
    }

    private void updateDeathEffects() {
        for (int i = deathEffects.size() - 1; i >= 0; i--) {
            deathEffects.get(i)[2]++;
            if (deathEffects.get(i)[2] >= DEATH_DURATION) {
                deathEffects.remove(i);
            }
        }
    }

    private void renderDeathEffects(Graphics2D g2) {
        if (deathEffectSheet == null) return;
        int fw = deathEffectSheet.getWidth() / DEATH_FRAMES;
        int fh = deathEffectSheet.getHeight();
        for (int[] de : deathEffects) {
            int frame = de[2] / DEATH_TICKS_PER_FRAME;
            if (frame >= DEATH_FRAMES) continue;
            int sx = frame * fw;
            g2.drawImage(deathEffectSheet, de[0], de[1], de[0] + 16, de[1] + 16,
                          sx, 0, sx + fw, fh, null);
        }
    }

    private static int globalFrameCounter = 0;

    public void render(Graphics2D g2) {
        globalFrameCounter++;

        if (overworldRenderer != null) {
            overworldRenderer.renderRoom(g2, roomX, roomY);

            // Water shimmer palette cycling (NES-style)
            if (collisionMap != null) {
                renderWaterShimmer(g2);
            }
        }

        // Render revealed secret entrance (stairway/cave opening)
        if (secretRevealed && revealedSecret != null) {
            int sx = revealedSecret.tileX * TILE_SIZE;
            int sy = revealedSecret.tileY * TILE_SIZE;
            // Dark stairway entrance
            g2.setColor(Color.BLACK);
            g2.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);
            // Step pattern
            g2.setColor(new Color(80, 80, 80));
            g2.fillRect(sx + 2, sy + 4, TILE_SIZE - 4, 3);
            g2.fillRect(sx + 4, sy + 9, TILE_SIZE - 8, 3);
        }

        for (Item item : items) item.render(g2);
        for (ZeldaEnemy e : enemies) e.render(g2);
        renderDeathEffects(g2);
        for (Projectile p : projectiles) p.render(g2);
    }

    private void renderWaterShimmer(Graphics2D g2) {
        int phase = (globalFrameCounter / 12) % 3;
        int alpha;
        int r, gr, b;
        switch (phase) {
            case 0: r = 55; gr = 172; b = 255; alpha = 25; break;
            case 1: r = 78; gr = 142; b = 255; alpha = 30; break;
            case 2: r = 56; gr = 222; b = 206; alpha = 20; break;
            default: r = 55; gr = 172; b = 255; alpha = 25; break;
        }
        Color shimmer = new Color(r, gr, b, alpha);

        for (int ty = 0; ty < TILES_Y; ty++) {
            for (int tx = 0; tx < TILES_X; tx++) {
                TileType tt = collisionMap.getTileType(roomX, roomY, tx, ty);
                if (tt == TileType.WATER) {
                    g2.setColor(shimmer);
                    g2.fillRect(tx * TILE_SIZE, ty * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    /** Search nearby tiles in expanding rings to find a WATER tile for water enemies. */
    private double[] findWaterNear(double origX, double origY) {
        for (int radius = 1; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;
                    double nx = origX + dx * TILE_SIZE;
                    double ny = origY + dy * TILE_SIZE;
                    if (nx < TILE_SIZE || nx > ROOM_PIXEL_W - TILE_SIZE) continue;
                    if (ny < TILE_SIZE || ny > ROOM_PIXEL_H - TILE_SIZE) continue;
                    TileType tile = getTileAt((int)nx + 8, (int)ny + 8);
                    if (tile == TileType.WATER) {
                        return new double[]{nx, ny};
                    }
                }
            }
        }
        return null;
    }

    /** Search nearby tiles in expanding rings to find a walkable spawn position. */
    private double[] findWalkableNear(double origX, double origY) {
        for (int radius = 1; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;
                    double nx = origX + dx * TILE_SIZE;
                    double ny = origY + dy * TILE_SIZE;
                    if (nx < SPAWN_MARGIN || nx > ROOM_PIXEL_W - SPAWN_MARGIN) continue;
                    if (ny < SPAWN_MARGIN || ny > ROOM_PIXEL_H - SPAWN_MARGIN) continue;
                    if (isWalkable((int)nx + 8, (int)ny + 8)) {
                        return new double[]{nx, ny};
                    }
                }
            }
        }
        return null;
    }

    public boolean isWalkable(int pixelX, int pixelY) {
        if (collisionMap == null) return true;
        return collisionMap.isWalkable(roomX, roomY, pixelX, pixelY);
    }

    public TileType getTileAt(int pixelX, int pixelY) {
        if (collisionMap == null) return TileType.FLOOR;
        int tileX = pixelX / TILE_SIZE;
        int tileY = pixelY / TILE_SIZE;
        return collisionMap.getTileType(roomX, roomY, tileX, tileY);
    }

    public int getRoomX() { return roomX; }
    public int getRoomY() { return roomY; }
    public List<ZeldaEnemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Item> getItems() { return items; }
    public boolean isCleared() { return cleared; }
    public boolean isVisited() { return visited; }
    public RoomData.RoomDef getRoomDef() { return roomDef; }

    public boolean hasCaveEntrance() {
        return roomDef != null && roomDef.caveId >= 0;
    }

    public int getCaveId() {
        return roomDef != null ? roomDef.caveId : -1;
    }

    public int getCaveTileX() {
        if (detectedEntranceTileX >= 0) return detectedEntranceTileX;
        return roomDef != null ? roomDef.caveTileX : -1;
    }

    public int getCaveTileY() {
        if (detectedEntranceTileY >= 0) return detectedEntranceTileY;
        return roomDef != null ? roomDef.caveTileY : -1;
    }

    public boolean hasDungeonEntrance() {
        return roomDef != null && roomDef.dungeonId >= 0;
    }

    public int getDungeonId() {
        return roomDef != null ? roomDef.dungeonId : -1;
    }

    public int getDungeonEntranceTileX() {
        if (detectedEntranceTileX >= 0) return detectedEntranceTileX;
        // Use hardcoded position from RoomData if available (stored in caveTileX)
        if (roomDef != null && roomDef.caveTileX >= 0) return roomDef.caveTileX;
        return 7; // default
    }

    public int getDungeonEntranceTileY() {
        if (detectedEntranceTileY >= 0) return detectedEntranceTileY;
        // Use hardcoded position from RoomData if available (stored in caveTileY)
        if (roomDef != null && roomDef.caveTileY >= 0) return roomDef.caveTileY;
        return 3; // default
    }

    // ==================== Secret Trigger System ====================

    private boolean secretRevealed = false;
    private RoomData.RoomSecret revealedSecret = null;

    /**
     * Check if a bomb explosion triggers a BOMB_WALL secret.
     * Called when a bomb explodes in this room.
     */
    public boolean checkBombSecret(double bombX, double bombY) {
        if (secretRevealed || roomDef == null || roomDef.secrets == null) return false;
        int bombTX = (int)(bombX / TILE_SIZE);
        int bombTY = (int)(bombY / TILE_SIZE);
        for (RoomData.RoomSecret s : roomDef.secrets) {
            if (s.type == RoomData.SecretType.BOMB_WALL) {
                if (Math.abs(bombTX - s.tileX) <= 2 && Math.abs(bombTY - s.tileY) <= 2) {
                    secretRevealed = true;
                    revealedSecret = s;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if candle fire triggers a BURN_BUSH secret.
     * Called when fire hits or stops at a position.
     */
    public boolean checkCandleSecret(double fireX, double fireY) {
        if (secretRevealed || roomDef == null || roomDef.secrets == null) return false;
        int fireTX = (int)(fireX / TILE_SIZE);
        int fireTY = (int)(fireY / TILE_SIZE);
        for (RoomData.RoomSecret s : roomDef.secrets) {
            if (s.type == RoomData.SecretType.BURN_BUSH) {
                if (Math.abs(fireTX - s.tileX) <= 1 && Math.abs(fireTY - s.tileY) <= 1) {
                    secretRevealed = true;
                    revealedSecret = s;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if all enemies killed triggers a KILL_ALL secret.
     * Called each frame once room is cleared. Also works for BOMB_WALL/BURN_BUSH
     * gated behind enemy kills (the standard overworld KILL_ALL reveals an item or passage).
     */
    public boolean checkKillAllSecret() {
        if (secretRevealed || roomDef == null || roomDef.secrets == null) return false;
        if (!cleared) return false;
        for (RoomData.RoomSecret s : roomDef.secrets) {
            // Skip types that require specific interaction (push, bomb, burn, recorder)
            if (s.type == RoomData.SecretType.PUSH_ROCK
                || s.type == RoomData.SecretType.PUSH_GRAVE
                || s.type == RoomData.SecretType.BOMB_WALL
                || s.type == RoomData.SecretType.BURN_BUSH
                || s.type == RoomData.SecretType.RECORDER
                || s.type == RoomData.SecretType.RAFT_DOCK
                || s.type == RoomData.SecretType.FAIRY_FOUNTAIN) {
                continue;
            }
            // This is a kill-all triggered secret
            secretRevealed = true;
            revealedSecret = s;
            return true;
        }
        return false;
    }

    /**
     * Check if player pushing against a PUSH_ROCK or PUSH_GRAVE tile triggers a secret.
     * @param px player world X
     * @param py player world Y
     * @param pushDir direction player is pushing (0=up, 1=left, 2=down, 3=right)
     * @param hasBracelet true if player has Power Bracelet (needed for rocks)
     */
    public boolean checkPushSecret(double px, double py, int pushDir, boolean hasBracelet) {
        if (secretRevealed || roomDef == null || roomDef.secrets == null) return false;
        int ptx = (int)(px / TILE_SIZE);
        int pty = (int)(py / TILE_SIZE);
        for (RoomData.RoomSecret s : roomDef.secrets) {
            if (s.type == RoomData.SecretType.PUSH_ROCK) {
                if (!hasBracelet) continue; // Need Power Bracelet for rocks
                // Check if player is adjacent to the secret tile and pushing toward it
                if (isAdjacentPushing(ptx, pty, s.tileX, s.tileY, pushDir)) {
                    secretRevealed = true;
                    revealedSecret = s;
                    return true;
                }
            } else if (s.type == RoomData.SecretType.PUSH_GRAVE) {
                // Gravestones can be pushed without bracelet (in specific direction: up)
                if (pushDir == 0 && isAdjacentPushing(ptx, pty, s.tileX, s.tileY, pushDir)) {
                    secretRevealed = true;
                    revealedSecret = s;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAdjacentPushing(int ptx, int pty, int stx, int sty, int pushDir) {
        switch (pushDir) {
            case 0: return ptx == stx && pty == sty + 1; // pushing up: player below target
            case 1: return pty == sty && ptx == stx + 1; // pushing left: player right of target
            case 2: return ptx == stx && pty == sty - 1; // pushing down: player above target
            case 3: return pty == sty && ptx == stx - 1; // pushing right: player left of target
        }
        return false;
    }

    public boolean isSecretRevealed() { return secretRevealed; }
    public RoomData.RoomSecret getRevealedSecret() { return revealedSecret; }

    /**
     * Check if a fairy fountain is at the player's position.
     */
    public boolean isAtFairyFountain(double px, double py) {
        if (roomDef == null || roomDef.secrets == null) return false;
        int ptx = (int)(px / TILE_SIZE);
        int pty = (int)(py / TILE_SIZE);
        for (RoomData.RoomSecret s : roomDef.secrets) {
            if (s.type == RoomData.SecretType.FAIRY_FOUNTAIN) {
                if (Math.abs(ptx - s.tileX) <= 1 && Math.abs(pty - s.tileY) <= 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
