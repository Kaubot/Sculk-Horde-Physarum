package net.alekrus.shphysarum.SculkPlayerAbility.InfectedAroundPlayer;



import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SporeBurstPacket {

    public SporeBurstPacket() {}
    public SporeBurstPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    public static SporeBurstPacket decode(FriendlyByteBuf buf) { return new SporeBurstPacket(buf); }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                SporeBurstHandler.toggleAbility(player);
            }
        });
        context.setPacketHandled(true);
    }
}