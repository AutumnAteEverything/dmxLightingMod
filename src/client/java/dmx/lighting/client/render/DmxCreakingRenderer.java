package dmx.lighting.client.render;

import dmx.lighting.DmxCreakingEntity;

import net.minecraft.client.renderer.entity.CreakingRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Reuses the vanilla Creaking model and animations with an emissive DMX tint. */
public final class DmxCreakingRenderer
        extends CreakingRenderer<DmxCreakingEntity> {

    public DmxCreakingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DmxCreakingRenderState createRenderState() {
        return new DmxCreakingRenderState();
    }

    @Override
    public void extractRenderState(
            DmxCreakingEntity entity,
            CreakingRenderState state,
            float tickProgress
    ) {
        super.extractRenderState(entity, state, tickProgress);

        if (!(state instanceof DmxCreakingRenderState dmxState)) {
            return;
        }

        entity.updateColorInterpolation(tickProgress);
        dmxState.outputRgb = entity.getSmoothedSyncedOutputRgb();
        dmxState.dimmer = entity.getSyncedDimmer();
    }

    @Override
    protected int getModelTint(CreakingRenderState state) {
        if (state instanceof DmxCreakingRenderState dmxState) {
            return ARGB.opaque(dmxState.outputRgb);
        }

        return super.getModelTint(state);
    }

    @Override
    protected RenderType getRenderType(
            CreakingRenderState state,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing
    ) {
        if (bodyVisible) {
            Identifier texture = getTextureLocation(state);
            return RenderTypes.entityTranslucentEmissive(texture);
        }

        return super.getRenderType(state, bodyVisible, translucent, glowing);
    }
}
