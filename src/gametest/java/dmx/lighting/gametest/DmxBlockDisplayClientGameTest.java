package dmx.lighting.gametest;

import dmx.lighting.DmxBlockDisplayEntity;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

@SuppressWarnings("UnstableApiUsage")
public final class DmxBlockDisplayClientGameTest
        implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer =
                context.worldBuilder().create()) {

            singleplayer.getClientLevel().waitForChunksDownload();
            singleplayer.getServer().runCommand(
                    "gamemode spectator @p"
            );
            singleplayer.getServer().runCommand(
                    "tp @p 0.5 -59 -6.5 0 11"
            );
            singleplayer.getServer().runCommand(
                    "time set midnight"
            );
            singleplayer.getServer().runCommand(
                    "summon dmxlighting:dmx_block_display 0 -59 0 "
                            + "{dmx_fixture_name:\"Display Test\","
                            + "dmx_universe:1,dmx_parameter_red:11,"
                            + "dmx_parameter_green:12,"
                            + "dmx_parameter_blue:13,"
                            + "dmx_parameter_dimmer:14,"
                            + "dmx_parameter_strobe:15}"
            );
            context.waitTicks(5);

            singleplayer.getServer().runCommand(
                    "dmxsend 1 11 255 0 0 255 0"
            );
            context.waitTicks(5);

            int packedRgb = context.computeOnClient(
                    client -> findDisplay(client.level)
                            .getVisiblePackedRgb(0.0F)
            );

            if (packedRgb != 0xFF0000) {
                throw new AssertionError(
                        "Expected client display RGB FF0000 but received "
                                + String.format("%06X", packedRgb)
                );
            }

            singleplayer.getClientLevel().waitForChunksRender();
            Path redScreenshot =
                    context.takeScreenshot("dmx-block-display-red");
            assertVisibleColor(redScreenshot, 0);

            singleplayer.getServer().runCommand(
                    "dmxsend 1 11 0 0 255 255 0"
            );
            context.waitTicks(5);
            Path blueScreenshot =
                    context.takeScreenshot("dmx-block-display-blue");
            assertVisibleColor(blueScreenshot, 2);

            singleplayer.getServer().runCommand(
                    "dmxsend 1 11 0 0 0 0 0"
            );
            singleplayer.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                BlockPos jukeboxPosition = new BlockPos(2, -59, 0);
                level.setBlockAndUpdate(
                        jukeboxPosition,
                        Blocks.JUKEBOX.defaultBlockState()
                );

                if (!(level.getBlockEntity(jukeboxPosition)
                        instanceof JukeboxBlockEntity jukebox)) {

                    throw new AssertionError("Jukebox was not created.");
                }

                jukebox.setTheItem(
                        new ItemStack(Items.MUSIC_DISC_CAT)
                );
            });
            context.waitTicks(5);

            int jukeboxPackedRgb = context.computeOnClient(
                    client -> findDisplay(client.level)
                            .getVisiblePackedRgb(0.0F)
            );

            if (jukeboxPackedRgb == 0) {
                throw new AssertionError(
                        "Jukebox show did not light the DMX Block Display."
                );
            }
        }
    }

    private static void assertVisibleColor(
            Path screenshot,
            int dominantChannel
    ) {
        BufferedImage image;

        try {
            image = ImageIO.read(screenshot.toFile());
        } catch (IOException exception) {
            throw new AssertionError(
                    "Could not read rendered DMX screenshot.",
                    exception
            );
        }

        if (image == null) {
            throw new AssertionError(
                    "Rendered DMX screenshot was not a readable image."
            );
        }

        int matchingPixels = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y);
                int[] channels = {
                        color >> 16 & 0xFF,
                        color >> 8 & 0xFF,
                        color & 0xFF
                };
                int dominant = channels[dominantChannel];
                int otherA = channels[(dominantChannel + 1) % 3];
                int otherB = channels[(dominantChannel + 2) % 3];

                if (dominant >= 128
                        && dominant > otherA * 2
                        && dominant > otherB * 2) {

                    matchingPixels++;
                }
            }
        }

        if (matchingPixels < 100) {
            throw new AssertionError(
                    "DMX Block Display color was not visibly rendered."
            );
        }
    }

    private static DmxBlockDisplayEntity findDisplay(ClientLevel level) {
        if (level != null) {
            for (Entity entity : level.entitiesForRendering()) {
                if (entity instanceof DmxBlockDisplayEntity display) {
                    return display;
                }
            }
        }

        throw new AssertionError("DMX Block Display did not reach the client.");
    }
}
