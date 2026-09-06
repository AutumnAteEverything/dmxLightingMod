package dmx.lighting;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Sends any number of consecutive values directly into a DMX universe.
 *
 * Syntax:
 *
 * /dmxsend <universe> <startChannel> <value1> [value2 ...]
 *
 * Example:
 *
 * /dmxsend 1 15 255 0 0 255 128
 *
 * This writes:
 *
 * channel 15 = 255
 * channel 16 = 0
 * channel 17 = 0
 * channel 18 = 255
 * channel 19 = 128
 *
 * This command operates directly on raw DMX data.
 *
 * It does not configure a fixture and does not depend on:
 *
 * - Base Channel
 * - FixtureParameterMap
 * - Fixture profile
 *
 * A fixture will respond only if its parameter map points at the channels
 * written by this command. Existing four-value RGBD commands remain valid.
 */
public final class DmxSendCommand {

    private DmxSendCommand() {
        // Utility class: do not instantiate.
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            Commands.literal(
                                    "dmxsend"
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
                                                    "startChannel",
                                                    IntegerArgumentType.integer(
                                                            DmxUniverse.MIN_CHANNEL,
                                                            DmxUniverse.MAX_CHANNEL
                                                    )
                                            )
                                            .then(
                                                    Commands.argument(
                                                            "values",
                                                            StringArgumentType.greedyString()
                                                    )
                                                    .executes(
                                                            context -> sendValues(
                                                                    context.getSource(),
                                                                    IntegerArgumentType.getInteger(
                                                                            context,
                                                                            "universe"
                                                                    ),
                                                                    IntegerArgumentType.getInteger(
                                                                            context,
                                                                            "startChannel"
                                                                    ),
                                                                    StringArgumentType.getString(
                                                                            context,
                                                                            "values"
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
                "Registered the /dmxsend command."
        );
    }

    private static int sendValues(
            CommandSourceStack source,
            int universe,
            int startChannel,
            String valuesText
    ) {
        String trimmedValues =
                valuesText == null
                        ? ""
                        : valuesText.trim();

        if (trimmedValues.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "Supply at least one DMX value."
                    )
            );

            return 0;
        }

        String[] tokens =
                trimmedValues.split(
                        "\\s+"
                );

        int availableChannels =
                DmxUniverse.MAX_CHANNEL
                        - startChannel
                        + 1;

        if (tokens.length > availableChannels) {
            source.sendFailure(
                    Component.literal(
                            "That value list would extend past DMX channel 512."
                    )
            );

            return 0;
        }

        int[] values =
                new int[tokens.length];

        for (int index = 0; index < tokens.length; index++) {
            try {
                values[index] =
                        Integer.parseInt(
                                tokens[index]
                        );
            } catch (NumberFormatException exception) {
                source.sendFailure(
                        Component.literal(
                                "DMX value "
                                        + (index + 1)
                                        + " is not a whole number."
                        )
                );

                return 0;
            }

            if (values[index] < DmxUniverse.MIN_VALUE
                    || values[index] > DmxUniverse.MAX_VALUE) {

                source.sendFailure(
                        Component.literal(
                                "DMX value "
                                        + (index + 1)
                                        + " must be between 0 and 255."
                        )
                );

                return 0;
            }
        }

        for (int index = 0; index < values.length; index++) {
            DmxUniverseManager.setChannel(
                    universe,
                    startChannel + index,
                    values[index]
            );
        }

        DmxMobFixtureRegistry.refreshMobFixturesInUniverse(
                universe
        );

        int endChannel =
                startChannel
                        + values.length
                        - 1;

        source.sendSuccess(
                () -> Component.literal(
                        "Sent "
                                + values.length
                                + " DMX value"
                                + (values.length == 1 ? "" : "s")
                                + " to universe "
                                + universe
                                + ", channels "
                                + startChannel
                                + (endChannel == startChannel
                                        ? ""
                                        : "-" + endChannel)
                                + "."
                ),
                false
        );

        return values.length;
    }
}
