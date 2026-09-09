# Command reference

Coordinates use Minecraft's normal block-position syntax, including relative coordinates such as `~ ~-1 ~`. Universes and channels are one-based; DMX values range from 0 to 255.

## Automatic DMX

Automatic DMX is enabled by default and follows music discs playing in jukeboxes.

```mcfunction
/automaticdmx status
/automaticdmx on
/automaticdmx off
```

`status` lists active jukebox shows and their BPM in the current dimension. Turning Automatic DMX off immediately returns fixtures and mobs to their underlying DMX or manual output. The command does not clear or rewrite DMX channels.

Built-in discs use individual starting BPM profiles. Custom music discs use a 120 BPM fallback in this version. See [Automatic DMX](AUTOMATIC_DMX.md) for behavior and testing notes.

## Command-pulse shows

Send one command on every beat to run an instant beat-driven show in the current dimension:

```mcfunction
/dmxPulse
```

The beat command intentionally sends no chat response, so repeated pulses do not fill the chat. Fixtures and mobs use the same color, dimmer, and movement pattern as a jukebox show. The interval between pulses is measured to keep movement smooth, but the mod never creates additional beats after the incoming pulses stop.

```mcfunction
/dmxPulse status
/dmxPulse stop
```

`status` reports the received pulse count, estimated BPM, and remaining timeout. `stop` ends the command-pulse show immediately. Otherwise it times out three seconds after the final pulse. The show is a temporary output layer: it does not rewrite DMX values, and the previous DMX/manual output becomes visible again when the show ends. If a jukebox show is active, the command pulses temporarily take priority and the jukebox resumes after the timeout.

Command-pulse shows remain available when `/automaticdmx off` has disabled jukebox shows. The lowercase `/dmxpulse` spelling is also accepted.

## Raw DMX output

### DMX Disco Ball quick test

The DMX Disco Ball defaults to Dimmer on channel 1 and bipolar Spin Rate on channel 2:

```mcfunction
/setblock ~ ~ ~ dmxlighting:dmx_disco_ball
/dmxsend 1 1 255 128
/dmxsend 1 1 255 255
/dmxsend 1 1 255 0
```

Spin Rate `128` is stopped, lower values spin in reverse, and higher values spin forward. Use the fixture's editor to change channels, initial angle, or top/bottom mounting.

### `/dmx`

Sets one channel in the in-game universe.

```mcfunction
/dmx <universe> <channel> <value>
```

Example:

```mcfunction
/dmx 1 104 255
```

### `/dmxsend`

Sets any number of consecutive channels, beginning at `startChannel`.

```mcfunction
/dmxsend <universe> <startChannel> <value1> [value2 ...]
```

Example:

```mcfunction
/dmxsend 1 101 255 0 0 255 128
```

This writes values to channels 101-105. Every value is validated before anything is changed, and the command fails if the list would extend past channel 512. Existing four-value RGBD uses remain valid because four is simply one possible list length.

## Fixture configuration

### Set universe (legacy shortcut)

```mcfunction
/dmxfixture configure <position> <universe>
```

This sets the fixture's universe. It does not assign parameter channels. The command remains as a shorter, older equivalent of the explicit `set ... universe` form below.

### Set universe

```mcfunction
/dmxfixture set <position> universe <universe>
```

### Assign a parameter channel

```mcfunction
/dmxfixture set <position> channel <parameter> <channel>
```

Supported parameter names:

```text
red green blue white amber dimmer pan tilt beam_width beam_length strobe
```

Aliases include `beamwidth`/`width`, `beamlength`/`length`, and `dim`/`intensity`. Use channel `0` to unassign a parameter.

Examples:

```mcfunction
/dmxfixture set 10 64 -20 channel pan 107
/dmxfixture set 10 64 -20 channel beam_width 109
/dmxfixture set 10 64 -20 channel amber 0
```

### Set visual type

```mcfunction
/dmxfixture set <position> type <fixtureType>
```

Current fixture choices are `rgb_par` and `static_spot`.

### Rename a fixture

```mcfunction
/dmxfixture set <position> name <fixture name>
```

The name may contain spaces because it consumes the rest of the command.

## DMX mob summon

### Summon a DMX mob

DMX mobs can be spawned with their universe and RGB/dimmer channels already assigned.

Parrot example:

```mcfunction
/summon dmxlighting:dmx_parrot ~ ~ ~ {dmx_fixture_name:"Stage Parrot 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104}
```

Enderman example:

```mcfunction
/summon dmxlighting:dmx_enderman ~ ~ ~ {dmx_fixture_name:"Stage Enderman 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104}
```

Warden example:

```mcfunction
/summon dmxlighting:dmx_warden ~ ~ ~ {dmx_fixture_name:"Stage Warden 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104}
```

Nautilus example:

```mcfunction
/summon dmxlighting:dmx_nautilus ~ ~ ~ {dmx_fixture_name:"Water Wash 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104}
```

Creaking example:

```mcfunction
/summon dmxlighting:dmx_creaking ~ ~ ~ {dmx_fixture_name:"Stage Creaking 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104}
```

Axolotl example:

```mcfunction
/summon dmxlighting:dmx_axolotl ~ ~ ~ {dmx_fixture_name:"Pool Axolotl 1",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104}
```

To place a DMX mob in a console group at summon time:

```mcfunction
/summon dmxlighting:dmx_parrot ~ ~ ~ {dmx_fixture_name:"Bird Wash 1",dmx_group_name:"Birds",dmx_group_key:"birds",dmx_universe:1,dmx_parameter_red:201,dmx_parameter_green:202,dmx_parameter_blue:203,dmx_parameter_dimmer:204}
```

```mcfunction
/summon dmxlighting:dmx_enderman ~ ~ ~ {dmx_fixture_name:"Ender Wash 1",dmx_group_name:"Endermen",dmx_group_key:"endermen",dmx_universe:1,dmx_parameter_red:211,dmx_parameter_green:212,dmx_parameter_blue:213,dmx_parameter_dimmer:214}
```

```mcfunction
/summon dmxlighting:dmx_warden ~ ~ ~ {dmx_fixture_name:"Warden Wash 1",dmx_group_name:"Wardens",dmx_group_key:"wardens",dmx_universe:1,dmx_parameter_red:221,dmx_parameter_green:222,dmx_parameter_blue:223,dmx_parameter_dimmer:224}
```

```mcfunction
/summon dmxlighting:dmx_nautilus ~ ~ ~ {dmx_fixture_name:"Pool Wash 1",dmx_group_name:"Nautiluses",dmx_group_key:"nautiluses",dmx_universe:1,dmx_parameter_red:231,dmx_parameter_green:232,dmx_parameter_blue:233,dmx_parameter_dimmer:234}
```

Important saved-data fields:

```text
dmx_universe
dmx_parameter_red
dmx_parameter_green
dmx_parameter_blue
dmx_parameter_dimmer
```

Optional but useful fields:

```text
dmx_fixture_name
dmx_group_name
dmx_group_key
dmx_color_interpolation_enabled
dmx_color_interpolation_time_seconds
```

For example, this spawns a grouped Warden whose visible color fades over two seconds:

```mcfunction
/summon dmxlighting:dmx_warden ~ ~ ~ {dmx_fixture_name:"Warden Fade 1",dmx_group_name:"Wardens",dmx_group_key:"wardens",dmx_universe:1,dmx_parameter_red:221,dmx_parameter_green:222,dmx_parameter_blue:223,dmx_parameter_dimmer:224,dmx_color_interpolation_enabled:1,dmx_color_interpolation_time_seconds:2.0f}
```

Names do not have to be unique. If a DMX mob has no saved name, the console gives it a generated `dmxParrot`, `dmxEnderman`, `dmxWarden`, `dmxNautilus`, `dmxCreaking`, or `dmxAxolotl` label using part of its UUID. Unique names are still recommended when several DMX mobs are in the same show file or group.

DMX mobs are DMX-only for now. The Lighting Console Output view writes to their assigned DMX channels instead of putting individual mobs into Manual mode.

DMX Wardens retain normal Minecraft Warden behavior and aggression.

DMX Nautiluses retain normal Nautilus swimming, taming, riding, saddle, armor, and breeding behavior. They are marked persistent so an unattended show Nautilus does not despawn.

DMX Creakings are standalone and do not require a Creaking Heart. They retain vanilla freezing and player aggression, but are normally damageable because they are not heart-bound.

DMX Axolotls retain normal swimming, land movement, variants, combat, and bucket interaction. A normal Axolotl bucket releases a vanilla Axolotl, so use the DMX spawn egg or summon command when DMX identity must be preserved.

## DMX Block Display summon

DMX Block Displays are DMX-only visual entities that register with the Lighting Console. Their default patch is universe 1, channels 1-5 for red, green, blue, dimmer, and strobe.

```mcfunction
/summon dmxlighting:dmx_block_display ~ ~ ~ {dmx_fixture_name:"Display 1",dmx_group_name:"Video Wall",dmx_group_key:"video wall",dmx_universe:1,dmx_parameter_red:101,dmx_parameter_green:102,dmx_parameter_blue:103,dmx_parameter_dimmer:104,dmx_parameter_strobe:105,dmx_display_skin:2,dmx_display_skin_channel:106}
```

They accept the normal display entity tags, including `transformation`, `interpolation_duration`, `start_interpolation`, `teleport_duration`, `billboard`, `brightness`, `view_range`, `shadow_radius`, `shadow_strength`, `width`, and `height`. See [DMX Blocks](DMX_BLOCK.md) for a negative-scale example.

## Control mode and manual output

Set a fixture to DMX or Manual mode:

```mcfunction
/dmxfixture mode <position> <dmx|manual>
```

Set stored manual RGB and dimmer values:

```mcfunction
/dmxfixture manual <position> <red> <green> <blue> <dimmer>
```

Example:

```mcfunction
/dmxfixture manual ~ ~-1 ~ 255 80 0 200
```

## Inspection and diagnostics

Show a fixture's identity, universe, profile, mode, parameter patch, current DMX values, visible RGB, and Minecraft light level:

```mcfunction
/dmxfixture inspect <position>
```

Show calculated beam direction, origin, sample positions, and related beam diagnostics:

```mcfunction
/dmxfixture beamdebug <position>
```

`beamdebug` is intended for development and troubleshooting rather than normal show operation.

## Bulk addressing

```mcfunction
/dmxaddress <from> <to> <universe> <startChannel>
```

The command finds DMX fixtures in the inclusive rectangular region and assigns sequential four-channel RGBD patches:

```text
Fixture 1: Red=start, Green=start+1, Blue=start+2, Dimmer=start+3
Fixture 2: Red=start+4, Green=start+5, Blue=start+6, Dimmer=start+7
```

White, amber, pan, tilt, beam width, beam length, and strobe are left unassigned. The entire operation is rejected if it would exceed channel 512.

Example:

```mcfunction
/dmxaddress 0 64 0 9 64 0 1 1
```

This addresses every fixture between `(0, 64, 0)` and `(9, 64, 0)` in universe 1, beginning at channel 1.

## Notes

- Commands affect the current running world and are not an Art-Net or sACN input.
- Minecraft's command permissions and server settings determine who may execute commands.
- A fixture responds only when its selected universe and parameter map point to the channels being written.
