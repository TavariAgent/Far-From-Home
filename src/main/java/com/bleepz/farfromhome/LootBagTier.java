package com.bleepz.farfromhome;

import net.minecraft.ChatFormatting;

/**
 * Defines the different tiers of loot bags with their properties.
 */
public enum LootBagTier {
    COMMON("Common", ChatFormatting.GRAY, 3, 5, 1000, 5000),
    UNCOMMON("Uncommon", ChatFormatting.GREEN, 4, 7, 5000, 10000),
    RARE("Rare", ChatFormatting.BLUE, 5, 9, 10000, 50000),
    LEGENDARY("Legendary", ChatFormatting.GOLD, 7, 12, 50000, Integer.MAX_VALUE);

    private final String displayName;
    private final ChatFormatting color;
    private final int minItems;
    private final int maxItems;
    private final int minChunks;
    private final int maxChunks;

    LootBagTier(String displayName, ChatFormatting color, int minItems, int maxItems, int minChunks, int maxChunks) {
        this.displayName = displayName;
        this.color = color;
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.minChunks = minChunks;
        this.maxChunks = maxChunks;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public int getMinItems() {
        return minItems;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public int getMinChunks() {
        return minChunks;
    }

    public int getMaxChunks() {
        return maxChunks;
    }

    /**
     * Gets the appropriate loot bag tier for a given distance.
     */
    public static LootBagTier getTierForDistance(int chunkDistance) {
        if (chunkDistance >= LEGENDARY.minChunks) return LEGENDARY;
        if (chunkDistance >= RARE.minChunks) return RARE;
        if (chunkDistance >= UNCOMMON.minChunks) return UNCOMMON;
        return COMMON;
    }
}
