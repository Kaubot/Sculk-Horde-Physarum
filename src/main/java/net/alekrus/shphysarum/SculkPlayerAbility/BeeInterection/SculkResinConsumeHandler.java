package net.alekrus.shphysarum.SculkPlayerAbility.BeeInterection;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkResinConsumeHandler {

    
    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack stack = event.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (id != null && id.getNamespace().equals("sculkhorde") && id.getPath().equals("sculk_resin")) {

            
            if (!InfectionHandler.isInfected(player)) {
                event.setCanceled(true); 

                if (player.level().isClientSide && event.getDuration() <= 1) {

                }
                return;
            }

            
            event.setDuration(32);
        }
    }

    
    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack stack = event.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (id != null && id.getNamespace().equals("sculkhorde") && id.getPath().equals("sculk_resin")) {

            int remainingTicks = event.getDuration();

            
            
            if (remainingTicks > 0 && remainingTicks % 4 == 0) {
                
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EAT, SoundSource.PLAYERS,
                        0.5F + 0.5F * player.getRandom().nextFloat(),
                        (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;

        
        if (player.level().isClientSide) return;

        ItemStack stack = event.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (id != null && id.getNamespace().equals("sculkhorde") && id.getPath().equals("sculk_resin")) {

            
            if (InfectionHandler.isInfected(player)) {

                
                player.giveExperiencePoints(50);

                
                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F,
                        player.level().random.nextFloat() * 0.1F + 0.9F);
            }
        }
    }
}
