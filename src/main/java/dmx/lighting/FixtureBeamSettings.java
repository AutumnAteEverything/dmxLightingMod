package dmx.lighting;

import java.util.Objects;

/**
 * Stores visualization settings for one fixture's beam.
 *
 * Beam width is stored as a user-facing control value.
 *
 * The stored width range is:
 *
 * 45 through 150
 *
 * That stored value is mapped onto the actual rendered full cone-angle
 * range:
 *
 * 8 through 90 degrees
 *
 * Beam length is stored directly in Minecraft blocks.
 *
 * Keeping the mapping here ensures that:
 *
 * - The visible beam renderer
 * - World illumination
 * - Future fixture previews
 *
 * all use the same cone geometry.
 */
public final class FixtureBeamSettings {

    /*
     * -----------------------------------------------------------------
     * Stored beam-width control range
     * -----------------------------------------------------------------
     */

    public static final int MIN_WIDTH_DEGREES =
            45;

    public static final int MAX_WIDTH_DEGREES =
            150;

    public static final int DEFAULT_WIDTH_DEGREES =
            60;

    /*
     * -----------------------------------------------------------------
     * Actual visual cone-angle range
     * -----------------------------------------------------------------
     */

    /**
     * Narrowest rendered full cone angle.
     */
    public static final float MIN_VISUAL_CONE_DEGREES =
            1.0F;

    /**
     * Widest rendered full cone angle.
     */
    public static final float MAX_VISUAL_CONE_DEGREES =
            150.0F;

    /*
     * -----------------------------------------------------------------
     * Beam length
     * -----------------------------------------------------------------
     */

    public static final int MIN_LENGTH_BLOCKS =
            1;

    public static final int MAX_LENGTH_BLOCKS =
            25;

    public static final int DEFAULT_LENGTH_BLOCKS =
            6;

    private int widthDegrees;
    private int lengthBlocks;

    /**
     * Creates the default beam settings.
     */
    public FixtureBeamSettings() {
        this(
                DEFAULT_WIDTH_DEGREES,
                DEFAULT_LENGTH_BLOCKS
        );
    }

    /**
     * Creates beam settings with explicit stored values.
     */
    public FixtureBeamSettings(
            int widthDegrees,
            int lengthBlocks
    ) {
        set(
                widthDegrees,
                lengthBlocks
        );
    }

    /*
     * -----------------------------------------------------------------
     * Stored width control
     * -----------------------------------------------------------------
     */

    /**
     * Returns the stored beam-width control value.
     *
     * This is not the literal rendered cone angle.
     */
    public int getWidthDegrees() {
        return widthDegrees;
    }

    /**
     * Sets the stored beam-width control value.
     */
    public void setWidthDegrees(
            int widthDegrees
    ) {
        this.widthDegrees =
                clamp(
                        widthDegrees,
                        MIN_WIDTH_DEGREES,
                        MAX_WIDTH_DEGREES
                );
    }

    /*
     * -----------------------------------------------------------------
     * Visual cone-angle mapping
     * -----------------------------------------------------------------
     */

    /**
     * Returns the actual rendered full cone angle for this beam.
     */
    public float getVisualConeDegrees() {
        return mapWidthToVisualConeDegrees(
                widthDegrees
        );
    }

    /**
     * Maps one stored beam-width value onto the actual rendered full
     * cone angle.
     *
     * Stored:
     *
     * 45 -> 8 degrees
     * 150 -> 90 degrees
     */
    public static float mapWidthToVisualConeDegrees(
            int widthDegrees
    ) {
        int clampedWidth =
                clamp(
                        widthDegrees,
                        MIN_WIDTH_DEGREES,
                        MAX_WIDTH_DEGREES
                );

        float normalized =
                (
                        clampedWidth
                                - MIN_WIDTH_DEGREES
                )
                        / (float) (
                        MAX_WIDTH_DEGREES
                                - MIN_WIDTH_DEGREES
                );

        return MIN_VISUAL_CONE_DEGREES
                + normalized
                * (
                MAX_VISUAL_CONE_DEGREES
                        - MIN_VISUAL_CONE_DEGREES
        );
    }

    /**
     * Returns the half-angle used by cone intersection calculations.
     */
    public static float getHalfVisualConeDegrees(
            int widthDegrees
    ) {
        return mapWidthToVisualConeDegrees(
                widthDegrees
        )
                / 2.0F;
    }

    /*
     * -----------------------------------------------------------------
     * Beam length
     * -----------------------------------------------------------------
     */

    /**
     * Returns the beam length in Minecraft blocks.
     */
    public int getLengthBlocks() {
        return lengthBlocks;
    }

    /**
     * Sets the beam length in Minecraft blocks.
     */
    public void setLengthBlocks(
            int lengthBlocks
    ) {
        this.lengthBlocks =
                clamp(
                        lengthBlocks,
                        MIN_LENGTH_BLOCKS,
                        MAX_LENGTH_BLOCKS
                );
    }

    /*
     * -----------------------------------------------------------------
     * Bulk updates
     * -----------------------------------------------------------------
     */

    /**
     * Changes both beam settings.
     */
    public void set(
            int widthDegrees,
            int lengthBlocks
    ) {
        setWidthDegrees(
                widthDegrees
        );

        setLengthBlocks(
                lengthBlocks
        );
    }

    /**
     * Restores the default beam settings.
     */
    public void reset() {
        set(
                DEFAULT_WIDTH_DEGREES,
                DEFAULT_LENGTH_BLOCKS
        );
    }

    /**
     * Returns true when both values use their defaults.
     */
    public boolean isDefault() {
        return widthDegrees
                == DEFAULT_WIDTH_DEGREES
                && lengthBlocks
                == DEFAULT_LENGTH_BLOCKS;
    }

    /**
     * Creates an independent copy.
     */
    public FixtureBeamSettings copy() {
        return new FixtureBeamSettings(
                widthDegrees,
                lengthBlocks
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

        if (!(object
                instanceof FixtureBeamSettings other)) {

            return false;
        }

        return widthDegrees
                == other.widthDegrees
                && lengthBlocks
                == other.lengthBlocks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                widthDegrees,
                lengthBlocks
        );
    }

    @Override
    public String toString() {
        return "FixtureBeamSettings{"
                + "widthDegrees="
                + widthDegrees
                + ", visualConeDegrees="
                + getVisualConeDegrees()
                + ", lengthBlocks="
                + lengthBlocks
                + '}';
    }

    /*
     * -----------------------------------------------------------------
     * Internal utilities
     * -----------------------------------------------------------------
     */

    private static int clamp(
            int value,
            int minimum,
            int maximum
    ) {
        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }
}