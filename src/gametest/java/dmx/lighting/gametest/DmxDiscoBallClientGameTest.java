package dmx.lighting.gametest;

import dmx.lighting.DmxDiscoBallBlockEntity;
import dmx.lighting.DmxPixelBlockEntity;
import dmx.lighting.FixtureParameterMap;
import dmx.lighting.ModBlocks;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

@SuppressWarnings("UnstableApiUsage")
public final class DmxDiscoBallClientGameTest
        implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer =
                context.worldBuilder().create()) {

            BlockPos discoPosition = new BlockPos(0, -59, 0);
            BlockPos pixelPosition = new BlockPos(3, -59, 0);

            singleplayer.getClientLevel().waitForChunksDownload();
            singleplayer.getServer().runCommand("gamemode spectator @p");
            singleplayer.getServer().runCommand(
                    "tp @p 0.5 -56 -6.5 0 18"
            );
            singleplayer.getServer().runCommand("time set midnight");

            singleplayer.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                level.setBlockAndUpdate(
                        discoPosition,
                        ModBlocks.DMX_DISCO_BALL.defaultBlockState()
                );
                level.setBlockAndUpdate(
                        pixelPosition,
                        ModBlocks.DMX_PIXEL_BLOCK.defaultBlockState()
                );

                DmxDiscoBallBlockEntity disco = findDisco(
                        level,
                        discoPosition
                );
                FixtureParameterMap map = disco.getParameterMap();
                map.setDimmerChannel(11);
                map.setPanChannel(12);
                disco.setInstallation(45.0F, false);

                DmxPixelBlockEntity pixel = findPixel(
                        level,
                        pixelPosition
                );
                pixel.setSkinConfiguration(1, 20);
            });

            singleplayer.getServer().runCommand(
                    "dmxsend 1 11 255 128"
            );
            singleplayer.getServer().runCommand(
                    "dmxsend 1 20 255"
            );
            context.waitTicks(8);

            int[] stoppedState = context.computeOnClient(client -> {
                DmxDiscoBallBlockEntity disco = findDisco(
                        client.level,
                        discoPosition
                );
                DmxPixelBlockEntity pixel = findPixel(
                        client.level,
                        pixelPosition
                );
                return new int[] {
                        disco.getActiveDimmer(),
                        disco.getActivePan(),
                        pixel.getRenderedSkin(),
                        disco.isBaseOnTop() ? 1 : 0
                };
            });

            if (stoppedState[0] != 255
                    || stoppedState[1] != 128
                    || stoppedState[2] != 16
                    || stoppedState[3] != 0) {
                throw new AssertionError(
                        "DMX Disco Ball or 16-skin mapping did not sync."
                );
            }

            float stoppedAngle = context.computeOnClient(client ->
                    findDisco(client.level, discoPosition)
                            .getSpinRotationDegrees(0.0F)
            );
            context.waitTicks(8);
            float stoppedAngleLater = context.computeOnClient(client ->
                    findDisco(client.level, discoPosition)
                            .getSpinRotationDegrees(0.0F)
            );

            if (angularDifference(stoppedAngle, stoppedAngleLater) > 0.1F) {
                throw new AssertionError("Spin value 128 did not stop.");
            }

            singleplayer.getClientLevel().waitForChunksRender();
            Path screenshot = context.takeScreenshot("dmx-disco-ball");
            assertPrismaticBeams(screenshot);

            singleplayer.getServer().runCommand(
                    "dmxsend 1 12 255"
            );
            context.waitTicks(8);

            float forwardSpeed = context.computeOnClient(client ->
                    findDisco(client.level, discoPosition)
                            .getSpinDegreesPerSecond()
            );
            if (forwardSpeed <= 0.0F) {
                throw new AssertionError("Spin value 255 was not forward.");
            }

            singleplayer.getServer().runCommand(
                    "dmxsend 1 12 0"
            );
            context.waitTicks(4);

            float reverseSpeed = context.computeOnClient(client ->
                    findDisco(client.level, discoPosition)
                            .getSpinDegreesPerSecond()
            );
            if (reverseSpeed >= 0.0F) {
                throw new AssertionError("Spin value 0 was not reverse.");
            }

            singleplayer.getServer().runOnServer(server ->
                    findDisco(server.overworld(), discoPosition)
                            .setInstallation(90.0F, true)
            );
            context.waitTicks(5);

            boolean baseOnTop = context.computeOnClient(client ->
                    findDisco(client.level, discoPosition).isBaseOnTop()
            );
            if (!baseOnTop) {
                throw new AssertionError("Top mount did not sync.");
            }
        }
    }

    private static float angularDifference(float first, float second) {
        float difference = Math.abs(first - second) % 360.0F;
        return Math.min(difference, 360.0F - difference);
    }

    private static void assertPrismaticBeams(Path screenshot) {
        BufferedImage image;
        try {
            image = ImageIO.read(screenshot.toFile());
        } catch (IOException exception) {
            throw new AssertionError("Could not read disco ball screenshot.", exception);
        }

        if (image == null) {
            throw new AssertionError("Disco ball screenshot was unreadable.");
        }

        int saturatedPixels = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y);
                int red = color >> 16 & 0xFF;
                int green = color >> 8 & 0xFF;
                int blue = color & 0xFF;
                int maximum = Math.max(red, Math.max(green, blue));
                int minimum = Math.min(red, Math.min(green, blue));

                if (maximum >= 100 && maximum - minimum >= 55) {
                    saturatedPixels++;
                }
            }
        }

        if (saturatedPixels < 30) {
            throw new AssertionError("Disco ball beams were not visible.");
        }
    }

    private static DmxDiscoBallBlockEntity findDisco(
            ServerLevel level,
            BlockPos position
    ) {
        if (level.getBlockEntity(position)
                instanceof DmxDiscoBallBlockEntity disco) {
            return disco;
        }
        throw new AssertionError("DMX Disco Ball was not created.");
    }

    private static DmxDiscoBallBlockEntity findDisco(
            ClientLevel level,
            BlockPos position
    ) {
        if (level != null
                && level.getBlockEntity(position)
                instanceof DmxDiscoBallBlockEntity disco) {
            return disco;
        }
        throw new AssertionError("DMX Disco Ball did not reach the client.");
    }

    private static DmxPixelBlockEntity findPixel(
            ServerLevel level,
            BlockPos position
    ) {
        if (level.getBlockEntity(position)
                instanceof DmxPixelBlockEntity pixel) {
            return pixel;
        }
        throw new AssertionError("DMX Block was not created.");
    }

    private static DmxPixelBlockEntity findPixel(
            ClientLevel level,
            BlockPos position
    ) {
        if (level != null
                && level.getBlockEntity(position)
                instanceof DmxPixelBlockEntity pixel) {
            return pixel;
        }
        throw new AssertionError("DMX Block did not reach the client.");
    }
}
