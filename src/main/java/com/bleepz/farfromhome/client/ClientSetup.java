package com.bleepz.farfromhome.client;

import com.bleepz.farfromhome.AncientAltarScreen;
import com.bleepz.farfromhome.FarFromHome;
import com.bleepz.farfromhome.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-side setup and registration.
 */
@EventBusSubscriber(modid = FarFromHome.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ANCIENT_ALTAR.get(), AncientAltarScreen::new);

        FarFromHome.LOGGER.info("Registered Ancient Altar screen");
    }
}