package dmx.lighting;

/**
 * Stores explicit DMX channel assignments for one fixture.
 *
 * This is the foundation of the new parameter-centric patching model.
 *
 * Instead of assigning one base channel and deriving parameter channels
 * from a fixed fixture profile, each controllable parameter stores its
 * own absolute DMX channel within the fixture's selected universe.
 *
 * Channel rules:
 *
 * - 0 means "unassigned"
 * - 1 through 512 are valid DMX channels
 *
 * This class deliberately contains only channel assignments.
 *
 * It does not:
 *
 * - Read DMX values
 * - Store fixture output values
 * - Store the universe
 * - Decide which renderer/model is used
 * - Apply color mixing
 *
 * Those responsibilities remain elsewhere.
 */
public final class FixtureParameterMap {

    /*
     * -----------------------------------------------------------------
     * DMX channel limits
     * -----------------------------------------------------------------
     */

    /**
     * Zero represents an unassigned parameter.
     */
    public static final int UNASSIGNED =
            0;

    /**
     * Lowest valid DMX channel.
     */
    public static final int MIN_CHANNEL =
            1;

    /**
     * Highest valid DMX channel.
     */
    public static final int MAX_CHANNEL =
            512;

    /*
     * -----------------------------------------------------------------
     * Color parameters
     * -----------------------------------------------------------------
     */

    private int redChannel;
    private int greenChannel;
    private int blueChannel;
    private int whiteChannel;
    private int amberChannel;

    /*
     * -----------------------------------------------------------------
     * Intensity / effects
     * -----------------------------------------------------------------
     */

    private int dimmerChannel;
    private int strobeChannel;

    /*
     * -----------------------------------------------------------------
     * Position
     * -----------------------------------------------------------------
     */

    private int panChannel;
    private int tiltChannel;

    /*
     * -----------------------------------------------------------------
     * Beam
     * -----------------------------------------------------------------
     */

    private int beamWidthChannel;
    private int beamLengthChannel;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    /**
     * Creates a parameter map with every parameter unassigned.
     */
    public FixtureParameterMap() {
    }

    /**
     * Creates a complete parameter map.
     */
    public FixtureParameterMap(
            int redChannel,
            int greenChannel,
            int blueChannel,
            int whiteChannel,
            int amberChannel,
            int dimmerChannel,
            int strobeChannel,
            int panChannel,
            int tiltChannel,
            int beamWidthChannel,
            int beamLengthChannel
    ) {
        set(
                redChannel,
                greenChannel,
                blueChannel,
                whiteChannel,
                amberChannel,
                dimmerChannel,
                strobeChannel,
                panChannel,
                tiltChannel,
                beamWidthChannel,
                beamLengthChannel
        );
    }

    /*
     * -----------------------------------------------------------------
     * Complete assignment
     * -----------------------------------------------------------------
     */

    /**
     * Sets every parameter channel at once.
     */
    public void set(
            int redChannel,
            int greenChannel,
            int blueChannel,
            int whiteChannel,
            int amberChannel,
            int dimmerChannel,
            int strobeChannel,
            int panChannel,
            int tiltChannel,
            int beamWidthChannel,
            int beamLengthChannel
    ) {
        setRedChannel(
                redChannel
        );

        setGreenChannel(
                greenChannel
        );

        setBlueChannel(
                blueChannel
        );

        setWhiteChannel(
                whiteChannel
        );

        setAmberChannel(
                amberChannel
        );

        setDimmerChannel(
                dimmerChannel
        );

        setStrobeChannel(
                strobeChannel
        );

        setPanChannel(
                panChannel
        );

        setTiltChannel(
                tiltChannel
        );

        setBeamWidthChannel(
                beamWidthChannel
        );

        setBeamLengthChannel(
                beamLengthChannel
        );
    }

    /**
     * Clears every parameter assignment.
     */
    public void clear() {
        set(
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED,
                UNASSIGNED
        );
    }

    /*
     * -----------------------------------------------------------------
     * Color accessors
     * -----------------------------------------------------------------
     */

    public int getRedChannel() {
        return redChannel;
    }

    public void setRedChannel(
            int redChannel
    ) {
        this.redChannel =
                normalizeChannel(
                        redChannel
                );
    }

    public int getGreenChannel() {
        return greenChannel;
    }

    public void setGreenChannel(
            int greenChannel
    ) {
        this.greenChannel =
                normalizeChannel(
                        greenChannel
                );
    }

    public int getBlueChannel() {
        return blueChannel;
    }

    public void setBlueChannel(
            int blueChannel
    ) {
        this.blueChannel =
                normalizeChannel(
                        blueChannel
                );
    }

    public int getWhiteChannel() {
        return whiteChannel;
    }

    public void setWhiteChannel(
            int whiteChannel
    ) {
        this.whiteChannel =
                normalizeChannel(
                        whiteChannel
                );
    }

    public int getAmberChannel() {
        return amberChannel;
    }

    public void setAmberChannel(
            int amberChannel
    ) {
        this.amberChannel =
                normalizeChannel(
                        amberChannel
                );
    }

    /*
     * -----------------------------------------------------------------
     * Intensity / effects accessors
     * -----------------------------------------------------------------
     */

    public int getDimmerChannel() {
        return dimmerChannel;
    }

    public void setDimmerChannel(
            int dimmerChannel
    ) {
        this.dimmerChannel =
                normalizeChannel(
                        dimmerChannel
                );
    }

    public int getStrobeChannel() {
        return strobeChannel;
    }

    public void setStrobeChannel(
            int strobeChannel
    ) {
        this.strobeChannel =
                normalizeChannel(
                        strobeChannel
                );
    }

    /*
     * -----------------------------------------------------------------
     * Position accessors
     * -----------------------------------------------------------------
     */

    public int getPanChannel() {
        return panChannel;
    }

    public void setPanChannel(
            int panChannel
    ) {
        this.panChannel =
                normalizeChannel(
                        panChannel
                );
    }

    public int getTiltChannel() {
        return tiltChannel;
    }

    public void setTiltChannel(
            int tiltChannel
    ) {
        this.tiltChannel =
                normalizeChannel(
                        tiltChannel
                );
    }

    /*
     * -----------------------------------------------------------------
     * Beam accessors
     * -----------------------------------------------------------------
     */

    public int getBeamWidthChannel() {
        return beamWidthChannel;
    }

    public void setBeamWidthChannel(
            int beamWidthChannel
    ) {
        this.beamWidthChannel =
                normalizeChannel(
                        beamWidthChannel
                );
    }

    public int getBeamLengthChannel() {
        return beamLengthChannel;
    }

    public void setBeamLengthChannel(
            int beamLengthChannel
    ) {
        this.beamLengthChannel =
                normalizeChannel(
                        beamLengthChannel
                );
    }

    /*
     * -----------------------------------------------------------------
     * Assignment helpers
     * -----------------------------------------------------------------
     */

    public boolean hasRed() {
        return isAssigned(
                redChannel
        );
    }

    public boolean hasGreen() {
        return isAssigned(
                greenChannel
        );
    }

    public boolean hasBlue() {
        return isAssigned(
                blueChannel
        );
    }

    public boolean hasRgb() {
        return hasRed()
                || hasGreen()
                || hasBlue();
    }

    public boolean hasWhite() {
        return isAssigned(
                whiteChannel
        );
    }

    public boolean hasAmber() {
        return isAssigned(
                amberChannel
        );
    }

    public boolean hasDimmer() {
        return isAssigned(
                dimmerChannel
        );
    }

    public boolean hasStrobe() {
        return isAssigned(
                strobeChannel
        );
    }

    public boolean hasPan() {
        return isAssigned(
                panChannel
        );
    }

    public boolean hasTilt() {
        return isAssigned(
                tiltChannel
        );
    }

    public boolean hasBeamWidth() {
        return isAssigned(
                beamWidthChannel
        );
    }

    public boolean hasBeamLength() {
        return isAssigned(
                beamLengthChannel
        );
    }

    /**
     * Returns true if at least one parameter is assigned.
     */
    public boolean hasAnyAssignment() {
        return hasRed()
                || hasGreen()
                || hasBlue()
                || hasWhite()
                || hasAmber()
                || hasDimmer()
                || hasStrobe()
                || hasPan()
                || hasTilt()
                || hasBeamWidth()
                || hasBeamLength();
    }

    /*
     * -----------------------------------------------------------------
     * Static helpers
     * -----------------------------------------------------------------
     */

    /**
     * Returns true when a channel is a valid assigned DMX channel.
     */
    public static boolean isAssigned(
            int channel
    ) {
        return channel >= MIN_CHANNEL
                && channel <= MAX_CHANNEL;
    }

    /**
     * Converts invalid channel values to UNASSIGNED.
     *
     * This makes UI and persistence handling forgiving:
     *
     * - negative values become 0
     * - values above 512 become 0
     * - valid 1-512 values are retained
     */
    public static int normalizeChannel(
            int channel
    ) {
        if (!isAssigned(
                channel
        )) {
            return UNASSIGNED;
        }

        return channel;
    }
}