package dmx.lighting;

/** Three-channel profile for the cubic DMX Disco Ball. */
public final class DmxDiscoBallProfile
        extends AbstractFixtureProfile {

    public static final String ID =
            "dmx_disco_ball";

    public static final DmxDiscoBallProfile INSTANCE =
            new DmxDiscoBallProfile();

    private DmxDiscoBallProfile() {
        super(
                ID,
                "DMX Disco Ball",
                new FixtureCapabilities(
                        false,
                        false,
                        true,
                        true,
                        false,
                        false,
                        true,
                        false,
                        false
                ),
                new FixtureChannelLayout(
                        3,
                        0,
                        -1,
                        -1,
                        -1,
                        -1,
                        1,
                        -1,
                        -1,
                        2,
                        -1
                )
        );
    }
}
