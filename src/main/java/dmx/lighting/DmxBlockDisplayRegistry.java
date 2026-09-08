package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tracks loaded DMX Block Display entities. */
public final class DmxBlockDisplayRegistry {

    private static final Map<
            ResourceKey<Level>,
            Map<UUID, DmxBlockDisplayEntity>
    > DISPLAYS_BY_DIMENSION = new HashMap<>();

    private DmxBlockDisplayRegistry() {
    }

    public static synchronized void register(DmxBlockDisplayEntity display) {
        if (!isUsable(display)) {
            return;
        }

        DISPLAYS_BY_DIMENSION
                .computeIfAbsent(
                        display.level().dimension(),
                        ignored -> new HashMap<>()
                )
                .put(display.getUUID(), display);
    }

    public static synchronized void unregister(DmxBlockDisplayEntity display) {
        if (display == null || display.level() == null) {
            return;
        }

        ResourceKey<Level> dimension = display.level().dimension();
        Map<UUID, DmxBlockDisplayEntity> displays =
                DISPLAYS_BY_DIMENSION.get(dimension);

        if (displays == null) {
            return;
        }

        displays.remove(display.getUUID());
        if (displays.isEmpty()) {
            DISPLAYS_BY_DIMENSION.remove(dimension);
        }
    }

    public static synchronized List<DmxBlockDisplayEntity>
    getDisplaysInDimension(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return List.of();
        }

        Map<UUID, DmxBlockDisplayEntity> displays =
                DISPLAYS_BY_DIMENSION.get(dimension);
        if (displays == null) {
            return List.of();
        }

        displays.values().removeIf(display -> !isUsable(display));
        if (displays.isEmpty()) {
            DISPLAYS_BY_DIMENSION.remove(dimension);
            return List.of();
        }

        return List.copyOf(displays.values());
    }

    public static synchronized List<DmxBlockDisplayEntity>
    getDisplaysInGroup(
            ResourceKey<Level> dimension,
            FixtureGroupName group
    ) {
        if (group == null || group.isUngrouped()) {
            return List.of();
        }

        List<DmxBlockDisplayEntity> matches = new ArrayList<>();
        for (DmxBlockDisplayEntity display :
                getDisplaysInDimension(dimension)) {
            if (group.key().equals(display.getGroupKey())) {
                matches.add(display);
            }
        }
        return List.copyOf(matches);
    }

    public static synchronized List<DmxBlockDisplayEntity>
    getDisplaysInUniverse(ResourceKey<Level> dimension, int universe) {
        int safeUniverse = Math.clamp(
                universe,
                FixturePatch.MIN_UNIVERSE,
                FixturePatch.MAX_UNIVERSE
        );
        List<DmxBlockDisplayEntity> matches = new ArrayList<>();
        for (DmxBlockDisplayEntity display :
                getDisplaysInDimension(dimension)) {
            if (display.getUniverse() == safeUniverse) {
                matches.add(display);
            }
        }
        return List.copyOf(matches);
    }

    public static synchronized DmxBlockDisplayEntity getByEntityId(
            ResourceKey<Level> dimension,
            int entityId
    ) {
        if (entityId == FixtureBrowserEntry.NO_TARGET_ENTITY_ID) {
            return null;
        }

        for (DmxBlockDisplayEntity display :
                getDisplaysInDimension(dimension)) {
            if (display.getId() == entityId) {
                return display;
            }
        }
        return null;
    }

    public static synchronized DmxBlockDisplayEntity getAt(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (position == null) {
            return null;
        }

        for (DmxBlockDisplayEntity display :
                getDisplaysInDimension(dimension)) {
            if (position.equals(display.blockPosition())) {
                return display;
            }
        }
        return null;
    }

    public static synchronized List<DmxBlockDisplayEntity> getAll() {
        List<DmxBlockDisplayEntity> displays = new ArrayList<>();
        for (ResourceKey<Level> dimension :
                new ArrayList<>(DISPLAYS_BY_DIMENSION.keySet())) {
            displays.addAll(getDisplaysInDimension(dimension));
        }
        return List.copyOf(displays);
    }

    public static void refreshInUniverse(int universe) {
        for (DmxBlockDisplayEntity display : getAll()) {
            if (display.getUniverse() == universe) {
                display.refreshFromDmx();
            }
        }
    }

    public static void refreshInDimension(ResourceKey<Level> dimension) {
        for (DmxBlockDisplayEntity display :
                getDisplaysInDimension(dimension)) {

            display.refreshFromDmx();
        }
    }

    private static boolean isUsable(DmxBlockDisplayEntity display) {
        return display != null
                && !display.isRemoved()
                && display.level() != null
                && !display.level().isClientSide();
    }
}
