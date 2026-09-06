package dmx.lighting;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portable handheld lighting console.
 *
 * Right-clicking this item requests the current fixture-browser data
 * for the player's dimension.
 *
 * The server builds the fixture list through LightingConsole and sends
 * the resulting snapshot to the client. The client-side receiver will
 * later open the Fixture Browser screen when the payload arrives.
 */
public class LightingConsoleItem extends Item {

    public LightingConsoleItem(
            Properties properties
    ) {
        super(properties);
    }

    /**
     * Called when the player right-clicks while holding this item.
     *
     * The use method runs on both the logical client and logical
     * server. Browser data is created only on the server because the
     * server owns the authoritative fixture registry.
     */
    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        /*
         * The client waits for the server response.
         */
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (!ServerPlayNetworking.canSend(
                serverPlayer,
                FixtureBrowserDataPayload.TYPE
        )) {
            DmxLighting.LOGGER.debug(
                    "Could not open the Lighting Console for {} because "
                            + "the client cannot receive fixture browser data.",
                    serverPlayer.getName().getString()
            );

            return InteractionResult.PASS;
        }

        List<FixtureBrowserEntry> entries =
                LightingConsole.getAllFixtures(
                        level.dimension(),
                        LightingConsole.FixtureSortMode.PATCH
                );

        ServerPlayNetworking.send(
                serverPlayer,
                new FixtureBrowserDataPayload(
                        entries
                )
        );

        return InteractionResult.SUCCESS;
    }
}
