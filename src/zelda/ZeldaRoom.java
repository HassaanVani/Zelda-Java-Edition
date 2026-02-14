package zelda;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
                // If spawn position is in a wall, find a nearby walkable spot
                if (!isWalkable((int)sx + 8, (int)sy + 8)) {
                    double[] safe = findWalkableNear(sx, sy);
                    if (safe == null) continue; // skip if no walkable spot
                    sx = safe[0]; sy = safe[1];
                }
                ZeldaEnemy enemy = EnemyFactory.create(es.type, sx, sy);
                if (enemy != null) enemies.add(enemy);
            }
            return;
        }

        // Fallback: biome-based random spawning for rooms without data
        int count = MIN_ENEMIES + (int)(Math.random() * (MAX_ENEMIES - MIN_ENEMIES + 1));
        String biome = getBiome(roomX, roomY);

        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
                double x = SPAWN_MARGIN + Math.random() * (ROOM_PIXEL_W - SPAWN_MARGIN * 2);
                double y = SPAWN_MARGIN + Math.random() * (ROOM_PIXEL_H - SPAWN_MARGIN * 2);

                if (isWalkable((int) x + 8, (int) y + 8)) {
                    ZeldaEnemy enemy = createEnemyForBiome(biome, x, y);
                    if (enemy != null) enemies.add(enemy);
                    break;
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
                return Math.random() < 0.6
                    ? new zelda.enemies.Stalfos(x, y)
                    : new zelda.enemies.Leever(x, y, Math.random() < 0.3);
            case "lake":
                return Math.random() < 0.7
                    ? new zelda.enemies.Tektite(x, y, true)
                    : new zelda.enemies.Peahat(x, y);
            case "desert":
                return Math.random() < 0.6
                    ? new zelda.enemies.Leever(x, y, Math.random() < 0.3)
                    : new zelda.enemies.Peahat(x, y);
            case "mountain":
                return Math.random() < 0.5
                    ? new zelda.enemies.Tektite(x, y, false)
                    : new zelda.enemies.Octorok(x, y, true);
            default:
                double roll = Math.random();
                if (roll < 0.4) return new zelda.enemies.Octorok(x, y, false);
                if (roll < 0.7) return new zelda.enemies.Moblin(x, y, false);
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
            if (!frozen) e.update(player, this, projectiles);
            if (!e.isAlive()) {
                dropItem(e);
                enemies.remove(i);
            }
        }

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

        for (Item item : items) item.render(g2);
        for (ZeldaEnemy e : enemies) e.render(g2);
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
        return 7; // default
    }

    public int getDungeonEntranceTileY() {
        if (detectedEntranceTileY >= 0) return detectedEntranceTileY;
        return 3; // default
    }
}
