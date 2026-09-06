package dmx.lighting;

/**
 * DMX-controlled Warden profile.
 *
 * First-pass layout:
 *
 * 1 Red
 * 2 Green
 * 3 Blue
 * 4 Dimmer
 */
public final class DmxWardenProfile
        extends AbstractFixtureProfile {

    public static final DmxWardenProfile INSTANCE =
            new DmxWardenProfile();

    public static final String ID =
            "dmx_warden";

    private DmxWardenProfile() {
        super(
                ID,
                "DMX Warden",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }
}
