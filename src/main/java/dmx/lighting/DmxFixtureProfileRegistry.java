package dmx.lighting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Central registry for supported DMX fixture visual profiles.
 *
 * Fixture profiles are now primarily visual model selections.
 *
 * Both built-in fixture types use the same parameter-centric control
 * architecture:
 *
 * - RGB / White / Amber
 * - Dimmer
 * - Pan / Tilt offsets
 * - Beam Width
 * - Beam Length
 * - Strobe
 *
 * The selected profile determines which physical fixture model is
 * rendered in-game.
 *
 * Built-in visual choices:
 *
 * - PAR
 * - Spotlight
 *
 * A LinkedHashMap is used so profiles retain registration order in
 * user interfaces.
 */
public final class DmxFixtureProfileRegistry {

    /*
     * -----------------------------------------------------------------
     * Current visual profile IDs
     * -----------------------------------------------------------------
     */

    /**
     * Default PAR visual profile.
     */
    public static final String DEFAULT_PROFILE_ID =
            "rgb_par";

    /**
     * Traditional long-barrel spotlight visual profile.
     */
    public static final String SPOTLIGHT_PROFILE_ID =
            "static_spot";

    /*
     * -----------------------------------------------------------------
     * Legacy compatibility IDs
     * -----------------------------------------------------------------
     */

    /**
     * Legacy RGBD fixture ID.
     */
    public static final String LEGACY_RGBD_ID =
            "rgbd";

    /**
     * Previous RGBW PAR profile.
     *
     * This now resolves to the standard PAR visual model.
     */
    public static final String LEGACY_RGBW_PAR_ID =
            "rgbw_par";

    /**
     * Previous moving-head spotlight profile.
     *
     * This now resolves to the standard Spotlight visual model.
     */
    public static final String LEGACY_MOVING_HEAD_SPOT_ID =
            "moving_head_spot";

    private static final Map<String, DmxFixtureProfile> PROFILES =
            new LinkedHashMap<>();

    private static boolean initialized;

    private DmxFixtureProfileRegistry() {
        /*
         * Utility class.
         */
    }

    /*
     * -----------------------------------------------------------------
     * Initialization
     * -----------------------------------------------------------------
     */

    /**
     * Registers the two built-in visual fixture profiles.
     *
     * This method is safe to call more than once.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        /*
         * PAR
         */
        register(
                RgbParProfile.INSTANCE
        );

        /*
         * Traditional spotlight
         */
        register(
                StaticSpotProfile.INSTANCE
        );

        register(
                DmxPixelProfile.INSTANCE
        );

        register(
                DmxParrotProfile.INSTANCE
        );

        register(
                DmxEndermanProfile.INSTANCE
        );

        register(
                DmxWardenProfile.INSTANCE
        );

        register(
                DmxNautilusProfile.INSTANCE
        );

        initialized =
                true;
    }

    /*
     * -----------------------------------------------------------------
     * Registration
     * -----------------------------------------------------------------
     */

    /**
     * Registers one fixture profile.
     *
     * Profile IDs must be unique.
     */
    public static synchronized void register(
            DmxFixtureProfile profile
    ) {
        Objects.requireNonNull(
                profile,
                "profile cannot be null"
        );

        String profileId =
                normalizeId(
                        profile.id()
                );

        DmxFixtureProfile existing =
                PROFILES.get(
                        profileId
                );

        if (existing != null) {
            if (existing == profile) {
                return;
            }

            throw new IllegalArgumentException(
                    "A fixture profile is already registered with ID: "
                            + profileId
            );
        }

        PROFILES.put(
                profileId,
                profile
        );
    }

    /*
     * -----------------------------------------------------------------
     * Lookup
     * -----------------------------------------------------------------
     */

    /**
     * Returns a profile by ID.
     *
     * Older profile IDs are translated into one of the two current
     * visual models.
     */
    public static synchronized Optional<DmxFixtureProfile> get(
            String profileId
    ) {
        initialize();

        if (profileId == null) {
            return Optional.empty();
        }

        String resolvedId =
                resolveCompatibilityId(
                        profileId
                );

        return Optional.ofNullable(
                PROFILES.get(
                        resolvedId
                )
        );
    }

    /**
     * Returns a profile by ID or the PAR fallback.
     */
    public static synchronized DmxFixtureProfile getOrDefault(
            String profileId
    ) {
        initialize();

        return get(
                profileId
        )
                .orElse(
                        RgbParProfile.INSTANCE
                );
    }

    /**
     * Returns true when an ID resolves to a supported visual profile.
     */
    public static synchronized boolean contains(
            String profileId
    ) {
        return get(
                profileId
        )
                .isPresent();
    }

    /**
     * Returns the modern visual profile ID represented by the supplied
     * saved or user-entered ID.
     */
    public static synchronized String resolveProfileId(
            String profileId
    ) {
        return getOrDefault(
                profileId
        )
                .id();
    }

    /*
     * -----------------------------------------------------------------
     * Lists
     * -----------------------------------------------------------------
     */

    /**
     * Returns the two current visual profiles in UI order.
     */
    public static synchronized List<DmxFixtureProfile> getAll() {
        initialize();

        return Collections.unmodifiableList(
                new ArrayList<>(
                        PROFILES.values()
                )
        );
    }

    /**
     * Returns all currently registered profile IDs.
     */
    public static synchronized List<String> getAllIds() {
        initialize();

        return Collections.unmodifiableList(
                new ArrayList<>(
                        PROFILES.keySet()
                )
        );
    }

    /**
     * Returns all currently registered display names.
     */
    public static synchronized List<String> getAllDisplayNames() {
        initialize();

        List<String> names =
                new ArrayList<>();

        for (
                DmxFixtureProfile profile :
                PROFILES.values()
        ) {
            names.add(
                    profile.displayName()
            );
        }

        return Collections.unmodifiableList(
                names
        );
    }

    /**
     * Returns the number of current visual profiles.
     */
    public static synchronized int size() {
        initialize();

        return PROFILES.size();
    }

    /**
     * Returns a read-only collection snapshot.
     */
    public static synchronized Collection<DmxFixtureProfile> values() {
        initialize();

        return Collections.unmodifiableCollection(
                new ArrayList<>(
                        PROFILES.values()
                )
        );
    }

    /*
     * -----------------------------------------------------------------
     * Statistics
     * -----------------------------------------------------------------
     */

    /**
     * Counts loaded fixtures by resolved visual profile ID.
     *
     * Legacy profile IDs are counted under their current visual model.
     */
    public static Map<String, Integer> countProfiles(
            Iterable<DmxFixtureBlockEntity> fixtures
    ) {
        Map<String, Integer> counts =
                new HashMap<>();

        if (fixtures == null) {
            return counts;
        }

        for (
                DmxFixtureBlockEntity fixture :
                fixtures
        ) {
            if (fixture == null) {
                continue;
            }

            String profileId =
                    resolveProfileId(
                            fixture.getFixtureType()
                    );

            counts.merge(
                    profileId,
                    1,
                    Integer::sum
            );
        }

        return counts;
    }

    /*
     * -----------------------------------------------------------------
     * Compatibility
     * -----------------------------------------------------------------
     */

    /**
     * Maps old profile IDs onto the two current visual fixture models.
     *
     * PAR family:
     *
     * rgbd
     * rgb_par
     * rgbw_par
     *
     * Spotlight family:
     *
     * static_spot
     * moving_head_spot
     */
    private static String resolveCompatibilityId(
            String profileId
    ) {
        String normalizedId =
                normalizeId(
                        profileId
                );

        return switch (
                normalizedId
        ) {
            case LEGACY_RGBD_ID,
                    LEGACY_RGBW_PAR_ID ->
                    DEFAULT_PROFILE_ID;

            case LEGACY_MOVING_HEAD_SPOT_ID ->
                    SPOTLIGHT_PROFILE_ID;

            default ->
                    normalizedId;
        };
    }

    /*
     * -----------------------------------------------------------------
     * ID normalization
     * -----------------------------------------------------------------
     */

    /**
     * Normalizes profile IDs.
     */
    private static String normalizeId(
            String profileId
    ) {
        Objects.requireNonNull(
                profileId,
                "profileId cannot be null"
        );

        return profileId
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
    }
}
