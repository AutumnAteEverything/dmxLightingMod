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
 * Prototype PAR fixture model.
 *
 * The fixture is divided into independently addressable parts:
 *
 * Stationary or pan-only mounting assembly:
 *
 * - Mounting base
 * - Mounting neck
 * - Left yoke arm
 * - Right yoke arm
 * - Yoke crossbar
 *
 * Pan-and-tilt fixture assembly:
 *
 * - Main housing
 * - Rear housing
 * - Lens rim
 * - Lens placeholder
 *
 * Keeping references to the individual ModelParts allows the renderer
 * to submit the mounting assembly and fixture body under different
 * transforms.
 *
 * Model coordinates use Minecraft model pixels:
 *
 *     16 model pixels = one Minecraft block
 *
 * Positive model-space Y points downward. Negative Y coordinates extend
 * the fixture upward from the model origin.
 */
public final class DmxFixtureModel
        extends Model<DmxFixtureModel.State> {

    /*
     * -----------------------------------------------------------------
     * Shared model state
     * -----------------------------------------------------------------
     */

    /**
     * Stateless animation value required by Model's generic API.
     */
    public static final State STATE =
            new State();

    /*
     * -----------------------------------------------------------------
     * Model-part names
     * -----------------------------------------------------------------
     *
     * These constants must exactly match the child names used in
     * createBodyLayer().
     */

    private static final String PART_BASE =
            "base";

    private static final String PART_NECK =
            "neck";

    private static final String PART_YOKE_LEFT =
            "yoke_left";

    private static final String PART_YOKE_RIGHT =
            "yoke_right";

    private static final String PART_YOKE_CROSSBAR =
            "yoke_crossbar";

    private static final String PART_HOUSING =
            "housing";

    private static final String PART_REAR_HOUSING =
            "rear_housing";

    private static final String PART_LENS_RIM =
            "lens_rim";

    private static final String PART_LENS =
            "lens";

    /*
     * -----------------------------------------------------------------
     * Individual fixture parts
     * -----------------------------------------------------------------
     */

    /**
     * Bottom mounting plate.
     */
    private final ModelPart base;

    /**
     * Short connector between the mounting base and yoke.
     */
    private final ModelPart neck;

    /**
     * Left side of the PAR yoke.
     */
    private final ModelPart yokeLeft;

    /**
     * Right side of the PAR yoke.
     */
    private final ModelPart yokeRight;

    /**
     * Lower crossbar connecting the yoke arms.
     */
    private final ModelPart yokeCrossbar;

    /**
     * Main PAR fixture body.
     */
    private final ModelPart housing;

    /**
     * Smaller electronics enclosure at the rear.
     */
    private final ModelPart rearHousing;

    /**
     * Hollow frame surrounding the front lens.
     */
    private final ModelPart lensRim;

    /**
     * Original lens placeholder.
     *
     * ParFixtureRenderer hides this part because the visible colored lens
     * is rendered by DmxFixtureLensModel.
     */
    private final ModelPart lens;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    /**
     * Creates the fixture model around its baked root part.
     */
    public DmxFixtureModel(
            ModelPart root
    ) {
        super(
                root,
                RenderTypes::entitySolid
        );

        this.base =
                root.getChild(
                        PART_BASE
                );

        this.neck =
                root.getChild(
                        PART_NECK
                );

        this.yokeLeft =
                root.getChild(
                        PART_YOKE_LEFT
                );

        this.yokeRight =
                root.getChild(
                        PART_YOKE_RIGHT
                );

        this.yokeCrossbar =
                root.getChild(
                        PART_YOKE_CROSSBAR
                );

        this.housing =
                root.getChild(
                        PART_HOUSING
                );

        this.rearHousing =
                root.getChild(
                        PART_REAR_HOUSING
                );

        this.lensRim =
                root.getChild(
                        PART_LENS_RIM
                );

        this.lens =
                root.getChild(
                        PART_LENS
                );
    }

    /*
     * -----------------------------------------------------------------
     * Part accessors
     * -----------------------------------------------------------------
     */

    public ModelPart base() {
        return base;
    }

    public ModelPart neck() {
        return neck;
    }

    public ModelPart yokeLeft() {
        return yokeLeft;
    }

    public ModelPart yokeRight() {
        return yokeRight;
    }

    public ModelPart yokeCrossbar() {
        return yokeCrossbar;
    }

    public ModelPart housing() {
        return housing;
    }

    public ModelPart rearHousing() {
        return rearHousing;
    }

    public ModelPart lensRim() {
        return lensRim;
    }

    public ModelPart lens() {
        return lens;
    }

    /*
     * -----------------------------------------------------------------
     * Model geometry
     * -----------------------------------------------------------------
     */

    /**
     * Creates the PAR fixture body layer.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh =
                new MeshDefinition();

        PartDefinition root =
                mesh.getRoot();

        /*
         * -------------------------------------------------------------
         * Mounting base
         * -------------------------------------------------------------
         */
        root.addOrReplaceChild(
                PART_BASE,
                CubeListBuilder.create()
                        .texOffs(
                                0,
                                0
                        )
                        .addBox(
                                -5.0F,
                                -2.0F,
                                -5.0F,
                                10.0F,
                                2.0F,
                                10.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Mounting neck
         * -------------------------------------------------------------
         */
        root.addOrReplaceChild(
                PART_NECK,
                CubeListBuilder.create()
                        .texOffs(
                                0,
                                14
                        )
                        .addBox(
                                -1.5F,
                                -4.0F,
                                -1.5F,
                                3.0F,
                                2.0F,
                                3.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * PAR yoke: left arm
         * -------------------------------------------------------------
         */
        root.addOrReplaceChild(
                PART_YOKE_LEFT,
                CubeListBuilder.create()
                        .texOffs(
                                48,
                                30
                        )
                        .addBox(
                                -6.25F,
                                -12.0F,
                                -1.0F,
                                1.25F,
                                9.0F,
                                2.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * PAR yoke: right arm
         * -------------------------------------------------------------
         */
        root.addOrReplaceChild(
                PART_YOKE_RIGHT,
                CubeListBuilder.create()
                        .texOffs(
                                56,
                                30
                        )
                        .addBox(
                                5.0F,
                                -12.0F,
                                -1.0F,
                                1.25F,
                                9.0F,
                                2.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * PAR yoke: lower crossbar
         * -------------------------------------------------------------
         */
        root.addOrReplaceChild(
                PART_YOKE_CROSSBAR,
                CubeListBuilder.create()
                        .texOffs(
                                40,
                                42
                        )
                        .addBox(
                                -6.25F,
                                -4.0F,
                                -1.0F,
                                12.5F,
                                1.25F,
                                2.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Main PAR housing
         * -------------------------------------------------------------
         *
         * The fixture faces toward negative Z.
         */
        root.addOrReplaceChild(
                PART_HOUSING,
                CubeListBuilder.create()
                        .texOffs(
                                0,
                                20
                        )
                        .addBox(
                                -5.0F,
                                -13.0F,
                                -3.0F,
                                10.0F,
                                9.0F,
                                6.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Rear electronics housing
         * -------------------------------------------------------------
         */
        root.addOrReplaceChild(
                PART_REAR_HOUSING,
                CubeListBuilder.create()
                        .texOffs(
                                32,
                                20
                        )
                        .addBox(
                                -3.75F,
                                -11.75F,
                                3.0F,
                                7.5F,
                                6.5F,
                                2.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Hollow front lens rim
         * -------------------------------------------------------------
         *
         * Four bars form a real opening for the separately rendered
         * colored lens.
         */
        root.addOrReplaceChild(
                PART_LENS_RIM,
                CubeListBuilder.create()

                        /*
                         * Top bar.
                         */
                        .texOffs(
                                32,
                                0
                        )
                        .addBox(
                                -4.25F,
                                -12.25F,
                                -3.75F,
                                8.5F,
                                0.75F,
                                0.75F
                        )

                        /*
                         * Bottom bar.
                         */
                        .texOffs(
                                32,
                                3
                        )
                        .addBox(
                                -4.25F,
                                -5.50F,
                                -3.75F,
                                8.5F,
                                0.75F,
                                0.75F
                        )

                        /*
                         * Left bar.
                         */
                        .texOffs(
                                32,
                                6
                        )
                        .addBox(
                                -4.25F,
                                -11.50F,
                                -3.75F,
                                0.75F,
                                6.0F,
                                0.75F
                        )

                        /*
                         * Right bar.
                         */
                        .texOffs(
                                36,
                                6
                        )
                        .addBox(
                                3.50F,
                                -11.50F,
                                -3.75F,
                                0.75F,
                                6.0F,
                                0.75F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Original body-model lens placeholder
         * -------------------------------------------------------------
         *
         * ParFixtureRenderer keeps this hidden because the visible lens
         * is submitted independently by DmxFixtureLensModel.
         */
        root.addOrReplaceChild(
                PART_LENS,
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

    /*
     * -----------------------------------------------------------------
     * Animation
     * -----------------------------------------------------------------
     */

    /**
     * The static PAR fixture currently has no model-part animation.
     *
     * Installation orientation is applied by the renderer's PoseStack.
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
     * Empty state used by the static fixture model.
     */
    public static final class State {

        private State() {
        }
    }
}