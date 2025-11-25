package com.bleepz.farfromhome;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles unlimited enchantment storage using VANILLA enchantment system.
 * Simply adds enchantments without worrying about compatibility rules or limits!
 */
public class UnlimitedEnchantmentSystem {

    /**
     * Record to hold enchantment data.
     */
    public record EnchantmentData(String id, int level) {}

    /**
     * Adds an enchantment from a book to an item using vanilla storage.
     * Ignores compatibility rules - just adds it!
     */
    public static boolean addEnchantmentFromBook(ItemStack item, ItemStack book) {
        if (item.isEmpty() || book.isEmpty()) {
            return false;
        }

        // Get enchantments from the book
        ItemEnchantments bookEnchants = EnchantmentHelper.getEnchantmentsForCrafting(book);

        if (bookEnchants.isEmpty()) {
            return false;
        }

        // Get current enchantments on the item (or empty if none)
        ItemEnchantments currentEnchants = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // Create mutable copy to add new enchantments
        ItemEnchantments.Mutable mutableEnchants = new ItemEnchantments.Mutable(currentEnchants);

        // Add each enchantment from the book (ignoring compatibility rules!)
        bookEnchants.entrySet().forEach(entry -> {
            mutableEnchants.set(entry.getKey(), entry.getIntValue());
        });

        // Save back to item
        item.set(DataComponents.ENCHANTMENTS, mutableEnchants.toImmutable());

        return true;
    }

    /**
     * Gets the number of enchantments on an item.
     */
    public static int getEnchantmentCount(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }

        ItemEnchantments enchants = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return enchants.size();
    }

    /**
     * Calculates the XP cost for adding an enchantment.
     * First 5 enchants are free, then exponential: 10, 20, 40, 80, 160...
     */
    public static int calculateXPCost(ItemStack item) {
        int currentCount = getEnchantmentCount(item);

        // First 5 enchants are free
        if (currentCount < 5) {
            return 0;
        }

        // After 5: 2^(n-5) * 10
        // 6th = 10, 7th = 20, 8th = 40, etc.
        int exponent = currentCount - 5;
        return (int)(Math.pow(2, exponent) * 10);
    }

    /**
     * Gets a list of all enchantments on an item.
     */
    public static List<EnchantmentData> getEnchantmentList(ItemStack item) {
        List<EnchantmentData> enchants = new ArrayList<>();

        if (item.isEmpty()) {
            return enchants;
        }

        ItemEnchantments itemEnchants = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        itemEnchants.entrySet().forEach(entry -> {
            String id = entry.getKey().getRegisteredName();
            int level = entry.getIntValue();
            enchants.add(new EnchantmentData(id, level));
        });

        return enchants;
    }

    /**
     * Gets enchantment tooltips - but vanilla already handles this!
     * This method is here for backwards compatibility but isn't needed.
     */
    public static List<Component> getEnchantmentTooltips(ItemStack item) {
        // Vanilla already shows enchantments in tooltips!
        // No custom rendering needed!
        return new ArrayList<>();
    }

    /**
     * Checks if an item has enchantments.
     */
    public static boolean hasUnlimitedEnchantments(ItemStack item) {
        return getEnchantmentCount(item) > 0;
    }
}