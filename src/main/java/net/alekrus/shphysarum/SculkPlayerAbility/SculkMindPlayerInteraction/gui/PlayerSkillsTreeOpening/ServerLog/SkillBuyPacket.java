package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog;

import net.alekrus.shphysarum.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SkillBuyPacket {
    private final String skillId;
    private final int cost;

    public SkillBuyPacket(String skillId, int cost) {
        this.skillId = skillId;
        this.cost = cost;
    }

    public static SkillBuyPacket decode(FriendlyByteBuf buf) {
        return new SkillBuyPacket(buf.readUtf(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(skillId);
        buf.writeInt(cost);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    if (cap.hasSkill(skillId)) return;

                    if (cap.consumeEvoPoints(cost)) {
                        cap.unlockSkill(skillId);
                        player.sendSystemMessage(Component.literal("§2Mutation Complete: " + skillId));


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
                                player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    } else {
                        player.sendSystemMessage(Component.literal("§cNot enough Biomass."));
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}
