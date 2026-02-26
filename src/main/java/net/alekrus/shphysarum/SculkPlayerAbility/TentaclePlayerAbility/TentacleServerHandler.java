package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import net.alekrus.shphysarum.AnimationAll.VisualSyncHelper;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TentacleServerHandler {

    private static final int XP_DRAIN_INTERVAL = 1;
    private static final float LEVELS_PER_TICK = 0.00001f;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {

            if (!cap.areTentaclesActive()) return;

            if (player.tickCount % XP_DRAIN_INTERVAL == 0) {
                if (!player.isCreative()) {

                    int xpNeededForCurrentLevel = player.getXpNeededForNextLevel();
                    int xpCost = Math.max(1, (int)(xpNeededForCurrentLevel * LEVELS_PER_TICK));

                    if (player.totalExperience >= xpCost) {
                        player.giveExperiencePoints(-xpCost);
                    }
                    else if (player.totalExperience > 0) {
                        player.giveExperiencePoints(-player.totalExperience);
                    }
                    else {
                        
                        cap.setTentaclesActive(false);
                        cap.removeActiveAbility("sharp_tentacle");

                        player.getPersistentData().putBoolean("sh_tentaclesActive", false);
                        player.getPersistentData().putBoolean("sh_isBlocking", false);

                        player.displayClientMessage(Component.literal("§3Experience depleted. Tentacles retracted."), true);

                        VisualSyncHelper.syncToTracking(player);
                        sync(player, cap);
                    }
                }
            }
        });
    }

    private static void sync(ServerPlayer player, net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.ISculkMind cap) {
        PacketHandler.CHANNEL.sendTo(
                new SkillSyncPacket(
                        cap.getUnlockedSkills(), cap.getEvoPoints(), cap.getFaith(),
                        cap.getActiveTaskNBT(), cap.areTentaclesActive(),
                        cap.getUserFollowerLimit(), cap.getAllowedFollowerTypes(),
                        cap.getKnownAnchors(), cap.getActiveAbilitiesSet()
                ),
                player.connection.connection, NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
