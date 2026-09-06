package dmx.lighting;

/**
 * Shared limits for visible RGB fade settings.
 */
public final class ColorInterpolationSettings {

    public static final float MIN_TIME_SECONDS =
            0.05F;

    public static final float MAX_TIME_SECONDS =
            60.0F;

    public static final float DEFAULT_TIME_SECONDS =
            1.0F;

    public static final float DISABLED_TIME_SECONDS =
            0.0F;

    private static final float CENTISECONDS =
            100.0F;

    private ColorInterpolationSettings() {
        // Utility class: do not instantiate.
    }

    public static float clampTimeSeconds(
            float seconds
    ) {
        if (!Float.isFinite(
                seconds
        )) {
            return DEFAULT_TIME_SECONDS;
        }

        return Math.max(
                MIN_TIME_SECONDS,
                Math.min(
                        MAX_TIME_SECONDS,
                        seconds
                )
        );
    }

    public static int toCentiseconds(
            boolean enabled,
            float seconds
    ) {
        if (!enabled) {
            return 0;
        }

        return Math.max(
                1,
                Math.round(
                        clampTimeSeconds(
                                seconds
                        )
                                * CENTISECONDS
                )
        );
    }

    public static float fromCentiseconds(
            int centiseconds
    ) {
        if (centiseconds <= 0) {
            return DISABLED_TIME_SECONDS;
        }

        return clampTimeSeconds(
                centiseconds / CENTISECONDS
        );
    }
}
