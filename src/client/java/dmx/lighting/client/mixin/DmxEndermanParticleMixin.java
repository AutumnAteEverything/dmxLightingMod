package dmx.lighting.client.mixin;

import dmx.lighting.DmxEndermanEntity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Recolors vanilla Enderman particles for DMX Endermen only.
 */
@Mixin(EnderMan.class)
public abstract class DmxEndermanParticleMixin {

    private static final float DMX_PARTICLE_SCALE =
            1.35F;

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle"
                            + "(Lnet/minecraft/core/particles/ParticleOptions;"
                            + "DDDDDD)V"
            )
    )
    private void dmxlighting$replacePortalParticle(
            Level level,
            ParticleOptions originalParticle,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
    ) {
        EnderMan enderman =
                (EnderMan) (Object) this;

        if (!(enderman instanceof DmxEndermanEntity dmxEnderman)) {
            level.addParticle(
                    originalParticle,
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed
            );

            return;
        }

        dmxEnderman.updateColorInterpolation(
                0.0F
        );

        int color =
                dmxEnderman.getSmoothedSyncedOutputRgb()
                        & 0x00FFFFFF;

        if (dmxEnderman.getSyncedDimmer() <= 0
                || color == 0) {

            return;
        }

        level.addParticle(
                new DustParticleOptions(
                        color,
                        DMX_PARTICLE_SCALE
                ),
                x,
                y,
                z,
                xSpeed,
                ySpeed,
                zSpeed
        );
    }
}
