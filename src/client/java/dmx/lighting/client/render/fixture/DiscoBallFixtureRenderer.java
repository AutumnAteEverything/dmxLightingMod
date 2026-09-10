package dmx.lighting.client.render.fixture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dmx.lighting.DmxDiscoBallBlockEntity;
import dmx.lighting.DmxDiscoBallEffectMode;
import dmx.lighting.client.DmxFixtureRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Renders a stationary mount with beam and projected-dot effects. */
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
    private static final int SHALLOW_BEAM_COUNT = 4;
    private static final float SHALLOW_BEAM_STEP_DEGREES =
            360.0F / SHALLOW_BEAM_COUNT;
    private static final float SHALLOW_DIAGONAL_TILT_DEGREES = 22.0F;

    private static final int DOT_RAY_COUNT = 64;
    private static final double DOT_RANGE_BLOCKS = 24.0D;
    private static final double DOT_RAY_START_BLOCKS = 0.90D;
    private static final double GOLDEN_ANGLE_RADIANS =
            Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final float DOT_HALF_SIZE = 0.075F;
    private static final float DOT_HALF_THICKNESS = 0.006F;
    private static final float DOT_SURFACE_OFFSET = 0.010F;

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

    private final Map<DmxDiscoBallBlockEntity, DotCache> dotCaches =
            new WeakHashMap<>();

    public void extractRenderState(
            DmxDiscoBallBlockEntity fixture,
            DmxFixtureRenderState state
    ) {
        DmxDiscoBallEffectMode effectMode =
                fixture.getResolvedEffectMode();
        state.setDiscoEffectMode(effectMode);

        if (effectMode != DmxDiscoBallEffectMode.DOTS
                || state.getDimmer() <= 0
                || fixture.getLevel() == null) {
            state.clearDiscoSpots();
            return;
        }

        Level level = fixture.getLevel();
        long gameTime = level.getGameTime();
        DotCache cached = dotCaches.get(fixture);

        if (cached != null && cached.gameTime() == gameTime) {
            state.setDiscoSpots(cached.spots());
            return;
        }

        List<DmxFixtureRenderState.DiscoSpot> spots =
                projectDots(
                        fixture,
                        state.getDiscoRotationDegrees()
                );

        dotCaches.put(
                fixture,
                new DotCache(gameTime, spots)
        );
        state.setDiscoSpots(spots);
    }

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
        if (state.getDiscoEffectMode()
                == DmxDiscoBallEffectMode.BEAMS) {
            submitBeams(state, matrices, queue);
        }
        matrices.popPose();

        if (state.getDiscoEffectMode()
                == DmxDiscoBallEffectMode.DOTS) {
            submitDots(state, matrices, queue);
        }
    }

    private static List<DmxFixtureRenderState.DiscoSpot> projectDots(
            DmxDiscoBallBlockEntity fixture,
            float rotationDegrees
    ) {
        Level level = fixture.getLevel();
        if (level == null) {
            return List.of();
        }

        Vec3 origin = Vec3.atCenterOf(fixture.getBlockPos());
        Vec3 blockOrigin = Vec3.atLowerCornerOf(fixture.getBlockPos());
        double rotationRadians = Math.toRadians(rotationDegrees);
        List<DmxFixtureRenderState.DiscoSpot> spots =
                new ArrayList<>(DOT_RAY_COUNT);

        for (int ray = 0; ray < DOT_RAY_COUNT; ray++) {
            double vertical = 1.0D
                    - 2.0D * (ray + 0.5D) / DOT_RAY_COUNT;
            double horizontal = Math.sqrt(
                    Math.max(0.0D, 1.0D - vertical * vertical)
            );
            double azimuth = ray * GOLDEN_ANGLE_RADIANS
                    + rotationRadians;
            Vec3 direction = new Vec3(
                    -Math.sin(azimuth) * horizontal,
                    vertical,
                    -Math.cos(azimuth) * horizontal
            );
            Vec3 start = origin.add(
                    direction.scale(DOT_RAY_START_BLOCKS)
            );
            Vec3 end = origin.add(
                    direction.scale(DOT_RANGE_BLOCKS)
            );

            BlockHitResult hit = level.clip(
                    new ClipContext(
                            start,
                            end,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            CollisionContext.empty()
                    )
            );

            if (hit.getType() != HitResult.Type.BLOCK) {
                continue;
            }

            Vec3 relative = hit.getLocation().subtract(blockOrigin);
            spots.add(
                    new DmxFixtureRenderState.DiscoSpot(
                            (float) relative.x,
                            (float) relative.y,
                            (float) relative.z,
                            hit.getDirection(),
                            BEAM_COLORS[ray % BEAM_COLORS.length]
                    )
            );
        }

        return List.copyOf(spots);
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

        float shallowOpenSideTilt =
                state.isDiscoBaseOnTop()
                        ? -SHALLOW_DIAGONAL_TILT_DEGREES
                        : SHALLOW_DIAGONAL_TILT_DEGREES;

        for (int beam = 0; beam < SHALLOW_BEAM_COUNT; beam++) {
            submitBeam(
                    state,
                    matrices,
                    queue,
                    BEAM_COLORS[(beam * 2 + 1) % BEAM_COLORS.length],
                    beam * SHALLOW_BEAM_STEP_DEGREES,
                    shallowOpenSideTilt
            );
        }
    }

    private static void submitDots(
            DmxFixtureRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue
    ) {
        if (state.getDimmer() <= 0 || state.getDiscoSpots().isEmpty()) {
            return;
        }

        int alpha = Math.max(
                8,
                Math.round(255.0F * state.getNormalizedDimmer())
        );

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.debugQuads(),
                (pose, vertices) -> {
                    for (DmxFixtureRenderState.DiscoSpot spot
                            : state.getDiscoSpots()) {
                        submitDot(pose, vertices, spot, alpha);
                    }
                }
        );
    }

    private static void submitDot(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            DmxFixtureRenderState.DiscoSpot spot,
            int alpha
    ) {
        Direction face = spot.face();
        float x = spot.x() + face.getStepX() * DOT_SURFACE_OFFSET;
        float y = spot.y() + face.getStepY() * DOT_SURFACE_OFFSET;
        float z = spot.z() + face.getStepZ() * DOT_SURFACE_OFFSET;
        float halfX = face.getAxis() == Direction.Axis.X
                ? DOT_HALF_THICKNESS
                : DOT_HALF_SIZE;
        float halfY = face.getAxis() == Direction.Axis.Y
                ? DOT_HALF_THICKNESS
                : DOT_HALF_SIZE;
        float halfZ = face.getAxis() == Direction.Axis.Z
                ? DOT_HALF_THICKNESS
                : DOT_HALF_SIZE;
        int color = (alpha << 24)
                | (spot.packedRgb() & 0x00FFFFFF);

        submitBox(
                pose,
                vertices,
                x - halfX,
                y - halfY,
                z - halfZ,
                x + halfX,
                y + halfY,
                z + halfZ,
                color
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

    private record DotCache(
            long gameTime,
            List<DmxFixtureRenderState.DiscoSpot> spots
    ) {
    }
}
