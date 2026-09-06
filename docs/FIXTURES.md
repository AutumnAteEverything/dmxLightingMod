# DMX Fixtures

The DMX Fixture is a configurable theatrical light. The selected profile controls its physical model; its output is patched parameter by parameter using absolute DMX channels.

## Fixture editor

Right-click a placed fixture to open the editor. Opening the UI consumes the interaction, so a block held in the player's hand is not placed against the fixture.

### Identity and patch

- **Name** identifies the fixture in the console.
- **Group** lets several fixtures be selected together. Group matching ignores capitalization and repeated spaces.
- **Profile** selects the PAR or Spotlight model.
- **Universe** selects the in-game universe.
- Each **DMX Channel** field assigns an absolute channel from 1-512. Blank means unassigned.

The fixture supports these independently assignable parameters:

| Parameter | Purpose | Neutral/default note |
| --- | --- | --- |
| Red | Red color component | 0-255 |
| Green | Green color component | 0-255 |
| Blue | Blue color component | 0-255 |
| White | Adds white | 0-255 |
| Amber | Adds warm amber | 0-255 |
| Dimmer | Master intensity | 0 is off; 255 is full |
| Pan Offset | Horizontal movement relative to mount | 128 is the installed position |
| Tilt Offset | Vertical movement relative to mount | 128 is the installed position |
| Beam Width | Visual cone width | 0-255 |
| Beam Length | Visible beam reach | 0-255 |
| Strobe | Pulsed output | 0 disables strobe |

The patch is parameter-centric: channels do not need to be consecutive and the same visual profile can use different patch layouts on different fixtures.

## Profiles

| UI profile | Internal ID | Appearance |
| --- | --- | --- |
| PAR | `rgb_par` | Compact PAR-style body and lens |
| Spotlight | `static_spot` | Traditional long-barrel theatrical spotlight |

Older saved profile IDs are translated for compatibility: `rgbd` and `rgbw_par` resolve to PAR, while `moving_head_spot` resolves to Spotlight. The DMX Block's internal `dmx_pixel` profile is not intended as a fixture-editor choice.

Both fixture models use the current parameter-centric controls. The profile is primarily a visual choice rather than a fixed channel footprint.

## DMX and manual modes

- In **DMX** mode, assigned parameters read from the selected universe.
- In **Manual** mode, the fixture uses the values stored by its editor, the console, or `/dmxfixture manual`.
- Switching modes does not erase either set of values.

While the whole fixture is in DMX mode, Beam Width and Beam Length can each select either DMX or their stored manual value. Whole-fixture Manual mode always uses the complete manual output.

## Mount orientation and movement

Mount Pan and Mount Tilt describe how the fixture was installed. Pan Offset and Tilt Offset describe live movement relative to that installed direction. Keeping these concepts separate makes it possible to mount fixtures in different orientations while controlling them consistently.

For live pan and tilt, DMX value 128 represents zero offset. Values below and above 128 move in opposite directions.

### Pan/tilt interpolation

The **Smooth** control enables optional pan/tilt interpolation. The adjacent time field specifies how long, from 0.05 to 60 seconds, a move should take. With smoothing off, pan and tilt jump immediately to their new values.

If a show requires exact snap changes, leave Smooth off. For slower moving-light motion, enable it and choose a positive duration.

## Color fades

The **Color Fade** control enables optional visible RGB fading. Its time field uses the same 0.05 to 60 second range as pan/tilt smoothing. It fades the rendered color after DMX/manual source selection, so red, green, blue, white, amber, and dimmer changes can ease visually while the console still shows the target values.

Strobe, beam width, beam length, pan, and tilt timing are not changed by Color Fade.

## Beams and light

The fixture renderer draws a colored beam from the lens. Beam Width and Beam Length control its shape, while color, dimmer, and strobe affect its appearance.

The fixture can also produce Minecraft block light based on its output. This is separate from the visible beam geometry: a beam is a visual direction and volume, while Minecraft's light engine illuminates blocks discretely.

## Example patch

A fixture using universe 1, channels 101-111 might be patched as:

| Channel | Parameter |
| --- | --- |
| 101 | Red |
| 102 | Green |
| 103 | Blue |
| 104 | White |
| 105 | Amber |
| 106 | Dimmer |
| 107 | Pan Offset |
| 108 | Tilt Offset |
| 109 | Beam Width |
| 110 | Beam Length |
| 111 | Strobe |

Send a complete look with:

```mcfunction
/dmxsend 1 101 255 120 0 0 40 255 128 128 110 220 0
```

See [Commands](COMMANDS.md) for configuration and inspection commands.
