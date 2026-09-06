package dmx.lighting.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Per-frame render data for the full-bright DMX Block surface.
 */
public final class DmxPixelBlockRenderState
        extends BlockEntityRenderState {

    private int packedRgb;

    private int skin =
            1;

    public int getPackedRgb() {
        return packedRgb;
    }

    public void setPackedRgb(
            int packedRgb
    ) {
        this.packedRgb =
                packedRgb & 0xFFFFFF;
    }

    public int getSkin() {
        return skin;
    }

    public void setSkin(
            int skin
    ) {
        this.skin =
                Math.clamp(
                        skin,
                        1,
                        5
                );
    }
}
