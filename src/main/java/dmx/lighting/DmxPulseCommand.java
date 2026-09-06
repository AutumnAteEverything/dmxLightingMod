package dmx.lighting;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Accepts externally timed beat pulses for a temporary lighting show.
 */
public final class DmxPulseCommand {

    private DmxPulseCommand() {
        // Utility class.
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    registerAlias(
                            dispatcher,
                            "dmxpulse"
                    );

                    registerAlias(
                            dispatcher,
                            "dmxPulse"
                    );
                }
        );

        DmxLighting.LOGGER.info(
                "Registered the /dmxpulse command."
        );
    }

    private static void registerAlias(
            CommandDispatcher<CommandSourceStack> dispatcher,
            String name
    ) {
        dispatcher.register(
                Commands.literal(
                        name
                )
                .executes(
                        context -> triggerPulse(
                                context.getSource()
                        )
                )
                .then(
                        Commands.literal(
                                "status"
                        )
                        .executes(
                                context -> showStatus(
                                        context.getSource()
                                )
                        )
                )
                .then(
                        Commands.literal(
                                "stop"
                        )
                        .executes(
                                context -> stopPulse(
                                        context.getSource()
                                )
                        )
                )
        );
    }

    private static int triggerPulse(
            CommandSourceStack source
    ) {
        AutomaticDmxShowManager.triggerCommandPulse(
                source.getLevel()
        );

        return 1;
    }

    private static int showStatus(
            CommandSourceStack source
    ) {
        AutomaticDmxShowManager.PulseShowInfo info =
                AutomaticDmxShowManager.getPulseShowInfo(
                        source.getLevel()
                );

        if (info == null) {
            source.sendSuccess(
                    () -> Component.literal(
                            "No DMX pulse show is active in this dimension."
                    ),
                    false
            );

            return 1;
        }

        String tempo =
                info.tempoMeasured()
                        ? String.format(
                                Locale.ROOT,
                                "%.1f BPM",
                                info.estimatedBpm()
                        )
                        : "tempo pending a second pulse";

        source.sendSuccess(
                () -> Component.literal(
                        "DMX pulse show: "
                                + info.pulsesReceived()
                                + " pulse"
                                + (info.pulsesReceived() == 1L ? "" : "s")
                                + ", "
                                + tempo
                                + ", timeout in "
                                + String.format(
                                        Locale.ROOT,
                                        "%.1f seconds.",
                                        info.secondsUntilTimeout()
                                )
                ),
                false
        );

        return 1;
    }

    private static int stopPulse(
            CommandSourceStack source
    ) {
        boolean wasActive =
                AutomaticDmxShowManager.stopCommandPulse(
                        source.getLevel()
                );

        source.sendSuccess(
                () -> Component.literal(
                        wasActive
                                ? "DMX pulse show stopped."
                                : "No DMX pulse show was active."
                ),
                false
        );

        return 1;
    }
}
