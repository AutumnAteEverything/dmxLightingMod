package dmx.lighting;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores all active DMX universes.
 *
 * Universes are created automatically the first time they are used.
 *
 * For example, the command:
 *
 * /dmx 3 100 255
 *
 * automatically creates universe 3 if it does not already exist.
 */
public final class DmxUniverseManager {

    public static final int MIN_UNIVERSE = 1;
    public static final int MAX_UNIVERSE = 9999;

    private static final Map<Integer, DmxUniverse> UNIVERSES =
            new HashMap<>();

    private DmxUniverseManager() {
        // Utility class: do not instantiate.
    }

    /**
     * Returns an existing universe or creates a new empty universe.
     */
    public static DmxUniverse getOrCreateUniverse(int universeNumber) {
        validateUniverseNumber(universeNumber);

        return UNIVERSES.computeIfAbsent(
                universeNumber,
                DmxUniverse::new
        );
    }

    /**
     * Returns a universe only if it already exists.
     *
     * @return the universe, or null when it has not been created
     */
    public static DmxUniverse getUniverse(int universeNumber) {
        validateUniverseNumber(universeNumber);

        return UNIVERSES.get(universeNumber);
    }

    /**
     * Updates one channel in one universe.
     */
    public static void setChannel(
            int universeNumber,
            int channel,
            int value
    ) {
        DmxUniverse universe =
                getOrCreateUniverse(universeNumber);

        universe.setChannel(channel, value);
    }

    /**
     * Gets one channel from one universe.
     *
     * Universes that have not been used yet are created automatically
     * with all channel values set to zero.
     */
    public static int getChannel(
            int universeNumber,
            int channel
    ) {
        DmxUniverse universe =
                getOrCreateUniverse(universeNumber);

        return universe.getChannel(channel);
    }

    /**
     * Sets every channel in one universe to zero.
     */
    public static void blackoutUniverse(int universeNumber) {
        DmxUniverse universe =
                getOrCreateUniverse(universeNumber);

        universe.blackout();
    }

    /**
     * Sets every channel in every active universe to zero.
     */
    public static void blackoutAll() {
        for (DmxUniverse universe : UNIVERSES.values()) {
            universe.blackout();
        }
    }

    /**
     * Returns the number of universes that have been created.
     */
    public static int getUniverseCount() {
        return UNIVERSES.size();
    }

    private static void validateUniverseNumber(int universeNumber) {
        if (universeNumber < MIN_UNIVERSE
                || universeNumber > MAX_UNIVERSE) {

            throw new IllegalArgumentException(
                    "DMX universe must be between "
                            + MIN_UNIVERSE
                            + " and "
                            + MAX_UNIVERSE
                            + "."
            );
        }
    }
}