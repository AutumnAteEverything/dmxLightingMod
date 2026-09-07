package dmx.lighting.client.render;

import net.minecraft.client.renderer.entity.state.AxolotlRenderState;

/**
 * Render-only DMX state for one Axolotl frame.
 */
public final class DmxAxolotlRenderState
        extends AxolotlRenderState {

    public int outputRgb =
            0xFFFFFF;

    public int dimmer =
            255;
}
