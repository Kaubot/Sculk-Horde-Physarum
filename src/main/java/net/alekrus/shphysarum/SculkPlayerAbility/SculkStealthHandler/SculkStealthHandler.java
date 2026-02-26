package net.alekrus.shphysarum.SculkPlayerAbility.SculkStealthHandler;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkStealthHandler {


    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {

        Entity cause = event.getCause();


        if (cause instanceof Player player) {


            if (InfectionHandler.isInfected(player)) {


                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
