package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class GravemindMessagePacket {

    private final String message;

    public GravemindMessagePacket(String message) {
        this.message = message;
    }

    public GravemindMessagePacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
    }

    
    public static GravemindMessagePacket decode(FriendlyByteBuf buf) {
        return new GravemindMessagePacket(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.message);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            
            ClientGravemindState.queueMessage(message);
        });
        context.setPacketHandled(true);
    }

    
    public static void sendToPlayer(ServerPlayer player, String message) {
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GravemindMessagePacket(message));
    }
}
