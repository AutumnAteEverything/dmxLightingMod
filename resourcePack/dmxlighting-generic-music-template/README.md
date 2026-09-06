# dmxLighting Generic Music Template

This Minecraft 26.1 resource pack contains the nine generic BPM disc audio
slots and default example songs supplied by dmxLighting 1.3.1.

## Add your music

1. Convert a track to Ogg Vorbis. Mono audio at 44.1 kHz is recommended.
2. Choose the disc whose BPM matches the music.
3. Replace the matching file in `assets/dmxlighting/sounds/music/`.
4. Keep the filename and folders exactly as supplied.
5. Put this folder or its ZIP in Minecraft's `resourcepacks` folder and enable
   it above the default resources.

The available files are `generic_60_bpm.ogg`, `generic_70_bpm.ogg`,
`generic_80_bpm.ogg`, `generic_90_bpm.ogg`, `generic_100_bpm.ogg`,
`generic_110_bpm.ogg`, `generic_120_bpm.ogg`, `generic_130_bpm.ogg`, and
`generic_140_bpm.ogg`.

The light-show clock starts when the disc starts, so remove unwanted silence
before the first beat. The mod cannot read the duration of client resource-pack
audio. Generic discs therefore remain active for up to one hour; eject the disc
when a shorter replacement song ends.

Do not add `sounds.json` to this pack. The mod already defines the sound events,
and the replacement OGG files override its guide tracks directly.
