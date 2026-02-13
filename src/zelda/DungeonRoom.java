package zelda;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DungeonRoom {
    public static final int DOOR_NORTH = 0;
    public static final int DOOR_WEST = 1;
    public static final int DOOR_SOUTH = 2;
    public static final int DOOR_EAST = 3;

    public enum DoorState { NONE, OPEN, LOCKED, BOSS_LOCKED, BOMBABLE, BOMBED }

    private int localX, localY;
    private int mapCol, mapRow;
    private DoorState[] doors = { DoorState.NONE, DoorState.NONE, DoorState.NONE, DoorState.NONE };

    private List<ZeldaEnemy> enemies = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private boolean visited = false;
    private boolean cleared = false;
    private boolean hasItem = false;
    private Item.ItemType roomItem = null;

    private String[] enemyTypes = new String[0];
    private String bossType = null;
    private boolean isDark = false;
    private boolean hasBlock = false;

    private DungeonRenderer renderer;
    private CollisionMap collisionMap;

    public DungeonRoom(int localX, int localY, int mapCol, int mapRow) {
        this.localX = localX;
        this.localY = localY;
        this.mapCol = mapCol;
        this.mapRow = mapRow;
    }

    public void setDoor(int position, DoorState state) {
        doors[position] = state;
    }

    public DoorState getDoor(int position) { return doors[position]; }

    public boolean canPass(int doorPosition) {
        DoorState ds = doors[doorPosition];
        return ds == DoorState.OPEN || ds == DoorState.BOMBED;
    }

    public boolean tryUnlock(int doorPosition, ZeldaPlayer player) {
        DoorState ds = doors[doorPosition];
        if (ds == DoorState.LOCKED) {
            Inventory inv = player.getInventory();
            if (inv.useKey()) {
                doors[doorPosition] = DoorState.OPEN;
                return true;
            }
        } else if (ds == DoorState.BOSS_LOCKED) {
            // Boss key unlocks boss doors (tracked per-dungeon in Inventory)
            doors[doorPosition] = DoorState.OPEN;
            return true;
        }
        return false;
    }

    public boolean tryBombWall(int doorPosition) {
        if (doors[doorPosition] == DoorState.BOMBABLE) {
            doors[doorPosition] = DoorState.BOMBED;
            return true;
        }
        return false;
    }

    public void setRoomItem(Item.ItemType type) {
        this.hasItem = true;
        this.roomItem = type;
    }

    public void initialize(DungeonRenderer r, CollisionMap c) {
        this.renderer = r;
        this.collisionMap = c;
    }

    public void enter() {
        if (!visited) {
            visited = true;
            spawnEnemies();
            if (bossType != null) {
                spawnBoss();
            }
            if (hasItem && roomItem != null) {
                items.add(new Item(ZeldaRoom.ROOM_PIXEL_W / 2, ZeldaRoom.ROOM_PIXEL_H / 2, roomItem));
            }
        }
    }

    private void spawnEnemies() {
        if (enemyTypes == null || enemyTypes.length == 0) return;
        for (int i = 0; i < enemyTypes.length; i++) {
            double x = 48 + Math.random() * 160;
            double y = 48 + Math.random() * 80;
            ZeldaEnemy enemy = EnemyFactory.create(enemyTypes[i], x, y);
            if (enemy != null) enemies.add(enemy);
        }
    }

    private void spawnBoss() {
        double bx = ZeldaRoom.ROOM_PIXEL_W / 2.0;
        double by = ZeldaRoom.ROOM_PIXEL_H / 2.0 - 16;
        switch (bossType) {
            case "Aquamentus":  enemies.add(new zelda.bosses.Aquamentus(bx, by)); break;
            case "Dodongo":     enemies.add(new zelda.bosses.Dodongo(bx, by)); break;
            case "Manhandla":   enemies.add(new zelda.bosses.Manhandla(bx, by)); break;
            case "Gleeok":      enemies.add(new zelda.bosses.Gleeok(bx, by)); break;
            case "Digdogger":   enemies.add(new zelda.bosses.Digdogger(bx, by)); break;
            case "Gohma":       enemies.add(new zelda.bosses.Gohma(bx, by)); break;
            case "Ganon":       enemies.add(new zelda.bosses.Ganon(bx, by)); break;
            default:            enemies.add(new zelda.bosses.Aquamentus(bx, by)); break;
        }
    }

    public void update(ZeldaPlayer player) {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            ZeldaEnemy e = enemies.get(i);
            e.update(player, null, projectiles);
            if (!e.isAlive()) {
                double roll = Math.random();
                if (roll < 0.3) items.add(new Item(e.getX(), e.getY(), Item.ItemType.HEART));
                else if (roll < 0.4) items.add(new Item(e.getX(), e.getY(), Item.ItemType.RUPEE));
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

        if (enemies.isEmpty() && !cleared) {
            cleared = true;
            onRoomCleared();
        }
    }

    private void onRoomCleared() {
        // Open shut doors on room clear
        for (int i = 0; i < 4; i++) {
            if (doors[i] == DoorState.LOCKED) doors[i] = DoorState.OPEN;
        }
        // Boss room: drop heart container
        if (bossType != null) {
            items.add(new Item(ZeldaRoom.ROOM_PIXEL_W / 2.0, ZeldaRoom.ROOM_PIXEL_H / 2.0,
                Item.ItemType.HEART_CONTAINER));
        }
    }

    public void render(Graphics2D g2) {
        if (renderer != null) {
            renderer.renderRoom(g2, mapCol, mapRow);
        }
        for (Item item : items) item.render(g2);
        for (ZeldaEnemy e : enemies) e.render(g2);
        for (Projectile p : projectiles) p.render(g2);

        renderDoors(g2);
    }

    private void renderDoors(Graphics2D g2) {
        int midX = ZeldaRoom.ROOM_PIXEL_W / 2 - 16;
        int midY = ZeldaRoom.ROOM_PIXEL_H / 2 - 16;
        int doorSize = 32;

        renderSingleDoor(g2, DOOR_NORTH, midX, 0, doorSize, doorSize);
        renderSingleDoor(g2, DOOR_SOUTH, midX, ZeldaRoom.ROOM_PIXEL_H - doorSize, doorSize, doorSize);
        renderSingleDoor(g2, DOOR_WEST, 0, midY, doorSize, doorSize);
        renderSingleDoor(g2, DOOR_EAST, ZeldaRoom.ROOM_PIXEL_W - doorSize, midY, doorSize, doorSize);
    }

    private void renderSingleDoor(Graphics2D g2, int position, int x, int y, int w, int h) {
        switch (doors[position]) {
            case LOCKED:
                g2.setColor(new Color(120, 80, 40));
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.YELLOW);
                g2.fillRect(x + w/2 - 3, y + h/2 - 3, 6, 6);
                break;
            case BOSS_LOCKED:
                g2.setColor(new Color(100, 20, 20));
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.RED);
                g2.fillRect(x + w/2 - 4, y + h/2 - 4, 8, 8);
                break;
            case BOMBABLE:
                // Appears as a normal wall — no visual hint
                break;
            case NONE:
                break;
            default:
                break;
        }
    }

    public int getLocalX() { return localX; }
    public int getLocalY() { return localY; }
    public int getMapCol() { return mapCol; }
    public int getMapRow() { return mapRow; }
    public boolean isCleared() { return cleared; }
    public boolean isVisited() { return visited; }
    public boolean isDark() { return isDark; }
    public boolean hasBlock() { return hasBlock; }
    public List<ZeldaEnemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }

    public void setEnemyTypes(String[] types) { this.enemyTypes = types; }
    public void setBossType(String type) { this.bossType = type; }
    public void setDark(boolean dark) { this.isDark = dark; }
    public void setHasBlock(boolean block) { this.hasBlock = block; }
}
