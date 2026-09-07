package dmx.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks loaded DMX-capable mobs.
 *
 * This intentionally lives beside, rather than inside, the block
 * fixture registry so moving entities can later gain UUID-based
 * targeting without disturbing block fixtures.
 */
public final class DmxMobFixtureRegistry {

    private static final Map<
            ResourceKey<Level>,
            Map<UUID, DmxParrotEntity>
    > PARROTS_BY_DIMENSION = new HashMap<>();

    private static final Map<
            ResourceKey<Level>,
            Map<UUID, DmxEndermanEntity>
    > ENDERMEN_BY_DIMENSION = new HashMap<>();

    private DmxMobFixtureRegistry() {
        // Utility class.
    }

    public static synchronized void register(
            DmxParrotEntity parrot
    ) {
        if (!isUsableServerParrot(
                parrot
        )) {
            return;
        }

        Level level =
                parrot.level();

        PARROTS_BY_DIMENSION
                .computeIfAbsent(
                        level.dimension(),
                        ignored -> new HashMap<>()
                )
                .put(
                        parrot.getUUID(),
                        parrot
                );
    }

    public static synchronized void register(
            DmxEndermanEntity enderman
    ) {
        if (!isUsableServerEnderman(
                enderman
        )) {
            return;
        }

        Level level =
                enderman.level();

        ENDERMEN_BY_DIMENSION
                .computeIfAbsent(
                        level.dimension(),
                        ignored -> new HashMap<>()
                )
                .put(
                        enderman.getUUID(),
                        enderman
                );
    }

    public static synchronized void unregister(
            DmxParrotEntity parrot
    ) {
        if (parrot == null) {
            return;
        }

        Level level =
                parrot.level();

        if (level == null) {
            return;
        }

        Map<UUID, DmxParrotEntity> dimensionParrots =
                PARROTS_BY_DIMENSION.get(
                        level.dimension()
                );

        if (dimensionParrots == null) {
            return;
        }

        dimensionParrots.remove(
                parrot.getUUID()
        );

        if (dimensionParrots.isEmpty()) {
            PARROTS_BY_DIMENSION.remove(
                    level.dimension()
            );
        }
    }

    public static synchronized void unregister(
            DmxEndermanEntity enderman
    ) {
        if (enderman == null) {
            return;
        }

        Level level =
                enderman.level();

        if (level == null) {
            return;
        }

        Map<UUID, DmxEndermanEntity> dimensionEndermen =
                ENDERMEN_BY_DIMENSION.get(
                        level.dimension()
                );

        if (dimensionEndermen == null) {
            return;
        }

        dimensionEndermen.remove(
                enderman.getUUID()
        );

        if (dimensionEndermen.isEmpty()) {
            ENDERMEN_BY_DIMENSION.remove(
                    level.dimension()
            );
        }
    }

    public static synchronized List<DmxParrotEntity>
    getParrotsInDimension(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return List.of();
        }

        Map<UUID, DmxParrotEntity> dimensionParrots =
                PARROTS_BY_DIMENSION.get(
                        dimension
                );

        if (dimensionParrots == null) {
            return List.of();
        }

        removeInvalidParrots(
                dimension,
                dimensionParrots
        );

        return List.copyOf(
                dimensionParrots.values()
        );
    }

    public static synchronized List<DmxEndermanEntity>
    getEndermenInDimension(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return List.of();
        }

        Map<UUID, DmxEndermanEntity> dimensionEndermen =
                ENDERMEN_BY_DIMENSION.get(
                        dimension
                );

        if (dimensionEndermen == null) {
            return List.of();
        }

        removeInvalidEndermen(
                dimension,
                dimensionEndermen
        );

        return List.copyOf(
                dimensionEndermen.values()
        );
    }

    public static synchronized List<DmxParrotEntity>
    getParrotsInGroup(
            ResourceKey<Level> dimension,
            FixtureGroupName group
    ) {
        if (dimension == null
                || group == null
                || group.isUngrouped()) {

            return List.of();
        }

        String targetKey =
                group.key();

        List<DmxParrotEntity> matches =
                new ArrayList<>();

        for (DmxParrotEntity parrot :
                getParrotsInDimension(
                        dimension
                )) {

            if (targetKey.equals(
                    parrot.getGroupKey()
            )) {
                matches.add(
                        parrot
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static synchronized List<DmxEndermanEntity>
    getEndermenInGroup(
            ResourceKey<Level> dimension,
            FixtureGroupName group
    ) {
        if (dimension == null
                || group == null
                || group.isUngrouped()) {

            return List.of();
        }

        String targetKey =
                group.key();

        List<DmxEndermanEntity> matches =
                new ArrayList<>();

        for (DmxEndermanEntity enderman :
                getEndermenInDimension(
                        dimension
                )) {

            if (targetKey.equals(
                    enderman.getGroupKey()
            )) {
                matches.add(
                        enderman
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static synchronized List<DmxParrotEntity>
    getParrotsInUniverse(
            ResourceKey<Level> dimension,
            int universe
    ) {
        int safeUniverse =
                Math.max(
                        FixturePatch.MIN_UNIVERSE,
                        Math.min(
                                FixturePatch.MAX_UNIVERSE,
                                universe
                        )
                );

        List<DmxParrotEntity> matches =
                new ArrayList<>();

        for (DmxParrotEntity parrot :
                getParrotsInDimension(
                        dimension
                )) {

            if (parrot.getUniverse()
                    == safeUniverse) {

                matches.add(
                        parrot
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static synchronized List<DmxEndermanEntity>
    getEndermenInUniverse(
            ResourceKey<Level> dimension,
            int universe
    ) {
        int safeUniverse =
                Math.max(
                        FixturePatch.MIN_UNIVERSE,
                        Math.min(
                                FixturePatch.MAX_UNIVERSE,
                                universe
                        )
                );

        List<DmxEndermanEntity> matches =
                new ArrayList<>();

        for (DmxEndermanEntity enderman :
                getEndermenInDimension(
                        dimension
                )) {

            if (enderman.getUniverse()
                    == safeUniverse) {

                matches.add(
                        enderman
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static void refreshParrotsInUniverse(
            int universe
    ) {
        for (DmxParrotEntity parrot :
                getAllParrots()) {

            if (parrot.getUniverse()
                    == universe) {

                parrot.refreshFromDmx();
            }
        }
    }

    public static void refreshMobFixturesInUniverse(
            int universe
    ) {
        refreshParrotsInUniverse(
                universe
        );

        for (DmxEndermanEntity enderman :
                getAllEndermen()) {

            if (enderman.getUniverse()
                    == universe) {

                enderman.refreshFromDmx();
            }
        }
    }

    public static synchronized DmxParrotEntity getParrotAt(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (dimension == null
                || position == null) {

            return null;
        }

        for (DmxParrotEntity parrot :
                getParrotsInDimension(
                        dimension
                )) {

            if (position.equals(
                    parrot.blockPosition()
            )) {
                return parrot;
            }
        }

        return null;
    }

    public static synchronized DmxEndermanEntity getEndermanAt(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (dimension == null
                || position == null) {

            return null;
        }

        for (DmxEndermanEntity enderman :
                getEndermenInDimension(
                        dimension
                )) {

            if (position.equals(
                    enderman.blockPosition()
            )) {
                return enderman;
            }
        }

        return null;
    }

    public static synchronized DmxParrotEntity getParrotByEntityId(
            ResourceKey<Level> dimension,
            int entityId
    ) {
        if (dimension == null
                || entityId
                == FixtureBrowserEntry.NO_TARGET_ENTITY_ID) {

            return null;
        }

        for (DmxParrotEntity parrot :
                getParrotsInDimension(
                        dimension
                )) {

            if (parrot.getId()
                    == entityId) {

                return parrot;
            }
        }

        return null;
    }

    public static synchronized DmxEndermanEntity getEndermanByEntityId(
            ResourceKey<Level> dimension,
            int entityId
    ) {
        if (dimension == null
                || entityId
                == FixtureBrowserEntry.NO_TARGET_ENTITY_ID) {

            return null;
        }

        for (DmxEndermanEntity enderman :
                getEndermenInDimension(
                        dimension
                )) {

            if (enderman.getId()
                    == entityId) {

                return enderman;
            }
        }

        return null;
    }

    public static synchronized List<DmxParrotEntity>
    getAllParrots() {
        List<DmxParrotEntity> parrots =
                new ArrayList<>();

        List<ResourceKey<Level>> dimensions =
                new ArrayList<>(
                        PARROTS_BY_DIMENSION.keySet()
                );

        for (ResourceKey<Level> dimension :
                dimensions) {

            parrots.addAll(
                    getParrotsInDimension(
                            dimension
                    )
            );
        }

        return List.copyOf(
                parrots
        );
    }

    public static synchronized List<DmxEndermanEntity>
    getAllEndermen() {
        List<DmxEndermanEntity> endermen =
                new ArrayList<>();

        List<ResourceKey<Level>> dimensions =
                new ArrayList<>(
                        ENDERMEN_BY_DIMENSION.keySet()
                );

        for (ResourceKey<Level> dimension :
                dimensions) {

            endermen.addAll(
                    getEndermenInDimension(
                            dimension
                    )
            );
        }

        return List.copyOf(
                endermen
        );
    }

    public static synchronized void clearDimension(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return;
        }

        PARROTS_BY_DIMENSION.remove(
                dimension
        );

        ENDERMEN_BY_DIMENSION.remove(
                dimension
        );
    }

    public static synchronized void clearAll() {
        PARROTS_BY_DIMENSION.clear();
        ENDERMEN_BY_DIMENSION.clear();
    }

    private static void removeInvalidParrots(
            ResourceKey<Level> dimension,
            Map<UUID, DmxParrotEntity> parrots
    ) {
        Collection<UUID> ids =
                new ArrayList<>(
                        parrots.keySet()
                );

        for (UUID id :
                ids) {

            DmxParrotEntity parrot =
                    parrots.get(
                            id
                    );

            if (!isUsableServerParrot(
                    parrot
            )) {
                parrots.remove(
                        id
                );
            }
        }

        if (parrots.isEmpty()) {
            PARROTS_BY_DIMENSION.remove(
                    dimension
            );
        }
    }

    private static void removeInvalidEndermen(
            ResourceKey<Level> dimension,
            Map<UUID, DmxEndermanEntity> endermen
    ) {
        Collection<UUID> ids =
                new ArrayList<>(
                        endermen.keySet()
                );

        for (UUID id :
                ids) {

            DmxEndermanEntity enderman =
                    endermen.get(
                            id
                    );

            if (!isUsableServerEnderman(
                    enderman
            )) {
                endermen.remove(
                        id
                );
            }
        }

        if (endermen.isEmpty()) {
            ENDERMEN_BY_DIMENSION.remove(
                    dimension
            );
        }
    }

    private static boolean isUsableServerParrot(
            DmxParrotEntity parrot
    ) {
        if (parrot == null
                || parrot.isRemoved()) {

            return false;
        }

        Level level =
                parrot.level();

        return level != null
                && !level.isClientSide();
    }

    private static boolean isUsableServerEnderman(
            DmxEndermanEntity enderman
    ) {
        if (enderman == null
                || enderman.isRemoved()) {

            return false;
        }

        Level level =
                enderman.level();

        return level != null
                && !level.isClientSide();
    }
}
