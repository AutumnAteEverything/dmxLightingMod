package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central registry of loaded DMX fixtures.
 *
 * Fixtures are indexed by:
 *
 * - Minecraft dimension
 * - Block position
 *
 * The registry is intentionally server-oriented. It tracks only block
 * entities that are currently loaded.
 *
 * Future systems using this registry may include:
 *
 * - Named fixture groups
 * - Multi-fixture editing
 * - In-world controllers
 * - Fixture browsers
 * - Universe monitors
 * - Patch views
 * - Cue and effect systems
 */
public final class DmxFixtureRegistry {

    /**
     * Loaded fixtures grouped by dimension.
     */
    private static final Map<
            ResourceKey<Level>,
            Map<BlockPos, DmxFixtureBlockEntity>
    > FIXTURES_BY_DIMENSION = new HashMap<>();

    private DmxFixtureRegistry() {
        // Utility class: do not instantiate.
    }

    /**
     * Registers or replaces one loaded fixture.
     *
     * Client-side fixtures are ignored because server state is
     * authoritative.
     */
    public static synchronized void register(
            DmxFixtureBlockEntity fixture
    ) {
        if (!isUsableServerFixture(fixture)) {
            return;
        }

        Level level = fixture.getLevel();

        if (level == null) {
            return;
        }

        ResourceKey<Level> dimension =
                level.dimension();

        BlockPos position =
                fixture.getBlockPos().immutable();

        FIXTURES_BY_DIMENSION
                .computeIfAbsent(
                        dimension,
                        ignored -> new HashMap<>()
                )
                .put(
                        position,
                        fixture
                );
    }

    /**
     * Removes one fixture from the registry.
     */
    public static synchronized void unregister(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return;
        }

        Level level = fixture.getLevel();

        if (level == null) {
            return;
        }

        unregister(
                level.dimension(),
                fixture.getBlockPos()
        );
    }

    /**
     * Removes the fixture at one dimension and block position.
     */
    public static synchronized void unregister(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (dimension == null || position == null) {
            return;
        }

        Map<BlockPos, DmxFixtureBlockEntity> dimensionFixtures =
                FIXTURES_BY_DIMENSION.get(dimension);

        if (dimensionFixtures == null) {
            return;
        }

        dimensionFixtures.remove(position);

        if (dimensionFixtures.isEmpty()) {
            FIXTURES_BY_DIMENSION.remove(dimension);
        }
    }

    /**
     * Returns one loaded fixture by dimension and block position.
     */
    public static synchronized DmxFixtureBlockEntity getFixture(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (dimension == null || position == null) {
            return null;
        }

        Map<BlockPos, DmxFixtureBlockEntity> dimensionFixtures =
                FIXTURES_BY_DIMENSION.get(dimension);

        if (dimensionFixtures == null) {
            return null;
        }

        DmxFixtureBlockEntity fixture =
                dimensionFixtures.get(position);

        if (!isUsableServerFixture(fixture)) {
            dimensionFixtures.remove(position);

            if (dimensionFixtures.isEmpty()) {
                FIXTURES_BY_DIMENSION.remove(dimension);
            }

            return null;
        }

        return fixture;
    }

    /**
     * Returns every currently loaded fixture in one dimension.
     *
     * The returned list is independent of the registry and may be
     * safely iterated by the caller.
     */
    public static synchronized List<DmxFixtureBlockEntity>
    getFixturesInDimension(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return List.of();
        }

        Map<BlockPos, DmxFixtureBlockEntity> dimensionFixtures =
                FIXTURES_BY_DIMENSION.get(dimension);

        if (dimensionFixtures == null) {
            return List.of();
        }

        removeInvalidFixtures(
                dimension,
                dimensionFixtures
        );

        return List.copyOf(
                dimensionFixtures.values()
        );
    }

    /**
     * Returns every currently loaded fixture across all dimensions.
     */
    public static synchronized List<DmxFixtureBlockEntity>
    getAllFixtures() {
        List<DmxFixtureBlockEntity> fixtures =
                new ArrayList<>();

        List<ResourceKey<Level>> dimensions =
                new ArrayList<>(
                        FIXTURES_BY_DIMENSION.keySet()
                );

        for (ResourceKey<Level> dimension : dimensions) {
            fixtures.addAll(
                    getFixturesInDimension(dimension)
            );
        }

        return List.copyOf(fixtures);
    }

    /**
     * Returns all loaded fixtures in one named group and dimension.
     *
     * Group matching uses FixtureGroupName's normalized internal key,
     * so these all refer to the same group:
     *
     * Balcony
     * balcony
     * BALCONY
     */
    public static synchronized List<DmxFixtureBlockEntity>
    getFixturesInGroup(
            ResourceKey<Level> dimension,
            FixtureGroupName group
    ) {
        if (dimension == null
                || group == null
                || group.isUngrouped()) {

            return List.of();
        }

        String targetKey =
                group.key();

        List<DmxFixtureBlockEntity> matches =
                new ArrayList<>();

        for (DmxFixtureBlockEntity fixture
                : getFixturesInDimension(dimension)) {

            if (targetKey.equals(
                    fixture.getGroupKey()
            )) {
                matches.add(fixture);
            }
        }

        return List.copyOf(matches);
    }

    /**
     * Convenience overload accepting user-entered group text.
     */
    public static synchronized List<DmxFixtureBlockEntity>
    getFixturesInGroup(
            ResourceKey<Level> dimension,
            String groupName
    ) {
        return getFixturesInGroup(
                dimension,
                FixtureGroupName.of(groupName)
        );
    }

    /**
     * Returns every loaded fixture patched to one DMX universe in one
     * dimension.
     */
    public static synchronized List<DmxFixtureBlockEntity>
    getFixturesInUniverse(
            ResourceKey<Level> dimension,
            int universe
    ) {
        int safeUniverse =
                Math.max(
                        FixturePatch.MIN_UNIVERSE,
                        Math.min(
                                FixturePatch.MAX_UNIVERSE,
                                universe
                        )
                );

        List<DmxFixtureBlockEntity> matches =
                new ArrayList<>();

        for (DmxFixtureBlockEntity fixture
                : getFixturesInDimension(dimension)) {

            if (fixture.getUniverse() == safeUniverse) {
                matches.add(fixture);
            }
        }

        return List.copyOf(matches);
    }

    /**
     * Returns all distinct non-empty groups currently represented by
     * loaded fixtures in one dimension.
     */
    public static synchronized Set<FixtureGroupName>
    getGroupsInDimension(
            ResourceKey<Level> dimension
    ) {
        Set<FixtureGroupName> groups =
                new LinkedHashSet<>();

        for (DmxFixtureBlockEntity fixture
                : getFixturesInDimension(dimension)) {

            FixtureGroupName group =
                    fixture.getFixtureGroup();

            if (group != null && !group.isUngrouped()) {
                groups.add(group);
            }
        }

        return Collections.unmodifiableSet(groups);
    }

    /**
     * Returns the number of loaded fixtures in one dimension.
     */
    public static synchronized int getFixtureCount(
            ResourceKey<Level> dimension
    ) {
        return getFixturesInDimension(
                dimension
        ).size();
    }

    /**
     * Returns the total number of loaded fixtures.
     */
    public static synchronized int getTotalFixtureCount() {
        return getAllFixtures().size();
    }

    /**
     * Clears all fixtures in one dimension.
     *
     * This will be useful when a server level unloads.
     */
    public static synchronized void clearDimension(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return;
        }

        FIXTURES_BY_DIMENSION.remove(dimension);
    }

    /**
     * Clears the complete registry.
     *
     * This will be useful when a server stops.
     */
    public static synchronized void clearAll() {
        FIXTURES_BY_DIMENSION.clear();
    }

    /**
     * Returns an immutable snapshot of fixtures indexed by position for
     * one dimension.
     */
    public static synchronized Map<
            BlockPos,
            DmxFixtureBlockEntity
    > getDimensionSnapshot(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return Map.of();
        }

        Map<BlockPos, DmxFixtureBlockEntity> dimensionFixtures =
                FIXTURES_BY_DIMENSION.get(dimension);

        if (dimensionFixtures == null) {
            return Map.of();
        }

        removeInvalidFixtures(
                dimension,
                dimensionFixtures
        );

        return Collections.unmodifiableMap(
                new HashMap<>(dimensionFixtures)
        );
    }

    /**
     * Removes stale or client-side entries from one dimension map.
     */
    private static void removeInvalidFixtures(
            ResourceKey<Level> dimension,
            Map<BlockPos, DmxFixtureBlockEntity> fixtures
    ) {
        Collection<BlockPos> positions =
                new ArrayList<>(
                        fixtures.keySet()
                );

        for (BlockPos position : positions) {
            DmxFixtureBlockEntity fixture =
                    fixtures.get(position);

            if (!isUsableServerFixture(fixture)) {
                fixtures.remove(position);
            }
        }

        if (fixtures.isEmpty()) {
            FIXTURES_BY_DIMENSION.remove(dimension);
        }
    }

    /**
     * Checks whether a fixture can safely remain in the registry.
     */
    private static boolean isUsableServerFixture(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null
                || fixture.isRemoved()) {

            return false;
        }

        Level level =
                fixture.getLevel();

        return level != null
                && !level.isClientSide();
    }
}