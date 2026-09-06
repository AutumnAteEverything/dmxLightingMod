package dmx.lighting.client.render;

import dmx.lighting.client.DmxFixtureRenderState;

/**
 * Central color calculations for rendered DMX fixtures.
 *
 * Keeping these calculations outside individual fixture renderers means
 * PARs, spotlights, moving heads, and future fixture types all respond
 * consistently to:
 *
 * - RGB output
 * - White output
 * - Dimmer
 * - Blackout
 * - Strobe
 * - Lens illumination
 *
 * Colors returned by this class use the 0xAARRGGBB format.
 */
public final class FixtureRenderColors {

    /*
     * -----------------------------------------------------------------
     * Housing colors
     * -----------------------------------------------------------------
     */

    /**
     * Main fixture housing.
     */
    private static final int HOUSING_COLOR =
            0xFF202226;

    /**
     * Slightly lighter housing color used for raised sections.
     */
    private static final int HOUSING_HIGHLIGHT_COLOR =
            0xFF34373D;

    /**
     * Dark color for recessed details and joints.
     */
    private static final int HOUSING_SHADOW_COLOR =
            0xFF101114;

    /**
     * Metallic-looking mount and yoke color.
     */
    private static final int MOUNT_COLOR =
            0xFF292C31;

    /**
     * Lens color while the fixture is blacked out.
     */
    private static final int DARK_LENS_COLOR =
            0xFF08090B;

    /**
     * Lens trim color.
     */
    private static final int LENS_RING_COLOR =
            0xFF15171A;

    /*
     * -----------------------------------------------------------------
     * Housing accessors
     * -----------------------------------------------------------------
     */

    public static int housing() {
        return HOUSING_COLOR;
    }

    public static int housingHighlight() {
        return HOUSING_HIGHLIGHT_COLOR;
    }

    public static int housingShadow() {
        return HOUSING_SHADOW_COLOR;
    }

    public static int mount() {
        return MOUNT_COLOR;
    }

    public static int lensRing() {
        return LENS_RING_COLOR;
    }

    /*
     * -----------------------------------------------------------------
     * Lens output
     * -----------------------------------------------------------------
     */

    /**
     * Returns the visible lens color.
     *
     * When the fixture is producing no light, the lens remains nearly
     * black rather than disappearing completely.
     */
    public static int lens(
            DmxFixtureRenderState state
    ) {
        if (state == null
                || !state.isLit()) {

            return DARK_LENS_COLOR;
        }

        return opaque(
                state.getPackedRgb()
        );
    }

    /**
     * Returns a brighter version of the active output for emissive lens
     * rendering.
     *
     * The RGB hue is preserved, but every active color receives a small
     * minimum brightness so saturated colors remain visible.
     */
    public static int emissiveLens(
            DmxFixtureRenderState state
    ) {
        if (state == null
                || !state.isLit()) {

            return DARK_LENS_COLOR;
        }

        int packedRgb =
                state.getPackedRgb();

        int red =
                componentRed(
                        packedRgb
                );

        int green =
                componentGreen(
                        packedRgb
                );

        int blue =
                componentBlue(
                        packedRgb
                );

        red =
                brightenComponent(
                        red
                );

        green =
                brightenComponent(
                        green
                );

        blue =
                brightenComponent(
                        blue
                );

        return argb(
                255,
                red,
                green,
                blue
        );
    }

    /**
     * Returns a subdued version of the active fixture color.
     *
     * This is useful for colored reflections or illuminated housing
     * details that should not be as bright as the primary lens.
     */
    public static int reflectedLight(
            DmxFixtureRenderState state
    ) {
        if (state == null
                || !state.isLit()) {

            return HOUSING_SHADOW_COLOR;
        }

        int packedRgb =
                state.getPackedRgb();

        int red =
                scaleComponent(
                        componentRed(
                                packedRgb
                        ),
                        0.30F
                );

        int green =
                scaleComponent(
                        componentGreen(
                                packedRgb
                        ),
                        0.30F
                );

        int blue =
                scaleComponent(
                        componentBlue(
                                packedRgb
                        ),
                        0.30F
                );

        /*
         * Mix a little of the base housing color into the reflected
         * light so the fixture does not look self-illuminated.
         */
        red =
                Math.max(
                        red,
                        componentRed(
                                HOUSING_COLOR
                        )
                );

        green =
                Math.max(
                        green,
                        componentGreen(
                                HOUSING_COLOR
                        )
                );

        blue =
                Math.max(
                        blue,
                        componentBlue(
                                HOUSING_COLOR
                        )
                );

        return argb(
                255,
                red,
                green,
                blue
        );
    }

    /*
     * -----------------------------------------------------------------
     * Beam colors
     * -----------------------------------------------------------------
     */

    /**
     * Returns a translucent color suitable for the future beam cone.
     *
     * The alpha increases with fixture dimmer level but remains capped
     * because a fully opaque beam would look like a solid object.
     */
    public static int beam(
            DmxFixtureRenderState state
    ) {
        if (state == null
                || !state.isLit()) {

            return 0x00000000;
        }

        int packedRgb =
                state.getPackedRgb();

        int alpha =
                Math.round(
                        24.0F
                                + state.getNormalizedDimmer()
                                * 72.0F
                );

        return argb(
                clampByte(
                        alpha
                ),
                componentRed(
                        packedRgb
                ),
                componentGreen(
                        packedRgb
                ),
                componentBlue(
                        packedRgb
                )
        );
    }

    /**
     * Returns a brighter but more transparent beam center color.
     */
    public static int beamCore(
            DmxFixtureRenderState state
    ) {
        if (state == null
                || !state.isLit()) {

            return 0x00000000;
        }

        int emissive =
                emissiveLens(
                        state
                );

        int alpha =
                Math.round(
                        12.0F
                                + state.getNormalizedDimmer()
                                * 52.0F
                );

        return argb(
                clampByte(
                        alpha
                ),
                componentRed(
                        emissive
                ),
                componentGreen(
                        emissive
                ),
                componentBlue(
                        emissive
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Color operations
     * -----------------------------------------------------------------
     */

    /**
     * Converts 0xRRGGBB to fully opaque 0xAARRGGBB.
     */
    public static int opaque(
            int packedRgb
    ) {
        return 0xFF000000
                | packedRgb
                & 0xFFFFFF;
    }

    /**
     * Creates an ARGB color from individual byte components.
     */
    public static int argb(
            int alpha,
            int red,
            int green,
            int blue
    ) {
        return clampByte(
                alpha
        ) << 24
                | clampByte(
                        red
                ) << 16
                | clampByte(
                        green
                ) << 8
                | clampByte(
                        blue
                );
    }

    public static int componentAlpha(
            int argb
    ) {
        return argb >>> 24
                & 0xFF;
    }

    public static int componentRed(
            int argb
    ) {
        return argb >>> 16
                & 0xFF;
    }

    public static int componentGreen(
            int argb
    ) {
        return argb >>> 8
                & 0xFF;
    }

    public static int componentBlue(
            int argb
    ) {
        return argb
                & 0xFF;
    }

    /**
     * Multiplies the RGB components while retaining the original alpha.
     */
    public static int multiplyBrightness(
            int color,
            float brightness
    ) {
        brightness =
                Math.max(
                        0.0F,
                        brightness
                );

        return argb(
                componentAlpha(
                        color
                ),
                scaleComponent(
                        componentRed(
                                color
                        ),
                        brightness
                ),
                scaleComponent(
                        componentGreen(
                                color
                        ),
                        brightness
                ),
                scaleComponent(
                        componentBlue(
                                color
                        ),
                        brightness
                )
        );
    }

    /**
     * Linearly mixes two ARGB colors.
     */
    public static int mix(
            int first,
            int second,
            float amount
    ) {
        amount =
                clampUnit(
                        amount
                );

        float inverse =
                1.0F - amount;

        return argb(
                Math.round(
                        componentAlpha(
                                first
                        ) * inverse
                                + componentAlpha(
                                        second
                                ) * amount
                ),
                Math.round(
                        componentRed(
                                first
                        ) * inverse
                                + componentRed(
                                        second
                                ) * amount
                ),
                Math.round(
                        componentGreen(
                                first
                        ) * inverse
                                + componentGreen(
                                        second
                                ) * amount
                ),
                Math.round(
                        componentBlue(
                                first
                        ) * inverse
                                + componentBlue(
                                        second
                                ) * amount
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Internal helpers
     * -----------------------------------------------------------------
     */

    /**
     * Raises an active color component toward a readable emissive
     * brightness without converting the color to white.
     */
    private static int brightenComponent(
            int component
    ) {
        if (component <= 0) {
            return 0;
        }

        return clampByte(
                Math.round(
                        72.0F
                                + component
                                * 0.72F
                )
        );
    }

    private static int scaleComponent(
            int component,
            float multiplier
    ) {
        return clampByte(
                Math.round(
                        component
                                * multiplier
                )
        );
    }

    private static int clampByte(
            int value
    ) {
        return Math.max(
                0,
                Math.min(
                        255,
                        value
                )
        );
    }

    private static float clampUnit(
            float value
    ) {
        return Math.max(
                0.0F,
                Math.min(
                        1.0F,
                        value
                )
        );
    }

    private FixtureRenderColors() {
        /*
         * Utility class.
         */
    }
}