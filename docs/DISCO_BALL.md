# DMX Disco Ball

The DMX Disco Ball is a cubic, two-channel fixture. Its mount remains stationary while the mirrored cube and seventeen narrow beams rotate together. Eight beams form a horizontal ring, eight more angle outward between them toward the open side of the mount, and one points straight away from the mount. The beams cycle through a fixed rainbow palette.

## Controls

Right-click the block, or select **Edit** from its Lighting Console row, to set:

- name, group, and universe;
- Dimmer channel;
- Spin Rate channel;
- initial rotation angle;
- base position at the top or bottom;
- DMX or Manual mode;
- manual dimmer and spin values.

The default patch is Universe 1, Dimmer 1, and Spin Rate 2.

## Spin mapping

| DMX value | Motion |
| --- | --- |
| 0 | Maximum reverse |
| 1-127 | Reverse, slowing toward stop |
| 128 | Stopped |
| 129-254 | Forward, speeding up |
| 255 | Maximum forward |

Changing the initial angle resets the cube to that position. The top/bottom selection moves only the stationary base and stem; the beam pointing through the mount is omitted.

During Automatic DMX playback, the ball uses a slower movement wave than ordinary fixtures. It spins in each direction for about 24 beats before easing through stop and reversing.

## Quick test

Place a ball at your feet:

```mcfunction
/setblock ~ ~ ~ dmxlighting:dmx_disco_ball
```

Full brightness, stopped:

```mcfunction
/dmxsend 1 1 255 128
```

Full brightness, maximum forward spin:

```mcfunction
/dmxsend 1 1 255 255
```

Full brightness, maximum reverse spin:

```mcfunction
/dmxsend 1 1 255 0
```

Blackout while preserving the current spin-rate value:

```mcfunction
/dmx 1 1 0
```

The ball participates in jukebox and `/dmxPulse` Automatic DMX shows. Automatic output is temporary and does not overwrite its underlying DMX or manual settings.
