package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Server-side handler for Lighting Console output packets.
 *
 * Supported targets:
 *
 * fixture
 *     Applies output to one fixture identified by the packet position.
 *
 * group
 *     Uses the fixture at the packet position to determine the group,
 *     then applies output to every loaded fixture in that group.
 *
 * all
 *     Applies output to every loaded fixture in the player's current
 *     dimension.
 *
 * Output changes modify each fixture's stored MANUAL values.
 *
 * They do not automatically change the whole-fixture DMX / Manual
 * control mode.
 *
 * The packet's apply mask determines exactly which manual parameters
 * are modified.
 *
 * This handler intentionally modifies the existing FixtureOutput
 * directly rather than rebuilding the complete output from packet
 * values.
 *
 * This is especially important for group and all-fixture live control:
 *
 *     Moving Red changes only Red on every targeted fixture.
 *
 *     Each fixture retains its own Green, Blue, White, Amber, Dimmer,
 *     Pan, Tilt, Beam Width, Beam Length, and Strobe values.
 */
public final class ConsoleFixtureOutputHandler {

    private ConsoleFixtureOutputHandler() {
        // Utility class.
    }

    /*
     * -----------------------------------------------------------------
     * Main entry point
     * -----------------------------------------------------------------
     */

    /**
     * Handles one console-output request.
     *
     * This method is intended to be called from the registered
     * server-side networking receiver.
     */
    public static void handle(
            ConsoleFixtureOutputPayload payload,
            ServerPlayer player
    ) {
        if (payload == null
                || player == null
                || !payload.appliesAnyParameter()) {

            return;
        }

        ServerLevel level =
                (ServerLevel) player.level();

        String target =
                normalizeTarget(
                        payload.target()
                );

        switch (target) {
            case ConsoleFixtureOutputPayload.TARGET_FIXTURE ->
                    applyToFixture(
                            level,
                            payload
                    );

            case ConsoleFixtureOutputPayload.TARGET_GROUP ->
                    applyToGroup(
                            level,
                            payload
                    );

            case ConsoleFixtureOutputPayload.TARGET_ALL ->
                    applyToAll(
                            level,
                            payload
                    );

            default -> {
                /*
                 * Unknown or malformed target.
                 *
                 * Ignore the packet.
                 */
            }
        }
    }

    /*
     * -----------------------------------------------------------------
     * Fixture target
     * -----------------------------------------------------------------
     */

    private static void applyToFixture(
            ServerLevel level,
            ConsoleFixtureOutputPayload payload
    ) {
        if (level == null
                || payload == null) {

            return;
        }

        BlockPos position =
                payload.position();

        if (position == null) {
            return;
        }

        DmxFixtureBlockEntity fixture =
                DmxFixtureRegistry.getFixture(
                        level.dimension(),
                        position
                );

        if (fixture == null
                || fixture.isRemoved()) {

            return;
        }

        applyPayloadToFixture(
                fixture,
                payload
        );
    }

    /*
     * -----------------------------------------------------------------
     * Group target
     * -----------------------------------------------------------------
     */

    private static void applyToGroup(
            ServerLevel level,
            ConsoleFixtureOutputPayload payload
    ) {
        if (level == null
                || payload == null) {

            return;
        }

        BlockPos sourcePosition =
                payload.position();

        if (sourcePosition == null) {
            return;
        }

        /*
         * The packet identifies the group by providing the position of
         * one fixture belonging to that group.
         */
        DmxFixtureBlockEntity sourceFixture =
                DmxFixtureRegistry.getFixture(
                        level.dimension(),
                        sourcePosition
                );

        if (sourceFixture == null
                || sourceFixture.isRemoved()
                || sourceFixture.isUngrouped()) {

            return;
        }

        FixtureGroupName group =
                sourceFixture.getFixtureGroup();

        if (group == null
                || group.isUngrouped()) {

            return;
        }

        List<DmxFixtureBlockEntity> fixtures =
                DmxFixtureRegistry.getFixturesInGroup(
                        level.dimension(),
                        group
                );

        applyPayloadToFixtures(
                fixtures,
                payload
        );
    }

    /*
     * -----------------------------------------------------------------
     * All target
     * -----------------------------------------------------------------
     */

    private static void applyToAll(
            ServerLevel level,
            ConsoleFixtureOutputPayload payload
    ) {
        if (level == null
                || payload == null) {

            return;
        }

        List<DmxFixtureBlockEntity> fixtures =
                DmxFixtureRegistry.getFixturesInDimension(
                        level.dimension()
                );

        applyPayloadToFixtures(
                fixtures,
                payload
        );
    }

    /*
     * -----------------------------------------------------------------
     * Batch application
     * -----------------------------------------------------------------
     */

    private static void applyPayloadToFixtures(
            List<DmxFixtureBlockEntity> fixtures,
            ConsoleFixtureOutputPayload payload
    ) {
        if (fixtures == null
                || fixtures.isEmpty()
                || payload == null) {

            return;
        }

        /*
         * Defensive copy protects iteration from any future registry
         * modifications triggered while fixture state is changing.
         */
        List<DmxFixtureBlockEntity> safeFixtures =
                new ArrayList<>(
                        fixtures
                );

        for (DmxFixtureBlockEntity fixture :
                safeFixtures) {

            if (fixture == null
                    || fixture.isRemoved()) {

                continue;
            }

            applyPayloadToFixture(
                    fixture,
                    payload
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Selective parameter application
     * -----------------------------------------------------------------
     */

    /**
     * Applies only the parameters selected by the payload's apply mask.
     *
     * IMPORTANT:
     *
     * We intentionally modify the fixture's existing manual
     * FixtureOutput object in place.
     *
     * This means a one-parameter live group command cannot accidentally
     * replace the rest of each fixture's manual state with the console's
     * local slider values.
     */
    private static void applyPayloadToFixture(
            DmxFixtureBlockEntity fixture,
            ConsoleFixtureOutputPayload payload
    ) {
        if (fixture == null
                || payload == null) {

            return;
        }

        FixtureState state =
                fixture.getFixtureState();

        if (state == null) {
            return;
        }

        FixtureOutput manualOutput =
                state.getManualOutput();

        if (manualOutput == null) {
            return;
        }

        boolean changed =
                false;

        /*
         * -------------------------------------------------------------
         * Color
         * -------------------------------------------------------------
         */

        if (payload.appliesRed()) {
            int value =
                    clampDmx(
                            payload.red()
                    );

            if (manualOutput.getRed()
                    != value) {

                manualOutput.setRed(
                        value
                );

                changed =
                        true;
            }
        }

        if (payload.appliesGreen()) {
            int value =
                    clampDmx(
                            payload.green()
                    );

            if (manualOutput.getGreen()
                    != value) {

                manualOutput.setGreen(
                        value
                );

                changed =
                        true;
            }
        }

        if (payload.appliesBlue()) {
            int value =
                    clampDmx(
                            payload.blue()
                    );

            if (manualOutput.getBlue()
                    != value) {

                manualOutput.setBlue(
                        value
                );

                changed =
                        true;
            }
        }

        if (payload.appliesWhite()) {
            int value =
                    clampDmx(
                            payload.white()
                    );

            if (manualOutput.getWhite()
                    != value) {

                manualOutput.setWhite(
                        value
                );

                changed =
                        true;
            }
        }

        if (payload.appliesAmber()) {
            int value =
                    clampDmx(
                            payload.amber()
                    );

            if (manualOutput.getAmber()
                    != value) {

                manualOutput.setAmber(
                        value
                );

                changed =
                        true;
            }
        }

        /*
         * -------------------------------------------------------------
         * Intensity
         * -------------------------------------------------------------
         */

        if (payload.appliesDimmer()) {
            int value =
                    clampDmx(
                            payload.dimmer()
                    );

            if (manualOutput.getDimmer()
                    != value) {

                manualOutput.setDimmer(
                        value
                );

                changed =
                        true;
            }
        }

        /*
         * -------------------------------------------------------------
         * Movement
         * -------------------------------------------------------------
         */

        if (payload.appliesPan()) {
            int value =
                    clampDmx(
                            payload.pan()
                    );

            if (manualOutput.getPan()
                    != value) {

                manualOutput.setPan(
                        value
                );

                changed =
                        true;
            }
        }

        if (payload.appliesTilt()) {
            int value =
                    clampDmx(
                            payload.tilt()
                    );

            if (manualOutput.getTilt()
                    != value) {

                manualOutput.setTilt(
                        value
                );

                changed =
                        true;
            }
        }

        /*
         * -------------------------------------------------------------
         * Beam
         * -------------------------------------------------------------
         */

        if (payload.appliesBeamWidth()) {
            int value =
                    clampDmx(
                            payload.beamWidth()
                    );

            if (manualOutput.getBeamWidth()
                    != value) {

                manualOutput.setBeamWidth(
                        value
                );

                changed =
                        true;
            }
        }

        if (payload.appliesBeamLength()) {
            int value =
                    clampDmx(
                            payload.beamLength()
                    );

            if (manualOutput.getBeamLength()
                    != value) {

                manualOutput.setBeamLength(
                        value
                );

                changed =
                        true;
            }
        }

        /*
         * -------------------------------------------------------------
         * Effects
         * -------------------------------------------------------------
         */

        if (payload.appliesStrobe()) {
            int value =
                    clampDmx(
                            payload.strobe()
                    );

            if (manualOutput.getStrobe()
                    != value) {

                manualOutput.setStrobe(
                        value
                );

                changed =
                        true;
            }
        }

        /*
         * setChanged() performs persistence notification and sends the
         * block-entity update packet to clients.
         *
         * No call to setManualParameterOutput(...) is made here because
         * that method represents a complete manual-output replacement,
         * while live console traffic may represent only one parameter.
         */
        if (changed) {
            fixture.setChanged();
        }
    }

    /*
     * -----------------------------------------------------------------
     * Helpers
     * -----------------------------------------------------------------
     */

    private static String normalizeTarget(
            String target
    ) {
        if (target == null) {
            return "";
        }

        return target
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private static int clampDmx(
            int value
    ) {
        return Math.max(
                FixtureOutput.MIN_VALUE,
                Math.min(
                        FixtureOutput.MAX_VALUE,
                        value
                )
        );
    }
}