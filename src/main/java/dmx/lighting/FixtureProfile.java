package dmx.lighting;

/**
 * Describes how a DMX fixture interprets its channels.
 *
 * Each fixture profile defines:
 *
 * - its unique name;
 * - how many DMX channels it occupies;
 * - how channel values become visible RGB output;
 * - which channel acts as the master dimmer.
 *
 * The first implemented profile is RGBD:
 *
 * Base + 0 = Red
 * Base + 1 = Green
 * Base + 2 = Blue
 * Base + 3 = Dimmer
 */
public interface FixtureProfile {

    /**
     * Returns the lowercase identifier used in commands and saved data.
     *
     * Example:
     *
     * rgbd
     */
    String getId();

    /**
     * Returns a human-readable name.
     *
     * Example:
     *
     * RGB + Dimmer
     */
    String getDisplayName();

    /**
     * Returns the number of consecutive DMX channels used by this
     * fixture profile.
     */
    int getChannelCount();

    /**
     * Reads the fixture's red output from its assigned universe.
     *
     * This returns the raw color value before applying the dimmer.
     */
    int getRed(
            DmxUniverse universe,
            int baseChannel
    );

    /**
     * Reads the fixture's green output from its assigned universe.
     */
    int getGreen(
            DmxUniverse universe,
            int baseChannel
    );

    /**
     * Reads the fixture's blue output from its assigned universe.
     */
    int getBlue(
            DmxUniverse universe,
            int baseChannel
    );

    /**
     * Reads the fixture's master dimmer.
     */
    int getDimmer(
            DmxUniverse universe,
            int baseChannel
    );

    /**
     * Returns the highest legal base channel for this profile.
     *
     * A fixture must fit entirely inside the 512-channel universe.
     */
    default int getMaximumBaseChannel() {
        return DmxUniverse.MAX_CHANNEL
                - getChannelCount()
                + 1;
    }
}