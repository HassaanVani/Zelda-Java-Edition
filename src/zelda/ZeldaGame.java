package zelda;

import engine.KeyHandler;
import java.awt.*;

public class ZeldaGame {
    public enum GameState {
        TITLE_SCREEN,
        NAME_ENTRY,
        PLAYING,
        PAUSED,
        GAME_OVER,
        ROOM_TRANSITION,
        CAVE
    }
    
    private GameState state = GameState.TITLE_SCREEN;
    private KeyHandler keyHandler;
    
    private TitleScreen titleScreen;
    private ZeldaPlayer player;
    private Overworld overworld;
    private ZeldaDungeon currentDungeon;
    private ZeldaHUD hud;
    private AudioManager audioManager;
    private SaveManager saveManager;
    private CombatManager combatManager;
    private Cave cave;
    
    private int currentSaveSlot = -1;
    private String playerName = "LINK";
    
    private int transitionTimer = 0;
    private static final int TRANSITION_DURATION = 20;
    private int transitionDirX = 0;
    private int transitionDirY = 0;
    
    public ZeldaGame(KeyHandler keyHandler) {
        this.keyHandler = keyHandler;
        
        saveManager = new SaveManager();
        audioManager = new AudioManager();
        titleScreen = new TitleScreen(this, keyHandler);
        hud = new ZeldaHUD();
        combatManager = new CombatManager();
        
        audioManager.playMusic("01. Title Screen.wav");
    }
    
    public void startNewGame(String name, int saveSlot) {
        this.playerName = name;
        this.currentSaveSlot = saveSlot;
        
        player = new ZeldaPlayer(120, 128, keyHandler);
        player.setName(name);
        
        overworld = new Overworld();
        overworld.initialize();
        
        hud.setPlayer(player);
        
        saveManager.createNewSave(saveSlot, name);
        
        audioManager.stopMusic();
        audioManager.playMusic("02. Overworld of Hyrule.wav");
        
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
            audioManager.playMusic("02. Overworld of Hyrule.wav");
            
            state = GameState.PLAYING;
        }
    }
    
    public void saveGame() {
        if (currentSaveSlot >= 0 && player != null) {
            ZeldaRoom room = getCurrentRoom();
            saveManager.saveGame(currentSaveSlot, player, 
                room != null ? room.getRoomX() : 7,
                room != null ? room.getRoomY() : 7);
            audioManager.playSFX("06. Secret.wav");
        }
    }
    
    public void update() {
        switch (state) {
            case TITLE_SCREEN:
            case NAME_ENTRY:
                titleScreen.update();
                break;
                
            case PLAYING:
                updatePlaying();
                break;
                
            case ROOM_TRANSITION:
                updateTransition();
                break;
                
            case PAUSED:
                if (keyHandler.startPressed) {
                    state = GameState.PLAYING;
                    keyHandler.startPressed = false;
                }
                break;
                
            case GAME_OVER:
                if (keyHandler.startPressed) {
                    state = GameState.TITLE_SCREEN;
                    audioManager.stopMusic();
                    audioManager.playMusic("01. Title Screen.wav");
                    keyHandler.startPressed = false;
                }
                break;
                
            case CAVE:
                updateCave();
                break;
        }
    }
    
    private void updatePlaying() {
        if (keyHandler.startPressed) {
            state = GameState.PAUSED;
            keyHandler.startPressed = false;
            return;
        }
        
        if (keyHandler.selectPressed) {
            saveGame();
            keyHandler.selectPressed = false;
        }
        
        player.update();
        
        ZeldaRoom currentRoom = getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.update(player, combatManager, audioManager);
            
            int edge = checkScreenEdge();
            if (edge != -1) {
                startRoomTransition(edge);
            }
        }
        
        combatManager.update(player, currentRoom);
        
        if (player.isDead()) {
            state = GameState.GAME_OVER;
            audioManager.stopMusic();
            audioManager.playMusic("11 Game Over.wav");
        }
        
        ZeldaRoom cr = getCurrentRoom();
        if (cr != null && cr.getRoomX() == 7 && cr.getRoomY() == 7 && !player.hasSword()) {
            int px = player.getWorldX();
            int py = player.getWorldY();
            if (px >= 104 && px <= 144 && py <= 56) {
                enterCave();
            }
        }
    }
    
    private void enterCave() {
        if (cave == null) cave = new Cave();
        cave.enter();
        player.setPosition(120, 140);
        state = GameState.CAVE;
    }
    
    private void updateCave() {
        player.update();
        cave.update(player);
        
        if (cave.checkExit(player)) {
            state = GameState.PLAYING;
            player.setPosition(120, 64);
        }
    }
    
    private int checkScreenEdge() {
        int px = player.getWorldX();
        int py = player.getWorldY();
        
        if (px < 8) return 3;
        if (px > 256 - 24) return 1;
        if (py < 8) return 0;
        if (py > 176 - 24) return 2;
        
        return -1;
    }
    
    private void startRoomTransition(int direction) {
        ZeldaRoom current = getCurrentRoom();
        if (current == null) return;
        
        int newRoomX = current.getRoomX();
        int newRoomY = current.getRoomY();
        
        switch (direction) {
            case 0: newRoomY--; transitionDirX = 0; transitionDirY = -1; break;
            case 1: newRoomX++; transitionDirX = 1; transitionDirY = 0; break;
            case 2: newRoomY++; transitionDirX = 0; transitionDirY = 1; break;
            case 3: newRoomX--; transitionDirX = -1; transitionDirY = 0; break;
        }
        
        if (overworld.hasRoom(newRoomX, newRoomY)) {
            overworld.setCurrentRoom(newRoomX, newRoomY);
            
            switch (direction) {
                case 0: player.setPosition(player.getWorldX(), 140); break;
                case 1: player.setPosition(24, player.getWorldY()); break;
                case 2: player.setPosition(player.getWorldX(), 24); break;
                case 3: player.setPosition(224, player.getWorldY()); break;
            }
            
            transitionTimer = TRANSITION_DURATION;
            state = GameState.ROOM_TRANSITION;
        }
    }
    
    private void updateTransition() {
        transitionTimer--;
        if (transitionTimer <= 0) {
            state = GameState.PLAYING;
        }
    }
    
    public void render(Graphics2D g2) {
        switch (state) {
            case TITLE_SCREEN:
            case NAME_ENTRY:
                titleScreen.render(g2);
                break;
                
            case PLAYING:
            case ROOM_TRANSITION:
            case PAUSED:
                renderGame(g2);
                if (state == GameState.PAUSED) {
                    renderPauseOverlay(g2);
                }
                break;
                
            case GAME_OVER:
                renderGameOver(g2);
                break;
                
            case CAVE:
                renderCave(g2);
                break;
        }
    }
    
    private void renderGame(Graphics2D g2) {
        g2.translate(0, 56);
        
        ZeldaRoom currentRoom = getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.render(g2);
        }
        
        player.render(g2);
        
        combatManager.render(g2);
        
        g2.translate(0, -56);
        
        hud.render(g2, getCurrentRoom());
        
        if (state == GameState.ROOM_TRANSITION) {
            g2.setColor(new Color(0, 0, 0, (int)(200 * (transitionTimer / (float)TRANSITION_DURATION))));
            g2.fillRect(0, 56, 256, 176);
        }
    }
    
    private void renderCave(Graphics2D g2) {
        g2.translate(0, 56);
        cave.render(g2);
        player.render(g2);
        g2.translate(0, -56);
        hud.render(g2, null);
    }
    
    private void renderPauseOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, 256, 240);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        String pauseText = "PAUSED";
        int textWidth = g2.getFontMetrics().stringWidth(pauseText);
        g2.drawString(pauseText, (256 - textWidth) / 2, 120);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        String saveText = "SELECT to Save";
        textWidth = g2.getFontMetrics().stringWidth(saveText);
        g2.drawString(saveText, (256 - textWidth) / 2, 140);
    }
    
    private void renderGameOver(Graphics2D g2) {
        g2.setColor(new Color(139, 0, 0));
        g2.fillRect(0, 0, 256, 240);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String text = "GAME OVER";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (256 - textWidth) / 2, 100);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        text = "Press START to continue";
        textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (256 - textWidth) / 2, 140);
    }
    
    private ZeldaRoom getCurrentRoom() {
        if (currentDungeon != null) {
            return currentDungeon.getCurrentRoom();
        }
        return overworld != null ? overworld.getCurrentRoom() : null;
    }
    
    public void setState(GameState state) { this.state = state; }
    public GameState getState() { return state; }
    public SaveManager getSaveManager() { return saveManager; }
    public AudioManager getAudioManager() { return audioManager; }
}
