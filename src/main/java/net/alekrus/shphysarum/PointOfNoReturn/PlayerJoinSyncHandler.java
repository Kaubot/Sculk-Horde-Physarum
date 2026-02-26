package net.alekrus.shphysarum.PointOfNoReturn;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerJoinSyncHandler {

    
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                
                PacketHandler.CHANNEL.sendTo(
                        new SkillSyncPacket(
                                cap.getUnlockedSkills(), cap.getEvoPoints(), cap.getFaith(),
                                cap.getActiveTaskNBT(), cap.areTentaclesActive(),
                                cap.getUserFollowerLimit(), cap.getAllowedFollowerTypes(),
                                cap.getKnownAnchors(), cap.getActiveAbilitiesSet()
                        ),
                        player.connection.connection, NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }
    }
}
