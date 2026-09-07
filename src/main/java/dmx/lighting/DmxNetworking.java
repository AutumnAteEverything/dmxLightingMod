package dmx.lighting;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Locale;

/**
 * Registers all networking used by dmxLighting.
 *
 * Client-to-server packets:
 *
 * UpdateFixturePayload
 *     Saves fixture name and universe.
 *
 * UpdateFixtureGroupPayload
 *     Assigns a named fixture group.
 *
 * UpdateFixtureProfilePayload
 *     Selects and stores a registered visual fixture profile.
 *
 * UpdateFixtureParameterMapPayload
 *     Stores explicit parameter-to-DMX-channel assignments.
 *
 * UpdateFixtureMountOrientationPayload
 *     Stores physical installation pan and tilt.
 *
 * UpdateFixturePanTiltInterpolationPayload
 *     Stores optional Pan/Tilt smoothing and its transition time.
 *
 * UpdateFixtureColorInterpolationPayload
 *     Stores optional visible RGB fade and its transition time.
 *
 * UpdateFixtureBeamSettingsPayload
 *     Temporary compatibility packet for the former stored visual beam
 *     settings.
 *
 * UpdateFixtureBeamControlModesPayload
 *     Selects DMX or Manual control independently for Beam Width and
 *     Beam Length.
 *
 * UpdatePixelBlockSkinPayload
 *     Selects one of the five visual skins for a DMX Block.
 *
 * ManualFixtureOutputPayload
 *     Stores complete parameter-centric manual fixture output values.
 *
 * TargetedFixtureOutputPayload
 *     Stores legacy manual RGBD values for either one fixture or an
 *     entire named group.
 *
 * ConsoleFixtureOutputPayload
 *     Stores complete parameter-centric manual output for:
 *
 *     - One fixture
 *     - One named group
 *     - Every loaded fixture in the player's current dimension
 *
 * SetFixtureModePayload
 *     Changes a fixture between DMX and Manual modes.
 *
 * UpdateDmxParrotPayload
 *     Saves name, group, universe, RGBD patch data, and color fade
 *     settings for a DMX parrot.
 *
 * UpdateDmxEndermanPayload
 *     Saves name, group, universe, RGBD patch data, and color fade
 *     settings for a DMX Enderman.
 *
 * UpdateDmxWardenPayload
 *     Saves name, group, universe, RGBD patch data, and color fade
 *     settings for a DMX Warden.
 *
 * UpdateDmxNautilusPayload
 *     Saves name, group, universe, RGBD patch data, and color fade
 *     settings for a DMX Nautilus.
 *
 * UpdateDmxBlockDisplayPayload
 *     Saves patch, skin, and color fade settings for a DMX Block
 *     Display entity.
 *
 * RequestFixtureBrowserPayload
 *     Requests a current browser snapshot for the player's dimension.
 *
 * Server-to-client packets:
 *
 * FixtureBrowserDataPayload
 *     Contains the current fixture-browser entries.
 */
public final class DmxNetworking {

    private DmxNetworking() {
        // Utility class: do not instantiate.
    }

    /**
     * Registers every custom payload and server-side receiver.
     */
    public static void initialize() {

        /*
         * -------------------------------------------------------------
         * Payload type registration
         * -------------------------------------------------------------
         */

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixturePayload.TYPE,
                UpdateFixturePayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixtureGroupPayload.TYPE,
                UpdateFixtureGroupPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixtureProfilePayload.TYPE,
                UpdateFixtureProfilePayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixtureParameterMapPayload.TYPE,
                UpdateFixtureParameterMapPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixtureMountOrientationPayload.TYPE,
                UpdateFixtureMountOrientationPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixturePanTiltInterpolationPayload.TYPE,
                UpdateFixturePanTiltInterpolationPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixtureColorInterpolationPayload.TYPE,
                UpdateFixtureColorInterpolationPayload.STREAM_CODEC
        );

        /*
         * Temporary compatibility registration.
         */
        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixtureBeamSettingsPayload.TYPE,
                UpdateFixtureBeamSettingsPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateFixtureBeamControlModesPayload.TYPE,
                UpdateFixtureBeamControlModesPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdatePixelBlockSkinPayload.TYPE,
                UpdatePixelBlockSkinPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                ManualFixtureOutputPayload.TYPE,
                ManualFixtureOutputPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                TargetedFixtureOutputPayload.TYPE,
                TargetedFixtureOutputPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                ConsoleFixtureOutputPayload.TYPE,
                ConsoleFixtureOutputPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                SetFixtureModePayload.TYPE,
                SetFixtureModePayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateDmxParrotPayload.TYPE,
                UpdateDmxParrotPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateDmxEndermanPayload.TYPE,
                UpdateDmxEndermanPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateDmxWardenPayload.TYPE,
                UpdateDmxWardenPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateDmxNautilusPayload.TYPE,
                UpdateDmxNautilusPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                UpdateDmxBlockDisplayPayload.TYPE,
                UpdateDmxBlockDisplayPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                RequestFixtureBrowserPayload.TYPE,
                RequestFixtureBrowserPayload.STREAM_CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                FixtureBrowserDataPayload.TYPE,
                FixtureBrowserDataPayload.STREAM_CODEC
        );

        /*
         * -------------------------------------------------------------
         * Server receiver registration
         * -------------------------------------------------------------
         */

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixturePayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleFixtureUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixtureGroupPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleGroupUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixtureProfilePayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleProfileUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixtureParameterMapPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleParameterMapUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixtureMountOrientationPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleMountOrientationUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixturePanTiltInterpolationPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handlePanTiltInterpolationUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixtureColorInterpolationPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleColorInterpolationUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixtureBeamSettingsPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleBeamSettingsUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateFixtureBeamControlModesPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleBeamControlModesUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdatePixelBlockSkinPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handlePixelBlockSkinUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ManualFixtureOutputPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleManualOutput(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TargetedFixtureOutputPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleTargetedOutput(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ConsoleFixtureOutputPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleConsoleOutput(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                SetFixtureModePayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleModeChange(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateDmxParrotPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleDmxParrotUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateDmxEndermanPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleDmxEndermanUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateDmxWardenPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleDmxWardenUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateDmxNautilusPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleDmxNautilusUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateDmxBlockDisplayPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleDmxBlockDisplayUpdate(
                                        context.player(),
                                        payload
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                RequestFixtureBrowserPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () -> handleBrowserRequest(
                                        context.player()
                                )
                        )
        );

        DmxLighting.LOGGER.info(
                "Registered dmxLighting networking."
        );
    }

    /*
     * -----------------------------------------------------------------
     * Fixture configuration
     * -----------------------------------------------------------------
     */

    private static void handleFixtureUpdate(
            ServerPlayer player,
            UpdateFixturePayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        int universe =
                clamp(
                        payload.universe(),
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        DmxFixtureBlockEntity.MAX_UNIVERSE
                );

        String fixtureName =
                payload.fixtureName() == null
                        ? ""
                        : payload.fixtureName().trim();

        if (fixtureName.length()
                > FixtureIdentity.MAX_NAME_LENGTH) {

            fixtureName =
                    fixtureName.substring(
                            0,
                            FixtureIdentity.MAX_NAME_LENGTH
                    );
        }

        fixture.setFixtureName(
                fixtureName
        );

        fixture.setUniverse(
                universe
        );

        fixture.refreshFromDmx();

        player.sendSystemMessage(
                Component.literal(
                        "Updated DMX fixture at "
                                + formatPosition(
                                        payload.position()
                                )
                                + "."
                )
        );
    }

    private static void handleGroupUpdate(
            ServerPlayer player,
            UpdateFixtureGroupPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        FixtureGroupName group =
                FixtureGroupName.of(
                        payload.groupName()
                );

        fixture.setFixtureGroup(
                group
        );

        if (group.isUngrouped()) {
            player.sendSystemMessage(
                    Component.literal(
                            "Fixture at "
                                    + formatPosition(
                                            payload.position()
                                    )
                                    + " is now ungrouped."
                    )
            );
        } else {
            player.sendSystemMessage(
                    Component.literal(
                            "Fixture at "
                                    + formatPosition(
                                            payload.position()
                                    )
                                    + " assigned to group \""
                                    + group.displayName()
                                    + "\"."
                    )
            );
        }
    }

    private static void handleProfileUpdate(
            ServerPlayer player,
            UpdateFixtureProfilePayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        DmxFixtureProfile selectedProfile =
                DmxFixtureProfileRegistry.get(
                        payload.profileId()
                )
                .orElse(
                        null
                );

        if (selectedProfile == null) {
            player.sendSystemMessage(
                    Component.literal(
                            "Unknown fixture profile: "
                                    + payload.profileId()
                    )
            );

            return;
        }

        fixture.setFixtureType(
                selectedProfile.id()
        );

        player.sendSystemMessage(
                Component.literal(
                        "Fixture at "
                                + formatPosition(
                                        payload.position()
                                )
                                + " changed to "
                                + selectedProfile.displayName()
                                + "."
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Parameter map
     * -----------------------------------------------------------------
     */

    private static void handleParameterMapUpdate(
            ServerPlayer player,
            UpdateFixtureParameterMapPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        if (!isValidParameterChannel(
                payload.redChannel()
        )
                || !isValidParameterChannel(
                        payload.greenChannel()
                )
                || !isValidParameterChannel(
                        payload.blueChannel()
                )
                || !isValidParameterChannel(
                        payload.whiteChannel()
                )
                || !isValidParameterChannel(
                        payload.amberChannel()
                )
                || !isValidParameterChannel(
                        payload.dimmerChannel()
                )
                || !isValidParameterChannel(
                        payload.panChannel()
                )
                || !isValidParameterChannel(
                        payload.tiltChannel()
                )
                || !isValidParameterChannel(
                        payload.beamWidthChannel()
                )
                || !isValidParameterChannel(
                        payload.beamLengthChannel()
                )
                || !isValidParameterChannel(
                        payload.strobeChannel()
                )) {

            player.sendSystemMessage(
                    Component.literal(
                            "DMX parameter channels must be 0 or 1-512."
                    )
            );

            return;
        }

        FixtureParameterMap map =
                fixture.getParameterMap();

        map.set(
                payload.redChannel(),
                payload.greenChannel(),
                payload.blueChannel(),
                payload.whiteChannel(),
                payload.amberChannel(),
                payload.dimmerChannel(),
                payload.strobeChannel(),
                payload.panChannel(),
                payload.tiltChannel(),
                payload.beamWidthChannel(),
                payload.beamLengthChannel()
        );

        fixture.refreshFromDmx();
        fixture.setChanged();

        player.sendSystemMessage(
                Component.literal(
                        "Updated DMX parameter map for fixture at "
                                + formatPosition(
                                        payload.position()
                                )
                                + "."
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Mount orientation
     * -----------------------------------------------------------------
     */

    private static void handleMountOrientationUpdate(
            ServerPlayer player,
            UpdateFixtureMountOrientationPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        fixture.setMountOrientation(
                payload.panDegrees(),
                payload.tiltDegrees()
        );
    }

    private static void handlePanTiltInterpolationUpdate(
            ServerPlayer player,
            UpdateFixturePanTiltInterpolationPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        fixture.setPanTiltInterpolation(
                payload.enabled(),
                payload.timeSeconds()
        );
    }

    private static void handleColorInterpolationUpdate(
            ServerPlayer player,
            UpdateFixtureColorInterpolationPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        fixture.setColorInterpolation(
                payload.enabled(),
                payload.timeSeconds()
        );
    }

    /*
     * -----------------------------------------------------------------
     * Beam compatibility settings
     * -----------------------------------------------------------------
     */

    private static void handleBeamSettingsUpdate(
            ServerPlayer player,
            UpdateFixtureBeamSettingsPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        fixture.setBeamSettings(
                payload.widthDegrees(),
                payload.lengthBlocks()
        );
    }

    /*
     * -----------------------------------------------------------------
     * Beam parameter control modes
     * -----------------------------------------------------------------
     */

    private static void handleBeamControlModesUpdate(
            ServerPlayer player,
            UpdateFixtureBeamControlModesPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        FixtureParameterControlMode beamWidthMode =
                parseParameterControlMode(
                        payload.beamWidthMode()
                );

        FixtureParameterControlMode beamLengthMode =
                parseParameterControlMode(
                        payload.beamLengthMode()
                );

        if (beamWidthMode == null
                || beamLengthMode == null) {

            player.sendSystemMessage(
                    Component.literal(
                            "Beam control mode must be \"dmx\" or \"manual\"."
                    )
            );

            return;
        }

        fixture.setBeamWidthControlMode(
                beamWidthMode
        );

        fixture.setBeamLengthControlMode(
                beamLengthMode
        );
    }

    private static void handlePixelBlockSkinUpdate(
            ServerPlayer player,
            UpdatePixelBlockSkinPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        if (!(fixture
                instanceof DmxPixelBlockEntity pixelBlock)) {

            player.sendSystemMessage(
                    Component.literal(
                            "That block is not a DMX Block."
                    )
            );

            return;
        }

        pixelBlock.setSkinConfiguration(
                payload.skin(),
                payload.skinDmxChannel()
        );
    }

    /*
     * -----------------------------------------------------------------
     * Manual output
     * -----------------------------------------------------------------
     */

    private static void handleManualOutput(
            ServerPlayer player,
            ManualFixtureOutputPayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        FixtureOutput output =
                new FixtureOutput(
                        clampChannel(
                                payload.red()
                        ),
                        clampChannel(
                                payload.green()
                        ),
                        clampChannel(
                                payload.blue()
                        ),
                        clampChannel(
                                payload.white()
                        ),
                        clampChannel(
                                payload.amber()
                        ),
                        clampChannel(
                                payload.dimmer()
                        ),
                        clampChannel(
                                payload.pan()
                        ),
                        clampChannel(
                                payload.tilt()
                        ),
                        clampChannel(
                                payload.beamWidth()
                        ),
                        clampChannel(
                                payload.beamLength()
                        ),
                        clampChannel(
                                payload.strobe()
                        )
                );

        fixture.setManualOutput(
                output
        );
    }

    /*
     * -----------------------------------------------------------------
     * Legacy targeted RGBD output
     * -----------------------------------------------------------------
     */

    private static void handleTargetedOutput(
            ServerPlayer player,
            TargetedFixtureOutputPayload payload
    ) {
        DmxFixtureBlockEntity selectedFixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (selectedFixture == null) {
            return;
        }

        FixtureOutput output =
                new FixtureOutput(
                        payload.red(),
                        payload.green(),
                        payload.blue(),
                        payload.dimmer()
                );

        if (!payload.targetGroup()) {
            selectedFixture.setManualOutput(
                    output
            );

            return;
        }

        FixtureGroupName group =
                selectedFixture.getFixtureGroup();

        if (group == null
                || group.isUngrouped()) {

            player.sendSystemMessage(
                    Component.literal(
                            "This fixture is not assigned to a group."
                    )
            );

            return;
        }

        List<DmxFixtureBlockEntity> groupFixtures =
                DmxFixtureRegistry.getFixturesInGroup(
                        player.level().dimension(),
                        group
                );

        for (DmxFixtureBlockEntity fixture :
                groupFixtures) {

            fixture.setManualOutput(
                    output
            );
        }

        player.sendSystemMessage(
                Component.literal(
                        "Applied manual values to "
                                + groupFixtures.size()
                                + " loaded fixture"
                                + (
                                groupFixtures.size() == 1
                                        ? ""
                                        : "s"
                        )
                                + " in group \""
                                + group.displayName()
                                + "\"."
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Console parameter-centric output
     * -----------------------------------------------------------------
     */

    private static void handleConsoleOutput(
            ServerPlayer player,
            ConsoleFixtureOutputPayload payload
    ) {
        /*
         * Let the dedicated handler honor the payload's apply mask.
         *
         * This is important for live Lighting Console sliders.
         */
        ConsoleFixtureOutputHandler.handle(
                payload,
                player
        );
    }

    /*
     * -----------------------------------------------------------------
     * Whole-fixture control mode
     * -----------------------------------------------------------------
     */

    private static void handleModeChange(
            ServerPlayer player,
            SetFixtureModePayload payload
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture(
                        player,
                        payload.position()
                );

        if (fixture == null) {
            return;
        }

        String requestedMode =
                payload.mode() == null
                        ? ""
                        : payload.mode()
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

        switch (requestedMode) {
            case "dmx" -> {
                fixture.setControlMode(
                        FixtureControlMode.DMX
                );

                fixture.refreshFromDmx();
            }

            case "manual" ->
                    fixture.setControlMode(
                            FixtureControlMode.MANUAL
                    );

            default -> {
                player.sendSystemMessage(
                        Component.literal(
                                "Unknown fixture mode: "
                                        + payload.mode()
                        )
                );

                return;
            }
        }

        player.sendSystemMessage(
                Component.literal(
                        "Fixture at "
                                + formatPosition(
                                        payload.position()
                                )
                                + " set to "
                                + requestedMode
                                + " mode."
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Browser
     * -----------------------------------------------------------------
     */

    private static void handleDmxParrotUpdate(
            ServerPlayer player,
            UpdateDmxParrotPayload payload
    ) {
        if (player == null
                || payload == null) {

            return;
        }

        ServerLevel level =
                (ServerLevel) player.level();

        DmxParrotEntity parrot =
                DmxMobFixtureRegistry.getParrotByEntityId(
                        level.dimension(),
                        payload.entityId()
                );

        if (parrot == null) {
            Entity entity =
                    level.getEntity(
                            payload.entityId()
                    );

            if (entity instanceof DmxParrotEntity foundParrot) {
                parrot =
                        foundParrot;

                DmxMobFixtureRegistry.register(
                        foundParrot
                );
            }
        }

        if (parrot == null
                || parrot.isRemoved()) {

            player.sendSystemMessage(
                    Component.literal(
                            "That DMX parrot is not currently loaded."
                    )
            );

            return;
        }

        if (!isValidParameterChannel(
                payload.redChannel()
        )
                || !isValidParameterChannel(
                        payload.greenChannel()
                )
                || !isValidParameterChannel(
                        payload.blueChannel()
                )
                || !isValidParameterChannel(
                        payload.dimmerChannel()
                )) {

            player.sendSystemMessage(
                    Component.literal(
                            "DMX parrot channels must be blank or 1-512."
                    )
            );

            return;
        }

        int universe =
                clamp(
                        payload.universe(),
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        DmxFixtureBlockEntity.MAX_UNIVERSE
                );

        parrot.setFixtureName(
                payload.fixtureName()
        );

        parrot.setFixtureGroup(
                FixtureGroupName.of(
                        payload.groupName()
                )
        );

        parrot.setUniverse(
                universe
        );

        parrot.setParameterMap(
                payload.redChannel(),
                payload.greenChannel(),
                payload.blueChannel(),
                payload.dimmerChannel()
        );

        parrot.setColorInterpolation(
                payload.colorInterpolationEnabled(),
                payload.colorInterpolationTimeSeconds()
        );

        player.sendSystemMessage(
                Component.literal(
                        "Updated "
                                + parrot.getConsoleName()
                                + "."
                )
        );
    }

    private static void handleDmxEndermanUpdate(
            ServerPlayer player,
            UpdateDmxEndermanPayload payload
    ) {
        if (player == null
                || payload == null) {

            return;
        }

        ServerLevel level =
                (ServerLevel) player.level();

        DmxEndermanEntity enderman =
                DmxMobFixtureRegistry.getEndermanByEntityId(
                        level.dimension(),
                        payload.entityId()
                );

        if (enderman == null) {
            Entity entity =
                    level.getEntity(
                            payload.entityId()
                    );

            if (entity instanceof DmxEndermanEntity foundEnderman) {
                enderman =
                        foundEnderman;

                DmxMobFixtureRegistry.register(
                        foundEnderman
                );
            }
        }

        if (enderman == null
                || enderman.isRemoved()) {

            player.sendSystemMessage(
                    Component.literal(
                            "That DMX Enderman is not currently loaded."
                    )
            );

            return;
        }

        if (!isValidParameterChannel(
                payload.redChannel()
        )
                || !isValidParameterChannel(
                        payload.greenChannel()
                )
                || !isValidParameterChannel(
                        payload.blueChannel()
                )
                || !isValidParameterChannel(
                        payload.dimmerChannel()
                )) {

            player.sendSystemMessage(
                    Component.literal(
                            "DMX Enderman channels must be blank or 1-512."
                    )
            );

            return;
        }

        int universe =
                clamp(
                        payload.universe(),
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        DmxFixtureBlockEntity.MAX_UNIVERSE
                );

        enderman.setFixtureName(
                payload.fixtureName()
        );

        enderman.setFixtureGroup(
                FixtureGroupName.of(
                        payload.groupName()
                )
        );

        enderman.setUniverse(
                universe
        );

        enderman.setParameterMap(
                payload.redChannel(),
                payload.greenChannel(),
                payload.blueChannel(),
                payload.dimmerChannel()
        );

        enderman.setColorInterpolation(
                payload.colorInterpolationEnabled(),
                payload.colorInterpolationTimeSeconds()
        );

        player.sendSystemMessage(
                Component.literal(
                        "Updated "
                                + enderman.getConsoleName()
                                + "."
                )
        );
    }

    private static void handleDmxWardenUpdate(
            ServerPlayer player,
            UpdateDmxWardenPayload payload
    ) {
        if (player == null
                || payload == null) {

            return;
        }

        ServerLevel level =
                (ServerLevel) player.level();

        DmxWardenEntity warden =
                DmxMobFixtureRegistry.getWardenByEntityId(
                        level.dimension(),
                        payload.entityId()
                );

        if (warden == null) {
            Entity entity =
                    level.getEntity(
                            payload.entityId()
                    );

            if (entity instanceof DmxWardenEntity foundWarden) {
                warden =
                        foundWarden;

                DmxMobFixtureRegistry.register(
                        foundWarden
                );
            }
        }

        if (warden == null
                || warden.isRemoved()) {

            player.sendSystemMessage(
                    Component.literal(
                            "That DMX Warden is not currently loaded."
                    )
            );

            return;
        }

        if (!isValidParameterChannel(
                payload.redChannel()
        )
                || !isValidParameterChannel(
                        payload.greenChannel()
                )
                || !isValidParameterChannel(
                        payload.blueChannel()
                )
                || !isValidParameterChannel(
                        payload.dimmerChannel()
                )) {

            player.sendSystemMessage(
                    Component.literal(
                            "DMX Warden channels must be blank or 1-512."
                    )
            );

            return;
        }

        int universe =
                clamp(
                        payload.universe(),
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        DmxFixtureBlockEntity.MAX_UNIVERSE
                );

        warden.setFixtureName(
                payload.fixtureName()
        );

        warden.setFixtureGroup(
                FixtureGroupName.of(
                        payload.groupName()
                )
        );

        warden.setUniverse(
                universe
        );

        warden.setParameterMap(
                payload.redChannel(),
                payload.greenChannel(),
                payload.blueChannel(),
                payload.dimmerChannel()
        );

        warden.setColorInterpolation(
                payload.colorInterpolationEnabled(),
                payload.colorInterpolationTimeSeconds()
        );

        player.sendSystemMessage(
                Component.literal(
                        "Updated "
                                + warden.getConsoleName()
                                + "."
                )
        );
    }

    private static void handleDmxNautilusUpdate(
            ServerPlayer player,
            UpdateDmxNautilusPayload payload
    ) {
        if (player == null
                || payload == null) {

            return;
        }

        ServerLevel level =
                (ServerLevel) player.level();

        DmxNautilusEntity nautilus =
                DmxMobFixtureRegistry.getNautilusByEntityId(
                        level.dimension(),
                        payload.entityId()
                );

        if (nautilus == null) {
            Entity entity =
                    level.getEntity(
                            payload.entityId()
                    );

            if (entity instanceof DmxNautilusEntity foundNautilus) {
                nautilus =
                        foundNautilus;

                DmxMobFixtureRegistry.register(
                        foundNautilus
                );
            }
        }

        if (nautilus == null
                || nautilus.isRemoved()) {

            player.sendSystemMessage(
                    Component.literal(
                            "That DMX Nautilus is not currently loaded."
                    )
            );

            return;
        }

        if (!isValidParameterChannel(
                payload.redChannel()
        )
                || !isValidParameterChannel(
                        payload.greenChannel()
                )
                || !isValidParameterChannel(
                        payload.blueChannel()
                )
                || !isValidParameterChannel(
                        payload.dimmerChannel()
                )) {

            player.sendSystemMessage(
                    Component.literal(
                            "DMX Nautilus channels must be blank or 1-512."
                    )
            );

            return;
        }

        int universe =
                clamp(
                        payload.universe(),
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        DmxFixtureBlockEntity.MAX_UNIVERSE
                );

        nautilus.setFixtureName(
                payload.fixtureName()
        );

        nautilus.setFixtureGroup(
                FixtureGroupName.of(
                        payload.groupName()
                )
        );

        nautilus.setUniverse(
                universe
        );

        nautilus.setParameterMap(
                payload.redChannel(),
                payload.greenChannel(),
                payload.blueChannel(),
                payload.dimmerChannel()
        );

        nautilus.setColorInterpolation(
                payload.colorInterpolationEnabled(),
                payload.colorInterpolationTimeSeconds()
        );

        player.sendSystemMessage(
                Component.literal(
                        "Updated "
                                + nautilus.getConsoleName()
                                + "."
                )
        );
    }

    private static void handleDmxBlockDisplayUpdate(
            ServerPlayer player,
            UpdateDmxBlockDisplayPayload payload
    ) {
        if (player == null || payload == null) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        DmxBlockDisplayEntity display =
                DmxBlockDisplayRegistry.getByEntityId(
                        level.dimension(),
                        payload.entityId()
                );

        if (display == null) {
            Entity entity = level.getEntity(payload.entityId());
            if (entity instanceof DmxBlockDisplayEntity foundDisplay) {
                display = foundDisplay;
                DmxBlockDisplayRegistry.register(foundDisplay);
            }
        }

        if (display == null || display.isRemoved()) {
            player.sendSystemMessage(
                    Component.literal(
                            "That DMX Block Display is not currently loaded."
                    )
            );
            return;
        }

        if (!isValidParameterChannel(payload.redChannel())
                || !isValidParameterChannel(payload.greenChannel())
                || !isValidParameterChannel(payload.blueChannel())
                || !isValidParameterChannel(payload.dimmerChannel())
                || !isValidParameterChannel(payload.strobeChannel())
                || !isValidParameterChannel(payload.skinDmxChannel())) {
            player.sendSystemMessage(
                    Component.literal(
                            "DMX Block Display channels must be blank or 1-512."
                    )
            );
            return;
        }

        display.setFixtureName(payload.fixtureName());
        display.setFixtureGroup(
                FixtureGroupName.of(payload.groupName())
        );
        display.setUniverse(
                clamp(
                        payload.universe(),
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        DmxFixtureBlockEntity.MAX_UNIVERSE
                )
        );
        display.setParameterMap(
                payload.redChannel(),
                payload.greenChannel(),
                payload.blueChannel(),
                payload.dimmerChannel(),
                payload.strobeChannel()
        );
        display.setSkinConfiguration(
                payload.skin(),
                payload.skinDmxChannel()
        );
        display.setColorInterpolation(
                payload.colorInterpolationEnabled(),
                payload.colorInterpolationTimeSeconds()
        );
        display.refreshFromDmx();

        player.sendSystemMessage(
                Component.literal(
                        "Updated " + display.getConsoleName() + "."
                )
        );
    }

    private static void handleBrowserRequest(
            ServerPlayer player
    ) {
        if (!ServerPlayNetworking.canSend(
                player,
                FixtureBrowserDataPayload.TYPE
        )) {
            DmxLighting.LOGGER.warn(
                    "Could not send fixture browser data to {} because "
                            + "the client cannot receive the payload.",
                    player.getName().getString()
            );

            return;
        }

        List<FixtureBrowserEntry> entries =
                LightingConsole.getAllFixtures(
                        player.level().dimension(),
                        LightingConsole.FixtureSortMode.PATCH
                );

        ServerPlayNetworking.send(
                player,
                new FixtureBrowserDataPayload(
                        entries
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Fixture lookup
     * -----------------------------------------------------------------
     */

    private static DmxFixtureBlockEntity getFixture(
            ServerPlayer player,
            BlockPos position
    ) {
        if (position == null) {
            player.sendSystemMessage(
                    Component.literal(
                            "No fixture position was supplied."
                    )
            );

            return null;
        }

        if (!player.level().isLoaded(
                position
        )) {
            player.sendSystemMessage(
                    Component.literal(
                            "That DMX fixture is not currently loaded."
                    )
            );

            return null;
        }

        BlockEntity blockEntity =
                player.level().getBlockEntity(
                        position
                );

        if (!(blockEntity
                instanceof DmxFixtureBlockEntity fixture)) {

            player.sendSystemMessage(
                    Component.literal(
                            "The block at "
                                    + formatPosition(
                                            position
                                    )
                                    + " is not a DMX fixture."
                    )
            );

            return null;
        }

        DmxFixtureRegistry.register(
                fixture
        );

        return fixture;
    }

    /*
     * -----------------------------------------------------------------
     * Validation helpers
     * -----------------------------------------------------------------
     */

    private static boolean isValidParameterChannel(
            int channel
    ) {
        return channel
                == FixtureParameterMap.UNASSIGNED
                || FixtureParameterMap.isAssigned(
                        channel
                );
    }

    private static FixtureParameterControlMode
    parseParameterControlMode(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return switch (normalized) {
            case "dmx" ->
                    FixtureParameterControlMode.DMX;

            case "manual" ->
                    FixtureParameterControlMode.MANUAL;

            default ->
                    null;
        };
    }

    private static int clampChannel(
            int value
    ) {
        return clamp(
                value,
                FixtureOutput.MIN_VALUE,
                FixtureOutput.MAX_VALUE
        );
    }

    private static int clamp(
            int value,
            int minimum,
            int maximum
    ) {
        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }

    private static String formatPosition(
            BlockPos position
    ) {
        return position.getX()
                + ", "
                + position.getY()
                + ", "
                + position.getZ();
    }
}
