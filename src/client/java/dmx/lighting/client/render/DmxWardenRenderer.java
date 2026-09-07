package dmx.lighting.client.render;

import dmx.lighting.DmxWardenEntity;

import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.monster.warden.Warden;

/**
 * DMX-aware Warden renderer.
 *
 * The normal Warden model and animation layers are reused, while the
 * body tint comes from the Warden's synced DMX output.
 */
public final class DmxWardenRenderer
        extends WardenRenderer {

    private static final Identifier DMX_WARDEN_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "dmxlighting",
                    "textures/entity/dmx_warden.png"
            );

    public DmxWardenRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context
        );
    }

    @Override
    public DmxWardenRenderState createRenderState() {
        return new DmxWardenRenderState();
    }

    @Override
    public void extractRenderState(
            Warden entity,
            WardenRenderState state,
            float tickProgress
    ) {
        super.extractRenderState(
                entity,
                state,
                tickProgress
        );

        if (!(entity instanceof DmxWardenEntity dmxWarden)
                || !(state instanceof DmxWardenRenderState dmxState)) {

            return;
        }

        dmxWarden.updateColorInterpolation(
                tickProgress
        );

        dmxState.outputRgb =
                dmxWarden.getSmoothedSyncedOutputRgb();

        dmxState.dimmer =
                dmxWarden.getSyncedDimmer();
    }

    @Override
    public Identifier getTextureLocation(
            WardenRenderState state
    ) {
        return DMX_WARDEN_TEXTURE;
    }

    @Override
    protected int getModelTint(
            WardenRenderState state
    ) {
        if (state instanceof DmxWardenRenderState dmxState) {
            return ARGB.opaque(
                    dmxState.outputRgb
            );
        }

        return super.getModelTint(
                state
        );
    }

    @Override
    protected RenderType getRenderType(
            WardenRenderState state,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing
    ) {
        if (bodyVisible) {
            return RenderTypes.entityTranslucentEmissive(
                    getTextureLocation(
                            state
                    )
            );
        }

        return super.getRenderType(
                state,
                bodyVisible,
                translucent,
                glowing
        );
    }
}
