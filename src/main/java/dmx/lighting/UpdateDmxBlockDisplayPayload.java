package dmx.lighting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-to-server editor payload for one DMX Block Display. */
public record UpdateDmxBlockDisplayPayload(
        int entityId,
        String fixtureName,
        String groupName,
        int universe,
        int redChannel,
        int greenChannel,
        int blueChannel,
        int dimmerChannel,
        int strobeChannel,
        int skin,
        int skinDmxChannel,
        boolean colorInterpolationEnabled,
        float colorInterpolationTimeSeconds
) implements CustomPacketPayload {

    public static final Type<UpdateDmxBlockDisplayPayload> TYPE =
            new Type<>(DmxLighting.id("update_dmx_block_display"));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateDmxBlockDisplayPayload
    > STREAM_CODEC = StreamCodec.of(
            UpdateDmxBlockDisplayPayload::write,
            UpdateDmxBlockDisplayPayload::read
    );

    private static UpdateDmxBlockDisplayPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new UpdateDmxBlockDisplayPayload(
                buffer.readVarInt(),
                buffer.readUtf(FixtureIdentity.MAX_NAME_LENGTH),
                buffer.readUtf(FixtureGroupName.MAX_LENGTH),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readFloat()
        );
    }

    private static void write(
            RegistryFriendlyByteBuf buffer,
            UpdateDmxBlockDisplayPayload payload
    ) {
        buffer.writeVarInt(payload.entityId());
        buffer.writeUtf(
                payload.fixtureName(),
                FixtureIdentity.MAX_NAME_LENGTH
        );
        buffer.writeUtf(payload.groupName(), FixtureGroupName.MAX_LENGTH);
        buffer.writeVarInt(payload.universe());
        buffer.writeVarInt(payload.redChannel());
        buffer.writeVarInt(payload.greenChannel());
        buffer.writeVarInt(payload.blueChannel());
        buffer.writeVarInt(payload.dimmerChannel());
        buffer.writeVarInt(payload.strobeChannel());
        buffer.writeVarInt(payload.skin());
        buffer.writeVarInt(payload.skinDmxChannel());
        buffer.writeBoolean(payload.colorInterpolationEnabled());
        buffer.writeFloat(payload.colorInterpolationTimeSeconds());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
