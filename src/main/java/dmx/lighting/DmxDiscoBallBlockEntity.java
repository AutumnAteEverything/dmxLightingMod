package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** DMX state and smooth bipolar rotation for a Disco Ball block. */
public final class DmxDiscoBallBlockEntity
        extends DmxFixtureBlockEntity {

    private static final float MAX_DEGREES_PER_SECOND =
            180.0F;

    private double lastSpinSampleTime =
            Double.NaN;

    private float displayedSpinDegrees;
    private float sampledInitialDegrees =
            Float.NaN;

    private DmxDiscoBallEffectMode effectMode =
            DmxDiscoBallEffectMode.BEAMS;

    public DmxDiscoBallBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(
                ModBlockEntities.DMX_DISCO_BALL_BLOCK_ENTITY,
                blockPos,
                blockState
        );

        getIdentity().setFixtureProfile(DmxDiscoBallProfile.ID);
        getParameterMap().set(
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                1,
                3,
                2,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED,
                FixtureParameterMap.UNASSIGNED
        );
        setManualParameterOutput(
                0,
                0,
                0,
                0,
                0,
                255,
                128,
                128,
                0,
                0,
                0
        );
    }

    @Override
    public String getFixtureType() {
        return DmxDiscoBallProfile.ID;
    }

    @Override
    public DmxFixtureProfile getCurrentProfile() {
        return DmxDiscoBallProfile.INSTANCE;
    }

    @Override
    public void setFixtureType(String fixtureType) {
        getIdentity().setFixtureProfile(DmxDiscoBallProfile.ID);
    }

    @Override
    protected FixtureOutput createAutomaticDmxOutput(
            FixtureOutput underlyingOutput
    ) {
        return AutomaticDmxShowManager.createDiscoBallOutput(
                level,
                worldPosition,
                underlyingOutput
        );
    }

    /** Returns a useful white intensity swatch for the console row. */
    @Override
    public int getOutputPackedRgb() {
        int brightness = getActiveDimmer();
        return brightness << 16
                | brightness << 8
                | brightness;
    }

    public boolean isBaseOnTop() {
        float tilt = getMountTiltDegrees();
        return tilt >= 90.0F && tilt < 270.0F;
    }

    public void setInstallation(
            float initialDegrees,
            boolean baseOnTop
    ) {
        setMountOrientation(
                initialDegrees,
                baseOnTop ? 180.0F : 0.0F
        );
    }

    public DmxDiscoBallEffectMode getEffectMode() {
        return effectMode;
    }

    /** Resolves the patched DMX effect channel or the saved fallback mode. */
    public DmxDiscoBallEffectMode getResolvedEffectMode() {
        if (getControlMode() == FixtureControlMode.DMX
                && FixtureParameterMap.isAssigned(
                        getParameterMap().getStrobeChannel()
                )) {
            return DmxDiscoBallEffectMode.fromDmxValue(getDmxStrobe());
        }

        return effectMode;
    }

    public void setEffectMode(DmxDiscoBallEffectMode effectMode) {
        DmxDiscoBallEffectMode safeMode =
                effectMode == null
                        ? DmxDiscoBallEffectMode.BEAMS
                        : effectMode;

        if (this.effectMode == safeMode) {
            return;
        }

        this.effectMode = safeMode;
        setChanged();
    }

    /**
     * Integrates rotation on the client without changing world state.
     * DMX 128 is stopped, 0 is maximum reverse, and 255 is maximum
     * forward.
     */
    public float getSpinRotationDegrees(float tickProgress) {
        if (level == null) {
            return getMountPanDegrees();
        }

        double now = level.getGameTime()
                + Math.max(0.0F, Math.min(1.0F, tickProgress));
        float initialDegrees = getMountPanDegrees();

        if (!Double.isFinite(lastSpinSampleTime)
                || sampledInitialDegrees != initialDegrees) {
            displayedSpinDegrees = initialDegrees;
            sampledInitialDegrees = initialDegrees;
            lastSpinSampleTime = now;
            return displayedSpinDegrees;
        }

        double elapsedTicks = Math.max(
                0.0D,
                Math.min(5.0D, now - lastSpinSampleTime)
        );

        displayedSpinDegrees = normalizeDegrees(
                displayedSpinDegrees
                        + getSpinDegreesPerSecond()
                        * (float) elapsedTicks
                        / 20.0F
        );
        lastSpinSampleTime = now;
        return displayedSpinDegrees;
    }

    public float getSpinDegreesPerSecond() {
        int spin = getActivePan();

        if (spin == 128) {
            return 0.0F;
        }

        float normalized = spin < 128
                ? (spin - 128) / 128.0F
                : (spin - 128) / 127.0F;

        return normalized * MAX_DEGREES_PER_SECOND;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString(
                "disco_effect_mode",
                effectMode.getSerializedName()
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        effectMode = DmxDiscoBallEffectMode.fromSerializedName(
                input.getStringOr(
                        "disco_effect_mode",
                        DmxDiscoBallEffectMode.BEAMS.getSerializedName()
                )
        );
    }

    private static float normalizeDegrees(float degrees) {
        float normalized = degrees % 360.0F;
        return normalized < 0.0F
                ? normalized + 360.0F
                : normalized;
    }
}
