package dmx.lighting;

import java.util.Objects;

/**
 * Stores the current operating state of one fixture.
 *
 * The fixture keeps two independent outputs:
 *
 * dmxOutput
 *     The latest values read from the assigned DMX universe.
 *
 * manualOutput
 *     Values selected through the fixture editor, presets, groups,
 *     controllers, or future cue systems.
 *
 * The selected fixture-wide control mode still determines the primary
 * output source.
 *
 * Beam Width and Beam Length may additionally select their own source
 * while the fixture remains in DMX mode.
 */
public final class FixtureState {

    private FixtureControlMode controlMode;

    private FixtureParameterControlMode beamWidthControlMode =
            FixtureParameterControlMode.DMX;

    private FixtureParameterControlMode beamLengthControlMode =
            FixtureParameterControlMode.DMX;

    private final FixtureOutput dmxOutput;
    private final FixtureOutput manualOutput;

    /**
     * Creates a fixture in DMX mode.
     *
     * Both DMX and manual outputs begin blacked out.
     */
    public FixtureState() {
        this(
                FixtureControlMode.DMX,
                new FixtureOutput(),
                new FixtureOutput()
        );
    }

    /**
     * Creates a fixture state with explicit values.
     *
     * Beam Width and Beam Length default to DMX control.
     */
    public FixtureState(
            FixtureControlMode controlMode,
            FixtureOutput dmxOutput,
            FixtureOutput manualOutput
    ) {
        this.controlMode =
                controlMode == null
                        ? FixtureControlMode.DMX
                        : controlMode;

        this.dmxOutput =
                dmxOutput == null
                        ? new FixtureOutput()
                        : dmxOutput.copy();

        this.manualOutput =
                manualOutput == null
                        ? new FixtureOutput()
                        : manualOutput.copy();
    }

    /*
     * -----------------------------------------------------------------
     * Fixture-wide control mode
     * -----------------------------------------------------------------
     */

    public FixtureControlMode getControlMode() {
        return controlMode;
    }

    public void setControlMode(
            FixtureControlMode controlMode
    ) {
        this.controlMode =
                controlMode == null
                        ? FixtureControlMode.DMX
                        : controlMode;
    }

    public boolean isDmxMode() {
        return controlMode
                == FixtureControlMode.DMX;
    }

    public boolean isManualMode() {
        return controlMode
                == FixtureControlMode.MANUAL;
    }

    public void useDmxMode() {
        setControlMode(
                FixtureControlMode.DMX
        );
    }

    public void useManualMode() {
        setControlMode(
                FixtureControlMode.MANUAL
        );
    }

    /*
     * -----------------------------------------------------------------
     * Per-parameter control modes
     * -----------------------------------------------------------------
     */

    public FixtureParameterControlMode
    getBeamWidthControlMode() {
        return beamWidthControlMode;
    }

    public void setBeamWidthControlMode(
            FixtureParameterControlMode mode
    ) {
        beamWidthControlMode =
                mode == null
                        ? FixtureParameterControlMode.DMX
                        : mode;
    }

    public FixtureParameterControlMode
    getBeamLengthControlMode() {
        return beamLengthControlMode;
    }

    public void setBeamLengthControlMode(
            FixtureParameterControlMode mode
    ) {
        beamLengthControlMode =
                mode == null
                        ? FixtureParameterControlMode.DMX
                        : mode;
    }

    public boolean isBeamWidthDmxControlled() {
        return beamWidthControlMode
                == FixtureParameterControlMode.DMX;
    }

    public boolean isBeamWidthManualControlled() {
        return beamWidthControlMode
                == FixtureParameterControlMode.MANUAL;
    }

    public boolean isBeamLengthDmxControlled() {
        return beamLengthControlMode
                == FixtureParameterControlMode.DMX;
    }

    public boolean isBeamLengthManualControlled() {
        return beamLengthControlMode
                == FixtureParameterControlMode.MANUAL;
    }

    /*
     * -----------------------------------------------------------------
     * DMX output
     * -----------------------------------------------------------------
     */

    public FixtureOutput getDmxOutput() {
        return dmxOutput;
    }

    public void setDmxOutput(
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        dmxOutput.set(
                red,
                green,
                blue,
                dimmer
        );
    }

    public void setDmxOutput(
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
        dmxOutput.set(
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
        );
    }

    public void setDmxOutput(
            FixtureOutput output
    ) {
        dmxOutput.set(
                output
        );
    }

    /*
     * -----------------------------------------------------------------
     * Manual output
     * -----------------------------------------------------------------
     */

    public FixtureOutput getManualOutput() {
        return manualOutput;
    }

    public void setManualOutput(
            int red,
            int green,
            int blue,
            int dimmer
    ) {
        manualOutput.set(
                red,
                green,
                blue,
                dimmer
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
        manualOutput.set(
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
        );
    }

    public void setManualOutput(
            FixtureOutput output
    ) {
        manualOutput.set(
                output
        );
    }

    /*
     * -----------------------------------------------------------------
     * Active output
     * -----------------------------------------------------------------
     */

    public FixtureOutput getActiveOutput() {
        return isManualMode()
                ? manualOutput
                : dmxOutput;
    }

    public FixtureOutput copyActiveOutput() {
        return getActiveOutput()
                .copy();
    }

    /*
     * -----------------------------------------------------------------
     * Active raw values
     * -----------------------------------------------------------------
     */

    public int getActiveRed() {
        return getActiveOutput()
                .getRed();
    }

    public int getActiveGreen() {
        return getActiveOutput()
                .getGreen();
    }

    public int getActiveBlue() {
        return getActiveOutput()
                .getBlue();
    }

    public int getActiveWhite() {
        return getActiveOutput()
                .getWhite();
    }

    public int getActiveDimmer() {
        return getActiveOutput()
                .getDimmer();
    }

    public int getActivePan() {
        return getActiveOutput()
                .getPan();
    }

    public int getActiveTilt() {
        return getActiveOutput()
                .getTilt();
    }

    public int getActiveZoom() {
        return getActiveOutput()
                .getZoom();
    }

    public int getActiveStrobe() {
        return getActiveOutput()
                .getStrobe();
    }

    public int getActiveGobo() {
        return getActiveOutput()
                .getGobo();
    }

    /**
     * Returns the active raw Beam Width parameter.
     *
     * Whole-fixture Manual mode always uses the manual value.
     *
     * In DMX mode, the per-parameter Beam Width control mode decides
     * whether DMX or Manual is authoritative.
     */
    public int getActiveBeamWidth() {
        if (isManualMode()) {
            return manualOutput.getBeamWidth();
        }

        return isBeamWidthManualControlled()
                ? manualOutput.getBeamWidth()
                : dmxOutput.getBeamWidth();
    }

    /**
     * Returns the active raw Beam Length parameter.
     *
     * Whole-fixture Manual mode always uses the manual value.
     *
     * In DMX mode, the per-parameter Beam Length control mode decides
     * whether DMX or Manual is authoritative.
     */
    public int getActiveBeamLength() {
        if (isManualMode()) {
            return manualOutput.getBeamLength();
        }

        return isBeamLengthManualControlled()
                ? manualOutput.getBeamLength()
                : dmxOutput.getBeamLength();
    }

    /*
     * -----------------------------------------------------------------
     * Visible output calculations
     * -----------------------------------------------------------------
     */

    public int getOutputRed() {
        return getActiveOutput()
                .getOutputRed();
    }

    public int getOutputGreen() {
        return getActiveOutput()
                .getOutputGreen();
    }

    public int getOutputBlue() {
        return getActiveOutput()
                .getOutputBlue();
    }

    public int getOutputWhite() {
        return getActiveOutput()
                .getOutputWhite();
    }

    public int getPackedRgb() {
        return getActiveOutput()
                .getPackedRgb();
    }

    public int getMinecraftLightLevel() {
        return getActiveOutput()
                .getMinecraftLightLevel();
    }

    /*
     * -----------------------------------------------------------------
     * Blackout and capture operations
     * -----------------------------------------------------------------
     */

    public void blackoutManual() {
        manualOutput.blackout();
    }

    public void blackoutDmx() {
        dmxOutput.blackout();
    }

    public void blackoutAll() {
        blackoutDmx();
        blackoutManual();
    }

    public void captureActiveOutputToManual() {
        manualOutput.set(
                getActiveOutput()
        );
    }

    public void takeManualControl() {
        if (isDmxMode()) {
            manualOutput.set(
                    dmxOutput
            );
        }

        useManualMode();
    }

    /*
     * -----------------------------------------------------------------
     * Complete-state operations
     * -----------------------------------------------------------------
     */

    public void set(
            FixtureControlMode controlMode,
            FixtureOutput dmxOutput,
            FixtureOutput manualOutput
    ) {
        setControlMode(
                controlMode
        );

        setDmxOutput(
                dmxOutput
        );

        setManualOutput(
                manualOutput
        );
    }

    public FixtureState copy() {
        FixtureState copy =
                new FixtureState(
                        controlMode,
                        dmxOutput,
                        manualOutput
                );

        copy.setBeamWidthControlMode(
                beamWidthControlMode
        );

        copy.setBeamLengthControlMode(
                beamLengthControlMode
        );

        return copy;
    }

    /*
     * -----------------------------------------------------------------
     * Object methods
     * -----------------------------------------------------------------
     */

    @Override
    public boolean equals(
            Object object
    ) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof FixtureState other)) {
            return false;
        }

        return controlMode
                == other.controlMode
                && beamWidthControlMode
                == other.beamWidthControlMode
                && beamLengthControlMode
                == other.beamLengthControlMode
                && dmxOutput.equals(
                        other.dmxOutput
                )
                && manualOutput.equals(
                        other.manualOutput
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                controlMode,
                beamWidthControlMode,
                beamLengthControlMode,
                dmxOutput,
                manualOutput
        );
    }

    @Override
    public String toString() {
        return "FixtureState{"
                + "controlMode="
                + controlMode
                + ", beamWidthControlMode="
                + beamWidthControlMode
                + ", beamLengthControlMode="
                + beamLengthControlMode
                + ", dmxOutput="
                + dmxOutput
                + ", manualOutput="
                + manualOutput
                + '}';
    }
}