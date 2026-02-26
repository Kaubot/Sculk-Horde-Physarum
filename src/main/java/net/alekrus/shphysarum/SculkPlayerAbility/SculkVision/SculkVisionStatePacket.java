package net.alekrus.shphysarum.SculkPlayerAbility.SculkVision;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkVisionStatePacket {

    private final boolean active;

    public SculkVisionStatePacket(boolean active) {
        this.active = active;
    }

    public static SculkVisionStatePacket decode(FriendlyByteBuf buf) {
        return new SculkVisionStatePacket(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    
                    if (active) {
                        cap.addActiveAbility("vision");
                    } else {
                        cap.removeActiveAbility("vision");
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
