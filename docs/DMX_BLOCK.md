# DMX Block

The DMX Block is a full-cube RGB surface designed for pixels, illuminated scenery, signs, and decorative arrays. It participates in the same fixture browser, grouping, universe, patch, and manual-output systems as regular fixtures.

## Controls

Right-click the block to set:

- name;
- universe;
- group;
- red, green, blue, dimmer, and strobe channels;
- manual RGB, dimmer, and strobe values;
- optional visible color fade time;
- control mode;
- one of sixteen visual skins;
- an optional skin-selection DMX channel.

Blank channel fields are unassigned.

## Emissive appearance

The colored skin is rendered full-bright so it remains visible in a dark room. The block always reports a Minecraft light level of zero, so it does not illuminate nearby blocks. A small amount of apparent edge color can still be produced by Minecraft rendering, shaders, bloom, or texture filtering; this is not world light emitted by the block.

The **Fade** control eases visible RGB changes over 0.05 to 60 seconds. It affects the rendered color only; strobe still flashes on/off sharply and the block still emits no Minecraft block light.

## Strobe

Strobe value 0 disables the effect. Higher values increase the visible flashing rate, up to approximately 10 flashes per second at 255. Strobe affects the rendered block surface but does not change surrounding light because the block never emits world light.

## Skins

The manual selector cycles through Skin 1 to Skin 16. The corresponding resource locations are:

```text
assets/dmxlighting/textures/block/dmx_pixel_block1.png
assets/dmxlighting/textures/block/dmx_pixel_block2.png
...
assets/dmxlighting/textures/block/dmx_pixel_block16.png
```

The supplied white design is Skin 16. `dmx_pixel_block.png` remains the base baked-block texture. See [Resource packs and textures](RESOURCE_PACKS.md) for customization.

During a jukebox Automatic DMX show, each DMX Block and DMX Block Display independently changes to a temporary skin on one randomly selected beat in every four-beat phrase. Ejecting the disc immediately restores the configured or DMX-controlled skin. Command-driven `/dmxPulse` shows do not change skins.

## DMX skin selection

When the block is in DMX mode and the Skin DMX Channel is assigned, its value selects the rendered skin:

| DMX value | Skin |
| --- | --- |
| 0-15 | Skin 1 |
| 16-31 | Skin 2 |
| 32-47 | Skin 3 |
| 48-63 | Skin 4 |
| 64-79 | Skin 5 |
| 80-95 | Skin 6 |
| 96-111 | Skin 7 |
| 112-127 | Skin 8 |
| 128-143 | Skin 9 |
| 144-159 | Skin 10 |
| 160-175 | Skin 11 |
| 176-191 | Skin 12 |
| 192-207 | Skin 13 |
| 208-223 | Skin 14 |
| 224-239 | Skin 15 |
| 240-255 | Skin 16 |

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
/dmxsend 1 101 0 160 255 255 255
```

The block displays a bright blue-cyan color using Skin 16.

## DMX Block Displays

`dmxlighting:dmx_block_display` is a summonable visual version of the DMX Block. It appears in the Lighting Console and supports RGB, dimmer, strobe, skins, an optional skin channel, groups, and color fades. It is DMX-only and emits no Minecraft world light.

The entity also retains Minecraft's normal display controls, including translation, scale, left and right rotation, billboard mode, interpolation, view range, shadows, width, and height. Those properties can be supplied in the summon command or changed later with `/data merge entity`.

Basic summon using channels 101-105 for red, green, blue, dimmer, and strobe:

```mcfunction
/summon dmxlighting:dmx_block_display ~ ~ ~ {dmx_fixture_name:"Display 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104,dmx_parameter_strobe:105}
```

This example makes a large inside-out cube centered on its summon point. The negative scale is useful for surrounding-room and optical-illusion effects:

```mcfunction
/summon dmxlighting:dmx_block_display ~ ~ ~ {dmx_fixture_name:"Inside Out",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104,transformation:{translation:[4.0f,4.0f,4.0f],scale:[-8.0f,-8.0f,-8.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]},width:8.0f,height:8.0f,view_range:4.0f}
```

The DMX editor is opened from the display's row in the Lighting Console. Transform settings remain command-driven so the entity stays compatible with vanilla display-building tools and command generators.

DMX Block Displays have no spawn egg. Remove one with `/kill` or target it with normal entity selectors, for example `@e[type=dmxlighting:dmx_block_display,limit=1,sort=nearest]`.
