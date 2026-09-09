package dmx.lighting.client;

import dmx.lighting.FixtureBeamSettings;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Per-frame rendering snapshot for one DMX fixture.
 *
 * Mutable block-entity values are copied into this object during
 * extractRenderState(...). Rendering then reads from this snapshot
 * rather than accessing the block entity directly.
 *
 * Stored information includes:
 *
 * - Fixture profile identity
 * - Active fixture channel values
 * - Final visible RGB color
 * - Fixture capabilities
 * - Physical installation pan and tilt
 * - Final resolved pan and tilt
 * - Visual beam width and length
 * - Effective beam hit distance
 *
 * Installation orientation and live movement are intentionally
 * separate:
 *
 * Mount Pan / Tilt
 *     Physical installed orientation.
 *
 * Resolved Pan / Tilt
 *     Installation orientation plus active DMX/manual movement.
 *
 * Strobe is stored as a standard 0-255 fixture parameter:
 *
 * 0
 *     Continuous output.
 *
 * 1-255
 *     Increasing flash rate.
 */
public final class DmxFixtureRenderState
        extends BlockEntityRenderState {

    /*
     * -----------------------------------------------------------------
     * Fixture profile identity
     * -----------------------------------------------------------------
     */

    private String profileId =
            "rgb_par";

    private String profileDisplayName =
            "RGB PAR";

    /*
     * -----------------------------------------------------------------
     * Active fixture values
     * -----------------------------------------------------------------
     */

    private int red;
    private int green;
    private int blue;
    private int white;
    private int dimmer;

    private int pan =
            128;

    private int tilt =
            128;

    private int zoom;
    private int strobe;
    private int gobo;

    /**
     * Final visible fixture color packed as 0xRRGGBB.
     */
    private int packedRgb;

    /*
     * -----------------------------------------------------------------
     * Profile capabilities
     * -----------------------------------------------------------------
     */

    private boolean supportsRgb;
    private boolean supportsWhite;
    private boolean supportsDimmer;

    private boolean supportsPan;
    private boolean supportsTilt;
    private boolean supportsZoom;
    private boolean supportsStrobe;
    private boolean supportsGobo;

    private boolean beamFixture;
    private boolean movingFixture;

    /*
     * -----------------------------------------------------------------
     * Installation orientation
     * -----------------------------------------------------------------
     *
     * These values describe how the fixture is physically mounted.
     *
     * They do not include live pan/tilt movement.
     */

    private float mountPanDegrees =
            0.0F;

    private float mountTiltDegrees =
            0.0F;

    private float discoRotationDegrees =
            0.0F;

    private boolean discoBaseOnTop;

    /*
     * -----------------------------------------------------------------
     * Resolved orientation
     * -----------------------------------------------------------------
     *
     * These are the actual angles used for rendering.
     *
     * resolved pan  = mount pan  + active pan offset
     * resolved tilt = mount tilt + active tilt offset
     */

    private float resolvedPanDegrees =
            0.0F;

    private float resolvedTiltDegrees =
            0.0F;

    /*
     * -----------------------------------------------------------------
     * Visual beam settings
     * -----------------------------------------------------------------
     */

    private int beamWidthDegrees =
            FixtureBeamSettings.DEFAULT_WIDTH_DEGREES;

    private int beamLengthBlocks =
            FixtureBeamSettings.DEFAULT_LENGTH_BLOCKS;

    /**
     * Effective beam travel distance for this frame.
     */
    private float beamHitDistanceBlocks =
            FixtureBeamSettings.DEFAULT_LENGTH_BLOCKS;

    /*
     * -----------------------------------------------------------------
     * Profile identity accessors
     * -----------------------------------------------------------------
     */

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(
            String profileId
    ) {
        this.profileId =
                profileId == null
                        || profileId.isBlank()
                        ? "rgb_par"
                        : profileId;
    }

    public String getProfileDisplayName() {
        return profileDisplayName;
    }

    public void setProfileDisplayName(
            String profileDisplayName
    ) {
        this.profileDisplayName =
                profileDisplayName == null
                        || profileDisplayName.isBlank()
                        ? "DMX Fixture"
                        : profileDisplayName;
    }

    /*
     * -----------------------------------------------------------------
     * Active color accessors
     * -----------------------------------------------------------------
     */

    public int getRed() {
        return red;
    }

    public void setRed(
            int red
    ) {
        this.red =
                clampDmx(
                        red
                );
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(
            int green
    ) {
        this.green =
                clampDmx(
                        green
                );
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(
            int blue
    ) {
        this.blue =
                clampDmx(
                        blue
                );
    }

    public int getWhite() {
        return white;
    }

    public void setWhite(
            int white
    ) {
        this.white =
                clampDmx(
                        white
                );
    }

    public int getDimmer() {
        return dimmer;
    }

    public void setDimmer(
            int dimmer
    ) {
        this.dimmer =
                clampDmx(
                        dimmer
                );
    }

    /*
     * -----------------------------------------------------------------
     * Active movement and effect accessors
     * -----------------------------------------------------------------
     */

    public int getPan() {
        return pan;
    }

    public void setPan(
            int pan
    ) {
        this.pan =
                clampDmx(
                        pan
                );
    }

    public int getTilt() {
        return tilt;
    }

    public void setTilt(
            int tilt
    ) {
        this.tilt =
                clampDmx(
                        tilt
                );
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(
            int zoom
    ) {
        this.zoom =
                clampDmx(
                        zoom
                );
    }

    public int getStrobe() {
        return strobe;
    }

    public void setStrobe(
            int strobe
    ) {
        this.strobe =
                clampDmx(
                        strobe
                );
    }

    /*
     * -----------------------------------------------------------------
     * Strobe timing
     * -----------------------------------------------------------------
     */

    /**
     * Returns true when the fixture should currently be visibly ON
     * after applying the active Strobe parameter.
     *
     * Strobe mapping:
     *
     * 0
     *     Continuous light.
     *
     * 1 through 255
     *     Increasing flash speed from approximately 1 Hz through
     *     20 Hz.
     *
     * The current strobe uses a 50 percent duty cycle:
     *
     * first half of each cycle
     *     ON
     *
     * second half of each cycle
     *     OFF
     *
     * The lens and visible beam can both use this method so their
     * flashing remains synchronized.
     */
    public boolean isStrobeVisible() {
        if (strobe <= 0) {
            return true;
        }

        float flashesPerSecond =
                getStrobeFlashesPerSecond();

        double cycleMilliseconds =
                1000.0D
                        / flashesPerSecond;

        double cyclePosition =
                System.currentTimeMillis()
                        % cycleMilliseconds;

        return cyclePosition
                < cycleMilliseconds
                        * 0.5D;
    }

    /**
     * Converts the raw Strobe parameter into flashes per second.
     *
     * 0
     *     Strobe disabled.
     *
     * 1
     *     Approximately 1 flash per second.
     *
     * 255
     *     Approximately 20 flashes per second.
     */
    public float getStrobeFlashesPerSecond() {
        if (strobe <= 0) {
            return 0.0F;
        }

        float normalized =
                (
                        strobe - 1
                )
                        / 254.0F;

        return 1.0F
                + normalized
                * 19.0F;
    }

    public int getGobo() {
        return gobo;
    }

    public void setGobo(
            int gobo
    ) {
        this.gobo =
                clampDmx(
                        gobo
                );
    }

    /*
     * -----------------------------------------------------------------
     * Visible output accessors
     * -----------------------------------------------------------------
     */

    public int getPackedRgb() {
        return packedRgb;
    }

    public void setPackedRgb(
            int packedRgb
    ) {
        this.packedRgb =
                packedRgb
                        & 0xFFFFFF;
    }

    public float getNormalizedDimmer() {
        return dimmer
                / 255.0F;
    }

    public boolean isLit() {
        return dimmer > 0
                && packedRgb != 0;
    }

    /*
     * -----------------------------------------------------------------
     * Capability accessors
     * -----------------------------------------------------------------
     */

    public boolean supportsRgb() {
        return supportsRgb;
    }

    public void setSupportsRgb(
            boolean supportsRgb
    ) {
        this.supportsRgb =
                supportsRgb;
    }

    public boolean supportsWhite() {
        return supportsWhite;
    }

    public void setSupportsWhite(
            boolean supportsWhite
    ) {
        this.supportsWhite =
                supportsWhite;
    }

    public boolean supportsDimmer() {
        return supportsDimmer;
    }

    public void setSupportsDimmer(
            boolean supportsDimmer
    ) {
        this.supportsDimmer =
                supportsDimmer;
    }

    public boolean supportsPan() {
        return supportsPan;
    }

    public void setSupportsPan(
            boolean supportsPan
    ) {
        this.supportsPan =
                supportsPan;
    }

    public boolean supportsTilt() {
        return supportsTilt;
    }

    public void setSupportsTilt(
            boolean supportsTilt
    ) {
        this.supportsTilt =
                supportsTilt;
    }

    public boolean supportsZoom() {
        return supportsZoom;
    }

    public void setSupportsZoom(
            boolean supportsZoom
    ) {
        this.supportsZoom =
                supportsZoom;
    }

    public boolean supportsStrobe() {
        return supportsStrobe;
    }

    public void setSupportsStrobe(
            boolean supportsStrobe
    ) {
        this.supportsStrobe =
                supportsStrobe;
    }

    public boolean supportsGobo() {
        return supportsGobo;
    }

    public void setSupportsGobo(
            boolean supportsGobo
    ) {
        this.supportsGobo =
                supportsGobo;
    }

    public boolean isBeamFixture() {
        return beamFixture;
    }

    public void setBeamFixture(
            boolean beamFixture
    ) {
        this.beamFixture =
                beamFixture;
    }

    public boolean isMovingFixture() {
        return movingFixture;
    }

    public void setMovingFixture(
            boolean movingFixture
    ) {
        this.movingFixture =
                movingFixture;
    }

    /*
     * -----------------------------------------------------------------
     * Installation orientation accessors
     * -----------------------------------------------------------------
     */

    public float getMountPanDegrees() {
        return mountPanDegrees;
    }

    public void setMountPanDegrees(
            float mountPanDegrees
    ) {
        this.mountPanDegrees =
                normalizeDegrees(
                        mountPanDegrees
                );
    }

    public float getMountTiltDegrees() {
        return mountTiltDegrees;
    }

    public void setMountTiltDegrees(
            float mountTiltDegrees
    ) {
        this.mountTiltDegrees =
                normalizeDegrees(
                        mountTiltDegrees
                );
    }

    /*
     * -----------------------------------------------------------------
     * Resolved orientation accessors
     * -----------------------------------------------------------------
     */

    /**
     * Returns the final Pan angle used to render the fixture.
     */
    public float getResolvedPanDegrees() {
        return resolvedPanDegrees;
    }

    /**
     * Sets the final Pan angle used to render the fixture.
     */
    public void setResolvedPanDegrees(
            float resolvedPanDegrees
    ) {
        this.resolvedPanDegrees =
                normalizeDegrees(
                        resolvedPanDegrees
                );
    }

    /**
     * Returns the final Tilt angle used to render the fixture.
     */
    public float getResolvedTiltDegrees() {
        return resolvedTiltDegrees;
    }

    /**
     * Sets the final Tilt angle used to render the fixture.
     */
    public void setResolvedTiltDegrees(
            float resolvedTiltDegrees
    ) {
        this.resolvedTiltDegrees =
                normalizeDegrees(
                        resolvedTiltDegrees
                );
    }

    /**
     * Sets both physical mount angles.
     */
    public void setMountOrientation(
            float panDegrees,
            float tiltDegrees
    ) {
        setMountPanDegrees(
                panDegrees
        );

        setMountTiltDegrees(
                tiltDegrees
        );
    }

    public float getDiscoRotationDegrees() {
        return discoRotationDegrees;
    }

    public void setDiscoRotationDegrees(float discoRotationDegrees) {
        this.discoRotationDegrees = normalizeDegrees(
                discoRotationDegrees
        );
    }

    public boolean isDiscoBaseOnTop() {
        return discoBaseOnTop;
    }

    public void setDiscoBaseOnTop(boolean discoBaseOnTop) {
        this.discoBaseOnTop = discoBaseOnTop;
    }

    /**
     * Sets both final resolved render angles.
     */
    public void setResolvedOrientation(
            float panDegrees,
            float tiltDegrees
    ) {
        setResolvedPanDegrees(
                panDegrees
        );

        setResolvedTiltDegrees(
                tiltDegrees
        );
    }

    /*
     * -----------------------------------------------------------------
     * Visual beam-setting accessors
     * -----------------------------------------------------------------
     */

    public int getBeamWidthDegrees() {
        return beamWidthDegrees;
    }

    public void setBeamWidthDegrees(
            int beamWidthDegrees
    ) {
        this.beamWidthDegrees =
                clamp(
                        beamWidthDegrees,
                        FixtureBeamSettings.MIN_WIDTH_DEGREES,
                        FixtureBeamSettings.MAX_WIDTH_DEGREES
                );
    }

    public int getBeamLengthBlocks() {
        return beamLengthBlocks;
    }

    public void setBeamLengthBlocks(
            int beamLengthBlocks
    ) {
        this.beamLengthBlocks =
                clamp(
                        beamLengthBlocks,
                        FixtureBeamSettings.MIN_LENGTH_BLOCKS,
                        FixtureBeamSettings.MAX_LENGTH_BLOCKS
                );

        this.beamHitDistanceBlocks =
                this.beamLengthBlocks;
    }

    public void setBeamSettings(
            int beamWidthDegrees,
            int beamLengthBlocks
    ) {
        setBeamWidthDegrees(
                beamWidthDegrees
        );

        setBeamLengthBlocks(
                beamLengthBlocks
        );
    }

    /*
     * -----------------------------------------------------------------
     * Beam collision accessors
     * -----------------------------------------------------------------
     */

    public float getBeamHitDistanceBlocks() {
        return beamHitDistanceBlocks;
    }

    public void setBeamHitDistanceBlocks(
            float beamHitDistanceBlocks
    ) {
        if (!Float.isFinite(
                beamHitDistanceBlocks
        )) {
            this.beamHitDistanceBlocks =
                    beamLengthBlocks;

            return;
        }

        this.beamHitDistanceBlocks =
                Math.max(
                        0.0F,
                        Math.min(
                                beamLengthBlocks,
                                beamHitDistanceBlocks
                        )
                );
    }

    public void resetBeamHitDistance() {
        beamHitDistanceBlocks =
                beamLengthBlocks;
    }

    /*
     * -----------------------------------------------------------------
     * Helpers
     * -----------------------------------------------------------------
     */

    private static int clampDmx(
            int value
    ) {
        return clamp(
                value,
                0,
                255
        );
    }

    private static int clamp(
            int value,
            int minimum,
            int maximum
    ) {
        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }

    private static float normalizeDegrees(
            float degrees
    ) {
        if (!Float.isFinite(
                degrees
        )) {
            return 0.0F;
        }

        float normalized =
                degrees
                        % 360.0F;

        if (normalized < 0.0F) {
            normalized +=
                    360.0F;
        }

        if (normalized == -0.0F) {
            return 0.0F;
        }

        return normalized;
    }
}
