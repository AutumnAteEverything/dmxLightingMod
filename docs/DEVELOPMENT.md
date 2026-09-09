# Development

## Toolchain

- Java 25
- Gradle wrapper included in the repository
- Fabric Loom 1.17-SNAPSHOT
- Minecraft 26.1
- Fabric Loader 0.19.3
- Fabric API 0.145.1+26.1

Version declarations live in `gradle.properties` and `build.gradle`.

## Run the development client

macOS and Linux:

```bash
./gradlew runClient
```

Windows:

```powershell
gradlew.bat runClient
```

The generated development world and client settings are stored below `run/` and should not be treated as release assets.

## Build

```bash
./gradlew build
```

Artifacts are written to `build/libs/`. Install the remapped regular JAR, not the sources JAR.

## Useful checks

```bash
./gradlew build
./gradlew runClient
```

There is not yet a dedicated automated test suite, so changes to rendering or screens require an in-game smoke test.

Suggested smoke test:

1. Search Creative inventory for `DMX` and obtain all three items.
2. Place both block types and confirm right-click never places the held item.
3. Save names, groups, universes, parameter maps, modes, manual values, mount settings, interpolation settings, and pixel skin settings; reload the world.
4. Test `/dmx` and `/dmxsend` at values 0, 1, 127, 128, 254, and 255.
5. Test a value list that ends at channel 512 and reject one that exceeds it.
6. Verify immediate movement with Smooth off and gradual movement with Smooth on.
7. Test every Lighting Console tab and Fixture, Group, and All output targets.
8. Confirm the DMX Block is full-bright in darkness and emits no block light.
9. Test all sixteen skins manually and through their DMX value ranges.
10. Enable the template resource pack and verify all replacement textures.

## Source layout

```text
src/main/java/dmx/lighting/                 Common/server logic, blocks, state, commands, payloads
src/client/java/dmx/lighting/client/        Screens, client networking, and renderers
src/main/resources/assets/dmxlighting/      Models, blockstates, language data, and textures
src/main/resources/fabric.mod.json          Fabric metadata and entrypoints
```

See [Architecture](ARCHITECTURE.md) for the main responsibilities and data flow.

## Preparing a public release

Before publishing:

1. Update the placeholder `description`, `authors`, `homepage`, and `sources` values in `src/main/resources/fabric.mod.json`.
2. Update `mod_version` in `gradle.properties`.
3. Add the final repository URL to the mod metadata and any future badges.
4. Add screenshots or a short demonstration video to the GitHub README/release.
5. Verify the exact Minecraft, Loader, Fabric API, and Java requirements.
6. Run the build and complete the smoke test above.
7. Update `CHANGELOG.md` and copy `.github/RELEASE_TEMPLATE.md` into the GitHub release description.
8. Test the built JAR in a clean Fabric instance rather than only through `runClient`.
9. Test a dedicated server with a matching client before advertising multiplayer support.

## Versioning and compatibility

The current project version is `1.0.0`. Until the save and networking formats are considered stable, release notes should identify any world-migration or client/server compatibility impact explicitly. Always back up test worlds before changes to block-entity persistence.
