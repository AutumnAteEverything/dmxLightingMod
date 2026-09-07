package dmx.lighting;

import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable snapshot of one fixture for the Fixture Browser.
 *
 * This is not the live block entity itself. It contains only the
 * information needed to display and operate on one browser row.
 *
 * Keeping browser entries separate from block entities means the
 * client GUI never receives or retains direct server-side fixture
 * objects.
 *
 * In the parameter-centric architecture, fixtures no longer have one
 * base address. Instead, the browser snapshot carries the actual set of
 * assigned DMX channels.
 *
 * The snapshot also carries stored Manual output values so the
 * handheld Lighting Console Output view can inherit the selected
 * fixture's existing control values instead of starting from arbitrary
 * defaults.
 */
public record FixtureBrowserEntry(
        BlockPos position,
        String fixtureName,
        String groupName,
        String fixtureType,
        int universe,
        int[] assignedChannels,
        String controlMode,
        int outputRgb,
        int lightLevel,

        int manualRed,
        int manualGreen,
        int manualBlue,
        int manualWhite,
        int manualAmber,
        int manualDimmer,

        int manualPan,
        int manualTilt,

        int manualBeamWidth,
        int manualBeamLength,

        int manualStrobe,

        String targetKind,
        int targetEntityId,

        int redChannel,
        int greenChannel,
        int blueChannel,
        int dimmerChannel,
        int strobeChannel,
        int skin,
        int skinDmxChannel,

        boolean colorInterpolationEnabled,
        float colorInterpolationTimeSeconds
) {

    public static final String DEFAULT_NAME =
            "Unnamed Fixture";

    public static final String DEFAULT_GROUP =
            "Ungrouped";

    public static final String DEFAULT_TYPE =
            "rgbd";

    public static final String DEFAULT_MODE =
            "dmx";

    public static final String TARGET_BLOCK =
            "block";

    public static final String TARGET_MOB =
            "mob";

    public static final int NO_TARGET_ENTITY_ID =
            -1;

    /**
     * Creates and sanitizes one browser entry.
     */
    public FixtureBrowserEntry {
        position =
                position == null
                        ? BlockPos.ZERO
                        : position.immutable();

        fixtureName =
                cleanDisplayText(
                        fixtureName,
                        DEFAULT_NAME
                );

        groupName =
                cleanDisplayText(
                        groupName,
                        DEFAULT_GROUP
                );

        fixtureType =
                cleanIdentifier(
                        fixtureType,
                        DEFAULT_TYPE
                );

        universe =
                clamp(
                        universe,
                        DmxFixtureBlockEntity.MIN_UNIVERSE,
                        DmxFixtureBlockEntity.MAX_UNIVERSE
                );

        assignedChannels =
                normalizeChannels(
                        assignedChannels
                );

        controlMode =
                cleanIdentifier(
                        controlMode,
                        DEFAULT_MODE
                );

        outputRgb &=
                0x00FFFFFF;

        lightLevel =
                clamp(
                        lightLevel,
                        0,
                        15
                );

        manualRed =
                clampDmx(
                        manualRed
                );

        manualGreen =
                clampDmx(
                        manualGreen
                );

        manualBlue =
                clampDmx(
                        manualBlue
                );

        manualWhite =
                clampDmx(
                        manualWhite
                );

        manualAmber =
                clampDmx(
                        manualAmber
                );

        manualDimmer =
                clampDmx(
                        manualDimmer
                );

        manualPan =
                clampDmx(
                        manualPan
                );

        manualTilt =
                clampDmx(
                        manualTilt
                );

        manualBeamWidth =
                clampDmx(
                        manualBeamWidth
                );

        manualBeamLength =
                clampDmx(
                        manualBeamLength
                );

        manualStrobe =
                clampDmx(
                        manualStrobe
                );

        targetKind =
                cleanIdentifier(
                        targetKind,
                        TARGET_BLOCK
                );

        if (!TARGET_MOB.equals(
                targetKind
        )) {
            targetKind =
                    TARGET_BLOCK;
        }

        targetEntityId =
                TARGET_MOB.equals(
                        targetKind
                )
                        ? Math.max(
                                NO_TARGET_ENTITY_ID,
                                targetEntityId
                        )
                        : NO_TARGET_ENTITY_ID;

        redChannel =
                cleanDmxChannel(
                        redChannel
                );

        greenChannel =
                cleanDmxChannel(
                        greenChannel
                );

        blueChannel =
                cleanDmxChannel(
                        blueChannel
                );

        dimmerChannel =
                cleanDmxChannel(
                        dimmerChannel
                );

        strobeChannel =
                cleanDmxChannel(
                        strobeChannel
                );

        skin =
                clamp(
                        skin,
                        DmxPixelBlockEntity.MIN_SKIN,
                        DmxPixelBlockEntity.MAX_SKIN
                );

        skinDmxChannel =
                cleanDmxChannel(
                        skinDmxChannel
                );

        colorInterpolationTimeSeconds =
                ColorInterpolationSettings.clampTimeSeconds(
                        colorInterpolationTimeSeconds
                );
    }

    /**
     * Returns a defensive copy of the assigned DMX channels.
     */
    @Override
    public int[] assignedChannels() {
        return Arrays.copyOf(
                assignedChannels,
                assignedChannels.length
        );
    }

    /**
     * Creates a browser entry from one loaded fixture.
     */
    public static FixtureBrowserEntry fromFixture(
            DmxFixtureBlockEntity fixture
    ) {
        Objects.requireNonNull(
                fixture,
                "fixture"
        );

        String name =
                fixture.getFixtureName();

        if (name == null
                || name.isBlank()) {

            name =
                    DEFAULT_NAME;
        }

        String group =
                fixture.getGroupDisplayName();

        if (group == null
                || group.isBlank()) {

            group =
                    DEFAULT_GROUP;
        }

        FixtureControlMode mode =
                fixture.getControlMode();

        FixtureParameterMap map =
                fixture.getParameterMap();

        int[] assignedChannels =
                new int[] {
                        map.getRedChannel(),
                        map.getGreenChannel(),
                        map.getBlueChannel(),
                        map.getWhiteChannel(),
                        map.getAmberChannel(),
                        map.getDimmerChannel(),
                        map.getPanChannel(),
                        map.getTiltChannel(),
                        map.getBeamWidthChannel(),
                        map.getBeamLengthChannel(),
                        map.getStrobeChannel()
                };

        return new FixtureBrowserEntry(
                fixture.getBlockPos(),
                name,
                group,
                fixture.getFixtureType(),
                fixture.getUniverse(),
                assignedChannels,
                mode == null
                        ? DEFAULT_MODE
                        : mode.getSerializedName(),
                fixture.getOutputPackedRgb(),
                fixture.getMinecraftLightLevel(),

                fixture.getManualRed(),
                fixture.getManualGreen(),
                fixture.getManualBlue(),
                fixture.getManualWhite(),
                fixture.getManualAmber(),
                fixture.getManualDimmer(),

                fixture.getManualPan(),
                fixture.getManualTilt(),

                fixture.getManualBeamWidth(),
                fixture.getManualBeamLength(),

                fixture.getManualStrobe(),

                TARGET_BLOCK,
                NO_TARGET_ENTITY_ID,

                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getDimmerChannel(),
                map.getStrobeChannel(),
                fixture instanceof DmxPixelBlockEntity pixelBlock
                        ? pixelBlock.getSkin()
                        : DmxPixelBlockEntity.MIN_SKIN,
                fixture instanceof DmxPixelBlockEntity pixelBlock
                        ? pixelBlock.getSkinDmxChannel()
                        : FixtureParameterMap.UNASSIGNED,

                fixture.isColorInterpolationEnabled(),
                fixture.getColorInterpolationTimeSeconds()
        );
    }

    /**
     * Creates a browser entry from one loaded DMX mob.
     */
    public static FixtureBrowserEntry fromDmxParrot(
            DmxParrotEntity parrot
    ) {
        Objects.requireNonNull(
                parrot,
                "parrot"
        );

        FixtureParameterMap map =
                parrot.getParameterMap();

        int[] assignedChannels =
                new int[] {
                        map.getRedChannel(),
                        map.getGreenChannel(),
                        map.getBlueChannel(),
                        map.getWhiteChannel(),
                        map.getAmberChannel(),
                        map.getDimmerChannel(),
                        map.getPanChannel(),
                        map.getTiltChannel(),
                        map.getBeamWidthChannel(),
                        map.getBeamLengthChannel(),
                        map.getStrobeChannel()
                };

        FixtureOutput output =
                parrot.getCurrentOutput();

        return new FixtureBrowserEntry(
                parrot.blockPosition(),
                parrot.getConsoleName(),
                parrot.getGroupDisplayName(),
                DmxParrotProfile.ID,
                parrot.getUniverse(),
                assignedChannels,
                parrot.getControlMode()
                        .getSerializedName(),
                parrot.getOutputPackedRgb(),
                parrot.getMinecraftLightLevel(),

                output.getRed(),
                output.getGreen(),
                output.getBlue(),
                output.getWhite(),
                output.getAmber(),
                output.getDimmer(),

                output.getPan(),
                output.getTilt(),

                output.getBeamWidth(),
                output.getBeamLength(),

                output.getStrobe(),

                TARGET_MOB,
                parrot.getId(),

                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getDimmerChannel(),
                map.getStrobeChannel(),
                DmxPixelBlockEntity.MIN_SKIN,
                FixtureParameterMap.UNASSIGNED,

                parrot.isColorInterpolationEnabled(),
                parrot.getColorInterpolationTimeSeconds()
        );
    }

    /**
     * Creates a browser entry from one loaded DMX Enderman.
     */
    public static FixtureBrowserEntry fromDmxEnderman(
            DmxEndermanEntity enderman
    ) {
        Objects.requireNonNull(
                enderman,
                "enderman"
        );

        FixtureParameterMap map =
                enderman.getParameterMap();

        int[] assignedChannels =
                new int[] {
                        map.getRedChannel(),
                        map.getGreenChannel(),
                        map.getBlueChannel(),
                        map.getWhiteChannel(),
                        map.getAmberChannel(),
                        map.getDimmerChannel(),
                        map.getPanChannel(),
                        map.getTiltChannel(),
                        map.getBeamWidthChannel(),
                        map.getBeamLengthChannel(),
                        map.getStrobeChannel()
                };

        FixtureOutput output =
                enderman.getCurrentOutput();

        return new FixtureBrowserEntry(
                enderman.blockPosition(),
                enderman.getConsoleName(),
                enderman.getGroupDisplayName(),
                DmxEndermanProfile.ID,
                enderman.getUniverse(),
                assignedChannels,
                enderman.getControlMode()
                        .getSerializedName(),
                enderman.getOutputPackedRgb(),
                enderman.getMinecraftLightLevel(),

                output.getRed(),
                output.getGreen(),
                output.getBlue(),
                output.getWhite(),
                output.getAmber(),
                output.getDimmer(),

                output.getPan(),
                output.getTilt(),

                output.getBeamWidth(),
                output.getBeamLength(),

                output.getStrobe(),

                TARGET_MOB,
                enderman.getId(),

                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getDimmerChannel(),
                map.getStrobeChannel(),
                DmxPixelBlockEntity.MIN_SKIN,
                FixtureParameterMap.UNASSIGNED,

                enderman.isColorInterpolationEnabled(),
                enderman.getColorInterpolationTimeSeconds()
        );
    }

    /**
     * Creates a browser entry from one loaded DMX Warden.
     */
    public static FixtureBrowserEntry fromDmxWarden(
            DmxWardenEntity warden
    ) {
        Objects.requireNonNull(
                warden,
                "warden"
        );

        FixtureParameterMap map =
                warden.getParameterMap();

        int[] assignedChannels =
                new int[] {
                        map.getRedChannel(),
                        map.getGreenChannel(),
                        map.getBlueChannel(),
                        map.getWhiteChannel(),
                        map.getAmberChannel(),
                        map.getDimmerChannel(),
                        map.getPanChannel(),
                        map.getTiltChannel(),
                        map.getBeamWidthChannel(),
                        map.getBeamLengthChannel(),
                        map.getStrobeChannel()
                };

        FixtureOutput output =
                warden.getCurrentOutput();

        return new FixtureBrowserEntry(
                warden.blockPosition(),
                warden.getConsoleName(),
                warden.getGroupDisplayName(),
                DmxWardenProfile.ID,
                warden.getUniverse(),
                assignedChannels,
                warden.getControlMode()
                        .getSerializedName(),
                warden.getOutputPackedRgb(),
                warden.getMinecraftLightLevel(),

                output.getRed(),
                output.getGreen(),
                output.getBlue(),
                output.getWhite(),
                output.getAmber(),
                output.getDimmer(),

                output.getPan(),
                output.getTilt(),

                output.getBeamWidth(),
                output.getBeamLength(),

                output.getStrobe(),

                TARGET_MOB,
                warden.getId(),

                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getDimmerChannel(),
                map.getStrobeChannel(),
                DmxPixelBlockEntity.MIN_SKIN,
                FixtureParameterMap.UNASSIGNED,

                warden.isColorInterpolationEnabled(),
                warden.getColorInterpolationTimeSeconds()
        );
    }

    /**
     * Creates a browser entry from one loaded DMX Nautilus.
     */
    public static FixtureBrowserEntry fromDmxNautilus(
            DmxNautilusEntity nautilus
    ) {
        Objects.requireNonNull(
                nautilus,
                "nautilus"
        );

        FixtureParameterMap map =
                nautilus.getParameterMap();

        int[] assignedChannels =
                new int[] {
                        map.getRedChannel(),
                        map.getGreenChannel(),
                        map.getBlueChannel(),
                        map.getWhiteChannel(),
                        map.getAmberChannel(),
                        map.getDimmerChannel(),
                        map.getPanChannel(),
                        map.getTiltChannel(),
                        map.getBeamWidthChannel(),
                        map.getBeamLengthChannel(),
                        map.getStrobeChannel()
                };

        FixtureOutput output =
                nautilus.getCurrentOutput();

        return new FixtureBrowserEntry(
                nautilus.blockPosition(),
                nautilus.getConsoleName(),
                nautilus.getGroupDisplayName(),
                DmxNautilusProfile.ID,
                nautilus.getUniverse(),
                assignedChannels,
                nautilus.getControlMode()
                        .getSerializedName(),
                nautilus.getOutputPackedRgb(),
                nautilus.getMinecraftLightLevel(),

                output.getRed(),
                output.getGreen(),
                output.getBlue(),
                output.getWhite(),
                output.getAmber(),
                output.getDimmer(),

                output.getPan(),
                output.getTilt(),

                output.getBeamWidth(),
                output.getBeamLength(),

                output.getStrobe(),

                TARGET_MOB,
                nautilus.getId(),

                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getDimmerChannel(),
                map.getStrobeChannel(),
                DmxPixelBlockEntity.MIN_SKIN,
                FixtureParameterMap.UNASSIGNED,

                nautilus.isColorInterpolationEnabled(),
                nautilus.getColorInterpolationTimeSeconds()
        );
    }

    /** Creates a browser entry from one loaded DMX Block Display. */
    public static FixtureBrowserEntry fromDmxBlockDisplay(
            DmxBlockDisplayEntity display
    ) {
        Objects.requireNonNull(display, "display");

        FixtureParameterMap map = display.getParameterMap();
        int[] assignedChannels = new int[] {
                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getDimmerChannel(),
                map.getStrobeChannel(),
                display.getSkinDmxChannel()
        };
        FixtureOutput output = display.getCurrentOutput();

        return new FixtureBrowserEntry(
                display.blockPosition(),
                display.getConsoleName(),
                display.getGroupDisplayName(),
                DmxBlockDisplayProfile.ID,
                display.getUniverse(),
                assignedChannels,
                display.getControlMode().getSerializedName(),
                display.getOutputPackedRgb(),
                display.getMinecraftLightLevel(),

                output.getRed(),
                output.getGreen(),
                output.getBlue(),
                output.getWhite(),
                output.getAmber(),
                output.getDimmer(),
                output.getPan(),
                output.getTilt(),
                output.getBeamWidth(),
                output.getBeamLength(),
                output.getStrobe(),

                TARGET_MOB,
                display.getId(),

                map.getRedChannel(),
                map.getGreenChannel(),
                map.getBlueChannel(),
                map.getDimmerChannel(),
                map.getStrobeChannel(),
                display.getSkin(),
                display.getSkinDmxChannel(),

                display.isColorInterpolationEnabled(),
                display.getColorInterpolationTimeSeconds()
        );
    }

    /**
     * Returns true when this entry represents an ungrouped fixture.
     */
    public boolean isUngrouped() {
        return DEFAULT_GROUP.equalsIgnoreCase(
                groupName
        );
    }

    /**
     * Returns true when this row targets a moving DMX mob rather than a
     * block fixture.
     */
    public boolean isMobTarget() {
        return TARGET_MOB.equals(
                targetKind
        )
                && targetEntityId
                != NO_TARGET_ENTITY_ID;
    }

    /**
     * Returns true when at least one DMX parameter channel is assigned.
     */
    public boolean hasAssignedChannels() {
        return assignedChannels.length > 0;
    }

    /**
     * Returns the lowest assigned DMX channel.
     *
     * Returns 0 when no channels are assigned.
     */
    public int firstChannel() {
        if (assignedChannels.length == 0) {
            return FixtureParameterMap.UNASSIGNED;
        }

        return assignedChannels[0];
    }

    /**
     * Returns the highest assigned DMX channel.
     *
     * Returns 0 when no channels are assigned.
     */
    public int lastChannel() {
        if (assignedChannels.length == 0) {
            return FixtureParameterMap.UNASSIGNED;
        }

        return assignedChannels[
                assignedChannels.length - 1
        ];
    }

    /**
     * Returns the number of unique occupied DMX channels.
     */
    public int channelCount() {
        return assignedChannels.length;
    }

    /**
     * Returns a compact universe/patch label.
     *
     * Examples:
     *
     * U1 / -
     * U1 / 005
     * U1 / 005,006,007,010
     */
    public String getPatchLabel() {
        if (assignedChannels.length == 0) {
            return "U"
                    + universe
                    + " / -";
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                "U"
        );

        builder.append(
                universe
        );

        builder.append(
                " / "
        );

        for (
                int index = 0;
                index < assignedChannels.length;
                index++
        ) {
            if (index > 0) {
                builder.append(
                        ","
                );
            }

            builder.append(
                    formatChannel(
                            assignedChannels[index]
                    )
            );
        }

        return builder.toString();
    }

    /**
     * Returns a compact coordinate label.
     */
    public String getPositionLabel() {
        return position.getX()
                + ", "
                + position.getY()
                + ", "
                + position.getZ();
    }

    /**
     * Returns the RGB color with a fully opaque alpha channel.
     */
    public int getArgbColor() {
        return 0xFF000000
                | outputRgb;
    }

    /*
     * -----------------------------------------------------------------
     * Channel normalization
     * -----------------------------------------------------------------
     */

    private static int[] normalizeChannels(
            int[] channels
    ) {
        if (channels == null
                || channels.length == 0) {

            return new int[0];
        }

        return Arrays.stream(
                channels
        )
                .filter(
                        FixtureParameterMap::isAssigned
                )
                .distinct()
                .sorted()
                .toArray();
    }

    /*
     * -----------------------------------------------------------------
     * Helpers
     * -----------------------------------------------------------------
     */

    private static int clampDmx(
            int value
    ) {
        return clamp(
                value,
                FixtureOutput.MIN_VALUE,
                FixtureOutput.MAX_VALUE
        );
    }

    private static int cleanDmxChannel(
            int channel
    ) {
        if (channel
                == FixtureParameterMap.UNASSIGNED) {

            return FixtureParameterMap.UNASSIGNED;
        }

        if (!FixtureParameterMap.isAssigned(
                channel
        )) {
            return FixtureParameterMap.UNASSIGNED;
        }

        return channel;
    }

    private static String formatChannel(
            int channel
    ) {
        return String.format(
                "%03d",
                channel
        );
    }

    private static String cleanDisplayText(
            String input,
            String fallback
    ) {
        if (input == null) {
            return fallback;
        }

        String cleaned =
                input
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        return cleaned.isEmpty()
                ? fallback
                : cleaned;
    }

    private static String cleanIdentifier(
            String input,
            String fallback
    ) {
        if (input == null) {
            return fallback;
        }

        String cleaned =
                input
                        .trim()
                        .toLowerCase();

        return cleaned.isEmpty()
                ? fallback
                : cleaned;
    }

    private static int clamp(
            int value,
            int minimum,
            int maximum
    ) {
        return Math.max(
                minimum,
                Math.min(
                        maximum,
                        value
                )
        );
    }
}
