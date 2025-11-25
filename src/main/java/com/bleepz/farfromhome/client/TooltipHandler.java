package com.bleepz.farfromhome.client;

import com.bleepz.farfromhome.FarFromHome;
import com.bleepz.farfromhome.UnlimitedEnchantmentSystem;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Adds unlimited enchantment tooltips to items.
 */
@EventBusSubscriber(modid = FarFromHome.MODID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (UnlimitedEnchantmentSystem.hasUnlimitedEnchantments(event.getItemStack())) {
            List<Component> enchantTooltips = UnlimitedEnchantmentSystem.getEnchantmentTooltips(event.getItemStack());

            // Add a blank line before our enchantments
            if (!enchantTooltips.isEmpty()) {
                event.getToolTip().add(Component.empty());
                event.getToolTip().addAll(enchantTooltips);
            }
        }
    }
}
