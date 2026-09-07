package dmx.lighting;

/**
 * DMX-controlled parrot profile.
 *
 * First-pass layout:
 *
 * 1 Red
 * 2 Green
 * 3 Blue
 * 4 Dimmer
 */
public final class DmxParrotProfile
        extends AbstractFixtureProfile {

    public static final DmxParrotProfile INSTANCE =
            new DmxParrotProfile();

    public static final String ID =
            "dmx_parrot";

    private DmxParrotProfile() {
        super(
                ID,
                "DMX Parrot",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }
}
