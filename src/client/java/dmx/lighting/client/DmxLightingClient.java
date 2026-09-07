package dmx.lighting.client;

import dmx.lighting.DmxFixtureBlockEntity;
import dmx.lighting.DmxLighting;
import dmx.lighting.DmxPixelBlockEntity;
import dmx.lighting.ModBlockEntities;
import dmx.lighting.ModBlocks;
import dmx.lighting.ModEntities;
import dmx.lighting.client.render.DmxFixtureBlockEntityRenderer;
import dmx.lighting.client.render.DmxBlockDisplayRenderer;
import dmx.lighting.client.render.DmxAxolotlRenderer;
import dmx.lighting.client.render.DmxCreakingRenderer;
import dmx.lighting.client.render.DmxEndermanRenderer;
import dmx.lighting.client.render.DmxNautilusRenderer;
import dmx.lighting.client.render.DmxParrotRenderer;
import dmx.lighting.client.render.DmxPixelBlockEntityRenderer;
import dmx.lighting.client.render.DmxWardenRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Client-only initialization for dmxLighting.
 *
 * Client features:
 *
 * - Fixture editor interaction
 * - Live fixture block tint
 * - Profile-aware block-entity rendering
 */
public class DmxLightingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DmxClientNetworking.initialize();

        registerFixtureTint();
        registerFixtureInteraction();
        registerFixtureRenderer();
        registerEntityRenderers();

        DmxLighting.LOGGER.info(
                "Registered DMX fixture client features."
        );
    }

    /**
     * Registers the custom DMX fixture block-entity renderer.
     */
    private static void registerFixtureRenderer() {
        BlockEntityRenderers.register(
                ModBlockEntities.DMX_FIXTURE_BLOCK_ENTITY,
                DmxFixtureBlockEntityRenderer::new
        );

        BlockEntityRenderers.register(
                ModBlockEntities.DMX_PIXEL_BLOCK_ENTITY,
                DmxPixelBlockEntityRenderer::new
        );
    }

    private static void registerEntityRenderers() {
        EntityRendererRegistry.register(
                ModEntities.DMX_PARROT,
                DmxParrotRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.DMX_ENDERMAN,
                DmxEndermanRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.DMX_WARDEN,
                DmxWardenRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.DMX_NAUTILUS,
                DmxNautilusRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.DMX_CREAKING,
                DmxCreakingRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.DMX_AXOLOTL,
                DmxAxolotlRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.DMX_BLOCK_DISPLAY,
                DmxBlockDisplayRenderer::new
        );
    }

    /**
     * Opens the fixture screen when a player right-clicks a DMX block.
     */
    private static void registerFixtureInteraction() {
        UseBlockCallback.EVENT.register(
                (player, level, hand, hitResult) -> {
                    BlockPos position =
                            hitResult.getBlockPos();

                    BlockState state =
                            level.getBlockState(
                                    position
                            );

                    boolean isPixelBlock = state.is(
                            ModBlocks.DMX_PIXEL_BLOCK
                    );

                    boolean isFixtureBlock = state.is(
                            ModBlocks.DMX_BLOCK
                    );

                    if (!isPixelBlock && !isFixtureBlock) {
                        return InteractionResult.PASS;
                    }

                    /*
                     * Consume the interaction on both sides so the held
                     * block is not also placed. Screens remain client-only.
                     */
                    if (level.isClientSide()) {
                        if (isPixelBlock) {
                            Minecraft.getInstance()
                                    .setScreenAndShow(
                                            new DmxPixelBlockScreen(
                                                    position
                                            )
                                    );
                        } else {
                            Minecraft.getInstance()
                                    .setScreenAndShow(
                                            new DmxFixtureScreen(
                                                    position
                                            )
                                    );
                        }

                        return InteractionResult.SUCCESS;
                    }

                    return InteractionResult.SUCCESS_SERVER;
                }
        );
    }

    /**
     * Registers the live RGB tint for placed fixture blocks.
     */
    private static void registerFixtureTint() {
        BlockTintSource fixtureTint =
                new BlockTintSource() {

                    @Override
                    public int colorInWorld(
                            BlockState state,
                            BlockAndTintGetter level,
                            BlockPos position
                    ) {
                        if (level == null
                                || position == null) {

                            return ARGB.opaque(
                                    0xFFFFFF
                            );
                        }

                        BlockEntity blockEntity =
                                level.getBlockEntity(
                                        position
                                );

                        if (!(blockEntity
                                instanceof DmxFixtureBlockEntity fixture)) {

                            return ARGB.opaque(
                                    0xFFFFFF
                            );
                        }

                        if (fixture
                                instanceof DmxPixelBlockEntity pixelBlock) {

                            return ARGB.opaque(
                                    pixelBlock.getVisiblePackedRgb()
                            );
                        }

                        return ARGB.opaque(
                                fixture.getSmoothedOutputPackedRgb()
                        );
                    }

                    @Override
                    public int color(
                            BlockState state
                    ) {
                        return ARGB.opaque(
                                0xFFFFFF
                        );
                    }
                };

        BlockColorRegistry.register(
                List.of(
                        fixtureTint
                ),
                ModBlocks.DMX_BLOCK,
                ModBlocks.DMX_PIXEL_BLOCK
        );
    }
}
