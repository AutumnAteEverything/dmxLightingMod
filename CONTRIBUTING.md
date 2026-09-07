# Contributing to dmxLighting

Thank you for helping improve dmxLighting. Contributions can include bug reports, feature proposals, documentation, textures, tests, and code.

## Before opening an issue

1. Search existing issues for the same problem or idea.
2. Read the [Troubleshooting guide](docs/TROUBLESHOOTING.md).
3. Reproduce the problem with the current version and without unrelated mods or shaders when possible.
4. Do not include world files, logs, or screenshots containing private server addresses, tokens, or personal information.

## Bug reports

Include:

- dmxLighting version;
- Minecraft version;
- Fabric Loader and Fabric API versions;
- Java version;
- single-player or multiplayer;
- client and server mod lists when relevant;
- exact reproduction steps;
- expected and actual behavior;
- relevant log excerpts or crash report;
- screenshots or video for rendering/UI problems.

## Feature requests

Describe the use case before the proposed implementation. For DMX features, include an example channel layout and explain whether the feature belongs to fixture patching, manual control, console output, rendering, or a future external-protocol bridge.

## Pull requests

1. Keep changes focused on one problem.
2. Follow the existing Java formatting and naming style.
3. Preserve server authority: client screens request changes, and server handlers validate them.
4. Preserve backward-compatible defaults when changing saved block-entity data.
5. Update documentation and `CHANGELOG.md` for user-visible changes.
6. Build the project and perform the relevant in-game smoke tests from [Development](docs/DEVELOPMENT.md).
7. Explain how the change was tested in the pull request.

## Commit guidance

Use short, descriptive commits. Examples:

```text
Fix pixel skin range at DMX boundary
Document consecutive DMX output command
Preserve fixture group after profile change
```

## Texture contributions

Texture changes should preserve the expected PNG filenames, transparency, and dimensions unless the code and model are deliberately updated as part of the same contribution. Include screenshots in daylight and darkness.

## License

By contributing, you agree that your contribution is made available under the repository's [CC0 1.0 Universal license](LICENSE).
