package dmx.lighting;

import java.util.Arrays;

/**
 * Stores the current values of one DMX universe.
 *
 * A standard DMX universe contains 512 channels.
 *
 * User-facing channel numbers are 1 through 512.
 * Internally, the Java array uses indexes 0 through 511.
 */
public final class DmxUniverse {

    public static final int MIN_CHANNEL = 1;
    public static final int MAX_CHANNEL = 512;

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 255;

    private final int universeNumber;
    private final int[] channels;

    /**
     * Creates an empty DMX universe.
     *
     * Every channel begins at zero.
     *
     * @param universeNumber universe number, beginning at 1
     */
    public DmxUniverse(int universeNumber) {
        if (universeNumber < 1) {
            throw new IllegalArgumentException(
                    "DMX universe numbers must be 1 or greater."
            );
        }

        this.universeNumber = universeNumber;
        this.channels = new int[MAX_CHANNEL];
    }

    public int getUniverseNumber() {
        return universeNumber;
    }

    /**
     * Returns the current value of a channel.
     *
     * @param channel channel number from 1 through 512
     * @return value from 0 through 255
     */
    public int getChannel(int channel) {
        validateChannel(channel);

        return channels[channel - 1];
    }

    /**
     * Updates one DMX channel.
     *
     * @param channel channel number from 1 through 512
     * @param value value from 0 through 255
     */
    public void setChannel(int channel, int value) {
        validateChannel(channel);
        validateValue(value);

        channels[channel - 1] = value;
    }

    /**
     * Sets every channel in the universe to zero.
     */
    public void blackout() {
        Arrays.fill(channels, 0);
    }

    /**
     * Returns a copy of all 512 channel values.
     *
     * A copy is returned so outside code cannot modify the universe
     * without using setChannel().
     */
    public int[] copyChannels() {
        return Arrays.copyOf(channels, channels.length);
    }

    private static void validateChannel(int channel) {
        if (channel < MIN_CHANNEL || channel > MAX_CHANNEL) {
            throw new IllegalArgumentException(
                    "DMX channel must be between "
                            + MIN_CHANNEL
                            + " and "
                            + MAX_CHANNEL
                            + "."
            );
        }
    }

    private static void validateValue(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException(
                    "DMX value must be between "
                            + MIN_VALUE
                            + " and "
                            + MAX_VALUE
                            + "."
            );
        }
    }
}