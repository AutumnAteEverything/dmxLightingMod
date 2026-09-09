# dmxLighting Resource Pack Template

This resource pack contains every PNG texture currently referenced by
dmxLighting. It targets Minecraft 26.1 and requires the dmxLighting mod.

## Installation

1. Edit the PNG files while preserving their filenames and folders.
2. Put the ZIP file in Minecraft's `resourcepacks` folder.
3. Enable the pack in Minecraft's Resource Packs screen.

The ZIP must contain `pack.mcmeta` at its top level. Do not add an extra
folder around the files when repackaging it.

## DMX Block textures

- `textures/block/dmx_pixel_block.png` is the inventory icon and base block
  texture.
- `textures/block/dmx_pixel_block1.png` through
  `dmx_pixel_block16.png` are the sixteen selectable placed-block skins.

The supplied white design is included as Skin 16. The base-named
`dmx_pixel_block.png` remains the inventory icon and base block texture.

The selectable skins are drawn as full-bright surfaces and multiplied by the
block's current DMX color and dimmer value. White pixels reproduce the DMX
color most accurately, gray pixels make it darker, and black pixels remain
black. Alpha transparency is supported, but transparent areas reveal the base
block texture underneath.

## Fixture textures

- `textures/entity/dmx_fixture.png` is the shared fixture body texture.
- `textures/entity/dmx_fixture_lens.png` is the PAR fixture lens.
- `textures/entity/dmx_fixture_spotlight_lens.png` is the spotlight lens.

The lens textures receive the fixture's live output color. White and grayscale
artwork works best when you want clean RGB tinting.

## Lighting Console

- `textures/item/lighting_console.png` controls the handheld Lighting Console
  artwork.

## Image guidelines

- Keep each image as a PNG.
- Keeping the original canvas dimensions is safest.
- Preserve transparency where needed.
- Restart Minecraft or reload resources after replacing textures.
- Add your preferred license and creator credits before publishing your edited
  pack.
