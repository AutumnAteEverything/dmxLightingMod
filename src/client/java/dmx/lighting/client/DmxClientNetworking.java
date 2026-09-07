package dmx.lighting.client;

import dmx.lighting.DmxLighting;
import dmx.lighting.FixtureBrowserDataPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;

/**
 * Registers client-side networking for dmxLighting.
 *
 * Fixture-browser data is stored and then displayed in the handheld
 * Lighting Console screen.
 */
public final class DmxClientNetworking {

    private DmxClientNetworking() {
        // Utility class: do not instantiate.
    }

    /**
     * Registers every client-side packet receiver.
     */
    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(
                FixtureBrowserDataPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () -> handleFixtureBrowserData(
                                        payload
                                )
                        )
        );

        DmxLighting.LOGGER.info(
                "Registered dmxLighting client networking."
        );
    }

    /**
     * Stores the new fixture snapshot and opens the Lighting Console.
     *
     * The 26.1 client still opens screens directly through Minecraft.
     */
    private static void handleFixtureBrowserData(
            FixtureBrowserDataPayload payload
    ) {
        if (payload == null) {
            FixtureBrowserClientStore.setEntries(
                    null
            );

            return;
        }

        FixtureBrowserClientStore.setEntries(
                payload.entries()
        );

        Minecraft minecraft =
                Minecraft.getInstance();

        /*
         * Minecraft 26.1 screen API.
         *
         * Reopening the screen after a refresh is acceptable in this
         * first browser version. LightingConsoleState can later restore
         * search, sorting, selection, and scrolling automatically.
         */
        minecraft.setScreen(
                new LightingConsoleScreen()
        );

        DmxLighting.LOGGER.debug(
                "Received {} fixture browser entries.",
                payload.entries().size()
        );
    }
}
