package com.bleepz.farfromhome;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers custom menu types for GUIs.
 */
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, FarFromHome.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AncientAltarMenu>> ANCIENT_ALTAR =
            MENUS.register("ancient_altar",
                    () -> IMenuTypeExtension.create(AncientAltarMenu::new));

    /**
     * Registers menu types to the event bus.
     */
    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
        FarFromHome.LOGGER.info("Registered Far From Home menu types");
    }
}
