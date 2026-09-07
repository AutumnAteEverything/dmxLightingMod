# DMX Block

The DMX Block is a full-cube RGB surface designed for pixels, illuminated scenery, signs, and decorative arrays. It participates in the same fixture browser, grouping, universe, patch, and manual-output systems as regular fixtures.

## Controls

Right-click the block to set:

- name;
- universe;
- group;
- red, green, blue, dimmer, and strobe channels;
- manual RGB, dimmer, and strobe values;
- control mode;
- one of five visual skins;
- an optional skin-selection DMX channel.

Blank channel fields are unassigned.

## Emissive appearance

The colored skin is rendered full-bright so it remains visible in a dark room. The block always reports a Minecraft light level of zero, so it does not illuminate nearby blocks. A small amount of apparent edge color can still be produced by Minecraft rendering, shaders, bloom, or texture filtering; this is not world light emitted by the block.

## Strobe

Strobe value 0 disables the effect. Higher values increase the visible flashing rate, up to approximately 10 flashes per second at 255. Strobe affects the rendered block surface but does not change surrounding light because the block never emits world light.

## Skins

The manual selector cycles through Skin 1 to Skin 5. The corresponding resource locations are:

```text
assets/dmxlighting/textures/block/dmx_pixel_block1.png
assets/dmxlighting/textures/block/dmx_pixel_block2.png
assets/dmxlighting/textures/block/dmx_pixel_block3.png
assets/dmxlighting/textures/block/dmx_pixel_block4.png
assets/dmxlighting/textures/block/dmx_pixel_block5.png
```

`dmx_pixel_block.png` remains the base baked-block texture. See [Resource packs and textures](RESOURCE_PACKS.md) for customization.

## DMX skin selection

When the block is in DMX mode and the Skin DMX Channel is assigned, its value selects the rendered skin:

| DMX value | Skin |
| --- | --- |
| 0-51 | Skin 1 |
| 52-102 | Skin 2 |
| 103-153 | Skin 3 |
| 154-204 | Skin 4 |
| 205-255 | Skin 5 |

The selected manual skin remains stored. It is used when the skin channel is unassigned or when the block is in Manual mode.

## Example

Patch universe 1 as follows:

| Channel | Function |
| --- | --- |
| 101 | Red |
| 102 | Green |
| 103 | Blue |
| 104 | Dimmer |
| 105 | Skin |

Then send:

```mcfunction
/dmxsend 1 101 0 160 255 255 210
```

The block displays a bright blue-cyan color using Skin 5.
