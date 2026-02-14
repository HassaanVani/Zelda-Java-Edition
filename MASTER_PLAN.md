# Legend of Zelda NES → Java: Complete Gap Analysis & Implementation Plan

## Cross-referenced from NES Disassembly (Z_00..Z_07.asm) vs. Current Java Source

---

## PHASE 1: CORE COMBAT & DAMAGE SYSTEM (Critical — affects all gameplay)

### 1A. NES-Accurate Damage Table
**Disasm ref:** `Z_01.asm:5574–5586` — `ObjTypeToDamagePoints` table
- NES uses a per-enemy-type damage table (93 entries), where each byte encodes damage as a packed nibble pair
- **Java gap:** `ZeldaEnemy.damage` is a single int set per-class, not per-type. Many enemies share the same `damage=1`
- **Fix:** Create a `DamageTable` class mapping enemy type → contact damage (half-hearts). Specific values from the disassembly:
  - Octorok (red): 1/2 heart, Octorok (blue): 1 heart
  - Moblin (red): 1/2 heart, Moblin (blue): 1 heart
  - Lynel (red): 1 heart, Lynel (blue): 2 hearts
  - Darknut (red): 1 heart, Darknut (blue): 2 hearts
  - Wizzrobe (red): 1 heart, Wizzrobe (blue): 1 heart
  - Gibdo: 1 heart, LikeLike: 1 heart (+ shield steal)
  - Stalfos: 1/2 heart, Keese: 1/2 heart
  - Goriya (red): 1/2 heart, Goriya (blue): 1 heart
  - Boss damages vary: Aquamentus=1, Dodongo=1, Gleeok=2, Ganon=4 half-hearts

### 1B. NES-Accurate HP Table
**Disasm ref:** `Z_04.asm` — `ExtractHitPointValue` + per-enemy init routines
- NES enemies have specific HP values extracted from level block attributes
- **Java gap:** HP is set per-class constructor (e.g., `Stalfos` always 2 HP) but doesn't match NES values
- **Fix:** Create `EnemyHPTable`:
  - Stalfos: 2, Keese: 1, Gel: 1, Zol: 2, Rope: 1
  - Goriya (red): 3, Goriya (blue): 5, Darknut (red): 4, Darknut (blue): 8
  - Moblin (red): 2, Moblin (blue): 3, Lynel (red): 4, Lynel (blue): 6
  - Octorok (red): 1, Octorok (blue): 2, Tektite (red): 1, Tektite (blue): 2
  - Gibdo: 6, Wizzrobe (blue): 3, Wizzrobe (red): 4, LikeLike: 4, PolsVoice: 6
  - Wallmaster: 2, Vire: 2, Peahat: 2, Leever (blue): 2, Leever (red): 4
  - Ghini: 10, Armos: 3, Zola: 2
  - Bosses: Aquamentus: 6, Dodongo: special (bombs only), Manhandla: 8, Gleeok: 8/head
  - Digdogger: special (recorder shrinks), Gohma: 1 (arrow to eye), Ganon: special

### 1C. Ring Damage Reduction (NES-accurate)
**Disasm ref:** `Z_01.asm:5750–5757` — `Link_BeHarmed`
- NES does `LSR $0D / ROR $0E` per ring level on a 16-bit damage value
- **Java current:** `Inventory.getDamageMultiplier()` returns 0.5/0.25 — correct ratio but applied with `Math.ceil` which never lets damage go below 1
- **Fix:** Matches original behavior. Current implementation is acceptable.

### 1D. Shield Deflection (NES-accurate)
**Disasm ref:** `Z_01.asm:5673–5714` — Shield blocking logic
- NES logic: Link must be idle (not in attack state), facing opposite direction to projectile
  - Small shield blocks: rocks (type < $55), arrows, boomerangs
  - Magical shield additionally blocks: fireballs, magic shots (types $55–$5A)
- **Java gap:** `CombatManager.canShieldDeflect()` checks direction but doesn't differentiate projectile types for small vs. magical shield
- **Fix:** Add projectile type enum. Small shield only blocks arrows/rocks/boomerangs. Magical shield blocks all.

### 1E. Knockback/Shove System
**Disasm ref:** `Z_01.asm:6597–6605` — `BeginShove`
- NES: Shove distance = $20 pixels, invincibility timer = $18 frames for Link; monster shove = $40 pixels, $10 frames
- **Java current:** `KNOCKBACK_FORCE=4` pixels instant push, `INVULN_FRAMES=60`
- **Fix:** NES invuln = $18 = 24 frames, not 60. Knockback should be gradual over frames (ObjShoveDistance decremented each frame), not instant. Monster knockback on hit should also be implemented.

### 1F. Enemy Invincibility Masks (Weapon Immunity)
**Disasm ref:** `Z_01.asm:5900–5926` — `ObjInvincibilityMask`
- NES enemies have bitmask flags for which damage types they're immune to
- Darknut: can only be hit from behind/sides (front attacks parried) — `Z_01.asm:5954–5967`
- Pols Voice: instantly killed by arrows (1 hit regardless of HP)
- Gohma: only damaged by arrows to the open eye — `Z_01.asm:6664–6676`
- Dodongo: only damaged by bombs (swallows them) — `Z_04.asm` `Dodongo_ObjBombHits`
- Ganon: invisible until hit by Silver Arrow — `Z_04.asm` `Ganon_ObjPhase`
- Wizzrobe (blue): immune to boomerang
- **Java gap:** No immunity system. All enemies take damage from all sources equally.
- **Fix:** Add `invincibilityMask` field to `ZeldaEnemy`, check against weapon type in `CombatManager`.

---

## PHASE 2: ITEM DROP SYSTEM (Deterministic, not random)

### 2A. NES Drop Table System
**Disasm ref:** `Z_04.asm:11030–11200` — `SetUpDroppedItem`
- NES uses a **deterministic** drop system, NOT random:
  1. Enemies are grouped into 4 drop classes (0-3) based on type
  2. A global `WorldKillCycle` counter (0-9) increments with each kill
  3. The drop is looked up from a 4×10 table indexed by [class][killCycle]
  4. A per-class random threshold (`DropItemRates`: $50, $98, $68, $68) determines if the drop appears
  5. Items: $00=bomb, $0F=5rupees, $18=rupee, $21=heart, $22=fairy, $23=fairy
  6. Every 16th kill (`WorldKillCount == $10`) guarantees a fairy
  7. After 10 consecutive kills without Link being hurt (`HelpDropCount >= $0A`), guaranteed bomb or 5-rupee drop
- **Java gap:** `ZeldaRoom.dropItem()` uses simple `Math.random()` probabilities — completely wrong
- **Fix:** Implement the full NES drop system: kill cycle, drop class lookup table, help drop counter, fairy guarantee.

### 2B. Specific Enemy Drop Classes (from disassembly)
- **Class 0** (easiest enemies): Leever(blue), Moblin(red), Octorok(red), Rope, Keese, Stalfos
- **Class 1** (medium): Armos, Ghini, Darknut(red), Goriya(red), Gel, Zora, Lynel(red), LikeLike, Peahat
- **Class 2** (hard): Gibdo, Darknut(blue), Wizzrobe(blue), Vire, Goriya(blue), Octorok(blue), Leever(red), Moblin(blue), Pols Voice
- **Class 3** (bosses/special): everything else
- Certain types NEVER drop items: fireballs, child gels, flying ghini, red keese, bubbles, traps, boulders

---

## PHASE 3: ENEMY AI — NES-ACCURATE BEHAVIORS

### 3A. Missing Enemy Types
- **Gel / Zol:** Currently mapped to Keese behavior — wrong. Gel is a tiny blob that hops randomly. Zol splits into 2 Gels when hit by non-killing damage.
- **Bubble:** Currently mapped to Keese — wrong. Bubbles are invincible, move randomly, and on contact disable Link's sword temporarily (blue=disable, red=re-enable).
- **Lamnola:** Giant centipede-like boss (Level 4/8 in 2nd quest), not implemented at all.
- **Patra:** Flying orbital boss (Level 6/9 in 2nd quest), not implemented.
- **Moldorm:** Worm boss, not implemented.
- **Trap:** Stationary enemy that charges at Link when aligned on axis — `Z_01.asm` `InitTrap_Full` / `UpdateTrap_Full`. Not implemented.
- **Boulder:** Falling rocks in Death Mountain — `Z_04.asm` `InitBoulder` / `UpdateBoulderSet`. Not implemented.
- **Statue (Guard Fire):** Dungeon statues that shoot fireballs — `Z_04.asm` `UpdateStatues`. Not implemented.
- **Pond Fairy:** Healing fairy at ponds — `Z_04.asm` `InitPondFairy` / `UpdatePondFairy`. Not implemented.

### 3B. Enemy-Specific Behavior Fixes (from disassembly)

**Octorok** (`Z_04.asm UpdateOctorock`):
- Red: walks randomly, shoots rocks occasionally. Blue: faster, tougher.
- Shooting: timer-based, fires a rock projectile in facing direction
- **Java gap:** Current Octorok has basic movement but shooting logic may not match NES timing

**Darknut** (`Z_04.asm UpdateDarknut`):
- Walks randomly, changes direction at walls
- **Cannot be damaged from the front** — attacks bounce off the shield
- Red: 4 HP, Blue: 8 HP
- **Java gap:** No front-shield mechanic at all

**Wizzrobe** (`Z_04.asm UpdateBlueWizzrobe / UpdateRedWizzrobe`):
- Blue: moves through walls, appears/disappears, shoots magic
- Red: teleports, appears briefly to shoot, disappears
- **Java gap:** Basic movement but not the NES teleport/phase-through behavior

**Goriya** (`Z_04.asm UpdateGoriya`):
- Throws boomerang at Link, waits for return before throwing again
- **Java gap:** May not have proper boomerang return mechanic

**Wallmaster** (`Z_04.asm UpdateWallmaster`):
- Emerges from walls, grabs Link, sends back to dungeon entrance
- **Java gap:** Current version chases from walls but doesn't teleport Link back

**LikeLike** (`Z_04.asm UpdateLikeLike`):
- Absorbs Link, steals Magical Shield after capture timer
- **Java gap:** No shield-stealing mechanic

**Rope** (`Z_04.asm UpdateRope`):
- Normally wanders; charges when aligned with Link on an axis
- **Java gap:** May not have the charging behavior

**Leever** (`Z_04.asm UpdateBlueLeever / UpdateRedLeever`):
- Blue: emerges from sand, moves, submerges
- Red: similar but faster, tracks toward Link
- `ActiveRedLeeverCount` limits concurrent reds to 2
- **Java gap:** May not have emerge/submerge cycle

**Vire** (`Z_04.asm UpdateVire`):
- When killed, splits into 2 Red Keese
- **Java gap:** No splitting behavior

**Peahat** (`Z_04.asm UpdatePeahat`):
- Invulnerable while flying; only vulnerable when stopped
- **Java gap:** No invulnerability-while-moving mechanic

**Ghini** (`Z_04.asm UpdateGhini / UpdateFlyingGhini`):
- First Ghini in graveyard is the "ringleader" — killing it kills all ghinis
- Other ghinis spawn when Link touches gravestones
- **Java gap:** No ringleader mechanic

### 3C. Boss-Specific Fixes

**Dodongo** (`Z_04.asm UpdateDodongo`):
- Immune to all weapons. Only damaged by swallowing bombs (walks over them)
- Needs 2 bomb hits to die. Bloated substate between hits.
- **Java gap:** Can be damaged by sword in current code

**Digdogger** (`Z_04.asm UpdateDigdogger`):
- Large form is invulnerable. Playing the Recorder shrinks it into 1 or 3 small Digdoggers
- Small Digdoggers can be damaged normally
- **Java gap:** No recorder interaction, no split mechanic

**Gohma** (`Z_04.asm UpdateGohma`):
- Only damaged by arrows hitting the open eye
- Eye cycles: closed → half-open → open → half-open → closed
- Red Gohma: 1 arrow kill; Blue Gohma: 3 arrows
- **Java gap:** No eye state, damageable by any weapon

**Ganon** (`Z_04.asm UpdateGanon`):
- Phase 1: invisible, teleports around room, shoots fireballs at Link
- Phase 2: after enough hits, becomes visible briefly
- Phase 3: must be finished with Silver Arrow to die
- Without Silver Arrow, Ganon cannot be killed
- **Java gap:** No invisibility, no phase system, no Silver Arrow requirement

**Gleeok** (`Z_04.asm UpdateGleeok`):
- Multiple detachable heads on necks. Each head must be destroyed.
- Detached heads float around shooting fireballs
- Level 4: 2 heads, Level 8: 4 heads
- **Java gap:** No neck/head detachment system

**Manhandla** (`Z_04.asm UpdateManhandla`):
- 4 pincers that shoot fireballs. Each pincer can be destroyed individually.
- Gets faster as pincers are destroyed. Bomb kills all pincers.
- **Java gap:** No multi-part body or speed increase

---

## PHASE 4: OVERWORLD COMPLETENESS

### 4A. Complete Room Data (128 rooms)
**Disasm ref:** `Z_05.asm` — LayoutRoomOW, room column data, LevelBlockAttrs
- NES overworld is 16×8 = 128 unique rooms, each with specific enemy spawns, tile layouts, and attributes
- **Java gap:** `RoomData.getRoomDef()` only defines ~20 rooms. The rest use biome-random fallback.
- **Fix:** Define all 128 rooms with correct enemy types, counts, and positions from NES data

### 4B. Overworld Secrets System
**Disasm ref:** `Z_05.asm:2374–2520` — `CheckSecretTrigger` + trigger types
- NES has 7 secret trigger types: None, AllDead, Ringleader, LastBoss, BlockDoor, BlockStairs, MoneyOrLife
- Secrets include: bombable walls → cave, burn bush → stairway, push rock → stairway, recorder → lake drains, push gravestone → stairway
- **Java gap:** `RoomData.SecretType` enum exists but no trigger/reveal logic is implemented
- **Fix:** Implement:
  - Bomb a specific wall tile → reveal cave entrance
  - Candle on specific bush tile → burn and reveal stairway
  - Push specific Armos statue or rock → reveal stairway
  - Play Recorder at specific lake screen → drain water, reveal dungeon entrance
  - Push specific gravestone → reveal stairway

### 4C. Fairy Fountains
- NES has specific pond locations where fairies heal Link to full
- **Java gap:** `SecretType.FAIRY_FOUNTAIN` exists in enum but no healing logic
- **Fix:** When Link steps on fairy fountain tile, gradually restore all hearts

### 4D. Raft Travel
**Disasm ref:** `Z_04.asm UpdateDock`
- Raft item lets Link travel across specific water tiles to reach otherwise-inaccessible areas
- Required to reach Level 4 (in some interpretations) and certain secrets
- **Java gap:** `hasRaft` tracked in inventory but no dock/travel mechanic
- **Fix:** Add dock tiles at specific rooms; when Link with raft steps on dock, auto-move across water

### 4E. Stepladder Logic
- Stepladder lets Link cross single-tile-wide water/gaps
- **Java gap:** `hasLadder` tracked but no crossing logic
- **Fix:** When Link is adjacent to a 1-tile water gap and has Ladder, auto-place bridge sprite

### 4F. Recorder / Whistle Effects
- Overworld: plays sound, creates whirlwind that teleports Link to dungeon entrances already visited
- Certain screen: drains lake to reveal Level 7 entrance
- In dungeons: shrinks Digdogger boss
- **Java gap:** Recorder item exists but has no gameplay effect
- **Fix:** Implement whirlwind teleport system and lake-drain secret

### 4G. Power Bracelet
- Lets Link push certain rocks/boulders to reveal secrets
- **Java gap:** `hasBracelet` tracked but no push mechanic on overworld tiles
- **Fix:** Mark specific overworld tiles as pushable; when bracelet owned + pushing into boulder, move it

### 4H. Food/Bait
- When used, placed on ground; certain enemies are attracted to it (Goriya in dungeon room blocks path until given food)
- **Java gap:** Food exists as B-item but no placement or attraction logic

### 4I. Letter → Potion Shop Chain
- Letter must be delivered to old woman to unlock potion purchases
- **Java gap:** `letterState` tracked but the potion shop cave doesn't check it
- **Fix:** Potion shop cave should verify `letterState >= 2` before allowing purchases

### 4J. Overworld Scroll/Transition Animation
- NES scrolls the screen smoothly during room transitions (not instant cut)
- **Java gap:** `ROOM_TRANSITION` state exists but renders a black screen during transition
- **Fix:** Implement smooth scroll animation where old room slides out and new room slides in

---

## PHASE 5: DUNGEON COMPLETENESS

### 5A. Accurate Dungeon Layouts (all 9 dungeons)
**Disasm ref:** `Z_05.asm` — LayoutUWFloor, room layouts, column data
- NES dungeons have complex multi-room layouts with specific shapes (Eagle, Moon, Manji, Snake, etc.)
- **Java gap:** All 9 dungeons use a simplified identical layout template (linear column of rooms)
- **Fix:** Implement actual NES dungeon room grids matching the iconic shapes:
  - Level 1 "Eagle": 8×4 grid, eagle shape
  - Level 2 "Moon": crescent shape
  - Level 3 "Manji": swastika/manji shape
  - Level 4 "Snake": snake shape
  - Level 5 "Lizard": lizard shape
  - Level 6 "Dragon": dragon shape
  - Level 7 "Demon": demon shape
  - Level 8 "Lion": lion shape
  - Level 9 "Death Mountain": skull shape, largest dungeon

### 5B. Dungeon Door Types (from disassembly)
**Disasm ref:** `Z_05.asm:7744–7790` — `CalcOpenDoorwayMask`
- NES door types: Open, Wall(1-3 types), Bombable, Locked, Shutter (opens when all enemies killed)
- **Java gap:** Missing SHUTTER door type — doors that are initially sealed and open when room is cleared
- **Fix:** Add `DoorState.SHUTTER` that automatically opens when `cleared == true`

### 5C. Dungeon Stairways / Cellars
**Disasm ref:** `Z_05.asm:2476–2501` — `CheckSecretTriggerBlockStairs`
- NES dungeons have stairways that connect non-adjacent rooms (underground passages)
- Push block to reveal stairway → enter → emerge in different room
- **Java gap:** `DungeonRoomDef` has `isStairway` and `stairwayTarget` fields but they're never used
- **Fix:** Implement stairway transitions: render stairway tile after block push, entering transitions to target room

### 5D. Old Man Rooms in Dungeons
**Disasm ref:** `Z_01.asm:74–108` — `InitCave` + underworld persons
- NES dungeons contain old man rooms (hints, money/life choice, item shops)
- "Money or Life" rooms: pay rupees or lose a heart container
- "Grumble Grumble" rooms: Goriya blocks path until given Food
- **Java gap:** No old man rooms, no money-or-life, no grumble rooms
- **Fix:** Add old-man room type to DungeonRoomDef, implement the interactions

### 5E. Dungeon Room Item Persistence
**Disasm ref:** `Z_01.asm:4363–4380` — `SetRoomFlagUWItemState` / `GetRoomFlagUWItemState`
- NES tracks per-room flags: bit 4 = item taken, bits 0-3 = door states
- Once a room item is collected, it never reappears
- **Java gap:** `Inventory.clearedDungeonRooms` tracks cleared rooms but not item collection per-room
- **Fix:** Track `collectedDungeonItems` set in Inventory; room items don't respawn if already collected

### 5F. Shutter Doors (Kill-All Trigger)
**Disasm ref:** `Z_05.asm:2415–2425` — `CheckSecretTriggerAllDead` → `TriggerShutters`
- When all enemies in room are killed, shutter doors open with sound effect
- **Java gap:** `onRoomCleared()` opens LOCKED doors but should open SHUTTER doors, not locked ones
- **Fix:** Change room clear logic: only SHUTTER doors open on clear. LOCKED doors require keys always.

### 5G. Block Push Mechanics
**Disasm ref:** `Z_05.asm:5470–5540` — `FindAndCreatePushBlockObject`
- Block can only be pushed in one specific direction per room
- Block push reveals stairway or opens shutter doors
- Only pushable AFTER room is cleared
- **Java gap:** Current block push is too simple — pushes in any direction on contact
- **Fix:** Block should only move when Link pushes it in the intended direction; require room cleared first

---

## PHASE 6: LINK MECHANICS

### 6A. Movement Speed (NES-accurate)
**Disasm ref:** `Z_07.asm` — Link moves at 1.5 pixels/frame
- **Java current:** `MOVE_SPEED = 1.5` — ✅ correct

### 6B. Sword Attack Duration
- NES: attack lasts about 12 frames
- **Java current:** `ATTACK_DURATION = 12` — ✅ correct

### 6C. Sword Beam Firing
**Disasm ref:** `Z_07.asm:1906–1907` — `UpdateSwordShotOrMagicShot`
- Only fires when at full health, once per attack
- **Java current:** Implemented in CombatManager — ✅ mostly correct
- **Fix:** Beam speed should be 3 pixels/frame (matches `Z_07.asm`). Currently 3.0 — correct.

### 6D. Candle Fire Behavior
- Blue candle: 1 use per screen. Red candle: unlimited.
- Fire travels forward ~48 pixels then becomes a stationary flame briefly
- **Java gap:** Fire projectile just moves until lifetime expires, doesn't become stationary
- **Fix:** After traveling ~32px, stop movement and persist as burning tile for ~60 frames

### 6E. Boomerang Return
- NES boomerang travels forward, then returns to Link's current position
- Magical boomerang travels further and faster
- Stuns enemies on hit (doesn't damage most)
- **Java gap:** Boomerang created as a simple projectile — no return mechanic
- **Fix:** Track owner position, reverse velocity after max range; return and deactivate on reaching Link

### 6F. Bomb Behavior
**Disasm ref:** `Z_07.asm:4783–4900` — `UpdateBomb`
- NES bomb has 5 states: placed → ticking → flash → explode → fade
- Explosion has area damage and can break bombable walls
- Timings from `BombTimes`: $30, $18, $0C, $06 frames
- **Java gap:** Bomb is a stationary damaging projectile — no explosion animation or multi-phase
- **Fix:** Implement bomb phases with proper timing, explosion radius, wall-breaking check

### 6G. Arrow Behavior
- Arrows fly straight until hitting enemy or wall; single use costs 1 rupee
- Silver arrows deal 4× damage and are required to kill Ganon
- **Java current:** Implemented — mostly ✅
- **Fix:** Arrow should disappear on wall collision, not just lifetime

### 6H. Rod + Book
- Magical Rod fires a projectile beam
- With Book of Magic: rod beam leaves a flame on impact (like candle fire)
- **Java gap:** Rod fires beam but no Book interaction for flame-on-impact
- **Fix:** If `hasBook`, spawn fire projectile at rod beam's death position

---

## PHASE 7: HUD & UI

### 7A. Minimap
- Overworld: shows Link's position on the overworld grid with a blinking dot
- Dungeon: shows explored rooms, current position, map layout
- **Java current:** `ZeldaHUD` exists but may need NES-accurate layout
- **Fix:** Verify minimap matches NES layout (position, colors, blinking)

### 7B. Inventory/Subscreen
- NES subscreen shows all collected items, B-item selection, triforce pieces, dungeon map
- **Java current:** `InventoryScreen` exists
- **Fix:** Verify it matches NES layout and allows proper B-item selection cycling

### 7C. Heart Display
- NES: hearts fill from right to left; partial hearts show as half-filled
- **Java gap:** Verify rendering matches NES (red=full, half=half, empty=outline)

### 7D. Item Get Animation
- NES: Link holds item above head, freeze frame, fanfare plays
- **Java gap:** Items are collected instantly with no animation
- **Fix:** Implement item-lift state: freeze player, show item above head for ~120 frames, play fanfare

---

## PHASE 8: GAME FLOW & STATE MANAGEMENT

### 8A. Death Animation
**Disasm ref:** `Z_01.asm:5807–5823` — `HandleDied`
- NES: Link spins in place, turns red, screen fades, then Game Over screen with save options
- **Java gap:** Instant transition to GAME_OVER state
- **Fix:** Add death animation state: spin 4 directions, color cycle, fade to black over ~120 frames

### 8B. Game Over Screen (NES-accurate)
- Shows "GAME OVER" with continue/save/retry options
- Death count tracked per save file
- **Java gap:** Simple text screen with "PRESS ENTER"
- **Fix:** Match NES game over screen with proper options; track death count in SaveManager

### 8C. Screen Transition Animation
- NES: smooth scroll between rooms (~32 frames)
- **Java gap:** Instant cut or black screen during transition
- **Fix:** Implement proper scrolling transition rendering both rooms simultaneously

### 8D. Cave Entry/Exit Animation
- NES: Link walks up into cave entrance (animation), screen fades
- **Java gap:** Instant teleport to cave
- **Fix:** Animate Link walking up, darken screen, fade to cave interior

### 8E. Title Screen / File Select
- NES has full animated title screen with waterfall, story scroll, then file select (3 save slots)
- **Java current:** `TitleScreen` class exists — verify completeness
- **Fix:** Ensure file select matches NES (REGISTER YOUR NAME, ELIMINATION MODE)

### 8F. Ending Sequence
- After defeating Ganon: rescue Zelda, ending credits scroll with images
- **Java gap:** Simple "CONGRATULATIONS" text
- **Fix:** Implement proper ending sequence with Zelda rescue scene, text scroll, and credits

### 8G. Second Quest
**Disasm ref:** `Variables.inc:229` — `QuestNumbers`
- After completing game, second quest unlocks with rearranged dungeons, enemies, and secrets
- **Java gap:** `secondQuest` flag exists in Inventory but no second quest data
- **Fix:** Long-term goal — define alternate room data, dungeon layouts, and cave contents for 2nd quest

---

## PHASE 9: AUDIO

### 9A. Sound Effects
**Disasm ref:** `Z_00.asm` — Complete audio driver with all SFX
- NES has distinct SFX for: sword swing, sword beam, enemy hit, enemy die, Link hurt, item pickup, key get, door open, bomb explode, arrow fire, boomerang, shield deflect (parry), stairs, heart beep (low health), text scroll, secret found, recorder, fairy
- **Java gap:** Most SFX constants are empty strings (e.g., `SFX_SWORD = ""`)
- **Fix:** Map all SFX to WAV files in /sounds/ directory. Add missing SFX triggers throughout code.

### 9B. Music Tracks
- NES tracks: Title, Overworld, Dungeon (regular), Level 9 Dungeon, Game Over, Item Fanfare, Triforce Fanfare, Ending, Ganon Battle
- **Java gap:** Only Title, Overworld, Dungeon, Game Over music mapped
- **Fix:** Add remaining tracks, especially Level 9's unique theme and Ganon battle music

### 9C. Low Health Beep
- NES: when at 1 heart or less, a continuous beeping sound plays
- **Java gap:** Not implemented
- **Fix:** In game loop, check health; if ≤ 2 half-hearts, play repeating beep tone

---

## PHASE 10: SAVE SYSTEM

### 10A. NES-accurate Save Data
**Disasm ref:** `Variables.inc:301–347` — Save file structure
- NES saves: inventory, world flags (per-room visited/cleared/item-taken), heart containers, death count, quest number
- **Java current:** `SaveManager` saves basic data via Properties file
- **Fix:** Ensure ALL per-room flags persist: which rooms visited, which items collected, which doors opened, which secrets revealed

### 10B. Three Save Slots
- NES has 3 independent save slots with name entry
- **Java gap:** `currentSaveSlot` exists but verify title screen offers 3-slot selection
- **Fix:** Title screen should display 3 save files with names, heart counts, and death counts

---

## IMPLEMENTATION PRIORITY ORDER

1. **Phase 1** (Combat) — Foundation everything else depends on
2. **Phase 2** (Item Drops) — Core gameplay loop
3. **Phase 3** (Enemy AI) — Makes combat interesting
4. **Phase 6** (Link Mechanics) — Boomerang return, bomb phases, item animations
5. **Phase 5** (Dungeons) — Accurate layouts, shutters, stairways
6. **Phase 4** (Overworld) — Secrets, raft, ladder, all 128 rooms
7. **Phase 7** (HUD/UI) — Polish
8. **Phase 8** (Game Flow) — Animations, transitions
9. **Phase 9** (Audio) — SFX and music
10. **Phase 10** (Save System) — Persistence
11. **Phase 8G** (Second Quest) — Stretch goal

---

## KEY FILES IN DISASSEMBLY (reference guide)

| File | Contents |
|------|----------|
| `Z_00.asm` | Audio driver, all SFX/music playback |
| `Z_01.asm` | Cave/person logic, collision detection, damage calculation, item pickup, shove/knockback |
| `Z_02.asm` | Link update, movement, weapon handling, animation |
| `Z_03.asm` | PPU/rendering utilities |
| `Z_04.asm` | ALL enemy Init/Update routines, boss logic, item drop system, projectiles |
| `Z_05.asm` | Room layout engine (OW + UW), door system, tile rendering, secret triggers, block push |
| `Z_06.asm` | Status bar, HUD, inventory screen rendering |
| `Z_07.asm` | Game mode management, world update loop, weapon updates, object initialization |
| `Variables.inc` | All RAM variable definitions (addresses + names) |
| `ObjVars.inc` | Per-enemy-type variable aliases |
| `CommonVars.inc` | Shared object variables |
