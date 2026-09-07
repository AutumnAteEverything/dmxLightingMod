package dmx.lighting;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * DMX controllable Warden.
 *
 * The entity keeps DMX patch data directly on the mob, registers while
 * loaded, and presents itself to the existing Lighting Console as a
 * fixture-browser row.
 */
public class DmxWardenEntity extends Warden {

    private static final EntityDataAccessor<Integer> DATA_OUTPUT_RGB =
            SynchedEntityData.defineId(
                    DmxWardenEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer> DATA_DIMMER =
            SynchedEntityData.defineId(
                    DmxWardenEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer>
    DATA_COLOR_INTERPOLATION_CENTISECONDS =
            SynchedEntityData.defineId(
                    DmxWardenEntity.class,
                    EntityDataSerializers.INT
            );

    private static final int DEFAULT_UNIVERSE =
            FixturePatch.DEFAULT_UNIVERSE;

    private static final int DEFAULT_RED_CHANNEL =
            1;

    private static final int DEFAULT_GREEN_CHANNEL =
            2;

    private static final int DEFAULT_BLUE_CHANNEL =
            3;

    private static final int DEFAULT_DIMMER_CHANNEL =
            4;

    private String fixtureName =
            "";

    private FixtureGroupName group =
            FixtureGroupName.UNGROUPED;

    private int universe =
            DEFAULT_UNIVERSE;

    private final FixtureParameterMap parameterMap =
            new FixtureParameterMap(
                    DEFAULT_RED_CHANNEL,
                    DEFAULT_GREEN_CHANNEL,
                    DEFAULT_BLUE_CHANNEL,
                    FixtureParameterMap.UNASSIGNED,
                    FixtureParameterMap.UNASSIGNED,
                    DEFAULT_DIMMER_CHANNEL,
                    FixtureParameterMap.UNASSIGNED,
                    FixtureParameterMap.UNASSIGNED,
                    FixtureParameterMap.UNASSIGNED,
                    FixtureParameterMap.UNASSIGNED,
                    FixtureParameterMap.UNASSIGNED
            );

    private FixtureControlMode controlMode =
            FixtureControlMode.DMX;

    private final FixtureOutput dmxOutput =
            new FixtureOutput();

    private final FixtureOutput manualOutput =
            FixtureOutput.FULL_WHITE.copy();

    private boolean colorInterpolationEnabled =
            false;

    private float colorInterpolationTimeSeconds =
            ColorInterpolationSettings.DEFAULT_TIME_SECONDS;

    private final ColorInterpolationState colorInterpolation =
            new ColorInterpolationState();

    public DmxWardenEntity(
            EntityType<? extends Warden> entityType,
            Level level
    ) {
        super(
                entityType,
                level
        );

        setGlowingTag(
                false
        );

        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(
                builder
        );

        builder.define(
                DATA_OUTPUT_RGB,
                0xFFFFFF
        );

        builder.define(
                DATA_DIMMER,
                0
        );

        builder.define(
                DATA_COLOR_INTERPOLATION_CENTISECONDS,
                0
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        setGlowingTag(
                false
        );

        DmxMobFixtureRegistry.register(
                this
        );

        refreshFromDmx();
    }

    @Override
    public void remove(
            Entity.RemovalReason reason
    ) {
        if (!level().isClientSide()) {
            DmxMobFixtureRegistry.unregister(
                    this
            );
        }

        super.remove(
                reason
        );
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack =
                player.getItemInHand(
                        hand
                );

        if (!stack.is(
                ModItems.LIGHTING_CONSOLE
        )) {
            return super.mobInteract(
                    player,
                    hand
            );
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        DmxMobFixtureRegistry.register(
                this
        );

        if (!ServerPlayNetworking.canSend(
                serverPlayer,
                FixtureBrowserDataPayload.TYPE
        )) {
            return InteractionResult.PASS;
        }

        List<FixtureBrowserEntry> entries =
                LightingConsole.getAllFixtures(
                        level().dimension(),
                        LightingConsole.FixtureSortMode.PATCH
                );

        ServerPlayNetworking.send(
                serverPlayer,
                new FixtureBrowserDataPayload(
                        entries
                )
        );

        serverPlayer.sendSystemMessage(
                Component.literal(
                        getConsoleName()
                                + " is available in the DMX console."
                )
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(
            ValueOutput output
    ) {
        super.addAdditionalSaveData(
                output
        );

        output.putString(
                "dmx_fixture_name",
                fixtureName
        );

        output.putString(
                "dmx_group_name",
                group.displayName()
        );

        output.putString(
                "dmx_group_key",
                group.key()
        );

        output.putInt(
                "dmx_universe",
                universe
        );

        output.putString(
                "dmx_control_mode",
                FixtureControlMode.DMX.getSerializedName()
        );

        output.putInt(
                "dmx_parameter_red",
                parameterMap.getRedChannel()
        );

        output.putInt(
                "dmx_parameter_green",
                parameterMap.getGreenChannel()
        );

        output.putInt(
                "dmx_parameter_blue",
                parameterMap.getBlueChannel()
        );

        output.putInt(
                "dmx_parameter_dimmer",
                parameterMap.getDimmerChannel()
        );

        output.putInt(
                "dmx_manual_red",
                manualOutput.getRed()
        );

        output.putInt(
                "dmx_manual_green",
                manualOutput.getGreen()
        );

        output.putInt(
                "dmx_manual_blue",
                manualOutput.getBlue()
        );

        output.putInt(
                "dmx_manual_dimmer",
                manualOutput.getDimmer()
        );

        output.putInt(
                "dmx_color_interpolation_enabled",
                colorInterpolationEnabled
                        ? 1
                        : 0
        );

        output.putFloat(
                "dmx_color_interpolation_time_seconds",
                colorInterpolationTimeSeconds
        );
    }

    @Override
    protected void readAdditionalSaveData(
            ValueInput input
    ) {
        super.readAdditionalSaveData(
                input
        );

        fixtureName =
                cleanName(
                        input.getStringOr(
                                "dmx_fixture_name",
                                ""
                        )
                );

        group =
                FixtureGroupName.fromSavedData(
                        input.getStringOr(
                                "dmx_group_key",
                                ""
                        ),
                        input.getStringOr(
                                "dmx_group_name",
                                ""
                        )
                );

        universe =
                clampUniverse(
                        input.getIntOr(
                                "dmx_universe",
                                DEFAULT_UNIVERSE
                        )
                );

        controlMode =
                FixtureControlMode.DMX;

        parameterMap.set(
                input.getIntOr(
                        "dmx_parameter_red",
                        DEFAULT_RED_CHANNEL
                ),
                input.getIntOr(
                        "dmx_parameter_green",
                        DEFAULT_GREEN_CHANNEL
                ),
                input.getIntOr(
                        "dmx_parameter_blue",
                        DEFAULT_BLUE_CHANNEL
                ),
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                input.getIntOr(
                        "dmx_parameter_dimmer",
                        DEFAULT_DIMMER_CHANNEL
                ),
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED
        );

        manualOutput.setRed(
                input.getIntOr(
                        "dmx_manual_red",
                        255
                )
        );

        manualOutput.setGreen(
                input.getIntOr(
                        "dmx_manual_green",
                        255
                )
        );

        manualOutput.setBlue(
                input.getIntOr(
                        "dmx_manual_blue",
                        255
                )
        );

        manualOutput.setDimmer(
                input.getIntOr(
                        "dmx_manual_dimmer",
                        255
                )
        );

        colorInterpolationEnabled =
                input.getIntOr(
                        "dmx_color_interpolation_enabled",
                        0
                ) != 0;

        colorInterpolationTimeSeconds =
                ColorInterpolationSettings.clampTimeSeconds(
                        input.getFloatOr(
                                "dmx_color_interpolation_time_seconds",
                                ColorInterpolationSettings.DEFAULT_TIME_SECONDS
                        )
                );

        syncColorInterpolationSetting();
        refreshSyncedOutput();
    }

    public String getFixtureName() {
        return fixtureName;
    }

    public void setFixtureName(
            String fixtureName
    ) {
        this.fixtureName =
                cleanName(
                        fixtureName
                );
    }

    public String getConsoleName() {
        if (fixtureName != null
                && !fixtureName.isBlank()) {

            return fixtureName;
        }

        return "dmxWarden "
                + getUUID()
                        .toString()
                        .substring(
                                0,
                                8
                        );
    }

    public FixtureGroupName getFixtureGroup() {
        return group;
    }

    public void setFixtureGroup(
            FixtureGroupName group
    ) {
        this.group =
                group == null
                        ? FixtureGroupName.UNGROUPED
                        : group;
    }

    public String getGroupKey() {
        return group.key();
    }

    public String getGroupDisplayName() {
        return group.getGuiLabel();
    }

    public boolean isUngrouped() {
        return group.isUngrouped();
    }

    public int getUniverse() {
        return universe;
    }

    public void setUniverse(
            int universe
    ) {
        this.universe =
                clampUniverse(
                        universe
                );

        refreshFromDmx();
    }

    public FixtureParameterMap getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(
            int redChannel,
            int greenChannel,
            int blueChannel,
            int dimmerChannel
    ) {
        parameterMap.set(
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
                FixtureParameterMap.UNASSIGNED
        );

        refreshFromDmx();
    }

    public FixtureControlMode getControlMode() {
        return FixtureControlMode.DMX;
    }

    public void setControlMode(
            FixtureControlMode controlMode
    ) {
        this.controlMode =
                FixtureControlMode.DMX;

        refreshFromDmx();
    }

    public FixtureOutput getManualOutput() {
        return manualOutput;
    }

    public FixtureOutput getCurrentOutput() {
        return dmxOutput;
    }

    public int getOutputPackedRgb() {
        return getCurrentOutput()
                .getPackedRgb();
    }

    public int getSyncedOutputRgb() {
        return getEntityData().get(
                DATA_OUTPUT_RGB
        );
    }

    public int getSyncedDimmer() {
        return getEntityData().get(
                DATA_DIMMER
        );
    }

    public boolean isColorInterpolationEnabled() {
        if (level().isClientSide()) {
            return getEntityData()
                    .get(
                            DATA_COLOR_INTERPOLATION_CENTISECONDS
                    ) > 0;
        }

        return colorInterpolationEnabled;
    }

    public float getColorInterpolationTimeSeconds() {
        if (level().isClientSide()) {
            int centiseconds =
                    getEntityData().get(
                            DATA_COLOR_INTERPOLATION_CENTISECONDS
                    );

            if (centiseconds <= 0) {
                return ColorInterpolationSettings
                        .DEFAULT_TIME_SECONDS;
            }

            return ColorInterpolationSettings
                    .fromCentiseconds(
                            centiseconds
                    );
        }

        return colorInterpolationTimeSeconds;
    }

    public void setColorInterpolation(
            boolean enabled,
            float timeSeconds
    ) {
        colorInterpolationEnabled =
                enabled;

        colorInterpolationTimeSeconds =
                ColorInterpolationSettings.clampTimeSeconds(
                        timeSeconds
                );

        syncColorInterpolationSetting();
    }

    public void updateColorInterpolation(
            float tickProgress
    ) {
        colorInterpolation.update(
                getSyncedOutputRgb(),
                isColorInterpolationEnabled(),
                getColorInterpolationTimeSeconds(),
                getInterpolationGameTime(
                        tickProgress
                )
        );
    }

    public int getSmoothedSyncedOutputRgb() {
        return isColorInterpolationEnabled()
                ? colorInterpolation.getDisplayedPackedRgb(
                        getSyncedOutputRgb()
                )
                : getSyncedOutputRgb();
    }

    public int getMinecraftLightLevel() {
        return 0;
    }

    public void applyConsoleDmxOutput(
            ConsoleFixtureOutputPayload payload
    ) {
        if (payload == null) {
            return;
        }

        if (payload.appliesRed()) {
            writeAssignedDmxChannel(
                    parameterMap.getRedChannel(),
                    payload.red()
            );
        }

        if (payload.appliesGreen()) {
            writeAssignedDmxChannel(
                    parameterMap.getGreenChannel(),
                    payload.green()
            );
        }

        if (payload.appliesBlue()) {
            writeAssignedDmxChannel(
                    parameterMap.getBlueChannel(),
                    payload.blue()
            );
        }

        if (payload.appliesDimmer()) {
            writeAssignedDmxChannel(
                    parameterMap.getDimmerChannel(),
                    payload.dimmer()
            );
        }

        refreshFromDmx();
    }

    public void refreshFromDmx() {
        FixtureOutput underlyingOutput =
                new FixtureOutput(
                        readChannel(
                                parameterMap.getRedChannel()
                        ),
                        readChannel(
                                parameterMap.getGreenChannel()
                        ),
                        readChannel(
                                parameterMap.getBlueChannel()
                        ),
                        0,
                        0,
                        readChannel(
                                parameterMap.getDimmerChannel()
                        ),
                        128,
                        128,
                        0,
                        0,
                        0
                );

        FixtureOutput showOutput =
                AutomaticDmxShowManager.createOutput(
                        level(),
                        blockPosition(),
                        underlyingOutput,
                        false
                );

        dmxOutput.set(
                showOutput == null
                        ? underlyingOutput
                        : showOutput
        );

        refreshSyncedOutput();
    }

    private void writeAssignedDmxChannel(
            int channel,
            int value
    ) {
        if (!FixtureParameterMap.isAssigned(
                channel
        )) {
            return;
        }

        DmxUniverseManager.setChannel(
                universe,
                channel,
                clampDmx(
                        value
                )
        );
    }

    private void refreshSyncedOutput() {
        getEntityData().set(
                DATA_OUTPUT_RGB,
                getOutputPackedRgb()
        );

        getEntityData().set(
                DATA_DIMMER,
                getCurrentOutput().getDimmer()
        );
    }

    private void syncColorInterpolationSetting() {
        getEntityData().set(
                DATA_COLOR_INTERPOLATION_CENTISECONDS,
                ColorInterpolationSettings.toCentiseconds(
                        colorInterpolationEnabled,
                        colorInterpolationTimeSeconds
                )
        );
    }

    private double getInterpolationGameTime(
            float tickProgress
    ) {
        return level().getGameTime()
                + Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                tickProgress
                        )
                );
    }

    private int readChannel(
            int channel
    ) {
        if (!FixtureParameterMap.isAssigned(
                channel
        )) {
            return 0;
        }

        return DmxUniverseManager.getChannel(
                universe,
                channel
        );
    }

    private static String cleanName(
            String value
    ) {
        if (value == null) {
            return "";
        }

        String cleaned =
                value.trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        if (cleaned.length()
                > FixtureIdentity.MAX_NAME_LENGTH) {

            return cleaned.substring(
                    0,
                    FixtureIdentity.MAX_NAME_LENGTH
            );
        }

        return cleaned;
    }

    private static int clampUniverse(
            int value
    ) {
        return Math.max(
                FixturePatch.MIN_UNIVERSE,
                Math.min(
                        FixturePatch.MAX_UNIVERSE,
                        value
                )
        );
    }

    private static int clampDmx(
            int value
    ) {
        return Math.max(
                FixtureOutput.MIN_VALUE,
                Math.min(
                        FixtureOutput.MAX_VALUE,
                        value
                )
        );
    }

}
