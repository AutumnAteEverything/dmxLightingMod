package dmx.lighting;

/** Control profile for the summonable DMX Block Display entity. */
public final class DmxBlockDisplayProfile extends AbstractFixtureProfile {

    public static final String ID = "dmx_block_display";

    public static final DmxBlockDisplayProfile INSTANCE =
            new DmxBlockDisplayProfile();

    private DmxBlockDisplayProfile() {
        super(
                ID,
                "DMX Block Display",
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
