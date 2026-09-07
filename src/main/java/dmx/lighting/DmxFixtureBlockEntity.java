package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Stores the configuration and operating state of one placed DMX
 * fixture.
 *
 * FixtureIdentity
 *     Fixture name, fixture profile, and named group.
 *
 * FixturePatch
 *     Legacy universe and base-channel patching.
 *
 * FixtureParameterMap
 *     Explicit parameter-to-DMX-channel assignments.
 *
 * FixtureState
 *     Control mode, cached DMX output, stored manual output, and
 *     per-parameter Beam Width / Beam Length source selection.
 *
 * Installation orientation and live movement are intentionally
 * separate:
 *
 * Mount Pan / Tilt
 *     Describe the physical installed orientation of the fixture.
 *
 * Active Pan / Tilt
 *     DMX or manual movement relative to that installed orientation.
 *
 * Beam Width / Length are live fixture parameters.
 *
 * While the whole fixture is in DMX mode, Beam Width and Beam Length
 * may independently follow:
 *
 * - Their assigned DMX channels
 * - Their stored Manual values
 *
 * Whole-fixture Manual mode still forces all output parameters to use
 * the manual output.
 *
 * The fixture block itself intentionally emits no omnidirectional
 * Minecraft block light.
 */
public class DmxFixtureBlockEntity extends BlockEntity {

    /*
     * -----------------------------------------------------------------
     * Compatibility constants
     * -----------------------------------------------------------------
     */

    public static final int MIN_UNIVERSE =
            FixturePatch.MIN_UNIVERSE;

    public static final int MAX_UNIVERSE =
            FixturePatch.MAX_UNIVERSE;

    public static final int MIN_BASE_CHANNEL =
            FixturePatch.MIN_BASE_CHANNEL;

    public static final int MAX_BASE_CHANNEL =
            FixturePatch.getMaximumBaseChannel(
                    4
            );

    private static final int PARAMETER_MAP_VERSION =
            1;

    public static final float MIN_PAN_TILT_INTERPOLATION_TIME_SECONDS =
            0.05F;

    public static final float MAX_PAN_TILT_INTERPOLATION_TIME_SECONDS =
            60.0F;

    public static final float DEFAULT_PAN_TILT_INTERPOLATION_TIME_SECONDS =
            1.0F;

    public static final float MIN_COLOR_INTERPOLATION_TIME_SECONDS =
            ColorInterpolationSettings.MIN_TIME_SECONDS;

    public static final float MAX_COLOR_INTERPOLATION_TIME_SECONDS =
            ColorInterpolationSettings.MAX_TIME_SECONDS;

    public static final float DEFAULT_COLOR_INTERPOLATION_TIME_SECONDS =
            ColorInterpolationSettings.DEFAULT_TIME_SECONDS;

    private static final float TICKS_PER_SECOND =
            20.0F;

    /*
     * -----------------------------------------------------------------
     * Structured fixture data
     * -----------------------------------------------------------------
     */

    private final FixtureIdentity identity =
            new FixtureIdentity();

    private final FixturePatch patch =
            new FixturePatch();

    private final FixtureParameterMap parameterMap =
            new FixtureParameterMap();

    private final FixtureState fixtureState =
            new FixtureState(
                    FixtureControlMode.DMX,
                    new FixtureOutput(),
                    new FixtureOutput(
                            255,
                            255,
                            255,
                            0,
                            255,
                            0,
                            0,
                            0,
                            0,
                            0
                    )
            );

    /*
     * -----------------------------------------------------------------
     * Installation orientation
     * -----------------------------------------------------------------
     */

    private float mountPanDegrees =
            0.0F;

    private float mountTiltDegrees =
            0.0F;

    /*
     * Pan/Tilt interpolation is a persisted fixture setting. The
     * in-progress movement below is deliberately transient and is
     * reconstructed from the current active output on the client.
     */
    private boolean panTiltInterpolationEnabled =
            false;

    private float panTiltInterpolationTimeSeconds =
            DEFAULT_PAN_TILT_INTERPOLATION_TIME_SECONDS;

    private boolean panTiltInterpolationInitialized =
            false;

    private float displayedPanValue =
            128.0F;

    private float displayedTiltValue =
            128.0F;

    private float interpolationStartPanValue =
            128.0F;

    private float interpolationStartTiltValue =
            128.0F;

    private int interpolationTargetPanValue =
            128;

    private int interpolationTargetTiltValue =
            128;

    private double interpolationStartGameTime =
            0.0D;

    /*
     * Visible RGB fade is persisted per fixture. The in-progress
     * transition is render-only and reconstructed on the client.
     */
    private boolean colorInterpolationEnabled =
            false;

    private float colorInterpolationTimeSeconds =
            DEFAULT_COLOR_INTERPOLATION_TIME_SECONDS;

    private final ColorInterpolationState colorInterpolation =
            new ColorInterpolationState();

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    public DmxFixtureBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        this(
                ModBlockEntities.DMX_FIXTURE_BLOCK_ENTITY,
                blockPos,
                blockState
        );
    }

    protected DmxFixtureBlockEntity(
            net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(
                type,
                blockPos,
                blockState
        );

        DmxFixtureProfileRegistry.initialize();
    }

    /*
     * -----------------------------------------------------------------
     * Structured-object access
     * -----------------------------------------------------------------
     */

    public FixtureIdentity getIdentity() {
        return identity;
    }

    public FixturePatch getPatch() {
        return patch;
    }

    public FixtureParameterMap getParameterMap() {
        return parameterMap;
    }

    public FixtureState getFixtureState() {
        return fixtureState;
    }

    /*
     * -----------------------------------------------------------------
     * Identity and compatibility patch getters
     * -----------------------------------------------------------------
     */

    public int getUniverse() {
        return patch.getUniverse();
    }

    public int getBaseChannel() {
        return patch.getBaseChannel();
    }

    public String getFixtureType() {
        return identity.getFixtureProfile();
    }

    public String getFixtureName() {
        return identity.getFixtureName();
    }

    public FixtureGroupName getFixtureGroup() {
        return identity.getGroup();
    }

    public String getGroupKey() {
        return identity.getGroupKey();
    }

    public String getGroupDisplayName() {
        return identity.getGroupDisplayName();
    }

    public boolean isUngrouped() {
        return identity.isUngrouped();
    }

    public FixtureControlMode getControlMode() {
        return fixtureState.getControlMode();
    }

    /*
     * -----------------------------------------------------------------
     * Per-parameter control modes
     * -----------------------------------------------------------------
     */

    public FixtureParameterControlMode
    getBeamWidthControlMode() {
        return fixtureState
                .getBeamWidthControlMode();
    }

    public FixtureParameterControlMode
    getBeamLengthControlMode() {
        return fixtureState
                .getBeamLengthControlMode();
    }

    public boolean isBeamWidthDmxControlled() {
        return fixtureState
                .isBeamWidthDmxControlled();
    }

    public boolean isBeamWidthManualControlled() {
        return fixtureState
                .isBeamWidthManualControlled();
    }

    public boolean isBeamLengthDmxControlled() {
        return fixtureState
                .isBeamLengthDmxControlled();
    }

    public boolean isBeamLengthManualControlled() {
        return fixtureState
                .isBeamLengthManualControlled();
    }

    public void setBeamWidthControlMode(
            FixtureParameterControlMode mode
    ) {
        FixtureParameterControlMode safeMode =
                mode == null
                        ? FixtureParameterControlMode.DMX
                        : mode;

        if (fixtureState.getBeamWidthControlMode()
                == safeMode) {

            return;
        }

        fixtureState.setBeamWidthControlMode(
                safeMode
        );

        setChanged();
    }

    public void setBeamLengthControlMode(
            FixtureParameterControlMode mode
    ) {
        FixtureParameterControlMode safeMode =
                mode == null
                        ? FixtureParameterControlMode.DMX
                        : mode;

        if (fixtureState.getBeamLengthControlMode()
                == safeMode) {

            return;
        }

        fixtureState.setBeamLengthControlMode(
                safeMode
        );

        setChanged();
    }

    /*
     * -----------------------------------------------------------------
     * Installation orientation
     * -----------------------------------------------------------------
     */

    public float getMountPanDegrees() {
        return mountPanDegrees;
    }

    public float getMountTiltDegrees() {
        return mountTiltDegrees;
    }

    public void setMountPanDegrees(
            float mountPanDegrees
    ) {
        float normalized =
                normalizeDegrees(
                        mountPanDegrees
                );

        if (this.mountPanDegrees == normalized) {
            return;
        }

        this.mountPanDegrees =
                normalized;

        setChanged();
    }

    public void setMountTiltDegrees(
            float mountTiltDegrees
    ) {
        float normalized =
                normalizeDegrees(
                        mountTiltDegrees
                );

        if (this.mountTiltDegrees == normalized) {
            return;
        }

        this.mountTiltDegrees =
                normalized;

        setChanged();
    }

    public void setMountOrientation(
            float panDegrees,
            float tiltDegrees
    ) {
        float normalizedPan =
                normalizeDegrees(
                        panDegrees
                );

        float normalizedTilt =
                normalizeDegrees(
                        tiltDegrees
                );

        if (mountPanDegrees == normalizedPan
                && mountTiltDegrees == normalizedTilt) {

            return;
        }

        mountPanDegrees =
                normalizedPan;

        mountTiltDegrees =
                normalizedTilt;

        setChanged();
    }

    public boolean isPanTiltInterpolationEnabled() {
        return panTiltInterpolationEnabled;
    }

    public float getPanTiltInterpolationTimeSeconds() {
        return panTiltInterpolationTimeSeconds;
    }

    public boolean isColorInterpolationEnabled() {
        return colorInterpolationEnabled;
    }

    public float getColorInterpolationTimeSeconds() {
        return colorInterpolationTimeSeconds;
    }

    public void setPanTiltInterpolation(
            boolean enabled,
            float timeSeconds
    ) {
        float safeTimeSeconds =
                clampPanTiltInterpolationTimeSeconds(
                        timeSeconds
                );

        if (panTiltInterpolationEnabled == enabled
                && panTiltInterpolationTimeSeconds
                == safeTimeSeconds) {

            return;
        }

        panTiltInterpolationEnabled =
                enabled;

        panTiltInterpolationTimeSeconds =
                safeTimeSeconds;

        if (!enabled) {
            snapPanTiltInterpolationToActiveOutput();
        }

        setChanged();
    }

    public void setColorInterpolation(
            boolean enabled,
            float timeSeconds
    ) {
        float safeTimeSeconds =
                ColorInterpolationSettings.clampTimeSeconds(
                        timeSeconds
                );

        if (colorInterpolationEnabled == enabled
                && colorInterpolationTimeSeconds
                == safeTimeSeconds) {

            return;
        }

        colorInterpolationEnabled =
                enabled;

        colorInterpolationTimeSeconds =
                safeTimeSeconds;

        if (!enabled) {
            snapColorInterpolationToActiveOutput();
        }

        setChanged();
    }

    /**
     * Updates the client-side displayed Pan/Tilt position. Using game
     * time plus render tick progress makes the movement smooth between
     * game ticks and avoids relying on server tick timing.
     */
    public void updatePanTiltInterpolation(
            float tickProgress
    ) {
        int activePan =
                clampDmxValue(
                        getActivePan()
                );

        int activeTilt =
                clampDmxValue(
                        getActiveTilt()
                );

        double now =
                getInterpolationGameTime(
                        tickProgress
                );

        if (!panTiltInterpolationEnabled) {
            setPanTiltInterpolationPosition(
                    activePan,
                    activeTilt,
                    now
            );

            return;
        }

        if (!panTiltInterpolationInitialized) {
            setPanTiltInterpolationPosition(
                    activePan,
                    activeTilt,
                    now
            );

            return;
        }

        samplePanTiltInterpolation(
                now
        );

        if (activePan != interpolationTargetPanValue
                || activeTilt != interpolationTargetTiltValue) {

            interpolationStartPanValue =
                    displayedPanValue;

            interpolationStartTiltValue =
                    displayedTiltValue;

            interpolationTargetPanValue =
                    activePan;

            interpolationTargetTiltValue =
                    activeTilt;

            interpolationStartGameTime =
                    now;
        }
    }

    public boolean updateColorInterpolation(
            float tickProgress
    ) {
        int activePackedRgb =
                getOutputPackedRgb();

        return colorInterpolation.update(
                activePackedRgb,
                colorInterpolationEnabled,
                colorInterpolationTimeSeconds,
                getInterpolationGameTime(
                        tickProgress
                )
        );
    }

    private void samplePanTiltInterpolation(
            double now
    ) {
        double durationTicks =
                Math.max(
                        1.0D,
                        panTiltInterpolationTimeSeconds
                                * TICKS_PER_SECOND
                );

        float progress =
                (float) Math.max(
                        0.0D,
                        Math.min(
                                1.0D,
                                (now - interpolationStartGameTime)
                                        / durationTicks
                        )
                );

        displayedPanValue =
                lerp(
                        interpolationStartPanValue,
                        interpolationTargetPanValue,
                        progress
                );

        displayedTiltValue =
                lerp(
                        interpolationStartTiltValue,
                        interpolationTargetTiltValue,
                        progress
                );
    }

    private void snapPanTiltInterpolationToActiveOutput() {
        setPanTiltInterpolationPosition(
                clampDmxValue(
                        getActivePan()
                ),
                clampDmxValue(
                        getActiveTilt()
                ),
                getInterpolationGameTime(
                        0.0F
                )
        );
    }

    private void snapColorInterpolationToActiveOutput() {
        colorInterpolation.snapTo(
                getOutputPackedRgb(),
                getInterpolationGameTime(
                        0.0F
                )
        );
    }

    private void setPanTiltInterpolationPosition(
            int pan,
            int tilt,
            double gameTime
    ) {
        displayedPanValue =
                pan;

        displayedTiltValue =
                tilt;

        interpolationStartPanValue =
                pan;

        interpolationStartTiltValue =
                tilt;

        interpolationTargetPanValue =
                pan;

        interpolationTargetTiltValue =
                tilt;

        interpolationStartGameTime =
                gameTime;

        panTiltInterpolationInitialized =
                true;
    }

    private double getInterpolationGameTime(
            float tickProgress
    ) {
        if (level == null) {
            return 0.0D;
        }

        return level.getGameTime()
                + Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                tickProgress
                        )
                );
    }

    /*
     * -----------------------------------------------------------------
     * Resolved fixture movement
     * -----------------------------------------------------------------
     */

    /**
     * Returns the currently active Pan offset.
     */
    public float getActivePanOffsetDegrees() {
        return mapDmxToSignedDegrees(
                panTiltInterpolationEnabled
                        ? displayedPanValue
                        : getActivePan(),
                180.0F
        );
    }

    /**
     * Returns the currently active Tilt offset.
     */
    public float getActiveTiltOffsetDegrees() {
        return mapDmxToSignedDegrees(
                panTiltInterpolationEnabled
                        ? displayedTiltValue
                        : getActiveTilt(),
                90.0F
        );
    }

    public float getResolvedPanDegrees() {
        return normalizeDegrees(
                mountPanDegrees
                        + getActivePanOffsetDegrees()
        );
    }

    public float getResolvedTiltDegrees() {
        return normalizeDegrees(
                mountTiltDegrees
                        + getActiveTiltOffsetDegrees()
        );
    }

    /*
     * -----------------------------------------------------------------
     * Resolved beam settings
     * -----------------------------------------------------------------
     */

    public int getActiveBeamWidth() {
        return fixtureState
                .getActiveBeamWidth();
    }

    public int getActiveBeamLength() {
        return fixtureState
                .getActiveBeamLength();
    }

    public int getResolvedBeamWidthDegrees() {
        return mapDmxToRange(
                getActiveBeamWidth(),
                FixtureBeamSettings.MIN_WIDTH_DEGREES,
                FixtureBeamSettings.MAX_WIDTH_DEGREES
        );
    }

    public int getResolvedBeamLengthBlocks() {
        return mapDmxToRange(
                getActiveBeamLength(),
                FixtureBeamSettings.MIN_LENGTH_BLOCKS,
                FixtureBeamSettings.MAX_LENGTH_BLOCKS
        );
    }

    /*
     * -----------------------------------------------------------------
     * Temporary beam compatibility
     * -----------------------------------------------------------------
     */

    public int getBeamWidthDegrees() {
        return getResolvedBeamWidthDegrees();
    }

    public int getBeamLengthBlocks() {
        return getResolvedBeamLengthBlocks();
    }

    public void setBeamSettings(
            int ignoredBeamWidthDegrees,
            int ignoredBeamLengthBlocks
    ) {
        /*
         * Intentionally empty.
         *
         * Retained temporarily for older networking/debug code.
         */
    }

    /*
     * -----------------------------------------------------------------
     * Cached DMX getters
     * -----------------------------------------------------------------
     */

    public int getRed() {
        return fixtureState
                .getDmxOutput()
                .getRed();
    }

    public int getGreen() {
        return fixtureState
                .getDmxOutput()
                .getGreen();
    }

    public int getBlue() {
        return fixtureState
                .getDmxOutput()
                .getBlue();
    }

    public int getWhite() {
        return fixtureState
                .getDmxOutput()
                .getWhite();
    }

    public int getAmber() {
        return fixtureState
                .getDmxOutput()
                .getAmber();
    }

    public int getDimmer() {
        return fixtureState
                .getDmxOutput()
                .getDimmer();
    }

    public int getDmxPan() {
        return fixtureState
                .getDmxOutput()
                .getPan();
    }

    public int getDmxTilt() {
        return fixtureState
                .getDmxOutput()
                .getTilt();
    }

    public int getDmxZoom() {
        return fixtureState
                .getDmxOutput()
                .getZoom();
    }

    public int getDmxBeamWidth() {
        return fixtureState
                .getDmxOutput()
                .getBeamWidth();
    }

    public int getDmxBeamLength() {
        return fixtureState
                .getDmxOutput()
                .getBeamLength();
    }

    public int getDmxStrobe() {
        return fixtureState
                .getDmxOutput()
                .getStrobe();
    }

    public int getDmxGobo() {
        return fixtureState
                .getDmxOutput()
                .getGobo();
    }

    /*
     * -----------------------------------------------------------------
     * Stored manual getters
     * -----------------------------------------------------------------
     */

    public int getManualRed() {
        return fixtureState
                .getManualOutput()
                .getRed();
    }

    public int getManualGreen() {
        return fixtureState
                .getManualOutput()
                .getGreen();
    }

    public int getManualBlue() {
        return fixtureState
                .getManualOutput()
                .getBlue();
    }

    public int getManualWhite() {
        return fixtureState
                .getManualOutput()
                .getWhite();
    }

    public int getManualAmber() {
        return fixtureState
                .getManualOutput()
                .getAmber();
    }

    public int getManualDimmer() {
        return fixtureState
                .getManualOutput()
                .getDimmer();
    }

    public int getManualPan() {
        return fixtureState
                .getManualOutput()
                .getPan();
    }

    public int getManualTilt() {
        return fixtureState
                .getManualOutput()
                .getTilt();
    }

    public int getManualZoom() {
        return fixtureState
                .getManualOutput()
                .getZoom();
    }

    public int getManualBeamWidth() {
        return fixtureState
                .getManualOutput()
                .getBeamWidth();
    }

    public int getManualBeamLength() {
        return fixtureState
                .getManualOutput()
                .getBeamLength();
    }

    public int getManualStrobe() {
        return fixtureState
                .getManualOutput()
                .getStrobe();
    }

    public int getManualGobo() {
        return fixtureState
                .getManualOutput()
                .getGobo();
    }

    /*
     * -----------------------------------------------------------------
     * Configuration setters
     * -----------------------------------------------------------------
     */

    public void setUniverse(
            int universe
    ) {
        int oldUniverse =
                patch.getUniverse();

        patch.setUniverse(
                universe
        );

        if (oldUniverse == patch.getUniverse()) {
            return;
        }

        refreshFromDmx();
        updateBlockLightLevel();
        setChanged();
    }

    public void setBaseChannel(
            int baseChannel
    ) {
        int oldBaseChannel =
                patch.getBaseChannel();

        patch.setBaseChannel(
                clamp(
                        baseChannel,
                        MIN_BASE_CHANNEL,
                        getMaximumBaseChannelForCurrentProfile()
                )
        );

        if (oldBaseChannel == patch.getBaseChannel()) {
            return;
        }

        refreshFromDmx();
        updateBlockLightLevel();
        setChanged();
    }

    public void setFixtureType(
            String fixtureType
    ) {
        String resolvedFixtureType =
                DmxFixtureProfileRegistry.resolveProfileId(
                        fixtureType
                );

        String oldFixtureType =
                DmxFixtureProfileRegistry.resolveProfileId(
                        identity.getFixtureProfile()
                );

        identity.setFixtureProfile(
                resolvedFixtureType
        );

        if (oldFixtureType.equals(
                resolvedFixtureType
        )) {
            return;
        }

        patch.clampForChannelCount(
                getCurrentProfileChannelCount()
        );

        refreshFromDmx();
        updateBlockLightLevel();
        setChanged();
    }

    public void setFixtureName(
            String fixtureName
    ) {
        String oldFixtureName =
                identity.getFixtureName();

        identity.setFixtureName(
                fixtureName
        );

        if (oldFixtureName.equals(
                identity.getFixtureName()
        )) {
            return;
        }

        setChanged();
    }

    public void setFixtureGroup(
            String groupName
    ) {
        setFixtureGroup(
                FixtureGroupName.of(
                        groupName
                )
        );
    }

    public void setFixtureGroup(
            FixtureGroupName group
    ) {
        FixtureGroupName oldGroup =
                identity.getGroup();

        identity.setGroup(
                group
        );

        if (oldGroup.equals(
                identity.getGroup()
        )) {
            return;
        }

        setChanged();
    }

    public void setGroupName(
            String groupName
    ) {
        setFixtureGroup(
                groupName
        );
    }

    public void setControlMode(
            FixtureControlMode controlMode
    ) {
        FixtureControlMode safeMode =
                controlMode == null
                        ? FixtureControlMode.DMX
                        : controlMode;

        if (fixtureState.getControlMode() == safeMode) {
            return;
        }

        fixtureState.setControlMode(
                safeMode
        );

        updateBlockLightLevel();
        setChanged();
    }

    /*
     * -----------------------------------------------------------------
     * Manual output setters
     * -----------------------------------------------------------------
     */

    public void setManualOutput(
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        setManualOutput(
                new FixtureOutput(
                        red,
                        green,
                        blue,
                        dimmer
                )
        );
    }

    public void setManualOutput(
            int red,
            int green,
            int blue,
            int white,
            int dimmer,
            int pan,
            int tilt,
            int zoom,
            int strobe,
            int gobo
    ) {
        setManualOutput(
                new FixtureOutput(
                        red,
                        green,
                        blue,
                        white,
                        dimmer,
                        pan,
                        tilt,
                        zoom,
                        strobe,
                        gobo
                )
        );
    }

    public void setManualParameterOutput(
            int red,
            int green,
            int blue,
            int white,
            int amber,
            int dimmer,
            int pan,
            int tilt,
            int beamWidth,
            int beamLength,
            int strobe
    ) {
        setManualOutput(
                new FixtureOutput(
                        red,
                        green,
                        blue,
                        white,
                        amber,
                        dimmer,
                        pan,
                        tilt,
                        beamWidth,
                        beamLength,
                        strobe
                )
        );
    }

    public void setManualOutput(
            FixtureOutput output
    ) {
        FixtureOutput safeOutput =
                output == null
                        ? FixtureOutput.BLACKOUT
                        : output;

        FixtureOutput manualOutput =
                fixtureState.getManualOutput();

        if (manualOutput.equals(
                safeOutput
        )) {
            return;
        }

        fixtureState.setManualOutput(
                safeOutput
        );

        if (fixtureState.isManualMode()) {
            updateBlockLightLevel();
        }

        setChanged();
    }

    /*
     * -----------------------------------------------------------------
     * Parameter-centric DMX polling
     * -----------------------------------------------------------------
     */

    public void refreshFromDmx() {
        FixtureOutput newOutput =
                readParameterMappedDmxOutput();

        applyDmxOutput(
                newOutput
        );
    }

    public FixtureOutput readParameterMappedDmxOutput() {
        DmxUniverse universe =
                DmxUniverseManager.getOrCreateUniverse(
                        getUniverse()
                );

        int red =
                readOptionalChannel(
                        universe,
                        parameterMap.getRedChannel(),
                        0
                );

        int green =
                readOptionalChannel(
                        universe,
                        parameterMap.getGreenChannel(),
                        0
                );

        int blue =
                readOptionalChannel(
                        universe,
                        parameterMap.getBlueChannel(),
                        0
                );

        int white =
                readOptionalChannel(
                        universe,
                        parameterMap.getWhiteChannel(),
                        0
                );

        int amber =
                readOptionalChannel(
                        universe,
                        parameterMap.getAmberChannel(),
                        0
                );

        int dimmer =
                readOptionalChannel(
                        universe,
                        parameterMap.getDimmerChannel(),
                        255
                );

        int pan =
                readOptionalChannel(
                        universe,
                        parameterMap.getPanChannel(),
                        128
                );

        int tilt =
                readOptionalChannel(
                        universe,
                        parameterMap.getTiltChannel(),
                        128
                );

        int beamWidth =
                readOptionalChannel(
                        universe,
                        parameterMap.getBeamWidthChannel(),
                        0
                );

        int beamLength =
                readOptionalChannel(
                        universe,
                        parameterMap.getBeamLengthChannel(),
                        0
                );

        int strobe =
                readOptionalChannel(
                        universe,
                        parameterMap.getStrobeChannel(),
                        0
                );

        return new FixtureOutput(
                red,
                green,
                blue,
                white,
                amber,
                dimmer,
                pan,
                tilt,
                beamWidth,
                beamLength,
                strobe
        );
    }

    /*
     * -----------------------------------------------------------------
     * Legacy comparison / fallback
     * -----------------------------------------------------------------
     */

    public boolean parameterMapMatchesLegacyDmx() {
        FixtureOutput parameterOutput =
                readParameterMappedDmxOutput();

        FixtureOutput legacyOutput =
                readLegacyDmxOutput();

        return parameterOutput.getRed()
                == legacyOutput.getRed()
                && parameterOutput.getGreen()
                == legacyOutput.getGreen()
                && parameterOutput.getBlue()
                == legacyOutput.getBlue()
                && parameterOutput.getWhite()
                == legacyOutput.getWhite()
                && parameterOutput.getDimmer()
                == legacyOutput.getDimmer()
                && parameterOutput.getPan()
                == legacyOutput.getPan()
                && parameterOutput.getTilt()
                == legacyOutput.getTilt()
                && parameterOutput.getBeamWidth()
                == legacyOutput.getZoom()
                && parameterOutput.getStrobe()
                == legacyOutput.getStrobe();
    }

    private FixtureOutput readLegacyDmxOutput() {
        FixtureChannelLayout layout =
                getCurrentChannelLayout();

        DmxUniverse universe =
                DmxUniverseManager.getOrCreateUniverse(
                        getUniverse()
                );

        int baseChannel =
                getBaseChannel();

        return new FixtureOutput(
                readOptionalChannel(
                        universe,
                        layout.redChannel(
                                baseChannel
                        ),
                        0
                ),
                readOptionalChannel(
                        universe,
                        layout.greenChannel(
                                baseChannel
                        ),
                        0
                ),
                readOptionalChannel(
                        universe,
                        layout.blueChannel(
                                baseChannel
                        ),
                        0
                ),
                readOptionalChannel(
                        universe,
                        layout.whiteChannel(
                                baseChannel
                        ),
                        0
                ),
                readOptionalChannel(
                        universe,
                        layout.dimmerChannel(
                                baseChannel
                        ),
                        255
                ),
                readOptionalChannel(
                        universe,
                        layout.panChannel(
                                baseChannel
                        ),
                        128
                ),
                readOptionalChannel(
                        universe,
                        layout.tiltChannel(
                                baseChannel
                        ),
                        128
                ),
                readOptionalChannel(
                        universe,
                        layout.zoomChannel(
                                baseChannel
                        ),
                        0
                ),
                readOptionalChannel(
                        universe,
                        layout.strobeChannel(
                                baseChannel
                        ),
                        0
                ),
                readOptionalChannel(
                        universe,
                        layout.goboChannel(
                                baseChannel
                        ),
                        0
                )
        );
    }

    private void applyDmxOutput(
            FixtureOutput newOutput
    ) {
        FixtureOutput dmxOutput =
                fixtureState.getDmxOutput();

        if (dmxOutput.equals(
                newOutput
        )) {
            return;
        }

        fixtureState.setDmxOutput(
                newOutput
        );

        if (fixtureState.isDmxMode()) {
            updateBlockLightLevel();
        }

        setChanged();
    }

    /*
     * -----------------------------------------------------------------
     * Active raw output
     * -----------------------------------------------------------------
     */

    public int getActiveRed() {
        return fixtureState.getActiveRed();
    }

    public int getActiveGreen() {
        return fixtureState.getActiveGreen();
    }

    public int getActiveBlue() {
        return fixtureState.getActiveBlue();
    }

    public int getActiveWhite() {
        return fixtureState.getActiveWhite();
    }

    public int getActiveDimmer() {
        return fixtureState.getActiveDimmer();
    }

    public int getActivePan() {
        return fixtureState.getActivePan();
    }

    public int getActiveTilt() {
        return fixtureState.getActiveTilt();
    }

    public int getActiveZoom() {
        return fixtureState.getActiveZoom();
    }

    public int getActiveStrobe() {
        return fixtureState.getActiveStrobe();
    }

    public int getActiveGobo() {
        return fixtureState.getActiveGobo();
    }

    /*
     * -----------------------------------------------------------------
     * Visible output
     * -----------------------------------------------------------------
     */

    public int getOutputRed() {
        return fixtureState.getOutputRed();
    }

    public int getOutputGreen() {
        return fixtureState.getOutputGreen();
    }

    public int getOutputBlue() {
        return fixtureState.getOutputBlue();
    }

    public int getOutputWhite() {
        return fixtureState.getOutputWhite();
    }

    public int getOutputPackedRgb() {
        return fixtureState.getPackedRgb();
    }

    public int getSmoothedOutputPackedRgb() {
        return colorInterpolationEnabled
                ? colorInterpolation.getDisplayedPackedRgb(
                        getOutputPackedRgb()
                )
                : getOutputPackedRgb();
    }

    public int getMinecraftLightLevel() {
        return fixtureState.getMinecraftLightLevel();
    }

    /*
     * -----------------------------------------------------------------
     * Fixture block light
     * -----------------------------------------------------------------
     */

    private void updateBlockLightLevel() {
        if (level == null
                || level.isClientSide()) {

            return;
        }

        BlockState currentState =
                getBlockState();

        if (!currentState.hasProperty(
                DmxBlock.LIGHT_LEVEL
        )) {
            return;
        }

        int currentLight =
                currentState.getValue(
                        DmxBlock.LIGHT_LEVEL
                );

        int newLight =
                0;

        if (currentLight == newLight) {
            return;
        }

        level.setBlock(
                worldPosition,
                currentState.setValue(
                        DmxBlock.LIGHT_LEVEL,
                        newLight
                ),
                Block.UPDATE_ALL
        );
    }

    /*
     * -----------------------------------------------------------------
     * Tick
     * -----------------------------------------------------------------
     */

    public static void tick(
            Level level,
            BlockPos blockPos,
            BlockState blockState,
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }

        fixture.refreshFromDmx();

        fixture.updateBlockLightLevel();

        /*
         * Directional world-light experiments are intentionally
         * disabled for now.
         *
         * FixtureBeamLightManager.update(
         *         fixture
         * );
         */
    }

    /*
     * -----------------------------------------------------------------
     * Persistence
     * -----------------------------------------------------------------
     */

    @Override
    protected void saveAdditional(
            ValueOutput output
    ) {
        output.putInt(
                "universe",
                patch.getUniverse()
        );

        output.putInt(
                "base_channel",
                patch.getBaseChannel()
        );

        output.putString(
                "fixture_type",
                DmxFixtureProfileRegistry.resolveProfileId(
                        identity.getFixtureProfile()
                )
        );

        output.putString(
                "fixture_name",
                identity.getFixtureName()
        );

        output.putString(
                "group_key",
                identity.getGroupKey()
        );

        output.putString(
                "group_name",
                identity.getGroupDisplayName()
        );

        output.putString(
                "control_mode",
                fixtureState
                        .getControlMode()
                        .getSerializedName()
        );

        output.putString(
                "beam_width_control_mode",
                fixtureState
                        .getBeamWidthControlMode()
                        .name()
                        .toLowerCase()
        );

        output.putString(
                "beam_length_control_mode",
                fixtureState
                        .getBeamLengthControlMode()
                        .name()
                        .toLowerCase()
        );

        output.putInt(
                "pan_tilt_interpolation_enabled",
                panTiltInterpolationEnabled
                        ? 1
                        : 0
        );

        output.putFloat(
                "pan_tilt_interpolation_time_seconds",
                panTiltInterpolationTimeSeconds
        );

        output.putInt(
                "color_interpolation_enabled",
                colorInterpolationEnabled
                        ? 1
                        : 0
        );

        output.putFloat(
                "color_interpolation_time_seconds",
                colorInterpolationTimeSeconds
        );

        output.putFloat(
                "mount_pan",
                mountPanDegrees
        );

        output.putFloat(
                "mount_tilt",
                mountTiltDegrees
        );

        output.putInt(
                "parameter_map_version",
                PARAMETER_MAP_VERSION
        );

        output.putInt(
                "parameter_red",
                parameterMap.getRedChannel()
        );

        output.putInt(
                "parameter_green",
                parameterMap.getGreenChannel()
        );

        output.putInt(
                "parameter_blue",
                parameterMap.getBlueChannel()
        );

        output.putInt(
                "parameter_white",
                parameterMap.getWhiteChannel()
        );

        output.putInt(
                "parameter_amber",
                parameterMap.getAmberChannel()
        );

        output.putInt(
                "parameter_dimmer",
                parameterMap.getDimmerChannel()
        );

        output.putInt(
                "parameter_strobe",
                parameterMap.getStrobeChannel()
        );

        output.putInt(
                "parameter_pan",
                parameterMap.getPanChannel()
        );

        output.putInt(
                "parameter_tilt",
                parameterMap.getTiltChannel()
        );

        output.putInt(
                "parameter_beam_width",
                parameterMap.getBeamWidthChannel()
        );

        output.putInt(
                "parameter_beam_length",
                parameterMap.getBeamLengthChannel()
        );

        FixtureOutput dmxOutput =
                fixtureState.getDmxOutput();

        output.putInt(
                "red",
                dmxOutput.getRed()
        );

        output.putInt(
                "green",
                dmxOutput.getGreen()
        );

        output.putInt(
                "blue",
                dmxOutput.getBlue()
        );

        output.putInt(
                "white",
                dmxOutput.getWhite()
        );

        output.putInt(
                "amber",
                dmxOutput.getAmber()
        );

        output.putInt(
                "dimmer",
                dmxOutput.getDimmer()
        );

        output.putInt(
                "pan",
                dmxOutput.getPan()
        );

        output.putInt(
                "tilt",
                dmxOutput.getTilt()
        );

        output.putInt(
                "beam_width_dmx",
                dmxOutput.getBeamWidth()
        );

        output.putInt(
                "beam_length_dmx",
                dmxOutput.getBeamLength()
        );

        output.putInt(
                "zoom",
                dmxOutput.getZoom()
        );

        output.putInt(
                "strobe",
                dmxOutput.getStrobe()
        );

        output.putInt(
                "gobo",
                dmxOutput.getGobo()
        );

        FixtureOutput manualOutput =
                fixtureState.getManualOutput();

        output.putInt(
                "manual_red",
                manualOutput.getRed()
        );

        output.putInt(
                "manual_green",
                manualOutput.getGreen()
        );

        output.putInt(
                "manual_blue",
                manualOutput.getBlue()
        );

        output.putInt(
                "manual_white",
                manualOutput.getWhite()
        );

        output.putInt(
                "manual_amber",
                manualOutput.getAmber()
        );

        output.putInt(
                "manual_dimmer",
                manualOutput.getDimmer()
        );

        output.putInt(
                "manual_pan",
                manualOutput.getPan()
        );

        output.putInt(
                "manual_tilt",
                manualOutput.getTilt()
        );

        output.putInt(
                "manual_beam_width",
                manualOutput.getBeamWidth()
        );

        output.putInt(
                "manual_beam_length",
                manualOutput.getBeamLength()
        );

        output.putInt(
                "manual_zoom",
                manualOutput.getZoom()
        );

        output.putInt(
                "manual_strobe",
                manualOutput.getStrobe()
        );

        output.putInt(
                "manual_gobo",
                manualOutput.getGobo()
        );

        super.saveAdditional(
                output
        );
    }

    @Override
    protected void loadAdditional(
            ValueInput input
    ) {
        super.loadAdditional(
                input
        );

        int oldOutputRgb =
                getOutputPackedRgb();

        int oldLightLevel =
                getMinecraftLightLevel();

        /*
         * Identity.
         */

        String loadedFixtureType =
                input.getStringOr(
                        "fixture_type",
                        FixtureIdentity.DEFAULT_PROFILE
                );

        loadedFixtureType =
                DmxFixtureProfileRegistry.resolveProfileId(
                        loadedFixtureType
                );

        String loadedFixtureName =
                input.getStringOr(
                        "fixture_name",
                        ""
                );

        String loadedGroupName =
                input.getStringOr(
                        "group_name",
                        ""
                );

        String loadedGroupKey =
                input.getStringOr(
                        "group_key",
                        ""
                );

        identity.set(
                loadedFixtureName,
                loadedFixtureType,
                FixtureGroupName.fromSavedData(
                        loadedGroupKey,
                        loadedGroupName
                )
        );

        /*
         * Compatibility patch.
         */

        patch.set(
                input.getIntOr(
                        "universe",
                        FixturePatch.DEFAULT_UNIVERSE
                ),
                input.getIntOr(
                        "base_channel",
                        FixturePatch.DEFAULT_BASE_CHANNEL
                )
        );

        patch.clampForChannelCount(
                getCurrentProfileChannelCount()
        );

        /*
         * Installation orientation.
         */

        mountPanDegrees =
                normalizeDegrees(
                        input.getFloatOr(
                                "mount_pan",
                                0.0F
                        )
                );

        mountTiltDegrees =
                normalizeDegrees(
                        input.getFloatOr(
                                "mount_tilt",
                                0.0F
                        )
                );

        panTiltInterpolationEnabled =
                input.getIntOr(
                        "pan_tilt_interpolation_enabled",
                        0
                ) != 0;

        panTiltInterpolationTimeSeconds =
                clampPanTiltInterpolationTimeSeconds(
                        input.getFloatOr(
                                "pan_tilt_interpolation_time_seconds",
                                DEFAULT_PAN_TILT_INTERPOLATION_TIME_SECONDS
                        )
                );

        colorInterpolationEnabled =
                input.getIntOr(
                        "color_interpolation_enabled",
                        0
                ) != 0;

        colorInterpolationTimeSeconds =
                ColorInterpolationSettings.clampTimeSeconds(
                        input.getFloatOr(
                                "color_interpolation_time_seconds",
                                DEFAULT_COLOR_INTERPOLATION_TIME_SECONDS
                        )
                );

        /*
         * Explicit parameter map.
         */

        int loadedParameterMapVersion =
                input.getIntOr(
                        "parameter_map_version",
                        0
                );

        parameterMap.set(
                input.getIntOr(
                        "parameter_red",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_green",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_blue",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_white",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_amber",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_dimmer",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_strobe",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_pan",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_tilt",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_beam_width",
                        FixtureParameterMap.UNASSIGNED
                ),
                input.getIntOr(
                        "parameter_beam_length",
                        FixtureParameterMap.UNASSIGNED
                )
        );

        if (loadedParameterMapVersion
                < PARAMETER_MAP_VERSION) {

            migrateLegacyPatchToParameterMap();
        }

        /*
         * Whole-fixture control mode.
         */

        FixtureControlMode loadedMode =
                FixtureControlMode.fromName(
                        input.getStringOr(
                                "control_mode",
                                "dmx"
                        )
                );

        if (loadedMode == null) {
            loadedMode =
                    FixtureControlMode.DMX;
        }

        /*
         * Per-parameter beam source modes.
         */

        FixtureParameterControlMode loadedBeamWidthControlMode =
                parseParameterControlMode(
                        input.getStringOr(
                                "beam_width_control_mode",
                                "dmx"
                        )
                );

        FixtureParameterControlMode loadedBeamLengthControlMode =
                parseParameterControlMode(
                        input.getStringOr(
                                "beam_length_control_mode",
                                "dmx"
                        )
                );

        /*
         * Cached DMX output.
         */

        int loadedBeamWidth =
                input.getIntOr(
                        "beam_width_dmx",
                        input.getIntOr(
                                "zoom",
                                0
                        )
                );

        int loadedBeamLength =
                input.getIntOr(
                        "beam_length_dmx",
                        0
                );

        FixtureOutput loadedDmxOutput =
                new FixtureOutput(
                        input.getIntOr(
                                "red",
                                0
                        ),
                        input.getIntOr(
                                "green",
                                0
                        ),
                        input.getIntOr(
                                "blue",
                                0
                        ),
                        input.getIntOr(
                                "white",
                                0
                        ),
                        input.getIntOr(
                                "amber",
                                0
                        ),
                        input.getIntOr(
                                "dimmer",
                                0
                        ),
                        input.getIntOr(
                                "pan",
                                128
                        ),
                        input.getIntOr(
                                "tilt",
                                128
                        ),
                        loadedBeamWidth,
                        loadedBeamLength,
                        input.getIntOr(
                                "strobe",
                                0
                        )
                );

        loadedDmxOutput.setGobo(
                input.getIntOr(
                        "gobo",
                        0
                )
        );

        /*
         * Stored manual output.
         */

        int loadedManualBeamWidth =
                input.getIntOr(
                        "manual_beam_width",
                        input.getIntOr(
                                "manual_zoom",
                                0
                        )
                );

        int loadedManualBeamLength =
                input.getIntOr(
                        "manual_beam_length",
                        0
                );

        FixtureOutput loadedManualOutput =
                new FixtureOutput(
                        input.getIntOr(
                                "manual_red",
                                255
                        ),
                        input.getIntOr(
                                "manual_green",
                                255
                        ),
                        input.getIntOr(
                                "manual_blue",
                                255
                        ),
                        input.getIntOr(
                                "manual_white",
                                0
                        ),
                        input.getIntOr(
                                "manual_amber",
                                0
                        ),
                        input.getIntOr(
                                "manual_dimmer",
                                255
                        ),
                        input.getIntOr(
                                "manual_pan",
                                128
                        ),
                        input.getIntOr(
                                "manual_tilt",
                                128
                        ),
                        loadedManualBeamWidth,
                        loadedManualBeamLength,
                        input.getIntOr(
                                "manual_strobe",
                                0
                        )
                );

        loadedManualOutput.setGobo(
                input.getIntOr(
                        "manual_gobo",
                        0
                )
        );

        /*
         * Restore fixture state.
         */

        fixtureState.set(
                loadedMode,
                loadedDmxOutput,
                loadedManualOutput
        );

        fixtureState.setBeamWidthControlMode(
                loadedBeamWidthControlMode
        );

        fixtureState.setBeamLengthControlMode(
                loadedBeamLengthControlMode
        );

        boolean visibleOutputChanged =
                oldOutputRgb
                        != getOutputPackedRgb()
                        || oldLightLevel
                        != getMinecraftLightLevel();

        if (visibleOutputChanged
                && level != null
                && level.isClientSide()) {

            BlockState state =
                    getBlockState();

            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    Block.UPDATE_ALL
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Client synchronization
     * -----------------------------------------------------------------
     */

    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registryLookup
    ) {
        return saveWithoutMetadata(
                registryLookup
        );
    }

    @Override
    public Packet<ClientGamePacketListener>
    getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(
                this
        );
    }

    /*
     * -----------------------------------------------------------------
     * Fixture registry lifecycle
     * -----------------------------------------------------------------
     */

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        if (level != null
                && !level.isClientSide()) {

            DmxFixtureRegistry.register(
                    this
            );
        }
    }

    @Override
    public void setRemoved() {
        if (level != null
                && !level.isClientSide()) {

            FixtureBeamLightManager.clearFixture(
                    this
            );

            DmxFixtureRegistry.unregister(
                    this
            );
        }

        super.setRemoved();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (level == null) {
            return;
        }

        BlockState state =
                getBlockState();

        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_ALL
        );
    }

    /*
     * -----------------------------------------------------------------
     * Parameter-map migration
     * -----------------------------------------------------------------
     */

    private void migrateLegacyPatchToParameterMap() {
        FixtureChannelLayout layout =
                getCurrentChannelLayout();

        int baseChannel =
                getBaseChannel();

        parameterMap.set(
                normalizeLegacyParameterChannel(
                        layout.redChannel(
                                baseChannel
                        )
                ),
                normalizeLegacyParameterChannel(
                        layout.greenChannel(
                                baseChannel
                        )
                ),
                normalizeLegacyParameterChannel(
                        layout.blueChannel(
                                baseChannel
                        )
                ),
                normalizeLegacyParameterChannel(
                        layout.whiteChannel(
                                baseChannel
                        )
                ),

                FixtureParameterMap.UNASSIGNED,

                normalizeLegacyParameterChannel(
                        layout.dimmerChannel(
                                baseChannel
                        )
                ),

                normalizeLegacyParameterChannel(
                        layout.strobeChannel(
                                baseChannel
                        )
                ),

                normalizeLegacyParameterChannel(
                        layout.panChannel(
                                baseChannel
                        )
                ),

                normalizeLegacyParameterChannel(
                        layout.tiltChannel(
                                baseChannel
                        )
                ),

                normalizeLegacyParameterChannel(
                        layout.zoomChannel(
                                baseChannel
                        )
                ),

                FixtureParameterMap.UNASSIGNED
        );
    }

    private static int normalizeLegacyParameterChannel(
            int channel
    ) {
        if (!FixtureParameterMap.isAssigned(
                channel
        )) {
            return FixtureParameterMap.UNASSIGNED;
        }

        return channel;
    }

    /*
     * -----------------------------------------------------------------
     * Legacy profile helpers
     * -----------------------------------------------------------------
     */

    public DmxFixtureProfile getCurrentProfile() {
        return DmxFixtureProfileRegistry.getOrDefault(
                getFixtureType()
        );
    }

    public FixtureChannelLayout getCurrentChannelLayout() {
        return getCurrentProfile()
                .channelLayout();
    }

    public int getCurrentProfileChannelCount() {
        return Math.max(
                1,
                getCurrentProfile().channelCount()
        );
    }

    public int getMaximumBaseChannelForCurrentProfile() {
        return FixturePatch.getMaximumBaseChannel(
                getCurrentProfileChannelCount()
        );
    }

    /*
     * -----------------------------------------------------------------
     * DMX helpers
     * -----------------------------------------------------------------
     */

    private static int readOptionalChannel(
            DmxUniverse universe,
            int channel,
            int unsupportedDefault
    ) {
        if (universe == null
                || channel < 1
                || channel > DmxUniverse.MAX_CHANNEL) {

            return clampDmxValue(
                    unsupportedDefault
            );
        }

        return clampDmxValue(
                universe.getChannel(
                        channel
                )
        );
    }

    private static int clampDmxValue(
            int value
    ) {
        return clamp(
                value,
                FixtureOutput.MIN_VALUE,
                FixtureOutput.MAX_VALUE
        );
    }

    private static float mapDmxToSignedDegrees(
            int value,
            float maximumMagnitude
    ) {
        return mapDmxToSignedDegrees(
                (float) value,
                maximumMagnitude
        );
    }

    private static float mapDmxToSignedDegrees(
            float value,
            float maximumMagnitude
    ) {
        float clampedValue =
                Math.max(
                        FixtureOutput.MIN_VALUE,
                        Math.min(
                                FixtureOutput.MAX_VALUE,
                                value
                        )
                );

        if (clampedValue == 128.0F) {
            return 0.0F;
        }

        if (clampedValue < 128.0F) {
            return -maximumMagnitude
                    * (
                    128.0F - clampedValue
            )
                    / 128.0F;
        }

        return maximumMagnitude
                * (
                clampedValue - 128.0F
        )
                / 127.0F;
    }

    private static int mapDmxToRange(
            int value,
            int minimum,
            int maximum
    ) {
        int safeValue =
                clamp(
                        value,
                        FixtureOutput.MIN_VALUE,
                        FixtureOutput.MAX_VALUE
                );

        float normalized =
                safeValue
                        / 255.0F;

        return Math.round(
                minimum
                        + normalized
                        * (
                        maximum
                                - minimum
                )
        );
    }

    private static FixtureParameterControlMode
    parseParameterControlMode(
            String value
    ) {
        if (value == null) {
            return FixtureParameterControlMode.DMX;
        }

        if ("manual".equalsIgnoreCase(
                value
        )) {
            return FixtureParameterControlMode.MANUAL;
        }

        return FixtureParameterControlMode.DMX;
    }

    private static float clampPanTiltInterpolationTimeSeconds(
            float seconds
    ) {
        if (!Float.isFinite(
                seconds
        )) {
            return DEFAULT_PAN_TILT_INTERPOLATION_TIME_SECONDS;
        }

        return Math.max(
                MIN_PAN_TILT_INTERPOLATION_TIME_SECONDS,
                Math.min(
                        MAX_PAN_TILT_INTERPOLATION_TIME_SECONDS,
                        seconds
                )
        );
    }

    private static float lerp(
            float start,
            float end,
            float progress
    ) {
        return start
                + (
                end - start
        )
                * progress;
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
