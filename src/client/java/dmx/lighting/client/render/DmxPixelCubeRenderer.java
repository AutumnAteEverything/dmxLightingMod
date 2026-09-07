package dmx.lighting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dmx.lighting.DmxLighting;
import dmx.lighting.DmxPixelBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Shared full-bright cube geometry for DMX Blocks and displays. */
final class DmxPixelCubeRenderer {

    private static final Identifier[] TEXTURES = {
            DmxLighting.id("textures/block/dmx_pixel_block1.png"),
            DmxLighting.id("textures/block/dmx_pixel_block2.png"),
            DmxLighting.id("textures/block/dmx_pixel_block3.png"),
            DmxLighting.id("textures/block/dmx_pixel_block4.png"),
            DmxLighting.id("textures/block/dmx_pixel_block5.png")
    };

    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float MIN = -0.001F;
    private static final float MAX = 1.001F;

    private DmxPixelCubeRenderer() {
    }

    static void submit(
            PoseStack matrices,
            SubmitNodeCollector queue,
            int packedRgb,
            int skin
    ) {
        if (packedRgb == 0) {
            return;
        }

        int safeSkin = Math.clamp(
                skin,
                DmxPixelBlockEntity.MIN_SKIN,
                DmxPixelBlockEntity.MAX_SKIN
        );
        int color = 0xFF000000 | packedRgb;

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.entityTranslucentEmissive(
                        TEXTURES[safeSkin - 1]
                ),
                (pose, vertices) -> submitCube(pose, vertices, color)
        );
    }

    private static void submitCube(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            int color
    ) {
        addVertex(pose, vertices, MIN, MAX, MIN, 0, 0, color, 0, 0, -1);
        addVertex(pose, vertices, MIN, MIN, MIN, 0, 1, color, 0, 0, -1);
        addVertex(pose, vertices, MAX, MIN, MIN, 1, 1, color, 0, 0, -1);
        addVertex(pose, vertices, MAX, MAX, MIN, 1, 0, color, 0, 0, -1);

        addVertex(pose, vertices, MAX, MAX, MAX, 0, 0, color, 0, 0, 1);
        addVertex(pose, vertices, MAX, MIN, MAX, 0, 1, color, 0, 0, 1);
        addVertex(pose, vertices, MIN, MIN, MAX, 1, 1, color, 0, 0, 1);
        addVertex(pose, vertices, MIN, MAX, MAX, 1, 0, color, 0, 0, 1);

        addVertex(pose, vertices, MIN, MAX, MAX, 0, 0, color, -1, 0, 0);
        addVertex(pose, vertices, MIN, MIN, MAX, 0, 1, color, -1, 0, 0);
        addVertex(pose, vertices, MIN, MIN, MIN, 1, 1, color, -1, 0, 0);
        addVertex(pose, vertices, MIN, MAX, MIN, 1, 0, color, -1, 0, 0);

        addVertex(pose, vertices, MAX, MAX, MIN, 0, 0, color, 1, 0, 0);
        addVertex(pose, vertices, MAX, MIN, MIN, 0, 1, color, 1, 0, 0);
        addVertex(pose, vertices, MAX, MIN, MAX, 1, 1, color, 1, 0, 0);
        addVertex(pose, vertices, MAX, MAX, MAX, 1, 0, color, 1, 0, 0);

        addVertex(pose, vertices, MIN, MAX, MIN, 0, 0, color, 0, 1, 0);
        addVertex(pose, vertices, MAX, MAX, MIN, 1, 0, color, 0, 1, 0);
        addVertex(pose, vertices, MAX, MAX, MAX, 1, 1, color, 0, 1, 0);
        addVertex(pose, vertices, MIN, MAX, MAX, 0, 1, color, 0, 1, 0);

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
        vertices.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(pose, normalX, normalY, normalZ);
    }
}
