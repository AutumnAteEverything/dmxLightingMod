package dmx.lighting;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents the name of a fixture group.
 *
 * A group has two forms:
 *
 * displayName
 *     The user-facing name shown in the GUI.
 *
 * key
 *     The normalized internal value used for comparisons and lookups.
 *
 * Examples:
 *
 * "Balcony"          -> key "balcony"
 * "  FRONT   WASH "  -> display "FRONT WASH", key "front wash"
 * ""                 -> ungrouped
 *
 * Two names with different capitalization or spacing therefore refer
 * to the same group:
 *
 * "Balcony"
 * "BALCONY"
 * " balcony "
 */
public final class FixtureGroupName {

    /**
     * Maximum number of characters accepted from the user.
     */
    public static final int MAX_LENGTH = 64;

    /**
     * Shared value representing a fixture that does not belong to a
     * group.
     */
    public static final FixtureGroupName UNGROUPED =
            new FixtureGroupName("", "");

    private final String key;
    private final String displayName;

    private FixtureGroupName(
            String key,
            String displayName
    ) {
        this.key = key;
        this.displayName = displayName;
    }

    /**
     * Creates a group name from text entered by the user.
     *
     * Blank text becomes the ungrouped value.
     */
    public static FixtureGroupName of(
            String input
    ) {
        String cleaned =
                cleanDisplayName(input);

        if (cleaned.isEmpty()) {
            return UNGROUPED;
        }

        String normalizedKey =
                cleaned.toLowerCase(Locale.ROOT);

        return new FixtureGroupName(
                normalizedKey,
                cleaned
        );
    }

    /**
     * Recreates a group from saved data.
     *
     * The display name is treated as authoritative. The saved key is
     * checked, but recalculated when it is missing or inconsistent.
     */
    public static FixtureGroupName fromSavedData(
            String savedKey,
            String savedDisplayName
    ) {
        FixtureGroupName reconstructed =
                of(savedDisplayName);

        if (reconstructed.isUngrouped()) {
            return UNGROUPED;
        }

        String cleanedSavedKey =
                savedKey == null
                        ? ""
                        : savedKey
                                .trim()
                                .toLowerCase(Locale.ROOT);

        if (!reconstructed.key.equals(cleanedSavedKey)) {
            DmxLighting.LOGGER.warn(
                    "Corrected inconsistent fixture group key '{}' to '{}'.",
                    savedKey,
                    reconstructed.key
            );
        }

        return reconstructed;
    }

    /**
     * Returns the normalized internal lookup key.
     */
    public String key() {
        return key;
    }

    /**
     * Returns the user-facing group name.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns true when this fixture is not assigned to a group.
     */
    public boolean isUngrouped() {
        return key.isEmpty();
    }

    /**
     * Returns text suitable for the fixture editor.
     */
    public String getGuiLabel() {
        return isUngrouped()
                ? "Ungrouped"
                : displayName;
    }

    /**
     * Two group names are equal when their normalized keys match.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof FixtureGroupName other)) {
            return false;
        }

        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return getGuiLabel();
    }

    /**
     * Trims the input, collapses repeated whitespace, and enforces the
     * maximum length.
     */
    private static String cleanDisplayName(
            String input
    ) {
        if (input == null) {
            return "";
        }

        String cleaned =
                input
                        .trim()
                        .replaceAll("\\s+", " ");

        if (cleaned.length() > MAX_LENGTH) {
            cleaned =
                    cleaned.substring(
                            0,
                            MAX_LENGTH
                    )
                    .trim();
        }

        return cleaned;
    }
}