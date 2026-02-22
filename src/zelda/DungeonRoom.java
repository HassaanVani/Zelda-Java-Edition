package zelda;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DungeonRoom {
    public static final int DOOR_NORTH = 0;
    public static final int DOOR_WEST = 1;
    public static final int DOOR_SOUTH = 2;
    public static final int DOOR_EAST = 3;

    public enum DoorState { NONE, OPEN, LOCKED, BOSS_LOCKED, BOMBABLE, BOMBED, SHUTTER }

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
    private boolean isLit = false;
    private float darkFadeAlpha = 1.0f;  // 1.0 = fully dark, 0.0 = fully lit
    private static final float FADE_SPEED = 0.025f;
    private boolean hasBlock = false;
    private boolean blockPushed = false;
    private double blockX = ZeldaRoom.ROOM_PIXEL_W / 2.0 - 8;
    private double blockY = ZeldaRoom.ROOM_PIXEL_H / 2.0 - 8;
    private double blockOrigX = blockX;
    private double blockOrigY = blockY;
    private int blockPushDir = -1; // -1=any, 0=N, 1=W, 2=S, 3=E
    private static final int BLOCK_SIZE = 16;
    private int dungeonNumber = 1;

    // Stairway support
    private boolean hasStairway = false;
    private boolean stairwayRevealed = false;
    private int stairTargetX = -1;
    private int stairTargetY = -1;

    private DungeonRenderer renderer;
    private CollisionMap collisionMap;
    private ItemDropSystem itemDropSystem;

    public void setItemDropSystem(ItemDropSystem ids) { this.itemDropSystem = ids; }

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
        if (ds == DoorState.SHUTTER) {
            // SHUTTER doors: passable before room is visited, or after room is cleared.
            // Between entry and clear, player is trapped.
            return !visited || cleared;
        }
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
            Inventory inv = player.getInventory();
            if (inv.hasBossKey(dungeonNumber)) {
                doors[doorPosition] = DoorState.OPEN;
                return true;
            }
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

    public void tryBombWalls(Projectile bomb) {
        int bx = (int)bomb.getX();
        int by = (int)bomb.getY();
        int margin = 24;
        // Check proximity to each wall
        if (by < margin) tryBombWall(DOOR_NORTH);
        if (by > ZeldaRoom.ROOM_PIXEL_H - margin) tryBombWall(DOOR_SOUTH);
        if (bx < margin) tryBombWall(DOOR_WEST);
        if (bx > ZeldaRoom.ROOM_PIXEL_W - margin) tryBombWall(DOOR_EAST);
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
            if (enemy != null) {
                if (enemy instanceof zelda.enemies.Vire) ((zelda.enemies.Vire) enemy).setRoomEnemies(enemies);
                if (enemy instanceof zelda.enemies.Zol) ((zelda.enemies.Zol) enemy).setRoomEnemies(enemies);
                enemies.add(enemy);
            }
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
            case "Gleeok2":     enemies.add(new zelda.bosses.Gleeok(bx, by, 2)); break;
            case "Gleeok3":     enemies.add(new zelda.bosses.Gleeok(bx, by, 3)); break;
            case "Gleeok4":     enemies.add(new zelda.bosses.Gleeok(bx, by, 4)); break;
            case "Digdogger":   enemies.add(new zelda.bosses.Digdogger(bx, by)); break;
            case "Gohma":       enemies.add(new zelda.bosses.Gohma(bx, by)); break;
            case "GohmaRed":    enemies.add(new zelda.bosses.Gohma(bx, by, false)); break;
            case "GohmaBlue":   enemies.add(new zelda.bosses.Gohma(bx, by, true)); break;
            case "Patra":       enemies.add(new zelda.bosses.Patra(bx, by)); break;
            case "Ganon":       enemies.add(new zelda.bosses.Ganon(bx, by)); break;
            default:            enemies.add(new zelda.bosses.Aquamentus(bx, by)); break;
        }
    }

    public void update(ZeldaPlayer player) {
        Inventory inv = player.getInventory();
        boolean frozen = inv.isEnemiesFrozen();
        inv.tickFreezeTimer();
        inv.tickSwordDisable();

        for (int i = enemies.size() - 1; i >= 0; i--) {
            ZeldaEnemy e = enemies.get(i);
            if (!frozen) e.update(player, null, projectiles);
            if (!e.isAlive()) {
                if (itemDropSystem != null) {
                    Item.ItemType drop = itemDropSystem.onEnemyKilled(e.getDropClass());
                    if (drop != null) items.add(new Item(e.getX(), e.getY(), drop));
                }
                enemies.remove(i);
            }
        }

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            boolean wasBefore = p.isActive();
            p.update();
            if (!p.isActive()) {
                if (wasBefore && p.doesLeaveFire()) {
                    Projectile fire = new Projectile(p.getX(), p.getY(), 0, 0, true);
                    fire.setColor(java.awt.Color.ORANGE);
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
                applyDungeonItem(item, player);
                items.remove(i);
            } else if (!item.isAlive()) {
                items.remove(i);
            }
        }

        if (enemies.isEmpty() && !cleared) {
            cleared = true;
            onRoomCleared();
        }

        // Push block logic
        if (hasBlock && !blockPushed && cleared) {
            updatePushBlock(player);
        }
    }

    private void updatePushBlock(ZeldaPlayer player) {
        Rectangle playerBox = player.getHitbox();
        Rectangle blockBox = new Rectangle((int)blockX, (int)blockY, BLOCK_SIZE, BLOCK_SIZE);

        if (!playerBox.intersects(blockBox)) return;

        // Determine push direction based on player position relative to block
        double dx = (blockX + BLOCK_SIZE / 2.0) - (player.getWorldX() + ZeldaPlayer.WIDTH / 2.0);
        double dy = (blockY + BLOCK_SIZE / 2.0) - (player.getWorldY() + ZeldaPlayer.HEIGHT / 2.0);

        int pushDir;
        if (Math.abs(dx) > Math.abs(dy)) {
            pushDir = (dx > 0) ? 3 : 1; // pushing east or west
        } else {
            pushDir = (dy > 0) ? 2 : 0; // pushing south or north
        }

        // Only allow push in specified direction (-1 = any direction)
        if (blockPushDir >= 0 && pushDir != blockPushDir) return;

        switch (pushDir) {
            case 0: blockY -= BLOCK_SIZE; break;
            case 1: blockX -= BLOCK_SIZE; break;
            case 2: blockY += BLOCK_SIZE; break;
            case 3: blockX += BLOCK_SIZE; break;
        }
        blockPushed = true;

        // Reveal stairway if room has one
        if (hasStairway) {
            stairwayRevealed = true;
        }
    }

    private void onRoomCleared() {
        // Open SHUTTER doors on room clear (not LOCKED — those require keys)
        for (int i = 0; i < 4; i++) {
            if (doors[i] == DoorState.SHUTTER) doors[i] = DoorState.OPEN;
        }
        // Boss room: drop heart container
        if (bossType != null) {
            items.add(new Item(ZeldaRoom.ROOM_PIXEL_W / 2.0, ZeldaRoom.ROOM_PIXEL_H / 2.0,
                Item.ItemType.HEART_CONTAINER));
        }
    }

    public void render(Graphics2D g2) {
        render(g2, -1, -1);
    }

    public void render(Graphics2D g2, int playerX, int playerY) {
        if (renderer != null) {
            renderer.renderRoom(g2, mapCol, mapRow);
        }
        for (Item item : items) item.render(g2);
        for (ZeldaEnemy e : enemies) e.render(g2);
        for (Projectile p : projectiles) p.render(g2);

        renderDoors(g2);

        // Stairway (revealed after block push)
        if (hasStairway && stairwayRevealed) {
            g2.setColor(new Color(20, 20, 20));
            g2.fillRect((int)blockOrigX, (int)blockOrigY, BLOCK_SIZE, BLOCK_SIZE);
            g2.setColor(new Color(60, 60, 60));
            // Draw stairway steps
            for (int i = 0; i < 4; i++) {
                int sy = (int)blockOrigY + i * 4;
                g2.fillRect((int)blockOrigX + i * 2, sy, BLOCK_SIZE - i * 4, 2);
            }
        }

        // Push block (uses dungeon palette)
        if (hasBlock) {
            Color wallColor = NESPalette.getDungeonWallColor(dungeonNumber);
            g2.setColor(blockPushed ? NESPalette.darken(wallColor, 0.4f) : wallColor);
            g2.fillRect((int)blockX, (int)blockY, BLOCK_SIZE, BLOCK_SIZE);
            if (!blockPushed) {
                g2.setColor(NESPalette.getDungeonDoorColor(dungeonNumber));
                g2.drawRect((int)blockX, (int)blockY, BLOCK_SIZE, BLOCK_SIZE);
            }
        }

        // Dark room overlay (show during fade-in too)
        if (isDark && darkFadeAlpha > 0) {
            renderDarkOverlay(g2, playerX, playerY);
        }

        // Boss room palette tint
        if (bossType != null) {
            Color[] bossPal = NESPalette.getBossPalette(bossType);
            if (bossPal != null && bossPal.length > 0) {
                g2.setColor(new Color(bossPal[0].getRed(), bossPal[0].getGreen(), bossPal[0].getBlue(), 25));
                g2.fillRect(0, 0, ZeldaRoom.ROOM_PIXEL_W, ZeldaRoom.ROOM_PIXEL_H);
            }
        }
    }

    private void renderDarkOverlay(Graphics2D g2, int px, int py) {
        int w = ZeldaRoom.ROOM_PIXEL_W;
        int h = ZeldaRoom.ROOM_PIXEL_H;

        if (isLit) {
            // Smooth fade-in: decrease darkness alpha each frame
            darkFadeAlpha = Math.max(0, darkFadeAlpha - FADE_SPEED);
            if (darkFadeAlpha <= 0) return; // fully lit, skip overlay
            // Uniform darkness that fades away
            int alpha = (int)(darkFadeAlpha * 255);
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRect(0, 0, w, h);
        } else {
            // Fully dark: small radius of light around player
            int radius = 28;
            for (int y = 0; y < h; y += 4) {
                for (int x = 0; x < w; x += 4) {
                    double dist = Math.sqrt((x - px) * (x - px) + (y - py) * (y - py));
                    if (dist > radius) {
                        float alpha = Math.min(1.0f, (float)((dist - radius) / 24.0));
                        g2.setColor(new Color(0, 0, 0, alpha));
                        g2.fillRect(x, y, 4, 4);
                    }
                }
            }
        }
    }

    public void lightRoom() {
        this.isLit = true;
        // darkFadeAlpha will smoothly decrease each frame in render
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
        Color wallColor = NESPalette.getDungeonWallColor(dungeonNumber);
        switch (doors[position]) {
            case LOCKED:
                g2.setColor(NESPalette.darken(wallColor, 0.7f));
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.YELLOW);
                g2.fillRect(x + w/2 - 3, y + h/2 - 3, 6, 6);
                break;
            case BOSS_LOCKED:
                g2.setColor(NESPalette.darken(wallColor, 0.5f));
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.RED);
                g2.fillRect(x + w/2 - 4, y + h/2 - 4, 8, 8);
                break;
            case BOMBABLE:
                g2.setColor(wallColor);
                g2.fillRect(x, y, w, h);
                break;
            case SHUTTER:
                // Closed shutter — solid wall with bar pattern
                g2.setColor(wallColor);
                g2.fillRect(x, y, w, h);
                g2.setColor(NESPalette.darken(wallColor, 0.6f));
                for (int i = 0; i < 4; i++) {
                    g2.fillRect(x + 2, y + 4 + i * 7, w - 4, 3);
                }
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
    public boolean isLit() { return isLit; }
    public boolean hasBlock() { return hasBlock; }
    public List<ZeldaEnemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }

    public void setEnemyTypes(String[] types) { this.enemyTypes = types; }
    public void setBossType(String type) { this.bossType = type; }
    public void setDark(boolean dark) { this.isDark = dark; }
    public void setHasBlock(boolean block) { this.hasBlock = block; }
    public void setBlockPushDir(int dir) { this.blockPushDir = dir; }
    public void setBlockPosition(double bx, double by) {
        this.blockX = bx; this.blockY = by;
        this.blockOrigX = bx; this.blockOrigY = by;
    }
    public void setStairway(boolean has, int targetX, int targetY) {
        this.hasStairway = has;
        this.stairTargetX = targetX;
        this.stairTargetY = targetY;
    }
    public void setDungeonNumber(int num) { this.dungeonNumber = num; }
    public boolean hasZeldaItem() { return hasItem && roomItem == Item.ItemType.ZELDA; }

    // Stairway access
    public boolean isOnStairway(ZeldaPlayer player) {
        if (!hasStairway || !stairwayRevealed) return false;
        Rectangle stairBox = new Rectangle((int)blockOrigX, (int)blockOrigY, BLOCK_SIZE, BLOCK_SIZE);
        return stairBox.intersects(player.getHitbox());
    }
    public boolean hasStairway() { return hasStairway; }
    public boolean isStairwayRevealed() { return stairwayRevealed; }
    public int getStairTargetX() { return stairTargetX; }
    public int getStairTargetY() { return stairTargetY; }

    private void applyDungeonItem(Item item, ZeldaPlayer player) {
        Inventory inv = player.getInventory();
        switch (item.getType()) {
            case MAP:
                inv.setHasMap(dungeonNumber, true);
                break;
            case COMPASS:
                inv.setHasCompass(dungeonNumber, true);
                break;
            case TRIFORCE:
                inv.setTriforce(dungeonNumber, true);
                break;
            case BOSS_KEY:
                inv.setHasBossKey(dungeonNumber, true);
                break;
            default:
                item.applyEffect(player);
                return;
        }
    }
}
