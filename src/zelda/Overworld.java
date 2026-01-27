package zelda;

import java.util.HashMap;

public class Overworld {
    private HashMap<String, ZeldaRoom> rooms = new HashMap<>();
    private ZeldaRoom currentRoom;
    private int currentRoomX = 7;
    private int currentRoomY = 7;
    
    public static final int MAP_WIDTH = 16;
    public static final int MAP_HEIGHT = 8;
    
    public Overworld() {}
    
    public void initialize() {
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                String key = x + "," + y;
                rooms.put(key, new ZeldaRoom(x, y));
            }
        }
        
        setCurrentRoom(7, 7);
    }
    
    public boolean hasRoom(int x, int y) {
        return x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT;
    }
    
    public ZeldaRoom getRoom(int x, int y) {
        String key = x + "," + y;
        if (!rooms.containsKey(key)) {
            rooms.put(key, new ZeldaRoom(x, y));
        }
        return rooms.get(key);
    }
    
    public void setCurrentRoom(int x, int y) {
        currentRoomX = x;
        currentRoomY = y;
        currentRoom = getRoom(x, y);
    }
    
    public ZeldaRoom getCurrentRoom() {
        return currentRoom;
    }
    
    public int getCurrentRoomX() { return currentRoomX; }
    public int getCurrentRoomY() { return currentRoomY; }
}
