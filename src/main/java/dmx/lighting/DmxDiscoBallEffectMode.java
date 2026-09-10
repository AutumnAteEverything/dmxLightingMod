package dmx.lighting;

/** Selects the visible projection produced by a DMX Disco Ball. */
public enum DmxDiscoBallEffectMode {
    BEAMS("beams"),
    DOTS("dots");

    private final String serializedName;

    DmxDiscoBallEffectMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static DmxDiscoBallEffectMode fromDmxValue(int value) {
        return value >= 128 ? DOTS : BEAMS;
    }

    public static DmxDiscoBallEffectMode fromSerializedName(
            String serializedName
    ) {
        for (DmxDiscoBallEffectMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(serializedName)) {
                return mode;
            }
        }

        return BEAMS;
    }
}
