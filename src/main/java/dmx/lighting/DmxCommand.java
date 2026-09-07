package dmx.lighting;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

/**
 * Registers command-based control of DMX universes and fixtures.
 *
 * Raw DMX:
 *
 * /dmx <universe> <channel> <value>
 *
 * Fixture configuration:
 *
 * /dmxfixture configure <position> <universe>
 *
 * /dmxfixture set <position> universe <number>
 * /dmxfixture set <position> name <fixtureName>
 * /dmxfixture set <position> type <fixtureType>
 *
 * Parameter-centric DMX patching:
 *
 * /dmxfixture set <position> channel red <channel>
 * /dmxfixture set <position> channel green <channel>
 * /dmxfixture set <position> channel blue <channel>
 * /dmxfixture set <position> channel white <channel>
 * /dmxfixture set <position> channel amber <channel>
 * /dmxfixture set <position> channel dimmer <channel>
 * /dmxfixture set <position> channel pan <channel>
 * /dmxfixture set <position> channel tilt <channel>
 * /dmxfixture set <position> channel beam_width <channel>
 * /dmxfixture set <position> channel beam_length <channel>
 * /dmxfixture set <position> channel strobe <channel>
 *
 * Channel 0 means unassigned.
 *
 * Inspection:
 *
 * /dmxfixture inspect <position>
 *
 * Beam diagnostics:
 *
 * /dmxfixture beamdebug <position>
 */
public final class DmxCommand {

    private DmxCommand() {
        // Utility class: do not instantiate.
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    /*
                     * -------------------------------------------------
                     * Raw DMX command
                     * -------------------------------------------------
                     */

                    dispatcher.register(
                            Commands.literal(
                                    "dmx"
                            )
                            .then(
                                    Commands.argument(
                                            "universe",
                                            IntegerArgumentType.integer(
                                                    DmxUniverseManager.MIN_UNIVERSE,
                                                    DmxUniverseManager.MAX_UNIVERSE
                                            )
                                    )
                                    .then(
                                            Commands.argument(
                                                    "channel",
                                                    IntegerArgumentType.integer(
                                                            DmxUniverse.MIN_CHANNEL,
                                                            DmxUniverse.MAX_CHANNEL
                                                    )
                                            )
                                            .then(
                                                    Commands.argument(
                                                            "value",
                                                            IntegerArgumentType.integer(
                                                                    DmxUniverse.MIN_VALUE,
                                                                    DmxUniverse.MAX_VALUE
                                                            )
                                                    )
                                                    .executes(
                                                            context -> {
                                                                int universe =
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "universe"
                                                                        );

                                                                int channel =
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "channel"
                                                                        );

                                                                int value =
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "value"
                                                                        );

                                                                DmxUniverseManager.setChannel(
                                                                        universe,
                                                                        channel,
                                                                        value
                                                                );

                                                                context.getSource().sendSuccess(
                                                                        () -> Component.literal(
                                                                                "DMX universe "
                                                                                        + universe
                                                                                        + ", channel "
                                                                                        + channel
                                                                                        + " set to "
                                                                                        + value
                                                                                        + "."
                                                                        ),
                                                                        false
                                                                );

                                                                return 1;
                                                            }
                                                    )
                                            )
                                    )
                            )
                    );

                    /*
                     * -------------------------------------------------
                     * Fixture commands
                     * -------------------------------------------------
                     */

                    dispatcher.register(
                            Commands.literal(
                                    "dmxfixture"
                            )

                            /*
                             * Configure
                             */
                            .then(
                                    Commands.literal(
                                            "configure"
                                    )
                                    .then(
                                            Commands.argument(
                                                    "position",
                                                    BlockPosArgument.blockPos()
                                            )
                                            .then(
                                                    Commands.argument(
                                                            "universe",
                                                            IntegerArgumentType.integer(
                                                                    DmxFixtureBlockEntity.MIN_UNIVERSE,
                                                                    DmxFixtureBlockEntity.MAX_UNIVERSE
                                                            )
                                                    )
                                                    .executes(
                                                            context -> {
                                                                BlockPos position =
                                                                        BlockPosArgument.getLoadedBlockPos(
                                                                                context,
                                                                                "position"
                                                                        );

                                                                DmxFixtureBlockEntity fixture =
                                                                        getFixtureAt(
                                                                                context.getSource()
                                                                                        .getLevel()
                                                                                        .getBlockEntity(
                                                                                                position
                                                                                        )
                                                                        );

                                                                if (fixture == null) {
                                                                    sendNotFixtureMessage(
                                                                            context.getSource(),
                                                                            position
                                                                    );

                                                                    return 0;
                                                                }

                                                                int universe =
                                                                        IntegerArgumentType.getInteger(
                                                                                context,
                                                                                "universe"
                                                                        );

                                                                fixture.setUniverse(
                                                                        universe
                                                                );

                                                                fixture.refreshFromDmx();

                                                                context.getSource().sendSuccess(
                                                                        () -> Component.literal(
                                                                                "Configured fixture at "
                                                                                        + formatPosition(
                                                                                                position
                                                                                        )
                                                                                        + " for DMX universe "
                                                                                        + universe
                                                                                        + "."
                                                                        ),
                                                                        false
                                                                );

                                                                return 1;
                                                            }
                                                    )
                                            )
                                    )
                            )

                            /*
                             * Set
                             */
                            .then(
                                    Commands.literal(
                                            "set"
                                    )
                                    .then(
                                            Commands.argument(
                                                    "position",
                                                    BlockPosArgument.blockPos()
                                            )

                                            /*
                                             * Universe
                                             */
                                            .then(
                                                    Commands.literal(
                                                            "universe"
                                                    )
                                                    .then(
                                                            Commands.argument(
                                                                    "universe",
                                                                    IntegerArgumentType.integer(
                                                                            DmxFixtureBlockEntity.MIN_UNIVERSE,
                                                                            DmxFixtureBlockEntity.MAX_UNIVERSE
                                                                    )
                                                            )
                                                            .executes(
                                                                    context -> {
                                                                        BlockPos position =
                                                                                BlockPosArgument.getLoadedBlockPos(
                                                                                        context,
                                                                                        "position"
                                                                                );

                                                                        DmxFixtureBlockEntity fixture =
                                                                                getFixtureAt(
                                                                                        context.getSource()
                                                                                                .getLevel()
                                                                                                .getBlockEntity(
                                                                                                        position
                                                                                                )
                                                                                );

                                                                        if (fixture == null) {
                                                                            sendNotFixtureMessage(
                                                                                    context.getSource(),
                                                                                    position
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        int universe =
                                                                                IntegerArgumentType.getInteger(
                                                                                        context,
                                                                                        "universe"
                                                                                );

                                                                        fixture.setUniverse(
                                                                                universe
                                                                        );

                                                                        fixture.refreshFromDmx();

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "Fixture at "
                                                                                                + formatPosition(
                                                                                                        position
                                                                                                )
                                                                                                + " now uses universe "
                                                                                                + universe
                                                                                                + "."
                                                                                ),
                                                                                false
                                                                        );

                                                                        return 1;
                                                                    }
                                                            )
                                                    )
                                            )

                                            /*
                                             * Parameter channel
                                             */
                                            .then(
                                                    Commands.literal(
                                                            "channel"
                                                    )
                                                    .then(
                                                            Commands.argument(
                                                                    "parameter",
                                                                    StringArgumentType.word()
                                                            )
                                                            .then(
                                                                    Commands.argument(
                                                                            "channel",
                                                                            IntegerArgumentType.integer(
                                                                                    FixtureParameterMap.UNASSIGNED,
                                                                                    DmxUniverse.MAX_CHANNEL
                                                                            )
                                                                    )
                                                                    .executes(
                                                                            context -> {
                                                                                BlockPos position =
                                                                                        BlockPosArgument.getLoadedBlockPos(
                                                                                                context,
                                                                                                "position"
                                                                                        );

                                                                                DmxFixtureBlockEntity fixture =
                                                                                        getFixtureAt(
                                                                                                context.getSource()
                                                                                                        .getLevel()
                                                                                                        .getBlockEntity(
                                                                                                                position
                                                                                                        )
                                                                                        );

                                                                                if (fixture == null) {
                                                                                    sendNotFixtureMessage(
                                                                                            context.getSource(),
                                                                                            position
                                                                                    );

                                                                                    return 0;
                                                                                }

                                                                                String parameter =
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "parameter"
                                                                                        );

                                                                                int channel =
                                                                                        IntegerArgumentType.getInteger(
                                                                                                context,
                                                                                                "channel"
                                                                                        );

                                                                                if (!setParameterChannel(
                                                                                        fixture,
                                                                                        parameter,
                                                                                        channel
                                                                                )) {

                                                                                    context.getSource().sendFailure(
                                                                                            Component.literal(
                                                                                                    "Unknown DMX parameter: "
                                                                                                            + parameter
                                                                                                            + ". Supported: "
                                                                                                            + "red, green, blue, white, amber, "
                                                                                                            + "dimmer, pan, tilt, beam_width, "
                                                                                                            + "beam_length, strobe."
                                                                                            )
                                                                                    );

                                                                                    return 0;
                                                                                }

                                                                                fixture.refreshFromDmx();
                                                                                fixture.setChanged();

                                                                                String displayedChannel =
                                                                                        channel
                                                                                                == FixtureParameterMap.UNASSIGNED
                                                                                                ? "unassigned"
                                                                                                : Integer.toString(
                                                                                                        channel
                                                                                                );

                                                                                context.getSource().sendSuccess(
                                                                                        () -> Component.literal(
                                                                                                "Fixture at "
                                                                                                        + formatPosition(
                                                                                                                position
                                                                                                        )
                                                                                                        + ": "
                                                                                                        + normalizeParameterName(
                                                                                                                parameter
                                                                                                        )
                                                                                                        + " channel set to "
                                                                                                        + displayedChannel
                                                                                                        + "."
                                                                                        ),
                                                                                        false
                                                                                );

                                                                                return 1;
                                                                            }
                                                                    )
                                                            )
                                                    )
                                            )

                                            /*
                                             * Visual fixture type
                                             */
                                            .then(
                                                    Commands.literal(
                                                            "type"
                                                    )
                                                    .then(
                                                            Commands.argument(
                                                                    "fixtureType",
                                                                    StringArgumentType.word()
                                                            )
                                                            .executes(
                                                                    context -> {
                                                                        BlockPos position =
                                                                                BlockPosArgument.getLoadedBlockPos(
                                                                                        context,
                                                                                        "position"
                                                                                );

                                                                        DmxFixtureBlockEntity fixture =
                                                                                getFixtureAt(
                                                                                        context.getSource()
                                                                                                .getLevel()
                                                                                                .getBlockEntity(
                                                                                                        position
                                                                                                )
                                                                                );

                                                                        if (fixture == null) {
                                                                            sendNotFixtureMessage(
                                                                                    context.getSource(),
                                                                                    position
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        String fixtureType =
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "fixtureType"
                                                                                );

                                                                        String resolvedType =
                                                                                DmxFixtureProfileRegistry.resolveProfileId(
                                                                                        fixtureType
                                                                                );

                                                                        DmxFixtureProfile profile =
                                                                                DmxFixtureProfileRegistry.get(
                                                                                        resolvedType
                                                                                )
                                                                                .orElse(
                                                                                        null
                                                                                );

                                                                        if (profile == null) {
                                                                            context.getSource().sendFailure(
                                                                                    Component.literal(
                                                                                            "Unknown fixture profile: "
                                                                                                    + fixtureType
                                                                                    )
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        fixture.setFixtureType(
                                                                                profile.id()
                                                                        );

                                                                        fixture.refreshFromDmx();

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "Fixture at "
                                                                                                + formatPosition(
                                                                                                        position
                                                                                                )
                                                                                                + " now uses visual profile "
                                                                                                + profile.displayName()
                                                                                                + "."
                                                                                ),
                                                                                false
                                                                        );

                                                                        return 1;
                                                                    }
                                                            )
                                                    )
                                            )

                                            /*
                                             * Name
                                             */
                                            .then(
                                                    Commands.literal(
                                                            "name"
                                                    )
                                                    .then(
                                                            Commands.argument(
                                                                    "fixtureName",
                                                                    StringArgumentType.greedyString()
                                                            )
                                                            .executes(
                                                                    context -> {
                                                                        BlockPos position =
                                                                                BlockPosArgument.getLoadedBlockPos(
                                                                                        context,
                                                                                        "position"
                                                                                );

                                                                        DmxFixtureBlockEntity fixture =
                                                                                getFixtureAt(
                                                                                        context.getSource()
                                                                                                .getLevel()
                                                                                                .getBlockEntity(
                                                                                                        position
                                                                                                )
                                                                                );

                                                                        if (fixture == null) {
                                                                            sendNotFixtureMessage(
                                                                                    context.getSource(),
                                                                                    position
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        String fixtureName =
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "fixtureName"
                                                                                );

                                                                        fixture.setFixtureName(
                                                                                fixtureName
                                                                        );

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "Fixture at "
                                                                                                + formatPosition(
                                                                                                        position
                                                                                                )
                                                                                                + " renamed to \""
                                                                                                + fixture.getFixtureName()
                                                                                                + "\"."
                                                                                ),
                                                                                false
                                                                        );

                                                                        return 1;
                                                                    }
                                                            )
                                                    )
                                            )
                                    )
                            )

                            /*
                             * Inspect
                             */
                            .then(
                                    Commands.literal(
                                            "inspect"
                                    )
                                    .then(
                                            Commands.argument(
                                                    "position",
                                                    BlockPosArgument.blockPos()
                                            )
                                            .executes(
                                                    context -> {
                                                        BlockPos position =
                                                                BlockPosArgument.getLoadedBlockPos(
                                                                        context,
                                                                        "position"
                                                                );

                                                        DmxFixtureBlockEntity fixture =
                                                                getFixtureAt(
                                                                        context.getSource()
                                                                                .getLevel()
                                                                                .getBlockEntity(
                                                                                        position
                                                                                )
                                                                );

                                                        if (fixture == null) {
                                                            sendNotFixtureMessage(
                                                                    context.getSource(),
                                                                    position
                                                            );

                                                            return 0;
                                                        }

                                                        fixture.refreshFromDmx();

                                                        FixtureParameterMap map =
                                                                fixture.getParameterMap();

                                                        String fixtureName =
                                                                fixture.getFixtureName()
                                                                        .isBlank()
                                                                        ? "(unnamed)"
                                                                        : fixture.getFixtureName();

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "DMX Fixture \""
                                                                                + fixtureName
                                                                                + "\" at "
                                                                                + formatPosition(
                                                                                        position
                                                                                )
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Universe: "
                                                                                + fixture.getUniverse()
                                                                                + " | Profile: "
                                                                                + fixture.getFixtureType()
                                                                                + " | Mode: "
                                                                                + fixture.getControlMode()
                                                                                        .getSerializedName()
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Channels: "
                                                                                + "R="
                                                                                + formatChannel(
                                                                                        map.getRedChannel()
                                                                                )
                                                                                + " G="
                                                                                + formatChannel(
                                                                                        map.getGreenChannel()
                                                                                )
                                                                                + " B="
                                                                                + formatChannel(
                                                                                        map.getBlueChannel()
                                                                                )
                                                                                + " W="
                                                                                + formatChannel(
                                                                                        map.getWhiteChannel()
                                                                                )
                                                                                + " A="
                                                                                + formatChannel(
                                                                                        map.getAmberChannel()
                                                                                )
                                                                                + " Dim="
                                                                                + formatChannel(
                                                                                        map.getDimmerChannel()
                                                                                )
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Channels: "
                                                                                + "Pan="
                                                                                + formatChannel(
                                                                                        map.getPanChannel()
                                                                                )
                                                                                + " Tilt="
                                                                                + formatChannel(
                                                                                        map.getTiltChannel()
                                                                                )
                                                                                + " Width="
                                                                                + formatChannel(
                                                                                        map.getBeamWidthChannel()
                                                                                )
                                                                                + " Length="
                                                                                + formatChannel(
                                                                                        map.getBeamLengthChannel()
                                                                                )
                                                                                + " Strobe="
                                                                                + formatChannel(
                                                                                        map.getStrobeChannel()
                                                                                )
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "DMX values: "
                                                                                + "R="
                                                                                + fixture.getRed()
                                                                                + " G="
                                                                                + fixture.getGreen()
                                                                                + " B="
                                                                                + fixture.getBlue()
                                                                                + " W="
                                                                                + fixture.getWhite()
                                                                                + " A="
                                                                                + fixture.getAmber()
                                                                                + " Dim="
                                                                                + fixture.getDimmer()
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "DMX values: "
                                                                                + "Pan="
                                                                                + fixture.getDmxPan()
                                                                                + " Tilt="
                                                                                + fixture.getDmxTilt()
                                                                                + " Width="
                                                                                + fixture.getDmxBeamWidth()
                                                                                + " Length="
                                                                                + fixture.getDmxBeamLength()
                                                                                + " Strobe="
                                                                                + fixture.getDmxStrobe()
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Visible RGB: "
                                                                                + fixture.getOutputRed()
                                                                                + ", "
                                                                                + fixture.getOutputGreen()
                                                                                + ", "
                                                                                + fixture.getOutputBlue()
                                                                                + " | Minecraft light: "
                                                                                + fixture.getMinecraftLightLevel()
                                                                ),
                                                                false
                                                        );

                                                        return 1;
                                                    }
                                            )
                                    )
                            )

                            /*
                             * -------------------------------------------------
                             * Beam diagnostics
                             * -------------------------------------------------
                             *
                             * /dmxfixture beamdebug <position>
                             */
                            .then(
                                    Commands.literal(
                                            "beamdebug"
                                    )
                                    .then(
                                            Commands.argument(
                                                    "position",
                                                    BlockPosArgument.blockPos()
                                            )
                                            .executes(
                                                    context -> {
                                                        BlockPos position =
                                                                BlockPosArgument.getLoadedBlockPos(
                                                                        context,
                                                                        "position"
                                                                );

                                                        DmxFixtureBlockEntity fixture =
                                                                getFixtureAt(
                                                                        context.getSource()
                                                                                .getLevel()
                                                                                .getBlockEntity(
                                                                                        position
                                                                                )
                                                                );

                                                        if (fixture == null) {
                                                            sendNotFixtureMessage(
                                                                    context.getSource(),
                                                                    position
                                                            );

                                                            return 0;
                                                        }

                                                        Vec3 direction =
                                                                FixtureBeamDirection.fromFixture(
                                                                        fixture
                                                                );

                                                        Vec3 origin =
                                                                FixtureBeamLightManager.calculateBeamOrigin(
                                                                        fixture
                                                                );

                                                        List<BlockPos> samples =
                                                                FixtureBeamLightManager.calculateLightSamples(
                                                                        fixture
                                                                );

                                                        float visualConeDegrees =
                                                                FixtureBeamSettings
                                                                        .mapWidthToVisualConeDegrees(
                                                                                fixture.getBeamWidthDegrees()
                                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Beam debug for fixture at "
                                                                                + formatPosition(
                                                                                        position
                                                                                )
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Origin: "
                                                                                + formatVec3(
                                                                                        origin
                                                                                )
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Direction: "
                                                                                + formatVec3(
                                                                                        direction
                                                                                )
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Mount: pan="
                                                                                + formatDecimal(
                                                                                        fixture.getMountPanDegrees()
                                                                                )
                                                                                + " tilt="
                                                                                + formatDecimal(
                                                                                        fixture.getMountTiltDegrees()
                                                                                )
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Beam: width control="
                                                                                + fixture.getBeamWidthDegrees()
                                                                                + " | visual cone="
                                                                                + formatDecimal(
                                                                                        visualConeDegrees
                                                                                )
                                                                                + " deg | length="
                                                                                + fixture.getBeamLengthBlocks()
                                                                                + " blocks"
                                                                ),
                                                                false
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Output light level: "
                                                                                + fixture.getMinecraftLightLevel()
                                                                                + " | calculated samples: "
                                                                                + samples.size()
                                                                ),
                                                                false
                                                        );

                                                        int displayedSamples =
                                                                Math.min(
                                                                        8,
                                                                        samples.size()
                                                                );

                                                        for (
                                                                int index = 0;
                                                                index < displayedSamples;
                                                                index++
                                                        ) {
                                                            BlockPos sample =
                                                                    samples.get(
                                                                            index
                                                                    );

                                                            int sampleNumber =
                                                                    index + 1;

                                                            BlockState sampleState =
                                                                    context.getSource()
                                                                            .getLevel()
                                                                            .getBlockState(
                                                                                    sample
                                                                            );

                                                            String sampleStateText;

                                                            if (sampleState.getBlock()
                                                                    == Blocks.LIGHT) {

                                                                int lightLevel =
                                                                        sampleState.getValue(
                                                                                LightBlock.LEVEL
                                                                        );

                                                                sampleStateText =
                                                                        "LIGHT level="
                                                                                + lightLevel;
                                                            } else if (sampleState.isAir()) {

                                                                sampleStateText =
                                                                        "AIR";
                                                            } else {

                                                                sampleStateText =
                                                                        sampleState
                                                                                .getBlock()
                                                                                .getName()
                                                                                .getString();
                                                            }

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "Sample "
                                                                                    + sampleNumber
                                                                                    + ": "
                                                                                    + formatPosition(
                                                                                            sample
                                                                                    )
                                                                                    + " | "
                                                                                    + sampleStateText
                                                                    ),
                                                                    false
                                                            );
                                                        }

                                                        if (samples.size()
                                                                > displayedSamples) {

                                                            int remaining =
                                                                    samples.size()
                                                                            - displayedSamples;

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "... plus "
                                                                                    + remaining
                                                                                    + " more sample"
                                                                                    + (
                                                                                    remaining == 1
                                                                                            ? ""
                                                                                            : "s"
                                                                            )
                                                                                    + "."
                                                                    ),
                                                                    false
                                                            );
                                                        }

                                                        return 1;
                                                    }
                                            )
                                    )
                            )
                    );
                }
        );

        DmxLighting.LOGGER.info(
                "Registered dmxLighting commands."
        );
    }

    /*
     * -----------------------------------------------------------------
     * Parameter-map editing
     * -----------------------------------------------------------------
     */

    private static boolean setParameterChannel(
            DmxFixtureBlockEntity fixture,
            String parameter,
            int channel
    ) {
        FixtureParameterMap map =
                fixture.getParameterMap();

        String normalized =
                normalizeParameterName(
                        parameter
                );

        int red =
                map.getRedChannel();

        int green =
                map.getGreenChannel();

        int blue =
                map.getBlueChannel();

        int white =
                map.getWhiteChannel();

        int amber =
                map.getAmberChannel();

        int dimmer =
                map.getDimmerChannel();

        int strobe =
                map.getStrobeChannel();

        int pan =
                map.getPanChannel();

        int tilt =
                map.getTiltChannel();

        int beamWidth =
                map.getBeamWidthChannel();

        int beamLength =
                map.getBeamLengthChannel();

        switch (normalized) {
            case "red" ->
                    red = channel;

            case "green" ->
                    green = channel;

            case "blue" ->
                    blue = channel;

            case "white" ->
                    white = channel;

            case "amber" ->
                    amber = channel;

            case "dimmer" ->
                    dimmer = channel;

            case "pan" ->
                    pan = channel;

            case "tilt" ->
                    tilt = channel;

            case "beam_width" ->
                    beamWidth = channel;

            case "beam_length" ->
                    beamLength = channel;

            case "strobe" ->
                    strobe = channel;

            default -> {
                return false;
            }
        }

        map.set(
                red,
                green,
                blue,
                white,
                amber,
                dimmer,
                strobe,
                pan,
                tilt,
                beamWidth,
                beamLength
        );

        return true;
    }

    private static String normalizeParameterName(
            String parameter
    ) {
        if (parameter == null) {
            return "";
        }

        String normalized =
                parameter
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replace(
                                '-',
                                '_'
                        );

        return switch (normalized) {
            case "beamwidth", "width" ->
                    "beam_width";

            case "beamlength", "length" ->
                    "beam_length";

            case "dim", "intensity" ->
                    "dimmer";

            default ->
                    normalized;
        };
    }

    /*
     * -----------------------------------------------------------------
     * Fixture lookup
     * -----------------------------------------------------------------
     */

    private static DmxFixtureBlockEntity getFixtureAt(
            BlockEntity blockEntity
    ) {
        if (blockEntity
                instanceof DmxFixtureBlockEntity fixture) {

            return fixture;
        }

        return null;
    }

    private static void sendNotFixtureMessage(
            CommandSourceStack source,
            BlockPos position
    ) {
        source.sendFailure(
                Component.literal(
                        "The block at "
                                + formatPosition(
                                        position
                                )
                                + " is not a DMX fixture."
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Formatting
     * -----------------------------------------------------------------
     */

    private static String formatChannel(
            int channel
    ) {
        if (!FixtureParameterMap.isAssigned(
                channel
        )) {
            return "-";
        }

        return Integer.toString(
                channel
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

    private static String formatVec3(
            Vec3 vector
    ) {
        if (vector == null) {
            return "(null)";
        }

        return "("
                + formatDecimal(
                        vector.x
                )
                + ", "
                + formatDecimal(
                        vector.y
                )
                + ", "
                + formatDecimal(
                        vector.z
                )
                + ")";
    }

    private static String formatDecimal(
            double value
    ) {
        return String.format(
                Locale.ROOT,
                "%.3f",
                value
        );
    }
}