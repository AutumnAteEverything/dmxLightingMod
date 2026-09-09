package dmx.lighting;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registers the blocks and block items supplied by dmxLighting.
 */
public final class ModBlocks {

    private static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS_TAB =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.withDefaultNamespace(
                            "functional_blocks"
                    )
            );

    public static final ResourceKey<Block> DMX_BLOCK_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    DmxLighting.id("dmx_block")
            );

    public static final ResourceKey<Item> DMX_BLOCK_ITEM_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    DmxLighting.id("dmx_block")
            );

    public static final ResourceKey<Block> DMX_PIXEL_BLOCK_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    DmxLighting.id("dmx_pixel_block")
            );

    public static final ResourceKey<Item> DMX_PIXEL_BLOCK_ITEM_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    DmxLighting.id("dmx_pixel_block")
            );

    public static final ResourceKey<Block> DMX_DISCO_BALL_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    DmxLighting.id("dmx_disco_ball")
            );

    public static final ResourceKey<Item> DMX_DISCO_BALL_ITEM_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    DmxLighting.id("dmx_disco_ball")
            );

    public static final Block DMX_BLOCK = registerBlock(
            DMX_BLOCK_KEY,
            DMX_BLOCK_ITEM_KEY,
            DmxBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.METAL)

                    /*
                     * Minecraft asks the block state how much light
                     * this fixture currently emits.
                     */
                    .lightLevel(
                            state -> state.getValue(
                                    DmxBlock.LIGHT_LEVEL
                            )
                    )
    );

    public static final Block DMX_PIXEL_BLOCK = registerBlock(
            DMX_PIXEL_BLOCK_KEY,
            DMX_PIXEL_BLOCK_ITEM_KEY,
            DmxPixelBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.GLASS)
                    .lightLevel(
                            state -> 0
                    )
                    .emissiveRendering(
                            (state, level, position) -> true
                    )
    );

    public static final Block DMX_DISCO_BALL = registerBlock(
            DMX_DISCO_BALL_KEY,
            DMX_DISCO_BALL_ITEM_KEY,
            DmxDiscoBallBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 0)
                    .emissiveRendering(
                            (state, level, position) -> true
                    )
    );

    private ModBlocks() {
    }

    private static Block registerBlock(
            ResourceKey<Block> blockKey,
            ResourceKey<Item> itemKey,
            Function<BlockBehaviour.Properties, Block> blockFactory,
            BlockBehaviour.Properties blockProperties
    ) {
        Block block = blockFactory.apply(
                blockProperties.setId(blockKey)
        );

        BlockItem blockItem = new BlockItem(
                block,
                new Item.Properties()
                        .setId(itemKey)
                        .useBlockDescriptionPrefix()
        );

        Registry.register(
                BuiltInRegistries.BLOCK,
                blockKey,
                block
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                blockItem
        );

        return block;
    }

    public static void initialize() {
        registerInteractionGuard();

        CreativeModeTabEvents.modifyOutputEvent(
                FUNCTIONAL_BLOCKS_TAB
        ).register(
                output -> {
                    output.accept(DMX_BLOCK);
                    output.accept(DMX_PIXEL_BLOCK);
                    output.accept(DMX_DISCO_BALL);
                }
        );

        DmxLighting.LOGGER.info(
                "Registering dmxLighting blocks."
        );
    }

    /**
     * Prevents the server from also using or placing the held item when a
     * dmxLighting-aware client opens one of the DMX block screens.
     */
    private static void registerInteractionGuard() {
        UseBlockCallback.EVENT.register(
                (player, level, hand, hitResult) -> {
                    Block stateBlock = level.getBlockState(
                            hitResult.getBlockPos()
                    ).getBlock();

                    if (stateBlock != DMX_BLOCK
                            && stateBlock != DMX_PIXEL_BLOCK
                            && stateBlock != DMX_DISCO_BALL) {

                        return InteractionResult.PASS;
                    }

                    /*
                     * Let the client-only handler open the screen. The
                     * server consumes the same interaction before vanilla
                     * can place or use the held item.
                     */
                    if (level.isClientSide()) {
                        return InteractionResult.PASS;
                    }

                    if (player instanceof ServerPlayer serverPlayer
                            && ServerPlayNetworking.canSend(
                                    serverPlayer,
                                    FixtureBrowserDataPayload.TYPE
                            )) {

                        return InteractionResult.SUCCESS_SERVER;
                    }

                    return InteractionResult.PASS;
                }
        );
    }
}
