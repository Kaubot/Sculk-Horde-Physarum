package net.alekrus.shphysarum.SculkPlayerAbility.PlayerFallDamageSkill;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BodyEvolutionHandler {

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        
        if (!InfectionHandler.isInfected(player)) return;

        
        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            if (cap.hasSkill("body_fall_1")) {

                
                if (event.getDistance() <= 10.0f) {
                    event.setDistance(0);
                    event.setCanceled(true); 
                }
                
                
                
            }
        });
    }
}