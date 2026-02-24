package zelda;

import java.awt.Color;

/**
 * NES-accurate palette system. Maps NES palette indices ($00-$3F) to RGB colors
 * and provides per-dungeon, per-boss, and overworld palette configurations.
 * Derived from the zelda1-disassembly-master palette transfer records.
 */
public class NESPalette {

    // Full NES 64-color palette (standard NTSC)
    private static final int[] NES_COLORS = {
        0x626262, 0x002492, 0x0C00AB, 0x3C00A4, // $00-$03
        0x5E0076, 0x6E0036, 0x6C0000, 0x5A1000, // $04-$07
        0x3C2800, 0x143A00, 0x004200, 0x003E00, // $08-$09, $0A, $0B
        0x003432, 0x000000, 0x000000, 0x000000, // $0C-$0F
        0xABABAB, 0x0D56DE, 0x3838FF, 0x7420E0, // $10-$13
        0xA31AB6, 0xBB1662, 0xBA2C14, 0x9E4A00, // $14-$17
        0x726A00, 0x368600, 0x069200, 0x008C38, // $18-$1B
        0x007E82, 0x000000, 0x000000, 0x000000, // $1C-$1F
        0xFFFFFF, 0x55ACFF, 0x7E8EFF, 0xB474FF, // $20-$23
        0xE46CFF, 0xFF6ABA, 0xFF7862, 0xEF9016, // $24-$27
        0xC4B200, 0x7ED200, 0x4EDE38, 0x34D880, // $28-$2B
        0x38CECE, 0x3C3C3C, 0x000000, 0x000000, // $2C-$2F
        0xFFFFFF, 0xB6E2FF, 0xCED6FF, 0xE2CCFF, // $30-$33
        0xF4C6FF, 0xFFC6E4, 0xFFCEB8, 0xF4D896, // $34-$37
        0xE2E686, 0xCEF29A, 0xB6F6B6, 0xAEF4D4, // $38-$39, $3A, $3B
        0xB6F2F0, 0x000000, 0x000000, 0x000000  // $3C-$3F
    };

    /** Convert NES palette index to Java Color */
    public static Color nesColor(int index) {
        if (index < 0 || index >= NES_COLORS.length) return Color.BLACK;
        if (index == 0x0F || index == 0x0D) return Color.BLACK;
        int rgb = NES_COLORS[index];
        return new Color(rgb);
    }

    // ===== DUNGEON PALETTES (from LevelInfo_PalettesTransferBuf) =====
    // Each dungeon has 4 BG palette rows and 4 sprite palette rows.
    // Format: [bg0, bg1, bg2, bg3] where each is {c1, c2, c3} (c0 is always $0F=black)

    /** Background palette for each dungeon (indices 1-9). Index 0 unused. */
    public static Color[][] getDungeonBGPalette(int level) {
        switch (level) {
            case 1: // Eagle - gray/brown
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x10) },  // walls
                    { nesColor(0x30), nesColor(0x10), nesColor(0x00) },  // floor
                    { nesColor(0x30), nesColor(0x00), nesColor(0x10) },  // doors
                    { nesColor(0x16), nesColor(0x27), nesColor(0x30) }   // accents
                };
            case 2: // Moon - blue
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x12) },
                    { nesColor(0x30), nesColor(0x12), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x12) },
                    { nesColor(0x16), nesColor(0x27), nesColor(0x30) }
                };
            case 3: // Manji - green
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x1A) },
                    { nesColor(0x30), nesColor(0x1A), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x1A) },
                    { nesColor(0x16), nesColor(0x27), nesColor(0x30) }
                };
            case 4: // Snake - dark blue/purple
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x02) },
                    { nesColor(0x30), nesColor(0x02), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x02) },
                    { nesColor(0x17), nesColor(0x27), nesColor(0x30) }
                };
            case 5: // Lizard - red
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x16) },
                    { nesColor(0x30), nesColor(0x16), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x16) },
                    { nesColor(0x15), nesColor(0x27), nesColor(0x30) }
                };
            case 6: // Dragon - orange/brown
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x17) },
                    { nesColor(0x30), nesColor(0x17), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x17) },
                    { nesColor(0x17), nesColor(0x27), nesColor(0x30) }
                };
            case 7: // Demon - dark red
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x06) },
                    { nesColor(0x30), nesColor(0x06), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x06) },
                    { nesColor(0x16), nesColor(0x27), nesColor(0x30) }
                };
            case 8: // Lion - dark blue
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x0C) },
                    { nesColor(0x30), nesColor(0x0C), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x0C) },
                    { nesColor(0x17), nesColor(0x27), nesColor(0x30) }
                };
            case 9: // Death Mountain - gray
                return new Color[][] {
                    { nesColor(0x30), nesColor(0x00), nesColor(0x10) },
                    { nesColor(0x30), nesColor(0x10), nesColor(0x00) },
                    { nesColor(0x30), nesColor(0x00), nesColor(0x10) },
                    { nesColor(0x16), nesColor(0x2C), nesColor(0x3C) }
                };
            default:
                return getDungeonBGPalette(1);
        }
    }

    /** Primary wall color for each dungeon (the dominant tint) */
    public static Color getDungeonWallColor(int level) {
        switch (level) {
            case 1: return nesColor(0x10); // gray
            case 2: return nesColor(0x12); // blue
            case 3: return nesColor(0x1A); // green
            case 4: return nesColor(0x02); // dark blue/purple
            case 5: return nesColor(0x16); // red
            case 6: return nesColor(0x17); // brown/orange
            case 7: return nesColor(0x06); // dark red
            case 8: return nesColor(0x0C); // dark teal
            case 9: return nesColor(0x10); // gray (Death Mountain)
            default: return nesColor(0x10);
        }
    }

    /** Floor color for each dungeon */
    public static Color getDungeonFloorColor(int level) {
        Color wall = getDungeonWallColor(level);
        // Floor is a darker version of the wall color
        return new Color(
            Math.max(wall.getRed() / 3, 10),
            Math.max(wall.getGreen() / 3, 10),
            Math.max(wall.getBlue() / 3, 10)
        );
    }

    /** Door color for each dungeon */
    public static Color getDungeonDoorColor(int level) {
        Color wall = getDungeonWallColor(level);
        return new Color(
            Math.min(wall.getRed() + 40, 255),
            Math.min(wall.getGreen() + 40, 255),
            Math.min(wall.getBlue() + 40, 255)
        );
    }

    // ===== BOSS PALETTE OVERRIDES (sprite palette row 7, from Z_06.asm) =====

    public static Color[] getBossPalette(String bossType) {
        if (bossType == null) return null;
        switch (bossType) {
            case "Aquamentus":
                // $0F,$0A,$29,$30 = black, dark green, green, white
                return new Color[] { nesColor(0x0A), nesColor(0x29), nesColor(0x30) };
            case "Dodongo":
            case "Manhandla":
                // $0F,$17,$27,$30 = black, brown, orange, white (OrangeBoss)
                return new Color[] { nesColor(0x17), nesColor(0x27), nesColor(0x30) };
            case "Digdogger":
                // $0F,$17,$27,$30 = same as orange boss
                return new Color[] { nesColor(0x17), nesColor(0x27), nesColor(0x30) };
            case "Gleeok":
                // $0F,$2A,$1A,$0C = black, green, dark green, dark teal
                return new Color[] { nesColor(0x2A), nesColor(0x1A), nesColor(0x0C) };
            case "Gohma":
                // $0F,$17,$27,$30 = orange boss
                return new Color[] { nesColor(0x17), nesColor(0x27), nesColor(0x30) };
            case "Ganon":
                // $0F,$16,$2C,$3C = black, dark red, blue-green, light blue
                return new Color[] { nesColor(0x16), nesColor(0x2C), nesColor(0x3C) };
            default:
                return null;
        }
    }

    // ===== SPECIAL PALETTES =====

    /** Death animation palette: $0F,$16 */
    public static final Color DEATH_BG2 = nesColor(0x16);    // dark red

    /** Game over palette */
    public static final Color GAME_OVER_BG = nesColor(0x06);  // dark red

    // ===== UTILITY: Color interpolation for fades =====

    /** Interpolate between two colors (t=0 returns a, t=1 returns b) */
    public static Color lerp(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
            (int)(a.getRed() + (b.getRed() - a.getRed()) * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t)
        );
    }

    /** Create a darkened version of a color (factor 0=black, 1=original) */
    public static Color darken(Color c, float factor) {
        return lerp(Color.BLACK, c, factor);
    }

}
