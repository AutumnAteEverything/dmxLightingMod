package dmx.lighting;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.warden.Warden;

/**
 * Registers custom entities supplied by dmxLighting.
 */
public final class ModEntities {

    public static final ResourceKey<EntityType<?>> DMX_PARROT_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    DmxLighting.id(
                            "dmx_parrot"
                    )
            );

    public static final ResourceKey<EntityType<?>> DMX_ENDERMAN_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    DmxLighting.id(
                            "dmx_enderman"
                    )
            );

    public static final ResourceKey<EntityType<?>> DMX_WARDEN_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    DmxLighting.id(
                            "dmx_warden"
                    )
            );

    public static final ResourceKey<EntityType<?>> DMX_NAUTILUS_KEY =
            ResourceKey.create(
                    Registries.ENTITY_TYPE,
                    DmxLighting.id(
                            "dmx_nautilus"
                    )
            );

    public static final EntityType<DmxParrotEntity> DMX_PARROT =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    DMX_PARROT_KEY,
                    EntityType.Builder
                            .of(
                                    DmxParrotEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(
                                    0.5F,
                                    0.9F
                            )
                            .clientTrackingRange(
                                    8
                            )
                            .updateInterval(
                                    3
                            )
                            .build(
                                    DMX_PARROT_KEY
                            )
            );

    public static final EntityType<DmxEndermanEntity> DMX_ENDERMAN =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    DMX_ENDERMAN_KEY,
                    EntityType.Builder
                            .of(
                                    DmxEndermanEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(
                                    0.6F,
                                    2.9F
                            )
                            .clientTrackingRange(
                                    8
                            )
                            .updateInterval(
                                    3
                            )
                            .build(
                                    DMX_ENDERMAN_KEY
                            )
            );

    public static final EntityType<DmxWardenEntity> DMX_WARDEN =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    DMX_WARDEN_KEY,
                    EntityType.Builder
                            .of(
                                    DmxWardenEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(
                                    0.9F,
                                    2.9F
                            )
                            .clientTrackingRange(
                                    16
                            )
                            .updateInterval(
                                    3
                            )
                            .build(
                                    DMX_WARDEN_KEY
                            )
            );

    public static final EntityType<DmxNautilusEntity> DMX_NAUTILUS =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    DMX_NAUTILUS_KEY,
                    EntityType.Builder
                            .of(
                                    DmxNautilusEntity::new,
                                    MobCategory.WATER_CREATURE
                            )
                            .sized(
                                    0.875F,
                                    0.95F
                            )
                            .passengerAttachments(
                                    1.1375F
                            )
                            .eyeHeight(
                                    0.2751F
                            )
                            .clientTrackingRange(
                                    10
                            )
                            .updateInterval(
                                    3
                            )
                            .build(
                                    DMX_NAUTILUS_KEY
                            )
            );

    private ModEntities() {
        // Utility class.
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(
                DMX_PARROT,
                Parrot.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
                DMX_ENDERMAN,
                EnderMan.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
                DMX_WARDEN,
                Warden.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
                DMX_NAUTILUS,
                Nautilus.createAttributes()
        );

        DmxLighting.LOGGER.info(
                "Registered dmxLighting entities."
        );
    }
}
