package dmx.lighting;

/**
 * Describes the physical and control capabilities of a fixture profile.
 *
 * This class does not control fixtures directly. It allows fixture
 * profiles and future interfaces to determine which controls and
 * lighting effects should be available.
 *
 * Examples:
 *
 * RGB Par:
 *     RGB color, dimmer
 *
 * RGBW Par:
 *     RGB color, white, dimmer
 *
 * Static Spotlight:
 *     RGB color, dimmer, zoom, directional beam
 *
 * Moving Head Spot:
 *     RGB color, dimmer, pan, tilt, zoom, strobe, gobo,
 *     directional beam
 */
public record FixtureCapabilities(
        boolean redGreenBlue,
        boolean white,
        boolean dimmer,
        boolean pan,
        boolean tilt,
        boolean zoom,
        boolean strobe,
        boolean gobo,
        boolean directionalBeam
) {

    /**
     * Basic RGB fixture such as an RGB PAR.
     */
    public static final FixtureCapabilities RGB_PAR =
            new FixtureCapabilities(
                    true,
                    false,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
            );

    /**
     * RGBW fixture such as an RGBW PAR.
     */
    public static final FixtureCapabilities RGBW_PAR =
            new FixtureCapabilities(
                    true,
                    true,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
            );

    /**
     * Static spotlight with an adjustable beam width.
     */
    public static final FixtureCapabilities STATIC_SPOTLIGHT =
            new FixtureCapabilities(
                    true,
                    false,
                    true,
                    false,
                    false,
                    true,
                    false,
                    false,
                    true
            );

    /**
     * Moving-head wash fixture.
     */
    public static final FixtureCapabilities MOVING_HEAD_WASH =
            new FixtureCapabilities(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true
            );

    /**
     * Moving-head spotlight with gobo support.
     */
    public static final FixtureCapabilities MOVING_HEAD_SPOT =
            new FixtureCapabilities(
                    true,
                    false,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true
            );

    /**
     * Single-channel dimmer fixture.
     */
    public static final FixtureCapabilities DIMMER =
            new FixtureCapabilities(
                    false,
                    false,
                    true,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
            );

    /**
     * Returns true when the fixture can change its physical direction.
     */
    public boolean supportsMovement() {
        return pan || tilt;
    }

    /**
     * Returns true when the fixture uses spotlight-style beam rendering.
     */
    public boolean supportsBeamRendering() {
        return directionalBeam;
    }

    /**
     * Returns true when the fixture provides any form of color control.
     */
    public boolean supportsColor() {
        return redGreenBlue || white;
    }
}