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
 * Output changes modify each block fixture's stored MANUAL values.
 *
 * DMX mobs write to their assigned DMX channels instead, because they
 * are DMX-only for now.
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

        DmxBlockDisplayEntity targetedDisplay =
                DmxBlockDisplayRegistry.getByEntityId(
                        level.dimension(),
                        payload.targetEntityId()
                );

        if (targetedDisplay != null
                && !targetedDisplay.isRemoved()) {
            applyPayloadToBlockDisplay(targetedDisplay, payload);
            return;
        }

        DmxParrotEntity targetedParrot =
                DmxMobFixtureRegistry.getParrotByEntityId(
                        level.dimension(),
                        payload.targetEntityId()
                );

        if (targetedParrot != null
                && !targetedParrot.isRemoved()) {

            applyPayloadToParrot(
                    targetedParrot,
                    payload
            );

            return;
        }

        DmxEndermanEntity targetedEnderman =
                DmxMobFixtureRegistry.getEndermanByEntityId(
                        level.dimension(),
                        payload.targetEntityId()
                );

        if (targetedEnderman != null
                && !targetedEnderman.isRemoved()) {

            applyPayloadToEnderman(
                    targetedEnderman,
                    payload
            );

            return;
        }

        DmxWardenEntity targetedWarden =
                DmxMobFixtureRegistry.getWardenByEntityId(
                        level.dimension(),
                        payload.targetEntityId()
                );

        if (targetedWarden != null
                && !targetedWarden.isRemoved()) {

            applyPayloadToWarden(
                    targetedWarden,
                    payload
            );

            return;
        }

        DmxNautilusEntity targetedNautilus =
                DmxMobFixtureRegistry.getNautilusByEntityId(
                        level.dimension(),
                        payload.targetEntityId()
                );

        if (targetedNautilus != null
                && !targetedNautilus.isRemoved()) {

            applyPayloadToNautilus(
                    targetedNautilus,
                    payload
            );

            return;
        }

        DmxCreakingEntity targetedCreaking =
                DmxMobFixtureRegistry.getCreakingByEntityId(
                        level.dimension(),
                        payload.targetEntityId()
                );

        if (targetedCreaking != null && !targetedCreaking.isRemoved()) {
            applyPayloadToCreaking(targetedCreaking, payload);
            return;
        }

        DmxAxolotlEntity targetedAxolotl =
                DmxMobFixtureRegistry.getAxolotlByEntityId(
                        level.dimension(),
                        payload.targetEntityId()
                );

        if (targetedAxolotl != null && !targetedAxolotl.isRemoved()) {
            applyPayloadToAxolotl(targetedAxolotl, payload);
            return;
        }

        DmxFixtureBlockEntity fixture =
                DmxFixtureRegistry.getFixture(
                        level.dimension(),
                        position
                );

        if (fixture != null
                && !fixture.isRemoved()) {

            applyPayloadToFixture(
                    fixture,
                    payload
            );
            return;
        }

        DmxParrotEntity parrot =
                DmxMobFixtureRegistry.getParrotAt(
                        level.dimension(),
                        position
                );

        applyPayloadToParrot(
                parrot,
                payload
        );

        DmxEndermanEntity enderman =
                DmxMobFixtureRegistry.getEndermanAt(
                        level.dimension(),
                        position
                );

        applyPayloadToEnderman(
                enderman,
                payload
        );

        DmxWardenEntity warden =
                DmxMobFixtureRegistry.getWardenAt(
                        level.dimension(),
                        position
                );

        applyPayloadToWarden(
                warden,
                payload
        );

        DmxNautilusEntity nautilus =
                DmxMobFixtureRegistry.getNautilusAt(
                        level.dimension(),
                        position
                );

        applyPayloadToNautilus(
                nautilus,
                payload
        );

        applyPayloadToCreaking(
                DmxMobFixtureRegistry.getCreakingAt(
                        level.dimension(),
                        position
                ),
                payload
        );

        applyPayloadToAxolotl(
                DmxMobFixtureRegistry.getAxolotlAt(
                        level.dimension(),
                        position
                ),
                payload
        );

        applyPayloadToBlockDisplay(
                DmxBlockDisplayRegistry.getAt(
                        level.dimension(),
                        position
                ),
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

        FixtureGroupName group =
                null;

        if (sourceFixture != null
                && !sourceFixture.isRemoved()
                && !sourceFixture.isUngrouped()) {

            group =
                    sourceFixture.getFixtureGroup();
        }

        if (group == null
                || group.isUngrouped()) {

            DmxParrotEntity sourceParrot =
                    DmxMobFixtureRegistry.getParrotByEntityId(
                            level.dimension(),
                            payload.targetEntityId()
                    );

            if (sourceParrot == null) {
                sourceParrot =
                        DmxMobFixtureRegistry.getParrotAt(
                                level.dimension(),
                                sourcePosition
                        );
            }

            if (sourceParrot != null
                    && !sourceParrot.isRemoved()
                    && !sourceParrot.isUngrouped()) {

                group =
                        sourceParrot.getFixtureGroup();
            }
        }

        if (group == null
                || group.isUngrouped()) {

            DmxEndermanEntity sourceEnderman =
                    DmxMobFixtureRegistry.getEndermanByEntityId(
                            level.dimension(),
                            payload.targetEntityId()
                    );

            if (sourceEnderman == null) {
                sourceEnderman =
                        DmxMobFixtureRegistry.getEndermanAt(
                                level.dimension(),
                                sourcePosition
                        );
            }

            if (sourceEnderman != null
                    && !sourceEnderman.isRemoved()
                    && !sourceEnderman.isUngrouped()) {

                group =
                        sourceEnderman.getFixtureGroup();
            }
        }

        if (group == null
                || group.isUngrouped()) {

            DmxWardenEntity sourceWarden =
                    DmxMobFixtureRegistry.getWardenByEntityId(
                            level.dimension(),
                            payload.targetEntityId()
                    );

            if (sourceWarden == null) {
                sourceWarden =
                        DmxMobFixtureRegistry.getWardenAt(
                                level.dimension(),
                                sourcePosition
                        );
            }

            if (sourceWarden != null
                    && !sourceWarden.isRemoved()
                    && !sourceWarden.isUngrouped()) {

                group =
                        sourceWarden.getFixtureGroup();
            }
        }

        if (group == null
                || group.isUngrouped()) {

            DmxNautilusEntity sourceNautilus =
                    DmxMobFixtureRegistry.getNautilusByEntityId(
                            level.dimension(),
                            payload.targetEntityId()
                    );

            if (sourceNautilus == null) {
                sourceNautilus =
                        DmxMobFixtureRegistry.getNautilusAt(
                                level.dimension(),
                                sourcePosition
                        );
            }

            if (sourceNautilus != null
                    && !sourceNautilus.isRemoved()
                    && !sourceNautilus.isUngrouped()) {

                group =
                        sourceNautilus.getFixtureGroup();
            }
        }

        if (group == null || group.isUngrouped()) {
            DmxCreakingEntity sourceCreaking =
                    DmxMobFixtureRegistry.getCreakingByEntityId(
                            level.dimension(),
                            payload.targetEntityId()
                    );
            if (sourceCreaking == null) {
                sourceCreaking = DmxMobFixtureRegistry.getCreakingAt(
                        level.dimension(),
                        sourcePosition
                );
            }
            if (sourceCreaking != null && !sourceCreaking.isUngrouped()) {
                group = sourceCreaking.getFixtureGroup();
            }
        }

        if (group == null || group.isUngrouped()) {
            DmxAxolotlEntity sourceAxolotl =
                    DmxMobFixtureRegistry.getAxolotlByEntityId(
                            level.dimension(),
                            payload.targetEntityId()
                    );
            if (sourceAxolotl == null) {
                sourceAxolotl = DmxMobFixtureRegistry.getAxolotlAt(
                        level.dimension(),
                        sourcePosition
                );
            }
            if (sourceAxolotl != null && !sourceAxolotl.isUngrouped()) {
                group = sourceAxolotl.getFixtureGroup();
            }
        }

        if (group == null
                || group.isUngrouped()) {

            DmxBlockDisplayEntity sourceDisplay =
                    DmxBlockDisplayRegistry.getByEntityId(
                            level.dimension(),
                            payload.targetEntityId()
                    );

            if (sourceDisplay == null) {
                sourceDisplay = DmxBlockDisplayRegistry.getAt(
                        level.dimension(),
                        sourcePosition
                );
            }

            if (sourceDisplay != null
                    && !sourceDisplay.isRemoved()
                    && !sourceDisplay.isUngrouped()) {
                group = sourceDisplay.getFixtureGroup();
            }
        }

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

        applyPayloadToParrots(
                DmxMobFixtureRegistry.getParrotsInGroup(
                        level.dimension(),
                        group
                ),
                payload
        );

        applyPayloadToEndermen(
                DmxMobFixtureRegistry.getEndermenInGroup(
                        level.dimension(),
                        group
                ),
                payload
        );

        applyPayloadToWardens(
                DmxMobFixtureRegistry.getWardensInGroup(
                        level.dimension(),
                        group
                ),
                payload
        );

        applyPayloadToNautiluses(
                DmxMobFixtureRegistry.getNautilusesInGroup(
                        level.dimension(),
                        group
                ),
                payload
        );

        applyPayloadToCreakings(
                DmxMobFixtureRegistry.getCreakingsInGroup(level.dimension(), group),
                payload
        );

        applyPayloadToAxolotls(
                DmxMobFixtureRegistry.getAxolotlsInGroup(level.dimension(), group),
                payload
        );

        applyPayloadToBlockDisplays(
                DmxBlockDisplayRegistry.getDisplaysInGroup(
                        level.dimension(),
                        group
                ),
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

        applyPayloadToParrots(
                DmxMobFixtureRegistry.getParrotsInDimension(
                        level.dimension()
                ),
                payload
        );

        applyPayloadToEndermen(
                DmxMobFixtureRegistry.getEndermenInDimension(
                        level.dimension()
                ),
                payload
        );

        applyPayloadToWardens(
                DmxMobFixtureRegistry.getWardensInDimension(
                        level.dimension()
                ),
                payload
        );

        applyPayloadToNautiluses(
                DmxMobFixtureRegistry.getNautilusesInDimension(
                        level.dimension()
                ),
                payload
        );

        applyPayloadToCreakings(
                DmxMobFixtureRegistry.getCreakingsInDimension(level.dimension()),
                payload
        );

        applyPayloadToAxolotls(
                DmxMobFixtureRegistry.getAxolotlsInDimension(level.dimension()),
                payload
        );

        applyPayloadToBlockDisplays(
                DmxBlockDisplayRegistry.getDisplaysInDimension(
                        level.dimension()
                ),
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

    private static void applyPayloadToParrots(
            List<DmxParrotEntity> parrots,
            ConsoleFixtureOutputPayload payload
    ) {
        if (parrots == null
                || parrots.isEmpty()
                || payload == null) {

            return;
        }

        List<DmxParrotEntity> safeParrots =
                new ArrayList<>(
                        parrots
                );

        for (DmxParrotEntity parrot :
                safeParrots) {

            applyPayloadToParrot(
                    parrot,
                    payload
            );
        }
    }

    private static void applyPayloadToEndermen(
            List<DmxEndermanEntity> endermen,
            ConsoleFixtureOutputPayload payload
    ) {
        if (endermen == null
                || endermen.isEmpty()
                || payload == null) {

            return;
        }

        List<DmxEndermanEntity> safeEndermen =
                new ArrayList<>(
                        endermen
                );

        for (DmxEndermanEntity enderman :
                safeEndermen) {

            applyPayloadToEnderman(
                    enderman,
                    payload
            );
        }
    }

    private static void applyPayloadToWardens(
            List<DmxWardenEntity> wardens,
            ConsoleFixtureOutputPayload payload
    ) {
        if (wardens == null
                || wardens.isEmpty()
                || payload == null) {

            return;
        }

        List<DmxWardenEntity> safeWardens =
                new ArrayList<>(
                        wardens
                );

        for (DmxWardenEntity warden :
                safeWardens) {

            applyPayloadToWarden(
                    warden,
                    payload
            );
        }
    }

    private static void applyPayloadToNautiluses(
            List<DmxNautilusEntity> nautiluses,
            ConsoleFixtureOutputPayload payload
    ) {
        if (nautiluses == null
                || nautiluses.isEmpty()
                || payload == null) {

            return;
        }

        List<DmxNautilusEntity> safeNautiluses =
                new ArrayList<>(
                        nautiluses
                );

        for (DmxNautilusEntity nautilus :
                safeNautiluses) {

            applyPayloadToNautilus(
                    nautilus,
                    payload
            );
        }
    }

    private static void applyPayloadToCreakings(
            List<DmxCreakingEntity> creakings,
            ConsoleFixtureOutputPayload payload
    ) {
        if (creakings == null || payload == null) {
            return;
        }
        for (DmxCreakingEntity creaking : new ArrayList<>(creakings)) {
            applyPayloadToCreaking(creaking, payload);
        }
    }

    private static void applyPayloadToAxolotls(
            List<DmxAxolotlEntity> axolotls,
            ConsoleFixtureOutputPayload payload
    ) {
        if (axolotls == null || payload == null) {
            return;
        }
        for (DmxAxolotlEntity axolotl : new ArrayList<>(axolotls)) {
            applyPayloadToAxolotl(axolotl, payload);
        }
    }

    private static void applyPayloadToBlockDisplays(
            List<DmxBlockDisplayEntity> displays,
            ConsoleFixtureOutputPayload payload
    ) {
        if (displays == null || displays.isEmpty() || payload == null) {
            return;
        }

        for (DmxBlockDisplayEntity display :
                new ArrayList<>(displays)) {
            applyPayloadToBlockDisplay(display, payload);
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

    private static void applyPayloadToParrot(
            DmxParrotEntity parrot,
            ConsoleFixtureOutputPayload payload
    ) {
        if (parrot == null
                || parrot.isRemoved()
                || payload == null) {

            return;
        }

        parrot.applyConsoleDmxOutput(
                payload
        );
    }

    private static void applyPayloadToEnderman(
            DmxEndermanEntity enderman,
            ConsoleFixtureOutputPayload payload
    ) {
        if (enderman == null
                || enderman.isRemoved()
                || payload == null) {

            return;
        }

        enderman.applyConsoleDmxOutput(
                payload
        );
    }

    private static void applyPayloadToWarden(
            DmxWardenEntity warden,
            ConsoleFixtureOutputPayload payload
    ) {
        if (warden == null
                || warden.isRemoved()
                || payload == null) {

            return;
        }

        warden.applyConsoleDmxOutput(
                payload
        );
    }

    private static void applyPayloadToNautilus(
            DmxNautilusEntity nautilus,
            ConsoleFixtureOutputPayload payload
    ) {
        if (nautilus == null
                || nautilus.isRemoved()
                || payload == null) {

            return;
        }

        nautilus.applyConsoleDmxOutput(
                payload
        );
    }

    private static void applyPayloadToCreaking(
            DmxCreakingEntity creaking,
            ConsoleFixtureOutputPayload payload
    ) {
        if (creaking != null && !creaking.isRemoved() && payload != null) {
            creaking.applyConsoleDmxOutput(payload);
        }
    }

    private static void applyPayloadToAxolotl(
            DmxAxolotlEntity axolotl,
            ConsoleFixtureOutputPayload payload
    ) {
        if (axolotl != null && !axolotl.isRemoved() && payload != null) {
            axolotl.applyConsoleDmxOutput(payload);
        }
    }

    private static void applyPayloadToBlockDisplay(
            DmxBlockDisplayEntity display,
            ConsoleFixtureOutputPayload payload
    ) {
        if (display == null || display.isRemoved() || payload == null) {
            return;
        }

        display.applyConsoleDmxOutput(payload);
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
