# Changelog

All notable changes to dmxLighting are documented here. This project follows a simple release-oriented changelog while its public versioning policy is being established.

## [Unreleased]

### Added

- DMX Disco Ball fixture with a cubic mirrored body, twenty colored beams, a surface-projected Dots mode, stationary top/bottom mounting, initial-angle control, Dimmer, bipolar Spin Rate with 128 stopped, and a third DMX channel for effect selection.
- DMX Block skins 6-16 for placed blocks and Block Displays, including the new resource-pack slots and exact sixteen-way DMX selection ranges.

- DMX Creaking mob with standalone spawning, vanilla movement/aggression, RGB/dimmer patching, console editing, color fades, emissive rendering, and automatic-show support.
- DMX Axolotl mob with vanilla aquatic behavior and variants, RGB/dimmer patching, console editing, color fades, emissive rendering, and automatic-show support.
- Summonable DMX Block Display entities with console editing and output, RGB, dimmer, strobe, sixteen skins, optional DMX skin selection, color fades, automatic-show support, and the full vanilla display transformation system including negative scale.

- Automatic DMX shows triggered by music discs playing in jukeboxes.
- Beat-synchronized color and dimmer output for fixtures, DMX Blocks, and DMX mobs.
- Beat-synchronized pan and tilt movement for fixture blocks.
- Built-in disc BPM profiles and a 120 BPM fallback for custom music discs.
- `/automaticdmx on`, `/automaticdmx off`, and `/automaticdmx status` commands.
- Thorough GitHub documentation, user guides, command reference, texture guide, contributor guidance, issue templates, and release checklist.
- DMX Warden mob with RGB/dimmer patching, console editing, color fades, spawn egg support, and emissive DMX-colored rendering.
- Generic 60, 70, 80, 90, 100, 110, 120, 130, and 140 BPM music discs for custom resource-pack audio.
- Short example songs and a ready-to-zip generic music resource-pack template.
- `/dmxPulse`, `/dmxPulse status`, and `/dmxPulse stop` for externally timed, non-destructive beat shows with an automatic three-second timeout.
- DMX Nautilus mob with RGB/dimmer patching, console editing and output, groups, color fades, spawn egg support, emissive rendering, and automatic-show integration.

### Changed

- Automatic DMX pan and tilt movement now begins from each ordinary fixture's underlying pre-show position, eases in over the first beat, and uses stable position-based phases so fixtures move independently.
- Disco Ball Beam mode now uses a dark charcoal checkerboard body while Dots mode retains the bright silver mirrored checkerboard.
- Disco Ball beam mode no longer has a straight vertical beam and adds four shallower angled beams; Automatic DMX spin reversals remain approximately 24 beats apart.
- `/dmxsend` accepts any valid number of consecutive DMX values instead of a fixed four-value footprint.
- Generic BPM disc item names now end in `disc` so they appear in searches for music discs.

### Fixed

- Restored inventory icons for the DMX Creaking and DMX Axolotl spawn eggs.
- DMX Block Displays now refresh directly from jukebox playback and return to their underlying DMX color when playback stops.
- DMX Block Displays now render their emissive color base beneath the selected skin overlay, making direct DMX and automatic-show colors visible.

## [1.0.0] - Unreleased

### Added

- Configurable DMX Fixture block with PAR and Spotlight visuals.
- Explicit DMX mappings for RGB, white, amber, dimmer, pan, tilt, beam width, beam length, and strobe.
- DMX and manual fixture modes.
- Optional pan/tilt interpolation time.
- Installation mount pan/tilt controls.
- Visible colored beams and Minecraft light behavior.
- Fixture names and normalized groups.
- Handheld right-docked Lighting Console with Fixtures, Groups, Universes, Patch, and Output views.
- Fixture, group, and all-fixture console output targeting.
- DMX Block with RGB, dimmer, strobe, emissive zero-light rendering, and five skins.
- Optional DMX channel selection for DMX Block skins.
- Creative inventory registration and `DMX` search support.
- Interaction handling that opens block editors without placing the held block.
- Raw DMX, fixture configuration, inspection, diagnostics, and bulk-addressing commands.
- Resource-pack support for pixel skins, fixture/lens textures, and console texture.

### Known limitations

- DMX is simulated inside Minecraft; Art-Net, sACN, USB-DMX, and physical-console input are not implemented.
- The console operates on loaded fixtures in the player's current dimension.
- Rendering and UI behavior still require manual in-game testing; there is no dedicated automated test suite yet.
