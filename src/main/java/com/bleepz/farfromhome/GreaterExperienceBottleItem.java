package com.bleepz.farfromhome;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A greater experience bottle that grants 50-100 XP when thrown or used.
 */
public class GreaterExperienceBottleItem extends Item {

    public GreaterExperienceBottleItem() {
        super(new Item.Properties()
                .stacksTo(16)); // Stack up to 16
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Grant 50-100 XP
            int xpAmount = 50 + level.random.nextInt(51); // 50-100

            // Spawn XP orbs at player position
            ExperienceOrb.award(serverLevel, player.position(), xpAmount);

            // Play bottle break sound
            level.playSound(null, player.blockPosition(),
                    SoundEvents.EXPERIENCE_BOTTLE_THROW,
                    SoundSource.PLAYERS,
                    0.5f,
                    0.4f / (level.random.nextFloat() * 0.4f + 0.8f));

            // Show message
            player.displayClientMessage(
                    Component.literal("§d+" + xpAmount + " XP!"),
                    true
            );

            // Consume one bottle
            stack.shrink(1);

            FarFromHome.LOGGER.debug("Player {} used Greater Experience Bottle, gained {} XP",
                    player.getName().getString(), xpAmount);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§dRight-click to gain 50-100 XP"));
        tooltipComponents.add(Component.literal("§7A concentrated essence of experience"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint to make it look special
        return true;
    }
}
