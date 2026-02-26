package net.alekrus.shphysarum.attackgoal;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkSheepEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttackCancelHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {


        if (event.getSource().getEntity() instanceof Player player) {

            if (InfectionHandler.isInfected(player)) {
                LivingEntity target = event.getEntity();


                if (isSculkAlly(target)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }


        if (event.getEntity() instanceof Player victimPlayer) {

            if (InfectionHandler.isInfected(victimPlayer)) {


                if (event.getSource().is(DamageTypes.THORNS)) {


                    Entity thornOwner = event.getSource().getEntity();


                    if (thornOwner instanceof SculkSheepEntity) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }


    private static boolean isSculkAlly(LivingEntity target) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());


        if (id != null) {
            if (id.toString().equals("sculkhorde:golem_of_wrath")) return false;
            if (id.toString().equals("sculkhorde:infestation_purifier")) return false;
        }

        boolean isSmartEntity = target instanceof ISculkSmartEntity;
        boolean isSculkHordeMob = id != null && id.getNamespace().equals("sculkhorde");

        return isSmartEntity || isSculkHordeMob;
    }
}
