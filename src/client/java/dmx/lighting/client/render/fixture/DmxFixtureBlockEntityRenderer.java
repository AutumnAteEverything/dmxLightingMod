package dmx.lighting.client.render.fixture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dmx.lighting.DmxFixtureBlockEntity;
import dmx.lighting.DmxFixtureProfile;
import dmx.lighting.client.DmxFixtureRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import dmx.lighting.client.render.FixtureDimensions;

/**
 * Main renderer for placed DMX fixtures.
 *
 * Current responsibilities:
 *
 * - Copy synchronized fixture data into DmxFixtureRenderState.
 * - Copy already-resolved Beam Width and Beam Length values.
 * - Render the prototype PAR fixture.
 * - Optionally display a diagnostic fixture-profile label.
 *
 * Beam Width and Beam Length resolution now belongs to
 * DmxFixtureBlockEntity.
 *
 * This keeps Manual mode, DMX mode, rendering, and future non-rendering
 * beam systems on one consistent resolution path.
 *
 * Every fixture profile currently uses ParFixtureRenderer.
 */
public final class DmxFixtureBlockEntityRenderer
        implements BlockEntityRenderer<
                DmxFixtureBlockEntity,
                DmxFixtureRenderState
        > {

    /**
     * Shows the fixture profile name above each fixture.
     *
     * Enable this temporarily while debugging profile selection,
     * synchronization, or renderer dispatch.
     */
    private static final boolean SHOW_DEBUG_LABELS =
            false;

    /**
     * Minecraft font used by the optional diagnostic label.
     */
    private final Font font;

    /**
     * Current prototype fixture renderer.
     *
     * This is temporarily used for every fixture profile.
     */
    private final ParFixtureRenderer parFixtureRenderer;

    /**
     * Creates the block-entity renderer.
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
    }

    /**
     * Creates the reusable render-state snapshot.
     */
    @Override
    public DmxFixtureRenderState createRenderState() {
        return new DmxFixtureRenderState();
    }

    /**
     * Copies mutable block-entity information into the render state.
     *
     * The submission phase renders only from DmxFixtureRenderState
     * rather than reading directly from the block entity.
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
         *
         * These values automatically follow the fixture's currently
         * selected control mode:
         *
         * - DMX
         * - Manual
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

        state.setPan(
                fixture.getActivePan()
        );

        state.setTilt(
                fixture.getActiveTilt()
        );

        /*
         * Compatibility:
         *
         * Zoom remains available in the render state while the codebase
         * completes its migration toward explicit Beam Width semantics.
         */
        state.setZoom(
                fixture.getActiveZoom()
        );

        state.setStrobe(
                fixture.getActiveStrobe()
        );

        state.setGobo(
                fixture.getActiveGobo()
        );

        /*
         * Final visible RGB output after:
         *
         * - Control-mode selection
         * - RGB processing
         * - White-channel mixing
         * - Dimmer application
         */
        fixture.updateColorInterpolation(
                tickProgress
        );

        state.setPackedRgb(
                fixture.getSmoothedOutputPackedRgb()
        );

        /*
         * -------------------------------------------------------------
         * Profile capabilities
         * -------------------------------------------------------------
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
         * Physical installation orientation
         * -------------------------------------------------------------
         *
         * These values are separate from live Manual/DMX Pan and Tilt.
         */

        state.setMountPanDegrees(
                fixture.getMountPanDegrees()
        );

        state.setMountTiltDegrees(
                fixture.getMountTiltDegrees()
        );

        /*
         * -------------------------------------------------------------
         * Resolved visual beam settings
         * -------------------------------------------------------------
         *
         * Beam Width and Beam Length are now resolved centrally by the
         * block entity.
         *
         * This means:
         *
         * - Manual mode uses stored manual beam controls.
         * - DMX mode uses assigned DMX controls.
         * - Unassigned DMX beam parameters fall back to installation
         *   visual settings.
         * - The renderer no longer contains duplicate mapping logic.
         */

        state.setBeamSettings(
                fixture.getResolvedBeamWidthDegrees(),
                fixture.getResolvedBeamLengthBlocks()
        );
    }

    /**
     * Submits the fixture body and optional diagnostic label.
     */
    @Override
    public void submit(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        /*
         * Every profile temporarily uses the PAR renderer.
         *
         * This will later become model-specific dispatch.
         */
        parFixtureRenderer.submit(
                state,
                matrices,
                queue
        );

        if (SHOW_DEBUG_LABELS) {
            submitProfileLabel(
                    state,
                    matrices,
                    queue
            );
        }
    }

    /**
     * Draws the active fixture profile above the fixture.
     *
     * This is retained as an optional debugging tool.
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

        /*
         * Position the label just above the fixture model.
         */
        matrices.translate(
                FixtureDimensions.BLOCK_CENTER,
                1.18D,
                FixtureDimensions.BLOCK_CENTER
        );

        /*
         * Lay the text flat above the fixture.
         */
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

    /**
     * Converts 0xRRGGBB into opaque 0xAARRGGBB.
     *
     * White is used during blackout so a diagnostic label remains
     * readable.
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
