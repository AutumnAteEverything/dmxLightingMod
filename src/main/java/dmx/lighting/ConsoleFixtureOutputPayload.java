package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-to-server packet for performance-oriented fixture output from
 * the handheld Lighting Console.
 *
 * The console may target:
 *
 * "fixture"
 *     One fixture identified by position.
 *
 * "group"
 *     Every loaded fixture belonging to the selected fixture's group.
 *
 * "all"
 *     Every loaded DMX fixture in the player's current dimension.
 *
 * The packet carries the complete parameter-centric manual output:
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
 * An apply mask determines which values should actually overwrite the
 * target fixture's stored manual values.
 *
 * Parameters whose bits are not set preserve each fixture's existing
 * manual value.
 *
 * This packet does not automatically change whole-fixture DMX / Manual
 * control mode.
 */
public record ConsoleFixtureOutputPayload(
        BlockPos position,
        String target,

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

        int strobe,

        int applyMask,
        int targetEntityId
) implements CustomPacketPayload {

    /*
     * -----------------------------------------------------------------
     * Targets
     * -----------------------------------------------------------------
     */

    public static final String TARGET_FIXTURE =
            "fixture";

    public static final String TARGET_GROUP =
            "group";

    public static final String TARGET_ALL =
            "all";

    /*
     * -----------------------------------------------------------------
     * Apply-mask bits
     * -----------------------------------------------------------------
     */

    public static final int APPLY_RED =
            1 << 0;

    public static final int APPLY_GREEN =
            1 << 1;

    public static final int APPLY_BLUE =
            1 << 2;

    public static final int APPLY_WHITE =
            1 << 3;

    public static final int APPLY_AMBER =
            1 << 4;

    public static final int APPLY_DIMMER =
            1 << 5;

    public static final int APPLY_PAN =
            1 << 6;

    public static final int APPLY_TILT =
            1 << 7;

    public static final int APPLY_BEAM_WIDTH =
            1 << 8;

    public static final int APPLY_BEAM_LENGTH =
            1 << 9;

    public static final int APPLY_STROBE =
            1 << 10;

    public static final int APPLY_ALL =
            APPLY_RED
                    | APPLY_GREEN
                    | APPLY_BLUE
                    | APPLY_WHITE
                    | APPLY_AMBER
                    | APPLY_DIMMER
                    | APPLY_PAN
                    | APPLY_TILT
                    | APPLY_BEAM_WIDTH
                    | APPLY_BEAM_LENGTH
                    | APPLY_STROBE;

    public static final int NO_TARGET_ENTITY_ID =
            -1;

    /*
     * -----------------------------------------------------------------
     * Payload type
     * -----------------------------------------------------------------
     */

    public static final Type<ConsoleFixtureOutputPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "console_fixture_output"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            ConsoleFixtureOutputPayload
    > STREAM_CODEC =
            StreamCodec.of(
                    ConsoleFixtureOutputPayload::write,
                    ConsoleFixtureOutputPayload::read
            );

    /*
     * -----------------------------------------------------------------
     * Convenience constructor
     * -----------------------------------------------------------------
     */

    /**
     * Compatibility constructor.
     *
     * Existing callers that do not supply an apply mask will apply all
     * parameters.
     */
    public ConsoleFixtureOutputPayload(
            BlockPos position,
            String target,

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
    ) {
        this(
                position,
                target,

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

                strobe,

                APPLY_ALL,
                NO_TARGET_ENTITY_ID
        );
    }

    public ConsoleFixtureOutputPayload(
            BlockPos position,
            String target,

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

            int strobe,

            int applyMask
    ) {
        this(
                position,
                target,

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

                strobe,

                applyMask,
                NO_TARGET_ENTITY_ID
        );
    }

    /*
     * -----------------------------------------------------------------
     * Mask helpers
     * -----------------------------------------------------------------
     */

    public boolean appliesAnyParameter() {
        return (applyMask & APPLY_ALL)
                != 0;
    }

    public boolean appliesRed() {
        return hasApplyFlag(
                APPLY_RED
        );
    }

    public boolean appliesGreen() {
        return hasApplyFlag(
                APPLY_GREEN
        );
    }

    public boolean appliesBlue() {
        return hasApplyFlag(
                APPLY_BLUE
        );
    }

    public boolean appliesWhite() {
        return hasApplyFlag(
                APPLY_WHITE
        );
    }

    public boolean appliesAmber() {
        return hasApplyFlag(
                APPLY_AMBER
        );
    }

    public boolean appliesDimmer() {
        return hasApplyFlag(
                APPLY_DIMMER
        );
    }

    public boolean appliesPan() {
        return hasApplyFlag(
                APPLY_PAN
        );
    }

    public boolean appliesTilt() {
        return hasApplyFlag(
                APPLY_TILT
        );
    }

    public boolean appliesBeamWidth() {
        return hasApplyFlag(
                APPLY_BEAM_WIDTH
        );
    }

    public boolean appliesBeamLength() {
        return hasApplyFlag(
                APPLY_BEAM_LENGTH
        );
    }

    public boolean appliesStrobe() {
        return hasApplyFlag(
                APPLY_STROBE
        );
    }

    private boolean hasApplyFlag(
            int flag
    ) {
        return (applyMask & flag)
                != 0;
    }

    /*
     * -----------------------------------------------------------------
     * Codec
     * -----------------------------------------------------------------
     */

    private static ConsoleFixtureOutputPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new ConsoleFixtureOutputPayload(
                buffer.readBlockPos(),
                buffer.readUtf(),

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

                buffer.readVarInt(),

                buffer.readVarInt(),

                buffer.readVarInt()
        );
    }

    private static void write(
            RegistryFriendlyByteBuf buffer,
            ConsoleFixtureOutputPayload payload
    ) {
        buffer.writeBlockPos(
                payload.position()
        );

        buffer.writeUtf(
                payload.target()
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

        buffer.writeVarInt(
                payload.applyMask()
        );

        buffer.writeVarInt(
                payload.targetEntityId()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
