package com.bleepz.farfromhome;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Network packet sent when player clicks the "Infuse" button.
 */
public record EnchantAltarPacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<EnchantAltarPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FarFromHome.MODID, "enchant_altar"));

    public static final StreamCodec<ByteBuf, EnchantAltarPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            EnchantAltarPacket::pos,
            EnchantAltarPacket::new
    );

    @Override
    public Type<EnchantAltarPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     */
    public static void handle(EnchantAltarPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // Get the player's current menu
                AbstractContainerMenu menu = serverPlayer.containerMenu;

                if (menu instanceof AncientAltarMenu altarMenu) {
                    // Verify the altar position matches
                    if (altarMenu.getAltarPos().equals(packet.pos())) {
                        // Try to perform the enchantment
                        altarMenu.tryEnchant(serverPlayer);
                    }
                }
            }
        });
    }
}
