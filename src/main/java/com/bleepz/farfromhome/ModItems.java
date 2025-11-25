package com.bleepz.farfromhome;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all custom items for the mod.
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, FarFromHome.MODID);

    // Loot bags of different tiers
    public static final DeferredHolder<Item, Item> COMMON_LOOT_BAG = ITEMS.register("common_loot_bag",
            () -> new LootBagItem(LootBagTier.COMMON));

    public static final DeferredHolder<Item, Item> UNCOMMON_LOOT_BAG = ITEMS.register("uncommon_loot_bag",
            () -> new LootBagItem(LootBagTier.UNCOMMON));

    public static final DeferredHolder<Item, Item> RARE_LOOT_BAG = ITEMS.register("rare_loot_bag",
            () -> new LootBagItem(LootBagTier.RARE));

    public static final DeferredHolder<Item, Item> LEGENDARY_LOOT_BAG = ITEMS.register("legendary_loot_bag",
            () -> new LootBagItem(LootBagTier.LEGENDARY));

    // Greater Experience Bottle - gives 50-100 XP
    public static final DeferredHolder<Item, Item> GREATER_EXPERIENCE_BOTTLE = ITEMS.register("greater_experience_bottle",
            GreaterExperienceBottleItem::new);

    // Ancient Enchanting Altar - single-use unlimited enchant block
    public static final DeferredHolder<Item, Item> ANCIENT_ENCHANTING_ALTAR = ITEMS.register("ancient_enchanting_altar",
            () -> new AncientEnchantingAltarItem(ModBlocks.ANCIENT_ENCHANTING_ALTAR));

    /**
     * Registers items to the event bus.
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        FarFromHome.LOGGER.info("Registered Far From Home items");
    }
}