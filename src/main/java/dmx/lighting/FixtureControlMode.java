package dmx.lighting;

/**
 * Determines where a DMX fixture gets its visible output.
 */
public enum FixtureControlMode {

    /**
     * The fixture reads RGBD values from its assigned DMX universe.
     */
    DMX("dmx"),

    /**
     * The fixture uses RGBD values entered directly by the user.
     */
    MANUAL("manual");

    private final String serializedName;

    FixtureControlMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Converts saved or command text into a control mode.
     *
     * @return the matching mode, or null when invalid
     */
    public static FixtureControlMode fromName(String name) {
        if (name == null) {
            return null;
        }

        for (FixtureControlMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(name)) {
                return mode;
            }
        }

        return null;
    }
}