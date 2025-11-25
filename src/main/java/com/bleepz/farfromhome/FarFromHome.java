package com.bleepz.farfromhome;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FarFromHome.MODID)
public class FarFromHome {
    public static final String MODID = "farfromhome";
    public static final Logger LOGGER = LoggerFactory.getLogger(FarFromHome.class);

    public FarFromHome(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Far From Home is initializing...");

        // Register our config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Register items (loot bags, bottles, altar)
        ModItems.register(modEventBus);

        // Register blocks (altar)
        ModBlocks.register(modEventBus);

        // Register menu types (GUIs)
        ModMenuTypes.register(modEventBus);

        LOGGER.info("Far From Home initialized successfully!");
    }
}