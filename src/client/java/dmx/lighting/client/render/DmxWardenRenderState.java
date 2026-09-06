package dmx.lighting.client.render;

import net.minecraft.client.renderer.entity.state.WardenRenderState;

/**
 * Render-only DMX state for one Warden frame.
 */
public final class DmxWardenRenderState
        extends WardenRenderState {

    public int outputRgb =
            0xFFFFFF;

    public int dimmer =
            255;
}
