# Automatic DMX

Automatic DMX turns a playing jukebox into a simple synchronized light show. It is enabled by default.

## What it controls

While a record is playing, every loaded DMX fixture, DMX Block, DMX Block Display, DMX Parrot, DMX Enderman, DMX Warden, DMX Nautilus, DMX Creaking, and DMX Axolotl in the same dimension follows the record's beat.

- All DMX fixtures and mobs receive saturated color changes every two beats.
- Brightness pulses on every beat, with a stronger pulse at the start of each four-beat show phrase.
- Fixture blocks receive smooth pan and tilt sweeps that begin from their underlying pre-show pan and tilt positions.
- Each DMX Block and DMX Block Display independently changes skin on one randomly selected beat per four-beat phrase.
- DMX Blocks, DMX Block Displays, and DMX mobs keep their normal physical behavior and use only the color and brightness part of the show.

If more than one jukebox is playing, each fixture or mob follows the nearest active jukebox in its dimension.

## Command-pulse shows

`/dmxPulse` starts the same style of beat-driven show without requiring a jukebox. Send the command once for every beat supplied by an external program, command block, or player:

```mcfunction
/dmxPulse
```

Each pulse changes the show on that beat. The mod measures the interval between received pulses to smooth fixture movement, but it does not invent beats after the commands stop. Three seconds after the last pulse, the show times out and all fixtures and mobs return to their underlying output. Use `/dmxPulse stop` to end it immediately or `/dmxPulse status` to inspect its pulse count, estimated BPM, and timeout.

Random DMX Block skin changes are jukebox-only. `/dmxPulse` leaves block and display skins under their normal configured or DMX-channel control.

A command-pulse show takes temporary priority over a jukebox show in the same dimension. If the jukebox is still playing when command pulses time out, its show resumes. Command pulses also work while jukebox Automatic DMX is turned off with `/automaticdmx off`.

## DMX safety

Automatic DMX is a temporary output layer. It does not write into the in-game DMX universes and does not replace a fixture's saved DMX or manual values. Jukebox skin changes likewise do not replace the saved skin or skin-channel assignment. Commands, console output, and external automation can continue changing DMX while a record is playing. When playback stops, those underlying values and skins become visible again.

Fixture movement is also relative to the underlying output. When a jukebox or `/dmxPulse` show begins at its first beat, each ordinary fixture starts from its existing pan and tilt rather than jumping to a shared center position. The temporary sweep is added to those starting values and kept within the standard 0-255 DMX range.

## Testing

1. Place or summon at least one DMX fixture, DMX Block, DMX Block Display, or DMX mob.
2. Put any music disc in a jukebox in the same dimension.
3. Run `/automaticdmx status` to see the detected disc and tempo.
4. Eject the disc and confirm that the fixtures return to their previous output.

Use these commands to control the feature:

```mcfunction
/automaticdmx on
/automaticdmx off
/automaticdmx status
```

The on/off setting lasts for the current game session.

## Disc timing

The built-in 26.1 records use these profiles:

| Disc | BPM | Timing note |
| --- | ---: | --- |
| 13 | 71 | Cinematic; the tempo is a show pulse rather than a continuous beat |
| cat | 112 | Steady |
| blocks | 85 | Steady |
| chirp | 110 | Steady |
| Creator | 81 | Steady |
| Creator (Music Box) | 111 | Steady |
| far | 130 | Steady |
| Lava Chicken | 157 | Steady |
| mall | 115 | Steady |
| mellohi | 91 | Steady |
| stal | 105 | Steady |
| strad | 188 | Steady |
| ward | 107 | Steady |
| 11 | 92 | Cinematic; no fixed musical BPM, so this is a measured show pulse |
| wait | 114 | Steady |
| otherside | 92 | Steady |
| Relic | 136 | Steady |
| 5 | 74 | Cinematic; musical sections are separated by sound design |
| Pigstep | 113 | Steady |
| Precipice | 136 | Steady |
| Tears | 99 | Steady |

These values were checked against the actual 26.1 game audio as well as published track metadata. Half-time and double-time readings are common for electronic music; the table uses the pulse that gives the most useful lighting movement. The cinematic profiles cannot follow every sound effect with a single repeating clock.

## Generic BPM discs

dmxLighting also provides `generic60bpm disc`, `generic70bpm disc`, `generic80bpm disc`, `generic90bpm disc`, `generic100bpm disc`, `generic110bpm disc`, `generic120bpm disc`, `generic130bpm disc`, and `generic140bpm disc`. Each disc always drives Automatic DMX at the tempo in its name. The supplied audio is a short example song that can be replaced by a resource pack.

For example:

```mcfunction
/give @s dmxlighting:generic_120_bpm
```

Generic discs allow up to one hour of playback so longer replacement tracks are not cut off. Minecraft's server cannot determine the duration of audio supplied by a client resource pack. Eject the disc after a shorter replacement track ends; otherwise the jukebox and Automatic DMX remain active until the one-hour ceiling.

Custom music discs are detected automatically. In this first version, a custom disc without its own profile uses a 120 BPM fallback beginning at the start of playback.

Long fixture color-fade settings can soften or delay the beat colors. For the clearest first test, turn color fading off or use a short fade time. Pan/tilt interpolation can remain enabled.
