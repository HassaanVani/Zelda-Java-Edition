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

    public ZeldaRoom(int roomX, int roomY) {
        this.roomX = roomX;
        this.roomY = roomY;
    }

    public void initialize(OverworldRenderer renderer, CollisionMap collision) {
        this.overworldRenderer = renderer;
        this.collisionMap = collision;
        if (!visited) {
            if (!isNoSpawnRoom()) spawnEnemies();
            visited = true;
        }
    }

    private boolean isNoSpawnRoom() {
        if (roomX == 7 && roomY == 7) return true;
        if (roomX == 7 && roomY == 3) return true;
        if (roomX == 7 && roomY == 6) return true;
        return false;
    }

    private void spawnEnemies() {
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
        for (int i = enemies.size() - 1; i >= 0; i--) {
            ZeldaEnemy e = enemies.get(i);
            e.update(player, this, projectiles);
            if (!e.isAlive()) {
                dropItem(e);
                enemies.remove(i);
            }
        }

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update();
            if (!p.isActive()) projectiles.remove(i);
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
        double roll = Math.random();
        if (roll < 0.25) {
            items.add(new Item(enemy.getX(), enemy.getY(), Item.ItemType.HEART));
        } else if (roll < 0.35) {
            items.add(new Item(enemy.getX(), enemy.getY(), Item.ItemType.RUPEE));
        } else if (roll < 0.40) {
            items.add(new Item(enemy.getX(), enemy.getY(), Item.ItemType.FIVE_RUPEES));
        }
    }

    public void render(Graphics2D g2) {
        if (overworldRenderer != null) {
            overworldRenderer.renderRoom(g2, roomX, roomY);
        }

        for (Item item : items) item.render(g2);
        for (ZeldaEnemy e : enemies) e.render(g2);
        for (Projectile p : projectiles) p.render(g2);
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
}
