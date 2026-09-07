# Command reference

Coordinates use Minecraft's normal block-position syntax, including relative coordinates such as `~ ~-1 ~`. Universes and channels are one-based; DMX values range from 0 to 255.

## Raw DMX output

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
