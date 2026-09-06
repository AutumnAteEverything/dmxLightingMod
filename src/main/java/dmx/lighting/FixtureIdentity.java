package dmx.lighting;

import java.util.Locale;
import java.util.Objects;

/**
 * Stores the descriptive identity of a DMX fixture.
 *
 * This class contains information describing what a fixture is, rather
 * than where it is patched or what output it is currently producing.
 *
 * Identity data:
 *
 * - Fixture name
 * - Fixture profile
 * - Fixture group
 *
 * This is intentionally a plain Java data object. It does not know
 * anything about Minecraft block entities, networking, rendering, or
 * DMX universes.
 */
public final class FixtureIdentity {

    public static final int MAX_NAME_LENGTH = 64;

    /**
     * Modern default fixture profile.
     */
    public static final String DEFAULT_PROFILE =
            DmxFixtureProfileRegistry.DEFAULT_PROFILE_ID;

    /**
     * Legacy profile identifier used by older saved fixtures.
     */
    public static final String LEGACY_PROFILE =
            DmxFixtureProfileRegistry.LEGACY_RGBD_ID;

    private String fixtureName;
    private String fixtureProfile;
    private FixtureGroupName group;

    /**
     * Creates a fixture using default identity values.
     */
    public FixtureIdentity() {
        this(
                "",
                DEFAULT_PROFILE,
                FixtureGroupName.UNGROUPED
        );
    }

    /**
     * Creates a fixture identity with explicit values.
     */
    public FixtureIdentity(
            String fixtureName,
            String fixtureProfile,
            FixtureGroupName group
    ) {
        this.fixtureName =
                cleanFixtureName(
                        fixtureName
                );

        this.fixtureProfile =
                cleanFixtureProfile(
                        fixtureProfile
                );

        this.group =
                group == null
                        ? FixtureGroupName.UNGROUPED
                        : group;
    }

    /**
     * Returns the user-facing fixture name.
     */
    public String getFixtureName() {
        return fixtureName;
    }

    /**
     * Changes the user-facing fixture name.
     */
    public void setFixtureName(
            String fixtureName
    ) {
        this.fixtureName =
                cleanFixtureName(
                        fixtureName
                );
    }

    /**
     * Returns the normalized modern fixture profile identifier.
     *
     * Examples:
     *
     * rgb_par
     * rgbw_par
     * static_spot
     * moving_head_spot
     */
    public String getFixtureProfile() {
        return fixtureProfile;
    }

    /**
     * Changes the fixture profile identifier.
     *
     * Legacy "rgbd" values are converted to "rgb_par".
     * Unknown profile IDs fall back to the default registered profile.
     */
    public void setFixtureProfile(
            String fixtureProfile
    ) {
        this.fixtureProfile =
                cleanFixtureProfile(
                        fixtureProfile
                );
    }

    /**
     * Returns the fixture's named group.
     */
    public FixtureGroupName getGroup() {
        return group;
    }

    /**
     * Assigns a named group to the fixture.
     *
     * Passing null makes the fixture ungrouped.
     */
    public void setGroup(
            FixtureGroupName group
    ) {
        this.group =
                group == null
                        ? FixtureGroupName.UNGROUPED
                        : group;
    }

    /**
     * Convenience overload for assigning a group from GUI or command
     * text.
     */
    public void setGroup(
            String groupName
    ) {
        setGroup(
                FixtureGroupName.of(
                        groupName
                )
        );
    }

    /**
     * Returns the normalized internal group lookup key.
     */
    public String getGroupKey() {
        return group.key();
    }

    /**
     * Returns the user-facing group name.
     */
    public String getGroupDisplayName() {
        return group.displayName();
    }

    /**
     * Returns true when the fixture is not assigned to a group.
     */
    public boolean isUngrouped() {
        return group.isUngrouped();
    }

    /**
     * Creates an independent copy of this identity.
     */
    public FixtureIdentity copy() {
        return new FixtureIdentity(
                fixtureName,
                fixtureProfile,
                group
        );
    }

    /**
     * Restores all identity values at once.
     */
    public void set(
            String fixtureName,
            String fixtureProfile,
            FixtureGroupName group
    ) {
        setFixtureName(
                fixtureName
        );

        setFixtureProfile(
                fixtureProfile
        );

        setGroup(
                group
        );
    }

    @Override
    public boolean equals(
            Object object
    ) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof FixtureIdentity other)) {
            return false;
        }

        return fixtureName.equals(
                other.fixtureName
        )
                && fixtureProfile.equals(
                other.fixtureProfile
        )
                && group.equals(
                other.group
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fixtureName,
                fixtureProfile,
                group
        );
    }

    @Override
    public String toString() {
        return "FixtureIdentity{"
                + "name='"
                + fixtureName
                + '\''
                + ", profile='"
                + fixtureProfile
                + '\''
                + ", group='"
                + group.getGuiLabel()
                + '\''
                + '}';
    }

    /**
     * Cleans and limits a fixture name entered by a user.
     */
    private static String cleanFixtureName(
            String input
    ) {
        if (input == null) {
            return "";
        }

        String cleaned =
                input
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        if (cleaned.length() > MAX_NAME_LENGTH) {
            cleaned =
                    cleaned.substring(
                            0,
                            MAX_NAME_LENGTH
                    )
                    .trim();
        }

        return cleaned;
    }

    /**
     * Cleans and resolves a fixture profile identifier.
     *
     * Behavior:
     *
     * - Null or blank values become rgb_par.
     * - Spaces and hyphens become underscores.
     * - Legacy rgbd becomes rgb_par.
     * - Unknown profiles fall back to rgb_par.
     */
    private static String cleanFixtureProfile(
            String input
    ) {
        if (input == null) {
            return DEFAULT_PROFILE;
        }

        String cleaned =
                input
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replace(
                                ' ',
                                '_'
                        )
                        .replace(
                                '-',
                                '_'
                        );

        if (cleaned.isBlank()) {
            return DEFAULT_PROFILE;
        }

        return DmxFixtureProfileRegistry.resolveProfileId(
                cleaned
        );
    }
}