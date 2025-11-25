package com.bleepz.farfromhome;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Monster Scaling Settings
    public static final ModConfigSpec.IntValue CHUNKS_PER_TIER;
    public static final ModConfigSpec.IntValue MIN_MONSTER_LEVEL;
    public static final ModConfigSpec.IntValue MAX_MONSTER_LEVEL_PER_TIER;
    public static final ModConfigSpec.DoubleValue HEALTH_SCALING_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue DAMAGE_SCALING_PER_LEVEL;
    public static final ModConfigSpec.BooleanValue ENABLE_MONSTER_SCALING;
    public static final ModConfigSpec.BooleanValue SHOW_VISUAL_INDICATORS;

    // Gear Spawning Settings
    public static final ModConfigSpec.DoubleValue BASE_GEAR_CHANCE;
    public static final ModConfigSpec.DoubleValue GEAR_CHANCE_PER_TIER;
    public static final ModConfigSpec.DoubleValue MAX_GEAR_CHANCE;

    // Rare Monster Buffs
    public static final ModConfigSpec.BooleanValue ENABLE_RARE_BUFFS;
    public static final ModConfigSpec.DoubleValue RARE_BUFF_CHANCE;
    public static final ModConfigSpec.IntValue MAX_BUFFS_PER_MOB;

    // Ore Reward Settings
    public static final ModConfigSpec.BooleanValue ENABLE_ORE_SCALING;
    public static final ModConfigSpec.DoubleValue ORE_DROP_MULTIPLIER_PER_TIER;
    public static final ModConfigSpec.DoubleValue ORE_XP_MULTIPLIER_PER_TIER;
    public static final ModConfigSpec.DoubleValue MAX_ORE_DROP_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_ORE_XP_MULTIPLIER;
    public static final ModConfigSpec.IntValue DIAMOND_SPAWN_DISTANCE;

    // Daytime Hostiles
    public static final ModConfigSpec.BooleanValue ENABLE_DAYTIME_HOSTILES;
    public static final ModConfigSpec.IntValue DAYTIME_HOSTILE_DISTANCE;
    public static final ModConfigSpec.IntValue DAYTIME_HOSTILE_MOB_CAP;
    public static final ModConfigSpec.IntValue MIN_MOB_DENSITY;
    public static final ModConfigSpec.BooleanValue ENABLE_SCALED_MOB_DENSITY;

    // Boss Encounters
    public static final ModConfigSpec.BooleanValue ENABLE_BOSS_ENCOUNTERS;
    public static final ModConfigSpec.IntValue BOSS_SPAWN_DISTANCE;
    public static final ModConfigSpec.DoubleValue BOSS_SPAWN_CHANCE;
    public static final ModConfigSpec.IntValue BOSS_SPAWN_COOLDOWN_TICKS;

    // Structure Modifications
    public static final ModConfigSpec.BooleanValue ENABLE_STRUCTURE_MODIFICATIONS;
    public static final ModConfigSpec.IntValue ABANDONED_VILLAGE_INCREASE_DISTANCE;
    public static final ModConfigSpec.DoubleValue ABANDONED_VILLAGE_CHANCE_MULTIPLIER;

    // Spawn Protection
    public static final ModConfigSpec.BooleanValue ENABLE_SPAWN_PROTECTION;
    public static final ModConfigSpec.IntValue SPAWN_PROTECTION_RADIUS;

    // Death Penalties
    public static final ModConfigSpec.BooleanValue ENABLE_DEATH_PENALTIES;
    public static final ModConfigSpec.IntValue DEATH_PENALTY_START_DISTANCE;
    public static final ModConfigSpec.DoubleValue DEATH_PENALTY_START_PERCENT;
    public static final ModConfigSpec.IntValue DEATH_PENALTY_MAX_DISTANCE;
    public static final ModConfigSpec.DoubleValue DEATH_PENALTY_MAX_PERCENT;
    public static final ModConfigSpec.IntValue DEATH_PENALTY_CATASTROPHIC_DISTANCE;
    public static final ModConfigSpec.DoubleValue DEATH_PENALTY_CATASTROPHIC_CHANCE;
    public static final ModConfigSpec.DoubleValue DEATH_PENALTY_CATASTROPHIC_PERCENT;

    // Loot Bags
    public static final ModConfigSpec.BooleanValue ENABLE_LOOT_BAGS;
    public static final ModConfigSpec.DoubleValue LOOT_BAG_BASE_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue LOOT_BAG_CHANCE_PER_TIER;
    public static final ModConfigSpec.DoubleValue LOOT_BAG_MAX_DROP_CHANCE;

    static {
        BUILDER.push("Monster Scaling");

        ENABLE_MONSTER_SCALING = BUILDER
                .comment("Enable monster level scaling based on distance from spawn")
                .define("enableMonsterScaling", true);

        CHUNKS_PER_TIER = BUILDER
                .comment("Number of chunks for each difficulty tier (default: 100)")
                .defineInRange("chunksPerTier", 100, 1, 10000);

        MIN_MONSTER_LEVEL = BUILDER
                .comment("Minimum monster level at spawn (default: 1)")
                .defineInRange("minMonsterLevel", 1, 1, 100);

        MAX_MONSTER_LEVEL_PER_TIER = BUILDER
                .comment("Maximum monster level increase per tier (default: 2)")
                .defineInRange("maxMonsterLevelPerTier", 2, 1, 50);

        HEALTH_SCALING_PER_LEVEL = BUILDER
                .comment("Health multiplier per monster level (default: 0.1 = 10% per level)")
                .defineInRange("healthScalingPerLevel", 0.1, 0.0, 5.0);

        DAMAGE_SCALING_PER_LEVEL = BUILDER
                .comment("Damage multiplier per monster level (default: 0.08 = 8% per level)")
                .defineInRange("damageScalingPerLevel", 0.08, 0.0, 5.0);

        SHOW_VISUAL_INDICATORS = BUILDER
                .comment("Show visual indicators like name tags with levels (disable if using mods like Neat or Damage Indicators)")
                .define("showVisualIndicators", true);

        BUILDER.pop();

        BUILDER.push("Gear Spawning");

        BASE_GEAR_CHANCE = BUILDER
                .comment("Base chance for monsters to spawn with gear (default: 0.05 = 5%)")
                .defineInRange("baseGearChance", 0.05, 0.0, 1.0);

        GEAR_CHANCE_PER_TIER = BUILDER
                .comment("Additional gear chance per tier (default: 0.02 = 2%)")
                .defineInRange("gearChancePerTier", 0.02, 0.0, 1.0);

        MAX_GEAR_CHANCE = BUILDER
                .comment("Maximum gear chance cap (default: 0.75 = 75%)")
                .defineInRange("maxGearChance", 0.75, 0.0, 1.0);

        BUILDER.pop();

        BUILDER.push("Rare Monster Buffs");

        ENABLE_RARE_BUFFS = BUILDER
                .comment("Enable rare monsters with special buffs")
                .define("enableRareBuffs", true);

        RARE_BUFF_CHANCE = BUILDER
                .comment("Chance for a monster to spawn with buffs (default: 0.03 = 3%)")
                .defineInRange("rareBuffChance", 0.03, 0.0, 1.0);

        MAX_BUFFS_PER_MOB = BUILDER
                .comment("Maximum number of buffs per rare monster (default: 3)")
                .defineInRange("maxBuffsPerMob", 3, 1, 10);

        BUILDER.pop();

        BUILDER.push("Ore Scaling");

        ENABLE_ORE_SCALING = BUILDER
                .comment("Enable ore drop and XP scaling based on distance")
                .define("enableOreScaling", true);

        ORE_DROP_MULTIPLIER_PER_TIER = BUILDER
                .comment("Ore drop multiplier increase per tier (default: 0.1 = 10%)")
                .defineInRange("oreDropMultiplierPerTier", 0.1, 0.0, 10.0);

        ORE_XP_MULTIPLIER_PER_TIER = BUILDER
                .comment("Ore XP multiplier increase per tier (default: 0.15 = 15%)")
                .defineInRange("oreXpMultiplierPerTier", 0.15, 0.0, 10.0);

        MAX_ORE_DROP_MULTIPLIER = BUILDER
                .comment("Maximum ore drop multiplier (default: 6.0 = 600%)")
                .defineInRange("maxOreDropMultiplier", 6.0, 1.0, 100.0);

        MAX_ORE_XP_MULTIPLIER = BUILDER
                .comment("Maximum ore XP multiplier (default: 10.0 = 1000%)")
                .defineInRange("maxOreXpMultiplier", 10.0, 1.0, 100.0);

        DIAMOND_SPAWN_DISTANCE = BUILDER
                .comment("Distance in chunks defining spawn penalty zone (default: 312 = ~5000 blocks). Within this zone: diamonds 99% fail, other ores 25% fail. Rates scale to 0% at this distance.")
                .defineInRange("diamondSpawnDistance", 312, 0, 1000000);

        BUILDER.pop();

        BUILDER.push("Daytime Hostiles");

        ENABLE_DAYTIME_HOSTILES = BUILDER
                .comment("Enable hostile mobs during daytime at extreme distances")
                .define("enableDaytimeHostiles", true);

        DAYTIME_HOSTILE_DISTANCE = BUILDER
                .comment("Distance in chunks where daytime hostiles start (default: 10000)")
                .defineInRange("daytimeHostileDistance", 10000, 0, 1000000);

        DAYTIME_HOSTILE_MOB_CAP = BUILDER
                .comment("Maximum persistent mobs allowed within 32 blocks to prevent lag (default: 50)")
                .defineInRange("daytimeHostileMobCap", 50, 10, 500);

        ENABLE_SCALED_MOB_DENSITY = BUILDER
                .comment("Scale mob density based on distance (default: true). When enabled, mob cap increases gradually with distance")
                .define("enableScaledMobDensity", true);

        MIN_MOB_DENSITY = BUILDER
                .comment("Minimum mob cap near spawn when scaled density is enabled (default: 5)")
                .defineInRange("minMobDensity", 5, 1, 50);

        BUILDER.pop();

        BUILDER.push("Boss Encounters");

        ENABLE_BOSS_ENCOUNTERS = BUILDER
                .comment("Enable random boss spawns at extreme distances")
                .define("enableBossEncounters", true);

        BOSS_SPAWN_DISTANCE = BUILDER
                .comment("Distance in chunks where bosses can spawn (default: 100000)")
                .defineInRange("bossSpawnDistance", 100000, 0, 10000000);

        BOSS_SPAWN_CHANCE = BUILDER
                .comment("Chance per check for a boss to spawn (default: 0.0001 = 0.01%)")
                .defineInRange("bossSpawnChance", 0.0001, 0.0, 1.0);

        BOSS_SPAWN_COOLDOWN_TICKS = BUILDER
                .comment("Cooldown between boss spawn checks in ticks (default: 12000 = 10 minutes)")
                .defineInRange("bossSpawnCooldownTicks", 12000, 0, 72000);

        BUILDER.pop();

        BUILDER.push("Structure Modifications");

        ENABLE_STRUCTURE_MODIFICATIONS = BUILDER
                .comment("Enable structure modifications based on distance")
                .define("enableStructureModifications", true);

        ABANDONED_VILLAGE_INCREASE_DISTANCE = BUILDER
                .comment("Distance in chunks where abandoned villages become more common (default: 1000)")
                .defineInRange("abandonedVillageIncreaseDistance", 1000, 0, 100000);

        ABANDONED_VILLAGE_CHANCE_MULTIPLIER = BUILDER
                .comment("Multiplier for abandoned village chance per tier (default: 1.5)")
                .defineInRange("abandonedVillageChanceMultiplier", 1.5, 1.0, 10.0);

        BUILDER.pop();

        BUILDER.push("Spawn Protection");

        ENABLE_SPAWN_PROTECTION = BUILDER
                .comment("Enable spawn protection zone where PvP is disabled")
                .define("enableSpawnProtection", true);

        SPAWN_PROTECTION_RADIUS = BUILDER
                .comment("Radius in blocks around spawn where PvP is disabled (default: 1000)")
                .defineInRange("spawnProtectionRadius", 1000, 0, 100000);

        BUILDER.pop();

        BUILDER.push("Death Penalties");

        ENABLE_DEATH_PENALTIES = BUILDER
                .comment("Enable distance-based death penalties")
                .define("enableDeathPenalties", true);

        DEATH_PENALTY_START_DISTANCE = BUILDER
                .comment("Distance in chunks where death penalties begin (default: 62 = 1000 blocks)")
                .defineInRange("deathPenaltyStartDistance", 62, 0, 1000000);

        DEATH_PENALTY_START_PERCENT = BUILDER
                .comment("Percentage of XP lost at start distance (default: 0.10 = 10%)")
                .defineInRange("deathPenaltyStartPercent", 0.10, 0.0, 1.0);

        DEATH_PENALTY_MAX_DISTANCE = BUILDER
                .comment("Distance in chunks where max penalty is reached (default: 625 = 10000 blocks)")
                .defineInRange("deathPenaltyMaxDistance", 625, 0, 1000000);

        DEATH_PENALTY_MAX_PERCENT = BUILDER
                .comment("Maximum percentage of XP lost at max distance (default: 0.25 = 25%)")
                .defineInRange("deathPenaltyMaxPercent", 0.25, 0.0, 1.0);

        DEATH_PENALTY_CATASTROPHIC_DISTANCE = BUILDER
                .comment("Distance in chunks where catastrophic loss can occur (default: 625 = 10000 blocks)")
                .defineInRange("deathPenaltyCatastrophicDistance", 625, 0, 1000000);

        DEATH_PENALTY_CATASTROPHIC_CHANCE = BUILDER
                .comment("Chance for catastrophic XP loss at extreme distances (default: 0.05 = 5%)")
                .defineInRange("deathPenaltyCatastrophicChance", 0.05, 0.0, 1.0);

        DEATH_PENALTY_CATASTROPHIC_PERCENT = BUILDER
                .comment("Percentage of XP lost on catastrophic loss (default: 0.80 = 80%)")
                .defineInRange("deathPenaltyCatastrophicPercent", 0.80, 0.0, 1.0);

        BUILDER.pop();

        BUILDER.push("Loot Bags");

        ENABLE_LOOT_BAGS = BUILDER
                .comment("Enable loot bag drops from monsters")
                .define("enableLootBags", true);

        LOOT_BAG_BASE_DROP_CHANCE = BUILDER
                .comment("Base chance for loot bags to drop (default: 0.01 = 1%)")
                .defineInRange("lootBagBaseDropChance", 0.01, 0.0, 1.0);

        LOOT_BAG_CHANCE_PER_TIER = BUILDER
                .comment("Additional drop chance per tier (default: 0.005 = 0.5%)")
                .defineInRange("lootBagChancePerTier", 0.005, 0.0, 1.0);

        LOOT_BAG_MAX_DROP_CHANCE = BUILDER
                .comment("Maximum loot bag drop chance cap (default: 0.25 = 25%)")
                .defineInRange("lootBagMaxDropChance", 0.25, 0.0, 1.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}