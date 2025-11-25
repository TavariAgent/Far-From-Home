package com.bleepz.farfromhome;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the Ancient Enchanting Altar.
 * Minimalist design with floating item slots.
 */
public class AncientAltarScreen extends AbstractContainerScreen<AncientAltarMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FarFromHome.MODID, "textures/gui/ancient_altar.png");

    private Button enchantButton;

    public AncientAltarScreen(AncientAltarMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // Standard GUI size with inventory
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94; // Standard position
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        // Add smaller "Infuse" button centered between the slots
        int buttonX = this.leftPos + 70;
        int buttonY = this.topPos + 60;

        this.enchantButton = Button.builder(
                        Component.literal("Infuse"),
                        button -> {
                            // Send packet to server to perform enchantment
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                                    new EnchantAltarPacket(this.menu.getAltarPos())
                            );
                        })
                .bounds(buttonX, buttonY, 36, 17)
                .build();

        this.addRenderableWidget(enchantButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // Render GUI
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render tooltips
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw the GUI background texture
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x8B00FF, false);

        // Draw inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // Draw slot labels
        guiGraphics.drawString(this.font, "Item", 44, 23, 0xCCCCCC, false);
        guiGraphics.drawString(this.font, "Book", 116, 23, 0xCCCCCC, false);
    }
}