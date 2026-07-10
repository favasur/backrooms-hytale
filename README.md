# Wrong Yellow — A Backrooms-Inspired Hytale Mod Pack

[![Hytale](https://img.shields.io/badge/Hytale-0.5.3–0.6.0-blue)](https://hytale.com)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://adoptium.net)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

> *"A harsh white light panel that buzzes with fluorescent energy..."*

**Wrong Yellow** is a creepy, backrooms-themed mod pack for Hytale. It adds unsettling decorative blocks, looping ambient sounds, and a flickering light plugin that brings abandoned-building atmospherics to your worlds.

---

## 📦 Included Content

### 🧱 Decorative Blocks

| Block | Texture | Sound | Description |
|:---|---|---|---|
| 🟡 Light Yellow Wool | `backrooms wallpaper.png` | — | The classic yellow wallpaper — familiar and wrong |
| 🟡 Yellow Wool | `backrooms wallpaper2.png` | — | A darker, more worn variant |
| 🟫 Beige Carpet | `Block_Beige_Carpet.png` | — | Nostalgic beige carpet |
| 🔵 Blue Poolroom Tiles | `blue poolroom tiles.png` | — | Endless poolroom tiles |
| ⚪ White Poolroom Tiles | `white poolroom tiles.png` | — | Pristine infinity tiles |
| ⬜ Light Gray Poolroom Tiles | `light gray poolrom tiles.png` | — | Hums with fluorescent light |
| 🗿 Caveman Cutout | `caveman cutout.png` | 🌐 Voyager Golden Record greetings (looping) | A whispering primitive figure |
| 🛑 Stop Sign | `stop sign.png` | — | A red stop sign out of place |
| 💡 White Build Lightsource | `Block_White_Build_Lightsource.png` | 🔇 Backrooms light buzz (looping) | **Flickers** via the Java plugin! |

### 🔌 Server Plugin — Flickering Lights

The **Wrong Yellow Flicker Plugin** (`com.favasur.wrongyellow.FlickerPlugin`) adds dynamic flickering to the White Build Lightsource block:

- **Random flicker bursts** every 2–5 seconds
- Rapidly cycles between **Full → Dimmed → Off → Dimmed → Full**
- Each block has staggered timings so they never sync up
- Three block variants: `Block_White_Build_Lightsource`, `_Dimmed`, `_Off`
- Plugin automatically discovers placed lightsource blocks and tracks them

---

## 📁 Project Structure

```
wrong-yellow-0.5.6/
├── pack.json                  # Hytale asset pack manifest
├── build.gradle.kts           # Gradle build (Kotlin DSL)
├── settings.gradle.kts        # Gradle settings
├── gradle.properties          # Plugin configuration
├── gradlew / gradlew.bat      # Gradle wrapper
├── gradle/wrapper/            # Gradle wrapper files
│
├── Server/
│   ├── Item/Items/            # Block/item JSON definitions
│   ├── soundevent/            # Sound event registrations
│   └── Languages/en-US/       # Translation strings
│
├── Common/
│   ├── BlockTextures/         # Block texture PNGs
│   ├── Icons/ItemsGenerated/  # Inventory icon PNGs
│   └── Sounds/                # OGG sound files
│
├── src/main/
│   ├── java/com/favasur/wrongyellow/
│   │   ├── FlickerPlugin.java            # Plugin entry point
│   │   └── FlickeringTickingSystem.java  # Flicker logic (ECS system)
│   └── resources/manifest.json           # Plugin manifest
│
└── Plugins/                   # Deploy target for compiled .jar
```

---

## 🚀 Installation

### For Players (Using the Mod)

You need **both** the plugin `.jar` AND the asset pack files:

1. **Plugin jar:** Copy `build/libs/WrongYellow_FlickerPlugin-1.0.0.jar` to your Hytale server's `Mods/` folder
2. **Asset pack:** Copy the entire `Server/` and `Common/` directories alongside your `pack.json` to the Hytale mods directory

> ⚠️ The jar alone is not enough — the block definitions, textures, sounds, and translations are in the asset pack files.

### For Developers (Building from Source)

**Prerequisites:**
- Java 25 JDK (Eclipse Adoptium or Microsoft OpenJDK)
- A Hytale server installation (for the SDK)

**Steps:**

```bash
# 1. Clone the repo
git clone https://github.com/favasur/wrong-yellow.git
cd wrong-yellow

# 2. Set up Hytale development environment
#    (requires Hytale OAuth authentication)
./gradlew.bat setupHytaleDev

# 3. Build the plugin
./gradlew.bat build

# 4. The compiled jar is at:
#    build/libs/WrongYellow_FlickerPlugin-1.0.0.jar
```

**If you have a local Hytale installation**, set `hytaleHomeOverride` in `gradle.properties`:
```properties
hytaleHomeOverride = C:/path/to/Hytale/Assets.zip
```

Then just run:
```bash
./gradlew.bat build
```

---

## 🎮 Usage

### Blocks
All blocks can be found in the Creative Menu under their respective categories (Decoration, Materials, Stone, Organic). Place them like any other block.

### Flickering Lights
1. Place a **White Build Lightsource** block
2. The plugin automatically discovers it (may take a few seconds)
3. It will emit a looping buzz sound and flicker at random intervals
4. The `AmbientSoundEventId: "Backrooms_Light_Buzz"` plays constantly
5. The plugin swaps between three block variants for the visual flicker effect

### Caveman Cutout Sound
The Caveman Cutout loops the Voyager Golden Record greetings in 55 languages via `AmbientSoundEventId: "Voyager_Golden_Record_Greetings"`.

---

## 🛠️ Sound Events

| Event ID | File | Looping | Volume |
|---|---|---|---|
| `Voyager_Golden_Record_Greetings` | `Sounds/Voyager_Golden_Record_-_Greetings_In_55_Languages.ogg` | ✅ | 1.5 |
| `Backrooms_Light_Buzz` | `Sounds/backrooms-light-buzz.ogg` | ✅ | 2.0 |

Sound events are defined in `Server/soundevent/` JSON files and registered with standard Hytale attenuation profiles (`SFX_Attn_Moderate`, `SFX_Attn_Loud`).

---

## 🧪 Technical Details

- **Plugin Type:** Java plugin for Hytale Server API
- **Base Class:** `com.hypixel.hytale.server.core.plugin.JavaPlugin`
- **ECS System:** `TickingSystem<ChunkStore>` with per-block state tracking via `Long2IntOpenHashMap`
- **Block Swapping:** `BlockAccessor.setBlock(x, y, z, blockId)`
- **Block Discovery:** Round-robin chunk scanning (2 chunks/tick, 16 Y-slices each)
- **Build System:** Gradle 9.5.1 with `com.azuredoom.hytale-tools` plugin
- **Server Compatibility:** Hytale `>=0.5.3 <0.6.0`

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

*Built with 💡 by FavaSur*
