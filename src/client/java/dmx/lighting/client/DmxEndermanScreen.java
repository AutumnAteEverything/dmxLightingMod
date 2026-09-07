package dmx.lighting.client;

import dmx.lighting.DmxFixtureBlockEntity;
import dmx.lighting.FixtureBrowserEntry;
import dmx.lighting.FixtureGroupName;
import dmx.lighting.FixtureIdentity;
import dmx.lighting.FixtureParameterMap;
import dmx.lighting.UpdateDmxEndermanPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Compact editor for the first DMX mob fixture.
 *
 * DMX Endermen currently expose RGB and Dimmer channels only.
 */
public final class DmxEndermanScreen
        extends Screen {

    private static final int FIELD_HEIGHT =
            20;

    private static final int ROW_SPACING =
            26;

    private static final int CHANNEL_FIELD_WIDTH =
            64;

    private final FixtureBrowserEntry entry;

    private EditBox nameField;
    private EditBox universeField;
    private EditBox groupField;

    private EditBox redChannelField;
    private EditBox greenChannelField;
    private EditBox blueChannelField;
    private EditBox dimmerChannelField;

    private Button colorInterpolationButton;
    private EditBox colorInterpolationTimeField;

    private boolean colorInterpolationEnabled;

    private float colorInterpolationTimeSeconds =
            DmxFixtureBlockEntity
                    .DEFAULT_COLOR_INTERPOLATION_TIME_SECONDS;

    private Component statusMessage =
            Component.empty();

    private int statusColor =
            0xFFFFFFFF;

    public DmxEndermanScreen(
            FixtureBrowserEntry entry
    ) {
        super(
                Component.literal(
                        "DMX Enderman"
                )
        );

        this.entry =
                entry;
    }

    @Override
    protected void init() {
        colorInterpolationEnabled =
                entry != null
                        && entry.colorInterpolationEnabled();

        colorInterpolationTimeSeconds =
                entry == null
                        ? DmxFixtureBlockEntity
                        .DEFAULT_COLOR_INTERPOLATION_TIME_SECONDS
                        : entry.colorInterpolationTimeSeconds();

        int centerX =
                width / 2;

        int fieldX =
                centerX - 60;

        int top =
                44;

        nameField =
                createTextField(
                        fieldX,
                        top,
                        220,
                        FixtureIdentity.MAX_NAME_LENGTH,
                        entry == null
                                ? ""
                                : entry.fixtureName(),
                        "DMX Enderman name"
                );

        universeField =
                createTextField(
                        fieldX,
                        top + ROW_SPACING,
                        80,
                        3,
                        entry == null
                                ? "1"
                                : Integer.toString(
                                        entry.universe()
                                ),
                        "DMX universe"
                );

        groupField =
                createTextField(
                        fieldX,
                        top + ROW_SPACING * 2,
                        220,
                        FixtureGroupName.MAX_LENGTH,
                        entry == null
                                || entry.isUngrouped()
                                ? ""
                                : entry.groupName(),
                        "DMX group"
                );

        int channelTop =
                top + ROW_SPACING * 3 + 28;

        redChannelField =
                createChannelField(
                        fieldX,
                        channelTop,
                        entry == null
                                ? FixtureParameterMap.UNASSIGNED
                                : entry.redChannel(),
                        "Red DMX channel"
                );

        greenChannelField =
                createChannelField(
                        fieldX,
                        channelTop + ROW_SPACING,
                        entry == null
                                ? FixtureParameterMap.UNASSIGNED
                                : entry.greenChannel(),
                        "Green DMX channel"
                );

        blueChannelField =
                createChannelField(
                        fieldX,
                        channelTop + ROW_SPACING * 2,
                        entry == null
                                ? FixtureParameterMap.UNASSIGNED
                                : entry.blueChannel(),
                        "Blue DMX channel"
                );

        dimmerChannelField =
                createChannelField(
                        fieldX,
                        channelTop + ROW_SPACING * 3,
                        entry == null
                                ? FixtureParameterMap.UNASSIGNED
                                : entry.dimmerChannel(),
                        "Dimmer DMX channel"
                );

        int fadeY =
                channelTop + ROW_SPACING * 4 + 8;

        colorInterpolationButton =
                addRenderableWidget(
                        Button.builder(
                                getColorInterpolationButtonMessage(),
                                button -> toggleColorInterpolation()
                        )
                        .bounds(
                                fieldX,
                                fadeY,
                                105,
                                FIELD_HEIGHT
                        )
                        .build()
                );

        colorInterpolationTimeField =
                createTextField(
                        fieldX + 115,
                        fadeY,
                        72,
                        6,
                        formatInterpolationSeconds(
                                colorInterpolationTimeSeconds
                        ),
                        "Color fade time in seconds"
                );

        int buttonY =
                fadeY + ROW_SPACING + 10;

        addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Save"
                        ),
                        button -> saveConfiguration()
                )
                .bounds(
                        fieldX,
                        buttonY,
                        105,
                        FIELD_HEIGHT
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
                        fieldX + 115,
                        buttonY,
                        105,
                        FIELD_HEIGHT
                )
                .build()
        );

        setInitialFocus(
                nameField
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

    private boolean saveConfiguration() {
        if (entry == null
                || !entry.isMobTarget()) {

            setError(
                    "Select a loaded DMX Enderman first."
            );

            return false;
        }

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

        if (redChannel == null
                || greenChannel == null
                || blueChannel == null
                || dimmerChannel == null) {

            setError(
                    "Channels must be blank or 1-512."
            );

            return false;
        }

        if (!validateColorInterpolationTime()) {
            setError(
                    "Color fade time must be between 0.05 and 60 seconds."
            );

            return false;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateDmxEndermanPayload.TYPE
        )) {
            setError(
                    "DMX Enderman packet is unavailable."
            );

            return false;
        }

        ClientPlayNetworking.send(
                new UpdateDmxEndermanPayload(
                        entry.targetEntityId(),
                        nameField.getValue(),
                        groupField.getValue(),
                        universe,
                        redChannel,
                        greenChannel,
                        blueChannel,
                        dimmerChannel,
                        colorInterpolationEnabled,
                        colorInterpolationTimeSeconds
                )
        );

        statusMessage =
                Component.literal(
                        "DMX Enderman saved."
                );

        statusColor =
                0xFF55FF55;

        return true;
    }

    private void toggleColorInterpolation() {
        boolean newEnabled =
                !colorInterpolationEnabled;

        boolean previousEnabled =
                colorInterpolationEnabled;

        float previousTimeSeconds =
                colorInterpolationTimeSeconds;

        if (newEnabled
                && !validateColorInterpolationTime()) {

            setError(
                    "Color fade time must be between 0.05 and 60 seconds."
            );

            return;
        }

        colorInterpolationEnabled =
                newEnabled;

        updateColorInterpolationButtonMessage();

        if (!saveConfiguration()) {
            colorInterpolationEnabled =
                    previousEnabled;

            colorInterpolationTimeSeconds =
                    previousTimeSeconds;

            updateColorInterpolationButtonMessage();

            return;
        }

        statusMessage =
                Component.literal(
                        colorInterpolationEnabled
                                ? "Color fade enabled."
                                : "Color fade disabled."
                );

        statusColor =
                0xFF55FF55;
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
                        && value >= FixtureParameterMap.MIN_CHANNEL
                        && value <= FixtureParameterMap.MAX_CHANNEL;

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

        int labelX =
                centerX - 180;

        int top =
                44;

        graphics.centeredText(
                font,
                title,
                centerX,
                18,
                0xFFFFFFFF
        );

        graphics.text(
                font,
                "Name",
                labelX,
                top + 6,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                "Universe",
                labelX,
                top + ROW_SPACING + 6,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                "Group",
                labelX,
                top + ROW_SPACING * 2 + 6,
                0xFFFFFFFF,
                false
        );

        int channelTop =
                top + ROW_SPACING * 3 + 28;

        graphics.text(
                font,
                "DMX channels",
                labelX,
                channelTop - 17,
                0xFFAAAAAA,
                false
        );

        String[] labels = {
                "Red",
                "Green",
                "Blue",
                "Dimmer",
                "Color Fade"
        };

        for (int index = 0; index < labels.length; index++) {
            graphics.text(
                    font,
                    labels[index],
                    labelX,
                    channelTop
                            + ROW_SPACING * index
                            + (
                            index >= 4
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
                    channelTop + ROW_SPACING * 5 + 62,
                    statusColor
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
