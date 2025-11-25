package com.bleepz.farfromhome;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Random;

/**
 * Implements distance-based death penalties to create risk/reward gameplay.
 * The further from spawn you die, the more experience you lose.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class DeathPenaltySystem {

    private static final Random RANDOM = new Random();

    /**
     * Captures player state before death to calculate penalties.
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!Config.ENABLE_DEATH_PENALTIES.get()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Calculate distance from spawn
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(player);

        // Get player's total XP
        int totalXP = getTotalExperience(player);

        // Calculate penalty
        double penaltyPercent = calculateDeathPenalty(chunkDistance);

        // Store data for clone event
        player.getPersistentData().putInt("FarFromHome_PreDeathXP", totalXP);
        player.getPersistentData().putInt("FarFromHome_DeathChunkDistance", chunkDistance);
        player.getPersistentData().putDouble("FarFromHome_PenaltyPercent", penaltyPercent);

        FarFromHome.LOGGER.debug("Player {} died at {} chunks with {} XP, penalty: {}%",
                player.getName().getString(),
                chunkDistance,
                totalXP,
                (int)(penaltyPercent * 100));
    }

    /**
     * Handles XP restoration/penalty when player is cloned (respawns).
     * This event fires when the old player entity is being copied to the new one.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!Config.ENABLE_DEATH_PENALTIES.get()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        // Get the old player's stored death data
        var oldData = event.getOriginal().getPersistentData();

        if (!oldData.contains("FarFromHome_PreDeathXP")) {
            return; // No death data
        }

        int preDeathXP = oldData.getInt("FarFromHome_PreDeathXP");
        int deathChunkDistance = oldData.getInt("FarFromHome_DeathChunkDistance");
        double penaltyPercent = oldData.getDouble("FarFromHome_PenaltyPercent");

        // Calculate XP to keep
        int xpToLose = (int) (preDeathXP * penaltyPercent);
        int xpToKeep = preDeathXP - xpToLose;

        // Set the new player's XP
        newPlayer.totalExperience = 0;
        newPlayer.experienceLevel = 0;
        newPlayer.experienceProgress = 0;

        if (xpToKeep > 0) {
            newPlayer.giveExperiencePoints(xpToKeep);
        }

        // Prepare message for the player
        if (penaltyPercent <= 0) {
            newPlayer.getPersistentData().putString("FarFromHome_DeathMessage",
                    "§aYou died in the safe zone - no experience lost!");

            FarFromHome.LOGGER.info("Player {} died in safe zone - kept all {} XP",
                    newPlayer.getName().getString(), preDeathXP);
        } else {
            newPlayer.getPersistentData().putString("FarFromHome_DeathMessage",
                    String.format("§cYou lost %d%% of your experience (%d XP) for dying %,d blocks from spawn!",
                            (int)(penaltyPercent * 100),
                            xpToLose,
                            deathChunkDistance * 16));

            FarFromHome.LOGGER.info("Player {} lost {} XP ({}%), kept {} XP",
                    newPlayer.getName().getString(),
                    xpToLose,
                    (int)(penaltyPercent * 100),
                    xpToKeep);
        }
    }

    /**
     * Shows the death message when player respawns.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Show death message if one exists
        var data = player.getPersistentData();
        if (data.contains("FarFromHome_DeathMessage")) {
            String message = data.getString("FarFromHome_DeathMessage");
            player.displayClientMessage(Component.literal(message), false);
            data.remove("FarFromHome_DeathMessage");
        }
    }

    /**
     * Calculates the death penalty percentage based on distance from spawn.
     *
     * @param chunkDistance Distance in chunks from spawn
     * @return Percentage of XP to lose (0.0 to 1.0)
     */
    private static double calculateDeathPenalty(int chunkDistance) {
        int safeDistance = Config.DEATH_PENALTY_START_DISTANCE.get();
        int maxPenaltyDistance = Config.DEATH_PENALTY_MAX_DISTANCE.get();
        int catastrophicDistance = Config.DEATH_PENALTY_CATASTROPHIC_DISTANCE.get();

        // No penalty in safe zone
        if (chunkDistance < safeDistance) {
            return 0.0;
        }

        // Check for catastrophic loss (5% chance at extreme distances)
        if (chunkDistance >= catastrophicDistance) {
            if (RANDOM.nextDouble() < Config.DEATH_PENALTY_CATASTROPHIC_CHANCE.get()) {
                FarFromHome.LOGGER.warn("CATASTROPHIC LOSS triggered at {} chunks!", chunkDistance);
                return Config.DEATH_PENALTY_CATASTROPHIC_PERCENT.get();
            }
        }

        // Linear scaling from safe zone to max penalty zone
        if (chunkDistance >= maxPenaltyDistance) {
            return Config.DEATH_PENALTY_MAX_PERCENT.get();
        }

        // Linear interpolation between start and max
        double progressToMax = (double)(chunkDistance - safeDistance) / (maxPenaltyDistance - safeDistance);
        double startPercent = Config.DEATH_PENALTY_START_PERCENT.get();
        double maxPercent = Config.DEATH_PENALTY_MAX_PERCENT.get();

        return startPercent + (progressToMax * (maxPercent - startPercent));
    }

    /**
     * Calculates total experience points from level and progress.
     */
    private static int getTotalExperience(Player player) {
        int level = player.experienceLevel;
        float progress = player.experienceProgress;

        int xpFromLevels = 0;
        for (int i = 0; i < level; i++) {
            xpFromLevels += getXPForLevel(i);
        }

        int xpForCurrentLevel = getXPForLevel(level);
        int partialXP = (int) (xpForCurrentLevel * progress);

        return xpFromLevels + partialXP;
    }

    /**
     * Gets XP required to advance from a given level to the next.
     */
    private static int getXPForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + level * 2;
        }
    }

    /**
     * Gets a description of the current death penalty at a given distance.
     */
    public static String getDeathPenaltyDescription(int chunkDistance) {
        double penalty = calculateDeathPenalty(chunkDistance);

        if (penalty == 0) {
            return "§aSafe Zone - No XP loss on death";
        }

        int penaltyPercent = (int)(penalty * 100);
        int catastrophicDistance = Config.DEATH_PENALTY_CATASTROPHIC_DISTANCE.get();

        if (chunkDistance >= catastrophicDistance) {
            return String.format("§c%d%% XP loss + §45%% chance of 80%% loss!", penaltyPercent);
        }

        return String.format("§e%d%% XP loss on death", penaltyPercent);
    }
}