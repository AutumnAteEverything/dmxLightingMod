# Getting started

This guide creates a fixture, patches it to an in-game DMX universe, tests it, and opens the lighting console.

## 1. Find the mod items

In Creative mode, search for `DMX`. The search should show:

- DMX Fixture
- DMX Block
- DMX Parrot Spawn Egg
- DMX Enderman Spawn Egg
- DMX Warden Spawn Egg
- DMX Nautilus Spawn Egg
- Lighting Console

The two blocks are also in Functional Blocks. The console and DMX mob spawn eggs are in Tools & Utilities.

## 2. Place and configure a fixture

Place a DMX Fixture and right-click it. The editor stores settings in the block entity, so they remain with the world after saving and reloading.

Recommended first setup:

| Setting | Value |
| --- | --- |
| Name | Front Wash 1 |
| Group | Front Wash |
| Profile | PAR |
| Universe | 1 |
| Red channel | 101 |
| Green channel | 102 |
| Blue channel | 103 |
| Dimmer channel | 104 |

Leave unused channel fields blank. Channel `0` in commands also means unassigned.

Choose **DMX** for the fixture mode, then save or close the editor as indicated by the screen.

## 3. Test DMX output

Set channels 101-104 in a single command:

```mcfunction
/dmxsend 1 101 255 0 128 255
```

This sends:

| Channel | Parameter | Value |
| --- | --- | --- |
| 101 | Red | 255 |
| 102 | Green | 0 |
| 103 | Blue | 128 |
| 104 | Dimmer | 255 |

Change one channel without affecting the others:

```mcfunction
/dmx 1 104 64
```

The fixture should respond immediately. If it does not, use:

```mcfunction
/dmxfixture inspect <x> <y> <z>
```

See [Troubleshooting](TROUBLESHOOTING.md) if the patch and values look correct but there is no visible output.

## 4. Try manual mode

Right-click the fixture and select **Manual**. The manual sliders now control its output independently of the universe values. Switching back to DMX restores live DMX control; stored manual values are retained.

Manual output can also be set by command:

```mcfunction
/dmxfixture manual <x> <y> <z> 255 100 0 255
```

## 5. Open the console

Hold the Lighting Console and right-click. Its panel remains docked to the right so most of the world stays visible.

Use the tabs to:

- browse fixtures;
- inspect groups and universes;
- identify patch conflicts;
- send live output to one fixture, a fixture group, or all loaded fixtures.

Only loaded fixtures in the player's current dimension are available to the console.

## 6. Add a DMX Block

Place a DMX Block and right-click it. Assign red, green, blue, dimmer, and optionally strobe and skin channels. Its colored surface is visible in darkness but it deliberately emits zero Minecraft block light.

Continue with [DMX Fixtures](FIXTURES.md), [DMX Blocks](DMX_BLOCK.md), and [Lighting Console](LIGHTING_CONSOLE.md).
