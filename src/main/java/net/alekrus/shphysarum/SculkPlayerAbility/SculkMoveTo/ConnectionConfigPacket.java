package net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ConnectionConfigPacket {
    private final int limit;
    private final Set<String> allowedTypes;

    public ConnectionConfigPacket(int limit, Set<String> allowedTypes) {
        this.limit = limit;
        this.allowedTypes = allowedTypes;
    }

    public static void encode(ConnectionConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.limit);
        buf.writeInt(msg.allowedTypes.size());
        for (String s : msg.allowedTypes) buf.writeUtf(s);
    }

    public static ConnectionConfigPacket decode(FriendlyByteBuf buf) {
        int limit = buf.readInt();
        int size = buf.readInt();
        Set<String> types = new HashSet<>();
        for (int i = 0; i < size; i++) types.add(buf.readUtf());
        return new ConnectionConfigPacket(limit, types);
    }

    public static void handle(ConnectionConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {

                    cap.setUserFollowerLimit(msg.limit);
                    cap.setAllowedFollowerTypes(msg.allowedTypes);


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
        });
        ctx.get().setPacketHandled(true);
    }
}
