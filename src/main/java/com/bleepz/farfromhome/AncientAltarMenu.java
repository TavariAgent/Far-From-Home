package com.bleepz.farfromhome;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Menu/Container for the Ancient Enchanting Altar.
 * Simple 2-slot system: Item slot + Book slot.
 */
public class AncientAltarMenu extends AbstractContainerMenu {

    private final Container container;
    private final BlockPos altarPos;

    // Slot indices
    private static final int ITEM_SLOT = 0;
    private static final int BOOK_SLOT = 1;

    /**
     * Client-side constructor (called from packet).
     */
    public AncientAltarMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(2), extraData.readBlockPos());
    }

    /**
     * Server-side constructor.
     */
    public AncientAltarMenu(int containerId, Inventory playerInventory, Container container, BlockPos altarPos) {
        super(ModMenuTypes.ANCIENT_ALTAR.get(), containerId);

        this.container = container;
        this.altarPos = altarPos;

        // Item slot (left) - accepts any item
        this.addSlot(new Slot(container, ITEM_SLOT, 44, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // Accept any item
            }
        });

        // Book slot (right) - only accepts enchanted books
        this.addSlot(new Slot(container, BOOK_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof EnchantedBookItem;
            }
        });

        // Player inventory slots
        int inventoryStartY = 84;

        // Main inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, inventoryStartY + row * 18));
            }
        }

        // Hotbar (bottom row)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    8 + col * 18, inventoryStartY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // If clicking on altar slots (0-1), move to player inventory
            if (index < 2) {
                if (!this.moveItemStackTo(slotStack, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // If clicking player inventory, try to move to appropriate altar slot
            else {
                // Try book slot first if it's an enchanted book
                if (slotStack.getItem() instanceof EnchantedBookItem) {
                    if (!this.moveItemStackTo(slotStack, BOOK_SLOT, BOOK_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Otherwise try item slot
                else if (!this.moveItemStackTo(slotStack, ITEM_SLOT, ITEM_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        // Check if altar block still exists
        return player.level().getBlockState(altarPos).is(ModBlocks.ANCIENT_ENCHANTING_ALTAR.get())
                && player.distanceToSqr(altarPos.getX() + 0.5, altarPos.getY() + 0.5, altarPos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // Drop items when GUI closes
        if (!player.level().isClientSide()) {
            this.clearContainer(player, container);
        }
    }

    /**
     * Gets the item in the item slot.
     */
    public ItemStack getItemSlot() {
        return container.getItem(ITEM_SLOT);
    }

    /**
     * Gets the book in the book slot.
     */
    public ItemStack getBookSlot() {
        return container.getItem(BOOK_SLOT);
    }

    /**
     * Attempts to enchant the item with the book.
     * Returns true if successful.
     */
    public boolean tryEnchant(Player player) {
        if (player.level().isClientSide()) {
            return false;
        }

        ItemStack item = getItemSlot();
        ItemStack book = getBookSlot();

        // Validate we have both items
        if (item.isEmpty() || book.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§cPlace an item and an enchanted book in the altar!"),
                    true
            );
            return false;
        }

        // Calculate XP cost
        int xpCost = UnlimitedEnchantmentSystem.calculateXPCost(item);
        int currentEnchants = UnlimitedEnchantmentSystem.getEnchantmentCount(item);

        // Check if player has enough XP
        if (player.experienceLevel < xpCost) {
            player.displayClientMessage(
                    Component.literal("§cNeed " + xpCost + " levels! (Enchant #" + (currentEnchants + 1) + ")"),
                    true
            );
            return false;
        }

        // Apply the enchantment from book to item
        if (!UnlimitedEnchantmentSystem.addEnchantmentFromBook(item, book)) {
            player.displayClientMessage(
                    Component.literal("§cFailed to apply enchantment!"),
                    true
            );
            return false;
        }

        // Consume XP
        player.giveExperienceLevels(-xpCost);

        // Consume the book
        book.shrink(1);
        container.setItem(BOOK_SLOT, book);

        // Update the item in the slot
        container.setItem(ITEM_SLOT, item);

        // Success message
        String costMsg = xpCost > 0 ? " (Cost: " + xpCost + " levels)" : " (Free!)";
        player.displayClientMessage(
                Component.literal("§d§lEnchantment Applied!" + costMsg),
                true
        );

        // Play success sound
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0f, 1.0f);

        // Destroy the altar block with effects
        destroyAltar(player);

        // Close the GUI
        player.closeContainer();

        FarFromHome.LOGGER.info("Player {} enchanted item with Ancient Altar. Enchant #{}, Cost: {} levels",
                player.getName().getString(), currentEnchants + 1, xpCost);

        return true;
    }

    /**
     * Destroys the altar block with dramatic particle effects.
     */
    private void destroyAltar(Player player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        // Spawn particles
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ENCHANT,
                altarPos.getX() + 0.5,
                altarPos.getY() + 0.5,
                altarPos.getZ() + 0.5,
                50, // particle count
                0.5, 0.5, 0.5, // spread
                0.05 // speed
        );

        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                altarPos.getX() + 0.5,
                altarPos.getY() + 0.5,
                altarPos.getZ() + 0.5,
                1,
                0, 0, 0,
                0
        );

        // Play destruction sound
        serverLevel.playSound(null, altarPos,
                net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0f, 0.8f);

        // Destroy the block
        serverLevel.destroyBlock(altarPos, false);

        FarFromHome.LOGGER.debug("Destroyed Ancient Altar at {}", altarPos);
    }

    public BlockPos getAltarPos() {
        return altarPos;
    }
}