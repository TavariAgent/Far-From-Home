package com.bleepz.farfromhome;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.SimpleContainer;

/**
 * Ancient Enchanting Altar - A single-use block that allows unlimited enchantments.
 * Opens a GUI when right-clicked to apply enchantments to items.
 */
public class AncientEnchantingAltarBlock extends Block {

    public AncientEnchantingAltarBlock() {
        super(Properties.of()
                .strength(5.0f, 1200.0f) // Same as enchantment table
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .noOcclusion()
                .lightLevel((state) -> 12)); // Glows like enchantment table
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Open the Ancient Altar GUI
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new AncientAltarMenu(
                                    containerId,
                                    playerInventory,
                                    new SimpleContainer(2),
                                    pos
                            ),
                            Component.literal("Ancient Enchanting Altar")
                    ),
                    buf -> buf.writeBlockPos(pos)
            );

            FarFromHome.LOGGER.info("Player {} opened Ancient Enchanting Altar GUI",
                    player.getName().getString());
        }

        return InteractionResult.SUCCESS;
    }
}