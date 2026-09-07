package dmx.lighting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dmx.lighting.DmxLighting;
import dmx.lighting.DmxPixelBlockEntity;
import dmx.lighting.client.DmxPixelBlockRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

/**
 * Draws a full-bright, RGB-tinted copy of the DMX Block texture.
 *
 * This is a visual-only emissive surface. The block itself continues to
 * report a Minecraft light level of zero, so nearby blocks are not lit.
 */
public final class DmxPixelBlockEntityRenderer
        implements BlockEntityRenderer<
                DmxPixelBlockEntity,
                DmxPixelBlockRenderState
        > {

    private static final Identifier[] TEXTURES = {
            DmxLighting.id("textures/block/dmx_pixel_block1.png"),
            DmxLighting.id("textures/block/dmx_pixel_block2.png"),
            DmxLighting.id("textures/block/dmx_pixel_block3.png"),
            DmxLighting.id("textures/block/dmx_pixel_block4.png"),
            DmxLighting.id("textures/block/dmx_pixel_block5.png")
    };

    private static final int FULL_BRIGHT =
            0x00F000F0;

    /*
     * Places the glow skin just outside the baked block model to prevent
     * the two surfaces from flickering over one another.
     */
    private static final float MIN =
            -0.001F;

    private static final float MAX =
            1.001F;

    public DmxPixelBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public DmxPixelBlockRenderState createRenderState() {
        return new DmxPixelBlockRenderState();
    }

    @Override
    public void extractRenderState(
            DmxPixelBlockEntity blockEntity,
            DmxPixelBlockRenderState state,
            float tickProgress,
            Vec3 cameraPosition,
            @Nullable
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity,
                state,
                tickProgress,
                cameraPosition,
                crumblingOverlay
        );

        state.setPackedRgb(
                blockEntity.getVisiblePackedRgb()
        );

        state.setSkin(
                blockEntity.getRenderedSkin()
        );
    }

    @Override
    public void submit(
            DmxPixelBlockRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        int packedRgb =
                state.getPackedRgb();

        if (packedRgb == 0) {
            return;
        }

        int color =
                0xFF000000 | packedRgb;

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.entityTranslucentEmissive(
                        TEXTURES[
                                state.getSkin() - 1
                        ]
                ),
                (pose, vertices) -> submitCube(
                        pose,
                        vertices,
                        color
                )
        );
    }

    private static void submitCube(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            int color
    ) {
        /* North (-Z). */
        addVertex(pose, vertices, MIN, MAX, MIN, 0, 0, color, 0, 0, -1);
        addVertex(pose, vertices, MIN, MIN, MIN, 0, 1, color, 0, 0, -1);
        addVertex(pose, vertices, MAX, MIN, MIN, 1, 1, color, 0, 0, -1);
        addVertex(pose, vertices, MAX, MAX, MIN, 1, 0, color, 0, 0, -1);

        /* South (+Z). */
        addVertex(pose, vertices, MAX, MAX, MAX, 0, 0, color, 0, 0, 1);
        addVertex(pose, vertices, MAX, MIN, MAX, 0, 1, color, 0, 0, 1);
        addVertex(pose, vertices, MIN, MIN, MAX, 1, 1, color, 0, 0, 1);
        addVertex(pose, vertices, MIN, MAX, MAX, 1, 0, color, 0, 0, 1);

        /* West (-X). */
        addVertex(pose, vertices, MIN, MAX, MAX, 0, 0, color, -1, 0, 0);
        addVertex(pose, vertices, MIN, MIN, MAX, 0, 1, color, -1, 0, 0);
        addVertex(pose, vertices, MIN, MIN, MIN, 1, 1, color, -1, 0, 0);
        addVertex(pose, vertices, MIN, MAX, MIN, 1, 0, color, -1, 0, 0);

        /* East (+X). */
        addVertex(pose, vertices, MAX, MAX, MIN, 0, 0, color, 1, 0, 0);
        addVertex(pose, vertices, MAX, MIN, MIN, 0, 1, color, 1, 0, 0);
        addVertex(pose, vertices, MAX, MIN, MAX, 1, 1, color, 1, 0, 0);
        addVertex(pose, vertices, MAX, MAX, MAX, 1, 0, color, 1, 0, 0);

        /* Top (+Y). */
        addVertex(pose, vertices, MIN, MAX, MIN, 0, 0, color, 0, 1, 0);
        addVertex(pose, vertices, MAX, MAX, MIN, 1, 0, color, 0, 1, 0);
        addVertex(pose, vertices, MAX, MAX, MAX, 1, 1, color, 0, 1, 0);
        addVertex(pose, vertices, MIN, MAX, MAX, 0, 1, color, 0, 1, 0);

        /* Bottom (-Y). */
        addVertex(pose, vertices, MIN, MIN, MAX, 0, 0, color, 0, -1, 0);
        addVertex(pose, vertices, MAX, MIN, MAX, 1, 0, color, 0, -1, 0);
        addVertex(pose, vertices, MAX, MIN, MIN, 1, 1, color, 0, -1, 0);
        addVertex(pose, vertices, MIN, MIN, MIN, 0, 1, color, 0, -1, 0);
    }

    private static void addVertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            float normalX,
            float normalY,
            float normalZ
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
                normalX,
                normalY,
                normalZ
        );
    }
}
