package net.alekrus.shphysarum.SculkPlayerAbility.PlayerShieldInteraction;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfectedCombatEvents {

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity() instanceof Player player) {

            
            if (InfectionHandler.isInfected(player)) {

                
                ItemStack activeItem = player.getUseItem();

                if (!activeItem.isEmpty() && activeItem.is(Items.SHIELD)) {

                    
                    
                    
                    int durabilityDamage = 102;

                    
                    
                    activeItem.hurtAndBreak(durabilityDamage, player, (p) -> {
                        p.broadcastBreakEvent(p.getUsedItemHand());
                    });

                    
                    
                    player.playSound(SoundEvents.WOOD_BREAK, 0.5f, 0.8f);

                    
                    
                    player.getCooldowns().addCooldown(activeItem.getItem(), 20); 
                }
            }
        }
    }
}