package zelda;

import engine.KeyHandler;
import java.awt.*;

public class ZeldaGame {
    public enum GameState {
        TITLE_SCREEN, PLAYING, PAUSED, GAME_OVER, ROOM_TRANSITION, CAVE, DUNGEON
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

    public static final int TRANSITION_DURATION = 20;

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
    private Cave cave;

    private ZeldaDungeon currentDungeon;
    private DungeonRenderer dungeonRenderer;

    private String playerName;
    private int currentSaveSlot;

    private int transitionTimer = 0;
    private int transitionDir = -1;
    private boolean wasAttacking = false;
    private boolean godMode = false;
    private boolean gKeyReleased = true;

    public ZeldaGame(KeyHandler keyHandler) {
        this.keyHandler = keyHandler;
        audioManager = new AudioManager();
        saveManager = new SaveManager();
        combatManager = new CombatManager();
        hud = new ZeldaHUD();
        cave = new Cave();
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
                audioManager.stopMusic();
                audioManager.playMusic(MUSIC_GAME_OVER);
                state = GameState.GAME_OVER;
            }
        }
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
            switch (dir) {
                case 0: player.setPosition(player.getWorldX(), ZeldaRoom.ROOM_PLAY_BOTTOM - ZeldaPlayer.HEIGHT - 8); break;
                case 1: player.setPosition(ZeldaRoom.ROOM_PLAY_LEFT + 8, player.getWorldY()); break;
                case 2: player.setPosition(player.getWorldX(), ZeldaRoom.ROOM_PLAY_TOP + 8); break;
                case 3: player.setPosition(ZeldaRoom.ROOM_PLAY_RIGHT - ZeldaPlayer.WIDTH - 8, player.getWorldY()); break;
            }
            overworld.setCurrentRoom(nx, ny);
        } else {
            player.revertPosition();
        }
    }

    private void checkCaveEntrance() {
        int roomX = overworld.getCurrentRoomX();
        int roomY = overworld.getCurrentRoomY();

        if (roomX == CAVE_ENTRANCE_ROOM_X && roomY == CAVE_ENTRANCE_ROOM_Y) {
            int tileX = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2) / ZeldaRoom.TILE_SIZE;
            int tileY = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2) / ZeldaRoom.TILE_SIZE;

            if (tileX == CAVE_ENTRANCE_TILE_X && tileY == CAVE_ENTRANCE_TILE_Y && keyHandler.upPressed) {
                audioManager.playSFX(SFX_STAIRS);
                cave.enter(player);
                state = GameState.CAVE;
            }
        }
    }

    private void checkDungeonEntrance() {
        int roomX = overworld.getCurrentRoomX();
        int roomY = overworld.getCurrentRoomY();

        if (roomX == DUNGEON1_ENTRANCE_ROOM_X && roomY == DUNGEON1_ENTRANCE_ROOM_Y) {
            int tileX = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2) / ZeldaRoom.TILE_SIZE;
            int tileY = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2) / ZeldaRoom.TILE_SIZE;

            if (tileX == DUNGEON1_ENTRANCE_TILE_X && tileY == DUNGEON1_ENTRANCE_TILE_Y && keyHandler.upPressed) {
                enterDungeon(1);
            }
        }
    }

    private void enterDungeon(int level) {
        audioManager.stopMusic();
        audioManager.playSFX(SFX_STAIRS);
        audioManager.playMusic(MUSIC_DUNGEON);

        currentDungeon = new ZeldaDungeon(level, "LEVEL-" + level);
        currentDungeon.initialize(dungeonRenderer, overworld.getCollisionMap());
        player.setPosition(ZeldaDungeon.ENTRANCE_SPAWN_X, ZeldaDungeon.ENTRANCE_SPAWN_Y);

        hud.setInDungeon(true, level);
        state = GameState.DUNGEON;
    }

    private void exitDungeon() {
        audioManager.stopMusic();
        audioManager.playMusic(MUSIC_OVERWORLD);

        hud.setInDungeon(false, 0);
        currentDungeon = null;

        player.setPosition(PLAYER_START_X, PLAYER_START_Y);
        state = GameState.PLAYING;
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

            // check sword combat
            if (player.isAttacking() && player.hasSword()) {
                Rectangle swordBox = player.getSwordHitbox();
                if (swordBox != null) {
                    for (ZeldaEnemy e : room.getEnemies()) {
                        if (e.isAlive() && swordBox.intersects(e.getHitbox())) {
                            e.damage(1);
                            audioManager.playSFX(SFX_ENEMY_HIT);
                            if (!e.isAlive()) audioManager.playSFX(SFX_ENEMY_DIE);
                        }
                    }
                }
            }

            // check enemy contact damage
            for (ZeldaEnemy e : room.getEnemies()) {
                if (e.isAlive() && e.canDamage() && e.getHitbox().intersects(player.getHitbox())) {
                    player.takeDamage(e.getDamage(), e.getX(), e.getY());
                    audioManager.playSFX(SFX_HURT);
                }
            }

            for (Projectile p : room.getProjectiles()) {
                if (p.isActive() && !p.isPlayerProjectile() && p.getHitbox().intersects(player.getHitbox())) {
                    player.takeDamage(1, p.getX(), p.getY());
                    p.deactivate();
                    audioManager.playSFX(SFX_HURT);
                }
            }

            checkDungeonRoomTransition();
        }

        if (!player.isAlive()) {
            audioManager.stopMusic();
            audioManager.playMusic(MUSIC_GAME_OVER);
            state = GameState.GAME_OVER;
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

        if (dir == DungeonRoom.DOOR_SOUTH && currentDungeon.getCurrentRoomX() == ZeldaDungeon.ENTRANCE_ROOM_X
            && currentDungeon.getCurrentRoomY() == ZeldaDungeon.ENTRANCE_ROOM_Y) {
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
            int roomX = overworld.getCurrentRoomX();
            int roomY = overworld.getCurrentRoomY();

            switch (transitionDir) {
                case 0: roomY--; player.setPosition(player.getWorldX(), ZeldaRoom.ROOM_PLAY_BOTTOM - ZeldaPlayer.HEIGHT - 8); break;
                case 1: roomX++; player.setPosition(ZeldaRoom.ROOM_PLAY_LEFT + 8, player.getWorldY()); break;
                case 2: roomY++; player.setPosition(player.getWorldX(), ZeldaRoom.ROOM_PLAY_TOP + 8); break;
                case 3: roomX--; player.setPosition(ZeldaRoom.ROOM_PLAY_RIGHT - ZeldaPlayer.WIDTH - 8, player.getWorldY()); break;
            }

            overworld.setCurrentRoom(roomX, roomY);
            state = GameState.PLAYING;
        }
    }

    private void updatePaused() {
        if (keyHandler.escapePressed) {
            state = (currentDungeon != null) ? GameState.DUNGEON : GameState.PLAYING;
        }
    }

    private void updateGameOver() {
        if (keyHandler.startPressed) {
            audioManager.stopMusic();
            audioManager.playMusic(MUSIC_TITLE);
            state = GameState.TITLE_SCREEN;
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
        }
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
            currentDungeon.getCurrentRoom().render(g2);
        }
        player.render(g2);
        g2.translate(0, -HUD_HEIGHT);
    }

    private void renderTransition(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void renderPaused(Graphics2D g2) {
        if (currentDungeon != null) renderDungeon(g2);
        else renderPlaying(g2);

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString("PAUSED", 100, 120);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.drawString("PRESS ESC TO RESUME", 68, 140);
    }

    private void renderGameOver(Graphics2D g2) {
        g2.setColor(new Color(80, 0, 0));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.drawString("GAME OVER", 72, 100);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.drawString("PRESS ENTER TO CONTINUE", 52, 140);
    }

    public void startNewGame(String name, int saveSlot) {
        this.playerName = name;
        this.currentSaveSlot = saveSlot;

        player = new ZeldaPlayer(PLAYER_START_X, PLAYER_START_Y, keyHandler);
        player.setName(name);

        overworld = new Overworld();
        overworld.initialize();

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

            player = new ZeldaPlayer(data.playerX, data.playerY, keyHandler);
            player.setName(data.playerName);
            player.setHealth(data.health);
            player.setMaxHealth(data.maxHealth);
            player.setRupees(data.rupees);
            player.setKeys(data.keys);
            player.setBombs(data.bombs);

            overworld = new Overworld();
            overworld.initialize();
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
