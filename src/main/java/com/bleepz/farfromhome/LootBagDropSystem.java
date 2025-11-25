package com.bleepz.farfromhome;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Random;

/**
 * Handles loot bag drops from monsters based on distance and rarity.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class LootBagDropSystem {

    private static final Random RANDOM = new Random();

    /**
     * Handles mob death and potentially drops loot bags.
     */
    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        // Only run on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!Config.ENABLE_LOOT_BAGS.get()) {
            return;
        }

        // Only apply to hostile mobs
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        // Must be killed by a player to drop bags
        if (event.getSource().getEntity() == null) {
            return;
        }

        // Calculate distance from spawn
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(monster);

        // Get appropriate tier for this distance
        LootBagTier tier = LootBagTier.getTierForDistance(chunkDistance);

        // Calculate drop chance
        double dropChance = calculateDropChance(monster, chunkDistance, tier);

        if (RANDOM.nextDouble() < dropChance) {
            dropLootBag(monster, tier);
        }
    }

    /**
     * Calculates the chance for a loot bag to drop.
     */
    private static double calculateDropChance(Monster monster, int chunkDistance, LootBagTier tier) {
        // Base drop chance from config
        double baseChance = Config.LOOT_BAG_BASE_DROP_CHANCE.get();

        // Increase chance with distance
        int chunkTier = DistanceManager.getTier(chunkDistance);
        double distanceBonus = chunkTier * Config.LOOT_BAG_CHANCE_PER_TIER.get();

        double totalChance = baseChance + distanceBonus;

        // Cap at max chance
        totalChance = Math.min(totalChance, Config.LOOT_BAG_MAX_DROP_CHANCE.get());

        // Bonus for rare monsters
        if (RareBuffSystem.isRareMonster(monster)) {
            int buffCount = RareBuffSystem.getBuffCount(monster);

            // Rare monsters have MUCH higher drop chances
            // 1 buff: +20%, 2 buffs: +40%, etc.
            double rareBonus = buffCount * 0.20;
            totalChance = Math.min(1.0, totalChance + rareBonus);

            // 10+ buffs guarantee legendary bags
            if (buffCount >= 10) {
                totalChance = 1.0;
            }
        }

        return totalChance;
    }

    /**
     * Drops a loot bag at the monster's position.
     */
    private static void dropLootBag(Monster monster, LootBagTier tier) {
        if (!(monster.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Rare monsters with 10+ buffs always drop legendary
        if (RareBuffSystem.isRareMonster(monster)) {
            int buffCount = RareBuffSystem.getBuffCount(monster);
            if (buffCount >= 10) {
                tier = LootBagTier.LEGENDARY;
            }
        }

        // Create the loot bag item
        ItemStack lootBag = switch (tier) {
            case COMMON -> new ItemStack(ModItems.COMMON_LOOT_BAG.get());
            case UNCOMMON -> new ItemStack(ModItems.UNCOMMON_LOOT_BAG.get());
            case RARE -> new ItemStack(ModItems.RARE_LOOT_BAG.get());
            case LEGENDARY -> new ItemStack(ModItems.LEGENDARY_LOOT_BAG.get());
        };

        // Drop the bag at monster location
        ItemEntity itemEntity = new ItemEntity(
                serverLevel,
                monster.getX(),
                monster.getY() + 0.5,
                monster.getZ(),
                lootBag
        );

        // Add some velocity for dramatic effect
        itemEntity.setDeltaMovement(
                (RANDOM.nextDouble() - 0.5) * 0.1,
                0.3,
                (RANDOM.nextDouble() - 0.5) * 0.1
        );

        itemEntity.setDefaultPickUpDelay();
        serverLevel.addFreshEntity(itemEntity);

        FarFromHome.LOGGER.debug("Dropped {} loot bag from {} at {} chunks",
                tier.getDisplayName(),
                monster.getType().getDescription().getString(),
                DistanceManager.getChunkDistanceFromSpawn(monster));
    }
}
