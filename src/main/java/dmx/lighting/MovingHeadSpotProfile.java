package dmx.lighting;

/**
 * Moving-head spotlight fixture.
 *
 * This profile represents an intelligent spotlight capable of
 * movement and future beam rendering.
 *
 * Planned renderer features:
 *
 * • Pan
 * • Tilt
 * • Zoom
 * • Strobe
 * • Gobos
 * • Volumetric beam
 * • Surface hotspot
 */
public final class MovingHeadSpotProfile
        extends AbstractFixtureProfile {

    public static final MovingHeadSpotProfile INSTANCE =
            new MovingHeadSpotProfile();

    private MovingHeadSpotProfile() {

        super(
                "moving_head_spot",
                "Moving Head Spot",
                FixtureCapabilities.MOVING_HEAD_SPOT,
                FixtureChannelLayout.MOVING_HEAD_SPOT
        );
    }

    public String description() {
        return "Moving spotlight with pan, tilt, zoom and gobos";
    }

    public String defaultFixtureName() {
        return "Moving Head";
    }
}