package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GravemindTaskEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            TaskManager.onMobKill(event.getEntity(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        
        if (event.player.tickCount % 20 == 0) {

            
            


            boolean isInfected = InfectionHandler.isInfected(event.player);
            boolean isCreative = event.player.isCreative();

            


            if (isInfected || isCreative) {

                TaskManager.checkInfectionProgress(event.player);
            } else {

            }
        }
    }
}
