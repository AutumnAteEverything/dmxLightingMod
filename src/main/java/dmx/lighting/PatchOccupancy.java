package dmx.lighting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generates an ordered DMX patch view for one dimension.
 *
 * In the parameter-centric architecture, fixtures no longer occupy one
 * contiguous base-channel range.
 *
 * Instead, occupancy is derived from the explicit channels assigned in
 * each fixture's FixtureParameterMap.
 *
 * This class is read-only.
 */
public final class PatchOccupancy {

    private PatchOccupancy() {
        // Utility class: do not instantiate.
    }

    /**
     * Builds an ordered list of DMX occupancy entries for one
     * dimension.
     *
     * Fixtures are sorted by:
     *
     * 1. Universe
     * 2. Lowest assigned parameter channel
     * 3. Fixture name
     */
    public static List<PatchOccupancyEntry> build(
            ResourceKey<Level> dimension
    ) {
        List<PatchOccupancyEntry> entries =
                new ArrayList<>();

        for (DmxFixtureBlockEntity fixture :
                DmxFixtureRegistry.getAllFixtures()) {

            /*
             * Ignore fixtures that are not currently attached to a
             * world or belong to another dimension.
             */
            Level level =
                    fixture.getLevel();

            if (level == null
                    || !level.dimension().equals(
                            dimension
                    )) {

                continue;
            }

            DmxFixtureProfile profile =
                    DmxFixtureProfileRegistry.getOrDefault(
                            fixture.getFixtureType()
                    );

            FixtureParameterMap map =
                    fixture.getParameterMap();

            int[] assignedChannels =
                    new int[] {
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

            entries.add(
                    new PatchOccupancyEntry(
                            fixture.getBlockPos(),
                            fixture.getFixtureName(),
                            profile.id(),
                            profile.displayName(),
                            fixture.getUniverse(),
                            assignedChannels
                    )
            );
        }

        entries.sort(
                Comparator
                        .comparingInt(
                                PatchOccupancyEntry::universe
                        )
                        .thenComparingInt(
                                PatchOccupancyEntry::firstChannel
                        )
                        .thenComparing(
                                PatchOccupancyEntry::fixtureName,
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        return List.copyOf(
                entries
        );
    }
}