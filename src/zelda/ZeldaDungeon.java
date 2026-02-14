package zelda;

import java.util.HashMap;

public class ZeldaDungeon {
    public static final double ENTRANCE_SPAWN_X = 120;
    public static final double ENTRANCE_SPAWN_Y = 144;

    private String dungeonName;
    private int dungeonNumber;
    private int entranceRoomX;
    private int entranceRoomY;

    private HashMap<String, DungeonRoom> rooms = new HashMap<>();
    private HashMap<String, DungeonData.DungeonRoomDef> roomDefs = new HashMap<>();
    private DungeonRoom currentRoom;
    private int currentRoomX = 0;
    private int currentRoomY = 0;

    private boolean bossDefeated = false;
    private boolean triforceCollected = false;

    private int mapWidth = 0;
    private int mapHeight = 0;
    private int triforceRoomX = -1;
    private int triforceRoomY = -1;

    private DungeonRenderer renderer;
    private CollisionMap collisionMap;

    public ZeldaDungeon(int number, String name) {
        this.dungeonNumber = number;
        this.dungeonName = name;
    }

    public void initialize(DungeonRenderer r, CollisionMap c) {
        this.renderer = r;
        this.collisionMap = c;
        buildFromData();
    }

    private void buildFromData() {
        DungeonData.DungeonDef def = DungeonData.getDungeon(dungeonNumber);
        this.dungeonName = def.name;
        this.entranceRoomX = def.entranceX;
        this.entranceRoomY = def.entranceY;
        this.mapWidth = def.mapWidth;
        this.mapHeight = def.mapHeight;

        for (DungeonData.DungeonRoomDef rd : def.rooms) {
            DungeonRoom room = new DungeonRoom(rd.localX, rd.localY, rd.mapCol, rd.mapRow);
            room.initialize(renderer, collisionMap);

            // Set doors from data
            DungeonRoom.DoorState[] doorStates = {
                DungeonRoom.DoorState.NONE, DungeonRoom.DoorState.OPEN,
                DungeonRoom.DoorState.LOCKED, DungeonRoom.DoorState.BOSS_LOCKED,
                DungeonRoom.DoorState.BOMBABLE, DungeonRoom.DoorState.BOMBED
            };
            for (int d = 0; d < 4; d++) {
                int doorVal = rd.doors[d];
                if (doorVal >= 0 && doorVal < doorStates.length) {
                    room.setDoor(d, doorStates[doorVal]);
                }
            }

            // Set room item
            if (rd.itemType != null) {
                Item.ItemType itemType = mapItemType(rd.itemType);
                if (itemType != null) {
                    room.setRoomItem(itemType);
                }
            }

            // Set enemy types, boss, and dungeon number
            room.setEnemyTypes(rd.enemies);
            room.setBossType(rd.bossType);
            room.setDark(rd.isDark);
            room.setHasBlock(rd.hasBlock);
            room.setDungeonNumber(dungeonNumber);

            if ("TRIFORCE".equals(rd.itemType)) {
                triforceRoomX = rd.localX;
                triforceRoomY = rd.localY;
            }

            String key = rd.localX + "," + rd.localY;
            rooms.put(key, room);
            roomDefs.put(key, rd);
        }

        setCurrentRoom(entranceRoomX, entranceRoomY);
    }

    private Item.ItemType mapItemType(String name) {
        switch (name) {
            case "KEY": return Item.ItemType.KEY;
            case "MAP": return Item.ItemType.MAP;
            case "COMPASS": return Item.ItemType.COMPASS;
            case "TRIFORCE": return Item.ItemType.TRIFORCE;
            case "BOSS_KEY": return Item.ItemType.BOSS_KEY;
            case "BOMB": return Item.ItemType.BOMB;
            case "HEART_CONTAINER": return Item.ItemType.HEART_CONTAINER;
            case "BOW": return Item.ItemType.BOW;
            case "BOOMERANG": return Item.ItemType.BOOMERANG;
            case "MAGICAL_BOOMERANG": return Item.ItemType.MAGICAL_BOOMERANG;
            case "RAFT": return Item.ItemType.RAFT;
            case "STEPLADDER": return Item.ItemType.STEPLADDER;
            case "RECORDER": return Item.ItemType.RECORDER;
            case "MAGICAL_ROD": return Item.ItemType.MAGICAL_ROD;
            case "RED_CANDLE": return Item.ItemType.RED_CANDLE;
            case "MAGICAL_KEY": return Item.ItemType.MAGICAL_KEY;
            case "SILVER_ARROW": return Item.ItemType.SILVER_ARROW;
            case "ZELDA": return Item.ItemType.ZELDA;
            default: return null;
        }
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
            currentRoom.enter();
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
            player.onScreenChange();

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
    public int getEntranceRoomX() { return entranceRoomX; }
    public int getEntranceRoomY() { return entranceRoomY; }
    public String getDungeonName() { return dungeonName; }
    public int getDungeonNumber() { return dungeonNumber; }
    public int getMapWidth() { return mapWidth; }
    public int getMapHeight() { return mapHeight; }

    public void setItemDropSystem(ItemDropSystem ids) {
        for (DungeonRoom room : rooms.values()) {
            room.setItemDropSystem(ids);
        }
    }

    public boolean isBossDefeated() { return bossDefeated; }
    public boolean isTriforceCollected() { return triforceCollected; }
    public void setBossDefeated(boolean defeated) { bossDefeated = defeated; }
    public void setTriforceCollected(boolean collected) { triforceCollected = collected; }
    public int getTriforceRoomX() { return triforceRoomX; }
    public int getTriforceRoomY() { return triforceRoomY; }
}
