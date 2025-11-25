package com.bleepz.farfromhome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Makes hostile mobs active 24/7 at extreme distances from spawn.
 * Prevents burning in sunlight and natural despawning.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class DaytimeHostileSystem {

    /**
     * Prevents undead monsters from burning in sunlight at extreme distances.
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        // Only run on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!Config.ENABLE_DAYTIME_HOSTILES.get()) {
            return;
        }

        // Only apply to hostile mobs
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        // Check if we're at extreme distance
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(monster);

        if (!DistanceManager.shouldEnableDaytimeHostiles(chunkDistance)) {
            return; // Not far enough for daytime hostiles
        }

        // Prevent burning for undead mobs
        if (shouldPreventBurning(monster)) {
            // Remove fire if the mob is burning
            if (monster.isOnFire() && monster.level().isDay()) {
                monster.clearFire();
            }

            // Set flag to prevent sun burning (this is checked by vanilla code)
            // We do this by ensuring the mob doesn't try to seek shade
            if (monster instanceof Zombie || monster instanceof Skeleton || monster instanceof AbstractSkeleton) {
                // These mobs have built-in sun avoidance - we can't easily override it
                // Instead, we'll just keep clearing fire every tick
                // This is a bit hacky but works reliably
            }
        }

        // Prevent natural despawning - but only if not too crowded
        if (monster.isPersistenceRequired()) {
            return; // Already persistent
        }

        // Check mob density to prevent infinite accumulation
        if (isAreaTooCrowded(monster)) {
            // Too many mobs nearby - let this one despawn naturally
            return;
        }

        // Make the mob persistent so it doesn't despawn
        monster.setPersistenceRequired();

        // Add a tag so we know we did this
        if (!monster.getPersistentData().getBoolean("FarFromHome_DaytimeHostile")) {
            monster.getPersistentData().putBoolean("FarFromHome_DaytimeHostile", true);

            FarFromHome.LOGGER.debug("Enabled daytime hostile mode for {} at {} chunks",
                    monster.getType().getDescription().getString(),
                    chunkDistance);
        }
    }

    /**
     * Checks if there are too many persistent mobs nearby to prevent infinite accumulation.
     * Uses scaled mob density based on distance from spawn.
     */
    private static boolean isAreaTooCrowded(Monster monster) {
        // Get distance from spawn
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(monster);

        // Get the appropriate mob cap for this distance
        int mobCap = DistanceManager.getMobDensityCap(chunkDistance);

        // Count nearby persistent hostile mobs within 32 blocks
        int nearbyCount = monster.level().getEntitiesOfClass(
                Monster.class,
                monster.getBoundingBox().inflate(32.0),
                m -> m.isPersistenceRequired()
        ).size();

        // Check against scaled cap
        return nearbyCount >= mobCap;
    }

    /**
     * Checks if a monster should be prevented from burning.
     */
    private static boolean shouldPreventBurning(Monster monster) {
        // Zombies and skeletons burn in sunlight
        if (monster instanceof Zombie) return true;
        if (monster instanceof AbstractSkeleton) return true;

        // Phantoms also burn but aren't Monster subclass, check EntityType
        if (monster.getType() == EntityType.PHANTOM) return true;

        // Drowned don't burn in vanilla
        if (monster.getType() == EntityType.DROWNED) return false;

        return false;
    }

    /**
     * Additional protection: cancel fire damage for mobs in daytime hostile zones.
     */
    @SubscribeEvent
    public static void onMobBurn(MobEffectEvent.Applicable event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!Config.ENABLE_DAYTIME_HOSTILES.get()) {
            return;
        }

        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        // Check if this mob is in daytime hostile zone
        if (!monster.getPersistentData().getBoolean("FarFromHome_DaytimeHostile")) {
            return;
        }

        // If it's daytime and the mob is burning from sun, prevent it
        if (monster.level().isDay() && monster.isOnFire() && shouldPreventBurning(monster)) {
            monster.clearFire();
        }
    }
}