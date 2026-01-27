package zelda;

import java.util.HashMap;

public class ZeldaDungeon {
    private String dungeonName;
    private int dungeonNumber;
    
    private HashMap<String, ZeldaRoom> rooms = new HashMap<>();
    private ZeldaRoom currentRoom;
    private int currentRoomX = 0;
    private int currentRoomY = 0;
    
    private boolean hasMap = false;
    private boolean hasCompass = false;
    private boolean hasBossKey = false;
    private boolean bossDefeated = false;
    
    public ZeldaDungeon(int number, String name) {
        this.dungeonNumber = number;
        this.dungeonName = name;
    }
    
    public void initialize() {
        rooms.put("0,0", createDungeonRoom(0, 0));
        rooms.put("1,0", createDungeonRoom(1, 0));
        rooms.put("0,1", createDungeonRoom(0, 1));
        rooms.put("1,1", createDungeonRoom(1, 1));
        rooms.put("2,0", createBossRoom(2, 0));
        
        setCurrentRoom(0, 0);
    }
    
    private ZeldaRoom createDungeonRoom(int x, int y) {
        ZeldaRoom room = new ZeldaRoom(x, y);
        return room;
    }
    
    private ZeldaRoom createBossRoom(int x, int y) {
        ZeldaRoom room = new ZeldaRoom(x, y);
        return room;
    }
    
    public boolean hasRoom(int x, int y) {
        return rooms.containsKey(x + "," + y);
    }
    
    public ZeldaRoom getRoom(int x, int y) {
        return rooms.get(x + "," + y);
    }
    
    public void setCurrentRoom(int x, int y) {
        currentRoomX = x;
        currentRoomY = y;
        currentRoom = getRoom(x, y);
    }
    
    public ZeldaRoom getCurrentRoom() { return currentRoom; }
    public int getCurrentRoomX() { return currentRoomX; }
    public int getCurrentRoomY() { return currentRoomY; }
    public String getDungeonName() { return dungeonName; }
    public int getDungeonNumber() { return dungeonNumber; }
    public boolean hasMap() { return hasMap; }
    public boolean hasCompass() { return hasCompass; }
    public boolean hasBossKey() { return hasBossKey; }
    public boolean isBossDefeated() { return bossDefeated; }
    
    public void setHasMap(boolean has) { hasMap = has; }
    public void setHasCompass(boolean has) { hasCompass = has; }
    public void setHasBossKey(boolean has) { hasBossKey = has; }
    public void setBossDefeated(boolean defeated) { bossDefeated = defeated; }
}
