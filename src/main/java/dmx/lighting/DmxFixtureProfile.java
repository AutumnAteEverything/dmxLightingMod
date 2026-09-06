package dmx.lighting;

/**
 * Base definition for every supported fixture profile.
 *
 * A profile describes what a fixture is capable of, how many
 * channels it occupies, and how those channels are interpreted.
 *
 * The profile itself contains no mutable state.
 * Multiple fixtures may safely share the same profile instance.
 */
public interface DmxFixtureProfile {

    /**
     * Unique identifier.
     *
     * Examples:
     *
     * rgb_par
     * rgbw_par
     * moving_head_spot
     */
    String id();

    /**
     * Human-readable name.
     */
    String displayName();

    /**
     * Physical capabilities.
     */
    FixtureCapabilities capabilities();

    /**
     * DMX channel layout.
     */
    FixtureChannelLayout channelLayout();

    /**
     * Convenience method.
     */
    default int channelCount() {
        return channelLayout().channelCount();
    }

    /**
     * Whether this fixture emits a directional beam.
     */
    default boolean isBeamFixture() {
        return capabilities().supportsBeamRendering();
    }

    /**
     * Whether this fixture supports movement.
     */
    default boolean isMovingFixture() {
        return capabilities().supportsMovement();
    }

    /**
     * Whether this fixture supports color mixing.
     */
    default boolean supportsColor() {
        return capabilities().supportsColor();
    }
}