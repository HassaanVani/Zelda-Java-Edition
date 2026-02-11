package zelda;

import java.util.HashMap;

public class ZeldaDungeon {
    public static final int ENTRANCE_ROOM_X = 3;
    public static final int ENTRANCE_ROOM_Y = 0;
    public static final double ENTRANCE_SPAWN_X = 120;
    public static final double ENTRANCE_SPAWN_Y = 144;

    private String dungeonName;
    private int dungeonNumber;

    private HashMap<String, DungeonRoom> rooms = new HashMap<>();
    private DungeonRoom currentRoom;
    private int currentRoomX = 0;
    private int currentRoomY = 0;

    private boolean hasMap = false;
    private boolean hasCompass = false;
    private boolean hasBossKey = false;
    private boolean bossDefeated = false;
    private boolean triforceCollected = false;

    private int mapWidth = 0;
    private int mapHeight = 0;

    private DungeonRenderer renderer;
    private CollisionMap collisionMap;

    public ZeldaDungeon(int number, String name) {
        this.dungeonNumber = number;
        this.dungeonName = name;
    }

    public void initialize(DungeonRenderer r, CollisionMap c) {
        this.renderer = r;
        this.collisionMap = c;

        switch (dungeonNumber) {
            case 1: buildLevel1(); break;
            default: buildLevel1(); break;
        }
    }

    private void buildLevel1() {
        mapWidth = 6;
        mapHeight = 4;

        addRoom(3, 0, 1, 0, "stalfos");
        addRoom(2, 0, 0, 0, "keese");
        addRoom(4, 0, 2, 0, "keese");
        addRoom(3, 1, 1, 1, "stalfos");
        addRoom(2, 1, 0, 1, "stalfos");
        addRoom(4, 1, 2, 1, "stalfos");
        addRoom(3, 2, 1, 2, "keese");

        DungeonRoom entrance = getRoom(ENTRANCE_ROOM_X, ENTRANCE_ROOM_Y);
        if (entrance != null) {
            entrance.setDoor(DungeonRoom.DOOR_NORTH, DungeonRoom.DoorState.OPEN);
            entrance.setDoor(DungeonRoom.DOOR_WEST, DungeonRoom.DoorState.OPEN);
            entrance.setDoor(DungeonRoom.DOOR_EAST, DungeonRoom.DoorState.OPEN);
        }

        DungeonRoom westRoom = getRoom(2, 0);
        if (westRoom != null) {
            westRoom.setDoor(DungeonRoom.DOOR_EAST, DungeonRoom.DoorState.OPEN);
            westRoom.setRoomItem(Item.ItemType.KEY);
        }

        DungeonRoom eastRoom = getRoom(4, 0);
        if (eastRoom != null) {
            eastRoom.setDoor(DungeonRoom.DOOR_WEST, DungeonRoom.DoorState.OPEN);
            eastRoom.setRoomItem(Item.ItemType.KEY);
        }

        DungeonRoom northRoom = getRoom(3, 1);
        if (northRoom != null) {
            northRoom.setDoor(DungeonRoom.DOOR_SOUTH, DungeonRoom.DoorState.OPEN);
            northRoom.setDoor(DungeonRoom.DOOR_WEST, DungeonRoom.DoorState.LOCKED);
            northRoom.setDoor(DungeonRoom.DOOR_EAST, DungeonRoom.DoorState.LOCKED);
            northRoom.setDoor(DungeonRoom.DOOR_NORTH, DungeonRoom.DoorState.LOCKED);
        }

        DungeonRoom westInner = getRoom(2, 1);
        if (westInner != null) {
            westInner.setDoor(DungeonRoom.DOOR_EAST, DungeonRoom.DoorState.OPEN);
        }

        DungeonRoom eastInner = getRoom(4, 1);
        if (eastInner != null) {
            eastInner.setDoor(DungeonRoom.DOOR_WEST, DungeonRoom.DoorState.OPEN);
        }

        DungeonRoom bossRoom = getRoom(3, 2);
        if (bossRoom != null) {
            bossRoom.setDoor(DungeonRoom.DOOR_SOUTH, DungeonRoom.DoorState.OPEN);
        }

        setCurrentRoom(ENTRANCE_ROOM_X, ENTRANCE_ROOM_Y);
    }

    private void addRoom(int localX, int localY, int mapCol, int mapRow, String enemyType) {
        DungeonRoom room = new DungeonRoom(localX, localY, mapCol, mapRow);
        room.initialize(renderer, collisionMap);
        rooms.put(localX + "," + localY, room);
    }

    public boolean hasRoom(int x, int y) {
        return rooms.containsKey(x + "," + y);
    }

    public DungeonRoom getRoom(int x, int y) {
        return rooms.get(x + "," + y);
    }

    public void setCurrentRoom(int x, int y) {
        currentRoomX = x;
        currentRoomY = y;
        currentRoom = getRoom(x, y);
        if (currentRoom != null) {
            String enemyType = (currentRoomY >= 2) ? "keese" : "stalfos";
            currentRoom.enter(enemyType);
        }
    }

    public boolean tryMoveRoom(int doorDirection, ZeldaPlayer player) {
        if (currentRoom == null) return false;
        if (!currentRoom.canPass(doorDirection)) {
            return currentRoom.tryUnlock(doorDirection, player);
        }

        int nx = currentRoomX, ny = currentRoomY;
        switch (doorDirection) {
            case DungeonRoom.DOOR_NORTH: ny++; break;
            case DungeonRoom.DOOR_SOUTH: ny--; break;
            case DungeonRoom.DOOR_WEST: nx--; break;
            case DungeonRoom.DOOR_EAST: nx++; break;
        }

        if (hasRoom(nx, ny)) {
            setCurrentRoom(nx, ny);

            switch (doorDirection) {
                case DungeonRoom.DOOR_NORTH: player.setPosition(player.getWorldX(), ZeldaRoom.ROOM_PIXEL_H - 32); break;
                case DungeonRoom.DOOR_SOUTH: player.setPosition(player.getWorldX(), 16); break;
                case DungeonRoom.DOOR_WEST: player.setPosition(ZeldaRoom.ROOM_PIXEL_W - 32, player.getWorldY()); break;
                case DungeonRoom.DOOR_EAST: player.setPosition(16, player.getWorldY()); break;
            }
            return true;
        }
        return false;
    }

    public DungeonRoom getCurrentRoom() { return currentRoom; }
    public int getCurrentRoomX() { return currentRoomX; }
    public int getCurrentRoomY() { return currentRoomY; }
    public String getDungeonName() { return dungeonName; }
    public int getDungeonNumber() { return dungeonNumber; }
    public int getMapWidth() { return mapWidth; }
    public int getMapHeight() { return mapHeight; }

    public boolean hasMap() { return hasMap; }
    public boolean hasCompass() { return hasCompass; }
    public boolean hasBossKey() { return hasBossKey; }
    public boolean isBossDefeated() { return bossDefeated; }
    public boolean isTriforceCollected() { return triforceCollected; }

    public void setHasMap(boolean has) { hasMap = has; }
    public void setHasCompass(boolean has) { hasCompass = has; }
    public void setHasBossKey(boolean has) { hasBossKey = has; }
    public void setBossDefeated(boolean defeated) { bossDefeated = defeated; }
    public void setTriforceCollected(boolean collected) { triforceCollected = collected; }
}
