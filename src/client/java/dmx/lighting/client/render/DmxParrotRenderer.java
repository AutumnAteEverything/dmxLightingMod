package dmx.lighting.client.render;

import dmx.lighting.DmxParrotEntity;

import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * DMX-aware parrot renderer.
 *
 * The normal parrot model and animations are reused, while the final
 * model tint comes from the parrot's synced DMX output.
 */
public final class DmxParrotRenderer
        extends MobRenderer<
                DmxParrotEntity,
                DmxParrotRenderState,
                ParrotModel
        > {

    public DmxParrotRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new ParrotModel(
                        context.bakeLayer(
                                ModelLayers.PARROT
                        )
                ),
                0.3F
        );
    }

    @Override
    public DmxParrotRenderState createRenderState() {
        return new DmxParrotRenderState();
    }

    @Override
    public void extractRenderState(
            DmxParrotEntity entity,
            DmxParrotRenderState state,
            float tickProgress
    ) {
        super.extractRenderState(
                entity,
                state,
                tickProgress
        );

        state.variant =
                entity.getVariant();

        float flap =
                Mth.lerp(
                        tickProgress,
                        entity.oFlap,
                        entity.flap
                );

        float flapSpeed =
                Mth.lerp(
                        tickProgress,
                        entity.oFlapSpeed,
                        entity.flapSpeed
                );

        state.flapAngle =
                (
                        Mth.sin(
                                flap
                        )
                                + 1.0F
                )
                        * flapSpeed;

        state.pose =
                ParrotModel.getPose(
                        entity
                );

        entity.updateColorInterpolation(
                tickProgress
        );

        state.outputRgb =
                entity.getSmoothedSyncedOutputRgb();

        state.dimmer =
                entity.getSyncedDimmer();
    }

    @Override
    public Identifier getTextureLocation(
            DmxParrotRenderState state
    ) {
        return ParrotRenderer.getVariantTexture(
                state.variant
        );
    }

    @Override
    protected int getModelTint(
            DmxParrotRenderState state
    ) {
        return ARGB.opaque(
                state.outputRgb
        );
    }

    @Override
    protected RenderType getRenderType(
            DmxParrotRenderState state,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing
    ) {
        if (bodyVisible) {
            return RenderTypes.entityTranslucentEmissive(
                    getTextureLocation(
                            state
                    )
            );
        }

        return super.getRenderType(
                state,
                bodyVisible,
                translucent,
                glowing
        );
    }
}
