# Resource packs and textures

dmxLighting's appearance can be changed with a normal Minecraft resource pack. Resource packs are optional; the mod includes working default textures.

## Customizable files

| Resource-pack path | Purpose |
| --- | --- |
| `assets/dmxlighting/textures/block/dmx_pixel_block.png` | Base baked model for the DMX Block |
| `assets/dmxlighting/textures/block/dmx_pixel_block1.png` | DMX Block Skin 1 emissive overlay |
| `assets/dmxlighting/textures/block/dmx_pixel_block2.png` | DMX Block Skin 2 emissive overlay |
| `assets/dmxlighting/textures/block/dmx_pixel_block3.png` | DMX Block Skin 3 emissive overlay |
| `assets/dmxlighting/textures/block/dmx_pixel_block4.png` | DMX Block Skin 4 emissive overlay |
| `assets/dmxlighting/textures/block/dmx_pixel_block5.png` | DMX Block Skin 5 emissive overlay |
| `assets/dmxlighting/textures/entity/dmx_fixture.png` | Fixture body texture |
| `assets/dmxlighting/textures/entity/dmx_fixture_lens.png` | PAR lens texture |
| `assets/dmxlighting/textures/entity/dmx_fixture_spotlight_lens.png` | Spotlight lens texture |
| `assets/dmxlighting/textures/item/lighting_console.png` | Lighting Console item texture |

Files named `copy` in the source asset folder are not referenced by the mod and do not need to be included in a pack.

## Pack structure

```text
My dmxLighting Pack/
├── pack.mcmeta
└── assets/
    └── dmxlighting/
        └── textures/
            ├── block/
            │   ├── dmx_pixel_block.png
            │   ├── dmx_pixel_block1.png
            │   ├── dmx_pixel_block2.png
            │   ├── dmx_pixel_block3.png
            │   ├── dmx_pixel_block4.png
            │   └── dmx_pixel_block5.png
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

## Installing a pack

1. Put the resource-pack folder or ZIP in Minecraft's `resourcepacks` folder.
2. Open Options, then Resource Packs.
3. Enable the custom pack above the default resources.
4. Load a world containing dmxLighting blocks and inspect every fixture profile and skin.

Resource packs only replace visuals. They do not change channel mappings, saved fixture data, commands, or control behavior.
