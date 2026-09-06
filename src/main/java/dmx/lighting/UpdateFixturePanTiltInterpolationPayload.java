package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Saves the Pan/Tilt interpolation setting for one fixture.
 */
public record UpdateFixturePanTiltInterpolationPayload(
        BlockPos position,
        boolean enabled,
        float timeSeconds
) implements CustomPacketPayload {

    public static final Type<UpdateFixturePanTiltInterpolationPayload>
    TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_fixture_pan_tilt_interpolation"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixturePanTiltInterpolationPayload
    > STREAM_CODEC =
            StreamCodec.of(
                    UpdateFixturePanTiltInterpolationPayload::write,
                    UpdateFixturePanTiltInterpolationPayload::read
            );

    private static UpdateFixturePanTiltInterpolationPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new UpdateFixturePanTiltInterpolationPayload(
                buffer.readBlockPos(),
                buffer.readBoolean(),
                buffer.readFloat()
        );
    }

    private static void write(
            RegistryFriendlyByteBuf buffer,
            UpdateFixturePanTiltInterpolationPayload payload
    ) {
        buffer.writeBlockPos(
                payload.position()
        );

        buffer.writeBoolean(
                payload.enabled()
        );

        buffer.writeFloat(
                payload.timeSeconds()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
