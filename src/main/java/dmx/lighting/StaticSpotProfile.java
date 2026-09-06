package dmx.lighting;

/**
 * Spotlight fixture profile.
 *
 * This fixture is functionally identical to the PAR fixture.
 *
 * The profile selection determines only which physical model is
 * rendered in-game.
 *
 * PAR and Spotlight therefore share the same fixture capabilities and
 * legacy channel-layout definition.
 *
 * The parameter-centric fixture system independently provides:
 *
 * - Red
 * - Green
 * - Blue
 * - White
 * - Amber
 * - Dimmer
 * - Pan Offset
 * - Tilt Offset
 * - Beam Width
 * - Beam Length
 * - Strobe
 *
 * Mechanical rendering behavior:
 *
 * - The mounting base remains stationary.
 * - The support/yoke assembly follows Pan.
 * - The fixture head follows Pan + Tilt.
 * - The lens and visible beam follow the fixture head.
 *
 * The Spotlight differs from the PAR only in its rendered geometry:
 *
 * - Long narrow barrel
 * - Single front lens
 * - Traditional theatrical spotlight appearance
 */
public final class StaticSpotProfile
        extends AbstractFixtureProfile {

    /**
     * Singleton instance.
     */
    public static final StaticSpotProfile INSTANCE =
            new StaticSpotProfile();

    /**
     * Prevent external construction.
     */
    private StaticSpotProfile() {

        super(
                "static_spot",
                "Spotlight",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }

    /**
     * Short description for fixture browsers and profile selectors.
     */
    public String description() {

        return "Traditional long-barrel spotlight";
    }

    /**
     * Default name for a new Spotlight fixture.
     */
    public String defaultFixtureName() {

        return "Spotlight";
    }
}