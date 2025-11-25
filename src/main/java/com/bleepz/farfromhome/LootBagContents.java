package com.bleepz.farfromhome;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.Holder;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates randomized loot for loot bags based on tier.
 */
public class LootBagContents {

    /**
     * Generates a list of random loot items for a given tier.
     */
    public static List<ItemStack> generateLoot(LootBagTier tier, RandomSource random) {
        List<ItemStack> loot = new ArrayList<>();

        // Determine number of items
        int itemCount = tier.getMinItems() + random.nextInt(tier.getMaxItems() - tier.getMinItems() + 1);

        for (int i = 0; i < itemCount; i++) {
            ItemStack item = generateRandomItem(tier, random);
            if (!item.isEmpty()) {
                loot.add(item);
            }
        }

        // LEGENDARY BAGS: 10% chance for Ancient Enchanting Altar!
        if (tier == LootBagTier.LEGENDARY && random.nextDouble() < 0.10) {
            loot.add(new ItemStack(ModItems.ANCIENT_ENCHANTING_ALTAR.get()));
        }

        return loot;
    }

    /**
     * Generates a single random item appropriate for the tier.
     */
    private static ItemStack generateRandomItem(LootBagTier tier, RandomSource random) {
        // Roll for item category
        double roll = random.nextDouble();

        return switch (tier) {
            case COMMON -> {
                if (roll < 0.25) yield generateMaterial(tier, random);
                else if (roll < 0.45) yield generateFood(random);
                else if (roll < 0.70) yield generatePotion(tier, random);
                else if (roll < 0.90) yield generateSimpleEnchantedBook(tier, random);
                else yield generateSimpleTool(tier, random);
            }
            case UNCOMMON -> {
                if (roll < 0.20) yield generateMaterial(tier, random);
                else if (roll < 0.35) yield generateFood(random);
                else if (roll < 0.60) yield generatePotion(tier, random);
                else if (roll < 0.85) yield generateSimpleEnchantedBook(tier, random);
                else yield generateSimpleTool(tier, random);
            }
            case RARE -> {
                if (roll < 0.25) yield generateMaterial(tier, random);
                else if (roll < 0.35) yield generateFood(random);
                else if (roll < 0.55) yield generatePotion(tier, random);
                else if (roll < 0.85) yield generateSimpleEnchantedBook(tier, random);
                else yield generateSimpleTool(tier, random);
            }
            case LEGENDARY -> {
                if (roll < 0.30) yield generateMaterial(tier, random);
                else if (roll < 0.40) yield generateFood(random);
                else if (roll < 0.55) yield generatePotion(tier, random);
                else if (roll < 0.90) yield generateSimpleEnchantedBook(tier, random);
                else yield generateSimpleTool(tier, random);
            }
        };
    }

    /**
     * Generates materials based on tier.
     */
    private static ItemStack generateMaterial(LootBagTier tier, RandomSource random) {
        return switch (tier) {
            case COMMON -> {
                Item[] materials = {Items.IRON_INGOT, Items.COAL, Items.COPPER_INGOT, Items.REDSTONE};
                Item item = materials[random.nextInt(materials.length)];
                yield new ItemStack(item, 4 + random.nextInt(9)); // 4-12 items
            }
            case UNCOMMON -> {
                Item[] materials = {Items.GOLD_INGOT, Items.DIAMOND, Items.LAPIS_LAZULI, Items.EMERALD};
                Item item = materials[random.nextInt(materials.length)];
                yield new ItemStack(item, 2 + random.nextInt(5)); // 2-6 items
            }
            case RARE -> {
                Item[] materials = {Items.DIAMOND, Items.EMERALD, Items.NETHERITE_SCRAP, Items.ANCIENT_DEBRIS};
                Item item = materials[random.nextInt(materials.length)];
                yield new ItemStack(item, 3 + random.nextInt(6)); // 3-8 items
            }
            case LEGENDARY -> {
                Item[] materials = {Items.NETHERITE_INGOT, Items.DIAMOND, Items.EMERALD, Items.NETHERITE_SCRAP};
                Item item = materials[random.nextInt(materials.length)];
                yield new ItemStack(item, 8 + random.nextInt(17)); // 8-24 items
            }
        };
    }

    /**
     * Generates food items.
     */
    private static ItemStack generateFood(RandomSource random) {
        Item[] foods = {
                Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN,
                Items.GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.BREAD,
                Items.BAKED_POTATO, Items.COOKED_SALMON
        };

        Item food = foods[random.nextInt(foods.length)];
        int count = food == Items.GOLDEN_APPLE ? 1 + random.nextInt(3) : 4 + random.nextInt(13); // 4-16 normal, 1-3 golden

        return new ItemStack(food, count);
    }

    /**
     * Generates potions based on tier.
     */
    private static ItemStack generatePotion(LootBagTier tier, RandomSource random) {
        var potionType = switch (tier) {
            case COMMON -> {
                var types = new Holder[]{
                        Potions.HEALING, Potions.SWIFTNESS, Potions.FIRE_RESISTANCE,
                        Potions.NIGHT_VISION, Potions.REGENERATION
                };
                yield types[random.nextInt(types.length)];
            }
            case UNCOMMON, RARE -> {
                var types = new Holder[]{
                        Potions.STRONG_HEALING, Potions.STRONG_SWIFTNESS, Potions.STRONG_STRENGTH,
                        Potions.LONG_FIRE_RESISTANCE, Potions.LONG_REGENERATION, Potions.STRONG_REGENERATION
                };
                yield types[random.nextInt(types.length)];
            }
            case LEGENDARY -> {
                var types = new Holder[]{
                        Potions.STRONG_HEALING, Potions.STRONG_STRENGTH, Potions.STRONG_REGENERATION,
                        Potions.STRONG_SWIFTNESS, Potions.STRONG_LEAPING
                };
                yield types[random.nextInt(types.length)];
            }
        };

        // Create potion with the selected type (1.21 way)
        ItemStack potion = PotionContents.createItemStack(Items.POTION, potionType);

        int count = tier == LootBagTier.LEGENDARY ? 3 + random.nextInt(4) : 1 + random.nextInt(3); // 1-3 or 3-6
        potion.setCount(count);

        return potion;
    }

    /**
     * Generates enchanted books based on tier.
     * Note: Gives Greater Experience Bottles for better XP gain
     */
    private static ItemStack generateSimpleEnchantedBook(LootBagTier tier, RandomSource random) {
        // Give Greater Experience Bottles (50-100 XP each)
        int count = switch (tier) {
            case COMMON -> 1 + random.nextInt(3); // 1-3 bottles
            case UNCOMMON -> 2 + random.nextInt(4); // 2-5 bottles
            case RARE -> 4 + random.nextInt(6); // 4-9 bottles
            case LEGENDARY -> 8 + random.nextInt(9); // 8-16 bottles
        };

        return new ItemStack(ModItems.GREATER_EXPERIENCE_BOTTLE.get(), count);
    }

    /**
     * Generates tools/armor based on tier (without enchantments for compatibility).
     */
    private static ItemStack generateSimpleTool(LootBagTier tier, RandomSource random) {
        return switch (tier) {
            case COMMON -> {
                Item[] tools = {Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SWORD, Items.IRON_SHOVEL};
                yield new ItemStack(tools[random.nextInt(tools.length)]);
            }
            case UNCOMMON -> {
                Item[] tools = {Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SWORD, Items.DIAMOND_HELMET};
                yield new ItemStack(tools[random.nextInt(tools.length)]);
            }
            case RARE -> {
                Item[] tools = {
                        Items.DIAMOND_PICKAXE, Items.DIAMOND_SWORD, Items.DIAMOND_CHESTPLATE,
                        Items.BOW, Items.CROSSBOW, Items.SHIELD
                };
                yield new ItemStack(tools[random.nextInt(tools.length)]);
            }
            case LEGENDARY -> {
                Item[] tools = {
                        Items.NETHERITE_PICKAXE, Items.NETHERITE_SWORD, Items.NETHERITE_CHESTPLATE,
                        Items.NETHERITE_AXE, Items.ELYTRA, Items.TRIDENT, Items.TOTEM_OF_UNDYING
                };
                yield new ItemStack(tools[random.nextInt(tools.length)]);
            }
        };
    }
}