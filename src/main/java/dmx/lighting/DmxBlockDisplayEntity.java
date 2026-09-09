package dmx.lighting;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Emissive, transformable DMX Block built on Minecraft's display entity.
 *
 * Display transformation data is owned and persisted by {@link Display},
 * while this class adds the DMX patch and visual output state.
 */
public final class DmxBlockDisplayEntity extends Display {

    public static final int MIN_SKIN = DmxPixelBlockEntity.MIN_SKIN;
    public static final int MAX_SKIN = DmxPixelBlockEntity.MAX_SKIN;

    private static final int DEFAULT_UNIVERSE = FixturePatch.DEFAULT_UNIVERSE;
    private static final int DEFAULT_RED_CHANNEL = 1;
    private static final int DEFAULT_GREEN_CHANNEL = 2;
    private static final int DEFAULT_BLUE_CHANNEL = 3;
    private static final int DEFAULT_DIMMER_CHANNEL = 4;
    private static final int DEFAULT_STROBE_CHANNEL = 5;

    private static final EntityDataAccessor<Integer> DATA_OUTPUT_RGB =
            SynchedEntityData.defineId(
                    DmxBlockDisplayEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer> DATA_STROBE =
            SynchedEntityData.defineId(
                    DmxBlockDisplayEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer> DATA_RENDERED_SKIN =
            SynchedEntityData.defineId(
                    DmxBlockDisplayEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer>
    DATA_COLOR_INTERPOLATION_CENTISECONDS =
            SynchedEntityData.defineId(
                    DmxBlockDisplayEntity.class,
                    EntityDataSerializers.INT
            );

    private String fixtureName = "";
    private FixtureGroupName group = FixtureGroupName.UNGROUPED;
    private int universe = DEFAULT_UNIVERSE;

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
                    DEFAULT_STROBE_CHANNEL
            );

    private final FixtureOutput dmxOutput = new FixtureOutput();
    private int skin = MIN_SKIN;
    private int skinDmxChannel = FixtureParameterMap.UNASSIGNED;
    private boolean colorInterpolationEnabled;
    private float colorInterpolationTimeSeconds =
            ColorInterpolationSettings.DEFAULT_TIME_SECONDS;
    private final ColorInterpolationState colorInterpolation =
            new ColorInterpolationState();

    public DmxBlockDisplayEntity(
            EntityType<?> entityType,
            Level level
    ) {
        super(entityType, level);
        setGlowingTag(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OUTPUT_RGB, 0);
        builder.define(DATA_STROBE, 0);
        builder.define(DATA_RENDERED_SKIN, MIN_SKIN);
        builder.define(DATA_COLOR_INTERPOLATION_CENTISECONDS, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);

        if (DATA_OUTPUT_RGB.equals(data)
                || DATA_STROBE.equals(data)
                || DATA_RENDERED_SKIN.equals(data)
                || DATA_COLOR_INTERPOLATION_CENTISECONDS.equals(data)) {
            updateRenderState = true;
        }
    }

    @Override
    protected void updateRenderSubState(
            boolean shouldLerp,
            float interpolationProgress
    ) {
        // The custom renderer reads the synchronized DMX state directly.
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            updateColorInterpolation(0.0F);
            return;
        }

        setGlowingTag(false);
        DmxBlockDisplayRegistry.register(this);
        refreshFromDmx();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide()) {
            DmxBlockDisplayRegistry.unregister(this);
        }

        super.remove(reason);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(
            Player player,
            InteractionHand hand,
            Vec3 location
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.is(ModItems.LIGHTING_CONSOLE)) {
            return super.interact(player, hand, location);
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        DmxBlockDisplayRegistry.register(this);

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
                new FixtureBrowserDataPayload(entries)
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("dmx_fixture_name", fixtureName);
        output.putString("dmx_group_name", group.displayName());
        output.putString("dmx_group_key", group.key());
        output.putInt("dmx_universe", universe);
        output.putString(
                "dmx_control_mode",
                FixtureControlMode.DMX.getSerializedName()
        );
        output.putInt("dmx_parameter_red", parameterMap.getRedChannel());
        output.putInt("dmx_parameter_green", parameterMap.getGreenChannel());
        output.putInt("dmx_parameter_blue", parameterMap.getBlueChannel());
        output.putInt("dmx_parameter_dimmer", parameterMap.getDimmerChannel());
        output.putInt("dmx_parameter_strobe", parameterMap.getStrobeChannel());
        output.putInt("dmx_display_skin", skin);
        output.putInt("dmx_display_skin_channel", skinDmxChannel);
        output.putInt(
                "dmx_color_interpolation_enabled",
                colorInterpolationEnabled ? 1 : 0
        );
        output.putFloat(
                "dmx_color_interpolation_time_seconds",
                colorInterpolationTimeSeconds
        );
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        fixtureName = cleanName(
                input.getStringOr("dmx_fixture_name", "")
        );
        group = FixtureGroupName.fromSavedData(
                input.getStringOr("dmx_group_key", ""),
                input.getStringOr("dmx_group_name", "")
        );
        universe = clampUniverse(
                input.getIntOr("dmx_universe", DEFAULT_UNIVERSE)
        );
        parameterMap.set(
                input.getIntOr("dmx_parameter_red", DEFAULT_RED_CHANNEL),
                input.getIntOr("dmx_parameter_green", DEFAULT_GREEN_CHANNEL),
                input.getIntOr("dmx_parameter_blue", DEFAULT_BLUE_CHANNEL),
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
                input.getIntOr(
                        "dmx_parameter_strobe",
                        DEFAULT_STROBE_CHANNEL
                )
        );
        skin = Math.clamp(
                input.getIntOr("dmx_display_skin", MIN_SKIN),
                MIN_SKIN,
                MAX_SKIN
        );
        skinDmxChannel = normalizeChannel(
                input.getIntOr(
                        "dmx_display_skin_channel",
                        FixtureParameterMap.UNASSIGNED
                )
        );
        colorInterpolationEnabled =
                input.getIntOr("dmx_color_interpolation_enabled", 0) != 0;
        colorInterpolationTimeSeconds =
                ColorInterpolationSettings.clampTimeSeconds(
                        input.getFloatOr(
                                "dmx_color_interpolation_time_seconds",
                                ColorInterpolationSettings.DEFAULT_TIME_SECONDS
                        )
                );
        syncColorInterpolationSetting();
        refreshFromDmx();
    }

    public String getFixtureName() {
        return fixtureName;
    }

    public void setFixtureName(String fixtureName) {
        this.fixtureName = cleanName(fixtureName);
    }

    public String getConsoleName() {
        if (!fixtureName.isBlank()) {
            return fixtureName;
        }

        return "DMX Block Display "
                + getUUID().toString().substring(0, 8);
    }

    public FixtureGroupName getFixtureGroup() {
        return group;
    }

    public void setFixtureGroup(FixtureGroupName group) {
        this.group = group == null
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

    public void setUniverse(int universe) {
        this.universe = clampUniverse(universe);
        refreshFromDmx();
    }

    public FixtureParameterMap getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(
            int redChannel,
            int greenChannel,
            int blueChannel,
            int dimmerChannel,
            int strobeChannel
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
                strobeChannel
        );
        refreshFromDmx();
    }

    public FixtureControlMode getControlMode() {
        return FixtureControlMode.DMX;
    }

    public FixtureOutput getCurrentOutput() {
        return dmxOutput;
    }

    public int getOutputPackedRgb() {
        return dmxOutput.getPackedRgb();
    }

    public int getMinecraftLightLevel() {
        return 0;
    }

    public int getSkin() {
        return skin;
    }

    public int getSkinDmxChannel() {
        return skinDmxChannel;
    }

    public int getRenderedSkin() {
        return getEntityData().get(DATA_RENDERED_SKIN);
    }

    public void setSkinConfiguration(int skin, int skinDmxChannel) {
        this.skin = Math.clamp(skin, MIN_SKIN, MAX_SKIN);
        this.skinDmxChannel = normalizeChannel(skinDmxChannel);
        refreshSkinFromDmx();
    }

    public boolean isColorInterpolationEnabled() {
        if (level().isClientSide()) {
            return getEntityData().get(
                    DATA_COLOR_INTERPOLATION_CENTISECONDS
            ) > 0;
        }

        return colorInterpolationEnabled;
    }

    public float getColorInterpolationTimeSeconds() {
        if (level().isClientSide()) {
            int centiseconds = getEntityData().get(
                    DATA_COLOR_INTERPOLATION_CENTISECONDS
            );
            return centiseconds <= 0
                    ? ColorInterpolationSettings.DEFAULT_TIME_SECONDS
                    : ColorInterpolationSettings.fromCentiseconds(
                            centiseconds
                    );
        }

        return colorInterpolationTimeSeconds;
    }

    public void setColorInterpolation(boolean enabled, float timeSeconds) {
        colorInterpolationEnabled = enabled;
        colorInterpolationTimeSeconds =
                ColorInterpolationSettings.clampTimeSeconds(timeSeconds);
        syncColorInterpolationSetting();
    }

    public void updateColorInterpolation(float tickProgress) {
        colorInterpolation.update(
                getEntityData().get(DATA_OUTPUT_RGB),
                isColorInterpolationEnabled(),
                getColorInterpolationTimeSeconds(),
                level().getGameTime() + Math.clamp(tickProgress, 0.0F, 1.0F)
        );
    }

    public int getVisiblePackedRgb(float tickProgress) {
        updateColorInterpolation(tickProgress);
        return isVisibleStrobePhase()
                ? colorInterpolation.getDisplayedPackedRgb(
                        getEntityData().get(DATA_OUTPUT_RGB)
                )
                : 0;
    }

    public void applyConsoleDmxOutput(ConsoleFixtureOutputPayload payload) {
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
        if (payload.appliesStrobe()) {
            writeAssignedDmxChannel(
                    parameterMap.getStrobeChannel(),
                    payload.strobe()
            );
        }

        refreshFromDmx();
    }

    public void refreshFromDmx() {
        FixtureOutput underlyingOutput = new FixtureOutput(
                readChannel(parameterMap.getRedChannel()),
                readChannel(parameterMap.getGreenChannel()),
                readChannel(parameterMap.getBlueChannel()),
                0,
                0,
                readChannel(parameterMap.getDimmerChannel()),
                128,
                128,
                0,
                0,
                readChannel(parameterMap.getStrobeChannel())
        );

        FixtureOutput showOutput = AutomaticDmxShowManager.createOutput(
                level(),
                blockPosition(),
                underlyingOutput,
                false
        );

        dmxOutput.set(showOutput == null ? underlyingOutput : showOutput);
        getEntityData().set(DATA_OUTPUT_RGB, dmxOutput.getPackedRgb());
        getEntityData().set(DATA_STROBE, dmxOutput.getStrobe());
        refreshSkinFromDmx();
    }

    private void refreshSkinFromDmx() {
        int renderedSkin = skin;

        if (FixtureParameterMap.isAssigned(skinDmxChannel)) {
            renderedSkin = MIN_SKIN
                    + DmxUniverseManager.getChannel(
                            universe,
                            skinDmxChannel
                    ) * DmxPixelBlockEntity.SKIN_COUNT / 256;
        }

        Integer showSkin =
                AutomaticDmxShowManager.createSkinOverride(
                        level(),
                        blockPosition(),
                        getUUID().getMostSignificantBits()
                                ^ getUUID().getLeastSignificantBits(),
                        renderedSkin
                );

        if (showSkin != null) {
            renderedSkin = showSkin;
        }

        getEntityData().set(
                DATA_RENDERED_SKIN,
                Math.clamp(renderedSkin, MIN_SKIN, MAX_SKIN)
        );
    }

    private boolean isVisibleStrobePhase() {
        int strobe = getEntityData().get(DATA_STROBE);

        if (strobe <= 0) {
            return true;
        }

        int halfPeriodTicks = Math.max(
                1,
                11 - Math.round(strobe / 255.0F * 10.0F)
        );
        return (level().getGameTime() / halfPeriodTicks) % 2L == 0L;
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

    private int readChannel(int channel) {
        return FixtureParameterMap.isAssigned(channel)
                ? DmxUniverseManager.getChannel(universe, channel)
                : 0;
    }

    private void writeAssignedDmxChannel(int channel, int value) {
        if (FixtureParameterMap.isAssigned(channel)) {
            DmxUniverseManager.setChannel(
                    universe,
                    channel,
                    Math.clamp(value, 0, 255)
            );
        }
    }

    private static int normalizeChannel(int channel) {
        return FixtureParameterMap.isAssigned(channel)
                ? channel
                : FixtureParameterMap.UNASSIGNED;
    }

    private static int clampUniverse(int value) {
        return Math.clamp(
                value,
                FixturePatch.MIN_UNIVERSE,
                FixturePatch.MAX_UNIVERSE
        );
    }

    private static String cleanName(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.length() > FixtureIdentity.MAX_NAME_LENGTH
                ? cleaned.substring(0, FixtureIdentity.MAX_NAME_LENGTH)
                : cleaned;
    }
}
