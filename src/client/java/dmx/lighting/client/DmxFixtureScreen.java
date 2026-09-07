package dmx.lighting.client;

import dmx.lighting.DmxFixtureBlockEntity;
import dmx.lighting.DmxFixtureProfile;
import dmx.lighting.DmxFixtureProfileRegistry;
import dmx.lighting.FixtureGroupName;
import dmx.lighting.FixtureParameterMap;
import dmx.lighting.ManualFixtureOutputPayload;
import dmx.lighting.SetFixtureModePayload;
import dmx.lighting.UpdateFixtureBeamControlModesPayload;
import dmx.lighting.UpdateFixtureGroupPayload;
import dmx.lighting.UpdateFixtureMountOrientationPayload;
import dmx.lighting.UpdateFixturePanTiltInterpolationPayload;
import dmx.lighting.UpdateFixtureParameterMapPayload;
import dmx.lighting.UpdateFixturePayload;
import dmx.lighting.UpdateFixtureProfilePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parameter-centric fixture configuration and manual-control screen.
 *
 * DMX patching is represented as one explicit absolute channel
 * assignment per controllable parameter.
 *
 * Blank parameter fields mean "unassigned".
 *
 * Manual fixture output uses the same parameter model:
 *
 * - Red
 * - Green
 * - Blue
 * - White
 * - Amber
 * - Dimmer
 * - Pan Offset
 * - Tilt Offset
 * - Beam Width
 * - Beam Length
 * - Strobe
 *
 * Installation orientation and movement are intentionally separate:
 *
 * Mount Pan / Tilt
 *     Describe the physical installed orientation.
 *
 * Pan / Tilt Offset
 *     Describe live Manual or DMX movement relative to that installed
 *     orientation.
 *
 * For Pan and Tilt movement:
 *
 *     128 = installed position / zero offset
 *
 * Beam Width and Beam Length support independent parameter source
 * selection while the whole fixture is in DMX mode.
 *
 * While the whole fixture remains in DMX mode, each beam parameter can
 * independently use:
 *
 * - DMX
 * - Manual
 *
 * Whole-fixture Manual mode always uses the complete stored manual
 * output. The individual Beam Width / Beam Length source buttons are
 * therefore hidden while Manual mode is active.
 *
 * Their stored source selections are preserved and become visible
 * again when the fixture returns to DMX mode.
 */
public class DmxFixtureScreen extends Screen {

    private static final int PANEL_WIDTH =
            340;

    private static final int FIELD_WIDTH =
            190;

    private static final int FIELD_HEIGHT =
            20;

    private static final int CHANNEL_FIELD_WIDTH =
            70;

    private static final int CHANNEL_FIELD_HEIGHT =
            20;

    private static final int SLIDER_WIDTH =
            220;

    /*
     * Beam sliders are slightly narrower so their source-selection
     * buttons can sit beside them without expanding the panel.
     */
    private static final int BEAM_SLIDER_WIDTH =
            160;

    private static final int BEAM_SOURCE_BUTTON_WIDTH =
            54;

    private static final int BEAM_SOURCE_BUTTON_GAP =
            6;

    private static final int SLIDER_HEIGHT =
            20;

    private static final int SLIDER_SPACING =
            26;

    private static final int INTERPOLATION_BUTTON_WIDTH =
            110;

    private static final int INTERPOLATION_TIME_FIELD_WIDTH =
            72;

    private static final int INTERPOLATION_CONTROL_GAP =
            8;

    private static final int INTERPOLATION_ROW_HEIGHT =
            20;

    private static final int INTERPOLATION_ROW_GAP =
            8;

    private static final int VIEWPORT_MARGIN_TOP =
            24;

    private static final int VIEWPORT_MARGIN_BOTTOM =
            24;

    private static final int CONFIGURATION_TOP =
            34;

    private static final int NAME_OFFSET =
            0;

    private static final int UNIVERSE_OFFSET =
            28;

    private static final int GROUP_OFFSET =
            56;

    private static final int PROFILE_OFFSET =
            84;

    /*
     * -----------------------------------------------------------------
     * Explicit DMX parameter-map offsets
     * -----------------------------------------------------------------
     */

    private static final int PATCH_TITLE_OFFSET =
            116;

    private static final int RED_CHANNEL_OFFSET =
            138;

    private static final int GREEN_CHANNEL_OFFSET =
            164;

    private static final int BLUE_CHANNEL_OFFSET =
            190;

    private static final int WHITE_CHANNEL_OFFSET =
            216;

    private static final int AMBER_CHANNEL_OFFSET =
            242;

    private static final int DIMMER_CHANNEL_OFFSET =
            268;

    private static final int PAN_CHANNEL_OFFSET =
            294;

    private static final int TILT_CHANNEL_OFFSET =
            320;

    private static final int BEAM_WIDTH_CHANNEL_OFFSET =
            346;

    private static final int BEAM_LENGTH_CHANNEL_OFFSET =
            372;

    private static final int STROBE_CHANNEL_OFFSET =
            398;

    /*
     * -----------------------------------------------------------------
     * Installation controls
     * -----------------------------------------------------------------
     */

    private static final int INSTALLATION_TITLE_OFFSET =
            434;

    private static final int MOUNT_PAN_OFFSET =
            456;

    private static final int MOUNT_TILT_OFFSET =
            484;

    private static final int PROFILE_SUMMARY_OFFSET =
            516;

    private static final int LIVE_DMX_OFFSET =
            534;

    /*
     * -----------------------------------------------------------------
     * Orientation summary
     * -----------------------------------------------------------------
     */

    private static final int ORIENTATION_TITLE_OFFSET =
            564;

    private static final int ORIENTATION_MOUNT_OFFSET =
            582;

    private static final int ORIENTATION_ACTIVE_OFFSET =
            594;

    private static final int ORIENTATION_RESOLVED_OFFSET =
            606;

    private static final int FIRST_CONTROL_OFFSET =
            632;

    private static final int SECTION_HEIGHT =
            18;

    private static final int SECTION_BOTTOM_GAP =
            6;

    private static final int SECTION_TOP_GAP =
            12;

    private static final int BUTTON_HEIGHT =
            20;

    private final BlockPos fixturePosition;

    /*
     * -----------------------------------------------------------------
     * General configuration widgets
     * -----------------------------------------------------------------
     */

    private EditBox nameField;
    private EditBox universeField;
    private EditBox groupField;

    private Button profileButton;

    /*
     * -----------------------------------------------------------------
     * Explicit DMX parameter fields
     * -----------------------------------------------------------------
     */

    private EditBox redChannelField;
    private EditBox greenChannelField;
    private EditBox blueChannelField;
    private EditBox whiteChannelField;
    private EditBox amberChannelField;

    private EditBox dimmerChannelField;

    private EditBox panChannelField;
    private EditBox tiltChannelField;

    private EditBox beamWidthChannelField;
    private EditBox beamLengthChannelField;

    private EditBox strobeChannelField;

    /*
     * -----------------------------------------------------------------
     * Installation controls
     * -----------------------------------------------------------------
     */

    private DmxAngleSlider mountPanSlider;
    private DmxAngleSlider mountTiltSlider;

    /*
     * -----------------------------------------------------------------
     * Manual output sliders
     * -----------------------------------------------------------------
     */

    private DmxValueSlider redSlider;
    private DmxValueSlider greenSlider;
    private DmxValueSlider blueSlider;
    private DmxValueSlider whiteSlider;
    private DmxValueSlider amberSlider;

    private DmxValueSlider dimmerSlider;

    private DmxValueSlider panSlider;
    private DmxValueSlider tiltSlider;

    private DmxValueSlider manualBeamWidthSlider;
    private DmxValueSlider manualBeamLengthSlider;
    private DmxValueSlider strobeSlider;

    private Button panTiltInterpolationButton;
    private EditBox panTiltInterpolationTimeField;

    private boolean panTiltInterpolationEnabled;

    private float panTiltInterpolationTimeSeconds =
            DmxFixtureBlockEntity
                    .DEFAULT_PAN_TILT_INTERPOLATION_TIME_SECONDS;

    /*
     * -----------------------------------------------------------------
     * Per-parameter beam source controls
     * -----------------------------------------------------------------
     */

    private Button beamWidthControlButton;
    private Button beamLengthControlButton;

    private String beamWidthControlMode =
            "dmx";

    private String beamLengthControlMode =
            "dmx";

    /*
     * -----------------------------------------------------------------
     * Mode and screen controls
     * -----------------------------------------------------------------
     */

    private Button dmxModeButton;
    private Button manualModeButton;

    private Button saveConfigButton;
    private Button doneButton;

    /*
     * -----------------------------------------------------------------
     * Profiles
     * -----------------------------------------------------------------
     */

    private List<DmxFixtureProfile> availableProfiles =
            List.of();

    private int selectedProfileIndex;

    private String fixtureProfile =
            DmxFixtureProfileRegistry.DEFAULT_PROFILE_ID;

    private String controlMode =
            "dmx";

    /*
     * -----------------------------------------------------------------
     * Stored installation orientation
     * -----------------------------------------------------------------
     */

    private float mountPanDegrees;
    private float mountTiltDegrees;

    /*
     * -----------------------------------------------------------------
     * Stored manual values
     * -----------------------------------------------------------------
     */

    private int manualRed =
            255;

    private int manualGreen =
            255;

    private int manualBlue =
            255;

    private int manualWhite;
    private int manualAmber;

    private int manualDimmer =
            255;

    private int manualPan =
            128;

    private int manualTilt =
            128;

    private int manualBeamWidth;
    private int manualBeamLength;

    private int manualStrobe;

    /*
     * -----------------------------------------------------------------
     * Dynamically calculated content locations
     * -----------------------------------------------------------------
     */

    private int colorSectionY =
            -1;

    private int dimmerSectionY =
            -1;

    private int positionSectionY =
            -1;

    private int interpolationControlsY =
            -1;

    private int beamSectionY =
            -1;

    private int previewY;
    private int modeButtonsY;
    private int behaviorMessageY;
    private int saveButtonsY;
    private int statusMessageY;
    private int positionLabelY;

    private int contentHeight;

    /*
     * -----------------------------------------------------------------
     * Scrolling
     * -----------------------------------------------------------------
     */

    private int scrollOffset;
    private int maximumScroll;

    /*
     * -----------------------------------------------------------------
     * Status
     * -----------------------------------------------------------------
     */

    private Component statusMessage =
            Component.empty();

    private int statusColor =
            0xFFFFFFFF;

    private boolean screenReady;

    public DmxFixtureScreen(
            BlockPos fixturePosition
    ) {
        super(
                Component.literal(
                        "DMX Fixture"
                )
        );

        this.fixturePosition =
                fixturePosition.immutable();
    }

    @Override
    protected void init() {
        screenReady =
                false;

        DmxFixtureProfileRegistry.initialize();

        availableProfiles =
                DmxFixtureProfileRegistry.getAll()
                        .stream()
                        .filter(
                                profile -> !"dmx_pixel".equals(
                                        profile.id()
                                )
                        )
                        .collect(
                                Collectors.toUnmodifiableList()
                        );

        DmxFixtureBlockEntity fixture =
                getFixture();

        loadFixtureValues(
                fixture
        );

        selectProfileById(
                fixtureProfile
        );

        createConfigurationWidgets(
                fixture
        );

        createParameterMapWidgets(
                fixture
        );

        createManualSliders();

        createPanTiltInterpolationControls();

        createBeamControlButtons();
        createModeButtons();
        createBottomButtons();

        rebuildDynamicLayout();

        setInitialFocus(
                nameField
        );

        screenReady =
                true;
    }

    /*
     * -----------------------------------------------------------------
     * Initialization
     * -----------------------------------------------------------------
     */

    private void loadFixtureValues(
            DmxFixtureBlockEntity fixture
    ) {
        if (fixture == null) {
            return;
        }

        fixtureProfile =
                fixture.getFixtureType();

        controlMode =
                fixture.getControlMode()
                        .getSerializedName();

        beamWidthControlMode =
                fixture.isBeamWidthManualControlled()
                        ? "manual"
                        : "dmx";

        beamLengthControlMode =
                fixture.isBeamLengthManualControlled()
                        ? "manual"
                        : "dmx";

        mountPanDegrees =
                fixture.getMountPanDegrees();

        mountTiltDegrees =
                fixture.getMountTiltDegrees();

        panTiltInterpolationEnabled =
                fixture.isPanTiltInterpolationEnabled();

        panTiltInterpolationTimeSeconds =
                fixture.getPanTiltInterpolationTimeSeconds();

        manualRed =
                fixture.getManualRed();

        manualGreen =
                fixture.getManualGreen();

        manualBlue =
                fixture.getManualBlue();

        manualWhite =
                fixture.getManualWhite();

        manualAmber =
                fixture.getManualAmber();

        manualDimmer =
                fixture.getManualDimmer();

        manualPan =
                fixture.getManualPan();

        manualTilt =
                fixture.getManualTilt();

        manualBeamWidth =
                fixture.getManualBeamWidth();

        manualBeamLength =
                fixture.getManualBeamLength();

        manualStrobe =
                fixture.getManualStrobe();
    }

    private void createConfigurationWidgets(
            DmxFixtureBlockEntity fixture
    ) {
        int panelLeft =
                getPanelLeft();

        int fieldX =
                panelLeft
                        + 135;

        int contentTop =
                VIEWPORT_MARGIN_TOP
                        + CONFIGURATION_TOP;

        nameField =
                new EditBox(
                        font,
                        fieldX,
                        contentTop
                                + NAME_OFFSET,
                        FIELD_WIDTH,
                        FIELD_HEIGHT,
                        Component.literal(
                                "Fixture Name"
                        )
                );

        nameField.setMaxLength(
                64
        );

        nameField.setValue(
                fixture == null
                        ? ""
                        : fixture.getFixtureName()
        );

        addRenderableWidget(
                nameField
        );

        universeField =
                new EditBox(
                        font,
                        fieldX,
                        contentTop
                                + UNIVERSE_OFFSET,
                        FIELD_WIDTH,
                        FIELD_HEIGHT,
                        Component.literal(
                                "Universe"
                        )
                );

        universeField.setMaxLength(
                4
        );

        universeField.setValue(
                fixture == null
                        ? "1"
                        : Integer.toString(
                                fixture.getUniverse()
                        )
        );

        addRenderableWidget(
                universeField
        );

        groupField =
                new EditBox(
                        font,
                        fieldX,
                        contentTop
                                + GROUP_OFFSET,
                        FIELD_WIDTH,
                        FIELD_HEIGHT,
                        Component.literal(
                                "Fixture Group"
                        )
                );

        groupField.setMaxLength(
                FixtureGroupName.MAX_LENGTH
        );

        groupField.setValue(
                fixture == null
                        ? ""
                        : fixture.getGroupDisplayName()
        );

        addRenderableWidget(
                groupField
        );

        profileButton =
                Button.builder(
                        getProfileButtonMessage(),
                        button ->
                                selectNextProfile()
                )
                .bounds(
                        fieldX,
                        contentTop
                                + PROFILE_OFFSET,
                        FIELD_WIDTH,
                        FIELD_HEIGHT
                )
                .build();

        addRenderableWidget(
                profileButton
        );

        int configurationSliderX =
                width / 2
                        - SLIDER_WIDTH / 2;

        mountPanSlider =
                new DmxAngleSlider(
                        configurationSliderX,
                        contentTop
                                + MOUNT_PAN_OFFSET,
                        SLIDER_WIDTH,
                        SLIDER_HEIGHT,
                        "Mount Pan",
                        Math.round(
                                mountPanDegrees
                        ),
                        value -> {
                            mountPanDegrees =
                                    value;

                            handleMountOrientationChanged();
                        }
                );

        addRenderableWidget(
                mountPanSlider
        );

        mountTiltSlider =
                new DmxAngleSlider(
                        configurationSliderX,
                        contentTop
                                + MOUNT_TILT_OFFSET,
                        SLIDER_WIDTH,
                        SLIDER_HEIGHT,
                        "Mount Tilt",
                        Math.round(
                                mountTiltDegrees
                        ),
                        value -> {
                            mountTiltDegrees =
                                    value;

                            handleMountOrientationChanged();
                        }
                );

        addRenderableWidget(
                mountTiltSlider
        );
    }

    /*
     * -----------------------------------------------------------------
     * Parameter-map widgets
     * -----------------------------------------------------------------
     */

    private void createParameterMapWidgets(
            DmxFixtureBlockEntity fixture
    ) {
        FixtureParameterMap map =
                fixture == null
                        ? new FixtureParameterMap()
                        : fixture.getParameterMap();

        int contentTop =
                VIEWPORT_MARGIN_TOP
                        + CONFIGURATION_TOP;

        int fieldX =
                getPanelLeft()
                        + 220;

        redChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + RED_CHANNEL_OFFSET,
                        map.getRedChannel(),
                        "Red DMX Channel"
                );

        greenChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + GREEN_CHANNEL_OFFSET,
                        map.getGreenChannel(),
                        "Green DMX Channel"
                );

        blueChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + BLUE_CHANNEL_OFFSET,
                        map.getBlueChannel(),
                        "Blue DMX Channel"
                );

        whiteChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + WHITE_CHANNEL_OFFSET,
                        map.getWhiteChannel(),
                        "White DMX Channel"
                );

        amberChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + AMBER_CHANNEL_OFFSET,
                        map.getAmberChannel(),
                        "Amber DMX Channel"
                );

        dimmerChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + DIMMER_CHANNEL_OFFSET,
                        map.getDimmerChannel(),
                        "Dimmer DMX Channel"
                );

        panChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + PAN_CHANNEL_OFFSET,
                        map.getPanChannel(),
                        "Pan Offset DMX Channel"
                );

        tiltChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + TILT_CHANNEL_OFFSET,
                        map.getTiltChannel(),
                        "Tilt Offset DMX Channel"
                );

        beamWidthChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + BEAM_WIDTH_CHANNEL_OFFSET,
                        map.getBeamWidthChannel(),
                        "Beam Width DMX Channel"
                );

        beamLengthChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + BEAM_LENGTH_CHANNEL_OFFSET,
                        map.getBeamLengthChannel(),
                        "Beam Length DMX Channel"
                );

        strobeChannelField =
                createChannelField(
                        fieldX,
                        contentTop
                                + STROBE_CHANNEL_OFFSET,
                        map.getStrobeChannel(),
                        "Strobe DMX Channel"
                );
    }

    private EditBox createChannelField(
            int x,
            int y,
            int channel,
            String narration
    ) {
        EditBox field =
                new EditBox(
                        font,
                        x,
                        y,
                        CHANNEL_FIELD_WIDTH,
                        CHANNEL_FIELD_HEIGHT,
                        Component.literal(
                                narration
                        )
                );

        field.setMaxLength(
                3
        );

        field.setValue(
                formatEditableParameterChannel(
                        channel
                )
        );

        addRenderableWidget(
                field
        );

        return field;
    }

    private static String formatEditableParameterChannel(
            int channel
    ) {
        if (!FixtureParameterMap.isAssigned(
                channel
        )) {
            return "";
        }

        return Integer.toString(
                channel
        );
    }

    /*
     * -----------------------------------------------------------------
     * Manual controls
     * -----------------------------------------------------------------
     */

    private void createManualSliders() {
        int normalSliderX =
                width / 2
                        - SLIDER_WIDTH / 2;

        int beamSliderX =
                normalSliderX;

        redSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Red",
                        manualRed,
                        value ->
                                manualRed = value
                );

        greenSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Green",
                        manualGreen,
                        value ->
                                manualGreen = value
                );

        blueSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Blue",
                        manualBlue,
                        value ->
                                manualBlue = value
                );

        whiteSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "White",
                        manualWhite,
                        value ->
                                manualWhite = value
                );

        amberSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Amber",
                        manualAmber,
                        value ->
                                manualAmber = value
                );

        dimmerSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Dimmer",
                        manualDimmer,
                        value ->
                                manualDimmer = value
                );

        panSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Pan Offset",
                        manualPan,
                        value ->
                                manualPan = value
                );

        tiltSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Tilt Offset",
                        manualTilt,
                        value ->
                                manualTilt = value
                );

        manualBeamWidthSlider =
                createSlider(
                        beamSliderX,
                        BEAM_SLIDER_WIDTH,
                        "Beam Width",
                        manualBeamWidth,
                        value ->
                                manualBeamWidth = value
                );

        manualBeamLengthSlider =
                createSlider(
                        beamSliderX,
                        BEAM_SLIDER_WIDTH,
                        "Beam Length",
                        manualBeamLength,
                        value ->
                                manualBeamLength = value
                );

        strobeSlider =
                createSlider(
                        normalSliderX,
                        SLIDER_WIDTH,
                        "Strobe",
                        manualStrobe,
                        value ->
                                manualStrobe = value
                );
    }

    private DmxValueSlider createSlider(
            int x,
            int sliderWidth,
            String label,
            int initialValue,
            java.util.function.IntConsumer valueConsumer
    ) {
        DmxValueSlider slider =
                new DmxValueSlider(
                        x,
                        0,
                        sliderWidth,
                        SLIDER_HEIGHT,
                        label,
                        initialValue,
                        value -> {
                            valueConsumer.accept(
                                    value
                            );

                            handleSliderChanged();
                        }
                );

        addRenderableWidget(
                slider
        );

        return slider;
    }

    /*
     * -----------------------------------------------------------------
     * Pan/Tilt interpolation controls
     * -----------------------------------------------------------------
     */

    private void createPanTiltInterpolationControls() {
        int controlsLeft =
                width / 2
                        - SLIDER_WIDTH / 2;

        panTiltInterpolationButton =
                Button.builder(
                        getPanTiltInterpolationButtonMessage(),
                        button -> togglePanTiltInterpolation()
                )
                .bounds(
                        controlsLeft,
                        0,
                        INTERPOLATION_BUTTON_WIDTH,
                        INTERPOLATION_ROW_HEIGHT
                )
                .build();

        addRenderableWidget(
                panTiltInterpolationButton
        );

        panTiltInterpolationTimeField =
                new EditBox(
                        font,
                        controlsLeft
                                + INTERPOLATION_BUTTON_WIDTH
                                + INTERPOLATION_CONTROL_GAP,
                        0,
                        INTERPOLATION_TIME_FIELD_WIDTH,
                        INTERPOLATION_ROW_HEIGHT,
                        Component.literal(
                                "Pan/Tilt smoothing time in seconds"
                        )
                );

        panTiltInterpolationTimeField.setMaxLength(
                6
        );

        panTiltInterpolationTimeField.setValue(
                formatInterpolationSeconds(
                        panTiltInterpolationTimeSeconds
                )
        );

        addRenderableWidget(
                panTiltInterpolationTimeField
        );
    }

    private void togglePanTiltInterpolation() {
        boolean newEnabled =
                !panTiltInterpolationEnabled;

        if (newEnabled
                && !validatePanTiltInterpolationTime()) {

            setErrorStatus(
                    "Smoothing time must be between 0.05 and 60 seconds."
            );

            return;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixturePanTiltInterpolationPayload.TYPE
        )) {
            setErrorStatus(
                    "Pan/Tilt smoothing packet is unavailable."
            );

            return;
        }

        panTiltInterpolationEnabled =
                newEnabled;

        updatePanTiltInterpolationButtonMessage();

        ClientPlayNetworking.send(
                new UpdateFixturePanTiltInterpolationPayload(
                        fixturePosition,
                        panTiltInterpolationEnabled,
                        panTiltInterpolationTimeSeconds
                )
        );

        statusMessage =
                Component.literal(
                        panTiltInterpolationEnabled
                                ? "Pan/Tilt smoothing enabled."
                                : "Pan/Tilt smoothing disabled."
                );

        statusColor =
                0xFF55FF55;
    }

    private boolean sendPanTiltInterpolation() {
        if (!validatePanTiltInterpolationTime()) {
            setErrorStatus(
                    "Smoothing time must be between 0.05 and 60 seconds."
            );

            return false;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixturePanTiltInterpolationPayload.TYPE
        )) {
            setErrorStatus(
                    "Pan/Tilt smoothing packet is unavailable."
            );

            return false;
        }

        ClientPlayNetworking.send(
                new UpdateFixturePanTiltInterpolationPayload(
                        fixturePosition,
                        panTiltInterpolationEnabled,
                        panTiltInterpolationTimeSeconds
                )
        );

        return true;
    }

    private boolean validatePanTiltInterpolationTime() {
        Double parsed =
                parseDouble(
                        panTiltInterpolationTimeField.getValue()
                );

        boolean valid =
                parsed != null
                        && parsed
                        >= DmxFixtureBlockEntity
                        .MIN_PAN_TILT_INTERPOLATION_TIME_SECONDS
                        && parsed
                        <= DmxFixtureBlockEntity
                        .MAX_PAN_TILT_INTERPOLATION_TIME_SECONDS;

        panTiltInterpolationTimeField.setTextColor(
                valid
                        ? 0xFFFFFFFF
                        : 0xFFFF5555
        );

        if (valid) {
            panTiltInterpolationTimeSeconds =
                    parsed.floatValue();
        }

        return valid;
    }

    private Component getPanTiltInterpolationButtonMessage() {
        return Component.literal(
                panTiltInterpolationEnabled
                        ? "Smooth: On"
                        : "Smooth: Off"
        );
    }

    private void updatePanTiltInterpolationButtonMessage() {
        if (panTiltInterpolationButton != null) {
            panTiltInterpolationButton.setMessage(
                    getPanTiltInterpolationButtonMessage()
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Beam parameter source buttons
     * -----------------------------------------------------------------
     */

    private void createBeamControlButtons() {
        int beamButtonX =
                getBeamSourceButtonX();

        beamWidthControlButton =
                Button.builder(
                        getBeamWidthControlButtonMessage(),
                        button ->
                                toggleBeamWidthControlMode()
                )
                .bounds(
                        beamButtonX,
                        0,
                        BEAM_SOURCE_BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .build();

        beamLengthControlButton =
                Button.builder(
                        getBeamLengthControlButtonMessage(),
                        button ->
                                toggleBeamLengthControlMode()
                )
                .bounds(
                        beamButtonX,
                        0,
                        BEAM_SOURCE_BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .build();

        addRenderableWidget(
                beamWidthControlButton
        );

        addRenderableWidget(
                beamLengthControlButton
        );
    }

    private int getBeamSourceButtonX() {
        return width / 2
                - SLIDER_WIDTH / 2
                + BEAM_SLIDER_WIDTH
                + BEAM_SOURCE_BUTTON_GAP;
    }

    private void toggleBeamWidthControlMode() {
        String newMode =
                isBeamWidthManualControlled()
                        ? "dmx"
                        : "manual";

        if ("manual".equals(
                newMode
        )) {
            if (!sendManualOutput(
                    false
            )) {
                return;
            }
        }

        String oldMode =
                beamWidthControlMode;

        beamWidthControlMode =
                newMode;

        updateBeamControlButtonMessages();

        if (!sendBeamControlModes(
                false
        )) {
            beamWidthControlMode =
                    oldMode;

            updateBeamControlButtonMessages();

            return;
        }

        statusMessage =
                Component.literal(
                        "Beam Width now uses "
                                + getReadableBeamControlMode(
                                        beamWidthControlMode
                                )
                                + "."
                );

        statusColor =
                0xFF55FF55;
    }

    private void toggleBeamLengthControlMode() {
        String newMode =
                isBeamLengthManualControlled()
                        ? "dmx"
                        : "manual";

        if ("manual".equals(
                newMode
        )) {
            if (!sendManualOutput(
                    false
            )) {
                return;
            }
        }

        String oldMode =
                beamLengthControlMode;

        beamLengthControlMode =
                newMode;

        updateBeamControlButtonMessages();

        if (!sendBeamControlModes(
                false
        )) {
            beamLengthControlMode =
                    oldMode;

            updateBeamControlButtonMessages();

            return;
        }

        statusMessage =
                Component.literal(
                        "Beam Length now uses "
                                + getReadableBeamControlMode(
                                        beamLengthControlMode
                                )
                                + "."
                );

        statusColor =
                0xFF55FF55;
    }

    private boolean sendBeamControlModes(
            boolean showStatus
    ) {
        if (!ClientPlayNetworking.canSend(
                UpdateFixtureBeamControlModesPayload.TYPE
        )) {
            if (showStatus) {
                setErrorStatus(
                        "Beam control-mode packet is unavailable."
                );
            }

            return false;
        }

        ClientPlayNetworking.send(
                new UpdateFixtureBeamControlModesPayload(
                        fixturePosition,
                        beamWidthControlMode,
                        beamLengthControlMode
                )
        );

        return true;
    }

    private Component getBeamWidthControlButtonMessage() {
        return Component.literal(
                isBeamWidthManualControlled()
                        ? "Manual"
                        : "DMX"
        );
    }

    private Component getBeamLengthControlButtonMessage() {
        return Component.literal(
                isBeamLengthManualControlled()
                        ? "Manual"
                        : "DMX"
        );
    }

    private void updateBeamControlButtonMessages() {
        if (beamWidthControlButton != null) {
            beamWidthControlButton.setMessage(
                    getBeamWidthControlButtonMessage()
            );
        }

        if (beamLengthControlButton != null) {
            beamLengthControlButton.setMessage(
                    getBeamLengthControlButtonMessage()
            );
        }
    }

    private boolean isBeamWidthManualControlled() {
        return "manual".equalsIgnoreCase(
                beamWidthControlMode
        );
    }

    private boolean isBeamLengthManualControlled() {
        return "manual".equalsIgnoreCase(
                beamLengthControlMode
        );
    }

    private static String getReadableBeamControlMode(
            String mode
    ) {
        return "manual".equalsIgnoreCase(
                mode
        )
                ? "Manual control"
                : "DMX";
    }

    /*
     * -----------------------------------------------------------------
     * Whole-fixture mode buttons
     * -----------------------------------------------------------------
     */

    private void createModeButtons() {
        dmxModeButton =
                Button.builder(
                        Component.literal(
                                "DMX Mode"
                        ),
                        button ->
                                setFixtureMode(
                                        "dmx"
                                )
                )
                .bounds(
                        width / 2
                                - 110,
                        0,
                        105,
                        BUTTON_HEIGHT
                )
                .build();

        manualModeButton =
                Button.builder(
                        Component.literal(
                                "Manual Mode"
                        ),
                        button ->
                                setFixtureMode(
                                        "manual"
                                )
                )
                .bounds(
                        width / 2
                                + 5,
                        0,
                        105,
                        BUTTON_HEIGHT
                )
                .build();

        addRenderableWidget(
                dmxModeButton
        );

        addRenderableWidget(
                manualModeButton
        );
    }

    private void createBottomButtons() {
        saveConfigButton =
                Button.builder(
                        Component.literal(
                                "Save Config"
                        ),
                        button ->
                                saveConfiguration()
                )
                .bounds(
                        width / 2
                                - 110,
                        0,
                        105,
                        BUTTON_HEIGHT
                )
                .build();

        doneButton =
                Button.builder(
                        Component.literal(
                                "Done"
                        ),
                        button ->
                                onClose()
                )
                .bounds(
                        width / 2
                                + 5,
                        0,
                        105,
                        BUTTON_HEIGHT
                )
                .build();

        addRenderableWidget(
                saveConfigButton
        );

        addRenderableWidget(
                doneButton
        );
    }

    /*
     * -----------------------------------------------------------------
     * Profiles
     * -----------------------------------------------------------------
     */

    private void selectProfileById(
            String profileId
    ) {
        if (availableProfiles.isEmpty()) {
            selectedProfileIndex =
                    0;

            fixtureProfile =
                    DmxFixtureProfileRegistry.DEFAULT_PROFILE_ID;

            return;
        }

        String resolvedProfileId =
                DmxFixtureProfileRegistry.resolveProfileId(
                        profileId
                );

        for (
                int index = 0;
                index < availableProfiles.size();
                index++
        ) {
            DmxFixtureProfile profile =
                    availableProfiles.get(
                            index
                    );

            if (profile.id().equalsIgnoreCase(
                    resolvedProfileId
            )) {
                selectedProfileIndex =
                        index;

                fixtureProfile =
                        profile.id();

                return;
            }
        }

        selectedProfileIndex =
                0;

        fixtureProfile =
                availableProfiles
                        .get(
                                0
                        )
                        .id();
    }

    private void selectNextProfile() {
        if (availableProfiles.isEmpty()) {
            return;
        }

        selectedProfileIndex =
                (
                        selectedProfileIndex
                                + 1
                )
                        % availableProfiles.size();

        DmxFixtureProfile profile =
                getSelectedProfile();

        if (profile == null) {
            return;
        }

        fixtureProfile =
                profile.id();

        profileButton.setMessage(
                getProfileButtonMessage()
        );

        statusMessage =
                Component.literal(
                        "Selected visual profile: "
                                + profile.displayName()
                );

        statusColor =
                0xFFFFFF55;

        rebuildDynamicLayout();
    }

    private DmxFixtureProfile getSelectedProfile() {
        if (availableProfiles.isEmpty()) {
            return null;
        }

        if (selectedProfileIndex < 0
                || selectedProfileIndex
                >= availableProfiles.size()) {

            selectedProfileIndex =
                    0;
        }

        return availableProfiles.get(
                selectedProfileIndex
        );
    }

    private Component getProfileButtonMessage() {
        DmxFixtureProfile profile =
                getSelectedProfile();

        if (profile == null) {
            return Component.literal(
                    "No Profiles"
            );
        }

        return Component.literal(
                profile.displayName()
                        + "  >"
        );
    }

    /*
     * -----------------------------------------------------------------
     * Dynamic manual-control layout
     * -----------------------------------------------------------------
     */

    private void rebuildDynamicLayout() {
        int cursor =
                VIEWPORT_MARGIN_TOP
                        + CONFIGURATION_TOP
                        + FIRST_CONTROL_OFFSET;

        colorSectionY =
                cursor;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        cursor =
                placeSlider(
                        redSlider,
                        cursor,
                        true
                );

        cursor =
                placeSlider(
                        greenSlider,
                        cursor,
                        true
                );

        cursor =
                placeSlider(
                        blueSlider,
                        cursor,
                        true
                );

        cursor =
                placeSlider(
                        whiteSlider,
                        cursor,
                        true
                );

        cursor =
                placeSlider(
                        amberSlider,
                        cursor,
                        true
                );

        cursor +=
                SECTION_TOP_GAP;

        dimmerSectionY =
                cursor;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        cursor =
                placeSlider(
                        dimmerSlider,
                        cursor,
                        true
                );

        cursor +=
                SECTION_TOP_GAP;

        positionSectionY =
                cursor;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        cursor =
                placeSlider(
                        panSlider,
                        cursor,
                        true
                );

        cursor =
                placeSlider(
                        tiltSlider,
                        cursor,
                        true
                );

        interpolationControlsY =
                cursor;

        cursor +=
                INTERPOLATION_ROW_HEIGHT
                        + INTERPOLATION_ROW_GAP;

        cursor +=
                SECTION_TOP_GAP;

        beamSectionY =
                cursor;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        int beamWidthY =
                cursor;

        cursor =
                placeSlider(
                        manualBeamWidthSlider,
                        cursor,
                        true
                );

        setWidgetPosition(
                beamWidthControlButton,
                getBeamSourceButtonX(),
                beamWidthY
                        - scrollOffset,
                isDmxModeSelected()
        );

        int beamLengthY =
                cursor;

        cursor =
                placeSlider(
                        manualBeamLengthSlider,
                        cursor,
                        true
                );

        setWidgetPosition(
                beamLengthControlButton,
                getBeamSourceButtonX(),
                beamLengthY
                        - scrollOffset,
                isDmxModeSelected()
        );

        cursor =
                placeSlider(
                        strobeSlider,
                        cursor,
                        true
                );

        cursor +=
                SECTION_TOP_GAP;

        modeButtonsY =
                cursor;

        cursor +=
                BUTTON_HEIGHT
                        + 12;

        previewY =
                cursor;

        cursor +=
                48;

        behaviorMessageY =
                cursor;

        cursor +=
                36;

        saveButtonsY =
                cursor;

        cursor +=
                BUTTON_HEIGHT
                        + 16;

        statusMessageY =
                cursor;

        cursor +=
                20;

        positionLabelY =
                cursor;

        cursor +=
                24;

        contentHeight =
                cursor
                        - VIEWPORT_MARGIN_TOP;

        calculateMaximumScroll();

        updateWidgetPositions();
        updateModeButtonStates();
        updateBeamControlButtonMessages();
        updatePanTiltInterpolationButtonMessage();
    }

    private int placeSlider(
            AbstractWidget slider,
            int y,
            boolean supported
    ) {
        if (!supported) {
            hideWidget(
                    slider
            );

            return y;
        }

        slider.setY(
                y
                        - scrollOffset
        );

        slider.visible =
                isWidgetInsideViewport(
                        slider
                );

        slider.active =
                true;

        return y
                + SLIDER_SPACING;
    }

    private void calculateMaximumScroll() {
        int viewportHeight =
                height
                        - VIEWPORT_MARGIN_TOP
                        - VIEWPORT_MARGIN_BOTTOM;

        maximumScroll =
                Math.max(
                        0,
                        contentHeight
                                - viewportHeight
                );

        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                maximumScroll,
                                scrollOffset
                        )
                );
    }

    private void updateWidgetPositions() {
        int panelLeft =
                getPanelLeft();

        int generalFieldX =
                panelLeft
                        + 135;

        int channelFieldX =
                panelLeft
                        + 220;

        int contentTop =
                VIEWPORT_MARGIN_TOP
                        + CONFIGURATION_TOP;

        setWidgetPosition(
                nameField,
                generalFieldX,
                contentTop
                        + NAME_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                universeField,
                generalFieldX,
                contentTop
                        + UNIVERSE_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                groupField,
                generalFieldX,
                contentTop
                        + GROUP_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                profileButton,
                generalFieldX,
                contentTop
                        + PROFILE_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                redChannelField,
                channelFieldX,
                contentTop
                        + RED_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                greenChannelField,
                channelFieldX,
                contentTop
                        + GREEN_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                blueChannelField,
                channelFieldX,
                contentTop
                        + BLUE_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                whiteChannelField,
                channelFieldX,
                contentTop
                        + WHITE_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                amberChannelField,
                channelFieldX,
                contentTop
                        + AMBER_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                dimmerChannelField,
                channelFieldX,
                contentTop
                        + DIMMER_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                panChannelField,
                channelFieldX,
                contentTop
                        + PAN_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                tiltChannelField,
                channelFieldX,
                contentTop
                        + TILT_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                beamWidthChannelField,
                channelFieldX,
                contentTop
                        + BEAM_WIDTH_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                beamLengthChannelField,
                channelFieldX,
                contentTop
                        + BEAM_LENGTH_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                strobeChannelField,
                channelFieldX,
                contentTop
                        + STROBE_CHANNEL_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                mountPanSlider,
                width / 2
                        - SLIDER_WIDTH / 2,
                contentTop
                        + MOUNT_PAN_OFFSET
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                mountTiltSlider,
                width / 2
                        - SLIDER_WIDTH / 2,
                contentTop
                        + MOUNT_TILT_OFFSET
                        - scrollOffset,
                true
        );

        setSliderPosition(
                redSlider,
                true
        );

        setSliderPosition(
                greenSlider,
                true
        );

        setSliderPosition(
                blueSlider,
                true
        );

        setSliderPosition(
                whiteSlider,
                true
        );

        setSliderPosition(
                amberSlider,
                true
        );

        setSliderPosition(
                dimmerSlider,
                true
        );

        setSliderPosition(
                panSlider,
                true
        );

        setSliderPosition(
                tiltSlider,
                true
        );

        int interpolationLeft =
                width / 2
                        - SLIDER_WIDTH / 2;

        setWidgetPosition(
                panTiltInterpolationButton,
                interpolationLeft,
                interpolationControlsY
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                panTiltInterpolationTimeField,
                interpolationLeft
                        + INTERPOLATION_BUTTON_WIDTH
                        + INTERPOLATION_CONTROL_GAP,
                interpolationControlsY
                        - scrollOffset,
                true
        );

        setSliderPosition(
                manualBeamWidthSlider,
                true
        );

        setSliderPosition(
                manualBeamLengthSlider,
                true
        );

        setSliderPosition(
                strobeSlider,
                true
        );

        boolean showBeamSourceControls =
                isDmxModeSelected();

        if (manualBeamWidthSlider != null) {
            setWidgetPosition(
                    beamWidthControlButton,
                    getBeamSourceButtonX(),
                    manualBeamWidthSlider.getY(),
                    showBeamSourceControls
            );
        }

        if (manualBeamLengthSlider != null) {
            setWidgetPosition(
                    beamLengthControlButton,
                    getBeamSourceButtonX(),
                    manualBeamLengthSlider.getY(),
                    showBeamSourceControls
            );
        }

        setWidgetPosition(
                dmxModeButton,
                width / 2
                        - 110,
                modeButtonsY
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                manualModeButton,
                width / 2
                        + 5,
                modeButtonsY
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                saveConfigButton,
                width / 2
                        - 110,
                saveButtonsY
                        - scrollOffset,
                true
        );

        setWidgetPosition(
                doneButton,
                width / 2
                        + 5,
                saveButtonsY
                        - scrollOffset,
                true
        );
    }

    private void setSliderPosition(
            AbstractWidget slider,
            boolean supported
    ) {
        if (!supported) {
            hideWidget(
                    slider
            );

            return;
        }

        slider.visible =
                isWidgetInsideViewport(
                        slider
                );

        slider.active =
                true;
    }

    private void setWidgetPosition(
            AbstractWidget widget,
            int x,
            int y,
            boolean supported
    ) {
        if (widget == null) {
            return;
        }

        widget.setX(
                x
        );

        widget.setY(
                y
        );

        widget.visible =
                supported
                        && isWidgetInsideViewport(
                                widget
                        );

        widget.active =
                supported;
    }

    private boolean isWidgetInsideViewport(
            AbstractWidget widget
    ) {
        return widget.getY()
                + widget.getHeight()
                >= VIEWPORT_MARGIN_TOP
                && widget.getY()
                <= height
                - VIEWPORT_MARGIN_BOTTOM;
    }

    private void hideWidget(
            AbstractWidget widget
    ) {
        if (widget == null) {
            return;
        }

        widget.visible =
                false;

        widget.active =
                false;
    }

    /*
     * -----------------------------------------------------------------
     * Installation orientation
     * -----------------------------------------------------------------
     */

    private void handleMountOrientationChanged() {
        if (!screenReady) {
            return;
        }

        sendMountOrientation(
                false
        );
    }

    private boolean sendMountOrientation(
            boolean showStatus
    ) {
        if (!ClientPlayNetworking.canSend(
                UpdateFixtureMountOrientationPayload.TYPE
        )) {
            if (showStatus) {
                setErrorStatus(
                        "Mount-orientation packet is unavailable."
                );
            }

            return false;
        }

        if (mountPanSlider == null
                || mountTiltSlider == null) {

            return false;
        }

        mountPanDegrees =
                mountPanSlider.getDegrees();

        mountTiltDegrees =
                mountTiltSlider.getDegrees();

        ClientPlayNetworking.send(
                new UpdateFixtureMountOrientationPayload(
                        fixturePosition,
                        mountPanDegrees,
                        mountTiltDegrees
                )
        );

        return true;
    }

    /*
     * -----------------------------------------------------------------
     * Manual output
     * -----------------------------------------------------------------
     */

    private void handleSliderChanged() {
        if (!screenReady) {
            return;
        }

        if (isManualModeSelected()) {
            sendManualOutput(
                    false
            );

            return;
        }

        if (isBeamWidthManualControlled()
                || isBeamLengthManualControlled()) {

            sendManualOutput(
                    false
            );
        }
    }

    private boolean sendManualOutput(
            boolean showStatus
    ) {
        if (!ClientPlayNetworking.canSend(
                ManualFixtureOutputPayload.TYPE
        )) {
            statusMessage =
                    Component.literal(
                            "Manual output packet is unavailable."
                    );

            statusColor =
                    0xFFFF5555;

            return false;
        }

        ClientPlayNetworking.send(
                new ManualFixtureOutputPayload(
                        fixturePosition,
                        manualRed,
                        manualGreen,
                        manualBlue,
                        manualWhite,
                        manualAmber,
                        manualDimmer,
                        manualPan,
                        manualTilt,
                        manualBeamWidth,
                        manualBeamLength,
                        manualStrobe
                )
        );

        if (showStatus) {
            statusMessage =
                    Component.literal(
                            "Manual fixture values applied."
                    );

            statusColor =
                    0xFF55FF55;
        }

        return true;
    }

    /*
     * -----------------------------------------------------------------
     * Whole-fixture control mode
     * -----------------------------------------------------------------
     */

    private void setFixtureMode(
            String mode
    ) {
        if (!ClientPlayNetworking.canSend(
                SetFixtureModePayload.TYPE
        )) {
            statusMessage =
                    Component.literal(
                            "Mode packet is unavailable."
                    );

            statusColor =
                    0xFFFF5555;

            return;
        }

        if ("manual".equalsIgnoreCase(
                mode
        )) {
            if (!sendManualOutput(
                    false
            )) {
                return;
            }
        }

        ClientPlayNetworking.send(
                new SetFixtureModePayload(
                        fixturePosition,
                        mode
                )
        );

        controlMode =
                mode;

        updateModeButtonStates();
        updateWidgetPositions();

        statusMessage =
                Component.literal(
                        isManualModeSelected()
                                ? "Manual mode active."
                                : "Fixture is following DMX with parameter overrides."
                );

        statusColor =
                0xFF55FF55;
    }

    private void updateModeButtonStates() {
        if (dmxModeButton != null) {
            dmxModeButton.active =
                    !isDmxModeSelected();
        }

        if (manualModeButton != null) {
            manualModeButton.active =
                    !isManualModeSelected();
        }
    }

    private boolean isDmxModeSelected() {
        return "dmx".equalsIgnoreCase(
                controlMode
        );
    }

    private boolean isManualModeSelected() {
        return "manual".equalsIgnoreCase(
                controlMode
        );
    }

    /*
     * -----------------------------------------------------------------
     * Configuration save
     * -----------------------------------------------------------------
     */

    private void saveConfiguration() {
        Integer universe =
                parseInteger(
                        universeField.getValue()
                );

        DmxFixtureProfile profile =
                getSelectedProfile();

        if (profile == null) {
            setErrorStatus(
                    "No valid visual profile is selected."
            );

            return;
        }

        if (universe == null
                || universe
                < DmxFixtureBlockEntity.MIN_UNIVERSE
                || universe
                > DmxFixtureBlockEntity.MAX_UNIVERSE) {

            universeField.setTextColor(
                    0xFFFF5555
            );

            setErrorStatus(
                    "Universe is invalid."
            );

            return;
        }

        universeField.setTextColor(
                0xFFFFFFFF
        );

        if (!validateParameterFields()) {
            setErrorStatus(
                    "DMX parameter channels must be blank or 1-512."
            );

            return;
        }

        int redChannel =
                parseChannelField(
                        redChannelField
                );

        int greenChannel =
                parseChannelField(
                        greenChannelField
                );

        int blueChannel =
                parseChannelField(
                        blueChannelField
                );

        int whiteChannel =
                parseChannelField(
                        whiteChannelField
                );

        int amberChannel =
                parseChannelField(
                        amberChannelField
                );

        int dimmerChannel =
                parseChannelField(
                        dimmerChannelField
                );

        int panChannel =
                parseChannelField(
                        panChannelField
                );

        int tiltChannel =
                parseChannelField(
                        tiltChannelField
                );

        int beamWidthChannel =
                parseChannelField(
                        beamWidthChannelField
                );

        int beamLengthChannel =
                parseChannelField(
                        beamLengthChannelField
                );

        int strobeChannel =
                parseChannelField(
                        strobeChannelField
                );

        if (!ClientPlayNetworking.canSend(
                UpdateFixtureProfilePayload.TYPE
        )) {
            setErrorStatus(
                    "Profile packet is unavailable."
            );

            return;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixturePayload.TYPE
        )) {
            setErrorStatus(
                    "Configuration packet is unavailable."
            );

            return;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixtureGroupPayload.TYPE
        )) {
            setErrorStatus(
                    "Group packet is unavailable."
            );

            return;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixtureParameterMapPayload.TYPE
        )) {
            setErrorStatus(
                    "Parameter-map packet is unavailable."
            );

            return;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixtureMountOrientationPayload.TYPE
        )) {
            setErrorStatus(
                    "Mount-orientation packet is unavailable."
            );

            return;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixtureBeamControlModesPayload.TYPE
        )) {
            setErrorStatus(
                    "Beam control-mode packet is unavailable."
            );

            return;
        }

        ClientPlayNetworking.send(
                new UpdateFixtureProfilePayload(
                        fixturePosition,
                        profile.id()
                )
        );

        ClientPlayNetworking.send(
                new UpdateFixturePayload(
                        fixturePosition,
                        nameField.getValue(),
                        universe
                )
        );

        ClientPlayNetworking.send(
                new UpdateFixtureGroupPayload(
                        fixturePosition,
                        groupField.getValue()
                )
        );

        ClientPlayNetworking.send(
                new UpdateFixtureParameterMapPayload(
                        fixturePosition,

                        redChannel,
                        greenChannel,
                        blueChannel,
                        whiteChannel,
                        amberChannel,

                        dimmerChannel,

                        panChannel,
                        tiltChannel,

                        beamWidthChannel,
                        beamLengthChannel,

                        strobeChannel
                )
        );

        if (!sendMountOrientation(
                false
        )) {
            setErrorStatus(
                    "Mount orientation could not be saved."
            );

            return;
        }

        if (!sendBeamControlModes(
                false
        )) {
            setErrorStatus(
                    "Beam control modes could not be saved."
            );

            return;
        }

        if (!sendPanTiltInterpolation()) {
            return;
        }

        if (!sendManualOutput(
                false
        )) {
            setErrorStatus(
                    "Stored manual values could not be saved."
            );

            return;
        }

        FixtureGroupName normalizedGroup =
                FixtureGroupName.of(
                        groupField.getValue()
                );

        groupField.setValue(
                normalizedGroup.displayName()
        );

        fixtureProfile =
                profile.id();

        statusMessage =
                Component.literal(
                        "Fixture configuration, movement, and parameter sources saved."
                );

        statusColor =
                0xFF55FF55;

        rebuildDynamicLayout();
    }

    /*
     * -----------------------------------------------------------------
     * Parameter validation and parsing
     * -----------------------------------------------------------------
     */

    private boolean validateParameterFields() {
        boolean valid =
                true;

        valid &=
                validateChannelField(
                        redChannelField
                );

        valid &=
                validateChannelField(
                        greenChannelField
                );

        valid &=
                validateChannelField(
                        blueChannelField
                );

        valid &=
                validateChannelField(
                        whiteChannelField
                );

        valid &=
                validateChannelField(
                        amberChannelField
                );

        valid &=
                validateChannelField(
                        dimmerChannelField
                );

        valid &=
                validateChannelField(
                        panChannelField
                );

        valid &=
                validateChannelField(
                        tiltChannelField
                );

        valid &=
                validateChannelField(
                        beamWidthChannelField
                );

        valid &=
                validateChannelField(
                        beamLengthChannelField
                );

        valid &=
                validateChannelField(
                        strobeChannelField
                );

        return valid;
    }

    private boolean validateChannelField(
            EditBox field
    ) {
        if (field == null) {
            return false;
        }

        String value =
                field.getValue()
                        .trim();

        if (value.isEmpty()) {
            field.setTextColor(
                    0xFFFFFFFF
            );

            return true;
        }

        Integer parsed =
                parseInteger(
                        value
                );

        boolean valid =
                parsed != null
                        && FixtureParameterMap.isAssigned(
                                parsed
                        );

        field.setTextColor(
                valid
                        ? 0xFFFFFFFF
                        : 0xFFFF5555
        );

        return valid;
    }

    private static int parseChannelField(
            EditBox field
    ) {
        if (field == null) {
            return FixtureParameterMap.UNASSIGNED;
        }

        String value =
                field.getValue()
                        .trim();

        if (value.isEmpty()) {
            return FixtureParameterMap.UNASSIGNED;
        }

        Integer parsed =
                parseInteger(
                        value
                );

        if (parsed == null
                || !FixtureParameterMap.isAssigned(
                        parsed
                )) {

            return FixtureParameterMap.UNASSIGNED;
        }

        return parsed;
    }

    private void setErrorStatus(
            String message
    ) {
        statusMessage =
                Component.literal(
                        message
                );

        statusColor =
                0xFFFF5555;
    }

    /*
     * -----------------------------------------------------------------
     * Scrolling
     * -----------------------------------------------------------------
     */

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (maximumScroll <= 0) {
            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    horizontalAmount,
                    verticalAmount
            );
        }

        int oldScrollOffset =
                scrollOffset;

        scrollOffset -=
                (int) Math.round(
                        verticalAmount
                                * 24.0D
                );

        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                maximumScroll,
                                scrollOffset
                        )
                );

        if (oldScrollOffset != scrollOffset) {
            rebuildSliderPositions();
            updateWidgetPositions();

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
        );
    }

    private void rebuildSliderPositions() {
        int cursor =
                VIEWPORT_MARGIN_TOP
                        + CONFIGURATION_TOP
                        + FIRST_CONTROL_OFFSET;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        setSliderAbsoluteY(
                redSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        setSliderAbsoluteY(
                greenSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        setSliderAbsoluteY(
                blueSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        setSliderAbsoluteY(
                whiteSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        setSliderAbsoluteY(
                amberSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING
                        + SECTION_TOP_GAP;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        setSliderAbsoluteY(
                dimmerSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING
                        + SECTION_TOP_GAP;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        setSliderAbsoluteY(
                panSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        setSliderAbsoluteY(
                tiltSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        interpolationControlsY =
                cursor;

        cursor +=
                INTERPOLATION_ROW_HEIGHT
                        + INTERPOLATION_ROW_GAP;

        cursor +=
                SECTION_TOP_GAP;

        cursor +=
                SECTION_HEIGHT
                        + SECTION_BOTTOM_GAP;

        setSliderAbsoluteY(
                manualBeamWidthSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        setSliderAbsoluteY(
                manualBeamLengthSlider,
                cursor
        );

        cursor +=
                SLIDER_SPACING;

        setSliderAbsoluteY(
                strobeSlider,
                cursor
        );
    }

    private void setSliderAbsoluteY(
            AbstractWidget slider,
            int absoluteY
    ) {
        slider.setY(
                absoluteY
                        - scrollOffset
        );
    }

    /*
     * -----------------------------------------------------------------
     * Rendering
     * -----------------------------------------------------------------
     */

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        updateWidgetPositions();

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int panelLeft =
                getPanelLeft();

        int contentTop =
                VIEWPORT_MARGIN_TOP
                        + CONFIGURATION_TOP
                        - scrollOffset;

        graphics.centeredText(
                font,
                title,
                width / 2,
                VIEWPORT_MARGIN_TOP
                        - scrollOffset,
                0xFFFFFFFF
        );

        drawLabel(
                graphics,
                "Name",
                panelLeft,
                contentTop
                        + NAME_OFFSET
                        + 6
        );

        drawLabel(
                graphics,
                "Universe",
                panelLeft,
                contentTop
                        + UNIVERSE_OFFSET
                        + 6
        );

        drawLabel(
                graphics,
                "Group",
                panelLeft,
                contentTop
                        + GROUP_OFFSET
                        + 6
        );

        drawLabel(
                graphics,
                "Model / Profile",
                panelLeft,
                contentTop
                        + PROFILE_OFFSET
                        + 6
        );

        drawConfigurationSectionTitle(
                graphics,
                "DMX Parameter Map",
                contentTop
                        + PATCH_TITLE_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Red",
                contentTop
                        + RED_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Green",
                contentTop
                        + GREEN_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Blue",
                contentTop
                        + BLUE_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "White",
                contentTop
                        + WHITE_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Amber",
                contentTop
                        + AMBER_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Dimmer",
                contentTop
                        + DIMMER_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Pan Offset",
                contentTop
                        + PAN_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Tilt Offset",
                contentTop
                        + TILT_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Beam Width",
                contentTop
                        + BEAM_WIDTH_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Beam Length",
                contentTop
                        + BEAM_LENGTH_CHANNEL_OFFSET
        );

        drawParameterLabel(
                graphics,
                "Strobe",
                contentTop
                        + STROBE_CHANNEL_OFFSET
        );

        drawConfigurationSectionTitle(
                graphics,
                "Installation Orientation",
                contentTop
                        + INSTALLATION_TITLE_OFFSET
        );

        DmxFixtureProfile profile =
                getSelectedProfile();

        if (profile != null) {
            drawProfileSummary(
                    graphics,
                    profile,
                    panelLeft,
                    contentTop
                            + PROFILE_SUMMARY_OFFSET
            );

            drawLiveDmxSummary(
                    graphics,
                    panelLeft,
                    contentTop
                            + LIVE_DMX_OFFSET
            );
        }

        drawOrientationSummary(
                graphics,
                contentTop
        );

        drawSection(
                graphics,
                "Color",
                colorSectionY
        );

        drawSection(
                graphics,
                "Dimmer",
                dimmerSectionY
        );

        drawSection(
                graphics,
                "Movement Offset",
                positionSectionY
        );

        drawSection(
                graphics,
                "Beam",
                beamSectionY
        );

        drawPreview(
                graphics
        );

        drawBehaviorMessage(
                graphics
        );

        drawStatusMessage(
                graphics
        );

        drawPositionLabel(
                graphics
        );

        drawScrollbar(
                graphics
        );
    }

    private void drawConfigurationSectionTitle(
            GuiGraphicsExtractor graphics,
            String text,
            int y
    ) {
        if (!isYVisible(
                y,
                12
        )) {
            return;
        }

        graphics.centeredText(
                font,
                Component.literal(
                        text
                ),
                width / 2,
                y,
                0xFFFFFF55
        );
    }

    private void drawParameterLabel(
            GuiGraphicsExtractor graphics,
            String text,
            int y
    ) {
        int x =
                getPanelLeft()
                        + 78;

        if (!isYVisible(
                y,
                CHANNEL_FIELD_HEIGHT
        )) {
            return;
        }

        graphics.text(
                font,
                text,
                x,
                y + 6,
                0xFFFFFFFF,
                false
        );
    }

    private void drawProfileSummary(
            GuiGraphicsExtractor graphics,
            DmxFixtureProfile profile,
            int x,
            int y
    ) {
        if (!isYVisible(
                y,
                10
        )) {
            return;
        }

        String summary =
                profile.displayName()
                        + " | Mode: "
                        + controlMode;

        graphics.text(
                font,
                summary,
                x,
                y,
                0xFFAAAAAA,
                false
        );
    }

    private void drawLiveDmxSummary(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        if (!isYVisible(
                y,
                24
        )) {
            return;
        }

        DmxFixtureBlockEntity fixture =
                getFixture();

        if (fixture == null) {
            graphics.text(
                    font,
                    "Live DMX: fixture unavailable",
                    x,
                    y,
                    0xFF888888,
                    false
            );

            return;
        }

        StringBuilder firstLine =
                new StringBuilder(
                        "Live: "
                );

        appendValue(
                firstLine,
                "R",
                fixture.getRed()
        );

        appendValue(
                firstLine,
                "G",
                fixture.getGreen()
        );

        appendValue(
                firstLine,
                "B",
                fixture.getBlue()
        );

        appendValue(
                firstLine,
                "W",
                fixture.getWhite()
        );

        appendValue(
                firstLine,
                "A",
                fixture.getAmber()
        );

        appendValue(
                firstLine,
                "Dim",
                fixture.getDimmer()
        );

        graphics.text(
                font,
                firstLine.toString(),
                x,
                y,
                0xFFAAAAAA,
                false
        );

        StringBuilder secondLine =
                new StringBuilder();

        appendValue(
                secondLine,
                "Pan",
                fixture.getDmxPan()
        );

        appendValue(
                secondLine,
                "Tilt",
                fixture.getDmxTilt()
        );

        appendValue(
                secondLine,
                "Width",
                fixture.getDmxBeamWidth()
        );

        appendValue(
                secondLine,
                "Length",
                fixture.getDmxBeamLength()
        );

        appendValue(
                secondLine,
                "Strobe",
                fixture.getDmxStrobe()
        );

        graphics.text(
                font,
                secondLine.toString(),
                x,
                y + 12,
                0xFF888888,
                false
        );
    }

    /*
     * -----------------------------------------------------------------
     * Live orientation summary
     * -----------------------------------------------------------------
     */

    private void drawOrientationSummary(
            GuiGraphicsExtractor graphics,
            int contentTop
    ) {
        DmxFixtureBlockEntity fixture =
                getFixture();

        int titleY =
                contentTop
                        + ORIENTATION_TITLE_OFFSET;

        if (fixture == null) {
            if (isYVisible(
                    titleY,
                    12
            )) {
                graphics.centeredText(
                        font,
                        Component.literal(
                                "Orientation unavailable"
                        ),
                        width / 2,
                        titleY,
                        0xFF888888
                );
            }

            return;
        }

        drawConfigurationSectionTitle(
                graphics,
                "Orientation",
                titleY
        );

        int x =
                getPanelLeft()
                        + 12;

        int mountY =
                contentTop
                        + ORIENTATION_MOUNT_OFFSET;

        int activeY =
                contentTop
                        + ORIENTATION_ACTIVE_OFFSET;

        int resolvedY =
                contentTop
                        + ORIENTATION_RESOLVED_OFFSET;

        if (isYVisible(
                mountY,
                10
        )) {
            String mountText =
                    "Mount: Pan "
                            + formatDegrees(
                                    fixture.getMountPanDegrees()
                            )
                            + "  Tilt "
                            + formatDegrees(
                                    fixture.getMountTiltDegrees()
                            );

            graphics.text(
                    font,
                    mountText,
                    x,
                    mountY,
                    0xFFAAAAAA,
                    false
            );
        }

        if (isYVisible(
                activeY,
                10
        )) {
            String activeText =
                    "Offset: Pan "
                            + formatSignedDegrees(
                                    fixture.getActivePanOffsetDegrees()
                            )
                            + "  Tilt "
                            + formatSignedDegrees(
                                    fixture.getActiveTiltOffsetDegrees()
                            );

            graphics.text(
                    font,
                    activeText,
                    x,
                    activeY,
                    0xFFCCCCCC,
                    false
            );
        }

        if (isYVisible(
                resolvedY,
                10
        )) {
            String resolvedText =
                    "Resolved: Pan "
                            + formatDegrees(
                                    fixture.getResolvedPanDegrees()
                            )
                            + "  Tilt "
                            + formatDegrees(
                                    fixture.getResolvedTiltDegrees()
                            );

            graphics.text(
                    font,
                    resolvedText,
                    x,
                    resolvedY,
                    0xFFFFFFFF,
                    false
            );
        }
    }

    private static void appendValue(
            StringBuilder builder,
            String name,
            int value
    ) {
        if (!builder.isEmpty()
                && !builder.toString().endsWith(
                        " "
                )) {

            builder.append(
                    " | "
            );
        }

        builder.append(
                name
        );

        builder.append(
                '='
        );

        builder.append(
                value
        );
    }

    private void drawSection(
            GuiGraphicsExtractor graphics,
            String title,
            int absoluteY
    ) {
        if (absoluteY < 0) {
            return;
        }

        int renderedY =
                absoluteY
                        - scrollOffset;

        if (!isYVisible(
                renderedY,
                SECTION_HEIGHT
        )) {
            return;
        }

        DmxSectionRenderer.render(
                graphics,
                font,
                title,
                width / 2,
                renderedY,
                SLIDER_WIDTH
        );
    }

    private void drawPreview(
            GuiGraphicsExtractor graphics
    ) {
        int renderedY =
                previewY
                        - scrollOffset;

        if (!isYVisible(
                renderedY,
                42
        )) {
            return;
        }

        int previewRed =
                clampChannel(
                        manualRed
                                + manualWhite
                                + manualAmber
                );

        int previewGreen =
                clampChannel(
                        manualGreen
                                + manualWhite
                                + Math.round(
                                        manualAmber
                                                * 0.65F
                                )
                );

        int previewBlue =
                clampChannel(
                        manualBlue
                                + manualWhite
                                + Math.round(
                                        manualAmber
                                                * 0.10F
                                )
                );

        DmxColorPreview.render(
                graphics,
                font,
                width / 2,
                renderedY,
                SLIDER_WIDTH,
                42,
                previewRed,
                previewGreen,
                previewBlue,
                manualDimmer
        );
    }

    private void drawBehaviorMessage(
            GuiGraphicsExtractor graphics
    ) {
        int renderedY =
                behaviorMessageY
                        - scrollOffset;

        if (!isYVisible(
                renderedY,
                36
        )) {
            return;
        }

        Component firstLine;
        Component secondLine;
        if (isManualModeSelected()) {
            firstLine =
                    Component.literal(
                            "Manual mode: output sliders control the fixture."
                    );

            secondLine =
                    Component.literal(
                            "Beam Width and Length are using their manual values."
                    );
        } else {
            firstLine =
                    Component.literal(
                            "DMX mode: Beam parameters may independently use Manual."
                    );

            secondLine =
                    Component.literal(
                            "Width: "
                                    + (
                                    isBeamWidthManualControlled()
                                            ? "Manual"
                                            : "DMX"
                            )
                                    + "   Length: "
                                    + (
                                    isBeamLengthManualControlled()
                                            ? "Manual"
                                            : "DMX"
                            )
                    );
        }

        graphics.centeredText(
                font,
                firstLine,
                width / 2,
                renderedY,
                0xFFAAAAAA
        );

        graphics.centeredText(
                font,
                secondLine,
                width / 2,
                renderedY + 12,
                0xFF888888
        );

    }

    private void drawStatusMessage(
            GuiGraphicsExtractor graphics
    ) {
        int renderedY =
                statusMessageY
                        - scrollOffset;

        if (!isYVisible(
                renderedY,
                10
        )) {
            return;
        }

        graphics.centeredText(
                font,
                statusMessage,
                width / 2,
                renderedY,
                statusColor
        );
    }

    private void drawPositionLabel(
            GuiGraphicsExtractor graphics
    ) {
        int renderedY =
                positionLabelY
                        - scrollOffset;

        if (!isYVisible(
                renderedY,
                10
        )) {
            return;
        }

        graphics.centeredText(
                font,
                Component.literal(
                        "Position: "
                                + fixturePosition.getX()
                                + ", "
                                + fixturePosition.getY()
                                + ", "
                                + fixturePosition.getZ()
                ),
                width / 2,
                renderedY,
                0xFF888888
        );
    }

    private void drawLabel(
            GuiGraphicsExtractor graphics,
            String text,
            int x,
            int y
    ) {
        if (!isYVisible(
                y,
                10
        )) {
            return;
        }

        graphics.text(
                font,
                text,
                x,
                y,
                0xFFFFFFFF,
                false
        );
    }

    private boolean isYVisible(
            int y,
            int regionHeight
    ) {
        return y
                + regionHeight
                >= VIEWPORT_MARGIN_TOP
                && y
                <= height
                - VIEWPORT_MARGIN_BOTTOM;
    }

    private void drawScrollbar(
            GuiGraphicsExtractor graphics
    ) {
        if (maximumScroll <= 0) {
            return;
        }

        int trackTop =
                VIEWPORT_MARGIN_TOP;

        int trackBottom =
                height
                        - VIEWPORT_MARGIN_BOTTOM;

        int trackHeight =
                trackBottom
                        - trackTop;

        int trackLeft =
                width / 2
                        + PANEL_WIDTH / 2
                        + 6;

        graphics.fill(
                trackLeft,
                trackTop,
                trackLeft
                        + 4,
                trackBottom,
                0xFF303030
        );

        int viewportHeight =
                height
                        - VIEWPORT_MARGIN_TOP
                        - VIEWPORT_MARGIN_BOTTOM;

        int thumbHeight =
                Math.max(
                        20,
                        Math.round(
                                trackHeight
                                        * (
                                        viewportHeight
                                                / (float) contentHeight
                                )
                        )
                );

        int usableTrack =
                trackHeight
                        - thumbHeight;

        int thumbOffset =
                maximumScroll == 0
                        ? 0
                        : Math.round(
                                usableTrack
                                        * (
                                        scrollOffset
                                                / (float) maximumScroll
                                )
                        );

        graphics.fill(
                trackLeft,
                trackTop
                        + thumbOffset,
                trackLeft
                        + 4,
                trackTop
                        + thumbOffset
                        + thumbHeight,
                0xFFAAAAAA
        );
    }

    /*
     * -----------------------------------------------------------------
     * Formatting
     * -----------------------------------------------------------------
     */

    private static String formatDegrees(
            float degrees
    ) {
        return Math.round(
                degrees
        )
                + "\u00b0";
    }

    private static String formatSignedDegrees(
            float degrees
    ) {
        int rounded =
                Math.round(
                        degrees
                );

        if (rounded > 0) {
            return "+"
                    + rounded
                    + "\u00b0";
        }

        return rounded
                + "\u00b0";
    }

    private static String formatInterpolationSeconds(
            float seconds
    ) {
        if (seconds == Math.round(
                seconds
        )) {
            return Integer.toString(
                    Math.round(
                            seconds
                    )
            );
        }

        return Float.toString(
                seconds
        );
    }

    /*
     * -----------------------------------------------------------------
     * Utilities
     * -----------------------------------------------------------------
     */

    private int getPanelLeft() {
        return (
                width
                        - PANEL_WIDTH
        )
                / 2;
    }

    private DmxFixtureBlockEntity getFixture() {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return null;
        }

        BlockEntity blockEntity =
                minecraft.level.getBlockEntity(
                        fixturePosition
                );

        if (blockEntity
                instanceof DmxFixtureBlockEntity fixture) {

            return fixture;
        }

        return null;
    }

    private static Integer parseInteger(
            String value
    ) {
        try {
            return Integer.parseInt(
                    value.trim()
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Double parseDouble(
            String value
    ) {
        if (value == null) {
            return null;
        }

        try {
            double parsed =
                    Double.parseDouble(
                            value.trim()
                    );

            if (!Double.isFinite(
                    parsed
            )) {
                return null;
            }

            return parsed;

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static int clampChannel(
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
