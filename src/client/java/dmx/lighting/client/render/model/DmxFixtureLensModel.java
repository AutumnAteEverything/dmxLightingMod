package dmx.lighting.client.render.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;


/**
 * Independently rendered optical lens for the prototype PAR fixture.
 *
 * Keeping the lens in its own Model instance avoids mutating visibility
 * on the deferred body-model submission.
 *
 * This separate model can later support:
 *
 * - Active RGB tint
 * - White-channel mixing
 * - Dimmer brightness
 * - Emissive rendering
 * - Strobe behavior
 * - Beam origin and direction
 */
public final class DmxFixtureLensModel
        extends Model<DmxFixtureLensModel.State> {

    /**
     * Stateless model state required by Minecraft's Model API.
     */
    public static final State STATE =
            new State();

    /**
     * Creates the lens model around its baked root part.
     */
        public DmxFixtureLensModel(
                ModelPart root
        ) {
        super(
                root,
                RenderTypes::entitySolid
        );
        }

    /**
     * Creates the front optical surface.
     *
     * These coordinates match the lens geometry previously contained in
     * DmxFixtureModel.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh =
                new MeshDefinition();

        PartDefinition root =
                mesh.getRoot();

        root.addOrReplaceChild(
                "lens",
                CubeListBuilder.create()
                        .texOffs(
                                32,
                                10
                        )
                        .addBox(
                        -3.25F,
                        -11.25F,
                        -3.70F,
                        6.50F,
                        5.50F,
                        0.20F
                ),
                PartPose.ZERO
        );

        return LayerDefinition.create(
                mesh,
                64,
                64
        );
    }

    /**
     * The static lens currently has no animation.
     */
    @Override
    public void setupAnim(
            State state
    ) {
        /*
         * Intentionally empty.
         */
    }

    /**
     * Empty state for the static lens.
     */
    public static final class State {

        private State() {
        }
    }
}