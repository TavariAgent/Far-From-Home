package com.bleepz.farfromhome;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

/**
 * Handles ore scaling mechanics based on distance from spawn.
 * Increases ore drops, XP, and restricts diamond availability near spawn.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class OreScalingSystem {
    
    private static final Random RANDOM = new Random();
    
    // Define which blocks are considered ores
    private static final Set<Block> VALUABLE_ORES = Set.of(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.NETHER_QUARTZ_ORE,
        Blocks.NETHER_GOLD_ORE,
        Blocks.ANCIENT_DEBRIS
    );
    
    // Diamond ores specifically for spawn restriction
    private static final Set<Block> DIAMOND_ORES = Set.of(
        Blocks.DIAMOND_ORE,
        Blocks.DEEPSLATE_DIAMOND_ORE
    );
    
    /**
     * Main event handler for block breaking.
     * Applies ore scaling and spawn zone penalties.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // Only run on server side
        if (event.getLevel().isClientSide()) {
            return;
        }
        
        if (!Config.ENABLE_ORE_SCALING.get()) {
            return;
        }
        
        BlockState state = event.getState();
        Block block = state.getBlock();
        
        // Check if this is an ore we care about
        if (!VALUABLE_ORES.contains(block)) {
            return;
        }
        
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        
        // Calculate distance from spawn
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(level, pos);
        
        // Check for spawn zone penalties
        if (chunkDistance < Config.DIAMOND_SPAWN_DISTANCE.get()) {
            if (shouldApplySpawnPenalty(block, chunkDistance)) {
                handleSpawnZonePenalty(event, block, pos);
                return; // Ore drops nothing
            }
        }
        
        // Apply normal ore scaling bonuses (outside spawn or penalty didn't trigger)
        applyOreScaling(event, block, pos, chunkDistance);
    }
    
    /**
     * Determines if spawn zone penalty should apply to this ore.
     * Diamonds: 99% chance to fail
     * Other ores: 25% chance to fail
     */
    private static boolean shouldApplySpawnPenalty(Block block, int chunkDistance) {
        // Closer to spawn = higher penalty chance (for non-diamonds)
        // At spawn (0 chunks): 25% penalty
        // At 312 chunks (5000 blocks): 0% penalty (gradually scales)
        
        double distanceRatio = (double) chunkDistance / Config.DIAMOND_SPAWN_DISTANCE.get();
        
        if (DIAMOND_ORES.contains(block)) {
            // Diamonds: 99% failure near spawn, scales down to 0% at distance threshold
            double penaltyChance = 0.99 * (1.0 - distanceRatio);
            return RANDOM.nextDouble() < penaltyChance;
        } else {
            // Other ores: 25% failure near spawn, scales down to 0% at distance threshold
            double penaltyChance = 0.25 * (1.0 - distanceRatio);
            return RANDOM.nextDouble() < penaltyChance;
        }
    }
    
    /**
     * Handles ore breaking in spawn zone when penalty is applied.
     * Ore breaks but drops nothing with a mysterious message.
     */
    private static void handleSpawnZonePenalty(BlockEvent.BreakEvent event, Block block, BlockPos pos) {
        // Cancel the normal drops
        event.setCanceled(true);
        
        // Manually break the block (so it doesn't stay there)
        Level level = (Level) event.getLevel();
        level.destroyBlock(pos, false); // false = no drops
        
        // Give the player the cheeky message
        if (event.getPlayer() instanceof ServerPlayer player) {
            player.displayClientMessage(
                Component.literal("§7Your skill led you to believe there was ore here, but nothing was recovered."), 
                true // Action bar
            );
            
            FarFromHome.LOGGER.debug("Spawn zone penalty applied - {} dropped nothing for {}", 
                block.getName().getString(), player.getName().getString());
        }
    }
    
    /**
     * Applies ore scaling bonuses for drops and XP.
     */
    private static void applyOreScaling(BlockEvent.BreakEvent event, Block block, BlockPos pos, int chunkDistance) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // Get multipliers
        double dropMultiplier = DistanceManager.getOreDropMultiplier(chunkDistance);
        double xpMultiplier = DistanceManager.getOreXpMultiplier(chunkDistance);
        
        // Only apply if there's actually a bonus
        if (dropMultiplier <= 1.0 && xpMultiplier <= 1.0) {
            return;
        }
        
        // Get the drops that would normally occur
        List<ItemStack> drops = Block.getDrops(event.getState(), serverLevel, pos, null, event.getPlayer(), event.getPlayer().getMainHandItem());
        
        if (drops.isEmpty()) {
            return;
        }
        
        // Cancel the default drop event
        event.setCanceled(true);
        
        // Break the block
        serverLevel.destroyBlock(pos, false);
        
        // Apply bonus drops
        for (ItemStack originalDrop : drops) {
            int bonusCount = calculateBonusDrops(originalDrop.getCount(), dropMultiplier);
            
            if (bonusCount > 0) {
                ItemStack bonusDrop = originalDrop.copy();
                bonusDrop.setCount(bonusCount);
                
                // Spawn the item in the world
                ItemEntity itemEntity = new ItemEntity(
                    serverLevel,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    bonusDrop
                );
                itemEntity.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(itemEntity);
            }
        }
        
        // Apply bonus XP
        int baseXP = getBaseXPForOre(block);
        if (baseXP > 0) {
            int bonusXP = (int) (baseXP * xpMultiplier);
            
            if (bonusXP > baseXP) {
                // Spawn extra XP orbs
                ExperienceOrb.award(serverLevel, 
                    pos.getCenter(), 
                    bonusXP - baseXP); // Only spawn the bonus amount (vanilla will spawn base)
                
                // Notify player of bonus
                if (event.getPlayer() instanceof ServerPlayer player) {
                    int tier = DistanceManager.getTier(chunkDistance);
                    if (tier > 0 && RANDOM.nextDouble() < 0.1) { // 10% chance to show message
                        player.displayClientMessage(
                            Component.literal(String.format("§6+%dx ore bonus! §7(Tier %d)", 
                                (int)dropMultiplier, tier)),
                            true
                        );
                    }
                }
            }
        }
        
        FarFromHome.LOGGER.debug("Ore scaling applied at {} chunks: {}x drops, {}x XP", 
            chunkDistance, String.format("%.1f", dropMultiplier), String.format("%.1f", xpMultiplier));
    }
    
    /**
     * Calculates bonus drops based on multiplier.
     * Uses probabilistic rounding for fairness (e.g., 1.3x = 1 drop + 30% chance of another).
     */
    private static int calculateBonusDrops(int baseCount, double multiplier) {
        double totalDrops = baseCount * multiplier;
        int guaranteedDrops = (int) totalDrops;
        double fractionalPart = totalDrops - guaranteedDrops;
        
        // Roll for the fractional part
        if (RANDOM.nextDouble() < fractionalPart) {
            guaranteedDrops++;
        }
        
        return guaranteedDrops;
    }
    
    /**
     * Gets the base XP value for different ore types.
     * Based on vanilla Minecraft XP drops.
     */
    private static int getBaseXPForOre(Block block) {
        // Coal: 0-2 XP
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return 2;
        }
        
        // Copper: 0 XP (raw ore)
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return 0;
        }
        
        // Iron: 0 XP (raw ore)
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return 0;
        }
        
        // Gold: 0 XP (raw ore)
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return 0;
        }
        
        // Redstone: 1-5 XP
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return 4;
        }
        
        // Lapis: 2-5 XP
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return 4;
        }
        
        // Diamond: 3-7 XP
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return 5;
        }
        
        // Emerald: 3-7 XP
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return 5;
        }
        
        // Nether Quartz: 2-5 XP
        if (block == Blocks.NETHER_QUARTZ_ORE) {
            return 4;
        }
        
        // Nether Gold: 0 XP (raw ore)
        if (block == Blocks.NETHER_GOLD_ORE) {
            return 0;
        }
        
        // Ancient Debris: 0 XP (doesn't drop XP normally)
        if (block == Blocks.ANCIENT_DEBRIS) {
            return 0;
        }
        
        return 0;
    }
}
