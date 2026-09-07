package dmx.lighting;

import net.minecraft.world.phys.Vec3;

/**
 * Calculates the world-space direction of a fixture's visible beam.
 *
 * This mirrors the mechanical transform hierarchy used by
 * ParFixtureRenderer.
 *
 * Renderer hierarchy:
 *
 * Installation Pan
 *     Installation Tilt
 *         Live Pan Offset
 *             Live Tilt Offset
 *                 Fixture head / lens / beam
 *
 * The fixture faces local negative Z before any rotations are applied.
 *
 * IMPORTANT:
 *
 * These rotations must be applied in the same effective order as the
 * renderer. Because transforms are nested, the local beam vector is
 * transformed starting with the innermost rotation:
 *
 * 1. Live Tilt Offset
 * 2. Live Pan Offset
 * 3. Mount Tilt
 * 4. Mount Pan
 *
 * This keeps server-side beam calculations aligned with the visible
 * client-side fixture beam.
 */
public final class FixtureBeamDirection {

    /*
     * -----------------------------------------------------------------
     * Local fixture direction
     * -----------------------------------------------------------------
     */

    /**
     * The PAR fixture faces toward negative local Z.
     */
    private static final Vec3 LOCAL_FORWARD =
            new Vec3(
                    0.0D,
                    0.0D,
                    -1.0D
            );

    private FixtureBeamDirection() {
        // Utility class: do not instantiate.
    }

    /*
     * -----------------------------------------------------------------
     * Fixture direction
     * -----------------------------------------------------------------
     */

    /**
     * Returns the world-space beam direction for a fixture.
     *
     * This includes:
     *
     * - Installation Pan
     * - Installation Tilt
     * - Active Manual/DMX Pan Offset
     * - Active Manual/DMX Tilt Offset
     */
    public static Vec3 fromFixture(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return defaultDirection();
        }

        return fromAngles(
                fixture.getMountPanDegrees(),
                fixture.getMountTiltDegrees(),
                fixture.getActivePanOffsetDegrees(),
                fixture.getActiveTiltOffsetDegrees()
        );
    }

    /**
     * Calculates a world-space beam direction using the complete
     * mechanical fixture hierarchy.
     */
    public static Vec3 fromAngles(
            float mountPanDegrees,
            float mountTiltDegrees,
            float panOffsetDegrees,
            float tiltOffsetDegrees
    ) {
        Vec3 direction =
                LOCAL_FORWARD;

        /*
         * -------------------------------------------------------------
         * Innermost transform: head Tilt
         * -------------------------------------------------------------
         *
         * The head/lens/beam is tilted inside the already-panned yoke.
         */

        direction =
                rotateTilt(
                        direction,
                        tiltOffsetDegrees
                );

        /*
         * -------------------------------------------------------------
         * Yoke Pan
         * -------------------------------------------------------------
         */

        direction =
                rotatePan(
                        direction,
                        panOffsetDegrees
                );

        /*
         * -------------------------------------------------------------
         * Installation Tilt
         * -------------------------------------------------------------
         */

        direction =
                rotateTilt(
                        direction,
                        mountTiltDegrees
                );

        /*
         * -------------------------------------------------------------
         * Installation Pan
         * -------------------------------------------------------------
         */

        direction =
                rotatePan(
                        direction,
                        mountPanDegrees
                );

        return normalizeOrDefault(
                direction
        );
    }

    /*
     * -----------------------------------------------------------------
     * Mount-only compatibility
     * -----------------------------------------------------------------
     */

    /**
     * Calculates beam direction using only installation Pan/Tilt.
     *
     * This remains available for diagnostics and compatibility.
     *
     * It does NOT include live Manual/DMX movement.
     */
    public static Vec3 fromMountAngles(
            float mountPanDegrees,
            float mountTiltDegrees
    ) {
        return fromAngles(
                mountPanDegrees,
                mountTiltDegrees,
                0.0F,
                0.0F
        );
    }

    /*
     * -----------------------------------------------------------------
     * Explicit rotation helpers
     * -----------------------------------------------------------------
     */

    /**
     * Rotates a direction around its current positive Y axis.
     *
     * This uses the same mathematical orientation expected by the
     * renderer's Axis.YP rotation.
     */
    public static Vec3 rotatePan(
            Vec3 direction,
            float panDegrees
    ) {
        Vec3 safeDirection =
                normalizeOrDefault(
                        direction
                );

        double radians =
                Math.toRadians(
                        panDegrees
                );

        double cosine =
                Math.cos(
                        radians
                );

        double sine =
                Math.sin(
                        radians
                );

        double x =
                safeDirection.x
                        * cosine
                        + safeDirection.z
                        * sine;

        double y =
                safeDirection.y;

        double z =
                -safeDirection.x
                        * sine
                        + safeDirection.z
                        * cosine;

        return normalizeOrDefault(
                new Vec3(
                        x,
                        y,
                        z
                )
        );
    }

    /**
     * Rotates a direction around its current positive X axis.
     *
     * This uses the same mathematical orientation expected by the
     * renderer's Axis.XP rotation.
     */
    public static Vec3 rotateTilt(
            Vec3 direction,
            float tiltDegrees
    ) {
        Vec3 safeDirection =
                normalizeOrDefault(
                        direction
                );

        double radians =
                Math.toRadians(
                        tiltDegrees
                );

        double cosine =
                Math.cos(
                        radians
                );

        double sine =
                Math.sin(
                        radians
                );

        double x =
                safeDirection.x;

        double y =
                safeDirection.y
                        * cosine
                        - safeDirection.z
                        * sine;

        double z =
                safeDirection.y
                        * sine
                        + safeDirection.z
                        * cosine;

        return normalizeOrDefault(
                new Vec3(
                        x,
                        y,
                        z
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Direction constants and validation
     * -----------------------------------------------------------------
     */

    /**
     * Returns the unrotated fixture direction.
     */
    public static Vec3 defaultDirection() {
        return LOCAL_FORWARD;
    }

    /**
     * Returns true when a direction is finite and non-zero.
     */
    public static boolean isValidDirection(
            Vec3 direction
    ) {
        if (direction == null) {
            return false;
        }

        return Double.isFinite(
                direction.x
        )
                && Double.isFinite(
                        direction.y
                )
                && Double.isFinite(
                        direction.z
                )
                && direction.lengthSqr()
                > 0.000001D;
    }

    /**
     * Normalizes a direction or returns local forward when invalid.
     */
    public static Vec3 normalizeOrDefault(
            Vec3 direction
    ) {
        if (!isValidDirection(
                direction
        )) {
            return defaultDirection();
        }

        return direction.normalize();
    }

    /*
     * -----------------------------------------------------------------
     * Diagnostics
     * -----------------------------------------------------------------
     */

    /**
     * Returns saved installation Pan normalized to 0-360.
     */
    public static float getMountPanDegrees(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return 0.0F;
        }

        return normalizeDegrees(
                fixture.getMountPanDegrees()
        );
    }

    /**
     * Returns saved installation Tilt normalized to 0-360.
     */
    public static float getMountTiltDegrees(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return 0.0F;
        }

        return normalizeDegrees(
                fixture.getMountTiltDegrees()
        );
    }

    /**
     * Returns the currently active Pan movement offset.
     */
    public static float getPanOffsetDegrees(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return 0.0F;
        }

        return fixture.getActivePanOffsetDegrees();
    }

    /**
     * Returns the currently active Tilt movement offset.
     */
    public static float getTiltOffsetDegrees(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return 0.0F;
        }

        return fixture.getActiveTiltOffsetDegrees();
    }

    /*
     * -----------------------------------------------------------------
     * Internal utilities
     * -----------------------------------------------------------------
     */

    private static float normalizeDegrees(
            float degrees
    ) {
        if (!Float.isFinite(
                degrees
        )) {
            return 0.0F;
        }

        float normalized =
                degrees
                        % 360.0F;

        if (normalized < 0.0F) {
            normalized +=
                    360.0F;
        }

        if (normalized == -0.0F) {
            return 0.0F;
        }

        return normalized;
    }
}