# dmxLighting

dmxLighting is a Fabric mod for building and operating theatrical-style lighting in Minecraft. It provides configurable fixture blocks, emissive RGB blocks, a handheld lighting console, manual controls, an in-game DMX universe, fixture groups, patch inspection, visible beams, optional smooth pan/tilt movement and color fades, jukebox-driven automatic light shows, and externally timed beat pulses.

> [!IMPORTANT]
> The current release simulates DMX inside Minecraft. It does **not** yet receive Art-Net, sACN, USB-DMX, or data from a physical lighting console.
>
> dmxLighting is primarily intended for use with the [Minecraft the Musical](https://github.com/AutumnAteEverything/minecraftTheMusical) Max for Live devices, allowing lighting to be controlled from Ableton Live.

## Features

- Configurable **DMX Fixture** with PAR and Spotlight visual models.
- Explicit channel assignment for red, green, blue, white, amber, dimmer, pan, tilt, beam width, beam length, and strobe.
- Manual and DMX operating modes, with optional visible color fades.
- Optional pan/tilt interpolation with a user-defined movement time.
- Optional RGB color fade time for fixtures, DMX Blocks, and DMX mobs.
- Automatic jukebox shows with beat-synchronized color, dimmer, and fixture movement.
- `/dmxPulse` instant-party shows driven one beat at a time by commands.
- Nine generic music discs from 60 to 140 BPM for resource-pack music.
- Mount orientation controls separate from live pan/tilt offsets.
- Colored lenses, visible fixture beams, and Minecraft light output.
- Full-cube **DMX Block** with RGB, dimmer, strobe, and five selectable skins.
- Optional DMX channel for selecting the DMX Block skin.
- Emissive DMX Block surfaces that remain visible in darkness while emitting no Minecraft block light.
- Summonable **DMX Block Displays** with the same emissive skins and DMX controls, plus vanilla display transformations including negative scale.
- **DMX Parrot**, **DMX Enderman**, **DMX Warden**, and **DMX Nautilus** mobs with RGB/dimmer patching, color fades, and emissive DMX color rendering.
- Handheld, right-docked **Lighting Console** with Fixtures, Groups, Universes, Patch, and Output views.
- Live output to one fixture, one group, or all loaded fixtures.
- Creative inventory registration and search terms for the mod items.
- Commands for raw channel output, consecutive channel output, fixture configuration, inspection, and bulk addressing.
- Persistent fixture configuration in saved worlds.

## Requirements

| Component | Version |
| --- | --- |
| Minecraft | 26.1 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | A Minecraft 26.1-compatible build; developed with 0.145.1+26.1 |
| Java | 25 or newer |

For multiplayer, install the mod and Fabric API on both the server and every connecting client. Single-player uses Minecraft's integrated server, so a normal client installation is sufficient.

## Installation

1. Install Fabric Loader for Minecraft 26.1.
2. Install Fabric API for Minecraft 26.1.
3. Download the regular dmxLighting JAR, not the sources JAR.
4. Place dmxLighting and Fabric API in the Minecraft `mods` folder.
5. Start Minecraft with the Fabric profile.

No configuration file is required for a first run.

Automatic DMX is enabled by default. Insert a music disc into a jukebox to make loaded DMX fixtures and mobs in that dimension follow its beat. The show is a temporary visual layer and does not overwrite DMX or manual values.

For an externally controlled show, send `/dmxPulse` once per beat. The temporary pulse show ends three seconds after the final command, returning every fixture and mob to its underlying output.

## Quick start

1. Open Creative inventory and search for `DMX`.
2. Place a **DMX Fixture**.
3. Right-click it to open the fixture editor. Right-clicking still opens the editor when another block is held and does not place that held block.
4. Give the fixture a name, choose a universe, and assign absolute channels to the parameters you want to use. A blank channel field is unassigned.
5. Set the fixture to **DMX** mode.
6. Send values from chat. This example writes red, green, blue, and dimmer to universe 1, channels 101-104:

   ```mcfunction
   /dmxsend 1 101 255 64 0 255
   ```

7. Hold the **Lighting Console** and right-click to browse and operate loaded fixtures.

For a DMX Block patched to channels 101-105 as red, green, blue, dimmer, and skin:

```mcfunction
/dmxsend 1 101 255 0 0 255 128
```

That makes the block bright red and selects skin 3.

Summon a transformable DMX Block Display with RGB, dimmer, and strobe on channels 101-105:

```mcfunction
/summon dmxlighting:dmx_block_display ~ ~ ~ {dmx_fixture_name:"Display 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104,dmx_parameter_strobe:105}
```

It appears in the Lighting Console and supports Minecraft's normal display-entity `transformation` data. See [DMX Blocks](docs/DMX_BLOCK.md) for an inside-out example.

## Items and blocks

| Name | Registry ID | Creative tab | Purpose |
| --- | --- | --- | --- |
| DMX Fixture | `dmxlighting:dmx_block` | Functional Blocks | Configurable PAR/Spotlight fixture with movement, beam, and light controls |
| DMX Block | `dmxlighting:dmx_pixel_block` | Functional Blocks | Emissive RGB surface with dimmer, strobe, and five skins; emits no world light |
| DMX Block Display | `dmxlighting:dmx_block_display` | Summon command | Transformable emissive DMX Block visual with console registration and no world light |
| Lighting Console | `dmxlighting:lighting_console` | Tools & Utilities | Portable browser, patch view, and live control surface |
| DMX Parrot Spawn Egg | `dmxlighting:dmx_parrot_spawn_egg` | Tools & Utilities | Spawns a DMX-addressable parrot mob that appears in the console |
| DMX Enderman Spawn Egg | `dmxlighting:dmx_enderman_spawn_egg` | Tools & Utilities | Spawns a DMX-addressable Enderman mob that appears in the console |
| DMX Warden Spawn Egg | `dmxlighting:dmx_warden_spawn_egg` | Tools & Utilities | Spawns a DMX-addressable Warden mob that appears in the console |
| DMX Nautilus Spawn Egg | `dmxlighting:dmx_nautilus_spawn_egg` | Tools & Utilities | Spawns a persistent, DMX-addressable Nautilus that appears in the console |
| Generic BPM Discs | `dmxlighting:generic_60_bpm` through `generic_140_bpm` | Tools & Utilities | Resource-pack music slots with fixed Automatic DMX tempos |

See the detailed guides:

- [Getting started](docs/GETTING_STARTED.md)
- [DMX Fixtures](docs/FIXTURES.md)
- [DMX Blocks](docs/DMX_BLOCK.md)
- [Lighting Console](docs/LIGHTING_CONSOLE.md)
- [Automatic DMX and jukebox shows](docs/AUTOMATIC_DMX.md)
- [Commands](docs/COMMANDS.md)
- [Resource packs and textures](docs/RESOURCE_PACKS.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Development](docs/DEVELOPMENT.md)
- [Architecture](docs/ARCHITECTURE.md)

## Building from source

macOS and Linux:

```bash
./gradlew build
```

Windows:

```powershell
gradlew.bat build
```

The distributable JAR is written to `build/libs/`. The file whose name contains `sources` is for development and is not the mod JAR to install.

To launch a development client:

```bash
./gradlew runClient
```

More setup and release information is in [Development](docs/DEVELOPMENT.md).

## Project status

The mod is under active development and its save format, UI, and commands may change. Back up important worlds before upgrading. See [CHANGELOG.md](CHANGELOG.md) for release notes and known limitations.

## AI-assisted development disclosure

dmxLighting was developed with substantial assistance from OpenAI's GPT-5.6 through Codex. GPT-5.6 was used to generate and revise code, documentation, and implementation ideas. The project's direction, requirements, testing, and final decisions were provided by the project maintainer.

## Contributing

Bug reports, documentation corrections, ideas, and pull requests are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before contributing.

## License

This project is released under [CC0 1.0 Universal](LICENSE).
