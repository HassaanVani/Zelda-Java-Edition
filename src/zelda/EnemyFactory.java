package zelda;

import zelda.enemies.*;

/**
 * Creates enemy instances from string type names.
 * Used by RoomData to spawn enemies from data definitions.
 */
public class EnemyFactory {

    public static ZeldaEnemy create(String type, double x, double y) {
        switch (type) {
            // Octoroks
            case "OctorokRed":   return new Octorok(x, y, true);
            case "OctorokBlue":  return new Octorok(x, y, false);

            // Moblins
            case "MoblinRed":    return new Moblin(x, y, true);
            case "MoblinBlue":   return new Moblin(x, y, false);
            case "MoblinBlack":  return new Moblin(x, y, false);

            // Stalfos
            case "Stalfos":      return new Stalfos(x, y);

            // Keese
            case "KeeseBlue":    return new Keese(x, y, true);
            case "KeeseRed":     return new Keese(x, y, false);

            // Tektites
            case "TektiteBlue":  return new Tektite(x, y, true);
            case "TektiteRed":   return new Tektite(x, y, false);

            // Leevers
            case "LeeverBlue":   return new Leever(x, y, true);
            case "LeeverRed":    return new Leever(x, y, false);

            // Peahat
            case "Peahat":       return new Peahat(x, y);

            // Lynels
            case "LynelRed":     return new Lynel(x, y, false);
            case "LynelBlue":    return new Lynel(x, y, true);

            // Ghini
            case "Ghini":        return new Ghini(x, y);

            // Zola/Zora
            case "Zola":         return new Zola(x, y);

            // Armos
            case "Armos":        return new Armos(x, y);

            // Darknut
            case "DarknutRed":   return new Darknut(x, y, false);
            case "DarknutBlue":  return new Darknut(x, y, true);

            // Goriya
            case "GoriyaRed":    return new Goriya(x, y, false);
            case "GoriyaBlue":   return new Goriya(x, y, true);
            case "Goriya":       return new Goriya(x, y, false);

            // Wizzrobe
            case "WizzrobeRed":  return new Wizzrobe(x, y, false);
            case "WizzrobeBlue": return new Wizzrobe(x, y, true);

            // Gibdo
            case "Gibdo":        return new Gibdo(x, y);

            // Like Like
            case "LikeLike":     return new LikeLike(x, y);

            // Pols Voice
            case "PolsVoice":    return new PolsVoice(x, y);

            // Vire
            case "Vire":         return new Vire(x, y);

            // Rope
            case "Rope":         return new Rope(x, y);

            // Wallmaster
            case "Wallmaster":   return new Wallmaster(x, y);

            // Gel/Zol (small enemies, use Keese as base behavior)
            case "GelBlack":     return new Keese(x, y, false);
            case "GelGreen":     return new Keese(x, y, false);
            case "ZolGreen":     return new Keese(x, y, false);
            case "ZolGrey":      return new Keese(x, y, false);

            // Bubble (invulnerable, use Keese as base behavior)
            case "Bubble":       return new Keese(x, y, false);
            case "BubbleBlue":   return new Keese(x, y, true);
            case "BubbleRed":    return new Keese(x, y, false);

            default:
                System.err.println("[EnemyFactory] Unknown enemy type: " + type);
                return new Octorok(x, y, false);
        }
    }
}
