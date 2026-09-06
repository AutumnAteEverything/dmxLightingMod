package dmx.lighting.client.render.fixture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dmx.lighting.client.DmxFixtureRenderState;
import dmx.lighting.client.render.model.DmxFixtureModel;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

/**
 * Renderer for the prototype PAR fixture.
 *
 * The fixture is rendered as a simple mechanical hierarchy:
 *
 * Installation orientation
 *
 *     Base + neck
 *
 * Installation orientation
 *     + Pan Offset
 *
 *     Yoke
 *
 * Installation orientation
 *     + Pan Offset
 *     + Tilt Offset
 *
 *     Fixture head
 *     Lens
 *     Beam
 *
 * This means live Manual/DMX Pan and Tilt no longer rotate the entire
 * mounting assembly.
 *
 * Instead:
 *
 * - The mounting base remains fixed at the installation orientation.
 * - Pan rotates the yoke and fixture head.
 * - Tilt rotates only the fixture head, lens, and beam.
 *
 * Strobe behavior:
 *
 * - The fixture body always remains visible.
 * - The optical lens becomes dark during the OFF phase of the strobe.
 * - The beam renderer uses the same render-state strobe phase so the
 *   lens and beam can remain synchronized.
 */
public final class ParFixtureRenderer {

    /*
     * -----------------------------------------------------------------
     * Atlas-backed textures
     * -----------------------------------------------------------------
     */

    private static final SpriteId BODY_TEXTURE =
            Sheets.BLOCK_ENTITIES_MAPPER.apply(
                    Identifier.fromNamespaceAndPath(
                            "dmxlighting",
                            "dmx_fixture"
                    )
            );

    private static final SpriteId LENS_TEXTURE =
            Sheets.BLOCK_ENTITIES_MAPPER.apply(
                    Identifier.fromNamespaceAndPath(
                            "dmxlighting",
                            "dmx_fixture_lens"
                    )
            );

    /*
     * -----------------------------------------------------------------
     * Colors and lighting
     * -----------------------------------------------------------------
     */

    private static final int BODY_COLOR =
            0xFF202226;

    private static final int BLACKOUT_LENS_COLOR =
            0xFF08090B;

    private static final int FULL_BRIGHT =
            0x00F000F0;

    /*
     * -----------------------------------------------------------------
     * Mechanical pivots
     * -----------------------------------------------------------------
     */

    /**
     * Approximate vertical center of the fixture housing.
     *
     * This is the tilt pivot through the yoke arms.
     *
     * -8.5 model pixels / 16.
     */
    private static final double TILT_PIVOT_Y =
            -0.53125D;

    /*
     * -----------------------------------------------------------------
     * Lens geometry
     * -----------------------------------------------------------------
     */

    private static final float LENS_LEFT =
            -3.25F / 16.0F;

    private static final float LENS_RIGHT =
            3.25F / 16.0F;

    private static final float LENS_TOP =
            -11.25F / 16.0F;

    private static final float LENS_BOTTOM =
            -5.75F / 16.0F;

    /**
     * Fixture faces negative local Z.
     */
    private static final float LENS_Z =
            -3.61F / 16.0F;

    /*
     * -----------------------------------------------------------------
     * Model passes
     * -----------------------------------------------------------------
     */

    /**
     * Base + neck only.
     *
     * This receives installation orientation but no live Pan/Tilt.
     */
    private final DmxFixtureModel mountModel;

    /**
     * Yoke only.
     *
     * This receives installation orientation + live Pan.
     */
    private final DmxFixtureModel yokeModel;

    /**
     * Fixture head only.
     *
     * This receives installation orientation + live Pan + live Tilt.
     */
    private final DmxFixtureModel headModel;

    private final ParBeamRenderer beamRenderer;

    private final SpriteGetter sprites;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    public ParFixtureRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.sprites =
                context.sprites();

        /*
         * Bake three independent model trees.
         *
         * This is important because each pass has its own permanent
         * visibility configuration.
         */

        this.mountModel =
                new DmxFixtureModel(
                        DmxFixtureModel
                                .createBodyLayer()
                                .bakeRoot()
                );

        this.yokeModel =
                new DmxFixtureModel(
                        DmxFixtureModel
                                .createBodyLayer()
                                .bakeRoot()
                );

        this.headModel =
                new DmxFixtureModel(
                        DmxFixtureModel
                                .createBodyLayer()
                                .bakeRoot()
                );

        configureMountModel(
                mountModel
        );

        configureYokeModel(
                yokeModel
        );

        configureHeadModel(
                headModel
        );

        this.beamRenderer =
                new ParBeamRenderer();
    }

    /*
     * -----------------------------------------------------------------
     * Model visibility configuration
     * -----------------------------------------------------------------
     */

    /**
     * Shows only the stationary mounting base and neck.
     */
    private static void configureMountModel(
            DmxFixtureModel model
    ) {
        hideAllParts(
                model
        );

        model.base().visible =
                true;

        model.neck().visible =
                true;
    }

    /**
     * Shows only the pan-following yoke.
     */
    private static void configureYokeModel(
            DmxFixtureModel model
    ) {
        hideAllParts(
                model
        );

        model.yokeLeft().visible =
                true;

        model.yokeRight().visible =
                true;

        model.yokeCrossbar().visible =
                true;
    }

    /**
     * Shows only the pan-and-tilt fixture head.
     *
     * The old ModelPart lens remains hidden because the visible optical
     * lens is rendered separately as a textured quad.
     */
    private static void configureHeadModel(
            DmxFixtureModel model
    ) {
        hideAllParts(
                model
        );

        model.housing().visible =
                true;

        model.rearHousing().visible =
                true;

        model.lensRim().visible =
                true;
    }

    /**
     * Hides every direct fixture ModelPart.
     */
    private static void hideAllParts(
            DmxFixtureModel model
    ) {
        model.base().visible =
                false;

        model.neck().visible =
                false;

        model.yokeLeft().visible =
                false;

        model.yokeRight().visible =
                false;

        model.yokeCrossbar().visible =
                false;

        model.housing().visible =
                false;

        model.rearHousing().visible =
                false;

        model.lensRim().visible =
                false;

        model.lens().visible =
                false;
    }

    /*
     * -----------------------------------------------------------------
     * Submission
     * -----------------------------------------------------------------
     */

    /**
     * Submits the complete PAR fixture.
     */
    public void submit(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        matrices.pushPose();

        /*
         * Center the logical model in the fixture block.
         */
        matrices.translate(
                0.5D,
                1.0D,
                0.5D
        );

        /*
         * -------------------------------------------------------------
         * Installation orientation
         * -------------------------------------------------------------
         *
         * This establishes the physical mounting orientation.
         *
         * Everything below inherits this transform.
         */

        applyInstallationOrientation(
                state,
                matrices
        );

        /*
         * -------------------------------------------------------------
         * Stationary mount
         * -------------------------------------------------------------
         *
         * No live Pan/Tilt movement.
         */

        submitBodyModel(
                mountModel,
                state,
                matrices,
                queue
        );

        /*
         * -------------------------------------------------------------
         * Pan assembly
         * -------------------------------------------------------------
         */

        matrices.pushPose();

        applyPanOffset(
                state,
                matrices
        );

        /*
         * The yoke follows Pan.
         */
        submitBodyModel(
                yokeModel,
                state,
                matrices,
                queue
        );

        /*
         * -------------------------------------------------------------
         * Tilt assembly
         * -------------------------------------------------------------
         */

        matrices.pushPose();

        applyTiltOffset(
                state,
                matrices
        );

        /*
         * The fixture head follows Pan + Tilt.
         */
        submitBodyModel(
                headModel,
                state,
                matrices,
                queue
        );

        /*
         * Lens and beam must use exactly the same head transform.
         */

        submitLensQuad(
                state,
                matrices,
                queue
        );

        beamRenderer.submit(
                state,
                matrices,
                queue
        );

        matrices.popPose();

        /*
         * End Pan transform.
         */
        matrices.popPose();

        /*
         * End installation transform.
         */
        matrices.popPose();
    }

    /*
     * -----------------------------------------------------------------
     * Body model submission
     * -----------------------------------------------------------------
     */

    private void submitBodyModel(
            DmxFixtureModel model,
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        model.setupAnim(
                DmxFixtureModel.STATE
        );

        queue.submitModel(
                model,
                DmxFixtureModel.STATE,
                matrices,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                BODY_COLOR,
                BODY_TEXTURE,
                sprites,
                0,
                state.breakProgress
        );
    }

    /*
     * -----------------------------------------------------------------
     * Installation orientation
     * -----------------------------------------------------------------
     */

    /**
     * Applies the saved physical installation orientation.
     *
     * This orientation is not live DMX movement.
     */
    private static void applyInstallationOrientation(
            DmxFixtureRenderState state,
            PoseStack matrices
    ) {
        matrices.translate(
                0.0D,
                TILT_PIVOT_Y,
                0.0D
        );

        matrices.mulPose(
                Axis.YP.rotationDegrees(
                        state.getMountPanDegrees()
                )
        );

        matrices.mulPose(
                Axis.XP.rotationDegrees(
                        state.getMountTiltDegrees()
                )
        );

        matrices.translate(
                0.0D,
                -TILT_PIVOT_Y,
                0.0D
        );
    }

    /*
     * -----------------------------------------------------------------
     * Live Pan movement
     * -----------------------------------------------------------------
     */

    /**
     * Applies live Pan relative to the installed fixture orientation.
     *
     * Pan mapping:
     *
     * 0   -> -180 degrees
     * 128 ->    0 degrees
     * 255 -> +180 degrees
     */
    private static void applyPanOffset(
            DmxFixtureRenderState state,
            PoseStack matrices
    ) {
        float panOffsetDegrees =
                state.getResolvedPanDegrees()
                        - state.getMountPanDegrees();

        matrices.mulPose(
                Axis.YP.rotationDegrees(
                        panOffsetDegrees
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Live Tilt movement
     * -----------------------------------------------------------------
     */

    /**
     * Applies live Tilt around the head/yoke pivot.
     *
     * Tilt mapping:
     *
     * 0   -> -90 degrees
     * 128 ->   0 degrees
     * 255 -> +90 degrees
     */
    private static void applyTiltOffset(
            DmxFixtureRenderState state,
            PoseStack matrices
    ) {
        float tiltOffsetDegrees =
                state.getResolvedTiltDegrees()
                        - state.getMountTiltDegrees();

        matrices.translate(
                0.0D,
                TILT_PIVOT_Y,
                0.0D
        );

        matrices.mulPose(
                Axis.XP.rotationDegrees(
                        tiltOffsetDegrees
                )
        );

        matrices.translate(
                0.0D,
                -TILT_PIVOT_Y,
                0.0D
        );
    }

    /*
     * -----------------------------------------------------------------
     * Explicit optical lens
     * -----------------------------------------------------------------
     */

    private void submitLensQuad(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        TextureAtlasSprite sprite =
                sprites.get(
                        LENS_TEXTURE
                );

        float u0 =
                sprite.getU0();

        float u1 =
                sprite.getU1();

        float v0 =
                sprite.getV0();

        float v1 =
                sprite.getV1();

        int color =
                getLensColor(
                        state
                );

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.entityTranslucentEmissive(
                        sprite.atlasLocation()
                ),
                (pose, vertices) ->
                        submitLensGeometry(
                                pose,
                                vertices,
                                color,
                                u0,
                                u1,
                                v0,
                                v1
                        )
        );
    }

    private static void submitLensGeometry(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            int color,
            float u0,
            float u1,
            float v0,
            float v1
    ) {
        addLensVertex(
                pose,
                vertices,
                LENS_LEFT,
                LENS_TOP,
                LENS_Z,
                u0,
                v0,
                color
        );

        addLensVertex(
                pose,
                vertices,
                LENS_LEFT,
                LENS_BOTTOM,
                LENS_Z,
                u0,
                v1,
                color
        );

        addLensVertex(
                pose,
                vertices,
                LENS_RIGHT,
                LENS_BOTTOM,
                LENS_Z,
                u1,
                v1,
                color
        );

        addLensVertex(
                pose,
                vertices,
                LENS_RIGHT,
                LENS_TOP,
                LENS_Z,
                u1,
                v0,
                color
        );
    }

    private static void addLensVertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color
    ) {
        vertices.addVertex(
                pose,
                x,
                y,
                z
        )
        .setColor(
                color
        )
        .setUv(
                u,
                v
        )
        .setOverlay(
                OverlayTexture.NO_OVERLAY
        )
        .setLight(
                FULL_BRIGHT
        )
        .setNormal(
                pose,
                0.0F,
                0.0F,
                -1.0F
        );
    }

    /*
     * -----------------------------------------------------------------
     * Movement mapping
     * -----------------------------------------------------------------
     */

    /**
     * Maps a 0-255 movement control to a centered signed angular range.
     *
     * Value 128 is guaranteed to produce exactly zero degrees.
     */
    private static float mapDmxToSignedDegrees(
            int value,
            float maximumMagnitude
    ) {
        int clampedValue =
                Math.max(
                        0,
                        Math.min(
                                255,
                                value
                        )
                );

        if (clampedValue == 128) {
            return 0.0F;
        }

        if (clampedValue < 128) {
            return -maximumMagnitude
                    * (
                    128 - clampedValue
            )
                    / 128.0F;
        }

        return maximumMagnitude
                * (
                clampedValue - 128
        )
                / 127.0F;
    }

    /*
     * -----------------------------------------------------------------
     * Lens color
     * -----------------------------------------------------------------
     */

    /**
     * Returns the visible optical-lens color.
     *
     * During the OFF phase of the active strobe, the lens is rendered
     * with the same dark color used for blackout.
     *
     * The fixture body remains visible.
     */
    private static int getLensColor(
            DmxFixtureRenderState state
    ) {
        if (!state.isStrobeVisible()) {
            return BLACKOUT_LENS_COLOR;
        }

        int packedRgb =
                state.getPackedRgb()
                        & 0xFFFFFF;

        if (packedRgb == 0) {
            return BLACKOUT_LENS_COLOR;
        }

        return 0xFF000000
                | packedRgb;
    }
}
