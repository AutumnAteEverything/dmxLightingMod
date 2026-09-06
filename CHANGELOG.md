# Changelog

All notable changes to dmxLighting are documented here. This project follows a simple release-oriented changelog while its public versioning policy is being established.

## [Unreleased]

### Added

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

- `/dmxsend` accepts any valid number of consecutive DMX values instead of a fixed four-value footprint.
- Generic BPM disc item names now end in `disc` so they appear in searches for music discs.

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
