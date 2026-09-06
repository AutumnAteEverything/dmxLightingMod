package dmx.lighting;

/**
 * Standard four-channel RGBD fixture profile.
 *
 * Channel layout:
 *
 * Base + 0 = Red
 * Base + 1 = Green
 * Base + 2 = Blue
 * Base + 3 = Dimmer
 */
public final class RgbdFixtureProfile
        implements FixtureProfile {

    public static final RgbdFixtureProfile INSTANCE =
            new RgbdFixtureProfile();

    public static final String ID = "rgbd";

    private static final int CHANNEL_COUNT = 4;

    private RgbdFixtureProfile() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "RGB + Dimmer";
    }

    @Override
    public int getChannelCount() {
        return CHANNEL_COUNT;
    }

    @Override
    public int getRed(
            DmxUniverse universe,
            int baseChannel
    ) {
        return universe.getChannel(baseChannel);
    }

    @Override
    public int getGreen(
            DmxUniverse universe,
            int baseChannel
    ) {
        return universe.getChannel(baseChannel + 1);
    }

    @Override
    public int getBlue(
            DmxUniverse universe,
            int baseChannel
    ) {
        return universe.getChannel(baseChannel + 2);
    }

    @Override
    public int getDimmer(
            DmxUniverse universe,
            int baseChannel
    ) {
        return universe.getChannel(baseChannel + 3);
    }
}