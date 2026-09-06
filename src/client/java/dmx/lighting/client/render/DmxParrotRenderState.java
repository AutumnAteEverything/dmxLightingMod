package dmx.lighting.client.render;

import net.minecraft.client.renderer.entity.state.ParrotRenderState;

/**
 * Render-only DMX state for one parrot frame.
 */
public final class DmxParrotRenderState
        extends ParrotRenderState {

    public int outputRgb =
            0xFFFFFF;

    public int dimmer =
            255;
}
