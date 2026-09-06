package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages temporary invisible Minecraft light blocks used to illuminate
 * surfaces struck by fixture beams.
 *
 * Each loaded fixture owns a set of temporary light positions.
 *
 * The manager:
 *
 * - Casts multiple rays through the visible beam cone.
 * - Finds world surfaces struck by those rays.
 * - Places vanilla invisible Light Blocks in the air immediately in
 *   front of those surfaces.
 * - Updates their light level as fixture intensity changes.
 * - Removes stale positions when the beam moves or changes size.
 * - Removes all owned lights when the fixture turns off or unloads.
 *
 * Existing non-air world blocks are never replaced.
 *
 * Multiple fixtures may share one temporary light position. A simple
 * reference-counting system prevents one fixture from removing a light
 * that another fixture is still using.
 *
 * Unlike the earlier implementation, this manager no longer fills the
 * beam volume or centerline with Light Blocks.
 */
public final class FixtureBeamLightManager {

    /*
     * -----------------------------------------------------------------
     * Surface-light sampling
     * -----------------------------------------------------------------
     */

    /**
     * Hard limit on actual Minecraft LIGHT blocks owned by one fixture.
     *
     * The surface sampler may discover more hit positions than this.
     * Keeping this limit modest reduces block-light update cost.
     */
    private static final int MAX_LIGHT_SAMPLES =
            32;

    /*
     * -----------------------------------------------------------------
     * Beam origin
     * -----------------------------------------------------------------
     */

    private static final double LENS_CENTER_Y =
            0.46875D;

    private static final double BEAM_ORIGIN_FORWARD_OFFSET =
            0.30D;

    /*
     * -----------------------------------------------------------------
     * Update behavior
     * -----------------------------------------------------------------
     */

    /**
     * Only recalculate the beam lighting every few game ticks.
     *
     * This reduces block-light churn while still reacting quickly.
     */
    private static final int UPDATE_INTERVAL_TICKS =
            4;

    /*
     * -----------------------------------------------------------------
     * Per-level state
     * -----------------------------------------------------------------
     */

    /**
     * Runtime light ownership is separated by Level object.
     *
     * IdentityHashMap is intentional because Level instances are unique
     * runtime worlds.
     */
    private static final Map<
            Level,
            LevelLightState
    > LEVEL_STATES =
            new IdentityHashMap<>();

    private FixtureBeamLightManager() {
        // Utility class: do not instantiate.
    }

    /*
     * -----------------------------------------------------------------
     * Public update API
     * -----------------------------------------------------------------
     */

    /**
     * Updates temporary beam lighting for one fixture.
     *
     * Call this from the server-side fixture tick.
     */
    public static void update(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return;
        }

        Level level =
                fixture.getLevel();

        if (level == null
                || level.isClientSide()) {

            return;
        }

        /*
         * Avoid recalculating every tick.
         */
        if (
                level.getGameTime()
                        % UPDATE_INTERVAL_TICKS
                        != 0
        ) {
            return;
        }

        BlockPos fixturePosition =
                fixture.getBlockPos()
                        .immutable();

        int lightLevel =
                clampLightLevel(
                        fixture.getMinecraftLightLevel()
                );

        /*
         * Dark fixture:
         *
         * Release all temporary beam lights previously owned by this
         * fixture.
         */
        if (lightLevel <= 0) {
            clearFixture(
                    fixture
            );

            return;
        }

        List<BlockPos> calculatedSamples =
                calculateLightSamples(
                        fixture
                );

        Set<BlockPos> desiredPositions =
                new LinkedHashSet<>(
                        calculatedSamples
                );

        LevelLightState levelState =
                LEVEL_STATES.computeIfAbsent(
                        level,
                        ignored ->
                                new LevelLightState()
                );

        FixtureLightState fixtureState =
                levelState.fixtureStates
                        .computeIfAbsent(
                                fixturePosition,
                                ignored ->
                                        new FixtureLightState()
                        );

        /*
         * -------------------------------------------------------------
         * Remove stale positions
         * -------------------------------------------------------------
         *
         * This is particularly important while transitioning from the
         * old volume-lighting system.
         *
         * Any old centerline or cone-volume LIGHT blocks owned by this
         * fixture will disappear automatically unless they are also
         * selected by the new surface sampler.
         */

        Set<BlockPos> stalePositions =
                new HashSet<>(
                        fixtureState.positions
                );

        stalePositions.removeAll(
                desiredPositions
        );

        for (BlockPos stalePosition
                : stalePositions) {

            releasePosition(
                    level,
                    levelState,
                    stalePosition
            );

            fixtureState.positions.remove(
                    stalePosition
            );
        }

        /*
         * -------------------------------------------------------------
         * Add newly requested positions
         * -------------------------------------------------------------
         */

        for (BlockPos desiredPosition
                : desiredPositions) {

            if (fixtureState.positions.contains(
                    desiredPosition
            )) {
                continue;
            }

            if (claimPosition(
                    level,
                    levelState,
                    desiredPosition
            )) {
                fixtureState.positions.add(
                        desiredPosition.immutable()
                );
            }
        }

        /*
         * -------------------------------------------------------------
         * Update brightness
         * -------------------------------------------------------------
         */

        fixtureState.lightLevel =
                lightLevel;

        for (BlockPos position
                : fixtureState.positions) {

            updateSharedLightLevel(
                    level,
                    levelState,
                    position
            );
        }

        /*
         * Empty states do not need to remain cached.
         */
        if (fixtureState.positions.isEmpty()) {
            levelState.fixtureStates.remove(
                    fixturePosition
            );
        }

        cleanupLevelStateIfEmpty(
                level,
                levelState
        );
    }

    /**
     * Removes every temporary beam-light position owned by one fixture.
     *
     * Call this when a fixture block entity is removed or unloaded.
     */
    public static void clearFixture(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return;
        }

        Level level =
                fixture.getLevel();

        if (level == null
                || level.isClientSide()) {

            return;
        }

        LevelLightState levelState =
                LEVEL_STATES.get(
                        level
                );

        if (levelState == null) {
            return;
        }

        BlockPos fixturePosition =
                fixture.getBlockPos()
                        .immutable();

        FixtureLightState fixtureState =
                levelState.fixtureStates.remove(
                        fixturePosition
                );

        if (fixtureState == null) {
            return;
        }

        List<BlockPos> ownedPositions =
                new ArrayList<>(
                        fixtureState.positions
                );

        for (BlockPos position
                : ownedPositions) {

            releasePosition(
                    level,
                    levelState,
                    position
            );
        }

        cleanupLevelStateIfEmpty(
                level,
                levelState
        );
    }

    /**
     * Clears all temporary beam lights managed in one Level.
     *
     * Mostly useful later for world shutdown/reload handling.
     */
    public static void clearLevel(
            Level level
    ) {
        if (level == null
                || level.isClientSide()) {

            return;
        }

        LevelLightState levelState =
                LEVEL_STATES.remove(
                        level
                );

        if (levelState == null) {
            return;
        }

        for (BlockPos position
                : new ArrayList<>(
                levelState.positionOwners.keySet()
        )) {

            removeManagedLightBlock(
                    level,
                    position
            );
        }

        levelState.fixtureStates.clear();
        levelState.positionOwners.clear();
    }

    /*
     * -----------------------------------------------------------------
     * Position ownership
     * -----------------------------------------------------------------
     */

    /**
     * Claims one air position for a fixture.
     *
     * Returns false when the position cannot safely host one of our
     * temporary lights.
     */
    private static boolean claimPosition(
            Level level,
            LevelLightState levelState,
            BlockPos position
    ) {
        if (level == null
                || position == null
                || !level.isLoaded(
                        position
                )) {

            return false;
        }

        int currentOwners =
                levelState.positionOwners.getOrDefault(
                        position,
                        0
                );

        /*
         * Already managed by another fixture.
         */
        if (currentOwners > 0) {
            levelState.positionOwners.put(
                    position.immutable(),
                    currentOwners + 1
            );

            return true;
        }

        BlockState currentState =
                level.getBlockState(
                        position
                );

        /*
         * Never replace a real world block.
         *
         * Existing LIGHT blocks are rejected when they are not already
         * owned by this manager because they may have been placed
         * manually by the player.
         */
        if (!currentState.isAir()) {
            return false;
        }

        levelState.positionOwners.put(
                position.immutable(),
                1
        );

        return true;
    }

    /**
     * Releases one fixture's ownership of a temporary light position.
     */
    private static void releasePosition(
            Level level,
            LevelLightState levelState,
            BlockPos position
    ) {
        Integer owners =
                levelState.positionOwners.get(
                        position
                );

        if (owners == null) {
            return;
        }

        if (owners > 1) {
            levelState.positionOwners.put(
                    position,
                    owners - 1
            );

            updateSharedLightLevel(
                    level,
                    levelState,
                    position
            );

            return;
        }

        levelState.positionOwners.remove(
                position
        );

        removeManagedLightBlock(
                level,
                position
        );
    }

    /*
     * -----------------------------------------------------------------
     * Light-block updates
     * -----------------------------------------------------------------
     */

    /**
     * Recalculates the brightest fixture using one shared position and
     * applies that light value.
     */
    private static void updateSharedLightLevel(
            Level level,
            LevelLightState levelState,
            BlockPos position
    ) {
        int brightestLevel =
                0;

        for (FixtureLightState fixtureState
                : levelState.fixtureStates.values()) {

            if (!fixtureState.positions.contains(
                    position
            )) {
                continue;
            }

            brightestLevel =
                    Math.max(
                            brightestLevel,
                            fixtureState.lightLevel
                    );
        }

        if (brightestLevel <= 0) {
            removeManagedLightBlock(
                    level,
                    position
            );

            return;
        }

        placeManagedLightBlock(
                level,
                position,
                brightestLevel
        );
    }

    /**
     * Places or updates an invisible vanilla Light Block.
     */
    private static void placeManagedLightBlock(
            Level level,
            BlockPos position,
            int lightLevel
    ) {
        if (!level.isLoaded(
                position
        )) {
            return;
        }

        BlockState currentState =
                level.getBlockState(
                        position
                );

        /*
         * Only air or an already-managed Light Block may be changed.
         */
        if (!currentState.isAir()
                && currentState.getBlock()
                != Blocks.LIGHT) {

            return;
        }

        int safeLightLevel =
                clampLightLevel(
                        lightLevel
                );

        BlockState desiredState =
                Blocks.LIGHT
                        .defaultBlockState()
                        .setValue(
                                LightBlock.LEVEL,
                                safeLightLevel
                        );

        if (currentState.equals(
                desiredState
        )) {
            return;
        }

        level.setBlock(
                position,
                desiredState,
                Block.UPDATE_ALL
        );
    }

    /**
     * Removes one light only when a vanilla Light Block is still at the
     * managed position.
     */
    private static void removeManagedLightBlock(
            Level level,
            BlockPos position
    ) {
        if (!level.isLoaded(
                position
        )) {
            return;
        }

        BlockState currentState =
                level.getBlockState(
                        position
                );

        if (currentState.getBlock()
                != Blocks.LIGHT) {

            return;
        }

        level.setBlock(
                position,
                Blocks.AIR.defaultBlockState(),
                Block.UPDATE_ALL
        );
    }

    /*
     * -----------------------------------------------------------------
     * Surface sample calculation
     * -----------------------------------------------------------------
     */

    /**
     * Calculates surface-aware illumination positions for one fixture.
     *
     * No world changes are performed here.
     *
     * Unlike the previous implementation, this method does NOT place
     * lights along the beam centerline and does NOT fill the cone.
     *
     * Instead, rays are cast through the visible cone and lights are
     * requested only immediately before struck world surfaces.
     */
    public static List<BlockPos> calculateLightSamples(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return List.of();
        }

        Level level =
                fixture.getLevel();

        if (level == null) {
            return List.of();
        }

        if (fixture.getMinecraftLightLevel()
                <= 0) {

            return List.of();
        }

        Vec3 direction =
                FixtureBeamDirection.fromFixture(
                        fixture
                );

        if (!FixtureBeamDirection.isValidDirection(
                direction
        )) {
            return List.of();
        }

        Vec3 origin =
                calculateBeamOrigin(
                        fixture,
                        direction
                );

        return calculateLightSamples(
                level,
                origin,
                direction,
                fixture.getBeamWidthDegrees(),
                fixture.getBeamLengthBlocks()
        );
    }

    /**
     * Calculates surface-aware illumination positions from explicit beam
     * geometry.
     */
    public static List<BlockPos> calculateLightSamples(
            Level level,
            Vec3 beamOrigin,
            Vec3 beamDirection,
            int beamWidthControl,
            int beamLengthBlocks
    ) {
        if (level == null
                || beamOrigin == null
                || beamDirection == null) {

            return List.of();
        }

        if (!FixtureBeamDirection.isValidDirection(
                beamDirection
        )) {
            return List.of();
        }

        int safeLength =
                Math.max(
                        FixtureBeamSettings.MIN_LENGTH_BLOCKS,
                        Math.min(
                                FixtureBeamSettings.MAX_LENGTH_BLOCKS,
                                beamLengthBlocks
                        )
                );

        /*
         * The stored Beam Width value is a control range.
         *
         * Convert it to the same practical visual cone angle used by the
         * rendered beam before ray casting.
         */
        float visualConeDegrees =
                FixtureBeamSettings
                        .mapWidthToVisualConeDegrees(
                                beamWidthControl
                        );

        List<BlockPos> surfacePositions =
                FixtureBeamIllumination
                        .calculateSurfaceLightPositions(
                                level,
                                beamOrigin,
                                beamDirection.normalize(),
                                visualConeDegrees,
                                safeLength
                        );

        if (surfacePositions.isEmpty()) {
            return List.of();
        }

        /*
         * The surface sampler may return many positions on a large
         * wall.
         *
         * Select an evenly distributed subset so we do not create an
         * excessive number of vanilla LIGHT blocks.
         */
        if (surfacePositions.size()
                <= MAX_LIGHT_SAMPLES) {

            return List.copyOf(
                    surfacePositions
            );
        }

        Set<BlockPos> reducedSamples =
                new LinkedHashSet<>();

        double stride =
                surfacePositions.size()
                        / (double) MAX_LIGHT_SAMPLES;

        for (
                int sampleIndex = 0;
                sampleIndex < MAX_LIGHT_SAMPLES;
                sampleIndex++
        ) {
            int sourceIndex =
                    Math.min(
                            surfacePositions.size() - 1,
                            (int) Math.floor(
                                    sampleIndex
                                            * stride
                            )
                    );

            reducedSamples.add(
                    surfacePositions
                            .get(
                                    sourceIndex
                            )
                            .immutable()
            );
        }

        return List.copyOf(
                reducedSamples
        );
    }

    /*
     * -----------------------------------------------------------------
     * Beam origin
     * -----------------------------------------------------------------
     */

    public static Vec3 calculateBeamOrigin(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return Vec3.ZERO;
        }

        Vec3 direction =
                FixtureBeamDirection.fromFixture(
                        fixture
                );

        return calculateBeamOrigin(
                fixture,
                direction
        );
    }

    private static Vec3 calculateBeamOrigin(
            DmxFixtureBlockEntity fixture,
            Vec3 beamDirection
    ) {
        BlockPos position =
                fixture.getBlockPos();

        Vec3 fixtureCenter =
                new Vec3(
                        position.getX()
                                + 0.5D,
                        position.getY()
                                + LENS_CENTER_Y,
                        position.getZ()
                                + 0.5D
                );

        Vec3 safeDirection =
                FixtureBeamDirection
                        .normalizeOrDefault(
                                beamDirection
                        );

        return fixtureCenter.add(
                safeDirection.scale(
                        BEAM_ORIGIN_FORWARD_OFFSET
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Diagnostics
     * -----------------------------------------------------------------
     */

    public static int countLightSamples(
            DmxFixtureBlockEntity fixture
    ) {
        return calculateLightSamples(
                fixture
        )
                .size();
    }

    public static List<BlockPos> copyLightSamples(
            DmxFixtureBlockEntity fixture
    ) {
        return new ArrayList<>(
                calculateLightSamples(
                        fixture
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Internal runtime state
     * -----------------------------------------------------------------
     */

    /**
     * All dynamic beam-light state belonging to one world instance.
     */
    private static final class LevelLightState {

        private final Map<
                BlockPos,
                FixtureLightState
        > fixtureStates =
                new HashMap<>();

        private final Map<
                BlockPos,
                Integer
        > positionOwners =
                new HashMap<>();
    }

    /**
     * Dynamic-light state belonging to one fixture.
     */
    private static final class FixtureLightState {

        private final Set<BlockPos> positions =
                new HashSet<>();

        private int lightLevel;
    }

    /*
     * -----------------------------------------------------------------
     * Utilities
     * -----------------------------------------------------------------
     */

    private static int clampLightLevel(
            int lightLevel
    ) {
        return Math.max(
                0,
                Math.min(
                        15,
                        lightLevel
                )
        );
    }

    private static void cleanupLevelStateIfEmpty(
            Level level,
            LevelLightState levelState
    ) {
        if (!levelState.fixtureStates.isEmpty()
                || !levelState.positionOwners.isEmpty()) {

            return;
        }

        LEVEL_STATES.remove(
                level
        );
    }
}