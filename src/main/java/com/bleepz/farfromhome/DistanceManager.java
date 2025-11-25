package com.bleepz.farfromhome;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Utility class for calculating distance from spawn and determining difficulty tiers.
 * All distances are calculated in chunks for performance and consistency.
 */
public class DistanceManager {

    /**
     * Calculates the distance in chunks from world spawn to the given position.
     * Uses 2D distance (ignores Y coordinate) for fairness across all heights.
     *
     * @param level The world level
     * @param pos The position to check
     * @return Distance in chunks from spawn (rounded down)
     */
    public static int getChunkDistanceFromSpawn(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos spawnPos = serverLevel.getSharedSpawnPos();
            return getChunkDistance(spawnPos, pos);
        }
        return 0;
    }

    /**
     * Calculates the distance in chunks from world spawn to the given entity.
     *
     * @param entity The entity to check
     * @return Distance in chunks from spawn (rounded down)
     */
    public static int getChunkDistanceFromSpawn(Entity entity) {
        return getChunkDistanceFromSpawn(entity.level(), entity.blockPosition());
    }

    /**
     * Calculates the 2D distance between two positions in chunks.
     *
     * @param pos1 First position
     * @param pos2 Second position
     * @return Distance in chunks (rounded down)
     */
    public static int getChunkDistance(BlockPos pos1, BlockPos pos2) {
        // Calculate distance in blocks
        double dx = pos1.getX() - pos2.getX();
        double dz = pos1.getZ() - pos2.getZ();
        double distanceInBlocks = Math.sqrt(dx * dx + dz * dz);

        // Convert to chunks (16 blocks = 1 chunk)
        return (int) (distanceInBlocks / 16.0);
    }

    /**
     * Determines the difficulty tier based on chunk distance from spawn.
     * Tier 0 = 0-99 chunks, Tier 1 = 100-199 chunks, etc.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return The difficulty tier (0-indexed)
     */
    public static int getTier(int chunkDistance) {
        int chunksPerTier = Config.CHUNKS_PER_TIER.get();
        return chunkDistance / chunksPerTier;
    }

    /**
     * Calculates the monster level for a given distance from spawn.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return The calculated monster level
     */
    public static int getMonsterLevel(int chunkDistance) {
        if (!Config.ENABLE_MONSTER_SCALING.get()) {
            return 1;
        }

        int tier = getTier(chunkDistance);
        int minLevel = Config.MIN_MONSTER_LEVEL.get();
        int levelRange = Config.MAX_MONSTER_LEVEL_PER_TIER.get();

        // Level increases with each tier
        // Tier 0: minLevel to minLevel + levelRange
        // Tier 1: minLevel + levelRange to minLevel + (levelRange * 2)
        // etc.
        int minLevelForTier = minLevel + (tier * levelRange);
        int maxLevelForTier = minLevel + ((tier + 1) * levelRange);

        // Return a random level within the tier's range
        return minLevelForTier + (int) (Math.random() * (maxLevelForTier - minLevelForTier + 1));
    }

    /**
     * Calculates the gear spawn chance for a given distance from spawn.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return The gear spawn chance (0.0 to 1.0)
     */
    public static double getGearChance(int chunkDistance) {
        int tier = getTier(chunkDistance);
        double baseChance = Config.BASE_GEAR_CHANCE.get();
        double chancePerTier = Config.GEAR_CHANCE_PER_TIER.get();
        double maxChance = Config.MAX_GEAR_CHANCE.get();

        double chance = baseChance + (tier * chancePerTier);
        return Math.min(chance, maxChance);
    }

    /**
     * Calculates the ore drop multiplier for a given distance from spawn.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return The ore drop multiplier (1.0 = normal drops)
     */
    public static double getOreDropMultiplier(int chunkDistance) {
        if (!Config.ENABLE_ORE_SCALING.get()) {
            return 1.0;
        }

        int tier = getTier(chunkDistance);
        double multiplierPerTier = Config.ORE_DROP_MULTIPLIER_PER_TIER.get();
        double maxMultiplier = Config.MAX_ORE_DROP_MULTIPLIER.get();

        double multiplier = 1.0 + (tier * multiplierPerTier);
        return Math.min(multiplier, maxMultiplier);
    }

    /**
     * Calculates the ore XP multiplier for a given distance from spawn.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return The ore XP multiplier (1.0 = normal XP)
     */
    public static double getOreXpMultiplier(int chunkDistance) {
        if (!Config.ENABLE_ORE_SCALING.get()) {
            return 1.0;
        }

        int tier = getTier(chunkDistance);
        double multiplierPerTier = Config.ORE_XP_MULTIPLIER_PER_TIER.get();
        double maxMultiplier = Config.MAX_ORE_XP_MULTIPLIER.get();

        double multiplier = 1.0 + (tier * multiplierPerTier);
        return Math.min(multiplier, maxMultiplier);
    }

    /**
     * Checks if daytime hostiles should be enabled at this distance.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return True if daytime hostiles are enabled
     */
    public static boolean shouldEnableDaytimeHostiles(int chunkDistance) {
        return Config.ENABLE_DAYTIME_HOSTILES.get() &&
                chunkDistance >= Config.DAYTIME_HOSTILE_DISTANCE.get();
    }

    /**
     * Checks if boss encounters can occur at this distance.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return True if bosses can spawn
     */
    public static boolean canSpawnBosses(int chunkDistance) {
        return Config.ENABLE_BOSS_ENCOUNTERS.get() &&
                chunkDistance >= Config.BOSS_SPAWN_DISTANCE.get();
    }

    /**
     * Gets a user-friendly description of the current danger level.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return A string describing the danger level
     */
    public static String getDangerLevelDescription(int chunkDistance) {
        int tier = getTier(chunkDistance);

        if (tier == 0) return "Safe";
        if (tier < 5) return "Moderate";
        if (tier < 10) return "Dangerous";
        if (tier < 50) return "Very Dangerous";
        if (tier < 100) return "Deadly";
        if (tier < 500) return "Extreme";
        if (tier < 1000) return "Catastrophic";
        return "Apocalyptic";
    }

    /**
     * Calculates the mob density cap based on distance from spawn.
     * Creates a smooth progression from low density near spawn to high density far away.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return The maximum number of persistent mobs allowed in a 32-block radius
     */
    public static int getMobDensityCap(int chunkDistance) {
        if (!Config.ENABLE_SCALED_MOB_DENSITY.get()) {
            // Flat cap everywhere if scaling disabled
            return Config.DAYTIME_HOSTILE_MOB_CAP.get();
        }

        int minCap = Config.MIN_MOB_DENSITY.get();
        int maxCap = Config.DAYTIME_HOSTILE_MOB_CAP.get();
        int daytimeHostileStart = Config.DAYTIME_HOSTILE_DISTANCE.get();

        // Before daytime hostile distance, use minimum cap
        if (chunkDistance < daytimeHostileStart) {
            return minCap;
        }

        // Linear scaling from min to max over distance
        // At daytimeHostileStart: minCap
        // At daytimeHostileStart * 5: maxCap
        int scalingDistance = daytimeHostileStart * 5; // Scale over 5x the hostile start distance

        if (chunkDistance >= scalingDistance) {
            return maxCap;
        }

        // Linear interpolation
        double progress = (double)(chunkDistance - daytimeHostileStart) / (scalingDistance - daytimeHostileStart);
        int cap = (int)(minCap + (progress * (maxCap - minCap)));

        return Math.max(minCap, Math.min(maxCap, cap));
    }
}