package net.alekrus.shphysarum.effects;

import com.github.sculkhorde.common.entity.SculkWitchEntity;
import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class ModEvents {

    private static final double SEARCH_RADIUS = 10.0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;


        if (!InfectionHandler.isInfected(player)) {
            return;
        }



        if (player.tickCount % 40 != 0) {
            return;
        }


        AABB searchArea = player.getBoundingBox().inflate(SEARCH_RADIUS);
        List<SculkWitchEntity> nearbyWitches = player.level().getEntitiesOfClass(SculkWitchEntity.class, searchArea);

        if (!nearbyWitches.isEmpty()) {



            MobEffectInstance witchBuff = SculkWitchEntity.effect;

            if (witchBuff != null) {

                player.addEffect(new MobEffectInstance(witchBuff));
            }
        }
    }
}
