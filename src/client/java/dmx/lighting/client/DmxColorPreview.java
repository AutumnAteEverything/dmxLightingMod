package dmx.lighting.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Draws a preview of an RGB color after applying a DMX-style dimmer.
 *
 * The numeric text is drawn below the colored rectangle rather than
 * directly over it. This avoids blurry or low-contrast text when the
 * preview color changes.
 */
public final class DmxColorPreview {

    private static final int BORDER_COLOR = 0xFF202020;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private DmxColorPreview() {
        // Utility class: do not instantiate.
    }

    /**
     * Draws a color swatch and the resulting RGB values.
     *
     * The supplied height includes both the swatch and its text label.
     */
    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            int centerX,
            int topY,
            int width,
            int height,
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        int outputRed = applyDimmer(red, dimmer);
        int outputGreen = applyDimmer(green, dimmer);
        int outputBlue = applyDimmer(blue, dimmer);

        int previewColor =
                0xFF000000
                        | (outputRed << 16)
                        | (outputGreen << 8)
                        | outputBlue;

        int left = centerX - width / 2;
        int right = left + width;

        /*
         * Reserve the bottom 11 pixels for the text label.
         */
        int swatchHeight = Math.max(12, height - 11);
        int swatchBottom = topY + swatchHeight;

        graphics.fill(
                left,
                topY,
                right,
                swatchBottom,
                BORDER_COLOR
        );

        graphics.fill(
                left + 2,
                topY + 2,
                right - 2,
                swatchBottom - 2,
                previewColor
        );

        graphics.centeredText(
                font,
                Component.literal(
                        "RGB "
                                + outputRed
                                + ", "
                                + outputGreen
                                + ", "
                                + outputBlue
                                + "   D "
                                + clamp(dimmer)
                ),
                centerX,
                swatchBottom + 2,
                TEXT_COLOR
        );
    }

    /**
     * Applies a 0-255 master dimmer to one 0-255 color channel.
     */
    public static int applyDimmer(
            int channel,
            int dimmer
    ) {
        int safeChannel = clamp(channel);
        int safeDimmer = clamp(dimmer);

        return Math.round(
                safeChannel * (safeDimmer / 255.0F)
        );
    }

    /**
     * Packs the dimmed result as 0xRRGGBB.
     */
    public static int getDimmedRgb(
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        int outputRed = applyDimmer(red, dimmer);
        int outputGreen = applyDimmer(green, dimmer);
        int outputBlue = applyDimmer(blue, dimmer);

        return (outputRed << 16)
                | (outputGreen << 8)
                | outputBlue;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}