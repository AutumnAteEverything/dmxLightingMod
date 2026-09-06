package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server message used to change a fixture's registered
 * profile.
 *
 * Example profile IDs:
 *
 * rgb_par
 * rgbw_par
 * static_spot
 * moving_head_spot
 */
public record UpdateFixtureProfilePayload(
        BlockPos position,
        String profileId
) implements CustomPacketPayload {

    public static final Type<UpdateFixtureProfilePayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_fixture_profile"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixtureProfilePayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateFixtureProfilePayload::position,

            ByteBufCodecs.STRING_UTF8,
            UpdateFixtureProfilePayload::profileId,

            UpdateFixtureProfilePayload::new
    );

    public UpdateFixtureProfilePayload {
        position =
                position.immutable();

        profileId =
                normalizeProfileId(
                        profileId
                );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static String normalizeProfileId(
            String profileId
    ) {
        if (profileId == null) {
            return "";
        }

        return profileId
                .trim()
                .toLowerCase()
                .replace(' ', '_')
                .replace('-', '_');
    }
}