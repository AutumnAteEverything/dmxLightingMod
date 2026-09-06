package dmx.lighting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server payload for editing one DMX Warden.
 *
 * DMX Wardens are moving entities, so they are targeted by entity id
 * rather than block position.
 */
public record UpdateDmxWardenPayload(
        int entityId,
        String fixtureName,
        String groupName,
        int universe,
        int redChannel,
        int greenChannel,
        int blueChannel,
        int dimmerChannel,
        boolean colorInterpolationEnabled,
        float colorInterpolationTimeSeconds
) implements CustomPacketPayload {

    public static final Type<UpdateDmxWardenPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_dmx_warden"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateDmxWardenPayload
    > STREAM_CODEC =
            StreamCodec.of(
                    UpdateDmxWardenPayload::write,
                    UpdateDmxWardenPayload::read
            );

    private static UpdateDmxWardenPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new UpdateDmxWardenPayload(
                buffer.readVarInt(),
                buffer.readUtf(
                        FixtureIdentity.MAX_NAME_LENGTH
                ),
                buffer.readUtf(
                        FixtureGroupName.MAX_LENGTH
                ),
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
            UpdateDmxWardenPayload payload
    ) {
        buffer.writeVarInt(
                payload.entityId()
        );

        buffer.writeUtf(
                payload.fixtureName(),
                FixtureIdentity.MAX_NAME_LENGTH
        );

        buffer.writeUtf(
                payload.groupName(),
                FixtureGroupName.MAX_LENGTH
        );

        buffer.writeVarInt(
                payload.universe()
        );

        buffer.writeVarInt(
                payload.redChannel()
        );

        buffer.writeVarInt(
                payload.greenChannel()
        );

        buffer.writeVarInt(
                payload.blueChannel()
        );

        buffer.writeVarInt(
                payload.dimmerChannel()
        );

        buffer.writeBoolean(
                payload.colorInterpolationEnabled()
        );

        buffer.writeFloat(
                payload.colorInterpolationTimeSeconds()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
