package zelda;

import java.awt.*;

public class CombatManager {
    public CombatManager() {}
    
    public void update(ZeldaPlayer player, ZeldaRoom room) {
        if (room == null) return;
        room.checkPlayerCollision(player);
    }
    
    public void render(Graphics2D g2) {}
}
