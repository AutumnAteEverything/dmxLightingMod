package dmx.lighting.client.render;

/**
 * Shared dimensions used by all rendered DMX fixture bodies.
 *
 * Measurements are expressed in block-space units:
 *
 *     1.0F = one Minecraft block
 *
 * The fixture renderer positions its geometry around the center of the
 * fixture block. Keeping dimensions here prevents individual fixture
 * renderers from accumulating unrelated hard-coded measurements.
 *
 * These values describe the first prototype models. They can be refined
 * later without changing renderer logic.
 */
public final class FixtureDimensions {

    /*
     * -----------------------------------------------------------------
     * General
     * -----------------------------------------------------------------
     */

    /**
     * Center of a Minecraft block on each axis.
     */
    public static final float BLOCK_CENTER = 0.5F;

    /**
     * Small separation used to prevent overlapping surfaces from
     * flickering.
     */
    public static final float SURFACE_OFFSET = 0.002F;

    /**
     * General thickness for small decorative fixture parts.
     */
    public static final float DETAIL_THICKNESS = 0.025F;

    /*
     * -----------------------------------------------------------------
     * Mounting base
     * -----------------------------------------------------------------
     */

    public static final float BASE_WIDTH = 0.54F;
    public static final float BASE_HEIGHT = 0.16F;
    public static final float BASE_DEPTH = 0.54F;

    public static final float BASE_X =
            BLOCK_CENTER - BASE_WIDTH / 2.0F;

    public static final float BASE_Y = 0.0F;

    public static final float BASE_Z =
            BLOCK_CENTER - BASE_DEPTH / 2.0F;

    /*
     * -----------------------------------------------------------------
     * PAR fixture
     * -----------------------------------------------------------------
     */

    /**
     * Main PAR housing.
     */
    public static final float PAR_BODY_WIDTH = 0.54F;
    public static final float PAR_BODY_HEIGHT = 0.54F;
    public static final float PAR_BODY_DEPTH = 0.38F;

    /**
     * Position of the PAR body above its mounting base.
     */
    public static final float PAR_BODY_X =
            BLOCK_CENTER - PAR_BODY_WIDTH / 2.0F;

    public static final float PAR_BODY_Y = 0.24F;

    public static final float PAR_BODY_Z =
            BLOCK_CENTER - PAR_BODY_DEPTH / 2.0F;

    /**
     * Colored front lens.
     */
    public static final float PAR_LENS_WIDTH = 0.43F;
    public static final float PAR_LENS_HEIGHT = 0.43F;
    public static final float PAR_LENS_DEPTH = 0.035F;

    public static final float PAR_LENS_X =
            BLOCK_CENTER - PAR_LENS_WIDTH / 2.0F;

    public static final float PAR_LENS_Y =
            PAR_BODY_Y
                    + (PAR_BODY_HEIGHT - PAR_LENS_HEIGHT) / 2.0F;

    /**
     * The initial fixture faces toward negative Z.
     */
    public static final float PAR_LENS_Z =
            PAR_BODY_Z
                    - PAR_LENS_DEPTH
                    - SURFACE_OFFSET;

    /**
     * Small mounting neck between the PAR body and base.
     */
    public static final float PAR_NECK_WIDTH = 0.14F;
    public static final float PAR_NECK_HEIGHT = 0.12F;
    public static final float PAR_NECK_DEPTH = 0.14F;

    public static final float PAR_NECK_X =
            BLOCK_CENTER - PAR_NECK_WIDTH / 2.0F;

    public static final float PAR_NECK_Y =
            BASE_HEIGHT;

    public static final float PAR_NECK_Z =
            BLOCK_CENTER - PAR_NECK_DEPTH / 2.0F;

    /*
     * -----------------------------------------------------------------
     * Static spotlight
     * -----------------------------------------------------------------
     */

    /**
     * Long spotlight barrel.
     */
    public static final float SPOT_BODY_WIDTH = 0.44F;
    public static final float SPOT_BODY_HEIGHT = 0.44F;
    public static final float SPOT_BODY_DEPTH = 0.68F;

    public static final float SPOT_BODY_X =
            BLOCK_CENTER - SPOT_BODY_WIDTH / 2.0F;

    public static final float SPOT_BODY_Y = 0.28F;

    public static final float SPOT_BODY_Z =
            BLOCK_CENTER - SPOT_BODY_DEPTH / 2.0F;

    /**
     * Spotlight front lens.
     */
    public static final float SPOT_LENS_WIDTH = 0.35F;
    public static final float SPOT_LENS_HEIGHT = 0.35F;
    public static final float SPOT_LENS_DEPTH = 0.04F;

    public static final float SPOT_LENS_X =
            BLOCK_CENTER - SPOT_LENS_WIDTH / 2.0F;

    public static final float SPOT_LENS_Y =
            SPOT_BODY_Y
                    + (SPOT_BODY_HEIGHT - SPOT_LENS_HEIGHT) / 2.0F;

    public static final float SPOT_LENS_Z =
            SPOT_BODY_Z
                    - SPOT_LENS_DEPTH
                    - SURFACE_OFFSET;

    /**
     * Rear section of the spotlight housing.
     */
    public static final float SPOT_REAR_WIDTH = 0.34F;
    public static final float SPOT_REAR_HEIGHT = 0.34F;
    public static final float SPOT_REAR_DEPTH = 0.14F;

    public static final float SPOT_REAR_X =
            BLOCK_CENTER - SPOT_REAR_WIDTH / 2.0F;

    public static final float SPOT_REAR_Y =
            SPOT_BODY_Y
                    + (SPOT_BODY_HEIGHT - SPOT_REAR_HEIGHT) / 2.0F;

    public static final float SPOT_REAR_Z =
            SPOT_BODY_Z
                    + SPOT_BODY_DEPTH;

    /*
     * -----------------------------------------------------------------
     * Moving-head fixture
     * -----------------------------------------------------------------
     */

    /**
     * Moving-head pedestal.
     */
    public static final float MOVING_BASE_WIDTH = 0.58F;
    public static final float MOVING_BASE_HEIGHT = 0.20F;
    public static final float MOVING_BASE_DEPTH = 0.58F;

    public static final float MOVING_BASE_X =
            BLOCK_CENTER - MOVING_BASE_WIDTH / 2.0F;

    public static final float MOVING_BASE_Y = 0.0F;

    public static final float MOVING_BASE_Z =
            BLOCK_CENTER - MOVING_BASE_DEPTH / 2.0F;

    /**
     * Central rotating section above the pedestal.
     */
    public static final float MOVING_TURNTABLE_WIDTH = 0.40F;
    public static final float MOVING_TURNTABLE_HEIGHT = 0.10F;
    public static final float MOVING_TURNTABLE_DEPTH = 0.40F;

    public static final float MOVING_TURNTABLE_X =
            BLOCK_CENTER - MOVING_TURNTABLE_WIDTH / 2.0F;

    public static final float MOVING_TURNTABLE_Y =
            MOVING_BASE_HEIGHT;

    public static final float MOVING_TURNTABLE_Z =
            BLOCK_CENTER - MOVING_TURNTABLE_DEPTH / 2.0F;

    /**
     * Moving-head yoke arms.
     */
    public static final float MOVING_YOKE_ARM_WIDTH = 0.10F;
    public static final float MOVING_YOKE_ARM_HEIGHT = 0.48F;
    public static final float MOVING_YOKE_ARM_DEPTH = 0.14F;

    public static final float MOVING_YOKE_GAP = 0.38F;

    public static final float MOVING_YOKE_LEFT_X =
            BLOCK_CENTER
                    - MOVING_YOKE_GAP / 2.0F
                    - MOVING_YOKE_ARM_WIDTH;

    public static final float MOVING_YOKE_RIGHT_X =
            BLOCK_CENTER
                    + MOVING_YOKE_GAP / 2.0F;

    public static final float MOVING_YOKE_Y =
            MOVING_BASE_HEIGHT
                    + MOVING_TURNTABLE_HEIGHT;

    public static final float MOVING_YOKE_Z =
            BLOCK_CENTER - MOVING_YOKE_ARM_DEPTH / 2.0F;

    /**
     * Crossbar joining the two yoke arms.
     */
    public static final float MOVING_YOKE_CROSSBAR_WIDTH =
            MOVING_YOKE_GAP
                    + MOVING_YOKE_ARM_WIDTH * 2.0F;

    public static final float MOVING_YOKE_CROSSBAR_HEIGHT = 0.09F;
    public static final float MOVING_YOKE_CROSSBAR_DEPTH = 0.14F;

    public static final float MOVING_YOKE_CROSSBAR_X =
            BLOCK_CENTER
                    - MOVING_YOKE_CROSSBAR_WIDTH / 2.0F;

    public static final float MOVING_YOKE_CROSSBAR_Y =
            MOVING_YOKE_Y
                    + MOVING_YOKE_ARM_HEIGHT
                    - MOVING_YOKE_CROSSBAR_HEIGHT;

    public static final float MOVING_YOKE_CROSSBAR_Z =
            BLOCK_CENTER
                    - MOVING_YOKE_CROSSBAR_DEPTH / 2.0F;

    /**
     * Tilting lamp head.
     */
    public static final float MOVING_HEAD_WIDTH = 0.34F;
    public static final float MOVING_HEAD_HEIGHT = 0.34F;
    public static final float MOVING_HEAD_DEPTH = 0.46F;

    /**
     * Pivot around which the head tilts.
     */
    public static final float MOVING_HEAD_PIVOT_X =
            BLOCK_CENTER;

    public static final float MOVING_HEAD_PIVOT_Y =
            MOVING_YOKE_Y
                    + MOVING_YOKE_ARM_HEIGHT * 0.58F;

    public static final float MOVING_HEAD_PIVOT_Z =
            BLOCK_CENTER;

    /**
     * Head coordinates relative to its tilt pivot.
     */
    public static final float MOVING_HEAD_LOCAL_X =
            -MOVING_HEAD_WIDTH / 2.0F;

    public static final float MOVING_HEAD_LOCAL_Y =
            -MOVING_HEAD_HEIGHT / 2.0F;

    public static final float MOVING_HEAD_LOCAL_Z =
            -MOVING_HEAD_DEPTH / 2.0F;

    /**
     * Moving-head lens, positioned at the negative-Z end of the head.
     */
    public static final float MOVING_LENS_WIDTH = 0.26F;
    public static final float MOVING_LENS_HEIGHT = 0.26F;
    public static final float MOVING_LENS_DEPTH = 0.035F;

    public static final float MOVING_LENS_LOCAL_X =
            -MOVING_LENS_WIDTH / 2.0F;

    public static final float MOVING_LENS_LOCAL_Y =
            -MOVING_LENS_HEIGHT / 2.0F;

    public static final float MOVING_LENS_LOCAL_Z =
            -MOVING_HEAD_DEPTH / 2.0F
                    - MOVING_LENS_DEPTH
                    - SURFACE_OFFSET;

    /*
     * -----------------------------------------------------------------
     * Future beam defaults
     * -----------------------------------------------------------------
     */

    /**
     * Initial beam length in blocks.
     */
    public static final float DEFAULT_BEAM_LENGTH = 12.0F;

    /**
     * Radius of the narrowest provisional beam.
     */
    public static final float MINIMUM_BEAM_RADIUS = 0.045F;

    /**
     * Default lens-plane distance used to calculate the widening cone.
     */
    public static final float BEAM_START_OFFSET = 0.025F;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    private FixtureDimensions() {
        /*
         * Utility class.
         */
    }
}