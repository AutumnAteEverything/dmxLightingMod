package dmx.lighting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dmx.lighting.DmxLighting;
import dmx.lighting.DmxPixelBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Full-bright DMX Block geometry for display entities. */
final class DmxPixelCubeRenderer {

    private static final Identifier BASE_TEXTURE =
            DmxLighting.id("textures/block/dmx_pixel_block.png");

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

        // Cutout surfaces write depth, keeping the expanded skin above the
        // colored base even when Minecraft batches the two textures.
        queue.submitCustomGeometry(
                matrices,
                RenderTypes.entityCutout(BASE_TEXTURE),
                (pose, vertices) -> submitCube(
                        pose,
                        vertices,
                        color,
                        0.0F,
                        1.0F
                )
        );

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.entityCutout(
                        TEXTURES[safeSkin - 1]
                ),
                (pose, vertices) -> submitCube(
                        pose,
                        vertices,
                        color,
                        MIN,
                        MAX
                )
        );
    }

    private static void submitCube(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            int color,
            float min,
            float max
    ) {
        addVertex(pose, vertices, min, max, min, 0, 0, color, 0, 0, -1);
        addVertex(pose, vertices, min, min, min, 0, 1, color, 0, 0, -1);
        addVertex(pose, vertices, max, min, min, 1, 1, color, 0, 0, -1);
        addVertex(pose, vertices, max, max, min, 1, 0, color, 0, 0, -1);

        addVertex(pose, vertices, max, max, max, 0, 0, color, 0, 0, 1);
        addVertex(pose, vertices, max, min, max, 0, 1, color, 0, 0, 1);
        addVertex(pose, vertices, min, min, max, 1, 1, color, 0, 0, 1);
        addVertex(pose, vertices, min, max, max, 1, 0, color, 0, 0, 1);

        addVertex(pose, vertices, min, max, max, 0, 0, color, -1, 0, 0);
        addVertex(pose, vertices, min, min, max, 0, 1, color, -1, 0, 0);
        addVertex(pose, vertices, min, min, min, 1, 1, color, -1, 0, 0);
        addVertex(pose, vertices, min, max, min, 1, 0, color, -1, 0, 0);

        addVertex(pose, vertices, max, max, min, 0, 0, color, 1, 0, 0);
        addVertex(pose, vertices, max, min, min, 0, 1, color, 1, 0, 0);
        addVertex(pose, vertices, max, min, max, 1, 1, color, 1, 0, 0);
        addVertex(pose, vertices, max, max, max, 1, 0, color, 1, 0, 0);

        addVertex(pose, vertices, min, max, min, 0, 0, color, 0, 1, 0);
        addVertex(pose, vertices, max, max, min, 1, 0, color, 0, 1, 0);
        addVertex(pose, vertices, max, max, max, 1, 1, color, 0, 1, 0);
        addVertex(pose, vertices, min, max, max, 0, 1, color, 0, 1, 0);

        addVertex(pose, vertices, min, min, max, 0, 0, color, 0, -1, 0);
        addVertex(pose, vertices, max, min, max, 1, 0, color, 0, -1, 0);
        addVertex(pose, vertices, max, min, min, 1, 1, color, 0, -1, 0);
        addVertex(pose, vertices, min, min, min, 0, 1, color, 0, -1, 0);
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
