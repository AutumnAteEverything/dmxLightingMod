package dmx.lighting;

/**
 * Describes the DMX channel offsets used by one fixture profile.
 *
 * Offsets are zero-based relative to the fixture's base address.
 *
 * Example:
 *
 * A fixture patched to address 101 with:
 *
 *     dimmerOffset = 0
 *     redOffset    = 1
 *     greenOffset  = 2
 *     blueOffset   = 3
 *
 * uses the following absolute DMX channels:
 *
 *     Dimmer = 101
 *     Red    = 102
 *     Green  = 103
 *     Blue   = 104
 *
 * A value of -1 means that the fixture does not support that channel.
 */
public record FixtureChannelLayout(
        int channelCount,
        int dimmerOffset,
        int redOffset,
        int greenOffset,
        int blueOffset,
        int whiteOffset,
        int panOffset,
        int tiltOffset,
        int zoomOffset,
        int strobeOffset,
        int goboOffset
) {

    /**
     * Common RGB fixture:
     *
     * 1 Red
     * 2 Green
     * 3 Blue
     * 4 Dimmer
     */
    public static final FixtureChannelLayout RGB =
            new FixtureChannelLayout(
                    4,
                    3,
                    0,
                    1,
                    2,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1
            );

    /**
     * Common RGBW fixture:
     *
     * 1 Red
     * 2 Green
     * 3 Blue
     * 4 White
     * 5 Dimmer
     */
    public static final FixtureChannelLayout RGBW =
            new FixtureChannelLayout(
                    5,
                    4,
                    0,
                    1,
                    2,
                    3,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1
            );

    /**
     * Single-channel dimmer fixture.
     */
    public static final FixtureChannelLayout DIMMER =
            new FixtureChannelLayout(
                    1,
                    0,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1,
                    -1
            );

    /**
     * Basic static RGB spotlight with zoom.
     *
     * 1 Dimmer
     * 2 Red
     * 3 Green
     * 4 Blue
     * 5 Zoom
     */
    public static final FixtureChannelLayout STATIC_SPOTLIGHT =
            new FixtureChannelLayout(
                    5,
                    0,
                    1,
                    2,
                    3,
                    -1,
                    -1,
                    -1,
                    4,
                    -1,
                    -1
            );

    /**
     * Basic moving-head wash.
     *
     * 1 Pan
     * 2 Tilt
     * 3 Dimmer
     * 4 Red
     * 5 Green
     * 6 Blue
     * 7 White
     * 8 Zoom
     * 9 Strobe
     */
    public static final FixtureChannelLayout MOVING_HEAD_WASH =
            new FixtureChannelLayout(
                    9,
                    2,
                    3,
                    4,
                    5,
                    6,
                    0,
                    1,
                    7,
                    8,
                    -1
            );

    /**
     * Basic moving-head spot.
     *
     * 1 Pan
     * 2 Tilt
     * 3 Dimmer
     * 4 Red
     * 5 Green
     * 6 Blue
     * 7 Zoom
     * 8 Strobe
     * 9 Gobo
     */
    public static final FixtureChannelLayout MOVING_HEAD_SPOT =
            new FixtureChannelLayout(
                    9,
                    2,
                    3,
                    4,
                    5,
                    -1,
                    0,
                    1,
                    6,
                    7,
                    8
            );

    public FixtureChannelLayout {
        if (channelCount < 1) {
            throw new IllegalArgumentException(
                    "channelCount must be at least 1"
            );
        }

        validateOffset("dimmerOffset", dimmerOffset, channelCount);
        validateOffset("redOffset", redOffset, channelCount);
        validateOffset("greenOffset", greenOffset, channelCount);
        validateOffset("blueOffset", blueOffset, channelCount);
        validateOffset("whiteOffset", whiteOffset, channelCount);
        validateOffset("panOffset", panOffset, channelCount);
        validateOffset("tiltOffset", tiltOffset, channelCount);
        validateOffset("zoomOffset", zoomOffset, channelCount);
        validateOffset("strobeOffset", strobeOffset, channelCount);
        validateOffset("goboOffset", goboOffset, channelCount);
    }

    /**
     * Returns the absolute DMX channel for an offset.
     *
     * Returns -1 when the requested control is unsupported.
     */
    public int absoluteChannel(
            int baseChannel,
            int offset
    ) {
        if (offset < 0) {
            return -1;
        }

        return baseChannel + offset;
    }

    public int dimmerChannel(int baseChannel) {
        return absoluteChannel(baseChannel, dimmerOffset);
    }

    public int redChannel(int baseChannel) {
        return absoluteChannel(baseChannel, redOffset);
    }

    public int greenChannel(int baseChannel) {
        return absoluteChannel(baseChannel, greenOffset);
    }

    public int blueChannel(int baseChannel) {
        return absoluteChannel(baseChannel, blueOffset);
    }

    public int whiteChannel(int baseChannel) {
        return absoluteChannel(baseChannel, whiteOffset);
    }

    public int panChannel(int baseChannel) {
        return absoluteChannel(baseChannel, panOffset);
    }

    public int tiltChannel(int baseChannel) {
        return absoluteChannel(baseChannel, tiltOffset);
    }

    public int zoomChannel(int baseChannel) {
        return absoluteChannel(baseChannel, zoomOffset);
    }

    public int strobeChannel(int baseChannel) {
        return absoluteChannel(baseChannel, strobeOffset);
    }

    public int goboChannel(int baseChannel) {
        return absoluteChannel(baseChannel, goboOffset);
    }

    public boolean hasDimmer() {
        return dimmerOffset >= 0;
    }

    public boolean hasRgb() {
        return redOffset >= 0
                && greenOffset >= 0
                && blueOffset >= 0;
    }

    public boolean hasWhite() {
        return whiteOffset >= 0;
    }

    public boolean hasPan() {
        return panOffset >= 0;
    }

    public boolean hasTilt() {
        return tiltOffset >= 0;
    }

    public boolean hasZoom() {
        return zoomOffset >= 0;
    }

    public boolean hasStrobe() {
        return strobeOffset >= 0;
    }

    public boolean hasGobo() {
        return goboOffset >= 0;
    }

    /**
     * Returns the final DMX channel occupied by this fixture.
     */
    public int finalChannel(int baseChannel) {
        return baseChannel + channelCount - 1;
    }

    /**
     * Returns true when the complete fixture footprint fits inside
     * one standard 512-channel DMX universe.
     */
    public boolean fitsInUniverse(int baseChannel) {
        return baseChannel >= 1
                && finalChannel(baseChannel) <= 512;
    }

    private static void validateOffset(
            String name,
            int offset,
            int channelCount
    ) {
        if (offset < -1) {
            throw new IllegalArgumentException(
                    name + " cannot be less than -1"
            );
        }

        if (offset >= channelCount) {
            throw new IllegalArgumentException(
                    name
                            + " must be smaller than channelCount"
            );
        }
    }
}