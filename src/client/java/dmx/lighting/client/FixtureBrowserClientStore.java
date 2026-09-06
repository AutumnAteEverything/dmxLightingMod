package dmx.lighting.client;

import dmx.lighting.FixtureBrowserEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Client-side storage for the most recently received Fixture Browser
 * data.
 *
 * The server remains authoritative. This class only retains immutable
 * FixtureBrowserEntry snapshots sent by FixtureBrowserDataPayload.
 *
 * The store also supports one optional update listener. The future
 * Lighting Console screen can use that listener to refresh its table
 * whenever new fixture data arrives.
 */
public final class FixtureBrowserClientStore {

    /**
     * Most recently received fixture list.
     *
     * Always immutable and never null.
     */
    private static List<FixtureBrowserEntry> entries =
            List.of();

    /**
     * Incremented whenever new browser data is stored.
     *
     * This gives screens another simple way to determine whether the
     * data changed since their previous refresh.
     */
    private static long revision = 0L;

    /**
     * Optional listener used by the currently open browser screen.
     */
    private static Consumer<List<FixtureBrowserEntry>> updateListener;

    private FixtureBrowserClientStore() {
        // Utility class: do not instantiate.
    }

    /**
     * Replaces the current fixture-browser snapshot.
     *
     * Null entries are removed and an immutable copy is stored.
     */
    public static void setEntries(
            List<FixtureBrowserEntry> newEntries
    ) {
        entries = sanitizeEntries(newEntries);
        revision++;

        Consumer<List<FixtureBrowserEntry>> listener =
                updateListener;

        if (listener != null) {
            listener.accept(entries);
        }
    }

    /**
     * Returns the current immutable fixture list.
     */
    public static List<FixtureBrowserEntry> getEntries() {
        return entries;
    }

    /**
     * Returns the current data revision.
     */
    public static long getRevision() {
        return revision;
    }

    /**
     * Returns the number of fixtures in the current snapshot.
     */
    public static int getFixtureCount() {
        return entries.size();
    }

    /**
     * Returns true when the current browser snapshot is empty.
     */
    public static boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Finds one fixture entry by block position coordinates.
     *
     * A direct BlockPos comparison could also be used, but this method
     * avoids retaining any caller-owned position object.
     */
    public static FixtureBrowserEntry findByPosition(
            int x,
            int y,
            int z
    ) {
        for (FixtureBrowserEntry entry : entries) {
            if (entry.position().getX() == x
                    && entry.position().getY() == y
                    && entry.position().getZ() == z) {

                return entry;
            }
        }

        return null;
    }

    /**
     * Registers one listener that will be called whenever new fixture
     * data arrives.
     *
     * Registering a new listener replaces the previous one.
     */
    public static void setUpdateListener(
            Consumer<List<FixtureBrowserEntry>> listener
    ) {
        updateListener = listener;
    }

    /**
     * Removes the listener only when it is the currently registered
     * listener.
     *
     * This prevents an older screen from accidentally clearing a
     * listener installed by a newer screen.
     */
    public static void clearUpdateListener(
            Consumer<List<FixtureBrowserEntry>> listener
    ) {
        if (updateListener == listener) {
            updateListener = null;
        }
    }

    /**
     * Clears all retained browser data.
     *
     * This will later be called when the client disconnects from a
     * server or leaves a world.
     */
    public static void clear() {
        entries = List.of();
        revision++;
        updateListener = null;
    }

    /**
     * Produces an immutable, null-free list.
     */
    private static List<FixtureBrowserEntry> sanitizeEntries(
            List<FixtureBrowserEntry> source
    ) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<FixtureBrowserEntry> safeEntries =
                new ArrayList<>(source.size());

        for (FixtureBrowserEntry entry : source) {
            if (entry != null) {
                safeEntries.add(entry);
            }
        }

        return List.copyOf(safeEntries);
    }
}