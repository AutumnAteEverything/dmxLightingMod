package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for targeted manual RGBD output.
 *
 * The selected fixture is identified by its block position.
 *
 * targetGroup:
 *
 * false
 *     Apply the values only to the selected fixture.
 *
 * true
 *     Apply the values to every loaded fixture in the selected
 *     fixture's named group.
 *
 * This packet stores manual values only. It does not automatically
 * switch any fixture into Manual mode.
 */
public record TargetedFixtureOutputPayload(
        BlockPos position,
        boolean targetGroup,
        int red,
        int green,
        int blue,
        int dimmer
) implements CustomPacketPayload {

    public static final Type<TargetedFixtureOutputPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "targeted_fixture_output"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TargetedFixtureOutputPayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TargetedFixtureOutputPayload::position,

            ByteBufCodecs.BOOL,
            TargetedFixtureOutputPayload::targetGroup,

            ByteBufCodecs.VAR_INT,
            TargetedFixtureOutputPayload::red,

            ByteBufCodecs.VAR_INT,
            TargetedFixtureOutputPayload::green,

            ByteBufCodecs.VAR_INT,
            TargetedFixtureOutputPayload::blue,

            ByteBufCodecs.VAR_INT,
            TargetedFixtureOutputPayload::dimmer,

            TargetedFixtureOutputPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}