package dmx.lighting;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.SpawnEggItem;

import java.util.List;
import java.util.function.Function;

/**
 * Registers all custom items used by dmxLighting.
 */
public final class ModItems {

    private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES_TAB =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.withDefaultNamespace(
                            "tools_and_utilities"
                    )
            );

    /**
     * Portable handheld Lighting Console.
     *
     * Right-clicking the item requests the current fixture-browser data
     * from the server.
     */
    public static final LightingConsoleItem LIGHTING_CONSOLE =
            register(
                    "lighting_console",
                    LightingConsoleItem::new,
                    new Item.Properties()
                            .stacksTo(1)
            );

    public static final SpawnEggItem DMX_PARROT_SPAWN_EGG =
            register(
                    "dmx_parrot_spawn_egg",
                    SpawnEggItem::new,
                    new Item.Properties()
                            .spawnEgg(
                                    ModEntities.DMX_PARROT
                            )
            );

    public static final SpawnEggItem DMX_ENDERMAN_SPAWN_EGG =
            register(
                    "dmx_enderman_spawn_egg",
                    SpawnEggItem::new,
                    new Item.Properties()
                            .spawnEgg(
                                    ModEntities.DMX_ENDERMAN
                            )
            );

    public static final SpawnEggItem DMX_WARDEN_SPAWN_EGG =
            register(
                    "dmx_warden_spawn_egg",
                    SpawnEggItem::new,
                    new Item.Properties()
                            .spawnEgg(
                                    ModEntities.DMX_WARDEN
                            )
            );

    public static final SpawnEggItem DMX_NAUTILUS_SPAWN_EGG =
            register(
                    "dmx_nautilus_spawn_egg",
                    SpawnEggItem::new,
                    new Item.Properties()
                            .spawnEgg(
                                    ModEntities.DMX_NAUTILUS
                            )
            );

    public static final Item GENERIC_60_BPM =
            registerGenericDisc(
                    "generic_60_bpm",
                    ModJukeboxSongs.GENERIC_60_BPM
            );

    public static final Item GENERIC_70_BPM =
            registerGenericDisc(
                    "generic_70_bpm",
                    ModJukeboxSongs.GENERIC_70_BPM
            );

    public static final Item GENERIC_80_BPM =
            registerGenericDisc(
                    "generic_80_bpm",
                    ModJukeboxSongs.GENERIC_80_BPM
            );

    public static final Item GENERIC_90_BPM =
            registerGenericDisc(
                    "generic_90_bpm",
                    ModJukeboxSongs.GENERIC_90_BPM
            );

    public static final Item GENERIC_100_BPM =
            registerGenericDisc(
                    "generic_100_bpm",
                    ModJukeboxSongs.GENERIC_100_BPM
            );

    public static final Item GENERIC_110_BPM =
            registerGenericDisc(
                    "generic_110_bpm",
                    ModJukeboxSongs.GENERIC_110_BPM
            );

    public static final Item GENERIC_120_BPM =
            registerGenericDisc(
                    "generic_120_bpm",
                    ModJukeboxSongs.GENERIC_120_BPM
            );

    public static final Item GENERIC_130_BPM =
            registerGenericDisc(
                    "generic_130_bpm",
                    ModJukeboxSongs.GENERIC_130_BPM
            );

    public static final Item GENERIC_140_BPM =
            registerGenericDisc(
                    "generic_140_bpm",
                    ModJukeboxSongs.GENERIC_140_BPM
            );

    private static final List<Item> GENERIC_BPM_DISCS =
            List.of(
                    GENERIC_60_BPM,
                    GENERIC_70_BPM,
                    GENERIC_80_BPM,
                    GENERIC_90_BPM,
                    GENERIC_100_BPM,
                    GENERIC_110_BPM,
                    GENERIC_120_BPM,
                    GENERIC_130_BPM,
                    GENERIC_140_BPM
            );

    private ModItems() {
        // Utility class: do not instantiate.
    }

    /**
     * Creates and registers one item.
     *
     * Minecraft 26.x requires the item's ResourceKey to be assigned to
     * Item.Properties before constructing the item.
     */
    private static <T extends Item> T register(
            String name,
            Function<Item.Properties, T> itemFactory,
            Item.Properties properties
    ) {
        ResourceKey<Item> itemKey =
                ResourceKey.create(
                        Registries.ITEM,
                        DmxLighting.id(name)
                );

        T item =
                itemFactory.apply(
                        properties.setId(itemKey)
                );

        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                item
        );

        return item;
    }

    private static Item registerGenericDisc(
            String name,
            ResourceKey<JukeboxSong> song
    ) {
        return register(
                name,
                Item::new,
                new Item.Properties()
                        .stacksTo(1)
                        .jukeboxPlayable(song)
        );
    }

    /**
     * Adds the Lighting Console to Creative inventory and forces this class
     * to load during mod initialization.
     */
    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(
                TOOLS_AND_UTILITIES_TAB
        ).register(
                output -> output.accept(
                        LIGHTING_CONSOLE
                )
        );

        CreativeModeTabEvents.modifyOutputEvent(
                TOOLS_AND_UTILITIES_TAB
        ).register(
                output -> {
                    for (Item disc : GENERIC_BPM_DISCS) {
                        output.accept(disc);
                    }
                }
        );

        CreativeModeTabEvents.modifyOutputEvent(
                TOOLS_AND_UTILITIES_TAB
        ).register(
                output -> output.accept(
                        DMX_PARROT_SPAWN_EGG
                )
        );

        CreativeModeTabEvents.modifyOutputEvent(
                TOOLS_AND_UTILITIES_TAB
        ).register(
                output -> output.accept(
                        DMX_ENDERMAN_SPAWN_EGG
                )
        );

        CreativeModeTabEvents.modifyOutputEvent(
                TOOLS_AND_UTILITIES_TAB
        ).register(
                output -> output.accept(
                        DMX_WARDEN_SPAWN_EGG
                )
        );

        CreativeModeTabEvents.modifyOutputEvent(
                TOOLS_AND_UTILITIES_TAB
        ).register(
                output -> output.accept(
                        DMX_NAUTILUS_SPAWN_EGG
                )
        );

        DmxLighting.LOGGER.info(
                "Registered dmxLighting items."
        );
    }
}
