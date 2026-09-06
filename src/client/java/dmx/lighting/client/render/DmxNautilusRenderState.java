package dmx.lighting.client.render;

import net.minecraft.client.renderer.entity.state.NautilusRenderState;

/**
 * Render-only DMX state for one Nautilus frame.
 */
public final class DmxNautilusRenderState
        extends NautilusRenderState {

    public int outputRgb =
            0xFFFFFF;

    public int dimmer =
            255;
}
