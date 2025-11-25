package com.bleepz.farfromhome;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all custom blocks for the mod.
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, FarFromHome.MODID);

    // Ancient Enchanting Altar - single-use unlimited enchant block
    public static final DeferredHolder<Block, Block> ANCIENT_ENCHANTING_ALTAR = BLOCKS.register("ancient_enchanting_altar",
            AncientEnchantingAltarBlock::new);

    /**
     * Registers blocks to the event bus.
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        FarFromHome.LOGGER.info("Registered Far From Home blocks");
    }
}
