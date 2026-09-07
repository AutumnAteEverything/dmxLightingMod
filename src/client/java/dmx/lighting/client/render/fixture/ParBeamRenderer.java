package dmx.lighting.client.render.fixture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dmx.lighting.FixtureBeamSettings;
import dmx.lighting.client.DmxFixtureRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Experimental visible beam for the prototype PAR fixture.
 *
 * The beam is rendered as two layered eight-sided tapered cones:
 *
 * - A wider translucent outer cone
 * - A narrower brighter inner core
 *
 * Both layers:
 *
 * - Begin near the center of the optical lens
 * - Widen toward the far end
 * - Use the fixture's visible RGB output
 * - Respond to saved beam width and length
 * - Respond to the active dimmer
 * - Fade gradually with distance
 * - Follow the active strobe phase
 *
 * The stored beam-width control is converted into the actual visual
 * cone angle by FixtureBeamSettings.
 *
 * This keeps rendered beam geometry synchronized with other systems
 * such as world illumination.
 */
public final class ParBeamRenderer {

    /*
     * -----------------------------------------------------------------
     * Beam placement
     * -----------------------------------------------------------------
     */

    /**
     * Beam start position slightly in front of the optical lens.
     *
     * The fixture faces toward negative local Z.
     */
    private static final float BEAM_START_Z =
            -0.26F;

    /**
     * Vertical center of the optical lens in local block units.
     */
    private static final float BEAM_CENTER_Y =
            -0.53125F;

    /*
     * -----------------------------------------------------------------
     * Beam geometry
     * -----------------------------------------------------------------
     */

    /**
     * Tiny non-zero outer-beam radius at the lens.
     */
    private static final float START_RADIUS =
            0.18F;

    /**
     * Number of sides used for the circular beam approximation.
     */
    private static final int BEAM_SIDES =
            8;

    /*
     * -----------------------------------------------------------------
     * Outer beam appearance
     * -----------------------------------------------------------------
     */

    /**
     * Maximum outer-beam opacity at full dimmer.
     */
    private static final int MAX_BEAM_ALPHA =
            72;

    /**
     * Minimum visible outer-beam opacity while the fixture is lit.
     */
    private static final int MIN_BEAM_ALPHA =
            8;

    /**
     * Fraction of the near opacity retained at the far end.
     */
    private static final float FAR_ALPHA_MULTIPLIER =
            0.22F;

    /*
     * -----------------------------------------------------------------
     * Inner beam core
     * -----------------------------------------------------------------
     */

    /**
     * Radius of the brighter inner cone relative to the outer beam.
     */
    private static final float INNER_RADIUS_MULTIPLIER =
            0.42F;

    /**
     * Inner-cone opacity relative to the outer beam.
     */
    private static final float INNER_ALPHA_MULTIPLIER =
            0.55F;

    /*
     * -----------------------------------------------------------------
     * Submission
     * -----------------------------------------------------------------
     */

    /**
     * Submits the outer beam and its brighter inner core.
     */
    public void submit(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        /*
         * Do not submit any beam geometry during the OFF phase of the
         * active strobe.
         *
         * The optical lens uses this same render-state decision, so
         * both remain synchronized.
         */
        if (!state.isStrobeVisible()) {
            return;
        }

        int packedRgb =
                state.getPackedRgb()
                        & 0xFFFFFF;

        if (packedRgb == 0
                || !state.isLit()) {

            return;
        }

        int red =
                packedRgb
                        >> 16
                        & 0xFF;

        int green =
                packedRgb
                        >> 8
                        & 0xFF;

        int blue =
                packedRgb
                        & 0xFF;

        /*
         * Outer-beam opacity follows the active dimmer.
         */
        int nearBeamAlpha =
                Math.max(
                        MIN_BEAM_ALPHA,
                        Math.round(
                                MAX_BEAM_ALPHA
                                        * state.getNormalizedDimmer()
                        )
                );

        int farBeamAlpha =
                Math.max(
                        1,
                        Math.round(
                                nearBeamAlpha
                                        * FAR_ALPHA_MULTIPLIER
                        )
                );

        float beamLength =
                state.getBeamLengthBlocks();

        /*
         * Use the shared beam-width mapping.
         *
         * This is now the same calculation that world illumination can
         * use, preventing visual and lighting cones from drifting apart.
         */
        float fullConeDegrees =
                FixtureBeamSettings.mapWidthToVisualConeDegrees(
                        state.getBeamWidthDegrees()
                );

        float halfAngleRadians =
                (float) Math.toRadians(
                        fullConeDegrees
                                / 2.0F
                );

        float outerEndRadius =
                (float) Math.tan(
                        halfAngleRadians
                )
                        * beamLength;

        /*
         * Outer translucent cone.
         */
        queue.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (pose, vertices) ->
                        submitBeamGeometry(
                                pose,
                                vertices,
                                red,
                                green,
                                blue,
                                nearBeamAlpha,
                                farBeamAlpha,
                                beamLength,
                                outerEndRadius,
                                START_RADIUS
                        )
        );

        /*
         * Narrower and brighter inner core.
         */
        int innerNearAlpha =
                Math.max(
                        1,
                        Math.round(
                                nearBeamAlpha
                                        * INNER_ALPHA_MULTIPLIER
                        )
                );

        int innerFarAlpha =
                Math.max(
                        1,
                        Math.round(
                                farBeamAlpha
                                        * INNER_ALPHA_MULTIPLIER
                        )
                );

        float innerStartRadius =
                START_RADIUS
                        * INNER_RADIUS_MULTIPLIER;

        float innerEndRadius =
                outerEndRadius
                        * INNER_RADIUS_MULTIPLIER;

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (pose, vertices) ->
                        submitBeamGeometry(
                                pose,
                                vertices,
                                red,
                                green,
                                blue,
                                innerNearAlpha,
                                innerFarAlpha,
                                beamLength,
                                innerEndRadius,
                                innerStartRadius
                        )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Geometry
     * -----------------------------------------------------------------
     */

    /**
     * Emits one eight-sided tapered cone.
     *
     * Each side is one quad connecting a near-ring segment to the
     * corresponding far-ring segment.
     *
     * Near vertices use nearAlpha.
     * Far vertices use farAlpha.
     */
    private static void submitBeamGeometry(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            int red,
            int green,
            int blue,
            int nearAlpha,
            int farAlpha,
            float beamLength,
            float endRadius,
            float startRadius
    ) {
        float nearZ =
                BEAM_START_Z;

        float farZ =
                BEAM_START_Z
                        - beamLength;

        for (
                int side = 0;
                side < BEAM_SIDES;
                side++
        ) {
            float angleA =
                    (float) (
                            Math.PI
                                    * 2.0D
                                    * side
                                    / BEAM_SIDES
                    );

            float angleB =
                    (float) (
                            Math.PI
                                    * 2.0D
                                    * (
                                    side + 1
                            )
                                    / BEAM_SIDES
                    );

            float nearAx =
                    (float) Math.cos(
                            angleA
                    )
                            * startRadius;

            float nearAy =
                    BEAM_CENTER_Y
                            + (float) Math.sin(
                            angleA
                    )
                            * startRadius;

            float nearBx =
                    (float) Math.cos(
                            angleB
                    )
                            * startRadius;

            float nearBy =
                    BEAM_CENTER_Y
                            + (float) Math.sin(
                            angleB
                    )
                            * startRadius;

            float farAx =
                    (float) Math.cos(
                            angleA
                    )
                            * endRadius;

            float farAy =
                    BEAM_CENTER_Y
                            + (float) Math.sin(
                            angleA
                    )
                            * endRadius;

            float farBx =
                    (float) Math.cos(
                            angleB
                    )
                            * endRadius;

            float farBy =
                    BEAM_CENTER_Y
                            + (float) Math.sin(
                            angleB
                    )
                            * endRadius;

            addVertex(
                    pose,
                    vertices,
                    nearAx,
                    nearAy,
                    nearZ,
                    red,
                    green,
                    blue,
                    nearAlpha
            );

            addVertex(
                    pose,
                    vertices,
                    nearBx,
                    nearBy,
                    nearZ,
                    red,
                    green,
                    blue,
                    nearAlpha
            );

            addVertex(
                    pose,
                    vertices,
                    farBx,
                    farBy,
                    farZ,
                    red,
                    green,
                    blue,
                    farAlpha
            );

            addVertex(
                    pose,
                    vertices,
                    farAx,
                    farAy,
                    farZ,
                    red,
                    green,
                    blue,
                    farAlpha
            );
        }
    }

    /**
     * Adds one transformed and colored vertex.
     */
    private static void addVertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x,
            float y,
            float z,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        vertices.addVertex(
                pose,
                x,
                y,
                z
        )
        .setColor(
                red,
                green,
                blue,
                alpha
        );
    }
}