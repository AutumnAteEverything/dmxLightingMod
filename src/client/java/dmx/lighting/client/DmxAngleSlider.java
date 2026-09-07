package dmx.lighting.client;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * Slider for an installation angle from 0 through 359 degrees.
 *
 * This is separate from DmxValueSlider because installation orientation
 * uses degrees rather than an 8-bit DMX value.
 */
public final class DmxAngleSlider
        extends AbstractSliderButton {

    public static final int MINIMUM_DEGREES =
            0;

    public static final int MAXIMUM_DEGREES =
            359;

    private static final int ANGLE_RANGE =
            MAXIMUM_DEGREES
                    - MINIMUM_DEGREES;

    private final String label;
    private final IntConsumer changeListener;

    /**
     * Prevents programmatic updates from sending a network packet.
     */
    private boolean notifying =
            true;

    public DmxAngleSlider(
            int x,
            int y,
            int width,
            int height,
            String label,
            int initialDegrees,
            IntConsumer changeListener
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.empty(),
                degreesToSliderValue(
                        initialDegrees
                )
        );

        this.label =
                label;

        this.changeListener =
                changeListener;

        updateMessage();
    }

    /**
     * Returns the current angle from 0 through 359 degrees.
     */
    public int getDegrees() {
        return clampDegrees(
                (int) Math.round(
                        value
                                * ANGLE_RANGE
                )
                        + MINIMUM_DEGREES
        );
    }

    /**
     * Changes the angle and notifies the listener.
     */
    public void setDegrees(
            int newDegrees
    ) {
        setDegrees(
                newDegrees,
                true
        );
    }

    /**
     * Changes the angle.
     *
     * notifyListener should normally be false while initializing or
     * synchronizing the screen from the block entity.
     */
    public void setDegrees(
            int newDegrees,
            boolean notifyListener
    ) {
        notifying =
                notifyListener;

        value =
                degreesToSliderValue(
                        newDegrees
                );

        updateMessage();

        if (notifying
                && changeListener != null) {

            changeListener.accept(
                    getDegrees()
            );
        }

        notifying =
                true;
    }

    @Override
    protected void updateMessage() {
        setMessage(
                Component.literal(
                        label
                                + ": "
                                + getDegrees()
                                + "°"
                )
        );
    }

    /**
     * Called continuously while the player drags the slider.
     */
    @Override
    protected void applyValue() {
        if (notifying
                && changeListener != null) {

            changeListener.accept(
                    getDegrees()
            );
        }
    }

    private static double degreesToSliderValue(
            int degrees
    ) {
        return (
                clampDegrees(
                        degrees
                )
                        - MINIMUM_DEGREES
        )
                / (double) ANGLE_RANGE;
    }

    private static int clampDegrees(
            int degrees
    ) {
        return Math.max(
                MINIMUM_DEGREES,
                Math.min(
                        MAXIMUM_DEGREES,
                        degrees
                )
        );
    }
}