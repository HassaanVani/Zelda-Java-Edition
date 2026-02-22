package zelda;

import engine.KeyHandler;
import java.awt.*;

public class ZeldaGame {
    public enum GameState {
        TITLE_SCREEN, PLAYING, PAUSED, GAME_OVER, GAME_WIN, ROOM_TRANSITION, CAVE, DUNGEON,
        FADE_OUT, FADE_IN, CAVE_WALK_IN
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

    private static final String SFX_SWORD = "04. Small Item Get.wav"; // Closest match
    private static final String SFX_ENEMY_HIT = "04. Small Item Get.wav";
    private static final String SFX_ENEMY_DIE = "04. Small Item Get.wav";
    private static final String SFX_HURT = "04. Small Item Get.wav";
    private static final String SFX_ITEM = "04. Small Item Get.wav";
    private static final String SFX_KEY = "06. Secret.wav";
    private static final String SFX_DOOR = "05. Discovery.wav";
    private static final String SFX_FANFARE = "07. Collect Item.wav";
    private static final String SFX_STAIRS = "06. Secret.wav";
    private static final String SFX_TRIFORCE = "09. Triforce Shard Collected.wav";
    private static final String SFX_FLUTE = "08 Flute.wav";
    private static final String SFX_LINK_DIES = "10. Link Dies.wav";

    private static final String MUSIC_TITLE = "01. Title Screen.wav";
    private static final String MUSIC_OVERWORLD = "02. Overworld of Hyrule.wav";
    private static final String MUSIC_DUNGEON = "03. Dungeon Theme.wav";
    private static final String MUSIC_GAME_OVER = "11 Game Over.wav";
    private static final String MUSIC_DEATH_MOUNTAIN = "12. Death Mountain Dungeon.wav";
    private static final String MUSIC_GANON_DEFEATED = "13. Ganon Defeated.wav";
    private static final String MUSIC_ZELDA_RESCUED = "14. Zelda Is Rescued.wav";
    private static final String MUSIC_ENDING = "15 Ending Theme.wav";

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

    // Lost Woods / Lost Hills maze tracking
    // Lost Woods (5,4): correct sequence N,W,S,W
    // Lost Hills (5,1): correct sequence N,N,N,N
    private int[] mazeSequence = new int[4];
    private int mazeStep = 0;
    private int lastMazeRoomX = -1, lastMazeRoomY = -1;

    private int deathAnimTimer = 0;
    private int deathSpinFrame = 0;

    private int fadeTimer = 0;
    private boolean fadeIn = false;
    private float fadeAlpha = 0f;
    private GameState fadeTargetState = GameState.PLAYING;
    private Runnable fadeCallback = null;

    private boolean godMode = false;
    private boolean gKeyReleased = true;

    private int gameOverCursor = 0; // 0=CONTINUE, 1=SAVE, 2=RETRY
    private int gameOverInputDelay = 0;
    private int lowHealthBeepTimer = 0;

    // Recorder warp: cycle through visited dungeon entrances
    private int recorderWarpIndex = 0;

    // Cave walk-in animation
    private int caveWalkTimer = 0;
    private static final int CAVE_WALK_DURATION = 20; // frames to walk into cave
    private double caveEntranceX, caveEntranceY; // target position for walk-in
    private Runnable caveWalkCallback; // what to do after walk completes

    // Dungeon entrance locations (roomX, roomY) for dungeons 1-9
    private static final int[][] DUNGEON_ENTRANCES = {
        {7, 3}, {12, 3}, {4, 4}, {5, 2}, {10, 1},
        {2, 2}, {12, 1}, {0, 3}, {6, 0}
    };

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
            case CAVE_WALK_IN: updateCaveWalkIn(); break;
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

        // Check bomb/candle secrets in overworld
        for (Projectile p : room.getProjectiles()) {
            if (p.isActive() && p.isPlayerProjectile()) {
                // Bomb explosion -> check BOMB_WALL secret
                if (p.isBomb() && p.getBombPhase() == 2) {
                    if (room.checkBombSecret(p.getX() + 12, p.getY() + 12)) {
                        audioManager.playSFX(SFX_KEY); // Secret SFX
                    }
                }
                // Candle fire -> check BURN_BUSH secret
                if (p.isCandleFire() || (p.getColor() != null && p.getColor().equals(Color.ORANGE)
                        && !p.isMoving() && p.isPlayerProjectile())) {
                    if (room.checkCandleSecret(p.getX(), p.getY())) {
                        audioManager.playSFX(SFX_KEY); // Secret SFX
                    }
                }
            }
        }

        // Fairy fountain gradual healing
        if (room.isAtFairyFountain(player.getWorldX(), player.getWorldY())) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(1); // Heal 1/2 heart per frame (fast)
            }
        }

        // Recorder: warp to next visited dungeon entrance (only previously entered ones)
        if (player.consumeRecorderFlag()) {
            audioManager.playSFX(SFX_FLUTE);
            Inventory inv = player.getInventory();
            int startIdx = recorderWarpIndex;
            for (int i = 0; i < 9; i++) {
                int idx = (startIdx + i) % 9;
                if (!inv.hasVisitedDungeon(idx + 1)) continue; // Skip unvisited
                int[] entrance = DUNGEON_ENTRANCES[idx];
                recorderWarpIndex = (idx + 1) % 9;
                startFadeOut(() -> {
                    overworld.setCurrentRoom(entrance[0], entrance[1]);
                    player.setPosition(PLAYER_START_X, PLAYER_START_Y);
                    state = GameState.PLAYING;
                    startFadeIn();
                });
                break;
            }
        }

        // Food: place bait on the ground (attracts enemies)
        if (player.consumeFoodFlag()) {
            // Place food item at player's position
            room.getItems().add(new Item(player.getWorldX(), player.getWorldY(),
                Item.ItemType.FAIRY)); // Visual placeholder — food on ground
        }

        // Kill-all secret: reveal when room cleared of enemies
        if (room.isCleared() && !room.isSecretRevealed()) {
            if (room.checkKillAllSecret()) {
                audioManager.playSFX(SFX_KEY);
            }
        }

        // Push rock/grave secret detection
        if (player.isMoving() && !room.isSecretRevealed()) {
            int pushDir = player.getDirection();
            boolean hasBracelet = player.getInventory().hasBracelet();
            if (room.checkPushSecret(player.getWorldX() + ZeldaPlayer.WIDTH / 2,
                    player.getWorldY() + ZeldaPlayer.HEIGHT / 2, pushDir, hasBracelet)) {
                audioManager.playSFX(SFX_KEY); // Secret SFX
            }
        }

        // Raft travel: auto-activate when Link with Raft steps on a dock tile facing water
        if (player.getInventory().hasRaft() && player.isMoving()) {
            checkRaftTravel(room);
        }

        checkRoomTransition();
        checkCaveEntrance();
        checkDungeonEntrance();

        if (!godMode) {
            int cx = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2);
            int cy = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2);

            if (!room.isWalkable(cx, cy)) {
                // Stepladder: auto-bridge 1-tile water/pit gaps
                boolean ladderCross = false;
                if (player.getInventory().hasLadder()) {
                    TileType currentTile = room.getTileAt(cx, cy);
                    if (currentTile == TileType.WATER || currentTile == TileType.PIT) {
                        // Check if tile beyond the gap (in facing direction) is walkable
                        int beyondX = cx, beyondY = cy;
                        switch (player.getDirection()) {
                            case ZeldaPlayer.DIR_UP:    beyondY -= ZeldaRoom.TILE_SIZE; break;
                            case ZeldaPlayer.DIR_DOWN:  beyondY += ZeldaRoom.TILE_SIZE; break;
                            case ZeldaPlayer.DIR_LEFT:  beyondX -= ZeldaRoom.TILE_SIZE; break;
                            case ZeldaPlayer.DIR_RIGHT: beyondX += ZeldaRoom.TILE_SIZE; break;
                        }
                        if (room.isWalkable(beyondX, beyondY)) {
                            ladderCross = true; // Allow passage
                        }
                    }
                }
                if (!ladderCross) {
                    player.revertPosition();
                }
            }
        }

        player.clampToPlayArea();

        if (!godMode) {
            combatManager.checkCombat(player, room, audioManager);

            if (!player.isAlive()) {
                startDeathAnimation();
            }
        }

        // Low health warning beep (~4 beeps/sec when <= 1 heart)
        if (player.isAlive() && player.getHealth() <= 2) {
            lowHealthBeepTimer++;
            if (lowHealthBeepTimer % 15 == 0) {
                audioManager.playSFX(SFX_HURT);
            }
        } else {
            lowHealthBeepTimer = 0;
        }
    }

    private void startDeathAnimation() {
        audioManager.stopMusic();
        audioManager.playSFX(SFX_LINK_DIES);
        deathAnimTimer = DEATH_ANIM_DURATION;
        deathSpinFrame = 0;
        gameOverCursor = 0;
        gameOverInputDelay = 10;
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

        // Lost Woods (5,4): loops unless N,W,S,W sequence
        // Lost Hills (5,1): loops unless N,N,N,N sequence
        int[] resolved = resolveMazeRoom(roomX, roomY, dir, nx, ny);
        nx = resolved[0];
        ny = resolved[1];

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

    /**
     * Handles Lost Woods and Lost Hills maze logic.
     * Lost Woods (5,4): must exit N,W,S,W to progress — otherwise loops back.
     * Lost Hills (5,1): must exit N,N,N,N to progress — otherwise loops back.
     */
    private int[] resolveMazeRoom(int fromX, int fromY, int dir, int defaultNX, int defaultNY) {
        // Lost Woods at (5,4)
        if (fromX == 5 && fromY == 4) {
            // Track maze exits
            if (lastMazeRoomX != 5 || lastMazeRoomY != 4) {
                mazeStep = 0; // Reset if we just entered
            }
            lastMazeRoomX = 5;
            lastMazeRoomY = 4;

            // Correct sequence: N(0), W(3), S(2), W(3)
            int[] correctSeq = {0, 3, 2, 3};
            if (mazeStep < 4) {
                mazeSequence[mazeStep] = dir;
                if (dir == correctSeq[mazeStep]) {
                    mazeStep++;
                    if (mazeStep >= 4) {
                        mazeStep = 0;
                        return new int[]{defaultNX, defaultNY}; // Progress through!
                    }
                } else {
                    mazeStep = 0; // Wrong — reset
                }
            }
            // Loop back to same room
            return new int[]{5, 4};
        }

        // Lost Hills at (5,1)
        if (fromX == 5 && fromY == 1) {
            if (lastMazeRoomX != 5 || lastMazeRoomY != 1) {
                mazeStep = 0;
            }
            lastMazeRoomX = 5;
            lastMazeRoomY = 1;

            // Correct sequence: N(0), N(0), N(0), N(0)
            if (dir == 0) {
                mazeStep++;
                if (mazeStep >= 4) {
                    mazeStep = 0;
                    return new int[]{defaultNX, defaultNY}; // Progress through!
                }
            } else {
                mazeStep = 0;
            }
            // Loop back to same room
            return new int[]{5, 1};
        }

        // Not a maze room — reset tracking and pass through normally
        lastMazeRoomX = fromX;
        lastMazeRoomY = fromY;
        mazeStep = 0;
        return new int[]{defaultNX, defaultNY};
    }

    private void checkCaveEntrance() {
        ZeldaRoom room = overworld.getCurrentRoom();
        if (room == null) return;

        int tileX = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2) / ZeldaRoom.TILE_SIZE;
        int tileY = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2) / ZeldaRoom.TILE_SIZE;

        // Normal cave entrance
        if (room.hasCaveEntrance()) {
            int caveTX = room.getCaveTileX();
            int caveTY = room.getCaveTileY();
            boolean atEntrance = Math.abs(tileX - caveTX) <= 1 && Math.abs(tileY - caveTY) <= 1;
            if (atEntrance && keyHandler.upPressed) {
                audioManager.playSFX(SFX_STAIRS);
                // Walk-in animation: Link walks up into cave, then fade to cave interior
                startCaveWalkIn(caveTX * ZeldaRoom.TILE_SIZE + 4, (caveTY - 1) * ZeldaRoom.TILE_SIZE, () -> {
                    cave.enter(player, overworld.getCurrentRoomX(), overworld.getCurrentRoomY());
                    state = GameState.CAVE;
                    startFadeIn();
                });
                return;
            }
        }

        // Revealed secret cave entrance (bomb wall, burn bush, push rock/grave)
        if (room.isSecretRevealed()) {
            RoomData.RoomSecret secret = room.getRevealedSecret();
            if (secret != null && secret.revealedCaveId >= 0) {
                boolean atSecret = Math.abs(tileX - secret.tileX) <= 1
                                && Math.abs(tileY - secret.tileY) <= 1;
                if (atSecret && keyHandler.upPressed) {
                    audioManager.playSFX(SFX_STAIRS);
                    final int caveId = secret.revealedCaveId;
                    startCaveWalkIn(secret.tileX * ZeldaRoom.TILE_SIZE + 4, (secret.tileY - 1) * ZeldaRoom.TILE_SIZE, () -> {
                        cave.enterById(player, caveId);
                        state = GameState.CAVE;
                        startFadeIn();
                    });
                }
            }
        }
    }

    // Raft routes: {fromRoomX, fromRoomY, toRoomX, toRoomY, destPlayerX, destPlayerY}
    // NES 1st Quest has dock tiles at specific screens for crossing water
    private static final int[][] RAFT_ROUTES = {
        {5, 4, 5, 2, 120, 128},   // Forest area to D4/Stepladder area
    };

    private void checkRaftTravel(ZeldaRoom room) {
        int cx = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2);
        int cy = (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2);
        TileType tile = room.getTileAt(cx, cy);
        if (tile != TileType.DOCK && tile != TileType.RAFT_DOCK) return;

        int roomX = overworld.getCurrentRoomX();
        int roomY = overworld.getCurrentRoomY();
        for (int[] route : RAFT_ROUTES) {
            if (route[0] == roomX && route[1] == roomY) {
                final int destRX = route[2], destRY = route[3];
                final double destPX = route[4], destPY = route[5];
                startFadeOut(() -> {
                    overworld.setCurrentRoom(destRX, destRY);
                    player.setPosition(destPX, destPY);
                    player.onScreenChange();
                    state = GameState.PLAYING;
                    startFadeIn();
                });
                return;
            }
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
        audioManager.playMusic(level == 9 ? MUSIC_DEATH_MOUNTAIN : MUSIC_DUNGEON);

        player.getInventory().markDungeonVisited(level);

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

            // Recorder in dungeon: shrinks Digdogger boss
            if (player.consumeRecorderFlag()) {
                audioManager.playSFX(SFX_FLUTE);
                for (ZeldaEnemy e : room.getEnemies()) {
                    if (e instanceof zelda.bosses.Digdogger) {
                        ((zelda.bosses.Digdogger) e).shrink();
                    }
                }
            }

            // Check ZELDA rescue (Level 9 win condition)
            if (room.isCleared() && currentDungeon.getDungeonNumber() == 9
                    && room.hasZeldaItem()) {
                audioManager.stopMusic();
                audioManager.playSFX(SFX_TRIFORCE);
                audioManager.playMusicOnce(MUSIC_ZELDA_RESCUED);
                winScreenTimer = 0;
                saveCurrentGame();
                state = GameState.GAME_WIN;
                return;
            }

            // Check triforce collection — triggers save + exit
            if (player.getInventory().hasTriforce(currentDungeon.getDungeonNumber())
                    && !currentDungeon.isTriforceCollected()) {
                currentDungeon.setTriforceCollected(true);
                currentDungeon.setBossDefeated(true);
                audioManager.playSFX(SFX_TRIFORCE);
                saveCurrentGame();
                exitDungeon();
                return;
            }

            // Check Wallmaster grab — teleport to dungeon entrance
            for (ZeldaEnemy e : room.getEnemies()) {
                if (e instanceof zelda.enemies.Wallmaster) {
                    zelda.enemies.Wallmaster wm = (zelda.enemies.Wallmaster) e;
                    if (wm.hasGrabbedPlayer()) {
                        wm.resetGrab();
                        currentDungeon.setCurrentRoom(
                            currentDungeon.getEntranceRoomX(),
                            currentDungeon.getEntranceRoomY());
                        player.setPosition(ZeldaDungeon.ENTRANCE_SPAWN_X,
                            ZeldaDungeon.ENTRANCE_SPAWN_Y);
                        return;
                    }
                }
            }

            // Check stairway transition (after block push reveals stairway)
            if (currentDungeon.checkStairwayTransition(player)) {
                return; // Transitioned to a new room
            }

            checkDungeonRoomTransition();
        }

        // Low health warning beep in dungeon
        if (player.isAlive() && player.getHealth() <= 2) {
            lowHealthBeepTimer++;
            if (lowHealthBeepTimer % 15 == 0) {
                audioManager.playSFX(SFX_HURT);
            }
        } else {
            lowHealthBeepTimer = 0;
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

    /** Start the cave walk-in animation: Link walks upward into the cave entrance. */
    private void startCaveWalkIn(double targetX, double targetY, Runnable callback) {
        caveEntranceX = targetX;
        caveEntranceY = targetY;
        caveWalkTimer = CAVE_WALK_DURATION;
        caveWalkCallback = callback;
        state = GameState.CAVE_WALK_IN;
    }

    private void updateCaveWalkIn() {
        caveWalkTimer--;
        // Move player upward toward cave entrance
        double progress = 1.0 - (double)caveWalkTimer / CAVE_WALK_DURATION;
        double startX = player.getWorldX();
        double startY = player.getWorldY();
        // Interpolate toward entrance position
        double newX = startX + (caveEntranceX - startX) * 0.15;
        double newY = startY + (caveEntranceY - startY) * 0.15;
        player.setPosition(newX, newY);

        if (caveWalkTimer <= 0) {
            // Walk complete, now fade to cave interior
            startFadeOut(caveWalkCallback);
        }
    }

    private void renderCaveWalkIn(Graphics2D g2) {
        // Render the overworld scene with player walking into cave
        renderPlaying(g2);
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
            // Start game over music after death animation completes
            if (deathAnimTimer == 0) {
                audioManager.playMusic(MUSIC_GAME_OVER);
            }
            return;
        }

        // Debounce input
        if (gameOverInputDelay > 0) {
            gameOverInputDelay--;
            return;
        }

        // Cursor navigation
        if (keyHandler.upPressed) {
            gameOverCursor = (gameOverCursor + 2) % 3;
            gameOverInputDelay = 8;
        } else if (keyHandler.downPressed) {
            gameOverCursor = (gameOverCursor + 1) % 3;
            gameOverInputDelay = 8;
        }

        if (keyHandler.startPressed || keyHandler.enterPressed) {
            switch (gameOverCursor) {
                case 0: // CONTINUE - respawn at start/dungeon entrance with 3 hearts
                    Inventory inv = player.getInventory();
                    inv.setHealth(6); // NES: 3 hearts on continue

                    if (currentDungeon != null) {
                        // Respawn at dungeon entrance
                        currentDungeon.setCurrentRoom(
                            currentDungeon.getEntranceRoomX(),
                            currentDungeon.getEntranceRoomY());
                        player.setPosition(ZeldaDungeon.ENTRANCE_SPAWN_X,
                            ZeldaDungeon.ENTRANCE_SPAWN_Y);
                        audioManager.stopMusic();
                        audioManager.playMusic(
                            currentDungeon.getDungeonNumber() == 9
                                ? MUSIC_DEATH_MOUNTAIN : MUSIC_DUNGEON);
                        state = GameState.DUNGEON;
                    } else {
                        currentDungeon = null;
                        hud.setInDungeon(false, 0);
                        player.setPosition(PLAYER_START_X, PLAYER_START_Y);
                        overworld.setCurrentRoom(Overworld.START_ROOM_X, Overworld.START_ROOM_Y);
                        audioManager.stopMusic();
                        audioManager.playMusic(MUSIC_OVERWORLD);
                        state = GameState.PLAYING;
                    }
                    break;
                case 1: // SAVE - save and return to title
                    player.getInventory().setHealth(6);
                    saveCurrentGame();
                    currentDungeon = null;
                    hud.setInDungeon(false, 0);
                    hud.setDungeon(null);
                    audioManager.stopMusic();
                    audioManager.playMusic(MUSIC_TITLE);
                    state = GameState.TITLE_SCREEN;
                    break;
                case 2: // RETRY - return to title without saving
                    currentDungeon = null;
                    hud.setInDungeon(false, 0);
                    hud.setDungeon(null);
                    audioManager.stopMusic();
                    audioManager.playMusic(MUSIC_TITLE);
                    state = GameState.TITLE_SCREEN;
                    break;
            }
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
        winScreenTimer++;

        // Switch from rescue theme to ending theme after a few seconds
        if (winScreenTimer == 300) {
            audioManager.stopMusic();
            audioManager.playMusicOnce(MUSIC_ENDING);
        }

        if (winScreenTimer > 60 && (keyHandler.startPressed || keyHandler.enterPressed)) {
            // Return to title screen after winning
            winScreenTimer = 0;
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
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Phase 1: Screen brightens (0-60 frames)
        if (winScreenTimer < 60) {
            int brightness = (int)(winScreenTimer * 4.25);
            g2.setColor(new Color(brightness, brightness, brightness, 100));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            return;
        }

        // Golden triforce display (pulsing glow)
        int triCX = SCREEN_WIDTH / 2;
        int triTopY = 20;
        int triSize = 36;
        int phase = (winScreenTimer / 8) % 3;
        Color triColor;
        switch (phase) {
            case 0: triColor = new Color(255, 250, 180); break;
            case 1: triColor = new Color(240, 220, 60); break;
            default: triColor = new Color(255, 215, 0); break;
        }
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

        // Story text (scrolls in after triforce appears)
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.setColor(new Color(240, 220, 60));
        String msg1 = "CONGRATULATIONS!";
        int w1 = g2.getFontMetrics().stringWidth(msg1);
        g2.drawString(msg1, (SCREEN_WIDTH - w1) / 2, 105);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));

        String[] storyLines = {
            "YOU RESCUED ZELDA!",
            "",
            "THANKS LINK, YOU'RE",
            "THE HERO OF HYRULE.",
            "",
            "FINALLY, PEACE RETURNS",
            "TO HYRULE. THIS ENDS",
            "THE STORY."
        };

        int textStartY = 120;
        int lineH = 12;
        // Reveal lines gradually
        int linesVisible = Math.min(storyLines.length, (winScreenTimer - 60) / 30);
        for (int i = 0; i < linesVisible; i++) {
            int sw = g2.getFontMetrics().stringWidth(storyLines[i]);
            g2.drawString(storyLines[i], (SCREEN_WIDTH - sw) / 2, textStartY + i * lineH);
        }

        // Flashing "PRESS START" after all text shown
        if (linesVisible >= storyLines.length && (winScreenTimer / 20) % 2 == 0) {
            g2.setColor(new Color(100, 200, 100));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
            String msg3 = "PRESS START";
            int w3 = g2.getFontMetrics().stringWidth(msg3);
            g2.drawString(msg3, (SCREEN_WIDTH - w3) / 2, 228);
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
            case CAVE_WALK_IN: renderCaveWalkIn(g2); break;
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

            // Spinning Link outline
            if (player != null && t < 0.8f) {
                int cx = (int)(player.getWorldX() + ZeldaPlayer.WIDTH / 2);
                int cy = HUD_HEIGHT + (int)(player.getWorldY() + ZeldaPlayer.HEIGHT / 2);
                double angle = deathSpinFrame * Math.PI / 2;
                Graphics2D g2r = (Graphics2D)g2.create();
                g2r.rotate(angle, cx, cy);
                // Draw Link outline during spin
                g2r.setColor(new Color(255, 255, 255, Math.max(0, 255 - (int)(t * 400))));
                g2r.fillRect(cx - ZeldaPlayer.WIDTH / 2, cy - ZeldaPlayer.HEIGHT / 2,
                    ZeldaPlayer.WIDTH, ZeldaPlayer.HEIGHT);
                g2r.dispose();
            }
            return;
        }

        // Static game over screen with 3 NES options
        g2.setColor(new Color(
            NESPalette.GAME_OVER_BG.getRed(),
            NESPalette.GAME_OVER_BG.getGreen(),
            NESPalette.GAME_OVER_BG.getBlue()));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        String go = "GAME OVER";
        int gow = g2.getFontMetrics().stringWidth(go);
        g2.drawString(go, (SCREEN_WIDTH - gow) / 2, 80);

        // Three options: CONTINUE / SAVE / RETRY
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String[] options = {"CONTINUE", "SAVE", "RETRY"};
        int optStartY = 120;
        int optSpacing = 24;

        for (int i = 0; i < 3; i++) {
            int ow = g2.getFontMetrics().stringWidth(options[i]);
            int ox = (SCREEN_WIDTH - ow) / 2;
            int oy = optStartY + i * optSpacing;

            // Heart cursor next to selected option
            if (i == gameOverCursor) {
                g2.setColor(Color.RED);
                // Small heart/diamond cursor
                int curX = ox - 16;
                int curY = oy - 7;
                g2.fillRect(curX + 2, curY, 4, 2);
                g2.fillRect(curX + 1, curY + 2, 6, 2);
                g2.fillRect(curX + 2, curY + 4, 4, 2);
                g2.fillRect(curX + 3, curY + 6, 2, 2);
            }

            g2.setColor(i == gameOverCursor ? Color.RED : Color.WHITE);
            g2.drawString(options[i], ox, oy);
        }
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
