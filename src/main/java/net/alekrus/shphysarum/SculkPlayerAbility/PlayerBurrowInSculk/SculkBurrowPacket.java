package net.alekrus.shphysarum.SculkPlayerAbility.PlayerBurrowInSculk;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkBurrowPacket {

    public SculkBurrowPacket() {}
    public SculkBurrowPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    public static SculkBurrowPacket decode(FriendlyByteBuf buf) { return new SculkBurrowPacket(buf); }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (InfectionHandler.isInfected(player)) {
                
                SculkBurrowHandler.toggle(player);
            }
        });
        context.setPacketHandled(true);
    }
}
