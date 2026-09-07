package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for live manual fixture control.
 *
 * Moving a supported control in the fixture editor sends this packet
 * immediately while the fixture is in Manual mode.
 *
 * Values:
 *
 * - Red
 * - Green
 * - Blue
 * - White
 * - Amber
 * - Dimmer
 * - Pan
 * - Tilt
 * - Beam Width
 * - Beam Length
 * - Strobe
 *
 * Every value is validated and clamped by the server receiver.
 */
public record ManualFixtureOutputPayload(
        BlockPos position,
        int red,
        int green,
        int blue,
        int white,
        int amber,
        int dimmer,
        int pan,
        int tilt,
        int beamWidth,
        int beamLength,
        int strobe
) implements CustomPacketPayload {

    public static final Type<ManualFixtureOutputPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "manual_fixture_output"
                    )
            );

    /**
     * Explicit codec used because this payload contains many fields and
     * an explicit encoder/decoder remains straightforward and readable.
     */
    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            ManualFixtureOutputPayload
    > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public ManualFixtureOutputPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    BlockPos position =
                            BlockPos.STREAM_CODEC.decode(
                                    buffer
                            );

                    int red =
                            buffer.readVarInt();

                    int green =
                            buffer.readVarInt();

                    int blue =
                            buffer.readVarInt();

                    int white =
                            buffer.readVarInt();

                    int amber =
                            buffer.readVarInt();

                    int dimmer =
                            buffer.readVarInt();

                    int pan =
                            buffer.readVarInt();

                    int tilt =
                            buffer.readVarInt();

                    int beamWidth =
                            buffer.readVarInt();

                    int beamLength =
                            buffer.readVarInt();

                    int strobe =
                            buffer.readVarInt();

                    return new ManualFixtureOutputPayload(
                            position,
                            red,
                            green,
                            blue,
                            white,
                            amber,
                            dimmer,
                            pan,
                            tilt,
                            beamWidth,
                            beamLength,
                            strobe
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        ManualFixtureOutputPayload payload
                ) {
                    BlockPos.STREAM_CODEC.encode(
                            buffer,
                            payload.position()
                    );

                    buffer.writeVarInt(
                            payload.red()
                    );

                    buffer.writeVarInt(
                            payload.green()
                    );

                    buffer.writeVarInt(
                            payload.blue()
                    );

                    buffer.writeVarInt(
                            payload.white()
                    );

                    buffer.writeVarInt(
                            payload.amber()
                    );

                    buffer.writeVarInt(
                            payload.dimmer()
                    );

                    buffer.writeVarInt(
                            payload.pan()
                    );

                    buffer.writeVarInt(
                            payload.tilt()
                    );

                    buffer.writeVarInt(
                            payload.beamWidth()
                    );

                    buffer.writeVarInt(
                            payload.beamLength()
                    );

                    buffer.writeVarInt(
                            payload.strobe()
                    );
                }
            };

    /**
     * Compatibility constructor for older RGBD callers.
     *
     * White, Amber, Position, Beam, and Strobe default to zero.
     */
    public ManualFixtureOutputPayload(
            BlockPos position,
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        this(
                position,
                red,
                green,
                blue,
                0,
                0,
                dimmer,
                0,
                0,
                0,
                0,
                0
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}