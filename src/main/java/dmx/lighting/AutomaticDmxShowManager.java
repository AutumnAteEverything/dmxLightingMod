package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Builds temporary beat-driven lighting shows from jukeboxes and
 * explicit command pulses.
 *
 * The generated output never writes to DmxUniverseManager. Fixtures
 * therefore return to their existing DMX or manual values as soon as
 * the active show ends.
 */
public final class AutomaticDmxShowManager {

    private static final double TICKS_PER_MINUTE =
            1200.0D;

    private static final long JUKEBOX_STALE_TICKS =
            2L;

    private static final long SHOW_UPDATE_INTERVAL_TICKS =
            2L;

    private static final long COMMAND_PULSE_TIMEOUT_TICKS =
            60L;

    private static final double DEFAULT_PULSE_INTERVAL_TICKS =
            10.0D;

    private static final double MINIMUM_PULSE_INTERVAL_TICKS =
            2.0D;

    private static final double PULSE_INTERVAL_SMOOTHING =
            0.35D;

    private static final int COMMAND_PULSE_PALETTE_SEED =
            "dmxlighting:command_pulse".hashCode();

    private static final int BEATS_PER_SKIN_CHANGE_WINDOW =
            4;

    private static final double FIXTURE_MOVEMENT_HALF_CYCLE_BEATS =
            4.0D;

    private static final double DISCO_SPIN_HALF_CYCLE_BEATS =
            24.0D;

    private static final long SKIN_RANDOM_SALT =
            0x5EED5A17C0FFEE1L;

    private static final DiscBeatProfile CUSTOM_DISC_FALLBACK =
            new DiscBeatProfile(
                    120.0D,
                    0.0D,
                    false
            );

    private static final int[][] COLOR_PALETTE = {
            {255, 0, 0},
            {255, 170, 0},
            {0, 255, 80},
            {0, 180, 255},
            {40, 60, 255},
            {255, 0, 255}
    };

    /*
     * Tempos were cross-checked against the 26.1 game audio. The
     * cinematic records contain long sound-design passages, so their
     * values are useful show pulses rather than continuous beat grids.
     */
    private static final Map<String, DiscBeatProfile> BUILT_IN_PROFILES =
            Map.ofEntries(
                    cinematicProfile("minecraft:music_disc_13", 71.0D),
                    profile("minecraft:music_disc_cat", 112.0D),
                    profile("minecraft:music_disc_blocks", 85.0D),
                    profile("minecraft:music_disc_chirp", 110.0D),
                    profile("minecraft:music_disc_creator", 81.0D),
                    profile("minecraft:music_disc_creator_music_box", 111.0D),
                    profile("minecraft:music_disc_far", 130.0D),
                    profile("minecraft:music_disc_lava_chicken", 157.0D),
                    profile("minecraft:music_disc_mall", 115.0D),
                    profile("minecraft:music_disc_mellohi", 91.0D),
                    profile("minecraft:music_disc_stal", 105.0D),
                    profile("minecraft:music_disc_strad", 188.0D),
                    profile("minecraft:music_disc_ward", 107.0D),
                    cinematicProfile("minecraft:music_disc_11", 92.0D),
                    profile("minecraft:music_disc_wait", 114.0D),
                    profile("minecraft:music_disc_otherside", 92.0D),
                    profile("minecraft:music_disc_relic", 136.0D),
                    cinematicProfile("minecraft:music_disc_5", 74.0D),
                    profile("minecraft:music_disc_pigstep", 113.0D),
                    profile("minecraft:music_disc_precipice", 136.0D),
                    profile("minecraft:music_disc_tears", 99.0D),
                    profile("dmxlighting:generic_60_bpm", 60.0D),
                    profile("dmxlighting:generic_70_bpm", 70.0D),
                    profile("dmxlighting:generic_80_bpm", 80.0D),
                    profile("dmxlighting:generic_90_bpm", 90.0D),
                    profile("dmxlighting:generic_100_bpm", 100.0D),
                    profile("dmxlighting:generic_110_bpm", 110.0D),
                    profile("dmxlighting:generic_120_bpm", 120.0D),
                    profile("dmxlighting:generic_130_bpm", 130.0D),
                    profile("dmxlighting:generic_140_bpm", 140.0D)
            );

    private static final Map<
            ResourceKey<Level>,
            Map<BlockPos, ActiveJukebox>
    > ACTIVE_JUKEBOXES = new HashMap<>();

    private static final Map<
            ResourceKey<Level>,
            ActiveCommandPulse
    > ACTIVE_COMMAND_PULSES = new HashMap<>();

    private static boolean enabled =
            true;

    private AutomaticDmxShowManager() {
        // Utility class.
    }

    public static synchronized void tickJukebox(
            Level level,
            BlockPos position,
            JukeboxBlockEntity jukebox
    ) {
        if (level == null
                || level.isClientSide()
                || position == null
                || jukebox == null) {

            return;
        }

        JukeboxSongPlayer songPlayer =
                jukebox.getSongPlayer();

        if (!enabled || !songPlayer.isPlaying()) {
            removeJukebox(
                    level,
                    position
            );

            return;
        }

        ItemStack disc =
                jukebox.getTheItem();

        if (disc.isEmpty()) {
            removeJukebox(
                    level,
                    position
            );

            return;
        }

        String discItemId =
                BuiltInRegistries.ITEM
                        .getKey(
                                disc.getItem()
                        )
                        .toString();

        DiscBeatProfile profile =
                BUILT_IN_PROFILES.getOrDefault(
                        discItemId,
                        CUSTOM_DISC_FALLBACK
                );

        ACTIVE_JUKEBOXES
                .computeIfAbsent(
                        level.dimension(),
                        ignored -> new HashMap<>()
                )
                .put(
                        position.immutable(),
                        new ActiveJukebox(
                                position.immutable(),
                                discItemId,
                                profile,
                                songPlayer.getTicksSinceSongStarted(),
                                level.getGameTime()
                        )
                );
    }

    public static synchronized void removeJukebox(
            Level level,
            BlockPos position
    ) {
        if (level == null || position == null) {
            return;
        }

        Map<BlockPos, ActiveJukebox> dimensionJukeboxes =
                ACTIVE_JUKEBOXES.get(
                        level.dimension()
                );

        if (dimensionJukeboxes == null) {
            return;
        }

        dimensionJukeboxes.remove(
                position
        );

        if (dimensionJukeboxes.isEmpty()) {
            ACTIVE_JUKEBOXES.remove(
                    level.dimension()
            );
        }
    }

    /**
     * Returns an automatic-show output, or null when no beat-driven
     * show is currently controlling this dimension.
     */
    public static synchronized FixtureOutput createOutput(
            Level level,
            BlockPos fixturePosition,
            FixtureOutput underlyingOutput,
            boolean allowMovement
    ) {
        return createOutput(
                level,
                fixturePosition,
                underlyingOutput,
                allowMovement
                        ? MovementStyle.FIXTURE
                        : MovementStyle.STATIC
        );
    }

    /** Uses a slower bipolar movement wave for disco-ball spin. */
    public static synchronized FixtureOutput createDiscoBallOutput(
            Level level,
            BlockPos fixturePosition,
            FixtureOutput underlyingOutput
    ) {
        return createOutput(
                level,
                fixturePosition,
                underlyingOutput,
                MovementStyle.DISCO_BALL
        );
    }

    private static FixtureOutput createOutput(
            Level level,
            BlockPos fixturePosition,
            FixtureOutput underlyingOutput,
            MovementStyle movementStyle
    ) {
        if (level == null
                || level.isClientSide()) {

            return null;
        }

        FixtureOutput base =
                underlyingOutput == null
                        ? FixtureOutput.BLACKOUT
                        : underlyingOutput;

        ActiveCommandPulse commandPulse =
                findActiveCommandPulse(
                        level
                );

        if (commandPulse != null) {
            return buildCommandPulseOutput(
                    commandPulse,
                    level.getGameTime(),
                    base,
                    movementStyle
            );
        }

        if (!enabled) {
            return null;
        }

        ActiveJukebox jukebox =
                findNearestActiveJukebox(
                        level,
                        fixturePosition
                );

        if (jukebox == null) {
            return null;
        }

        return buildShowOutput(
                jukebox,
                base,
                movementStyle
        );
    }

    /**
     * Returns a temporary jukebox-show skin, or null when the fixture
     * should use its configured or DMX-controlled skin.
     */
    public static synchronized Integer createSkinOverride(
            Level level,
            BlockPos fixturePosition,
            long fixtureSeed,
            int underlyingSkin
    ) {
        if (level == null
                || level.isClientSide()
                || !enabled) {

            return null;
        }

        if (findActiveCommandPulse(level) != null) {
            return null;
        }

        ActiveJukebox jukebox =
                findNearestActiveJukebox(
                        level,
                        fixturePosition
                );

        if (jukebox == null) {
            return null;
        }

        long beatIndex =
                (long) Math.floor(
                        getBeatPosition(jukebox)
                );

        if (beatIndex < 0L) {
            return null;
        }

        long windowIndex =
                Math.floorDiv(
                        beatIndex,
                        BEATS_PER_SKIN_CHANGE_WINDOW
                );

        int beatInWindow =
                Math.floorMod(
                        beatIndex,
                        BEATS_PER_SKIN_CHANGE_WINDOW
                );

        long randomSeed =
                skinRandomSeed(
                        jukebox,
                        fixturePosition,
                        fixtureSeed
                );

        int changeBeat =
                Math.floorMod(
                        mix64(
                                randomSeed
                                        + windowIndex
                                        * 0x9E3779B97F4A7C15L
                        ),
                        BEATS_PER_SKIN_CHANGE_WINDOW
                );

        long activeWindow =
                beatInWindow >= changeBeat
                        ? windowIndex
                        : windowIndex - 1L;

        if (activeWindow < 0L) {
            return null;
        }

        int safeUnderlying =
                Math.clamp(
                        underlyingSkin,
                        DmxPixelBlockEntity.MIN_SKIN,
                        DmxPixelBlockEntity.MAX_SKIN
                );

        int availableAlternatives =
                DmxPixelBlockEntity.MAX_SKIN
                        - DmxPixelBlockEntity.MIN_SKIN;

        int direction =
                (mix64(randomSeed ^ SKIN_RANDOM_SALT) & 1L) == 0L
                        ? 1
                        : availableAlternatives - 1;

        int relativeSkin =
                1 + Math.floorMod(
                        Math.floorMod(
                                mix64(randomSeed),
                                availableAlternatives
                        ) + activeWindow * direction,
                        availableAlternatives
                );

        int skinCount =
                DmxPixelBlockEntity.MAX_SKIN
                        - DmxPixelBlockEntity.MIN_SKIN
                        + 1;

        return DmxPixelBlockEntity.MIN_SKIN
                + Math.floorMod(
                        safeUnderlying
                                - DmxPixelBlockEntity.MIN_SKIN
                                + relativeSkin,
                        skinCount
                );
    }

    public static synchronized void triggerCommandPulse(
            Level level
    ) {
        if (level == null || level.isClientSide()) {
            return;
        }

        long currentGameTime =
                level.getGameTime();

        ActiveCommandPulse previous =
                findActiveCommandPulse(
                        level
                );

        if (previous == null) {
            ACTIVE_COMMAND_PULSES.put(
                    level.dimension(),
                    new ActiveCommandPulse(
                            1L,
                            currentGameTime,
                            DEFAULT_PULSE_INTERVAL_TICKS,
                            false
                    )
            );

            return;
        }

        long elapsedTicks =
                currentGameTime
                        - previous.lastPulseGameTime();

        double estimatedInterval =
                previous.estimatedIntervalTicks();

        boolean tempoMeasured =
                previous.tempoMeasured();

        if (elapsedTicks > 0L) {
            double measuredInterval =
                    Math.clamp(
                            (double) elapsedTicks,
                            MINIMUM_PULSE_INTERVAL_TICKS,
                            (double) COMMAND_PULSE_TIMEOUT_TICKS
                    );

            estimatedInterval =
                    tempoMeasured
                            ? estimatedInterval
                                    * (1.0D - PULSE_INTERVAL_SMOOTHING)
                                    + measuredInterval
                                    * PULSE_INTERVAL_SMOOTHING
                            : measuredInterval;

            tempoMeasured =
                    true;
        }

        ACTIVE_COMMAND_PULSES.put(
                level.dimension(),
                new ActiveCommandPulse(
                        previous.pulsesReceived() + 1L,
                        currentGameTime,
                        estimatedInterval,
                        tempoMeasured
                )
        );
    }

    public static synchronized boolean stopCommandPulse(
            Level level
    ) {
        if (level == null) {
            return false;
        }

        return ACTIVE_COMMAND_PULSES.remove(
                level.dimension()
        ) != null;
    }

    public static synchronized PulseShowInfo getPulseShowInfo(
            Level level
    ) {
        ActiveCommandPulse pulse =
                findActiveCommandPulse(
                        level
                );

        if (pulse == null) {
            return null;
        }

        long elapsedTicks =
                level.getGameTime()
                        - pulse.lastPulseGameTime();

        return new PulseShowInfo(
                pulse.pulsesReceived(),
                TICKS_PER_MINUTE
                        / pulse.estimatedIntervalTicks(),
                pulse.tempoMeasured(),
                Math.max(
                        0.0D,
                        (COMMAND_PULSE_TIMEOUT_TICKS - elapsedTicks)
                                / 20.0D
                )
        );
    }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    public static synchronized void setEnabled(
            boolean shouldEnable
    ) {
        enabled =
                shouldEnable;

        if (!enabled) {
            ACTIVE_JUKEBOXES.clear();
        }
    }

    public static synchronized List<ActiveShowInfo> getActiveShows(
            Level level
    ) {
        if (level == null || !enabled) {
            return List.of();
        }

        removeStaleJukeboxes(
                level.dimension(),
                level.getGameTime()
        );

        Map<BlockPos, ActiveJukebox> dimensionJukeboxes =
                ACTIVE_JUKEBOXES.get(
                        level.dimension()
                );

        if (dimensionJukeboxes == null) {
            return List.of();
        }

        List<ActiveShowInfo> shows =
                new ArrayList<>();

        for (ActiveJukebox jukebox :
                dimensionJukeboxes.values()) {

            shows.add(
                    new ActiveShowInfo(
                            jukebox.position(),
                            jukebox.discItemId(),
                            jukebox.profile().bpm(),
                            jukebox.ticksSinceSongStarted(),
                            BUILT_IN_PROFILES.containsKey(
                                    jukebox.discItemId()
                            ),
                            jukebox.profile().steadyBeat()
                    )
            );
        }

        shows.sort(
                Comparator.comparing(
                        ActiveShowInfo::discItemId
                )
        );

        return List.copyOf(
                shows
        );
    }

    private static ActiveJukebox findNearestActiveJukebox(
            Level level,
            BlockPos fixturePosition
    ) {
        ResourceKey<Level> dimension =
                level.dimension();

        removeStaleJukeboxes(
                dimension,
                level.getGameTime()
        );

        Map<BlockPos, ActiveJukebox> dimensionJukeboxes =
                ACTIVE_JUKEBOXES.get(
                        dimension
                );

        if (dimensionJukeboxes == null
                || dimensionJukeboxes.isEmpty()) {

            return null;
        }

        BlockPos safeFixturePosition =
                fixturePosition == null
                        ? BlockPos.ZERO
                        : fixturePosition;

        ActiveJukebox nearest =
                null;

        double nearestDistance =
                Double.MAX_VALUE;

        for (ActiveJukebox candidate :
                dimensionJukeboxes.values()) {

            double distance =
                    squaredDistance(
                            safeFixturePosition,
                            candidate.position()
                    );

            if (distance < nearestDistance) {
                nearest =
                        candidate;

                nearestDistance =
                        distance;
            }
        }

        return nearest;
    }

    private static FixtureOutput buildShowOutput(
            ActiveJukebox jukebox,
            FixtureOutput base,
            MovementStyle movementStyle
    ) {
        return buildBeatShowOutput(
                getBeatPosition(jukebox),
                jukebox.discItemId().hashCode(),
                base,
                movementStyle
        );
    }

    private static double getBeatPosition(
            ActiveJukebox jukebox
    ) {
        DiscBeatProfile profile =
                jukebox.profile();

        double ticksPerBeat =
                TICKS_PER_MINUTE
                        / profile.bpm();

        double adjustedTicks =
                Math.floorDiv(
                        jukebox.ticksSinceSongStarted(),
                        SHOW_UPDATE_INTERVAL_TICKS
                )
                        * SHOW_UPDATE_INTERVAL_TICKS
                        - profile.offsetSeconds()
                        * 20.0D;

        return adjustedTicks
                / ticksPerBeat;
    }

    private static FixtureOutput buildCommandPulseOutput(
            ActiveCommandPulse pulse,
            long currentGameTime,
            FixtureOutput base,
            MovementStyle movementStyle
    ) {
        double elapsedTicks =
                Math.max(
                        0.0D,
                        currentGameTime
                                - pulse.lastPulseGameTime()
                );

        double beatFraction =
                Math.min(
                        0.999999D,
                        elapsedTicks
                                / pulse.estimatedIntervalTicks()
                );

        double beatPosition =
                pulse.pulsesReceived()
                        - 1L
                        + beatFraction;

        return buildBeatShowOutput(
                beatPosition,
                COMMAND_PULSE_PALETTE_SEED,
                base,
                movementStyle
        );
    }

    private static FixtureOutput buildBeatShowOutput(
            double beatPosition,
            int paletteSeed,
            FixtureOutput base,
            MovementStyle movementStyle
    ) {

        long beatIndex =
                (long) Math.floor(
                        beatPosition
                );

        double beatFraction =
                beatPosition
                        - Math.floor(
                                beatPosition
                        );

        double pulse =
                0.30D
                        + 0.70D
                        * Math.pow(
                                1.0D - beatFraction,
                                2.0D
                        );

        boolean downbeat =
                Math.floorMod(
                        beatIndex,
                        4L
                ) == 0L;

        int dimmer =
                clampDmx(
                        (int) Math.round(
                                255.0D
                                        * pulse
                                        * (downbeat ? 1.0D : 0.88D)
                        )
                );

        int paletteStep =
                (int) Math.floorDiv(
                        beatIndex,
                        2L
                );

        int[] color =
                COLOR_PALETTE[
                        Math.floorMod(
                                Math.floorMod(
                                        paletteSeed,
                                        COLOR_PALETTE.length
                                ) + paletteStep,
                                COLOR_PALETTE.length
                        )
                ];

        int pan =
                base.getPan();

        int tilt =
                base.getTilt();

        if (movementStyle != MovementStyle.STATIC) {
            double halfCycleBeats =
                    movementStyle == MovementStyle.DISCO_BALL
                            ? DISCO_SPIN_HALF_CYCLE_BEATS
                            : FIXTURE_MOVEMENT_HALF_CYCLE_BEATS;

            double movementPhase =
                    beatPosition
                            * Math.PI
                            / halfCycleBeats;

            pan =
                    clampDmx(
                            (int) Math.round(
                                    128.0D
                                            + 96.0D
                                            * Math.sin(
                                                    movementPhase
                                            )
                            )
                    );

            if (movementStyle == MovementStyle.FIXTURE) {
                tilt =
                        clampDmx(
                                (int) Math.round(
                                        128.0D
                                                + 56.0D
                                                * Math.sin(
                                                        movementPhase
                                                                * 2.0D
                                                                + Math.PI / 2.0D
                                                )
                                )
                        );
            }
        }

        FixtureOutput output =
                new FixtureOutput(
                        color[0],
                        color[1],
                        color[2],
                        0,
                        0,
                        dimmer,
                        pan,
                        tilt,
                        base.getBeamWidth(),
                        base.getBeamLength(),
                        0
                );

        output.setGobo(
                base.getGobo()
        );

        return output;
    }

    private static ActiveCommandPulse findActiveCommandPulse(
            Level level
    ) {
        if (level == null || level.isClientSide()) {
            return null;
        }

        ResourceKey<Level> dimension =
                level.dimension();

        ActiveCommandPulse pulse =
                ACTIVE_COMMAND_PULSES.get(
                        dimension
                );

        if (pulse == null) {
            return null;
        }

        long currentGameTime =
                level.getGameTime();

        long elapsedTicks =
                currentGameTime
                        - pulse.lastPulseGameTime();

        if (elapsedTicks < 0L
                || elapsedTicks >= COMMAND_PULSE_TIMEOUT_TICKS) {

            ACTIVE_COMMAND_PULSES.remove(
                    dimension
            );

            return null;
        }

        return pulse;
    }

    private static void removeStaleJukeboxes(
            ResourceKey<Level> dimension,
            long currentGameTime
    ) {
        Map<BlockPos, ActiveJukebox> dimensionJukeboxes =
                ACTIVE_JUKEBOXES.get(
                        dimension
                );

        if (dimensionJukeboxes == null) {
            return;
        }

        Iterator<ActiveJukebox> iterator =
                dimensionJukeboxes.values()
                        .iterator();

        while (iterator.hasNext()) {
            ActiveJukebox jukebox =
                    iterator.next();

            long lastSeen =
                    jukebox.lastSeenGameTime();

            if (currentGameTime < lastSeen
                    || currentGameTime - lastSeen
                    > JUKEBOX_STALE_TICKS) {

                iterator.remove();
            }
        }

        if (dimensionJukeboxes.isEmpty()) {
            ACTIVE_JUKEBOXES.remove(
                    dimension
            );
        }
    }

    private static double squaredDistance(
            BlockPos first,
            BlockPos second
    ) {
        double x =
                first.getX() - second.getX();

        double y =
                first.getY() - second.getY();

        double z =
                first.getZ() - second.getZ();

        return x * x + y * y + z * z;
    }

    private static long skinRandomSeed(
            ActiveJukebox jukebox,
            BlockPos fixturePosition,
            long fixtureSeed
    ) {
        BlockPos safePosition =
                fixturePosition == null
                        ? BlockPos.ZERO
                        : fixturePosition;

        long positionSeed =
                safePosition.getX() * 3129871L
                        ^ safePosition.getY() * 42317861L
                        ^ safePosition.getZ() * 116129781L;

        long jukeboxSeed =
                jukebox.position().getX() * 73428767L
                        ^ jukebox.position().getY() * 912931L
                        ^ jukebox.position().getZ() * 438289L;

        return mix64(
                SKIN_RANDOM_SALT
                        ^ fixtureSeed
                        ^ positionSeed
                        ^ jukeboxSeed
                        ^ jukebox.discItemId().hashCode()
        );
    }

    private static long mix64(long value) {
        value ^= value >>> 33;
        value *= 0xFF51AFD7ED558CCDL;
        value ^= value >>> 33;
        value *= 0xC4CEB9FE1A85EC53L;
        return value ^ value >>> 33;
    }

    private static Map.Entry<String, DiscBeatProfile> profile(
            String discItemId,
            double bpm
    ) {
        return Map.entry(
                discItemId,
                new DiscBeatProfile(
                        bpm,
                        0.0D,
                        true
                )
        );
    }

    private static Map.Entry<String, DiscBeatProfile> cinematicProfile(
            String discItemId,
            double showPulseBpm
    ) {
        return Map.entry(
                discItemId,
                new DiscBeatProfile(
                        showPulseBpm,
                        0.0D,
                        false
                )
        );
    }

    private static int clampDmx(
            int value
    ) {
        return Math.clamp(
                value,
                FixtureOutput.MIN_VALUE,
                FixtureOutput.MAX_VALUE
        );
    }

    private record DiscBeatProfile(
            double bpm,
            double offsetSeconds,
            boolean steadyBeat
    ) {
    }

    private enum MovementStyle {
        STATIC,
        FIXTURE,
        DISCO_BALL
    }

    private record ActiveJukebox(
            BlockPos position,
            String discItemId,
            DiscBeatProfile profile,
            long ticksSinceSongStarted,
            long lastSeenGameTime
    ) {
    }

    private record ActiveCommandPulse(
            long pulsesReceived,
            long lastPulseGameTime,
            double estimatedIntervalTicks,
            boolean tempoMeasured
    ) {
    }

    public record ActiveShowInfo(
            BlockPos position,
            String discItemId,
            double bpm,
            long ticksSinceSongStarted,
            boolean builtInProfile,
            boolean steadyBeat
    ) {
    }

    public record PulseShowInfo(
            long pulsesReceived,
            double estimatedBpm,
            boolean tempoMeasured,
            double secondsUntilTimeout
    ) {
    }
}
