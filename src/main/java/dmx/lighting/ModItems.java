package dmx.lighting;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

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

        DmxLighting.LOGGER.info(
                "Registered dmxLighting items."
        );
    }
}
