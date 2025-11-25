package com.bleepz.farfromhome;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Objects;
import java.util.Random;

/**
 * Handles all monster scaling mechanics based on distance from spawn.
 * This includes attribute scaling, gear spawning, and visual indicators.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class MonsterScalingSystem {

    private static final Random RANDOM = new Random();

    /**
     * Main event handler that triggers when any entity joins the world.
     * We filter for hostile mobs and apply scaling based on their spawn location.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Only run on server side - client just renders what server tells it
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Check if monster scaling is enabled
        if (!Config.ENABLE_MONSTER_SCALING.get()) {
            return;
        }

        // Only apply to hostile mobs (Monster class covers most hostiles)
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        // Calculate distance from spawn
        int chunkDistance = DistanceManager.getChunkDistanceFromSpawn(monster);

        // Get the monster level for this distance
        int monsterLevel = DistanceManager.getMonsterLevel(chunkDistance);

        // Apply scaling
        applyLevelScaling(monster, monsterLevel, chunkDistance);

        // Apply gear based on distance
        applyGear(monster, chunkDistance);

        // Add visual indicator if enabled
        if (Config.SHOW_VISUAL_INDICATORS.get()) {
            applyVisualIndicator(monster, monsterLevel);
        }

        // Log for debugging (can be removed later)
        FarFromHome.LOGGER.debug("Spawned {} at {} chunks from spawn with level {}",
            monster.getType().getDescription().getString(),
            chunkDistance,
            monsterLevel);
    }

    /**
     * Applies attribute scaling to the monster based on its level.
     * Scales health, damage, knockback resistance, and movement speed.
     */
    private static void applyLevelScaling(Monster monster, int level, int chunkDistance) {
        // Calculate multipliers
        double healthMultiplier = 1.0 + (level * Config.HEALTH_SCALING_PER_LEVEL.get());
        double damageMultiplier = 1.0 + (level * Config.DAMAGE_SCALING_PER_LEVEL.get());

        // Apply health scaling
        var healthAttribute = monster.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            double baseHealth = healthAttribute.getBaseValue();
            double newHealth = baseHealth * healthMultiplier;
            healthAttribute.setBaseValue(newHealth);
            monster.setHealth((float) newHealth);
        }

        // Apply damage scaling
        var damageAttribute = monster.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute != null) {
            double baseDamage = damageAttribute.getBaseValue();
            double newDamage = baseDamage * damageMultiplier;
            damageAttribute.setBaseValue(newDamage);
        }

        // Slight knockback resistance increase for higher levels
        if (level > 5) {
            var knockbackAttribute = monster.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (knockbackAttribute != null) {
                double baseKnockback = knockbackAttribute.getBaseValue();
                double knockbackBonus = Math.min(0.5, level * 0.02);
                knockbackAttribute.setBaseValue(baseKnockback + knockbackBonus);
            }
        }

        // Slight movement speed increase for distant mobs
        if (chunkDistance > 500) {
            var speedAttribute = monster.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                double baseSpeed = speedAttribute.getBaseValue();
                double speedBonus = Math.min(0.1, chunkDistance * 0.00001);
                speedAttribute.setBaseValue(baseSpeed + speedBonus);
            }
        }
    }

    /**
     * Applies gear to monsters based on distance from spawn.
     * Further from spawn = better gear and higher chance of having it.
     */
    private static void applyGear(Monster monster, int chunkDistance) {
        double gearChance = DistanceManager.getGearChance(chunkDistance);

        // Roll for gear spawn
        if (RANDOM.nextDouble() > gearChance) {
            return; // No gear this time
        }

        // Determine gear quality based on tier
        int tier = DistanceManager.getTier(chunkDistance);

        // Apply armor (chance for each piece)
        if (RANDOM.nextDouble() < 0.7) { // 70% chance for helmet
            monster.setItemSlot(EquipmentSlot.HEAD, getArmorForTier(tier, EquipmentSlot.HEAD));
        }
        if (RANDOM.nextDouble() < 0.6) { // 60% chance for chestplate
            monster.setItemSlot(EquipmentSlot.CHEST, getArmorForTier(tier, EquipmentSlot.CHEST));
        }
        if (RANDOM.nextDouble() < 0.6) { // 60% chance for leggings
            monster.setItemSlot(EquipmentSlot.LEGS, getArmorForTier(tier, EquipmentSlot.LEGS));
        }
        if (RANDOM.nextDouble() < 0.5) { // 50% chance for boots
            monster.setItemSlot(EquipmentSlot.FEET, getArmorForTier(tier, EquipmentSlot.FEET));
        }

        // Apply weapon if the mob can hold items
        if (RANDOM.nextDouble() < 0.5) { // 50% chance for weapon
            monster.setItemSlot(EquipmentSlot.MAINHAND, getWeaponForTier(tier));
        }

        // Prevent gear from dropping (optional - makes gear less exploitable)
        // You can adjust these drop chances or remove this entirely
        monster.setDropChance(EquipmentSlot.HEAD, 0.1f);
        monster.setDropChance(EquipmentSlot.CHEST, 0.1f);
        monster.setDropChance(EquipmentSlot.LEGS, 0.1f);
        monster.setDropChance(EquipmentSlot.FEET, 0.1f);
        monster.setDropChance(EquipmentSlot.MAINHAND, 0.15f);
    }

    /**
     * Returns appropriate armor for the given tier and slot.
     */
    private static ItemStack getArmorForTier(int tier, EquipmentSlot slot) {
        // Tier 0-2: Leather
        // Tier 3-5: Chainmail/Gold
        // Tier 6-10: Iron
        // Tier 11+: Diamond
        // Tier 50+: Netherite (very rare, extreme distance)

        if (tier >= 50 && RANDOM.nextDouble() < 0.3) { // 30% chance at extreme distances
            return switch (slot) {
                case HEAD -> new ItemStack(Items.NETHERITE_HELMET);
                case CHEST -> new ItemStack(Items.NETHERITE_CHESTPLATE);
                case LEGS -> new ItemStack(Items.NETHERITE_LEGGINGS);
                case FEET -> new ItemStack(Items.NETHERITE_BOOTS);
                default -> ItemStack.EMPTY;
            };
        } else if (tier >= 11) {
            return switch (slot) {
                case HEAD -> new ItemStack(Items.DIAMOND_HELMET);
                case CHEST -> new ItemStack(Items.DIAMOND_CHESTPLATE);
                case LEGS -> new ItemStack(Items.DIAMOND_LEGGINGS);
                case FEET -> new ItemStack(Items.DIAMOND_BOOTS);
                default -> ItemStack.EMPTY;
            };
        } else if (tier >= 6) {
            return switch (slot) {
                case HEAD -> new ItemStack(Items.IRON_HELMET);
                case CHEST -> new ItemStack(Items.IRON_CHESTPLATE);
                case LEGS -> new ItemStack(Items.IRON_LEGGINGS);
                case FEET -> new ItemStack(Items.IRON_BOOTS);
                default -> ItemStack.EMPTY;
            };
        } else if (tier >= 3) {
            // Mix of chainmail and gold
            if (RANDOM.nextBoolean()) {
                return switch (slot) {
                    case HEAD -> new ItemStack(Items.CHAINMAIL_HELMET);
                    case CHEST -> new ItemStack(Items.CHAINMAIL_CHESTPLATE);
                    case LEGS -> new ItemStack(Items.CHAINMAIL_LEGGINGS);
                    case FEET -> new ItemStack(Items.CHAINMAIL_BOOTS);
                    default -> ItemStack.EMPTY;
                };
            } else {
                return switch (slot) {
                    case HEAD -> new ItemStack(Items.GOLDEN_HELMET);
                    case CHEST -> new ItemStack(Items.GOLDEN_CHESTPLATE);
                    case LEGS -> new ItemStack(Items.GOLDEN_LEGGINGS);
                    case FEET -> new ItemStack(Items.GOLDEN_BOOTS);
                    default -> ItemStack.EMPTY;
                };
            }
        } else {
            // Leather armor for low tiers
            return switch (slot) {
                case HEAD -> new ItemStack(Items.LEATHER_HELMET);
                case CHEST -> new ItemStack(Items.LEATHER_CHESTPLATE);
                case LEGS -> new ItemStack(Items.LEATHER_LEGGINGS);
                case FEET -> new ItemStack(Items.LEATHER_BOOTS);
                default -> ItemStack.EMPTY;
            };
        }
    }

    /**
     * Returns appropriate weapon for the given tier.
     */
    private static ItemStack getWeaponForTier(int tier) {
        if (tier >= 50 && RANDOM.nextDouble() < 0.3) {
            return new ItemStack(Items.NETHERITE_SWORD);
        } else if (tier >= 11) {
            return new ItemStack(Items.DIAMOND_SWORD);
        } else if (tier >= 6) {
            return new ItemStack(Items.IRON_SWORD);
        } else if (tier >= 3) {
            return RANDOM.nextBoolean() ? new ItemStack(Items.GOLDEN_SWORD) : new ItemStack(Items.IRON_SWORD);
        } else {
            return new ItemStack(Items.STONE_SWORD);
        }
    }

    /**
     * Adds a visual indicator (name tag) showing the monster's level.
     * Format: "Zombie [Lv 5]"
     */
    private static void applyVisualIndicator(Monster monster, int level) {
        // Get the monster's default name
        String mobName = monster.getType().getDescription().getString();

        // Create custom name with level
        Component customName = Component.literal(mobName + " §e[Lv " + level + "]");

        // Apply the custom name
        monster.setCustomName(customName);
        monster.setCustomNameVisible(true);
    }
}
