package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-to-client response containing the current Fixture Browser
 * entries for the requesting player's dimension.
 *
 * The server creates immutable FixtureBrowserEntry snapshots through
 * LightingConsole. The client can safely display these entries without
 * retaining references to server-side block entities.
 *
 * Fixture Browser patch data is parameter-centric. Each entry carries
 * the actual unique DMX channels occupied by the fixture rather than a
 * legacy base address.
 *
 * The payload also carries the fixture's stored Manual parameter values
 * so the Lighting Console Output view can inherit the currently stored
 * fixture settings.
 */
public record FixtureBrowserDataPayload(
        List<FixtureBrowserEntry> entries
) implements CustomPacketPayload {

    /**
     * Prevents an unexpectedly large fixture list from allocating
     * excessive memory or producing an oversized packet.
     */
    public static final int MAX_ENTRIES =
            4096;

    /**
     * Safety limit for one fixture's encoded occupied-channel list.
     *
     * The current FixtureParameterMap contains far fewer parameters
     * than this, so this leaves plenty of room for future expansion.
     */
    public static final int MAX_CHANNELS_PER_FIXTURE =
            64;

    public static final Type<FixtureBrowserDataPayload> TYPE =
            new Type<>(
                    DmxLighting.id(
                            "fixture_browser_data"
                    )
            );

    /**
     * Explicit list encoder and decoder.
     *
     * Encoding format:
     *
     * entry count
     *
     * For every entry:
     *
     * block position
     * fixture name
     * group name
     * fixture type
     * universe
     * assigned-channel count
     * assigned channels
     * control mode
     * packed RGB
     * Minecraft light level
     *
     * stored manual red
     * stored manual green
     * stored manual blue
     * stored manual white
     * stored manual amber
     * stored manual dimmer
     *
     * stored manual pan
     * stored manual tilt
     *
     * stored manual beam width
     * stored manual beam length
     *
     * stored manual strobe
     */
    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            FixtureBrowserDataPayload
    > STREAM_CODEC = new StreamCodec<>() {

        @Override
        public FixtureBrowserDataPayload decode(
                RegistryFriendlyByteBuf buffer
        ) {
            int entryCount =
                    buffer.readVarInt();

            if (entryCount < 0
                    || entryCount > MAX_ENTRIES) {

                throw new IllegalArgumentException(
                        "Invalid fixture browser entry count: "
                                + entryCount
                );
            }

            List<FixtureBrowserEntry> decodedEntries =
                    new ArrayList<>(
                            entryCount
                    );

            for (
                    int index = 0;
                    index < entryCount;
                    index++
            ) {
                decodedEntries.add(
                        decodeEntry(
                                buffer
                        )
                );
            }

            return new FixtureBrowserDataPayload(
                    decodedEntries
            );
        }

        @Override
        public void encode(
                RegistryFriendlyByteBuf buffer,
                FixtureBrowserDataPayload payload
        ) {
            List<FixtureBrowserEntry> safeEntries =
                    sanitizeEntries(
                            payload == null
                                    ? null
                                    : payload.entries()
                    );

            buffer.writeVarInt(
                    safeEntries.size()
            );

            for (FixtureBrowserEntry entry :
                    safeEntries) {

                encodeEntry(
                        buffer,
                        entry
                );
            }
        }
    };

    /**
     * Ensures the payload owns an immutable, non-null list and never
     * carries more than MAX_ENTRIES.
     */
    public FixtureBrowserDataPayload {
        entries =
                sanitizeEntries(
                        entries
                );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Encodes one browser row.
     */
    private static void encodeEntry(
            RegistryFriendlyByteBuf buffer,
            FixtureBrowserEntry entry
    ) {
        BlockPos.STREAM_CODEC.encode(
                buffer,
                entry.position()
        );

        buffer.writeUtf(
                entry.fixtureName(),
                FixtureIdentity.MAX_NAME_LENGTH
        );

        buffer.writeUtf(
                entry.groupName(),
                FixtureGroupName.MAX_LENGTH
        );

        buffer.writeUtf(
                entry.fixtureType(),
                64
        );

        buffer.writeVarInt(
                entry.universe()
        );

        int[] assignedChannels =
                entry.assignedChannels();

        int channelCount =
                Math.min(
                        assignedChannels.length,
                        MAX_CHANNELS_PER_FIXTURE
                );

        buffer.writeVarInt(
                channelCount
        );

        for (
                int index = 0;
                index < channelCount;
                index++
        ) {
            buffer.writeVarInt(
                    assignedChannels[index]
            );
        }

        buffer.writeUtf(
                entry.controlMode(),
                16
        );

        buffer.writeVarInt(
                entry.outputRgb()
        );

        buffer.writeVarInt(
                entry.lightLevel()
        );

        /*
         * -------------------------------------------------------------
         * Stored manual color/output values
         * -------------------------------------------------------------
         */

        buffer.writeVarInt(
                entry.manualRed()
        );

        buffer.writeVarInt(
                entry.manualGreen()
        );

        buffer.writeVarInt(
                entry.manualBlue()
        );

        buffer.writeVarInt(
                entry.manualWhite()
        );

        buffer.writeVarInt(
                entry.manualAmber()
        );

        buffer.writeVarInt(
                entry.manualDimmer()
        );

        /*
         * -------------------------------------------------------------
         * Stored manual movement
         * -------------------------------------------------------------
         */

        buffer.writeVarInt(
                entry.manualPan()
        );

        buffer.writeVarInt(
                entry.manualTilt()
        );

        /*
         * -------------------------------------------------------------
         * Stored manual beam parameters
         * -------------------------------------------------------------
         */

        buffer.writeVarInt(
                entry.manualBeamWidth()
        );

        buffer.writeVarInt(
                entry.manualBeamLength()
        );

        /*
         * -------------------------------------------------------------
         * Stored manual effects
         * -------------------------------------------------------------
         */

        buffer.writeVarInt(
                entry.manualStrobe()
        );
    }

    /**
     * Decodes one browser row.
     *
     * FixtureBrowserEntry performs final sanitization, sorting, and
     * duplicate removal on the channel array.
     */
    private static FixtureBrowserEntry decodeEntry(
            RegistryFriendlyByteBuf buffer
    ) {
        BlockPos position =
                BlockPos.STREAM_CODEC.decode(
                        buffer
                );

        String fixtureName =
                buffer.readUtf(
                        FixtureIdentity.MAX_NAME_LENGTH
                );

        String groupName =
                buffer.readUtf(
                        FixtureGroupName.MAX_LENGTH
                );

        String fixtureType =
                buffer.readUtf(
                        64
                );

        int universe =
                buffer.readVarInt();

        int channelCount =
                buffer.readVarInt();

        if (channelCount < 0
                || channelCount > MAX_CHANNELS_PER_FIXTURE) {

            throw new IllegalArgumentException(
                    "Invalid fixture browser channel count: "
                            + channelCount
            );
        }

        int[] assignedChannels =
                new int[channelCount];

        for (
                int index = 0;
                index < channelCount;
                index++
        ) {
            assignedChannels[index] =
                    buffer.readVarInt();
        }

        String controlMode =
                buffer.readUtf(
                        16
                );

        int outputRgb =
                buffer.readVarInt();

        int lightLevel =
                buffer.readVarInt();

        /*
         * -------------------------------------------------------------
         * Stored manual color/output values
         * -------------------------------------------------------------
         */

        int manualRed =
                buffer.readVarInt();

        int manualGreen =
                buffer.readVarInt();

        int manualBlue =
                buffer.readVarInt();

        int manualWhite =
                buffer.readVarInt();

        int manualAmber =
                buffer.readVarInt();

        int manualDimmer =
                buffer.readVarInt();

        /*
         * -------------------------------------------------------------
         * Stored manual movement
         * -------------------------------------------------------------
         */

        int manualPan =
                buffer.readVarInt();

        int manualTilt =
                buffer.readVarInt();

        /*
         * -------------------------------------------------------------
         * Stored manual beam parameters
         * -------------------------------------------------------------
         */

        int manualBeamWidth =
                buffer.readVarInt();

        int manualBeamLength =
                buffer.readVarInt();

        /*
         * -------------------------------------------------------------
         * Stored manual effects
         * -------------------------------------------------------------
         */

        int manualStrobe =
                buffer.readVarInt();

        return new FixtureBrowserEntry(
                position,
                fixtureName,
                groupName,
                fixtureType,
                universe,
                assignedChannels,
                controlMode,
                outputRgb,
                lightLevel,

                manualRed,
                manualGreen,
                manualBlue,
                manualWhite,
                manualAmber,
                manualDimmer,

                manualPan,
                manualTilt,

                manualBeamWidth,
                manualBeamLength,

                manualStrobe
        );
    }

    /**
     * Removes null entries, limits the list size, and returns an
     * immutable snapshot.
     */
    private static List<FixtureBrowserEntry> sanitizeEntries(
            List<FixtureBrowserEntry> entries
    ) {
        if (entries == null
                || entries.isEmpty()) {

            return List.of();
        }

        List<FixtureBrowserEntry> safeEntries =
                new ArrayList<>(
                        Math.min(
                                entries.size(),
                                MAX_ENTRIES
                        )
                );

        for (FixtureBrowserEntry entry :
                entries) {

            if (entry == null) {
                continue;
            }

            safeEntries.add(
                    entry
            );

            if (safeEntries.size()
                    >= MAX_ENTRIES) {

                break;
            }
        }

        return List.copyOf(
                safeEntries
        );
    }
}