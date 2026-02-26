package net.alekrus.shphysarum.SculkPlayerAbility.Food;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerFoodRestrictionEvents {

    @SubscribeEvent
    public static void onPlayerAttemptToEat(LivingEntityUseItemEvent.Start event) {
        
        if (event.getEntity() instanceof Player player) {
            if (InfectionHandler.isInfected(player)) {
                ItemStack stack = event.getItem();

                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                boolean isSculkResin = id != null && id.getNamespace().equals("sculkhorde") && id.getPath().equals("sculk_resin");

                if (stack.isEdible() && !isSculkResin) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        
        Player player = event.getEntity();

        if (InfectionHandler.isInfected(player)) {
            if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof CakeBlock) {
                event.setCanceled(true);
            }
        }
    }
}
