package com.bleepz.farfromhome;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Protects the spawn area from PvP and other hostile actions.
 * Configurable radius to match server spawn protection needs.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class SpawnProtectionSystem {
    
    /**
     * Prevents player damage in the spawn protection zone.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        // Only run on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        // Check if spawn protection is enabled
        if (!Config.ENABLE_SPAWN_PROTECTION.get()) {
            return;
        }
        
        // Only protect players
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        
        // Check if damage source is from another player
        var source = event.getSource();
        if (source.getEntity() instanceof Player attacker) {
            
            // Check if victim is in spawn protection zone
            if (isInSpawnProtectionZone(victim)) {
                // Cancel the damage
                event.setCanceled(true);
                
                // Notify the attacker
                attacker.displayClientMessage(
                    Component.literal("§cPvP is disabled in the spawn protection zone!"), 
                    true // Show in action bar
                );
                
                FarFromHome.LOGGER.debug("Blocked PvP damage from {} to {} in spawn zone", 
                    attacker.getName().getString(), 
                    victim.getName().getString());
                
                return;
            }
            
            // Also check if attacker is in spawn zone (prevents spawn camping)
            if (isInSpawnProtectionZone(attacker)) {
                event.setCanceled(true);
                
                attacker.displayClientMessage(
                    Component.literal("§cYou cannot attack from the spawn protection zone!"), 
                    true
                );
                
                FarFromHome.LOGGER.debug("Blocked PvP attack from {} (in spawn zone) to {}", 
                    attacker.getName().getString(), 
                    victim.getName().getString());
            }
        }
    }
    
    /**
     * Checks if a player is within the spawn protection radius.
     */
    private static boolean isInSpawnProtectionZone(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(player);
        int protectionChunks = Config.SPAWN_PROTECTION_RADIUS.get() / 16; // Convert blocks to chunks
        
        return chunkDistance <= protectionChunks;
    }
    
    /**
     * Helper method to check if a position is in the spawn protection zone.
     * Can be used by other systems if needed.
     */
    public static boolean isInSpawnProtectionZone(ServerLevel level, int chunkDistance) {
        if (!Config.ENABLE_SPAWN_PROTECTION.get()) {
            return false;
        }
        
        int protectionChunks = Config.SPAWN_PROTECTION_RADIUS.get() / 16;
        return chunkDistance <= protectionChunks;
    }
}
