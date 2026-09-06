package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload for updating one fixture's visual beam
 * settings.
 *
 * These values are independent of live DMX Zoom:
 *
 * - Beam width: 45 through 135
 * - Beam length: 1 through 25 blocks
 */
public record UpdateFixtureBeamSettingsPayload(
        BlockPos position,
        int widthDegrees,
        int lengthBlocks
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<
            UpdateFixtureBeamSettingsPayload
    > TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(
                            "dmxlighting",
                            "update_fixture_beam_settings"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixtureBeamSettingsPayload
    > STREAM_CODEC =
            StreamCodec.of(
                    UpdateFixtureBeamSettingsPayload::write,
                    UpdateFixtureBeamSettingsPayload::read
            );

    private static UpdateFixtureBeamSettingsPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new UpdateFixtureBeamSettingsPayload(
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    private static void write(
            RegistryFriendlyByteBuf buffer,
            UpdateFixtureBeamSettingsPayload payload
    ) {
        buffer.writeBlockPos(
                payload.position()
        );

        buffer.writeVarInt(
                payload.widthDegrees()
        );

        buffer.writeVarInt(
                payload.lengthBlocks()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}