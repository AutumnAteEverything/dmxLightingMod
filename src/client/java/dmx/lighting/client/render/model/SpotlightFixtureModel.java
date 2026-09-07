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
 * Traditional long-barrel spotlight model.
 *
 * The mechanical hierarchy intentionally mirrors DmxFixtureModel so
 * Spotlight and PAR can use identical Pan/Tilt rendering behavior.
 *
 * Independently addressable parts:
 *
 * Stationary:
 *
 * - Base
 * - Neck
 *
 * Pan-following:
 *
 * - Left yoke arm
 * - Right yoke arm
 * - Yoke crossbar
 *
 * Pan + Tilt:
 *
 * - Long spotlight barrel
 * - Rear housing
 * - Front lens rim
 * - Lens placeholder
 *
 * Model coordinates:
 *
 * 16 model pixels = 1 Minecraft block.
 *
 * The fixture points toward negative local Z.
 */
public final class SpotlightFixtureModel
        extends Model<SpotlightFixtureModel.State> {

    public static final State STATE =
            new State();

    /*
     * -----------------------------------------------------------------
     * Part names
     * -----------------------------------------------------------------
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
     * Parts
     * -----------------------------------------------------------------
     */

    private final ModelPart base;
    private final ModelPart neck;

    private final ModelPart yokeLeft;
    private final ModelPart yokeRight;
    private final ModelPart yokeCrossbar;

    private final ModelPart housing;
    private final ModelPart rearHousing;
    private final ModelPart lensRim;
    private final ModelPart lens;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    public SpotlightFixtureModel(
            ModelPart root
    ) {
        super(
                root,
                RenderTypes::entitySolid
        );

        base =
                root.getChild(
                        PART_BASE
                );

        neck =
                root.getChild(
                        PART_NECK
                );

        yokeLeft =
                root.getChild(
                        PART_YOKE_LEFT
                );

        yokeRight =
                root.getChild(
                        PART_YOKE_RIGHT
                );

        yokeCrossbar =
                root.getChild(
                        PART_YOKE_CROSSBAR
                );

        housing =
                root.getChild(
                        PART_HOUSING
                );

        rearHousing =
                root.getChild(
                        PART_REAR_HOUSING
                );

        lensRim =
                root.getChild(
                        PART_LENS_RIM
                );

        lens =
                root.getChild(
                        PART_LENS
                );
    }

    /*
     * -----------------------------------------------------------------
     * Accessors
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
     * Geometry
     * -----------------------------------------------------------------
     */

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh =
                new MeshDefinition();

        PartDefinition root =
                mesh.getRoot();

        /*
         * -------------------------------------------------------------
         * Base
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
         * Neck
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
         * Yoke
         * -------------------------------------------------------------
         *
         * Slightly narrower than the PAR because the spotlight barrel
         * is narrower.
         */

        root.addOrReplaceChild(
                PART_YOKE_LEFT,
                CubeListBuilder.create()
                        .texOffs(
                                48,
                                30
                        )
                        .addBox(
                                -4.75F,
                                -12.0F,
                                -1.0F,
                                1.25F,
                                9.0F,
                                2.0F
                        ),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                PART_YOKE_RIGHT,
                CubeListBuilder.create()
                        .texOffs(
                                56,
                                30
                        )
                        .addBox(
                                3.50F,
                                -12.0F,
                                -1.0F,
                                1.25F,
                                9.0F,
                                2.0F
                        ),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                PART_YOKE_CROSSBAR,
                CubeListBuilder.create()
                        .texOffs(
                                40,
                                42
                        )
                        .addBox(
                                -4.75F,
                                -4.0F,
                                -1.0F,
                                9.50F,
                                1.25F,
                                2.0F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Long spotlight barrel
         * -------------------------------------------------------------
         *
         * The front is toward negative Z.
         *
         * This is intentionally narrower and much deeper than the PAR
         * fixture housing.
         */

        root.addOrReplaceChild(
                PART_HOUSING,
                CubeListBuilder.create()
                        .texOffs(
                                0,
                                20
                        )
                        .addBox(
                                -3.50F,
                                -11.75F,
                                -8.50F,
                                7.00F,
                                6.50F,
                                14.00F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Rear housing
         * -------------------------------------------------------------
         *
         * Slightly wider electronics / ventilation section.
         */

        root.addOrReplaceChild(
                PART_REAR_HOUSING,
                CubeListBuilder.create()
                        .texOffs(
                                0,
                                42
                        )
                        .addBox(
                                -4.00F,
                                -11.25F,
                                5.50F,
                                8.00F,
                                5.50F,
                                3.00F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Front lens rim
         * -------------------------------------------------------------
         *
         * Four bars form an opening around the independently rendered
         * lens quad.
         */

        root.addOrReplaceChild(
                PART_LENS_RIM,
                CubeListBuilder.create()

                        /*
                         * Top
                         */
                        .texOffs(
                                32,
                                0
                        )
                        .addBox(
                                -3.75F,
                                -11.75F,
                                -9.25F,
                                7.50F,
                                0.65F,
                                0.75F
                        )

                        /*
                         * Bottom
                         */
                        .texOffs(
                                32,
                                3
                        )
                        .addBox(
                                -3.75F,
                                -5.90F,
                                -9.25F,
                                7.50F,
                                0.65F,
                                0.75F
                        )

                        /*
                         * Left
                         */
                        .texOffs(
                                32,
                                6
                        )
                        .addBox(
                                -3.75F,
                                -11.10F,
                                -9.25F,
                                0.65F,
                                5.20F,
                                0.75F
                        )

                        /*
                         * Right
                         */
                        .texOffs(
                                36,
                                6
                        )
                        .addBox(
                                3.10F,
                                -11.10F,
                                -9.25F,
                                0.65F,
                                5.20F,
                                0.75F
                        ),
                PartPose.ZERO
        );

        /*
         * -------------------------------------------------------------
         * Lens placeholder
         * -------------------------------------------------------------
         *
         * Hidden by SpotlightFixtureRenderer. The visible colored lens
         * will be drawn separately using an emissive quad.
         */

        root.addOrReplaceChild(
                PART_LENS,
                CubeListBuilder.create()
                        .texOffs(
                                32,
                                10
                        )
                        .addBox(
                                -3.10F,
                                -11.05F,
                                -9.20F,
                                6.20F,
                                5.10F,
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

    @Override
    public void setupAnim(
            State state
    ) {
        /*
         * Mechanical motion is handled by the renderer PoseStack.
         */
    }

    public static final class State {

        private State() {
        }
    }
}