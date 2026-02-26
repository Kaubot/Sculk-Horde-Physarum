package net.alekrus.shphysarum.SculkPlayerAbility.FastHealing;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ImmediateActionStatePacket {
    private final boolean active;

    public ImmediateActionStatePacket(boolean active) {
        this.active = active;
    }

    public static ImmediateActionStatePacket decode(FriendlyByteBuf buf) {
        return new ImmediateActionStatePacket(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                
                player.getPersistentData().putBoolean("ImmediateHealingActive", active);

                
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    if (active) {
                        cap.addActiveAbility("immediate_actions");
                    } else {
                        cap.removeActiveAbility("immediate_actions");
                    }

                    
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
        });
        ctx.get().setPacketHandled(true);
    }
}
