package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * DMX-controlled full-cube color block.
 *
 * It reuses the fixture state and networking model so it participates
 * in the lighting console, groups, manual control, and patch views.
 * Its physical capabilities are limited to RGB, Dimmer, and Strobe.
 */
public final class DmxPixelBlockEntity
        extends DmxFixtureBlockEntity {

    public static final int MIN_SKIN =
            1;

    public static final int MAX_SKIN =
            5;

    private static final String SKIN_KEY =
            "pixel_skin";

    private static final String SKIN_DMX_CHANNEL_KEY =
            "pixel_skin_dmx_channel";

    private static final String RENDERED_SKIN_KEY =
            "pixel_rendered_skin";

    private int skin =
            MIN_SKIN;

    private int skinDmxChannel =
            FixtureParameterMap.UNASSIGNED;

    private int renderedSkin =
            MIN_SKIN;

    private boolean lastVisibleStrobeState =
            true;

    public DmxPixelBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(
                ModBlockEntities.DMX_PIXEL_BLOCK_ENTITY,
                blockPos,
                blockState
        );

        getIdentity().setFixtureProfile(
                DmxPixelProfile.ID
        );
    }

    @Override
    public String getFixtureType() {
        return DmxPixelProfile.ID;
    }

    @Override
    public DmxFixtureProfile getCurrentProfile() {
        return DmxPixelProfile.INSTANCE;
    }

    /**
     * Returns the current visual color, including the strobe blackout
     * phase. This never affects Minecraft's world-light level.
     */
    public int getVisiblePackedRgb() {
        return isVisibleStrobePhase()
                ? getOutputPackedRgb()
                : 0;
    }

    public int getSkin() {
        return skin;
    }

    public int getSkinDmxChannel() {
        return skinDmxChannel;
    }

    public int getRenderedSkin() {
        return renderedSkin;
    }

    public void setSkin(
            int skin
    ) {
        setSkinConfiguration(
                skin,
                skinDmxChannel
        );
    }

    public void setSkinConfiguration(
            int skin,
            int skinDmxChannel
    ) {
        int safeSkin =
                Math.clamp(
                        skin,
                        MIN_SKIN,
                        MAX_SKIN
                );

        int safeChannel =
                normalizeSkinDmxChannel(
                        skinDmxChannel
                );

        boolean configurationChanged =
                this.skin != safeSkin
                        || this.skinDmxChannel != safeChannel;

        this.skin =
                safeSkin;

        this.skinDmxChannel =
                safeChannel;

        refreshSkinFromDmx();

        if (configurationChanged) {
            setChanged();
            sendBlockEntityUpdate();
        }
    }

    @Override
    public void refreshFromDmx() {
        super.refreshFromDmx();

        refreshSkinFromDmx();
    }

    @Override
    public void setControlMode(
            FixtureControlMode controlMode
    ) {
        super.setControlMode(
                controlMode
        );

        refreshSkinFromDmx();
    }

    private void refreshSkinFromDmx() {
        int targetSkin =
                skin;

        if (getControlMode() == FixtureControlMode.DMX
                && FixtureParameterMap.isAssigned(
                        skinDmxChannel
                )) {

            int dmxValue =
                    DmxUniverseManager.getChannel(
                            getUniverse(),
                            skinDmxChannel
                    );

            targetSkin =
                    MIN_SKIN
                            + dmxValue
                            * MAX_SKIN
                            / 256;
        }

        setRenderedSkin(
                targetSkin
        );
    }

    private void setRenderedSkin(
            int renderedSkin
    ) {
        int safeSkin =
                Math.clamp(
                        renderedSkin,
                        MIN_SKIN,
                        MAX_SKIN
                );

        if (this.renderedSkin == safeSkin) {
            return;
        }

        this.renderedSkin =
                safeSkin;

        setChanged();

        sendBlockEntityUpdate();
    }

    private void sendBlockEntityUpdate() {
        if (level != null) {
            BlockState state = getBlockState();

            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    Block.UPDATE_ALL
            );
        }
    }

    private static int normalizeSkinDmxChannel(
            int channel
    ) {
        return FixtureParameterMap.isAssigned(
                channel
        )
                ? channel
                : FixtureParameterMap.UNASSIGNED;
    }

    @Override
    protected void saveAdditional(
            ValueOutput output
    ) {
        super.saveAdditional(
                output
        );

        output.putInt(
                SKIN_KEY,
                skin
        );

        output.putInt(
                SKIN_DMX_CHANNEL_KEY,
                skinDmxChannel
        );

        output.putInt(
                RENDERED_SKIN_KEY,
                renderedSkin
        );
    }

    @Override
    protected void loadAdditional(
            ValueInput input
    ) {
        super.loadAdditional(
                input
        );

        skin =
                Math.clamp(
                        input.getIntOr(
                                SKIN_KEY,
                                MIN_SKIN
                        ),
                        MIN_SKIN,
                        MAX_SKIN
                );

        skinDmxChannel =
                normalizeSkinDmxChannel(
                        input.getIntOr(
                                SKIN_DMX_CHANNEL_KEY,
                                FixtureParameterMap.UNASSIGNED
                        )
                );

        renderedSkin =
                Math.clamp(
                        input.getIntOr(
                                RENDERED_SKIN_KEY,
                                skin
                        ),
                        MIN_SKIN,
                        MAX_SKIN
                );
    }

    private boolean isVisibleStrobePhase() {
        int strobe =
                getActiveStrobe();

        if (strobe <= 0 || level == null) {
            return true;
        }

        int halfPeriodTicks =
                Math.max(
                        1,
                        11 - Math.round(
                                strobe / 255.0F * 10.0F
                        )
                );

        return (
                level.getGameTime()
                        / halfPeriodTicks
        ) % 2L == 0L;
    }

    private void updateClientStrobeRendering() {
        if (level == null
                || !level.isClientSide()) {

            return;
        }

        boolean visible =
                isVisibleStrobePhase();

        if (visible == lastVisibleStrobeState) {
            return;
        }

        lastVisibleStrobeState =
                visible;

        BlockState state =
                getBlockState();

        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
    }

    public static void tick(
            Level level,
            BlockPos blockPos,
            BlockState blockState,
            DmxPixelBlockEntity blockEntity
    ) {
        if (level.isClientSide()) {
            blockEntity.updateClientStrobeRendering();
            return;
        }

        DmxFixtureBlockEntity.tick(
                level,
                blockPos,
                blockState,
                blockEntity
        );
    }
}
