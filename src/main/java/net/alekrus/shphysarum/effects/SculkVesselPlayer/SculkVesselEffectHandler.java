package net.alekrus.shphysarum.effects.SculkVesselPlayer;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.core.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkVesselEffectHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;

        if (InfectionHandler.isInfected(player)) {
            if (!player.hasEffect(ModMobEffects.SCULK_VESSEL.get()) || player.getEffect(ModMobEffects.SCULK_VESSEL.get()).getDuration() < 5) {
                MobEffectInstance sculkVesselEffect = new MobEffectInstance(
                        ModMobEffects.SCULK_VESSEL.get(),
                        300,
                        0,
                        false,
                        false,
                        false
                );
                player.addEffect(sculkVesselEffect);
            }
        }
    }
}
