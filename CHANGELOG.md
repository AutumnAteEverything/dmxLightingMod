# Changelog

All notable changes to dmxLighting are documented here. This project follows a simple release-oriented changelog while its public versioning policy is being established.

## [Unreleased]

### Added

- Thorough GitHub documentation, user guides, command reference, texture guide, contributor guidance, issue templates, and release checklist.

### Changed

- `/dmxsend` accepts any valid number of consecutive DMX values instead of a fixed four-value footprint.

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
