package dmx.lighting;

import java.util.Objects;

/**
 * Stores the DMX patch information for one fixture.
 *
 * Patch data:
 *
 * - Universe
 * - Base channel
 *
 * This class is intentionally independent from Minecraft and from the
 * fixture block entity. It only represents where a fixture is patched.
 */
public final class FixturePatch {

    public static final int MIN_UNIVERSE = 1;
    public static final int MAX_UNIVERSE = 9999;

    public static final int MIN_BASE_CHANNEL = 1;
    public static final int MAX_BASE_CHANNEL = 512;

    public static final int DEFAULT_UNIVERSE = 1;
    public static final int DEFAULT_BASE_CHANNEL = 1;

    private int universe;
    private int baseChannel;

    /**
     * Creates a patch using the default universe and base channel.
     */
    public FixturePatch() {
        this(
                DEFAULT_UNIVERSE,
                DEFAULT_BASE_CHANNEL
        );
    }

    /**
     * Creates a patch with explicit values.
     */
    public FixturePatch(
            int universe,
            int baseChannel
    ) {
        this.universe =
                clampUniverse(universe);

        this.baseChannel =
                clampBaseChannel(baseChannel);
    }

    /**
     * Returns the patched universe.
     */
    public int getUniverse() {
        return universe;
    }

    /**
     * Changes the patched universe.
     */
    public void setUniverse(int universe) {
        this.universe =
                clampUniverse(universe);
    }

    /**
     * Returns the first DMX channel used by the fixture.
     */
    public int getBaseChannel() {
        return baseChannel;
    }

    /**
     * Changes the first DMX channel used by the fixture.
     */
    public void setBaseChannel(int baseChannel) {
        this.baseChannel =
                clampBaseChannel(baseChannel);
    }

    /**
     * Sets the full patch at once.
     */
    public void set(
            int universe,
            int baseChannel
    ) {
        setUniverse(universe);
        setBaseChannel(baseChannel);
    }

    /**
     * Returns the highest valid base address for a fixture using the
     * supplied number of DMX channels.
     *
     * Examples:
     *
     * 1-channel fixture -> maximum base 512
     * 4-channel fixture -> maximum base 509
     * 16-channel fixture -> maximum base 497
     */
    public static int getMaximumBaseChannel(
            int channelCount
    ) {
        int safeChannelCount =
                Math.max(1, channelCount);

        return Math.max(
                MIN_BASE_CHANNEL,
                513 - safeChannelCount
        );
    }

    /**
     * Clamps the current base address so the supplied fixture profile
     * fits entirely inside one 512-channel universe.
     */
    public void clampForChannelCount(
            int channelCount
    ) {
        baseChannel =
                clamp(
                        baseChannel,
                        MIN_BASE_CHANNEL,
                        getMaximumBaseChannel(channelCount)
                );
    }

    /**
     * Returns the final channel occupied by a fixture with the supplied
     * channel count.
     */
    public int getEndChannel(
            int channelCount
    ) {
        int safeChannelCount =
                Math.max(1, channelCount);

        return Math.min(
                MAX_BASE_CHANNEL,
                baseChannel + safeChannelCount - 1
        );
    }

    /**
     * Returns true when this patch overlaps another patch in the same
     * universe.
     */
    public boolean overlaps(
            FixturePatch other,
            int thisChannelCount,
            int otherChannelCount
    ) {
        if (other == null) {
            return false;
        }

        if (universe != other.universe) {
            return false;
        }

        int thisStart =
                baseChannel;

        int thisEnd =
                getEndChannel(thisChannelCount);

        int otherStart =
                other.baseChannel;

        int otherEnd =
                other.getEndChannel(otherChannelCount);

        return thisStart <= otherEnd
                && otherStart <= thisEnd;
    }

    /**
     * Creates an independent copy.
     */
    public FixturePatch copy() {
        return new FixturePatch(
                universe,
                baseChannel
        );
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof FixturePatch other)) {
            return false;
        }

        return universe == other.universe
                && baseChannel == other.baseChannel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                universe,
                baseChannel
        );
    }

    @Override
    public String toString() {
        return "FixturePatch{"
                + "universe="
                + universe
                + ", baseChannel="
                + baseChannel
                + '}';
    }

    private static int clampUniverse(
            int universe
    ) {
        return clamp(
                universe,
                MIN_UNIVERSE,
                MAX_UNIVERSE
        );
    }

    private static int clampBaseChannel(
            int baseChannel
    ) {
        return clamp(
                baseChannel,
                MIN_BASE_CHANNEL,
                MAX_BASE_CHANNEL
        );
    }

    private static int clamp(
            int value,
            int minimum,
            int maximum
    ) {
        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }
}