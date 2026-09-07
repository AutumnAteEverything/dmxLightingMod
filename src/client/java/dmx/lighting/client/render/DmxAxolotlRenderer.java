package dmx.lighting.client.render;

import dmx.lighting.DmxAxolotlEntity;

import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

/** Reuses vanilla Axolotl variants and animations with an emissive DMX tint. */
public final class DmxAxolotlRenderer extends AxolotlRenderer {

    public DmxAxolotlRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DmxAxolotlRenderState createRenderState() {
        return new DmxAxolotlRenderState();
    }

    @Override
    public void extractRenderState(
            Axolotl entity,
            AxolotlRenderState state,
            float tickProgress
    ) {
        super.extractRenderState(entity, state, tickProgress);

        if (!(entity instanceof DmxAxolotlEntity dmxAxolotl)
                || !(state instanceof DmxAxolotlRenderState dmxState)) {
            return;
        }

        dmxAxolotl.updateColorInterpolation(tickProgress);
        dmxState.outputRgb = dmxAxolotl.getSmoothedSyncedOutputRgb();
        dmxState.dimmer = dmxAxolotl.getSyncedDimmer();
    }

    @Override
    protected int getModelTint(AxolotlRenderState state) {
        if (state instanceof DmxAxolotlRenderState dmxState) {
            return ARGB.opaque(dmxState.outputRgb);
        }

        return super.getModelTint(state);
    }

    @Override
    protected RenderType getRenderType(
            AxolotlRenderState state,
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
