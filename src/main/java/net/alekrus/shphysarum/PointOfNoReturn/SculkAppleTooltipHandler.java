package net.alekrus.shphysarum.PointOfNoReturn;

import net.alekrus.shphysarum.shPhysarum;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class SculkAppleTooltipHandler {

    private static final String APPLE_ID = "sculk_apple";

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (itemId == null || !itemId.getNamespace().equals(shPhysarum.MODID) || !itemId.getPath().equals(APPLE_ID)) {
            return;
        }

        if (InfectionHandler.isClientInfected(player)) {
            event.getToolTip().add(Component.literal("GREATER THAN LIFE").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        } else {

            event.getToolTip().add(Component.literal("A bad smelling soft fruit").withStyle(ChatFormatting.WHITE));
        }
    }
}
