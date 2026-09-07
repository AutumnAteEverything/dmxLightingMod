package dmx.lighting;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Central registry of all fixture profiles supported by dmxLighting.
 *
 * New fixture personalities will be registered here later.
 */
public final class FixtureProfiles {

    private static final Map<String, FixtureProfile> PROFILES =
            new LinkedHashMap<>();

    public static final FixtureProfile RGBD =
            register(RgbdFixtureProfile.INSTANCE);

    private FixtureProfiles() {
    }

    /**
     * Registers one fixture profile.
     */
    private static FixtureProfile register(
            FixtureProfile profile
    ) {
        if (profile == null) {
            throw new IllegalArgumentException(
                    "Fixture profile cannot be null."
            );
        }

        String normalizedId = normalizeId(profile.getId());

        if (normalizedId.isBlank()) {
            throw new IllegalArgumentException(
                    "Fixture profile ID cannot be blank."
            );
        }

        if (PROFILES.containsKey(normalizedId)) {
            throw new IllegalStateException(
                    "Duplicate fixture profile ID: "
                            + normalizedId
            );
        }

        PROFILES.put(normalizedId, profile);

        return profile;
    }

    /**
     * Finds a fixture profile by its command or saved-data ID.
     *
     * @return the matching profile, or null when unsupported
     */
    public static FixtureProfile get(String id) {
        if (id == null) {
            return null;
        }

        return PROFILES.get(normalizeId(id));
    }

    /**
     * Returns the requested profile, falling back to RGBD when the
     * saved name is missing or unsupported.
     */
    public static FixtureProfile getOrDefault(String id) {
        FixtureProfile profile = get(id);

        return profile == null
                ? RGBD
                : profile;
    }

    /**
     * Returns all currently registered profiles.
     *
     * The returned collection cannot be modified.
     */
    public static Collection<FixtureProfile> getAll() {
        return Collections.unmodifiableCollection(
                PROFILES.values()
        );
    }

    /**
     * Returns true when a profile ID is registered.
     */
    public static boolean contains(String id) {
        return get(id) != null;
    }

    private static String normalizeId(String id) {
        return id
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}