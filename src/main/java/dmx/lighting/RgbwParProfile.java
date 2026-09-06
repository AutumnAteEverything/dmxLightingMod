package dmx.lighting;

/**
 * Standard RGBW PAR fixture.
 *
 * DMX Layout
 * ----------
 *
 * Channel 1 : Red
 * Channel 2 : Green
 * Channel 3 : Blue
 * Channel 4 : White
 * Channel 5 : Dimmer
 */
public final class RgbwParProfile
        extends AbstractFixtureProfile {

    public static final RgbwParProfile INSTANCE =
            new RgbwParProfile();

    private RgbwParProfile() {
        super(
                "rgbw_par",
                "RGBW PAR",
                FixtureCapabilities.RGBW_PAR,
                FixtureChannelLayout.RGBW
        );
    }

    public String description() {
        return "Standard RGBW PAR fixture";
    }

    public String defaultFixtureName() {
        return "RGBW PAR";
    }
}