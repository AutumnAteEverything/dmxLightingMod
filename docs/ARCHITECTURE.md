# Architecture

dmxLighting is split into common/server-authoritative state and client-only presentation.

## Runtime flow

```text
/dmx, /dmxsend, or in-game console output
                     │
                     ▼
            DMX universe/state update
                     │
                     ▼
       fixture parameter map + control mode
                     │
                     ▼
           resolved underlying output
                     │
      playing jukebox│ optional temporary overlay
                     ▼
     effective fixture output and block-entity sync
          │                         │
          ▼                         ▼
 client renderer/UI          Minecraft light updates
```

The server owns saved fixture configuration and authoritative output. Client screens send custom payloads to request changes; validated server handlers update block entities and synchronize render data back to clients.

## Main components

| Area | Key classes | Responsibility |
| --- | --- | --- |
| Initialization | `DmxLighting`, `DmxLightingClient` | Registers blocks, items, block entities, commands, payloads, screens, and renderers |
| DMX values | `DmxUniverse`, `DmxUniverseManager` | Stores in-game universe/channel values |
| Automatic DMX | `AutomaticDmxShowManager`, `JukeboxBlockEntityMixin`, `AutomaticDmxCommand` | Tracks jukebox playback and builds non-destructive beat-driven output |
| Fixture state | `DmxFixtureBlockEntity`, `FixtureState`, `FixtureOutput` | Persists configuration and resolves DMX/manual output |
| Parameter patch | `FixtureParameterMap` | Maps each supported parameter to an absolute channel or unassigned state |
| Identity/grouping | `FixtureIdentity`, `FixtureGroupName`, `DmxFixtureRegistry` | Names, groups, profiles, and loaded-fixture discovery |
| Fixture types | `DmxFixtureProfileRegistry`, `RgbParProfile`, `StaticSpotProfile` | Selects supported visual fixture models and compatibility aliases |
| Pixel block | `DmxPixelBlock`, `DmxPixelBlockEntity` | RGB/dimmer/strobe surface, five skins, and DMX skin selection |
| Console | `LightingConsole`, `LightingConsoleScreen`, console payload handlers | Browser summaries, patch analysis, and targeted output |
| Beams/light | `FixtureBeamDirection`, `FixtureBeamSettings`, `FixtureBeamLightManager`, client renderers | Beam geometry, orientation, sampling, and light behavior |
| Commands | `DmxCommand`, `DmxSendCommand`, `DmxBulkAddressCommand`, `DmxControlCommand` | Raw output, fixture setup, diagnostics, and bulk patching |
| Networking | `DmxNetworking` and payload records | Client/server validation and synchronization |

## Persistence

Fixture block entities save identity, universe, explicit parameter channels, control mode, manual values, beam/mount configuration, interpolation settings, and other render state. DMX Blocks add selected skin, skin channel, and rendered skin state.

Because state is stored in the world, changes to persistence keys need migration/default handling. Never assume a new key exists in an older world.

## Parameter-centric patching

The current fixture editor does not rely on one fixed base address. Every parameter stores its own absolute channel. This permits custom layouts and unused parameters, but it also means conflict analysis must inspect each assignment individually.

`/dmxaddress` is a convenience exception: it assigns sequential RGBD groups of four channels to fixtures in a region.

## Control modes

DMX and manual values coexist. The selected fixture mode decides which source is active. Beam Width and Beam Length also have independent source choices while the fixture remains in DMX mode. Pan/tilt interpolation and visible color interpolation operate after source resolution so they can smooth rendered target changes without changing the underlying DMX/manual values.

Automatic DMX is a transient layer above that source resolution. Its block-entity output is synchronized to clients but omitted from world saves, and it never writes to `DmxUniverseManager` or the stored manual output.

## Networking boundary

The custom payload system is Minecraft client/server networking, not a DMX transport protocol. Adding real-world DMX input would require a new receiver/bridge layer that validates and writes incoming values into `DmxUniverseManager`, plus lifecycle, threading, configuration, and security work.
