package zelda;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ZeldaRoom {
    private int roomX, roomY;
    private List<ZeldaEnemy> enemies = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    
    private boolean cleared = false;
    private boolean visited = false;
    
    private static OverworldRenderer overworldRenderer;
    private static CollisionMap collisionMap;
    
    public static final int TILE_SIZE = 16;
    public static final int TILES_X = 16;
    public static final int TILES_Y = 11;

    public ZeldaRoom(int roomX, int roomY) {
        this.roomX = roomX;
        this.roomY = roomY;
        
        if (overworldRenderer == null) {
            overworldRenderer = new OverworldRenderer();
        }
        if (collisionMap == null) {
            collisionMap = new CollisionMap();
            collisionMap.setRenderer(overworldRenderer);
        }
    }
    
    public void spawnEnemies() {
        if (!enemies.isEmpty() || cleared) return;
        if (roomX == 7 && roomY == 7) return;
        
        java.util.Random rand = new java.util.Random(roomX * 100 + roomY);
        int numEnemies = 1 + rand.nextInt(3);
        
        for (int i = 0; i < numEnemies; i++) {
            Point spawn = findWalkableSpawn(rand);
            if (spawn == null) continue;
            
            ZeldaEnemy enemy;
            double type = rand.nextDouble();
            
            if (type < 0.5) {
                enemy = new zelda.enemies.Octorok(spawn.x, spawn.y, rand.nextBoolean());
            } else if (type < 0.75) {
                enemy = new zelda.enemies.Moblin(spawn.x, spawn.y, rand.nextBoolean());
            } else {
                enemy = new zelda.enemies.Tektite(spawn.x, spawn.y, rand.nextBoolean());
            }
            enemies.add(enemy);
        }
    }
    
    private Point findWalkableSpawn(java.util.Random rand) {
        for (int attempt = 0; attempt < 20; attempt++) {
            int tx = 2 + rand.nextInt(12);
            int ty = 2 + rand.nextInt(7);
            
            if (collisionMap.getTileType(roomX, roomY, tx, ty).walkable) {
                return new Point(tx * TILE_SIZE + 4, ty * TILE_SIZE + 4);
            }
        }
        return new Point(128, 88);
    }
    
    public void update(ZeldaPlayer player, CombatManager combat, AudioManager audio) {
        if (!visited) {
            visited = true;
            spawnEnemies();
        }
        
        Iterator<ZeldaEnemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            ZeldaEnemy enemy = enemyIter.next();
            
            if (!enemy.isActive()) {
                enemyIter.remove();
                if (Math.random() < 0.35) {
                    spawnDrop(enemy.getX(), enemy.getY());
                    if (audio != null) audio.playSFX("04. Small Item Get.wav");
                }
                continue;
            }
            
            enemy.update(player, this, projectiles);
            
            if (enemy.getHitbox().intersects(player.getHitbox()) && enemy.canDamage()) {
                player.damage(enemy.getDamage());
            }
            
            if (player.isAttacking() && player.getSwordHitbox().intersects(enemy.getHitbox())) {
                enemy.damage(1);
            }
        }
        
        if (enemies.isEmpty() && !cleared) cleared = true;
        
        Iterator<Item> itemIter = items.iterator();
        while (itemIter.hasNext()) {
            Item item = itemIter.next();
            if (!item.isActive()) { itemIter.remove(); continue; }
            
            item.update();
            if (item.getHitbox().intersects(player.getHitbox())) {
                item.applyToPlayer(player);
                if (audio != null) audio.playSFX("04. Small Item Get.wav");
            }
        }
        
        Iterator<Projectile> projIter = projectiles.iterator();
        while (projIter.hasNext()) {
            Projectile proj = projIter.next();
            if (!proj.isActive()) { projIter.remove(); continue; }
            
            proj.update();
            
            if (!proj.isPlayerOwned() && proj.getHitbox().intersects(player.getHitbox())) {
                player.damage(1);
                proj.deactivate();
            }
            
            if (proj.isPlayerOwned()) {
                for (ZeldaEnemy enemy : enemies) {
                    if (proj.getHitbox().intersects(enemy.getHitbox())) {
                        enemy.damage(1);
                        proj.deactivate();
                        break;
                    }
                }
            }
        }
        
        checkPlayerCollision(player);
    }
    
    private void spawnDrop(double x, double y) {
        double r = Math.random();
        Item.ItemType type = r < 0.5 ? Item.ItemType.HEART : 
                            r < 0.75 ? Item.ItemType.RUPEE_GREEN : Item.ItemType.RUPEE_BLUE;
        items.add(new Item(x, y, type));
    }
    
    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= 256 || y < 0 || y >= 176) {
            return true;
        }
        return collisionMap.isWalkable(roomX, roomY, x, y);
    }
    
    public void checkPlayerCollision(ZeldaPlayer player) {
        Rectangle box = player.getHitbox();
        
        if (box.x < 4 || box.x > 240 || box.y < 4 || box.y > 156) {
            return;
        }
        
        boolean blocked = !isWalkable(box.x + 2, box.y + 2) ||
                         !isWalkable(box.x + box.width - 2, box.y + 2) ||
                         !isWalkable(box.x + 2, box.y + box.height - 2) ||
                         !isWalkable(box.x + box.width - 2, box.y + box.height - 2);
        
        if (blocked) {
            player.rollbackPosition();
        }
    }
    
    public void render(Graphics2D g2) {
        overworldRenderer.renderRoom(g2, roomX, roomY);
        
        for (Item item : items) item.render(g2);
        for (ZeldaEnemy enemy : enemies) enemy.render(g2);
        for (Projectile proj : projectiles) proj.render(g2);
    }
    
    public void addProjectile(Projectile proj) { projectiles.add(proj); }
    public int getRoomX() { return roomX; }
    public int getRoomY() { return roomY; }
    public boolean isCleared() { return cleared; }
    public List<ZeldaEnemy> getEnemies() { return enemies; }
}
