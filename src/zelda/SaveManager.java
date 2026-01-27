package zelda;

import java.io.*;
import java.util.Properties;

public class SaveManager {
    private static final String SAVE_DIR = "saves/";
    
    public static class SaveData {
        public String playerName;
        public int health;
        public int maxHealth;
        public int rupees;
        public int keys;
        public int bombs;
        public int playerX;
        public int playerY;
        public int roomX;
        public int roomY;
        public boolean hasSword;
        public long playTime;
        
        public SaveData() {}
    }
    
    public SaveManager() {
        File saveDir = new File(SAVE_DIR);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
    }
    
    public void createNewSave(int slot, String playerName) {
        SaveData data = new SaveData();
        data.playerName = playerName;
        data.health = 6;
        data.maxHealth = 6;
        data.rupees = 0;
        data.keys = 0;
        data.bombs = 0;
        data.playerX = 128;
        data.playerY = 88;
        data.roomX = 7;
        data.roomY = 7;
        data.hasSword = false;
        data.playTime = 0;
        
        writeSaveFile(slot, data);
    }
    
    public void saveGame(int slot, ZeldaPlayer player, int roomX, int roomY) {
        SaveData data = new SaveData();
        data.playerName = player.getName();
        data.health = player.getHealth();
        data.maxHealth = player.getMaxHealth();
        data.rupees = player.getRupees();
        data.keys = player.getKeys();
        data.bombs = player.getBombs();
        data.playerX = player.getWorldX();
        data.playerY = player.getWorldY();
        data.roomX = roomX;
        data.roomY = roomY;
        data.hasSword = player.hasSword();
        
        writeSaveFile(slot, data);
    }
    
    private void writeSaveFile(int slot, SaveData data) {
        Properties props = new Properties();
        props.setProperty("playerName", data.playerName);
        props.setProperty("health", String.valueOf(data.health));
        props.setProperty("maxHealth", String.valueOf(data.maxHealth));
        props.setProperty("rupees", String.valueOf(data.rupees));
        props.setProperty("keys", String.valueOf(data.keys));
        props.setProperty("bombs", String.valueOf(data.bombs));
        props.setProperty("playerX", String.valueOf(data.playerX));
        props.setProperty("playerY", String.valueOf(data.playerY));
        props.setProperty("roomX", String.valueOf(data.roomX));
        props.setProperty("roomY", String.valueOf(data.roomY));
        props.setProperty("hasSword", String.valueOf(data.hasSword));
        props.setProperty("playTime", String.valueOf(data.playTime));
        
        try (FileOutputStream fos = new FileOutputStream(SAVE_DIR + "save" + slot + ".dat")) {
            props.store(fos, "Zelda Save File");
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }
    
    public SaveData loadGame(int slot) {
        File saveFile = new File(SAVE_DIR + "save" + slot + ".dat");
        if (!saveFile.exists()) {
            return null;
        }
        
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(saveFile)) {
            props.load(fis);
            
            SaveData data = new SaveData();
            data.playerName = props.getProperty("playerName", "LINK");
            data.health = Integer.parseInt(props.getProperty("health", "6"));
            data.maxHealth = Integer.parseInt(props.getProperty("maxHealth", "6"));
            data.rupees = Integer.parseInt(props.getProperty("rupees", "0"));
            data.keys = Integer.parseInt(props.getProperty("keys", "0"));
            data.bombs = Integer.parseInt(props.getProperty("bombs", "0"));
            data.playerX = Integer.parseInt(props.getProperty("playerX", "128"));
            data.playerY = Integer.parseInt(props.getProperty("playerY", "88"));
            data.roomX = Integer.parseInt(props.getProperty("roomX", "7"));
            data.roomY = Integer.parseInt(props.getProperty("roomY", "7"));
            data.hasSword = Boolean.parseBoolean(props.getProperty("hasSword", "false"));
            data.playTime = Long.parseLong(props.getProperty("playTime", "0"));
            
            return data;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load save: " + e.getMessage());
            return null;
        }
    }
    
    public void deleteSave(int slot) {
        File saveFile = new File(SAVE_DIR + "save" + slot + ".dat");
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }
    
    public boolean saveExists(int slot) {
        return new File(SAVE_DIR + "save" + slot + ".dat").exists();
    }
}
