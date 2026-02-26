package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class TaskProgressPacket {

    private final int amount;

    public TaskProgressPacket(int amount) {
        this.amount = amount;
    }

    public static TaskProgressPacket decode(FriendlyByteBuf buf) {
        return new TaskProgressPacket(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(amount);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {

            
            GravemindTask current = TaskManager.getCurrentTask();
            if (current != null) {
                current.currentAmount = amount;

                if (current.isComplete()) {
                    ClientGravemindState.queueMessage("I'm glad that we understand each other. Objective Complete");
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    
    public static void sendToPlayer(ServerPlayer player, int amount) {
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TaskProgressPacket(amount));
    }
}
