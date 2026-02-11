package zelda;

import java.util.HashMap;

public class Overworld {
    public static final int MAP_COLS = OverworldRenderer.MAP_COLS;
    public static final int MAP_ROWS = OverworldRenderer.MAP_ROWS;
    public static final int START_ROOM_X = 7;
    public static final int START_ROOM_Y = 7;

    private HashMap<String, ZeldaRoom> rooms = new HashMap<>();
    private ZeldaRoom currentRoom;
    private int currentRoomX = START_ROOM_X;
    private int currentRoomY = START_ROOM_Y;

    private OverworldRenderer renderer;
    private CollisionMap collisionMap;

    public Overworld() {}

    public void initialize() {
        renderer = new OverworldRenderer();
        collisionMap = new CollisionMap();
        collisionMap.setRenderer(renderer);

        for (int x = 0; x < MAP_COLS; x++) {
            for (int y = 0; y < MAP_ROWS; y++) {
                rooms.put(x + "," + y, new ZeldaRoom(x, y));
            }
        }

        setCurrentRoom(START_ROOM_X, START_ROOM_Y);
    }

    public void setCurrentRoom(int x, int y) {
        currentRoomX = x;
        currentRoomY = y;
        String key = x + "," + y;
        currentRoom = rooms.get(key);
        if (currentRoom != null) {
            currentRoom.initialize(renderer, collisionMap);
        }
    }

    public ZeldaRoom getCurrentRoom() { return currentRoom; }
    public int getCurrentRoomX() { return currentRoomX; }
    public int getCurrentRoomY() { return currentRoomY; }
    public OverworldRenderer getRenderer() { return renderer; }
    public CollisionMap getCollisionMap() { return collisionMap; }

    public boolean hasRoom(int x, int y) {
        return x >= 0 && x < MAP_COLS && y >= 0 && y < MAP_ROWS;
    }

    public ZeldaRoom getRoom(int x, int y) {
        return rooms.get(x + "," + y);
    }
}
