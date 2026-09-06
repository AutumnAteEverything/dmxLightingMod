package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Saves the visible RGB fade setting for one block fixture.
 */
public record UpdateFixtureColorInterpolationPayload(
        BlockPos position,
        boolean enabled,
        float timeSeconds
) implements CustomPacketPayload {

    public static final Type<UpdateFixtureColorInterpolationPayload>
    TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_fixture_color_interpolation"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixtureColorInterpolationPayload
    > STREAM_CODEC =
            StreamCodec.of(
                    UpdateFixtureColorInterpolationPayload::write,
                    UpdateFixtureColorInterpolationPayload::read
            );

    private static UpdateFixtureColorInterpolationPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new UpdateFixtureColorInterpolationPayload(
                buffer.readBlockPos(),
                buffer.readBoolean(),
                buffer.readFloat()
        );
    }

    private static void write(
            RegistryFriendlyByteBuf buffer,
            UpdateFixtureColorInterpolationPayload payload
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
