package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.FaithSystem;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FaithSyncPacket {
    private final int amount;

    public FaithSyncPacket(int amount) {
        this.amount = amount;
    }

    public static FaithSyncPacket decode(FriendlyByteBuf buf) {
        return new FaithSyncPacket(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(amount);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientGravemindState.setFaith(amount);
        });
        context.get().setPacketHandled(true);
    }
}
