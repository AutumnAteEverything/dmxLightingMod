package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Saves the Beams or Dots effect selected for a DMX Disco Ball. */
public record UpdateDmxDiscoBallEffectPayload(
        BlockPos position,
        boolean dotsMode
) implements CustomPacketPayload {

    public static final Type<UpdateDmxDiscoBallEffectPayload> TYPE =
            new Type<>(
                    DmxLighting.id("update_dmx_disco_ball_effect")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateDmxDiscoBallEffectPayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateDmxDiscoBallEffectPayload::position,
            ByteBufCodecs.BOOL,
            UpdateDmxDiscoBallEffectPayload::dotsMode,
            UpdateDmxDiscoBallEffectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
