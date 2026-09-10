package dmx.lighting.client;

import dmx.lighting.DmxDiscoBallBlockEntity;
import dmx.lighting.DmxDiscoBallEffectMode;
import dmx.lighting.DmxFixtureBlockEntity;
import dmx.lighting.FixtureControlMode;
import dmx.lighting.FixtureGroupName;
import dmx.lighting.FixtureIdentity;
import dmx.lighting.FixtureParameterMap;
import dmx.lighting.ManualFixtureOutputPayload;
import dmx.lighting.SetFixtureModePayload;
import dmx.lighting.UpdateFixtureGroupPayload;
import dmx.lighting.UpdateFixtureMountOrientationPayload;
import dmx.lighting.UpdateFixtureParameterMapPayload;
import dmx.lighting.UpdateFixturePayload;
import dmx.lighting.UpdateDmxDiscoBallEffectPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Focused two-channel editor for a DMX Disco Ball. */
public final class DmxDiscoBallScreen extends Screen {

    private static final int FIELD_HEIGHT = 20;
    private static final int ROW_SPACING = 24;

    private final BlockPos fixturePosition;

    private EditBox nameField;
    private EditBox universeField;
    private EditBox groupField;
    private EditBox dimmerChannelField;
    private EditBox spinChannelField;

    private DmxValueSlider manualDimmerSlider;
    private DmxValueSlider manualSpinSlider;
    private DmxAngleSlider initialAngleSlider;
    private Button basePositionButton;
    private Button dmxModeButton;
    private Button manualModeButton;
    private Button beamsEffectButton;
    private Button dotsEffectButton;

    private boolean baseOnTop;
    private DmxDiscoBallEffectMode effectMode =
            DmxDiscoBallEffectMode.BEAMS;
    private String controlMode = FixtureControlMode.DMX.getSerializedName();
    private Component statusMessage = Component.empty();
    private int statusColor = 0xFFFFFFFF;

    public DmxDiscoBallScreen(BlockPos fixturePosition) {
        super(Component.literal("DMX Disco Ball"));
        this.fixturePosition = fixturePosition.immutable();
    }

    @Override
    protected void init() {
        DmxDiscoBallBlockEntity fixture = getFixture();
        int left = width / 2 - 220;
        int right = width / 2 + 20;
        int fieldX = left + 76;
        int top = 42;

        if (fixture != null) {
            baseOnTop = fixture.isBaseOnTop();
            effectMode = fixture.getEffectMode();
            controlMode = fixture.getControlMode().getSerializedName();
        }

        nameField = createTextField(
                fieldX,
                top,
                144,
                FixtureIdentity.MAX_NAME_LENGTH,
                fixture == null ? "" : fixture.getFixtureName(),
                "DMX Disco Ball name"
        );
        universeField = createTextField(
                fieldX,
                top + ROW_SPACING,
                64,
                3,
                fixture == null ? "1" : Integer.toString(fixture.getUniverse()),
                "DMX universe"
        );
        groupField = createTextField(
                fieldX,
                top + ROW_SPACING * 2,
                144,
                FixtureGroupName.MAX_LENGTH,
                fixture == null || fixture.isUngrouped()
                        ? ""
                        : fixture.getGroupDisplayName(),
                "DMX group"
        );
        dimmerChannelField = createChannelField(
                fieldX,
                top + ROW_SPACING * 3,
                fixture == null
                        ? 1
                        : fixture.getParameterMap().getDimmerChannel(),
                "Dimmer DMX channel"
        );
        spinChannelField = createChannelField(
                fieldX,
                top + ROW_SPACING * 4,
                fixture == null
                        ? 2
                        : fixture.getParameterMap().getPanChannel(),
                "Spin rate DMX channel"
        );

        initialAngleSlider = addRenderableWidget(
                new DmxAngleSlider(
                        left,
                        top + ROW_SPACING * 5 + 6,
                        220,
                        FIELD_HEIGHT,
                        "Initial angle",
                        fixture == null
                                ? 0
                                : Math.round(fixture.getMountPanDegrees()),
                        value -> {
                        }
                )
        );

        basePositionButton = addRenderableWidget(
                Button.builder(
                        getBasePositionMessage(),
                        button -> {
                            baseOnTop = !baseOnTop;
                            button.setMessage(getBasePositionMessage());
                        }
                ).bounds(
                        left,
                        top + ROW_SPACING * 6 + 6,
                        220,
                        FIELD_HEIGHT
                ).build()
        );

        manualDimmerSlider = addRenderableWidget(
                new DmxValueSlider(
                        right,
                        top,
                        220,
                        FIELD_HEIGHT,
                        "Manual dimmer",
                        fixture == null ? 255 : fixture.getManualDimmer(),
                        value -> {
                        }
                )
        );
        manualSpinSlider = addRenderableWidget(
                new DmxValueSlider(
                        right,
                        top + ROW_SPACING,
                        220,
                        FIELD_HEIGHT,
                        "Manual spin (128 = stop)",
                        fixture == null ? 128 : fixture.getManualPan(),
                        value -> {
                        }
                )
        );

        dmxModeButton = addRenderableWidget(
                Button.builder(
                        Component.literal("DMX"),
                        button -> setMode(FixtureControlMode.DMX)
                ).bounds(right, top + ROW_SPACING * 3, 105, FIELD_HEIGHT)
                        .build()
        );
        manualModeButton = addRenderableWidget(
                Button.builder(
                        Component.literal("Manual"),
                        button -> setMode(FixtureControlMode.MANUAL)
                ).bounds(
                        right + 115,
                        top + ROW_SPACING * 3,
                        105,
                        FIELD_HEIGHT
                ).build()
        );

        beamsEffectButton = addRenderableWidget(
                Button.builder(
                        Component.literal("Beams"),
                        button -> setEffectMode(
                                DmxDiscoBallEffectMode.BEAMS
                        )
                ).bounds(
                        right,
                        top + ROW_SPACING * 5,
                        105,
                        FIELD_HEIGHT
                ).build()
        );
        dotsEffectButton = addRenderableWidget(
                Button.builder(
                        Component.literal("Dots"),
                        button -> setEffectMode(
                                DmxDiscoBallEffectMode.DOTS
                        )
                ).bounds(
                        right + 115,
                        top + ROW_SPACING * 5,
                        105,
                        FIELD_HEIGHT
                ).build()
        );

        int bottom = Math.max(top + ROW_SPACING * 6 + 6, height - 48);
        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> saveConfiguration()
                ).bounds(right, bottom, 105, FIELD_HEIGHT).build()
        );
        addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> {
                            if (saveConfiguration()) {
                                onClose();
                            }
                        }
                ).bounds(right + 115, bottom, 105, FIELD_HEIGHT).build()
        );

        updateModeButtons();
        updateEffectModeButtons();
        setInitialFocus(nameField);
    }

    private EditBox createTextField(
            int x,
            int y,
            int width,
            int maxLength,
            String value,
            String narration
    ) {
        EditBox field = new EditBox(
                font,
                x,
                y,
                width,
                FIELD_HEIGHT,
                Component.literal(narration)
        );
        field.setMaxLength(maxLength);
        field.setValue(value == null ? "" : value);
        return addRenderableWidget(field);
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
                64,
                3,
                FixtureParameterMap.isAssigned(channel)
                        ? Integer.toString(channel)
                        : "",
                narration
        );
    }

    private void setMode(FixtureControlMode mode) {
        controlMode = mode.getSerializedName();
        updateModeButtons();
    }

    private void updateModeButtons() {
        boolean dmx = FixtureControlMode.DMX.getSerializedName()
                .equals(controlMode);
        if (dmxModeButton != null) {
            dmxModeButton.active = !dmx;
        }
        if (manualModeButton != null) {
            manualModeButton.active = dmx;
        }
    }

    private Component getBasePositionMessage() {
        return Component.literal(
                baseOnTop ? "Base position: Top" : "Base position: Bottom"
        );
    }

    private void setEffectMode(DmxDiscoBallEffectMode mode) {
        effectMode = mode;
        updateEffectModeButtons();
    }

    private void updateEffectModeButtons() {
        if (beamsEffectButton != null) {
            beamsEffectButton.active =
                    effectMode != DmxDiscoBallEffectMode.BEAMS;
        }
        if (dotsEffectButton != null) {
            dotsEffectButton.active =
                    effectMode != DmxDiscoBallEffectMode.DOTS;
        }
    }

    private boolean saveConfiguration() {
        Integer universe = parseInteger(universeField.getValue());
        Integer dimmerChannel = parseChannel(dimmerChannelField);
        Integer spinChannel = parseChannel(spinChannelField);

        if (universe == null
                || universe < DmxFixtureBlockEntity.MIN_UNIVERSE
                || universe > DmxFixtureBlockEntity.MAX_UNIVERSE) {
            setError("Universe is invalid.");
            return false;
        }

        if (dimmerChannel == null || spinChannel == null) {
            setError("Channels must be blank or 1-512.");
            return false;
        }

        if (!ClientPlayNetworking.canSend(UpdateFixturePayload.TYPE)) {
            setError("DMX Disco Ball controls are unavailable.");
            return false;
        }

        if (!ClientPlayNetworking.canSend(
                UpdateDmxDiscoBallEffectPayload.TYPE
        )) {
            setError("DMX Disco Ball effect controls are unavailable.");
            return false;
        }

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
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        dimmerChannel,
                        spinChannel,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED,
                        FixtureParameterMap.UNASSIGNED
                )
        );
        ClientPlayNetworking.send(
                new UpdateFixtureMountOrientationPayload(
                        fixturePosition,
                        initialAngleSlider.getDegrees(),
                        baseOnTop ? 180.0F : 0.0F
                )
        );
        ClientPlayNetworking.send(
                new UpdateDmxDiscoBallEffectPayload(
                        fixturePosition,
                        effectMode == DmxDiscoBallEffectMode.DOTS
                )
        );
        ClientPlayNetworking.send(
                new ManualFixtureOutputPayload(
                        fixturePosition,
                        0,
                        0,
                        0,
                        0,
                        0,
                        manualDimmerSlider.getDmxValue(),
                        manualSpinSlider.getDmxValue(),
                        128,
                        0,
                        0,
                        0
                )
        );
        ClientPlayNetworking.send(
                new SetFixtureModePayload(fixturePosition, controlMode)
        );

        statusMessage = Component.literal("DMX Disco Ball saved.");
        statusColor = 0xFF55FF55;
        return true;
    }

    private Integer parseChannel(EditBox field) {
        String text = field.getValue().trim();
        if (text.isEmpty()) {
            field.setTextColor(0xFFFFFFFF);
            return FixtureParameterMap.UNASSIGNED;
        }

        Integer value = parseInteger(text);
        boolean valid = value != null
                && value >= FixtureParameterMap.MIN_CHANNEL
                && value <= FixtureParameterMap.MAX_CHANNEL;
        field.setTextColor(valid ? 0xFFFFFFFF : 0xFFFF5555);
        return valid ? value : null;
    }

    private static Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void setError(String message) {
        statusMessage = Component.literal(message);
        statusColor = 0xFFFF5555;
    }

    private DmxDiscoBallBlockEntity getFixture() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }

        BlockEntity blockEntity = minecraft.level.getBlockEntity(
                fixturePosition
        );
        return blockEntity instanceof DmxDiscoBallBlockEntity discoBall
                ? discoBall
                : null;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int centerX = width / 2;
        int left = centerX - 220;
        int top = 42;

        graphics.centeredText(font, title, centerX, 18, 0xFFFFFFFF);

        String[] labels = {
                "Name",
                "Universe",
                "Group",
                "Dimmer",
                "Spin Rate"
        };

        for (int index = 0; index < labels.length; index++) {
            graphics.text(
                    font,
                    labels[index],
                    left,
                    top + ROW_SPACING * index + 6,
                    0xFFFFFFFF,
                    false
            );
        }

        graphics.text(
                font,
                "Control mode",
                centerX + 20,
                top + ROW_SPACING * 2 + 6,
                0xFFAAAAAA,
                false
        );

        graphics.text(
                font,
                "Effect mode",
                centerX + 20,
                top + ROW_SPACING * 4 + 6,
                0xFFAAAAAA,
                false
        );

        if (!statusMessage.getString().isEmpty()) {
            graphics.centeredText(
                    font,
                    statusMessage,
                    centerX,
                    height - 22,
                    statusColor
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
