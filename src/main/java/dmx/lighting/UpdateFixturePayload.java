package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server message sent when the player saves general fixture
 * configuration.
 *
 * This payload now handles only:
 *
 * - Fixture position
 * - Fixture name
 * - DMX universe
 *
 * Parameter-to-channel assignments are stored separately through
 * UpdateFixtureParameterMapPayload.
 *
 * The legacy Base Channel value is no longer part of normal fixture
 * configuration.
 */
public record UpdateFixturePayload(
        BlockPos position,
        String fixtureName,
        int universe
) implements CustomPacketPayload {

    /*
     * -----------------------------------------------------------------
     * Payload identity
     * -----------------------------------------------------------------
     */

    public static final Type<
            UpdateFixturePayload
    > TYPE =
            new Type<>(
                    DmxLighting.id(
                            "update_fixture"
                    )
            );

    /*
     * -----------------------------------------------------------------
     * Network codec
     * -----------------------------------------------------------------
     */

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixturePayload
    > STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    UpdateFixturePayload::position,

                    ByteBufCodecs.STRING_UTF8,
                    UpdateFixturePayload::fixtureName,

                    ByteBufCodecs.VAR_INT,
                    UpdateFixturePayload::universe,

                    UpdateFixturePayload::new
            );

    /*
     * -----------------------------------------------------------------
     * CustomPacketPayload
     * -----------------------------------------------------------------
     */

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}