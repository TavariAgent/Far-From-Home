package com.bleepz.farfromhome;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles rare monster spawns with special buffs and abilities.
 * Rare monsters have enhanced abilities and can drop special gear.
 */
@EventBusSubscriber(modid = FarFromHome.MODID)
public class RareBuffSystem {

    private static final Random RANDOM = new Random();

    // NBT keys for storing buff data
    private static final String NBT_RARE_BUFFS = "FarFromHome_RareBuffs";
    private static final String NBT_BUFF_COUNT = "FarFromHome_BuffCount";
    private static final String NBT_HAS_BOUNCEBACK = "FarFromHome_Bounceback";
    private static final String NBT_HAS_THORNS = "FarFromHome_Thorns";

    // List of beneficial potion effects that can be applied
    private static final List<Holder<MobEffect>> BENEFICIAL_EFFECTS = List.of(
        MobEffects.MOVEMENT_SPEED,        // Speed
        MobEffects.DIG_SPEED,             // Haste
        MobEffects.DAMAGE_BOOST,          // Strength
        MobEffects.JUMP,                  // Jump Boost
        MobEffects.REGENERATION,          // Regeneration
        MobEffects.DAMAGE_RESISTANCE,     // Resistance
        MobEffects.FIRE_RESISTANCE,       // Fire Resistance
        MobEffects.WATER_BREATHING,       // Water Breathing
        MobEffects.INVISIBILITY,          // Invisibility (spooky!)
        MobEffects.NIGHT_VISION,          // Night Vision
        MobEffects.ABSORPTION,            // Absorption (extra hearts)
        MobEffects.SATURATION,            // Saturation
        MobEffects.HEALTH_BOOST           // Health Boost
    );

    /**
     * Checks for rare buff spawns after monster scaling is applied.
     * This runs with lower priority to ensure it happens after MonsterScalingSystem.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Only run on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Check if rare buffs are enabled
        if (!Config.ENABLE_RARE_BUFFS.get()) {
            return;
        }

        // Only apply to hostile mobs
        if (!(event.getEntity() instanceof Monster monster)) {
            return;
        }

        // Roll for rare spawn
        if (RANDOM.nextDouble() > Config.RARE_BUFF_CHANCE.get()) {
            return; // Not a rare spawn
        }

        // This is a rare monster! Apply buffs
        applyRareBuffs(monster);
    }

    /**
     * Applies random buffs to a rare monster.
     */
    private static void applyRareBuffs(Monster monster) {
        // Determine number of buffs (1 to MAX_BUFFS_PER_MOB)
        int maxBuffs = Config.MAX_BUFFS_PER_MOB.get();
        int buffCount = 1 + RANDOM.nextInt(maxBuffs);

        List<String> appliedBuffs = new ArrayList<>();

        // Apply random potion effects
        List<Holder<MobEffect>> availableEffects = new ArrayList<>(BENEFICIAL_EFFECTS);
        int potionBuffCount = Math.min(buffCount, availableEffects.size());

        for (int i = 0; i < potionBuffCount; i++) {
            // Pick a random effect and remove it from available list
            Holder<MobEffect> effect = availableEffects.remove(RANDOM.nextInt(availableEffects.size()));

            // Determine amplifier (power level) based on how rare this spawn is
            // More buffs = stronger individual effects
            int amplifier = RANDOM.nextInt(Math.min(3, buffCount)); // 0-2 (or up to buffCount-1)

            // Apply with infinite duration
            monster.addEffect(new MobEffectInstance(effect, -1, amplifier, false, true, true));

            String effectName = getEffectName(effect, amplifier);
            appliedBuffs.add(effectName);
        }

        // Chance to apply custom buffs (Bounce-back and Thorns)
        boolean hasBounceBack = false;
        boolean hasThorns = false;

        if (buffCount >= 2 && RANDOM.nextDouble() < 0.3) { // 30% chance if 2+ buffs
            hasBounceBack = true;
            appliedBuffs.add("§dBounce-back");
        }

        if (buffCount >= 3 && RANDOM.nextDouble() < 0.25) { // 25% chance if 3+ buffs
            hasThorns = true;
            appliedBuffs.add("§cThorns");
        }

        // Store buff data in NBT for persistence and drop calculation
        CompoundTag entityData = monster.getPersistentData();
        entityData.putInt(NBT_BUFF_COUNT, appliedBuffs.size());
        entityData.putBoolean(NBT_HAS_BOUNCEBACK, hasBounceBack);
        entityData.putBoolean(NBT_HAS_THORNS, hasThorns);

        // Store buff names for display
        ListTag buffList = new ListTag();
        for (String buff : appliedBuffs) {
            buffList.add(StringTag.valueOf(buff));
        }
        entityData.put(NBT_RARE_BUFFS, buffList);

        // Apply visual indicators
        applyRareVisuals(monster, appliedBuffs);

        // Make the mob glow (using glowing effect)
        monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0, false, false, true));

        FarFromHome.LOGGER.info("Spawned RARE {} with {} buffs: {}",
            monster.getType().getDescription().getString(),
            appliedBuffs.size(),
            String.join(", ", appliedBuffs));
    }

    /**
     * Applies visual indicators to show this is a rare monster.
     */
    private static void applyRareVisuals(Monster monster, List<String> buffs) {
        if (!Config.SHOW_VISUAL_INDICATORS.get()) {
            return;
        }

        // Get base name (might already have level from MonsterScalingSystem)
        String baseName = monster.hasCustomName() ?
            monster.getCustomName().getString() :
            monster.getType().getDescription().getString();

        // Create fancy rare name with color and star
        Component rareName = Component.literal("§6⭐ §l" + baseName + " §6⭐");

        // Add buff count indicator
        Component fullName = Component.literal(rareName.getString() + " §e(" + buffs.size() + " buffs)");

        monster.setCustomName(fullName);
        monster.setCustomNameVisible(true);
    }

    /**
     * Handles custom buff mechanics when monsters take or deal damage.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // Handle Thorns: reflect melee damage back to attacker
        if (event.getEntity() instanceof Monster monster) {
            CompoundTag data = monster.getPersistentData();

            if (data.getBoolean(NBT_HAS_THORNS)) {
                var source = event.getSource();

                // Check if this is melee damage (direct entity attack)
                if (source.getDirectEntity() != null && source.getEntity() != null) {
                    var attacker = source.getEntity();

                    // Reflect 100% of damage back to attacker
                    float reflectedDamage = event.getOriginalDamage();
                    attacker.hurt(monster.damageSources().thorns(monster), reflectedDamage);

                    // Optional: Reduce damage taken by monster (or set to 0 to only allow ranged)
                    event.setNewDamage(0); // Monster takes no melee damage

                    FarFromHome.LOGGER.debug("Thorns reflected {} damage to {}",
                        reflectedDamage,
                        attacker.getName().getString());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        // Handle Bounce-back: knockback attacker when hit
        if (event.getEntity() instanceof Monster monster) {
            CompoundTag data = monster.getPersistentData();

            if (data.getBoolean(NBT_HAS_BOUNCEBACK)) {
                var source = event.getSource();

                if (source.getEntity() != null) {
                    var attacker = source.getEntity();

                    // Calculate knockback based on buff count (more buffs = stronger knockback)
                    int buffCount = data.getInt(NBT_BUFF_COUNT);
                    double knockbackStrength = 0.5 + (buffCount * 0.2); // 0.5 to 2.5+

                    // Calculate direction from monster to attacker
                    double dx = attacker.getX() - monster.getX();
                    double dz = attacker.getZ() - monster.getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    if (distance > 0) {
                        // Normalize and apply knockback
                        dx /= distance;
                        dz /= distance;

                        attacker.push(dx * knockbackStrength, 0.4, dz * knockbackStrength);
                        attacker.hurtMarked = true; // Force velocity update

                        FarFromHome.LOGGER.debug("Bounce-back knocked {} away with strength {}",
                            attacker.getName().getString(),
                            knockbackStrength);
                    }
                }
            }
        }
    }

    /**
     * Gets a formatted name for a potion effect with its amplifier.
     */
    private static String getEffectName(Holder<MobEffect> effectHolder, int amplifier) {
        MobEffect effect = effectHolder.value();
        String name = effect.getDescriptionId()
            .replace("effect.minecraft.", "")
            .replace("_", " ");

        // Capitalize first letter
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        if (amplifier > 0) {
            name += " " + (amplifier + 1); // Display as I, II, III
        }

        return "§b" + name; // Cyan color for potion effects
    }

    /**
     * Gets the number of buffs a monster has (for drop calculation).
     */
    public static int getBuffCount(Monster monster) {
        return monster.getPersistentData().getInt(NBT_BUFF_COUNT);
    }

    /**
     * Checks if a monster is a rare buffed monster.
     */
    public static boolean isRareMonster(Monster monster) {
        return monster.getPersistentData().contains(NBT_RARE_BUFFS);
    }
}
