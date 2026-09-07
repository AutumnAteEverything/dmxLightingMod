package dmx.lighting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server request for the current Fixture Browser data.
 *
 * This packet contains no fields. The server uses the requesting
 * player's current dimension and returns every loaded DMX fixture
 * visible to the LightingConsole in that dimension.
 */
public record RequestFixtureBrowserPayload()
        implements CustomPacketPayload {

    public static final Type<RequestFixtureBrowserPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "request_fixture_browser"
                    )
            );

    /**
     * A packet with no data still needs a codec.
     */
    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            RequestFixtureBrowserPayload
    > STREAM_CODEC = StreamCodec.unit(
            new RequestFixtureBrowserPayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}