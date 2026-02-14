# Presentation Overhaul Plan — NES Zelda 1 Disassembly Analysis

Analysis of the zelda1-disassembly-master focused exclusively on **visual presentation**:
overworld layout, cave/dungeon layout & positions, graphics, animations, and screen transitions.

---

## 1. OVERWORLD MAP LAYOUT SYSTEM

### How NES Stores the 128-Room Overworld

The overworld is a **16×8 grid** (128 rooms, IDs $00–$7F). Each room is NOT stored as a flat
tile grid — it uses a **compressed column-based format**:

- **`RoomLayoutsOW` (binary data)**: Each room is defined as a sequence of **column descriptors**.
  Each column descriptor encodes: which column table (0–F) + which column index within that table.
  - Source: `Z_05.asm:4220` → `.INCBIN "dat/RoomLayoutsOW.dat"`

- **`ColumnDirectoryOW`** (Z_06.asm:460): 16 pointers to 16 column heap tables (0–F).
  Each heap contains the actual tile data for columns in that table.

- **`ColumnHeapOW0` through `ColumnHeapOWF`** (Z_05.asm:4235–4400): The raw column data.
  Each column is a run-length encoded sequence of tile squares.

- **`PrimarySquaresOW`** (Z_05.asm:5749): 64-entry table mapping square IDs to tile pattern IDs.
  Each "square" = a 2×2 group of 8×8 tiles (so each square is 16×16 pixels).

- **`SecondarySquaresOW`** (Z_05.asm:5758): The complementary tiles for each primary square
  (tile+1, tile+2, tile+3 positions within the 2×2 block).

### Room Attributes (6 attribute bytes per room × 128 rooms)

Stored in LevelBlock tables, loaded into RAM at game start:

| Table | RAM Address | Contents |
|-------|-------------|----------|
| `LevelBlockAttrsA` | $687E | Bits 7-4: exit X position. Bits 1-0: outer palette selector |
| `LevelBlockAttrsB` | $68FE | Bits 7-2: cave index (type $6A+). Bits 1-0: inner palette selector |
| `LevelBlockAttrsC` | $697E | Bits 5-0: monster list ID (low 6). Bits 7-4: exit X in cellars |
| `LevelBlockAttrsD` | $69FE | Bit 7: monster list ID bit 6. Other bits: various flags |
| `LevelBlockAttrsE` | $6A7E | Bits 6-5: ambient sound effect. Bit 7: dark room flag |
| `LevelBlockAttrsF` | $6AFE | Bits 2-0: secret trigger type. Bit 3: monsters from edges |

Source: `Variables.inc:325–330`, usage throughout `Z_05.asm`

### Palette Selection Per Room

Each overworld room selects from 4 palettes using 2 selectors:
- **Outer palette** (AttrsA bits 1-0) → controls the outer edge/border tiles
- **Inner palette** (AttrsB bits 1-0) → controls the inner ground/feature tiles
- Mapped via `RoomPaletteSelectorToNTAttr` (Z_05.asm:996)

### Key Implications for Java Project
- Current `RoomData.java` likely uses a simplified flat-grid approach
- NES rooms are 16 columns × 11 rows of 16×16px squares = 256×176 play area
- Status bar takes top 64px → total screen is 256×240
- Each room should reference a biome-style palette pair, not hardcoded colors

---

## 2. CAVE SYSTEM & LAYOUT

### Cave Types (ObjType values $6A–$7F)

Caves are entered from the overworld via `LevelBlockAttrsB` cave index.
The cave index is `((AttrsB >> 2) - $10)` (Z_05.asm:1977).

Cave types from Z_01.asm:
| Type | Description |
|------|-------------|
| $6A | Item shop (3 items for sale) |
| $6B | Old man with hint text |
| $6C | White Sword cave (5 hearts required) |
| $6D | Potion shop |
| $6E | Pay-to-play money game |
| $6F–$74 | Various hint caves, gambling |
| $75 | "Take any one you want" (choose 1 of 3 items) |
| $76–$7A | More hint/item caves |
| $7B+ | Moblin giving money ("IT'S A SECRET TO EVERYBODY") |

### Cave Layouts

3 cave subroom layouts defined (Z_05.asm:6035):
- **`RoomLayoutOWCave0`** (Z_05.asm:4223): Standard cave (open floor, fire on sides)
- **`RoomLayoutOWCave1`** (Z_05.asm:4227): Cave with additional features
- **`RoomLayoutOWCave2`** (Z_05.asm:4231): Special cave layout (arched)

### Cave Rendering

- Cave person drawn via `DrawCavePerson` (Z_01.asm:378)
- Person type < $7B = old man/woman/shopkeeper (drawn mirrored)
- Person type >= $7B = moblin (drawn not mirrored)
- Cave items positioned at X = $58, $78, $98 (3 positions, Z_01.asm:392)
- Cave palette: `CaveBgPaletteRowsTransferBuf` (Z_06.asm:724)

### Key Implications
- Current `Cave.java` / `CaveData.java` should define all ~20 cave types
- Each cave needs: person type, items offered, prices, text
- Cave visual layout is one of 3 templates, not custom per cave
- Item positions are fixed at 3 spots horizontally centered

---

## 3. DUNGEON LAYOUT & STRUCTURE

### LevelInfo Data (per dungeon, 9 total)

Each dungeon has a **LevelInfo** block loaded into RAM at $6B7E:

| Field | Offset | Description |
|-------|--------|-------------|
| `PalettesTransferBuf` | $6B7E | Background palette for this dungeon |
| `FoeCounts` | $6BA2 | Enemy count per room type (4 bytes) |
| `StartY` | $6BA6 | Link's starting Y position |
| `ShortcutOrItemPosArray` | $6BA7 | Item/shortcut positions (4 bytes) |
| `SubmenuMapRotation` | $6BAB | How many columns to rotate the minimap |
| `StatusBarMapXOffset` | $6BAC | X offset for the minimap on status bar |
| `StartRoomId` | $6BAD | Room ID where Link enters the dungeon |
| `TriforceRoomId` | $6BAE | Room ID containing the Triforce piece |
| `WorldFlagsAddr` | $6BAF | Address of this dungeon's world flags (2 bytes) |
| `LevelNumber` | $6BB1 | Dungeon number (1–9) |
| `CellarRoomIdArray` | $6BB2 | Array of room IDs that have cellars/stairs (10 bytes) |
| `BossRoomId` | $6BBC | Room ID of the boss |
| `SubmenuMapMask` | $6BBD | Bitmask for minimap shape (16 bytes) |
| `StatusBarMapTransferBuf` | $6BCD | Tile data for status bar minimap |
| `PaletteCycles` | $6BFA | Palette animation cycle data |
| `DeathPaletteSeries` | $6C5A | Palette used during death animation |

Source: `Variables.inc:331–346`, data files `dat/LevelInfoUW1.dat` – `dat/LevelInfoUW9.dat`

### Dungeon Room Layout System

- UW rooms use a **different column system** from OW
- **`ColumnDirectoryUW`** (Z_05.asm:5303): 10 pointers to UW column heaps (0–9)
- **`ColumnHeapUW0` through `ColumnHeapUW9`**: Column tile data for dungeons
- **`PrimarySquaresUW`** (Z_05.asm:5315): 8-entry table: `$B0,$74,$94,$B4,$70,$68,$F4,$24`
- **`RoomLayoutsUW`**: Binary data for all dungeon room shapes

### Dungeon Room Types (UniqueRoomId)

`GetUniqueRoomId` (Z_05.asm:5318+) computes a **unique room layout index** from
the room ID and level block attributes. Room layout $21 = entrance room.

### Dungeon Palettes

Each dungeon has its own palette defined in `LevelInfo_PalettesTransferBuf`.
Boss rooms use separate palette overrides:
- `AquamentusPaletteRow7TransferBuf` (Z_06.asm:742): `$0F,$0A,$29,$30`
- `OrangeBossPaletteRow7TransferBuf` (Z_06.asm:745): `$0F,$17,$27,$30`
- `GanonPaletteRow7TransferBuf` (Z_06.asm:692): `$0F,$16,$2C,$3C`
- `GhostPaletteRow7TransferBuf` (Z_06.asm:715): `$0F,$30,$00,$12`
- `GleeokPaletteRow7TransferBuf` (Z_06.asm:739): `$0F,$2A,$1A,$0C`
- `RedArmosPaletteRow7TransferBuf` (Z_06.asm:736): `$0F,$0F,$1C,$16`

### Dungeon Minimap

- Shape defined by `SubmenuMapMask` — 16 bytes, each byte is a bitmask for one row
- Map rotation (`SubmenuMapRotation`) shifts the map horizontally on the submenu
- Each room the player visits is marked in the minimap via room flags

### Second Quest

- Q2 uses **replacement tables** (Z_06.asm:271–377) that patch LevelInfo and LevelBlockAttrs
- Different dungeon shapes, item placements, and room configurations
- 8 replacement offsets for AttrsB + individual patching of AttrsA/D/F

### Key Implications
- `DungeonData.java` should store per-dungeon: palette, start room, boss room, triforce room,
  cellar rooms, minimap mask, and map rotation
- Room shapes should use a lookup table (currently hardcoded?)
- Each dungeon has a distinct color palette — not all gray
- Dark rooms need explicit tracking per `LevelBlockAttrsE` bit 7

---

## 4. GRAPHICS / PATTERN (CHR) SYSTEM

### Pattern Block Architecture

The NES uses **CHR pattern tables** for both background tiles and sprites.
Zelda 1 has separate pattern blocks for OW and UW:

**Overworld patterns** (Z_03.asm:41–43):
- `PatternBlockOWBG` → PPU $1700 (background tiles: ground, trees, rocks, water, etc.)
- `PatternBlockOWSP` → PPU $08E0 (sprite tiles: enemies, items, Link)

**Underworld patterns** (Z_03.asm:37–39):
- `PatternBlockUWBG` → PPU $1700 (background: walls, doors, floors, blocks)
- `PatternBlockUWSP` → PPU $08E0 (base sprites)

**Per-dungeon sprite extensions** (Z_03.asm:13–23):
- Levels 1,2,7 share `PatternBlockUWSP127` → PPU $09E0
- Levels 3,5,8 share `PatternBlockUWSP358` → PPU $09E0
- Levels 4,6,9 share `PatternBlockUWSP469` → PPU $09E0

**Boss sprite patterns** (Z_03.asm:25–35):
- Levels 1,2,5,7 share `PatternBlockUWSPBoss1257` → PPU $0C00
- Levels 3,4,6,8 share `PatternBlockUWSPBoss3468` → PPU $0C00
- Level 9 has unique `PatternBlockUWSPBoss9` → PPU $0C00

**Common patterns** (Z_02.asm:148–155):
- `CommonSpritePatterns` → PPU $0000 (7 pages, $700 bytes)
- `CommonBackgroundPatterns` → PPU $1000 (7 pages, $700 bytes)
- `CommonMiscPatterns` → PPU $1F20 ($E0 bytes)

### Key Implications
- The Java project uses individual GIF/PNG sprites — this is fine for Java
- But the NES shares sprite tiles between enemy types within a level group
- Dungeons 1-2-7, 3-5-8, and 4-6-9 share enemy sprite sheets
- Boss sprites are also grouped: 1-2-5-7, 3-4-6-8, 9

---

## 5. ANIMATION SYSTEM

### Object Animation Architecture

The NES animation system uses heap-based lookup tables:

- **`ObjAnimFrameHeap`** (Z_01.asm:4951): Maps animation index + frame number → tile IDs
  for left and right halves of each sprite.

- **`ObjAnimAttrHeap`** (Z_01.asm:4979): Maps animation index → sprite palette row (0–3).
  Values like $00=palette 4, $01=palette 5, $02=palette 6, $03=palette 7,
  $8x = horizontally flipped.

- **`ObjAnimCounter`** ($3D0): Per-object frame timer. When it expires, `ObjAnimFrame` toggles.

- **`ObjAnimFrame`** ($3E4): Current animation frame (0 or 1) per object.

### Link's Animation

- Link uses **sprite offsets $48 (left) and $4C (right)** — hardcoded in DrawObjectWithAnim
- Walking animation: 2 frames (legs apart / legs together), toggled by `ObjAnimCounter`
- Direction determines which tile pair to use from the frame heap
- Link has 2 animation indexes (most enemies have 1, starting at type+1)
- Horizontal flipping controlled by `$0F` register

### Enemy Animation

- Each enemy type maps to an animation index = `ObjType + 1`
- `DrawObjectWithType` / `DrawObjectWithAnim` look up tiles from the heap
- Mirroring (`DrawObjectMirrored`) draws left half then right half flipped
- Not mirrored (`DrawObjectNotMirrored`) draws both halves independently
- Half-width objects skip the second sprite

### Item Animation

- **`Anim_ItemFrameOffsets`** (Z_01.asm:5244): Maps item slot → offset in ItemFrameTiles
- **`Anim_ItemFrameTiles`** (Z_01.asm:5251): Tile IDs for each item's sprite frames
- Items flash by cycling palette rows based on FrameCounter

### Key Implications
- Current Java sprites use pre-rendered GIFs — animation is mostly frame-swapping
- NES uses 2-frame walk cycles for all characters (toggle every N frames)
- Palette cycling creates flash/glow effects without changing sprites
- Enemy sprites are drawn as 2 halves (16×16 = two 8×16 tiles)

---

## 6. SCREEN TRANSITIONS

### Mode 7: Room-to-Room Scrolling

The NES scrolls between rooms using 8 submodes (Z_05.asm:723–1205):

| Submode | Action |
|---------|--------|
| 0 | Reset scroll offsets, check for pond/flute animation |
| 1 | Draw Link between rooms, set up opened doors, lay out next room |
| 2 | Lay out next room tiles to NT2, transfer column-by-column |
| 3-4 | Transfer play area attributes (top half, then bottom half) to NT2 |
| 5 | Check dark room transitions, begin fade if needed |
| 6 | Animate world fading (dark↔light transition) |
| End | Set current room = next room, switch to play mode |

### Scroll Update (Mode 7 Update Submodes, Z_05.asm:1043–1205):

| Submode | Action |
|---------|--------|
| 0 | Reset scroll registers, fill play area attributes |
| 1 | Transfer attributes to NT0 |
| 2 | Calculate VScroll start frame for smooth scrolling |
| 3 | **ScrollWorld** + copy column/row to tile buffer (the actual pixel scrolling) |
| 4-5 | Transfer new room attributes to NT0 |
| 6 | Handle dark room brighten/darken |
| 7 | Finish: set mode to play, reset submodes |

### Scroll Mechanics

- Horizontal scroll: `CurHScroll` ($FD) applied to `PpuScroll_2005`
- Vertical scroll: `CurVScroll` ($FC) applied to `PpuScroll_2005`
- NES uses **dual nametable** trick: current room in NT0, next room loads into NT1
- **Sprite 0 hit** used to split the screen between status bar and play area
- Columns/rows are transferred one at a time during scroll for smooth animation

### Mode 4/6: Enter/Leave Room

- Used for entering caves, dungeons, and special transitions
- Handles brightening (entering lit room from dark) and darkening
- `AnimateWorldFading` does smooth palette fade

### Mode 10: Stairway Transition
- Used when entering/exiting stairs within dungeons
- Special spiral stairway animation

### Mode 11: Death Animation
- Link spins and changes color using `Mode11DeadLinkPalette`
- Background fades to `Mode11BackgroundPaletteBottomHalfTransferBuf`
- Play area attributes set to $FF (all one palette)

### Mode 12: End Level / Get Triforce
- Triforce lifting animation with `DrawLinkLiftingItem`
- Screen flashes white, then fades

### Key Implications
- Current Java project likely does instant room transitions
- NES scrolls 1 pixel per frame (takes ~256 frames for horizontal, ~176 for vertical)
- The smooth scroll reveals the next room gradually
- Dark rooms need a fade-in effect when lit by candle
- Death animation = Link spinning + palette fade to red/brown
- Stairway transitions have their own animation sequence

---

## 7. PALETTE SYSTEM

### NES Palette Architecture

NES has 4 background palettes and 4 sprite palettes (4 colors each, first always $0F=black):

**Background palettes** (rows 0–3): Used for tiles/terrain
**Sprite palettes** (rows 4–7): Used for characters/objects

### Level Palettes

Each dungeon/overworld level has its own palette set in `LevelInfo_PalettesTransferBuf`.
This is a PPU transfer record that writes to $3F00 (palette RAM).

### Palette Cycling

`LevelInfo_PaletteCycles` (Variables.inc:345) defines how palettes animate over time.
This creates effects like:
- Water shimmering
- Lava pulsing  
- Dungeon wall color shifts

### Boss-Specific Palettes (sprite palette row 7)

Written to PPU $3F1C (last sprite palette):
- Aquamentus: dark green ($0A), blue ($29), white ($30)
- Orange bosses (Dodongo, Manhandla): brown ($17), orange ($27), white ($30)
- Ganon: dark red ($16), blue ($2C), light blue ($3C)
- Gleeok: green ($2A), dark green ($1A), dark blue ($0C)
- Ghost (Pols Voice?): white ($30), gray ($00), blue ($12)

### Special Palettes

- **Title screen**: Golden/brown theme (Z_02.asm:449)
- **Story scroll**: Grayscale + greens (Z_02.asm:495)
- **Menu**: Blue/cyan theme (Z_06.asm:446)
- **Death**: Brown/red (Z_06.asm:831)
- **Cave**: Gray stone (Z_06.asm:724)
- **Cellar**: All palette $AA (Z_06.asm:728)
- **Ending**: Green + gold (Z_06.asm:695)

### Key Implications
- Each dungeon should have a distinct color theme, not generic gray
- Boss rooms should shift to boss-specific palette
- Water/lava should animate via palette cycling (color shifts, not tile animation)
- Death should fade to brown/red, not just show "Game Over"
- The overworld uses per-room palette pairs (inner + outer selectors)

---

## 8. HUD / STATUS BAR

### Status Bar Layout

Defined by `StatusBarStaticsTransferBuf` (Z_06.asm:761):
- Top 64 pixels of screen (rows 0-7 of nametable)
- Contains: rupee count, key count, bomb count, B-item icon, A-item (sword),
  life meter (hearts), minimap, level number

### Minimap (Overworld)

- Player position tracked by `UpdatePlayerPositionMarker`
- Blinking dot shows current room on 16×8 grid
- Map data from `LevelInfo_StatusBarMapTransferBuf`

### Minimap (Dungeon)

- Shape defined by `SubmenuMapMask` (16 bytes of bitmasks)
- Rooms visited shown as filled squares
- Compass enables blinking triforce room indicator
- Map item reveals all rooms

### Submenu (Pause Screen)

Multiple transfer buffers for the inventory screen:
- `InventoryTextTransferBuf`: "INVENTORY" header
- `SubmenuBoxesTopsTransferBuf` / `SubmenuBoxesSidesTransferBuf`: item box borders
- `SelectedItemBoxBottomTransferBuf`: selected B-item highlight
- `UseBButtonTextTransferBuf`: "USE B BUTTON" text
- Triforce display with glowing animation (`TriforceGlowingColors`)

---

## 9. DUNGEON ENTRANCE POSITIONS ON OVERWORLD

### How Dungeon Entrances Work

Dungeon entrances are **caves** on the overworld. The cave index in `LevelBlockAttrsB`
determines what happens when Link enters. If the cave type corresponds to a dungeon entrance,
`Z_05.asm:7394` stores the source room ID and sets target mode to 2 (load level).

The actual **overworld room** that contains each dungeon entrance is determined by the
`LevelBlockAttrsB` cave index for that room. The dungeon number is encoded in the
cave data.

### Dungeon Entry Position

When entering a dungeon:
- `LevelInfo_StartRoomId` defines which room Link starts in
- `LevelInfo_StartY` defines Link's Y position (X is centered at $78)
- Entry direction is always facing up (entering from bottom of room)

### Cave Exit Position  

When exiting underground back to overworld:
- `LevelBlockAttrsA` bits 7-4: X coordinate of exit position
- `LevelBlockAttrsF` bits 2-0: square row (Y position) of exit
- `CaveSourceRoomId` remembers which overworld room to return to

---

## 10. SUMMARY: WHAT NEEDS WORK IN THE JAVA PROJECT

### High Priority (Visual Impact)

1. **Per-dungeon color palettes** — each dungeon should have a unique color theme
2. **Smooth room scrolling** — gradual pixel-by-pixel scroll between rooms
3. **Dark room system** — rooms start black, candle lights them with fade-in
4. **Death animation** — Link spinning + palette fade, not instant game over
5. **Overworld palette variety** — rooms should use different palette pairs for biome variety

### Medium Priority

6. **Cave visual system** — 3 cave templates with proper NPC positioning
7. **Boss palette overrides** — each boss type gets its own color scheme
8. **Minimap accuracy** — per-dungeon shape masks, rotation, visited room tracking
9. **Palette cycling** — water shimmer, lava pulse, dungeon wall animation
10. **2-frame walk animation** — all characters toggle between 2 frames

### Lower Priority (Polish)

11. **Title screen** — proper golden palette with waterfall animation
12. **Story scroll** — opening text crawl with triforce glow
13. **Triforce room** — lifting animation with screen flash
14. **Stairway transitions** — spiral stair animation between dungeon floors
15. **Submenu inventory** — proper box layout with item grid and triforce display
