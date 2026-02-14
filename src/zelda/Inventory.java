package zelda;

import java.util.Properties;

/**
 * Tracks all collectible items and game progression state for a single save file.
 * Mirrors the NES Legend of Zelda inventory system.
 */
public class Inventory {

    // --- Sword: 0=none, 1=Wooden, 2=White, 3=Magical ---
    private int swordLevel = 0;
    private boolean swordDisabled = false;
    private int swordDisableTimer = 0;

    // --- Shield: 0=none, 1=Small (Wooden), 2=Magical ---
    private int shieldLevel = 1;

    // --- Boomerang: 0=none, 1=Wooden, 2=Magical ---
    private int boomerangLevel = 0;

    // --- Ring: 0=none (green tunic), 1=Blue, 2=Red ---
    private int ringLevel = 0;

    // --- Bow & Arrows ---
    private boolean hasBow = false;
    // arrowLevel: 0=none, 1=Wooden Arrow, 2=Silver Arrow
    private int arrowLevel = 0;

    // --- Candle: 0=none, 1=Blue, 2=Red ---
    private int candleLevel = 0;

    // --- Single-flag items ---
    private boolean hasRecorder = false;
    private boolean hasFood = false;
    private boolean hasRaft = false;
    private boolean hasLadder = false;
    private boolean hasBracelet = false;
    private boolean hasMagicKey = false;
    private boolean hasRod = false;
    private boolean hasBook = false;

    // --- Letter / Potion chain ---
    // letterState: 0=none, 1=has letter (not delivered), 2=letter delivered (potion shop open)
    private int letterState = 0;
    // potionCount: 0=none, 1=Life Potion (1 use), 2=2nd Potion (2 uses)
    private int potionCount = 0;

    // --- Consumables ---
    private int rupees = 0;
    private int keys = 0;
    private int bombs = 0;
    private int maxBombs = 8;

    // --- Health ---
    private int health = 6;       // half-hearts (6 = 3 full hearts)
    private int maxHealth = 6;
    private int heartContainers = 3;

    // --- Per-dungeon items (indices 0-8 for dungeons 1-9) ---
    private boolean[] hasMap = new boolean[9];
    private boolean[] hasCompass = new boolean[9];
    private boolean[] hasBossKey = new boolean[9];
    private boolean[] triforce = new boolean[9]; // 0-7 = shards, 8 = Triforce of Power

    // --- Currently selected B-item ---
    private BItem selectedBItem = BItem.NONE;

    // --- Overworld / dungeon progress ---
    // Tracks which rooms have been cleared (enemies killed) — key = "rX_rY"
    private java.util.Set<String> clearedRooms = new java.util.HashSet<>();
    // Tracks which caves/secrets have been visited — key = "rX_rY_caveId"
    private java.util.Set<String> visitedCaves = new java.util.HashSet<>();
    // Tracks which dungeon rooms have been cleared per dungeon — key = "dLevel_rX_rY"
    private java.util.Set<String> clearedDungeonRooms = new java.util.HashSet<>();

    // --- Freeze timer (from CLOCK item) ---
    private int freezeTimer = 0;

    // --- Second Quest flag ---
    private boolean secondQuest = false;

    /**
     * Enum for B-button assignable items.
     */
    public enum BItem {
        NONE,
        BOOMERANG,
        BOMB,
        BOW,
        CANDLE,
        RECORDER,
        FOOD,
        POTION,
        ROD
    }

    public Inventory() {}

    // ========== Sword ==========
    public int getSwordLevel() { return swordLevel; }
    public void setSwordLevel(int level) { swordLevel = Math.max(0, Math.min(3, level)); }
    public boolean hasSword() { return swordLevel > 0 && !swordDisabled; }
    public void disableSword() { swordDisabled = true; swordDisableTimer = 256; }
    public void enableSword() { swordDisabled = false; swordDisableTimer = 0; }
    public void tickSwordDisable() { if (swordDisableTimer > 0) { swordDisableTimer--; if (swordDisableTimer <= 0) swordDisabled = false; } }
    public int getSwordDamage() {
        switch (swordLevel) {
            case 1: return 1;
            case 2: return 2;
            case 3: return 4;
            default: return 0;
        }
    }

    // ========== Shield ==========
    public int getShieldLevel() { return shieldLevel; }
    public void setShieldLevel(int level) { shieldLevel = Math.max(0, Math.min(2, level)); }
    public boolean hasMagicalShield() { return shieldLevel >= 2; }
    public void loseShield() { if (shieldLevel > 1) shieldLevel = 1; }

    // ========== Boomerang ==========
    public int getBoomerangLevel() { return boomerangLevel; }
    public void setBoomerangLevel(int level) { boomerangLevel = Math.max(0, Math.min(2, level)); }
    public boolean hasBoomerang() { return boomerangLevel > 0; }
    public boolean hasMagicalBoomerang() { return boomerangLevel >= 2; }

    // ========== Ring ==========
    public int getRingLevel() { return ringLevel; }
    public void setRingLevel(int level) { ringLevel = Math.max(0, Math.min(2, level)); }
    public double getDamageMultiplier() {
        switch (ringLevel) {
            case 1: return 0.5;
            case 2: return 0.25;
            default: return 1.0;
        }
    }
    public String getLinkColorVariant() {
        switch (ringLevel) {
            case 1: return "Blue";
            case 2: return "Red";
            default: return "Normal";
        }
    }

    // ========== Bow & Arrows ==========
    public boolean hasBow() { return hasBow; }
    public void setHasBow(boolean b) { hasBow = b; }
    public int getArrowLevel() { return arrowLevel; }
    public void setArrowLevel(int level) { arrowLevel = Math.max(0, Math.min(2, level)); }
    public boolean hasArrows() { return arrowLevel > 0; }
    public boolean hasSilverArrows() { return arrowLevel >= 2; }
    public boolean canShootArrow() { return hasBow && arrowLevel > 0 && rupees > 0; }

    // ========== Candle ==========
    public int getCandleLevel() { return candleLevel; }
    public void setCandleLevel(int level) { candleLevel = Math.max(0, Math.min(2, level)); }
    public boolean hasCandle() { return candleLevel > 0; }
    public boolean hasRedCandle() { return candleLevel >= 2; }

    // ========== Single-flag items ==========
    public boolean hasRecorder() { return hasRecorder; }
    public void setHasRecorder(boolean b) { hasRecorder = b; }
    public boolean hasFood() { return hasFood; }
    public void setHasFood(boolean b) { hasFood = b; }
    public boolean hasRaft() { return hasRaft; }
    public void setHasRaft(boolean b) { hasRaft = b; }
    public boolean hasLadder() { return hasLadder; }
    public void setHasLadder(boolean b) { hasLadder = b; }
    public boolean hasBracelet() { return hasBracelet; }
    public void setHasBracelet(boolean b) { hasBracelet = b; }
    public boolean hasMagicKey() { return hasMagicKey; }
    public void setHasMagicKey(boolean b) { hasMagicKey = b; }
    public boolean hasRod() { return hasRod; }
    public void setHasRod(boolean b) { hasRod = b; }
    public boolean hasBook() { return hasBook; }
    public void setHasBook(boolean b) { hasBook = b; }

    // ========== Letter / Potion ==========
    public int getLetterState() { return letterState; }
    public void setLetterState(int s) { letterState = Math.max(0, Math.min(2, s)); }
    public boolean hasPotionShopAccess() { return letterState >= 2; }
    public int getPotionCount() { return potionCount; }
    public void setPotionCount(int c) { potionCount = Math.max(0, Math.min(2, c)); }
    public boolean usePotion() {
        if (potionCount > 0) {
            health = maxHealth;
            potionCount--;
            return true;
        }
        return false;
    }

    // ========== Consumables ==========
    public int getRupees() { return rupees; }
    public void setRupees(int r) { rupees = Math.max(0, Math.min(255, r)); }
    public void addRupees(int a) { setRupees(rupees + a); }
    public boolean spendRupees(int cost) {
        if (rupees >= cost) { rupees -= cost; return true; }
        return false;
    }

    public int getKeys() { return keys; }
    public void setKeys(int k) { keys = Math.max(0, Math.min(255, k)); }
    public void addKeys(int k) { setKeys(keys + k); }
    public boolean useKey() {
        if (hasMagicKey) return true;
        if (keys > 0) { keys--; return true; }
        return false;
    }

    public int getBombs() { return bombs; }
    public void setBombs(int b) { bombs = Math.max(0, Math.min(maxBombs, b)); }
    public void addBombs(int b) { setBombs(bombs + b); }
    public int getMaxBombs() { return maxBombs; }
    public void setMaxBombs(int m) { maxBombs = m; }
    public boolean useBomb() {
        if (bombs > 0) { bombs--; return true; }
        return false;
    }

    // ========== Health ==========
    public int getHealth() { return health; }
    public void setHealth(int h) { health = Math.max(0, Math.min(maxHealth, h)); }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int m) { maxHealth = m; }
    public int getHeartContainers() { return heartContainers; }
    public void addHeartContainer() {
        if (heartContainers < 16) {
            heartContainers++;
            maxHealth = heartContainers * 2;
            health = maxHealth;
        }
    }
    public void heal(int halfHearts) { setHealth(health + halfHearts); }
    public boolean isFullHealth() { return health >= maxHealth; }
    public boolean isAlive() { return health > 0; }
    public void takeDamage(int halfHearts) {
        int actual = (int) Math.ceil(halfHearts * getDamageMultiplier());
        if (actual < 1) actual = 1;
        setHealth(health - actual);
    }

    // ========== Per-dungeon items ==========
    public boolean hasMap(int dungeon) { return dungeon >= 1 && dungeon <= 9 && hasMap[dungeon - 1]; }
    public void setHasMap(int dungeon, boolean b) { if (dungeon >= 1 && dungeon <= 9) hasMap[dungeon - 1] = b; }
    public boolean hasCompass(int dungeon) { return dungeon >= 1 && dungeon <= 9 && hasCompass[dungeon - 1]; }
    public void setHasCompass(int dungeon, boolean b) { if (dungeon >= 1 && dungeon <= 9) hasCompass[dungeon - 1] = b; }
    public boolean hasBossKey(int dungeon) { return dungeon >= 1 && dungeon <= 9 && hasBossKey[dungeon - 1]; }
    public void setHasBossKey(int dungeon, boolean b) { if (dungeon >= 1 && dungeon <= 9) hasBossKey[dungeon - 1] = b; }
    public boolean hasTriforce(int dungeon) { return dungeon >= 1 && dungeon <= 9 && triforce[dungeon - 1]; }
    public void setTriforce(int dungeon, boolean b) { if (dungeon >= 1 && dungeon <= 9) triforce[dungeon - 1] = b; }
    public int getTriforceCount() {
        int count = 0;
        for (int i = 0; i < 8; i++) if (triforce[i]) count++;
        return count;
    }
    public boolean hasAllTriforce() { return getTriforceCount() >= 8; }

    // ========== B-item selection ==========
    public BItem getSelectedBItem() { return selectedBItem; }
    public void setSelectedBItem(BItem item) { selectedBItem = item; }

    // ========== Progress tracking ==========
    public void markRoomCleared(int roomX, int roomY) { clearedRooms.add(roomX + "_" + roomY); }
    public boolean isRoomCleared(int roomX, int roomY) { return clearedRooms.contains(roomX + "_" + roomY); }
    public void markCaveVisited(int roomX, int roomY, int caveId) { visitedCaves.add(roomX + "_" + roomY + "_" + caveId); }
    public boolean isCaveVisited(int roomX, int roomY, int caveId) { return visitedCaves.contains(roomX + "_" + roomY + "_" + caveId); }
    public void markDungeonRoomCleared(int level, int roomX, int roomY) { clearedDungeonRooms.add(level + "_" + roomX + "_" + roomY); }
    public boolean isDungeonRoomCleared(int level, int roomX, int roomY) { return clearedDungeonRooms.contains(level + "_" + roomX + "_" + roomY); }

    // ========== Freeze Timer (CLOCK item) ==========
    public int getFreezeTimer() { return freezeTimer; }
    public void setFreezeTimer(int t) { freezeTimer = t; }
    public boolean isEnemiesFrozen() { return freezeTimer > 0; }
    public void tickFreezeTimer() { if (freezeTimer > 0) freezeTimer--; }

    // ========== Second Quest ==========
    public boolean isSecondQuest() { return secondQuest; }
    public void setSecondQuest(boolean b) { secondQuest = b; }

    // ========== Save / Load via Properties ==========

    public void saveToProperties(Properties props) {
        props.setProperty("swordLevel", String.valueOf(swordLevel));
        props.setProperty("shieldLevel", String.valueOf(shieldLevel));
        props.setProperty("boomerangLevel", String.valueOf(boomerangLevel));
        props.setProperty("ringLevel", String.valueOf(ringLevel));
        props.setProperty("hasBow", String.valueOf(hasBow));
        props.setProperty("arrowLevel", String.valueOf(arrowLevel));
        props.setProperty("candleLevel", String.valueOf(candleLevel));
        props.setProperty("hasRecorder", String.valueOf(hasRecorder));
        props.setProperty("hasFood", String.valueOf(hasFood));
        props.setProperty("hasRaft", String.valueOf(hasRaft));
        props.setProperty("hasLadder", String.valueOf(hasLadder));
        props.setProperty("hasBracelet", String.valueOf(hasBracelet));
        props.setProperty("hasMagicKey", String.valueOf(hasMagicKey));
        props.setProperty("hasRod", String.valueOf(hasRod));
        props.setProperty("hasBook", String.valueOf(hasBook));
        props.setProperty("letterState", String.valueOf(letterState));
        props.setProperty("potionCount", String.valueOf(potionCount));
        props.setProperty("rupees", String.valueOf(rupees));
        props.setProperty("keys", String.valueOf(keys));
        props.setProperty("bombs", String.valueOf(bombs));
        props.setProperty("maxBombs", String.valueOf(maxBombs));
        props.setProperty("health", String.valueOf(health));
        props.setProperty("maxHealth", String.valueOf(maxHealth));
        props.setProperty("heartContainers", String.valueOf(heartContainers));
        props.setProperty("selectedBItem", selectedBItem.name());
        props.setProperty("secondQuest", String.valueOf(secondQuest));

        // Per-dungeon flags
        for (int i = 0; i < 9; i++) {
            props.setProperty("hasMap_" + (i + 1), String.valueOf(hasMap[i]));
            props.setProperty("hasCompass_" + (i + 1), String.valueOf(hasCompass[i]));
            props.setProperty("hasBossKey_" + (i + 1), String.valueOf(hasBossKey[i]));
            props.setProperty("triforce_" + (i + 1), String.valueOf(triforce[i]));
        }

        // Cleared rooms (serialize as comma-separated)
        props.setProperty("clearedRooms", String.join(",", clearedRooms));
        props.setProperty("visitedCaves", String.join(",", visitedCaves));
        props.setProperty("clearedDungeonRooms", String.join(",", clearedDungeonRooms));
    }

    public void loadFromProperties(Properties props) {
        swordLevel = Integer.parseInt(props.getProperty("swordLevel", "0"));
        shieldLevel = Integer.parseInt(props.getProperty("shieldLevel", "1"));
        boomerangLevel = Integer.parseInt(props.getProperty("boomerangLevel", "0"));
        ringLevel = Integer.parseInt(props.getProperty("ringLevel", "0"));
        hasBow = Boolean.parseBoolean(props.getProperty("hasBow", "false"));
        arrowLevel = Integer.parseInt(props.getProperty("arrowLevel", "0"));
        candleLevel = Integer.parseInt(props.getProperty("candleLevel", "0"));
        hasRecorder = Boolean.parseBoolean(props.getProperty("hasRecorder", "false"));
        hasFood = Boolean.parseBoolean(props.getProperty("hasFood", "false"));
        hasRaft = Boolean.parseBoolean(props.getProperty("hasRaft", "false"));
        hasLadder = Boolean.parseBoolean(props.getProperty("hasLadder", "false"));
        hasBracelet = Boolean.parseBoolean(props.getProperty("hasBracelet", "false"));
        hasMagicKey = Boolean.parseBoolean(props.getProperty("hasMagicKey", "false"));
        hasRod = Boolean.parseBoolean(props.getProperty("hasRod", "false"));
        hasBook = Boolean.parseBoolean(props.getProperty("hasBook", "false"));
        letterState = Integer.parseInt(props.getProperty("letterState", "0"));
        potionCount = Integer.parseInt(props.getProperty("potionCount", "0"));
        rupees = Integer.parseInt(props.getProperty("rupees", "0"));
        keys = Integer.parseInt(props.getProperty("keys", "0"));
        bombs = Integer.parseInt(props.getProperty("bombs", "0"));
        maxBombs = Integer.parseInt(props.getProperty("maxBombs", "8"));
        health = Integer.parseInt(props.getProperty("health", "6"));
        maxHealth = Integer.parseInt(props.getProperty("maxHealth", "6"));
        heartContainers = Integer.parseInt(props.getProperty("heartContainers", "3"));
        secondQuest = Boolean.parseBoolean(props.getProperty("secondQuest", "false"));

        try {
            selectedBItem = BItem.valueOf(props.getProperty("selectedBItem", "NONE"));
        } catch (IllegalArgumentException e) {
            selectedBItem = BItem.NONE;
        }

        for (int i = 0; i < 9; i++) {
            hasMap[i] = Boolean.parseBoolean(props.getProperty("hasMap_" + (i + 1), "false"));
            hasCompass[i] = Boolean.parseBoolean(props.getProperty("hasCompass_" + (i + 1), "false"));
            hasBossKey[i] = Boolean.parseBoolean(props.getProperty("hasBossKey_" + (i + 1), "false"));
            triforce[i] = Boolean.parseBoolean(props.getProperty("triforce_" + (i + 1), "false"));
        }

        clearedRooms.clear();
        String cr = props.getProperty("clearedRooms", "");
        if (!cr.isEmpty()) for (String s : cr.split(",")) clearedRooms.add(s);

        visitedCaves.clear();
        String vc = props.getProperty("visitedCaves", "");
        if (!vc.isEmpty()) for (String s : vc.split(",")) visitedCaves.add(s);

        clearedDungeonRooms.clear();
        String cdr = props.getProperty("clearedDungeonRooms", "");
        if (!cdr.isEmpty()) for (String s : cdr.split(",")) clearedDungeonRooms.add(s);
    }

    /**
     * Creates a fresh inventory for a new game.
     */
    public static Inventory createNew() {
        return new Inventory();
    }
}
