package com.bleepz.farfromhome;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Spawns powerful boss monsters near players at extreme distances.
 * Creates dramatic, high-stakes encounters in the far reaches of the world.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class BossEncounterSystem {
    
    private static final Random RANDOM = new Random();
    
    // Track last boss spawn time per player to implement cooldowns
    private static final Map<UUID, Long> LAST_BOSS_SPAWN_TIME = new HashMap<>();
    
    // Track how many ticks since we last checked this player (throttling)
    private static final Map<UUID, Integer> PLAYER_CHECK_COOLDOWN = new HashMap<>();
    
    private static final int CHECK_INTERVAL_TICKS = 200; // Check every 10 seconds (20 ticks/sec * 10)
    
    /**
     * Periodically checks if players at extreme distances should spawn bosses.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        // Only run on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        if (!Config.ENABLE_BOSS_ENCOUNTERS.get()) {
            return;
        }
        
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        
        // Throttle checks - don't check every tick
        UUID playerId = player.getUUID();
        int cooldown = PLAYER_CHECK_COOLDOWN.getOrDefault(playerId, 0);
        
        if (cooldown > 0) {
            PLAYER_CHECK_COOLDOWN.put(playerId, cooldown - 1);
            return;
        }
        
        // Reset cooldown for next check
        PLAYER_CHECK_COOLDOWN.put(playerId, CHECK_INTERVAL_TICKS);
        
        // Check if player is at extreme distance
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(player);
        
        if (!DistanceManager.canSpawnBosses(chunkDistance)) {
            return; // Not far enough
        }
        
        // Check cooldown since last boss spawn for this player
        long currentTime = player.level().getGameTime();
        long lastSpawnTime = LAST_BOSS_SPAWN_TIME.getOrDefault(playerId, 0L);
        long timeSinceLastSpawn = currentTime - lastSpawnTime;
        
        if (timeSinceLastSpawn < Config.BOSS_SPAWN_COOLDOWN_TICKS.get()) {
            return; // Still on cooldown
        }
        
        // Roll for boss spawn
        if (RANDOM.nextDouble() > Config.BOSS_SPAWN_CHANCE.get()) {
            return; // No spawn this check
        }
        
        // SUCCESS! Spawn a boss!
        spawnRandomBoss(player, chunkDistance);
        
        // Update last spawn time
        LAST_BOSS_SPAWN_TIME.put(playerId, currentTime);
    }
    
    /**
     * Spawns a random boss near the player with dramatic effects.
     */
    private static void spawnRandomBoss(ServerPlayer player, int chunkDistance) {
        ServerLevel level = player.serverLevel();
        
        // Choose boss type randomly
        BossType bossType = RANDOM.nextBoolean() ? BossType.ENDER_DRAGON : BossType.WARDEN;
        
        // Find spawn position near player (20-40 blocks away)
        Vec3 playerPos = player.position();
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        double distance = 20 + RANDOM.nextDouble() * 20; // 20-40 blocks
        
        double spawnX = playerPos.x + Math.cos(angle) * distance;
        double spawnY = playerPos.y + 10; // Spawn above player
        double spawnZ = playerPos.z + Math.sin(angle) * distance;
        
        BlockPos spawnPos = new BlockPos((int)spawnX, (int)spawnY, (int)spawnZ);
        
        // Find safe ground position
        spawnPos = findSafeSpawnPosition(level, spawnPos);
        
        // Dramatic warning effects
        applyWarningEffects(player, bossType);
        
        // Spawn the boss
        boolean success = false;
        
        switch (bossType) {
            case ENDER_DRAGON -> success = spawnEnderDragon(level, spawnPos, chunkDistance);
            case WARDEN -> success = spawnWarden(level, spawnPos, chunkDistance);
        }
        
        if (success) {
            FarFromHome.LOGGER.warn("BOSS ENCOUNTER: {} spawned near player {} at {} chunks from spawn!", 
                bossType.name(), 
                player.getName().getString(), 
                chunkDistance);
        }
    }
    
    /**
     * Applies dramatic warning effects to the player before boss spawn.
     */
    private static void applyWarningEffects(ServerPlayer player, BossType bossType) {
        // Title message
        String title = bossType == BossType.ENDER_DRAGON ? 
            "§5§lDRAGONIC PRESENCE DETECTED" : 
            "§0§lSOMETHING STIRS BELOW";
        
        String subtitle = "§c§lPREPARE FOR BATTLE!";
        
        player.sendSystemMessage(Component.literal("§c§l========================================"));
        player.sendSystemMessage(Component.literal(title));
        player.sendSystemMessage(Component.literal(subtitle));
        player.sendSystemMessage(Component.literal("§c§l========================================"));
        
        // Sound effect
        if (bossType == BossType.ENDER_DRAGON) {
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.ENDER_DRAGON_GROWL, 
                SoundSource.HOSTILE, 
                1.0f, 
                0.8f);
        } else {
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.WARDEN_EMERGE, 
                SoundSource.HOSTILE, 
                1.0f, 
                1.0f);
        }
        
        // Brief darkness effect for ambiance
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false, true));
    }
    
    /**
     * Spawns an Ender Dragon at the given position.
     */
    private static boolean spawnEnderDragon(ServerLevel level, BlockPos pos, int chunkDistance) {
        EnderDragon dragon = EntityType.ENDER_DRAGON.create(level);
        
        if (dragon == null) {
            return false;
        }
        
        // Position the dragon
        dragon.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 
            RANDOM.nextFloat() * 360, 0);
        
        // Make dragon stronger based on distance (optional)
        int tier = DistanceManager.getTier(chunkDistance);
        double healthBonus = 1.0 + (tier * 0.1); // 10% more health per tier
        
        if (dragon.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) != null) {
            double baseHealth = dragon.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue();
            dragon.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(baseHealth * healthBonus);
            dragon.setHealth((float)(baseHealth * healthBonus));
        }
        
        // Set custom name
        dragon.setCustomName(Component.literal("§5§l" + generateDragonName()));
        dragon.setCustomNameVisible(true);
        
        // Add to world
        return level.addFreshEntity(dragon);
    }
    
    /**
     * Spawns a Warden at the given position.
     */
    private static boolean spawnWarden(ServerLevel level, BlockPos pos, int chunkDistance) {
        Warden warden = EntityType.WARDEN.create(level);
        
        if (warden == null) {
            return false;
        }
        
        // Position the warden at ground level
        BlockPos groundPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        
        warden.moveTo(groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() + 0.5, 
            RANDOM.nextFloat() * 360, 0);
        
        // Make warden stronger based on distance
        int tier = DistanceManager.getTier(chunkDistance);
        double healthBonus = 1.0 + (tier * 0.15); // 15% more health per tier
        
        if (warden.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) != null) {
            double baseHealth = warden.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue();
            warden.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(baseHealth * healthBonus);
            warden.setHealth((float)(baseHealth * healthBonus));
        }
        
        // Set custom name
        warden.setCustomName(Component.literal("§0§l" + generateWardenName()));
        warden.setCustomNameVisible(true);
        
        // Add to world
        return level.addFreshEntity(warden);
    }
    
    /**
     * Finds a safe spawn position (adjusts Y to ground level).
     */
    private static BlockPos findSafeSpawnPosition(ServerLevel level, BlockPos startPos) {
        // Get highest solid block at this X/Z position
        BlockPos groundPos = level.getHeightmapPos(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 
            startPos
        );
        
        // Add a few blocks up to avoid spawning inside ground
        return groundPos.above(2);
    }
    
    /**
     * Generates a dramatic name for an ender dragon.
     */
    private static String generateDragonName() {
        String[] prefixes = {"Ancient", "Dread", "Shadow", "Void", "Chaos", "Eternal", "Dark", "Fallen"};
        String[] suffixes = {"Wyrm", "Drake", "Scourge", "Doom", "Terror", "Bane", "Destroyer", "Reaper"};
        
        return prefixes[RANDOM.nextInt(prefixes.length)] + " " + 
               suffixes[RANDOM.nextInt(suffixes.length)];
    }
    
    /**
     * Generates a dramatic name for a warden.
     */
    private static String generateWardenName() {
        String[] names = {
            "Depths Guardian", 
            "Ancient Sentinel", 
            "Deep Horror",
            "Primordial Watcher",
            "Abyss Walker",
            "Stone Colossus",
            "Dark Warden",
            "Eternal Guardian"
        };
        
        return names[RANDOM.nextInt(names.length)];
    }
    
    /**
     * Boss types that can spawn.
     */
    private enum BossType {
        ENDER_DRAGON,
        WARDEN
    }
}
