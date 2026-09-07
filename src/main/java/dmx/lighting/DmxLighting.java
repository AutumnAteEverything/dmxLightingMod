package dmx.lighting;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmxLighting implements ModInitializer {

    public static final String MOD_ID = "dmxlighting";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("dmxLighting is initializing.");
        DmxFixtureProfileRegistry.initialize();
        ModEntities.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModItems.initialize();
        DmxNetworking.initialize();

        DmxCommand.initialize();
        DmxBulkAddressCommand.initialize();
        DmxSendCommand.initialize();
        DmxControlCommand.initialize();
        AutomaticDmxCommand.initialize();

        LOGGER.info("dmxLighting loaded successfully.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                path
        );
    }
}
