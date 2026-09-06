package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates world positions related to one fixture's beam cone.
 *
 * Two sampling models are currently available:
 *
 * 1. Volume sampling
 *
 *    calculateBeamPositions(...)
 *
 *    Returns block positions geometrically located inside the complete
 *    cone volume.
 *
 *    This is retained for diagnostics and compatibility.
 *
 * 2. Surface-aware sampling
 *
 *    calculateSurfaceLightPositions(...)
 *
 *    Casts multiple rays through the beam cone and finds empty blocks
 *    immediately before those rays strike world geometry.
 *
 *    Surface hits are counted. A candidate position must be hit by
 *    multiple rays before it becomes an illumination sample.
 *
 *    This rejects isolated grazing collisions that would otherwise
 *    create obvious omnidirectional spill.
 */
public final class FixtureBeamIllumination {

    /*
     * -----------------------------------------------------------------
     * General limits
     * -----------------------------------------------------------------
     */

    private static final int MAX_SAMPLE_POSITIONS =
            4096;

    private static final int MAX_SURFACE_POSITIONS =
            256;

    /*
     * -----------------------------------------------------------------
     * Surface-ray sampling
     * -----------------------------------------------------------------
     */

    private static final double RAY_STEP =
            0.25D;

    /**
     * Surface collisions closer than this to the lens are ignored.
     */
    private static final double MIN_SURFACE_LIGHT_DISTANCE =
            2.0D;

    /**
     * Number of rays that must independently resolve to the same air
     * block before that block becomes a light sample.
     *
     * This is the main anti-grazing filter.
     */
    private static final int MIN_SURFACE_HIT_COUNT =
            2;

    private static final int MIN_RAY_RINGS =
            3;

    private static final int MAX_RAY_RINGS =
            7;

    private static final int BASE_RING_SEGMENTS =
            8;

    private FixtureBeamIllumination() {
        // Utility class: do not instantiate.
    }

    /*
     * -----------------------------------------------------------------
     * Legacy / diagnostic volume sampling
     * -----------------------------------------------------------------
     */

    public static List<BlockPos> calculateBeamPositions(
            Vec3 beamOrigin,
            Vec3 beamDirection,
            float beamWidthDegrees,
            int beamLengthBlocks
    ) {
        if (beamOrigin == null
                || beamDirection == null) {

            return List.of();
        }

        if (beamDirection.lengthSqr()
                <= 0.000001D) {

            return List.of();
        }

        int safeLength =
                Math.max(
                        1,
                        beamLengthBlocks
                );

        float safeWidth =
                Math.max(
                        1.0F,
                        Math.min(
                                179.0F,
                                beamWidthDegrees
                        )
                );

        Vec3 normalizedDirection =
                beamDirection.normalize();

        double halfAngleRadians =
                Math.toRadians(
                        safeWidth / 2.0F
                );

        double minimumAlignment =
                Math.cos(
                        halfAngleRadians
                );

        double maximumRadius =
                Math.tan(
                        halfAngleRadians
                )
                        * safeLength;

        int searchRadius =
                (int) Math.ceil(
                        maximumRadius
                );

        BlockPos originBlock =
                BlockPos.containing(
                        beamOrigin
                );

        int horizontalRadius =
                safeLength
                        + searchRadius
                        + 1;

        int verticalRadius =
                safeLength
                        + searchRadius
                        + 1;

        List<BlockPos> positions =
                new ArrayList<>();

        for (
                int y =
                        originBlock.getY()
                                - verticalRadius;
                y <=
                        originBlock.getY()
                                + verticalRadius;
                y++
        ) {
            for (
                    int z =
                            originBlock.getZ()
                                    - horizontalRadius;
                    z <=
                            originBlock.getZ()
                                    + horizontalRadius;
                    z++
            ) {
                for (
                        int x =
                                originBlock.getX()
                                        - horizontalRadius;
                        x <=
                                originBlock.getX()
                                        + horizontalRadius;
                        x++
                ) {
                    BlockPos candidate =
                            new BlockPos(
                                    x,
                                    y,
                                    z
                            );

                    Vec3 candidateCenter =
                            Vec3.atCenterOf(
                                    candidate
                            );

                    Vec3 toCandidate =
                            candidateCenter.subtract(
                                    beamOrigin
                            );

                    double distance =
                            toCandidate.length();

                    if (distance <= 0.000001D) {
                        continue;
                    }

                    if (distance > safeLength) {
                        continue;
                    }

                    Vec3 directionToCandidate =
                            toCandidate.scale(
                                    1.0D / distance
                            );

                    double alignment =
                            normalizedDirection.dot(
                                    directionToCandidate
                            );

                    if (alignment <= 0.0D) {
                        continue;
                    }

                    if (alignment < minimumAlignment) {
                        continue;
                    }

                    positions.add(
                            candidate.immutable()
                    );

                    if (positions.size()
                            >= MAX_SAMPLE_POSITIONS) {

                        return List.copyOf(
                                positions
                        );
                    }
                }
            }
        }

        return List.copyOf(
                positions
        );
    }

    public static List<BlockPos> calculateBeamPositions(
            DmxFixtureBlockEntity fixture,
            Vec3 beamOrigin,
            Vec3 beamDirection
    ) {
        if (fixture == null) {
            return List.of();
        }

        return calculateBeamPositions(
                beamOrigin,
                beamDirection,
                fixture.getBeamWidthDegrees(),
                fixture.getBeamLengthBlocks()
        );
    }

    /*
     * -----------------------------------------------------------------
     * Surface-aware illumination sampling
     * -----------------------------------------------------------------
     */

    /**
     * Casts multiple rays through the beam cone.
     *
     * Every ray records the empty block immediately before its first
     * world collision.
     *
     * After all rays have been processed, only candidate positions with
     * at least MIN_SURFACE_HIT_COUNT votes are returned.
     */
    public static List<BlockPos> calculateSurfaceLightPositions(
            Level level,
            Vec3 beamOrigin,
            Vec3 beamDirection,
            float beamWidthDegrees,
            int beamLengthBlocks
    ) {
        if (level == null
                || beamOrigin == null
                || beamDirection == null) {

            return List.of();
        }

        if (beamDirection.lengthSqr()
                <= 0.000001D) {

            return List.of();
        }

        int safeLength =
                Math.max(
                        1,
                        beamLengthBlocks
                );

        float safeWidth =
                Math.max(
                        1.0F,
                        Math.min(
                                170.0F,
                                beamWidthDegrees
                        )
                );

        Vec3 forward =
                beamDirection.normalize();

        Vec3 reference =
                Math.abs(
                        forward.y
                ) < 0.99D
                        ? new Vec3(
                                0.0D,
                                1.0D,
                                0.0D
                        )
                        : new Vec3(
                                1.0D,
                                0.0D,
                                0.0D
                        );

        Vec3 right =
                forward
                        .cross(
                                reference
                        )
                        .normalize();

        Vec3 up =
                right
                        .cross(
                                forward
                        )
                        .normalize();

        double halfAngleRadians =
                Math.toRadians(
                        safeWidth / 2.0F
                );

        int ringCount =
                Math.round(
                        MIN_RAY_RINGS
                                + (
                                safeWidth
                                        / 170.0F
                        )
                                * (
                                MAX_RAY_RINGS
                                        - MIN_RAY_RINGS
                        )
                );

        ringCount =
                Math.max(
                        MIN_RAY_RINGS,
                        Math.min(
                                MAX_RAY_RINGS,
                                ringCount
                        )
                );

        Map<BlockPos, Integer> hitCounts =
                new LinkedHashMap<>();

        /*
         * Center ray.
         */
        addSurfaceHit(
                level,
                beamOrigin,
                forward,
                safeLength,
                hitCounts
        );

        /*
         * Concentric ray rings.
         */
        for (
                int ring = 1;
                ring <= ringCount;
                ring++
        ) {
            double radialFraction =
                    ring
                            / (double) ringCount;

            double rayAngle =
                    halfAngleRadians
                            * radialFraction;

            int segments =
                    BASE_RING_SEGMENTS
                            * ring;

            for (
                    int segment = 0;
                    segment < segments;
                    segment++
            ) {
                double around =
                        Math.PI
                                * 2.0D
                                * segment
                                / segments;

                Vec3 radialDirection =
                        right.scale(
                                Math.cos(
                                        around
                                )
                        )
                        .add(
                                up.scale(
                                        Math.sin(
                                                around
                                        )
                                )
                        );

                Vec3 rayDirection =
                        forward.scale(
                                Math.cos(
                                        rayAngle
                                )
                        )
                        .add(
                                radialDirection.scale(
                                        Math.sin(
                                                rayAngle
                                        )
                                )
                        )
                        .normalize();

                addSurfaceHit(
                        level,
                        beamOrigin,
                        rayDirection,
                        safeLength,
                        hitCounts
                );
            }
        }

        /*
         * Keep only positions confirmed by multiple rays.
         */
        List<BlockPos> confirmedPositions =
                new ArrayList<>();

        for (Map.Entry<BlockPos, Integer> entry
                : hitCounts.entrySet()) {

            if (entry.getValue()
                    < MIN_SURFACE_HIT_COUNT) {

                continue;
            }

            confirmedPositions.add(
                    entry.getKey()
                            .immutable()
            );

            if (confirmedPositions.size()
                    >= MAX_SURFACE_POSITIONS) {

                break;
            }
        }

        return List.copyOf(
                confirmedPositions
        );
    }

    public static List<BlockPos> calculateSurfaceLightPositions(
            DmxFixtureBlockEntity fixture,
            Vec3 beamOrigin,
            Vec3 beamDirection
    ) {
        if (fixture == null
                || fixture.getLevel() == null) {

            return List.of();
        }

        return calculateSurfaceLightPositions(
                fixture.getLevel(),
                beamOrigin,
                beamDirection,
                FixtureBeamSettings.mapWidthToVisualConeDegrees(
                        fixture.getBeamWidthDegrees()
                ),
                fixture.getBeamLengthBlocks()
        );
    }

    /*
     * -----------------------------------------------------------------
     * Ray tracing
     * -----------------------------------------------------------------
     */

    /**
     * Traces one beam ray.
     *
     * When world geometry is encountered, the empty block immediately
     * before the collision receives one vote.
     */
    private static void addSurfaceHit(
            Level level,
            Vec3 origin,
            Vec3 rayDirection,
            int beamLengthBlocks,
            Map<BlockPos, Integer> hitCounts
    ) {
        BlockPos previousEmptyPosition =
                null;

        BlockPos previousTestPosition =
                null;

        for (
                double distance = RAY_STEP;
                distance <= beamLengthBlocks;
                distance += RAY_STEP
        ) {
            Vec3 samplePoint =
                    origin.add(
                            rayDirection.scale(
                                    distance
                            )
                    );

            BlockPos samplePosition =
                    BlockPos.containing(
                            samplePoint
                    );

            /*
             * Quarter-block stepping can repeatedly land inside the
             * same Minecraft block.
             */
            if (samplePosition.equals(
                    previousTestPosition
            )) {
                continue;
            }

            previousTestPosition =
                    samplePosition;

            if (!level.isLoaded(
                    samplePosition
            )) {
                return;
            }

            BlockState state =
                    level.getBlockState(
                            samplePosition
                    );

            if (isBeamTransparent(
                    state
            )) {
                previousEmptyPosition =
                        samplePosition.immutable();

                continue;
            }

            /*
             * World collision.
             *
             * Ignore near-lens collisions.
             */
            if (previousEmptyPosition != null
                    && distance
                    >= MIN_SURFACE_LIGHT_DISTANCE) {

                BlockPos confirmedCandidate =
                        previousEmptyPosition
                                .immutable();

                hitCounts.merge(
                        confirmedCandidate,
                        1,
                        Integer::sum
                );
            }

            return;
        }
    }

    /**
     * AIR and existing LIGHT blocks remain transparent to diagnostic
     * beam rays.
     */
    private static boolean isBeamTransparent(
            BlockState state
    ) {
        if (state == null) {
            return true;
        }

        return state.isAir()
                || state.getBlock()
                == Blocks.LIGHT;
    }

    /*
     * -----------------------------------------------------------------
     * Single-position cone test
     * -----------------------------------------------------------------
     */

    public static boolean isInsideBeam(
            Vec3 beamOrigin,
            Vec3 beamDirection,
            BlockPos position,
            float beamWidthDegrees,
            int beamLengthBlocks
    ) {
        if (beamOrigin == null
                || beamDirection == null
                || position == null) {

            return false;
        }

        if (beamDirection.lengthSqr()
                <= 0.000001D) {

            return false;
        }

        Vec3 candidateCenter =
                Vec3.atCenterOf(
                        position
                );

        Vec3 toCandidate =
                candidateCenter.subtract(
                        beamOrigin
                );

        double distance =
                toCandidate.length();

        if (distance <= 0.000001D) {
            return false;
        }

        if (distance > beamLengthBlocks) {
            return false;
        }

        Vec3 normalizedDirection =
                beamDirection.normalize();

        Vec3 directionToCandidate =
                toCandidate.scale(
                        1.0D / distance
                );

        double alignment =
                normalizedDirection.dot(
                        directionToCandidate
                );

        if (alignment <= 0.0D) {
            return false;
        }

        double halfAngleRadians =
                Math.toRadians(
                        beamWidthDegrees / 2.0F
                );

        double minimumAlignment =
                Math.cos(
                        halfAngleRadians
                );

        return alignment
                >= minimumAlignment;
    }
}