package dmx.lighting;

/**
 * DMX-controlled Enderman profile.
 *
 * First-pass layout:
 *
 * 1 Red
 * 2 Green
 * 3 Blue
 * 4 Dimmer
 */
public final class DmxEndermanProfile
        extends AbstractFixtureProfile {

    public static final DmxEndermanProfile INSTANCE =
            new DmxEndermanProfile();

    public static final String ID =
            "dmx_enderman";

    private DmxEndermanProfile() {
        super(
                ID,
                "DMX Enderman",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }
}
