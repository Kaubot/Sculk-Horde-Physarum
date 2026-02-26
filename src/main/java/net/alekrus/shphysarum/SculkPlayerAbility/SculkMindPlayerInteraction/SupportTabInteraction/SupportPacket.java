package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.SupportTabInteraction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SupportPacket {

    private final int type;
    private final BlockPos target;

    public SupportPacket(int type, BlockPos target) {
        this.type = type;
        this.target = target;
    }

    public SupportPacket(FriendlyByteBuf buf) {
        this.type = buf.readInt();
        this.target = buf.readBlockPos();
    }

    public static SupportPacket decode(FriendlyByteBuf buf) {
        return new SupportPacket(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(type);
        buf.writeBlockPos(target);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                switch (type) {
                    case 0:
                        SupportHandler.executeSporeDelivery(player); 
                        break;
                    case 1:
                        SupportHandler.startTeleportSequence(player, target); 
                        break;
                    case 2:
                        SupportHandler.executeSkeletonReinforcement(player);
                        break;
                    case 3:
                        SupportHandler.executeWitchReinforcement(player);
                        break;
                    case 4:
                        SupportHandler.executeCreeperSupport(player); 
                        break;
                }
            }
        });
        context.setPacketHandled(true);
    }
}
