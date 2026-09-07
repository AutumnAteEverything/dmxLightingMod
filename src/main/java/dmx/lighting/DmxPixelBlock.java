package dmx.lighting;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Full-cube DMX color surface. It is rendered emissively but always
 * reports zero Minecraft light emission.
 */
public final class DmxPixelBlock
        extends BaseEntityBlock {

    public static final MapCodec<DmxPixelBlock> CODEC =
            simpleCodec(
                    DmxPixelBlock::new
            );

    public DmxPixelBlock(
            BlockBehaviour.Properties properties
    ) {
        super(
                properties
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(
            BlockState blockState
    ) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        return new DmxPixelBlockEntity(
                blockPos,
                blockState
        );
    }

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
                ModBlockEntities.DMX_PIXEL_BLOCK_ENTITY,
                DmxPixelBlockEntity::tick
        );
    }
}
