package dmx.lighting.client.render;

import net.minecraft.client.renderer.entity.state.EndermanRenderState;

/**
 * Render-only DMX state for one Enderman frame.
 */
public final class DmxEndermanRenderState
        extends EndermanRenderState {

    public int outputRgb =
            0xFFFFFF;

    public int dimmer =
            255;
}
