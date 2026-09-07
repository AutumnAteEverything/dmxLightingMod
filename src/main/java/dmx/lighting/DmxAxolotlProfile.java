package dmx.lighting;

/**
 * DMX-controlled Axolotl profile.
 *
 * First-pass layout:
 *
 * 1 Red
 * 2 Green
 * 3 Blue
 * 4 Dimmer
 */
public final class DmxAxolotlProfile
        extends AbstractFixtureProfile {

    public static final DmxAxolotlProfile INSTANCE =
            new DmxAxolotlProfile();

    public static final String ID =
            "dmx_axolotl";

    private DmxAxolotlProfile() {
        super(
                ID,
                "DMX Axolotl",
                FixtureCapabilities.RGB_PAR,
                FixtureChannelLayout.RGB
        );
    }
}
