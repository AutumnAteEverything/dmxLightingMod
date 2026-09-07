package dmx.lighting.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Draws consistent section headings in the DMX fixture editor.
 */
public final class DmxSectionRenderer {

    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int LINE_COLOR = 0xFF555555;

    private DmxSectionRenderer() {
        // Utility class: do not instantiate.
    }

    /**
     * Draws a centered section label with horizontal divider lines.
     */
    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            Component title,
            int centerX,
            int y,
            int totalWidth
    ) {
        int titleWidth = font.width(title);
        int gap = 6;

        int left = centerX - totalWidth / 2;
        int right = centerX + totalWidth / 2;

        int titleLeft = centerX - titleWidth / 2;
        int titleRight = centerX + titleWidth / 2;

        int lineY = y + 4;

        if (titleLeft - gap > left) {
            graphics.fill(
                    left,
                    lineY,
                    titleLeft - gap,
                    lineY + 1,
                    LINE_COLOR
            );
        }

        if (titleRight + gap < right) {
            graphics.fill(
                    titleRight + gap,
                    lineY,
                    right,
                    lineY + 1,
                    LINE_COLOR
            );
        }

        graphics.centeredText(
                font,
                title,
                centerX,
                y,
                TITLE_COLOR
        );
    }

    /**
     * Convenience overload for ordinary text.
     */
    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            String title,
            int centerX,
            int y,
            int totalWidth
    ) {
        render(
                graphics,
                font,
                Component.literal(title),
                centerX,
                y,
                totalWidth
        );
    }
}