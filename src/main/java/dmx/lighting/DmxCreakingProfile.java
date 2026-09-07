package dmx.lighting;

/**
 * DMX-controlled Creaking profile.
 *
 * First-pass layout:
 *
 * 1 Red
 * 2 Green
 * 3 Blue
 * 4 Dimmer
 */
public final class DmxCreakingProfile
        extends AbstractFixtureProfile {

    public static final DmxCreakingProfile INSTANCE =
            new DmxCreakingProfile();

    public static final String ID =
            "dmx_creaking";

    private DmxCreakingProfile() {
        super(
                ID,
                "DMX Creaking",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }
}
