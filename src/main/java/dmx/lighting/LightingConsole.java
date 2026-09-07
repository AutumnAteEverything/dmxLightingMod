package dmx.lighting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * High-level access point for the dmxLighting system.
 *
 * The fixture registry is responsible for tracking which fixtures are
 * currently loaded.
 *
 * LightingConsole is responsible for presenting, filtering, sorting,
 * and eventually controlling those fixtures.
 *
 * Future systems using this class may include:
 *
 * - Fixture Browser
 * - Group Manager
 * - Patch View
 * - Universe Monitor
 * - In-world Console Block
 * - Cue Lists
 * - Effects
 * - Fixture Selection
 *
 * This class does not store its own fixture collection. It always asks
 * DmxFixtureRegistry for the current loaded fixtures.
 *
 * In the parameter-centric architecture, patch occupancy is based on
 * each fixture's explicit FixtureParameterMap assignments rather than
 * a legacy contiguous base-address footprint.
 */
public final class LightingConsole {

    /**
     * Available sorting modes for fixture lists.
     */
    public enum FixtureSortMode {

        /**
         * Sort alphabetically by fixture name.
         */
        NAME,

        /**
         * Sort alphabetically by group, then fixture name.
         */
        GROUP,

        /**
         * Sort by universe, then lowest assigned DMX channel.
         */
        PATCH,

        /**
         * Sort by fixture profile, then fixture name.
         */
        PROFILE,

        /**
         * Sort by control mode, then fixture name.
         */
        MODE,

        /**
         * Sort by X, then Y, then Z block position.
         */
        POSITION
    }

    private LightingConsole() {
        // Utility class: do not instantiate.
    }

    /**
     * Returns browser entries for every loaded fixture in one
     * dimension.
     *
     * Entries are sorted by DMX patch by default.
     */
    public static List<FixtureBrowserEntry> getAllFixtures(
            ResourceKey<Level> dimension
    ) {
        return getAllFixtures(
                dimension,
                FixtureSortMode.PATCH
        );
    }

    /**
     * Returns browser entries for every loaded fixture in one
     * dimension using the requested sorting mode.
     */
    public static List<FixtureBrowserEntry> getAllFixtures(
            ResourceKey<Level> dimension,
            FixtureSortMode sortMode
    ) {
        List<DmxFixtureBlockEntity> blockFixtures =
                DmxFixtureRegistry.getFixturesInDimension(
                        dimension
                );

        List<DmxParrotEntity> parrots =
                DmxMobFixtureRegistry.getParrotsInDimension(
                        dimension
                );

        List<DmxEndermanEntity> endermen =
                DmxMobFixtureRegistry.getEndermenInDimension(
                        dimension
                );

        List<DmxWardenEntity> wardens =
                DmxMobFixtureRegistry.getWardensInDimension(
                        dimension
                );

        return createSortedEntries(
                blockFixtures,
                parrots,
                endermen,
                wardens,
                sortMode
        );
    }

    /**
     * Returns browser entries for every loaded fixture across every
     * dimension.
     *
     * This is mainly intended for server administration and debugging.
     * Most player-facing interfaces should remain within the player's
     * current dimension.
     */
    public static List<FixtureBrowserEntry> getAllFixturesAcrossDimensions(
            FixtureSortMode sortMode
    ) {
        return createSortedEntries(
                DmxFixtureRegistry.getAllFixtures(),
                DmxMobFixtureRegistry.getAllParrots(),
                DmxMobFixtureRegistry.getAllEndermen(),
                DmxMobFixtureRegistry.getAllWardens(),
                sortMode
        );
    }

    /**
     * Returns every loaded fixture in one named group.
     *
     * Group names are normalized by FixtureGroupName, so capitalization
     * and repeated spaces do not affect matching.
     */
    public static List<FixtureBrowserEntry> getFixturesByGroup(
            ResourceKey<Level> dimension,
            String groupName
    ) {
        return getFixturesByGroup(
                dimension,
                groupName,
                FixtureSortMode.PATCH
        );
    }

    /**
     * Returns every loaded fixture in one named group using the
     * requested sorting mode.
     */
    public static List<FixtureBrowserEntry> getFixturesByGroup(
            ResourceKey<Level> dimension,
            String groupName,
            FixtureSortMode sortMode
    ) {
        FixtureGroupName group =
                FixtureGroupName.of(
                        groupName
                );

        List<DmxFixtureBlockEntity> blockFixtures =
                DmxFixtureRegistry.getFixturesInGroup(
                        dimension,
                        group
                );

        List<DmxParrotEntity> parrots =
                DmxMobFixtureRegistry.getParrotsInGroup(
                        dimension,
                        group
                );

        List<DmxEndermanEntity> endermen =
                DmxMobFixtureRegistry.getEndermenInGroup(
                        dimension,
                        group
                );

        List<DmxWardenEntity> wardens =
                DmxMobFixtureRegistry.getWardensInGroup(
                        dimension,
                        group
                );

        return createSortedEntries(
                blockFixtures,
                parrots,
                endermen,
                wardens,
                sortMode
        );
    }

    /**
     * Returns every loaded fixture using one universe.
     */
    public static List<FixtureBrowserEntry> getFixturesByUniverse(
            ResourceKey<Level> dimension,
            int universe
    ) {
        return getFixturesByUniverse(
                dimension,
                universe,
                FixtureSortMode.PATCH
        );
    }

    /**
     * Returns every loaded fixture using one universe with the
     * requested sorting mode.
     */
    public static List<FixtureBrowserEntry> getFixturesByUniverse(
            ResourceKey<Level> dimension,
            int universe,
            FixtureSortMode sortMode
    ) {
        List<DmxFixtureBlockEntity> blockFixtures =
                DmxFixtureRegistry.getFixturesInUniverse(
                        dimension,
                        universe
                );

        List<DmxParrotEntity> parrots =
                DmxMobFixtureRegistry.getParrotsInUniverse(
                        dimension,
                        universe
                );

        List<DmxEndermanEntity> endermen =
                DmxMobFixtureRegistry.getEndermenInUniverse(
                        dimension,
                        universe
                );

        List<DmxWardenEntity> wardens =
                DmxMobFixtureRegistry.getWardensInUniverse(
                        dimension,
                        universe
                );

        return createSortedEntries(
                blockFixtures,
                parrots,
                endermen,
                wardens,
                sortMode
        );
    }

    /**
     * Searches loaded fixtures by fixture name, group name, fixture
     * profile, control mode, patch, or block position.
     *
     * Blank search text returns all fixtures.
     */
    public static List<FixtureBrowserEntry> searchFixtures(
            ResourceKey<Level> dimension,
            String searchText,
            FixtureSortMode sortMode
    ) {
        List<FixtureBrowserEntry> allEntries =
                getAllFixtures(
                        dimension,
                        sortMode
                );

        String query =
                normalizeSearchText(
                        searchText
                );

        if (query.isEmpty()) {
            return allEntries;
        }

        List<FixtureBrowserEntry> matches =
                new ArrayList<>();

        for (FixtureBrowserEntry entry :
                allEntries) {

            if (matchesSearch(
                    entry,
                    query
            )) {
                matches.add(
                        entry
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    /*
     * -----------------------------------------------------------------
     * Patch conflict detection
     * -----------------------------------------------------------------
     */

    /**
     * Returns true when two or more loaded fixtures in the requested
     * dimension share at least one explicitly assigned DMX channel in
     * the same universe.
     */
    public static boolean hasPatchConflicts(
            ResourceKey<Level> dimension
    ) {
        return !getPatchConflicts(
                dimension
        ).isEmpty();
    }

    /**
     * Returns every fixture participating in at least one patch
     * conflict.
     *
     * A conflict occurs when:
     *
     * - Two fixtures use the same universe, and
     * - At least one assigned parameter channel is shared.
     *
     * Each fixture appears at most once in the returned list.
     */
    public static List<FixtureBrowserEntry> getPatchConflicts(
            ResourceKey<Level> dimension
    ) {
        List<DmxFixtureBlockEntity> fixtures =
                DmxFixtureRegistry.getFixturesInDimension(
                        dimension
                );

        List<DmxFixtureBlockEntity> conflictingFixtures =
                new ArrayList<>();

        for (
                int firstIndex = 0;
                firstIndex < fixtures.size();
                firstIndex++
        ) {
            DmxFixtureBlockEntity first =
                    fixtures.get(
                            firstIndex
                    );

            if (first == null
                    || first.isRemoved()) {

                continue;
            }

            for (
                    int secondIndex = firstIndex + 1;
                    secondIndex < fixtures.size();
                    secondIndex++
            ) {
                DmxFixtureBlockEntity second =
                        fixtures.get(
                                secondIndex
                        );

                if (second == null
                        || second.isRemoved()) {

                    continue;
                }

                if (!fixturesOverlap(
                        first,
                        second
                )) {
                    continue;
                }

                if (!conflictingFixtures.contains(
                        first
                )) {
                    conflictingFixtures.add(
                            first
                    );
                }

                if (!conflictingFixtures.contains(
                        second
                )) {
                    conflictingFixtures.add(
                            second
                    );
                }
            }
        }

        return createSortedEntries(
                conflictingFixtures,
                List.of(),
                List.of(),
                List.of(),
                FixtureSortMode.PATCH
        );
    }

    /**
     * Returns true when two fixtures share at least one explicitly
     * assigned channel in the same universe.
     */
    private static boolean fixturesOverlap(
            DmxFixtureBlockEntity first,
            DmxFixtureBlockEntity second
    ) {
        if (first == null
                || second == null) {

            return false;
        }

        if (first.getUniverse()
                != second.getUniverse()) {

            return false;
        }

        int[] firstChannels =
                getAssignedChannels(
                        first.getParameterMap()
                );

        int[] secondChannels =
                getAssignedChannels(
                        second.getParameterMap()
                );

        for (int firstChannel :
                firstChannels) {

            if (!FixtureParameterMap.isAssigned(
                    firstChannel
            )) {
                continue;
            }

            for (int secondChannel :
                    secondChannels) {

                if (firstChannel == secondChannel
                        && FixtureParameterMap.isAssigned(
                                secondChannel
                        )) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns all parameter assignments from one map.
     *
     * Duplicate channels are intentionally allowed in this temporary
     * array because conflict detection only needs to know whether at
     * least one shared channel exists.
     */
    private static int[] getAssignedChannels(
            FixtureParameterMap map
    ) {
        if (map == null) {
            return new int[0];
        }

        return new int[] {
                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getWhiteChannel(),
                map.getAmberChannel(),
                map.getDimmerChannel(),
                map.getPanChannel(),
                map.getTiltChannel(),
                map.getBeamWidthChannel(),
                map.getBeamLengthChannel(),
                map.getStrobeChannel()
        };
    }

    /*
     * -----------------------------------------------------------------
     * Groups and counts
     * -----------------------------------------------------------------
     */

    /**
     * Returns every currently represented non-empty group name in one
     * dimension, sorted alphabetically.
     */
    public static List<String> getGroupNames(
            ResourceKey<Level> dimension
    ) {
        List<String> groupNames =
                new ArrayList<>();

        for (FixtureGroupName group :
                DmxFixtureRegistry.getGroupsInDimension(
                        dimension
                )) {

            groupNames.add(
                    group.displayName()
            );
        }

        groupNames.sort(
                String.CASE_INSENSITIVE_ORDER
        );

        return List.copyOf(
                groupNames
        );
    }

    /**
     * Returns the number of loaded fixtures in one dimension.
     */
    public static int getFixtureCount(
            ResourceKey<Level> dimension
    ) {
        return DmxFixtureRegistry.getFixtureCount(
                dimension
        );
    }

    /**
     * Returns the number of non-empty named groups represented in one
     * dimension.
     */
    public static int getGroupCount(
            ResourceKey<Level> dimension
    ) {
        return DmxFixtureRegistry
                .getGroupsInDimension(
                        dimension
                )
                .size();
    }

    /*
     * -----------------------------------------------------------------
     * Browser-entry creation and sorting
     * -----------------------------------------------------------------
     */

    /**
     * Converts block entities to immutable browser entries and applies
     * the requested sorting mode.
     */
    private static List<FixtureBrowserEntry> createSortedEntries(
            List<DmxFixtureBlockEntity> fixtures,
            List<DmxParrotEntity> parrots,
            List<DmxEndermanEntity> endermen,
            List<DmxWardenEntity> wardens,
            FixtureSortMode sortMode
    ) {
        List<FixtureBrowserEntry> entries =
                new ArrayList<>();

        if (fixtures != null) {
            for (DmxFixtureBlockEntity fixture :
                    fixtures) {

                if (fixture != null
                        && !fixture.isRemoved()) {

                    entries.add(
                            FixtureBrowserEntry.fromFixture(
                                    fixture
                            )
                    );
                }
            }
        }

        if (parrots != null) {
            for (DmxParrotEntity parrot :
                    parrots) {

                if (parrot != null
                        && !parrot.isRemoved()) {

                    entries.add(
                            FixtureBrowserEntry.fromDmxParrot(
                                    parrot
                            )
                    );
                }
            }
        }

        if (endermen != null) {
            for (DmxEndermanEntity enderman :
                    endermen) {

                if (enderman != null
                        && !enderman.isRemoved()) {

                    entries.add(
                            FixtureBrowserEntry.fromDmxEnderman(
                                    enderman
                            )
                    );
                }
            }
        }

        if (wardens != null) {
            for (DmxWardenEntity warden :
                    wardens) {

                if (warden != null
                        && !warden.isRemoved()) {

                    entries.add(
                            FixtureBrowserEntry.fromDmxWarden(
                                    warden
                            )
                    );
                }
            }
        }

        FixtureSortMode safeSortMode =
                sortMode == null
                        ? FixtureSortMode.PATCH
                        : sortMode;

        entries.sort(
                getComparator(
                        safeSortMode
                )
        );

        return List.copyOf(
                entries
        );
    }

    /**
     * Returns the comparator used for one fixture sorting mode.
     */
    private static Comparator<FixtureBrowserEntry> getComparator(
            FixtureSortMode sortMode
    ) {
        Comparator<FixtureBrowserEntry> byName =
                Comparator.comparing(
                        FixtureBrowserEntry::fixtureName,
                        String.CASE_INSENSITIVE_ORDER
                );

        Comparator<FixtureBrowserEntry> byPosition =
                Comparator
                        .comparingInt(
                                (FixtureBrowserEntry entry) ->
                                        entry.position().getX()
                        )
                        .thenComparingInt(
                                entry ->
                                        entry.position().getY()
                        )
                        .thenComparingInt(
                                entry ->
                                        entry.position().getZ()
                        );

        return switch (sortMode) {
            case NAME ->
                    byName
                            .thenComparing(
                                    byPosition
                            );

            case GROUP ->
                    Comparator
                            .comparing(
                                    FixtureBrowserEntry::groupName,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                            .thenComparing(
                                    byName
                            )
                            .thenComparing(
                                    byPosition
                            );

            case PATCH ->
                    Comparator
                            .comparingInt(
                                    FixtureBrowserEntry::universe
                            )
                            .thenComparingInt(
                                    FixtureBrowserEntry::firstChannel
                            )
                            .thenComparing(
                                    byName
                            )
                            .thenComparing(
                                    byPosition
                            );

            case PROFILE ->
                    Comparator
                            .comparing(
                                    FixtureBrowserEntry::fixtureType,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                            .thenComparing(
                                    byName
                            )
                            .thenComparing(
                                    byPosition
                            );

            case MODE ->
                    Comparator
                            .comparing(
                                    FixtureBrowserEntry::controlMode,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                            .thenComparing(
                                    byName
                            )
                            .thenComparing(
                                    byPosition
                            );

            case POSITION ->
                    byPosition
                            .thenComparing(
                                    byName
                            );
        };
    }

    /*
     * -----------------------------------------------------------------
     * Search
     * -----------------------------------------------------------------
     */

    /**
     * Checks one browser entry against normalized search text.
     */
    private static boolean matchesSearch(
            FixtureBrowserEntry entry,
            String query
    ) {
        return containsNormalized(
                entry.fixtureName(),
                query
        )
                || containsNormalized(
                entry.groupName(),
                query
        )
                || containsNormalized(
                entry.fixtureType(),
                query
        )
                || containsNormalized(
                entry.controlMode(),
                query
        )
                || containsNormalized(
                entry.getPatchLabel(),
                query
        )
                || containsNormalized(
                entry.getPositionLabel(),
                query
        );
    }

    private static boolean containsNormalized(
            String text,
            String query
    ) {
        return normalizeSearchText(
                text
        ).contains(
                query
        );
    }

    private static String normalizeSearchText(
            String text
    ) {
        if (text == null) {
            return "";
        }

        return text
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }
}
