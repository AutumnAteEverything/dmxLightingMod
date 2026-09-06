package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet used to change a fixture between DMX and
 * Manual control modes.
 */
public record SetFixtureModePayload(
        BlockPos position,
        String mode
) implements CustomPacketPayload {

    public static final Type<SetFixtureModePayload> TYPE =
            new Type<>(
                    DmxLighting.id("set_fixture_mode")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            SetFixtureModePayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SetFixtureModePayload::position,

            ByteBufCodecs.STRING_UTF8,
            SetFixtureModePayload::mode,

            SetFixtureModePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}