package dmx.lighting.client.render;

import net.minecraft.client.renderer.entity.state.CreakingRenderState;

/**
 * Render-only DMX state for one Creaking frame.
 */
public final class DmxCreakingRenderState
        extends CreakingRenderState {

    public int outputRgb =
            0xFFFFFF;

    public int dimmer =
            255;
}
