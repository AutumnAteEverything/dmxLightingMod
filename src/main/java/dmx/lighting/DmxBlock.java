package dmx.lighting;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import org.jetbrains.annotations.Nullable;

/**
 * A configurable DMX fixture block.
 *
 * The block entity stores its fixture configuration, output values,
 * installation orientation, and visual beam settings.
 *
 * LIGHT_LEVEL controls the amount of vanilla Minecraft block light
 * emitted by the fixture.
 *
 * This block is non-occluding because its visible fixture model does
 * not fill the entire Minecraft block. Without that setting, Minecraft
 * may cull neighboring block faces and make nearby blocks appear hollow
 * when viewed through the fixture.
 */
public class DmxBlock
        extends BaseEntityBlock {

    public static final MapCodec<DmxBlock> CODEC =
            simpleCodec(
                    DmxBlock::new
            );

    /**
     * Minecraft block light ranges from 0 through 15.
     */
    public static final IntegerProperty LIGHT_LEVEL =
            IntegerProperty.create(
                    "light_level",
                    0,
                    15
            );

    /**
     * Creates the fixture block.
     *
     * noOcclusion() prevents neighboring block faces from being removed
     * merely because the invisible logical fixture block occupies that
     * block position.
     */
    public DmxBlock(
            BlockBehaviour.Properties properties
    ) {
        super(
                configureProperties(
                        properties
                )
        );

        registerDefaultState(
                defaultBlockState()
                        .setValue(
                                LIGHT_LEVEL,
                                0
                        )
        );
    }

    /**
     * Applies fixture-specific block behavior before BaseEntityBlock is
     * constructed.
     */
    private static BlockBehaviour.Properties configureProperties(
            BlockBehaviour.Properties properties
    ) {
        return properties.noOcclusion();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * Adds light_level to the block's stored state.
     */
    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    net.minecraft.world.level.block.Block,
                    BlockState
            > builder
    ) {
        builder.add(
                LIGHT_LEVEL
        );
    }

    /**
     * Creates the fixture's block entity.
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        return new DmxFixtureBlockEntity(
                blockPos,
                blockState
        );
    }

    /**
     * Provides the server-side DMX polling ticker.
     */
    @Nullable
    @Override
    public <T extends BlockEntity>
            BlockEntityTicker<T> getTicker(
                    Level level,
                    BlockState blockState,
                    BlockEntityType<T> blockEntityType
            ) {

        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.DMX_FIXTURE_BLOCK_ENTITY,
                DmxFixtureBlockEntity::tick
        );
    }
}