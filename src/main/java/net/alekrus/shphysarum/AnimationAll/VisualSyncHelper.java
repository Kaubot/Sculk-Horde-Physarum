package net.alekrus.shphysarum.AnimationAll;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VisualSyncHelper {


    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            sendVisualsTo(target, PacketDistributor.PLAYER.with(() -> viewer));
        }
    }


    public static void syncToTracking(ServerPlayer player) {
        sendVisualsTo(player, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
    }

    private static void sendVisualsTo(ServerPlayer targetPlayer, PacketDistributor.PacketTarget packetTarget) {
        targetPlayer.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            boolean isInfected = cap.hasSkill("root");
            boolean hasSkill = cap.hasSkill("adaptive_body_structuring");
            boolean active = cap.areTentaclesActive();
            boolean blocking = targetPlayer.getPersistentData().getBoolean("TentacleBlocking");
            long lastAttack = targetPlayer.getPersistentData().getLong("sh_lastAttackServerTime");

            PlayerVisualSyncPacket packet = new PlayerVisualSyncPacket(
                    targetPlayer.getId(), isInfected, hasSkill, active, blocking, lastAttack
            );
            PacketHandler.CHANNEL.send(packetTarget, packet);
        });
    }
}