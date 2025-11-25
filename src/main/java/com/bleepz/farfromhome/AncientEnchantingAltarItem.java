package com.bleepz.farfromhome;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;

/**
 * The item form of the Ancient Enchanting Altar block.
 * Has enchantment glint and special tooltip.
 */
public class AncientEnchantingAltarItem extends BlockItem {

    public AncientEnchantingAltarItem(Supplier<? extends Block> blockSupplier) {
        super(blockSupplier.get(),
                new Properties()
                        .stacksTo(1) // Only one at a time
                        .fireResistant()); // Doesn't burn in lava/fire
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal(ChatFormatting.LIGHT_PURPLE + "Place and right-click to enchant"));
        tooltipComponents.add(Component.literal(ChatFormatting.GOLD + "Allows unlimited enchantments!"));
        tooltipComponents.add(Component.literal(ChatFormatting.RED + "Single use - destroys after enchanting"));
        tooltipComponents.add(Component.literal(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC +
                "First 5 enchants are free, then costs XP"));
        tooltipComponents.add(Component.literal(ChatFormatting.DARK_PURPLE + "" + ChatFormatting.ITALIC +
                "An artifact from the world's edge"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint to make it look magical
        return true;
    }
}