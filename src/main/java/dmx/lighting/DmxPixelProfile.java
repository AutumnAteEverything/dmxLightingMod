package dmx.lighting;

/**
 * Control profile used by the full-cube DMX Block.
 *
 * The block supports RGB, a master dimmer, and strobe. It has no
 * movement, beam, white, amber, or gobo controls.
 */
public final class DmxPixelProfile
        extends AbstractFixtureProfile {

    public static final String ID =
            "dmx_pixel";

    public static final DmxPixelProfile INSTANCE =
            new DmxPixelProfile();

    private DmxPixelProfile() {
        super(
                ID,
                "DMX Block",
                new FixtureCapabilities(
                        true,
                        false,
                        true,
                        false,
                        false,
                        false,
                        true,
                        false,
                        false
                ),
                new FixtureChannelLayout(
                        5,
                        3,
                        0,
                        1,
                        2,
                        -1,
                        -1,
                        -1,
                        -1,
                        4,
                        -1
                )
        );
    }
}
