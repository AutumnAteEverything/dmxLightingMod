package dmx.lighting;

import java.util.Objects;

/**
 * Stores the physical installation orientation of a fixture.
 *
 * These values describe how the fixture is mounted in the world. They
 * are separate from live DMX-controlled pan and tilt values.
 *
 * Examples:
 *
 * - Hanging PAR
 * - Floor uplight
 * - Wall-mounted spotlight
 * - Pedestal fixture
 * - Side-mounted fixture
 *
 * Both axes support complete 360-degree rotation.
 */
public final class FixtureMountOrientation {

    public static final float DEFAULT_PAN_DEGREES =
            0.0F;

    public static final float DEFAULT_TILT_DEGREES =
            0.0F;

    private float panDegrees;
    private float tiltDegrees;

    /**
     * Creates the default installation orientation.
     */
    public FixtureMountOrientation() {
        this(
                DEFAULT_PAN_DEGREES,
                DEFAULT_TILT_DEGREES
        );
    }

    /**
     * Creates an installation orientation with explicit angles.
     */
    public FixtureMountOrientation(
            float panDegrees,
            float tiltDegrees
    ) {
        set(
                panDegrees,
                tiltDegrees
        );
    }

    /**
     * Returns the installation pan angle from 0 up to 360 degrees.
     */
    public float getPanDegrees() {
        return panDegrees;
    }

    /**
     * Changes the installation pan angle.
     *
     * Values automatically wrap into the 0-360 degree range.
     */
    public void setPanDegrees(
            float panDegrees
    ) {
        this.panDegrees =
                normalizeDegrees(
                        panDegrees
                );
    }

    /**
     * Returns the installation tilt angle from 0 up to 360 degrees.
     */
    public float getTiltDegrees() {
        return tiltDegrees;
    }

    /**
     * Changes the installation tilt angle.
     *
     * Values automatically wrap into the 0-360 degree range.
     */
    public void setTiltDegrees(
            float tiltDegrees
    ) {
        this.tiltDegrees =
                normalizeDegrees(
                        tiltDegrees
                );
    }

    /**
     * Changes both installation angles at once.
     */
    public void set(
            float panDegrees,
            float tiltDegrees
    ) {
        setPanDegrees(
                panDegrees
        );

        setTiltDegrees(
                tiltDegrees
        );
    }

    /**
     * Restores the default installation orientation.
     */
    public void reset() {
        set(
                DEFAULT_PAN_DEGREES,
                DEFAULT_TILT_DEGREES
        );
    }

    /**
     * Returns true when no installation rotation is applied.
     */
    public boolean isDefaultOrientation() {
        return Float.compare(
                panDegrees,
                DEFAULT_PAN_DEGREES
        ) == 0
                && Float.compare(
                tiltDegrees,
                DEFAULT_TILT_DEGREES
        ) == 0;
    }

    /**
     * Creates an independent copy.
     */
    public FixtureMountOrientation copy() {
        return new FixtureMountOrientation(
                panDegrees,
                tiltDegrees
        );
    }

    @Override
    public boolean equals(
            Object object
    ) {
        if (this == object) {
            return true;
        }

        if (!(object
                instanceof FixtureMountOrientation other)) {

            return false;
        }

        return Float.compare(
                panDegrees,
                other.panDegrees
        ) == 0
                && Float.compare(
                tiltDegrees,
                other.tiltDegrees
        ) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                panDegrees,
                tiltDegrees
        );
    }

    @Override
    public String toString() {
        return "FixtureMountOrientation{"
                + "panDegrees="
                + panDegrees
                + ", tiltDegrees="
                + tiltDegrees
                + '}';
    }

    /**
     * Wraps an angle into the range 0 inclusive through 360 exclusive.
     */
    private static float normalizeDegrees(
            float degrees
    ) {
        if (!Float.isFinite(
                degrees
        )) {
            return 0.0F;
        }

        float normalized =
                degrees
                        % 360.0F;

        if (normalized < 0.0F) {
            normalized +=
                    360.0F;
        }

        /*
         * Avoid retaining negative zero.
         */
        if (normalized == -0.0F) {
            return 0.0F;
        }

        return normalized;
    }
}