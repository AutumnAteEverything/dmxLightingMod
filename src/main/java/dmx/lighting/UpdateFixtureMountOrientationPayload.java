package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for changing a fixture's physical
 * installation orientation.
 *
 * These values are separate from live DMX pan and tilt.
 *
 * Both angles are stored as degrees and normalized into the range:
 *
 *     0 inclusive through 360 exclusive
 */
public record UpdateFixtureMountOrientationPayload(
        BlockPos position,
        float panDegrees,
        float tiltDegrees
) implements CustomPacketPayload {

    public static final Type<UpdateFixtureMountOrientationPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_fixture_mount_orientation"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixtureMountOrientationPayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateFixtureMountOrientationPayload::position,

            ByteBufCodecs.FLOAT,
            UpdateFixtureMountOrientationPayload::panDegrees,

            ByteBufCodecs.FLOAT,
            UpdateFixtureMountOrientationPayload::tiltDegrees,

            UpdateFixtureMountOrientationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}