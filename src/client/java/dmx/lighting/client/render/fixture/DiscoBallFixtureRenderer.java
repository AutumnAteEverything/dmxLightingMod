package dmx.lighting.client.render.fixture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dmx.lighting.client.DmxFixtureRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Renders a stationary mount, rotating mirror cube, and 17 beams. */
public final class DiscoBallFixtureRenderer {

    private static final float BODY_MIN = 0.27F;
    private static final float BODY_MAX = 0.73F;
    private static final float BODY_CENTER = 0.5F;
    private static final int MIRROR_TILES = 4;
    private static final float BEAM_LENGTH = 7.0F;
    private static final float BEAM_START_HALF_WIDTH = 0.025F;
    private static final float BEAM_END_HALF_WIDTH = 0.075F;
    private static final int BEAMS_PER_RING = 8;
    private static final float RING_STEP_DEGREES =
            360.0F / BEAMS_PER_RING;
    private static final float DIAGONAL_OFFSET_DEGREES =
            RING_STEP_DEGREES / 2.0F;
    private static final float DIAGONAL_TILT_DEGREES = 42.0F;

    private static final int[] BEAM_COLORS = {
            0xFF3B30,
            0xFF9F0A,
            0xFFE84A,
            0x4CFF64,
            0x28D7FF,
            0x3478FF,
            0xAF52DE,
            0xFF2BD6
    };

    public void submit(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        submitMount(state.isDiscoBaseOnTop(), matrices, queue);

        matrices.pushPose();
        matrices.translate(BODY_CENTER, BODY_CENTER, BODY_CENTER);
        matrices.mulPose(
                Axis.YP.rotationDegrees(
                        state.getDiscoRotationDegrees()
                )
        );
        matrices.translate(-BODY_CENTER, -BODY_CENTER, -BODY_CENTER);

        submitMirrorCube(state, matrices, queue);
        submitBeams(state, matrices, queue);
        matrices.popPose();
    }

    private static void submitMount(
            boolean baseOnTop,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        queue.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (pose, vertices) -> {
                    float plateMinY = baseOnTop ? 0.90F : 0.02F;
                    float plateMaxY = baseOnTop ? 0.98F : 0.10F;
                    float stemMinY = baseOnTop ? BODY_MAX : 0.10F;
                    float stemMaxY = baseOnTop ? 0.90F : BODY_MIN;

                    submitBox(
                            pose,
                            vertices,
                            0.20F,
                            plateMinY,
                            0.20F,
                            0.80F,
                            plateMaxY,
                            0.80F,
                            0xFF24272C
                    );
                    submitBox(
                            pose,
                            vertices,
                            0.455F,
                            stemMinY,
                            0.455F,
                            0.545F,
                            stemMaxY,
                            0.545F,
                            0xFF5D626A
                    );
                }
        );
    }

    private static void submitMirrorCube(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        int floor = 70;
        int boost = Math.round(150.0F * state.getNormalizedDimmer());

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (pose, vertices) -> {
                    float tile = (BODY_MAX - BODY_MIN) / MIRROR_TILES;

                    for (int row = 0; row < MIRROR_TILES; row++) {
                        for (int column = 0; column < MIRROR_TILES; column++) {
                            int checker = (row + column) & 1;
                            int brightness = Math.min(
                                    255,
                                    floor + boost + checker * 32
                            );
                            int color = 0xFF000000
                                    | brightness << 16
                                    | brightness << 8
                                    | Math.min(255, brightness + 10);
                            float a = BODY_MIN + column * tile;
                            float b = a + tile;
                            float c = BODY_MIN + row * tile;
                            float d = c + tile;

                            addQuad(pose, vertices,
                                    a, d, BODY_MIN,
                                    a, c, BODY_MIN,
                                    b, c, BODY_MIN,
                                    b, d, BODY_MIN,
                                    color);
                            addQuad(pose, vertices,
                                    b, d, BODY_MAX,
                                    b, c, BODY_MAX,
                                    a, c, BODY_MAX,
                                    a, d, BODY_MAX,
                                    color);
                            addQuad(pose, vertices,
                                    BODY_MIN, d, b,
                                    BODY_MIN, c, b,
                                    BODY_MIN, c, a,
                                    BODY_MIN, d, a,
                                    color);
                            addQuad(pose, vertices,
                                    BODY_MAX, d, a,
                                    BODY_MAX, c, a,
                                    BODY_MAX, c, b,
                                    BODY_MAX, d, b,
                                    color);
                            addQuad(pose, vertices,
                                    a, BODY_MAX, c,
                                    a, BODY_MAX, d,
                                    b, BODY_MAX, d,
                                    b, BODY_MAX, c,
                                    color);
                            addQuad(pose, vertices,
                                    a, BODY_MIN, d,
                                    a, BODY_MIN, c,
                                    b, BODY_MIN, c,
                                    b, BODY_MIN, d,
                                    color);
                        }
                    }
                }
        );
    }

    private static void submitBeams(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        if (state.getDimmer() <= 0) {
            return;
        }

        for (int beam = 0; beam < BEAMS_PER_RING; beam++) {
            submitBeam(
                    state,
                    matrices,
                    queue,
                    BEAM_COLORS[beam],
                    beam * RING_STEP_DEGREES,
                    0.0F
            );
        }

        float openSideTilt =
                state.isDiscoBaseOnTop()
                        ? -DIAGONAL_TILT_DEGREES
                        : DIAGONAL_TILT_DEGREES;

        for (int beam = 0; beam < BEAMS_PER_RING; beam++) {
            submitBeam(
                    state,
                    matrices,
                    queue,
                    BEAM_COLORS[(beam + 3) % BEAM_COLORS.length],
                    DIAGONAL_OFFSET_DEGREES
                            + beam * RING_STEP_DEGREES,
                    openSideTilt
            );
        }

        submitBeam(
                state,
                matrices,
                queue,
                BEAM_COLORS[2],
                0.0F,
                state.isDiscoBaseOnTop() ? -90.0F : 90.0F
        );
    }

    private static void submitBeam(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int packedRgb,
            float yRotation,
            float xRotation
    ) {
        int alpha = Math.max(
                4,
                Math.round(220.0F * state.getNormalizedDimmer())
        );

        matrices.pushPose();
        matrices.translate(BODY_CENTER, BODY_CENTER, BODY_CENTER);
        matrices.mulPose(Axis.YP.rotationDegrees(yRotation));
        matrices.mulPose(Axis.XP.rotationDegrees(xRotation));

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (pose, vertices) -> submitBeamPrism(
                        pose,
                        vertices,
                        packedRgb,
                        alpha
                )
        );
        matrices.popPose();
    }

    private static void submitBeamPrism(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            int packedRgb,
            int nearAlpha
    ) {
        float nearZ = -BODY_MAX + BODY_CENTER;
        float farZ = nearZ - BEAM_LENGTH;
        int farAlpha = Math.max(1, nearAlpha / 5);
        int red = packedRgb >> 16 & 0xFF;
        int green = packedRgb >> 8 & 0xFF;
        int blue = packedRgb & 0xFF;
        float n = BEAM_START_HALF_WIDTH;
        float f = BEAM_END_HALF_WIDTH;

        addBeamQuad(pose, vertices,
                -n, n, nearZ, -n, -n, nearZ,
                -f, -f, farZ, -f, f, farZ,
                red, green, blue, nearAlpha, farAlpha);
        addBeamQuad(pose, vertices,
                n, -n, nearZ, n, n, nearZ,
                f, f, farZ, f, -f, farZ,
                red, green, blue, nearAlpha, farAlpha);
        addBeamQuad(pose, vertices,
                n, n, nearZ, -n, n, nearZ,
                -f, f, farZ, f, f, farZ,
                red, green, blue, nearAlpha, farAlpha);
        addBeamQuad(pose, vertices,
                -n, -n, nearZ, n, -n, nearZ,
                f, -f, farZ, -f, -f, farZ,
                red, green, blue, nearAlpha, farAlpha);
    }

    private static void addBeamQuad(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            int red, int green, int blue,
            int nearAlpha, int farAlpha
    ) {
        vertices.addVertex(pose, x1, y1, z1)
                .setColor(red, green, blue, nearAlpha);
        vertices.addVertex(pose, x2, y2, z2)
                .setColor(red, green, blue, nearAlpha);
        vertices.addVertex(pose, x3, y3, z3)
                .setColor(red, green, blue, farAlpha);
        vertices.addVertex(pose, x4, y4, z4)
                .setColor(red, green, blue, farAlpha);
    }

    private static void submitBox(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            int color
    ) {
        addQuad(pose, vertices,
                minX, maxY, minZ, minX, minY, minZ,
                maxX, minY, minZ, maxX, maxY, minZ, color);
        addQuad(pose, vertices,
                maxX, maxY, maxZ, maxX, minY, maxZ,
                minX, minY, maxZ, minX, maxY, maxZ, color);
        addQuad(pose, vertices,
                minX, maxY, maxZ, minX, minY, maxZ,
                minX, minY, minZ, minX, maxY, minZ, color);
        addQuad(pose, vertices,
                maxX, maxY, minZ, maxX, minY, minZ,
                maxX, minY, maxZ, maxX, maxY, maxZ, color);
        addQuad(pose, vertices,
                minX, maxY, minZ, maxX, maxY, minZ,
                maxX, maxY, maxZ, minX, maxY, maxZ, color);
        addQuad(pose, vertices,
                minX, minY, maxZ, maxX, minY, maxZ,
                maxX, minY, minZ, minX, minY, minZ, color);
    }

    private static void addQuad(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            int color
    ) {
        vertices.addVertex(pose, x1, y1, z1).setColor(color);
        vertices.addVertex(pose, x2, y2, z2).setColor(color);
        vertices.addVertex(pose, x3, y3, z3).setColor(color);
        vertices.addVertex(pose, x4, y4, z4).setColor(color);
    }
}
