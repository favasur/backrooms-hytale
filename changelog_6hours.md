# Changelog — Last 6 Hours (July 10, 2026)

All commits pushed to `origin/master` on https://github.com/favasur/backrooms-hytale.git

---

## Latest — `c231933` (tag: `v0.1.12-0.5.6`) — Fix halfblock vertical wall placement

### Halfblock fix
- **Block_Wool_Yellow_Slab.json** → **Block_Wool_Yellow_Half.json** rename
- **Block_Wool_Yellow_Light_Slab.json** → **Block_Wool_Yellow_Light_Half.json** rename
  - Renamed files from `_Slab` to `_Half` for consistency
  - Updated translation keys (`items.block_wool_yellow_*_slab.*` → `_half.*`) and display names
  - Added missing `PlacementSettings.RotationMode: "StairFacingPlayer"` to both halfblock files
  - This was accidentally removed in commit `58875f9` when `VariantRotation` was changed to `UpDownNESW`
  - Without it, halfblocks couldn't be placed vertically against walls
  - Combined with `VariantRotation: "UpDownNESW"` and `HitboxType: "Block_Half"`, halfblocks now orient correctly on both floors and walls

---

## `acbd176` — Refactor flickering onto vanilla block with proper OFF variant

### Repo rename
- GitHub repository renamed from `wrong-yellow` → `backrooms-hytale`
- Local root folder renamed to match
- All URLs and references updated in README, changelog
- Git remote URL updated to `https://github.com/favasur/backrooms-hytale.git`

### Big refactor: Custom blocks → Vanilla override
- Removed all 3 custom `Block_White_Build_Lightsource` variants (ON/Flickering/Off)
- Created vanilla `Build_Lightsource_White` override at `Build/Lightsource/` with:
  - `AmbientSoundEventId: "Backrooms_Light_Buzz"` for looping buzz sound
  - `Light.Radius: 15` for actual world light emission
- Created `Block_Lightsource_White_Off` as OFF variant (same vanilla texture, no light, no sound)
- Flickering now works on the **vanilla** lightsource block — no custom block needed

### Flickering timing rebalance
- Flicker burst chance: **50% → 12%** (much rarer)
- Rest period between bursts: **0.5-1s → 2-6s**
- Initial delay before first flicker: **0.25-1s → 1.5-4.5s**
- Rapid cycling during bursts kept the same (1-4 ticks per state)
- `BLOCK_OFF` changed from `Build_Black_Cube` → `Block_Lightsource_White_Off`

### Sound fixes
- Sound events restored at `Server/Audio/SoundEvents/` (correct path the game scans)
- Removed old test path `Server/soundevent/`
- Added `AudioCategory: AudioCat_Ambient` back to both sound events
- Changed `Volume` from `0` back to `0.0` (matching original working format)
- Lightsource and Caveman Cutout now emit their looping ambient sounds correctly

### Texture fixes
- Panel_2Tall and Panel_3Tall blockymodels:
  - `doubleSided: true → false` (single layer rendering)
  - Back face offset matches front face offset (was using different atlas region)
  - Right/left face offsets changed to `x=0` (solid color, prevents texture bleeding)

---

## `2c7a211` — Fix Caveman/Stop Sign collision

- **Block_Caveman_Cutout.json**, **Block_Stop_Sign.json**
  - Removed `Material: Solid` field entirely (following base game plant block pattern)
  - Changed `HitboxType` from `None` -> `Plant_Tall_Full` (valid 2-block-tall plant hitbox)
  - Removes collision by mimicking how plant blocks handle it

---

## `b65d4d0` — Major rework: Flickering, sound, panels

### New flickering variant block
- Created Block_White_Build_Lightsource_Flickering.json with buzz sound
- Deleted Block_White_Build_Lightsource_Dimmed.json (ON/OFF only now)

### Rewritten flickering logic
- FlickeringTickingSystem.java: ON/OFF only, rapid cycling, 50% flicker start chance
- Buzz sound flickers naturally with block swaps

### Double-layer texture fix
- Panel_2Tall.blockymodel, Panel_3Tall.blockymodel: doubleSided false

### Sound fix
- Block_White_Build_Lightsource.json: restored buzz sound on FULL variant only

---

## `d14580a` — Texture, placement & sound fixes

- Block_Caveman_Cutout: fixed texture proportions (StretchUV)
- Wool slabs: SlabFacingPlayer rotation mode
- Block_Stop_Sign: Stone -> Metal sound

---

## `2dfa5c0` — Validation error fixes

- Fixed Block_Stop_Sign RotationMode error
- Removed unused Flammable/Friction/Hardness from all 15 blocks

---

## `350bbd9` — Sound ID fix

- All blocks: ItemSoundSetId ISS_Items_Stone -> ISS_Blocks_Stone

---

## `f1c37b8` — Icons, models & missing fields

- Added PNG icons for all 15 blocks
- Created blockymodel files for slabs/stairs
- Added missing JSON fields

---

## `01a14cb` & `f31ad1b` — Audio fixes

- Moved sound events to Server/Audio/SoundEvents/
- Converted audio to mono (Common/Sounds/)
- Created SFX_Attn_Loud parent event

---

## `0ebbbe5` — Ambient sound restore

- Restored AmbientSoundEventId on lightsource and caveman blocks

---

## `3e2e310` — Validation error fixes

- Corrected sound/particle IDs
- Removed invalid hitbox types

---

## `5469211` — Mod ID rename

- wrongyellow_flicker -> wrongyellow

---

## `8d44333` — Initial asset validation fixes

- Removed invalid HitboxType: FullBlock
- Fixed SlabFacingPlayer rotation
- Restored vanilla blockymodel references
- Added Opacity, State, ConnectedBlockRuleSet to slabs/stairs
