package net.alekrus.shphysarum.InteractionWithItems;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfectedItemRestrictions {


    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {



        Player player = event.getEntity();

        ItemStack stack = event.getItemStack();


        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return;
        String idString = itemId.toString();


        if (idString.equals("sculkhorde:eye_of_purity") || idString.equals("sculkhorde:purification_flask")) {


            if (InfectionHandler.isInfected(player)) {


                event.setCanceled(true);


                player.displayClientMessage(Component.literal("§3Do not see the point in this action"), true);

            }
        }
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;

        if (!InfectionHandler.isInfected(player)){
            return;
        }


        boolean holdingSword = isPurityBlade(player.getMainHandItem()) || isPurityBlade(player.getOffhandItem());

        if (holdingSword) {

            MobEffect purityEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "purity"));

            if (purityEffect != null) {


                player.addEffect(new MobEffectInstance(purityEffect, 200, 0, false, false, true));
            }
        }
    }


    private static boolean isPurityBlade(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.toString().equals("sculkhorde:blade_of_purity");
    }




}