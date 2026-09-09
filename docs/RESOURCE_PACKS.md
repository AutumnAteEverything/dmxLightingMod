# Resource packs and textures

dmxLighting's appearance and generic music-disc audio can be changed with a normal Minecraft resource pack. Resource packs are optional; the mod includes working default textures and short example songs.

## Customizable files

| Resource-pack path | Purpose |
| --- | --- |
| `assets/dmxlighting/textures/block/dmx_pixel_block.png` | Base baked model for the DMX Block |
| `assets/dmxlighting/textures/block/dmx_pixel_block1.png` | DMX Block Skin 1 emissive overlay |
| `assets/dmxlighting/textures/block/dmx_pixel_block2.png` through `dmx_pixel_block16.png` | DMX Block Skins 2-16 emissive overlays |
| `assets/dmxlighting/textures/entity/dmx_fixture.png` | Fixture body texture |
| `assets/dmxlighting/textures/entity/dmx_fixture_lens.png` | PAR lens texture |
| `assets/dmxlighting/textures/entity/dmx_fixture_spotlight_lens.png` | Spotlight lens texture |
| `assets/dmxlighting/textures/item/lighting_console.png` | Lighting Console item texture |
| `assets/dmxlighting/sounds/music/generic_60_bpm.ogg` | Audio played by `generic60bpm` |
| `assets/dmxlighting/sounds/music/generic_70_bpm.ogg` | Audio played by `generic70bpm` |
| `assets/dmxlighting/sounds/music/generic_80_bpm.ogg` | Audio played by `generic80bpm` |
| `assets/dmxlighting/sounds/music/generic_90_bpm.ogg` | Audio played by `generic90bpm` |
| `assets/dmxlighting/sounds/music/generic_100_bpm.ogg` | Audio played by `generic100bpm` |
| `assets/dmxlighting/sounds/music/generic_110_bpm.ogg` | Audio played by `generic110bpm` |
| `assets/dmxlighting/sounds/music/generic_120_bpm.ogg` | Audio played by `generic120bpm` |
| `assets/dmxlighting/sounds/music/generic_130_bpm.ogg` | Audio played by `generic130bpm` |
| `assets/dmxlighting/sounds/music/generic_140_bpm.ogg` | Audio played by `generic140bpm` |

Files named `copy` in the source asset folder are not referenced by the mod and do not need to be included in a pack.

## Pack structure

```text
My dmxLighting Pack/
├── pack.mcmeta
└── assets/
    └── dmxlighting/
        ├── sounds/
        │   └── music/
        │       ├── generic_60_bpm.ogg
        │       ├── generic_70_bpm.ogg
        │       └── ... generic_140_bpm.ogg
        └── textures/
            ├── block/
            │   ├── dmx_pixel_block.png
            │   ├── dmx_pixel_block1.png
            │   ├── dmx_pixel_block2.png
            │   ├── ...
            │   └── dmx_pixel_block16.png
            ├── entity/
            │   ├── dmx_fixture.png
            │   ├── dmx_fixture_lens.png
            │   └── dmx_fixture_spotlight_lens.png
            └── item/
                └── lighting_console.png
```

The `pack_format` in `pack.mcmeta` must match the resource-pack version required by Minecraft 26.1. Minecraft will report an incompatible pack if that number is wrong.

## Editing guidelines

- Preserve each image's existing dimensions unless the renderer has been tested with a different size.
- Preserve transparency where it exists.
- Keep the exact filenames and lowercase path names.
- Save files as PNG.
- Reload packs with `F3+T` while testing.
- Test skins both in daylight and complete darkness.
- Test with shaders disabled first; bloom and shader lighting can change the emissive appearance.

## Replacing generic disc music

The ready-made template is in `resourcePack/dmxlighting-generic-music-template`. Replace any guide track while keeping its exact lowercase filename and folder. Use Ogg Vorbis audio; mono, 44.1 kHz files are recommended for jukebox-style positional playback.

The BPM is taken from the disc, not detected from the replacement audio. Trim unwanted silence before the first beat when precise synchronization matters. Music can contain an intro, but the light-show beat clock starts immediately when the disc begins.

The generic discs have a one-hour playback ceiling because the server cannot inspect audio supplied by a client resource pack. Eject the disc when a shorter custom track finishes. Every listener needs the same resource pack to hear the same song in multiplayer.

## Installing a pack

1. Put the resource-pack folder or ZIP in Minecraft's `resourcepacks` folder.
2. Open Options, then Resource Packs.
3. Enable the custom pack above the default resources.
4. Load a world containing dmxLighting blocks and inspect every fixture profile and skin.

Resource packs do not change BPM profiles, channel mappings, saved fixture data, commands, or control behavior.

Skin 16 currently duplicates the bundled Skin 15 artwork as a temporary placeholder. Resource packs can replace `dmx_pixel_block16.png` independently now, and a distinct default can be added without another code change.
