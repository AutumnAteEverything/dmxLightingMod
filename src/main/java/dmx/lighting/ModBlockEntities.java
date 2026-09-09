package dmx.lighting;

import net.fabricmc.fabric.api.object.builder.v1.block.entity
        .FabricBlockEntityTypeBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registers all block-entity types belonging to dmxLighting.
 */
public final class ModBlockEntities {

    public static final BlockEntityType<DmxFixtureBlockEntity>
            DMX_FIXTURE_BLOCK_ENTITY =
            register(
                    "dmx_fixture",
                    DmxFixtureBlockEntity::new,
                    ModBlocks.DMX_BLOCK
            );

    public static final BlockEntityType<DmxPixelBlockEntity>
            DMX_PIXEL_BLOCK_ENTITY =
            register(
                    "dmx_pixel_block",
                    DmxPixelBlockEntity::new,
                    ModBlocks.DMX_PIXEL_BLOCK
            );

    public static final BlockEntityType<DmxDiscoBallBlockEntity>
            DMX_DISCO_BALL_BLOCK_ENTITY =
            register(
                    "dmx_disco_ball",
                    DmxDiscoBallBlockEntity::new,
                    ModBlocks.DMX_DISCO_BALL
            );

    private ModBlockEntities() {
    }

    private static <T extends BlockEntity>
            BlockEntityType<T> register(
                    String name,
                    FabricBlockEntityTypeBuilder.Factory<? extends T>
                            factory,
                    Block... validBlocks
            ) {

        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                DmxLighting.id(name),
                FabricBlockEntityTypeBuilder
                        .<T>create(factory, validBlocks)
                        .build()
        );
    }

    public static void initialize() {
        DmxLighting.LOGGER.info(
                "Registering dmxLighting block entities."
        );
    }
}
