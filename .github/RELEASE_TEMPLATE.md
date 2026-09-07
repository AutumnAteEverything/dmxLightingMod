# dmxLighting VERSION

## Compatibility

- Minecraft: 26.2
- Fabric Loader: 0.19.3 or newer
- Fabric API: 0.155.2+26.2 or another compatible 26.2 build
- Java: 25 or newer

Install the regular JAR, not the sources JAR. Fabric API is required. For multiplayer, install matching versions on the server and all clients.

## Highlights

- 

## Added

- 

## Changed

- 

## Fixed

- 

## Known limitations

- DMX data is currently generated inside Minecraft; Art-Net, sACN, USB-DMX, and external-console input are not implemented.
- The Lighting Console lists loaded fixtures in the player's current dimension.

## Upgrade notes

- Back up important worlds before upgrading.
- Note any save-data migration or client/server compatibility changes here.

## Verification

- [ ] `./gradlew build` passes
- [ ] Built JAR tested in a clean Fabric client
- [ ] Dedicated server and matching client tested
- [ ] Existing world upgraded and reloaded successfully
- [ ] All three items appear in Creative search for `DMX`
- [ ] Fixture and DMX Block screens tested
- [ ] Lighting Console tabs and output targets tested
- [ ] All five DMX Block skins tested manually and by DMX
- [ ] Changelog and documentation updated

See the repository README for installation and usage instructions.
