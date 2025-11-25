# Far From Home

> *The world scales. The deeper you go, the deadlier it becomes. The loot matches the risk.*

A server-side mod that transforms Minecraft into a distance-based progression system. Spawn is safe. Everything beyond becomes increasingly dangerous—and rewarding.

---

## Quick Navigation

| Section | Description |
|---------|-------------|
| [Features Overview](#features-overview) | What this mod does |
| [Safe Zone](#safe-zone) | Spawn protection and camping penalties |
| [Distance Scaling](#distance-scaling) | How the world gets harder |
| [Monster Modifiers](#monster-modifiers) | Rare enemy abilities |
| [Loot System](#loot-system) | Bags, bottles, and the Altar |
| [Ore Scaling](#ore-scaling) | Risk vs reward for mining |
| [Configuration](#configuration) | Server admin settings |
| [Installation](#installation) | Setup instructions |

---

## Features Overview

<details>
<summary><strong>Click to expand feature summary</strong></summary>

- **Safe Zone** -- Configurable spawn radius with PVP disabled and resource penalties
- **Distance Scaling** -- Monster levels, density, and behavior change with distance from spawn
- **Monster Modifiers** -- Rare enemies spawn with Bounce Back, Thorns, or permanent potion effects
- **Mob Density Controls** -- Check configs to adjust mob density scaling
- **Loot Bags** -- Tiered drops from Common to Legendary containing gear and valuables
- **Greater Experience Bottles** -- 50-100 XP per bottle, found in loot bags
- **Ancient Enchanting Altar** -- Bypass enchantment slot limits entirely
- **Ore Scaling** -- Mining yields increase in dangerous territories
- **Server-Side** -- No client installation required

</details>

---

## Safe Zone

<details>
<summary><strong>How the safe zone works</strong></summary>

The area around world spawn is protected. PVP is disabled and new players can get their bearings without immediate threat.

**But camping is punished:**

| Penalty | Effect |
|---------|--------|
| Ore failure rate | 25% chance mining yields nothing |
| Diamond drop rate | Only 1% in safe zone |
| Feedback message | "Your skill led you to believe there was ore here, but nothing was recovered." |

The message is clear: **leave**.

The safe zone radius is fully configurable. Adjust it to match your server's intended difficulty curve.

</details>

---

## Distance Scaling

<details>
<summary><strong>How distance affects the world</strong></summary>

The further from spawn, the more dangerous the world becomes. This isn't a gentle curve—it's exponential.

| Distance | Monster Level Range | Special Behavior                                               |
|----------|--------------------|----------------------------------------------------------------|
| Safe Zone - 10k | 1 - ~100           | Some monsters spawn with gear                                  |
| 10k+ | 100 - 500          | Mobs no longer burn in daylight                                |
| 100k+ | 500 - 1000         | Elite territory (expect monsters with Netherite gear commonly) |
| 1 million+ | 2000+              | Endgame zone                                                   |

**Mob Density:**
Monster spawn density also scales with distance.

**Boss Spawns:**
After 10k blocks, bosses can spawn randomly on players. Stay alert.

</details>

<details>
<summary><strong>Daylight immunity</strong></summary>

Past 10,000 blocks from spawn, undead mobs **stop burning in sunlight**.

This fundamentally changes survival strategy. Daytime is no longer safe. You cannot simply wait out the night. The deep zones require constant vigilance.

</details>

---

## Monster Modifiers

<details>
<summary><strong>Rare monster abilities</strong></summary>

Some monsters spawn with special modifiers that require tactical thinking to defeat.

| Modifier | Effect | Counter Strategy |
|----------|--------|------------------|
| **Bounce Back** | Attacker receives knockback after hitting the monster | Ranged attacks, corner trapping |
| **Thorns** | Immune to melee, reflects 100% melee damage back | Bows, crossbows, magic, environmental damage |

These modifiers exist to prevent mindless sword-swinging. Players must diversify their combat approach or travel in parties with varied loadouts.

</details>

<details>
<summary><strong>Potion-enhanced monsters</strong></summary>

Rare monsters can spawn with **1 to 10 permanent positive potion effects**.

Possible effects include:
- Speed
- Strength
- Resistance
- Regeneration
- Fire Resistance
- And more...

A monster with 8+ stacked effects is a serious threat. These encounters reward preparation and punish overconfidence.

</details>

---

## Loot System

<details>
<summary><strong>Loot bags</strong></summary>

Killed monsters have a chance to drop loot bags. Rarity scales with monster level.

| Tier | Contents |
|------|----------|
| **Common** | Basic supplies, low-tier materials |
| **Uncommon** | Tools, armor components, moderate resources |
| **Rare** | Enchanted items, valuable ores |
| **Epic** | High-tier equipment, rare materials |
| **Legendary** | Best gear, Greater Experience Bottles, Ancient Enchanting Altar (rare) |

Higher level monsters in deeper zones drop higher tier bags more frequently.

</details>

<details>
<summary><strong>Greater Experience Bottles</strong></summary>

Found in loot bags, these enhanced bottles contain **50-100 XP orbs** per bottle.

Far more efficient than standard bottles o' enchanting. Essential for enchanting gear in the field without returning to mob grinders.

</details>

<details>
<summary><strong>Ancient Enchanting Altar</strong></summary>

The ultimate prize. Drops rarely from **Legendary loot bags** only.

**What it does:**
- Bypasses the normal enchantment slot limit
- Allows stacking infinite different enchantments on a single item (Costs scale exponentially with the number of enchantments.)
- This is how you build gear capable of surviving level 2000+ zones (Maybe. Testing revealed 200+ swings with a basic Netherite Sword to kill a zombie after level 2000 monsters.)

The Altar is not craftable. You must earn it by pushing deep enough to farm Legendary bags consistently.

</details>

---

## Ore Scaling

<details>
<summary><strong>How ore yields change with distance</strong></summary>

Mining in dangerous territory is rewarded.

| Zone | Ore Yield |
|------|-----------|
| Safe Zone | Penalized (25% failure, 1% diamond rate) |
| Near Spawn | Normal vanilla rates |
| Mid Distance | Increased yields |
| Deep Zones | Significantly boosted drops |

The exact multipliers are configurable 6x is the maximum ore boost. Default settings create meaningful progression where deep-zone mining trips are worth the risk.

</details>

---

## Configuration

<details>
<summary><strong>Safe zone settings</strong></summary>

```toml
["Spawn Protection"]
#Enable spawn protection zone where PvP is disabled
enableSpawnProtection = true
#Radius in blocks around spawn where PvP is disabled (default: 1000)
# Default: 1000
# Range: 0 ~ 100000
spawnProtectionRadius = 1000
```
```toml
["Death Penalties"]
	#Enable distance-based death penalties
	enableDeathPenalties = true
	#Distance in chunks where death penalties begin (default: 62 = 1000 blocks)
	# Default: 62
	# Range: 0 ~ 1000000
```
</details>

<details>
<summary><strong>Scaling settings</strong></summary>

```toml
["Monster Scaling"]
#Enable monster level scaling based on distance from spawn
enableMonsterScaling = true
#Number of chunks for each difficulty tier (default: 100)
# Default: 100
# Range: 1 ~ 10000
chunksPerTier = 100
#Minimum monster level at spawn (default: 1)
# Default: 1
# Range: 1 ~ 100
minMonsterLevel = 1
#Maximum monster level increase per tier (default: 2)
# Default: 2
# Range: 1 ~ 50
maxMonsterLevelPerTier = 2
#Health multiplier per monster level (default: 0.1 = 10% per level)
# Default: 0.1
# Range: 0.0 ~ 5.0
healthScalingPerLevel = 0.1
#Damage multiplier per monster level (default: 0.08 = 8% per level)
# Default: 0.08
# Range: 0.0 ~ 5.0
damageScalingPerLevel = 0.08
#Show visual indicators like name tags with levels (disable if using mods like Neat or Damage Indicators)
showVisualIndicators = true
#Show monster level in custom nametag (disable for compatibility with Neat, Damage Indicators, etc.)
showLevelNametags = true

["Rare Monster Buffs"]
#Enable rare monsters with special buffs
enableRareBuffs = true
#Chance for a monster to spawn with buffs (default: 0.03 = 3%)
# Default: 0.03
# Range: 0.0 ~ 1.0
rareBuffChance = 0.03
#Maximum number of buffs per rare monster (default: 3)
# Default: 3
# Range: 1 ~ 10
maxBuffsPerMob = 3

["Ore Scaling"]
#Enable ore drop and XP scaling based on distance
enableOreScaling = true
#Ore drop multiplier increase per tier (default: 0.1 = 10%)
# Default: 0.1
# Range: 0.0 ~ 10.0
oreDropMultiplierPerTier = 0.1
#Ore XP multiplier increase per tier (default: 0.15 = 15%)
# Default: 0.15
# Range: 0.0 ~ 10.0
oreXpMultiplierPerTier = 0.15
#Maximum ore drop multiplier (default: 6.0 = 600%)
# Default: 6.0
# Range: 1.0 ~ 100.0
maxOreDropMultiplier = 5.0
#Maximum ore XP multiplier (default: 10.0 = 1000%)
# Default: 10.0
# Range: 1.0 ~ 100.0
maxOreXpMultiplier = 10.0
#Distance in chunks defining spawn penalty zone (default: 312 = ~5000 blocks). Within this zone: diamonds 99% fail, other ores 25% fail. Rates scale to 0% at this distance.
# Default: 312
# Range: 0 ~ 1000000
diamondSpawnDistance = 312

["Daytime Hostiles"]
#Enable hostile mobs during daytime at extreme distances
enableDaytimeHostiles = true
#Distance in chunks where daytime hostiles start (default: 10000)
# Default: 10000
# Range: 0 ~ 1000000
daytimeHostileDistance = 10000
#Maximum persistent mobs allowed within 32 blocks to prevent lag (default: 50)
# Default: 50
# Range: 10 ~ 500
daytimeHostileMobCap = 50
```
</details>

<details>
<summary><strong>Loot settings</strong></summary>

```toml
["Loot Bags"]
#Enable loot bag drops from monsters
enableLootBags = true
#Base chance for loot bags to drop (default: 0.01 = 1%)
# Default: 0.01
# Range: 0.0 ~ 1.0
lootBagBaseDropChance = 0.01
#Additional drop chance per tier (default: 0.005 = 0.5%)
# Default: 0.005
# Range: 0.0 ~ 1.0
lootBagChancePerTier = 0.005
#Maximum loot bag drop chance cap (default: 0.25 = 25%)
# Default: 0.25
# Range: 0.0 ~ 1.0
lootBagMaxDropChance = 0.25
```

</details>

<details>
<summary><strong>Monster modifier settings</strong></summary>

```toml
["Boss Encounters"]
#Enable random boss spawns at extreme distances
enableBossEncounters = true
#Distance in chunks where bosses can spawn (default: 100000)
# Default: 100000
# Range: 0 ~ 10000000
bossSpawnDistance = 100000
#Chance per check for a boss to spawn (default: 0.0001 = 0.01%)
# Default: 1.0E-4
# Range: 0.0 ~ 1.0
bossSpawnChance = 1.0E-4
#Cooldown between boss spawn checks in ticks (default: 12000 = 10 minutes)
# Default: 12000
# Range: 0 ~ 72000
bossSpawnCooldownTicks = 12000

["Daytime Hostiles"]
#Enable hostile mobs during daytime at extreme distances
enableDaytimeHostiles = true
#Distance in chunks where daytime hostiles start (default: 10000)
# Default: 10000
# Range: 0 ~ 1000000
daytimeHostileDistance = 10000
#Maximum persistent mobs allowed within 32 blocks to prevent lag (default: 50)
# Default: 50
# Range: 10 ~ 500
daytimeHostileMobCap = 50
#Scale mob density based on distance (default: true). When enabled, mob cap increases gradually with distance
enableScaledMobDensity = true
#Minimum mob cap near spawn when scaled density is enabled (default: 5)
# Default: 5
# Range: 1 ~ 50
minMobDensity = 5

```

</details>

---

## Installation

<details>
<summary><strong>Setup instructions</strong></summary>

1. Install NeoForge (version 21.1.215)
2. Download latest release from [GitHub Releases](#) | [CurseForge](#) | [Modrinth](#)
3. Place `.jar` in server's `mods` folder
4. Start server to generate config
5. Adjust settings in `config/far-from-home.toml`
6. Restart server

**Note:** This is a server-side mod. Players do not need to install anything.

</details>

---

## The Goal

Push deeper than anyone else. Prove you can survive where others die. Bring back gear that shouldn't exist.

---

## Companion Mods

<details>
<summary><strong>Part of the Frontier trilogy</strong></summary>

| Mod | Purpose | Integration                                       |
|-----|---------|---------------------------------------------------|
| [Sacred Settlements](#https://github.com/TavariAgent/Sacred-Settlements) | Village claiming + protection | Safe havens for storing deep-zone loot            |
| [Frontier Stats](#https://github.com/TavariAgent/Frontier-Stats) | Guilds, bounties, milestones | Track who's pushed furthest, fight over territory |

Each mod works standalone. Together they form a cohesive survival overhaul.

</details>

---

## License

[MIT License](LICENSE) — Fork it, modify it, ship it.

[View Source on GitHub](#) • [Report Issues](#)
