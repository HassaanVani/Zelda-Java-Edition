package zelda;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

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

    private static AudioManager audioManager;
    public static void setAudioManager(AudioManager am) { audioManager = am; }
    private int dungeonNumber = 1;

    // Enemy death poof animation
    private static BufferedImage deathEffectSheet;
    private static boolean deathEffectLoaded = false;
    private static final int DEATH_FRAMES = 4;
    private static final int DEATH_TICKS_PER_FRAME = 4;
    private static final int DEATH_DURATION = DEATH_FRAMES * DEATH_TICKS_PER_FRAME;
    private List<int[]> deathEffects = new ArrayList<>();

    // Stairway support
    private boolean hasStairway = false;
    private boolean stairwayRevealed = false;
    private int stairTargetX = -1;
    private int stairTargetY = -1;

    private int[][] dungeonCollisionGrid;

    private int[][] getDungeonCollisionGrid() {
        if (dungeonCollisionGrid != null) return dungeonCollisionGrid;

        int tilesX = ZeldaRoom.TILES_X;
        int tilesY = ZeldaRoom.TILES_Y;
        dungeonCollisionGrid = new int[tilesX][tilesY];

        // Border walls (2 tiles thick on each side)
        for (int tx = 0; tx < tilesX; tx++) {
            for (int ty = 0; ty < tilesY; ty++) {
                if (tx <= 1 || tx >= tilesX - 2 || ty <= 1 || ty >= tilesY - 2) {
                    dungeonCollisionGrid[tx][ty] = TileType.WALL.ordinal();
                } else {
                    dungeonCollisionGrid[tx][ty] = TileType.FLOOR.ordinal();
                }
            }
        }

        // Open door passages only where doors exist (not NONE)
        int midX = tilesX / 2;
        int midY = tilesY / 2;
        int floor = TileType.FLOOR.ordinal();

        if (doors[DOOR_NORTH] != DoorState.NONE) {
            dungeonCollisionGrid[midX - 1][0] = floor;
            dungeonCollisionGrid[midX][0] = floor;
            dungeonCollisionGrid[midX - 1][1] = floor;
            dungeonCollisionGrid[midX][1] = floor;
        }
        if (doors[DOOR_SOUTH] != DoorState.NONE) {
            dungeonCollisionGrid[midX - 1][tilesY - 1] = floor;
            dungeonCollisionGrid[midX][tilesY - 1] = floor;
            dungeonCollisionGrid[midX - 1][tilesY - 2] = floor;
            dungeonCollisionGrid[midX][tilesY - 2] = floor;
        }
        if (doors[DOOR_WEST] != DoorState.NONE) {
            dungeonCollisionGrid[0][midY - 1] = floor;
            dungeonCollisionGrid[0][midY] = floor;
            dungeonCollisionGrid[1][midY - 1] = floor;
            dungeonCollisionGrid[1][midY] = floor;
        }
        if (doors[DOOR_EAST] != DoorState.NONE) {
            dungeonCollisionGrid[tilesX - 1][midY - 1] = floor;
            dungeonCollisionGrid[tilesX - 1][midY] = floor;
            dungeonCollisionGrid[tilesX - 2][midY - 1] = floor;
            dungeonCollisionGrid[tilesX - 2][midY] = floor;
        }

        return dungeonCollisionGrid;
    }

    public boolean isWalkable(int pixelX, int pixelY) {
        int tileX = pixelX / ZeldaRoom.TILE_SIZE;
        int tileY = pixelY / ZeldaRoom.TILE_SIZE;
        if (tileX < 0 || tileX >= ZeldaRoom.TILES_X || tileY < 0 || tileY >= ZeldaRoom.TILES_Y)
            return false;
        return TileType.fromId(getDungeonCollisionGrid()[tileX][tileY]).walkable;
    }

    private DungeonRenderer renderer;
    private CollisionMap collisionMap;
    private ItemDropSystem itemDropSystem;
    private Item.ItemType pendingItemGet = null; // For item-get animation trigger

    // Old Man room support (NES dungeon NPCs)
    public enum OldManType { NONE, DOOR_REPAIR, GRUMBLE, MONEY_OR_LIFE, HINT }
    private OldManType oldManType = OldManType.NONE;
    private String oldManText = "";
    private int oldManCost = 0;         // Rupee cost for DOOR_REPAIR / MONEY_OR_LIFE
    private boolean oldManResolved = false; // Has the player satisfied the requirement?
    private int oldManTextReveal = 0;   // For character-by-character text reveal
    private java.awt.image.BufferedImage oldManSprite;

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

    private Inventory playerInventory; // Set during enter for item persistence checks

    public void enter(Inventory inv) {
        this.playerInventory = inv;
        if (!visited) {
            visited = true;
            spawnEnemies();
            if (bossType != null) {
                spawnBoss();
            }
            if (hasItem && roomItem != null) {
                if (inv == null || !inv.isDungeonItemCollected(dungeonNumber, localX, localY)) {
                    items.add(new Item(ZeldaRoom.ROOM_PIXEL_W / 2, ZeldaRoom.ROOM_PIXEL_H / 2, roomItem));
                }
            }
        }
    }

    /** Backward-compatible enter without inventory. */
    public void enter() {
        enter(null);
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
            if (!frozen) {
                if (!e.processKnockback()) {
                    e.update(player, null, projectiles);
                }
            }
            if (!e.isAlive()) {
                spawnDeathEffect(e.getX(), e.getY());
                if (itemDropSystem != null) {
                    Item.ItemType drop = itemDropSystem.onEnemyKilled(e.getDropClass());
                    if (drop != null) items.add(new Item(e.getX(), e.getY(), drop));
                }
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

        // Old Man interaction
        if (oldManType != OldManType.NONE && !oldManResolved) {
            oldManTextReveal = Math.min(oldManText.length(), oldManTextReveal + 1);
            updateOldMan(player);
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

    private void updateOldMan(ZeldaPlayer player) {
        Inventory inv = player.getInventory();
        // Old Man is at center top of room
        double npcX = ZeldaRoom.ROOM_PIXEL_W / 2.0 - 8;
        double npcY = 48;

        // Check if player is close enough to interact
        double dx = player.getWorldX() - npcX;
        double dy = player.getWorldY() - npcY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 32) return;

        switch (oldManType) {
            case DOOR_REPAIR:
                // Auto-pay when close enough
                if (inv.getRupees() >= oldManCost) {
                    inv.addRupees(-oldManCost);
                    oldManResolved = true;
                }
                break;
            case GRUMBLE:
                // Need Food item
                if (inv.hasFood()) {
                    inv.setHasFood(false);
                    oldManResolved = true;
                }
                break;
            case MONEY_OR_LIFE:
                // Pay rupees or lose a heart container
                if (inv.getRupees() >= oldManCost) {
                    inv.addRupees(-oldManCost);
                    oldManResolved = true;
                } else {
                    // Can't pay — lose a heart container on passage
                    if (inv.getHeartContainers() > 3) {
                        inv.setMaxHealth((inv.getHeartContainers() - 1) * 2);
                        inv.setHealth(Math.min(inv.getHealth(), inv.getMaxHealth()));
                    }
                    oldManResolved = true;
                }
                break;
            case HINT:
                oldManResolved = true; // Just show text, no cost
                break;
            default:
                break;
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
        renderDeathEffects(g2);
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

        // Old Man NPC rendering
        if (oldManType != OldManType.NONE) {
            int npcX = ZeldaRoom.ROOM_PIXEL_W / 2 - 8;
            int npcY = 48;
            if (oldManSprite != null) {
                g2.drawImage(oldManSprite, npcX, npcY, 16, 16, null);
            } else {
                g2.setColor(new Color(200, 160, 120));
                g2.fillRect(npcX, npcY, 16, 16);
            }
            // Two fires flanking the Old Man
            int firePhase = ((int)(System.currentTimeMillis() / 150)) % 2;
            Color fireC = (firePhase == 0) ? new Color(252, 152, 56)
                                                     : new Color(252, 216, 108);
            g2.setColor(fireC);
            g2.fillRect(npcX - 24, npcY + 4, 8, 8);
            g2.fillRect(npcX + 32, npcY + 4, 8, 8);
            // Text
            if (oldManText != null && oldManText.length() > 0) {
                g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
                g2.setColor(Color.WHITE);
                String visible = oldManText.substring(0, Math.min(oldManTextReveal, oldManText.length()));
                // Word-wrap at ~28 chars per line
                int maxLineLen = 28;
                int textY = npcY + 24;
                for (int i = 0; i < visible.length(); i += maxLineLen) {
                    String line = visible.substring(i, Math.min(i + maxLineLen, visible.length()));
                    int sw = g2.getFontMetrics().stringWidth(line);
                    g2.drawString(line, (ZeldaRoom.ROOM_PIXEL_W - sw) / 2, textY);
                    textY += 10;
                }
            }
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

    public void setOldMan(OldManType type, String text, int cost) {
        this.oldManType = type;
        this.oldManText = text;
        this.oldManCost = cost;
        try {
            File f = new File("sprites/NPCs/Old Man.gif");
            if (f.exists()) this.oldManSprite = ImageIO.read(f);
        } catch (Exception ex) {}
    }
    public OldManType getOldManType() { return oldManType; }
    public boolean isOldManResolved() { return oldManResolved; }
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

    private boolean isSpecialDungeonItem(Item.ItemType type) {
        switch (type) {
            case TRIFORCE: case HEART_CONTAINER: case BOW: case MAGICAL_BOOMERANG:
            case RAFT: case STEPLADDER: case RECORDER: case MAGICAL_ROD:
            case RED_CANDLE: case MAGICAL_KEY: case SILVER_ARROW: case BOSS_KEY: case BOOK:
                return true;
            default: return false;
        }
    }

    /** Returns and clears the pending item-get animation trigger. */
    public Item.ItemType consumePendingItemGet() {
        Item.ItemType t = pendingItemGet;
        pendingItemGet = null;
        return t;
    }

    private void applyDungeonItem(Item item, ZeldaPlayer player) {
        Inventory inv = player.getInventory();
        // Mark this room's item as collected for persistence
        inv.markDungeonItemCollected(dungeonNumber, localX, localY);
        // Trigger item-get animation for special items
        if (isSpecialDungeonItem(item.getType())) {
            pendingItemGet = item.getType();
        }
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
