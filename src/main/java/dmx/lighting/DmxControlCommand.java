package dmx.lighting;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Adds Manual and DMX control commands to /dmxfixture.
 *
 * Commands:
 *
 * /dmxfixture mode <position> dmx
 * /dmxfixture mode <position> manual
 *
 * /dmxfixture manual <position> <red> <green> <blue> <dimmer>
 */
public final class DmxControlCommand {

    private DmxControlCommand() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(
                            Commands.literal("dmxfixture")

                                    /*
                                     * /dmxfixture mode <position> <mode>
                                     */
                                    .then(
                                            Commands.literal("mode")
                                                    .then(
                                                            Commands.argument(
                                                                    "position",
                                                                    BlockPosArgument.blockPos()
                                                            )
                                                            .then(
                                                                    Commands.argument(
                                                                            "mode",
                                                                            StringArgumentType.word()
                                                                    )
                                                                    .suggests(
                                                                            (context, builder) -> {
                                                                                builder.suggest("dmx");
                                                                                builder.suggest("manual");
                                                                                return builder.buildFuture();
                                                                            }
                                                                    )
                                                                    .executes(context -> {
                                                                        BlockPos position =
                                                                                BlockPosArgument.getLoadedBlockPos(
                                                                                        context,
                                                                                        "position"
                                                                                );

                                                                        DmxFixtureBlockEntity fixture =
                                                                                getFixtureAt(
                                                                                        context.getSource()
                                                                                                .getLevel()
                                                                                                .getBlockEntity(position)
                                                                                );

                                                                        if (fixture == null) {
                                                                            context.getSource().sendFailure(
                                                                                    Component.literal(
                                                                                            "The block at "
                                                                                                    + formatPosition(position)
                                                                                                    + " is not a DMX fixture."
                                                                                    )
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        String requestedMode =
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "mode"
                                                                                );

                                                                        FixtureControlMode mode =
                                                                                FixtureControlMode.fromName(
                                                                                        requestedMode
                                                                                );

                                                                        if (mode == null) {
                                                                            context.getSource().sendFailure(
                                                                                    Component.literal(
                                                                                            "Unknown fixture mode: "
                                                                                                    + requestedMode
                                                                                                    + ". Use dmx or manual."
                                                                                    )
                                                                            );

                                                                            return 0;
                                                                        }

                                                                        fixture.setControlMode(mode);

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "Fixture at "
                                                                                                + formatPosition(position)
                                                                                                + " is now in "
                                                                                                + mode.getSerializedName()
                                                                                                + " mode."
                                                                                ),
                                                                                false
                                                                        );

                                                                        return 1;
                                                                    })
                                                            )
                                                    )
                                    )

                                    /*
                                     * /dmxfixture manual
                                     *     <position> <red> <green> <blue> <dimmer>
                                     */
                                    .then(
                                            Commands.literal("manual")
                                                    .then(
                                                            Commands.argument(
                                                                    "position",
                                                                    BlockPosArgument.blockPos()
                                                            )
                                                            .then(
                                                                    Commands.argument(
                                                                            "red",
                                                                            IntegerArgumentType.integer(
                                                                                    0,
                                                                                    255
                                                                            )
                                                                    )
                                                                    .then(
                                                                            Commands.argument(
                                                                                    "green",
                                                                                    IntegerArgumentType.integer(
                                                                                            0,
                                                                                            255
                                                                                    )
                                                                            )
                                                                            .then(
                                                                                    Commands.argument(
                                                                                            "blue",
                                                                                            IntegerArgumentType.integer(
                                                                                                    0,
                                                                                                    255
                                                                                            )
                                                                                    )
                                                                                    .then(
                                                                                            Commands.argument(
                                                                                                    "dimmer",
                                                                                                    IntegerArgumentType.integer(
                                                                                                            0,
                                                                                                            255
                                                                                                    )
                                                                                            )
                                                                                            .executes(context -> {
                                                                                                BlockPos position =
                                                                                                        BlockPosArgument.getLoadedBlockPos(
                                                                                                                context,
                                                                                                                "position"
                                                                                                        );

                                                                                                DmxFixtureBlockEntity fixture =
                                                                                                        getFixtureAt(
                                                                                                                context.getSource()
                                                                                                                        .getLevel()
                                                                                                                        .getBlockEntity(position)
                                                                                                        );

                                                                                                if (fixture == null) {
                                                                                                    context.getSource().sendFailure(
                                                                                                            Component.literal(
                                                                                                                    "The block at "
                                                                                                                            + formatPosition(position)
                                                                                                                            + " is not a DMX fixture."
                                                                                                            )
                                                                                                    );

                                                                                                    return 0;
                                                                                                }

                                                                                                int red =
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "red"
                                                                                                        );

                                                                                                int green =
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "green"
                                                                                                        );

                                                                                                int blue =
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "blue"
                                                                                                        );

                                                                                                int dimmer =
                                                                                                        IntegerArgumentType.getInteger(
                                                                                                                context,
                                                                                                                "dimmer"
                                                                                                        );

                                                                                                fixture.setManualOutput(
                                                                                                        red,
                                                                                                        green,
                                                                                                        blue,
                                                                                                        dimmer
                                                                                                );

                                                                                                context.getSource().sendSuccess(
                                                                                                        () -> Component.literal(
                                                                                                                "Manual RGBD for fixture at "
                                                                                                                        + formatPosition(position)
                                                                                                                        + " set to "
                                                                                                                        + red
                                                                                                                        + ", "
                                                                                                                        + green
                                                                                                                        + ", "
                                                                                                                        + blue
                                                                                                                        + ", "
                                                                                                                        + dimmer
                                                                                                                        + "."
                                                                                                        ),
                                                                                                        false
                                                                                                );

                                                                                                return 1;
                                                                                            })
                                                                                    )
                                                                            )
                                                                    )
                                                            )
                                                    )
                                    )
                    );
                }
        );

        DmxLighting.LOGGER.info(
                "Registered DMX fixture control-mode commands."
        );
    }

    private static DmxFixtureBlockEntity getFixtureAt(
            BlockEntity blockEntity
    ) {
        if (blockEntity instanceof DmxFixtureBlockEntity fixture) {
            return fixture;
        }

        return null;
    }

    private static String formatPosition(BlockPos position) {
        return position.getX()
                + ", "
                + position.getY()
                + ", "
                + position.getZ();
    }
}