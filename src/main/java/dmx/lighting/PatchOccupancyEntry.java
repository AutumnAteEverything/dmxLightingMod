package dmx.lighting;

import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents one fixture's occupied DMX channels.
 *
 * In the parameter-centric architecture, a fixture no longer occupies
 * one contiguous base-channel range.
 *
 * Instead, it occupies the explicitly assigned channels used by its
 * FixtureParameterMap.
 */
public record PatchOccupancyEntry(
        BlockPos position,
        String fixtureName,
        String profileId,
        String profileDisplayName,
        int universe,
        int[] assignedChannels
) {

    public PatchOccupancyEntry {
        position =
                Objects.requireNonNull(
                        position,
                        "position cannot be null"
                )
                .immutable();

        fixtureName =
                cleanText(
                        fixtureName,
                        "Unnamed Fixture"
                );

        profileId =
                cleanText(
                        profileId,
                        "unknown"
                );

        profileDisplayName =
                cleanText(
                        profileDisplayName,
                        profileId
                );

        universe =
                Math.max(
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        Math.min(
                                DmxFixtureBlockEntity.MAX_UNIVERSE,
                                universe
                        )
                );

        assignedChannels =
                normalizeChannels(
                        assignedChannels
                );
    }

    /**
     * Returns a defensive copy of the occupied DMX channels.
     */
    @Override
    public int[] assignedChannels() {
        return Arrays.copyOf(
                assignedChannels,
                assignedChannels.length
        );
    }

    /**
     * Returns the number of unique occupied DMX channels.
     */
    public int channelCount() {
        return assignedChannels.length;
    }

    /**
     * Returns the lowest assigned channel.
     *
     * Returns 0 when no channels are assigned.
     */
    public int firstChannel() {
        if (assignedChannels.length == 0) {
            return FixtureParameterMap.UNASSIGNED;
        }

        return assignedChannels[0];
    }

    /**
     * Returns the highest assigned channel.
     *
     * Returns 0 when no channels are assigned.
     */
    public int lastChannel() {
        if (assignedChannels.length == 0) {
            return FixtureParameterMap.UNASSIGNED;
        }

        return assignedChannels[
                assignedChannels.length - 1
        ];
    }

    /**
     * Returns true when at least one valid DMX channel is assigned.
     */
    public boolean hasAssignedChannels() {
        return assignedChannels.length > 0;
    }

    /**
     * Returns true when every assigned channel fits inside a standard
     * 512-channel DMX universe.
     *
     * Empty fixtures are considered valid.
     */
    public boolean fitsInUniverse() {
        for (int channel : assignedChannels) {
            if (!FixtureParameterMap.isAssigned(
                    channel
            )) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true when this fixture occupies at least one of the same
     * DMX channels as another fixture in the same universe.
     */
    public boolean overlaps(
            PatchOccupancyEntry other
    ) {
        if (other == null) {
            return false;
        }

        if (universe != other.universe) {
            return false;
        }

        int firstIndex =
                0;

        int secondIndex =
                0;

        while (firstIndex < assignedChannels.length
                && secondIndex < other.assignedChannels.length) {

            int firstChannel =
                    assignedChannels[firstIndex];

            int secondChannel =
                    other.assignedChannels[secondIndex];

            if (firstChannel == secondChannel) {
                return true;
            }

            if (firstChannel < secondChannel) {
                firstIndex++;
            } else {
                secondIndex++;
            }
        }

        return false;
    }

    /**
     * Returns a compact patch label for console display.
     *
     * Examples:
     *
     * U1 -
     * U1 001
     * U1 001,002,003,010
     */
    public String patchRangeLabel() {
        if (assignedChannels.length == 0) {
            return "U"
                    + universe
                    + " -";
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                "U"
        );

        builder.append(
                universe
        );

        builder.append(
                " "
        );

        for (
                int index = 0;
                index < assignedChannels.length;
                index++
        ) {
            if (index > 0) {
                builder.append(
                        ","
                );
            }

            builder.append(
                    formatChannel(
                            assignedChannels[index]
                    )
            );
        }

        return builder.toString();
    }

    /**
     * Returns a compact position label.
     */
    public String positionLabel() {
        return position.getX()
                + ","
                + position.getY()
                + ","
                + position.getZ();
    }

    /*
     * -----------------------------------------------------------------
     * Channel normalization
     * -----------------------------------------------------------------
     */

    /**
     * Removes:
     *
     * - Unassigned values
     * - Invalid channels
     * - Duplicate channels
     *
     * Returned channels are sorted ascending.
     */
    private static int[] normalizeChannels(
            int[] channels
    ) {
        if (channels == null
                || channels.length == 0) {

            return new int[0];
        }

        return Arrays.stream(
                channels
        )
                .filter(
                        FixtureParameterMap::isAssigned
                )
                .distinct()
                .sorted()
                .toArray();
    }

    private static String formatChannel(
            int channel
    ) {
        return String.format(
                "%03d",
                channel
        );
    }

    private static String cleanText(
            String value,
            String fallback
    ) {
        if (value == null) {
            return fallback;
        }

        String cleaned =
                value.trim();

        if (cleaned.isEmpty()) {
            return fallback;
        }

        return cleaned;
    }
}