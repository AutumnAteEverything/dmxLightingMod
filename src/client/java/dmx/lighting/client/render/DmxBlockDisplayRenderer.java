package dmx.lighting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import dmx.lighting.DmxBlockDisplayEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Renders a DMX cube through Minecraft's normal display transform path. */
public final class DmxBlockDisplayRenderer
        extends DisplayRenderer<
                DmxBlockDisplayEntity,
                Void,
                DmxBlockDisplayRenderState
        > {

    public DmxBlockDisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DmxBlockDisplayRenderState createRenderState() {
        return new DmxBlockDisplayRenderState();
    }

    @Override
    public void extractRenderState(
            DmxBlockDisplayEntity entity,
            DmxBlockDisplayRenderState state,
            float tickProgress
    ) {
        super.extractRenderState(entity, state, tickProgress);
        state.packedRgb = entity.getVisiblePackedRgb(tickProgress);
        state.skin = entity.getRenderedSkin();
    }

    @Override
    protected void submitInner(
            DmxBlockDisplayRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            float interpolationProgress
    ) {
        DmxPixelCubeRenderer.submit(
                matrices,
                queue,
                state.packedRgb,
                state.skin
        );
    }
}
