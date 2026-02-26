package net.alekrus.shphysarum.SculkPlayerAbility.MobBehavior;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerFearHandler {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractVillager villager) {
            villager.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    villager,
                    Player.class,
                    16.0F,
                    0.8D,
                    1.0D,
                    (entity) -> {
                        if (entity instanceof Player player) {
                            return !player.isCreative() &&
                                    !player.isSpectator() &&
                                    InfectionHandler.isInfected(player);
                        }
                        return false;
                    }
            ));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof AbstractVillager villager) {
            Player player = event.getEntity();

            if (player.isCreative() || player.isSpectator()) return;

            if (InfectionHandler.isInfected(player)) {

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);

                if (event.getLevel().isClientSide && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
                    player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);


                }
            }
        }
    }
}
