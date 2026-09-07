package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server message used to select a DMX Block texture skin.
 */
public record UpdatePixelBlockSkinPayload(
        BlockPos position,
        int skin,
        int skinDmxChannel
) implements CustomPacketPayload {

    public static final Type<UpdatePixelBlockSkinPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_pixel_block_skin"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdatePixelBlockSkinPayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdatePixelBlockSkinPayload::position,

            ByteBufCodecs.VAR_INT,
            UpdatePixelBlockSkinPayload::skin,

            ByteBufCodecs.VAR_INT,
            UpdatePixelBlockSkinPayload::skinDmxChannel,

            UpdatePixelBlockSkinPayload::new
    );

    public UpdatePixelBlockSkinPayload {
        position =
                position.immutable();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
