package dmx.lighting.client;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * Slider for one DMX value from 0 through 255.
 *
 * User interaction:
 *
 *     Moving the slider calls the supplied change listener.
 *
 * Programmatic synchronization:
 *
 *     Calling setDmxValue(...) updates the displayed slider value
 *     without calling the change listener.
 *
 * Mixed state:
 *
 *     Calling setMixed(true) marks the slider as representing multiple
 *     differing values.
 *
 *     While mixed, the slider displays:
 *
 *         Label: Mixed
 *
 *     As soon as the user moves the slider, the mixed state is cleared
 *     and the slider becomes a normal concrete DMX value.
 *
 * This distinction is useful for the Lighting Console Output view:
 *
 * - A single fixture has one concrete value.
 *
 * - A group may contain fixtures with different stored values.
 *
 * - The All target may also contain different stored values.
 *
 * - Loading values into the UI must not transmit output.
 *
 * - Actual user movement should transmit or update output.
 */
public class DmxValueSlider extends AbstractSliderButton {

    /*
     * -----------------------------------------------------------------
     * Slider identity
     * -----------------------------------------------------------------
     */

    private final String label;

    private final IntConsumer changeListener;

    /*
     * -----------------------------------------------------------------
     * Mixed state
     * -----------------------------------------------------------------
     */

    /**
     * True when the slider represents multiple differing source
     * values rather than one concrete DMX value.
     */
    private boolean mixed =
            false;

    /*
     * -----------------------------------------------------------------
     * Construction
     * -----------------------------------------------------------------
     */

    public DmxValueSlider(
            int x,
            int y,
            int width,
            int height,
            String label,
            int initialValue,
            IntConsumer changeListener
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.empty(),
                clamp(
                        initialValue
                )
                        / 255.0D
        );

        this.label =
                label == null
                        ? ""
                        : label;

        this.changeListener =
                changeListener;

        updateMessage();
    }

    /*
     * -----------------------------------------------------------------
     * Value access
     * -----------------------------------------------------------------
     */

    /**
     * Returns the current slider value from 0 through 255.
     *
     * If the slider is currently marked Mixed, this still returns the
     * slider's internal numeric position.
     *
     * Call isMixed() when the distinction matters.
     */
    public int getDmxValue() {
        return clamp(
                (int) Math.round(
                        value
                                * 255.0D
                )
        );
    }

    /**
     * Changes the slider value programmatically.
     *
     * This intentionally DOES NOT call the change listener.
     *
     * Setting a concrete value also clears the mixed state.
     */
    public void setDmxValue(
            int newValue
    ) {
        int safeValue =
                clamp(
                        newValue
                );

        value =
                safeValue
                        / 255.0D;

        mixed =
                false;

        updateMessage();
    }

    /*
     * -----------------------------------------------------------------
     * Mixed-state access
     * -----------------------------------------------------------------
     */

    /**
     * Returns true when this control currently represents differing
     * values from multiple fixtures.
     */
    public boolean isMixed() {
        return mixed;
    }

    /**
     * Sets whether this slider should display a mixed state.
     *
     * This does not fire the change listener.
     *
     * The slider's underlying numeric position is preserved so the
     * thumb does not jump unexpectedly.
     */
    public void setMixed(
            boolean mixed
    ) {
        if (this.mixed == mixed) {
            return;
        }

        this.mixed =
                mixed;

        updateMessage();
    }

    /**
     * Convenience helper for putting the slider into mixed state.
     */
    public void setMixed() {
        setMixed(
                true
        );
    }

    /**
     * Clears the mixed state while retaining the current numeric value.
     *
     * This does not fire the change listener.
     */
    public void clearMixed() {
        setMixed(
                false
        );
    }

    /*
     * -----------------------------------------------------------------
     * Display
     * -----------------------------------------------------------------
     */

    /**
     * Updates the text displayed inside the slider.
     */
    @Override
    protected void updateMessage() {
        if (mixed) {
            setMessage(
                    Component.literal(
                            label
                                    + ": Mixed"
                    )
            );

            return;
        }

        setMessage(
                Component.literal(
                        label
                                + ": "
                                + getDmxValue()
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * User interaction
     * -----------------------------------------------------------------
     */

    /**
     * Called by AbstractSliderButton when the user changes the slider.
     *
     * User interaction resolves a Mixed value into a concrete value.
     *
     * This is the only place where the external change listener is
     * invoked.
     */
    @Override
    protected void applyValue() {
        /*
         * Once the user manipulates this parameter, they are choosing
         * one explicit value for the target set.
         */
        if (mixed) {
            mixed =
                    false;

            updateMessage();
        }

        if (changeListener != null) {
            changeListener.accept(
                    getDmxValue()
            );
        }
    }

    /*
     * -----------------------------------------------------------------
     * Helpers
     * -----------------------------------------------------------------
     */

    /**
     * Clamps a value to the legal 8-bit DMX range.
     */
    private static int clamp(
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
}