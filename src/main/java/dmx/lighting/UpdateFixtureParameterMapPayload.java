package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload for updating one fixture's explicit
 * parameter-to-DMX-channel assignments.
 *
 * Channel rules:
 *
 * - 0 means unassigned
 * - 1 through 512 are valid DMX channels
 *
 * The fixture's universe is stored separately.
 */
public record UpdateFixtureParameterMapPayload(
        BlockPos position,

        int redChannel,
        int greenChannel,
        int blueChannel,
        int whiteChannel,
        int amberChannel,

        int dimmerChannel,

        int panChannel,
        int tiltChannel,

        int beamWidthChannel,
        int beamLengthChannel,

        int strobeChannel
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<
            UpdateFixtureParameterMapPayload
    > TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(
                            "dmxlighting",
                            "update_fixture_parameter_map"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixtureParameterMapPayload
    > STREAM_CODEC =
            StreamCodec.of(
                    UpdateFixtureParameterMapPayload::write,
                    UpdateFixtureParameterMapPayload::read
            );

    private static UpdateFixtureParameterMapPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new UpdateFixtureParameterMapPayload(
                buffer.readBlockPos(),

                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),

                buffer.readVarInt(),

                buffer.readVarInt(),
                buffer.readVarInt(),

                buffer.readVarInt(),
                buffer.readVarInt(),

                buffer.readVarInt()
        );
    }

    private static void write(
            RegistryFriendlyByteBuf buffer,
            UpdateFixtureParameterMapPayload payload
    ) {
        buffer.writeBlockPos(
                payload.position()
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
                payload.whiteChannel()
        );

        buffer.writeVarInt(
                payload.amberChannel()
        );

        buffer.writeVarInt(
                payload.dimmerChannel()
        );

        buffer.writeVarInt(
                payload.panChannel()
        );

        buffer.writeVarInt(
                payload.tiltChannel()
        );

        buffer.writeVarInt(
                payload.beamWidthChannel()
        );

        buffer.writeVarInt(
                payload.beamLengthChannel()
        );

        buffer.writeVarInt(
                payload.strobeChannel()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}