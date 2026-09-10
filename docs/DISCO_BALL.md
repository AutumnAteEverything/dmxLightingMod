# DMX Disco Ball

The DMX Disco Ball is a cubic, three-channel fixture with two selectable visual effects. Its mount remains stationary while the mirrored cube and its projection pattern rotate together.

**Beams** draws twenty narrow rainbow beams: eight form a horizontal ring, eight form a steeper staggered ring toward the open side of the mount, and four form a shallower ring. There is no straight vertical beam.

**Dots** hides the beam volumes and projects scattered full-bright colored squares onto collidable floors, walls, and ceilings within 24 blocks. The spots follow the spinning mirror pattern but do not emit Minecraft world light.

## Controls

Right-click the block, or select **Edit** from its Lighting Console row, to set:

- name, group, and universe;
- Dimmer channel;
- Spin Rate channel;
- Effect Mode channel;
- initial rotation angle;
- base position at the top or bottom;
- Beams or Dots effect mode;
- DMX or Manual mode;
- manual dimmer and spin values.

The default patch is Universe 1, Dimmer 1, Spin Rate 2, and Effect Mode 3.

## Effect mapping

| DMX value | Projection |
| --- | --- |
| 0-127 | Beams |
| 128-255 | Dots |

The Beams and Dots buttons store the fallback used in Manual mode or when the Effect Mode channel is blank. In DMX mode, an assigned Effect Mode channel takes control. Existing balls from an earlier build keep their saved selection until you assign this channel.

## Spin mapping

| DMX value | Motion |
| --- | --- |
| 0 | Maximum reverse |
| 1-127 | Reverse, slowing toward stop |
| 128 | Stopped |
| 129-254 | Forward, speeding up |
| 255 | Maximum forward |

Changing the initial angle resets the cube to that position. The top/bottom selection moves the stationary base and stem and points the angled beam rings toward the open side of the mount.

During Automatic DMX playback, the ball uses a slower movement wave than ordinary fixtures. It spins in each direction for about 24 beats before easing through stop and reversing.

## Quick test

Place a ball at your feet:

```mcfunction
/setblock ~ ~ ~ dmxlighting:dmx_disco_ball
```

Full brightness, stopped:

```mcfunction
/dmxsend 1 1 255 128 0
```

Full brightness, maximum forward spin:

```mcfunction
/dmxsend 1 1 255 255 0
```

Full brightness, maximum reverse spin:

```mcfunction
/dmxsend 1 1 255 0 0
```

Switch to Dots while preserving brightness and spin:

```mcfunction
/dmx 1 3 255
```

Blackout while preserving the current spin-rate value:

```mcfunction
/dmx 1 1 0
```

The ball participates in jukebox and `/dmxPulse` Automatic DMX shows. Automatic output is temporary and does not overwrite its underlying DMX or manual settings.
