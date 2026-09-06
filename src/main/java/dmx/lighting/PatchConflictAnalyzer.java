package dmx.lighting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects overlapping DMX patch ranges.
 *
 * Fixtures only conflict when:
 *
 * - They are in the same universe.
 * - Their occupied channel ranges overlap.
 */
public final class PatchConflictAnalyzer {

    private PatchConflictAnalyzer() {
        // Utility class: do not instantiate.
    }

    /**
     * Analyzes an ordered or unordered collection of patch entries.
     */
    public static List<PatchConflictResult> analyze(
            Iterable<PatchOccupancyEntry> entries
    ) {
        List<PatchOccupancyEntry> entryList =
                new ArrayList<>();

        for (PatchOccupancyEntry entry : entries) {
            if (entry != null) {
                entryList.add(entry);
            }
        }

        Map<PatchOccupancyEntry, PatchConflictResult> results =
                new LinkedHashMap<>();

        for (PatchOccupancyEntry entry : entryList) {
            results.put(
                    entry,
                    new PatchConflictResult(entry)
            );
        }

        /*
         * Compare each pair only once.
         */
        for (int firstIndex = 0;
                firstIndex < entryList.size();
                firstIndex++) {

            PatchOccupancyEntry first =
                    entryList.get(firstIndex);

            for (int secondIndex = firstIndex + 1;
                    secondIndex < entryList.size();
                    secondIndex++) {

                PatchOccupancyEntry second =
                        entryList.get(secondIndex);

                if (!first.overlaps(second)) {
                    continue;
                }

                results.get(first)
                        .addConflict(second);

                results.get(second)
                        .addConflict(first);
            }
        }

        return List.copyOf(
                results.values()
        );
    }

    /**
     * Returns true when any patch problem exists.
     */
    public static boolean hasAnyProblems(
            Iterable<PatchConflictResult> results
    ) {
        for (PatchConflictResult result : results) {
            if (result != null
                    && result.hasProblem()) {

                return true;
            }
        }

        return false;
    }

    /**
     * Returns the total number of fixtures with patch problems.
     */
    public static int countProblemFixtures(
            Iterable<PatchConflictResult> results
    ) {
        int count = 0;

        for (PatchConflictResult result : results) {
            if (result != null
                    && result.hasProblem()) {

                count++;
            }
        }

        return count;
    }
}