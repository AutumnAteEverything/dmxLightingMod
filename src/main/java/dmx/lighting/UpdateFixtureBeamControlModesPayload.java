package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for changing the independent control source
 * of Beam Width and Beam Length.
 *
 * Each mode is serialized as a string:
 *
 * "dmx"
 *     Follow the assigned DMX channel.
 *
 * "manual"
 *     Use the stored manual parameter value while the fixture itself
 *     may remain in DMX mode.
 */
public record UpdateFixtureBeamControlModesPayload(
        BlockPos position,
        String beamWidthMode,
        String beamLengthMode
) implements CustomPacketPayload {

    public static final Type<UpdateFixtureBeamControlModesPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_fixture_beam_control_modes"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixtureBeamControlModesPayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateFixtureBeamControlModesPayload::position,

            ByteBufCodecs.STRING_UTF8,
            UpdateFixtureBeamControlModesPayload::beamWidthMode,

            ByteBufCodecs.STRING_UTF8,
            UpdateFixtureBeamControlModesPayload::beamLengthMode,

            UpdateFixtureBeamControlModesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}