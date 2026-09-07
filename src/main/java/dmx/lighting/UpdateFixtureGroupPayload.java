package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet used to assign a named group to a fixture.
 *
 * Examples:
 *
 * Balcony
 * Front Wash
 * House Lights
 *
 * Blank text means the fixture is ungrouped.
 *
 * The server normalizes the supplied text through FixtureGroupName, so
 * capitalization and repeated spaces do not create different groups.
 */
public record UpdateFixtureGroupPayload(
        BlockPos position,
        String groupName
) implements CustomPacketPayload {

    public static final Type<UpdateFixtureGroupPayload> TYPE =
            new Type<>(
                    DmxLighting.id("update_fixture_group")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            UpdateFixtureGroupPayload
    > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateFixtureGroupPayload::position,

            ByteBufCodecs.STRING_UTF8,
            UpdateFixtureGroupPayload::groupName,

            UpdateFixtureGroupPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}