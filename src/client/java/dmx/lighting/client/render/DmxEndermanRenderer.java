package dmx.lighting.client.render;

import dmx.lighting.DmxEndermanEntity;

import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.monster.EnderMan;

/**
 * DMX-aware Enderman renderer.
 *
 * The normal Enderman model and animation layers are reused, while the
 * body tint comes from the Enderman's synced DMX output.
 */
public final class DmxEndermanRenderer
        extends EndermanRenderer {

    private static final Identifier DMX_ENDERMAN_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    "dmxlighting",
                    "textures/entity/dmx_enderman.png"
            );

    public DmxEndermanRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context
        );
    }

    @Override
    public DmxEndermanRenderState createRenderState() {
        return new DmxEndermanRenderState();
    }

    @Override
    public void extractRenderState(
            EnderMan entity,
            EndermanRenderState state,
            float tickProgress
    ) {
        super.extractRenderState(
                entity,
                state,
                tickProgress
        );

        if (!(entity instanceof DmxEndermanEntity dmxEnderman)
                || !(state instanceof DmxEndermanRenderState dmxState)) {

            return;
        }

        dmxEnderman.updateColorInterpolation(
                tickProgress
        );

        dmxState.outputRgb =
                dmxEnderman.getSmoothedSyncedOutputRgb();

        dmxState.dimmer =
                dmxEnderman.getSyncedDimmer();
    }

    @Override
    public Identifier getTextureLocation(
            EndermanRenderState state
    ) {
        return DMX_ENDERMAN_TEXTURE;
    }

    @Override
    protected int getModelTint(
            EndermanRenderState state
    ) {
        if (state instanceof DmxEndermanRenderState dmxState) {
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
            EndermanRenderState state,
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
