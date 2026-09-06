package dmx.lighting;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * Controls and inspects jukebox-driven automatic DMX shows.
 */
public final class AutomaticDmxCommand {

    private AutomaticDmxCommand() {
        // Utility class.
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                Commands.literal(
                                        "automaticdmx"
                                )
                                .executes(
                                        context -> showStatus(
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
                                                "on"
                                        )
                                        .executes(
                                                context -> setEnabled(
                                                        context.getSource(),
                                                        true
                                                )
                                        )
                                )
                                .then(
                                        Commands.literal(
                                                "off"
                                        )
                                        .executes(
                                                context -> setEnabled(
                                                        context.getSource(),
                                                        false
                                                )
                                        )
                                )
                        )
        );

        DmxLighting.LOGGER.info(
                "Registered the /automaticdmx command."
        );
    }

    private static int setEnabled(
            CommandSourceStack source,
            boolean enabled
    ) {
        AutomaticDmxShowManager.setEnabled(
                enabled
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Automatic DMX is now "
                                + (enabled ? "on" : "off")
                                + "."
                ),
                false
        );

        return 1;
    }

    private static int showStatus(
            CommandSourceStack source
    ) {
        if (!AutomaticDmxShowManager.isEnabled()) {
            source.sendSuccess(
                    () -> Component.literal(
                            "Automatic DMX is off."
                    ),
                    false
            );

            return 1;
        }

        List<AutomaticDmxShowManager.ActiveShowInfo> shows =
                AutomaticDmxShowManager.getActiveShows(
                        source.getLevel()
                );

        if (shows.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal(
                            "Automatic DMX is on; no jukebox is currently playing in this dimension."
                    ),
                    false
            );

            return 1;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Automatic DMX is following "
                                + shows.size()
                                + " jukebox"
                                + (shows.size() == 1 ? "" : "es")
                                + " in this dimension."
                ),
                false
        );

        for (AutomaticDmxShowManager.ActiveShowInfo show : shows) {
            source.sendSuccess(
                    () -> Component.literal(
                            show.discItemId()
                                    + " at "
                                    + formatPosition(
                                            show.position()
                                    )
                                    + ": "
                                    + String.format(
                                            Locale.ROOT,
                                            show.steadyBeat()
                                                    ? "%.1f BPM"
                                                    : "%.1f BPM show pulse",
                                            show.bpm()
                                    )
                                    + profileNote(
                                            show
                                    )
                    ),
                    false
            );
        }

        return shows.size();
    }

    private static String profileNote(
            AutomaticDmxShowManager.ActiveShowInfo show
    ) {
        if (!show.builtInProfile()) {
            return " (custom-disc fallback)";
        }

        if (!show.steadyBeat()) {
            return " (cinematic track; no continuous beat)";
        }

        return "";
    }

    private static String formatPosition(
            net.minecraft.core.BlockPos position
    ) {
        return position.getX()
                + ", "
                + position.getY()
                + ", "
                + position.getZ();
    }
}
