package net.alekrus.shphysarum.effects.PurityToPlayer;

import com.github.sculkhorde.core.ModMobEffects;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum")
public class PurityResistanceHandler {

    
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;

        
        if (!InfectionHandler.isInfected(player)) return;

        
        if (event.getEffectInstance().getEffect() == ModMobEffects.PURITY.get()) {

            float multiplier = ResistanceLogic.getDurationMultiplier(player);

            
            if (multiplier < 1.0f) {
                
                int originalDuration = event.getEffectInstance().getDuration();
                int newDuration = (int) (originalDuration * multiplier);
                

                try {


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    
    @SubscribeEvent
    public static void onPlayerTickReducePurity(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.START || event.player.level().isClientSide) return;

        Player player = event.player;
        if (!InfectionHandler.isInfected(player)) return;

        if (player.hasEffect(ModMobEffects.PURITY.get())) {
            var effect = player.getEffect(ModMobEffects.PURITY.get());
            if (effect == null) return;

            float multiplier = ResistanceLogic.getDurationMultiplier(player);
            if (multiplier >= 1.0f) return; 


            int speedUp = (int) (1.0f / multiplier) - 1;
            

            if (speedUp > 0) {
                
                int currentDuration = effect.getDuration();
                if (currentDuration > speedUp) {

                    
                    for (int i = 0; i < speedUp; i++) {
                        effect.tick(player, () -> {}); 
                        
                    }
                }
            }
        }
    }
}