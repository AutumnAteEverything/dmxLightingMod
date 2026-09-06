package dmx.lighting;

/**
 * Selects the live source for an individually overrideable fixture
 * parameter while the fixture itself remains in DMX mode.
 */
public enum FixtureParameterControlMode {

    /**
     * Follow the assigned DMX channel.
     */
    DMX,

    /**
     * Use the fixture's stored manual value.
     */
    MANUAL
}