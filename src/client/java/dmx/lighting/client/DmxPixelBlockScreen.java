package dmx.lighting.client;

import dmx.lighting.DmxFixtureBlockEntity;
import dmx.lighting.DmxPixelBlockEntity;
import dmx.lighting.FixtureParameterMap;
import dmx.lighting.ManualFixtureOutputPayload;
import dmx.lighting.SetFixtureModePayload;
import dmx.lighting.UpdateFixtureColorInterpolationPayload;
import dmx.lighting.UpdateFixtureGroupPayload;
import dmx.lighting.UpdateFixtureParameterMapPayload;
import dmx.lighting.UpdateFixturePayload;
import dmx.lighting.UpdatePixelBlockSkinPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Compact editor for a DMX Block.
 *
 * Only RGB, Dimmer, and Strobe are exposed. Existing fixture payloads
 * are reused so validation and console behavior remain consistent.
 */
public final class DmxPixelBlockScreen
        extends Screen {

    private static final int FIELD_HEIGHT =
            20;

    private static final int ROW_SPACING =
            26;

    private static final int CHANNEL_FIELD_WIDTH =
            64;

    private static final int SLIDER_WIDTH =
            220;

    private final BlockPos blockPosition;

    private EditBox nameField;
    private EditBox universeField;
    private EditBox groupField;

    private EditBox redChannelField;
    private EditBox greenChannelField;
    private EditBox blueChannelField;
    private EditBox dimmerChannelField;
    private EditBox strobeChannelField;
    private EditBox skinDmxChannelField;

    private DmxValueSlider redSlider;
    private DmxValueSlider greenSlider;
    private DmxValueSlider blueSlider;
    private DmxValueSlider dimmerSlider;
    private DmxValueSlider strobeSlider;

    private Button dmxModeButton;
    private Button manualModeButton;
    private Button skinButton;
    private Button colorInterpolationButton;

    private EditBox colorInterpolationTimeField;

    private Component statusMessage =
            Component.empty();

    private int statusColor =
            0xFFFFFFFF;

    private int manualRed =
            255;

    private int manualGreen =
            255;

    private int manualBlue =
            255;

    private int manualDimmer =
            255;

    private int manualStrobe;

    private String controlMode =
            "dmx";

    private int selectedSkin =
            DmxPixelBlockEntity.MIN_SKIN;

    private boolean colorInterpolationEnabled;

    private float colorInterpolationTimeSeconds =
            DmxFixtureBlockEntity
                    .DEFAULT_COLOR_INTERPOLATION_TIME_SECONDS;

    private boolean screenReady;

    public DmxPixelBlockScreen(
            BlockPos blockPosition
    ) {
        super(
                Component.literal(
                        "DMX Block"
                )
        );

        this.blockPosition =
                blockPosition.immutable();
    }

    @Override
    protected void init() {
        screenReady =
                false;

        DmxPixelBlockEntity blockEntity =
                getBlockEntity();

        loadValues(
                blockEntity
        );

        int centerX =
                width / 2;

        int generalLabelX =
                centerX - 230;

        int generalFieldX =
                centerX - 120;

        int top =
                38;

        nameField =
                createTextField(
                        generalFieldX,
                        top,
                        240,
                        64,
                        blockEntity == null
                                ? ""
                                : blockEntity.getFixtureName(),
                        "DMX Block name"
                );

        universeField =
                createTextField(
                        generalFieldX,
                        top + ROW_SPACING,
                        80,
                        3,
                        blockEntity == null
                                ? "1"
                                : Integer.toString(
                                        blockEntity.getUniverse()
                                ),
                        "DMX universe"
                );

        groupField =
                createTextField(
                        generalFieldX,
                        top + ROW_SPACING * 2,
                        240,
                        64,
                        blockEntity == null
                                ? ""
                                : blockEntity.getGroupDisplayName(),
                        "DMX group"
                );

        skinButton =
                addRenderableWidget(
                        Button.builder(
                                skinLabel(),
                                button -> selectNextSkin()
                        )
                        .bounds(
                                generalFieldX,
                                top + ROW_SPACING * 3,
                                100,
                                FIELD_HEIGHT
                        )
                        .build()
                );

        skinDmxChannelField =
                createChannelField(
                        generalFieldX + 110,
                        top + ROW_SPACING * 3,
                        blockEntity == null
                                ? FixtureParameterMap.UNASSIGNED
                                : blockEntity.getSkinDmxChannel(),
                        "Skin DMX channel"
                );

        FixtureParameterMap map =
                blockEntity == null
                        ? new FixtureParameterMap()
                        : blockEntity.getParameterMap();

        int channelX =
                centerX - 225;

        int controlsTop =
                top + ROW_SPACING * 4 + 18;

        redChannelField =
                createChannelField(
                        channelX,
                        controlsTop,
                        map.getRedChannel(),
                        "Red DMX channel"
                );

        greenChannelField =
                createChannelField(
                        channelX,
                        controlsTop + ROW_SPACING,
                        map.getGreenChannel(),
                        "Green DMX channel"
                );

        blueChannelField =
                createChannelField(
                        channelX,
                        controlsTop + ROW_SPACING * 2,
                        map.getBlueChannel(),
                        "Blue DMX channel"
                );

        dimmerChannelField =
                createChannelField(
                        channelX,
                        controlsTop + ROW_SPACING * 3,
                        map.getDimmerChannel(),
                        "Dimmer DMX channel"
                );

        strobeChannelField =
                createChannelField(
                        channelX,
                        controlsTop + ROW_SPACING * 4,
                        map.getStrobeChannel(),
                        "Strobe DMX channel"
                );

        int sliderX =
                centerX + 5;

        redSlider =
                createSlider(
                        sliderX,
                        controlsTop,
                        "Red",
                        manualRed,
                        value -> manualRed = value
                );

        greenSlider =
                createSlider(
                        sliderX,
                        controlsTop + ROW_SPACING,
                        "Green",
                        manualGreen,
                        value -> manualGreen = value
                );

        blueSlider =
                createSlider(
                        sliderX,
                        controlsTop + ROW_SPACING * 2,
                        "Blue",
                        manualBlue,
                        value -> manualBlue = value
                );

        dimmerSlider =
                createSlider(
                        sliderX,
                        controlsTop + ROW_SPACING * 3,
                        "Dimmer",
                        manualDimmer,
                        value -> manualDimmer = value
                );

        strobeSlider =
                createSlider(
                        sliderX,
                        controlsTop + ROW_SPACING * 4,
                        "Strobe",
                        manualStrobe,
                        value -> manualStrobe = value
                );

        int modeY =
                controlsTop + ROW_SPACING * 6 + 12;

        int fadeY =
                controlsTop + ROW_SPACING * 5 + 8;

        colorInterpolationButton =
                addRenderableWidget(
                        Button.builder(
                                getColorInterpolationButtonMessage(),
                                button -> toggleColorInterpolation()
                        )
                        .bounds(
                                centerX - 110,
                                fadeY,
                                105,
                                FIELD_HEIGHT
                        )
                        .build()
                );

        colorInterpolationTimeField =
                createTextField(
                        centerX + 5,
                        fadeY,
                        72,
                        6,
                        formatInterpolationSeconds(
                                colorInterpolationTimeSeconds
                        ),
                        "Color fade time in seconds"
                );

        dmxModeButton =
                addRenderableWidget(
                        Button.builder(
                                Component.literal(
                                        "DMX Mode"
                                ),
                                button -> setControlMode(
                                        "dmx"
                                )
                        )
                        .bounds(
                                centerX - 110,
                                modeY,
                                105,
                                20
                        )
                        .build()
                );

        manualModeButton =
                addRenderableWidget(
                        Button.builder(
                                Component.literal(
                                        "Manual Mode"
                                ),
                                button -> setControlMode(
                                        "manual"
                                )
                        )
                        .bounds(
                                centerX + 5,
                                modeY,
                                105,
                                20
                        )
                        .build()
                );

        int buttonY =
                modeY + 30;

        addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Save"
                        ),
                        button -> saveConfiguration()
                )
                .bounds(
                        centerX - 110,
                        buttonY,
                        105,
                        20
                )
                .build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Done"
                        ),
                        button -> {
                            if (saveConfiguration()) {
                                onClose();
                            }
                        }
                )
                .bounds(
                        centerX + 5,
                        buttonY,
                        105,
                        20
                )
                .build()
        );

        updateModeButtons();

        setInitialFocus(
                nameField
        );

        screenReady =
                true;
    }

    private void loadValues(
            DmxPixelBlockEntity blockEntity
    ) {
        if (blockEntity == null) {
            return;
        }

        controlMode =
                blockEntity.getControlMode()
                        .getSerializedName();

        manualRed =
                blockEntity.getManualRed();

        manualGreen =
                blockEntity.getManualGreen();

        manualBlue =
                blockEntity.getManualBlue();

        manualDimmer =
                blockEntity.getManualDimmer();

        manualStrobe =
                blockEntity.getManualStrobe();

        selectedSkin =
                blockEntity.getSkin();

        colorInterpolationEnabled =
                blockEntity.isColorInterpolationEnabled();

        colorInterpolationTimeSeconds =
                blockEntity.getColorInterpolationTimeSeconds();
    }

    private void selectNextSkin() {
        selectedSkin++;

        if (selectedSkin > DmxPixelBlockEntity.MAX_SKIN) {
            selectedSkin =
                    DmxPixelBlockEntity.MIN_SKIN;
        }

        skinButton.setMessage(
                skinLabel()
        );
    }

    private Component skinLabel() {
        return Component.literal(
                "Skin " + selectedSkin
        );
    }

    private EditBox createTextField(
            int x,
            int y,
            int width,
            int maxLength,
            String value,
            String narration
    ) {
        EditBox field =
                new EditBox(
                        font,
                        x,
                        y,
                        width,
                        FIELD_HEIGHT,
                        Component.literal(
                                narration
                        )
                );

        field.setMaxLength(
                maxLength
        );

        field.setValue(
                value == null
                        ? ""
                        : value
        );

        return addRenderableWidget(
                field
        );
    }

    private EditBox createChannelField(
            int x,
            int y,
            int channel,
            String narration
    ) {
        return createTextField(
                x,
                y,
                CHANNEL_FIELD_WIDTH,
                3,
                FixtureParameterMap.isAssigned(
                        channel
                )
                        ? Integer.toString(
                                channel
                        )
                        : "",
                narration
        );
    }

    private DmxValueSlider createSlider(
            int x,
            int y,
            String label,
            int value,
            java.util.function.IntConsumer consumer
    ) {
        DmxValueSlider slider =
                new DmxValueSlider(
                        x,
                        y,
                        SLIDER_WIDTH,
                        FIELD_HEIGHT,
                        label,
                        value,
                        updatedValue -> {
                            consumer.accept(
                                    updatedValue
                            );

                            if (screenReady
                                    && isManualMode()) {

                                sendManualOutput();
                            }
                        }
                );

        return addRenderableWidget(
                slider
        );
    }

    private void setControlMode(
            String mode
    ) {
        if (!ClientPlayNetworking.canSend(
                SetFixtureModePayload.TYPE
        )) {
            setError(
                    "Mode packet is unavailable."
            );

            return;
        }

        if ("manual".equals(
                mode
        ) && !sendManualOutput()) {
            return;
        }

        ClientPlayNetworking.send(
                new SetFixtureModePayload(
                        blockPosition,
                        mode
                )
        );

        controlMode =
                mode;

        updateModeButtons();

        statusMessage =
                Component.literal(
                        isManualMode()
                                ? "Manual mode active."
                                : "DMX mode active."
                );

        statusColor =
                0xFF55FF55;
    }

    private void updateModeButtons() {
        if (dmxModeButton != null) {
            dmxModeButton.active =
                    isManualMode();
        }

        if (manualModeButton != null) {
            manualModeButton.active =
                    !isManualMode();
        }
    }

    private boolean isManualMode() {
        return "manual".equalsIgnoreCase(
                controlMode
        );
    }

    private boolean saveConfiguration() {
        Integer universe =
                parseInteger(
                        universeField.getValue()
                );

        if (universe == null
                || universe < DmxFixtureBlockEntity.MIN_UNIVERSE
                || universe > DmxFixtureBlockEntity.MAX_UNIVERSE) {

            setError(
                    "Universe is invalid."
            );

            return false;
        }

        Integer redChannel =
                parseChannel(
                        redChannelField
                );

        Integer greenChannel =
                parseChannel(
                        greenChannelField
                );

        Integer blueChannel =
                parseChannel(
                        blueChannelField
                );

        Integer dimmerChannel =
                parseChannel(
                        dimmerChannelField
                );

        Integer strobeChannel =
                parseChannel(
                        strobeChannelField
                );

        Integer skinDmxChannel =
                parseChannel(
                        skinDmxChannelField
                );

        if (redChannel == null
                || greenChannel == null
                || blueChannel == null
                || dimmerChannel == null
                || strobeChannel == null
                || skinDmxChannel == null) {

            setError(
                    "Channels must be blank or 1-512."
            );

            return false;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixturePayload.TYPE
        )
                || !ClientPlayNetworking.canSend(
                        UpdateFixtureGroupPayload.TYPE
                )
                || !ClientPlayNetworking.canSend(
                        UpdateFixtureParameterMapPayload.TYPE
                )
                || !ClientPlayNetworking.canSend(
                        UpdatePixelBlockSkinPayload.TYPE
                )
                || !ClientPlayNetworking.canSend(
                        UpdateFixtureColorInterpolationPayload.TYPE
                )) {

            setError(
                    "DMX Block configuration packets are unavailable."
            );

            return false;
        }

        ClientPlayNetworking.send(
                new UpdateFixturePayload(
                        blockPosition,
                        nameField.getValue(),
                        universe
                )
        );

        ClientPlayNetworking.send(
                new UpdateFixtureGroupPayload(
                        blockPosition,
                        groupField.getValue()
                )
        );

        ClientPlayNetworking.send(
                new UpdateFixtureParameterMapPayload(
                        blockPosition,
                        redChannel,
                        greenChannel,
                        blueChannel,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        dimmerChannel,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        strobeChannel
                )
        );

        ClientPlayNetworking.send(
                new UpdatePixelBlockSkinPayload(
                        blockPosition,
                        selectedSkin,
                        skinDmxChannel
                )
        );

        if (!sendColorInterpolation()) {
            return false;
        }

        if (!sendManualOutput()) {
            return false;
        }

        statusMessage =
                Component.literal(
                        "DMX Block saved."
                );

        statusColor =
                0xFF55FF55;

        return true;
    }

    private void toggleColorInterpolation() {
        boolean newEnabled =
                !colorInterpolationEnabled;

        if (newEnabled
                && !validateColorInterpolationTime()) {

            setError(
                    "Color fade time must be between 0.05 and 60 seconds."
            );

            return;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateFixtureColorInterpolationPayload.TYPE
        )) {
            setError(
                    "Color fade packet is unavailable."
            );

            return;
        }

        colorInterpolationEnabled =
                newEnabled;

        updateColorInterpolationButtonMessage();

        ClientPlayNetworking.send(
                new UpdateFixtureColorInterpolationPayload(
                        blockPosition,
                        colorInterpolationEnabled,
                        colorInterpolationTimeSeconds
                )
        );

        statusMessage =
                Component.literal(
                        colorInterpolationEnabled
                                ? "Color fade enabled."
                                : "Color fade disabled."
                );

        statusColor =
                0xFF55FF55;
    }

    private boolean sendColorInterpolation() {
        if (!validateColorInterpolationTime()) {
            setError(
                    "Color fade time must be between 0.05 and 60 seconds."
            );

            return false;
        }

        ClientPlayNetworking.send(
                new UpdateFixtureColorInterpolationPayload(
                        blockPosition,
                        colorInterpolationEnabled,
                        colorInterpolationTimeSeconds
                )
        );

        return true;
    }

    private boolean validateColorInterpolationTime() {
        Double parsed =
                parseDouble(
                        colorInterpolationTimeField.getValue()
                );

        boolean valid =
                parsed != null
                        && parsed
                        >= DmxFixtureBlockEntity
                        .MIN_COLOR_INTERPOLATION_TIME_SECONDS
                        && parsed
                        <= DmxFixtureBlockEntity
                        .MAX_COLOR_INTERPOLATION_TIME_SECONDS;

        colorInterpolationTimeField.setTextColor(
                valid
                        ? 0xFFFFFFFF
                        : 0xFFFF5555
        );

        if (valid) {
            colorInterpolationTimeSeconds =
                    parsed.floatValue();
        }

        return valid;
    }

    private Component getColorInterpolationButtonMessage() {
        return Component.literal(
                colorInterpolationEnabled
                        ? "Fade: On"
                        : "Fade: Off"
        );
    }

    private void updateColorInterpolationButtonMessage() {
        if (colorInterpolationButton != null) {
            colorInterpolationButton.setMessage(
                    getColorInterpolationButtonMessage()
            );
        }
    }

    private boolean sendManualOutput() {
        if (!ClientPlayNetworking.canSend(
                ManualFixtureOutputPayload.TYPE
        )) {
            setError(
                    "Manual-output packet is unavailable."
            );

            return false;
        }

        ClientPlayNetworking.send(
                new ManualFixtureOutputPayload(
                        blockPosition,
                        manualRed,
                        manualGreen,
                        manualBlue,
                        0,
                        0,
                        manualDimmer,
                        128,
                        128,
                        0,
                        0,
                        manualStrobe
                )
        );

        return true;
    }

    private Integer parseChannel(
            EditBox field
    ) {
        String text =
                field.getValue()
                        .trim();

        if (text.isEmpty()) {
            field.setTextColor(
                    0xFFFFFFFF
            );

            return FixtureParameterMap.UNASSIGNED;
        }

        Integer value =
                parseInteger(
                        text
                );

        boolean valid =
                value != null
                        && value >= 1
                        && value <= 512;

        field.setTextColor(
                valid
                        ? 0xFFFFFFFF
                        : 0xFFFF5555
        );

        return valid
                ? value
                : null;
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
        try {
            return Double.parseDouble(
                    value.trim()
            );
        } catch (NumberFormatException exception) {
            return null;
        }
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

    private void setError(
            String message
    ) {
        statusMessage =
                Component.literal(
                        message
                );

        statusColor =
                0xFFFF5555;
    }

    private DmxPixelBlockEntity getBlockEntity() {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return null;
        }

        BlockEntity blockEntity =
                minecraft.level.getBlockEntity(
                        blockPosition
                );

        return blockEntity
                instanceof DmxPixelBlockEntity pixelBlock
                ? pixelBlock
                : null;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int centerX =
                width / 2;

        int top =
                38;

        graphics.centeredText(
                font,
                title,
                centerX,
                16,
                0xFFFFFFFF
        );

        graphics.text(
                font,
                "Name",
                centerX - 230,
                top + 6,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                "Universe",
                centerX - 230,
                top + ROW_SPACING + 6,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                "Group",
                centerX - 230,
                top + ROW_SPACING * 2 + 6,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                "Skin / DMX channel",
                centerX - 230,
                top + ROW_SPACING * 3 + 6,
                0xFFFFFFFF,
                false
        );

        int controlsTop =
                top + ROW_SPACING * 4 + 18;

        graphics.text(
                font,
                "DMX channels",
                centerX - 225,
                controlsTop - 17,
                0xFFAAAAAA,
                false
        );

        graphics.text(
                font,
                "Manual output",
                centerX + 5,
                controlsTop - 17,
                0xFFAAAAAA,
                false
        );

        String[] labels = {
                "Red",
                "Green",
                "Blue",
                "Dimmer",
                "Strobe",
                "Color Fade"
        };

        for (int index = 0; index < labels.length; index++) {
            graphics.text(
                    font,
                    labels[index],
                    centerX - 150,
                    controlsTop
                            + ROW_SPACING * index
                            + (
                            index == 5
                                    ? 8
                                    : 0
                    )
                            + 6,
                    0xFFFFFFFF,
                    false
            );
        }

        if (!statusMessage.getString().isEmpty()) {
            graphics.centeredText(
                    font,
                    statusMessage,
                    centerX,
                    controlsTop + ROW_SPACING * 6 + 70,
                    statusColor
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
