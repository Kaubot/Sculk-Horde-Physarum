package net.alekrus.shphysarum.PlayerModel;

import net.alekrus.shphysarum.AnimationAll.VisualSyncHelper;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfectionVisualAutoFix {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            if (event.player instanceof ServerPlayer player) {

                if (player.tickCount % 10 == 0) {
                    player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {

                        
                        boolean hasRootSkill = cap.hasSkill("root");
                        boolean hasTentacleSkill = cap.hasSkill("adaptive_body_structuring");

                        boolean oldRoot = player.getPersistentData().getBoolean("sh_isInfected");
                        boolean oldTentacle = player.getPersistentData().getBoolean("sh_hasTentacleSkill");

                        
                        if (hasRootSkill != oldRoot || hasTentacleSkill != oldTentacle || player.tickCount % 40 == 0) {
                            player.getPersistentData().putBoolean("sh_isInfected", hasRootSkill);
                            player.getPersistentData().putBoolean("sh_hasTentacleSkill", hasTentacleSkill);
                            VisualSyncHelper.syncToTracking(player);
                        }
                    });
                }
            }
        }
    }
}
