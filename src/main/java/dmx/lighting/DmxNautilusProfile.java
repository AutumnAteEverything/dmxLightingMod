package dmx.lighting;

/**
 * DMX-controlled Nautilus profile.
 *
 * First-pass layout:
 *
 * 1 Red
 * 2 Green
 * 3 Blue
 * 4 Dimmer
 */
public final class DmxNautilusProfile
        extends AbstractFixtureProfile {

    public static final DmxNautilusProfile INSTANCE =
            new DmxNautilusProfile();

    public static final String ID =
            "dmx_nautilus";

    private DmxNautilusProfile() {
        super(
                ID,
                "DMX Nautilus",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }
}
