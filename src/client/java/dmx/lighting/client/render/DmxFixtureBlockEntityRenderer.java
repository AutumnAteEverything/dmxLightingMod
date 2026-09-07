package dmx.lighting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dmx.lighting.DmxFixtureBlockEntity;
import dmx.lighting.DmxFixtureProfile;
import dmx.lighting.client.DmxFixtureRenderState;
import dmx.lighting.client.render.fixture.ParFixtureRenderer;
import dmx.lighting.client.render.fixture.SpotlightFixtureRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

/**
 * Main renderer for placed DMX fixtures.
 *
 * Current responsibilities:
 *
 * - Copy synchronized fixture data into DmxFixtureRenderState.
 * - Preserve physical installation Pan/Tilt.
 * - Copy final resolved Pan/Tilt.
 * - Copy final resolved Beam Width and Beam Length.
 * - Dispatch to the appropriate physical fixture renderer.
 *
 * Fixture profile now primarily selects physical appearance:
 *
 * rgb_par
 *     PAR fixture
 *
 * static_spot
 *     Traditional long-barrel Spotlight
 *
 * Both fixture types share the same control architecture and beam
 * behavior.
 */
public final class DmxFixtureBlockEntityRenderer
        implements BlockEntityRenderer<
                DmxFixtureBlockEntity,
                DmxFixtureRenderState
        > {

    /*
     * -----------------------------------------------------------------
     * Debugging
     * -----------------------------------------------------------------
     */

    private static final boolean SHOW_DEBUG_LABELS =
            false;

    /*
     * -----------------------------------------------------------------
     * Profile IDs
     * -----------------------------------------------------------------
     */

    private static final String SPOTLIGHT_PROFILE_ID =
            "static_spot";

    /*
     * -----------------------------------------------------------------
     * Rendering helpers
     * -----------------------------------------------------------------
     */

    private final Font font;

    private final ParFixtureRenderer parFixtureRenderer;

    private final SpotlightFixtureRenderer spotlightFixtureRenderer;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    public DmxFixtureBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.font =
                context.font();

        this.parFixtureRenderer =
                new ParFixtureRenderer(
                        context
                );

        this.spotlightFixtureRenderer =
                new SpotlightFixtureRenderer(
                        context
                );
    }

    /*
     * -----------------------------------------------------------------
     * Render state
     * -----------------------------------------------------------------
     */

    @Override
    public DmxFixtureRenderState createRenderState() {
        return new DmxFixtureRenderState();
    }

    /*
     * -----------------------------------------------------------------
     * State extraction
     * -----------------------------------------------------------------
     */

    @Override
    public void extractRenderState(
            DmxFixtureBlockEntity fixture,
            DmxFixtureRenderState state,
            float tickProgress,
            Vec3 cameraPosition,
            @Nullable
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(
                fixture,
                state,
                tickProgress,
                cameraPosition,
                crumblingOverlay
        );

        DmxFixtureProfile profile =
                fixture.getCurrentProfile();

        /*
         * -------------------------------------------------------------
         * Fixture identity
         * -------------------------------------------------------------
         */

        state.setProfileId(
                profile.id()
        );

        state.setProfileDisplayName(
                profile.displayName()
        );

        /*
         * -------------------------------------------------------------
         * Active fixture output
         * -------------------------------------------------------------
         */

        state.setRed(
                fixture.getActiveRed()
        );

        state.setGreen(
                fixture.getActiveGreen()
        );

        state.setBlue(
                fixture.getActiveBlue()
        );

        state.setWhite(
                fixture.getActiveWhite()
        );

        state.setDimmer(
                fixture.getActiveDimmer()
        );

        /*
         * -------------------------------------------------------------
         * Movement
         * -------------------------------------------------------------
         */

        state.setPan(
                fixture.getActivePan()
        );

        state.setTilt(
                fixture.getActiveTilt()
        );

        /*
         * Legacy Zoom compatibility.
         *
         * Zoom currently aliases Beam Width in FixtureOutput.
         */
        state.setZoom(
                fixture.getActiveZoom()
        );

        /*
         * -------------------------------------------------------------
         * Effects
         * -------------------------------------------------------------
         */

        state.setStrobe(
                fixture.getActiveStrobe()
        );

        state.setGobo(
                fixture.getActiveGobo()
        );

        /*
         * -------------------------------------------------------------
         * Visible RGB
         * -------------------------------------------------------------
         */

        fixture.updateColorInterpolation(
                tickProgress
        );

        state.setPackedRgb(
                fixture.getSmoothedOutputPackedRgb()
        );

        /*
         * -------------------------------------------------------------
         * Legacy profile capabilities
         * -------------------------------------------------------------
         *
         * These remain populated for existing diagnostics and renderer
         * compatibility.
         */

        state.setSupportsRgb(
                profile.channelLayout()
                        .hasRgb()
        );

        state.setSupportsWhite(
                profile.channelLayout()
                        .hasWhite()
        );

        state.setSupportsDimmer(
                profile.channelLayout()
                        .hasDimmer()
        );

        state.setSupportsPan(
                profile.channelLayout()
                        .hasPan()
        );

        state.setSupportsTilt(
                profile.channelLayout()
                        .hasTilt()
        );

        state.setSupportsZoom(
                profile.channelLayout()
                        .hasZoom()
        );

        state.setSupportsStrobe(
                profile.channelLayout()
                        .hasStrobe()
        );

        state.setSupportsGobo(
                profile.channelLayout()
                        .hasGobo()
        );

        state.setBeamFixture(
                profile.isBeamFixture()
        );

        state.setMovingFixture(
                profile.isMovingFixture()
        );

        /*
         * -------------------------------------------------------------
         * Installation orientation
         * -------------------------------------------------------------
         */

        state.setMountPanDegrees(
                fixture.getMountPanDegrees()
        );

        state.setMountTiltDegrees(
                fixture.getMountTiltDegrees()
        );

        /*
         * -------------------------------------------------------------
         * Final resolved movement
         * -------------------------------------------------------------
         */

        fixture.updatePanTiltInterpolation(
                tickProgress
        );

        state.setResolvedPanDegrees(
                fixture.getResolvedPanDegrees()
        );

        state.setResolvedTiltDegrees(
                fixture.getResolvedTiltDegrees()
        );

        /*
         * -------------------------------------------------------------
         * Final resolved beam settings
         * -------------------------------------------------------------
         */

        state.setBeamSettings(
                fixture.getResolvedBeamWidthDegrees(),
                fixture.getResolvedBeamLengthBlocks()
        );
    }

    /*
     * -----------------------------------------------------------------
     * Renderer dispatch
     * -----------------------------------------------------------------
     */

    @Override
    public void submit(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        String profileId =
                state.getProfileId();

        /*
         * Physical fixture selection.
         *
         * Spotlight gets its own model renderer.
         *
         * Every other profile currently falls back to PAR.
         */
        if (SPOTLIGHT_PROFILE_ID.equals(
                profileId
        )) {
            spotlightFixtureRenderer.submit(
                    state,
                    matrices,
                    queue
            );
        } else {
            parFixtureRenderer.submit(
                    state,
                    matrices,
                    queue
            );
        }

        if (SHOW_DEBUG_LABELS) {
            submitProfileLabel(
                    state,
                    matrices,
                    queue
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Debug label
     * -----------------------------------------------------------------
     */

    private void submitProfileLabel(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        String text =
                state.getProfileDisplayName();

        float textWidth =
                font.width(
                        text
                );

        int textColor =
                getReadableFixtureColor(
                        state.getPackedRgb()
                );

        matrices.pushPose();

        matrices.translate(
                FixtureDimensions.BLOCK_CENTER,
                1.18D,
                FixtureDimensions.BLOCK_CENTER
        );

        matrices.mulPose(
                Axis.XP.rotationDegrees(
                        90.0F
                )
        );

        matrices.scale(
                1.0F / 36.0F,
                1.0F / 36.0F,
                1.0F / 36.0F
        );

        queue.submitText(
                matrices,
                -textWidth / 2.0F,
                -4.0F,
                Component.literal(
                        text
                ).getVisualOrderText(),
                false,
                Font.DisplayMode.SEE_THROUGH,
                state.lightCoords,
                textColor,
                0,
                0
        );

        matrices.popPose();
    }

    /*
     * -----------------------------------------------------------------
     * Utilities
     * -----------------------------------------------------------------
     */

    private static int getReadableFixtureColor(
            int packedRgb
    ) {
        packedRgb &=
                0xFFFFFF;

        if (packedRgb == 0) {
            return 0xFFFFFFFF;
        }

        return 0xFF000000
                | packedRgb;
    }
}
