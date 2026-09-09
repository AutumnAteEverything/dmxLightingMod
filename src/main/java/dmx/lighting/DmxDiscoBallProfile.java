package dmx.lighting;

/** Two-channel profile for the cubic DMX Disco Ball. */
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
                        false,
                        false,
                        false
                ),
                new FixtureChannelLayout(
                        2,
                        0,
                        -1,
                        -1,
                        -1,
                        -1,
                        1,
                        -1,
                        -1,
                        -1,
                        -1
                )
        );
    }
}
