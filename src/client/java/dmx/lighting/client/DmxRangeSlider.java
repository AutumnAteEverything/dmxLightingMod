package dmx.lighting.client;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * Slider supporting any inclusive integer range.
 *
 * This is useful for controls that are neither standard DMX values
 * from 0 through 255 nor full-circle angles from 0 through 359.
 *
 * Examples:
 *
 * - Beam width: 45 through 135 degrees
 * - Beam length: 1 through 25 blocks
 */
public final class DmxRangeSlider
        extends AbstractSliderButton {

    private final String label;
    private final String suffix;

    private final int minimumValue;
    private final int maximumValue;

    private final IntConsumer changeListener;

    /**
     * Prevents programmatic updates from notifying the listener when
     * notification is not desired.
     */
    private boolean notifying =
            true;

    /**
     * Creates a ranged integer slider.
     *
     * @param x slider X position
     * @param y slider Y position
     * @param width slider width
     * @param height slider height
     * @param label visible label
     * @param suffix visible unit suffix, such as "°" or " blocks"
     * @param minimumValue inclusive minimum
     * @param maximumValue inclusive maximum
     * @param initialValue initial integer value
     * @param changeListener called continuously while moved
     */
    public DmxRangeSlider(
            int x,
            int y,
            int width,
            int height,
            String label,
            String suffix,
            int minimumValue,
            int maximumValue,
            int initialValue,
            IntConsumer changeListener
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.empty(),
                valueToSliderPosition(
                        initialValue,
                        minimumValue,
                        maximumValue
                )
        );

        if (maximumValue <= minimumValue) {
            throw new IllegalArgumentException(
                    "maximumValue must be greater than minimumValue"
            );
        }

        this.label =
                label == null
                        ? ""
                        : label;

        this.suffix =
                suffix == null
                        ? ""
                        : suffix;

        this.minimumValue =
                minimumValue;

        this.maximumValue =
                maximumValue;

        this.changeListener =
                changeListener;

        /*
         * The AbstractSliderButton constructor may invoke methods before
         * these fields are assigned, so refresh the final message here.
         */
        updateMessage();
    }

    /**
     * Returns the current integer value.
     */
    public int getRangeValue() {
        double scaledValue =
                minimumValue
                        + value
                        * (
                        maximumValue
                                - minimumValue
                );

        return clamp(
                (int) Math.round(
                        scaledValue
                )
        );
    }

    /**
     * Changes the current value and notifies the listener.
     */
    public void setRangeValue(
            int newValue
    ) {
        setRangeValue(
                newValue,
                true
        );
    }

    /**
     * Changes the current value.
     *
     * @param newValue new integer value
     * @param notifyListener whether to invoke the listener
     */
    public void setRangeValue(
            int newValue,
            boolean notifyListener
    ) {
        notifying =
                notifyListener;

        value =
                valueToSliderPosition(
                        newValue,
                        minimumValue,
                        maximumValue
                );

        updateMessage();

        if (notifying
                && changeListener != null) {

            changeListener.accept(
                    getRangeValue()
            );
        }

        notifying =
                true;
    }

    @Override
    protected void updateMessage() {
        /*
         * During the superclass constructor, our own fields have not yet
         * been initialized. Avoid constructing the final message until
         * the range is valid.
         */
        if (maximumValue <= minimumValue) {
            setMessage(
                    Component.empty()
            );

            return;
        }

        setMessage(
                Component.literal(
                        label
                                + ": "
                                + getRangeValue()
                                + suffix
                )
        );
    }

    /**
     * Called continuously while the slider is dragged.
     */
    @Override
    protected void applyValue() {
        if (notifying
                && changeListener != null) {

            changeListener.accept(
                    getRangeValue()
            );
        }
    }

    /**
     * Converts an integer value into AbstractSliderButton's normalized
     * position from 0.0 through 1.0.
     */
    private static double valueToSliderPosition(
            int value,
            int minimumValue,
            int maximumValue
    ) {
        if (maximumValue <= minimumValue) {
            return 0.0D;
        }

        int clampedValue =
                Math.max(
                        minimumValue,
                        Math.min(
                                maximumValue,
                                value
                        )
                );

        return (
                clampedValue
                        - minimumValue
        )
                / (double) (
                maximumValue
                        - minimumValue
        );
    }

    /**
     * Restricts a value to this slider's configured range.
     */
    private int clamp(
            int value
    ) {
        return Math.max(
                minimumValue,
                Math.min(
                        maximumValue,
                        value
                )
        );
    }
}