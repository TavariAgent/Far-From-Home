package com.bleepz.farfromhome;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A loot bag that can be opened by right-clicking to receive random rewards.
 */
public class LootBagItem extends Item {

    private final LootBagTier tier;

    public LootBagItem(LootBagTier tier) {
        super(new Item.Properties()
                .stacksTo(16)); // Can stack up to 16 bags
        this.tier = tier;
    }

    public LootBagTier getTier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Generate and give loot
            List<ItemStack> loot = LootBagContents.generateLoot(tier, level.random);

            // Give items to player
            boolean allGiven = true;
            for (ItemStack lootItem : loot) {
                if (!player.getInventory().add(lootItem)) {
                    // Inventory full - drop on ground
                    player.drop(lootItem, false);
                    allGiven = false;
                }
            }

            // Play sound
            level.playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS,
                    0.5f,
                    1.0f + (level.random.nextFloat() * 0.2f));

            // Send message
            String message = allGiven ?
                    "§aOpened " + tier.getColor() + tier.getDisplayName() + " Loot Bag§a!" :
                    "§eOpened " + tier.getColor() + tier.getDisplayName() + " Loot Bag§e! (Some items dropped)";

            serverPlayer.displayClientMessage(Component.literal(message), true);

            // Consume one bag
            stack.shrink(1);

            FarFromHome.LOGGER.debug("Player {} opened {} loot bag, received {} items",
                    player.getName().getString(),
                    tier.getDisplayName(),
                    loot.size());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Add tooltip
        tooltipComponents.add(Component.literal(tier.getColor() + "Right-click to open!"));
        tooltipComponents.add(Component.literal(ChatFormatting.GRAY + "Contains " +
                tier.getMinItems() + "-" + tier.getMaxItems() + " random items"));

        // Add tier-specific flavor text
        String flavorText = switch (tier) {
            case COMMON -> "Basic supplies from distant lands";
            case UNCOMMON -> "Valuable goods from far reaches";
            case RARE -> "Precious treasures from the deep world";
            case LEGENDARY -> "Legendary artifacts from the world's edge";
        };

        tooltipComponents.add(Component.literal(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC + flavorText));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(tier.getColor() + tier.getDisplayName() + " Loot Bag");
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint to Rare and Legendary bags
        return tier == LootBagTier.RARE || tier == LootBagTier.LEGENDARY;
    }
}