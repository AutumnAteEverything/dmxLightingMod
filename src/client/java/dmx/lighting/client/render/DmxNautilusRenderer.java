package dmx.lighting.client.render;

import dmx.lighting.DmxNautilusEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NautilusRenderer;
import net.minecraft.client.renderer.entity.state.NautilusRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * DMX-aware Nautilus renderer.
 *
 * The vanilla adult/baby models, animations, textures, saddle, and
 * armor layers are retained. The body receives an emissive DMX tint.
 */
public final class DmxNautilusRenderer
        extends NautilusRenderer<DmxNautilusEntity> {

    public DmxNautilusRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context
        );
    }

    @Override
    public DmxNautilusRenderState createRenderState() {
        return new DmxNautilusRenderState();
    }

    @Override
    public void extractRenderState(
            DmxNautilusEntity entity,
            NautilusRenderState state,
            float tickProgress
    ) {
        super.extractRenderState(
                entity,
                state,
                tickProgress
        );

        if (!(state instanceof DmxNautilusRenderState dmxState)) {
            return;
        }

        entity.updateColorInterpolation(
                tickProgress
        );

        dmxState.outputRgb =
                entity.getSmoothedSyncedOutputRgb();

        dmxState.dimmer =
                entity.getSyncedDimmer();
    }

    @Override
    protected int getModelTint(
            NautilusRenderState state
    ) {
        if (state instanceof DmxNautilusRenderState dmxState) {
            return ARGB.opaque(
                    dmxState.outputRgb
            );
        }

        return super.getModelTint(
                state
        );
    }

    @Override
    protected RenderType getRenderType(
            NautilusRenderState state,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing
    ) {
        if (bodyVisible) {
            Identifier texture =
                    getTextureLocation(
                            state
                    );

            return RenderTypes.entityTranslucentEmissive(
                    texture
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
