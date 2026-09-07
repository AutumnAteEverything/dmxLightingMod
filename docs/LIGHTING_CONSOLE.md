# Lighting Console

The Lighting Console is a handheld control surface for loaded DMX Fixtures, DMX Blocks, DMX Block Displays, and DMX mobs. Hold it and right-click to open a panel docked to the right side of the screen.

## Fixture discovery

The server builds the fixture list for the player's current dimension. A fixture must be in a loaded chunk to appear. If a fixture is missing, move close enough to load it and reopen or refresh the console.

## Views

### Fixtures

Lists loaded fixtures with identity, group, profile, patch, position, mode, and color information. Selecting a fixture establishes the source fixture for single-fixture and group operations.

### Groups

Summarizes named groups, fixture counts, and whether the group's members use DMX, Manual, or mixed modes. Opening a group filters the fixture list to that group.

### Universes

Summarizes each represented universe, how many fixtures use it, how many channels are occupied, and whether overlapping patch assignments were detected.

### Patch

Shows channel occupancy and conflicts. Because fixtures use explicit parameter channels, two parameters are in conflict when they are assigned to the same universe and absolute channel.

### Output

Provides live manual-style parameter controls and three targets:

- **Fixture** — only the selected fixture;
- **Group** — all loaded fixtures in the selected fixture's named group;
- **All** — every loaded fixture in the current dimension.

The console sends only the parameters selected by its output controls. Applying one parameter to a group does not overwrite unrelated stored values.

## Recommended workflow

1. Name fixtures clearly in their block editors.
2. Assign consistent group names such as `Front Wash`, `Backlight`, or `Pixels Left`.
3. Open the console and check Universes and Patch for collisions.
4. Select a fixture or group.
5. Open Output, choose Fixture, Group, or All, and adjust the desired parameters.

## Important behavior

- Group names are case-insensitive and repeated spaces are normalized.
- Ungrouped fixtures cannot be used as a Group target.
- Console output operates on loaded fixtures only.
- Console control does not create an external DMX stream; it updates fixture state inside Minecraft.
- Closing the screen does not delete fixture names, groups, patches, modes, or stored manual values.
- DMX Block Displays are edited from their console row. Their vanilla display transformations remain command-driven.
