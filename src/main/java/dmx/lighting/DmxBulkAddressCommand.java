package dmx.lighting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Registers a bulk DMX parameter-mapping command for fixtures.
 *
 * Syntax:
 *
 * /dmxaddress <from> <to> <universe> <startChannel>
 *
 * Example:
 *
 * /dmxaddress 0 64 0 9 64 0 1 1
 *
 * All DMX fixtures in the selected rectangular region receive
 * sequential four-channel RGBD parameter assignments:
 *
 * Fixture 1:
 *
 * Red     = 1
 * Green   = 2
 * Blue    = 3
 * Dimmer  = 4
 *
 * Fixture 2:
 *
 * Red     = 5
 * Green   = 6
 * Blue    = 7
 * Dimmer  = 8
 *
 * etc.
 *
 * White, Amber, Pan, Tilt, Beam Width, Beam Length, and Strobe are
 * left unassigned.
 *
 * This command no longer uses or stores a fixture Base Channel.
 */
public final class DmxBulkAddressCommand {

    /**
     * Standard bulk-addressing footprint:
     *
     * Red
     * Green
     * Blue
     * Dimmer
     */
    private static final int RGBD_CHANNEL_COUNT =
            4;

    /**
     * Highest possible starting channel for one complete RGBD block.
     */
    private static final int MAX_START_CHANNEL =
            DmxUniverse.MAX_CHANNEL
                    - RGBD_CHANNEL_COUNT
                    + 1;

    private DmxBulkAddressCommand() {
        // Utility class: do not instantiate.
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            Commands.literal(
                                    "dmxaddress"
                            )
                            .then(
                                    Commands.argument(
                                            "from",
                                            BlockPosArgument.blockPos()
                                    )
                                    .then(
                                            Commands.argument(
                                                    "to",
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
                                                    .then(
                                                            Commands.argument(
                                                                    "startChannel",
                                                                    IntegerArgumentType.integer(
                                                                            DmxUniverse.MIN_CHANNEL,
                                                                            MAX_START_CHANNEL
                                                                    )
                                                            )
                                                            .executes(
                                                                    context -> {

                                                                        BlockPos from =
                                                                                BlockPosArgument.getLoadedBlockPos(
                                                                                        context,
                                                                                        "from"
                                                                                );

                                                                        BlockPos to =
                                                                                BlockPosArgument.getLoadedBlockPos(
                                                                                        context,
                                                                                        "to"
                                                                                );

                                                                        int universe =
                                                                                IntegerArgumentType.getInteger(
                                                                                        context,
                                                                                        "universe"
                                                                                );

                                                                        int startChannel =
                                                                                IntegerArgumentType.getInteger(
                                                                                        context,
                                                                                        "startChannel"
                                                                                );

                                                                        ServerLevel level =
                                                                                context.getSource()
                                                                                        .getLevel();

                                                                        List<BlockPos> fixtures =
                                                                                findFixtures(
                                                                                        level,
                                                                                        from,
                                                                                        to
                                                                                );

                                                                        if (fixtures.isEmpty()) {
                                                                            context.getSource().sendFailure(
                                                                                    Component.literal(
                                                                                            "No DMX fixtures were found in the selected region."
                                                                                    )
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        /*
                                                                         * Each fixture requires four sequential
                                                                         * channels.
                                                                         *
                                                                         * Calculate the final channel that would
                                                                         * be consumed before modifying anything.
                                                                         */
                                                                        int finalChannel =
                                                                                startChannel
                                                                                        + (
                                                                                        fixtures.size()
                                                                                                * RGBD_CHANNEL_COUNT
                                                                                )
                                                                                        - 1;

                                                                        if (finalChannel
                                                                                > DmxUniverse.MAX_CHANNEL) {

                                                                            context.getSource().sendFailure(
                                                                                    Component.literal(
                                                                                            "The selected region contains "
                                                                                                    + fixtures.size()
                                                                                                    + " fixtures, but assigning RGBD channels from "
                                                                                                    + startChannel
                                                                                                    + " would exceed DMX channel "
                                                                                                    + DmxUniverse.MAX_CHANNEL
                                                                                                    + "."
                                                                                    )
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        int nextChannel =
                                                                                startChannel;

                                                                        for (BlockPos position :
                                                                                fixtures) {

                                                                            BlockEntity blockEntity =
                                                                                    level.getBlockEntity(
                                                                                            position
                                                                                    );

                                                                            if (!(blockEntity
                                                                                    instanceof DmxFixtureBlockEntity fixture)) {

                                                                                continue;
                                                                            }

                                                                            /*
                                                                             * Set the fixture universe.
                                                                             */
                                                                            fixture.setUniverse(
                                                                                    universe
                                                                            );

                                                                            /*
                                                                             * Build a standard sequential RGBD
                                                                             * parameter map.
                                                                             *
                                                                             * The remaining parameters are
                                                                             * deliberately unassigned.
                                                                             */
                                                                            FixtureParameterMap map =
                                                                                    fixture.getParameterMap();

                                                                            map.set(
                                                                                    /*
                                                                                     * Red
                                                                                     */
                                                                                    nextChannel,

                                                                                    /*
                                                                                     * Green
                                                                                     */
                                                                                    nextChannel + 1,

                                                                                    /*
                                                                                     * Blue
                                                                                     */
                                                                                    nextChannel + 2,

                                                                                    /*
                                                                                     * White
                                                                                     */
                                                                                    FixtureParameterMap.UNASSIGNED,

                                                                                    /*
                                                                                     * Amber
                                                                                     */
                                                                                    FixtureParameterMap.UNASSIGNED,

                                                                                    /*
                                                                                     * Dimmer
                                                                                     */
                                                                                    nextChannel + 3,

                                                                                    /*
                                                                                     * Strobe
                                                                                     */
                                                                                    FixtureParameterMap.UNASSIGNED,

                                                                                    /*
                                                                                     * Pan
                                                                                     */
                                                                                    FixtureParameterMap.UNASSIGNED,

                                                                                    /*
                                                                                     * Tilt
                                                                                     */
                                                                                    FixtureParameterMap.UNASSIGNED,

                                                                                    /*
                                                                                     * Beam Width
                                                                                     */
                                                                                    FixtureParameterMap.UNASSIGNED,

                                                                                    /*
                                                                                     * Beam Length
                                                                                     */
                                                                                    FixtureParameterMap.UNASSIGNED
                                                                            );

                                                                            /*
                                                                             * Re-read DMX using the new
                                                                             * assignments.
                                                                             */
                                                                            fixture.refreshFromDmx();

                                                                            /*
                                                                             * The parameter map itself is mutable,
                                                                             * so explicitly mark the block entity
                                                                             * changed after editing it.
                                                                             */
                                                                            fixture.setChanged();

                                                                            nextChannel +=
                                                                                    RGBD_CHANNEL_COUNT;
                                                                        }

                                                                        int firstChannel =
                                                                                startChannel;

                                                                        int lastChannel =
                                                                                finalChannel;

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "Mapped "
                                                                                                + fixtures.size()
                                                                                                + " DMX fixtures in universe "
                                                                                                + universe
                                                                                                + " as sequential RGBD channels "
                                                                                                + firstChannel
                                                                                                + "-"
                                                                                                + lastChannel
                                                                                                + "."
                                                                                ),
                                                                                false
                                                                        );

                                                                        return fixtures.size();
                                                                    }
                                                            )
                                                    )
                                            )
                                    )
                            )
                    );
                }
        );

        DmxLighting.LOGGER.info(
                "Registered the /dmxaddress command."
        );
    }

    /**
     * Finds all DMX fixtures in the rectangular region.
     *
     * Ordering:
     *
     * Y is the outer loop.
     * Z is the middle loop.
     * X is the inner loop.
     *
     * Consequently, fixtures in a horizontal X-axis row receive
     * consecutive channel assignments from low X to high X.
     */
    private static List<BlockPos> findFixtures(
            ServerLevel level,
            BlockPos from,
            BlockPos to
    ) {
        int minimumX =
                Math.min(
                        from.getX(),
                        to.getX()
                );

        int maximumX =
                Math.max(
                        from.getX(),
                        to.getX()
                );

        int minimumY =
                Math.min(
                        from.getY(),
                        to.getY()
                );

        int maximumY =
                Math.max(
                        from.getY(),
                        to.getY()
                );

        int minimumZ =
                Math.min(
                        from.getZ(),
                        to.getZ()
                );

        int maximumZ =
                Math.max(
                        from.getZ(),
                        to.getZ()
                );

        List<BlockPos> fixtures =
                new ArrayList<>();

        for (
                int y = minimumY;
                y <= maximumY;
                y++
        ) {
            for (
                    int z = minimumZ;
                    z <= maximumZ;
                    z++
            ) {
                for (
                        int x = minimumX;
                        x <= maximumX;
                        x++
                ) {
                    BlockPos position =
                            new BlockPos(
                                    x,
                                    y,
                                    z
                            );

                    BlockEntity blockEntity =
                            level.getBlockEntity(
                                    position
                            );

                    if (blockEntity
                            instanceof DmxFixtureBlockEntity) {

                        fixtures.add(
                                position
                        );
                    }
                }
            }
        }

        return fixtures;
    }
}