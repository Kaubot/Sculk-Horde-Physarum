package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class GravemindScenePacket {

    private final int sceneId;

    public GravemindScenePacket(int sceneId) {
        this.sceneId = sceneId;
    }

    public GravemindScenePacket(FriendlyByteBuf buf) {
        this.sceneId = buf.readInt();
    }

    public static GravemindScenePacket decode(FriendlyByteBuf buf) {
        return new GravemindScenePacket(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(sceneId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {

            
            switch (sceneId) {
                case 1:
                    ClientGravemindState.queueMessage("This place...");
                    break;

                case 2:
                    ClientGravemindState.queueMessage("For some it's the end, for others it's the beginning");
                    break;

                case 3:
                    ClientGravemindState.queueMessage("Someone erected these mineshaft");
                    ClientGravemindState.queueMessage("But who?");
                    ClientGravemindState.queueMessage("Maybe you are not alone after all");
                    break;

                case 4:
                    ClientGravemindState.queueMessage("A monument to something.");
                    break;

                case 5:
                    ClientGravemindState.queueMessage("It's remarkably they were able to construct this at all.");
                    break;

                case 6:
                    ClientGravemindState.queueMessage("A fortress of bone and fire.");
                    break;
            }
        });
        context.setPacketHandled(true);
    }

    
    public static void sendToPlayer(ServerPlayer player, int sceneId) {
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GravemindScenePacket(sceneId));
    }
}
