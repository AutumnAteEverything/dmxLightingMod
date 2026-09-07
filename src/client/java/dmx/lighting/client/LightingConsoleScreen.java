package dmx.lighting.client;

import dmx.lighting.ConsoleFixtureOutputPayload;
import dmx.lighting.DmxAxolotlProfile;
import dmx.lighting.DmxBlockDisplayProfile;
import dmx.lighting.DmxCreakingProfile;
import dmx.lighting.DmxEndermanProfile;
import dmx.lighting.DmxFixtureProfile;
import dmx.lighting.DmxFixtureProfileRegistry;
import dmx.lighting.DmxNautilusProfile;
import dmx.lighting.DmxParrotProfile;
import dmx.lighting.DmxWardenProfile;
import dmx.lighting.FixtureBrowserEntry;
import dmx.lighting.PatchConflictAnalyzer;
import dmx.lighting.PatchConflictResult;
import dmx.lighting.PatchOccupancyEntry;
import dmx.lighting.RequestFixtureBrowserPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Right-docked handheld DMX Lighting Console.
 *
 * The console intentionally leaves most of the Minecraft world visible
 * while providing live fixture control on the right side of the screen.
 */
public class LightingConsoleScreen extends Screen {

    private enum ConsoleView {
        FIXTURES,
        GROUPS,
        UNIVERSES,
        PATCH,
        OUTPUT
    }

    private record GroupSummary(
            String name,
            int fixtureCount,
            int dmxCount,
            int manualCount
    ) {

        public String modeSummary() {
            if (fixtureCount <= 0) {
                return "-";
            }

            if (manualCount == 0) {
                return "DMX";
            }

            if (dmxCount == 0) {
                return "Manual";
            }

            return "Mixed";
        }
    }

    private record UniverseSummary(
            int universe,
            int fixtureCount,
            int usedChannelCount,
            int conflictChannelCount
    ) {

        public boolean hasConflicts() {
            return conflictChannelCount > 0;
        }
    }

    /*
     * -----------------------------------------------------------------
     * Right-docked panel sizing
     * -----------------------------------------------------------------
     */

    private static final int DESIRED_PANEL_WIDTH =
            360;

    private static final int MINIMUM_PANEL_WIDTH =
            300;

    private static final int PANEL_MARGIN =
            10;

    private static final int PANEL_VERTICAL_MARGIN =
            12;

    /*
     * -----------------------------------------------------------------
     * Vertical layout
     * -----------------------------------------------------------------
     */

    private static final int TITLE_HEIGHT =
            26;

    private static final int TAB_HEIGHT =
            26;

    private static final int HEADER_HEIGHT =
            TITLE_HEIGHT
                    + TAB_HEIGHT;

    private static final int FOOTER_HEIGHT =
            82;

    private static final int TABLE_HEADER_HEIGHT =
            18;

    private static final int ROW_HEIGHT =
            18;

    /*
     * -----------------------------------------------------------------
     * Tabs
     * -----------------------------------------------------------------
     */

    private static final int TAB_COUNT =
            5;

    private static final int TAB_HORIZONTAL_MARGIN =
            5;

    private static final int FIXTURES_TAB_INDEX =
            0;

    private static final int GROUPS_TAB_INDEX =
            1;

    private static final int UNIVERSES_TAB_INDEX =
            2;

    private static final int PATCH_TAB_INDEX =
            3;

    private static final int OUTPUT_TAB_INDEX =
            4;

    /*
     * -----------------------------------------------------------------
     * Tables
     * -----------------------------------------------------------------
     */

    private static final int TABLE_HORIZONTAL_MARGIN =
            6;

    private static final int COLOR_COLUMN_WIDTH =
            22;

    private static final int FIXTURE_PATCH_COLUMN_WIDTH =
            55;

    private static final int POSITION_COLUMN_WIDTH =
            82;

    private static final int MODE_COLUMN_WIDTH =
            46;

    private static final int FIXTURE_PROFILE_COLUMN_WIDTH =
            56;

    private static final int MIN_NAME_COLUMN_WIDTH =
            72;

    private static final int MIN_GROUP_COLUMN_WIDTH =
            64;

    private static final int GROUP_COUNT_COLUMN_WIDTH =
            52;

    private static final int GROUP_MODE_COLUMN_WIDTH =
            62;

    private static final int GROUP_UNIVERSE_COLUMN_WIDTH =
            60;

    private static final int MIN_GROUP_NAME_COLUMN_WIDTH =
            90;

    private static final int UNIVERSE_NUMBER_COLUMN_WIDTH =
            62;

    private static final int UNIVERSE_FIXTURE_COLUMN_WIDTH =
            64;

    private static final int UNIVERSE_USED_COLUMN_WIDTH =
            72;

    private static final int PATCH_STATUS_COLUMN_WIDTH =
            18;

    private static final int PATCH_RANGE_COLUMN_WIDTH =
            78;

    private static final int PATCH_PROFILE_COLUMN_WIDTH =
            92;

    private static final int PATCH_POSITION_COLUMN_WIDTH =
            78;

    /*
     * -----------------------------------------------------------------
     * Output layout
     * -----------------------------------------------------------------
     */

    private static final int OUTPUT_SLIDER_HEIGHT =
            18;

    private static final int OUTPUT_SLIDER_SPACING =
            21;

    private static final int OUTPUT_TARGET_BUTTON_HEIGHT =
            18;

    private static final int OUTPUT_TARGET_BUTTON_GAP =
            4;

    /*
     * -----------------------------------------------------------------
     * Colors
     * -----------------------------------------------------------------
     */

    private static final int PANEL_COLOR =
            0xF0202020;

    private static final int HEADER_COLOR =
            0xFF292929;

    private static final int ROW_COLOR_A =
            0xFF181818;

    private static final int ROW_COLOR_B =
            0xFF202020;

    private static final int SELECTED_ROW_COLOR =
            0xFF3A4D61;

    private static final int SELECTED_ROW_BORDER =
            0xFF87B9E8;

    private static final int BORDER_COLOR =
            0xFF555555;

    private static final int TEXT_COLOR =
            0xFFFFFFFF;

    private static final int MUTED_TEXT_COLOR =
            0xFFAAAAAA;

    private static final int DMX_MODE_COLOR =
            0xFF48D06B;

    private static final int MANUAL_MODE_COLOR =
            0xFFF2C744;

    private static final int UNKNOWN_MODE_COLOR =
            0xFF808080;

    private static final int PATCH_OK_COLOR =
            0xFF48D06B;

    private static final int PATCH_CONFLICT_COLOR =
            0xFFE05252;

    private static final int PATCH_OUT_OF_RANGE_COLOR =
            0xFFF2C744;

    private static final int PATCH_CONFLICT_ROW_COLOR =
            0xFF4A2020;

    private static final int PATCH_OUT_OF_RANGE_ROW_COLOR =
            0xFF4A4020;

    private static final int OUTPUT_SUCCESS_COLOR =
            0xFF55FF55;

    private static final int OUTPUT_ERROR_COLOR =
            0xFFFF5555;

    /*
     * -----------------------------------------------------------------
     * Browser data
     * -----------------------------------------------------------------
     */

    private List<FixtureBrowserEntry> entries =
            List.of();

    private List<GroupSummary> groupSummaries =
            List.of();

    private List<UniverseSummary> universeSummaries =
            List.of();

    private List<PatchConflictResult> patchResults =
            List.of();

    private List<FixtureBrowserEntry> visibleFixtureEntries =
            List.of();

    private List<PatchConflictResult> visiblePatchResults =
            List.of();

    private ConsoleView currentView =
            ConsoleView.FIXTURES;

    private long lastSeenRevision =
            -1L;

    /*
     * -----------------------------------------------------------------
     * Filters
     * -----------------------------------------------------------------
     */

    private String fixtureGroupFilterKey;

    private Integer patchUniverseFilter;

    /*
     * -----------------------------------------------------------------
     * Pagination
     * -----------------------------------------------------------------
     */

    private int currentPage =
            0;

    private int totalPages =
            1;

    private int rowsPerPage =
            1;

    /*
     * -----------------------------------------------------------------
     * Selection
     * -----------------------------------------------------------------
     */

    private BlockPos selectedPosition;
    private String selectedTargetKind;
    private int selectedTargetEntityId =
            FixtureBrowserEntry.NO_TARGET_ENTITY_ID;

    private String selectedGroupKey;

    private Integer selectedUniverse;

    /*
     * -----------------------------------------------------------------
     * Geometry
     * -----------------------------------------------------------------
     */

    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    private int nameColumnWidth;
    private int groupColumnWidth;
    private int groupSummaryNameColumnWidth;
    private int patchNameColumnWidth;

    /*
     * -----------------------------------------------------------------
     * Footer controls
     * -----------------------------------------------------------------
     */

    private Button previousPageButton;
    private Button nextPageButton;
    private Button editFixtureButton;
    private Button showAllButton;
    private Button refreshButton;
    private Button closeButton;

    /*
     * -----------------------------------------------------------------
     * Output controls
     * -----------------------------------------------------------------
     */

    private Button outputFixtureTargetButton;
    private Button outputGroupTargetButton;
    private Button outputAllTargetButton;
    private Button outputApplyButton;

    private String outputTarget =
            ConsoleFixtureOutputPayload.TARGET_FIXTURE;

    private DmxValueSlider outputRedSlider;
    private DmxValueSlider outputGreenSlider;
    private DmxValueSlider outputBlueSlider;
    private DmxValueSlider outputWhiteSlider;
    private DmxValueSlider outputAmberSlider;
    private DmxValueSlider outputDimmerSlider;
    private DmxValueSlider outputPanSlider;
    private DmxValueSlider outputTiltSlider;
    private DmxValueSlider outputBeamWidthSlider;
    private DmxValueSlider outputBeamLengthSlider;
    private DmxValueSlider outputStrobeSlider;

    private int outputRed =
            255;

    private int outputGreen =
            255;

    private int outputBlue =
            255;

    private int outputWhite =
            0;

    private int outputAmber =
            0;

    private int outputDimmer =
            255;

    private int outputPan =
            128;

    private int outputTilt =
            128;

    private int outputBeamWidth =
            0;

    private int outputBeamLength =
            0;

    private int outputStrobe =
            0;

    private Component outputStatusMessage =
            Component.literal(
                    "Select a target and adjust output."
            );

    private int outputStatusColor =
            MUTED_TEXT_COLOR;

    public LightingConsoleScreen() {
        super(
                Component.literal(
                        "DMX Lighting Console"
                )
        );
    }

    @Override
    protected void init() {
        DmxFixtureProfileRegistry.initialize();

        calculatePanelDimensions();
        calculateColumnWidths();

        refreshEntries();
        calculatePagination();

        createButtons();
        createOutputControls();

        updateOutputWidgetVisibility();
        updateButtonStates();
    }

    /*
     * -----------------------------------------------------------------
     * Layout
     * -----------------------------------------------------------------
     */

    private void calculatePanelDimensions() {
        int maximumWidthFromScreen =
                Math.max(
                        MINIMUM_PANEL_WIDTH,
                        width
                                - PANEL_MARGIN
                                * 2
                );

        panelWidth =
                Math.min(
                        DESIRED_PANEL_WIDTH,
                        maximumWidthFromScreen
                );

        panelWidth =
                Math.max(
                        Math.min(
                                MINIMUM_PANEL_WIDTH,
                                maximumWidthFromScreen
                        ),
                        panelWidth
                );

        panelHeight =
                Math.max(
                        180,
                        height
                                - PANEL_VERTICAL_MARGIN
                                * 2
                );

        panelLeft =
                width
                        - PANEL_MARGIN
                        - panelWidth;

        panelTop =
                PANEL_VERTICAL_MARGIN;
    }

    private void calculateColumnWidths() {
        calculateFixtureColumnWidths();
        calculateGroupColumnWidths();
        calculatePatchColumnWidths();
    }

    private void calculateFixtureColumnWidths() {
        int tableInteriorWidth =
                getTableRight()
                        - getTableLeft()
                        - 10;

        int fixedWidth =
                COLOR_COLUMN_WIDTH
                        + FIXTURE_PATCH_COLUMN_WIDTH;

        if (hasPositionColumn()) {
            fixedWidth +=
                    POSITION_COLUMN_WIDTH;
        }

        if (hasModeColumn()) {
            fixedWidth +=
                    MODE_COLUMN_WIDTH;
        }

        if (hasFixtureProfileColumn()) {
            fixedWidth +=
                    FIXTURE_PROFILE_COLUMN_WIDTH;
        }

        int flexibleWidth =
                Math.max(
                        MIN_NAME_COLUMN_WIDTH,
                        tableInteriorWidth
                                - fixedWidth
                );

        if (hasGroupColumn()) {
            nameColumnWidth =
                    Math.max(
                            MIN_NAME_COLUMN_WIDTH,
                            flexibleWidth
                                    * 3
                                    / 5
                    );

            groupColumnWidth =
                    Math.max(
                            MIN_GROUP_COLUMN_WIDTH,
                            flexibleWidth
                                    - nameColumnWidth
                    );
        } else {
            nameColumnWidth =
                    flexibleWidth;

            groupColumnWidth =
                    0;
        }
    }

    private void calculateGroupColumnWidths() {
        int tableInteriorWidth =
                getTableRight()
                        - getTableLeft()
                        - 10;

        int fixedWidth =
                GROUP_COUNT_COLUMN_WIDTH
                        + GROUP_MODE_COLUMN_WIDTH
                        + GROUP_UNIVERSE_COLUMN_WIDTH;

        groupSummaryNameColumnWidth =
                Math.max(
                        MIN_GROUP_NAME_COLUMN_WIDTH,
                        tableInteriorWidth
                                - fixedWidth
                );
    }

    private void calculatePatchColumnWidths() {
        int tableInteriorWidth =
                getTableRight()
                        - getTableLeft()
                        - 10;

        int fixedWidth =
                PATCH_STATUS_COLUMN_WIDTH
                        + PATCH_RANGE_COLUMN_WIDTH
                        + PATCH_PROFILE_COLUMN_WIDTH;

        if (hasPatchPositionColumn()) {
            fixedWidth +=
                    PATCH_POSITION_COLUMN_WIDTH;
        }

        patchNameColumnWidth =
                Math.max(
                        60,
                        tableInteriorWidth
                                - fixedWidth
                );
    }

    /*
     * -----------------------------------------------------------------
     * Footer buttons
     * -----------------------------------------------------------------
     */

    private void createButtons() {
        int footerTop =
                getPanelBottom()
                        - FOOTER_HEIGHT;

        int gap =
                4;

        int buttonWidth =
                Math.max(
                        46,
                        (
                                panelWidth
                                        - 20
                                        - gap
                                        * 5
                        )
                                / 6
                );

        int left =
                getPanelLeft()
                        + 10;

        int y =
                footerTop
                        + 54;

        previousPageButton =
                createFooterButton(
                        "Prev",
                        left,
                        y,
                        buttonWidth,
                        this::previousPage
                );

        nextPageButton =
                createFooterButton(
                        "Next",
                        left
                                + (
                                buttonWidth
                                        + gap
                        ),
                        y,
                        buttonWidth,
                        this::nextPage
                );

        editFixtureButton =
                createFooterButton(
                        "Edit",
                        left
                                + (
                                buttonWidth
                                        + gap
                        )
                                * 2,
                        y,
                        buttonWidth,
                        this::editSelectedFixture
                );

        showAllButton =
                createFooterButton(
                        "All",
                        left
                                + (
                                buttonWidth
                                        + gap
                        )
                                * 3,
                        y,
                        buttonWidth,
                        this::clearViewFilters
                );

        refreshButton =
                createFooterButton(
                        "Refresh",
                        left
                                + (
                                buttonWidth
                                        + gap
                        )
                                * 4,
                        y,
                        buttonWidth,
                        this::requestRefresh
                );

        closeButton =
                createFooterButton(
                        "Close",
                        left
                                + (
                                buttonWidth
                                        + gap
                        )
                                * 5,
                        y,
                        buttonWidth,
                        this::onClose
                );
    }

    private Button createFooterButton(
            String label,
            int x,
            int y,
            int width,
            Runnable action
    ) {
        Button button =
                Button.builder(
                        Component.literal(
                                label
                        ),
                        ignored ->
                                action.run()
                )
                .bounds(
                        x,
                        y,
                        width,
                        18
                )
                .build();

        addRenderableWidget(
                button
        );

        return button;
    }

    /*
     * -----------------------------------------------------------------
     * Output controls
     * -----------------------------------------------------------------
     */

    private void createOutputControls() {
        int innerLeft =
                getTableLeft()
                        + 10;

        int innerRight =
                getTableRight()
                        - 10;

        int innerWidth =
                innerRight
                        - innerLeft;

        int targetButtonWidth =
                (
                        innerWidth
                                - OUTPUT_TARGET_BUTTON_GAP
                                * 2
                )
                        / 3;

        int targetY =
                getTableTop()
                        + 8;

        outputFixtureTargetButton =
                createOutputTargetButton(
                        getOutputFixtureTargetMessage(),
                        innerLeft,
                        targetY,
                        targetButtonWidth,
                        ConsoleFixtureOutputPayload.TARGET_FIXTURE
                );

        outputGroupTargetButton =
                createOutputTargetButton(
                        getOutputGroupTargetMessage(),
                        innerLeft
                                + targetButtonWidth
                                + OUTPUT_TARGET_BUTTON_GAP,
                        targetY,
                        targetButtonWidth,
                        ConsoleFixtureOutputPayload.TARGET_GROUP
                );

        outputAllTargetButton =
                createOutputTargetButton(
                        getOutputAllTargetMessage(),
                        innerLeft
                                + (
                                targetButtonWidth
                                        + OUTPUT_TARGET_BUTTON_GAP
                        )
                                * 2,
                        targetY,
                        targetButtonWidth,
                        ConsoleFixtureOutputPayload.TARGET_ALL
                );

        int sliderWidth =
                innerWidth;

        int firstSliderY =
                targetY
                        + 46;

        outputRedSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY,
                        sliderWidth,
                        "Red",
                        outputRed,
                        ConsoleFixtureOutputPayload.APPLY_RED
                );

        outputGreenSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING,
                        sliderWidth,
                        "Green",
                        outputGreen,
                        ConsoleFixtureOutputPayload.APPLY_GREEN
                );

        outputBlueSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 2,
                        sliderWidth,
                        "Blue",
                        outputBlue,
                        ConsoleFixtureOutputPayload.APPLY_BLUE
                );

        outputWhiteSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 3,
                        sliderWidth,
                        "White",
                        outputWhite,
                        ConsoleFixtureOutputPayload.APPLY_WHITE
                );

        outputAmberSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 4,
                        sliderWidth,
                        "Amber",
                        outputAmber,
                        ConsoleFixtureOutputPayload.APPLY_AMBER
                );

        outputDimmerSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 5,
                        sliderWidth,
                        "Dimmer",
                        outputDimmer,
                        ConsoleFixtureOutputPayload.APPLY_DIMMER
                );

        outputPanSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 6,
                        sliderWidth,
                        "Pan Offset",
                        outputPan,
                        ConsoleFixtureOutputPayload.APPLY_PAN
                );

        outputTiltSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 7,
                        sliderWidth,
                        "Tilt Offset",
                        outputTilt,
                        ConsoleFixtureOutputPayload.APPLY_TILT
                );

        outputBeamWidthSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 8,
                        sliderWidth,
                        "Beam Width",
                        outputBeamWidth,
                        ConsoleFixtureOutputPayload.APPLY_BEAM_WIDTH
                );

        outputBeamLengthSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 9,
                        sliderWidth,
                        "Beam Length",
                        outputBeamLength,
                        ConsoleFixtureOutputPayload.APPLY_BEAM_LENGTH
                );

        outputStrobeSlider =
                createLiveOutputSlider(
                        innerLeft,
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 10,
                        sliderWidth,
                        "Strobe",
                        outputStrobe,
                        ConsoleFixtureOutputPayload.APPLY_STROBE
                );

        int applyY =
                Math.min(
                        firstSliderY
                                + OUTPUT_SLIDER_SPACING
                                * 11
                                + 2,
                        getTableBottom()
                                - 22
                );

        outputApplyButton =
                Button.builder(
                        Component.literal(
                                "Apply All Output"
                        ),
                        ignored ->
                                applyConsoleOutput()
                )
                .bounds(
                        innerLeft,
                        applyY,
                        sliderWidth,
                        18
                )
                .build();

        addRenderableWidget(
                outputApplyButton
        );

        updateOutputTargetButtonMessages();
        updateOutputWidgetVisibility();
    }

    private Button createOutputTargetButton(
            Component label,
            int x,
            int y,
            int width,
            String target
    ) {
        Button button =
                Button.builder(
                        label,
                        ignored ->
                                setOutputTarget(
                                        target
                                )
                )
                .bounds(
                        x,
                        y,
                        width,
                        OUTPUT_TARGET_BUTTON_HEIGHT
                )
                .build();

        addRenderableWidget(
                button
        );

        return button;
    }

    private DmxValueSlider createLiveOutputSlider(
            int x,
            int y,
            int sliderWidth,
            String label,
            int initialValue,
            int applyMask
    ) {
        DmxValueSlider slider =
                new DmxValueSlider(
                        x,
                        y,
                        sliderWidth,
                        OUTPUT_SLIDER_HEIGHT,
                        label,
                        initialValue,
                        value -> {
                            setOutputValue(
                                    applyMask,
                                    value
                            );

                            sendLiveOutput(
                                    applyMask
                            );
                        }
                );

        addRenderableWidget(
                slider
        );

        return slider;
    }

    private void setOutputValue(
            int applyMask,
            int value
    ) {
        if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_RED) {

            outputRed =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_GREEN) {

            outputGreen =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_BLUE) {

            outputBlue =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_WHITE) {

            outputWhite =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_AMBER) {

            outputAmber =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_DIMMER) {

            outputDimmer =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_PAN) {

            outputPan =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_TILT) {

            outputTilt =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_BEAM_WIDTH) {

            outputBeamWidth =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_BEAM_LENGTH) {

            outputBeamLength =
                    value;

        } else if (applyMask
                == ConsoleFixtureOutputPayload.APPLY_STROBE) {

            outputStrobe =
                    value;
        }
    }

    /*
     * -----------------------------------------------------------------
     * Output loading
     * -----------------------------------------------------------------
     */

    private void loadOutputFromSelectedFixture() {
        FixtureBrowserEntry selected =
                getSelectedBrowserEntry();

        if (selected == null) {
            outputStatusMessage =
                    Component.literal(
                            "No fixture selected."
                    );

            outputStatusColor =
                    MUTED_TEXT_COLOR;

            return;
        }

        outputRed =
                selected.manualRed();

        outputGreen =
                selected.manualGreen();

        outputBlue =
                selected.manualBlue();

        outputWhite =
                selected.manualWhite();

        outputAmber =
                selected.manualAmber();

        outputDimmer =
                selected.manualDimmer();

        outputPan =
                selected.manualPan();

        outputTilt =
                selected.manualTilt();

        outputBeamWidth =
                selected.manualBeamWidth();

        outputBeamLength =
                selected.manualBeamLength();

        outputStrobe =
                selected.manualStrobe();

        if (outputRedSlider != null) {
            outputRedSlider.setDmxValue(
                    outputRed
            );
        }

        if (outputGreenSlider != null) {
            outputGreenSlider.setDmxValue(
                    outputGreen
            );
        }

        if (outputBlueSlider != null) {
            outputBlueSlider.setDmxValue(
                    outputBlue
            );
        }

        if (outputWhiteSlider != null) {
            outputWhiteSlider.setDmxValue(
                    outputWhite
            );
        }

        if (outputAmberSlider != null) {
            outputAmberSlider.setDmxValue(
                    outputAmber
            );
        }

        if (outputDimmerSlider != null) {
            outputDimmerSlider.setDmxValue(
                    outputDimmer
            );
        }

        if (outputPanSlider != null) {
            outputPanSlider.setDmxValue(
                    outputPan
            );
        }

        if (outputTiltSlider != null) {
            outputTiltSlider.setDmxValue(
                    outputTilt
            );
        }

        if (outputBeamWidthSlider != null) {
            outputBeamWidthSlider.setDmxValue(
                    outputBeamWidth
            );
        }

        if (outputBeamLengthSlider != null) {
            outputBeamLengthSlider.setDmxValue(
                    outputBeamLength
            );
        }

        if (outputStrobeSlider != null) {
            outputStrobeSlider.setDmxValue(
                    outputStrobe
            );
        }

        outputStatusMessage =
                Component.literal(
                        "Loaded \""
                                + selected.fixtureName()
                                + "\"."
                );

        outputStatusColor =
                MUTED_TEXT_COLOR;
    }

    /*
     * -----------------------------------------------------------------
     * Output target
     * -----------------------------------------------------------------
     */

    private void setOutputTarget(
            String target
    ) {
        outputTarget =
                target;

        if (ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                outputTarget
        )) {
            loadOutputFromSelectedFixture();
        } else {
            outputStatusMessage =
                    Component.literal(
                            getOutputTargetDescription()
                    );

            outputStatusColor =
                    MUTED_TEXT_COLOR;
        }

        updateOutputTargetButtonMessages();
    }

    private void updateOutputTargetButtonMessages() {
        if (outputFixtureTargetButton != null) {
            outputFixtureTargetButton.setMessage(
                    getOutputFixtureTargetMessage()
            );
        }

        if (outputGroupTargetButton != null) {
            outputGroupTargetButton.setMessage(
                    getOutputGroupTargetMessage()
            );
        }

        if (outputAllTargetButton != null) {
            outputAllTargetButton.setMessage(
                    getOutputAllTargetMessage()
            );
        }
    }

    private Component getOutputFixtureTargetMessage() {
        return Component.literal(
                ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                        outputTarget
                )
                        ? "[Fixture]"
                        : "Fixture"
        );
    }

    private Component getOutputGroupTargetMessage() {
        return Component.literal(
                ConsoleFixtureOutputPayload.TARGET_GROUP.equals(
                        outputTarget
                )
                        ? "[Group]"
                        : "Group"
        );
    }

    private Component getOutputAllTargetMessage() {
        return Component.literal(
                ConsoleFixtureOutputPayload.TARGET_ALL.equals(
                        outputTarget
                )
                        ? "[All]"
                        : "All"
        );
    }

    private void updateOutputWidgetVisibility() {
        boolean visible =
                currentView
                        == ConsoleView.OUTPUT;

        boolean dmxMobFixtureTarget =
                visible
                        && isSelectedDmxMob()
                        && ConsoleFixtureOutputPayload.TARGET_FIXTURE
                        .equals(
                                outputTarget
                        );

        boolean dmxBlockDisplayTarget =
                visible
                        && isSelectedDmxBlockDisplay()
                        && ConsoleFixtureOutputPayload.TARGET_FIXTURE
                        .equals(
                                outputTarget
                        );

        boolean compactDmxEntityTarget =
                dmxMobFixtureTarget
                        || dmxBlockDisplayTarget;

        setWidgetVisible(
                outputFixtureTargetButton,
                visible
        );

        setWidgetVisible(
                outputGroupTargetButton,
                visible
        );

        setWidgetVisible(
                outputAllTargetButton,
                visible
        );

        setWidgetVisible(
                outputRedSlider,
                visible
        );

        setWidgetVisible(
                outputGreenSlider,
                visible
        );

        setWidgetVisible(
                outputBlueSlider,
                visible
        );

        setWidgetVisible(
                outputWhiteSlider,
                visible
                        && !compactDmxEntityTarget
        );

        setWidgetVisible(
                outputAmberSlider,
                visible
                        && !compactDmxEntityTarget
        );

        setWidgetVisible(
                outputDimmerSlider,
                visible
        );

        setWidgetVisible(
                outputPanSlider,
                visible
                        && !compactDmxEntityTarget
        );

        setWidgetVisible(
                outputTiltSlider,
                visible
                        && !compactDmxEntityTarget
        );

        setWidgetVisible(
                outputBeamWidthSlider,
                visible
                        && !compactDmxEntityTarget
        );

        setWidgetVisible(
                outputBeamLengthSlider,
                visible
                        && !compactDmxEntityTarget
        );

        setWidgetVisible(
                outputStrobeSlider,
                visible
                        && !dmxMobFixtureTarget
        );

        setWidgetVisible(
                outputApplyButton,
                visible
        );

        if (outputApplyButton != null) {
            outputApplyButton.setMessage(
                    Component.literal(
                            compactDmxEntityTarget
                                    ? "Apply DMX Output"
                                    : "Apply All Output"
                    )
            );
        }
    }

    private static void setWidgetVisible(
            AbstractWidget widget,
            boolean visible
    ) {
        if (widget == null) {
            return;
        }

        widget.visible =
                visible;

        widget.active =
                visible;
    }

    /*
     * -----------------------------------------------------------------
     * Live output
     * -----------------------------------------------------------------
     */

    private void sendLiveOutput(
            int applyMask
    ) {
        sendConsoleOutput(
                applyMask,
                false
        );
    }

    private void applyConsoleOutput() {
        sendConsoleOutput(
                getApplyAllMaskForCurrentTarget(),
                true
        );
    }

    private void sendConsoleOutput(
            int applyMask,
            boolean showSuccessMessage
    ) {
        if (!ClientPlayNetworking.canSend(
                ConsoleFixtureOutputPayload.TYPE
        )) {
            setOutputError(
                    "Console output packet unavailable."
            );

            return;
        }

        BlockPos targetPosition =
                resolveOutputTargetPosition();

        if (targetPosition == null) {
            return;
        }

        ClientPlayNetworking.send(
                new ConsoleFixtureOutputPayload(
                        targetPosition,
                        outputTarget,

                        outputRed,
                        outputGreen,
                        outputBlue,
                        outputWhite,
                        outputAmber,

                        outputDimmer,

                        outputPan,
                        outputTilt,

                        outputBeamWidth,
                        outputBeamLength,

                        outputStrobe,

                        applyMask,
                        resolveOutputTargetEntityId()
                )
        );

        if (showSuccessMessage) {
            outputStatusMessage =
                    Component.literal(
                            "Output applied to "
                                    + getOutputTargetDescription()
                                    + "."
                    );

            outputStatusColor =
                    OUTPUT_SUCCESS_COLOR;
        }
    }

    private BlockPos resolveOutputTargetPosition() {
        if (ConsoleFixtureOutputPayload.TARGET_ALL.equals(
                outputTarget
        )) {
            FixtureBrowserEntry selected =
                    getSelectedBrowserEntry();

            if (selected != null) {
                return selected.position();
            }

            if (!entries.isEmpty()) {
                return entries
                        .get(
                                0
                        )
                        .position();
            }

            return BlockPos.ZERO;
        }

        if (ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                outputTarget
        )) {
            FixtureBrowserEntry selected =
                    getSelectedBrowserEntry();

            if (selected == null) {
                setOutputError(
                        "Select a fixture first."
                );

                return null;
            }

            return selected.position();
        }

        if (ConsoleFixtureOutputPayload.TARGET_GROUP.equals(
                outputTarget
        )) {
            FixtureBrowserEntry groupFixture =
                    findOutputGroupFixture();

            if (groupFixture == null) {
                setOutputError(
                        "Select a grouped fixture or group."
                );

                return null;
            }

            return groupFixture.position();
        }

        setOutputError(
                "Unknown output target."
        );

        return null;
    }

    private int resolveOutputTargetEntityId() {
        FixtureBrowserEntry targetEntry =
                null;

        if (ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                outputTarget
        )) {
            targetEntry =
                    getSelectedBrowserEntry();
        } else if (ConsoleFixtureOutputPayload.TARGET_GROUP.equals(
                outputTarget
        )) {
            targetEntry =
                    findOutputGroupFixture();
        }

        if (targetEntry == null
                || !targetEntry.isMobTarget()) {

            return ConsoleFixtureOutputPayload.NO_TARGET_ENTITY_ID;
        }

        return targetEntry.targetEntityId();
    }

    private int getApplyAllMaskForCurrentTarget() {
        if (isSelectedDmxBlockDisplay()
                && ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                        outputTarget
                )) {
            return ConsoleFixtureOutputPayload.APPLY_RED
                    | ConsoleFixtureOutputPayload.APPLY_GREEN
                    | ConsoleFixtureOutputPayload.APPLY_BLUE
                    | ConsoleFixtureOutputPayload.APPLY_DIMMER
                    | ConsoleFixtureOutputPayload.APPLY_STROBE;
        }

        if (isSelectedDmxMob()
                && ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                        outputTarget
                )) {

            return ConsoleFixtureOutputPayload.APPLY_RED
                    | ConsoleFixtureOutputPayload.APPLY_GREEN
                    | ConsoleFixtureOutputPayload.APPLY_BLUE
                    | ConsoleFixtureOutputPayload.APPLY_DIMMER;
        }

        return ConsoleFixtureOutputPayload.APPLY_ALL;
    }

    private boolean isSelectedDmxMob() {
        FixtureBrowserEntry selected =
                getSelectedBrowserEntry();

        if (selected == null) {
            return false;
        }

        String fixtureType =
                selected.fixtureType();

        return DmxParrotProfile.ID.equals(
                fixtureType
        )
                || DmxEndermanProfile.ID.equals(
                        fixtureType
                )
                || DmxWardenProfile.ID.equals(
                        fixtureType
                )
                || DmxNautilusProfile.ID.equals(
                        fixtureType
                )
                || DmxCreakingProfile.ID.equals(
                        fixtureType
                )
                || DmxAxolotlProfile.ID.equals(
                        fixtureType
                );
    }

    private boolean isSelectedDmxBlockDisplay() {
        FixtureBrowserEntry selected = getSelectedBrowserEntry();
        return selected != null
                && DmxBlockDisplayProfile.ID.equals(
                        selected.fixtureType()
                );
    }

    private FixtureBrowserEntry findOutputGroupFixture() {
        if (selectedGroupKey != null) {
            for (FixtureBrowserEntry entry :
                    entries) {

                if (selectedGroupKey.equals(
                        normalizeGroupKey(
                                entry.groupName()
                        )
                )
                        && !entry.isUngrouped()) {

                    return entry;
                }
            }
        }

        FixtureBrowserEntry selected =
                getSelectedBrowserEntry();

        if (selected != null
                && !selected.isUngrouped()) {

            return selected;
        }

        return null;
    }

    private String getOutputTargetDescription() {
        if (ConsoleFixtureOutputPayload.TARGET_ALL.equals(
                outputTarget
        )) {
            return "all loaded fixtures";
        }

        if (ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                outputTarget
        )) {
            FixtureBrowserEntry selected =
                    getSelectedBrowserEntry();

            if (selected == null) {
                return "selected fixture";
            }

            return "fixture \""
                    + selected.fixtureName()
                    + "\"";
        }

        if (ConsoleFixtureOutputPayload.TARGET_GROUP.equals(
                outputTarget
        )) {
            if (selectedGroupKey != null) {
                GroupSummary group =
                        findGroupSummaryByKey(
                                selectedGroupKey
                        );

                if (group != null) {
                    return "group \""
                            + group.name()
                            + "\"";
                }
            }

            FixtureBrowserEntry selected =
                    getSelectedBrowserEntry();

            if (selected != null
                    && !selected.isUngrouped()) {

                return "group \""
                        + selected.groupName()
                        + "\"";
            }

            return "selected group";
        }

        return "unknown target";
    }

    private void setOutputError(
            String message
    ) {
        outputStatusMessage =
                Component.literal(
                        message
                );

        outputStatusColor =
                OUTPUT_ERROR_COLOR;
    }

    /*
     * -----------------------------------------------------------------
     * Browser snapshot
     * -----------------------------------------------------------------
     */

    public void refreshEntries() {
        entries =
                FixtureBrowserClientStore.getEntries();

        lastSeenRevision =
                FixtureBrowserClientStore.getRevision();

        rebuildGroupSummaries();
        rebuildUniverseSummaries();
        rebuildPatchResults();
        rebuildVisibleRows();

        restoreFixtureSelectionAfterRefresh();
        restoreGroupSelectionAfterRefresh();
        restoreUniverseSelectionAfterRefresh();

        calculatePagination();
    }

    private void rebuildVisibleRows() {
        if (fixtureGroupFilterKey == null) {
            visibleFixtureEntries =
                    entries;
        } else {
            List<FixtureBrowserEntry> filtered =
                    new ArrayList<>();

            for (FixtureBrowserEntry entry :
                    entries) {

                if (fixtureGroupFilterKey.equals(
                        normalizeGroupKey(
                                entry.groupName()
                        )
                )) {
                    filtered.add(
                            entry
                    );
                }
            }

            visibleFixtureEntries =
                    List.copyOf(
                            filtered
                    );
        }

        if (patchUniverseFilter == null) {
            visiblePatchResults =
                    patchResults;
        } else {
            List<PatchConflictResult> filtered =
                    new ArrayList<>();

            for (PatchConflictResult result :
                    patchResults) {

                if (result.entry().universe()
                        == patchUniverseFilter) {

                    filtered.add(
                            result
                    );
                }
            }

            visiblePatchResults =
                    List.copyOf(
                            filtered
                    );
        }
    }

    private void drillDownSelectedGroup() {
        GroupSummary group =
                getSelectedGroupSummary();

        if (group == null) {
            return;
        }

        fixtureGroupFilterKey =
                normalizeGroupKey(
                        group.name()
                );

        patchUniverseFilter =
                null;

        rebuildVisibleRows();

        currentView =
                ConsoleView.FIXTURES;

        currentPage =
                0;

        calculatePagination();
        updateOutputWidgetVisibility();
        updateButtonStates();
    }

    private void drillDownSelectedUniverse() {
        UniverseSummary universe =
                getSelectedUniverseSummary();

        if (universe == null) {
            return;
        }

        patchUniverseFilter =
                universe.universe();

        fixtureGroupFilterKey =
                null;

        rebuildVisibleRows();

        currentView =
                ConsoleView.PATCH;

        currentPage =
                0;

        calculatePagination();
        updateOutputWidgetVisibility();
        updateButtonStates();
    }

    private void clearViewFilters() {
        fixtureGroupFilterKey =
                null;

        patchUniverseFilter =
                null;

        rebuildVisibleRows();

        currentPage =
                0;

        calculatePagination();
        updateButtonStates();
    }

    private boolean hasActiveFilter() {
        return fixtureGroupFilterKey != null
                || patchUniverseFilter != null;
    }

    /*
     * -----------------------------------------------------------------
     * Groups
     * -----------------------------------------------------------------
     */

    private void rebuildGroupSummaries() {
        class MutableGroupSummary {

            private final String displayName;

            private int fixtureCount;
            private int dmxCount;
            private int manualCount;

            private MutableGroupSummary(
                    String displayName
            ) {
                this.displayName =
                        displayName;
            }

            private void add(
                    FixtureBrowserEntry entry
            ) {
                fixtureCount++;

                String mode =
                        entry.controlMode();

                if ("manual".equalsIgnoreCase(
                        mode
                )) {
                    manualCount++;
                } else if ("dmx".equalsIgnoreCase(
                        mode
                )) {
                    dmxCount++;
                }
            }

            private GroupSummary freeze() {
                return new GroupSummary(
                        displayName,
                        fixtureCount,
                        dmxCount,
                        manualCount
                );
            }
        }

        Map<String, MutableGroupSummary> groups =
                new LinkedHashMap<>();

        for (FixtureBrowserEntry entry :
                entries) {

            if (entry.isUngrouped()) {
                continue;
            }

            String groupName =
                    entry.groupName();

            if (groupName == null
                    || groupName.isBlank()) {

                continue;
            }

            String key =
                    normalizeGroupKey(
                            groupName
                    );

            groups.computeIfAbsent(
                    key,
                    ignored ->
                            new MutableGroupSummary(
                                    groupName.trim()
                            )
            ).add(
                    entry
            );
        }

        List<GroupSummary> summaries =
                new ArrayList<>();

        for (MutableGroupSummary summary :
                groups.values()) {

            summaries.add(
                    summary.freeze()
            );
        }

        summaries.sort(
                (first, second) ->
                        first.name()
                                .compareToIgnoreCase(
                                        second.name()
                                )
        );

        groupSummaries =
                List.copyOf(
                        summaries
                );
    }

    /*
     * -----------------------------------------------------------------
     * Universes
     * -----------------------------------------------------------------
     */

    private void rebuildUniverseSummaries() {
        class MutableUniverseSummary {

            private int fixtureCount;

            private final Set<Integer> usedChannels =
                    new TreeSet<>();

            private final Map<Integer, Integer> channelUseCounts =
                    new TreeMap<>();

            private void add(
                    FixtureBrowserEntry entry
            ) {
                fixtureCount++;

                for (int channel :
                        entry.assignedChannels()) {

                    usedChannels.add(
                            channel
                    );

                    channelUseCounts.merge(
                            channel,
                            1,
                            Integer::sum
                    );
                }
            }

            private UniverseSummary freeze(
                    int universe
            ) {
                int conflicts =
                        0;

                for (int count :
                        channelUseCounts.values()) {

                    if (count > 1) {
                        conflicts++;
                    }
                }

                return new UniverseSummary(
                        universe,
                        fixtureCount,
                        usedChannels.size(),
                        conflicts
                );
            }
        }

        Map<Integer, MutableUniverseSummary> universes =
                new TreeMap<>();

        for (FixtureBrowserEntry entry :
                entries) {

            universes.computeIfAbsent(
                    entry.universe(),
                    ignored ->
                            new MutableUniverseSummary()
            ).add(
                    entry
            );
        }

        List<UniverseSummary> summaries =
                new ArrayList<>();

        for (
                Map.Entry<
                        Integer,
                        MutableUniverseSummary
                > universeEntry :
                universes.entrySet()
        ) {
            summaries.add(
                    universeEntry
                            .getValue()
                            .freeze(
                                    universeEntry.getKey()
                            )
            );
        }

        universeSummaries =
                List.copyOf(
                        summaries
                );
    }

    /*
     * -----------------------------------------------------------------
     * Patch
     * -----------------------------------------------------------------
     */

    private void rebuildPatchResults() {
        List<PatchOccupancyEntry> occupancyEntries =
                new ArrayList<>();

        for (FixtureBrowserEntry entry :
                entries) {

            DmxFixtureProfile profile =
                    DmxFixtureProfileRegistry.getOrDefault(
                            entry.fixtureType()
                    );

            occupancyEntries.add(
                    new PatchOccupancyEntry(
                            entry.position(),
                            entry.fixtureName(),
                            profile.id(),
                            profile.displayName(),
                            entry.universe(),
                            entry.assignedChannels()
                    )
            );
        }

        occupancyEntries.sort(
                (first, second) -> {
                    int universeComparison =
                            Integer.compare(
                                    first.universe(),
                                    second.universe()
                            );

                    if (universeComparison != 0) {
                        return universeComparison;
                    }

                    int channelComparison =
                            Integer.compare(
                                    first.firstChannel(),
                                    second.firstChannel()
                            );

                    if (channelComparison != 0) {
                        return channelComparison;
                    }

                    return first.fixtureName()
                            .compareToIgnoreCase(
                                    second.fixtureName()
                            );
                }
        );

        patchResults =
                PatchConflictAnalyzer.analyze(
                        occupancyEntries
                );
    }

    /*
     * -----------------------------------------------------------------
     * Selection restoration
     * -----------------------------------------------------------------
     */

    private void restoreFixtureSelectionAfterRefresh() {
        if (selectedPosition != null
                && findBrowserEntryIndex(
                        selectedPosition,
                        selectedTargetKind,
                        selectedTargetEntityId
                ) < 0) {

            selectedPosition =
                    null;
            selectedTargetKind =
                    null;
            selectedTargetEntityId =
                    FixtureBrowserEntry.NO_TARGET_ENTITY_ID;
        }
    }

    private void restoreGroupSelectionAfterRefresh() {
        if (selectedGroupKey != null
                && findGroupSummaryIndex(
                        selectedGroupKey
                ) < 0) {

            selectedGroupKey =
                    null;
        }
    }

    private void restoreUniverseSelectionAfterRefresh() {
        if (selectedUniverse != null
                && findUniverseSummaryIndex(
                        selectedUniverse
                ) < 0) {

            selectedUniverse =
                    null;
        }
    }

    /*
     * -----------------------------------------------------------------
     * Refresh
     * -----------------------------------------------------------------
     */

    private void requestRefresh() {
        if (ClientPlayNetworking.canSend(
                RequestFixtureBrowserPayload.TYPE
        )) {
            ClientPlayNetworking.send(
                    new RequestFixtureBrowserPayload()
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Edit
     * -----------------------------------------------------------------
     */

    private void editSelectedFixture() {
        if (currentView
                != ConsoleView.FIXTURES
                && currentView
                != ConsoleView.PATCH) {

            return;
        }

        FixtureBrowserEntry selectedEntry =
                getSelectedBrowserEntry();

        if (selectedEntry == null) {
            return;
        }

        if (DmxParrotProfile.ID.equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxParrotScreen(
                            selectedEntry
                    )
            );
            return;
        }

        if (DmxEndermanProfile.ID.equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxEndermanScreen(
                            selectedEntry
                    )
            );
            return;
        }

        if (DmxWardenProfile.ID.equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxWardenScreen(
                            selectedEntry
                    )
            );
            return;
        }

        if (DmxNautilusProfile.ID.equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxNautilusScreen(
                            selectedEntry
                    )
            );
            return;
        }

        if (DmxCreakingProfile.ID.equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxCreakingScreen(selectedEntry)
            );
            return;
        }

        if (DmxAxolotlProfile.ID.equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxAxolotlScreen(selectedEntry)
            );
            return;
        }

        if (DmxBlockDisplayProfile.ID.equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxBlockDisplayScreen(selectedEntry)
            );
            return;
        }

        if ("dmx_pixel".equals(
                selectedEntry.fixtureType()
        )) {
            Minecraft.getInstance().setScreen(
                    new DmxPixelBlockScreen(
                            selectedEntry.position()
                    )
            );
        } else {
            Minecraft.getInstance().setScreen(
                    new DmxFixtureScreen(
                            selectedEntry.position()
                    )
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * View control
     * -----------------------------------------------------------------
     */

    private void setCurrentView(
            ConsoleView view
    ) {
        if (view == null
                || currentView == view) {

            return;
        }

        currentView =
                view;

        currentPage =
                0;

        if (currentView
                == ConsoleView.OUTPUT
                && ConsoleFixtureOutputPayload.TARGET_FIXTURE.equals(
                        outputTarget
                )) {

            loadOutputFromSelectedFixture();
        }

        calculatePagination();
        updateOutputWidgetVisibility();
        updateButtonStates();
    }

    /*
     * -----------------------------------------------------------------
     * Pagination
     * -----------------------------------------------------------------
     */

    private void calculatePagination() {
        int bodyHeight =
                getTableBottom()
                        - getTableTop()
                        - TABLE_HEADER_HEIGHT;

        rowsPerPage =
                Math.max(
                        1,
                        bodyHeight
                                / ROW_HEIGHT
                );

        int itemCount =
                getCurrentItemCount();

        totalPages =
                Math.max(
                        1,
                        (
                                itemCount
                                        + rowsPerPage
                                        - 1
                        )
                                / rowsPerPage
                );

        currentPage =
                Math.max(
                        0,
                        Math.min(
                                totalPages
                                        - 1,
                                currentPage
                        )
                );

        updateButtonStates();
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateButtonStates();
        }
    }

    private void nextPage() {
        if (currentPage
                < totalPages
                - 1) {

            currentPage++;
            updateButtonStates();
        }
    }

    private void updateButtonStates() {
        boolean outputView =
                currentView
                        == ConsoleView.OUTPUT;

        if (previousPageButton != null) {
            previousPageButton.visible =
                    !outputView;

            previousPageButton.active =
                    !outputView
                            && currentPage > 0;
        }

        if (nextPageButton != null) {
            nextPageButton.visible =
                    !outputView;

            nextPageButton.active =
                    !outputView
                            && currentPage
                            < totalPages
                            - 1;
        }

        if (editFixtureButton != null) {
            editFixtureButton.visible =
                    !outputView;

            editFixtureButton.active =
                    !outputView
                            && (
                            currentView
                                    == ConsoleView.FIXTURES
                                    || currentView
                                    == ConsoleView.PATCH
                    )
                            && getSelectedBrowserEntry()
                            != null;
        }

        if (showAllButton != null) {
            showAllButton.visible =
                    !outputView;

            showAllButton.active =
                    !outputView
                            && hasActiveFilter();
        }

        if (refreshButton != null) {
            refreshButton.visible =
                    true;

            refreshButton.active =
                    true;
        }

        if (closeButton != null) {
            closeButton.visible =
                    true;

            closeButton.active =
                    true;
        }
    }

    /*
     * -----------------------------------------------------------------
     * Mouse
     * -----------------------------------------------------------------
     */

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        double mouseX =
                event.x();

        double mouseY =
                event.y();

        if (event.button() == 0) {
            if (isInsideFixturesTab(
                    mouseX,
                    mouseY
            )) {
                setCurrentView(
                        ConsoleView.FIXTURES
                );

                return true;
            }

            if (isInsideGroupsTab(
                    mouseX,
                    mouseY
            )) {
                setCurrentView(
                        ConsoleView.GROUPS
                );

                return true;
            }

            if (isInsideUniversesTab(
                    mouseX,
                    mouseY
            )) {
                setCurrentView(
                        ConsoleView.UNIVERSES
                );

                return true;
            }

            if (isInsidePatchTab(
                    mouseX,
                    mouseY
            )) {
                setCurrentView(
                        ConsoleView.PATCH
                );

                return true;
            }

            if (isInsideOutputTab(
                    mouseX,
                    mouseY
            )) {
                setCurrentView(
                        ConsoleView.OUTPUT
                );

                return true;
            }

            if (currentView
                    == ConsoleView.OUTPUT) {

                return super.mouseClicked(
                        event,
                        doubleClick
                );
            }

            if (isInsideTableBody(
                    mouseX,
                    mouseY
            )) {
                int rowIndex =
                        (int) (
                                mouseY
                                        - getTableBodyTop()
                        )
                                / ROW_HEIGHT;

                int entryIndex =
                        currentPage
                                * rowsPerPage
                                + rowIndex;

                if (currentView
                        == ConsoleView.GROUPS) {

                    GroupSummary group =
                            getGroupSummaryAtIndex(
                                    entryIndex
                            );

                    if (group != null) {
                        selectedGroupKey =
                                normalizeGroupKey(
                                        group.name()
                                );

                        if (doubleClick) {
                            drillDownSelectedGroup();
                        }

                        return true;
                    }
                }

                if (currentView
                        == ConsoleView.UNIVERSES) {

                    UniverseSummary universe =
                            getUniverseSummaryAtIndex(
                                    entryIndex
                            );

                    if (universe != null) {
                        selectedUniverse =
                                universe.universe();

                        if (doubleClick) {
                            drillDownSelectedUniverse();
                        }

                        return true;
                    }
                }

                FixtureBrowserEntry selectedEntry =
                        getEntryAtCurrentViewIndex(
                                entryIndex
                        );

                if (selectedEntry != null) {
                    selectedPosition =
                            selectedEntry.position().immutable();
                    selectedTargetKind =
                            selectedEntry.targetKind();
                    selectedTargetEntityId =
                            selectedEntry.targetEntityId();

                    if (doubleClick) {
                        editSelectedFixture();
                    }

                    return true;
                }
            }
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (currentView
                == ConsoleView.OUTPUT) {

            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    horizontalAmount,
                    verticalAmount
            );
        }

        if (verticalAmount > 0.0D) {
            previousPage();
            return true;
        }

        if (verticalAmount < 0.0D) {
            nextPage();
            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
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
        long revision =
                FixtureBrowserClientStore.getRevision();

        if (revision
                != lastSeenRevision) {

            refreshEntries();
        }

        drawPanel(
                graphics
        );

        drawTitleArea(
                graphics
        );

        drawTabBar(
                graphics
        );

        switch (currentView) {
            case FIXTURES ->
                    drawFixtureTable(
                            graphics
                    );

            case GROUPS ->
                    drawGroupsTable(
                            graphics
                    );

            case UNIVERSES ->
                    drawUniversesTable(
                            graphics
                    );

            case PATCH ->
                    drawPatchTable(
                            graphics
                    );

            case OUTPUT ->
                    drawOutputSurface(
                            graphics
                    );
        }

        drawFooter(
                graphics
        );

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );
    }

    private void drawPanel(
            GuiGraphicsExtractor graphics
    ) {
        graphics.fill(
                getPanelLeft(),
                getPanelTop(),
                getPanelRight(),
                getPanelBottom(),
                PANEL_COLOR
        );

        drawBorder(
                graphics,
                getPanelLeft(),
                getPanelTop(),
                getPanelRight(),
                getPanelBottom(),
                BORDER_COLOR
        );
    }

    private void drawTitleArea(
            GuiGraphicsExtractor graphics
    ) {
        graphics.fill(
                getPanelLeft()
                        + 1,
                getPanelTop()
                        + 1,
                getPanelRight()
                        - 1,
                getPanelTop()
                        + TITLE_HEIGHT,
                HEADER_COLOR
        );

        graphics.centeredText(
                font,
                title,
                getPanelLeft()
                        + panelWidth
                        / 2,
                getPanelTop()
                        + 8,
                TEXT_COLOR
        );
    }

    private void drawTabBar(
            GuiGraphicsExtractor graphics
    ) {
        int left =
                getTabBarLeft();

        int tabWidth =
                getTabWidth();

        drawCenteredTabLabel(
                graphics,
                "Fix",
                FIXTURES_TAB_INDEX,
                left,
                tabWidth,
                currentView == ConsoleView.FIXTURES
        );

        drawCenteredTabLabel(
                graphics,
                "Grp",
                GROUPS_TAB_INDEX,
                left,
                tabWidth,
                currentView == ConsoleView.GROUPS
        );

        drawCenteredTabLabel(
                graphics,
                "Uni",
                UNIVERSES_TAB_INDEX,
                left,
                tabWidth,
                currentView == ConsoleView.UNIVERSES
        );

        drawCenteredTabLabel(
                graphics,
                "Patch",
                PATCH_TAB_INDEX,
                left,
                tabWidth,
                currentView == ConsoleView.PATCH
        );

        drawCenteredTabLabel(
                graphics,
                "Out",
                OUTPUT_TAB_INDEX,
                left,
                tabWidth,
                currentView == ConsoleView.OUTPUT
        );
    }

    private void drawCenteredTabLabel(
            GuiGraphicsExtractor graphics,
            String label,
            int tabIndex,
            int tabLeft,
            int tabWidth,
            boolean selected
    ) {
        String displayed =
                selected
                        ? "["
                        + label
                        + "]"
                        : label;

        int centerX =
                tabLeft
                        + tabIndex
                        * tabWidth
                        + tabWidth
                        / 2;

        graphics.centeredText(
                font,
                Component.literal(
                        displayed
                ),
                centerX,
                getPanelTop()
                        + TITLE_HEIGHT
                        + 8,
                selected
                        ? TEXT_COLOR
                        : MUTED_TEXT_COLOR
        );
    }

    private void drawOutputSurface(
            GuiGraphicsExtractor graphics
    ) {
        graphics.fill(
                getTableLeft(),
                getTableTop(),
                getTableRight(),
                getTableBottom(),
                0xFF101010
        );

        drawBorder(
                graphics,
                getTableLeft(),
                getTableTop(),
                getTableRight(),
                getTableBottom(),
                BORDER_COLOR
        );

        graphics.centeredText(
                font,
                Component.literal(
                        getOutputTargetDescription()
                ),
                getPanelLeft()
                        + panelWidth
                        / 2,
                getTableTop()
                        + 31,
                MUTED_TEXT_COLOR
        );
    }

    /*
     * -----------------------------------------------------------------
     * Fixture table
     * -----------------------------------------------------------------
     */

    private void drawFixtureTable(
            GuiGraphicsExtractor graphics
    ) {
        drawTableSurface(
                graphics
        );

        int x =
                getTableLeft()
                        + 5;

        int y =
                getTableTop()
                        + 5;

        graphics.text(
                font,
                "Out",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                COLOR_COLUMN_WIDTH;

        graphics.text(
                font,
                "Name",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                nameColumnWidth;

        if (hasGroupColumn()) {
            graphics.text(
                    font,
                    "Group",
                    x,
                    y,
                    MUTED_TEXT_COLOR,
                    false
            );

            x +=
                    groupColumnWidth;
        }

        graphics.text(
                font,
                "Patch",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        int first =
                currentPage
                        * rowsPerPage;

        int last =
                Math.min(
                        visibleFixtureEntries.size(),
                        first
                                + rowsPerPage
                );

        for (
                int index = first;
                index < last;
                index++
        ) {
            FixtureBrowserEntry entry =
                    visibleFixtureEntries.get(
                            index
                    );

            int visibleIndex =
                    index
                            - first;

            drawFixtureRow(
                    graphics,
                    entry,
                    visibleIndex,
                    getTableBodyTop()
                            + visibleIndex
                            * ROW_HEIGHT
            );
        }
    }

    private void drawFixtureRow(
            GuiGraphicsExtractor graphics,
            FixtureBrowserEntry entry,
            int visibleIndex,
            int rowTop
    ) {
        boolean selected =
                isSelected(
                        entry
                );

        int rowColor =
                selected
                        ? SELECTED_ROW_COLOR
                        : visibleIndex % 2 == 0
                        ? ROW_COLOR_A
                        : ROW_COLOR_B;

        drawRowBackground(
                graphics,
                rowTop,
                rowColor,
                selected
        );

        int x =
                getTableLeft()
                        + 5;

        int textY =
                rowTop
                        + 5;

        graphics.fill(
                x,
                rowTop
                        + 4,
                x
                        + 10,
                rowTop
                        + 14,
                entry.getArgbColor()
        );

        graphics.fill(
                x
                        + 13,
                rowTop
                        + 4,
                x
                        + 18,
                rowTop
                        + 14,
                getControlModeIndicatorColor(
                        entry.controlMode()
                )
        );

        x +=
                COLOR_COLUMN_WIDTH;

        graphics.text(
                font,
                fitText(
                        entry.fixtureName(),
                        nameColumnWidth
                                - 4
                ),
                x,
                textY,
                TEXT_COLOR,
                false
        );

        x +=
                nameColumnWidth;

        if (hasGroupColumn()) {
            graphics.text(
                    font,
                    fitText(
                            entry.groupName(),
                            groupColumnWidth
                                    - 4
                    ),
                    x,
                    textY,
                    TEXT_COLOR,
                    false
            );

            x +=
                    groupColumnWidth;
        }

        graphics.text(
                font,
                fitText(
                        entry.getPatchLabel(),
                        FIXTURE_PATCH_COLUMN_WIDTH
                                - 4
                ),
                x,
                textY,
                TEXT_COLOR,
                false
        );
    }

    /*
     * -----------------------------------------------------------------
     * Group table
     * -----------------------------------------------------------------
     */

    private void drawGroupsTable(
            GuiGraphicsExtractor graphics
    ) {
        drawTableSurface(
                graphics
        );

        int x =
                getTableLeft()
                        + 5;

        int y =
                getTableTop()
                        + 5;

        graphics.text(
                font,
                "Group",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                groupSummaryNameColumnWidth;

        graphics.text(
                font,
                "#",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                GROUP_COUNT_COLUMN_WIDTH;

        graphics.text(
                font,
                "Mode",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        int first =
                currentPage
                        * rowsPerPage;

        int last =
                Math.min(
                        groupSummaries.size(),
                        first
                                + rowsPerPage
                );

        for (
                int index = first;
                index < last;
                index++
        ) {
            GroupSummary summary =
                    groupSummaries.get(
                            index
                    );

            int visibleIndex =
                    index
                            - first;

            boolean selected =
                    isSelectedGroup(
                            summary.name()
                    );

            drawRowBackground(
                    graphics,
                    getTableBodyTop()
                            + visibleIndex
                            * ROW_HEIGHT,
                    selected
                            ? SELECTED_ROW_COLOR
                            : visibleIndex % 2 == 0
                            ? ROW_COLOR_A
                            : ROW_COLOR_B,
                    selected
            );

            int rowX =
                    getTableLeft()
                            + 5;

            int rowY =
                    getTableBodyTop()
                            + visibleIndex
                            * ROW_HEIGHT
                            + 5;

            graphics.text(
                    font,
                    fitText(
                            summary.name(),
                            groupSummaryNameColumnWidth
                                    - 4
                    ),
                    rowX,
                    rowY,
                    TEXT_COLOR,
                    false
            );

            rowX +=
                    groupSummaryNameColumnWidth;

            graphics.text(
                    font,
                    Integer.toString(
                            summary.fixtureCount()
                    ),
                    rowX,
                    rowY,
                    TEXT_COLOR,
                    false
            );

            rowX +=
                    GROUP_COUNT_COLUMN_WIDTH;

            graphics.text(
                    font,
                    summary.modeSummary(),
                    rowX,
                    rowY,
                    TEXT_COLOR,
                    false
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Universe table
     * -----------------------------------------------------------------
     */

    private void drawUniversesTable(
            GuiGraphicsExtractor graphics
    ) {
        drawTableSurface(
                graphics
        );

        int x =
                getTableLeft()
                        + 5;

        int y =
                getTableTop()
                        + 5;

        graphics.text(
                font,
                "Uni",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                UNIVERSE_NUMBER_COLUMN_WIDTH;

        graphics.text(
                font,
                "Fix",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                UNIVERSE_FIXTURE_COLUMN_WIDTH;

        graphics.text(
                font,
                "Used",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                UNIVERSE_USED_COLUMN_WIDTH;

        graphics.text(
                font,
                "Conflict",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        int first =
                currentPage
                        * rowsPerPage;

        int last =
                Math.min(
                        universeSummaries.size(),
                        first
                                + rowsPerPage
                );

        for (
                int index = first;
                index < last;
                index++
        ) {
            UniverseSummary summary =
                    universeSummaries.get(
                            index
                    );

            int visibleIndex =
                    index
                            - first;

            int rowTop =
                    getTableBodyTop()
                            + visibleIndex
                            * ROW_HEIGHT;

            boolean selected =
                    selectedUniverse != null
                            && selectedUniverse
                            == summary.universe();

            int rowColor =
                    selected
                            ? SELECTED_ROW_COLOR
                            : summary.hasConflicts()
                            ? PATCH_CONFLICT_ROW_COLOR
                            : visibleIndex % 2 == 0
                            ? ROW_COLOR_A
                            : ROW_COLOR_B;

            drawRowBackground(
                    graphics,
                    rowTop,
                    rowColor,
                    selected
            );

            int rowX =
                    getTableLeft()
                            + 5;

            int rowY =
                    rowTop
                            + 5;

            graphics.text(
                    font,
                    Integer.toString(
                            summary.universe()
                    ),
                    rowX,
                    rowY,
                    TEXT_COLOR,
                    false
            );

            rowX +=
                    UNIVERSE_NUMBER_COLUMN_WIDTH;

            graphics.text(
                    font,
                    Integer.toString(
                            summary.fixtureCount()
                    ),
                    rowX,
                    rowY,
                    TEXT_COLOR,
                    false
            );

            rowX +=
                    UNIVERSE_FIXTURE_COLUMN_WIDTH;

            graphics.text(
                    font,
                    Integer.toString(
                            summary.usedChannelCount()
                    ),
                    rowX,
                    rowY,
                    TEXT_COLOR,
                    false
            );

            rowX +=
                    UNIVERSE_USED_COLUMN_WIDTH;

            graphics.text(
                    font,
                    Integer.toString(
                            summary.conflictChannelCount()
                    ),
                    rowX,
                    rowY,
                    summary.hasConflicts()
                            ? PATCH_CONFLICT_COLOR
                            : PATCH_OK_COLOR,
                    false
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Patch table
     * -----------------------------------------------------------------
     */

    private void drawPatchTable(
            GuiGraphicsExtractor graphics
    ) {
        drawTableSurface(
                graphics
        );

        int x =
                getTableLeft()
                        + 5;

        int y =
                getTableTop()
                        + 5;

        graphics.text(
                font,
                "!",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                PATCH_STATUS_COLUMN_WIDTH;

        graphics.text(
                font,
                "DMX",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                PATCH_RANGE_COLUMN_WIDTH;

        graphics.text(
                font,
                "Profile",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        x +=
                PATCH_PROFILE_COLUMN_WIDTH;

        graphics.text(
                font,
                "Fixture",
                x,
                y,
                MUTED_TEXT_COLOR,
                false
        );

        int first =
                currentPage
                        * rowsPerPage;

        int last =
                Math.min(
                        visiblePatchResults.size(),
                        first
                                + rowsPerPage
                );

        for (
                int index = first;
                index < last;
                index++
        ) {
            PatchConflictResult result =
                    visiblePatchResults.get(
                            index
                    );

            int visibleIndex =
                    index
                            - first;

            drawPatchRow(
                    graphics,
                    result,
                    visibleIndex,
                    getTableBodyTop()
                            + visibleIndex
                            * ROW_HEIGHT
            );
        }
    }

    private void drawPatchRow(
            GuiGraphicsExtractor graphics,
            PatchConflictResult result,
            int visibleIndex,
            int rowTop
    ) {
        PatchOccupancyEntry entry =
                result.entry();

        boolean selected =
                isSelected(
                        findBrowserEntry(
                                entry
                        )
                );

        int rowColor =
                selected
                        ? SELECTED_ROW_COLOR
                        : result.isOutOfRange()
                        ? PATCH_OUT_OF_RANGE_ROW_COLOR
                        : result.hasConflict()
                        ? PATCH_CONFLICT_ROW_COLOR
                        : visibleIndex % 2 == 0
                        ? ROW_COLOR_A
                        : ROW_COLOR_B;

        drawRowBackground(
                graphics,
                rowTop,
                rowColor,
                selected
        );

        int x =
                getTableLeft()
                        + 5;

        int y =
                rowTop
                        + 5;

        graphics.fill(
                x
                        + 3,
                rowTop
                        + 5,
                x
                        + 9,
                rowTop
                        + 11,
                getPatchStatusColor(
                        result
                )
        );

        x +=
                PATCH_STATUS_COLUMN_WIDTH;

        graphics.text(
                font,
                fitText(
                        entry.patchRangeLabel(),
                        PATCH_RANGE_COLUMN_WIDTH
                                - 4
                ),
                x,
                y,
                TEXT_COLOR,
                false
        );

        x +=
                PATCH_RANGE_COLUMN_WIDTH;

        graphics.text(
                font,
                fitText(
                        entry.profileDisplayName(),
                        PATCH_PROFILE_COLUMN_WIDTH
                                - 4
                ),
                x,
                y,
                TEXT_COLOR,
                false
        );

        x +=
                PATCH_PROFILE_COLUMN_WIDTH;

        graphics.text(
                font,
                fitText(
                        entry.fixtureName(),
                        patchNameColumnWidth
                                - 4
                ),
                x,
                y,
                TEXT_COLOR,
                false
        );
    }

    /*
     * -----------------------------------------------------------------
     * Shared rendering
     * -----------------------------------------------------------------
     */

    private void drawTableSurface(
            GuiGraphicsExtractor graphics
    ) {
        graphics.fill(
                getTableLeft(),
                getTableTop(),
                getTableRight(),
                getTableBottom(),
                0xFF101010
        );

        drawBorder(
                graphics,
                getTableLeft(),
                getTableTop(),
                getTableRight(),
                getTableBottom(),
                BORDER_COLOR
        );

        graphics.fill(
                getTableLeft()
                        + 1,
                getTableTop()
                        + 1,
                getTableRight()
                        - 1,
                getTableTop()
                        + TABLE_HEADER_HEIGHT,
                HEADER_COLOR
        );
    }

    private void drawRowBackground(
            GuiGraphicsExtractor graphics,
            int rowTop,
            int rowColor,
            boolean selected
    ) {
        graphics.fill(
                getTableLeft()
                        + 1,
                rowTop,
                getTableRight()
                        - 1,
                rowTop
                        + ROW_HEIGHT,
                rowColor
        );

        if (selected) {
            graphics.fill(
                    getTableLeft()
                            + 1,
                    rowTop,
                    getTableLeft()
                            + 3,
                    rowTop
                            + ROW_HEIGHT,
                    SELECTED_ROW_BORDER
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Footer
     * -----------------------------------------------------------------
     */

    private void drawFooter(
            GuiGraphicsExtractor graphics
    ) {
        int footerTop =
                getPanelBottom()
                        - FOOTER_HEIGHT;

        graphics.fill(
                getPanelLeft()
                        + 1,
                footerTop,
                getPanelRight()
                        - 1,
                getPanelBottom()
                        - 1,
                HEADER_COLOR
        );

        if (currentView
                == ConsoleView.OUTPUT) {

            graphics.centeredText(
                    font,
                    outputStatusMessage,
                    getPanelLeft()
                            + panelWidth
                            / 2,
                    footerTop
                            + 12,
                    outputStatusColor
            );

            return;
        }

        graphics.text(
                font,
                getFooterLeftLabel(),
                getPanelLeft()
                        + 8,
                footerTop
                        + 8,
                TEXT_COLOR,
                false
        );

        graphics.text(
                font,
                getPageLabel(),
                getPanelRight()
                        - 8
                        - font.width(
                        getPageLabel()
                ),
                footerTop
                        + 8,
                MUTED_TEXT_COLOR,
                false
        );

        graphics.centeredText(
                font,
                Component.literal(
                        fitText(
                                getSelectedFooterLabel(),
                                panelWidth
                                        - 20
                        )
                ),
                getPanelLeft()
                        + panelWidth
                        / 2,
                footerTop
                        + 27,
                MUTED_TEXT_COLOR
        );
    }

    private String getFooterLeftLabel() {
        return switch (currentView) {
            case FIXTURES ->
                    "Fixtures "
                            + visibleFixtureEntries.size();

            case GROUPS ->
                    "Groups "
                            + groupSummaries.size();

            case UNIVERSES ->
                    "Universes "
                            + universeSummaries.size();

            case PATCH ->
                    "Patch "
                            + visiblePatchResults.size();

            case OUTPUT ->
                    "Output";
        };
    }

    private String getSelectedFooterLabel() {
        if (currentView
                == ConsoleView.GROUPS) {

            GroupSummary group =
                    getSelectedGroupSummary();

            return group == null
                    ? "Double-click group to open"
                    : group.name();
        }

        if (currentView
                == ConsoleView.UNIVERSES) {

            UniverseSummary universe =
                    getSelectedUniverseSummary();

            return universe == null
                    ? "Double-click universe to inspect patch"
                    : "Universe "
                    + universe.universe();
        }

        FixtureBrowserEntry entry =
                getSelectedBrowserEntry();

        return entry == null
                ? "Select a fixture"
                : entry.fixtureName();
    }

    /*
     * -----------------------------------------------------------------
     * Lookup helpers
     * -----------------------------------------------------------------
     */

    private FixtureBrowserEntry getSelectedBrowserEntry() {
        if (selectedPosition == null) {
            return null;
        }

        int index =
                findBrowserEntryIndex(
                        selectedPosition,
                        selectedTargetKind,
                        selectedTargetEntityId
                );

        return index < 0
                ? null
                : entries.get(
                        index
                );
    }

    private GroupSummary getSelectedGroupSummary() {
        if (selectedGroupKey == null) {
            return null;
        }

        int index =
                findGroupSummaryIndex(
                        selectedGroupKey
                );

        return index < 0
                ? null
                : groupSummaries.get(
                        index
                );
    }

    private UniverseSummary getSelectedUniverseSummary() {
        if (selectedUniverse == null) {
            return null;
        }

        int index =
                findUniverseSummaryIndex(
                        selectedUniverse
                );

        return index < 0
                ? null
                : universeSummaries.get(
                        index
                );
    }

    private GroupSummary getGroupSummaryAtIndex(
            int index
    ) {
        if (index < 0
                || index >= groupSummaries.size()) {

            return null;
        }

        return groupSummaries.get(
                index
        );
    }

    private UniverseSummary getUniverseSummaryAtIndex(
            int index
    ) {
        if (index < 0
                || index >= universeSummaries.size()) {

            return null;
        }

        return universeSummaries.get(
                index
        );
    }

    private GroupSummary findGroupSummaryByKey(
            String key
    ) {
        int index =
                findGroupSummaryIndex(
                        key
                );

        return index < 0
                ? null
                : groupSummaries.get(
                        index
                );
    }

    private int findBrowserEntryIndex(
            BlockPos position,
            String targetKind,
            int targetEntityId
    ) {
        for (
                int index = 0;
                index < entries.size();
                index++
        ) {
            FixtureBrowserEntry entry = entries.get(index);
            boolean entityMatch = targetEntityId
                    != FixtureBrowserEntry.NO_TARGET_ENTITY_ID
                    && entry.targetEntityId() == targetEntityId;
            boolean blockMatch = targetEntityId
                    == FixtureBrowserEntry.NO_TARGET_ENTITY_ID
                    && entry.targetEntityId()
                    == FixtureBrowserEntry.NO_TARGET_ENTITY_ID
                    && position.equals(entry.position())
                    && java.util.Objects.equals(
                            targetKind,
                            entry.targetKind()
                    );

            if (entityMatch || blockMatch) {
                return index;
            }
        }

        return -1;
    }

    private int findGroupSummaryIndex(
            String groupKey
    ) {
        if (groupKey == null) {
            return -1;
        }

        for (
                int index = 0;
                index < groupSummaries.size();
                index++
        ) {
            if (groupKey.equals(
                    normalizeGroupKey(
                            groupSummaries
                                    .get(
                                            index
                                    )
                                    .name()
                    )
            )) {
                return index;
            }
        }

        return -1;
    }

    private int findUniverseSummaryIndex(
            int universe
    ) {
        for (
                int index = 0;
                index < universeSummaries.size();
                index++
        ) {
            if (universeSummaries
                    .get(
                            index
                    )
                    .universe()
                    == universe) {

                return index;
            }
        }

        return -1;
    }

    private FixtureBrowserEntry getEntryAtCurrentViewIndex(
            int index
    ) {
        if (currentView
                == ConsoleView.PATCH) {

            if (index < 0
                    || index
                    >= visiblePatchResults.size()) {

                return null;
            }

            return findBrowserEntry(
                    visiblePatchResults
                            .get(
                                    index
                            )
                            .entry()
            );
        }

        if (currentView
                == ConsoleView.FIXTURES) {

            if (index < 0
                    || index
                    >= visibleFixtureEntries.size()) {

                return null;
            }

            return visibleFixtureEntries.get(index);
        }

        return null;
    }

    private boolean isSelected(
            FixtureBrowserEntry entry
    ) {
        if (entry == null || selectedPosition == null) {
            return false;
        }

        if (selectedTargetEntityId
                != FixtureBrowserEntry.NO_TARGET_ENTITY_ID) {
            return selectedTargetEntityId == entry.targetEntityId();
        }

        return entry.targetEntityId()
                == FixtureBrowserEntry.NO_TARGET_ENTITY_ID
                && selectedPosition.equals(entry.position())
                && java.util.Objects.equals(
                        selectedTargetKind,
                        entry.targetKind()
                );
    }

    private FixtureBrowserEntry findBrowserEntry(
            PatchOccupancyEntry occupancy
    ) {
        if (occupancy == null) {
            return null;
        }

        for (FixtureBrowserEntry entry : entries) {
            if (occupancy.position().equals(entry.position())
                    && occupancy.fixtureName().equals(entry.fixtureName())
                    && occupancy.profileId().equals(entry.fixtureType())) {
                return entry;
            }
        }

        return null;
    }

    private boolean isSelectedGroup(
            String groupName
    ) {
        return selectedGroupKey != null
                && selectedGroupKey.equals(
                        normalizeGroupKey(
                                groupName
                        )
                );
    }

    private static String normalizeGroupKey(
            String groupName
    ) {
        if (groupName == null) {
            return "";
        }

        return groupName
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    /*
     * -----------------------------------------------------------------
     * Tab hitboxes
     * -----------------------------------------------------------------
     */

    private boolean isInsideFixturesTab(
            double x,
            double y
    ) {
        return isInsideTab(
                x,
                y,
                FIXTURES_TAB_INDEX
        );
    }

    private boolean isInsideGroupsTab(
            double x,
            double y
    ) {
        return isInsideTab(
                x,
                y,
                GROUPS_TAB_INDEX
        );
    }

    private boolean isInsideUniversesTab(
            double x,
            double y
    ) {
        return isInsideTab(
                x,
                y,
                UNIVERSES_TAB_INDEX
        );
    }

    private boolean isInsidePatchTab(
            double x,
            double y
    ) {
        return isInsideTab(
                x,
                y,
                PATCH_TAB_INDEX
        );
    }

    private boolean isInsideOutputTab(
            double x,
            double y
    ) {
        return isInsideTab(
                x,
                y,
                OUTPUT_TAB_INDEX
        );
    }

    private boolean isInsideTab(
            double x,
            double y,
            int tab
    ) {
        int tabWidth =
                getTabWidth();

        int left =
                getTabBarLeft()
                        + tab
                        * tabWidth;

        int right =
                tab == TAB_COUNT
                        - 1
                        ? getTabBarRight()
                        : left
                        + tabWidth;

        int top =
                getPanelTop()
                        + TITLE_HEIGHT;

        int bottom =
                top
                        + TAB_HEIGHT;

        return x >= left
                && x < right
                && y >= top
                && y < bottom;
    }

    private boolean isInsideTableBody(
            double x,
            double y
    ) {
        return x >= getTableLeft()
                && x < getTableRight()
                && y >= getTableBodyTop()
                && y < getTableBottom();
    }

    /*
     * -----------------------------------------------------------------
     * Formatting
     * -----------------------------------------------------------------
     */

    private int getControlModeIndicatorColor(
            String controlMode
    ) {
        if (controlMode == null) {
            return UNKNOWN_MODE_COLOR;
        }

        return switch (
                controlMode.toLowerCase(
                        Locale.ROOT
                )
        ) {
            case "dmx" ->
                    DMX_MODE_COLOR;

            case "manual" ->
                    MANUAL_MODE_COLOR;

            default ->
                    UNKNOWN_MODE_COLOR;
        };
    }

    private int getPatchStatusColor(
            PatchConflictResult result
    ) {
        if (result.isOutOfRange()) {
            return PATCH_OUT_OF_RANGE_COLOR;
        }

        if (result.hasConflict()) {
            return PATCH_CONFLICT_COLOR;
        }

        return PATCH_OK_COLOR;
    }

    private int getCurrentItemCount() {
        return switch (currentView) {
            case FIXTURES ->
                    visibleFixtureEntries.size();

            case GROUPS ->
                    groupSummaries.size();

            case UNIVERSES ->
                    universeSummaries.size();

            case PATCH ->
                    visiblePatchResults.size();

            case OUTPUT ->
                    0;
        };
    }

    private boolean hasGroupColumn() {
        return panelWidth >= 345;
    }

    private boolean hasPositionColumn() {
        return false;
    }

    private boolean hasModeColumn() {
        return false;
    }

    private boolean hasFixtureProfileColumn() {
        return false;
    }

    private boolean hasPatchPositionColumn() {
        return false;
    }

    private String getPageLabel() {
        return (currentPage + 1)
                + "/"
                + totalPages;
    }

    private String fitText(
            String text,
            int maximumWidth
    ) {
        String safe =
                text == null
                        ? ""
                        : text;

        if (font.width(
                safe
        ) <= maximumWidth) {

            return safe;
        }

        String ellipsis =
                "...";

        StringBuilder result =
                new StringBuilder();

        for (
                int index = 0;
                index < safe.length();
                index++
        ) {
            String candidate =
                    result.toString()
                            + safe.charAt(
                            index
                    );

            if (font.width(
                    candidate
                            + ellipsis
            ) > maximumWidth) {

                break;
            }

            result.append(
                    safe.charAt(
                            index
                    )
            );
        }

        return result
                + ellipsis;
    }

    private void drawBorder(
            GuiGraphicsExtractor graphics,
            int left,
            int top,
            int right,
            int bottom,
            int color
    ) {
        graphics.fill(
                left,
                top,
                right,
                top + 1,
                color
        );

        graphics.fill(
                left,
                bottom - 1,
                right,
                bottom,
                color
        );

        graphics.fill(
                left,
                top,
                left + 1,
                bottom,
                color
        );

        graphics.fill(
                right - 1,
                top,
                right,
                bottom,
                color
        );
    }

    /*
     * -----------------------------------------------------------------
     * Geometry
     * -----------------------------------------------------------------
     */

    private int getPanelLeft() {
        return panelLeft;
    }

    private int getPanelRight() {
        return panelLeft
                + panelWidth;
    }

    private int getPanelTop() {
        return panelTop;
    }

    private int getPanelBottom() {
        return panelTop
                + panelHeight;
    }

    private int getTabBarLeft() {
        return getPanelLeft()
                + TAB_HORIZONTAL_MARGIN;
    }

    private int getTabBarRight() {
        return getPanelRight()
                - TAB_HORIZONTAL_MARGIN;
    }

    private int getTabWidth() {
        return Math.max(
                1,
                (
                        getTabBarRight()
                                - getTabBarLeft()
                )
                        / TAB_COUNT
        );
    }

    private int getTableLeft() {
        return getPanelLeft()
                + TABLE_HORIZONTAL_MARGIN;
    }

    private int getTableRight() {
        return getPanelRight()
                - TABLE_HORIZONTAL_MARGIN;
    }

    private int getTableTop() {
        return getPanelTop()
                + HEADER_HEIGHT;
    }

    private int getTableBodyTop() {
        return getTableTop()
                + TABLE_HEADER_HEIGHT;
    }

    private int getTableBottom() {
        return getPanelBottom()
                - FOOTER_HEIGHT
                - 4;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
