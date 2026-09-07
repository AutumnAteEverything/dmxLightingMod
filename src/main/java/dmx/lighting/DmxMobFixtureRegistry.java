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

    private static final Map<
            ResourceKey<Level>,
            Map<UUID, DmxWardenEntity>
    > WARDENS_BY_DIMENSION = new HashMap<>();

    private static final Map<
            ResourceKey<Level>,
            Map<UUID, DmxNautilusEntity>
    > NAUTILUSES_BY_DIMENSION = new HashMap<>();

    private static final Map<
            ResourceKey<Level>,
            Map<UUID, DmxCreakingEntity>
    > CREAKINGS_BY_DIMENSION = new HashMap<>();

    private static final Map<
            ResourceKey<Level>,
            Map<UUID, DmxAxolotlEntity>
    > AXOLOTLS_BY_DIMENSION = new HashMap<>();

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

    public static synchronized void register(
            DmxWardenEntity warden
    ) {
        if (!isUsableServerWarden(
                warden
        )) {
            return;
        }

        Level level =
                warden.level();

        WARDENS_BY_DIMENSION
                .computeIfAbsent(
                        level.dimension(),
                        ignored -> new HashMap<>()
                )
                .put(
                        warden.getUUID(),
                        warden
                );
    }

    public static synchronized void register(
            DmxNautilusEntity nautilus
    ) {
        if (!isUsableServerNautilus(
                nautilus
        )) {
            return;
        }

        Level level =
                nautilus.level();

        NAUTILUSES_BY_DIMENSION
                .computeIfAbsent(
                        level.dimension(),
                        ignored -> new HashMap<>()
                )
                .put(
                        nautilus.getUUID(),
                        nautilus
                );
    }

    public static synchronized void register(DmxCreakingEntity creaking) {
        if (!isUsableServerCreaking(creaking)) {
            return;
        }

        CREAKINGS_BY_DIMENSION
                .computeIfAbsent(creaking.level().dimension(), ignored -> new HashMap<>())
                .put(creaking.getUUID(), creaking);
    }

    public static synchronized void register(DmxAxolotlEntity axolotl) {
        if (!isUsableServerAxolotl(axolotl)) {
            return;
        }

        AXOLOTLS_BY_DIMENSION
                .computeIfAbsent(axolotl.level().dimension(), ignored -> new HashMap<>())
                .put(axolotl.getUUID(), axolotl);
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

    public static synchronized void unregister(
            DmxWardenEntity warden
    ) {
        if (warden == null) {
            return;
        }

        Level level =
                warden.level();

        if (level == null) {
            return;
        }

        Map<UUID, DmxWardenEntity> dimensionWardens =
                WARDENS_BY_DIMENSION.get(
                        level.dimension()
                );

        if (dimensionWardens == null) {
            return;
        }

        dimensionWardens.remove(
                warden.getUUID()
        );

        if (dimensionWardens.isEmpty()) {
            WARDENS_BY_DIMENSION.remove(
                    level.dimension()
            );
        }
    }

    public static synchronized void unregister(
            DmxNautilusEntity nautilus
    ) {
        if (nautilus == null) {
            return;
        }

        Level level =
                nautilus.level();

        if (level == null) {
            return;
        }

        Map<UUID, DmxNautilusEntity> dimensionNautiluses =
                NAUTILUSES_BY_DIMENSION.get(
                        level.dimension()
                );

        if (dimensionNautiluses == null) {
            return;
        }

        dimensionNautiluses.remove(
                nautilus.getUUID()
        );

        if (dimensionNautiluses.isEmpty()) {
            NAUTILUSES_BY_DIMENSION.remove(
                    level.dimension()
            );
        }
    }

    public static synchronized void unregister(DmxCreakingEntity creaking) {
        if (creaking != null) {
            unregisterEntity(
                    creaking.level(),
                    creaking.getUUID(),
                    CREAKINGS_BY_DIMENSION
            );
        }
    }

    public static synchronized void unregister(DmxAxolotlEntity axolotl) {
        if (axolotl != null) {
            unregisterEntity(
                    axolotl.level(),
                    axolotl.getUUID(),
                    AXOLOTLS_BY_DIMENSION
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

    public static synchronized List<DmxWardenEntity>
    getWardensInDimension(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return List.of();
        }

        Map<UUID, DmxWardenEntity> dimensionWardens =
                WARDENS_BY_DIMENSION.get(
                        dimension
                );

        if (dimensionWardens == null) {
            return List.of();
        }

        removeInvalidWardens(
                dimension,
                dimensionWardens
        );

        return List.copyOf(
                dimensionWardens.values()
        );
    }

    public static synchronized List<DmxNautilusEntity>
    getNautilusesInDimension(
            ResourceKey<Level> dimension
    ) {
        if (dimension == null) {
            return List.of();
        }

        Map<UUID, DmxNautilusEntity> dimensionNautiluses =
                NAUTILUSES_BY_DIMENSION.get(
                        dimension
                );

        if (dimensionNautiluses == null) {
            return List.of();
        }

        removeInvalidNautiluses(
                dimension,
                dimensionNautiluses
        );

        return List.copyOf(
                dimensionNautiluses.values()
        );
    }

    public static synchronized List<DmxCreakingEntity> getCreakingsInDimension(
            ResourceKey<Level> dimension
    ) {
        Map<UUID, DmxCreakingEntity> entities =
                dimension == null ? null : CREAKINGS_BY_DIMENSION.get(dimension);

        if (entities == null) {
            return List.of();
        }

        entities.values().removeIf(entity -> !isUsableServerCreaking(entity));
        if (entities.isEmpty()) {
            CREAKINGS_BY_DIMENSION.remove(dimension);
            return List.of();
        }
        return List.copyOf(entities.values());
    }

    public static synchronized List<DmxAxolotlEntity> getAxolotlsInDimension(
            ResourceKey<Level> dimension
    ) {
        Map<UUID, DmxAxolotlEntity> entities =
                dimension == null ? null : AXOLOTLS_BY_DIMENSION.get(dimension);

        if (entities == null) {
            return List.of();
        }

        entities.values().removeIf(entity -> !isUsableServerAxolotl(entity));
        if (entities.isEmpty()) {
            AXOLOTLS_BY_DIMENSION.remove(dimension);
            return List.of();
        }
        return List.copyOf(entities.values());
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

    public static synchronized List<DmxWardenEntity>
    getWardensInGroup(
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

        List<DmxWardenEntity> matches =
                new ArrayList<>();

        for (DmxWardenEntity warden :
                getWardensInDimension(
                        dimension
                )) {

            if (targetKey.equals(
                    warden.getGroupKey()
            )) {
                matches.add(
                        warden
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static synchronized List<DmxNautilusEntity>
    getNautilusesInGroup(
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

        List<DmxNautilusEntity> matches =
                new ArrayList<>();

        for (DmxNautilusEntity nautilus :
                getNautilusesInDimension(
                        dimension
                )) {

            if (targetKey.equals(
                    nautilus.getGroupKey()
            )) {
                matches.add(
                        nautilus
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static synchronized List<DmxCreakingEntity> getCreakingsInGroup(
            ResourceKey<Level> dimension,
            FixtureGroupName group
    ) {
        if (group == null || group.isUngrouped()) {
            return List.of();
        }

        String key = group.key();
        return getCreakingsInDimension(dimension).stream()
                .filter(entity -> key.equals(entity.getGroupKey()))
                .toList();
    }

    public static synchronized List<DmxAxolotlEntity> getAxolotlsInGroup(
            ResourceKey<Level> dimension,
            FixtureGroupName group
    ) {
        if (group == null || group.isUngrouped()) {
            return List.of();
        }

        String key = group.key();
        return getAxolotlsInDimension(dimension).stream()
                .filter(entity -> key.equals(entity.getGroupKey()))
                .toList();
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

    public static synchronized List<DmxWardenEntity>
    getWardensInUniverse(
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

        List<DmxWardenEntity> matches =
                new ArrayList<>();

        for (DmxWardenEntity warden :
                getWardensInDimension(
                        dimension
                )) {

            if (warden.getUniverse()
                    == safeUniverse) {

                matches.add(
                        warden
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static synchronized List<DmxNautilusEntity>
    getNautilusesInUniverse(
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

        List<DmxNautilusEntity> matches =
                new ArrayList<>();

        for (DmxNautilusEntity nautilus :
                getNautilusesInDimension(
                        dimension
                )) {

            if (nautilus.getUniverse()
                    == safeUniverse) {

                matches.add(
                        nautilus
                );
            }
        }

        return List.copyOf(
                matches
        );
    }

    public static synchronized List<DmxCreakingEntity> getCreakingsInUniverse(
            ResourceKey<Level> dimension,
            int universe
    ) {
        int safeUniverse = Math.clamp(
                universe,
                FixturePatch.MIN_UNIVERSE,
                FixturePatch.MAX_UNIVERSE
        );
        return getCreakingsInDimension(dimension).stream()
                .filter(entity -> entity.getUniverse() == safeUniverse)
                .toList();
    }

    public static synchronized List<DmxAxolotlEntity> getAxolotlsInUniverse(
            ResourceKey<Level> dimension,
            int universe
    ) {
        int safeUniverse = Math.clamp(
                universe,
                FixturePatch.MIN_UNIVERSE,
                FixturePatch.MAX_UNIVERSE
        );
        return getAxolotlsInDimension(dimension).stream()
                .filter(entity -> entity.getUniverse() == safeUniverse)
                .toList();
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

        for (DmxWardenEntity warden :
                getAllWardens()) {

            if (warden.getUniverse()
                    == universe) {

                warden.refreshFromDmx();
            }
        }

        for (DmxNautilusEntity nautilus :
                getAllNautiluses()) {

            if (nautilus.getUniverse()
                    == universe) {

                nautilus.refreshFromDmx();
            }
        }

        for (DmxCreakingEntity creaking : getAllCreakings()) {
            if (creaking.getUniverse() == universe) {
                creaking.refreshFromDmx();
            }
        }

        for (DmxAxolotlEntity axolotl : getAllAxolotls()) {
            if (axolotl.getUniverse() == universe) {
                axolotl.refreshFromDmx();
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

    public static synchronized DmxWardenEntity getWardenAt(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (dimension == null
                || position == null) {

            return null;
        }

        for (DmxWardenEntity warden :
                getWardensInDimension(
                        dimension
                )) {

            if (position.equals(
                    warden.blockPosition()
            )) {
                return warden;
            }
        }

        return null;
    }

    public static synchronized DmxNautilusEntity getNautilusAt(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (dimension == null
                || position == null) {

            return null;
        }

        for (DmxNautilusEntity nautilus :
                getNautilusesInDimension(
                        dimension
                )) {

            if (position.equals(
                    nautilus.blockPosition()
            )) {
                return nautilus;
            }
        }

        return null;
    }

    public static synchronized DmxCreakingEntity getCreakingAt(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (position == null) {
            return null;
        }
        return getCreakingsInDimension(dimension).stream()
                .filter(entity -> position.equals(entity.blockPosition()))
                .findFirst()
                .orElse(null);
    }

    public static synchronized DmxAxolotlEntity getAxolotlAt(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        if (position == null) {
            return null;
        }
        return getAxolotlsInDimension(dimension).stream()
                .filter(entity -> position.equals(entity.blockPosition()))
                .findFirst()
                .orElse(null);
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

    public static synchronized DmxWardenEntity getWardenByEntityId(
            ResourceKey<Level> dimension,
            int entityId
    ) {
        if (dimension == null
                || entityId
                == FixtureBrowserEntry.NO_TARGET_ENTITY_ID) {

            return null;
        }

        for (DmxWardenEntity warden :
                getWardensInDimension(
                        dimension
                )) {

            if (warden.getId()
                    == entityId) {

                return warden;
            }
        }

        return null;
    }

    public static synchronized DmxNautilusEntity getNautilusByEntityId(
            ResourceKey<Level> dimension,
            int entityId
    ) {
        if (dimension == null
                || entityId
                == FixtureBrowserEntry.NO_TARGET_ENTITY_ID) {

            return null;
        }

        for (DmxNautilusEntity nautilus :
                getNautilusesInDimension(
                        dimension
                )) {

            if (nautilus.getId()
                    == entityId) {

                return nautilus;
            }
        }

        return null;
    }

    public static synchronized DmxCreakingEntity getCreakingByEntityId(
            ResourceKey<Level> dimension,
            int entityId
    ) {
        return getCreakingsInDimension(dimension).stream()
                .filter(entity -> entity.getId() == entityId)
                .findFirst()
                .orElse(null);
    }

    public static synchronized DmxAxolotlEntity getAxolotlByEntityId(
            ResourceKey<Level> dimension,
            int entityId
    ) {
        return getAxolotlsInDimension(dimension).stream()
                .filter(entity -> entity.getId() == entityId)
                .findFirst()
                .orElse(null);
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

    public static synchronized List<DmxWardenEntity>
    getAllWardens() {
        List<DmxWardenEntity> wardens =
                new ArrayList<>();

        List<ResourceKey<Level>> dimensions =
                new ArrayList<>(
                        WARDENS_BY_DIMENSION.keySet()
                );

        for (ResourceKey<Level> dimension :
                dimensions) {

            wardens.addAll(
                    getWardensInDimension(
                            dimension
                    )
            );
        }

        return List.copyOf(
                wardens
        );
    }

    public static synchronized List<DmxNautilusEntity>
    getAllNautiluses() {
        List<DmxNautilusEntity> nautiluses =
                new ArrayList<>();

        List<ResourceKey<Level>> dimensions =
                new ArrayList<>(
                        NAUTILUSES_BY_DIMENSION.keySet()
                );

        for (ResourceKey<Level> dimension :
                dimensions) {

            nautiluses.addAll(
                    getNautilusesInDimension(
                            dimension
                    )
            );
        }

        return List.copyOf(
                nautiluses
        );
    }

    public static synchronized List<DmxCreakingEntity> getAllCreakings() {
        return CREAKINGS_BY_DIMENSION.keySet().stream()
                .toList()
                .stream()
                .flatMap(dimension -> getCreakingsInDimension(dimension).stream())
                .toList();
    }

    public static synchronized List<DmxAxolotlEntity> getAllAxolotls() {
        return AXOLOTLS_BY_DIMENSION.keySet().stream()
                .toList()
                .stream()
                .flatMap(dimension -> getAxolotlsInDimension(dimension).stream())
                .toList();
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

        WARDENS_BY_DIMENSION.remove(
                dimension
        );

        NAUTILUSES_BY_DIMENSION.remove(
                dimension
        );

        CREAKINGS_BY_DIMENSION.remove(dimension);
        AXOLOTLS_BY_DIMENSION.remove(dimension);
    }

    public static synchronized void clearAll() {
        PARROTS_BY_DIMENSION.clear();
        ENDERMEN_BY_DIMENSION.clear();
        WARDENS_BY_DIMENSION.clear();
        NAUTILUSES_BY_DIMENSION.clear();
        CREAKINGS_BY_DIMENSION.clear();
        AXOLOTLS_BY_DIMENSION.clear();
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

    private static void removeInvalidWardens(
            ResourceKey<Level> dimension,
            Map<UUID, DmxWardenEntity> wardens
    ) {
        Collection<UUID> ids =
                new ArrayList<>(
                        wardens.keySet()
                );

        for (UUID id :
                ids) {

            DmxWardenEntity warden =
                    wardens.get(
                            id
                    );

            if (!isUsableServerWarden(
                    warden
            )) {
                wardens.remove(
                        id
                );
            }
        }

        if (wardens.isEmpty()) {
            WARDENS_BY_DIMENSION.remove(
                    dimension
            );
        }
    }

    private static void removeInvalidNautiluses(
            ResourceKey<Level> dimension,
            Map<UUID, DmxNautilusEntity> nautiluses
    ) {
        Collection<UUID> ids =
                new ArrayList<>(
                        nautiluses.keySet()
                );

        for (UUID id :
                ids) {

            DmxNautilusEntity nautilus =
                    nautiluses.get(
                            id
                    );

            if (!isUsableServerNautilus(
                    nautilus
            )) {
                nautiluses.remove(
                        id
                );
            }
        }

        if (nautiluses.isEmpty()) {
            NAUTILUSES_BY_DIMENSION.remove(
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

    private static boolean isUsableServerWarden(
            DmxWardenEntity warden
    ) {
        if (warden == null
                || warden.isRemoved()) {

            return false;
        }

        Level level =
                warden.level();

        return level != null
                && !level.isClientSide();
    }

    private static boolean isUsableServerNautilus(
            DmxNautilusEntity nautilus
    ) {
        if (nautilus == null
                || nautilus.isRemoved()) {

            return false;
        }

        Level level =
                nautilus.level();

        return level != null
                && !level.isClientSide();
    }

    private static <T> void unregisterEntity(
            Level level,
            UUID id,
            Map<ResourceKey<Level>, Map<UUID, T>> entitiesByDimension
    ) {
        if (level == null) {
            return;
        }

        Map<UUID, T> entities = entitiesByDimension.get(level.dimension());
        if (entities == null) {
            return;
        }

        entities.remove(id);
        if (entities.isEmpty()) {
            entitiesByDimension.remove(level.dimension());
        }
    }

    private static boolean isUsableServerCreaking(DmxCreakingEntity creaking) {
        return creaking != null
                && !creaking.isRemoved()
                && creaking.level() != null
                && !creaking.level().isClientSide();
    }

    private static boolean isUsableServerAxolotl(DmxAxolotlEntity axolotl) {
        return axolotl != null
                && !axolotl.isRemoved()
                && axolotl.level() != null
                && !axolotl.level().isClientSide();
    }
}
