package dmx.lighting;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

/**
 * Registry keys for the resource-pack-friendly generic music discs.
 */
public final class ModJukeboxSongs {

    public static final ResourceKey<JukeboxSong> GENERIC_60_BPM =
            create("generic_60_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_70_BPM =
            create("generic_70_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_80_BPM =
            create("generic_80_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_90_BPM =
            create("generic_90_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_100_BPM =
            create("generic_100_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_110_BPM =
            create("generic_110_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_120_BPM =
            create("generic_120_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_130_BPM =
            create("generic_130_bpm");

    public static final ResourceKey<JukeboxSong> GENERIC_140_BPM =
            create("generic_140_bpm");

    private ModJukeboxSongs() {
        // Utility class.
    }

    private static ResourceKey<JukeboxSong> create(
            String name
    ) {
        return ResourceKey.create(
                Registries.JUKEBOX_SONG,
                DmxLighting.id(name)
        );
    }
}
