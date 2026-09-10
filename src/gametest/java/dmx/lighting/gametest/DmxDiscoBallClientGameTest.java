package dmx.lighting.gametest;

import dmx.lighting.DmxDiscoBallBlockEntity;
import dmx.lighting.DmxDiscoBallEffectMode;
import dmx.lighting.DmxPixelBlockEntity;
import dmx.lighting.AutomaticDmxShowManager;
import dmx.lighting.FixtureParameterMap;
import dmx.lighting.FixtureOutput;
import dmx.lighting.ModBlocks;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

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
                assertAutomaticFixtureMovementStartsAtBase(level);
                assertAutomaticSpinTiming(level);
                buildProjectionWall(level);
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
                map.setStrobeChannel(13);
                disco.setInstallation(45.0F, false);

                DmxPixelBlockEntity pixel = findPixel(
                        level,
                        pixelPosition
                );
                pixel.setSkinConfiguration(1, 20);
            });

            singleplayer.getServer().runCommand(
                    "dmxsend 1 11 255 128 0"
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
                    "dmxsend 1 13 255"
            );
            context.waitTicks(5);

            int[] dotsState = context.computeOnClient(client -> {
                DmxDiscoBallBlockEntity disco = findDisco(
                        client.level,
                        discoPosition
                );
                return new int[] {
                        disco.getResolvedEffectMode()
                                == DmxDiscoBallEffectMode.DOTS ? 1 : 0,
                        disco.getActiveDimmer(),
                        disco.getDmxStrobe()
                };
            });

            if (dotsState[0] != 1
                    || dotsState[1] != 255
                    || dotsState[2] != 255) {
                throw new AssertionError(
                        "DMX effect channel did not select projected dots."
                );
            }

            Path dotsScreenshot = context.takeScreenshot(
                    "dmx-disco-ball-dots"
            );
            assertScatteredDots(dotsScreenshot);

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

    private static void assertAutomaticFixtureMovementStartsAtBase(
            ServerLevel level
    ) {
        FixtureOutput base = new FixtureOutput(
                0,
                0,
                0,
                0,
                0,
                255,
                64,
                80,
                0,
                0,
                0
        );

        AutomaticDmxShowManager.stopCommandPulse(level);
        AutomaticDmxShowManager.triggerCommandPulse(level);

        FixtureOutput initial = AutomaticDmxShowManager.createOutput(
                level,
                BlockPos.ZERO,
                base,
                true
        );

        if (initial == null
                || initial.getPan() != base.getPan()
                || initial.getTilt() != base.getTilt()) {
            throw new AssertionError(
                    "Automatic fixture movement did not start at its base position."
            );
        }

        AutomaticDmxShowManager.triggerCommandPulse(level);
        FixtureOutput moving = AutomaticDmxShowManager.createOutput(
                level,
                BlockPos.ZERO,
                base,
                true
        );
        AutomaticDmxShowManager.stopCommandPulse(level);

        if (moving == null
                || moving.getPan() <= base.getPan()
                || moving.getTilt() <= base.getTilt()) {
            throw new AssertionError(
                    "Automatic fixture movement did not sweep from its base position."
            );
        }
    }

    private static void assertAutomaticSpinTiming(ServerLevel level) {
        AutomaticDmxShowManager.stopCommandPulse(level);
        AutomaticDmxShowManager.triggerCommandPulse(level);
        AutomaticDmxShowManager.triggerCommandPulse(level);

        int firstDirection = AutomaticDmxShowManager
                .createDiscoBallOutput(
                        level,
                        BlockPos.ZERO,
                        FixtureOutput.BLACKOUT
                )
                .getPan();

        for (int pulse = 2; pulse < 24; pulse++) {
            AutomaticDmxShowManager.triggerCommandPulse(level);
        }

        int sameDirection = AutomaticDmxShowManager
                .createDiscoBallOutput(
                        level,
                        BlockPos.ZERO,
                        FixtureOutput.BLACKOUT
                )
                .getPan();

        AutomaticDmxShowManager.triggerCommandPulse(level);
        AutomaticDmxShowManager.triggerCommandPulse(level);

        int reversedDirection = AutomaticDmxShowManager
                .createDiscoBallOutput(
                        level,
                        BlockPos.ZERO,
                        FixtureOutput.BLACKOUT
                )
                .getPan();

        AutomaticDmxShowManager.stopCommandPulse(level);

        if (firstDirection <= 128
                || sameDirection <= 128
                || reversedDirection >= 128) {
            throw new AssertionError(
                    "Automatic disco spin did not keep its direction for 24 beats."
            );
        }
    }

    private static void buildProjectionWall(ServerLevel level) {
        for (int x = -6; x <= 6; x++) {
            for (int y = -60; y <= -50; y++) {
                level.setBlockAndUpdate(
                        new BlockPos(x, y, 5),
                        Blocks.WHITE_CONCRETE.defaultBlockState()
                );
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

    private static void assertScatteredDots(Path screenshot) {
        BufferedImage image;
        try {
            image = ImageIO.read(screenshot.toFile());
        } catch (IOException exception) {
            throw new AssertionError(
                    "Could not read disco-dot screenshot.",
                    exception
            );
        }

        if (image == null) {
            throw new AssertionError("Disco-dot screenshot was unreadable.");
        }

        int saturatedPixels = 0;
        int cellSize = 40;
        boolean[][] occupied = new boolean[
                (image.getHeight() + cellSize - 1) / cellSize
        ][
                (image.getWidth() + cellSize - 1) / cellSize
        ];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y);
                int red = color >> 16 & 0xFF;
                int green = color >> 8 & 0xFF;
                int blue = color & 0xFF;
                int maximum = Math.max(red, Math.max(green, blue));
                int minimum = Math.min(red, Math.min(green, blue));

                if (maximum >= 120 && maximum - minimum >= 65) {
                    saturatedPixels++;
                    occupied[y / cellSize][x / cellSize] = true;
                }
            }
        }

        int occupiedCells = 0;
        for (boolean[] row : occupied) {
            for (boolean cell : row) {
                if (cell) {
                    occupiedCells++;
                }
            }
        }

        if (saturatedPixels < 30 || occupiedCells < 6) {
            throw new AssertionError(
                    "Projected disco dots were not visible and scattered."
            );
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
