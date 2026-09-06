package dmx.lighting;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Stores the result of checking one patch entry for conflicts.
 *
 * A fixture may conflict with more than one other fixture.
 */
public final class PatchConflictResult {

    private final PatchOccupancyEntry entry;

    private final Set<PatchOccupancyEntry> conflictingEntries =
            new LinkedHashSet<>();

    public PatchConflictResult(
            PatchOccupancyEntry entry
    ) {
        this.entry =
                Objects.requireNonNull(
                        entry,
                        "entry cannot be null"
                );
    }

    /**
     * Returns the fixture whose patch is being evaluated.
     */
    public PatchOccupancyEntry entry() {
        return entry;
    }

    /**
     * Adds another fixture that overlaps this fixture.
     */
    public void addConflict(
            PatchOccupancyEntry conflictingEntry
    ) {
        if (conflictingEntry == null
                || conflictingEntry.equals(entry)) {

            return;
        }

        conflictingEntries.add(
                conflictingEntry
        );
    }

    /**
     * Returns true when at least one overlapping fixture exists.
     */
    public boolean hasConflict() {
        return !conflictingEntries.isEmpty();
    }

    /**
     * Returns the number of overlapping fixtures.
     */
    public int conflictCount() {
        return conflictingEntries.size();
    }

    /**
     * Returns an immutable view of all conflicting fixtures.
     */
    public Set<PatchOccupancyEntry> conflicts() {
        return Collections.unmodifiableSet(
                conflictingEntries
        );
    }

    /**
     * Returns true when the fixture footprint extends beyond channel
     * 512.
     */
    public boolean isOutOfRange() {
        return !entry.fitsInUniverse();
    }

    /**
     * Returns true when either an overlap or invalid channel range is
     * present.
     */
    public boolean hasProblem() {
        return hasConflict()
                || isOutOfRange();
    }

    /**
     * Returns a compact status label for the console.
     */
    public String statusLabel() {
        if (isOutOfRange()) {
            return "Out of range";
        }

        if (hasConflict()) {
            return conflictCount() == 1
                    ? "1 conflict"
                    : conflictCount()
                            + " conflicts";
        }

        return "OK";
    }
}