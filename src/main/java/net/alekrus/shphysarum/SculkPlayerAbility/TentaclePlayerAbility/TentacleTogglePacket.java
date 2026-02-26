package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import net.alekrus.shphysarum.AnimationAll.VisualSyncHelper;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TentacleTogglePacket {

    private final boolean active;

    public TentacleTogglePacket(boolean active) {
        this.active = active;
    }

    public static TentacleTogglePacket decode(FriendlyByteBuf buf) {
        return new TentacleTogglePacket(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                if (!cap.hasSkill("adaptive_body_structuring")) return;

                if (active) {
                    if (!player.isCreative() && player.totalExperience <= 0) {
                        player.displayClientMessage(Component.literal("§cNot enough experience to extend tentacles."), true);
                        sync(player, cap);
                        return;
                    }

                    cap.setTentaclesActive(true);
                    cap.addActiveAbility("sharp_tentacle");

                    
                    player.getPersistentData().putBoolean("sh_tentaclesActive", true);

                } else {
                    cap.setTentaclesActive(false);
                    cap.removeActiveAbility("sharp_tentacle");

                    
                    player.getPersistentData().putBoolean("sh_tentaclesActive", false);
                    player.getPersistentData().putBoolean("sh_isBlocking", false);
                }
                VisualSyncHelper.syncToTracking(player);

                sync(player, cap);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private void sync(ServerPlayer player, net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.ISculkMind cap) {
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
