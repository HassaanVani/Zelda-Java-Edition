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
    
    public static final int ROOM_WIDTH = 16;
    public static final int ROOM_HEIGHT = 11;
    public static final int TILE_SIZE = 16;

    public ZeldaRoom(int roomX, int roomY) {
        this.roomX = roomX;
        this.roomY = roomY;
        
        if (overworldRenderer == null) {
            overworldRenderer = new OverworldRenderer();
        }
    }
    
    public void spawnEnemies() {
        if (enemies.isEmpty() && !cleared) {
            java.util.Random rand = new java.util.Random(roomX * 100 + roomY + 999);
            
            if (roomX == 7 && roomY == 7) return;
            
            int numEnemies = 1 + rand.nextInt(4);
            for (int i = 0; i < numEnemies; i++) {
                Point spawnPoint = findWalkableSpawn(rand);
                if (spawnPoint == null) continue;
                
                int ex = spawnPoint.x;
                int ey = spawnPoint.y;
                
                ZeldaEnemy enemy;
                double type = rand.nextDouble();
                
                if (roomY <= 2) {
                    if (type < 0.5) {
                        enemy = new zelda.enemies.Octorok(ex, ey, rand.nextBoolean());
                    } else if (type < 0.8) {
                        enemy = new zelda.enemies.Tektite(ex, ey, rand.nextBoolean());
                    } else {
                        enemy = new zelda.enemies.Peahat(ex, ey);
                    }
                } else if (roomY <= 4) {
                    if (type < 0.35) {
                        enemy = new zelda.enemies.Octorok(ex, ey, rand.nextBoolean());
                    } else if (type < 0.65) {
                        enemy = new zelda.enemies.Moblin(ex, ey, rand.nextBoolean());
                    } else if (type < 0.9) {
                        enemy = new zelda.enemies.Tektite(ex, ey, rand.nextBoolean());
                    } else {
                        enemy = new zelda.enemies.Leever(ex, ey, rand.nextBoolean());
                    }
                } else {
                    if (type < 0.3) {
                        enemy = new zelda.enemies.Octorok(ex, ey, true);
                    } else if (type < 0.5) {
                        enemy = new zelda.enemies.Moblin(ex, ey, rand.nextBoolean());
                    } else if (type < 0.7) {
                        enemy = new zelda.enemies.Stalfos(ex, ey);
                    } else if (type < 0.85) {
                        enemy = new zelda.enemies.Keese(ex, ey, rand.nextBoolean());
                    } else {
                        enemy = new zelda.enemies.Leever(ex, ey, true);
                    }
                }
                enemies.add(enemy);
            }
        }
    }
    
    private Point findWalkableSpawn(java.util.Random rand) {
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = 32 + rand.nextInt(192);
            int y = 32 + rand.nextInt(96);
            
            if (isWalkable(x, y) && isWalkable(x + 12, y) && 
                isWalkable(x, y + 12) && isWalkable(x + 12, y + 12)) {
                return new Point(x, y);
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
                if (audio != null) audio.playSFX("07. Collect Item.wav");
            }
        }
        
        if (enemies.isEmpty() && !cleared) {
            cleared = true;
        }
        
        Iterator<Item> itemIter = items.iterator();
        while (itemIter.hasNext()) {
            Item item = itemIter.next();
            if (!item.isActive()) {
                itemIter.remove();
                continue;
            }
            
            item.update();
            
            if (item.getHitbox().intersects(player.getHitbox())) {
                item.applyToPlayer(player);
                if (audio != null) audio.playSFX("04. Small Item Get.wav");
            }
        }
        
        Iterator<Projectile> projIter = projectiles.iterator();
        while (projIter.hasNext()) {
            Projectile proj = projIter.next();
            if (!proj.isActive()) {
                projIter.remove();
                continue;
            }
            
            proj.update();
            
            if (!isWalkable((int)proj.getX(), (int)proj.getY())) {
                proj.deactivate();
            }
            
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
        double rand = Math.random();
        Item.ItemType type;
        
        if (rand < 0.45) {
            type = Item.ItemType.HEART;
        } else if (rand < 0.65) {
            type = Item.ItemType.RUPEE_GREEN;
        } else if (rand < 0.80) {
            type = Item.ItemType.RUPEE_BLUE;
        } else if (rand < 0.92) {
            type = Item.ItemType.KEY;
        } else {
            type = Item.ItemType.BOMB;
        }
        
        items.add(new Item(x, y, type));
    }
    
    public boolean isWalkable(int x, int y) {
        if (x < 8 || x > 248 || y < 8 || y > 168) {
            return false;
        }
        return overworldRenderer.isPixelWalkable(roomX, roomY, x, y);
    }
    
    public void checkPlayerCollision(ZeldaPlayer player) {
        Rectangle box = player.getHitbox();
        
        boolean blocked = !isWalkable(box.x, box.y) ||
                         !isWalkable(box.x + box.width, box.y) ||
                         !isWalkable(box.x, box.y + box.height) ||
                         !isWalkable(box.x + box.width, box.y + box.height);
        
        if (blocked) {
            player.rollbackPosition();
        }
    }
    
    public void render(Graphics2D g2) {
        overworldRenderer.renderRoom(g2, roomX, roomY);
        
        for (Item item : items) {
            item.render(g2);
        }
        
        for (ZeldaEnemy enemy : enemies) {
            enemy.render(g2);
        }
        
        for (Projectile proj : projectiles) {
            proj.render(g2);
        }
    }
    
    public void addProjectile(Projectile proj) {
        projectiles.add(proj);
    }
    
    public int getRoomX() { return roomX; }
    public int getRoomY() { return roomY; }
    public boolean isCleared() { return cleared; }
    public boolean isVisited() { return visited; }
    public List<ZeldaEnemy> getEnemies() { return enemies; }
    
    public boolean hasWaterAt(int x, int y) {
        return overworldRenderer.isWaterAt(roomX, roomY, x, y);
    }
}
