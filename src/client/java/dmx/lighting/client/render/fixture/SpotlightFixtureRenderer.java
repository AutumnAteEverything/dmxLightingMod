package dmx.lighting.client.render.fixture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dmx.lighting.client.DmxFixtureRenderState;
import dmx.lighting.client.render.model.SpotlightFixtureModel;

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
 * Renderer for the traditional long-barrel Spotlight fixture.
 *
 * Functionally, Spotlight behaves exactly like the PAR fixture.
 *
 * Mechanical hierarchy:
 *
 * Installation orientation
 *
 *     Base + neck
 *
 * Installation orientation
 *     + Pan Offset
 *
 *     Yoke / support arms
 *
 * Installation orientation
 *     + Pan Offset
 *     + Tilt Offset
 *
 *     Spotlight barrel
 *     Lens
 *     Beam
 *
 * Therefore:
 *
 * - The mounting base remains fixed at the installation orientation.
 * - Pan rotates the yoke and fixture head.
 * - Tilt rotates the fixture head, lens, and beam.
 * - Beam Width, Beam Length, color, dimmer, and strobe behave exactly
 *   like the PAR fixture.
 *
 * The Spotlight uses its own lens texture so its optical appearance can
 * differ from the PAR while retaining identical control behavior.
 */
public final class SpotlightFixtureRenderer {

    /*
     * -----------------------------------------------------------------
     * Atlas-backed textures
     * -----------------------------------------------------------------
     */

    /**
     * Spotlight currently shares the PAR body texture.
     */
    private static final SpriteId BODY_TEXTURE =
            Sheets.BLOCK_ENTITIES_MAPPER.apply(
                    Identifier.fromNamespaceAndPath(
                            "dmxlighting",
                            "dmx_fixture"
                    )
            );

    /**
     * Dedicated Spotlight optical lens texture.
     *
     * Expected resource:
     *
     * assets/dmxlighting/textures/entity/
     * dmx_fixture_spotlight_lens.png
     */
    private static final SpriteId LENS_TEXTURE =
            Sheets.BLOCK_ENTITIES_MAPPER.apply(
                    Identifier.fromNamespaceAndPath(
                            "dmxlighting",
                            "dmx_fixture_spotlight_lens"
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
     * Mechanical pivot
     * -----------------------------------------------------------------
     */

    /**
     * Vertical center of the Spotlight barrel.
     *
     * The model barrel spans approximately:
     *
     * Y -11.75 through -5.25
     *
     * giving a center near:
     *
     * -8.5 model pixels
     *
     * which intentionally matches the current PAR tilt pivot.
     */
    private static final double TILT_PIVOT_Y =
            -0.53125D;

    /*
     * -----------------------------------------------------------------
     * Spotlight lens geometry
     * -----------------------------------------------------------------
     *
     * The Spotlight model has its front lens around:
     *
     * Z = -9.2 model pixels
     *
     * rather than the PAR's much shallower front surface.
     */

    private static final float LENS_LEFT =
            -3.10F / 16.0F;

    private static final float LENS_RIGHT =
            3.10F / 16.0F;

    private static final float LENS_TOP =
            -11.05F / 16.0F;

    private static final float LENS_BOTTOM =
            -5.95F / 16.0F;

    /**
     * Spotlight faces negative local Z.
     */
    private static final float LENS_Z =
            -9.21F / 16.0F;

    /*
     * -----------------------------------------------------------------
     * Beam positioning
     * -----------------------------------------------------------------
     *
     * ParBeamRenderer currently assumes its beam begins at:
     *
     *     Z = -0.26
     *
     * local block units.
     *
     * The Spotlight barrel is considerably longer, so we translate the
     * beam renderer forward so its starting ring appears immediately in
     * front of the Spotlight lens.
     */

    private static final double BEAM_FORWARD_OFFSET_Z =
            -0.324375D;

    /*
     * -----------------------------------------------------------------
     * Model passes
     * -----------------------------------------------------------------
     */

    /**
     * Stationary base + neck.
     */
    private final SpotlightFixtureModel mountModel;

    /**
     * Pan-following support/yoke.
     */
    private final SpotlightFixtureModel yokeModel;

    /**
     * Pan-and-tilt-following Spotlight barrel.
     */
    private final SpotlightFixtureModel headModel;

    /**
     * Shared beam renderer.
     *
     * Reusing the PAR beam renderer keeps Beam Width, Beam Length,
     * color, dimmer, and strobe behavior identical.
     */
    private final ParBeamRenderer beamRenderer;

    private final SpriteGetter sprites;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    public SpotlightFixtureRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.sprites =
                context.sprites();

        /*
         * Bake three independent copies of the Spotlight model.
         */

        this.mountModel =
                new SpotlightFixtureModel(
                        SpotlightFixtureModel
                                .createBodyLayer()
                                .bakeRoot()
                );

        this.yokeModel =
                new SpotlightFixtureModel(
                        SpotlightFixtureModel
                                .createBodyLayer()
                                .bakeRoot()
                );

        this.headModel =
                new SpotlightFixtureModel(
                        SpotlightFixtureModel
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
     * Model visibility
     * -----------------------------------------------------------------
     */

    /**
     * Shows only the stationary mounting base and neck.
     */
    private static void configureMountModel(
            SpotlightFixtureModel model
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
     * Shows only the support/yoke assembly.
     */
    private static void configureYokeModel(
            SpotlightFixtureModel model
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
     * Shows only the tilting Spotlight head.
     *
     * The model's built-in lens placeholder stays hidden because the
     * visible optical lens is submitted independently as an emissive
     * textured quad.
     */
    private static void configureHeadModel(
            SpotlightFixtureModel model
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
     * Hides every Spotlight ModelPart.
     */
    private static void hideAllParts(
            SpotlightFixtureModel model
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
     * Submits the complete Spotlight fixture.
     */
    public void submit(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        matrices.pushPose();

        /*
         * Center the model in the fixture block.
         */
        matrices.translate(
                0.5D,
                1.0D,
                0.5D
        );

        /*
         * -------------------------------------------------------------
         * Physical installation orientation
         * -------------------------------------------------------------
         */

        applyInstallationOrientation(
                state,
                matrices
        );

        /*
         * -------------------------------------------------------------
         * Stationary base
         * -------------------------------------------------------------
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

        submitBodyModel(
                headModel,
                state,
                matrices,
                queue
        );

        /*
         * Lens follows the exact same Pan + Tilt transform as the
         * barrel.
         */
        submitLensQuad(
                state,
                matrices,
                queue
        );

        /*
         * Move the shared beam renderer forward so its near ring begins
         * at the Spotlight's front lens.
         */
        matrices.pushPose();

        matrices.translate(
                0.0D,
                0.0D,
                BEAM_FORWARD_OFFSET_Z
        );

        beamRenderer.submit(
                state,
                matrices,
                queue
        );

        matrices.popPose();

        /*
         * End Tilt.
         */
        matrices.popPose();

        /*
         * End Pan.
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
            SpotlightFixtureModel model,
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        model.setupAnim(
                SpotlightFixtureModel.STATE
        );

        queue.submitModel(
                model,
                SpotlightFixtureModel.STATE,
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
     * Pan
     * -----------------------------------------------------------------
     */

    /**
     * Applies live Pan relative to installation orientation.
     *
     * 0   -> -180 degrees
     * 128 -> 0 degrees
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
     * Tilt
     * -----------------------------------------------------------------
     */

    /**
     * Applies live Tilt around the Spotlight barrel pivot.
     *
     * 0   -> -90 degrees
     * 128 -> 0 degrees
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
     * Optical lens
     * -----------------------------------------------------------------
     */

    /**
     * Submits the colored Spotlight lens.
     *
     * The Spotlight has its own texture but uses the same color,
     * dimmer, and strobe behavior as the PAR.
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
     * Maps 0-255 to a centered signed angular range.
     *
     * 128 is guaranteed to be exactly zero.
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
     * Lens color / strobe
     * -----------------------------------------------------------------
     */

    /**
     * During the OFF phase of Strobe, the optical lens becomes dark.
     *
     * The Spotlight body remains visible.
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
