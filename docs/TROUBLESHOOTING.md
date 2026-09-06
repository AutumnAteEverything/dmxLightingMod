# Troubleshooting

## A fixture does not respond to `/dmx` or `/dmxsend`

Check all of the following:

1. The fixture is in DMX mode.
2. The command universe matches the fixture universe.
3. The parameter's channel field is assigned to the channel being written.
4. Dimmer is above 0.
5. At least one color parameter is above 0.
6. The fixture's chunk is loaded.

Inspect the live state:

```mcfunction
/dmxfixture inspect <x> <y> <z>
```

## Pan and tilt jump instead of moving smoothly

Open the fixture editor and verify that Smooth is enabled and its time field contains a valid value from 0.05 to 60 seconds. Interpolation applies only to pan and tilt; color, dimmer, beam, and strobe changes remain immediate.

If exact snap movement is desired, turn Smooth off.

## A fixture is missing from the Lighting Console

The console lists loaded fixtures in the player's current dimension. Move close enough to load the fixture's chunk, verify that you are in the same dimension, and reopen the console.

## Group output fails

Group targeting needs either a selected named group or a selected fixture that belongs to one. Blank group names mean ungrouped. Group names ignore case and normalize repeated spaces.

## The patch view shows a conflict

Two or more parameters are assigned to the same universe and channel. Open the affected fixtures and move one of the assignments. Shared channels are allowed by the data model and can be intentional, but the console reports them so accidental overlaps are visible.

## The DMX Block is visible but does not light nearby blocks

That is intentional. Its surface is emissive/full-bright, but its Minecraft block-light level is always zero. Use a DMX Fixture if surrounding Minecraft light is required.

## The DMX Block skin does not follow DMX

Verify that:

- the block is in DMX mode;
- Skin DMX Channel is assigned;
- the command uses the same universe and channel;
- the value is in the expected skin range.

Manual mode and an unassigned skin channel use the skin selected by the UI button.

## Creative search does not show the items

Search for `DMX`. DMX Fixture and DMX Block are in Functional Blocks; Lighting Console is in Tools & Utilities. Confirm that the mod loaded by checking the Mods screen or game log for `dmxlighting`.

## Right-click places the block in my hand

The current mod consumes interactions on both DMX block types before vanilla placement. If placement still occurs, confirm that both client and server are running the same current mod JAR and remove older duplicate dmxLighting JARs from the `mods` folder.

## Resource-pack textures do not appear

Check the namespace and capitalization exactly: `assets/dmxlighting/...`. Reload resources with `F3+T`, ensure the pack is enabled above lower-priority packs, and check that the PNG names match [the texture list](RESOURCE_PACKS.md).

## The mod does not receive my external console

External protocol input is not implemented. `/dmx`, `/dmxsend`, the fixture UI, and the Lighting Console create the current in-game values. Art-Net, sACN, USB-DMX, and physical consoles are not connected by this release.

## Build errors

- Confirm Java 25 is active with `java -version`.
- Use the included Gradle wrapper, not a separately installed Gradle version.
- Ensure the computer can download Fabric and Minecraft development dependencies.
- If a previous Gradle process was interrupted, close other editors/builds and retry.
- Run `./gradlew clean build` only when a normal build remains stale; it removes generated build output, not source files.

When reporting a bug, include the Minecraft, Fabric Loader, Fabric API, Java, and dmxLighting versions; the relevant log section; reproduction steps; and whether the problem occurs in single-player or multiplayer.
