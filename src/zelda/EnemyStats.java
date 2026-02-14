package zelda;

/**
 * NES-accurate enemy statistics derived from the Z_04.asm disassembly.
 * HP values from ExtractHitPointValue, damage from ObjTypeToDamagePoints table (Z_01.asm:5574),
 * drop classes from DropItemMonsterTypes0/1/2 (Z_04.asm:11035-11044).
 *
 * Contact damage is in half-hearts (NES native unit).
 * HP is in sword hits (wooden sword = 1 damage point).
 */
public class EnemyStats {

    // Weapon damage types (bitmask) — used for invincibility checks
    public static final int DMG_SWORD       = 0x01;
    public static final int DMG_BOOMERANG   = 0x02;
    public static final int DMG_ARROW       = 0x04;
    public static final int DMG_BOMB        = 0x08;
    public static final int DMG_FIRE        = 0x10;
    public static final int DMG_ROD         = 0x20;
    public static final int DMG_ALL         = 0x3F;

    /** Describes a single enemy type's NES-accurate stats. */
    public static class Stats {
        public final int hp;
        public final int contactDamage;   // in half-hearts
        public final double speed;
        public final int dropClass;       // 0-3 for drop table, -1 = no drops
        public final int immunityMask;    // bitmask of DMG_ types that DO NOT affect this enemy

        public Stats(int hp, int contactDamage, double speed, int dropClass, int immunityMask) {
            this.hp = hp;
            this.contactDamage = contactDamage;
            this.speed = speed;
            this.dropClass = dropClass;
            this.immunityMask = immunityMask;
        }
    }

    // ==================== OVERWORLD ENEMIES ====================

    public static Stats octorokRed()    { return new Stats(1,  1, 0.5,  0, 0); }
    public static Stats octorokBlue()   { return new Stats(2,  1, 0.75, 2, 0); }

    public static Stats moblinRed()     { return new Stats(2,  1, 0.5,  0, 0); }
    public static Stats moblinBlue()    { return new Stats(3,  2, 0.75, 2, 0); }

    public static Stats lynelRed()      { return new Stats(4,  2, 0.5,  1, 0); }
    public static Stats lynelBlue()     { return new Stats(6,  4, 0.75, 2, 0); }

    public static Stats tektiteRed()    { return new Stats(1,  1, 1.0,  0, 0); }
    public static Stats tektiteBlue()   { return new Stats(2,  1, 1.2,  0, 0); }

    public static Stats leeverRed()     { return new Stats(4,  2, 0.75, 2, 0); }
    public static Stats leeverBlue()    { return new Stats(2,  1, 0.5,  0, 0); }

    public static Stats peahat()        { return new Stats(2,  1, 1.0,  1, 0); }
    public static Stats ghini()         { return new Stats(10, 1, 0.75, 1, 0); }
    public static Stats armos()         { return new Stats(3,  1, 0.5,  1, 0); }
    public static Stats zola()          { return new Stats(2,  1, 0.0,  1, 0); }

    // ==================== DUNGEON ENEMIES ====================

    public static Stats stalfos()       { return new Stats(2,  1, 0.5,  0, 0); }

    public static Stats keeseBlue()     { return new Stats(1,  1, 1.2,  0, 0); }
    public static Stats keeseRed()      { return new Stats(1,  1, 1.0, -1, 0); } // red keese: no drops

    public static Stats goriyaRed()     { return new Stats(3,  1, 0.5,  1, 0); }
    public static Stats goriyaBlue()    { return new Stats(5,  2, 0.75, 2, 0); }

    // Darknuts: immune to frontal attacks (handled in CombatManager, not via mask)
    public static Stats darknutRed()    { return new Stats(4,  2, 0.5,  1, 0); }
    public static Stats darknutBlue()   { return new Stats(8,  4, 0.75, 2, 0); }

    // Wizzrobes: blue is immune to boomerang
    public static Stats wizzrobeRed()   { return new Stats(3,  2, 0.75, 2, 0); }
    public static Stats wizzrobeBlue()  { return new Stats(3,  2, 0.0,  1, DMG_BOOMERANG); }

    public static Stats gibdo()         { return new Stats(6,  2, 0.5,  2, 0); }
    public static Stats likeLike()      { return new Stats(4,  1, 0.4,  1, 0); }
    public static Stats polsVoice()     { return new Stats(6,  1, 1.0,  2, 0); } // 1-hit kill by arrow handled specially
    public static Stats vire()          { return new Stats(2,  1, 1.0,  2, 0); }
    public static Stats rope()          { return new Stats(1,  1, 1.5,  0, 0); }
    public static Stats wallmaster()    { return new Stats(2,  1, 1.0,  2, 0); }

    // Gel / Zol (currently mapped to Keese)
    public static Stats gel()           { return new Stats(1,  1, 0.75, -1, 0); } // no drops
    public static Stats zol()           { return new Stats(2,  1, 0.5,  2, 0); }

    // Bubble: completely invulnerable, no contact damage (disables sword instead)
    public static Stats bubble()        { return new Stats(999, 0, 1.0, -1, DMG_ALL); }

    // Trap: invulnerable blade trap, 1 damage on contact, no drops
    public static Stats trap()          { return new Stats(999, 1, 3.0, -1, DMG_ALL); }

    // ==================== BOSSES ====================

    public static Stats aquamentus()    { return new Stats(6,  2, 0.25, 3, DMG_BOOMERANG); }
    // Dodongo: immune to everything except bombs (swallowed)
    public static Stats dodongo()       { return new Stats(1,  1, 0.5,  3, DMG_SWORD | DMG_BOOMERANG | DMG_ARROW | DMG_FIRE | DMG_ROD); }
    public static Stats manhandla()     { return new Stats(8,  2, 0.5,  3, DMG_BOOMERANG); }
    public static Stats gleeok()        { return new Stats(8,  2, 0.25, 3, DMG_BOOMERANG); }
    public static Stats digdogger()     { return new Stats(6,  2, 0.5,  3, DMG_ALL); } // invulnerable until recorder shrinks
    public static Stats digdoggerSmall(){ return new Stats(1,  1, 1.5,  3, 0); }
    // Gohma: only damaged by arrows to open eye (handled in combat, not via mask)
    public static Stats gohmaRed()      { return new Stats(1,  2, 0.25, 3, 0); }
    public static Stats gohmaBlue()     { return new Stats(3,  2, 0.25, 3, 0); }
    // Ganon: immune to everything except Silver Arrow in final phase
    public static Stats ganon()         { return new Stats(8,  4, 0.0,  3, 0); }

    // ==================== PROJECTILE ENEMY DAMAGE ====================
    // (damage from enemy projectiles like rocks, spears, magic — in half-hearts)
    public static final int ROCK_DAMAGE = 1;
    public static final int SPEAR_DAMAGE = 1;
    public static final int MAGIC_DAMAGE = 2;
    public static final int FIREBALL_DAMAGE = 2;
    public static final int SWORD_BEAM_DAMAGE = 2;
    public static final int BOOMERANG_PROJ_DAMAGE = 1;
}
