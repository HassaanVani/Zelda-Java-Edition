# MASTER PLAN — NES-Accurate Legend of Zelda Java Recreation

**Generated**: Feb 22, 2026 | **Source**: Cross-reference of 60 Java files vs zelda1-disassembly-master

---

## CURRENT STATE SUMMARY

### Working
- 16×8 overworld with sprite-map rendering + collision
- 9 dungeons with rooms, doors, enemies, items, stairways
- 27 enemy types + 10 bosses with basic AI
- Full inventory, deterministic item drops, combat with shield deflection
- Cave system (31 IDs), title/game-over/win screens, save/load, room transitions
- Lost Woods/Hills maze, raft, stepladder, recorder warp

### Key Gaps (cross-referenced against disassembly)
- Damage/HP tables use simple ints instead of NES nibble-packed values
- Most enemies use simplified randomMove() instead of NES-specific AI
- Bosses missing NES phase details (Ganon intro, Dodongo bloat states, Gleeok detached heads)
- Many overworld rooms fall back to biome-random spawning
- Missing: dungeon old-man rooms, shutter door animation, proper grid movement
- Audio SFX are mostly placeholders
- Second Quest data not implemented

---

## PHASE 1: COMBAT & DAMAGE (CRITICAL)
**Files**: CombatManager, ZeldaPlayer, Inventory, EnemyStats
**ASM**: Z_01.asm:5574 ObjTypeToDamagePoints, Z_07.asm:5256 ObjectTypeToHpPairs

### 1.1 NES Damage Table
The NES packs contact damage into nibbles (Z_01.asm:5574-5586). $80=½heart, $01=1heart, $02=2hearts, $04=4hearts. Ring reduces via bit-shift per level.
- [ ] Add NES damage lookup or fix all EnemyStats contactDamage values
- [ ] Ring reduction: shift damage right once per ring level (not multiply)

### 1.2 HP Table Corrections
NES ObjectTypeToHpPairs (Z_07.asm:5256) nibble-packs two enemies per byte. Several Java values are wrong:
- [ ] Blue Lynel: Java=6 → NES=4
- [ ] Blue Moblin: Java=3 → NES=5  
- [ ] Blue Goriya: Java=5 → NES=1
- [ ] Decode+fix ALL enemy HPs from the NES table

### 1.3 Weapon Damage
- [ ] Rod beam always does 2 damage (Book only adds fire, doesn't change beam dmg)
- [ ] Sword beam damage = sword damage ✓

### 1.4 Shield Deflection
- [ ] Compare Link facing dir vs projectile velocity dir (not position-based)
- [ ] Wizzrobe magic + statue fire = unblockable by small shield ✓

### 1.5 Knockback
- [ ] Change KNOCKBACK_DISTANCE from 32 to 16 (NES = 8 frames × 2px = 16px)
- [ ] Block knockback against walls

---

## PHASE 2: LINK MECHANICS & MOVEMENT (HIGH)
**Files**: ZeldaPlayer, ZeldaGame | **ASM**: Z_05.asm:6921 Link_HandleInput

### 2.1 Grid Movement
NES Link moves on 8px grid. At grid offset=0 can change direction; between points can only continue/stop.
- [ ] Implement gridOffset tracking
- [ ] At offset 0: allow direction change; otherwise continue or stop
- [ ] Diagonal input: pick perpendicular to current direction

### 2.2 Sword
- [ ] Sword hitbox can collect item drops on contact
- [ ] Boomerang return path collects items
- [ ] Only one sword beam active at a time

### 2.3 Push Animation
- [ ] Add "pushing" frame when Link walks into walls (visual feedback for block push)

---

## PHASE 3: ENEMY AI FIXES (HIGH)
**Files**: enemies/*.java | **ASM**: Z_04.asm Update* routines, Z_07.asm:5321 jump table

### 3.1 Shooter Enemies — Currently don't shoot
- [ ] **Octorok**: Shoot rocks (dmg=½heart) every ~64 frames in facing direction
- [ ] **Moblin**: Shoot spears (dmg=½heart) periodically
- [ ] **Lynel**: Shoot sword beams (dmg=1heart) periodically
- [ ] **Goriya**: Throw boomerang that returns to self; pause while boomerang out

### 3.2 Darknut Fixes (Z_04.asm:6475)
- [ ] Never stunned (boomerang has no effect)
- [ ] Walk straight, turn on wall collision (not timer-based random turns)

### 3.3 Wizzrobe Rework (MAJOR)
- [ ] **Blue**: Teleport-appear-shoot-disappear cycle (not walker)
- [ ] **Red**: Normal walker + periodic magic beam
- [ ] Magic beam: unblockable by small shield, magic shield CAN block

### 3.4 Bubble (3 variants)
- [ ] Normal: disable sword 256 frames ✓
- [ ] Red: permanent sword disable (only Blue Bubble or fairy cures)
- [ ] Blue: cures sword disable on contact
- [ ] All: diagonal bounce movement

### 3.5 Trap (Blade Trap)
- [ ] Lunge toward Link when aligned on axis, fast (3.0)
- [ ] Return to origin slowly (~1.0)

### 3.6 Gel/Zol Split
- [ ] Zol: on sword hit splits into 2 Gels (not on kill—on damage)
- [ ] Spawned Gels have dropClass=-1

### 3.7 Vire Split
- [ ] On death: splits into 2 Red Keese (dropClass=-1)

### 3.8 Leever (Red vs Blue)
- [ ] Blue: emerge/submerge from ground randomly
- [ ] Red: appears near Link, more aggressive
- [ ] Invulnerable while submerged

### 3.9 Peahat
- [ ] Invulnerable while moving; only hittable when stopped

### 3.10 Like-Like Shield Eating
- [ ] On sustained contact (~120 frames): eat Magical Shield → downgrade to Small
- [ ] Link escapes by killing it or taking damage from another source

### 3.11 Pols Voice (Z_04.asm:6533)
- [ ] Jump physics: parabolic arc with gravity ($38 acceleration)
- [ ] Immune to everything EXCEPT sword and arrow (arrow=1-hit kill ✓)

### 3.12 Ghini (Overworld)
- [ ] 1 "real" Ghini per screen; touching gravestones spawns invulnerable copies
- [ ] Kill original = kill all copies

### 3.13 Armos
- [ ] Start as immobile statue; activate on Link's touch
- [ ] Inactive = invulnerable + looks like scenery

### 3.14 Zola
- [ ] Submerge/surface cycle; shoots fireball when surfaced
- [ ] Fireball is unblockable; only hittable while surfaced

### 3.15 Rope
- [ ] Charge at high speed when Link enters same row/column

### 3.16 Wallmaster
- [ ] Emerge from wall nearest to Link; add emergence animation

### 3.17 Spark (Anti-Fairy)  
- [ ] Follow room wall perimeter (clockwise/counter-clockwise), not random

---

## PHASE 4: BOSS AI — NES ACCURACY (HIGH)
**Files**: bosses/*.java | **ASM**: Z_04.asm boss Update routines

### 4.1 Aquamentus — Horizontal only movement, 3-fireball spread ✓ mostly
### 4.2 Dodongo (Z_04.asm:5855)
- [ ] Bomb must be eaten from FRONT (facing direction check)
- [ ] 3 states: Move, Bloated (3 substates with timers $20/$40/$40), Stunned
- [ ] 2 full bloat cycles then die

### 4.3 Manhandla
- [ ] Speed DOUBLES per destroyed hand (critical missing behavior)
- [ ] Each hand shoots independently

### 4.4 Gleeok
- [ ] Severed heads become invulnerable floating entities that shoot fireballs

### 4.5 Digdogger — Split count: D5=1 small, later=3 smalls
### 4.6 Gohma — Eye open/close cycle; arrow only during open ✓ mostly

### 4.7 Ganon (Z_04.asm:10284) — MAJOR REWORK
- [ ] Add intro: Scene Phase 0 (dark room + Triforce) → Phase 1 (brighten + song)
- [ ] Phase 2 fight: brown state counts DOWN to 0 → returns to invisible
- [ ] Silver Arrow must hit during brown/visible state
- [ ] Death sequence: burst rays (8 directions) → ashes → Triforce appears
- [ ] Teleport: Y=$A0, X=random($30 or $B0)

### 4.8 Patra — Destroy all 8 orbiters before core becomes vulnerable
### 4.9 Lanmola — Head-only vulnerability, segment chain follow
### 4.10 Moldorm — Tail-first destruction order

---

## PHASE 5: ITEM & WEAPON MECHANICS (MEDIUM-HIGH)
**Files**: ZeldaPlayer, Projectile, Item, Inventory

- [ ] Boomerang: picks up items on contact + curved return path
- [ ] Boomerang stun duration = 16 frames
- [ ] Bombs: max 2 active; damage during flash AND explode phases
- [ ] Rod: always 2 damage; Book adds fire only; add rod melee poke
- [ ] Food/Bait: attract Goriyas; implement Grumble room mechanic
- [ ] Fairy drops: add floating sine-wave movement
- [ ] Letter→Potion chain: potion downgrade (2nd→Life→empty)

---

## PHASE 6: DUNGEON SYSTEM POLISH (HIGH)
**Files**: ZeldaDungeon, DungeonRoom, DungeonData, DungeonRenderer

- [ ] Shutter door close/open animation + SFX on entry/clear
- [ ] Proper per-room wall collision (walls on all sides, 32px door openings)
- [ ] Old Man / Merchant rooms in dungeons (hint, shop, grumble, life-or-money, door-repair)
- [ ] Fixed enemy spawn positions (not random)
- [ ] Verify all 9 dungeon shapes against NES maps
- [ ] Block push SFX

---

## PHASE 7: OVERWORLD DATA (MEDIUM-HIGH)
**Files**: RoomData, ZeldaRoom, Overworld

- [ ] Define explicit enemy types+counts for ALL 128 rooms (remove biome fallback)
- [ ] Audit all secrets against NES (bomb walls, burn bushes, push rocks, push graves)
- [ ] Add missing raft routes (NES has 2)
- [ ] Recorder lake drain secret (reveals Level 7)
- [ ] Increase enemy counts to NES levels (4-6 per room)

---

## PHASE 8: CAVE SYSTEM (MEDIUM)
**Files**: Cave, CaveData

- [ ] Audit all 31 caves: text, items, prices, heart requirements
- [ ] White Sword: 5 hearts required; Magical Sword: 12 hearts
- [ ] Money game: NES permutation table (6 groups × 3 positions)
- [ ] Take-any rooms: mutual exclusivity ✓
- [ ] Door repair charge rooms
- [ ] Potion shop: only opens after letter delivery

---

## PHASE 9: HUD & INVENTORY SCREEN (MEDIUM)
**Files**: ZeldaHUD, InventoryScreen

- [ ] Overworld minimap: show explored rooms (currently only shows player dot)
- [ ] Dungeon minimap: room shapes should show door connections
- [ ] Inventory screen: proper item grid layout matching NES
- [ ] B-item selection cursor on inventory screen
- [ ] Triforce shard display on inventory screen
- [ ] NES font rendering (or close approximation)

---

## PHASE 10: GAME FLOW & TRANSITIONS (MEDIUM)
**Files**: ZeldaGame, TitleScreen

- [ ] Title screen: NES waterfall animation, demo playback, file select
- [ ] Name entry screen with NES character grid
- [ ] Dungeon room transition: scrolling (like overworld) or instant with fade
- [ ] Item pickup freeze-frame (Link holds item above head, text appears)
- [ ] Triforce collection ceremony (room brightens, music plays, fade to overworld)
- [ ] Death animation: Link spins, turns red, flashes, explodes into 4 pieces
- [ ] "GAME OVER" screen matches NES layout ✓ mostly
- [ ] Continue: respawn at overworld start or dungeon entrance ✓

---

## PHASE 11: AUDIO & SFX (MEDIUM)
**Files**: AudioManager, ZeldaGame SFX constants

Most SFX_* constants point to placeholder files. Need proper mapping:
- [ ] Sword slash SFX
- [ ] Sword beam fire SFX  
- [ ] Enemy hit SFX (different from kill)
- [ ] Enemy kill SFX (poof)
- [ ] Link hurt SFX
- [ ] Item pickup SFX (small vs big fanfare)
- [ ] Key unlock SFX
- [ ] Door open/shutter SFX
- [ ] Bomb place + explode SFX
- [ ] Boomerang throw + catch SFX
- [ ] Arrow fire SFX
- [ ] Low health beep (proper pitch)
- [ ] Boss hit cry
- [ ] Boss death cry
- [ ] Secret discovery jingle ✓
- [ ] Recorder melody ✓

---

## PHASE 12: SAVE SYSTEM (LOW)
**Files**: SaveManager, Inventory

- [ ] Persist per-room cleared flags across saves ✓
- [ ] Persist dungeon room cleared flags ✓
- [ ] Persist which secrets have been revealed
- [ ] On continue: reset health to 3 hearts ✓, reset position to start/dungeon entrance ✓
- [ ] 3 save slots on title screen ✓

---

## PHASE 13: SECOND QUEST (LOW — after 1st Quest is perfect)

- [ ] Alternate dungeon layouts (different shapes, room contents)
- [ ] Alternate overworld secret locations
- [ ] Harder enemy placement
- [ ] Different item locations in dungeons
- [ ] Triggered by: completing game OR entering "ZELDA" as name
- [ ] Flag exists in Inventory.secondQuest ✓ but no alternate data

---

## PHASE 14: VISUAL POLISH (LOW)
**Files**: NESPalette, OverworldRenderer, DungeonRenderer, all render() methods

- [ ] NES palette accuracy (4-color palette groups per sprite)
- [ ] Enemy death poof animation (cloud sprite, not just disappear)
- [ ] Spawn cloud animation when enemies first appear
- [ ] Screen flash on bomb explosion
- [ ] Water palette cycling ✓ (implemented)
- [ ] Dungeon palette per level ✓ (NESPalette has colors)
- [ ] Boss palette tint ✓ (implemented)
- [ ] Item flash/cycle animation ✓ (implemented for special items)

---

## IMPLEMENTATION ORDER (Recommended)

| Order | Phase | Impact | Effort |
|-------|-------|--------|--------|
| 1 | 1.1-1.2 HP/Damage tables | Critical | Medium |
| 2 | 3.1 Shooter enemies | Critical | Medium |
| 3 | 3.3 Wizzrobe rework | High | High |
| 4 | 4.7 Ganon rework | High | High |
| 5 | 2.1 Grid movement | High | High |
| 6 | 3.2-3.17 All enemy fixes | High | High |
| 7 | 4.2-4.10 Boss fixes | High | Medium |
| 8 | 6.1-6.6 Dungeon polish | High | Medium |
| 9 | 7.1-7.5 Overworld data | Medium | High |
| 10 | 5.1-5.7 Item mechanics | Medium | Medium |
| 11 | 8.1-8.3 Cave fixes | Medium | Low |
| 12 | 9 HUD/UI | Medium | Medium |
| 13 | 10 Game flow | Medium | Medium |
| 14 | 11 Audio | Medium | Medium |
| 15 | 14 Visual polish | Low | Medium |
| 16 | 12 Save system | Low | Low |
| 17 | 13 Second Quest | Low | Very High |

---

## KEY DISASSEMBLY REFERENCE FILES

| ASM File | Size | Contents |
|----------|------|----------|
| Z_00.asm | 25KB | Audio driver, sound tables |
| Z_01.asm | 165KB | Cave logic, collision, damage calc, knockback, shield deflection |
| Z_02.asm | 113KB | Drawing routines, PPU management |
| Z_04.asm | 296KB | ALL enemy Init/Update, boss logic, item drops, HP extraction |
| Z_05.asm | 232KB | Room layouts (OW+UW), doors, secrets, Link input, block push |
| Z_07.asm | 150KB | Game loop, object init/update dispatch, weapon updates, HP table |
