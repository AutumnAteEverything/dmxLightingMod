package dmx.lighting;

/**
 * Standard RGB PAR fixture.
 *
 * This is the reference implementation for all future fixture
 * profiles.
 *
 * DMX Layout
 * ----------
 *
 * Channel 1 : Red
 * Channel 2 : Green
 * Channel 3 : Blue
 * Channel 4 : Dimmer
 *
 * Features
 * --------
 *
 * • RGB color mixing
 * • Master dimmer
 * • No movement
 * • No zoom
 * • No strobe
 * • No gobo
 * • No directional beam
 */
public final class RgbParProfile
        extends AbstractFixtureProfile {

    /**
     * Singleton instance.
     *
     * Fixture profiles are immutable, so only one instance is needed.
     */
    public static final RgbParProfile INSTANCE =
            new RgbParProfile();

    /**
     * Prevent external construction.
     */
    private RgbParProfile() {

        super(
                "rgb_par",
                "RGB PAR",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }

    /**
     * Returns a short human-readable description.
     *
     * This will later be useful in the fixture browser and profile
     * picker.
     */
    public String description() {

        return "Standard RGB PAR fixture";
    }

    /**
     * Default fixture name when a new fixture of this profile is
     * created.
     */
    public String defaultFixtureName() {

        return "RGB PAR";
    }
}