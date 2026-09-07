package dmx.lighting.client.render;

import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;

/** Render-only DMX values for one Block Display frame. */
public final class DmxBlockDisplayRenderState
        extends DisplayEntityRenderState {

    public int packedRgb;
    public int skin = 1;

    @Override
    public boolean hasSubState() {
        return true;
    }
}
