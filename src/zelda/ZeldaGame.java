package zelda;

import engine.KeyHandler;
import java.awt.*;

public class ZeldaGame {
    public enum GameState {
        TITLE_SCREEN, PLAYING, PAUSED, GAME_OVER, GAME_WIN, ROOM_TRANSITION, CAVE, DUNGEON,
        FADE_OUT, FADE_IN
    }

    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 240;
    public static final int HUD_HEIGHT = 56;
    public static final int PLAY_AREA_Y = HUD_HEIGHT;
    public static final int PLAY_AREA_H = SCREEN_HEIGHT - HUD_HEIGHT;

    public static final double PLAYER_START_X = 120;
    public static final double PLAYER_START_Y = 128;

    public static final int CAVE_ENTRANCE_ROOM_X = 7;
    public static final int CAVE_ENTRANCE_ROOM_Y = 7;
    public static final int CAVE_ENTRANCE_TILE_X = 7;
    public static final int CAVE_ENTRANCE_TILE_Y = 3;

    public static final int DUNGEON1_ENTRANCE_ROOM_X = 7;
    public static final int DUNGEON1_ENTRANCE_ROOM_Y = 3;
    public static final int DUNGEON1_ENTRANCE_TILE_X = 7;
    public static final int DUNGEON1_ENTRANCE_TILE_Y = 3;

    public static final int TRANSITION_DURATION = 32;
    private static final int DEATH_ANIM_DURATION = 90;
    private static final int FADE_DURATION = 16;

    private static final String SFX_SWORD = "";
    private static final String SFX_ENEMY_HIT = "";
    private static final String SFX_ENEMY_DIE = "";
    private static final String SFX_HURT = "";
    private static final String SFX_ITEM = "04. Small Item Get.wav";
    private static final String SFX_KEY = "06. Secret.wav";
    private static final String SFX_DOOR = "05. Discovery.wav";
    private static final String SFX_FANFARE = "07. Collect Item.wav";
    private static final String SFX_STAIRS = "06. Secret.wav";

    private static final String MUSIC_TITLE = "01. Title Screen.wav";
    private static final String MUSIC_OVERWORLD = "02. Overworld of Hyrule.wav";
    private static final String MUSIC_DUNGEON = "03. Dungeon Theme.wav";
    private static final String MUSIC_GAME_OVER = "11 Game Over.wav";

    private GameState state = GameState.TITLE_SCREEN;
    private KeyHandler keyHandler;

    private ZeldaPlayer player;
    private Overworld overworld;
    private ZeldaHUD hud;
    private TitleScreen titleScreen;
    private AudioManager audioManager;
    private SaveManager saveManager;
    private CombatManager combatManager;
    private ItemDropSystem itemDropSystem;
    private Cave cave;
    private InventoryScreen inventoryScreen;

    private ZeldaDungeon currentDungeon;
    private DungeonRenderer dungeonRenderer;

    private String playerName;
    private int currentSaveSlot;

    private int transitionTimer = 0;
    private int transitionDir = -1;
    private int transitionOldRoomX, transitionOldRoomY;
    private double transitionPlayerStartX, transitionPlayerStartY;
    private double transitionPlayerEndX, transitionPlayerEndY;

    private int deathAnimTimer = 0;
    private int deathSpinFrame = 0;

    private int fadeTimer = 0;
    private boolean fadeIn = false;
    private float fadeAlpha = 0f;
    private GameState fadeTargetState = GameState.PLAYING;
    private Runnable fadeCallback = null;

    private boolean godMode = false;
    private boolean gKeyReleased = true;

    public ZeldaGame(KeyHandler keyHandler) {
        this.keyHandler = keyHandler;
        audioManager = new AudioManager();
        saveManager = new SaveManager();
        combatManager = new CombatManager();
        itemDropSystem = new ItemDropSystem();
        combatManager.setItemDropSystem(itemDropSystem);
        hud = new ZeldaHUD();
        cave = new Cave();
        inventoryScreen = new InventoryScreen();
        titleScreen = new TitleScreen(this, keyHandler);
        dungeonRenderer = new DungeonRenderer();

        audioManager.playMusic(MUSIC_TITLE);
    }

    public void update() {
        switch (state) {
            case TITLE_SCREEN: titleScreen.update(); break;
            case PLAYING: updatePlaying(); break;
            case CAVE: updateCave(); break;
            case DUNGEON: updateDungeon(); break;
            case ROOM_TRANSITION: updateTransition(); break;
            case PAUSED: updatePaused(); break;
            case GAME_OVER: updateGameOver(); break;
            case GAME_WIN: updateGameWin(); break;
            case FADE_OUT: case FADE_IN: updateFade(); break;
        }
    }

    private void updatePlaying() {
        if (keyHandler.escapePressed) {
            state = GameState.PAUSED;
            return;
        }

        if (keyHandler.gPressed && gKeyReleased) {
            godMode = !godMode;
            gKeyReleased = false;
            if (godMode) player.setHealth(player.getMaxHealth());
        }
        if (!keyHandler.gPressed) gKeyReleased = true;

        boolean wasAttackingBefore = player.isAttacking();
        player.update();

        if (player.isAttacking() && !wasAttackingBefore) {
            audioManager.playSFX(SFX_SWORD);
        }

        ZeldaRoom room = overworld.getCurrentRoom();
        player.setRoomProjectiles(room.getProjectiles());
        room.update(player);

        checkRoomTransition();
        checkCaveEntrance();
        checkDungeonEntrance();

        if (!godMode) {
            int cx = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2);
            int cy = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2);

            if (!room.isWalkable(cx, cy)) {
                player.revertPosition();
            }
        }

        player.clampToPlayArea();

        if (!godMode) {
            combatManager.checkCombat(player, room, audioManager);

            if (!player.isAlive()) {
                startDeathAnimation();
            }
        }
    }

    private void startDeathAnimation() {
        audioManager.stopMusic();
        audioManager.playMusic(MUSIC_GAME_OVER);
        deathAnimTimer = DEATH_ANIM_DURATION;
        deathSpinFrame = 0;
        state = GameState.GAME_OVER;
    }

    private void checkRoomTransition() {
        double px = player.getWorldX();
        double py = player.getWorldY();
        int roomX = overworld.getCurrentRoomX();
        int roomY = overworld.getCurrentRoomY();
        int dir = -1;

        if (px <= ZeldaRoom.ROOM_PLAY_LEFT - ZeldaPlayer.WIDTH / 2) dir = 3;
        else if (px >= ZeldaRoom.ROOM_PLAY_RIGHT - ZeldaPlayer.WIDTH / 2) dir = 1;
        else if (py <= ZeldaRoom.ROOM_PLAY_TOP - ZeldaPlayer.HEIGHT / 2) dir = 0;
        else if (py >= ZeldaRoom.ROOM_PLAY_BOTTOM - ZeldaPlayer.HEIGHT / 2) dir = 2;

        if (dir < 0) return;

        int nx = roomX, ny = roomY;
        switch (dir) {
            case 0: ny--; break;
            case 1: nx++; break;
            case 2: ny++; break;
            case 3: nx--; break;
        }

        if (overworld.hasRoom(nx, ny)) {
            transitionDir = dir;
            transitionTimer = TRANSITION_DURATION;
            transitionOldRoomX = roomX;
            transitionOldRoomY = roomY;
            transitionPlayerStartX = px;
            transitionPlayerStartY = py;

            switch (dir) {
                case 0: transitionPlayerEndX = px; transitionPlayerEndY = ZeldaRoom.ROOM_PLAY_BOTTOM - ZeldaPlayer.HEIGHT - 8; break;
                case 1: transitionPlayerEndX = ZeldaRoom.ROOM_PLAY_LEFT + 8; transitionPlayerEndY = py; break;
                case 2: transitionPlayerEndX = px; transitionPlayerEndY = ZeldaRoom.ROOM_PLAY_TOP + 8; break;
                case 3: transitionPlayerEndX = ZeldaRoom.ROOM_PLAY_RIGHT - ZeldaPlayer.WIDTH - 8; transitionPlayerEndY = py; break;
            }

            overworld.setCurrentRoom(nx, ny);
            player.onScreenChange();
            state = GameState.ROOM_TRANSITION;
        } else {
            player.revertPosition();
        }
    }

    private void checkCaveEntrance() {
        ZeldaRoom room = overworld.getCurrentRoom();
        if (room == null || !room.hasCaveEntrance()) return;

        int tileX = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2) / ZeldaRoom.TILE_SIZE;
        int tileY = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2) / ZeldaRoom.TILE_SIZE;

        int caveTX = room.getCaveTileX();
        int caveTY = room.getCaveTileY();

        // Allow 1-tile tolerance for entrance detection
        boolean atEntrance = Math.abs(tileX - caveTX) <= 1 && Math.abs(tileY - caveTY) <= 1;

        if (atEntrance && keyHandler.upPressed) {
            audioManager.playSFX(SFX_STAIRS);
            startFadeOut(() -> {
                cave.enter(player, overworld.getCurrentRoomX(), overworld.getCurrentRoomY());
                state = GameState.CAVE;
                startFadeIn();
            });
        }
    }

    private void checkDungeonEntrance() {
        ZeldaRoom room = overworld.getCurrentRoom();
        if (room == null || !room.hasDungeonEntrance()) return;

        int tileX = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2) / ZeldaRoom.TILE_SIZE;
        int tileY = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2) / ZeldaRoom.TILE_SIZE;

        // Use auto-detected entrance position from sprite map
        int entranceTileX = room.getDungeonEntranceTileX();
        int entranceTileY = room.getDungeonEntranceTileY();

        // Allow 1-tile tolerance for entrance detection
        boolean atEntrance = Math.abs(tileX - entranceTileX) <= 1 && Math.abs(tileY - entranceTileY) <= 1;

        if (atEntrance && keyHandler.upPressed) {
            final int level = room.getDungeonId();
            startFadeOut(() -> {
                enterDungeon(level);
                startFadeIn();
            });
        }
    }

    private void enterDungeon(int level) {
        audioManager.stopMusic();
        audioManager.playSFX(SFX_STAIRS);
        audioManager.playMusic(MUSIC_DUNGEON);

        dungeonRenderer.setDungeonLevel(level);
        currentDungeon = new ZeldaDungeon(level, "LEVEL-" + level);
        currentDungeon.initialize(dungeonRenderer, overworld.getCollisionMap());
        currentDungeon.setItemDropSystem(itemDropSystem);
        player.setPosition(ZeldaDungeon.ENTRANCE_SPAWN_X, ZeldaDungeon.ENTRANCE_SPAWN_Y);

        hud.setInDungeon(true, level);
        hud.setDungeon(currentDungeon);
        state = GameState.DUNGEON;
    }

    private void exitDungeon() {
        startFadeOut(() -> {
            audioManager.stopMusic();
            audioManager.playMusic(MUSIC_OVERWORLD);

            hud.setInDungeon(false, 0);
            hud.setDungeon(null);
            currentDungeon = null;

            player.setPosition(PLAYER_START_X, PLAYER_START_Y);
            state = GameState.PLAYING;
            startFadeIn();
        });
    }

    private void updateDungeon() {
        if (keyHandler.escapePressed) {
            state = GameState.PAUSED;
            return;
        }

        player.update();

        if (currentDungeon != null && currentDungeon.getCurrentRoom() != null) {
            DungeonRoom room = currentDungeon.getCurrentRoom();
            room.update(player);
            player.setRoomProjectiles(room.getProjectiles());

            combatManager.checkDungeonCombat(player, room, audioManager);

            // Candle lights dark rooms
            if (room.isDark() && !room.isLit()) {
                for (Projectile p : room.getProjectiles()) {
                    if (p.isActive() && p.isPlayerProjectile() && p.getColor() != null
                            && p.getColor().equals(java.awt.Color.ORANGE)) {
                        room.lightRoom();
                        break;
                    }
                }
            }

            // Bomb projectiles try to open bombable walls
            for (Projectile p : room.getProjectiles()) {
                if (p.isActive() && p.isPlayerProjectile() && !p.isMoving()
                        && p.getColor() != null && p.getColor().equals(java.awt.Color.DARK_GRAY)) {
                    room.tryBombWalls(p);
                }
            }

            // Check ZELDA rescue (Level 9 win condition)
            if (room.isCleared() && currentDungeon.getDungeonNumber() == 9
                    && room.hasZeldaItem()) {
                audioManager.stopMusic();
                audioManager.playSFX(SFX_FANFARE);
                saveCurrentGame();
                state = GameState.GAME_WIN;
                return;
            }

            // Check triforce collection — triggers save + exit
            if (player.getInventory().hasTriforce(currentDungeon.getDungeonNumber())
                    && !currentDungeon.isTriforceCollected()) {
                currentDungeon.setTriforceCollected(true);
                currentDungeon.setBossDefeated(true);
                audioManager.playSFX(SFX_FANFARE);
                saveCurrentGame();
                exitDungeon();
                return;
            }

            checkDungeonRoomTransition();
        }

        if (!player.isAlive()) {
            startDeathAnimation();
        }
    }

    private void checkDungeonRoomTransition() {
        double px = player.getWorldX();
        double py = player.getWorldY();
        int dir = -1;

        if (px < 4) dir = DungeonRoom.DOOR_WEST;
        else if (px > ZeldaRoom.ROOM_PIXEL_W - ZeldaPlayer.WIDTH - 4) dir = DungeonRoom.DOOR_EAST;
        else if (py < 4) dir = DungeonRoom.DOOR_NORTH;
        else if (py > ZeldaRoom.ROOM_PIXEL_H - ZeldaPlayer.HEIGHT - 4) dir = DungeonRoom.DOOR_SOUTH;

        if (dir < 0) return;

        if (dir == DungeonRoom.DOOR_SOUTH && currentDungeon.getCurrentRoomX() == currentDungeon.getEntranceRoomX()
            && currentDungeon.getCurrentRoomY() == currentDungeon.getEntranceRoomY()) {
            exitDungeon();
            return;
        }

        if (!currentDungeon.tryMoveRoom(dir, player)) {
            player.revertPosition();
        }
    }

    private void updateCave() {
        player.update();
        boolean exited = cave.update(player);
        if (exited) {
            if (cave.isSwordTaken()) {
                audioManager.playSFX(SFX_FANFARE);
            }
            player.setPosition(PLAYER_START_X, PLAYER_START_Y);
            state = GameState.PLAYING;
        }
    }

    private void updateTransition() {
        transitionTimer--;
        if (transitionTimer <= 0) {
            player.setPosition(transitionPlayerEndX, transitionPlayerEndY);
            state = GameState.PLAYING;
        } else {
            float t = 1.0f - (float)transitionTimer / TRANSITION_DURATION;
            double lx = transitionPlayerStartX + (transitionPlayerEndX - transitionPlayerStartX) * t;
            double ly = transitionPlayerStartY + (transitionPlayerEndY - transitionPlayerStartY) * t;
            player.setPosition(lx, ly);
        }
    }

    private void updatePaused() {
        if (player != null) {
            inventoryScreen.update(keyHandler, player.getInventory());
        }
        if (keyHandler.escapePressed || keyHandler.startPressed) {
            state = (currentDungeon != null) ? GameState.DUNGEON : GameState.PLAYING;
        }
    }

    private void updateGameOver() {
        if (deathAnimTimer > 0) {
            deathAnimTimer--;
            deathSpinFrame = (DEATH_ANIM_DURATION - deathAnimTimer) / 4;
            return;
        }

        if (keyHandler.startPressed || keyHandler.enterPressed) {
            Inventory inv = player.getInventory();
            inv.setHealth(inv.getMaxHealth());
            saveCurrentGame();

            currentDungeon = null;
            hud.setInDungeon(false, 0);
            player.setPosition(PLAYER_START_X, PLAYER_START_Y);
            overworld.setCurrentRoom(Overworld.START_ROOM_X, Overworld.START_ROOM_Y);

            audioManager.stopMusic();
            audioManager.playMusic(MUSIC_OVERWORLD);
            state = GameState.PLAYING;
        }
    }

    private void startFadeOut(Runnable callback) {
        fadeTimer = FADE_DURATION;
        fadeIn = false;
        fadeAlpha = 0f;
        fadeCallback = callback;
        fadeTargetState = state; // remember what we were doing
        state = GameState.FADE_OUT;
    }

    private void startFadeIn() {
        fadeTargetState = state; // save current state to return to after fade
        fadeTimer = FADE_DURATION;
        fadeIn = true;
        fadeAlpha = 1.0f;
        fadeCallback = null;
        state = GameState.FADE_IN;
    }

    private void updateFade() {
        fadeTimer--;
        if (state == GameState.FADE_OUT) {
            fadeAlpha = 1.0f - (float)fadeTimer / FADE_DURATION;
            if (fadeTimer <= 0) {
                fadeAlpha = 1.0f;
                if (fadeCallback != null) {
                    Runnable cb = fadeCallback;
                    fadeCallback = null;
                    cb.run();
                    // callback sets state to target + calls startFadeIn
                }
            }
        } else { // FADE_IN
            fadeAlpha = (float)fadeTimer / FADE_DURATION;
            if (fadeTimer <= 0) {
                fadeAlpha = 0f;
                state = fadeTargetState;
            }
        }
    }

    private void updateGameWin() {
        if (keyHandler.startPressed || keyHandler.enterPressed) {
            // Return to title screen after winning
            currentDungeon = null;
            hud.setInDungeon(false, 0);
            hud.setDungeon(null);
            state = GameState.TITLE_SCREEN;
            audioManager.stopMusic();
            audioManager.playMusic(MUSIC_TITLE);
        }
    }

    private int winScreenTimer = 0;

    private void renderGameWin(Graphics2D g2) {
        winScreenTimer++;
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Golden triforce display (pulsing glow)
        int triCX = SCREEN_WIDTH / 2;
        int triTopY = 30;
        int triSize = 40;
        boolean glow = (winScreenTimer / 10) % 3 == 0;
        Color triColor = glow ? new Color(255, 250, 180) : new Color(240, 220, 60);
        g2.setColor(triColor);
        int[] tx = {triCX, triCX - triSize, triCX + triSize};
        int[] ty = {triTopY, triTopY + triSize * 2, triTopY + triSize * 2};
        g2.fillPolygon(tx, ty, 3);

        // Inner black triangle (triforce gap)
        g2.setColor(Color.BLACK);
        int innerSize = triSize / 2;
        int[] ix = {triCX, triCX - innerSize, triCX + innerSize};
        int innerTopY = triTopY + triSize;
        int[] iy = {innerTopY, innerTopY + innerSize, innerTopY + innerSize};
        g2.fillPolygon(ix, iy, 3);

        g2.setColor(new Color(240, 220, 60));
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        String msg1 = "CONGRATULATIONS!";
        int w1 = g2.getFontMetrics().stringWidth(msg1);
        g2.drawString(msg1, (SCREEN_WIDTH - w1) / 2, 130);

        g2.setColor(Color.WHITE);
        String msg2 = "YOU RESCUED ZELDA!";
        int w2 = g2.getFontMetrics().stringWidth(msg2);
        g2.drawString(msg2, (SCREEN_WIDTH - w2) / 2, 155);

        // Flashing "PRESS START"
        if ((winScreenTimer / 20) % 2 == 0) {
            g2.setColor(new Color(100, 200, 100));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            String msg3 = "PRESS START";
            int w3 = g2.getFontMetrics().stringWidth(msg3);
            g2.drawString(msg3, (SCREEN_WIDTH - w3) / 2, 190);
        }
    }

    private void saveCurrentGame() {
        if (player != null && overworld != null) {
            saveManager.saveGame(currentSaveSlot, player,
                overworld.getCurrentRoomX(), overworld.getCurrentRoomY());
        }
    }

    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        switch (state) {
            case TITLE_SCREEN: titleScreen.render(g2); break;
            case PLAYING: renderPlaying(g2); break;
            case CAVE: renderCave(g2); break;
            case DUNGEON: renderDungeon(g2); break;
            case ROOM_TRANSITION: renderTransition(g2); break;
            case PAUSED: renderPaused(g2); break;
            case GAME_OVER: renderGameOver(g2); break;
            case GAME_WIN: renderGameWin(g2); break;
            case FADE_OUT: case FADE_IN: renderFade(g2); break;
        }
    }

    private void renderFade(Graphics2D g2) {
        // Render underlying scene based on target state
        if (currentDungeon != null) {
            renderDungeon(g2);
        } else if (cave.isActive()) {
            renderCave(g2);
        } else {
            renderPlaying(g2);
        }
        // Black overlay
        int alpha = (int)(fadeAlpha * 255);
        alpha = Math.max(0, Math.min(255, alpha));
        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void renderPlaying(Graphics2D g2) {
        ZeldaRoom room = overworld.getCurrentRoom();
        hud.render(g2, room);

        g2.translate(0, HUD_HEIGHT);
        if (room != null) room.render(g2);
        player.render(g2);
        g2.translate(0, -HUD_HEIGHT);

        if (godMode) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Monospaced", Font.BOLD, 8));
            g2.drawString("GOD", SCREEN_WIDTH - 28, 10);
        }
    }

    private void renderCave(Graphics2D g2) {
        ZeldaRoom room = overworld.getCurrentRoom();
        hud.render(g2, room);

        g2.translate(0, HUD_HEIGHT);
        cave.render(g2);
        player.render(g2);
        g2.translate(0, -HUD_HEIGHT);
    }

    private void renderDungeon(Graphics2D g2) {
        hud.render(g2, null);

        g2.translate(0, HUD_HEIGHT);
        if (currentDungeon != null && currentDungeon.getCurrentRoom() != null) {
            int px = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2);
            int py = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2);
            currentDungeon.getCurrentRoom().render(g2, px, py);
        }
        player.render(g2);
        g2.translate(0, -HUD_HEIGHT);
    }

    private void renderTransition(Graphics2D g2) {
        float t = 1.0f - (float)transitionTimer / TRANSITION_DURATION;
        int scrollOffset;

        ZeldaRoom room = overworld.getCurrentRoom();
        hud.render(g2, room);
        g2.translate(0, HUD_HEIGHT);

        switch (transitionDir) {
            case 0: // Link went UP: old room slides DOWN, new room enters from TOP
                scrollOffset = (int)(t * PLAY_AREA_H);
                g2.translate(0, scrollOffset);
                renderOldRoom(g2);
                g2.translate(0, -PLAY_AREA_H);
                if (room != null) room.render(g2);
                g2.translate(0, PLAY_AREA_H - scrollOffset);
                break;
            case 2: // Link went DOWN: old room slides UP, new room enters from BOTTOM
                scrollOffset = (int)(t * PLAY_AREA_H);
                g2.translate(0, -scrollOffset);
                renderOldRoom(g2);
                g2.translate(0, PLAY_AREA_H);
                if (room != null) room.render(g2);
                g2.translate(0, -PLAY_AREA_H + scrollOffset);
                break;
            case 1: // Link went RIGHT: old room slides LEFT, new room enters from RIGHT
                scrollOffset = (int)(t * SCREEN_WIDTH);
                g2.translate(-scrollOffset, 0);
                renderOldRoom(g2);
                g2.translate(SCREEN_WIDTH, 0);
                if (room != null) room.render(g2);
                g2.translate(-SCREEN_WIDTH + scrollOffset, 0);
                break;
            case 3: // Link went LEFT: old room slides RIGHT, new room enters from LEFT
                scrollOffset = (int)(t * SCREEN_WIDTH);
                g2.translate(scrollOffset, 0);
                renderOldRoom(g2);
                g2.translate(-SCREEN_WIDTH, 0);
                if (room != null) room.render(g2);
                g2.translate(SCREEN_WIDTH - scrollOffset, 0);
                break;
            default:
                if (room != null) room.render(g2);
                break;
        }

        player.render(g2);
        g2.translate(0, -HUD_HEIGHT);
    }

    private void renderOldRoom(Graphics2D g2) {
        if (overworld != null) {
            ZeldaRoom oldRoom = overworld.getRoom(transitionOldRoomX, transitionOldRoomY);
            if (oldRoom != null) oldRoom.render(g2);
        }
    }

    private void renderPaused(Graphics2D g2) {
        int dungeonLvl = (currentDungeon != null) ? currentDungeon.getDungeonNumber() : 0;
        if (player != null) {
            inventoryScreen.render(g2, player.getInventory(), dungeonLvl);
        }
    }

    private void renderGameOver(Graphics2D g2) {
        if (deathAnimTimer > 0) {
            // Death animation: gradually fade to red/brown with spinning Link
            float t = 1.0f - (float)deathAnimTimer / DEATH_ANIM_DURATION;

            // Render the current scene underneath
            if (currentDungeon != null) {
                renderDungeon(g2);
            } else {
                renderPlaying(g2);
            }

            // Red/brown fade overlay that increases with time
            int alpha = (int)(t * 200);
            g2.setColor(new Color(
                NESPalette.DEATH_BG2.getRed(),
                NESPalette.DEATH_BG2.getGreen(),
                NESPalette.DEATH_BG2.getBlue(), alpha));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            // Spinning Link effect (rotate the player render area)
            if (player != null && t < 0.8f) {
                int cx = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2);
                int cy = HUD_HEIGHT + (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2);
                double angle = deathSpinFrame * Math.PI / 2;
                Graphics2D g2r = (Graphics2D)g2.create();
                g2r.rotate(angle, cx, cy);
                g2r.dispose();
            }
            return;
        }

        // Static game over screen
        g2.setColor(new Color(
            NESPalette.GAME_OVER_BG.getRed(),
            NESPalette.GAME_OVER_BG.getGreen(),
            NESPalette.GAME_OVER_BG.getBlue()));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        String go = "GAME OVER";
        int gow = g2.getFontMetrics().stringWidth(go);
        g2.drawString(go, (SCREEN_WIDTH - gow) / 2, 100);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        String cont = "PRESS ENTER TO CONTINUE";
        int cw = g2.getFontMetrics().stringWidth(cont);
        g2.drawString(cont, (SCREEN_WIDTH - cw) / 2, 140);
    }

    public void startNewGame(String name, int saveSlot) {
        this.playerName = name;
        this.currentSaveSlot = saveSlot;

        Inventory inventory = Inventory.createNew();
        player = new ZeldaPlayer(PLAYER_START_X, PLAYER_START_Y, keyHandler, inventory);
        player.setName(name);

        overworld = new Overworld();
        overworld.initialize();
        overworld.setItemDropSystem(itemDropSystem);

        hud.setPlayer(player);
        hud.setInDungeon(false, 0);

        saveManager.createNewSave(saveSlot, name);

        audioManager.stopMusic();
        audioManager.playMusic(MUSIC_OVERWORLD);

        state = GameState.PLAYING;
    }

    public void loadGame(int saveSlot) {
        SaveManager.SaveData data = saveManager.loadGame(saveSlot);
        if (data != null) {
            this.currentSaveSlot = saveSlot;
            this.playerName = data.playerName;

            Inventory inventory = (data.inventory != null) ? data.inventory : Inventory.createNew();
            player = new ZeldaPlayer(data.playerX, data.playerY, keyHandler, inventory);
            player.setName(data.playerName);

            overworld = new Overworld();
            overworld.initialize();
            overworld.setItemDropSystem(itemDropSystem);
            overworld.setCurrentRoom(data.roomX, data.roomY);

            hud.setPlayer(player);

            audioManager.stopMusic();
            audioManager.playMusic(MUSIC_OVERWORLD);

            state = GameState.PLAYING;
        }
    }

    public AudioManager getAudioManager() { return audioManager; }
    public SaveManager getSaveManager() { return saveManager; }
    public GameState getState() { return state; }
}
