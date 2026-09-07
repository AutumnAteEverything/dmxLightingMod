package dmx.lighting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server payload for editing one DMX parrot.
 *
 * DMX parrots are moving entities, so they are targeted by entity id
 * rather than block position.
 */
public record UpdateDmxParrotPayload(
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

    public static final Type<UpdateDmxParrotPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_dmx_parrot"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateDmxParrotPayload
    > STREAM_CODEC =
            StreamCodec.of(
                    UpdateDmxParrotPayload::write,
                    UpdateDmxParrotPayload::read
            );

    private static UpdateDmxParrotPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new UpdateDmxParrotPayload(
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
            UpdateDmxParrotPayload payload
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
