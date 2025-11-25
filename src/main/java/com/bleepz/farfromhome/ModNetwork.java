package com.bleepz.farfromhome;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers network packets for client-server communication.
 */
@EventBusSubscriber(modid = FarFromHome.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Register enchant altar packet
        registrar.playToServer(
                EnchantAltarPacket.TYPE,
                EnchantAltarPacket.STREAM_CODEC,
                EnchantAltarPacket::handle
        );

        FarFromHome.LOGGER.info("Registered network packets");
    }
}
