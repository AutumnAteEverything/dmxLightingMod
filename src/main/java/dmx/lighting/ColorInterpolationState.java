package dmx.lighting;

/**
 * Client-side visible RGB transition state.
 *
 * The target RGB remains authoritative; this class only decides what
 * color should be displayed for the current rendered frame.
 */
public final class ColorInterpolationState {

    private static final float TICKS_PER_SECOND =
            20.0F;

    private boolean initialized;

    private int displayedPackedRgb;
    private int startPackedRgb;
    private int targetPackedRgb;

    private double startGameTime;

    public boolean update(
            int activePackedRgb,
            boolean enabled,
            float timeSeconds,
            double now
    ) {
        int oldDisplayedPackedRgb =
                displayedPackedRgb;

        int oldTargetPackedRgb =
                targetPackedRgb;

        boolean wasInitialized =
                initialized;

        int safeTarget =
                activePackedRgb
                        & 0x00FFFFFF;

        if (!enabled) {
            snapTo(
                    safeTarget,
                    now
            );

            return hasChanged(
                    wasInitialized,
                    oldDisplayedPackedRgb,
                    oldTargetPackedRgb
            );
        }

        if (!initialized) {
            snapTo(
                    safeTarget,
                    now
            );

            return hasChanged(
                    wasInitialized,
                    oldDisplayedPackedRgb,
                    oldTargetPackedRgb
            );
        }

        sample(
                timeSeconds,
                now
        );

        if (safeTarget != targetPackedRgb) {
            startPackedRgb =
                    displayedPackedRgb;

            targetPackedRgb =
                    safeTarget;

            startGameTime =
                    now;
        }

        return hasChanged(
                wasInitialized,
                oldDisplayedPackedRgb,
                oldTargetPackedRgb
        );
    }

    public int getDisplayedPackedRgb(
            int fallbackPackedRgb
    ) {
        return initialized
                ? displayedPackedRgb
                : fallbackPackedRgb
                & 0x00FFFFFF;
    }

    public void snapTo(
            int packedRgb,
            double now
    ) {
        int safePackedRgb =
                packedRgb
                        & 0x00FFFFFF;

        displayedPackedRgb =
                safePackedRgb;

        startPackedRgb =
                safePackedRgb;

        targetPackedRgb =
                safePackedRgb;

        startGameTime =
                now;

        initialized =
                true;
    }

    private void sample(
            float timeSeconds,
            double now
    ) {
        double durationTicks =
                Math.max(
                        1.0D,
                        ColorInterpolationSettings
                                .clampTimeSeconds(
                                        timeSeconds
                                )
                                * TICKS_PER_SECOND
                );

        float progress =
                (float) Math.max(
                        0.0D,
                        Math.min(
                                1.0D,
                                (now - startGameTime)
                                        / durationTicks
                        )
                );

        displayedPackedRgb =
                interpolatePackedRgb(
                        startPackedRgb,
                        targetPackedRgb,
                        progress
                );
    }

    private static int interpolatePackedRgb(
            int start,
            int end,
            float progress
    ) {
        int red =
                interpolateChannel(
                        (start >> 16) & 0xFF,
                        (end >> 16) & 0xFF,
                        progress
                );

        int green =
                interpolateChannel(
                        (start >> 8) & 0xFF,
                        (end >> 8) & 0xFF,
                        progress
                );

        int blue =
                interpolateChannel(
                        start & 0xFF,
                        end & 0xFF,
                        progress
                );

        return red << 16
                | green << 8
                | blue;
    }

    private static int interpolateChannel(
            int start,
            int end,
            float progress
    ) {
        return Math.round(
                start
                        + (
                        end - start
                )
                        * progress
        );
    }

    private boolean hasChanged(
            boolean wasInitialized,
            int oldDisplayedPackedRgb,
            int oldTargetPackedRgb
    ) {
        return initialized != wasInitialized
                || displayedPackedRgb != oldDisplayedPackedRgb
                || targetPackedRgb != oldTargetPackedRgb;
    }
}
