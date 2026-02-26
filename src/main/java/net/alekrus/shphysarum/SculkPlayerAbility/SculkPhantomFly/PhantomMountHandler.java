package net.alekrus.shphysarum.SculkPlayerAbility.SculkPhantomFly;

import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PhantomMountHandler {


    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof SculkPhantomEntity phantom)) return;
        Player player = event.getEntity();

        if (!InfectionHandler.isInfected(player)) return;

        if (!player.level().isClientSide) {

            Goal suicideGoal = null;
            for (WrappedGoal wrapped : phantom.goalSelector.getAvailableGoals()) {
                if (wrapped.getGoal().getClass().getSimpleName().equals("FallToTheGroundIfMobsUnder")) {
                    suicideGoal = wrapped.getGoal();
                    break;
                }
            }
            if (suicideGoal != null) phantom.goalSelector.removeGoal(suicideGoal);

            player.startRiding(phantom);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }


    @SubscribeEvent
    public static void onPhantomTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof SculkPhantomEntity phantom && !phantom.level().isClientSide) {


            if (phantom.isVehicle() && phantom.getFirstPassenger() instanceof Player player) {

                phantom.setTarget(null);
                phantom.getNavigation().stop();


                phantom.setYRot(player.getYRot());
                phantom.yHeadRot = player.getYRot();
                phantom.yBodyRot = player.getYRot();
                phantom.setXRot(player.getXRot());
            }
        }
    }


    @SubscribeEvent
    public static void onPhantomHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof SculkPhantomEntity phantom) {
            if (phantom.isVehicle() && phantom.getFirstPassenger() instanceof Player) {
                if (event.getSource() == phantom.damageSources().fall() || event.getSource() == phantom.damageSources().flyIntoWall()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}