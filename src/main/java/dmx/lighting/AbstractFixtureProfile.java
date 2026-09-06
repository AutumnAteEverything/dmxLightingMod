package dmx.lighting;

import java.util.Objects;

/**
 * Base implementation for immutable fixture profiles.
 *
 * This class stores the common profile information so individual
 * fixture types only need to provide their own constructor values.
 *
 * Example:
 *
 * public final class RgbParProfile
 *         extends AbstractFixtureProfile {
 *
 *     public RgbParProfile() {
 *         super(
 *                 "rgb_par",
 *                 "RGB PAR",
 *                 FixtureCapabilities.RGB_PAR,
 *                 FixtureChannelLayout.RGB
 *         );
 *     }
 * }
 */
public abstract class AbstractFixtureProfile
        implements DmxFixtureProfile {

    private final String id;
    private final String displayName;

    private final FixtureCapabilities capabilities;
    private final FixtureChannelLayout channelLayout;

    protected AbstractFixtureProfile(
            String id,
            String displayName,
            FixtureCapabilities capabilities,
            FixtureChannelLayout channelLayout
    ) {
        this.id =
                validateId(id);

        this.displayName =
                validateDisplayName(displayName);

        this.capabilities =
                Objects.requireNonNull(
                        capabilities,
                        "capabilities cannot be null"
                );

        this.channelLayout =
                Objects.requireNonNull(
                        channelLayout,
                        "channelLayout cannot be null"
                );

        validateCompatibility(
                this.capabilities,
                this.channelLayout
        );
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final String displayName() {
        return displayName;
    }

    @Override
    public final FixtureCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public final FixtureChannelLayout channelLayout() {
        return channelLayout;
    }

    /**
     * Verifies that the declared capabilities agree with the declared
     * DMX channel layout.
     *
     * This catches profile-definition mistakes during startup rather
     * than allowing a broken fixture profile to enter the registry.
     */
    private static void validateCompatibility(
            FixtureCapabilities capabilities,
            FixtureChannelLayout layout
    ) {
        if (capabilities.redGreenBlue()
                != layout.hasRgb()) {

            throw new IllegalArgumentException(
                    "RGB capability does not match channel layout"
            );
        }

        if (capabilities.white()
                != layout.hasWhite()) {

            throw new IllegalArgumentException(
                    "White capability does not match channel layout"
            );
        }

        if (capabilities.dimmer()
                != layout.hasDimmer()) {

            throw new IllegalArgumentException(
                    "Dimmer capability does not match channel layout"
            );
        }

        if (capabilities.pan()
                != layout.hasPan()) {

            throw new IllegalArgumentException(
                    "Pan capability does not match channel layout"
            );
        }

        if (capabilities.tilt()
                != layout.hasTilt()) {

            throw new IllegalArgumentException(
                    "Tilt capability does not match channel layout"
            );
        }

        if (capabilities.zoom()
                != layout.hasZoom()) {

            throw new IllegalArgumentException(
                    "Zoom capability does not match channel layout"
            );
        }

        if (capabilities.strobe()
                != layout.hasStrobe()) {

            throw new IllegalArgumentException(
                    "Strobe capability does not match channel layout"
            );
        }

        if (capabilities.gobo()
                != layout.hasGobo()) {

            throw new IllegalArgumentException(
                    "Gobo capability does not match channel layout"
            );
        }
    }

    /**
     * Profile IDs are normalized to lowercase snake_case.
     */
    private static String validateId(
            String id
    ) {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        String normalized =
                id.trim()
                        .toLowerCase()
                        .replace(' ', '_')
                        .replace('-', '_');

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "id cannot be blank"
            );
        }

        if (!normalized.matches(
                "[a-z0-9_]+"
        )) {
            throw new IllegalArgumentException(
                    "id may only contain lowercase letters, "
                            + "numbers, and underscores"
            );
        }

        return normalized;
    }

    private static String validateDisplayName(
            String displayName
    ) {
        Objects.requireNonNull(
                displayName,
                "displayName cannot be null"
        );

        String normalized =
                displayName.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "displayName cannot be blank"
            );
        }

        return normalized;
    }

    @Override
    public final String toString() {
        return displayName
                + " ["
                + id
                + "]";
    }
}