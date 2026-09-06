package dmx.lighting;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Registers sound events used by the generic music discs.
 */
public final class ModSounds {

    public static final SoundEvent GENERIC_60_BPM =
            register("music_disc.generic_60_bpm");

    public static final SoundEvent GENERIC_70_BPM =
            register("music_disc.generic_70_bpm");

    public static final SoundEvent GENERIC_80_BPM =
            register("music_disc.generic_80_bpm");

    public static final SoundEvent GENERIC_90_BPM =
            register("music_disc.generic_90_bpm");

    public static final SoundEvent GENERIC_100_BPM =
            register("music_disc.generic_100_bpm");

    public static final SoundEvent GENERIC_110_BPM =
            register("music_disc.generic_110_bpm");

    public static final SoundEvent GENERIC_120_BPM =
            register("music_disc.generic_120_bpm");

    public static final SoundEvent GENERIC_130_BPM =
            register("music_disc.generic_130_bpm");

    public static final SoundEvent GENERIC_140_BPM =
            register("music_disc.generic_140_bpm");

    private ModSounds() {
        // Utility class.
    }

    public static void initialize() {
        DmxLighting.LOGGER.info(
                "Registered dmxLighting sounds."
        );
    }

    private static SoundEvent register(
            String name
    ) {
        Identifier id =
                DmxLighting.id(name);

        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(
                        id
                )
        );
    }
}
