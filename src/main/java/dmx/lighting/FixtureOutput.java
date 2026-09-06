package dmx.lighting;

import java.util.Objects;

/**
 * Stores one complete fixture output.
 *
 * New parameter-centric values:
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
 * Every controllable value is stored as a standard eight-bit DMX value
 * from 0 through 255.
 *
 * Beam Width and Beam Length therefore store raw DMX parameter values.
 * Conversion into visual degrees / blocks happens later in the render
 * or fixture-control layer.
 *
 * Compatibility:
 *
 * The old Zoom API temporarily aliases Beam Width.
 *
 * The old Gobo value is temporarily retained as legacy data so existing
 * networking, persistence, and manual controls continue compiling while
 * the rest of the project is migrated.
 *
 * Once the parameter-centric migration is complete, the compatibility
 * Zoom/Gobo API can be removed.
 */
public final class FixtureOutput {

    public static final int MIN_VALUE =
            0;

    public static final int MAX_VALUE =
            255;

    /*
     * -----------------------------------------------------------------
     * Amber visual mixing
     * -----------------------------------------------------------------
     *
     * Minecraft ultimately renders RGB light, so the dedicated amber
     * emitter needs an RGB approximation.
     *
     * These values can easily be tuned later.
     */

    private static final float AMBER_RED_MULTIPLIER =
            1.00F;

    private static final float AMBER_GREEN_MULTIPLIER =
            0.65F;

    private static final float AMBER_BLUE_MULTIPLIER =
            0.10F;

    /*
     * -----------------------------------------------------------------
     * Common outputs
     * -----------------------------------------------------------------
     */

    /**
     * Completely dark and neutral output.
     */
    public static final FixtureOutput BLACKOUT =
            new FixtureOutput(
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
            );

    /**
     * Full RGB white at full dimmer.
     *
     * Dedicated White and Amber remain zero.
     */
    public static final FixtureOutput FULL_WHITE =
            new FixtureOutput(
                    255,
                    255,
                    255,
                    0,
                    0,
                    255,
                    0,
                    0,
                    0,
                    0,
                    0
            );

    /*
     * -----------------------------------------------------------------
     * Color
     * -----------------------------------------------------------------
     */

    private int red;
    private int green;
    private int blue;
    private int white;
    private int amber;

    /*
     * -----------------------------------------------------------------
     * Intensity / effects
     * -----------------------------------------------------------------
     */

    private int dimmer;
    private int strobe;

    /*
     * -----------------------------------------------------------------
     * Position
     * -----------------------------------------------------------------
     */

    private int pan;
    private int tilt;

    /*
     * -----------------------------------------------------------------
     * Beam
     * -----------------------------------------------------------------
     */

    private int beamWidth;
    private int beamLength;

    /*
     * -----------------------------------------------------------------
     * Temporary legacy data
     * -----------------------------------------------------------------
     */

    /**
     * Retained only while the old Gobo API is being removed from the
     * rest of the project.
     */
    private int legacyGobo;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    /**
     * Creates a completely blacked-out fixture output.
     */
    public FixtureOutput() {
        this(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }

    /**
     * Compatibility constructor for the original RGBD model.
     */
    public FixtureOutput(
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        this(
                red,
                green,
                blue,
                0,
                0,
                dimmer,
                0,
                0,
                0,
                0,
                0
        );
    }

    /**
     * New parameter-centric constructor.
     */
    public FixtureOutput(
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
        set(
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
                strobe
        );

        legacyGobo =
                0;
    }

    /**
     * Compatibility constructor for the previous fixture architecture.
     *
     * Old:
     *
     *     Zoom -> Beam Width
     *     Gobo -> temporary legacy Gobo storage
     *
     * Amber and Beam Length did not previously exist and therefore
     * initialize to zero.
     */
    public FixtureOutput(
            int red,
            int green,
            int blue,
            int white,
            int dimmer,
            int pan,
            int tilt,
            int zoom,
            int strobe,
            int gobo
    ) {
        this(
                red,
                green,
                blue,
                white,
                0,
                dimmer,
                pan,
                tilt,
                zoom,
                0,
                strobe
        );

        legacyGobo =
                clamp(
                        gobo
                );
    }

    /*
     * -----------------------------------------------------------------
     * Color accessors
     * -----------------------------------------------------------------
     */

    public int getRed() {
        return red;
    }

    public void setRed(
            int red
    ) {
        this.red =
                clamp(
                        red
                );
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(
            int green
    ) {
        this.green =
                clamp(
                        green
                );
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(
            int blue
    ) {
        this.blue =
                clamp(
                        blue
                );
    }

    public int getWhite() {
        return white;
    }

    public void setWhite(
            int white
    ) {
        this.white =
                clamp(
                        white
                );
    }

    public int getAmber() {
        return amber;
    }

    public void setAmber(
            int amber
    ) {
        this.amber =
                clamp(
                        amber
                );
    }

    /*
     * -----------------------------------------------------------------
     * Intensity / effects
     * -----------------------------------------------------------------
     */

    public int getDimmer() {
        return dimmer;
    }

    public void setDimmer(
            int dimmer
    ) {
        this.dimmer =
                clamp(
                        dimmer
                );
    }

    public int getStrobe() {
        return strobe;
    }

    public void setStrobe(
            int strobe
    ) {
        this.strobe =
                clamp(
                        strobe
                );
    }

    /*
     * -----------------------------------------------------------------
     * Position
     * -----------------------------------------------------------------
     */

    public int getPan() {
        return pan;
    }

    public void setPan(
            int pan
    ) {
        this.pan =
                clamp(
                        pan
                );
    }

    public int getTilt() {
        return tilt;
    }

    public void setTilt(
            int tilt
    ) {
        this.tilt =
                clamp(
                        tilt
                );
    }

    /*
     * -----------------------------------------------------------------
     * Beam controls
     * -----------------------------------------------------------------
     */

    /**
     * Returns the raw DMX Beam Width parameter from 0 through 255.
     */
    public int getBeamWidth() {
        return beamWidth;
    }

    public void setBeamWidth(
            int beamWidth
    ) {
        this.beamWidth =
                clamp(
                        beamWidth
                );
    }

    /**
     * Returns the raw DMX Beam Length parameter from 0 through 255.
     */
    public int getBeamLength() {
        return beamLength;
    }

    public void setBeamLength(
            int beamLength
    ) {
        this.beamLength =
                clamp(
                        beamLength
                );
    }

    /*
     * -----------------------------------------------------------------
     * Temporary compatibility API
     * -----------------------------------------------------------------
     */

    /**
     * Compatibility alias.
     *
     * Legacy Zoom now represents the new Beam Width parameter.
     */
    public int getZoom() {
        return getBeamWidth();
    }

    /**
     * Compatibility alias.
     *
     * Legacy Zoom now represents the new Beam Width parameter.
     */
    public void setZoom(
            int zoom
    ) {
        setBeamWidth(
                zoom
        );
    }

    /**
     * Temporary legacy Gobo accessor.
     *
     * Remove after networking, persistence, UI, and render state no
     * longer reference Gobo.
     */
    public int getGobo() {
        return legacyGobo;
    }

    /**
     * Temporary legacy Gobo setter.
     */
    public void setGobo(
            int gobo
    ) {
        legacyGobo =
                clamp(
                        gobo
                );
    }

    /*
     * -----------------------------------------------------------------
     * Bulk updates
     * -----------------------------------------------------------------
     */

    /**
     * Compatibility method for original RGBD output.
     *
     * All other values reset to zero.
     */
    public void set(
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        set(
                red,
                green,
                blue,
                0,
                0,
                dimmer,
                0,
                0,
                0,
                0,
                0
        );

        legacyGobo =
                0;
    }

    /**
     * Sets every parameter in the new architecture.
     */
    public void set(
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
        setRed(
                red
        );

        setGreen(
                green
        );

        setBlue(
                blue
        );

        setWhite(
                white
        );

        setAmber(
                amber
        );

        setDimmer(
                dimmer
        );

        setPan(
                pan
        );

        setTilt(
                tilt
        );

        setBeamWidth(
                beamWidth
        );

        setBeamLength(
                beamLength
        );

        setStrobe(
                strobe
        );
    }

    /**
     * Compatibility bulk update for the previous architecture.
     *
     * Zoom is transferred into Beam Width.
     *
     * Amber and Beam Length reset to zero because there were no legacy
     * equivalents.
     */
    public void set(
            int red,
            int green,
            int blue,
            int white,
            int dimmer,
            int pan,
            int tilt,
            int zoom,
            int strobe,
            int gobo
    ) {
        set(
                red,
                green,
                blue,
                white,
                0,
                dimmer,
                pan,
                tilt,
                zoom,
                0,
                strobe
        );

        setGobo(
                gobo
        );
    }

    /**
     * Changes Color + Dimmer using the new RGBWA model.
     */
    public void setColorAndDimmer(
            int red,
            int green,
            int blue,
            int white,
            int amber,
            int dimmer
    ) {
        setRed(
                red
        );

        setGreen(
                green
        );

        setBlue(
                blue
        );

        setWhite(
                white
        );

        setAmber(
                amber
        );

        setDimmer(
                dimmer
        );
    }

    /**
     * Compatibility overload for the old RGBW + Dimmer model.
     *
     * Amber is preserved.
     */
    public void setColorAndDimmer(
            int red,
            int green,
            int blue,
            int white,
            int dimmer
    ) {
        setRed(
                red
        );

        setGreen(
                green
        );

        setBlue(
                blue
        );

        setWhite(
                white
        );

        setDimmer(
                dimmer
        );
    }

    /**
     * Changes position and beam parameters.
     */
    public void setPositionAndBeam(
            int pan,
            int tilt,
            int beamWidth,
            int beamLength,
            int strobe
    ) {
        setPan(
                pan
        );

        setTilt(
                tilt
        );

        setBeamWidth(
                beamWidth
        );

        setBeamLength(
                beamLength
        );

        setStrobe(
                strobe
        );
    }

    /**
     * Compatibility method for the previous advanced-control model.
     *
     * Zoom maps to Beam Width.
     */
    public void setAdvancedControls(
            int pan,
            int tilt,
            int zoom,
            int strobe,
            int gobo
    ) {
        setPan(
                pan
        );

        setTilt(
                tilt
        );

        setBeamWidth(
                zoom
        );

        setStrobe(
                strobe
        );

        setGobo(
                gobo
        );
    }

    /**
     * Copies all values from another FixtureOutput.
     *
     * Passing null applies blackout.
     */
    public void set(
            FixtureOutput other
    ) {
        if (other == null) {
            blackout();
            return;
        }

        set(
                other.red,
                other.green,
                other.blue,
                other.white,
                other.amber,
                other.dimmer,
                other.pan,
                other.tilt,
                other.beamWidth,
                other.beamLength,
                other.strobe
        );

        legacyGobo =
                other.legacyGobo;
    }

    /*
     * -----------------------------------------------------------------
     * Common states
     * -----------------------------------------------------------------
     */

    /**
     * Sets every parameter to zero.
     */
    public void blackout() {
        set(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        );

        legacyGobo =
                0;
    }

    /**
     * Full RGB white with full Dimmer.
     */
    public void fullWhite() {
        set(
                255,
                255,
                255,
                0,
                0,
                255,
                0,
                0,
                0,
                0,
                0
        );

        legacyGobo =
                0;
    }

    /**
     * Dedicated White emitter at full intensity.
     */
    public void fullDedicatedWhite() {
        set(
                0,
                0,
                0,
                255,
                0,
                255,
                0,
                0,
                0,
                0,
                0
        );

        legacyGobo =
                0;
    }

    /**
     * Dedicated Amber emitter at full intensity.
     */
    public void fullAmber() {
        set(
                0,
                0,
                0,
                0,
                255,
                255,
                0,
                0,
                0,
                0,
                0
        );

        legacyGobo =
                0;
    }

    /**
     * Returns true when no visible color can be produced.
     */
    public boolean isBlackout() {
        return dimmer == 0
                || (
                red == 0
                        && green == 0
                        && blue == 0
                        && white == 0
                        && amber == 0
        );
    }

    /*
     * -----------------------------------------------------------------
     * Visible color calculations
     * -----------------------------------------------------------------
     */

    /**
     * Returns undimmed Red after White and Amber emitters are mixed in.
     */
    public int getCombinedRed() {
        return clamp(
                red
                        + white
                        + Math.round(
                        amber
                                * AMBER_RED_MULTIPLIER
                )
        );
    }

    /**
     * Returns undimmed Green after White and Amber emitters are mixed
     * in.
     */
    public int getCombinedGreen() {
        return clamp(
                green
                        + white
                        + Math.round(
                        amber
                                * AMBER_GREEN_MULTIPLIER
                )
        );
    }

    /**
     * Returns undimmed Blue after White and Amber emitters are mixed in.
     */
    public int getCombinedBlue() {
        return clamp(
                blue
                        + white
                        + Math.round(
                        amber
                                * AMBER_BLUE_MULTIPLIER
                )
        );
    }

    public int getOutputRed() {
        return applyDimmer(
                getCombinedRed(),
                dimmer
        );
    }

    public int getOutputGreen() {
        return applyDimmer(
                getCombinedGreen(),
                dimmer
        );
    }

    public int getOutputBlue() {
        return applyDimmer(
                getCombinedBlue(),
                dimmer
        );
    }

    public int getOutputWhite() {
        return applyDimmer(
                white,
                dimmer
        );
    }

    public int getOutputAmber() {
        return applyDimmer(
                amber,
                dimmer
        );
    }

    /**
     * Returns the brightest effective RGB component.
     */
    public int getMaximumOutputComponent() {
        return Math.max(
                getOutputRed(),
                Math.max(
                        getOutputGreen(),
                        getOutputBlue()
                )
        );
    }

    /**
     * Converts fixture brightness to Minecraft's 0-15 block-light
     * scale.
     */
    public int getMinecraftLightLevel() {
        return Math.round(
                getMaximumOutputComponent()
                        * (
                        15.0F / 255.0F
                )
        );
    }

    /**
     * Returns final visible color as 0xRRGGBB.
     */
    public int getPackedRgb() {
        return (
                getOutputRed()
                        << 16
        )
                | (
                getOutputGreen()
                        << 8
        )
                | getOutputBlue();
    }

    /**
     * Returns undimmed RGBWA-mixed color as 0xRRGGBB.
     */
    public int getRawPackedRgb() {
        return (
                getCombinedRed()
                        << 16
        )
                | (
                getCombinedGreen()
                        << 8
        )
                | getCombinedBlue();
    }

    /**
     * Returns raw RGB only.
     *
     * White and Amber are excluded.
     */
    public int getRawRgbWithoutWhite() {
        return (
                red
                        << 16
        )
                | (
                green
                        << 8
        )
                | blue;
    }

    /*
     * -----------------------------------------------------------------
     * Copy / interpolation
     * -----------------------------------------------------------------
     */

    public FixtureOutput copy() {
        FixtureOutput copy =
                new FixtureOutput(
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
                        strobe
                );

        copy.legacyGobo =
                legacyGobo;

        return copy;
    }

    /**
     * Interpolates every new parameter.
     */
    public FixtureOutput interpolate(
            FixtureOutput target,
            float progress
    ) {
        FixtureOutput safeTarget =
                target == null
                        ? BLACKOUT
                        : target;

        float safeProgress =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                progress
                        )
                );

        FixtureOutput result =
                new FixtureOutput(
                        interpolateChannel(
                                red,
                                safeTarget.red,
                                safeProgress
                        ),
                        interpolateChannel(
                                green,
                                safeTarget.green,
                                safeProgress
                        ),
                        interpolateChannel(
                                blue,
                                safeTarget.blue,
                                safeProgress
                        ),
                        interpolateChannel(
                                white,
                                safeTarget.white,
                                safeProgress
                        ),
                        interpolateChannel(
                                amber,
                                safeTarget.amber,
                                safeProgress
                        ),
                        interpolateChannel(
                                dimmer,
                                safeTarget.dimmer,
                                safeProgress
                        ),
                        interpolateChannel(
                                pan,
                                safeTarget.pan,
                                safeProgress
                        ),
                        interpolateChannel(
                                tilt,
                                safeTarget.tilt,
                                safeProgress
                        ),
                        interpolateChannel(
                                beamWidth,
                                safeTarget.beamWidth,
                                safeProgress
                        ),
                        interpolateChannel(
                                beamLength,
                                safeTarget.beamLength,
                                safeProgress
                        ),
                        interpolateChannel(
                                strobe,
                                safeTarget.strobe,
                                safeProgress
                        )
                );

        result.legacyGobo =
                interpolateChannel(
                        legacyGobo,
                        safeTarget.legacyGobo,
                        safeProgress
                );

        return result;
    }

    /*
     * -----------------------------------------------------------------
     * Static utilities
     * -----------------------------------------------------------------
     */

    public static int applyDimmer(
            int channel,
            int dimmer
    ) {
        int safeChannel =
                clamp(
                        channel
                );

        int safeDimmer =
                clamp(
                        dimmer
                );

        return Math.round(
                safeChannel
                        * (
                        safeDimmer / 255.0F
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Object methods
     * -----------------------------------------------------------------
     */

    @Override
    public boolean equals(
            Object object
    ) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof FixtureOutput other)) {
            return false;
        }

        return red == other.red
                && green == other.green
                && blue == other.blue
                && white == other.white
                && amber == other.amber
                && dimmer == other.dimmer
                && pan == other.pan
                && tilt == other.tilt
                && beamWidth == other.beamWidth
                && beamLength == other.beamLength
                && strobe == other.strobe
                && legacyGobo == other.legacyGobo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
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
                legacyGobo
        );
    }

    @Override
    public String toString() {
        return "FixtureOutput{"
                + "red="
                + red
                + ", green="
                + green
                + ", blue="
                + blue
                + ", white="
                + white
                + ", amber="
                + amber
                + ", dimmer="
                + dimmer
                + ", pan="
                + pan
                + ", tilt="
                + tilt
                + ", beamWidth="
                + beamWidth
                + ", beamLength="
                + beamLength
                + ", strobe="
                + strobe
                + ", legacyGobo="
                + legacyGobo
                + '}';
    }

    /*
     * -----------------------------------------------------------------
     * Internal utilities
     * -----------------------------------------------------------------
     */

    private static int interpolateChannel(
            int start,
            int end,
            float progress
    ) {
        return clamp(
                Math.round(
                        start
                                + (
                                end - start
                        )
                                * progress
                )
        );
    }

    private static int clamp(
            int value
    ) {
        return Math.max(
                MIN_VALUE,
                Math.min(
                        MAX_VALUE,
                        value
                )
        );
    }
}