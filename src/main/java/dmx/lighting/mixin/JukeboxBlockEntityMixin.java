package dmx.lighting.mixin;

import dmx.lighting.AutomaticDmxShowManager;
import dmx.lighting.DmxBlockDisplayRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private static void dmxlighting$trackJukebox(
            Level level,
            BlockPos position,
            BlockState state,
            JukeboxBlockEntity jukebox,
            CallbackInfo info
    ) {
        AutomaticDmxShowManager.tickJukebox(
                level,
                position,
                jukebox
        );

        if (!level.isClientSide()) {
            DmxBlockDisplayRegistry.refreshInDimension(
                    level.dimension()
            );
        }
    }

    @Inject(
            method = "setRemoved",
            at = @At("HEAD")
    )
    private void dmxlighting$forgetJukebox(
            CallbackInfo info
    ) {
        JukeboxBlockEntity jukebox =
                (JukeboxBlockEntity) (Object) this;

        AutomaticDmxShowManager.removeJukebox(
                jukebox.getLevel(),
                jukebox.getBlockPos()
        );

        Level level = jukebox.getLevel();

        if (level != null && !level.isClientSide()) {
            DmxBlockDisplayRegistry.refreshInDimension(
                    level.dimension()
            );
        }
    }
}
