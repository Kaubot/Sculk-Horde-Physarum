package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.FaithSystem;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;

public class FaithHandler {

    public static int getFaith(Player player) {
        return player.getCapability(SculkMindProvider.SCULK_MIND)
                .map(net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.ISculkMind::getFaith)
                .orElse(0);
    }

    public static void addFaith(ServerPlayer player, int amount) {
        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            cap.addFaith(amount);

            
            PacketHandler.CHANNEL.sendTo(
                    new SkillSyncPacket(
                            cap.getUnlockedSkills(),
                            cap.getEvoPoints(),
                            cap.getFaith(),
                            cap.getActiveTaskNBT(),
                            cap.areTentaclesActive(),
                            cap.getUserFollowerLimit(),     
                            cap.getAllowedFollowerTypes(),
                            cap.getKnownAnchors(),
                            cap.getActiveAbilitiesSet()
                    ),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        });
    }

    public static void syncFaithToClient(ServerPlayer player, int amount) {
        
    }
}
