# Automatic DMX

Automatic DMX turns a playing jukebox into a simple synchronized light show. It is enabled by default.

## What it controls

While a record is playing, every loaded DMX fixture, DMX Block, DMX Parrot, DMX Enderman, and DMX Warden in the same dimension follows the record's beat.

- All DMX fixtures and mobs receive saturated color changes every two beats.
- Brightness pulses on every beat, with a stronger pulse at the start of each four-beat show phrase.
- Fixture blocks receive smooth pan and tilt sweeps.
- DMX Blocks and DMX mobs keep their normal physical behavior and use only the color and brightness part of the show.

If more than one jukebox is playing, each fixture or mob follows the nearest active jukebox in its dimension.

## DMX safety

Automatic DMX is a temporary output layer. It does not write into the in-game DMX universes and does not replace a fixture's saved DMX or manual values. Commands, console output, and external automation can continue changing DMX while a record is playing. When playback stops, those underlying values become visible again.

## Testing

1. Place at least one DMX fixture, DMX Block, or DMX mob.
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

Custom music discs are detected automatically. In this first version, a custom disc without its own profile uses a 120 BPM fallback beginning at the start of playback.

Long fixture color-fade settings can soften or delay the beat colors. For the clearest first test, turn color fading off or use a short fade time. Pan/tilt interpolation can remain enabled.
